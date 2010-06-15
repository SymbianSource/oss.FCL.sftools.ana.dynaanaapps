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
using System.Threading;
using System.Runtime.InteropServices;
using Excel = Microsoft.Office.Interop.Excel;
using System.Reflection; 
using SymbianExcelUtils;
using SymbianUtils;

namespace HeapComparisonLib.CSV
{
    internal class CSVExcelExporterTwoDataSets : DisposableObject
    {
        #region Constructors & destructor
        public CSVExcelExporterTwoDataSets( string aOutputFileName, string aCol1FileName, string aCol2FileName )
        {
            iFileName = aOutputFileName;
            iCol1FileName = aCol1FileName;
            iCol2FileName = aCol2FileName;
            //
            iExcelApp = new Excel.Application();
            if ( iExcelApp != null )
            {
                iExcelApp.Visible = false;
                iExcelApp.DisplayAlerts = false;

                // Prepare sheets
                PrepareWorksheetReadyForData();
            }
            else
            {
                throw new Exception( "Microsoft Excel not available" );
            }
        }
        #endregion

        #region API
        public void CompareThread( CSVThread aThread1, CSVThread aThread2 )
        {
            // Do comparison
            iComparerHeapSize.Compare( aThread1, aThread2 );
            iComparerCellCounts.Compare( aThread1, aThread2 );
            iComparerLargestCells.Compare( aThread1, aThread2 );
            iComparerFragmentation.Compare( aThread1, aThread2 );
            iComparerSlackSpace.Compare( aThread1, aThread2 );
        }
        #endregion

        #region From DisposableObject
        protected override void CleanupManagedResources()
        {
            try
            {
                if ( iExcelApp != null )
                {
                    // We're finished
                    PrepareWorsheetForSaving();

                    // Save excel workbook
                    SaveWorkbook();

                    // Close app
                    Utils.CloseExcel( iExcelApp );
                }

                iExcelApp = null;
            }
            finally
            {
                base.CleanupManagedResources();
            }
        }
        #endregion

        #region Internal methods
        private void PrepareWorksheetReadyForData()
        {
            Excel.Workbooks workbooks = iExcelApp.Workbooks;
            workbooks.Add(Excel.XlWBATemplate.xlWBATWorksheet);
            Excel.Workbook workbook = workbooks.get_Item(workbooks.Count);
            Excel.Sheets sheets = workbook.Worksheets;

            Excel.Worksheet sheet = null;
            //
            sheet = (Excel.Worksheet)sheets.Add(Type.Missing, sheets.get_Item(sheets.Count), 1, Excel.XlSheetType.xlWorksheet);
            iComparerHeapSize = new Comparers.HeapSize( sheet, iCol1FileName, iCol2FileName );
            iComparerHeapSize.PrepareWorksheetReadyForData();
            //
            sheet = (Excel.Worksheet)sheets.Add(Type.Missing, sheets.get_Item(sheets.Count), 1, Excel.XlSheetType.xlWorksheet);
            iComparerCellCounts = new Comparers.CellCounts( sheet, iCol1FileName, iCol2FileName );
            iComparerCellCounts.PrepareWorksheetReadyForData();
            //
            sheet = (Excel.Worksheet)sheets.Add(Type.Missing, sheets.get_Item(sheets.Count), 1, Excel.XlSheetType.xlWorksheet);
            iComparerLargestCells = new Comparers.LargestCells( sheet, iCol1FileName, iCol2FileName );
            iComparerLargestCells.PrepareWorksheetReadyForData();
            //
            sheet = (Excel.Worksheet)sheets.Add(Type.Missing, sheets.get_Item(sheets.Count), 1, Excel.XlSheetType.xlWorksheet);
            iComparerFragmentation = new Comparers.Fragmentation( sheet, iCol1FileName, iCol2FileName );
            iComparerFragmentation.PrepareWorksheetReadyForData();
            //
            sheet = (Excel.Worksheet)sheets.Add(Type.Missing, sheets.get_Item(sheets.Count), 1, Excel.XlSheetType.xlWorksheet);
            iComparerSlackSpace = new Comparers.SlackSpace( sheet, iCol1FileName, iCol2FileName );
            iComparerSlackSpace.PrepareWorksheetReadyForData();
        }

