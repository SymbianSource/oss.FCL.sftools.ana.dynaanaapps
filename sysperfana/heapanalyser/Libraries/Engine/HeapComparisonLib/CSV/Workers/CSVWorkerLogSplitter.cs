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
using System.IO;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
    internal class CSVWorkerLogSplitter : AsyncTextFileReader
    {
        #region Constructors
        public CSVWorkerLogSplitter( string aFileName, CSVDataSetCollection aStorage )
            : base( aFileName )
		{
            iStorage = aStorage;
        }
        #endregion

        #region API
        public void SplitAsync()
        {
            base.AsyncRead();
        }
        #endregion

        #region Properties
		#endregion

        #region From AsyncTextFileReader
        protected override void HandleFilteredLine( string aLine )
        {
            string originalLine = aLine;
            iCleaner.CleanLine( ref aLine );

            // Strip prefix
            if ( CheckForKnownElement( ref aLine, KRDebugPrefixes ) )
            {
            }

            // Main handler
            if ( CheckForKnownElement( ref aLine, KMarkerEnd ) )
            {
                // Finished an item - forced flush
                if ( iCurrentDataSet != null )
                {
                    iStorage.Add( iCurrentDataSet );
                }

                iCurrentDataSet = new CSVDataSet( base.FileName, base.LineNumber );
                iParser.CurrentDataSet = iCurrentDataSet;
            }
            else if ( CheckForKnownElement( ref aLine, KMarkerStart ) )
            {
                // Start of item - make a new entry
                iCurrentDataSet = new CSVDataSet( base.FileName, base.LineNumber );
                iParser.CurrentDataSet = iCurrentDataSet;
            }
            else
            {
                // Is it a valid line?
                CSVThread threadEntry = iParser.ParseLine( aLine );
                if ( threadEntry != null )
                {
                    if ( iCurrentDataSet != null )
                    {
                        iCurrentDataSet.Add( threadEntry );
                    }
                }
            }
        }

        protected override void HandleReadCompleted()
        {
            try
            {
                base.HandleReadCompleted();
            }
            finally
            {
                // Save any WIP entry
                if ( iCurrentDataSet != null && iCurrentDataSet.Count > 0 && !iStorage.Contains( iCurrentDataSet ) )
                {
                    iStorage.Add( iCurrentDataSet );
                    iCurrentDataSet = null;
                }
                
                // Remove any collections with no entries
                iStorage.RemoveEmptyDataSets();

                // Order by timestamp
                iStorage.SortByTimestamp();
            }
        }
        #endregion

        #region Internal methods
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

        #region Interanl constants
        private static readonly string[] KRDebugPrefixes = new string[] { "[Heap Summary] ", "CSV - " };
        private static readonly string[] KMarkerEnd = new string[] { "</MEMSPY_HEAP_CSV>", "</CSV_HEAP_SUMMARY>", "<= End Heap Summary =>", "<= HEAP SUMMARY END =>" };
        private static readonly string[] KMarkerStart = new string[] { "<MEMSPY_HEAP_CSV>", "<CSV_HEAP_SUMMARY>", "<= HEAP SUMMARY START =>", "Thread, Chunk, Base Addr, Size, Min, Max, 1st Free Addr, 1st Free Len, Free Count, Total Free Space" };
        #endregion

        #region Data members
        private readonly CSVDataSetCollection iStorage;
        private CSVDataSet iCurrentDataSet = null;
        private TracePrefixAnalyser iCleaner = new TracePrefixAnalyser();
        private CSVThreadParser iParser = new CSVThreadParser();
        #endregion
    }
}
