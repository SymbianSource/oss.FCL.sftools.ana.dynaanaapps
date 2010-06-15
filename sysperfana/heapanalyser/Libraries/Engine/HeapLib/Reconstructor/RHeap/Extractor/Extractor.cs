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
using SymbianStructuresLib.CodeSegments;
using SymbianStructuresLib.Debug.Symbols;
using SymbianDebugLib.Engine;
using SymbianDebugLib.PluginAPI.Types;
using SymbianDebugLib.PluginAPI.Types.Symbol;
using SymbianUtils;
using SymbianUtils.Range;
using SymbianUtils.RawItems;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using HeapLib.Relationships;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;

namespace HeapLib.Reconstructor.RHeap.Extractor
{
    internal class RHeapExtractor : AsyncReaderBase
    {
        #region Constructors & destructor
        public RHeapExtractor( DataSource aDataSource, Options aOptions, DbgEngine aDebugEngine, RelationshipInspector aRelationshipInspector, HeapStatistics aStatistics, HeapCellArray aData )
        {
            iData = aData;
            iOptions = aOptions;
            iStatistics = aStatistics;
            iDataSource = aDataSource;
            iDebugEngine = aDebugEngine;
            iState = new ExtractionState( aDataSource );
            iRelationshipInspector = aRelationshipInspector;
            //
            HeapCell.AllocatedCellHeaderSize = AllocatedCellHeaderSize;

            // Must prime these
            iState.NextFreeCellAddress = aDataSource.MetaData.Heap.InfoFree.FreeCellAddress;
            iState.NextFreeCellLength = aDataSource.MetaData.Heap.InfoFree.FreeCellLength;
            //
            if ( iState.NextFreeCellAddress == 0 )
            {
                throw new ArgumentException( "Next free cell information invalid" );
            }

            //iState.DebugEnabled = true;
        }
        #endregion

        #region API
        public void Extract()
        {
            base.AsyncRead();
        }
        #endregion

        #region Properties
        public AddressRange AddressRange
        {
            get { return Statistics.AddressRange; }
        }

        public DbgViewSymbol SymbolView
        {
            get { return iDebugView.Symbols; }
        }

        public Options Options
        {
            get { return iOptions; }
        }

        public HeapStatistics Statistics
        {
            get { return iStatistics; }
        }

        public uint AllocatedCellHeaderSize
        {
            get
            {
                HeapCell.TBuildType buildType = HeapCell.TBuildType.ERelease;
                //
                if ( iDataSource.MetaData.Heap.DebugAllocator )
                {
                    buildType = HeapCell.TBuildType.EDebug;
                }
                //
                uint size = HeapCell.AllocatedCellSizeByBuildType( buildType );
                return size;
            }
        }

        public HeapCellArray Data
        {
            get { return iData; }
        }

        public DataSource SourceData
        {
            get { return iDataSource; }
        }
        #endregion

        #region From AsyncReaderBase
        protected override void HandleReadStarted()
        {
            try
            {
                // Prepare view
                CodeSegDefinitionCollection codeSegs = iDataSource.MetaData.CodeSegments;
                iDebugView = iDebugEngine.CreateView( "Heap Analyser: " + iDataSource.ThreadName, codeSegs );
            }
            finally
            {
                base.HandleReadStarted();
            }
        }

        protected override void HandleReadCompleted()
        {
            base.HandleReadCompleted();

            // Do we have a cell that isn't quite flushed?
            if ( iCurrentHeapCell != null )
            {
                FinaliseCurrentCell();
            }

            // Finished with the debug view now.
            iDebugView.Dispose();
            iDebugView = null;
        }

        protected override void PerformOperation()
        {
            uint baseAddress = iDataSource.MetaData.Heap.HeapBaseAddress;
            byte[] data = iDataSource.MetaData.HeapData.Data;
            long size = data.LongLength;
            iPosition = 0;
            //
            while ( iPosition < size )
            {
                int amountToProcess = (int) Math.Min( KBatchSize, size - iPosition );
                if ( amountToProcess > 0 )
                {
                    // Extract bytes
                    byte[] transientData = new byte[ amountToProcess ];
                    System.Array.Copy( data, iPosition, transientData, 0, amountToProcess );
                    
                    // Add them to queue
                    uint address = (uint) ( baseAddress + iPosition );
                    iWorkingItemQueue.Add( transientData, address );

                    // Process items in queue
                    ExecuteStateLoop();

                    // Move to next address
                    iPosition += amountToProcess;
                }

                // Report progress
                NotifyEvent( TEvent.EReadingProgress );
            }
        }

