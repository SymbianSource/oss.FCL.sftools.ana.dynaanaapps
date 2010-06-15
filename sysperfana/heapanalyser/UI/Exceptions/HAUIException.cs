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

namespace HeapAnalyser.Exceptions
{
	internal class HAUIException : Exception
	{
        #region Constants
        // <summary>
        // Indicates no error
        // </summary>
        public const int KErrCommandLineNone = 0;

        // <summary>
        // Indicates an unspecified or general error
        // </summary>
        public const int KErrCommandLineGeneral = -1;

        // <summary>
        // Indicates that one or more mandatory command line arguments was omitted
        // </summary>
        public const int KErrCommandLineArgumentsMissing = -2;

        // <summary>
        // Indicates that an input command was not supported or valid
        // </summary>
        public const int KErrCommandLineInvalidCommand = -3;

        // <summary>
        // Indicates that the specified input file was not found
        // </summary>
        public const int KErrCommandLineArgumentsFileNotFound = -4;

        // <summary>
        // Indicates a fatal problem when attempting to read the input file
        // </summary>
        public const int KErrCommandLineArgumentsFileInvalid = -5;

        // <summary>
        // Indicates source file(s) were not found or specified
        // </summary>
        public const int KErrCommandLineSourceFileNotFound = -6;

        // <summary>
        // Indicates debug meta data was omitted from the inputs
        // </summary>
        public const int KErrCommandLineDebugMetaDataMissing = -7;

        // <summary>
        // Occurs if Heap Analyser cannot find a handler for the specified command
        // line arguments.
        // </summary>
        public const int KErrCommandLineUINotAvailable = -8;

        // <summary>
        // The requested analysis type is not supported
        // </summary>
        public const int KErrCommandLineAnalysisTypeNotSupported = -9;

        // <summary>
        // The specified analysis thread name is invalid
        // </summary>
        public const int KErrCommandLineAnalysisThreadNameInvalid = -10;

        // <summary>
        // The specified output data is invalid
        // </summary>
        public const int KErrCommandLineAnalysisOutputInvalid = -11;
        #endregion
        
        #region Constructors
        public HAUIException( string aMessage )
            : this( aMessage, KErrCommandLineGeneral )
		{
		}
       
        public HAUIException( string aMessage, Exception aInnerException )
            : this( aMessage, aInnerException, KErrCommandLineGeneral )
        {
        }

        public HAUIException( string aMessage, Exception aInnerException, int aErrorCode )
            : base( aMessage, aInnerException )
        {
            iErrorCode = aErrorCode;
        }
       
        public HAUIException( string aMessage, int aErrorCode )
            : base( aMessage )
		{
            iErrorCode = aErrorCode;
		}
        #endregion

		#region API
        #endregion

		#region Properties
        public int ErrorCode
        {
            get { return iErrorCode; }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Member data
        private readonly int iErrorCode;
        #endregion
	}
}
