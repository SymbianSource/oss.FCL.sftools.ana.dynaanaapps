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

namespace HeapCtrlLib.Utilities
{
	public class HeapCellLargestRectangleCalculator
	{
        #region Constructors & destructor
		public HeapCellLargestRectangleCalculator( HeapRenderingNavigator aNavigator )
            : this( aNavigator, 0 )
		{
        }

        public HeapCellLargestRectangleCalculator( HeapRenderingNavigator aNavigator, int aStartingBoxNumber )
        {
            aNavigator.iNavBegin += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavBegin( Navigator_NavBegin );
            aNavigator.iNavEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavEnd( Navigator_NavEnd );
            aNavigator.iNavHeapCellBegin += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavHeapCellBegin( Navigator_NavHeapCellBegin );
            aNavigator.iNavHeapCellEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavHeapCellEnd( Navigator_NavHeapCellEnd );
            aNavigator.iNavNewColumn += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewColumn( Navigator_NavNewColumn );
            aNavigator.iNavNewRowBody += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewRowBody( Navigator_NavNewRowBody );
            //
            iStartingBoxNumber = aStartingBoxNumber;
        }
        #endregion

        #region API
        #endregion

        #region Properties
        public Rectangle Rectangle
        {
            get { return new Rectangle( iPosition, iSize ); }
        }

        public int NumberOfContinuousBoxes
        {
            get { return iMaxRunLength; }
        }
        #endregion

        #region Navigator call backs
        public void Navigator_NavBegin()
        {
        }

        public void Navigator_NavEnd()
        {
        }

        public void Navigator_NavHeapCellBegin( HeapCell aCell, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            CaptureStart( aPosition, aBoxSize, aPadding );
        }

        public void Navigator_NavHeapCellEnd( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
        }

        public void Navigator_NavNewRowBody( HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            if ( aAddress == 0x700144 )
            { int x = 0; x++; }

            if ( !iFoundMaximumSize )
            {
                if  ( aMetaData.CellRowIndex == 1 )
                {
                    // If the last row was just a few items and this second row has
                    // more (or it is a complete row) then we start the tracing again
                    // from the beginning.
                    int remaining = aMetaData.RemainingBoxes;
                    if  ( remaining > iMaxRunLength )
                    {
                        // Reset - this row is longer
                        if  ( iMaxRunLength < aDimensions.Width )
                        {
                            iMaxRunLength = 0;
                            iPosition = aPosition;
                            iSize = new Size( 0, aBoxSize.Height + aPadding.Height ); // aBoxSize + aPadding;
                        }
                        else
                        {
                            iSize.Width = 0;
                            iSize.Height += aBoxSize.Height + aPadding.Height;
                        }
                    }
                    else
                    {
                        iFoundMaximumSize = true;
                    }
                }
                else if ( aMetaData.RemainingBoxes > aDimensions.Width )
                {
                    iSize.Width = 0;
                    iSize.Height += aBoxSize.Height + aPadding.Height;
                }
                else
                {
                    iFoundMaximumSize = true;
                }
            }
        }

        public void Navigator_NavNewColumn( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPixelPos, Point aBoxPos, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            if ( aCell.Address == 0x700144 ) 
            { int x=0; x++; }

            if  ( !iFoundMaximumSize )
            {
                // If we must exclude the first box from our rectangle calculations,
                // then begin the capture process again.
                if ( aMetaData.CellBoxIndex == iStartingBoxNumber )
                {
                    CaptureStart( aPixelPos, aBoxSize, aPadding );
                }
                //
                ++iMaxRunLength;
                iSize.Width += aBoxSize.Width + aPadding.Width;
            }
        }
        #endregion

        #region Internal methods
        private void CaptureStart( Point aPosition, Size aBoxSize, Size aPadding )
        {
            iFoundMaximumSize = false;
            iPosition = aPosition;
            iMaxRunLength = 0;
            iSize = new Size( 0, aBoxSize.Height + aPadding.Height );
        }
        #endregion

        #region Data members
        private readonly int iStartingBoxNumber;
        private bool iFoundMaximumSize = false;
        private int iMaxRunLength = 0;
        private Size iSize = new Size();
        private Point iPosition = new Point();
        #endregion
	}
}
