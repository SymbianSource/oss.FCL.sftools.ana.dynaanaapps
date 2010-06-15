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
    internal class LargestCells : ComparerBase
	{
		#region Constructors & destructor
		public LargestCells( Worksheet aSheet, string aFileName1, string aFileName2 )
		:	base( aSheet, aFileName1, aFileName2 )
		{
		}
		#endregion

		#region API
		public override void PrepareWorksheetReadyForData()
		{
			Sheet.Name = "Largest Cells by Type";
			//
			Range r = null;
			//
			Utils.SetValue( Row,  1, Sheet, "Thread" );
			r = Utils.SetValue( Row,  2, Sheet, "Largest Free Cell Size" );
			r.AddComment( FileName1 );
			r = Utils.SetValue( Row,  3, Sheet, "Largest Free Cell Size" );
			r.AddComment( FileName2 );
			Utils.SetValue( Row,  4, Sheet, "Delta" );
			//
			Utils.SetValue( Row,  5, Sheet, " " );
			//
			r = Utils.SetValue( Row,  6, Sheet, "Largest Alloc Cell Size" );
			r.AddComment( FileName1 );
			r = Utils.SetValue( Row,  7, Sheet, "Largest Alloc Cell Size" );
			r.AddComment( FileName2 );
			Utils.SetValue( Row,  8, Sheet, "Delta" );
			//
			Utils.MakeBoxedTitleRow( Row, 8, Sheet, 0xFF0000 );
			//
			NextRow();
			NextRow();
		}

		public override void FinaliseWorksheet()
		{
			Utils.AutoFitColumn( 1, Sheet, 70.0 );
			for( int col=2; col<=8; col++ )
			{
				Utils.AutoFitColumn( col, Sheet );
			}
		}

        public override void Compare( CSVThread aMaster, CSVThread aOther )
		{
            Utils.SetValue( Row, 1, Sheet, aMaster.FullName );
			//
			if	( aMaster.IsDefault || aOther.IsDefault )
			{
				Utils.GetRangeByColumnAndRow( Row, 1, Row, 1, Sheet ).Font.Bold = true;
			}
			//
			CompareAndWriteItems( aMaster, aMaster.FreeCellLargest, aOther, aOther.FreeCellLargest,  2, 0xFF0000, 0x0000FF );
			CompareAndWriteItems( aMaster, aMaster.AllocCellLargest, aOther, aOther.AllocCellLargest, 6, 0x0000FF, 0xFF0000 );
			//
			NextRow();
		}
		#endregion
	}
}
