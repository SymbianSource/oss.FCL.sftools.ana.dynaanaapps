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
*/


package com.nokia.s60tools.traceanalyser.containers;

import java.util.ArrayList;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.traceviewer.engine.TraceProperties;


/**
 * class DummyRule.
 * DummyRule that is used for sending messages from rule manager to main view.
 */
public class DummyRule extends TraceAnalyserRule {

	/**
	 * DummyRule
	 * @param type Type of the rule.
	 */
	public DummyRule(String type) {
		super(type);
		name = type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#checkRuleStatus(com.nokia.traceviewer.engine.TraceProperties)
	 */
	@Override
	public RuleEvent checkRuleStatus(TraceProperties traceProperties) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#getLimitUnit()
	 */
	@Override
	public String getUnit() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#readHistory()
	 */
	@Override
	public ArrayList<RuleEvent> readHistory() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#saveHistory(java.util.ArrayList)
	 */
	@Override
	public boolean saveHistory(ArrayList<RuleEvent> history) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#writeXML()
	 */
	@Override
	public boolean writeXML() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#equals(com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule)
	 */
	@Override
	public boolean equals(TraceAnalyserRule rule) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#getLimits()
	 */
	@Override
	public int[] getLimits() {
		return null;
	}
	
	

}
