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
using System.Text;
using SymbianUtils.RawItems;
using HeapLib.Statistics;

namespace HeapLib.Cells.Descriptors
{
    public abstract class DescriptorAlgorithmBase
    {
        #region Constructors & destructor
        protected DescriptorAlgorithmBase( HeapStatistics aStats )
        {
            iStats = aStats;
        }
        #endregion

        #region Framework API
        internal virtual bool IsDescriptor( HeapCell aCell, out DescriptorInfo aInfo )
        {
            aInfo = null;
            iCell = aCell;
            //
            bool couldBeDescriptor = ( aCell.Symbol == null && RawItems.Count > 1 );
            if ( couldBeDescriptor )
            {
                int payloadLength = PayloadLengthWhenCellIsDescriptor;
                int possibleDescriptorLength = CalculatedDescriptorLength;
                //
                couldBeDescriptor = ( possibleDescriptorLength <= payloadLength );
                //
                if ( couldBeDescriptor )
                {
                    // Need to make a descriptor info object just to get the type...
                    DescriptorInfo info = new DescriptorInfo( aCell );
                    couldBeDescriptor = ( info.Type != HeapCell.TDescriptorType.EUnknown );
                }
            }
            //
            return couldBeDescriptor;
        }
        #endregion

        #region Properties
        protected HeapCell Cell
        {
            get { return iCell; }
        }

        protected RawItemCollection RawItems
        {
            get { return Cell.RawItems; }
        }

        protected HeapStatistics Statistics
        {
            get { return iStats; }
        }

        protected int PayloadLengthWhenCellIsDescriptor
        {
            get
            {
                // Layout is:
                //
                // First DWORD:
                //
                //   < 4 bits> Type
                //   <28 bits> Length
                //
                // Second DWORD:
                //
                //   Max Length
                //
                // Reduce the size by 4 since we discard the descriptor length
                int payloadLength = (int) Cell.PayloadLength - 4;
                return payloadLength;
            }
        }

        protected int CalculatedDescriptorLength
        {
            get
            {
                uint vTable = Cell.PossibleVTableAddress;
                uint len = vTable & KDescriptorLengthMask;
                return (int) len;
            }
        }

        protected int CalculatedDescriptorMaxLength
        {
            get
            {
                System.Diagnostics.Debug.Assert( RawItems.Count >= 2 );
                RawItem maxLenRI = Cell.RawItems[ 1 ];
                int maxLength = (int) maxLenRI.Data;
                return maxLength;
            }
        }
        #endregion

        #region Shared constants
        protected const int KDescriptorRawItemOffsetAfterLengthAndType = 1;
        protected const int KDescriptorTextMaxLength = 256;
        protected const byte KDescriptorPaddingMagicCharacterValue = 0xa5;
        protected const uint KDescriptorLengthMask = 0x00FFFFFF;
        #endregion

        #region Data members
        private readonly HeapStatistics iStats;
        private HeapCell iCell = new HeapCell();
        #endregion
    }
}
