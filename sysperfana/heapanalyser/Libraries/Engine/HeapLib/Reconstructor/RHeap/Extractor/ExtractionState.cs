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
using HeapLib.Reconstructor.DataSources;

namespace HeapLib.Reconstructor.RHeap.Extractor
{
    internal class ExtractionState
    {
        #region Constructors & destructor
        public ExtractionState( DataSource aSource )
        {
            iDataSource = aSource;
        }
        #endregion

        #region API
        public bool IsFreeCellAddress()
        {
            bool ret = false;
            //
            uint address = iCurrentAddress;
            if ( address == iNextFreeCellAddress )
            {
                ret = true;
            }
            else
            {
                ret = iDataSource.MetaData.Heap.InfoFree.IsFreeCell( address );
            }
            //
            return ret;
        }
        #endregion

        #region Properties
        public bool DebugEnabled
        {
            get { return iDebugEnabled; }
            set { iDebugEnabled = value; }
        }

        public bool EncounteredException
        {
            get { return iEncounteredException; }
            set { iEncounteredException = value; }
        }

        public uint NextFreeCellAddress
        {
            get { return iNextFreeCellAddress; }
            set { iNextFreeCellAddress = value; }
        }

        public uint NextFreeCellLength
        {
            get { return iNextFreeCellLength; }
            set { iNextFreeCellLength = value; }
        }

        public uint NextCellAddress
        {
            get { return iStartOfNextCell; }
            set { iStartOfNextCell = value; }
        }

        public uint ExpectedAddress
        {
            get { return iNextExpectedAddress; }
            set { iNextExpectedAddress = value; }
        }

        public uint CurrentAddress
        {
            get { return iCurrentAddress; }
            set { iCurrentAddress = value; }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Internal constants
        private const int KBaseHex = 16;
        #endregion

        #region Data members
        private readonly DataSource iDataSource;
        private bool iDebugEnabled = false;
        private bool iEncounteredException = false;
        private uint iNextFreeCellAddress;
        private uint iNextFreeCellLength;
        private uint iNextExpectedAddress;
        private uint iCurrentAddress = 0;
        private uint iStartOfNextCell;
        #endregion
    }
}
