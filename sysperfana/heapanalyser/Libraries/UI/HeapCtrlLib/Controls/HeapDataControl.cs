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
using System.Data;
using System.Windows.Forms;
using HeapLib.Cells;
using HeapLib.Reconstructor;
using HeapCtrlLib.Renderers;
using HeapCtrlLib.Types;
using HeapLib;
using HeapLib.Array;
using SymbianUtils.RawItems;

namespace HeapCtrlLib
{
    public class HeapDataControl : System.Windows.Forms.UserControl
    {
        #region Delegates & events
        public delegate void OnCellSelected( HeapCell aCell );
        public event OnCellSelected CellSelected;
        public delegate void OnCellDoubleClicked( HeapCell aCell );
        public event OnCellDoubleClicked CellDoubleClicked;
        public delegate void CellRightClickedHandler( HeapCell aCell, RawItem aRawItem, Point aScreenPos );
        public event CellRightClickedHandler CellRightClicked;
        #endregion

        #region Windows Form Designer code
        private System.ComponentModel.Container components = null;
        private HeapCtrlLib.Renderers.HeapDataRenderer iRenderer = null;
        private SymbianUtilsUi.Controls.ScrollBarWithoutKeyConsumption iSB_Vertical = null;
        #endregion

        #region Constructors & destructor
        public HeapDataControl()
        {
            InitializeComponent();
            //
            //			this.SetStyle(ControlStyles.DoubleBuffer, true);
            //			this.SetStyle(ControlStyles.AllPaintingInWmPaint, true);
            //			this.SetStyle(ControlStyles.UserPaint, true);
            this.SetStyle(ControlStyles.ResizeRedraw, true);
        }

        protected override void Dispose( bool disposing )
        {
            if( disposing )
            {
                if(components != null)
                {
                    components.Dispose();
                }
            }
            base.Dispose( disposing );
        }
        #endregion

        #region Component Designer generated code
        private void InitializeComponent()
        {
            this.iRenderer = new HeapDataRenderer();
            this.iSB_Vertical = new SymbianUtilsUi.Controls.ScrollBarWithoutKeyConsumption();
            this.SuspendLayout();
            // 
            // iSB_Vertical
            // 
            this.iSB_Vertical.Dock = System.Windows.Forms.DockStyle.Right;
            this.iSB_Vertical.Location = new System.Drawing.Point( 598, 2 );
            this.iSB_Vertical.Name = "iSB_Vertical";
            this.iSB_Vertical.Size = new System.Drawing.Size( 16, 156 );
            this.iSB_Vertical.TabIndex = 2;
            this.iSB_Vertical.Scroll += new System.Windows.Forms.ScrollEventHandler( this.iSB_Vertical_Scroll );
            // 
            // iRenderer
            // 
            this.iRenderer.Address = ( (uint) ( 0u ) );
            this.iRenderer.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iRenderer.FocusedCell = null;
            this.iRenderer.Location = new System.Drawing.Point( 3, 3 );
            this.iRenderer.Name = "iRenderer";
            this.iRenderer.Reconstructor = null;
            this.iRenderer.Size = new System.Drawing.Size( 596, 154 );
            this.iRenderer.TabIndex = 1;
            this.iRenderer.Zoom = HeapCtrlLib.Types.THeapCtrlZoom.EHeapCtrlZoomMedium;
            this.iRenderer.Enter += new System.EventHandler( this.iRenderer_Enter );
            this.iRenderer.AddressChanged += new HeapCtrlLib.Renderers.HeapDataRenderer.AddressChangeHandler( this.iRenderer_AddressChanged );
            this.iRenderer.Leave += new System.EventHandler( this.iRenderer_Leave );
            this.iRenderer.CellSelected += new HeapCtrlLib.Renderers.HeapDataRenderer.CellSelectionHandler( this.iRenderer_CellSelected );
            this.iRenderer.CellRightClicked += new HeapDataRenderer.CellRightClickedHandler( iRenderer_CellRightClicked );
            this.iRenderer.CellDoubleClicked += new HeapDataRenderer.CellDoubleClickHandler( iRenderer_CellDoubleClicked );
            // 
            // HeapDataControl
            // 
            this.Controls.Add( this.iSB_Vertical );
            this.Controls.Add( this.iRenderer );
            this.Name = "HeapDataControl";
            this.Padding = new System.Windows.Forms.Padding( 2 );
            this.Size = new System.Drawing.Size( 616, 160 );
            this.SizeChanged += new System.EventHandler( this.HeapDataControl_SizeChanged );
            this.ResumeLayout( false );
        }
        #endregion

        #region API
        public void SetupFilters()
        {
            iRenderer.SetupFilters();
        }

        public void BreadcrumbsChanged()
        {
            iRenderer.Invalidate();
        }
        #endregion

        #region Properties
        public THeapCtrlZoom Zoom
        {
            get { return iRenderer.Zoom; }
            set
            {
                iRenderer.Zoom = value;

                // Force repositioning to top of view.
                Reconstructor = Reconstructor;
            }
        }

        public THeapCtrlRenderingType Type
        {
            get { return iType; }
            set
            {
                if  ( iType != value )
                {
                    iType = value;
                    iRenderer.LoadTypeSet( iType );
                    iRenderer.Invalidate();
                }
            }
        }

