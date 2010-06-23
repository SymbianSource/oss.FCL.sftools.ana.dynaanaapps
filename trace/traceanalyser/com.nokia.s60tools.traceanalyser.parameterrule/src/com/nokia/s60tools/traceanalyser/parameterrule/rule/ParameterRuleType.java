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


package com.nokia.s60tools.traceanalyser.parameterrule.rule;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
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
import com.nokia.s60tools.traceanalyser.parameterrule.plugin.TraceAnalyserParameterRulePlugin;
import com.nokia.traceviewer.engine.TraceInformation;

/**
 * class Parameter Rule.
 * "Parameter Rule"-rule type for Trace Analyser
 */

public class ParameterRuleType implements ITraceAnalyserRuleType, ITraceSelectionCompositeListener, SelectionListener, ModifyListener{

	public static enum ParameterType{GREATER, LESS, BETWEEN, EQUAL};
	
	public static final String TYPE_COMBO_TEXTS_GREATER_THAN = "Greater than/Equal to";
	public static final String TYPE_COMBO_TEXTS_LESS_THAN = "Less than/Equal to";
	public static final String TYPE_COMBO_TEXTS_BETWEEN = "Between";
	public static final String TYPE_COMBO_TEXTS_EQUAL_TO = "Equal to";
		
	/* UI components */
	private GridData gridDataComposite;
	private Composite compositeParameterRule;
	private Combo comboParameterType;
	private Text textParameterValue1;
	private Text textParameterValue2;
	private GridData gridDataTextRuleName2;
	private GridData gridDataLabelAndText;
	private Label labelAndText;
		
	/* Trace Selection composites */
	TraceSelectionComposite traceSelectionComposite;
	
