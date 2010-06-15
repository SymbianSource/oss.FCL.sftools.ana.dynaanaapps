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

namespace HeapLib.Reconstructor.RHeap.Extractor
{
    internal class RawItemPrecursor
    {
        #region Constructors & destructor
        public RawItemPrecursor()
        {
        }
        #endregion

        #region API
        public void ConvertBytesToCharacters()
        {
            for ( int i = 0; i <= 3; i++ )
            {
                uint b = Bytes[ i ];
                char c = System.Convert.ToChar( b );
                //
                SetCharValue( c, i );
            }
        }

        public void SetCharValue( char aValue, int aIndex )
        {
            if ( aIndex < 0 || aIndex > 3 )
            {
                throw new ArgumentException( "Invalid index" );
            }

            int charCode = (int) aValue;
            if ( charCode >= 0x20 && charCode < 0x7F )
            {
                Chars[ aIndex ] = aValue;
            }
            else
            {
                Chars[ aIndex ] = '.';
            }
        }
        #endregion

        #region Properties
        public uint[] Bytes 
        {
            get { return iBytes; }
        }

        public uint ByteValue
        {
            get
            {
                uint ret = 0;
                //
                ret += ( iBytes[ 0 ] );
                ret += ( iBytes[ 1 ] << 8 );
                ret += ( iBytes[ 2 ] << 16 );
                ret += ( iBytes[ 3 ] << 24 );
                //
                return ret;
            }
        }

        public uint ByteValueReversed
        {
            get
            {
                uint ret = 0;
                //
                ret += ( iBytes[ 3 ] );
                ret += ( iBytes[ 2 ] << 8 );
                ret += ( iBytes[ 1 ] << 16 );
                ret += ( iBytes[ 0 ] << 24 );
                //
                return ret;
            }
        }

        public char[] Chars
        {
            get { return iChars; }
            set { iChars = value; }
        }

        public string CharValue
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                foreach ( char c in iChars )
                {
                    ret.Append( c );
                }
                //
                return ret.ToString();
            }
        }

        public string CharValueReversed
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                foreach ( char c in iChars )
                {
                    ret.Insert( 0, c );
                }
                //
                return ret.ToString();
            }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Data members
        private uint[] iBytes = new uint[] { 0, 0, 0, 0 };
        private char[] iChars = new char[] { '.', '.', '.', '.' };
        #endregion
    }
}
