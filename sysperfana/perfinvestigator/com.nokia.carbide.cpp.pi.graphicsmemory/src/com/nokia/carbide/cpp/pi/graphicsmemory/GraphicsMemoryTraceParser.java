/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.pi.graphicsmemory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;

public class GraphicsMemoryTraceParser extends Parser {
	private boolean debug = false;
	private String version;

	private GraphicsMemoryTrace memTrace;
	private int readCount = 0;

	// constants
	public final static int SAMPLE_TOTAL_MEMORY_PROCESS_ID = -1;
	public final static String SAMPLE_TOTAL_MEMORY_PROCESS_NAME = "TOTAL_MEMORY_USAGE"; //$NON-NLS-1$

	public ParsedTraceData parse(File file) throws IOException {
		if (!file.exists() || file.isDirectory()) {
			throw new IOException(Messages
					.getString("GraphicsMemoryTraceParser.cannotOpenTraceFile")); //$NON-NLS-1$
		}
		if (debug)
			System.out
					.println(Messages
							.getString("GraphicsMemoryTraceParser.traceFileLength") + file.length()); //$NON-NLS-1$

		parseMemTrace(file);
		int versionNumber = convertVersionStringToInt(version);
		memTrace.setVersion(versionNumber);

		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();

		return ptd;
	}

	private void parseV100GPUFile(DataInputStream dis) throws IOException {
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug)
			System.out
					.println(Messages
							.getString("GraphicsMemoryTraceParser.readVersionDebug") + new String(version)); //$NON-NLS-1$
		this.version = new String(version);

