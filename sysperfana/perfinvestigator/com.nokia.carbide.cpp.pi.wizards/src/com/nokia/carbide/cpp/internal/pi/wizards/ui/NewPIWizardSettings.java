/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgEntryList;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.RofsObySymbolPair;
import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;

/*
 *  Handles all import wizard's persist data
 */

public class NewPIWizardSettings {
	static NewPIWizardSettings instance = null;
	
	static final int COMBO_MAX_ITEM_VALUES = 5;
	
	//This means something on Windows and Unix, should be good enough for file path not having this
	static final String PKG_SEPERATOR = "<SDK>";									//$NON-NLS-1$
	static final String ROFS_SEPERATOR = "<ROFSPAIR>";								//$NON-NLS-1$
	
	static final int CURRENT_VERSION = 5;
	static final String IMPORTER_SETTING_VERSION = "version";						//$NON-NLS-1$
	static final String SAMPLEFILENAME_KEY = "sampleFileName";						//$NON-NLS-1$
	static final String ENABLE_APP_KEY = "enableApp";								//$NON-NLS-1$ 
	static final String ENABLE_ROM_KEY = "enableRom";								//$NON-NLS-1$
	static final String ENABLE_CUST_KEY = "enableCust";								//$NON-NLS-1$
	static final String SELECTED_PKG_ENTRY_KEY = "selectedPkgEntry";				//$NON-NLS-1$
	static final String UNSELECTED_PKG_ENTRY_KEY = "availablePkgEntry";				//$NON-NLS-1$
	static final String PROJECT_KEY = "project";									//$NON-NLS-1$
	static final String PROJECT_CONFIG_KEY = "projectConfig";						//$NON-NLS-1$
	static final String ROM_SDK_KEY = "romSdk";										//$NON-NLS-1$
	static final String ROM_SYMBOL_KEY = "romSymbol";								//$NON-NLS-1$
	static final String ROM_OBY_KEY = "romOby";										//$NON-NLS-1$
	static final String ROFS_SYMBOL_KEY = "rofsSymbol";								//$NON-NLS-1$
	static final String ROFS_OBY_KEY = "romRofsOby";								//$NON-NLS-1$
	static final String ROFS_OBY_SYMBOL_KEY = "romRofsObySymbol";					//$NON-NLS-1$
	static final String CUST_SAMPLE_KEY = "custSample";								//$NON-NLS-1$
	static final String CUST_VAL_BASED_KEY = "isValBased";							//$NON-NLS-1$
	static final String CUST_NAME_BASED_KEY = "isNameBased";						//$NON-NLS-1$
	static final String CUST_SEPARATOR_KEY = "separator";							//$NON-NLS-1$
	static final String OUTPUT_PROJECT_KEY = "outputProject";						//$NON-NLS-1$
	static final String OUTPUT_NPI_KEY = "piFileName";								//$NON-NLS-1$
	static final String KEY_PROFILE_KEY = "keyProfile";								//$NON-NLS-1$
	static final String SAMPLEFILENAMES_KEY = "sampleFileName";						//$NON-NLS-1$

	
//	start version 2
	String sampleFileName;
	String[] sampleFileNames;
	boolean haveAppOnly = false;
	boolean haveRomOnly = false;
	boolean haveAppRom = false;
	boolean haveNothing = false;
	boolean enableCust;
	ArrayList<IPkgEntry> selectedAppFileList = new ArrayList<IPkgEntry>();
	ArrayList<IPkgEntry> availableAppFileList = new ArrayList<IPkgEntry>();
	ArrayList<IProject> selectedProjectList = new ArrayList<IProject>();
	ArrayList<ICarbideBuildConfiguration> selectedBuildConfigList = new ArrayList<ICarbideBuildConfiguration>();
	ISymbianSDK romSdk;
	String romSymbolFile;
	String romObyFile;
	ArrayList <RofsObySymbolPair> rofsObySymbolPairList = new ArrayList<RofsObySymbolPair>();
	String custSampleFile;
	boolean custValBased;
	boolean custNameBased;
	String custSeparator;	
	IContainer outputContainer;
//	end version 2
//	start version 3
	String piFileName;
//	end version 3
//	start version 4
	IBupEventMapProfile keyMapProfile;
//	end version 4
//	start internal states not saved
	long sampleFileNameModifiedNanoTime;	// timestamp java.lang.System.nanoTime()
	long piFileNameModifiedNanoTime;		// timestamp java.lang.System.nanoTime()
//	end internal states not saved
	
