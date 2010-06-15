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
using MemAnalysisLib.Parser.Prefixes;
using MemAnalysisLib.Parser.Options;
using MemAnalysisLib.MemoryOperations.Operations;
using MemAnalysisLib.MemoryOperations.Functions;
using SymbolLib.Generics;
using SymbolLib.Engines;
using SymbianUtils;

namespace MemAnalysisLib.Parser.Base
{
	public abstract class MemAnalysisParserBase : AsyncTextFileReader
	{
		#region Constructors & destructor
		public MemAnalysisParserBase( MemAnalysisParserOptions aOptions, SymbolManager aSymbolManager )
			: base( aOptions.SourceFileName, new AsyncTextReaderPrefix( aOptions.TracePrefix ) )
		{
			iOptions = aOptions;
			iSymbolManager = aSymbolManager;
			//
			if	( aOptions.SpecificThreadFilter != string.Empty )
			{
				AddFilter( new SymbianUtils.AsyncTextReaderFilter( aOptions.SpecificThreadFilter ) );
			}
		}
		#endregion

        #region API
        public void Parse()
        {
            base.AsyncRead();
        }
        #endregion

        #region Properties
        public SymbolManager SymbolManager
		{
			get { return iSymbolManager; }
		}

		public MemAnalysisParserOptions Options
		{
			get { return iOptions; }
		}

		public MemAnalysisParserPrefixesBase Prefixes
		{
			get { return iOptions.Prefixes; }
		}
		#endregion

		#region New Framework Methods
		protected abstract void HandleOpAllocation( MemOpAllocation aObject );
		protected abstract void HandleOpFree( MemOpFree aObject );
		protected abstract void HandleOpReallocation( MemOpReallocation aObject );
		#endregion

		#region From AsyncTextReader - Parsing related
		protected override void HandleFilteredLine( string aLine )
		{
			if	( aLine != null )
			{
                MemOpBase operation = null;
                MemOpFnBase function = null;
                //
                bool handled = Prefixes.MatchesPrefix( ref aLine, out function );
                if ( handled && function != null )
                {
                    operation = function.Parse( ref aLine, Prefixes );
                    if ( operation != null )
                    {
                        // Finalise it
                        operation.LineNumber = System.Convert.ToUInt32( LineNumber );
                        operation.Finalise( SymbolManager );

                        // Notify
                        switch ( function.Class )
                        {
                        case MemAnalysisLib.MemoryOperations.Class.TClass.EAllocation:
                            HandleOpAllocation( operation as MemOpAllocation );
                            break;
                        case MemAnalysisLib.MemoryOperations.Class.TClass.EDeallocation:
                            HandleOpFree( operation as MemOpFree );
                            break;
                        case MemAnalysisLib.MemoryOperations.Class.TClass.EReallocation:
                            HandleOpReallocation( operation as MemOpReallocation );
                            break;
                        default:
                            break;
                        }
                    }
                }
			}
		}

		protected override void HandleReadCompleted()
		{
			base.HandleReadCompleted();
		}

		protected override void HandleReadException( Exception aException )
		{
			base.HandleReadException( aException );
		}
		#endregion

		#region Data members
		private readonly SymbolManager iSymbolManager;
		private readonly MemAnalysisParserOptions iOptions;
		#endregion
	}
}
