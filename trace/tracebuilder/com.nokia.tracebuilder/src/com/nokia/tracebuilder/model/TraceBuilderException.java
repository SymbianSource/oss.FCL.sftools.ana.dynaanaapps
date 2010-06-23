/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Exceptions thrown by Trace Builder engine
*
*/
package com.nokia.tracebuilder.model;

/**
 * Exceptions thrown by Trace Builder engine
 * 
 */
public class TraceBuilderException extends Exception {

	/**
	 * Error codes for trace builder exceptions
	 * 
	 */
	public interface TraceBuilderExceptionCode {
	}

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2991616409482985157L; // CodForChk_Dis_Magic

	/**
	 * Error code
	 */
	private TraceBuilderExceptionCode errorCode;

	/**
	 * Error parameters
	 */
	private TraceBuilderErrorParameters parameters;

	/**
	 * Source object
	 */
	private Object source;

	/**
	 * Flag that defines will event related to exception posted to trace event view
	 */
	private boolean postEvent = true;
	
	/**
	 * Constructor with error code
	 * 
	 * @param errorCode
	 *            the error code
	 */
	public TraceBuilderException(TraceBuilderExceptionCode errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * Constructor with error code and postEvent flag
	 * 
	 * @param errorCode
	 *            the error code
	 * @param postEvent
	 *            flag that defines will event related to exception posted to trace event view   
	 */
	public TraceBuilderException(TraceBuilderExceptionCode errorCode, boolean postEvent) {
		this.errorCode = errorCode;
		this.postEvent = postEvent;
	}

	/**
	 * Constructor with error code and parameters
	 * 
	 * @param errorCode
	 *            the error code
	 * @param parameters
	 *            the error parameters
	 */
	public TraceBuilderException(TraceBuilderExceptionCode errorCode,
			TraceBuilderErrorParameters parameters) {
		this.errorCode = errorCode;
		this.parameters = parameters;
	}

	/**
	 * Constructor with error code, parameters and source object
	 * 
	 * @param errorCode
	 *            the error code
	 * @param parameters
	 *            the error parameters
	 * @param source
	 *            the source object
	 */
	public TraceBuilderException(TraceBuilderExceptionCode errorCode,
			TraceBuilderErrorParameters parameters, Object source) {
		this.errorCode = errorCode;
		this.parameters = parameters;
		this.source = source;
	}
	
	/**
	 * Constructor with error code and root cause
	 * 
	 * @param errorCode
	 *            the error code
	 * @param cause
	 *            the reason for this exception
	 */
	public TraceBuilderException(TraceBuilderExceptionCode errorCode,
			Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * Gets the error code
	 * 
	 * @return error code
	 */
	public TraceBuilderExceptionCode getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the parameters related to the error
	 * 
	 * @return the parameters
	 */
	public TraceBuilderErrorParameters getErrorParameters() {
		return parameters;
	}

	/**
	 * Gets the source of this error
	 * 
	 * @return the source
	 */
	public Object getErrorSource() {
		return source;
	}

	/**
	 * Is event related to exception wanted to post to trace event view
	 * 
	 * @return true is event is wanted to post trace event view
	 *         false is event is not wanted to post trace event view
	 */
	public boolean isEventWantedToPost() {
		return postEvent;
	}
	
}
