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
using HeapLib.Array;
using HeapLib.Cells;
using SymbianUtils.Range;

namespace HeapLib.Array
{
	internal class HeapCellArrayAllocationNumberMap
	{
        #region Constructors & destructor
        public HeapCellArrayAllocationNumberMap( HeapCellArrayBase aArray )
		{
            int index = 0;
            /*
            System.IO.StreamWriter w = new System.IO.StreamWriter( "C:\\temp\\temp.txt", false );
             */
            foreach( HeapCell cell in aArray )
            {
                if  ( cell.Type == HeapCell.TType.EAllocated )
                {
                    uint allocNum = cell.AllocationNumber;
                    //
                    iRange.UpdateMin( allocNum );
                    iRange.UpdateMax( allocNum );
                    //
                    if  ( !iTable.ContainsKey( allocNum ) )
                    {
                        iTable.Add( allocNum, index );
                        //w.WriteLine( "      Alloc#: " + allocNum.ToString("d8") + ", addr: 0x" + cell.Address.ToString("x8") + ", indx: " + index.ToString() );
                    }
                    else
                    {
                        //w.WriteLine( "DUPE  Alloc#: " + allocNum.ToString("d8") + ", addr: 0x" + cell.Address.ToString("x8") );
                    }
                }
                ++index;
            }
            //w.Close();
            //
            iEntries = aArray;
		}
        #endregion

        #region API
        public bool InRange( uint aAllocNum )
        {
            return iRange.Contains( aAllocNum );
        }
        #endregion

        #region Properties
        public HeapCell this[ uint aAllocNum ]
        {
            get
            {
                HeapCell ret = null;
                //
                object indexObj = iTable[ aAllocNum ];
                if  ( indexObj != null )
                {
                    int index = (int) indexObj;
                    if  ( index >= 0 && index < iEntries.Count )
                    {
                        ret = iEntries[ index ];
                    }
                }
                //
                return ret;
            }
        }
        #endregion

        #region Data members
        private readonly HeapCellArrayBase iEntries;
        private Hashtable iTable = new Hashtable();
        private SymbianUtils.Range.AddressRange iRange = new AddressRange();
        #endregion
	}
}