        protected override long Size
        {
            get { return iDataSource.MetaData.HeapData.Count; }
        }

        protected override long Position
        {
            get { return iPosition; }
        }
        #endregion

        #region Internal state enumeration
        private enum TState
        {
            EGettingHeapCellLength = 0,
            EGettingNestingLevel,
            EGettingNextFreeCellAddress,
            EGettingAllocationNumber,
            EGettingVTable,
            ESkippingToAddress,
            ECapturingRawData
        }
        #endregion

        #region State handlers
        private void ExecuteStateLoop()
        {
            // Now handle the items that were parsed according to the
            // current state
            bool continueProcessing = ( iWorkingItemQueue.Count > 0 );
            while ( continueProcessing )
            {
                switch ( iCurrentState )
                {
                case TState.EGettingHeapCellLength:
                    StateGettingHeapCellLength();
                    break;
                case TState.EGettingNextFreeCellAddress:
                    StateGettingNextFreeCellAddress();
                    break;
                case TState.EGettingNestingLevel:
                    StateGettingNestingLevel();
                    break;
                case TState.EGettingAllocationNumber:
                    StateGettingAllocationNumber();
                    break;
                case TState.EGettingVTable:
                    StateGettingVTable();
                    break;
                case TState.ESkippingToAddress:
                    StateSkippingToAddress();
                    break;
                case TState.ECapturingRawData:
                    StateCapturingRawData();
                    break;
                default:
                    continueProcessing = false;
                    break;
                }

                // If we don't have any items left then we must wait
                // for more data
                continueProcessing = ( iWorkingItemQueue.Count > 0 );
            }
        }

        private void SetNextState( TState aNewState )
        {
            iCurrentState = aNewState;
        }

        private void StateGettingHeapCellLength()
        {
            if ( iCurrentHeapCell != null )
                throw new ArgumentException( "Heap cell should be NULL" );

            RawItem item = iWorkingItemQueue.DequeueHeadItem();

            // Now make a new cell
            iCurrentHeapCell = new HeapCell();
            iCurrentHeapCell.Address = item.Address;
            iCurrentHeapCell.AddRawItemHeader( item );
            //
            iState.NextCellAddress = iCurrentHeapCell.Address + iCurrentHeapCell.Length;
            iState.CurrentAddress = item.Address;
            //
            Debug( "[Cell] 0x" + iState.CurrentAddress.ToString( "x8" ) + ", " + item.Data.ToString() + " bytes long, next cell: 0x" + iState.NextCellAddress.ToString( "x8" ) );
            //
            TState nextState = TState.EGettingVTable;
            if ( iState.IsFreeCellAddress() )
            {
                // FREE cell
                iCurrentHeapCell.Type = HeapCell.TType.EFree;

                // Try to get the next free cell address
                nextState = TState.EGettingNextFreeCellAddress;
            }
            else
            {
                // ALLOCATED cell
                if ( HeapCell.IsDebugAllocator )
                {
                    nextState = TState.EGettingNestingLevel;
                }
            }
            //
            SetNextState( nextState );
        }

        private void StateGettingNextFreeCellAddress()
        {
            if ( iCurrentHeapCell == null )
                throw new ArgumentException( "Heap cell is NULL!" );

            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            iState.NextFreeCellAddress = item.Data;
            if ( iState.NextFreeCellAddress > Statistics.AddressRange.Max )
            {
                iDataSource.AddError( DataSource.TErrorTypes.EErrorTypeFreeCellAddressOutOfBounds );
            }

            iCurrentHeapCell.AddRawItemHeader( item );
            MarkForInspection( item );

            iState.CurrentAddress += RawItem.KSizeOfOneRawItemInBytes;
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - free:        " + item.Data.ToString( "x8" ) );

            // If we're decoding free cell contents, instead
            // we should try to identify the vTable info from
            // what remains of the free cell data
            TState nextState = TState.ECapturingRawData;
            if ( Options.AttemptToDecodeFreeCellContents )
            {
                nextState = TState.EGettingVTable;
            }
            SetNextState( nextState );
        }

        private void StateGettingNestingLevel()
        {
            System.Diagnostics.Debug.Assert( HeapCell.IsDebugAllocator );
            if ( iCurrentHeapCell == null )
                throw new ArgumentException( "Heap cell is NULL!" );
            //
            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            iCurrentHeapCell.AddRawItemHeader( item );
            //
            iState.CurrentAddress += RawItem.KSizeOfOneRawItemInBytes;
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - nexting lev: " + iCurrentHeapCell.NestingLevel );
            //
            SetNextState( TState.EGettingAllocationNumber );
        }

