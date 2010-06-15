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
using System.Collections.Generic;
using System.ComponentModel;
using System.Text;
using System.Windows.Forms;
using SymbianUtils.Settings;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapUiLib.Forms;

namespace HeapUiLib.SubForms
{
	public class HeapCellContentsForm : System.Windows.Forms.Form
	{
		#region Windows Form Designer Code
        private HeapUiLib.Controls.HeapCellViewerControl iContents;
		#endregion

		#region Constructors & destructors
        // <summary>
        // Construct a new cell contents form, but persist no settings associated with this
        // popup window, nor observe the main form for positional changes
        // </summary>
        // <param name="aMainForm"></param>
        public HeapCellContentsForm( HeapViewerForm aMainForm, HeapCell aFixedCell )
        {
            iMainForm = aMainForm;
            iMainForm.Closing += new CancelEventHandler( iMainForm_Closing );
            //
            iSettings = null;
            //
            InitializeComponent();
            
            // Show contents
            Cell = aFixedCell;

            // Title
            Text += " - 0x" + aFixedCell.Address.ToString( "x8" ) + " " + aFixedCell.SymbolString;
        }

        // <summary>
        // Create a cell contents form that follows the main form selection and also
        // persists position of the form when it closes.
        // </summary>
        // <param name="aMainForm"></param>
        // <param name="aSettings"></param>
        public HeapCellContentsForm( HeapViewerForm aMainForm, XmlSettings aSettings )
		{
			iMainForm = aMainForm;
			iMainForm.HeapCellSelectedObserver += new HeapUiLib.Forms.HeapViewerForm.HeapCellSelectedObserverHandler(iMainForm_HeapCellSelectedObserver);
			iMainForm.Closing += new CancelEventHandler(iMainForm_Closing);
            iTrackingMainForm = true;
			//
			iSettings = aSettings;
			//
			InitializeComponent();
		}

		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
                if ( iMainForm != null  )
                {
                    if ( iTrackingMainForm )
                    {
                        iMainForm.HeapCellSelectedObserver -= new HeapUiLib.Forms.HeapViewerForm.HeapCellSelectedObserverHandler( iMainForm_HeapCellSelectedObserver );
                    }
                    //
                    iMainForm.Closing -= new CancelEventHandler( iMainForm_Closing );
                }

			}
			base.Dispose( disposing );
		}
		#endregion

		#region Windows Form Designer generated code
		private void InitializeComponent()
		{
            HeapLib.Cells.HeapCell heapCell1 = new HeapLib.Cells.HeapCell();
            this.iContents = new HeapUiLib.Controls.HeapCellViewerControl();
            this.SuspendLayout();
            // 
            // iContents
            // 
            heapCell1.Address = ( (uint) ( 0u ) );
            heapCell1.Index = ( (uint) ( 0u ) );
            heapCell1.Symbol = null;
            heapCell1.Symbol2 = null;
            heapCell1.Symbol3 = null;
            heapCell1.Tag = null;
            heapCell1.Type = HeapLib.Cells.HeapCell.TType.EAllocated;
            this.iContents.Cell = heapCell1;
            this.iContents.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iContents.Location = new System.Drawing.Point( 0, 0 );
            this.iContents.MinimumSize = new System.Drawing.Size( 334, 120 );
            this.iContents.Name = "iContents";
            this.iContents.Size = new System.Drawing.Size( 355, 284 );
            this.iContents.TabIndex = 0;
            this.iContents.CellLinkDoubleClicked += new HeapUiLib.Controls.HeapCellViewerControl.OnCellLinkDoubleClicked( this.iContents_CellLinkDoubleClicked );
            // 
            // HeapCellContentsForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 14 );
            this.ClientSize = new System.Drawing.Size( 355, 284 );
            this.Controls.Add( this.iContents );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.MinimumSize = new System.Drawing.Size( 363, 308 );
            this.Name = "HeapCellContentsForm";
            this.Text = "Cell Viewer";
            this.TopMost = true;
            this.Load += new System.EventHandler( this.HeapViewerLinkedCellsForm_Load );
            this.Closing += new System.ComponentModel.CancelEventHandler( this.HeapViewerLinkedCellsForm_Closing );
            this.ResumeLayout( false );

		}
		#endregion
	
		#region API
		public HeapCell MainFormSelectedCell
		{
			get
			{
                HeapCell ret = Cell;
                //
                if ( iMainForm != null )
                {
                    ret = iMainForm.FocusedCell;
                }
                //
                return ret;
			}
		}

        public HeapCell Cell
        {
            get { return iContents.Cell; }
            set
            {
                iContents.Cell = value;
            }
        }
		#endregion

		#region Form loading & closing event handlers
		private void HeapViewerLinkedCellsForm_Load(object sender, System.EventArgs e)
		{
            if ( iSettings != null )
            {
                Point position = new Point();
                position.X = iSettings.Load( "HeapViewerCellViewerForm", "WindowPositionX", 0 );
                position.Y = iSettings.Load( "HeapViewerCellViewerForm", "WindowPositionY", 0 );
                Location = position;
            }

            iMainForm_HeapCellSelectedObserver( MainFormSelectedCell );
		}

		private void HeapViewerLinkedCellsForm_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
            if ( iSettings != null )
            {
                iSettings.Save( "HeapViewerCellViewerForm", "WindowPositionX", Location.X );
                iSettings.Save( "HeapViewerCellViewerForm", "WindowPositionY", Location.Y );
            }
		}
		#endregion

		#region Main Form event handlers
		private void iMainForm_HeapCellSelectedObserver( HeapCell aCell )
		{
            iContents.Cell = aCell;
        }

		private void iMainForm_Closing(object sender, CancelEventArgs e)
		{
			// Close ourselves when our parent closes.
			DialogResult = DialogResult.OK;
			Close();
		}
		#endregion

        #region Event handlers
        private void iContents_CellLinkDoubleClicked( HeapCell aCell )
        {
            if ( iMainForm != null )
            {
                iMainForm.FocusedCell = aCell;
            }
        }
        #endregion

        #region Data members
        private readonly HeapViewerForm iMainForm;
        private readonly XmlSettings iSettings;
        private bool iTrackingMainForm = false;
        #endregion
    }
}
