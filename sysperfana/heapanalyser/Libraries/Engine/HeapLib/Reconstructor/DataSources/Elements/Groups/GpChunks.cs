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
    public class GpChunkCollection
    {
        #region Constructors & destructor
        public GpChunkCollection()
        {
        }
        #endregion

        #region API
        public void Add( GpChunk aChunk )
        {
            iEntries.Add( aChunk );
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iEntries.Count; }
        }
        #endregion

        #region Data members
        private List<GpChunk> iEntries = new List<GpChunk>();
        #endregion
    }

    public class GpChunk
    {
        #region Constructors & destructor
        public GpChunk()
        {
        }
        #endregion

        #region API

        #endregion

        #region Properties
        public uint BaseAddress
        {
            get { return iBaseAddress; }
            set { iBaseAddress = value; }
        }

        public uint Size
        {
            get { return iSize; }
            set { iSize = value; }
        }

        public uint Handle
        {
            get { return iHandle; }
            set { iHandle = value; }
        }

        public string Name
        {
            get { return iName; }
            set { iName = value; }
        }
        #endregion

        #region Data members
        private uint iBaseAddress = 0;
        private uint iSize = 0;
        private uint iHandle = 0;
        private string iName = string.Empty;
        #endregion
    }
}
