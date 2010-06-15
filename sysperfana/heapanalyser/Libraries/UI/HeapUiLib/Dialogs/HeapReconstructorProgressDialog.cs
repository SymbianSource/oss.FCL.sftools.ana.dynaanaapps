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

namespace HeapUiLib.Dialogs
{
	public class HeapReconstructorProgressDialog : System.Windows.Forms.Form
	{
		#region Windows Form Designer generated code
        private IContainer components;
        private Timer iTimer_OpStart;
        private System.Windows.Forms.ProgressBar iProgressBar;
		#endregion

		#region Constructors & destructor
		public static void ParseLog( HeapReconstructor aReconstructor  )
		{
            ParseLog( aReconstructor, string.Empty );
		}

		public static void ParseLog( HeapReconstructor aReconstructor, string aTitle  )
		{
            HeapReconstructorProgressDialog self = new HeapReconstructorProgressDialog( aReconstructor, aTitle );
			self.ShowDialog();
		}

        internal HeapReconstructorProgressDialog( HeapReconstructor aReconstructor, string aTitle )
		{
            iReconstructor = aReconstructor;
            //
			InitializeComponent();
            //
            iTimer_OpStart.Start();
            //
            if ( aTitle != string.Empty )
            {
                this.Text = aTitle;
            }
            //
            iReconstructor.iObserver += new HeapReconstructor.Observer( HeapReconstructorObserver );
        }

		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if(components != null)
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
            // HeapCellReconstructorProgressDialog
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.ClientSize = new System.Drawing.Size( 392, 39 );
            this.ControlBox = false;
            this.Controls.Add( this.iProgressBar );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "HeapCellReconstructorProgressDialog";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = " Analysing Heap Data...";
            this.ResumeLayout( false );

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
                switch ( aEvent )
                {
                    case HeapReconstructor.TEvent.EReconstructingStarted:
                        iProgressBar.Maximum = 100; //%
                        iProgressBar.Minimum = 0; //%
                        iProgressBar.Value = 0;
                        break;
                    case HeapReconstructor.TEvent.EReconstructingProgress:
                        iProgressBar.Value = iReconstructor.Progress;
                        break;
                    case HeapReconstructor.TEvent.EReconstructingComplete:
                        Close();
                        break;
                }
            }
        }
        #endregion

        #region Event handlers
        private void iTimer_OpStart_Tick( object sender, EventArgs e )
        {
            iTimer_OpStart.Stop();
            iTimer_OpStart.Enabled = false;
            //
            iReconstructor.Reconstruct();
        }
        #endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		#endregion
	}
}
