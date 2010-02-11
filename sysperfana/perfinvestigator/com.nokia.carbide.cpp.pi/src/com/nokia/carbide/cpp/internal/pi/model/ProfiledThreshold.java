/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

/**
 * 
 */
package com.nokia.carbide.cpp.internal.pi.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ProfiledThreshold extends ProfiledGeneric implements Serializable
{
	private static final long serialVersionUID = 4902799424564460641L;
	
	// number of items (threads, binaries, functions) represented by this object 
	private int[] itemCount = new int[3];
	
	// lists of items (threads, binaries, functions) represented by this object
	private ArrayList<ProfiledGeneric>[] items = new ArrayList[3];
	
	public ProfiledThreshold(String name) {
		this.setNameString(name);
	}
	
	public void setItemCount(int graphIndex, int itemCount) {
		this.itemCount[graphIndex] = itemCount;
	}

	public void incItemCount(int graphIndex) {
		this.itemCount[graphIndex]++;
	}
	
	public int getItemCount(int graphIndex) {
		return this.itemCount[graphIndex];
	}
	
	public void init(int graphIndex) {
		this.enableValue[graphIndex]      = false;
		this.itemCount[graphIndex]        = 0;
		this.graphSampleCount[graphIndex] = 0;

		if (this.items[graphIndex] != null)
			this.items[graphIndex].clear();

		if (this.activityList == null)
			this.activityList = new int[this.activityIndx];

		for (int i = 0; i < this.activityIndx; i++) {
			this.activityList[i] = 0;
			this.activityP[i] = 0;
		}
	}
	
	public void initAll() {
		this.enableValue[0]      = false;
		this.itemCount[0]        = 0;
		this.graphSampleCount[0] = 0;
		this.enableValue[1]      = false;
		this.itemCount[1]        = 0;
		this.graphSampleCount[1] = 0;
		this.enableValue[2]      = false;
		this.itemCount[2]        = 0;
		this.graphSampleCount[2] = 0;

		if (this.activityList == null)
			this.activityList = new int[this.activityIndx];

		for (int i = 0; i < this.activityIndx; i++) {
			this.activityList[i] = 0;
			this.activityP[i] = 0;
		}
	}
	
	public void addItem(int graphIndex, ProfiledGeneric pGeneric, int count)
	{
		if (pGeneric instanceof ProfiledThreshold)
			this.totalSampleCount += count;	// this assumes that threshold only has one non-zero graphIndex element

		this.itemCount[graphIndex]++;
		this.graphSampleCount[graphIndex] += count;

		// add to the activity list
		if (this.activityList == null)
			this.activityList = new int[this.activityIndx];

		int[] activityList = pGeneric.getActivityList();
		for (int i = 0; i < activityList.length; i++)
			this.activityList[i] += activityList[i];

		// add to the list of items associated with this threshold object
		if (items[graphIndex] == null)
			items[graphIndex] = new ArrayList<ProfiledGeneric>();
		
		items[graphIndex].add(pGeneric);

		this.enableValue[graphIndex] = true;
	}
	
	public ArrayList<ProfiledGeneric> getItems(int graphIndex)
	{
		return items[graphIndex];
	}
	
    public void setEnabled(int graphIndex, boolean enableValue)
    {
    	this.enableValue[graphIndex] = enableValue;
    	
    	if (items[graphIndex] == null)
    		return;
    	
    	for (int i = 0; i < items[graphIndex].size(); i++)
    		items[graphIndex].get(i).enableValue[graphIndex] = enableValue;
    }
}
