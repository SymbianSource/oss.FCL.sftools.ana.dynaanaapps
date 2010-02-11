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

import java.util.*;
import java.io.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * An event log class. Contains a list of all events found in XML.
 * 
 * Event log is part of MobileCrash data, which contains 30 latest 
 * window and key press events which occurred before the crash.
 * 
 * List of events example:
 * [window][Home screen...]
 * [key][Backspace key]
 * [window][Home screen...]
 * .
 * .
 * .
 *
 */
public final class EventLog {
	
	// XML tags
	public static final String TAG_EVENT = "event";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String TYPE_KEY = "key";
	
	// events list
	private final List<String[]> logEvents;
	
	/**
	 * Constructor
	 * @param events list of events
	 */
	private EventLog(List<String[]> events) {
		logEvents = events;
	}
	
	public List<String[]> getLogEvents() {
		return logEvents;
	}
	
	/**
	 * Writes events in to a buffer (e.g. a text file)
	 * @param out where to write
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		writeLine(out,"");
		writeLine(out, "SYSTEM EVENT LOG:");
		writeLine(out, "-----------------");
		// if there are any events
		if (logEvents != null && !logEvents.isEmpty()) {
			int longestEventName = 0;
			// calculate the longest event name
			for (int i = 0; i < logEvents.size(); i++) {
				String[] event = logEvents.get(i);
				if (event[0].length() > longestEventName)
					longestEventName = event[0].length();
			}
			// print events
			for (int i = 0; i < logEvents.size(); i++) {
				String[] event = logEvents.get(i);
				String format = String.format("%%-%ds  %%s", longestEventName);
				String line = String.format(format, event[0], event[1]);
				writeLine(out, line);
			}
		}
	}
	
	/**
	 * Writes given line plus a line break.
	 * @param out where to write
	 * @param line what to write
	 * @throws IOException
	 */
	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}
	
	/**
	 * Reads events from an XML element
	 * @param elementSegEventLog segeventlog tag
	 * @return a created EventLog class or null
	 */
	public static EventLog read(Element elementSegEventLog) {
		try {
			List<String[]> events = new ArrayList<String[]>();
			
			// get all event nodes
			NodeList children = elementSegEventLog.getChildNodes();
			if (children != null && children.getLength() > 0) {
				// go through all event nodes
				for (int i = 0; i < children.getLength(); i++) {
					Node el = children.item(i);
					// node is event node
					if (TAG_EVENT.equals(el.getNodeName())) {
						String eventValue = el.getFirstChild().getNodeValue();
						NamedNodeMap attributes = el.getAttributes();
						// if node has attributes
						if (attributes != null && attributes.getLength() > 0) {
							Node typeAttribute = attributes.getNamedItem(ATTRIBUTE_TYPE);
							String type = typeAttribute.getNodeValue();
							events.add(new String[]{type, eventValue});
						}
					}
				}
			}
			
			return new EventLog(events);
		} catch (Exception e) {
			return null;
		}
	}
}
