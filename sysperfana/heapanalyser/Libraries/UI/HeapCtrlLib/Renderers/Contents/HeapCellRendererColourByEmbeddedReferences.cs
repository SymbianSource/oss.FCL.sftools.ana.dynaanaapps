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
    internal class HeapCellRendererColourByEmbeddedReferences : HeapCellRendererByColour, IHeapCellRendererContent 
    {
        #region Constructors & destructor
        public HeapCellRendererColourByEmbeddedReferences()
        {
        }
        #endregion

        #region From IHeapCellRendererContent
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            if  ( aReconstructor != null )
            {
                int min = aReconstructor.Statistics.StatsAllocated.CellNumberOfEmbeddedReferencesLeast.RelationshipManager.EmbeddedReferencesTo.Count + 1;
                double logMin = Math.Log( (double) min );
                //
                int max = aReconstructor.Statistics.StatsAllocated.CellNumberOfEmbeddedReferencesMost.RelationshipManager.EmbeddedReferencesTo.Count + 1;
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
            SymRect boxRect = new SymRect( aPosition, aBoxSize );
            boxRect.HalfOffset( aPaddingSize );

            // Draw actual cell
            using ( SolidBrush brush = new SolidBrush( aMetaData.CellBoxColor ) )
            {
                aGraphics.FillRectangle( brush, boxRect.Rectangle );
            }

            HeapCell.TRegion region = aMetaData.Region;
            if ( region == HeapCell.TRegion.EHeader && aMetaData.CellBoxIndex == 0 )
            {
                // If first box, we show the number of inwards links to the cell
                boxRect.Inflate( KShrinkSize, KShrinkSize );

                // Draw the fill
                Color lightenColor = ColourUtils.Lighten( aMetaData.CellBoxColor );
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
                PaintBoxedTextWithLuminanceHandling( aCell.RelationshipManager.EmbeddedReferencesTo.Count.ToString(), aGraphics, Color.Black, boxRect );
            }
            else
            {
                // If we're in the payload section, then get the raw item corresponding to the address we are drawing
                RawItem rawItem = aMetaData.RawItem;
                if ( rawItem != null && rawItem.Tag != null && rawItem.Tag is HeapLib.Relationships.RelationshipInfo )
                {
                    RelationshipInfo relInfo = (RelationshipInfo) rawItem.Tag;

                    // Make the box a bit smaller
                    boxRect.Inflate( KShrinkSize, KShrinkSize );

                    // Draw the fill
                    Color lightenColor = ColourUtils.Lighten( aMetaData.CellBoxColor );
                    using ( SolidBrush brush = new SolidBrush( lightenColor ) )
                    {
                        aGraphics.FillRectangle( brush, boxRect.Rectangle );
                    }
                    lightenColor = ColourUtils.Lighten( lightenColor );
                    using ( Pen borderPen = new Pen( lightenColor, KHeaderBoxWidth ) )
                    {
                        aGraphics.DrawRectangle( borderPen, boxRect.Rectangle );
  
                        // If it's a clean reference, then draw a diagonal line to decorate the box reference
                        if ( relInfo.IsCleanLink )
                        {
                            Point linePosStart = boxRect.TopLeft;
                            linePosStart.X += KHeaderBoxLineCornerOffset;
                            Point linePosEnd = boxRect.TopLeft;
                            linePosEnd.Y += KHeaderBoxLineCornerOffset;
                            //
                            aGraphics.DrawLine( borderPen, linePosStart, linePosEnd );
                        }
                   }
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
            Color ret = KBaselineColourDescriptor;
            //
            if ( !aCell.IsDescriptor )
            {
                float rF = KRampBaselineColourStart.R;
                float gF = KRampBaselineColourStart.G;
                float bF = KRampBaselineColourStart.B;
                //
                float count = aCell.RelationshipManager.EmbeddedReferencesTo.Count + 1.0f;
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
            //
            return ret;
        }
        #endregion

        #region Internal constants
        private const int KHeaderBoxLineCornerOffset = 5;
        private const int KShrinkSize = -3;
        private const float KBorderLightenAmount = 0.2f;
        private const float KHeaderBoxWidth = 2.0f;
        private Color KRampBaselineColourStart = Color.LightBlue;
        private Color KRampBaselineColourEnd = Color.Red;
        private Color KBaselineColourDescriptor = Color.Gold;
        #endregion

        #region Data members
        private float iDomain = 0.01f;
        #endregion
    }
}
