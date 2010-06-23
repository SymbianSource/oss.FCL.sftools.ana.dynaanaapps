/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Open trace location command
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for open trace location command
 */
public final class OpenTraceLocationAction extends TraceViewerAction {

	/**
	 * Filter names in open file dialog
	 */
	private static final String[] FILTER_NAMES = {
			Messages.getString("OpenTraceLocationAction.SourceCodeFilter"), //$NON-NLS-1$
			Messages.getString("OpenTraceLocationAction.AllFilesFilter") }; //$NON-NLS-1$

	/**
	 * Filter extensions in open file dialog
	 */
	private static final String[] FILTER_EXTS = { "*.cpp", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Space
	 */
	private static final String SPACE = " "; //$NON-NLS-1$

	/**
	 * Trace MetaData used when opening location
	 */
	private TraceMetaData traceMetadata;

	/**
	 * If only the drive character is changed from the path of the source files,
	 * try to find those files also behind this drive
	 */
	private char changedDriveLetter;

	/**
	 * If this is true, we have already tried to open the source file also with
	 * the changed drive letter
	 */
	private boolean triedWithChangedDrive;

	/**
	 * If source is not found, ask user to browse for it
	 */
	private boolean askToBrowseSource;

	/**
	 * Image for this Action
	 */
	private static ImageDescriptor image;

	static {
		URL url = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/gotolocation.gif"); //$NON-NLS-1$
		image = ImageDescriptor.createFromURL(url);
	}

	/**
	 * Constructor
	 */
	OpenTraceLocationAction() {
		setText(Messages.getString("OpenTraceLocationAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("OpenTraceLocationAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(image);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				TraceViewerHelpContextIDs.ACTIONS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {

		// If metadata is null, don't do anything
		if (traceMetadata != null && traceMetadata.getPath() != null
				&& traceMetadata.getLineNumber() != 0) {

			// Open source file to editor
			openSourceFileToEditor(traceMetadata.getPath());
		}
	}

	/**
	 * Opens source file to editor
	 * 
	 * @param filePath
	 *            file path
	 */
	private void openSourceFileToEditor(String filePath) {
		IPath epocRootLocation = null;
		IFile file = null;
		IFile[] fileArr = null;

		// Dictionary location
		IPath dictionaryLocation = new Path(filePath);

		// Try to find EPOC root
		String epocRoot = getEpocRoot();
		if (epocRoot != null && epocRoot.length() > 1) {

			// Get only the device part (e.g. X:)
			epocRoot = epocRoot.substring(0, 2);
			epocRootLocation = dictionaryLocation.setDevice(epocRoot);
		}

		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();

		// Try to find with EPOC root location
		if (epocRootLocation != null) {
			fileArr = root.findFilesForLocationURI(makeURI(epocRootLocation));
		}

		// If not found, try to find with Dictionary location
		if (fileArr == null || fileArr.length == 0) {
			fileArr = root.findFilesForLocationURI(makeURI(dictionaryLocation));
		}

		// There can be only one file with this url so take the first
		// item
		if (fileArr != null && fileArr.length > 0) {
			file = fileArr[0];
		} else {
			// File not found from Workspace, create External files project
			file = createExternalFilesProject(dictionaryLocation, root);
		}

		// Open the file to the default editor
		if (file != null) {
			openEditor(file);
		}
	}

	/**
	 * Creates URI from given IPath instance.
	 * 
	 * @param path
	 * @return URI from given IPath instance
	 */
	private URI makeURI(IPath path) {
		File file = path.toFile();
		return file.toURI();
	}

	/**
	 * Opens the editor
	 * 
	 * @param file
	 */
	@SuppressWarnings("unchecked")
	private void openEditor(IFile file) {

		// Get workbench page
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		String defaultEditor = null;
		IEditorDescriptor editorDesc = IDE.getDefaultEditor(file);
		if (editorDesc == null) {
			defaultEditor = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			defaultEditor = editorDesc.getId();
		}

		HashMap<String, Comparable> map = new HashMap<String, Comparable>();
		map.put(IMarker.LINE_NUMBER, Integer.valueOf(traceMetadata
				.getLineNumber()));
		map.put(IDE.EDITOR_ID_ATTR, defaultEditor);

		// Try to set markers
		try {
			IMarker marker;
			marker = file.createMarker(IMarker.TEXT);

			marker.setAttributes(map);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates external files project
	 * 
	 * @param location
	 *            location of the file
	 * @param root
	 *            workspace root
	 * @return the file
	 */
	private IFile createExternalFilesProject(IPath location, IWorkspaceRoot root) {
		String externalProject = Messages
				.getString("OpenTraceLocationAction.ExternalFilesProject"); //$NON-NLS-1$

		IFile file = null;
		try {

			IProject project = root.getProject(externalProject);
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen()) {
				project.open(null);
			}

			// Location must be valid
			if (location.isValidPath(location.toOSString())) {
				file = project.getFile(location.lastSegment());
				if (!file.exists()) {
					file.createLink(location, IResource.NONE, null);

					// Creating the link succeeded
					triedWithChangedDrive = false;
				}
			} else {
				// Location not valid, give an error message

				String locationStr = Messages
						.getString("OpenTraceLocationAction.LocationStr"); //$NON-NLS-1$
				String notValidStr = Messages
						.getString("OpenTraceLocationAction.NotValidStr"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(
								locationStr + SPACE + location.toOSString()
										+ SPACE + notValidStr);
			}

		} catch (CoreException e) {
			file = null;

			// Try with changed drive letter
			if (changedDriveLetter != Character.UNASSIGNED
					&& !triedWithChangedDrive) {

				triedWithChangedDrive = true;
				String newPath = changedDriveLetter
						+ traceMetadata.getPath().substring(1);

				// Try to open with new drive letter
				openSourceFileToEditor(newPath);

			} else {

				triedWithChangedDrive = false;

				if (askToBrowseSource) {

					// Show file was not found dialog
					String notFoundStr = Messages
							.getString("OpenTraceLocationAction.CouldNotFindStr"); //$NON-NLS-1$
					String browseStr = Messages
							.getString("OpenTraceLocationAction.BrowseString"); //$NON-NLS-1$

					boolean browse = TraceViewerGlobals.getTraceViewer()
							.getDialogs().showConfirmationDialog(
									notFoundStr + SPACE + location.toOSString()
											+ browseStr);

					// Browse for a file
					if (browse) {

						// Try to find the original file but change the path to
						// OS specific string first
						IPath loc = new Path(traceMetadata.getPath());
						String osString = loc.toOSString();
						browseForTheFile(osString);
					}
				}
			}

		}
		return file;
	}

	/**
	 * Browse for a file
	 * 
	 * @param path
	 *            initial file path to open
	 */
	private void browseForTheFile(String path) {
		String pathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
		int indexOfLastSeparator = path.lastIndexOf(pathSeparator);

		// Get variables
		String fileName = path.substring(indexOfLastSeparator + 1);
		String[] names = { fileName, FILTER_NAMES[0], FILTER_NAMES[1] };
		String[] extensions = { fileName, FILTER_EXTS[0], FILTER_EXTS[1] };

		// Open the file dialog
		String[] files = TraceViewerActionUtils.openFileDialog(names,
				extensions, null, fileName, false, true);

		// Open the file to the editor
		if (files != null && files.length > 0) {
			String file = files[0];

			// Check if the only thing that was different in the old and new
			// path is the drive, save the new drive letter also
			String oldFilePath = path.substring(1);
			String newFilePath = file.substring(1);
			if (oldFilePath.equals(newFilePath)) {
				changedDriveLetter = file.charAt(0);
			}

			openSourceFileToEditor(file);
		}
	}

	/**
	 * Sets trace metadata
	 * 
	 * @param metaData
	 *            the metaData to set
	 * @param askToBrowseSource
	 *            if true and source was not found, ask user to browse for the
	 *            source
	 */
	public void setMetaData(TraceMetaData metaData, boolean askToBrowseSource) {
		this.traceMetadata = metaData;
		this.askToBrowseSource = askToBrowseSource;
	}

	/**
	 * Gets EPOC root from Carbide.c++
	 * 
	 * @return Current Epoc root
	 */
	private String getEpocRoot() {
		String epocRoot = null;
		IEditorInput input = null;
		IWorkbench wb = PlatformUI.getWorkbench();

		// Try to get active editor and input for that
		if (wb != null) {
			IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
			if (wbw != null) {
				IWorkbenchPage page = wbw.getActivePage();
				if (page != null) {
					IEditorPart editorPart = page.getActiveEditor();
					if (editorPart != null) {
						input = editorPart.getEditorInput();
					}
				}
			}
		}

		// Get the active File
		if (input instanceof FileEditorInput) {
			IFile file = ((FileEditorInput) input).getFile();
			IPath epocRootPath = getEpocRootForProject(file.getProject());

			if (epocRootPath != null) {
				epocRoot = epocRootPath.toOSString();
			}
		}

		return epocRoot;
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
	private IPath getEpocRootForProject(IProject project) {
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
}
