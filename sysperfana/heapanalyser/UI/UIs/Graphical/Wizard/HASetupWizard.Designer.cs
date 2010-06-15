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

﻿namespace HeapAnalyser.UIs.Graphical.Wizard
{
    partial class HASetupWizard
    {
        // <summary>
        // Required designer variable.
        // </summary>
        private System.ComponentModel.IContainer components = null;

        // <summary>
        // Clean up any resources being used.
        // </summary>
        // <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose( bool disposing )
        {
            if ( disposing && ( components != null ) )
            {
                components.Dispose();
            }
            base.Dispose( disposing );
        }

        #region Windows Form Designer generated code

        // <summary>
        // Required method for Designer support - do not modify
        // the contents of this method with the code editor.
        // </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(HASetupWizard));
            this.iWizard = new SymbianWizardLib.GUI.SymWizard();
            this.iPG_OpType = new SymbianWizardLib.GUI.SymWizardPage();
            this.iPG1_OpType_GroupBox = new System.Windows.Forms.GroupBox();
            this.iPG1_OpType_FLP = new System.Windows.Forms.FlowLayoutPanel();
            this.iPG1_RB_OpType_HeapViewer = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_OpType_CompareHeapDump = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_OpType_CompareCSV = new System.Windows.Forms.RadioButton();
            this.iLbl_Version = new System.Windows.Forms.Label();
            this.header4 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_SourceData_Log = new SymbianWizardLib.GUI.SymWizardPage();
            this.groupBox7 = new System.Windows.Forms.GroupBox();
            this.iPG_SourceData_Log_FB = new SymbianUtilsUi.Controls.SymbianFileBrowserControl();
            this.header5 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_SourceData_CompareCSV = new SymbianWizardLib.GUI.SymWizardPage();
            this.iPG_SourceData_CompareCSV_Files = new SymbianUtilsUi.Controls.SymbianFileListManagementControl();
            this.symWizardHeaderSection1 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_SourceData_CompareHeapData = new SymbianWizardLib.GUI.SymWizardPage();
            this.iPG_SourceData_CompareHeapData_GP_Log2 = new System.Windows.Forms.GroupBox();
            this.iPG202_Combo_ThreadName2 = new System.Windows.Forms.ComboBox();
            this.iPG202_TB_LogFile2 = new SymbianUtilsUi.Controls.SymbianFileBrowserControl();
            this.label3 = new System.Windows.Forms.Label();
            this.iPG_SourceData_CompareHeapData_GP_Log1 = new System.Windows.Forms.GroupBox();
            this.iPG202_Combo_ThreadName1 = new System.Windows.Forms.ComboBox();
            this.label2 = new System.Windows.Forms.Label();
            this.iPG202_TB_LogFile1 = new SymbianUtilsUi.Controls.SymbianFileBrowserControl();
            this.header3 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_Cmn_Symbolics = new SymbianWizardLib.GUI.SymWizardPage();
            this.iPG301_DebugControl = new SymbianDebugLibUi.Controls.DebugEngineControl();
            this.header7 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_Cmn_Filters = new SymbianWizardLib.GUI.SymWizardPage();
            this.groupBox20 = new System.Windows.Forms.GroupBox();
            this.label8 = new System.Windows.Forms.Label();
            this.iPG302_Combo_Filter = new System.Windows.Forms.ComboBox();
            this.label41 = new System.Windows.Forms.Label();
            this.header6 = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_OutputToFile = new SymbianWizardLib.GUI.SymWizardPage();
            this.groupBox8 = new System.Windows.Forms.GroupBox();
            this.iPG_OutputToFile_FB = new SymbianUtilsUi.Controls.SymbianFileSaveAsControl();
            this.iHeader_OutputToFile = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_OutputToDirectory = new SymbianWizardLib.GUI.SymWizardPage();
            this.groupBox9 = new System.Windows.Forms.GroupBox();
            this.iPG_OutputToDirectory_FB = new SymbianUtilsUi.Controls.SymbianFolderBrowserControl();
            this.iHeader_OutputToDirectory = new SymbianWizardLib.GUI.SymWizardHeaderSection();
            this.iPG_Final = new SymbianWizardLib.GUI.SymWizardPage();
            this.iPG5000_InfoControl = new SymbianWizardLib.GUI.SymWizardInfoControl();
            this.label1 = new System.Windows.Forms.Label();
            this.iErrorProvider = new System.Windows.Forms.ErrorProvider(this.components);
            this.iPG_OpType.SuspendLayout();
            this.iPG1_OpType_GroupBox.SuspendLayout();
            this.iPG1_OpType_FLP.SuspendLayout();
            this.iPG_SourceData_Log.SuspendLayout();
            this.groupBox7.SuspendLayout();
            this.iPG_SourceData_CompareCSV.SuspendLayout();
            this.iPG_SourceData_CompareHeapData.SuspendLayout();
            this.iPG_SourceData_CompareHeapData_GP_Log2.SuspendLayout();
            this.iPG_SourceData_CompareHeapData_GP_Log1.SuspendLayout();
            this.iPG_Cmn_Symbolics.SuspendLayout();
            this.iPG_Cmn_Filters.SuspendLayout();
            this.groupBox20.SuspendLayout();
            this.iPG_OutputToFile.SuspendLayout();
            this.groupBox8.SuspendLayout();
            this.iPG_OutputToDirectory.SuspendLayout();
            this.groupBox9.SuspendLayout();
            this.iPG_Final.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.iErrorProvider)).BeginInit();
            this.SuspendLayout();
            // 
            // iWizard
            // 
            this.iWizard.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iWizard.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.iWizard.Location = new System.Drawing.Point(0, 0);
            this.iWizard.Margin = new System.Windows.Forms.Padding(0);
            this.iWizard.Name = "iWizard";
            this.iWizard.Pages.AddRange(new SymbianWizardLib.GUI.SymWizardPage[] {
            this.iPG_OpType,
            this.iPG_SourceData_Log,
            this.iPG_SourceData_CompareCSV,
            this.iPG_SourceData_CompareHeapData,
            this.iPG_Cmn_Symbolics,
            this.iPG_Cmn_Filters,
            this.iPG_OutputToFile,
            this.iPG_OutputToDirectory,
            this.iPG_Final});
            this.iWizard.Size = new System.Drawing.Size(573, 497);
            this.iWizard.TabIndex = 0;
            this.iWizard.WizardClosedFromFinish += new SymbianWizardLib.GUI.SymWizard.WizardClosedFromFinishHandler(this.iWizard_WizardClosedFromFinish);
            this.iWizard.WizardClosedFromAuxillary += new SymbianWizardLib.GUI.SymWizard.WizardClosedFromAuxillaryHandler(this.iWizard_WizardClosedFromAuxillary);
            // 
            // iPG_OpType
            // 
            this.iPG_OpType.Controls.Add(this.iPG1_OpType_GroupBox);
            this.iPG_OpType.Controls.Add(this.iLbl_Version);
            this.iPG_OpType.Controls.Add(this.header4);
            this.iPG_OpType.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_OpType.Location = new System.Drawing.Point(0, 0);
            this.iPG_OpType.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_OpType.Name = "iPG_OpType";
            this.iPG_OpType.Size = new System.Drawing.Size(573, 441);
            this.iPG_OpType.TabIndex = 8;
            this.iPG_OpType.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG1_OpType_CloseFromNext);
            // 
            // iPG1_OpType_GroupBox
            // 
            this.iPG1_OpType_GroupBox.AutoSize = true;
            this.iPG1_OpType_GroupBox.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.iPG1_OpType_GroupBox.Controls.Add(this.iPG1_OpType_FLP);
            this.iPG1_OpType_GroupBox.Location = new System.Drawing.Point(139, 162);
            this.iPG1_OpType_GroupBox.Name = "iPG1_OpType_GroupBox";
            this.iPG1_OpType_GroupBox.Size = new System.Drawing.Size(270, 131);
            this.iPG1_OpType_GroupBox.TabIndex = 4;
            this.iPG1_OpType_GroupBox.TabStop = false;
            this.iPG1_OpType_GroupBox.Text = " Operation type ";
            // 
            // iPG1_OpType_FLP
            // 
            this.iPG1_OpType_FLP.AutoSize = true;
            this.iPG1_OpType_FLP.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.iPG1_OpType_FLP.Controls.Add(this.iPG1_RB_OpType_HeapViewer);
            this.iPG1_OpType_FLP.Controls.Add(this.iPG1_RB_OpType_CompareHeapDump);
            this.iPG1_OpType_FLP.Controls.Add(this.iPG1_RB_OpType_CompareCSV);
            this.iPG1_OpType_FLP.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_OpType_FLP.FlowDirection = System.Windows.Forms.FlowDirection.TopDown;
            this.iPG1_OpType_FLP.Location = new System.Drawing.Point(3, 17);
            this.iPG1_OpType_FLP.Name = "iPG1_OpType_FLP";
            this.iPG1_OpType_FLP.Padding = new System.Windows.Forms.Padding(15);
            this.iPG1_OpType_FLP.Size = new System.Drawing.Size(264, 111);
            this.iPG1_OpType_FLP.TabIndex = 2;
            this.iPG1_OpType_FLP.WrapContents = false;
            // 
            // iPG1_RB_OpType_HeapViewer
            // 
            this.iPG1_RB_OpType_HeapViewer.AutoSize = true;
            this.iPG1_RB_OpType_HeapViewer.Checked = true;
            this.iPG1_RB_OpType_HeapViewer.Location = new System.Drawing.Point(18, 18);
            this.iPG1_RB_OpType_HeapViewer.Name = "iPG1_RB_OpType_HeapViewer";
            this.iPG1_RB_OpType_HeapViewer.Padding = new System.Windows.Forms.Padding(2);
            this.iPG1_RB_OpType_HeapViewer.Size = new System.Drawing.Size(228, 21);
            this.iPG1_RB_OpType_HeapViewer.TabIndex = 2;
            this.iPG1_RB_OpType_HeapViewer.TabStop = true;
            this.iPG1_RB_OpType_HeapViewer.Text = "Interactively inspect MemSpy Heap Dump";
            // 
            // iPG1_RB_OpType_CompareHeapDump
            // 
            this.iPG1_RB_OpType_CompareHeapDump.AutoSize = true;
            this.iPG1_RB_OpType_CompareHeapDump.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.iPG1_RB_OpType_CompareHeapDump.Location = new System.Drawing.Point(18, 45);
            this.iPG1_RB_OpType_CompareHeapDump.Name = "iPG1_RB_OpType_CompareHeapDump";
            this.iPG1_RB_OpType_CompareHeapDump.Padding = new System.Windows.Forms.Padding(2);
            this.iPG1_RB_OpType_CompareHeapDump.Size = new System.Drawing.Size(199, 21);
            this.iPG1_RB_OpType_CompareHeapDump.TabIndex = 6;
            this.iPG1_RB_OpType_CompareHeapDump.Text = "Compare two MemSpy Heap Dumps";
            // 
            // iPG1_RB_OpType_CompareCSV
            // 
            this.iPG1_RB_OpType_CompareCSV.AutoSize = true;
            this.iPG1_RB_OpType_CompareCSV.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.iPG1_RB_OpType_CompareCSV.Location = new System.Drawing.Point(18, 72);
            this.iPG1_RB_OpType_CompareCSV.Name = "iPG1_RB_OpType_CompareCSV";
            this.iPG1_RB_OpType_CompareCSV.Padding = new System.Windows.Forms.Padding(2);
            this.iPG1_RB_OpType_CompareCSV.Size = new System.Drawing.Size(226, 21);
            this.iPG1_RB_OpType_CompareCSV.TabIndex = 6;
            this.iPG1_RB_OpType_CompareCSV.Text = "Compare MemSpy Compact Heap Listings";
            // 
            // iLbl_Version
            // 
            this.iLbl_Version.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.iLbl_Version.Location = new System.Drawing.Point(200, 385);
            this.iLbl_Version.Name = "iLbl_Version";
            this.iLbl_Version.Size = new System.Drawing.Size(361, 45);
            this.iLbl_Version.TabIndex = 3;
            this.iLbl_Version.Text = "Copyright && version information go here";
            // 
            // header4
            // 
            this.header4.BackColor = System.Drawing.SystemColors.Window;
            this.header4.CausesValidation = false;
            this.header4.Description = resources.GetString("header4.Description");
            this.header4.Dock = System.Windows.Forms.DockStyle.Top;
            this.header4.Image = null;
            this.header4.Location = new System.Drawing.Point(0, 0);
            this.header4.Margin = new System.Windows.Forms.Padding(0);
            this.header4.Name = "header4";
            this.header4.Size = new System.Drawing.Size(573, 87);
            this.header4.TabIndex = 1;
            this.header4.Title = "Operation Type";
            // 
            // iPG_SourceData_Log
            // 
            this.iPG_SourceData_Log.Controls.Add(this.groupBox7);
            this.iPG_SourceData_Log.Controls.Add(this.header5);
            this.iPG_SourceData_Log.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_SourceData_Log.Location = new System.Drawing.Point(0, 0);
            this.iPG_SourceData_Log.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_SourceData_Log.Name = "iPG_SourceData_Log";
            this.iPG_SourceData_Log.Size = new System.Drawing.Size(573, 441);
            this.iPG_SourceData_Log.TabIndex = 9;
            this.iPG_SourceData_Log.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG_SourceData_Log_CloseFromNext);
            // 
            // groupBox7
            // 
            this.groupBox7.Controls.Add(this.iPG_SourceData_Log_FB);
            this.groupBox7.Location = new System.Drawing.Point(16, 80);
            this.groupBox7.Name = "groupBox7";
            this.groupBox7.Size = new System.Drawing.Size(536, 58);
            this.groupBox7.TabIndex = 8;
            this.groupBox7.TabStop = false;
            this.groupBox7.Text = " Log File ";
            // 
            // iPG_SourceData_Log_FB
            // 
            this.iPG_SourceData_Log_FB.DialogFilter = "Log Files (*.log;*.txt)|*.log;*.txt|All Files (*.*)|*.*";
            this.iPG_SourceData_Log_FB.DialogTitle = "Select Log";
            this.iPG_SourceData_Log_FB.Location = new System.Drawing.Point(17, 22);
            this.iPG_SourceData_Log_FB.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_SourceData_Log_FB.MinimumSize = new System.Drawing.Size(396, 21);
            this.iPG_SourceData_Log_FB.Name = "iPG_SourceData_Log_FB";
            this.iPG_SourceData_Log_FB.Size = new System.Drawing.Size(506, 21);
            this.iPG_SourceData_Log_FB.TabIndex = 10;
            // 
            // header5
            // 
            this.header5.BackColor = System.Drawing.SystemColors.Window;
            this.header5.CausesValidation = false;
            this.header5.Description = "Select the file to be analysed";
            this.header5.Dock = System.Windows.Forms.DockStyle.Top;
            this.header5.Image = null;
            this.header5.Location = new System.Drawing.Point(0, 0);
            this.header5.Name = "header5";
            this.header5.Size = new System.Drawing.Size(573, 64);
            this.header5.TabIndex = 1;
            this.header5.Title = "Log Analysis";
            // 
            // iPG_SourceData_CompareCSV
            // 
            this.iPG_SourceData_CompareCSV.Controls.Add(this.iPG_SourceData_CompareCSV_Files);
            this.iPG_SourceData_CompareCSV.Controls.Add(this.symWizardHeaderSection1);
            this.iPG_SourceData_CompareCSV.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_SourceData_CompareCSV.Location = new System.Drawing.Point(0, 0);
            this.iPG_SourceData_CompareCSV.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_SourceData_CompareCSV.Name = "iPG_SourceData_CompareCSV";
            this.iPG_SourceData_CompareCSV.Size = new System.Drawing.Size(573, 441);
            this.iPG_SourceData_CompareCSV.TabIndex = 3;
            this.iPG_SourceData_CompareCSV.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG_SourceData_CompareCSV_PageClosedFromButtonNext);
            // 
            // iPG_SourceData_CompareCSV_Files
            // 
            this.iPG_SourceData_CompareCSV_Files.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.iPG_SourceData_CompareCSV_Files.DialogFilter = "Compact Heap Data files (*.txt;*.log;*.csv)|*.txt;*.log;*.csv|All files (*.*)|*.*" +
                "";
            this.iPG_SourceData_CompareCSV_Files.DialogMultiselect = true;
            this.iPG_SourceData_CompareCSV_Files.DialogTitle = "Select files to compare...";
            this.iPG_SourceData_CompareCSV_Files.FileName = "";
            this.iPG_SourceData_CompareCSV_Files.FileNames = ((System.Collections.Generic.List<string>)(resources.GetObject("iPG_SourceData_CompareCSV_Files.FileNames")));
            this.iPG_SourceData_CompareCSV_Files.Location = new System.Drawing.Point(16, 77);
            this.iPG_SourceData_CompareCSV_Files.Name = "iPG_SourceData_CompareCSV_Files";
            this.iPG_SourceData_CompareCSV_Files.Size = new System.Drawing.Size(540, 357);
            this.iPG_SourceData_CompareCSV_Files.TabIndex = 13;
            // 
            // symWizardHeaderSection1
            // 
            this.symWizardHeaderSection1.BackColor = System.Drawing.SystemColors.Window;
            this.symWizardHeaderSection1.CausesValidation = false;
            this.symWizardHeaderSection1.Description = "Select the heap data CSV or trace files to be compared";
            this.symWizardHeaderSection1.Dock = System.Windows.Forms.DockStyle.Top;
            this.symWizardHeaderSection1.Image = null;
            this.symWizardHeaderSection1.Location = new System.Drawing.Point(0, 0);
            this.symWizardHeaderSection1.Name = "symWizardHeaderSection1";
            this.symWizardHeaderSection1.Size = new System.Drawing.Size(573, 64);
            this.symWizardHeaderSection1.TabIndex = 10;
            this.symWizardHeaderSection1.Title = "Compare MemSpy CSV Data";
            // 
            // iPG_SourceData_CompareHeapData
            // 
            this.iPG_SourceData_CompareHeapData.Controls.Add(this.iPG_SourceData_CompareHeapData_GP_Log2);
            this.iPG_SourceData_CompareHeapData.Controls.Add(this.iPG_SourceData_CompareHeapData_GP_Log1);
            this.iPG_SourceData_CompareHeapData.Controls.Add(this.header3);
            this.iPG_SourceData_CompareHeapData.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_SourceData_CompareHeapData.Location = new System.Drawing.Point(0, 0);
            this.iPG_SourceData_CompareHeapData.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_SourceData_CompareHeapData.Name = "iPG_SourceData_CompareHeapData";
            this.iPG_SourceData_CompareHeapData.Size = new System.Drawing.Size(573, 441);
            this.iPG_SourceData_CompareHeapData.TabIndex = 18;
            this.iPG_SourceData_CompareHeapData.PageShownFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageShownFromButtonNextHandler(this.iPG_SourceData_CompareHeapData_PageShownFromButtonNext);
            this.iPG_SourceData_CompareHeapData.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG202_SourceData_Comparison_CloseFromNext);
            // 
            // iPG_SourceData_CompareHeapData_GP_Log2
            // 
            this.iPG_SourceData_CompareHeapData_GP_Log2.Controls.Add(this.iPG202_Combo_ThreadName2);
            this.iPG_SourceData_CompareHeapData_GP_Log2.Controls.Add(this.iPG202_TB_LogFile2);
            this.iPG_SourceData_CompareHeapData_GP_Log2.Controls.Add(this.label3);
            this.iPG_SourceData_CompareHeapData_GP_Log2.Location = new System.Drawing.Point(16, 187);
            this.iPG_SourceData_CompareHeapData_GP_Log2.Name = "iPG_SourceData_CompareHeapData_GP_Log2";
            this.iPG_SourceData_CompareHeapData_GP_Log2.Size = new System.Drawing.Size(536, 91);
            this.iPG_SourceData_CompareHeapData_GP_Log2.TabIndex = 9;
            this.iPG_SourceData_CompareHeapData_GP_Log2.TabStop = false;
            this.iPG_SourceData_CompareHeapData_GP_Log2.Text = " Log File 2";
            // 
            // iPG202_Combo_ThreadName2
            // 
            this.iPG202_Combo_ThreadName2.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.iPG202_Combo_ThreadName2.FormattingEnabled = true;
            this.iPG202_Combo_ThreadName2.Location = new System.Drawing.Point(95, 57);
            this.iPG202_Combo_ThreadName2.MaxDropDownItems = 14;
            this.iPG202_Combo_ThreadName2.Name = "iPG202_Combo_ThreadName2";
            this.iPG202_Combo_ThreadName2.Size = new System.Drawing.Size(376, 21);
            this.iPG202_Combo_ThreadName2.TabIndex = 32;
            // 
            // iPG202_TB_LogFile2
            // 
            this.iPG202_TB_LogFile2.DialogFilter = "Log Files (*.log;*.txt)|*.log;*.txt|All Files (*.*)|*.*";
            this.iPG202_TB_LogFile2.DialogTitle = "Select Log 2";
            this.iPG202_TB_LogFile2.Location = new System.Drawing.Point(17, 22);
            this.iPG202_TB_LogFile2.Margin = new System.Windows.Forms.Padding(0);
            this.iPG202_TB_LogFile2.MinimumSize = new System.Drawing.Size(396, 21);
            this.iPG202_TB_LogFile2.Name = "iPG202_TB_LogFile2";
            this.iPG202_TB_LogFile2.Size = new System.Drawing.Size(506, 21);
            this.iPG202_TB_LogFile2.TabIndex = 0;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(17, 60);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(73, 13);
            this.label3.TabIndex = 31;
            this.label3.Text = "Thread name:";
            // 
            // iPG_SourceData_CompareHeapData_GP_Log1
            // 
            this.iPG_SourceData_CompareHeapData_GP_Log1.Controls.Add(this.iPG202_Combo_ThreadName1);
            this.iPG_SourceData_CompareHeapData_GP_Log1.Controls.Add(this.label2);
            this.iPG_SourceData_CompareHeapData_GP_Log1.Controls.Add(this.iPG202_TB_LogFile1);
            this.iPG_SourceData_CompareHeapData_GP_Log1.Location = new System.Drawing.Point(16, 80);
            this.iPG_SourceData_CompareHeapData_GP_Log1.Name = "iPG_SourceData_CompareHeapData_GP_Log1";
            this.iPG_SourceData_CompareHeapData_GP_Log1.Size = new System.Drawing.Size(536, 91);
            this.iPG_SourceData_CompareHeapData_GP_Log1.TabIndex = 9;
            this.iPG_SourceData_CompareHeapData_GP_Log1.TabStop = false;
            this.iPG_SourceData_CompareHeapData_GP_Log1.Text = " Log File 1";
            // 
            // iPG202_Combo_ThreadName1
            // 
            this.iPG202_Combo_ThreadName1.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.iPG202_Combo_ThreadName1.FormattingEnabled = true;
            this.iPG202_Combo_ThreadName1.Location = new System.Drawing.Point(95, 57);
            this.iPG202_Combo_ThreadName1.MaxDropDownItems = 14;
            this.iPG202_Combo_ThreadName1.Name = "iPG202_Combo_ThreadName1";
            this.iPG202_Combo_ThreadName1.Size = new System.Drawing.Size(376, 21);
            this.iPG202_Combo_ThreadName1.TabIndex = 32;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(17, 60);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(73, 13);
            this.label2.TabIndex = 31;
            this.label2.Text = "Thread name:";
            // 
            // iPG202_TB_LogFile1
            // 
            this.iPG202_TB_LogFile1.DialogFilter = "Log Files (*.log;*.txt)|*.log;*.txt|All Files (*.*)|*.*";
            this.iPG202_TB_LogFile1.DialogTitle = "Select Log 1";
            this.iPG202_TB_LogFile1.Location = new System.Drawing.Point(17, 22);
            this.iPG202_TB_LogFile1.Margin = new System.Windows.Forms.Padding(0);
            this.iPG202_TB_LogFile1.MinimumSize = new System.Drawing.Size(396, 21);
            this.iPG202_TB_LogFile1.Name = "iPG202_TB_LogFile1";
            this.iPG202_TB_LogFile1.Size = new System.Drawing.Size(506, 21);
            this.iPG202_TB_LogFile1.TabIndex = 0;
            // 
            // header3
            // 
            this.header3.BackColor = System.Drawing.SystemColors.Window;
            this.header3.CausesValidation = false;
            this.header3.Description = "Select the heap data log files to be compared. A common thread must exist in each" +
                " log file so that two heap dumps can be directly compared.";
            this.header3.Dock = System.Windows.Forms.DockStyle.Top;
            this.header3.Image = null;
            this.header3.Location = new System.Drawing.Point(0, 0);
            this.header3.Name = "header3";
            this.header3.Size = new System.Drawing.Size(573, 64);
            this.header3.TabIndex = 2;
            this.header3.Title = "Compare MemSpy Heap Dumps";
            // 
            // iPG_Cmn_Symbolics
            // 
            this.iPG_Cmn_Symbolics.Controls.Add(this.iPG301_DebugControl);
            this.iPG_Cmn_Symbolics.Controls.Add(this.header7);
            this.iPG_Cmn_Symbolics.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_Cmn_Symbolics.Location = new System.Drawing.Point(0, 0);
            this.iPG_Cmn_Symbolics.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_Cmn_Symbolics.Name = "iPG_Cmn_Symbolics";
            this.iPG_Cmn_Symbolics.Size = new System.Drawing.Size(573, 441);
            this.iPG_Cmn_Symbolics.TabIndex = 11;
            this.iPG_Cmn_Symbolics.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG301_AnalysisSymbolics_CloseFromNext);
            // 
            // iPG301_DebugControl
            // 
            this.iPG301_DebugControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG301_DebugControl.Location = new System.Drawing.Point(0, 64);
            this.iPG301_DebugControl.Margin = new System.Windows.Forms.Padding(0);
            this.iPG301_DebugControl.Name = "iPG301_DebugControl";
            this.iPG301_DebugControl.Size = new System.Drawing.Size(573, 377);
            this.iPG301_DebugControl.TabIndex = 14;
            // 
            // header7
            // 
            this.header7.BackColor = System.Drawing.SystemColors.Window;
            this.header7.CausesValidation = false;
            this.header7.Description = "In order to resolve object class types the tool requires access to both a Symbol " +
                "file and (for NAND-based products) the location of the image\'s Map files.";
            this.header7.Dock = System.Windows.Forms.DockStyle.Top;
            this.header7.Image = null;
            this.header7.Location = new System.Drawing.Point(0, 0);
            this.header7.Name = "header7";
            this.header7.Size = new System.Drawing.Size(573, 64);
            this.header7.TabIndex = 2;
            this.header7.Title = "Symbolics";
            // 
            // iPG_Cmn_Filters
            // 
            this.iPG_Cmn_Filters.Controls.Add(this.groupBox20);
            this.iPG_Cmn_Filters.Controls.Add(this.header6);
            this.iPG_Cmn_Filters.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_Cmn_Filters.Location = new System.Drawing.Point(0, 0);
            this.iPG_Cmn_Filters.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_Cmn_Filters.Name = "iPG_Cmn_Filters";
            this.iPG_Cmn_Filters.Size = new System.Drawing.Size(573, 441);
            this.iPG_Cmn_Filters.TabIndex = 10;
            this.iPG_Cmn_Filters.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG302_Cmn_Filters_CloseFromNext);
            // 
            // groupBox20
            // 
            this.groupBox20.Controls.Add(this.label8);
            this.groupBox20.Controls.Add(this.iPG302_Combo_Filter);
            this.groupBox20.Controls.Add(this.label41);
            this.groupBox20.Location = new System.Drawing.Point(16, 80);
            this.groupBox20.Name = "groupBox20";
            this.groupBox20.Size = new System.Drawing.Size(536, 105);
            this.groupBox20.TabIndex = 32;
            this.groupBox20.TabStop = false;
            this.groupBox20.Text = " Inspected Thread";
            // 
            // label8
            // 
            this.label8.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label8.Location = new System.Drawing.Point(116, 60);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(384, 32);
            this.label8.TabIndex = 35;
            this.label8.Text = "If the captured data contains heap information for more than one thread, then sel" +
                "ect the entity you wish to study from the above list.";
            // 
            // iPG302_Combo_Filter
            // 
            this.iPG302_Combo_Filter.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.iPG302_Combo_Filter.FormattingEnabled = true;
            this.iPG302_Combo_Filter.Location = new System.Drawing.Point(116, 26);
            this.iPG302_Combo_Filter.MaxDropDownItems = 14;
            this.iPG302_Combo_Filter.Name = "iPG302_Combo_Filter";
            this.iPG302_Combo_Filter.Size = new System.Drawing.Size(376, 21);
            this.iPG302_Combo_Filter.TabIndex = 30;
            // 
            // label41
            // 
            this.label41.AutoSize = true;
            this.label41.Location = new System.Drawing.Point(16, 30);
            this.label41.Name = "label41";
            this.label41.Size = new System.Drawing.Size(73, 13);
            this.label41.TabIndex = 24;
            this.label41.Text = "Thread name:";
            // 
            // header6
            // 
            this.header6.BackColor = System.Drawing.SystemColors.Window;
            this.header6.CausesValidation = false;
            this.header6.Description = "Define the trace prefix and/or filters used to analyse the logs";
            this.header6.Dock = System.Windows.Forms.DockStyle.Top;
            this.header6.Image = null;
            this.header6.Location = new System.Drawing.Point(0, 0);
            this.header6.Name = "header6";
            this.header6.Size = new System.Drawing.Size(573, 64);
            this.header6.TabIndex = 2;
            this.header6.Title = "Analysis Filters";
            // 
            // iPG_OutputToFile
            // 
            this.iPG_OutputToFile.Controls.Add(this.groupBox8);
            this.iPG_OutputToFile.Controls.Add(this.iHeader_OutputToFile);
            this.iPG_OutputToFile.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_OutputToFile.Location = new System.Drawing.Point(0, 0);
            this.iPG_OutputToFile.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_OutputToFile.Name = "iPG_OutputToFile";
            this.iPG_OutputToFile.Size = new System.Drawing.Size(573, 441);
            this.iPG_OutputToFile.TabIndex = 19;
            this.iPG_OutputToFile.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG500_Comparison_Output_CloseFromNext);
            // 
            // groupBox8
            // 
            this.groupBox8.Controls.Add(this.iPG_OutputToFile_FB);
            this.groupBox8.Location = new System.Drawing.Point(16, 80);
            this.groupBox8.Name = "groupBox8";
            this.groupBox8.Size = new System.Drawing.Size(536, 64);
            this.groupBox8.TabIndex = 12;
            this.groupBox8.TabStop = false;
            // 
            // iPG_OutputToFile_FB
            // 
            this.iPG_OutputToFile_FB.DialogFilter = "(*.xls)|*.xls";
            this.iPG_OutputToFile_FB.DialogTitle = "Output File";
            this.iPG_OutputToFile_FB.EntityMustExist = false;
            this.iPG_OutputToFile_FB.Location = new System.Drawing.Point(15, 22);
            this.iPG_OutputToFile_FB.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_OutputToFile_FB.MinimumSize = new System.Drawing.Size(396, 21);
            this.iPG_OutputToFile_FB.Name = "iPG_OutputToFile_FB";
            this.iPG_OutputToFile_FB.Size = new System.Drawing.Size(506, 21);
            this.iPG_OutputToFile_FB.TabIndex = 0;
            // 
            // iHeader_OutputToFile
            // 
            this.iHeader_OutputToFile.BackColor = System.Drawing.SystemColors.Window;
            this.iHeader_OutputToFile.CausesValidation = false;
            this.iHeader_OutputToFile.Description = "Enter a destination file for the ";
            this.iHeader_OutputToFile.Dock = System.Windows.Forms.DockStyle.Top;
            this.iHeader_OutputToFile.Image = null;
            this.iHeader_OutputToFile.Location = new System.Drawing.Point(0, 0);
            this.iHeader_OutputToFile.Name = "iHeader_OutputToFile";
            this.iHeader_OutputToFile.Size = new System.Drawing.Size(573, 64);
            this.iHeader_OutputToFile.TabIndex = 11;
            this.iHeader_OutputToFile.Title = "<DYNAMIC CONTENT>";
            // 
            // iPG_OutputToDirectory
            // 
            this.iPG_OutputToDirectory.Controls.Add(this.groupBox9);
            this.iPG_OutputToDirectory.Controls.Add(this.iHeader_OutputToDirectory);
            this.iPG_OutputToDirectory.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_OutputToDirectory.Location = new System.Drawing.Point(0, 0);
            this.iPG_OutputToDirectory.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_OutputToDirectory.Name = "iPG_OutputToDirectory";
            this.iPG_OutputToDirectory.Size = new System.Drawing.Size(573, 441);
            this.iPG_OutputToDirectory.TabIndex = 13;
            this.iPG_OutputToDirectory.PageClosedFromButtonNext += new SymbianWizardLib.GUI.SymWizardPage.PageClosedFromButtonNextHandler(this.iPG_OutputToDirectory_CloseFromNext);
            // 
            // groupBox9
            // 
            this.groupBox9.Controls.Add(this.iPG_OutputToDirectory_FB);
            this.groupBox9.Location = new System.Drawing.Point(16, 80);
            this.groupBox9.Name = "groupBox9";
            this.groupBox9.Size = new System.Drawing.Size(536, 64);
            this.groupBox9.TabIndex = 10;
            this.groupBox9.TabStop = false;
            // 
            // iPG_OutputToDirectory_FB
            // 
            this.iPG_OutputToDirectory_FB.DialogDescription = "Select destination location";
            this.iPG_OutputToDirectory_FB.Location = new System.Drawing.Point(15, 22);
            this.iPG_OutputToDirectory_FB.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_OutputToDirectory_FB.MinimumSize = new System.Drawing.Size(396, 21);
            this.iPG_OutputToDirectory_FB.Name = "iPG_OutputToDirectory_FB";
            this.iPG_OutputToDirectory_FB.Size = new System.Drawing.Size(506, 21);
            this.iPG_OutputToDirectory_FB.TabIndex = 14;
            // 
            // iHeader_OutputToDirectory
            // 
            this.iHeader_OutputToDirectory.BackColor = System.Drawing.SystemColors.Window;
            this.iHeader_OutputToDirectory.CausesValidation = false;
            this.iHeader_OutputToDirectory.Description = "Enter a destination location for the ";
            this.iHeader_OutputToDirectory.Dock = System.Windows.Forms.DockStyle.Top;
            this.iHeader_OutputToDirectory.Image = null;
            this.iHeader_OutputToDirectory.Location = new System.Drawing.Point(0, 0);
            this.iHeader_OutputToDirectory.Name = "iHeader_OutputToDirectory";
            this.iHeader_OutputToDirectory.Size = new System.Drawing.Size(573, 64);
            this.iHeader_OutputToDirectory.TabIndex = 1;
            this.iHeader_OutputToDirectory.Title = "<DYNAMIC CONTENT>";
            // 
            // iPG_Final
            // 
            this.iPG_Final.BackColor = System.Drawing.SystemColors.Window;
            this.iPG_Final.Controls.Add(this.iPG5000_InfoControl);
            this.iPG_Final.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG_Final.IsFinishingPage = true;
            this.iPG_Final.Location = new System.Drawing.Point(0, 0);
            this.iPG_Final.Margin = new System.Windows.Forms.Padding(0);
            this.iPG_Final.Name = "iPG_Final";
            this.iPG_Final.Size = new System.Drawing.Size(573, 441);
            this.iPG_Final.TabIndex = 4;
            // 
            // iPG5000_InfoControl
            // 
            this.iPG5000_InfoControl.BackColor = System.Drawing.SystemColors.Window;
            this.iPG5000_InfoControl.Description = "The wizard has now collected enough information. Press Finish to start the analys" +
                "is phase.";
            this.iPG5000_InfoControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG5000_InfoControl.Location = new System.Drawing.Point(0, 0);
            this.iPG5000_InfoControl.Margin = new System.Windows.Forms.Padding(0);
            this.iPG5000_InfoControl.Name = "iPG5000_InfoControl";
            this.iPG5000_InfoControl.Size = new System.Drawing.Size(573, 441);
            this.iPG5000_InfoControl.TabIndex = 0;
            this.iPG5000_InfoControl.Title = "Ready to Analyse";
            // 
            // label1
            // 
            this.label1.Location = new System.Drawing.Point(16, 88);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(88, 16);
            this.label1.TabIndex = 31;
            this.label1.Text = "Rebuild kernel?";
            // 
            // iErrorProvider
            // 
            this.iErrorProvider.ContainerControl = this;
            // 
            // HASetupWizard
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.ClientSize = new System.Drawing.Size(573, 497);
            this.Controls.Add(this.iWizard);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "HASetupWizard";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Heap Analyser Setup Wizard";
            this.Load += new System.EventHandler(this.Form_Load);
            this.Closing += new System.ComponentModel.CancelEventHandler(this.Form_Closing);
            this.iPG_OpType.ResumeLayout(false);
            this.iPG_OpType.PerformLayout();
            this.iPG1_OpType_GroupBox.ResumeLayout(false);
            this.iPG1_OpType_GroupBox.PerformLayout();
            this.iPG1_OpType_FLP.ResumeLayout(false);
            this.iPG1_OpType_FLP.PerformLayout();
            this.iPG_SourceData_Log.ResumeLayout(false);
            this.groupBox7.ResumeLayout(false);
            this.iPG_SourceData_CompareCSV.ResumeLayout(false);
            this.iPG_SourceData_CompareHeapData.ResumeLayout(false);
            this.iPG_SourceData_CompareHeapData_GP_Log2.ResumeLayout(false);
            this.iPG_SourceData_CompareHeapData_GP_Log2.PerformLayout();
            this.iPG_SourceData_CompareHeapData_GP_Log1.ResumeLayout(false);
            this.iPG_SourceData_CompareHeapData_GP_Log1.PerformLayout();
            this.iPG_Cmn_Symbolics.ResumeLayout(false);
            this.iPG_Cmn_Filters.ResumeLayout(false);
            this.groupBox20.ResumeLayout(false);
            this.groupBox20.PerformLayout();
            this.iPG_OutputToFile.ResumeLayout(false);
            this.groupBox8.ResumeLayout(false);
            this.iPG_OutputToDirectory.ResumeLayout(false);
            this.groupBox9.ResumeLayout(false);
            this.iPG_Final.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.iErrorProvider)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private SymbianWizardLib.GUI.SymWizard iWizard;
        private System.Windows.Forms.Label label1;
        private SymbianWizardLib.GUI.SymWizardPage iPG_OpType;
        private SymbianWizardLib.GUI.SymWizardPage iPG_Cmn_Symbolics;
        private SymbianWizardLib.GUI.SymWizardPage iPG_Cmn_Filters;
        private SymbianWizardLib.GUI.SymWizardPage iPG_OutputToDirectory;
        private SymbianWizardLib.GUI.SymWizardHeaderSection header4;
        private SymbianWizardLib.GUI.SymWizardPage iPG_Final;
        private SymbianWizardLib.GUI.SymWizardHeaderSection header5;
        private System.Windows.Forms.GroupBox groupBox7;
        private SymbianWizardLib.GUI.SymWizardHeaderSection header6;
        private System.Windows.Forms.GroupBox groupBox20;
        private System.Windows.Forms.Label label41;
        private SymbianWizardLib.GUI.SymWizardHeaderSection header7;
        private SymbianWizardLib.GUI.SymWizardHeaderSection iHeader_OutputToDirectory;
        private System.Windows.Forms.GroupBox groupBox9;
        private SymbianWizardLib.GUI.SymWizardPage iPG_SourceData_Log;
        private System.Windows.Forms.Label iLbl_Version;
        private System.Windows.Forms.ComboBox iPG302_Combo_Filter;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.ErrorProvider iErrorProvider;
        private SymbianWizardLib.GUI.SymWizardPage iPG_SourceData_CompareHeapData;
        private System.Windows.Forms.GroupBox iPG_SourceData_CompareHeapData_GP_Log1;
        private SymbianUtilsUi.Controls.SymbianFileBrowserControl iPG202_TB_LogFile1;
        private SymbianWizardLib.GUI.SymWizardHeaderSection header3;
        private System.Windows.Forms.GroupBox iPG_SourceData_CompareHeapData_GP_Log2;
        private SymbianUtilsUi.Controls.SymbianFileBrowserControl iPG202_TB_LogFile2;
        private SymbianWizardLib.GUI.SymWizardPage iPG_OutputToFile;
        private System.Windows.Forms.GroupBox groupBox8;
        private SymbianWizardLib.GUI.SymWizardHeaderSection iHeader_OutputToFile;
        private SymbianUtilsUi.Controls.SymbianFileSaveAsControl iPG_OutputToFile_FB;
        private SymbianWizardLib.GUI.SymWizardInfoControl iPG5000_InfoControl;
        private System.Windows.Forms.GroupBox iPG1_OpType_GroupBox;
        private System.Windows.Forms.FlowLayoutPanel iPG1_OpType_FLP;
        private System.Windows.Forms.RadioButton iPG1_RB_OpType_HeapViewer;
        private System.Windows.Forms.RadioButton iPG1_RB_OpType_CompareHeapDump;
        private SymbianDebugLibUi.Controls.DebugEngineControl iPG301_DebugControl;
        private System.Windows.Forms.RadioButton iPG1_RB_OpType_CompareCSV;
        private SymbianWizardLib.GUI.SymWizardPage iPG_SourceData_CompareCSV;
        private SymbianWizardLib.GUI.SymWizardHeaderSection symWizardHeaderSection1;
        private SymbianUtilsUi.Controls.SymbianFileListManagementControl iPG_SourceData_CompareCSV_Files;
        private System.Windows.Forms.ComboBox iPG202_Combo_ThreadName2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.ComboBox iPG202_Combo_ThreadName1;
        private System.Windows.Forms.Label label2;
        private SymbianUtilsUi.Controls.SymbianFileBrowserControl iPG_SourceData_Log_FB;
        private SymbianUtilsUi.Controls.SymbianFolderBrowserControl iPG_OutputToDirectory_FB;
    }
}