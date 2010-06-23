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
 * Property Map class stores multiple string mappings
 *
 */
package com.nokia.trace.eventrouter;

import java.util.ArrayList;

/**
 * Property Map class stores multiple string mappings
 * 
 */
public class PropertyMap {

	/**
	 * ArrayList containing keys
	 */
	private ArrayList<String> keys;

	/**
	 * ArrayList containing values
	 */
	private ArrayList<String> values;

	/**
	 * Constructor
	 */
	public PropertyMap() {
		keys = new ArrayList<String>();
		values = new ArrayList<String>();
	}

	/**
	 * Inserts string pair to the map
	 * 
	 * @param key
	 *            key to insert
	 * @param value
	 *            value to insert
	 */
	public void put(String key, String value) {
		keys.add(key);
		values.add(value);
	}

	/**
	 * Gets key from index
	 * 
	 * @param index
	 *            index
	 * @return key if index is valid, null otherwise
	 */
	public String getKey(int index) {
		String key = null;
		if (index >= 0 && index < keys.size()) {
			key = keys.get(index);
		}
		return key;
	}

	/**
	 * Gets value from index
	 * 
	 * @param index
	 *            index
	 * @return value if index is valid, null otherwise
	 */
	public String get(int index) {
		String value = null;
		if (index >= 0 && index < values.size()) {
			value = values.get(index);
		}
		return value;
	}

	/**
	 * Gets value with key. If there is more than one value with the same key,
	 * the first one found from the map is returned
	 * 
	 * @param key
	 *            key
	 * @return value if key found, null otherwise
	 */
	public String get(String key) {
		String value = null;
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				value = values.get(i);
				break;
			}
		}
		return value;
	}

	/**
	 * Returns the size of the map
	 * 
	 * @return size of the map
	 */
	public int size() {
		return keys.size();
	}

	/**
	 * Removes a String pair from the map with the key. If there is more than
	 * one occurrence, only the first one if removed
	 * 
	 * @param key
	 *            key to be removed
	 */
	public void remove(String key) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				keys.remove(i);
				values.remove(i);
				break;
			}
		}
	}

}
