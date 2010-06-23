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
* UI for property dialogs
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * Wrapper for a trace object property
 * 
 */
final class PropertyWrapper extends WrapperBase {

	/**
	 * The property
	 */
	private String property;

	/**
	 * Type of the property
	 */
	private String type;

	/**
	 * Creates a new property wrapper
	 * 
	 * @param type
	 *            the property type
	 * @param property
	 *            the property
	 * @param parent
	 *            tree view parent
	 * @param updater
	 *            the update notifier
	 */
	PropertyWrapper(String type, String property, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.type = type;
		this.property = property;
	}

	/**
	 * Sets the property
	 * 
	 * @param property
	 *            the new property
	 */
	void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Sets the type
	 * 
	 * @param type
	 *            the type
	 */
	void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#getChildren()
	 */
	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperBase#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Returns the type. This never returns null
	 * 
	 * @return the property type
	 */
	String getType() {
		String ret;
		if (type == null) {
			ret = ""; //$NON-NLS-1$
		} else {
			ret = type;
		}
		return ret;
	}

	/**
	 * Returns the property. This never returns null
	 * 
	 * @return the property
	 */
	String getProperty() {
		String ret;

		if (type.equals(Messages.getString("TraceObjectWrapper.ModelID"))) { //$NON-NLS-1$
			if (property == null || property.length() == 0
					|| property.equals("0")) { //$NON-NLS-1$
				if (TraceBuilderGlobals.getTraceModel().getName() == null
						|| TraceBuilderGlobals.getTraceModel().getName()
								.length() == 0) {
					// TraceBuilder project is not open, show None as Project ID
					// value.
					String noProperty = Messages
							.getString("PropertyWrapper.NoProperty"); //$NON-NLS-1$
					ret = noProperty;
				} else {
					// If TraceBuilder project is open, but Project ID is, show
					// Not available as Project ID value.
					String notAvailable = Messages
							.getString("PropertyWrapper.NotAvailable"); //$NON-NLS-1$
					ret = notAvailable;
				}

			} else {
				ret = property;
			}
		} else {
			if (property == null || property.length() == 0) {
				ret = Messages.getString("PropertyWrapper.NoProperty"); //$NON-NLS-1$
			} else {
				ret = property;
			}
		}

		return ret;
	}
}