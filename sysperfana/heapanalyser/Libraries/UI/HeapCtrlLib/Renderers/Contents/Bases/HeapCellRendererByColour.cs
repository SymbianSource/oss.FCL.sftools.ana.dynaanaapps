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
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Utilities;
using HeapLib;
using SymbianUtils.Colour;
using HeapCtrlLib.Renderers.Utilities;

namespace HeapCtrlLib.Renderers.Contents.Bases
{
    public class HeapCellRendererByColour : HeapCellRendererWithText
    {
        #region Constructors & destructor
        public HeapCellRendererByColour()
        {
            // Add a few extra banned colours that are specific to this app
            iColorUtil.BannedColors.Add( HeapCellRendererColour.KCellAllocatedDescriptor );
            iColorUtil.BannedColors.Add( HeapCellRendererColour.KCellAllocatedUnknown );
            iColorUtil.BannedColors.Add( HeapCellRendererColour.KCellAllocatedWithSymbol );
            iColorUtil.BannedColors.Add( HeapCellRendererColour.KCellFree );
        }
        #endregion

        #region API
        protected Color GenerateRandomColour( Random aRandomNumberGenerator )
        {
            return iColorUtil.GenerateRandomColour( aRandomNumberGenerator );
        }
        #endregion

        #region Internal methods
        protected List<KnownColor> StandardColours
        {
            get
            {
                return iColorUtil.CreateStandardColors();
            }
        }

        protected List<KnownColor> BannedColours
        {
            get
            {
                return iColorUtil.CreateBannedColors();
            }
        }
        #endregion

        #region Data members
        private ColourGenerationUtil iColorUtil = new ColourGenerationUtil();
        #endregion
    }
}
