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
 * OST Writer
 *
 */
package com.nokia.traceviewer.ost;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import com.nokia.traceviewer.engine.DataWriter;

/**
 * OST Writer
 * 
 */
public class OstWriter implements DataWriter {

	/**
	 * Target channel for write operations
	 */
	private ByteChannel writeChannel;

	/**
	 * Constructor
	 * 
	 * @param writeChannel
	 *            write channel
	 */
	public OstWriter(ByteChannel writeChannel) {
		this.writeChannel = writeChannel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataWriter#writeMessage(java.nio.ByteBuffer,
	 * int, int)
	 */
	public void writeMessage(ByteBuffer sourceBuffer, int msgStart, int msgLen) {
		try {
			// Write the message to file
			if (sourceBuffer != null && writeChannel != null) {
				int position = sourceBuffer.position();
				int limit = sourceBuffer.limit();
				sourceBuffer.limit(msgStart + msgLen);
				sourceBuffer.position(msgStart);
				writeChannel.write(sourceBuffer);
				sourceBuffer.limit(limit);
				sourceBuffer.position(position);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataWriter#close()
	 */
	public void closeChannel() {
		try {
			if (writeChannel != null) {
				writeChannel.close();
				writeChannel = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
