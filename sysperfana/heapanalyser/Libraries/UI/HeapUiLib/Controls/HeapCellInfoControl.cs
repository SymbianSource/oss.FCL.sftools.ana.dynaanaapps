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
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Forms;
using System.Text;
using SymbianUtils;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Reconstructor;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapLib.Reconstructor.DataSources;

namespace HeapUiLib.Controls
{
    public partial class HeapCellInfoControl : UserControl
    {
        #region Constructors & destructor
        public HeapCellInfoControl()
        {
            InitializeComponent();
        }
        #endregion

        #region Properties
        public bool ShowStackBasedFunctionAddresses
        {
            get { return iShowStackBasedFunctionAddresses; }
            set 
            { 
                iShowStackBasedFunctionAddresses = value; 
                if ( value )
                {
                    iLVInfoCellHeader.Items[ 3 ].Text = "Symbol #1";
                    iLVInfoCellHeader.Items[ 4 ].Text = "Symbol #2";
                }
                else
                {
                    iLVInfoCellHeader.Items[ 3 ].Text = "Nesting level";
                    iLVInfoCellHeader.Items[ 4 ].Text = "Allocation #";
                }
            }
        }

        public HeapCell Cell
        {
            set
            {
                if ( value != null )
                {
                    UpdateInfoHeaderListView( value );
                    UpdateInfoPayloadListView( value );
                }
            }
        }
        #endregion

        #region Internal methods
        private void UpdateInfoHeaderListView( HeapCell aCell )
        {
            // Address
            iLVInfoCellHeader.Items[ 0 ].SubItems[ 1 ].Text = "0x" + aCell.Address.ToString( "x8" );

            // Payload address
            iLVInfoCellHeader.Items[ 1 ].SubItems[ 1 ].Text = "0x" + aCell.StartOfPayloadAddress.ToString( "x8" ) + " -> 0x" + aCell.EndAddress.ToString( "x8" );

            // Length
            iLVInfoCellHeader.Items[ 2 ].SubItems[ 1 ].Text = aCell.Length.ToString();

            // Nesting level
            string nestingLevel = "N/A (Release EUSER)";
            if ( aCell.Type == HeapCell.TType.EFree )
            {
                nestingLevel = "N/A (Free Cell)";
            }
            else if ( ShowStackBasedFunctionAddresses )
            {
                nestingLevel = "Unknown";
                if ( aCell.Symbol2 != null )
                {
                    nestingLevel = aCell.Symbol2.Address.ToString( "x8" ) + " " + aCell.Symbol2.Name;
                }
            }
            else if ( HeapCell.IsDebugAllocator )
            {
                nestingLevel = aCell.NestingLevel.ToString();
            }
            iLVInfoCellHeader.Items[ 3 ].SubItems[ 1 ].Text = nestingLevel;

            // Allocation #
            string allocNumber = "N/A (Release EUSER)";
            if ( aCell.Type == HeapCell.TType.EFree )
            {
                allocNumber = "N/A (Free Cell)";
            }
            else if ( ShowStackBasedFunctionAddresses )
            {
                allocNumber = "Unknown";
                if ( aCell.Symbol3 != null )
                {
                    allocNumber = aCell.Symbol3.Address.ToString( "x8" ) + " " + aCell.Symbol3.Name;
                }
            }
            else if ( HeapCell.IsDebugAllocator )
            {
                allocNumber = aCell.AllocationNumber.ToString();
            }
            iLVInfoCellHeader.Items[ 4 ].SubItems[ 1 ].Text = allocNumber;
        }

        private void UpdateInfoPayloadListView( HeapCell aCell )
        {
            // Length
            iLVInfoCellPayload.Items[ 0 ].SubItems[ 1 ].Text = aCell.PayloadLength.ToString();

            // Symbol
            if ( aCell.Symbol != null )
            {
                iLVInfoCellPayload.Items[ 1 ].SubItems[ 1 ].Text = aCell.Symbol.NameWithoutVTablePrefix;
            }
            else if ( aCell.Type == HeapCell.TType.EFree )
            {
                iLVInfoCellPayload.Items[ 1 ].SubItems[ 1 ].Text = "N/A (Free Cell)";
            }
            else
            {
                iLVInfoCellPayload.Items[ 1 ].SubItems[ 1 ].Text = "???";
            }
        }
        #endregion

        #region Event handlers
        private void HeapCellInfoControl_SizeChanged( object sender, EventArgs e )
        {
            int col1Width = iLVInfoCellHeader.Columns[ 0 ].Width;
            int col2Width = iLVInfoCellHeader.Columns[ 1 ].Width;
            int width = iLVInfoCellHeader.Width;
            iLVInfoCellHeader.Columns[ 1 ].Width = width - col1Width;
        }

        private void iLVInfoCellHeader_KeyDown( object sender, KeyEventArgs e )
        {
            bool wasControl = ( e.Modifiers & Keys.Control ) == Keys.Control;
            if ( wasControl && ( e.KeyCode == Keys.C ) )
            {
                if ( iLVInfoCellHeader.SelectedItems != null && iLVInfoCellHeader.SelectedItems.Count > 0 )
                {
                    ListViewItem item = iLVInfoCellHeader.SelectedItems[ 0 ];
                    ListViewItem.ListViewSubItem infoItem = item.SubItems[ 1 ];
                    Clipboard.SetText( infoItem.Text.Trim() );
                }
            }
        }
        #endregion

        #region Data members
        private bool iShowStackBasedFunctionAddresses = false;
        #endregion
    }
}
