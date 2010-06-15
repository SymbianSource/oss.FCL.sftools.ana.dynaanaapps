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
using System.IO;
using System.Drawing;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Management;
using System.Text;
using SymbianUtils.Settings;
using SymbianUtilsUi.Dialogs;
using HeapAnalyser.Engine;
using HeapAnalyser.Engine.Types;
using HeapLib;
using HeapLib.Constants;
using HeapLib.Reconstructor;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.DataSources.Analyser;
using HeapUiLib.Dialogs;
using SymbianWizardLib.Engine;
using SymbianWizardLib.GUI;
using SymbianDebugLib.Engine;
using SymbianDebugLibUi.Controls;

namespace HeapAnalyser.UIs.Graphical.Wizard
{
    public partial class HASetupWizard : Form
    {
		#region Constructors & destructors
        public HASetupWizard( XmlSettings aSettings, HeapWizardEngine aEngine )
		{
			iSettings = aSettings;
			iEngine = aEngine;
			//
			InitializeComponent();
		}
		#endregion

		#region Form event handlers
        private void Form_Load( object sender, System.EventArgs e )
		{
            // Set up version information
            iLbl_Version.Text = HeapLibConstants.Version + " " + HeapLibConstants.Copyright;
			iSettings[ "Wizard", "DialogResult"] = DialogResult.None.ToString();
			//
            Setup_OpType();
            //
            Setup_SourceData_LogFileName();
            Setup_SourceData_HeapDataComparison();
            Setup_SourceData_HeapCSVComparison();
            //
            Setup_Cmn_Symbolics();
            Setup_Cmn_Filters();
            //
            Setup_Output_Directory();
            Setup_Output_File();
		}

		private void Form_Closing( object sender, System.ComponentModel.CancelEventArgs e )
		{
			iSettings.Store();
		}

        private void iWizard_WizardClosedFromAuxillary( SymWizardClosureEvent aEventArgs )
		{
			iSettings[ "Wizard", "DialogResult"] = DialogResult.Cancel.ToString();
		}

        private void iWizard_WizardClosedFromFinish( SymWizardClosureEvent aEventArgs )
		{
			iSettings[ "Wizard", "DialogResult"] = DialogResult.OK.ToString();
		}
		#endregion

        #region Pages

        #region Page [Operation type]
        private void Setup_OpType()
		{
			string opType = iSettings[ "Wizard", "OperationType" ];

			// Make sure something is selected
            if ( opType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps.ToString() )
            {
                iPG1_RB_OpType_CompareHeapDump.Checked = true;
            }
            else if ( opType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapCSV.ToString() )
            {
                iPG1_RB_OpType_CompareCSV.Checked = true;
            }
            else
            {
                // Default
                iPG1_RB_OpType_HeapViewer.Checked = true;
            }
        }

        private void iPG1_OpType_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
		{
			if	( iPG1_RB_OpType_HeapViewer.Checked )
			{
				iEngine.OperationType = HeapWizardEngine.TOperationType.EOperationTypeAnalyseAndView;
                aEventArgs.SuggestedNewPage = iPG_SourceData_Log;
			}
            else if ( iPG1_RB_OpType_CompareHeapDump.Checked )
            {
                iEngine.OperationType = HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps;
                aEventArgs.SuggestedNewPage =  iPG_SourceData_CompareHeapData;
            }
            else if ( iPG1_RB_OpType_CompareCSV.Checked )
			{
				iEngine.OperationType = HeapWizardEngine.TOperationType.EOperationTypeCompareHeapCSV;
                aEventArgs.SuggestedNewPage = iPG_SourceData_CompareCSV;
            }

            Setup_Output_File_Dynamic();
            Setup_Output_Directory_Dynamic();

            iSettings[ "Wizard", "OperationType" ] = iEngine.OperationType.ToString();
		}
		#endregion

		#region Page [SourceData - Log file name]
		private void Setup_SourceData_LogFileName()
		{
            iSettings.Load( "Wizard", iPG_SourceData_Log_FB );
		}

