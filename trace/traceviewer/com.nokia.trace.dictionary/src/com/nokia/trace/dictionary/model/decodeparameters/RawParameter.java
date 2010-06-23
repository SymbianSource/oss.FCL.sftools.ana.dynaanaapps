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
 * Raw parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Raw parameter
 * 
 */
public class RawParameter extends DecodeParameter {

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 */
	public RawParameter(String type, boolean hidden) {
		super(type, hidden);
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
}
