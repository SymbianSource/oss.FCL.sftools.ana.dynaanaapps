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
* Deletes multiple traces
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;
import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogType;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.QueryDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Deletes multiple traces
 * 
 */
public final class DeleteMultipleTracesCallback extends
		DeleteTraceFromSourceCallback {

	/**
	 * Instrumentation engine contains sets of instrumented traces
	 */
	private InstrumentationEngine instrumentationEngine;

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            trace model
	 * @param sourceEngine
	 *            source engine
	 * @param instrumentationEngine
	 *            instrumentation engine
	 */
	public DeleteMultipleTracesCallback(TraceModel model,
			SourceEngine sourceEngine,
			InstrumentationEngine instrumentationEngine) {
		super(model, sourceEngine);
		this.model = model;
		this.instrumentationEngine = instrumentationEngine;
	}

	/**
	 * Shows the delete dialog
	 * 
	 * @return the dialog result
	 * @throws TraceBuilderException
	 *             if traces cannot be deleted
	 */
	@Override
	public int delete() throws TraceBuilderException {
		int retval;
		ArrayList<Trace> traces = new ArrayList<Trace>();
		showDialog(traces);
		if (traces.size() > 0) {
			model.startProcessing();
			try {
				for (int i = 0; i < traces.size(); i++) {
					deleteTrace(traces.get(i));
				}
			} finally {
				model.processingComplete();
			}
			retval = TraceBuilderDialogs.OK;
		} else {
			retval = TraceBuilderDialogs.CANCEL;
		}
		return retval;
	}

	/**
	 * Deletes a trace
	 * 
	 * @param trace
	 *            the trace to be deleted
	 */
	private void deleteTrace(Trace trace) {
		removeTraceFromSource(trace);
		trace.getGroup().removeTrace(trace);
	}

	/**
	 * Shows the dialog
	 * 
	 * @param traces
	 *            the list for traces to be removed
	 * @throws TraceBuilderException
	 *             if processing fails
	 */
	private void showDialog(List<Trace> traces) throws TraceBuilderException {
		List<CheckListDialogEntry> rootEntries = null;
		CheckListDialogEntry groups = createGroups();
		CheckListDialogEntry instrumentations = createInstrumentations(groups);
		if (instrumentations.hasChildren()) {
			rootEntries = new ArrayList<CheckListDialogEntry>();
			rootEntries.add(instrumentations);
			rootEntries.add(groups);
		} else if (groups.hasChildren()) {
			rootEntries = new ArrayList<CheckListDialogEntry>();
			rootEntries.add(groups);
		}
		if (rootEntries != null) {
			CheckListDialogParameters parameters = buildParameters(instrumentations
					.hasChildren());
			parameters.rootItems = rootEntries;
			parameters.dialogType = CheckListDialogType.DELETE_TRACES;
			int ret = TraceBuilderGlobals.getDialogs()
					.showCheckList(parameters);
			if (ret == TraceBuilderDialogs.OK) {
				// Deletes the checked traces
				for (CheckListDialogEntry entry : rootEntries) {
					addToList(entry, traces);
				}
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.NO_TRACES_TO_DELETE);
		}
	}

	/**
	 * Builds the parameter list
	 * 
	 * @param hasInstrumentation
	 *            true if there are instrumentation entries
	 * @return the list
	 */
	private CheckListDialogParameters buildParameters(boolean hasInstrumentation) {
		CheckListDialogParameters parameters = new CheckListDialogParameters();
		parameters.expandLevel = 0;
		parameters.showRoot = hasInstrumentation;
		return parameters;
	}

	/**
	 * Creates instrumentations list
	 * 
	 * @param groupRoot
	 *            the root of groups list
	 * @return the root entry for instrumentations
	 */
	private CheckListDialogEntry createInstrumentations(
			CheckListDialogEntry groupRoot) {
		Iterator<String> instrumentations = instrumentationEngine
				.getInstrumenterIDs();
		CheckListDialogEntry instrumenterRoot = new CheckListDialogEntry();
		instrumenterRoot.setObject("Traces from instrumentations"); //$NON-NLS-1$
		while (instrumentations.hasNext()) {
			String instrumenterID = instrumentations.next();
			addInstrumentation(instrumenterRoot, instrumenterID, groupRoot);
		}
		return instrumenterRoot;
	}

	/**
	 * Adds traces from given instrumentation to the list
	 * 
	 * @param entry
	 *            the root entry for instrumentations
	 * @param instrumenterID
	 *            the instrumenter ID
	 * @param groupRoot
	 *            the root of groups list
	 */
	private void addInstrumentation(CheckListDialogEntry entry,
			String instrumenterID, CheckListDialogEntry groupRoot) {
		Iterator<InstrumentedTraceRule> instrumentedTraces = instrumentationEngine
				.getInstrumentedTraces(instrumenterID);
		CheckListDialogEntry instrumenterEntry = new CheckListDialogEntry();
		instrumenterEntry.setObject(instrumenterID);
		while (instrumentedTraces.hasNext()) {
			Trace trace = (Trace) instrumentedTraces.next().getOwner();
			addTrace(instrumenterEntry, trace);
		}
		if (instrumenterEntry.hasChildren()) {
			entry.addChild(instrumenterEntry);
		}
	}

	/**
	 * Adds a trace to given entry
	 * 
	 * @param entry
	 *            the root entry
	 * @param trace
	 *            the trace
	 * @return the trace entry
	 */
	private CheckListDialogEntry addTrace(CheckListDialogEntry entry,
			Trace trace) {
		CheckListDialogEntry traceEntry = new CheckListDialogEntry();

		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		if (list != null) {
			for (LocationProperties loc : list) {
				String traceLocationFileName = loc.getFileName();
				Iterator<SourceProperties> sources = TraceBuilderGlobals
						.getTraceBuilder().getOpenSources();
				while (sources.hasNext()) {
					SourceProperties source = sources.next();
					if (traceLocationFileName.equals(source.getFileName())) {
						traceEntry.setObject(trace);
						entry.addChild(traceEntry);
						break;
					}
				}
			}
		}

		return traceEntry;
	}

	/**
	 * Adds groups to the list
	 * 
	 * @return the root of group entries
	 */
	private CheckListDialogEntry createGroups() {
		CheckListDialogEntry groupRoot = new CheckListDialogEntry();
		groupRoot.setObject("Traces from groups"); //$NON-NLS-1$
		for (TraceGroup group : model) {
			addGroup(groupRoot, group);
		}
		return groupRoot;
	}

	/**
	 * Adds a group to the list
	 * 
	 * @param rootEntry
	 *            the root entry
	 * @param group
	 *            the group
	 */
	private void addGroup(CheckListDialogEntry rootEntry, TraceGroup group) {
		CheckListDialogEntry groupEntry = new CheckListDialogEntry();
		groupEntry.setObject(group);
		for (Trace trace : group) {
			addTrace(groupEntry, trace);
		}
		if (groupEntry.hasChildren()) {
			rootEntry.addChild(groupEntry);
		}
	}

	/**
	 * Recursively adds children of given entry to the list of traces to be
	 * removed
	 * 
	 * @param entry
	 *            an entry
	 * @param traces
	 *            the list of traces
	 */
	private void addToList(CheckListDialogEntry entry, List<Trace> traces) {
		if (entry.isChecked() && entry.getObject() instanceof Trace) {
			traces.add((Trace) entry.getObject());
		}
		for (CheckListDialogEntry child : entry) {
			addToList(child, traces);
		}
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
		// Not used, this class overrides the delete method
		return null;
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
		// Not used, this class overrides the delete method
	}

}
