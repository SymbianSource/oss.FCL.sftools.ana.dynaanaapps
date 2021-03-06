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
using System.Collections.Generic;
using SymbianUtils.Graphics;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Renderers.Utilities;
using HeapLib;
using HeapLib.Array;
using HeapLib.Cells;
using HeapLib.Reconstructor;

namespace HeapCtrlLib.Renderers.ContentBorders
{
	internal class HeapCellRendererContentBorderFloating : HeapCellRendererColour, IHeapCellRendererContentBorder
	{
        #region Constructors & destructor
        public HeapCellRendererContentBorderFloating()
		{
        }
        #endregion

        #region API
        #endregion

        #region Properties
        #endregion

        #region From IHeapCellRendererContentBorder
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
        }

        public void RenderingComplete( Graphics aGraphics )
        {
        }

        public void PaintContentBorder( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize )
        {
            SymRect boxRect = new SymRect( aPosition, aBoxSize );
            boxRect.HalfOffset( aPaddingSize );
            aGraphics.DrawRectangle( Pens.Black, boxRect.Rectangle );

            // If we're drawing the header, we can decorate the header with an 
            // additional line.
            HeapCell.TRegion region = aCell.RegionForAddress( aAddress );
            if ( region == HeapCell.TRegion.EHeader )
            {
                Point linePosStart = boxRect.TopLeft;
                linePosStart.X += KHeaderBoxLineCornerOffset;
                Point linePosEnd = boxRect.TopLeft;
                linePosEnd.Y += KHeaderBoxLineCornerOffset;
                //
                aGraphics.DrawLine( Pens.Black, linePosStart, linePosEnd );
            }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Internal constants
        private const int KHeaderBoxLineCornerOffset = 5;
        #endregion

        #region Data members
        #endregion
    }
}
