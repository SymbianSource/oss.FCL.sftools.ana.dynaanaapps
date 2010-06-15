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
using System.Collections;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using SymbianUtils.Graphics;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Renderers.Contents.Bases;

namespace HeapCtrlLib.Renderers.Contents
{
    internal class HeapCellRendererColourByLength : HeapCellRendererByColour, IHeapCellRendererContent 
    {
        #region Constructors & destructor
        public HeapCellRendererColourByLength()
        {
        }
        #endregion

        #region From IHeapCellRendererContent
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            if  ( aReconstructor != null )
            {
                uint largestLenAlloc = aReconstructor.Statistics.StatsAllocated.CellLargest.Length;
                uint largestLenFree = aReconstructor.Statistics.StatsFree.CellLargest.Length;
                iCellLengthLongest = Math.Max( largestLenAlloc, largestLenFree );
                //
                uint smallestLenAlloc = aReconstructor.Statistics.StatsAllocated.CellSmallest.Length;
                uint smallestLenFree = aReconstructor.Statistics.StatsFree.CellSmallest.Length;
                iCellLengthSmallest = Math.Min( smallestLenAlloc, smallestLenFree );
            }
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
            iLargestRectangleCalculator = new HeapCellLargestRectangleCalculator( aNavigator );
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
            Color fillColour = RampedColourByIntensityRange( KBiggestCellColour, aCell.Length, iCellLengthSmallest, iCellLengthLongest );
            //
            string text = aCell.SymbolStringWithoutDescriptorPrefix;
            PaintBoxedTextWithLuminanceHandling( text, aGraphics, fillColour );
        }

        public void RenderingComplete( Graphics aGraphics )
        {
        }

        public void PaintContent( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize )
        {
            // We paint the cell a different shade of red, depending on it's length. The larger the heap cell,
            // then the more intense the colour. Shorter length cells are de-emphasised.
            aMetaData.CellBoxColor = RampedColourByIntensityRange( KBiggestCellColour, aCell.Length, 
                                                                   iCellLengthSmallest, iCellLengthLongest );

            // Draw actual cell
            SymRect boxRect = new SymRect( aPosition, aBoxSize );
            boxRect.HalfOffset( aPaddingSize );

            using ( SolidBrush brush = new SolidBrush( aMetaData.CellBoxColor ) )
            {
                aGraphics.FillRectangle( brush, boxRect.Rectangle );
            }
        }

        public bool SupportsFiltering
        {
            get { return false; }
        }

        public void SetupFilters()
        {
        }
        #endregion

        #region Internal constants
        private Color KBiggestCellColour = Color.Red;
        #endregion

        #region Data members
        private uint iCellLengthLongest = 0;
        private uint iCellLengthSmallest = 0;
        #endregion
    }
}