        private void StateGettingAllocationNumber()
        {
            System.Diagnostics.Debug.Assert( HeapCell.IsDebugAllocator );
            if ( iCurrentHeapCell == null )
                throw new ArgumentException( "Heap cell is NULL!" );
            //
            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            iCurrentHeapCell.AddRawItemHeader( item );
            //
            iState.CurrentAddress += 4;
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - alloc num:   " + iCurrentHeapCell.AllocationNumber );
            //
            SetNextState( TState.EGettingVTable );
        }

        private void StateGettingVTable()
        {
            if ( iCurrentHeapCell == null )
                throw new ArgumentException( "Heap cell is NULL!" );

            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            iState.CurrentAddress += RawItem.KSizeOfOneRawItemInBytes;
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );

            // When dealing with allocated cells, then the vtable is always the first 4 bytes
            // of the cell.
            //
            // When dealing with free cells, then this raw data item might be part of the free
            // cell (if the free cell length is > 12 bytes) or then it might be part of the
            // next cell.
            bool isFromNextCell = ( item.Address == iState.NextCellAddress );
            //
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - raw:         " + item.Data.ToString( "x8" ) );
            //
            if ( isFromNextCell )
            {
                // Finalise the cell, i.e. update tracker and store
                // cell to array.
                FinaliseCurrentCell();

                // Push the item back again ready for parings
                iWorkingItemQueue.ReEnqueueItem( item );

                // We were skipping, but we found the first new item.
                // Save the cell as we've now completely processed it.
                SetNextState( TState.EGettingHeapCellLength );
            }
            else
            {
                // Treat this as raw data for this cell
                AddRawItemToCurrentCell( item );

                // Get next item
                SetNextState( TState.ECapturingRawData );
            }
        }

        private void StateSkippingToAddress()
        {
            if ( iCurrentHeapCell != null )
                throw new ArgumentException( "Heap cell should be NULL" );
            if ( iState.NextCellAddress <= 0 )
                throw new ArgumentException( "Start of next cell is <= 0" );

            // Check to see if this item falls within our data range...
            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            bool preserveItem = ( item.Address >= iState.NextCellAddress );
            //
            iState.CurrentAddress += RawItem.KSizeOfOneRawItemInBytes;
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - skiping:     " + item.Data.ToString( "x8" ) );
            //
            if ( preserveItem )
            {
                // We were skipping, but we found the first new item
                SetNextState( TState.EGettingHeapCellLength );

                // Push the item back again ready for parings
                iWorkingItemQueue.ReEnqueueItem( item );
            }
        }

        private void StateCapturingRawData()
        {
            if ( iCurrentHeapCell == null )
                throw new ArgumentException( "Heap cell is NULL!" );
            if ( iState.NextCellAddress <= 0 )
                throw new ArgumentException( "Start of next cell is <= 0" );

            // Check to see if this item falls within our data range...
            RawItem item = iWorkingItemQueue.DequeueHeadItem();
            bool isFromNextCell = ( item.Address == iState.NextCellAddress );
            //
            iState.CurrentAddress += RawItem.KSizeOfOneRawItemInBytes;
            System.Diagnostics.Debug.Assert( iState.CurrentAddress == item.Address );
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - raw:         " + item.Data.ToString( "x8" ) );
            //
            if ( isFromNextCell )
            {
                // Finalise the cell, i.e. update tracker and store
                // cell to array.
                FinaliseCurrentCell();

                // Push the item back again ready for parings
                iWorkingItemQueue.ReEnqueueItem( item );

                // We were skipping, but we found the first new item.
                // Save the cell as we've now completely processed it.
                SetNextState( TState.EGettingHeapCellLength );
            }
            else
            {
                // Treat this as raw data for this cell
                AddRawItemToCurrentCell( item );
            }
        }
        #endregion

        #region Internal methods
        private bool AddressIsWithinHeapBounds( uint aAddress )
        {
            bool inBounds = iStatistics.WithinHeapBounds( aAddress );
            return inBounds;
        }

        private void AddRawItemToCurrentCell( RawItem aItem )
        {
            iCurrentHeapCell.AddRawItem( aItem );
            MarkForInspection( aItem );
        }

        private void MarkForInspection( RawItem aItem )
        {
            if ( !iAlreadyMarkedForInspection )
            {
                // Check this cell later on to see what kind of relationships it has
                // with other cells.
                bool isWithinHeap = AddressIsWithinHeapBounds( aItem.Data );
                if ( isWithinHeap )
                {
                    iRelationshipInspector.InspectLater( iCurrentHeapCell );
                    iAlreadyMarkedForInspection = true;
                }
            }
        }

