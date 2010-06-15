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
using SymbianUtils;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers.Utilities;

namespace HeapCtrlLib.Renderers.Headers
{
	internal class HeapCellRendererRowHeader : DisposableObject, IHeapCellRendererRowHeader
	{
        #region Constructors & destructor
		public HeapCellRendererRowHeader()
		{
            iFont = new Font( KFontName, KFontSize, KFontStyle );
        }
        #endregion

        #region From DisposableObject
        protected override void CleanupManagedResources()
        {
            try
            {
                iFont.Dispose();
            }
            finally
            {
                base.CleanupManagedResources();
            }
        }
        #endregion

        #region From HeapCellRendererRowHeader
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

        public int MeasureCellHeaderText( Graphics aGraphics )
        {
            int size = ( int ) ( aGraphics.MeasureString( "00000000", iFont ).Width + KHeaderTextPaddingAmountInPixels );
            return size;
        }

        public void PaintRowHeader( Graphics aGraphics, Point aPosition, Size aSize, uint aAddress )
        {
            Rectangle textRect = new Rectangle( aPosition, aSize );
            string addressText = aAddress.ToString( "x8" );
            //
            using( SolidBrush textBrush = new SolidBrush( Color.Black ) )
            {
                using( StringFormat format = new StringFormat() )
                {
                    format.Alignment = StringAlignment.Center;
                    format.LineAlignment = StringAlignment.Center;
                    //
                    aGraphics.DrawString( addressText, iFont, textBrush, textRect, format );
                }
            }
        }
        #endregion

        #region Constants
        private const string KFontName = "Lucida Console";
        private const float KFontSize = 7.25f;
        private const FontStyle KFontStyle = FontStyle.Bold;
        private const float KHeaderTextPaddingAmountInPixels = 10.0f;
        #endregion

        #region Data members
        private readonly System.Drawing.Font iFont;
        #endregion
    }
}
