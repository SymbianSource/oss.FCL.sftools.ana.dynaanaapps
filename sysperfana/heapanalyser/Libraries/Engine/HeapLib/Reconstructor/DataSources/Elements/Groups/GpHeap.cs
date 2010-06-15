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

namespace HeapLib.Reconstructor.DataSources.Elements.Groups
{
    public class GpHeap
    {
        #region Constructors & destructor
        public GpHeap()
        {
        }
        #endregion

        #region API
        public uint GetExpectedAmountOfHeapData()
        {
            uint ret = ChunkSize - SizeOfRHeap;
            //
            if ( SizeOfRHeap == 0 )
            {
                // Maybe we have the allocated/free stats instead
                ret = InfoAlloc.AllocCellTotalSpace + InfoFree.FreeCellTotalSpace;
            }
            return ret;
        }
        #endregion

        #region Properties
        public bool IsDebugAllocatorWithStoredStackAddresses
        {
            get
            {
                bool ret = DebugAllocator;
                if ( ret )
                {
                    // Check high word of iRand for our special marker
                    uint masked = ( iRand & 0xFFFF0000 ) >> 16;
                    ret = ( masked == 0xDADD );
                }
                return ret;
            }
        }

        public uint TypeUid
        {
            get { return iTypeUid; }
            set { iTypeUid = value; }
        }

        // <summary>
        // This is the base address of the heap, NOT the base address of the heap chunk.
        // </summary>
        public uint HeapBaseAddress
        {
            get { return iBaseAddress; }
            set { iBaseAddress = value; }
        }

        // <summary>
        // This is the size of the heap chunk, not the size of the heap
        // </summary>
        public uint ChunkSize
        {
            get { return iSize; }
            set { iSize = value; }
        }

        // <summary>
        // This is the size of the RHeap object, in bytes
        // </summary>
        public uint SizeOfRHeap
        {
            get { return iSizeOfRHeap; }
            set
            {
                // MemSpy's size doesn't include the vtable
                // and therefore ChunkSize - value - 4 is actually the
                // correct size of the heap data.
                iSizeOfRHeap = value + KSizeOfRHeapVTable; 
            }
        }

        public uint MinSize
        {
            get { return iMinSize; }
            set { iMinSize = value; }
        }

        public uint MaxSize
        {
            get { return iMaxSize; }
            set { iMaxSize = value; }
        }

        public uint Slack
        {
            get { return iSlack; }
            set { iSlack = value; }
        }

        public bool DebugAllocator
        {
            get { return iDebugAllocator; }
            set { iDebugAllocator = value; }
        }

        public uint MinCellSize
        {
            get { return iMinCellSize; }
            set { iMinCellSize = value; }
        }

        public uint Rand
        {
            get { return iRand; }
            set { iRand = value; }
        }

        public GpHeapAllocCell InfoAlloc
        {
            get { return iGroupCellAlloc; }
        }

        public GpHeapFreeCell InfoFree
        {
            get { return iGroupCellFree; }
        }
        #endregion

        #region Internal constants
        private const int KSizeOfRHeapVTable = 4;
        #endregion

        #region Data members
        private uint iTypeUid = 0;
        private uint iBaseAddress = 0;
        private uint iSize = 0;
        private uint iSizeOfRHeap = 0;
        private uint iMinSize = 0;
        private uint iMaxSize = 0;
        private uint iSlack = 0;
        private bool iDebugAllocator = false;
        private uint iMinCellSize = 0;
        private uint iRand = 0;
        private GpHeapAllocCell iGroupCellAlloc = new GpHeapAllocCell();
        private GpHeapFreeCell iGroupCellFree = new GpHeapFreeCell();
        #endregion
    }
}
