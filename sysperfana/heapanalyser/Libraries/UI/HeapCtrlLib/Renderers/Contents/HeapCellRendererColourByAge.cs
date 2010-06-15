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
using SymbianUtils.Colour;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Renderers.Contents.Bases;

namespace HeapCtrlLib.Renderers.Contents
{
    internal class HeapCellRendererColourByAge : HeapCellRendererByColour, IHeapCellRendererContent 
    {
        #region Constructors & destructor
        public HeapCellRendererColourByAge()
        {
        }
        #endregion

        #region From IHeapCellRendererContent
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            if  ( aReconstructor != null )
            {
                iHighestCellAllocationNumber = aReconstructor.Statistics.StatsAllocated.CellAllocationNumberLargest.AllocationNumber;
            }
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
            iLargestRectangleCalculator = new HeapCellLargestRectangleCalculator( aNavigator );
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
            Color fillColour = CellColour( aCell );
            //
            string text = aCell.SymbolStringWithoutDescriptorPrefix;
            PaintBoxedTextWithLuminanceHandling( text, aGraphics, fillColour );
        }

        public void RenderingComplete( Graphics aGraphics )
        {
        }

        public void PaintContent( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize )
        {
            // Get the cell colour that is associated with the cell symbol object type. This
            // makes oldest cells very light and youngest cells very dark. We really want
            // it the other way around...
            aMetaData.CellBoxColor = CellColour( aCell );

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

        #region Internal methods
        public Color CellColour( HeapCell aCell )
        {
            Color fillColour = Color.LightGray;
            //
            if  ( aCell.Type == HeapCell.TType.EAllocated )
            {
                // Get the cell colour that is associated with the cell symbol object type. This
                // makes oldest cells very light and youngest cells very dark. We really want
                // it the other way around...
                //
                float maxBrightness = KRampBaselineColour.GetBrightness();
                float allocationNumberPercentage = ((float) aCell.AllocationNumber / (float) iHighestCellAllocationNumber );
                float targetBrightness = allocationNumberPercentage * maxBrightness;
                float amountToDarkenBy = maxBrightness - targetBrightness;
                fillColour = ColourUtils.Darken( KRampBaselineColour, amountToDarkenBy );
            }
            //
            return fillColour;
        }
        #endregion

        #region Internal constants
        private Color KRampBaselineColour = Color.LightBlue;
        #endregion

        #region Data members
        private uint iHighestCellAllocationNumber = 0;
        private Hashtable iColourHashes = new Hashtable();
        #endregion
    }
}
