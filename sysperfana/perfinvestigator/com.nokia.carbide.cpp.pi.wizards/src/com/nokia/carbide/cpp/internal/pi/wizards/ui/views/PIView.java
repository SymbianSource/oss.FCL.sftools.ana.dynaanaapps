/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description: 
 *
 */
package com.nokia.carbide.cpp.internal.pi.wizards.ui.views;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.nokia.carbide.cpp.internal.pi.button.ui.ImportBupMapWizardDialog;
import com.nokia.carbide.cpp.internal.pi.wizards.model.SessionHandler;
import com.nokia.carbide.cpp.internal.pi.wizards.model.TraceFile;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.actions.ToolbarShortcutAction;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;
import com.nokia.s60tools.ui.S60ToolsTable;
import com.nokia.s60tools.ui.S60ToolsTableColumnData;
import com.nokia.s60tools.ui.S60ToolsTableFactory;


public class PIView extends ViewPart implements IResourceChangeListener  {
	
	public static final String ID = "com.nokia.carbide.cpp.internal.pi.wizards.ui.views.PIView"; //$NON-NLS-1$
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".view";  //$NON-NLS-1$
	private static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".pi_view_context";  //$NON-NLS-1$
	private static final int COLUMN_TRACE_FILE_ID = 0;
	private static final int COLUMN_PROJECT_NAME_ID = 1;
	private static final int COLUMN_SDK_NAME_ID = 2;	
	private static final int COLUMN_IMPORT_TIME_ID = 3;
	private static final int COLUMN_FILE_SIZE_ID = 4;
	private static final int COLUMN_TRACE_TIME_ID = 5;
	
