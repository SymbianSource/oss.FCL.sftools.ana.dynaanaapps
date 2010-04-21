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
package com.nokia.s60tools.swmtanalyser.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.s60tools.swmtanalyser.SwmtAnalyserPlugin;

/**
 * Helper class to store the selections in Advanced Filter Dialog.
 *
 */
public class SaveFilterOptions {

	public static final String FILTER_OPTION_SECTION = "Filter Selection Section";
	public static final String FILTER_OPTION = "Filter Selection";
	
	public static final String FILTER_TEXT_SECTION = "Filter Text Section";
	public static final String FILTER_TEXT = "Filter text";
	
	public static final String EVENTS_SECTION = "Events Section";
	public static final String EVENTS_LIST = "List of events";
	
	public static final String SEVERITY_SECTION = "Severity Section";
	public static final String SEVERITY_LIST = "List of severities";
	
	public static final String SIZE_OPTION_SECTION = "Size Selection Section";
	public static final String SIZE_OPTION = "Size Selection";
	
	public static final String COUNT_OPTION_SECTION = "Count Selection Section";
	public static final String COUNT_OPTION = "Count Selection";

	public static final String SIZES_SECTION = "Sizes Section";
	public static final String TO_FROM_SIZES = "Sizes";
	
	public static final String COUNTS_SECTION = "Counts Section";
	public static final String TO_FROM_COUNT = "Counts";
	
	public static enum ValueTypes {FILTER_TYPE, ITEM_TEXT, EVENTS, SEVERITIES, SIZE_TYPE, COUNT_TYPE, SIZES, COUNTS}
	
	
	/**
	 * Save selected values
	 * @see #getValues(ValueTypes)
	 * @param key
	 * @param values
	 */
	public void saveValues(ValueTypes key, String[] values) {
		IDialogSettings section = null;
		switch (key) {
		case EVENTS:
			section = getSection(EVENTS_SECTION);
			if(section!=null)
				section.put(EVENTS_LIST, values);
			break;
		case SEVERITIES:
			section = getSection(SEVERITY_SECTION);
			if(section!=null)
				section.put(SEVERITY_LIST, values);
			break;
		case SIZES:
			section = getSection(SIZES_SECTION);
			if(section!=null)
				section.put(TO_FROM_SIZES, values);
			break;
		case COUNTS:
			section = getSection(COUNTS_SECTION);
			if(section!=null)
				section.put(TO_FROM_COUNT, values);
			
			break;
		default:
			break;
		}
	}

	/**
	 * Get values for type
	 * @see #saveValues(ValueTypes, String[])
	 * @param key
	 * @return values
	 */
	public String[] getValues(ValueTypes key)
	{
		IDialogSettings section = null;
		String[] values = null;
		switch (key) {
		case EVENTS:
			section = getSection(EVENTS_SECTION);
			if(section != null)
				values = section.getArray(EVENTS_LIST);
			break;
		case SEVERITIES:
			section = getSection(SEVERITY_SECTION);
			if(section != null)
				values = section.getArray(SEVERITY_LIST);
			break;
		case SIZES:
			section = getSection(SIZES_SECTION);
			if(section != null)
				values = section.getArray(TO_FROM_SIZES);
			break;
		case COUNTS:
			section = getSection(COUNTS_SECTION);
			if(section != null)
				values = section.getArray(TO_FROM_COUNT);
			break;
		default:
			break;
		}
		return values;		
	}

	/**
	 * Save selected value using that in next time when dialog is opened
	 * @see #getPreviousDropdownOption(ValueTypes)
	 * @see #getValues(ValueTypes) for indexes
	 * @param key
	 * @param index
	 */
	public void saveDropdownOption(ValueTypes key, String index)
	{
		IDialogSettings section = null;
		switch (key) {
		case SIZE_TYPE:
			section = getSection(SIZE_OPTION_SECTION);
			if(section!=null)
				section.put(SIZE_OPTION, index);
			break;

		case COUNT_TYPE:
			section = getSection(COUNT_OPTION_SECTION);
			if(section!=null)
				section.put(COUNT_OPTION, index);
			break;
		case FILTER_TYPE:
			section = getSection(FILTER_OPTION_SECTION);
			if(section!=null)
				section.put(FILTER_OPTION, index);
			break;
		case ITEM_TEXT:
			section = getSection(FILTER_TEXT_SECTION);
			if(section!=null)
				section.put(FILTER_TEXT, index);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Get previously selected value for type
	 * @see #saveDropdownOption(ValueTypes, String)
	 * @param key
	 * @return previously selected value for type (one of the public constants described in this class).
	 */
	public String getPreviousDropdownOption(ValueTypes key)
	{
		IDialogSettings section = null;
		String index = null;
		switch (key) {
		case SIZE_TYPE:
			section = getSection(SIZE_OPTION_SECTION);
			if(section != null)
				index = section.get(SIZE_OPTION);
			break;
		case COUNT_TYPE:
			section = getSection(COUNT_OPTION_SECTION);
			if(section != null)
				index = section.get(COUNT_OPTION);
			break;
		case FILTER_TYPE:
			section = getSection(FILTER_OPTION_SECTION);
			if(section != null)
				index = section.get(FILTER_OPTION);
			break;
		case ITEM_TEXT:
			section = getSection(FILTER_TEXT_SECTION);
			if(section != null)
				index = section.get(FILTER_TEXT);
			break;
		default:
			break;
		}
		return index;
	}
	
	/**
	 * @param sectionName name of the section to be retrieved
	 * @return section which maps to given name, in dialog_setting.xml file.
	 */
	protected IDialogSettings getSection(String sectionName) {
		IDialogSettings retVal = null;
		if (SwmtAnalyserPlugin.getDefault().getDialogSettings() != null) {
			 retVal = SwmtAnalyserPlugin.getDefault().getDialogSettings().getSection(sectionName);
			if (retVal == null) {
			 retVal = SwmtAnalyserPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
			}
		}
		return retVal;
	}	

}
