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
 * Trace Metadata contains metadata for this trace
 *
 */
package com.nokia.traceviewer.engine;

import java.util.HashMap;

/**
 * Trace Metadata contains metadata for this trace
 * 
 */
public class TraceMetaData {

	/**
	 * Path to this trace
	 */
	private String path;

	/**
	 * Line number where trace is defined
	 */
	private int lineNumber;

	/**
	 * Class name
	 */
	private String className;

	/**
	 * Method name
	 */
	private String methodName;

	/**
	 * Metadata
	 */
	private HashMap<String, HashMap<String, Object>> metaData;

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
	 * Gets the path
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets class name
	 * 
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets class name
	 * 
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets method name
	 * 
	 * @return the method Name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets method name
	 * 
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets metadata map
	 * 
	 * @return the metaData map
	 */
	public HashMap<String, HashMap<String, Object>> getMetaData() {
		return metaData;
	}

	/**
	 * Sets metadata map
	 * 
	 * @param metaData
	 *            the metaData map
	 */
	public void setMetaData(HashMap<String, HashMap<String, Object>> metaData) {
		this.metaData = metaData;
	}
}
