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
 * Trace Information contains ID's to identify this trace
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Trace Information contains ID's to identify this trace
 * 
 */
public class TraceInformation {

	/**
	 * Component Id
	 */
	private int componentId;

	/**
	 * Group Id
	 */
	private int groupId;

	/**
	 * Trace Id
	 */
	private int traceId;

	/**
	 * Gets component Id
	 * 
	 * @return the componentId
	 */
	public int getComponentId() {
		return componentId;
	}

	/**
	 * Sets component Id
	 * 
	 * @param componentId
	 *            the componentId to set
	 */
	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	/**
	 * Gets group Id
	 * 
	 * @return the groupId
	 */
	public int getGroupId() {
		return groupId;
	}

	/**
	 * Sets group Id
	 * 
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	/**
	 * Gets trace Id
	 * 
	 * @return the traceId
	 */
	public int getTraceId() {
		return traceId;
	}

	/**
	 * Sets trace Id
	 * 
	 * @param traceId
	 *            the traceId to set
	 */
	public void setTraceId(int traceId) {
		this.traceId = traceId;
	}

	/**
	 * Tells is this Trace Information defined
	 * 
	 * @return true if trace information is defined
	 */
	public boolean isDefined() {
		boolean isDefined = false;
		if (componentId != 0 || groupId != 0 || traceId != 0) {
			isDefined = true;
		}
		return isDefined;
	}

}
