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
* Base class for message console
*
*/
package com.nokia.tracebuilder.utils;

import java.io.OutputStream;

/**
 * Base class for message console
 * 
 */
public abstract class ConsoleBase {

	/**
	 * Console title
	 */
	private String title;

	/**
	 * Constructor
	 */
	protected ConsoleBase() {
	}

	/**
	 * Creates a stream that writes to the console
	 * 
	 * @return the stream
	 */
	protected abstract OutputStream createStream();

	/**
	 * Gets the console title
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the console title
	 * 
	 * @param title
	 *            the title to set
	 */
	void setTitle(String title) {
		this.title = title;
	}

}
