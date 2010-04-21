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
package com.nokia.s60tools.swmtanalyser.editors;

import org.eclipse.swt.graphics.Color;

/**
 * Input class for the tableviewer in GraphedItems tab. 
 *
 */
public class GraphedItemsInput {
	
	public static final String COL1_ELEMENTS = "Elements";
	public static final String COL2_EVENT = "Event";
	public static final String COL3_TYPE = "Type";

	private static final String TAB = "\t";

	private String name;
	private String event;
	private String type;
	private Color color;
	
	/**
	 * Get event parameter
	 * @return event
	 */
	public String getEvent() {
		return event;
	}
	/**
	 * Set event parameter
	 * @param event
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	/**
	 * Get name parameter
	 * @return name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set name parameter
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Get type parameter
	 * @return type
	 */
	public String getType() {
		return type;
	}
	/**
	 * Set type parameter
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Get color parameter
	 * @return color
	 */
	public Color getColor() {
		return color;
	}
	/**
	 * Set color parameter
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * Get tab separated values for this result.
	 * @return values
	 */	
	public String getTabSeparatedValues() {
		StringBuffer b = new StringBuffer();
		b.append(getName());
		b.append(TAB);
		b.append(getEvent());
		b.append(TAB);
		b.append(getType());

		return b.toString();
	}
	
	/**
	 * Get tab separated headers for this result.
	 * @return headers with tab as separator
	 */
	public String getTabSeparatedHeaders() {
		StringBuffer b = new StringBuffer();
		b.append(COL1_ELEMENTS);
		b.append(TAB);
		b.append(COL2_EVENT);
		b.append(TAB);
		b.append(COL3_TYPE);

		return b.toString();
	}
	
}
