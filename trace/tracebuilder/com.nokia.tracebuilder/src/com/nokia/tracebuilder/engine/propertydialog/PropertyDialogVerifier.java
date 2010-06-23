/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Verifies the contents of a property dialog
 *
 */
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogVerifier;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.rules.PerformanceEventTemplate;
import com.nokia.tracebuilder.engine.rules.StateTraceTemplate;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObjectPropertyVerifier;
import com.nokia.tracebuilder.plugin.TraceFormatConstants;
import com.nokia.tracebuilder.project.FormattingUtils;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Verifies the contents of a property dialog
 * 
 */
final class PropertyDialogVerifier implements TraceObjectPropertyDialogVerifier {

	/**
	 * Property dialog
	 */
	private TraceObjectPropertyDialog propertyDialog;

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param propertyDialog
	 *            the property dialog to be verified
	 */
	PropertyDialogVerifier(TraceModel model,
			TraceObjectPropertyDialog propertyDialog) {
		this.propertyDialog = propertyDialog;
		this.model = model;
	}

	/**
	 * Changes the property dialog
	 * 
	 * @param propertyDialog
	 *            new property dialog
	 */
	void setPropertyDialog(TraceObjectPropertyDialog propertyDialog) {
		this.propertyDialog = propertyDialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.tracebuilder.engine.TraceObjectPropertyDialogVerifier#
	 * verifyContents()
	 */
	public void verifyContents() throws TraceBuilderException {
		int type = propertyDialog.getDialogType();
		String target = propertyDialog.getTarget();
		TraceObjectPropertyVerifier verifier = model.getVerifier();
		TraceGroup group;
		TraceConstantTable table;
		Trace trace;
		int nextTraceID;
		switch (type) {
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			table = model.findConstantTableByName(target);
			verifier.checkConstantProperties(table, null, propertyDialog
					.getID(), propertyDialog.getName());
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			TraceConstantTableEntry entry = (TraceConstantTableEntry) propertyDialog
					.getTargetObject();
			verifier.checkConstantProperties(entry.getTable(), entry,
					propertyDialog.getID(), propertyDialog.getName());
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			trace = (Trace) propertyDialog.getTargetObject();
			verifier.checkTraceParameterProperties(trace, null, propertyDialog
					.getID(), propertyDialog.getName(), propertyDialog
					.getValue());
			break;
		case TraceObjectPropertyDialog.ADD_TRACE:
			nextTraceID = verifyGroupName(target, verifier);
			group = model.findGroupByName(target);
			verifier.checkTraceProperties(group, null, nextTraceID, propertyDialog
					.getName(), propertyDialog.getValue());
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			trace = (Trace) propertyDialog.getTargetObject();
			verifier.checkTraceProperties(trace.getGroup(), trace,
					propertyDialog.getID(), propertyDialog.getName(),
					propertyDialog.getValue());
			break;
		case TraceObjectPropertyDialog.EDIT_GROUP:
			group = model.findGroupByName(target);
			verifier.checkTraceGroupProperties(model, group, propertyDialog
					.getID(), propertyDialog.getName());
			break;

		case TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE:
			table = model.findConstantTableByName(target);
			verifier.checkConstantTableProperties(model, table, propertyDialog
					.getID(), propertyDialog.getName());
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			nextTraceID = verifyGroupName(target, verifier);

			// In case of instrumentation the formatting is verified
			verifyInstrumentation();
			break;
		}
	}

	/**
	 * Verify Group Name
	 * 
	 * @param groupName
	 *            the name of the trace group
	 * @param verifier
	 *            the verifier
	 * @return next trace ID
	 * @throws TraceBuilderException
	 */
	private int verifyGroupName(String groupName,
			TraceObjectPropertyVerifier verifier) throws TraceBuilderException {
		TraceGroup group;
		int nextTraceID;
		preverifyGroupName();

		// If group does not exist, the group properties must also be
		// checked since it will be created
		group = model.findGroupByName(groupName);
		nextTraceID = 1;
		if (group != null) {
			nextTraceID = group.getNextTraceID();
		} else {
			int groupId = FormattingUtils.getGroupID(model, propertyDialog
					.getTarget());

			verifier.checkTraceGroupProperties(model, null, groupId,
					propertyDialog.getTarget());
		}
		return nextTraceID;
	}

	/**
	 * Preverify group name
	 * 
	 * @throws TraceBuilderException
	 */
	private void preverifyGroupName() throws TraceBuilderException {

		// Group name TRACE_STATE is allowed only if selected template is State
		// Trace
		// Group name TRACE_PERFORMANCE is allowed only if selected template is
		// Performnce
		// Event Entry-Exist
		String groupName = propertyDialog.getTarget();
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals
				.getGroupNameHandler();
		String[] defaultGroups = groupNameHandler.getDefaultGroups();
		if ((!(propertyDialog.getTemplate() instanceof StateTraceTemplate) && groupName
				.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()]))
				|| (!(propertyDialog.getTemplate() instanceof PerformanceEventTemplate) && groupName
						.equals(defaultGroups[groupNameHandler
								.getPerformanceGroupIdIndex()]))) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_GROUP_NAME);
		}
	}

	/**
	 * Verifies that instrumentation dialog contents are valid
	 * 
	 * @throws TraceBuilderException
	 *             if contents are not valid
	 */
	private void verifyInstrumentation() throws TraceBuilderException {
		String nameFormat = propertyDialog.getName();
		String traceFormat = propertyDialog.getValue();
		if (nameFormat != null && traceFormat != null) {
			if (hasFunctionName(nameFormat)) {
				checkInstrumentationFormat(nameFormat, traceFormat);
			} else {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.TRACE_NAME_FORMAT_MISSING_FUNCTION);
			}
		} else {
			if (nameFormat == null) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_TRACE_NAME_FORMAT);
			}
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_TRACE_TEXT_FORMAT);
		}
	}

	/**
	 * Formats the instrumentation dialog contents and checks the formatted data
	 * 
	 * @param nameFormat
	 *            the name format
	 * @param traceFormat
	 *            the trace text format
	 * @throws TraceBuilderException
	 *             if data is not valid
	 */
	private void checkInstrumentationFormat(String nameFormat,
			String traceFormat) throws TraceBuilderException {
		// Removes tags and checks the remaining data
		String name = TraceUtils.formatTrace(nameFormat, "_", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		String text = TraceUtils.formatTrace(traceFormat, "_", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		String target = propertyDialog.getTarget();
		TraceGroup group = model.findGroupByName(target);
		int id = 1;
		if (group != null) {
			id = group.getNextTraceID();
		}
		model.getVerifier().checkTraceProperties(group, null, id, name, text);
	}

	/**
	 * Checks if the format has the function name
	 * 
	 * @param format
	 *            the format
	 * @return true if it has the function name
	 */
	private boolean hasFunctionName(String format) {
		String nc = TraceFormatConstants.FORMAT_FUNCTION_NAME_NORMAL_CASE;
		String uc = TraceFormatConstants.FORMAT_FUNCTION_NAME_UPPER_CASE;
		return format.contains(nc) || format.contains(uc);
	}

}