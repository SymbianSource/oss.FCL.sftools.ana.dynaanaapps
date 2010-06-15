/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright notice,
*   this list of conditions and the following disclaimer.
* - Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* - Neither the name of Nokia Corporation nor the names of its contributors
*   may be used to endorse or promote products derived from this software
*   without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
* 
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

using System;
using System.Text;
using System.IO;
using System.Xml;
using System.Collections.Generic;
using System.Windows.Forms;
using SymbianUtils;
using SymbianUtils.Settings;
using SymbianUtils.Tracer;
using SymbianUtils.FileSystem.Utilities;
using SymbianXmlInputLib.Parser;
using SymbianXmlInputLib.Parser.Nodes;
using SymbianXmlInputLib.Elements;
using SymbianXmlInputLib.Elements.Types.Category;
using SymbianXmlInputLib.Elements.Types.FileSystem;
using SymbianXmlInputLib.Elements.Types.Command;
using SymbianDebugLib.Engine;
using SymbianDebugLib.Entity;
using SymbianStructuresLib.CodeSegments;
using HeapLib;
using HeapLib.Reconstructor;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.DataSources.Analyser;
using HeapUiLib.Dialogs;
using HeapUiLib.Forms;
using HeapAnalyser.Engine;
using HeapAnalyser.Engine.Types;
using HeapAnalyser.Exceptions;
using HeapComparisonUiLib.Progress;
using HeapAnalyser.UIs.Console.Files;
using HeapAnalyser.UIs.Console.Inputs;

namespace HeapAnalyser.UIs.Console
{
	public class HAUIConsole : HAUI
	{
		#region Constructors
        public HAUIConsole( string[] aArgs, XmlSettings aSettings, HeapWizardEngine aEngine, ITracer aTracer )
            : base( aArgs, aSettings, aEngine, aTracer )
		{
            iInputs = new HACmdLineInputParameters( this, aEngine );
		}
		#endregion

        #region From HAUI
        public override bool IsAppropriateUI
        {
            get
            { 
                // There must be command line arguments in order to use the command line UI
                bool ret = base.Args.Length != 0;
                //
                if ( ret )
                {
                    string[] args = base.Args;
                    for ( int i = 0; i < args.Length; i++ )
                    {
                        string cmd = args[ i ].Trim().ToUpper();
                        string nextArg = ( i < args.Length - 1 ? args[ i + 1 ].Trim().ToUpper() : string.Empty );
                        //
                        try
                        {
                            if ( cmd == KPluginInputParameter && nextArg != string.Empty )
                            {
                                ret = true;
                                break;
                            }
                        }
                        catch ( Exception )
                        {
                        }
                    }
                }
                //
                return ret;
            }
        }

        public override Form PrepareInitialForm()
        {
            Form formToShow = null;
            //
            ParseInputs();
            PrimeDebugEngine();
            
            // If we're running the graphical UI then show the main analysis form,
            // otherwise just run the comparison operation and exit.
            switch ( base.Engine.OperationType )
            {
            case HeapWizardEngine.TOperationType.EOperationTypeAnalyseAndView:
                formToShow = RunAnalyser();
                break;
            case HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps:
                RunComparison();
                break;
            }

            return formToShow;
        }

        public override Form HandleFormClosed( object aSender, EventArgs aArgs )
        {
            Form ret = null;
            return ret;
        }
        #endregion

		#region Properties
		#endregion

        #region Event handlers
        private void DbgEngine_EntityPrimingStarted( DbgEngine aEngine, DbgEntity aEntity, object aContext )
        {
            Trace( "[HA Cmd] Priming debug meta-data: " + aEntity.FullName );
        }

        private void DbgEngine_EntityPrimingProgress( DbgEngine aEngine, DbgEntity aEntity, object aContext )
        {
            if ( aContext != null )
            {
                if ( aContext.GetType() == typeof( int ) )
                {
                    int value = (int) aContext;
                    Trace( "[HA Cmd] Priming debug meta-data progress: {0:d3}% {1}", value, aEntity.FullName );
                }
            }
        }

        private void DbgEngine_EntityPrimingComplete( DbgEngine aEngine, DbgEntity aEntity, object aContext )
        {
            Trace( "[HA Cmd] Primed debug meta-data: " + aEntity.FullName );
        }

