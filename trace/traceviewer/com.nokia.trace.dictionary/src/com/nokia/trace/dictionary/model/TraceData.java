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
 * Trace Data
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;

import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;

/**
 * Trace Data
 * 
 */
public final class TraceData {

	/**
	 * ID of the trace data
	 */
	private int id;

	/**
	 * Type of the data
	 */
	private String type;

	/**
	 * Indicates that this trace contains only one variable
	 */
	private boolean containsOnlyOneVariable;

	/**
	 * Decode parameters
	 */
	private ArrayList<DecodeParameter> decodeParameters;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the trace ID
	 * @param type
	 *            the trace type
	 */
	public TraceData(int id, String type) {
		this.id = id;
		this.type = type;
		decodeParameters = new ArrayList<DecodeParameter>();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets type
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Adds decode parameters to list
	 * 
	 * @param parameter
	 */
	public void addDecodeParameter(DecodeParameter parameter) {
		decodeParameters.add(parameter);
	}

	/**
	 * Gets decode parameters
	 * 
	 * @return the decodeParameters
	 */
	public ArrayList<DecodeParameter> getDecodeParameters() {
		return decodeParameters;
	}

	/**
	 * Gets the contains only one variable value
	 * 
	 * @return the containsOnlyOneVariable
	 */
	public boolean containsOnlyOneVariable() {
		return containsOnlyOneVariable;
	}

	/**
	 * Sets the contains only one variable value
	 * 
	 * @param containsOnlyOneVariable
	 *            the new value
	 */
	public void setContainsOnlyOneVariable(boolean containsOnlyOneVariable) {
		this.containsOnlyOneVariable = containsOnlyOneVariable;
	}
}
