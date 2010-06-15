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
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Reconstructor;
using HeapCtrlLib.Utilities;
using HeapCtrlLib.Interfaces;
using HeapCtrlLib.Factories;
using HeapCtrlLib.Types;
using SymbianUtils.Range;
using SymbianUtils.RawItems;

namespace HeapCtrlLib.Renderers
{
    internal class HeapDataRenderer : UserControl
    {
        #region Delegates & events
        public delegate void AddressChangeHandler( uint aAddressOld, uint aAddressNew, HeapCell aFirstCell, int aFirstCellIndex );
        public event AddressChangeHandler AddressChanged;
        public delegate void CellSelectionHandler( HeapCell aCell );
        public event CellSelectionHandler CellSelected;
        public delegate void CellDoubleClickHandler( HeapCell aCell );
        public event CellDoubleClickHandler CellDoubleClicked;
        public delegate void CellRightClickedHandler( HeapCell aCell, RawItem aItem, Point aViewerPos );
        public event CellRightClickedHandler CellRightClicked;
        #endregion

        #region Constructors & destructor
        internal HeapDataRenderer()
        {
            InitializeComponent();
            //
            LoadTypeSet( THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell );
            //
            this.SetStyle( ControlStyles.UserPaint, true );
            this.SetStyle( ControlStyles.DoubleBuffer, true );
            this.SetStyle( ControlStyles.AllPaintingInWmPaint, true );
            this.SetStyle( ControlStyles.ResizeRedraw, true );
            this.SetStyle( ControlStyles.Selectable, true );
            //
            this.MouseWheel += new MouseEventHandler( HeapDataRenderer_MouseWheel );
        }

        protected override void Dispose( bool disposing )
        {
            if  ( disposing )
            {
            }
            base.Dispose( disposing );
        }
        #endregion

        #region Component Designer generated code
        private void InitializeComponent()
        {
            this.iLbl_NoContent = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iLbl_NoContent
            // 
            this.iLbl_NoContent.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iLbl_NoContent.Location = new System.Drawing.Point( 0, 0 );
            this.iLbl_NoContent.Name = "iLbl_NoContent";
            this.iLbl_NoContent.Size = new System.Drawing.Size( 600, 408 );
            this.iLbl_NoContent.TabIndex = 0;
            this.iLbl_NoContent.Text = "No Content";
            this.iLbl_NoContent.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // HeapDataRenderer
            // 
            this.Controls.Add( this.iLbl_NoContent );
            this.DoubleBuffered = true;
            this.Name = "HeapDataRenderer";
            this.Size = new System.Drawing.Size( 600, 408 );
            this.MouseDown += new System.Windows.Forms.MouseEventHandler( this.HeapDataRenderer_MouseDown );
            this.MouseMove += new System.Windows.Forms.MouseEventHandler( this.HeapDataRenderer_MouseMove );
            this.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler( this.HeapDataRenderer_MouseDoubleClick );
            this.MouseLeave += new System.EventHandler( this.HeapDataRenderer_MouseLeave );
            this.MouseUp += new System.Windows.Forms.MouseEventHandler( this.HeapDataRenderer_MouseUp );
            this.KeyDown += new System.Windows.Forms.KeyEventHandler( this.HeapDataRenderer_KeyDown );
            this.ResumeLayout( false );

        }
        #endregion

        #region API
        public void SetupFilters()
        {
            if ( !SupportsFiltering )
            {
                throw new NotSupportedException();
            }

            iFactory.Renderers.Content.SetupFilters();
        }

        public void LoadTypeSet( THeapCtrlRenderingType aType )
        {
            iFactory = HeapCtrlLib.Factories.Factory.CreateByType( aType );

            // Set sizes
            CellBoxSize = iFactory.CellBoxSize( Zoom );
            CellPadding = iFactory.CellPadding( Zoom );

            // Do init.
            InitialiseRenderers();
        }

        public int RowForAddress( uint aAddress )
        {
            uint addressOffset = aAddress - Reconstructor.Statistics.HeapAddressStart;
            uint bytesPerRow = BytesPerRow;
            int row = (int) ( addressOffset / bytesPerRow );
            //System.Diagnostics.Debug.WriteLine( "base: 0x" + Reconstructor.Statistics.HeapAddressStart.ToString("x8") + ", offset: 0x" + addressOffset.ToString("x8") + ", addr: 0x" + aAddress.ToString("x8") + ", row: " + row + ", remainder: " + ( addressOffset % bytesPerRow ) + ", bytesPerRow: " + bytesPerRow );
            //
            return row;
        }
        #endregion

        #region Properties
        public HeapReconstructor Reconstructor
        {
            get
            {
                return iReconstructor;
            }
            set
            {
                iReconstructor = value;
                if  ( iReconstructor != null )
                {
                    iCells = iReconstructor.Data;
                    InitialiseRenderers();
                    if  ( iCells.Count > 0 )
                    {
                        HeapCell cell = Cells[ 0 ];

                        // Must first set the address, or else this messes up
                        // VisibleAddressRange...
                        Address = cell.Address;

                        // ... which is needed by FocusedCell when changing
                        // the focus.
                        FocusedCell = cell;
                    }
                }
            }
        }

        public HeapCellArray Cells
        {
            get
            {
                return iCells;
            }
        }

