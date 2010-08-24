/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
*/

package com.nokia.s60tools.crashanalyser.containers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import java.io.*;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import java.util.*;

/**
 * A process class. Contains all process information.
 * This class owns this process' threads and code segments 
 *
 */
public final class Process {
	
	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "name";
	public static final String TAG_UID1 = "uid1";
	public static final String TAG_UID2 = "uid2";
	public static final String TAG_UID3 = "uid3";
	public static final String TAG_SID = "sid";
	public static final String TAG_LINK = "link";
	public static final String ATTRIBUTE_SEG = "seg";
	public static final String SEGMENT_THREADS = "seg_threads";
	public static final String SEGMENT_CODESEGS = "seg_codesegs";
	
	public static enum StackItems {ALL, SYMBOLS, ACCURATE};
	
	static final String FORMAT = "%-13s: %s";

	// process data
	private final int processId;
	private final String processName;
	private final String processUid1;
	private final String processUid2;
	private final String processUid3;
	private final String processSid;
	private final List<Thread> processThreads;
	private final List<CodeSegment> processCodeSegments;
	
	/**
	 * Constructor 
	 * @param id process id
	 * @param name process name
	 * @param uid1 process UID1
	 * @param uid2 process UID2
	 * @param uid3 process UID3
	 * @param sid process SID
	 * @param threads threads of the process
	 * @param codeSegments code segments of the process
	 */
	private Process(final int id, final String name, final String uid1, final String uid2, final String uid3, final String sid,
					final List<Thread> threads, final List<CodeSegment> codeSegments) {
		processId = id;
		processName = name;
		processUid1 = uid1;
		processUid2 = uid2;
		processUid3 = uid3;
		processSid = sid;
		processThreads = threads;
		processCodeSegments = codeSegments;
	}
	
	public int getId() {
		return processId;
	}
	
	public String getName() {
		return processName;
	}
	
	public List<CodeSegment> getCodeSegments() {
		return processCodeSegments;
	}
	
	/**
	 * Writes process data into given buffer (i.e text file)
	 * @param out buffer to write to
	 * @param stackItems
	 * @param html defines whether html format should be written
	 * @throws IOException
	 */
	public void writeTo(final BufferedWriter out, final StackItems stackItems, final boolean html) throws IOException {
		
		// write basic process data
		writeLine(out,"");
		writeLine(out, "PROCESS:");
		writeLine(out, "--------");
		writeLine(out, "Process Name", processName);
		writeLine(out, "Uid1", processUid1);
		writeLine(out, "Uid2", processUid2);
		writeLine(out, "Uid3", processUid3);
		writeLine(out, "Sid", processSid);
		writeLine(out, "");
		
		// write code segments of the process
		if (processCodeSegments != null && !processCodeSegments.isEmpty()) {
			writeLine(out, "CODE SEGMENTS:");
			writeLine(out, "--------------");
			for (int i = 0; i < processCodeSegments.size(); i++) {
				final CodeSegment codeSegment = processCodeSegments.get(i);
				codeSegment.writeTo(out);
			}
			writeLine(out, "");
		}

		// write threads of the process
		if (processThreads != null && !processThreads.isEmpty()) {
			for (int i = 0; i < processThreads.size(); i++) {
				final Thread thread = processThreads.get(i);
				thread.writeTo(out, stackItems, html);
				writeLine(out, "");
			}
			writeLine(out, "");
		}
	}
	
	void writeLine(final BufferedWriter out, final String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	void writeLine(final BufferedWriter out, final String header, final String value) throws IOException {
		if (!"".equals(value)) {
			out.write(String.format(FORMAT, header, value));
			out.newLine();
		}
	}

	/**
	 * Reads and creates a process from process xml element
	 * @param elementProcess
	 * @param threads
	 * @param codeSegments
	 * @return created process or null
	 */
	public static Process read(final Element elementProcess, 
								final Map<Integer, Thread> threads,
								final Map<Integer, CodeSegment> codeSegments) {
		try {
			if (threads == null || threads.isEmpty())
				return null;
			
			// read process id
			final String processId = XmlUtils.getTextValue(elementProcess, TAG_ID);
			if (processId == null)
				return null;
			
			// convert process id to integer
			final int id = Integer.parseInt(processId);
			
			// read process name
			final String  processName = XmlUtils.getTextValue(elementProcess, TAG_NAME);
			if (processName == null)
				return null;
			
			// read UID1 if exists
			String uid1 = XmlUtils.getTextValue(elementProcess, TAG_UID1);
			if (uid1 == null)
				uid1 = "";
			
			// read UID2 if exists
			String uid2 = XmlUtils.getTextValue(elementProcess, TAG_UID2);
			if (uid2 == null)
				uid2 = "";
	
			// read UID# if exists
			String uid3 = XmlUtils.getTextValue(elementProcess, TAG_UID3);
			if (uid3 == null)
				uid3 = "";
	
			// read SID if exists
			String sid = XmlUtils.getTextValue(elementProcess, TAG_SID);
			if (sid == null)
				sid = "";
			
			// get link nodes for thread and codesegment ids
			final NodeList nl = elementProcess.getElementsByTagName(TAG_LINK);
			if (nl == null || nl.getLength() < 1)
				return null;

			final List<Thread> processThreads = new ArrayList<Thread>();
			final List<CodeSegment> processCodesegments = new ArrayList<CodeSegment>();

			// read threads and code segments
			for (int i = 0; i < nl.getLength(); i++) {
				final Node node = nl.item(i);
				final String nodeValue = XmlUtils.getNodeValue(node);
				final NamedNodeMap attributes = node.getAttributes();
				if (attributes != null && attributes.getLength() > 0) {
					final Node seg = attributes.getNamedItem(ATTRIBUTE_SEG);
					// thread id
					if (SEGMENT_THREADS.equals(XmlUtils.getNodeValue(seg))) {
						final int tId = Integer.parseInt(nodeValue);
						if (threads.containsKey(tId))
							processThreads.add(threads.get(tId));
					// codesegment id
					} else if (SEGMENT_CODESEGS.equals(XmlUtils.getNodeValue(seg))) {
						final int segmentId = Integer.parseInt(nodeValue);
						if (codeSegments.containsKey(segmentId))
							processCodesegments.add(codeSegments.get(segmentId));
					}
				}
			}
			
			if (processThreads.isEmpty())
				return null;
			
			return new Process(id, processName, uid1, uid2, uid3, sid, processThreads, processCodesegments);
			
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the first thread of this process
	 * @return first thread of this process or null
	 */
	public Thread getFirstThread() {
		Thread thread = null;
		if (processThreads != null && !processThreads.isEmpty())
			thread = processThreads.get(0);
			
		return thread;
	}
	
	/**
	 * Returns the threads of this process
	 * @return threads of this process or null
	 */
	public List<Thread> getThreads() {
		return processThreads;
	}
}
