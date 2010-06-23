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
* Location converter monitors locations and converts them to traces if necessary.
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;
import java.util.Iterator;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.propertydialog.PropertyDialogEnabler;
import com.nokia.tracebuilder.engine.propertydialog.PropertyDialogEngine;
import com.nokia.tracebuilder.engine.source.SourceParserRule;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.engine.source.SourceParserRule.ParameterConversionResult;
import com.nokia.tracebuilder.engine.source.SourceParserRule.TraceConversionResult;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelPersistentExtension;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.project.FormattingUtils;

/**
 * Location converter monitors locations and converts them to traces if
 * necessary.
 * 
 */
public final class TraceLocationConverter {

	/**
	 * Regular expression for catching variable syntax
	 */
	private final String VARIABLE_REGEX = "%\\S*"; //$NON-NLS-1$

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Property dialog engine
	 */
	private PropertyDialogEngine propertyDialogEngine;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param propertyDialogEngine
	 *            property dialog engine
	 */
	TraceLocationConverter(TraceModel model,
			PropertyDialogEngine propertyDialogEngine) {
		this.model = model;
		this.propertyDialogEngine = propertyDialogEngine;
	}

	/**
	 * Parse traces from source
	 * 
	 * @param properties
	 *            the source properties
	 */
	public void parseTracesFromSource(SourceProperties properties) {
		TraceBuilderGlobals.getSourceContextManager().setConverting(true);
		model.startProcessing();
		try {
			for (TraceLocation loc : properties) {
				autoConvertLocation(loc);
			}
			// If there are duplicates or unused traces, they are removed
			// Note that this will work across source files although this
			// function is processing only one file.
			// If a trace is created, all locations from all open source files
			// are linked to that trace and thus it will be removed as
			// duplicate.
			removeDuplicateTraces();
			// Unused traces may exist if the cache file is not up-to-date
			removeUnusedTraces();

		} finally {
			model.processingComplete();
			SourceContextManager manager = TraceBuilderGlobals
					.getSourceContextManager();
			manager.setConverting(false);
			manager.setContext(null);
		}
	}

	/**
	 * Converts the given location to trace if parser supports auto-conversion
	 * 
	 * @param location
	 *            the location
	 */
	private void autoConvertLocation(TraceLocation location) {
		// Stores the context of the location to the context manager.
		TraceBuilderGlobals.getSourceContextManager().setContext(
				location.getParser().getContext(location.getOffset()));
		Trace trace = location.getTrace();
		if (trace == null) {
			// If the trace does not exist, the parser determines if the
			// location can be converted
			if (location.getParserRule().getLocationParser()
					.isLocationConverted(location)) {
				try {
					convertLocation(location, null, true);
				} catch (TraceBuilderException e) {
					// If converter fails, the error code is stored into the
					// location. The location notifies all validity listeners
					// about the change
					location.setConverterErrorCode((TraceBuilderErrorCode) e
							.getErrorCode(), e.getErrorParameters());
				}
			}
		} else {
			// If the trace already exists in the model, it is updated
			// based on the source file contents
			updateLocation(location);
		}
	}

	/**
	 * Updates all locations of the source
	 * 
	 * @param properties
	 *            the source that was saved
	 */
	void sourceSaved(SourceProperties properties) {
		TraceBuilderGlobals.getSourceContextManager().setConverting(true);
		model.startProcessing();
		try {
			// When a source is saved, all traces that have been removed from
			// sources are also removed from the model.
			removeUnusedTraces();

			for (TraceLocation loc : properties) {
				updateLocation(loc);
			}

			// TODO: When a duplicate location is removed from one source file,
			// the other files are not processed and thus the duplicates
			// will not be converted until the file they are in is saved.
			// -> This affects UI builder only
			// If there are duplicates, they are removed
			removeDuplicateTraces();

		} finally {
			model.processingComplete();
			TraceBuilderGlobals.getSourceContextManager().setConverting(false);
		}
	}