		this.readV100GPUSample(dis);
	}

	public String getProfilerVersion() {
		return version;
	}

	private void readV100GPUSample(DataInputStream dis) throws IOException {
		memTrace = new GraphicsMemoryTrace();

		GraphicsMemoryProcess process;

		// read sample header length
		dis.readUnsignedByte();
		readCount++;

		int length;
		int mode = -1;
		String processName = null;
		GraphicsMemorySample gpuSample = null;
		int usedMemory = -1;
		int totalMemory = -1;
		long sample = -1;

		List<GraphicsMemoryProcess> processList = new ArrayList<GraphicsMemoryProcess>();
		try {

			while (true) {
				length = dis.readUnsignedByte();
				readCount++;
				if (length == 1) {
					mode = dis.readUnsignedByte();
					readCount++;
				} else {
					throw new IOException(Messages
							.getString("GraphicsMemoryTraceParser.wrongLength")); //$NON-NLS-1$
				}

				if (mode == 0xac) { // time stamp
					// first there should be 4 bytes of sample time
					sample = this.readTUint(dis);
				}

				else if (mode == 0xaa) { // process name
					length = dis.readUnsignedByte();
					readCount++; // process sample length
					// reading process name
					processName = readProcessName(dis, length);
				} else if (mode == 0xab) { // process id
					// reading process id
					long processId = readTUint(dis);
					// create process and sample
					process = new GraphicsMemoryProcess(Integer
							.valueOf((int) processId), processName);
					gpuSample = new GraphicsMemorySample(process, (int) sample);

				} else if (mode == 0xdb) { // total GPU data
					// read the total GPU data
					long total = readTUint(dis);
					totalMemory = (int) total;

				} else if (mode == 0xdc) { // used GPU data
					// read the used GPU data
					long used = readTUint(dis);
					usedMemory = (int) used;

					if (debug)
						System.out
								.println(MessageFormat
										.format(
												Messages
														.getString("GraphicsMemoryTraceParser.readMemoryUsageDebug"), usedMemory, totalMemory, sample)); //$NON-NLS-1$
					GraphicsMemorySample memoryUsageSample = new GraphicsMemorySample(
							usedMemory, totalMemory, (int) sample);
					memTrace.addSample(memoryUsageSample);
					processList.add(memoryUsageSample.process);

					length = dis.readUnsignedByte(); // read process sample
														// length
					readCount++;

				} else if (mode == 0xde) { // private data
					// read the private data
					long privateSize = readTUint(dis);
					gpuSample.privateSize = (int) privateSize;
				} else if (mode == 0xdf) { // shared data
					// read the shared data
					long sharedSize = readTUint(dis);
					gpuSample.sharedSize = (int) sharedSize;

					if (debug)
						System.out
								.println(MessageFormat
										.format(
												Messages
														.getString("GraphicsMemoryTraceParser.readProcessDebug"), gpuSample.process.processName, gpuSample.sampleSynchTime, Long.toHexString(gpuSample.process.processId), gpuSample.privateSize, gpuSample.sharedSize)); //$NON-NLS-1$

					memTrace.addSample(gpuSample);
					processList.add(gpuSample.process);

					length = dis.readUnsignedByte(); // read process sample
														// length
					readCount++;
				} else {
					throw new IOException(
							Messages
									.getString("GraphicsMemoryTraceParser.wrongMode") + mode); //$NON-NLS-1$
				}
			}
		} catch (EOFException e) {
			memTrace.setProcesses(processList
					.toArray(new GraphicsMemoryProcess[processList.size()]));
			System.out.println(Messages
					.getString("GraphicsMemoryTraceParser.finishedReading")); //$NON-NLS-1$
		}
	}

	/*
	 * A method for calculating hash value for a string
	 */
	public static long sum(String arg) {
		int total = 0;
		for (int i = 0; i < arg.length(); i++) {
			total += (long) arg.charAt(i);
		}
		return total; // returns the sum of the chars after cast
	}

	private String readProcessName(DataInputStream dis, int length)
			throws IOException {
		if (length != 0) {
			byte[] element = new byte[length];
			dis.read(element, 0, length);
			readCount += length;
			return new String(element);
		} else
			return null;
	}

	private byte[] readElementWithLength(DataInputStream dis) throws IOException {
		byte length = dis.readByte();
		readCount++;
		if (length != 0) {
			byte[] element = new byte[length];
			dis.read(element, 0, length);
			readCount += length;
			return element;
		} else
			return null;
	}

	private long readTUint(DataInputStream dis) throws IOException {
		long result = dis.readUnsignedByte();
		readCount++;
		result += dis.readUnsignedByte() << 8;
		readCount++;
		result += dis.readUnsignedByte() << 16;
		readCount++;
		result += dis.readUnsignedByte() << 24;
		readCount++;
		return result;
	}

	private void parseMemTrace(File file) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		byte[] traceArray = new byte[(int) file.length()];
		dis.readFully(traceArray);

		// test the graph mem trace version
		String s = new String(traceArray, 1, traceArray[0]);
		if (traceArray.length > 257) {
			if (s.startsWith("Bappea_GPU_V1.00")) { //$NON-NLS-1$
				ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
				dis = new DataInputStream(bais);
				this.parseV100GPUFile(dis);
				return;
			}
		}
		String version = s.substring(8, 12);
		String traceType = s.substring(13, s.length());
		System.out
				.println(Messages
						.getString("GraphicsMemoryTraceParser.foundVersion1") + version + Messages.getString("GraphicsMemoryTraceParser.foundVersion2") + traceType + Messages.getString("GraphicsMemoryTraceParser.foundVersion3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		GeneralMessages
				.showErrorMessage(Messages
						.getString("GraphicsMemoryTraceParser.wrongTraceType1") + traceType + Messages.getString("GraphicsMemoryTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
		throw new IOException(
				Messages.getString("GraphicsMemoryTraceParser.wrongTraceType1") + traceType + Messages.getString("GraphicsMemoryTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	private GenericSampledTrace getTrace() {
		return (GenericSampledTrace) memTrace;
	}

	private int convertVersionStringToInt(String version) {
		// Coverts version number from string to int
		int i = 0;
		int versionInt = 0;
		String versionString = ""; //$NON-NLS-1$

		// goes string thru and copies all digits into another string
		while (i < version.length()) {
			if (Character.isDigit(version.charAt(i))) {
				versionString += version.charAt(i);
			}
			i++;

		}
		// convert string to int
		try {
			versionInt = Integer.parseInt(versionString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionInt;
	}
}
