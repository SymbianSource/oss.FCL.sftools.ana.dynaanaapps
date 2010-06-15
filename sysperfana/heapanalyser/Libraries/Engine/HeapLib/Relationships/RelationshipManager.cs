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
using System.Collections.Generic;
using System.Text;
using SymbianUtils.RawItems;
using SymbianUtils.Utilities;
using HeapLib.Array;
using HeapLib.Cells;

namespace HeapLib.Relationships
{
    public class RelationshipManager
    {
        #region Constructors & destructor
        internal RelationshipManager( HeapCell aCell )
        {
            iCell = aCell;
        }
        #endregion

        #region API
        internal void AddReferencedBy( HeapCell aCell )
        {
            iReferencedBy.Add( aCell );
        }

        internal void AddEmbeddedReferenceTo( RawItem aRawItemInThisCell, HeapCell aToCell )
        {
            System.Diagnostics.Debug.Assert( aRawItemInThisCell.Tag == null );

            if ( ContainsEmbeddedReference( aToCell ) == false )
            {
                // Make reference description
                RelationshipInfo referenceTo = new RelationshipInfo( iCell, aRawItemInThisCell, aToCell );

                // Set up relationship between a raw item in this cell and
                // the specified other cell.
                aRawItemInThisCell.Tag = referenceTo;

                // This cell now contains a reference to the other cell.
                iEmbeddedReferencesTo.Add( aToCell.Address, referenceTo );

                // aAnotherCell has been referenced by 'me'
                aToCell.RelationshipManager.ReferencedBy.Add( Parent );
            }
            else
            {
                //System.Diagnostics.Debug.WriteLine( "PREVENTING CYCLIC LINK - Cell: 0x" + aCell.Address.ToString("x8") + " already contains a linkRef to: 0x" + cell.Address.ToString("x8") );
            }
        }

        public HeapCell EmbeddedReference( uint aAddress )
        {
            HeapCell ret = null;
            //
            if ( ContainsEmbeddedReference( aAddress ) )
            {
                RelationshipInfo info = iEmbeddedReferencesTo[ aAddress ];
                ret = info.ToCell;
            }
            //
            return ret;
        }

        public bool ContainsEmbeddedReference( uint aAddress )
        {
            bool found = iEmbeddedReferencesTo.ContainsKey( aAddress );
            return found;
        }

        public bool ContainsEmbeddedReference( HeapCell aCell )
        {
            return ContainsEmbeddedReference( aCell.Address );
        }
        #endregion

        #region Properties
        public HeapCell Parent
        {
            get { return iCell; }
        }

        // <summary>
        // The array of cells that this particular cell contains references
        // to within it's raw item array (i.e. it's payload section).
        // </summary>
        public RelationshipCollection EmbeddedReferencesTo
        {
            get { return new RelationshipCollection( iEmbeddedReferencesTo ); }
        }

        // <summary>
        // The array of cells that are referencing this cell, i.e. the
        // list of other cells that contain addresses that point to 'me'
        // </summary>
        public HeapCellArrayBase ReferencedBy
        {
            get { return iReferencedBy; }
        }

        public HeapCell ReferencedByUnique
        {
            get
            {
                if (iReferencedBy.Count == 1)
                    return iReferencedBy[0];


                if (iReferencedBy.Count > 1)
                {
                    HeapCell firstHeapCell = iReferencedBy[0];

                    for (int i = 1; i < iReferencedBy.Count; i++)
                    {
                        if (!firstHeapCell.Equals(iReferencedBy[i]))
                        {
                            return null;
                        }
                    }
                    return iReferencedBy[0];
                }

                return null;
            }
        }
        
        public uint PayloadLengthOfEmbeddedCells
        {
            get
            {
                uint ret = 0;
                //
                foreach ( RelationshipInfo info in EmbeddedReferencesTo )
                {
                    ret += info.ToCell.PayloadLength;
                }
                //
                return ret;
            }
        }
        #endregion

        #region Data members
        private readonly HeapCell iCell;
        private Dictionary<uint, RelationshipInfo> iEmbeddedReferencesTo = new Dictionary<uint, RelationshipInfo>();
        private HeapCellArrayUnsorted iReferencedBy = new HeapCellArrayUnsorted();
        #endregion
    }
}
