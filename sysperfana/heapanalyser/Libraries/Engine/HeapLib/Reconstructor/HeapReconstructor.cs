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
using SymbianDebugLib.Engine;
using SymbianUtils;
using SymbianUtils.Range;
using SymbianUtils.RawItems;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using HeapLib.Relationships;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.RHeap.Extractor;

namespace HeapLib.Reconstructor
{
	public sealed class HeapReconstructor
	{
        #region Events
        public enum TEvent
        {
            EReconstructingStarted = 0,
            EReconstructingProgress,
            EReconstructingComplete
        }

        public delegate void Observer( TEvent aEvent, HeapReconstructor aReconstructor );
        public event Observer iObserver;
        public delegate void ExceptionHandler( string aTitle, string aBody );
        public event ExceptionHandler iExceptionHandler;
        #endregion

        #region Constructors & destructor
		public HeapReconstructor( DataSource aDataSource, Options aOptions, DbgEngine aDebugEngine )
		{
            iRelationshipInspector = new RelationshipInspector( iData, iStatistics );
            iRelationshipInspector.iObserver += new AsyncReaderBase.Observer( RelationshipInspector_Observer );
            //
            iExtractor = new RHeapExtractor( aDataSource, aOptions, aDebugEngine, iRelationshipInspector, iStatistics, iData );
            iExtractor.iObserver += new AsyncReaderBase.Observer( Extractor_Observer );
            iExtractor.iExceptionHandler += new AsyncReaderBase.ExceptionHandler( Extractor_ExceptionHandler );
		}
        #endregion

		#region API
        public void Reconstruct()
        {
            iState = TState.EStateExtractingHeapObjects;
            //
            iData.Clear();
            //
            iStatistics.Clear();
            iStatistics.HeapBaseAddress = SourceData.MetaData.Heap.HeapBaseAddress;
            iStatistics.HeapSize = SourceData.MetaData.Heap.ChunkSize;
            if ( iStatistics.HeapSize == 0 || iStatistics.HeapBaseAddress == 0 )
            {
                throw new ArgumentException( "Heap size or Heap base address not initialised" );
            }
            //
            iExtractor.Extract();
        }
        #endregion

		#region Properties
        public AddressRange AddressRange
        {
            get { return Statistics.AddressRange; }
        }

		public Options Options
		{
            get { return iExtractor.Options; }
        }

		public HeapStatistics Statistics
		{
			get { return iStatistics; }
		}

		public uint AllocatedCellHeaderSize
		{
			get { return iExtractor.AllocatedCellHeaderSize; }
		}

		public HeapCellArray Data
		{
			get { return iData; }
		}

		public DataSource SourceData
		{
            get { return iExtractor.SourceData; }
		}

        public bool IsDebugAllocator
        {
            get { return SourceData.MetaData.Heap.DebugAllocator; }
        }

        public int Progress
        {
            get
            {
                int progress = 0;
                //
                progress += iExtractor.Progress;
                progress += iRelationshipInspector.Progress;
                //
                return progress / 2;
            }
        }
        #endregion

        #region From System.Object
        public string ToString( bool aShowAllEntries )
        {
            StringBuilder text = new StringBuilder();
            //
            int count = Data.Count;
            for ( int i = 0; i < count; i++ )
            {
                HeapCell element = Data[ i ];
                if ( aShowAllEntries || ( !aShowAllEntries && element.Symbol != null ) )
                {
                    string line = element.ToString() + Environment.NewLine;
                    text.Append( line );
                }
            }
            //
            return text.ToString();
        }
        #endregion

		#region Internal state enumeration
		private enum TState
		{
            EStateExtractingHeapObjects = 0,
            EStateInspectingCellRelationships,
            EStateUpdatingStatistics
		}
		#endregion

        #region Extractor observer
        private void Extractor_Observer( AsyncReaderBase.TEvent aEvent, AsyncReaderBase aSender )
        {
            if ( iObserver != null )
            {
                if ( aEvent == AsyncReaderBase.TEvent.EReadingStarted )
                {
                    iObserver( TEvent.EReconstructingStarted, this );
                }
                else if ( aEvent == AsyncReaderBase.TEvent.EReadingComplete )
                {
                    iState = TState.EStateInspectingCellRelationships;
                    iRelationshipInspector.Inspect();
                    iObserver( TEvent.EReconstructingProgress, this );
                }
                else
                {
                    iObserver( TEvent.EReconstructingProgress, this );
                }
            }
        }

        private void Extractor_ExceptionHandler( Exception aException, AsyncReaderBase aSender )
        {
            System.Diagnostics.Debug.WriteLine( "EXTRACTOR EXCEPTION: " + aException.Message );
            if ( iExceptionHandler != null )
            {
                iExceptionHandler( "Heap Extraction Exception", aException.Message + System.Environment.NewLine + aException.StackTrace );
            }
        }
        #endregion

        #region Relationship Inspector observer
        private void RelationshipInspector_Observer( AsyncReaderBase.TEvent aEvent, AsyncReaderBase aSender )
        {
            if ( iObserver != null )
            {
                if ( aEvent == AsyncReaderBase.TEvent.EReadingStarted || aEvent == AsyncReaderBase.TEvent.EReadingProgress )
                {
                    iObserver( TEvent.EReconstructingProgress, this );
                }
                else if ( aEvent == AsyncReaderBase.TEvent.EReadingComplete )
                {
                    iState = TState.EStateUpdatingStatistics;
                    iStatistics.Finalise( iData );
                    iObserver( TEvent.EReconstructingComplete, this );
                }
            }
        }
        #endregion

		#region Data members
        private TState iState;
        private HeapStatistics iStatistics = new HeapStatistics();
        private HeapCellArray iData = new HeapCellArray();
        private readonly RHeapExtractor iExtractor;
		private readonly RelationshipInspector iRelationshipInspector;
        #endregion
	}
}