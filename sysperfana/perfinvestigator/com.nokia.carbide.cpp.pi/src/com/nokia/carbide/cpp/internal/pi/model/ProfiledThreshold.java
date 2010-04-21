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
import java.util.Arrays;
import java.util.List;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * Class representing a Threshold item. It may represent several Profiled items
 * with low sample counts, which are below a certain threshold
 * 
 * Each ProfiledThreshold is created and solely owned and managed by one GppTraceGraph.
 * 
 */
public class ProfiledThreshold extends ProfiledGeneric implements Serializable
{
	private static final long serialVersionUID = 4902799424564460641L;
	
	// number of items (threads, binaries, functions) represented by this object 
	// no separate array needs to be created for SMP since the threshold is owned
	// just by one graph
	private int itemCount;
	
	// lists of items represented by this object
	private List<ProfiledGeneric> items = new ArrayList<ProfiledGeneric>();;
	
	public ProfiledThreshold(String name, int cpuCount, int graphCount) {
		super(cpuCount, graphCount);
		this.setNameString(name);
	}
	
	/**
	 * Sets the number of ProfiledGeneric items below the threshold
	 * @param graphIndex the graphIndex for which to set the number of items
	 * @param itemCount the number of items to set
	 */
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	/**
	 * Increases the number of ProfiledGeneric items below the threshold by one.
	 */
	public void incItemCount() {
		this.itemCount++;
	}
	
	/**
	 * Getter for the number of ProfiledGeneric items below the threshold
	 * @return the number of profiled items below the threshold for the given graph
	 */
	public int getItemCount() {
		return this.itemCount;
	}
	
	/**
	 * Resetting data structures for given graph
	 * @param graphIndex the ordinal of the graph to use
	 */
	public void init(int graphIndex) {
		internalInit(graphIndex);
		Arrays.fill(activityP, 0);
	}

	private void internalInit(int graphIndex) {
		this.enableValue[graphIndex]      = false;
		this.graphSampleCount[graphIndex] = 0;
		this.itemCount        = 0;
		this.items.clear();
	}
	
	/**
	 * Clear any values from internal structures for given graph
	 * @param cpu The CPU for which to reset
	 * @param graphIndex the graph for which to reset
	 */
	public void initForSMP(int cpu, int graphIndex) {
		internalInit(graphIndex);
		Arrays.fill(this.activityPSMP[cpu], 0);
	}
	
	/**
	 * Adds the given ProfiledGeneric and its bucket values to this threshold item
	 * @param graphIndex The ordinal of the graph to use
	 * @param pGeneric The ProfiledGeneric to use
	 * @param count the total sample count for the ProfiledGeneric
	 */
	public void addItem(int graphIndex, ProfiledGeneric pGeneric, int count)
	{
		if (pGeneric instanceof ProfiledThreshold)
			this.totalSampleCount += count;	// this assumes that threshold only has one non-zero graphIndex element

		this.itemCount++;
		this.graphSampleCount[graphIndex] += count;

		//add bucket percentages from graph to threshold item
		float[] activityList = pGeneric.getActivityList();
		for (int i = 0; i < activityList.length; i++){
			this.activityP[i] += activityList[i];
		}

		// add to the list of items associated with this threshold object
		items.add(pGeneric);

		this.enableValue[graphIndex] = true;
	}
	
	/**
	 * Adds a ProfiledGeneric with low sample count to this threshold item for the given CPU and graph index. 
	 * @param cpu the CPU it applies to
	 * @param graphIndex the graphIndex to use
	 * @param pGeneric the ProfiledGeneric to add to the threshold item
	 * @param count the sample count for this ProfiledGeneric on the given CPU
	 */
	public void addItemForSMP(int cpu, int graphIndex, ProfiledGeneric pGeneric, int count)
	{
		if (pGeneric instanceof ProfiledThreshold)
			this.totalSampleCountSMP[cpu] += count;	// this assumes that threshold only has one non-zero graphIndex element

		this.itemCount++;
		this.graphSampleCount[graphIndex] += count;


		float[] activityList = pGeneric.getActivityListForSMP(cpu);
		for (int i = 0; i < activityList.length; i++)
			this.activityPSMP[cpu][i] += activityList[i];

		// add to the list of items associated with this threshold object
		items.add(pGeneric);

		this.enableValue[graphIndex] = true;
	}
	
	/**
	 * Returns list of items below the threshold
	 * @return
	 */
	public List<ProfiledGeneric> getItems()
	{
		return items;
	}
	
    /* (non-Javadoc)
     * @see com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric#setEnabled(int, boolean)
     */
    @Override
	public void setEnabled(int graphIndex, boolean enableValue)
    {
    	this.enableValue[graphIndex] = enableValue;
    	
    	for (ProfiledGeneric item : items)
    		item.enableValue[graphIndex] = enableValue;
    }
}
