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

namespace HeapLib.Cells.Descriptors.Implementations.Ascii
{
    internal class DesAsciiAlgorithmBase : DescriptorAlgorithmBase
    {
        #region Constructors & destructor
        public DesAsciiAlgorithmBase( HeapStatistics aStats )
            : base( aStats )
        {
        }
        #endregion

        #region Internal constants
        protected const int KDescriptorRawItemOffsetEBufC = KDescriptorRawItemOffsetAfterLengthAndType + 1; // Allows for [Type+Length] + MaxLength
        #endregion

        #region Internal methods
        protected string ConvertToText( int aLengthInUnicodeChars )
        {
            return ConvertToText( aLengthInUnicodeChars, KDescriptorRawItemOffsetAfterLengthAndType );
        }

        protected string ConvertToText( int aLengthInAsciiChars, int aStartRawItemIndex )
        {
            StringBuilder text = new StringBuilder();
            for ( int i = aStartRawItemIndex; i < RawItems.Count && i < Math.Min( aLengthInAsciiChars, DescriptorAlgorithmBase.KDescriptorTextMaxLength ); i++ )
            {
                RawItem item = RawItems[ i ];
                //
                text.Append( item.OriginalCharacterisedData );
            }
            //
            string ret = text.ToString();
            if ( ret.Length > aLengthInAsciiChars )
            {
                ret = ret.Substring( 0, aLengthInAsciiChars );
            }
            return ret;
        }

        protected int PrintableCharacterCount( int aMaxNumberOfAsciiBytesToProcess, out bool aAllBytesWereAsciiCharacters )
        {
            return PrintableCharacterCount( aMaxNumberOfAsciiBytesToProcess, KDescriptorRawItemOffsetAfterLengthAndType, out aAllBytesWereAsciiCharacters );
        }

        protected int PrintableCharacterCount( int aMaxNumberOfAsciiBytesToProcess, int aStartRawItemIndex, out bool aAllBytesWereAsciiCharacters )
        {
            int printableCharCount = 0;
            int bytesRemaining = aMaxNumberOfAsciiBytesToProcess;

            for ( int i = aStartRawItemIndex; i < RawItems.Count && bytesRemaining > 0; i++ )
            {
                RawItem item = RawItems[ i ];
                byte[] data = item.DataArray;
                //
                for ( int j = 0; j < data.Length && bytesRemaining > 0; j++ )
                {
                    byte b = data[ j ];
                    bool byteIsPrint = IsPrintableAsciiCharacter( b );
                    if ( byteIsPrint )
                    {
                        ++printableCharCount;
                    }
                    --bytesRemaining;
                }
            }
            //
            aAllBytesWereAsciiCharacters = ( aMaxNumberOfAsciiBytesToProcess == printableCharCount );
            return printableCharCount;
        }

        protected static bool IsPrintableAsciiCharacter( byte aByte )
        {
            bool ret = false;
            //
            if ( aByte >= 32 /*SPACE*/ && aByte < 127 /*DEL*/ || aByte == 0x0D /*CR*/ || aByte == 0x0A /*LF*/ || aByte == 0x09 /*TAB*/ )
            {
                ret = true;
            }
            //
            return ret;
        }
        #endregion
    }
}
