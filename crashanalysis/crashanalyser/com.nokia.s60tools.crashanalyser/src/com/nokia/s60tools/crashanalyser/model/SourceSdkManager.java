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
*/

package com.nokia.s60tools.crashanalyser.model;

import com.nokia.s60tools.sdk.SdkUtils;
import java.util.*;

/**
 * Manages SDKs for 'Open Source File' functionality. 
 *
 */
public final class SourceSdkManager {
	private SourceSdkManager() {
		// no implementation needed
	}
	
	/**
	 * Returns a list of all SDKs and their location
	 * @return all SDKS.
	 */
	public static Map<String, String> getAllSdks() {
		try {
			return SdkUtils.getSdkMapFileFolders(true, true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set given SDK as the SDK which is to be used in 'Open Source File' functionality
	 * @param sdk default sdk to be used
	 */
	public static void setSdk(String sdk) {
		UserEnteredData data = new UserEnteredData();
		data.savePreviousSdk(sdk);
	}
	
	/**
	 * Gets the name of the SDK which is currently the one being used 
	 * by 'Open Source File' functionality
	 * @return name of the currently default SDK
	 */
	public static String getCurrentSkdName() {
		UserEnteredData data = new UserEnteredData();
		return data.getPreviousSdk();
	}

	/**
	 * Returns the epocroot folder of given sdk.
	 * @param sdkName sdk
	 * @return epocroot folder or null
	 */
	public static String getEpocroot(String sdkName) {
		try {
			Map<String, String> sdks = SdkUtils.getSdkMapFileFolders(true, true);
			if (sdks.containsKey(sdkName))
				return sdks.get(sdkName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