	private TableViewer viewer;
	private Action actionOpenWizard;
	private Action actionOpenKeyPressWizard;
	private Action actionDelete;
	private IWorkspace workspace;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view.
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return SessionHandler.getInstance().loadTraceFile();
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			final TraceFile traceFile = (TraceFile)obj;
			SimpleDateFormat formatter = null;
			switch (index) {
			case COLUMN_TRACE_FILE_ID:
				return traceFile.getTraceFilePath().lastSegment();
			case COLUMN_PROJECT_NAME_ID:
				return traceFile.getProjectName();
			case COLUMN_SDK_NAME_ID:
				if(traceFile.getSdkName().length() <= 0){
					return "-"; //$NON-NLS-1$
				}
				return traceFile.getSdkName();
			case COLUMN_IMPORT_TIME_ID:
				formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss"); //$NON-NLS-1$
				return formatter.format(new Date(traceFile.getImportTime()));
			case COLUMN_FILE_SIZE_ID:
				IProject project = (IProject) workspace.getRoot().findMember(traceFile.getProjectName());
				if(project == null){
					return "-"; //$NON-NLS-1$
				}
				
				IResource resource = project.findMember(traceFile.getTraceFilePath().lastSegment());
				if (resource != null) {					
					SessionHandler.getInstance().removeTraceFile(traceFile);
					traceFile.setTraceFileSize(resource.getLocation().toFile().length());
					SessionHandler.getInstance().addTraceFile(traceFile);
				}
				return String.valueOf(traceFile.getTraceFileSize() / 1024 );
			case COLUMN_TRACE_TIME_ID:
				if(traceFile.getTraceLengthInTime() == -1){
					return "-"; //$NON-NLS-1$
				}
				formatter = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
				return formatter.format(new Date(traceFile.getTraceLengthInTime()));
			default:
				break;
			}
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return null;
		}
	}
	
	class TableSorter extends ViewerSorter {
		private Table table;
		private int column = 0;
		private boolean sortAscending;
		public TableSorter(Table table, int column){
			this.table = table;		
			doSort(column);
		}
		
		public void doSort(int column) {
			sortAscending = !sortAscending;

			// find the TableColumn corresponding to column, and give it a
			// column direction
			TableColumn sortByColumn = table.getColumn(column);
			if (sortByColumn != null) {
				table.setSortColumn(sortByColumn);
				table.setSortDirection(sortAscending ? SWT.UP : SWT.DOWN);
			}
			this.column = column;
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			TraceFile tf1 = (TraceFile)e1;
			TraceFile tf2 = (TraceFile)e2;
			int returnCode = 0;
			switch (column) {
			case COLUMN_TRACE_FILE_ID:
				returnCode = tf1.getTraceFilePath().lastSegment().compareTo(
						tf2.getTraceFilePath().lastSegment());
				break;
			case COLUMN_PROJECT_NAME_ID:
				returnCode =  tf1.getProjectName().compareTo(tf2.getProjectName());
				break;
			case COLUMN_SDK_NAME_ID:
				returnCode =  tf1.getSdkName().compareTo(tf2.getSdkName());
				break;
			case COLUMN_IMPORT_TIME_ID:
				returnCode =  numericSort(tf1.getImportTime(), tf2.getImportTime());
				break;
			case COLUMN_FILE_SIZE_ID:
				returnCode =  numericSort(tf1.getTraceFileSize(), tf2
						.getTraceFileSize());
				break;
			case COLUMN_TRACE_TIME_ID:
				returnCode =  numericSort(tf1.getTraceLengthInTime(), tf2
						.getTraceLengthInTime());
				break;
			default:
				break;
			}
			
			if (!sortAscending)
				returnCode = -returnCode;
			return returnCode;
			
		}
		
		/**
		 * Numeric sort.
		 * @param num1 1st number to compare.
		 * @param num2 2nd  number to compare.
		 * @return Returns a negative integer, zero, 
		 * 	       or a positive integer as the first argument 
		 * 	       is less than, equal to, or greater 
		 *	       than the second. 
		 */
		private int numericSort(long num1, long num2){
			long result = (num1 - num2);
			if(result > 0){
				return 1;
			}
			else if(result < 0){
				return -1;
			}
			else{
				return 0;
			}
		}
	}
	

	/**
	 * Constructor
	 */
	public PIView(){
		workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		workspace.removeResourceChangeListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = createListViewTableViewer(parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());		
		viewer.setInput(getViewSite());

	
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HELP_CONTEXT_ID_MAIN_PAGE);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}
	
	/**
	 * Creates table viewer for import functions tab item. 
	 * @return New <code>TableViewer</code> object instance.
	 */
	private TableViewer createListViewTableViewer(Composite parent) {
		
		List<S60ToolsTableColumnData> columnDataArr = new ArrayList<S60ToolsTableColumnData>();
				
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnTraceFileName"), 200, COLUMN_TRACE_FILE_ID)); //$NON-NLS-1$
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnProjectName"), 200, COLUMN_PROJECT_NAME_ID)); //$NON-NLS-1$
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnSDKName"), 150, COLUMN_SDK_NAME_ID)); //$NON-NLS-1$
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnImportTime"), 150, COLUMN_IMPORT_TIME_ID)); //$NON-NLS-1$
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnFileSize"), 140, COLUMN_FILE_SIZE_ID)); //$NON-NLS-1$
		columnDataArr.add(new S60ToolsTableColumnData(Messages.getString("PIView.columnTime"), 140, COLUMN_TRACE_TIME_ID)); //$NON-NLS-1$
		
		S60ToolsTableColumnData[] arr 
				= (S60ToolsTableColumnData[]) columnDataArr.toArray(
											   new S60ToolsTableColumnData[0]);
		
		S60ToolsTable tbl = S60ToolsTableFactory.create(parent, arr);
		
		TableViewer tblViewer = new TableViewer(tbl.getTableInstance());
		final TableSorter sorter = new TableSorter(tblViewer.getTable(), COLUMN_IMPORT_TIME_ID);
		tblViewer.setSorter(sorter);
		for(int i=0; i < tbl.getColumnCount();i++){
			final int column = i;
			tbl.getColumn(i).addSelectionListener(new SelectionAdapter() {

				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					sorter.doSort(column);
					viewer.refresh();
				}
				
			});
		}

		tbl.setHostingViewer(tblViewer);
		
		return tblViewer;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PIView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionDelete);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionOpenWizard);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(actionOpenKeyPressWizard);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(actionDelete);
	}

	private void makeActions() {
		// Open Performance Investigator wizard button
		actionOpenWizard = new Action() {
			public void run() {
				ToolbarShortcutAction.openWizard(getSite().getShell());
			}
		};

		actionOpenWizard.setText(Messages.getString("PIView.openPIButton")); //$NON-NLS-1$
		actionOpenWizard.setToolTipText(Messages.getString("PIView.openPIButtonDescription")); //$NON-NLS-1$
		actionOpenWizard.setImageDescriptor(WizardsPlugin.getImageDescriptor("icons/open_pi_wizard.png")); //$NON-NLS-1$

	
		// Open Performance Investigator Key Press Profile button		
		actionOpenKeyPressWizard = new Action() {
			public void run() {	
				ImportBupMapWizardDialog wizDialog = new ImportBupMapWizardDialog(getSite().getShell());	
				wizDialog.create();		
				wizDialog.open();
			}
		};
		
		actionOpenKeyPressWizard.setText(Messages.getString("PIView.openKeyPressProfilerButton")); //$NON-NLS-1$
		actionOpenKeyPressWizard.setToolTipText(Messages.getString("PIView.openKeyPressProfilerButtonDescription")); //$NON-NLS-1$
		actionOpenKeyPressWizard.setImageDescriptor(WizardsPlugin.getImageDescriptor("icons/open_key_press_profile_wizard.png")); //$NON-NLS-1$

		// Delete selected item button
		actionDelete = new Action() {
			public void run() {
				
				ISelection selection = viewer.getSelection();
				if (selection == null || selection.isEmpty())
					return;
				
				@SuppressWarnings("unchecked")
				Iterator i = ((IStructuredSelection)selection).iterator();
				while (i.hasNext()) {		
					SessionHandler.getInstance().removeTraceFile((TraceFile)i.next());
				}
				viewer.refresh();
			}
		};
		actionDelete.setText(Messages.getString("PIView.deleteButton")); //$NON-NLS-1$
		actionDelete.setToolTipText(Messages.getString("PIView.deleteButtonDescription")); //$NON-NLS-1$
		actionDelete.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));		
		
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if(selection instanceof StructuredSelection){
					StructuredSelection structuredSelection = (StructuredSelection)selection;
					openProfilerDataFile(((TraceFile)structuredSelection.getFirstElement()));
				}
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
		
	}
	
	/**
	 * Reloads Trace files to table
	 *
	 */
	public void updateView() {
		try {
			getViewSite().getPage().showView(PIView.ID);
			viewer.refresh();
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}
	
	/**
	 * Open given profiler data file with an editor 
	 * 
	 * @param traceFile to be opened
	 */
	private void openProfilerDataFile(final TraceFile traceFile ) {	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(traceFile.getProjectName());
		
		IProject project = null;
		if (resource instanceof IProject) {
			project = (IProject) resource;
		} else {
			return;
		}

		final IFile analysisFile = project.getFile(traceFile.getTraceFilePath().lastSegment());
		final File analysisFilePath = analysisFile.getLocation().toFile();
		
		// open the saved file
		if (analysisFilePath != null && analysisFilePath.exists() ) {
				// open the file itself		
			// need to open in UI context
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {	
					try {
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() , analysisFile, true);
					} catch (PartInitException e) {
						e.printStackTrace();
					}											
				}
			});
		}else{
			MessageDialog.openError(getSite().getShell(), Messages
					.getString("PIView.errorFileIsRemovedDialogTitle"), MessageFormat.format(Messages //$NON-NLS-1$
							.getString("PIView.errorFileIsRemovedDialogMessage"), //$NON-NLS-1$
							traceFile.getTraceFilePath().lastSegment()));
			SessionHandler.getInstance().removeTraceFile(traceFile);
			viewer.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if(event.getDelta() instanceof IResourceDelta){
			IResourceDelta rootDelta = (IResourceDelta) event.getDelta();		
			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {		
					IResource resource = delta.getResource();
					if (resource.getType() == IResource.FILE
							&& "npi".equalsIgnoreCase(resource.getFileExtension())) { //$NON-NLS-1$
						if (delta.getKind() == IResourceDelta.REMOVED) {								
							final TraceFile traceFile = SessionHandler.getInstance().getTraceFile(resource.getFullPath());
							if(traceFile != null){
								IPath movedTo = delta.getMovedToPath();
								if(movedTo != null){
									TraceFile movedToFile = SessionHandler.getInstance().getTraceFile(movedTo);
									if(movedToFile != null){
										SessionHandler.getInstance().removeTraceFile(movedToFile);
									}
									SessionHandler.getInstance().removeTraceFile(traceFile);									 
									traceFile.setTraceFilePath(movedTo);
									String projectName = movedTo.segment(0);
									traceFile.setProjectName(projectName);
									SessionHandler.getInstance().addTraceFile(traceFile);
									Display.getDefault().asyncExec(new Runnable() {										
										public void run() {
											viewer.refresh();											
										}
									});
								}else{
									Display.getDefault().asyncExec(new Runnable() {									
										public void run() {
											SessionHandler.getInstance().removeTraceFile(traceFile);
											viewer.refresh();											
										}
									});	
								}														
							}
						} 
					}
					return true;
				}

			};
			try {
				rootDelta.accept(visitor);
			} catch (CoreException e) {	
				e.printStackTrace();
			}
		}
	
	}
	
}