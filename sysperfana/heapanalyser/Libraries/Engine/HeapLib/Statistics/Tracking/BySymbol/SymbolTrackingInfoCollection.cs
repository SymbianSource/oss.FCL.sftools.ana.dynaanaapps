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
using SymbianUtils.Collections;
using SymbianStructuresLib.Debug.Symbols;
using SymbianStructuresLib.Debug.Symbols.Constants;
using HeapLib.Cells;
using HeapLib.Statistics.Tracking.Base;

namespace HeapLib.Statistics.Tracking.BySymbol
{
	public class SymbolTrackingInfoCollection : TrackingCollectionBase
	{
        #region Constructors & destructor
        public SymbolTrackingInfoCollection()
            : this( new TrackingInfoSortByAssociatedMemory( false ) )
        {
        }

        public SymbolTrackingInfoCollection( IComparer<TrackingInfo> aComparer )
        {
            iList = new SymbianSortedListByValue<uint, TrackingInfo>( aComparer );
        }

        public SymbolTrackingInfoCollection( SymbolTrackingInfoCollection aCollection )
        {
            iList = new SymbianSortedListByValue<uint, TrackingInfo>( aCollection.iList );
        }
        #endregion

        #region From TrackingCollectionBase
        internal override void Track( HeapCell aCell )
        {
            if ( aCell.Symbol != null && aCell.Symbol.IsDefault )
            {
                System.Diagnostics.Debug.WriteLine( "Unknown cell: " + aCell.ToStringExtended() + ", size: " + aCell.Symbol.Size + ", " + aCell.Symbol.Object );
            }

            if  ( aCell.Type == HeapCell.TType.EAllocated && !aCell.IsUnknown )
            {
                System.Diagnostics.Debug.Assert( aCell.Symbol != null );

                base.HandleCell( aCell );

                uint address = (uint) aCell.Symbol.Address;
                TrackingInfo entry = iList[ address ];

                // If we found an entry, then associate this cell with it.
                // Otherwise, we'll need to make a new entry...
                if  ( entry == null )
                {
                    entry = new TrackingInfo( aCell.Symbol );
                    entry.PayloadLength = aCell.PayloadLength;

                    iList.Add( address, entry );
                }
                entry.Associate( aCell );
            }
        }

        public override void Remove( TrackingInfo aItem )
        {
            // Make a predicate to find any items that match the specified
            // address
            Predicate<TrackingInfo> matchPredicate = delegate( TrackingInfo aInfo )
            {
                return ( aInfo.Key == aItem.Key );
            };
            iList.Remove( matchPredicate );
        }

        public override int Count
        {
            get { return iList.Count; }
        }

        protected override void Sort( IComparer<TrackingInfo> aComparer )
        {
            iList.Sort( aComparer );
        }

        public override TrackingInfo this[ int aIndex ]
        {
            get { return iList[ aIndex ]; }
        }

        public override TrackingInfo this[ Symbol aSymbol ]
        {
            get
            {
                uint address = aSymbol != null ? aSymbol.Address : SymbolConstants.KNullEntryAddress;
                TrackingInfo ret = iList[ address ];
                return ret;
           }
        }
        #endregion

        #region API
        public void RemoveRange( int aStartIndex, int aCount )
        {
            iList.RemoveRange( aStartIndex, aCount );
        }
        #endregion

        #region Data members
        private SymbianSortedListByValue<uint, TrackingInfo> iList;
        #endregion
    }
}
