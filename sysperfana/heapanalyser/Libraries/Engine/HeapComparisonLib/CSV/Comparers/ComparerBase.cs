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
using System.Runtime.InteropServices;
using Microsoft.Office.Interop.Excel;
using SymbianExcelUtils;

namespace HeapComparisonLib.CSV.Comparers
{
    internal abstract class ComparerBase
	{
		#region Constructors & destructor
		protected ComparerBase( Worksheet aSheet, string aFileName1, string aFileName2 )
		{
			iWorksheet = aSheet;
			iFileName1 = aFileName1;
			iFileName2 = aFileName2;
		}
		#endregion

		#region API
		public abstract void PrepareWorksheetReadyForData();
		public abstract void FinaliseWorksheet();
        public abstract void Compare( CSVThread aOther, CSVThread aMaster );

		public virtual string DeltaFormula( int aIndex )
		{
			return string.Empty;
		}
		#endregion

		#region Internal Properties
		protected int Row
		{
			get { return iRowNumber; }
		}

		protected Worksheet Sheet
		{
			get { return iWorksheet; }
		}

		protected string FileName1
		{
			get { return iFileName1; }
		}

		protected string FileName2
		{
			get { return iFileName2; }
		}
		#endregion

		#region Internal methods
		protected void NextRow()
		{
			++iRowNumber;
		}

        protected void CompareAndWriteItems( CSVThread aItem1, long aItemVal1, CSVThread aItem2, long aItemVal2, int aColumn, int aGrowColour, int aShrinkColour )
		{
			// First write the values to the columns
			if	( !aItem1.IsDefault )
				Utils.SetValue( Row, aColumn   , Sheet, aItemVal1.ToString() );
			if	( !aItem2.IsDefault )
				Utils.SetValue( Row, aColumn + 1, Sheet, aItemVal2.ToString() );

			// Delta
			long delta = aItemVal1 - aItemVal2;
			Utils.SetValue( Row, aColumn + 2, Sheet, ( delta * -1).ToString() );

			// Colour code accordingly
			int colour = 0x000000; // black
			if	( aItemVal1 < aItemVal2 )
			{
				colour = aGrowColour; // 0x0000FF; // red
			}
			else if ( aItemVal1 > aItemVal2 )
			{
				colour = aShrinkColour; // 0x00FF00; // green
			}
			Utils.SetRangeFontColour( Row, aColumn + 2, Row, aColumn + 2, Sheet, colour );
		}
		#endregion

		#region Data members
		private readonly string iFileName1;
		private readonly string iFileName2;
		private readonly Worksheet iWorksheet;
		private int iRowNumber = 1;
		#endregion
	}
}
