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
* Implementation of the property interface
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import com.nokia.tracebuilder.model.TraceObjectProperty;

/**
 * Implementation of the property interface
 * 
 */
class TraceObjectPropertyImpl implements TraceObjectProperty {

	/**
	 * Property name
	 */
	private String name;

	/**
	 * Property value
	 */
	private String value;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 */
	TraceObjectPropertyImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectProperty#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectProperty#getValue()
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String separator = Messages
				.getString("TraceObjectPropertyImpl.NameValueSeparator"); //$NON-NLS-1$
		return name + separator + value;
	}

}
