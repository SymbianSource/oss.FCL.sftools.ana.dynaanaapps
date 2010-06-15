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
using System.Windows.Forms;
using SymbianDebugLib.Engine;
using SymbianUtils;
using HeapLib;
using HeapLib.Reconstructor;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.Misc;
using HeapComparisonLib.Data;

namespace HeapAnalyser.Engine.Types
{
    public class ComparisonEngineHeapData
    {
		#region Constructors & destructor
        public ComparisonEngineHeapData( HeapWizardEngine aParent )
		{
            iParent = aParent;
		}
		#endregion

		#region API
        public void CreateReconstructors()
		{
            iReconstructor1 = new HeapReconstructor( DataSource1, ReconstructorOptions, DebugEngine );
            iReconstructor2 = new HeapReconstructor( DataSource2, ReconstructorOptions, DebugEngine );
        }
		#endregion

		#region Properties
		public HeapReconstructor Reconstructor1
		{
			get { return iReconstructor1; }
		}

        public HeapReconstructor Reconstructor2
        {
            get { return iReconstructor2; }
        }

        public DataSource DataSource1
		{
            get { return iDataSource1; }
            set { iDataSource1 = value; }
		}

        public DataSource DataSource2
        {
            get { return iDataSource2; }
            set { iDataSource2 = value; }
        }

        public HeapLib.Reconstructor.Misc.Options ReconstructorOptions
        {
            get { return iParent.HeapDataOptions; }
        }

        public DbgEngine DebugEngine
        {
            get { return iParent.DebugEngine; }
        }

		public string OutputFileName
		{
            get { return iOutputFileName; }
            set { iOutputFileName = value; }
		}
		#endregion

		#region Internal methods
		#endregion

		#region Member data
        private readonly HeapWizardEngine iParent;
        private string iOutputFileName = string.Empty;
        //
		private HeapReconstructor iReconstructor1;
        private HeapReconstructor iReconstructor2;
        private DataSource iDataSource1 = null;
        private DataSource iDataSource2 = null;
		#endregion
   }
}
