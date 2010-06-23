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
* Event list entry, which contains a reference to TraceObject
*
*/
package com.nokia.tracebuilder.eventhandler;

import org.eclipse.jface.action.IMenuManager;

import com.nokia.trace.eventview.EventListEntryString;
import com.nokia.tracebuilder.action.TraceViewActions;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceObjectRuleOnDelete;

/**
 * Event list entry, which contains a reference to TraceObject. This implements
 * OnDelete so the event gets removed from the list when the actual object is
 * removed.
 * 
 */
class EventListEntryTraceObject extends EventListEntryString implements
		TraceObjectRuleOnDelete {

	/**
	 * Event object
	 */
	private TraceObject object;

	/**
	 * Object name
	 */
	private String objectName;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            event type
	 * @param description
	 *            event description
	 * @param object
	 *            the event object
	 */
	EventListEntryTraceObject(int type, String description, TraceObject object) {
		// getSourceName is overridden in this class -> Pass null to superclass
		super(type, description, null);
		this.object = object;
		object.addExtension(this);
	}

	/**
	 * Gets the object
	 * 
	 * @return the object
	 */
	TraceObject getObject() {
		return object;
	}

	/**
	 * Links the event to a new object. The existing object must be null and
	 * have the same name as the new object
	 * 
	 * @param object
	 *            the new object
	 */
	void setObject(TraceObject object) {
		this.object = object;
		object.addExtension(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSourceActions()
	 */
	@Override
	protected boolean hasSourceActions() {
		return object != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#hasSource()
	 */
	@Override
	protected boolean hasSource() {
		return object != null || objectName != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSource()
	 */
	@Override
	protected Object getSource() {
		return object != null ? object : objectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#
	 *      addSourceActions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void addSourceActions(IMenuManager manager) {
		if (object != null) {
			((TraceViewActions) TraceBuilderGlobals.getActions())
					.fillContextMenu(manager);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectRuleOnDelete#objectDeleted()
	 */
	public void objectDeleted() {
		// Removes the object reference
		// -> Menu actions will no longer be available
		if (object != null) {
			objectName = object.getName();
		}
		object = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
		// Already set in constructor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntry#reset()
	 */
	@Override
	protected void reset() {
		if (object != null) {
			object.removeExtension(this);
		}
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.trace.eventview.EventListEntryString#getSourceName()
	 */
	@Override
	protected String getSourceName() {
		return object != null ? object.getName() : objectName;
	}

}
