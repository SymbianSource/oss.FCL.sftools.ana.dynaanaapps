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
 * Trace activation trace item
 *
 */
package com.nokia.traceviewer.engine.activation;

/**
 * Trace activation trace item
 * 
 */
public class TraceActivationTraceItem {

	/**
	 * Parent group
	 */
	private TraceActivationGroupItem parent;

	/**
	 * Name of the group
	 */
	private String name;

	/**
	 * ID of the group
	 */
	private int id;

	/**
	 * Parameter count
	 */
	private int parameterCount;

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
	public TraceActivationTraceItem(TraceActivationGroupItem parent, int id,
			String name) {
		this.parent = parent;
		this.id = id;
		this.name = name;

		parent.addTrace(this);
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
	public TraceActivationGroupItem getParent() {
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

	/**
	 * Gets parameter count of the traces
	 * 
	 * @return parameter count of the traces
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * Sets parameter count of the traces
	 * 
	 * @param parameterCount
	 *            parameter count
	 */
	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}

}
