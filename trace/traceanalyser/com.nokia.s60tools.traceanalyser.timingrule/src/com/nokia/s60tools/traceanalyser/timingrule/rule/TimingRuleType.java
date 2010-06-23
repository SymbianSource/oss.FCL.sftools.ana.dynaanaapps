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


package com.nokia.s60tools.traceanalyser.timingrule.rule;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType;
import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleTypeListener;
import com.nokia.s60tools.traceanalyser.export.ITraceSelectionCompositeListener;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.export.TraceInfo;
import com.nokia.s60tools.traceanalyser.export.TraceSelectionComposite;
import com.nokia.s60tools.traceanalyser.timingrule.plugin.TraceAnalyserTimingRulePlugin;
import com.nokia.traceviewer.engine.TraceInformation;

/**
 * class Timing Rule.
 * "Parameter Rule"-rule type for Trace Analyser
 */

public class TimingRuleType implements ITraceAnalyserRuleType, ITraceSelectionCompositeListener, ModifyListener{

	/* UI components */
	private GridData gridDataComposite;
	private Composite compositeTimingRule;
	private Text textLimitTraceA;
	private Text textLimitTraceB;
		
	/* Trace Selection composites */
	TraceSelectionComposite traceSelectionCompositeA;
	TraceSelectionComposite traceSelectionCompositeB;
	
