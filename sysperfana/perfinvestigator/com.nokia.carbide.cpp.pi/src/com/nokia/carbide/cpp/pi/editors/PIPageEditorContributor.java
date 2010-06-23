/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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


package com.nokia.carbide.cpp.pi.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
public class PIPageEditorContributor extends MultiPageEditorActionBarContributor {
	
	private MenuManager piManager;

	/**
	 * Creates a multi-page contributor.
	 */
	public PIPageEditorContributor() {
		super();
		PIPageEditor.createActions();
	}

	public void setActivePage(IEditorPart part) {
	}

	public void contributeToMenu(IMenuManager manager) {
		// record the ActionBars object
		PIPageEditor.setMenuManager(manager);

		// record the ActionBars object
		if (getActionBars() != null)
			PIPageEditor.setActionBars(getActionBars());

		// create a top-level menu manager for PIPageEditor
		if (piManager != null) {
			piManager.dispose();
		}
		piManager = new MenuManager(Messages.getString("PIPageEditorContributor.PerformanceInvestigator"), PIPageEditor.MENU_ID); //$NON-NLS-1$
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, piManager);
		PIPageEditor.setPIMenuManager(piManager);
	}
	
	public void contributeToToolBar(IToolBarManager manager) {
		// add the created actions to the tool bar
		manager.add(new Separator("piEditorGroup1")); //$NON-NLS-1$
		manager.add(PIPageEditor.getSelectTimeAction());
		manager.add(new Separator("piEditorGroup2")); //$NON-NLS-1$
		manager.add(PIPageEditor.getZoomInAction());
		manager.add(PIPageEditor.getZoomOutAction());
		manager.add(PIPageEditor.getZoomToSelectionAction());
		manager.add(PIPageEditor.getZoomToTraceAction());
	}
	
	public void init(IActionBars bars) {
		super.init(bars);
	}
}
