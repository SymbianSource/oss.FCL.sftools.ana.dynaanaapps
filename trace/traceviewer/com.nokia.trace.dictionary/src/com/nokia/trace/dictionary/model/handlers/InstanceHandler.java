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
 * Instance handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryContentVariables;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.Location;
import com.nokia.trace.dictionary.model.Trace;
import com.nokia.trace.dictionary.model.TraceData;
import com.nokia.trace.dictionary.model.TraceGroup;
import com.nokia.trace.dictionary.model.DictionaryContentVariables.ParentDecodeObject;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Instance handler
 */
final class InstanceHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String INSTANCE_TAG = "instance"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	InstanceHandler(DictionaryDecodeModel model) {
		super(model, INSTANCE_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processStartElement
	 * (org.xml.sax.Attributes,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		DictionaryContentVariables variables = handler.getVariables();
		// Get parent group
		TraceGroup parentGroup = variables.getPreviousGroup();
		// Get this traces data-ref value
		int dataRef = variables.getPreviousTraceDataRef();
		// Get this traces name
		String traceName = variables.getPreviousTraceName();
		// Get trace data for this data-ref value
		TraceData traceData = model.getTraceData(dataRef);
		// Get location for this instance
		Location location = model.getLocation(Integer.parseInt(atts
				.getValue(LOCREF)));

		// Create new trace instance
		Trace trace = model.getFactory().createTrace(
				Integer.parseInt(atts.getValue(ID)), traceName, traceData,
				location, Integer.parseInt(atts.getValue(LINE)),
				atts.getValue(METHODNAME), atts.getValue(CLASSNAME),
				parentGroup);

		// Add trace to the group
		Trace oldTrace = parentGroup.addTrace(trace);

		// Check collision
		trace = checkCollision(oldTrace, trace, handler);

		// Set this trace to be the previous trace instance and decode object
		variables.setPreviousTrace(trace);
		variables.setParentDecodeObject(ParentDecodeObject.TRACEINSTANCE);

		// Add trace to the trace instance list
		if (oldTrace != trace) {
			variables.getTraceInstanceList().addTrace(trace);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.handlers.BaseHandler#processEndElement
	 * (java.lang.StringBuffer, java.lang.Object,
	 * com.nokia.trace.dictionary.model.DictionaryContentHandler,
	 * com.nokia.trace.dictionary.model.DecodeObject)
	 */
	@Override
	public void processEndElement(StringBuffer elementContent,
			Object unFinishedObject, DictionaryContentHandler handler,
			DecodeObject parentObject) {
		// Parent decode object is set back to trace
		handler.getVariables().setParentDecodeObject(ParentDecodeObject.TRACE);
	}

	/**
	 * Check collision
	 * 
	 * @param oldTrace
	 *            old trace
	 * @param newTrace
	 *            new trace
	 * @param handler
	 *            dictionary content handler
	 * @return the trace which caused the collision or null if everything is OK
	 */
	private Trace checkCollision(Trace oldTrace, Trace newTrace,
			DictionaryContentHandler handler) {
		boolean collision = true;
		// If old trace is null, everything is ok. If not, check collision
		if (oldTrace != null) {
			// Trace data must be the same
			if (oldTrace.getTraceData() == newTrace.getTraceData()) {
				// Locations must be same
				if (oldTrace.getLocation() == newTrace.getLocation()) {
					// Line numbers must be same
					if (oldTrace.getLineNumber() == newTrace.getLineNumber()) {
						// Class names must be same
						String className = oldTrace.getClassName();
						String className2 = newTrace.getClassName();
						if (className == null) {
							className = ""; //$NON-NLS-1$
						}
						if (className2 == null) {
							className2 = ""; //$NON-NLS-1$
						}
						if (className.equals(className2)) {
							// Method names must be same
							String methodName = oldTrace.getMethodName();
							String methodName2 = newTrace.getMethodName();
							if (methodName == null) {
								methodName = ""; //$NON-NLS-1$
							}
							if (methodName2 == null) {
								methodName2 = ""; //$NON-NLS-1$
							}
							if (methodName.equals(methodName2)) {
								collision = false;
							}
						}
					}
				}
			}
			if (collision) {
				newTrace = oldTrace;
				TraceEvent event = new TraceEvent(TraceEvent.WARNING, Messages
						.getString("InstanceHandler.CollisionTraceID") //$NON-NLS-1$
						+ oldTrace.getId());
				event.setCategory(EVENT_CATEGORY);
				event.setSource(Integer.valueOf(handler.getLocator()
						.getLineNumber()));
				TraceDictionaryEngine.postEvent(event);
			}
		}
		return newTrace;
	}

}
