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
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
	public class CSVComparisonEngine
	{
        #region Events
        public enum TEvent
        {
            // Reported as events
            EEventOperationStarted = 0,
            EEventOperationComplete,

            // Reported as percentage progress
            EEventSplittingProgress,
            EEventComparingProgress,

            // Reported as indexed progress
            EEventSplittingMovedToNewFile,
            EEventComparingMovedToNewDataSet,
        }

        public delegate void PercentageProgressHandler( TEvent aEvent, CSVComparisonEngine aSender, int aProgressPercent );
        public event PercentageProgressHandler ePercentageProgressHandler;

        public delegate void IndexedProgressHandler( TEvent aEvent, CSVComparisonEngine aSender, int aCurrentIndex, int aMaxIndex );
        public event IndexedProgressHandler eIndexedProgressHandler;

        public delegate void EventHandler( TEvent aEvent, CSVComparisonEngine aSender );
        public event EventHandler eEventHandler;

        public delegate void ExceptionHandler( string aTitle, string aBody );
        public event ExceptionHandler eExceptionHandler;
        #endregion

        #region Constructors & destructor
        public CSVComparisonEngine( List<string> aSource, string aDestinationPath )
        {
            iSource.AddRange( aSource );
            iDestinationPath = aDestinationPath;
        }
		#endregion

		#region API
        public void CompareAsync()
        {
            iIndex = 0;
            iState = TState.EStateIdle;

            // Start things off...
            OnStateSplitting();
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iDataSets.Count; }
        }
        #endregion
        
        #region Internal state related
        private enum TState
        {
            EStateIdle = 0,
            EStateSplittingLogs,
            EStateComparingAndExportingIndividualDataSets,
            EStateComparingAllDataSets,
            EStateFinished
        }

        private void OnStateSplitting()
        {
            // If we have more files to split, then kick off
            // a new splitter.
            if ( iIndex < iSource.Count )
            {
                string fileName = iSource[ iIndex ];
                if ( eIndexedProgressHandler != null )
                {
                    eIndexedProgressHandler( TEvent.EEventSplittingMovedToNewFile, this, iIndex + 1, iSource.Count );
                }

                // Move to next file
                iIndex++;

                // Set up next worker
                iWorkerSplitter = new CSVWorkerLogSplitter( fileName, iDataSets );
                iWorkerSplitter.iObserver += new AsyncReaderBase.Observer( WorkerSplitter_Observer );
                iWorkerSplitter.SplitAsync();
                //
                ChangeState( TState.EStateSplittingLogs );
            }
            else
            {
                // Done with splitting phase, move on to comparing.
                // We must have at least two sets, or else there's nothing
                // to compare.
                if ( iDataSets.Count <= 1 )
                {
                    if ( eExceptionHandler != null )
                    {
                        eExceptionHandler( "Nothing to compare", "There are insufficient data sets to compare." );
                    }

                    ChangeState( TState.EStateFinished );
                }
                else
                {
                    // Next, find the shared chunks
                    IdentifySharedHeaps();

                    // And now start to export
                    iIndex = 0;
                    if ( iWorkerSplitter != null )
                    {
                        iWorkerSplitter.Dispose();
                        iWorkerSplitter = null;
                    }

                    ChangeState( TState.EStateComparingAndExportingIndividualDataSets );
                    OnStateComparingAndExportingIndividualDataSets();
                }
            }
        }

        private void OnStateComparingAndExportingIndividualDataSets()
        {
            // If there are only two sets to compare, then we generate a single output file and that's it.
            // If we have 3 (or more) data sets, then we will generate three (or more) comparison files, e.g.:
            //
            // [File 1]    0 vs 1
            // [File 2]    1 vs 2
            // 
            // and also
            // 
            // [File 3]    0 vs 2
            //
            // Etc.
            int numberOfComparisons = ( iDataSets.Count > 2 ? iDataSets.Count : 1 );

            if ( iIndex + 1 < iDataSets.Count )
            {
                if ( eIndexedProgressHandler != null )
                {
                    eIndexedProgressHandler( TEvent.EEventComparingMovedToNewDataSet, this, iIndex + 1, numberOfComparisons );
                }

                // Get sets to compare
                CSVDataSet set1 = iDataSets[ iIndex + 0 ];
                CSVDataSet set2 = iDataSets[ iIndex + 1 ];
                
                // Build excel file name
                string excelFileName = BuildExcelFileNameForTwoSetComparison();

                // Since we've now built the filename it's okay to increment the index
                ++iIndex;
 
                // Create and initiate comparison
                iWorkerComparatorTwo = new CSVWorkerTwoDataSetComparator( set1, set2, excelFileName );
                iWorkerComparatorTwo.eObserver += new CSVWorkerTwoDataSetComparator.Observer( WorkerComparatorTwo_Observer );
                iWorkerComparatorTwo.CompareAndSaveAsync();

                ChangeState( TState.EStateComparingAndExportingIndividualDataSets );
            }
            else
            {
                // Special comparison of first vs last set when comparing
                // more than two data sets
                if ( iDataSets.Count > 2 )
                {
                    if ( eIndexedProgressHandler != null )
                    {
                        eIndexedProgressHandler( TEvent.EEventComparingMovedToNewDataSet, this, numberOfComparisons, numberOfComparisons );
                    }

                    // Special value to indicate first vs last comparison
                    iIndex = KSpecialFirstVsLastComparisonIndex;

                    // Get sets to compare
                    CSVDataSet set1 = iDataSets[ 0 ];
                    CSVDataSet set2 = iDataSets[ iDataSets.Count - 1 ];

                    // Build excel file name
                    string excelFileName = BuildExcelFileNameForTwoSetComparison();

                    // Create and initiate comparison
                    if ( iWorkerComparatorTwo != null )
                    {
                        iWorkerComparatorTwo.Dispose();
                        iWorkerComparatorTwo = null;
                    }

                    iWorkerComparatorTwo = new CSVWorkerTwoDataSetComparator( set1, set2, excelFileName );
                    iWorkerComparatorTwo.eObserver += new CSVWorkerTwoDataSetComparator.Observer( WorkerComparatorTwo_Observer );
                    iWorkerComparatorTwo.CompareAndSaveAsync();

                    ChangeState( TState.EStateComparingAndExportingIndividualDataSets );
                }
                else
                {
                    if ( iWorkerComparatorTwo != null )
                    {
                        iWorkerComparatorTwo.Dispose();
                        iWorkerComparatorTwo = null;
                    }
                    //
                    ChangeState( TState.EStateComparingAllDataSets );
                    OnStateComparingAndExportingAllDataSets();
                }
            }
        }

        private void OnStateComparingAndExportingAllDataSets()
        {
            // Create and initiate comparison
            string excelFileName = BuildExcelFileNameForSummarySpreadsheet();
            iWorkerComparatorAll = new CSVWorkerAllDataSetComparator( iDataSets, excelFileName );
            iWorkerComparatorAll.eObserver += new CSVWorkerAllDataSetComparator.Observer( WorkerComparatorAll_Observer );
            iWorkerComparatorAll.CompareAndSaveAsync();

            ChangeState( TState.EStateComparingAllDataSets );
        }
        #endregion

        #region Event handlers
        void WorkerSplitter_Observer( AsyncReaderBase.TEvent aEvent, AsyncReaderBase aSender )
        {
            switch ( aEvent )
            {
            case AsyncReaderBase.TEvent.EReadingStarted:
                break;
            case AsyncReaderBase.TEvent.EReadingProgress:
                if ( ePercentageProgressHandler != null )
                {
                    ePercentageProgressHandler( TEvent.EEventSplittingProgress, this, aSender.Progress );
                }
                break;
            case AsyncReaderBase.TEvent.EReadingComplete:
                OnStateSplitting();
                break;
            }
        }

        void WorkerComparatorTwo_Observer( CSVWorkerTwoDataSetComparator.TEvent aEvent, string aThreadName )
        {
            switch ( aEvent )
            {
            case CSVWorkerTwoDataSetComparator.TEvent.EExportingStarted:
                break;
            case CSVWorkerTwoDataSetComparator.TEvent.EExportingProgress:
                if ( ePercentageProgressHandler != null )
                {
                    ePercentageProgressHandler( TEvent.EEventComparingProgress, this, iWorkerComparatorTwo.Progress );
                }
                break;
            case CSVWorkerTwoDataSetComparator.TEvent.EExportingComplete:
                if ( iIndex >= iDataSets.Count )
                {
                    OnStateComparingAndExportingAllDataSets();
                }
                else
                {
                    OnStateComparingAndExportingIndividualDataSets();
                }
                break;
            }
        }

        void WorkerComparatorAll_Observer( CSVWorkerAllDataSetComparator.TEvent aEvent )
        {
            switch ( aEvent )
            {
            case CSVWorkerAllDataSetComparator.TEvent.EExportingStarted:
                break;
            case CSVWorkerAllDataSetComparator.TEvent.EExportingProgress:
                if ( ePercentageProgressHandler != null )
                {
                    ePercentageProgressHandler( TEvent.EEventComparingProgress, this, iWorkerComparatorAll.Progress );
                }
                break;
            case CSVWorkerAllDataSetComparator.TEvent.EExportingComplete:
                ChangeState( TState.EStateFinished );
                break;
            }
        }
        #endregion

        #region Internal methods
        private void ChangeState( TState aNewState )
        {
            if ( eEventHandler != null )
            {
                if ( iState == TState.EStateIdle )
                {
                    eEventHandler( TEvent.EEventOperationStarted, this );
                }
                else if ( aNewState == TState.EStateFinished )
                {
                    eEventHandler( TEvent.EEventOperationComplete, this );
                }
            }

            iState = aNewState;
        }

        private string BuildExcelFileNameForTwoSetComparison()
        {
            StringBuilder temp = new StringBuilder( iDestinationPath );
            if ( iDestinationPath[ iDestinationPath.Length - 1 ] != Path.DirectorySeparatorChar )
            {
                temp.Append( Path.DirectorySeparatorChar );
            }

            // Special case for first vs last
            if ( iIndex == KSpecialFirstVsLastComparisonIndex )
            {
                temp.AppendFormat( "Comparison ({0:d3} vs {1:d3}).xls", 1, iDataSets.Count );
            }
            else
            {
                temp.AppendFormat( "Comparison ({0:d3} vs {1:d3}).xls", iIndex + 1, iIndex + 2 );
            }

            return temp.ToString();
        }

        private string BuildExcelFileNameForSummarySpreadsheet()
        {
            StringBuilder temp = new StringBuilder( iDestinationPath );
            if ( iDestinationPath[ iDestinationPath.Length - 1 ] != Path.DirectorySeparatorChar )
            {
                temp.Append( Path.DirectorySeparatorChar );
            }
            temp.Append( "Summary.xls" );
            return temp.ToString();
        }

        private int CalculateProgressPercentage( int aCurrent, int aMax )
        {
            float ret = (float) aCurrent / (float) aMax;
            ret *= 100.0f;
            return (int) ret;
        }

        private void IdentifySharedHeaps()
        {
            // MemSpy should (and could) do this, at least eventually, but for now
            // we'll work this out on the PC if MemSpy supplied us with the chunk handles for
            // each thread.
            int count = iDataSets.Count;
            for ( int i = 0; i < count; i++ )
            {
                CSVDataSet dataSet = iDataSets[ i ];
                dataSet.FindSharedHeaps();
            }
        }
        #endregion

        #region Internal constants
        private const int KSpecialFirstVsLastComparisonIndex = int.MaxValue;
        #endregion

        #region Data members
        private readonly string iDestinationPath;
        private List<string> iSource = new List<string>();
        private CSVDataSetCollection iDataSets = new CSVDataSetCollection();
        private int iIndex = 0;
        private TState iState = TState.EStateIdle;
        private CSVWorkerLogSplitter iWorkerSplitter;
        private CSVWorkerTwoDataSetComparator iWorkerComparatorTwo;
        private CSVWorkerAllDataSetComparator iWorkerComparatorAll;
		#endregion
    }
}
