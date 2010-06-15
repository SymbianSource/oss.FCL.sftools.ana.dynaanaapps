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
    internal class CSVWorkerAllDataSetComparator : DisposableObject
	{
		#region Observer related
		public enum TEvent
		{
			EExportingStarted = 0,
			EExportingProgress,
			EExportingComplete
		}

        public delegate void Observer( TEvent aEvent );
		public event Observer eObserver;
		#endregion

		#region Constructors & destructor
        public CSVWorkerAllDataSetComparator( CSVDataSetCollection aDataSets, string aOutputFileName )
		{
            // Create our list of sorted comparison pairs.
            iDataSets = aDataSets;
            iUniqueThreadDetails = IdentifyAllThreadNames();
            iExcelExporter = new CSVExcelExporterAllDataSets( aOutputFileName, iUniqueThreadDetails, aDataSets.Count );
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
                    count = iDataSets.Count;
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
                eObserver( TEvent.EExportingStarted );
            }

            // Now compare them and export to excel
            CompareAndExport();

            // Dispose excel object - closes file
            iExcelExporter.Dispose();
            iExcelExporter = null;

            // Report finished
            if ( eObserver != null )
            {
                eObserver( TEvent.EExportingComplete );
            }
        }
		#endregion

		#region Internal methods
        private SortedDictionary<long, string> IdentifyAllThreadNames()
        {
            // Get a list of unique thread ids and their full names
            SortedDictionary<long, string> list = new SortedDictionary<long, string>();
            int count = iDataSets.Count;
            for ( int i = 0; i<count; i++ )
            {
                CSVDataSet set = iDataSets[ i ];
                AddThreadsToList( set, list );
            }

            return list;
        }

        private void AddThreadsToList( CSVDataSet aSet, SortedDictionary<long, string> aList )
        {
            int count = aSet.Count;
            for ( int i = count - 1; i >= 0; i-- )
            {
                CSVThread master = aSet[ i ];
                if ( aList.ContainsKey( master.ThreadId ) == false )
                {
                    aList.Add( master.ThreadId, master.FullName );
                }
            }
        }

        private void CompareAndExport()
        {
            lock ( this )
            {
                iIndex = 0;
            }

            for( ; iIndex<iDataSets.Count; )
            {
                CSVDataSet set = iDataSets[ iIndex ];

                iExcelExporter.Export( set );

                // Report progress to any observers
                if ( eObserver != null )
                {
                    eObserver( TEvent.EExportingProgress );
                }

                lock ( this )
                {
                    ++iIndex;
                }
            }
        }
		#endregion

		#region Data members
        private readonly CSVDataSetCollection iDataSets;
        private readonly SortedDictionary<long, string> iUniqueThreadDetails;
        private CSVExcelExporterAllDataSets iExcelExporter = null;
        private System.Threading.Thread iWorkerThread;
        private int iIndex = 0;
		#endregion
	}
}
