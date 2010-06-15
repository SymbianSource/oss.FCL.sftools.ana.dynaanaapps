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
using System.Collections;
using System.Collections.Specialized;
using SymbianDebugLib.Engine;
using SymbianDebugLib.PluginAPI.Types;
using SymbianUtils;
using HeapLib;
using HeapLib.Reconstructor;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;

namespace HeapAnalyser.Engine.Types
{
	public class AnalyseEngineHeapData
	{
		#region Constructors & destructor
        public AnalyseEngineHeapData( HeapWizardEngine aParent )
		{
            iParent = aParent;
		}
		#endregion

		#region API
		public void CreateParser()
		{
            if ( iDataSource == null )
            {
                throw new NotSupportedException( "Data source is not yet initialised" );
            }
            //
            iReconstructor = new HeapReconstructor( DataSource, ReconstructorOptions, DebugEngine );
            HeapUiLib.Dialogs.HeapReconstructorProgressDialog.ParseLog( Reconstructor );
		}
		#endregion

		#region Properties
		public HeapReconstructor Reconstructor
		{
			get { return iReconstructor; }
		}

		public DataSource DataSource
		{
			get { return iDataSource; }
			set { iDataSource = value; }
		}

        public DbgEngine DebugEngine
		{
			get { return iParent.DebugEngine; }
        }

		public Options ReconstructorOptions
		{
			get { return iParent.HeapDataOptions; }
        }
		#endregion

		#region Internal methods
		#endregion

		#region Member data
        private readonly HeapWizardEngine iParent;
        //
        private HeapReconstructor iReconstructor;
        private DataSource iDataSource = null;
		#endregion
	}
}
