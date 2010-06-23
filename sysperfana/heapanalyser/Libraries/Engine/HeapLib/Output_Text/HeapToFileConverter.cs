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
using HeapLib.Array;
using HeapLib.Cells;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToFileConverter
	{
		public HeapToFileConverter( bool aShowOnlySymbolicMatches, HeapReconstructor aReconstructor )
		{
			iShowOnlySymbolicMatches = aShowOnlySymbolicMatches;
			iReconstructor = aReconstructor;
		}
		
		public void Export( string aFileName )
		{
			using( StreamWriter writer = new StreamWriter( aFileName ) )
			{
				DoWriteHeapToFile( writer );
				writer.Flush();
			}
		}

		private void DoWriteHeapToFile( StreamWriter aWriter )
		{
			aWriter.WriteLine( "Cell\t\tCounter\t\tLength\t\tSymbol" );
			aWriter.WriteLine( "" );
			//
			HeapCellArray heapItems = iReconstructor.Data;
			for (int r = 0; r < heapItems.Count; r++)
			{
				HeapCell entry = (HeapCell) heapItems[r];
				//
				if	( iShowOnlySymbolicMatches == false || (iShowOnlySymbolicMatches == true && entry.Symbol != null) )
				{
					StringBuilder line = new StringBuilder();
					//
					line.Append( entry.Address.ToString("x8") );
					line.Append( "\t" );
					line.Append( entry.AllocationNumber.ToString("d8") );
					line.Append( "\t" );
					line.Append( entry.Length.ToString("x8") );
					line.Append( "\t" );
					if	( entry.Symbol != null )
					{
						line.Append( entry.Symbol.Name );
					}
					//
					aWriter.WriteLine( line.ToString() );
				}
			}
		}	

		#region Data members
		private readonly bool iShowOnlySymbolicMatches;
		private readonly HeapReconstructor iReconstructor;
		#endregion
	}
}