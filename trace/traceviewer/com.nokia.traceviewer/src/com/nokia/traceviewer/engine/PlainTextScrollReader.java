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
 * PlainTextScrollReader class
 *
 */
package com.nokia.traceviewer.engine;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * PlainText Scroll Reader class
 * 
 */
public class PlainTextScrollReader extends BaseDataReader implements
		DataScrollReader {

	/**
	 * Running boolean
	 */
	private boolean running;

	/**
	 * Number of blocks to process when reader is block-reader
	 */
	private int numberOfBlocks = 1;

	/**
	 * Starting offset when reader is block-reader
	 */
	private int startReadingTracesFrom;

	/**
	 * Number of traces read when reader is block-reader
	 */
	private int numberOfTracesRead;

	/**
	 * Indicates that not all traces can be fit in one buffer
	 */
	private boolean tracesDontFitInBuffer;

	/**
	 * Is this reader blocking
	 */
	private boolean blocking;

	/**
	 * Possible middle of line break
	 */
	private boolean middleOfLineBreak;

	/**
	 * Constructor
	 * 
	 * @param mediaCallback
	 *            callback
	 * @param configuration
	 *            trace configuration used in this reader
	 */
	public PlainTextScrollReader(MediaCallback mediaCallback,
			TraceConfiguration configuration) {
		filePath = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getFilePath();
		createFileChannel();
		this.mediaCallback = mediaCallback;
		trace = new TraceProperties(configuration);
		traceConfiguration = configuration;

		receiveBuffer = ByteBuffer.allocateDirect(RECEIVE_BUFFER_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public synchronized void run() {
		try {
			running = true;
			receiveBuffer.position(0);
			while (running) {

				// Set buffer limit
				if (!tracesDontFitInBuffer) {
					tracesDontFitInBuffer = setBufferLimit();
				}

				// Reads data to buffer. ReceiveBuffer position is always zero
				// here so readBytes will read until the limit or to the end of
				// file
				int readBytes = readFile.getChannel().read(receiveBuffer);
				if (readBytes <= 0) {
					putToSleep();
					// If data is found, process buffer
				} else {
					// If file is at end, set limit according
					if (readBytes < receiveBuffer.limit()) {
						receiveBuffer.limit(readBytes);
					}
					// Start processing
					processBuffer();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Sets buffer limit
	 * 
	 * @return true if traces fitted into the buffer
	 * @throws IOException
	 */
	private boolean setBufferLimit() throws IOException {
		boolean tracesDontFitInBuffer = false;
		FileMap fileMap = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getFileMap();
		int index = startReadingTracesFrom / TraceViewerGlobals.blockSize;

		// Filemap must have the offset for end trace
		if (fileMap.size() > index + numberOfBlocks) {

			// Get positions and the difference
			long pos = readFile.getFilePointer();
			long pos2 = fileMap.getItem(index + numberOfBlocks).longValue();
			long limit = pos2 - pos;

			// If difference is smaller than capacity, set as limit
			if (limit < receiveBuffer.capacity()) {
				receiveBuffer.limit((int) limit);
			} else {
				receiveBuffer.limit(receiveBuffer.capacity());
				tracesDontFitInBuffer = true;
			}
		} else {
			receiveBuffer.limit(receiveBuffer.capacity());
			tracesDontFitInBuffer = true;
		}
		return tracesDontFitInBuffer;
	}

	/**
	 * Process character buffer
	 */
	private void processBuffer() {
		receiveBuffer.position(0);
		StringBuffer buf = new StringBuffer();
		boolean traceReady = false;

		// Loop until there is characters left
		while (receiveBuffer.hasRemaining()) {
			byte c = receiveBuffer.get();

			// If in the middle of a line break from previous buffer, skip first
			// character
			if (middleOfLineBreak && c == '\n' && receiveBuffer.hasRemaining()) {
				c = receiveBuffer.get();
			}
			middleOfLineBreak = false;

			// Check the character
			switch ((char) c) {
			case '\n':
				traceReady = true;
				break;
			case '\r':
				if (!receiveBuffer.hasRemaining()) {
					middleOfLineBreak = true;
				} else if (((char) receiveBuffer.get()) == '\n') {
				} else {
					receiveBuffer.position(receiveBuffer.position() - 1);
				}
				traceReady = true;
				break;
			default:
				buf.append((char) c);
				traceReady = false;
			}

			// Trace is ready, insert it
			if (traceReady) {
				String traceString = buf.toString();
				buf.setLength(0);
				processTrace(traceString);
				traceReady = false;
			}
		}

		// File at end, process last trace
		try {
			if (readFile.getFilePointer() >= readFile.length()) {
				String traceString = buf.toString();
				buf.setLength(0);
				processTrace(traceString);
				traceReady = false;
			}

			// If data left in buffer, move file backwards the same amount
			else if (buf.length() > 0) {
				setFilePosition(readFile.getFilePointer() - buf.length());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		receiveBuffer.position(0);
	}

	/**
	 * Process plain text trace line
	 * 
	 * @param traceString
	 *            trace string
	 */
	private void processTrace(String traceString) {
		numberOfTracesRead++;
		trace = new TraceProperties(traceConfiguration);
		trace.traceString = traceString;
		trace.traceNumber = startReadingTracesFrom + numberOfTracesRead;

		// Not the last trace yet
		if (!lastTrace()) {
			trace.lastTrace = false;
			mediaCallback.processTrace(trace);

			// Last trace
		} else {
			tracesDontFitInBuffer = false;
			trace.lastTrace = true;
			blocking = true;
			numberOfTracesRead = 0;

			mediaCallback.processTrace(trace);

			putToSleep();
		}
	}

	/**
	 * Tells if this is the last trace
	 * 
	 * @return indication of if this is the last trace or not
	 */
	public boolean lastTrace() {
		boolean lastTrace = false;

		// 1 or 2 blocks read
		if (numberOfTracesRead >= TraceViewerGlobals.blockSize * numberOfBlocks) {
			lastTrace = true;
			// Last block
		} else if (startReadingTracesFrom + numberOfTracesRead >= TraceViewerGlobals
				.getTraceViewer().getDataReaderAccess().getCurrentDataReader()
				.getTraceCount()) {
			lastTrace = true;
		}
		return lastTrace;
	}

	/**
	 * Puts this thread to sleep
	 */
	public void putToSleep() {
		try {
			// Null variables if this reader is only for blocks
			if (TraceViewerGlobals.getTraceViewer().getStateHolder()
					.isScrolling()) {
				numberOfTracesRead = 0;
				receiveBuffer.limit(receiveBuffer.capacity());
				receiveBuffer.position(receiveBuffer.capacity());
			}
			if (blocking) {
				this.notifyAll();
				this.wait();
			}

		} catch (InterruptedException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#shutdown()
	 */
	public void shutdown() {
		running = false;
		try {
			sourceChannel.close();
			readFile.close();
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataScrollReader#setBlockReader(int,
	 * int, boolean)
	 */
	public void setBlockReader(int numberOfBlocks, int startingTrace,
			boolean blocking) {
		this.numberOfBlocks = numberOfBlocks;
		this.startReadingTracesFrom = startingTrace;
		this.blocking = blocking;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataScrollReader#setFilePosition(long)
	 */
	@Override
	public void setFilePosition(long filePos) {
		try {
			readFile.seek(filePos);

			if (filePos == fileStartOffset) {
				receiveBuffer.limit(receiveBuffer.capacity());
				receiveBuffer.position(receiveBuffer.capacity());
			}

		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#pause(boolean)
	 */
	public void pause(boolean pause) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#isPaused()
	 */
	public boolean isPaused() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getTracePositionInFile()
	 */
	public long getTracePositionInFile() {
		return 0;
	}

	@Override
	protected byte[] setProtocolSpecificStuffToMultiPartTrace(byte[] byteArr,
			TraceProperties trace) {
		// There isn't any multipart traces in plain text
		return byteArr;
	}
}
