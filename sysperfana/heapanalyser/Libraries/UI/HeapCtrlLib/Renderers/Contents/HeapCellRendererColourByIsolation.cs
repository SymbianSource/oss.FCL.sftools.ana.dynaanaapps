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
using HeapLib.Relationships;
using HeapLib.Reconstructor;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Renderers.Contents.Bases;
using SymbianUtils.Colour;
using SymbianUtils.Graphics;
using SymbianUtils.RawItems;

namespace HeapCtrlLib.Renderers.Contents
{
    internal class HeapCellRendererColourByIsolation : HeapCellRendererByColour, IHeapCellRendererContent 
    {
        #region Constructors & destructor
        public HeapCellRendererColourByIsolation()
        {
        }
        #endregion

        #region From IHeapCellRendererContent
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            if  ( aReconstructor != null )
            {
                int min = aReconstructor.Statistics.StatsAllocated.CellNumberOfReferencesLowest.RelationshipManager.ReferencedBy.Count + 1;
                double logMin = Math.Log( (double) min );
                //
                int max = aReconstructor.Statistics.StatsAllocated.CellNumberOfReferencesGreatest.RelationshipManager.ReferencedBy.Count + 1;
                double logMax = Math.Log( (double) max );

                iDomain = (float) ( logMax - logMin ); 
            }
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
            iLargestRectangleCalculator = new HeapCellLargestRectangleCalculator( aNavigator, 1 /* skip first box, start at 2nd box which has index 1 */ );
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
            Color fillColour = ColourForHeapCell( aCell );
            //
            string text = aCell.SymbolStringWithoutDescriptorPrefix;
            PaintBoxedTextWithLuminanceHandling( text, aGraphics, fillColour );
        }

        public void RenderingComplete( Graphics aGraphics )
        {
        }

        public void PaintContent( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize )
        {
            // Get the cell colour that is associated with the cell symbol object type
            aMetaData.CellBoxColor = ColourForHeapCell( aCell );

            // Draw actual cell
            SymRect boxRect = new SymRect( aPosition, aBoxSize );
            boxRect.HalfOffset( aPaddingSize );
            using ( SolidBrush brush = new SolidBrush( aMetaData.CellBoxColor ) )
            {
                aGraphics.FillRectangle( brush, boxRect.Rectangle );
            }

            // If first box, we show the number of inwards links to the cell
            HeapCell.TRegion region = aMetaData.Region;
            if ( region == HeapCell.TRegion.EHeader && aMetaData.CellBoxIndex == 0 )
            {
                boxRect.Inflate( KShrinkSize, KShrinkSize );

                // Draw the fill
                Color lightenColor = ColourUtils.LightenMore( aMetaData.CellBoxColor );
                using ( SolidBrush brush = new SolidBrush( lightenColor ) )
                {
                    aGraphics.FillRectangle( brush, boxRect.Rectangle );
                }
                lightenColor = ColourUtils.Lighten( lightenColor );
                using ( Pen borderPen = new Pen( lightenColor, KHeaderBoxWidth ) )
                {
                    aGraphics.DrawRectangle( borderPen, boxRect.Rectangle );
                }

                // Draw the count
                int count = aCell.RelationshipManager.ReferencedBy.Count;
                if ( count == 0 )
                {
                    PaintBoxedText( count.ToString(), aGraphics, Color.Red, boxRect );
                }
                else
                {
                    PaintBoxedTextWithLuminanceHandling( count.ToString(), aGraphics, Color.Black, boxRect );
                }
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
        private Color ColourForHeapCell( HeapCell aCell )
        {
            float rF = KRampBaselineColourStart.R;
            float gF = KRampBaselineColourStart.G;
            float bF = KRampBaselineColourStart.B;
            //
            float count = aCell.RelationshipManager.ReferencedBy.Count + 1.0f;
            float val = (float) Math.Log( count );
            val /= iDomain;
            //
            rF += ( ( KRampBaselineColourEnd.R - KRampBaselineColourStart.R ) * val );
            gF += ( ( KRampBaselineColourEnd.G - KRampBaselineColourStart.G ) * val );
            bF += ( ( KRampBaselineColourEnd.B - KRampBaselineColourStart.B ) * val );
            //
            int r = Math.Min( Math.Max( 0, (int) rF ), 255 );
            int g = Math.Min( Math.Max( 0, (int) gF ), 255 );
            int b = Math.Min( Math.Max( 0, (int) bF ), 255 );
            //
            return System.Drawing.Color.FromArgb( r, g, b );
        }
        #endregion

        #region Internal constants
        private Color KRampBaselineColourStart = Color.DarkBlue;
        private Color KRampBaselineColourEnd = Color.Coral;
        private const float KHeaderBoxWidth = 2.0f;
        private const int KShrinkSize = -3;
        private const float KBorderLightenAmount = 0.2f;
        #endregion

        #region Data members
        private float iDomain = 0.01f;
        #endregion
    }
}
