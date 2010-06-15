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
using HeapLib.Array;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLPageJavaScriptManager : AsyncTextWriterBase
	{
		#region Constructors & destructor
		public HeapToHTMLPageJavaScriptManager( HeapReconstructor aReconstructor, string aBasePath )
		{
			iReconstructor = aReconstructor;
			iEntries = iReconstructor.Data;
			iBasePath = aBasePath;
		}
		#endregion
		
		#region API
		public static string JavaScriptHelperFileName
		{
			get { return "HeapDataJavaScriptLib.js"; }
		}

		public static string MakeToolTipLink( string aToolTipTitle, string aToolTipBody, string aUrl, string aWindowTarget, string aLinkText )
		{
			string javaScript = JavaScript( aToolTipTitle, aToolTipBody );
			
			StringBuilder ret = new StringBuilder();
			//
			ret.Append( "<a href=\"" + aUrl + "\" " );
			if	( aWindowTarget != string.Empty )
			{
				ret.Append( "target=\"" + aWindowTarget + "\" " );
			}
			ret.Append( javaScript );
			ret.Append( ">" );
			ret.Append( aLinkText );
			ret.Append( "</a>" );
			//
			return ret.ToString();
		}

		public static string MakeToolTipDiv( string aToolTipTitle, string aToolTipBody, string aDivText )
		{
			return MakeToolTipDiv( aToolTipTitle, aToolTipBody, aDivText, -1 );
		}

		public static string MakeToolTipDiv( string aToolTipTitle, string aToolTipBody, string aDivText, int aWidth )
		{
			string javaScript = JavaScript( aToolTipTitle, aToolTipBody, aWidth);
			
			StringBuilder ret = new StringBuilder();
			//
			ret.Append( "<div " + javaScript + ">" );
			ret.Append( aDivText );
			ret.Append( "</div>" );
			//
			return ret.ToString();
		}
		#endregion

		#region Internal methods
		private static string JavaScript( string aTooltipTitle, string aToolTipBody )
		{
			return JavaScript( aTooltipTitle, aToolTipBody, -1 );
		}

		private static string JavaScript( string aTooltipTitle, string aToolTipBody, int aWidth )
		{
			string onMouseOver = " onmouseover=\"showInfo(\'" + aTooltipTitle + "\', \'" + aToolTipBody + "\'";
			if	( aWidth > 0 )
			{
				onMouseOver += ", \'" + aWidth.ToString() + "\'";
			}
			onMouseOver += ")\"";
			//
			string onMouseOut = " onmouseout=\"hideInfo()\"";
			string onFocus = " onfocus=\"this.blur()\"";
			string javaScript = onMouseOver + onMouseOut + onFocus;
			//
			return javaScript;
		}
		#endregion

		#region From AsyncTextWriterBase
        public override long Size
		{
			get
			{
				return 2;
			}
		}

        public override long Position
		{
			get
			{
				return iPosition;
			}
		}

        public override void ExportData()
		{
			string javaScriptLibSourceFileName = Path.Combine( System.Windows.Forms.Application.StartupPath, JavaScriptHelperFileName );
			if	( File.Exists( javaScriptLibSourceFileName ) == false )
			{
				throw new FileNotFoundException( "Cannot locate Heap Data java script library", javaScriptLibSourceFileName );
			}

			string javaScriptLibFileNameHeapData = Path.Combine( HeapToHTMLConverter.PageDirectoryNameEnsuringPathExists( iBasePath, "HeapData" ), JavaScriptHelperFileName );
			File.Copy( javaScriptLibSourceFileName, javaScriptLibFileNameHeapData, true );
			iPosition++;

			string javaScriptLibFileNameHeapLinkInfo = Path.Combine( HeapToHTMLConverter.PageDirectoryNameEnsuringPathExists( iBasePath, "HeapLinkInfo" ), JavaScriptHelperFileName );
			File.Copy( javaScriptLibSourceFileName, javaScriptLibFileNameHeapLinkInfo, true );
			iPosition++;
		}
		#endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		private readonly HeapCellArray iEntries;
		private readonly string iBasePath;
		private long iPosition = 0;
		#endregion
	}
}
