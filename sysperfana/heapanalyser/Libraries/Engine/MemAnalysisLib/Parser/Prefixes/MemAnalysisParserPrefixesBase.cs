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
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using MemAnalysisLib.MemoryOperations.Functions;
using SymbolLib.Generics;
using SymbolLib.Engines;
using SymbianUtils;

namespace MemAnalysisLib.Parser.Prefixes
{
    public class MemAnalysisParserPrefixesBase
	{
		#region Constructors & destructor
		protected MemAnalysisParserPrefixesBase()
		{
		}
		#endregion

        #region API
        internal void AssociatePrefixWithFunction( string aPrefix, MemOpFnBase aFunction )
        {
            iFunctionPrefixes.Add( aPrefix, aFunction );
        }

        internal bool MatchesPrefix( ref string aLine, out MemOpFnBase aFunction )
        {
            bool foundMatch = false;
            aFunction = null;
            //
            foreach ( KeyValuePair< string, MemOpFnBase> entry in iFunctionPrefixes )
            {
                int pos = aLine.IndexOf( entry.Key );
                //
                if ( pos >= 0 )
                {
                    SymbianUtils.PrefixParser.SkipPrefix( entry.Key, ref aLine );
                    aFunction = entry.Value;
                    foundMatch = true;
                    break;
                }
            }
            //
            return foundMatch;
        }
        #endregion

        #region Properties
		public string HeapSize
		{
			get { return iHeapSize; }
			set { iHeapSize = value; }
		}

        public string ChunkSize
		{
            get { return iChunkSize; }
            set { iChunkSize = value; }
		}

		public string CellAddress
		{
			get { return iCellAddress; }
			set { iCellAddress = value; }
		}

		public string LinkRegister
		{
			get { return iLinkRegister; }
			set { iLinkRegister = value; }
		}

		public string AllocSize
		{
			get { return iAllocSize; }
			set { iAllocSize = value; }
		}

        public string AllocNumber
		{
            get { return iAllocNumber; }
            set { iAllocNumber = value; }
		}

		public string OriginalCellAddress
		{
			get { return iOriginalCellAddress; }
			set { iOriginalCellAddress = value; }
		}

		public string OriginalCellSize
		{
            get { return iOriginalCellSize; }
            set { iOriginalCellSize = value; }
		}

        public string OriginalCellAllocNumber
		{
            get { return iOriginalCellAllocNumber; }
            set { iOriginalCellAllocNumber = value; }
		}

		public string ReallocMode
		{
			get { return iReallocMode; }
			set { iReallocMode = value; }
		}

        public string VTable
        {
            get { return iVTable; }
            set { iVTable = value; }
        }

        public string CellSize
        {
            get { return iCellSize; }
            set { iCellSize = value; }
        }
        #endregion

		#region Data members
		private string iHeapSize = "heapSize: ";
        private string iChunkSize = "HCS: ";
		private string iCellAddress = "cell: ";
		private string iLinkRegister = "linkR:";
		private string iAllocSize = "allocSize:";
		private string iOriginalCellAddress = "oCell:";
		private string iOriginalCellSize = "oCSize:";
        private string iOriginalCellAllocNumber = "oCAN:";
        private string iReallocMode = "aMode:";
		private string iAllocNumber = "allocNum:";
        private string iVTable = "vTable:";
        private string iCellSize = "cSize:";
        private Dictionary<string, MemOpFnBase> iFunctionPrefixes = new Dictionary<string, MemOpFnBase>();
		#endregion
	}
}