	/**
	 * Recreates the trace from changed location when source is saved
	 * 
	 * @param location
	 *            the location to be checked
	 */
	private void updateLocation(TraceLocation location) {
		// Parser determines if the location can be converted
		if (location.getParserRule().getLocationParser().isLocationConverted(
				location)) {
			try {
				Trace trace = location.getTrace();

				// If a location has changed, the old trace is removed
				// and a new one created. Persistent extensions are moved to the
				// new trace
				Iterator<TraceModelPersistentExtension> extensions = null;
				if (trace != null) {
					extensions = trace
							.getExtensions(TraceModelPersistentExtension.class);
					trace.getGroup().removeTrace(trace);
				}
				convertLocation(location, extensions, true);

				// Check that the location is inside a function. Otherwise throw
				// an error because the code is unreachable
				if (location.getFunctionName() == null) {
					throw new TraceBuilderException(
							TraceBuilderErrorCode.UNREACHABLE_TRACE_LOCATION);
				}

			} catch (TraceBuilderException e) {
				// If converter fails, the error code is stored into the
				// location. The location notifies all validity listeners about
				// the change
				location.setConverterErrorCode((TraceBuilderErrorCode) e
						.getErrorCode(), e.getErrorParameters());
			}
		}
	}


	/**
	 * Source closed notification
	 * 
	 * @param properties
	 *            the source properties
	 */
	void sourceClosed(SourceProperties properties) {
		TraceBuilderGlobals.getSourceContextManager().setConverting(true);
		model.startProcessing();
		try {
			removeUnusedTraces();
		} finally {
			model.processingComplete();
			TraceBuilderGlobals.getSourceContextManager().setConverting(false);
		}
	}

	/**
	 * Removes all unused traces from the model
	 */
	private void removeUnusedTraces() {
		boolean groupRemoved = true;
		while (groupRemoved) {
			groupRemoved = false;
			for (TraceGroup group : model) {
				removeUnusedTracesFromGroup(group);
				if (!group.hasTraces()) {
					model.removeGroup(group);
					groupRemoved = true;
					break;
				}
			}
		}
	}

	/**
	 * Removes unused traces from a trace group
	 * 
	 * @param group
	 *            the group
	 */
	private void removeUnusedTracesFromGroup(TraceGroup group) {
		boolean traceRemoved = true;
		while (traceRemoved) {
			traceRemoved = false;
			for (Trace trace : group) {
				TraceLocationList list = trace
						.getExtension(TraceLocationList.class);
				LastKnownLocationList plist = trace
						.getExtension(LastKnownLocationList.class);
				if ((list == null || !list.hasLocations())
						&& (plist == null || !plist.hasLocations())) {
					group.removeTrace(trace);
					traceRemoved = true;
					break;
				}
			}
		}
	}

	/**
	 * Removes all duplicate traces from the model
	 */
	private void removeDuplicateTraces() {
		boolean groupRemoved = true;
		while (groupRemoved) {
			groupRemoved = false;
			for (TraceGroup group : model) {
				removeDuplicateTracesFromGroup(group);
				if (!group.hasTraces()) {
					model.removeGroup(group);
					groupRemoved = true;
					break;
				}
			}
		}
	}

	/**
	 * Removes duplicate traces from a trace group
	 * 
	 * @param group
	 *            the group
	 */
	private void removeDuplicateTracesFromGroup(TraceGroup group) {
		boolean traceRemoved = true;
		while (traceRemoved) {
			traceRemoved = false;
			for (Trace trace : group) {
				TraceLocationList list = trace
						.getExtension(TraceLocationList.class);
				if (list != null) {
					if (list.getLocationCount() > 1) {
						// All the locations are marked as duplicates and the
						// trace is deleted
						TraceBuilderErrorCode code = TraceBuilderErrorCode.TRACE_HAS_MULTIPLE_LOCATIONS;
						for (LocationProperties loc : list) {
							((TraceLocation) loc).setConverterErrorCode(code,
									null);
						}
						group.removeTrace(trace);
						traceRemoved = true;
						break;
					}
				}
			}
		}
	}

