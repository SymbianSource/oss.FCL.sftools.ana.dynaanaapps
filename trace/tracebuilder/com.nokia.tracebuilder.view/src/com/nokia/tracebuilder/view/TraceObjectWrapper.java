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
* Base class for tree view elements containing a TraceObject
*
*/
package com.nokia.tracebuilder.view;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Base class for tree view elements containing a TraceObject
 * 
 * @see com.nokia.tracebuilder.model.TraceObject
 */
abstract class TraceObjectWrapper extends ListWrapper implements
		TraceModelExtension {

	/**
	 * The trace object
	 */
	private TraceObject object;

	/**
	 * ID property
	 */
	protected PropertyWrapper id;

	/**
	 * Value property
	 */
	protected PropertyWrapper value;

	/**
	 * Extensions that are not visible
	 */
	private ArrayList<TraceViewExtensionWrapper> hiddenExtensions;

	/**
	 * Constructor adds property list, parameter list and extensions to the list
	 * of sub-wrappers.
	 * 
	 * @param object
	 *            the trace object
	 * @param parent
	 *            the parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceObjectWrapper(TraceObject object, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.object = object;
		// This wrapper is stored into the trace object for quick access
		object.addExtension(this);
		addExtensions();
		addProperties();
	}

	/**
	 * Adds an extension
	 * 
	 * @param extension
	 *            the extension to be added
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addExtension(TraceViewExtension extension) {
		TraceViewExtensionWrapper wrapper = new TraceViewExtensionWrapper(
				extension, this, getUpdater());
		if (extension.hasChildren() || !extension.hideWhenEmpty()) {
			add(wrapper);
		} else {
			if (hiddenExtensions == null) {
				hiddenExtensions = new ArrayList<TraceViewExtensionWrapper>();
			}
			hiddenExtensions.add(wrapper);
		}
		return this;
	}

	/**
	 * Removes an extension.
	 * 
	 * @param extension
	 *            the extension to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeExtension(TraceViewExtension extension) {
		TraceViewExtensionWrapper wrapper = (TraceViewExtensionWrapper) extension
				.getViewReference();
		remove(wrapper);
		if (hiddenExtensions != null) {
			hiddenExtensions.remove(wrapper);
		}
		return this;
	}

	/**
	 * Gets the wrapped trace object
	 * 
	 * @return the trace object
	 */
	TraceObject getTraceObject() {
		return object;
	}

	/**
	 * Refreshes the value of given property
	 * 
	 * @param property
	 *            the property type
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase refreshProperty(int property) {
		WrapperBase retval;
		if (object instanceof Trace) {
			retval = refreshTraceProperty(property);
		} else if (object instanceof TraceModel) {
			retval = refreshModelProperty(property);
		} else {
			retval = this;
		}
		return retval;
	}

	/**
	 * Refreshes a trace model property
	 * 
	 * @param property
	 *            the property to be refreshed
	 * @return the wrapper that was affected
	 */
	private WrapperBase refreshModelProperty(int property) {
		WrapperBase retval = null;
		if (property == TraceModelListener.ID) {
			retval = updateID();
		} else if (property == TraceModelListener.NAME) {
			value.setProperty(((TraceModel) object).getName());
			retval = value;
		}
		return retval;
	}

	/**
	 * Refreshes a trace property
	 * 
	 * @param property
	 *            the property to be refreshed
	 * @return the wrapper that was affected
	 */
	private WrapperBase refreshTraceProperty(int property) {
		WrapperBase retval = null;
		if (property == TraceModelListener.TRACE) {
			value.setProperty(((Trace) object).getTrace());
			retval = value;
		}
		return retval;
	}

	/**
	 * Updates the ID property
	 * 
	 * @return the ID property wrapper
	 */
	private WrapperBase updateID() {
		id.setProperty(Messages.getString("TraceObjectWrapper.HexPrefix") //$NON-NLS-1$
				+ Integer.toHexString(object.getID())
				+ Messages.getString("TraceObjectWrapper.HexPostfix")); //$NON-NLS-1$
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#delete()
	 */
	@Override
	void delete() {
		object.removeExtension(this);
		object = null;
		if (hiddenExtensions != null) {
			Iterator<TraceViewExtensionWrapper> itr = hiddenExtensions
					.iterator();
			while (itr.hasNext()) {
				itr.next().delete();
			}
		}
		super.delete();
	}

	/**
	 * Extension update hides the extension if it becomes empty and has the
	 * hideWhenEmpty flag. Also shows if the extension is no longer empty.
	 * 
	 * @param extension
	 *            the extension
	 * @return the wrapper that needs to be refreshed
	 */
	WrapperBase updateExtension(TraceViewExtension extension) {
		TraceViewExtensionWrapper wrapper = (TraceViewExtensionWrapper) extension
				.getViewReference();
		WrapperBase retval = null;
		if (contains(wrapper)) {
			if (!extension.hasChildren() && extension.hideWhenEmpty()) {
				hide(wrapper);
				if (hiddenExtensions == null) {
					hiddenExtensions = new ArrayList<TraceViewExtensionWrapper>();
				}
				hiddenExtensions.add(wrapper);
				retval = this;
			}
		} else {
			if (extension.hasChildren() || !extension.hideWhenEmpty()) {
				if (hiddenExtensions != null) {
					hiddenExtensions.remove(wrapper);
				}
				add(wrapper);
				retval = this;
			}
		}
		if (retval == null) {
			retval = wrapper;
		}
		return retval;
	}

	/**
	 * Hides this object from the view
	 * 
	 * @return the object that needs to be refreshed
	 */
	WrapperBase hideFromView() {
		TraceObjectListWrapper parent = (TraceObjectListWrapper) getParent();
		parent.hide(this);
		WrapperBase retval;
		if (parent.hasChildren()) {
			retval = parent;
		} else {
			retval = parent.hideFromView();
		}
		return retval;
	}

	/**
	 * Adds the extensions to the list
	 */
	void addExtensions() {
		// Extensions are added directly under the object
		Iterator<TraceViewExtension> itr = object
				.getExtensions(TraceViewExtension.class);
		while (itr.hasNext()) {
			addExtension(itr.next());
		}
	}

	/**
	 * Creates and adds the properties
	 */
	void addProperties() {
		if (object instanceof Trace) {
			value = new PropertyWrapper(Messages
					.getString("TraceObjectWrapper.Trace"), //$NON-NLS-1$
					((Trace) object).getTrace(), this, getUpdater());
			add(value);
		} else if (object instanceof TraceModel) {
			// Property is not needed, because component id's will be read from mmp files
			id = new PropertyWrapper(Messages
					.getString("TraceObjectWrapper.ModelID"), //$NON-NLS-1$
					"", this, getUpdater()); //$NON-NLS-1$
			add(id);
			// Property is not needed, because component names will be read from mmp files
			value = new PropertyWrapper(Messages
					.getString("TraceObjectWrapper.ModelName"), //$NON-NLS-1$
					"", this, getUpdater()); //$NON-NLS-1$
			add(value);
		}
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

}