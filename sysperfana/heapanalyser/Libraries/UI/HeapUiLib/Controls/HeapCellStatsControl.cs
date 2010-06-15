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
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapLib.Statistics;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapLib.Reconstructor.DataSources;

namespace HeapUiLib.Controls
{
    public partial class HeapCellStatsControl : UserControl
    {
        #region Constructors
        public HeapCellStatsControl()
        {
            InitializeComponent();
        }
        #endregion

        #region API
        #endregion

        #region Properties
        [Browsable(false)]
        [DefaultValue(null)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        public HeapReconstructor Reconstructor
        {
            get { return iReconstructor; }
            set
            {
                iReconstructor = value;
                //
                if ( iReconstructor != null )
                {
                    UpdateMainTable();
                    UpdateSummary();
                }
            }
        }

        private HeapStatistics Statistics
        {
            get
            {
                HeapStatistics ret = null;
                //
                if ( Reconstructor != null )
                {
                    ret = Reconstructor.Statistics;
                }
                //
                return ret;
            }
        }
        #endregion

        #region Event handlers
        private void iPG3_StatsTable_SelectionChanged( object sender, XPTable.Events.SelectionEventArgs e )
        {
            if ( e.NewSelectedIndicies.Length > 0 )
            {
                UpdateSelectionTotals();
            }
        }

        private void iPG3_StatsTable_PrepareForSort( object sender, XPTable.Events.SortEventArgs e )
        {
            TrackingComparisonBase type = null;
            switch ( e.Index )
            {
            default:
            case 0:
                type = new HeapLib.Statistics.Tracking.Base.TrackingInfoSortBySymbolName( false );
                break;
            case 1:
                type = new HeapLib.Statistics.Tracking.Base.TrackingInfoSortByCount( false );
                break;
            case 2:
                type = new HeapLib.Statistics.Tracking.Base.TrackingInfoSortByPayloadLength( false );
                break;
            case 3:
                type = new HeapLib.Statistics.Tracking.Base.TrackingInfoSortByAssociatedMemory( false );
                break;
            }
            //
            if ( type == null )
            {
                throw new Exception( "No comparer for specified column" );
            }
            //
            e.Comparer = new TrackingInfoComparerWrapper( type );
        }
        #endregion

        #region Internal classes
        private class TrackingInfoComparerWrapper : IComparer
        {
            #region Constructors
            public TrackingInfoComparerWrapper( TrackingComparisonBase aBase )
            {
                iBaseComparer = aBase;
            }
            #endregion

            #region IComparer Members
            public int Compare( object aLeft, object aRight )
            {
                TrackingInfo left = null;
                TrackingInfo right = null;
                //
                if ( ( aLeft is TrackingInfo ) && ( aRight is TrackingInfo ) )
                {
                    left = (TrackingInfo) aLeft;
                    right = (TrackingInfo) aRight;
                }
                else if ( aLeft is XPTable.Models.Cell && aRight is XPTable.Models.Cell )
                {
                    XPTable.Models.Cell cellLeft = ( aLeft as XPTable.Models.Cell );
                    XPTable.Models.Cell cellRight = ( aRight as XPTable.Models.Cell );
                    //
                    left = (TrackingInfo) cellLeft.Row.Tag;
                    right = (TrackingInfo) cellRight.Row.Tag;
                }

                // Now do the compare...
                int ret = iBaseComparer.Compare( left, right );
                return ret;
            }
            #endregion

            #region Data members
            private readonly TrackingComparisonBase iBaseComparer;
            #endregion
        }
        #endregion

        #region Internal methods
        private void UpdateMainTable()
        {
            iTable_SymbolMemory.BeginUpdate();
            iTable_SymbolMemory.TableModel.Rows.Clear();

            iReconstructor.Statistics.StatsAllocated.TrackerSymbols.SortByAllocatedMemory();
            //
            foreach ( TrackingInfo item in iReconstructor.Statistics.StatsAllocated.TrackerSymbols )
            {
                XPTable.Models.Row row = new XPTable.Models.Row();
                row.Tag = item;

                // SYMBOL
                System.Diagnostics.Debug.Assert( item.Symbol != null );
                row.Cells.Add( new XPTable.Models.Cell( item.Symbol.NameWithoutVTablePrefix ) );

                // ALLOC COUNT
                row.Cells.Add( new XPTable.Models.Cell( item.Count.ToString() ) );

                // MEMORY-PER-INSTANCE
                row.Cells.Add( new XPTable.Models.Cell( item.PayloadLength.ToString() ) );

                // TOTAL ALLOCATED MEMORY
                row.Cells.Add( new XPTable.Models.Cell( item.AssociatedMemory.ToString() ) );

                // Add row
                iTable_SymbolMemory.TableModel.Rows.Add( row );
            }

            iTable_SymbolMemory.EndUpdate();
        }

        private void UpdateSummary()
        {
            long heapCellHeaderOverheadSize = Statistics.SizeTotalHeader;
            long symbolicMatchMemorySize = Statistics.StatsAllocated.TrackerSymbols.TypeSize;
            long otherMemorySize = ( Statistics.StatsAllocated.TypeSize - Statistics.StatsAllocated.TrackerSymbols.TypeSize );
            long freeSpaceSize = Statistics.StatsFree.TypeSize;
            long totalCellCount = Statistics.StatsAllocated.TypeCount + Statistics.StatsFree.TypeCount;

            // 1ST GROUP
            iLV_Summary.Items[ 0 ].SubItems[ 1 ].Text = Statistics.StatsAllocated.TypeCount.ToString();
            iLV_Summary.Items[ 0 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( Statistics.StatsAllocated.TypeCount, totalCellCount ) + " %";
            iLV_Summary.Items[ 1 ].SubItems[ 1 ].Text = Statistics.StatsFree.TypeCount.ToString();
            iLV_Summary.Items[ 1 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( Statistics.StatsFree.TypeCount, totalCellCount ) + " %";
            
            // 2ND GROUP
            iLV_Summary.Items[ 2 ].SubItems[ 1 ].Text = symbolicMatchMemorySize.ToString();
            iLV_Summary.Items[ 2 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( symbolicMatchMemorySize, symbolicMatchMemorySize + otherMemorySize + freeSpaceSize ) + " %";
            iLV_Summary.Items[ 3 ].SubItems[ 1 ].Text = otherMemorySize.ToString();
            iLV_Summary.Items[ 3 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( otherMemorySize, symbolicMatchMemorySize + otherMemorySize + freeSpaceSize ) + " %";
            // (4) is a spacer
            iLV_Summary.Items[ 5 ].SubItems[ 1 ].Text = ( symbolicMatchMemorySize + otherMemorySize ).ToString();
            iLV_Summary.Items[ 5 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( symbolicMatchMemorySize + otherMemorySize, symbolicMatchMemorySize + otherMemorySize + freeSpaceSize ) + " %";
            iLV_Summary.Items[ 6 ].SubItems[ 1 ].Text = freeSpaceSize.ToString();
            iLV_Summary.Items[ 6 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( freeSpaceSize, symbolicMatchMemorySize + otherMemorySize + freeSpaceSize ) + " %";
            // (7) is a spacer
            iLV_Summary.Items[ 8 ].SubItems[ 1 ].Text = heapCellHeaderOverheadSize.ToString();
            iLV_Summary.Items[ 8 ].SubItems[ 2 ].Text = NumberFormattingUtils.NumberAsPercentageTwoDP( heapCellHeaderOverheadSize, symbolicMatchMemorySize + otherMemorySize + freeSpaceSize ) + " %";
            
            // 3RD GROUP
            iLV_Summary.Items[ 10 ].SubItems[ 1 ].Text = ( heapCellHeaderOverheadSize + symbolicMatchMemorySize + otherMemorySize ).ToString();
            iLV_Summary.Items[ 10 ].SubItems[ 2 ].Text = "100.00 %";
        }

        private void UpdateSelectionTotals()
        {
            bool atLeastOneValue = false;
            long total = 0;

            foreach ( XPTable.Models.Row row in iTable_SymbolMemory.SelectedItems )
            {
                TrackingInfo item = (TrackingInfo) row.Tag;
                //
                total += item.AssociatedMemory;
                atLeastOneValue = true;
            }

            string totalValueAsString = "[nothing selected]";
            string totalAsPercentageString = "";
            //
            if ( atLeastOneValue )
            {
                int allocCount;
                int freeCount;
                long freeSpaceSize;
                long allocatedUnknownSize;
                long allocatedSymbolMatchSize;
                long totalHeapAllocatedMemory = TotalAllocatedMemory( out allocCount, out freeCount, out freeSpaceSize, out allocatedUnknownSize, out allocatedSymbolMatchSize );
                //
                totalValueAsString = total.ToString();
                totalAsPercentageString = NumberFormattingUtils.NumberAsPercentageTwoDP( total, totalHeapAllocatedMemory ) + " %";
            }
            //
            iLV_Summary.Items[ 9 ].SubItems[ 1 ].Text = totalValueAsString;
            iLV_Summary.Items[ 9 ].SubItems[ 2 ].Text = totalAsPercentageString;
        }

        private long TotalAllocatedMemory( out int aAllocCount, out int aFreeCount, out long aFreeSpaceSize, out long aAllocatedUnknownSize, out long aAllocatedSymbolMatchSize )
        {
            aAllocCount = 0;
            aFreeCount = 0;
            aFreeSpaceSize = 0;
            aAllocatedUnknownSize = 0;
            aAllocatedSymbolMatchSize = 0;
            //
            HeapCellArray data = iReconstructor.Data;
            int count = data.Count;
            //
            for ( int i = 0; i < count; i++ )
            {
                HeapCell cell = data[ i ];
                //
                if ( cell.Type == HeapCell.TType.EAllocated )
                {
                    ++aAllocCount;
                    if ( cell.Symbol != null )
                        aAllocatedSymbolMatchSize += cell.Length;
                    else
                        aAllocatedUnknownSize += cell.Length;
                }
                else
                {
                    ++aFreeCount;
                    aFreeSpaceSize += cell.Length;
                }
            }

            return ( aAllocatedSymbolMatchSize + aAllocatedUnknownSize + aFreeSpaceSize );
        }
        #endregion

        #region Data members
        private HeapReconstructor iReconstructor = null;
        #endregion
    }
}
