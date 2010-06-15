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
using System.Drawing;
using HeapLib.Cells;
using SymbianUtils.RawItems;

namespace HeapCtrlLib.Utilities
{
    public class HeapCellMetaData
    {
        #region Constructors & destructor
        public HeapCellMetaData( HeapRenderingNavigator aNavigator )
        {
            aNavigator.iNavBegin += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavBegin( Navigator_NavBegin );
            aNavigator.iNavEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavEnd( Navigator_NavEnd );
            aNavigator.iNavHeapCellBegin += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavHeapCellBegin( Navigator_NavHeapCellBegin );
            aNavigator.iNavHeapCellEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavHeapCellEnd( Navigator_NavHeapCellEnd );
            aNavigator.iNavNewColumn += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewColumn( Navigator_NavNewColumn );
            aNavigator.iNavNewRowBody += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewRowBody( Navigator_NavNewRowBody );
        }
        #endregion

        #region Properties
        public Point BoxCoordinates
        {
            get { return iBoxCoordinates; }
            set { iBoxCoordinates = value; }
        }

        public int RemainingBoxes
        {
            get { return iRemainingBoxes; }
            set { iRemainingBoxes = value; }
        }

        public int RemainingBoxesAfterThisRow
        {
            get { return iRemainingBoxesAfterThisRow; }
            set { iRemainingBoxesAfterThisRow = value; }
        }

        public int RemainingRows
        {
            get { return iRemainingRows; }
            set { iRemainingRows = value; }
        }

        public long RemainingBytes
        {
            get { return iRemainingBytes; }
            set { iRemainingBytes = value; }
        }

        public int CellBoxIndex
        {
            get { return iCellBoxIndex; }
            set { iCellBoxIndex = value; }
        }

        public int CellRowIndex
        {
            get { return iCellRowIndex; }
            set { iCellRowIndex = value; }
        }

        public int CellBoxCount
        {
            get { return iCellBoxCount; }
            set { iCellBoxCount = value; }
        }

        public RawItem RawItem
        {
            get { return iRawItem; }
        }

        public HeapCell.TRegion Region
        {
            get
            {
                System.Diagnostics.Debug.Assert( iCell != null, "Not rendering a heap cell" );
                System.Diagnostics.Debug.Assert( iAddress > 0, "Invalid current address" );
                //
                HeapCell.TRegion region = iCell.RegionForAddress( iAddress );
                return region;
            }
        }

        public HeapCellBorderInfo Borders
        {
            get { return iBorders; }
            set { iBorders = value; }
        }

        // Set by the content renderer, read by the border renderer
        public Color CellBoxColor
        {
            get { return iCellBoxColor; }
            set { iCellBoxColor = value; }
        }
        #endregion

        #region Internal methods
        private void CalculateBorders( HeapCell aCell, long aAddress, Size aDimensions )
        {
            long remainingBoxes = ( aCell.EndAddress - aAddress ) / SymbianUtils.RawItems.RawItem.KSizeOfOneRawItemInBytes;

            // Start with no borders
            Borders.Reset();

            // How many boxes are left to render on this row
            int remainingBoxesForThisLine = ( aDimensions.Width - BoxCoordinates.X );

            // Its the first line if we are drawing the first row, or if we're drawing the
            // second row and there weren't any boxes from this heap cell directly above us
            // in the grid.
            bool firstLine = ( CellRowIndex == 0 );
            if  ( CellRowIndex == 1 )
            {
                int numberOfBoxesDrawnOnPreviousLine = ( CellBoxIndex - BoxCoordinates.X );
                int xPosOfFirstBox = aDimensions.Width - numberOfBoxesDrawnOnPreviousLine;
                firstLine = ( BoxCoordinates.X < xPosOfFirstBox );
            }
            Borders.SetBorder( THeapCellBorderType.ETop, firstLine );

            // Its the last line if we are drawing the last row, or then if we're drawing
            // the last-but-one row and there weren't any boxes from this heap cell directly
            // below us in the grid.
            bool lastLine = ( RemainingBoxes <= remainingBoxesForThisLine );
            if  ( RemainingRows > 0 && BoxCoordinates.Y == aDimensions.Height - 1 )
            {
                lastLine = true;
            }
            else if ( RemainingRows == 1 )
            {
                // Now we need to work out how many boxes of the next row will be
                // required to finish rendering it fully.
                lastLine = ( BoxCoordinates.X >= RemainingBoxesAfterThisRow );
            }
            Borders.SetBorder( THeapCellBorderType.EBottom, lastLine );

            // Its the first box if it is the absolute first box we have rendered for a given
            // cell, or then it is the first box in a new row.
            bool firstBox = ( CellBoxIndex == 0 ) || ( BoxCoordinates.X == 0 );
            Borders.SetBorder( THeapCellBorderType.ELeft, firstBox );

            // Its the last box if it is the absolute last box we have rendered for a given
            // cell, or then it is the last box in a new row.
            bool lastBox = ( CellBoxIndex == CellBoxCount - 1 ) || ( remainingBoxes == 0 ) || ( BoxCoordinates.X == aDimensions.Width - 1 );
            Borders.SetBorder( THeapCellBorderType.ERight, lastBox );
        }
        #endregion

