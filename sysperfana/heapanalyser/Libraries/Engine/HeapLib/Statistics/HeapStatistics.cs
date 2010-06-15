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
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics.Types;
using HeapLib.Statistics.Distribution;
using SymbianUtils.Range;

namespace HeapLib.Statistics
{
	public class HeapStatistics
	{
        #region Constructors & destructor
		public HeapStatistics()
		{
		}
        #endregion

        #region Cell handler
        internal void HandleCell( HeapCell aCell )
        {
            HandleCellCommon( aCell );
            //
            switch( aCell.Type )
            {
            case HeapCell.TType.EAllocated:
                StatsAllocated.HandleCell( aCell );
                break;
            case HeapCell.TType.EFree:
                StatsFree.HandleCell( aCell );
                break;
            default:
                break;
            }
        }

        internal void Finalise( HeapCellArrayBase aArray )
        {
            iStatsAllocated.Finalise( aArray );
            iStatsAllocated.Finalise( aArray );
        }
        #endregion

        #region API
        public void Clear()
        {
            iHeapAddressRange = new AddressRange();
            iStatsAllocated = new HeapStatisticsAllocated();
            iStatsFree = new HeapStatisticsFree();
            iDistributionCommon = new HeapCellSizeDistribution();
        }

        public bool WithinHeapBounds( uint aAddress )
        {
            bool ret = iHeapAddressRange.Contains( aAddress );
            return ret;
        }

        public float CellLengthAsHeapPercentage( HeapCell aCell )
        {
            float ret = CellLengthAsHeapPercentage( aCell.Length );
            return ret;
        }

        public float CellLengthAsHeapPercentage( long aLength )
        {
            float ret = (float) aLength / (float) SizeTotal;
            return ret * 100.0f;
        }

        public float CellLengthAsTypePercentage( HeapCell aCell )
        {
            float ret = 0.0f;
            //
            switch( aCell.Type )
            {
            case HeapCell.TType.EAllocated:
                ret = StatsAllocated.CellLengthAsTypePercentage( aCell );
                break;
            case HeapCell.TType.EFree:
                ret = StatsFree.CellLengthAsTypePercentage( aCell );
                break;
            }
            //
            return ret;
        }
        #endregion

        #region Properties
        public uint HeapBaseAddress
        {
            get { return (uint) iHeapAddressRange.Min; }
            set
            {
                iHeapAddressRange.UpdateMin( value );
            }
        }

        public uint HeapSize
        {
            get
            {
                uint val = HeapAddressEnd - HeapAddressStart;
                return val;
            }
            set
            {
                uint maxAddress = HeapBaseAddress + value;
                iHeapAddressRange.UpdateMax( maxAddress );
            }
        }

        public long SizeTotal
        {
            get { return StatsAllocated.TypeSize + StatsFree.TypeSize; }
        }

        public long SizeTotalHeader
        {
            get { return StatsAllocated.TypeSizeHeader + StatsFree.TypeSizeHeader; }
        }

        public long SizeTotalPayload
        {
            get { return StatsAllocated.TypeSizePayload + StatsFree.TypeSizePayload; }
        }

        public AddressRange AddressRange
        {
            get { return iHeapAddressRange; }
        }

        public uint HeapAddressStart
        {
            get { return (uint) iHeapAddressRange.Min; }
        }

        public uint HeapAddressEnd
        {
            get { return (uint) iHeapAddressRange.Max; }
        }

        public HeapStatisticsFree StatsFree
        {
            get { return iStatsFree; }
        }

        public HeapStatisticsAllocated StatsAllocated
        {
            get { return iStatsAllocated; }
        }

        public HeapCellSizeDistribution DistributionCommon
        {
            get { return iDistributionCommon; }
        }
        #endregion

        #region Internal methods
        private void HandleCellCommon( HeapCell aCell )
        {
            iDistributionCommon.Register( aCell.Length );
        }
        #endregion

        #region Data members
        private AddressRange iHeapAddressRange = new AddressRange();
        private HeapStatisticsFree iStatsFree = new HeapStatisticsFree();
        private HeapStatisticsAllocated iStatsAllocated = new HeapStatisticsAllocated();
        private HeapCellSizeDistribution iDistributionCommon = new HeapCellSizeDistribution();
        #endregion
	}
}
