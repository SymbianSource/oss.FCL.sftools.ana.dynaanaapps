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

namespace MemAnalysisLib
{
	public class MemObjRegionMarker
	{
		#region Properties
		public bool Initialised
		{
			get
			{
				return !( LineNumber == KUninitialisedLineNumber && RegionText == KUninitialisedRegionText );
			}
		}
		
		public bool MatchedRegionText
		{
			get { return iMatchedRegionText; }
			set { iMatchedRegionText = value; }
		}

		public string RegionText
		{
			get { return iRegionText; }
			set { iRegionText = value; }
		}

		public long LineNumber
		{
			get { return iLineNumber; }
			set { iLineNumber = value; }
		}
		#endregion

		#region Internal constants
		const long KUninitialisedLineNumber = -1;
		const string KUninitialisedRegionText = "__!__!__!UNINIT!__!__!__";
		#endregion

		#region Data members
		private bool iMatchedRegionText;
		private string iRegionText = KUninitialisedRegionText;
		private long iLineNumber = KUninitialisedLineNumber;
		#endregion
	}

	public class MemObjRegionalCollection
	{
		#region Constructor & destructor
		public MemObjRegionalCollection()
		{
		}
		#endregion

		#region API
		public void Add( MemOpBase aItem )
		{
			iItems.Add( aItem );
		}

		public MemOpBase ItemByAddress( long aCellAddress )
		{
			int index;
			return ItemByAddress( aCellAddress, out index );
		}

		public MemOpBase ItemByAddress( long aCellAddress, out int aIndex )
		{
			aIndex = -1;
			MemOpBase ret = null;
			int count = iItems.Count;
			//
			for( int i = count-1; i>=0; i-- )
			{
				MemOpBase item = (MemOpBase) iItems[ i ];
				if	( item.CellAddress == aCellAddress )
				{
					aIndex = i;
					ret = item;
					break;
				}
			}
			//
			return ret;
		}

		public MemOpBase ItemByAddress( long aCellAddress, TClass aClass )
		{
			int index;
			return ItemByAddress( aCellAddress, aClass, out index );
		}

