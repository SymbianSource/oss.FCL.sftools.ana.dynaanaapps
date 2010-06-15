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
using System.Collections;
using System.Collections.Generic;
using MemAnalysisLib.MemoryOperations.Class;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Operations;
using MemAnalysisLib.Interfaces;

namespace MemAnalysisLib
{
	public class MemObjStatisticalCollection
	{
		#region Constructor & destructor
		public MemObjStatisticalCollection()
		{
		}
		#endregion

		#region API
		public void Add( MemOpBase aItem )
		{
			iItems.Add( aItem );
		}

		public bool Remove( MemOpBase aItem )
		{
			int index;
			bool ret = false;
			//
            if ( ItemByAddressAndOperationIndex( aItem.CellAddress, aItem.OperationIndex, aItem.Class, out index ) != null )
			{
				iItems.RemoveAt( index );
				ret = true;
			}
			//
			return ret;
		}
		#endregion

		#region Properties
		public int Count
		{
			get { return iItems.Count; }
		}

		public MemOpBase this[int aIndex]
		{
			get
			{
				MemOpBase item = iItems[aIndex];
				return item;
			}
		}

		public long AllocationCount
		{
			get
			{
				long ret = 0;
				//
                foreach( MemOpBase op in iItems )
                {
                    if ( op.IsAllocationType )
                    {
                        ++ret;
                    }
				}
				//
				return ret;
			}
		}

		public long DeallocationCount
		{
			get
			{
				long ret = 0;
				//
                foreach ( MemOpBase op in iItems )
                {
                    if ( op.Class == TClass.EDeallocation )
                    {
                        ret++;
                    }
                }
                //
				return ret;
			}
		}

        public long TotalAmountOfAllocatedMemory
		{
			get
			{
				long ret = 0;
				//
                foreach ( MemOpBase op in iItems )
                {
                    if ( op.IsAllocationType )
                    {
                        ret += op.CellSize;
                    }
                }
				//
				return ret;
			}
		}

        public long TotalAmountOfDeallocatedMemory
		{
			get
			{
                long ret = 0;
                //
                foreach ( MemOpBase op in iItems )
                {
                    if ( op.IsDeallocationType )
                    {
                        ret += op.CellSize;
                    }
                }
                //
                return ret;
			}
		}

        public long TotalMemoryAllocatedButNotFreed
		{
			get
			{
                long ret = 0;
                //
                foreach ( MemOpBase op in iItems )
                {
                    // Allocated cells without links back to the deleted cells
                    // must be orphans.
                    if ( op.Link == null )
                    {
                        if ( op.Class == TClass.EAllocation )
                        {
                            ret += op.CellSize;
                        }
                        else if ( op.Class == TClass.EReallocation )
                        {
                            ret += op.CellSize;
                        }
                    }
                }
                //
                return ret;
			}
		}
		#endregion

		#region Internal methods
		public MemOpBase ItemByAddressAndOperationIndex( uint aCellAddress, int aOperationIndex, TClass aClass, out int aIndex )
		{
            aIndex = -1;
            MemOpBase ret = null;
            //
			for( int i = iItems.Count-1; i>=0; i-- )
			{
				MemOpBase item = iItems[ i ];
                //
				if	( item.CellAddress == aCellAddress && item.OperationIndex == aOperationIndex )
				{
                    if ( aClass == TClass.ENotApplicable )
                    {
                        aIndex = i;
                        ret = item;
                        break;
                    }
                    else if ( item.Class == aClass )
                    {
                        aIndex = i;
                        ret = item;
                        break;
                    }
				}
			}
			//
			return ret;
		}
		#endregion

		#region Data members
        private List<MemOpBase> iItems = new List<MemOpBase>( 50 );
		#endregion
	}

    public class MemObjStatisticalData : CollectionManager
	{
		#region Constructor & destructor
		public MemObjStatisticalData()
		{
			iCollections.Add( iNullSymbolCollection );
		}
		#endregion

