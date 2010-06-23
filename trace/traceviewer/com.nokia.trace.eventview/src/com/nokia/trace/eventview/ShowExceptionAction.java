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
 * Action to display exception contents
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action to display exception contents
 * 
 */
final class ShowExceptionAction extends Action {

	/**
	 * Number of elements to be shown
	 */
	private static final int MAX_STACK_TRACE_ELEMENTS_TO_DISPLAY = 20;

	/**
	 * The throwable
	 */
	private Throwable throwable;

	/**
	 * Constructor
	 * 
	 * @param throwable
	 *            the throwable to be shown
	 */
	ShowExceptionAction(Throwable throwable) {
		this.throwable = throwable;
		setText(Messages.getString("ShowExceptionAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ShowExceptionAction.Description")); //$NON-NLS-1$
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		if (throwable != null) {
			StringBuffer sb = new StringBuffer();
			StackTraceElement[] elements = throwable.getStackTrace();
			int end = elements.length;
			boolean max = false;
			if (end > MAX_STACK_TRACE_ELEMENTS_TO_DISPLAY) {
				end = MAX_STACK_TRACE_ELEMENTS_TO_DISPLAY;
				max = true;
			}
			sb.append(throwable.toString());
			sb.append(":\r\n"); //$NON-NLS-1$
			for (int i = 0; i < end; i++) {
				sb.append(elements[i].toString());
				sb.append("\r\n"); //$NON-NLS-1$
			}
			if (max) {
				sb.append(Messages
						.getString("ShowExceptionAction.ElementsCutOff")); //$NON-NLS-1$
			}
			MessageDialog.openInformation(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), Messages
					.getString("ShowExceptionAction.Title"), //$NON-NLS-1$
					sb.toString());
		}
	}
}
