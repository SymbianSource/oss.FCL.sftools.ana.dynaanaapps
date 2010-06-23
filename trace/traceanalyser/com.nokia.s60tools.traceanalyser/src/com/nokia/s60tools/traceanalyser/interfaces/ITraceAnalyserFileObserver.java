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



package com.nokia.s60tools.traceanalyser.interfaces;

/**
 * interface ITraceAnalyserFileObserver.
 * interface that Trace Analyser File managers use when "read files"- operation is finished.
 */
public interface ITraceAnalyserFileObserver {
	/**
	 * rulesUpdated.
	 * Rules are updated and all rules need to be reread.
	 */
	public void rulesUpdated();

	/**
	 * ruleUpdated.
	 * One rule's history has been changed.
	 */
	public void ruleUpdated(String ruleName);
	
	/**
	 * failLogUpdated.
	 * Fail log is updated.
	 */
	public void failLogUpdated();
	
}