        public HeapCell FocusedCell
        {
            get
            {
                return iFocusedCellKeyboard;
            }
            set
            {
                iFocusedCellKeyboard = value;
                if  ( iFocusedCellKeyboard != null )
                {
                    uint visRangeMin = (uint) VisibleAddressRange.Min;
                    uint visRangeMax = (uint) VisibleAddressRange.Max - BytesPerRow;

                    // Do we need to move the view?
                    int currentMinRow = RowForAddress( visRangeMin );
                    int row = RowForAddress( value.Address );
                    int currentMaxRow = RowForAddress ( visRangeMax );

                    //
                    //System.Diagnostics.Debug.WriteLine( "row: " + row + ", minR: " + currentMinRow + "(" + visRangeMin.ToString("x8") + "), maxR: " + currentMaxRow + "(" + visRangeMax.ToString("x8") + ")" );
                    //System.Diagnostics.Debug.WriteLine( " " );

                    if  ( row < currentMinRow || row > currentMaxRow )
                    {
                        Address = (uint) ( Reconstructor.Statistics.HeapAddressStart + ( row * BytesPerRow ) );
                    }
                    else
                    {
                        Invalidate();
                    }
                }

                if  ( CellSelected != null )
                {
                    CellSelected( iFocusedCellKeyboard );
                }
            }
        }

        public uint Address
        {
            get { return iCellAddress; }
            set
            {
                this.Enabled = ( value > 0 );
                iLbl_NoContent.Visible = ( value == 0 );

                // Validate
                int index = -1;
                HeapCell cell = Cells.CellByAddress( value, out index );
                if  ( cell != null )
                {
                    iFactory.PopupManager.PopupHide();
                    //
                    iCellIndex = index;
                    uint oldAddress = iCellAddress;
                    iCellAddress = value;
                    iFocusedCellMouse = null;
                    //
                    if  ( AddressChanged != null )
                    {
                        AddressChanged( oldAddress, iCellAddress, cell, iCellIndex );
                    }
                    //
                    Invalidate();
                }
                else if ( value == 0 )
                {
                    // Do nothing, we're disabled
                }
                else
                {
                    // Bad address
                    throw new ArgumentException( "Invalid cell address", "value" );
                }
            }
        }

        public uint BytesPerRow
        {
            get
            {
                uint bytesPerRow = (uint) RowsAndColumns.Width * 4;
                return bytesPerRow;
            }
        }

        public uint BytesPerScreen
        {
            get
            {
                uint bytesPerScreen = BytesPerRow * (uint) RowsAndColumns.Height;
                return bytesPerScreen;
            }
        }

        public THeapCtrlZoom Zoom
        {
            get { return iZoom; }
            set
            {
                if ( value != iZoom )
                {
                    // Get base size from factory
                    CellBoxSize = iFactory.CellBoxSize( value );
                    CellPadding = iFactory.CellPadding( value );
                }
                //
                iZoom = value;
            }
        }

        public AddressRange VisibleAddressRange
        {
            get
            {
                AddressRange range = new AddressRange();
                //
                range.UpdateMin( Address );
                range.UpdateMax( Address + BytesPerScreen );
                //
                return range;
            }
        }

        public bool SupportsFiltering
        {
            get { return iFactory.Renderers.Content.SupportsFiltering; }
        }

        public HeapCellArrayBase BreadcrumbCellsOutgoing
        {
            get { return iBreadcrumbCellsOutgoing; }
            set { iBreadcrumbCellsOutgoing = new HeapCellArrayUnsorted( value ); }
        }

        public HeapCellArrayBase BreadcrumbCellsIncoming
        {
            get { return iBreadcrumbCellsIncoming; }
            set { iBreadcrumbCellsIncoming = new HeapCellArrayUnsorted( value ); }
        }

        internal Factory Factory
        {
            get { return iFactory; }
        }
        #endregion

        #region Co-ordinate mapping
        public Point AddressToPixelCoordinate( uint aAddress )
        {
            Point ret = new Point( 0, 0 );
            //
            Size overallPaddingSize = RowsAndColumnsRemainingPixels;
            Size rowAndColumnDimensions = RowsAndColumns;

            // First, work out how many boxes would be required to draw
            // the specific cell address
            long delta = ((long) aAddress - (long) iCellAddress);

            // This can be a negative number
            int boxesForDelta = (int) ( delta / KDWordSize );

            // Then work out how many rows and columns would be needed to reach
            // that value.
            Point numberOfRowsAndColumns = new Point( 0, 0 );

            if ( delta > 0 )
            {
                // Going forwards from current top left of view port.
                numberOfRowsAndColumns.Y = ( boxesForDelta / rowAndColumnDimensions.Width );
                numberOfRowsAndColumns.X = ( boxesForDelta - ( numberOfRowsAndColumns.Y * rowAndColumnDimensions.Width ) );
            }
            else
            {
                numberOfRowsAndColumns.Y = ( boxesForDelta / rowAndColumnDimensions.Width ) - 1;
                numberOfRowsAndColumns.X = Math.Abs( ( numberOfRowsAndColumns.Y * rowAndColumnDimensions.Width ) - boxesForDelta );
            }

            // Work our pixel pos
            ret.X = numberOfRowsAndColumns.X * CellBoxSizeIncludingPadding.Width;
            ret.Y = numberOfRowsAndColumns.Y * CellBoxSizeIncludingPadding.Height;

            // And take into account padding and header size
            ret.X += CellBoxRect.X;
            ret.Y += CellBoxRect.Y;

            //
            return ret;
        }

