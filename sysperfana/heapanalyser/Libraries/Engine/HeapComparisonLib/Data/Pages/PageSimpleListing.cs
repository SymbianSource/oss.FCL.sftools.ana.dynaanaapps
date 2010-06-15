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
using Excel = Microsoft.Office.Interop.Excel;
using SymbianExcelUtils;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;

namespace HeapComparisonLib.Data.Pages
{
    internal class PageSimpleListing : PageBase
    {
        #region Constructors & destructor
        public PageSimpleListing( HeapCellArrayWithStatistics aArray, Workbook aWorkbook, string aCaption )
            : this( aArray, aWorkbook, aCaption, true )
        {
        }

        public PageSimpleListing( HeapCellArrayWithStatistics aArray, Workbook aWorkbook, string aCaption, bool aMakeStats )
            : this( aArray, aWorkbook, aCaption, 1, 2 )
        {
            iMakeStats = aMakeStats;
        }

        public PageSimpleListing( HeapCellArrayWithStatistics aArray, Workbook aWorkbook, string aCaption, int aMasterCol, int aOtherCol )
            : base( aArray, aWorkbook, aCaption )
        {
            iMasterCol = aMasterCol;
            iOtherCol = aOtherCol;
            iMakeStats = true;
        }
        #endregion

        #region New framework
        protected virtual int MakeExtraColumns( int aRow )
        {
            return 0;
        }

        protected virtual void OnTableComplete( int aLastRow )
        {
        }

        protected virtual void OnRowComplete( HeapCell aCell, int aRow, int aNextFreeColumn )
        {
        }
        #endregion

