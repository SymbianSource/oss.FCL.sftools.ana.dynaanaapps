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
 * Trace Group
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Trace Group
 */
public class TraceGroup extends DecodeObject {

	/**
	 * Prefix of the group
	 */
	private String prefix;

	/**
	 * Suffix of the group
	 */
	private String suffix;

	/**
	 * Component this groups belongs to
	 */
	private TraceComponent component;

	/**
	 * List of traces in this group
	 */
	private ArrayList<Trace> traces;

	/**
	 * Constructor Should only be called from DecodeObjectFactory
	 * 
	 * @param id
	 * @param name
	 * @param prefix
	 * @param suffix
	 * @param component
	 */
	public TraceGroup(int id, String name, String prefix, String suffix,
			TraceComponent component) {
		super(id, name);
		this.prefix = prefix;
		this.suffix = suffix;
		this.component = component;
		traces = new ArrayList<Trace>();
	}

	/**
	 * Adds trace to the list to right position
	 * 
	 * @param trace
	 *            trace to be added
	 * @return null if adding was ok, old trace if a trace with same ID already
	 *         exists
	 */
	public Trace addTrace(Trace trace) {
		Trace returnTrace = null;
		int pos = Collections.binarySearch(traces, trace,
				new Comparator<Trace>() {

					public int compare(Trace o1, Trace o2) {
						int id1 = o1.getId();
						int id2 = o2.getId();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			traces.add(-pos - 1, trace);
		} else {
			returnTrace = traces.get(pos);
		}
		return returnTrace;

	}

	/**
	 * Gets trace for specific id
	 * 
	 * @param id
	 *            id of the trace
	 * @return the trace
	 */
	public Trace getTrace(int id) {
		int pos = Collections.binarySearch(traces, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((Trace) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		Trace trace = null;
		if (pos >= 0) {
			trace = traces.get(pos);
		}
		return trace;

	}

	/**
	 * Gets all traces as a list
	 * 
	 * @return all traces
	 */
	public ArrayList<Trace> getTraces() {
		return traces;
	}

	/**
	 * Gets prefix
	 * 
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets prefix
	 * 
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Gets suffix
	 * 
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * Sets suffix
	 * 
	 * @param suffix
	 *            the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Gets parent component
	 * 
	 * @return the component
	 */
	public TraceComponent getComponent() {
		return component;
	}

}
