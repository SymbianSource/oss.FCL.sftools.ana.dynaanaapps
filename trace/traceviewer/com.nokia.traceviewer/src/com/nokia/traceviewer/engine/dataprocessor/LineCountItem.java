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
 * Line Count Item
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

/**
 * Line count item showed in the lineCountingTable
 * 
 */
public class LineCountItem {

	/**
	 * Name of the item
	 */
	private String name;

	/**
	 * Count of the item
	 */
	private int count;

	/**
	 * Indicates if item has changed since last update
	 */
	private boolean changed;

	/**
	 * @param name
	 *            Name of the item
	 * @param count
	 *            count of the item
	 */
	public LineCountItem(String name, int count) {
		this.name = name;
		this.count = count;
	}

	/**
	 * Tells the status of changed attribute
	 * 
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Sets the changed attribute
	 * 
	 * @param changed
	 *            the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * Gets count
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets count
	 * 
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Gets name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
