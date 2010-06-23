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
* Workbench listener
*
*/
package com.nokia.tracebuilder.eclipse;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.nokia.tracebuilder.file.FileUtils;

/**
 * Workbench listener
 * 
 */
final class WorkbenchListener implements IWindowListener, IPageListener,
		IPartListener2 {

	/**
	 * List of open windows
	 */
	private ArrayList<IWorkbenchWindow> windowList = new ArrayList<IWorkbenchWindow>();

	/**
	 * Callback for editor notifications
	 */
	private WorkbenchListenerCallback callback;

	/**
	 * Constructor
	 * 
	 * @param callback
	 *            callback for editor notifications
	 */
	WorkbenchListener(WorkbenchListenerCallback callback) {
		this.callback = callback;
	}

	/**
	 * Starts this listener
	 */
	void startListener() {
		PlatformUI.getWorkbench().addWindowListener(this);
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (IWorkbenchWindow element : windows) {
			windowOpened(element);
		}
	}

	/**
	 * Stops this listener
	 */
	void stopListener() {
		Iterator<IWorkbenchWindow> itr;
		do {
			itr = windowList.iterator();
			if (itr.hasNext()) {
				windowClosed(itr.next());
			}
		} while (itr.hasNext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#
	 *      windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#
	 *      windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#
	 *      windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
		windowList.remove(window);
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage element : pages) {
			pageClosed(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#
	 *      windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		boolean found = false;
		for (int j = 0; j < windowList.size(); j++) {
			if (windowList.get(j) == window) {
				found = true;
			}
		}
		if (!found) {
			window.addPageListener(this);
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage element : pages) {
				pageOpened(element);
			}
			windowList.add(window);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
		IEditorReference[] editors = page.getEditorReferences();
		for (IEditorReference element : editors) {
			partClosed(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
		IEditorReference[] references = page.getEditorReferences();
		for (IEditorReference element : references) {
			partOpened(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {
		IFile file = WorkbenchUtils.getEditorFile(partRef);
		if (file != null && FileUtils.isFileAllowed(file.getName())) {
			callback.editorOpened((ITextEditor) ((IEditorReference) partRef)
					.getEditor(false), file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef != null) {
			IFile file = WorkbenchUtils.getEditorFile(partRef);

			if (partRef instanceof IEditorReference) {
				IEditorPart editorPart = ((IEditorReference) partRef)
						.getEditor(false);
				// Check that editorPart is instance of ITextEditor
				if (editorPart instanceof ITextEditor) {
					// Variable file might be null, but it does not matter. So
					// null check is not needed.
					callback.editorClosed((ITextEditor) editorPart, file);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		IFile file = WorkbenchUtils.getEditorFile(partRef);
		if (file != null && FileUtils.isFileAllowed(file.getName())) {
			callback.editorActivated((ITextEditor) ((IEditorReference) partRef)
					.getEditor(false), file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
		IFile file = WorkbenchUtils.getEditorFile(partRef);
		if (file != null && FileUtils.isFileAllowed(file.getName())) {
			callback.editorReplaced((ITextEditor) ((IEditorReference) partRef)
					.getEditor(false), file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
		IFile file = WorkbenchUtils.getEditorFile(partRef);
		if (file != null && FileUtils.isFileAllowed(file.getName())) {
			callback.editorVisible((ITextEditor) ((IEditorReference) partRef)
					.getEditor(false), file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
		IFile file = WorkbenchUtils.getEditorFile(partRef);
		if (file != null && FileUtils.isFileAllowed(file.getName())) {
			callback.editorHidden((ITextEditor) ((IEditorReference) partRef)
					.getEditor(false), file);
		}
	}

}