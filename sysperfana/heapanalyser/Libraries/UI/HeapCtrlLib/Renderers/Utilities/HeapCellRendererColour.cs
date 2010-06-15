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
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers.Utilities;
using HeapLib;
using HeapLib.Array;
using HeapLib.Cells;
using SymbianUtils.Graphics;
using SymbianUtils.Colour;

namespace HeapCtrlLib.Renderers.Utilities
{
	public class HeapCellRendererColour
	{
        #region Constructors & destructor
		public HeapCellRendererColour()
		{
        }
        #endregion

        #region Drawing
        protected virtual void PaintBorders( HeapCellBorderInfo aBorders, Graphics aGraphics, SymRect aRect, Pen aPen )
        {
            if ( aBorders[ THeapCellBorderType.ELeft ] )
            {
                aGraphics.DrawLine( aPen, aRect.TopLeft, aRect.BottomLeft );
            }
            if ( aBorders[ THeapCellBorderType.ERight ] )
            {
                aGraphics.DrawLine( aPen, aRect.TopRight, aRect.BottomRight );
            }
            if ( aBorders[ THeapCellBorderType.ETop ] )
            {
                aGraphics.DrawLine( aPen, aRect.TopLeft, aRect.TopRight );
            }
            if ( aBorders[ THeapCellBorderType.EBottom ] )
            {
                aGraphics.DrawLine( aPen, aRect.BottomLeft, aRect.BottomRight );
            }
        }
        #endregion

        #region Colouring
        public static Color ColourByCellType( HeapCell aCell )
        {
            bool isUnknown = false;
            return ColourByCellType( aCell, out isUnknown );
        }

        public static Color ColourByCellType( HeapCell aCell, out bool aIsUnknown )
        {
            aIsUnknown = false;
            Color ret = Color.WhiteSmoke;
            //
            switch ( aCell.Type )
            {
                case HeapCell.TType.EAllocated:
                    {
                        if ( aCell.IsDescriptor )
                        {
                            ret = CellAllocatedDescriptor;
                        }
                        else if ( aCell.Symbol != null )
                        {
                            ret = CellAllocatedWithSymbol;
                        }
                        else
                        {
                            aIsUnknown = true;
                            ret = CellAllocatedUnknown;
                        }
                        break;
                    }
                case HeapCell.TType.EFree:
                    ret = CellFree;
                    break;
            }
            //
            return ret;
        }

        public static Color CellFillColourByRegion( HeapCell aCell, uint aAddress, out HeapCell.TRegion aRegion )
        {
            // Cell coloring
            aRegion = aCell.RegionForAddress( aAddress );

            Color fillColour = Color.White;
            switch( aRegion )
            {
            case HeapCell.TRegion.EHeader:
                fillColour = Color.GhostWhite;
                break;
            case HeapCell.TRegion.EPayload:
                fillColour = ColourByCellType( aCell );
                break;
            default:
                System.Diagnostics.Debug.Assert( false );
                break;
            }

            return fillColour;
        }

        public static Color RampedCellFillColourByRegion( HeapCell aCell, int aCellBoxCount, uint aAddress, out HeapCell.TRegion aRegion )
        {
            // Get the cell colour to use and also the region associated with
            // the cell address.
            Color fillColour = CellFillColourByRegion( aCell, aAddress, out aRegion );

            // If we're rendering the payload we'll want to ramp the cell colour
            // to make it easier to read.
            if  ( aRegion == HeapCell.TRegion.EPayload )
            {
                fillColour = RampedColourByBoxNumber( fillColour, aCellBoxCount, aCell.Address, aAddress );
            }

            return fillColour;
        }

        public static Color RampedColourByBoxNumber( Color aBaseline, int aBoxCount, uint aCellBaseAddress, uint aAddress )
        {
            float cellAddressOffset = (float) ( ( aAddress - aCellBaseAddress ) / 4 );

            Color dark = ColourUtils.Darken( aBaseline, 0.30f );
            Color light = ColourUtils.Lighten( aBaseline, 0.30f );

            // This is the baseline brightness for the colour
            float brightnessBaseline = light.GetBrightness() - dark.GetBrightness();
    
            // This is how much brightness we can apply to each box.
            float brightnessPerBox = ( brightnessBaseline / aBoxCount );
          
            // This is the brightness of the target box
            float brightnessPercentage = brightnessPerBox * cellAddressOffset; 

            Color ret = ColourUtils.Lighten( aBaseline, brightnessPercentage );
            return ret;
        }

        public static Color RampedColourByIntensityRange( Color aMaxIntensity, long aValue, long aMin, long aMax )
        {
            // Calculate the difference between the maximum and minimum. This tells us how many different
            // "intensity" values we have to work with.
            long valueSpan = ( aMax - aMin );

            // Get the brightness of the baseline colour and then scale it based upon the number of different
            // value intensities we must cope with.
            float brightness = aMaxIntensity.GetBrightness();
            float brightnessPerValue = ( brightness / (float) valueSpan );

            // Ramp the cell colour based upon the index
            float percentage = brightness - ( brightnessPerValue * ( aValue - aMin ) );

            Color ret = ColourUtils.Darken( aMaxIntensity, percentage );
            return ret;
        }
        #endregion

        #region Properties
        public static Color CellAllocatedDescriptor
        {
            get { return Color.FromKnownColor( KCellAllocatedDescriptor ); }
        }

        public static Color CellAllocatedUnknown
        {
            get { return Color.FromKnownColor( KCellAllocatedUnknown ); }
        }

        public static Color CellAllocatedWithSymbol
        {
            get { return Color.FromKnownColor( KCellAllocatedWithSymbol ); }
        }

        public static Color CellFree
        {
            get { return Color.FromKnownColor( KCellFree ); }
        }
        #endregion

        #region Constants
        public const KnownColor KCellAllocatedDescriptor = KnownColor.Gold;
        public const KnownColor KCellAllocatedUnknown = KnownColor.Firebrick;
        public const KnownColor KCellAllocatedWithSymbol = KnownColor.OrangeRed;
        public const KnownColor KCellFree = KnownColor.RoyalBlue;
        #endregion
    }
}
