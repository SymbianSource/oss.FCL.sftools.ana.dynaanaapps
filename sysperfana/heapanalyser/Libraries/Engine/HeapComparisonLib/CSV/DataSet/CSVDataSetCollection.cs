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
using System.Collections;
using System.Collections.Generic;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
    internal class CSVDataSetCollection
	{
		#region Constructors & destructor
        public CSVDataSetCollection()
        {
        }
		#endregion

		#region API
        internal void Remove( int aIndex )
        {
            iDataSets.RemoveAt( aIndex );
        }

        internal void RemoveEmptyDataSets()
        {
            for ( int i = iDataSets.Count - 1; i >= 0; i-- )
            {
                CSVDataSet set = iDataSets[ i ];
                if ( set.Count == 0 )
                {
                    iDataSets.RemoveAt( i );
                }
            }
        }

		internal void Add( CSVDataSet aDataSet )
		{
            iDataSets.Add( aDataSet );
		}

        internal bool Contains( CSVDataSet aDataSet )
        {
            Predicate<CSVDataSet> findByFileAndLine = delegate( CSVDataSet setToCheck )
            {
                return ( setToCheck.FileName == aDataSet.FileName ) &&
                       ( setToCheck.LineNumber == aDataSet.LineNumber );
            };

            bool ret = iDataSets.Exists( findByFileAndLine );
            return ret;
        }

        internal void SortByTimestamp()
        {
            Comparison<CSVDataSet> comparer = delegate( CSVDataSet aLeft, CSVDataSet aRight )
            {
                int ret = -1;
                //
                if ( aLeft.TimeStamp == aRight.TimeStamp )
                {
                    ret = 0;
                }
                else if ( aLeft.TimeStamp > aRight.TimeStamp )
                {
                    ret = 1;
                }
                //
                return ret;
            };

            iDataSets.Sort( comparer );
        }
        #endregion

        #region Properties
        public int Count
        {
            get { return iDataSets.Count; }
        }

        public CSVDataSet this[ int aIndex ]
		{
            get
            {
			    return iDataSets[ aIndex ];
            }
        }
        #endregion

		#region Data members
        private List<CSVDataSet> iDataSets = new List<CSVDataSet>();
		#endregion
    }
}
