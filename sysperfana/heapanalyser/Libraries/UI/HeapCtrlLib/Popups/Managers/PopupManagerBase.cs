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
using System.Windows.Forms;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Popups.Forms;
using HeapLib.Array;
using HeapLib.Cells;
using HeapLib.Statistics;
using SymbianUtils.RawItems;
using SymbianUtils.Graphics;

namespace HeapCtrlLib.Popups.Managers
{
	internal abstract class PopupManagerBase : IHeapCellPopupManager
	{
        #region Constructors & destructor
		public PopupManagerBase()
		{
        }
        #endregion

        #region New framework
        public virtual bool SupportsRawItemInfoImpl
        {
            get { return false; }
        }

        public virtual void PopupShowAsyncImpl( HeapCell aCell, RawItem aItem, HeapStatistics aStats, Point aLocalControlCoordiantes, Point aScreenCoordinates, Size aOffsetBy, System.Windows.Forms.KeyEventHandler aKeyHandler )
        {
        }

        public abstract void PopupShowAsyncImpl( HeapCellArrayWithStatistics aCells, HeapStatistics aStats, Point aLocalControlCoordinates, Point aScreenCoordinates, Size aOffsetBy, System.Windows.Forms.KeyEventHandler aKeyHandler );
        #endregion

        #region IHeapCellPopupManager Members
        public bool Visible
        {
            get { return ( iActivePopup != null && iActivePopup.Visible ); }
        }

        public PopupBase Popup
        {
            get { return iActivePopup; }
        }

        public bool SupportsRawItemInfo
        {
            get { return SupportsRawItemInfoImpl; }
        }

        public void PopupHide()
        {
            lock( this )
            {
                if  ( iActivePopup != null )
                {
                    iActivePopup.PopupHide();
                    iActivePopup.Dispose();
                    iActivePopup = null;
                }
            }
        }

        public void PopupShowAsync( HeapCell aCell, RawItem aItem, HeapStatistics aStats, Point aLocalControlCoordinates, Point aScreenCoordinates, Size aOffsetBy, System.Windows.Forms.KeyEventHandler aKeyHandler )
        {
            if ( SupportsRawItemInfo )
            {
                lock ( this )
                {
                    PopupShowAsyncImpl( aCell, aItem, aStats, aLocalControlCoordinates, aScreenCoordinates, aOffsetBy, aKeyHandler );
                }
            }
        }

        public void PopupShowAsync( HeapCellArrayWithStatistics aCells, HeapStatistics aStats, Point aLocalControlCoordinates, Point aScreenCoordinates, Size aOffsetBy, System.Windows.Forms.KeyEventHandler aKeyHandler )
        {
            lock ( this )
            {
                PopupShowAsyncImpl( aCells, aStats, aLocalControlCoordinates, aScreenCoordinates, aOffsetBy, aKeyHandler );
            }
        }

        public void PopupRelocate( HeapCellArrayWithStatistics aCells, HeapStatistics aStats, Point aLocalControlCoordiantes, Point aScreenCoordinates, Size aOffsetBy )
        {
            if  ( iActivePopup != null && Visible )
            {
                lock( this )
                {
                    iActivePopup.PopupRelocate( aScreenCoordinates, aCells, aStats, aOffsetBy );
                }
            }
        }
        #endregion

        #region Data members
        protected PopupBase iActivePopup = null;
        #endregion
    }
}
