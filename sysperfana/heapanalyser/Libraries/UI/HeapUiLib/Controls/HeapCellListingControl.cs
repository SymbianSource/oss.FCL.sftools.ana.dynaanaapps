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
using System.Collections;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Windows.Forms;
using System.Text;
using SymbianStructuresLib.Debug.Symbols;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;

namespace HeapUiLib.Controls
{
    public class HeapCellListingControl : System.Windows.Forms.UserControl
    {
        #region Component Designer generated code
        private XPTable.Models.Table iTable;
        private XPTable.Models.TableModel iTableModel;
        private XPTable.Models.ColumnModel iColumnModel;
        private XPTable.Models.TextColumn iCol_Type;
        private XPTable.Models.TextColumn iCol_Address;
        private XPTable.Models.TextColumn iCol_Length;
        private XPTable.Models.TextColumn iCol_Symbol;
        private XPTable.Models.TextColumn iCol_Index;
        private XPTable.Models.TextColumn iCol_DescriptorLength;
        private System.ComponentModel.Container components = null;
        #endregion

        #region Constructors & destructor
        public HeapCellListingControl()
        {
            InitializeComponent();
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

        #region Component Designer generated code
        private void InitializeComponent()
        {
            XPTable.Models.DataSourceColumnBinder dataSourceColumnBinder1 = new XPTable.Models.DataSourceColumnBinder();
            this.iTable = new XPTable.Models.Table();
            this.iColumnModel = new XPTable.Models.ColumnModel();
            this.iCol_Index = new XPTable.Models.TextColumn();
            this.iCol_Type = new XPTable.Models.TextColumn();
            this.iCol_Address = new XPTable.Models.TextColumn();
            this.iCol_Length = new XPTable.Models.TextColumn();
            this.iCol_DescriptorLength = new XPTable.Models.TextColumn();
            this.iCol_Symbol = new XPTable.Models.TextColumn();
            this.iTableModel = new XPTable.Models.TableModel();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).BeginInit();
            this.SuspendLayout();
            // 
            // iTable
            // 
            this.iTable.AlternatingRowColor = System.Drawing.Color.Gainsboro;
            this.iTable.BorderColor = System.Drawing.Color.Black;
            this.iTable.ColumnModel = this.iColumnModel;
            this.iTable.DataMember = null;
            this.iTable.DataSourceColumnBinder = dataSourceColumnBinder1;
            this.iTable.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTable.EditStartAction = XPTable.Editors.EditStartAction.CustomKey;
            this.iTable.EnableToolTips = true;
            this.iTable.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.FullRowSelect = true;
            this.iTable.HeaderFont = new System.Drawing.Font( "Tahoma", 6.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.Location = new System.Drawing.Point( 0, 0 );
            this.iTable.MultiSelect = true;
            this.iTable.Name = "iTable";
            this.iTable.NoItemsText = "No Data";
            this.iTable.NoItemsTextColor = System.Drawing.SystemColors.ControlText;
            this.iTable.NoItemsTextFont = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iTable.Size = new System.Drawing.Size( 791, 336 );
            this.iTable.TabIndex = 0;
            this.iTable.TableModel = this.iTableModel;
            this.iTable.UnfocusedBorderColor = System.Drawing.Color.Black;
            this.iTable.PrepareForSort += new XPTable.Events.SortEventHandler( this.iTable_PrepareForSort );
            this.iTable.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iTable_KeyDown );
            this.iTable.DoubleClick += new System.EventHandler( this.iTable_DoubleClick );
            // 
            // iColumnModel
            // 
            this.iColumnModel.Columns.AddRange( new XPTable.Models.Column[] {
            this.iCol_Index,
            this.iCol_Type,
            this.iCol_Address,
            this.iCol_Length,
            this.iCol_DescriptorLength,
            this.iCol_Symbol} );
            // 
            // iCol_Index
            // 
            this.iCol_Index.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_Index.ContentWidth = 0;
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
            // iCol_DescriptorLength
            // 
            this.iCol_DescriptorLength.Alignment = XPTable.Models.ColumnAlignment.Right;
            this.iCol_DescriptorLength.ContentWidth = 53;
            this.iCol_DescriptorLength.Text = "Des. Len.";
            this.iCol_DescriptorLength.Width = 85;
            // 
            // iCol_Symbol
            // 
            this.iCol_Symbol.ContentWidth = 70;
            this.iCol_Symbol.Text = "Object Name";
            this.iCol_Symbol.Width = 350;
            // 
            // HeapCellListingControl
            // 
            this.Controls.Add( this.iTable );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Name = "HeapCellListingControl";
            this.Size = new System.Drawing.Size( 791, 336 );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).EndInit();
            this.ResumeLayout( false );

		}
        #endregion

