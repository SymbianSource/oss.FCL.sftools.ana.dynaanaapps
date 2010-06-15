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
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLPageHeapCellManager : AsyncTextWriterBase
	{
		#region Constructors & destructor
		public HeapToHTMLPageHeapCellManager( HeapReconstructor aReconstructor, string aBasePath )
		{
			iReconstructor = aReconstructor;
			iEntries = iReconstructor.Data;
			iBasePath = aBasePath;

			// Make sure our required directories exist
			HeapToHTMLConverter.PageDirectoryNameEnsuringPathExists( iBasePath, "HeapData" );
			HeapToHTMLConverter.PageDirectoryNameEnsuringPathExists( iBasePath, "HeapLinkInfo" );
		}
		#endregion
		
		#region API
        public static string HeapCellFileName( HeapCell aCell )
        {
            // Basic filename
            string basicName = aCell.Address.ToString( "x8" );
            return basicName;
        }

        public static string HeapCellDirectory( HeapCell aCell, string aStandardSubDir, char aDirectorySeparatorCharacter )
        {
            string subDirType = "Free";

            // Work out which subdir to put the file in. Allocated & free cells go in different base directories
            // We don't put more than KMaxNumberOfFilesPerDirectory (100) files in each directory.
            if ( aCell.Type == HeapCell.TType.EAllocated )
            {
                subDirType = "Allocated";
            }

            uint rampedIndex = aCell.Index / KMaxNumberOfFilesPerDirectory;
            string subDirCellIndex = rampedIndex.ToString( "d4" );
            string combinedSubDir = aStandardSubDir + aDirectorySeparatorCharacter + subDirType + aDirectorySeparatorCharacter + subDirCellIndex;
            return combinedSubDir;
        }

        public static string HeapCellFileNameAndPath( HeapCell aCell, string aBasePath, string aStandardSubDir )
        {
            string basicName = HeapCellFileName( aCell );
            string combinedSubDir = HeapCellDirectory( aCell, aStandardSubDir, System.IO.Path.DirectorySeparatorChar );
            string heapDataFileName = HeapToHTMLConverter.PageFileNameEnsuringPathExists( basicName, aBasePath, combinedSubDir );
            return heapDataFileName;
        }
		#endregion

		#region From AsyncTextWriterBase
        public override long Size
		{
			get
			{
				return (long) iEntries.Count;
			}
		}

        public override long Position
		{
			get
			{
				long pos = (long) iCurrentHeapCellIndex;
				return pos;
			}
		}

        public override void ExportData()
		{
			HeapCell entry = iEntries[ iCurrentHeapCellIndex++ ];

			// Create heap data writer
			string heapDataFileName = HeapCellFileNameAndPath( entry, iBasePath, "HeapData" );
			using( HeapToHTMLPageHeapData writer = new HeapToHTMLPageHeapData( iReconstructor, heapDataFileName, entry ) )
			{
				writer.ConstructWriter();
				writer.ExportData();
			}

			// Create heap linkced cell writer
            string heapLinkInfoFileName = HeapCellFileNameAndPath( entry, iBasePath, "HeapLinkInfo" );
			using( HeapToHTMLPageHeapLinkedCells writer = new HeapToHTMLPageHeapLinkedCells( iReconstructor, heapLinkInfoFileName, entry ) )
			{
				writer.ConstructWriter();
				writer.ExportData();
			}
		}
		#endregion

        #region Internal constants
        private const uint KMaxNumberOfFilesPerDirectory = 100;
        #endregion

        #region Data members
        private readonly HeapReconstructor iReconstructor;
		private readonly HeapCellArray iEntries;
		private readonly string iBasePath;
		private int iCurrentHeapCellIndex = 0;
		#endregion
	}
}
