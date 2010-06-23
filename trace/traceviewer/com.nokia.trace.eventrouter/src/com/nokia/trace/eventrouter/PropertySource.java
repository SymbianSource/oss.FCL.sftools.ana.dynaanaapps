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
 * Property Source has a action name, target, source and a map containing string mappings
 *
 */
package com.nokia.trace.eventrouter;

/**
 * Property Source has a action name, target, source and a map containing string
 * mappings
 * 
 */
public class PropertySource {

	/**
	 * Name for the action what this event should execute. Is used to
	 * distinguish what properties this event should / can contain.
	 */
	private String actionName;

	/**
	 * Target ID string
	 */
	private String targetId;

	/**
	 * Source ID string
	 */
	private String sourceId;

	/**
	 * Properties map
	 */
	private PropertyMap properties;

	/**
	 * Constructor
	 * 
	 * @param actionName
	 *            action name
	 * @param targetId
	 *            target ID String
	 * @param sourceId
	 *            source ID String
	 */
	public PropertySource(String actionName, String targetId, String sourceId) {
		this.actionName = actionName;
		this.targetId = targetId;
		this.sourceId = sourceId;
		properties = new PropertyMap();
	}

	/**
	 * Constructor without action name
	 * 
	 * @param targetId
	 *            target ID String
	 * @param sourceId
	 *            source ID String
	 */
	public PropertySource(String targetId, String sourceId) {
		this("unknown", targetId, sourceId); //$NON-NLS-1$
	}

	/**
	 * Gets properties
	 * 
	 * @return the properties
	 */
	public PropertyMap getProperties() {
		return properties;
	}

	/**
	 * Gets the source ID
	 * 
	 * @return the sourceId
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Gets the target ID
	 * 
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * Gets the action name
	 * 
	 * @return the action name
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * Sets the action name
	 * 
	 * @param actionName
	 *            the action name to set
	 */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
}
