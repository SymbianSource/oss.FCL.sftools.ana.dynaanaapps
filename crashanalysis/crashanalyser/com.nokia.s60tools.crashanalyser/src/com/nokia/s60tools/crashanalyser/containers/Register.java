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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;

/**
 * A class for register information.
 *
 */
public final class Register {

	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "name";
	public static final String TAG_VALUE = "value";
	public static final String TAG_LINK = "link";
	public static final String ATTRIBUTE_SEG = "seg";
	public static final String SEGMENT_SYMBOLS = "seg_symbols";
	public static final String SEGMENT_MESSAGES = "seg_messages";
	
	static final String FORMAT = "%-10s %-10s  %s";

	// register data
	private final int registerId;
	private final String registerName;
	private final String registerValue;
	private final String registerSymbol;
	private final String registerComments;
	
	/**
	 * Constructor
	 * @param id register id
	 * @param name register name
	 * @param value register value
	 * @param symbol register symbol information
	 * @param comments registers comments
	 */
	private Register(int id, String name, String value, String symbol, String comments) {
		registerId = id;
		registerName = name;
		registerValue = value;
		registerSymbol = symbol;
		registerComments = comments;
	}
	
	public int getId() {
		return registerId;
	}
	
	public String getName() {
		return registerName;
	}
	
	public String getValue() {
		return registerValue;
	}
	
	public String getSymbol() {
		return registerSymbol;
	}
	
	public String getComments() {
		return registerComments;
	}
	
	/**
	 * Write register into given buffer (i.e. text file)
	 * @param out buffer to write to
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		if (!"".equals(registerName)) {
			if ("".equals(registerComments))
				writeLine(out, String.format(FORMAT, registerName, registerValue, registerSymbol));
			else
				writeLine(out, String.format(FORMAT, registerName, registerValue, registerSymbol + " (" + registerComments + ")"));
		}
	}
	
	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}
	
	/**
	 * Reads and creates a register from register xml element.
	 * @param elementRegister
	 * @param symbols
	 * @param messages
	 * @return created register or null
	 */
	public static Register read(Element elementRegister, 
								Map<Integer, Symbol> symbols,
								Map<Integer, Message> messages) {
		try {
			// read register id
			String registerId = XmlUtils.getTextValue(elementRegister, TAG_ID);
			if (registerId == null)
				return null;
			
			int regId = Integer.parseInt(registerId);

			// read register name
			String registerName = XmlUtils.getTextValue(elementRegister, TAG_NAME);
			if (registerName == null)
				return null;
			
			// read register value
			String registerValue = XmlUtils.getTextValue(elementRegister, TAG_VALUE);
			if (registerValue == null)
				return null;
			
			String symbol = "";
			String comments = "";
			
			// see if register has a symbol and/or message
			NodeList nl = elementRegister.getElementsByTagName(TAG_LINK);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node linkNode = nl.item(i);
					String nodeValue = XmlUtils.getNodeValue(linkNode);
					NamedNodeMap attributes = linkNode.getAttributes();
					if (attributes != null && attributes.getLength() > 0) {
						Node seg = attributes.getNamedItem(ATTRIBUTE_SEG);
						// symbol id
						if (SEGMENT_SYMBOLS.equals(XmlUtils.getNodeValue(seg))) {
							int sId = Integer.parseInt(nodeValue);
							if (symbols.containsKey(sId))
								symbol = symbols.get(sId).getName();
						// message id
						} else if (SEGMENT_MESSAGES.equals(XmlUtils.getNodeValue(seg))) {
							int mId = Integer.parseInt(nodeValue);
							if (messages.containsKey(mId))
								comments = messages.get(mId).getMessage();
						}
					}
				}
			}
			
			return new Register(regId, registerName, registerValue, symbol, comments);
		} catch (Exception e) {
			return null;
		}
	}
}
