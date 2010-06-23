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
* Base class for context-based templates
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TracePropertyDialogTemplate;
import com.nokia.tracebuilder.engine.propertydialog.ContextBasedTemplate;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.TraceObjectUtils;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Base class for context-based templates
 * 
 */
abstract class ContextTemplateBase implements TracePropertyDialogTemplate,
		ContextBasedTemplate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getName()
	 */
	public String getName() {
		// Trace name must be unique within the model
		return TraceObjectUtils.modifyDuplicateTraceName(
				TraceBuilderGlobals.getTraceModel(), getFormattedTraceName())
				.getData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getName()
	 */
	public String getValue() {
		return getFormattedTraceText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TracePropertyDialogTemplate#getText(java.lang.String)
	 */
	public String getText(String groupName) {
		return getFormattedTraceText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isIDEnabled()
	 */
	public boolean isIDEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isNameEnabled()
	 */
	public boolean isNameEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isValueEnabled()
	 */
	public boolean isValueEnabled() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.TraceObjectPropertyDialogTemplate#isTargetEnabled()
	 */
	public boolean isTargetEnabled() {
		return true;
	}		

	/**
	 * Gets trace name based on context
	 * 
	 * @return the trace name
	 */
	private String getFormattedTraceName() {
		String name;
		SourceContext context = TraceBuilderGlobals.getSourceContextManager()
				.getContext();
		if (context != null) {
			String fname = context.getFunctionName();
			String cname = context.getClassName();
			if (fname != null) {
				name = TraceUtils.convertName(TraceUtils.formatTrace(
						getTraceNameFormat(), cname, fname));
			} else {
				name = ""; //$NON-NLS-1$
			}
		} else {
			name = ""; //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Gets trace text based on the context
	 * 
	 * @return trace text
	 */
	private String getFormattedTraceText() {
		String value;
		SourceContext context = TraceBuilderGlobals.getSourceContextManager()
				.getContext();
		if (context != null) {
			String fname = context.getFunctionName();
			String cname = context.getClassName();
			if (fname != null) {
				value = TraceUtils.formatTrace(getTraceTextFormat(), cname,
						fname);
			} else {
				value = ""; //$NON-NLS-1$
			}
		} else {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}

	/**
	 * Gets the trace name format
	 * 
	 * @return the format
	 */
	protected abstract String getTraceNameFormat();

	/**
	 * Gets the trace text format
	 * 
	 * @return the format
	 */
	protected abstract String getTraceTextFormat();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.ContextBasedTemplate#
	 *      isAvailableInContext(com.nokia.tracebuilder.source.SourceContext)
	 */
	public boolean isAvailableInContext(SourceContext context) {
		return context != null;
	}

}