        public Point CoordinateToBox( Point aPixelPos )
        {
            Point ret = new Point( -1, -1 );
            //
            Size rowsAndColumns = RowsAndColumns;
            if  ( CellBoxRect.Contains( aPixelPos ) )
            {
                Size overallPaddingSize = RowsAndColumnsRemainingPixels;

                // Work out which row this event occurs within.
                int row = ( aPixelPos.Y - ( overallPaddingSize.Height / 2 ) ) / CellBoxSizeIncludingPadding.Height;
                if  ( row >= 0 && row < rowsAndColumns.Height )
                {
                    // Work out the column index for that cell.
                    int xpos = aPixelPos.X - ( overallPaddingSize.Width / 2 ) - CellAddressHeaderSize.Width;

                    // How many boxes could we have rendered for that xpos
                    int boxCount = ( xpos / CellBoxSizeIncludingPadding.Width );

                    ret.X = boxCount;
                    ret.Y = row;
                }
            }
            //System.Diagnostics.Debug.WriteLine( "aPixelPos[ " + aPixelPos.X + ", " + aPixelPos.Y + " ], boxPos: [ " + ret.X + ", " + ret.Y + " ]" );
            //
            return ret;
        }

        public Point BoxCoordinateByAddress( uint aAddress )
        {
            Point ret = new Point( -1, -1 );

            // First, work out how many boxes would be required to draw
            // the specific cell address
            uint delta = aAddress - iCellAddress;
            int boxesForDelta = (int) ( delta / KDWordSize );

            // Then work out how many rows & columns would need to be
            // drawn to reach that value.
            Size rowAndColumnDimensions = RowsAndColumns;
            ret.Y = boxesForDelta / rowAndColumnDimensions.Width;
            ret.X = boxesForDelta - ( ret.Y * rowAndColumnDimensions.Width );
            //
            return ret;
        }

        public uint AddressByBoxCoordinates( Point aBoxCoordinates )
        {
            uint ret = iCellAddress;
            //
            if  ( Cells.Count > 0 && aBoxCoordinates.X >= 0 && aBoxCoordinates.Y >= 0 )
            {
                Size rowsAndColumns = RowsAndColumns;
                //
                ret += (uint) ( aBoxCoordinates.Y * KDWordSize * rowsAndColumns.Width );
                ret += (uint) ( aBoxCoordinates.X * KDWordSize );
            }
            //
            return ret;
        }

        public uint AddressByCoordinate( Point aPixelPos )
        {
            Point boxPos = CoordinateToBox( aPixelPos );
            uint ret = AddressByBoxCoordinates( boxPos );
            return ret;
        }

        public RawItem RawItemByPixelPos( Point aPixelPos )
        {
            RawItem ret = null;
            //
            Point boxPos = CoordinateToBox( aPixelPos );
            HeapCell cell = CellByBoxCoordinates( boxPos );
            //
            if ( cell != null )
            {
                uint rawItemAddress = AddressByBoxCoordinates( boxPos );
                HeapCell.TRegion region = cell.RegionForAddress( rawItemAddress );
                
                // We only provide raw items for payload sections
                if ( region == HeapCell.TRegion.EPayload )
                {
                    ret = cell[ rawItemAddress ];
                }
            }
            //
            return ret;
        }

        public HeapCell CellByPosition( Point aPixelPos )
        {
            HeapCell ret = null;
            //
            Point boxPos = CoordinateToBox( aPixelPos );
            if  ( Cells.Count > 0 && boxPos.X >= 0 && boxPos.Y >= 0 )
            {
                ret = CellByBoxCoordinates( boxPos );
            }
            //
            return ret;
        }

        public HeapCell CellByBoxCoordinates( Point aBoxCoordinates )
        {
            uint address = AddressByBoxCoordinates( aBoxCoordinates );
            HeapCell ret = Cells.CellByAddress( address );
            return ret;
        }

        public int CellIndex( HeapCell aCell )
        {
            int index = -1;
            Cells.CellByExactAddress( aCell.Address, out index );
            return index;
        }
        #endregion

        #region Sizing
        internal Rectangle CellBoxRect
        {
            get
            {
                Size overallPaddingSize = RowsAndColumnsRemainingPixels;
                Size bodySize = RowsAndColumnsInPixels;
                //
                Rectangle ret = new Rectangle();
                ret.X = ( overallPaddingSize.Width / 2 ) + CellAddressHeaderSize.Width;
                ret.Y = overallPaddingSize.Height / 2;
                ret.Width = bodySize.Width;
                ret.Height = bodySize.Height;
                //
                return ret;
            }
        }

        [Browsable(false)]
        internal Size CellBoxSize
        {
            get { return iCellBoxSize; }
            set
            {
                iCellBoxSize = value;
                Invalidate();
            }
        }

        internal Size CellBoxSizeIncludingPadding
        {
            get
            {
                Size size = CellBoxSize;
                //
                size.Width += CellPadding.Width;
                size.Height += CellPadding.Height;
                //
                return size;
            }
        }

        internal Size CellBoxSizeIncludingPaddingHalved
        {
            get
            {
                Size size = CellBoxSize;
                //
                size.Width += CellPadding.Width;
                size.Height += CellPadding.Height;
                //
                return new Size( size.Width / 2, size.Height / 2 );
            }
        }

