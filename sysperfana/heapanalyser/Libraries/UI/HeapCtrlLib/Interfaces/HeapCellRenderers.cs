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
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapLib;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Renderers;

namespace HeapCtrlLib.Interfaces
{
    internal interface IHeapCellRendererBase
    {
        void Initialise( HeapCellArray aCells, HeapReconstructor aReconstructor, HeapDataRenderer aRenderer );
        void RenderingComplete( Graphics aGraphics );
        void PrepareToNavigate( HeapRenderingNavigator aNavigator );
        void HeapCellRenderingComplete( Graphics aGraphics, HeapCell aCell, HeapCellMetaData aMetaData );
    }

    // Paints the address label at the start of each row
    internal interface IHeapCellRendererRowHeader : IHeapCellRendererBase
    {
        int MeasureCellHeaderText( Graphics aGraphics );
        void PaintRowHeader( Graphics aGraphics, Point aPosition, Size aSize, uint aAddress );
    }

    // Paints the selection outline for keyboard and mouse selected heap cells
    internal interface IHeapCellRendererSelectionBorder : IHeapCellRendererBase
    {
        void PaintSelectionBorder( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize, THeapSelectionBorderType aType );
    }

    // Paints any outline around individual heap cell boxes.
    internal interface IHeapCellRendererContentBorder : IHeapCellRendererBase
    {
        void PaintContentBorder( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize );
    }

    // Paints heap cell box content
    internal interface IHeapCellRendererContent : IHeapCellRendererBase
    {
        bool SupportsFiltering { get; }
        void SetupFilters();
        void PaintContent( Graphics aGraphics, HeapCellMetaData aMetaData, HeapCell aCell, uint aAddress, Point aPosition, Size aBoxSize, Size aPaddingSize );
    }
}
