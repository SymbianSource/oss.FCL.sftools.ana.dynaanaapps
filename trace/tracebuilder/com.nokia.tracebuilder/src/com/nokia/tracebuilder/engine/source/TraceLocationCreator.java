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
* SourceEditorUpdater instance to add a new trace location
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.engine.TraceLocationProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParser;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * SourceEditorUpdater instance to add a new trace location
 * 
 */
class TraceLocationCreator extends SourceEditorUpdater {

	/**
	 * Trace to be added to source
	 */
	private Trace trace;

	/**
	 * Offset where trace is added
	 */
	private SourceLocationBase location;

	/**
	 * Trace location properties
	 */
	private TraceLocationProperties locationProperties;

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            the source to be updated
	 * @param trace
	 *            the trace to be added to source
	 * @param offset
	 *            the offset where trace is added
	 * @param locationProperties
	 *            optional properties for the new location
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	TraceLocationCreator(SourceProperties properties, Trace trace, int offset,
			TraceLocationProperties locationProperties)
			throws SourceParserException {
		super(properties);
		this.trace = trace;
		this.locationProperties = locationProperties;
		// The location properties may adjust the location where the trace gets
		// inserted. If the location properties do not contain a location rule,
		// the trace is queried for it
		SourceLocationRule locationRule = null;
		if (locationProperties != null) {
			locationRule = locationProperties.getLocationRule();
		}
		if (locationRule == null) {
			locationRule = trace.getExtension(SourceLocationRule.class);
			// The extension is removed from the trace to avoid relocation
			// problems when inserting the trace to other places
			if (locationRule != null && locationRule.isRemovedAfterInsert()) {
				trace.removeExtension(locationRule);
			}
		}
		offset = processLocationRule(locationRule, offset);
		// Offset is stored into the document
		// This is needed since the update is run asynchronously and the
		// document may change before this update has chance to run
		location = properties.getSourceEditor().createHiddenLocation(offset, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#runUpdate()
	 */
	@Override
	protected boolean runUpdate() throws SourceParserException {
		SourceProperties source = getSource();
		try {
			source.internalInsertTrace(trace, location.getOffset(), null,
					locationProperties);
		} finally {
			// The location is removed after use
			source.getSourceEditor().removeHiddenLocation(location);
		}
		// Always makes an update
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#getPosition()
	 */
	@Override
	protected SourceLocationBase getPosition() {
		return location;
	}

	/**
	 * Processes the location rule of the trace and adjusts the
	 * traceInsertLocation if the rule exists
	 * 
	 * @param locationRule
	 *            the location rule of the trace
	 * @param offset
	 *            the original offset
	 * @return the new offset or -1 if not adjusted
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	private int processLocationRule(SourceLocationRule locationRule, int offset)
			throws SourceParserException {
		SourceParser sourceEditor = getSource().getSourceEditor();
		if (locationRule != null) {
			int start = 0;
			int end = sourceEditor.getDataLength();
			SourceContext context;
			switch (locationRule.getLocationType()) {
			case SourceLocationRule.ABSOLUTE:
				offset = locationRule.getLocationOffset();
				break;
			case SourceLocationRule.CONTEXT_RELATIVE:
				context = sourceEditor.getContext(offset);
				if (context != null) {
					offset = locationRule.getLocationOffset()
							+ context.getOffset();
					// If location traceInsertLocation is >0, the trace is exit
					// trace and
					// this needs to search for return statements
					if (locationRule.getLocationOffset() > 0) {
						end = sourceEditor.findReturn(context);
					} else {
						end = context.getOffset() + context.getLength();
					}
					start = context.getOffset();
				}
				break;
			}
			if (offset > end) {
				offset = end;
			} else if (offset < start) {
				offset = start;
			}
		}
		// Location is inserted to the beginning of the line
		// Indent is calculated when the location is inserted
		return sourceEditor.findStartOfLine(offset, false, true);
	}

}