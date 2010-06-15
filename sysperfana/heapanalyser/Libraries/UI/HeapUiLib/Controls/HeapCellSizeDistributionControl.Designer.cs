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
    partial class HeapCellSizeDistributionControl
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
            XPTable.Models.DataSourceColumnBinder dataSourceColumnBinder1 = new XPTable.Models.DataSourceColumnBinder();
            this.iTable = new XPTable.Models.Table();
            this.iColumnModel = new XPTable.Models.ColumnModel();
            this.iCol_Size = new XPTable.Models.TextColumn();
            this.iCol_Count = new XPTable.Models.TextColumn();
            this.iCol_Total = new XPTable.Models.TextColumn();
            this.iCol_PercentageOfType = new XPTable.Models.TextColumn();
            this.iCol_PercentageOfHeap = new XPTable.Models.TextColumn();
            this.iTableModel = new XPTable.Models.TableModel();
            this.iGP_Show = new System.Windows.Forms.GroupBox();
            this.iRB_Free = new System.Windows.Forms.RadioButton();
            this.iRB_Allocated = new System.Windows.Forms.RadioButton();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).BeginInit();
            this.iGP_Show.SuspendLayout();
            this.SuspendLayout();
            // 
            // iTable
            // 
            this.iTable.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iTable.BorderColor = System.Drawing.Color.Black;
            this.iTable.ColumnModel = this.iColumnModel;
            this.iTable.DataMember = null;
            this.iTable.DataSourceColumnBinder = dataSourceColumnBinder1;
            this.iTable.EnableToolTips = true;
            this.iTable.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iTable.FullRowSelect = true;
            this.iTable.HeaderFont = new System.Drawing.Font( "Tahoma", 6.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.Location = new System.Drawing.Point( 0, 52 );
            this.iTable.Name = "iTable";
            this.iTable.NoItemsText = "No Data";
            this.iTable.NoItemsTextColor = System.Drawing.SystemColors.ControlText;
            this.iTable.NoItemsTextFont = new System.Drawing.Font( "Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTable.Size = new System.Drawing.Size( 517, 130 );
            this.iTable.TabIndex = 0;
            this.iTable.TableModel = this.iTableModel;
            this.iTable.UnfocusedBorderColor = System.Drawing.Color.Black;
            this.iTable.PrepareForSort += new XPTable.Events.SortEventHandler( this.iTable_PrepareForSort );
            // 
            // iColumnModel
            // 
            this.iColumnModel.Columns.AddRange( new XPTable.Models.Column[] {
            this.iCol_Size,
            this.iCol_Count,
            this.iCol_Total,
            this.iCol_PercentageOfType,
            this.iCol_PercentageOfHeap} );
            // 
            // iCol_Size
            // 
            this.iCol_Size.ContentWidth = 26;
            this.iCol_Size.Text = "Size";
            this.iCol_Size.Width = 120;
            // 
            // iCol_Count
            // 
            this.iCol_Count.ContentWidth = 80;
            this.iCol_Count.Text = "Instance Count";
            this.iCol_Count.Width = 90;
            // 
            // iCol_Total
            // 
            this.iCol_Total.ContentWidth = 29;
            this.iCol_Total.Text = "Total";
            this.iCol_Total.Width = 90;
            // 
            // iCol_PercentageOfType
            // 
            this.iCol_PercentageOfType.ContentWidth = 62;
            this.iCol_PercentageOfType.Text = "Percentage";
            // 
            // iCol_PercentageOfHeap
            // 
            this.iCol_PercentageOfHeap.ContentWidth = 100;
            this.iCol_PercentageOfHeap.Text = "Percentage (Heap)";
            this.iCol_PercentageOfHeap.Width = 110;
            // 
            // iGP_Show
            // 
            this.iGP_Show.Controls.Add( this.iRB_Free );
            this.iGP_Show.Controls.Add( this.iRB_Allocated );
            this.iGP_Show.Location = new System.Drawing.Point( 0, 0 );
            this.iGP_Show.Name = "iGP_Show";
            this.iGP_Show.Size = new System.Drawing.Size( 243, 46 );
            this.iGP_Show.TabIndex = 1;
            this.iGP_Show.TabStop = false;
            this.iGP_Show.Text = "Show...";
            // 
            // iRB_Free
            // 
            this.iRB_Free.AutoSize = true;
            this.iRB_Free.Location = new System.Drawing.Point( 139, 20 );
            this.iRB_Free.Name = "iRB_Free";
            this.iRB_Free.Size = new System.Drawing.Size( 70, 17 );
            this.iRB_Free.TabIndex = 0;
            this.iRB_Free.Text = "Free cells";
            this.iRB_Free.UseVisualStyleBackColor = true;
            this.iRB_Free.CheckedChanged += new System.EventHandler( this.iRB_CheckedChanged );
            // 
            // iRB_Allocated
            // 
            this.iRB_Allocated.AutoSize = true;
            this.iRB_Allocated.Checked = true;
            this.iRB_Allocated.Location = new System.Drawing.Point( 22, 20 );
            this.iRB_Allocated.Name = "iRB_Allocated";
            this.iRB_Allocated.Size = new System.Drawing.Size( 92, 17 );
            this.iRB_Allocated.TabIndex = 0;
            this.iRB_Allocated.TabStop = true;
            this.iRB_Allocated.Text = "Allocated cells";
            this.iRB_Allocated.UseVisualStyleBackColor = true;
            this.iRB_Allocated.CheckedChanged += new System.EventHandler( this.iRB_CheckedChanged );
            // 
            // HeapCellSizeDistributionControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iGP_Show );
            this.Controls.Add( this.iTable );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Name = "HeapCellSizeDistributionControl";
            this.Size = new System.Drawing.Size( 517, 182 );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).EndInit();
            this.iGP_Show.ResumeLayout( false );
            this.iGP_Show.PerformLayout();
            this.ResumeLayout( false );

        }

        #endregion

        private XPTable.Models.Table iTable;
        private XPTable.Models.ColumnModel iColumnModel;
        private XPTable.Models.TableModel iTableModel;
        private XPTable.Models.TextColumn iCol_Size;
        private XPTable.Models.TextColumn iCol_Count;
        private XPTable.Models.TextColumn iCol_PercentageOfType;
        private XPTable.Models.TextColumn iCol_PercentageOfHeap;
        private XPTable.Models.TextColumn iCol_Total;
        private System.Windows.Forms.GroupBox iGP_Show;
        private System.Windows.Forms.RadioButton iRB_Free;
        private System.Windows.Forms.RadioButton iRB_Allocated;
    }
}
