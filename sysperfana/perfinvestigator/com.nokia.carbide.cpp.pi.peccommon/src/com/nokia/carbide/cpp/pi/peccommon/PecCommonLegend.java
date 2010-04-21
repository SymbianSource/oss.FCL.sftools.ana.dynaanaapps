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

package com.nokia.carbide.cpp.pi.peccommon;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveTable;
import com.nokia.carbide.cpp.internal.pi.save.SaveTableWizard;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * Manages the legend for Performance Counter traces. 
 */
public class PecCommonLegend extends Composite{
	private static final char QUOTE = '"';
	private static final String PI_SAVE_TABLE = "PISaveTable";//$NON-NLS-1$
	private static final String PI_COPY_TABLE = "PICopyTable";//$NON-NLS-1$
	private static final String SAVE_ALL = "saveAll";//$NON-NLS-1$
	
	private final static String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String COMMA = ",";//$NON-NLS-1$
	
	/** short description column, such as 'A' */
	public static final int COLUMN_SHORT_TITLE = 0;
	/** performance counter name column */
	public static final int COLUMN_NAME = 1;
	/** average value column */
	public static final int COLUMN_AVERAGE = 2;
	/** sum of values column */
	public static final int COLUMN_SUM = 3;
	/** minimum of values column */
	public static final int COLUMN_MIN = 4;
	/** maximum of values column */
	public static final int COLUMN_MAX = 5;
	
	protected CheckboxTableViewer viewer;
	protected PecCommonTraceGraph graph;
	protected PecCommonTrace trace;
	
