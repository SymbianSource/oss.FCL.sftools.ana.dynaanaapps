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
using MemAnalysisLib.Parser.Prefixes;
using MemAnalysisLib.MemoryOperations.Functions;
using MemAnalysisLib.MemoryOperations.Class;
using MemAnalysisLib.MemoryOperations.Operations;
using MemAnalysisLib.Interfaces;
using SymbianUtils;

namespace MemAnalysisLib.MemoryOperations.Functions.Kernel
{
    internal abstract class MemOpFnKReallocBase : MemOpFnKAllocBase
    {
		#region Constructors & destructor
        protected MemOpFnKReallocBase()
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
            base.ParseCommonKernel( ret, ref aLine, aPrefixes );
            base.ParseCommonAlloc( ret, ref aLine, aPrefixes );
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

    internal class MemOpFnKernReAlloc : MemOpFnKReallocBase
	{
		#region Constructors & destructor
        public MemOpFnKernReAlloc()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "Kern::ReAlloc";
        }
		#endregion
	}

    internal class MemOpFnKernSafeReAlloc : MemOpFnKReallocBase
	{
		#region Constructors & destructor
        public MemOpFnKernSafeReAlloc()
		{
		}
		#endregion

		#region From MemOpFnBase
        public override string ToString()
        {
            return base.ToString() + "Kern::SafeReAlloc";
        }