        #region API
        public void SetFilter( TFilterType aType )
        {
            SetFilter( aType, null );
        }

        public void SetFilter( TFilterType aType, Symbol aSymbol )
        {
            iFilterType = aType;
            iFilterSymbol = aSymbol;
            //
            iCol_DescriptorLength.Visible = ( aType == TFilterType.EFilterShowCellsAllocatedDescriptor );
            //
            UpdateListView();
        }

        public void CopySelectedDataToClipboard()
		{
			StringBuilder clipboardText = new StringBuilder();
			
			// Get columns
			foreach( XPTable.Models.Column column in iTable.ColumnModel.Columns )
			{
				clipboardText.Append( column.Text + "\t" );
			}
			clipboardText.Append( System.Environment.NewLine );

			// Do underline (there must be a better way!)
			int len = clipboardText.Length;
			for( int i=0; i<len; i++ )
			{
				clipboardText.Append( "=" );
			}
			clipboardText.Append( System.Environment.NewLine );

			// Get cell values
			foreach( XPTable.Models.Row row in iTable.SelectedItems )
			{
				foreach( XPTable.Models.Cell cell in row.Cells )
				{
					string text = cell.Text;
					clipboardText.Append( text + "\t" );
				}
				//
				clipboardText.Append( System.Environment.NewLine );
			}
			//
			Clipboard.SetDataObject( clipboardText.ToString(), true );
		}
		#endregion

        #region Properties
        [Browsable( false )]
        public HeapCell SelectedCell
        {
            get
            {
                HeapCell cell = null;
                //
                if  ( iTable.SelectedItems.Length > 0 )
                {
                    int index = iTable.SelectedIndicies[ 0 ];
                    cell = iTable.TableModel.Rows[ index ].Tag as HeapCell;
                }
                //
                return cell;
            }
            set
            {
                foreach ( XPTable.Models.Row row in iTable.TableModel.Rows )
                {
                    HeapCell cell = (HeapCell) row.Tag;
                    //
                    if ( cell.Address == value.Address )
                    {
                        iTable.EnsureVisible( row.Index, 0 );
                        iTable.TableModel.Selections.Clear();
                        iTable.TableModel.Selections.AddCell( row.Index, 0 );
                        break;
                    }
                }
            }
        }

        [Browsable( false )]
        public HeapCellArray Cells
        {
            get { return iCells; }
            set
            {
                iCells = value;
                UpdateListView();
            }
        }
        #endregion

        #region Event handlers
        private void iTable_DoubleClick( object sender, System.EventArgs e )
        {
            OnDoubleClick( e );
        }

		private void iTable_KeyDown( object sender, System.Windows.Forms.KeyEventArgs e )
		{
			OnKeyDown( e );
		}

