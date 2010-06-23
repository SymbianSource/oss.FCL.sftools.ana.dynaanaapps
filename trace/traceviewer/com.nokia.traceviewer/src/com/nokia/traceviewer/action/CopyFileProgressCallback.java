/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Notifies about copy file progress in bytes
 *
 */
package com.nokia.traceviewer.action;

/**
 * Notifies about copy file progress in bytes
 * 
 */
public interface CopyFileProgressCallback {

	/**
	 * Notifies about file position when copying
	 * 
	 * @param filePosition
	 *            file position in bytes
	 */
	void notifyFilePosition(long filePosition);

	/**
	 * If true, copying should be canceled
	 * 
	 * @return true if copying should be canceled
	 */
	boolean cancelCopying();

	/**
	 * Inform callback that copying is finished
	 */
	void copyingFinished();

}
