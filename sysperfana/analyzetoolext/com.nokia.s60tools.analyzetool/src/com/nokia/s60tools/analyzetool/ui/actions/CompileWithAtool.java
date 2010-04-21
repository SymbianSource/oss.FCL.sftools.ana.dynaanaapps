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
 * Description:  Definitions for the class CompileWithAtoolS60
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
import com.nokia.s60tools.analyzetool.builder.BuilderUtil;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.ui.IActionListener;

/**
 * Class to compile project with atool.exe.
 *
 * @author kihe
 *
 */
public class CompileWithAtool implements IObjectActionDelegate {

	/** project reference.*/
	public IProject project;

	/**
	 * Constructor.
	 */
	public CompileWithAtool() {
		super();
	}

	/**
	 * Opens AnalyzeTool view and activates/deactivates build with AnalyzeTool.
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
			listener.runAction(project, Constants.ACTIONS.RUN_BUILD);
		}
	}

	/**
	 * Listening selection changed events and updates AnalyzeTool
	 * "build with AnalyzeTool" action state.
	 *
	 * @param action User selected action
	 *
	 * @param selection User selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		try {
			project = (IProject) ((IStructuredSelection) selection)
					.getFirstElement();
			BuilderUtil util = new BuilderUtil();
			action.setChecked(util.isNatureEnabled(project));
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
	 * @param targetPart Workbench Part
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

}