	/* Edit rule-window */
	ITraceAnalyserRuleTypeListener listener;
	

	
	/**
	 * TimingRule.
	 * constructor
	 */
	public TimingRuleType(){
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRule#createUIComponents(org.eclipse.swt.widgets.Composite)
	 */
	public void createUIComponents(Composite composite, ITraceAnalyserRuleTypeListener listener) {
	
		this.listener = listener;
		
		// Create composite for rule
		compositeTimingRule = new Composite(composite, SWT.NONE);
 		GridLayout layoutTraceA = new GridLayout();
 		layoutTraceA.numColumns = 1;
 		compositeTimingRule.setLayout(layoutTraceA);
 		gridDataComposite = new GridData(GridData.FILL_HORIZONTAL);
 		compositeTimingRule.setLayoutData(gridDataComposite);
 		
 		// create trace selection components.
		traceSelectionCompositeA = new TraceSelectionComposite(compositeTimingRule, "Trace A:", this);
		traceSelectionCompositeB = new TraceSelectionComposite(compositeTimingRule, "Trace B:", this);

		// create composite for time limits
		Composite compositeTimeLimits = new Composite(compositeTimingRule, SWT.NONE);
 		GridLayout layoutTimeLimitA = new GridLayout();
 		layoutTimeLimitA.numColumns = 2;
 		compositeTimeLimits.setLayout(layoutTimeLimitA);
 		GridData gridDataTimeLimits = new GridData(GridData.FILL_HORIZONTAL);
 		compositeTimeLimits.setLayoutData(gridDataTimeLimits);
 		
 		// create time limit components.
 		textLimitTraceA = this.createTimeLimitControls(compositeTimeLimits, "Time Limit A(Trace B must arrive after this):");
 		textLimitTraceB = this.createTimeLimitControls(compositeTimeLimits, "Time Limit B(Trace B must arrive before this):");

		
	}
	
	/**
	 * createTimeLimitControls.
	 * Method that creates time limit related ui-components.
	 * @param composite composite where components are placed
	 * @param labelText text of the label
	 * @return textBox where time limit is written.
	 */
	private Text createTimeLimitControls(Composite composite, String labelText){
		
		// create composite for trace limit components
		Composite compositeTimeLimits = new Composite(composite, SWT.NONE);
 		GridLayout layoutTimeLimitA = new GridLayout();
 		layoutTimeLimitA.numColumns = 2;
 		compositeTimeLimits.setLayout(layoutTimeLimitA);
 		GridData gridDataTimeLimits = new GridData(GridData.FILL_HORIZONTAL);
 		compositeTimeLimits.setLayoutData(gridDataTimeLimits);
 		
		// Create time limit label
		Label labelTimeLimit = new Label(compositeTimeLimits, SWT.NONE);
		labelTimeLimit.setText(labelText);
		GridData gridDataLabelTimeLimit = new GridData(GridData.BEGINNING);
		gridDataLabelTimeLimit.horizontalSpan = 2;
		labelTimeLimit.setLayoutData(gridDataLabelTimeLimit);
		
		// Create time limit text box.
		Text textBox = new Text(compositeTimeLimits, SWT.BORDER);
		GridData gridDataTextBox = new GridData(GridData.BEGINNING);
		gridDataTextBox.widthHint = 30;
		textBox.setTextLimit(5);
		textBox.setToolTipText("Define time limit for trace.");
		textBox.setLayoutData(gridDataTextBox);
		textBox.setText("0");
		
		// Create ms-label
		Label labelMS = new Label(compositeTimeLimits, SWT.NONE);
		labelMS.setText("ms");
		
		// Set text box to accept only numbers.
		textBox.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String string = event.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						event.doit = false;
						return;
					}
				}
			}

		});
		
		textBox.addModifyListener(this);
		
		return textBox;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRule#setVisible(boolean)
	 */
	public void setVisible(boolean value){
		compositeTimingRule.setVisible(value);
		gridDataComposite.exclude = !value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#saveRule(java.lang.String, java.lang.String)
	 */
	public TraceAnalyserRule getRule(String name, String description){
		
		// Create new timing rule.
		TimingRule newRule = new TimingRule();
		
		// set basic info.
		newRule.setName(name);
		newRule.setDescription(description);
		
		
		// set directory for rule.
		newRule.setWorkingDirectory(getPluginWorkingLocation());
		
		// set trace items for rule.
		newRule.setTraceItemA(traceSelectionCompositeA.getTraceInformation());
		newRule.setTraceItemB(traceSelectionCompositeB.getTraceInformation());
		
		
		// set time limits for rule.
		
		// if empty, set to zero.
		if(textLimitTraceA.getText() == ""){
			textLimitTraceA.setText("0");
		}
		if(textLimitTraceB.getText() == ""){
			textLimitTraceB.setText("0");
		}
		
		int limitA = 0;
		int limitB = 0;
		try{
			limitA = Integer.parseInt(textLimitTraceA.getText());
			limitB = Integer.parseInt(textLimitTraceB.getText());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		newRule.setTimeLimitA(limitA);
		newRule.setTimeLimitB(limitB);

		// write rule into xml-file.
	//	if(newRule.writeXML()){
			return newRule;
		//}
		//else{
			//return null;
	//	}
	}
	
		
	/**
	 * getPluginWorkingLocation.
	 * Returns a path where Rule plugin can do various tasks (located under workspace).
	 */	
	public static String getPluginWorkingLocation() {
		IPath location = Platform.getStateLocation( TraceAnalyserTimingRulePlugin.getDefault().getBundle());
		return location.toOSString();		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#getRules()
	 */
	public ArrayList<TraceAnalyserRule> getRules(){
		
		ArrayList<TraceAnalyserRule> rules = new ArrayList<TraceAnalyserRule>();
		File rootFolder = new File(getPluginWorkingLocation());
		File[] rootFiles = rootFolder.listFiles();
		int i = 0;
		
		while( i < rootFiles.length ){
			if(rootFiles[i].isDirectory()){
				// Create new rule.
				TimingRule rule = new TimingRule();
				rule.setRulePath(rootFiles[i].toString());
				
				// read XML, if successful add object to arraylist.
				if( rule.readXML() ){
					rules.add(rule);
				}
			}
			i++;
			
		}
		
		return rules;
		
	}

	/**
	 * getRuleType.
	 * @param this rule types name.
	 */
	public String getRuleType() {
		return "Timing Rule";
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#formatRuleDefinitions(com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule)
	 */
	public boolean formatRuleDefinitions(TraceAnalyserRule rule) {
		
		if(TimingRule.class == rule.getClass()){
			TimingRule timingRule = (TimingRule) rule;
			
			traceSelectionCompositeA.setTraceItem(timingRule.getTraceItemA());
			traceSelectionCompositeB.setTraceItem(timingRule.getTraceItemB());
			
			textLimitTraceA.setText(Integer.toString( timingRule.getTimeLimitA()));
			textLimitTraceB.setText(Integer.toString( timingRule.getTimeLimitB()));
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#canFinish()
	 */
	public String canFinish() {
		if(traceSelectionCompositeA.getTraceInformation() == null ){
			return "Trace A must be defined";
		}
		if(traceSelectionCompositeB.getTraceInformation() == null){
			return "Trace B must be defined";
		}
	
		int limit1 = Integer.parseInt(textLimitTraceA.getText());
		int limit2 = Integer.parseInt(textLimitTraceB.getText());

		if(limit1 >= limit2 && !(limit1 == 0 && limit2 == 0) ){
			return "Limit B must be greater that limit A";
		}
		
		return null;

	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceSelectionCompositeListener#traceInfoUpdated()
	 */
	public void traceInfoUpdated() {
		listener.canFinish();
	}
	
	static public TimingRule createDummyRule(String ruleName){
		// Create new timing rule.

		TimingRule newRule = new TimingRule();
		
		// set basic info.
		newRule.setName(ruleName);
		newRule.setDescription("testDescription");
		
		// set directory for rule.
		newRule.setWorkingDirectory(getPluginWorkingLocation());
		
		// set trace items for rule.
		TraceInfo info1 = new TraceInfo();
		TraceInformation information = new TraceInformation();
		information.setTraceId(1);
		info1.setIdNumbers(information);
		info1.setIdNumbers(information);
		

		TraceInfo info2 = new TraceInfo();
		TraceInformation information2 = new TraceInformation();
		information2.setTraceId(2);
		info2.setIdNumbers(information2);
		
		newRule.setTraceItemA(info1);
		newRule.setTraceItemB(info2);
		
		
			
		int limitA = 0;
		int limitB = 0;
		try{
			limitA = Integer.parseInt("10");
			limitB = Integer.parseInt("100");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		newRule.setTimeLimitA(limitA);
		newRule.setTimeLimitB(limitB);

		// write rule into xml-file.
			return newRule;
		
	}

	public void modifyText(ModifyEvent arg0) {
		listener.canFinish();
	}



}
