/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
*/
package com.nokia.s60tools.memspy.export;

/**
 * Callback interface reporting trace related error information from trace source side.
 */
public interface ITraceClientNotificationsIf {
	
	/**
	 * Notifies informative message from trace source.
	 * @param message message
	 */
	public void notifyInformation(String message);
	
	/**
	 * Notifies warning from trace source.
	 * @param message message
	 */
	public void notifyWarning(String message);
	
	/**
	 * Notifies error from trace source.
	 * @param message message
	 */
	public void notifyError(String message);
}
