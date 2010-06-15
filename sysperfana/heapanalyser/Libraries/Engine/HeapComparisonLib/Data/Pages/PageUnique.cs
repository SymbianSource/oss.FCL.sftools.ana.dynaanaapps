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
using SymbianExcelUtils;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;

namespace HeapComparisonLib.Data.Pages
{
    internal class PageUnique : PageSimpleListing
    {
        #region Constructors & destructor
        public PageUnique( HeapReconstructor aReconstructor, HeapCellArrayWithStatistics aArray, Workbook aWorkbook, string aCaption, int aMasterCol, int aOtherCol )
            : base( aArray, aWorkbook, aCaption, aMasterCol, aOtherCol )
        {
            iReconstructor = aReconstructor;
        }
        #endregion

        #region API
        public override void SaveToPage()
        {
            base.SaveToPage();
        }

        protected override int MakeExtraColumns( int aRow )
        {
            Utils.SetValue( aRow, 5, Worksheet, "Program Counter" );
            Utils.SetValue( aRow, 6, Worksheet, "Link Register" );
            Utils.SetColumnHorizontalAlignment( 5, Worksheet, XlHAlign.xlHAlignLeft );
            Utils.SetColumnHorizontalAlignment( 6, Worksheet, XlHAlign.xlHAlignLeft );
            return 6;
        }

        protected override void OnTableComplete( int aLastRow )
        {
            Utils.AutoFitColumn( 5, Worksheet );
            Utils.AutoFitColumn( 6, Worksheet );
        }

        protected override void OnRowComplete( HeapCell aCell, int aRow, int aNextFreeColumn )
        {
            if ( iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses )
            {
                if ( aCell.IsUnknown && aCell.Type == HeapCell.TType.EAllocated )
                {
                    object[ , ] items = new object[ 1, 2 ];

                    if ( aCell.Symbol2 != null )
                    {
                        items[ 0, 0 ] = aCell.Symbol2.Name;
                    }
                    else
                    {
                        items[ 0, 0 ] = string.Empty;
                    }

                    if ( aCell.Symbol3 != null )
                    {
                        items[ 0, 1 ] = aCell.Symbol3.Name;
                    }
                    else
                    {
                        items[ 0, 1 ] = string.Empty;
                    }

                    Range range = Utils.GetRangeByColumnAndRow( aRow, 5, aRow, 6, Worksheet );
                    range.Value2 = items;
                }
            }
        }
        #endregion

        #region Data members
        private readonly HeapReconstructor iReconstructor;
        #endregion
    }
}
