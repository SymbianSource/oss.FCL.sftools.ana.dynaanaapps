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
 * Base DataReader class
 *
 */
package com.nokia.traceviewer.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Base DataReader class
 */
public abstract class BaseDataReader extends Thread implements DataReader,
		TraceViewerConst, BTraceConstants {

	/**
	 * Sleeping time
	 */
	protected static final int SLEEP_TIME = 200;

	/**
	 * Pause waiting time
	 */
	protected static final int PAUSE_TIME = 300;

	/**
	 * Trace configuration to be used in this Reader
	 */
	protected TraceConfiguration traceConfiguration;

	/**
	 * Trace properties, passed to MediaCallback
	 */
	protected TraceProperties trace;

	/**
	 * Data source
	 */
	protected FileChannel sourceChannel;

	/**
	 * Buffer for data received from data source
	 */
	protected ByteBuffer receiveBuffer;

	/**
	 * Media processor
	 */
	protected MediaCallback mediaCallback;

	/**
	 * File path from where to read
	 */
	protected String filePath;

	/**
	 * Random access file we use to read the binary
	 */
	protected RandomAccessFile readFile;

	/**
	 * Filemap that hold the trace block positions
	 */
	protected FileMap fileMap;

	/**
	 * Trace count in this file
	 */
	protected int traceCount;

	/**
	 * File start offset
	 */
	protected long fileStartOffset;

	/**
	 * Multipart trace array
	 */
	protected static Map<Integer, MultiPartItem> multiPartTraceArray;

	/**
	 * Constructor
	 */
	public BaseDataReader() {
		fileMap = new FileMap();
	}

	/**
	 * Creates file channel
	 */
	protected void createFileChannel() {
		try {
			if (readFile != null) {
				// Close previous file channel
				readFile.close();
			}
			readFile = new RandomAccessFile(filePath, "r"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (readFile != null) {
			sourceChannel = readFile.getChannel();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getFilePath()
	 */
	public String getFilePath() {
		return filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataReader#setFilePath(java.lang.String)
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#setFileStartOffset(long)
	 */
	public void setFileStartOffset(long fileStartOffset) {
		this.fileStartOffset = fileStartOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getFileStartOffset()
	 */
	public long getFileStartOffset() {
		return fileStartOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getFileMap()
	 */
	public FileMap getFileMap() {
		return fileMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#clearFile()
	 */
	public void clearFile() {
		fileMap.clearMap();
		traceCount = 0;
		fileStartOffset = 0;
		if (readFile != null) {
			createFileChannel();

			// Set everything to zero
			setFilePosition(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getTraceCount()
	 */
	public int getTraceCount() {
		return traceCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataReader#createScrollReader(com.nokia.
	 * traceviewer.engine.MediaCallback,
	 * com.nokia.traceviewer.engine.TraceConfiguration)
	 */
	public DataScrollReader createScrollReader(MediaCallback mediaCallback,
			TraceConfiguration conf) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#getTraceConfiguration()
	 */
	public TraceConfiguration getTraceConfiguration() {
		return traceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataReader#setFilePosition(long)
	 */
	public void setFilePosition(long filePos) {
		try {
			if (readFile != null) {
				readFile.seek(filePos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses BTrace variables
	 * 
	 * @param buf
	 *            buffer where the data is located
	 * @param bTraceHeaderOffset
	 *            BTrace header offset
	 * @param trace
	 *            trace where the variables are inserted
	 */
	protected void parseBTraceVariables(ByteBuffer buf, int bTraceHeaderOffset,
			TraceProperties trace) {
		int variablesFound = 0;

		// Get BTrace header bytes
		byte recordSize = buf.get(bTraceHeaderOffset);
		byte flags = buf.get(bTraceHeaderOffset + 1);
		byte category = buf.get(bTraceHeaderOffset + 2);
		byte subCategory = buf.get(bTraceHeaderOffset + 3);

		// Set BTrace header bytes
		trace.bTraceInformation.setRecordSize(recordSize);
		trace.bTraceInformation.setFlags(flags);
		trace.bTraceInformation.setCategory(category);
		trace.bTraceInformation.setSubCategory(subCategory);

		// Loop through flags
		for (int i = 0; i < FLAGS_LENGTH; i++) {
			if ((flags & (1 << i)) != 0) {

				switch (i) {

				// Header2 present (bit number 0)
				case HEADER2_PRESENT_BIT:
					trace.bTraceInformation.setHeader2Present(true);

					// CPU Id
					int cpuId = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH);
					cpuId = Integer.reverseBytes(cpuId);
					cpuId = (cpuId & CPU_ID_MASK) >> CPU_ID_SHIFT;
					trace.bTraceInformation.setCpuId(cpuId);

					// Multipart trace
					int multipart = 0;
					multipart |= buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH);
					multipart = Integer.reverseBytes(multipart);
					multipart = multipart & MULTIPART_MASK;
					trace.bTraceInformation.setMultiPart(multipart);
					break;

				// Timestamp present (bit number 1)
				case TIMESTAMP_PRESENT_BIT:
					int timestamp = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH
							+ (variablesFound * BTRACE_VARIABLE_LENGTH));

					timestamp = Integer.reverseBytes(timestamp);
					trace.bTraceInformation.setTimestampPresent(true);
					trace.bTraceInformation.setTimestamp(timestamp);
					break;

				// Timestamp2 present (bit number 2)
				case TIMESTAMP2_PRESENT_BIT:
					int timestamp2 = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH
							+ (variablesFound * BTRACE_VARIABLE_LENGTH));

					timestamp2 = Integer.reverseBytes(timestamp2);
					trace.bTraceInformation.setTimestamp2Present(true);
					trace.bTraceInformation.setTimestamp2(timestamp2);
					break;

				// Context ID present (bit number 3)
				case CONTEXT_ID_PRESENT_BIT:
					int contextId = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH
							+ (variablesFound * BTRACE_VARIABLE_LENGTH));

					contextId = Integer.reverseBytes(contextId);
					trace.bTraceInformation.setContextIdPresent(true);
					trace.bTraceInformation.setThreadId(contextId);
					break;

				// Program counter present (bit number 4)
				case PROGRAM_COUNTER_PRESENT_BIT:
					int programCounter = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH
							+ (variablesFound * BTRACE_VARIABLE_LENGTH));

					programCounter = Integer.reverseBytes(programCounter);
					trace.bTraceInformation.setProgramCounterPresent(true);
					trace.bTraceInformation.setProgramCounter(programCounter);
					break;

				// Extra value present (bit number 5)
				case EXTRA_VALUE_PRESENT_BIT:
					int extraValue = buf.getInt(bTraceHeaderOffset
							+ BTRACE_HEADER_LENGTH
							+ (variablesFound * BTRACE_VARIABLE_LENGTH));

					extraValue = Integer.reverseBytes(extraValue);
					trace.bTraceInformation.setExtraValuePresent(true);
					trace.bTraceInformation.setExtraValue(extraValue);
					break;

				// Record truncated (bit number 6)
				case RECORD_TRUNCATED_BIT:
					trace.bTraceInformation.setTruncated(true);
					variablesFound--;
					break;

				// Record missing (bit number 7)
				case RECORD_MISSING_BIT:
					trace.bTraceInformation.setTraceMissing(true);
					variablesFound--;
					break;
				}

				// Add variable count for each found flag
				variablesFound++;
			}
		}

		// Data starts after BTrace header + possible variables
		trace.dataStart = bTraceHeaderOffset + BTRACE_HEADER_LENGTH
				+ (variablesFound * BTRACE_VARIABLE_LENGTH);

		// Get length of the BTrace data
		int btraceDataLength = buf.get(bTraceHeaderOffset) & BYTE_MASK;

		// If length is FF, use the length from header instead
		if (btraceDataLength == BYTE_MASK) {
			trace.dataLength = trace.messageLength
					- (trace.dataStart - trace.messageStart);
		} else {
			trace.dataLength = btraceDataLength
					- ((variablesFound + 1) * BTRACE_VARIABLE_LENGTH);
		}
	}

	/**
	 * Handles multipart trace
	 * 
	 * @param trace
	 *            trace properties
	 * @param multiPartValue
	 *            multi part value
	 * @param bTraceHeaderOffset
	 *            BTrace header offset
	 * @return true if trace should be given to DataProcessors
	 */
	protected boolean handleMultiPart(TraceProperties trace,
			int multiPartValue, int bTraceHeaderOffset) {
		boolean valid = true;
		if (multiPartTraceArray == null) {
			multiPartTraceArray = Collections
					.synchronizedMap(new HashMap<Integer, MultiPartItem>());
		}

		Integer key = Integer.valueOf(trace.bTraceInformation.getExtraValue());
		MultiPartItem item = multiPartTraceArray.get(key);

		// First or middle part
		if (multiPartValue == 1 || multiPartValue == 2) {
			valid = false;

			// Add trace to array
			if (item == null) {
				MultiPartItem newItem = new MultiPartItem();
				newItem.addPart(trace, false);
				multiPartTraceArray.put(key, newItem);

				// Don't add parts if trace is already completed
			} else if (!item.isCompleted()) {
				item.addPart(trace, false);
			}

			// Last part
		} else if (multiPartValue == 3 && item != null) {
			if (!item.isCompleted()) {
				item.addPart(trace, true);
				item.setCompleted(true);
			}

			// Create byte array
			int totalMessageLength = item.getTotalMessageLength();
			byte byteArr[] = new byte[totalMessageLength];

			// First copy the header from the last trace part
			int initialHeaderSize = trace.dataStart - trace.messageStart;
			trace.byteBuffer.position(trace.messageStart);
			trace.byteBuffer.get(byteArr, 0, initialHeaderSize);

			// Then the old data
			Iterator<Integer> headerLenIterator = item
					.getTracePartHeaderSizes().iterator();
			Iterator<byte[]> byteIterator = item.getTraceParts().iterator();
			int startOffset = initialHeaderSize;

			// Copy all the stuff from parts
			while (headerLenIterator.hasNext()) {
				int headerLen = headerLenIterator.next().intValue();
				byte[] partBytes = byteIterator.next();
				System.arraycopy(partBytes, headerLen, byteArr, startOffset,
						partBytes.length - headerLen);
				startOffset += (partBytes.length - headerLen);
			}

			// Set multipart item to the trace
			trace.bTraceInformation.setMultiPartTraceParts(item);

			// Set record size
			int recordSize = totalMessageLength;
			if (recordSize > 0xFF) {
				recordSize = 0xFF;
			}
			bTraceHeaderOffset -= trace.messageStart;
			byteArr[bTraceHeaderOffset] = (byte) recordSize;
			trace.bTraceInformation.setRecordSize((byte) recordSize);

			// Remove multipart info from BTrace header
			int bTraceHeader2Offset = bTraceHeaderOffset + BTRACE_HEADER_LENGTH;
			byteArr[bTraceHeader2Offset] &= ~MULTIPART_MASK;

			// Set protocol specific stuff to multi part trace
			byteArr = setProtocolSpecificStuffToMultiPartTrace(byteArr, trace);

			trace.byteBuffer = ByteBuffer.wrap(byteArr);
		}

		return valid;
	}

	/**
	 * Sets protocol specific stuff to multi part trace
	 * 
	 * @param byteArr
	 *            byte array containing the multi part trace
	 * @param trace
	 *            Trace properties
	 * @return byte array containing the trace after procotol specific stuff is
	 *         added
	 */
	protected abstract byte[] setProtocolSpecificStuffToMultiPartTrace(
			byte[] byteArr, TraceProperties trace);
}
