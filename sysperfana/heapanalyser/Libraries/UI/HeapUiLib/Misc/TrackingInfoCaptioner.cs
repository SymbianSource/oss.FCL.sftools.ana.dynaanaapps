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
using System.Collections.Generic;
using System.Text;
using SymbianStructuresLib.Debug.Symbols;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapUiLib.Controls;

namespace HeapUiLib.Misc
{
    internal class TrackingInfoCaptioner
    {
        #region Constructors & destructor
        public TrackingInfoCaptioner( TrackingInfo aInfo )
        {
            iTrackingInfo = aInfo;
        }

        public TrackingInfoCaptioner( string aText, TFilterType aFilterType )
        {
            iText = aText;
            iFilterType = aFilterType;
        }
        #endregion

        #region Properties
        public TrackingInfo TrackingInfo
        {
            get { return iTrackingInfo; }
        }

        public TFilterType FilterType
        {
            get { return iFilterType; }
        }

        public Symbol Symbol
        {
            get
            {
                Symbol ret = null;
                //
                if ( iTrackingInfo != null && iTrackingInfo.Symbol != null )
                {
                    ret = iTrackingInfo.Symbol;
                }
                //
                return ret;
            }
        }

        public bool IsSymbolBasedAllocationEntry
        {
            get { return iFilterType == TFilterType.EFilterShowCellsAllocatedByType; }
        }
        #endregion

        #region From System.Object
        public override string ToString()
        {
            if ( iText != null )
            {
                return iText;
            }

            System.Diagnostics.Debug.Assert( iTrackingInfo != null );

            string captionFormat = "[A{0}] [{1:d5}] [{2:d8}] {3}";
            string prefix = "U";
            string postfix = "Unknown Cell Types";
            //
            if ( !iTrackingInfo.IsUnknownSymbolMatchItem )
            {
                prefix = "S";
                postfix = iTrackingInfo.Symbol.NameWithoutVTablePrefix;
            }
            //
            string ret = String.Format( captionFormat, prefix, iTrackingInfo.Count, iTrackingInfo.AssociatedMemory, postfix );
            return ret;
        }
        #endregion

        #region Data members
        private readonly TrackingInfo iTrackingInfo;
        private string iText = null;
        private TFilterType iFilterType = TFilterType.EFilterShowCellsAllocatedByType;
        #endregion
    }
}
