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
using SymbianUtils.Colour;
using SymbianUtils.Graphics;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers.Utilities;

namespace HeapCtrlLib.Renderers.Contents.Bases
{
    public class HeapCellRendererWithText : HeapCellRendererColour 
    {
        #region Constructors & destructor
        public HeapCellRendererWithText()
        {
        }
        #endregion
        
        #region Constants
        private const float KSymbolFontSizeInitial = 9.0f;
        private const float KSymbolFontSizeMinimum = 6.0f;
        private const float KSymbolFontSizeDecrement = 0.5f;
        private const string KSymbolFontName = "Tahoma";
        #endregion

        #region Protected properties
        protected HeapCellLargestRectangleCalculator RectangleCalculator
        {
            get { return iLargestRectangleCalculator; }
        }
        #endregion

        #region Protected drawing utilities
        protected void PaintBoxedTextWithLuminanceHandling( string aText, Graphics aGraphics, Color aBackgroundBaselineColour )
        {
            SymRect rectangle = new SymRect( iLargestRectangleCalculator.Rectangle );
            PaintBoxedTextWithLuminanceHandling( aText, aGraphics, aBackgroundBaselineColour, rectangle );
        }

        protected void PaintBoxedTextWithLuminanceHandling( string aText, Graphics aGraphics, Color aBackgroundBaselineColour, SymRect aRectangle )
        {
            Color textCol = Color.Black;
            if ( aBackgroundBaselineColour.GetBrightness() < 0.4 )
            {
                textCol = Color.White;
            }
            //
            PaintBoxedText( aText, aGraphics, textCol, aRectangle );
        }

        protected void PaintBoxedText( string aText, Graphics aGraphics, Color aTextColor )
        {
            SymRect rectangle = new SymRect( iLargestRectangleCalculator.Rectangle );
            PaintBoxedText( aText, aGraphics, aTextColor, rectangle );
        }

        protected void PaintBoxedText( string aText, Graphics aGraphics, Color aTextColor, SymRect aRectangle )
        {
            using( StringFormat stringFormat = new StringFormat() )
            {
                stringFormat.Alignment = StringAlignment.Center;
                stringFormat.LineAlignment = StringAlignment.Center;

                for( float fontSize = KSymbolFontSizeInitial; fontSize >= KSymbolFontSizeMinimum; fontSize -= KSymbolFontSizeDecrement )
                {
                    using( System.Drawing.Font font = new Font( KSymbolFontName, fontSize, FontStyle.Bold ) )
                    {
                        SizeF renderSizeF = aGraphics.MeasureString( aText, font, aRectangle.Width, stringFormat );
                        Size renderSize = new Size( (int) renderSizeF.Width, (int) renderSizeF.Height );
                        //
                        if ( renderSize.Width <= aRectangle.Width && renderSize.Height <= aRectangle.Height )
                        {
                            using( SolidBrush brush = new SolidBrush( aTextColor ) )
                            {
                                aGraphics.DrawString( aText, font, brush, aRectangle.Rectangle, stringFormat );
                                break;
                            }
                        }
                    }
                }
            }
        }
        #endregion

        #region Data members
        protected HeapCellLargestRectangleCalculator iLargestRectangleCalculator;
        #endregion
    }
}
