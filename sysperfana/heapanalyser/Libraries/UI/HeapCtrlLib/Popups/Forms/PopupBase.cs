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
using SymbianUtils.Colour;

namespace HeapCtrlLib.Popups.Forms
{
    public class PopupBase : System.Windows.Forms.Form
    {
        #region Delegates and events
        public delegate void BackgroundColourChangeHandler( Color aColour );
        public event BackgroundColourChangeHandler ColourChangeBackground;
        public delegate void TitleColourChangeHandler( Color aColour );
        public event TitleColourChangeHandler ColourChangeTitle;
        #endregion

        #region Windows Form Designer data members
        private System.ComponentModel.IContainer components;
        private System.Windows.Forms.Timer iTimer;
        private System.Windows.Forms.Label iLbl_Title;
        private System.Windows.Forms.Label iLbl_Footer;
        private System.Windows.Forms.Label iLbl_Title_Length_Payload;
        private System.Windows.Forms.Label iLbl_Footer_Percentage_OfTotal;
        private System.Windows.Forms.Label iLbl_Footer_Percentage_OfType;
        private System.Windows.Forms.Label iLbl_Title_Length_Header;
        #endregion

        #region Constructors & destructor
        public PopupBase()
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
                if(components != null)
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
            this.components = new System.ComponentModel.Container();
            this.iLbl_Title = new System.Windows.Forms.Label();
            this.iLbl_Title_Length_Payload = new System.Windows.Forms.Label();
            this.iLbl_Title_Length_Header = new System.Windows.Forms.Label();
            this.iLbl_Footer = new System.Windows.Forms.Label();
            this.iTimer = new System.Windows.Forms.Timer( this.components );
            this.iLbl_Footer_Percentage_OfTotal = new System.Windows.Forms.Label();
            this.iLbl_Footer_Percentage_OfType = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // iLbl_Title
            // 
            this.iLbl_Title.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Title.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Title.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Title.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Title.Location = new System.Drawing.Point( 1, 1 );
            this.iLbl_Title.Name = "iLbl_Title";
            this.iLbl_Title.Size = new System.Drawing.Size( 518, 22 );
            this.iLbl_Title.TabIndex = 0;
            this.iLbl_Title.Text = "Popup Title";
            this.iLbl_Title.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iLbl_Title_Length_Payload
            // 
            this.iLbl_Title_Length_Payload.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Title_Length_Payload.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Title_Length_Payload.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Title_Length_Payload.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Title_Length_Payload.Location = new System.Drawing.Point( 408, 1 );
            this.iLbl_Title_Length_Payload.Name = "iLbl_Title_Length_Payload";
            this.iLbl_Title_Length_Payload.Size = new System.Drawing.Size( 104, 22 );
            this.iLbl_Title_Length_Payload.TabIndex = 0;
            this.iLbl_Title_Length_Payload.Text = "P: [00056024]";
            this.iLbl_Title_Length_Payload.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.iLbl_Title_Length_Payload.TextChanged += new System.EventHandler( this.iLbl_TextChanged );
            // 
            // iLbl_Title_Length_Header
            // 
            this.iLbl_Title_Length_Header.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Title_Length_Header.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Title_Length_Header.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Title_Length_Header.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Title_Length_Header.Location = new System.Drawing.Point( 280, 1 );
            this.iLbl_Title_Length_Header.Name = "iLbl_Title_Length_Header";
            this.iLbl_Title_Length_Header.Size = new System.Drawing.Size( 120, 22 );
            this.iLbl_Title_Length_Header.TabIndex = 0;
            this.iLbl_Title_Length_Header.Text = "H: [2]";
            this.iLbl_Title_Length_Header.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.iLbl_Title_Length_Header.TextChanged += new System.EventHandler( this.iLbl_TextChanged );
            // 
            // iLbl_Footer
            // 
            this.iLbl_Footer.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Footer.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Footer.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Footer.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Footer.Location = new System.Drawing.Point( 1, 119 );
            this.iLbl_Footer.Name = "iLbl_Footer";
            this.iLbl_Footer.Size = new System.Drawing.Size( 518, 24 );
            this.iLbl_Footer.TabIndex = 2;
            this.iLbl_Footer.Text = " Footer";
            this.iLbl_Footer.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // iTimer
            // 
            this.iTimer.Interval = 650;
            this.iTimer.Tick += new System.EventHandler( this.iTimer_Tick );
            // 
            // iLbl_Footer_Percentage_OfTotal
            // 
            this.iLbl_Footer_Percentage_OfTotal.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Footer_Percentage_OfTotal.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Footer_Percentage_OfTotal.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Footer_Percentage_OfTotal.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Footer_Percentage_OfTotal.Location = new System.Drawing.Point( 416, 120 );
            this.iLbl_Footer_Percentage_OfTotal.Name = "iLbl_Footer_Percentage_OfTotal";
            this.iLbl_Footer_Percentage_OfTotal.Size = new System.Drawing.Size( 96, 22 );
            this.iLbl_Footer_Percentage_OfTotal.TabIndex = 3;
            this.iLbl_Footer_Percentage_OfTotal.Text = "T: [048.04%]";
            this.iLbl_Footer_Percentage_OfTotal.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.iLbl_Footer_Percentage_OfTotal.TextChanged += new System.EventHandler( this.iLbl_TextChanged );
            // 
            // iLbl_Footer_Percentage_OfType
            // 
            this.iLbl_Footer_Percentage_OfType.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iLbl_Footer_Percentage_OfType.BackColor = System.Drawing.Color.Gainsboro;
            this.iLbl_Footer_Percentage_OfType.Font = new System.Drawing.Font( "Tahoma", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iLbl_Footer_Percentage_OfType.ForeColor = System.Drawing.Color.SlateGray;
            this.iLbl_Footer_Percentage_OfType.Location = new System.Drawing.Point( 248, 120 );
            this.iLbl_Footer_Percentage_OfType.Name = "iLbl_Footer_Percentage_OfType";
            this.iLbl_Footer_Percentage_OfType.Size = new System.Drawing.Size( 96, 22 );
            this.iLbl_Footer_Percentage_OfType.TabIndex = 3;
            this.iLbl_Footer_Percentage_OfType.Text = "F: [089.49%]";
            this.iLbl_Footer_Percentage_OfType.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.iLbl_Footer_Percentage_OfType.TextChanged += new System.EventHandler( this.iLbl_TextChanged );
            // 
            // PopupBase
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 14 );
            this.AutoScroll = true;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.CausesValidation = false;
            this.ClientSize = new System.Drawing.Size( 520, 144 );
            this.ControlBox = false;
            this.Controls.Add( this.iLbl_Footer_Percentage_OfType );
            this.Controls.Add( this.iLbl_Footer_Percentage_OfTotal );
            this.Controls.Add( this.iLbl_Footer );
            this.Controls.Add( this.iLbl_Title_Length_Header );
            this.Controls.Add( this.iLbl_Title_Length_Payload );
            this.Controls.Add( this.iLbl_Title );
            this.Enabled = false;
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "PopupBase";
            this.Opacity = 0.85;
            this.ShowInTaskbar = false;
            this.TopMost = true;
            this.Layout += new System.Windows.Forms.LayoutEventHandler( this.PopupBase_Layout );
            this.KeyDown += new System.Windows.Forms.KeyEventHandler( this.PopupBase_KeyDown );
            this.ResumeLayout( false );

        }
        #endregion

