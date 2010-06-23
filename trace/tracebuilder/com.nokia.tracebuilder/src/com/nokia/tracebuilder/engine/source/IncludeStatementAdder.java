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
* Queues addition of #include statement into source file
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.plugin.TraceFormatConstants;
import com.nokia.tracebuilder.plugin.TraceAPIFormatter.TraceFormatType;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceEditor;
import com.nokia.tracebuilder.source.SourceLocation;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Queues addition of <code>#include</code> statement into source file
 * 
 */
class IncludeStatementAdder extends SourceEditorUpdater {

	/**
	 * Name of include file
	 */
	private String includeFile;

	/**
	 * Location (0, 0) -> this is run after other operations
	 */
	private SourceLocationBase location;

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            the source to update
	 * @param include
	 *            the include file to add to the source
	 */
	IncludeStatementAdder(SourceProperties properties, String include) {
		super(properties);
		includeFile = include;
		location = properties.getSourceEditor().createHiddenLocation(0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceEditorUpdater#runUpdate()
	 */
	@Override
	protected boolean runUpdate() throws SourceParserException {
		boolean updated = false;
		SourceEditor sourceEditor = getSource().getSourceEditor();
		try {
			if (includeFile != null) {
				
				// Update excluded areas before finding the include
				sourceEditor.findExcludedAreas();
				
				int index = sourceEditor.findInclude(includeFile);
				if (index >= 0) {
					// Already in the source
					includeFile = null;
				} else {
					index = -1 - index;
					String inc = createIncludeLine();
					sourceEditor.updateSource(index, 0, inc);
					updated = true;
					postIncludeAddedMessage(sourceEditor, index, inc);
				}
			}
		} finally {
			// The location needs to be removed from the source
			sourceEditor.removeHiddenLocation(location);
		}
		return updated;
	}

	/**
	 * Posts an event specifying that include was added to code
	 * 
	 * @param sourceEditor
	 *            the source editor
	 * @param index
	 *            the index where include was added
	 * @param inc
	 *            the include statement
	 */
	private void postIncludeAddedMessage(SourceEditor sourceEditor, int index,
			String inc) {
		// Removes white spaces from the include statement
		String trimmed = inc.trim();
		SourceLocation loc = sourceEditor.createLocation(index
				+ SourceConstants.LINE_FEED.length(), trimmed.length());
		String pref = Messages
				.getString("IncludeStatementAdder.IncludeAddedEventPrefix"); //$NON-NLS-1$
		String post = Messages
				.getString("IncludeStatementAdder.IncludeAddedEventPostfix"); //$NON-NLS-1$
		TraceBuilderGlobals.getEvents().postInfoMessage(
				pref + includeFile + post, loc);
		// The location reference is removed. The event handler has
		// incremented the reference if it stored the location
		loc.dereference();
	}

	/**
	 * Creates the #include statement to be inserted into source
	 * 
	 * @return the string buffer containing the #include
	 */
	private String createIncludeLine() {
		TraceFormattingRule rule = TraceBuilderGlobals.getTraceModel()
				.getExtension(TraceFormattingRule.class);
		String template = rule.getFormat(null, TraceFormatType.INCLUDE_FORMAT);
		int templateIndex = template
				.indexOf(TraceFormatConstants.INCLUDE_FORMAT);
		StringBuffer sb = new StringBuffer();
		sb.append(template);
		sb.append(SourceConstants.LINE_FEED);
		sb.replace(templateIndex, templateIndex
				+ TraceFormatConstants.INCLUDE_FORMAT.length(), includeFile);
		sb.insert(0, SourceConstants.LINE_FEED);
		return sb.toString();
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

}