		#region API
		public void Add( MemOpBase aItem, bool aDiscardAllocAndFreedMatchingCells )
		{
            /*
            if ( aItem.CellAddress == 0xc8036684 )
            {
                int x = 0;
                x++;
                MemOpAllocation ob = (MemOpAllocation) aItem;
                if	( ob.LinkRegisterAddress == 0x80874729 )
                {
                    int x = 0;
                    x++;
                }
            }
            */

            
            int allItemCount = iAllItems.Count;

            // Check for chained operations - we just treat it as a high level op in that case.
            if  ( aItem.Function.ChainedFunction != null && allItemCount > 0 )
            {
                MemOpBase lastOp = PriorOperationByAllocationNumber( aItem.AllocationNumber );
                if ( lastOp != null &&
                     lastOp.AllocationNumber == aItem.AllocationNumber &&
                     lastOp.CellAddress == aItem.CellAddress && 
                     lastOp.Class == aItem.Class &&
                     lastOp.Function.ToString() == aItem.Function.ChainedFunction.ToString() )
                {
                    // The current operation, replaces the prior one.
                    RemoveFromCollection( lastOp );
                    RemovePriorOperation( lastOp );
                    allItemCount = iAllItems.Count;

                    // Also, we must reset any linkage that the prior operation may have
                    // created when it was added.
                    if ( lastOp.Link != null )
                    {
                        lastOp.Link.Link = null;
                    }
                }
            }

			// If the item is a de-allocation, hunt backwards through the allocation
			// list until we find the allocating cell. Then setup their two-way relationship
            bool saveItem = true;
            if ( aItem.IsDeallocationType )
			{
                MemOpBase lastOp = PriorOperationByAllocationNumber( aItem.AllocationNumber );
                if ( aItem.Function.ChainedFunction != null && lastOp != null && lastOp.Class == TClass.EDeallocation )
                {
                    if ( lastOp.CellAddress == aItem.CellAddress &&
                         lastOp.Function.ToString() == aItem.Function.ChainedFunction.ToString() )
                    {
                        if ( aDiscardAllocAndFreedMatchingCells )
                        {
                            // This is a chained delete operation, e.g:
                            //
                            // [KMEM] OKF     - C: 0xc802776c, HS:    80064, HCS:    85896, LR: 0x800c98c4, AS:      316, AN:      314, VT: 0x800d09c8
                            // [KMEM] OD      - C: 0xc802776c, HS:    80064, HCS:    85896, LR: 0x800a2ea8, AS:      316, AN:      314, VT: 0x800d09c8
                            // [KMEM] ODBD    - C: 0xc802776c, HS:    80064, HCS:    85896, LR: 0x800adec8, AS:      316, AN:      314, VT: 0x800d0000
                            //
                            // and we're handling OD or ODBD after already having processed OKF. In that case, we've already thrown away the original
                            // alloc, and there's nothing left for us to do here.
                            saveItem = false;
                        }
                        else
                        {
                            // We need to replace the OKF/OD operation with the OD/ODBD respectively. In order
                            // for the loop below to process the item, we must re-nullify the link
                            MemOpBase originalAllocationOp = lastOp.Link;
                            originalAllocationOp.Link = null;
                        }
                    }
                }

                // If the item is a deallocation, but we can't find any matching alloc
                // then just ignore the deallocation entirely.
                //
                // We'll assume that its an isolated dealloc, but if we find a matching
                // alloc we'll toggle this back again to preseve the item.
                saveItem = false;
                //
                for ( int i = allItemCount - 1; i >= 0; i-- )
				{
					MemOpBase item = iAllItems[i];
					//
					if	( item.Link == null && item.CellAddress == aItem.CellAddress && item.IsAllocationType != aItem.IsAllocationType )
					{
						// The item should be the allocation that this de-alloc is associated
						// with..
						if	( aDiscardAllocAndFreedMatchingCells )
						{
							// Can ignore both the allocation and deallocation.
							RemoveFromCollection( item );
							iAllItems.RemoveAt( i );
							
                            // Don't save the delete - we are ignoring matching alloc & frees
                            break;
						}
						else
						{
							item.Link = aItem;
							aItem.Link = item;
                            saveItem = true;
							break;
						}
					}
				}
			}
	
			// Add the item to our master list...
            if ( saveItem )
            {

                // Locate the corresponding collection and also add the item there too
                MemOpAllocation searchObject = null;
                if ( aItem.IsAllocationType )
                {
                    searchObject = (MemOpAllocation) aItem;
                }
                else
                {
                    // Try to base the search object on the link item (if we have one..)
                    if ( aItem.Link != null )
                    {
                        searchObject = (MemOpAllocation) aItem.Link;
                    }
                }

                if ( searchObject != null )
                {
                    if ( searchObject.LinkRegisterSymbol != null )
                    {
                        MemObjStatisticalCollection collection = CollectionBySymbol( searchObject );
                        if ( collection == null )
                        {
                            // Make a new collection
                            collection = new MemObjStatisticalCollection();
                            iCollections.Add( collection );
                        }
                        collection.Add( aItem );
                    }
                    else
                    {
                        // Use the null symbol collection
                        iNullSymbolCollection.Add( aItem );
                    }
                }

                AddNewPriorOperation( aItem );
            }
		}

