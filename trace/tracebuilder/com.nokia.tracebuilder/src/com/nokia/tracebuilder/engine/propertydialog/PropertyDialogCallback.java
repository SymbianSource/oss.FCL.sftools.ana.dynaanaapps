/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Base class for property dialog manager callbacks
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogType;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.SingletonParameterRule;

/**
 * Base class for property dialog manager callbacks
 * 
 */
abstract class PropertyDialogCallback implements PropertyDialogEngineCallback {

	/**
	 * Trace model
	 */
	protected TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	PropertyDialogCallback(TraceModel model) {
		this.model = model;
	}

	/**
	 * Shows a location confirmation query
	 * 
	 * @param root
	 *            the root trace object
	 * @return YES / NO
	 */
	static int showLocationConfirmationQuery(TraceObject root) {
		int queryResult = TraceBuilderDialogs.OK;
		boolean hasLocations;
		if (root instanceof TraceModel) {
			hasLocations = checkModelForLocations((TraceModel) root);
		} else if (root instanceof TraceGroup) {
			hasLocations = checkGroupForLocations((TraceGroup) root);
		} else if (root instanceof Trace) {
			hasLocations = checkTraceForLocations((Trace) root);
		} else if (root instanceof TraceParameter) {
			hasLocations = checkTraceForLocations(((TraceParameter) root)
					.getTrace());
		} else {
			hasLocations = false;
		}
		if (hasLocations) {
			QueryDialogParameters params = new QueryDialogParameters();
			params.dialogType = QueryDialogType.UPDATE_WHEN_SOURCE_NOT_OPEN;
			queryResult = TraceBuilderGlobals.getDialogs()
					.showConfirmationQuery(params);
		}
		return queryResult;
	}

	/**
	 * Checks a trace for last known locations
	 * 
	 * @param trace
	 *            the trace
	 * @return true if there are last known locations
	 */
	private static boolean checkTraceForLocations(Trace trace) {
		boolean hasLocations = false;
		LastKnownLocationList list = trace
				.getExtension(LastKnownLocationList.class);
		if (list != null && list.hasLocations()) {
			hasLocations = true;
		}
		return hasLocations;
	}

	/**
	 * Checks a trace group for last known locations
	 * 
	 * @param group
	 *            the group
	 * @return true if there are last known locations
	 */
	private static boolean checkGroupForLocations(TraceGroup group) {
		boolean hasLocations = false;
		Iterator<Trace> traceItr = group.getTraces();
		while (traceItr.hasNext() && !hasLocations) {
			hasLocations = checkTraceForLocations(traceItr.next());
		}
		return hasLocations;
	}

	/**
	 * Checks the model for last known locations
	 * 
	 * @param model
	 *            the model
	 * @return true if there are last known locations
	 */
	private static boolean checkModelForLocations(TraceModel model) {
		boolean hasLocations = false;
		Iterator<TraceGroup> groupItr = model.getGroups();
		while (groupItr.hasNext() && !hasLocations) {
			hasLocations = checkGroupForLocations(groupItr.next());
		}
		return hasLocations;
	}

	/**
	 * Creates the extensions array from the dialog flags, selected template and
	 * an existing list of extensions
	 * 
	 * @param owner
	 *            the owner of the new object
	 * @param dialog
	 *            the dialog
	 * @param extList
	 *            the list of extensions
	 * @return the extensions array
	 * @throws TraceBuilderException
	 *             if the extensions list is not valid
	 */
	protected TraceModelExtension[] createExtensions(TraceObject owner,
			TraceObjectPropertyDialog dialog, List<TraceModelExtension> extList)
			throws TraceBuilderException {
		TraceModelExtension[] retval = null;
		ArrayList<TraceModelExtension> extensions = new ArrayList<TraceModelExtension>();
		List<TraceObjectPropertyDialogFlag> flags = dialog.getFlags();
		if (flags != null) {
			for (int i = 0; i < flags.size(); i++) {
				TraceObjectPropertyDialogFlag flag = flags.get(i);
				if (flag.isEnabled()) {
					flag.createExtensions(extensions);
				}
			}
		}
		TraceObjectPropertyDialogTemplate template = dialog.getTemplate();
		if (template != null) {
			template.createExtensions(extensions);
		}
		if (extList != null) {
			extensions.addAll(extList);
		}
		checkExtensionValidity(owner, extensions);
		int extensionCount = extensions.size();
		if (extensionCount > 0) {
			retval = new TraceModelExtension[extensionCount];
			extensions.toArray(retval);
		}
		return retval;
	}

	/**
	 * Creates the extensions array from the dialog flags and selected template
	 * 
	 * @param owner
	 *            the owner of the new object
	 * @param dialog
	 *            the dialog
	 * @return the extensions array
	 * @throws TraceBuilderException
	 *             if the extensions list is not valid
	 */
	protected TraceModelExtension[] createExtensions(TraceObject owner,
			TraceObjectPropertyDialog dialog) throws TraceBuilderException {
		return createExtensions(owner, dialog, null);
	}

	/**
	 * Checks the validity of the list of extensions to be added to a new trace
	 * object
	 * 
	 * @param owner
	 *            the owner of the new trace object
	 * @param extensions
	 *            the list of extensions
	 * @throws TraceBuilderException
	 *             if the extension list is not valid
	 */
	private void checkExtensionValidity(TraceObject owner,
			ArrayList<TraceModelExtension> extensions)
			throws TraceBuilderException {
		if (owner instanceof Trace) {
			for (TraceParameter oldParam : (Trace) owner) {
				Iterator<SingletonParameterRule> singletons = oldParam
						.getExtensions(SingletonParameterRule.class);
				while (singletons.hasNext()) {
					SingletonParameterRule singleton = singletons.next();
					for (int i = 0; i < extensions.size(); i++) {
						TraceModelExtension extension = extensions.get(i);
						if (extension instanceof SingletonParameterRule
								&& singleton.getClass().equals(
										extension.getClass())) {
							throw new TraceBuilderException(
									TraceBuilderErrorCode.PARAMETER_TEMPLATE_ALREADY_IN_USE,
									null, null);
						}
					}
				}
			}
		}
	}

}
