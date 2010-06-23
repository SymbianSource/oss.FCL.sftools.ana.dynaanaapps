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
* Parser rule, which delegates calls to plug-in API
*
*/
package com.nokia.tracebuilder.engine.rules.plugin;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.rules.SourceParserRuleBase;
import com.nokia.tracebuilder.engine.source.SourceParserResult;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.plugin.TraceAPIParser;

/**
 * Parser rule, which delegates calls to plug-in API
 * 
 */
public final class PluginTraceParserRule extends SourceParserRuleBase {

	/**
	 * Trace parser plug-in
	 */
	private TraceAPIParser parser;

	/**
	 * Creates a new parser rule
	 * 
	 * @param parser
	 *            the parser API
	 */
	public PluginTraceParserRule(TraceAPIParser parser) {
		super(parser.getSourceTag(), parser.getTagSuffixes());
		this.parser = parser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#getName()
	 */
	public String getName() {
		// TODO: Add to API or get rid of getName
		return "PluginParser"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.SourceParserRuleBase#getLocationGroup()
	 */
	@Override
	public String getLocationGroup() {
		return parser.getLocationGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      parseParameters(java.util.List)
	 */
	public SourceParserResult parseParameters(String tag, List<String> list)
			throws TraceBuilderException {
		SourceParserResult result = new SourceParserResult();
		if (list.size() >= 1) {
			result.originalName = list.get(0);
			result.convertedName = result.originalName;
			result.traceText = result.originalName;
			result.parameters = new ArrayList<String>();
			for (int i = 1; i < list.size(); i++) {
				result.parameters.add(list.get(i));
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.NOT_ENOUGH_PARAMETERS);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.rules.SourceParserRuleBase#
	 *      checkLocationValidity(com.nokia.tracebuilder.engine.TraceLocation,
	 *      boolean)
	 */
	@Override
	public TraceBuilderErrorCode checkLocationValidity(TraceLocation location) {
		TraceBuilderErrorCode error = super.checkLocationValidity(location);
		if (error == TraceBuilderErrorCode.OK) {
			error = parser.checkLocationValidity(location);
		}
		return error;
	}

}
