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
using SymbianUtils;
using SymbianUtils.RawItems;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLPageEntireListing : AsyncHTMLFileWriter
	{
		#region Enumerations
		public enum TSortType
		{
			ESortTypeByAddress = 0,
			ESortTypeByType,
			ESortTypeByLength
		}
		#endregion

		#region Constructors & destructor
		public HeapToHTMLPageEntireListing( HeapReconstructor aReconstructor, TSortType aSortType, string aFileName )
			:	base( aFileName )
		{
            iSortType = aSortType;
            iReconstructor = aReconstructor;

            // Make a new (but empty) array
            iEntries = new HeapCellArray( aReconstructor.Data.Count + 1 );
			
            // Depending on the sort type, trigger the sort method
            // in the array. Since the array is empty this won't
            // do a lot, except set up the array to use our chosen
            // comparison object...
			switch( iSortType )
			{
			default:
			case TSortType.ESortTypeByAddress:
                iEntries.SortByAddress();
				break;
			case TSortType.ESortTypeByType:
                iEntries.SortByType();
				break;
			case TSortType.ESortTypeByLength:
                iEntries.SortByLength();
				break;
			}
            
            // Now copy the entries into the array
            iEntries.Copy( aReconstructor.Data );
        }
		#endregion
		
		#region API
		public static string PageFileName( TSortType aSortType )
		{
			string ret = "EntireHeapListing_";
			//
			switch( aSortType )
			{
			default:
			case TSortType.ESortTypeByAddress:
				ret += "ByAddress";
				break;
			case TSortType.ESortTypeByType:
				ret += "ByType";
				break;
			case TSortType.ESortTypeByLength:
				ret += "ByLength";
				break;
			}
			//
			ret += ".html";
			//
			return ret;
		}

		public static string HeapCellLinkIdentifierName( HeapCell aCell )
		{
			string ret = "Cell_" + aCell.Address.ToString("x8");
			return ret;
		}
		#endregion

		#region Internal state related
		private enum TState
		{
			EStateHeader = 0,
			EStateBody,
			EStateFooter,
			EStateDone
		}

		private TState State
		{
			get { return iState; }
			set { iState = value; }
		}

		private void OnStateHeader()
		{
			WriteDocumentBegin();
			WriteHeadBegin();
			WriteTitle( "Heap Information" );

			#region Styles
			WriteStyleBegin();
				WriteStyleName( "td, tr, p, li" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 10pt;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();

				WriteLine( "a:link { text-decoration: none; color:  #0000CC }" );
				WriteLine( "a:visited { text-decoration: none; color:  #0000CC }" );
				WriteLine( "a:active { text-decoration: none; color:  #0000CC }" );
				WriteLine( "a:hover { text-decoration: underline; color:  #0000CC }" );

				WriteStyleName( ".rawData" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: courier, courier new, monospace;" );
				WriteStyleBody( "font-size: 10pt;" );
				WriteStyleBody( "color: #000099;" );
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

				WriteStyleName( ".tableHeaderSelected" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif;" );
				WriteStyleBody( "font-size: 12pt;" );
				WriteStyleBody( "color: #FF0000; font-weight: bold;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBody(	"border-bottom: #000000 1px solid;" );
				WriteStyleBodyEnd();

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

				WriteStyleName( ".tableRowBody1" );
				WriteStyleBodyBegin();
                WriteStyleBody( "background-color: #FFFFFF;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".tableRowFreeCell" );
				WriteStyleBodyBegin();
                WriteStyleBody( "background-color: #C5F5F1;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".tableRowBody2" );
				WriteStyleBodyBegin();
                WriteStyleBody( "background-color: #EEEEEE;" );
				WriteStyleBodyEnd();

				WriteStyleName( ".freeCell" );
				WriteStyleBodyBegin();
				WriteStyleBody( "font-family: courier, courier new, monospace;" );
				WriteStyleBody( "font-size: 10pt;" );
				WriteStyleBody( "color: #009900;" );
				WriteStyleBody( "white-space: nowrap;" );
				WriteStyleBodyEnd();
			WriteStyleEnd();
			#endregion

			WriteLine( "<SCRIPT SRC=\"HeapData/" + HeapToHTMLPageJavaScriptManager.JavaScriptHelperFileName + "\"></SCRIPT>" );

			WriteHeadEnd();

			WriteBodyBegin();

			WriteDivisionBegin( AsyncHTMLFileWriter.TAlignment.EAlignCenter );
			
			Writer.WriteLine( "<TABLE width=\"65%\" cellspacing=\"3\" cellpadding=\"3\">" );
			WriteTableRowBegin();
			WriteTableColumnBegin();

				Writer.WriteLine( "<TABLE class=\"tableBorder\" width=\"100%\" cellspacing=\"0\" cellpadding=\"4\">" );

				// Set up table columns. If we've got a debug version of EUSER then
				// we can show the allocation numbers.
				Writer.WriteLine( "<TR class=\"tableRowTitle\">" );

					MakeColumnHeading( "Address", TAlignment.EAlignLeft, TSortType.ESortTypeByAddress );
					MakeColumnHeading( "Type", TAlignment.EAlignCenter, TSortType.ESortTypeByType );

					if	( iReconstructor.SourceData.MetaData.Heap.DebugAllocator )
					{
						WriteTableColumn( "Alloc #", TAlignment.EAlignCenter, "tableHeaders" );
					}
					MakeColumnHeading( "Length", TAlignment.EAlignCenter, TSortType.ESortTypeByLength );

					WriteTableColumn( "&nbsp", "tableHeaders" );
					WriteTableColumn( "Symbol", TAlignment.EAlignLeft, "tableHeaders" );
					WriteTableColumn( "Object Name", TAlignment.EAlignLeft, "tableHeaders" );

				WriteTableRowEnd();

				#region Blank row
				WriteTableRowBegin();
					WriteTableColumnEmpty();
					WriteTableColumnEmpty();
                    if ( iReconstructor.SourceData.MetaData.Heap.DebugAllocator )
					{
						WriteTableColumnEmpty();
					}
					WriteTableColumnEmpty();
					WriteTableColumnEmpty();
					WriteTableColumnEmpty();
					WriteTableColumnEmpty();
				WriteTableRowEnd();
				#endregion
		}

		private void OnStateBody()
		{
			HeapCell entry = iEntries[ iCurrentHeapCellIndex++ ];
			string link;
			string body = string.Empty;

			string style = TableRowBodyStyleName;
			if	( entry.Type == HeapCell.TType.EFree )
			{
				style = "tableRowFreeCell";
			}
			Writer.WriteLine( "<TR class=\"" + style + "\">" );

			// Address
			WriteTableColumnBegin();
			WriteDivisionBeginWithId( HeapCellLinkIdentifierName( entry ) );
			string cellAddress = "0x" + entry.Address.ToString("x8");
			MakeAddressLink( entry );
			WriteDivisionEnd();
			WriteTableColumnEnd();
		
			// Type
			string typeProperName = string.Empty;
			if	( entry.Type == HeapCell.TType.EAllocated )
			{
				typeProperName = "Allocated";
				body = CreateCellContentsQuickView( entry );
			}
			else if ( entry.Type == HeapCell.TType.EFree )
			{
				typeProperName = "Free";
				body = CreateCellContentsQuickView( entry );
			}

			link = HeapToHTMLPageJavaScriptManager.MakeToolTipDiv( typeProperName + " Cell", body, typeProperName, KHeapDataPreviewLength );
			WriteTableColumnBegin( TAlignment.EAlignCenter, string.Empty );
			WriteLine( link );
			WriteTableColumnEnd();

			// Alloc number
            if ( iReconstructor.SourceData.MetaData.Heap.DebugAllocator )
			{
				link = HeapToHTMLPageJavaScriptManager.MakeToolTipDiv( "Allocation Number", "Number " + entry.AllocationNumber.ToString() + " of " + iEntries.Count.ToString() + " cells.", entry.AllocationNumber.ToString() );
				
				WriteTableColumnBegin( TAlignment.EAlignRight, string.Empty );
				WriteLine( link );
				WriteTableColumnEnd();
			}

			// Cell length
			body = string.Empty;
			body += "Type:    " + typeProperName + "<BR>";
			body += "Header:  " + entry.HeaderSize.ToString() + " bytes<BR>";
			body += "Payload: " + entry.PayloadLength.ToString() + " bytes<BR><BR>";
			if	( entry.Type == HeapCell.TType.EFree )
			{
                string instanceSizeAsPercentageOfTotal = iReconstructor.Statistics.CellLengthAsHeapPercentage( entry ).ToString("##0.000");
				body += "This individual cell corresponds to " + instanceSizeAsPercentageOfTotal + " of the total free cells in this heap.";
			}
            link = HeapToHTMLPageJavaScriptManager.MakeToolTipDiv( "Cell Length", body, entry.Length.ToString() );
			WriteTableColumnBegin( TAlignment.EAlignRight, string.Empty );
			WriteLine( link );
			WriteTableColumnEnd();

			// Spacer
			WriteTableColumnSpace();

			// Symbol
			if	( entry.Symbol != null )
			{
                // Get symbol tracking info.
                TrackingInfo trackingInfo = iReconstructor.Statistics.StatsAllocated.TrackerSymbols[ entry.Symbol ];
				System.Diagnostics.Debug.Assert( trackingInfo != null );
                string instanceSizeAsPercentageOfTotal = iReconstructor.Statistics.CellLengthAsHeapPercentage( entry ).ToString("##0.000") + "%";
                string totalSymbolicSizeAsPercentageOfTotal = NumberFormattingUtils.NumberAsPercentageThreeDP( trackingInfo.AssociatedMemory, iReconstructor.Statistics.SizeTotal ) + "%";

				body = string.Empty;
				if	( trackingInfo.Count > 1 )
				{
					body += "There are " + trackingInfo.Count.ToString() + " other instances of this symbol ";
				}
				else
				{
					body += "This is the only instance of this symbol ";
				}
				body += "and each instance is using " + trackingInfo.PayloadLength.ToString() + " bytes of memory (plus cell header overhead).<BR><BR>";
				body += "Of the total allocated heap memory, cells associated with this symbolic are using a total of " + trackingInfo.AssociatedMemory.ToString() + " bytes (" + totalSymbolicSizeAsPercentageOfTotal + " of total heap) memory.<BR><BR>";
				body += "This individual cell corresponds to " + instanceSizeAsPercentageOfTotal + " of the total " + typeProperName + " heap space.<BR>";

                link = HeapToHTMLPageJavaScriptManager.MakeToolTipDiv( "Symbol Information", body, entry.Symbol.NameWithoutVTablePrefix );
				WriteTableColumnBegin( TAlignment.EAlignLeft, string.Empty );
				WriteLine( link );
				WriteTableColumnEnd();
			}
			else
			{
				WriteTableColumnEmpty();
			}

			// Object name	
			if	( entry.Symbol != null )
			{
				WriteTableColumn( entry.Symbol.ObjectWithoutSection, TAlignment.EAlignLeft ); 
			}
			else
			{
				WriteTableColumnEmpty();
			}

			WriteTableRowEnd();
		}

		private void OnStateFooter()
		{
			WriteTableEnd();

			WriteTableColumnEnd();
			WriteTableRowEnd();
			WriteTableEnd();
			WriteDivisionEnd();

			WriteBodyEnd();

			WriteDocumentEnd();
		}
		#endregion

		#region From AsyncTextWriterBase
		protected override bool ContinueProcessing()
		{
			return ( State != TState.EStateDone );
		}

        public override long Size
		{
			get
			{
				return (long) iEntries.Count + 2;
			}
		}

        public override long Position
		{
			get
			{
				long pos = 0;
				//
				switch( State )
				{
				case TState.EStateHeader:
					pos = 1;
					break;
				case TState.EStateBody:
					pos = (long) (1 + iCurrentHeapCellIndex);
					break;
				case TState.EStateFooter:
					pos = (long) (2 + iCurrentHeapCellIndex);
					break;
				default:
					pos = (long) iEntries.Count + 2;
					break;
				}
				//
				return pos;
			}
		}

        public override void ExportData()
		{
			switch( State )
			{
			case TState.EStateHeader:
				OnStateHeader();
				State = TState.EStateBody;
				break;
			case TState.EStateBody:
				OnStateBody();
				if	( iCurrentHeapCellIndex >= iEntries.Count )
				{
					State = TState.EStateFooter;
				}
				break;
			case TState.EStateFooter:
				OnStateFooter();
				State = TState.EStateDone;
				break;
			}

		}
		#endregion

		#region Internal methods
		private string CreateCellContentsQuickView( HeapCell aCell )
		{
			const int KDWordsPerLine = 4;

			StringBuilder ret = new StringBuilder();
			//
			ret.Append( "<table>" );

			int rawItemsCount = Math.Min( KDWordsPerLine * 2, aCell.RawItems.Count );
			for( int i=0; i<rawItemsCount; i += KDWordsPerLine )
			{
				ret.Append( "<tr>" );

				// Work out how many items on this line
				int runIndex;
				int runExtent = i + KDWordsPerLine;
				RawItem rawItem = null;
				
				// First set of columns - original data
				for( runIndex = i; runIndex < runExtent; runIndex++ )
				{
					// Get the item
					if	( runIndex < rawItemsCount )
					{
						rawItem = aCell[ runIndex ];
						ret.Append( "<td align=center>" + rawItem.OriginalData.ToString("x8") + "</td>" );
					}
					else
					{
						ret.Append( "<td></td>" );
						rawItem = null;
					}
				}
				
				ret.Append( "<td>&nbsp;</td>" );

				// Second set of columns - characterised data
				for( runIndex = i; runIndex < runExtent; runIndex++ )
				{
					// Get the item
					if	( runIndex < rawItemsCount )
					{
						rawItem = aCell[ runIndex ];
						ret.Append( "<td align=center>" + HTMLEntityUtility.Entitize( rawItem.OriginalCharacterisedData ) + "</td>" );
					}
					else
					{
						ret.Append( "<td></td>" );
						rawItem = null;
					}
				}
				
				ret.Append( "</tr>" );
			}
			ret.Append( "</table>" );
			//
			return ret.ToString();
		}

		private string TableRowBodyStyleName
		{
			get
			{
				string ret = "tableRowBody";
				ret += iTableBodyRowStyleAlternator.ToString("d1");
				//
				if	( iTableBodyRowStyleAlternator == 2 )
				{
					iTableBodyRowStyleAlternator = 1;
				}
				else
				{
					iTableBodyRowStyleAlternator = 2;
				}
				//
				return ret;
			}
		}

		private void MakeColumnHeading( string aTitle, TAlignment aAlignment, TSortType aSortType )
		{
			if	( iSortType != aSortType )
			{
				string toolTipBody = "Sort by ";
				//
				switch( aSortType )
				{
				default:
				case TSortType.ESortTypeByAddress:
					toolTipBody += "address";
					break;
				case TSortType.ESortTypeByType:
					toolTipBody += "type";
					break;
				case TSortType.ESortTypeByLength:
					toolTipBody += "length";
					break;
				}
				//
				string url = PageFileName( aSortType );
				string linkWithToolTip = HeapToHTMLPageJavaScriptManager.MakeToolTipLink( "Sorting", toolTipBody, url, string.Empty, aTitle );
				//
				WriteTableColumnBegin( aAlignment, "tableHeaders" );
				WriteLine( linkWithToolTip );
				WriteTableColumnEnd();
			}
			else
			{
				WriteTableColumn( aTitle, aAlignment, "tableHeaderSelected" );
			}
		}

		private void MakeAddressLink( HeapCell aCell )
		{
            string fileName = HeapToHTMLPageHeapCellManager.HeapCellFileName( aCell );
            string subDir = HeapToHTMLPageHeapCellManager.HeapCellDirectory( aCell, string.Empty, '/' );

            string heapCellDataPageUrl = "./HeapData" + subDir + "/" + fileName + ".html";
            string heapCellLinkedCellPageUrl = "./HeapLinkInfo" + subDir + "/" + fileName + ".html";

			StringBuilder line = new StringBuilder();
			line.Append( "<A " );
			line.Append( "TARGET=\"HeapCellData\" " );
			line.Append( "HREF=\"" );
			line.Append( heapCellDataPageUrl );
			line.Append( "\" " );
			line.Append( "onClick=\"parent.HeapLinkInfo.location=\'" + heapCellLinkedCellPageUrl + "\'\"" );
			line.Append( ">" );
            line.Append( "0x" + fileName );
			line.Append( "</A>" );
			Writer.Write( line.ToString() );
		}
    	#endregion

		#region Internal constants
		private const int KHeapDataPreviewLength = 500;
		#endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		private readonly HeapCellArray iEntries;
		private readonly TSortType iSortType;
		private int iCurrentHeapCellIndex = 0;
		private TState iState;
		private int iTableBodyRowStyleAlternator = 1;
		#endregion
	}
}
