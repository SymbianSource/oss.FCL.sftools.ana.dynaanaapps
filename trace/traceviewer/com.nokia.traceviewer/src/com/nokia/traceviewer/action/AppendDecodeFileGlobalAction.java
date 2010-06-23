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
 * Handler for append decode file global command
 *
 */
package com.nokia.traceviewer.action;

import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Handler for append decode file global command
 */
public final class AppendDecodeFileGlobalAction extends TraceViewerGlobalAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerGlobalAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
				.getAppendDecodeFileAction().run();
	}

}