        private Symbol FindMatchingSymbolAny( uint aAddress )
        {
            return FindMatchingSymbol( aAddress, 0, false );
        }
        
        private Symbol FindMatchingSymbolVTable( uint aAddress, uint aLength )
        {
            return FindMatchingSymbol( aAddress, aLength, true );
        }

        private Symbol FindMatchingSymbol( uint aAddress, uint aLength, bool aMustBeVTable )
        {
            SymbolCollection collection;
            Symbol symbol = iDebugView.Symbols.Lookup( aAddress, out collection );
            //
            if ( symbol != null )
            {
                // If we just hit a "placeholder" symbol, then let's replace it with
                // something more unique so that we can better track statistics for these
                // binaries which we don't have proper symbolics for.
                if ( symbol.IsDefault && aLength > 0 )
                {
                    Symbol temp = Symbol.NewDefault();
                    temp.OffsetAddress = aAddress;
                    temp.Size = aLength;                    // This is a kludge because we can never know 
                    temp.Object = symbol.Object;            // how big the symbol was if we don't have symbolic info
                    symbol = temp;
                    System.Diagnostics.Debug.WriteLine( string.Format( "[IsUnknown]  vTable: 0x{0:x8}, len: {1}", aAddress, aLength ) );
                }
                else if ( aMustBeVTable && !symbol.IsVTable )
                {
                    // We did find a symbol, but it looks like a function or some other address
                    // which is not type-info related. Do not associated with symbol in this situation.
                    System.Diagnostics.Debug.WriteLine( string.Format( "[Not vTable]  vTable: 0x{0:x8}, len: {1}, sym: {2}", aAddress, aLength, symbol.ToString() ) );
                    symbol = null;
                }
            }
            else if ( collection != null )
            {
                System.Diagnostics.Debug.WriteLine( string.Format( "[No symbol]  vTable: 0x{0:x8}, len: {1}, collection: {2}", aAddress, aLength, collection.FileName.FileNameInHost ) );
            }
            //
            return symbol;
        }

        private void FinaliseCurrentCell()
        {
            HeapCell cell = iCurrentHeapCell;

            // Zero out cell. We'll make a new one in
            // StateGettingHeapCellLength()
            iCurrentHeapCell = null;

            // Set index
            cell.Index = (uint) iData.Count;

            // If we have just finished a cell, make sure we update
            // the statistics tracker with that cell's data.
            iData.Add( cell );

            // Set this back to false since we're moving to a new cell
            iAlreadyMarkedForInspection = false;

            // Finish rest of construction
            DoFinaliseCell( cell );
        }

        private void DoFinaliseCell( object aCell )
        {
            HeapCell cell = (HeapCell) aCell;

            // Do symbolic lookups
            Debug( "  {0x" + iState.CurrentAddress.ToString( "x8" ) + "} - vTable:      " + cell.PossibleVTableAddress.ToString( "x8" ) );
            cell.Symbol = FindMatchingSymbolVTable( cell.PossibleVTableAddress, cell.Length );

            // If the MemSpy data includes stack-based function addresses stored instead
            // of nesting level, then we can also try to find a matching symbol.
            if ( SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses )
            {
                cell.Symbol2 = FindMatchingSymbolAny( cell.NestingLevel );
                cell.Symbol3 = FindMatchingSymbolAny( cell.AllocationNumber );
            }

            // Cell is now finished.
            cell.ConstructionComplete( iStatistics );

            // Update stats - do this after finalising the cell
            // as the act of finalisation may result in the
            // cell being tagged as a descriptor. This must be done
            // prior to the stats update, or else we won't treat
            // any cell as a descriptor!
            lock ( iStatistics )
            {
                iStatistics.HandleCell( cell );
            }
        }

        private void Debug( string aMessage )
        {
            System.Diagnostics.Debug.WriteLineIf( iState.DebugEnabled, aMessage );
        }
        #endregion

        #region Internal constants
        private const int KBatchSize = 1024;
        #endregion

        #region Data members
        private readonly DataSource iDataSource;
        private readonly Options iOptions;
        private readonly RelationshipInspector iRelationshipInspector;
        private readonly HeapStatistics iStatistics;
        private readonly HeapCellArray iData;
        private readonly ExtractionState iState;
        private readonly DbgEngine iDebugEngine;
        private DbgEngineView iDebugView = null;
        private TState iCurrentState;
        private HeapCell iCurrentHeapCell;
        private RawItemQueue iWorkingItemQueue = new RawItemQueue();
        private long iPosition = 0;
        private bool iAlreadyMarkedForInspection = false;
        #endregion
    }
}