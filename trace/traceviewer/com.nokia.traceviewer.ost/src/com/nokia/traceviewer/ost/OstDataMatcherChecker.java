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
 * OST Data Matcher Checker
 *
 */
package com.nokia.traceviewer.ost;

import java.io.FileInputStream;
import java.io.IOException;

import com.nokia.traceviewer.engine.TraceViewerConst;

/**
 * OST Data Matcher Checker
 * 
 */
public class OstDataMatcherChecker implements OstConsts, TraceViewerConst {

	/**
	 * Checks if file format seems to match
	 * 
	 * @param filePath
	 *            file path
	 * @return true if file format seems to match, false if not
	 */
	boolean checkIfFileFormatMatches(String filePath) {
		boolean matches = false;
		FileInputStream is = null;
		try {
			is = new FileInputStream(filePath);
			if (is.available() > 0) {
				byte[] arr = new byte[OST_V05_PROTOCOLID_OFFSET + 1];
				is.read(arr);
				int versionNumber = 0;
				int msgId = 0;

				// Get version number and message ID
				versionNumber |= arr[OST_VERSION_OFFSET] & BYTE_MASK;

				// Version must be 0.0, 0.1, 0.5 or 1.0
				if (versionNumber == OST_V00 || versionNumber == OST_V01) {
					msgId |= arr[OST_V01_PROTOCOLID_OFFSET] & BYTE_MASK;
				} else if (versionNumber == OST_V05 || versionNumber == OST_V10) {
					msgId |= arr[OST_V05_PROTOCOLID_OFFSET] & BYTE_MASK;
				}

				// Message ID must be Simple Trace or ASCII
				if (msgId == OST_SIMPLE_TRACE_ID || msgId == OST_ASCII_TRACE_ID) {
					matches = true;
				}
			}

		} catch (IOException e) {

			// Close the stream
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

		}

		return matches;
	}
}
