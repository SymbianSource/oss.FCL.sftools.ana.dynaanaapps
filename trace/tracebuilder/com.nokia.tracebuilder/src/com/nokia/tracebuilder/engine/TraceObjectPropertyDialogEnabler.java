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
* Interface to disable property dialog editors
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface which can be implemented by a trace object rule to prevent object
 * modification with a property dialog. If this interface is not present, all
 * properties can be edited.
 * 
 */
public interface TraceObjectPropertyDialogEnabler extends TraceModelExtension {

	/**
	 * Checks if target field is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isTargetEnabled();
	
	/**
	 * Checks if ID field is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isIdEnabled();

	/**
	 * Checks if name field is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isNameEnabled();

	/**
	 * Checks if value field is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isValueEnabled();

	/**
	 * Checks if type selector is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isTypeEnabled();

}
