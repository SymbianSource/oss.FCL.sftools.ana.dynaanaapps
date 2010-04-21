/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.peccommon;

/**
 * An element for the Performance Counter Trace legend view.
 * Represented in one line of the legend table viewer.
 */
public class PecCommonLegendElement {
	private int id; // id of this trace event
	private String name; //name of performance counter
	private char character; //short name, such as 'A' used to refer to in column headings
	private long sum; //sum off all values in time frame
	private long cnt; //number of samples in time frame
	private int min; //the lowest value in the time frame
	private int max; //the highest value in the time frame
	private boolean coreValuesOnly; //show only 
	
	/**
	 * Constructor
	 * @param id trace event id
	 * @param name the name of the event
	 * @param character character representing the event, i.e. A for the first event, B for the second etc.
	 * @param coreValuesOnly Calculate / show only average, min, and max
	 */
	public PecCommonLegendElement(int id, String name, char character, boolean coreValuesOnly) {
		this.id = id;
		this.name = name;
		this.character = character;
		this.coreValuesOnly = coreValuesOnly;
	}
	/**
	 * @return the trace event id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the sum
	 */
	public long getSum() {
		return sum;
	}
	/**
	 * @param sum the sum to set
	 */
	public void setSum(long sum) {
		this.sum = sum;
	}
	/**
	 * @return the cnt
	 */
	public long getCnt() {
		return cnt;
	}
	/**
	 * @param cnt the cnt to set
	 */
	public void setCnt(long cnt) {
		this.cnt = cnt;
	}
	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}
	/**
	 * @return the max of sample values in the selected area. 
	 */
	public int getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the character
	 */
	public char getCharacter() {
		return character;
	}
	/**
	 * @return true, if only average, min, and max values are to be displayed; false otherwise
	 */
	public boolean isCoreValuesOnly() {
		return coreValuesOnly;
	}

}
