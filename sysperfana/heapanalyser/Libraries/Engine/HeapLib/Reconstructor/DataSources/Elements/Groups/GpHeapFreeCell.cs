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
    public class GpHeapFreeCell
    {
        #region Constructors & destructor
        public GpHeapFreeCell()
        {
        }
        #endregion

        #region API
        public bool IsFreeCell( uint aAddress )
        {
            bool ret = false;
            //
            if ( iFreeCellAddress != aAddress )
            {
                if ( iFreeCellLargestAddress != aAddress )
                {
                    ret = iCells.ContainsKey( aAddress );
                    if ( ret )
                    {
                        // Debugging support
                        System.Diagnostics.Debug.WriteLine( "FC: 0x" + aAddress.ToString( "x8" ) );
                        int x = 0;
                        x++;
                    }
                }
                else
                {
                    ret = true;
                }
            }
            else
            {
                ret = true;
            }
            //
            return ret;
        }

        public void AddFreeCell( uint aAddress, int aLength, int aType )
        {
            if ( iCells.ContainsKey( aAddress ) == false )
            {
                iCells.Add( aAddress, new FreeCell( aAddress, aLength, aType ) );
            }
        }
        #endregion

        #region Properties
        public uint FreeCellAddress
        {
            get { return iFreeCellAddress; }
            set { iFreeCellAddress = value; }
        }

        public uint FreeCellLength
        {
            get { return iFreeCellLength; }
            set { iFreeCellLength = value; }
        }

        public int FreeCellCount
        {
            get { return iFreeCellCount; }
            set { iFreeCellCount = value; }
        }

        public uint FreeCellLargestAddress
        {
            get { return iFreeCellLargestAddress; }
            set { iFreeCellLargestAddress = value; }
        }

        public uint FreeCellLargestLength
        {
            get { return iFreeCellLargestLength; }
            set { iFreeCellLargestLength = value; }
        }

        public uint FreeCellTotalSpace
        {
            get { return iFreeCellTotalSpace; }
            set { iFreeCellTotalSpace = value; }
        }
        #endregion

        #region Data members
        private uint iFreeCellAddress = 0;
        private uint iFreeCellLength = 0;
        private int iFreeCellCount = 0;
        private uint iFreeCellLargestAddress = 0;
        private uint iFreeCellLargestLength = 0;
        private uint iFreeCellTotalSpace = 0;
        private Dictionary<uint,FreeCell> iCells = new Dictionary<uint, FreeCell>();
        #endregion
    }

    internal class FreeCell
    {
        #region Enumerations
        public enum TType
        {
            EGoodAllocatedCell = 0,
            EGoodFreeCell,
            EBadAllocatedCellSize,
            EBadAllocatedCellAddress,
            EBadFreeCellAddress,
            EBadFreeCellSize
        }
        #endregion

        #region Constructors & destructor
        public FreeCell( uint aAddress, int aLength, int aType )
        {
            iAddress = aAddress;
            iLength = aLength;
            iType = (TType) aType;
        }
        #endregion

        #region Properties
        public uint Address
        {
            get { return iAddress; }
        }

        public int Length
        {
            get { return iLength; }
        }

        public TType Type
        {
            get { return iType; }
        }
        #endregion

        #region Data members
        private readonly uint iAddress;
        private readonly int iLength;
        private readonly TType iType;
        #endregion
    }
}