		public void Add( MemOpReallocation aItem )
		{
            aItem.Function.Process( aItem, this );

#warning This code is broken (but is disabled)
           
			/*
             * The main issue is that it doesn't store the full reallocation chain for repeated reallocations
             * 
			if	( aItem.CellAddress == 0x00607ddc )
			{
				int x = 0;
				x++;
			}

			// Locate the original allocation item...
			MemObjStatisticalCollection collection = CollectionByCellAddress( aItem.OriginalCellAddress, TClass.EAllocation );
			if	( collection != null )
			{
				// Find the item
				MemOpBase item = collection.ItemByAddress( aItem.OriginalCellAddress, TClass.EAllocation );

				// It should be an allocation item
				System.Diagnostics.Debug.Assert( item is MemOpAllocation );

				// Update it
				MemOpAllocation allocItem = (MemOpAllocation) item;
				allocItem.AllocationSize = aItem.AllocationSize;
				allocItem.HeapSize = aItem.HeapSize;
				allocItem.CellAddress = aItem.CellAddress;
			}
			else
			{
				// Its permissable to call Realloc to allocate first-time memory
				// so its entirely possible that there wasn't any prior call to any of
				// the 'pure' alloc functions.

				// Add the item to our master list...
				iAllItems.Add( aItem );
                aItem.OperationIndex = iAllItems.Count;

				// Locate the corresponding collection and also add the item there too
                if ( aItem.LinkRegisterSymbol != null )
				{
					collection = CollectionBySymbol( aItem );
					if	( collection == null )
					{
						// Make a new collection
						collection = new MemObjStatisticalCollection();
						iCollections.Add( collection );
					}
					collection.Add( aItem );
				}
			}
			*/
        }

		public void AllItemsLocated()
		{
			// Sort the collection by number of items per sub-item
			MemAnalysisStatisticalComparerByAllocationCount comparer = new MemAnalysisStatisticalComparerByAllocationCount();
			iCollections.Sort( comparer );
		}

		public MemOpBase AllItemAt( int aIndex )
		{
			MemOpBase item = (MemOpBase) iAllItems[ aIndex ];
			return item;
		}

		public MemObjStatisticalCollection CollectionAt( int aIndex )
		{
			MemObjStatisticalCollection ret = (MemObjStatisticalCollection) iCollections[ aIndex ];
			return ret;
		}

		public MemObjStatisticalCollection CollectionByCellAddress( uint aCellAddress, TClass aClass )
		{
            MemObjStatisticalCollection ret = null;
            //
			int count = iAllItems.Count;
			for( int i = count-1; i>=0; i-- )
			{
				MemOpBase item = (MemOpBase) iAllItems[ i ];
                //
                if ( item.CellAddress == aCellAddress )
                {
                    if ( aClass == TClass.ENotApplicable )
                    {
                        // Found the item, now locate its collection...
                        ret = CollectionBySymbol( item );
                        break;
                    }
                    else if ( aClass == item.Class )
                    {
                        // Found the item, now locate its collection...
                        ret = CollectionBySymbol( item );
                        break;
                    }
                }
			}
            //
            return ret;
		}

        public MemObjStatisticalCollection CollectionByCellAddressSearchForwards( uint aCellAddress, TClass aClass )
		{
            MemObjStatisticalCollection ret = null;
            //
            int count = iAllItems.Count;
			for( int i = 0; i<count; i++ )
			{
                MemOpBase item = (MemOpBase) iAllItems[ i ];
                //
                if ( item.CellAddress == aCellAddress )
                {
                    if ( aClass == TClass.ENotApplicable )
                    {
                        // Found the item, now locate its collection...
                        ret = CollectionBySymbol( item );
                        break;
                    }
                    else if ( aClass == item.Class )
                    {
                        // Found the item, now locate its collection...
                        ret = CollectionBySymbol( item );
                        break;
                    }
                }
            }
            //
            return ret;
        }

        public MemObjStatisticalCollection CollectionBySymbol( MemOpBase aItem )
        {
			System.Diagnostics.Debug.Assert( aItem.IsAllocationType || aItem.IsReallocationType );
            //
			MemOpAllocation allocObject = (MemOpAllocation) aItem;
            MemObjStatisticalCollection ret = CollectionBySymbol( allocObject.LinkRegisterSymbol, allocObject.LinkRegisterAddress );
            //
            return ret;
        }

