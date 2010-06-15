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
    internal class RelationshipDictionary : IEnumerable<HeapCell>
    {
        #region Constructors & destructor
        public RelationshipDictionary()
        {
        }
        #endregion

        #region API
        public bool Contains( HeapCell aCell )
        {
            uint address = aCell.Address;
            bool ret = iDictionary.ContainsKey( address );
            return ret;
        }

        public void Add( HeapCell aCell )
        {
            bool added = Contains( aCell );
            if ( !added )
            {
                iDictionary.Add( aCell.Address, aCell );
            }
        }

        public void Clear()
        {
            iDictionary.Clear();
        }
        #endregion

        #region Properties
        public long Count
        {
            get { return iDictionary.Count; }
        }
        #endregion

        #region IEnumerable<HeapCell> Members
        public IEnumerator<HeapCell> GetEnumerator()
        {
            foreach ( KeyValuePair<uint, HeapCell> kvp in iDictionary )
            {
                yield return kvp.Value;
            }
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            IEnumerator<HeapCell> self = (IEnumerator<HeapCell>) this;
            return self;
        }
        #endregion

        #region Data members
        private SortedDictionary<uint, HeapCell> iDictionary = new SortedDictionary<uint, HeapCell>();
        #endregion
    }
}
