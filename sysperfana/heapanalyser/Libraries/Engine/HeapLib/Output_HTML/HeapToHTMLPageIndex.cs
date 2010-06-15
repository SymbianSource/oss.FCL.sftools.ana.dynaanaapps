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
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLPageIndex : AsyncHTMLFileWriter
	{
		#region Constructors & destructor
		public HeapToHTMLPageIndex( HeapReconstructor aReconstructor, string aFileName )
			:	base( aFileName )
		{
			iReconstructor = aReconstructor;
		}
		#endregion
		
		#region API
		#endregion

		#region From AsyncTextWriterBase
        public override long Size
		{
			get
			{
				// So small that we write the entire file in one go
				return 1;
			}
		}

        public override long Position
		{
			get
			{
				// We're immediately finished once we've written the data
				// once
				return iPosition;
			}
		}

        public override void ExportData()
		{
			WriteDocumentBegin();
				WriteHeadBegin();
					WriteTitle( "Heap Viewer" );
					WriteStyleBegin();
						WriteStyleName( "a:link" );
							WriteStyleBodyBegin();
							WriteStyleBody( "font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10pt; color: #000000; text-decoration: none;" );
							WriteStyleBodyEnd();
					WriteStyleEnd();
				WriteHeadEnd();

				Writer.WriteLine( "<FRAMESET ROWS=\"66%,33%\">" );
				Writer.WriteLine( "<FRAME SRC=\"EntireHeapListing_ByAddress.html\" name=\"MainWindow\">" );
				Writer.WriteLine( "<FRAMESET COLS=\"60%,40%\">" );
				Writer.WriteLine( "<FRAME SCROLLING=\"auto\" name=\"HeapCellData\">" );
				Writer.WriteLine( "<FRAME SCROLLING=\"auto\" name=\"HeapLinkInfo\">" );
				Writer.WriteLine( "</FRAMESET>" );
				Writer.WriteLine( "</FRAMESET>" );

			WriteDocumentEnd();

			iPosition = 1; // Finished
		}
		#endregion

		#region Internal methods
		#endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		private long iPosition = 0;
		#endregion
	}
}