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
* Dialog callback to process instrumentation
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;
import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogParameters;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs.CheckListDialogType;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.StringErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObjectModifier;
import com.nokia.tracebuilder.model.TraceObjectUtils;
import com.nokia.tracebuilder.project.FormattingUtils;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceParser;

/**
 * Dialog callback to process instrumentation
 * 
 */
public final class RunInstrumenterCallback extends PropertyDialogCallback {

	/**
	 * Duplicate name changed warning
	 */
	private static final String DUPLICATE_NAME_CHANGED = Messages
			.getString("RunInstrumenterCallback.DuplicateName"); //$NON-NLS-1$

	/**
	 * Source engine for trace additions
	 */
	private SourceEngine sourceEngine;

	/**
	 * Context manager
	 */
	private SourceContextManager contextManager;

	/**
	 * Instrumenter ID
	 */
	private String instrumenterID;

	/**
	 * Creates a new instrumenter callback
	 * 
	 * @param model
	 *            the trace model
	 * @param sourceEngine
	 *            the source engine
	 * @param contextManager
	 *            the source context manager
	 * @param instrumenterID
	 *            the instrumenter ID
	 */
	public RunInstrumenterCallback(TraceModel model, SourceEngine sourceEngine,
			SourceContextManager contextManager, String instrumenterID) {
		super(model);
		this.sourceEngine = sourceEngine;
		this.contextManager = contextManager;
		this.instrumenterID = instrumenterID;
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
		// If group does not exist, it will be created
		TraceGroup group = model.findGroupByName(groupName);
		int groupId = 0;
		if (group == null) {
			groupId = FormattingUtils.getGroupID(model, groupName);
			model.getVerifier().checkTraceGroupProperties(model, null, groupId,
					groupName);
		}
		Iterator<SourceContext> contexts = showFunctionQueryDialog(dialog);
		if (contexts != null) {
			String oldCategory = TraceBuilderGlobals.getEvents()
					.setEventCategory("Instrumenter"); //$NON-NLS-1$
			contextManager.setInstrumenterID(instrumenterID);
			model.startProcessing();
			try {
				// Group is created if it does not exist
				if (group == null && contexts.hasNext()) {
					group = model.getFactory().createTraceGroup(groupId,
							groupName, null);
				}
				while (contexts.hasNext()) {
					SourceContext context = contexts.next();
					contextManager.setContext(context);
					processContext(dialog, group, context);
				}
			} finally {
				model.processingComplete();
				// These must be reset even if errors occurs
				contextManager.setContext(null);
				contextManager.setInstrumenterID(""); //$NON-NLS-1$
				TraceBuilderGlobals.getEvents().setEventCategory(oldCategory);
			}
		}
	}

	/**
	 * Shows the function tree view, which can be used to select the functions
	 * to be instrumented
	 * 
	 * @param dialog
	 *            the property dialog
	 * @return the iterator of functions to be instrumented
	 * @throws TraceBuilderException
	 *             if instrumenter fails
	 */
	private Iterator<SourceContext> showFunctionQueryDialog(
			TraceObjectPropertyDialog dialog) throws TraceBuilderException {
		Iterator<SourceContext> retval = null;
		CheckListDialogEntry root = createFunctionQueryDialogTree(dialog);
		if (root.hasChildren()) {
			ArrayList<CheckListDialogEntry> rootItems = new ArrayList<CheckListDialogEntry>();
			rootItems.add(root);
			CheckListDialogParameters params = new CheckListDialogParameters();
			params.dialogType = CheckListDialogType.INSTRUMENT_FILES;
			params.rootItems = rootItems;
			params.expandLevel = 1;
			params.showRoot = false;
			int res = TraceBuilderGlobals.getDialogs().showCheckList(params);
			if (res == TraceBuilderDialogs.OK) {
				retval = getCheckedContexts(root);
			}
		} else {
			StringErrorParameters sp = new StringErrorParameters();
			sp.string = dialog.getTemplate().getTitle();
			throw new TraceBuilderException(
					TraceBuilderErrorCode.NO_FUNCTIONS_TO_INSTRUMENT_WITH_TEMPLATE,
					sp);
		}
		return retval;
	}

	/**
	 * Creates the tree for the query dialog
	 * 
	 * @param dialog
	 *            the property dialog
	 * @return the root of the tree
	 */
	private CheckListDialogEntry createFunctionQueryDialogTree(
			TraceObjectPropertyDialog dialog) {
		TraceObjectPropertyDialogTemplate template = dialog.getTemplate();
		CheckListDialogEntry root = new CheckListDialogEntry();
		// Tells the dialog to go through children and check those that are
		// checked
		root.setChecked(true);
		for (SourceProperties source : sourceEngine) {
			ArrayList<String> nonSourceFiles = sourceEngine.getNonSourceFiles();
			if (!nonSourceFiles.contains(source.getFilePath()+ source.getFileName())) {
				addSourceToList(template, root, source);
			}
		}
		return root;
	}

