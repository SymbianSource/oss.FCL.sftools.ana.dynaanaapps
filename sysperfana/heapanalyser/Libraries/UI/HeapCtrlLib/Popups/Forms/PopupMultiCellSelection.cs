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
	public class PopupMultiCellSelection : PopupBase
	{
        #region Windows form designer variables
        private System.Windows.Forms.Label iLbl_Description;
		private System.ComponentModel.IContainer components = null;
        #endregion

        #region Constructors & destructor
		public PopupMultiCellSelection()
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
		private void InitializeComponent()
		{
            this.iLbl_Description = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iLbl_Description
            // 
            this.iLbl_Description.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
                | System.Windows.Forms.AnchorStyles.Left) 
                | System.Windows.Forms.AnchorStyles.Right)));
            this.iLbl_Description.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Description.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Description.Location = new System.Drawing.Point(6, 30);
            this.iLbl_Description.Name = "iLbl_Description";
            this.iLbl_Description.Size = new System.Drawing.Size(372, 130);
            this.iLbl_Description.TabIndex = 1;
            // 
            // PopupMultiCellSelection
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.ClientSize = new System.Drawing.Size(384, 192);
            this.Controls.Add(this.iLbl_Description);
            this.Name = "PopupMultiCellSelection";
            this.Title = "2 Cells Selected";
            this.Layout += new System.Windows.Forms.LayoutEventHandler(this.PopupCellFree_Layout);
            this.Controls.SetChildIndex(this.iLbl_Description, 0);
            this.ResumeLayout(false);

        }
		#endregion

        #region From PopupBase
        public override void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats )
        {
            try
            {
                Title = aCells.Count + " Cells Selected";

                StringBuilder msg = new StringBuilder();
                //
                if  ( aCells.Statistics.StatsAllocated.TypeCount > 0 )
                {
                    msg.AppendFormat( "Selection contains {0} allocated cell{1}", aCells.Statistics.StatsAllocated.TypeCount, (aCells.Statistics.StatsAllocated.TypeCount == 1) ? string.Empty : "s" );
                    if  ( aCells.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount > 0 )
                    {
                        msg.AppendFormat( ", {0} descriptor{1}", aCells.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount, ( aCells.Statistics.StatsAllocated.TrackerDescriptors.AssociatedCellCount == 1 ) ? string.Empty : "s" );
                    }

                    if  ( aCells.Statistics.StatsAllocated.TrackerSymbols.Count > 0 )
                    {
                        msg.AppendFormat( " and {0} symbol instance{1}", aCells.Statistics.StatsAllocated.TrackerSymbols.Count, (aCells.Statistics.StatsAllocated.TrackerSymbols.Count == 1) ? string.Empty : "s" );
                        msg.AppendFormat( " from {0} different object{1}", aCells.Statistics.StatsAllocated.TrackerObjects.Count, (aCells.Statistics.StatsAllocated.TrackerObjects.Count == 1) ? string.Empty : "s" );
                    }
                    msg.Append( "." );
                    msg.Append( System.Environment.NewLine + System.Environment.NewLine );

                    if  ( aCells.Statistics.StatsAllocated.CellSmallest != null )
                    {
                        msg.AppendFormat( "The smallest allocated cell is {0} bytes long. ", aCells.Statistics.StatsAllocated.CellSmallest.Length );
                    }
                    if  ( aCells.Statistics.StatsAllocated.CellLargest != null && ( aCells.Statistics.StatsAllocated.CellSmallest.Address != aCells.Statistics.StatsAllocated.CellLargest.Address ) )
                    {
                        msg.AppendFormat( "The largest allocated cell is {0} bytes long. ", aCells.Statistics.StatsAllocated.CellLargest.Length );
                    }
 
                    msg.Append( System.Environment.NewLine + System.Environment.NewLine );

                    
                }
                if  ( aCells.Statistics.StatsFree.TypeCount > 0 )
                {
                    msg.AppendFormat( "Selection contains {0} free cell{1}", aCells.Statistics.StatsFree.TypeCount, (aCells.Statistics.StatsFree.TypeCount == 1) ? string.Empty : "s" );
                    msg.Append( "." );
                    msg.Append( System.Environment.NewLine + System.Environment.NewLine );

                    if  ( aCells.Statistics.StatsFree.CellSmallest != null )
                    {
                        msg.AppendFormat( "The smallest free cell is {0} bytes long. ", aCells.Statistics.StatsFree.CellSmallest.Length );
                    }
                    if  ( aCells.Statistics.StatsFree.CellLargest != null && ( aCells.Statistics.StatsFree.CellSmallest.Address != aCells.Statistics.StatsFree.CellLargest.Address ) )
                    {
                        msg.AppendFormat( "The largest free cell is {0} bytes long. ", aCells.Statistics.StatsFree.CellLargest.Length );
                    }
                }

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

