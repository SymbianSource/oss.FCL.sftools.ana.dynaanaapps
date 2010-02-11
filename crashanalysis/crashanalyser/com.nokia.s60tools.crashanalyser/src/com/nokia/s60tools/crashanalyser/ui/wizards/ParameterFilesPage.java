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

package com.nokia.s60tools.crashanalyser.ui.wizards;

import com.nokia.s60tools.ui.wizards.DebugMetadataWizardPage;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.sdk.SdkUtils;
import java.util.HashMap;

/**
 * This 3rd wizard page will query the user for locations of symbol files,
 * map files and image files.
 *
 */
public class ParameterFilesPage extends DebugMetadataWizardPage {
	DecoderEngine engine;
	boolean decodingWizard;
	
	public ParameterFilesPage(DecoderEngine decEng, boolean decodeWizard){
		super("",
				"Define Decoding Preferences.",
				true,
				UserEnteredData.getParameterFilesSection(),
				UserEnteredData.MAX_SAVED_VALUES);
			
		engine = decEng;
		decodingWizard = decodeWizard;
	 }
	
	public boolean folderContainsMapFiles(String folder) {
		return SdkUtils.folderContainsMapFiles(folder);
	}
	
	public boolean zipContainsMapFiles(String path) {
		return SdkUtils.zipContainsMapFiles(path);
	}

	public String getHelpContext() {
		if (decodingWizard)
			return HelpContextIDs.CRASH_ANALYSER_HELP_DECODE_CRASH_FILES;
		else
			return HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES;
	}
	
	public boolean canFinish()  {
		return canProceed();
	}
	
	public HashMap<String, String> getSdkMapFolders() {
		return SdkUtils.getSdkMapFileFolders(true);
	}
	
	public boolean canFlipToNextPage() {
		return false;
	}
}
