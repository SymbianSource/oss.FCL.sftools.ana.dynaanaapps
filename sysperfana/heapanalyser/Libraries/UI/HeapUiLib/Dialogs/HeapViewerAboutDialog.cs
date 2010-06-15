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
using HeapLib.Constants;

namespace HeapUiLib.Dialogs
{
	public class HeapViewerAboutDialog : System.Windows.Forms.Form
    {
        #region Windows Form
        private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label iLbl_Version;
        private System.Windows.Forms.Label iLbl_Copyright;
		private System.ComponentModel.Container components = null;
        #endregion

        #region Constructors & destructor
        public HeapViewerAboutDialog()
		{
			InitializeComponent();
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
        /// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
            this.label1 = new System.Windows.Forms.Label();
            this.iLbl_Version = new System.Windows.Forms.Label();
            this.iLbl_Copyright = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.Font = new System.Drawing.Font("Tahoma", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(12, 9);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(336, 16);
            this.label1.TabIndex = 3;
            this.label1.Text = "Heap Analyser";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // iLbl_Version
            // 
            this.iLbl_Version.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.iLbl_Version.Location = new System.Drawing.Point(12, 45);
            this.iLbl_Version.Name = "iLbl_Version";
            this.iLbl_Version.Size = new System.Drawing.Size(336, 16);
            this.iLbl_Version.TabIndex = 4;
            this.iLbl_Version.Text = "v0.99";
            this.iLbl_Version.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // iLbl_Copyright
            // 
            this.iLbl_Copyright.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.iLbl_Copyright.Location = new System.Drawing.Point(12, 68);
            this.iLbl_Copyright.Name = "iLbl_Copyright";
            this.iLbl_Copyright.Size = new System.Drawing.Size(378, 35);
            this.iLbl_Copyright.TabIndex = 1;
            this.iLbl_Copyright.Text = "Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). All rights rese" +
                "rved.";
            this.iLbl_Copyright.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // HeapViewerAboutDialog
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 14);
            this.ClientSize = new System.Drawing.Size(402, 112);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.iLbl_Version);
            this.Controls.Add(this.iLbl_Copyright);
            this.Font = new System.Drawing.Font("Tahoma", 8.25F);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "HeapViewerAboutDialog";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "About Heap Analyser";
            this.Load += new System.EventHandler(this.HeapViewerAboutDialog_Load);
            this.ResumeLayout(false);

		}
		#endregion

        #region Event handlers
        private void HeapViewerAboutDialog_Load( object sender, EventArgs e )
        {
            iLbl_Copyright.Text = HeapLibConstants.Copyright;
            iLbl_Version.Text = HeapLibConstants.Version;
        }
        #endregion
    }
}
