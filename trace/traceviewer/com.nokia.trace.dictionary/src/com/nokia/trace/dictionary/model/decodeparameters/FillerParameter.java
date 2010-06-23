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
 * Filler parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Filler parameter
 */
public class FillerParameter extends DecodeParameter {

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 */
	public FillerParameter(String type, boolean hidden) {
		super(type, hidden);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter#decode
	 * (java.nio.ByteBuffer, int, java.lang.StringBuffer, int, int,
	 * java.util.ArrayList)
	 */
	@Override
	public int decode(ByteBuffer dataFrame, int offset,
			StringBuffer traceString, int dataStart, int dataLength,
			List<String> parameterList) {

		// Just add offset by one
		offset++;

		return offset;
	}
}