        #region Internal constants
        #endregion

        #region API
        public void PopupShowAsync( Point aLocation, HeapCell aCell, RawItem aItem, HeapStatistics aStats, Size aOffset, System.Windows.Forms.KeyEventHandler aKeyHandler )
        {
            //System.Diagnostics.Debug.WriteLine( "PopupBase - Timer Started" );
            iKeyHandler = aKeyHandler;

            HeapCellArrayWithStatistics array = new HeapCellArrayWithStatistics();
            array.Add( aCell );
            PrepareContent( array, aStats, aItem );
            //
            iHoverPos = aLocation;
            //
            iShowPos = aLocation;
            iShowPos.Offset( aOffset.Width, aOffset.Height );

            // If we are using an async timer delay then we must start the timer and
            // when it expires, the popup will become visible. Otherwise, we show
            // the popup immediately.
            if ( VisibilityDelay > 0 )
            {
                iTimer.Stop();
                iTimer.Enabled = true;
                iTimer.Start();
            }
            else
            {
                PopupShow();
            }
        }

        public void PopupShowAsync( Point aLocation, HeapCellArrayWithStatistics aCells, HeapStatistics aStats, Size aOffset, System.Windows.Forms.KeyEventHandler aKeyHandler )
        {
            //System.Diagnostics.Debug.WriteLine( "PopupBase - Timer Started" );
            iKeyHandler = aKeyHandler;
            PrepareContent( aCells, aStats );
            //
            iHoverPos = aLocation;
            //
            iShowPos = aLocation;
            iShowPos.Offset( aOffset.Width, aOffset.Height );
            
            // If we are using an async timer delay then we must start the timer and
            // when it expires, the popup will become visible. Otherwise, we show
            // the popup immediately.
            if  ( VisibilityDelay > 0 )
            {
                iTimer.Stop();
                iTimer.Enabled = true;
                iTimer.Start();
            }
            else
            {
                PopupShow();
            }
        }

