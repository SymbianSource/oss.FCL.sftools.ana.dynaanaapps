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



package com.nokia.s60tools.memspy.model;

import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;

/**
 * This class is used to save and restore data which is entered by user in 
 * wizard pages.
 *
 */
public class UserEnteredData {

	// sections names for each wizard page
	private static final String SECTION_SELECT_ACTION = "SelectActionPage";
	private static final String SECTION_IMPORT_HEAP = "SelectDeviceOrFile";
	private static final String SECTION_COMPARE_HEAPS_FIRST = "CompareHeapsFirst";
	private static final String SECTION_COMPARE_HEAPS_SECOND = "CompareHeapsSecond";
	private static final String SECTION_DEFINE_OUTPUT = "OutputFile";
	private static final String SECTION_IMPORT_SWMT = "SWMT";
	private static final String SECTION_PARAMETER_FILES = "ParameterFiles";
	
	
	// Item key names.
	private static final String PREVIOUS_RADIO_BUTTON_SELECTION = "PreviousRadioButtonSelection";
	private static final String PREVIOUS_IMPORTED_FILES = "PreviousFiles";
	private static final String PREVIOUS_INTERVAL = "Interval";
	
	/**
	 * How many previously entered values are saved
	 */
	public static final int MAX_SAVED_VALUES = 5;
	

	/**
	 * Enumeration for actions available
	 */
	public static enum ValueTypes {	SELECT_ACTION,
									IMPORT_HEAP, 
									COMPARE_HEAP_FIRST_HEAP,
									COMPARE_HEAP_SECOND_HEAP,
									SWMT,
									OUTPUT_FILE} 

