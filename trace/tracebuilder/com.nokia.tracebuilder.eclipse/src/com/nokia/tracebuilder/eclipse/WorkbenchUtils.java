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
 * Description:
 *
 * Utility functions for workbench monitoring
 *
 */
package com.nokia.tracebuilder.eclipse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.file.FileUtils;

/**
 * Utility functions for workbench monitoring
 * 
 */
final class WorkbenchUtils {

	/**
	 * Gets the path from given editor input
	 * 
	 * @param input
	 *            the editor input
	 * @return the path
	 */
	static String getEditorInputPath(IEditorInput input) {
		String ret = null;
		// Eclipse 3.2
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pathInput = (IPathEditorInput) input;
			if (pathInput.exists()) {
				IPath path = ((IPathEditorInput) input).getPath();
				if (path != null) {
					ret = path.toOSString();
				}
			}
		} else {
			// Eclipse 3.3 might not be present
			try {
				Class<?> uriEditor = Class
						.forName("org.eclipse.ui.IURIEditorInput"); //$NON-NLS-1$
				if (uriEditor.isAssignableFrom(input.getClass())) {
					Method m = uriEditor.getMethod("getURI"); //$NON-NLS-1$
					URI uri = (URI) m.invoke(input);
					if (uri != null) {
						ret = uri.getPath();
					}
				}
			} catch (ClassNotFoundException e) {
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return ret;
	}

	/**
	 * Checks if there are any editors open
	 * 
	 * @return true if editors are open, false if not
	 */
	static boolean isEditorsOpen() {
		boolean editorOpen = false;
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] refs = page.getEditorReferences();
				for (IEditorReference ref : refs) {
					IFile file = WorkbenchUtils.getEditorFile(ref);
					if (file != null && FileUtils.isFileAllowed(file.getName())) {
						editorOpen = true;
						break;
					}
				}
				if (editorOpen) {
					break;
				}
			}
			if (editorOpen) {
				break;
			}
		}
		return editorOpen;
	}

	/**
	 * Checks if the editor is valid and returns the file the editor is
	 * associated to
	 * 
	 * @param partRef
	 *            the editor reference
	 * @return the editor file
	 */
	static IFile getEditorFile(IWorkbenchPartReference partRef) {
		IFile editorFile = null;
		if (partRef instanceof IEditorReference) {
			IEditorPart editor = ((IEditorReference) partRef).getEditor(false);
			if (editor instanceof ITextEditor) {
				IDocumentProvider provider = ((ITextEditor) editor)
						.getDocumentProvider();
				if (provider != null) {
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IPathEditorInput) {
						IPath path = null;

						IPathEditorInput pathEditorInput = (IPathEditorInput) input;
						if (pathEditorInput.exists()) {
							path = pathEditorInput.getPath();
						}

						if (path != null) {
							IWorkspaceRoot root = ResourcesPlugin
									.getWorkspace().getRoot();

							IFile[] fileArr = root
									.findFilesForLocationURI(FileUtils
											.makeURI(path));
							// There can be only one file with this path
							if (fileArr != null && fileArr.length > 0) {
								editorFile = fileArr[0];
							}
						}

					}
				}
			}
		}
		return editorFile;
	}

	/**
	 * Returns the absolute file system path to the EPOCROOT directory of the
	 * SDK for the active build configuration of the project
	 * 
	 * @param project
	 *            the project
	 * @return the absolute path to EPOCROOT, or null if the project is not a
	 *         Carbide project, is closed, or there are no build configurations
	 *         in the project.
	 */
	static IPath getEpocRootForProject(IProject project) {
		IPath epocroot = null;

		if (project != null) {
			if (CarbideBuilderPlugin.getBuildManager()
					.isCarbideProject(project)
					&& project.isAccessible()) {
				ICarbideProjectInfo cpi = CarbideBuilderPlugin
						.getBuildManager().getProjectInfo(project);
				if (cpi != null) {
					ICarbideBuildConfiguration config = cpi
							.getDefaultConfiguration();
					if (config != null) {
						ISymbianSDK sdk = config.getSDK();
						if (sdk != null) {
							epocroot = new Path(sdk.getEPOCROOT());
						}
					}
				}
			}
		}

		return epocroot;
	}

	/**
	 * Posts user include added message
	 * 
	 * @param mmpPath
	 *            MMP path
	 */
	protected static void postUserIncludeMsg(IPath mmpPath) {
		String msg = Messages.getString("WorkbenchUtils.UserIncludeMsg"); //$NON-NLS-1$

		// Post the message
		TraceBuilderGlobals.getEvents().postInfoMessage(msg,
				mmpPath.lastSegment());
	}
}
