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
 * Data Writer interface
 *
 */
package com.nokia.traceviewer.engine;

import java.nio.ByteBuffer;

/**
 * Data Writer Interface
 */
public interface DataWriter {

	/**
	 * Writes message
	 * 
	 * @param sourceBuffer
	 *            sourceBuffer where to write the message
	 * @param msgStart
	 *            message start offset
	 * @param msgLen
	 *            message length
	 */
	public void writeMessage(ByteBuffer sourceBuffer, int msgStart, int msgLen);

	/**
	 * Closes writing
	 */
	public void closeChannel();
}
