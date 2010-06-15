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
using System.Windows.Forms;
using System.IO;
using System.Text;
using SymbianUtils;
using SymbianUtils.Settings;
using SymbianUtils.XRef;
using SymbianUtils.RawItems;
using SymbianUtils.FileSystem.FilePair;
using SymbianZipLib.GUI;
using ZedGraph;
using HeapLib;
using HeapLib.Cells;
using HeapLib.Array;
using HeapLib.Relationships;
using HeapLib.Reconstructor;
using HeapLib.Statistics.Tracking.Base;
using HeapLib.Statistics.Tracking.BySymbol;
using HeapLib.Reconstructor.DataSources;
using HeapUiLib.Dialogs;
using HeapUiLib.SubForms;
using HeapUiLib.Controls;
using HeapUiLib.Misc;
using HeapCtrlLib;

namespace HeapUiLib.Forms
{
	public class HeapViewerForm : System.Windows.Forms.Form
	{
		#region Events
		public delegate void HeapCellSelectedObserverHandler( HeapCell aCell );
		public event HeapCellSelectedObserverHandler HeapCellSelectedObserver;
		#endregion

		#region Windows Form Designer generated code
		private System.Windows.Forms.Timer iTimer_CreateToolBoxItems;
		private System.Windows.Forms.MainMenu iMainMenu;
		private System.Windows.Forms.MenuItem iMenuItem_File;
		private System.Windows.Forms.MenuItem iMenuItem_Help;
		private System.Windows.Forms.MenuItem iMenuItem_Help_About;
		private System.Windows.Forms.MenuItem iMenuItem_File_Exit;
		private System.Windows.Forms.MenuItem menuItem6;
		private System.Windows.Forms.MenuItem iMenuItem_View;
		private System.Windows.Forms.MenuItem iMenuItem_View_Show_RelationshipInspector;
		private System.Windows.Forms.MenuItem iMenuItem_View_Show_CellContentsViewer;
		private System.Windows.Forms.MenuItem iMenuItem_View_Show;
		private System.Windows.Forms.Panel iPanel_Main;
		private System.Windows.Forms.TabControl iTabs;
		private System.Windows.Forms.TabPage iPG1_HeapView;
        private System.Windows.Forms.TabPage iPG3_Objects;
		private System.Windows.Forms.MenuItem iMenuItem_File_SaveAs;
        private System.Windows.Forms.MenuItem iMenuItem_File_SaveAs_Zip;
		private XPTable.Models.ColumnModel iTab2_ColModel_Stats;
		private XPTable.Models.TableModel iTab2_TabModel_Stats;
		private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_Symbol;
		private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_AllocationCount;
		private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_MemoryPerInstance;
		private XPTable.Models.TextColumn iTab2_ColModel_Stats_Col_TotalAllocatedMemory;
		private ZedGraph.ZedGraphControl iPG3_ObjectPieChart;
        private System.Windows.Forms.TabPage iPG2_CellList;
		private System.Windows.Forms.MenuItem iMenuItem_File_SaveAs_CSV;
        private System.Windows.Forms.MenuItem iMenuItem_File_SaveAs_Text;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.Button iPG1_BT_Search_ByAllocNumber;
        private System.Windows.Forms.NumericUpDown iPG1_NumUD_Search_ByAllocNumber;
        private System.Windows.Forms.TextBox iPG1_TB_Search_ByAddress;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button iPG1_BT_Search_ByAddress;
        private System.Windows.Forms.GroupBox groupBox6;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.RadioButton iPG1_RB_HeapView_ByCellType;
        private HeapCtrlLib.HeapDataControl iPG1_HeapView_Viewer;
        private System.Windows.Forms.RadioButton iPG1_RB_HeapView_ByObjectType;
        private System.Windows.Forms.RadioButton iPG1_RB_HeapView_ByParentBinary;
        private System.Windows.Forms.RadioButton iPG1_RB_HeapView_ByCellLength;
        private System.Windows.Forms.RadioButton iPG1_RB_HeapView_ByCellAge;
        private System.Windows.Forms.TabPage iPG4_Graphs;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type_Cell;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type_Object;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type_ParentBinary;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type_Length;
        private System.Windows.Forms.MenuItem iMenuItem_View_Type_Age;
        private ZedGraph.ZedGraphControl iPG4_GraphCtrl;
        private System.Windows.Forms.RadioButton iPG4_RB_GraphType_SizeByIndex;
        private System.Windows.Forms.RadioButton iPG4_RB_GraphType_CellSizeFree;
        private System.Windows.Forms.RadioButton iPG4_RB_GraphType_CellSizeAllocated;
        private System.Windows.Forms.RadioButton iPG4_RB_GraphType_CellOverhead;
        private MenuItem iMenuItem_View_Zoom;
        private MenuItem iMenuItem_View_Zoom_Small;
        private MenuItem iMenuItem_View_Zoom_Medium;
        private MenuItem iMenuItem_View_Zoom_Large;
        private SplitContainer iPG2_SplitCon;
        private HeapUiLib.Controls.HeapCellListingControl iPG2_CellList_Cells;
        private RadioButton iPG4_RB_GraphType_AssociatedBinary;
        private GroupBox iPG1_GP_Navigate_ByAllocNumber;
        private Button iPG1_BT_Navigate_ByAllocNumber_Next;
        private Button iPG1_BT_Navigate_ByAllocNumber_Previous;
        private GroupBox groupBox4;
        private Button iPG1_BT_Navigate_ByPosition_Next;
        private Button iPG1_BT_Navigate_ByPosition_Previous;
        private GroupBox groupBox5;
        private Button iPG1_BT_HeapView_Filters;
        private TabPage iPG5_Distribution;
        private XPTable.Models.TableModel iPG5_TableModel_CellDistributionFree;
        private XPTable.Models.TableModel iPG5_TableModel_CellDistributionAllocated;
        private RadioButton iPG1_RB_HeapView_ByIsolation;
        private RadioButton iPG1_RB_HeapView_ByEmbeddedReferences;
        private TabPage iPG6_Relationships;
        private HeapCellRelationshipControl iPG6_RelationshipControl;
        private HeapCellListingFilter iPG2_FilterControl;
        private ContextMenuStrip iPG1_HeapView_ContextMenu;
        private ToolStripMenuItem iPopupMenu_Relationships;
        private ToolStripMenuItem iPopupMenu_Show;
        private ToolStripMenuItem iPopupMenu_Show_CellContentsViewer;
        private ToolStripMenuItem iPopupMenu_Show_CellRelationshipInspector;
        private ToolStripMenuItem iPopupMenu_XRef;
        private ToolStripMenuItem iPopupMenu_XRef_Settings;
        private MenuItem iMenuItem_View_Type_Isolation;
        private MenuItem iMenuItem_View_Type_EmbeddedReferences;
        private ToolStripMenuItem iPopupMenu_GoTo;
        private ToolStripMenuItem iPopupMenu_GoTo_EmbeddedReferences;
        private ToolStripMenuItem iPopupMenu_GoTo_IncomingReferences;
        private ToolStripMenuItem iPopupMenu_Relationships_Outgoing;
        private ToolStripMenuItem iPopupMenu_Relationships_Outgoing_View;
        private ToolStripMenuItem iPopupMenu_Relationships_Incoming;
        private ToolStripMenuItem iPopupMenu_Relationships_Incoming_View;
        private ToolStripMenuItem iPopupMenu_Relationships_Incoming_Breadcrumbs;
        private ToolStripMenuItem iPopupMenu_Relationships_Outgoing_Breadcrumbs;
        private ToolStripMenuItem iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show;
        private ToolStripMenuItem iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll;
        private ToolStripMenuItem iPopupMenu_Relationships_Incoming_Breadcrumbs_Show;
        private ToolStripMenuItem iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll;
        private ToolTip iToolTip;
        private FlowLayoutPanel iPG1_FLP_NavigateAndSearch;
        private HeapCellInfoControl iPG1_HeapCellInfo;
        private SplitContainer iPG1_SplitCon;
        private FlowLayoutPanel iPG1_FLP_Top;
        private TableLayoutPanel iPG1_TLP_Top;
        private TableLayoutPanel iPG1_TLP_Bottom;
        private SplitContainer iPG3_SplitCon;
        private HeapCellStatsControl iPG3_StatsControl;
        private HeapCellSizeDistributionControl iPG4_DistributionControl;
        private GroupBox groupBox7;
        private GroupBox groupBox8;
        private MenuItem iMenuItem_File_SaveAs_HTML;
		private System.ComponentModel.IContainer components;
		#endregion