	/**
	 * Converts a location to a Trace object.
	 * 
	 * @param location
	 *            the location to be converted
	 * @return the new trace
	 * @throws TraceBuilderException
	 *             if conversion fails
	 */
	Trace convertLocation(TraceLocation location) throws TraceBuilderException {
		return convertLocation(location, null, false);
	}

	/**
	 * Converts a location to a Trace object.
	 * 
	 * @param location
	 *            the location to be converted
	 * @param extensions
	 *            persistent extensions to be added to the new trace
	 * @param autoConvert
	 *            true if converting without user interaction
	 * @return the new trace
	 * @throws TraceBuilderException
	 *             if conversion fails
	 */
	private Trace convertLocation(TraceLocation location,
			Iterator<TraceModelPersistentExtension> extensions,
			boolean autoConvert) throws TraceBuilderException {
		Trace trace = null;
		// If the parser has failed, the validity code is not OK and the
		// location cannot be converted. Traces marked with no-trace error code
		// have not yet been converted, so that is OK. Traces that have
		// duplicate ID's error code can be parsed, since the duplicates might
		// no longer exist.
		if (!autoConvert
				|| location.getValidityCode() == TraceBuilderErrorCode.OK
				|| location.getValidityCode() == TraceBuilderErrorCode.TRACE_DOES_NOT_EXIST
				|| location.getValidityCode() == TraceBuilderErrorCode.TRACE_HAS_MULTIPLE_LOCATIONS) {
			// The parser does the actual conversion
			SourceParserRule rule = location.getParserRule();
			TraceConversionResult result = rule.getLocationParser()
					.convertLocation(location);
			// After parser has finished, the trace is created.
			if (!autoConvert) {
				trace = convertWithUI(result, extensions);
			} else {
				trace = convertWithoutUI(result, extensions);
			}
			if (trace != null) {
				model.startProcessing();
				try {
					createParametersFromConversionResult(location, result,
							trace);
					// Runs a location validity check and notifies listeners
					// that location is now OK
					location.setConverterErrorCode(TraceBuilderErrorCode.OK,
							null);
				} catch (TraceBuilderException e) {
					// If parameters cannot be created, the trace is removed
					TraceGroup group = trace.getGroup();
					trace.getGroup().removeTrace(trace);
					if (!group.hasTraces()) {
						group.getModel().removeGroup(group);
					}
					throw e;
				} finally {
					model.processingComplete();
				}
			}
		}
		return trace;
	}

