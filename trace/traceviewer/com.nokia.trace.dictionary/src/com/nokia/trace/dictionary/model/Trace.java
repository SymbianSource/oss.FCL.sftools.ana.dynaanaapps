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
 * Trace element
 *
 */
package com.nokia.trace.dictionary.model;

/**
 * Trace element
 * 
 */
public class Trace extends DecodeObject {

	/**
	 * Group this trace belongs to
	 */
	private TraceGroup group;

	/**
	 * Trace data containing list of decode parameters of this trace
	 */
	private TraceData traceData;

	/**
	 * Location of this trace
	 */
	private Location location;

	/**
	 * Line number where this trace is defined
	 */
	private int lineNumber;

	/**
	 * Method name where this trace is defined
	 */
	private String methodName;

	/**
	 * Class name where this string is defined
	 */
	private String className;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            id of the trace
	 * @param name
	 *            name of the trace
	 * @param traceData
	 *            trace data
	 * @param location
	 *            trace location
	 * @param lineNum
	 *            line number where trace is defined
	 * @param methodName
	 *            method name where trace is defined
	 * @param className
	 *            class name where trace is defined
	 * @param group
	 *            group where trace belongs to
	 */
	public Trace(int id, String name, TraceData traceData, Location location,
			int lineNum, String methodName, String className, TraceGroup group) {
		super(id, name);
		this.traceData = traceData;
		this.location = location;
		this.lineNumber = lineNum;
		this.methodName = methodName;
		this.className = className;
		this.group = group;
	}

	/**
	 * Gets the class name
	 * 
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the class name
	 * 
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets the line number
	 * 
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets the line number
	 * 
	 * @param lineNumber
	 *            the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Gets the location
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Sets the location
	 * 
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Gets the method name
	 * 
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets the method name
	 * 
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets the trace data
	 * 
	 * @return the traceData
	 */
	public TraceData getTraceData() {
		return traceData;
	}

	/**
	 * Sets the trace data
	 * 
	 * @param traceData
	 *            the traceData to set
	 */
	public void setTraceData(TraceData traceData) {
		this.traceData = traceData;
	}

	/**
	 * Gets the parent group
	 * 
	 * @return the group
	 */
	public TraceGroup getGroup() {
		return group;
	}

}
