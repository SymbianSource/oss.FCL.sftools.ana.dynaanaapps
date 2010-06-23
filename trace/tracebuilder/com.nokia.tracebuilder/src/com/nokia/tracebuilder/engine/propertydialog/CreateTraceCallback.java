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
 * Callback for add trace property dialog
 *
 */
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.List;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.rules.PerformanceEventTemplate;
import com.nokia.tracebuilder.engine.rules.RuleUtils;
import com.nokia.tracebuilder.engine.rules.StateTraceTemplate;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.project.FormattingUtils;

/**
 * Callback for add trace property dialog
 * 
 */
final class CreateTraceCallback extends PropertyDialogCallback {

	/**
	 * New trace
	 */
	private Trace trace;

	/**
	 * List of extensions to be added to the trace
	 */
	private List<TraceModelExtension> extensions;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param extensions
	 *            the extensions to be added to the new trace
	 */
	CreateTraceCallback(TraceModel model, List<TraceModelExtension> extensions) {
		super(model);
		this.extensions = extensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.PropertyDialogManagerCallback#
	 *      okSelected(com.nokia.tracebuilder.engine.TraceObjectPropertyDialog)
	 */
	public void okSelected(TraceObjectPropertyDialog dialog)
			throws TraceBuilderException {
		String groupName = dialog.getTarget();
		try {
			model.startProcessing();
			TraceGroup group = model.findGroupByName(groupName);
			if (group == null) {
				// If group does not exist, it is created
				int id = FormattingUtils.getGroupID(model, groupName);
				model.getVerifier().checkTraceGroupProperties(model, null, id,
						groupName);
				group = model.getFactory()
						.createTraceGroup(id, groupName, null);
			}
			String name = dialog.getName();

			// In case of Performance Event trace and State trace add prefix and
			// suffix to trace name
			if (dialog.getTemplate() instanceof PerformanceEventTemplate) {
				name = RuleUtils.ENTRY_NAME_PREFIXES[RuleUtils.TYPE_PERF_EVENT]
						+ name
						+ RuleUtils.ENTRY_NAME_SUFFIXES[RuleUtils.TYPE_PERF_EVENT];
			} else if (dialog.getTemplate() instanceof StateTraceTemplate) {
				name = RuleUtils.ENTRY_NAME_PREFIXES[RuleUtils.TYPE_STATE_TRACE]
						+ name;
			}

			String data = dialog.getValue();
			int id = group.getNextTraceID();
			group.getModel().getVerifier().checkTraceProperties(group, null,
					id, name, data);
			TraceModelExtension[] extArray = createExtensions(group, dialog,
					extensions);
			trace = group.getModel().getFactory().createTrace(group, id, name,
					data, extArray);
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(trace,
					true, false);
		} finally {
			model.processingComplete();
		}
	}

	/**
	 * Gets the trace
	 * 
	 * @return trace
	 */
	public Trace getTrace() {
		return trace;
	}

}