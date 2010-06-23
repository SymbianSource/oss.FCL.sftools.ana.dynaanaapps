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
 * Plain Text Reader
 *
 */
package com.nokia.traceviewer.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Plain Text Reader
 */
public class PlainTextReader extends BaseDataReader {

	/**
	 * Running boolean
	 */
	private boolean running;

	/**
	 * Position of file
	 */
	private long filePos;

	/**
	 * File reader
	 */
	private BufferedReader input;

	/**
	 * File to read from
	 */
	private File file;

	/**
	 * Character buffer where chars are read
	 */
	private final CharBuffer cbuf;

	/**
	 * Remaining characters from the last read
	 */
	private int remainingChars;

	/**
	 * Temporary buffer for data
	 */
	private final CharBuffer tempBuffer;

	/**
	 * Paused boolean
	 */
	private boolean paused;

	/**
	 * Indicates that traces can be processed
	 */
	private boolean canProcessTraces;

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
	public PlainTextReader(MediaCallback mediaCallback,
			TraceConfiguration configuration) {
		super();
		this.mediaCallback = mediaCallback;
		trace = new TraceProperties(configuration);
		traceConfiguration = configuration;
		cbuf = CharBuffer.allocate(RECEIVE_BUFFER_SIZE);
		tempBuffer = CharBuffer.allocate(MAX_MESSAGE_SIZE);

		running = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#start()
	 */
	@Override
	public void run() {
		try {
			file = new File(filePath);
			input = new BufferedReader(new FileReader(file));
			while (running) {
				canProcessTraces = true;
				if (!paused) {
					// Read from the file
					int chars = input.read(cbuf);
					if (chars > 0) {
						cbuf.limit(chars + remainingChars);
						remainingChars = 0;
						processBuffer();
					} else {
						// Inform callback for EOF
						mediaCallback.endOfFile(this);
						Thread.sleep(PAUSE_TIME);
					}
				} else {
					Thread.sleep(PAUSE_TIME);
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	/**
	 * Process character buffer
	 */
	private void processBuffer() {
		canProcessTraces = true;
		cbuf.position(0);
		StringBuffer buf = new StringBuffer();
		int extra = 0;
		boolean traceReady = false;

		// Loop until there is characters left
		while (cbuf.hasRemaining()) {
			char c = cbuf.get();

			// If in the middle of a line break from previous buffer, skip first
			// character
			if (middleOfLineBreak && c == '\n' && cbuf.hasRemaining()) {
				c = cbuf.get();
			}
			middleOfLineBreak = false;

			// Check the character
			switch (c) {
			case '\n':
				extra = 1;
				traceReady = true;
				break;
			case '\r':
				if (!cbuf.hasRemaining()) {
					extra = 1;
					middleOfLineBreak = true;
				} else if ((cbuf.get()) == '\n') {
					extra = 2;
				} else {
					cbuf.position(cbuf.position() - 1);
					extra = 1;
				}

				traceReady = true;
				break;
			default:
				buf.append(c);
				traceReady = false;
			}

			// Trace is ready, insert it
			if (traceReady) {
				String traceString = buf.toString();
				buf.setLength(0);
				if (canProcessTraces) {
					processTrace(extra, traceString);
				}
				extra = 0;
				traceReady = false;
			}
		}
		remainingChars = buf.length();

		// If the end of the file and there is characters in the buffer, process
		// the remaining trace
		if ((filePos + remainingChars) >= file.length()
				&& buf.length() > 0
				&& !TraceViewerGlobals.getTraceViewer()
						.getDataProcessorAccess().getFilterProcessor()
						.isProcessingFilter()) {
			String traceString = buf.toString();
			if (canProcessTraces) {
				processTrace(extra, traceString);
			}
			remainingChars = 0;
		}

		// Copy the remaining characters to the start of the buffer
		copyRemainingToBufferStart();
	}

	/**
	 * Copy remaining characters to the start of the buffer
	 */
	private void copyRemainingToBufferStart() {
		if (remainingChars > 0 && canProcessTraces) {
			cbuf.position(cbuf.limit() - remainingChars);
			cbuf.limit(cbuf.limit());
			tempBuffer.position(0);
			tempBuffer.limit(MAX_MESSAGE_SIZE);
			tempBuffer.put(cbuf);

			cbuf.position(0);
			cbuf.limit(RECEIVE_BUFFER_SIZE);
			tempBuffer.position(0);
			tempBuffer.limit(remainingChars);

			cbuf.put(tempBuffer);
			cbuf.position(remainingChars);
		} else {
			remainingChars = 0;
			cbuf.position(0);
			cbuf.limit(RECEIVE_BUFFER_SIZE);
		}
	}

	/**
	 * Process trace
	 * 
	 * @param extra
	 *            number of extra linefeed characters to add to file position
	 * @param traceString
	 *            trace string
	 */
	private void processTrace(int extra, String traceString) {
		traceCount++;
		trace = new TraceProperties(traceConfiguration);
		trace.traceString = traceString;
		trace.traceNumber = traceCount;

		// Count the file position if trace is dividable by
		// blockSize
		if (traceCount % TraceViewerGlobals.blockSize == 1) {
			fileMap.insert(Long.valueOf(filePos));
		}
		filePos += trace.traceString.length() + extra;

		// Check if last trace
		checkLastTrace();

		mediaCallback.processTrace(trace);

	}

	/**
	 * Checks if last trace
	 */
	private void checkLastTrace() {
		if (filePos >= file.length()) {
			trace.lastTrace = true;
		} else {
			trace.lastTrace = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#pause(boolean)
	 */
	public void pause(boolean pause) {
		paused = pause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#shutdown()
	 */
	public void shutdown() {
		running = false;
		// Close the stream
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#setFilePosition(long)
	 */
	@Override
	public void setFilePosition(long filePos) {
		// Start from the beginning
		if (filePos == 0) {
			try {
				canProcessTraces = false;
				input.close();
				this.filePos = 0;
				remainingChars = 0;
				fileMap.clearMap();
				traceCount = 0;
				cbuf.clear();
				file = new File(filePath);
				input = new BufferedReader(new FileReader(file));
				paused = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#clearFile()
	 */
	@Override
	public void clearFile() {
		super.clearFile();
		setFilePosition(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.BaseDataReader#createScrollReader(com.nokia
	 * .traceviewer.engine.MediaCallback,
	 * com.nokia.traceviewer.engine.TraceConfiguration)
	 */
	@Override
	public DataScrollReader createScrollReader(MediaCallback mediaCallback,
			TraceConfiguration conf) {
		return new PlainTextScrollReader(mediaCallback, conf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#isPaused()
	 */
	public boolean isPaused() {
		return paused;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getTracePositionInFile()
	 */
	public long getTracePositionInFile() {
		return filePos;
	}

	@Override
	protected byte[] setProtocolSpecificStuffToMultiPartTrace(byte[] byteArr,
			TraceProperties trace) {
		// There isn't any multipart traces in plain text
		return byteArr;
	}
}
