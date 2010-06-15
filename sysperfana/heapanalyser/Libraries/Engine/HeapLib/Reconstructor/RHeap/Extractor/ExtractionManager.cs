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

namespace HeapLib.Reconstructor.RHeap.Extractor
{
    internal class ExtractionManager
    {
        #region Constructors & destructor
        public ExtractionManager()
        {
        }
        #endregion

        #region API
        public bool TryToProcessLine( string aLine )
        {
            Match m = iRegEx.Match( aLine );
            bool success = m.Success;
	        //
            if ( success )
            {
                GroupCollection groups = m.Groups;
                iGpData = groups[ "Data" ].Captures;
                iGpCharacters = groups[ "CharData" ].Captures;

                // Get address
                Group gpAddress = groups[ "Address" ];
                string addressAsString = gpAddress.Value;
                iCurrentHeapAddress = System.Convert.ToUInt32( addressAsString, KBaseHex );
            }
            //
            return success;
        }
        #endregion

        #region Properties
        public uint CurrentHeapAddress
        {
            get { return iCurrentHeapAddress; }
        }

        public CaptureCollection Data
        {
            get { return iGpData; }
        }

        public CaptureCollection Characters
        {
            get { return iGpCharacters; }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Internal constants
        private const int KBaseHex = 16;
        #endregion

        #region Data members
        private static readonly Regex iRegEx = new Regex(
            @"(?<Address>[a-fA-F0-9]{8})\x3A\s{1}(?<Data>[a-fA-F0-9]{2}\s{1}){1,16}(?<Padding>\s{1}){0,44}(?<CharData>.){16}",
            RegexOptions.IgnoreCase
            );

        private uint iCurrentHeapAddress = 0;
        private CaptureCollection iGpData = null;
        private CaptureCollection iGpCharacters = null;
        #endregion
    }
}
