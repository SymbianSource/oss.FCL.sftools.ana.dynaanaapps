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
 * OST message callback interface
 *
 */
package com.nokia.traceviewer.ost;

import java.io.IOException;

/**
 * OST message callback interface
 * 
 */
public interface OstMessageCallback {

	/**
	 * Processes message
	 * 
	 * @param msgStart
	 *            start offset of message
	 * @param msgLength
	 *            message length
	 * @param headerLength
	 *            header length
	 * @param headerVersion
	 *            header version
	 * @return true if processing should be continued, false otherwise
	 * @throws IOException
	 */
	boolean processMessage(int msgStart, int msgLength, int headerLength,
			int headerVersion) throws IOException;

}
