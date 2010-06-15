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
using System.Text.RegularExpressions;
using System.Threading;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils;
using SymbianUtils.Range;
using SymbianUtils.RawItems;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using SymbianUtils.TextUtilities.Readers.Types.Array;

namespace HeapComparisonLib.Data
{
    public class ComparsionEngine : AsyncArrayReader<HeapCell>
    {
        #region Constructors & destructor
        public ComparsionEngine( HeapReconstructor aReconstructor1, HeapReconstructor aReconstructor2 )
            : base( new ComparsionCellSupplierProxy( aReconstructor1 ) )
        {
            iReconstructor1 = aReconstructor1;
            iReconstructor2 = aReconstructor2;
        }
        #endregion

        #region API
        public void Compare()
        {
            AsyncRead();
        }
        #endregion

        #region Properties
        public HeapReconstructor Reconstructor1
        {
            get { return iReconstructor1; }
        }

        public HeapReconstructor Reconstructor2
        {
            get { return iReconstructor2; }
        }

        public HeapCellArrayWithStatistics ResultsUnchanged
        {
            get { return iUnchanged; }
        }

        public HeapCellArrayWithStatistics ResultsUnchangedLengthButDifferentContents
        {
            get { return iUnchangedLengthButDifferentContents; }
        }

        public HeapCellArrayWithStatistics ResultsChanged
        {
            get { return iChanged; }
        }

        public HeapCellArrayWithStatistics ResultsUniqueInHeap1
        {
            get { return iUniqueInReconstructor1; }
        }

        public HeapCellArrayWithStatistics ResultsUniqueInHeap2
        {
            get { return iUniqueInReconstructor2; }
        }
        #endregion

        #region From AsyncArrayReader
        protected override void HandleObject( HeapCell aCell, int aIndex, int aCount )
        {
            if ( aCell.Address == 0xc8023e5c || 
                 aCell.Address == 0xc8024064 ||
                 aCell.Address == 0xc80276d4 ||
                 aCell.Address == 0xc82100b4
                )
            {
                int x = 0;
                x++;
            }

            uint address = aCell.Address;
            HeapCell locatedCell = iReconstructor2.Data.CellByExactAddress( address );
            //
            if ( locatedCell != null )
            {
                System.Diagnostics.Debug.Assert( aCell.Tag == null );

                // Remove sucessfully located cells from the 2nd list
                iReconstructor2.Data.Remove( locatedCell );

                // Check vtables to detect if the same
                HeapCell.TType origType = aCell.Type;
                HeapCell.TType locatedType = locatedCell.Type;

                uint origVTable = aCell.PossibleVTableAddress;
                uint locatedVTable = locatedCell.PossibleVTableAddress;

                // Compare length, type and vTable
                if ( origType == locatedType )
                {
                    // Type is the same...
                    if ( aCell.Length == locatedCell.Length )
                    {
                        CompareCellsWithSameLengths( aCell, locatedCell );
                    }
                    else
                    {
                        CompareCellsWithDifferentLengths( aCell, locatedCell );
                    }
                }
                else
                {
                    // The cell is not of the same type, so they cannot have
                    // any commonality between them. Therefore they are unique in their
                    // respective heaps.
                    iUniqueInReconstructor1.Add( aCell );
                    iUniqueInReconstructor2.Add( locatedCell );
                }
            }
            else
            {
                // There wasn't any corresponding cell in the second list, so it's got to be
                // unique to the first heap
                iUniqueInReconstructor1.Add( aCell );
            }
        }

        protected override void HandleReadCompleted()
        {
            try
            {
                base.HandleReadCompleted();
            }
            finally
            {
                // Anything left behind in reconstructor 2 was unique to it.
                int count = iReconstructor2.Data.Count;
                int alreadyHave = iUniqueInReconstructor2.Count;
                foreach ( HeapCell leftover in iReconstructor2.Data )
                {
                    iUniqueInReconstructor2.Add( leftover );
                }
                System.Diagnostics.Debug.Assert( alreadyHave + count == iUniqueInReconstructor2.Count );
            }

            /*
            int unchanged = CountBySymbol( "CFsDisconnectRequest", iUnchanged );
            int diffContents = CountBySymbol( "CFsDisconnectRequest", iUnchangedLengthButDifferentContents );
            int changed = CountBySymbol( "CFsDisconnectRequest", iChanged );
            int unique1 = 0;// CountBySymbol( "CFsDisconnectRequest", iUniqueInReconstructor1 );
            int unique2 = CountBySymbol( "CFsDisconnectRequest", iUniqueInReconstructor2 );

            int total = unchanged + diffContents + changed + unique1 + unique2;

            int x = total;
            */
        }
        #endregion

