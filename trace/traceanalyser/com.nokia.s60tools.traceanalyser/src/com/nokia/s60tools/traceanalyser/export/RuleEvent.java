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


package com.nokia.s60tools.traceanalyser.export;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class RuleEvent.
 * Class that represents one rule event, either passed or failed. Rule event also contains 
 * trace value that was measured and limit that was violated. 
 */
public class RuleEvent implements Serializable {
	/**
	 * serialization UID number
	 */
	private static final long serialVersionUID = 1L;
	
	
	/* indexes for fail log tableviewer */
	public static final int INDEX_FAIL_LOG_TIME = 0;
	public static final int INDEX_FAIL_LOG_RULE_NAME = 1;
	public static final int INDEX_FAIL_LOG_VIOLATION = 2;
	public static final int INDEX_FAIL_LOG_LIMIT = 3;
	
	/* indexes for History views tableviewer */
	public static final int INDEX_HISTORY_STATUS = 0;
	public static final int INDEX_HISTORY_TIME = 1;
	public static final int INDEX_HISTORY_VALUE = 2;
	public static final int INDEX_HISTORY_VIOLATION = 3;
	
	/* Status stating if rule was failed or passed. */
	public enum RuleStatus{ PASS, NONE, FAIL};	
	private RuleStatus status;

	/* value that was measured */ 
	private int value;

	/* measured violation */
	private int violation;
	
	/* limit that was broken */
	private int limit;
	
	/* time when violation occured */
	private Date time;
	
	/* Numbers of traces */
	private int[] traceNumbers;
	
	/* unit of value */
	private String unit = "";
	
	/* Name of the rule */
	private String ruleName;
	
	/**
	 * RuleEvent.
	 * constructor
	 * @param status Status stating if rule was failed or passed.
	 * @param value value that was measured 
	 * @param limit limit that was broken
	 * @param ruleName Name of the rule
	 * @param unit unit of value
	 * @param time time when violation occured
	 * @param traceNumbers Numbers of traces
	 */
	public RuleEvent(RuleStatus status, int value, int limit, int violation, String ruleName, 
					 String unit, Date time, int[] traceNumbers){
		this.status = status;
		this.value = value;
		this.limit = limit;
		this.ruleName = ruleName;
		this.unit = unit;
		this.time = time;
		this.traceNumbers = traceNumbers;
		this.violation = violation;
	}
	
	/**
	 * RuleEvent.
	 * constructor
	 * @param status Status stating if rule was failed or passed.
	 */
	public RuleEvent(RuleStatus status){
		this.status = status;
		this.value = 0;
		this.limit = 0;
		this.ruleName = "";
		this.unit = "";
		this.time = null;
		this.traceNumbers = null;
	}
	
	/**
	 * RuleEvent.
	 * constructor
	 * @param status Status stating if rule was failed or passed.
	 * @param value value that was measured 
	 * @param limit limit that was broken
	 * @param ruleName Name of the rule
	 * @param unit unit of value
	 */ 
	public RuleEvent(RuleStatus status, int value, Date time, String unit) {
		this.status = status;
		this.value = value;
		this.time = time;
		this.unit = unit;
	}
	
	/**
	 * RuleEvent.
	 * constructor
	 */
	public RuleEvent(){
		this.status = RuleStatus.NONE;
		this.value = 0;
		this.limit = 0;
		this.ruleName = "";
		this.unit = "";
		this.time = null;
		this.traceNumbers = null;
		
	}
	
	/**
	 * equals
	 * Comparator.
	 * @param event event that is compared to this object.
	 * @return true if events are equal.
	 */
	public boolean equals(RuleEvent event){
		if( limit == event.getLimit() &&
			status == event.getStatus() &&
			value == event.getValue() ){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * getFailLogText.
	 * Fail log can use this to get description for each column in the grid
	 * @param index index of the column
	 * @return value for asked column
	 */
	
	public String getFailLogText(int index) {
		String retval = "";
		switch (index) {
			case INDEX_FAIL_LOG_TIME:
				if(time != null){
					SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
					retval = formatter.format(time);
				}
				else{
					return "";
				}
				break;
			case INDEX_FAIL_LOG_RULE_NAME:
				retval = ruleName ;
				break;
			case INDEX_FAIL_LOG_VIOLATION:
				retval = Integer.toString(violation) + unit;
				break;
			case INDEX_FAIL_LOG_LIMIT:
				retval = Integer.toString(limit) + unit;
				break;
			default:
				break;
		}
		return retval;
	}

	/**
	 * getHistoryText.
	 * History View can use this to get description for each column in the grid
	 * @param index index of the column
	 * @return value for asked column
	 */
	public String getHistoryText(int index) {
		String retval = "";
		switch (index) {
			case INDEX_HISTORY_STATUS:
				if(status == RuleStatus.FAIL){
					retval = "Fail";
				}
				else{
					retval = "Pass";
				}
			break;
			case INDEX_HISTORY_TIME:
				if(time != null){
					SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
					retval = formatter.format(time);
				}
				else{
					return "";
				}
				break;
			case INDEX_HISTORY_VALUE:
				retval = Integer.toString(value) + unit;
				break;
			case INDEX_HISTORY_VIOLATION:
				if(status == RuleStatus.FAIL){
					retval = Integer.toString(value - limit) + unit;
				}
				else{
					retval = "";
				}
				break;
			default:
				break;
		}
		return retval;
	}
	
	/* getters and setters for member variables */
	
	public RuleStatus getStatus() {
		return status;
	}
	public void setStatus(RuleStatus status) {
		this.status = status;
	}

	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int[] getTraceNumbers() {
		return traceNumbers;
	}

	public void setTraceNumbers(int[] traceNumbers) {
		this.traceNumbers = traceNumbers;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public int getViolation() {
		return violation;
	}

	public void setViolation(int violation) {
		this.violation = violation;
	}
	
	
		
}
