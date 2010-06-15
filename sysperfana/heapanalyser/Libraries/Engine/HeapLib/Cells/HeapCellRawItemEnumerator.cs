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
using SymbianUtils.RawItems;

namespace HeapLib.Cells
{
    internal class HeapCellRawItemEnumerator : IEnumerator<RawItem>
    {
        #region Constructors & destructor
        public HeapCellRawItemEnumerator( HeapCell aCell )
        {
            iCell = aCell;
        }
        #endregion

        #region IEnumerator Members
        void IEnumerator.Reset()
        {
            iCurrentIndex = -1;
        }

        object IEnumerator.Current
        {
            get
            {
                return iCell[ iCurrentIndex ];
            }
        }

        bool IEnumerator.MoveNext()
        {
            return ( ++iCurrentIndex < iCell.RawItems.Count );
        }
        #endregion

        #region From IEnumerator<RawItem>
        RawItem IEnumerator<RawItem>.Current
        {
            get { return iCell[ iCurrentIndex ]; }
        }
        #endregion

        #region From IDisposable
        public void Dispose()
        {
        }
        #endregion

        #region Data members
        private readonly HeapCell iCell;
        private int iCurrentIndex = -1;
        #endregion
    }
}
