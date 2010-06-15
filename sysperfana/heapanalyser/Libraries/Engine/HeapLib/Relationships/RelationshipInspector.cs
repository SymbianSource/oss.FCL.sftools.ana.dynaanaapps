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
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using SymbianUtils.RawItems;
using SymbianUtils.TextUtilities.Readers.Types.Array;

namespace HeapLib.Relationships
{
    internal class RelationshipInspector : AsyncEnumerableReader<HeapCell>
	{
		#region Constructors & destructor
		public RelationshipInspector( HeapCellArray aEntries, HeapStatistics aStats )
            : base( null )
		{
			iEntries = aEntries;
            iStats = aStats;
		}
		#endregion

		#region API
		public void InspectLater( HeapCell aCell )
		{
            iDictionary.Add( aCell );
		}

		public void Inspect()
		{
            iResultCache.Clear();
            base.Setup( iDictionary.Count, iDictionary.GetEnumerator() );
            base.AsyncRead();
		}
		#endregion

        #region From AsyncEnumerableReader
        protected override void HandleObject( HeapCell aObject, long aIndex, long aCount )
        {
            CheckForCrossReferences( aObject );
        }

        protected override void HandleReadCompleted()
        {
            iResultCache.Clear();
            iDictionary.Clear();
            base.HandleReadCompleted();
        }
        #endregion

        #region Internal methods
        private void CheckForCrossReferences( HeapCell aCell )
		{
            // If we're dealing with a free cell, then set up the free cell 
            // linked list.
            if ( aCell.Type == HeapCell.TType.EFree && aCell.HeaderRawItems.Count == 2 )
            {
                uint length = aCell.HeaderRawItems[ 0 ].Data;
                uint address = aCell.HeaderRawItems[ 1 ].Data;
                //
                LocateMatch( aCell, aCell.HeaderRawItems[ 1 ], 0 );
            }

            // For all other cells, identify other links
			foreach( RawItem rawItem in aCell )
			{
                uint headerOffset = HeapCell.CellHeaderSize( aCell.Type );
                LocateMatch( aCell, rawItem, headerOffset );
			}
        }

        private void LocateMatch( HeapCell aCell, RawItem aItem, uint aHeaderOffset )
        {
            RelationshipManager linkManager = aCell.RelationshipManager;

            // Now check if this address exactly matches a heap cell
            if ( iStats.WithinHeapBounds( aItem.Data ) )
            {
                // The address of another object within the heap
                // is always the address of the object within the payload
                // section of a heap cell. Therefore in order to find the
                // corresponding heap cell address, we must subtract the
                // heap cell header length from the starting address.
                //
                // In other words, the rawItem.Data value points to a payload address.
                // However, our cell search function (CellByExactAddress) works in 
                // terms of a heap cell address. The heap cell includes [header + payload].
                //
                // In order to convert the object address into a heap cell address,
                // we must subtract the allocated heap cell header size from the 
                // raw address.
                uint exactMatchAddress = aItem.Data - aHeaderOffset;

                HeapCell cell = iEntries.CellByExactAddress( exactMatchAddress );
                if ( cell != null && cell.Address != aCell.Address )
                {
                    linkManager.AddEmbeddedReferenceTo( aItem, cell );
                }
                else if ( cell == null )
                {
                    uint partialMatchAddress = aItem.Data;

                    // Didn't find an exact match, but we might find that this address
                    // points to part way through another heap cell.
                    cell = Lookup( partialMatchAddress );
                    if ( cell != null && cell.Address != aCell.Address )
                    {
                        // It also has to point to the payload
                        HeapCell.TRegion region = cell.RegionForAddress( partialMatchAddress );
                        if ( region == HeapCell.TRegion.EPayload )
                        {
                            linkManager.AddEmbeddedReferenceTo( aItem, cell );
                        }
                    }
                }
            }
        }

        private HeapCell Lookup( uint aAddress )
        {
            HeapCell ret = null;
            //
            if ( !iResultCache.TryGetValue( aAddress, out ret ) )
            {
                ret = iEntries.CellByAddress( aAddress );
                if ( ret != null )
                {
                    iResultCache.Add( aAddress, ret );
                }
            }
            //
            return ret;
        }
		#endregion

        #region Data members
        private readonly HeapCellArray iEntries;
        private readonly HeapStatistics iStats;
        private RelationshipDictionary iDictionary = new RelationshipDictionary();
        private Dictionary<uint, HeapCell> iResultCache = new Dictionary<uint, HeapCell>();
		#endregion
    }
}