        #region Internal methods
        private void CompareCellsWithSameLengths( HeapCell aFromRecon1, HeapCell aFromRecon2 )
        {
            System.Diagnostics.Debug.Assert( aFromRecon1.Tag == null );
            System.Diagnostics.Debug.Assert( aFromRecon1.Address == aFromRecon2.Address );
            System.Diagnostics.Debug.Assert( aFromRecon2.Type == aFromRecon2.Type );
            System.Diagnostics.Debug.Assert( aFromRecon1.Length == aFromRecon2.Length );

            // Cells are the same, at least at face value. We also compare
            // contents to spot those that are entirely [unchanged].
            bool equal = ( aFromRecon1.IsIdentical( aFromRecon2 ) );
            if ( equal )
            {
                // Absolutely identical to one another
                aFromRecon1.Tag = aFromRecon2;
                iUnchanged.Add( aFromRecon1 );
            }
            else
            {
                // Same length, same type, but otherwise the contents are different
                //
                // If it's an allocated cell and the vTables are different, then 
                // the cell is treated as [unique].
                //
                // If it's a free cell, we don't care.
                if ( aFromRecon2.Type == HeapCell.TType.EAllocated )
                {
                    uint vt1 = aFromRecon1.IsUnknown ? 0 : aFromRecon1.PossibleVTableAddress;
                    uint vt2 = aFromRecon2.IsUnknown ? 0 : aFromRecon2.PossibleVTableAddress;

                    // If there was no associated symbol then the comparison will be zero vs
                    // zero, in which case we'll still treat it as the [same length, diff content],
                    // rather than unique. This might happen for blob cells, e.g. those that
                    // are the payload for RArray or CBufFlat etc.
                    if ( vt1 == vt2 )
                    {
                        // VTables are the same, but somehow the remaining content is different.
                        // Must also check whether one is a descriptor an the other isn't!
                        if ( aFromRecon1.IsDescriptor != aFromRecon2.IsDescriptor )
                        {
                            // One is a descriptor, the other isn't => [unique]
                            iUniqueInReconstructor1.Add( aFromRecon1 );
                            iUniqueInReconstructor2.Add( aFromRecon2 );
                        }
                        else
                        {
                            // [same length, diff content]
                            aFromRecon1.Tag = aFromRecon2;
                            iUnchangedLengthButDifferentContents.Add( aFromRecon1 );
                        }
                    }
                    else
                    {
                        // VTables are different, lengths and types are the same => [unique]
                        iUniqueInReconstructor1.Add( aFromRecon1 );
                        iUniqueInReconstructor2.Add( aFromRecon2 );
                    }
                }
                else
                {
                    // Must be a free cell, and since the length is the same we
                    // really don't care => [same length, diff content]
                    aFromRecon1.Tag = aFromRecon2;
                    iUnchangedLengthButDifferentContents.Add( aFromRecon1 );
                }
            }
        }

