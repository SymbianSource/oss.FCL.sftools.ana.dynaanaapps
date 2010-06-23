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
 * String parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * String parameter
 * 
 */
public class StringParameter extends DecodeParameter {

	/**
	 * Datablock size
	 */
	private static final int DATABLOCK_SIZE = 4;

	/**
	 * Length of ascii data in XML file
	 */
	private static final int LENGTH_OF_ASCII = 1;

	/**
	 * Length of unicode data in XML file
	 */
	private static final int LENGTH_OF_UNICODE = 2;

	/**
	 * Length in bytes of length data in binary format
	 */
	private static final int LENGTH_OF_STRINGLENGTH = 4;

	/**
	 * String end character
	 */
	private static final char STR_END_CHAR = '\0';

	/**
	 * Length of this string parameter
	 */
	private int length;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden
	 * @param length
	 *            length
	 */
	public StringParameter(String type, boolean hidden, int length) {
		super(type, hidden);
		this.length = length;
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
			postDataMissingEvent(traceString, 0, LENGTH_OF_STRINGLENGTH);
		} else {

			int stringLength;

			// If the only variable in the trace, length can be get by dividing
			// the number of bytes with bytes per character. One for ASCII and
			// two for UNICODE
			if (isOnlyVariableInTrace) {
				stringLength = bytesRemaining / length;

				// Read the length if this string is not the only variable in
				// the trace
			} else {
				stringLength = DecodeUtils.getIntegerFromBuffer(dataFrame,
						offset, LENGTH_OF_STRINGLENGTH);

				// If not enough data left, check if all length bytes seemed to
				// be characters. If yes, add them also the the string
				if (stringLength > bytesRemaining) {
					int a = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
							ONE_BYTE);
					int b = DecodeUtils.getIntegerFromBuffer(dataFrame,
							offset + 1, ONE_BYTE);
					int c = DecodeUtils.getIntegerFromBuffer(dataFrame,
							offset + 2, ONE_BYTE);
					int d = DecodeUtils.getIntegerFromBuffer(dataFrame,
							offset + 3, ONE_BYTE);

					// Go back 4 bytes of length
					if (isAscii(a) && isAscii(b) && isAscii(c) && isAscii(d)) {
						offset = offset - LENGTH_OF_STRINGLENGTH;
					}
				}

				offset = offset + LENGTH_OF_STRINGLENGTH;
			}

			// Set data frame to right position
			dataFrame.position(offset);

			// Read from databuffer
			byte[] byteArr = null;
			String newString = null;

			// Process ASCII string
			if (length == LENGTH_OF_ASCII) {
				bytesRemaining = dataLength - (offset - dataStart);
				if (stringLength > bytesRemaining) {
					stringLength = bytesRemaining;
				}

				// Get bytes and create the String
				if (stringLength > 0) {
					byteArr = new byte[stringLength];
					dataFrame.get(byteArr, 0, stringLength);
					removeLineBreaks(byteArr);
					int stringLenWithoutZeros = removeStringEndChars(byteArr,
							true);
					newString = new String(byteArr, 0, stringLenWithoutZeros);
				}

				// Process UNICODE string
			} else if (length == LENGTH_OF_UNICODE) {
				stringLength = stringLength * LENGTH_OF_UNICODE;
				bytesRemaining = dataLength - (offset - dataStart);
				if (stringLength > bytesRemaining) {
					stringLength = bytesRemaining;
				}

				if (stringLength > 0) {
					// Get bytes and create the String
					byteArr = new byte[stringLength];
					dataFrame.get(byteArr, 0, stringLength);
					removeLineBreaks(byteArr);
					int stringLenWithoutZeros = removeStringEndChars(byteArr,
							false);
					try {
						newString = new String(byteArr, 0,
								stringLenWithoutZeros, "UTF-16LE"); //$NON-NLS-1$
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}

			// String must have been created
			if (newString != null) {
				boolean foundEndChar = false;

				// Find possible end chars
				for (int i = newString.length() - DATABLOCK_SIZE; i < newString
						.length()
						&& i >= 0; i++) {
					char c = newString.charAt(i);
					if (c == STR_END_CHAR) {
						String str = newString.substring(0, i);
						traceString.append(str);
						parameterList.add(str);
						foundEndChar = true;
						break;
					}
				}

				if (!foundEndChar) {
					traceString.append(newString);
					parameterList.add(newString);
				}
			}

			// Check fillers
			int fillerCount = DATABLOCK_SIZE - (stringLength % DATABLOCK_SIZE);
			if (fillerCount == DATABLOCK_SIZE) {
				// All good
			} else {
				// Add fillerCount to offset
				offset = offset + fillerCount;
			}

			// Add string length to offset
			if (stringLength > 0) {
				offset = offset + stringLength;
			}
		}
		return offset;
	}

	/**
	 * Checks if given parameters seems to be ASCII character
	 * 
	 * @param value
	 * @return true if parameter seems to be ASCII
	 */
	private boolean isAscii(int value) {
		boolean isAscii = false;
		if (value >= 32 && value <= 126) {
			isAscii = true;
		}

		return isAscii;
	}

	/**
	 * Removes string end characters
	 * 
	 * @param byteArr
	 *            byte array
	 * @param ascii
	 *            if true, ASCII string. Otherwise UNICODE string
	 * @return length of the string without string end characters
	 */
	private int removeStringEndChars(byte[] byteArr, boolean ascii) {
		int len = byteArr.length;
		boolean firstCharFound = false;

		for (int i = byteArr.length - 1; i >= 0; i--) {
			if (byteArr[i] == '\0') {

				// In case of ASCII, remove one byte at a time
				if (ascii) {
					len--;

					// In case of UNICODE, remove two bytes at a time
				} else {
					if (firstCharFound) {
						len = len - LENGTH_OF_UNICODE;
						firstCharFound = false;
					} else {
						firstCharFound = true;
					}
				}
			} else {
				break;
			}
		}
		return len;
	}

	/**
	 * Removes line breaks from the byte array
	 * 
	 * @param byteArr
	 *            byte array to process
	 */
	private void removeLineBreaks(byte[] byteArr) {

		// Remove line breaks from the buffer
		for (int i = 0; i < byteArr.length; i++) {
			byte b = byteArr[i];
			if (b == '\n' || b == '\r') {
				byteArr[i] = ' ';
			}
		}
	}

	/**
	 * Gets length
	 * 
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets length
	 * 
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

}
