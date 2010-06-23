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
 * Float parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Float parameter
 */
public final class FloatParameter extends DecodeParameter {

	/**
	 * Size of the float
	 */
	private int size;

	/**
	 * Signed or not
	 */
	private boolean signed;

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
	public FloatParameter(String type, boolean hidden, int size, boolean signed) {
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

			long data = 0;
			if (size == TWO_BYTES) {
				data = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						TWO_BYTES);
				double floatData = Double.longBitsToDouble(data);
				String floatStr = Double.toString(floatData);
				traceString.append(floatStr);
				parameterList.add(floatStr);
			} else if (size == FOUR_BYTES) {
				data = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						FOUR_BYTES);
				double floatData = Double.longBitsToDouble(data);
				String floatStr = Double.toString(floatData);
				traceString.append(floatStr);
				parameterList.add(floatStr);
			} else if (size == EIGHT_BYTES) {
				data = DecodeUtils.getLongFromBuffer(dataFrame, offset,
						EIGHT_BYTES);
				double floatData = Double.longBitsToDouble(data);
				String floatStr = Double.toString(floatData);
				traceString.append(floatStr);
				parameterList.add(floatStr);
			}
		}
		offset = offset + size;
		return offset;
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

}
