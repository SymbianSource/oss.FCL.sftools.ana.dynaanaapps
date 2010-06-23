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
 * Component handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.TraceComponent;
import com.nokia.trace.dictionary.model.DictionaryContentVariables.ParentDecodeObject;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Component handler
 * 
 */
final class ComponentHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private final static String COMPONENT_TAG = "component"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	ComponentHandler(DictionaryDecodeModel model) {
		super(model, COMPONENT_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.dictionary.model.handlers.DictionaryHandler#
	 * processElement(org.xml.sax.Attributes)
	 */
	@Override
	public void processStartElement(Attributes atts,
			DictionaryContentHandler handler) {
		TraceComponent component = model.getFactory().createTraceComponent(
				Integer.parseInt(atts.getValue(ID)), atts.getValue(NAME),
				atts.getValue(PREFIX), atts.getValue(SUFFIX), model);
		TraceComponent oldComponent = model.addComponent(component);

		// Check collision
		component = checkCollision(oldComponent, component, handler);

		// Set this component as the previous component and decode object
		handler.getVariables().setPreviousComponent(component);
		handler.getVariables().setParentDecodeObject(
				ParentDecodeObject.COMPONENT);

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
		// Parent decode object is set back to nothing
		handler.getVariables()
				.setParentDecodeObject(ParentDecodeObject.NOTHING);
	}

	/**
	 * Check collision
	 * 
	 * @param oldComponent
	 *            old component
	 * @param newComponent
	 *            new component
	 * @param handler
	 *            Dictionary handler
	 * @return component to be set as previous component
	 */
	private TraceComponent checkCollision(TraceComponent oldComponent,
			TraceComponent newComponent, DictionaryContentHandler handler) {
		boolean collision = true;
		// If old component is null, everyrything is ok. If not, check collision
		if (oldComponent != null) {
			// UIDs must be same
			if (oldComponent.getId() == newComponent.getId()) {
				collision = false;
			}
			if (collision) {
				newComponent = oldComponent;
				TraceEvent event = new TraceEvent(TraceEvent.WARNING, Messages
						.getString("ComponentHandler.CollisionComponentID") //$NON-NLS-1$
						+ Integer.toHexString(oldComponent.getId()));
				event.setCategory(EVENT_CATEGORY);
				event.setSource(Integer.valueOf(handler.getLocator()
						.getLineNumber()));
				TraceDictionaryEngine.postEvent(event);
			}
		}
		return newComponent;
	}

}
