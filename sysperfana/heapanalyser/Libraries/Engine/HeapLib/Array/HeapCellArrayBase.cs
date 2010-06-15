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

namespace HeapLib.Array
{
	public abstract class HeapCellArrayBase : IEnumerable<HeapCell>
	{
		#region Constructors & destructor
		public HeapCellArrayBase()
        {
		}
		#endregion

		#region API - abstract
        public abstract void Clear();

        public abstract void Add( HeapCell aCell );

        public abstract void Remove( HeapCell aCell );

        public abstract int CellIndex( HeapCell aCell );
        #endregion

        #region API
        public bool Contains( HeapCell aCell )
        {
            bool exists = CellByAddress( aCell.Address ) != null;
            return exists;
        }

        public HeapCell CellByAddress( uint aAddress )
		{
			int index = 0;
            HeapCell ret = CellByAddress( aAddress, out index );
            return ret;
		}

        public HeapCell CellByExactAddress( uint aAddress )
		{
            int index = 0;
            HeapCell ret = CellByExactAddress( aAddress, out index );
            return ret;
        }

        public virtual HeapCell CellByAddress( uint aAddress, out int aIndex )
        {
            HeapCell ret = null;
            aIndex = -1;
            //
            int i = 0;
            foreach( HeapCell cell in this )
            {
                HeapCell.TRegion region = cell.RegionForAddress( aAddress );
                //
                if	( region == HeapCell.TRegion.EHeader || region == HeapCell.TRegion.EPayload )
                {
                    aIndex = i;
                    ret = cell;
                    break;
                }
                else
                {
                    i++;
                }
            }
            //
            return ret;
        }

        public virtual HeapCell CellByExactAddress( uint aAddress, out int aIndex )
        {
            aIndex  = -1;
            HeapCell dummyCell = new HeapCell();
            dummyCell.Address = aAddress;
            //
            HeapCell ret = null;
            int index = CellIndex( dummyCell );
            //
            if	( index >= 0 && index < Count )
            {
                ret = this[ index ];
                aIndex = index;
            }
            //
            return ret;
        }

		public HeapCell CellByAllocationNumber( uint aNumber )
		{
            int index;
            return CellByAllocationNumber( aNumber, out index );
		}

        public HeapCell CellByAllocationNumber( uint aNumber, out int aIndex )
        {
            HeapCell ret = null;
            //
            aIndex = -1;
            int index = 0;
            foreach( HeapCell cell in this )
            {
                if	( cell.Type == HeapCell.TType.EAllocated && cell.AllocationNumber == aNumber )
                {
                    ret = cell;
                    aIndex = index;
                    break;
                }
                ++index;
            }
            //
            return ret;
        }

        public HeapCell CellByAllocationNumberIndexed( uint aNumber, int aDelta )
        {
            if  ( iAllocMap == null )
            {
                iAllocMap = new HeapCellArrayAllocationNumberMap( this );
            }
            //
            HeapCell cell = null;
            uint nextAllocNum = (uint) ( aNumber + aDelta );
            //
            while( cell == null && iAllocMap.InRange( nextAllocNum ) )
            {
                cell = iAllocMap[ nextAllocNum ];
                nextAllocNum = (uint) ( nextAllocNum + aDelta );
            }
            //
            return cell;
        }

        public bool AllocationNumberInRange( uint aNumber )
        {
            if  ( iAllocMap == null )
            {
                iAllocMap = new HeapCellArrayAllocationNumberMap( this );
            }
            //
            return iAllocMap.InRange( aNumber );
        }

        public IEnumerator<HeapCell> CreateEnumerator()
        {
            return new HeapCellArrayEnumerator( this );
        }

        public void Copy( HeapCellArray aArray )
        {
            foreach ( HeapCell cell in aArray )
            {
                Add( cell );
            }
        }
        #endregion

		#region Properties - abstract
        public abstract int Count
        {
            get;
        }

		public abstract HeapCell this[ int aIndex ]
		{
            get;
		}
		#endregion