        #region Navigator call backs
        private void Navigator_NavBegin()
        {
        }

        private void Navigator_NavEnd()
        {
            iCell = null;
        }

        private void Navigator_NavHeapCellBegin( HeapCell aCell, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            // Starting a new cell
            iCell = aCell;
            iAddress = aAddress;

            // This contains the absolute number of boxes required to render a given
            // heap cell.
            CellBoxCount = (int) ( aCell.Length / SymbianUtils.RawItems.RawItem.KSizeOfOneRawItemInBytes );
            BoxCoordinates = new Point();
            RemainingBoxes = 0;
            RemainingBoxesAfterThisRow = 0;
            RemainingRows = 0;
            RemainingBytes = 0;
            CellRowIndex = 0;
            Borders.Reset();
 
            // Reset current raw item - we won't have a new one until we hit the payload section
            iRawItem = null;

            // This starts at -1 since the first act of
            // preparing the meta data is to increment the index by one.
            CellBoxIndex = -1;
        }

        private void Navigator_NavHeapCellEnd( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            // Finished with the cell...
            iCell = null;
        }

        private void Navigator_NavNewColumn( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPixelPos, Point aBoxPos, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            System.Diagnostics.Debug.Assert( iCell != null && iCell.Address == aCell.Address );

            // Indicate that we're processing a new box
            ++CellBoxIndex;
            iAddress = aAddress;

            // Set our box coordinates
            iBoxCoordinates = aBoxPos;

            // Get current raw item if we're in the payload section
            iRawItem = null;
            try
            {
                HeapCell.TRegion region = Region;
                if ( region == HeapCell.TRegion.EPayload )
                {
                    iRawItem = iCell[ aAddress ];
                }
            }
            catch( ArgumentException )
            {
            }


            // Some up front calculations that we'll need below...
            RemainingBytes = aCell.Remainder( aAddress );
            int remainingBoxesForThisLine = ( aDimensions.Width - BoxCoordinates.X );
            RemainingBoxes = (int) ( RemainingBytes / SymbianUtils.RawItems.RawItem.KSizeOfOneRawItemInBytes );
            RemainingBoxesAfterThisRow = Math.Max( 0, RemainingBoxes - remainingBoxesForThisLine );

            // If we can render all the remaining boxes in the not-yet-drawn
            // boxes from this row, we don't need anymore rows.
            RemainingRows = 0;
            if  ( RemainingBoxesAfterThisRow > 0 )
            {
                // Otherwise, we need to identify how many more rows we will need
                // in order to complete the remaining boxes that are left over
                // after this row's boxes.
                RemainingRows = ( RemainingBoxesAfterThisRow / aDimensions.Width ) + 1;
            }

            // Work out the borders that should be enabled for the cell
            CalculateBorders( aCell, aAddress, aDimensions );
        }

        private void Navigator_NavNewRowBody( HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            ++CellRowIndex;
        }
        #endregion

        #region Data members
        private Point iBoxCoordinates = new Point( 0, 0 );
        private int iRemainingBoxes = 0;
        private int iRemainingBoxesAfterThisRow = 0;
        private int iRemainingRows = 0;
        private long iRemainingBytes = 0;
        private int iCellBoxIndex = 0;
        private int iCellRowIndex = 0;
        private int iCellBoxCount = 0;
        private uint iAddress = 0;
        private HeapCell iCell = null;
        private RawItem iRawItem = null;
        private HeapCellBorderInfo iBorders = new HeapCellBorderInfo();
        private Color iCellBoxColor = Color.HotPink;
        #endregion
    }
}
