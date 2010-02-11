/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.model;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import com.nokia.s60tools.crashanalyser.containers.ErrorLibraryError;
import com.nokia.s60tools.crashanalyser.plugin.*;

/**
 * This class reads all error xml files with SAX parser.  
 *
 */
public class ErrorsXmlReader extends DefaultHandler {

	Map<String, ErrorLibraryError> errors = null;
	Map<String, ErrorLibraryError> panics = null;
	Map<String, ErrorLibraryError> categories = null;
	ErrorLibraryError error = null;
	ErrorLibraryError error2 = null;
	ErrorLibraryError panic = null;
	ErrorLibraryError category = null;
	String nodeText = "";
	String categoryDescription = "";
	String categoryKey = "";
	String panicKey = "";
	String errorKey1 = "";
	String errorKey2 = "";
	String errorComponent = "";
	static final String TAG_CATEGORY_NAME = "category_name";
	static final String TAG_CATEGORY_DESCRIPTION = "category_description";
	static final String TAG_PANIC_ID = "panic_id";
	static final String TAG_PANIC_DESCRIPTION = "panic_description";
	static final String TAG_PANIC_CATEGORY = "panic_category";
	static final String TAG_ERROR_NAME = "error_name";
	static final String TAG_ERROR_VALUE = "error_value";
	static final String TAG_ERROR_TEXT = "error_text";
	static final String TAG_ERROR_COMPONENT = "error_component";

	/**
	 * Returns list of errors and gives up the ownership of the list.
	 * @return list of errors
	 */
	public Map<String, ErrorLibraryError> getErrorsOwnership() {
		if (errors == null || errors.isEmpty()) {
			return null;
		} else {
			Map<String, ErrorLibraryError> err = errors; 
			errors = null;
			return err;
		}
	}
	
	/**
	 * Returns list of panics and gives up the ownership of the list.
	 * @return list of panics
	 */
	public Map<String, ErrorLibraryError> getPanicsOwnership() {
		if (panics == null || panics.isEmpty()) {
			return null;
		} else {
			Map<String, ErrorLibraryError> pan = panics;
			panics = null;
			return pan;
		}
	}
	
	/**
	 * Returns list of panic categories and gives up the ownership of the list.
	 * @return list of panic categories
	 */
	public Map<String, ErrorLibraryError> getCategoriesOwnership() {
		if (categories == null || categories.isEmpty()) {
			return null;
		} else {
			Map<String, ErrorLibraryError> cat = categories;
			categories = null;
			return cat;
		}
	}

	/**
	 * Reads all error xml files
	 */
	public void readAll() {
		try {
			errors = null;
			panics = null;
			categories = null;
			
			String dataPath = CrashAnalyserPlugin.getDataPath();
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setSchema(sf.newSchema(new File(dataPath + "schema.xsd")));
			spf.setValidating(true);
			SAXParser sp = spf.newSAXParser();
			
			List<String> xmlFiles = getXmlFiles(dataPath);
			
			if (xmlFiles == null || xmlFiles.isEmpty())
				return;
			
			for (int i = 0; i < xmlFiles.size(); i++) {
				try {
					// parse the file and also register this class for call backs
					sp.parse(new File(xmlFiles.get(i)), this);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startDocument() throws SAXException {
		if (errors == null)
			errors = new HashMap<String, ErrorLibraryError>();
		if (panics == null)
			panics = new HashMap<String, ErrorLibraryError>();
		if (categories == null)
			categories = new HashMap<String, ErrorLibraryError>();
		super.startDocument();
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,	Attributes arg3) 
			throws SAXException {
		nodeText = "";
		super.startElement(arg0, arg1, arg2, arg3);
	}
	
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		String s = String.copyValueOf(arg0, arg1, arg2);
		nodeText += s;
		super.characters(arg0, arg1, arg2);
	}	

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if (TAG_CATEGORY_NAME.equals(arg2)) {
			category = new ErrorLibraryError();
			category.SetName(nodeText);
			categoryKey = nodeText;
			
		} else if (TAG_CATEGORY_DESCRIPTION.equals(arg2)) {
			categoryDescription = HtmlFormatter.formatCategoryDescription(category.toString(), nodeText);
			category.SetDescription(categoryDescription);
			
		} else if (TAG_PANIC_ID.equals(arg2)) {
			category.AddToDescription(HtmlFormatter.formatPanicName(category.toString(), nodeText));
			panic = new ErrorLibraryError();
			panic.SetName(category.toString() + " " + nodeText);
			panicKey = categoryKey + nodeText;
			
		} else if (TAG_PANIC_DESCRIPTION.equals(arg2)) {
			category.AddToDescription(nodeText);
			panic.SetDescription(HtmlFormatter.formatPanicDescription(panic.toString(), nodeText));
			panic.AddToDescription(categoryDescription);
			panics.put(panicKey, panic);
			panic = null;
			panicKey = "";
			
		} else if (TAG_PANIC_CATEGORY.equals(arg2)) {
			categories.put(categoryKey, category);
			category = null;
			categoryKey = "";
			
		} else if (TAG_ERROR_NAME.equals(arg2)) {
			error = new ErrorLibraryError();
			error.SetName(nodeText);
			errorKey1 = nodeText;
			
		} else if (TAG_ERROR_VALUE.equals(arg2)) {
			error.SetDescription(HtmlFormatter.formatErrorDescription(error.toString(), nodeText));
			error2 = new ErrorLibraryError();
			error2.SetName(nodeText);
			error2.SetDescription(HtmlFormatter.formatErrorDescription(nodeText, error.toString()));
			errorKey2 = nodeText;
			
		} else if (TAG_ERROR_COMPONENT.equals(arg2)) {
			errorComponent = nodeText;
			
		} else if (TAG_ERROR_TEXT.equals(arg2)) {
			error.AddToDescription(nodeText);
			error.AddToDescription(HtmlFormatter.formatErrorComponent(errorComponent));
			error2.AddToDescription(nodeText);
			error2.AddToDescription(HtmlFormatter.formatErrorComponent(errorComponent));
			errors.put(errorKey1, error);
			error = null;
			errorKey1 = "";
			errors.put(errorKey2, error2);
			error2 = null;
			errorKey2 = "";
		}
		
		super.endElement(arg0, arg1, arg2);
	}

	/**
	 * Returns a list of all error xml files 
	 * @param path xml files path
	 * @return a list of all error xml files
	 */
	List<String> getXmlFiles(String path) {
		try {
			List<String> xmlFiles = new ArrayList<String>();
			
			File file = new File(path);
			
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.endsWith("xml"));
				}
			};
			File[] files = file.listFiles(filter);
			
			for (int i = 0; i < files.length; i++) {
				xmlFiles.add(files[i].getAbsolutePath());
			}
			
			if (xmlFiles.isEmpty())
				return null;
			
			return xmlFiles;
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	
}
