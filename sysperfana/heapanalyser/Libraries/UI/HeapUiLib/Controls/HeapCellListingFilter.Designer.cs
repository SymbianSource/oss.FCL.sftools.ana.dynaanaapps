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

namespace HeapUiLib.Controls
{
    partial class HeapCellListingFilter
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

        #region Component Designer generated code

        // <summary> 
        // Required method for Designer support - do not modify 
        // the contents of this method with the code editor.
        // </summary>
        private void InitializeComponent()
        {
            this.iCombo_FilterList = new System.Windows.Forms.ComboBox();
            this.label5 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iCombo_FilterList
            // 
            this.iCombo_FilterList.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iCombo_FilterList.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.iCombo_FilterList.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iCombo_FilterList.FormattingEnabled = true;
            this.iCombo_FilterList.Location = new System.Drawing.Point( 50, 1 );
            this.iCombo_FilterList.Margin = new System.Windows.Forms.Padding( 0 );
            this.iCombo_FilterList.MaxDropDownItems = 50;
            this.iCombo_FilterList.Name = "iCombo_FilterList";
            this.iCombo_FilterList.Size = new System.Drawing.Size( 559, 19 );
            this.iCombo_FilterList.TabIndex = 3;
            this.iCombo_FilterList.SelectedIndexChanged += new System.EventHandler( this.iCombo_FilterList_SelectedIndexChanged );
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.label5.Location = new System.Drawing.Point( 0, 4 );
            this.label5.Margin = new System.Windows.Forms.Padding( 0 );
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size( 35, 13 );
            this.label5.TabIndex = 2;
            this.label5.Text = "Filter:";
            this.label5.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // HeapCellListingFilter
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iCombo_FilterList );
            this.Controls.Add( this.label5 );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Margin = new System.Windows.Forms.Padding( 0 );
            this.MinimumSize = new System.Drawing.Size( 0, 20 );
            this.Name = "HeapCellListingFilter";
            this.Size = new System.Drawing.Size( 609, 20 );
            this.ResumeLayout( false );
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ComboBox iCombo_FilterList;
        private System.Windows.Forms.Label label5;
    }
}
