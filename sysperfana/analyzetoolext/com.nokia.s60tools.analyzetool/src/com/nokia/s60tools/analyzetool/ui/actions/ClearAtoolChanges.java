/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ClearAtoolChanges
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.ui.IActionListener;

/**
 * Class to clear atool.exe made changes.
 *
 * @author kihe
 *
 */
public class ClearAtoolChanges implements IObjectActionDelegate {

	/** project reference.*/
	public IProject project;

	/**
	 * Constructor.
	 */
	public ClearAtoolChanges() {
		super();
	}

	/**
	 * Opens the AnalyzeTool view and runs clean action.
	 *
	 * @param action User selected action
	 */
	public void run(IAction action) {
		// open AnalyzeTool window
		try {
			Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(Constants.ANALYZE_TOOL_VIEW_ID,
							null, org.eclipse.ui.IWorkbenchPage.VIEW_CREATE);
		} catch (PartInitException pie) {
			pie.printStackTrace();
		}

		IActionListener listener = Activator.getActionListener();
		if (listener != null) {
			listener.runAction(project, Constants.ACTIONS.RUN_CLEAN);
		}

	}

	/**
	 * Listening selection changed events and stores selected project reference.
	 *
	 * @param action User selected action
	 *
	 * @param selection User selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			project = (IProject) ((IStructuredSelection) selection)
					.getFirstElement();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the active part for the delegate.
	 *
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 *
	 * @param action User selected action
	 *
	 * @param targetPart Workbench part
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}
}
