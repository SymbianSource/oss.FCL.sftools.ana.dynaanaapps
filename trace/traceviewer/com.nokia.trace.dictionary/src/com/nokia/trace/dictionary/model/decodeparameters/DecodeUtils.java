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
 * Decode Utils
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;

/**
 * Decode Utils
 * 
 */
final class DecodeUtils {

	/**
	 * Prevents construction
	 */
	private DecodeUtils() {
	}

	/**
	 * Gets integer from buffer
	 * 
	 * @param buffer
	 *            dataBuffer
	 * @param offset
	 *            offset of the data
	 * @param size
	 *            size of the data
	 * @return integer read from buffer
	 */
	static int getIntegerFromBuffer(ByteBuffer buffer, int offset, int size) {
		int data = 0;
		try {
			if (size == 1) {
				data |= buffer.get(offset);
			} else if (size == 2) {
				data |= buffer.getShort(offset) & 0xFFFF;
				data = Short.reverseBytes((short) data);
			} else if (size == 4) {
				data = buffer.getInt(offset);
				data = Integer.reverseBytes(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Gets long from buffer
	 * 
	 * @param buffer
	 *            dataBuffer
	 * @param offset
	 *            offset of the data
	 * @param size
	 *            size of the data
	 * @return long read from buffer
	 */
	static long getLongFromBuffer(ByteBuffer buffer, int offset, int size) {
		long data = 0;
		try {
			if (size == 1) {
				data |= buffer.get(offset) & 0xFF;
			} else if (size == 2) {
				data |= buffer.getShort(offset) & 0xFFFF;
				data = Short.reverseBytes((short) data);
			} else if (size == 4) {
				data = buffer.getInt(offset);
				data = Integer.reverseBytes((int) data);
			} else if (size == 8) {
				data |= buffer.getLong(offset) & 0xFFFFFFFFFFFFFFFFL;
				data = Long.reverseBytes(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}
