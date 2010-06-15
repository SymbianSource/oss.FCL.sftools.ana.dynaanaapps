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
using System.IO;
using System.Text;
using System.Collections;
using System.Drawing;
using SymbianUtils;
using SymbianUtils.RawItems;
using HeapLib.Cells;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLPageHeapData : AsyncHTMLFileWriter
	{
		#region Constructors & destructor
		public HeapToHTMLPageHeapData( HeapReconstructor aReconstructor, string aFileName, HeapCell aCell )
			: base( aFileName, TNotUsingThread.ENotUsingThread )
		{
			iReconstructor = aReconstructor;
			iCell = aCell;
		}
		#endregion
		
		#region API
		#endregion

		#region Internal state related
		private void OnStateHeader()
		{
			#region Header
			WriteDocumentBegin();
			WriteHeadBegin();
			WriteTitle( "Heap Cell Contents - 0x" + iCell.Address.ToString("x8") );
			WriteStyleBegin();

				WriteStyleName( "td, tr, li" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: courier, courier new, monospace;" );
				WriteStyleBody( "font-size: 9pt;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteLine( "a:link { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:visited { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:active { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:hover { text-decoration: underline; color:  #CC0066 }" );

				WriteStyleName( "p" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 10pt;" );
				WriteStyleBody( "color: #0000AA; font-weight: bold;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".cellDataAsNumeric" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: courier, courier new, monospace;" );
				WriteStyleBody( "font-size: 9pt;" );
				WriteStyleBody( "color: #000099;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".cellDataAsString" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: courier, courier new, monospace;" );
				WriteStyleBody( "font-size: 11pt;" );
				WriteStyleBody( "color: #009900;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();
			WriteStyleEnd();

            WriteLine("<SCRIPT SRC=\"../../" + HeapToHTMLPageJavaScriptManager.JavaScriptHelperFileName + "\"></SCRIPT>");

			WriteHeadEnd();
		#endregion

			WriteBodyBegin();

			WriteDivisionBegin();

			WriteLine( "<P id=\"PageTop\">Heap Data Viewer</P>" );

			WriteTableBegin();
			WriteTableRowBegin();

				// Address
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );

				// Raw byte values	
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				
				// Characterised data
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "", TAlignment.EAlignCenter, "tableHeaders" );

			WriteTableRowEnd();

			#region Blank row
			WriteTableRowBegin();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
			WriteTableRowEnd();
			#endregion
		}

		private void OnStateBody()
		{
			const int KDWordsPerLine = 4;

			int rawItemsCount = iCell.RawItems.Count;
			for( int i=0; i<rawItemsCount; i += KDWordsPerLine )
			{
                RawItem rawItem = iCell[ i ];

				// Start new row
				WriteTableRowBegin();

				// Address
				WriteTableColumnBegin();
				WriteAnchorWithName( rawItem.Address.ToString("x8") );
				Writer.Write( "0x" + rawItem.Address.ToString("x8") );
				WriteTableColumnEnd();

				// Blank spacer
				WriteTableColumnBegin( TAlignment.EAlignNone, string.Empty, 20 );
				WriteSpace();
				WriteTableColumnEnd();

				// Work out how many items on this line
				int runIndex;
				int runExtent = i + KDWordsPerLine;

				// First set of columns - original data
				for( runIndex = i; runIndex < runExtent; runIndex++ )
				{
					// Get the item
					if	( runIndex < rawItemsCount )
					{
						rawItem = iCell[ runIndex ];

						// Does the raw item have a linked cell associated with it?
						if	( rawItem.Tag != null && rawItem.Tag is Relationships.RelationshipInfo )
						{
                            Relationships.RelationshipInfo relationshipInfo = (Relationships.RelationshipInfo) rawItem.Tag;
                            HeapCell associatedCell = relationshipInfo.ToCell;

							string url = "javascript:showMainFormCell(\'" + associatedCell.Address.ToString("x8") + "\')";
							string windowTarget = "MainWindow";
							string toolTipTitle = "Link to ";
							if	( associatedCell.Symbol != null )
							{
								toolTipTitle += associatedCell.Symbol.NameWithoutVTablePrefix;
							}
							else
							{
								toolTipTitle += "Cell";
							}
							string toolTipBody = ToolTipBody( associatedCell );
							string linkText = rawItem.OriginalData.ToString("x8");
							string linkWithToolTip = HeapToHTMLPageJavaScriptManager.MakeToolTipLink( toolTipTitle, toolTipBody, url, windowTarget, linkText );

							WriteTableColumnBegin( TAlignment.EAlignCenter, "cellDataAsNumeric" );
							WriteLine( linkWithToolTip );
							WriteTableColumnEnd();
						}
						else
						{
							WriteTableColumnHex( rawItem.OriginalData, TAlignment.EAlignCenter, "cellDataAsNumeric" );
						}
					}
					else
					{
						WriteTableColumnEmpty();
						rawItem = null;
					}
				}

				// Blank spacer
				WriteTableColumnBegin( TAlignment.EAlignNone, string.Empty, 20 );
				WriteSpace();
				WriteTableColumnEnd();

				// Second set of columns - characterised data
				for( runIndex = i; runIndex < runExtent; runIndex++ )
				{
					// Get the item
					if	( runIndex < rawItemsCount )
					{
						rawItem = iCell[ runIndex ];
						WriteTableColumn( rawItem.OriginalCharacterisedData, TAlignment.EAlignCenter, "cellDataAsString" );
					}
					else
					{
						WriteTableColumnEmpty();
						rawItem = null;
					}
				}
			}

			WriteTableRowEnd();
		}

		private void OnStateFooter()
		{
			WriteTableEnd();
			WriteDivisionEnd();

			WriteBodyEnd();

			WriteDocumentEnd();
		}
		#endregion

		#region Internal methods
		private static string LinkedHeapCellLayerName( HeapCell aCell )
		{
			return "LinkedHeapCell_" + aCell.Address.ToString("x8");
		}

		private static string ToolTipBody( HeapCell aCell )
		{
			StringBuilder ret = new StringBuilder();
			//
			ret.Append( "There appears to be a link to the heap cell at address: 0x" + aCell.Address.ToString("x8") + "<BR><BR>" );
			if	( aCell.Symbol != null )
			{
				ret.Append( "The link cell has an associated symbol: " + aCell.Symbol.NameWithoutVTablePrefix );
			}
			//
			return ret.ToString();
		}
		#endregion

		#region From AsyncTextWriterBase
        public override long Size
		{
			get
			{
				return 1;
			}
		}

        public override long Position
		{
			get
			{
				return 0;
			}
		}

        public override void ExportData()
		{
			// NB this object is called synchronously, despite its name
			OnStateHeader();
			OnStateBody();
			OnStateFooter();
		}
		#endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		private readonly HeapCell iCell;
		#endregion
	}
}
