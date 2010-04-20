/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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
*/package com.nokia.s60tools.crashanalyser.containers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class OstTraceLine {
	// XML tags
	public static final String TAG_LINE              = "line";
	public static final String ATTRIBUTE_TYPE        = "type";
	public static final String ATTRIBUTE_TIMESTAMP   = "timestamp";
	public static final String ATTRIBUTE_PREFIX      = "prefix";
	public static final String ATTRIBUTE_COMPONENT   = "component";
	public static final String ATTRIBUTE_GROUP       = "group";
	public static final String ATTRIBUTE_ID          = "id";
	public static final String ATTRIBUTE_FILE        = "file";
	public static final String ATTRIBUTE_LINE_NUMBER = "line_number";
	public static final String ATTRIBUTE_CONTEXT_ID  = "context_id";
	
	
	private final String type;
	private final String timestamp;
	private final String prefix;
	private final String component;
	private final String group;
	private final String id;
	private final String file;
	private final String lineNumber;
	private final String contextId;
	private final String traceText;
	
	 
	/**
	 * Constructor
	 * @param aType type
	 * @param aTimestamp timestamp
	 * @param aPrefix prefix
	 * @param aComponent component
	 * @param aGroup group
	 * @param aId id
	 * @param aFile file
	 * @param aLineNumber line number
	 * @param aContextId context id
	 * 
	 */
	private OstTraceLine(String aType, String aTimestamp, String aPrefix,
			String aComponent, String aGroup, String aId, String aFile, 
			String aLineNumber, String aContextId, String aTraceText) {
		type = aType;
		timestamp = aTimestamp;
		prefix = aPrefix;
		component = aComponent;
		group = aGroup;
		id = aId;
		file = aFile;
		lineNumber = aLineNumber;
		contextId = aContextId;
		traceText = aTraceText;
	}
	
	
	/**
	 * Reads trace from an XML element
	 * @param elementTraceLine One line element
	 * @return a created OstTrace class or null
	 */
	public static OstTraceLine read(Node elementTraceLine) {

		if (elementTraceLine.getNodeName().equals(TAG_LINE)) {
			String traceTextStr = elementTraceLine.getFirstChild().getNodeValue();			
			NamedNodeMap attributes = elementTraceLine.getAttributes();
			if(attributes != null) {
				String typeStr = getAttributeStr(attributes, ATTRIBUTE_TYPE);
				String timestampStr = getAttributeStr(attributes, ATTRIBUTE_TIMESTAMP);
				String prefixStr = getAttributeStr(attributes, ATTRIBUTE_PREFIX);
				String componentStr = getAttributeStr(attributes, ATTRIBUTE_COMPONENT);
				String groupStr = getAttributeStr(attributes, ATTRIBUTE_GROUP);
				String idStr = getAttributeStr(attributes, ATTRIBUTE_ID);
				String fileStr = getAttributeStr(attributes, ATTRIBUTE_FILE);
				String lineNumberStr = getAttributeStr(attributes, ATTRIBUTE_LINE_NUMBER);
				String contextIdStr = getAttributeStr(attributes, ATTRIBUTE_CONTEXT_ID);
								
			return new OstTraceLine(typeStr, timestampStr, prefixStr, componentStr, groupStr, idStr, 
				fileStr, lineNumberStr, contextIdStr, traceTextStr);
			}
		}
		return null;
	}

	public String getType() {
		return type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getComponent() {
		return component;
	}

	public String getGroup() {
		return group;
	}

	public String getId() {
		return id;
	}

	public String getFile() {
		return file;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public String getContextId() {
		return contextId;
	}

	public String getTraceText() {
		return traceText;
	}		

	private static String getAttributeStr(NamedNodeMap attributes, String tag) {
		Node node = attributes.getNamedItem(tag);
		if (node != null) {
			return node.getNodeValue();
		}
		return "";
	}
}
