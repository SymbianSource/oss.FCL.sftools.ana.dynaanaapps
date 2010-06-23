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
* List of properties
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.engine.rules.ExtensionBase;
import com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener;
import com.nokia.tracebuilder.model.TraceObjectProperty;
import com.nokia.tracebuilder.model.TraceObjectPropertyList;

/**
 * List of properties
 * 
 */
final class TraceObjectPropertyListImpl extends ExtensionBase implements
		TraceViewExtension, TraceObjectPropertyList {

	/**
	 * Property map
	 */
	private HashMap<String, TraceObjectProperty> properties = new HashMap<String, TraceObjectProperty>();

	/**
	 * Update listeners
	 */
	private ArrayList<TraceModelExtensionUpdateListener> updateListeners = new ArrayList<TraceModelExtensionUpdateListener>();

	/**
	 * View reference
	 */
	private Object viewReference;

	/**
	 * Adds a property to this list
	 * 
	 * @param property
	 *            the property
	 */
	void addProperty(TraceObjectProperty property) {
		properties.put(property.getName(), property);
		for (TraceModelExtensionUpdateListener listener : updateListeners) {
			listener.extensionUpdated();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyList#getProperty(java.lang.String)
	 */
	public TraceObjectProperty getProperty(String name) {
		return properties.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<TraceObjectProperty> iterator() {
		return properties.values().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getChildren()
	 */
	public Iterator<?> getChildren() {
		return properties.values().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getViewReference()
	 */
	public Object getViewReference() {
		return viewReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hasChildren()
	 */
	public boolean hasChildren() {
		return !properties.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hideWhenEmpty()
	 */
	public boolean hideWhenEmpty() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#setViewReference(java.lang.Object)
	 */
	public void setViewReference(Object reference) {
		this.viewReference = reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      addUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void addUpdateListener(TraceModelExtensionUpdateListener listener) {
		updateListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      removeUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void removeUpdateListener(TraceModelExtensionUpdateListener listener) {
		updateListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Messages.getString("TraceObjectPropertyList.Title"); //$NON-NLS-1$
	}

}
