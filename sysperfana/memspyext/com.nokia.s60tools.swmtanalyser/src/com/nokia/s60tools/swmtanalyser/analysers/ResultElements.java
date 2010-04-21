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
package com.nokia.s60tools.swmtanalyser.analysers;

import org.eclipse.swt.graphics.Color;
/**
 * Input object for tree viewer in analysis tab.
 *
 */
public class ResultElements implements Comparable<ResultElements>{
	
	/**
	 * Item name
	 */
	public static final String ITEM_NAME_COLUMN = "Item name";
	/**
	 * Event
	 */
	public static final String EVENT_COLUMN = "Event";
	/**
	 * Delta
	 */
	public static final String DELTA_COLUMN = "Delta";
	/**
	 * Severity
	 */
	public static final String SEVERITY_COLUMN = "Severity";
	
	private static final String TAB = "\t";

	private String itemName;
	private String event;
	private String delta;
	private long delta_value;
	private double growing_factor;
	private Color color;
	private long [] event_values;
	
	private AnalyserConstants.Priority priority = AnalyserConstants.Priority.NEGLIGIBLE;
	private AnalyserConstants.DeltaType deltaType = AnalyserConstants.DeltaType.COUNT; 
	
	/**
	 * Construction
	 * @param itemName
	 * @param event
	 * @param delta
	 * @param deltaValue
	 * @param type
	 */
	ResultElements(String itemName, String event, String delta, long deltaValue, AnalyserConstants.DeltaType type)
	{
		this.itemName = itemName;
		this.event = event;
		this.delta = delta;
		this.delta_value = deltaValue;
		this.deltaType = type;
	}
	
	/**
	 * @return item name
	 */
	public String toString()
	{
		return this.itemName;
	}
		

	/**
	 * Get tab separated headers for this result.
	 * @return headers with tab as separator
	 */
	public String getTabSeparatedHeaders()
	{
		//NOTE: If reorganized, also #getTabSeparatedValues() must reorganize
		StringBuffer b = new StringBuffer();
		b.append(ITEM_NAME_COLUMN);
		b.append(TAB);
		b.append(EVENT_COLUMN);
		b.append(TAB);
		b.append(DELTA_COLUMN);
		b.append(TAB);
		b.append(SEVERITY_COLUMN);
		
		return b.toString();
	
	}			
	
	/**
	 * Get tab separated values for this result.
	 * @return values
	 */
	public String getTabSeparatedValues()
	{
		//NOTE: If reorganized, also #getTabSeparatedHeaders() must reorganize
		StringBuffer b = new StringBuffer();
		b.append(itemName);
		b.append(TAB);
		b.append(event);
		b.append(TAB);
		b.append(delta);
		b.append(TAB);
		b.append(getPriority());
		
		return b.toString();
	
	}		
	


	/**
	 * Get delta
	 * @return delta
	 */
	public String getDelta() {
		return delta;
	}

	/**
	 * Set delta
	 * @param delta
	 */
	public void setDelta(String delta) {
		this.delta = delta;
	}

	/**
	 * Get item name
	 * @return item name
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * Get event
	 * @return event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Set event
	 * @param event
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Get growing factor
	 * @return growing factor
	 */
	public double getGrowingFactor() {
		return growing_factor;
	}

	/**
	 * Set growing factor
	 * @param growing_factor
	 */
	public void setGrowingFactor(double growing_factor) {
		this.growing_factor = growing_factor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultElements input) {
		
		int priority_comparision = this.getPriority().compareTo(input.getPriority());
		
		if(priority_comparision == 0)
		{
			if(this.growing_factor > input.growing_factor)
				return 1;
			else if(this.growing_factor < input.growing_factor)
				return -1;
			else
				return 0;
		}
		else
			return priority_comparision;
	}

	/**
	 * Compare this objects {@link #getDelta()} to given object {@link #getDelta()}
	 * Returns a negative integer, zero, or a positive integer 
	 * as this object is less than, equal to, or greater than the specified object.
	 * @param input
	 * @return 0 if this object is equal to given object, 
	 * 1 if this object is greater to given object,
	 * -1 if this object is less to given object
	 */
	public int compareByDelta(ResultElements input)
	{
		long input_delta = input.getDeltaValue();
		
		if(delta_value > input_delta)
			return 1;
		else if(delta_value < input_delta)
			return -1;
		else
			return 0;

	}
	/**
	 * Get priority
	 * @return priority
	 */
	public AnalyserConstants.Priority getPriority() {
		return priority;
	}

	/**
	 * Set priority
	 * @param priority
	 */
	public void setPriority(AnalyserConstants.Priority priority) {
		this.priority = priority;
	}
		
	/**
	 * Get delta
	 * @return delta
	 */
	public long getDeltaValue() {
		return delta_value;
	}

	/**
	 * Set delta
	 * @param delta_value
	 */
	public void setDeltaValue(long delta_value) {
		this.delta_value = delta_value;
	}

	/**
	 * Get Delta type
	 * @return delta type
	 */
	public AnalyserConstants.DeltaType getType() {
		return deltaType;
	}

	/**
	 * Set delta type
	 * @param type
	 */
	public void setType(AnalyserConstants.DeltaType type) {
		this.deltaType = type;
	}

	/**
	 * Get color
	 * @return color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set Color
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Get event values
	 * @return values
	 */
	public long[] getEventValues() {
		return event_values;
	}

	/**
	 * Set event values
	 * @param event_values
	 */
	public void setEventValues(long[] event_values) {
		this.event_values = event_values;
	}
}

