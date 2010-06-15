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
using System.IO;
using System.Text;
using SymbianUtils;
using SymbianStructuresLib.Debug.Symbols;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapLib.Statistics;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapUiLib.Dialogs;
using HeapUiLib.SubForms;
using HeapUiLib.Controls;
using HeapUiLib.Misc;

namespace HeapUiLib.Controls
{
    public partial class HeapCellListingFilter : UserControl
    {
        #region Events & Delegates
        public delegate void FilterChangedHandler( TFilterType aFilter, Symbol aSymbolOrNull );
        public event FilterChangedHandler FilterChanged;
        #endregion

        #region Constructors & destructor
        public HeapCellListingFilter()
        {
            InitializeComponent();
        }
        #endregion

        #region Properties
        [Browsable(false)]
        public HeapStatistics Statistics
        {
            get { return iStatistics; }
            set
            {
                iStatistics = value;
                UpdateList();
            }
        }

        [Browsable( false )]
        public TFilterType FilterType
        {
            get
            {
                TrackingInfoCaptioner captioner = new TrackingInfoCaptioner( "All Items", TFilterType.EFilterShowAll );
                //
                if ( iCombo_FilterList.SelectedItem != null )
                {
                    captioner = (TrackingInfoCaptioner) iCombo_FilterList.SelectedItem;
                }
                //
                return captioner.FilterType;
            }
            set
            {
                if ( value != FilterType )
                {
                    foreach ( object obj in iCombo_FilterList.Items )
                    {
                        TrackingInfoCaptioner captioner = (TrackingInfoCaptioner) obj;
                        //
                        if ( captioner.FilterType == value )
                        {
                            iCombo_FilterList.SelectedItem = obj;
                            break;
                        }
                    }
                }
            }
        }
        #endregion

        #region Event handlers
        private void iCombo_FilterList_SelectedIndexChanged( object sender, EventArgs e )
        {
            TrackingInfoCaptioner captioner = (TrackingInfoCaptioner) iCombo_FilterList.SelectedItem;
            //
            if ( FilterChanged != null && !captioner.IsSymbolBasedAllocationEntry )
            {
                FilterChanged( captioner.FilterType, null );
            }
            else if ( FilterChanged != null )
            {
                FilterChanged( captioner.FilterType, captioner.Symbol );
            }
        }
        #endregion

        #region Internal methods
        private void UpdateList()
        {
            // Setup combo box filter
            iCombo_FilterList.BeginUpdate();
            iCombo_FilterList.Items.Clear();

            // Sort the items by count. This will keep us in sync with
            // our 'items' collection.
            Statistics.StatsAllocated.TrackerSymbols.SortByCount();

            // Add standard items
            iCombo_FilterList.Items.Add( new TrackingInfoCaptioner( "Show All", TFilterType.EFilterShowAll ) );
            iCombo_FilterList.Items.Add( new TrackingInfoCaptioner( String.Format( "[FR] [{0:d5}] [{1:d8}] Free Cells", Statistics.StatsFree.TypeCount, Statistics.StatsFree.TypeSize ), TFilterType.EFilterShowCellsFree ) );
            iCombo_FilterList.Items.Add( new TrackingInfoCaptioner( String.Format( "[AA] [{0:d5}] [{1:d8}] Allocations", Statistics.StatsAllocated.TypeCount, Statistics.StatsAllocated.TypeSize ), TFilterType.EFilterShowCellsAllocated ) );
            iCombo_FilterList.Items.Add( new TrackingInfoCaptioner( String.Format( "[AD] [{0:d5}] [{1:d8}] Descriptors", Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount, Statistics.StatsAllocated.TrackerDescriptors.AssociatedMemory ), TFilterType.EFilterShowCellsAllocatedDescriptor ) );
            foreach ( TrackingInfo info in Statistics.StatsAllocated.TrackerSymbols )
            {
                iCombo_FilterList.Items.Add( new TrackingInfoCaptioner( info ) );
            }

            // Make sure "show all" is selected
            iCombo_FilterList.SelectedIndex = 0;
            iCombo_FilterList.EndUpdate();
        }
        #endregion

        #region Data members
        private HeapStatistics iStatistics = new HeapStatistics();
        #endregion
    }
}