		#region Constructors & destructor
		public HeapViewerForm( HeapReconstructor aReconstructor, XmlSettings aSettings )
		{
			iReconstructor = aReconstructor;
			iSettings = aSettings;
			//
			InitializeComponent();
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager( typeof( HeapViewerForm ) );
            HeapLib.Statistics.HeapStatistics heapStatistics1 = new HeapLib.Statistics.HeapStatistics();
            HeapLib.Array.HeapCellArray heapCellArray1 = new HeapLib.Array.HeapCellArray();
            HeapLib.Array.HeapCellArray heapCellArray2 = new HeapLib.Array.HeapCellArray();
            HeapLib.Statistics.HeapStatistics heapStatistics2 = new HeapLib.Statistics.HeapStatistics();
            this.iTimer_CreateToolBoxItems = new System.Windows.Forms.Timer( this.components );
            this.iMainMenu = new System.Windows.Forms.MainMenu( this.components );
            this.iMenuItem_File = new System.Windows.Forms.MenuItem();
            this.iMenuItem_File_SaveAs = new System.Windows.Forms.MenuItem();
            this.iMenuItem_File_SaveAs_Zip = new System.Windows.Forms.MenuItem();
            this.iMenuItem_File_SaveAs_CSV = new System.Windows.Forms.MenuItem();
            this.iMenuItem_File_SaveAs_Text = new System.Windows.Forms.MenuItem();
            this.menuItem6 = new System.Windows.Forms.MenuItem();
            this.iMenuItem_File_Exit = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Show = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Show_RelationshipInspector = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Show_CellContentsViewer = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_Cell = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_Object = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_ParentBinary = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_Length = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_Isolation = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_EmbeddedReferences = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Type_Age = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Zoom = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Zoom_Small = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Zoom_Medium = new System.Windows.Forms.MenuItem();
            this.iMenuItem_View_Zoom_Large = new System.Windows.Forms.MenuItem();
            this.iMenuItem_Help = new System.Windows.Forms.MenuItem();
            this.iMenuItem_Help_About = new System.Windows.Forms.MenuItem();
            this.iPanel_Main = new System.Windows.Forms.Panel();
            this.iTabs = new System.Windows.Forms.TabControl();
            this.iPG1_HeapView = new System.Windows.Forms.TabPage();
            this.iPG1_SplitCon = new System.Windows.Forms.SplitContainer();
            this.iPG1_TLP_Top = new System.Windows.Forms.TableLayoutPanel();
            this.iPG1_FLP_Top = new System.Windows.Forms.FlowLayoutPanel();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.iPG1_RB_HeapView_ByCellType = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByObjectType = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByParentBinary = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByCellLength = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByEmbeddedReferences = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByIsolation = new System.Windows.Forms.RadioButton();
            this.iPG1_RB_HeapView_ByCellAge = new System.Windows.Forms.RadioButton();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.iPG1_BT_HeapView_Filters = new System.Windows.Forms.Button();
            this.iPG1_HeapView_Viewer = new HeapCtrlLib.HeapDataControl();
            this.iPG1_TLP_Bottom = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox6 = new System.Windows.Forms.GroupBox();
            this.iPG1_FLP_NavigateAndSearch = new System.Windows.Forms.FlowLayoutPanel();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.iPG1_BT_Navigate_ByPosition_Next = new System.Windows.Forms.Button();
            this.iPG1_BT_Navigate_ByPosition_Previous = new System.Windows.Forms.Button();
            this.iPG1_GP_Navigate_ByAllocNumber = new System.Windows.Forms.GroupBox();
            this.iPG1_BT_Navigate_ByAllocNumber_Next = new System.Windows.Forms.Button();
            this.iPG1_BT_Navigate_ByAllocNumber_Previous = new System.Windows.Forms.Button();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.iPG1_BT_Search_ByAllocNumber = new System.Windows.Forms.Button();
            this.iPG1_NumUD_Search_ByAllocNumber = new System.Windows.Forms.NumericUpDown();
            this.iPG1_TB_Search_ByAddress = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.iPG1_BT_Search_ByAddress = new System.Windows.Forms.Button();
            this.iPG2_CellList = new System.Windows.Forms.TabPage();
            this.iPG2_SplitCon = new System.Windows.Forms.SplitContainer();
            this.iPG3_Objects = new System.Windows.Forms.TabPage();
            this.iPG3_SplitCon = new System.Windows.Forms.SplitContainer();
            this.groupBox7 = new System.Windows.Forms.GroupBox();
            this.groupBox8 = new System.Windows.Forms.GroupBox();
            this.iPG3_ObjectPieChart = new ZedGraph.ZedGraphControl();
            this.iPG6_Relationships = new System.Windows.Forms.TabPage();
            this.iPG5_Distribution = new System.Windows.Forms.TabPage();
            this.iPG4_Graphs = new System.Windows.Forms.TabPage();
            this.iPG4_GraphCtrl = new ZedGraph.ZedGraphControl();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.iPG4_RB_GraphType_AssociatedBinary = new System.Windows.Forms.RadioButton();
            this.iPG4_RB_GraphType_SizeByIndex = new System.Windows.Forms.RadioButton();
            this.iPG4_RB_GraphType_CellSizeFree = new System.Windows.Forms.RadioButton();
            this.iPG4_RB_GraphType_CellSizeAllocated = new System.Windows.Forms.RadioButton();
            this.iPG4_RB_GraphType_CellOverhead = new System.Windows.Forms.RadioButton();
            this.iTab2_ColModel_Stats = new XPTable.Models.ColumnModel();
            this.iTab2_ColModel_Stats_Col_Symbol = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_AllocationCount = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance = new XPTable.Models.TextColumn();
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory = new XPTable.Models.TextColumn();
            this.iTab2_TabModel_Stats = new XPTable.Models.TableModel();
            this.iPG5_TableModel_CellDistributionFree = new XPTable.Models.TableModel();
            this.iPG5_TableModel_CellDistributionAllocated = new XPTable.Models.TableModel();
            this.iPG1_HeapView_ContextMenu = new System.Windows.Forms.ContextMenuStrip( this.components );
            this.iPopupMenu_Relationships = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Outgoing = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Outgoing_View = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Incoming = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Incoming_View = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Show = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Show_CellRelationshipInspector = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_Show_CellContentsViewer = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_XRef = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_XRef_Settings = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_GoTo = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_GoTo_EmbeddedReferences = new System.Windows.Forms.ToolStripMenuItem();
            this.iPopupMenu_GoTo_IncomingReferences = new System.Windows.Forms.ToolStripMenuItem();
            this.iToolTip = new System.Windows.Forms.ToolTip( this.components );
            this.iMenuItem_File_SaveAs_HTML = new System.Windows.Forms.MenuItem();
            this.iPG1_HeapCellInfo = new HeapUiLib.Controls.HeapCellInfoControl();
            this.iPG2_FilterControl = new HeapUiLib.Controls.HeapCellListingFilter();
            this.iPG2_CellList_Cells = new HeapUiLib.Controls.HeapCellListingControl();
            this.iPG3_StatsControl = new HeapUiLib.Controls.HeapCellStatsControl();
            this.iPG6_RelationshipControl = new HeapUiLib.Controls.HeapCellRelationshipControl();
            this.iPG4_DistributionControl = new HeapUiLib.Controls.HeapCellSizeDistributionControl();
            this.iPanel_Main.SuspendLayout();
            this.iTabs.SuspendLayout();
            this.iPG1_HeapView.SuspendLayout();
            this.iPG1_SplitCon.Panel1.SuspendLayout();
            this.iPG1_SplitCon.Panel2.SuspendLayout();
            this.iPG1_SplitCon.SuspendLayout();
            this.iPG1_TLP_Top.SuspendLayout();
            this.iPG1_FLP_Top.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.iPG1_TLP_Bottom.SuspendLayout();
            this.groupBox6.SuspendLayout();
            this.iPG1_FLP_NavigateAndSearch.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.iPG1_GP_Navigate_ByAllocNumber.SuspendLayout();
            this.groupBox3.SuspendLayout();
            ( (System.ComponentModel.ISupportInitialize) ( this.iPG1_NumUD_Search_ByAllocNumber ) ).BeginInit();
            this.iPG2_CellList.SuspendLayout();
            this.iPG2_SplitCon.Panel1.SuspendLayout();
            this.iPG2_SplitCon.Panel2.SuspendLayout();
            this.iPG2_SplitCon.SuspendLayout();
            this.iPG3_Objects.SuspendLayout();
            this.iPG3_SplitCon.Panel1.SuspendLayout();
            this.iPG3_SplitCon.Panel2.SuspendLayout();
            this.iPG3_SplitCon.SuspendLayout();
            this.groupBox7.SuspendLayout();
            this.groupBox8.SuspendLayout();
            this.iPG6_Relationships.SuspendLayout();
            this.iPG5_Distribution.SuspendLayout();
            this.iPG4_Graphs.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.iPG1_HeapView_ContextMenu.SuspendLayout();
            this.SuspendLayout();
            // 
            // iTimer_CreateToolBoxItems
            // 
            this.iTimer_CreateToolBoxItems.Enabled = true;
            this.iTimer_CreateToolBoxItems.Interval = 50;
            this.iTimer_CreateToolBoxItems.Tick += new System.EventHandler( this.iTimer_CreateToolBoxItems_Tick );
            // 
            // iMainMenu
            // 
            this.iMainMenu.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_File,
            this.iMenuItem_View,
            this.iMenuItem_Help} );
            // 
            // iMenuItem_File
            // 
            this.iMenuItem_File.Index = 0;
            this.iMenuItem_File.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_File_SaveAs,
            this.menuItem6,
            this.iMenuItem_File_Exit} );
            this.iMenuItem_File.Text = "&File";
            // 
            // iMenuItem_File_SaveAs
            // 
            this.iMenuItem_File_SaveAs.Index = 0;
            this.iMenuItem_File_SaveAs.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_File_SaveAs_Zip,
            this.iMenuItem_File_SaveAs_CSV,
            this.iMenuItem_File_SaveAs_Text,
            this.iMenuItem_File_SaveAs_HTML} );
            this.iMenuItem_File_SaveAs.Text = "Save &As...";
            // 
            // iMenuItem_File_SaveAs_Zip
            // 
            this.iMenuItem_File_SaveAs_Zip.Index = 0;
            this.iMenuItem_File_SaveAs_Zip.Text = "As &Zip...";
            this.iMenuItem_File_SaveAs_Zip.Click += new System.EventHandler( this.iMenuItem_File_SaveAs_Zip_Click );
            // 
            // iMenuItem_File_SaveAs_CSV
            // 
            this.iMenuItem_File_SaveAs_CSV.Index = 1;
            this.iMenuItem_File_SaveAs_CSV.Text = "As &CSV...";
            this.iMenuItem_File_SaveAs_CSV.Click += new System.EventHandler( this.iMenuItem_File_SaveAs_CSV_Click );
            // 
            // iMenuItem_File_SaveAs_Text
            // 
            this.iMenuItem_File_SaveAs_Text.Index = 2;
            this.iMenuItem_File_SaveAs_Text.Text = "As &Text...";
            this.iMenuItem_File_SaveAs_Text.Click += new System.EventHandler( this.iMenuItem_File_SaveAs_Text_Click );
            // 
            // menuItem6
            // 
            this.menuItem6.Index = 1;
            this.menuItem6.Text = "-";
            // 
            // iMenuItem_File_Exit
            // 
            this.iMenuItem_File_Exit.Index = 2;
            this.iMenuItem_File_Exit.Text = "E&xit";
            this.iMenuItem_File_Exit.Click += new System.EventHandler( this.iMenuItem_File_Exit_Click );
            // 
            // iMenuItem_View
            // 
            this.iMenuItem_View.Index = 1;
            this.iMenuItem_View.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_View_Show,
            this.iMenuItem_View_Type,
            this.iMenuItem_View_Zoom} );
            this.iMenuItem_View.Text = "&View";
            // 
            // iMenuItem_View_Show
            // 
            this.iMenuItem_View_Show.Index = 0;
            this.iMenuItem_View_Show.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_View_Show_RelationshipInspector,
            this.iMenuItem_View_Show_CellContentsViewer} );
            this.iMenuItem_View_Show.Text = "&Show";
            // 
            // iMenuItem_View_Show_RelationshipInspector
            // 
            this.iMenuItem_View_Show_RelationshipInspector.Index = 0;
            this.iMenuItem_View_Show_RelationshipInspector.Text = "Cell Relationship &Inspector";
            this.iMenuItem_View_Show_RelationshipInspector.Click += new System.EventHandler( this.iMenuItem_View_Show_RelationshipInspector_Click );
            // 
            // iMenuItem_View_Show_CellContentsViewer
            // 
            this.iMenuItem_View_Show_CellContentsViewer.Index = 1;
            this.iMenuItem_View_Show_CellContentsViewer.Text = "Cell &Contents Viewer";
            this.iMenuItem_View_Show_CellContentsViewer.Click += new System.EventHandler( this.iMenuItem_View_Show_CellContentsViewer_Click );
            // 
            // iMenuItem_View_Type
            // 
            this.iMenuItem_View_Type.Index = 1;
            this.iMenuItem_View_Type.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_View_Type_Cell,
            this.iMenuItem_View_Type_Object,
            this.iMenuItem_View_Type_ParentBinary,
            this.iMenuItem_View_Type_Length,
            this.iMenuItem_View_Type_Isolation,
            this.iMenuItem_View_Type_EmbeddedReferences,
            this.iMenuItem_View_Type_Age} );
            this.iMenuItem_View_Type.Text = "&Type";
            // 
            // iMenuItem_View_Type_Cell
            // 
            this.iMenuItem_View_Type_Cell.Index = 0;
            this.iMenuItem_View_Type_Cell.Text = "...by &Cell Type";
            this.iMenuItem_View_Type_Cell.Click += new System.EventHandler( this.iMenuItem_View_Type_Cell_Click );
            // 
            // iMenuItem_View_Type_Object
            // 
            this.iMenuItem_View_Type_Object.Index = 1;
            this.iMenuItem_View_Type_Object.Text = "...by &Object Type";
            this.iMenuItem_View_Type_Object.Click += new System.EventHandler( this.iMenuItem_View_Type_Object_Click );
            // 
            // iMenuItem_View_Type_ParentBinary
            // 
            this.iMenuItem_View_Type_ParentBinary.Index = 2;
            this.iMenuItem_View_Type_ParentBinary.Text = "...by Parent &Binary";
            this.iMenuItem_View_Type_ParentBinary.Click += new System.EventHandler( this.iMenuItem_View_Type_ParentBinary_Click );
            // 
            // iMenuItem_View_Type_Length
            // 
            this.iMenuItem_View_Type_Length.Index = 3;
            this.iMenuItem_View_Type_Length.Text = "...by Cell &Length";
            this.iMenuItem_View_Type_Length.Click += new System.EventHandler( this.iMenuItem_View_Type_Length_Click );
            // 
            // iMenuItem_View_Type_Isolation
            // 
            this.iMenuItem_View_Type_Isolation.Index = 4;
            this.iMenuItem_View_Type_Isolation.Text = "...by Isolation";
            this.iMenuItem_View_Type_Isolation.Click += new System.EventHandler( this.iMenuItem_View_Type_Isolation_Click );
            // 
            // iMenuItem_View_Type_EmbeddedReferences
            // 
            this.iMenuItem_View_Type_EmbeddedReferences.Index = 5;
            this.iMenuItem_View_Type_EmbeddedReferences.Text = "...by Pervasiveness";
            this.iMenuItem_View_Type_EmbeddedReferences.Click += new System.EventHandler( this.iMenuItem_View_Type_EmbeddedReferences_Click );
            // 
            // iMenuItem_View_Type_Age
            // 
            this.iMenuItem_View_Type_Age.Index = 6;
            this.iMenuItem_View_Type_Age.Text = "...by Cell &Age";
            this.iMenuItem_View_Type_Age.Click += new System.EventHandler( this.iMenuItem_View_Type_Age_Click );
            // 
            // iMenuItem_View_Zoom
            // 
            this.iMenuItem_View_Zoom.Index = 2;
            this.iMenuItem_View_Zoom.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_View_Zoom_Small,
            this.iMenuItem_View_Zoom_Medium,
            this.iMenuItem_View_Zoom_Large} );
            this.iMenuItem_View_Zoom.Text = "Si&ze";
            // 
            // iMenuItem_View_Zoom_Small
            // 
            this.iMenuItem_View_Zoom_Small.Index = 0;
            this.iMenuItem_View_Zoom_Small.Text = "Small";
            this.iMenuItem_View_Zoom_Small.Click += new System.EventHandler( this.iMenuItem_View_Zoom_Small_Click );
            // 
            // iMenuItem_View_Zoom_Medium
            // 
            this.iMenuItem_View_Zoom_Medium.Index = 1;
            this.iMenuItem_View_Zoom_Medium.Text = "Medium";
            this.iMenuItem_View_Zoom_Medium.Click += new System.EventHandler( this.iMenuItem_View_Zoom_Medium_Click );
            // 
            // iMenuItem_View_Zoom_Large
            // 
            this.iMenuItem_View_Zoom_Large.Index = 2;
            this.iMenuItem_View_Zoom_Large.Text = "Large";
            this.iMenuItem_View_Zoom_Large.Click += new System.EventHandler( this.iMenuItem_View_Zoom_Large_Click );
            // 
            // iMenuItem_Help
            // 
            this.iMenuItem_Help.Index = 2;
            this.iMenuItem_Help.MenuItems.AddRange( new System.Windows.Forms.MenuItem[] {
            this.iMenuItem_Help_About} );
            this.iMenuItem_Help.Text = "&Help";
            // 
            // iMenuItem_Help_About
            // 
            this.iMenuItem_Help_About.Index = 0;
            this.iMenuItem_Help_About.Text = "&About";
            this.iMenuItem_Help_About.Click += new System.EventHandler( this.iMenuItem_Help_About_Click );
            // 
            // iPanel_Main
            // 
            this.iPanel_Main.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.iPanel_Main.Controls.Add( this.iTabs );
            this.iPanel_Main.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPanel_Main.Location = new System.Drawing.Point( 0, 0 );
            this.iPanel_Main.Name = "iPanel_Main";
            this.iPanel_Main.Size = new System.Drawing.Size( 1031, 608 );
            this.iPanel_Main.TabIndex = 7;
            // 
            // iTabs
            // 
            this.iTabs.Controls.Add( this.iPG1_HeapView );
            this.iTabs.Controls.Add( this.iPG2_CellList );
            this.iTabs.Controls.Add( this.iPG3_Objects );
            this.iTabs.Controls.Add( this.iPG6_Relationships );
            this.iTabs.Controls.Add( this.iPG5_Distribution );
            this.iTabs.Controls.Add( this.iPG4_Graphs );
            this.iTabs.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iTabs.Location = new System.Drawing.Point( 0, 0 );
            this.iTabs.Name = "iTabs";
            this.iTabs.SelectedIndex = 0;
            this.iTabs.Size = new System.Drawing.Size( 1027, 604 );
            this.iTabs.TabIndex = 7;
            // 
            // iPG1_HeapView
            // 
            this.iPG1_HeapView.Controls.Add( this.iPG1_SplitCon );
            this.iPG1_HeapView.Location = new System.Drawing.Point( 4, 22 );
            this.iPG1_HeapView.Name = "iPG1_HeapView";
            this.iPG1_HeapView.Padding = new System.Windows.Forms.Padding( 0, 2, 0, 0 );
            this.iPG1_HeapView.Size = new System.Drawing.Size( 1019, 578 );
            this.iPG1_HeapView.TabIndex = 0;
            this.iPG1_HeapView.Text = "Heap View";
            this.iPG1_HeapView.UseVisualStyleBackColor = true;
            // 
            // iPG1_SplitCon
            // 
            this.iPG1_SplitCon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_SplitCon.FixedPanel = System.Windows.Forms.FixedPanel.Panel2;
            this.iPG1_SplitCon.Location = new System.Drawing.Point( 0, 2 );
            this.iPG1_SplitCon.Name = "iPG1_SplitCon";
            this.iPG1_SplitCon.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // iPG1_SplitCon.Panel1
            // 
            this.iPG1_SplitCon.Panel1.Controls.Add( this.iPG1_TLP_Top );
            // 
            // iPG1_SplitCon.Panel2
            // 
            this.iPG1_SplitCon.Panel2.Controls.Add( this.iPG1_TLP_Bottom );
            this.iPG1_SplitCon.Size = new System.Drawing.Size( 1019, 576 );
            this.iPG1_SplitCon.SplitterDistance = 415;
            this.iPG1_SplitCon.TabIndex = 32;
            // 
            // iPG1_TLP_Top
            // 
            this.iPG1_TLP_Top.ColumnCount = 1;
            this.iPG1_TLP_Top.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iPG1_TLP_Top.Controls.Add( this.iPG1_FLP_Top, 0, 0 );
            this.iPG1_TLP_Top.Controls.Add( this.iPG1_HeapView_Viewer, 0, 1 );
            this.iPG1_TLP_Top.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_TLP_Top.Location = new System.Drawing.Point( 0, 0 );
            this.iPG1_TLP_Top.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG1_TLP_Top.Name = "iPG1_TLP_Top";
            this.iPG1_TLP_Top.RowCount = 2;
            this.iPG1_TLP_Top.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Absolute, 55F ) );
            this.iPG1_TLP_Top.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iPG1_TLP_Top.Size = new System.Drawing.Size( 1019, 415 );
            this.iPG1_TLP_Top.TabIndex = 0;
            // 
            // iPG1_FLP_Top
            // 
            this.iPG1_FLP_Top.Controls.Add( this.groupBox1 );
            this.iPG1_FLP_Top.Controls.Add( this.groupBox5 );
            this.iPG1_FLP_Top.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_FLP_Top.Location = new System.Drawing.Point( 0, 0 );
            this.iPG1_FLP_Top.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG1_FLP_Top.Name = "iPG1_FLP_Top";
            this.iPG1_FLP_Top.Size = new System.Drawing.Size( 1019, 55 );
            this.iPG1_FLP_Top.TabIndex = 0;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByCellType );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByObjectType );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByParentBinary );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByCellLength );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByEmbeddedReferences );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByIsolation );
            this.groupBox1.Controls.Add( this.iPG1_RB_HeapView_ByCellAge );
            this.groupBox1.Location = new System.Drawing.Point( 3, 3 );
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size( 724, 48 );
            this.groupBox1.TabIndex = 0;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = " View Type";
            // 
            // iPG1_RB_HeapView_ByCellType
            // 
            this.iPG1_RB_HeapView_ByCellType.AutoSize = true;
            this.iPG1_RB_HeapView_ByCellType.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByCellType.Location = new System.Drawing.Point( 27, 19 );
            this.iPG1_RB_HeapView_ByCellType.Name = "iPG1_RB_HeapView_ByCellType";
            this.iPG1_RB_HeapView_ByCellType.Size = new System.Drawing.Size( 69, 17 );
            this.iPG1_RB_HeapView_ByCellType.TabIndex = 0;
            this.iPG1_RB_HeapView_ByCellType.Text = "Cell Type";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByCellType, resources.GetString( "iPG1_RB_HeapView_ByCellType.ToolTip" ) );
            this.iPG1_RB_HeapView_ByCellType.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByObjectType
            // 
            this.iPG1_RB_HeapView_ByObjectType.AutoSize = true;
            this.iPG1_RB_HeapView_ByObjectType.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByObjectType.Location = new System.Drawing.Point( 119, 19 );
            this.iPG1_RB_HeapView_ByObjectType.Name = "iPG1_RB_HeapView_ByObjectType";
            this.iPG1_RB_HeapView_ByObjectType.Size = new System.Drawing.Size( 84, 17 );
            this.iPG1_RB_HeapView_ByObjectType.TabIndex = 0;
            this.iPG1_RB_HeapView_ByObjectType.Text = "Object Type";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByObjectType, resources.GetString( "iPG1_RB_HeapView_ByObjectType.ToolTip" ) );
            this.iPG1_RB_HeapView_ByObjectType.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByParentBinary
            // 
            this.iPG1_RB_HeapView_ByParentBinary.AutoSize = true;
            this.iPG1_RB_HeapView_ByParentBinary.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByParentBinary.Location = new System.Drawing.Point( 225, 19 );
            this.iPG1_RB_HeapView_ByParentBinary.Name = "iPG1_RB_HeapView_ByParentBinary";
            this.iPG1_RB_HeapView_ByParentBinary.Size = new System.Drawing.Size( 90, 17 );
            this.iPG1_RB_HeapView_ByParentBinary.TabIndex = 0;
            this.iPG1_RB_HeapView_ByParentBinary.Text = "Parent Binary";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByParentBinary, resources.GetString( "iPG1_RB_HeapView_ByParentBinary.ToolTip" ) );
            this.iPG1_RB_HeapView_ByParentBinary.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByCellLength
            // 
            this.iPG1_RB_HeapView_ByCellLength.AutoSize = true;
            this.iPG1_RB_HeapView_ByCellLength.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByCellLength.Location = new System.Drawing.Point( 336, 19 );
            this.iPG1_RB_HeapView_ByCellLength.Name = "iPG1_RB_HeapView_ByCellLength";
            this.iPG1_RB_HeapView_ByCellLength.Size = new System.Drawing.Size( 78, 17 );
            this.iPG1_RB_HeapView_ByCellLength.TabIndex = 0;
            this.iPG1_RB_HeapView_ByCellLength.Text = "Cell Length";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByCellLength, "A simple view that emphasises the length of the cell. \r\n\r\nThe darker the cell col" +
                    "our appears, the large the cell." );
            this.iPG1_RB_HeapView_ByCellLength.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByEmbeddedReferences
            // 
            this.iPG1_RB_HeapView_ByEmbeddedReferences.AutoSize = true;
            this.iPG1_RB_HeapView_ByEmbeddedReferences.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByEmbeddedReferences.Location = new System.Drawing.Point( 524, 19 );
            this.iPG1_RB_HeapView_ByEmbeddedReferences.Name = "iPG1_RB_HeapView_ByEmbeddedReferences";
            this.iPG1_RB_HeapView_ByEmbeddedReferences.Size = new System.Drawing.Size( 94, 17 );
            this.iPG1_RB_HeapView_ByEmbeddedReferences.TabIndex = 0;
            this.iPG1_RB_HeapView_ByEmbeddedReferences.Text = "Pervasiveness";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByEmbeddedReferences, resources.GetString( "iPG1_RB_HeapView_ByEmbeddedReferences.ToolTip" ) );
            this.iPG1_RB_HeapView_ByEmbeddedReferences.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByIsolation
            // 
            this.iPG1_RB_HeapView_ByIsolation.AutoSize = true;
            this.iPG1_RB_HeapView_ByIsolation.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByIsolation.Location = new System.Drawing.Point( 437, 19 );
            this.iPG1_RB_HeapView_ByIsolation.Name = "iPG1_RB_HeapView_ByIsolation";
            this.iPG1_RB_HeapView_ByIsolation.Size = new System.Drawing.Size( 66, 17 );
            this.iPG1_RB_HeapView_ByIsolation.TabIndex = 0;
            this.iPG1_RB_HeapView_ByIsolation.Text = "Isolation";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByIsolation, resources.GetString( "iPG1_RB_HeapView_ByIsolation.ToolTip" ) );
            this.iPG1_RB_HeapView_ByIsolation.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // iPG1_RB_HeapView_ByCellAge
            // 
            this.iPG1_RB_HeapView_ByCellAge.AutoSize = true;
            this.iPG1_RB_HeapView_ByCellAge.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_RB_HeapView_ByCellAge.Location = new System.Drawing.Point( 641, 19 );
            this.iPG1_RB_HeapView_ByCellAge.Name = "iPG1_RB_HeapView_ByCellAge";
            this.iPG1_RB_HeapView_ByCellAge.Size = new System.Drawing.Size( 64, 17 );
            this.iPG1_RB_HeapView_ByCellAge.TabIndex = 0;
            this.iPG1_RB_HeapView_ByCellAge.Text = "Cell Age";
            this.iToolTip.SetToolTip( this.iPG1_RB_HeapView_ByCellAge, resources.GetString( "iPG1_RB_HeapView_ByCellAge.ToolTip" ) );
            this.iPG1_RB_HeapView_ByCellAge.CheckedChanged += new System.EventHandler( this.iPG1_RB_HeapView_ViewType_CheckedChanged );
            // 
            // groupBox5
            // 
            this.groupBox5.Controls.Add( this.iPG1_BT_HeapView_Filters );
            this.groupBox5.Location = new System.Drawing.Point( 733, 3 );
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size( 101, 48 );
            this.groupBox5.TabIndex = 1;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "Filters";
            // 
            // iPG1_BT_HeapView_Filters
            // 
            this.iPG1_BT_HeapView_Filters.Location = new System.Drawing.Point( 15, 15 );
            this.iPG1_BT_HeapView_Filters.Name = "iPG1_BT_HeapView_Filters";
            this.iPG1_BT_HeapView_Filters.Size = new System.Drawing.Size( 71, 25 );
            this.iPG1_BT_HeapView_Filters.TabIndex = 0;
            this.iPG1_BT_HeapView_Filters.Text = "Set Filters";
            this.iPG1_BT_HeapView_Filters.UseVisualStyleBackColor = true;
            this.iPG1_BT_HeapView_Filters.Click += new System.EventHandler( this.iPG1_BT_HeapView_Filters_Click );
            // 
            // iPG1_HeapView_Viewer
            // 
            this.iPG1_HeapView_Viewer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_HeapView_Viewer.FocusedCell = null;
            this.iPG1_HeapView_Viewer.Location = new System.Drawing.Point( 3, 58 );
            this.iPG1_HeapView_Viewer.Name = "iPG1_HeapView_Viewer";
            this.iPG1_HeapView_Viewer.Padding = new System.Windows.Forms.Padding( 2 );
            this.iPG1_HeapView_Viewer.Reconstructor = null;
            this.iPG1_HeapView_Viewer.Size = new System.Drawing.Size( 1013, 354 );
            this.iPG1_HeapView_Viewer.TabIndex = 25;
            this.iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell;
            this.iPG1_HeapView_Viewer.Zoom = HeapCtrlLib.Types.THeapCtrlZoom.EHeapCtrlZoomMedium;
            this.iPG1_HeapView_Viewer.CellSelected += new HeapCtrlLib.HeapDataControl.OnCellSelected( this.iPG1_HeapView_Viewer_CellSelected );
            this.iPG1_HeapView_Viewer.CellDoubleClicked += new HeapCtrlLib.HeapDataControl.OnCellDoubleClicked( this.iPG1_HeapView_Viewer_CellDoubleClicked );
            this.iPG1_HeapView_Viewer.CellRightClicked += new HeapCtrlLib.HeapDataControl.CellRightClickedHandler( this.iPG1_HeapView_Viewer_CellRightClicked );
            this.iPG1_HeapView_Viewer.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iPG1_HeapView_Viewer_KeyDown );
            // 
            // iPG1_TLP_Bottom
            // 
            this.iPG1_TLP_Bottom.ColumnCount = 2;
            this.iPG1_TLP_Bottom.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iPG1_TLP_Bottom.ColumnStyles.Add( new System.Windows.Forms.ColumnStyle( System.Windows.Forms.SizeType.Absolute, 484F ) );
            this.iPG1_TLP_Bottom.Controls.Add( this.groupBox6, 0, 0 );
            this.iPG1_TLP_Bottom.Controls.Add( this.iPG1_FLP_NavigateAndSearch, 1, 0 );
            this.iPG1_TLP_Bottom.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG1_TLP_Bottom.Location = new System.Drawing.Point( 0, 0 );
            this.iPG1_TLP_Bottom.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG1_TLP_Bottom.Name = "iPG1_TLP_Bottom";
            this.iPG1_TLP_Bottom.RowCount = 1;
            this.iPG1_TLP_Bottom.RowStyles.Add( new System.Windows.Forms.RowStyle( System.Windows.Forms.SizeType.Percent, 100F ) );
            this.iPG1_TLP_Bottom.Size = new System.Drawing.Size( 1019, 157 );
            this.iPG1_TLP_Bottom.TabIndex = 0;
            // 
            // groupBox6
            // 
            this.groupBox6.Controls.Add( this.iPG1_HeapCellInfo );
            this.groupBox6.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox6.Location = new System.Drawing.Point( 3, 3 );
            this.groupBox6.Name = "groupBox6";
            this.groupBox6.Size = new System.Drawing.Size( 529, 151 );
            this.groupBox6.TabIndex = 24;
            this.groupBox6.TabStop = false;
            this.groupBox6.Text = "Information";
            // 
            // iPG1_FLP_NavigateAndSearch
            // 
            this.iPG1_FLP_NavigateAndSearch.Controls.Add( this.groupBox4 );
            this.iPG1_FLP_NavigateAndSearch.Controls.Add( this.iPG1_GP_Navigate_ByAllocNumber );
            this.iPG1_FLP_NavigateAndSearch.Controls.Add( this.groupBox3 );
            this.iPG1_FLP_NavigateAndSearch.FlowDirection = System.Windows.Forms.FlowDirection.TopDown;
            this.iPG1_FLP_NavigateAndSearch.Location = new System.Drawing.Point( 535, 0 );
            this.iPG1_FLP_NavigateAndSearch.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG1_FLP_NavigateAndSearch.Name = "iPG1_FLP_NavigateAndSearch";
            this.iPG1_FLP_NavigateAndSearch.Size = new System.Drawing.Size( 481, 152 );
            this.iPG1_FLP_NavigateAndSearch.TabIndex = 31;
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add( this.iPG1_BT_Navigate_ByPosition_Next );
            this.groupBox4.Controls.Add( this.iPG1_BT_Navigate_ByPosition_Previous );
            this.groupBox4.Location = new System.Drawing.Point( 3, 3 );
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size( 186, 48 );
            this.groupBox4.TabIndex = 29;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Navigate by cell position";
            // 
            // iPG1_BT_Navigate_ByPosition_Next
            // 
            this.iPG1_BT_Navigate_ByPosition_Next.Location = new System.Drawing.Point( 97, 16 );
            this.iPG1_BT_Navigate_ByPosition_Next.Name = "iPG1_BT_Navigate_ByPosition_Next";
            this.iPG1_BT_Navigate_ByPosition_Next.Size = new System.Drawing.Size( 80, 24 );
            this.iPG1_BT_Navigate_ByPosition_Next.TabIndex = 4;
            this.iPG1_BT_Navigate_ByPosition_Next.Text = "Next";
            this.iPG1_BT_Navigate_ByPosition_Next.Click += new System.EventHandler( this.iPG1_BT_Navigate_ByPosition_Next_Click );
            // 
            // iPG1_BT_Navigate_ByPosition_Previous
            // 
            this.iPG1_BT_Navigate_ByPosition_Previous.Location = new System.Drawing.Point( 9, 16 );
            this.iPG1_BT_Navigate_ByPosition_Previous.Name = "iPG1_BT_Navigate_ByPosition_Previous";
            this.iPG1_BT_Navigate_ByPosition_Previous.Size = new System.Drawing.Size( 80, 24 );
            this.iPG1_BT_Navigate_ByPosition_Previous.TabIndex = 3;
            this.iPG1_BT_Navigate_ByPosition_Previous.Text = "Previous";
            this.iPG1_BT_Navigate_ByPosition_Previous.Click += new System.EventHandler( this.iPG1_BT_Navigate_ByPosition_Previous_Click );
            // 
            // iPG1_GP_Navigate_ByAllocNumber
            // 
            this.iPG1_GP_Navigate_ByAllocNumber.Controls.Add( this.iPG1_BT_Navigate_ByAllocNumber_Next );
            this.iPG1_GP_Navigate_ByAllocNumber.Controls.Add( this.iPG1_BT_Navigate_ByAllocNumber_Previous );
            this.iPG1_GP_Navigate_ByAllocNumber.Location = new System.Drawing.Point( 3, 57 );
            this.iPG1_GP_Navigate_ByAllocNumber.Name = "iPG1_GP_Navigate_ByAllocNumber";
            this.iPG1_GP_Navigate_ByAllocNumber.Size = new System.Drawing.Size( 186, 48 );
            this.iPG1_GP_Navigate_ByAllocNumber.TabIndex = 30;
            this.iPG1_GP_Navigate_ByAllocNumber.TabStop = false;
            this.iPG1_GP_Navigate_ByAllocNumber.Text = "Navigate by allocation number";
            // 
            // iPG1_BT_Navigate_ByAllocNumber_Next
            // 
            this.iPG1_BT_Navigate_ByAllocNumber_Next.Location = new System.Drawing.Point( 97, 16 );
            this.iPG1_BT_Navigate_ByAllocNumber_Next.Name = "iPG1_BT_Navigate_ByAllocNumber_Next";
            this.iPG1_BT_Navigate_ByAllocNumber_Next.Size = new System.Drawing.Size( 80, 24 );
            this.iPG1_BT_Navigate_ByAllocNumber_Next.TabIndex = 4;
            this.iPG1_BT_Navigate_ByAllocNumber_Next.Text = "Next";
            this.iPG1_BT_Navigate_ByAllocNumber_Next.Click += new System.EventHandler( this.iPG1_BT_Navigate_ByAllocNumber_Next_Click );
            // 
            // iPG1_BT_Navigate_ByAllocNumber_Previous
            // 
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.Location = new System.Drawing.Point( 9, 16 );
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.Name = "iPG1_BT_Navigate_ByAllocNumber_Previous";
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.Size = new System.Drawing.Size( 80, 24 );
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.TabIndex = 3;
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.Text = "Previous";
            this.iPG1_BT_Navigate_ByAllocNumber_Previous.Click += new System.EventHandler( this.iPG1_BT_Navigate_ByAllocNumber_Previous_Click );
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add( this.iPG1_BT_Search_ByAllocNumber );
            this.groupBox3.Controls.Add( this.iPG1_NumUD_Search_ByAllocNumber );
            this.groupBox3.Controls.Add( this.iPG1_TB_Search_ByAddress );
            this.groupBox3.Controls.Add( this.label3 );
            this.groupBox3.Controls.Add( this.label4 );
            this.groupBox3.Controls.Add( this.iPG1_BT_Search_ByAddress );
            this.groupBox3.Location = new System.Drawing.Point( 195, 3 );
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size( 280, 80 );
            this.groupBox3.TabIndex = 25;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Search...";
            // 
            // iPG1_BT_Search_ByAllocNumber
            // 
            this.iPG1_BT_Search_ByAllocNumber.Location = new System.Drawing.Point( 232, 45 );
            this.iPG1_BT_Search_ByAllocNumber.Name = "iPG1_BT_Search_ByAllocNumber";
            this.iPG1_BT_Search_ByAllocNumber.Size = new System.Drawing.Size( 32, 23 );
            this.iPG1_BT_Search_ByAllocNumber.TabIndex = 6;
            this.iPG1_BT_Search_ByAllocNumber.Text = "Go";
            this.iPG1_BT_Search_ByAllocNumber.Click += new System.EventHandler( this.iPG1_BT_Search_ByAllocNumber_Click );
            // 
            // iPG1_NumUD_Search_ByAllocNumber
            // 
            this.iPG1_NumUD_Search_ByAllocNumber.Location = new System.Drawing.Point( 104, 46 );
            this.iPG1_NumUD_Search_ByAllocNumber.Name = "iPG1_NumUD_Search_ByAllocNumber";
            this.iPG1_NumUD_Search_ByAllocNumber.Size = new System.Drawing.Size( 120, 21 );
            this.iPG1_NumUD_Search_ByAllocNumber.TabIndex = 5;
            this.iPG1_NumUD_Search_ByAllocNumber.Value = new decimal( new int[] {
            1,
            0,
            0,
            0} );
            this.iPG1_NumUD_Search_ByAllocNumber.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iPG1_NumUD_Search_ByAllocNumber_KeyDown );
            // 
            // iPG1_TB_Search_ByAddress
            // 
            this.iPG1_TB_Search_ByAddress.Location = new System.Drawing.Point( 104, 20 );
            this.iPG1_TB_Search_ByAddress.Name = "iPG1_TB_Search_ByAddress";
            this.iPG1_TB_Search_ByAddress.Size = new System.Drawing.Size( 120, 21 );
            this.iPG1_TB_Search_ByAddress.TabIndex = 4;
            this.iPG1_TB_Search_ByAddress.Text = "0x00000000";
            this.iPG1_TB_Search_ByAddress.KeyDown += new System.Windows.Forms.KeyEventHandler( this.iPG1_TB_Search_ByAddress_KeyDown );
            // 
            // label3
            // 
            this.label3.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.label3.Location = new System.Drawing.Point( 16, 22 );
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size( 72, 16 );
            this.label3.TabIndex = 0;
            this.label3.Text = "...by address:";
            // 
            // label4
            // 
            this.label4.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.label4.Location = new System.Drawing.Point( 16, 48 );
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size( 72, 16 );
            this.label4.TabIndex = 0;
            this.label4.Text = "...by alloc #:";
            // 
            // iPG1_BT_Search_ByAddress
            // 
            this.iPG1_BT_Search_ByAddress.Location = new System.Drawing.Point( 232, 19 );
            this.iPG1_BT_Search_ByAddress.Name = "iPG1_BT_Search_ByAddress";
            this.iPG1_BT_Search_ByAddress.Size = new System.Drawing.Size( 32, 23 );
            this.iPG1_BT_Search_ByAddress.TabIndex = 3;
            this.iPG1_BT_Search_ByAddress.Text = "Go";
            this.iPG1_BT_Search_ByAddress.Click += new System.EventHandler( this.iPG1_BT_Search_ByAddress_Click );
            // 
            // iPG2_CellList
            // 
            this.iPG2_CellList.Controls.Add( this.iPG2_SplitCon );
            this.iPG2_CellList.Location = new System.Drawing.Point( 4, 22 );
            this.iPG2_CellList.Name = "iPG2_CellList";
            this.iPG2_CellList.Padding = new System.Windows.Forms.Padding( 2 );
            this.iPG2_CellList.Size = new System.Drawing.Size( 1019, 589 );
            this.iPG2_CellList.TabIndex = 3;
            this.iPG2_CellList.Text = "Cell List";
            this.iPG2_CellList.UseVisualStyleBackColor = true;
            // 
            // iPG2_SplitCon
            // 
            this.iPG2_SplitCon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG2_SplitCon.FixedPanel = System.Windows.Forms.FixedPanel.Panel1;
            this.iPG2_SplitCon.IsSplitterFixed = true;
            this.iPG2_SplitCon.Location = new System.Drawing.Point( 2, 2 );
            this.iPG2_SplitCon.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG2_SplitCon.Name = "iPG2_SplitCon";
            this.iPG2_SplitCon.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // iPG2_SplitCon.Panel1
            // 
            this.iPG2_SplitCon.Panel1.Controls.Add( this.iPG2_FilterControl );
            this.iPG2_SplitCon.Panel1.Margin = new System.Windows.Forms.Padding( 2 );
            this.iPG2_SplitCon.Panel1MinSize = 20;
            // 
            // iPG2_SplitCon.Panel2
            // 
            this.iPG2_SplitCon.Panel2.Controls.Add( this.iPG2_CellList_Cells );
            this.iPG2_SplitCon.Size = new System.Drawing.Size( 1015, 585 );
            this.iPG2_SplitCon.SplitterDistance = 20;
            this.iPG2_SplitCon.TabIndex = 1;
            // 
            // iPG3_Objects
            // 
            this.iPG3_Objects.Controls.Add( this.iPG3_SplitCon );
            this.iPG3_Objects.Location = new System.Drawing.Point( 4, 22 );
            this.iPG3_Objects.Name = "iPG3_Objects";
            this.iPG3_Objects.Padding = new System.Windows.Forms.Padding( 2 );
            this.iPG3_Objects.Size = new System.Drawing.Size( 1019, 589 );
            this.iPG3_Objects.TabIndex = 1;
            this.iPG3_Objects.Text = "Objects";
            this.iPG3_Objects.UseVisualStyleBackColor = true;
            this.iPG3_Objects.Visible = false;
            // 
            // iPG3_SplitCon
            // 
            this.iPG3_SplitCon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG3_SplitCon.Location = new System.Drawing.Point( 2, 2 );
            this.iPG3_SplitCon.Name = "iPG3_SplitCon";
            this.iPG3_SplitCon.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // iPG3_SplitCon.Panel1
            // 
            this.iPG3_SplitCon.Panel1.Controls.Add( this.groupBox7 );
            // 
            // iPG3_SplitCon.Panel2
            // 
            this.iPG3_SplitCon.Panel2.Controls.Add( this.groupBox8 );
            this.iPG3_SplitCon.Size = new System.Drawing.Size( 1015, 585 );
            this.iPG3_SplitCon.SplitterDistance = 311;
            this.iPG3_SplitCon.TabIndex = 7;
            // 
            // groupBox7
            // 
            this.groupBox7.Controls.Add( this.iPG3_StatsControl );
            this.groupBox7.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox7.Location = new System.Drawing.Point( 0, 0 );
            this.groupBox7.Name = "groupBox7";
            this.groupBox7.Padding = new System.Windows.Forms.Padding( 5, 0, 5, 5 );
            this.groupBox7.Size = new System.Drawing.Size( 1015, 311 );
            this.groupBox7.TabIndex = 1;
            this.groupBox7.TabStop = false;
            this.groupBox7.Text = "Statistics";
            // 
            // groupBox8
            // 
            this.groupBox8.Controls.Add( this.iPG3_ObjectPieChart );
            this.groupBox8.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox8.Location = new System.Drawing.Point( 0, 0 );
            this.groupBox8.Name = "groupBox8";
            this.groupBox8.Padding = new System.Windows.Forms.Padding( 5, 0, 5, 5 );
            this.groupBox8.Size = new System.Drawing.Size( 1015, 270 );
            this.groupBox8.TabIndex = 7;
            this.groupBox8.TabStop = false;
            // 
            // iPG3_ObjectPieChart
            // 
            this.iPG3_ObjectPieChart.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG3_ObjectPieChart.Location = new System.Drawing.Point( 5, 14 );
            this.iPG3_ObjectPieChart.Name = "iPG3_ObjectPieChart";
            this.iPG3_ObjectPieChart.ScrollGrace = 0;
            this.iPG3_ObjectPieChart.ScrollMaxX = 0;
            this.iPG3_ObjectPieChart.ScrollMaxY = 0;
            this.iPG3_ObjectPieChart.ScrollMaxY2 = 0;
            this.iPG3_ObjectPieChart.ScrollMinX = 0;
            this.iPG3_ObjectPieChart.ScrollMinY = 0;
            this.iPG3_ObjectPieChart.ScrollMinY2 = 0;
            this.iPG3_ObjectPieChart.Size = new System.Drawing.Size( 1005, 251 );
            this.iPG3_ObjectPieChart.TabIndex = 6;
            // 
            // iPG6_Relationships
            // 
            this.iPG6_Relationships.Controls.Add( this.iPG6_RelationshipControl );
            this.iPG6_Relationships.Location = new System.Drawing.Point( 4, 22 );
            this.iPG6_Relationships.Name = "iPG6_Relationships";
            this.iPG6_Relationships.Size = new System.Drawing.Size( 1019, 589 );
            this.iPG6_Relationships.TabIndex = 6;
            this.iPG6_Relationships.Text = "Relationships";
            this.iPG6_Relationships.UseVisualStyleBackColor = true;
            // 
            // iPG5_Distribution
            // 
            this.iPG5_Distribution.Controls.Add( this.iPG4_DistributionControl );
            this.iPG5_Distribution.Location = new System.Drawing.Point( 4, 22 );
            this.iPG5_Distribution.Name = "iPG5_Distribution";
            this.iPG5_Distribution.Padding = new System.Windows.Forms.Padding( 3 );
            this.iPG5_Distribution.Size = new System.Drawing.Size( 1019, 589 );
            this.iPG5_Distribution.TabIndex = 5;
            this.iPG5_Distribution.Text = "Cell Size Distributions";
            this.iPG5_Distribution.UseVisualStyleBackColor = true;
            // 
            // iPG4_Graphs
            // 
            this.iPG4_Graphs.Controls.Add( this.iPG4_GraphCtrl );
            this.iPG4_Graphs.Controls.Add( this.groupBox2 );
            this.iPG4_Graphs.Location = new System.Drawing.Point( 4, 22 );
            this.iPG4_Graphs.Name = "iPG4_Graphs";
            this.iPG4_Graphs.Padding = new System.Windows.Forms.Padding( 0, 2, 0, 0 );
            this.iPG4_Graphs.Size = new System.Drawing.Size( 1019, 589 );
            this.iPG4_Graphs.TabIndex = 4;
            this.iPG4_Graphs.Text = "Graphs";
            this.iPG4_Graphs.UseVisualStyleBackColor = true;
            // 
            // iPG4_GraphCtrl
            // 
            this.iPG4_GraphCtrl.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iPG4_GraphCtrl.IsShowPointValues = true;
            this.iPG4_GraphCtrl.Location = new System.Drawing.Point( 6, 49 );
            this.iPG4_GraphCtrl.Name = "iPG4_GraphCtrl";
            this.iPG4_GraphCtrl.ScrollGrace = 0;
            this.iPG4_GraphCtrl.ScrollMaxX = 0;
            this.iPG4_GraphCtrl.ScrollMaxY = 0;
            this.iPG4_GraphCtrl.ScrollMaxY2 = 0;
            this.iPG4_GraphCtrl.ScrollMinX = 0;
            this.iPG4_GraphCtrl.ScrollMinY = 0;
            this.iPG4_GraphCtrl.ScrollMinY2 = 0;
            this.iPG4_GraphCtrl.Size = new System.Drawing.Size( 1007, 534 );
            this.iPG4_GraphCtrl.TabIndex = 1;
            // 
            // groupBox2
            // 
            this.groupBox2.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.groupBox2.Controls.Add( this.iPG4_RB_GraphType_AssociatedBinary );
            this.groupBox2.Controls.Add( this.iPG4_RB_GraphType_SizeByIndex );
            this.groupBox2.Controls.Add( this.iPG4_RB_GraphType_CellSizeFree );
            this.groupBox2.Controls.Add( this.iPG4_RB_GraphType_CellSizeAllocated );
            this.groupBox2.Controls.Add( this.iPG4_RB_GraphType_CellOverhead );
            this.groupBox2.Location = new System.Drawing.Point( 6, 5 );
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size( 1007, 38 );
            this.groupBox2.TabIndex = 0;
            this.groupBox2.TabStop = false;
            // 
            // iPG4_RB_GraphType_AssociatedBinary
            // 
            this.iPG4_RB_GraphType_AssociatedBinary.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_RB_GraphType_AssociatedBinary.Location = new System.Drawing.Point( 710, 13 );
            this.iPG4_RB_GraphType_AssociatedBinary.Name = "iPG4_RB_GraphType_AssociatedBinary";
            this.iPG4_RB_GraphType_AssociatedBinary.Size = new System.Drawing.Size( 144, 20 );
            this.iPG4_RB_GraphType_AssociatedBinary.TabIndex = 1;
            this.iPG4_RB_GraphType_AssociatedBinary.Text = "Associated Binary";
            this.iPG4_RB_GraphType_AssociatedBinary.CheckedChanged += new System.EventHandler( this.iPG4_RB_GraphType_CheckedChanged );
            // 
            // iPG4_RB_GraphType_SizeByIndex
            // 
            this.iPG4_RB_GraphType_SizeByIndex.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_RB_GraphType_SizeByIndex.Location = new System.Drawing.Point( 115, 13 );
            this.iPG4_RB_GraphType_SizeByIndex.Name = "iPG4_RB_GraphType_SizeByIndex";
            this.iPG4_RB_GraphType_SizeByIndex.Size = new System.Drawing.Size( 144, 20 );
            this.iPG4_RB_GraphType_SizeByIndex.TabIndex = 0;
            this.iPG4_RB_GraphType_SizeByIndex.Text = "Size by Cell Index";
            this.iPG4_RB_GraphType_SizeByIndex.CheckedChanged += new System.EventHandler( this.iPG4_RB_GraphType_CheckedChanged );
            // 
            // iPG4_RB_GraphType_CellSizeFree
            // 
            this.iPG4_RB_GraphType_CellSizeFree.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_RB_GraphType_CellSizeFree.Location = new System.Drawing.Point( 267, 13 );
            this.iPG4_RB_GraphType_CellSizeFree.Name = "iPG4_RB_GraphType_CellSizeFree";
            this.iPG4_RB_GraphType_CellSizeFree.Size = new System.Drawing.Size( 144, 20 );
            this.iPG4_RB_GraphType_CellSizeFree.TabIndex = 0;
            this.iPG4_RB_GraphType_CellSizeFree.Text = "Free Cell Size";
            this.iPG4_RB_GraphType_CellSizeFree.CheckedChanged += new System.EventHandler( this.iPG4_RB_GraphType_CheckedChanged );
            // 
            // iPG4_RB_GraphType_CellSizeAllocated
            // 
            this.iPG4_RB_GraphType_CellSizeAllocated.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_RB_GraphType_CellSizeAllocated.Location = new System.Drawing.Point( 419, 13 );
            this.iPG4_RB_GraphType_CellSizeAllocated.Name = "iPG4_RB_GraphType_CellSizeAllocated";
            this.iPG4_RB_GraphType_CellSizeAllocated.Size = new System.Drawing.Size( 144, 20 );
            this.iPG4_RB_GraphType_CellSizeAllocated.TabIndex = 0;
            this.iPG4_RB_GraphType_CellSizeAllocated.Text = "Allocated Cell Size";
            this.iPG4_RB_GraphType_CellSizeAllocated.CheckedChanged += new System.EventHandler( this.iPG4_RB_GraphType_CheckedChanged );
            // 
            // iPG4_RB_GraphType_CellOverhead
            // 
            this.iPG4_RB_GraphType_CellOverhead.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_RB_GraphType_CellOverhead.Location = new System.Drawing.Point( 579, 13 );
            this.iPG4_RB_GraphType_CellOverhead.Name = "iPG4_RB_GraphType_CellOverhead";
            this.iPG4_RB_GraphType_CellOverhead.Size = new System.Drawing.Size( 144, 20 );
            this.iPG4_RB_GraphType_CellOverhead.TabIndex = 0;
            this.iPG4_RB_GraphType_CellOverhead.Text = "Cell Overhead";
            this.iPG4_RB_GraphType_CellOverhead.CheckedChanged += new System.EventHandler( this.iPG4_RB_GraphType_CheckedChanged );
            // 
            // iTab2_ColModel_Stats
            // 
            this.iTab2_ColModel_Stats.Columns.AddRange( new XPTable.Models.Column[] {
            this.iTab2_ColModel_Stats_Col_Symbol,
            this.iTab2_ColModel_Stats_Col_AllocationCount,
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance,
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory} );
            // 
            // iTab2_ColModel_Stats_Col_Symbol
            // 
            this.iTab2_ColModel_Stats_Col_Symbol.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_Symbol.Text = "Symbol";
            this.iTab2_ColModel_Stats_Col_Symbol.Width = 270;
            // 
            // iTab2_ColModel_Stats_Col_AllocationCount
            // 
            this.iTab2_ColModel_Stats_Col_AllocationCount.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_AllocationCount.Text = "Allocation Count";
            this.iTab2_ColModel_Stats_Col_AllocationCount.Width = 100;
            // 
            // iTab2_ColModel_Stats_Col_MemoryPerInstance
            // 
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.Text = "Memory-per-Instance";
            this.iTab2_ColModel_Stats_Col_MemoryPerInstance.Width = 100;
            // 
            // iTab2_ColModel_Stats_Col_TotalAllocatedMemory
            // 
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.ContentWidth = 0;
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.Text = "Total Allocated Memory";
            this.iTab2_ColModel_Stats_Col_TotalAllocatedMemory.Width = 110;
            // 
            // iPG1_HeapView_ContextMenu
            // 
            this.iPG1_HeapView_ContextMenu.Items.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships,
            this.iPopupMenu_Show,
            this.iPopupMenu_XRef,
            this.iPopupMenu_GoTo} );
            this.iPG1_HeapView_ContextMenu.Name = "iPG1_HeapView_ContextMenu";
            this.iPG1_HeapView_ContextMenu.Size = new System.Drawing.Size( 150, 92 );
            this.iPG1_HeapView_ContextMenu.Opening += new System.ComponentModel.CancelEventHandler( this.iPG1_HeapView_ContextMenu_Opening );
            // 
            // iPopupMenu_Relationships
            // 
            this.iPopupMenu_Relationships.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships_Outgoing,
            this.iPopupMenu_Relationships_Incoming} );
            this.iPopupMenu_Relationships.Name = "iPopupMenu_Relationships";
            this.iPopupMenu_Relationships.Size = new System.Drawing.Size( 149, 22 );
            this.iPopupMenu_Relationships.Text = "Relationships...";
            // 
            // iPopupMenu_Relationships_Outgoing
            // 
            this.iPopupMenu_Relationships_Outgoing.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships_Outgoing_View,
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs} );
            this.iPopupMenu_Relationships_Outgoing.Name = "iPopupMenu_Relationships_Outgoing";
            this.iPopupMenu_Relationships_Outgoing.Size = new System.Drawing.Size( 130, 22 );
            this.iPopupMenu_Relationships_Outgoing.Text = "Outgoing...";
            // 
            // iPopupMenu_Relationships_Outgoing_View
            // 
            this.iPopupMenu_Relationships_Outgoing_View.Name = "iPopupMenu_Relationships_Outgoing_View";
            this.iPopupMenu_Relationships_Outgoing_View.Size = new System.Drawing.Size( 148, 22 );
            this.iPopupMenu_Relationships_Outgoing_View.Text = "View";
            this.iPopupMenu_Relationships_Outgoing_View.Click += new System.EventHandler( this.iPopupMenu_Relationships_Outgoing_View_Click );
            // 
            // iPopupMenu_Relationships_Outgoing_Breadcrumbs
            // 
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show,
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll} );
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs.Name = "iPopupMenu_Relationships_Outgoing_Breadcrumbs";
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs.Size = new System.Drawing.Size( 148, 22 );
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs.Text = "Breadcrumbs...";
            // 
            // iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show
            // 
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show.Name = "iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show";
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show.Size = new System.Drawing.Size( 109, 22 );
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show.Text = "Show";
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show.Click += new System.EventHandler( this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show_Click );
            // 
            // iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll
            // 
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll.Name = "iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll";
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll.Size = new System.Drawing.Size( 109, 22 );
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll.Text = "Hide All";
            this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll.Click += new System.EventHandler( this.iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll_Click );
            // 
            // iPopupMenu_Relationships_Incoming
            // 
            this.iPopupMenu_Relationships_Incoming.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships_Incoming_View,
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs} );
            this.iPopupMenu_Relationships_Incoming.Name = "iPopupMenu_Relationships_Incoming";
            this.iPopupMenu_Relationships_Incoming.Size = new System.Drawing.Size( 130, 22 );
            this.iPopupMenu_Relationships_Incoming.Text = "Incoming...";
            // 
            // iPopupMenu_Relationships_Incoming_View
            // 
            this.iPopupMenu_Relationships_Incoming_View.Name = "iPopupMenu_Relationships_Incoming_View";
            this.iPopupMenu_Relationships_Incoming_View.Size = new System.Drawing.Size( 148, 22 );
            this.iPopupMenu_Relationships_Incoming_View.Text = "View";
            this.iPopupMenu_Relationships_Incoming_View.Click += new System.EventHandler( this.iPopupMenu_Relationships_Incoming_View_Click );
            // 
            // iPopupMenu_Relationships_Incoming_Breadcrumbs
            // 
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show,
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll} );
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs.Name = "iPopupMenu_Relationships_Incoming_Breadcrumbs";
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs.Size = new System.Drawing.Size( 148, 22 );
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs.Text = "Breadcrumbs...";
            // 
            // iPopupMenu_Relationships_Incoming_Breadcrumbs_Show
            // 
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show.Name = "iPopupMenu_Relationships_Incoming_Breadcrumbs_Show";
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show.Size = new System.Drawing.Size( 109, 22 );
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show.Text = "Show";
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show.Click += new System.EventHandler( this.iPopupMenu_Relationships_Incoming_Breadcrumbs_Show_Click );
            // 
            // iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll
            // 
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll.Name = "iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll";
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll.Size = new System.Drawing.Size( 109, 22 );
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll.Text = "Hide All";
            this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll.Click += new System.EventHandler( this.iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll_Click );
            // 
            // iPopupMenu_Show
            // 
            this.iPopupMenu_Show.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_Show_CellRelationshipInspector,
            this.iPopupMenu_Show_CellContentsViewer} );
            this.iPopupMenu_Show.Name = "iPopupMenu_Show";
            this.iPopupMenu_Show.Size = new System.Drawing.Size( 149, 22 );
            this.iPopupMenu_Show.Text = "Show...";
            // 
            // iPopupMenu_Show_CellRelationshipInspector
            // 
            this.iPopupMenu_Show_CellRelationshipInspector.Name = "iPopupMenu_Show_CellRelationshipInspector";
            this.iPopupMenu_Show_CellRelationshipInspector.Size = new System.Drawing.Size( 201, 22 );
            this.iPopupMenu_Show_CellRelationshipInspector.Text = "Cell Relationship Inspector";
            this.iPopupMenu_Show_CellRelationshipInspector.Click += new System.EventHandler( this.iPopupMenu_Show_CellRelationshipInspector_Click );
            // 
            // iPopupMenu_Show_CellContentsViewer
            // 
            this.iPopupMenu_Show_CellContentsViewer.Name = "iPopupMenu_Show_CellContentsViewer";
            this.iPopupMenu_Show_CellContentsViewer.Size = new System.Drawing.Size( 201, 22 );
            this.iPopupMenu_Show_CellContentsViewer.Text = "Cell Contents Viewer";
            this.iPopupMenu_Show_CellContentsViewer.Click += new System.EventHandler( this.iPopupMenu_Show_CellContentsViewer_Click );
            // 
            // iPopupMenu_XRef
            // 
            this.iPopupMenu_XRef.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_XRef_Settings} );
            this.iPopupMenu_XRef.Name = "iPopupMenu_XRef";
            this.iPopupMenu_XRef.Size = new System.Drawing.Size( 149, 22 );
            this.iPopupMenu_XRef.Text = "X-Ref...";
            // 
            // iPopupMenu_XRef_Settings
            // 
            this.iPopupMenu_XRef_Settings.Font = new System.Drawing.Font( "Tahoma", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ( (byte) ( 0 ) ) );
            this.iPopupMenu_XRef_Settings.Name = "iPopupMenu_XRef_Settings";
            this.iPopupMenu_XRef_Settings.Size = new System.Drawing.Size( 130, 22 );
            this.iPopupMenu_XRef_Settings.Text = "Settings...";
            this.iPopupMenu_XRef_Settings.Click += new System.EventHandler( this.iPopupMenu_XRef_Settings_Click );
            // 
            // iPopupMenu_GoTo
            // 
            this.iPopupMenu_GoTo.DropDownItems.AddRange( new System.Windows.Forms.ToolStripItem[] {
            this.iPopupMenu_GoTo_EmbeddedReferences,
            this.iPopupMenu_GoTo_IncomingReferences} );
            this.iPopupMenu_GoTo.Name = "iPopupMenu_GoTo";
            this.iPopupMenu_GoTo.Size = new System.Drawing.Size( 149, 22 );
            this.iPopupMenu_GoTo.Text = "Go to...";
            // 
            // iPopupMenu_GoTo_EmbeddedReferences
            // 
            this.iPopupMenu_GoTo_EmbeddedReferences.Name = "iPopupMenu_GoTo_EmbeddedReferences";
            this.iPopupMenu_GoTo_EmbeddedReferences.Size = new System.Drawing.Size( 183, 22 );
            this.iPopupMenu_GoTo_EmbeddedReferences.Text = "Outgoing Reference...";
            // 
            // iPopupMenu_GoTo_IncomingReferences
            // 
            this.iPopupMenu_GoTo_IncomingReferences.Name = "iPopupMenu_GoTo_IncomingReferences";
            this.iPopupMenu_GoTo_IncomingReferences.Size = new System.Drawing.Size( 183, 22 );
            this.iPopupMenu_GoTo_IncomingReferences.Text = "Incoming Reference...";
            // 
            // iMenuItem_File_SaveAs_HTML
            // 
            this.iMenuItem_File_SaveAs_HTML.Index = 3;
            this.iMenuItem_File_SaveAs_HTML.Text = "As &HTML...";
            this.iMenuItem_File_SaveAs_HTML.Click += new System.EventHandler( this.iMenuItem_File_SaveAs_HTML_Click );
            // 
            // iPG1_HeapCellInfo
            // 
            this.iPG1_HeapCellInfo.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
            this.iPG1_HeapCellInfo.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG1_HeapCellInfo.Location = new System.Drawing.Point( 3, 12 );
            this.iPG1_HeapCellInfo.MaximumSize = new System.Drawing.Size( 1024, 134 );
            this.iPG1_HeapCellInfo.MinimumSize = new System.Drawing.Size( 387, 134 );
            this.iPG1_HeapCellInfo.Name = "iPG1_HeapCellInfo";
            this.iPG1_HeapCellInfo.ShowStackBasedFunctionAddresses = false;
            this.iPG1_HeapCellInfo.Size = new System.Drawing.Size( 523, 134 );
            this.iPG1_HeapCellInfo.TabIndex = 0;
            // 
            // iPG2_FilterControl
            // 
            this.iPG2_FilterControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG2_FilterControl.FilterType = HeapUiLib.Controls.TFilterType.EFilterShowAll;
            this.iPG2_FilterControl.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG2_FilterControl.Location = new System.Drawing.Point( 0, 0 );
            this.iPG2_FilterControl.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG2_FilterControl.MinimumSize = new System.Drawing.Size( 0, 25 );
            this.iPG2_FilterControl.Name = "iPG2_FilterControl";
            this.iPG2_FilterControl.Size = new System.Drawing.Size( 1015, 25 );
            heapStatistics1.HeapBaseAddress = ( (uint) ( 4294967295u ) );
            heapStatistics1.HeapSize = ( (uint) ( 1u ) );
            this.iPG2_FilterControl.Statistics = heapStatistics1;
            this.iPG2_FilterControl.TabIndex = 0;
            this.iPG2_FilterControl.FilterChanged += new HeapUiLib.Controls.HeapCellListingFilter.FilterChangedHandler( this.iPG2_FilterControl_FilterChanged );
            // 
            // iPG2_CellList_Cells
            // 
            this.iPG2_CellList_Cells.Cells = heapCellArray1;
            this.iPG2_CellList_Cells.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG2_CellList_Cells.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG2_CellList_Cells.Location = new System.Drawing.Point( 0, 0 );
            this.iPG2_CellList_Cells.Name = "iPG2_CellList_Cells";
            this.iPG2_CellList_Cells.SelectedCell = null;
            this.iPG2_CellList_Cells.Size = new System.Drawing.Size( 1015, 561 );
            this.iPG2_CellList_Cells.TabIndex = 1;
            this.iPG2_CellList_Cells.DoubleClick += new System.EventHandler( this.iPG2_CellList_Cells_DoubleClick );
            // 
            // iPG3_StatsControl
            // 
            this.iPG3_StatsControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG3_StatsControl.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG3_StatsControl.Location = new System.Drawing.Point( 5, 14 );
            this.iPG3_StatsControl.Margin = new System.Windows.Forms.Padding( 0 );
            this.iPG3_StatsControl.Name = "iPG3_StatsControl";
            this.iPG3_StatsControl.Size = new System.Drawing.Size( 1005, 292 );
            this.iPG3_StatsControl.TabIndex = 0;
            // 
            // iPG6_RelationshipControl
            // 
            this.iPG6_RelationshipControl.Cells = heapCellArray2;
            this.iPG6_RelationshipControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG6_RelationshipControl.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG6_RelationshipControl.ListMode = HeapUiLib.Controls.HeapCellRelationshipControl.TListMode.EListModeReferencedBy;
            this.iPG6_RelationshipControl.Location = new System.Drawing.Point( 0, 0 );
            this.iPG6_RelationshipControl.Name = "iPG6_RelationshipControl";
            this.iPG6_RelationshipControl.SelectedCell = null;
            this.iPG6_RelationshipControl.Size = new System.Drawing.Size( 1019, 589 );
            heapStatistics2.HeapBaseAddress = ( (uint) ( 4294967295u ) );
            heapStatistics2.HeapSize = ( (uint) ( 1u ) );
            this.iPG6_RelationshipControl.Statistics = heapStatistics2;
            this.iPG6_RelationshipControl.TabIndex = 0;
            this.iPG6_RelationshipControl.DoubleClick += new System.EventHandler( this.iPG6_RelationshipControl_DoubleClick );
            // 
            // iPG4_DistributionControl
            // 
            this.iPG4_DistributionControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.iPG4_DistributionControl.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.iPG4_DistributionControl.Location = new System.Drawing.Point( 3, 3 );
            this.iPG4_DistributionControl.Name = "iPG4_DistributionControl";
            this.iPG4_DistributionControl.Size = new System.Drawing.Size( 1013, 583 );
            this.iPG4_DistributionControl.TabIndex = 0;
            // 
            // HeapViewerForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size( 5, 14 );
            this.ClientSize = new System.Drawing.Size( 1031, 608 );
            this.Controls.Add( this.iPanel_Main );
            this.Font = new System.Drawing.Font( "Tahoma", 8.25F );
            this.Menu = this.iMainMenu;
            this.MinimumSize = new System.Drawing.Size( 1000, 642 );
            this.Name = "HeapViewerForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Heap Analyser";
            this.Load += new System.EventHandler( this.HeapViewerForm_Load );
            this.Closing += new System.ComponentModel.CancelEventHandler( this.HeapViewerForm_Closing );
            this.KeyDown += new System.Windows.Forms.KeyEventHandler( this.HeapViewerForm_KeyDown );
            this.iPanel_Main.ResumeLayout( false );
            this.iTabs.ResumeLayout( false );
            this.iPG1_HeapView.ResumeLayout( false );
            this.iPG1_SplitCon.Panel1.ResumeLayout( false );
            this.iPG1_SplitCon.Panel2.ResumeLayout( false );
            this.iPG1_SplitCon.ResumeLayout( false );
            this.iPG1_TLP_Top.ResumeLayout( false );
            this.iPG1_FLP_Top.ResumeLayout( false );
            this.groupBox1.ResumeLayout( false );
            this.groupBox1.PerformLayout();
            this.groupBox5.ResumeLayout( false );
            this.iPG1_TLP_Bottom.ResumeLayout( false );
            this.groupBox6.ResumeLayout( false );
            this.iPG1_FLP_NavigateAndSearch.ResumeLayout( false );
            this.groupBox4.ResumeLayout( false );
            this.iPG1_GP_Navigate_ByAllocNumber.ResumeLayout( false );
            this.groupBox3.ResumeLayout( false );
            this.groupBox3.PerformLayout();
            ( (System.ComponentModel.ISupportInitialize) ( this.iPG1_NumUD_Search_ByAllocNumber ) ).EndInit();
            this.iPG2_CellList.ResumeLayout( false );
            this.iPG2_SplitCon.Panel1.ResumeLayout( false );
            this.iPG2_SplitCon.Panel2.ResumeLayout( false );
            this.iPG2_SplitCon.ResumeLayout( false );
            this.iPG3_Objects.ResumeLayout( false );
            this.iPG3_SplitCon.Panel1.ResumeLayout( false );
            this.iPG3_SplitCon.Panel2.ResumeLayout( false );
            this.iPG3_SplitCon.ResumeLayout( false );
            this.groupBox7.ResumeLayout( false );
            this.groupBox8.ResumeLayout( false );
            this.iPG6_Relationships.ResumeLayout( false );
            this.iPG5_Distribution.ResumeLayout( false );
            this.iPG4_Graphs.ResumeLayout( false );
            this.groupBox2.ResumeLayout( false );
            this.iPG1_HeapView_ContextMenu.ResumeLayout( false );
            this.ResumeLayout( false );

        }
		#endregion

		#region API
		#endregion

		#region Properties
		public HeapReconstructor Reconstructor
		{
			get { return iReconstructor; }
		}

		public HeapCellArray CellCollection
		{
			get { return iReconstructor.Data; }
		}

		public HeapCell FocusedCell
		{
			get { return iPG1_HeapView_Viewer.FocusedCell; }
            set { iPG1_HeapView_Viewer.FocusedCell = value; }
		}
		#endregion

        #region Tab Pages

        #region Page 1 - Heap View
        private void SetupPage1()
        {
            // Update the heap viewer control with the reconstructor
            iPG1_HeapView_Viewer.Reconstructor = iReconstructor;

            // Can't use the "cell age" type if we don't have a debug
            // allocator.
            bool hideAge = iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses || !iReconstructor.IsDebugAllocator;
            if ( hideAge )
            {
                iMenuItem_View_Type.MenuItems.Remove( iMenuItem_View_Type_Age );
                iPG1_RB_HeapView_ByCellAge.Enabled = false;
            }

            // Can't navigate by allocation number unless we have a debug allocator
            iPG1_GP_Navigate_ByAllocNumber.Visible = iReconstructor.IsDebugAllocator && !iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses;
            iPG1_NumUD_Search_ByAllocNumber.Enabled = iReconstructor.IsDebugAllocator && !iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses;
            iPG1_BT_Search_ByAllocNumber.Enabled = iReconstructor.IsDebugAllocator && !iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses;

            // Make sure the heap view type is reset
            iPG1_RB_HeapView_ByCellType.Checked = true;
            iMenuItem_View_Zoom_Medium.Checked = true;
            
            // Seed alloc num
            iPG1_NumUD_Search_ByAllocNumber.Minimum = 1;
            iPG1_NumUD_Search_ByAllocNumber.Maximum = iReconstructor.Data.Count;

            // If the heap data includes stack-based function addresses rather than 
            // alloc number and nesting level, then change the list view text accordingly.
            iPG1_HeapCellInfo.ShowStackBasedFunctionAddresses = iReconstructor.SourceData.MetaData.Heap.IsDebugAllocatorWithStoredStackAddresses;
        }

        private void iPG1_HeapView_Viewer_CellSelected( HeapLib.Cells.HeapCell aCell )
        {
            iPG1_HeapCellInfo.Cell = aCell;
            iPG1_UpdateNavigationButtons();
            //
            if	( HeapCellSelectedObserver != null )
            {
                HeapCellSelectedObserver( aCell );
            }
        }

        private void iPG1_HeapView_Viewer_CellDoubleClicked( HeapCell aCell )
        {
            // Create a new popup window for the double clicked cell's contents
            HeapCellContentsForm popupContents = new HeapCellContentsForm( this, aCell );
            popupContents.Show();
        }

        private void iPG1_HeapView_Viewer_CellRightClicked( HeapCell aCell, RawItem aItem, Point aScreenPos )
        {
            iPG1_HeapView_ContextMenu.Tag = new CellRightClickMetaData( aCell, aItem );
            iPG1_HeapView_ContextMenu.Show( aScreenPos );
        }

        private void iPG1_BT_Navigate_ByPosition_Next_Click(object sender, System.EventArgs e)
        {
            HeapCell focusedCell = iPG1_HeapView_Viewer.FocusedCell;
            if ( focusedCell != null )
            {
                int cellCount = iReconstructor.Data.Count;
                int index = iReconstructor.Data.CellIndex( focusedCell );
                if ( index + 1 < cellCount )
                {
                    HeapCell newFocusingCell = iReconstructor.Data[ index + 1 ];
                    FocusedCell = newFocusingCell;
                }
            }
        }

        private void iPG1_BT_Navigate_ByPosition_Previous_Click(object sender, System.EventArgs e)
        {
            HeapCell focusedCell = iPG1_HeapView_Viewer.FocusedCell;
            if  ( focusedCell != null )
            {
                int cellCount = iReconstructor.Data.Count;
                int index = iReconstructor.Data.CellIndex( focusedCell );
                if  ( index-1 >= 0 )
                {
                    HeapCell newFocusingCell = iReconstructor.Data[ index - 1 ];
                    FocusedCell = newFocusingCell;
                }
            }
        }

        private void iPG1_BT_Navigate_ByAllocNumber_Next_Click(object sender, System.EventArgs e)
        {
            HeapCell focusedCell = iPG1_HeapView_Viewer.FocusedCell;
            if  ( focusedCell != null )
            {
                uint allocNum = focusedCell.AllocationNumber;
                HeapCell cell = iReconstructor.Data.CellByAllocationNumberIndexed( allocNum, 1 );
                FocusedCell = cell;
            }
        }

        private void iPG1_BT_Navigate_ByAllocNumber_Previous_Click(object sender, System.EventArgs e)
        {
            HeapCell focusedCell = iPG1_HeapView_Viewer.FocusedCell;
            if  ( focusedCell != null )
            {
                uint allocNum = focusedCell.AllocationNumber;
                HeapCell cell = iReconstructor.Data.CellByAllocationNumberIndexed( allocNum, -1 );
                FocusedCell = cell;
            }
        }

        private void iPG1_BT_HeapView_Filters_Click( object sender, EventArgs e )
        {
            iPG1_HeapView_Viewer.SetupFilters();
        }

        private void iPG1_BT_Search_ByAllocNumber_Click(object sender, System.EventArgs e)
        {
            // Find the starting address of the item that is specified
            HeapCell cell = iReconstructor.Data.CellByAllocationNumber( (uint) iPG1_NumUD_Search_ByAllocNumber.Value );
            if	( cell != null )
            {
                FocusedCell = cell;
            }
        }

        private void iPG1_BT_Search_ByAddress_Click( object sender, System.EventArgs e )
        {
            // Convert the string to a number
            string addressString = iPG1_TB_Search_ByAddress.Text;
            long address;
            NumberBaseUtils.TNumberBase numberBase;
            if	( NumberBaseUtils.TextToDecimalNumber( ref addressString, out address, out numberBase ) )
            {
                HeapCell cell = iReconstructor.Data.CellByAddress( (uint) address );
                if	( cell != null )
                {
                    FocusedCell = cell;
                }
            }
        }

        private void iPG1_TB_Search_ByAddress_KeyDown(object sender, System.Windows.Forms.KeyEventArgs e)
        {
            if	( e.KeyCode == Keys.Enter )
            {
                iPG1_BT_Search_ByAddress_Click( this, System.EventArgs.Empty );
                e.Handled = true;
            }
        }
 
        private void iPG1_RB_HeapView_ViewType_CheckedChanged(object sender, System.EventArgs e)
        {
            HeapCtrlLib.Types.THeapCtrlRenderingType type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell;
            ClearAllMenuViewTypeCheckboxes();
            //
            if  ( iPG1_RB_HeapView_ByObjectType.Checked )
            {
                iMenuItem_View_Type_Object.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeBySymbol;
            }
            else if ( iPG1_RB_HeapView_ByParentBinary.Checked )
            {
                iMenuItem_View_Type_ParentBinary.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByObject;
            }
            else if ( iPG1_RB_HeapView_ByCellLength.Checked )
            {
                iMenuItem_View_Type_Length.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByLength;
            }
            else if ( iPG1_RB_HeapView_ByCellAge.Checked )
            {
                iMenuItem_View_Type_Age.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByAge;
            }
            else if ( iPG1_RB_HeapView_ByIsolation.Checked )
            {
                iMenuItem_View_Type_Isolation.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByIsolation;
            }
            else if ( iPG1_RB_HeapView_ByEmbeddedReferences.Checked )
            {
                iMenuItem_View_Type_EmbeddedReferences.Checked = true;
                type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByEmbeddedReferences;
            }
            else
            {
                iMenuItem_View_Type_Cell.Checked = true;
            }
            
            // Switch renderer type
            iPG1_HeapView_Viewer.Type = type;

            // Enable/disable filters button if supported
            iPG1_BT_HeapView_Filters.Enabled = iPG1_HeapView_Viewer.SupportsFiltering;
            
        }

        private void iPG1_NumUD_Search_ByAllocNumber_KeyDown(object sender, System.Windows.Forms.KeyEventArgs e)
        {
            if	( e.KeyCode == Keys.Enter )
            {
                iPG1_BT_Search_ByAllocNumber_Click( this, System.EventArgs.Empty );
                e.Handled = true;
            }
        }

        private void iPG1_UpdateNavigationButtons()
        {
            HeapCell focusedCell = iPG1_HeapView_Viewer.FocusedCell;
            if  ( focusedCell != null )
            {
                int index = iReconstructor.Data.CellIndex( focusedCell );
                int count = iReconstructor.Data.Count;

                // Navigate by cell index
                iPG1_BT_Navigate_ByPosition_Previous.Enabled = ( index > 0 );
                iPG1_BT_Navigate_ByPosition_Next.Enabled = ( index < count - 1 );

                // Navigate by allocation number
                if  ( iPG1_GP_Navigate_ByAllocNumber.Visible )
                {
                    bool isAllocCell = ( focusedCell.Type == HeapCell.TType.EAllocated );
                    uint nextAllocNum  = focusedCell.AllocationNumber + 1;
                    bool nextAvailable = iReconstructor.Data.AllocationNumberInRange( nextAllocNum );
                    iPG1_BT_Navigate_ByAllocNumber_Next.Enabled = isAllocCell && nextAvailable; 
                    //
                    uint prevAllocNum  = focusedCell.AllocationNumber - 1;
                    bool prevAvailable = iReconstructor.Data.AllocationNumberInRange( prevAllocNum );
                    iPG1_BT_Navigate_ByAllocNumber_Previous.Enabled = isAllocCell && prevAvailable; 
                }
            }
        }

        private void ClearAllMenuViewTypeCheckboxes()
        {
            iMenuItem_View_Type_Cell.Checked = false;
            iMenuItem_View_Type_Age.Checked = false;
            iMenuItem_View_Type_Length.Checked = false;
            iMenuItem_View_Type_Object.Checked = false;
            iMenuItem_View_Type_ParentBinary.Checked = false;
            iMenuItem_View_Type_EmbeddedReferences.Checked = false;
            iMenuItem_View_Type_Isolation.Checked = false;
        }

        private void ClearAllMenuViewZoomCheckboxes()
        {
            iMenuItem_View_Zoom_Large.Checked = false;
            iMenuItem_View_Zoom_Medium.Checked = false;
            iMenuItem_View_Zoom_Small.Checked = false;
        }
        #endregion

        #region Page 2 - Cell List
        private void SetupPage2()
        {
            // Update the cell listing object with the list of heap cells
            iPG2_CellList_Cells.Cells = iReconstructor.Data;

            // Prepare filter list
            iPG2_FilterControl.Statistics = iReconstructor.Statistics;
        }

        private void iPG2_FilterControl_FilterChanged( TFilterType aFilter, SymbianStructuresLib.Debug.Symbols.Symbol aSymbolOrNull )
        {
            if ( aSymbolOrNull != null )
            {
                iPG2_CellList_Cells.SetFilter( aFilter, aSymbolOrNull );
            }
            else
            {
                iPG2_CellList_Cells.SetFilter( aFilter );
            }
        }

        private void iPG2_CellList_Cells_DoubleClick(object sender, System.EventArgs e)
        {
            HeapCell selectedCell = iPG2_CellList_Cells.SelectedCell;
            if  ( selectedCell != null )
            {
                FocusedCell = selectedCell;
                iTabs.SelectedTab = iPG1_HeapView;
                iPG1_HeapView.Select();
                iPG1_HeapView.Focus();
            }
        }

        private void iPG2_CellList_Cells_KeyDown( object sender, System.Windows.Forms.KeyEventArgs e )
        {
            bool wasControl = ( e.Modifiers & Keys.Control ) == Keys.Control;
            if	( wasControl && ( e.KeyCode == Keys.C ) )
            {
                iPG2_CellList_Cells.CopySelectedDataToClipboard();
            }
        }
        #endregion

        #region Page 3 - Object/Symbol Information
        private void SetupPage3()
        {
            iPG3_StatsControl.Reconstructor = this.Reconstructor;
            PrepareStatsGraph();
        }

		private void PrepareStatsGraph()
		{
			GraphPane myPane = iPG3_ObjectPieChart.GraphPane;

			// Set the GraphPane title
			myPane.Title.Text = "Top 10 Cell\nAllocation Distribution";
            myPane.Title.FontSpec.Size = 20f;
            myPane.Title.FontSpec.Family = "Verdana";

			// Fill the pane background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.LightBlue, 45.0f );

            // No fill for the chart background
            myPane.Chart.Fill.Type = FillType.None;

			// Set the legend to an arbitrary location
			myPane.Legend.Position = LegendPos.Float ;
			myPane.Legend.Location = new Location( 0.98f, 0.04f, CoordType.PaneFraction, AlignH.Right, AlignV.Top );
			myPane.Legend.FontSpec.Size = 8f;
			myPane.Legend.IsHStack = false;

			// Find top ten memory allocations
            SymbolTrackingInfoCollection topTen = new SymbolTrackingInfoCollection( iReconstructor.Statistics.StatsAllocated.TrackerSymbols );
			topTen.SortByAllocatedMemory();

            // If there are more than ten entries then discard the smallest ones
            if	( topTen.Count > 10 )
			{
				int amountToRemove = topTen.Count - 10;
				topTen.RemoveRange( 10, amountToRemove );
			}

			// Add some pie slices
			System.Drawing.Color[] colors = 
				{
				Color.Navy,
				Color.Purple,
				Color.LimeGreen,
				Color.SandyBrown,
				Color.Red,
				Color.Blue,
				Color.Green,
				Color.Pink,
				Color.Yellow,
				Color.Orange
				};

			int i = 0;
            foreach( TrackingInfo item in topTen )
            {
				PieItem segment = myPane.AddPieSlice( (double) item.AssociatedMemory, colors[ i++ ], Color.White, 45f, 0, item.Symbol.NameWithoutVTablePrefix );
			}
			 
			iPG3_ObjectPieChart.AxisChange();
			iPG3_ObjectPieChart.Refresh();
		}
        #endregion

        #region Page 4 - Graphs
        private void SetupPage4()
        {
            iPG4_RB_GraphType_SizeByIndex.Checked = true;
            iPG4_RB_GraphType_CheckedChanged( this, EventArgs.Empty );
        }

        private void iPG4_RB_GraphType_CheckedChanged( object aSender, System.EventArgs e )
        {
            if ( aSender is RadioButton )
            {
                RadioButton button = (RadioButton) aSender;
                //
                if ( button.Checked )
                {
                    Size size = iPG4_GraphCtrl.Size;
                    Point pos = iPG4_GraphCtrl.Location;

                    iPG4_GraphCtrl.Dispose();

                    iPG4_Graphs.SuspendLayout();
                    iPG4_GraphCtrl = new ZedGraphControl();
                    iPG4_Graphs.Controls.Add( iPG4_GraphCtrl );
                    iPG4_GraphCtrl.Anchor = ( (System.Windows.Forms.AnchorStyles) ( ( ( ( System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom )
                        | System.Windows.Forms.AnchorStyles.Left )
                        | System.Windows.Forms.AnchorStyles.Right ) ) );
                    iPG4_GraphCtrl.Name = "iPG4_GraphCtrl";
                    iPG4_GraphCtrl.Location = pos;
                    iPG4_GraphCtrl.Size = size;
                    iPG4_GraphCtrl.IsShowPointValues = true;
                    iPG4_Graphs.ResumeLayout();

                    GraphPane myPane = iPG4_GraphCtrl.GraphPane;

                    if ( iPG4_RB_GraphType_SizeByIndex.Checked )
                    {
                        iPG4_PrepareGraph_SizeByIndex();
                    }
                    else if ( iPG4_RB_GraphType_CellSizeFree.Checked )
                    {
                        iPG4_PrepareGraph_CellSizeFree();
                    }
                    else if ( iPG4_RB_GraphType_CellSizeAllocated.Checked )
                    {
                        iPG4_PrepareGraph_CellSizeAllocated();
                    }
                    else if ( iPG4_RB_GraphType_CellOverhead.Checked )
                    {
                        iPG4_PrepareGraph_CellOverhead();
                    }
                    else if ( iPG4_RB_GraphType_AssociatedBinary.Checked )
                    {
                        iPG4_PrepareGraph_AssociatedBinary();
                    }
                }
            }
        }

        private void iPG4_PrepareGraph_SizeByIndex()
		{
			GraphPane myPane = iPG4_GraphCtrl.GraphPane;
            myPane.CurveList.Clear();
            myPane.GraphObjList.Clear();

			// Set the titles and axis labels
			myPane.Title.Text = "Size by Index";
            myPane.Title.FontSpec.Size = 16;
            myPane.XAxis.Title.Text = "Index";
			myPane.YAxis.Title.Text = "Allocation Size";
            myPane.Legend.IsVisible = false;

			// Build a PointPairList with points based on Sine wave
			PointPairList cellsAllocated = new PointPairList();
            PointPairList cellsFree = new PointPairList();

			HeapCellArray data = iReconstructor.Data;
			int count = data.Count;
			for( int i=0; i<count; i++ )
			{
				HeapCell cell = data[ i ];
				//
				double x = (double) i;
				double y = (double) cell.Length;
				//
                if ( cell.Type == HeapCell.TType.EAllocated )
                {
                    cellsAllocated.Add( x, y );
                }
                else if ( cell.Type == HeapCell.TType.EFree )
                {
                    cellsFree.Add( x, y );
                }
            }

            // Generate a blue curve with diamond symbols, and "Free" in the legend
            LineItem curveFree = myPane.AddCurve( "Free", cellsFree, Color.Blue, SymbolType.Diamond );
            curveFree.Symbol.Size = 5;
            curveFree.Symbol.Fill = new Fill( Color.DarkBlue, Color.LightBlue );
            curveFree.Symbol.Fill.Type = FillType.GradientByY;
            curveFree.Symbol.Fill.RangeMax = iReconstructor.Statistics.StatsFree.CellLargest.Length;
            curveFree.Symbol.Fill.RangeMin = 0;
            //curveFree.Symbol.Border.IsVisible = false;
            curveFree.Line.IsVisible = false;

            // Generate a red curve with diamond symbols, and "Free" in the legend
            LineItem curveAllocated = myPane.AddCurve( "Allocated", cellsAllocated, Color.Red, SymbolType.Circle );
            curveAllocated.Symbol.Size = 5;
            curveAllocated.Symbol.Fill = new Fill( Color.LightPink, Color.DarkRed );
            curveAllocated.Symbol.Fill.Type = FillType.GradientByY;
            curveAllocated.Symbol.Fill.RangeMax = iReconstructor.Statistics.StatsAllocated.CellLargest.Length;
            curveAllocated.Symbol.Fill.RangeMin = 0;
            //curveAllocated.Symbol.Border.IsVisible = false;
            curveAllocated.Line.IsVisible = false;

            // Show the X and Y grids
            myPane.XAxis.MajorGrid.IsVisible = true;
            myPane.YAxis.MajorGrid.IsVisible = true;

            // Set the x and y scale and title font sizes to 14
            myPane.XAxis.Scale.FontSpec.Size = 14;
            myPane.XAxis.Title.FontSpec.Size = 14;
            myPane.YAxis.Scale.FontSpec.Size = 14;
            myPane.YAxis.Title.FontSpec.Size = 14;

            // Fill the axis background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.SteelBlue, 45.0F );
            myPane.Chart.Fill = new Fill( Color.White, Color.Gainsboro, 90F );

            iPG4_GraphCtrl.AxisChange();
            iPG4_GraphCtrl.RestoreScale( myPane );

            /*

			// Hide the legend
			myPane.Legend.IsVisible = false;

			// Add a curve
			LineItem curve = myPane.AddCurve( "label", list, Color.Red, SymbolType.Circle );
			curve.Line.Width = 1.5F;
			curve.Symbol.Fill = new Fill( Color.White );
			curve.Symbol.Size = 5;

			// Make the XAxis start with the first label at 50
            myPane.XAxis.Scale.BaseTic = 50;

            myPane.BarSettings.Type = BarType.Cluster;
            myPane.BarSettings.Base = BarBase.X;
			
			// Fill the axis background with a gradient
			myPane.Fill = new Fill( Color.White, Color.SteelBlue, 45.0F );

            iPG4_GraphCtrl.Invalidate();
             * */
        }

        private void iPG4_PrepareGraph_CellOverhead()
        {
            GraphPane myPane = iPG4_GraphCtrl.GraphPane;
            myPane.CurveList.Clear();
            myPane.GraphObjList.Clear();

            // Set the title and axis labels
            myPane.Title.Text = "Cell Overhead";
            myPane.YAxis.Title.Text = "Type";
            myPane.XAxis.Title.Text = "Size";
			
            // Y-axis labels
            string[] yAxisLabels = { "Free", "Allocated (with Object)", "Allocated (Unknown)" };

            double freeHeader = (double) iReconstructor.Statistics.StatsFree.TypeSizeHeader;
            double freePayload = (double) iReconstructor.Statistics.StatsFree.TypeSizePayload;
            double allocatedHeader = (double) iReconstructor.Statistics.StatsAllocated.TypeSizeHeader;
            double allocatedPayload = (double) iReconstructor.Statistics.StatsAllocated.TypeSizePayload;

            // We can work out the symbol vs no-symbol count based upon the tracker data.
            double allocatedWithSymbolHeader = (double) ( iReconstructor.Statistics.StatsAllocated.TrackerSymbols.CellMatchCount * HeapCell.AllocatedCellHeaderSize );
            double allocatedWithSymbolPayload = (double) ( iReconstructor.Statistics.StatsAllocated.TrackerSymbols.TypeSize - allocatedWithSymbolHeader );
            double allocatedNoSymbolHeader = allocatedHeader - allocatedWithSymbolHeader;
            double allocatedNoSymbolPayload = allocatedPayload - allocatedWithSymbolPayload;

            double[] headerPoints = { freeHeader, allocatedWithSymbolHeader, allocatedNoSymbolHeader };
            double[] payloadPoints = { freePayload, allocatedWithSymbolPayload, allocatedNoSymbolPayload };

            // Shoiw the legend
            myPane.Legend.IsVisible = true;

            // x-axis curve one (red) - header size
            BarItem myCurve = myPane.AddBar( "Header", headerPoints, null, Color.Red );
            myCurve.Bar.Fill = new Fill( Color.Red, Color.White, Color.Red, 90f );

            // x-axis curve one (blue) - payload size
            myCurve = myPane.AddBar( "Payload", payloadPoints, null, Color.Blue );
            myCurve.Bar.Fill = new Fill( Color.Blue, Color.White, Color.Blue, 90f );

            // Draw the Y tics between the labels instead of at the labels
            myPane.YAxis.MajorTic.IsAllTics = false;
            myPane.YAxis.MajorTic.IsBetweenLabels = false;

            // Set the YAxis labels
            myPane.YAxis.Scale.TextLabels = yAxisLabels;

            // Set the YAxis to Text type
            myPane.YAxis.Type = AxisType.Text;

            // Set the bar type to stack, which stacks the bars by automatically accumulating the values
            myPane.BarSettings.Type = BarType.Stack;

            // Make the bars horizontal by setting the BarBase to "Y"
            myPane.BarSettings.Base = BarBase.Y;

            // Fill the axis background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.SteelBlue, 45.0F );
            myPane.Chart.Fill = new Fill( Color.White, Color.Gainsboro, 90F );

            iPG4_GraphCtrl.AxisChange();

            BarItem.CreateBarLabels( myPane, true, "f0" );
        }

        private void iPG4_PrepareGraph_CellSizeFree()
        {
            // get a reference to the GraphPane
            GraphPane myPane = iPG4_GraphCtrl.GraphPane;
            myPane.CurveList.Clear();
            myPane.GraphObjList.Clear();

            // Set the Titles
            myPane.Title.Text = "Free Cell Length Distribution";
            myPane.XAxis.Title.Text = "Cell Length";
            myPane.YAxis.Title.Text = "Free Cell Count";

            // Get distribution
            HeapLib.Statistics.Distribution.HeapCellSizeDistribution distribution = iReconstructor.Statistics.StatsFree.Distribution;

            PointPairList points = new PointPairList();
            ArrayList labels = new ArrayList( distribution.Count );

            int index = 1;
            double maxCount = -1;
            foreach( DictionaryEntry entry in distribution )
            {
                double cellLength = (double) ( (uint) entry.Key );
                double matchingCells = (double) ( (uint) entry.Value );

                maxCount = Math.Max( matchingCells, maxCount );

                labels.Add( cellLength.ToString() );
                labels.Add( string.Empty );
                points.Add( index, matchingCells ); 
                index += 2;
            }

            // Create the curve
            BarItem myCurve = myPane.AddBar( string.Empty, points, Color.CadetBlue );
            myCurve.IsOverrideOrdinal = true;
            myCurve.Bar.Fill = new Fill( Color.Blue, Color.White, Color.Blue, 90f );

            // Set up X-axis
            myPane.XAxis.Type = AxisType.Text;
            myPane.XAxis.Scale.TextLabels = (string[]) labels.ToArray( typeof( string ) );
            myPane.XAxis.Scale.Min = 1;
            myPane.XAxis.Scale.Max = (double) index + 2;
            myPane.XAxis.Scale.MajorStep = 1;
            myPane.XAxis.MinorTic.IsAllTics = false;
            myPane.XAxis.MajorTic.IsAllTics = true;
            myPane.YAxis.MajorTic.IsInside = false;
            
            // Set the extent of the Y-axis
            myPane.YAxis.Scale.Min = 0;
            myPane.YAxis.Scale.Max = maxCount + 20;
            myPane.YAxis.Scale.MajorStep = 1;
            myPane.YAxis.MinorTic.IsAllTics = false;
            myPane.YAxis.MajorTic.IsBetweenLabels = false;
            myPane.YAxis.MajorTic.IsAllTics = true;
            myPane.YAxis.MajorTic.IsOutside = true;

            myPane.BarSettings.Type = BarType.Cluster;
            myPane.BarSettings.Base = BarBase.X;

            // Show the X and Y grids
            myPane.XAxis.MajorGrid.IsVisible = true;
            myPane.YAxis.MajorGrid.IsVisible = true;

            // Fill the axis background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.SteelBlue, 45.0F );
            myPane.Chart.Fill = new Fill( Color.White, Color.Gainsboro, 90F );

            // Asynch redraw request
            iPG4_GraphCtrl.AxisChange();
            iPG4_GraphCtrl.RestoreScale( myPane );
        }

        private void iPG4_PrepareGraph_CellSizeAllocated()
        {
            // get a reference to the GraphPane
            GraphPane myPane = iPG4_GraphCtrl.GraphPane;
            myPane.CurveList.Clear();
            myPane.GraphObjList.Clear();

            // Set the Titles
            myPane.Title.Text = "Allocated Cell Length Distribution";
            myPane.XAxis.Title.Text = "Cell Length";
            myPane.YAxis.Title.Text = "Allocated Cell Count";

            // Get distribution
            HeapLib.Statistics.Distribution.HeapCellSizeDistribution distribution = iReconstructor.Statistics.StatsAllocated.Distribution;

            PointPairList points = new PointPairList();
            ArrayList labels = new ArrayList( distribution.Count );

            int index = 1;
            double maxCount = -1;
            foreach ( DictionaryEntry entry in distribution )
            {
                double cellLength = (double) ( (uint) entry.Key );
                double matchingCells = (double) ( (uint) entry.Value );

                maxCount = Math.Max( matchingCells, maxCount );

                labels.Add( cellLength.ToString() );
                labels.Add( string.Empty );
                points.Add( index, matchingCells );
                index += 2;
            }

            // Create the curve
            BarItem myCurve = myPane.AddBar( string.Empty, points, Color.CadetBlue );
            myCurve.IsOverrideOrdinal = true;
            myCurve.Bar.Fill = new Fill( Color.Red, Color.White, Color.Red, 90f );

            // Set up X-axis
            myPane.XAxis.Type = AxisType.Text;
            myPane.XAxis.Scale.TextLabels = (string[]) labels.ToArray( typeof( string ) );
            myPane.XAxis.Scale.Min = 1;
            myPane.XAxis.Scale.Max = (double) index + 2;
            myPane.XAxis.Scale.MajorStep = 1;
            myPane.XAxis.MinorTic.IsAllTics = false;
            myPane.XAxis.MajorTic.IsAllTics = true;
            myPane.YAxis.MajorTic.IsInside = false;

            // Set the extent of the Y-axis
            myPane.YAxis.Scale.Min = 0;
            myPane.YAxis.Scale.Max = maxCount + 20;
            myPane.YAxis.Scale.MajorStep = 1;
            myPane.YAxis.MinorTic.IsAllTics = false;
            myPane.YAxis.MajorTic.IsAllTics = false;
            myPane.YAxis.MajorTic.IsBetweenLabels = false;

            myPane.BarSettings.Type = BarType.Cluster;
            myPane.BarSettings.Base = BarBase.X;

            // Show the X and Y grids
            myPane.XAxis.MajorGrid.IsVisible = true;
            myPane.YAxis.MajorGrid.IsVisible = true;

            // Fill the axis background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.SteelBlue, 45.0F );
            myPane.Chart.Fill = new Fill( Color.White, Color.Gainsboro, 90F );

            // Asynch redraw request
            iPG4_GraphCtrl.AxisChange();
            iPG4_GraphCtrl.RestoreScale( myPane );
        }

        private void iPG4_PrepareGraph_AssociatedBinary()
        {
            GraphPane myPane = iPG4_GraphCtrl.GraphPane;
            myPane.CurveList.Clear();
            myPane.GraphObjList.Clear();

            // Set the GraphPane title
            myPane.Title.Text = "Allocated Cells\nby Asssociated Binary";
            myPane.Title.FontSpec.Size = 20f;
            myPane.Title.FontSpec.Family = "Verdana";

            // Fill the pane background with a color gradient
            myPane.Fill = new Fill( Color.White, Color.LightBlue, 45.0f );

            // No fill for the chart background
            myPane.Chart.Fill.Type = FillType.None;

            // Set the legend to an arbitrary location
            myPane.Legend.Position = LegendPos.Float;
            myPane.Legend.Location = new Location( 0.99f, 0.01f, CoordType.PaneFraction, AlignH.Right, AlignV.Top );
            myPane.Legend.FontSpec.Size = 8f;
            myPane.Legend.IsHStack = false;

            SymbianUtils.Colour.ColourGenerationUtil colorUtil = new SymbianUtils.Colour.ColourGenerationUtil();
            colorUtil.SuplimentStandardColoursWithAdditionalEntries( iReconstructor.Statistics.StatsAllocated.TrackerObjects.Count + 1 );

            // Calculate the 5% total amount. First we get the number of cells that had object(symbols)
            // associated with them.
            long totalObjectCount = iReconstructor.Statistics.StatsAllocated.TrackerObjects.CellMatchCount;

            // Constants
            const double KMinValueToShow = 0.01; // percent of total

            // Then work out the 5% figure
            double minValuePercentageTotal = (double) ( totalObjectCount * KMinValueToShow );

            // Work out which is the largest entry
            TrackingInfo largestItem = iReconstructor.Statistics.StatsAllocated.TrackerObjects.EntryWithLargestCount;

            double othersAmount = 0.0;
            int i = 0;
            foreach ( TrackingInfo item in iReconstructor.Statistics.StatsAllocated.TrackerObjects )
            {
                // Get entry count
                int entryCount = item.Count;

                // If it contains more than 5% of the total, then include it, otherwise it gets
                // stuck in the "Others" basket.
                if ( entryCount >= minValuePercentageTotal )
                {
                    // Get colour for pie slice
                    KnownColor kc = colorUtil.StandardColors[ i++ ];
                    Color col = Color.FromKnownColor( kc );

                    // Build label 
                    string label = item.Symbol.ObjectWithoutSection;
                    if ( !item.IsUnknownSymbolMatchItem )
                    {
                        label = label.ToUpper();
                    }

                    // Make item & set displacement for largest slice
                    PieItem segment = myPane.AddPieSlice( (double) item.Count, col, Color.White, 45f, 0, label );
                    if ( item == largestItem )
                    {
                        segment.Displacement = 0.1f;
                    }
                }
                else
                {
                    othersAmount += entryCount;
                }
            }

            // Create "others" entry
            Color colOthers = Color.FromKnownColor( colorUtil.StandardColors[ i ] );
            myPane.AddPieSlice( othersAmount, colOthers, Color.White, 45f, 0, "Others" );

            iPG4_GraphCtrl.AxisChange();
            iPG4_GraphCtrl.Refresh();
        }
        #endregion

        #region Page 5 - Cell Distributions
        private void SetupPage5()
        {
            iPG4_DistributionControl.Statistics = Reconstructor.Statistics;
        }
        #endregion

        #region Page 6 - Relationships
        private void SetupPage6()
        {
            iPG6_RelationshipControl.Cells = iReconstructor.Data;
            iPG6_RelationshipControl.Statistics = iReconstructor.Statistics;
        }

        private void iPG6_RelationshipControl_DoubleClick( object sender, EventArgs e )
        {
            HeapCell selectedCell = iPG6_RelationshipControl.SelectedCell;
            if ( selectedCell != null )
            {
                FocusedCell = selectedCell;
                iTabs.SelectedTab = iPG1_HeapView;
                iPG1_HeapView.Select();
                iPG1_HeapView.Focus();
            }
        }
        #endregion
        
        #endregion

        #region Event handlers

        #region Form loading/closing event handlers
        private void HeapViewerForm_Load(object sender, System.EventArgs e)
		{
			// Set title based upon thread info
            Text += " [ " + iReconstructor.SourceData.ThreadName + " ]";
			//
            SetupPage1();
            SetupPage2();
            SetupPage3();
            SetupPage4();
            SetupPage5();
            SetupPage6();
            //
            CheckSourceForErrors( Reconstructor.SourceData );
		}

		private void HeapViewerForm_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
            iSettings.Save( "HeapViewerCellViewerForm", "iMenuItem_View_Show_RelationshipInspector", iMenuItem_View_Show_RelationshipInspector.Checked );
            iSettings.Save( "HeapViewerCellViewerForm", "iMenuItem_View_Show_CellContentsViewer", iMenuItem_View_Show_CellContentsViewer.Checked );
		}
		#endregion

        #region Menu event handlers
        private void iMenuItem_File_SaveAs_Zip_Click(object sender, System.EventArgs e)
		{
			SaveFileDialog saveFileDialog = new SaveFileDialog();
			saveFileDialog.Filter = "Zip file (*.zip)|*.zip"  ;
			saveFileDialog.RestoreDirectory = true;
			saveFileDialog.Title = "Save as Zip...";
			saveFileDialog.OverwritePrompt = false;
			//
			DialogResult ret = saveFileDialog.ShowDialog();
			//
			if	( ret == DialogResult.OK )
			{
                string tempFileName = string.Empty;
				string destinationFileName = saveFileDialog.FileName;

                FileNamePairCollection sourceFiles = new FileNamePairCollection();

				// Source data file
				FileNamePair logFile = iReconstructor.SourceData.PrepareSourceFileDataForZip();
				sourceFiles.Add( logFile );

#if SAVE_TO_ZIP
				// ROM Files
                tempFileName = Path.GetTempFileName();
                iReconstructor.SymbolManager.ROMEngine.SaveTaggedCollections( tempFileName );
                FileNamePair romSymbolFile = new FileNamePair( tempFileName );
                romSymbolFile.Destination = "/Symbols/" + Path.GetFileName( iReconstructor.SymbolManager.ROMEngine.FileName( 0 ) );
                romSymbolFile.DeleteFile = true;
                sourceFiles.Add( romSymbolFile );

                // ROFS - Symbol files
                int rofsSymbolCount = iReconstructor.SymbolManager.ROFSEngine.SymbolFiles.SymbolFileCount;
                for ( int i = 0; i < rofsSymbolCount; i++ )
                {
                    GenericSymbolEngine rofsSymbolFileEngine = iReconstructor.SymbolManager.ROFSEngine.SymbolFiles.SymbolFileEngineAt( i );
                    FileInfo fileInfo = new FileInfo( iReconstructor.SymbolManager.ROFSEngine.SymbolFiles.SymbolFileNames[ i ] );
                    if ( fileInfo.Exists )
                    {
                        // Save symbols to temp file...
                        tempFileName = Path.GetTempFileName();
                        rofsSymbolFileEngine.SaveTaggedCollections( tempFileName );

                        FileNamePair rofsFile = new FileNamePair( tempFileName );
                        rofsFile.Destination = "/Symbols/" + Path.GetFileName( fileInfo.Name );
                        rofsFile.DeleteFile = true;
                        sourceFiles.Add( rofsFile );
                    }
                }

                // ROFS - Map files
                List<string> mapFiles = iReconstructor.SymbolManager.ROFSEngine.MapFileNames;
                foreach( string mapFileName in mapFiles )
                {
                    FileNamePair mapFile = new FileNamePair( mapFileName );
                    mapFile.SetCustomDestinationPath( "/Symbols/ROFS_Maps/" );
                    sourceFiles.Add( mapFile );
                }
#endif

                // Text representation
                tempFileName = Path.GetTempFileName();
                using ( StreamWriter stream = new StreamWriter( tempFileName, false ) )
				{
					stream.Write( iReconstructor.Data.ToString() );
                    FileNamePair textHeap = new FileNamePair( tempFileName );
                    textHeap.DeleteFile = true;
                    textHeap.Destination = "/Output/Heap_AsText.txt";
					sourceFiles.Add( textHeap  );
				}

				// CSV representation
                tempFileName = Path.GetTempFileName();
                using ( StreamWriter stream = new StreamWriter( tempFileName, false ) )
				{
					stream.Write( iReconstructor.Data.ToString() );
                    FileNamePair csvHeap = new FileNamePair( tempFileName );
                    csvHeap.DeleteFile = true;
                    csvHeap.Destination = "/Output/Heap_AsCSV.csv";
					sourceFiles.Add( csvHeap  );
				}

				ZipCompressionProgressDialog.CompressFiles( sourceFiles, destinationFileName );
			}
		}

		private void iMenuItem_File_SaveAs_Text_Click(object sender, System.EventArgs e)
		{
			SaveFileDialog saveFileDialog = new SaveFileDialog();
			saveFileDialog.Filter = "Text file (*.txt)|*.txt"  ;
			saveFileDialog.RestoreDirectory = true;
			saveFileDialog.Title = "Save as Text...";
			saveFileDialog.OverwritePrompt = false;
			//
			DialogResult ret = saveFileDialog.ShowDialog();
			//
			if	( ret == DialogResult.OK )
			{
				string destinationFileName = saveFileDialog.FileName;

				using( StreamWriter writer = new StreamWriter( destinationFileName, false ) )
				{
					writer.Write( iReconstructor.Data.ToString() );
				}
			}	
		}

		private void iMenuItem_File_SaveAs_CSV_Click(object sender, System.EventArgs e)
		{
			SaveFileDialog saveFileDialog = new SaveFileDialog();
			saveFileDialog.Filter = "CSV file (*.csv)|*.csv"  ;
			saveFileDialog.RestoreDirectory = true;
			saveFileDialog.Title = "Save as CSV...";
			saveFileDialog.OverwritePrompt = false;
			//
			DialogResult ret = saveFileDialog.ShowDialog();
			//
			if	( ret == DialogResult.OK )
			{
				string destinationFileName = saveFileDialog.FileName;

				using( StreamWriter writer = new StreamWriter( destinationFileName, false ) )
				{
					writer.Write( iReconstructor.Data.ToCSV() );
				}
			}	
		}

        private void iMenuItem_File_SaveAs_HTML_Click( object sender, EventArgs e )
        {
            FolderBrowserDialog folderBrowser = new FolderBrowserDialog();
            folderBrowser.Description = "Save HTML to...";
            folderBrowser.ShowNewFolderButton = true;
            //
            DialogResult ret = folderBrowser.ShowDialog();
            //
            if ( ret == DialogResult.OK )
            {
                string path = folderBrowser.SelectedPath;
                HeapExportToHTMLProgressDialog dialog = new HeapExportToHTMLProgressDialog( iReconstructor, path );
                dialog.ShowDialog();
           }
        }

		private void iMenuItem_File_Exit_Click(object sender, System.EventArgs e)
		{
			DialogResult = DialogResult.OK;
			Close();
		}

		private void iMenuItem_View_Show_RelationshipInspector_Click(object sender, System.EventArgs e)
		{
			iMenuItem_View_Show_RelationshipInspector.Checked = !iMenuItem_View_Show_RelationshipInspector.Checked;
			//
            if ( iMenuItem_View_Show_RelationshipInspector.Checked && iForm_RelationshipInspector == null )
			{
                ShowFormCellRelationshipInspector();
			}
			else if ( iMenuItem_View_Show_RelationshipInspector.Checked == false )
			{
				if	( iForm_RelationshipInspector != null )
				{
                    iForm_RelationshipInspector.Close();
				}
                iForm_RelationshipInspector = null;
			}
		}

		private void iMenuItem_View_Show_CellContentsViewer_Click(object sender, System.EventArgs e)
		{
			iMenuItem_View_Show_CellContentsViewer.Checked = !iMenuItem_View_Show_CellContentsViewer.Checked;
			//
			if	( iMenuItem_View_Show_CellContentsViewer.Checked && iForm_CellContents == null )
			{
				ShowFormCellViewer();
			}
			else if ( iMenuItem_View_Show_CellContentsViewer.Checked == false )
			{
				if	( iForm_CellContents != null )
				{
					iForm_CellContents.Close();
				}
				iForm_CellContents = null;
			}
		}

        private void iMenuItem_View_Type_Cell_Click(object sender, System.EventArgs e)
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByCellAge.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCtrlRenderingTypeByCell;
        }

        private void iMenuItem_View_Type_Object_Click(object sender, System.EventArgs e)
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByObjectType.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeBySymbol;
        }

        private void iMenuItem_View_Type_ParentBinary_Click(object sender, System.EventArgs e)
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByParentBinary.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByObject;
        }

        private void iMenuItem_View_Type_Length_Click(object sender, System.EventArgs e)
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByCellLength.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByLength;
        }

        private void iMenuItem_View_Type_EmbeddedReferences_Click( object sender, EventArgs e )
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByEmbeddedReferences.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByEmbeddedReferences;
        }

        private void iMenuItem_View_Type_Isolation_Click( object sender, EventArgs e )
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByIsolation.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByIsolation;
        }

        private void iMenuItem_View_Type_Age_Click(object sender, System.EventArgs e)
        {
            ClearAllMenuViewTypeCheckboxes();
            iPG1_RB_HeapView_ByCellAge.Checked = true;
            iPG1_HeapView_Viewer.Type = HeapCtrlLib.Types.THeapCtrlRenderingType.EHeapCellRenderingTypeByAge;
        }

        private void iMenuItem_View_Zoom_Small_Click( object sender, EventArgs e )
        {
            ClearAllMenuViewZoomCheckboxes();
            iMenuItem_View_Zoom_Small.Checked = true;
            iPG1_HeapView_Viewer.Zoom = HeapCtrlLib.Types.THeapCtrlZoom.EHeapCtrlZoomSmall;
        }

        private void iMenuItem_View_Zoom_Medium_Click( object sender, EventArgs e )
        {
            ClearAllMenuViewZoomCheckboxes();
            iMenuItem_View_Zoom_Medium.Checked = true;
            iPG1_HeapView_Viewer.Zoom = HeapCtrlLib.Types.THeapCtrlZoom.EHeapCtrlZoomMedium;
        }

        private void iMenuItem_View_Zoom_Large_Click( object sender, EventArgs e )
        {
            ClearAllMenuViewZoomCheckboxes();
            iMenuItem_View_Zoom_Large.Checked = true;
            iPG1_HeapView_Viewer.Zoom = HeapCtrlLib.Types.THeapCtrlZoom.EHeapCtrlZoomMaximum;
        }

        private void iMenuItem_Help_About_Click(object sender, System.EventArgs e)
		{
			HeapViewerAboutDialog aboutDialog = new HeapViewerAboutDialog();
			aboutDialog.ShowDialog();
		}
		#endregion

        #region Popup event handlers
        private void iPG1_HeapView_ContextMenu_Opening( object sender, CancelEventArgs e )
        {
            CellRightClickMetaData metaData = (CellRightClickMetaData) iPG1_HeapView_ContextMenu.Tag;
            //
            if ( metaData.Cell.Symbol != null )
            {
                // Remove old XRef entries and ensure we keep the settings
                iPopupMenu_XRef.DropDownItems.Clear();
                iPopupMenu_XRef.DropDownItems.Add( iPopupMenu_XRef_Settings );

                // Add new xref items
                XRefEngine engine = new XRefEngine();
                engine.ParseIdentifiers( metaData.Cell.Symbol.NameWithoutVTablePrefix );

                // Do we have some items to display?
                foreach ( XRefIdentifer identifier in engine.Identifiers )
                {
                    ToolStripMenuItem dynamicXRefItem = new ToolStripMenuItem( identifier.Identifier );
                    dynamicXRefItem.Name = "XREF_DYNAMIC_ITEM_" + identifier.Identifier;
                    dynamicXRefItem.Click += new EventHandler( iPopupMenu_XRef_DynamicItem_Click );
                    dynamicXRefItem.Tag = identifier;
                    iPopupMenu_XRef.DropDownItems.Add( dynamicXRefItem );
                }
            }

            // Remove goto sub-panes - we'll re-add them if they contain content
            iPopupMenu_GoTo.DropDownItems.Clear();

            // Add embedded references goto items.
            iPopupMenu_GoTo_EmbeddedReferences.DropDownItems.Clear();
            int rawItemCount = Math.Min( 50, metaData.Cell.RawItems.Count );
            for( int i=0; i<rawItemCount; i++ )
            {
                RawItem item = metaData.Cell[ i ];
                if ( item.Tag != null && item.Tag is RelationshipInfo )
                {
                    RelationshipInfo relInfo = (RelationshipInfo) item.Tag;
                    //
                    ToolStripMenuItem dynamicItem = new ToolStripMenuItem( relInfo.ToString() );
                    dynamicItem.Name = "GOTO_DYNAMIC_ITEM_" + relInfo.ToCell.Address.ToString("x8");
                    dynamicItem.Click += new EventHandler( iPopupMenu_GoTo_DynamicItem_Click );
                    dynamicItem.Tag = relInfo.ToCell;

                    // Make the item bold, if it corresponds to the right clicked raw item
                    if ( relInfo.FromCellRawItem.Address == metaData.RawItem.Address )
                    {
                        dynamicItem.Font = new Font( dynamicItem.Font, FontStyle.Bold );
                    }

                    // Add it
                    iPopupMenu_GoTo_EmbeddedReferences.DropDownItems.Add( dynamicItem );
                }
            }

            // Add referenced by goto items
            iPopupMenu_GoTo_IncomingReferences.DropDownItems.Clear();
            int refCount = Math.Min( 50, metaData.RelationshipManager.ReferencedBy.Count );
            for( int i=0; i<refCount; i++ )
            {
                HeapCell incomingCellRef = metaData.RelationshipManager.ReferencedBy[ i ];
                //
                ToolStripMenuItem dynamicItem = new ToolStripMenuItem( incomingCellRef.ToStringExtended() );
                dynamicItem.Name = "GOTO_DYNAMIC_ITEM_" + incomingCellRef.Address.ToString( "x8" );
                dynamicItem.Click += new EventHandler( iPopupMenu_GoTo_DynamicItem_Click );
                dynamicItem.Tag = incomingCellRef;
                iPopupMenu_GoTo_IncomingReferences.DropDownItems.Add( dynamicItem );
            }

            // Re-add the embedded/incoming reference items if there is some valid content
            if ( iPopupMenu_GoTo_EmbeddedReferences.DropDownItems.Count > 0 )
            {
                iPopupMenu_GoTo.DropDownItems.Add( iPopupMenu_GoTo_EmbeddedReferences );
            }
            if ( iPopupMenu_GoTo_IncomingReferences.DropDownItems.Count > 0 )
            {
                iPopupMenu_GoTo.DropDownItems.Add( iPopupMenu_GoTo_IncomingReferences );
            }

            // Set the subform item checkbox state
            iPopupMenu_Show_CellContentsViewer.Checked = ( iForm_CellContents != null );
            iPopupMenu_Show_CellRelationshipInspector.Checked = ( iForm_RelationshipInspector != null );

            // Do not show incoming relationship if there isn't one
            iPopupMenu_Relationships_Incoming.DropDownItems.Clear();
            iPopupMenu_Relationships_Incoming_Breadcrumbs.DropDownItems.Clear();
            if ( metaData.RelationshipManager.EmbeddedReferencesTo.Count > 0 )
            {
                iPopupMenu_Relationships_Incoming.DropDownItems.Add( iPopupMenu_Relationships_Incoming_View );
                iPopupMenu_Relationships_Incoming.DropDownItems.Add( iPopupMenu_Relationships_Incoming_Breadcrumbs );
                iPopupMenu_Relationships_Incoming_Breadcrumbs.DropDownItems.Add( iPopupMenu_Relationships_Incoming_Breadcrumbs_Show );
                iPopupMenu_Relationships_Incoming_Breadcrumbs_Show.Checked = iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Contains( metaData.Cell );
            }
            if ( iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Count > 0 )
            {
                if ( !iPopupMenu_Relationships_Incoming.DropDownItems.Contains( iPopupMenu_Relationships_Incoming_Breadcrumbs ) )
                {
                    iPopupMenu_Relationships_Incoming.DropDownItems.Add( iPopupMenu_Relationships_Incoming_Breadcrumbs );
                }
                if ( !iPopupMenu_Relationships_Incoming_Breadcrumbs.DropDownItems.Contains( iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll ) )
                {
                    iPopupMenu_Relationships_Incoming_Breadcrumbs.DropDownItems.Add( iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll );
                }
            }
            if ( iPopupMenu_Relationships_Incoming.DropDownItems.Count == 0 )
            {
                iPopupMenu_Relationships.DropDownItems.Remove( iPopupMenu_Relationships_Incoming );
            }
            else if ( !iPopupMenu_Relationships.DropDownItems.Contains( iPopupMenu_Relationships_Incoming ) )
            {
                iPopupMenu_Relationships.DropDownItems.Insert( 0, iPopupMenu_Relationships_Incoming );
            }

            // Do not show outgoing relationship if there isn't one
            iPopupMenu_Relationships_Outgoing.DropDownItems.Clear();
            iPopupMenu_Relationships_Outgoing_Breadcrumbs.DropDownItems.Clear();
            if ( metaData.RelationshipManager.EmbeddedReferencesTo.Count > 0 )
            {
                iPopupMenu_Relationships_Outgoing.DropDownItems.Add( iPopupMenu_Relationships_Outgoing_View );
                iPopupMenu_Relationships_Outgoing.DropDownItems.Add( iPopupMenu_Relationships_Outgoing_Breadcrumbs );
                iPopupMenu_Relationships_Outgoing_Breadcrumbs.DropDownItems.Add( iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show );
                iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show.Checked = iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Contains( metaData.Cell );
            }
            if ( iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Count > 0 )
            {
                if ( !iPopupMenu_Relationships_Outgoing.DropDownItems.Contains( iPopupMenu_Relationships_Outgoing_Breadcrumbs ) )
                {
                    iPopupMenu_Relationships_Outgoing.DropDownItems.Add( iPopupMenu_Relationships_Outgoing_Breadcrumbs );
                }
                if ( !iPopupMenu_Relationships_Outgoing_Breadcrumbs.DropDownItems.Contains( iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll ) )
                {
                    iPopupMenu_Relationships_Outgoing_Breadcrumbs.DropDownItems.Add( iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll );
                }
            }
            if ( iPopupMenu_Relationships_Outgoing.DropDownItems.Count == 0 )
            {
                iPopupMenu_Relationships.DropDownItems.Remove( iPopupMenu_Relationships_Outgoing );
            }
            else if ( !iPopupMenu_Relationships.DropDownItems.Contains( iPopupMenu_Relationships_Outgoing ) )
            {
                iPopupMenu_Relationships.DropDownItems.Insert( 0, iPopupMenu_Relationships_Outgoing );
            }

            // Add/remove relationship item if not needed
            if ( iPopupMenu_Relationships.DropDownItems.Count == 0 )
            {
                iPG1_HeapView_ContextMenu.Items.Remove( iPopupMenu_Relationships );
            }
            else if ( !iPG1_HeapView_ContextMenu.Items.Contains( iPopupMenu_Relationships ) )
            {
                iPG1_HeapView_ContextMenu.Items.Insert( 0, iPopupMenu_Relationships );
            }
        }

        private void iPopupMenu_Show_CellRelationshipInspector_Click( object sender, EventArgs e )
        {
            iMenuItem_View_Show_RelationshipInspector_Click( sender, e );
        }

        private void iPopupMenu_Show_CellContentsViewer_Click( object sender, EventArgs e )
        {
            iMenuItem_View_Show_CellContentsViewer_Click( sender, e );
        }

        private void iPopupMenu_XRef_Settings_Click( object sender, EventArgs e )
        {
            using ( XRefSettings settings = new XRefSettings() )
            {
                SymbianUtilsUi.XRef.XRefSettingsDialog dialog = new SymbianUtilsUi.XRef.XRefSettingsDialog( settings );
                dialog.ShowDialog();
            }
        }

        private void iPopupMenu_XRef_DynamicItem_Click( object aSender, EventArgs aArgs )
        {
            if ( aSender is ToolStripMenuItem )
            {
                ToolStripMenuItem item = (ToolStripMenuItem) aSender;
                //
                using ( XRefSettings settings = new XRefSettings() )
                {
                    XRefIdentifer identifier = (XRefIdentifer) item.Tag;
                    XRefLauncher launcher = new XRefLauncher();
                    launcher.Launch( identifier, settings );
                }
            }
        }

        private void iPopupMenu_GoTo_DynamicItem_Click( object aSender, EventArgs aArgs )
        {
            if ( aSender is ToolStripMenuItem )
            {
                ToolStripMenuItem item = (ToolStripMenuItem) aSender;
                HeapCell cell = (HeapCell) item.Tag;
                //
                iPG1_HeapView_Viewer.FocusedCell = cell;
            }
        }

        private void iPopupMenu_Relationships_Outgoing_View_Click( object sender, EventArgs e )
        {
            CellRightClickMetaData metaData = (CellRightClickMetaData) iPG1_HeapView_ContextMenu.Tag;

            // First show everything
            iPG6_RelationshipControl.SetFilter( TFilterType.EFilterShowAll );

            // Make sure we're in the right sub-view mode
            iPG6_RelationshipControl.ListMode = HeapCellRelationshipControl.TListMode.EListModeEmbeddedReferences;

            // Then select the right item
            iPG6_RelationshipControl.SelectedCell = metaData.Cell;

            // Show the right tab
            iTabs.SelectedTab = iPG6_Relationships;
            iPG6_RelationshipControl.Focus();
            iPG6_RelationshipControl.Select();
        }

        private void iPopupMenu_Relationships_Outgoing_Breadcrumbs_Show_Click( object sender, EventArgs e )
        {
            CellRightClickMetaData metaData = (CellRightClickMetaData) iPG1_HeapView_ContextMenu.Tag;
            //
            if ( iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Contains( metaData.Cell ) )
            {
                iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Remove( metaData.Cell );
            }
            else
            {
                iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Add( metaData.Cell );
            }
            //
            iPG1_HeapView_Viewer.BreadcrumbsChanged();
        }

        private void iPopupMenu_Relationships_Outgoing_Breadcrumbs_HideAll_Click( object sender, EventArgs e )
        {
            iPG1_HeapView_Viewer.BreadcrumbCellsOutgoing.Clear();
            iPG1_HeapView_Viewer.BreadcrumbsChanged();
        }

        private void iPopupMenu_Relationships_Incoming_View_Click( object sender, EventArgs e )
        {
            CellRightClickMetaData metaData = (CellRightClickMetaData) iPG1_HeapView_ContextMenu.Tag;

            // First show everything
            iPG6_RelationshipControl.SetFilter( TFilterType.EFilterShowAll );

            // Make sure we're in the right sub-view mode
            iPG6_RelationshipControl.ListMode = HeapCellRelationshipControl.TListMode.EListModeReferencedBy;

            // Then select the right item
            iPG6_RelationshipControl.SelectedCell = metaData.Cell;

            // Show the right tab
            iTabs.SelectedTab = iPG6_Relationships;
            iPG6_RelationshipControl.Focus();
            iPG6_RelationshipControl.Select();
        }

        private void iPopupMenu_Relationships_Incoming_Breadcrumbs_Show_Click( object sender, EventArgs e )
        {
            CellRightClickMetaData metaData = (CellRightClickMetaData) iPG1_HeapView_ContextMenu.Tag;
            //
            if ( iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Contains( metaData.Cell ) )
            {
                iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Remove( metaData.Cell );
            }
            else
            {
                iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Add( metaData.Cell );
            }
            //
            iPG1_HeapView_Viewer.BreadcrumbsChanged();
        }

        private void iPopupMenu_Relationships_Incoming_Breadcrumbs_HideAll_Click( object sender, EventArgs e )
        {
            iPG1_HeapView_Viewer.BreadcrumbCellsIncoming.Clear();
            iPG1_HeapView_Viewer.BreadcrumbsChanged();
        }
        #endregion

        #region Toolbox related event handlers
        private void ShowFormCellRelationshipInspector()
		{
            System.Diagnostics.Debug.Assert( iForm_RelationshipInspector == null );
			//
            iForm_RelationshipInspector = new HeapCellRelationshipInspectorForm( this, iSettings );
            iForm_RelationshipInspector.Closing += new CancelEventHandler( iForm_RelationshipInspector_Closing );
            iForm_RelationshipInspector.Show();
		}

		private void ShowFormCellViewer()
		{
            System.Diagnostics.Debug.Assert( iForm_CellContents == null );
			//
            iForm_CellContents = new HeapCellContentsForm( this, iSettings );
            iForm_CellContents.Closing += new CancelEventHandler( iForm_CellContents_Closing );
            iForm_CellContents.Show();
		}

		private void iTimer_CreateToolBoxItems_Tick(object sender, System.EventArgs e)
		{
			iTimer_CreateToolBoxItems.Dispose();
			iTimer_CreateToolBoxItems = null;
			
			// Work out which toolboxes should be visible. Set them to be NOT (setting)
			// so that when the event handlers are called (just below), the visibility of the
			// checkbox is toggled correctly.
            iMenuItem_View_Show_RelationshipInspector.Checked = iSettings.LoadBool( "HeapViewerCellViewerForm", "iMenuItem_View_Show_RelationshipInspector" );
            iMenuItem_View_Show_CellContentsViewer.Checked = iSettings.LoadBool( "HeapViewerCellViewerForm", "iMenuItem_View_Show_CellContentsViewer" );
		}
		#endregion

        #region Keyboard & pointer
        private void HeapViewerForm_KeyDown( object sender, KeyEventArgs e )
        {
            if ( e.Control && e.KeyCode == Keys.G )
            {
                iPG1_TB_Search_ByAddress.Focus();
                iPG1_TB_Search_ByAddress.Select();
                iPG1_TB_Search_ByAddress.SelectAll();
            }
        }

        private void iPG1_HeapView_Viewer_KeyDown( object sender, KeyEventArgs e )
        {
            HeapViewerForm_KeyDown( sender, e );
        }
        #endregion

        #endregion

        #region Sub forms

        #region Navigation form related
        private void iForm_RelationshipInspector_Closing( object sender, CancelEventArgs e )
		{
            iForm_RelationshipInspector = null;
			iMenuItem_View_Show_RelationshipInspector.Checked = false;
		}
		#endregion

		#region Cell viewer form related
        private void iForm_CellContents_Closing( object sender, CancelEventArgs e )
		{
			iForm_CellContents = null;
			iMenuItem_View_Show_CellContentsViewer.Checked = false;
		}
		#endregion
        
        #endregion

        #region Internal methods
        private void CheckSourceForErrors( DataSource aSource )
        {
            string description = string.Empty;
            //
            bool errorsDetected = aSource.ErrorsDetected( out description );
            if ( errorsDetected )
            {
                StringBuilder msg = new StringBuilder( description );
                //
                msg.Append( System.Environment.NewLine );
                msg.Append( "You are recommended to save the heap data to zip and contact" );
                msg.Append( "your support provider." );
                //
                MessageBox.Show( msg.ToString(), "Errors Detected", MessageBoxButtons.OK );
            }
        }
        #endregion

        #region Data members
        private readonly XmlSettings iSettings;
		private readonly HeapReconstructor iReconstructor;
        private HeapCellContentsForm iForm_CellContents;
		private HeapCellRelationshipInspectorForm iForm_RelationshipInspector;
		#endregion
    }
}