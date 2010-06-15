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
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils;
using SymbianUtils.RawItems;

namespace HeapLib.Reconstructor.RHeap.Extractor
{
    internal class RawItemQueue
    {
        #region Constructors & destructor
        public RawItemQueue()
        {
            iItems = new List<RawItem>( 10 * 1000 );
        }
        #endregion

        #region API
        public void Add( byte[] aData, uint aAddress )
        {
            int length = aData.Length;
            if ( length % 4 != 0 )
            {
                throw new ArgumentException( "Data is not a multiple of 4 bytes" );
            }

            for ( int i = 0; i < length; i += 4 )
            {
                // Make helper
                RawItemPrecursor prec = new RawItemPrecursor();
                prec.Bytes[ 3 ] = aData[ i + 0 ];
                prec.Bytes[ 2 ] = aData[ i + 1 ];
                prec.Bytes[ 1 ] = aData[ i + 2 ];
                prec.Bytes[ 0 ] = aData[ i + 3 ];
                prec.ConvertBytesToCharacters();

                // Make final item
                RawItem ret = new RawItem();
                ret.Address = (uint) ( aAddress + i );
                ret.OriginalData = prec.ByteValue;
                ret.Data = prec.ByteValueReversed;
                ret.CharacterisedData = prec.CharValue;
                //
                iItems.Add( ret );
            }
        }

        public void ReEnqueueItem( RawItem aItem )
        {
            iItems.Insert( 0, aItem );
        }

        public RawItem DequeueHeadItem()
        {
            RawItem head = iItems[ 0 ];
            iItems.RemoveAt( 0 );
            return head;
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iItems.Count; }
        }
        #endregion

        #region Internal methods
        private static RawItemPrecursor CreatePrecursor( Queue<Capture> aDataQ, Queue<char> aCharQ )
        {
            System.Diagnostics.Debug.Assert( aDataQ.Count >= 4 && aCharQ.Count >= 4 );
            //
            RawItemPrecursor ret = new RawItemPrecursor();

            // Get data
            ret.Bytes[ 3 ] = ConvertCaptureToByteValue( aDataQ.Dequeue() );
            ret.Bytes[ 2 ] = ConvertCaptureToByteValue( aDataQ.Dequeue() );
            ret.Bytes[ 1 ] = ConvertCaptureToByteValue( aDataQ.Dequeue() );
            ret.Bytes[ 0 ] = ConvertCaptureToByteValue( aDataQ.Dequeue() );
            
            // Get characters
            ret.Chars[ 0 ] = aCharQ.Dequeue();
            ret.Chars[ 1 ] = aCharQ.Dequeue();
            ret.Chars[ 2 ] = aCharQ.Dequeue();
            ret.Chars[ 3 ] = aCharQ.Dequeue();
            
            return ret;
        }

        private static uint ConvertCaptureToByteValue( Capture aCapture )
        {
            uint ret = System.Convert.ToUInt32( aCapture.Value.Trim(), KBaseHex );
            return ret;
        }

        private static uint GetValueFromByteArray( uint[] aArray )
        {
            uint ret = aArray[ 0 ];
            //
            ret += ( aArray[ 1 ] << 8 );
            ret += ( aArray[ 2 ] << 16 );
            ret += ( aArray[ 3 ] << 24 );
            //
            return ret;
        }

        private static uint GetReversedValueFromByteArray( uint[] aArray )
        {
            // Get original bytes
            uint b1 = aArray[ 0 ];
            uint b2 = aArray[ 1 ];
            uint b3 = aArray[ 2 ];
            uint b4 = aArray[ 3 ];

            // Build reversed value
            uint ret = b4;
            ret += ( b3 << 8 );
            ret += ( b2 << 16 );
            ret += ( b1 << 24 );
            //
            return ret;
        }

        private static string GetCharacterisedDataFromCharacterQueue( Queue<char> aCharQ )
        {
            char[] chars = { '.', '.', '.', '.' };
            //
            if ( aCharQ.Count >= 4 )
            {
                char c1 = aCharQ.Dequeue();
                char c2 = aCharQ.Dequeue();
                char c3 = aCharQ.Dequeue();
                char c4 = aCharQ.Dequeue();
                //
                chars[ 0 ] = c1;
                chars[ 1 ] = c2;
                chars[ 2 ] = c3;
                chars[ 3 ] = c4;
            }

            // It's very dumb that this is the only way to get a char array into a string.
            StringBuilder ret = new StringBuilder();
            foreach( char c in chars )
            {
                ret.Append( c );
            }
            return ret.ToString();
        }

        private static RawItem CreateRawItem( uint aAddress, Queue<Capture> aDataQ, Queue<char> aCharQ )
        {
            RawItemPrecursor precusor = CreatePrecursor( aDataQ, aCharQ );
            //
            RawItem ret = new RawItem();
            ret.Address = aAddress;
            ret.OriginalData = precusor.ByteValue;
            ret.Data = precusor.ByteValueReversed;
            ret.CharacterisedData = precusor.CharValueReversed;
            //
            return ret;
        }
        #endregion

        #region Internal constants
        private const int KBaseHex = 16;
        #endregion

        #region Data members
        private readonly List<RawItem> iItems;
        #endregion
    }
}
