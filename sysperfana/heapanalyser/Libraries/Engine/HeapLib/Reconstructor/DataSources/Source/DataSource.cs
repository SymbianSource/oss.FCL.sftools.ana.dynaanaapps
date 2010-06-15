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
using System.Text.RegularExpressions;
using System.Collections;
using System.Collections.Generic;
using SymbianUtils.FileSystem.FilePair;
using HeapLib.Reconstructor.DataSources.Analyser.Interpreter;
using HeapLib.Reconstructor.DataSources.Analyser.Extractor;

namespace HeapLib.Reconstructor.DataSources
{
    public class DataSource
    {
        #region Enumerations
        public enum TSourceType
        {
            ESourceTypeTextTrace = 0,
            ESourceTypeTextDynamic,
            ESourceTypeBinary
        }

        [Flags]
        public enum TErrorTypes
        {
            EErrorTypeNone = 0,
            EErrorTypeInvalidFreeCellIndexInFreeList = 1,
            EErrorTypeFreeCellAddressOutOfBounds = 2,
            EErrorTypeDataMissing = 4,
        }
        #endregion

        #region Constructors & destructor
        internal DataSource( string aFileName, long aLineNumber )
        {
            iFileName = aFileName;
            iLineNumber = aLineNumber;
        }
        #endregion

        #region API
        internal void AddLine( string aLine )
        {
            iLines.Add( aLine );
        }

        internal void AddError( TErrorTypes aError )
        {
            iErrorType |= aError;
        }

        public bool ErrorsDetected()
        {
            string msg = string.Empty;
            return ErrorsDetected( out msg );
        }

        public bool ErrorsDetected( out string aMessage )
        {
            StringBuilder msg = new StringBuilder();
            //
            if ( ( iErrorType & TErrorTypes.EErrorTypeDataMissing ) == TErrorTypes.EErrorTypeDataMissing )
            {
                msg.AppendLine( "ERROR: Data is incomplete." );
            }
            if ( ( iErrorType & TErrorTypes.EErrorTypeInvalidFreeCellIndexInFreeList ) == TErrorTypes.EErrorTypeInvalidFreeCellIndexInFreeList )
            {
                msg.AppendLine( "ERROR: Mis-match between free cell list and free cell count." );
            }
            if ( ( iErrorType & TErrorTypes.EErrorTypeFreeCellAddressOutOfBounds ) == TErrorTypes.EErrorTypeFreeCellAddressOutOfBounds )
            {
                msg.AppendLine( "ERROR: Free cell address falls outside heap bounds." );
            }
            //
            aMessage = msg.ToString();
            return ( iErrorType != TErrorTypes.EErrorTypeNone );
        }

        public FileNamePair PrepareSourceFileDataForZip()
        {
            FileNamePair ret = new FileNamePair( FileName );

            // Need to make a temporary file
            string tempFileName = Path.GetTempFileName();
            using ( StreamWriter writer = new StreamWriter( tempFileName, false ) )
            {
                foreach ( string line in iLines )
                {
                    writer.WriteLine( line );
                }
            }

            ret = new FileNamePair( tempFileName );
            ret.Destination = "HeapData/SourceData.txt";
            ret.DeleteFile = true;
            //
            return ret;
        }
        #endregion

        #region Properties
        public TSourceType SourceType
        {
            get
            {
                TSourceType ret = TSourceType.ESourceTypeTextDynamic;
                //
                if ( FileName.Length > 0 )
                {
                    ret = TSourceType.ESourceTypeTextTrace;
                }
                //
                return ret;
            }
        }

        public string FileName
        {
            get { return iFileName; }
        }

        public string ThreadName
        {
            get { return iThreadName; }
            set { iThreadName = value; }
        }

        public Elements.MetaData MetaData
        {
            get { return iMetaData; }
        }

        internal bool IsEmpty
        {
            get
            {
                bool empty = iMetaData.HeapData.Count == 0;
                return empty;
            }
        }

        public long LineNumber
        {
            get { return iLineNumber; }
        }
        #endregion

        #region From System.Object
        public override string ToString()
        {
            StringBuilder ret = new StringBuilder();
            ret.AppendFormat( "{0} @ line {1:d6}, {2} bytes, {3} allocs", ThreadName, LineNumber, MetaData.Heap.ChunkSize, MetaData.Heap.InfoAlloc.AllocCellCount );
            return ret.ToString();
        }
        #endregion

        #region Data members
        private readonly string iFileName;
        private readonly long iLineNumber;
        private TErrorTypes iErrorType = TErrorTypes.EErrorTypeNone;
        private string iThreadName = string.Empty;
        private List<string> iLines = new List<string>();
        private Elements.MetaData iMetaData = new HeapLib.Reconstructor.DataSources.Elements.MetaData();
        #endregion
    }
}
