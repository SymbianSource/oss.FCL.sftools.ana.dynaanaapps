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
 * Input object for 4 tables in events tab 
 *
 */
public class TableViewerInputObject {
	private Color color;
	private String name;
	
	/**
	 * Constructor
	 * @param name
	 * @param color
	 */
	public TableViewerInputObject(String name, Color color) {
		this.color = color;
		this.name = name;
	}
	/**
	 * Get color
	 * @return color
	 */
	public Color getColor() {
		return color;
	}
	/**
	 * Set color
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	/**
	 * Get name
	 * @return name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
