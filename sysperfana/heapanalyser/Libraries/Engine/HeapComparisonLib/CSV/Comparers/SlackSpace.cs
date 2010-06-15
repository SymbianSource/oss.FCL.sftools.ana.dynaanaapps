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
    internal class SlackSpace : ComparerBase
	{
		#region Constructors & destructor
		public SlackSpace( Worksheet aSheet, string aFileName1, string aFileName2 )
		:	base( aSheet, aFileName1, aFileName2 )
		{
		}
		#endregion

		#region API
		public override string DeltaFormula( int aIndex )
		{
			int column = 4;
			return Utils.MakeSummationFormula( 3, Row - 2, column, Sheet );
		}

		public override void PrepareWorksheetReadyForData()
		{
			Sheet.Name = "Slack Space";
			//
			Range r = null;
			//
			Utils.SetValue( Row, 1, Sheet, "Thread" );
			r = Utils.SetValue( Row, 2, Sheet, "Slack Space" );
			r.AddComment( FileName1 );
			r = Utils.SetValue( Row, 3, Sheet, "Slack Space" );
			r.AddComment( FileName2 );
			Utils.SetValue( Row, 4, Sheet, "Delta" );
			//
			Utils.MakeBoxedTitleRow( Row, 4, Sheet, 0xFF0000 );
			//
			NextRow();
			NextRow();
		}

		public override void FinaliseWorksheet()
		{
			// Make totals
			NextRow();
			Utils.SetValue( Row, 1, Sheet, "Totals:" );
			Utils.SetValue( Row, 2, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 2) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 2) + ")" );
			Utils.SetValue( Row, 3, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 3) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 3) + ")" );
			Utils.SetValue( Row, 4, Sheet, DeltaFormula( 0 ) );
			Utils.BoldRow( Row, Sheet );

			// Calculate column sizes
			Utils.AutoFitColumn( 1, Sheet, 70.0 );
			Utils.AutoFitColumn( 2, Sheet );
			Utils.AutoFitColumn( 3, Sheet );
			Utils.AutoFitColumn( 4, Sheet );
		}

        public override void Compare( CSVThread aMaster, CSVThread aOther )
		{
            Utils.SetValue( Row, 1, Sheet, aMaster.FullName );

			if	( !aMaster.IsDefault )
				Utils.SetValue( Row, 2, Sheet, aMaster.FreeSpaceSlack.ToString() );
			if	( !aOther.IsDefault )
				Utils.SetValue( Row, 3, Sheet, aOther.FreeSpaceSlack.ToString() );
			//
			if	( aMaster.IsDefault || aOther.IsDefault )
			{
				Utils.GetRangeByColumnAndRow( Row, 1, Row, 1, Sheet ).Font.Bold = true;
			}

			long delta = aMaster.FreeSpaceSlack - aOther.FreeSpaceSlack;
			Utils.SetValue( Row, 4, Sheet, ( delta * -1).ToString() );

			// Colourise line according to difference
			int colour = 0x000000; // black
			if	( aOther.FreeSpaceSlack > aMaster.FreeSpaceSlack )
			{
				colour = 0x0000FF; // red
			}
			else if ( aOther.FreeSpaceSlack < aMaster.FreeSpaceSlack )
			{
				colour = 0xFF0000; // blue
			}
			Utils.SetRangeFontColour( Row, 4, Row, 4, Sheet, colour );
			//
			NextRow();
		}
		#endregion
	}
}
