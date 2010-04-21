/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
*/



package com.nokia.s60tools.memspy.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.s60tools.memspy.ui.views.MemSpyMainView;
import com.nokia.s60tools.memspy.ui.wizards.MemSpyWizard.MemSpyWizardType;
import com.nokia.s60tools.util.debug.DbgUtility;





/**
 * Action for toolbar and import shortcuts that launch MemSpy
 * @see IWorkbenchWindowActionDelegate
 */
public class ToolbarShortcutAction implements IWorkbenchWindowActionDelegate {

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// Open MemSpy's Main View
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Show main view");
		MemSpyMainView mainView = MemSpyMainView.showAndReturnYourself();
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Run wizard");
		mainView.runWizard( MemSpyWizardType.FULL, null );
		
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Not needed
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
		// Not needed
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		// Not needed
	}
}