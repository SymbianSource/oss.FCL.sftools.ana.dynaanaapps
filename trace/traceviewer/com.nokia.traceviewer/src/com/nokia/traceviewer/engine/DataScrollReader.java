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
 * DataScrollReader interface
 *
 */
package com.nokia.traceviewer.engine;

/**
 * DataScrollReader interface
 * 
 */
public interface DataScrollReader {

	/**
	 * Read data from file
	 */
	public void start();

	/**
	 * Pauses the reader
	 * 
	 * @param pause
	 *            pause status
	 */
	public void pause(boolean pause);

	/**
	 * Shuts down this DataReader
	 */
	public void shutdown();

	/**
	 * Sets this DataReader to read block(s)
	 * 
	 * @param numberOfBlocks
	 *            number of blocks to read
	 * @param startingTrace
	 *            number of first trace to read
	 * @param blocking
	 *            should this reader be blocking. Caller thread is waiting until
	 *            all data is gathered
	 */
	public void setBlockReader(int numberOfBlocks, int startingTrace,
			boolean blocking);

	/**
	 * Sets file position
	 * 
	 * @param filePos
	 *            file position
	 */
	public void setFilePosition(long filePos);
}
