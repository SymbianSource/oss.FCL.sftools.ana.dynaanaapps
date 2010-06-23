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
 * Trace Activation Group Item
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.util.ArrayList;

/**
 * Trace Activation Group Item
 * 
 */
public class TraceActivationGroupItem {

	/**
	 * Parent component
	 */
	private TraceActivationComponentItem parent;

	/**
	 * Name of the group
	 */
	private String name;

	/**
	 * ID of the group
	 */
	private int id;

	/**
	 * Activation info of the group
	 */
	private boolean activated;

	/**
	 * Traces inside this group
	 */
	private ArrayList<TraceActivationTraceItem> traces;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent of this item
	 * @param id
	 *            id of the item
	 * @param name
	 *            name of the item
	 */
	public TraceActivationGroupItem(TraceActivationComponentItem parent,
			int id, String name) {
		traces = new ArrayList<TraceActivationTraceItem>();
		this.parent = parent;
		this.id = id;
		this.name = name;

		parent.addGroup(this);
	}

	/**
	 * Get traces of this group
	 * 
	 * @return the traces
	 */
	public ArrayList<TraceActivationTraceItem> getTraces() {
		return traces;
	}

	/**
	 * Adds a new group for this component
	 * 
	 * @param trace
	 *            trace
	 */
	public void addTrace(TraceActivationTraceItem trace) {
		traces.add(trace);
	}

	/**
	 * Returns activation status of the group
	 * 
	 * @return the activated
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Sets activation status
	 * 
	 * @param activated
	 *            the activated to set
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets parent
	 * 
	 * @return the parent
	 */
	public TraceActivationComponentItem getParent() {
		return parent;
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
	 * Sets ID
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
}
