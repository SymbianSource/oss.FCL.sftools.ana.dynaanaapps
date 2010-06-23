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
 * Action for opening a source file
 *
 */
package com.nokia.tracebuilder.action;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Action for opening a source file
 * 
 */
final class OpenSourceAction extends TraceBuilderAction {

	/**
	 * Location which specifies the source to be opened
	 */
	private LastKnownLocation location;

	/**
	 * Constructor
	 */
	OpenSourceAction() {
		setText(Messages.getString("OpenSourceAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("OpenSourceAction.Tooltip")); //$NON-NLS-1$
		setDefaultProperties(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#doRun()
	 */
	@Override
	protected void doRun() throws TraceBuilderException {
		openSource(location.getFilePath(), location.getFileName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.action.TraceBuilderAction#getEnabledFlag(java.
	 * lang.Object)
	 */
	@Override
	protected boolean getEnabledFlag(Object selectedObject) {
		boolean retval;
		if (selectedObject instanceof LastKnownLocation) {
			location = (LastKnownLocation) selectedObject;
			retval = true;
		} else {
			location = null;
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.action.TraceBuilderAction#isInMenu()
	 */
	@Override
	protected boolean isInMenu() {
		return false;
	}

	/**
	 * Stores the location for next action call
	 * 
	 * @param location
	 *            the location
	 */
	void setLocation(LastKnownLocation location) {
		this.location = location;
	}

	/**
	 * Opens a source file
	 * 
	 * @param filePath
	 *            the file path
	 * @param fileName
	 *            the file name
	 */
	private void openSource(String filePath, String fileName) {
		String pathString = filePath + fileName;
		try {
			IPath path = new Path(pathString);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] fileArr = root.findFilesForLocationURI(FileUtils.makeURI(path));
			IFile file = null;
			// There can be only one file with this url so take the first item
			if (fileArr != null && fileArr.length > 0) {
				file = fileArr[0];
			}
			Trace trace = location.getTrace();

			// Store trace name, because it will be lost during editor open
			// operation
			String name = trace.getName();
			if (file != null) {
				IEditorDescriptor editorDesc = IDE.getDefaultEditor(file);
				String defaultEditor;
				if (editorDesc == null) {
					defaultEditor = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
				} else {
					defaultEditor = editorDesc.getId();
				}
				IDE.openEditor(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage(), file,
						defaultEditor, true);
			} else {
				String msg = Messages
						.getString("OpenSourceAction.FailedToOpenErrorPrefix") //$NON-NLS-1$
						+ pathString;
				TraceBuilderGlobals.getEvents().postErrorMessage(msg, null,
						true);
			}

			// Return trace name back to trace
			trace.setName(name);
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(trace,
					false, true);
		} catch (Exception e) {
			String msg = Messages
					.getString("OpenSourceAction.FailedToOpenErrorPrefix") //$NON-NLS-1$
					+ pathString;
			TraceBuilderGlobals.getEvents().postErrorMessage(msg, null, true);
		}
	}

}