        private void iPG_SourceData_Log_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
		{
            string logFileName = iPG_SourceData_Log_FB.EntityName;

            if ( !iPG_SourceData_Log_FB.IsValid )
            {
                aEventArgs.SuggestedNewPage = iPG_SourceData_Log;
            }
            else
            {
                iSettings.Save( "Wizard", iPG_SourceData_Log_FB );
                //
                iEngine.HeapDataOptions = new Options();
                //
                DataSourceAnalyser analyser = HeapReconstructorDataSourceAnalyserDialog.Analyse( logFileName );
                SeedAnalysisFiltersAfterDataSourceScan( analyser.DataSources, iPG302_Combo_Filter );
			
                // Only allowed to continue if we found a valid source
                if ( iPG302_Combo_Filter.Items.Count > 0 )
                {
                    aEventArgs.SuggestedNewPage = iPG_Cmn_Symbolics;
                }
                else
                {
                    aEventArgs.SuggestedNewPage = aEventArgs.CurrentPage;
                }
			}
		}
		#endregion

        #region Page [SourceData - Heap data comparison source files]
        private void Setup_SourceData_HeapDataComparison()
        {
            iSettings.Load( "Wizard", iPG202_TB_LogFile1 );
            iSettings.Load( "Wizard", iPG202_TB_LogFile2 );

            // Start off with the combo's disabled
            iPG202_Combo_ThreadName1.Enabled = false;
            iPG202_Combo_ThreadName2.Enabled = false;

            // We queue these up so that the scan occurs only after the page is actually displayed.
            this.iPG202_TB_LogFile1.FileSelectionChanged += new SymbianUtilsUi.Controls.SymbianFileControl.FileSelectionChangedHandler( this.iPG202_TB_LogFile1_FileSelectionChanged );
            this.iPG202_TB_LogFile2.FileSelectionChanged += new SymbianUtilsUi.Controls.SymbianFileControl.FileSelectionChangedHandler( this.iPG202_TB_LogFile2_FileSelectionChanged );
        }

        private void iPG202_TB_LogFile1_FileSelectionChanged( SymbianUtilsUi.Controls.SymbianFileControl aSelf, string aFileName )
        {
            DataSourceAnalyser analyser = HeapReconstructorDataSourceAnalyserDialog.Analyse( aFileName );
            SeedAnalysisFiltersAfterDataSourceScan( analyser.DataSources, iPG202_Combo_ThreadName1 );

            iPG_SourceData_CompareHeapData_GP_Log2.Enabled = ( iPG202_Combo_ThreadName1.Items.Count > 0 );
        }

        private void iPG202_TB_LogFile2_FileSelectionChanged( SymbianUtilsUi.Controls.SymbianFileControl aSelf, string aFileName )
        {
            DataSourceAnalyser analyser = HeapReconstructorDataSourceAnalyserDialog.Analyse( aFileName );

            // Get the master thread name
            string threadName = ThreadNameFromFilterCombo( iPG202_Combo_ThreadName1 );
            
            if ( threadName != string.Empty )
            {
            }
            else
            {
                analyser.DataSources.Clear();
            }

            // Seed combobox with filter options
            SeedAnalysisFiltersAfterDataSourceScan( analyser.DataSources, iPG202_Combo_ThreadName2 );
        }

        private void iPG_SourceData_CompareHeapData_PageShownFromButtonNext( SymWizardPage aSender )
        {
            if ( iPG202_TB_LogFile1.EntityName != string.Empty && File.Exists( iPG202_TB_LogFile1.EntityName ) )
            {
                iPG202_TB_LogFile1_FileSelectionChanged( iPG202_TB_LogFile1, iPG202_TB_LogFile1.EntityName );
            }
            if ( iPG202_TB_LogFile2.EntityName != string.Empty && File.Exists( iPG202_TB_LogFile2.EntityName ) )
            {
                iPG202_TB_LogFile2_FileSelectionChanged( iPG202_TB_LogFile2, iPG202_TB_LogFile2.EntityName );
            }
        }

