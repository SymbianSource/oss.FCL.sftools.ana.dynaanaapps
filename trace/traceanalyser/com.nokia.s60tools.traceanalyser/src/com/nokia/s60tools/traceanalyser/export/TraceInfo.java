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

import java.io.BufferedReader;
import java.io.IOException;

import com.nokia.traceviewer.engine.TraceInformation;

/**
 * class TraceInfo.
 * Class that contains name and id numbers of one trace. 
 *
 */
public class TraceInfo {
	
	public static final String XML_COMPONENT_ID = "ComponentID";
	public static final String XML_GROUP_ID = "GroupID";
	public static final String XML_TRACE_ID = "TraceID";
	public static final String XML_TRACE_NAME = "TraceName";
	
	/* TraceInformation object containing ComponentID, GroupID and TraceID */
	private TraceInformation idNumbers;
	
	/* Name of the Trace */
	private String traceName;

	/**
	 * TraceInfo.
	 * Constructor.
	 */
	public TraceInfo() {
		idNumbers = new TraceInformation();
		traceName = "";
	}

	/**
	 * getXMLString.
	 * Returns XML-string that is written to file when saving rule.
	 * @param itemName Name of the trace item.
	 * @param indent indent that is used.
	 * @return xml string.
	 */
	public String getXMLString(String itemName, int indent) {
		String xml = "";
		String indentString = getIndentString(indent);
		
		xml += indentString + "<" + itemName + ">\n";
		
		xml += indentString + "    " + "<" + XML_COMPONENT_ID + "=\"";
		xml += Integer.toHexString(idNumbers.getComponentId());
		xml += "\">\n";
		
		xml += indentString + "    " + "<" + XML_GROUP_ID + "=\"";
		xml += Integer.toHexString(idNumbers.getGroupId());
		xml += "\">\n";
		
		xml += indentString + "    " + "<" + XML_TRACE_ID + "=\"";
		xml += Integer.toHexString(idNumbers.getTraceId());
		xml += "\">\n";
		
		xml += indentString + "    " + "<" + XML_TRACE_NAME + "=\"";
		xml += traceName;
		xml += "\">\n";
	
		xml += indentString + "</" + itemName + ">\n";
		
		return xml;
	}
	
	/**
	 * readXMLBuffer.
	 * Reads xml trace information from bufferedReader and saves it into this object
	 * @param input bufferedReader
	 * @param searchTraceName name of the trace in buffer
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */

	public boolean readXMLBuffer(BufferedReader input, String searchTraceName) throws NumberFormatException, IOException{

		String line = null;
		
		
		// This while loop searches xml tags from file. When tag is found information is saved and 
		// new searchPhrase is updated. Tags are searched in order where they should be.
		
		String searchPhrase = searchTraceName;
		while ((line = input.readLine()) != null) {
			if (line.contains(searchPhrase)) {
				if (searchPhrase.equals(searchTraceName)) {
					searchPhrase = XML_COMPONENT_ID;
				}

			
				else if (searchPhrase.equals(XML_COMPONENT_ID)) {
					Long id = Long.parseLong(GeneralMethods.getTextBetweenQuotes(line), 16);
					idNumbers.setComponentId(id.intValue());
					searchPhrase = XML_GROUP_ID;
				}
				else if (searchPhrase.equals(XML_GROUP_ID)) {
					idNumbers.setGroupId(Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line), 16));
					searchPhrase = XML_TRACE_ID;
				}
				else if (searchPhrase.equals(XML_TRACE_ID)) {
					idNumbers.setTraceId(Integer.parseInt(GeneralMethods.getTextBetweenQuotes(line), 16));
					searchPhrase = XML_TRACE_NAME;
				}
				else if (searchPhrase.equals(XML_TRACE_NAME)) {
					String name = GeneralMethods.getTextBetweenQuotes(line);
					setTraceName(name);
					return true;
				}
			}
		}
		return false;

	}

    /**
     * getIndentString.
     * @param indent indent.
     * @return string containing as many spaces as parameter states.
     */
    protected String getIndentString(int indent){
    	String indentString = "";
    	while(indent > 0){
			indentString += " ";
			indent--;
		}
    	return indentString;
    }

    /**
     * getIdNumbers.
     * @return id numbers of the trace.
     */
	public TraceInformation getIdNumbers() {
		return idNumbers;
	}

	/**
	 * setIdNumbers.
	 * @param idNumbers
	 */
	public void setIdNumbers(TraceInformation idNumbers) {
		this.idNumbers = idNumbers;
	}

	/**
	 * getTraceName.
	 * @return name of the trace.
	 */
	public String getTraceName() {
		return traceName;
	}

	/**
	 * setTraceName.
	 * @param traceName
	 */
	public void setTraceName(String traceName) {
		this.traceName = traceName;
	}

	/**
	 * equals.
	 * compares two traceInfos
	 * @param traceB 
	 * @return true if traceinfo objects are equal.
	 */
	public boolean equals(TraceInfo traceB){
		if(compareTraces(this.getIdNumbers(), traceB.getIdNumbers()) && 
			this.traceName.equals(traceB.getTraceName())){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/**
	 * compareTraces.
	 * compares two traces.
	 * @param traceA Trace A
	 * @param traceB Trace B
	 * @return true if trace information was same.
	 */
	public static boolean compareTraces(TraceInformation traceA, TraceInformation traceB){
		if(traceA.getComponentId() == traceB.getComponentId() && 
		   traceA.getGroupId() == traceB.getGroupId()&&
		   traceA.getTraceId() == traceB.getTraceId()){
			return true;
		}
		else{
			return false;
		}
	}
	
}
