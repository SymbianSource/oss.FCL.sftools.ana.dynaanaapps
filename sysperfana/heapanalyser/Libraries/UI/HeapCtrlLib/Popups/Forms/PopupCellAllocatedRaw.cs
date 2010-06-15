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
using System.Runtime.InteropServices;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Statistics;
using SymbianUtils.RawItems;

namespace HeapCtrlLib.Popups.Forms
{
	public class PopupCellAllocatedRaw : PopupBase
	{
        #region Windows Form Designer data members
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.Label iLbl_Values1;
        private System.Windows.Forms.Label iLbl_Values2;
        private System.Windows.Forms.Label iLbl_Values3;
        private System.Windows.Forms.Label iLbl_Values4;
        private System.Windows.Forms.Label iLbl_Chars1;
        private System.Windows.Forms.Label iLbl_Chars2;
        private System.Windows.Forms.Label iLbl_Chars3;
        private System.Windows.Forms.Label iLbl_Chars4;
        #endregion

        #region Constructors & destructor
        public PopupCellAllocatedRaw()
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
            this.iLbl_Values1 = new System.Windows.Forms.Label();
            this.iLbl_Values2 = new System.Windows.Forms.Label();
            this.iLbl_Values3 = new System.Windows.Forms.Label();
            this.iLbl_Values4 = new System.Windows.Forms.Label();
            this.iLbl_Chars1 = new System.Windows.Forms.Label();
            this.iLbl_Chars2 = new System.Windows.Forms.Label();
            this.iLbl_Chars3 = new System.Windows.Forms.Label();
            this.iLbl_Chars4 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iLbl_Values1
            // 
            this.iLbl_Values1.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Values1.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Values1.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Values1.Location = new System.Drawing.Point(5, 28);
            this.iLbl_Values1.Name = "iLbl_Values1";
            this.iLbl_Values1.Size = new System.Drawing.Size(243, 16);
            this.iLbl_Values1.TabIndex = 1;
            this.iLbl_Values1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Values2
            // 
            this.iLbl_Values2.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Values2.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Values2.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Values2.Location = new System.Drawing.Point(5, 44);
            this.iLbl_Values2.Name = "iLbl_Values2";
            this.iLbl_Values2.Size = new System.Drawing.Size(243, 16);
            this.iLbl_Values2.TabIndex = 1;
            this.iLbl_Values2.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Values3
            // 
            this.iLbl_Values3.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Values3.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Values3.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Values3.Location = new System.Drawing.Point(5, 60);
            this.iLbl_Values3.Name = "iLbl_Values3";
            this.iLbl_Values3.Size = new System.Drawing.Size(243, 16);
            this.iLbl_Values3.TabIndex = 1;
            this.iLbl_Values3.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Values4
            // 
            this.iLbl_Values4.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Values4.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Values4.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Values4.Location = new System.Drawing.Point(5, 76);
            this.iLbl_Values4.Name = "iLbl_Values4";
            this.iLbl_Values4.Size = new System.Drawing.Size(243, 16);
            this.iLbl_Values4.TabIndex = 1;
            this.iLbl_Values4.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Chars1
            // 
            this.iLbl_Chars1.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Chars1.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Chars1.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Chars1.Location = new System.Drawing.Point(264, 28);
            this.iLbl_Chars1.Name = "iLbl_Chars1";
            this.iLbl_Chars1.Size = new System.Drawing.Size(120, 16);
            this.iLbl_Chars1.TabIndex = 2;
            this.iLbl_Chars1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Chars2
            // 
            this.iLbl_Chars2.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Chars2.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Chars2.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Chars2.Location = new System.Drawing.Point(264, 44);
            this.iLbl_Chars2.Name = "iLbl_Chars2";
            this.iLbl_Chars2.Size = new System.Drawing.Size(120, 16);
            this.iLbl_Chars2.TabIndex = 2;
            this.iLbl_Chars2.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Chars3
            // 
            this.iLbl_Chars3.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Chars3.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Chars3.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Chars3.Location = new System.Drawing.Point(264, 60);
            this.iLbl_Chars3.Name = "iLbl_Chars3";
            this.iLbl_Chars3.Size = new System.Drawing.Size(120, 16);
            this.iLbl_Chars3.TabIndex = 2;
            this.iLbl_Chars3.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Chars4
            // 
            this.iLbl_Chars4.BackColor = System.Drawing.Color.Transparent;
            this.iLbl_Chars4.Font = new System.Drawing.Font("Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.iLbl_Chars4.ForeColor = System.Drawing.Color.Black;
            this.iLbl_Chars4.Location = new System.Drawing.Point(264, 76);
            this.iLbl_Chars4.Name = "iLbl_Chars4";
            this.iLbl_Chars4.Size = new System.Drawing.Size(120, 16);
            this.iLbl_Chars4.TabIndex = 2;
            this.iLbl_Chars4.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // PopupCellAllocatedRaw
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size(392, 120);
            this.Controls.Add(this.iLbl_Chars1);
            this.Controls.Add(this.iLbl_Values1);
            this.Controls.Add(this.iLbl_Values2);
            this.Controls.Add(this.iLbl_Values3);
            this.Controls.Add(this.iLbl_Values4);
            this.Controls.Add(this.iLbl_Chars2);
            this.Controls.Add(this.iLbl_Chars3);
            this.Controls.Add(this.iLbl_Chars4);
            this.Name = "PopupCellAllocatedRaw";
            this.Title = "Allocated Cell (Unknown)";
            this.Controls.SetChildIndex(this.iLbl_Chars4, 0);
            this.Controls.SetChildIndex(this.iLbl_Chars3, 0);
            this.Controls.SetChildIndex(this.iLbl_Chars2, 0);
            this.Controls.SetChildIndex(this.iLbl_Values4, 0);
            this.Controls.SetChildIndex(this.iLbl_Values3, 0);
            this.Controls.SetChildIndex(this.iLbl_Values2, 0);
            this.Controls.SetChildIndex(this.iLbl_Values1, 0);
            this.Controls.SetChildIndex(this.iLbl_Chars1, 0);
            this.ResumeLayout(false);

        }
		#endregion

