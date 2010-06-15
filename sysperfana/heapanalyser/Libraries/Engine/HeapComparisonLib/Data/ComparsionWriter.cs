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
using System.Threading;
using System.Globalization;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils;
using SymbianUtils.Range;
using SymbianUtils.RawItems;
using SymbianUtils.TextUtilities.Readers.Types.Array;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using System.Runtime.InteropServices;
using Microsoft.Office.Interop.Excel;
using Excel = Microsoft.Office.Interop.Excel;
using HeapComparisonLib.Data.Pages;
using SymbianExcelUtils;
using HeapLib.Statistics.Types;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapLib.Statistics.Tracking.Base;

namespace HeapComparisonLib.Data
{
    public class ComparisonWriter : AsyncTextWriterBase
    {
        #region Constructors
        public ComparisonWriter( ComparsionEngine aEngine, string aFileName )
        {
            iEngine = aEngine;
            iFileName = aFileName;
        }
        #endregion

        #region API
        public void SaveAsExcel()
        {
            AsyncWrite();
        }
        #endregion

        #region Properties
        #endregion

        #region From AsyncWriterBase
        protected override void HandleWriteStarted()
        {
            // There are problems with exporting to excel on non-English machines
            // See: http://www.made4dotnet.com/Default.aspx?tabid=141&aid=15
            iOriginalCulture = System.Threading.Thread.CurrentThread.CurrentCulture;
            ChangeToEnglishCulture();

            iExcelApp = new Microsoft.Office.Interop.Excel.Application();
            iExcelApp.DisplayAlerts = false;

            Workbooks workbooks = iExcelApp.Workbooks;
            workbooks.Add( XlWBATemplate.xlWBATWorksheet );
            iWorkbook = workbooks.get_Item( workbooks.Count );

            // Create summary sheet
            Sheets sheets = iWorkbook.Worksheets;
            iSummary = (Worksheet) sheets.get_Item( 1 );
            CreateSummary( iSummary );

            // Create pages
            iPages.Add( new Pages.PageUnchanged( iEngine.ResultsUnchanged, iWorkbook ) );
            iPages.Add( new Pages.PageUnique( iEngine.Reconstructor2, iEngine.ResultsUniqueInHeap2, iWorkbook, "Unique in Heap 2", 2, 1 ) );
            iPages.Add( new Pages.PageUnique( iEngine.Reconstructor1, iEngine.ResultsUniqueInHeap1, iWorkbook, "Unique in Heap 1", 1, 2 ) );
            iPages.Add( new Pages.PageSimpleListing( iEngine.ResultsUnchangedLengthButDifferentContents, iWorkbook, "Same length, different contents" ) );
            iPages.Add( new Pages.PageSimpleListing( iEngine.ResultsChanged, iWorkbook, "Changed", false ) );
        }

        protected override void HandleWriteCompleted()
        {
            try
            {
                base.HandleWriteCompleted();

                // Populate summary sheet
                FinaliseSummary( iSummary );

                // Activate summary
                ( (Microsoft.Office.Interop.Excel._Worksheet) iSummary ).Activate();

                // Save and close workbook
                SaveFile();

                // Exit Excel
                SymbianExcelUtils.Utils.CloseExcel( iExcelApp );
            }
            catch
            {
            }
            finally
            {
                RestoreOriginalCulture();
            }
        }

        public override void ExportData()
        {
            PageBase page = null;
            //
            lock ( this )
            {
                if ( iCurrentPageIndex < iPages.Count )
                {
                    page = iPages[ iCurrentPageIndex++ ];
                }
            }
            //
            if ( page != null )
            {
                page.SaveToPage();
            }
        }

        public override long Size
        {
            get
            {
                long size = 0;
                lock ( iPages )
                {
                    size = iPages.Count;
                }
                return size;
            }
        }

        public override long Position
        {
            get
            {
                long pos = 0;
                lock ( this )
                {
                    pos = iCurrentPageIndex;
                }
                return pos;
            }
        }
        #endregion

