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
* Tree viewer help listener
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.TraceBuilderHelp;

/**
 * Tree viewer help listener
 * 
 */
final class TraceViewHelpListener implements HelpListener {

	/**
	 * Constructor
	 * 
	 */
	TraceViewHelpListener() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.HelpListener#
	 * helpRequested(org.eclipse.swt.events.HelpEvent)
	 */
	public void helpRequested(HelpEvent e) {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				TraceBuilderHelp.HELP_CONTEXT_BASE
						+ TraceBuilderHelp.TREE_VIEWER_HELP_CONTEXT);

	}
}