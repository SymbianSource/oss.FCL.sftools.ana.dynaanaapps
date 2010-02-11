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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.crashanalyser.files.CrashFile;
import com.nokia.s60tools.crashanalyser.files.SummaryFile;
import com.nokia.s60tools.crashanalyser.model.HtmlFormatter;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;

/**
 * Errors & Warnings page in Crash Visualiser editor. 
 *
 */
public class ErrorPage {
	SummaryFile crashFile = null;
	Browser browserMessages;
	
	/**
	 * Creates page
	 * @param parent composite
	 * @param file data file
	 * @return composite
	 */
	public Composite createPage(Composite parent, SummaryFile file) {
		crashFile = file;
		return doCreate(parent);
	}
	
	/**
	 * Creates page
	 * @param parent composite
	 * @return composite
	 */
	public Composite createPage(Composite parent) {
		return doCreate(parent);
	}
	
	/**
	 * Loads data from given file into UI elements.
	 * @param file crash file
	 */
	public void setFile(CrashFile file) {
		if (file != null) {
			crashFile = file;
			showMessages();
		}
	}
	
	/**
	 * Creates all UI elements to the page
	 * @param parent
	 * @return composite
	 */
	Composite doCreate(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		browserMessages = new Browser(parent, SWT.NONE);
		if (crashFile != null)
			showMessages();
		
		browserMessages.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserMessages,
				HelpContextIDs.CRASH_ANALYSER_HELP_CRASH_VISUALISER);			
		
		return parent;
	}
	
	/**
	 * Shows messages in UI
	 */
	void showMessages() {
		String errorsAndWarnings = HtmlFormatter.formatErrosAndWarnings(crashFile.getMessages());
		browserMessages.setText(HtmlFormatter.formatHtmlStyle(browserMessages.getFont(), errorsAndWarnings));
	}
	
}
