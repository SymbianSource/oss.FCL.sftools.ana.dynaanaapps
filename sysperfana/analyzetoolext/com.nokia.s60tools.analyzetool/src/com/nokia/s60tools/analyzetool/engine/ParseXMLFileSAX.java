/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ParseXMLFileSAX
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IProject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Parses atool.exe generated xml file and creates memory analysis results.
 * Using the SAX parser to parse xml file. SAX parser fits better than DOM
 * parser because it is possible that AT must handle very big xml files.
 *
 * @author kihe
 *
 */
public class ParseXMLFileSAX implements org.xml.sax.ContentHandler {
	/** XML constants. */
	public static final String XML_BUILD_TARGET = "build_target";
	public static final String XML_CALC_MEMADDR = "calc_addr";
	public static final String XML_CALLSTACK = "callstack";
	public static final String XML_COUNT = "count";
	public static final String XML_END_TIME = "end_time";
	public static final String XML_FILE = "file";
	public static final String XML_FUNCTION = "function";
	public static final String XML_FUNCTION_LINE = "function_line";
	public static final String XML_HANDLE_LEAKS = "handle_leaks";
	public static final String XML_ID = "id";
	public static final String XML_ITEM = "item";
	public static final String XML_LEAK = "leak";
	public static final String XML_LEAKS = "leaks";
	public static final String XML_LINE = "line";
	public static final String XML_MEM_LEAKS = "mem_leaks";
	public static final String XML_MEMADDR = "memaddress";
	public static final String XML_MODULE = "module";
	public static final String XML_NAME = "name";
	public static final String XML_PROCESS_NAME = "process_name";
	public static final String XML_RUN = "run";
	public static final String XML_SIZE = "size";
	public static final String XML_START_TIME = "start_time";
	public static final String XML_SUBTEST = "subtest";
	public static final String XML_TIME = "time";
	public static final String XML_UNKNOWN = "???";

	/**Active call stack item id*/
	private int activeCallstackID = 1;

	/** Active item(memory leak) information */
	private AnalysisItem activeItem;

	/**Active item(memory leak) id*/
	private int activeItemID = 1;

	/** Summary field active module memory leak count*/
	private String activeModuleCount;

	/** Summary field active module name*/
	private String activeModuleName;

	/** Active run information */
	protected RunResults activeRunResults;

	/** Contains all the runs parsed results */
	private final AbstractList<RunResults> activeRuns;

	/** Active subtest item information */
	private Subtest activeSubtest;

	/**Active subtest id*/
	private int activeSubtestID = 1;

	/** Active callstack item information */
	private CallstackItem callstackItem;

	/** xml file path. */
	private final String filePath;

	/** Do we parsing module leak summary field */
	private boolean moduleLeakActive = true;

	/** List of modules */
	private final AbstractList<String> moduleList;

	/** Do we parsing subtest info just now */
	private boolean parsingSubtest = false;

	/** Project reference */
	private final IProject project;

	/** Project result reference where to save results */
	private final ProjectResults results;

	/** Index for active items */
	private int runID = 1;
	

	/**
	 * Constructor.
	 *
	 * @param projectRef
	 *            Project reference
	 * @param newFilePath
	 *            Used file path
	 * @param projResults
	 *            Project results reference
	 */
	public ParseXMLFileSAX(
			final org.eclipse.core.resources.IProject projectRef,
			final String newFilePath, final ProjectResults projResults) {
		results = projResults;
		filePath = newFilePath;
		project = projectRef;
		activeRuns = new ArrayList<RunResults>();
		moduleList = new ArrayList<String>();
	}

	/**
	 * Adds module name to list
	 *
	 * @param moduleName
	 *            Module to add to the list
	 */
	private void addModuleNameToList(final String moduleName) {
		if (moduleName == null || ("").equals(moduleName)) {
			return;
		}
		if (!moduleList.contains(moduleName)) {
			moduleList.add(moduleName);
		}
	}

