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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import java.util.*;

/**
 * A register bit class. Contains information about a single
 * bit. Used e.g. in Crash Visualiser's CPSR details group. 
 * One of these bits represents a single bit-textbox in CPSR 
 * details UI group.
 *
 */
public final class RegisterBit {
	
	// XML tags
	final static String ATTRIBUTE_INDEX = "index";
	final static String ATTRIBUTE_VALUE = "value";
	final static String ATTRIBUTE_CATEGORY = "category";
	final static String ATTRIBUTE_INTERPRETATION = "interpretation";
	final static String ATTRIBUTE_START = "start";
	final static String ATTRIBUTE_END = "end";
	final static String ATTRIBUTE_CHAR = "char";
	final static String ATTRIBUTE_TYPE = "type";

	// Data
	private final int registerIndex;
	private final String registerValue;
	private final String registerCategory;
	private final String registerInterpretation;
	private String registerChar;
	
	/**
	 * Constructor
	 * @param index bit index
	 * @param value bit value
	 * @param category bit category
	 * @param interpretation bit interpretation
	 * @param regChar bit register character
	 */
	private RegisterBit(int index, String value, String category, String interpretation, String regChar) {
		registerIndex = index;
		registerValue = value;
		registerCategory = category;
		registerInterpretation = interpretation;
		registerChar = regChar;
	}
	
	/**
	 * Constructor
	 * @param index bit index
	 * @param value bit value
	 * @param category bit category
	 * @param interpretation bit interpretation
	 * @param reserved bit is of reserved type
	 */
	private RegisterBit(int index, String value, String category, String interpretation, boolean reserved) {
		registerIndex = index;
		registerValue = value;
		registerCategory = category;
		registerInterpretation = interpretation;
		registerChar = value;
		if (reserved)
			registerChar = "-";
	}

	public int getIndex() {
		return registerIndex;
	}
	
	public String getValue() {
		return registerValue;
	}
	
	public String getCategory() {
		return registerCategory;
	}
	
	public String getInterpretation() {
		return registerInterpretation;
	}
	
	public String getRegisterChar() {
		return registerChar;
	}
	
	/**
	 * Reads and creates bit from bit xml node
	 * @param bit
	 * @return created bit or null
	 */
	public static RegisterBit readFromBit(Node bit) {
		NamedNodeMap attributes = bit.getAttributes();
		if (attributes == null || attributes.getLength() < 1) 
			return null;
		
		String index = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_INDEX));
		if (index == null)
			return null;
		
		int i;
		try {
			i = Integer.parseInt(index);
		} catch (Exception e) {
			return null;
		}
		
		String value = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_VALUE));
		if (value == null)
			return null;

		String category = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_CATEGORY));
		if (category == null)
			return null;

		String interpretation = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_INTERPRETATION));
		if (interpretation == null)
			interpretation = "";
		
		String regChar = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_CHAR));
		if (regChar == null)
			regChar = "";

		return new RegisterBit(i, value, category, interpretation, regChar);
	}
	
	/**
	 * Reads and creates a range of bits from range xml node
	 * @param range
	 * @return list of created bits or null
	 */
	public static List<RegisterBit> readFromRange(Node range) {
		NamedNodeMap attributes = range.getAttributes();
		if (attributes == null || attributes.getLength() < 1) 
			return null;

		String start = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_START));
		if (start == null)
			return null;
		
		String end = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_END));
		if (end == null)
			return null;
		
		String value = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_VALUE));
		if (value == null)
			return null;

		String category = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_CATEGORY));
		if (category == null)
			return null;

		String interpretation = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_INTERPRETATION));
		if (interpretation == null)
			interpretation = "";
		
		boolean reserved = false;
		String type = XmlUtils.getNodeValue(attributes.getNamedItem(ATTRIBUTE_TYPE));
		if (type != null && !"".equals(type))
			reserved = true;

		List<RegisterBit> bits = new ArrayList<RegisterBit>();
		
		try {
			int s = Integer.parseInt(start);
			int e = Integer.parseInt(end);
			for (int i = 0;s <= e; s++, i++) {
				bits.add(new RegisterBit(s,value.substring(i,i+1),category,interpretation,reserved));
			}
		} catch (Exception e) {
			return null;
		}
		
		return bits;
	}
}
