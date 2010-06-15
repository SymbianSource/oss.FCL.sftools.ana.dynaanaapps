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
using System.Collections;
using System.Collections.Generic;
using MemAnalysisLib.Parser.Prefixes;

namespace MemAnalysisLib.Parser.Options
{
	public class MemAnalysisParserOptions
	{
		#region Constructors & destructor
		public MemAnalysisParserOptions( MemAnalysisParserPrefixesBase aPrefixes )
		{
            iPrefixes = aPrefixes;
		}
		#endregion

		#region Properties
		public bool DiscardMatchingAllocsAndFrees
		{
			get { return iDiscardMatchingAllocsAndFrees; }
			set { iDiscardMatchingAllocsAndFrees = value; }
		}

		public string TracePrefix
		{
			get { return iTracePrefix; }
			set { iTracePrefix = value; }
		}

		public string SpecificThreadFilter
		{
			get { return iSpecificThreadFilter; }
			set { iSpecificThreadFilter = value; }
		}

		public string SourceFileName
		{
			get { return iSourceFileName; }
			set { iSourceFileName = value; }
		}

		public MemAnalysisParserPrefixesBase Prefixes
		{
			get { return iPrefixes; }
			set { iPrefixes = value; }
		}

		public string MarkerTextStart
		{
			get { return iMarkerTextStart; }
			set { iMarkerTextStart = value; }
		}

		public string MarkerTextEnd
		{
			get { return iMarkerTextEnd; }
			set { iMarkerTextEnd = value; }
		}

		public bool MarkerTextToBeStripped
		{
			get { return iMarkerTextToBeStripped; }
			set { iMarkerTextToBeStripped = value; }
		}

		public bool MarkerOperationsOutsideRegionToBeIgnored
		{
			get { return iMarkerOperationsOutsideRegionToBeIgnored; }
			set { iMarkerOperationsOutsideRegionToBeIgnored = value; }
		}
		#endregion

		#region Data members
		private bool iDiscardMatchingAllocsAndFrees = true;
		private bool iMarkerTextToBeStripped = true;
		private bool iMarkerOperationsOutsideRegionToBeIgnored = true;
		private string iTracePrefix = string.Empty;
		private string iSpecificThreadFilter = string.Empty;
		private string iSourceFileName = string.Empty;
		private string iMarkerTextStart = string.Empty;
		private string iMarkerTextEnd = string.Empty;
		private MemAnalysisParserPrefixesBase iPrefixes = null;
		#endregion
	}
}
