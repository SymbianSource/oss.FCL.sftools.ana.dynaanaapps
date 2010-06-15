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
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Forms;
using SymbianUtils;
using HeapComparisonLib.CSV;

namespace HeapComparisonUiLib.Progress
{
	public class ComparisonProgressDialogCSV : System.Windows.Forms.Form
	{
		#region Windows Form Designer generated code
        private IContainer components;
        private Timer iTimer_OpStart;
        private System.Windows.Forms.ProgressBar iProgressBar;
		#endregion

		#region Static constructor
		public static void Compare( List<string> aSource, string aDestinationDir )
		{
            ComparisonProgressDialogCSV self = new ComparisonProgressDialogCSV( aSource, aDestinationDir );
			self.ShowDialog();
		}
		#endregion

        #region Constructors & destructor
        private ComparisonProgressDialogCSV( List<string> aSource, string aDestinationDir )
        {
            iEngine = new CSVComparisonEngine( aSource, aDestinationDir );
            iEngine.eExceptionHandler += new CSVComparisonEngine.ExceptionHandler( Engine_ExceptionHandler );
            iEngine.eEventHandler += new CSVComparisonEngine.EventHandler( Engine_EventHandler );
            iEngine.eIndexedProgressHandler += new CSVComparisonEngine.IndexedProgressHandler( Engine_IndexedProgressHandler );
            iEngine.ePercentageProgressHandler += new CSVComparisonEngine.PercentageProgressHandler( Engine_PercentageProgressHandler );
            //
            InitializeComponent();
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
            this.iProgressBar = new System.Windows.Forms.ProgressBar();
            this.iTimer_OpStart = new System.Windows.Forms.Timer( this.components );
            this.SuspendLayout();
            // 
            // iProgressBar
            // 
            this.iProgressBar.Location = new System.Drawing.Point( 8, 8 );
            this.iProgressBar.Name = "iProgressBar";
            this.iProgressBar.Size = new System.Drawing.Size( 376, 24 );
            this.iProgressBar.TabIndex = 0;
            // 
            // iTimer_OpStart
            // 
            this.iTimer_OpStart.Interval = 10;
            this.iTimer_OpStart.Tick += new System.EventHandler( this.iTimer_OpStart_Tick );
            // 
            // ComparisonProgressDialogCSV
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.ClientSize = new System.Drawing.Size( 392, 39 );
            this.ControlBox = false;
            this.Controls.Add( this.iProgressBar );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ComparisonProgressDialogCSV";
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
            //
            iEngine.CompareAsync();
        }
        #endregion

        #region Engine observer
        void Engine_PercentageProgressHandler( CSVComparisonEngine.TEvent aEvent, CSVComparisonEngine aSender, int aProgressPercent )
        {
            if ( InvokeRequired )
            {
                CSVComparisonEngine.PercentageProgressHandler observer = new CSVComparisonEngine.PercentageProgressHandler( Engine_PercentageProgressHandler );
                this.BeginInvoke( observer, new object[] { aEvent, aSender, aProgressPercent } );
            }
            else
            {
                iProgressBar.Value = aProgressPercent;
            }
        }

        void Engine_IndexedProgressHandler( CSVComparisonEngine.TEvent aEvent, CSVComparisonEngine aSender, int aCurrentIndex, int aMaxIndex )
        {
            if ( InvokeRequired )
            {
                CSVComparisonEngine.IndexedProgressHandler observer = new CSVComparisonEngine.IndexedProgressHandler( Engine_IndexedProgressHandler );
                this.BeginInvoke( observer, new object[] { aEvent, aSender, aCurrentIndex, aMaxIndex } );
            }
            else
            {
                if ( aEvent == CSVComparisonEngine.TEvent.EEventComparingMovedToNewDataSet )
                {
                    Text = string.Format( "Performing comparsion {0}/{1}", aCurrentIndex, aMaxIndex );
                }
                else if ( aEvent == CSVComparisonEngine.TEvent.EEventSplittingMovedToNewFile )
                {
                    Text = string.Format( "Extracting data set(s) from file {0}/{1}", aCurrentIndex, aMaxIndex ); 
                }

                // Reset progress
                iProgressBar.Maximum = 100; //%
                iProgressBar.Minimum = 0; //%
                iProgressBar.Value = 0;
            }
        }

        void Engine_EventHandler( CSVComparisonEngine.TEvent aEvent, CSVComparisonEngine aSender )
        {
            if ( InvokeRequired )
            {
                CSVComparisonEngine.EventHandler observer = new CSVComparisonEngine.EventHandler( Engine_EventHandler );
                this.BeginInvoke( observer, new object[] { aEvent, aSender } );
            }
            else
            {
                if ( aEvent == CSVComparisonEngine.TEvent.EEventOperationStarted )
                {
                }
                else if ( aEvent == CSVComparisonEngine.TEvent.EEventOperationComplete )
                {
                    iProgressBar.Value = 100;
                    Close();
                }
            }
        }

        void Engine_ExceptionHandler( string aTitle, string aBody )
        {
            if ( InvokeRequired )
            {
                CSVComparisonEngine.ExceptionHandler observer = new CSVComparisonEngine.ExceptionHandler( Engine_ExceptionHandler );
                this.BeginInvoke( observer, new object[] { aTitle, aBody } );
            }
            else
            {
                MessageBox.Show( aBody, aBody, MessageBoxButtons.OK );
                Close();
            }
        }
        #endregion

		#region Data members
        private readonly CSVComparisonEngine iEngine;
		#endregion
	}
}
