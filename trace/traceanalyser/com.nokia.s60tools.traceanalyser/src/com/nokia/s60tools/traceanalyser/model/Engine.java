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

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;


import com.nokia.s60tools.traceanalyser.containers.RuleInformation;
import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver;
import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;
import com.nokia.s60tools.traceanalyser.ui.views.MainView;

/**
 * class engine.
 * Engine of Trace Analyser extension.
 */

public class Engine implements ITraceAnalyserFileObserver {
	
	/* Fail Log Manager */
	private FailLogManager failLogManager;
	
	/* Rule Manager */
	private RuleManager ruleManager;	
	
	/* Trace Listener */
	private TraceListener traceListener;
	
	/* list of rule types */
	ArrayList<ITraceAnalyserRuleType> ruleTypes;
	
	/* Main view */
	MainView mainView;
	
	/**
	 * Engine.
	 * constructor.
	 */
	public Engine(){
		
		// Get rule types
		ruleTypes = new ArrayList<ITraceAnalyserRuleType>();
		getRuleTypeArray();
		
		// create fail log manager
		failLogManager = new FailLogManager(this);
		
		// create rule manager
		ruleManager = new RuleManager(this, ruleTypes);
		
		// create trace listener
		traceListener = new TraceListener(this);
		
		// refresh fail log and rule information.
		failLogManager.refresh();
		ruleManager.refresh();
	}

	/**
	 * setMainView.
	 * @param mainView main view.
	 */
	public void setMainView(MainView mainView){
		this.mainView = mainView;
	}


	/**
	 * getRuleArray.
	 * Searches for rule type plug-ins and creates rule array from them.
	 */
	private void getRuleTypeArray(){

		final String EXTENSION_TRACE_ANALYSER_RULE = "traceanalyserrule"; //$NON-NLS-1$

		try {
			IExtensionRegistry er = Platform.getExtensionRegistry();
			IExtensionPoint ep = 
				er.getExtensionPoint(TraceAnalyserPlugin.PLUGIN_ID, EXTENSION_TRACE_ANALYSER_RULE);
			IExtension[] extensions = ep.getExtensions();

			// if plug-ins were found.
			if (extensions != null && extensions.length > 0) {
				
				// read all found rules
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] ce = extensions[i].getConfigurationElements();
					if (ce != null && ce.length > 0) {
						try {
							ITraceAnalyserRuleType provider = (ITraceAnalyserRuleType)ce[0].createExecutableExtension("class");
							if (provider != null) {
								ruleTypes.add(provider);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * getFailLog
	 * @return fail log
	 */
	public RuleEvent[] getFailLog(){
		return failLogManager.getTraceAnalyserFailLog();
	}
	
	/**
	 * refreshFailLog.
	 * Refreshes Fail log
	 */
	public void refreshFailLog(){
		failLogManager.refresh();
	}
	
	/**
	 * clearFailLog.
	 * Clears fail log.
	 */
	public void clearFailLog(){
		failLogManager.clearLog();
	}
	
	/**
	 * addFailLogItem.
	 * @param item, new fail log item.
	 */
	public void addFailLogItem(RuleEvent item){
		failLogManager.addItem(item);
		if(mainView != null){
			mainView.blinkIcon();
		}
	}

	/**
	 * stop.
	 * Stops Trace Analyser Engine.
	 */
	public void stop(){
		failLogManager.saveLogToFile();
		ruleManager.saveData();
		traceListener.stopListening();
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#failLogUpdated()
	 */
	public void failLogUpdated() {
		if(mainView != null){
			mainView.failLogUpdated();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#rulesUpdated()
	 */
	public void rulesUpdated() {
		TraceAnalyserRule[] rules = ruleManager.getActivatedRules();
		traceListener.setRules(rules);
		if(mainView != null){
			mainView.rulesUpdated();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver#ruleUpdated(java.lang.String)
	 */
	public void ruleUpdated(String ruleName) {
		if(mainView != null){
			mainView.ruleUpdated(ruleName);
		}
	}
	
	/**
	 * refreshRuleList.
	 * Refreshes rule list. 
	 */
	public void refreshRuleList(){
		ruleManager.refresh();
	}

	/**
	 * getRuleInformation.
	 * @return Trace analyser rules.
	 */
	public RuleInformation[] getRuleInformation(){
		return ruleManager.getRuleInformation();
	}

	/**
	 * getRule.
	 * returns one rule.
	 * @param ruleName name of the rule that is requested.
	 * @return rule
	 */
	public TraceAnalyserRule getRule(String ruleName){
		return ruleManager.getRule(ruleName);
	}
	
	/**
	 * getRuleTypes.
	 * @return Trace Analyser Rule Types
	 */
	public ArrayList<ITraceAnalyserRuleType> getRuleTypes() {
		return ruleTypes;
	}
	
	/**
	 * addRule.
	 * @param newRule new Trace Analyser rule
	 * @return true if rule was added succesfully.
	 */
	public boolean addRule(TraceAnalyserRule newRule){
		boolean retval = ruleManager.addRule(newRule);
		traceListener.setRules(ruleManager.getActivatedRules());
		return retval;
	}
	
	
	/**
	 * getTraceListener.
	 * @return trace listener
	 */
	public TraceListener getTraceListener() {
		return traceListener;
	}

	/**
	 * changeRuleaActivation.
	 * @param ruleName name of the rule
	 */
	public void changeRuleaActivation(String ruleName, boolean value){
		ruleManager.changeRuleActivation(ruleName, value);
		TraceAnalyserRule[] rules = ruleManager.getActivatedRules();
		traceListener.setRules(rules);
	}
	/**
	 * removeRule
	 * @param ruleName rule that should be removed.
	 * @return true if rule was removed successfully.
	 */
	public boolean removeRule(String ruleName){
		return ruleManager.removeRule(ruleName);
	}
	
	/**
	 * addRuleEvent.
	 * @param ruleName name of rule where event is added.
	 * @param event rule that event is added into.
	 */
	public void addRuleEvent(String ruleName, RuleEvent event){
		ruleManager.addHistoryEvent(ruleName, event);
	}

	/**
	 * resets all rules history data.
	 */
	public void resetHistory(){
		ruleManager.clearHistory();
	}
	
	/**
	 * resets one rules history.
	 * @param ruleName
	 */
	public void resetOneRulesHistory(String ruleName){
		ruleManager.clearOneRulesHistory(ruleName);
	}
	
	/**
	 * ruleExists.
	 * @param ruleName name of the rule
	 * @return true if rule with that name already exists
	 */

	public boolean ruleExists(String ruleName){
		return ruleManager.ruleExists(ruleName);
	}
	
	
}
