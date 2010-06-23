/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Editor selection event listener
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Editor selection event listener
 * 
 */
final class JFaceSelectionListener implements ISelectionListener {

	/**
	 * Workbench monitor
	 */
	private WorkbenchEditorMonitor monitor;

	/**
	 * Source to be monitored
	 */
	private JFaceDocumentWrapper source;

	/**
	 * Flag for disabling next event
	 */
	private boolean disableNextSelectionEvent;

	/**
	 * Constructor
	 * 
	 * @param monitor
	 *            the monitor for callbacks
	 * @param source
	 *            the source which is observed
	 */
	JFaceSelectionListener(WorkbenchEditorMonitor monitor,
			JFaceDocumentWrapper source) {
		this.monitor = monitor;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!disableNextSelectionEvent) {
			if (part == source.getTextEditor()
					&& selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				monitor.selectionChanged(source, textSelection.getOffset(),
						textSelection.getLength());
			}
		} else {
			disableNextSelectionEvent = false;
		}
	}

	/**
	 * Disables the next selection event
	 */
	void disableNextSelectionEvent() {
		disableNextSelectionEvent = true;
	}

}
