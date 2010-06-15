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
    internal abstract class MemOpFnUAllocBase : MemOpFnUBase
    {
		#region Constructors & destructor
        protected MemOpFnUAllocBase()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override MemAnalysisLib.MemoryOperations.Class.TClass Class
        {
            get { return TClass.EAllocation; } 
        }

        public override MemOpBase Parse( ref string aLine, MemAnalysisParserPrefixesBase aPrefixes )
        {
            MemOpAllocation ret = new MemOpAllocation();
            
            // Parse fields
            base.ParseCommonUser( ret, ref aLine, aPrefixes );
            ParseCommonAlloc( ret, ref aLine, aPrefixes );

            // Set type
            ret.Function = this;

            return ret;
        }
        #endregion

        #region Internal methods
        protected void ParseCommonAlloc( MemOpAllocation aOperation, ref string aLine, MemAnalysisParserPrefixesBase aPrefixes )
        {
            if ( aLine.IndexOf( aPrefixes.AllocSize ) >= 0 )
            {
                PrefixParser.SkipPrefix( aPrefixes.AllocSize, ref aLine );
                aOperation.AllocationSize = PrefixParser.ReadUint( ref aLine );
            }
        }
        #endregion
    }

    internal class MemOpFnUA : MemOpFnUAllocBase
	{
		#region Constructors & destructor
        public MemOpFnUA()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "Alloc()";
        }
		#endregion
	}

    internal class MemOpFnUAL : MemOpFnUAllocBase
    {
        #region Constructors & destructor
        public MemOpFnUAL()
        {
        }
        #endregion

        #region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "AllocL()";
        }
        #endregion
    }

    internal class MemOpFnUALC : MemOpFnUAllocBase
    {
        #region Constructors & destructor
        public MemOpFnUALC()
        {
        }
        #endregion

        #region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "AllocLC()";
        }
        #endregion
    }

    internal class MemOpFnUAZ : MemOpFnUAllocBase
    {
        #region Constructors & destructor
        public MemOpFnUAZ()
        {
        }
        #endregion

        #region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "AllocZ()";
        }
        #endregion
    }

    internal class MemOpFnUAZL : MemOpFnUAllocBase
    {
        #region Constructors & destructor
        public MemOpFnUAZL()
        {
        }
        #endregion

        #region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "AllocZL()";
        }
        #endregion
    }
}
