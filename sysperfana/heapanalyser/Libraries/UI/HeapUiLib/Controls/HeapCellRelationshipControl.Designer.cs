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

namespace HeapUiLib.Controls
{
    partial class HeapCellRelationshipControl
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
            HeapLib.Statistics.HeapStatistics heapStatistics1 = new HeapLib.Statistics.HeapStatistics();
            XPTable.Models.DataSourceColumnBinder dataSourceColumnBinder1 = new XPTable.Models.DataSourceColumnBinder();
            HeapLib.Array.HeapCellArray heapCellArray1 = new HeapLib.Array.HeapCellArray();
            this.iTLP = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.iCellFilter = new HeapUiLib.Controls.HeapCellListingFilter();
            this.iTable = new XPTable.Models.Table();
            this.iCM_Cells = new XPTable.Models.ColumnModel();
            this.iCol_Type = new XPTable.Models.TextColumn();
            this.iCol_Address = new XPTable.Models.TextColumn();
            this.iCol_Length = new XPTable.Models.TextColumn();
            this.iCol_ReferencedBy = new XPTable.Models.TextColumn();
            this.iCol_EmbeddedReferencesTo = new XPTable.Models.TextColumn();
            this.iCol_Symbol = new XPTable.Models.TextColumn();
            this.iTM_Cells = new XPTable.Models.TableModel();
            this.iGP_AssociatedWith = new System.Windows.Forms.GroupBox();
            this.iAssociatedWith = new HeapUiLib.Controls.HeapCellListingControl();
            this.iCol_Index = new XPTable.Models.TextColumn();
            this.iRB_ReferencedBy = new System.Windows.Forms.RadioButton();
            this.iRB_EmbeddedReferencesTo = new System.Windows.Forms.RadioButton();
            this.iTLP.SuspendLayout();
            this.groupBox1.SuspendLayout();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).BeginInit();
            this.iGP_AssociatedWith.SuspendLayout();
            this.SuspendLayout();
            // 
            // iTLP
            // 
            this.iTLP.ColumnCount = 1;
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTLP.Controls.Add( this.groupBox1, 0, 0 );
            this.iTLP.Controls.Add( this.iGP_AssociatedWith, 0, 2 );
            this.iTLP.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTLP.Location = new System.Drawing.Point( 0, 0 );
            this.iTLP.Margin = new System.Windows.Forms.Padding( 0 );
            this.iTLP.Name = "iTLP";
            this.iTLP.RowCount = 3;
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 50F ) );
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Absolute, 10F ) );
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 50F ) );
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Absolute, 20F ) );
            this.iTLP.Size = new System.Drawing.Size( 603, 538 );
            this.iTLP.TabIndex = 0;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add( this.iCellFilter );
            this.groupBox1.Controls.Add( this.iTable );
            this.groupBox1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox1.Location = new System.Drawing.Point( 3, 3 );
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size( 597, 258 );
            this.groupBox1.TabIndex = 0;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Cells";
            // 
            // iCellFilter
            // 
            this.iCellFilter.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iCellFilter.FilterType = HeapUiLib.Controls.TFilterType.EFilterShowAll;
            this.iCellFilter.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iCellFilter.Location = new System.Drawing.Point( 10, 16 );
            this.iCellFilter.Margin = new System.Windows.Forms.Padding( 0 );
            this.iCellFilter.MinimumSize = new System.Drawing.Size( 0, 20 );
            this.iCellFilter.Name = "iCellFilter";
            this.iCellFilter.Size = new System.Drawing.Size( 578, 20 );
            heapStatistics1.HeapBaseAddress = ( (uint) ( 4294967295u ) );
            heapStatistics1.HeapSize = ( (uint) ( 1u ) );
            this.iCellFilter.Statistics = heapStatistics1;
            this.iCellFilter.TabIndex = 1;
            this.iCellFilter.FilterChanged += new HeapUiLib.Controls.HeapCellListingFilter.FilterChangedHandler( this.iCellFilter_FilterChanged );
            // 
            // iTable
            // 
            this.iTable.AlternatingRowColor = System.Drawing.Color.Gainsboro;
            this.iTable.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iTable.BorderColor = System.Drawing.Color.Black;
            this.iTable.ColumnModel = this.iCM_Cells;
            this.iTable.DataMember = null;
            this.iTable.DataSourceColumnBinder = dataSourceColumnBinder1;
            this.iTable.EnableToolTips = true;
            this.iTable.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.FullRowSelect = true;
            this.iTable.HeaderFont = new System.Drawing.Font( "Tahoma", 6.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.Location = new System.Drawing.Point( 10, 39 );
            this.iTable.Name = "iTable";
            this.iTable.NoItemsText = "No Cells";
            this.iTable.NoItemsTextColor = System.Drawing.SystemColors.ControlText;
            this.iTable.NoItemsTextFont = new System.Drawing.Font( "Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.Size = new System.Drawing.Size( 578, 209 );
            this.iTable.TabIndex = 0;
            this.iTable.TableModel = this.iTM_Cells;
            this.iTable.Text = "table1";
            this.iTable.UnfocusedBorderColor = System.Drawing.Color.Black;
            this.iTable.UnfocusedSelectionBackColor = System.Drawing.SystemColors.Desktop;
            this.iTable.UnfocusedSelectionForeColor = System.Drawing.SystemColors.HighlightText;
            this.iTable.PrepareForSort += new XPTable.Events.SortEventHandler( this.iTable_PrepareForSort );
            this.iTable.DoubleClick += new System.EventHandler( this.iTable_DoubleClick );
            this.iTable.SelectionChanged += new XPTable.Events.SelectionEventHandler( this.iTable_SelectionChanged );
            // 
            // iCM_Cells
            // 
            this.iCM_Cells.Columns.AddRange( new XPTable.Models.Column[] {
            this.iCol_Type,
            this.iCol_Address,
            this.iCol_Length,
            this.iCol_ReferencedBy,
            this.iCol_EmbeddedReferencesTo,
            this.iCol_Symbol} );
            // 
            // iCol_Type
            // 
            this.iCol_Type.ContentWidth = 29;
            this.iCol_Type.Text = "Type";
            this.iCol_Type.Width = 85;
            // 
            // iCol_Address
            // 
            this.iCol_Address.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_Address.ContentWidth = 46;
            this.iCol_Address.Text = "Address";
            this.iCol_Address.Width = 85;
            // 
            // iCol_Length
            // 
            this.iCol_Length.Alignment = XPTable.Models.ColumnAlignment.Right;
            this.iCol_Length.ContentWidth = 39;
            this.iCol_Length.Text = "Length";
            this.iCol_Length.Width = 85;
            // 
            // iCol_ReferencedBy
            // 
            this.iCol_ReferencedBy.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_ReferencedBy.ContentWidth = 41;
            this.iCol_ReferencedBy.Text = "Ref. By";
            this.iCol_ReferencedBy.Width = 85;
            // 
            // iCol_EmbeddedReferencesTo
            // 
            this.iCol_EmbeddedReferencesTo.Alignment = XPTable.Models.ColumnAlignment.Right;
            this.iCol_EmbeddedReferencesTo.ContentWidth = 41;
            this.iCol_EmbeddedReferencesTo.Text = "Ref. To";
            this.iCol_EmbeddedReferencesTo.Width = 85;
            // 
            // iCol_Symbol
            // 
            this.iCol_Symbol.ContentWidth = 70;
            this.iCol_Symbol.Text = "Object Name";
            this.iCol_Symbol.Width = 350;
            // 
            // iGP_AssociatedWith
            // 
            this.iGP_AssociatedWith.Controls.Add( this.iRB_EmbeddedReferencesTo );
            this.iGP_AssociatedWith.Controls.Add( this.iRB_ReferencedBy );
            this.iGP_AssociatedWith.Controls.Add( this.iAssociatedWith );
            this.iGP_AssociatedWith.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iGP_AssociatedWith.Location = new System.Drawing.Point( 3, 277 );
            this.iGP_AssociatedWith.Name = "iGP_AssociatedWith";
            this.iGP_AssociatedWith.Size = new System.Drawing.Size( 597, 258 );
            this.iGP_AssociatedWith.TabIndex = 1;
            this.iGP_AssociatedWith.TabStop = false;
            this.iGP_AssociatedWith.Text = "Associated with...";
            // 
            // iAssociatedWith
            // 
            this.iAssociatedWith.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iAssociatedWith.Cells = heapCellArray1;
            this.iAssociatedWith.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iAssociatedWith.Location = new System.Drawing.Point( 10, 46 );
            this.iAssociatedWith.Name = "iAssociatedWith";
            this.iAssociatedWith.SelectedCell = null;
            this.iAssociatedWith.Size = new System.Drawing.Size( 578, 202 );
            this.iAssociatedWith.TabIndex = 0;
            // 
            // iCol_Index
            // 
            this.iCol_Index.ContentWidth = 0;
            // 
            // iRB_ReferencedBy
            // 
            this.iRB_ReferencedBy.AutoSize = true;
            this.iRB_ReferencedBy.Checked = true;
            this.iRB_ReferencedBy.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iRB_ReferencedBy.Location = new System.Drawing.Point( 10, 20 );
            this.iRB_ReferencedBy.Name = "iRB_ReferencedBy";
            this.iRB_ReferencedBy.Size = new System.Drawing.Size( 96, 17 );
            this.iRB_ReferencedBy.TabIndex = 1;
            this.iRB_ReferencedBy.TabStop = true;
            this.iRB_ReferencedBy.Text = "Referenced By";
            this.iRB_ReferencedBy.UseVisualStyleBackColor = true;
            this.iRB_ReferencedBy.CheckedChanged += new System.EventHandler( this.iRB_Type_CheckedChanged );
            // 
            // iRB_EmbeddedReferencesTo
            // 
            this.iRB_EmbeddedReferencesTo.AutoSize = true;
            this.iRB_EmbeddedReferencesTo.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iRB_EmbeddedReferencesTo.Location = new System.Drawing.Point( 136, 20 );
            this.iRB_EmbeddedReferencesTo.Name = "iRB_EmbeddedReferencesTo";
            this.iRB_EmbeddedReferencesTo.Size = new System.Drawing.Size( 148, 17 );
            this.iRB_EmbeddedReferencesTo.TabIndex = 1;
            this.iRB_EmbeddedReferencesTo.Text = "Embedded References To";
            this.iRB_EmbeddedReferencesTo.UseVisualStyleBackColor = true;
            this.iRB_EmbeddedReferencesTo.CheckedChanged += new System.EventHandler( this.iRB_Type_CheckedChanged );
            // 
            // HeapCellRelationshipControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iTLP );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Name = "HeapCellRelationshipControl";
            this.Size = new System.Drawing.Size( 603, 538 );
            this.iTLP.ResumeLayout( false );
            this.groupBox1.ResumeLayout( false );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).EndInit();
            this.iGP_AssociatedWith.ResumeLayout( false );
            this.iGP_AssociatedWith.PerformLayout();
            this.ResumeLayout( false );

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel iTLP;
        private System.Windows.Forms.GroupBox groupBox1;
        private XPTable.Models.Table iTable;
        private System.Windows.Forms.GroupBox iGP_AssociatedWith;
        private XPTable.Models.TableModel iTM_Cells;
        private XPTable.Models.ColumnModel iCM_Cells;
        private XPTable.Models.TextColumn iCol_Type;
        private XPTable.Models.TextColumn iCol_Address;
        private XPTable.Models.TextColumn iCol_Length;
        private XPTable.Models.TextColumn iCol_Symbol;
        private XPTable.Models.TextColumn iCol_Index;
        private XPTable.Models.TextColumn iCol_ReferencedBy;
        private XPTable.Models.TextColumn iCol_EmbeddedReferencesTo;
        private HeapCellListingControl iAssociatedWith;
        private HeapCellListingFilter iCellFilter;
        private System.Windows.Forms.RadioButton iRB_EmbeddedReferencesTo;
        private System.Windows.Forms.RadioButton iRB_ReferencedBy;
    }
}
