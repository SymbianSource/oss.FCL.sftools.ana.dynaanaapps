/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Filter Rule Set
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Filter Rule Set
 */
public class FilterRuleSet implements FilterRuleObject {

	/**
	 * Filter rule objects contained in this rule set
	 */
	private final List<FilterRuleObject> filterRuleObjects;

	/**
	 * Indicates if this rule set is inside logical NOT
	 */
	private boolean notRuleSet;

	/**
	 * Logical operation for this rule set
	 */
	private LogicalOperator operator;

	/**
	 * Logical operations enumeration
	 */
	public enum LogicalOperator {

		/**
		 * Logical operator OR
		 */
		OR,

		/**
		 * Logical operator AND
		 */
		AND;
	}

	/**
	 * Constructor
	 */
	public FilterRuleSet() {
		filterRuleObjects = new ArrayList<FilterRuleObject>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject#isLogicalNotRule
	 * ()
	 */
	public boolean isLogicalNotRule() {
		return notRuleSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject#setLogicalNotRule
	 * (boolean)
	 */
	public void setLogicalNotRule(boolean notRule) {
		// Change the operator when NOT status changes
		if (this.notRuleSet != notRule) {
			if (operator == LogicalOperator.OR) {
				operator = LogicalOperator.AND;
			} else {
				operator = LogicalOperator.OR;
			}
		}
		this.notRuleSet = notRule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.dataprocessor.FilterRuleObject
	 * #processRule(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public boolean processRule(TraceProperties properties) {
		int len = getFilterRules().size();
		boolean filterHits = true;
		// Loop through rules
		for (int i = 0; filterHits && i < len; i++) {
			// Get the rule
			FilterRuleObject rule = getFilterRules().get(i);

			// Process filter rule
			if (rule != null) {
				filterHits = rule.processRule(properties);
			}

			// Logical operator OR in use
			if (getOperator() == LogicalOperator.OR) {
				// One rule has hit, ready and break
				if (filterHits) {
					break;
					// Not in end, continue
				} else if (i < len - 1) {
					filterHits = true;
					// End, rule was not found
				} else if (i == len - 1) {
					filterHits = false;
				}

				// Logical operator AND in use
			} else if (getOperator() == LogicalOperator.AND) {
				// Some rule didn't hit, break
				if (!filterHits) {
					break;
				}
			}
		}
		return filterHits;
	}

	/**
	 * Gets the logical operator
	 * 
	 * @return the operator
	 */
	public LogicalOperator getOperator() {
		return operator;
	}

	/**
	 * Sets the logical operator
	 * 
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	/**
	 * Adds new Filter rule object to the list
	 * 
	 * @param object
	 *            object to be added
	 */
	public void addObject(FilterRuleObject object) {
		filterRuleObjects.add(object);
	}

	/**
	 * Adds new Filter rule object to the list
	 * 
	 * @param position
	 *            position to add the object
	 * @param object
	 *            object to be added
	 */
	public void addObject(int position, FilterRuleObject object) {
		filterRuleObjects.add(position, object);
	}

	/**
	 * Gets filter rule objects from this filter rule set
	 * 
	 * @return the filterRuleObjects
	 */
	public List<FilterRuleObject> getFilterRules() {
		return filterRuleObjects;
	}
}
