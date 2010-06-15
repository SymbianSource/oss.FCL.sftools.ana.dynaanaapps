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
using HeapCtrlLib.Utilities;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapCtrlLib.Renderers;

namespace HeapCtrlLib.Factories
{
    internal class FactoryRenderers
    {
        #region Constructors
        public FactoryRenderers()
        {
        }
        #endregion

        #region API
        public void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer )
        {
            SelectionBorder.Initialise( aCells, aReconstructor, aRenderer );
            Header.Initialise( aCells, aReconstructor, aRenderer );
            Content.Initialise( aCells, aReconstructor, aRenderer );
            ContentBorder.Initialise( aCells, aReconstructor, aRenderer );
        }

        public void PrepareToNavigate( HeapRenderingNavigator aNavigator )
        {
            SelectionBorder.PrepareToNavigate( aNavigator );
            Header.PrepareToNavigate( aNavigator );
            Content.PrepareToNavigate( aNavigator );
            ContentBorder.PrepareToNavigate( aNavigator );
        }

        public void RenderingComplete( Graphics aGraphics )
        {
            SelectionBorder.RenderingComplete( aGraphics );
            Header.RenderingComplete( aGraphics );
            Content.RenderingComplete( aGraphics );
            ContentBorder.RenderingComplete( aGraphics );
        }

        public void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData )
        {
            SelectionBorder.HeapCellRenderingComplete( aGraphics, aCell, aMetaData );
            Header.HeapCellRenderingComplete( aGraphics, aCell, aMetaData );
            Content.HeapCellRenderingComplete( aGraphics, aCell, aMetaData );
            ContentBorder.HeapCellRenderingComplete( aGraphics, aCell, aMetaData );
        }
        
        public bool CheckIfAnyItemsInvalid()
        {
            bool invalid = ( iHeader == null || iBorderContent == null || iBorderSelection == null || iContent == null );
            return invalid;
        }
        #endregion

        #region Properties
        public IHeapCellRendererRowHeader Header
        {
            get { return iHeader; }
            set { iHeader = value; }
        }

        public IHeapCellRendererSelectionBorder SelectionBorder
        {
            get { return iBorderSelection; }
            set { iBorderSelection = value; }
        }

        public IHeapCellRendererContent Content
        {
            get { return iContent; }
            set { iContent = value; }
        }

        public IHeapCellRendererContentBorder ContentBorder
        {
            get { return iBorderContent; }
            set { iBorderContent = value; }
        }
        #endregion

        #region Data members
        private IHeapCellRendererRowHeader iHeader;
        private IHeapCellRendererSelectionBorder iBorderSelection;
        private IHeapCellRendererContent iContent;
        private IHeapCellRendererContentBorder iBorderContent;
        #endregion
    }
}
