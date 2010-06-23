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
 * Integer parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Integer parameter
 * 
 */
public final class IntegerParameter extends DecodeParameter {

	/**
	 * Size of the integer
	 */
	private int size;

	/**
	 * Signed or not
	 */
	private boolean signed;

	/**
	 * Format to how many characters
	 */
	private int formatToChars;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 * @param size
	 *            size
	 * @param signed
	 *            signed
	 */
	public IntegerParameter(String type, boolean hidden, int size,
			boolean signed) {
		super(type, hidden);
		this.size = size;
		this.signed = signed;
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
				String integer = Integer.toString(data);
				addValue(traceString, parameterList, integer);
			} else if (size == TWO_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						TWO_BYTES);
				String integer = Integer.toString(data);
				addValue(traceString, parameterList, integer);
			} else if (size == FOUR_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						FOUR_BYTES);
				String integer = Integer.toString(data);
				addValue(traceString, parameterList, integer);
			} else if (size == EIGHT_BYTES) {
				long bigData = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						EIGHT_BYTES);
				String integer = Long.toString(bigData);
				addValue(traceString, parameterList, integer);
			}
		}
		offset = offset + size;

		return offset;
	}

	/**
	 * Adds the value
	 * 
	 * @param traceString
	 *            trace buffer
	 * @param parameterList
	 *            parameter list
	 * @param integer
	 *            the value to be added
	 */
	private void addValue(StringBuffer traceString, List<String> parameterList,
			String integer) {

		// If we need to add starting zeros
		if (formatToChars != 0) {
			int intLen = integer.length();
			if (intLen < formatToChars) {

				// Create a buffer for the original string and the starting
				// zeros
				StringBuffer buf = new StringBuffer(intLen
						+ (formatToChars - intLen));

				// Append zeros
				while (intLen < formatToChars) {
					buf.append('0');
					intLen++;
				}

				// Append original string
				buf.append(integer);
				integer = buf.toString();
			}
		}

		traceString.append(integer);
		parameterList.add(integer);
	}

	/**
	 * Gets the signed value
	 * 
	 * @return the signed
	 */
	public boolean isSigned() {
		return signed;
	}

	/**
	 * Sets the signed value
	 * 
	 * @param signed
	 *            the signed to set
	 */
	public void setSigned(boolean signed) {
		this.signed = signed;
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

	/**
	 * Sets to how many characters should the parameter be formatted to
	 * 
	 * @param formatToChars
	 *            the number of characters
	 */
	public void setFormatToChars(int formatToChars) {
		this.formatToChars = formatToChars;
	}

}
