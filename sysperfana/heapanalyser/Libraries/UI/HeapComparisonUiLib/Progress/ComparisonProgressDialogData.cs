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
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using SymbianUtils;
using HeapLib;
using HeapLib.Reconstructor;
using HeapComparisonLib.Data;

namespace HeapComparisonUiLib.Progress
{
    public class ComparisonProgressDialogData : System.Windows.Forms.Form
    {
        #region Windows Form Designer generated code
        private IContainer components;
        private ProgressBar iProgBar;
        private Timer iTimer_OpStart;
        #endregion

        #region Static API
        public static void Compare( HeapReconstructor aReconstructor1, HeapReconstructor aReconstructor2, string aFileName )
        {
            ComparisonProgressDialogData self = new ComparisonProgressDialogData( aReconstructor1, aReconstructor2, aFileName );
            self.ShowDialog();
        }
        #endregion

        #region Constructors & destructor
        private ComparisonProgressDialogData( HeapReconstructor aReconstructor1, HeapReconstructor aReconstructor2, string aFileName )
        {
            iReconstructor1 = aReconstructor1;
            iReconstructor2 = aReconstructor2;
            //
            InitializeComponent();
            //
            iReconstructor1.iObserver += new HeapReconstructor.Observer( HeapReconstructorObserver );
            iReconstructor2.iObserver += new HeapReconstructor.Observer( HeapReconstructorObserver );
            //
            iComparisonEngine = new ComparsionEngine( iReconstructor1, iReconstructor2 );
            iComparisonEngine.iObserver += new AsyncReaderBase.Observer( ComparisonEngineObserver );
            //
            iComparisonWriter = new ComparisonWriter( iComparisonEngine, aFileName );
            iComparisonWriter.iObserver += new AsyncTextWriterBase.Observer( ComparsionWriterObserver );
            //
            iTimer_OpStart.Start();
        }

        protected override void Dispose( bool disposing )
        {
            if ( disposing )
            {
                if ( components != null )
                {
                    components.Dispose();
                }
            }
            base.Dispose( disposing );
        }
        #endregion

        #region Windows Form Designer generated code
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.iTimer_OpStart = new System.Windows.Forms.Timer( this.components );
            this.iProgBar = new System.Windows.Forms.ProgressBar();
            this.SuspendLayout();
            // 
            // iTimer_OpStart
            // 
            this.iTimer_OpStart.Interval = 10;
            this.iTimer_OpStart.Tick += new System.EventHandler( this.iTimer_OpStart_Tick );
            // 
            // iProgBar
            // 
            this.iProgBar.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iProgBar.Location = new System.Drawing.Point( 3, 3 );
            this.iProgBar.Margin = new System.Windows.Forms.Padding( 0 );
            this.iProgBar.Name = "iProgBar";
            this.iProgBar.Size = new System.Drawing.Size( 351, 26 );
            this.iProgBar.TabIndex = 2;
            // 
            // ComparisonProgressDialogData
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.ClientSize = new System.Drawing.Size( 357, 32 );
            this.ControlBox = false;
            this.Controls.Add( this.iProgBar );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ComparisonProgressDialogData";
            this.Padding = new System.Windows.Forms.Padding( 3 );
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Analysing...";
            this.ResumeLayout( false );

        }
        #endregion

        #region Event handlers
        private void iTimer_OpStart_Tick( object sender, EventArgs e )
        {
            iTimer_OpStart.Stop();
            iTimer_OpStart.Enabled = false;

            // Start process by analysing the first heap
            iReconstructor1.Reconstruct();
        }
        #endregion

        #region Heap reconstructor callback
        private void HeapReconstructorObserver( HeapReconstructor.TEvent aEvent, HeapReconstructor aReconstructor )
        {
            if ( InvokeRequired )
            {
                HeapReconstructor.Observer observer = new HeapReconstructor.Observer( HeapReconstructorObserver );
                this.BeginInvoke( observer, new object[] { aEvent, aReconstructor } );
            }
            else
            {
                // Select correct progress bar...
                string title = "Reading Heap 1...";
                if ( aReconstructor == iReconstructor2 )
                {
                    title = "Reading Heap 2...";
                }
                //
                switch ( aEvent )
                {
                case HeapReconstructor.TEvent.EReconstructingStarted:
                    this.Text = title;
                    iProgBar.Maximum = 100; //%
                    iProgBar.Minimum = 0; //%
                    iProgBar.Value = 0;
                    break;
                case HeapReconstructor.TEvent.EReconstructingProgress:
                    iProgBar.Value = aReconstructor.Progress;
                    break;
                case HeapReconstructor.TEvent.EReconstructingComplete:
                    iProgBar.Value = 100;

                    if ( aReconstructor == iReconstructor1 )
                    {
                        // Finished first heap, now move on to second
                        iReconstructor2.Reconstruct();
                    }
                    else
                    {
                        // Start output engine
                        iComparisonEngine.Compare();
                    }
                    break;
                }
            }
        }
        #endregion

        #region Comparsion engine observer
        private void ComparisonEngineObserver( AsyncReaderBase.TEvent aEvent, AsyncReaderBase aSender )
        {
            if ( InvokeRequired )
            {
                SymbianUtils.AsyncReaderBase.Observer observer = new SymbianUtils.AsyncReaderBase.Observer( ComparisonEngineObserver );
                this.BeginInvoke( observer, new object[] { aEvent, aSender } );
            }
            else
            {
                switch ( aEvent )
                {
                case SymbianUtils.AsyncReaderBase.TEvent.EReadingStarted:
                    this.Text = "Comparing Heaps...";
                    iProgBar.Maximum = 100; //%
                    iProgBar.Minimum = 0; //%
                    iProgBar.Value = 0;
                    break;
                case SymbianUtils.AsyncReaderBase.TEvent.EReadingProgress:
                    iProgBar.Value = aSender.Progress;
                    break;
                case SymbianUtils.AsyncReaderBase.TEvent.EReadingComplete:
                    iProgBar.Value = 100;
                    iComparisonWriter.SaveAsExcel();
                    break;
                }
            }
        }
        #endregion

        #region Comparsion writer observer
        private void ComparsionWriterObserver( AsyncTextWriterBase.TEvent aEvent, AsyncTextWriterBase aObject )
        {
            if ( InvokeRequired )
            {
                SymbianUtils.AsyncTextWriterBase.Observer observer = new SymbianUtils.AsyncTextWriterBase.Observer( ComparsionWriterObserver );
                this.BeginInvoke( observer, new object[] { aEvent, aObject } );
            }
            else
            {
                switch ( aEvent )
                {
                case SymbianUtils.AsyncTextWriterBase.TEvent.EWritingStarted:
                    this.Text = "Creating Excel File...";
                    iProgBar.Maximum = 100; //%
                    iProgBar.Minimum = 0; //%
                    iProgBar.Value = 0;
                    break;
                case SymbianUtils.AsyncTextWriterBase.TEvent.EWritingProgress:
                    iProgBar.Value = aObject.Progress;
                    break;
                case SymbianUtils.AsyncTextWriterBase.TEvent.EWritingComplete:
                    iProgBar.Value = 100;
                    Close();
                    break;
                }
            }
        }
        #endregion

        #region Data members
        private readonly HeapReconstructor iReconstructor1;
        private readonly HeapReconstructor iReconstructor2;
        private readonly ComparsionEngine iComparisonEngine;
        private readonly ComparisonWriter iComparisonWriter;
        #endregion
    }
}
