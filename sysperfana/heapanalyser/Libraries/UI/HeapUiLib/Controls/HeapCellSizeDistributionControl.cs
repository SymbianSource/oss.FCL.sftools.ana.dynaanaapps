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
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using HeapLib.Statistics.Distribution;
using SymbianUtils;
using XPTable.Models;

namespace HeapUiLib.Controls
{
    public partial class HeapCellSizeDistributionControl : UserControl
    {
        #region Constructors & destructor
        public HeapCellSizeDistributionControl()
        {
            InitializeComponent();
        }
        #endregion

        #region API
        #endregion

        #region Properties
        [Browsable(false)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        public HeapStatistics Statistics
        {
            get { return iStats; }
            set
            {
                iStats = value;
                iRB_CheckedChanged( this, EventArgs.Empty );
            }
        }
        #endregion

        #region Internal properties
        private HeapCellSizeDistribution Distribution
        {
            get { return iDistribution; }
            set
            {
                iDistribution = value;
                UpdateTable();
            }
        }

        private long TotalHeapSize
        {
            get
            {
                long ret = 1;
                //
                if ( Statistics != null )
                {
                    ret = Statistics.SizeTotalHeader + Statistics.SizeTotalPayload;
                }
                //
                return ret; 
            }
        }
        #endregion

        #region Internal methods
        private void UpdateTable()
        {
            iTable.BeginUpdate();
            iTableModel.Rows.Clear();
            //
            long total = iDistribution.Total;
            foreach ( DictionaryEntry entry in iDistribution )
            {
                uint size = (uint) ( entry.Key );
                uint instanceCount = (uint) entry.Value;
                uint totalForSize = size * instanceCount;
                double percentageDistribution = ( (double) ( totalForSize ) / ( (double) total ) ) * 100.0;
                double percentageHeap = ( (double) ( totalForSize ) / ( (double) TotalHeapSize ) ) * 100.0;
                string percentageTextDistribution = NumberFormattingUtils.NumberAsPercentageTwoDP( totalForSize, total ) + " %";
                string percentageTextHeap = NumberFormattingUtils.NumberAsPercentageTwoDP( totalForSize, TotalHeapSize ) + " %";
                //
                XPTable.Models.Row row = new XPTable.Models.Row();
                //
                XPTable.Models.Cell cellSize = new XPTable.Models.Cell( size.ToString() + " bytes" );
                cellSize.Tag = size;
                XPTable.Models.Cell cellInstanceCount = new XPTable.Models.Cell( instanceCount.ToString() );
                cellInstanceCount.Data = instanceCount;
                XPTable.Models.Cell cellTotalForSize = new XPTable.Models.Cell( totalForSize.ToString() );
                cellTotalForSize.Data = totalForSize;
                XPTable.Models.Cell cellPercentage = new XPTable.Models.Cell( percentageTextDistribution );
                cellPercentage.Tag = percentageDistribution;
                XPTable.Models.Cell cellPercentageOfHeap = new XPTable.Models.Cell( percentageTextHeap );
                cellPercentageOfHeap.Tag = percentageHeap;
                //
                row.Cells.Add( cellSize );
                row.Cells.Add( cellInstanceCount );
                row.Cells.Add( cellTotalForSize );
                row.Cells.Add( cellPercentage );
                row.Cells.Add( cellPercentageOfHeap );
                //
                iTableModel.Rows.Add( row );
            }
            //
            iTable.EndUpdate();
        }
        #endregion

        #region Event handlers
        private void iTable_PrepareForSort( object sender, XPTable.Events.SortEventArgs e )
        {
            if ( e.Column == iCol_Count || e.Column == iCol_Total )
            {
                e.Comparer = new XPTable.Sorting.NumberComparer( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else if ( e.Column == iCol_Size )
            {
                e.Comparer = new HeapCellDistributionCustomComparerUint( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
            else
            {
                e.Comparer = new HeapCellDistributionCustomComparerDouble( iTable.TableModel, e.Index, e.Column.SortOrder );
            }
        }

        private void iRB_CheckedChanged( object sender, EventArgs e )
        {
            if ( iRB_Allocated.Checked )
            {
                Distribution = Statistics.StatsAllocated.Distribution;
            }
            else if ( iRB_Free.Checked )
            {
                Distribution = Statistics.StatsFree.Distribution;
            }
        }
        #endregion

        #region Data members
        private HeapStatistics iStats = null;
        private HeapCellSizeDistribution iDistribution = new HeapCellSizeDistribution();
        #endregion
    }

    #region Internal classes
    internal class HeapCellDistributionCustomComparerUint : XPTable.Sorting.ComparerBase
    {
        #region Constructors & destructor
        public HeapCellDistributionCustomComparerUint( TableModel aModel, int aColumn, SortOrder aSortOrder )
            : base( aModel, aColumn, aSortOrder )
        {
        }
        #endregion

        #region From ComparerBase
        protected override int CompareCells( XPTable.Models.Cell cell1, XPTable.Models.Cell cell2 )
        {
            // check for null cells
            if ( cell1 == null && cell2 == null )
            {
                return 0;
            }
            else if ( cell1 == null )
            {
                return -1;
            }
            else if ( cell2 == null )
            {
                return 1;
            }

            if ( cell1.Tag == null && cell2.Tag == null )
            {
                return 0;
            }
            else if ( cell1.Tag == null )
            {
                return -1;
            }
            else if ( cell2.Tag == null )
            {
                return 1;
            }

            uint cell1Val = (uint) cell1.Tag;
            uint cell2Val = (uint) cell2.Tag;

            return cell1Val.CompareTo( cell2Val );
        }
        #endregion
    }

    internal class HeapCellDistributionCustomComparerDouble : XPTable.Sorting.ComparerBase
    {
        #region Constructors & destructor
        public HeapCellDistributionCustomComparerDouble( TableModel aModel, int aColumn, SortOrder aSortOrder )
            : base( aModel, aColumn, aSortOrder )
        {
        }
        #endregion

        #region From ComparerBase
        protected override int CompareCells( XPTable.Models.Cell cell1, XPTable.Models.Cell cell2 )
        {
            // check for null cells
            if ( cell1 == null && cell2 == null )
            {
                return 0;
            }
            else if ( cell1 == null )
            {
                return -1;
            }
            else if ( cell2 == null )
            {
                return 1;
            }

            if ( cell1.Tag == null && cell2.Tag == null )
            {
                return 0;
            }
            else if ( cell1.Tag == null )
            {
                return -1;
            }
            else if ( cell2.Tag == null )
            {
                return 1;
            }

            double cell1Val = (double) cell1.Tag;
            double cell2Val = (double) cell2.Tag;

            return cell1Val.CompareTo( cell2Val );
        }
        #endregion
    }
    #endregion
}
