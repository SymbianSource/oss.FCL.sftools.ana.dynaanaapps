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
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Windows.Forms;
using HeapLib.Cells;
using HeapLib.Relationships;

namespace HeapUiLib.Controls
{
    public partial class HeapCellViewerControl : UserControl
    {
        #region Events
        public delegate void OnCellLinkDoubleClicked( HeapCell aCell );
        public event OnCellLinkDoubleClicked CellLinkDoubleClicked;
        #endregion

        #region Constructors & destructor
        public HeapCellViewerControl()
        {
            InitializeComponent();
        }
        #endregion

        #region Properties
        [Browsable(false)]
        public HeapCell Cell
        {
            get { return iCell; }
            set
            {
                // This will asynchronously redraw the table
                iCell = value;
                DisplayRawDataItemIndex = 0;
            }
        }
        #endregion

        #region Event handlers
        private void iTimerRefresh_Tick( object sender, EventArgs e )
        {
            iTimerRefresh.Enabled = false;
            iTimerRefresh.Stop();
            //
            UpdateTable();
        }

        private void iTable_RawItems_CellDoubleClick( object sender, XPTable.Events.CellMouseEventArgs e )
        {
            if ( e.Cell.Tag != null && e.Cell.Tag is HeapCell )
            {
                HeapCell linkedCell = (HeapCell) e.Cell.Tag;
                //
                if ( CellLinkDoubleClicked != null )
                {
                    CellLinkDoubleClicked( linkedCell );
                }
            }
        }

        private void iButton_PageUp_Click( object sender, EventArgs e )
        {
            HeapCell selectedCell = Cell;
            if ( selectedCell != null )
            {
                DisplayRawDataItemIndex -= KNumberOfRowsPerPage;
            }
        }

        private void iButton_PageDown_Click( object sender, EventArgs e )
        {
            HeapCell selectedCell = Cell;
            if ( selectedCell != null )
            {
                DisplayRawDataItemIndex += KNumberOfRowsPerPage;
            }
        }
        #endregion

        #region Internal properties
        private int DisplayRawDataItemIndex
        {
            get { return iDisplayRawDataItemIndex; }
            set
            {
                iDisplayRawDataItemIndex = value;
                //
                SetPageUpButtonStatus();
                SetPageDownButtonStatus();
                //
                RefreshContentsByTimer();
            }
        }
        #endregion

        #region Internal methods
        private void UpdateTable()
        {
            HeapCell cell = Cell;
            //
            iTable_RawItems.BeginUpdate();
            iTableModel.Rows.Clear();
            //            
            if ( cell != null )
            {
                // Work out our raw item offset and how many raw items to create in this operation
                SymbianUtils.RawItems.RawItemCollection rawItems = cell.RawItems;
                int rawItemIndexStart = DisplayRawDataItemIndex;
                int rawItemIndexEnd = Math.Min( rawItems.Count, rawItemIndexStart + KNumberOfRowsPerPage ); 
                //
                for ( int rawItemIndex = rawItemIndexStart; rawItemIndex < rawItemIndexEnd; rawItemIndex++ )
                {
                    SymbianUtils.RawItems.RawItem rawEntry = rawItems[ rawItemIndex ];
                    XPTable.Models.Row row = new XPTable.Models.Row();

                    // Cell 1: address
                    XPTable.Models.Cell cellAddress = new XPTable.Models.Cell( rawEntry.Address.ToString( "x8" ) );

                    // Cell 2: raw data
                    XPTable.Models.Cell cellRawData = new XPTable.Models.Cell( rawEntry.OriginalData.ToString( "x8" ) );

                    // Cell 3: interpreted data
                    XPTable.Models.Cell cellInterpretedData = new XPTable.Models.Cell( rawEntry.Data.ToString( "x8" ) );

                    // Cell 4: characterised data
                    XPTable.Models.Cell cellCharacterisedData = new XPTable.Models.Cell( rawEntry.OriginalCharacterisedData );

                    // Update style of "interpreted data" if there is a link to another cell. 
                    RelationshipInfo relationshipDescriptor = ( rawEntry.Tag != null && rawEntry.Tag is RelationshipInfo ) ? (RelationshipInfo) rawEntry.Tag : null;
                    if ( relationshipDescriptor != null )
                    {
                        // The colour depends on whether it is a clean link or not. Clean means whether or not the
                        // link points to the start of the specified cell.
                        HeapCell linkedCell = relationshipDescriptor.ToCell;
                        //
                        if ( relationshipDescriptor.IsCleanLink )
                        {
                            cellInterpretedData.ForeColor = Color.Blue;
                            cellInterpretedData.ToolTipText = "Link to start of: " + linkedCell.SymbolString + " @ 0x" + linkedCell.Address.ToString( "x8" );
                        }
                        else
                        {
                            cellInterpretedData.ForeColor = Color.DarkBlue;
                            cellInterpretedData.ToolTipText = "Link within: " + linkedCell.SymbolString + " @ 0x" + linkedCell.Address.ToString( "x8" );
                        }

                        cellInterpretedData.Font = new Font( iTable_RawItems.Font, FontStyle.Underline );
                        cellInterpretedData.Tag = linkedCell;
                    }

                    // Finish construction
                    row.Cells.Add( cellAddress );
                    row.Cells.Add( cellRawData );
                    row.Cells.Add( cellInterpretedData );
                    row.Cells.Add( cellCharacterisedData );
                    iTableModel.Rows.Add( row );
                }
            }

            // Try to select first item
            if ( iTable_RawItems.TableModel.Rows.Count > 0 )
            {
                iTable_RawItems.EnsureVisible( 0, 0 );
                iTable_RawItems.TableModel.Selections.SelectCell( 0, 0 );
            }

            iTable_RawItems.EndUpdate();
        }

        private void RefreshContentsByTimer()
        {
            if ( iTimerRefresh.Enabled )
            {
                iTimerRefresh.Enabled = false;
                iTimerRefresh.Stop();
            }

            iTimerRefresh.Enabled = true;
            iTimerRefresh.Start();
        }

		private void SetPageUpButtonStatus()
		{
			bool enabled = false;
            HeapCell selectedCell = Cell;
            if ( selectedCell != null )
            {
                SymbianUtils.RawItems.RawItemCollection rawItems = selectedCell.RawItems;
                //
                int previousWindowStart = DisplayRawDataItemIndex - KNumberOfRowsPerPage;
                int previousWindowEnd = DisplayRawDataItemIndex;
                //
                enabled = ( previousWindowStart >= 0 );
            }
			iButton_PageUp.Enabled = enabled;
		}

		private void SetPageDownButtonStatus()
		{
			bool enabled = false;
            HeapCell selectedCell = Cell;
            if ( selectedCell != null )
            {
                SymbianUtils.RawItems.RawItemCollection rawItems = selectedCell.RawItems;
                //
                int nextWindowStart = DisplayRawDataItemIndex + KNumberOfRowsPerPage;
                int nextWindowEnd = Math.Min( rawItems.Count, nextWindowStart + KNumberOfRowsPerPage );
                //
                enabled = ( nextWindowStart < rawItems.Count );
            }
			iButton_PageDown.Enabled = enabled;
		}
        #endregion

        #region Internal constants
        private const int KNumberOfRowsPerPage = 128;
        #endregion

        #region Data members
        private HeapCell iCell = new HeapCell();
        private int iDisplayRawDataItemIndex = 0;
        #endregion
    }
}
