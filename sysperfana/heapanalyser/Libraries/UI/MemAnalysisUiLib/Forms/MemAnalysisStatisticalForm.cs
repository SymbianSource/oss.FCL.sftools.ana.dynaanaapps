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
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using MemAnalysisLib;
using MemAnalysisLib.Parser;
using SymbianUtils.Settings;
using MemAnalysisLib.MemoryOperations.Class;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Operations;
using MemAnalysisLib.Parser.Base;
using MemAnalysisLib.Parser.Implementations;

namespace MemAnalysisUiLib.Forms
{
	public class MemAnalysisStatisticalForm : System.Windows.Forms.Form
	{
		#region Windows Form Designer Code
        private System.Windows.Forms.Button iCloseButton;
        private System.Windows.Forms.Timer iTimerRefreshGrid;
        private XPTable.Models.TextColumn iGridCol_MemOp;
        private XPTable.Models.TextColumn iGridCol_LineNumber;
        private XPTable.Models.TextColumn iGridCol_CellAddr;
        private XPTable.Models.TextColumn iGridCol_AllocSize;
        private XPTable.Models.TextColumn iGridCol_Type;
        private XPTable.Models.TextColumn iGridCol_HeapSize;
        private XPTable.Models.TextColumn iGridCol_AssociatedOp;
        private XPTable.Models.ColumnModel iGrid_ColModel;
        private XPTable.Models.TableModel iGrid_TableModel;
        private MenuStrip menuStrip1;
        private ToolStripMenuItem iMenuItem_File;
        private SplitContainer iSplitContainer;
        private GroupBox groupBox32;
        private ListView iListView;
        private ColumnHeader iColHdrTotals;
        private ColumnHeader iColHdrObjectAllocCount;
        private ColumnHeader iColHdrObjectFreeCount;
        private ColumnHeader iColHdrTotalAllocSize;
        private ColumnHeader iColHdrFreedMemory;
        private ColumnHeader iColHdrNetAllocSize;
        private ColumnHeader iColHdrSymbolName;
        private GroupBox iMemAnalysisDetailedInfoForSymbolGroupBox;
        private XPTable.Models.Table iGrid;
        private ToolStripMenuItem iMenuItem_File_SaveAs;
        private ToolStripMenuItem iMenuItem_File_Exit;
        private Button iBT_CopyToClipboard;
		private System.ComponentModel.IContainer components;
		#endregion

		#region Constructors & destructor
		public MemAnalysisStatisticalForm( MemAnalysisParserBase aParser, XmlSettings aSettings )
		{
			InitializeComponent();

			System.Diagnostics.Debug.Assert( aParser is MemAnalysisStatisticalParser );
			iParser = (MemAnalysisStatisticalParser) aParser;
			iSettings = aSettings;
		}

		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if(components != null)
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}
		#endregion

