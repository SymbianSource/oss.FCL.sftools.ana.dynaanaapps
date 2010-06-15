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

namespace HeapLib.Reconstructor.DataSources.Analyser.Extractor
{
    internal sealed class ExtractedData
    {
        #region Enumerations
        public enum TType
        {
            EText = 0,
            EBinaryData
        }
        #endregion

        #region Static constructor
        public static ExtractedData NewText( string aText, string aOriginalLine )
        {
            ExtractedData self = new ExtractedData( aText, aOriginalLine );
            return self;
        }

        public static ExtractedData NewBinaryData( byte[] aBinaryData, string aOriginalLine )
        {
            ExtractedData self = new ExtractedData( aBinaryData, aOriginalLine );
            return self;
        }
        #endregion

        #region Constructors & destructor
        private ExtractedData( string aOriginalLine )
        {
            iOriginalLine = aOriginalLine;
        }

        private ExtractedData( string aText, string aOriginalLine )
            : this( aOriginalLine ) 
        {
            iType = TType.EText;
            iText = aText;
        }

        private ExtractedData( byte[] aBinaryData, string aOriginalLine )
            : this( aOriginalLine )
        {
            iType = TType.EBinaryData;
            iBinaryData = aBinaryData;
        }
        #endregion

        #region Properties
        public TType Type
        {
            get { return iType; }
        }

        public string Text
        {
            get
            {
                if ( iType != TType.EText )
                {
                    throw new ArgumentException();
                }

                return iText;
            }
        }

        public byte[] BinaryData
        {
            get
            {
                if ( iType != TType.EBinaryData )
                {
                    throw new ArgumentException();
                }

                return iBinaryData;
            }
        }

        public string OriginalLine
        {
            get
            {
                return iOriginalLine;
            }
        }
        #endregion

        #region Data members
        private readonly TType iType;
        private readonly string iOriginalLine;
        private string iText = null;
        private byte[] iBinaryData = null;
        #endregion
    }
}
