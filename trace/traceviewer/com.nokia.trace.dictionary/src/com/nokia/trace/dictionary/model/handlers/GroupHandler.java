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
 * Group handler
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import org.xml.sax.Attributes;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.DecodeObject;
import com.nokia.trace.dictionary.model.DictionaryContentHandler;
import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.TraceComponent;
import com.nokia.trace.dictionary.model.TraceGroup;
import com.nokia.trace.dictionary.model.DictionaryContentVariables.ParentDecodeObject;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Group handler
 */
final class GroupHandler extends BaseHandler {

	/**
	 * Tag name this handler handles
	 */
	private static final String GROUP_TAG = "group"; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	GroupHandler(DictionaryDecodeModel model) {
		super(model, GROUP_TAG);
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

		// Get parent component
		TraceComponent parentComponent = handler.getVariables()
				.getPreviousComponent();

		// Create new group
		TraceGroup group = model.getFactory().createTraceGroup(
				Integer.parseInt(atts.getValue(ID)), atts.getValue(NAME),
				atts.getValue(PREFIX), atts.getValue(SUFFIX), parentComponent);

		// Add group to the component
		TraceGroup oldGroup = parentComponent.addGroup(group);

		// Check collision
		group = checkCollision(oldGroup, group, handler);

		// Set this group as the previous group and decode object
		handler.getVariables().setPreviousGroup(group);
		handler.getVariables().setParentDecodeObject(ParentDecodeObject.GROUP);
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
		// Parent decode object is set back to component
		handler.getVariables().setParentDecodeObject(
				ParentDecodeObject.COMPONENT);
	}

	/**
	 * Check collision
	 * 
	 * @param oldGroup
	 *            old group
	 * @param newGroup
	 *            new group
	 * @param handler
	 *            dictionary content handler
	 * @return old group if there was collision, new group if not
	 */
	private TraceGroup checkCollision(TraceGroup oldGroup, TraceGroup newGroup,
			DictionaryContentHandler handler) {
		boolean collision = true;
		// If old group is null, everything is OK. If not, check collision
		if (oldGroup != null) {
			// Classes must be same
			if (oldGroup.getClass() == newGroup.getClass()) {
				// Names must be same
				if (oldGroup.getName().equals(newGroup.getName())) {
					// Prefixes must be same
					String prefix = oldGroup.getPrefix();
					String prefix2 = newGroup.getPrefix();
					if (prefix == null) {
						prefix = ""; //$NON-NLS-1$
					}
					if (prefix2 == null) {
						prefix2 = ""; //$NON-NLS-1$
					}
					if (prefix.equals(prefix2)) {
						// Suffixes must be same
						String suffix = oldGroup.getSuffix();
						String suffix2 = newGroup.getSuffix();
						if (suffix == null) {
							suffix = ""; //$NON-NLS-1$
						}
						if (suffix2 == null) {
							suffix2 = ""; //$NON-NLS-1$
						}
						if (suffix.equals(suffix2)) {
							collision = false;
						}
					}
				}
			}
			if (collision) {
				newGroup = oldGroup;
				TraceEvent event = new TraceEvent(TraceEvent.WARNING, Messages
						.getString("GroupHandler.CollisionGroupID") //$NON-NLS-1$
						+ Integer.toHexString(oldGroup.getId()));
				event.setCategory(EVENT_CATEGORY);
				event.setSource(Integer.valueOf(handler.getLocator()
						.getLineNumber()));
				TraceDictionaryEngine.postEvent(event);
			}
		}
		return newGroup;
	}

}