        private void iTable_PrepareForSort( object sender, XPTable.Events.SortEventArgs e )
        {
            if ( e.Column == iCol_Length )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else if ( e.Column == iCol_DescriptorLength )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else
            {
                e.Comparer = new XPTable.Sorting.TextComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
        }
        #endregion

        #region Internal methods
        private void UpdateListView()
        {
            iTable.BeginUpdate();
            iTable.TableModel.Rows.Clear();
            //
            int index = 0;
            foreach( HeapCell cell in Cells )
            {
                // Check whether the filter permits this item to be included.
                if ( CheckAgainstFilter( cell ) )
                {
                    XPTable.Models.Row row = new XPTable.Models.Row();
                    row.Tag = cell;
                    //
                    row.Cells.Add( new XPTable.Models.Cell( index.ToString( "d6" ) ) );
                    XPTable.Models.Cell cellType = new XPTable.Models.Cell( " " + cell.TypeString );
                    if ( cell.Type == HeapCell.TType.EAllocated )
                    {
                        if ( cell.IsDescriptor )
                        {
                            cellType.ForeColor = Color.DarkGoldenrod;
                            cellType.ToolTipText = "Descriptor";
                        }
                        else
                        {
                            cellType.ForeColor = Color.Red;
                        }
                    }
                    else if ( cell.Type == HeapCell.TType.EFree )
                    {
                        cellType.ForeColor = Color.Blue;
                    }
                    row.Cells.Add( cellType );
                    row.Cells.Add( new XPTable.Models.Cell( "0x" + cell.Address.ToString( "x8" ) ) );

                    // Must initialise "data" in order for sorting to work (XPTable's numeric sorter relies upon it).
                    XPTable.Models.Cell cellLength = new XPTable.Models.Cell( cell.Length.ToString() );
                    cellLength.Data = cell.Length;
                    row.Cells.Add( cellLength );

                    // If we are filtering to show only descriptor entries, then we also include an extra
                    // column containing the descriptor length.
                    if ( iFilterType == TFilterType.EFilterShowCellsAllocatedDescriptor )
                    {
                        if ( cell.IsDescriptor )
                        {
                            // Must initialise "data" in order for sorting to work (XPTable's numeric sorter relies upon it).
                            XPTable.Models.Cell cellDescriptorLength = new XPTable.Models.Cell( cell.DescriptorLength.ToString() );
                            cellDescriptorLength.Data = cell.DescriptorLength;
                            row.Cells.Add( cellDescriptorLength );
                        }
                        else
                        {
                            row.Cells.Add( new XPTable.Models.Cell( string.Empty ) );
                        }
                    }
                    else
                    {
                        row.Cells.Add( new XPTable.Models.Cell( string.Empty ) );
                    }

                    // Payload column
                    if ( cell.IsDescriptor )
                    {
                        row.Cells.Add( new XPTable.Models.Cell( cell.DescriptorText ) );
                    }
                    else if ( cell.Symbol != null )
                    {
                        row.Cells.Add( new XPTable.Models.Cell( cell.SymbolString ) );
                    }
                    else
                    {
                        row.Cells.Add( new XPTable.Models.Cell( cell.RawItems.FirstLine ) );
                    }
                    //
                    iTable.TableModel.Rows.Add( row );

                    ++index;
                }
            }
            
            // Must sort if the user had previously selected a sort column
            if ( iTable.IsValidColumn( iTable.SortingColumn ) )
            {
                iTable.Sort();
            }

            // Done - end transaction & redraw
            iTable.EndUpdate();
        }

        private bool CheckAgainstFilter( HeapCell aCell )
        {
            bool ret = false;
            //
            if ( iFilterType == TFilterType.EFilterShowAll )
            {
                ret = true;
            }
            else if ( iFilterType == TFilterType.EFilterShowCellsFree && aCell.Type == HeapCell.TType.EFree )
            {
                ret = true;
            }
            else if ( iFilterType == TFilterType.EFilterShowCellsAllocated && aCell.Type == HeapCell.TType.EAllocated )
            {
                ret = true;
            }
            else if ( iFilterType == TFilterType.EFilterShowCellsAllocatedDescriptor && aCell.Type == HeapCell.TType.EAllocated && aCell.IsDescriptor )
            {
                ret = true;
            }
            else if ( iFilterType == TFilterType.EFilterShowCellsAllocatedByType && aCell.Type == HeapCell.TType.EAllocated )
            {
                bool noSymbol = ( iFilterSymbol == null || iFilterSymbol.Address == 0 );
                bool descriptor = aCell.IsDescriptor;
                //
                if ( !descriptor )
                {
                    if ( noSymbol && aCell.Symbol == null )
                    {
                        ret = true;
                    }
                    else if ( iFilterSymbol != null && aCell.Symbol != null )
                    {
                        ret = ( aCell.Symbol.Address == iFilterSymbol.Address );
                    }
                }
            }
            //
            return ret;
        }
        #endregion

        #region Data members
        private TFilterType iFilterType = TFilterType.EFilterShowAll;
        private Symbol iFilterSymbol = null;
        private HeapCellArray iCells = new HeapCellArray();
        #endregion
    }
}
