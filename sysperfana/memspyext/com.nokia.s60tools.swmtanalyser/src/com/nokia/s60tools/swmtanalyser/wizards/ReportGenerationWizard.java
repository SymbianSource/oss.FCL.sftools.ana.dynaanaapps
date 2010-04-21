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
package com.nokia.s60tools.swmtanalyser.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.ui.wizards.S60ToolsWizard;

/**
 * Report Generation Wizard
 *
 */
public class ReportGenerationWizard extends S60ToolsWizard {

	//Comments page
	private CommentsPage comments_page;
	//Overview object to write overview info to pdf
	private OverviewData ov;
	//ROM Checksum
	private String rom_checkSum;
	//ROM Version
	private String rom_version;
	//Tree object from Analysis view
	private Tree issues_tree;
	//PDF file name
	private String fileName;
	
	/**
	 * Constructor
	 * @param ov Overview Data Object
	 * @param checksum ROM Checksum string
	 * @param version ROM Version string
	 * @param issues_tree Tree object from the Analysis view.
	 */
	public ReportGenerationWizard(OverviewData ov, String checksum, String version, Tree issues_tree) {
		setWindowTitle("Create Report");
		this.ov = ov;
		this.rom_checkSum = checksum;
		this.rom_version = version;
		this.issues_tree = issues_tree;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizard#addPages()
	 */
	public void addPages() {
		comments_page = new CommentsPage("Comments", issues_tree);
		addPage(comments_page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
	
		fileName = comments_page.getFileName();
		String comment = comments_page.getComments();
		boolean isOverviewReport = comments_page.isOverviewReportSelected();
		ReportCreationJob engine = new ReportCreationJob("Creating report", fileName, comment, ov, this.rom_checkSum, this.rom_version, issues_tree, isOverviewReport);
		engine.setUser(true);
		engine.schedule();
		
		//Ask user whether to open the created pdf file or not
		Runnable p = new Runnable(){
		public void run() {
				if(MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),"Confirmation","Would you like to open the saved report?"))  //$NON-NLS-2$
				{
					Program p=Program.findProgram(".pdf"); 
					if(p!=null)
						p.execute(fileName);
				}	
			}					
		};
		Display.getDefault().asyncExec(p);		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		return comments_page.checkForCompletion();
	}
	
}