        #region Internal constants
        private const string KLabelValuesPrefix = "iLbl_Values";
        private const string KLabelCharsPrefix = "iLbl_Chars";
        private const int KNumberOfLabelRows = 4;
        #endregion

        #region From PopupBase
        public override void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats )
        {
            System.Diagnostics.Debug.Assert( aCells.Count == 1 );
            //
            try
            {
                HeapCell cell = aCells[ 0 ];
                System.Diagnostics.Debug.Assert( cell.Type == HeapCell.TType.EAllocated );

                int descriptorLength = 0;
                if ( cell.IsDescriptor )
                {
                    descriptorLength = cell.DescriptorLength;

                    string title = "[ASCII] Des. Len: [";
                    if ( cell.IsDescriptorUnicode )
                    {
                        title = "[UNICD] Des. Len: [";
                    }
                    //
                    this.Title = title + descriptorLength.ToString( "d6" ) + "]";
                }

                // Data values & characterised data
                int labelNumber = 0;
                int itemIndex = 0;
                foreach( RawItem item in cell )
                {
                    // Build the names of the labels we will adjust
                    labelNumber = ( itemIndex / KNumberOfLabelRows ) + 1;
                    string labelNameValues = KLabelValuesPrefix + labelNumber.ToString("d1");
                    string labelNameChars = KLabelCharsPrefix + labelNumber.ToString("d1");

                    // Get the label object by name
                    System.Windows.Forms.Label labelValues = LabelByName( labelNameValues );
                    System.Windows.Forms.Label labelChars = LabelByName( labelNameChars );

                    // Reset contents if first time we have populated the label
                    if  ( labelValues == null || labelChars == null )
                    {
                        break;
                    }
                    else if  ( ( itemIndex % KNumberOfLabelRows == 0 ) || itemIndex == 0 )
                    {
                        labelValues.Text = string.Empty;
                        labelChars.Text = string.Empty;
                        labelValues.Visible = true;
                        labelChars.Visible = true;
                    }

                    // Add new data to the label
                    labelValues.Text += item.Data.ToString("x8") + " ";

                    if  ( cell.IsDescriptor )
                    {
                        labelChars.Text += item.OriginalCharacterisedData;
                    }
                    else
                    {
                        labelChars.Text += item.CharacterisedData;
                    }
                    
                    // Next item
                    ++itemIndex;
                }

                // Reduce height of form based upon usage of labels
                int unusedLabelHeight = iLbl_Values1.Height * ( KNumberOfLabelRows - Math.Min( KNumberOfLabelRows, labelNumber ) );
                int formHeight = this.Height;
                int newFormHeight = formHeight - unusedLabelHeight;

                // Hide unused items
                for( ++labelNumber; labelNumber<=KNumberOfLabelRows; labelNumber++ )
                {
                    // Build the names of the labels we will adjust
                    string labelNameValues = KLabelValuesPrefix + labelNumber.ToString("d1");
                    string labelNameChars = KLabelCharsPrefix + labelNumber.ToString("d1");

                    System.Windows.Forms.Label labelValues = LabelByName( labelNameValues );
                    labelValues.Visible = false;
                    System.Windows.Forms.Label labelChars = LabelByName( labelNameChars );
                    labelChars.Visible = false;
                }

                // Set final height
                base.Height = newFormHeight;
            }
            finally
            {
                base.PrepareContent( aCells, aStats );
            }
        }
        #endregion

        #region Event handlers
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
