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

﻿namespace HeapUiLib.Controls
{
    partial class HeapCellInfoControl
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
            System.Windows.Forms.ListViewItem listViewItem1 = new System.Windows.Forms.ListViewItem( new string[] {
            "Length",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem2 = new System.Windows.Forms.ListViewItem( new string[] {
            "Symbol",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem3 = new System.Windows.Forms.ListViewItem( new string[] {
            "Address",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem4 = new System.Windows.Forms.ListViewItem( new string[] {
            "Payload at",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem5 = new System.Windows.Forms.ListViewItem( new string[] {
            "Length",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem6 = new System.Windows.Forms.ListViewItem( new string[] {
            "Nesting level",
            ""}, -1 );
            System.Windows.Forms.ListViewItem listViewItem7 = new System.Windows.Forms.ListViewItem( new string[] {
            "Allocation #",
            ""}, -1 );
            this.iLVInfoCellPayload = new System.Windows.Forms.ListView();
            this.columnHeader5 = new System.Windows.Forms.ColumnHeader();
            this.columnHeader6 = new System.Windows.Forms.ColumnHeader();
            this.label1 = new System.Windows.Forms.Label();
            this.iLVInfoCellHeader = new System.Windows.Forms.ListView();
            this.columnHeader3 = new System.Windows.Forms.ColumnHeader();
            this.columnHeader4 = new System.Windows.Forms.ColumnHeader();
            this.label2 = new System.Windows.Forms.Label();
            this.iTLP = new System.Windows.Forms.TableLayoutPanel();
            this.iTLP.SuspendLayout();
            this.SuspendLayout();
            // 
            // iLVInfoCellPayload
            // 
            this.iLVInfoCellPayload.Columns.AddRange( new System.Windows.Forms.ColumnHeader[] {
            this.columnHeader5,
            this.columnHeader6} );
            this.iLVInfoCellPayload.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iLVInfoCellPayload.FullRowSelect = true;
            this.iLVInfoCellPayload.GridLines = true;
            this.iLVInfoCellPayload.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.None;
            this.iLVInfoCellPayload.Items.AddRange( new System.Windows.Forms.ListViewItem[] {
            listViewItem1,
            listViewItem2} );
            this.iLVInfoCellPayload.Location = new System.Drawing.Point( 72, 90 );
            this.iLVInfoCellPayload.MultiSelect = false;
            this.iLVInfoCellPayload.Name = "iLVInfoCellPayload";
            this.iLVInfoCellPayload.Size = new System.Drawing.Size( 325, 41 );
            this.iLVInfoCellPayload.TabIndex = 9;
            this.iLVInfoCellPayload.UseCompatibleStateImageBehavior = false;
            this.iLVInfoCellPayload.View = System.Windows.Forms.View.Details;
            // 
            // columnHeader5
            // 
            this.columnHeader5.Width = 70;
            // 
            // columnHeader6
            // 
            this.columnHeader6.Width = 237;
            // 
            // label1
            // 
            this.label1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label1.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.label1.Location = new System.Drawing.Point( 3, 0 );
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size( 63, 87 );
            this.label1.TabIndex = 8;
            this.label1.Text = "Header:";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // iLVInfoCellHeader
            // 
            this.iLVInfoCellHeader.Columns.AddRange( new System.Windows.Forms.ColumnHeader[] {
            this.columnHeader3,
            this.columnHeader4} );
            this.iLVInfoCellHeader.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iLVInfoCellHeader.FullRowSelect = true;
            this.iLVInfoCellHeader.GridLines = true;
            this.iLVInfoCellHeader.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.None;
            listViewItem5.Tag = "";
            this.iLVInfoCellHeader.Items.AddRange( new System.Windows.Forms.ListViewItem[] {
            listViewItem3,
            listViewItem4,
            listViewItem5,
            listViewItem6,
            listViewItem7} );
            this.iLVInfoCellHeader.Location = new System.Drawing.Point( 72, 3 );
            this.iLVInfoCellHeader.MultiSelect = false;
            this.iLVInfoCellHeader.Name = "iLVInfoCellHeader";
            this.iLVInfoCellHeader.Scrollable = false;
            this.iLVInfoCellHeader.Size = new System.Drawing.Size( 325, 81 );
            this.iLVInfoCellHeader.TabIndex = 6;
            this.iLVInfoCellHeader.UseCompatibleStateImageBehavior = false;
            this.iLVInfoCellHeader.View = System.Windows.Forms.View.Details;
            this.iLVInfoCellHeader.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iLVInfoCellHeader_KeyDown );
            // 
            // columnHeader3
            // 
            this.columnHeader3.Width = 70;
            // 
            // columnHeader4
            // 
            this.columnHeader4.Width = 237;
            // 
            // label2
            // 
            this.label2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label2.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.label2.Location = new System.Drawing.Point( 3, 87 );
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size( 63, 47 );
            this.label2.TabIndex = 7;
            this.label2.Text = "Contents:";
            this.label2.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // iTLP
            // 
            this.iTLP.ColumnCount = 2;
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Absolute, 69F ) );
            this.iTLP.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTLP.Controls.Add( this.iLVInfoCellPayload, 1, 1 );
            this.iTLP.Controls.Add( this.label1, 0, 0 );
            this.iTLP.Controls.Add( this.iLVInfoCellHeader, 1, 0 );
            this.iTLP.Controls.Add( this.label2, 0, 1 );
            this.iTLP.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTLP.Location = new System.Drawing.Point( 0, 0 );
            this.iTLP.Margin = new System.Windows.Forms.Padding( 5 );
            this.iTLP.Name = "iTLP";
            this.iTLP.RowCount = 2;
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 65.41354F ) );
            this.iTLP.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 34.58647F ) );
            this.iTLP.Size = new System.Drawing.Size( 400, 134 );
            this.iTLP.TabIndex = 10;
            // 
            // HeapCellInfoControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iTLP );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.MaximumSize = new System.Drawing.Size( 1024, 134 );
            this.MinimumSize = new System.Drawing.Size( 400, 134 );
            this.Name = "HeapCellInfoControl";
            this.Size = new System.Drawing.Size( 400, 134 );
            this.SizeChanged += new System.EventHandler( this.HeapCellInfoControl_SizeChanged );
            this.iTLP.ResumeLayout( false );
            this.ResumeLayout( false );

        }

        #endregion

        private System.Windows.Forms.ListView iLVInfoCellPayload;
        private System.Windows.Forms.ColumnHeader columnHeader5;
        private System.Windows.Forms.ColumnHeader columnHeader6;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.ListView iLVInfoCellHeader;
        private System.Windows.Forms.ColumnHeader columnHeader3;
        private System.Windows.Forms.ColumnHeader columnHeader4;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TableLayoutPanel iTLP;
    }
}
