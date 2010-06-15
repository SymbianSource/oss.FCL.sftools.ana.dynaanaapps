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
    internal abstract class MemOpFnUReallocBase : MemOpFnUBase
    {
		#region Constructors & destructor
        protected MemOpFnUReallocBase()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override MemAnalysisLib.MemoryOperations.Class.TClass Class
        {
            get { return TClass.EReallocation; } 
        }

        public override MemOpBase Parse( ref string aLine, MemAnalysisParserPrefixesBase aPrefixes )
        {
            MemOpReallocation ret = new MemOpReallocation();

            // Parse fields
            base.ParseCommonUser( ret, ref aLine, aPrefixes );
            ParseCommonRealloc( ret, ref aLine, aPrefixes );

            // Set type
            ret.Function = this;

            return ret;
        }
        #endregion

        #region Internal methods
        protected void ParseCommonRealloc( MemOpReallocation aOperation, ref string aLine, MemAnalysisParserPrefixesBase aPrefixes )
        {
            if ( aLine.IndexOf( aPrefixes.ReallocMode ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.ReallocMode, ref aLine );
                aOperation.ReallocationMode = (byte) PrefixParser.ReadLong( ref aLine );
            }
            if ( aLine.IndexOf( aPrefixes.OriginalCellAddress ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.OriginalCellAddress, ref aLine );
                aOperation.OriginalAddress = PrefixParser.ReadUint( ref aLine );
            }
            if ( aLine.IndexOf( aPrefixes.OriginalCellSize ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.OriginalCellSize, ref aLine );
                aOperation.OriginalAllocationSize = PrefixParser.ReadUint( ref aLine );
            }
            if ( aLine.IndexOf( aPrefixes.OriginalCellAllocNumber ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.OriginalCellAllocNumber, ref aLine );
                aOperation.OriginalAllocationNumber = PrefixParser.ReadUint( ref aLine );
            }
        }
        #endregion
    }

    internal class MemOpFnUR : MemOpFnUReallocBase
	{
		#region Constructors & destructor
        public MemOpFnUR()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "Realloc()";
        }
		#endregion
	}

    internal class MemOpFnURL : MemOpFnUReallocBase
    {
        #region Constructors & destructor
        public MemOpFnURL()
        {
        }
        #endregion

        #region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "ReallocL()";
        }
        #endregion
    }
}
