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
* Model processing listener for event manager
*
*/
package com.nokia.tracebuilder.engine.event;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceProcessingListener;

/**
 * Model processing listener for event manager
 * 
 */
final class EventManagerProcessingListener implements TraceProcessingListener {

	/**
	 * The event manager
	 */
	private final EventEngine manager;

	/**
	 * @param manager
	 */
	EventManagerProcessingListener(EventEngine manager) {
		this.manager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceProcessingListener#processingComplete(boolean)
	 */
	public void processingComplete(boolean changed) {
		String err = manager.getProcessingError();
		if (err != null) {
			TraceBuilderGlobals.getDialogs().showErrorMessage(err);
			manager.resetProcessingError();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceProcessingListener#processingStarted()
	 */
	public void processingStarted() {
		manager.resetProcessingError();
	}
}