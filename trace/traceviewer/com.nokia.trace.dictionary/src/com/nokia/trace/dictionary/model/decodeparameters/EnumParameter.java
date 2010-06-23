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
 * Enum parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * Enum parameter
 */
public class EnumParameter extends DecodeParameter {

	/**
	 * Size of the enum
	 */
	private int size;

	/**
	 * List of Enum members
	 */
	private ArrayList<EnumMember> members;

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
	public EnumParameter(String type, boolean hidden, int size) {
		super(type, hidden);
		this.size = size;
		members = new ArrayList<EnumMember>();
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
			if (size == ONE_BYTE) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						ONE_BYTE);
			} else if (size == TWO_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						TWO_BYTES);
			} else if (size == FOUR_BYTES) {
				data = DecodeUtils.getIntegerFromBuffer(dataFrame, offset,
						FOUR_BYTES);
			}
			String memberName = getMemberName(data);
			traceString.append(memberName);
			parameterList.add(memberName);
		}
		offset = offset + size;
		return offset;
	}

	/**
	 * Adds enum member to this parameter
	 * 
	 * @param member
	 */
	public void addMember(EnumMember member) {
		int pos = Collections.binarySearch(members, member,
				new Comparator<EnumMember>() {

					public int compare(EnumMember o1, EnumMember o2) {
						int id1 = o1.getValue();
						int id2 = o2.getValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			members.add(-pos - 1, member);
		}
	}

	/**
	 * Gets members name with value
	 * 
	 * @param value
	 * @return members name
	 */
	public String getMemberName(int value) {
		int pos = Collections.binarySearch(members, Integer.valueOf(value),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((EnumMember) o1).getValue();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		String name = ""; //$NON-NLS-1$
		if (pos >= 0) {
			EnumMember member = members.get(pos);
			name = member.getName();
		}
		return name;
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
