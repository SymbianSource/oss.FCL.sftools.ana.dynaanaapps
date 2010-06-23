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
* Properties of property dialog
*
*/
package com.nokia.tracebuilder.view;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogVerifier;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Properties of property dialog
 * 
 */
class PropertyDialogProperties implements TraceObjectPropertyDialog {

	/**
	 * Type of dialog
	 */
	private int dialogType;

	/**
	 * Trace object name
	 */
	private String name;

	/**
	 * Trace object ID
	 */
	private int id;

	/**
	 * Trace object value
	 */
	private String value;

	/**
	 * Target object
	 */
	private String target;

	/**
	 * Flags for check boxes
	 */
	private List<TraceObjectPropertyDialogFlag> flags;

	/**
	 * Dialog templates
	 */
	private List<TraceObjectPropertyDialogTemplate> templates;

	/**
	 * Enabler
	 */
	private TraceObjectPropertyDialogEnabler enabler;

	/**
	 * Currently selected template
	 */
	private TraceObjectPropertyDialogTemplate selectedTemplate;

	/**
	 * Data verification interface
	 */
	protected TraceObjectPropertyDialogVerifier dataVerifier;

	/**
	 * Trace view
	 */
	private TraceView view;

	/**
	 * Target object
	 */
	private TraceObject targetObject;

	/**
	 * Creates a new property dialog properties object
	 * 
	 * @param view
	 *            the view
	 */
	PropertyDialogProperties(TraceView view) {
		this.view = view;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#getID()
	 */
	public int getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setDialogType(int)
	 */
	public void setDialogType(int dialogType) {
		this.dialogType = dialogType;
	}

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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#setTarget(java.lang.String)
	 */
	public void setTarget(String target) {
		this.target = target;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setUpdateObject(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setTargetObject(TraceObject targetObject) {
		this.targetObject = targetObject;
		if (targetObject != null) {
			this.target = targetObject.getName();
		} else {
			this.target = ""; //$NON-NLS-1$
		}
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setFlags(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag[])
	 */
	public void setFlags(List<TraceObjectPropertyDialogFlag> flags) {
		this.flags = flags;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setTemplates(java.util.List,
	 *      com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	public void setTemplates(List<TraceObjectPropertyDialogTemplate> templates,
			TraceObjectPropertyDialogTemplate active) {
		this.templates = templates;
		selectedTemplate = active;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setEnabler(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler)
	 */
	public void setEnabler(TraceObjectPropertyDialogEnabler enabler) {
		this.enabler = enabler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#
	 *      setVerifier(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogVerifier)
	 */
	public void setVerifier(TraceObjectPropertyDialogVerifier verifier) {
		this.dataVerifier = verifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialog#open()
	 */
	public int open() {
		Shell shell = view.getShell();
		int retval;
		if (shell != null) {
			PropertyDialog dialog = new PropertyDialog(shell, this);
			retval = dialog.open();
		} else {
			retval = CANCEL;
		}
		return retval;
	}

	/**
	 * Verfies the properties
	 * 
	 * @throws TraceBuilderException
	 *             if contents are not valid
	 */
	void verifyContents() throws TraceBuilderException {
		if (dataVerifier != null) {
			dataVerifier.verifyContents();
		}
	}

	/**
	 * Gets the templates list
	 * 
	 * @return the templates
	 */
	List<TraceObjectPropertyDialogTemplate> getTemplates() {
		return templates;
	}

	/**
	 * Gets the enabler interface
	 * 
	 * @return the interface
	 */
	TraceObjectPropertyDialogEnabler getEnabler() {
		return enabler;
	}

	/**
	 * Sets the selected template
	 * 
	 * @param template
	 *            the template that was selected
	 */
	void setTemplate(TraceObjectPropertyDialogTemplate template) {
		this.selectedTemplate = template;
	}

}
