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
using System.Collections.Generic;
using SymbianUtils;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;
using HeapLib.Relationships;

namespace HeapLib
{
	public class HeapToHTMLPageHeapLinkedCells : AsyncHTMLFileWriter
	{
		#region Constructors & destructor
		public HeapToHTMLPageHeapLinkedCells( HeapReconstructor aReconstructor, string aFileName, HeapCell aCell )
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
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 9pt;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteLine( "a:link { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:visited { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:active { text-decoration: none; color:  #CC0000 }" );
				WriteLine( "a:hover { text-decoration: underline; color:  #CC0066 }" );

				WriteStyleName( ".tableBorder" );
				WriteStyleBodyBegin();
				WriteStyleBody(	"border-width: 1px;" );
				WriteStyleBody( "border-style: solid;" );
				WriteStyleBody(	"border-color: #000000;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".tableRowTitle" );
				WriteStyleBodyBegin();
				WriteStyleBody( "background-color: #DDDDDD;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".tableRowTotal" );
				WriteStyleBodyBegin();
				WriteStyleBody( "background-color: #EEEEEE;" );
				WriteStyleBody(	"border-top: #000000 1px solid;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".title" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 10pt;" );
				WriteStyleBody( "color: #0000AA; font-weight: bold;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".noitems" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 8pt;" );
				WriteStyleBody( "color: #0000AA;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".tableHeaders" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 12pt;" );
				WriteStyleBody( "color: #000000; font-weight: bold;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBody(	"border-bottom: #000000 1px solid;" );
				WriteStyleBodyEnd();

			WriteStyleEnd();

            WriteLine("<SCRIPT SRC=\"../../" + HeapToHTMLPageJavaScriptManager.JavaScriptHelperFileName + "\"></SCRIPT>");

			WriteHeadEnd();
		#endregion

			WriteBodyBegin();

			WriteDivisionBegin();

			WriteLine( "<P id=\"PageTop\" class=\"title\">Cell Associations</P>" );

            if ( iCell.RelationshipManager.EmbeddedReferencesTo.Count > 0 )
			{
				Writer.WriteLine( "<TABLE class=\"tableBorder\" width=\"100%\" cellspacing=\"0\" cellpadding=\"4\">" );
				Writer.WriteLine( "<TR class=\"tableRowTitle\">" );

				WriteTableColumn( "Address", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "Length", TAlignment.EAlignCenter, "tableHeaders" );
				WriteTableColumn( "Symbol", TAlignment.EAlignCenter, "tableHeaders" );

				WriteTableRowEnd();

				#region Blank row
				WriteTableRowBegin();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableRowEnd();
				#endregion
			}
			else
			{
				WriteLine( "<P class=\"noitems\">No linked cells</P>" );
			}
		}

		private void OnStateBody()
		{
            RelationshipCollection linkedCells = iCell.RelationshipManager.EmbeddedReferencesTo;
            if ( linkedCells.Count > 0 )
			{
                foreach( RelationshipInfo info in linkedCells )
                {
                    HeapCell linkedCell = info.ToCell;

					// Start new row
					WriteTableRowBegin();

					// Address
					WriteTableColumnHexAddress( linkedCell.Address, TAlignment.EAlignLeft );

					// Length
					WriteTableColumn( linkedCell.PayloadLength, TAlignment.EAlignRight );

					// Symbol
					string linkText = "[Unknown linked cell]";
					if	( linkedCell.Symbol != null )
					{
						linkText = linkedCell.Symbol.NameWithoutVTablePrefix;
					}

					string url = "javascript:showMainFormCell(\'" + linkedCell.Address.ToString("x8") + "\')";
					string windowTarget = "MainWindow";
					WriteTableColumnBegin( TAlignment.EAlignLeft, string.Empty );
					WriteAnchorWithTarget( windowTarget, url, linkText );
					WriteTableColumnEnd();

                    WriteTableRowEnd();
				}

				#region Blank row
				WriteTableRowBegin();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableColumnEmpty();
				WriteTableRowEnd();
				#endregion

				#region Total
				WriteTableRowBegin();
				WriteTableColumn( "Total:", "tableRowTotal" );
				WriteTableColumn( ( iCell.PayloadLengthIncludingLinkedCells - iCell.PayloadLength ).ToString( "" ), TAlignment.EAlignRight, "tableRowTotal" );
				WriteTableColumn( "&nbsp;", "tableRowTotal" );
				WriteTableRowEnd();
				#endregion
			}
		}

		private void OnStateFooter()
		{
            if ( iCell.RelationshipManager.EmbeddedReferencesTo.Count > 0 )
            {
                WriteTableEnd();
			}

			WriteDivisionEnd();
			WriteBodyEnd();
			WriteDocumentEnd();
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