	public void clear() {
		sampleFileName = "";			//$NON-NLS-1$
		haveAppOnly = false;
		haveRomOnly = false;
		haveAppRom = false;
		haveNothing = false;
		enableCust = false;
		selectedAppFileList.clear();
		availableAppFileList.clear();
		selectedProjectList.clear();
		selectedBuildConfigList.clear();
		romSdk = null;
		romSymbolFile = "";			//$NON-NLS-1$
		romObyFile = "";				//$NON-NLS-1$
		rofsObySymbolPairList.clear();
		custSampleFile = "";			//$NON-NLS-1$
		custValBased = false;
		custNameBased = false;
		custSeparator = "";			//$NON-NLS-1$	
		outputContainer = null;
		piFileName= "";				//$NON-NLS-1$
		keyMapProfile = null;
		sampleFileNameModifiedNanoTime = 0;
		piFileNameModifiedNanoTime = 0;
	}
	
	private NewPIWizardSettings() {
		clear();
	}
	
	public static NewPIWizardSettings getInstance() {
		if (instance == null)
			instance = new NewPIWizardSettings();
		return instance;
	}
		
	public void saveState(IDialogSettings dialogSettings) {
		try {
			dialogSettings.put(IMPORTER_SETTING_VERSION, CURRENT_VERSION);
			dialogSettings.put(SAMPLEFILENAME_KEY, sampleFileName);
			saveFileNameToArray();
			dialogSettings.put(SAMPLEFILENAMES_KEY, sampleFileNames);
			dialogSettings.put(ENABLE_CUST_KEY, enableCust);
			
			{	// traverse all entries of app config and pkg file
				ArrayList<String> projectStringArrayList = new ArrayList<String>();
				for (IProject selectedProject: selectedProjectList) {
					if (selectedProject.isOpen())
						projectStringArrayList.add(selectedProject.getName());
				}
				dialogSettings.put(PROJECT_KEY, projectStringArrayList.toArray(new String[projectStringArrayList.size()]));		
				
				ArrayList<String> bcStringArrayList = new ArrayList<String>();
				for (ICarbideBuildConfiguration currentConfig : selectedBuildConfigList) {
					if (currentConfig.getCarbideProject().getProject().isOpen()) {
						bcStringArrayList.add(currentConfig.getCarbideProject().getProject().getName() + PKG_SEPERATOR + currentConfig.getDisplayString());
					}
				}
				dialogSettings.put(PROJECT_CONFIG_KEY, bcStringArrayList.toArray(new String[bcStringArrayList.size()]));
				
				ArrayList<String> pkgStringArrayList = new ArrayList<String>();
				for (IPkgEntry currentPkgEntry : selectedAppFileList) {
					pkgStringArrayList.add(currentPkgEntry.getPkgFile() + PKG_SEPERATOR + currentPkgEntry.getSdk().getUniqueId());			
				}
				dialogSettings.put(SELECTED_PKG_ENTRY_KEY, pkgStringArrayList.toArray(new String[pkgStringArrayList.size()]));

				pkgStringArrayList.clear();
				for (IPkgEntry currentPkgEntry : availableAppFileList) {
					pkgStringArrayList.add(currentPkgEntry.getPkgFile() + PKG_SEPERATOR + currentPkgEntry.getSdk().getUniqueId());			
				}
				dialogSettings.put(UNSELECTED_PKG_ENTRY_KEY, pkgStringArrayList.toArray(new String[pkgStringArrayList.size()]));
			}
			if (romSdk != null) {
				dialogSettings.put(ROM_SDK_KEY, romSdk.getUniqueId());
			}
			dialogSettings.put(ROM_SYMBOL_KEY, romSymbolFile);
			dialogSettings.put(ROM_OBY_KEY, romObyFile);
			{
				ArrayList<String> rofsObySymbolStringArrayList = new ArrayList<String>();
				for (RofsObySymbolPair pair : rofsObySymbolPairList) {
					String setting = "";	//$NON-NLS-1$
					if (pair.haveObyFile()) {
						setting += pair.getObyFile();
					}
					setting += ROFS_SEPERATOR;
					if (pair.haveSymbolFile()) {
						setting += pair.getSymbolFile();
					}
					rofsObySymbolStringArrayList.add(setting);
				}
				dialogSettings.put(ROFS_OBY_SYMBOL_KEY, rofsObySymbolStringArrayList.toArray(new String[rofsObySymbolStringArrayList.size()]));
			}
			dialogSettings.put(CUST_SAMPLE_KEY, custSampleFile);
			dialogSettings.put(CUST_VAL_BASED_KEY, custValBased);
			dialogSettings.put(CUST_NAME_BASED_KEY, custNameBased);
			dialogSettings.put(CUST_SEPARATOR_KEY, custSeparator);
			String outProject = outputContainer.getFullPath().toString();
			dialogSettings.put(OUTPUT_PROJECT_KEY, outProject);
			dialogSettings.put(OUTPUT_NPI_KEY, piFileName);
			dialogSettings.put(KEY_PROFILE_KEY, keyMapProfile != null ? keyMapProfile.toString() : "");	//$NON-NLS-1$
//			 newer members go here
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void restoreState(IDialogSettings dialogSettings) {
		try {
			if (dialogSettings.get(IMPORTER_SETTING_VERSION) == null) {
				return;
			}
			if (dialogSettings.getInt(IMPORTER_SETTING_VERSION) == CURRENT_VERSION) {
				sampleFileName = dialogSettings.get(SAMPLEFILENAME_KEY);
				sampleFileNames = dialogSettings.getArray(SAMPLEFILENAMES_KEY);
//				if(!(new java.io.File(sampleFileName).exists()))
//					sampleFileName = "";	//$NON-NLS-1$
				enableCust = dialogSettings.getBoolean(ENABLE_CUST_KEY);
				
				{	// traverse all entries of app config and pkg file
					String selectedProjectStringArray[];
					String projectConfigStringArray[];
					String pkgStringArray[];
					selectedProjectStringArray = dialogSettings.getArray(PROJECT_KEY);
					selectedProjectList.clear();
					for (String selectedProjectString : selectedProjectStringArray) {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectString);
						if (project != null) {
							if (project.exists()) {
								// project in persist data match one of the project in workspace
								selectedProjectList.add(project);
							}
						}
					}
					
					projectConfigStringArray = dialogSettings.getArray(PROJECT_CONFIG_KEY);
					selectedBuildConfigList.clear();
					for (String projectConfig : projectConfigStringArray) {
						// attempt to match workspace with <project>:<config> entry in setting
						String projectConfigDetail[] = projectConfig.split(PKG_SEPERATOR);
						if (projectConfigDetail.length == 2) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectConfigDetail[0]);
							if (project != null) {
								if (project.exists() && project.isOpen()) {
									ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(project);
									for (ICarbideBuildConfiguration currentConfig : cpi.getBuildConfigurations()) {
										if (currentConfig.getDisplayString().equals(projectConfigDetail[1])) {
											// Found Carbide.c++ config matching entry
											selectedBuildConfigList.add(currentConfig);
											break;
										}
									}
								}
							}
						}
					}
					pkgStringArray = dialogSettings.getArray(SELECTED_PKG_ENTRY_KEY);
					selectedAppFileList.clear();
					for (String pkgString : pkgStringArray) {
						// attempt to match file system with <PKG file>:<SDK> entry in setting
						String pkgDetail[] = pkgString.split(PKG_SEPERATOR);
						if (pkgDetail.length == 2) {
							ISymbianSDK sdk = SDKCorePlugin.getSDKManager().getSDK(pkgDetail[1], true);
							if (new java.io.File(pkgDetail[0]).exists() && sdk != null) {
								IPkgEntry pkg = PkgEntryList.getInstance().getPkgEntry(pkgDetail[0], sdk);
								selectedAppFileList.add(pkg);
							}
						}
					}
					pkgStringArray = dialogSettings.getArray(UNSELECTED_PKG_ENTRY_KEY);
					availableAppFileList.clear();
					for (String pkgString : pkgStringArray) {
						// attempt to match file system with <PKG file>:<SDK> entry in setting
						String pkgDetail[] = pkgString.split(PKG_SEPERATOR);
						if (pkgDetail.length == 2) {
							ISymbianSDK sdk = SDKCorePlugin.getSDKManager().getSDK(pkgDetail[1], true);
							if (new java.io.File(pkgDetail[0]).exists() && sdk != null) {
								IPkgEntry pkg = PkgEntryList.getInstance().getPkgEntry(pkgDetail[0], sdk);
								availableAppFileList.add(pkg);
							}
						}
					}
				}
				
				String romSdkId = dialogSettings.get(ROM_SDK_KEY);
				if (romSdkId != null) {
					romSdk = SDKCorePlugin.getSDKManager().getSDK(romSdkId, true);
				}
				romSymbolFile = dialogSettings.get(ROM_SYMBOL_KEY);
//				if(!(new java.io.File(romSymbolFile).exists()))
//					romSymbolFile = "";		//$NON-NLS-1$
				romObyFile = dialogSettings.get(ROM_OBY_KEY);
//				if(!(new java.io.File(romObyFile).exists()))
//					romObyFile = "";		//$NON-NLS-1$
				{
					String rofsObySymbolStringArray[];
					rofsObySymbolStringArray = dialogSettings.getArray(ROFS_OBY_SYMBOL_KEY);
					rofsObySymbolPairList.clear();
					for (String rofsObySymbolString : rofsObySymbolStringArray) {
						int seperator = rofsObySymbolString.indexOf(ROFS_SEPERATOR);
						RofsObySymbolPair pair = new RofsObySymbolPair();
						pair.setObyFile(rofsObySymbolString.substring(0, seperator));
						pair.setSymbolFile(rofsObySymbolString.substring(seperator + ROFS_SEPERATOR.length()));
						rofsObySymbolPairList.add(pair);
					}
				}
				custSampleFile = dialogSettings.get(CUST_SAMPLE_KEY);
//				if(!(new java.io.File(custSampleFile).exists()))
//					custSampleFile = "";	//$NON-NLS-1$
				custValBased = dialogSettings.getBoolean(CUST_VAL_BASED_KEY);
				custNameBased = dialogSettings.getBoolean(CUST_NAME_BASED_KEY);
				custSeparator = dialogSettings.get(CUST_SEPARATOR_KEY);
				String outputFolder = dialogSettings.get(OUTPUT_PROJECT_KEY);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource resource = root.findMember(outputFolder);
				if (resource instanceof IContainer) {
					outputContainer = (IContainer) resource;
				} else {
					outputContainer = null;
				}
				piFileName = dialogSettings.get(OUTPUT_NPI_KEY);
				String keyProfileString = dialogSettings.get(KEY_PROFILE_KEY);
				ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getAllProfiles();
				keyMapProfile = null;
				for (IBupEventMapProfile profile : profiles) {
					if(profile.toString().equals(keyProfileString)) {
						keyMapProfile = profile;
					}
				}
			} // if (dialogSettings.getInt(IMPORTER_SETTING_VERSION) == CURRENT_VERSION)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * @param sampleFileName path to save
	 * @param array name of the array which contains correct values
	 * @param section section which has array 
	 */
	protected void saveFileNameToArray() {
			
		// No previous values exist
		if (sampleFileNames == null) {
			sampleFileNames = new String[1];
			sampleFileNames[0] = sampleFileName;
		// Previous values exists
		} else {
			int valuesCount = sampleFileNames.length;
			
			boolean valueExisted = false;
			// see if passed value already exist.
			for (int i = 0; i < valuesCount; i++) {
				if (sampleFileNames[i].compareToIgnoreCase(sampleFileName) == 0) {
					valueExisted = true;
					
					// passed value exists, move it to first position
					for (int j = i; j > 0; j--) {
						sampleFileNames[j] = sampleFileNames[j-1];
					}
					sampleFileNames[0] = sampleFileName;
					
					break;
				}
			}
			
			// passed value did not exist, add it to first position (and move older values "down")
			if (!valueExisted) {
				if (valuesCount >= COMBO_MAX_ITEM_VALUES) {
					for (int i = valuesCount-1; i > 0; i--) {
						sampleFileNames[i] = sampleFileNames[i-1];
					}
					sampleFileNames[0] = sampleFileName;
				} else {
					String[] values = new String[valuesCount + 1];
					values[0] = sampleFileName;
					for (int i = 0; i < valuesCount; i++) {
						values[i+1] = sampleFileNames[i];
					}
					sampleFileNames = values;
				}
			}
		}
		
	}
	
}
