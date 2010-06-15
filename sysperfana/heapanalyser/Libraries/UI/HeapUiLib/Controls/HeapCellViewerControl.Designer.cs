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
    partial class HeapCellViewerControl
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
            this.components = new System.ComponentModel.Container();
            XPTable.Models.DataSourceColumnBinder dataSourceColumnBinder1 = new XPTable.Models.DataSourceColumnBinder();
            this.iTL_Bottom = new System.Windows.Forms.TableLayoutPanel();
            this.iButton_PageUp = new System.Windows.Forms.Button();
            this.iButton_PageDown = new System.Windows.Forms.Button();
            this.iTable_RawItems = new XPTable.Models.Table();
            this.iColumnModel = new XPTable.Models.ColumnModel();
            this.iCol_Address = new XPTable.Models.TextColumn();
            this.iCol_RawData = new XPTable.Models.TextColumn();
            this.iCol_Interpreted = new XPTable.Models.TextColumn();
            this.iCol_CharacterisedData = new XPTable.Models.TextColumn();
            this.iTableModel = new XPTable.Models.TableModel();
            this.iTimerRefresh = new System.Windows.Forms.Timer( this.components );
            this.iTL_Bottom.SuspendLayout();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable_RawItems ) ).BeginInit();
            this.SuspendLayout();
            // 
            // iTL_Bottom
            // 
            this.iTL_Bottom.ColumnCount = 2;
            this.iTL_Bottom.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTL_Bottom.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Absolute, 50F ) );
            this.iTL_Bottom.Controls.Add( this.iButton_PageUp, 1, 0 );
            this.iTL_Bottom.Controls.Add( this.iButton_PageDown, 1, 2 );
            this.iTL_Bottom.Controls.Add( this.iTable_RawItems, 0, 0 );
            this.iTL_Bottom.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTL_Bottom.Location = new System.Drawing.Point( 0, 0 );
            this.iTL_Bottom.Name = "iTL_Bottom";
            this.iTL_Bottom.RowCount = 3;
            this.iTL_Bottom.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 33.33333F ) );
            this.iTL_Bottom.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 33.33333F ) );
            this.iTL_Bottom.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 33.33333F ) );
            this.iTL_Bottom.Size = new System.Drawing.Size( 334, 120 );
            this.iTL_Bottom.TabIndex = 4;
            // 
            // iButton_PageUp
            // 
            this.iButton_PageUp.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iButton_PageUp.Location = new System.Drawing.Point( 287, 3 );
            this.iButton_PageUp.Name = "iButton_PageUp";
            this.iButton_PageUp.Size = new System.Drawing.Size( 44, 34 );
            this.iButton_PageUp.TabIndex = 13;
            this.iButton_PageUp.Text = "Page Up";
            this.iButton_PageUp.Click += new System.EventHandler( this.iButton_PageUp_Click );
            // 
            // iButton_PageDown
            // 
            this.iButton_PageDown.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iButton_PageDown.Location = new System.Drawing.Point( 287, 83 );
            this.iButton_PageDown.Name = "iButton_PageDown";
            this.iButton_PageDown.Size = new System.Drawing.Size( 44, 34 );
            this.iButton_PageDown.TabIndex = 15;
            this.iButton_PageDown.Text = "Page Down";
            this.iButton_PageDown.Click += new System.EventHandler( this.iButton_PageDown_Click );
            // 
            // iTable_RawItems
            // 
            this.iTable_RawItems.AlternatingRowColor = System.Drawing.Color.WhiteSmoke;
            this.iTable_RawItems.BorderColor = System.Drawing.Color.Black;
            this.iTable_RawItems.ColumnModel = this.iColumnModel;
            this.iTable_RawItems.DataMember = null;
            this.iTable_RawItems.DataSourceColumnBinder = dataSourceColumnBinder1;
            this.iTable_RawItems.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTable_RawItems.EnableToolTips = true;
            this.iTable_RawItems.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable_RawItems.GridLines = XPTable.Models.GridLines.Both;
            this.iTable_RawItems.HeaderFont = new System.Drawing.Font( "Tahoma", 6.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable_RawItems.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.Nonclickable;
            this.iTable_RawItems.Location = new System.Drawing.Point( 3, 3 );
            this.iTable_RawItems.Name = "iTable_RawItems";
            this.iTable_RawItems.NoItemsText = "Nothing Selected or No Data";
            this.iTable_RawItems.NoItemsTextColor = System.Drawing.SystemColors.ControlText;
            this.iTable_RawItems.NoItemsTextFont = new System.Drawing.Font( "Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTL_Bottom.SetRowSpan( this.iTable_RawItems, 3 );
            this.iTable_RawItems.Size = new System.Drawing.Size( 278, 114 );
            this.iTable_RawItems.TabIndex = 21;
            this.iTable_RawItems.TableModel = this.iTableModel;
            this.iTable_RawItems.UnfocusedBorderColor = System.Drawing.Color.Black;
            this.iTable_RawItems.CellDoubleClick += new XPTable.Events.CellMouseEventHandler( this.iTable_RawItems_CellDoubleClick );
            // 
            // iColumnModel
            // 
            this.iColumnModel.Columns.AddRange( new XPTable.Models.Column[] {
            this.iCol_Address,
            this.iCol_RawData,
            this.iCol_Interpreted,
            this.iCol_CharacterisedData} );
            // 
            // iCol_Address
            // 
            this.iCol_Address.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_Address.ContentWidth = 44;
            this.iCol_Address.Text = "Address";
            this.iCol_Address.Width = 70;
            // 
            // iCol_RawData
            // 
            this.iCol_RawData.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_RawData.ContentWidth = 52;
            this.iCol_RawData.Text = "Raw Data";
            this.iCol_RawData.Width = 70;
            // 
            // iCol_Interpreted
            // 
            this.iCol_Interpreted.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_Interpreted.ContentWidth = 61;
            this.iCol_Interpreted.Text = "Interpreted";
            this.iCol_Interpreted.Width = 70;
            // 
            // iCol_CharacterisedData
            // 
            this.iCol_CharacterisedData.Alignment = XPTable.Models.ColumnAlignment.Center;
            this.iCol_CharacterisedData.ContentWidth = 4;
            this.iCol_CharacterisedData.Text = " ";
            this.iCol_CharacterisedData.Width = 40;
            // 
            // iTimerRefresh
            // 
            this.iTimerRefresh.Interval = 1;
            this.iTimerRefresh.Tick += new System.EventHandler( this.iTimerRefresh_Tick );
            // 
            // HeapCellViewerControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iTL_Bottom );
            this.MinimumSize = new System.Drawing.Size( 334, 120 );
            this.Name = "HeapCellViewerControl";
            this.Size = new System.Drawing.Size( 334, 120 );
            this.iTL_Bottom.ResumeLayout( false );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable_RawItems ) ).EndInit();
            this.ResumeLayout( false );

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel iTL_Bottom;
        private System.Windows.Forms.Button iButton_PageUp;
        private System.Windows.Forms.Button iButton_PageDown;
        private XPTable.Models.Table iTable_RawItems;
        private XPTable.Models.ColumnModel iColumnModel;
        private XPTable.Models.TableModel iTableModel;
        private XPTable.Models.TextColumn iCol_Address;
        private XPTable.Models.TextColumn iCol_RawData;
        private XPTable.Models.TextColumn iCol_Interpreted;
        private XPTable.Models.TextColumn iCol_CharacterisedData;
        private System.Windows.Forms.Timer iTimerRefresh;
    }
}