        private void iPG202_SourceData_Comparison_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
        {
            iEngine.ComparisonEngineData.DataSource1 = DataSourceFromFilterCombo( iPG202_Combo_ThreadName1 );
            iEngine.ComparisonEngineData.DataSource2 = DataSourceFromFilterCombo( iPG202_Combo_ThreadName2 );
            //
            if ( !( iPG202_TB_LogFile1.IsValid && iPG202_TB_LogFile2.IsValid ) )
            {
                aEventArgs.SuggestedNewPage =  iPG_SourceData_CompareHeapData;
            }
            else if ( iEngine.ComparisonEngineData.DataSource1 == null )
            {
                iPG202_TB_LogFile1.SetError( "Select a valid MemSpy heap data log" );
                aEventArgs.SuggestedNewPage =  iPG_SourceData_CompareHeapData;
            }
            else if ( iEngine.ComparisonEngineData.DataSource2 == null )
            {
                iPG202_TB_LogFile2.SetError( "Select a valid MemSpy heap data log" );
                aEventArgs.SuggestedNewPage =  iPG_SourceData_CompareHeapData;
            }
            else
            {
                // Get both data sources and check thread names are common
                DataSource ds1 = (DataSource ) iPG202_Combo_ThreadName1.SelectedItem;
                DataSource ds2 = (DataSource) iPG202_Combo_ThreadName2.SelectedItem;
                //
                if ( ds1.ThreadName.ToLower() != ds2.ThreadName.ToLower() )
                {
                    aEventArgs.SuggestedNewPage = iPG_SourceData_CompareHeapData;
                    iErrorProvider.SetError( iPG202_Combo_ThreadName2, "Thread names differ" );
                }
                else
                {
                    iSettings.Save( "Wizard", iPG202_TB_LogFile1 );
                    iSettings.Save( "Wizard", iPG202_TB_LogFile2 );

                    iErrorProvider.Clear();
                    aEventArgs.SuggestedNewPage = iPG_Cmn_Symbolics;
                }
            }
        }
        #endregion

        #region Page [SourceData - Heap CSV comparison source files]
        private void Setup_SourceData_HeapCSVComparison()
        {
            iSettings.Load( "PG_SourceData_CompareCSV_Files", iPG_SourceData_CompareCSV_Files );
        }

        private void iPG_SourceData_CompareCSV_PageClosedFromButtonNext( SymWizardPageTransitionEvent aEventArgs )
        {
            // Check we have at least one valid file.
            if ( iPG_SourceData_CompareCSV_Files.FileNames.Count == 0 )
            {
                aEventArgs.SuggestedNewPage = iPG_SourceData_CompareCSV;
            }
            else
            {
                iSettings.Save( "PG_SourceData_CompareCSV_Files", iPG_SourceData_CompareCSV_Files );
                iEngine.ComparisonEngineCSV.SourceFileNames = iPG_SourceData_CompareCSV_Files.FileNames;
                aEventArgs.SuggestedNewPage = iPG_OutputToDirectory;
            }
        }
        #endregion

        #region Page [Cmn - Symbolics]
        private void Setup_Cmn_Symbolics()
		{
            iPG301_DebugControl.Engine = iEngine.DebugEngine;
		}