	/**
	 * Converts a location to trace without UI
	 * 
	 * @param result
	 *            the conversion result from parser
	 * @param extensions
	 *            persistent extensions to be added to the new trace
	 * @return the converted trace
	 * @throws TraceBuilderException
	 *             if location properties are not valid
	 */
	private Trace convertWithoutUI(TraceConversionResult result,
			Iterator<TraceModelPersistentExtension> extensions)
			throws TraceBuilderException {
		Trace trace = null;
		if (result.group != null) {
			String groupName = result.group;
			TraceGroup group = handleGroup(groupName);
			trace = handleTrace(result, extensions, group);
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.GROUP_NOT_SELECTED);
		}
		return trace;
	}

	/**
	 * Handle trace
	 * 
	 * @param result
	 *            the conversion result from parser
	 * @param extensions
	 *            persistent extensions to be added to the new trace
	 * @param group
	 *            the group where trace belongs to
	 * @return the trace
	 * @throws TraceBuilderException
	 */
	private Trace handleTrace(TraceConversionResult result,
			Iterator<TraceModelPersistentExtension> extensions, TraceGroup group)
			throws TraceBuilderException {

		Trace trace = null;
		String traceName = result.name;
		int traceId = group.getNextTraceID();
		String text = result.text;
		model.getVerifier().checkTraceProperties(group, null, traceId,
				traceName, text);
		TraceModelExtension[] extArray = createExtensionArray(result,
				extensions);
		trace = model.getFactory().createTrace(group, traceId, traceName, text,
				extArray);

		return trace;
	}

	/**
	 * Handle group. Try to fnd group from model. If it does not exist then
	 * create new group.
	 * 
	 * @param groupName
	 *            the name of the group
	 * @return the handled group
	 * @throws TraceBuilderException
	 */
	private TraceGroup handleGroup(String groupName)
			throws TraceBuilderException {
		// If auto-convert flag is set, the location is converted without
		// user interaction. A new trace group is created if not found
		TraceGroup group = model.findGroupByName(groupName);
		if (group == null) {
			int groupId = FormattingUtils.getGroupID(model, groupName);
			model.getVerifier().checkTraceGroupProperties(model, null, groupId,
					groupName);
			group = model.getFactory().createTraceGroup(groupId, groupName,
					null);
		}

		return group;
	}

	/**
	 * Combines extensions into one array
	 * 
	 * @param result
	 *            the conversion result
	 * @param extensions
	 *            the persistent extensions from old trace
	 * @return the combined array of extensions
	 */
	private TraceModelExtension[] createExtensionArray(
			TraceConversionResult result,
			Iterator<TraceModelPersistentExtension> extensions) {
		TraceModelExtension[] extArray = null;
		ArrayList<TraceModelExtension> ext = null;
		if (result.extensions != null) {
			ext = new ArrayList<TraceModelExtension>();
			ext.addAll(result.extensions);
		}
		if (extensions != null) {
			if (ext == null) {
				ext = new ArrayList<TraceModelExtension>();
			}
			while (extensions.hasNext()) {
				ext.add(extensions.next());
			}
		}
		if (ext != null) {
			extArray = new TraceModelExtension[ext.size()];
			ext.toArray(extArray);
		}
		return extArray;
	}

	/**
	 * Converts a location via UI
	 * 
	 * @param result
	 *            the conversion result from parser
	 * @param extensions
	 *            persistent extensions to be added to the new trace
	 * @return the converted trace
	 */
	private Trace convertWithUI(TraceConversionResult result,
			Iterator<TraceModelPersistentExtension> extensions) {
		Trace trace;
		// Templates and flags are disabled
		PropertyDialogEnabler enabler = new PropertyDialogEnabler(
				PropertyDialogEnabler.ENABLE_ID
						| PropertyDialogEnabler.ENABLE_NAME
						| PropertyDialogEnabler.ENABLE_VALUE
						| PropertyDialogEnabler.ENABLE_TARGET);
		// If auto-convert flag is not set, the "Add Trace" dialog is
		// shown
		TraceGroup group = null;
		if (result.group != null) {
			group = model.findGroupByName(result.group);
		}

		// Remove all variables from the text before showing the UI
		if (result.text != null) {
			result.text = result.text.replaceAll(VARIABLE_REGEX, ""); //$NON-NLS-1$
		}
		trace = propertyDialogEngine.showAddTraceDialog(group, result.name,
				result.text, result.extensions, enabler);
		return trace;
	}

	/**
	 * Creates the trace parameters based on trace conversion result
	 * 
	 * @param converted
	 *            the location that was converted
	 * @param result
	 *            the conversion result
	 * @param trace
	 *            the trace
	 * @throws TraceBuilderException
	 *             if parameters cannot be created
	 */
	private void createParametersFromConversionResult(TraceLocation converted,
			TraceConversionResult result, Trace trace)
			throws TraceBuilderException {
		if (result.parameters != null) {
			for (int i = 0; i < result.parameters.size(); i++) {
				int id = trace.getNextParameterID();
				ParameterConversionResult res = result.parameters.get(i);
				boolean warning = false;
				if (res.type == null) {
					warning = true;
					res.type = TraceParameter.HEX32;
				}
				model.getVerifier().checkTraceParameterProperties(trace, null,
						id, res.name, res.type);
				TraceModelExtension[] extArray = null;
				if (res.extensions != null) {
					extArray = new TraceModelExtension[res.extensions.size()];
					res.extensions.toArray(extArray);
				}
				TraceParameter param = model.getFactory().createTraceParameter(
						trace, id, res.name, res.type, extArray);
				if (warning) {
					String msg = Messages
							.getString("TraceBuilder.UnknownTypeWarning"); //$NON-NLS-1$
					TraceBuilderGlobals.getEvents().postWarningMessage(msg,
							param);
				}
			}
		}
	}

}
