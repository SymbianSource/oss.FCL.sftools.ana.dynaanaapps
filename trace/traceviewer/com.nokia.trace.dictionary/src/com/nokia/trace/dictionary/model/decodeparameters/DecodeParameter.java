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
 * Decode parameter
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

import java.nio.ByteBuffer;
import java.util.List;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Decode parameter
 * 
 */
public abstract class DecodeParameter {

	/**
	 * One byte
	 */
	protected static final int ONE_BYTE = 1;

	/**
	 * Two bytes
	 */
	protected static final int TWO_BYTES = 2;

	/**
	 * Four bytes
	 */
	protected static final int FOUR_BYTES = 4;

	/**
	 * Eight bytes
	 */
	protected static final int EIGHT_BYTES = 8;

	/**
	 * Category for events
	 */
	private final static String EVENT_CATEGORY = Messages
			.getString("DecodeParameter.EventCategory"); //$NON-NLS-1$

	/**
	 * Explanation of data missing error
	 */
	private final static String DATA_MISSING = Messages
			.getString("DecodeParameter.DataMissingMsg"); //$NON-NLS-1$

	/**
	 * Boolean indicating that we have already informed about data missing once
	 */
	private boolean informedAboutDataMissing;

	/**
	 * Type of the parameter
	 */
	protected String type;

	/**
	 * Is this parameter hidden from the trace output
	 */
	protected boolean hidden;

	/**
	 * If this flag is on, it means that this decode parameter is the only
	 * variable in the trace. This affects for example to the way how arrays are
	 * decoded.
	 */
	public static boolean isOnlyVariableInTrace = false;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type of the parameter
	 * @param hidden
	 *            hidden or not
	 * 
	 */
	public DecodeParameter(String type, boolean hidden) {
		this.type = type;
		this.hidden = hidden;
	}

	/**
	 * Decodes this parameter and puts the input to the stringbuffer
	 * 
	 * @param dataFrame
	 *            dataFrame where data is
	 * @param offset
	 *            offset where to start reading
	 * @param traceString
	 *            trace where to append data
	 * @param dataStart
	 *            data start offset
	 * @param dataLength
	 *            data length
	 * @param parameterList
	 *            parameter list where to append parameters separately
	 * @return the offset after parameters
	 */
	public abstract int decode(ByteBuffer dataFrame, int offset,
			StringBuffer traceString, int dataStart, int dataLength,
			List<String> parameterList);

	/**
	 * Gets the type
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets hidden value
	 * 
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Sets hidden value
	 * 
	 * @param hidden
	 *            the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Sets type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets size of the parameter
	 * 
	 * @return size of the parameter. 0 if there is no size with this parameter.
	 *         Function should be overridden by every parameter that has a size
	 */
	public int getSize() {
		return 0;
	}

	/**
	 * Posts data missing error
	 * 
	 * @param string
	 *            string from which some data is missing
	 * @param remaining
	 *            remaining bytes
	 * @param required
	 *            required bytes
	 */
	public void postDataMissingEvent(StringBuffer string, int remaining,
			int required) {
		if (!informedAboutDataMissing) {
			TraceEvent event = new TraceEvent(TraceEvent.ERROR, DATA_MISSING
					+ Messages.getString("DecodeParameter.DataMissingMsgLine1") //$NON-NLS-1$
					+ required
					+ Messages.getString("DecodeParameter.DataMissingMsgLine2") //$NON-NLS-1$
					+ remaining
					+ Messages.getString("DecodeParameter.DataMissingMsgLine3")); //$NON-NLS-1$
			event.setCategory(EVENT_CATEGORY);
			event.setSource(new String(string));
			TraceDictionaryEngine.postEvent(event);
		}
		informedAboutDataMissing = true;
	}

}
