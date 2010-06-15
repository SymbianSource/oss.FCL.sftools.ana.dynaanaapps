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
    public class GpHeader
    {
        #region Constructors & destructor
        public GpHeader()
        {
        }
        #endregion

        #region API

        #endregion

        #region Properties
        public uint Signature
        {
            get { return iSignature; }
            set { iSignature = value; }
        }

        public int VersionNumber
        {
            get { return iVersionNumber; }
            set { iVersionNumber = value; }
        }

        public uint ExpectedDataSize
        {
            get { return iExpectedDataSize; }
            set { iExpectedDataSize = value; }
        }

        public uint ProcessId
        {
            get { return iProcessId; }
            set { iProcessId = value; }
        }

        public uint ThreadId
        {
            get { return iThreadId; }
            set { iThreadId = value; }
        }

        public bool IsUserThread
        {
            get { return iIsUserThread; }
            set { iIsUserThread = value; }
        }

        public string ThreadName
        {
            get { return iThreadName; }
            set { iThreadName = value; }
        }
        #endregion

        #region Data members
        private uint iSignature = 0;
        private int iVersionNumber = 0;
        private uint iExpectedDataSize = 0;
        private uint iProcessId = 0;
        private uint iThreadId = 0;
        private bool iIsUserThread = false;
        private string iThreadName = string.Empty;
        #endregion
    }
}
