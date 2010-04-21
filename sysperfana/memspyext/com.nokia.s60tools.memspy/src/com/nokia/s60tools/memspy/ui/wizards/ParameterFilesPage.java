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



package com.nokia.s60tools.memspy.ui.wizards;

import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.sdk.SdkUtils;
import com.nokia.s60tools.ui.wizards.DebugMetadataWizardPage;


/**
 * class ParameterFilesPage
 * This wizard page will query the user for locations symbol and map files.
 */
public class ParameterFilesPage extends DebugMetadataWizardPage {
	
	private String[] symbolOrMapFiles;
	private String mapFileDirectory;
	
	private enum FileType{ SYMBOL, MAPFILE };

	
	public ParameterFilesPage( String[] symbolOrMapFiles, String mapFileDirectory, String descriptionText ){
		super("MemSpy", descriptionText, false, UserEnteredData.getParameterFilesSection(),
				UserEnteredData.MAX_SAVED_VALUES);
		this.symbolOrMapFiles = symbolOrMapFiles;
		this.mapFileDirectory = mapFileDirectory;
		
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		// read symbolOrMapFiles-variables values and set them to UI items.
		
		if( this.symbolOrMapFiles != null ){
			
			//Reset Symbol file list-component
			this.listSymbols.removeAll();
			
			
			// Reset ui-components values
			
			// set "No Map Files"-radio button selected.(and unselect other buttons)
			this.buttonMapFilesFolderRadio.setSelection(false);
			this.buttonMapFilesZipRadio.setSelection(false);
			this.buttonSdkFolderRadio.setSelection(false);
			this.buttonNoMapFilesRadio.setSelection(true);

			// disable combo boxes and browse buttons buttons
			this.comboMapFilesFolder.setEnabled(false);
			this.buttonMapFolderBrowse.setEnabled(false);
			this.comboMapFilesZip.setEnabled(false);
			this.buttonZipBrowse.setEnabled(false);
			this.comboSdkFolder.setEnabled(false);
			
			
			for( String item : symbolOrMapFiles ){
				if( getFileType(item) == FileType.SYMBOL ){
					listSymbols.add(item);
					
				}
				else if(getFileType(item) == FileType.MAPFILE ){
					comboMapFilesZip.setText(item);
					
					// set zip-radio button selected.(and unselect other buttons)
					this.buttonMapFilesFolderRadio.setSelection(false);
					this.buttonMapFilesZipRadio.setSelection(true);
					this.buttonSdkFolderRadio.setSelection(false);
					this.buttonNoMapFilesRadio.setSelection(false);

					// enable zip buttons
					this.comboMapFilesFolder.setEnabled(false);
					this.buttonMapFolderBrowse.setEnabled(false);
					this.comboMapFilesZip.setEnabled(true);
					this.buttonZipBrowse.setEnabled(true);
					this.comboSdkFolder.setEnabled(false);
				}
			}
					
		}
		if( this.mapFileDirectory != null ){
			comboMapFilesFolder.setText( mapFileDirectory );
			
			// set folder-radio button selected.(and unselect other buttons)
			this.buttonMapFilesFolderRadio.setSelection(true);
			this.buttonMapFilesZipRadio.setSelection(false);
			this.buttonSdkFolderRadio.setSelection(false);
			this.buttonNoMapFilesRadio.setSelection(false);
			
			// enable folder buttons
			this.comboMapFilesFolder.setEnabled(true);
			this.buttonMapFolderBrowse.setEnabled(true);
			this.comboMapFilesZip.setEnabled(false);
			this.buttonZipBrowse.setEnabled(false);
			this.comboSdkFolder.setEnabled(false);

		}
	}
	
	public boolean zipContainsMapFiles(String path) {
		return SdkUtils.zipContainsMapFiles(path);
	}

	public String getHelpContext() {
		return HelpContextIDs.MEMSPY_IMPORT_SYMBOLS;
	}
	
	public boolean canFlipToNextPage(){
		return false;
	}
	
	public boolean canFinish()  {
		return canProceed();
	}
	
	public HashMap<String, String> getSdkMapFolders() {
		return SdkUtils.getSdkMapFileFolders(true);
	}
	
	private static FileType getFileType( String fileName ){
		int index = fileName.lastIndexOf('.');
		if ( index <= 0 ){
			return FileType.SYMBOL;
		}
		else{
			String end = fileName.substring(index);
			if(end.equals(".zip")){
				return FileType.MAPFILE;
			}
			else{
				return FileType.SYMBOL;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.DebugMetadataWizardPage#folderContainsMapFiles(java.lang.String)
	 */	
	public boolean folderContainsMapFiles(String folder) {
		return SdkUtils.folderContainsMapFiles(folder);
	}
	
}