        private void iPG301_AnalysisSymbolics_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
		{
            string errorText = string.Empty;
            if ( iPG301_DebugControl.IsReadyToPrime( out errorText ) )
            {
                iPG301_DebugControl.Prime();

                if ( iEngine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps )
                {
                    aEventArgs.SuggestedNewPage = iPG_OutputToFile;
                }
                else
                {
                    // If there is only one thread available, then there's no point
                    // asking the user to pick it...
                    int number = 0;
                    bool okayToProceed = SetDataSourceFromFirstAvailableThread( out number );
                    if ( okayToProceed && number == 1 )
                    {
                        aEventArgs.SuggestedNewPage = iPG_Final;
                    }
                    else
                    {
                        aEventArgs.SuggestedNewPage = iPG_Cmn_Filters;
                    }
                }

                // Also save debug engine configuration
                iPG301_DebugControl.XmlSettingsSave();
            }
            else
            {
                MessageBox.Show( errorText, "Error" );
                aEventArgs.SuggestedNewPage = iPG_Cmn_Symbolics;
            }
		}
		#endregion

        #region Page [Cmn - Heap thread filter]
        private void Setup_Cmn_Filters()
		{
		}

        private void iPG302_Cmn_Filters_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
		{
            if ( SetDataSourceFromFirstAvailableThread() )
            {
                // Clear any error
                iErrorProvider.SetError( iPG302_Combo_Filter, string.Empty );

                // Decide where to go next...
                aEventArgs.SuggestedNewPage = iPG_Final;
            }
            else
            {
                iErrorProvider.SetError( iPG302_Combo_Filter, "No thread's were detected. Is the log corrupt?" );
                aEventArgs.SuggestedNewPage = iPG_Cmn_Filters;
            }
		}
		#endregion

		#region Page [Output - Directory]
        private void Setup_Output_Directory()
		{
            iSettings.Load( "Wizard", iPG_OutputToDirectory_FB );
		}

        private void Setup_Output_Directory_Dynamic()
        {
            iHeader_OutputToDirectory.Title = "Save CSV Comparsion Reports";
            iHeader_OutputToDirectory.Description += "Microsoft Excel comparison reports";
        }

        private void iPG_OutputToDirectory_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
		{
            if ( !iPG_OutputToDirectory_FB.IsValid )
            {
                aEventArgs.SuggestedNewPage = iPG_OutputToDirectory;
            }
            else
            {
                string dir = iPG_OutputToDirectory_FB.EntityName;
                iEngine.ComparisonEngineCSV.OutputDirectory = dir;
                iSettings.Save( "Wizard", iPG_OutputToDirectory_FB );
                aEventArgs.SuggestedNewPage = iPG_Final;
            }
		}
		#endregion

        #region Page [Output - File]
        private void Setup_Output_File()
        {
            iSettings.Load( "Wizard", iPG_OutputToFile_FB );
        }

        private void Setup_Output_File_Dynamic()
        {
            if ( iEngine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps )
            {
                iHeader_OutputToFile.Title = "Save Heap Dump Comparison to Microsoft Excel";
                iHeader_OutputToFile.Description += "Microsoft Excel comparison report";
            }
        }

        private void iPG500_Comparison_Output_CloseFromNext( SymWizardPageTransitionEvent aEventArgs )
        {
            if ( iPG_OutputToFile_FB.IsValid )
            {
                iSettings.Save( "Wizard", iPG_OutputToFile_FB );
                //
                if ( iEngine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps )
                {
                    iEngine.ComparisonEngineData.OutputFileName = iPG_OutputToFile_FB.EntityName;
                }
                //
                aEventArgs.SuggestedNewPage =  iPG_Final;
            }
            else
            {
                aEventArgs.SuggestedNewPage =  iPG_OutputToFile;
            }
        }
        #endregion

        #endregion

        #region Internal methods
        private bool SetDataSourceFromFirstAvailableThread()
        {
            int number;
            return SetDataSourceFromFirstAvailableThread( out number );
        }

        private bool SetDataSourceFromFirstAvailableThread( out int aNumberOfDataSources )
        {
            bool okayToProceed = false;

            // Must be at least one thread selected
            DataSource dataSource = (DataSource) iPG302_Combo_Filter.SelectedItem;
            if ( dataSource == null || iPG302_Combo_Filter.Items.Count == 0 )
            {
                // Can't do anything at this point... Let a later page
                // handle this scenario.
                aNumberOfDataSources = 0;
            }
            else
            {
                aNumberOfDataSources = iPG302_Combo_Filter.Items.Count;
                iEngine.AnalysisEngine.DataSource = dataSource;
                
                // Save setting for filter thread
                iSettings[ "Wizard", iPG302_Combo_Filter.Name ] = iPG302_Combo_Filter.Text;

                // Good to go
                okayToProceed = true;
            }

            return okayToProceed;
        }