        private void CompareCellsWithDifferentLengths( HeapCell aFromRecon1, HeapCell aFromRecon2 )
        {
            System.Diagnostics.Debug.Assert( aFromRecon1.Tag == null );
            System.Diagnostics.Debug.Assert( aFromRecon1.Address == aFromRecon2.Address );
            System.Diagnostics.Debug.Assert( aFromRecon2.Type == aFromRecon2.Type );
            System.Diagnostics.Debug.Assert( aFromRecon1.Length != aFromRecon2.Length );

            if ( aFromRecon1.Type == HeapCell.TType.EFree )
            {
                // Free cells, same address, different lengths => [changed]
                aFromRecon1.Tag = aFromRecon2;
                iChanged.Add( aFromRecon1 );
            }
            else if ( aFromRecon1.Type == HeapCell.TType.EAllocated )
            {
                if ( aFromRecon1.IsUnknown && aFromRecon2.IsUnknown )
                {
                    // Check if both are descriptors. If they are not, then
                    // assume unique.
                    if ( aFromRecon1.IsDescriptor != aFromRecon2.IsDescriptor )
                    {
                        // One is a descriptor, one isn't => [unique]
                        iUniqueInReconstructor1.Add( aFromRecon1 );
                        iUniqueInReconstructor2.Add( aFromRecon2 );
                    }
                    else
                    {
                        // Cells with unknown vTables, but at the same address are just assumed to be blobs that
                        // have grown or shrunk => [changed]
                        aFromRecon1.Tag = aFromRecon2;
                        iChanged.Add( aFromRecon1 );
                    }
                }
                else
                {
                    uint vt1 = aFromRecon1.IsUnknown ? 0 : aFromRecon1.PossibleVTableAddress;
                    uint vt2 = aFromRecon2.IsUnknown ? 0 : aFromRecon2.PossibleVTableAddress;

                    // If either vTable is different, then they are [unique]. If the vTables
                    // are the same, then this is odd - classes with known vTables shouldn't change
                    // size dynamically?
                    if ( vt1 == vt2 )
                    {
                        // VTables are the same, but somehow the length has
                        // changed. I don't know what to do about this at the moment so
                        // am filing the cells in the unique bucket!
                        iUniqueInReconstructor1.Add( aFromRecon1 );
                        iUniqueInReconstructor2.Add( aFromRecon2 );
                    }
                    else
                    {
                        // VTables are different, lengths not the same, types is the same => [unique]
                        iUniqueInReconstructor1.Add( aFromRecon1 );
                        iUniqueInReconstructor2.Add( aFromRecon2 );
                    }
                }
            }
        }

        private static bool CompareCommonContents( HeapCell aLeft, HeapCell aRight )
        {
            HeapCell smaller = aLeft;
            HeapCell larger = aRight;
            if ( aLeft.RawItems.Count > aRight.RawItems.Count )
            {
                smaller = aRight;
                larger = aLeft;
            }

            // The payload length is in bytes, the raw item count is in 32 bit words.
            // They should agree, once multipled by 4 - if not, something is badly borked.
            // We should probably throw an exception, but try to be tolerant for now...
            if ( smaller.RawItems.Count * 4 != smaller.PayloadLength )
            {
                return false;
            }
            else if ( larger.RawItems.Count * 4 != larger.PayloadLength )
            {
                return false;
            }

            int smallerSize = smaller.RawItems.Count;
            for ( int i = 0; i < smallerSize; i++ )
            {
                RawItem itemS = smaller[ i ];
                RawItem itemL = larger[ i ];
                //
                if ( itemS.Data != itemL.Data )
                {
                    return false;
                }
            }

            return true;
        }

        private static int CountBySymbol( string aText, HeapCellArray aArray )
        {
            int ret = 0;
            int count = aArray.Count;
            for ( int i = 0; i < count; i++ )
            {
                HeapCell cell = aArray[ i ];
                string sym = cell.SymbolStringWithoutDescriptorPrefix;
                if ( sym.Contains( aText ) )
                {
                    ++ret;
                }
            }
            return ret;
        }
        #endregion

        #region Data members
        private readonly HeapReconstructor iReconstructor1;
        private readonly HeapReconstructor iReconstructor2;
        //
        private HeapCellArrayWithStatistics iUnchanged = new HeapCellArrayWithStatistics();
        private HeapCellArrayWithStatistics iUnchangedLengthButDifferentContents = new HeapCellArrayWithStatistics();
        private HeapCellArrayWithStatistics iChanged = new HeapCellArrayWithStatistics();
        private HeapCellArrayWithStatistics iUniqueInReconstructor1 = new HeapCellArrayWithStatistics();
        private HeapCellArrayWithStatistics iUniqueInReconstructor2 = new HeapCellArrayWithStatistics();
        #endregion
    }

    internal class ComparsionCellSupplierProxy : AsyncArrayObjectSupplier<HeapCell>
    {
        #region Constructors & destructor
        public ComparsionCellSupplierProxy( HeapReconstructor aReconstructor )
        {
            iReconstructor = aReconstructor;
        }
        #endregion

        #region AsyncArrayObjectSupplier<HeapCell> Members
        public int ObjectCount
        {
            get { return iReconstructor.Data.Count; }
        }

        public HeapCell this[ int aIndex ]
        {
            get { return iReconstructor.Data[ aIndex ]; }
        }
        #endregion

        #region Data members
        private readonly HeapReconstructor iReconstructor;
        #endregion
    }
}