        [Browsable(false)]
        internal Size CellPadding
        {
            get { return iCellPadding; }
            set
            {
                iCellPadding = value;
                Invalidate();
            }
        }

        internal Size CellPaddingHalved
        {
            get { return new Size( iCellPadding.Width / 2, iCellPadding.Height / 2 ); }
        }

        internal Size RowsAndColumns
        {
            get
            {
                Size s = this.Size;
                
                // First, strip off the address header text width
                s.Width -= iHeaderTextWidth;

                // Next, calculate how wide each box will be, including padding
                int boxWidthPerCell = CellBoxSizeIncludingPadding.Width;
                int cols = Math.Max( 0, ( s.Width - 1 ) / boxWidthPerCell );

                // Now, do the same for rows
                int boxHeightPerCell = CellBoxSizeIncludingPadding.Height;
                int rows = Math.Max( 0, ( s.Height - 1 ) / boxHeightPerCell );
                //
                return new Size( cols, rows );
            }
        }

        internal Size RowsAndColumnsInPixels
        {
            get
            {
                Size rowsAndCols = RowsAndColumns;
                Size rowsAndColsInPixels = new Size( rowsAndCols.Width * CellBoxSizeIncludingPadding.Width, rowsAndCols.Height * CellBoxSizeIncludingPadding.Height );
                //
                return rowsAndColsInPixels;
            }
        }

        internal Size RowsAndColumnsRemainingPixels
        {
            get
            {
                Size s = this.Size;
                Size rowsAndColsInPixels = RowsAndColumnsInPixels;
                //
                int colsRemainder = s.Width - rowsAndColsInPixels.Width - CellAddressHeaderSize.Width;
                int rowsRemainder = s.Height - rowsAndColsInPixels.Height;
                //
                return new Size( colsRemainder, rowsRemainder );
            }
        }

        internal Size RowsAndColumnsRemainingPixelsDistributedEvenly
        {
            get
            {
                Size overallPaddingSize = RowsAndColumnsRemainingPixels;
                overallPaddingSize.Width /= 2;
                overallPaddingSize.Height /= 2;
                return overallPaddingSize;
            }
        }

        internal Size CellAddressHeaderSize
        {
            get
            {
                return new Size( iHeaderTextWidth, CellBoxSize.Height );
            }
        }
        #endregion

        #region Drawing
        protected override void OnPaint( PaintEventArgs aArgs )
        {
            try
            {
                // First must measure the size of the header text as we need
                // this in order to layout the boxes. We cannot measure the text width
                // without a graphics object, hence we have to do that here. There must
                // be a better way?
                iHeaderTextWidth = iFactory.Renderers.Header.MeasureCellHeaderText( aArgs.Graphics );

                // Store our graphics object for later use
                iGraphics = aArgs.Graphics;

                // Make the navigator object that will help us lay out the cells
                HeapRenderingNavigator navigator = new HeapRenderingNavigator( Cells );

                // Queue events we want to receive
                navigator.iNavBegin += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavBegin( Navigator_NavBegin );
                navigator.iNavEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavEnd( Navigator_NavEnd );
                navigator.iNavNewRowHeader += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewRowHeader( Navigator_NavNewRowHeader );
                navigator.iNavNewColumn += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavNewColumn( Navigator_NavNewColumn );
                navigator.iNavHeapCellEnd += new HeapCtrlLib.Utilities.HeapRenderingNavigator.NavHeapCellEnd( Navigator_NavHeapCellEnd );

                // The padding amount we apply around the entire control area in order to
                // center the data.
                Size centerAlignmentPadding = RowsAndColumnsRemainingPixelsDistributedEvenly;

                // The co-ordinates at which we will render a box (or header).
                Point startPos = new Point( centerAlignmentPadding.Width, centerAlignmentPadding.Height );

                // Tell the renderers we're about to start
                iFactory.Renderers.PrepareToNavigate( navigator );

                // Do it
                navigator.Navigate( iCellIndex, iCellAddress, iHeaderTextWidth, startPos, RowsAndColumns, CellBoxSize, CellPadding );

                // Draw link lines
                aArgs.Graphics.Clip = new Region( CellBoxRect );

                foreach ( HeapCell cell in BreadcrumbCellsOutgoing )
                {
                    RelationshipManager relManager = cell.RelationshipManager;

                    // Draw embedded (outgoing) lines
                    foreach ( RelationshipInfo outgoingRelationship in relManager.EmbeddedReferencesTo )
                    {
                        // Get coordinates of top left corner of the box for the specified 
                        // address. If we have a clean link, we point right at the cell header.
                        // If not, then we point at the box in question within the target cell.
                        uint targetAddress = outgoingRelationship.FromCellRawItem.Data;
                        if ( outgoingRelationship.IsCleanLink )
                        {
                            targetAddress = outgoingRelationship.ToCell.Address;
                        }
                        Point pixelPosEnd = AddressToPixelCoordinate( targetAddress );
                        pixelPosEnd += CellBoxSizeIncludingPaddingHalved;
                        Point pixelPosStart = AddressToPixelCoordinate( outgoingRelationship.FromCellRawItem.Address );
                        pixelPosStart += CellBoxSizeIncludingPaddingHalved;
                        //
                        Color outgoingPenColour = Color.FromArgb( 120, 255, 0, 0 );
                        using ( Pen pen = new Pen( outgoingPenColour, 4.0f ) )
                        {
                            pen.DashStyle = System.Drawing.Drawing2D.DashStyle.Dash;
                            pen.EndCap = System.Drawing.Drawing2D.LineCap.ArrowAnchor;
                            //
                            aArgs.Graphics.DrawLine( pen, pixelPosStart, pixelPosEnd );
                        }
                    }
                }

                foreach( HeapCell cell in BreadcrumbCellsIncoming )
                {
                    RelationshipManager relManager = cell.RelationshipManager;
                    
                    // Draw incoming links
                    foreach ( HeapCell fromCell in relManager.ReferencedBy )
                    {
                        Point pixelPosStart = AddressToPixelCoordinate( fromCell.Address );
                        pixelPosStart += CellBoxSizeIncludingPaddingHalved;
                        Point pixelPosEnd = AddressToPixelCoordinate( cell.Address );
                        pixelPosEnd += CellBoxSizeIncludingPaddingHalved;
                        //
                        Color outgoingPenColour = Color.FromArgb( 100, 0, 0, 255 );
                        using ( Pen pen = new Pen( outgoingPenColour, 4.0f ) )
                        {
                            pen.DashStyle = System.Drawing.Drawing2D.DashStyle.Dash;
                            pen.EndCap = System.Drawing.Drawing2D.LineCap.ArrowAnchor;
                            //
                            aArgs.Graphics.DrawLine( pen, pixelPosStart, pixelPosEnd );
                        }
                    }
                }
            }
            finally
            {
                iGraphics = null;
                base.OnPaint( aArgs );
            }
        }
        #endregion

