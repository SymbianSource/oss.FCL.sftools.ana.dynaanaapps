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
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
	internal class CSVDataSet : IComparer<CSVThread>
	{
		#region Constructors & destructor
        public CSVDataSet( string aFileName, long aLineNumber )
        {
            iOriginalFileName = aFileName;
            iLineNumber = aLineNumber;
        }

        public CSVDataSet( CSVDataSet aCopy )
        {
            iOriginalFileName = aCopy.OriginalFileName;
            iLineNumber = aCopy.LineNumber;

            foreach ( KeyValuePair<string, CSVThread> kvp in aCopy.iEntries )
            {
                iEntries.Add( kvp.Key, kvp.Value );
            }
        }
		#endregion

		#region API
        public void Add( CSVThread aThread )
        {
            if ( !iEntries.ContainsKey( aThread.FullName ) )
            {
                iEntries.Add( aThread.FullName, aThread );
            }
        }

        public void Remove( CSVThread aThread )
        {
            if ( aThread.FullName != string.Empty && iEntries.ContainsKey( aThread.FullName ) )
            {
                iEntries.Remove( aThread.FullName );
            }
        }

		public void RemoveByThreadName( string aName )
		{
			if	( iEntries[ aName ] != null )
			{
				iEntries.Remove( aName );
			}
		}

        public void FindSharedHeaps()
        {
            foreach ( KeyValuePair<string, CSVThread> kvp in iEntries )
            {
                LookForOtherUsesOfChunkHandle( kvp.Value );
            }
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iEntries.Count; }
        }

        public string OriginalFileName
        {
            get { return iOriginalFileName; }
        }

        public string FileNameAndPath
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                ret.Append( Path.GetDirectoryName( iOriginalFileName ) + Path.DirectorySeparatorChar );
                ret.Append( Path.GetFileNameWithoutExtension( iOriginalFileName ) );
                ret.AppendFormat( "_{0:d8}{1}", LineNumber, Path.GetExtension( iOriginalFileName ) );
                //
                return ret.ToString(); 
            }
        }

        public string FileName
        {
            get
            {
                return Path.GetFileNameWithoutExtension( FileNameAndPath );
            }
        }

        public long LineNumber
        {
            get { return iLineNumber; }
        }

        public long TimeStamp
        {
            get { return iTimeStamp; }
            set { iTimeStamp = value; }
        }

        public CSVThread this[ string aName ]
		{
            get
            {
                CSVThread ret = null;
                //
                if ( iEntries.ContainsKey( aName ) )
                {
                    ret = iEntries[ aName ];
                }
                //
                return ret;
            }
		}

        public CSVThread this[ int aIndex ]
        {
            get
            {
                string thread = iEntries.Keys[ aIndex ];
                return this[ thread ];
            }
        }
        #endregion

        #region Internal methods
        private void LookForOtherUsesOfChunkHandle( CSVThread aMaster )
        {
            if ( aMaster.Version != CSVThread.TFileFormatVersion.EFileFormatVersion1 )
            {
                foreach ( KeyValuePair<string, CSVThread> kvp in iEntries )
                {
                    CSVThread thread = kvp.Value;
                    if ( thread != aMaster )
                    {
                        if ( thread.ChunkHandle == aMaster.ChunkHandle )
                        {
                            thread.IsSharedHeap = true;
                            thread.AddThreadWithCommonHeapChunkHandle( aMaster );
                        }
                    }
                }
            }
        }
        #endregion

        #region IComparer Members
        public int Compare( CSVThread aLeft, CSVThread aRight )
		{
            return aLeft.FullName.CompareTo( aRight.FullName );
		}
		#endregion

		#region Data members
		private string iOriginalFileName = string.Empty;
        private long iLineNumber;
        private long iTimeStamp;
        private SortedList<string, CSVThread> iEntries = new SortedList<string, CSVThread>();
		#endregion
	}
}
