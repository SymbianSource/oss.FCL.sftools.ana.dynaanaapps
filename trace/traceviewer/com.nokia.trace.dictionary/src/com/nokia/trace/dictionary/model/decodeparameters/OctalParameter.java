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
 * Octal parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Octal parameter
 */
public class OctalParameter extends DecodeParameter {

	/**
	 * Size of the octal
	 */
	private int size;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 * @param size
	 *            size
	 */
	public OctalParameter(String type, boolean hidden, int size) {
		super(type, hidden);
		this.size = size;
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
		if (bytesRemaining < size) {
			postDataMissingEvent(traceString, bytesRemaining, size);
		} else {

			int data = 0;
			if (size == ONE_BYTE) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						ONE_BYTE);
				String octalString = Integer.toOctalString(data);
				traceString.append(octalString);
				parameterList.add(octalString);
			} else if (size == TWO_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						TWO_BYTES);
				String octalString = Integer.toOctalString(data);
				traceString.append(octalString);
				parameterList.add(octalString);
			} else if (size == FOUR_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						FOUR_BYTES);
				String octalString = Integer.toOctalString(data);
				traceString.append(octalString);
				parameterList.add(octalString);
			} else if (size == EIGHT_BYTES) {
				long bigData = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						EIGHT_BYTES);
				String octalString = Long.toOctalString(bigData);
				traceString.append(octalString);
				parameterList.add(octalString);
			}
		}
		offset = offset + size;
		return offset;
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
		return size;
	}

	/**
	 * Sets size
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

}
