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
using System.Text.RegularExpressions;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils.FileSystem.FilePair;

namespace HeapLib.Reconstructor.DataSources
{
    public sealed class DataSourceCollection : IEnumerable<DataSource>
    {
        #region Constructors & destructor
        public DataSourceCollection()
        {
        }

        public DataSourceCollection( DataSourceCollection aList1, DataSourceCollection aList2 )
        {
            foreach ( DataSource source in aList1 )
            {
                if ( aList2.IndexOf( source.ThreadName ) >= 0 )
                {
                    Add( source );
                }
            }
        }
        #endregion

        #region API
        public int IndexOf( string aThread )
        {
            int ret = -1;
            //
            for ( int i = 0; i < Count; i++ )
            {
                DataSource src = this[ i ];
                if ( src.ThreadName.ToUpper() == aThread.ToUpper() )
                {
                    ret = i;
                    break;
                }
            }
            //
            return ret;
        }

        public void RemoveAt( int aIndex )
        {
            iEntries.RemoveAt( aIndex );
        }

        public void Clear()
        {
            iEntries.Clear();
        }

        internal void RemoveEmptySources()
        {
            int count = Count;
            for ( int i = count - 1; i >= 0; i-- )
            {
                DataSource src = this[ i ];
                if ( src.IsEmpty )
                {
                    iEntries.RemoveAt( i );
                }
            }
        }

        internal void Add( DataSource aSource )
        {
            iEntries.Add( aSource );
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iEntries.Count; }
        }

        public DataSource this[ int aIndex ]
        {
            get
            {
                DataSource ret = iEntries[ aIndex ];
                return ret;
            }
        }

        public DataSource this[ string aThread ]
        {
            get
            {
                DataSource ret = null;
                //
                int index = IndexOf( aThread );
                if ( index >= 0 && index < Count )
                {
                    ret = this[ index ];
                }
                //
                return ret;
            }
        }

        public DataSource Last
        {
            get
            {
                DataSource ret = this[ Count - 1 ];
                return ret;
            }
        }
        #endregion

        #region IEnumerable<DataSource> Members
        IEnumerator<DataSource> IEnumerable<DataSource>.GetEnumerator()
        {
            return new DataSourceCollectionEnumerator( this );
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new DataSourceCollectionEnumerator( this );
        }
        #endregion

        #region Data members
        private List<DataSource> iEntries = new List<DataSource>();
        #endregion
    }

    #region Internal enumerator class
    internal sealed class DataSourceCollectionEnumerator : IEnumerator<DataSource>
    {
        #region Constructors & destructor
        public DataSourceCollectionEnumerator( DataSourceCollection aCollection )
        {
            iCollection = aCollection;
        }
        #endregion

        #region IEnumerator Members
        public void Reset()
        {
            iCurrentIndex = -1;
        }

        public object Current
        {
            get
            {
                return (DataSource) iCollection[ iCurrentIndex ];
            }
        }

        public bool MoveNext()
        {
            return ( ++iCurrentIndex < iCollection.Count );
        }
        #endregion

        #region From IEnumerator<DataSource>
        DataSource IEnumerator<DataSource>.Current
        {
            get { return iCollection[ iCurrentIndex ]; }
        }
        #endregion

        #region From IDisposable
        public void Dispose()
        {
        }
        #endregion

        #region Data members
        private readonly DataSourceCollection iCollection;
        private int iCurrentIndex = -1;
        #endregion
    }
    #endregion
}
