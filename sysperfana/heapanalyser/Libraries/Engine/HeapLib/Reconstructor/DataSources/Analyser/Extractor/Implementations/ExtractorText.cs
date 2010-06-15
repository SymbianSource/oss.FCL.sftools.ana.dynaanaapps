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

#define SUPPORT_OLD_MEMSPY_KERNEL_HEAP_DUMPS

using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources.Analyser.Interpreter;
using SymbianUtils;

namespace HeapLib.Reconstructor.DataSources.Analyser.Extractor.Implementations
{
    internal sealed class ExtractorText : Extractor
    {
        #region Constructors & destructor
        public ExtractorText()
        {
        }
        #endregion

        #region API
        public override bool CanExtractFrom( string aLine )
        {
            // CASE 1: Original file format, logging via trace
            // 
            //      HeapData - EComServer::!ecomserver - HEAP INFO FOR THREAD 'EComServer::!ecomserver'
            //      HeapData - EComServer::!ecomserver - ==============================================
            //      HeapData - EComServer::!ecomserver - HeapInfo - heapBaseAddress:     0x00700074
            //      HeapData - EComServer::!ecomserver - HeapInfo - heapSize:                745352
            //      HeapData - EComServer::!ecomserver - HeapInfo - heapChunkSize:           745472
            //      ...
            //      HeapData - EComServer::!ecomserver - 00700084: 00 00 00 00 03 03 03 03 03 03 03 03 03 03 03 03 ................
            //      HeapData - EComServer::!ecomserver - 00700094: 03 03 03 03 03 03 03 03 28 00 00 00 ec 47 28 80 ........(....G(.
            //      HeapData - EComServer::!ecomserver - 007000a4: d8 0c 70 00 58 0d 70 00 f8 0c 70 00 03 03 03 03 ..p.X.p...p.....
            //      HeapData - EComServer::!ecomserver - 007000b4: 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 ................
            //      HeapData - EComServer::!ecomserver - 007000c4: 48 00 00 00 e8 2b 30 80 00 2c 30 80 ec 02 70 00 H....+0..,0...p.
            //
            // CASE 2: Original file format, logging to file
            //
            //      HEAP INFO FOR THREAD 'ecomserver::!ecomserver'
            //      ==============================================
            //      HeapInfo - heapBaseAddress:     0x0f8c0074
            //      HeapInfo - heapSize:                262028
            //
            // CASE 3: Original file format, logging to file - kernel - the thread name is omitted!
            //
            //      HeapInfo - heapBaseAddress:     0xc809b074
            //      HeapInfo - heapSize:               3374984
            //      HeapInfo - heapMinSize:             720892
            //      HeapInfo - heapMaxSize:           17408000
            //      HeapInfo - heapFreeCellAddress: 0xc809b04c
            //      HeapInfo - heapFreeCellLength:           0
            //      HeapInfo - heapMinCellSize:              40
            //      HeapInfo - heapUsingDebugAlloc:           0
            //      HeapInfo - chunkName:              ekern.exe[100041af]0001::SvHeap
            //      HeapInfo - heapCellAllocCount:       15216
            //      HeapInfo - heapAllocSpace:         3171512
            //
            // CASE 4: New file format, still text, logging via trace
            //
            //      HeapData - ApsExe::AppArcServerThread - HEAP INFO FOR THREAD 'APSEXE::APPARCSERVERTHREAD'  
            //      HeapData - ApsExe::AppArcServerThread - =================================================  
            //      HeapData - ApsExe::AppArcServerThread -                                                     
            //      HeapData - ApsExe::AppArcServerThread - Meta Data                                          
            //      HeapData - ApsExe::AppArcServerThread - =========                                          
            //      HeapData - ApsExe::AppArcServerThread - Type:                                               Symbian OS RHeap
            //      HeapData - ApsExe::AppArcServerThread - Chunk Name:                                         ApsExe.exe[10003a3f]0001::AppArcServerThread::$HEAP
            //      HeapData - ApsExe::AppArcServerThread - Chunk Size:                                         319488
            //      HeapData - ApsExe::AppArcServerThread - Chunk Base Address:                                 0x00600000
            //      HeapData - ApsExe::AppArcServerThread - Debug Allocator:                                    0
            //      

            // First step is to identify the prefix. We do this by looking for the common line which remains
            // unchanged in all text file formats, and that is the "HEAP INFO FOR THREAD" item.
            //
            // If the data included tracing prefixes (e.g. Musti), these will already have been removed.
            bool ret = false;

            string line = aLine;

#if SUPPORT_OLD_MEMSPY_KERNEL_HEAP_DUMPS
            if ( line.Contains( "HeapInfo - heapBaseAddress:     0xc" ) )
            {
                // This is a work around for a problem with an old version of MemSpy in which
                // the kernel thread name was not included in the heap data listing. This
                // results in Heap Analyser discarding the thread entirely.
                //
                // Spoof the line
                int startPos = line.IndexOf( "HeapInfo - heapBaseAddress:     0xc" );
                line = line.Substring( 0, startPos ) + "HEAP INFO FOR THREAD \'ekern.exe[100041af]0001::Supervisor\'  ";
            }
#endif

            Match heapInfoForThreadMatch = KHeapInfoInitialRegEx.Match( line );
            if ( heapInfoForThreadMatch.Success )
            {
                // Work out if there is a prefix prior to the HEAP INFO element...
                int pos = heapInfoForThreadMatch.Index;
                if ( pos == 0 )
                {
                    // There's no prefix
                }
                else
                {
                    // There is some kind of standard prefix
                    iPrefix = line.Substring( 0, pos );

                    // Check if there is the "HeapData -" prefix also. If there is we may need to strip off a musti timestamp:
                    //
                    // E.g.:
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600a74: 00 00 00 00 20 00 00 00 04 00 00 00 57 4c 29 80 ............WL).
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600a84: 00 00 00 00 d8 11 00 00 03 03 03 03 03 03 03 03 ................
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600a94: 28 00 00 00 64 f9 52 80 00 24 60 00 50 00 00 10 (...d.R..$`.P...
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600aa4: 69 3c 00 10 d6 72 20 10 00 00 00 00 01 00 00 00 i<...r..........
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600ab4: 38 21 60 00 00 00 00 00 40 00 00 00 c4 4c 2c 80 8!`.....@....L,.
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600ac4: 38 00 00 00 20 00 00 00 40 00 00 00 28 43 60 00 8.......@...(C`.
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600ad4: 28 00 00 00 a4 1f 60 00 14 00 bf 00 c1 00 cb 00 (.....`.........
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600ae4: 10 4e 40 00 ad 2f 37 80 a8 1f 60 00 03 03 03 03 .N@../7...`.....
                    // 10:54:29.672 HeapData - !MsvServer::!MsvServer - 00600af4: 03 03 03 03 03 03 03 03 98 04 00 00 94 f6 52 80 ..............R.
                    if ( iPrefix.Contains( KHeapDataPrefix ) )
                    {
                        int pos2 = iPrefix.IndexOf( KHeapDataPrefix );
                        iPrefix = iPrefix.Substring( pos2 );
                    }
                }

                // Either way, we need the thread name as we use this
                // as the identifier for the data source, i.e. the hash
                iThreadName = heapInfoForThreadMatch.Groups[ "ThreadName" ].Value;
                
                // Save data after the prefix
                base.PrimaryLine = line.Substring( heapInfoForThreadMatch.Index );

                ret = true;
            }

            return ret;
        }

        public override bool ExtractFrom( string aLine, bool aForceExtractionOfThreadIdentifier )
        {
            // First pass identification stage
            string line = aLine;
            bool handled = ( iPrefix.Length == 0 );
            if ( !handled )
            {
                // Check for prefix match
                handled = aLine.Contains( iPrefix );
                if ( handled )
                {
                    // Check that it's not the start of a new data set
                    if ( !aForceExtractionOfThreadIdentifier && aLine.Contains( KHeapDataDataSetStartMarker ) ) // "HEAP INFO FOR THREAD"
                    {
                        handled = false;
                    }
                    else
                    {
                        int pos = aLine.IndexOf( iPrefix );
                        line = aLine.Substring( pos + iPrefix.Length );
                    }
                }
            }

            // Data processing stage
            if ( handled )
            {
                ExtractedData data = PrepareExtractedData( line, aLine );
                base.Interpreter.Interpret( data );
            }

            // Done
            return handled;
        }
        #endregion

        #region Properties
        public override string Hash
        {
            get { return iThreadName; }
        }
        #endregion

        #region Internal methods
        private ExtractedData PrepareExtractedData( string aLine, string aOriginalLine )
        {
            ExtractedData ret = null;
            
            // We need the heap base address before we are able to start
            // extracting binary data.
            DataSource ds = Interpreter.DataSource;
            Elements.MetaData metaData = ds.MetaData;
            Elements.Groups.GpHeap heapInfo = metaData.Heap;
            uint baseAddress = heapInfo.HeapBaseAddress;
            if ( baseAddress != 0 )
            {
                // We can now work out the next expected address
                uint nextExpectedAddress = baseAddress + (uint) metaData.HeapData.Count;

                // Build our regex string that will match the binary heap data.
                string prefix = string.Format( "{0:x8}: ", nextExpectedAddress );
                int pos = aLine.IndexOf( prefix );
                if ( pos >= 0 )
                {
                    Match m = KHeapDataRegEx.Match( aLine );
                    if ( m.Success )
                    {
                        List<byte> bytes = new List<byte>();
                        //
                        CaptureCollection data = m.Groups[ "Data" ].Captures;
                        foreach ( Capture dataItem in data )
                        {
                            string hexValue = dataItem.Value.Trim();
                            uint byteVal = System.Convert.ToUInt32( hexValue, KBaseHex );
                            bytes.Add( (byte) byteVal );
                        }
                        //
                        ret = ExtractedData.NewBinaryData( bytes.ToArray(), aOriginalLine );
                    }
                }
                //
                if ( ret == null )
                {
                    ret = ExtractedData.NewText( aLine, aOriginalLine );
                }
            }
            else
            {
                ret = ExtractedData.NewText( aLine, aOriginalLine );
            }
            //
            return ret;
        }
        #endregion

        #region Internal constants
        private const int KBaseHex = 16;
        private const string KHeapDataPrefix = "HeapData -";
        private const string KHeapDataDataSetStartMarker = "HEAP INFO FOR THREAD";
        #endregion

        #region Internal regular expressions
        private static readonly Regex KHeapInfoInitialRegEx = new Regex( 
            @"HEAP INFO FOR THREAD \'(?<ThreadName>.+)\'", 
            RegexOptions.Singleline | RegexOptions.Compiled | RegexOptions.IgnoreCase );
        private static readonly Regex KHeapDataRegEx = new Regex(
                                                        @"\x3A" +
                                                        @"\s{1}" +
                                                        @"(?<Data>[a-fA-F0-9]{2}\s{1}){1,16}" +
                                                        @"(?<Padding>\s{1}){0,44}",
            RegexOptions.Singleline | RegexOptions.Compiled | RegexOptions.IgnoreCase );
        #endregion

        #region Data members
        private string iPrefix = string.Empty;
        private string iThreadName = string.Empty;
        #endregion
    }
}
