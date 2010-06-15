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
using System.Text.RegularExpressions;

namespace HeapLib.Reconstructor.DataSources.Analyser.Extractor
{
    internal sealed class ExtractorBinary//: ExtractorBase
    {
        /*
        #region Constructors & destructor
        public ExtractorBinary()
        {
        }
        #endregion

        #region API
        public override ExtractedData ProcessLine( string aLine )
        {
            // [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]<BinHeapData:00000131:00000130:000000000001346e>
            // [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]00000000: be d0 be d0 ba da ba da 00 00 00 d0 01 00 00 00 ................
            // [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]00000010: 6e 34 01 00 82 00 00 00 83 00 00 00 01 00 00 00 n4..............
            // [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]00000020: 2c 00 00 00 61 6b 6e 73 6b 69 6e 73 72 76 2e 65 ,...aknskinsrv.e
            //
            // or
            //
            // <BinHeapData:00000131:00000130:000000000001346e>
            // 00000000: be d0 be d0 ba da ba da 00 00 00 d0 01 00 00 00 ................
            // 00000010: 6e 34 01 00 82 00 00 00 83 00 00 00 01 00 00 00 n4..............
            // 00000020: 2c 00 00 00 61 6b 6e 73 6b 69 6e 73 72 76 2e 65 ,...aknskinsrv.e
            string line = aLine;
            ExtractedData ret = new ExtractedData();

            // Check if it's a "[BinHeap:" line...
            bool isTaggedWithBinaryPrefix = KRegExHeapInfoTraceBinaryPrefix.IsMatch( line );
            if ( isTaggedWithBinaryPrefix )
            {
                Match m = KRegExHeapInfoTraceBinaryPrefix.Match( line );
                Group gpThreadId = m.Groups[ "TID" ];
                Group gpLine = m.Groups[ "LINE" ];

                ret.ThreadId = ThreadIdFromText( gpThreadId.Value );

                // Continue with rest of data...
                line = gpLine.Value;
            }

            // Check if it is a "<BinHeapData" line...
            bool accept = KRegExHeapInfoBinaryTag.IsMatch( aLine );
            if ( accept )
            {
                Match m = KRegExHeapInfoBinaryTag.Match( aLine );
                Group gpThreadId = m.Groups[ "TID" ];
                Group gpProcessId = m.Groups[ "PID" ];
                Group gpLength = m.Groups[ "LENGTH" ];

                // Get the thread id.
                uint tid = ThreadIdFromText( gpThreadId.Value );
                System.Diagnostics.Debug.Assert( ret.ThreadId == 0 || ret.ThreadId == tid );
                ret.ThreadId = tid;
            }
            else
            {
                // If we still didn't match, then it might be a MemSpy log file, in which case it'll just 
                // contain address lines
                accept = KRegExHeapInfoAddress.IsMatch( aLine );
                if ( accept )
                {
                    Match m = KRegExHeapInfoAddress.Match( aLine );
                    Group gpAddress = m.Groups[ "ADDRESS" ];
                    Group gpLine = m.Groups[ "LINE" ];

                    ret.Address = AddressFromText( gpAddress.Value );
                    ret.Payload = gpLine.Value;
                }
            }

            // Our hash is the thread id.
            ret.Hash = ret.ThreadId.ToString( "x8" );

            return ret;
        }
        #endregion

        #region Internal methods
        private static uint ThreadIdFromText( string aText )
        {
            uint ret = SymbianUtils.PrefixParser.ReadUint( ref aText );
            return ret;
        }

        private static uint AddressFromText( string aText )
        {
            uint ret = SymbianUtils.PrefixParser.ReadUint( ref aText );
            return ret;
        }
        #endregion

        #region Internal constants
        private const string KMemSpyBinaryHeapDumpTrace = "[BinHeap:";
        private const string KMemSpyBinaryHeapDumpLog = "<BinHeapData:";
        #endregion

        #region Internal regular expressions
        private static readonly Regex KRegExHeapInfoBinaryTag = new Regex( @"<(?<TYPE>\x2F?)BinHeapData\x3A(?<TID>[a-fA-F0-9]{8})\x3A(?<PID>[a-fA-F0-9]{8})\x3A(?<LENGTH>[a-fA-F0-9]{16})>", RegexOptions.Singleline | RegexOptions.CultureInvariant | RegexOptions.Compiled | RegexOptions.IgnoreCase );
        private static readonly Regex KRegExHeapInfoAddress = new Regex( @"(?<ADDRESS>[a-fA-F0-9]{8})\x5d(?<LINE>.+)", RegexOptions.Singleline | RegexOptions.CultureInvariant | RegexOptions.Compiled | RegexOptions.IgnoreCase );
        private static readonly Regex KRegExHeapInfoTraceBinaryPrefix = new Regex( @"^\x5bBinHeap\x3a(?<TID>[a-fA-F0-9]{8})\x5d(?<LINE>.+)", RegexOptions.Singleline | RegexOptions.CultureInvariant | RegexOptions.Compiled | RegexOptions.IgnoreCase );
        #endregion

        #region Data members
        #endregion
         */
    }
}
