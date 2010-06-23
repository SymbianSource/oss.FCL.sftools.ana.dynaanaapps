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
* Trace parameter deletion query processor
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.DeleteObjectQueryParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogType;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.TraceFormattingRule;
import com.nokia.tracebuilder.engine.source.TraceParameterFormattingRule;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Trace parameter deletion query processor
 * 
 */
public final class DeleteParameterCallback extends DeleteObjectCallback {

	/**
	 * Constructor
	 * 
	 * @param parameter
	 *            the parameter to be deleted
	 */
	public DeleteParameterCallback(TraceParameter parameter) {
		super(parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      buildQuery(com.nokia.tracebuilder.model.TraceObject)
	 */
	@Override
	protected QueryDialogParameters buildQuery(TraceObject object)
			throws TraceBuilderException {
		TraceParameter parameter = (TraceParameter) object;
		// If the owner prevents parameter deletion, it is not deleted
		TraceParameterRestrictionRule rule = parameter.getTrace().getExtension(
				TraceParameterRestrictionRule.class);
		DeleteObjectQueryParameters params;
		if (rule == null || rule.canRemoveParameters()) {
			params = new DeleteObjectQueryParameters();
			params.dialogType = QueryDialogType.DELETE_PARAMETER;
			params.objectName = parameter.getName();
			params.ownerName = parameter.getTrace().getName();
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_REMOVE_NOT_ALLOWED);
		}
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback#
	 *      deleteObject(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters)
	 */
	@Override
	protected void deleteObject(TraceObject object,
			QueryDialogParameters queryResults) {
		TraceParameter parameter = (TraceParameter) object;
		Trace owner = parameter.getTrace();
		Iterator<TraceParameter> parameters = owner.getParameters();
		int index = 0;
		boolean found = false;
		// Calculates the index of the parameter in source
		while (parameters.hasNext() && !found) {
			TraceParameter param = parameters.next();
			TraceParameterFormattingRule rule = param
					.getExtension(TraceParameterFormattingRule.class);
			if (rule == null || rule.isShownInSource()) {
				// Only parameters that are shown in source are checked
				if (parameter == param) {
					found = true;
				} else {
					index++;
				}
			} else {
				// If the parameter to be removed is not shown in source, there
				// is no need to update the locations
				if (parameter == param) {
					found = true;
					index = -1;
				}
			}
		}
		if (found && index >= 0) {
			// If index in source was found, the parameter lists in locations of
			// the trace are updated. When the source is updated, the location
			// gets the correct parameter list
			TraceLocationList list = owner
					.getExtension(TraceLocationList.class);
			if (list != null) {
				for (LocationProperties loc : list) {
					((TraceLocation) loc).removeParameterAt(index);
				}
			}
		}
		TraceFormattingRule rule = owner
				.getExtension(TraceFormattingRule.class);
		if (rule == null) {
			rule = owner.getModel().getExtension(TraceFormattingRule.class);
		}
		if (rule != null) {
			// Remove parameter text from trace text
			rule.parameterAboutToBeRemoved(parameter, index);
		}
		parameter.getTrace().removeParameter(parameter);
	}

}
