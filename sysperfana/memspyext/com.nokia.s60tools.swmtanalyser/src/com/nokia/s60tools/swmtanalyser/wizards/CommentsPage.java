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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.swmtanalyser.resources.HelpContextIds;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;
/**
 * First page in the Report Generation wizard. i.e. Comments page.
 *
 */
public class CommentsPage extends S60ToolsWizardPage implements	SelectionListener, ModifyListener {

	//Radio button for creating type1 report
	private Button type1_radio;
	//Radio button for creating overview type report
	private Button type2_radio; 
	//Save as button to select the pdf file path/name
	private Button browse_btn;
	//Text box to show the selected path
	private Text path_txt;
	//Text box to enetr comments
	private Text commentsText;
	//Label to explain about the selected radio option
	private Label info;
	
	//Temporary variables used
	private Tree all_tree_items;
	private boolean checked = false;
	
	/**
	 * Create a comments page
	 * @param pageName
	 * @param all_tree_items
	 */
	protected CommentsPage(String pageName, Tree all_tree_items) {
		super(pageName);
		setTitle("Report Options");
		setDescription("Select your options");
		this.all_tree_items = all_tree_items;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#recalculateButtonStates()
	 */
	public void recalculateButtonStates() {

	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#setInitialFocus()
	 */
	public void setInitialFocus() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == browse_btn)
		{
			FileDialog dlg = new FileDialog(this.getShell(), SWT.SAVE);
			dlg.setFilterExtensions(new String[]{"*.pdf"});
			String path  = dlg.open();
			if(path != null)
				path_txt.setText(path);
		}
		else if(e.widget == type1_radio)
		{
			//If option 1 is selected, explain about that option.
			info.setText("This case the report contains only the information about the selected issues.\nAnd graph will be shown for the selected issues.");
		}
		else if(e.widget == type2_radio)
		{
			//If option 2 is selected, explain about that option.
			info.setText("This case the report contains the overview information of all the type of issues.");
		}
	
		checkForCompletion();
		this.getContainer().updateButtons();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent arg0) {
		checkForCompletion();
		this.getContainer().updateButtons();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		
		Label title = new Label(parentComposite, SWT.WRAP);
		title.setText("Select type of report to be created:");
		title.setLayoutData(gd);
		
		type1_radio = new Button(parentComposite, SWT.RADIO);
		type1_radio.setText("Create report for the selected issues");
		type1_radio.setLayoutData(gd);
		type1_radio.setToolTipText("This case the report contains only the information about the selected issues.\nAnd graph will be shown for the selected issues.");
		type1_radio.addSelectionListener(this);
		
		type2_radio = new Button(parentComposite, SWT.RADIO);
		type2_radio.setText("Create overview report");
		type2_radio.setLayoutData(gd);
		type2_radio.setToolTipText("This case the report contains the overview information of all the type of issues");
		type2_radio.addSelectionListener(this);
		
		info=new Label(parentComposite,SWT.WRAP);
		info.setText("This case the report contains only the information about the selected issues.\nAnd graph will be shown for the selected issues.");
		GridData lblGD=new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		lblGD.horizontalSpan = 2;
		lblGD.verticalIndent=8;
		info.setLayoutData(lblGD);
		
		Label comments_label = new Label(parentComposite, SWT.WRAP);
		comments_label.setText("Enter your comments here:");
		GridData lbl_gd = new GridData(GridData.FILL_HORIZONTAL);
		lbl_gd.horizontalSpan = 2;
		comments_label.setLayoutData(lbl_gd);
		
		commentsText = new Text(parentComposite, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		GridData txt_gd = new GridData(GridData.FILL_HORIZONTAL);
		txt_gd.heightHint = 100;
		txt_gd.horizontalSpan = 2;
		commentsText.setLayoutData(txt_gd);
		commentsText.addModifyListener(this);
		
		Label browse_label = new Label(parentComposite, SWT.WRAP);
		browse_label.setText("Provide report file path here:");
		browse_label.setLayoutData(lbl_gd);
		
		path_txt = new Text(parentComposite, SWT.BORDER);
		path_txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		path_txt.addModifyListener(this);
		
		browse_btn = new Button(parentComposite, SWT.PUSH);
		browse_btn.setText("Save as...");
		browse_btn.addSelectionListener(this);
		
		setHelp(parentComposite);
		setControl(parentComposite);
	}
	
	/**
	 * Check if overview report was selected or not.
	 * @return <code>true</code> if overview report was selected, <code>false</code> otherwise.
	 */
	public boolean isOverviewReportSelected()
	{
		return type2_radio.getSelection();
	}
	
	/**
	 * Returns filename with path provided in the text box
	 * @return file name
	 */
	public String getFileName()
	{
		return path_txt.getText();
	}
	
	/**
	 * Returns comments provided. 
	 * @return comment text
	 */
	public String getComments()
	{
		return commentsText.getText();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		checkForCompletion();
		return super.canFlipToNextPage();
	}
	
	/**
	 * Checks wizard completion.
	 * @return true if completed.
	 */
	public boolean checkForCompletion(){
		setErrorMessage(null);
		if(type1_radio.getSelection() && !areItemsChecked())
		{
			setErrorMessage("No issues are selected. Please select some issues in the analysis view.");
			return false;
		}
		else if(commentsText.getText()==null || commentsText.getText() == "")
		{
			setErrorMessage("Enter your comments");
			return false;
		}
		else if(!(new File(path_txt.getText()).isAbsolute())|| !(new File(path_txt.getText())).getParentFile().exists())
		{
			setErrorMessage("Invalid file name");
			return false;
		}
		return true;
	}
	
	/**
	 * Checks whether any issues are selected or not.
	 * @return true if any issue(child) is selected.
	 */
	private boolean areItemsChecked() {
		checked = false;
		//To avoid invalid thread access, running in new thread.
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for(TreeItem i:all_tree_items.getItems())
				{
					if(i.getItemCount() > 0)
					{
						for(TreeItem child : i.getItems())
						{
							if(child.getChecked())
							{	
								checked = true;
								return;
							}
						}
					}
				}
			}});
		return checked;
	}
	
	/**
	 * Set context sensitive helps
	 * @param parentComposite
	 */
	private void setHelp(Composite parentComposite)
	{
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parentComposite,
					HelpContextIds.SWMT_REPORT_WIZARD_HELP);
			
	}
}
