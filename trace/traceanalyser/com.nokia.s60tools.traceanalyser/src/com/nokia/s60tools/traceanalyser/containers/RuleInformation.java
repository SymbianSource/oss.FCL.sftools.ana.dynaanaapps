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

import java.io.Serializable;
import java.util.ArrayList;

import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;


/**
 * class RuleInformation.
 * Class that contains information about one rule that is shown on main view of Trace Analyser. 
 */
public class RuleInformation implements Serializable {

	
	/**
	 * UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/* indexes for TableViewer */
	public static final int INDEX_CHECK_BOX = 0;
	public static final int INDEX_NAME = 1;
	public static final int INDEX_PASS = 2;
	public static final int INDEX_FAIL = 3;
	public static final int INDEX_PASS_FAIL_RATIO = 4;
	public static final int INDEX_MIN = 5;
	public static final int INDEX_MAX = 6;
	public static final int INDEX_AVG = 7;
	public static final int INDEX_MED = 8;
	
	/* Trace Analyser Rule object */
	private TraceAnalyserRule rule;
	
	/* amount of passes */
	private int pass;
	
	/* amount of fails */
	private int fail;
	
	/* min value */
	private int min;
	
	/* max value */
	private int max;
	
	/* average value */
	private int avg;
	
	/* median value */
	private int med;
	
	/* all history events */
	private ArrayList<RuleEvent> events;
	
	/**
	 * RuleInformation
	 * Constructor
	 * @param rule rule for this information.
	 */
	public RuleInformation(TraceAnalyserRule rule){
		this.rule = rule;
		this.events = new ArrayList<RuleEvent>();
	}
	

	/**
	 * getText.
	 * MainView can use this to get description for each column in the grid
	 * @param index index of the column
	 * @return value for asked column
	 */
	public String getText(int index) {
		String retval = "";
		switch (index) {
			case INDEX_CHECK_BOX:
				retval = "";
				break;
			case INDEX_NAME:
				retval = rule.getName();
				break;
			case INDEX_PASS:
				retval = Integer.toString(pass);
				break;
			case INDEX_FAIL:
				retval = Integer.toString(fail);
				break;
			case INDEX_PASS_FAIL_RATIO:
				
				double value = getPassPercent();
				if(value == -1){
					return "-";
				}
				else{
					retval = Double.toString(value);
				}
				
				break;
			case INDEX_MAX:
				retval = Integer.toString(max) + rule.getUnit();
				break;	
			case INDEX_MIN:
				retval = Integer.toString(min) + rule.getUnit();
				break;
			case INDEX_AVG:
				if(pass > 0 || fail > 0){
					retval = Integer.toString(avg) + rule.getUnit();
				}
				else{
					retval = "-";
				}
				break;	
			case INDEX_MED:
				if(pass > 0 || fail > 0){
					retval = Integer.toString(med) + rule.getUnit();
				}
				else{
					retval = "-";
				}
				break;	
			default:
				break;
		}
		return retval;
	}
	
	/**
	 * delete.
	 * Deletes rule and all information relating to it.
	 * @return true if file operations were successful
	 */
	public boolean delete(){
		return rule.delete();
	}
	
	/* Getters and setters for member variables */
	
	public TraceAnalyserRule getRule() {
		return rule;
	}
	public void setRule(TraceAnalyserRule rule) {
		this.rule = rule;
	}
	public int getPass() {
		return pass;
	}
	public void setPass(int pass) {
		this.pass = pass;
	}
	public int getFail() {
		return fail;
	}
	public void setFail(int fail) {
		this.fail = fail;
	}
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public int getAvg() {
		return avg;
	}
	public void setAvg(int avg) {
		this.avg = avg;
	}
	public int getMed() {
		return med;
	}
	public void setMed(int med) {
		this.med = med;
	}
	
	public ArrayList<RuleEvent> getEvents() {
		return events;
	}
	public void setEvents(ArrayList<RuleEvent> events) {
		this.events = events;
	}

	/**
	 * getPassPercent.
	 * @return returns current pass percent for rule.
	 */
	public double getPassPercent(){
		if(pass == 0 && fail == 0){
			return -1;
		}
		else{
			double passDouble = pass;
			double allEvents = fail + pass;
			double value = passDouble / allEvents; 
			double roundedValue = (int)(value * 1000 + 0.5)/10.0;
			return roundedValue;
		}
	}
	
}
