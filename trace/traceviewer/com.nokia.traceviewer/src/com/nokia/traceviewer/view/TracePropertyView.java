/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of "Eclipse Public License v1.0"
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
 * Trace Property view
 *
 */
package com.nokia.traceviewer.view;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.AddLineCountRuleAction;
import com.nokia.traceviewer.action.AddVariableTracingRuleAction;
import com.nokia.traceviewer.action.DeactivateLineCountRuleAction;
import com.nokia.traceviewer.action.DeactivateVariableTracingRuleAction;
import com.nokia.traceviewer.action.EditLineCountRuleAction;
import com.nokia.traceviewer.action.EditTraceCommentAction;
import com.nokia.traceviewer.action.EditVariableTracingRuleAction;
import com.nokia.traceviewer.action.JumpToTraceAction;
import com.nokia.traceviewer.action.RemoveTraceCommentAction;
import com.nokia.traceviewer.api.TraceViewerAPI;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerPropertyViewInterface;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.engine.dataprocessor.LineCountItem;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingItem;

/**
 * Trace Property view
 */
public class TracePropertyView extends ViewPart implements
		TraceViewerPropertyViewInterface, DataProcessor, Runnable {

	/**
	 * Line count rule name width configuration entry
	 */
	private static final String LINECOUNTNAME_WIDTH_CONFIGURATION = "TracePropertyView.LineCountNameWidth"; //$NON-NLS-1$

	/**
	 * Line count occurrences width configuration entry
	 */
	private static final String LINECOUNTOCCURRENCES_WIDTH_CONFIGURATION = "TracePropertyView.OccurrencesWidth"; //$NON-NLS-1$

	/**
	 * Variable tracing name width configuration entry
	 */
	private static final String VARIABLENAME_WIDTH_CONFIGURATION = "TracePropertyView.VariableNameWidth"; //$NON-NLS-1$

	/**
	 * Variable tracing value width configuration entry
	 */
	private static final String VARIABLEVALUE_WIDTH_CONFIGURATION = "TracePropertyView.VariableValueWidth"; //$NON-NLS-1$

	/**
	 * Name of the right column in variable tracing table
	 */
	private static final String VARIABLETABLE_RIGHT_COLUMN_NAME = Messages
			.getString("TracePropertyView.ValueColumnName"); //$NON-NLS-1$

	/**
	 * Name of the left column in variable tracing table
	 */
	private static final String VARIABLETABLE_LEFT_COLUMN_NAME = Messages
			.getString("TracePropertyView.VariableTracingColumnName"); //$NON-NLS-1$

	/**
	 * Name of the right column in line count table
	 */
	private static final String LINECOUNTTABLE_OCCURRENCES_COLUMN_NAME = Messages
			.getString("TracePropertyView.OccurrencesColumnName"); //$NON-NLS-1$

	/**
	 * Name of the left column in line count table
	 */
	private static final String LINECOUNTTABLE_RULE_COLUMN_NAME = Messages
			.getString("TracePropertyView.LineCountRuleColumnName"); //$NON-NLS-1$

	/**
	 * Name of the trace number column in the trace comment table
	 */
	private static final String TRACECOMMENTTABLE_NUMBER_COLUMN_NAME = Messages
			.getString("TracePropertyView.TraceNumberColumnName"); //$NON-NLS-1$

	/**
	 * Name of the trace comment column in the trace comment table
	 */
	private static final String TRACECOMMENTTABLE_COMMENT_COLUMN_NAME = Messages
			.getString("TracePropertyView.TraceCommentColumnName"); //$NON-NLS-1$

	/**
	 * Width of the left column of line count table
	 */
	private static final int LINECOUNTTABLE_RULE_COLUMN_SIZE = 300;

	/**
	 * Width of the right column of line count table
	 */
	private static final int LINECOUNTTABLE_OCCURRENCES_COLUMN_SIZE = 150;

	/**
	 * Width of the left column of variable tracing table
	 */
	private static final int VARIABLETABLE_RULE_COLUMN_SIZE = 150;

	/**
	 * Width of the right column of variable tracing table
	 */
	private static final int VARIABLETABLE_VALUE_COLUMN_SIZE = 300;

	/**
	 * Update time interval in milliseconds
	 */
	private static final long UPDATE_INTERVAL = 100;

	/**
	 * Timestamp of next update
	 */
	private long updateNextTime;

	/**
	 * Sash that divides the tables horizontally
	 */
	private SashForm horizontalSash;

	/**
	 * Sash that divides the tables vertically
	 */
	private SashForm verticalSash;

	/**
	 * Line counting items table
	 */
	private Table lineCountingTable;

	/**
	 * Variable tracing items table
	 */
	private Table variableTracingTable;

	/**
	 * Trace comment table
	 */
	private Table traceCommentTable;

	/**
	 * Line counting table rule name column
	 */
	private TableColumn lineCountRuleNameColumn;

	/**
	 * Line counting table occurrences column
	 */
	private TableColumn lineCountOccurrencesColumn;

	/**
	 * Variable tracing table value column
	 */
	private TableColumn variableTracingValueColumn;

	/**
	 * Variable tracing table rule name column
	 */
	private TableColumn variableTracingRuleNameColumn;

	/**
	 * Indicates if lineCountingTable should be updated
	 */
	private boolean changeToLineCountingTable;

	/**
	 * Indicates if variableTracingTable should be updated
	 */
	private boolean changeToVariableTracingTable;

	/**
	 * Indicates if traceCommentTable should be updated
	 */
	private boolean changeToTraceCommentTable;

	/**
	 * Indicates that all data hasn't been updated
	 */
	private boolean hasUnshownData;

	/**
	 * Parent composite
	 */
	private Composite parent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		verticalSash = new SashForm(parent, SWT.VERTICAL);
		horizontalSash = new SashForm(verticalSash, SWT.HORIZONTAL);

		// Create tables and columns
		createTables();

		// Create context menus to all tables
		createContextMenus();

		// Create listeners for the tables
		createListeners();

		horizontalSash.setWeights(new int[] { 50, 50 });
		verticalSash.setWeights(new int[] { 100, 0 });

		// Get items to tables if they exist
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess() != null
				&& TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor() != null
				&& TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getLineCountProcessor().getLineCountItems() != null) {
			createNewPropertyTableItems();
		}

		// Set this view to engine
		TraceViewerGlobals.getTraceViewer().setPropertyView(this);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.PROPERTY_VIEW);
	}

	/**
	 * Creates tables and columns
	 */
	private void createTables() {
		createLineCountTable();
		createVariableTracingTable();
		createTraceCommentTable();
		configureWidgets();
		createResizeListeners();
	}

	/**
	 * Creates line count table
	 */
	private void createLineCountTable() {
		// Line counting table
		lineCountingTable = new Table(horizontalSash, SWT.BORDER
				| SWT.FULL_SELECTION);
		lineCountingTable.setHeaderVisible(true);

		// Create linecount rule name column
		lineCountRuleNameColumn = new TableColumn(lineCountingTable, SWT.NONE);
		lineCountRuleNameColumn.setText(LINECOUNTTABLE_RULE_COLUMN_NAME);

		// Create linecount occurrences column
		lineCountOccurrencesColumn = new TableColumn(lineCountingTable,
				SWT.NONE);
		lineCountOccurrencesColumn
				.setText(LINECOUNTTABLE_OCCURRENCES_COLUMN_NAME);
	}

	/**
	 * Creates variable tracing table
	 */
	private void createVariableTracingTable() {
		// Variable tracing table
		variableTracingTable = new Table(horizontalSash, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI);
		variableTracingTable.setHeaderVisible(true);

		// Create variabletracing rule name column
		variableTracingRuleNameColumn = new TableColumn(variableTracingTable,
				SWT.NONE);
		variableTracingRuleNameColumn.setText(VARIABLETABLE_LEFT_COLUMN_NAME);

		// Create variable tracing value column
		variableTracingValueColumn = new TableColumn(variableTracingTable,
				SWT.NONE);
		variableTracingValueColumn.setText(VARIABLETABLE_RIGHT_COLUMN_NAME);
	}

	/**
	 * Creates trace comment table
	 */
	private void createTraceCommentTable() {
		// Trace comment table
		traceCommentTable = new Table(verticalSash, SWT.BORDER
				| SWT.FULL_SELECTION);
		traceCommentTable.setLayout(new GridLayout());
		traceCommentTable.setHeaderVisible(true);
		traceCommentTable.setVisible(false);

		// Create trace number column
		TableColumn traceNumberColumn = new TableColumn(traceCommentTable,
				SWT.NONE);
		traceNumberColumn.setText(TRACECOMMENTTABLE_NUMBER_COLUMN_NAME);
		traceNumberColumn.setWidth(60);

		// Create trace comment column
		TableColumn traceCommentColumn = new TableColumn(traceCommentTable,
				SWT.NONE);
		traceCommentColumn.setText(TRACECOMMENTTABLE_COMMENT_COLUMN_NAME);
		traceCommentColumn.setWidth(400);
	}

	/**
	 * Creates resize listeners to all columns
	 */
	private void createResizeListeners() {
		// Add resize listener to linecount rule name column
		lineCountRuleNameColumn.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			// When column is resized, save new width to preferencestore
			public void controlResized(ControlEvent e) {
				TraceViewerPlugin.getDefault().getPreferenceStore().setValue(
						LINECOUNTNAME_WIDTH_CONFIGURATION,
						lineCountRuleNameColumn.getWidth());
			}

		});

		// Add resize listener to linecount occurrences column
		lineCountOccurrencesColumn.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			// When column is resized, save new width to preferencestore
			public void controlResized(ControlEvent e) {
				TraceViewerPlugin.getDefault().getPreferenceStore().setValue(
						LINECOUNTOCCURRENCES_WIDTH_CONFIGURATION,
						lineCountOccurrencesColumn.getWidth());
			}

		});

		// Add resize listener to variabletracing rule name column
		variableTracingRuleNameColumn.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			// When column is resized, save new width to preferencestore
			public void controlResized(ControlEvent e) {
				TraceViewerPlugin.getDefault().getPreferenceStore().setValue(
						VARIABLENAME_WIDTH_CONFIGURATION,
						variableTracingRuleNameColumn.getWidth());
			}

		});

		// Add resize listener to variabletracing occurrences column
		variableTracingValueColumn.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			// When column is resized, save new width to preferencestore
			public void controlResized(ControlEvent e) {
				TraceViewerPlugin.getDefault().getPreferenceStore().setValue(
						VARIABLEVALUE_WIDTH_CONFIGURATION,
						variableTracingValueColumn.getWidth());
			}

		});
	}

	/**
	 * Configures the table column sizes
	 */
	private void configureWidgets() {

		// Get preference store from the plugin
		IPreferenceStore preferences = TraceViewerPlugin.getDefault()
				.getPreferenceStore();

		// Set line count rule name column width
		int value = preferences.getInt(LINECOUNTNAME_WIDTH_CONFIGURATION);
		if (value > 0) {
			lineCountRuleNameColumn.setWidth(value);
		} else {
			lineCountRuleNameColumn.setWidth(LINECOUNTTABLE_RULE_COLUMN_SIZE);
		}

		// Set line count occurrences column width
		value = preferences.getInt(LINECOUNTOCCURRENCES_WIDTH_CONFIGURATION);
		if (value > 0) {
			lineCountOccurrencesColumn.setWidth(value);
		} else {
			lineCountOccurrencesColumn
					.setWidth(LINECOUNTTABLE_OCCURRENCES_COLUMN_SIZE);
		}

		// Set variable tracing rule name column width
		value = preferences.getInt(VARIABLENAME_WIDTH_CONFIGURATION);
		if (value > 0) {
			variableTracingRuleNameColumn.setWidth(value);
		} else {
			variableTracingRuleNameColumn
					.setWidth(VARIABLETABLE_RULE_COLUMN_SIZE);
		}

		// Set variable tracing value column width
		value = preferences.getInt(VARIABLEVALUE_WIDTH_CONFIGURATION);
		if (value > 0) {
			variableTracingValueColumn.setWidth(value);
		} else {
			variableTracingValueColumn
					.setWidth(VARIABLETABLE_VALUE_COLUMN_SIZE);
		}
	}

	/**
	 * Creates context menu
	 */
	private void createContextMenus() {

		// Create menu manager for line count table.
		final MenuManager lineCountMenuMgr = new MenuManager();
		lineCountMenuMgr.setRemoveAllWhenShown(true);
		lineCountMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillLineCountContextMenu(lineCountMenuMgr);
			}
		});

		// Create menu
		Menu menu = lineCountMenuMgr.createContextMenu(lineCountingTable);
		lineCountingTable.setMenu(menu);

		// Create menu manager for variable tracing table.
		final MenuManager variableTracingMenuMgr = new MenuManager();
		variableTracingMenuMgr.setRemoveAllWhenShown(true);
		variableTracingMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillVariableTracingContextMenu(variableTracingMenuMgr);
			}
		});

		// Create menu
		menu = variableTracingMenuMgr.createContextMenu(variableTracingTable);
		variableTracingTable.setMenu(menu);

		// Create menu manager for trace comment table.
		final MenuManager traceCommentMenuMgr = new MenuManager();
		traceCommentMenuMgr.setRemoveAllWhenShown(true);
		traceCommentMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillTraceCommentContextMenu(traceCommentMenuMgr);
			}
		});

		// Create menu
		menu = traceCommentMenuMgr.createContextMenu(traceCommentTable);
		traceCommentTable.setMenu(menu);

	}

	/**
	 * Fills the line count context menu
	 * 
	 * @param menuMgr
	 *            menu manager
	 */
	public void fillLineCountContextMenu(MenuManager menuMgr) {
		menuMgr.add(new AddLineCountRuleAction());
		if (lineCountingTable.getSelectionCount() == 1) {
			EditLineCountRuleAction editAction = new EditLineCountRuleAction(
					lineCountingTable.getSelectionIndex());
			DeactivateLineCountRuleAction deactivateAction = new DeactivateLineCountRuleAction(
					lineCountingTable.getSelectionIndex(), lineCountingTable);
			menuMgr.add(editAction);
			menuMgr.add(deactivateAction);
		}
	}

	/**
	 * Fills the variable tracing context menu
	 * 
	 * @param menuMgr
	 *            menu manager
	 */
	public void fillVariableTracingContextMenu(MenuManager menuMgr) {
		// Variable tracing history
		menuMgr.add(TraceViewerGlobals.getTraceViewer().getView()
				.getActionFactory().getOpenVariableTracingHistoryAction());
		if (variableTracingTable.getSelectionCount() == 0) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getOpenVariableTracingHistoryAction().setEnabled(false);
		} else {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getOpenVariableTracingHistoryAction().setEnabled(true);
		}

		// Add new and edit actions
		menuMgr.add(new AddVariableTracingRuleAction());
		if (variableTracingTable.getSelectionCount() == 1) {
			EditVariableTracingRuleAction editAction = new EditVariableTracingRuleAction(
					variableTracingTable.getSelectionIndex());
			DeactivateVariableTracingRuleAction deactivateAction = new DeactivateVariableTracingRuleAction(
					variableTracingTable.getSelectionIndex(),
					variableTracingTable);
			menuMgr.add(editAction);
			menuMgr.add(deactivateAction);
		}
	}

	/**
	 * Fills the trace comment table context menu
	 * 
	 * @param menuMgr
	 *            menu manager
	 */
	public void fillTraceCommentContextMenu(MenuManager menuMgr) {
		int selectedTraceNumber = 0;
		if (traceCommentTable.getSelectionCount() == 1) {
			selectedTraceNumber = ((Integer) traceCommentTable.getSelection()[0]
					.getData()).intValue();
		}

		// Add Jump, Edit and Remove actions
		JumpToTraceAction jumpToTraceAction = new JumpToTraceAction(
				selectedTraceNumber);
		EditTraceCommentAction editAction = new EditTraceCommentAction(
				selectedTraceNumber);
		RemoveTraceCommentAction removeAction = new RemoveTraceCommentAction(
				selectedTraceNumber);
		menuMgr.add(jumpToTraceAction);
		menuMgr.add(new Separator());
		menuMgr.add(editAction);
		menuMgr.add(removeAction);

		// If no comments are selected, disable the action
		if (traceCommentTable.getSelectionCount() != 1) {
			jumpToTraceAction.setEnabled(false);
			editAction.setEnabled(false);
			removeAction.setEnabled(false);
		}
	}

	/**
	 * Creates listeners for the tables
	 */
	private void createListeners() {

		// Add double click support
		lineCountingTable.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				EditLineCountRuleAction action = new EditLineCountRuleAction(
						lineCountingTable.getSelectionIndex());
				action.run();
			}
		});

		// Add double click support
		variableTracingTable.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				TraceViewerGlobals.getTraceViewer().getView()
						.getActionFactory()
						.getOpenVariableTracingHistoryAction().run();
			}
		});

		// Add double click support
		traceCommentTable.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				if (traceCommentTable.getSelectionCount() > 0) {
					int traceNumber = ((Integer) traceCommentTable
							.getSelection()[0].getData()).intValue();
					TraceViewerAPI.syncToTrace(traceNumber, traceNumber);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#clearAll()
	 */
	public void clearAll() {
		// Empty lineCount items
		List<LineCountItem> lineCountItems = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getLineCountProcessor().getLineCountItems();

		for (int i = 0; i < lineCountItems.size(); i++) {
			lineCountItems.get(i).setCount(0);
			lineCountItems.get(i).setChanged(true);
		}

		// Empty variableTracing items
		List<VariableTracingItem> variableTracingItems = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getVariableTracingItems();

		for (int i = 0; i < variableTracingItems.size(); i++) {
			variableTracingItems.get(i).clear();
			variableTracingItems.get(i).setChanged(true);
		}

		changeToLineCountingTable = true;
		changeToVariableTracingTable = true;
		changeToTraceCommentTable = true;
		update();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerViewInterface#hasUnshownData()
	 */
	public boolean hasUnshownData() {
		return hasUnshownData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerPropertyViewInterface#
	 * updatePropertyTables()
	 */
	public void updatePropertyTables() {
		changeToLineCountingTable = true;
		changeToVariableTracingTable = true;

		// Linecounts
		List<LineCountItem> lineItems = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLineCountProcessor()
				.getLineCountItems();
		for (int i = 0; i < lineItems.size(); i++) {
			lineItems.get(i).setChanged(true);
		}

		// Variables
		List<VariableTracingItem> varItems = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getVariableTracingItems();
		for (int i = 0; i < varItems.size(); i++) {
			varItems.get(i).setChanged(true);
		}

		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerPropertyViewInterface#
	 * updateTraceComments()
	 */
	public void updateTraceComments() {
		changeToTraceCommentTable = true;
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#update()
	 */
	public void update() {
		Display display = this.getDisplay();
		if ((display != null) && !display.isDisposed()) {
			display.asyncExec(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		if (changeToLineCountingTable || changeToVariableTracingTable) {

			long time = System.currentTimeMillis();
			// If update interval has passed, call the view update
			if (time > updateNextTime) {
				updateNextTime = time + UPDATE_INTERVAL;
				update();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Change to the linecounting table
		if (changeToLineCountingTable) {
			List<LineCountItem> items = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getLineCountProcessor()
					.getLineCountItems();

			if (!lineCountingTable.isDisposed()) {
				// If item count changed, update it to table
				for (int i = 0; i < items.size(); i++) {
					LineCountItem item = items.get(i);
					if (item.isChanged()) {
						lineCountingTable.getItem(i).setText(1,
								String.valueOf(item.getCount()));
						item.setChanged(false);
					}
				}
				changeToLineCountingTable = false;
			}
		}

		// Change to the variable tracing table
		if (changeToVariableTracingTable) {
			List<VariableTracingItem> items = TraceViewerGlobals
					.getTraceViewer().getDataProcessorAccess()
					.getVariableTracingProcessor().getVariableTracingItems();

			if (!variableTracingTable.isDisposed()) {
				// If item changed, update it to table
				for (int i = 0; i < items.size(); i++) {
					VariableTracingItem item = items.get(i);
					if (item.isChanged()) {
						variableTracingTable.getItem(i).setText(1,
								item.getValue());
						item.setChanged(false);
					}
				}
				changeToVariableTracingTable = false;
			}
		}

		// Change to the trace comment table
		if (changeToTraceCommentTable) {
			if (!traceCommentTable.isDisposed()) {
				Map<Integer, String> traceComments = TraceViewerGlobals
						.getTraceViewer().getDataProcessorAccess()
						.getTraceCommentHandler().getComments();

				if (!traceComments.isEmpty() && !traceCommentTable.isVisible()) {
					traceCommentTable.setVisible(true);

					// If table not visible, show it
					if (verticalSash.getWeights()[0] == 1000) {
						verticalSash.setWeights(new int[] { 50, 50 });
					}

					verticalSash.redraw();
				}

				traceCommentTable.removeAll();

				// Loop the comments map
				Iterator<Entry<Integer, String>> it = traceComments.entrySet()
						.iterator();
				while (it.hasNext()) {
					Entry<Integer, String> entry = it.next();
					TableItem item = new TableItem(traceCommentTable, SWT.NONE);
					item.setData(entry.getKey());
					String traceNumber = String.valueOf(entry.getKey());
					item
							.setText(new String[] { traceNumber,
									entry.getValue() });
				}
			}

		}

		hasUnshownData = false;
	}

	/**
	 * Creates new items to table
	 */
	public void createNewPropertyTableItems() {
		// Line counting table - delete old items
		lineCountingTable.removeAll();

		List<LineCountItem> countItems = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getLineCountProcessor()
				.getLineCountItems();

		// Insert new items
		for (int i = 0; i < countItems.size(); i++) {
			TableItem item = new TableItem(lineCountingTable, SWT.NONE);
			item.setText(new String[] { countItems.get(i).getName(),
					String.valueOf(countItems.get(i).getCount()) });
		}

		// Variabletracing table - delete old items
		variableTracingTable.removeAll();

		List<VariableTracingItem> variableItems = TraceViewerGlobals
				.getTraceViewer().getDataProcessorAccess()
				.getVariableTracingProcessor().getVariableTracingItems();

		// Insert new items
		for (int i = 0; i < variableItems.size(); i++) {
			TableItem item = new TableItem(variableTracingTable, SWT.NONE);
			item.setText(new String[] { variableItems.get(i).getName(),
					variableItems.get(i).getValue() });
		}

		changeToLineCountingTable = true;
		changeToVariableTracingTable = true;
		update();

	}

	/**
	 * Gets display
	 * 
	 * @return display
	 */
	private Display getDisplay() {
		Display display = null;
		if (parent != null && !parent.isDisposed()
				&& parent.getDisplay() != null
				&& !parent.getDisplay().isDisposed()) {
			display = parent.getDisplay();
		}
		return display;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerViewInterface#
	 * setLineCountTableChanged()
	 */
	public void setLineCountTableChanged() {
		changeToLineCountingTable = true;
		hasUnshownData = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerViewInterface#
	 * setVariableTracingTableChanged()
	 */
	public void setVariableTracingTableChanged() {
		changeToVariableTracingTable = true;
		hasUnshownData = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		parent.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#isDisposed()
	 */
	public boolean isDisposed() {
		boolean disposed = false;
		Display display = getDisplay();
		if (display == null || display.isDisposed()) {
			disposed = true;
		}
		return disposed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerPropertyViewInterface#
	 * getSelectedVariableItems()
	 */
	public int[] getSelectedVariableIndices() {
		return variableTracingTable.getSelectionIndices();
	}
}