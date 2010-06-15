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
using System.Drawing;
using System.Runtime.InteropServices;
using Microsoft.Office.Interop.Excel;
using SymbianExcelUtils;
using SymbianUtils.Colour;

namespace HeapComparisonLib.CSV.Comparers
{
    internal class HeapSize : ComparerBase
	{
		#region Constructors & destructor
		public HeapSize( Worksheet aSheet, string aFileName1, string aFileName2 )
		:	base( aSheet, aFileName1, aFileName2 )
		{
            // Build colour list for shared chunks
            foreach( string colorName in KColourTable )
            {
                iSharedChunkColours.Enqueue( Color.FromName( colorName ) );
            }
		}
		#endregion

		#region API
		public override string DeltaFormula( int aIndex )
		{
            int column = 4;
            //
            switch ( aIndex )
            {
                default:
                case 0:
                    column = 4;
                    break;
                case 1:
                    column = 8;
                    break;
                case 2:
                    column = 12;
                    break;
            }
            //
            return Utils.MakeSummationFormula( 3, Row - 2, column, Sheet );
		}

		public override void PrepareWorksheetReadyForData()
		{
			Sheet.Name = "Heap Size";
			//
			Utils.SetValue( Row, 1, Sheet, "Thread" );
            MakeColumnTitleWithDelta( 2, "Heap Size" );
            Utils.SetValue( Row, 5, Sheet, "" );
            MakeColumnTitleWithDelta( 6, "Free Space" );
            Utils.SetValue( Row, 9, Sheet, "" );
            MakeColumnTitleWithDelta( 10, "Alloc Space" );
            Utils.SetValue( Row, 13, Sheet, "" );
			Utils.SetValue( Row, 14, Sheet, "Max. Heap Size" );
			//
			Utils.MakeBoxedTitleRow( Row, 14, Sheet, 0xFF0000 );
			//
			NextRow();
			NextRow();
		}

