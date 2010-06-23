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
* Interface to a template for the property dialog
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface to a template for the property dialog. Templates contain predefined
 * values for property dialogs.
 * 
 */
public interface TraceObjectPropertyDialogTemplate {

	/**
	 * Gets the title of the template
	 * 
	 * @return the title
	 */
	public String getTitle();

	/**
	 * Gets the object name
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the object value
	 * 
	 * @return the value
	 */
	public String getValue();

	/**
	 * Checks if the target can be changed after it has been set to the value
	 * specified by this template
	 * 
	 * @return true if target can be changed, false if not
	 */
	public boolean isTargetEnabled();	
	
	/**
	 * Checks if the ID can be changed after it has been set to the value
	 * specified by this template
	 * 
	 * @return true if ID can be changed, false if not
	 */
	public boolean isIDEnabled();

	/**
	 * Checks if the name can be changed after it has been set to the value
	 * specified by this template
	 * 
	 * @return true if name can be changed, false if not
	 */
	public boolean isNameEnabled();

	/**
	 * Checks if the value can be changed after it has been set to the value
	 * specified by this template
	 * 
	 * @return true if value can be changed, false if not
	 */
	public boolean isValueEnabled();

	/**
	 * Creates the extensions provided by this template and adds them to the
	 * given list. The extensions are passed to the creation function of object
	 * factory.
	 * 
	 * @param list
	 *            the list where extension are added
	 */
	public void createExtensions(List<TraceModelExtension> list);

}
