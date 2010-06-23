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
* Updater interface for trace model wrappers
*
*/
package com.nokia.tracebuilder.view;

/**
 * Updater interface for trace model wrappers
 * 
 */
interface WrapperUpdater {

	/**
	 * Updates the given wrapper
	 * 
	 * @param wrapper
	 *            the wrapper to be updated
	 */
	void update(WrapperBase wrapper);

	/**
	 * Queues an asynchronous update
	 * 
	 * @param wrapper
	 *            the wrapper to be updated
	 */
	void queueUpdate(WrapperBase wrapper);

	/**
	 * Queues a selection action. Selection is done after all updates and only
	 * the latest selection is effective.
	 * 
	 * @param wrapper
	 *            the wrapper to be selected
	 */
	void queueSelection(WrapperBase wrapper);

}
