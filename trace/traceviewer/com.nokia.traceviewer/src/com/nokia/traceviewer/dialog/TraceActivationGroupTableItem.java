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
 * Trace Activation Group Table Item
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;

/**
 * Trace Activation Group Table Item
 */
public class TraceActivationGroupTableItem {

	/**
	 * ID of the item
	 */
	private int id;

	/**
	 * Name of the item
	 */
	private String name;

	/**
	 * Is item activated or not
	 */
	private boolean activated;

	/**
	 * Does item have two different states
	 */
	private boolean differentStates;

	/**
	 * How many components has this group
	 */
	private int componentCount;

	/**
	 * Reference to real group items
	 */
	private List<TraceActivationGroupItem> realGroups;

	/**
	 * Constructor
	 */
	public TraceActivationGroupTableItem() {
		realGroups = new ArrayList<TraceActivationGroupItem>();
	}

	/**
	 * Gets component count
	 * 
	 * @return the componentCount
	 */
	public int getComponentCount() {
		return componentCount;
	}

	/**
	 * Sets component count
	 * 
	 * @param componentCount
	 *            the componentCount to set
	 */
	public void setComponentCount(int componentCount) {
		this.componentCount = componentCount;
	}

	/**
	 * Tells has this item two different states
	 * 
	 * @return the differentStates
	 */
	public boolean isDifferentStates() {
		return differentStates;
	}

	/**
	 * Sets this item to have two different states
	 * 
	 * @param differentStates
	 *            the differentStates to set
	 */
	public void setDifferentStates(boolean differentStates) {
		this.differentStates = differentStates;
	}

	/**
	 * Gets ID
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets ID
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets name of the item
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name of the item
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Tells is this item activated
	 * 
	 * @return the activated
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Sets this item activated
	 * 
	 * @param activated
	 *            the activated to set
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * Gets real groups of this item
	 * 
	 * @return the realGroups
	 */
	public List<TraceActivationGroupItem> getRealGroups() {
		return realGroups;
	}

	/**
	 * Sets real groups of this item
	 * 
	 * @param realGroups
	 *            the realGroups to set
	 */
	public void setRealGroups(List<TraceActivationGroupItem> realGroups) {
		this.realGroups = realGroups;
	}
}