        #region Navigation call backs
        private void Navigator_NavBegin()
        {
            iRenderStartTime = DateTime.Now;
            //System.Diagnostics.Debug.WriteLine( "DRAW START" );

            // Clear any existing data
            iGraphics.Clear( Color.White );
        }

        private void Navigator_NavEnd()
        {
            iFactory.Renderers.RenderingComplete( iGraphics );

            System.DateTime renderTimeEnd = DateTime.Now;
            //System.Diagnostics.Debug.WriteLine( "DRAW END - " + ( renderTimeEnd.Ticks - iRenderStartTime.Ticks ) / 100 );
        }

        private void Navigator_NavNewRowHeader( uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            // Draw the address at the start of each row
            iFactory.Renderers.Header.PaintRowHeader( iGraphics, aPosition, CellAddressHeaderSize, aAddress );
        }

        private void Navigator_NavNewColumn( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPixelPos, Point aBoxPos, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            // Draw content
            iFactory.Renderers.Content.PaintContent( iGraphics, aMetaData, aCell, aAddress, aPixelPos, aBoxSize, aPadding );

            // Draw cell border
            iFactory.Renderers.ContentBorder.PaintContentBorder( iGraphics, aMetaData, aCell, aAddress, aPixelPos, aBoxSize, aPadding );

            // If we are handling a click & drag operation, then check whether the cell
            // needs a border.
            if  ( iMouseSelectionCell != null )
            {
                // Get the cell index within the cell array 
                int index = CellIndex( aCell );
                int mouseBoundLower = (int) iMouseSelectionCellBoundaryLower.Tag;
                int mouseBoundUpper = (int) iMouseSelectionCellBoundaryUpper.Tag;

                if  ( index >= mouseBoundLower && index <= mouseBoundUpper )
                {
                    iFactory.Renderers.SelectionBorder.PaintSelectionBorder( iGraphics, aMetaData, aCell, aAddress, aPixelPos, aBoxSize, aPadding, THeapSelectionBorderType.ESelectionMouse );
                }
            }
            else if ( iFocusedCellKeyboard == aCell )
            {
                // Draw border around currently mouse over'd cell
                iFactory.Renderers.SelectionBorder.PaintSelectionBorder( iGraphics, aMetaData, aCell, aAddress, aPixelPos, aBoxSize, aPadding, THeapSelectionBorderType.ESelectionKeyboard );
            }
            else if ( iFocusedCellMouse == aCell )
            {
                // Draw border around currently mouse over'd cell
                iFactory.Renderers.SelectionBorder.PaintSelectionBorder( iGraphics, aMetaData, aCell, aAddress, aPixelPos, aBoxSize, aPadding, THeapSelectionBorderType.ESelectionMouse );
            }
        }

        private void Navigator_NavHeapCellEnd( HeapCell aCell, HeapCellMetaData aMetaData, uint aAddress, Point aPosition, Size aDimensions, Size aBoxSize, Size aPadding )
        {
            iFactory.Renderers.HeapCellRenderingComplete( iGraphics, aCell, aMetaData );
        }
        #endregion

        #region Internal constants
        private const uint KDWordSize = SymbianUtils.RawItems.RawItem.KSizeOfOneRawItemInBytes;
        private Label iLbl_NoContent;
        #endregion

        #region Internal methods
        private void InitialiseRenderers()
        {
            if  ( Reconstructor != null )
            {
                iFactory.Renderers.Initialise( Cells, Reconstructor, this );
            }
        }

