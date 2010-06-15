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
using HeapLib.Relationships;
using HeapUiLib.Forms;

namespace HeapUiLib.SubForms
{
	public class HeapCellRelationshipInspectorForm : System.Windows.Forms.Form
	{
		#region Windows Form Designer Code
        private System.Windows.Forms.Timer iTimer_RefreshRawItems;
        private TableLayoutPanel iTL_Main;
        private TreeView iTreeView;
        private TableLayoutPanel iTL_Middle;
        private ComboBox iCombo_SizingType;
        private Button iButton_GoToSelectedCell;
        private TextBox iTextBox_CombinedSize;
		private System.ComponentModel.IContainer components;
		#endregion

		#region Constructors & destructors
        public HeapCellRelationshipInspectorForm( HeapViewerForm aMainForm, XmlSettings aSettings )
		{
			iMainForm = aMainForm;
			iMainForm.HeapCellSelectedObserver += new HeapUiLib.Forms.HeapViewerForm.HeapCellSelectedObserverHandler(iMainForm_HeapCellSelectedObserver);
			iMainForm.Closing += new CancelEventHandler(iMainForm_Closing);
			//
			iCollection = aMainForm.CellCollection;
			iSettings = aSettings;
			//
			InitializeComponent();
		}

		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				iMainForm.HeapCellSelectedObserver -= new HeapUiLib.Forms.HeapViewerForm.HeapCellSelectedObserverHandler(iMainForm_HeapCellSelectedObserver);
				iMainForm.Closing -= new CancelEventHandler(iMainForm_Closing);

