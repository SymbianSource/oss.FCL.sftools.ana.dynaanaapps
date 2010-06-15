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
using HeapCtrlLib.Types;

namespace HeapCtrlLib.Factories
{
	internal abstract class Factory
	{
        #region Constructors & destructor
		public static Factory CreateByType( THeapCtrlRenderingType aType )
		{
            Factory ret = null;
            //
            switch( aType )
            {
                case THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell:
                    ret = new FactoryByCellType();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeBySymbol:
                    ret = new FactoryBySymbol();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeByObject:
                    ret = new FactoryByObject();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeByAge:
                    ret = new FactoryByAge();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeByLength:
                    ret = new FactoryByLength();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeByIsolation:
                    ret = new FactoryByIsolation();
                    break;
                case THeapCtrlRenderingType.EHeapCellRenderingTypeByEmbeddedReferences:
                    ret = new FactoryByEmbeddedReferences();
                    break;
            }
            //
            if  ( ret == null )
            {
                throw new NotSupportedException();
            }
            else if ( ret.Renderers.CheckIfAnyItemsInvalid() )
            {
                throw new Exception( "One or more renderers were not initialized" );
            }
            //
            return ret;
		}
        #endregion

        #region API
        public virtual Size CellBoxSize( THeapCtrlZoom aZoom )
        {
            Size ret = new Size( 0, 0 );
            //
            switch ( aZoom )
            {
            case THeapCtrlZoom.EHeapCtrlZoomSmall:
                ret = new Size( 13, 13 );
                break;
            default:
            case THeapCtrlZoom.EHeapCtrlZoomMedium:
                ret = new Size( 25, 25 );
                break;
            case THeapCtrlZoom.EHeapCtrlZoomMaximum:
                ret = new Size( 39, 39 );
                break;
            }
            //
            return ret;
        }

        public virtual Size CellPadding( THeapCtrlZoom aZoom )
        {
            Size ret = new Size( 0, 0 );
            //
            switch ( aZoom )
            {
            case THeapCtrlZoom.EHeapCtrlZoomSmall:
                ret = new Size( 0, 0 );
                break;
            default:
            case THeapCtrlZoom.EHeapCtrlZoomMedium:
                ret = new Size( 0, 0 );
                break;
            case THeapCtrlZoom.EHeapCtrlZoomMaximum:
                ret = new Size( 0, 0 );
                break;
            }
            //
            return ret;
        }
        #endregion

        #region Properties
        public FactoryRenderers Renderers
        {
            get { return iRenderers; }
        }

        public IHeapCellPopupManager PopupManager
        {
            get { return iPopupManager; }
            protected set { iPopupManager = value; }
        }
        #endregion

        #region Data members
        private IHeapCellPopupManager iPopupManager;
        private FactoryRenderers iRenderers = new FactoryRenderers();
        #endregion
    }
}