        private void AsyncShowPopup( HeapCellArrayWithStatistics aCells, Point aPos, RawItem aRawItem )
        {
            if ( iFactory.PopupManager.SupportsRawItemInfo && aRawItem != null && aRawItem.Tag != null && aRawItem.Tag is RelationshipInfo && aCells.Count == 1 )
            {
                HeapCell cell = aCells[ 0 ];
                iFactory.PopupManager.PopupShowAsync( cell, aRawItem, Reconstructor.Statistics, aPos, PointToScreen( aPos ), CellBoxSizeIncludingPadding, new KeyEventHandler( HeapDataRenderer_KeyDown ) );
            }
            else
            {
                iFactory.PopupManager.PopupShowAsync( aCells, Reconstructor.Statistics, aPos, PointToScreen( aPos ), CellBoxSizeIncludingPadding, new KeyEventHandler( HeapDataRenderer_KeyDown ) );
            }

            iMouseHoverPosition = aPos;
        }
        #endregion

        #region Key handling
        protected override bool ProcessCmdKey(ref Message aMsg, Keys aKeyData)
        {
            const int WM_KEYFIRST = 0x100;
            bool handled = false;
            //
            if ( aMsg.Msg == WM_KEYFIRST )
            {
                //SymbianUtilsUi.Utilities.WindowMessages.PrintMessage( "RCMD [" + aKeyData + "] ", aMsg.Msg );
                KeyEventArgs keyArgs = new KeyEventArgs( aKeyData );
                HandleKey( this, keyArgs );
                handled = keyArgs.Handled;
            }
            //
            if  ( !handled )
            {
                handled = base.ProcessCmdKey( ref aMsg, aKeyData );
            }
            //
            return handled;
        }

        private void HeapDataRenderer_KeyDown(object sender, System.Windows.Forms.KeyEventArgs e)
        {
            //System.Diagnostics.Debug.WriteLine( "Key Down: " + e.KeyCode );
            HandleKey( sender, e );
        }

        private void HandleKey( object sender, System.Windows.Forms.KeyEventArgs e )
        {
            switch( e.KeyCode )
            {
                    // Move up & down one LINE at a time...
                case Keys.Down:
                    ScrollByLineDelta( 1 );
                    e.Handled = true;
                    break;
                case Keys.Up:
                    ScrollByLineDelta( -1 );
                    e.Handled = true;
                    break;

                    // Move up & down one PAGE at a time...
                case Keys.Next:
                    ScrollByLineDelta( RowsAndColumns.Height );
                    e.Handled = true;
                    break;
                case Keys.Prior:
                    ScrollByLineDelta( -RowsAndColumns.Height );
                    e.Handled = true;
                    break;

                    // Move to beginning or end of heap
                case Keys.Home:
                    ScrollToAddress( Reconstructor.Statistics.HeapAddressStart );
                    e.Handled = true;
                    break;
                case Keys.End:
                {
                    uint rowsForHeap = iReconstructor.Statistics.HeapSize / BytesPerRow;
                    uint rowsPerPage = (uint) RowsAndColumns.Height;
                    uint targetRow = rowsForHeap - rowsPerPage + 1;
                    uint address = Reconstructor.Statistics.HeapAddressStart + ( targetRow *  BytesPerRow );
                    ScrollToAddress( address );
                    e.Handled = true;
                    break;
                }

                    // Move one cell at a time
                case Keys.Right:
                {
                    if  ( iFocusedCellKeyboard != null )
                    {
                        int index = Cells.CellIndex( iFocusedCellKeyboard );
                        if  ( index + 1 < Cells.Count )
                        {
                            iFactory.PopupManager.PopupHide();
                            FocusedCell = Cells[ index + 1 ];
                            e.Handled = true;
                        }
                    }
                    break;
                }
                case Keys.Left:
                {
                    if  ( iFocusedCellKeyboard != null )
                    {
                        int index = Cells.CellIndex( iFocusedCellKeyboard );
                        if  ( index - 1 >= 0 )
                        {
                            iFactory.PopupManager.PopupHide();
                            FocusedCell = Cells[ index - 1 ];
                            e.Handled = true;
                        }
                    }
                    break;
                }

                    // Unhandled
                default:
                    e.Handled = false;
                    break;
            }
        } 
       
        private void ScrollByLineDelta( int aLines )
        {
            // Update delta to now finally take into account how many
            // bytes we are going to offset the current address by
            uint delta = (uint) ( aLines * BytesPerRow );

            // Get current address from renderer
            uint address = Address;

            // We are going to attempt to offset the current renderer address
            // by the delta, but we don't want to fall out of bounds (before or
            // after the min/max address range for the heap).
            address += delta;

            // Set address
            ScrollToAddress( address );
        }

        private void ScrollToAddress( uint aAddress )
        {
            // Work out the maximum address. We never prevent the user to scroll so far
            // that they go past the last line.
            uint rowsForHeap = Reconstructor.Statistics.HeapSize / BytesPerRow;
            uint lastRowStartingAddress = Reconstructor.Statistics.HeapAddressStart + ( rowsForHeap *  BytesPerRow );
            uint address = Math.Min( lastRowStartingAddress, Math.Max( Reconstructor.Statistics.HeapAddressStart, aAddress ) );

            if  ( address >= Reconstructor.Statistics.HeapAddressStart )
            {
                Address = address;
            }
        }
        #endregion