    // menu items
	protected Action copyTableAction;
	protected Action copyAction;
	protected Action saveTableAction;
	private IAction checkAllAction;
	private IAction uncheckAllAction;

	
	/**
	 * Constructor
	 * @param graph the graph that this legend belongs to 
	 * @param parent the parent composite
	 * @param title Title for the legend
	 * @param trace The model containing the samples
	 */
	public PecCommonLegend(final PecCommonTraceGraph graph, Composite parent, String title, PecCommonTrace trace){
		super(parent, SWT.NONE);
		this.graph = graph;
		this.trace = trace;
		
		setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).create());

		Label label = new Label(this, SWT.CENTER);
		label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		//label.setFont(PIPageEditor.helvetica_8);
		label.setText(title);
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).create());
		
		//create table viewer
		final Table table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.viewer = new CheckboxTableViewer(table);

		PecCommonLegendLabelProvider labelProvider = createLabelProvider();
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new PecCommonLegendContentProvider(trace));

        PecCommonLegendViewerSorter columnSorter = createLegendSorter();
        createColumns(this.viewer, columnSorter);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
        columnSorter.setSortColumn(COLUMN_SHORT_TITLE);
        viewer.setComparator(columnSorter);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				if (copyAction == null)
					return;

				// when selection changes, the ability to copy may change
				copyAction.setEnabled(table.getSelectionCount() > 0);
				PIPageEditor.getActionBars().updateActionBars();
			}
		});
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				PecCommonLegendElement el = (PecCommonLegendElement)event.getElement();
				graph.addOrRemoveSeries(el.getId(), event.getChecked());
			}
		});
		
        viewer.setInput(trace);
        viewer.getTable().setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
        addActions(viewer);
        viewer.getTable().addFocusListener(new PecCommonLegendFocusListener(viewer));
        addContextMenu(viewer);
        viewer.setAllChecked(true); //default

        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, graph.getContextHelpId());
	}
	


	/**
	 * Creates the LabelProvider for the legend table
	 * @return
	 */
	protected PecCommonLegendLabelProvider createLabelProvider() {
		return new PecCommonLegendLabelProvider();
	}



	/**
	 * Refreshes content of the legend
	 */
	public void refreshLegend(){
		viewer.refresh();
	}
	
	protected void createColumns(final TableViewer aViewer, final PecCommonLegendViewerSorter columnSorter)
 {
		final Table table = aViewer.getTable();

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.PecCommonLegend_0);
		column.setWidth(50);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_SHORT_TITLE);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.PecCommonLegend_1);
		column.setWidth(150);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_NAME);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(String.format(Messages.PecCommonLegend_2, trace.getSamplingInterval()));
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_AVERAGE);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(Messages.PecCommonLegend_3);
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_SUM);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(String.format(Messages.PecCommonLegend_4, trace.getSamplingInterval()));
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_MIN);
				aViewer.refresh();
			}
		});

		column = new TableColumn(table, SWT.RIGHT);
		column.setText(String.format(Messages.PecCommonLegend_5, trace.getSamplingInterval()));
		column.setWidth(100);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnSorter.setSortColumn(COLUMN_MAX);
				aViewer.refresh();
			}
		});
	}

	/**
	 * Maximises or restores the legend table viewer
	 * @param maximise true for maximise, false for restore
	 */
	public void setLegendMaximised(boolean maximise) {
		((SashForm)this.getParent()).setMaximizedControl(maximise ? this : null);		
	}

	/**
	 * Minimises or restores legend view
	 * @param visible true for setting visible, false for hiding
	 */
	public void setLegendVisible(boolean visible) {
		this.setVisible(visible);
		
	}
	
	private void addActions(final TableViewer legendViewer) {	
		
		checkAllAction = new Action(Messages.PecCommonLegend_6){
			@Override
			public void run() {
				viewer.setAllChecked(true);
				graph.showAllSeries();
			}			
		};
		checkAllAction.setEnabled(true);
		checkAllAction.setText(Messages.PecCommonLegend_7);
		
		uncheckAllAction = new Action(Messages.PecCommonLegend_8){
			@Override
			public void run() {
				viewer.setAllChecked(false);
				graph.removeAllSeries();
			}			
		};
		uncheckAllAction.setEnabled(true);
		uncheckAllAction.setText(Messages.PecCommonLegend_9);
		
		copyAction = new Action(Messages.PecCommonLegend_10) {
			@Override
			public void run() {
				copyToClipboard(legendContentToString(((IStructuredSelection)legendViewer.getSelection()).toArray(), false, TAB));
			}
		};
		copyAction.setEnabled(false);
		copyAction.setText(Messages.PecCommonLegend_11);

		copyTableAction = new Action(Messages.PecCommonLegend_12) {
			@Override
			public void run() {
				Object[] elements = ((IStructuredContentProvider)legendViewer.getContentProvider()).getElements(null); 
				legendViewer.getComparator().sort(legendViewer, elements);
				copyToClipboard(legendContentToString(elements, true, TAB));
			}
		};
		copyTableAction.setEnabled(true);
		copyTableAction.setId(PI_COPY_TABLE); 
		copyTableAction.setText(Messages.PecCommonLegend_13);

		saveTableAction = new Action(Messages.PecCommonLegend_14) { 
			@Override
			public void run() {
				
				WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), new SaveTableWizard(new ISaveTable(){
					public String getData() {
						Object[] elements = ((PecCommonLegendContentProvider)legendViewer.getContentProvider()).getElements(null); 
						legendViewer.getComparator().sort(legendViewer, elements);
						return legendContentToString(elements, true, COMMA).toString();
					}
				}));
		    	dialog.open();
			}
		};
		saveTableAction.setEnabled(true);
		saveTableAction.setId(PI_SAVE_TABLE);
		saveTableAction.setText(Messages.PecCommonLegend_15);
		
}
	
	/**
	 * Copies the content of the given StringBuilder to the clipboard. 
	 */
	protected void copyToClipboard(StringBuilder sb) {
		
		if (sb.length() > 0) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			Clipboard cb = new Clipboard(this.getDisplay());
			try {
				cb.setContents(new Object[] { sb.toString() },
						new Transfer[] { textTransfer });
			} finally {
				cb.dispose();
			}
		}
		
	}
	
	/**
	 * Returns the content of the given objects in human-readable format.
	 * Optionally includes column headers. Columns are separated by tabs. Elements are separated
	 * by newlines. 
	 * @param objects Array of objects. Elements of the array are assumed to be of type PecCommonLegendElement
	 * @param includeHeader true if column headers are to be included	 
	 * @return 
	 */
	protected StringBuilder legendContentToString(Object[] objects, boolean includeHeader, String separator) {
		StringBuilder sb = new StringBuilder();
		
		if (includeHeader){
			for (int i = 0; i < viewer.getTable().getColumnCount(); i++) {
				if (i != 0){
					sb.append(separator);
				}
				sb.append(viewer.getTable().getColumn(i).getText());
			}
			sb.append(NEWLINE);
		}
		
		PecCommonLegendLabelProvider labelProvider = (PecCommonLegendLabelProvider)viewer.getLabelProvider();
		for (Object o : objects) {
			PecCommonLegendElement pecLegendElement = (PecCommonLegendElement) o;
			for (int i = 0; i < viewer.getTable().getColumnCount(); i++) {
				if (i != 0){
					sb.append(separator);
				}
				String s = labelProvider.getColumnText(pecLegendElement, i);
				if (s.indexOf(separator) >= 0){
					sb.append(QUOTE).append(s).append(QUOTE);	//quote if the separator occurs in the value string				
				} else {
					sb.append(s);
				}
			}
			sb.append(NEWLINE);
		}
		return sb;
	}


	/**
	 * Updates enabled-state of actions
	 * @param table the table that the actions apply to
	 */
	protected void updateActionStatus(Table table) {
		copyAction.setEnabled(table.getSelectionCount() > 0);
		copyTableAction.setEnabled(table.getItemCount() > 0);
		saveTableAction.setEnabled(table.getItemCount() > 0);		
	}


	private void addContextMenu(final TableViewer aViewer) {
		final MenuManager mgr = new MenuManager();
		mgr.add(new Separator());
		mgr.add(checkAllAction);
		mgr.add(uncheckAllAction);
		mgr.add(new Separator());
		mgr.add(copyAction);
		mgr.add(copyTableAction);
		mgr.add(new Separator());
		mgr.add(saveTableAction);
		aViewer.getControl().setMenu(mgr.createContextMenu(aViewer.getControl()));		
	}
	

	/**
	 * On gaining focus, we add copy / save actions to the top file and edit menu, 
	 * on losing it, we remove those actions again.   
	 *
	 */
	private class PecCommonLegendFocusListener implements FocusListener {
		private TableViewer legendViewer;
		private IAction oldCopyAction;

		public PecCommonLegendFocusListener(TableViewer legendViewer) {
			this.legendViewer = legendViewer;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.
		 * swt.events.FocusEvent)
		 */
		public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
			IActionBars bars = PIPageEditor.getActionBars();

			oldCopyAction = PIPageEditor.getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId());
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(),	copyAction);
			bars.updateActionBars();

			// add copyTableAction to the Edit menu
			IMenuManager editMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_EDIT);

			if (editMenuManager instanceof SubMenuManager) {
				IContributionManager editManager = ((SubMenuManager) editMenuManager).getParent();
				editMenuManager.remove(PI_COPY_TABLE);
				ActionContributionItem item = new ActionContributionItem(copyTableAction);
				item.setVisible(true);
				editManager.prependToGroup(IIDEActionConstants.CUT_EXT, item);
			}

			// add saveTableAction to the File menu
			IMenuManager fileMenuManager = bars.getMenuManager().findMenuUsingPath(IIDEActionConstants.M_FILE);

			if (fileMenuManager instanceof SubMenuManager) {
				IContributionManager fileManager = ((SubMenuManager) fileMenuManager).getParent();

				fileMenuManager.remove(PI_SAVE_TABLE);
				ActionContributionItem item = new ActionContributionItem(saveTableAction);
				item.setVisible(true);
				fileManager.insertAfter(SAVE_ALL, item);
			}
			
			updateActionStatus(legendViewer.getTable());
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt
		 * .events.FocusEvent)
		 */
		public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
			IActionBars bars = PIPageEditor.getActionBars();
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					oldCopyAction);
			bars.updateActionBars();

			SubMenuManager editMenuManager = (SubMenuManager) PIPageEditor
					.getMenuManager().find(IIDEActionConstants.M_EDIT);
			editMenuManager.remove(PI_COPY_TABLE);
			editMenuManager.update();

			SubMenuManager fileMenuManager = (SubMenuManager) PIPageEditor
					.getMenuManager().find(IIDEActionConstants.M_FILE);
			fileMenuManager.remove(PI_SAVE_TABLE);
			fileMenuManager.update();
		}
	}		

	/**
	 * Creates the sorter used for the legend view. Sub classes may override this
	 * @return PecCommonLegendViewerSorter
	 */
	protected PecCommonLegendViewerSorter createLegendSorter(){
		return new PecCommonLegendViewerSorter();
	}
}