        [Browsable(false)]
        internal Size CellBoxSize
        {
            get { return iRenderer.CellBoxSize; }
            set { iRenderer.CellBoxSize = value; }
        }

        [Browsable(false)]
        internal Size CellPadding
        {
            get { return iRenderer.CellPadding; }
            set { iRenderer.CellPadding = value; }
        }

        [Browsable(false)]
        public HeapReconstructor Reconstructor
        {
            get
            {
                return iReconstructor;
            }
            set
            {
                iReconstructor = value;
                iRenderer.Reconstructor = value;
                SetupScrollBar();
                //
                Invalidate();
            }
        }

        [Browsable(false)]
        public HeapCell FocusedCell
        {
            get { return iRenderer.FocusedCell; }
            set { iRenderer.FocusedCell = value; }
        }

        [Browsable(false)]
        public bool SupportsFiltering
        {
            get { return iRenderer.SupportsFiltering; }
        }

        [Browsable( false )]
        public HeapCellArrayBase BreadcrumbCellsOutgoing
        {
            get { return iRenderer.BreadcrumbCellsOutgoing; }
        }

        [Browsable( false )]
        public HeapCellArrayBase BreadcrumbCellsIncoming
        {
            get { return iRenderer.BreadcrumbCellsIncoming; }
        }
        #endregion

        #region Drawing
        protected override void OnPaint( PaintEventArgs aArgs )
        {
            aArgs.Graphics.Clear( Color.LightGray );					
            //
            float borderWidth = ( iRenderer.Focused ? 2.0f : 1.0f );
            Color borderColor  = ( iRenderer.Focused ? Color.Black : Color.Gray );

            using( Pen borderPen = new Pen( borderColor, borderWidth ) )
            {
                aArgs.Graphics.DrawRectangle( borderPen, 0, 0, ClientSize.Width-1, ClientSize.Height-1 );
            }
            //
            base.OnPaint( aArgs );
        }
        #endregion

        #region Event handlers
        private void HeapDataControl_SizeChanged(object sender, System.EventArgs e)
        {
            SetupScrollBar();
        }

        private void iSB_Vertical_Scroll(object sender, System.Windows.Forms.ScrollEventArgs e)
        {
            int rowIndex = e.NewValue;
            int bytesPerRow = iRenderer.RowsAndColumns.Width * 4;
            uint baseAddressOffset = (uint) ( bytesPerRow * rowIndex );
            uint address = iReconstructor.Statistics.HeapAddressStart + baseAddressOffset;
            //
            iRenderer.Address = address ;
        }
 
        private void iRenderer_AddressChanged( uint aAddressOld, uint aAddressNew, HeapLib.Cells.HeapCell aFirstCell, int aFirstCellIndex )
        {
            SetupScrollBar();
        }

        private void iRenderer_Enter(object sender, System.EventArgs e)
        {
            Invalidate();
        }

        private void iRenderer_Leave(object sender, System.EventArgs e)
        {
            Invalidate();
        }

        private void iRenderer_CellSelected(HeapLib.Cells.HeapCell aCell)
        {
            if  ( CellSelected != null )
            {
                CellSelected( aCell );
            }
        }

        private void iRenderer_CellDoubleClicked( HeapCell aCell )
        {
            if ( CellDoubleClicked != null )
            {
                CellDoubleClicked( aCell );
            }
        }

        private void iRenderer_CellRightClicked( HeapCell aCell, RawItem aRawItem, Point aViewerPos )
        {
            if ( CellRightClicked != null )
            {
                Point screenPos = iRenderer.PointToScreen( aViewerPos );
                CellRightClicked( aCell, aRawItem, screenPos );
            }
        }

        /*protected override void WndProc(ref Message m)
        {
            SymbianUtilsUi.Utilities.WindowMessages.PrintMessage( "CTRL ", m.Msg );
            base.WndProc (ref m);
        }*/
        #endregion

        #region Internal methods
        private void SetupScrollBar()
        {                
            Size rendererDimensions = iRenderer.RowsAndColumns;
            //
            if  ( iReconstructor != null && rendererDimensions.Width > 0 && rendererDimensions.Height > 0 )
            {
                uint extent = ( iReconstructor.Statistics.HeapAddressEnd - iReconstructor.Statistics.HeapAddressStart );
                uint bytesPerRow = (uint) ( iRenderer.RowsAndColumns.Width * 4 );
                uint numberOfRows = extent / bytesPerRow;
                uint rowNumber = ( iRenderer.Address - iReconstructor.Statistics.HeapAddressStart ) / bytesPerRow;
                if ( rowNumber > numberOfRows )
                {
                    rowNumber = numberOfRows;
                }
                //
                iSB_Vertical.Minimum = 0;
                iSB_Vertical.Maximum = (int) Math.Max( 0, numberOfRows );
                iSB_Vertical.Value = (int) rowNumber;
            }
        }
        #endregion

        #region Data members
        private HeapReconstructor iReconstructor;
        private THeapCtrlRenderingType iType = THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell;
        #endregion
	}
}
