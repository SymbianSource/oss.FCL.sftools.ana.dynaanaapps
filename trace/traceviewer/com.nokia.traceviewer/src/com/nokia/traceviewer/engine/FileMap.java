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
 * FileMap class
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold information about trace block positions in file
 * 
 */
public class FileMap {

	/**
	 * Map which holds the file pointers to messages
	 */
	private final List<Long> map;

	/**
	 * Constructor
	 */
	public FileMap() {
		map = new ArrayList<Long>();
	}

	/**
	 * Insert item to map
	 * 
	 * @param item
	 *            item
	 */
	public void insert(Long item) {
		map.add(item);
	}

	/**
	 * Gets item from map
	 * 
	 * @param index
	 *            index of item
	 * @return item
	 */
	public Long getItem(int index) {
		long item = 0;
		if (index < 0) {
			item = 0;
		} else if (index > map.size() - 1) {
			item = 0;
		} else {
			item = map.get(index).longValue();
		}
		return Long.valueOf(item);
	}

	/**
	 * Gets index from map from specific offset
	 * 
	 * @param offset
	 *            offset of item
	 * @return index
	 */
	public int getIndexFromOffset(int offset) {
		int index = (offset / TraceViewerGlobals.blockSize);
		return index;
	}

	/**
	 * Empties map
	 */
	public void clearMap() {
		map.clear();
	}

	/**
	 * Gets size of this map
	 * 
	 * @return size
	 */
	public int size() {
		return map.size();
	}
}
