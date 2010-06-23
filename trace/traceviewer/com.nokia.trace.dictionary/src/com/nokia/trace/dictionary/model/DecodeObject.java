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
 * Decode object
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.HashMap;

/**
 * Decode object
 * 
 */
public class DecodeObject {

	/**
	 * ID number
	 */
	private int id;

	/**
	 * Name
	 */
	private String name;

	/**
	 * Hashmap containing metadata for this object
	 */
	protected HashMap<String, HashMap<String, Object>> metadataMap;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            id of this component
	 * @param name
	 *            name of this component
	 */
	public DecodeObject(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Gets ID
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Attaches metadata to this trace
	 * 
	 * @param id
	 *            id of the metadata
	 * @param tagName
	 *            tag name of the metadata
	 * @param metadata
	 *            the metadata object
	 */
	public void addMetadata(String id, String tagName, Object metadata) {
		if (metadataMap == null) {
			metadataMap = new HashMap<String, HashMap<String, Object>>();
		}
		HashMap<String, Object> tagMap = metadataMap.get(id);
		if (tagMap == null) {
			tagMap = new HashMap<String, Object>();
			metadataMap.put(id, tagMap);
		}
		tagMap.put(tagName, metadata);
	}

	/**
	 * Gets metadata map
	 * 
	 * @return the metadata
	 */
	public HashMap<String, HashMap<String, Object>> getMetadata() {
		return metadataMap;
	}
}
