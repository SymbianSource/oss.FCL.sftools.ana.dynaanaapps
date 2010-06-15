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
using System.Text;
using System.Collections;
using System.Collections.Generic;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Popups.Managers;
using HeapCtrlLib.Renderers.Headers;
using HeapCtrlLib.Renderers.SelectionBorders;
using HeapCtrlLib.Renderers.Contents;
using HeapCtrlLib.Renderers.ContentBorders;
using HeapCtrlLib.Utilities;
using HeapLib.Reconstructor;
using HeapLib.Statistics.Tracking.Base;
using SymbianUtils.Colour;

namespace HeapCtrlLib.Factories
{
	internal class FactoryBySymbol : Factory
	{
        #region Constructors & destructor
        public FactoryBySymbol()
		{
            base.Renderers.Header = new HeapCellRendererRowHeader();
            base.Renderers.SelectionBorder = new HeapCellRendererSelectionBorder();
            base.Renderers.Content = new HeapCellRendererColourBySymbol();
            base.Renderers.ContentBorder = new HeapCellRendererContentBorder3d();
            //
            PopupManager = new PopupManagerBySymbol();
        }
        #endregion

        #region API
        internal void PrepareColourHashes( HeapReconstructor aReconstructor, List<KnownColor> aStandardColours )
        {
            if ( iColourHashes.Count == 0 )
            {
                // Pick colours at random from the standard colours until we have used them all
                System.Random random = new Random( 0 );

                // How many objects do we have to colourise?
                int count = aReconstructor.Statistics.StatsAllocated.TrackerSymbols.Count;
                for ( int i = 0; i < count; i++ )
                {
                    // Get associated symbol item
                    TrackingInfo item = aReconstructor.Statistics.StatsAllocated.TrackerSymbols[ i ];

                    // Form caption
                    string caption = item.Symbol.NameWithoutVTablePrefix;

                    // If we still have some system colours left, then we'll
                    // try to use them up. Otherwise, we'll resort to generating
                    // random colors.
                    Color col = Color.Black;
                    //
                    if ( aStandardColours.Count > 0 )
                    {
                        // Get system colour index
                        int colItemIndex = random.Next( 0, aStandardColours.Count - 1 );

                        // Link colour with symbol
                        KnownColor knownCol = (KnownColor) aStandardColours[ colItemIndex ];
                        col = Color.FromKnownColor( knownCol );

                        // Remove known colour so we don't use it again
                        aStandardColours.RemoveAt( colItemIndex );
                    }
                    else
                    {
                        // Get random known colour
                        col = iColorUtil.GenerateRandomColour( random );
                    }

                    // Associate object with colour
                    string hash = HeapCellRendererColourBySymbol.MakeColourHash( item.Symbol );
                    iColourHashes.Add( hash, caption, col, item );
                }
            }
        }
        #endregion

        #region Properties
        internal HeapCellFilterTripletDictionary ColourHashes
        {
            get { return iColourHashes; }
            set { iColourHashes = value; }
        }
        #endregion

        #region Data members
        private HeapCellFilterTripletDictionary iColourHashes = new HeapCellFilterTripletDictionary();
        private ColourGenerationUtil iColorUtil = new ColourGenerationUtil();
        #endregion
    }
}
