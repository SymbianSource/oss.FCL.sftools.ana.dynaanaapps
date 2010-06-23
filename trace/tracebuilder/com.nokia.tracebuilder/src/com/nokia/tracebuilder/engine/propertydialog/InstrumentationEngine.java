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
* Instrumentation manager groups traces that were instrumented within a single run
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelExtensionListener;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceModelResetListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Instrumentation engine groups traces that were instrumented within a single
 * run.
 * 
 */
public final class InstrumentationEngine {

	/**
	 * Model listener for instrumenter
	 * 
	 */
	private final class InstrumenterModelListener implements TraceModelListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelListener#
		 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
		 *      com.nokia.tracebuilder.model.TraceObject)
		 */
		public void objectAdded(TraceObject owner, TraceObject object) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelListener#
		 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
		 */
		public void objectCreationComplete(TraceObject object) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelListener#
		 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
		 *      com.nokia.tracebuilder.model.TraceObject)
		 */
		public void objectRemoved(TraceObject owner, TraceObject object) {
			if (object instanceof Trace) {
				removeInstrumentedTrace(object);
			} else if (object instanceof TraceGroup) {
				Iterator<Trace> itr = ((TraceGroup) object).getTraces();
				while (itr.hasNext()) {
					removeInstrumentedTrace(itr.next());
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelListener#
		 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
		 */
		public void propertyUpdated(TraceObject object, int property) {
		}

	}

	/**
	 * Extension listener for instrumenter
	 * 
	 */
	private final class InstrumenterExtensionListener implements
			TraceModelExtensionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
		 *      extensionAdded(com.nokia.tracebuilder.model.TraceObject,
		 *      com.nokia.tracebuilder.model.TraceModelExtension)
		 */
		public void extensionAdded(TraceObject object,
				TraceModelExtension extension) {
			if (extension instanceof InstrumentedTraceRule) {
				addInstrumentedTrace((InstrumentedTraceRule) extension);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelExtensionListener#
		 *      extensionRemoved(com.nokia.tracebuilder.model.TraceObject,
		 *      com.nokia.tracebuilder.model.TraceModelExtension)
		 */
		public void extensionRemoved(TraceObject object,
				TraceModelExtension extension) {
			if (extension instanceof InstrumentedTraceRule) {
				removeInstrumentedTrace((InstrumentedTraceRule) extension);
			}
		}

	}

	/**
	 * Clears the instrumenter IDs when model is reset
	 * 
	 */
	private final class InstrumenterModelResetListener implements
			TraceModelResetListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelReset()
		 */
		public void modelReset() {
			clearInstrumentation();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelResetting()
		 */
		public void modelResetting() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.model.TraceModelResetListener#modelValid(boolean)
		 */
		public void modelValid(boolean valid) {
		}
	}

	/**
	 * List of instrumented traces
	 */
	private HashMap<String, List<InstrumentedTraceRule>> instrumentedTraces;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public InstrumentationEngine(TraceModel model) {
		model.addResetListener(new InstrumenterModelResetListener());
		model.addModelListener(new InstrumenterModelListener());
		model.addExtensionListener(new InstrumenterExtensionListener());
	}

	/**
	 * Gets a unique ID for instrumentation
	 * 
	 * @return instrumenter ID
	 */
	public String getNewInstrumenterID() {
		SimpleDateFormat format = new SimpleDateFormat("yy-MMM-dd, hh:mm:ss"); //$NON-NLS-1$
		return format.format(Calendar.getInstance().getTime());
	}

	/**
	 * Checks that there are source files that can be instrumented
	 * 
	 * @param sourceEngine
	 *            the source engine
	 * @throws TraceBuilderException
	 *             if sources are not valid
	 */
	public void checkSourceFunctions(SourceEngine sourceEngine)
			throws TraceBuilderException {
		Iterator<SourceProperties> sources = sourceEngine.getSources();
		if (sources.hasNext()) {
			boolean hasFunctions = false;
			while (!hasFunctions && sources.hasNext()) {
				SourceProperties source = sources.next();
				Iterator<SourceContext> contexts = source.getSourceEditor()
						.getContexts();
				if (contexts.hasNext()) {
					hasFunctions = true;
				}
			}
			if (!hasFunctions) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.NO_FUNCTIONS_TO_INSTRUMENT);
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.SOURCE_NOT_OPEN);
		}
	}

	/**
	 * Instrumented trace was added to the model
	 * 
	 * @param rule
	 *            the trace rule
	 */
	private void addInstrumentedTrace(InstrumentedTraceRule rule) {
		if (instrumentedTraces == null) {
			instrumentedTraces = new HashMap<String, List<InstrumentedTraceRule>>();
		}
		List<InstrumentedTraceRule> list = instrumentedTraces.get(rule
				.getInstrumenterID());
		if (list == null) {
			list = new ArrayList<InstrumentedTraceRule>();
			instrumentedTraces.put(rule.getInstrumenterID(), list);
		}
		list.add(rule);
	}

	/**
	 * Instrumented trace was removed
	 * 
	 * @param rule
	 *            the trace rule
	 */
	private void removeInstrumentedTrace(InstrumentedTraceRule rule) {
		if (instrumentedTraces != null) {
			List<InstrumentedTraceRule> list = instrumentedTraces.get(rule
					.getInstrumenterID());
			if (list != null) {
				list.remove(rule);
				if (list.isEmpty()) {
					instrumentedTraces.remove(rule.getInstrumenterID());
				}
			}
		}
	}

	/**
	 * Checks if a trace contains the InstrumentedTraceRule extension and
	 * removes it from the list if it does
	 * 
	 * @param trace
	 *            the trace
	 */
	private void removeInstrumentedTrace(TraceObject trace) {
		InstrumentedTraceRule rule = trace
				.getExtension(InstrumentedTraceRule.class);
		if (rule != null) {
			removeInstrumentedTrace(rule);
		}
	}

	/**
	 * Removes all instrumented traces from the list
	 */
	private void clearInstrumentation() {
		if (instrumentedTraces != null) {
			instrumentedTraces.clear();
		}
	}

	/**
	 * Gets the instrumentation ID list
	 * 
	 * @return the instrumentation IDs
	 */
	Iterator<String> getInstrumenterIDs() {
		Set<String> set;
		if (instrumentedTraces != null) {
			set = instrumentedTraces.keySet();
		} else {
			set = Collections.emptySet();
		}
		return set.iterator();
	}

	/**
	 * Gets the instrumented traces for given ID
	 * 
	 * @param instrumenterID
	 *            the instrumenter ID
	 * @return instrumented trace list
	 */
	Iterator<InstrumentedTraceRule> getInstrumentedTraces(String instrumenterID) {
		List<InstrumentedTraceRule> list;
		if (instrumentedTraces != null) {
			list = instrumentedTraces.get(instrumenterID);
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}
}
