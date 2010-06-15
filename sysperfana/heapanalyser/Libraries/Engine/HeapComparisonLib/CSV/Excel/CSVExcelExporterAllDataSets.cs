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
using System.Text;
using System.Threading;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using Excel = Microsoft.Office.Interop.Excel;
using System.Reflection; 
using SymbianExcelUtils;
using SymbianUtils;
using SymbianUtils.Range;

namespace HeapComparisonLib.CSV
{
    internal class CSVExcelExporterAllDataSets : DisposableObject
    {
        #region Constructors & destructor
        public CSVExcelExporterAllDataSets( string aOutputFileName, SortedDictionary<long, string> aThreadMap, int aDataSetCount )
        {
            iDataSetCount = aDataSetCount;
            iFileName = aOutputFileName;

            // Sort the aThreadMap list by thread name, rather than id.
            SortedDictionary<string, TThreadMapEntry> temp = new SortedDictionary<string, TThreadMapEntry>();
            foreach ( KeyValuePair<long, string> kvp in aThreadMap )
            {
                TThreadMapEntry entry = new TThreadMapEntry();
                entry.iThreadName = kvp.Value;
                entry.iThreadId = kvp.Key;
                //
                string mungedName = string.Format( "{1} [{0:d5}]", kvp.Key, kvp.Value );
                temp.Add( mungedName, entry );
            }

            // Now build new list, in the right order
            iThreadMap = new Dictionary<long, TThreadMapEntry>( temp.Count + 1 );
            foreach ( KeyValuePair<string, TThreadMapEntry> kvp in temp )
            {
                iThreadMap.Add( kvp.Value.iThreadId, kvp.Value );
            }
  
            // Open up excel
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
        public void Export( CSVDataSet aSet )
        {
            int count = aSet.Count;
            for ( int i = 0; i < count; i++ )
            {
                CSVThread thread = aSet[ i ];

                // find row
                if ( iThreadMap.ContainsKey( thread.ThreadId ) )
                {
                    TThreadMapEntry entry = iThreadMap[ thread.ThreadId ];

                    Utils.SetValue( entry.iRowIndex, iColumnCounter, iSheetChunkSize, thread.SizeCurrent.ToString() );
                    Utils.SetValue( entry.iRowIndex, iColumnCounter, iSheetAlloc, thread.AllocSpaceTotal.ToString() );
                    Utils.SetValue( entry.iRowIndex, iColumnCounter, iSheetFree, thread.FreeSpaceTotal.ToString() );

                    // Update stats
                    ++entry.iNumberOfMatchingDataSets;
                    
                    // Min & max for each type
                    entry.iRangeChunk.UpdateMin( thread.SizeCurrent );
                    entry.iRangeChunk.UpdateMax( thread.SizeCurrent );
                    entry.iRangeAlloc.UpdateMin( thread.AllocSpaceTotal );
                    entry.iRangeAlloc.UpdateMax( thread.AllocSpaceTotal );
                    entry.iRangeFree.UpdateMin( thread.FreeSpaceTotal );
                    entry.iRangeFree.UpdateMax( thread.FreeSpaceTotal );

                    // Delta for each type
                    long deltaChunk = entry.iLastChunk > 0 ? ( thread.SizeCurrent - entry.iLastChunk ) : 0;
                    long deltaAlloc = entry.iLastAlloc > 0 ? ( thread.AllocSpaceTotal - entry.iLastAlloc ) : 0;
                    long deltaFree = entry.iLastFree > 0 ? ( thread.FreeSpaceTotal - entry.iLastFree ) : 0;
                    entry.iDeltaChunk += deltaChunk;
                    entry.iDeltaAlloc += deltaAlloc;
                    entry.iDeltaFree += deltaFree;

                    // Net effect
                    entry.iNetEffectChunk += CSVExcelExporterAllDataSets.NetEffectForDelta( deltaChunk );
                    entry.iNetEffectAlloc += CSVExcelExporterAllDataSets.NetEffectForDelta( deltaAlloc );
                    entry.iNetEffectFree += CSVExcelExporterAllDataSets.NetEffectForDelta( deltaFree );

                    // Update last values
                    entry.iLastChunk = thread.SizeCurrent;
                    entry.iLastAlloc = thread.AllocSpaceTotal;
                    entry.iLastFree = thread.FreeSpaceTotal;
                }
                else
                {
                    throw new Exception( "Cannot find thread entry for thread named: " + thread.ThreadName );
                }
            }

            ++iColumnCounter;
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
        private void PrepareWorksheetReadyForData( )
        {
            Excel.Workbooks workbooks = iExcelApp.Workbooks;
            workbooks.Add(Excel.XlWBATemplate.xlWBATWorksheet);
            Excel.Workbook workbook = workbooks.get_Item(workbooks.Count);
            Excel.Sheets sheets = workbook.Worksheets;

            iSheetChunkSize = (Excel.Worksheet) sheets.get_Item( 1 );
            CreateSheet( iSheetChunkSize, "Chunk Size" );

            iSheetFree = (Excel.Worksheet) sheets.Add( Type.Missing, sheets.get_Item( sheets.Count ), 1, Excel.XlSheetType.xlWorksheet );
            CreateSheet( iSheetFree, "Free Size" );

            iSheetAlloc = (Excel.Worksheet) sheets.Add( Type.Missing, sheets.get_Item( sheets.Count ), 1, Excel.XlSheetType.xlWorksheet );
            CreateSheet( iSheetAlloc, "Alloc Size" );
        }

        private void PrepareWorsheetForSaving()
        {
            // Update all sheets change factors...
            CalculateChangeFactor( iSheetChunkSize, TThreadMapEntry.TType.ETypeChunk );
            CalculateChangeFactor( iSheetAlloc, TThreadMapEntry.TType.ETypeAlloc );
            CalculateChangeFactor( iSheetFree, TThreadMapEntry.TType.ETypeFree );

            // Deltas
            CalculateDelta( iSheetChunkSize, TThreadMapEntry.TType.ETypeChunk );
            CalculateDelta( iSheetAlloc, TThreadMapEntry.TType.ETypeAlloc );
            CalculateDelta( iSheetFree, TThreadMapEntry.TType.ETypeFree );
 
            // Sort
            SortByChangeFactor( iSheetChunkSize );
            SortByChangeFactor( iSheetAlloc );
            SortByChangeFactor( iSheetFree );

            iSheetAlloc.Activate();
        }

        private void CreateSheet( Excel.Worksheet aSheet, string aTitle )
        {
            aSheet.Name = aTitle;
            
            // Create standard columns
            Utils.SetValue( 1, KColumnNumberThreadName, aSheet, "Thread Name" );
            Utils.SetValue( 1, KColumnNumberChangeFactor, aSheet, "Change Factor" );
            Utils.SetValue( 1, KColumnNumberDelta, aSheet, "Overall Delta" );

            // Set up column formatting
            Utils.FormatColumn( KColumnNumberChangeFactor, aSheet, KColumnNumberFormatChangeFactor );
            Utils.FormatColumn( KColumnNumberDelta, aSheet, KColumnNumberFormatDelta );

            for ( int i = 0; i < iDataSetCount; i++ )
            {
                int col = i + KColumnNumberCycleFirst;
                Utils.SetValue( 1, col, aSheet, string.Format( "Cycle {0:d}", i + 1 ) );
                Utils.FormatColumn( col, aSheet, KColumnNumberFormatCycle );
            }

            Utils.MakeBoxedTitleRow( 1, KColumnNumberCycleFirst + iDataSetCount, aSheet, 0xFF0000 );

            // Add thread names & ids
            int row = 2;
            foreach ( KeyValuePair<long, TThreadMapEntry> kvp in iThreadMap )
            {
                TThreadMapEntry entry = kvp.Value;
                Utils.SetValue( row, KColumnNumberThreadName, aSheet, string.Format( "[{0:d5}] {1}", kvp.Key, kvp.Value.iThreadName ) );
                if ( entry.iRowIndex == 0 )
                {
                    entry.iRowIndex = row;
                }
                ++row;
            }

            // Size columns
            Utils.AutoFitColumn( KColumnNumberThreadName, aSheet );
            Utils.SetColumnWidth( KColumnNumberChangeFactor, aSheet, 15 );
            Utils.SetColumnWidth( KColumnNumberDelta, aSheet, 15 );
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

        private void SortByChangeFactor( Excel.Worksheet aSheet )
        {
            int colCount = KColumnNumberCycleFirst + iDataSetCount;
            
            Excel.Range masterSortCell = (Excel.Range) aSheet.Cells[ 1, KColumnNumberChangeFactor ];
            Excel.Range range = Utils.GetRangeByColumnAndRow( 1, KColumnNumberThreadName, 1 + iThreadMap.Count, colCount, aSheet );
            range.Sort( masterSortCell.Columns[ 1, Type.Missing ], Microsoft.Office.Interop.Excel.XlSortOrder.xlDescending,
                        Type.Missing, Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlAscending, 
                        Type.Missing, Microsoft.Office.Interop.Excel.XlSortOrder.xlAscending, Microsoft.Office.Interop.Excel.XlYesNoGuess.xlYes,
                        Type.Missing, Type.Missing,
                        Microsoft.Office.Interop.Excel.XlSortOrientation.xlSortColumns,
                        Microsoft.Office.Interop.Excel.XlSortMethod.xlPinYin,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal,
                        Microsoft.Office.Interop.Excel.XlSortDataOption.xlSortNormal );
        }

        private void CalculateChangeFactor( Excel.Worksheet aSheet, TThreadMapEntry.TType aType )
        {
            aSheet.Activate();

            foreach ( KeyValuePair<long, TThreadMapEntry> kvp in iThreadMap )
            {
                TThreadMapEntry entry = kvp.Value;
                //
                string formula = entry.ChangeFactor( aType );
                Utils.SetValue( entry.iRowIndex, KColumnNumberChangeFactor, aSheet, formula );
            }
        }

        private void CalculateDelta( Excel.Worksheet aSheet, TThreadMapEntry.TType aType )
        {
            aSheet.Activate();

            foreach ( KeyValuePair<long, TThreadMapEntry> kvp in iThreadMap )
            {
                TThreadMapEntry entry = kvp.Value;
                //
                string formula = entry.Delta( aType );
                Utils.SetValue( entry.iRowIndex, KColumnNumberDelta, aSheet, formula );
            }
        }

        private static int NetEffectForDelta( long aDelta )
        {
            int ret = -1;
            //
            if ( aDelta > 0 )
            {
                ret = 1;
            }
            else if ( aDelta == 0 )
            {
                ret = 0;
            }
            //
            return ret;
        }
        #endregion

        #region Internal constants
        private const int KColumnNumberThreadName = 1;
        private const int KColumnNumberChangeFactor = 2;
        private const int KColumnNumberDelta = 3;

        private const int KColumnNumberCycleFirst = 5;

        private const string KColumnNumberFormatDelta = "[Red]###,###,##0;[Blue]-###,###,##0";
        private const string KColumnNumberFormatCycle = "###,###,##0";
        private const string KColumnNumberFormatChangeFactor = "[Red]#,##0.000000;[Blue]-#,##0.000000";
        #endregion

        #region Data members
        private readonly string iFileName;
        private readonly int iDataSetCount;
        private readonly Dictionary<long, TThreadMapEntry> iThreadMap;
        private Excel.Worksheet iSheetFree;
        private Excel.Worksheet iSheetAlloc;
        private Excel.Worksheet iSheetChunkSize;
        private Excel.Application iExcelApp;
        private int iColumnCounter = KColumnNumberCycleFirst;
        #endregion
    }

    #region Internal class
    internal class TThreadMapEntry
    {
        #region Enumerations
        public enum TType
        {
            ETypeChunk = 0,
            ETypeAlloc,
            ETypeFree
        }
        #endregion

        #region API
        public string ChangeFactor( TType aType )
        {
            string ret = string.Empty;
            //
            if ( aType == TType.ETypeChunk )
            {
                ret = ChangeFactor( iNetEffectChunk, iDeltaChunk, iRangeChunk );
            }
            else if ( aType == TType.ETypeAlloc )
            {
                ret = ChangeFactor( iNetEffectAlloc, iDeltaAlloc, iRangeAlloc );
            }
            else if ( aType == TType.ETypeFree )
            {
                ret = ChangeFactor( iNetEffectFree, iDeltaFree, iRangeFree );
            }
            //
            return ret;
        }

        public string Delta( TType aType )
        {
            string ret = string.Empty;
            //
            if ( aType == TType.ETypeChunk )
            {
                ret = ( iDeltaChunk != 0 ) ? "=" + iDeltaChunk.ToString() : string.Empty;
            }
            else if ( aType == TType.ETypeAlloc )
            {
                ret = ( iDeltaAlloc != 0 ) ? "=" + iDeltaAlloc.ToString() : string.Empty;
            }
            else if ( aType == TType.ETypeFree )
            {
                ret = ( iDeltaFree != 0 ) ? "=" + iDeltaFree.ToString() : string.Empty;
            }
            //
            return ret;
        }
        #endregion

        #region Internal methods
        private string ChangeFactor( int aNetEffect, long aDelta, AddressRange aRange )
        {
            StringBuilder ret = new StringBuilder();

            //      ( Total net effect * Total delta )
            // --------------------------------------------
            // (Max - Min) * (Number of Matching Data Sets)
            // 
            if ( aRange.Max - aRange.Min == 0 )
            {
                // Avoid divide by zero when there has been no change
            }
            else if ( aDelta == 0 )
            {
                // Overall change delta was zero
            }
            else
            {
                ret.Append( "=" );
                ret.AppendFormat( "( {0} * ABS({1}) )", aNetEffect, aDelta );
                ret.Append( " / " );
                ret.AppendFormat( "( ({0} - {1}) * {2} )", aRange.Max, aRange.Min, iNumberOfMatchingDataSets );
            }

            //
            return ret.ToString();
        }
        #endregion

        #region Data members
        public string iThreadName = string.Empty;
        public long iThreadId = 0;
        public int iRowIndex = 0;
        public int iNumberOfMatchingDataSets = 0;

        public AddressRange iRangeChunk = new AddressRange();
        public AddressRange iRangeAlloc = new AddressRange();
        public AddressRange iRangeFree = new AddressRange();

        public long iLastChunk = 0;
        public long iLastAlloc = 0;
        public long iLastFree = 0;

        public long iDeltaChunk = 0;
        public long iDeltaAlloc = 0;
        public long iDeltaFree = 0;

        public int iNetEffectChunk = 0;
        public int iNetEffectAlloc = 0;
        public int iNetEffectFree = 0;
        #endregion
    }
    #endregion
}
