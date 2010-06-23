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

import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ILock;

import com.nokia.s60tools.traceanalyser.containers.DummyRule;
import com.nokia.s60tools.traceanalyser.containers.RuleInformation;
import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.export.RuleEvent.RuleStatus;
import com.nokia.s60tools.traceanalyser.interfaces.ITraceAnalyserFileObserver;


/**
 * This class is responsible for providing Trace Analyser rules and history data to MainView's content provider.
 * Class calls every rule types getRules-method to get every Trace Analyser Rule rule.
 * After that class calls each rules getHistoryData method to get history data. 
 */
public class RuleManager extends Job {
	
	/* file observer */
	ITraceAnalyserFileObserver engine = null;
	
	/* accesslock */
	ILock accessLock = null;
	
	/* boolean value that is true when job is on-going */
	boolean jobRunning = false;
	
	/* List of rule types */
	ArrayList<ITraceAnalyserRuleType> ruleTypes = null;

	/* HashMap containing each rule and its history data. */
	HashMap<String, RuleInformation> history = null;
	
	/**
	 * TraceAnalyserFileManager.
	 * Constructor.
	 * @param engine, engine which is notified when reading is finished.
	 * @param ruleTypes, ArrayList, which contains all rule types.
	 */
	public RuleManager( ITraceAnalyserFileObserver engine, ArrayList<ITraceAnalyserRuleType> ruleTypes ) {
		super("Trace Analyser - Reading Rules");
		this.engine = engine;
		accessLock = Job.getJobManager().newLock();
		jobRunning = false;
		this.ruleTypes = ruleTypes;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {

		accessLock.acquire();
		
		// if history has never been read, read it now.
		if(history == null){
			
			history = new HashMap<String,RuleInformation>();
			ArrayList<TraceAnalyserRule> ruleArray = new ArrayList<TraceAnalyserRule>();
			
			// read rules
			for(ITraceAnalyserRuleType item : ruleTypes){
				ruleArray.addAll(item.getRules());
			}
			
			// Read History.
			for(TraceAnalyserRule item : ruleArray){
				
				// Get history data from rule
				RuleInformation information = new RuleInformation(item);
				information.setEvents(item.readHistory());
				
				// count history data(amount off passes, fails....)
				countHistory(information);
				history.put(item.getName(), information);
				}		
		}
		engine.rulesUpdated();
		accessLock.release();
		jobRunning = false;
		return Status.OK_STATUS;
	}


	
	/**
	 * getRuleInformation.
	 * Method that returns reads rules and history . If rules are not read method starts reading them.
	 * @return Rules and History data, or dummy object if job is on-going.
	 */
	public RuleInformation[] getRuleInformation() {
		// files have not yet been read, start reading process
		if (history == null) {
			
			if( jobRunning == false ){
				jobRunning = true;
				setPriority(Job.LONG);
				setUser(false);
				schedule(100);
			}
			RuleInformation[] cFiles = new RuleInformation[1];
			DummyRule dummyRule = new DummyRule("Loading rules and history...");
			cFiles[0] = new RuleInformation(dummyRule);
			return cFiles;
		}
		else{
			return history.values().toArray(new RuleInformation[history.size()]);
		}
		
	}
	
	
	
	
	/**
	 * refresh.
	 * Resfresh rule list.
	 */
	public void refresh() {
		
		accessLock.acquire();
		try {
			if (!jobRunning) {
				jobRunning = true;
				setPriority(Job.LONG);
				setUser(false);
				schedule(100);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			accessLock.release();
		}
	}

	/**
	 * addRule.
	 * Adds new rule.
	 * @param newRule rule that should be added.
	 * @return true if rule was added(file operations successful)
	 */
	public boolean addRule(TraceAnalyserRule newRule){
		accessLock.acquire();
		if(history.containsKey(newRule.getName())){
			accessLock.release();
			return false;
		}
		else{
			history.put(newRule.getName(),new RuleInformation(newRule));
			accessLock.release();
			return true;
		}
	}
	
	/**
	 * changeRuleActivation
	 * @param ruleName name of the rule
 
	 */
	public void changeRuleActivation(String ruleName, boolean value){
		RuleInformation information = history.get(ruleName);
		if(information != null){
			information.getRule().setActivated(value);
			
		}
	}
	
	
	/**
	 * removeRule.
	 * Removes rule
	 * @param ruleName rule that should be removed.
	 * @return true if rule was removed(file operations successful)
	 */
	public boolean removeRule(String ruleName){
		accessLock.acquire();
		
		RuleInformation item = history.get(ruleName);
		boolean deleteComplete = item.delete();
		if( deleteComplete ){
			history.remove(ruleName);
		}
		accessLock.release();
		return deleteComplete;
		

	}
	
	
	/**
	 * saveHistory.
	 * Saves history data into each rule.
	 */
	@SuppressWarnings("unchecked")
	public void saveData(){
		Set set = history.entrySet();
		Iterator i = set.iterator();
		int index = 0;
		
		
		
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			RuleInformation information = (RuleInformation) me.getValue();
			if(!information.getRule().writeXML()){
				System.out.println("Unable to save rule definitions"
						+ information.getRule().getName());
			}
			if (!information.getRule().saveHistory(information.getEvents())) {
				System.out.println("Unable to save history data on rule"
						+ information.getRule().getName());
			}
		
			index++;
		}
	}

	/**
	 * getActivatedRules.
	 * @return array of all activated rules.
	 */
	@SuppressWarnings("unchecked")
	public TraceAnalyserRule[] getActivatedRules() {
		accessLock.acquire();
		TraceAnalyserRule[] ruleArray = null;
		ArrayList<TraceAnalyserRule> rules = new ArrayList<TraceAnalyserRule>();
		if(history != null){
			//ruleArray = new TraceAnalyserRule[history.size()];
			Set set = history.entrySet();
		    Iterator i = set.iterator();
		    int index = 0;
		    while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				TraceAnalyserRule rule = ((RuleInformation) me.getValue()).getRule();
				if(rule.isActivated()){
					rules.add(rule);
				}
				//ruleArray[index] = ((RuleInformation) me.getValue()).getRule().isActivated();
				index++;
			}
			ruleArray = rules.toArray(new TraceAnalyserRule[rules.size()]);

		}
		else{
			ruleArray = new TraceAnalyserRule[0];	
		}
		accessLock.release();
		return ruleArray;
	}

