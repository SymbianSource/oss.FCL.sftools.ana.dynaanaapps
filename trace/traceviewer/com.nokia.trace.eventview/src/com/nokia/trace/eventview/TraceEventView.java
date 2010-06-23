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
 * Event view
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Event view. {@link #getEventList() getEventList} method is the access point
 * for view extensions.
 */
public class TraceEventView extends ViewPart implements ContentChangeListener {

	/**
	 * Type column title
	 */
	private static final String TYPE_TITLE = Messages
			.getString("TraceEventView.TypeColumnTitle"); //$NON-NLS-1$

	/**
	 * Category column title
	 */
	private static final String CATEGORY_TITLE = Messages
			.getString("TraceEventView.CategoryColumnTitle"); //$NON-NLS-1$

	/**
	 * Source column title
	 */
	private static final String SOURCE_TITLE = Messages
			.getString("TraceEventView.SourceColumnTitle"); //$NON-NLS-1$

	/**
	 * Description column title
	 */
	private static final String DESCRIPTION_TITLE = Messages
			.getString("TraceEventView.DescriptionColumnTitle"); //$NON-NLS-1$

	/**
	 * Type width configuration entry
	 */
	private static final String TYPE_WIDTH_CONFIGURATION = "TraceEventView.TypeWidth"; //$NON-NLS-1$

	/**
	 * Category width configuration entry
	 */
	private static final String CATEGORY_WIDTH_CONFIGURATION = "TraceEventView.CategoryWidth"; //$NON-NLS-1$

	/**
	 * Source width configuration entry
	 */
	private static final String SOURCE_WIDTH_CONFIGURATION = "TraceEventView.SourceWidth"; //$NON-NLS-1$

	/**
	 * Description width configuration entry
	 */
	private static final String DESCRIPTION_WIDTH_CONFIGURATION = "TraceEventView.DescriptionsWidth"; //$NON-NLS-1$

	/**
	 * Default width for type column
	 */
	private static final int DEFAULT_TYPE_WIDTH = 75;

	/**
	 * Default width for category column
	 */
	private static final int DEFAULT_CATEGORY_WIDTH = 150;

	/**
	 * Default width for source column
	 */
	private static final int DEFAULT_SOURCE_WIDTH = 150;

	/**
	 * Default width for description column
	 */
	private static final int DEFAULT_DESCRIPTION_WIDTH = 800;

	/**
	 * Type column index
	 */
	static final int TYPE_COLUM_INDEX = 0;

	/**
	 * Category column index
	 */
	static final int CATEGORY_COLUM_INDEX = 1;

	/**
	 * Source column index
	 */
	static final int SOURCE_COLUM_INDEX = 2;

	/**
	 * Description column index
	 */
	static final int DESCRIPTION_COLUM_INDEX = 3;

	/**
	 * Message list
	 */
	private TableViewer table;

	/**
	 * Remove event action
	 */
	private RemoveEventAction removeEventAction;

	/**
	 * Remove event action
	 */
	private RemoveAllEventsAction removeAllEventsAction;

	/**
	 * Descriptions column
	 */
	private TableColumn descriptionColumn;

	/**
	 * Descriptions column
	 */
	private TableColumn categoryColumn;

	/**
	 * Source column
	 */
	private TableColumn sourceColumn;

	/**
	 * Type column
	 */
	private TableColumn typeColumn;

	/**
	 * Content change listener
	 */
	public static ContentChangeListener contentChangeListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		table = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		EventListContentProvider contentProvider = (EventListContentProvider) getEventList();
		EventListLabelProvider labelProvider = new EventListLabelProvider();
		removeEventAction = new RemoveEventAction(contentProvider);
		removeAllEventsAction = new RemoveAllEventsAction(contentProvider);
		table.setContentProvider(contentProvider);
		table.setLabelProvider(labelProvider);

		// Content provider creates the viewer updater. The updater needs the
		// display reference in order to create asynchronous callbacks to the UI
		// thread.
		Table tableWidget = table.getTable();
		createTypeColumn(tableWidget);
		createCategoryColumn(tableWidget);
		createSourceColumn(tableWidget);
		createDescriptionColumn(tableWidget);
		configureWidgets();
		tableWidget.setHeaderVisible(true);
		tableWidget.setLinesVisible(true);
		hookContextMenu();
		table.setInput(getViewSite().getShell().getDisplay());
		setContentListener(this);
	}

	/**
	 * Sets content listener
	 * 
	 * @param listener
	 *            new content listener
	 */
	private void setContentListener(ContentChangeListener listener) {
		contentChangeListener = listener;
	}

	/**
	 * Configures the widgets
	 */
	void configureWidgets() {

		// Get preference store from the plugin
		IPreferenceStore preferences = Activator.getDefault()
				.getPreferenceStore();

		// Set description column width
		int value = preferences.getInt(DESCRIPTION_WIDTH_CONFIGURATION);
		if (value > 0) {
			descriptionColumn.setWidth(value);
		} else {
			descriptionColumn.setWidth(DEFAULT_DESCRIPTION_WIDTH);
		}

		// Set category column width
		value = preferences.getInt(CATEGORY_WIDTH_CONFIGURATION);
		if (value > 0) {
			categoryColumn.setWidth(value);
		} else {
			categoryColumn.setWidth(DEFAULT_CATEGORY_WIDTH);
		}

		// Set source column width
		value = preferences.getInt(SOURCE_WIDTH_CONFIGURATION);
		if (value > 0) {
			sourceColumn.setWidth(value);
		} else {
			sourceColumn.setWidth(DEFAULT_SOURCE_WIDTH);
		}

		// Set type column width
		value = preferences.getInt(TYPE_WIDTH_CONFIGURATION);
		if (value > 0) {
			typeColumn.setWidth(value);
		} else {
			typeColumn.setWidth(DEFAULT_TYPE_WIDTH);
		}
	}

	/**
	 * Creates the descriptions column
	 * 
	 * @param tableWidget
	 *            the table widget
	 */
	private void createDescriptionColumn(Table tableWidget) {
		descriptionColumn = new TableColumn(tableWidget, SWT.NONE);
		descriptionColumn.setText(DESCRIPTION_TITLE);
		descriptionColumn.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(
						DESCRIPTION_WIDTH_CONFIGURATION,
						descriptionColumn.getWidth());
			}

		});
		createColumnSorter(tableWidget, descriptionColumn,
				DESCRIPTION_COLUM_INDEX);
	}

	/**
	 * Creates the category column
	 * 
	 * @param tableWidget
	 *            the table widget
	 */
	private void createCategoryColumn(Table tableWidget) {
		categoryColumn = new TableColumn(tableWidget, SWT.NONE);
		categoryColumn.setText(CATEGORY_TITLE);
		categoryColumn.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Activator.getDefault().getPreferenceStore()
						.setValue(CATEGORY_WIDTH_CONFIGURATION,
								categoryColumn.getWidth());
			}

		});
		createColumnSorter(tableWidget, categoryColumn, CATEGORY_COLUM_INDEX);
	}

	/**
	 * Creates the sources column
	 * 
	 * @param tableWidget
	 *            the table widget
	 */
	private void createSourceColumn(Table tableWidget) {
		sourceColumn = new TableColumn(tableWidget, SWT.NONE);
		sourceColumn.setText(SOURCE_TITLE);
		sourceColumn.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(
						SOURCE_WIDTH_CONFIGURATION, sourceColumn.getWidth());
			}

		});
		createColumnSorter(tableWidget, sourceColumn, SOURCE_COLUM_INDEX);
	}

	/**
	 * Creates the type column
	 * 
	 * @param tableWidget
	 *            the table widget
	 */
	private void createTypeColumn(final Table tableWidget) {
		typeColumn = new TableColumn(tableWidget, SWT.NONE);
		typeColumn.setText(TYPE_TITLE);
		typeColumn.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(
						TYPE_WIDTH_CONFIGURATION, typeColumn.getWidth());
			}

		});
		createColumnSorter(tableWidget, typeColumn, TYPE_COLUM_INDEX);
	}

	/**
	 * Creates sort listener
	 * 
	 * @param tableWidget
	 *            table widget
	 * @param column
	 *            column to create sort listener for
	 * @param index
	 *            column index
	 */
	private void createColumnSorter(final Table tableWidget,
			final TableColumn column, final int index) {

		TableColumnSorter cSorter = new TableColumnSorter(table, column) {

			@Override
			protected int doCompare(Viewer v, Object o1, Object o2) {
				ITableLabelProvider lp = ((ITableLabelProvider) table
						.getLabelProvider());
				String t1 = lp.getColumnText(o1, index);
				String t2 = lp.getColumnText(o2, index);
				return t1.compareTo(t2);
			}
		};

		// Set no sorter as initial state
		cSorter.setSorter(cSorter, TableColumnSorter.NONE);
	}

	/**
	 * Adds menu listener to popup menu
	 */
	private void hookContextMenu() {

		// Create menumanager
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);

		// Create the menu when event is received
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection) table
						.getSelection();
				if (!selection.isEmpty()) {
					EventListEntry entry = (EventListEntry) selection
							.getFirstElement();
					removeEventAction.setEntry(entry);
					manager.add(removeAllEventsAction);
					manager.add(removeEventAction);
					if (entry.hasSourceActions()) {
						manager.add(new Separator());
						entry.addSourceActions(manager);
					}
					manager.add(new Separator(
							IWorkbenchActionConstants.MB_ADDITIONS));
				}
			}
		});

		// Set the menu in to the table
		Menu menu = menuMgr.createContextMenu(table.getControl());
		table.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, table);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		table.getControl().setFocus();
	}

	/**
	 * Gets the event list interface
	 * 
	 * @return the event list
	 */
	public static TraceEventList getEventList() {
		TraceEventList list = Activator.getDefault().eventList;
		if (list == null) {
			list = new EventListContentProvider();
			Activator.getDefault().eventList = list;
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.ContentChangeListener#contentChanged()
	 */
	public void contentChanged() {
		IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService) getSite()
				.getAdapter(IWorkbenchSiteProgressService.class);
		progressService.warnOfContentChange();
	}
}