        #region Mouse handling
        /*protected override void WndProc(ref Message m)
        {
            SymbianUtilsUi.Utilities.WindowMessages.PrintMessage( "RNDR ", m.Msg );
            base.WndProc (ref m);
        }*/

        private void HeapDataRenderer_MouseDown(object sender, System.Windows.Forms.MouseEventArgs e)
        {
            if ( e.Button == MouseButtons.Right )
            {
                // Convert the click pos to a cell
                HeapCell cell = CellByPosition( new Point( e.X, e.Y ) );
                if ( cell != null && CellRightClicked != null )
                {
                    // Convert the click pos to a box
                    Point boxPos = CoordinateToBox( e.Location );
                    
                    // and then convert the box coordinate to an address
                    uint address = AddressByBoxCoordinates( boxPos );
                    
                    // And the try to map that address to a raw item
                    RawItem rawItem = cell[ address ];

                    // Then notify observer
                    CellRightClicked( cell, rawItem, e.Location );
                }
            }
            else
            {
                bool fc = Focus();
                Select();
                //System.Diagnostics.Debug.WriteLine( "RNDR Focused: " + fc );

                if ( Cells.Count > 0 && iMouseSelectionCell == null )
                {
                    iMouseSelectionCell = CellByPosition( new Point( e.X, e.Y ) );
                    //
                    if ( iMouseSelectionCell != null )
                    {
                        iFactory.PopupManager.PopupHide();
                        //
                        iMouseSelectionCell.Tag = CellIndex( iMouseSelectionCell );
                        //
                        iMouseSelectionCellBoundaryLower = iMouseSelectionCell;
                        iMouseSelectionCellBoundaryUpper = iMouseSelectionCell;
                        //
                        //System.Diagnostics.Debug.WriteLine( "MouseDOWN: [ " + e.X + ", " + e.Y + " ] " + iMouseSelectionCell.ToString() );
                        //
                    }
                }
            }
        }
 
        private void HeapDataRenderer_MouseUp(object sender, System.Windows.Forms.MouseEventArgs e)
        {
            //System.Diagnostics.Debug.WriteLine( "MouseUP:   [ " + e.X + ", " + e.Y + " ]" );
            if  ( Cells.Count > 0 && iMouseSelectionCell != null )
            {
                HeapCell mouseUpHeapCell = CellByPosition( new Point( e.X, e.Y ) );
                //
                if  ( mouseUpHeapCell != null )
                {
                    //System.Diagnostics.Debug.WriteLine( "MouseUP:   [ " + e.X + ", " + e.Y + " ] " + mouseUpHeapCell.ToString() );
                }

                FocusedCell = mouseUpHeapCell;

                iMouseSelectionCell = null;
                Invalidate();
            }
        }

