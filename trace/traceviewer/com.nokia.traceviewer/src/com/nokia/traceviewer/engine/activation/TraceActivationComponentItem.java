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
 * Trace Activation Component Item
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.util.ArrayList;

/**
 * Trace Activation Component Item
 * 
 */
public class TraceActivationComponentItem {

	/**
	 * Name of the item
	 */
	private String name;

	/**
	 * Defined in a file
	 */
	private String definedInFile;

	/**
	 * ID of the component
	 */
	private int id;

	/**
	 * Groups inside this component
	 */
	private ArrayList<TraceActivationGroupItem> groups;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            id of the component
	 * @param name
	 *            name of the component
	 */
	public TraceActivationComponentItem(int id, String name) {
		groups = new ArrayList<TraceActivationGroupItem>();
		this.id = id;
		this.name = name;
	}

	/**
	 * Get groups of this component
	 * 
	 * @return the groups
	 */
	public ArrayList<TraceActivationGroupItem> getGroups() {
		return groups;
	}

	/**
	 * Adds a new group for this component
	 * 
	 * @param group
	 */
	public void addGroup(TraceActivationGroupItem group) {
		groups.add(group);
	}

	/**
	 * Gets the name of the component
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name for the component
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the file path where this component is defined in
	 * 
	 * @return file path
	 */
	public String getFilePath() {
		return definedInFile;
	}

	/**
	 * Sets the file path where this component is defined in
	 * 
	 * @param filePath
	 *            file path
	 */
	public void setFilePath(String filePath) {
		this.definedInFile = filePath;
	}

	/**
	 * Description here
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Description here
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
}
