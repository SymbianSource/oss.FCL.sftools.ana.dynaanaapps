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
 * VariableTracing Event contains information about events when the value of variable changes
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import com.nokia.traceviewer.engine.TraceInformation;

/**
 * VariableTracing Event contains information about events when the value of
 * variable changes
 */
public class VariableTracingEvent {

	/**
	 * Parent VariableTracing item
	 */
	private final VariableTracingItem parent;

	/**
	 * Timestamp string
	 */
	private final String timestamp;

	/**
	 * Value of the variable in this event
	 */
	private final String value;

	/**
	 * Line where event occurred
	 */
	private int line;

	/**
	 * Trace information included in the event
	 */
	private final TraceInformation traceInformation;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent item
	 * @param timestamp
	 *            timestamp of the event
	 * @param value
	 *            value of the event
	 * @param line
	 *            line where event happened
	 * @param traceInformation
	 *            trace information of the event
	 */
	public VariableTracingEvent(VariableTracingItem parent, String timestamp,
			String value, int line, TraceInformation traceInformation) {
		this.parent = parent;
		this.timestamp = timestamp;
		this.value = value;
		this.line = line;
		this.traceInformation = traceInformation;
	}

	/**
	 * Gets the line where event occurred
	 * 
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Sets the line where event occurred
	 * 
	 * @param line
	 *            the line to set
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * Gets trace information
	 * 
	 * @return the traceInformation
	 */
	public TraceInformation getTraceInformation() {
		return traceInformation;
	}

	/**
	 * Gets the value of the variable
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets parent variableTracing item
	 * 
	 * @return the parent
	 */
	public VariableTracingItem getParent() {
		return parent;
	}

	/**
	 * Gets the timestamp
	 * 
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

}
