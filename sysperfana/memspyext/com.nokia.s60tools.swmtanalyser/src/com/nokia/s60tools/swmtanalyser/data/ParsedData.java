/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
package com.nokia.s60tools.swmtanalyser.data;

import java.util.ArrayList;
/**
 * Stores the cycle data objects of all log files.
 *
 */
public class ParsedData {

	private ArrayList<CycleData> logData;
	
	public void setParsedData(ArrayList<CycleData> logData){
		this.logData = logData;
	}
	
	public CycleData[] getLogData(){
		if(this.logData != null)
			return logData.toArray(new CycleData[0]);
		else
			return null;
	}
	
	public int getNumberOfCycles(){
		if(logData == null)
			return 0;
		else
			return logData.size();
	}
}
