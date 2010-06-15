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
using System.Collections.Generic;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
    internal class CSVThread
    {
        #region Enumerations
        public enum TFileFormatVersion
        {
            EFileFormatVersion1 = 1,
            EFileFormatVersion2,
        }
        #endregion

        #region Static constructor
        static public CSVThread New()
        {
            CSVThread ret = new CSVThread();
            return ret;
        }

		static public CSVThread NewDefault( string aThreadName )
		{
			CSVThread ret = new CSVThread();
			ret.iThreadName = aThreadName;
			ret.iIsDefault = true;
			return ret;
		}
		#endregion

        #region Constructor
		private CSVThread()
		{
		}
		#endregion

        #region API
        public void AddThreadWithCommonHeapChunkHandle( CSVThread aOtherThread )
        {
            if ( aOtherThread != this )
            {
                if ( iThreadsWithCommonHeapChunk == null )
                {
                    iThreadsWithCommonHeapChunk = new List<CSVThread>();
                }
                iThreadsWithCommonHeapChunk.Add( aOtherThread );
            }
        }
        #endregion

        #region Properties
        public TFileFormatVersion Version
        {
            get
            {
                TFileFormatVersion ret = TFileFormatVersion.EFileFormatVersion1;
                if ( ProcessName != string.Empty && ThreadId != 0 )
                {
                    ret = TFileFormatVersion.EFileFormatVersion2;
                }
                return ret;
            }
        }

        public bool IsDefault { get { return iIsDefault; } set { iIsDefault = value; } }
        public bool IsSharedHeap
        { 
            get 
            { 
                return iIsSharedHeap; 
            }
            set { iIsSharedHeap = value; } 
        }

        public string FullName
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                if ( Version == TFileFormatVersion.EFileFormatVersion2 )
                {
                    ret.AppendFormat( "{0}::{1}", ProcessName, ThreadName );
                }
                else
                {
                    // Old format doesn't support process name
                    ret.Append( ThreadName );
                }
                //
                return ret.ToString();
            }
        }

        public string ThreadName { get { return iThreadName; } set { iThreadName = value; } }
        public string ProcessName { get { return iProcessName; } set { iProcessName = value; } }
        public string ChunkName { get { return iChunkName; } set { iChunkName = value; } }

        public long ThreadId { get { return iThreadId; } set { iThreadId = value; } }

        public long ChunkHandle { get { return iChunkHandle; } set { iChunkHandle = value; } }
        public long ChunkBaseAddress { get { return iChunkBaseAddress; } set { iChunkBaseAddress = value; } }

        public long SizeCurrent { get { return iSizeCurrent; } set { iSizeCurrent = value; } }
        public long SizeMin { get { return iSizeMin; } set { iSizeMin = value; } }
        public long SizeMax { get { return iSizeMax; } set { iSizeMax = value; } }

        public long FirstFreeCellAddress { get { return iFirstFreeCellAddress; } set { iFirstFreeCellAddress = value; } }
        public long FirstFreeCellLength { get { return iFirstFreeCellLength; } set { iFirstFreeCellLength = value; } }

        public long FreeCellCount { get { return iFreeCellCount; } set { iFreeCellCount = value; } }
        public long FreeCellLargest { get { return iFreeCellLargest; } set { iFreeCellLargest = value; } }
        public long FreeSpaceTotal { get { return iFreeSpaceTotal; } set { iFreeSpaceTotal = value; } }
        public long FreeSpaceSlack { get { return iFreeSpaceSlack; } set { iFreeSpaceSlack = value; } }

        public long AllocCellLargest { get { return iAllocCellLargest; } set { iAllocCellLargest = value; } }
        public long AllocCellCount { get { return iAllocCellCount; } set { iAllocCellCount = value; } }
        public long AllocSpaceTotal { get { return iAllocSpaceTotal; } set { iAllocSpaceTotal = value; } }

        public long MinCellSize { get { return iMinCellSize; } set { iMinCellSize = value; } }
        #endregion

		#region From System.Object
		public override string ToString()
		{
			return iThreadName;
		}

		public override Int32 GetHashCode()
		{
			return iThreadName.GetHashCode();
		}
		#endregion

		#region Internal methods
		private static long ParseHexValue( string aItem )
		{
			long ret = 0;
			//
			if	( aItem.Length > 0 )
			{
				const string KHexPrefix = "0x";
				if	( aItem.IndexOf( KHexPrefix ) == 0 )
				{
					aItem = aItem.Substring( KHexPrefix.Length );
				}

				ret = System.Convert.ToInt32( aItem, 16 );
			}
			//
			return ret;
		}

		private static long ParseDecimalValue( string aItem )
		{
			long ret = 0;
			//
			if	( aItem.Length > 0 ) 
			{
				ret = System.Convert.ToInt32( aItem );
			}
			//
			return ret;
		}
		#endregion

        #region Internal constants
        private const int KExpectedItemCount = 16;
        private const string KRDebugLinePrefix = "[Heap Summary] ";
        #endregion

        #region Data members
        private bool iIsDefault = false;
        private bool iIsSharedHeap = false;
        
        private string iThreadName = string.Empty;
        private string iProcessName = string.Empty;
        private string iChunkName = string.Empty;

        private long iThreadId = 0;

        private long iChunkHandle = 0;
        private long iChunkBaseAddress = 0;

        private long iSizeCurrent = 0;
        private long iSizeMin = 0;
        private long iSizeMax = 0;

        private long iFirstFreeCellAddress = 0;
        private long iFirstFreeCellLength = 0;
        //
        private long iFreeCellCount = 0;
        private long iFreeCellLargest = 0;

        private long iFreeSpaceTotal = 0;
        private long iFreeSpaceSlack = 0;

        private long iAllocCellLargest = 0;
        private long iAllocCellCount = 0;
        private long iAllocSpaceTotal = 0;

        private long iMinCellSize = 0;

        private List<CSVThread> iThreadsWithCommonHeapChunk;
		#endregion
	}
}
