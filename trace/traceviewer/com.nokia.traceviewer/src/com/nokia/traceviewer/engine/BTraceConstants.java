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
 * TraceViewer Constants
 *
 */
package com.nokia.traceviewer.engine;

/**
 * BTrace Constants
 */
public interface BTraceConstants {

	/**
	 * Header 2 present bit
	 */
	static final int HEADER2_PRESENT_BIT = 0;

	/**
	 * Timestamp present bit
	 */
	static final int TIMESTAMP_PRESENT_BIT = 1;

	/**
	 * Timestamp2 present bit
	 */
	static final int TIMESTAMP2_PRESENT_BIT = 2;

	/**
	 * Context ID present bit
	 */
	static final int CONTEXT_ID_PRESENT_BIT = 3;

	/**
	 * Program counter present bit
	 */
	static final int PROGRAM_COUNTER_PRESENT_BIT = 4;

	/**
	 * Extra value present bit
	 */
	static final int EXTRA_VALUE_PRESENT_BIT = 5;

	/**
	 * Record truncated bit
	 */
	static final int RECORD_TRUNCATED_BIT = 6;

	/**
	 * Record missing bit
	 */
	static final int RECORD_MISSING_BIT = 7;

	/**
	 * Length of a single variable
	 */
	static final int BTRACE_VARIABLE_LENGTH = 4;

	/**
	 * Length of BTrace header
	 */
	static final int BTRACE_HEADER_LENGTH = 4;

	/**
	 * Number of flags
	 */
	static final int FLAGS_LENGTH = 8;

	/**
	 * Mask to get CPU ID from BTrace Header 2
	 */
	static final int CPU_ID_MASK = 0xfff << 20;

	/**
	 * Shift to get CPU ID from BTrace Header 2
	 */
	static final int CPU_ID_SHIFT = 20;

	/**
	 * Mask to get MultiPart ID from BTrace Header 2
	 */
	static final int MULTIPART_MASK = 3 << 0;

}
