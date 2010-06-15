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
using MemAnalysisLib.Parser.Base;
using MemAnalysisLib.Parser.Prefixes;
using MemAnalysisLib.Parser.Options;
using MemAnalysisLib.MemoryOperations.Operations;
using MemAnalysisLib.MemoryOperations.Functions;
using SymbolLib.Generics;
using SymbolLib.Engines;
using SymbianUtils;

namespace MemAnalysisLib.Parser.Implementations
{
	public class MemAnalysisRegionalParser : MemAnalysisParserBase
	{
		#region Constructors & destructor
		public MemAnalysisRegionalParser( MemAnalysisParserOptions aOptions, SymbolManager aSymbolManager )
			: base( aOptions, aSymbolManager )
		{
		}
		#endregion

		#region Properties
		public MemObjRegionalData Data
		{
			get { return iData; }
		}
		#endregion

		#region From AsyncTextReader - Parsing related
		protected override void HandleFilteredLine( string aLine )
		{
			if	( aLine != null )
			{
				/*
				if	( LineNumber == 45244 )
				{
					int x = 0;
					x++;
				}
				*/

				// Check for a region match
				if ( aLine.Length >= Options.MarkerTextEnd.Length && aLine.IndexOf( Options.MarkerTextEnd ) >= 0 )
				{
					int pos = aLine.IndexOf( Options.MarkerTextEnd );
					string marker = aLine.Substring( pos );
					//
					if	( Options.MarkerTextToBeStripped )
					{
						marker = aLine.Substring( pos + Options.MarkerTextEnd.Length );
					}
					//
					iData.MarkerEndIdentified( marker, LineNumber );
				}
				else if	( aLine.Length >= Options.MarkerTextStart.Length && aLine.IndexOf( Options.MarkerTextStart ) >= 0 )
				{
					int pos = aLine.IndexOf( Options.MarkerTextStart );
					string marker = aLine.Substring( pos );
					//
					if	( Options.MarkerTextToBeStripped )
					{
						marker = aLine.Substring( pos + Options.MarkerTextStart.Length );
					}
					//
					iData.MarkerStartIdentified( marker, LineNumber );
				}
				else
				{
					base.HandleFilteredLine( aLine );
				}
			}
		}

		protected override void HandleReadCompleted()
		{
			try
			{
				iData.AllItemsLocated( LineNumber );
			}
			finally
			{
				base.HandleReadCompleted();
			}
		}
		#endregion

		#region From MemAnalysisParserBase
        protected override void HandleOpAllocation( MemOpAllocation aObject )
		{
			iData.Add( aObject, Options.DiscardMatchingAllocsAndFrees );
		}

        protected override void HandleOpFree( MemOpFree aObject )
		{
			iData.Add( aObject, Options.DiscardMatchingAllocsAndFrees );
		}

        protected override void HandleOpReallocation( MemOpReallocation aObject )
		{
			iData.Add( aObject );
		}
		#endregion

		#region Data members
		private MemObjRegionalData iData = new MemObjRegionalData();
		#endregion
	}
}
