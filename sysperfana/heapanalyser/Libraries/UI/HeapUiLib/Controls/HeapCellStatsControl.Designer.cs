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

﻿namespace HeapUiLib.Controls
{
    partial class HeapCellStatsControl
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

        #region Component Designer generated code

        // <summary> 
        // Required method for Designer support - do not modify 
        // the contents of this method with the code editor.
        // </summary>
        private void InitializeComponent()
        {
            System.Windows.Forms.ListViewGroup listViewGroup1 = new System.Windows.Forms.ListViewGroup( "Cell Counts", System.Windows.Forms.HorizontalAlignment.Left );
            System.Windows.Forms.ListViewGroup listViewGroup2 = new System.Windows.Forms.ListViewGroup( "Memory", System.Windows.Forms.HorizontalAlignment.Left );
            System.Windows.Forms.ListViewGroup listViewGroup3 = new System.Windows.Forms.ListViewGroup( "Totals", System.Windows.Forms.HorizontalAlignment.Left );
            System.Windows.Forms.ListViewItem listViewItem1 = new System.Windows.Forms.ListViewItem( new string[] {
            "Number of allocated cells:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem2 = new System.Windows.Forms.ListViewItem( new string[] {
            "Number of free cells:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem3 = new System.Windows.Forms.ListViewItem( new string[] {
            "Memory for items matching symbols:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem4 = new System.Windows.Forms.ListViewItem( new string[] {
            "Memory for other allocations:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem5 = new System.Windows.Forms.ListViewItem( "" );
            System.Windows.Forms.ListViewItem listViewItem6 = new System.Windows.Forms.ListViewItem( new string[] {
            "Total consumed by allocated cells:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem7 = new System.Windows.Forms.ListViewItem( new string[] {
            "Total consumed by free cells:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem8 = new System.Windows.Forms.ListViewItem( "" );
            System.Windows.Forms.ListViewItem listViewItem9 = new System.Windows.Forms.ListViewItem( new string[] {
            "Heap cell header overhead size:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem10 = new System.Windows.Forms.ListViewItem( new string[] {
            "Selection total:",
            "",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem11 = new System.Windows.Forms.ListViewItem( new string[] {
            "Total allocated memory for heap cells:",
            "",
            ""}, -1 );
            XPTable.Models.DataSourceColumnBinder dataSourceColumnBinder1 = new XPTable.Models.DataSourceColumnBinder();
            this.iLV_Summary = new System.Windows.Forms.ListView();
            this.iColItem = new System.Windows.Forms.ColumnHeader();
            this.iColTotal = new System.Windows.Forms.ColumnHeader();
            this.iColPercentage = new System.Windows.Forms.ColumnHeader();
            this.iTable_SymbolMemory = new XPTable.Models.Table();
            this.iColumnModel_SymbolMemory = new XPTable.Models.ColumnModel();
            this.iTab2_ColModel_Stats_Col_Symbol = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_AllocationCount = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory = new XPTable.Models.TextColumn();
            this.iTableModel_SymbolMemory = new XPTable.Models.TableModel();
            this.iTLP = new System.Windows.Forms.TableLayoutPanel();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable_SymbolMemory ) ).BeginInit();
            this.iTLP.SuspendLayout();
            this.SuspendLayout();
            // 
            // iLV_Summary
            // 
            this.iLV_Summary.Columns.AddRange( new System.Windows.Forms.ColumnHeader[] {
            this.iColItem,
            this.iColTotal,
            this.iColPercentage} );
            this.iLV_Summary.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iLV_Summary.FullRowSelect = true;
            this.iLV_Summary.GridLines = true;
            listViewGroup1.Header = "Cell Counts";
            listViewGroup1.Name = "iLVGP_CellCounts";
            listViewGroup2.Header = "Memory";
            listViewGroup2.Name = "iLVGP_Memory";
            listViewGroup3.Header = "Totals";
            listViewGroup3.Name = "iLVGP_Totals";
            this.iLV_Summary.Groups.AddRange( new System.Windows.Forms.ListViewGroup[] {
            listViewGroup1,
            listViewGroup2,
            listViewGroup3} );
            this.iLV_Summary.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.None;
            listViewItem1.Group = listViewGroup1;
            listViewItem2.Group = listViewGroup1;
            listViewItem3.Group = listViewGroup2;
            listViewItem4.Group = listViewGroup2;
            listViewItem5.Group = listViewGroup2;
            listViewItem6.Group = listViewGroup2;
            listViewItem7.Group = listViewGroup2;
            listViewItem8.Group = listViewGroup2;
            listViewItem9.Group = listViewGroup2;
            listViewItem10.Group = listViewGroup3;
            listViewItem11.Group = listViewGroup3;
            this.iLV_Summary.Items.AddRange( new System.Windows.Forms.ListViewItem[] {
            listViewItem1,
            listViewItem2,
            listViewItem3,
            listViewItem4,
            listViewItem5,
            listViewItem6,
            listViewItem7,
            listViewItem8,
            listViewItem9,
            listViewItem10,
            listViewItem11} );
            this.iLV_Summary.Location = new System.Drawing.Point( 578, 0 );
            this.iLV_Summary.Margin = new System.Windows.Forms.Padding( 0 );
            this.iLV_Summary.Name = "iLV_Summary";
            this.iLV_Summary.Scrollable = false;
            this.iLV_Summary.Size = new System.Drawing.Size( 353, 293 );
            this.iLV_Summary.TabIndex = 4;
            this.iLV_Summary.UseCompatibleStateImageBehavior = false;
            this.iLV_Summary.View = System.Windows.Forms.View.Details;
            // 
            // iColItem
            // 
            this.iColItem.Text = "";
            this.iColItem.Width = 190;
            // 
            // iColTotal
            // 
            this.iColTotal.Text = "Total";
            this.iColTotal.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColTotal.Width = 105;
            // 
            // iColPercentage
            // 
            this.iColPercentage.Text = "%";
            this.iColPercentage.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            // 
            // iTable_SymbolMemory
            // 
            this.iTable_SymbolMemory.BorderColor = System.Drawing.Color.Black;
            this.iTable_SymbolMemory.ColumnModel = this.iColumnModel_SymbolMemory;
            this.iTable_SymbolMemory.DataMember = null;
            this.iTable_SymbolMemory.DataSourceColumnBinder = dataSourceColumnBinder1;
            this.iTable_SymbolMemory.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTable_SymbolMemory.EditStartAction = XPTable.Editors.EditStartAction.CustomKey;
            this.iTable_SymbolMemory.FullRowSelect = true;
            this.iTable_SymbolMemory.GridLines = XPTable.Models.GridLines.Rows;
            this.iTable_SymbolMemory.HeaderFont = new System.Drawing.Font( "Tahoma", 6.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable_SymbolMemory.Location = new System.Drawing.Point( 0, 0 );
            this.iTable_SymbolMemory.Margin = new System.Windows.Forms.Padding( 0 );
            this.iTable_SymbolMemory.MultiSelect = true;
            this.iTable_SymbolMemory.Name = "iTable_SymbolMemory";
            this.iTable_SymbolMemory.NoItemsText = "Statistics Unavailable";
            this.iTable_SymbolMemory.NoItemsTextColor = System.Drawing.SystemColors.ControlText;
            this.iTable_SymbolMemory.NoItemsTextFont = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iTable_SymbolMemory.SelectionStyle = XPTable.Models.SelectionStyle.Grid;
            this.iTable_SymbolMemory.Size = new System.Drawing.Size( 574, 293 );
            this.iTable_SymbolMemory.TabIndex = 6;
            this.iTable_SymbolMemory.TableModel = this.iTableModel_SymbolMemory;
            this.iTable_SymbolMemory.UnfocusedBorderColor = System.Drawing.Color.Black;
            this.iTable_SymbolMemory.PrepareForSort += new XPTable.Events.SortEventHandler( this.iPG3_StatsTable_PrepareForSort );
            this.iTable_SymbolMemory.SelectionChanged += new XPTable.Events.SelectionEventHandler( this.iPG3_StatsTable_SelectionChanged );
            // 
            // iColumnModel_SymbolMemory
            // 
            this.iColumnModel_SymbolMemory.Columns.AddRange( new XPTable.Models.Column[] {
            this.iTab2_ColModel_Stats_Col_Symbol,
            this.iTab2_ColModel_Stats_Col_AllocationCount,
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance,
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory} );
            // 
            // iTab2_ColModel_Stats_Col_Symbol
            // 
            this.iTab2_ColModel_Stats_Col_Symbol.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_Symbol.Text = "Symbol";
            this.iTab2_ColModel_Stats_Col_Symbol.Width = 270;
            // 
            // iTab2_ColModel_Stats_Col_AllocationCount
            // 
            this.iTab2_ColModel_Stats_Col_AllocationCount.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_AllocationCount.Text = "Allocation Count";
            this.iTab2_ColModel_Stats_Col_AllocationCount.Width = 100;
            // 
            // iTab2_ColModel_Stats_Col_MemoryPerInstance
            // 
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.Text = "Memory-per-Instance";
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.Width = 100;
            // 
            // iTab2_ColModel_Stats_Col_TotalAllocatedMemory
            // 
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.Text = "Total Allocated Memory";
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.Width = 110;
            // 
            // iTLP
            // 
            this.iTLP.ColumnCount = 3;
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 62F ) );
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Absolute, 4F ) );
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 38F ) );
            this.iTLP.Controls.Add( this.iLV_Summary, 2, 0 );
            this.iTLP.Controls.Add( this.iTable_SymbolMemory, 0, 0 );
            this.iTLP.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTLP.Location = new System.Drawing.Point( 0, 0 );
            this.iTLP.Margin = new System.Windows.Forms.Padding( 0 );
            this.iTLP.Name = "iTLP";
            this.iTLP.RowCount = 1;
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTLP.Size = new System.Drawing.Size( 931, 293 );
            this.iTLP.TabIndex = 7;
            // 
            // HeapCellStatsControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iTLP );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Name = "HeapCellStatsControl";
            this.Size = new System.Drawing.Size( 931, 293 );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable_SymbolMemory ) ).EndInit();
            this.iTLP.ResumeLayout( false );
            this.ResumeLayout( false );

        }

        #endregion

        private System.Windows.Forms.ListView iLV_Summary;
        private System.Windows.Forms.ColumnHeader iColItem;
        private System.Windows.Forms.ColumnHeader iColTotal;
        private System.Windows.Forms.ColumnHeader iColPercentage;
        private XPTable.Models.Table iTable_SymbolMemory;
        private XPTable.Models.ColumnModel iColumnModel_SymbolMemory;
        private XPTable.Models.TableModel iTableModel_SymbolMemory;
        private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_Symbol;
        private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_AllocationCount;
        private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_MemoryPerInstance;
        private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_TotalAllocatedMemory;
        private System.Windows.Forms.TableLayoutPanel iTLP;
    }
}
