/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Dummy property dialog implementation
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

import com.nokia.tracebuilder.model.TraceObject;

/**
 * Property dialog interface adapter. The getters return what the setters have
 * set and <i>open</i> returns CANCEL
 * 
 */
public class PropertyDialogAdapter implements TraceObjectPropertyDialog {

	/**
	 * Dialog type
	 */
	private int dialogType;

	/**
	 * Object ID
	 */
	private int id;

	/**
	 * Object name
	 */
	private String name;

	/**
	 * Object value
	 */
	private String value;

	/**
	 * Target object
	 */
	private String target;

	/**
	 * Target object
	 */
	private TraceObject targetObject;

	/**
	 * Object template
	 */
	private TraceObjectPropertyDialogTemplate selectedTemplate;

	/**
	 * Dialog flags
	 */
	private List<TraceObjectPropertyDialogFlag> flags;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getDialogType()
	 */
	public int getDialogType() {
		return dialogType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getFlags()
	 */
	public List<TraceObjectPropertyDialogFlag> getFlags() {
		return flags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getID()
	 */
	public int getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getTarget()
	 */
	public String getTarget() {
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getUpdateObject()
	 */
	public TraceObject getTargetObject() {
		return targetObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getTemplate()
	 */
	public TraceObjectPropertyDialogTemplate getTemplate() {
		return selectedTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getValue()
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#open()
	 */
	public int open() {
		return CANCEL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setDialogType(int)
	 */
	public void setDialogType(int dialogType) {
		this.dialogType = dialogType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setEnabler(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler)
	 */
	public void setEnabler(TraceObjectPropertyDialogEnabler enabler) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setFlags(java.util.List)
	 */
	public void setFlags(List<TraceObjectPropertyDialogFlag> flags) {
		this.flags = flags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setID(int)
	 */
	public void setID(int id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setTarget(java.lang.String)
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setUpdateObject(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setTargetObject(TraceObject targetObject) {
		this.targetObject = targetObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setTemplates(java.util.List,
	 *      com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	public void setTemplates(List<TraceObjectPropertyDialogTemplate> templates,
			TraceObjectPropertyDialogTemplate active) {
		selectedTemplate = active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setVerifier(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogVerifier)
	 */
	public void setVerifier(TraceObjectPropertyDialogVerifier verifier) {
	}

}
