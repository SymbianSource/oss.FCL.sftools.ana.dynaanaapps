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
 * Description:  Definitions for the class ViewMemoryLeakFromFile
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
 * Entry point for displaying memory analysis results from file.
 * 
 * @author kihe
 * 
 */
public class ViewMemoryLeakFromFile implements IObjectActionDelegate {

	/** project reference. */
	public IProject project;

	/**
	 * Constructor.
	 */
	public ViewMemoryLeakFromFile() {
		super();
	}

	/**
	 * Performs this action.
	 * 
	 * @param action
	 *            User selected action
	 */
	public void run(IAction action) {

		// open AnalyzeTool window
		try {
			Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(Constants.ANALYZE_TOOL_VIEW_ID,
							null, org.eclipse.ui.IWorkbenchPage.VIEW_ACTIVATE);

		} catch (PartInitException pie) {
			pie.printStackTrace();
		}

		// analyze results
		IActionListener listener = Activator.getActionListener();
		if (listener != null) {
			listener.runAction(project, Constants.ACTIONS.RUN_VIEW_MEM_LEAKS);
		}
	}

	/**
	 * Notifies this action delegate that the selection in the workbench has
	 * changed.
	 * 
	 * @param action
	 *            User selected action
	 * 
	 * @param selection
	 *            User selection
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
	 * @param action
	 *            User selected action
	 * 
	 * @param targetPart
	 *            Workbench part
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}
}
