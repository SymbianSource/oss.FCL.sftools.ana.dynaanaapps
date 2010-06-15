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
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources.Analyser.Readers;
using HeapLib.Reconstructor.DataSources.Analyser.Extractor;
using HeapLib.Reconstructor.DataSources.Analyser.Interpreter;

namespace HeapLib.Reconstructor.DataSources.Analyser
{
    public class DataSourceAnalyser
    {
        #region Events
        public enum TEvent
        {
            EReadingStarted = 0,
            EReadingProgress,
            EReadingComplete
        }

        public delegate void Observer( TEvent aEvent, DataSourceAnalyser aSender );
        public event Observer iObserver;

        public delegate void ExceptionHandler( Exception aException, DataSourceAnalyser aSender );
        public event ExceptionHandler iExceptionHandler;
        #endregion

        #region Constructors & destructor
        public DataSourceAnalyser( string[] aLines )
        {
            iReader = new ReaderLines( this, aLines );
            iReader.iObserver += new AsyncReaderBase.Observer( Reader_Observer );
            iReader.iExceptionHandler += new AsyncReaderBase.ExceptionHandler( Reader_ExceptionHandler );
        }

        public DataSourceAnalyser( string aFileName )
        {
            iReader = new ReaderFile( this, aFileName );
            iReader.iObserver += new AsyncReaderBase.Observer( Reader_Observer );
            iReader.iExceptionHandler += new AsyncReaderBase.ExceptionHandler( Reader_ExceptionHandler );
        }
        #endregion

        #region API
        public void Analyse()
        {
            if ( iReader is ReaderFile )
            {
                ReaderFile reader = (ReaderFile) iReader;
                reader.StartRead();
            }
            else if ( iReader is ReaderLines )
            {
                ReaderLines reader = (ReaderLines) iReader;
                reader.StartRead();
            }
        }
        #endregion

        #region Properties
        public bool IsReady
        {
            get { return iReader.IsReady; }
        }

        public int Progress
        {
            get { return iReader.Progress; }
        }

        public DataSourceCollection DataSources
        {
            get { return iDataSources; }
        }
        #endregion

        #region Reader call back method
        internal void HandleFilteredLine( string aLine, long aLineNumber )
        {
            // The four types of data we allow.
            //
            // 1) Text data from a trace file
            //    [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HEAP INFO FOR THREAD 'EComServer::!ecomserver'
            //
            // 2) Text data from a MemSpy heap dump file
            //    HEAP INFO FOR THREAD 'ecomserver::!ecomserver'
            //
            // 3) Binary data from a trace file:
            //    [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]<BinHeapData:00000131:00000130:000000000001346e>
            //    [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]00000000: be d0 be d0 ba da ba da 00 00 00 d0 01 00 00 00 ................
            //    [14:39:24.344] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:[BinHeap:00000131]00000010: 6e 34 01 00 82 00 00 00 83 00 00 00 01 00 00 00 n4..............
            //
            // 4) Binary data from a MemSpy heap dump file
            //    <BinHeapData:00000114:00000113:0000000000001234>
            //    000ae8cc: 00 00 00 00 00 00 00 00 9d 2a 17 00 24 00 00 00

            // First, try running the line past any existing extractors
            bool handled = false;
            foreach ( Extractor.Extractor ext in iActiveExtractors )
            {
                if ( ext.ExtractFrom( aLine, false ) )
                {
                    handled = true;
                    break;
                }
            }
            //
            if ( !handled )
            {
                // Try to create a new extractor that can handle this type of line
                Extractor.Extractor extractor = iExtractorFactory.FindSuitableExtractor( aLine );
                if ( extractor != null )
                {
                    InterpreterBase interpreter = iInterpreterFactory.FindSuitableInterpreter( extractor );

                    if ( interpreter != null )
                    {
                        // Link up extractor and interpreter with a new data source
                        string dataSourceFileName = GetSourceFileName();
                        DataSource dataSource = new DataSource( dataSourceFileName, aLineNumber );
                        interpreter.DataSource = dataSource;
                        extractor.Interpreter = interpreter;

                        // We should also initialise the interpreter now that we've
                        // prepared the data source
                        interpreter.PrepareToStart( extractor );

                        // We must fire the extractor
                        extractor.ExtractFrom( aLine, true );

                        // Save the extractor so that we can ask it to handle future lines
                        AddExtractor( extractor );

                        // Save the data source
                        iDataSources.Add( dataSource );
                    }
                }
            }
        }
        #endregion

        #region Internal methods
        private void ReadingComplete()
        {
            RemoveEmptySources();

            // Check if the sources that are left were all completed, and if not
            // update their error flags.
            foreach ( DataSource source in iDataSources )
            {
                Elements.MetaData metaData = source.MetaData;
                if ( metaData.IsDataComplete == false )
                {
                    source.AddError( DataSource.TErrorTypes.EErrorTypeDataMissing );
                }
            }
        }

        private string GetSourceFileName()
        {
            string fileName = "RawHeapData.txt";
            if ( iReader is ReaderFile )
            {
                fileName = ( (ReaderFile) iReader ).FileName;
            }
            return fileName;
        }

        private void RemoveEmptySources()
        {
            iDataSources.RemoveEmptySources();
        }

        private void AddExtractor( Extractor.Extractor aExtractor )
        {
            // Check whether we already have an extractor for this thread.
            string threadName = aExtractor.Interpreter.DataSource.ThreadName;
            
            // If an entry with the same thread name already exists, then we
            // remove it, and replace it with a new entry
            Extractor.Extractor existingEntry = ExtractorByThread( threadName );
            if ( existingEntry != null )
            {
                iActiveExtractors.Remove( existingEntry );
            }

            // Save new extractor for the thread
            iActiveExtractors.Add( aExtractor );
        }

        private Extractor.Extractor ExtractorByThread( string aName )
        {
            Predicate<Extractor.Extractor> findByNamePredicate = delegate( Extractor.Extractor aExtractor )
            {
                string entryName = aExtractor.Interpreter.DataSource.ThreadName;
                return ( entryName == aName );
            };

            Extractor.Extractor ret = iActiveExtractors.Find( findByNamePredicate );
            return ret;
        }
        #endregion

        #region From AsyncReaderBase
        void Reader_ExceptionHandler( Exception aException, AsyncReaderBase aSender )
        {
            if ( iExceptionHandler != null )
            {
                iExceptionHandler( aException, this );
            }
        }

        void Reader_Observer( AsyncReaderBase.TEvent aEvent, AsyncReaderBase aSender )
        {
            if ( iObserver != null )
            {
                switch ( aEvent )
                {
                case AsyncReaderBase.TEvent.EReadingStarted:
                    iObserver( TEvent.EReadingStarted, this );
                    break;
                case AsyncReaderBase.TEvent.EReadingProgress:
                    iObserver( TEvent.EReadingProgress, this );
                    break;
                case AsyncReaderBase.TEvent.EReadingComplete:
                    ReadingComplete();
                    iObserver( TEvent.EReadingComplete, this );
                    break;
                default:
                    break;
                }
            }
        }
        #endregion

        #region Data members
        private readonly AsyncTextReader iReader;
        private DataSourceCollection iDataSources = new DataSourceCollection();
        private ExtractorFactory iExtractorFactory = new ExtractorFactory();
        private InterpreterFactory iInterpreterFactory = new InterpreterFactory();
        private List<Extractor.Extractor> iActiveExtractors = new List<HeapLib.Reconstructor.DataSources.Analyser.Extractor.Extractor>();
        #endregion
    }
}