        private void PrepareWorsheetForSaving()
        {
            // Finalise comparison sheets
            iComparerHeapSize.FinaliseWorksheet();
            iComparerCellCounts.FinaliseWorksheet();
            iComparerLargestCells.FinaliseWorksheet();
            iComparerFragmentation.FinaliseWorksheet();
            iComparerSlackSpace.FinaliseWorksheet();

            // Create summary sheet
            Excel.Workbooks workbooks = iExcelApp.Workbooks;
            Excel.Workbook workbook = workbooks.get_Item(workbooks.Count);
            Excel.Sheets sheets = workbook.Worksheets;
            Excel.Worksheet sheet = (Excel.Worksheet)sheets.get_Item(1);
            CreateSummary( sheet );
            sheet.Activate();
        }

        private void CreateSummary( Microsoft.Office.Interop.Excel.Worksheet aSheet )
        {
            aSheet.Name = "Summary";
            //
            int row = 1;
            Utils.SetValue( row, 2, aSheet, "Delta" );
            Utils.MakeBoxedTitleRow( row, 2, aSheet, 0xFF0000 );
            row++;
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "File 1:" );
            Utils.SetValue( row, 2, aSheet, iCol1FileName );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "File 2:" );
            Utils.SetValue( row, 2, aSheet, iCol2FileName );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Heap size:" );
            Utils.SetValue( row, 2, aSheet, iComparerHeapSize.DeltaFormula( 0 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Free space:" );
            Utils.SetValue( row, 2, aSheet, iComparerHeapSize.DeltaFormula( 1 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Alloc space:" );
            Utils.SetValue( row, 2, aSheet, iComparerHeapSize.DeltaFormula( 2 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Heap size:" );
            Utils.SetValue( row, 2, aSheet, iComparerHeapSize.DeltaFormula( 0 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Free cell count:" );
            Utils.SetValue( row, 2, aSheet, iComparerCellCounts.DeltaFormula( 0 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Allocated cell count:" );
            Utils.SetValue( row, 2, aSheet, iComparerCellCounts.DeltaFormula( 1 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Fragmentation:" );
            Utils.SetValue( row, 2, aSheet, iComparerFragmentation.DeltaFormula( 0 ) );
            //
            row++;
            Utils.SetValue( row, 1, aSheet, "Slack space:" );
            Utils.SetValue( row, 2, aSheet, iComparerSlackSpace.DeltaFormula( 0 ) );
            Utils.BoldColumn( 1, aSheet );

            // Calculate column sizes
            Utils.AutoFitColumn( 1, aSheet );
            Utils.AutoFitColumn( 2, aSheet );
        }

        private void SaveWorkbook()
        {
            string path = Path.GetDirectoryName( iFileName );
            System.IO.DirectoryInfo dirInfo = new System.IO.DirectoryInfo( path );
            if ( !dirInfo.Exists )
            {
                dirInfo.Create();
            }

            System.IO.FileInfo fileInfo = new System.IO.FileInfo( iFileName );
            if ( fileInfo.Exists )
            {
                try
                {
                    fileInfo.Delete();
                }
                catch ( Exception )
                {
                }
            }

            Microsoft.Office.Interop.Excel.Workbooks workbooks = iExcelApp.Workbooks;
            Microsoft.Office.Interop.Excel.Workbook workbook = workbooks.get_Item( workbooks.Count );

            try
            {
                workbook.SaveAs( iFileName,
                                    Excel.XlFileFormat.xlExcel9795,
                                    Type.Missing,
                                    Type.Missing,
                                    Type.Missing,
                                    Type.Missing,
                                    Excel.XlSaveAsAccessMode.xlNoChange,
                                    Type.Missing,
                                    Type.Missing,
                                    Type.Missing,
                                    Type.Missing,
                                    Type.Missing );
            }
            catch ( System.IO.IOException )
            {
            }

            workbook.Close( false, Type.Missing, Type.Missing );
        }
        #endregion

        #region Data members
        private readonly string iFileName;
        private readonly string iCol1FileName;
        private readonly string iCol2FileName;
        private Excel.Application iExcelApp;
        private Comparers.HeapSize iComparerHeapSize;
        private Comparers.CellCounts iComparerCellCounts;
        private Comparers.LargestCells iComparerLargestCells;
        private Comparers.Fragmentation iComparerFragmentation;
        private Comparers.SlackSpace iComparerSlackSpace;
        #endregion
    }
}
