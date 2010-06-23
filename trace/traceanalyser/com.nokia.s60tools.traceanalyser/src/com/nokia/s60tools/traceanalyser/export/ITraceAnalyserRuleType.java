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
import java.util.ArrayList;
import org.eclipse.swt.widgets.Composite;

/**
 * interface ITraceAnalyserRuleType.
 * Interface that all Trace Analyser rule type Plug
 * -Ins must implement
 *
 */
public interface ITraceAnalyserRuleType {
	
	/**
	 * createUIComponents.
	 * Method that places all UI components related 
	 * to rule into give composite-parameter.
	 * @param composite composite where all UI 
	 * components all placed
	 * @param listener listener for UI-changes.
	 */
	public void createUIComponents(
			Composite composite, 
			ITraceAnalyserRuleTypeListener listener);

	/**
	 * formatRuleDefinitions.
	 * Inserts settings from given rule to UI 
	 * components if rule's type is same.
	 * @param rule rule which values are formatted 
	 * to UI.
	 * @return true if parameter rule was from 
	 * this type.
	 */
	public boolean formatRuleDefinitions(
			TraceAnalyserRule rule);
	
	/**
	 * getRuleName.
	 * @return Name of the rule
	 */
	public String getRuleType();
	
	/**
	 * setVisible.
	 * Excludes & hides rule's ui components.
	 * @param value true if rule needs to be hidden.
	 */
	public void setVisible(boolean value);
	
	/**
	 * getRule.
	 * Gets rule from values that are inserted into 
	 * user interface.
	 * @param name rule name.
	 * @param description rule description.
	 * @return Trace Analyser Rule
	 */
	public TraceAnalyserRule getRule(String name, 
			String description);
	
	/**
	 * getRules.
	 * Loads list of rules from file system.
	 * @return Rule list.
	 */
	public ArrayList<TraceAnalyserRule> getRules();
	
	/**
	 * canFinish.
	 * @return implementation of this rule type method 
	 * returns null if this page is complete. 
	 * If page is not complete, error message is returned. 
	 */
	public String canFinish();
	
}
