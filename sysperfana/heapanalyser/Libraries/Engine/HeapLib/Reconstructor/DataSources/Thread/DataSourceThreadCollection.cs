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

namespace HeapLib.Reconstructor.DataSources
{
    public class HeapReconstructorDataSourceThreadCollection : IEnumerable<HeapReconstructorDataSourceThread>
    {
        #region Constructors & destructor
        public HeapReconstructorDataSourceThreadCollection()
        {
        }
        #endregion

        #region API
        public void Add( string aThreadName )
        {
            iItems.Add( new HeapReconstructorDataSourceThread( aThreadName ) );
        }

        public void Add( string aThreadName, string aPrefix )
        {
            iItems.Add( new HeapReconstructorDataSourceThread( aThreadName, aPrefix ) );
        }

        public void Add( HeapReconstructorDataSourceThread aItem )
        {
            iItems.Add( aItem );
        }

        public bool Contains( string aThread )
        {
            bool ret = false;
            //
            foreach ( HeapReconstructorDataSourceThread item in iItems )
            {
                if ( item.Name == aThread )
                {
                    ret = true;
                    break;
                }
            }
            //
            return ret;
        }

        public int IndexOf( string aThread )
        {
            string thread = aThread.ToLower();
            int index = -1;
            //
            for ( int i = 0; i < iItems.Count; i++ )
            {
                HeapReconstructorDataSourceThread item = iItems[ i ];
                if ( item.Name.ToLower() == thread )
                {
                    index = i;
                    break;
                }
            }
            //
            return index;
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iItems.Count; }
        }

        public HeapReconstructorDataSourceThread this[ int aIndex ]
        {
            get { return iItems[ aIndex ]; }
        }
        #endregion

        #region IEnumerable<HeapReconstructorDataSourceThread> Members
        IEnumerator<HeapReconstructorDataSourceThread> IEnumerable<HeapReconstructorDataSourceThread>.GetEnumerator()
        {
            return new HeapReconstructorDataSourceThreadEnumerator( this );
        }
        #endregion

        #region IEnumerable Members
        IEnumerator IEnumerable.GetEnumerator()
        {
            return new HeapReconstructorDataSourceThreadEnumerator( this );
        }
        #endregion

        #region Data members
        private List<HeapReconstructorDataSourceThread> iItems = new List<HeapReconstructorDataSourceThread>();
        #endregion
    }

    #region Enumerator
    internal class HeapReconstructorDataSourceThreadEnumerator : IEnumerator<HeapReconstructorDataSourceThread>
    {
        #region Constructors & destructor
        public HeapReconstructorDataSourceThreadEnumerator( HeapReconstructorDataSourceThreadCollection aCollection )
        {
            iCollection = aCollection;
        }
        #endregion

        #region IEnumerator<HeapReconstructorDataSourceThread> Members
        HeapReconstructorDataSourceThread IEnumerator<HeapReconstructorDataSourceThread>.Current
        {
            get
            {
                return iCollection[ iCurrentIndex ];
            }
        }
        #endregion

        #region IDisposable Members
        void IDisposable.Dispose()
        {
        }
        #endregion

        #region IEnumerator Members
        object IEnumerator.Current
        {
            get
            {
                return iCollection[ iCurrentIndex ];
            }
        }

        bool IEnumerator.MoveNext()
        {
            return ( ++iCurrentIndex < iCollection.Count );
        }

        void IEnumerator.Reset()
        {
            iCurrentIndex = -1;
        }
        #endregion

        #region Data members
        private readonly HeapReconstructorDataSourceThreadCollection iCollection;
        private int iCurrentIndex = -1;
        #endregion
    }
    #endregion
}
