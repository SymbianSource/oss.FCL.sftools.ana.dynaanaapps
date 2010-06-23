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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.nokia.s60tools.traceanalyser.export.GeneralMethods;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.export.TraceInfo;
import com.nokia.s60tools.traceanalyser.export.RuleEvent.RuleStatus;
import com.nokia.s60tools.traceanalyser.parameterrule.rule.ParameterRuleType.ParameterType;
import com.nokia.traceviewer.engine.TraceProperties;

public class ParameterRule extends TraceAnalyserRule {

	/* Strings that are searched from xml-file */
	
	public static final String XML_TRACE = "ParameterTrace";
	public static final String XML_TYPE = "Type";
	public static final String XML_LIMITA = "LimitA";
	public static final String XML_LIMITB = "LimitB";
	
	private ParameterType parameterType;
	
	private TraceInfo traceItem;

	private int LimitA;
	private int LimitB;
	
	private String workingDirectory;
	
	/**
	 * ParameterRule.
	 * Constructor
	 */
	public ParameterRule() {
		super("Parameter Rule");
		traceItem = new TraceInfo();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#writeXML()
	 */
	public boolean writeXML(){
		
		if(rulePath == null){
			// get directory for rule.
			
			String path = getNextFreeDirectory(workingDirectory);
			if(path != null){
				rulePath = path;
			}
			else{
				return false;
			}
		}	
		rulePath = addSlashToEnd(rulePath);
		if(super.writeBasicInfoIntoFile()){
			String fileName = rulePath + FILENAME_ADDITIONAL_INFO;
			try {
				// Create file
				FileWriter fstream = new FileWriter(fileName);
				BufferedWriter output = new BufferedWriter(fstream);
				output.write(this.getAdditionalInfoXmlString());
				// Close the output stream
				output.close();
				return true;
			} catch (Exception e) {// Catch exception if any
				return false;
			}
		}
		return false;

	}
	
	/**
	 * readXML.
	 * Reads xml-file and formats this rule's definitions. NOTE rulePath needs to be defined before calling this function.
	 */
	public boolean readXML(){
		if(super.readXML()){
			
			if(rulePath != null){
				String additionalInfoPath = TraceAnalyserRule.addSlashToEnd(rulePath) + FILENAME_ADDITIONAL_INFO;
				File additionalInfoFile = new File(additionalInfoPath);
				boolean errorOccured = false;
				if (additionalInfoFile.exists()) {
					try {
						// Create buffered reader.
						BufferedReader input = new BufferedReader(new FileReader(additionalInfoFile));
						try {
							if(!readFile(input)){
								errorOccured = true;
							}
						} 
						catch(Exception e){
							errorOccured = true;
						}
						finally {
							input.close();
						}
					} catch (Exception e) {
						errorOccured = true;
					}
				}
				if(errorOccured){
					return false;
				}
				else{
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * readFile
	 * Reads xml file given as parameter.
	 * @param input bufferedReader opened to inputFile.
	 * @throws IOException if file operations fails.
	 * @return true if file was read successfully
	 */
	private boolean readFile(BufferedReader input) throws IOException{
		String line = null;
		
		// This while loop searches xml tags from file. When tag is found information is saved and 
		// new searchPhrase is updated. Tags are searched in order where they should be.
		
		if(!traceItem.readXMLBuffer(input, XML_TRACE)){
			return false;
		}
		
		
		String searchPhrase = XML_TYPE;
		while ((line = input.readLine()) != null) {
			if (line.contains(searchPhrase)) {
				if(searchPhrase.equals(XML_TYPE)){
					String type = GeneralMethods.getTextBetweenQuotes(line);
					if(!setParameterString(type)){
						return false;
					}
					searchPhrase = XML_LIMITA;

				}

				else if(searchPhrase.equals(XML_LIMITA)){
					LimitA = Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line));
					searchPhrase = XML_LIMITB;

				}
				else if(searchPhrase.equals(XML_LIMITB)){
					LimitB = Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line));
					return true;
				}
				
			}

		}
		return false;
	}

	/**
	 * getAdditionalInfoXmlString.
	 * @return XML string that can be written to file.
	 */
	private String getAdditionalInfoXmlString(){
		String xml = "";	
		xml += "<AdditionalInfo>\n";
		xml += "    <Traces>\n";
		xml += traceItem.getXMLString(XML_TRACE,8);
		xml += "    </Traces>\n";
		xml += "    <Limitations>\n";
		xml += getLimitationInfoXmlString(8);
		xml += "    </Limitations>\n";
		xml += "</AdditionalInfo>\n";
		return xml;
	}
	
	
	/**
	 * getTimingInfoXmlString.
	 * @param indent indent that is used.
	 * @return xml-string that can be written to file.
	 */
	private String getLimitationInfoXmlString(int indent){
		String xml = "";
		String indentString = this.getIndentString(indent);
		
		xml += indentString + "<ParameterType=\"";
		xml += getParameterString();
		xml += "\">\n";
		
		xml += indentString + "<" + XML_LIMITA + "=\"";
		xml += Integer.toString(LimitA);
		xml += "\">\n";
		
		xml += indentString + "<" + XML_LIMITB+ "=\"";
		xml += Integer.toString(LimitB);
		xml += "\">\n";
				
		
		return xml;
	}
	
	
	/**
	 * getParameterString.
	 * Returns parameter String
	 * @return Parameter type.
	 */
	public String getParameterString(){
		if(parameterType == ParameterType.BETWEEN){
			return ParameterRuleType.TYPE_COMBO_TEXTS_BETWEEN;
		}
		else if(parameterType == ParameterType.EQUAL){
			return ParameterRuleType.TYPE_COMBO_TEXTS_EQUAL_TO;
		}
		else if(parameterType == ParameterType.GREATER){
			return ParameterRuleType.TYPE_COMBO_TEXTS_GREATER_THAN;
		}
		else if(parameterType == ParameterType.LESS){
			return ParameterRuleType.TYPE_COMBO_TEXTS_LESS_THAN;
		}
		else{
			return null;
		}
	}
	
	/**
	 * setParameterString.
	 * @param type Sets parameter string
	 * @return true if string was equal to some rule type.
	 */
	public boolean setParameterString(String type){
		if(type.equals(ParameterRuleType.TYPE_COMBO_TEXTS_BETWEEN)){
			this.parameterType = ParameterType.BETWEEN;
			return true;
		}
		else if(type.equals(ParameterRuleType.TYPE_COMBO_TEXTS_EQUAL_TO)){
			this.parameterType = ParameterType.EQUAL;
			return true;
		}
		else if(type.equals(ParameterRuleType.TYPE_COMBO_TEXTS_GREATER_THAN)){
			this.parameterType = ParameterType.GREATER;
			return true;
		}
		else if(type.equals(ParameterRuleType.TYPE_COMBO_TEXTS_LESS_THAN)){
			this.parameterType = ParameterType.LESS;
			return true;
		}
		else{
			return false;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#checkIfRuleFails(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public RuleEvent checkRuleStatus(TraceProperties traceProperties) {

		
		
		//get properties of trace:
		ArrayList<String> properties = traceProperties.parameters;
		if(properties.size() == 0){
			return new RuleEvent(RuleStatus.NONE);
		}
		
		if(TraceInfo.compareTraces(traceProperties.information, traceItem.getIdNumbers())){
			//TODO trycatch
			int parameter = Integer.parseInt(properties.get(0));
			
			int violation = 0;
			RuleStatus status = RuleStatus.PASS;
			int limit = 0;
			
			if(parameterType == ParameterType.LESS){
				if( !(parameter <= LimitA) ){
					status = RuleStatus.FAIL;
					violation = parameter - LimitA; 
					limit = LimitA;
				}
				else{
					status = RuleStatus.PASS;
				}
				
				
			}
			else if(parameterType == ParameterType.EQUAL){
				if(parameter != LimitA ){
					status = RuleStatus.FAIL;
					violation = parameter - LimitA; 
					limit = LimitA;
				}
				else{
					status = RuleStatus.PASS;
				}
			}
			else if(parameterType == ParameterType.GREATER){
				if( !(parameter >= LimitA) ){
					status = RuleStatus.FAIL;
					violation = parameter - LimitA; 
					limit = LimitA;
				}
				else{
					status = RuleStatus.PASS;
				}
				
			}

			else if(parameterType == ParameterType.BETWEEN){
				if(parameter < LimitA){
					status = RuleStatus.FAIL;
					violation = parameter - LimitA; 
					limit = LimitA;
				
				}
				else if(parameter > LimitB){
					status = RuleStatus.FAIL;
					violation = parameter - LimitB; 
					limit = LimitB;
				}
				else{
					status = RuleStatus.PASS;
				
				}

			}
			return new RuleEvent( status, parameter, limit, violation, name, "", 
					  new Date(), new int[]{traceProperties.traceNumber} );


		}
		return new RuleEvent(RuleStatus.NONE);

		
		
	}
	


	

	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#getUnit()
	 */
	public String getUnit(){
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#equals(com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule)
	 */
	public boolean equals(TraceAnalyserRule rule) {
		ParameterRule parameterRule = (ParameterRule) rule;
		if (parameterRule.getName().equals(this.name)
				&& parameterRule.getDescription().equals(this.description)
				&& parameterRule.getLimitA() == this.LimitA
				&& parameterRule.getTraceItem().equals(this.traceItem)) {
			if (parameterRule.getParameterType() == ParameterType.BETWEEN && 
				parameterRule.getLimitB() != this.LimitB ){
				return false;
			}
			return true;
		} 
		else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#getLimits()
	 */
	public int[] getLimits() {
		if(parameterType == ParameterType.BETWEEN){
			return new int[]{LimitA, LimitB};
		}
		else{
			return new int[]{LimitA};

		}
	}
	
	/* Getters and setters for member variables. */


	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public TraceInfo getTraceItem() {
		return traceItem;
	}

	public void setTraceItem(TraceInfo traceItem) {
		this.traceItem = traceItem;
	}



	public int getLimitA() {
		return LimitA;
	}

	public void setLimitA(int limitA) {
		LimitA = limitA;
	}

	public int getLimitB() {
		return LimitB;
	}

	public void setLimitB(int limitB) {
		LimitB = limitB;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}


	
	
	
	
		
	
}
