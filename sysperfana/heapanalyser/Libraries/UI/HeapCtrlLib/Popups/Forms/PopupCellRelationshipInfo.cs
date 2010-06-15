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
using System.Text;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Statistics;
using HeapLib.Statistics.Tracking.Base;
using SymbianUtils.RawItems;

namespace HeapCtrlLib.Popups.Forms
{
	public class PopupCellRelationshipInfo : PopupBase
	{
        #region Windows Form Designer data members
        private System.ComponentModel.IContainer components = null;
        #endregion

        #region Constructors & destructor
        public PopupCellRelationshipInfo()
		{
			InitializeComponent();
            //
            this.SetStyle( ControlStyles.UserPaint, true );
            this.SetStyle( ControlStyles.DoubleBuffer, true );
            this.SetStyle( ControlStyles.AllPaintingInWmPaint, true );
            this.SetStyle( ControlStyles.ResizeRedraw, true );
            this.SetStyle( ControlStyles.Selectable, false );
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

		#region Windows Form Designer generated code
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
            this.iLbl_Description.Location = new System.Drawing.Point( 8, 32 );
            this.iLbl_Description.Name = "iLbl_Description";
            this.iLbl_Description.Size = new System.Drawing.Size( 344, 129 );
            this.iLbl_Description.TabIndex = 4;
            // 
            // PopupCellAllocatedSymbols
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 13 );
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size( 360, 193 );
            this.Controls.Add( this.iLbl_Description );
            this.Name = "PopupCellRelationshipInfo";
            this.Title = "Allocated Cell (23 instances)";
            this.Layout += new System.Windows.Forms.LayoutEventHandler( this.PopupCellAllocatedRaw_Layout );
            this.Controls.SetChildIndex( this.iLbl_Description, 0 );
            this.ResumeLayout( false );

        }
		#endregion

        #region Internal constants
        private const string KLabelValuesPrefix = "iLbl_Values";
        private const string KLabelCharsPrefix = "iLbl_Chars";
        private System.Windows.Forms.Label iLbl_Description;
        private const int KNumberOfLabelRows = 4;
        #endregion

        #region From PopupBase
        public override void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats, RawItem aRawItem )
        {
            try
            {
                System.Diagnostics.Debug.Assert( aCells.Count == 1 );
                System.Diagnostics.Debug.Assert( aRawItem.Tag != null && aRawItem.Tag is RelationshipInfo );
                HeapCell cell = aCells[ 0 ];
                RelationshipManager relMgr = cell.RelationshipManager;
                RelationshipInfo relInfo = (RelationshipInfo) aRawItem.Tag;
                //
                StringBuilder msg = new StringBuilder();
                //
                msg.AppendFormat( "This is an embedded reference to the cell at address 0x{0}", relInfo.ToCell.Address.ToString("x8") );
                if ( relInfo.ToCell.Symbol != null )
                {
                    msg.AppendFormat( ", which is an instance of a {0} object.", relInfo.ToCell.SymbolString );
                }
                msg.Append( System.Environment.NewLine + System.Environment.NewLine );
                //
                msg.AppendFormat( "In total, this cell references {0} other cell(s), and is referenced by {1} cell(s)", relMgr.EmbeddedReferencesTo.Count, relMgr.ReferencedBy.Count );
                msg.Append( System.Environment.NewLine + System.Environment.NewLine );
                // 
                iLbl_Description.Text = msg.ToString();
            }
            finally
            {
                base.PrepareContent( aCells, aStats, aRawItem );
            }
        }
        #endregion

        #region Event handlers
        private void PopupCellAllocatedRaw_Layout( object sender, System.Windows.Forms.LayoutEventArgs e )
        {
            iLbl_Description.Bounds = base.ClientRect;
        }
        #endregion

        #region Internal methods
        private Label LabelByName( string aName )
        {
            Label ret = null;
            //
            foreach( Control ctrl in Controls )
            {
                if  ( ctrl is Label && ctrl.Name == aName )
                {
                    ret = (Label) ctrl;
                    break;
                }
            }
            //
            return ret;
        }
        #endregion

        #region Data members
        #endregion
    }
}
