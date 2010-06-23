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
* Creates the action objects for Trace Builder view
*
*/
package com.nokia.tracebuilder.action;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * Creates the action objects for Trace Builder view
 * 
 */
public final class ActionFactory implements TraceViewActions {

	/**
	 * List of menu actions
	 */
	private ArrayList<Object> actions = new ArrayList<Object>();

	/**
	 * Menu / toolbar separator
	 */
	private Separator separator = new Separator();

	/**
	 * Last selection is always stored
	 */
	private Object lastSelectedObject;

	/**
	 * Constructor
	 */
	public ActionFactory() {
		createActions();
	}

	/**
	 * Enables / disables actions based on current selection. The labels for
	 * some of the actions are updated depending on which object has been
	 * selected.
	 * 
	 * @param selectedObject
	 *            the object that is currently selected
	 */
	public void enableActions(Object selectedObject) {
		for (int i = 0; i < actions.size(); i++) {
			Object action = actions.get(i);
			if (action instanceof TraceBuilderAction) {
				((TraceBuilderAction) action).setEnabled(TraceBuilderGlobals
						.getTraceModel().isValid(), selectedObject);
			}
		}
		lastSelectedObject = selectedObject;
	}

	/**
	 * Fills the view menu with the actions
	 * 
	 * @param manager
	 *            the menu manager
	 */
	public void fillMenu(IMenuManager manager) {
		for (int i = 0; i < actions.size(); i++) {
			Object action = actions.get(i);
			if (action instanceof TraceBuilderAction
					&& ((TraceBuilderAction) action).isInMenu()) {
				manager.add((TraceBuilderAction) action);
			} else if (action instanceof IContributionItem) {
				manager.add((IContributionItem) action);
			}
		}
	}

	/**
	 * Fills the right-click popup-menu.
	 * 
	 * @param manager
	 *            the menu manager
	 */
	public void fillContextMenu(IMenuManager manager) {
		for (int i = 0; i < actions.size(); i++) {
			Object action = actions.get(i);
			if (action instanceof TraceBuilderAction
					&& ((TraceBuilderAction) action)
							.isInPopupMenu(lastSelectedObject)) {
				manager.add((TraceBuilderAction) action);
			} else if (action instanceof IContributionItem) {
				manager.add((IContributionItem) action);
			}
		}
		addSelectionActions(manager);
	}

	/**
	 * Adds the actions from the selection list to the manager
	 * 
	 * @param manager
	 *            the menu manager
	 */
	private void addSelectionActions(IMenuManager manager) {
		if (lastSelectedObject instanceof TraceViewActionProvider) {
			Iterator<IAction> itr = ((TraceViewActionProvider) lastSelectedObject)
					.getActions();
			if (itr.hasNext()) {
				manager.add(separator);
			}
			while (itr.hasNext()) {
				manager.add(itr.next());
			}
		}
	}

	/**
	 * Fills the tool bar
	 * 
	 * @param manager
	 *            the tool bar manager
	 */
	public void fillToolBar(IToolBarManager manager) {
		manager.add(separator);
	}

	/**
	 * Creates the menu actions and adds them to the actions list
	 */
	private void createActions() {
		actions.add(new AddTraceAction());
		actions.add(new AddParameterAction());
		// Constant tables go to the property file
		actions.add(separator);
		actions.add(new AddConstantAction());
		actions.add(new ParseEnumAction());
		actions.add(separator);
		actions.add(new EditPropertiesAction());
		actions.add(new DeleteAction());
		actions.add(separator);
		actions.add(new InstrumentAction());
		actions.add(new DeleteTracesAction());
		actions.add(separator);
		actions.add(separator);
		actions.add(new ConvertTraceAction());
		actions.add(new OpenSourceAction());
		actions.add(new RemoveSourceAction());
		actions.add(new RemoveUnrelatedAction());
		enableActions(null);
	}

}