	/**
	 * Returns previous radio button selection from given valuetype
	 * @param valueType what radio button selection is requested
	 * @return previous radio button selection
	 */
	public int getPreviousRadioButtonSelection( ValueTypes valueType) {
		try {
			int retval = 0;
			
			// Get int from that section where value is saved.
			
			switch(valueType){
				case SELECT_ACTION:{
					IDialogSettings section = getSection(SECTION_SELECT_ACTION);
					if (section != null) {
						retval = section.getInt(PREVIOUS_RADIO_BUTTON_SELECTION);
						
					}
					break;
				}
				case IMPORT_HEAP:{
					IDialogSettings section = getSection(SECTION_IMPORT_HEAP);
					if (section != null) {
						retval = section.getInt(PREVIOUS_RADIO_BUTTON_SELECTION);
					}
					break;
				}
				case COMPARE_HEAP_FIRST_HEAP:{
					IDialogSettings section = getSection(SECTION_COMPARE_HEAPS_FIRST);
					if (section != null) {
						retval = section.getInt(PREVIOUS_RADIO_BUTTON_SELECTION);
						
					}
					break;
				}
				case COMPARE_HEAP_SECOND_HEAP:{
					IDialogSettings section = getSection(SECTION_COMPARE_HEAPS_SECOND);
					if (section != null) {
						retval = section.getInt(PREVIOUS_RADIO_BUTTON_SELECTION);
						
					}
					break;
				}
				case SWMT:{
					IDialogSettings section = getSection(SECTION_IMPORT_SWMT);
					if (section != null) {
						retval = section.getInt(PREVIOUS_RADIO_BUTTON_SELECTION);
						
					}
					break;
				}
				default:{
					break;
				}
			}	
			return retval;
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Saves radio button selection into correct section
	 * @param valueType type of radio button
	 * @param value radio button value
	 */
	public void saveRadioButtonSelection(ValueTypes valueType, int value) {
		try {
 
			// Save integer into correct section.

			switch(valueType){
				case SELECT_ACTION:{
					IDialogSettings section = getSection(SECTION_SELECT_ACTION);
					if (section != null) {
						section.put(PREVIOUS_RADIO_BUTTON_SELECTION, value);
					}
					break;
				}
				case IMPORT_HEAP:{
					IDialogSettings section = getSection(SECTION_IMPORT_HEAP);
					if (section != null) {
						section.put(PREVIOUS_RADIO_BUTTON_SELECTION, value);
					}
					break;
				}
				case COMPARE_HEAP_FIRST_HEAP:{
					IDialogSettings section = getSection(SECTION_COMPARE_HEAPS_FIRST);
					if (section != null) {
						section.put(PREVIOUS_RADIO_BUTTON_SELECTION, value);
					}
					break;
				}
				case COMPARE_HEAP_SECOND_HEAP:{
					IDialogSettings section = getSection(SECTION_COMPARE_HEAPS_SECOND);
					if (section != null) {
						section.put(PREVIOUS_RADIO_BUTTON_SELECTION, value);
					}
					
					break;
				}
				case SWMT:{
					IDialogSettings section = getSection(SECTION_IMPORT_SWMT);
					if (section != null) {
						section.put(PREVIOUS_RADIO_BUTTON_SELECTION, value);
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
		if (MemSpyPlugin.getDefault().getDialogSettings() != null) {
			retVal = MemSpyPlugin.getDefault().getDialogSettings().getSection(section);
			if (retVal == null) {
				retVal = MemSpyPlugin.getDefault().getDialogSettings().addNewSection(section);
			}
		}
		return retVal;
	}	
	
	
	/**
	 * Returns values user has previously entered to wizard pages.
	 * @param valueType type of the values 
	 * @return user's previous values
	 */
	public String[] getPreviousValues(ValueTypes valueType) {
		try {
			String[] retVal = null;
			
			// get value from correct section
			switch (valueType) {
				case IMPORT_HEAP: {
					retVal = getPreviousPaths(SECTION_IMPORT_HEAP, PREVIOUS_IMPORTED_FILES);
					break;
				}
				case COMPARE_HEAP_FIRST_HEAP: {
					retVal = getPreviousPaths(SECTION_COMPARE_HEAPS_FIRST, PREVIOUS_IMPORTED_FILES);
					break;
				}
				case COMPARE_HEAP_SECOND_HEAP: {
					retVal = getPreviousPaths(SECTION_COMPARE_HEAPS_SECOND, PREVIOUS_IMPORTED_FILES);
					break;
				}
				case OUTPUT_FILE:{
					retVal = getPreviousPaths(SECTION_DEFINE_OUTPUT, PREVIOUS_IMPORTED_FILES);
					break;
				}
				case SWMT:{
					retVal = getPreviousPaths(SECTION_IMPORT_SWMT, PREVIOUS_INTERVAL);
					break;
				}
				default:{
					break;
				}
			}
			
			return retVal;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Saves user's latest value.
	 * @param pathType value type
	 * @param value value to be saved
	 */
	public void saveValue(ValueTypes pathType, String value) {
		if (value.trim().length() < 1)
			return;
		
		try {
			switch (pathType) {
				case IMPORT_HEAP: {
					savePath(value, PREVIOUS_IMPORTED_FILES, getSection(SECTION_IMPORT_HEAP));
					break;
				}
								
				case COMPARE_HEAP_FIRST_HEAP: {
					savePath(value, PREVIOUS_IMPORTED_FILES, getSection(SECTION_COMPARE_HEAPS_FIRST));

					break;
				}
				case COMPARE_HEAP_SECOND_HEAP:{
					savePath(value, PREVIOUS_IMPORTED_FILES, getSection(SECTION_COMPARE_HEAPS_SECOND));

					break;
				}
				case OUTPUT_FILE:{
					savePath(value, PREVIOUS_IMPORTED_FILES, getSection(SECTION_DEFINE_OUTPUT));
					break;
				}
				case SWMT:{
					savePath(value, PREVIOUS_INTERVAL, getSection(SECTION_IMPORT_SWMT));
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
	 * Returns previously entered values of wanted context (i.e. wizard page).
	 * @param section section which contains array
	 * @param array name of the array whose values are needed
	 * @return previously entered paths of given section
	 */
	protected String[] getPreviousPaths(String section, String array) {
		String[] retVal = null;
		IDialogSettings sect = getSection(section);
		if (sect != null) {
			retVal = sect.getArray(array);
		}
		
		return retVal;
	}
	
	/**
	 * Saves given path to correct section in dialog_settings.xml
	 * @param path path to save
	 * @param array name of the array which contains correct values
	 * @param section section which has array 
	 */
	protected void savePath(String path, String array, IDialogSettings section) {
		if (section != null) {
			String[] previousValues = section.getArray(array);
			
			// No previous values exist
			if (previousValues == null) {
				previousValues = new String[1];
				previousValues[0] = path;
			// Previous values exists
			} else {
				int valuesCount = previousValues.length;
				
				boolean valueExisted = false;
				// see if passed value already exist.
				for (int i = 0; i < valuesCount; i++) {
					if (previousValues[i].compareToIgnoreCase(path) == 0) {
						valueExisted = true;
						
						// passed value exists, move it to first position
						for (int j = i; j > 0; j--) {
							previousValues[j] = previousValues[j-1];
						}
						previousValues[0] = path;
						
						break;
					}
				}
				
				// passed value did not exist, add it to first position (and move older values "down")
				if (!valueExisted) {
					if (valuesCount >= MAX_SAVED_VALUES) {
						for (int i = valuesCount-1; i > 0; i--) {
							previousValues[i] = previousValues[i-1];
						}
						previousValues[0] = path;
					} else {
						String[] values = new String[valuesCount + 1];
						values[0] = path;
						for (int i = 0; i < valuesCount; i++) {
							values[i+1] = previousValues[i];
						}
						previousValues = values;
					}
				}
			}
			section.put(array, previousValues);
		}
	}

	
	/**
	 * Get selections for dialog
	 * @return parameter files section
	 */
	public static IDialogSettings getParameterFilesSection() {
		IDialogSettings retVal = null;
		if (MemSpyPlugin.getDefault().getDialogSettings() != null) {
			retVal = MemSpyPlugin.getDefault().getDialogSettings().getSection(SECTION_PARAMETER_FILES);
			if (retVal == null) {
				retVal = MemSpyPlugin.getDefault().getDialogSettings().addNewSection(SECTION_PARAMETER_FILES);
			}
		}
		return retVal;
	}

}