		public MemOpBase ItemByAddress( long aCellAddress, TClass aClass, out int aIndex )
		{
			aIndex = -1;
			MemOpBase ret = null;
			int count = iItems.Count;
			//
			for( int i = count-1; i>=0; i-- )
			{
				MemOpBase item = (MemOpBase) iItems[ i ];
				if	( item.CellAddress == aCellAddress )
				{
					if	( aClass == TClass.ENotApplicable || item.Class == aClass )
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

		public MemOpBase ItemByOperationIndex( long aOpIndex, TClass aClass, out int aIndex )
		{
			aIndex = -1;
			MemOpBase ret = null;
			int count = iItems.Count;
			//
			for( int i = count-1; i>=0; i-- )
			{
				MemOpBase item = (MemOpBase) iItems[ i ];
				if	( item.OperationIndex == aOpIndex )
				{
					if	( aClass == TClass.ENotApplicable || item.Class == aClass )
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

		public bool RemoveByCellAddress( MemOpBase aItem )
		{
			int index;
			bool ret = false;
			//
			if	( ItemByAddress( aItem.CellAddress, aItem.Class, out index ) != null )
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
				MemOpBase item = (MemOpBase) iItems[aIndex];
				return item;
			}
		}

		public MemObjRegionMarker RegionStart
		{
			get { return iRegionStart; }
			set { iRegionStart = value; }
		}

		public MemObjRegionMarker RegionEnd
		{
			get { return iRegionEnd; }
			set { iRegionEnd = value; }
		}

		public long AllocationCount
		{
			get
			{
				long ret = 0;
				//
				int count = Count;
				for(int i=0; i<count; i++)
				{
					MemOpBase item = this[i];
					if	( item is MemOpAllocation )
					{
						ret++;
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
				int count = Count;
				for(int i=0; i<count; i++)
				{
					MemOpBase item = this[i];
					if	( item is MemOpFree )
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
                    if ( op.Class == TClass.EReallocation )
                    {
                        #warning Fix me - TotalAmountOfAllocatedMemory is broken for reallocs
                    }
                    else if ( op.Class == TClass.EAllocation )
                    {
                        MemOpAllocation allocItem = (MemOpAllocation) op;
                        ret += allocItem.CellSize;
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
                    if ( op.Class == TClass.EDeallocation && op.Link != null )
                    {
                        MemOpAllocation allocItem = (MemOpAllocation) op.Link;
                        ret += allocItem.CellSize;
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
                            MemOpAllocation allocItem = (MemOpAllocation) op;
                            ret += allocItem.CellSize;
                        }
                        else if ( op.Class == TClass.EReallocation )
                        {
                            #warning Fix me - TotalMemoryAllocatedButNotFreed is broken for reallocs
                            MemOpReallocation allocItem = (MemOpReallocation) op;
                            ret += allocItem.CellSize;
                        }
                    }
                }
                //
                return ret;
            }
		}
		#endregion

		#region Internal methods
		#endregion

		#region Data members
		private MemObjRegionMarker iRegionStart = new MemObjRegionMarker();
		private MemObjRegionMarker iRegionEnd = new MemObjRegionMarker();
		private ArrayList iItems = new ArrayList(50);
		#endregion
	}

	public class MemObjRegionalData
	{
		#region Constants
		const string KOperationOutsideOfRegionText = "Memory operation(s) took place outside of region markers";
		#endregion

		#region Constructor & destructor
		public MemObjRegionalData()
		{
		}
		#endregion

		#region API
		public void MarkerStartIdentified( string aText, long aLineNumber )
		{
			if	( iCurrentCollection != null )
			{
				// We've just finished a collection. Normally this is handled
				// by the end item, but if the end item wasn't found, then
				// we must do it manually.
				CurrentCollectionComplete( KOperationOutsideOfRegionText, aLineNumber - 1, false );
			}

			// Set the starting items
			iCurrentCollection = new MemObjRegionalCollection();
			iCurrentCollection.RegionStart.RegionText = aText;
			iCurrentCollection.RegionStart.LineNumber = aLineNumber;
			iCurrentCollection.RegionStart.MatchedRegionText = true;
		}

		public void MarkerEndIdentified( string aText, long aLineNumber )
		{
			CurrentCollectionComplete( aText, aLineNumber, true );
		}

		public void AllItemsLocated( long aLastLineNumber )
		{
			// Don't need these anymore
			if	( iCurrentCollection != null )
			{
				iCurrentCollection.RegionEnd.RegionText = "";
				iCurrentCollection.RegionEnd.LineNumber = aLastLineNumber;
				iCollections.Add( iCurrentCollection );
			}
			iCurrentCollection = null;
			iAllItems.Clear();
		}

		public void Add( MemOpBase aItem, bool aDiscardAllocAndFreedMatchingCells )
		{
			#region When deallocating, search for original alloc and link items...
			// If the item is a de-allocation, hunt backwards through the allocation
			// list until we find the allocating cell. Then setup their two-way relationship
			bool throwAwayObject = false;

			if	( aItem.IsAllocationType == false )
			{
				int count = iAllItems.Count;
				for(int i=count - 1; i>=0; i--)
				{
					MemOpBase item = (MemOpBase) iAllItems[i];
					//
					if	( item.Link == null && item.CellAddress == aItem.CellAddress && item.IsAllocationType != aItem.IsAllocationType )
					{
						// The item should be the allocation that this de-alloc is associated
						// with..
						//System.Diagnostics.Debug.Assert( item.IsAllocationType == true ); - User::Realloc screwing things up?
						if	( aDiscardAllocAndFreedMatchingCells )
						{
							// Can ignore both cells. First remove teh previous allocation cell.
							iAllItems.RemoveAt( i );

							// We don't even add aItem to the 'all items' container.
							// However, we still need to remove the linked allocation from it's container.
							if	( item.Collection != null )
							{
								MemObjRegionalCollection collectionForAllocation = (MemObjRegionalCollection) item.Collection;
								int colCount = collectionForAllocation.Count;
								collectionForAllocation.RemoveByCellAddress( item );

								// Make sure we really removed it.
								System.Diagnostics.Debug.Assert( collectionForAllocation.Count == colCount - 1 );

								// We don't want to log this 'free' operation since it perfectly 
								// matched an allocation.
								throwAwayObject = true;
							}
						}
						else
						{
							item.Link = aItem;
							aItem.Link = item;
						}

						break;
					}
				}
			}
			#endregion
	
			// Add the item to our master list...
			if	( !throwAwayObject )
			{
				AddToCollection( aItem );
			}
		}

		public void Add( MemOpReallocation aItem )
		{
#warning THIS CODE IS TOTALLY BROKEN - RETEST AFTER COLLECING NEW TRACES

			/*
             * The main issue is that it doesn't store the full reallocation chain for repeated reallocations
             * 
			// Locate the original allocation item...
			int itemIndex;
			int collectionIndex;
			MemOpBase item;
			MemObjRegionalCollection collection = CollectionByCellAddress( aItem.OriginalCellAddress, TClass.EAllocation, out item, out collectionIndex, out itemIndex );
			//
			if	( collection != null && item != null )
			{
				// It should be an allocation item
				System.Diagnostics.Debug.Assert( item is MemOpAllocation );

				// Update it
				MemOpAllocation allocItem = (MemOpAllocation) item;
				allocItem.AllocationSize = aItem.AllocationSize;
				allocItem.HeapSize = aItem.HeapSize;
				allocItem.CellAddress = aItem.CellAddress;

				// Associate
				aItem.Link = item;

				AddToCollection( aItem );
			}
             */
		}

		public MemObjRegionalCollection CollectionByOperationIndex( long aOpIndex, TClass aClass, out MemOpBase aItem, out int aCollectionIndex, out int aItemIndex )
		{
			aItem = null;
			aCollectionIndex = -1;
			aItemIndex = -1;
			MemObjRegionalCollection ret = null;

			// First check whether the item is in the current collection (if we have one)
			if	( iCurrentCollection != null )
			{
				aItem = iCurrentCollection.ItemByOperationIndex( aOpIndex, aClass, out aItemIndex );
				if	( aItem != null )
				{
					// Yes, it resides in the current collection...
					ret = iCurrentCollection;
				}
			}
			else
			{
				// Need to search the remaining collections. Must search backwards!
				int count = iCollections.Count;
				for( int i = count-1; i>=0; i-- )
				{
					MemObjRegionalCollection collection = (MemObjRegionalCollection) iCollections[ i ];
					aItem = collection.ItemByOperationIndex( aOpIndex, aClass, out aItemIndex );
					if	( aItem != null )
					{
						// Yes, its in this collection 
						ret = collection;
						aCollectionIndex = i;
						break;
					}
				}
			}

			return ret;
		}

        public MemObjRegionalCollection CollectionByCellAddress( long aCellAddress, TClass aClass )
		{
			int index;
			return CollectionByCellAddress( aCellAddress, aClass, out index );
		}

		public MemObjRegionalCollection CollectionByCellAddress( long aCellAddress, TClass aClass, out int aCollectionIndex )
		{
			aCollectionIndex = -1;
			int itemIndex = -1;
			MemOpBase item = null;
			return CollectionByCellAddress( aCellAddress, aClass, out item, out aCollectionIndex, out itemIndex );
		}
		
		public MemObjRegionalCollection CollectionByCellAddress( long aCellAddress, TClass aClass, out MemOpBase aItem, out int aCollectionIndex, out int aItemIndex )
		{
			aItem = null;
			aCollectionIndex = -1;
			aItemIndex = -1;
			MemObjRegionalCollection ret = null;

			// First check whether the item is in the current collection (if we have one)
			if	( iCurrentCollection != null )
			{
				aItem = iCurrentCollection.ItemByAddress( aCellAddress, aClass, out aItemIndex );
				if	( aItem != null )
				{
					// Yes, it resides in the current collection...
					ret = iCurrentCollection;
				}
			}
			else
			{
				// Need to search the remaining collections. Must search backwards!
				int count = iCollections.Count;
				for( int i = count-1; i>=0; i-- )
				{
					MemObjRegionalCollection collection = (MemObjRegionalCollection) iCollections[ i ];
					aItem = collection.ItemByAddress( aCellAddress, aClass, out aItemIndex );
					if	( aItem != null )
					{
						// Yes, its in this collection 
						ret = collection;
						aCollectionIndex = i;
						break;
					}
				}
			}

			return ret;
		}
		#endregion

		#region Properties
		public int Count
		{
			get { return iCollections.Count; }
		}

		public MemObjRegionalCollection this[ int aIndex ]
		{
			get
			{
				MemObjRegionalCollection ret = (MemObjRegionalCollection) iCollections[ aIndex ];
				return ret;
			}
		}
		#endregion

		#region Internal methods
		private void AddToCollection( MemOpBase aItem )
		{
			// Add the item to our master list...
			iAllItems.Add( aItem );
			aItem.OperationIndex = iAllItems.Count;

			// If the start region marker hasn't been initialised, it means
			// that the object operation took place outside of an allocation
			if	( iCurrentCollection == null || iCurrentCollection.RegionStart.Initialised == false )
			{
				if	( iCurrentCollection == null )
					iCurrentCollection = new MemObjRegionalCollection();

				// In this case, we set the start line number to the line upon
				// which the operation took place.
				iCurrentCollection.RegionStart.LineNumber = aItem.LineNumber;
				iCurrentCollection.RegionStart.RegionText = KOperationOutsideOfRegionText;
			}

			// Associate the item with this collection
			aItem.Collection = iCurrentCollection;
			iCurrentCollection.Add( aItem );
		}
		
		private void CurrentCollectionComplete( string aText, long aLineNumber, bool aMatchedRegionText )
		{
			if	( iCurrentCollection != null )
			{
				iCurrentCollection.RegionEnd.RegionText = aText;
				iCurrentCollection.RegionEnd.LineNumber = aLineNumber;
				iCurrentCollection.RegionEnd.MatchedRegionText = aMatchedRegionText;
				//
				iCollections.Add( iCurrentCollection );
				iCurrentCollection = null;
			}
		}
		#endregion

		#region Data members
		private MemObjRegionalCollection iCurrentCollection;
		private ArrayList iAllItems = new ArrayList( 5000 );
		private ArrayList iCollections = new ArrayList( 100 );
		#endregion
	}
}