        private void SymbolLibCodeSegDefinitionResolver_LocatedFile( string aFileName )
        {
            Trace( "[HA Cmd] Located debug meta data: " + aFileName );
        }
        #endregion

        #region Internal constants
        private const string KPluginInputParameter = "-INPUT";
        #endregion

		#region Internal methods
        private void ParseInputs()
        {
            Trace( "[HA Cmd] ParseInputs() - START " );
            Trace( string.Empty );
            Trace( "[HA Cmd] command line: " + System.Environment.CommandLine );
            Trace( "[HA Cmd] command wd:   " + System.Environment.CurrentDirectory );
            Trace( "[HA Cmd] proc count:   " + System.Environment.ProcessorCount );
            Trace( "[HA Cmd] sysdir:       " + System.Environment.SystemDirectory );
            Trace( "[HA Cmd] version:      " + System.Environment.Version.ToString() );
            Trace( string.Empty );

            // We expect to see an "-input" parameter
            string inputFileName = ExtractCommandLineInputParameter( Args );

            // If no file was found then inputFileName will be an empty string.
            if ( string.IsNullOrEmpty( inputFileName ) )
            {
                throw new HAUIException( "Input file parameter missing", HAUIException.KErrCommandLineArgumentsMissing );
            }
            else if ( !FSUtilities.Exists( inputFileName ) )
            {
                throw new HAUIException( "Input file not found", HAUIException.KErrCommandLineArgumentsFileNotFound );
            }
            else
            {
                Trace( "[HA Cmd] ParseInputs() - start read inputs" );
                iInputs.Read( inputFileName );
                Trace( "[HA Cmd] ParseInputs() - inputs read successfully" );

                // Validate the inputs are correct.
                if ( string.IsNullOrEmpty( iInputs.ThreadName ) )
                {
                    throw new HAUIException( "The specified thread name is invalid", HAUIException.KErrCommandLineAnalysisThreadNameInvalid );
                }
                else 
                {
                    // Validate input data
                    int sourceCount = iInputs.SourceFiles.Count;
                    switch( base.Engine.OperationType )
                    {
                    case HeapWizardEngine.TOperationType.EOperationTypeAnalyseAndView:
                        if ( iInputs.SourceFiles.Count != 1 && iInputs.SourceFiles[ 0 ].IsFile )
                        {
                            Trace( "[HA Cmd] ParseInputs() - viewer - missing source file!" );
                            throw new HAUIException( "Source file not specified", HAUIException.KErrCommandLineSourceFileNotFound );
                        }
                        break;
                    case HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps:
                        if ( iInputs.SourceFiles.Count != 2 && iInputs.SourceFiles[ 0 ].IsFile && iInputs.SourceFiles[ 1 ].IsFile )
                        {
                            Trace( "[HA Cmd] ParseInputs() - comparison - missing source files!" );
                            throw new HAUIException( "Source files not specified", HAUIException.KErrCommandLineSourceFileNotFound );
                        }
                        break;
                    }

                    // Validate output (where needed)
                    if ( base.Engine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps )
                    {
                        bool outputSet = iInputs.OutputFile != null;
                        if ( !outputSet )
                        {
                            throw new HAUIException( "Output file not specified", HAUIException.KErrCommandLineAnalysisOutputInvalid );
                        }
                    }
                }
            }

            Trace( "[HA Cmd] ParseInputs() - END" );
        }

