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
 * TraceViewer Constants
 * 
 */
public interface TraceViewerConst {

	/**
	 * Number of bytes shown in view
	 */
	int MAX_MESSAGE_SIZE = 65536;

	/**
	 * Receive buffer must be able to fit two complete messages
	 */
	int RECEIVE_BUFFER_SIZE = MAX_MESSAGE_SIZE * 2;

	/**
	 * Byte mask
	 */
	int BYTE_MASK = 0xFF;

	/**
	 * Short mask
	 */
	int SHORT_MASK = 0xFFFF;

	/**
	 * Int mask
	 */
	int INT_MASK = 0xFFFFFFFF;
}
