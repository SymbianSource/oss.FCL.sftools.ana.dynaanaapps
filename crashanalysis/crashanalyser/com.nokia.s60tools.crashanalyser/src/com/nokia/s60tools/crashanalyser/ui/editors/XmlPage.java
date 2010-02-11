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

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;

/**
 * XML page in Crash Visualiser editor. 
 *
 */
public class XmlPage {

	Browser browserXml;
	SummaryFile crashFile = null;

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
}
