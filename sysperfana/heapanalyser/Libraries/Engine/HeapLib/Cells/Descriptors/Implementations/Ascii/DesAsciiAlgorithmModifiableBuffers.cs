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
using HeapLib.Statistics;

namespace HeapLib.Cells.Descriptors.Implementations.Ascii
{
    internal class DesAsciiAlgorithmModifiableBuffers : DesAsciiAlgorithmBase
    {
        #region Constructors & destructor
        public DesAsciiAlgorithmModifiableBuffers( HeapStatistics aStats )
            : base( aStats )
        {
        }
        #endregion

        #region From DescriptorAlgorithmBase
        internal override bool IsDescriptor( HeapCell aCell, out DescriptorInfo aInfo )
        {
            // Base-class makes first pass.
            bool isDes = base.IsDescriptor( aCell, out aInfo );
            //
            if ( isDes )
            {
                DescriptorInfo info = new DescriptorInfo( aCell );
                HeapCell.TDescriptorType type = info.Type;

                // There needs to be at least two raw items, one for the descriptor type & length, and 
                // another for the max length.
                if ( type != HeapCell.TDescriptorType.EUnknown && RawItems.Count > 2 && type == HeapCell.TDescriptorType.EBuf )
                {
                    // Now we can check if it meet our requirements...
                    int length = base.CalculatedDescriptorLength;
                    int maxLength = base.CalculatedDescriptorMaxLength;
                    //
                    bool allWerePrintableCharacers = false;
                    int printCharCount = PrintableCharacterCount( length, DesAsciiAlgorithmBase.KDescriptorRawItemOffsetEBufC, out allWerePrintableCharacers );
                    //
                    if ( printCharCount == length && length <= maxLength )
                    {
                        aInfo = info;
                        aInfo.Text = ConvertToText( length, DesAsciiAlgorithmBase.KDescriptorRawItemOffsetEBufC );
                        isDes = true;
                    }
                }
                else if ( type == HeapCell.TDescriptorType.EBufCPtr )
                {
                    int x = 0;
                    x++;
                }
            }
            //
            return isDes;
        }
        #endregion
    }
}
