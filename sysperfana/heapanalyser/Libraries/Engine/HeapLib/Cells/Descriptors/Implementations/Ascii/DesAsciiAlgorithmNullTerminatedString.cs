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
    internal class DesAsciiAlgorithmNullTerminatedString : DesAsciiAlgorithmBase
    {
        #region Constructors & destructor
        public DesAsciiAlgorithmNullTerminatedString( HeapStatistics aStats )
            : base( aStats )
        {
        }
        #endregion

        #region From DescriptorAlgorithmBase
        internal override bool IsDescriptor( HeapCell aCell, out DescriptorInfo aInfo )
        {
            // Use this to initialise 'Cell'
            base.IsDescriptor( aCell, out aInfo );

            bool isDes = ( aCell.Symbol == null && RawItems.Count > 1 );
            if ( isDes )
            {
                isDes = false;
                int checkedCharCount = 0;
                int printableCharCount = 0;
                int letterOrDigitCount = 0;
                int numberOfHeapAddresses = 0;
                
                // Keep reading chars until we hit a NULL
                bool foundNull = false;
                foreach ( RawItem item in RawItems )
                {
                    if ( Statistics.WithinHeapBounds( item.Data ) )
                    {
                        ++numberOfHeapAddresses;
                    }

                    byte[] data = item.DataArray;
                    foreach ( byte b in data )
                    {
                        if ( b != 0 )
                        {
                            ++checkedCharCount;

                            char c = Convert.ToChar( b );
                            if ( Char.IsLetterOrDigit( c ) )
                            {
                                ++letterOrDigitCount;
                            }

                            bool isPrint = IsPrintableAsciiCharacter( b );
                            if ( isPrint )
                            {
                                ++printableCharCount;
                            }
                            else
                            {
                                break;
                            }
                        }
                        else
                        {
                            foundNull = true;
                            break;
                        }
                    }

                    if ( foundNull )
                    {
                        break;
                    }
                }

                // All chars must be printable
                bool allWerePrintable = ( checkedCharCount == printableCharCount && printableCharCount > 0 );
                if ( foundNull && allWerePrintable && numberOfHeapAddresses == 0 && letterOrDigitCount > 0 )
                {
                    // If the size of the cell is very big and the "string" itself is very small, then chances
                    // are this is not a null terminated string at all. 
                    int percentageOfStringUsed = AsPercentage( aCell, printableCharCount );
                    if ( percentageOfStringUsed > KMinimumUtilisationPercentage )
                    {
                        aInfo = new DescriptorInfo( aCell );
                        aInfo.Text = ConvertToText( printableCharCount, 0 );
                        aInfo.Type = HeapCell.TDescriptorType.ENullTerminatedString;
                        isDes = true;
                    }
                }
            }
            //
            return isDes;
        }
        #endregion

        #region Internal constants
        private const int KMinimumUtilisationPercentage = 10; // Percent
        #endregion

        #region Internal methods
        private static int AsPercentage( HeapCell aCell, int aLength )
        {
            uint totalLength = aCell.PayloadLength;
            float ret = ( (float) aLength / (float) totalLength ) * 100.0f;
            return (int) ret;
        }
        #endregion
    }
}