	/**
	 * addHistoryEvent.
	 * Adds new history event.
	 * @param ruleName name of the rule where event is added.
	 * @param event history event.
	 */
	public void addHistoryEvent(String ruleName, RuleEvent event){
		accessLock.acquire();
		RuleInformation item = history.get(ruleName);
		item.getEvents().add(event);
		countHistory(item);
		engine.ruleUpdated(ruleName);
		accessLock.release();

	}
	
	
	/**
	 * countHistory.
	 * Counts history values.
	 * @param information rule which history values are counted.
	 */
	private void countHistory(RuleInformation information){
		ArrayList<RuleEvent> events = information.getEvents();
		
		int pass = 0;
		int fail = 0;
		int min = 0;
		int max = 0;
		int sum = 0;
		
		int[] values = new int[events.size()];
		
		for(int i = 0; i < events.size(); i++){
			
			
			RuleEvent event = events.get(i);
			
			// if first event set as max and min value.
			if(i == 0){
				min = event.getValue();
				max = event.getValue();
			}
			else{
				// if event value is smaller than min value set event value as min.
				if(event.getValue() < min){
					min = event.getValue();
				}
				// if event value is greater than max value set event value as max.
				else if(event.getValue() > max){
					max = event.getValue();
				}
			}
			
			// count passes and fails.
			if(event.getStatus() == RuleStatus.PASS){
				pass++;
			}
			else{
				fail++;
			}
			
			// count sum and add value to value-array.
			sum += event.getValue();
			values[i] = event.getValue();
		}
		information.setPass(pass);
		information.setFail(fail);
		information.setMin(min);
		information.setMax(max);
		
		// count average based on sum of values.
		if(events.size() > 0){
			information.setAvg(sum/events.size());
		}
		
		// count median based on value-array.
		if(values.length > 0){
			Arrays.sort(values);
			information.setMed(values[values.length/2]);
		}
		
	}
	
	
	/**
	 * clearHistory.
	 * Clears history data about each rule.
	 */
	@SuppressWarnings("unchecked")
	public void clearHistory(){
		Set set = history.entrySet();
	    Iterator i = set.iterator();
	    
	    int index = 0;
	    
	    while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			((RuleInformation) me.getValue()).setEvents(new ArrayList<RuleEvent>());
			countHistory((RuleInformation) me.getValue());
			index++;
		}
		engine.rulesUpdated();
	}
	
	/**
	 * clearOneRulesHistory.
	 * Clears history data about one rule.
	 * @param ruleName rule that's history is cleared.
	 */
	public void clearOneRulesHistory(String ruleName){
		RuleInformation information = history.get(ruleName);
		information.setEvents(new ArrayList<RuleEvent>());
		countHistory(information);
	    engine.ruleUpdated(ruleName);

	}
	
	/**
	 * ruleExists.
	 * @param ruleName name of the rule
	 * @return true if rule with that name already exists
	 */
	public boolean ruleExists(String ruleName){
		if(history.get(ruleName) != null){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/**
	 * getRule.
	 * returns one rule.
	 * @param ruleName name of the rule that is requested.
	 * @return rule
	 */
	public TraceAnalyserRule getRule(String ruleName){
		RuleInformation information = history.get(ruleName);
		if(information != null){
			return information.getRule();
		}
		else{
			return null;
		}
	}

}
