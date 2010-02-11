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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import java.util.*;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;

/**
 * 
 *
 */
public final class RegisterDetails {
	
	// XML tags
	final static String TAG_DESCRIPTION = "description";
	final static String TAG_ENDIAN = "endian";
	final static String TAG_BIT_RANGE = "bit_range";
	final static String TAG_BIT = "bit";
	final static String ATTRIBUTE_BIT0 = "bit0";
	
	// Data
	private final String registerDescription;
	private final List<RegisterBit> registerBits;
	private final boolean bitsStartFromRight;
	
	/**
	 * Constructor
	 * @param description register description
	 * @param bits list of bits
	 * @param startFromRight bits start from right (true) or from left (false), endianess issue
	 */
	private RegisterDetails(String description, List<RegisterBit> bits, boolean startFromRight) {
		registerDescription = description;
		registerBits = bits;
		bitsStartFromRight = startFromRight;
	}
	
	public String getDescription() {
		return registerDescription;
	}
	
	public boolean bitsStartFromRight() {
		return bitsStartFromRight;
	}
	
	public RegisterBit getBit(int index) {
		RegisterBit bit = null;
		for (int i = 0; i < registerBits.size(); i++) {
			if (registerBits.get(i).getIndex() == index) {
				bit = registerBits.get(i);
				break;
			}
		}
		return bit;
	}
	
	/**
	 * Reads and creates a register details from vientry xml element
	 * @param elementViEntry
	 * @return created register details
	 */
	public static RegisterDetails read(Element elementViEntry) {
		String description = XmlUtils.getTextValue(elementViEntry, TAG_DESCRIPTION);
		if (description == null)
			return null;
		
		boolean bitsStartFromRight = true;
		NodeList endian = elementViEntry.getElementsByTagName(TAG_ENDIAN);
		if (endian != null && endian.getLength() > 0) {
			NamedNodeMap attributes = endian.item(0).getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				Node bit0 = attributes.getNamedItem(ATTRIBUTE_BIT0);
				String bitStart = XmlUtils.getNodeValue(bit0);
				if (bitStart != null && "left".equals(bitStart))
					bitsStartFromRight = false;
			}
		}

		List<RegisterBit> registerBits = new ArrayList<RegisterBit>();

		// read bit ranges
		NodeList bitRanges = elementViEntry.getElementsByTagName(TAG_BIT_RANGE);
		if (bitRanges != null && bitRanges.getLength() > 0) {
			for (int i = 0; i < bitRanges.getLength(); i++) {
				List<RegisterBit> regBits = RegisterBit.readFromRange(bitRanges.item(i));
				if (regBits == null || regBits.size() < 1)
					return null;
				registerBits.addAll(regBits);
			}
		}
		
		// read bits
		NodeList bits = elementViEntry.getElementsByTagName(TAG_BIT);
		if (bits != null && bits.getLength() > 0) {
			for (int i = 0; i < bits.getLength(); i++) {
				RegisterBit regBit = RegisterBit.readFromBit(bits.item(i));
				if (regBit == null)
					return null;
				registerBits.add(regBit);
			}
		}

		return new RegisterDetails(description, registerBits, bitsStartFromRight);
	}
}
