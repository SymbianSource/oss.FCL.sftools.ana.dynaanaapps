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
using System.Collections.Generic;
using System.Text;
using Microsoft.Office.Interop.Excel;
using HeapLib.Cells;
using HeapLib.Array;
using SymbianExcelUtils;

namespace HeapComparisonLib.Data.Pages
{
    internal abstract class PageBase
    {
        #region Constructors & destructor
        public PageBase( HeapCellArrayWithStatistics aArray, Workbook aWorkbook, string aSheetName )
        {
            iArray = aArray;
            iWorkbook = aWorkbook;
            iWorksheet = (Worksheet) iWorkbook.Sheets.Add( iWorkbook.Sheets.get_Item( 1 ), Type.Missing, 1, XlSheetType.xlWorksheet );
            iWorksheet.Name = aSheetName;
        }
        #endregion

        #region Properties
        public HeapCellArrayWithStatistics Array
        {
            get { return iArray; }
        }

        public Workbook Workbook
        {
            get { return iWorkbook; }
        }

        public Worksheet Worksheet
        {
            get { return iWorksheet; }
        }
        #endregion

        #region API
        public abstract void SaveToPage();
        #endregion

        #region Internal methods
        protected void AutoFitColumns( int aStart, int aEnd )
        {
            iWorksheet.Activate();

            for ( int i = aStart; i <= aEnd; i++ )
            {
                Utils.AutoFitColumnNoMax( i, Worksheet );
            }
        }
        #endregion

        #region Data members
        private readonly HeapCellArrayWithStatistics iArray;
        private readonly Workbook iWorkbook;
        private readonly Worksheet iWorksheet;
        #endregion
    }
}
