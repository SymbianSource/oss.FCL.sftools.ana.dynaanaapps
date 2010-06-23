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
 * Base class for all action objects of Trace Builder view
 *
 */
package com.nokia.tracebuilder.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderHelp;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.view.TraceViewPlugin;

/**
 * Base class for all action objects of Trace Builder view
 * 
 */
abstract class TraceBuilderAction extends Action {

	/**
	 * Constructor
	 */
	protected TraceBuilderAction() {
	}

	/**
	 * Constructor with action type
	 * 
	 * @param type
	 *            the action type
	 */
	protected TraceBuilderAction(int type) {
		super("", type); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		try {
			doRun();
		} catch (TraceBuilderException e) {
			TraceBuilderGlobals.getEvents().postError(e);
		}
	}

	/**
	 * Sets the properties of this action
	 * 
	 * @param actionID
	 *            the action ID
	 * @param helpID
	 *            the help ID
	 */
	protected void setDefaultProperties(String actionID, String helpID) {
		if (actionID != null) {
			setImageDescriptor(TraceViewPlugin
					.getImageDescriptor(ActionIDs.ICONS_DIRECTORY + actionID
							+ ActionIDs.GIF));
			setActionDefinitionId(ActionIDs.ACTION_ID_BASE + actionID);
		}
		if (helpID != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					TraceBuilderHelp.HELP_CONTEXT_BASE + helpID);
		}
	}

	/**
	 * Enables this action based on model validity and selected object
	 * 
	 * @param modelValid
	 *            the model validity
	 * @param selectedObject
	 *            the object
	 */
	protected void setEnabled(boolean modelValid, Object selectedObject) {
		if (!modelValid) {
			setEnabled(false);
		} else {
			setEnabled(getEnabledFlag(selectedObject));
		}
	}

	/**
	 * Checks if this action belongs to the menu
	 * 
	 * @return true if in menu, false if not
	 */
	protected boolean isInMenu() {
		return true;
	}

	/**
	 * Checks if this action belongs to the pop-up menu
	 * 
	 * @param selectedObject
	 *            the selection associated to the pop-up
	 * @return true if in pop-up, false if not
	 */
	protected boolean isInPopupMenu(Object selectedObject) {
		return isEnabled();
	}

	/**
	 * If given object is a location or location list, this returns the
	 * associated trace. Otherwise returns the object passed as parameter
	 * 
	 * @param selectedObject
	 *            the object
	 * @return the trace related to the object
	 */
	protected Object locationToTrace(Object selectedObject) {
		Object retval = null;
		if (selectedObject instanceof TraceLocation) {
			retval = ((TraceLocation) selectedObject).getTrace();
		} else if (selectedObject instanceof LastKnownLocation) {
			retval = ((LastKnownLocation) selectedObject).getTrace();
		} else if (selectedObject instanceof TraceLocationList) {
			retval = ((TraceLocationList) selectedObject).getOwner();
		} else if (selectedObject instanceof LastKnownLocationList) {
			retval = ((LastKnownLocationList) selectedObject).getOwner();
		}
		if (retval == null) {
			retval = selectedObject;
		}
		return retval;
	}

	/**
	 * Gets the enabled flag based on the object selected by user
	 * 
	 * @param selectedObject
	 *            the object
	 * @return true if enabled, false if not
	 */
	protected abstract boolean getEnabledFlag(Object selectedObject);

	/**
	 * Runs this action
	 * 
	 * @throws TraceBuilderException
	 *             if an exception occurs
	 */
	protected abstract void doRun() throws TraceBuilderException;

}
