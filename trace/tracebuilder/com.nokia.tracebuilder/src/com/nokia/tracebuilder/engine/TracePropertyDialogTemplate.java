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
 * Interface to a template for the Trace property dialog.
 * 
 */
public interface TracePropertyDialogTemplate extends
		TraceObjectPropertyDialogTemplate {

	/**
	 * Gets the preferred target group for new traces based on this template
	 * 
	 * @return the target group name
	 */
	public String getGroupName();

	/**
	 * Gets a new trace text value. Called when the target group is changed in
	 * the property dialog
	 * 
	 * @param groupName
	 *            the target group name
	 * @return the trace text
	 */
	public String getText(String groupName);

}
