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

/**
 * Data structure to maintain the number of event occurences
 * for each window group, in each cycle.
 */
public class WindowGroupEventData {
 
	private int event_0_count;
	private int event_1_count;
	private int event_2_count;
	private int event_3_count;
	
	public WindowGroupEventData(WindowGroupEventData target)
	{	
		this.event_0_count = target.event_0_count;
		this.event_1_count = target.event_1_count;
		this.event_2_count = target.event_2_count;
		this.event_3_count = target.event_3_count;
	}
	public WindowGroupEventData(){}
	
	public void incrementEventCount(int eventNum)
	{
		switch(eventNum)
		{
			case 0:
				event_0_count++;
				break;
			case 1:
				event_1_count++;
				break;
			case 2:
				event_2_count++;
				break;
			case 3:
				event_3_count++;
				break;
		}
	}

	public void decrementEventCount(int eventNum)
	{
		switch(eventNum)
		{
			case 0:
				event_0_count--;
				break;
			case 1:
				event_1_count--;
				break;
			case 2:
				event_2_count--;
				break;
			case 3:
				event_3_count--;
				break;
		}
	}
	
	public int getEvent_0_count() {
		return event_0_count;
	}

	public int getEvent_1_count() {
		return event_1_count;
	}

	public int getEvent_2_count() {
		return event_2_count;
	}
	
	public int getEvent_3_count() {
		return event_3_count;
	}

}

