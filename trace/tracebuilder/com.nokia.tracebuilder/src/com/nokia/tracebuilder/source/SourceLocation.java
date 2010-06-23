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
* Represents a location in source
*
*/
package com.nokia.tracebuilder.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a location in source
 * 
 */
public class SourceLocation extends SourceLocationBase {

	/**
	 * Location listeners
	 */
	private ArrayList<SourceLocationListener> locationListeners;

	/**
	 * Temporary array for callback purposes. Prevents concurrent modifications
	 * if listeners are removed during a callback
	 */
	private ArrayList<SourceLocationListener> tempListeners;

	/**
	 * Offset that was used in last notification
	 */
	private int notifiedOffset;

	/**
	 * Length that was used in last notification
	 */
	private int notifiedLength;

	/**
	 * Reference count
	 */
	private int refCount = 0;

	/**
	 * Listener change flag
	 */
	private boolean listenersChanged = true;

	/**
	 * Constructor
	 * 
	 * @param parser
	 *            the parser owning this location
	 * @param offset
	 *            offset of location
	 * @param length
	 *            length of location
	 */
	public SourceLocation(SourceParser parser, int offset, int length) {
		super(parser, offset, length);
		this.notifiedOffset = offset;
		this.notifiedLength = length;
		parser.addLocation(this);
		refCount = 1;
	}

	/**
	 * Selects the source code represented by this location. This does not work
	 * if the source has not been associated with a selector.
	 */
	public void selectFromSource() {
		if (getParser() != null && !isDeleted()) {
			SourceDocumentInterface owner = getParser().getSource();
			if (owner != null) {
				SourceSelector selector = owner.getSourceSelector();
				if (selector != null) {
					selector.setSelection(getOffset(), getLength());
				}
			}
		}
	}

	/**
	 * Gets the name of the class which owns this location
	 * 
	 * @return the class name
	 */
	public String getClassName() {
		String retval = null;
		if (getParser() != null) {
			SourceContext context = getParser().getContext(getOffset());
			if (context != null) {
				retval = context.getClassName();
			}
		}
		return retval;
	}

	/**
	 * Gets the name of function which owns this location
	 * 
	 * @return the function name
	 */
	public String getFunctionName() {
		String retval = null;
		if (getParser() != null) {
			SourceContext context = getParser().getContext(getOffset());
			if (context != null) {
				retval = context.getFunctionName();
			}
		}
		return retval;
	}

	/**
	 * Adds a location listener to this location
	 * 
	 * @param listener
	 *            the location listener
	 */
	public void addLocationListener(SourceLocationListener listener) {
		if (locationListeners == null) {
			locationListeners = new ArrayList<SourceLocationListener>();
		}
		locationListeners.add(listener);
		listenersChanged = true;
	}

	/**
	 * Removes a location listener from this location
	 * 
	 * @param listener
	 *            the location listener
	 */
	public void removeLocationListener(SourceLocationListener listener) {
		if (locationListeners != null) {
			if (locationListeners.remove(listener)) {
				listenersChanged = true;
			}
		}
	}

	/**
	 * Gets the listener interfaces
	 * 
	 * @return the listeners
	 */
	protected Iterator<SourceLocationListener> getListeners() {
		List<SourceLocationListener> list;
		if (locationListeners != null) {
			if (listenersChanged) {
				listenersChanged = false;
				if (tempListeners == null) {
					tempListeners = new ArrayList<SourceLocationListener>();
				}
				tempListeners.clear();
				tempListeners.addAll(locationListeners);
			}
			list = tempListeners;
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}

	/**
	 * Fires a location deleted event to registered listeners.
	 * 
	 * @see SourceLocationListener
	 */
	void notifyLocationDeleted() {
		if (locationListeners != null) {
			Iterator<SourceLocationListener> listeners = getListeners();
			locationListeners = null;
			resetParser();
			while (listeners.hasNext()) {
				listeners.next().locationDeleted(this);
			}
		}
	}

	/**
	 * Sends locationChanged notifications to listeners if this location has
	 * changed.
	 */
	protected void notifyLocationChanged() {
		if (locationListeners != null) {
			if (notifiedOffset != getOffset() || notifiedLength != getLength()) {
				notifiedOffset = getOffset();
				notifiedLength = getLength();
				resetLineNumber();
				Iterator<SourceLocationListener> itr = locationListeners
						.iterator();
				while (itr.hasNext()) {
					itr.next().locationChanged(this);
				}
			}
		}
	}

	/**
	 * Increases the reference count of this location.
	 * 
	 * @see #dereference()
	 */
	public void reference() {
		refCount++;
	}

	/**
	 * Decrements the reference count of this location. When reference count is
	 * 0, this is removed from source. Note that a location can also be removed
	 * from source even if it has outstanding references left. In that case it
	 * can no longer be selected.
	 */
	public void dereference() {
		if (--refCount <= 0) {
			removeFromSource();
		}
	}

	/**
	 * Removes this location from the source
	 */
	private void removeFromSource() {
		delete();
		if (getParser() != null) {
			getParser().removeLocation(this);
			resetParser();
		}
		notifyLocationDeleted();
	}

}
