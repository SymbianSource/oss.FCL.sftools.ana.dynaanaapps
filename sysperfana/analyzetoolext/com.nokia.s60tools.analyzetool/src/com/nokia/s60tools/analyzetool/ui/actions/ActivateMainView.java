/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ActivateMainView
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Entry point for main view when selecting AnalyzeTool in Carbide menu.
 *
 * @author kihe
 *
 */
public class ActivateMainView implements IWorkbenchWindowActionDelegate {

	/**Active workbench window.*/
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public ActivateMainView() {
		// ConstructorDeclaration[@Private='false'][count(BlockStatement) = 0
		// and ($ignoreExplicitConstructorInvocation = 'true' or
		// not(ExplicitConstructorInvocation)) and @containsComment = 'false']
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init
	 *
	 * @param windowRef Workbench Window
	 */
	public void init(IWorkbenchWindow windowRef) {
		this.window = windowRef;
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#run(IAction)
	 *
	 * @param action User selected action
	 */
	public void run(IAction action) {

		try {
			IWorkbenchPage page = window.getActivePage();

			// display main view
			page.showView(Constants.ANALYZE_TOOL_VIEW_ID);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @see IWorkbenchWindowActionDelegate#selectionChanged(IAction action, ISelection selection)
	 *
	 * @param action User selected action
	 *
	 * @param selection User selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}
}
