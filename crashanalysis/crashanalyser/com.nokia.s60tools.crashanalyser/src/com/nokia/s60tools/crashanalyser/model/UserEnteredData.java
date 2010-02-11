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

import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.s60tools.crashanalyser.plugin.CrashAnalyserPlugin;

/**
 * This class is used to save and restore data which is entered by user in 
 * wizard pages.
 *
 */
public class UserEnteredData {
	// sections for each wizard page
	public static final String SECTION_CRASH_FILE_OR_FOLDER = "CrashFileOrFolderPage"; //$NON-NLS-1$
	public static final String SECTION_PARAMETER_FILES = "ParameterFilesPage"; //$NON-NLS-1$
	public static final String SECTION_ADDITIONAL_SETTINGS = "AdditionalSettingsPage"; //$NON-NLS-1$
	public static final String SECTION_CALL_STACK = "CallStack";
	
	// FileOrPathSelectionPage
	public static final String LAST_USED_CRASH_FILES_OR_FOLDERS = "CrashFilesOrPaths"; //$NON-NLS-1$
	
	//SDK selection for Call Stack popup menu
	public static final String LAST_USED_SOURCE_SDK = "LastUsedSourceSdk";
	
	public static enum ValueTypes {	CRASH_FILE_OR_PATH, 
									TEXT_OR_HTML_OUTPUT_PATH} 
	public static final int MAX_SAVED_VALUES = 5;

	/**
	 * Returns values user has previously entered to wizard pages.
	 * @param valueType type of the values 
	 * @return user's previous values
	 */
	public String[] getPreviousValues(ValueTypes valueType) {
		try {
			String[] retVal = null;

			if (ValueTypes.CRASH_FILE_OR_PATH.equals(valueType)) {
				retVal = getPreviousPaths(SECTION_CRASH_FILE_OR_FOLDER, LAST_USED_CRASH_FILES_OR_FOLDERS);
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
			if (ValueTypes.CRASH_FILE_OR_PATH.equals(pathType)) {
				savePath(value, LAST_USED_CRASH_FILES_OR_FOLDERS, getSection(SECTION_CRASH_FILE_OR_FOLDER));
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	public void savePreviousSdk(String sdkName) {
		try {
			IDialogSettings section = getSection(SECTION_CALL_STACK);
			if (section != null) {
				section.put(LAST_USED_SOURCE_SDK, sdkName);
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	public String getPreviousSdk() {
		String retval = "";
		try {
			IDialogSettings section = getSection(SECTION_CALL_STACK);
			if (section != null) {
				retval = section.get(LAST_USED_SOURCE_SDK);
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
		return retval;
	}

	/**
	 * Saves given path to correct section in dialog_settings.xml
	 * @param path path to save
	 * @param array name of the array which contains correct values
	 * @param section section which has array 
	 */
	protected void savePath(String path, String array, IDialogSettings section) {
		savePath(path, array, section, false);
	}

	/**
	 * Saves given path to correct section in dialog_settings.xml
	 * @param path path to save
	 * @param array name of the array which contains correct values
	 * @param section section which has array 
	 * @param saveJustOne if true, only 'path' is saved. If false, 'path' is added to previously used paths.
	 */
	protected void savePath(String path, String array, IDialogSettings section, boolean saveJustOne) {
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
	 * Returns wanted section
	 * @param section name of the wanted section
	 * @return wanted section
	 */
	protected IDialogSettings getSection(String section) {
		IDialogSettings retVal = null;
		if (CrashAnalyserPlugin.getDefault().getDialogSettings() != null) {
			retVal = CrashAnalyserPlugin.getDefault().getDialogSettings().getSection(section);
			if (retVal == null) {
				retVal = CrashAnalyserPlugin.getDefault().getDialogSettings().addNewSection(section);
			}
		}
		return retVal;
	}	
	
	public static IDialogSettings getParameterFilesSection() {
		IDialogSettings retVal = null;
		if (CrashAnalyserPlugin.getDefault().getDialogSettings() != null) {
			retVal = CrashAnalyserPlugin.getDefault().getDialogSettings().getSection(SECTION_PARAMETER_FILES);
			if (retVal == null) {
				retVal = CrashAnalyserPlugin.getDefault().getDialogSettings().addNewSection(SECTION_PARAMETER_FILES);
			}
		}
		return retVal;
	}
}

