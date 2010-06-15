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
using System.Text;
using System.Collections;
using System.Collections.Generic;
using HeapLib.Cells;
using SymbianUtils.Collections;

namespace HeapLib.Array
{
	public class HeapCellArray : HeapCellArrayBase
	{
		#region Constructors & destructor
		public HeapCellArray()
            : this( 100 )
		{
		}

		public HeapCellArray( int aGranularity )
		{
            iComparer = new HeapCellComparerByAddress();
            iSortedList = new SortedList<uint, HeapCell>();
            iFlatList = new List<HeapCell>( aGranularity );
		}

		public HeapCellArray( HeapCellArray aCopy )
            : this( aCopy.Count + 1 )
		{
			foreach( HeapCell cell in aCopy )
			{
				Add( cell );
			}
		}
		#endregion

		#region API
        public override void Clear()
        {
            iFlatList.Clear();
            iSortedList.Clear();
        }

        public override void Add( HeapCell aCell )
        {
            uint address = aCell.Address;
            //
            iSortedList.Add( address, aCell );
            //
            int insertPos = iFlatList.BinarySearch( aCell, iComparer );
            System.Diagnostics.Debug.Assert( insertPos < 0 );
            insertPos = ~insertPos;
            iFlatList.Insert( insertPos, aCell );
        }

        public override void Remove( HeapCell aCell )
        {
            uint address = aCell.Address;
            if ( iSortedList.ContainsKey( address ) )
            {
                HeapCell c = iSortedList[ address ];
                iSortedList.Remove( address );
                iFlatList.Remove( c );
            }
        }

        public override int CellIndex( HeapCell aCell )
		{
            int ret = -1;
            //
            uint address = aCell.Address;
            int pos = iFlatList.BinarySearch( aCell, iComparer );
            if ( pos >= 0 )
            {
                ret = pos;
            }
            //
			return ret;
		}

        public override HeapCell CellByAddress( uint aAddress, out int aIndex )
        {
            HeapCell ret = null;

            // If we're using an address-based comparer, we can optimise the lookup.
            // Otherwise, we have to fall back to the slow iterative base class algorithm.
            if ( iComparer is HeapCellComparerByAddress )
            {
                HeapCell temp = new HeapCell( aAddress, 0, HeapCell.TType.EAllocated );
                aIndex = iFlatList.BinarySearch( temp, iComparer );
                if ( aIndex < 0 )
                {
                    // There wasn't an exact match, so the binary search algorithm returns
                    // us the next largest entry - or in other words, the insertion point
                    // if we were about to insert a new cell into the list.
                    //
                    // Because we want to locate the cell that contains the specified
                    // address value, then most likely it's the prior cell.
                    aIndex = ~aIndex - 1;
                }
                if ( aIndex >= 0 && aIndex < iFlatList.Count )
                {
                    ret = iFlatList[ aIndex ];
                }
                else
                {
                    aIndex = -1;
                }
            }
            else
            {
                ret = base.CellByAddress( aAddress, out aIndex );
            }
            //
            return ret;
        }
      
        public override HeapCell CellByExactAddress( uint aAddress, out int aIndex )
        {
            aIndex = -1;
            HeapCell ret = null;
            iSortedList.TryGetValue( aAddress, out ret );
            if ( ret != null )
            {
                aIndex = CellIndex( ret );
            }
            return ret;
        }
        #endregion

		#region Properties
		public override int Count
		{
            get { return iFlatList.Count; }
		}

        public override HeapCell this[ int aIndex ]
		{
            get
            {
                HeapCell ret = iFlatList[ aIndex ];
                return ret;
            }
		}
		#endregion

        #region Sorting
        public void SortByAddress()
        {
            IComparer<HeapCell> comparer = new HeapCellComparerByAddress();
            Sort( comparer );
        }

        public void SortByType()
        {
            IComparer<HeapCell> comparer = new HeapCellComparerByType();
            Sort( comparer );
        }

        public void SortByLength()
        {
            IComparer<HeapCell> comparer = new HeapCellComparerByLength();
            Sort( comparer );
        }
        #endregion

        #region Internal methods
        private void Sort( IComparer<HeapCell> aComparer )
        {
            iComparer = aComparer;
            iFlatList.Sort( iComparer );
        }
        #endregion

		#region Data members
        private IComparer<HeapCell> iComparer = null;
        private List<HeapCell> iFlatList = new List<HeapCell>();
        private SortedList<uint, HeapCell> iSortedList = new SortedList<uint, HeapCell>();
		#endregion
	}
}
