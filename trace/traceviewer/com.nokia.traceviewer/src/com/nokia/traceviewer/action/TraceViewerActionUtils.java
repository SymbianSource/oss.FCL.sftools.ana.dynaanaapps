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
 * TraceViewer Action Utils contains utilities that can be used from all Actions
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerViewInterface;

/**
 * TraceViewer Action Utils contains utilities that can be used from all Actions
 * 
 */
public class TraceViewerActionUtils {

	/**
	 * Previous filter path
	 */
	private static String previousFilterPath;

	/**
	 * Copy buffer size
	 */
	private static final int COPY_BUFFER_SIZE = 8192 * 100 * 2;

	/**
	 * Comment prefix
	 */
	public static final String COMMENT_PREFIX = " // "; //$NON-NLS-1$

	/**
	 * Traces dropped message
	 */
	public static final String TRACES_DROPPED_MSG = Messages
			.getString("TraceViewerActionUtils.TracesDroppedMsg"); //$NON-NLS-1$

	/**
	 * Opens a file browse dialog
	 * 
	 * @param filters
	 *            file filters
	 * @param filterExtensions
	 *            file filter extensions
	 * @param filterPath
	 *            initial filter path to open
	 * @param fileName
	 *            initial filename to open
	 * @param multiSelect
	 *            if true, open as multiselection dialog
	 * @param opendialog
	 *            if true, open as OPEN dialog. If false, open as SAVE dialog.
	 * 
	 * @return array of selected files or null if no files were selected
	 */
	static String[] openFileDialog(String[] filters, String[] filterExtensions,
			String filterPath, String fileName, boolean multiSelect,
			boolean opendialog) {
		String pathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
		Shell activeShell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();
		Shell fileDialogShell = new Shell(activeShell);
		FileDialog dlg;

		// Create as multiselect dialog
		if (multiSelect) {
			if (opendialog) {
				dlg = new FileDialog(fileDialogShell, SWT.MULTI | SWT.OPEN);
			} else {
				dlg = new FileDialog(fileDialogShell, SWT.MULTI | SWT.SAVE);
			}
		} else {
			if (opendialog) {
				dlg = new FileDialog(fileDialogShell, SWT.OPEN);
			} else {
				dlg = new FileDialog(fileDialogShell, SWT.SAVE);
			}
		}

		dlg.setFilterNames(filters);
		dlg.setFilterExtensions(filterExtensions);
		String filterPathToUse = filterPath;
		if (filterPathToUse == null) {
			filterPathToUse = previousFilterPath;
		}
		dlg.setFilterPath(filterPathToUse);
		dlg.setFileName(fileName);

		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = activeShell.getBounds();
		Point dialogSize = fileDialogShell.getSize();

		fileDialogShell.setLocation(shellBounds.x
				+ (shellBounds.width - dialogSize.x) / 2, shellBounds.y
				+ (shellBounds.height - dialogSize.y) / 2);
		String file = dlg.open();
		String[] files = null;
		if (file != null) {
			files = dlg.getFileNames();

			// Add path to files
			for (int i = 0; i < files.length; i++) {
				files[i] = dlg.getFilterPath() + pathSeparator + files[i];
			}
			previousFilterPath = dlg.getFilterPath();
		}
		return files;
	}

	/**
	 * Copies file to another file
	 * 
	 * @param sourceFile
	 *            to to be copied
	 * @param targetFile
	 *            new file to be created
	 * @param callback
	 *            callback whom to inform about file position when copying. Can
	 *            be null.
	 * @param fileStartOffset
	 *            source file reading start offset
	 * @throws Exception
	 */
	public static void copyFile(File sourceFile, File targetFile,
			CopyFileProgressCallback callback, long fileStartOffset)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;

		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int bytesRead;
		long numberOfBytesTransferred = 0;
		try {
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(targetFile);

			// Skip bytes
			long skipped = in.skip(fileStartOffset);
			if (skipped != fileStartOffset) {
				throw new Exception(Messages
						.getString("TraceViewerActionUtils.SkippingFailedMsg")); //$NON-NLS-1$
			}

			while ((bytesRead = in.read(buffer)) >= 0) {
				out.write(buffer, 0, bytesRead);
				numberOfBytesTransferred += bytesRead;

				// Inform callback about new file position
				if (callback != null) {
					callback.notifyFilePosition(numberOfBytesTransferred);

					// If callback says cancel, abort the copy
					if (callback.cancelCopying()) {
						break;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}

			// Inform callback that copying ended
			if (callback != null) {
				callback.copyingFinished();
			}
		}
	}

	/**
	 * Opens property view if it's not open
	 */
	public static void openPropertyView() {
		// View is already open, do nothing
		if (TraceViewerGlobals.getTraceViewer().getPropertyView() != null
				&& !TraceViewerGlobals.getTraceViewer().getPropertyView()
						.isDisposed()) {

		} else {
			// Open it
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(
								TraceViewerViewInterface.PROPERTYVIEW_ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
