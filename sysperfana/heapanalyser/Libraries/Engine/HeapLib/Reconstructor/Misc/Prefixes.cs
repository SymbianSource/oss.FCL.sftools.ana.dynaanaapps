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

namespace HeapLib.Reconstructor.Misc
{
    public sealed class Prefixes
	{
		#region Constructors & destructor
		public Prefixes()
		{
		}
		#endregion

		#region Properties
        // <summary>
        // This is the prefix for the base address of the heap, NOT the base address of the heap chunk.
        // </summary>
		public string[] HeapBaseAddress
		{
            get { return iHeapBaseAddress; }
		}

        // <summary>
        // This is the prefix for the size of the heap chunk, not the size of the heap
        // </summary>
		public string[] HeapChunkSize
		{
            get { return iHeapSize; }
		}

        // <summary>
        // This is the prefix for the size of the RHeap object
        // </summary>
        public string[] SizeOfRHeap
        {
            get { return iSizeOfRHeap; }
        }

        public string[] DebugAllocator
		{
			get { return iDebugAllocator; }
		}

        public string[] AllocCellCount
        {
            get { return iAllocCellCount; }
        }

        public string[] AllocStatistics
        {
            get { return iAllocStatistics; }
        }

        public string[] FreeCellAddress
		{
			get { return iFreeCellAddress; }
		}

        public string[] FreeCellLength
		{
			get { return iFreeCellLength; }
		}

        public string[] FreeCellCount
		{
			get { return iFreeCellCount; }
		}

        public string[] FreeStatistics
        {
            get { return iFreeStatistics; }
        }

        public string[] FreeCellList
        {
            get { return iFreeCellList; }
        }

        public string[] CommonStatisticsSizeOfCells
        {
            get { return iStatisticsSizeOfCells; }
        }

        public string[] HeapCellMinimumSize
		{
			get { return iHeapCellMinimumSize; }
		}

        public string[] CodeSegment
		{
			get { return iCodeSegment; }
		}

        public string[] MiscRHeapRand
		{
            get { return iMisc_RHeap_iRand; }
		}
		#endregion

		#region Data members
        private static readonly string[] iHeapSize = new string[] { "HeapInfo - heapSize:", "Chunk Size:" };
        private static readonly string[] iHeapBaseAddress = new string[] { "HeapInfo - heapBaseAddress:", "RHeap::iBase" };
		private static readonly string[] iDebugAllocator = new string[] { "HeapInfo - heapUsingDebugAlloc:", "Debug Allocator:" };
        private static readonly string[] iSizeOfRHeap = new string[] { "Object Size:" };
        private static readonly string[] iStatisticsSizeOfCells = new string[] { "Size of cells:" };
        //
        private static readonly string[] iMisc_RHeap_iRand = new string[] { "RHeap::iRand" };
        //
        private static readonly string[] iAllocCellCount = new string[] { "HeapInfo - heapCellAllocCount:", "Number of cells:" };
        private static readonly string[] iAllocStatistics = new string[] { "Allocated Cell Statistics" };
        //
		private static readonly string[] iFreeCellAddress = new string[] {  "HeapInfo - heapFreeCellAddress:", "RHeap::iFree.next" };
        private static readonly string[] iFreeCellCount = new string[] { "HeapInfo - heapFreeCellCount:", "Number of cells:" };
        private static readonly string[] iFreeCellLength = new string[] { "HeapInfo - heapFreeCellLength:", "RHeap::iFree.len" };
        private static readonly string[] iFreeStatistics = new string[] { "Free Cell Statistics" };
        private static readonly string[] iFreeCellList = new string[] { "Free Cell List" };
        //
		private static readonly string[] iHeapCellMinimumSize = new string[] { "HeapInfo - heapMinCellSize:", "RHeap::iMinCell" };
        private static readonly string[] iCodeSegment = new string[] { "CodeSegs - " };
		#endregion
	}
}
