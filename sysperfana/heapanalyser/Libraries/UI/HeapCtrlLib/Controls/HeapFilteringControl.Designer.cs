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

namespace HeapCtrlLib.Controls
{
    partial class HeapFilteringControl
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
            this.iTableModel = new XPTable.Models.TableModel();
            this.iTable = new XPTable.Models.Table();
            this.iColumnModel = new XPTable.Models.ColumnModel();
            this.iCol_Enabled = new XPTable.Models.CheckBoxColumn();
            this.iCol_Colour = new XPTable.Models.ColorColumn();
            this.iCol_Entity = new XPTable.Models.TextColumn();
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).BeginInit();
            this.SuspendLayout();
            // 
            // iTable
            // 
            this.iTable.ColumnModel = this.iColumnModel;
            this.iTable.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTable.GridLines = XPTable.Models.GridLines.Both;
            this.iTable.Location = new System.Drawing.Point( 0, 0 );
            this.iTable.Name = "iTable";
            this.iTable.Size = new System.Drawing.Size( 625, 300 );
            this.iTable.TabIndex = 0;
            this.iTable.TableModel = this.iTableModel;
            this.iTable.CellCheckChanged += new XPTable.Events.CellCheckBoxEventHandler( this.iTable_CellCheckChanged );
            // 
            // iColumnModel
            // 
            this.iColumnModel.Columns.AddRange( new XPTable.Models.Column[] {
            this.iCol_Enabled,
            this.iCol_Colour,
            this.iCol_Entity} );
            // 
            // iCol_Enabled
            // 
            this.iCol_Enabled.Text = "Enabled";
            this.iCol_Enabled.Width = 60;
            // 
            // iCol_Colour
            // 
            this.iCol_Colour.Text = "Colour";
            this.iCol_Colour.Width = 140;
            // 
            // iCol_Entity
            // 
            this.iCol_Entity.Text = "Entity";
            this.iCol_Entity.Width = 350;
            // 
            // HeapFilteringControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF( 6F, 13F );
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add( this.iTable );
            this.Name = "HeapFilteringControl";
            this.Size = new System.Drawing.Size( 625, 300 );
            ( (System.ComponentModel.ISupportInitialize) ( this.iTable ) ).EndInit();
            this.ResumeLayout( false );

        }

        #endregion

        private XPTable.Models.TableModel iTableModel;
        private XPTable.Models.Table iTable;
        private XPTable.Models.ColumnModel iColumnModel;
        private XPTable.Models.CheckBoxColumn iCol_Enabled;
        private XPTable.Models.ColorColumn iCol_Colour;
        private XPTable.Models.TextColumn iCol_Entity;
    }
}