        #region API
        public override void SaveToPage()
        {        
            int row = 1;
            if ( iMakeStats )
            {
                MakeStats( ref row );
            }

            Utils.SetValue( row, 1, Worksheet, "Alloc Cell Address" );
            Utils.SetValue( row, 2, Worksheet, "Original Length" );
            Utils.SetValue( row, 3, Worksheet, "New Length" );
            Utils.SetValue( row, 4, Worksheet, "Symbol" );

            // Make row bold
            Utils.BoldRow( row, Worksheet );

            // Add border and cell colours
            int maxCol = Math.Max( 4, MakeExtraColumns( row ) );
            Utils.MakeBoxedTitleRow( row, maxCol, Worksheet, 0xFF0000 );

            // Set column alignments
            Utils.SetColumnHorizontalAlignment( 2, Worksheet, XlHAlign.xlHAlignRight );
            Utils.SetColumnHorizontalAlignment( 3, Worksheet, XlHAlign.xlHAlignRight );
            Utils.SetColumnHorizontalAlignment( 4, Worksheet, XlHAlign.xlHAlignLeft );

            // Create rows for allocs
            row++;

            int startRowAlloc = row;
            foreach ( HeapCell cell in Array )
            {
                if ( cell.Type == HeapCell.TType.EAllocated )
                {
                    object[ , ] items = new object[ 1, 4 ];
                    //
                    items[ 0, 0 ] = "0x" + cell.Address.ToString( "x8" );

                    // Put the length in the master cell and then
                    // if we have a value for the other column, put it's value too
                    items[ 0, iMasterCol ] = cell.Length.ToString();
                    if ( cell.Tag != null && cell.Tag is HeapCell )
                    {
                        HeapCell newCell = (HeapCell) cell.Tag;
                        items[ 0, iOtherCol ] = newCell.Length;
                    }
                    else
                    {
                        items[ 0, iOtherCol ] = "?";
                    }

                    // Cell description
                    if ( !cell.IsUnknown )
                    {
                        items[ 0, 3 ] = cell.SymbolStringWithoutDescriptorPrefix;
                    }
                    else if ( cell.IsDescriptor )
                    {
                        items[ 0, 3 ] = cell.SymbolStringWithoutDescriptorPrefix;
                    }
                    else
                    {
                        StringBuilder body = new StringBuilder();
                        body.AppendFormat( "[?] I[{0:d3}] O[{1:d3}] {2}", cell.RelationshipManager.ReferencedBy.Count, cell.RelationshipManager.EmbeddedReferencesTo.Count, cell.RawItems.FirstLine );
                        items[ 0, 3 ] = body.ToString();
                    }
                    //
                    Range range = Utils.GetRangeByColumnAndRow( row, 1, row, 4, Worksheet );
                    range.Value2 = items;
                    OnRowComplete( cell, row, 5 );
                    ++row;
                }
            }

            // Sort allocations by symbol
            Excel.Range sortRange = Utils.GetRangeByColumnAndRow( startRowAlloc, 1, row, maxCol, Worksheet );
            sortRange.Sort(
                        Worksheet.Columns[ 4, Type.Missing ], Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending,
                        Type.Missing, Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending,
                        Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending, Microsoft.Office.Interop.Excel.XlYesNoGuess.xlNo,
                        Type.Missing, Type.Missing,
                        Microsoft.Office.Interop.Excel.XlSortOrientation.xlSortColumns,
                        Microsoft.Office.Interop.Excel.XlSortMethod.xlPinYin,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal );

            // Write totals
            ++row;
            Utils.SetValue( row, 1, Worksheet, "Total" );
            Utils.SetValue( row, 1 + iMasterCol, Worksheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( startRowAlloc, 1 + iMasterCol ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( row - 1, 1 + iMasterCol ) + ")" );
            Utils.SetValue( row, 1 + iOtherCol, Worksheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( startRowAlloc, 1 + iOtherCol ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( row - 1, 1 + iOtherCol ) + ")" );
            Utils.SetValue( row, 4, Worksheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 1 + iMasterCol ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 1 + iOtherCol ) );
            Utils.BoldRow( row, Worksheet );
            
            // Spacers
            row += 3;

            // Do the same for free cells
            Utils.SetValue( row, 1, Worksheet, "Free Cell Address" );
            Utils.SetValue( row, 2, Worksheet, "Original Length" );
            Utils.SetValue( row, 3, Worksheet, "New Length" );
            Utils.SetValue( row, 4, Worksheet, "Symbol" );

            // Make row bold
            Utils.BoldRow( row, Worksheet );

            // Add border and cell colours
            Utils.MakeBoxedTitleRow( row, 4, Worksheet, 0xFF0000 );

            // Create rows for frees
            ++row;
            int startRowFree = row;
            foreach ( HeapCell cell in Array )
            {
                if ( cell.Type == HeapCell.TType.EFree )
                {
                    object[ , ] items = new object[ 1, 4 ];
                    //
                    items[ 0, 0 ] = "0x" + cell.Address.ToString( "x8" );

                    // Put the length in the master cell and then
                    // if we have a value for the other column, put it's value too
                    items[ 0, iMasterCol ] = cell.Length.ToString();
                    if ( cell.Tag != null && cell.Tag is HeapCell )
                    {
                        HeapCell newCell = (HeapCell) cell.Tag;
                        items[ 0, iOtherCol ] = newCell.Length;
                    }
                    else
                    {
                        items[ 0, iOtherCol ] = "?";
                    }
                    items[ 0, 3 ] = cell.SymbolStringWithoutDescriptorPrefix;
                    //
                    Range range = Utils.GetRangeByColumnAndRow( row, 1, row, 4, Worksheet );
                    range.Value2 = items;
                    ++row;
                }
            }

            // Write totals
            ++row;
            Utils.SetValue( row, 1, Worksheet, "Total" );
            Utils.SetValue( row, 1 + iMasterCol, Worksheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( startRowFree, 1 + iMasterCol ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( row - 1, 1 + iMasterCol ) + ")" );
            Utils.SetValue( row, 1 + iOtherCol, Worksheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( startRowFree, 1 + iOtherCol ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( row - 1, 1 + iOtherCol ) + ")" );
            Utils.SetValue( row, 4, Worksheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 1 + iMasterCol ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 1 + iOtherCol ) );
            Utils.BoldRow( row, Worksheet );

            // Auto fit the columns
            AutoFitColumns( 1, 4 );
            OnTableComplete( row );
        }
        #endregion

        #region Internal methods
        private void MakeStats( ref int aRow )
        {
            Utils.SetValue( aRow, 1, Worksheet, "Type" );
            Utils.SetValue( aRow, 2, Worksheet, "Amount" );

            // Make row bold
            Utils.BoldRow( aRow, Worksheet );

            // Add border and cell colours
            Utils.MakeBoxedTitleRow( aRow, 2, Worksheet, 0xFF0000 );

            // Set column alignments
            Utils.SetColumnHorizontalAlignment( 2, Worksheet, XlHAlign.xlHAlignRight );

            // Spacer
            aRow++;
            int firstRealRow = aRow;
            
            HeapStatistics stats = Array.Statistics;

            {
                Utils.SetValue( aRow, 1, Worksheet, "Total" );
                Utils.SetValue( aRow, 2, Worksheet, stats.SizeTotal.ToString() );
            }

            // Spacer
            aRow++;

            {
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Allocated Space" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TypeSize.ToString() );
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Allocated Cell Count" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TypeCount.ToString() );
            }

            // Spacer
            aRow++;

            {
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "\"Unknown\" Allocated Space" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerUnknown.AssociatedMemory.ToString() );
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "\"Unknown\" Allocated Cell Count" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerUnknown.AssociatedCellCount.ToString() );
            }

            // Spacer
            aRow++;

            {
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Descriptor Allocated Space" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerDescriptors.AssociatedMemory.ToString() );
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Descriptor Allocated Cell Count" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerDescriptors.AssociatedCellCount.ToString() );
            }

            // Spacer
            aRow++;

            {
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "\"With Symbols\" Allocated Space" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerSymbols.TypeSize.ToString() );
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "\"With Symbols\" Allocated Cell Count" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsAllocated.TrackerSymbols.CellMatchCount.ToString() );
            }

            // Spacer
            aRow++;
            int freeaRow = aRow;

            {
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Free Space" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsFree.TypeSize.ToString() );
                aRow++;
                Utils.SetValue( aRow, 1, Worksheet, "Free Cell Count" );
                Utils.SetValue( aRow, 2, Worksheet, stats.StatsFree.TypeCount.ToString() );
            }

            Utils.GetRangeByColumnAndRow( firstRealRow, 1, aRow, 1, Worksheet ).Font.Bold = true;

            // Spacer
            aRow++;
            aRow++;
        }
        #endregion

        #region Data members
        private readonly int iMasterCol;
        private readonly int iOtherCol;
        private readonly bool iMakeStats;
        #endregion
    }
}
