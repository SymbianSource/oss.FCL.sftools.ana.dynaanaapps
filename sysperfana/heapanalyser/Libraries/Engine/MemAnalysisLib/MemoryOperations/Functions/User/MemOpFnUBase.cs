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
using MemAnalysisLib.Parser.Prefixes;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Class;
using MemAnalysisLib.MemoryOperations.Operations;
using SymbianUtils;

namespace MemAnalysisLib.MemoryOperations.Functions.User
{
    internal abstract class MemOpFnUBase : MemOpFnBase
    {
		#region Constructors & destructor
        protected MemOpFnUBase()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override string ToString()
        {
            return "User::";
        }
		#endregion

        #region Internal methods
        protected void ParseCommonUser( MemOpBase aOperation, ref string aLine, MemAnalysisParserPrefixesBase aPrefixes )
        {
            string line = aLine;
            //
            if ( line.IndexOf( aPrefixes.CellAddress ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.CellAddress, ref line );
                aOperation.CellAddress = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.HeapSize ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.HeapSize, ref line );
                aOperation.HeapSize = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.ChunkSize ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.ChunkSize, ref line );
                aOperation.ChunkSize = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.LinkRegister ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.LinkRegister, ref line );
                aOperation.LinkRegisterAddress = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.CellSize ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.CellSize, ref line );
                aOperation.CellSize = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.AllocNumber ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.AllocNumber, ref line );
                aOperation.AllocationNumber = PrefixParser.ReadUint( ref line );
            }
            if ( line.IndexOf( aPrefixes.VTable ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.VTable, ref line );
                aOperation.VTable = PrefixParser.ReadUint( ref line );
            }
        }
        #endregion
    }
}
