/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
* Properties associated with formatting rules
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.rules.osttrace.OstTraceFormatRule;
import com.nokia.tracebuilder.engine.rules.osttrace.OstTraceParserRule;
import com.nokia.tracebuilder.engine.source.SourceParserRule;
import com.nokia.tracebuilder.project.TraceProjectAPI;

/**
 * Constants for rules engine
 * 
 */
interface RulesEngineConstants {

	/**
	 * Trace parsers
	 */
	SourceParserRule[] TRACE_PARSERS = { new OstTraceParserRule() };

	/**
	 * Printf parsers
	 */
	String[] PRINTF_PARSERS = { "RDebug::Print", //$NON-NLS-1$
			"Kern::Printf" //$NON-NLS-1$
	};

	/**
	 * List of supported API's
	 */
	TraceProjectAPI[] TRACE_APIS = { new OstTraceFormatRule() };

	/**
	 * Persistent extensions
	 */
	ClassNameWrapper[] PERSISTENT_EXTENSIONS = {
			new ClassNameWrapper(InstrumentedTraceRuleImpl.STORAGE_NAME,
					InstrumentedTraceRuleImpl.class),
			new ClassNameWrapper(ParameterTypeMappingRule.STORAGE_NAME,
					ParameterTypeMappingRule.class),
			new ClassNameWrapper(ArrayParameterRuleImpl.STORAGE_NAME,
					ArrayParameterRuleImpl.class) };

	/**
	 * Dialog flags for "Add Trace" dialog
	 */
	TraceObjectPropertyDialogFlag[] TRACE_FLAGS = { new AddThisPtrFlag(),
			new AddFunctionParametersFlag(), new AddMatchingTraceFlag(), new AddReturnParameterFlag() };

	/**
	 * Dialog flags for instrumenter dialog.
	 */
	TraceObjectPropertyDialogFlag[] INSTRUMENTER_FLAGS = TRACE_FLAGS;

	/**
	 * Dialog flags for "Add Parameter" dialog
	 */
	TraceObjectPropertyDialogFlag[] PARAMETER_FLAGS = { new ArrayParameterFlag() };

	/**
	 * Templates used in "Add Trace" dialog
	 */
	TraceObjectPropertyDialogTemplate[] TRACE_TEMPLATES = {
			new EntryExitTraceTemplate(), new PerformanceEventTemplate(), new StateTraceTemplate()};

	/**
	 * Templates used in instrumenter dialog
	 */
	TraceObjectPropertyDialogTemplate[] INSTRUMENTER_TEMPLATES = { new EntryExitInstrumenterTemplate() };

	/**
	 * Templates used with "Add Parameter" dialog
	 */
	TraceObjectPropertyDialogTemplate[] PARAMETER_TEMPLATES = { new ThisPointerParameterTemplate() };

}
