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
 * Compound parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Compound parameter
 * 
 */
public class CompoundParameter extends DecodeParameter {

	/**
	 * List of parameter types
	 */
	private ArrayList<DecodeParameter> parameters;

	/**
	 * Name of the parameter types
	 */
	private ArrayList<String> parameterNames;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 */
	public CompoundParameter(String type, boolean hidden) {
		super(type, hidden);
		parameters = new ArrayList<DecodeParameter>();
		parameterNames = new ArrayList<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter#decode
	 * (java.nio.ByteBuffer, int, java.lang.StringBuffer, int, int,
	 * java.util.List)
	 */
	@Override
	public int decode(ByteBuffer dataFrame, int offset,
			StringBuffer traceString, int dataStart, int dataLength,
			List<String> parameterList) {
		// Check that there is enough data left in the buffer
		int bytesRemaining = dataLength - (offset - dataStart);
		if (bytesRemaining <= 0) {
			postDataMissingEvent(traceString, 0, 4);
		}
		return offset;
	}

	/**
	 * Adds new decode parameter to list
	 * 
	 * @param parameter
	 *            the parameter
	 * @param name
	 *            the name of the parameter
	 */
	public void addParameter(DecodeParameter parameter, String name) {
		parameters.add(parameter);
		parameterNames.add(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter#getSize
	 * ()
	 */
	@Override
	public int getSize() {
		// Not implemented yet
		return 0;
	}
}
