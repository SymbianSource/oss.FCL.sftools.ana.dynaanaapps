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
using SymbianUtils;
using SymbianStructuresLib.CodeSegments;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.DataSources.Analyser.Extractor;

namespace HeapLib.Reconstructor.DataSources.Analyser.Interpreter
{
    internal sealed class InterpreterText : InterpreterBase
    {
        #region Constructors & destructor
        public InterpreterText()
        {
        }
        #endregion

        #region API
        public override bool CanInterpretFrom( Extractor.Extractor aExtractor )
        {
            // The text interpreter only works with the text extractor
            bool ret = ( aExtractor is Extractor.Implementations.ExtractorText );
            if ( ret )
            {
            }
            //
            return ret;
        }

        public override void PrepareToStart( Extractor.Extractor aExtractor )
        {
            // Seed the data source with the thread name. We can do this immediately
            // since the thread name is identified during the extractor initialisation
            DataSource.ThreadName = aExtractor.Hash;
        }

        public override void Interpret( ExtractedData aItem )
        {
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HEAP INFO FOR THREAD 'EComServer::!ecomserver'
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - ==============================================
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapBaseAddress:     0x00700074
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapSize:                745352
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapChunkSize:           745472
            // ...
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 00700084: 00 00 00 00 03 03 03 03 03 03 03 03 03 03 03 03 ................
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 00700094: 03 03 03 03 03 03 03 03 28 00 00 00 ec 47 28 80 ........(....G(.
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000a4: d8 0c 70 00 58 0d 70 00 f8 0c 70 00 03 03 03 03 ..p.X.p...p.....
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000b4: 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 ................
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000c4: 48 00 00 00 e8 2b 30 80 00 2c 30 80 ec 02 70 00 H....+0..,0...p.
            //
            // or
            //
            // HEAP INFO FOR THREAD 'ecomserver::!ecomserver'
            // ==============================================
            // HeapInfo - heapBaseAddress:     0x0f8c0074
            // HeapInfo - heapSize:                262028

            // Save the line - we must do this in order to be able to write a zip file
            // containing the original source data for this data source.
            DataSource.AddLine( aItem.OriginalLine );
            
            if ( aItem.Type == ExtractedData.TType.EBinaryData )
            {
                DataSource.MetaData.HeapData.Add( aItem.BinaryData );
            }
            else if ( aItem.Type == ExtractedData.TType.EText )
            {
                if ( CheckForKnownElement( aItem.Text ) )
                {
                }
            }
        }
        #endregion

        #region Internal methods
        private bool CheckForKnownElement( string aLine )
        {
            bool handled = false;
            string origLine = aLine;
            //
            if ( CheckForKnownElement( ref aLine, iPrefixes.HeapBaseAddress ) )
            {
                DataSource.MetaData.Heap.HeapBaseAddress = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.HeapChunkSize ) )
            {
                DataSource.MetaData.Heap.ChunkSize = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.SizeOfRHeap ) )
            {
                DataSource.MetaData.Heap.SizeOfRHeap = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.HeapCellMinimumSize ) )
            {
                DataSource.MetaData.Heap.MinCellSize = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.FreeCellAddress ) )
            {
                DataSource.MetaData.Heap.InfoFree.FreeCellAddress = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.FreeCellLength ) )
            {
                DataSource.MetaData.Heap.InfoFree.FreeCellLength = PrefixParser.ReadUint( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.DebugAllocator ) )
            {
                DataSource.MetaData.Heap.DebugAllocator = PrefixParser.ReadBool( ref aLine );
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.MiscRHeapRand ) )
            {
                long val = PrefixParser.ReadLong( ref aLine );
                uint rand = (uint) val;
                DataSource.MetaData.Heap.Rand = rand;
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.FreeStatistics ) )
            {
                iState = TState.EStateStatisticsFree;
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.AllocStatistics ) )
            {
                iState = TState.EStateStatisticsAlloc;
                handled = true;
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.FreeCellCount ) )
            {
                if ( iState == TState.EStateNone || iState == TState.EStateStatisticsFree )
                {
                    DataSource.MetaData.Heap.InfoFree.FreeCellCount = PrefixParser.ReadInt( ref aLine );
                    handled = true;
                }
                else if ( iState == TState.EStateStatisticsAlloc )
                {
                    // Must also handle alloc cells inside the free block, because in the new file format
                    // both alloc and free cell statistics contain the same prefix.
                    DataSource.MetaData.Heap.InfoAlloc.AllocCellCount = PrefixParser.ReadInt( ref aLine );
                    handled = true;
                }
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.CommonStatisticsSizeOfCells ) )
            {
                if ( iState == TState.EStateStatisticsFree )
                {
                    DataSource.MetaData.Heap.InfoFree.FreeCellTotalSpace = PrefixParser.ReadUint( ref aLine );
                    handled = true;
                }
                else if ( iState == TState.EStateStatisticsAlloc )
                {
                    DataSource.MetaData.Heap.InfoAlloc.AllocCellTotalSpace = PrefixParser.ReadUint( ref aLine );
                    handled = true;
                }
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.AllocCellCount ) )
            {
                if ( iState == TState.EStateNone || iState == TState.EStateStatisticsAlloc )
                {
                    DataSource.MetaData.Heap.InfoAlloc.AllocCellCount = PrefixParser.ReadInt( ref aLine );
                    handled = true;
                }
            }
            else if ( CheckForKnownElement( ref aLine, iPrefixes.FreeCellList ) )
            {
                iState = TState.EStateFreeCellList;
            }
            else
            {
                // Check for free cell list match
                if ( iState == TState.EStateFreeCellList )
                {
                    Match freeCellListMatch = KFreeCellRegEx.Match( aLine );
                    if ( freeCellListMatch.Success )
                    {
                        int index = int.Parse( freeCellListMatch.Groups[ "Index" ].Value );
                        uint address = uint.Parse( freeCellListMatch.Groups[ "Address" ].Value, System.Globalization.NumberStyles.AllowHexSpecifier );
                        int length = int.Parse( freeCellListMatch.Groups[ "Length" ].Value );
                        int type = int.Parse( freeCellListMatch.Groups[ "Type" ].Value );

                        // The index should be between 1 and FreeCellCount
                        int max = DataSource.MetaData.Heap.InfoFree.FreeCellCount;

                        // However, if we didn't read the free cell count then don't panic...
                        if ( max > 0 )
                        {
                            if ( index >= 1 && index <= max )
                            {
                                DataSource.MetaData.Heap.InfoFree.AddFreeCell( address, length, type );
                            }
                            else
                            {
                                DataSource.AddError( DataSource.TErrorTypes.EErrorTypeInvalidFreeCellIndexInFreeList );
                            }
                        }
                        else
                        {
                            // No free cell to validate against, just hope it's okay!
                            DataSource.MetaData.Heap.InfoFree.AddFreeCell( address, length, type );
                        }
                    }
                }

                // Is it a code segment?
                if ( !handled )
                {
                    CodeSegDefinition def = CodeSegDefinitionParser.ParseDefinition( aLine );
                    if ( def != null )
                    {
                        DataSource.MetaData.CodeSegments.Add( def );
                    }
                }
            }
            //
            return handled;
        }

        private bool CheckForKnownElement( ref string aLine, string[] aPrefixes )
        {
            bool handled = false;
            //
            foreach ( string prefix in aPrefixes )
            {
                if ( aLine.Contains( prefix ) )
                {
                    PrefixParser.SkipPrefix( prefix, ref aLine );
                    handled = true;
                    break;
                }
            }
            //
            return handled;
        }
        #endregion

        #region Internal enumerations
        private enum TState
        {
            EStateNone = 0,
            EStateStatisticsFree,
            EStateStatisticsAlloc,
            EStateFreeCellList,
        }
        #endregion

        #region Internal constants
        private readonly static Regex KFreeCellRegEx = new Regex(
           "FC\\s(?<Index>[0-9]{4})\\s+0x(?<Address>[0-9a-fA-F]{8})\\s+(" +
           "?<Length>[0-9]+)\\s(?<Type>[0-9]{1})\r\n",
           RegexOptions.Multiline
         | RegexOptions.CultureInvariant
         | RegexOptions.IgnorePatternWhitespace
         | RegexOptions.Compiled
         ); 
        #endregion

        #region Data members
        private Prefixes iPrefixes = new Prefixes();
        private TState iState = TState.EStateNone;
        #endregion
    }
}
