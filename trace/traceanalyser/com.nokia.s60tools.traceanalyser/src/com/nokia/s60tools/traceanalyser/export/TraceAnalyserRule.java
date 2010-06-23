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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.nokia.traceviewer.engine.TraceProperties;


/**
 * class TraceAnalyserRule.
 * Base class for every Trace Analyser rule.
 */
public abstract class TraceAnalyserRule {
	
	
	/* indexes for TableViewer */
	public static final int INDEX_RULE_NAME = 0;
	public static final int INDEX_FILE_DESCRIPTION = 1;
	
	/* XML-file names */
	public static final String FILENAME_BASIC_INFO ="BasicInfo.xml";
	public static final String FILENAME_ADDITIONAL_INFO ="AdditionalInfo.xml";
	public static final String FILENAME_HISTORY = "History.log";
	
	/* XML-tags that are used in saving rule data */
	public static final String XML_NAME = "    <Name=\"";
	public static final String XML_DESCRIPTION = "    <Description=\"";
	public static final String XML_ACTIVATED = "    <Activated=\"";

	
	/* Rule name */
	protected String name;
	
	/* Rule's Description */
	protected String description;
	
	/* Rule's type name */
	protected String ruleTypeName;

	/* is rule activated */
	protected boolean activated;
	
	/* Path of rule folder */
	protected String rulePath; 
	
	/**
	 * writeXML.
	 * Writes xml-information into file system. NOTE rulePath needs to be defined before calling this function.
	 * @return true if file operations were successful
	 */
	public abstract boolean writeXML();
	
	/**
	 * writeBasicInfoIntoFile.
	 * Writes basic info(name and description) into file system.
	 * @return true if file operations were successful
	 */
	protected boolean writeBasicInfoIntoFile() {
		String fileName = rulePath + FILENAME_BASIC_INFO;

		try {
			// Create file
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter output = new BufferedWriter(fstream);
			output.write(this.getBasicInfoXmlString());
			// Close the output stream
			output.close();
			return true;
		} 
		catch (Exception e) {// Catch exception if any
			return false;
		}
	}
	
	/**
	 * getBasicInfoXmlString.
	 * @return XML-string that is written to file when saving.
	 */
	private String getBasicInfoXmlString(){
		String xml = "";
		xml += "<BasicInfo>\n";
		xml += XML_NAME;
		xml += name + "\">\n";
		xml += XML_DESCRIPTION;
		xml += description + "\">\n";
		xml += XML_ACTIVATED;
		xml += activated + "\">\n";
		xml += "</BasicInfo>\n";
		return xml;
	}
	
	
	
	/**
	 * TraceAnalyserRule
	 * Constructor
	 * @param ruleType Rule Type's name
	 */
	public TraceAnalyserRule(String ruleType){
		this.ruleTypeName = ruleType;
		this.activated = true;
	}
	
	/**
	 * getName.
	 * @return name of the rule
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * setName.
	 * @param name, new name for the rule
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * getDescription.
	 * @return description of the rule
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * setDescription.
	 * @param description, new description for the rule
	 */
	public void setDescription(String definition) {
		this.description = definition;
	}

	/**
	 * getRuleType
	 * @return type of the rule
	 */
	public String getRuleType() {
		return ruleTypeName;
	}

	/**
	 * setRuleType
	 * @param ruleType, a new rule type
	 */
	public void setRuleName(String ruleName) {
		this.ruleTypeName = ruleName;
	}
		
	/**
	 * getRulePath.
	 * @return path of rule's setting files
	 */
	public String getRulePath() {
		return rulePath;
	}

