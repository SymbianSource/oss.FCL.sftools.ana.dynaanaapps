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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.nokia.s60tools.traceanalyser.export.GeneralMethods;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.export.TraceInfo;
import com.nokia.s60tools.traceanalyser.export.RuleEvent.RuleStatus;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;

public class TimingRule extends TraceAnalyserRule {

	/* Strings that are searched from xml-file */
	public static final String XML_COMPONENT_ID = "ComponentID";
	public static final String XML_GROUP_ID = "GroupID";
	public static final String XML_TRACE_ID = "TraceID";
	public static final String XML_TRACE_NAME = "TraceName";
	public static final String XML_TIME_LIMIT_A = "TimeLimitA";
	public static final String XML_TIME_LIMIT_B = "TimeLimitB";
	


	
	/* Trace items assigned for this rule */
	private TraceInfo traceItemA;
	private TraceInfo traceItemB;

	/* Time limits assigned for this rule */
	private int timeLimitA;
	private int timeLimitB;
	
	/* previous trace event that has something to do with this rule */
	//private TraceProperties previousEvent;
	
	private int previousTraceNumber;
	private long previousTraceTimeStamp;
	
	private String workingDirectory;
	
	/**
	 * TimingRule.
	 * Constructor
	 */
	public TimingRule() {
		super("Timing Rule");
		traceItemA = new TraceInfo();
		traceItemB = new TraceInfo();
		previousTraceNumber = -1;
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
	 * readFile.
	 * Reads xml file given as parameter.
	 * @param input bufferedReader opened to inputFile.
	 * @throws IOException if file operations fails.
	 * @return true if rule was read successfully
	 */
	private boolean readFile(BufferedReader input) throws IOException{
		String line = null;
	
		// This while loop searches xml tags from file. When tag is found information is saved and 
		// new searchPhrase is updated. Tags are searched in order where they should be.
		
		if(!traceItemA.readXMLBuffer(input, "TraceA")){
			return false;
		}
		
		if(!traceItemB.readXMLBuffer(input, "TraceB")){
			return false;
		}
		
		String searchPhrase = XML_TIME_LIMIT_A;
		while ((line = input.readLine()) != null) {
			if (line.contains(searchPhrase)) {
				if(searchPhrase.equals(XML_TIME_LIMIT_A)){
					timeLimitA = Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line));
					searchPhrase = XML_TIME_LIMIT_B;

				}
				else if(searchPhrase.equals(XML_TIME_LIMIT_B)){
					timeLimitB = Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line));
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
		xml += traceItemA.getXMLString("TraceA",8);
		xml += traceItemB.getXMLString("TraceB",8);
		xml += "    </Traces>\n";
		xml += "    <TimingInfo>\n";
		xml += getTimingInfoXmlString(8);
		xml += "    </TimingInfo>\n";
		xml += "</AdditionalInfo>\n";
		return xml;
	}
	
	/**
	 * getTimingInfoXmlString.
	 * @param indent indent that is used.
	 * @return xml-string that can be written to file.
	 */
	private String getTimingInfoXmlString(int indent){
		String xml = "";
		String indentString = this.getIndentString(indent);
		xml += indentString + "<" + XML_TIME_LIMIT_A +"=\""; 
		xml += Integer.toString(timeLimitA);
		xml += "\">\n";
		xml += indentString + "<" + XML_TIME_LIMIT_B +"=\"";
		xml += Integer.toString(timeLimitB);
		xml += "\">\n";	
		
		return xml;
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#checkIfRuleFails(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public RuleEvent checkRuleStatus(TraceProperties traceProperties) {

		
		// check if received trace is trace A
		if(TraceInfo.compareTraces(traceProperties.information, traceItemA.getIdNumbers())){
			previousTraceNumber = traceProperties.traceNumber;
			previousTraceTimeStamp = traceProperties.timestamp;
			
			//previousEvent = traceProperties.clone();
			/*previousEvent = new TraceProperties(null);
			previousEvent.timestamp = traceProperties.timestamp;
			previousEvent.traceNumber = traceProperties.traceNumber;*/

		}
		
		// Check if received trace is trace B
		else if(TraceInfo.compareTraces(traceProperties.information, traceItemB.getIdNumbers()) && previousTraceNumber != -1){
		
			
			long difference = traceProperties.timestamp - previousTraceTimeStamp;
			double doubleDiff = difference/1000000.0000;
			difference = Math.round(doubleDiff);
			
			int[] traceNumbers = new int[]{previousTraceNumber, traceProperties.traceNumber};
			previousTraceNumber = -1;
			
			if(timeLimitA > difference){
				int violation = (int)difference - timeLimitA;
				return new RuleEvent(RuleStatus.FAIL, (int)difference, timeLimitA, violation, name, "ms", 
									new Date(), traceNumbers );
			}
			if(timeLimitB < difference){
				int violation = (int)difference - timeLimitB;
				return new RuleEvent(RuleStatus.FAIL, (int)difference, timeLimitB, violation, name, "ms", 
						new Date(), traceNumbers );
			}
			return new RuleEvent(RuleStatus.PASS, (int)difference, new Date(), "ms");
		
		}
		return new RuleEvent(RuleStatus.NONE);

	}
	


	

	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#getUnit()
	 */
	public String getUnit(){
		return "ms";
	}


	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule#equals(com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule)
	 */
	public boolean equals(TraceAnalyserRule rule) {
		TimingRule timingRule = (TimingRule) rule;
		if (timingRule.getName().equals(this.name)
				&& timingRule.getDescription().equals(this.description)
				&& timingRule.getTimeLimitA() == this.timeLimitA
				&& timingRule.getTimeLimitB() == this.timeLimitB
				&& true
				&& timingRule.getTraceItemA().equals(this.traceItemA)
				&& timingRule.getTraceItemB().equals(this.traceItemB)) {
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
		return new int[]{timeLimitA, timeLimitB};
	}
	
	
	/* Getters and setters for member variables. */
	public TraceInfo getTraceItemA() {
		return traceItemA;
	}
	public void setTraceItemA(TraceInfo traceItemA) {
		this.traceItemA = traceItemA;
	}
	public TraceInfo getTraceItemB() {
		return traceItemB;
	}
	public void setTraceItemB(TraceInfo traceItemB) {
		this.traceItemB = traceItemB;
	}
	public int getTimeLimitA() {
		return timeLimitA;
	}
	public void setTimeLimitA(int timeLimitA) {
		this.timeLimitA = timeLimitA;
	}
	public int getTimeLimitB() {
		return timeLimitB;
	}
	public void setTimeLimitB(int timeLimitB) {
		this.timeLimitB = timeLimitB;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	
	
	
	
		
	
}