        public override void Process( MemOpBase aItem, CollectionManager aCollectionManager )
        {
            if ( aItem.CellAddress == 0xc8099f8c )
            {
                int x = 0;
                x++;
            }

            // (1) Alloc, SafeRealloc
            // 
            // [KMEM] OKA     - C: 0xc801a65c, HS:    29096, HCS:    53128, LR: 0x800b82f4, CS:        4, AS:        4, AN:       93
            // [KMEM] OKSR    - C: 0xc801a65c, HS:    29096, HCS:    53128, LR: 0x800aebdc, AS:        4, AN:       93, OC: 0x00000000, OCS:        0, OCAN:        0, VT: 0x00000000
            // 
            // 
            // (2) Realloc, SafeRealloc
            // [KMEM] OKR     - C: 0xc801ae1c, HS:    33656, HCS:    53128, LR: 0x800b82d4, AS:       12, AN:      106, MD: 1, OC: 0xc801ae1c, OCS:       12, OCAN:      106, VT: 0xc801a688
            // [KMEM] OKSR    - C: 0xc801ae1c, HS:    33656, HCS:    53128, LR: 0x800aebdc, AS:       12, AN:      106, OC: 0xc801ae1c, OCS:       12, OCAN:      106, VT: 0xc801a688
            // ...
            // [KMEM] OKR     - C: 0xc822ee8c, HS:  1672036, HCS:  2236296, LR: 0x800b83c8 [ Kern::SafeReAlloc(void*&, int, int) ], CS:      660, AS:      652, AN:    99936, MD: 0, OC: 0xc822ee8c, OCS:      660, OCAN:    99936, VT: 0xc80131e8
            // [KMEM] OKSR    - C: 0xc822ee8c, HS:  1672036, HCS:  2236296, LR: 0x800adc7c [ DObjectCon::Remove(DObject*) ], CS:      660, AS:      652, AN:    99936, OC: 0xc822ee8c, OCS:      660, OCAN:    99936, VT: 0xc80131e8
            // 
            //
            // (3) Alloc, Free, SafeRealloc
            //
            // [KMEM] OKA     - C: 0xc801a744, HS:    29268, HCS:    53128, LR: 0x800b82f4, CS:        4, AS:        4, AN:       98
            // [KMEM] OKSR    - C: 0xc801a744, HS:    29268, HCS:    53128, LR: 0x800aebdc, AS:        4, AN:       98, OC: 0x00000000, OCS:        0, OCAN:        0, VT: 0x00000000
            // ...
            // [KMEM] OKA     - C: 0xc801ae1c, HS:    30932, HCS:    53128, LR: 0x800b82f4, CS:       12, AS:        8, AN:      106
            // [KMEM] OKF     - C: 0xc801a744, HS:    30928, HCS:    53128, LR: 0x800b836c, CS:        4, AN:       98, VT: 0xc801a688
            // [KMEM] OKSR    - C: 0xc801ae1c, HS:    30928, HCS:    53128, LR: 0x800aebdc, AS:        8, AN:      106, OC: 0xc801a744, OCS:        4, OCAN:       98, VT: 0xc801a688

            MemOpBase lastOpByAllocNumber = aCollectionManager.PriorOperationByAllocationNumber( aItem.AllocationNumber );
            MemOpReallocation op = (MemOpReallocation) aItem;

            if ( lastOpByAllocNumber != null )
            {
                if ( lastOpByAllocNumber.CellAddress == op.CellAddress &&
                     lastOpByAllocNumber.Class == TClass.EAllocation &&
                     op.OriginalAddress == 0 )
                {
                    // 1) Alloc, SafeRealloc => dump alloc
                    aCollectionManager.RemoveFromCollection( lastOpByAllocNumber );
                    aCollectionManager.RemovePriorOperation( lastOpByAllocNumber );
                    aCollectionManager.AddToCollection( op, op.LinkRegisterSymbol, op.LinkRegisterAddress );
                    aCollectionManager.AddNewPriorOperation( op );
                }
                else if ( lastOpByAllocNumber.Class == TClass.EReallocation && lastOpByAllocNumber.CellAddress == op.CellAddress )
                {
                    // 2) Realloc, SafeRealloc => dump realloc
                    aCollectionManager.RemoveFromCollection( lastOpByAllocNumber );
                    aCollectionManager.RemovePriorOperation( lastOpByAllocNumber );

                    // Also, it's possible that this is just a no-op. For example, this pattern can occur when removing
                    // an object from a DObjectCon, in which case the DObjectCon code requests that the cell be shrunk in size.
                    //
                    // If there is no cell growth, then we shouldn't bother saving the operation.
                    int impact = (int) op.AllocationSize - (int) op.OriginalAllocationSize;
                    if ( impact > 0 )
                    {
                        aCollectionManager.AddToCollection( op, op.LinkRegisterSymbol, op.LinkRegisterAddress );
                        aCollectionManager.AddNewPriorOperation( op );
                    }
                }
                else
                {
                    // Checking for scenario (3)
                    //
                    // lastOpByAllocNumber will correspond to the 'alloc' operation. 
                    if ( lastOpByAllocNumber.Class == TClass.EAllocation &&
                         lastOpByAllocNumber.CellAddress == op.CellAddress ) // Satisfies linkage between OKSR and OKA
                    {
                        // We need to find out what the prior op was for the original cell allocation number. 
                        // This should point to the 'free' operation.
                        MemOpBase freeOp = aCollectionManager.PriorOperationByAllocationNumber( op.OriginalAllocationNumber );

                        // If we're throwing away matching allocated & subsequently freed cells, then freeOp will be null...
                        if ( freeOp == null || ( freeOp.Class == TClass.EDeallocation && freeOp.CellAddress == op.OriginalAddress ) )
                        {
                            // (3) Alloc, Free, SafeRealloc => dump alloc and free!

                            // Remove alloc, which is replaced by our new SafeRealloc entry
                            aCollectionManager.RemovePriorOperation( lastOpByAllocNumber );
                            aCollectionManager.RemoveFromCollection( lastOpByAllocNumber );

                            // Remove free for the old SafeRealloc heap cell
                            if ( freeOp != null )
                            {
                                aCollectionManager.RemovePriorOperation( freeOp );
                                aCollectionManager.RemoveFromCollection( freeOp );

                                // We may also need to remove a prior SafeRealloc that uses the same heap cell
                                MemOpBase safeReallocForDeletedCell = aCollectionManager.PriorOperationByAllocationNumber( freeOp.AllocationNumber );
                                if ( safeReallocForDeletedCell.Class == TClass.EReallocation && safeReallocForDeletedCell.CellAddress == freeOp.CellAddress )
                                {
                                    // Remove prior realloc associated with the old heap cell
                                    aCollectionManager.RemovePriorOperation( safeReallocForDeletedCell );
                                    aCollectionManager.RemoveFromCollection( safeReallocForDeletedCell );
                                }
                            }
                            
                            // Now add our new SafeRealloc entry to the master list
                            aCollectionManager.AddToCollection( op, op.LinkRegisterSymbol, op.LinkRegisterAddress );
                            aCollectionManager.AddNewPriorOperation( op );
                        }
                    }
                }
            }
        }
        #endregion
	}
}