	/**
	 * setRulePath.
	 * @param rulePath, a new path for setting files.
	 */
	public void setRulePath(String rulePath) {
		this.rulePath = rulePath;
	}

	
	/**
	 * isActivated.
	 * @return true if rule is activated
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * setActivated
	 * sets activation value of rule.
	 * @param activated
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * getNextFreeDirectory.
	 * Creates next free directory for Trace Analyser Rule data files
	 * @return next free directory where files can be copied.
	 */
	public String getNextFreeDirectory(String directory){
	
		// Get file list
		File file = new File( directory );	
		String[] fileList = file.list();
		
		
		directory = TraceAnalyserRule.addSlashToEnd( directory );
		int i = 0;
		File newFile = null; 
		
		// if some files are found
		if( fileList != null ){
			// Go thru directory in a loop and search for first free integer value for directory name.
			while( i <= fileList.length ){
				
				newFile = new File( directory + Integer.toString(i) );
				if ( !newFile.isDirectory() ){
					break;
				}
				i++;
			}
		}
		else{
			newFile = new File( directory + "0" );
		}

		// if directories are created successfully, return path, if not return null
		if( newFile.mkdirs() ){
			String newFileString = TraceAnalyserRule.addSlashToEnd(newFile.toString());
			return newFileString;
		}
		else{
			return null;
		}
		
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
			case INDEX_RULE_NAME:
				retval = name;
				break;
			case INDEX_FILE_DESCRIPTION:
				retval = description;
				break;
			default:
				break;
		}
		return retval;
	}
	
	/**
	 * readXML.
	 * Reads basic info from xml file. NOTE. rulePath variable must be defined before reading.
	 * @return true if rule info was read successfully
	 */
	public boolean readXML(){
		if (rulePath != null) {
			String basicInfoPath = TraceAnalyserRule.addSlashToEnd(rulePath) + FILENAME_BASIC_INFO;
			
			//Open file
			File basicInfoFile = new File(basicInfoPath);
			if (basicInfoFile.exists()) {
				try {
					// Create buffered reader
					BufferedReader input = new BufferedReader(new FileReader(basicInfoFile));
					try {
						String line = null; 

						// Read file
						while ((line = input.readLine()) != null) {
							// detect XML-tags and save values.
							if (line.contains(XML_NAME)) {
								name = GeneralMethods.getTextBetweenQuotes(line);
							} 
							else if (line.contains(XML_DESCRIPTION)) {
								description = GeneralMethods.getTextBetweenQuotes(line);
							}
							else if(line.contains(XML_ACTIVATED)){
								if(GeneralMethods.getTextBetweenQuotes(line).equals("false")){
									activated = false;
								}
								else{
									activated = true;
								}
								
							}
						}

					} 
					catch (Exception e) {
						e.printStackTrace();
					} 
					finally {
						
						// close file.
						input.close();
						
						// confirm that all needed info was read
						if (name != null && description != null) {
							return true;
						}
					}
				} 
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * If the last character of the given path is not backslash, it is added
	 * and path with backslash is returned.
	 * @param path Path to which backslash is added
	 * @return Path which last character is backslash
	 */
	public static String addSlashToEnd(String path) {
		if (path.endsWith(File.separator)){
			return path;
		}
		else{
			return path + File.separator;
		}
	}
	
	/**
	 * delete.
	 * Deletes this rule. I.e deletes all files under this
	 * rule folder and finally deletes the rule folder
	 */
	public boolean delete() {
		if (!"".equals(rulePath) && rulePath != null) {
			return this.deleteDir(new File(rulePath));
		}
		else{
			return true;
		}
		
	}
	
	/**
	 * deleteDir
	 * Deletes directory and everything inside.
	 * @param dir directory name
	 * @return true if file operations were successful
	 */
    private boolean deleteDir(File dir) {
        if ( dir.isDirectory() ) {
            
        	// get list of everything inside file.
        	String[] children = dir.list();
        	
        	// go thru file list and call this function recursively.
            for ( int i=0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // after everything inside directory is deleted, remove directory itself.
        boolean value = dir.delete();
        return value;
    }
    
    /**
     * getTextBetweenQuotes.
     * Returns text between two first quotation marks on line that is give as parameter
     * @param line where quotations are searched.
     * @return text between quotation marks
     */
    /*protected String getTextBetweenQuotes(String line){
    	int index = 0;
    	if((index =line.indexOf("\"")) > -1){
			line = line.substring(index+1);
    		if((index =line.indexOf("\"")) > -1){
    			return line.substring(0,index); 
    		}
		}
    	return null;
    }*/
    
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
     * checkRuleStatus.
     * Checks if parameter trace has some affect on rule.
     * @param traceProperties received trace
     * @return ruleEvent.
     */
    public abstract RuleEvent checkRuleStatus(TraceProperties traceProperties);
    
    /**
     * getLimitUnit.
     * @return unit of value that is measured.
     */
    public abstract String getUnit();
    
    /**
     * readHistory.
     * Reads history data from file system.
     * @return arraylist containing history data.
     */
    @SuppressWarnings("unchecked")
	public ArrayList<RuleEvent> readHistory() {
		ArrayList<RuleEvent> history = null;
		
		try {
			String fileName = addSlashToEnd(rulePath) + FILENAME_HISTORY;
			// use buffering
			InputStream file = new FileInputStream(fileName);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				history = (ArrayList<RuleEvent>) input.readObject();

			} finally {
				input.close();
				if (history == null) {
					history = new ArrayList<RuleEvent>();
				}
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			history = new ArrayList<RuleEvent>();
		} catch (IOException ex) {
			history = new ArrayList<RuleEvent>();
		}
		return history;
	}

    /**
     * saveHistory.
     * Saves given history data into this rule's own folder.
     * @param history history data that is saved.
     * @return true if file operations were successful
     */
	public boolean saveHistory(ArrayList<RuleEvent> history) {

		boolean retVal = true;
		try {
			String fileName = addSlashToEnd(rulePath) + FILENAME_HISTORY;

			OutputStream file = new FileOutputStream(fileName);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				// serialize fail list.
				output.writeObject(history);
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			retVal = false;
		}
		return retVal;

	}
	
	/**
	 * equals,
	 * Comparator
	 * @param rule Rule that is compared to this rule.
	 * @return true if rules are equal.
	 */
	public abstract boolean equals(TraceAnalyserRule rule);
    
	
	/**
	 * getLimits.
	 * @return all limits that are defined for rule
	 */
	public abstract int[] getLimits();
}
