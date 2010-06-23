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
* Implementation of TraceObjectPropertyDialogConfiguration interface
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogConfiguration;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;

/**
 * Implementation of TraceObjectPropertyDialogConfiguration interface
 * 
 */
final class PropertyDialogConfiguration implements
		TraceObjectPropertyDialogConfiguration {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogConfiguration#
	 *      addViewFlags(java.util.List, int)
	 */
	public void addViewFlags(List<TraceObjectPropertyDialogFlag> flags,
			int dialogType) {
		if (dialogType == TraceObjectPropertyDialog.ADD_TRACE) {
			addFlagsFromArray(flags, RulesEngineConstants.TRACE_FLAGS);
		} else if (dialogType == TraceObjectPropertyDialog.ADD_PARAMETER) {
			addFlagsFromArray(flags, RulesEngineConstants.PARAMETER_FLAGS);
		} else if (dialogType == TraceObjectPropertyDialog.INSTRUMENTER) {
			addFlagsFromArray(flags, RulesEngineConstants.INSTRUMENTER_FLAGS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogConfiguration#
	 *      addViewTemplates(java.util.List, int)
	 */
	public void addViewTemplates(List<TraceObjectPropertyDialogTemplate> list,
			int dialogType) {
		if (dialogType == TraceObjectPropertyDialog.ADD_TRACE) {
			addTemplatesFromArray(list, RulesEngineConstants.TRACE_TEMPLATES);
		} else if (dialogType == TraceObjectPropertyDialog.ADD_PARAMETER) {
			addTemplatesFromArray(list,
					RulesEngineConstants.PARAMETER_TEMPLATES);
		} else if (dialogType == TraceObjectPropertyDialog.INSTRUMENTER) {
			addTemplatesFromArray(list,
					RulesEngineConstants.INSTRUMENTER_TEMPLATES);
		}
	}

	/**
	 * Adds flags from given array to given list
	 * 
	 * @param flags
	 *            the list
	 * @param flagArray
	 *            the array
	 */
	private void addFlagsFromArray(List<TraceObjectPropertyDialogFlag> flags,
			TraceObjectPropertyDialogFlag[] flagArray) {
		for (TraceObjectPropertyDialogFlag element : flagArray) {
			flags.add(element);
		}
	}

	/**
	 * Adds templates from given array to given list
	 * 
	 * @param templates
	 *            the list
	 * @param templateArray
	 *            the array
	 */
	private void addTemplatesFromArray(
			List<TraceObjectPropertyDialogTemplate> templates,
			TraceObjectPropertyDialogTemplate[] templateArray) {
		for (TraceObjectPropertyDialogTemplate element : templateArray) {
			templates.add(element);
		}
	}

}