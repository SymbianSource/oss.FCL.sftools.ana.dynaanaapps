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
* Configuration listener interface
*
*/
package com.nokia.tracebuilder.engine;

/**
 * Configuration listener interface
 * 
 */
public interface TraceBuilderConfigurationListener {

	/**
	 * Configuration creation notification
	 */
	public void configurationCreated();

	/**
	 * Change notification in configuration
	 * 
	 * @param property
	 *            the property that was changed
	 * @param newValue
	 *            the new value
	 */
	public void configurationChanged(String property, Object newValue);

}
