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
using System.Collections.Generic;
using SymbianUtils.Tracer;
using SymbianUtils.FileSystem.Utilities;
using SymbianXmlInputLib.Parser;
using SymbianXmlInputLib.Parser.Nodes;
using SymbianXmlInputLib.Elements;
using SymbianXmlInputLib.Elements.Types.Category;
using SymbianXmlInputLib.Elements.Types.Extension;
using SymbianXmlInputLib.Elements.Types.FileSystem;
using SymbianXmlInputLib.Elements.Types.Command;
using HeapAnalyser.UIs.Console.Files;
using HeapAnalyser.Exceptions;
using HeapAnalyser.Engine;

namespace HeapAnalyser.UIs.Console.Inputs
{
	internal class HACmdLineInputParameters
    {
        #region Constructors & destructor
        public HACmdLineInputParameters( ITracer aTracer, HeapWizardEngine aEngine )
		{
            iEngine = aEngine;
            iTracer = aTracer;
		}
		#endregion

		#region API
        public void Read( string aFileName )
        {
            Trace( "[CmdInput] Read() - aFileName: " + aFileName );
            try
            {
                // First create the tree
                SXILDocument doc = CreateDocumentTree( aFileName );

                // Then convert it to the list of elements that we care about
                ExtractData( doc );
            }
            catch ( HAUIException cmdLineException )
            {
                Trace( "[CmdInput] Read() - HAUIException: " + cmdLineException.Message + " " + cmdLineException.StackTrace );
                throw cmdLineException;
            }
            catch ( Exception generalException )
            {
                Trace( "[CmdInput] Read() - generalException: " + generalException.Message + " " + generalException.StackTrace );
                throw new HAUIException( "Error reading input xml file", generalException, HAUIException.KErrCommandLineArgumentsFileInvalid );
            }
            Trace( "[CmdInput] Read() - read OK: " + aFileName );
        }
        #endregion

		#region Properties
        public string ThreadName
        {
            get { return iThreadName; }
        }

        public HACmdLineFSEntity OutputFile
        {
            get { return iOutputFile; }
        }

        public HACmdLineFSEntityList<HACmdLineFileSource> SourceFiles
        {
            get { return iSources; }
        }

        public HACmdLineFSEntityList<HACmdLineFSEntity> MetaDataFiles
        {
            get { return iMetaData; }
        }
        #endregion

        #region Internal constants
        private const string KInputFileDocumentRootNode = "heap_analysis";
        private const string KInputFileCategorySource = "source";
        private const string KInputFileCategoryDebugMetaData = "debug_meta_data";
        private const string KInputFileCategoryParameters = "parameters";
        private const string KInputFileCategoryOutput = "output";
        private const string KInputFileCommandThread = "thread";
        private const string KInputFileCommandNameAnalysis = "analysis_type";
        private const string KInputFileCommandNameAnalysisViewer = "VIEWER";
        private const string KInputFileCommandNameAnalysisCompareTwoHeaps = "COMPARETWOHEAPS";
        #endregion

        #region Internal methods
        private SXILDocument CreateDocumentTree( string aFileName )
        {
            SXILDocument doc = new SXILDocument();

            // Read input file into document
            using ( SXILParser parser = new SXILParser( aFileName, KInputFileDocumentRootNode, doc ) )
            {
                parser.CategoryAdd( KInputFileCategorySource, new SXILParserNodeFileSystem() );
                parser.CategoryAdd( KInputFileCategoryDebugMetaData, new SXILParserNodeFileSystem() );
                parser.CategoryAdd( KInputFileCategoryParameters,
                    new SXILParserNodeCommand( KInputFileCommandNameAnalysis ),
                    new SXILParserNodeCommand( KInputFileCommandThread )
                    );
                parser.CategoryAdd( KInputFileCategoryOutput, new SXILParserNodeFileSystem() );
                parser.Parse();
            }

            return doc;
        }

        private void ExtractData( SXILDocument aDocument )
        {
            foreach ( SXILElement element in aDocument )
            {
                if ( element is SXILElementCategory )
                {
                    SXILElementCategory category = (SXILElementCategory) element;
                    string name = category.Name.ToLower();
                    //
                    switch ( name )
                    {
                    case KInputFileCategorySource:
                        ExtractFileList<HACmdLineFileSource>( iSources, category, true );
                        break;
                    case KInputFileCategoryDebugMetaData:
                        ExtractFileList<HACmdLineFSEntity>( iMetaData, category, false );
                        break;
                    case KInputFileCategoryParameters:
                        ExtractParameters( category );
                        break;
                    case KInputFileCategoryOutput:
                        ExtractOutput( category );
                        break;
                    }
                }
            }

            // We don't require debug meta data if performing a summary operation. Otherwise, we do.
            if ( iMetaData.Count == 0 )
            {
                Trace( "[CmdInput] ExtractData() - no debug meta data supplied!" );
                throw new HAUIException( "Debug meta-data not present", HAUIException.KErrCommandLineDebugMetaDataMissing );
            }
        
        }