	public void characters(char[] ch, int start, int length) {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	/**
	 * When the SAX parser reach the end of the XML file this function is
	 * called.
	 *
	 * Updates results.
	 */
	public void endDocument() throws SAXException {
		results.updateRunResults(project, activeRuns, filePath);
	}

	/**
	 * When the SAX parser reach the end of the xml tag this function is called.
	 *
	 * Checks what tag read is finished and stores corresponding results
	 */
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// "results" tag => store results
		if (name.equals(XML_RUN)) {
			activeRuns.add(activeRunResults);
		} else if (name.equals(XML_LEAK)) {

			//not enough information so clear the active item
			if( activeItem == null || !activeItem.checkData() ) {
				activeItem = null;
			}
			else if (parsingSubtest) {
				activeSubtest.addAnalysisItem(activeItem);
			} else {
				activeRunResults.addAnalysisItem(activeItem);
			}
		} else if (name.equals(XML_ITEM)) {
			if( callstackItem.isEmpty() ) {
				callstackItem = null;
			}
			else {
				activeItem.addCallstackItem(callstackItem);
			}
		} else if (name.equals(XML_SUBTEST)) {
			activeRunResults.addSubtest(activeSubtest);
			parsingSubtest = false;
		} else if (name.equals(XML_MODULE)) {

			try {
				// check that module count value is valid
				if (activeModuleCount == null || ("").equals(activeModuleCount)) {
					moduleLeakActive = true;
					return;
				}

				// convert module count information to string object
				int count = Integer.parseInt(activeModuleCount,16);

				// if module leak summary is active
				if (moduleLeakActive) {
					activeRunResults.addModuleLeak(activeModuleName, count);
				}
				// if handle leak summary is active
				else {
					activeRunResults.addHandleLeak(activeModuleName, count);
				}

			}catch( NumberFormatException npe ) {
				npe.printStackTrace();
			}
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	/**
	 * Returns list of modules
	 *
	 * @return List of modules
	 */
	public AbstractList<String> getModules() {
		return moduleList;
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	/**
	 * Parses xml file content.
	 *
	 * @return True if there are no errors while parsing XML file otherwise
	 *         False
	 */
	public final boolean parse() {

		boolean ret = false;
		try {

			// check that file exists
			File file = new File(filePath);
			if (file.exists()) {

				SAXParserFactory factor = null;
				SAXParser parser = null;
				XMLReader xmlReader = null;

				// get parser factory
				factor = SAXParserFactory.newInstance();

				if (factor != null) {
					// get xml parser
					parser = factor.newSAXParser();

					if (parser != null) {
						// get xml reader
						xmlReader = parser.getXMLReader();

						if (xmlReader != null) {
							// set content handle to this class
							xmlReader.setContentHandler(this);

							// parse xml file
							xmlReader.parse(new InputSource(file.getPath()));
							ret = true;
						}
					}
				}
			}

			// file does not exists
			else {
				ret = false;
			}

			//check that XML file contains data
			//otherwise return false
			if( activeRunResults == null ) {
				ret = false;
			}
		} catch (SAXException sae) {
			sae.printStackTrace();
			ret = false;
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			ret = false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ret = false;
		}

		// no errors => return positive value
		return ret;
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	public void setDocumentLocator(Locator locator) {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	public void skippedEntity(String name) throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	public void startDocument() throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}

	/**
	 * When the SAX parser start to read xml tag this function is called.
	 *
	 * Checks what tag read is started and initializes corresponding objects
	 */
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {

		//parse one run information
		if (name.equals(XML_RUN)) {
			activeRunResults = new RunResults(runID);
			activeRunResults.setEndTime(atts.getValue(XML_END_TIME));
			activeRunResults.setBuildTarget(atts.getValue(XML_BUILD_TARGET));
			activeRunResults.setProcessName(atts.getValue(XML_PROCESS_NAME));
			activeRunResults.setStartTime(atts.getValue(XML_START_TIME));
			runID++;
			activeItemID = 1;
			activeSubtestID = 1;
		}
		//parse one memory leak information
		else if (name.equals(XML_LEAK)) {
			try {
				activeItem = new AnalysisItem();
				activeItem.setID(activeItemID);
				activeItem.setMemoryAddress(atts.getValue(XML_MEMADDR));
				activeItem.setMemoryLeakTime(atts.getValue(XML_TIME));
				String moduleName = atts.getValue(XML_MODULE);
				addModuleNameToList(moduleName);
				activeItem.setModuleName(moduleName);
				String size = atts.getValue(XML_SIZE);
				if (size != null && !("").equals(size)) {
					activeItem.setLeakSize(Integer
							.parseInt(size));
				}

			}catch(NumberFormatException npe) {
				npe.printStackTrace();
			}
			activeItemID++;
			activeCallstackID = 1;
		}
		//parse call stack item information
		else if (name.equals(XML_ITEM)) {
			try{
				//create new item
				callstackItem = new CallstackItem();
				callstackItem.setID(activeCallstackID);
				callstackItem.setFileName(atts.getValue(XML_FILE));
				callstackItem.setFunctionName(atts.getValue(XML_FUNCTION));
				callstackItem.setMemoryAddress(atts.getValue(XML_MEMADDR));

				//get module name and add it to list
				//module name list is used later.
				String moduleName = atts.getValue(XML_MODULE);
				addModuleNameToList(moduleName);
				callstackItem.setModuleName(moduleName);

				//if line number is added to the XML file
				//this means that the results are generated to UREL
				String lineNumber = atts.getValue(XML_LINE);
				if (lineNumber != null && !("").equals(lineNumber) && !(XML_UNKNOWN).equals(lineNumber)) {
					callstackItem.setLeakLineNumber(Integer.parseInt(lineNumber));
				}

				//if function line is added to the XML file
				//this means that the results are generated to UREL
				String functionLine = atts.getValue(XML_FUNCTION_LINE);
				if (functionLine != null && !("").equals(functionLine) && !(XML_UNKNOWN).equals(functionLine)) {
					callstackItem.setLeakLineNumber(Integer.parseInt(functionLine));
					callstackItem.setUrelBuild(true);
				}
			}catch(NumberFormatException npe) {
				npe.printStackTrace();
			}
			activeCallstackID++;

		}
		//parse subtest information
		else if (name.equals(XML_SUBTEST)) {
			activeSubtest = new Subtest(activeSubtestID);
			activeItemID = 1;
			activeCallstackID = 1;
			activeSubtest.setEndTime(atts.getValue(XML_END_TIME));
			activeSubtest.setName(atts.getValue(XML_NAME));
			activeSubtest.setStartTime(atts.getValue(XML_START_TIME));
			parsingSubtest = true;
			activeSubtestID++;
		} else if (name.equals(XML_HANDLE_LEAKS)) {
			moduleLeakActive = false;
		} else if (name.equals(XML_MEM_LEAKS)) {
			moduleLeakActive = true;
		} else if (name.equals(XML_MODULE)) {
			activeModuleName = atts.getValue(XML_NAME);
			activeModuleCount = atts.getValue(XML_LEAKS);
		}

	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// Do nothing be design, because AT is not interested of this
		// information
		// also this method is overloaded
	}
}
