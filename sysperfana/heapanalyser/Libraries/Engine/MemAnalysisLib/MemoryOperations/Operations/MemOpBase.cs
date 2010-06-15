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
using System.Text;
using System.Collections;
using System.Collections.Generic;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Class;
using SymbolLib.Generics;
using SymbolLib.Engines;

namespace MemAnalysisLib.MemoryOperations.Operations
{
	public abstract class MemOpBase
	{
		#region Constructors & destructor
        public MemOpBase()
		{
		}
		#endregion

        #region From System.Object
        public override string ToString()
        {
            string ret = "[" + LineNumber.ToString( "d8" ) + "] 0x" + CellAddress.ToString( "x8" ) + " " + FunctionName;
            return ret;
        }
        #endregion

		#region Type-related properties
		public bool IsAllocationType
		{
			get
			{
				return ( Class == TClass.EAllocation || Class == TClass.EReallocation );
			}
		}

		public bool IsDeallocationType
		{
			get
			{
				return ( Class == TClass.EDeallocation );
			}
		}

		public bool IsReallocationType
		{
			get
			{
				return ( Class == TClass.EReallocation );
			}
		}
        #endregion

        #region General properties
        public string FunctionName
        {
            get { return Function.ToString(); }
        }

        internal MemOpFnBase Function
		{
			get { return iFunction; }
			set { iFunction = value; }
		}

		public TClass Class
		{
			get
			{
                TClass ret = TClass.ENotApplicable;
				//
                if ( Function != null )
                {
                    ret = Function.Class;
                }
                //
                return ret;
			}
		}

        public uint LineNumber
		{
			get { return iLineNumber; }
			set { iLineNumber = value; }
		}

		public uint CellAddress
		{
			get { return iCellAddress; }
			set { iCellAddress = value; }
		}

        public uint CellSize
        {
            get { return iCellSize; }
            set { iCellSize = value; }
        }

        public uint AllocationNumber
        {
            get { return iAllocationNumber; }
            set { iAllocationNumber = value; }
        }

		public uint HeapSize
		{
			get { return iHeapSize; }
			set { iHeapSize = value; }
		}

		public uint ChunkSize
		{
            get { return iChunkSize; }
            set { iChunkSize = value; }
		}

        public uint VTable
		{
            get { return iVTable; }
            set { iVTable = value; }
		}

		public int OperationIndex
		{
			get { return iOperationIndex; }
			set { iOperationIndex = value; }
		}

        public MemOpBase Link
		{
			get { return iLink; }
			set { iLink = value; }
		}

		public object Collection
		{
			get { return iCollection; }
			set { iCollection = value; }
		}

        public uint LinkRegisterAddress
        {
            get { return iLinkRegisterAddress; }
            set { iLinkRegisterAddress = value; }
        }

        public GenericSymbol LinkRegisterSymbol
        {
            get { return iLinkRegisterSymbol; }
        }

        public GenericSymbol VTableSymbol
        {
            get { return iVTableSymbol; }
        }
        #endregion

        #region Abstract API
        public virtual void Finalise( SymbolLib.Engines.SymbolManager aSymbolManager )
        {
            iLinkRegisterSymbol = SymbolForAddress( LinkRegisterAddress, aSymbolManager );
            iVTableSymbol = SymbolForAddress( VTable, aSymbolManager );
        }
        #endregion

        #region Internal methods
        private GenericSymbol SymbolForAddress( uint aAddress, SymbolManager aSymbolManager )
        {
            System.Diagnostics.Debug.Assert( aSymbolManager != null );

            // Try and find a matching collection / symbol entry
            GenericSymbolCollection collection;
            GenericSymbol symbol = aSymbolManager.EntryByAddress( aAddress, out collection );
            return symbol;
        }
        #endregion

		#region Data members
        private int iOperationIndex;
		private uint iCellAddress;
		private uint iHeapSize;
        private uint iChunkSize;
        private uint iAllocationNumber;
        private uint iAllocationSize;
        private uint iCellSize;
		private uint iLineNumber;
        private uint iVTable;
		private MemOpFnBase iFunction = null;
		private MemOpBase iLink = null;
		private object iCollection = null;
        private uint iLinkRegisterAddress;
        private GenericSymbol iLinkRegisterSymbol;
        private GenericSymbol iVTableSymbol;
        #endregion
	}
}
