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
 * Contains the location of DataProcessors in the DataProcessors list
 *
 */
package com.nokia.traceviewer.api;

/**
 * Contains the location of DataProcessors
 * 
 */
final class DataProcessorLocation {

	/**
	 * Location of this location
	 */
	private DPLocation location;

	/**
	 * Priority level of this location
	 */
	private int priority;

	/**
	 * Constructor
	 * 
	 * @param location
	 *            location
	 * @param priority
	 *            priority
	 */
	public DataProcessorLocation(DPLocation location, int priority) {
		this.location = location;
		this.priority = priority;
	}

	/**
	 * Gets the location
	 * 
	 * @return the location
	 */
	public DPLocation getLocation() {
		return location;
	}

	/**
	 * Sets the location
	 * 
	 * @param location
	 *            the location to set
	 */
	public void setLocation(DPLocation location) {
		this.location = location;
	}

	/**
	 * Gets the priority
	 * 
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the priority
	 * 
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
