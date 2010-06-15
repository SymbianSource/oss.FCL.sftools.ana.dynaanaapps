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
using HeapCtrlLib.Utilities;
using HeapLib.Statistics.Tracking.Base;

namespace HeapCtrlLib.Controls
{
    internal partial class HeapFilteringControl : UserControl
    {
        #region Constructors & destructor
        public HeapFilteringControl()
        {
            InitializeComponent();
        }
        #endregion

        #region Properties
        internal HeapCellFilterTripletDictionary Dictionary
        {
            get
            {
                HeapCellFilterTripletDictionary ret = new HeapCellFilterTripletDictionary();
                //
                foreach ( XPTable.Models.Row row in iTableModel.Rows )
                {
                    TripletDictionaryEntry entry = (TripletDictionaryEntry) row.Tag;
                    //
                    //XPTable.Models.Cell cellEnabled = row.Cells[ 0 ];
                    XPTable.Models.Cell cellColor = row.Cells[ 1 ];
                    //
                    ret.Add( entry.iKey, entry.iTriplet.Entity, (Color) cellColor.Data, entry.iTriplet.TrackingInfo );
                }

                return ret;
            }
            set
            {
                iDictionary = value;
                UpdateFilters();
            }
        }
        #endregion

        #region Event handlers
        private void iTable_CellCheckChanged( object sender, XPTable.Events.CellCheckBoxEventArgs e )
        {
            XPTable.Models.Row row = iTableModel.Rows[ e.Row ];
            XPTable.Models.Cell cellEnabled = e.Cell;
            XPTable.Models.Cell cellColor = row.Cells[ 1 ];
            TripletDictionaryEntry entry = (TripletDictionaryEntry) row.Tag;
            //
            if ( e.Column == 0 ) // iCol_Enabled
            {
                entry.iTriplet.Enabled = ( cellEnabled.Checked );
                //
                if ( cellEnabled.Checked )
                {
                    row.ForeColor = iTable.ForeColor;
                    cellColor.Data = entry.iTriplet.Color;
                }
                else
                {
                    row.ForeColor = Color.DarkGray;
                    cellColor.Data = HeapCellFilterTriplet.KDisabledColour;
                }
            }
            else if ( e.Column == 1 ) // iCol_Colour
            {
                if ( cellEnabled.Checked )
                {
                    entry.iTriplet.Color = (Color) cellColor.Data;
                }
            }
        }
        #endregion

        #region Internal methods
        private void UpdateFilters()
        {
            iTable.BeginUpdate();
            iTable.TableModel.Rows.Clear();
            //
            foreach ( KeyValuePair<string, HeapCellFilterTriplet> entry in iDictionary )
            {
                XPTable.Models.Row row = new XPTable.Models.Row();
                row.Tag = new TripletDictionaryEntry( entry.Key, entry.Value );
                //
                XPTable.Models.Cell cellEnabled = new XPTable.Models.Cell( string.Empty, entry.Value.Enabled );
                XPTable.Models.Cell cellColour = new XPTable.Models.Cell( string.Empty, entry.Value.Color );
                XPTable.Models.Cell cellText = new XPTable.Models.Cell( entry.Value.Entity );
                //
                row.Cells.Add( cellEnabled );
                row.Cells.Add( cellColour );
                row.Cells.Add( cellText );
                //
                if ( cellEnabled.Checked )
                {
                    row.ForeColor = iTable.ForeColor;
                    cellColour.Data = entry.Value.Color;
                }
                else
                {
                    row.ForeColor = Color.DarkGray;
                    cellColour.Data = HeapCellFilterTriplet.KDisabledColour;
                }
                //
                if ( !entry.Value.TrackingInfo.IsUnknownSymbolMatchItem )
                {
                    iTableModel.Rows.Add( row );
                }
            }
            //
            iTable.EndUpdate();
        }
        #endregion

        #region Data members
        private HeapCellFilterTripletDictionary iDictionary = new HeapCellFilterTripletDictionary();
        #endregion
    }

    #region Internal class
    internal class TripletDictionaryEntry
    {
        public TripletDictionaryEntry( string aKey, HeapCellFilterTriplet aTriplet )
        {
            iKey = aKey;
            iTriplet = aTriplet;
        }

        public readonly string iKey;
        public readonly HeapCellFilterTriplet iTriplet;
    }
    #endregion
}
