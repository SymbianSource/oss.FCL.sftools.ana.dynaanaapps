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
using SymbianUtils;
using SymbianDebugLib.Engine;
using SymbianDebugLib.PluginAPI.Types;
using HeapLib;
using HeapLib.Reconstructor.Misc;
using HeapAnalyser.Engine.Types;

namespace HeapAnalyser.Engine
{
	public class HeapWizardEngine
	{
		#region Enumerations
		public enum TOperationType
		{
			EOperationTypeAnalyseAndView = 0,
            EOperationTypeCompareHeapDumps,
            EOperationTypeCompareHeapCSV,
		}
		#endregion

		#region Constructors & destructor
		public HeapWizardEngine( string aPrivatePathBaseDirectory, string[] aArgs )
		{
            iAnalysisEngine = new AnalyseEngineHeapData( this );
            iComparsionEngineData = new ComparisonEngineHeapData( this );
            iComparsionEngineCSV = new ComparisonEngineHeapCSV( this );
		}
		#endregion

		#region Properties
		public TOperationType OperationType
		{
			get { return iOperationType; }
			set { iOperationType = value; }
		}

        public Options HeapDataOptions
        {
            get { return iHeapDataOptions; }
            set { iHeapDataOptions = value; }
        }

        public DbgEngine DebugEngine
        {
            get { return iDebugEngine; }
        }
        #endregion

        #region Analysis engines
        public AnalyseEngineHeapData AnalysisEngine
		{
			get { return iAnalysisEngine; }
		}

        public ComparisonEngineHeapData ComparisonEngineData
        {
            get { return iComparsionEngineData; }
        }

        public ComparisonEngineHeapCSV ComparisonEngineCSV
        {
            get { return iComparsionEngineCSV; }
        }
		#endregion

		#region Data members
        private TOperationType iOperationType = TOperationType.EOperationTypeAnalyseAndView;
        private Options iHeapDataOptions = new Options();
        private DbgEngine iDebugEngine = new DbgEngine();
        private readonly AnalyseEngineHeapData iAnalysisEngine;
        private readonly ComparisonEngineHeapData iComparsionEngineData;
        private readonly ComparisonEngineHeapCSV iComparsionEngineCSV;
		#endregion
	}
}
