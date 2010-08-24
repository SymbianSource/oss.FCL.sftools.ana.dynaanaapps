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

package com.nokia.s60tools.crashanalyser.ui.editors;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.corecomponents.plugin.CrashAnalyserCoreComponentsPlugin;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.model.FileOperations;
import com.nokia.s60tools.crashanalyser.plugin.CrashAnalyserPlugin;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;

/**
 * XML page in Crash Visualiser editor. 
 *
 */
public class XmlPage {

	private static final String CRASHXML_DTD = "MobileCrashXmlSchema.dtd";
	private Browser browserXml;
	private SummaryFile crashFile = null;

	/**
	 * Creates the page
	 * @param parent composite
	 * @param file summary file
	 * @return composite
	 */
	public Composite createPage(Composite parent, SummaryFile file) {
		crashFile = file;
		return doCreatePage(parent);
	}
	
	/**
	 * Creates the page
	 * @param parent composite
	 * @return composite
	 */
	public Composite createPage(Composite parent) {
		return doCreatePage(parent);
	}
	
	/**
	 * Creates all UI elements to the page
	 * @param parent
	 * @return composite
	 */
	Composite doCreatePage(Composite parent) {
		copyDtd();
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		browserXml = new Browser(parent, SWT.NONE);
		if (crashFile != null)
			browserXml.setUrl(crashFile.getFilePath());
		
		browserXml.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserXml,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);	
		
		return parent;
	}
	
	/**
	 * Loads data from given file into UI elements.
	 * @param file crash file
	 */
	public void setFile(CrashFile file) {
		if (file != null) {
			crashFile = file;
			browserXml.setUrl(crashFile.getFilePath());
		}
	}
	
	/**
	 * Copy the DTD file for crashxml. 
	 * Original location is com.nokia.s60tools.crashanalyser.corecomponents\data
	 * and it will be copied to Carbide workspace
	 * .metadata\.plugins\com.nokia.s60tools.crashanalyser\
	 * 
	 */
	private static final void copyDtd() {
		String fileName = FileOperations.addSlashToEnd(CrashAnalyserCoreComponentsPlugin.getDataPath()) +
			CRASHXML_DTD;
		String destinationPath = Platform.getStateLocation(CrashAnalyserPlugin
				.getDefault().getBundle()).toOSString();
 
		FileOperations.copyFile(new File(fileName), destinationPath, false);
	}
	
}
