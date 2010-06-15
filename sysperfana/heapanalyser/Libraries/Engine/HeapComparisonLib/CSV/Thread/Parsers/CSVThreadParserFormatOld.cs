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

namespace HeapComparisonLib.CSV.Thread.Parsers
{
    internal class CSVThreadParserFormatOld
    {
        #region Constructors & destructor
        public CSVThreadParserFormatOld( CSVThreadParser aParser )
        {
        }
        #endregion

        #region API
        public CSVThread ParseLine( string aLine )
        {
            CSVThread ret = null;
            //
            Match m = KParserRegEx.Match( aLine );
            if ( m.Success )
            {
                ret = CSVThread.New();
                //
                string[] items = m.Value.Split( ',' );
                if ( items.Length >= KExpectedItemCount )
                {
                    // Ensure each item is trimmed before processing
                    for ( int i = 0; i < items.Length; i++ )
                    {
                        items[ i ] = items[ i ].Trim();
                    }

                    try
                    {
                        // THREAD
                        ret.ThreadName = items[ 0 ];

                        // CHUNK
                        ret.ChunkName = items[ 1 ];
                        ret.ChunkBaseAddress = ParseHexValue( items[ 2 ] );

                        // HEAP
                        ret.SizeCurrent = ParseDecimalValue( items[ 3 ] );
                        ret.SizeMin = ParseDecimalValue( items[ 4 ] );
                        ret.SizeMax = ParseDecimalValue( items[ 5 ] );

                        // FIRST FREE CELL
                        ret.FirstFreeCellAddress = ParseHexValue( items[ 6 ] );
                        ret.FirstFreeCellLength = ParseDecimalValue( items[ 7 ] );

                        // OTHER FREE CELL INFO
                        ret.FreeCellCount = ParseDecimalValue( items[ 8 ] );
                        ret.FreeSpaceTotal = ParseDecimalValue( items[ 9 ] );
                        ret.FreeSpaceSlack = ParseDecimalValue( items[ 10 ] );
                        ret.FreeCellLargest = ParseDecimalValue( items[ 11 ] );

                        // ALLOC CELL INFO
                        ret.AllocCellLargest = ParseDecimalValue( items[ 12 ] );
                        ret.AllocCellCount = ParseDecimalValue( items[ 13 ] );
                        ret.AllocSpaceTotal = ParseDecimalValue( items[ 15 ] );

                        // MISC
                        ret.MinCellSize = ParseDecimalValue( items[ 14 ] );
                    }
                    catch ( Exception )
                    {
                        ret = null;
                    }
                }
            }

            return ret;
        }
        #endregion

        #region Internal constants
        private const int KExpectedItemCount = 16;
        #endregion

        #region Internal methods
        private static long ParseHexValue( string aItem )
        {
            long ret = 0;
            //
            if ( aItem.Length > 0 )
            {
                const string KHexPrefix = "0x";
                if ( aItem.IndexOf( KHexPrefix ) == 0 )
                {
                    aItem = aItem.Substring( KHexPrefix.Length );
                }

                ret = System.Convert.ToInt32( aItem, 16 );
            }
            //
            return ret;
        }

        private static long ParseDecimalValue( string aItem )
        {
            long ret = 0;
            //
            if ( aItem.Length > 0 )
            {
                ret = System.Convert.ToInt32( aItem );
            }
            //
            return ret;
        }
        #endregion

        #region Internal regular expression
        // <summary>
        //  Regular expression built for C# on: Tue, Sep 9, 2008, 02:09:20 PM
        //  Using Expresso Version: 3.0.2766, http://www.ultrapico.com
        //  
        //  A description of the regular expression:
        //  
        //  [Thread]: A named capture group. [(?:[A-Za-z0-9!_$ ]+)\:\:(?:[A-Za-z0-9!_$: ]+)]
        //      (?:[A-Za-z0-9!_$ ]+)\:\:(?:[A-Za-z0-9!_$: ]+)
        //          Match expression but don't capture it. [[A-Za-z0-9!_$ ]+]
        //              Any character in this class: [A-Za-z0-9!_$ ], one or more repetitions
        //          Literal :
        //          Literal :
        //          Match expression but don't capture it. [[A-Za-z0-9!_$: ]+]
        //              Any character in this class: [A-Za-z0-9!_$: ], one or more repetitions
        //  Literal ,
        //  [Chunk]: A named capture group. [[A-Za-z0-9!_$ \[\]:.]+]
        //      Any character in this class: [A-Za-z0-9!_$ \[\]:.], one or more repetitions
        //  Match expression but don't capture it. [   \,   (?<Items>        [A-Za-z0-9!_$ \[\]:.]   +)], exactly 14 repetitions
        //         \,   (?<Items>        [A-Za-z0-9!_$ \[\]:.]   +)
        //          Literal ,
        //          [Items]: A named capture group. [        [A-Za-z0-9!_$ \[\]:.]   +]
        //              Any character in this class: [A-Za-z0-9!_$ \[\]:.], one or more repetitions
        //  
        //
        // </summary>
        private static readonly Regex KParserRegEx = new Regex(
              "(?<Thread>(?:[A-Za-z0-9!_$ ]+)\\:\\:(?:[A-Za-z0-9!_$: ]+))\r\n"+
              "\\,\r\n(?<Chunk>[A-Za-z0-9!_$ \\[\\]:.]+)\r\n(?:\r\n   \\,\r\n   (?<"+
              "Items>\r\n        [A-Za-z0-9!_$ \\[\\]:.]\r\n   +)\r\n){14}",
            RegexOptions.Singleline
            | RegexOptions.CultureInvariant
            | RegexOptions.IgnorePatternWhitespace
            | RegexOptions.Compiled
            );
        #endregion

        #region Data members
        #endregion
    }
}
