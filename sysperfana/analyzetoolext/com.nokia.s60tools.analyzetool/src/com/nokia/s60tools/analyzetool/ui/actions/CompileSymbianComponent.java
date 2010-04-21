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
 * Description:  Definitions for the class CompileSymbianComponent
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.s60tools.analyzetool.builder.CustomPreBuilder;

/**
 * Class to instrument and compile user selected mmp files with atool.exe.
 *
 * @author kihe
 *
 */
public class CompileSymbianComponent implements IObjectActionDelegate {

	/** list of user selected files.*/
	public List<IFile> selectedFiles;

	/**
	 * Constructor.
	 */
	public CompileSymbianComponent() {
		super();
		selectedFiles = new ArrayList<IFile>(0);
	}

	/**
	 * Instruments, builds and unistruments selected mmp's with AnalyzeTool builder.
	 *
	 * @param action User selected action
	 */
	public void run(IAction action) {

		// if for some reason makefile list is empty => leave
		if (selectedFiles.isEmpty()) {
			return;
		}

		CustomPreBuilder builder = new CustomPreBuilder();
		builder.buildComponents(selectedFiles);

	}

	/**
	 * Listening selection changed events and is selection is mmp file stores it to list.
	 *
	 * @param action User selected action
	 *
	 * @param selection User selection
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {

		// clear existing data
		selectedFiles.clear();

		// if selection is null => leave
		if (selection == null) {
			return;
		}

		// if selection is
		if (selection instanceof IStructuredSelection) {
			// thru selections
			for (Iterator<Object> iter = ((IStructuredSelection) selection)
					.iterator(); iter.hasNext();) {

				// get next selection
				Object selectionItem = iter.next();
				IFile file = null;
				// if selection is IFile
				if (selectionItem instanceof IFile) {
					file = (IFile) selectionItem;
				} else if (selectionItem instanceof IAdaptable) {
					file = (IFile) ((IAdaptable) selectionItem)
							.getAdapter(IFile.class);
				}

				// only allow mmp and mk files
				if ( file != null
						&& (file.getName().toUpperCase(Locale.US).endsWith(".MMP")
						|| file.getName().toUpperCase(Locale.US).endsWith(".MK"))) {
					selectedFiles.add(file);
				}

			}
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

		//do nothing be design
	}
}
