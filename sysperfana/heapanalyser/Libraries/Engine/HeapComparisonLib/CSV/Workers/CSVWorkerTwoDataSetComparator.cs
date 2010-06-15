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
using System.Collections.Generic;
using System.Threading;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
    internal class CSVWorkerTwoDataSetComparator : DisposableObject
	{
		#region Observer related
		public enum TEvent
		{
			EExportingStarted = 0,
			EExportingProgress,
			EExportingComplete
		}

        public delegate void Observer( TEvent aEvent, string aFullName );
		public event Observer eObserver;
		#endregion

		#region Constructors & destructor
        public CSVWorkerTwoDataSetComparator( CSVDataSet aCol1, CSVDataSet aCol2, string aOutputFileName )
		{
            // Make copies because we actually dequeue items from the list as we processed them
            // and we must not alter the originals or else we'll break the subsequent comparison(s)
			iCol1 = new CSVDataSet( aCol1 );
			iCol2 = new CSVDataSet( aCol2 );
            iExcelExporter = new CSVExcelExporterTwoDataSets( aOutputFileName, aCol1.FileName, aCol2.FileName );
		}
		#endregion

		#region API
		public void CompareAndSaveAsync()
		{
            iWorkerThread = new System.Threading.Thread( new ThreadStart( ThreadFunction ) );
            iWorkerThread.Start();
		}
		#endregion

		#region Properties
		public int Progress
		{
			get
            {
                int count = 0;
                int index = 0;
                //
                lock ( this )
                {
                    index = iIndex;
                    count = iPairings.Count;
                }

                float ret = (float) index / (float) count;
                ret *= 100.0f;
                return (int) ret;
            }
		}
		#endregion

        #region From DisposableObject
        protected override void CleanupManagedResources()
        {
            try
            {
                if ( iExcelExporter != null )
                {
                    iExcelExporter.Dispose();
                    iExcelExporter = null;
                }
            }
            finally
            {
                base.CleanupManagedResources();
            }
        }
        #endregion

        #region Thread function
        private void ThreadFunction()
        {
            // Report started
            if ( eObserver != null )
            {
                eObserver( TEvent.EExportingStarted, null );
            }

            // Create our list of sorted comparison pairs.
            BuildPairs();

            // Now compare them and export to excel
            CompareAndExport();

            // Dispose excel object - closes file
            iExcelExporter.Dispose();
            iExcelExporter = null;

            // Report finished
            if ( eObserver != null )
            {
                eObserver( TEvent.EExportingComplete, null );
            }
        }
		#endregion

		#region Internal methods
        private void BuildPairs()
        {
            // Treat the first collection as the master list. 
            int count = iCol1.Count;
            for ( int i = count - 1; i >= 0; i-- )
            {
                CSVThread master = iCol1[ i ];

                // Remove primary entry also
                iCol1.Remove( master );

                // Create or locate secondary
                CSVThread secondary = FindAppropriateSecondary( master, iCol2 );

                // Create a pairing
                iPairings.Add( master.FullName, new CSVThreadPair( master, secondary ) );
            }

            // Now do the same but for the secondary list this time. It should
            // be largely empty. The only entries that will remain are threads
            // which don't exist in the primary list.
            count = iCol2.Count;
            for ( int i = count - 1; i >= 0; i-- )
            {
                CSVThread master = iCol2[ i ];

                // Remove primary entry also
                iCol2.Remove( master );

                // Create or locate secondary
                CSVThread secondary = FindAppropriateSecondary( master, iCol1 );

                // Create a pairing - but this time the master is the 2nd entry 
                // in the pair. This ensures we keep the threads in the correct
                // columns in the final excel spreadsheet.
                iPairings.Add( master.FullName, new CSVThreadPair( secondary, master ) );
            }
        }

        private CSVThread FindAppropriateSecondary( CSVThread aMaster, CSVDataSet aSecondaryCol )
        {
            System.Diagnostics.Debug.Assert( aMaster.FullName != string.Empty );

            // We must try to find the corresponding secondary thread with which to compare
            // aMaster.
            CSVThread secondary = aSecondaryCol[ aMaster.FullName ];
            if ( secondary == null )
            {
                // Secondary collection doesn't contain this thread, so make a default entry.
                secondary = CSVThread.NewDefault( aMaster.FullName );
            }
            else
            {
                aSecondaryCol.Remove( secondary );
            }

            return secondary;
        }

        private void CompareAndExport()
        {
            lock ( this )
            {
                iIndex = 0;
            }

            foreach ( KeyValuePair<string, CSVThreadPair> kvp in iPairings )
            {
                CSVThreadPair pair = kvp.Value;

                // Report progress to any observers
                if ( eObserver != null )
                {
                    eObserver( TEvent.EExportingProgress, kvp.Key );
                }

                lock ( this )
                {
                    ++iIndex;
                }

                CompareAndExport( pair.Master, pair.Secondary );
            }
        }

        private void CompareAndExport( CSVThread aMaster, CSVThread aSecondary )
        {
            iExcelExporter.CompareThread( aMaster, aSecondary );
        }
		#endregion

		#region Data members
        private readonly CSVDataSet iCol1;
        private readonly CSVDataSet iCol2;
        private CSVExcelExporterTwoDataSets iExcelExporter = null;
        private SortedList<string, CSVThreadPair> iPairings = new SortedList< string, CSVThreadPair>();
        private System.Threading.Thread iWorkerThread;
        private int iIndex = 0;
		#endregion
	}
}
