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

import java.io.BufferedWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import java.util.*;

/**
 * A Code Segment class. Contains code segment's data. 
 *
 */
public final class CodeSegment {
	
	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "name";
	public static final String TAG_RANGE = "range";
	public static final String TAG_ATTRIBUTES = "attributes";
	public static final String TAG_NOSYMBOLS = "nosymbols";
	public static final String TAG_MISMATCH = "mismatch";
	public static final String TAG_LINK = "link";
	
	// segment data
	private final int segmentId;
	private final String segmentName;
	private final String segmentRange;
	private final String segmentText;
	private final boolean segmentShouldBeHighlighted;
	
	/**
	 * Constructor
	 * 
	 * @param id element id
	 * @param name segment name
	 * @param range segment range
	 * @param text segment text
	 * @param shouldBeHighlighted defines whether segment should be highlight e.g. due to missing symbols etc.
	 */
	private CodeSegment(int id, String name, String range, String text, boolean shouldBeHighlighted) {
		segmentId = id;
		segmentName = name;
		segmentRange = range;
		segmentShouldBeHighlighted = shouldBeHighlighted;
		segmentText = text;
	}
	
	@Override
	public String toString() {
		return segmentText;
	}
	
	public int getId() {
		return segmentId;
	}
	
	public String getSegmentName() {
		return segmentName;
	}
	
	public String getSegmentRange() {
		return segmentRange;
	}
	
	public boolean shouldBeHighlighted() {
		return segmentShouldBeHighlighted;
	}
	
	/**
	 * Writes code segment data into a buffer (e.g. a text file)
	 * @param out 
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		String line = "";
		if (!"".equals(segmentRange))
			line += segmentRange + "  ";
		if (!"".equals(segmentName))
			line += segmentName;
		writeLine(out, line);
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
	 * Reads code segment data from XML element.
	 * @param elementCodeseg codeseg tag
	 * @param messages list of messages to which code segment might have a reference to.
	 * @return a created CodeSegment class or null
	 */
	public static CodeSegment read(Element elementCodeseg, Map<Integer, Message> messages) {
		try {
			// read code segment id
			String segmentId = XmlUtils.getTextValue(elementCodeseg, TAG_ID);
			if (segmentId == null)
				return null;
			
			// convert code segment id to integer
			int id;
			try {
				id = Integer.parseInt(segmentId);
			} catch (Exception e) {
				return null;
			}
			
			// read segment name
			String segmentName = XmlUtils.getTextValue(elementCodeseg, TAG_NAME);
			if (segmentName == null)
				segmentName = "";
			
			// read segment range
			String segmentRange = XmlUtils.getTextValue(elementCodeseg, TAG_RANGE);
			if (segmentRange == null)
				segmentRange = "";
			
			boolean shouldBeHighlighted = false;
			
			// get the attributes node
			NodeList attribute = elementCodeseg.getElementsByTagName(TAG_ATTRIBUTES);
			if (attribute != null && attribute.getLength() > 0) {
				// check if attributes node contains nosymbols node
				shouldBeHighlighted = XmlUtils.containsNode((Element)attribute.item(0), TAG_NOSYMBOLS);
				if (!shouldBeHighlighted)
					shouldBeHighlighted = XmlUtils.containsNode((Element)attribute.item(0), TAG_MISMATCH);
			}
			
			String messageText = "";
			// see if there are any messages for this code segment
			NodeList message = elementCodeseg.getElementsByTagName(TAG_LINK);
			if (message != null && message.getLength() > 0) {
				String messageId = XmlUtils.getNodeValue(message.item(0));
				try {
					int msgId = Integer.parseInt(messageId);
					if (messages.containsKey(msgId))
						messageText = messages.get(msgId).getTitle();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return new CodeSegment(id, segmentName, segmentRange, messageText, shouldBeHighlighted);
		} catch (Exception e) {
			return null;
		}
	}
}
