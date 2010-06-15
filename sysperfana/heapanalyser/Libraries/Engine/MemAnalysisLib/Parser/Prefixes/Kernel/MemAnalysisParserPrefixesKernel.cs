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
using MemAnalysisLib.Parser.Prefixes;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Functions.Kernel;
using SymbolLib.Generics;
using SymbolLib.Engines;
using SymbianUtils;

namespace MemAnalysisLib.Parser.Prefixes.Kernel
{
    public class MemAnalysisParserPrefixesKernel : MemAnalysisParserPrefixesBase
    {
        #region Constructors & destructor
        public MemAnalysisParserPrefixesKernel()
		{
            // From gcchelpers.cpp - Allocations
            AssociatePrefixWithFunction( "ON      - ", new MemOpFnNew() );
            AssociatePrefixWithFunction( "ON[]    - ", new MemOpFnNewArray() );
            AssociatePrefixWithFunction( "ON+EX   - ", new MemOpFnNewExtraSize() );
            AssociatePrefixWithFunction( "ON-NT   - ", new MemOpFnNewNoThrow() );
            AssociatePrefixWithFunction( "ON-NT[] - ", new MemOpFnNewNoThrowArray() );

            // From gcchelpers.cpp - Frees
            AssociatePrefixWithFunction( "OD      - ", new MemOpFnDel() );
            AssociatePrefixWithFunction( "OD[]    - ", new MemOpFnDelArray() );

            // From DBase
            AssociatePrefixWithFunction( "ODBN    - ", new MemOpFnNewDBase() );
            AssociatePrefixWithFunction( "ODBN-EX - ", new MemOpFnNewDBaseExtraSize() );
            AssociatePrefixWithFunction( "ODBD    - ", new MemOpFnDelDBase() );

            // From TProperty
            AssociatePrefixWithFunction( "OTPN    - ", new MemOpFnNewTProperty() );
            AssociatePrefixWithFunction( "OTPD    - ", new MemOpFnDelTProperty() );

            // From Kern::
            AssociatePrefixWithFunction( "OKA     - ", new MemOpFnKernAlloc() );
            AssociatePrefixWithFunction( "OKAZ    - ", new MemOpFnKernAllocZ() );
            AssociatePrefixWithFunction( "OKF     - ", new MemOpFnKernFree() );
            AssociatePrefixWithFunction( "OKR     - ", new MemOpFnKernReAlloc() );
            AssociatePrefixWithFunction( "OKSR    - ", new MemOpFnKernSafeReAlloc() );

            // These are re-defined
            HeapSize                = "HS: ";
            CellAddress             = "C: ";
            LinkRegister            = "LR: ";
            CellSize                = "CS: ";
            AllocSize               = "AS: ";
            AllocNumber             = "AN: ";
            VTable                  = "VT: ";
            ReallocMode             = "MD: ";
            OriginalCellAddress     = "OC: ";
            OriginalCellSize        = "OCS: ";
            OriginalCellAllocNumber = "OCAN: ";
        }
        #endregion
    }
}