        private void PrimeDebugEngine()
        {
            DbgEngine debugEngine = base.Engine.DebugEngine;
            //
            Exception primerException = null;
            HACmdLineFSEntityList<HACmdLineFSEntity> metaDataFiles = iInputs.MetaDataFiles;
            //
            try
            {
                debugEngine.Clear();

                foreach ( HACmdLineFSEntity entry in metaDataFiles )
                {
                    Trace( "[HA Cmd] Seeding debug meta engine with entry: " + entry.Name );
                    DbgEntity entity = debugEngine.Add( entry.Name );
                    if ( entity != null )
                    {
                        Trace( "[HA Cmd] Entry type detected as: [" + entity.CategoryName + "]" );
                        entity.Tag = entry;
                    }
                    else
                    {
                        Trace( "[HA Cmd] Entry not handled: " + entry.Name );
                    }
                }

                // Listen to prime events
                try
                {
                    Trace( "[HA Cmd] Starting prime operation... " );
                    debugEngine.EntityPrimingStarted += new DbgEngine.EventHandler( DbgEngine_EntityPrimingStarted );
                    debugEngine.EntityPrimingProgress += new DbgEngine.EventHandler( DbgEngine_EntityPrimingProgress );
                    debugEngine.EntityPrimingComplete += new DbgEngine.EventHandler( DbgEngine_EntityPrimingComplete );
                    debugEngine.Prime( TSynchronicity.EAsynchronous );
                    Trace( "[HA Cmd] Debug meta data priming completed successfully." );
                }
                finally
                {
                    debugEngine.EntityPrimingStarted -= new DbgEngine.EventHandler( DbgEngine_EntityPrimingStarted );
                    debugEngine.EntityPrimingProgress -= new DbgEngine.EventHandler( DbgEngine_EntityPrimingProgress );
                    debugEngine.EntityPrimingComplete -= new DbgEngine.EventHandler( DbgEngine_EntityPrimingComplete );
                }
            }
            catch ( Exception exception )
            {
                Trace( "[HA Cmd] Debug meta data priming exception: " + exception.Message + ", " + exception.StackTrace );
                primerException = exception;
            }
        }

        private Form RunAnalyser()
        {
            string file = iInputs.SourceFiles[ 0 ].File.FullName;
            base.Engine.AnalysisEngine.DataSource = PrepareDataSource( file );
            base.Engine.AnalysisEngine.CreateParser();
            //
            Form ret = new HeapViewerForm( base.Engine.AnalysisEngine.Reconstructor, base.Settings );
            return ret;
        }

        private void RunComparison()
        {
            // Does the output file already exist? If so, try to delete it.
            // If not, then carry on.
            if ( iInputs.OutputFile.File.Exists )
            {
                // If deletion fails then return an error by way of heap analyser exception.
                try
                {
                    iInputs.OutputFile.File.Delete();
                }
                catch ( Exception )
                {
                    throw new HAUIException( "Could not overwrite output file", HAUIException.KErrCommandLineAnalysisOutputInvalid );
                }
            }

            ComparisonEngineHeapData comparisonEngine = base.Engine.ComparisonEngineData;
            //
            string file1 = iInputs.SourceFiles[ 0 ].File.FullName;
            comparisonEngine.DataSource1 = PrepareDataSource( file1 );
            //
            string file2 = iInputs.SourceFiles[ 1 ].File.FullName;
            comparisonEngine.DataSource2 = PrepareDataSource( file2 );
            //
            comparisonEngine.OutputFileName = iInputs.OutputFile.File.FullName;
            base.Engine.ComparisonEngineData.CreateReconstructors();
            ComparisonProgressDialogData.Compare( comparisonEngine.Reconstructor1,
                                                  comparisonEngine.Reconstructor2,
                                                  comparisonEngine.OutputFileName );

        }

        private DataSource PrepareDataSource( string aFileName )
        {
            DataSourceAnalyser analyser = HeapReconstructorDataSourceAnalyserDialog.Analyse( aFileName );
            DataSourceCollection sources = analyser.DataSources;
            DataSource source = sources[ iInputs.ThreadName ];
            if ( source == null )
            {
                throw new HAUIException( "Thread was not found in source data", HAUIException.KErrCommandLineAnalysisThreadNameInvalid );
            }
            //
            return source;
        }

        private static string ExtractCommandLineInputParameter( string[] aArgs )
        {
            string ret = string.Empty;

            // -input d:\ca_fullsummary.xml
            for ( int i = 0; i < aArgs.Length; i++ )
            {
                string cmd = aArgs[ i ].Trim().ToUpper();
                string nextArg = ( i < aArgs.Length - 1 ? aArgs[ i + 1 ].Trim().ToUpper() : string.Empty );
                //
                if ( cmd == KPluginInputParameter && nextArg != string.Empty )
                {
                    ret = nextArg;
                }
            }

            return ret;
        }
        #endregion

		#region Data members
        private readonly HACmdLineInputParameters iInputs;
        #endregion
	}
}
