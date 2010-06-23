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
 * Hex parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Hex parameter
 * 
 */
public final class HexParameter extends DecodeParameter {

	/**
	 * Size of the hex
	 */
	private int size;

	/**
	 * Print in upper case boolean
	 */
	private boolean printInUpperCase;

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
	 */
	public HexParameter(String type, boolean hidden, int size) {
		super(type, hidden);
		this.size = size;
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
		// Check that there is enough data left in the buffer
		int bytesRemaining = dataLength - (offset - dataStart);
		if (bytesRemaining < size) {
			postDataMissingEvent(traceString, bytesRemaining, size);
		} else {
			int data = 0;
			int numberOfChars = formatToChars;
			if (numberOfChars == 0) {
				numberOfChars = size * 2;
			}

			// One byte
			if (size == ONE_BYTE) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						ONE_BYTE);
				String hexStr = Integer.toHexString(data);
				addValue(traceString, parameterList, numberOfChars, hexStr,
						size);

				// Two bytes
			} else if (size == TWO_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						TWO_BYTES);
				String hexStr = Integer.toHexString(data);
				addValue(traceString, parameterList, numberOfChars, hexStr,
						size);

				// Four bytes
			} else if (size == FOUR_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						FOUR_BYTES);
				String hexStr = Integer.toHexString(data);
				addValue(traceString, parameterList, numberOfChars, hexStr,
						size);

				// Eight bytes
			} else if (size == EIGHT_BYTES) {
				long bigData = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						EIGHT_BYTES);
				String hexStr = Long.toHexString(bigData);
				addValue(traceString, parameterList, numberOfChars, hexStr,
						size);
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
	 * @param numberOfChars
	 *            number of characters where this hex should be fitted
	 * @param hexStr
	 *            the value to be added
	 * @param size
	 *            size of the parameter
	 */
	private void addValue(StringBuffer traceString, List<String> parameterList,
			int numberOfChars, String hexStr, int size) {

		// If size 1 or 2 bytes, the value can be like 0xFFFFFF10. Let's remove
		// the FFFFFF.
		if (hexStr.length() > size * 2) {
			hexStr = hexStr.substring(hexStr.length() - (size * 2));
		}

		// Upper case
		if (printInUpperCase) {
			hexStr = hexStr.toUpperCase();
		}

		int hexLen = hexStr.length();
		if (hexLen < numberOfChars) {

			// Create a buffer for the original string and the starting
			// zeros
			StringBuffer buf = new StringBuffer(hexLen
					+ (numberOfChars - hexLen));

			// Append zeros
			while (hexLen < numberOfChars) {
				buf.append('0');
				hexLen++;
			}

			// Append original string
			buf.append(hexStr);
			hexStr = buf.toString();
		}

		traceString.append(hexStr);
		parameterList.add(hexStr);
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
	 * Gets print in upper case boolean
	 * 
	 * @return true if this hex parameter should be printed in upper case
	 */
	public boolean getPrintInUpperCase() {
		return printInUpperCase;
	}

	/**
	 * Sets print in upper case boolean
	 * 
	 * @param printInUpperCase
	 *            the new value
	 */
	public void setPrintInUpperCase(boolean printInUpperCase) {
		this.printInUpperCase = printInUpperCase;
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
