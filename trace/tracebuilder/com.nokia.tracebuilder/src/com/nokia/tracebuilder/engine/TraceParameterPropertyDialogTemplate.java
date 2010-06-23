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
* Interface to a template for the property dialog
*
*/
package com.nokia.tracebuilder.engine;

/**
 * Interface to a template for the TraceParameter property dialog.
 * 
 */
public interface TraceParameterPropertyDialogTemplate extends
		TraceObjectPropertyDialogTemplate {

	/**
	 * Gets the parameter type
	 * 
	 * @return the type
	 */
	public String getType();

	/**
	 * Checks if the type can be changed after it has been set to the value
	 * specified by this template
	 * 
	 * @return true if type can be changed, false if not
	 */
	public boolean isTypeEnabled();

}
