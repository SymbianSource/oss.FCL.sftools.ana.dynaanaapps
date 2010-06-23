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
 * Internal Trace File Handler
 *
 */
package com.nokia.traceviewer.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Internal Trace File Handler
 * 
 */
final class InternalFileHandler implements TraceFileHandler {

	/**
	 * File path
	 */
	private final String filePath;

	/**
	 * Trace File
	 */
	private RandomAccessFile file;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            trace file path
	 */
	public InternalFileHandler(String filePath) {
		this.filePath = filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#clearFile()
	 */
	public void clearFile() {
		if (file != null) {
			try {
				file.setLength(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#openFile()
	 */
	public void openFile() {
		if (filePath != null) {
			// Create new RandomAccessFile
			try {
				if (getChannel() == null || !getChannel().isOpen()) {
					this.file = new RandomAccessFile(filePath, "rw"); //$NON-NLS-1$
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceFileHandler#closeFile()
	 */
	public void closeFile() {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets file channel
	 * 
	 * @return file channel
	 */
	private FileChannel getChannel() {
		FileChannel channel = null;
		if (file != null) {
			channel = file.getChannel();
		}
		return channel;
	}
}
