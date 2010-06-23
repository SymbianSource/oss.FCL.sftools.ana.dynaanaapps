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
* Interface which can be added to a TraceObject to prevent parameter addition
*
*/
package com.nokia.tracebuilder.rules;

import com.nokia.tracebuilder.model.TraceObjectRule;

/**
 * Interface which can be used to restrict changes to the parameters of trace
 * objects
 * 
 */
public interface TraceParameterRestrictionRule extends TraceObjectRule {

	/**
	 * Called before showing add parameter dialog to user. If this returns
	 * false, an error message is displayed
	 * 
	 * @return true if the dialog can be shown, false if an error message needs
	 *         to be displayed
	 */
	public boolean canAddParameters();

	/**
	 * Called before removing a parameter. If this returns false, the parameter
	 * is not removed and error message is displayed
	 * 
	 * @return true if the dialog can be shown, false if an error message needs
	 *         to be displayed
	 */
	public boolean canRemoveParameters();

}
