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
using System.ComponentModel;
using System.Windows.Forms;
using SymbianUtils.Tracer;
using SymbianUtils.Settings;
using SymbianUtils.FileSystem.Utilities;
using HeapLib;
using HeapAnalyser.Engine;
using HeapAnalyser.Engine.Types;

namespace HeapAnalyser.UIs
{
	public abstract class HAUI : ITracer
	{
		#region Constructors
        protected HAUI( string[] aArgs, XmlSettings aSettings, HeapWizardEngine aEngine, ITracer aTracer )
		{
            iArgs = aArgs;
            iSettings = aSettings;
            iEngine = aEngine;
            iTracer = aTracer;
		}
		#endregion

        #region Framework API
        public abstract bool IsAppropriateUI
        {
            get;
        }

        public abstract Form PrepareInitialForm();

        public abstract Form HandleFormClosed( object aSender, EventArgs aArgs );
        #endregion

        #region Properties
        protected string[] Args
        {
            get { return iArgs; }
        }

        protected XmlSettings Settings
        {
            get { return iSettings; }
        }

        protected HeapWizardEngine Engine
        {
            get { return iEngine; }
        }
        #endregion

		#region Event handlers
		#endregion

        #region ITracer Members
        public void Trace( string aMessage )
        {
            iTracer.Trace( aMessage );
        }

        public void Trace( string aFormat, params object[] aParams )
        {
            string text = string.Format( aFormat, aParams );
            Trace( text );
        }
        #endregion

		#region Data members
        private readonly string[] iArgs;
		private readonly XmlSettings iSettings;
		private readonly HeapWizardEngine iEngine;
        private readonly ITracer iTracer;
		#endregion
	}
}