        public void PopupRelocate( Point aLocation, HeapCellArrayWithStatistics aCells, HeapStatistics aStats, Size aOffset )
        {
            PrepareContent( aCells, aStats );
            Invalidate();

            iShowPos = aLocation;
            iShowPos.Offset( aOffset.Width, aOffset.Height );

            // Get the screen size
            Rectangle rect = Screen.GetWorkingArea( this );

            // Make sure that the popup is displayed within the screen bounds.
            Point pos = iShowPos;
            if  ( iShowPos.X + this.Width > rect.Right )
            {
                pos.X = rect.Width - this.Width;
            }
            if  ( iShowPos.Y + this.Height > rect.Bottom )
            {
                pos.Y = rect.Bottom - this.Height;
            }
            Location = pos;
        }

        public void PopupHide()
        {
            //System.Diagnostics.Debug.WriteLine( "PopupBase - Timer Cancelled" );
            iTimer.Stop();
            iTimer.Enabled = false;
            iKeyHandler = null;
            //
            if  ( Visible )
            {
                //System.Diagnostics.Debug.WriteLine( "PopupBase - POPUP HIDDEN!" );
                Hide();
            }
        }
        #endregion

        #region Framework API
        public virtual void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats )
        {
            // Set title and border colour 
            HeapCell firstCell = aCells[ 0 ];
            ColourTitle = HeapCtrlLib.Renderers.Utilities.HeapCellRendererColour.ColourByCellType( firstCell );
            ColourBorder = ColourUtils.Darken( ColourTitle );

            // Get stats
            long lengthPayload = aCells.Statistics.SizeTotalPayload;
            long lengthHeader = aCells.Statistics.SizeTotalHeader;
            float lengthsAsHeapPercentage = aStats.CellLengthAsHeapPercentage( lengthHeader + lengthPayload );

            // Header length
            iLbl_Title_Length_Header.Text = "H: [" + lengthHeader.ToString("d6") + "]";

            // Payload length
            iLbl_Title_Length_Payload.Text = "P: [" + lengthPayload.ToString("d8") + "]";

            // Set cell allocation number (for allocated cells) or then
            // the cell index for free cells.
            string heapSpecificPrefix = "H: ";
            if  ( aCells.Count == 1 )
            {
                float lengthsAsTypePercentage = aStats.CellLengthAsTypePercentage( firstCell );
                //
                string typeSpecificPrefix = "A: ";
                if ( firstCell.Type == HeapCell.TType.EFree )
                {
                    typeSpecificPrefix = "F: ";
                }
                //
                if ( HeapCell.IsDebugAllocator )
                {
                    switch ( firstCell.Type )
                    {
                    case HeapCell.TType.EAllocated:
                        iLbl_Footer.Text = "Alloc #: [" + firstCell.AllocationNumber.ToString( "d6" ) + " / " + aStats.StatsAllocated.CellAllocationNumberLargest.AllocationNumber.ToString( "d6" ) + "]";
                        break;
                    case HeapCell.TType.EFree:
                        iLbl_Footer.Text = "Free cell #: [" + firstCell.AllocationNumber.ToString( "d4" ) + " / " + aStats.StatsFree.TypeCount.ToString( "d4" ) + "]";
                        break;
                    }
                }
                else
                {
                    iLbl_Footer.Text = "[ " + firstCell.Address.ToString( "x8" ) + " ]";
                }
                //
                iLbl_Footer_Percentage_OfType.Text = typeSpecificPrefix + "[" + lengthsAsTypePercentage.ToString( "#00.00" ) + "%]";
            }
            else
            {
                heapSpecificPrefix = "Of Heap: ";
                iLbl_Footer.Text = string.Empty;
                iLbl_Footer_Percentage_OfType.Visible = false;
            }
            //
            iLbl_Footer_Percentage_OfTotal.Text = heapSpecificPrefix + "[" + lengthsAsHeapPercentage.ToString( "#00.00" ) + "%]";
        }

        public virtual void PrepareContent( HeapCellArrayWithStatistics aCells, HeapStatistics aStats, RawItem aRawItem )
        {
            PrepareContent( aCells, aStats );
        }
        #endregion

        #region Properties
        public bool ColourFromHoverCoordinate
        {
            get { return iColourFromHoverCoordinate; }
            set { iColourFromHoverCoordinate = value; }
        }

        public int VisibilityDelay
        {
            get { return iVisibilityDelay; }
            set
            {
                iVisibilityDelay = value;
                if  ( iVisibilityDelay > 0 )
                {
                    iTimer.Interval = iVisibilityDelay;
                }
            }
        }

        public Color ColourBackground
        {
            get { return iColourBackground; }
            set
            {
                iColourBackground = value;
                //
                if  ( ColourChangeBackground != null )
                {
                    ColourChangeBackground( value );
                }
            }
        }

        public Color ColourTitle
        {
            get { return iLbl_Title.BackColor; }
            set
            {
                iLbl_Title.BackColor = value;
                iLbl_Title_Length_Header.BackColor = value;
                iLbl_Title_Length_Payload.BackColor = value;
                
                // Make the footer slightly lighter
                iLbl_Footer.BackColor = ColourUtils.Lighten( value );
                iLbl_Footer_Percentage_OfTotal.BackColor = iLbl_Footer.BackColor;
                iLbl_Footer_Percentage_OfType.BackColor = iLbl_Footer.BackColor;
                //
                if  ( ColourChangeTitle != null )
                {
                    ColourChangeTitle( value );
                }
            }
        }

        public Color ColourBorder
        {
            get { return iColourBorder; }
            set { iColourBorder = value; }
        }

        public string Title
        {
            get { return iLbl_Title.Text; }
            set { iLbl_Title.Text = value; }
        }
        
        public Rectangle ClientRect
        {
            get
            {
                Size paddingSize = new Size( 2, 2 );

                // Calculate bottom right corner position
                Point bottomRightPos = new Point();
                bottomRightPos.X = ( Width - 1 ) - paddingSize.Width;
                bottomRightPos.Y = ( Height - 1 - iLbl_Footer.Height - 1 ) - paddingSize.Height;

                // Calculate top left
                Point topLeft = new Point();
                topLeft.X = 1 + paddingSize.Width;
                topLeft.Y = 1 + iLbl_Title.Height + 1 + paddingSize.Height;

                // Set rect
                Rectangle ret = new Rectangle();
                ret.Location = topLeft;
                ret.Size = new Size( bottomRightPos.X - topLeft.X, bottomRightPos.Y - topLeft.Y );
 
                return ret;
            }
        }
        #endregion

        #region Drawing
        protected override void OnPaint( PaintEventArgs aArgs )
        {
            aArgs.Graphics.Clear( ColourBackground );					
            //
            using( Pen borderPen = new Pen( ColourBorder ) )
            {
                // Border
                aArgs.Graphics.DrawRectangle( borderPen, 0, 0, Width-1, Height-1 );

                int yPos;

                // Draw a line underneath the title label
                yPos = iLbl_Title.Top + iLbl_Title.Height;
                aArgs.Graphics.DrawLine( borderPen, 0, yPos, Width, yPos );

                // Draw a line above the footer label
                yPos = iLbl_Footer.Top - 1;
                aArgs.Graphics.DrawLine( borderPen, 0, yPos, Width, yPos );
            }
            //
            base.OnPaint( aArgs );
        }
        #endregion

        #region Events
        private void iTimer_Tick( object sender, System.EventArgs e )
        {
            iTimer.Stop();
            iTimer.Enabled = false;
            //
            PopupShow();
            //System.Diagnostics.Debug.WriteLine( "Timer TICK: [ " + Location.X + ", " + Location.Y + " ] -> Popup Shown" );
        }

        private void PopupBase_KeyDown(object sender, KeyEventArgs e)
        {
            if  ( iKeyHandler != null )
            {
                iKeyHandler( this, e );
            }
        }

        private void PopupBase_Layout(object sender, System.Windows.Forms.LayoutEventArgs e)
        {
            iLbl_Title_Length_Payload.Location = new Point( Width - iLbl_Title_Length_Payload.Width - 1, 1 );
            iLbl_Title_Length_Header.Location = new Point( iLbl_Title_Length_Payload.Location.X - iLbl_Title_Length_Header.Width - 10, 1 );
            //
            iLbl_Title.Location = new Point( 1, 1 );
            iLbl_Title.Width = Width - 2;
            iLbl_Title.SendToBack();
            //
            iLbl_Footer.Location = new Point( 1, Height - iLbl_Footer.Height - 1 );
            iLbl_Footer.Width = Width - 2;
            //
            iLbl_Footer_Percentage_OfTotal.Location = new Point( Width - iLbl_Footer_Percentage_OfTotal.Width - 1, iLbl_Footer.Location.Y );
            iLbl_Footer_Percentage_OfType.Location = new Point( iLbl_Footer_Percentage_OfTotal.Location.X - iLbl_Footer_Percentage_OfType.Width - 10, iLbl_Footer.Location.Y );
        }

        private void iLbl_TextChanged( object sender, System.EventArgs e )
        {
            Label lbl = (Label) sender;
            lbl.Width = lbl.PreferredWidth;
            //System.Diagnostics.Debug.WriteLine( "Label [" + lbl.Text + "], width now: " + lbl.Width + ", preferredWid: " + lbl.PreferredWidth );
        }
        #endregion

        #region Internal methods
        private void PopupShow()
        {
            //System.Diagnostics.Debug.WriteLine( "PopupBase - POPUP SHOW NOW - Visible: " + Visible + ", iShowPos[ " + iShowPos.X + ", " + iShowPos.Y + " ]" );

            if  ( !Visible )
            {
                // Should we colourise the form header & footer
                // based upon the mouse co-ordinates at the time of
                // the asynch display?
                if  ( ColourFromHoverCoordinate )
                {
                    using( Bitmap bmp = new Bitmap( 1, 1 ) )
                    {
                        using( Graphics gfx = Graphics.FromImage( bmp ) )
                        {
                            Color c = SymbianUtils.Graphics.ScreenUtils.ColorAtPixel( iHoverPos );
                            ColourTitle = c;

                            // Make the border slightly darker
                            ColourBorder = ColourUtils.Darken( ColourTitle );
                        }
                    }
                }

                // Get the screen size
                Rectangle rect = Screen.GetWorkingArea( this );

                // Make sure that the popup is displayed within the screen bounds.
                Point pos = iShowPos;
                if  ( iShowPos.X + this.Width > rect.Right )
                {
                    pos.X = rect.Width - this.Width;
                }
                if  ( iShowPos.Y + this.Height > rect.Bottom )
                {
                    pos.Y = rect.Bottom - this.Height;
                }

                // Now make the form visible and topmost.
                ShowWindow( this.Handle, SW_SHOWNOACTIVATE );
                SetWindowPos( this.Handle, HWND_TOP_MOST, pos.X, pos.Y, 0, 0, SWP_NOMOVE | SWP_NOSIZE );
                Location = pos;
            }
        }
        #endregion

        #region Unmanaged code
        private static IntPtr HWND_TOP_MOST = (IntPtr)(-1);
        private static IntPtr HWND_NOTOPMOST = (IntPtr)(-2);
        private const uint SWP_NOMOVE = 0x2;
        private const uint SWP_NOSIZE = 0x1;
        private const int SW_SHOWNOACTIVATE = 4;

        [DllImport("user32.dll")]
        public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int x, int y, int cx, int cy, uint uFlags);
        [DllImport("user32.dll")]
        public  static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
        #endregion

        #region Data members
        private int iVisibilityDelay = 700;
        private bool iColourFromHoverCoordinate = false;
        private Point iHoverPos;
        private Point iShowPos;
        private Color iColourBackground = Color.White;
        private Color iColourBorder = Color.Black;
        private System.Windows.Forms.KeyEventHandler iKeyHandler = null;
        #endregion
    }
}
