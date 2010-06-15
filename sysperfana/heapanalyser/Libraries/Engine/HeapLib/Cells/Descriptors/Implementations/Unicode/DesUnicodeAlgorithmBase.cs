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

namespace HeapLib.Cells.Descriptors.Implementations.Unicode
{
    internal class DesUnicodeAlgorithmBase : DescriptorAlgorithmBase
    {
        #region Constructors & destructor
        public DesUnicodeAlgorithmBase( HeapStatistics aStats )
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

        protected string ConvertToText( int aLengthInUnicodeChars, int aStartRawItemIndex )
        {
            StringBuilder text = new StringBuilder();
            for ( int i = aStartRawItemIndex; i < RawItems.Count && i < Math.Min( aLengthInUnicodeChars, DescriptorAlgorithmBase.KDescriptorTextMaxLength ); i++ )
            {
                RawItem item = RawItems[ i ];
                //
                text.Append( item.OriginalCharacterisedDataAsUnicode );
            }
            //
            string ret = text.ToString();
            if ( ret.Length > aLengthInUnicodeChars )
            {
                ret = ret.Substring( 0, aLengthInUnicodeChars );
            }
            return ret;
        }

        protected int PrintableCharacterCount( int aMaxNumberOfUnicodeWordsToProcess, out bool aAllWordsWereAsciiCharacters )
        {
            return PrintableCharacterCount( aMaxNumberOfUnicodeWordsToProcess, KDescriptorRawItemOffsetAfterLengthAndType, out aAllWordsWereAsciiCharacters );
        }

        protected int PrintableCharacterCount( int aMaxNumberOfUnicodeWordsToProcess, int aStartRawItemIndex, out bool aAllWordsWereAsciiCharacters )
        {
            int printableCharCount = 0;
            int wordsRemaining = aMaxNumberOfUnicodeWordsToProcess;
            
            for ( int i = aStartRawItemIndex; i < RawItems.Count && wordsRemaining > 0; i++ )
            {
                RawItem item = RawItems[ i ];
                ushort[] data = item.DataArrayWords;
                //
                bool word1IsPrint = IsPrintableAsciiCharacter( data[ 0 ] );
                if ( word1IsPrint )
                {
                    ++printableCharCount;
                }
                --wordsRemaining;
                //
                if ( wordsRemaining > 0 )
                {
                    bool word2IsPrint = IsPrintableAsciiCharacter( data[ 1 ] );
                    if ( word2IsPrint )
                    {
                        ++printableCharCount;
                    }
                    --wordsRemaining;
                }
            }
            //
            aAllWordsWereAsciiCharacters = ( aMaxNumberOfUnicodeWordsToProcess == printableCharCount );
            return printableCharCount;
        }

        protected static bool IsPrintableAsciiCharacter( ushort aWord )
        {
            bool ret = false;
            //
            if ( aWord >= 32 /*SPACE*/ && aWord < 127 /*DEL*/ || aWord == 0x0D /*CR*/ || aWord == 0x0A /*LF*/ || aWord == 0x09 /*TAB*/ )
            {
                ret = true;
            }
            //
            return ret;
        }
        #endregion
    }
}
