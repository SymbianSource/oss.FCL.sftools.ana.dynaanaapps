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
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Text;
using SymbianUtils.Tracer;
using SymbianUtils.Settings;
using SymbianUtils.FileSystem.Utilities;
using SymbianUtilsUi.Dialogs;
using HeapLib;
using HeapUiLib.Forms;
using HeapAnalyser.Engine;
using HeapAnalyser.Engine.Types;
using HeapAnalyser.UIs.Graphical.Wizard;
using HeapComparisonUiLib.Progress;

namespace HeapAnalyser.UIs.Graphical
{
	public class HAUIGraphical : HAUI
	{
		#region Constructors
        public HAUIGraphical( string[] aArgs, XmlSettings aSettings, HeapWizardEngine aEngine, ITracer aTracer )
            : base( aArgs, aSettings, aEngine, aTracer )
		{
		}
		#endregion

		#region From HAUI
        public override bool IsAppropriateUI
        {
            get 
            { 
                // There should be no command line arguments when using the graphical UI
                return base.Args.Length == 0; 
            }
        }

        public override Form PrepareInitialForm()
        {
            iWizard = new HASetupWizard( base.Settings, base.Engine );
            return iWizard;
        }

        public override Form HandleFormClosed( object aSender, EventArgs aArgs )
        {
            Form ret = null;
            //
            if ( aSender is HASetupWizard )
            {
                // Check if the wizard finished successfully...
                string result = base.Settings[ "Wizard", "DialogResult" ];
                if ( result == DialogResult.OK.ToString() )
                {
                    // Decide which form to show next
                    if ( base.Engine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeAnalyseAndView )
                    {
                        base.Engine.AnalysisEngine.CreateParser();
                        //
                        iAnalysisViewerForm = new HeapViewerForm( base.Engine.AnalysisEngine.Reconstructor, base.Settings );
                        ret = this.iAnalysisViewerForm;
                    }
                    else if ( base.Engine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapDumps )
                    {
                        // NB: Shows progress dialog immediately
                        base.Engine.ComparisonEngineData.CreateReconstructors();
                        ComparisonProgressDialogData.Compare( base.Engine.ComparisonEngineData.Reconstructor1,
                                                              base.Engine.ComparisonEngineData.Reconstructor2,
                                                              base.Engine.ComparisonEngineData.OutputFileName );
                    }
                    else if ( base.Engine.OperationType == HeapWizardEngine.TOperationType.EOperationTypeCompareHeapCSV )
                    {
                        // NB: Shows progress dialog immediately
                        ComparisonProgressDialogCSV.Compare( base.Engine.ComparisonEngineCSV.SourceFileNames, base.Engine.ComparisonEngineCSV.OutputDirectory );
                    }
                }
            }
            //
            return ret;
        }
        #endregion

		#region Data members
        private HASetupWizard iWizard = null;
		private HeapViewerForm iAnalysisViewerForm = null;
		#endregion
	}
}