        private void SeedAnalysisFiltersAfterDataSourceScan( DataSourceCollection aSources, ComboBox aCombo )
        {
            // Thread filtering - seed with detected threads
            aCombo.BeginUpdate();
            aCombo.Items.Clear();
            foreach ( DataSource source in aSources )
            {
                bool allowSource = true;
                bool containsErrors = CheckSourceForErrors( source, out allowSource );
                if ( !containsErrors || allowSource )
                {
                    aCombo.Items.Add( source );
                }
            }

            // Make sure something is selected
            if ( aCombo.Items.Count > 0 )
            {
                iErrorProvider.SetError( aCombo, string.Empty );

                int selectedThreadIndex = 0;

                // If the user has picked a thread previously, try to select the same one
                // again this time.
                string lastSelectedThread = iSettings[ "Wizard", aCombo.Name ];
                if ( lastSelectedThread.Length > 0 )
                {
                    int index = aSources.IndexOf( lastSelectedThread );
                    if ( index >= 0 )
                    {
                        selectedThreadIndex = index;
                    }
                }

                // Now pick the thread...
                aCombo.SelectedIndex = selectedThreadIndex;
            }
            else
            {
                iErrorProvider.SetError( aCombo, "No thread's were detected. Is the log corrupt?" );
            }

            aCombo.Enabled = ( aCombo.Items.Count > 1 );
            aCombo.EndUpdate();
        }

        private bool CheckSourceForErrors( DataSource aSource, out bool aAllowSourceAnyway )
        {
            aAllowSourceAnyway = false;
            string description = string.Empty;
            //
            bool errorsDetected = aSource.ErrorsDetected( out description );
            if ( errorsDetected )
            {
                StringBuilder msg = new StringBuilder();
                msg.Append( "File: " + aSource.FileName );
                msg.Append( System.Environment.NewLine );
                msg.Append( "Thread: " + aSource.ThreadName );
                msg.Append( System.Environment.NewLine );
                msg.Append( System.Environment.NewLine );
                msg.Append( description );
                msg.Append( System.Environment.NewLine );
                msg.Append( "You are recommended to save the heap data to zip and contact" );
                msg.Append( "your support provider." );
                msg.Append( System.Environment.NewLine );
                msg.Append( System.Environment.NewLine );
                msg.Append( "Do you want to allow this data anyway?" );
                //
                string title = string.Format( "Errors Detected - {0}", aSource.ThreadName );
                DialogResult result = MessageBox.Show( msg.ToString(), title, MessageBoxButtons.YesNo );
                aAllowSourceAnyway = ( result == DialogResult.Yes );
            }
            //
            return errorsDetected;
        }

        private static DataSource DataSourceFromFilterCombo( ComboBox aCombo )
        {
            DataSource ret = null;
            //
            int index = aCombo.SelectedIndex;
            if ( index >= 0 && index < aCombo.Items.Count )
            {
                object obj = aCombo.Items[ index ];
                if ( obj is DataSource )
                {
                    ret = (DataSource) obj;
                }
            }
            //
            return ret;
        }

        private static string ThreadNameFromFilterCombo( ComboBox aCombo )
        {
            string ret = string.Empty;
            //
            DataSource ds = DataSourceFromFilterCombo( aCombo );
            if ( ds != null )
            {
                ret = ds.ThreadName;
            }
            //
            return ret;
        }
        #endregion

        #region Data members
        private readonly HeapWizardEngine iEngine;
		private readonly XmlSettings iSettings;
		#endregion
    }
}