        #region Internal methods
        private void SaveFile()
        {
            // Delete any existing file with the same name
            System.IO.FileInfo fileInfo = new System.IO.FileInfo( iFileName );
            if ( fileInfo.Exists )
            {
                fileInfo.Delete();
            }

            // Save and tidy up
            try
            {
                iWorkbook.SaveAs( iFileName,
								  XlFileFormat.xlWorkbookNormal,
								  Type.Missing,
								  Type.Missing,
								  Type.Missing,
								  Type.Missing,
								  XlSaveAsAccessMode.xlNoChange,
								  Type.Missing,
								  Type.Missing,
								  Type.Missing,
								  Type.Missing,
								  Type.Missing );
            }
            catch ( System.IO.IOException )
            {
            }

            iWorkbook.Close( false, Type.Missing, Type.Missing );
        }

        private void CreateSummary( Worksheet aSheet )
        {
            aSheet.Name = "Summary";
            //
            int row = 0;
            //
            row++;
            Utils.SetValue( row, 2, aSheet, "Heap 1" ).AddComment( "From: " + iEngine.Reconstructor1.SourceData.FileName );
            Utils.SetValue( row, 3, aSheet, "Heap 2" ).AddComment( "From: " + iEngine.Reconstructor2.SourceData.FileName );
            Utils.SetValue( row, 4, aSheet, "Delta" );
            Utils.MakeBoxedTitleRow( 1, 4, aSheet, 0xFF0000 );
        }

