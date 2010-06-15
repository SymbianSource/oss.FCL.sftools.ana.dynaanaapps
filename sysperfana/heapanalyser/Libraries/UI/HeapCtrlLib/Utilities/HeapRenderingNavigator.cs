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
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;

namespace HeapCtrlLib.Utilities
{
	public class HeapRenderingNavigator
	{
        #region Delegates
        public delegate void NavBegin();
        public delegate void NavEnd();
        public delegate void NavHeapCellBegin( HeapCell aCell, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding );
        public delegate void NavHeapCellEnd( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding );
        public delegate void NavNewRowHeader( uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding );
        public delegate void NavNewRowBody( HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding );
        public delegate void NavNewColumn( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPixelPos, Point aBoxPos, Size aDimensions, Size aBoxSize, Size aPadding );
        #endregion

        #region Events
        public event NavBegin iNavBegin;
        public event NavEnd iNavEnd;
        public event NavHeapCellBegin iNavHeapCellBegin;
        public event NavHeapCellEnd iNavHeapCellEnd;
        public event NavNewRowHeader iNavNewRowHeader;
        public event NavNewRowBody iNavNewRowBody;
        public event NavNewColumn iNavNewColumn;
        #endregion

        #region Constructors & destructor
		public HeapRenderingNavigator( HeapCellArray aCells )
		{
            iCells = aCells;
            //
            iMetaData = new HeapCellMetaData( this );
		}
        #endregion

        #region Properties
        public HeapCellArray Cells
        {
            get { return iCells; }
        }
        #endregion

        #region API
        public void Navigate( int aStartingCellIndex, uint aStartingAddress, int aHeaderTextWidth, Point aStartOffset, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            // This is the size of the cell including padding
            Size cellSizeIncPadding = aBoxSize + aPadding;
            
            if  ( Cells.Count > 0 )
            {
                // The co-ordinates at which we will render a box (or header).
                Point pos = new Point( aStartOffset.X, aStartOffset.Y - cellSizeIncPadding.Height );

                // The index of the current heap cell within the iCells data structure.
                // We start one before the requested cell, since we immediately skip
                // on to the next one during the initialisation phase.
                int cellIndex = aStartingCellIndex - 1;

                // The total number of cells we have to work with
                int cellCount = Cells.Count;

                // The cell we are rendering. This changes as we move through the capture data.
                HeapCell cell = null;

                // The address of the currently paining box
                uint address = aStartingAddress;

                // Notify that we are starting.
                if  ( iNavBegin != null )
                {
                    iNavBegin();
                }

                // Draw the cells
                bool abort = false;
                for( int row = 0; row < aDimensions.Height && !abort; row++ )
                {
                    // Handle the header at the start of each row
                    pos.X = aStartOffset.X;
                    pos.Y += cellSizeIncPadding.Height;
                    if  ( iNavNewRowHeader != null )
                    {
                        iNavNewRowHeader( address, pos, aDimensions, aBoxSize, aPadding );
                    }

                    // Move forwards, past the header
                    pos.X += aHeaderTextWidth;
                    if  ( iNavNewRowBody != null )
                    {
                        iNavNewRowBody( iMetaData, address, pos, aDimensions, aBoxSize, aPadding );
                    }

                    // Prepare for columns in this row
                    //System.Diagnostics.Debug.WriteLine( "Nav[" /*+ col.ToString("d2")*/ + "00," + row.ToString("d2") + "], ");
                    //System.Diagnostics.Debug.Write( ".");
                    for( int col = 0; col < aDimensions.Width && !abort; col++ )
                    {
                        // Do we need to also move to the next cell?
                        if  ( cell == null || cell.RegionForAddress( address ) == HeapCell.TRegion.EAfterCell )
                        {
                            // Notify finishing a cell
                            if  ( cell != null && iNavHeapCellEnd != null )
                            {
                                iNavHeapCellEnd( cell, iMetaData, address, pos, aDimensions, aBoxSize, aPadding );
                            }

                            if  ( cellIndex < cellCount - 1 )
                            {
                                // Get next cell
                                cell = Cells[ ++cellIndex ];

                                // Notify starting a cell
                                if  ( iNavHeapCellBegin != null )
                                {
                                    iNavHeapCellBegin( cell, address, pos, aDimensions, aBoxSize, aPadding );
                                }
                            }
                            else
                            {
                                abort = true;
                                break;
                            }
                        }
                        //
                        if ( !abort )
                        {
                            // Notify starting a new box
                            if  ( iNavNewColumn != null )
                            {
                                iNavNewColumn( cell, iMetaData, address, pos, new Point( col, row ), aDimensions, aBoxSize, aPadding );
                            }
                          
                            // Move to next address & position
                            pos.X += cellSizeIncPadding.Width;
                            address += SymbianUtils.RawItems.RawItem.KSizeOfOneRawItemInBytes;
                        }
                    }
                }

                if  ( !abort )
                {
                    if  ( cell != null && iNavHeapCellEnd != null )
                    {
                        iNavHeapCellEnd( cell, iMetaData, address, pos, aDimensions, aBoxSize, aPadding );
                    }
                }

                // Notify that we are ending
                if  ( iNavEnd != null )
                {
                    iNavEnd();
                }
            }
        }
        #endregion

        #region Data members
        private readonly HeapCellArray iCells;
        private readonly HeapCellMetaData iMetaData;
        #endregion
    }
}
