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
	public class MemAnalysisRegionalForm : System.Windows.Forms.Form
	{
		#region Windows Form Designer Code
		private System.Windows.Forms.GroupBox groupBox1;
		private System.Windows.Forms.ListView iListView;
		private System.Windows.Forms.ColumnHeader iColRegionNumber;
		private System.Windows.Forms.ColumnHeader iColLineNumber;
		private System.Windows.Forms.ColumnHeader iColCountAllocs;
		private System.Windows.Forms.ColumnHeader iColCountFrees;
		private System.Windows.Forms.ColumnHeader iColMemAllocated;
		private System.Windows.Forms.ColumnHeader iColMemFreed;
		private System.Windows.Forms.ColumnHeader iColNet;
		private System.Windows.Forms.ColumnHeader iColRegionText;
		private System.Windows.Forms.GroupBox groupBox2;
		private System.Windows.Forms.TextBox iMarkerStartText;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.Label label3;
		private System.Windows.Forms.TextBox iMarkerEndText;
		private System.Windows.Forms.Panel iPnl_Upper;
		private System.Windows.Forms.Panel iPnl_Lower;
		private System.Windows.Forms.Splitter iSplitter_Horizontal;
		private XPTable.Models.Table iTable;
		private XPTable.Models.TableModel iTable_Model;
		private XPTable.Models.ColumnModel iTable_ColModel;
		private XPTable.Models.TextColumn iCol_MemOp;
		private XPTable.Models.TextColumn iCol_LineNumber;
		private XPTable.Models.TextColumn iCol_CellAddr;
		private XPTable.Models.TextColumn iCol_AllocSize;
		private XPTable.Models.TextColumn iCol_Type;
		private XPTable.Models.TextColumn iCol_HeapSize;
		private XPTable.Models.TextColumn iCol_AssociatedOp;
		private XPTable.Models.TextColumn iCol_Symbol;
		private System.ComponentModel.Container components = null;
		#endregion

		#region Constructors & destructor
		public MemAnalysisRegionalForm( MemAnalysisParserBase aParser, XmlSettings aSettings )
		{
			InitializeComponent();

			System.Diagnostics.Debug.Assert( aParser is MemAnalysisRegionalParser );
			iParser = (MemAnalysisRegionalParser) aParser;
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
			this.iPnl_Upper = new System.Windows.Forms.Panel();
			this.groupBox1 = new System.Windows.Forms.GroupBox();
			this.iListView = new System.Windows.Forms.ListView();
			this.iColRegionNumber = new System.Windows.Forms.ColumnHeader();
			this.iColLineNumber = new System.Windows.Forms.ColumnHeader();
			this.iColCountAllocs = new System.Windows.Forms.ColumnHeader();
			this.iColCountFrees = new System.Windows.Forms.ColumnHeader();
			this.iColMemAllocated = new System.Windows.Forms.ColumnHeader();
			this.iColMemFreed = new System.Windows.Forms.ColumnHeader();
			this.iColNet = new System.Windows.Forms.ColumnHeader();
			this.iColRegionText = new System.Windows.Forms.ColumnHeader();
			this.iPnl_Lower = new System.Windows.Forms.Panel();
			this.groupBox2 = new System.Windows.Forms.GroupBox();
			this.iTable = new XPTable.Models.Table();
			this.iTable_ColModel = new XPTable.Models.ColumnModel();
			this.iCol_MemOp = new XPTable.Models.TextColumn();
			this.iCol_LineNumber = new XPTable.Models.TextColumn();
			this.iCol_CellAddr = new XPTable.Models.TextColumn();
			this.iCol_Type = new XPTable.Models.TextColumn();
			this.iCol_AllocSize = new XPTable.Models.TextColumn();
			this.iCol_HeapSize = new XPTable.Models.TextColumn();
			this.iCol_AssociatedOp = new XPTable.Models.TextColumn();
			this.iCol_Symbol = new XPTable.Models.TextColumn();
			this.iTable_Model = new XPTable.Models.TableModel();
			this.iMarkerStartText = new System.Windows.Forms.TextBox();
			this.label1 = new System.Windows.Forms.Label();
			this.label2 = new System.Windows.Forms.Label();
			this.label3 = new System.Windows.Forms.Label();
			this.iMarkerEndText = new System.Windows.Forms.TextBox();
			this.iSplitter_Horizontal = new System.Windows.Forms.Splitter();
			this.iPnl_Upper.SuspendLayout();
			this.groupBox1.SuspendLayout();
			this.iPnl_Lower.SuspendLayout();
			this.groupBox2.SuspendLayout();
			((System.ComponentModel.ISupportInitialize)(this.iTable)).BeginInit();
			this.SuspendLayout();
			// 
			// iPnl_Upper
			// 
			this.iPnl_Upper.Controls.Add(this.groupBox1);
			this.iPnl_Upper.Dock = System.Windows.Forms.DockStyle.Top;
			this.iPnl_Upper.Location = new System.Drawing.Point(0, 0);
			this.iPnl_Upper.Name = "iPnl_Upper";
			this.iPnl_Upper.Size = new System.Drawing.Size(1016, 172);
			this.iPnl_Upper.TabIndex = 3;
			// 
			// groupBox1
			// 
			this.groupBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
				| System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.groupBox1.Controls.Add(this.iListView);
			this.groupBox1.Location = new System.Drawing.Point(8, 5);
			this.groupBox1.Name = "groupBox1";
			this.groupBox1.Size = new System.Drawing.Size(1006, 164);
			this.groupBox1.TabIndex = 1;
			this.groupBox1.TabStop = false;
			this.groupBox1.Text = "Identified regions...";
			// 
			// iListView
			// 
			this.iListView.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
				| System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.iListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
																						this.iColRegionNumber,
																						this.iColLineNumber,
																						this.iColCountAllocs,
																						this.iColCountFrees,
																						this.iColMemAllocated,
																						this.iColMemFreed,
																						this.iColNet,
																						this.iColRegionText});
			this.iListView.FullRowSelect = true;
			this.iListView.GridLines = true;
			this.iListView.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.Nonclickable;
			this.iListView.Location = new System.Drawing.Point(12, 20);
			this.iListView.Name = "iListView";
			this.iListView.Size = new System.Drawing.Size(980, 136);
			this.iListView.TabIndex = 0;
			this.iListView.View = System.Windows.Forms.View.Details;
			this.iListView.SelectedIndexChanged += new System.EventHandler(this.iListView_SelectedIndexChanged);
			// 
			// iColRegionNumber
			// 
			this.iColRegionNumber.Text = "Region #";
			// 
			// iColLineNumber
			// 
			this.iColLineNumber.Text = "Line #";
			// 
			// iColCountAllocs
			// 
			this.iColCountAllocs.Text = "Alloc. Count";
			this.iColCountAllocs.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.iColCountAllocs.Width = 73;
			// 
			// iColCountFrees
			// 
			this.iColCountFrees.Text = "Free\'d Count";
			this.iColCountFrees.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.iColCountFrees.Width = 75;
			// 
			// iColMemAllocated
			// 
			this.iColMemAllocated.Text = "Allocated Memory";
			this.iColMemAllocated.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.iColMemAllocated.Width = 96;
			// 
			// iColMemFreed
			// 
			this.iColMemFreed.Text = "Free\'d Memory";
			this.iColMemFreed.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.iColMemFreed.Width = 84;
			// 
			// iColNet
			// 
			this.iColNet.Text = "Net";
			this.iColNet.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.iColNet.Width = 80;
			// 
			// iColRegionText
			// 
			this.iColRegionText.Text = "";
			this.iColRegionText.Width = 337;
			// 
			// iPnl_Lower
			// 
			this.iPnl_Lower.Controls.Add(this.groupBox2);
			this.iPnl_Lower.Dock = System.Windows.Forms.DockStyle.Fill;
			this.iPnl_Lower.Location = new System.Drawing.Point(0, 172);
			this.iPnl_Lower.Name = "iPnl_Lower";
			this.iPnl_Lower.Size = new System.Drawing.Size(1016, 569);
			this.iPnl_Lower.TabIndex = 4;
			// 
			// groupBox2
			// 
			this.groupBox2.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
				| System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.groupBox2.Controls.Add(this.iTable);
			this.groupBox2.Controls.Add(this.iMarkerStartText);
			this.groupBox2.Controls.Add(this.label1);
			this.groupBox2.Controls.Add(this.label2);
			this.groupBox2.Controls.Add(this.label3);
			this.groupBox2.Controls.Add(this.iMarkerEndText);
			this.groupBox2.Location = new System.Drawing.Point(8, 5);
			this.groupBox2.Name = "groupBox2";
			this.groupBox2.Size = new System.Drawing.Size(1006, 558);
			this.groupBox2.TabIndex = 2;
			this.groupBox2.TabStop = false;
			this.groupBox2.Text = "Memory operations within region";
			// 
			// iTable
			// 
			this.iTable.AlternatingRowColor = System.Drawing.Color.Gainsboro;
			this.iTable.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
				| System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.iTable.ColumnModel = this.iTable_ColModel;
			this.iTable.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.iTable.FullRowSelect = true;
			this.iTable.HeaderFont = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.iTable.Location = new System.Drawing.Point(88, 47);
			this.iTable.Name = "iTable";
			this.iTable.Size = new System.Drawing.Size(904, 476);
			this.iTable.TabIndex = 3;
			this.iTable.TableModel = this.iTable_Model;
			this.iTable.CellClick += new XPTable.Events.CellMouseEventHandler(this.iTable_CellClick);
			// 
			// iTable_ColModel
			// 
			this.iTable_ColModel.Columns.AddRange(new XPTable.Models.Column[] {
																				  this.iCol_MemOp,
																				  this.iCol_LineNumber,
																				  this.iCol_CellAddr,
																				  this.iCol_Type,
																				  this.iCol_AllocSize,
																				  this.iCol_HeapSize,
																				  this.iCol_AssociatedOp,
																				  this.iCol_Symbol});
			// 
			// iCol_MemOp
			// 
			this.iCol_MemOp.Text = "Op. #";
			this.iCol_MemOp.Width = 72;
			// 
			// iCol_LineNumber
			// 
			this.iCol_LineNumber.Text = "Line";
			this.iCol_LineNumber.Width = 62;
			// 
			// iCol_CellAddr
			// 
			this.iCol_CellAddr.Text = "Cell Addr.";
			this.iCol_CellAddr.Width = 62;
			// 
			// iCol_Type
			// 
			this.iCol_Type.Text = "Type";
			this.iCol_Type.Width = 110;
			// 
			// iCol_AllocSize
			// 
			this.iCol_AllocSize.Alignment = XPTable.Models.ColumnAlignment.Right;
			this.iCol_AllocSize.Text = "Alloc. Size";
			this.iCol_AllocSize.Width = 64;
			// 
			// iCol_HeapSize
			// 
			this.iCol_HeapSize.Alignment = XPTable.Models.ColumnAlignment.Right;
			this.iCol_HeapSize.Text = "Heap Size";
			this.iCol_HeapSize.Width = 64;
			// 
			// iCol_AssociatedOp
			// 
			this.iCol_AssociatedOp.Text = "Associated Op.";
			this.iCol_AssociatedOp.Width = 170;
			// 
			// iCol_Symbol
			// 
			this.iCol_Symbol.Text = "Symbol";
			this.iCol_Symbol.Width = 270;
			// 
			// iMarkerStartText
			// 
			this.iMarkerStartText.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.iMarkerStartText.Location = new System.Drawing.Point(88, 22);
			this.iMarkerStartText.Name = "iMarkerStartText";
			this.iMarkerStartText.ReadOnly = true;
			this.iMarkerStartText.Size = new System.Drawing.Size(904, 20);
			this.iMarkerStartText.TabIndex = 1;
			this.iMarkerStartText.Text = "";
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(16, 24);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(64, 16);
			this.label1.TabIndex = 2;
			this.label1.Text = "Start text:";
			this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(16, 52);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(64, 20);
			this.label2.TabIndex = 2;
			this.label2.Text = "Operations:";
			this.label2.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// label3
			// 
			this.label3.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
			this.label3.Location = new System.Drawing.Point(16, 530);
			this.label3.Name = "label3";
			this.label3.Size = new System.Drawing.Size(64, 16);
			this.label3.TabIndex = 2;
			this.label3.Text = "End text:";
			this.label3.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// iMarkerEndText
			// 
			this.iMarkerEndText.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.iMarkerEndText.Location = new System.Drawing.Point(88, 528);
			this.iMarkerEndText.Name = "iMarkerEndText";
			this.iMarkerEndText.ReadOnly = true;
			this.iMarkerEndText.Size = new System.Drawing.Size(904, 20);
			this.iMarkerEndText.TabIndex = 1;
			this.iMarkerEndText.Text = "";
			// 
			// iSplitter_Horizontal
			// 
			this.iSplitter_Horizontal.Dock = System.Windows.Forms.DockStyle.Top;
			this.iSplitter_Horizontal.Location = new System.Drawing.Point(0, 172);
			this.iSplitter_Horizontal.Name = "iSplitter_Horizontal";
			this.iSplitter_Horizontal.Size = new System.Drawing.Size(1016, 3);
			this.iSplitter_Horizontal.TabIndex = 5;
			this.iSplitter_Horizontal.TabStop = false;
			// 
			// MemAnalysisRegionalForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(1016, 741);
			this.Controls.Add(this.iSplitter_Horizontal);
			this.Controls.Add(this.iPnl_Lower);
			this.Controls.Add(this.iPnl_Upper);
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.MinimumSize = new System.Drawing.Size(1024, 456);
			this.Name = "MemAnalysisRegionalForm";
			this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
			this.Text = "Marker-based Analysis";
			this.Load += new System.EventHandler(this.MemAnalysisRegionalForm_Load);
			this.iPnl_Upper.ResumeLayout(false);
			this.groupBox1.ResumeLayout(false);
			this.iPnl_Lower.ResumeLayout(false);
			this.groupBox2.ResumeLayout(false);
			((System.ComponentModel.ISupportInitialize)(this.iTable)).EndInit();
			this.ResumeLayout(false);

		}
		#endregion

		#region Event handlers
		private void MemAnalysisRegionalForm_Load(object sender, System.EventArgs e)
		{
			PrepareListView();
		}

		private void iListView_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			if	( iListView.SelectedIndices.Count > 0 )
			{
				// Check the selected item isn't one of the footer (total) rows
				ListViewItem item = iListView.SelectedItems[ 0 ];
				if	( item.Tag != null )
				{
					PrepareGridForSelectedListItem();
				}
			}
		}

		private void iTable_CellClick(object sender, XPTable.Events.CellMouseEventArgs e)
		{
			if	( e.Cell.Tag is MemOpBase )
			{
				// Get the object
				MemOpBase baseObject = (MemOpBase) e.Cell.Tag;
				System.Diagnostics.Debug.Assert( baseObject.Link != null );

				// Get the current collection
				if	( iListView.SelectedIndices.Count > 0 && !iPreparingGrid )
				{
					ListViewItem listItem = iListView.SelectedItems[ 0 ];
					if	( listItem.Tag != null && listItem.Tag is MemObjRegionalCollection )
					{
						MemObjRegionalCollection collection = (MemObjRegionalCollection) listItem.Tag;

						// Get the associated (linked) collection
						int collectionIndex;
						int itemIndex;
						MemOpBase linkedItem;
						MemObjRegionalCollection linkedCollection = iParser.Data.CollectionByOperationIndex( baseObject.Link.OperationIndex,
							baseObject.Link.Class,
							out linkedItem,
							out collectionIndex,
							out itemIndex );

						// Did we find a linked item?
						if	( linkedCollection != null && linkedItem != null )
						{
							// Select the correct list item
							if	( collection != linkedCollection )
							{
								// We need to jump to a different collection...
								if	( linkedCollection.RegionStart.MatchedRegionText || !iParser.Options.MarkerOperationsOutsideRegionToBeIgnored )
								{
									if	( iParser.Options.MarkerOperationsOutsideRegionToBeIgnored )
									{
										collectionIndex = CollectionIndex( linkedCollection );
									}
									iListView.Items[ iListView.SelectedIndices[0] ].Selected = false;
									iListView.Items[ collectionIndex ].Selected = true;
									iListView.Select();
								}
							}

							// Select it
							iTable.TableModel.Selections.Clear();
							iTable.TableModel.Selections.AddCell( itemIndex, 6 );
							iTable.EnsureVisible( itemIndex, 6 );
							iTable.Select();
						}
					}
				}
			}
		}
		#endregion

		#region Internal methods
		private void PrepareListView()
		{
			try
			{
				iListView.Enabled = false;
				Cursor.Current = Cursors.WaitCursor;

				long totalAllocCount = 0;
				long totalFreeCount = 0;
				long totalAllocSize = 0;
				long totalDeallocSize = 0;
				long totalNetSize = 0;

				// Ensure that each allocation-symbol is added to the symbol listbox
				MemObjRegionalData data = iParser.Data;
				//
				iListView.BeginUpdate();
				int count = data.Count;
				for(int i=0; i<count; i++)
				{
					MemObjRegionalCollection collection = data[ i ];
					//
					if	( collection.RegionStart.MatchedRegionText || !iParser.Options.MarkerOperationsOutsideRegionToBeIgnored  )
					{
						long allocCount = collection.AllocationCount;
						long freeCount = collection.DeallocationCount;
						long allocSize = collection.TotalAmountOfAllocatedMemory;
						long deallocSize = collection.TotalAmountOfDeallocatedMemory;
						long netSize = collection.TotalMemoryAllocatedButNotFreed;
						//
						ListViewItem item = new ListViewItem( (i+1).ToString("d8") );
						item.SubItems.Add( collection.RegionStart.LineNumber.ToString("d8") );
						item.SubItems.Add( allocCount.ToString() );
						item.SubItems.Add( freeCount.ToString() );
						item.SubItems.Add( allocSize.ToString() );
						item.SubItems.Add( deallocSize.ToString() );
						item.SubItems.Add( netSize.ToString() );
						item.SubItems.Add( collection.RegionStart.RegionText );
						item.Tag = collection;
						iListView.Items.Add( item );

						// Update totals
						totalAllocCount += allocCount;
						totalFreeCount += freeCount;
						totalAllocSize += allocSize;
						totalDeallocSize += deallocSize;
						totalNetSize += netSize;
					}
					//
					if	( count % 100 != 0 )
						Application.DoEvents();
				}

				// Make the first item selected
				if	( iListView.Items.Count > 0 && iListView.SelectedIndices.Count == 0 )
				{
					// Add total item
					iListView.Items.Add( new ListViewItem( "" ) );
					//
					ListViewItem totalItem = new ListViewItem( "Totals:" );
					totalItem.SubItems.Add( "" );
					totalItem.SubItems.Add( totalAllocCount.ToString() );
					totalItem.SubItems.Add( totalFreeCount.ToString() );
					totalItem.SubItems.Add( totalAllocSize.ToString() );
					totalItem.SubItems.Add( totalDeallocSize.ToString() );
					totalItem.SubItems.Add( totalNetSize.ToString() );
					totalItem.SubItems.Add( "" );
					iListView.Items.Add( totalItem );
					//
					iListView.Items[0].Selected = true;
					iListView.Select();
				}

				iListView.EndUpdate();
			}
			finally
			{
				Cursor.Current = Cursors.Default;
				iListView.Enabled = true;
				iListView.Select();
			}
		}

		private void PrepareGridForSelectedListItem()
		{
			if	( !( iPreparingGrid || iListView.SelectedItems.Count == 0 || iListView.SelectedItems[ 0 ].Tag == null ) )
			{
				iPreparingGrid = true;
				iListView.Enabled = false;
				Cursor.Current = Cursors.WaitCursor;
				//
				ListViewItem listItem = iListView.SelectedItems[ 0 ];
				MemObjRegionalCollection collection = (MemObjRegionalCollection) listItem.Tag;
				//
				try
				{
					// First update the text labels to show the marker values
					iMarkerStartText.Text = collection.RegionStart.RegionText;
					iMarkerEndText.Text = collection.RegionEnd.RegionText;

					// Clear existing content
					iTable.TableModel.Rows.Clear();

					// Make new content
					int count = collection.Count;
					for(int i=0; i<count; i++)
					{
						// The entry we are rendering
						MemOpBase baseObject = collection[i];

						// Only initialised if we are dealing with an allocation (or realloc) type cell.
						MemOpAllocation memObj = null;

						// The color format for the entire row.
						System.Drawing.Color rowColor = Color.Black;

						// The row we are creating
						XPTable.Models.Row row = new XPTable.Models.Row();
						
						// Common items
						// ============
						row.Cells.Add( new XPTable.Models.Cell( baseObject.OperationIndex.ToString("d6") ) );
						row.Cells.Add( new XPTable.Models.Cell( baseObject.LineNumber.ToString("d6") ) );
						row.Cells.Add( new XPTable.Models.Cell( baseObject.CellAddress.ToString("x8") ) );
                        row.Cells.Add( new XPTable.Models.Cell( " " + baseObject.FunctionName ) );

						// Row Color & Object Association
						// ==============================
						if	( baseObject is MemOpAllocation )
						{
							// Allocation
							memObj = (MemOpAllocation) baseObject;
							rowColor = Color.Red;
						}
						else if ( baseObject is MemOpFree )
						{
							// Deallocation
							if	( baseObject.Link != null )
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
							if	( baseObject.Link != null )
							{
								memObj = (MemOpAllocation) baseObject.Link;
							}
							else
							{
								memObj = null;
							}
							rowColor = Color.Blue;
						}

						// Allocation size
						// ===============
						string allocationSize = "???";
						if	( memObj != null )
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
						if	( memObj != null && baseObject.Link != null )
						{
							// If we have an associated link item, we can connect the two items together
							string associatedText = string.Empty;
							if	( baseObject.IsAllocationType )
							{
								associatedText = "Free'd by op #:  " + baseObject.Link.OperationIndex.ToString("d5");
							}
							else if ( baseObject.IsReallocationType )
							{
								associatedText = "First alloc'd by op #: " + baseObject.Link.OperationIndex.ToString("d5");
								symbolObject = ( baseObject.Link as MemOpAllocation );
							}
							else
							{
								associatedText = "Alloc'd by op #: " + baseObject.Link.OperationIndex.ToString("d5");
							}

							// We store the object with the cell so that we can handle hyperlinks between
							// associated objects.
							XPTable.Models.Cell associatedCell = new XPTable.Models.Cell( associatedText );
							associatedCell.Tag = baseObject;

							// Make it look like a hyperlink
							associatedCell.Font = new Font( iTable.Font.FontFamily.Name, iTable.Font.SizeInPoints, System.Drawing.FontStyle.Underline );

							// Add the cell to the row
							row.Cells.Add( associatedCell );
						}
						else
						{
							if	( baseObject.IsAllocationType )
							{
								if	( memObj != null )
								{
									symbolObject = memObj;
								}

								rowColor = Color.Red;
								row.Font = new System.Drawing.Font( iTable.Font.FontFamily.Name, iTable.Font.SizeInPoints, System.Drawing.FontStyle.Bold );
								row.Cells.Add( new XPTable.Models.Cell( "Object never free'd!" ) );
							}
							else
							{
								row.Cells.Add( new XPTable.Models.Cell( "???!" ) );
							}
						}

						// Symbol
						// ======
						string symbol = string.Empty;
                        if ( symbolObject != null && symbolObject.LinkRegisterSymbol != null )
						{
                            symbol = memObj.LinkRegisterSymbol.Symbol.ToString();
						}
						row.Cells.Add( new XPTable.Models.Cell( symbol ) );

						// Set row color
						// =============
						row.ForeColor = rowColor;

						// Add row
						// =======
						iTable.TableModel.Rows.Add( row );
					}

					// If no items, then dim table
					iTable.Enabled = ( count > 0 );
				}
				finally
				{
					Cursor.Current = Cursors.Default;
					iPreparingGrid = false;
					iListView.Enabled = true;
					iListView.Select();
				}
			}
		}

		private int CollectionIndex( MemObjRegionalCollection aCollection )
		{
			int ret = -1;
			int index = 0;
			//
			int count = iListView.Items.Count;
			foreach( ListViewItem item in iListView.Items )
			{
				if	( item.Tag != null )
				{
					MemObjRegionalCollection col = (MemObjRegionalCollection) item.Tag;
					if	( col == aCollection )
					{
						index = ret;
						break;
					}
				}
				else
				{
					break;
				}

				++index;
			}
			//
			return index;
		}
		#endregion

		#region Data members
		private bool iPreparingGrid = false;
		private MemAnalysisRegionalParser iParser;
		private XmlSettings iSettings;
		#endregion
	}
}