        private void FinaliseSummary( Worksheet aSheet )
        {
            int row = 1;

            {
                long heapSize1 = iEngine.Reconstructor1.Statistics.HeapSize;
                long heapSize2 = iEngine.Reconstructor2.Statistics.HeapSize;
                row++;
                Utils.SetValue( row, 1, aSheet, "Heap Chunk Size" );
                Utils.SetValue( row, 2, aSheet, heapSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );
            }

            // Spacer
            row++;

            {
                long heapAllocSize1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TypeSize;
                long heapAllocSize2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TypeSize;
                row++;
                Utils.SetValue( row, 1, aSheet, "Allocated Space" );
                Utils.SetValue( row, 2, aSheet, heapAllocSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                long heapAllocCount1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TypeCount;
                long heapAllocCount2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TypeCount;
                row++;
                Utils.SetValue( row, 1, aSheet, "Allocated Cell Count" );
                Utils.SetValue( row, 2, aSheet, heapAllocCount1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocCount2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );
            }

            // Spacer
            row++;

            {
                long heapAllocUnknownSize1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerUnknown.AssociatedMemory;
                long heapAllocUnknownSize2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerUnknown.AssociatedMemory;
                row++;
                Utils.SetValue( row, 1, aSheet, "\"Unknown\" Allocated Space" );
                Utils.SetValue( row, 2, aSheet, heapAllocUnknownSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocUnknownSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                long heapAllocUnknownCount1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerUnknown.AssociatedCellCount;
                long heapAllocUnknownCount2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerUnknown.AssociatedCellCount;
                row++;
                Utils.SetValue( row, 1, aSheet, "\"Unknown\" Allocated Cell Count" );
                Utils.SetValue( row, 2, aSheet, heapAllocUnknownCount1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocUnknownCount2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );
            }

            // Spacer
            row++;

            {
                long heapAllocDesSize1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerDescriptors.AssociatedMemory;
                long heapAllocDesSize2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerDescriptors.AssociatedMemory;
                row++;
                Utils.SetValue( row, 1, aSheet, "Descriptor Allocated Space" );
                Utils.SetValue( row, 2, aSheet, heapAllocDesSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocDesSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                long heapAllocDesCount1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount;
                long heapAllocDesCount2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount;
                row++;
                Utils.SetValue( row, 1, aSheet, "Descriptor Allocated Cell Count" );
                Utils.SetValue( row, 2, aSheet, heapAllocDesCount1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapAllocDesCount2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                /*
                using ( System.IO.StreamWriter w1 = new System.IO.StreamWriter( "O:\\desc1.txt", false ) )
                {
                    foreach ( HeapCell c1 in iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerDescriptors )
                    {
                        w1.WriteLine( c1.ToStringExtended() + " " + c1.DescriptorTextBeautified );
                    }
                }
                using ( System.IO.StreamWriter w2 = new System.IO.StreamWriter( "O:\\desc2.txt", false ) )
                {
                    foreach ( HeapCell c2 in iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerDescriptors )
                    {
                        w2.WriteLine( c2.ToStringExtended() + " " + c2.DescriptorTextBeautified );
                    }
                }
                */
            }

            // Spacer
            row++;

            {
                long typeSize1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerSymbols.TypeSize;
                long typeSize2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerSymbols.TypeSize;
                row++;
                Utils.SetValue( row, 1, aSheet, "\"With Symbols\" Allocated Space" );
                Utils.SetValue( row, 2, aSheet, typeSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, typeSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                long typeCount1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerSymbols.CellMatchCount;
                long typeCount2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerSymbols.CellMatchCount;
                row++;
                Utils.SetValue( row, 1, aSheet, "\"With Symbols\" Allocated Cell Count" );
                Utils.SetValue( row, 2, aSheet, typeCount1.ToString() );
                Utils.SetValue( row, 3, aSheet, typeCount2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );
            }

            // Spacer
            row++;
            int freeRow = row;

            {
                long heapFreeSize1 = iEngine.Reconstructor1.Statistics.StatsFree.TypeSize;
                long heapFreeSize2 = iEngine.Reconstructor2.Statistics.StatsFree.TypeSize;
                row++;
                Utils.SetValue( row, 1, aSheet, "Free Space" );
                Utils.SetValue( row, 2, aSheet, heapFreeSize1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapFreeSize2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );

                long heapFreeCount1 = iEngine.Reconstructor1.Statistics.StatsFree.TypeCount;
                long heapFreeCount2 = iEngine.Reconstructor2.Statistics.StatsFree.TypeCount;
                row++;
                Utils.SetValue( row, 1, aSheet, "Free Cell Count" );
                Utils.SetValue( row, 2, aSheet, heapFreeCount1.ToString() );
                Utils.SetValue( row, 3, aSheet, heapFreeCount2.ToString() );
                Utils.SetValue( row, 4, aSheet, "=" + Utils.ColumnAndRowAsExcelIdentifier( row, 3 ) + "-" + Utils.ColumnAndRowAsExcelIdentifier( row, 2 ) );
            }

            // Spacer
            row++;
            row++;
            Utils.SetValue( row, 1, aSheet, "Symbol" );
            Utils.SetValue( row, 2, aSheet, "Heap 1" ).AddComment( "Associated cell count from: " + iEngine.Reconstructor1.SourceData.FileName );
            Utils.SetValue( row, 3, aSheet, "Heap 2" ).AddComment( "Associated cell count from: " + iEngine.Reconstructor2.SourceData.FileName );
            Utils.SetValue( row, 4, aSheet, "Delta" );
            Utils.MakeBoxedTitleRow( row, 4, aSheet, 0xFF0000 );

            row++;
            int firstSymbolRow = row;

            // Trackers for both heaps
            SymbolTrackingInfoCollection tracker1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerSymbols;
            SymbolTrackingInfoCollection tracker2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerSymbols;

            // We need a row for unknown items
            {
                Utils.SetValue( row, 1, aSheet, "Unknown" );
                int count1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerUnknown.AssociatedCellCount;
                int count2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerUnknown.AssociatedCellCount;
                Utils.SetValue( row, 2, aSheet, count1.ToString() );
                Utils.SetValue( row, 3, aSheet, count2.ToString() );
                Utils.SetValue( row, 4, aSheet, ( count2 - count1 ).ToString() );
                row++;
            }

            // We need a row for descriptor items
            {
                Utils.SetValue( row, 1, aSheet, "Descriptors" );
                int count1 = iEngine.Reconstructor1.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount;
                int count2 = iEngine.Reconstructor2.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount;
                Utils.SetValue( row, 2, aSheet, count1.ToString() );
                Utils.SetValue( row, 3, aSheet, count2.ToString() );
                Utils.SetValue( row, 4, aSheet, ( count2 - count1 ).ToString() );
                row++;
            }

            // Symbols from heap 1
            List<TrackingInfo> uniqueInHeap2 = new List<TrackingInfo>();
            foreach ( TrackingInfo info in tracker1 )
            {
                System.Diagnostics.Debug.Assert( info.IsUnknownSymbolMatchItem == false );

                int count1 = info.Count;
                int count2 = 0;
                TrackingInfo info2 = tracker2[ info.Symbol ];
                if ( info2 != null )
                {
                    count2 = info2.Count;
                }

                row++;
                Utils.SetValue( row, 2, aSheet, count1.ToString() );
                Utils.SetValue( row, 1, aSheet, info.Symbol.NameWithoutVTablePrefix );
                Utils.SetValue( row, 3, aSheet, count2.ToString() );
                Utils.SetValue( row, 4, aSheet, ( count2 - count1 ).ToString() );

                // Must remove the item so we can spot any types that were new in heap 2
                tracker2.Remove( info );
            }

            // The remaining items in tracker2 had no corresponding entries in tracker1
            foreach ( TrackingInfo info in tracker2 )
            {
                System.Diagnostics.Debug.Assert( info.IsUnknownSymbolMatchItem == false );
                row++;
                Utils.SetValue( row, 1, aSheet, info.Symbol.NameWithoutVTablePrefix );
                Utils.SetValue( row, 2, aSheet, "0" );
                Utils.SetValue( row, 3, aSheet, info.Count.ToString() );
                Utils.SetValue( row, 4, aSheet, info.Count.ToString() );
            }

            // Set the number format for the delta column
            Range allocRange = Utils.GetRangeByColumnAndRow( 2, 4, row, 4, aSheet );
            allocRange.NumberFormat = "[Red]0;[Blue]-0;[Black]0;";

            // Except for the free cells, which have the colour highlighting the other way around
            Range freeRange = Utils.GetRangeByColumnAndRow( freeRow, 4, freeRow + 1, 4, aSheet );
            freeRange.NumberFormat = "[Blue]0;[Red]-0;[Black]0;";

            // Sort by delta
            Excel.Range masterSortCell = (Excel.Range) aSheet.Cells[ 1, 4 ];
            Excel.Range range = Utils.GetRangeByColumnAndRow( firstSymbolRow, 1, row, 4, aSheet );
            range.Sort( masterSortCell.Columns[ 1, Type.Missing ], Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending,
                        Type.Missing, Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending,
                        Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending, Microsoft.Office.Interop.Excel.XlYesNoGuess.xlNo,
                        Type.Missing, Type.Missing,
                        Microsoft.Office.Interop.Excel.XlSortOrientation.xlSortColumns,
                        Microsoft.Office.Interop.Excel.XlSortMethod.xlPinYin,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal );

            // Summation of deltas
            string sumForm = Utils.MakeSummationFormula( firstSymbolRow, row, 4, aSheet );
            Utils.SetValue( row + 2, 4, aSheet, sumForm );

            Utils.BoldColumn( 1, aSheet );
            Utils.AutoFitColumnNoMax( 1, aSheet );
        }
        
        private void ChangeToEnglishCulture()
        {
            System.Threading.Thread.CurrentThread.CurrentCulture = new System.Globalization.CultureInfo("en-US");
        }
        
        private void RestoreOriginalCulture()
        {
            System.Threading.Thread.CurrentThread.CurrentCulture = iOriginalCulture;
        }
        #endregion

        #region Internal callback methods
        #endregion

        #region Data members
        private readonly ComparsionEngine iEngine;
        private readonly string iFileName;
        private CultureInfo iOriginalCulture;
        private Application iExcelApp = null;
        private Workbook iWorkbook = null;
        private Worksheet iSummary = null;
        private int iCurrentPageIndex = 0;
        private List<PageBase> iPages = new List<PageBase>();
        #endregion
    }
}