        private void HeapDataRenderer_MouseMove(object sender, System.Windows.Forms.MouseEventArgs e)
        {
            if  ( Cells.Count > 0 )
            {
                Point mousePos = new Point( e.X, e.Y );
                HeapCell cell = CellByPosition( mousePos );
                //
                if  ( cell != null )
                {
                    // Also try to get the raw item associated with the hover position. This can
                    // also return null.
                    RawItem rawItem = RawItemByPixelPos( mousePos );

                    // Now process the mouse movement
                    if  ( iMouseSelectionCell != null )
                    {
                        int mouseBoundStart = (int) iMouseSelectionCell.Tag;
                        int mouseBoundLower = (int) iMouseSelectionCellBoundaryLower.Tag;
                        int mouseBoundUpper = (int) iMouseSelectionCellBoundaryUpper.Tag;
                        int originalSelectionRangeCount = ( mouseBoundUpper - mouseBoundLower );

                        int index = CellIndex( cell );
                        if  ( index < mouseBoundStart )
                        {
                            // Reset upper boundary
                            iMouseSelectionCellBoundaryUpper = iMouseSelectionCell;
                            iMouseSelectionCellBoundaryLower = cell;
                            iMouseSelectionCellBoundaryLower.Tag = index;
                        }
                        else if ( index > mouseBoundStart )
                        {
                            // Reset lower boundary
                            iMouseSelectionCellBoundaryLower = iMouseSelectionCell;
                            iMouseSelectionCellBoundaryUpper = cell;
                            iMouseSelectionCellBoundaryUpper.Tag = index;
                        }
                        else if ( index == mouseBoundStart )
                        {
                            iMouseSelectionCellBoundaryUpper = iMouseSelectionCell;
                            iMouseSelectionCellBoundaryLower = iMouseSelectionCell;
                        }

                        // Update boundary
                        mouseBoundLower = (int) iMouseSelectionCellBoundaryLower.Tag;
                        mouseBoundUpper = (int) iMouseSelectionCellBoundaryUpper.Tag;

                        // Work out if this is a multi-select scenario
                        int selectionRangeCount = mouseBoundUpper - mouseBoundLower;
                        bool selectionHasChanged = ( selectionRangeCount != originalSelectionRangeCount );
                        if  ( selectionHasChanged && Math.Abs( selectionRangeCount ) + 1 > 1 )
                        {
                            // Build array of selected cells
                            HeapCellArrayWithStatistics cells = new HeapCellArrayWithStatistics();
                            for( index = mouseBoundLower; index <= mouseBoundUpper; index++ )
                            {
                                cell = Cells[ index ];
                                cells.Add( cell );
                            }

                            // Show popup
                            bool popupVisible = iFactory.PopupManager.Visible;
                            Point pos = new Point( e.X, e.Y );

                            if  ( !popupVisible || !( e.X == iMouseHoverPosition.X && e.Y == iMouseHoverPosition.Y ) )
                            {
                                if  ( !popupVisible )
                                {
                                    //System.Diagnostics.Debug.WriteLine( "Mouse MOVE - MS [popup not vis], Differing Coords: [ " + e.X + ", " + e.Y + " ] -> Popup Show" );
                                    AsyncShowPopup( cells, pos, rawItem );
                                }
                                else
                                {
                                    //System.Diagnostics.Debug.WriteLine( "Mouse MOVE - MS [popup visible], Differing Coords: [ " + e.X + ", " + e.Y + " ] -> Popup Already Shown" );
                                    iMouseHoverPosition = pos;
                                    iFactory.PopupManager.PopupRelocate( cells, Reconstructor.Statistics, pos, PointToScreen( pos ), CellBoxSizeIncludingPadding );
                                }
                            }
                            else if ( !popupVisible )
                            {
                                //System.Diagnostics.Debug.WriteLine( "Mouse MOVE - MS [popup not vis], Differing Coords: [ " + e.X + ", " + e.Y + " ] -> Popup Show" );
                                AsyncShowPopup( cells, pos, rawItem );
                            }
                       }
                        else if ( selectionHasChanged )
                        {
                            //System.Diagnostics.Debug.WriteLine( "Mouse MOVE - Have Existing Selection: [ " + e.X + ", " + e.Y + " ] -> Single Item Hover" );
                            iFactory.PopupManager.PopupHide();
                        }
                    }
                    else
                    {
                        if  ( iFactory.PopupManager.Visible )
                        {
                            if  ( ! ( e.X == iMouseHoverPosition.X && e.Y == iMouseHoverPosition.Y ) )
                            {
                                //System.Diagnostics.Debug.WriteLine( "Mouse MOVE: [ " + e.X + ", " + e.Y + " ] -> Popup Hidden" );
                                iFactory.PopupManager.PopupHide();
                            }
                        }
                        else if  ( ! ( e.X == iMouseHoverPosition.X && e.Y == iMouseHoverPosition.Y ) )
                        {
                            //System.Diagnostics.Debug.WriteLine( "Mouse MOVE: [ " + e.X + ", " + e.Y + " ] -> Popup Show Async" );
                            Point pos = new Point( e.X, e.Y );
                            HeapCellArrayWithStatistics cells = new HeapCellArrayWithStatistics();
                            cells.Add( cell );
                            AsyncShowPopup( cells, pos, rawItem );
                        }
                    }
                    //
                    Invalidate();
                }
                //
                iFocusedCellMouse = cell;
            }
        }

        private void HeapDataRenderer_MouseLeave(object sender, System.EventArgs e)
        {
            Point pos = System.Windows.Forms.Cursor.Position;
            //System.Diagnostics.Debug.WriteLine( "Mouse LEAVE: Popup hidden - pos: " + pos + ", locY: " + PointToScreen( Location ).Y );

            iFactory.PopupManager.PopupHide();
            //
            iFocusedCellMouse = null;
            iMouseSelectionCell = null;
            //
            Invalidate();
        }

        private void HeapDataRenderer_MouseWheel(object sender, MouseEventArgs e)
        {
            // For each scroll of the mouse wheel
            int mouseScrollLines = SystemInformation.MouseWheelScrollLines;

            // Odd(?), but scrolling down results in a negative delta (-120), and
            // scrolling up results in positive
            int delta = ( e.Delta < 0 ) ? 1 : -1;
            delta *= mouseScrollLines;

            ScrollByLineDelta( delta );
        }

        private void HeapDataRenderer_MouseDoubleClick( object sender, MouseEventArgs e )
        {
            HeapCell cell = CellByPosition( new Point( e.X, e.Y ) );
            //
            if ( cell !=  null && CellDoubleClicked != null )
            {
                CellDoubleClicked( cell );
            }
        }
        #endregion

        #region Data members
        private HeapCell iFocusedCellKeyboard = null;
        private HeapCell iFocusedCellMouse = null;
        private Point iMouseHoverPosition;
        private HeapCell iMouseSelectionCell = null;
        private HeapCell iMouseSelectionCellBoundaryLower = null;
        private HeapCell iMouseSelectionCellBoundaryUpper = null;
        private HeapReconstructor iReconstructor = null;
        private HeapCellArray iCells = new HeapCellArray();
        private int iHeaderTextWidth = 30;
        private Size iCellBoxSize = new Size( 100, 100 );
        private Size iCellPadding = new Size(  20,  20 );
        private int iCellIndex = 0;
        private uint iCellAddress = 0;
        private Factory iFactory = null;
        private Graphics iGraphics = null;
        private THeapCtrlZoom iZoom = THeapCtrlZoom.EHeapCtrlZoomMedium;
        private HeapCellArrayWithStatistics iSelectedCells = new HeapCellArrayWithStatistics();
        private System.DateTime iRenderStartTime = new DateTime();
        private HeapCellArrayUnsorted iBreadcrumbCellsOutgoing = new HeapCellArrayUnsorted();
        private HeapCellArrayUnsorted iBreadcrumbCellsIncoming = new HeapCellArrayUnsorted();
        #endregion
    }
}
