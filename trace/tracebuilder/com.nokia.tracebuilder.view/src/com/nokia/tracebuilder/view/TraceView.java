/*
 * Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Trace Builder view implementation
 *
 */
package com.nokia.tracebuilder.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerConsts;
import com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerPlugin;
import com.nokia.tracebuilder.action.ActionFactory;
import com.nokia.tracebuilder.action.TraceViewActions;
import com.nokia.tracebuilder.engine.TraceBuilderActions;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderView;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.preferences.ConfigurationImpl;

/**
 * Trace Builder view implementation
 * 
 */
public final class TraceView extends ViewPart implements TraceBuilderView {

	/**
	 * Trace Groups branch text
	 */
	private static final String TRACE_GROUPS_BRANCH_TEXT = "Trace Groups"; //$NON-NLS-1$

	/**
	 * Tree viewer widget
	 */
	private TreeViewer viewer;

	/**
	 * Tree view selection listener
	 */
	private TraceViewSelectionListener selectionListener;

	/**
	 * Tree view expansion listener
	 */
	private TraceViewTreeListener treeListener;

	/**
	 * Dialog interface implementation
	 */
	private TraceViewDialogs dialogs;

	/**
	 * Action factory
	 */
	private TraceViewActions actions;

	/**
	 * Configuration
	 */
	private TraceBuilderConfiguration configuration;

	/**
	 * Help listener
	 */
	private TraceViewHelpListener helpListener;

	/**
	 * Property dialog properties
	 */
	private PropertyDialogProperties propertyDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		actions = new ActionFactory();
		configuration = new ConfigurationImpl();
		selectionListener = new TraceViewSelectionListener(actions);
		// Creates the tree viewer
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);
		TraceContentProvider contentProvider = new TraceContentProvider();
		TraceLabelProvider labelProvider = new TraceLabelProvider();
		TraceNameSorter nameSorter = new TraceNameSorter();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setSorter(nameSorter);
		// Content provider creates the viewer updater. The updater needs the
		// display reference in order to create asynchronous callbacks to the UI
		// thread.
		viewer.setInput(getViewSite().getShell().getDisplay());
		treeListener = new TraceViewTreeListener(viewer);
		helpListener = new TraceViewHelpListener();
		viewer.addSelectionChangedListener(selectionListener);
		viewer.addDoubleClickListener(selectionListener);
		viewer.addHelpListener(helpListener);
		viewer.addTreeListener(treeListener);

		// Hooks actions to menus
		hookContextMenu();
		fillMenuAndToolBar();

		// Start using feature
		FeatureUseTrackerPlugin.getFeatureUseProxy().startUsingFeature(
				FeatureUseTrackerConsts.CARBIDE_OST_TRACE);

		// Sets the view reference to trace builder engine
		TraceBuilderGlobals.setView(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		TraceBuilderGlobals.setView(null);
		viewer.removeTreeListener(treeListener);
		viewer.removeSelectionChangedListener(selectionListener);
		viewer.removeHelpListener(helpListener);

		// Stop using feature
		FeatureUseTrackerPlugin.getFeatureUseProxy().stopUsingFeature(
				FeatureUseTrackerConsts.CARBIDE_OST_TRACE);

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#hasFocus()
	 */
	public boolean hasFocus() {
		return viewer.getControl().isFocusControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 * selectObject(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void selectObject(TraceObject object) {
		TraceObjectWrapper wrapper = object
				.getExtension(TraceObjectWrapper.class);
		// Wrapper is null if user makes a selection before view is updated
		if (wrapper != null) {
			revealSelectedObject(wrapper);
			actions.enableActions(object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 * selectLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void selectLocation(TraceLocation location) {
		TraceLocationWrapper wrapper = (TraceLocationWrapper) location
				.getProperties().getViewReference();
		// Wrapper is null if user makes a selection before view is updated
		if (wrapper != null) {
			revealSelectedObject(wrapper);
			actions.enableActions(location);
		}
	}

	/**
	 * Reveals the given wrapper
	 * 
	 * @param wrapper
	 *            the wrapper to be revealed
	 */
	private void revealSelectedObject(WrapperBase wrapper) {
		ListWrapper parent = (ListWrapper) wrapper.getParent();
		WrapperBase updated = parent.moveChildToView(wrapper);
		if (updated != null) {
			viewer.removeSelectionChangedListener(selectionListener);
			updated.getUpdater().update(updated);
			viewer.addSelectionChangedListener(selectionListener);
		}
		wrapper.getUpdater().queueSelection(wrapper);
	}

	/**
	 * Gets the view shell
	 * 
	 * @return the shell of the tree viewer
	 */
	Shell getShell() {
		Shell retval = null;
		if (viewer != null) {
			retval = viewer.getControl().getShell();
			if (retval != null && retval.isDisposed()) {
				retval = null;
			}
		}
		return retval;
	}

	/**
	 * Adds menu listener to popup menu
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Adds action items to menu and toolbar
	 */
	private void fillMenuAndToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		fillMenu(bars.getMenuManager());
		fillToolBar(bars.getToolBarManager());
	}

	/**
	 * Adds menu items to view pull-down menu
	 * 
	 * @param manager
	 *            the menu to be filled
	 */
	private void fillMenu(IMenuManager manager) {
		actions.fillMenu(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Adds actions to tool bar
	 * 
	 * @param manager
	 *            the tool bar
	 */
	private void fillToolBar(IToolBarManager manager) {
		actions.fillToolBar(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Called prior to showing the context menu
	 * 
	 * @param manager
	 *            the menu to be filled
	 */
	private void fillContextMenu(IMenuManager manager) {
		actions.fillContextMenu(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getDialogs()
	 */
	public TraceBuilderDialogs getDialogs() {
		if (dialogs == null) {
			dialogs = new TraceViewDialogs(this);
		}
		return dialogs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getPropertyDialog()
	 */
	public TraceObjectPropertyDialog getPropertyDialog() {
		if (propertyDialog == null) {
			propertyDialog = new PropertyDialogProperties(this);
		}
		return propertyDialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getActions()
	 */
	public TraceBuilderActions getActions() {
		return actions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#getConfiguration()
	 */
	public TraceBuilderConfiguration getConfiguration() {
		return configuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView#
	 * runAsyncOperation(java.lang.Runnable)
	 */
	public void runAsyncOperation(Runnable runnable) {
		Shell shell = getShell();
		if (shell != null) {
			shell.getDisplay().asyncExec(runnable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderView# refresh()
	 */
	public void refresh() {
		viewer.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderView#expandTraceGroupsBranch()
	 */
	public void expandTraceGroupsBranch() {
		TreeItem[] items = viewer.getTree().getItems();
		if (items != null) {
			for (TreeItem item : items) {
				if (item.getText().equals(TRACE_GROUPS_BRANCH_TEXT)) {
					item.setExpanded(true);
					break;
				}
			}
		}
	}
}