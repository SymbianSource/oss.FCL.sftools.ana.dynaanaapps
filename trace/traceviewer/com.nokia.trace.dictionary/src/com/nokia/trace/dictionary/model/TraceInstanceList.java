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
 * Holds trace instances for a trace structure
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Holds trace instances for a trace structure
 * 
 */
public class TraceInstanceList extends DecodeObject {

	/**
	 * Trace list
	 */
	private ArrayList<Trace> traces;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Id
	 * @param name
	 *            Name
	 */
	public TraceInstanceList(int id, String name) {
		super(id, name);
		traces = new ArrayList<Trace>();
	}

	/**
	 * Add trace
	 * 
	 * @param trace
	 *            new trace
	 */
	public void addTrace(Trace trace) {
		traces.add(trace);
	}

	/**
	 * Sets metadata to all the traces in the list. Is called when trace tag
	 * ends in dictionary file.
	 */
	public void setMetadataToTraces() {
		if (getMetadata() != null) {
			Iterator<Trace> it = traces.iterator();
			while (it.hasNext()) {
				Trace trace = it.next();
				if (trace.metadataMap == null) {
					trace.metadataMap = new HashMap<String, HashMap<String, Object>>();
				}
				addMissingMetadataToList(getMetadata(), trace.metadataMap);
			}
		}
	}

	/**
	 * Adds missing metadata to list
	 * 
	 * @param oldList
	 *            old list to append from
	 * @param newList
	 *            new list to append to
	 */
	private void addMissingMetadataToList(
			HashMap<String, HashMap<String, Object>> oldList,
			HashMap<String, HashMap<String, Object>> newList) {
		Iterator<String> it = oldList.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();

			// Doesn't contain this key, add it
			if (!newList.containsKey(key)) {
				newList.put(key, oldList.get(key));

				// Contains the key, check values inside
			} else {
				Iterator<String> valueIt = oldList.get(key).keySet().iterator();
				while (valueIt.hasNext()) {
					String valueKey = valueIt.next();

					// Doesn't contain value key, add it
					if (!newList.get(key).containsKey(valueKey)) {
						newList.get(key).put(valueKey,
								newList.get(key).get(valueKey));
					}
				}
			}
		}
	}
}