		public override void FinaliseWorksheet()
		{
			// Make total row
			NextRow();
			Utils.SetValue( Row, 1, Sheet, "Totals:" );

            // Heap size
			Utils.SetValue( Row, 2, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 2) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 2) + ")" );
			Utils.SetValue( Row, 3, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 3) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 3) + ")" );
			Utils.SetValue( Row, 4, Sheet, DeltaFormula( 0 ) );

            // Free space
            Utils.SetValue( Row, 6, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 6 ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 6 ) + ")" );
            Utils.SetValue( Row, 7, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 7 ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 7 ) + ")" );
            Utils.SetValue( Row, 8, Sheet, DeltaFormula( 1 ) );

            // Alloc space
            Utils.SetValue( Row, 10, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 10 ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 10 ) + ")" );
            Utils.SetValue( Row, 11, Sheet, "=SUM(" + Utils.ColumnAndRowAsExcelIdentifier( 3, 11 ) + ":" + Utils.ColumnAndRowAsExcelIdentifier( Row - 2, 11 ) + ")" );
            Utils.SetValue( Row, 12, Sheet, DeltaFormula( 2 ) );

            Utils.BoldRow( Row, Sheet );

			// Calculate column sizes
			Utils.AutoFitColumn( 1, Sheet, 70.0 );
			Utils.AutoFitColumn( 2, Sheet );
			Utils.AutoFitColumn( 3, Sheet );
			Utils.AutoFitColumn( 4, Sheet );
			Utils.AutoFitColumn( 5, Sheet, 10.0 );
            Utils.AutoFitColumn( 6, Sheet );
            Utils.AutoFitColumn( 7, Sheet );
            Utils.AutoFitColumn( 8, Sheet );
            Utils.AutoFitColumn( 9, Sheet, 10.0 );
            Utils.AutoFitColumn( 10, Sheet );
            Utils.AutoFitColumn( 11, Sheet );
            Utils.AutoFitColumn( 12, Sheet );
            Utils.AutoFitColumn( 13, Sheet, 10.0 );
            Utils.AutoFitColumn( 14, Sheet );
        }

        public override void Compare( CSVThread aMaster, CSVThread aOther )
		{
            // Set thread and make bold if one of the entries is just a default
            // i.e. blank placeholder (occurs when a thread doesn't exist in one or
            // other comparsion lists)
            Range threadName = Utils.SetValue( Row, 1, Sheet, aMaster.FullName );
            if ( aMaster.IsDefault || aOther.IsDefault )
            {
                Utils.GetRangeByColumnAndRow( Row, 1, Row, 1, Sheet ).Font.Bold = true;
            }

            if ( aMaster.IsSharedHeap )
            {
                long chunkHandle = aMaster.ChunkHandle;
                Color backColour = ColourForChunkHandle( chunkHandle );
                threadName.Interior.Color = backColour.ToArgb();

                // Check text will be legible
                Color foreColour = Color.Black;
                float lum = backColour.GetBrightness();
                if ( lum < 0.5 )
                {
                    foreColour = Color.White;
                }

                threadName.Font.Color = foreColour.ToArgb();
            }

            // Column 2 -> Column 4 ====> Heap size comparsion
            CreateComparsion( 2, aMaster, aOther, aMaster.SizeCurrent, aOther.SizeCurrent, -1 );
            
            // Column 5 is a spacer

            // Column 6 -> Column 8 ====> Free space comparsion
            CreateComparsion( 6, aMaster, aOther, aMaster.FreeSpaceTotal, aOther.FreeSpaceTotal, 1 );
            
            // Column 9 is a spacer

            // Column 10 -> Column 12 ====> Alloc space comparsion
            CreateComparsion( 10, aMaster, aOther, aMaster.AllocSpaceTotal, aOther.AllocSpaceTotal, -1 );
            
            // Column 13 is a spacer
            
            // Max heap size is the same for both
            Utils.SetValue( Row, 14, Sheet, aOther.SizeMax.ToString() );

            // Next comes the free cell size

            NextRow();
		}
		#endregion

        #region Internal methods
        private void MakeColumnTitleWithDelta( int aColumn, string aCaption )
        {
            Range r = null;
            //
            r = Utils.SetValue( Row, aColumn, Sheet, aCaption );
            r.AddComment( FileName1 );
            r = Utils.SetValue( Row, aColumn + 1, Sheet, aCaption );
            r.AddComment( FileName2 );
            Utils.SetValue( Row, aColumn + 2, Sheet, "Delta" );
        }

        private void CreateComparsion( int aStartCol, CSVThread aMaster, CSVThread aOther, long aMasterVal, long aOtherVal, int aMultiplier )
        {
            if ( !aMaster.IsDefault )
            {
                Utils.SetValue( Row, aStartCol, Sheet, aMasterVal.ToString() );
            }
            if ( !aOther.IsDefault )
            {
                Utils.SetValue( Row, aStartCol + 1, Sheet, aOtherVal.ToString() );
            }

            // Delta
            long delta = 0;
            

            int colour = 0x000000; // black
            if ( aMultiplier < 0 )
            {
                delta = aMasterVal - aOtherVal;
                if ( delta < 0 )
                {
                    // Original size was smaller than new size => heap has grown
                    colour = 0x0000FF; // red
                }
                else if ( delta > 0 )
                {
                    // Original size was bigger than new size => heap has shrunk
                    colour = 0xFF0000; // blue
                }
            }
            else if ( aMultiplier > 0 )
            {
                delta = ( aMasterVal - aOtherVal ) * -1;
                if ( delta < 0 )
                {
                    // Original size was smaller than new size => heap has grown
                    colour = 0x0000FF; // red
                }
                else if ( delta > 0 )
                {
                    // Original size was bigger than new size => heap has shrunk
                    colour = 0xFF0000; // blue
                }
            }

            Utils.SetValue( Row, aStartCol + 2, Sheet, ( delta * aMultiplier ).ToString() );
            Utils.SetRangeFontColour( Row, aStartCol + 2, Row, aStartCol + 2, Sheet, colour );
        }

        private Color ColourForChunkHandle( long aHandle )
        {
            Color ret = Color.Transparent;

            // Is it already in the dictionary?
            if ( iSharedChunkColourDictionary.ContainsKey( aHandle ) )
            {
                ret = iSharedChunkColourDictionary[ aHandle ];
            }
            else
            {
                // Nope, need to allocate a new one
                if ( iSharedChunkColours.Count > 0 )
                {
                    ret = iSharedChunkColours.Dequeue();
                    iSharedChunkColourDictionary.Add( aHandle, ret );
                }
                else
                {
                    // Run out of colours!
                }
            }

            return ret;
        }
        #endregion

        #region Internal constants
        private readonly static string[] KColourTable = { 
            "AliceBlue",
	        "Azure",
	        "Bisque",
	        "BlanchedAlmond",
	        "BlueViolet",
	        "Brown",
	        "Chartreuse",
	        "Crimson",
	        "Cyan",
	        "DarkGoldenrod",
	        "DarkOliveGreen",
	        "DarkOrange",
	        "DarkOrchid",
	        "DarkSalmon",
	        "DarkTurquoise",
	        "DeepPink",
	        "DeepSkyBlue",
	        "Gainsboro",
	        "Goldenrod",
	        "GreenYellow",
	        "HotPink",
	        "Ivory",
	        "Lavender",
	        "LawnGreen",
	        "LightBlue",
	        "LightGoldenrodYellow",
	        "LightSalmon",
	        "LightSteelBlue",
	        "Linen",
	        "MediumPurple",
	        "MediumSpringGreen",
	        "MintCream",
	        "MistyRose",
	        "Moccasin",
	        "Olive",
	        "Orange",
	        "OrangeRed",
	        "PaleGreen",
	        "PaleTurquoise",
	        "PaleVioletRed",
	        "PeachPuff",
	        "Peru",
	        "Pink",
	        "Red",
	        "RoyalBlue",
	        "Salmon",
	        "SeaShell",
	        "Silver",
	        "SkyBlue",
	        "SlateBlue",
	        "Tomato",
	        "Turquoise",
	        "Violet",
	        "Wheat",
	        "WhiteSmoke",
	        "Yellow",
	        "YellowGreen"
        };
        #endregion

        #region Data members
        private Queue<Color> iSharedChunkColours = new Queue<Color>();
        private Dictionary<long, Color> iSharedChunkColourDictionary = new Dictionary<long, Color>();
        #endregion
    }
}