        private void ExtractFileList<T>( HACmdLineFSEntityList<T> aList, SXILElementCategory aCategory, bool aExpandDirectoriesToUnderlyingFiles ) where T : HACmdLineFSEntity, new()
        {
            foreach ( SXILElement element in aCategory )
            {
                if ( element is SXILElementFile )
                {
                    SXILElementFile file = (SXILElementFile) element;
                    Trace( "[CmdInput] ExtractFileList() - file: " + file );
                    if ( !file.Exists )
                    {
                        throw new FileNotFoundException( "File not found", file.Name );
                    }
                    //
                    aList.Add( file );
                }
                else if ( element is SXILElementDirectory )
                {
                    SXILElementDirectory dir = (SXILElementDirectory) element;
                    Trace( "[CmdInput] ExtractFileList() - dir:  " + dir );
                    if ( !dir.Exists )
                    {
                        throw new DirectoryNotFoundException( "Directory not found: " + dir.Name );
                    }
                    //
                    if ( aExpandDirectoriesToUnderlyingFiles )
                    {
                        aList.AddRange( dir.Files );
                    }
                    else
                    {
                        aList.Add( dir.Directory );
                    }
                }
            }
        }

        private void ExtractParameters( SXILElementCategory aCategory )
        {
            foreach ( SXILElement element in aCategory )
            {
                if ( element is SXILElementCommand )
                {
                    SXILElementCommand entry = (SXILElementCommand) element;
                    //
                    string type = entry.Details.Trim();
                    if ( entry.Name == KInputFileCommandNameAnalysis )
                    {
                        Trace( "[CmdInput] ExtractFileList() - command: " + type );
                        switch ( type.ToUpper() )
                        {
                        case KInputFileCommandNameAnalysisViewer:
                            iEngine.OperationType = HeapWizardEngine.TOperationType.EOperationTypeAnalyseAndView;
                            break;
                        case KInputFileCommandNameAnalysisCompareTwoHeaps:
                            iEngine.OperationType = HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps;
                            break;
                        default:
                            throw new HAUIException( "Unsupported analysis type", HAUIException.KErrCommandLineAnalysisTypeNotSupported );
                        }
                    }
                    else if ( entry.Name == KInputFileCommandThread )
                    {
                        iThreadName = type;
                    }
                    else
                    {
                        throw new HAUIException( "Unsupported command: " + entry.Name, HAUIException.KErrCommandLineInvalidCommand );
                    }
                }
            }
        }

        private void ExtractOutput( SXILElementCategory aCategory )
        {
            // We either output to file or directory - if both are present then bail out
            iOutputFile = null;
            //
            foreach ( SXILElement element in aCategory )
            {
                if ( element is SXILElementFile )
                {
                    if ( iOutputFile != null )
                    {
                        throw new HAUIException( "Output specified twice", HAUIException.KErrCommandLineAnalysisOutputInvalid );
                    }
                    else
                    {
                        SXILElementFile file = (SXILElementFile) element;
                        iOutputFile = new HACmdLineFSEntity();
                        iOutputFile.File = new FileInfo( file.Name );
                    }
                }
                else if ( element is SXILElementDirectory )
                {
                    throw new HAUIException( "Cannot output directory", HAUIException.KErrCommandLineAnalysisOutputInvalid );
                }
            }
        }

        public void Trace( string aMessage )
        {
            iTracer.Trace( aMessage );
        }

        public void Trace( string aFormat, params object[] aParams )
        {
            string text = string.Format( aFormat, aParams );
            Trace( text );
        }
        #endregion

        #region Data members
        private readonly ITracer iTracer;
        private readonly HeapWizardEngine iEngine;
        private string iThreadName = string.Empty;
        private HACmdLineFSEntity iOutputFile = null;
        private HACmdLineFSEntityList<HACmdLineFSEntity> iMetaData = new HACmdLineFSEntityList<HACmdLineFSEntity>();
        private HACmdLineFSEntityList<HACmdLineFileSource> iSources = new HACmdLineFSEntityList<HACmdLineFileSource>();
        #endregion
	}
}
