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

import java.util.Map;
import java.io.*;
import org.w3c.dom.*;
import com.nokia.s60tools.crashanalyser.model.*;

/**
 * Contains symbol data.
 *
 */
public final class Symbol {
	
	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "name";
	public static final String TAG_OBJECT = "object";
	public static final String TAG_ATTRIBUTES = "attributes";
	public static final String TAG_XIP = "xip";
	public static final String TAG_LINK = "link";
	
	// Data
	private final int symbolId;
	private final String symbolSource;
	private final String symbolName;
	private final String symbolObject;
	private final String symbolCodeSegmentName;
	private final boolean xip;
	
	private Symbol(int id,String source, String name, String object, String codeSegmentName, boolean attributeXip) {
		symbolId = id;
		symbolSource = source;
		symbolName = name;
		symbolObject = object;
		symbolCodeSegmentName = codeSegmentName;
		xip = attributeXip;
	}
	
	public int getId() {
		return symbolId;
	}
	
	public String getName() {
		return symbolName;
	}
	
	public String getObject() {
		return symbolObject;
	}
	
	public String getSource() {
		return symbolSource;
	}
	
	public boolean xip() {
		return xip;
	}
	
	public String getCodeSegmentName() {
		return symbolCodeSegmentName;
	}
	
	/**
	 * Reads and creates a symbol from symbol xml element
	 * @param elementSymbol
	 * @param source
	 * @param codeSegments
	 * @return created symbol
	 */
	public static Symbol read(Element elementSymbol, String source, Map<Integer, CodeSegment> codeSegments) {
		String symbolId = XmlUtils.getTextValue(elementSymbol, TAG_ID);
		if (symbolId == null)
			return null;
		
		int id;
		try {
			id = Integer.parseInt(symbolId);
		} catch (Exception e) {
			return null;
		}

		String symbolName = XmlUtils.getTextValue(elementSymbol, TAG_NAME);
		if (symbolName == null)
			symbolName = "";
		
		String symbolObject = XmlUtils.getTextValue(elementSymbol, TAG_OBJECT);
		if (symbolObject == null)
			symbolObject = "";
		
		boolean xip = false;
		NodeList attribute = elementSymbol.getElementsByTagName(TAG_ATTRIBUTES);
		if (attribute != null && attribute.getLength() > 0) {
			xip = XmlUtils.containsNode((Element)attribute.item(0), TAG_XIP);
		}
		
		String codeSegmentName = "";
		NodeList links = elementSymbol.getElementsByTagName(TAG_LINK);
		if (links != null && links.getLength() > 0) {
			String link = XmlUtils.getNodeValue(links.item(0));
			if (link != null && !"".equals(link)) {
				try {
					int codeSegmentId = Integer.parseInt(link);
					if (codeSegments.containsKey(codeSegmentId)) {
						CodeSegment cs = codeSegments.get(codeSegmentId);
						File f = new File(cs.getSegmentName());
						codeSegmentName = f.getName();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return new Symbol(id, source, symbolName, symbolObject, codeSegmentName, xip);
	}
}
