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
using System.Collections;
using System.ComponentModel;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;

namespace HeapCtrlLib.Popups.Forms
{
	public class PopupCellFree : PopupBase
	{
        #region Windows form designer variables
        private System.Windows.Forms.Label iLbl_Description;
		private System.ComponentModel.IContainer components = null;
        #endregion

        #region Constructors & destructor
		public PopupCellFree()
		{
			InitializeComponent();
		}

		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}
        #endregion

		#region Designer generated code
		// <summary>
		// Required method for Designer support - do not modify
		// the contents of this method with the code editor.
		// </summary>
		private void InitializeComponent()
		{
            this.iLbl_Description = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iLbl_Description
            // 
            this.iLbl_Description.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Description.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Description.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Description.Location = new System.Drawing.Point( 6, 30 );
            this.iLbl_Description.Name = "iLbl_Description";
            this.iLbl_Description.Size = new System.Drawing.Size( 355, 105 );
            this.iLbl_Description.TabIndex = 1;
            // 
            // PopupCellFree
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.ClientSize = new System.Drawing.Size( 367, 167 );
            this.Controls.Add( this.iLbl_Description );
            this.Name = "PopupCellFree";
            this.Title = "Free Cell";
            this.Layout += new System.Windows.Forms.LayoutEventHandler( this.PopupCellFree_Layout );
            this.Controls.SetChildIndex( this.iLbl_Description, 0 );
            this.ResumeLayout( false );

        }
		#endregion

        #region From PopupBase
        public override void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats )
        {
            try
            {
                HeapCell cell = aCells[ 0 ];
                System.Diagnostics.Debug.Assert( cell.Type == HeapCell.TType.EFree );
                //
                StringBuilder msg = new StringBuilder();
                //
                msg.AppendFormat( "This free cell has a {0} byte header and a is occupying a total of {1} byte(s) of heap space.", cell.HeaderSize, cell.Length );
                msg.Append( System.Environment.NewLine + System.Environment.NewLine );
                //
                msg.AppendFormat( "In total, this heap contains {0} bytes of free space.", aStats.StatsFree.TypeSize );
                msg.Append( System.Environment.NewLine + System.Environment.NewLine );
                //
                if ( cell.Symbol != null )
                {
                    // Free cells can contain symbols if we attempt to decode them...
                    msg.AppendFormat( "The free cell may have originally been of type \"{0}.\"", cell.SymbolString );
                    msg.Append( System.Environment.NewLine + System.Environment.NewLine );
                }
                // 
                iLbl_Description.Text = msg.ToString();
            }
            finally
            {
                base.PrepareContent( aCells, aStats );
            }
        }
        #endregion

        #region Event handlers
        private void PopupCellFree_Layout(object sender, System.Windows.Forms.LayoutEventArgs e)
        {
            iLbl_Description.Bounds = base.ClientRect;
        }
        #endregion
	}
}

