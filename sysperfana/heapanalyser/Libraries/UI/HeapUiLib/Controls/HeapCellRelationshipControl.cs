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
using HeapLib.Relationships;
using HeapLib.Statistics;

namespace HeapUiLib.Controls
{
    public partial class HeapCellRelationshipControl : UserControl
    {
        #region Enumerations
        public enum TListMode
        {
            EListModeEmbeddedReferences = 0,
            EListModeReferencedBy
        }
        #endregion

        #region Constructors & destructor
        public HeapCellRelationshipControl()
        {
            InitializeComponent();
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
            iCellFilter.FilterType = aType;
            //
            UpdateListView();
        }

        public void CopySelectedDataToClipboard()
        {
            StringBuilder clipboardText = new StringBuilder();

            // Get columns
            foreach ( XPTable.Models.Column column in iTable.ColumnModel.Columns )
            {
                clipboardText.Append( column.Text + "\t" );
            }
            clipboardText.Append( System.Environment.NewLine );

            // Do underline (there must be a better way!)
            int len = clipboardText.Length;
            for ( int i = 0; i < len; i++ )
            {
                clipboardText.Append( "=" );
            }
            clipboardText.Append( System.Environment.NewLine );

            // Get cell values
            foreach ( XPTable.Models.Row row in iTable.SelectedItems )
            {
                foreach ( XPTable.Models.Cell cell in row.Cells )
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
        public HeapStatistics Statistics
        {
            get { return iCellFilter.Statistics; }
            set { iCellFilter.Statistics = value; }
        }

        [Browsable( false )]
        public HeapCell SelectedCell
        {
            get
            {
                HeapCell cell = null;
                //
                if ( iTable.SelectedItems.Length > 0 )
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
                        iTable.Select();
                        iTable.Focus();
                        iTable.TableModel.Selections.Clear();
                        iTable.TableModel.Selections.AddCell( row.Index, 0 );
                        iTable.EnsureVisible( row.Index, 0 );
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

        public TListMode ListMode
        {
            get
            {
                TListMode ret = TListMode.EListModeReferencedBy;
                //
                if ( iRB_EmbeddedReferencesTo.Checked )
                {
                    ret = TListMode.EListModeEmbeddedReferences;
                }
                //
                return ret;
            }
            set
            {
                if ( value == TListMode.EListModeEmbeddedReferences )
                {
                    iRB_EmbeddedReferencesTo.Checked = true;
                }
                else
                {
                    iRB_ReferencedBy.Checked = true;
                }
            }
        }
        #endregion

        #region Event handlers
        private void iTable_DoubleClick( object sender, EventArgs e )
        {
            OnDoubleClick( e );
        }

        private void iTable_PrepareForSort( object sender, XPTable.Events.SortEventArgs e )
        {
            if ( e.Column == iCol_Length )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else if ( e.Column == iCol_ReferencedBy )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else if ( e.Column == iCol_EmbeddedReferencesTo )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else
            {
                e.Comparer = new XPTable.Sorting.TextComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
        }

        private void iTable_SelectionChanged( object sender, XPTable.Events.SelectionEventArgs e )
        {
            if ( e.NewSelectedIndicies.Length > 0 )
            {
                int rowIndex = e.NewSelectedIndicies[ 0 ];
                XPTable.Models.Row row = e.TableModel.Rows[ rowIndex ];
                HeapCell cell = (HeapCell) row.Tag;
                UpdateReferenceInfoList( cell );
            }
        }

        private void iCellFilter_FilterChanged( TFilterType aFilter, Symbol aSymbolOrNull )
        {
            SetFilter( aFilter, aSymbolOrNull );
        }

        private void iRB_Type_CheckedChanged( object sender, EventArgs e )
        {
            if ( iTable.SelectedItems.Length > 0 )
            {
                HeapCell cell = (HeapCell) iTable.SelectedItems[ 0 ].Tag;
                UpdateReferenceInfoList( cell );
            }
        }
        #endregion

        #region Internal methods
        private void UpdateListView()
        {
            iTable.BeginUpdate();
            iTable.TableModel.Rows.Clear();
            //
            foreach ( HeapCell cell in Cells )
            {
                // Check whether the filter permits this item to be included.
                if ( CheckAgainstFilter( cell ) )
                {
                    RelationshipManager relManager = cell.RelationshipManager;

                    XPTable.Models.Row row = new XPTable.Models.Row();
                    row.Tag = cell;
                    //
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

                    // Reference by
                    XPTable.Models.Cell cellRefBy = new XPTable.Models.Cell( relManager.ReferencedBy.Count.ToString() );
                    cellRefBy.Data = relManager.ReferencedBy.Count;
                    row.Cells.Add( cellRefBy );

                    // Embedded references to
                    XPTable.Models.Cell cellEmbeddedRefsTo = new XPTable.Models.Cell( relManager.EmbeddedReferencesTo.Count.ToString() );
                    cellEmbeddedRefsTo.Data = relManager.EmbeddedReferencesTo.Count;
                    row.Cells.Add( cellEmbeddedRefsTo );

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

        private void UpdateReferenceInfoList( HeapCell aCell )
        {
            RelationshipManager relManager = aCell.RelationshipManager;
            //
            if ( aCell.Symbol != null )
            {
                iGP_AssociatedWith.Text = "[0x" + aCell.Address + "] " + aCell.SymbolString;
            }
            else
            {
                iGP_AssociatedWith.Text = "[0x" + aCell.Address + "]";
            }

            //
            if ( iRB_EmbeddedReferencesTo.Checked )
            {
                HeapCellArray array = new HeapCellArray();
                foreach ( RelationshipInfo info in relManager.EmbeddedReferencesTo )
                {
                    array.Add( info.ToCell );
                }
                //
                iAssociatedWith.Cells = array;
            }
            else
            {
                HeapCellArray array = new HeapCellArray();
                foreach ( HeapCell refCell in relManager.ReferencedBy )
                {
                    array.Add( refCell );
                }
                //
                iAssociatedWith.Cells = array;
            }
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
