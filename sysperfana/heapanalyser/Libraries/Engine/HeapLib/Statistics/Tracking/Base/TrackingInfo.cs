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
using SymbianStructuresLib.Debug.Symbols;
using SymbianStructuresLib.Debug.Symbols.Constants;
using HeapLib.Cells;
using HeapLib.Array;

namespace HeapLib.Statistics.Tracking.Base
{
    public class TrackingInfo : IEnumerable<HeapCell>
    {
        #region Static 'Null Entry' item
        internal static TrackingInfo CreateNull()
        {
            Symbol defaultSym = Symbol.NewDefault();
            TrackingInfo self = new TrackingInfo( defaultSym );
            return self;
        }
        #endregion

        #region Constructors & destructor
        internal TrackingInfo( Symbol aSymbol )
        {
            iSymbol = aSymbol;
        }
        #endregion

        #region Methods
        internal void Associate( HeapCell aCell )
        {
            // Don't track cell associations for items
            // without symbols.
            if  ( aCell.Symbol != null )
            {
                iAssociatedCells.Add( aCell );
            }

            // Then we'll update the rest of the details
            // for all types.
            IncrementCount();
            iAssociatedMemory += aCell.Length;
        }

        internal void IncrementCount()
        {
            ++iCount;
        }
        #endregion

        #region Properties
        public int AssociatedCellCount
        {
            get { return iAssociatedCells.Count; }
        }

        internal long Key
        {
            get
            {
                long key = SymbolConstants.KNullEntryAddress;
                //
                if ( !IsUnknownSymbolMatchItem )
                {
                    key = iSymbol.Address;
                }
                //
                return key;
            }
        }

        public bool IsUnknownSymbolMatchItem
        {
            get { return iSymbol == null; }
        }

        public Symbol Symbol
        {
            get { return iSymbol; }
        }

        public int Count
        {
            get { return iCount; }
        }

        public long AssociatedMemory
        {
            get { return iAssociatedMemory; }
        }

        public long PayloadLength
        {
            get { return iPayloadLength; }
            set { iPayloadLength = value; }
        }

        public HeapCell this[ int aIndex ]
        {
            get { return iAssociatedCells[ aIndex ]; }
        }
        #endregion

        #region From System.Object
        public override string ToString()
        {
            string ret = iSymbol.ToString();
            return ret;
        }
        #endregion

        #region IEnumerable Members
        IEnumerator IEnumerable.GetEnumerator()
        {
            return iAssociatedCells.CreateEnumerator();
        }

        IEnumerator<HeapCell> IEnumerable<HeapCell>.GetEnumerator()
        {
            return iAssociatedCells.CreateEnumerator();
        }
        #endregion

        #region Data Members
        private Symbol iSymbol = null;
        private int iCount = 0;
        private long iAssociatedMemory = 0;
        private long iPayloadLength = 0;
        private HeapCellArray iAssociatedCells = new HeapCellArray( 10 );
        #endregion
    }
}