		#region Windows Form Designer generated code
		private void InitializeComponent()
		{
            this.components = new System.ComponentModel.Container();
            this.iCloseButton = new System.Windows.Forms.Button();
            this.iTimerRefreshGrid = new System.Windows.Forms.Timer( this.components );
            this.iGrid_ColModel = new XPTable.Models.ColumnModel();
            this.iGridCol_MemOp = new XPTable.Models.TextColumn();
            this.iGridCol_LineNumber = new XPTable.Models.TextColumn();
            this.iGridCol_CellAddr = new XPTable.Models.TextColumn();
            this.iGridCol_Type = new XPTable.Models.TextColumn();
            this.iGridCol_AllocSize = new XPTable.Models.TextColumn();
            this.iGridCol_HeapSize = new XPTable.Models.TextColumn();
            this.iGridCol_AssociatedOp = new XPTable.Models.TextColumn();
            this.iGrid_TableModel = new XPTable.Models.TableModel();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.iMenuItem_File = new System.Windows.Forms.ToolStripMenuItem();
            this.iMenuItem_File_SaveAs = new System.Windows.Forms.ToolStripMenuItem();
            this.iMenuItem_File_Exit = new System.Windows.Forms.ToolStripMenuItem();
            this.iSplitContainer = new System.Windows.Forms.SplitContainer();
            this.groupBox32 = new System.Windows.Forms.GroupBox();
            this.iListView = new System.Windows.Forms.ListView();
            this.iColHdrTotals = new System.Windows.Forms.ColumnHeader();
            this.iColHdrObjectAllocCount = new System.Windows.Forms.ColumnHeader();
            this.iColHdrObjectFreeCount = new System.Windows.Forms.ColumnHeader();
            this.iColHdrTotalAllocSize = new System.Windows.Forms.ColumnHeader();
            this.iColHdrFreedMemory = new System.Windows.Forms.ColumnHeader();
            this.iColHdrNetAllocSize = new System.Windows.Forms.ColumnHeader();
            this.iColHdrSymbolName = new System.Windows.Forms.ColumnHeader();
            this.iMemAnalysisDetailedInfoForSymbolGroupBox = new System.Windows.Forms.GroupBox();
            this.iBT_CopyToClipboard = new System.Windows.Forms.Button();
            this.iGrid = new XPTable.Models.Table();
            this.menuStrip1.SuspendLayout();
            this.iSplitContainer.Panel1.SuspendLayout();
            this.iSplitContainer.Panel2.SuspendLayout();
            this.iSplitContainer.SuspendLayout();
            this.groupBox32.SuspendLayout();
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.SuspendLayout();
            ( (System.ComponentModel.ISupportInitialize) ( this.iGrid ) ).BeginInit();
            this.SuspendLayout();
            // 
            // iCloseButton
            // 
            this.iCloseButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.iCloseButton.Location = new System.Drawing.Point( 439, 680 );
            this.iCloseButton.Name = "iCloseButton";
            this.iCloseButton.Size = new System.Drawing.Size( 75, 23 );
            this.iCloseButton.TabIndex = 51;
            this.iCloseButton.Text = "Close";
            this.iCloseButton.Click += new System.EventHandler( this.iCloseButton_Click );
            // 
            // iTimerRefreshGrid
            // 
            this.iTimerRefreshGrid.Interval = 200;
            this.iTimerRefreshGrid.Tick += new System.EventHandler( this.iTimerRefreshGrid_Tick );
            // 
            // iGrid_ColModel
            // 
            this.iGrid_ColModel.Columns.AddRange( new XPTable.Models.Column[] {
            this.iGridCol_MemOp,
            this.iGridCol_LineNumber,
            this.iGridCol_CellAddr,
            this.iGridCol_Type,
            this.iGridCol_AllocSize,
            this.iGridCol_HeapSize,
            this.iGridCol_AssociatedOp} );
            // 
            // iGridCol_MemOp
            // 
            this.iGridCol_MemOp.Text = "Op. #";
            this.iGridCol_MemOp.Width = 72;
            // 
            // iGridCol_LineNumber
            // 
            this.iGridCol_LineNumber.Text = "Line";
            this.iGridCol_LineNumber.Width = 62;
            // 
            // iGridCol_CellAddr
            // 
            this.iGridCol_CellAddr.Text = "Cell Addr.";
            this.iGridCol_CellAddr.Width = 62;
            // 
            // iGridCol_Type
            // 
            this.iGridCol_Type.Text = "Type";
            this.iGridCol_Type.Width = 110;
            // 
            // iGridCol_AllocSize
            // 
            this.iGridCol_AllocSize.Alignment = XPTable.Models.ColumnAlignment.Right;
            this.iGridCol_AllocSize.Text = "Alloc. Size";
            this.iGridCol_AllocSize.Width = 64;
            // 
            // iGridCol_HeapSize
            // 
            this.iGridCol_HeapSize.Alignment = XPTable.Models.ColumnAlignment.Right;
            this.iGridCol_HeapSize.Text = "Heap Size";
            this.iGridCol_HeapSize.Width = 64;
            // 
            // iGridCol_AssociatedOp
            // 
            this.iGridCol_AssociatedOp.Text = "Associated Op.";
            this.iGridCol_AssociatedOp.Width = 170;
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iMenuItem_File} );
            this.menuStrip1.Location = new System.Drawing.Point( 0, 0 );
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size( 738, 24 );
            this.menuStrip1.TabIndex = 53;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // iMenuItem_File
            // 
            this.iMenuItem_File.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iMenuItem_File_SaveAs,
            this.iMenuItem_File_Exit} );
            this.iMenuItem_File.Name = "iMenuItem_File";
            this.iMenuItem_File.Size = new System.Drawing.Size( 35, 20 );
            this.iMenuItem_File.Text = "&File";
            // 
            // iMenuItem_File_SaveAs
            // 
            this.iMenuItem_File_SaveAs.Name = "iMenuItem_File_SaveAs";
            this.iMenuItem_File_SaveAs.Size = new System.Drawing.Size( 152, 22 );
            this.iMenuItem_File_SaveAs.Text = "Save &As...";
            this.iMenuItem_File_SaveAs.Click += new System.EventHandler( this.iMenuItem_File_SaveAs_Click );
            // 
            // iMenuItem_File_Exit
            // 
            this.iMenuItem_File_Exit.Name = "iMenuItem_File_Exit";
            this.iMenuItem_File_Exit.Size = new System.Drawing.Size( 152, 22 );
            this.iMenuItem_File_Exit.Text = "E&xit";
            this.iMenuItem_File_Exit.Click += new System.EventHandler( this.iMenuItem_File_Exit_Click );
            // 
            // iSplitContainer
            // 
            this.iSplitContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iSplitContainer.Location = new System.Drawing.Point( 0, 24 );
            this.iSplitContainer.Name = "iSplitContainer";
            this.iSplitContainer.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // iSplitContainer.Panel1
            // 
            this.iSplitContainer.Panel1.Controls.Add( this.groupBox32 );
            // 
            // iSplitContainer.Panel2
            // 
            this.iSplitContainer.Panel2.Controls.Add( this.iMemAnalysisDetailedInfoForSymbolGroupBox );
            this.iSplitContainer.Size = new System.Drawing.Size( 738, 425 );
            this.iSplitContainer.SplitterDistance = 116;
            this.iSplitContainer.TabIndex = 54;
            // 
            // groupBox32
            // 
            this.groupBox32.Controls.Add( this.iListView );
            this.groupBox32.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox32.Location = new System.Drawing.Point( 0, 0 );
            this.groupBox32.Name = "groupBox32";
            this.groupBox32.Size = new System.Drawing.Size( 738, 116 );
            this.groupBox32.TabIndex = 51;
            this.groupBox32.TabStop = false;
            this.groupBox32.Text = "Memory Analysis by Symbol";
            // 
            // iListView
            // 
            this.iListView.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iListView.Columns.AddRange( new System.Windows.Forms.ColumnHeader[] {
            this.iColHdrTotals,
            this.iColHdrObjectAllocCount,
            this.iColHdrObjectFreeCount,
            this.iColHdrTotalAllocSize,
            this.iColHdrFreedMemory,
            this.iColHdrNetAllocSize,
            this.iColHdrSymbolName} );
            this.iListView.Font = new System.Drawing.Font( "Lucida Sans Unicode", 7.25F );
            this.iListView.FullRowSelect = true;
            this.iListView.GridLines = true;
            this.iListView.Location = new System.Drawing.Point( 6, 16 );
            this.iListView.MultiSelect = false;
            this.iListView.Name = "iListView";
            this.iListView.Size = new System.Drawing.Size( 724, 94 );
            this.iListView.TabIndex = 47;
            this.iListView.UseCompatibleStateImageBehavior = false;
            this.iListView.View = System.Windows.Forms.View.Details;
            this.iListView.SelectedIndexChanged += new System.EventHandler( this.iListView_SelectedIndexChanged );
            // 
            // iColHdrTotals
            // 
            this.iColHdrTotals.Text = "";
            this.iColHdrTotals.Width = 57;
            // 
            // iColHdrObjectAllocCount
            // 
            this.iColHdrObjectAllocCount.Text = "Alloc. Count";
            this.iColHdrObjectAllocCount.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColHdrObjectAllocCount.Width = 83;
            // 
            // iColHdrObjectFreeCount
            // 
            this.iColHdrObjectFreeCount.Text = "Free\'d Count";
            this.iColHdrObjectFreeCount.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColHdrObjectFreeCount.Width = 81;
            // 
            // iColHdrTotalAllocSize
            // 
            this.iColHdrTotalAllocSize.Text = "Allocated Memory";
            this.iColHdrTotalAllocSize.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColHdrTotalAllocSize.Width = 100;
            // 
            // iColHdrFreedMemory
            // 
            this.iColHdrFreedMemory.Text = "Free\'d Memory";
            this.iColHdrFreedMemory.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColHdrFreedMemory.Width = 84;
            // 
            // iColHdrNetAllocSize
            // 
            this.iColHdrNetAllocSize.Text = "Net";
            this.iColHdrNetAllocSize.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.iColHdrNetAllocSize.Width = 108;
            // 
            // iColHdrSymbolName
            // 
            this.iColHdrSymbolName.Text = "Symbol Name";
            this.iColHdrSymbolName.Width = 362;
            // 
            // iMemAnalysisDetailedInfoForSymbolGroupBox
            // 
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Controls.Add( this.iBT_CopyToClipboard );
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Controls.Add( this.iGrid );
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Location = new System.Drawing.Point( 0, 0 );
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Name = "iMemAnalysisDetailedInfoForSymbolGroupBox";
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Size = new System.Drawing.Size( 738, 305 );
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.TabIndex = 52;
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.TabStop = false;
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.Text = "Detailed Analysis for Symbol";
            // 
            // iBT_CopyToClipboard
            // 
            this.iBT_CopyToClipboard.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iBT_CopyToClipboard.Location = new System.Drawing.Point( 7, 270 );
            this.iBT_CopyToClipboard.Name = "iBT_CopyToClipboard";
            this.iBT_CopyToClipboard.Size = new System.Drawing.Size( 723, 31 );
            this.iBT_CopyToClipboard.TabIndex = 1;
            this.iBT_CopyToClipboard.Text = "Copy to Clipboard...";
            this.iBT_CopyToClipboard.UseVisualStyleBackColor = true;
            // 
            // iGrid
            // 
            this.iGrid.AlternatingRowColor = System.Drawing.Color.WhiteSmoke;
            this.iGrid.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iGrid.ColumnModel = this.iGrid_ColModel;
            this.iGrid.Font = new System.Drawing.Font( "Microsoft Sans Serif", 7.25F );
            this.iGrid.FullRowSelect = true;
            this.iGrid.GridLines = XPTable.Models.GridLines.Both;
            this.iGrid.HeaderFont = new System.Drawing.Font( "Lucida Sans Unicode", 7.25F );
            this.iGrid.Location = new System.Drawing.Point( 7, 19 );
            this.iGrid.Name = "iGrid";
            this.iGrid.Size = new System.Drawing.Size( 723, 249 );
            this.iGrid.TabIndex = 0;
            this.iGrid.TableModel = this.iGrid_TableModel;
            this.iGrid.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iGrid_KeyDown );
            this.iGrid.CellClick += new XPTable.Events.CellMouseEventHandler( this.iGrid_CellClick );
            // 
            // MemAnalysisStatisticalForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.ClientSize = new System.Drawing.Size( 738, 449 );
            this.Controls.Add( this.iSplitContainer );
            this.Controls.Add( this.iCloseButton );
            this.Controls.Add( this.menuStrip1 );
            this.Name = "MemAnalysisStatisticalForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Memory Analysis";
            this.Load += new System.EventHandler( this.MemAnalysisForm_Load );
            this.menuStrip1.ResumeLayout( false );
            this.menuStrip1.PerformLayout();
            this.iSplitContainer.Panel1.ResumeLayout( false );
            this.iSplitContainer.Panel2.ResumeLayout( false );
            this.iSplitContainer.ResumeLayout( false );
            this.groupBox32.ResumeLayout( false );
            this.iMemAnalysisDetailedInfoForSymbolGroupBox.ResumeLayout( false );
            ( (System.ComponentModel.ISupportInitialize) ( this.iGrid ) ).EndInit();
            this.ResumeLayout( false );
            this.PerformLayout();

		}
		#endregion

		#region Event handlers
		private void MemAnalysisForm_Load(object sender, System.EventArgs e)
		{
			PrepareMemoryAnalysisData();
		}

		private void iCloseButton_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void iTimerRefreshGrid_Tick(object sender, System.EventArgs e)
		{
			lock( this )
			{
				iTimerRefreshGrid.Enabled = false;
				iTimerRefreshGrid.Stop();
				UpdateMemoryAnalysisGridForSelectedSymbol();
			}
		}

		private void iListView_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			if	( iListView.SelectedIndices.Count > 0 )
			{
				// Check the selected item isn't one of the footer (total) rows
				int selectedIndex = iListView.SelectedIndices[0];
				if	( selectedIndex >= 0 && selectedIndex < iListView.Items.Count - 2 )
				{
					// Start the timer - this will refresh the grid when it expires...
					lock( this )
					{
						iGridIsDirty = true;
						iStopPopulatingGrid = true;
						//
						iTimerRefreshGrid.Stop();
						iTimerRefreshGrid.Enabled = true;
						iTimerRefreshGrid.Start();
					}
				}
			}
		}

		private void iGrid_KeyDown(object sender, KeyEventArgs e)
		{
			if	( e.KeyCode == Keys.C && e.Control && iListView.SelectedIndices.Count > 0 )
			{
                XPTable.Models.Row[] rows = iGrid.SelectedItems;
                if ( rows.Length > 0 && rows[ 0 ].Tag != null && rows[ 0 ].Tag is MemOpBase )
                {
                    MemOpBase op = rows[ 0 ].Tag as MemOpBase;
                    //
                    Clipboard.SetDataObject( op.ToString(), true );
					e.Handled = true;
                }
			}
		}

        private void iGrid_CellClick( object sender, XPTable.Events.CellMouseEventArgs e )
        {
            if ( e.Cell.Tag != null && e.Cell.Tag is MemOpBase )
            {
				// Get the object & collection
				MemOpBase currentObject = (MemOpBase) e.Cell.Tag;
                MemObjStatisticalCollection collection = (MemObjStatisticalCollection) currentObject.Collection;

                // If we have a linked item, then we'll attempt to show it
                if ( currentObject.Link != null )
                {
                    MemOpBase linkedOp = currentObject.Link;

                    // We know that the linked item should be in the same collection - so search for it.
                    int rowIndex = 0;
                    foreach( XPTable.Models.Row row in iGrid.TableModel.Rows )
                    {
                        // The first row's tag has been setup to point to a MemOpBase object for the entire row.
                        MemOpBase rowTagEntry = (MemOpBase) row.Tag;
                        if ( rowTagEntry.CellAddress == linkedOp.CellAddress && 
                             rowTagEntry.OperationIndex == linkedOp.OperationIndex &&
                             rowTagEntry.AllocationNumber == linkedOp.AllocationNumber &&
                             rowTagEntry != currentObject )
                        {
                            // This is the one to focus to.
                            iGrid.TableModel.Selections.SelectCell( rowIndex, 5 );
                            iGrid.EnsureVisible( rowIndex, 5 );
                            iGrid.Select();
                            break;
                        }

                        // No match...
                        ++rowIndex;
                    }
                }
            }
        }

        private void iMenuItem_File_SaveAs_Click( object sender, EventArgs e )
        {

        }

        private void iMenuItem_File_Exit_Click( object sender, EventArgs e )
        {
            this.Close();
        }
        #endregion

		#region Internal methods
		private void PrepareMemoryAnalysisData()
		{
			try
			{
				Cursor.Current = Cursors.WaitCursor;
				iListView.Enabled = false;

				long totalAllocCount = 0;
				long totalFreeCount = 0;
				long totalAllocSize = 0;
				long totalFreeSize = 0;
				long totalNetSize = 0;

				// Ensure that each allocation-symbol is added to the symbol listbox
				MemObjStatisticalData data = iParser.Data;
				//
				iListView.BeginUpdate();
				int count = data.CollectionCount;
				for(int i=0; i<count; i++)
				{
					MemObjStatisticalCollection collection = data.CollectionAt( i );
					if	( collection.Count > 0 )
					{
						System.Diagnostics.Debug.Assert( collection[0] is MemOpAllocation );
						MemOpAllocation memObj = (MemOpAllocation) collection[0];
						//
						long allocCount = collection.AllocationCount;
						long freeCount = collection.DeallocationCount;
						long allocSize = collection.TotalAmountOfAllocatedMemory;
						long freeSize = collection.TotalAmountOfDeallocatedMemory;
						long netAllocSize = collection.TotalMemoryAllocatedButNotFreed;
						//
						ListViewItem item = new ListViewItem( "" );
						item.Tag = collection;
						item.SubItems.Add( allocCount.ToString() );
						item.SubItems.Add( freeCount.ToString() );
						item.SubItems.Add( allocSize.ToString() );
						item.SubItems.Add( freeSize.ToString() );
						item.SubItems.Add( netAllocSize.ToString() );
						string symbolText = "Unknown";
						if	( memObj.LinkRegisterSymbol != null && memObj.LinkRegisterSymbol.Symbol != null )
						{
							symbolText = memObj.LinkRegisterSymbol.Symbol.ToString();
						}
						item.SubItems.Add( symbolText );
						iListView.Items.Add( item );

						// Update totals
						totalAllocCount += allocCount;
						totalFreeCount += freeCount;
						totalAllocSize += allocSize;
						totalFreeSize += freeSize;
						totalNetSize += netAllocSize;
					}

					if	( count % 100 != 0 )
						Application.DoEvents();
				}
				iListView.EndUpdate();

				// Make the first item selected
				if	( iListView.Items.Count > 0 && iListView.SelectedIndices.Count == 0 )
				{
					// Add total item
					iListView.Items.Add( new ListViewItem( "" ) );
					//
					ListViewItem totalItem = new ListViewItem( "Totals:" );
					totalItem.SubItems.Add( totalAllocCount.ToString() );
					totalItem.SubItems.Add( totalFreeCount.ToString() );
					totalItem.SubItems.Add( totalAllocSize.ToString() );
					totalItem.SubItems.Add( totalFreeSize.ToString() );
					totalItem.SubItems.Add( totalNetSize.ToString() );
					totalItem.SubItems.Add( "" );
					iListView.Items.Add( totalItem );
					//
					iListView.Items[0].Selected = true;
					iListView.Select();
				}
			}
			finally
			{
				Cursor.Current = Cursors.Default;
				iListView.Enabled = true;
				iListView.Select();
			}
		}

        private void PopulateTableRows( MemObjStatisticalCollection aCollection )
        {
            // Clear existing content
            iGrid.BeginUpdate();
            iGrid.TableModel.Rows.Clear();
            iGrid.Tag = aCollection;

            // Make new content
            int count = aCollection.Count;
            for ( int i = 0; i < count; i++ )
            {
                // The entry we are rendering
                MemOpBase baseObject = aCollection[ i ];

                // Only initialised if we are dealing with an allocation (or realloc) type cell.
                MemOpAllocation memObj = null;

                // The color format for the entire row.
                System.Drawing.Color rowColor = Color.Black;

                // The row we are creating
                XPTable.Models.Row row = new XPTable.Models.Row();

                // Set tag for the row
                row.Tag = baseObject;

                // Common items
                // ============
                XPTable.Models.Cell opIndexCell = new XPTable.Models.Cell( baseObject.OperationIndex.ToString( "d6" ) );
                row.Cells.Add( opIndexCell );
                XPTable.Models.Cell lineNumberCell = new XPTable.Models.Cell( baseObject.LineNumber.ToString( "d6" ) );
                row.Cells.Add( lineNumberCell );
                XPTable.Models.Cell cellAddressCell = new XPTable.Models.Cell( baseObject.CellAddress.ToString( "x8" ) );
                row.Cells.Add( cellAddressCell );
                XPTable.Models.Cell functionCell = new XPTable.Models.Cell( " " + baseObject.FunctionName );
                row.Cells.Add( functionCell );

                // Row Color & Object Association
                // ==============================
                if ( baseObject is MemOpAllocation )
                {
                    // Allocation
                    memObj = (MemOpAllocation) baseObject;
                    rowColor = Color.Blue;
                }
                else if ( baseObject is MemOpFree )
                {
                    // Deallocation
                    if ( baseObject.Link != null )
                    {
                        memObj = (MemOpAllocation) baseObject.Link;
                    }
                    else
                    {
                        memObj = null;
                    }
                    rowColor = Color.Green;
                }
                else if ( baseObject is MemOpReallocation )
                {
                    // Reallocation
                    if ( baseObject.Link != null )
                    {
                        memObj = (MemOpAllocation) baseObject.Link;
                    }
                    else
                    {
                        memObj = null;
                    }
                    rowColor = Color.Purple;
                }

                // Allocation size
                // ===============
                string allocationSize = "???";
                if ( memObj != null )
                {
                    allocationSize = memObj.AllocationSize.ToString();
                }
                row.Cells.Add( new XPTable.Models.Cell( allocationSize + "  " ) );

                // Heap size
                // =========
                row.Cells.Add( new XPTable.Models.Cell( baseObject.HeapSize.ToString() + "  " ) );

                // Associated object
                // =================
                MemOpAllocation symbolObject = memObj;
                if ( memObj != null && baseObject.Link != null )
                {
                    // If we have an associated link item, we can connect the two items together
                    string associatedText = string.Empty;
                    if ( baseObject.IsAllocationType )
                    {
                        associatedText = "Free'd by op #:  " + baseObject.Link.OperationIndex.ToString( "d5" );
                    }
                    else if ( baseObject.IsReallocationType )
                    {
                        associatedText = "First alloc'd by op #: " + baseObject.Link.OperationIndex.ToString( "d5" );
                        symbolObject = ( baseObject.Link as MemOpAllocation );
                    }
                    else
                    {
                        associatedText = "Alloc'd by op #: " + baseObject.Link.OperationIndex.ToString( "d5" );
                    }

                    // We store the object with the cell so that we can handle hyperlinks between
                    // associated objects.
                    XPTable.Models.Cell associatedCell = new XPTable.Models.Cell( associatedText );
                    associatedCell.Tag = baseObject;

                    // Make it look like a hyperlink
                    associatedCell.Font = new Font( iGrid.Font.FontFamily.Name, iGrid.Font.SizeInPoints, System.Drawing.FontStyle.Underline );

                    // Add the cell to the row
                    row.Cells.Add( associatedCell );
                }
                else
                {
                    if ( baseObject.IsAllocationType )
                    {
                        if ( memObj != null )
                        {
                            symbolObject = memObj;
                        }

                        rowColor = Color.Red;
                        row.Font = new System.Drawing.Font( iGrid.Font.FontFamily.Name, iGrid.Font.SizeInPoints, System.Drawing.FontStyle.Regular );
                        row.Cells.Add( new XPTable.Models.Cell( "Object never free'd!" ) );
                    }
                    else
                    {
                        row.Cells.Add( new XPTable.Models.Cell( "???!" ) );
                    }
                }

                // Set row color
                // =============
                row.ForeColor = rowColor;

                // Add row
                // =======
                iGrid.TableModel.Rows.Add( row );

                // Event handling
                // ==============
                if ( i % 100 != 0 )
                {
                    Application.DoEvents();
                }
                lock ( this )
                {
                    if ( iStopPopulatingGrid )
                    {
                        break;
                    }
                }
            }

            // If no items, then dim table
            iGrid.Enabled = ( count > 0 );
            iGrid.EndUpdate();
        }

        private void UpdateMemoryAnalysisGridForSelectedSymbol()
		{
			lock( this )
			{
				iStopPopulatingGrid = false;
			}
			//
			try
			{
				Cursor.Current = Cursors.WaitCursor;
				//
				if	( iListView.SelectedIndices.Count > 0 )
				{
					MemObjStatisticalData data = iParser.Data;
					int selectedIndex = iListView.SelectedIndices[0];

					ListViewItem selectedItem = iListView.SelectedItems[ 0 ];
					if	( selectedItem.Tag != null )
					{
						MemObjStatisticalCollection collection = (MemObjStatisticalCollection) selectedItem.Tag;
						MemOpBase baseObject = collection[0];
						System.Diagnostics.Debug.Assert( baseObject is MemOpAllocation );
						MemOpAllocation memObj = (MemOpAllocation) baseObject;
						//
                        string symbolText = "Unknown";
						if	( memObj.LinkRegisterSymbol != null && memObj.LinkRegisterSymbol.Symbol != null )
						{
							symbolText = memObj.LinkRegisterSymbol.Symbol.ToString();
						}
						iMemAnalysisDetailedInfoForSymbolGroupBox.Text = @"Detailed Analysis for Symbol '" + symbolText + @"'";
						//
                        PopulateTableRows( collection );
					}
				}
			}
			catch(Exception)
			{
				iGridIsDirty = false;
			}
			finally
			{
				Cursor.Current = Cursors.Default;
				iStopPopulatingGrid = false;
				//iListView.Enabled = false;
				//iListView.Select();
			}

			lock( this )
			{
				iGridIsDirty = false;
			}
		}
		#endregion

		#region Data members
		private bool iGridIsDirty = false;
		private bool iStopPopulatingGrid;
		private MemAnalysisStatisticalParser iParser;
		private XmlSettings iSettings;
		#endregion
    }
}
