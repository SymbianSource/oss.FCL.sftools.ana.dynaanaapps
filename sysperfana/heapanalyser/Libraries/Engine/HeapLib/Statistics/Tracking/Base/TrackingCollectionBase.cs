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
using System.Collections.Generic;
using SymbianUtils.Collections;
using SymbianStructuresLib.Debug.Symbols;
using HeapLib.Cells;

namespace HeapLib.Statistics.Tracking.Base
{
	public abstract class TrackingCollectionBase : IEnumerable<TrackingInfo>
	{
        #region Constructors & destructor
	    protected TrackingCollectionBase()
		{
        }
        #endregion

        #region API
        internal abstract void Track( HeapCell aCell );

        public virtual void Remove( TrackingInfo aItem )
        {
            throw new NotSupportedException();
        }
        #endregion

        #region Sorting
        public void SortByAllocatedMemory()
        {
            TrackingInfoSortByAssociatedMemory comparer = new TrackingInfoSortByAssociatedMemory( false );
            Sort( comparer );
        }

        public void SortByCount()
        {
            TrackingInfoSortByCount comparer = new TrackingInfoSortByCount( false );
            Sort( comparer );
        }

        public void SortByAssociatedCellCount()
        {
            TrackingInfoSortByAssociatedCellCount comparer = new TrackingInfoSortByAssociatedCellCount( false );
            Sort( comparer );
        }

        public void SortBySymbolName()
        {
            TrackingInfoSortBySymbolName comparer = new TrackingInfoSortBySymbolName( false );
            Sort( comparer );
        }

        public void SortByPayloadLength()
        {
            TrackingInfoSortByPayloadLength comparer = new TrackingInfoSortByPayloadLength( false );
            Sort( comparer );
        }
        #endregion

        #region Properties
        public abstract int Count
        {
            get;
        }
 
        public long TypeSize
        {
            get { return iTypeSize; }
        }

        public long CellMatchCount
        {
            get { return iCellMatchCount; }
        }

        public TrackingInfo EntryWithSmallestCount
        {
            get
            {
                TrackingInfo ret = null;
                //
                foreach ( TrackingInfo entry in this )
                {
                    if ( ret == null || entry.Count < ret.Count )
                    {
                        ret = entry;
                    }
                }
                //
                return ret;
            }
        }

        public TrackingInfo EntryWithLargestCount
        {
            get
            {
                TrackingInfo ret = null;
                //
                foreach ( TrackingInfo entry in this )
                {
                    if ( ret == null || entry.Count > ret.Count )
                    {
                        ret = entry;
                    }
                }
                //
                return ret;
            }
        }

        public abstract TrackingInfo this[ int aIndex ]
        {
            get;
        }

        public virtual TrackingInfo this[ Symbol aSymbol ]
        {
            get
            {
                throw new NotSupportedException();
            }
        }
        #endregion

        #region Internal methods
        protected abstract void Sort( IComparer<TrackingInfo> aComparer );

        protected void HandleCell( HeapCell aCell )
        {
            // Keep track of the total size of cells that
            // actually have symbols associated with them
            if  ( aCell.Symbol != null )
            {
                iTypeSize += aCell.Length;

                // Every cell we see that matches goes towards our 
                // tracking count.
                ++iCellMatchCount;
            }
        }
        #endregion

        #region IEnumerable Members
        IEnumerator<TrackingInfo> IEnumerable<TrackingInfo>.GetEnumerator()
        {
            int count = this.Count;
            for ( int i = 0; i < count; i++ )
            {
                TrackingInfo entry = this[ i ];
                yield return entry;
            }
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            int count = this.Count;
            for ( int i = 0; i < count; i++ )
            {
                TrackingInfo entry = this[ i ];
                yield return entry;
            }
        }
        #endregion

        #region Data members
        private long iTypeSize = 0;
        private long iCellMatchCount = 0;
        #endregion
    }
}