				if  ( components != null )
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
            this.iTimer_RefreshRawItems = new System.Windows.Forms.Timer( this.components );
            this.iTL_Main = new System.Windows.Forms.TableLayoutPanel();
            this.iTreeView = new System.Windows.Forms.TreeView();
            this.iTL_Middle = new System.Windows.Forms.TableLayoutPanel();
            this.iButton_GoToSelectedCell = new System.Windows.Forms.Button();
            this.iTextBox_CombinedSize = new System.Windows.Forms.TextBox();
            this.iCombo_SizingType = new System.Windows.Forms.ComboBox();
            this.iTL_Main.SuspendLayout();
            this.iTL_Middle.SuspendLayout();
            this.SuspendLayout();
            // 
            // iTL_Main
            // 
            this.iTL_Main.ColumnCount = 1;
            this.iTL_Main.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTL_Main.Controls.Add( this.iTreeView, 0, 0 );
            this.iTL_Main.Controls.Add( this.iTL_Middle, 0, 1 );
            this.iTL_Main.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTL_Main.Location = new System.Drawing.Point( 0, 0 );
            this.iTL_Main.Margin = new System.Windows.Forms.Padding( 0 );
            this.iTL_Main.Name = "iTL_Main";
            this.iTL_Main.RowCount = 2;
            this.iTL_Main.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 50F ) );
            this.iTL_Main.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Absolute, 30F ) );
            this.iTL_Main.Size = new System.Drawing.Size( 355, 284 );
            this.iTL_Main.TabIndex = 19;
            // 
            // iTreeView
            // 
            this.iTreeView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTreeView.Font = new System.Drawing.Font( "Lucida Console", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iTreeView.Location = new System.Drawing.Point( 3, 3 );
            this.iTreeView.Name = "iTreeView";
            this.iTreeView.Size = new System.Drawing.Size( 349, 248 );
            this.iTreeView.TabIndex = 1;
            this.iTreeView.BeforeExpand += new System.Windows.Forms.TreeViewCancelEventHandler( this.iTreeView_BeforeExpand );
            this.iTreeView.DoubleClick += new System.EventHandler( this.iTreeView_DoubleClick );
            this.iTreeView.AfterSelect += new System.Windows.Forms.TreeViewEventHandler( this.iTreeView_AfterSelect );
            // 
            // iTL_Middle
            // 
            this.iTL_Middle.ColumnCount = 3;
            this.iTL_Middle.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 65F ) );
            this.iTL_Middle.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 35F ) );
            this.iTL_Middle.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Absolute, 108F ) );
            this.iTL_Middle.Controls.Add( this.iButton_GoToSelectedCell, 0, 0 );
            this.iTL_Middle.Controls.Add( this.iTextBox_CombinedSize, 0, 0 );
            this.iTL_Middle.Controls.Add( this.iCombo_SizingType, 0, 0 );
            this.iTL_Middle.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTL_Middle.Location = new System.Drawing.Point( 0, 254 );
            this.iTL_Middle.Margin = new System.Windows.Forms.Padding( 0 );
            this.iTL_Middle.MaximumSize = new System.Drawing.Size( 0, 27 );
            this.iTL_Middle.MinimumSize = new System.Drawing.Size( 0, 27 );
            this.iTL_Middle.Name = "iTL_Middle";
            this.iTL_Middle.RowCount = 1;
            this.iTL_Middle.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iTL_Middle.Size = new System.Drawing.Size( 355, 27 );
            this.iTL_Middle.TabIndex = 2;
            // 
            // iButton_GoToSelectedCell
            // 
            this.iButton_GoToSelectedCell.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iButton_GoToSelectedCell.Location = new System.Drawing.Point( 249, 3 );
            this.iButton_GoToSelectedCell.Name = "iButton_GoToSelectedCell";
            this.iButton_GoToSelectedCell.Size = new System.Drawing.Size( 103, 21 );
            this.iButton_GoToSelectedCell.TabIndex = 20;
            this.iButton_GoToSelectedCell.Text = "Go to cell...";
            this.iButton_GoToSelectedCell.Click += new System.EventHandler( this.iButton_GoToSelectedCell_Click );
            // 
            // iTextBox_CombinedSize
            // 
            this.iTextBox_CombinedSize.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTextBox_CombinedSize.Enabled = false;
            this.iTextBox_CombinedSize.Location = new System.Drawing.Point( 163, 3 );
            this.iTextBox_CombinedSize.Name = "iTextBox_CombinedSize";
            this.iTextBox_CombinedSize.Size = new System.Drawing.Size( 80, 21 );
            this.iTextBox_CombinedSize.TabIndex = 19;
            // 
            // iCombo_SizingType
            // 
            this.iCombo_SizingType.DisplayMember = "Text";
            this.iCombo_SizingType.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iCombo_SizingType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.iCombo_SizingType.Items.AddRange( new object[] {
            "Payload size",
            "Payload size (including children)",
            "Payload size (just children)",
            "Payload size (recursive)"} );
            this.iCombo_SizingType.Location = new System.Drawing.Point( 3, 3 );
            this.iCombo_SizingType.MaxDropDownItems = 4;
            this.iCombo_SizingType.Name = "iCombo_SizingType";
            this.iCombo_SizingType.Size = new System.Drawing.Size( 154, 21 );
            this.iCombo_SizingType.TabIndex = 18;
            this.iCombo_SizingType.ValueMember = "Text";
            this.iCombo_SizingType.SelectedIndexChanged += new System.EventHandler( this.iCombo_SizingType_SelectedIndexChanged );
            // 
            // HeapCellRelationshipInspectorForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 14 );
            this.ClientSize = new System.Drawing.Size( 355, 284 );
            this.Controls.Add( this.iTL_Main );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.MinimumSize = new System.Drawing.Size( 363, 308 );
            this.Name = "HeapCellRelationshipInspectorForm";
            this.Text = "Embedded Cell References";
            this.TopMost = true;
            this.Load += new System.EventHandler( this.HeapCellRelationshipInspectorForm_Load );
            this.Closing += new System.ComponentModel.CancelEventHandler( this.HeapCellRelationshipInspectorForm_Closing );
            this.iTL_Main.ResumeLayout( false );
            this.iTL_Middle.ResumeLayout( false );
            this.iTL_Middle.PerformLayout();
            this.ResumeLayout( false );

		}
		#endregion
	
		#region API
		public HeapCell MainFormSelectedCell
		{
			get
			{
				return iMainForm.FocusedCell;
			}
		}
		#endregion

		#region Form loading & closing event handlers
        private void HeapCellRelationshipInspectorForm_Load( object sender, System.EventArgs e )
		{
			Point position = new Point();
            position.X = iSettings.Load( "HeapCellRelationshipInspectorForm", "WindowPositionX", 0 );
            position.Y = iSettings.Load( "HeapCellRelationshipInspectorForm", "WindowPositionY", 0 );
			Location = position;
			//
			iCombo_SizingType.SelectedIndex = 0;
            //
            iMainForm_HeapCellSelectedObserver( MainFormSelectedCell );
		}

        private void HeapCellRelationshipInspectorForm_Closing( object sender, System.ComponentModel.CancelEventArgs e )
		{
            iSettings.Save( "HeapCellRelationshipInspectorForm", "WindowPositionX", Location.X );
            iSettings.Save( "HeapCellRelationshipInspectorForm", "WindowPositionY", Location.Y );
		}
		#endregion

		#region Main Form event handlers
		private void iMainForm_HeapCellSelectedObserver( HeapCell aCell )
		{
            // The focused heap cell on the main form has changed. 
			PopulateTree( aCell );
			UpdateSizingTextBoxByType();

			// Make sure the tree view selects the top (root) node
			if	( iTreeView.Nodes.Count > 0 )
			{
				iTreeView.SelectedNode = iTreeView.Nodes[ 0 ];
			}
        }

		private void iMainForm_Closing(object sender, CancelEventArgs e)
		{
			// Close ourselves when our parent closes.
			DialogResult = DialogResult.OK;
			Close();
		}
		#endregion

		#region Tree event handlers
		private void iTreeView_AfterSelect( object sender, System.Windows.Forms.TreeViewEventArgs e )
		{
			if	( e.Node.Tag != null && e.Node.Tag is HeapCell )
			{
				HeapCell cell = (HeapCell) e.Node.Tag;
			
				// Update the raw cell view
                //ContentsViewerCell = cell;

				// Update the combined sizing (guesstimate)
				UpdateSizingTextBoxByType();
			}
		}

        private void iTreeView_BeforeExpand( object sender, TreeViewCancelEventArgs e )
        {
            if ( e.Node.Nodes.Count == 1 && e.Node.FirstNode.Text == KPlaceHolderTreeNodeText )
            {
                // Its a place holder. Let's fix it with the real tree items.
                HeapCell cell = (HeapCell) e.Node.Tag;

                // Start update operation so that we don't redraw until the entries
                // have been added.
                iTreeView.BeginUpdate();

                // Add real entries
                PopulateBranch( cell, e.Node );

                // Discard first child node which was our placeholder
                e.Node.FirstNode.Remove();

                // End process
                iTreeView.EndUpdate();
            }
        }

		private void iTreeView_DoubleClick( object sender, System.EventArgs e )
		{
		}
		#endregion

		#region Internal tree methods
        private static TreeNode CreateChildNode( HeapCell aParent, HeapCell aCell, RelationshipInfo aRelationshipDescriptor /* optional */ )
		{
            StringBuilder caption = new StringBuilder();
            caption.Append( "[" + aCell.Address.ToString( "x8" ) + "] " );
            if ( aRelationshipDescriptor != null )
            {
                caption.Append( "[" + aRelationshipDescriptor.LinkAddressOffsetWithinFromCell.ToString( "d4" ) + "] " );
            }
            caption.Append( "[" + aCell.PayloadLength.ToString( "d6" ) + "] " );
			//
			System.Drawing.Color foreColour = Color.Black;
            if ( aCell.Symbol != null )
			{
                caption.Append( aCell.Symbol.NameWithoutVTablePrefix );
				foreColour = Color.Blue;
			}
			else
			{
				caption.Append( "No symbol" );
				foreColour = Color.LightGray;
			}
			//
			TreeNode node = new TreeNode( caption.ToString() );
			node.ForeColor = foreColour;
			return node;
		}

		private void PopulateTree( HeapCell aCell )
		{
			iTreeView.BeginUpdate();
			iTreeView.Nodes.Clear();

			// First add the main parent node
            TreeNode topNode = CreateChildNode( null, aCell, null );
			topNode.Tag = aCell;
			topNode.Expand();
			//
			PopulateBranch( aCell, topNode );
			//
			iTreeView.Nodes.Add( topNode );
			iTreeView.EndUpdate();
		}
		
		private bool IsCyclic( HeapCell aCell, TreeNode aNode ) 
		{
			bool cyclic = false;
			TreeNode currentNode = aNode;
			//
			while( currentNode.Parent != null )
			{
				if	( currentNode.Parent.Tag != null && currentNode.Parent.Tag is HeapCell )
				{
					HeapCell parentCell = (HeapCell) currentNode.Parent.Tag;
					if	( parentCell.Address == aCell.Address )
					{
						cyclic = true;
						break;
					}
				}
				currentNode = currentNode.Parent;
			}
			//
			return cyclic;
		}

		private void PopulateBranch( HeapCell aCell, TreeNode aParentNode )
		{
            RelationshipCollection linkedCells = aCell.RelationshipManager.EmbeddedReferencesTo;
		    //
            if ( linkedCells.Count == 0 && aParentNode.Parent == null )
			{
				TreeNode noChildrenNode = new TreeNode( "Has no linked items" );
				noChildrenNode.Tag = null;
				aParentNode.Nodes.Add( noChildrenNode );
			}
			else if ( linkedCells.Count > 0 )
			{
                foreach ( RelationshipInfo relationshipInfo in linkedCells ) 
				{
                    TreeNode childNode = CreateChildNode( aCell, relationshipInfo.ToCell, relationshipInfo );
                    //
					childNode.Tag = relationshipInfo.ToCell;
					aParentNode.Nodes.Add( childNode );
					//
                    if ( IsCyclic( relationshipInfo.ToCell, childNode ) == false )
					{
                        if ( aParentNode.Level < KMaxRecursiveDepth )
                        {
                            int childRelationshipCount = relationshipInfo.ToCell.RelationshipManager.EmbeddedReferencesTo.Count;
                            if ( childRelationshipCount > 0 )
                            {
                                // Make a place holder
                                TreeNode placeholder = new TreeNode( KPlaceHolderTreeNodeText );
                                childNode.Nodes.Add( placeholder );
                            }
                        }
                        else
                        {
						    childNode.Text += " [Max. Depth Exceeded]";
						    childNode.ForeColor = Color.Red;
                        }
					}
					else
					{
						childNode.Text += " [C]";
						childNode.ForeColor = Color.Red;
					}
				}
			}
		}
		#endregion

		#region Sizing (combo) event handler
		private enum TSizingType
		{
			ESizingTypePayloadSize = 0,
			ESizingTypePayloadSizeIncludingChildren,
			ESizingTypePayloadSizeJustChildren,
			ESizingTypePayloadSizeRecursive
		}

		private void UpdateSizingTextBoxByType()
		{
			int index = iCombo_SizingType.SelectedIndex;
			TSizingType type = (TSizingType) index;
			UpdateSizingTextBoxByType( type );
		}

		private void UpdateSizingTextBoxByType( TSizingType aType )
		{
			string size = string.Empty;
			//
			TreeNode node = iTreeView.SelectedNode;
			if	( node != null && node.Tag != null && node.Tag is HeapCell )
			{
				HeapCell selectedCell = (HeapCell) node.Tag;
				//
				switch( aType )
				{
				case TSizingType.ESizingTypePayloadSize:
					size = selectedCell.PayloadLength.ToString( "d12" );
					break;
				case TSizingType.ESizingTypePayloadSizeIncludingChildren:
					size = selectedCell.PayloadLengthIncludingLinkedCells.ToString( "d12" );
					break;
				case TSizingType.ESizingTypePayloadSizeJustChildren:
					size = ( selectedCell.PayloadLengthIncludingLinkedCells - selectedCell.PayloadLength ).ToString( "d12" );
					break;
				case TSizingType.ESizingTypePayloadSizeRecursive:
					size = selectedCell.CombinedLinkedCellPayloadLengths.ToString( "d12" );
					break;
				default:
					break;
				}
			}
			//
			iTextBox_CombinedSize.Text = size;
		}

		private void iCombo_SizingType_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			UpdateSizingTextBoxByType();
		}
		#endregion

		#region Button event handlers
		private void iButton_GoToSelectedCell_Click(object sender, System.EventArgs e)
		{
			TreeNode node = iTreeView.SelectedNode;
			if	( node != null && node.Tag != null && node.Tag is HeapCell )
			{
				HeapCell selectedCell = (HeapCell) node.Tag;
				iMainForm.FocusedCell = selectedCell;
			}
		}
		#endregion

        #region Internal constants
        private const int KMaxRecursiveDepth = 10;
        private const string KPlaceHolderTreeNodeText = "#!/|PLACE HOLDER!!";
        #endregion

        #region Data members
        private readonly HeapCellArray iCollection;
		private readonly HeapViewerForm iMainForm;
		private readonly XmlSettings iSettings;
		#endregion
    }
}
