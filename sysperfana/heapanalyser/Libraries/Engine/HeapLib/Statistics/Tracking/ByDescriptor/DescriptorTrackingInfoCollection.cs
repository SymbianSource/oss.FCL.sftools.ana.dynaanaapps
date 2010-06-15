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

namespace HeapLib.Statistics.Tracking.ByDescriptor
{
    public class DescriptorTrackingInfoCollection : IEnumerable<HeapCell>
    {
        #region Constructors & destructor
        public DescriptorTrackingInfoCollection()
        {
        }
        #endregion

        #region API
        public float DescriptorLengthAsTypePercentage( HeapCell aCell )
        {
            System.Diagnostics.Debug.Assert( aCell.Type == HeapCell.TType.EAllocated );
            System.Diagnostics.Debug.Assert( aCell.IsDescriptor );
            //
            float ret = (float) aCell.DescriptorLength / (float) AssociatedMemory;
            return ret * 100.0f;
        }
        #endregion

        #region Properties
        public int AssociatedCellCount
        {
            get { return iAssociatedCells.Count; }
        }

        public int Count
        {
            get { return AssociatedCellCount; }
        }

        public long AssociatedMemory
        {
            get { return iAssociatedMemory; }
        }

        public HeapCell this[ int aIndex ]
        {
            get { return iAssociatedCells[ aIndex ]; }
        }
        #endregion

        #region From TrackingCollectionBase
        internal void Track( HeapCell aCell )
        {
            if ( aCell.Type == HeapCell.TType.EAllocated && aCell.IsDescriptor )
            {
                iAssociatedCells.Add( aCell );
                iAssociatedMemory += aCell.Length;
            }
        }
        #endregion

        #region IEnumerable Members
        public IEnumerator<HeapCell> GetEnumerator()
        {
            return iAssociatedCells.CreateEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return iAssociatedCells.CreateEnumerator();
        }
        #endregion

        #region Data members
        private long iAssociatedMemory = 0;
        private HeapCellArray iAssociatedCells = new HeapCellArray();
        #endregion
    }
}
