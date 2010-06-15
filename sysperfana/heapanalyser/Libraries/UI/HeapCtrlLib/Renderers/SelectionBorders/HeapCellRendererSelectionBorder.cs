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

namespace HeapCtrlLib.Renderers.SelectionBorders
{
	internal class HeapCellRendererSelectionBorder : HeapCellRendererColour, IHeapCellRendererSelectionBorder
	{
        #region Constructors & destructor
		public HeapCellRendererSelectionBorder()
            : this( Color.Maroon, Color.Green )
		{
        }

        public HeapCellRendererSelectionBorder( Color aMouse, Color aKeyboard )
        {
            iColorForSelectionByKeyboard = aKeyboard;
            iColorForSelectionByMouse = aMouse;
        }
        #endregion

        #region API
        protected virtual void FlushLines( Graphics aGraphics, List<HeapCellRendererSelectionBorderItem> aLines )
        {
            int lineCount = aLines.Count;
            for ( int i = 0; i < lineCount; i++ )
            {
                HeapCellRendererSelectionBorderItem spec = aLines[ i ];
                spec.Draw( aGraphics );
            }
        }
        #endregion

        #region Properties
        protected virtual Color GetBorderProperties( THeapSelectionBorderType aType, out float aWidth )
        {
            Color ret = Color.Black;
            aWidth = 1.0f;
            //
            switch( aType )
            {
            default:
                break;
            case THeapSelectionBorderType.ESelectionKeyboard:
                aWidth = KBorderWidthSelectedCellByKeyboard;
                ret = iColorForSelectionByKeyboard;
                break;
            case THeapSelectionBorderType.ESelectionMouse:
                aWidth = KBorderWidthSelectedCellByMouse;
                ret = iColorForSelectionByMouse;
                break;
            }
            //
            return ret;
        }
        #endregion

        #region From IHeapCellBorderRenderer
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
            FlushLines( aGraphics, Lines );
            iLines.Clear();
        }

        public void PaintSelectionBorder( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize, THeapSelectionBorderType aType )
        {
            float width;
            Color color = GetBorderProperties( aType, out width );
            SymRect rect = new SymRect( aPosition, aBoxSize + aPaddingSize );

            if ( aMetaData.Borders[ THeapCellBorderType.ELeft ] ) // Draw left-hand outline
            {
                PaintCellBorder( aGraphics, aMetaData, rect.TopLeft, rect.BottomLeft, aAddress, aCell, width, color, THeapCellBorderType.ELeft );
            }
            if  (  aMetaData.Borders[ THeapCellBorderType.ETop ] ) // Draw top-side outline
            {
                PaintCellBorder( aGraphics, aMetaData, rect.TopLeft, rect.TopRight, aAddress, aCell, width, color, THeapCellBorderType.ETop );
            }
            if  (  aMetaData.Borders[ THeapCellBorderType.ERight ] ) // Draw right-hand outline
            {
                PaintCellBorder( aGraphics, aMetaData, rect.TopRight, rect.BottomRight, aAddress, aCell, width, color, THeapCellBorderType.ERight );
            }
            if  (  aMetaData.Borders[ THeapCellBorderType.EBottom ] ) // Draw bottom-side outline
            {
                PaintCellBorder( aGraphics, aMetaData, rect.BottomLeft, rect.BottomRight, aAddress, aCell, width, color, THeapCellBorderType.EBottom );
            }
        }
        #endregion

        #region Internal constants
        private const float KBorderWidthSelectedCellByKeyboard = 3.0f;
        private const float KBorderWidthSelectedCellByMouse = 3.0f;
        #endregion

        #region Internal methods
        private List<HeapCellRendererSelectionBorderItem> Lines
        {
            get { return iLines; }
        }

        private void PaintCellBorder( Graphics aGraphics, HeapCellMetaData aMetaData, Point aStart, Point aEnd, uint aAddress, HeapCell aCell, float aWidth, Color aColour, THeapCellBorderType aType )
        {
            HeapCellRendererSelectionBorderItem spec = new HeapCellRendererSelectionBorderItem( aStart, aEnd, aWidth, aColour, aType );
            iLines.Add( spec );
        }
        #endregion

        #region Data members
        private readonly Color iColorForSelectionByKeyboard;
        private readonly Color iColorForSelectionByMouse;
        private List<HeapCellRendererSelectionBorderItem> iLines = new List<HeapCellRendererSelectionBorderItem>();
        #endregion
    }
}