	/* Edit rule-window */
	ITraceAnalyserRuleTypeListener listener;

	
	/**
	 * TimingRule.
	 * constructor
	 */
	public ParameterRuleType(){
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRule#createUIComponents(org.eclipse.swt.widgets.Composite)
	 */
	public void createUIComponents(Composite composite, ITraceAnalyserRuleTypeListener listener) {
	
		this.listener = listener;
		
		// Create composite for rule
		compositeParameterRule = new Composite(composite, SWT.NONE);
 		GridLayout layoutcompositeParameterRule = new GridLayout();
 		layoutcompositeParameterRule.numColumns = 1;
 		compositeParameterRule.setLayout(layoutcompositeParameterRule);
 		gridDataComposite = new GridData(GridData.FILL_HORIZONTAL);
 		compositeParameterRule.setLayoutData(gridDataComposite);
 		
 		// create trace selection components.
		traceSelectionComposite = new TraceSelectionComposite(compositeParameterRule, "Trace:", this);

		// create composite for parameters
		Composite compositeParameters = new Composite(compositeParameterRule, SWT.NONE);
 		GridLayout layoutcompositeParameters = new GridLayout();
 		layoutcompositeParameters.numColumns = 4;
 		compositeParameters.setLayout(layoutcompositeParameters);
 		GridData gridDataCompositeParameters = new GridData(GridData.FILL_HORIZONTAL);
 		compositeParameters.setLayoutData(gridDataCompositeParameters);
 		
 		createParameterComponents(compositeParameters);
 		

	}
	
	private void createParameterComponents(Composite composite){
		
		// create label
		Label labelParameterText = new Label(composite, SWT.NONE);
		labelParameterText.setText("Trace parameter(integer) should be:");
		GridData gridDataLabelParameterText = new GridData(GridData.BEGINNING);
		gridDataLabelParameterText.horizontalSpan = 4;
		labelParameterText.setLayoutData(gridDataLabelParameterText);

		
		// Parameter type
		comboParameterType = new Combo(composite, SWT.READ_ONLY);
		GridData dataGridComboparameterTypeCombo = new GridData(GridData.BEGINNING);
		comboParameterType.setLayoutData(dataGridComboparameterTypeCombo);

		String[] parameterTypes = new String[]{ TYPE_COMBO_TEXTS_GREATER_THAN, TYPE_COMBO_TEXTS_LESS_THAN,	
												TYPE_COMBO_TEXTS_BETWEEN, TYPE_COMBO_TEXTS_EQUAL_TO };
		comboParameterType.setItems(parameterTypes);
		comboParameterType.select(0);
		
		// Create rule name text field
		textParameterValue1 = new Text(composite, SWT.BORDER);
		GridData gridDataTextRuleName = new GridData(GridData.BEGINNING);
		gridDataTextRuleName.widthHint = 30;

		textParameterValue1.setToolTipText("Define Parameter");
		textParameterValue1.setLayoutData(gridDataTextRuleName);
		
		// create label
		labelAndText = new Label(composite, SWT.NONE);
		labelAndText.setText(" and ");
		gridDataLabelAndText = new GridData(GridData.BEGINNING);
		labelAndText.setLayoutData(gridDataLabelAndText);

		textParameterValue2 = new Text(composite, SWT.BORDER);
		gridDataTextRuleName2 = new GridData(GridData.BEGINNING);
		gridDataTextRuleName2.widthHint = 30;
		textParameterValue2.setToolTipText("Define Parameter");
		textParameterValue2.setLayoutData(gridDataTextRuleName2);
		
		
		Listener inputListener = new Listener() {
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

		};
		// Set text box to accept only numbers.
		textParameterValue1.addListener(SWT.Verify, inputListener);
		textParameterValue2.addListener(SWT.Verify, inputListener);

		comboParameterType.addSelectionListener(this);
		textParameterValue1.addModifyListener(this);
		textParameterValue2.addModifyListener(this);

/*		comboParameterType.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent event) {
				// Nothing to be done
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				
				
				hideAndRevealParameter2();
				listener.canFinish();
			}
			
		});
	*/	
	}
	
	private void hideAndRevealParameter2(){
		boolean value = false;
		if(comboParameterType.getText().equals("Between")){
			value = true;
		}
		textParameterValue2.setVisible(value);
		labelAndText.setVisible(value);
		
		gridDataLabelAndText.exclude = !value;
		gridDataTextRuleName2.exclude = !value;
		
		textParameterValue2.getParent().layout();
		textParameterValue2.getParent().redraw();
	}
		
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRule#setVisible(boolean)
	 */
	public void setVisible(boolean value){
		compositeParameterRule.setVisible(value);
		gridDataComposite.exclude = !value;
		hideAndRevealParameter2();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#saveRule(java.lang.String, java.lang.String)
	 */
	public TraceAnalyserRule getRule(String name, String description){
		
		// Create new parameter rule.
		ParameterRule newRule = new ParameterRule();
		
		// set basic info.
		newRule.setName(name);
		newRule.setDescription(description);
		
		
		// set directory for rule.
		newRule.setWorkingDirectory(getPluginWorkingLocation());
		
		// set trace item for rule.
		newRule.setTraceItem(traceSelectionComposite.getTraceInformation());
		
		
		
		// set parameter type for rule.
		
		if(comboParameterType.getText().equals(TYPE_COMBO_TEXTS_GREATER_THAN)){
			newRule.setParameterType(ParameterType.GREATER);
		}
		else if(comboParameterType.getText().equals(TYPE_COMBO_TEXTS_EQUAL_TO)){
			newRule.setParameterType(ParameterType.EQUAL);

		}
		else if(comboParameterType.getText().equals(TYPE_COMBO_TEXTS_BETWEEN)){
			newRule.setParameterType(ParameterType.BETWEEN);

		}
		else if(comboParameterType.getText().equals(TYPE_COMBO_TEXTS_LESS_THAN)){
			newRule.setParameterType(ParameterType.LESS);

		}
		
		// if empty, set to zero.
		if(textParameterValue1.getText() == ""){
			textParameterValue1.setText("0");
		}
		
		if(newRule.getParameterType() != ParameterType.BETWEEN || textParameterValue2.getText() == ""){
			textParameterValue2.setText("0");
			
		}
		int limitA = 0;
		int limitB = 0;
		try{
			limitA = Integer.parseInt(textParameterValue1.getText());
			limitB = Integer.parseInt(textParameterValue2.getText());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		newRule.setLimitA(limitA);
		newRule.setLimitB(limitB);

		// write rule into xml-file.
	//	if(newRule.writeXML()){
			return newRule;
		//}
		//else{
			//return null;
	//	}
		//return null;
	}
	
		
	/**
	 * getPluginWorkingLocation.
	 * Returns a path where Rule plugin can do various tasks (located under workspace).
	 */	
	public static String getPluginWorkingLocation() {
		IPath location = Platform.getStateLocation( TraceAnalyserParameterRulePlugin.getDefault().getBundle());
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
				ParameterRule rule = new ParameterRule();
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
		return "Parameter Rule";
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#formatRuleDefinitions(com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule)
	 */
	public boolean formatRuleDefinitions(TraceAnalyserRule rule) {
		
		if(ParameterRule.class == rule.getClass()){
			ParameterRule parameterRule = (ParameterRule) rule;
			
			traceSelectionComposite.setTraceItem(parameterRule.getTraceItem());
			
			comboParameterType.select(parameterRule.getParameterType().ordinal());
			
			textParameterValue1.setText(Integer.toString(parameterRule.getLimitA()));
			textParameterValue2.setText(Integer.toString(parameterRule.getLimitB()));
			
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType#canFinish()
	 */
	public String canFinish() {
		if(traceSelectionComposite.getTraceInformation() == null ){
			return "Trace must be defined.";
		}
		if(textParameterValue1.getText().equals("")){
			return "Limit must be defined.";
		}
		
		if(textParameterValue2.isVisible() && textParameterValue2.getText().equals("")){
			return "Both Limits must be defined";
		}
		
		if(comboParameterType.getText().equals(TYPE_COMBO_TEXTS_BETWEEN)){
			int limit1 = Integer.parseInt(textParameterValue1.getText());
			int limit2 = Integer.parseInt(textParameterValue2.getText());
			if(limit1 >= limit2 ){
				return "Second limitation parameter must be greater that first.";
			}
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

	public void widgetDefaultSelected(SelectionEvent arg0) {
		// Nothing to be done		
		
	}

	public void widgetSelected(SelectionEvent event) {
		
		if(event.widget == comboParameterType){
			hideAndRevealParameter2();
			listener.canFinish();
		}
	}

	public void modifyText(ModifyEvent arg0) {
		listener.canFinish();
		
	}
	
	static public ParameterRule createDummyRule(String ruleName, ParameterType type){
		// Create new timing rule.

		ParameterRule newRule = new ParameterRule();
		
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
				
		newRule.setTraceItem(info1);
		
		newRule.setParameterType(type);
		
		
		int limitA = 0;
		int limitB = 0;
		try{
			limitA = Integer.parseInt("10");
			limitB = Integer.parseInt("100");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		newRule.setLimitA(limitA);
		if(type == ParameterType.BETWEEN){
			newRule.setLimitB(limitB);
		}
		// write rule into xml-file.
		return newRule;
		
	}



}
