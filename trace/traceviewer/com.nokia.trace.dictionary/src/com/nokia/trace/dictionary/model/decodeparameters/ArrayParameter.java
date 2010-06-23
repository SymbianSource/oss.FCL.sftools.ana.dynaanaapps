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
 * Array parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Array parameter
 * 
 */
public final class ArrayParameter extends DecodeParameter {

	/**
	 * Reference to a type that this array is full of
	 */
	private DecodeParameter parameterType;

	/**
	 * Length in bytes of length data in binary format
	 */
	private static final int LENGTH_OF_ARRAYLENGTH = 4;

	/**
	 * Datablock size
	 */
	private static final int DATABLOCK_SIZE = 4;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param hidden
	 *            hidden value
	 * @param parameterType
	 *            parameterType
	 */
	public ArrayParameter(String type, boolean hidden,
			DecodeParameter parameterType) {
		super(type, hidden);
		this.parameterType = parameterType;
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
			postDataMissingEvent(traceString, 0, LENGTH_OF_ARRAYLENGTH);
		}

		int parameterLen = parameterType.getSize();
		int arrayLength;

		// If the only variable in the trace, length is the data left
		if (isOnlyVariableInTrace) {
			arrayLength = bytesRemaining / parameterLen;

			// Read the length if this array is not the only variable in the
			// trace
		} else {

			arrayLength = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
					LENGTH_OF_ARRAYLENGTH);

			offset = offset + LENGTH_OF_ARRAYLENGTH;
			dataFrame.position(offset);
		}

		List<String> arrayParameterString = new ArrayList<String>();
		StringBuffer arrayParameterBuf = new StringBuffer();

		// Calculate how many elements there are in the array
		int nrOfElements = arrayLength;

		// Decode all the parameters from the array
		for (int i = 0; i < nrOfElements
				&& offset + parameterLen <= dataStart + dataLength; i++) {

			offset = parameterType.decode(dataFrame, offset, traceString,
					dataStart, dataLength, arrayParameterString);

			// Add next array element to array parameter String
			if (arrayParameterString.size() > 0) {
				arrayParameterBuf.append(arrayParameterString.get(0));
				arrayParameterString.clear();
			}

			// Append commas between elements
			if (i != nrOfElements - 1) {
				traceString.append(',');
				arrayParameterBuf.append(',');
			}
		}

		parameterList.add(arrayParameterBuf.toString());

		// Check fillers
		int fillerCount = DATABLOCK_SIZE
				- ((arrayLength * parameterLen) % DATABLOCK_SIZE);
		if (fillerCount == DATABLOCK_SIZE) {
			// All good
		} else {
			// Add fillerCount to offset
			offset = offset + fillerCount;
		}

		return offset;
	}

	/**
	 * Gets parameter type
	 * 
	 * @return the parameterType
	 */
	public DecodeParameter getParameterType() {
		return parameterType;
	}

	/**
	 * Sets parameter type
	 * 
	 * @param parameterType
	 *            the parameterType to set
	 */
	public void setParameterType(DecodeParameter parameterType) {
		this.parameterType = parameterType;
	}
}
