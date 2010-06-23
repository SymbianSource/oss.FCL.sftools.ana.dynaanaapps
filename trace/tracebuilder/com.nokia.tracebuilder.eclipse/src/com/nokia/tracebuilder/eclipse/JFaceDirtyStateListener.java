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
* Dirty state change notification
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;

/**
 * Dirty state change notification
 * 
 */
final class JFaceDirtyStateListener implements IPropertyListener {

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
	 *            source to be monitored
	 */
	JFaceDirtyStateListener(WorkbenchEditorMonitor monitor,
			JFaceDocumentWrapper source) {
		this.monitor = monitor;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
	 *      int)
	 */
	public void propertyChanged(Object obj, int propId) {
		if (propId == IWorkbenchPartConstants.PROP_DIRTY) {
			if (!source.getTextEditor().isDirty()) {
				monitor.sourceSaved(source);
			}
		}
	}
}