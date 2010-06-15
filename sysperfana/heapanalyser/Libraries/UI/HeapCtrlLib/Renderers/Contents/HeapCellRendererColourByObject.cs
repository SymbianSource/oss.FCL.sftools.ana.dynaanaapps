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
using System.Collections.Generic;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapLib.Statistics.Tracking.Base;
using SymbianUtils.Graphics;
using HeapCtrlLib.Dialogs;
using HeapCtrlLib.Factories;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Renderers.Utilities;
using HeapCtrlLib.Renderers.Contents.Bases;

namespace HeapCtrlLib.Renderers.Contents
{
    internal class HeapCellRendererColourByObject : HeapCellRendererByColour, IHeapCellRendererContent 
    {
        #region Constructors & destructor
        public HeapCellRendererColourByObject()
        {
        }
        #endregion

        #region From IHeapCellRendererContent
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            iRenderer = aRenderer;
            //
            iFactory = (FactoryByObject) aRenderer.Factory;
            iFactory.PrepareColourHashes( aReconstructor, StandardColours );
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
            iLargestRectangleCalculator = new HeapCellLargestRectangleCalculator( aNavigator );
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
            Color fillColour = ColorForCell( aCell );
            //
            string text = "Unknown";
            if ( aCell.Symbol != null )
            {
                text = aCell.Symbol.ObjectWithoutSection;
            }
            //
            PaintBoxedTextWithLuminanceHandling( text, aGraphics, fillColour );
        }

        public void RenderingComplete( Graphics aGraphics )
        {
        }

        public void PaintContent( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize )
        {
            // Get the cell colour that is associated with the cell symbol object type
            aMetaData.CellBoxColor = RampedColourByBoxNumber( ColorForCell( aCell ), aMetaData.CellBoxCount, aCell.Address, aAddress );

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
            get { return true; }
        }

        public void SetupFilters()
        {
            if ( iRenderer != null )
            {
                HeapCellFilterTripletDictionary dictionary = new HeapCellFilterTripletDictionary( ColourHashes );
                HeapRendererFilterConfigDialog dialog = new HeapRendererFilterConfigDialog( dictionary );
                if ( dialog.ShowDialog() == System.Windows.Forms.DialogResult.OK )
                {
                    ColourHashes = dialog.Dictionary;
                    iRenderer.Invalidate();
                }
            }
        }
        #endregion

        #region Internal methods
        private HeapCellFilterTripletDictionary ColourHashes
        {
            get { return iFactory.ColourHashes; }
            set { iFactory.ColourHashes = value; }
        }

        private Color ColorForCell( HeapCell aCell )
        {
            Color fillColour = HeapCellRendererColour.ColourByCellType( aCell );
            //
            if ( aCell.Type == HeapCell.TType.EAllocated && !aCell.IsUnknown )
            {
                string hash = aCell.Symbol.ObjectWithoutSection;
                //
                HeapCellFilterTriplet val = ColourHashes[ hash ];
                System.Diagnostics.Debug.Assert( val != null );
                fillColour = val.Color;
            }
            //
            return fillColour;
        }
        #endregion

        #region Data members
        private HeapDataRenderer iRenderer = null;
        private FactoryByObject iFactory = null;
        #endregion
    }
}
