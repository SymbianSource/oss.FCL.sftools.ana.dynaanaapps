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

namespace HeapCtrlLib.Dialogs
{
    partial class HeapRendererFilterConfigDialog
    {
        // <summary>
        // Required designer variable.
        // </summary>
        private System.ComponentModel.IContainer components = null;

        // <summary>
        // Clean up any resources being used.
        // </summary>
        // <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose( bool disposing )
        {
            if ( disposing && ( components != null ) )
            {
                components.Dispose();
            }
            base.Dispose( disposing );
        }

        #region Windows Form Designer generated code

        // <summary>
        // Required method for Designer support - do not modify
        // the contents of this method with the code editor.
        // </summary>
        private void InitializeComponent()
        {
            this.iFilterControl = new HeapCtrlLib.Controls.HeapFilteringControl();
            this.iBT_Done = new System.Windows.Forms.Button();
            this.iBT_EnableAll = new System.Windows.Forms.Button();
            this.iBT_EnableNone = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // iFilterControl
            // 
            this.iFilterControl.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iFilterControl.Location = new System.Drawing.Point( 12, 12 );
            this.iFilterControl.Name = "iFilterControl";
            this.iFilterControl.Size = new System.Drawing.Size( 577, 226 );
            this.iFilterControl.TabIndex = 0;
            // 
            // iBT_Done
            // 
            this.iBT_Done.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iBT_Done.Location = new System.Drawing.Point( 601, 215 );
            this.iBT_Done.Name = "iBT_Done";
            this.iBT_Done.Size = new System.Drawing.Size( 81, 23 );
            this.iBT_Done.TabIndex = 1;
            this.iBT_Done.Text = "Done";
            this.iBT_Done.UseVisualStyleBackColor = true;
            this.iBT_Done.Click += new System.EventHandler( this.iBT_Done_Click );
            // 
            // iBT_EnableAll
            // 
            this.iBT_EnableAll.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iBT_EnableAll.Location = new System.Drawing.Point( 601, 12 );
            this.iBT_EnableAll.Name = "iBT_EnableAll";
            this.iBT_EnableAll.Size = new System.Drawing.Size( 81, 23 );
            this.iBT_EnableAll.TabIndex = 1;
            this.iBT_EnableAll.Text = "Enable All";
            this.iBT_EnableAll.UseVisualStyleBackColor = true;
            this.iBT_EnableAll.Click += new System.EventHandler( this.iBT_EnableAll_Click );
            // 
            // iBT_EnableNone
            // 
            this.iBT_EnableNone.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iBT_EnableNone.Location = new System.Drawing.Point( 601, 41 );
            this.iBT_EnableNone.Name = "iBT_EnableNone";
            this.iBT_EnableNone.Size = new System.Drawing.Size( 81, 23 );
            this.iBT_EnableNone.TabIndex = 1;
            this.iBT_EnableNone.Text = "Enable None";
            this.iBT_EnableNone.UseVisualStyleBackColor = true;
            this.iBT_EnableNone.Click += new System.EventHandler( this.iBT_EnableNone_Click );
            // 
            // HeapRendererFilterConfigDialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size( 689, 249 );
            this.ControlBox = false;
            this.Controls.Add( this.iBT_EnableNone );
            this.Controls.Add( this.iBT_EnableAll );
            this.Controls.Add( this.iBT_Done );
            this.Controls.Add( this.iFilterControl );
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "HeapRendererFilterConfigDialog";
            this.Text = "Configure Filters...";
            this.Load += new System.EventHandler( this.HeapRendererFilterConfigDialog_Load );
            this.ResumeLayout( false );

        }

        #endregion

        private HeapCtrlLib.Controls.HeapFilteringControl iFilterControl;
        private System.Windows.Forms.Button iBT_Done;
        private System.Windows.Forms.Button iBT_EnableAll;
        private System.Windows.Forms.Button iBT_EnableNone;
    }
}