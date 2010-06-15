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
    internal class CSVThreadParserFormatNew
    {
        #region Constructors & destructor
        public CSVThreadParserFormatNew( CSVThreadParser aParser )
        {
            iParser = aParser;
        }
        #endregion

        #region API
        public CSVThread ParseLine( string aLine )
        {
            if ( !TryToParseEntry( aLine ) )
            {
                if ( !TryToParseTimestamp( aLine ) )
                {
                    TryToParseSupportedTags( aLine );
                }
            }

            // Return a constructed entry (or null) if we have one ready
            CSVThread ret = iConstructedEntry;
            iConstructedEntry = null;
            return ret;
        }
        #endregion

        #region Internal enumerations
        private enum TState
        {
            EStateIdle = 0,
            EStateSeenEntryStart,
            EStateSeenThreadName,
            EStateSeenProcessName,
            EStateSeenChunkName,
            EStateSeenFieldBody,
            EStateSeenEntryEnd,
        }
        #endregion

        #region Internal methods
        private bool TryToParseEntry( string aLine )
        {
            Match matchEntry = KRegExEntry.Match( aLine );
            if ( matchEntry.Success )
            {
                // Check if it's an opening or closing tag.
                bool isOpen = string.IsNullOrEmpty( matchEntry.Groups[ "TagType" ].Value );
                int index = int.Parse( matchEntry.Groups[ "Index" ].Value );
                if ( isOpen )
                {
                    // Opening tag - starting a new entry, save entry id
                    ChangeState( TState.EStateSeenEntryStart );
                    SaveCurrentEntryIfValidState();

                    iCurrentEntryId = index;
                    iWorkInProgressThread = CSVThread.New();
                }
                else
                {
                    // Closing tag, we should've finished an entry now. 
                    // Validate the index is as we expect
                    CheckExpectedIndexId( index );
                    ChangeState( TState.EStateSeenEntryEnd );
                    SaveCurrentEntryIfValidState();
                }
            }
            
            return matchEntry.Success;
        }

        private bool TryToParseTimestamp( string aLine )
        {
            Match match = KRegExTimestamp.Match( aLine );
            if ( match.Success )
            {
                // Get time value
                long timestamp = long.Parse( match.Groups[ "Timestamp" ].Value );
                if ( iParser.CurrentDataSet != null )
                {
                    iParser.CurrentDataSet.TimeStamp = timestamp;
                }
            }

            return match.Success;
        }

        private bool TryToParseSupportedTags( string aLine )
        {
            Match matchStandardFields = KRegExTagTypes.Match( aLine );
            if ( matchStandardFields.Success )
            {
                if ( iWorkInProgressThread == null )
                {
                    throw new Exception( "Corruption detected - work in progress thread is null" );
                }

                // Check the index is what we expect it to be
                int index = int.Parse( matchStandardFields.Groups[ "Index" ].Value );
                CheckExpectedIndexId( index );

                // Now digest the tag body
                string body = matchStandardFields.Groups[ "Body" ].Value;
                string tagName = matchStandardFields.Groups[ "TagName" ].Value;
                if ( tagName == "THREAD_NAME" )
                {
                    iWorkInProgressThread.ThreadName = body;
                    ChangeState( TState.EStateSeenThreadName );
                }
                else if ( tagName == "PROCESS_NAME" )
                {
                    iWorkInProgressThread.ProcessName = body;
                    ChangeState( TState.EStateSeenProcessName );
                }
                else if ( tagName == "CHUNK_NAME" )
                {
                    iWorkInProgressThread.ChunkName = body;
                    ChangeState( TState.EStateSeenChunkName );
                }
                else if ( tagName == "FIELDS" )
                {
                    string[] elements = body.Trim().Split( ',' );
                    ExtractFields( elements );
                    ChangeState( TState.EStateSeenFieldBody );
                }
            }

            return matchStandardFields.Success;
        }

        private void ChangeState( TState aState )
        {
            iState = aState;
        }

        private void CheckExpectedIndexId( int aValue )
        {
            if ( aValue != iCurrentEntryId )
            {
                throw new Exception( "Corruption detected - index id incorrect" );
            }
        }

        private void SaveCurrentEntryIfValidState()
        {
            if ( iWorkInProgressThread != null )
            {
                // Perhaps we didn't encounter the closing tag for some odd reason?
                if ( iState == TState.EStateSeenFieldBody || iState == TState.EStateSeenEntryEnd )
                {
                    iConstructedEntry = iWorkInProgressThread;
                    iWorkInProgressThread = null;
                }
            }
        }

        private void ExtractFields( string[] aValues )
        {
            if ( aValues.Length != KExpectedFieldCount )
            {
                throw new Exception( "Corruption detected - field count incorrect" );
            }

            // TID
            iWorkInProgressThread.ThreadId = ParseDecimalValue( aValues[ 0 ] );

            // CHUNK
            iWorkInProgressThread.ChunkHandle = ParseHexValue( aValues[ 1 ] );
            iWorkInProgressThread.ChunkBaseAddress = ParseHexValue( aValues[ 2 ] );

            // HEAP
            iWorkInProgressThread.SizeCurrent = ParseDecimalValue( aValues[ 3 ] );
            iWorkInProgressThread.SizeMin = ParseDecimalValue( aValues[ 4 ] );
            iWorkInProgressThread.SizeMax = ParseDecimalValue( aValues[ 5 ] );

            // FIRST FREE CELL
            iWorkInProgressThread.FirstFreeCellAddress = ParseHexValue( aValues[ 6 ] );
            iWorkInProgressThread.FirstFreeCellLength = ParseDecimalValue( aValues[ 7 ] );

            // OTHER FREE CELL INFO
            iWorkInProgressThread.FreeCellCount = ParseDecimalValue( aValues[ 8 ] );
            iWorkInProgressThread.FreeSpaceTotal = ParseDecimalValue( aValues[ 9 ] );
            iWorkInProgressThread.FreeSpaceSlack = ParseDecimalValue( aValues[ 10 ] );
            iWorkInProgressThread.FreeCellLargest = ParseDecimalValue( aValues[ 11 ] );

            // ALLOC CELL INFO
            iWorkInProgressThread.AllocCellLargest = ParseDecimalValue( aValues[ 12 ] );
            iWorkInProgressThread.AllocCellCount = ParseDecimalValue( aValues[ 13 ] );
            iWorkInProgressThread.AllocSpaceTotal = ParseDecimalValue( aValues[ 15 ] ); // NB: this is item 15, not 14!

            // MISC
            iWorkInProgressThread.MinCellSize = ParseDecimalValue( aValues[ 14 ] );
            iWorkInProgressThread.IsSharedHeap = ( ParseDecimalValue( aValues[ 16 ] ) != 0 );
        }

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

        #region Internal constants
        private const int KExpectedFieldCount = 17;
        #endregion

        #region Internal regular expression
        private static Regex KRegExEntry = new Regex(
              "\\<(?<TagType>/|)ENTRY_(?<Index>[0-9]{4})\\>",
              RegexOptions.Singleline
            | RegexOptions.CultureInvariant
            | RegexOptions.IgnorePatternWhitespace
            | RegexOptions.Compiled
            );
 
        private static Regex KRegExTimestamp = new Regex(
              "\\<TIMESTAMP\\>(?<Timestamp>[0-9].+?)\\</TIMESTAMP\\>",
              RegexOptions.Singleline
            | RegexOptions.CultureInvariant
            | RegexOptions.IgnorePatternWhitespace
            | RegexOptions.Compiled
            );

        // <summary>
        //  Regular expression built for C# on: Wed, Sep 10, 2008, 09:39:13 AM
        //  Using Expresso Version: 3.0.2766, http://www.ultrapico.com
        //  
        //  A description of the regular expression:
        //  
        //  Literal <
        //  [TagName]: A named capture group. [THREAD_NAME|PROCESS_NAME|CHUNK_NAME|FIELDS]
        //      Select from 4 alternatives
        //          THREAD_NAME
        //              THREAD_NAME
        //          PROCESS_NAME
        //              PROCESS_NAME
        //          CHUNK_NAME
        //              CHUNK_NAME
        //          FIELDS
        //              FIELDS
        //  _
        //  [Index]: A named capture group. [[0-9]{4}]
        //      Any character in this class: [0-9], exactly 4 repetitions
        //  Literal >
        //  [Body]: A named capture group. [.+?]
        //      Any character, one or more repetitions, as few as possible
        //  \</
        //      Literal <
        //      /
        //  Match expression but don't capture it. [THREAD_NAME|PROCESS_NAME|CHUNK_NAME|FIELDS]
        //      Select from 4 alternatives
        //          THREAD_NAME
        //              THREAD_NAME
        //          PROCESS_NAME
        //              PROCESS_NAME
        //          CHUNK_NAME
        //              CHUNK_NAME
        //          FIELDS
        //              FIELDS
        //  _
        //  Match expression but don't capture it. [[0-9]{4}]
        //      Any character in this class: [0-9], exactly 4 repetitions
        //  Literal >
        //  
        //
        // </summary>
        private static Regex KRegExTagTypes = new Regex(
              "\\<(?<TagName>THREAD_NAME|PROCESS_NAME|CHUNK_NAME|FIELDS)_(?" +
              "<Index>[0-9]{4})\\>(?<Body>.+?)\\</(?:THREAD_NAME|PROCESS_NA" +
              "ME|CHUNK_NAME|FIELDS)_(?:[0-9]{4})\\>",
            RegexOptions.Singleline
            | RegexOptions.CultureInvariant
            | RegexOptions.IgnorePatternWhitespace
            | RegexOptions.Compiled
            );
        #endregion

        #region Data members
        private readonly CSVThreadParser iParser;
        private TState iState = TState.EStateIdle;
        private int iCurrentEntryId = -1;
        private CSVThread iWorkInProgressThread = null;
        private CSVThread iConstructedEntry = null;
        #endregion
    }
}
