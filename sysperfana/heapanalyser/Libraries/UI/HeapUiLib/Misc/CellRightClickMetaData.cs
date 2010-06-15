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
using System.ComponentModel;
using System.Windows.Forms;
using System.IO;
using System.Text;
using SymbianUtils;
using SymbianUtils.XRef;
using SymbianUtils.RawItems;
using SymbianUtils.FileSystem.FilePair;
using SymbianZipLib.GUI;
using ZedGraph;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Reconstructor;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapUiLib.Dialogs;
using HeapUiLib.SubForms;
using HeapUiLib.Controls;
using HeapUiLib.Misc;
using HeapCtrlLib;

namespace HeapUiLib.Misc
{
    internal class CellRightClickMetaData
    {
        #region Constructors & destructor
        public CellRightClickMetaData( HeapCell aCell, RawItem aRawItem )
        {
            iCell = aCell;
            iRawItem = aRawItem;
        }
        #endregion

        #region API

        #endregion

        #region Properties
        public HeapCell Cell
        {
            get { return iCell; }
        }

        public RawItem RawItem
        {
            get { return iRawItem; }
        }

        public RelationshipManager RelationshipManager
        {
            get { return Cell.RelationshipManager; }
        }
        #endregion

        #region Data members
        private readonly HeapCell iCell;
        private readonly RawItem iRawItem;
        #endregion
    }
}
