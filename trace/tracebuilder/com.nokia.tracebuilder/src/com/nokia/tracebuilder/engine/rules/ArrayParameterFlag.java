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
 * UI flag for array parameter type
 *
 */
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceParameterPropertyDialogDynamicFlag;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * UI flag for array parameter type
 * 
 */
final class ArrayParameterFlag extends DialogDynamicFlagBase implements
		TraceParameterPropertyDialogDynamicFlag {

	/**
	 * Title shown in UI
	 */
	private static final String UI_TITLE = Messages
			.getString("ArrayParameterFlag.Title"); //$NON-NLS-1$

	/**
	 * Template has been selected flag
	 */
	private boolean hasTemplate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#
	 * createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> extList) {
		extList.add(new ArrayParameterRuleImpl());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#getText()
	 */
	public String getText() {
		return UI_TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag#
	 * templateChanged
	 * (com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	@Override
	public boolean templateChanged(TraceObjectPropertyDialogTemplate template) {
		boolean retval = false;
		if (template != null) {
			if (isAvailable()) {
				setAvailable(false);
				retval = true;
			}
			hasTemplate = true;
		} else {
			if (!isAvailable()) {
				setAvailable(true);
				retval = true;
			}
			hasTemplate = false;
		}

		// If Performance or State Trace is selected, array types are
		// not supported
		if (isPerformaceTraceSelected() || isStateTraceSelected()) {
			setAvailable(false);
			retval = true;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceParameterPropertyDialogDynamicFlag
	 * #typeChanged(int)
	 */
	public boolean typeChanged(String newType) {
		boolean retval = false;
		if (!hasTemplate) {
			// String arrays are not supported
			if (newType != TraceParameter.ASCII
					&& newType != TraceParameter.UNICODE
					&& !isPerformaceTraceSelected() && !isStateTraceSelected()) {
				if (!isAvailable()) {
					setAvailable(true);
					retval = true;
				}
			} else {
				if (isAvailable()) {
					setAvailable(false);
					retval = true;
				}
			}
		}

		return retval;
	}

	/**
	 * Check is Performance Event trace selected
	 * 
	 * @return true if Performance Event Trace is selected, otherwise false
	 */
	private boolean isPerformaceTraceSelected() {
		boolean retval = false;

		TraceObject selectedObject = TraceBuilderGlobals.getTraceBuilder()
				.getSelectedObject();

		if (selectedObject instanceof Trace) {
			Trace trace = (Trace) selectedObject;
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			if (trace.getGroup().getName().equals(
					groupNameHandler.getDefaultGroups()[groupNameHandler.getPerformanceGroupIdIndex()])) {
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Check is State trace selected
	 * 
	 * @return true if State Trace is selected, otherwise false
	 */
	private boolean isStateTraceSelected() {
		boolean retval = false;

		TraceObject selectedObject = TraceBuilderGlobals.getTraceBuilder()
				.getSelectedObject();

		if (selectedObject instanceof Trace) {
			Trace trace = (Trace) selectedObject;
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			if (trace.getGroup().getName().equals(
					groupNameHandler.getDefaultGroups()[groupNameHandler.getStateGroupIdIndex()])) {
				retval = true;
			}
		}
		return retval;
	}
}
