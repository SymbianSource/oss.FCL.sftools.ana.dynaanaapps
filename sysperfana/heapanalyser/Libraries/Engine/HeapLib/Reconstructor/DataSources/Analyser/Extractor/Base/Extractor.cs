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
using System.Collections.Generic;
using System.Text;
using System.Reflection;
using HeapLib.Reconstructor.DataSources.Analyser.Interpreter;

namespace HeapLib.Reconstructor.DataSources.Analyser.Extractor
{
    // <summary>
    // The job of the extractor is just to remove any prefix associated
    // with a given line. For example, the text-based file formats contain
    // prefixes which allow multiple heap dumps to appear within one 
    // trace file. We must partition these heap dumps into individual data
    // sources, hence we must be able to identify those prefixes and use
    // them during the partitioning operation.
    // 
    // This is the sole purpose for this class.
    // </summary>
    internal abstract class Extractor
    {
        #region Constructors & destrutor
        protected Extractor()
        {
        }
        #endregion

        #region API
        public abstract bool CanExtractFrom( string aLine );

        public abstract bool ExtractFrom( string aLine, bool aForceExtractionOfThreadIdentifier );
        #endregion

        #region Properties
        public abstract string Hash { get; }

        public string PrimaryLine
        {
            get { return iPrimaryLine; }
            protected set { iPrimaryLine = value; }
        }

        public InterpreterBase Interpreter
        {
            get { return iInterpreter; }
            set { iInterpreter = value; }
        }
        #endregion

        #region Internal methods
        #endregion

        #region Data members
        private string iPrimaryLine = string.Empty;
        private InterpreterBase iInterpreter = null;
        #endregion
    }
}
