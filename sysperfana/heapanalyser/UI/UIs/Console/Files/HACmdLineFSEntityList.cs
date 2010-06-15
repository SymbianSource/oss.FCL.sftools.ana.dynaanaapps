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
using System.IO;
using System.Collections.Generic;

namespace HeapAnalyser.UIs.Console.Files
{
    internal class HACmdLineFSEntityList<T> : IEnumerable<T> where T : HACmdLineFSEntity, new()
	{
        #region Constructors & destructor
        public HACmdLineFSEntityList()
		{
		}
        #endregion

		#region API
        public void Add( FileInfo aFile )
        {
            T entry = new T();
            entry.File = aFile;
            iFiles.Add( entry );
        }

        public void Add( DirectoryInfo aDir )
        {
            T entry = new T();
            entry.Directory = aDir;
            iFiles.Add( entry );
        }

        public void AddRange( FileInfo[] aFiles )
        {
            foreach ( FileInfo file in aFiles )
            {
                Add( file );
            }
        }

        public bool Contains( string aFileName )
        {
            HACmdLineFSEntity ret = this[ aFileName ]; 
            return ret != null;
        }
        #endregion

		#region Properties
        public int Count
        {
            get { return iFiles.Count; }
        }

        public T this[ int aIndex ]
        {
            get { return iFiles[ aIndex ]; }
        }

        public T this[ string aFileName ]
        {
            get
            {
                T ret = iFiles.Find(
                   delegate( T file )
                   {
                       return file.Name.ToUpper() == aFileName.ToUpper();
                   }
                );
                return ret;
            }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Operators
        public static implicit operator string[]( HACmdLineFSEntityList<T> aList )
        {
            List<string> ret = new List<string>();
            foreach ( T file in aList )
            {
                ret.Add( file.Name );
            }
            return ret.ToArray();
        }
        #endregion

        #region IEnumerable<T> Members
        public IEnumerator<T> GetEnumerator()
        {
            foreach ( T file in iFiles )
            {
                yield return file;
            }
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            IEnumerator<T> self = (IEnumerator<T>) this;
            System.Collections.IEnumerator ret = (System.Collections.IEnumerator) self;
            return ret;
        }
        #endregion

        #region Member data
        private List<T> iFiles = new List<T>();
        #endregion
    }
}