		public MemObjStatisticalCollection CollectionBySymbol( SymbolLib.Generics.GenericSymbol aSymbol, uint aAddress )
		{
			// We also need a symbol...
			MemObjStatisticalCollection ret = null;
			//
            if ( aSymbol != null )
			{
				int count = iCollections.Count;
				for( int i=0; i<count; i++ )
				{
					MemObjStatisticalCollection collection = (MemObjStatisticalCollection) iCollections[ i ];
					if	( collection != iNullSymbolCollection )
					{
						System.Diagnostics.Debug.Assert( collection.Count > 0 );
						MemOpAllocation compareToObject = (MemOpAllocation) collection[0];

						// Comparing the link register address is a very specific match - therefore we
						// prefer the slightly slower comparison against the symbol name. This prevents
						// multiple entries in the list that come from the same symbol (but different 
						// instruction address).. i.e. the allocation occurs within the same function,
						// but a slightly different location.
                        if ( compareToObject.LinkRegisterSymbol != null )
						{
                            if ( compareToObject.LinkRegisterSymbol.Symbol == aSymbol.Symbol )
							{
								ret = collection;
								break;
							}
						}
                        else if ( compareToObject.LinkRegisterAddress == aAddress )
						{
							// Symbols match, add it to this container
							ret = collection;
							break;
						}
					}
				}
			}
			else
			{
				// Must be from the null symbol collection then.
				ret = iNullSymbolCollection;
			}
			//
			return ret;
		}

		public MemOpBase ItemByLineNumber( long aLineNumber )
		{
			int count = iAllItems.Count;
			for( int i = count-1; i>=0; i-- )
			{
				MemOpBase item = (MemOpBase) iAllItems[ i ];
				if	( item.LineNumber == aLineNumber )
				{
					return item;
				}
			}

			return null;
		}
		#endregion

		#region Properties
		public int CollectionCount
		{
			get { return iCollections.Count; }
		}
		#endregion

        #region CollectionManager Members
        public void RemoveLastOperation()
        {
            int count = AllPriorOperationsCount;
            if ( count > 0 )
            {
                AllPriorOperations.RemoveAt( count - 1 );
                iLastOperation = LastOperation;
            }
        }

        public void RemovePriorOperation( MemOpBase aItem )
        {
            /*
            if ( aItem.CellAddress == 0xc8036684 )
            {
                int x = 0;
                x++;
            }
            */
            iAllItems.Remove( aItem );
        }

        public void AddNewPriorOperation( MemOpBase aItem )
        {
            iLastOperation = aItem;
            AllPriorOperations.Add( aItem );
            aItem.OperationIndex = AllPriorOperationsCount;
        }

        public void RemoveFromCollection( MemOpBase aItem )
        {
            int count = iCollections.Count;
            for ( int i = count - 1; i >= 0; i-- )
            {
                MemObjStatisticalCollection collection = (MemObjStatisticalCollection) iCollections[ i ];
                System.Diagnostics.Debug.Assert( collection.Count > 0 || collection == iNullSymbolCollection );
                if ( collection.Remove( aItem ) )
                {
                    // Remove the collection as well (if its empty)
                    if ( collection.Count == 0 )
                    {
                        iCollections.RemoveAt( i );
                    }
                    break;
                }
            }
        }

        public void AddToCollection( MemOpBase aItem, SymbolLib.Generics.GenericSymbol aSymbol, uint aLinkRegisterAddress )
        {
            MemObjStatisticalCollection collection = CollectionBySymbol( aSymbol, aLinkRegisterAddress );
            if ( collection == null )
            {
                // Make a new collection
                collection = new MemObjStatisticalCollection();
                iCollections.Add( collection );
            }

            collection.Add( aItem );
        }

        public MemOpBase PriorOperationByAllocationNumber( uint aAllocNumber )
        {
            MemOpBase ret = null;
            //
            for ( int i = AllPriorOperationsCount - 1; i >= 0; i-- )
            {
                MemOpBase op = AllPriorOperations[ i ];
                //
                if ( op.AllocationNumber == aAllocNumber )
                {
                    ret = op;
                    break;
                }
            }
            //
            return ret;
        }

        public MemOpBase LastOperation
        {
            get
            {
                MemOpBase ret = null;
                //
                if ( AllPriorOperationsCount > 0 )
                {
                    ret = AllPriorOperations[ AllPriorOperationsCount - 1 ];
                }
                //
                return ret;
            }
        }

        public int AllPriorOperationsCount
        {
            get { return iAllItems.Count; }
        }

        public List<MemOpBase> AllPriorOperations
        {
            get { return iAllItems; }
        }
        #endregion

		#region Data members
        private MemOpBase iLastOperation = new MemOpFree();
        private List<MemOpBase> iAllItems = new List<MemOpBase>( 5000 );
		private List<MemObjStatisticalCollection> iCollections = new List<MemObjStatisticalCollection>( 100 );
		private MemObjStatisticalCollection iNullSymbolCollection = new MemObjStatisticalCollection();
		#endregion
    }
}
