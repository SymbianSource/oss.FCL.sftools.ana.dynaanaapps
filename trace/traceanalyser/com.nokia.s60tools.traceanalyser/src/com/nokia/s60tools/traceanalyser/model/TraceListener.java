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



package com.nokia.s60tools.traceanalyser.model;


import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.export.RuleEvent.RuleStatus;
import com.nokia.traceviewer.api.DPLocation;
import com.nokia.traceviewer.api.TraceViewerAPI;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;

/**
 * class TraceListener.
 * Class that is responsible for listening to trace.
 */
public class TraceListener implements DataProcessor {

	/* Trace Analyser engine */ 
	Engine engine;
	
	/* Rule array */
	TraceAnalyserRule[] rules = null;
	
	/**
	 * TraceListener.
	 * constructor.
	 * @param engine Trace Analyser Engine.
	 */
	public TraceListener(Engine engine){
		this.engine = engine;
		//Add DataProcessor to TraceViewer
		TVAPIError error = TVAPIError.NONE;
		error = TraceViewerAPI.addDataProcessor(this, DPLocation.AFTER_VIEW, 50); // CodForChk_Dis_Magic
		
		// Check error code
		if( error != TVAPIError.NONE ){
			System.out.println("Unable to add dataprocessor to TraceViewer");

		}
	}
	
	/**
	 * stopListening.
	 * stops listening to trace.
	 */
	public void stopListening(){
		//Add DataProcessor to TraceViewer
		TVAPIError error = TVAPIError.NONE;
		error = TraceViewerAPI.removeDataProcessor(this);
		
		// Check error code
		if( error != TVAPIError.NONE ){
			System.out.println("Unable to remove dataprocessor from TraceViewer");
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.traceviewer.engine.dataprocessor.DataProcessor#processData(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public void processData(TraceProperties traceEvent) {
			
		// Make sure that this is not scrolled trace
		if(! traceEvent.traceConfiguration.isScrolledTrace()){
			if(rules != null){
				
				// forwards trace to all rules and if rule some rule event is detected it is sent to engine and fail log.
				for(TraceAnalyserRule rule:rules){
					
					// Check rule status.
					RuleEvent event = rule.checkRuleStatus(traceEvent);
					
				
					if(event.getStatus() == RuleStatus.FAIL){
					
						// if rule failed, create add item to fail log log item.
						engine.addFailLogItem(event);
					}
					if(event.getStatus() != RuleStatus.NONE){
						// is some event(either fail or pass) is detected, sent it to engine.
						engine.addRuleEvent(rule.getName(), event);
					}
				}
			}
		}
	}

	/**
	 * setRules.
	 * @param traceAnalyserRules new rule array.
	 */
	public void setRules(TraceAnalyserRule[] traceAnalyserRules) {
		this.rules = traceAnalyserRules;
	}


	

}
