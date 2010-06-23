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



package com.nokia.s60tools.traceanalyser.model;

import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;

/**
 * This class is used to save and restore data which is entered by user in 
 * wizard pages.
 *
 */
public class UserEnteredData {

	// section names 
	public static final String SECTION_SELECT_TRACE = "TraceSelectionDialog";
	public static final String PREVIOUS_DICTIONARY = "PreviousDictionary";
	public static final String PREVIOUS_GROUP = "PreviousGroup";
	
	public static enum ValueTypes {	PREVIOUS_GROUP,
									PREVIOUS_DICTIONARY } 

	
	
	/**
	 * getString
	 * gets string value from correct section
	 * @param valueType type of string
	 * @return String value
	 */
	
	public String getString( ValueTypes valueType) {
		try {
			String retval = "";
			
			// get value from correct section
			switch(valueType){
				case PREVIOUS_DICTIONARY:{
					IDialogSettings section = getSection(SECTION_SELECT_TRACE);
					if (section != null) {
						retval = section.get(PREVIOUS_DICTIONARY);
						
					}
					break;
			
				}
				case PREVIOUS_GROUP:{
					IDialogSettings section = getSection(SECTION_SELECT_TRACE);
					if (section != null) {
						retval = section.get(PREVIOUS_GROUP);
					}	
					break;
				}
				
				
				default:{
					break;
				}
			}	
			return retval;
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * saveString
	 * saves string value into correct section. 
	 * @param valueType type of string
	 * @param value value that is saved
	 */
	
	public void saveString(ValueTypes valueType, String value) {
		try {

			// save value into correct section

			switch(valueType){
				case PREVIOUS_DICTIONARY:{
					IDialogSettings section = getSection(SECTION_SELECT_TRACE);
					if (section != null) {
						section.put(PREVIOUS_DICTIONARY, value);
					}
					break;
				}
				case PREVIOUS_GROUP:{
					IDialogSettings section = getSection(SECTION_SELECT_TRACE);
					if (section != null) {
						section.put(PREVIOUS_GROUP, value);
					}
					break;
				}

				default:{
					break;
				}
			}	
			
			
		} catch (Exception E) {
			// No actions needed
		}
	}
	
	
	/**
	 * Returns wanted section
	 * @param section name of the wanted section
	 * @return wanted section
	 */
	protected IDialogSettings getSection(String section) {
		IDialogSettings retVal = null;
		if (TraceAnalyserPlugin.getDefault().getDialogSettings() != null) {
			retVal = TraceAnalyserPlugin.getDefault().getDialogSettings().getSection(section);
			if (retVal == null) {
				retVal = TraceAnalyserPlugin.getDefault().getDialogSettings().addNewSection(section);
			}
		}
		return retVal;
	}	
}
