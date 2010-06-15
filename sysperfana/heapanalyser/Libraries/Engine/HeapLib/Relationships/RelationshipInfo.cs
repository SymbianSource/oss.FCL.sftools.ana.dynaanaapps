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
using System.Text;
using SymbianUtils.RawItems;
using SymbianUtils.Utilities;
using HeapLib.Cells;

namespace HeapLib.Relationships
{
    public class RelationshipInfo
    {
        #region Constructors & destructor
        public RelationshipInfo()
        {
        }

        public RelationshipInfo( HeapCell aFromCell, RawItem aRawItemInFromCell, HeapCell aToCell )
        {
            FromCell = aFromCell;
            FromCellRawItem = aRawItemInFromCell;
            ToCell = aToCell;
        }
        #endregion

        #region Properties
        // <summary>
        //  The cell which contains the emebdded reference
        // </summary>
        public HeapCell FromCell
        {
            get { return iFromCell; }
            set { iFromCell = value; }
        }

        // <summary>
        // The cell to which the embedded reference points
        // </summary>
        public HeapCell ToCell
        {
            get { return iToCell; }
            set { iToCell = value; }
        }

        // <summary>
        // The raw item in the 'from' cell that points to
        // the 'to' cell.
        // </summary>
        public RawItem FromCellRawItem
        {
            get { return iFromCellRawItem; }
            set { iFromCellRawItem = value; }
        }

        // <summary>
        // A clean link, is one that goes from within 'from cell' to
        // the very start of 'to cell's' payload section
        // </summary>
        public bool IsCleanLink
        {
            get
            {
                RawItem rawItemInFromCell = FromCellRawItem;
                uint exactMatchAddress = rawItemInFromCell.Data - HeapCell.AllocatedCellHeaderSize;
                uint destinationAddress = ToCell.Address;
                bool cleanMatch = ( destinationAddress == exactMatchAddress );
                //
                return cleanMatch;
            }
        }

        // <summary>
        // This is the offset, in bytes into the 'from' cell that contains
        // the raw item that points to the 'to' cell.
        // </summary>
        public uint LinkAddressOffsetWithinFromCell
        {
            get
            {
                uint ret = FromCellRawItem.Address - FromCell.Address;
                return ret;
            }
        }
        #endregion

        #region From System.Object
        public override string ToString()
        {
            return ToCell.ToStringExtended();
        }
        #endregion

        #region Data members
        private HeapCell iFromCell = new HeapCell();
        private HeapCell iToCell = new HeapCell();
        private RawItem iFromCellRawItem = new RawItem();
        #endregion
    }
}
