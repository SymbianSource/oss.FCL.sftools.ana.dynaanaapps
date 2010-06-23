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
* Interface to a property dialog of Trace Builder
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

import com.nokia.tracebuilder.model.TraceObject;

/**
 * Interface to a property dialog of Trace Builder. Implemented into the view
 * 
 */
public interface TraceObjectPropertyDialog {

	/**
	 * OK button pressed
	 */
	int OK = 0;

	/**
	 * Cancel button pressed
	 */
	int CANCEL = 1;

	/**
	 * Add trace dialog type
	 */
	int ADD_TRACE = 1;

	/**
	 * Add parameter dialog type
	 */
	int ADD_PARAMETER = 2;

	/**
	 * Select component dialog type
	 */
	int SELECT_COMPONENT = 3;

	/**
	 * Add constant dialog type
	 */
	int ADD_CONSTANT = 4;

	/**
	 * Edit group dialog type
	 */
	int EDIT_GROUP = 5;

	/**
	 * Edit trace dialog type
	 */
	int EDIT_TRACE = 6;

	/**
	 * Edit constant table dialog type
	 */
	int EDIT_CONSTANT_TABLE = 7;

	/**
	 * Edit constant dialog type
	 */
	int EDIT_CONSTANT = 8;

	/**
	 * Instrumenter dialog type
	 */
	int INSTRUMENTER = 9;

	/**
	 * Sets the dialog type
	 * 
	 * @param dialogType
	 *            the type
	 */
	public void setDialogType(int dialogType);

	/**
	 * Gets the dialog type
	 * 
	 * @return the dialog type
	 */
	public int getDialogType();

	/**
	 * Gets the name of the target. When OK button is pressed, this may return a
	 * different name than the name of the object set by setTargetObject
	 * 
	 * @return the name of the target object
	 */
	public String getTarget();

	
	/**
	 * Sets the target name
	 * 
	 * @param target
	 *            the new target name
	 */
	public void setTarget(String target);
	
	/**
	 * Sets the target object. The object may not be changed by the UI.
	 * 
	 * @param object
	 *            the target object
	 */
	public void setTargetObject(TraceObject object);

	/**
	 * Gets the object that was set with setTargetObject
	 * 
	 * @return the target object
	 */
	public TraceObject getTargetObject();

	/**
	 * Shows the dialog.
	 * 
	 * @return the button ID used to close the dialog
	 */
	public int open();

	/**
	 * Sets the object identifier
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#getID()
	 * @param id
	 *            the object ID
	 */
	public void setID(int id);

	/**
	 * Gets the object identifier
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#getID()
	 * @return the object ID
	 */
	public int getID();

	/**
	 * Sets the object name
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#getName()
	 * @param name
	 *            the new name
	 */
	public void setName(String name);

	/**
	 * Returns the object name
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#getName()
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the object value.
	 * 
	 * @see com.nokia.tracebuilder.model.Trace#getTrace()
	 * @see com.nokia.tracebuilder.model.TraceParameter#getType()
	 * @return the value
	 */
	public String getValue();

	/**
	 * Sets the object value.
	 * 
	 * @see com.nokia.tracebuilder.model.Trace#getTrace()
	 * @see com.nokia.tracebuilder.model.TraceParameter#getType()
	 * @param value
	 *            the object value
	 */
	public void setValue(String value);

	/**
	 * Sets the dialog flags
	 * 
	 * @param flags
	 *            the flags
	 */
	public void setFlags(List<TraceObjectPropertyDialogFlag> flags);

	/**
	 * Gets the dialog flags
	 * 
	 * @return the list of flags
	 */
	public List<TraceObjectPropertyDialogFlag> getFlags();

	/**
	 * Sets the templates to the dialog.
	 * 
	 * @param templates
	 *            the list of templates
	 * @param active
	 *            the template that should be selected when the dialog is shown
	 */
	public void setTemplates(List<TraceObjectPropertyDialogTemplate> templates,
			TraceObjectPropertyDialogTemplate active);

	/**
	 * Gets the template that was selected
	 * 
	 * @return the template
	 */
	public TraceObjectPropertyDialogTemplate getTemplate();

	/**
	 * Sets the enabler interface
	 * 
	 * @param enabler
	 *            the enabler
	 */
	public void setEnabler(TraceObjectPropertyDialogEnabler enabler);

	/**
	 * Sets an interface that can be used by the view to verify dialog contents
	 * while user is changing them
	 * 
	 * @param verifier
	 *            the dialog content verifier
	 */
	public void setVerifier(TraceObjectPropertyDialogVerifier verifier);

}
