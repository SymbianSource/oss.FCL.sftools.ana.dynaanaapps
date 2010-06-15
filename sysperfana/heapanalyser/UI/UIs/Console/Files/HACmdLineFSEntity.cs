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
using System.Text;
using System.IO;
using System.Collections.Generic;

namespace HeapAnalyser.UIs.Console.Files
{
    internal class HACmdLineFSEntity
	{
        #region Constructors & destructor
        public HACmdLineFSEntity()
		{
		}
        #endregion

		#region API
        #endregion

		#region Properties
        public FileInfo File
        {
            get { return iFile; }
            set { iFile = value; }
        }

        public DirectoryInfo Directory
        {
            get { return iDirectory; }
            set { iDirectory = value; }
        }

        public bool IsFile
        {
            get { return ( iFile != null ); }
        }

        public bool IsDirectory
        {
            get { return ( iDirectory != null); }
        }

        public string Name
        {
            get
            {
                StringBuilder ret = new StringBuilder();
                //
                if ( IsDirectory )
                {
                    ret.Append( Directory.FullName );
                }
                else if ( IsFile )
                {
                    ret.Append( File.FullName );
                }
                //
                return ret.ToString(); 
            }
        }

        public bool Exists
        {
            get
            {
                bool ret = false;
                //
                if ( IsDirectory )
                {
                    ret = Directory.Exists;
                }
                else if ( IsFile )
                {
                    ret = File.Exists;
                }
                //
                return ret;
            }
        }
        
        public object Tag
        {
            get { return iTag; }
            set { iTag = value; }
        }
        #endregion

        #region Operators
        public static implicit operator FileInfo( HACmdLineFSEntity aFile )
        {
            return aFile.iFile;
        }
        #endregion

        #region Internal methods
        #endregion

        #region From System.Object
        public override string ToString()
        {
            return Name;
        }
        #endregion

        #region Member data
        private object iTag = null;
        private FileInfo iFile = null;
        private DirectoryInfo iDirectory = null;
        #endregion
	}
}
