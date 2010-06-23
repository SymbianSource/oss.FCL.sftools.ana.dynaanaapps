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
* Implements TraceBuilderEvents to forward events to the event router plug-in
*
*/
package com.nokia.tracebuilder.engine.event;

import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventrouter.TraceEventRouter;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderEvents;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Implements TraceBuilderEvents to forward events to the event router plug-in
 * 
 */
public final class EventEngine implements TraceBuilderEvents {

	/**
	 * Error which is shown after sequential processing
	 */
	private String processingError;

	/**
	 * Category for new events
	 */
	private String defaultCategory = "General"; //$NON-NLS-1$

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public EventEngine(TraceModel model) {
		model.addProcessingListener(new EventManagerProcessingListener(this));
		this.model = model;
	}

	/**
	 * Resets the processing error
	 */
	void resetProcessingError() {
		processingError = null;
	}

	/**
	 * Gets the processing error
	 * 
	 * @return the error
	 */
	String getProcessingError() {
		return processingError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#
	 *      postError(com.nokia.tracebuilder.engine.TraceBuilderException)
	 */
	public void postError(TraceBuilderException exception) {
		String message = TraceBuilderErrorMessages.getErrorMessage(exception);
		if (message != null && message.length() > 0) {
			postErrorMessage(message, exception.getErrorSource(), exception.isEventWantedToPost());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#postErrorMessage(java.lang.String, java.lang.Object, boolean)
	 */
	public void postErrorMessage(String message, Object source, boolean postEvent) {
		
		if (postEvent) {
			postEvent(TraceEvent.ERROR, message, defaultCategory, source);
		}
		
		// If the auto-converter is running, dialogs are not shown at all
		// Otherwise they would pop up on every file save
		if (!TraceBuilderGlobals.getSourceContextManager().isConverting()) {
			// If the model is running a sequence of operations, the error
			// dialog is now shown to user. After processing is complete,
			// the dialog is shown
			if (!model.isProcessing()) {
				TraceBuilderGlobals.getDialogs().showErrorMessage(message);
			} else {
				// Stores the error to be viewed later. If there are multiple
				// errors, the individual errors are not shown as message
				// dialogs
				if (processingError == null) {
					processingError = message;
				} else {
					TraceBuilderErrorCode errorid = TraceBuilderErrorCode.MULTIPLE_ERRORS_IN_OPERATION;
					processingError = TraceBuilderErrorMessages
							.getErrorMessage(errorid, null);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#
	 *      postWarningMessage(java.lang.String, java.lang.Object)
	 */
	public void postWarningMessage(String message, Object source) {
		postEvent(TraceEvent.WARNING, message, defaultCategory, source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#
	 *      postInfoMessage(java.lang.String, java.lang.Object)
	 */
	public void postInfoMessage(String message, Object source) {
		postEvent(TraceEvent.INFO, message, defaultCategory, source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#
	 *      postAssertionFailed(java.lang.String, java.lang.Object)
	 */
	public void postAssertionFailed(String message, Object source) {
		message = "Assertion failed. Reason: " //$NON-NLS-1$
				+ message;
		postEvent(TraceEvent.ASSERT_NORMAL, message, "Normal", source); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#
	 *      postCriticalAssertionFailed(java.lang.String, java.lang.Object)
	 */
	public void postCriticalAssertionFailed(String message, Object source) {
		message = "Critical assertion failure, the project has been closed. Reason: " //$NON-NLS-1$
				+ message;
		// Closes the trace project and shows an error message
		TraceBuilderGlobals.getTraceBuilder().closeProject();
		TraceBuilderGlobals.getActions().enableActions(null);
		postEvent(TraceEvent.ASSERT_CRITICAL, message, "Critical", source); //$NON-NLS-1$
		TraceBuilderGlobals.getDialogs().showErrorMessage(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#setEventCategory(java.lang.String)
	 */
	public String setEventCategory(String category) {
		String oldCategory = this.defaultCategory;
		this.defaultCategory = category;
		return oldCategory;
	}

	/**
	 * Posts an event to the event router plug-in if it is available
	 * 
	 * @param message
	 *            event message
	 * @param category
	 *            event category
	 * @param source
	 *            event source
	 * @param type
	 *            event type
	 */
	private void postEvent(int type, String message, String category,
			Object source) {
		TraceEventRouter router = TraceEventRouter.getInstance();
		if (router != null) {
			TraceEvent event = new TraceEvent(type, message);
			event.setCategory(category);
			event.setSource(source);
			router.postEvent(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEvents#getEventCategory()
	 */
	public String getEventCategory() {
		return defaultCategory;
	}

}
