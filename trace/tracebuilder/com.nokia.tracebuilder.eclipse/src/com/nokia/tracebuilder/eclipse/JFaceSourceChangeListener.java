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
* Listener interface for source file changes
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * Listener interface for source file changes
 * 
 */
final class JFaceSourceChangeListener implements IDocumentListener {

	/**
	 * Workbench monitor
	 */
	private WorkbenchEditorMonitor monitor;

	/**
	 * Source to be monitored
	 */
	private JFaceDocumentWrapper source;

	/**
	 * Constructor
	 * 
	 * @param monitor
	 *            the workbench monitor
	 * @param source
	 *            the observed source
	 */
	JFaceSourceChangeListener(WorkbenchEditorMonitor monitor,
			JFaceDocumentWrapper source) {
		this.monitor = monitor;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener#
	 *      documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		try {
			monitor.sourceAboutToBeChanged(source, event.getOffset(), event
					.getLength(), event.getText());
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Document change preprocessor failure", e); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener#
	 *      documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		try {
			monitor.sourceChanged(source, event.getOffset(), event.getLength(),
					event.getText());
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Document change processor failure", e); //$NON-NLS-1$
			}
		}
	}
}