	/**
	 * Adds a source to the query dialog tree
	 * 
	 * @param template
	 *            the template from the property dialog
	 * @param root
	 *            the root entry
	 * @param source
	 *            the source properties
	 */
	private void addSourceToList(TraceObjectPropertyDialogTemplate template,
			CheckListDialogEntry root, SourceProperties source) {
		if (!source.isReadOnly()) {
			SourceParser parser = source.getSourceEditor();
			CheckListDialogEntry sourceEntry = new CheckListDialogEntry();
			sourceEntry.setObject(source.getFileName());
			Iterator<SourceContext> contexts = parser.getContexts();
			while (contexts.hasNext()) {
				addContextToList(template, sourceEntry, contexts.next());
			}
			if (sourceEntry.hasChildren()) {
				root.addChild(sourceEntry);
			}
		}
	}

	/**
	 * Adds a context to the query dialog tree
	 * 
	 * @param template
	 *            the template from the property dialog
	 * @param sourceEntry
	 *            the source where the context is to be added
	 * @param context
	 *            the source context to be added
	 */
	private void addContextToList(TraceObjectPropertyDialogTemplate template,
			CheckListDialogEntry sourceEntry, SourceContext context) {
		boolean available = true;
		if (template instanceof ContextBasedTemplate) {
			available = ((ContextBasedTemplate) template)
					.isAvailableInContext(context);
		}
		if (available) {
			CheckListDialogEntry contextEntry = new CheckListDialogEntry();
			contextEntry.setObject(context);
			contextEntry.setChecked(true);
			sourceEntry.addChild(contextEntry);
		}
	}

	/**
	 * Gets the checked contexts from the query tree
	 * 
	 * @param root
	 *            the root of the tree
	 * @return iterator of checked contexts
	 */
	private Iterator<SourceContext> getCheckedContexts(CheckListDialogEntry root) {
		ArrayList<SourceContext> contextList = new ArrayList<SourceContext>();
		for (CheckListDialogEntry entry : root) {
			for (CheckListDialogEntry context : entry) {
				if (context.isChecked()) {
					contextList.add((SourceContext) context.getObject());
				}
			}
		}
		Iterator<SourceContext> retval;
		if (!contextList.isEmpty()) {
			retval = contextList.iterator();
		} else {
			retval = null;
		}
		return retval;
	}

	/**
	 * Processes the given context
	 * 
	 * @param dialog
	 *            the flags from the dialog
	 * @param group
	 *            the target trace group
	 * @param context
	 *            the context to be processed
	 */
	private void processContext(TraceObjectPropertyDialog dialog,
			TraceGroup group, SourceContext context) {
		try {
			int id = group.getNextTraceID();
			String name = TraceUtils.convertName(formatTrace(dialog.getName(),
					context));
			String value = formatTrace(dialog.getValue(), context);
			TraceObjectModifier nameModifier = TraceObjectUtils
					.modifyDuplicateTraceName(group.getModel(), name);
			group.getModel().getVerifier().checkTraceProperties(group, null,
					id, nameModifier.getData(), value);
			TraceModelExtension[] extArray = createExtensions(group, dialog);
			Trace trace = group.getModel().getFactory().createTrace(group, id,
					nameModifier.getData(), value, extArray);
			if (nameModifier.hasChanged()) {
				TraceBuilderGlobals.getEvents().postWarningMessage(
						DUPLICATE_NAME_CHANGED + name, trace);
			}
			sourceEngine.insertTrace(trace, sourceEngine
					.getSourceOfContext(context), context.getOffset());
			TraceUtils.multiplyTrace(trace, context.getOffset(), sourceEngine);

			SourceProperties properties = sourceEngine
					.getSourceOfContext(context);
			String fileName = context.getFileName();
			if (fileName != null) {
				String headerFileName = TraceBuilderGlobals.getHeaderFileName(fileName);
				sourceEngine.addInclude(properties, headerFileName);
			}

		} catch (TraceBuilderException e) {
			TraceBuilderGlobals.getEvents().postError(e);
		}
	}

	/**
	 * Formats the trace specified into instrumenter dialog
	 * 
	 * @param format
	 *            the formatting
	 * @param context
	 *            the context where trace is added
	 * @return the formatted trace
	 */
	private String formatTrace(String format, SourceContext context) {
		String cname = context.getClassName();
		String fname = context.getFunctionName();
		return TraceUtils.formatTrace(format, cname, fname);
	}
}
