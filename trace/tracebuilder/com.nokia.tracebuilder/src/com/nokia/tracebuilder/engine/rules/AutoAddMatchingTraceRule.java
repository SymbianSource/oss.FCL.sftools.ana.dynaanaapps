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
* Trace rule for automatically adding an exit trace when creating an entry trace
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.utils.TraceMultiplierRule;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObjectModifier;
import com.nokia.tracebuilder.model.TraceObjectUtils;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceLocation;
import com.nokia.tracebuilder.source.SourceReturn;

/**
 * Trace rule for automatically adding an exit trace when creating an entry
 * trace.
 * 
 */
final class AutoAddMatchingTraceRule extends RuleBase implements
		TraceMultiplierRule {

	/**
	 * Duplicate name changed warning
	 */
	private static final String DUPLICATE_NAME_CHANGED = Messages
			.getString("AutoAddMatchingTraceRule.DuplicateName"); //$NON-NLS-1$
	/**
	 * Return statement has unwanted side effects
	 */
	private static final String INVALID_RETURN_STATEMENT = Messages
			.getString("FunctionReturnLocationMultiplierRule.InvalidReturnStatement"); //$NON-NLS-1$

	/**
	 * The template that was used to create the entry trace
	 */
	private TraceObjectPropertyDialogTemplate entryTemplate;

	/**
	 * Constructor
	 * 
	 * @param template
	 *            the template for the entry trace
	 */
	AutoAddMatchingTraceRule(TraceObjectPropertyDialogTemplate template) {
		entryTemplate = template;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceMultiplierRule#
	 * createCopies(com.nokia.tracebuilder.model.Trace)
	 */
	public Iterator<Trace> createCopies(Trace trace) {
		List<Trace> exitTraces = null;
		SourceContext context = TraceBuilderGlobals.getSourceContextManager()
				.getContext();
		if (context != null) {
			exitTraces = new ArrayList<Trace>();
			copyTrace(exitTraces, trace, context);
		} else {
			exitTraces = Collections.emptyList();
		}
		return exitTraces.iterator();
	}

	/**
	 * Creates the exit traces from entry trace and adds it to given list
	 * 
	 * @param exitTraces
	 *            the list where exit traces are added
	 * @param trace
	 *            the entry trace
	 * @param context
	 *            the active source context
	 */
	private void copyTrace(List<Trace> exitTraces, Trace trace,
			SourceContext context) {
		String name;
		String text;
		String cname = context.getClassName();
		String fname = context.getFunctionName();
		if (entryTemplate instanceof ExitTracePropertyBuilder) {
			name = ((ExitTracePropertyBuilder) entryTemplate)
					.createExitName(trace.getName());
			text = ((ExitTracePropertyBuilder) entryTemplate)
					.createExitText(trace.getTrace());
		} else {
			name = TraceUtils.formatTrace(TraceUtils.getDefaultNameFormat(),
					cname, fname);
			text = TraceUtils.formatTrace(TraceUtils.getDefaultTraceFormat(),
					cname, fname);
		}
		// Performance event gets one matching exit trace
		// Function entry-exit is copied to all return statements
		PerformanceEventStartRule rule = trace
				.getExtension(PerformanceEventStartRule.class);
		if (rule == null) {
			ArrayList<SourceReturn> returnList = new ArrayList<SourceReturn>();
			RuleUtils.getCurrentContextReturns(returnList);
			AutoAddReturnParameterRule.resetNumberOfHandledReturnStatements();
			for (SourceReturn ret : returnList) {
				Trace exitTrace = processReturnStatement(trace, ret, name, text);
				if (exitTrace != null) {
					exitTraces.add(exitTrace);
				}
			}
		} else {
			try {
				Trace exitTrace = createTrace(trace, name, text);
				if (exitTrace != null) {
					exitTraces.add(exitTrace);
				}
			} catch (TraceBuilderException e) {
				TraceBuilderGlobals.getEvents().postError(e);
			}
		}
		// All copy-and-remove rules are removed from the original trace
		trace.removeExtensions(CopyAndRemoveExtensionRule.class);
	}

	/**
	 * Processes a return statement from source context
	 * 
	 * @param entryTrace
	 *            the entry trace
	 * @param ret
	 *            the return statement
	 * @param name
	 *            the name for exit trace
	 * @param text
	 *            the text for exit trace
	 * @return the new trace or null if creation fails
	 */
	private Trace processReturnStatement(Trace entryTrace, SourceReturn ret,
			String name, String text) {
		Trace exitTrace = null;
		boolean valid = checkTraceValidity(ret);
		// Invalid exit traces are not created
		if (valid) {
			try {
				exitTrace = createTrace(entryTrace, name, text);
				exitTrace.addExtension(new FunctionReturnLocationRule(ret));
			} catch (TraceBuilderException e) {
				TraceBuilderGlobals.getEvents().postError(e);
			}
		} else {
			// Even we did not add new exit trace we handled this return
			// statement
			AutoAddReturnParameterRule
					.increaseNumberOfHandledReturnStatements();
		}
		return exitTrace;
	}

	/**
	 * Creates the exit trace from entry trace
	 * 
	 * @param entryTrace
	 *            the entry trace
	 * @param name
	 *            the name for exit trace
	 * @param text
	 *            the text for exit trace
	 * @return the exit trace
	 * @throws TraceBuilderException
	 *             if creation fails
	 */
	private Trace createTrace(Trace entryTrace, String name, String text)
			throws TraceBuilderException {
		TraceGroup group = entryTrace.getGroup();
		TraceObjectModifier nameModifier;
		// Formats the name and text according to the exit template
		nameModifier = TraceObjectUtils.modifyDuplicateTraceName(group
				.getModel(), name);
		int id = group.getNextTraceID();
		String newName = nameModifier.getData();
		group.getModel().getVerifier().checkTraceProperties(group, null, id,
				newName, text);
		TraceModelExtension[] extArray = copyExtensions(entryTrace);
		Trace exitTrace = entryTrace.getModel().getFactory().createTrace(group,
				id, newName, text, extArray);
		if (nameModifier.hasChanged()) {
			TraceBuilderGlobals.getEvents().postWarningMessage(
					DUPLICATE_NAME_CHANGED + name, exitTrace);
		}
		return exitTrace;
	}

	/**
	 * Checks the validity of an exit trace
	 * 
	 * @param ret
	 *            the return statement
	 * @return the trace validity
	 */
	private boolean checkTraceValidity(SourceReturn ret) {
		boolean valid = true;
		// Previous character hazard is always checked
		if (ret.hasPreviousCharHazard()) {
			SourceLocation loc = ret.getParser().createLocation(
					ret.getOffset(), ret.getLength());
			TraceBuilderGlobals.getEvents().postWarningMessage(
					INVALID_RETURN_STATEMENT, loc);
			loc.dereference();
			valid = false;

			// Tag has function call or increment / decrement values
		} else if (ret.hasTagHazard()) {
			SourceLocation loc = ret.getParser().createLocation(
					ret.getOffset(), ret.getLength());
			TraceBuilderGlobals.getEvents().postWarningMessage(
					INVALID_RETURN_STATEMENT, loc);
			loc.dereference();
			valid = false;
		}
		return valid;
	}

	/**
	 * Copies the extensions from the entry trace
	 * 
	 * @param trace
	 *            the trace
	 * @return the list of extensions to be added to exit trace
	 */
	private TraceModelExtension[] copyExtensions(Trace trace) {
		ArrayList<TraceModelExtension> extList = new ArrayList<TraceModelExtension>();
		Iterator<CopyExtensionRule> extItr = trace
				.getExtensions(CopyExtensionRule.class);
		int extCount = 0;
		while (extItr.hasNext()) {
			CopyExtensionRule rule = extItr.next();
			extList.add(rule.createCopy());
			extCount++;
		}
		TraceModelExtension[] extArray = null;
		if (extCount > 0) {
			extArray = new TraceModelExtension[extCount];
			extList.toArray(extArray);
		}
		return extArray;
	}

}
