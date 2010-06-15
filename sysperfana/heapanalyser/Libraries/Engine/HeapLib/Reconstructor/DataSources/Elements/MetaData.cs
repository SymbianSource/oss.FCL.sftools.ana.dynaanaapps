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
using SymbianStructuresLib.CodeSegments;
using HeapLib.Reconstructor.DataSources.Elements.Data;
using HeapLib.Reconstructor.DataSources.Elements.Groups;

namespace HeapLib.Reconstructor.DataSources.Elements
{
    public class MetaData
    {
        #region Constructors & destructor
        public MetaData()
        {
        }
        #endregion

        #region API
        #endregion

        #region Properties
        public HeapData HeapData
        {
            get { return iDataHeap; }
        }

        public StackData StackData
        {
            get { return iDataStack; }
        }

        public GlobalData GlobalData
        {
            get { return iDataGlobal; }
        }

        public GpChunkCollection Chunks
        {
            get { return iGroupChunks; }
        }

        public GpHeader Header
        {
            get { return iGroupHeader; }
        }

        public GpFooter Footer
        {
            get { return iGroupFooter; }
        }

        public GpHeap Heap
        {
            get { return iGroupHeap; }
        }

        public CodeSegDefinitionCollection CodeSegments
        {
            get { return iCodeSegments; }
        }

        public bool IsDataComplete
        {
            get
            {
                bool ret = true;

                long actualCount = iDataHeap.Count;
                long expectedCount = iGroupHeap.GetExpectedAmountOfHeapData();
                if ( actualCount > 0 && expectedCount > 0 )
                {
                    ret = ( actualCount == expectedCount );
                }
                //
                return ret;
            }
        }
        #endregion

        #region Data members
        private HeapData iDataHeap = new HeapData();
        private StackData iDataStack = new StackData();
        private GlobalData iDataGlobal = new GlobalData();
        private GpChunkCollection iGroupChunks = new GpChunkCollection();
        private GpHeader iGroupHeader = new GpHeader();
        private GpFooter iGroupFooter = new GpFooter();
        private GpHeap iGroupHeap = new GpHeap();
        private CodeSegDefinitionCollection iCodeSegments = new CodeSegDefinitionCollection();
        #endregion
    }
}