        #region As CSV & Text
        public override string ToString()
        {
            StringBuilder ret = new StringBuilder();

            // Create entries
            int index = 0;
            foreach( HeapCell cell in this )
            {
                // INDEX
                ret.Append( "[" + index.ToString("d6") + "]  " );

                // TYPE
                ret.Append( cell.TypeString );
                if	( cell.Type == HeapCell.TType.EFree )
                {
                    ret.Append( "     " ); // to make the same length as "Allocated" we need 5 extra spaces
                }
                ret.Append( "  " );

                // ADDRESS
                ret.Append( "@ 0x" + cell.Address.ToString("x8") );
                ret.Append( "  " );

                // LENGTH
                ret.Append( "Len: " + cell.Length.ToString( "########" ) );
                ret.Append( ", " );

                // PAYLOAD LENGTH
                ret.Append( "PayL: " + cell.PayloadLength.ToString( "########" ) );
                ret.Append( ", " );

                // NESTING LEVEL
                ret.Append( "Nest: " + cell.NestingLevel.ToString( "########" ) );
                ret.Append( ", " );

                // ALLOCATION NUMBER
                ret.Append( "Allo#: " + cell.AllocationNumber.ToString( "########" ) );
                ret.Append( ", " );

                // VTABLE ADDRESS
                ret.Append( "VT: 0x" + cell.PossibleVTableAddress.ToString("x8") );
                ret.Append( ", " );

                // VTABLE SYMBOL
                ret.Append( "{ " + cell.SymbolString + " }" );
                ret.Append( System.Environment.NewLine );

                ++index;
            }
            //
            return ret.ToString();
        }

        public string ToCSV()
        {
            StringBuilder ret = new StringBuilder();
			
            // Create header
            ret.Append( "Index,Type,Address,Length,Payload Length,Payload Length (inc  linked cells),Recursive (linked) Payload Length,Nesting Level,Allocation Number,VTable Address,VTable Symbol" );
            ret.Append( System.Environment.NewLine );

            // Create entries
            int index = 0;
            foreach( HeapCell cell in this )
            {
                // INDEX
                ret.Append( index.ToString("d6") );
                ret.Append( "," );

                // TYPE
                ret.Append( cell.TypeString );
                ret.Append( "," );

                // ADDRESS
                ret.Append( "0x" + cell.Address.ToString("x8") );
                ret.Append( "," );

                // LENGTH
                ret.Append( cell.Length );
                ret.Append( "," );

                // PAYLOAD LENGTH
                ret.Append( cell.PayloadLength );
                ret.Append( "," );

                // PAYLOAD LENGTH INCLUDING LINKED CELLS
                ret.Append( cell.PayloadLengthIncludingLinkedCells );
                ret.Append( "," );

                // RECURSIVE LINKED PAYLOAD LENGTH
                ret.Append( cell.CombinedLinkedCellPayloadLengths );
                ret.Append( "," );

                // NESTING LEVEL
                ret.Append( cell.NestingLevel );
                ret.Append( "," );

                // ALLOCATION NUMBER
                ret.Append( cell.AllocationNumber );
                ret.Append( "," );

                // VTABLE ADDRESS
                ret.Append( "0x" + cell.PossibleVTableAddress.ToString("x8") );
                ret.Append( "," );

                // VTABLE SYMBOL
                ret.Append( cell.SymbolString );

                ret.Append( System.Environment.NewLine );

                ++index;
            }
            //
            return ret.ToString();
        }
        #endregion

		#region IEnumerable Members
        IEnumerator IEnumerable.GetEnumerator()
		{
			return new HeapCellArrayEnumerator( this );
		}

        IEnumerator<HeapCell> IEnumerable<HeapCell>.GetEnumerator()
        {
            return CreateEnumerator();
        }
        #endregion

		#region Data members
        private HeapCellArrayAllocationNumberMap iAllocMap = null;
		#endregion
	}
}
