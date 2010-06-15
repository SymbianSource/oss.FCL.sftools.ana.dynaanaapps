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
using HeapLib.Statistics.Distribution;
using HeapLib.Array;

namespace HeapLib.Statistics.Types
{
	public class HeapStatisticsBase
	{
		#region Constructors & destructor
		public HeapStatisticsBase( HeapCell.TType aType )
		{
            iType = aType;
		}
		#endregion

        #region Framework API
        internal virtual void Finalise( HeapCellArrayBase aArray )
        {
            // Some operations cannot run until after the relationship inspector has finished it's task.
            foreach ( HeapCell cell in aArray )
            {
                int newCellCountReferencedBy = cell.RelationshipManager.ReferencedBy.Count;
                if ( iCellNumberOfReferencesGreatest == null || newCellCountReferencedBy > iCellNumberOfReferencesGreatest.RelationshipManager.ReferencedBy.Count )
                {
                    iCellNumberOfReferencesGreatest = cell;
                }
                if ( iCellNumberOfReferencesLowest == null || newCellCountReferencedBy < iCellNumberOfReferencesLowest.RelationshipManager.ReferencedBy.Count )
                {
                    iCellNumberOfReferencesLowest = cell;
                }

                int newCellCountEmbeddedReferences = cell.RelationshipManager.EmbeddedReferencesTo.Count;
                if ( iCellNumberOfEmbeddedReferencesMost == null || newCellCountEmbeddedReferences > iCellNumberOfEmbeddedReferencesMost.RelationshipManager.EmbeddedReferencesTo.Count )
                {
                    iCellNumberOfEmbeddedReferencesMost = cell;
                }
                if ( iCellNumberOfEmbeddedReferencesLeast == null || newCellCountEmbeddedReferences < iCellNumberOfEmbeddedReferencesLeast.RelationshipManager.EmbeddedReferencesTo.Count )
                {
                    iCellNumberOfEmbeddedReferencesLeast = cell;
                }
            }
        }

        internal virtual void HandleCell( HeapCell aCell )
		{
            ++iTypeCount;
            //
            iTypeSizeHeader += aCell.HeaderSize;
            iTypeSizePayload += aCell.PayloadLength;
            //
            iDistribution.Register( aCell.Length );
            //
            if  ( iCellLargest == null || aCell.Length > iCellLargest.Length )
            {
                iCellLargest = aCell;
            }
            if  ( iCellSmallest == null || aCell.Length < iCellSmallest.Length )
            {
                iCellSmallest = aCell;
            }
            //
            if  ( iCellAllocationNumberLargest == null || aCell.AllocationNumber > iCellAllocationNumberLargest.AllocationNumber )
            {
                iCellAllocationNumberLargest = aCell;
            }
            if  ( iCellAllocationNumberSmallest == null || aCell.AllocationNumber > iCellAllocationNumberSmallest.AllocationNumber )
            {
                iCellAllocationNumberSmallest = aCell;
            }
        }
        #endregion

		#region API
        public float CellLengthAsTypePercentage( HeapCell aCell )
        {
            float ret = (float) aCell.Length / (float) TypeSize;
            return ret * 100.0f;
        }
        #endregion

		#region Properties
        public HeapCell.TType Type
        {
            get { return iType; }
        }

        public int TypeCount
        {
            get { return iTypeCount; }
        }

        public long TypeSize
        {
            get { return iTypeSizeHeader + iTypeSizePayload; }
        }

        public long TypeSizeHeader
        {
            get { return iTypeSizeHeader; }
        }

        public long TypeSizePayload
        {
            get { return iTypeSizePayload; }
        }

        public HeapCell CellAllocationNumberLargest
        {
            get { return iCellAllocationNumberLargest; }
        }

        public HeapCell CellAllocationNumberSmallest
        {
            get { return iCellAllocationNumberSmallest; }
        }

        public HeapCell CellLargest
        {
            get { return iCellLargest; }
        }

        public HeapCell CellSmallest
        {
            get { return iCellSmallest; }
        }

        public HeapCell CellNumberOfReferencesLowest
        {
            get { return iCellNumberOfReferencesLowest; }
        }

        public HeapCell CellNumberOfReferencesGreatest
        {
            get { return iCellNumberOfReferencesGreatest; }
        }

        public HeapCell CellNumberOfEmbeddedReferencesLeast
        {
            get { return iCellNumberOfEmbeddedReferencesLeast; }
        }

        public HeapCell CellNumberOfEmbeddedReferencesMost
        {
            get { return iCellNumberOfEmbeddedReferencesMost; }
        }

        public HeapCellSizeDistribution Distribution
		{
			get { return iDistribution; }
		}
        #endregion

		#region Data members
        private readonly HeapCell.TType iType;
        private int iTypeCount;
        private long iTypeSizeHeader;
        private long iTypeSizePayload;
        private HeapCell iCellAllocationNumberLargest = null;
        private HeapCell iCellAllocationNumberSmallest = null;
        private HeapCell iCellNumberOfReferencesLowest = null;
        private HeapCell iCellNumberOfReferencesGreatest = null;
        private HeapCell iCellNumberOfEmbeddedReferencesLeast = null;
        private HeapCell iCellNumberOfEmbeddedReferencesMost = null;
        private HeapCell iCellLargest = new HeapCell( 0, uint.MinValue, HeapCell.TType.EAllocated );
        private HeapCell iCellSmallest = new HeapCell( 0, uint.MaxValue, HeapCell.TType.EAllocated );
        private HeapCellSizeDistribution iDistribution = new HeapCellSizeDistribution();
        #endregion
	}
}
