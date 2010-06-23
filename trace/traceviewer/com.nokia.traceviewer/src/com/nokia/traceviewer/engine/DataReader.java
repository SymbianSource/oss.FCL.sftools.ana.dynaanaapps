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
 * DataReader interface
 *
 */
package com.nokia.traceviewer.engine;

/**
 * DataReader interface
 */
public interface DataReader {

	/**
	 * Clears file
	 */
	public void clearFile();

	/**
	 * Creates scroll reader
	 * 
	 * @param mediaCallback
	 *            callback
	 * @param conf
	 *            TraceConfiguration to use with this scroll reader
	 * @return new scroll reader
	 */
	public DataScrollReader createScrollReader(MediaCallback mediaCallback,
			TraceConfiguration conf);

	/**
	 * Gets file map
	 * 
	 * @return file map
	 */
	public FileMap getFileMap();

	/**
	 * Gets file path
	 * 
	 * @return file path
	 */
	public String getFilePath();

	/**
	 * Gets file start offset
	 * 
	 * @return file start offset
	 */
	public long getFileStartOffset();

	/**
	 * Gets trace count
	 * 
	 * @return trace count
	 */
	public int getTraceCount();

	/**
	 * Gets current trace position in file
	 * 
	 * @return current trace position in file
	 */
	public long getTracePositionInFile();

	/**
	 * Gets trace configuration
	 * 
	 * @return trace configuration
	 */
	public TraceConfiguration getTraceConfiguration();

	/**
	 * Tells if this reader is paused
	 * 
	 * @return pause status
	 */
	public boolean isPaused();

	/**
	 * Pauses the reader
	 * 
	 * @param pause
	 *            pause status
	 */
	public void pause(boolean pause);

	/**
	 * Sets file path
	 * 
	 * @param filePath
	 *            file path
	 */
	public void setFilePath(String filePath);

	/**
	 * Sets file position
	 * 
	 * @param filePos
	 *            the new file position
	 */
	public void setFilePosition(long filePos);

	/**
	 * Sets file start offset
	 * 
	 * @param fileStartOffset
	 *            file start offset
	 */
	public void setFileStartOffset(long fileStartOffset);

	/**
	 * Shuts down this DataReader
	 */
	public void shutdown();

	/**
	 * Read data from file
	 */
	public void start();
}
