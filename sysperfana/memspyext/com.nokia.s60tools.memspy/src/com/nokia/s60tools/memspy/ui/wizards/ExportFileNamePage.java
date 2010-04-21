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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

public class ExportFileNamePage extends S60ToolsWizardPage implements ModifyListener, SelectionListener{
	
	// UI-components
	private Combo fileCombo;
	private Button buttonBrowseFile;

	// Strings
	private final static String  REPORT_LOCATION_TEXT 		= "File Location";
	private final static String  BROWSE_TEXT		 		= "Browse...";
	private final static String  FILE_SELECTION_DIALOG_TEXT = "Define location for exported file.";
	private final static String  OUTPUT_FILE_TEXT 			= "Output file";
	private final static String  DESCRIPTION_TEXT 			= "Define file name for exported comparison report file(xls-file).";
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#setInitialFocus()
	 */
    public void setInitialFocus() {
    	fileCombo.setFocus();
	}

    
    
    /*
     * (non-Javadoc)
     * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#recalculateButtonStates()
     */
    public void recalculateButtonStates() {
	}
    
    /**
     * ExportFileNamePage
     * constructor
     * @param pageName name of the page
     */
    protected ExportFileNamePage(String pageName) {
              super(pageName);
              setTitle(OUTPUT_FILE_TEXT);
              setDescription(DESCRIPTION_TEXT);
    }
    
    public void createControl(Composite parent) {
 		// Radio button group
    	Composite composite = new Composite(parent, SWT.NULL);

 		// create the desired layout for this wizard page
 		GridLayout gl = new GridLayout();
 		gl.numColumns = 1;
 		composite.setLayout(gl);
    	
 		// file location group
		Group fileLocationGroup = new Group(composite, SWT.NONE);
		fileLocationGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout fileLocationLayout = new GridLayout();
		fileLocationLayout.numColumns = 2;
		fileLocationGroup.setLayout( fileLocationLayout );
		fileLocationGroup.setText( REPORT_LOCATION_TEXT );
		
		// file combo
		fileCombo = new Combo(fileLocationGroup, SWT.BORDER);
		GridData fileDataGrid = new GridData(GridData.FILL_HORIZONTAL);
		fileCombo.setLayoutData(fileDataGrid);
		fileCombo.addModifyListener(this);
		
		// browse button
		buttonBrowseFile = new Button(fileLocationGroup, SWT.PUSH);
		buttonBrowseFile.setText(BROWSE_TEXT);
		buttonBrowseFile.addSelectionListener(this);
		
		// load previous value
		this.loadUserEnteredData();

		setHelps();
		setInitialFocus();
		setControl(composite);
     }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
	public void modifyText(ModifyEvent event) {
		
		if (event.widget.equals(fileCombo)) {
			try {
				getWizard().getContainer().updateButtons();
			} catch (Exception e) {
			}
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == buttonBrowseFile) {
			// open file dialog for selecting a crash file
			FileDialog dialog = new FileDialog(this.getShell(), SWT.SAVE );
			dialog.setText(FILE_SELECTION_DIALOG_TEXT);
			String[] filterExt = { "*.xls" };
			dialog.setFilterExtensions(filterExt);
		
			dialog.setFilterPath(fileCombo.getText());
			String result = dialog.open();
			
			// add xls-end if needed
			if( !result.endsWith( ".xls" ) ){
				result += ".xls";
			}
			
			

			fileCombo.setText(result);

		}
		
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
 		if( fileCombo.getText() != "" ){
 			return true;
 		}
 		else {
 			return false;
 			
 		}
 		
	}
	
	/**
	 * loadUserEnteredData
	 * loads previous values into UI components
	 */
	private void loadUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
	
 		// Restore previous values to file combobox
		String[] lastUsedFiles = data.getPreviousValues(ValueTypes.OUTPUT_FILE);
		if (lastUsedFiles != null) {
			fileCombo.setItems(lastUsedFiles);
			fileCombo.select(0);
		}
		

		
 	}
	
	/**
	 * saveUserEnteredData
	 * Saves current user entered data from UI components
	 */
	public void saveUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
		
		// Save file combo box
		String item = fileCombo.getText();
		data.saveValue(ValueTypes.OUTPUT_FILE, item);
	}
	
	/**
	 * getOutputFileName
	 * @return output file name
	 */
	public String getOutputFileName(){
		return fileCombo.getText();
	}
	
	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp( fileCombo, HelpContextIDs.MEMSPY_IMPORT_COMPARE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( buttonBrowseFile, HelpContextIDs.MEMSPY_IMPORT_COMPARE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( buttonBrowseFile, HelpContextIDs.MEMSPY_IMPORT_COMPARE);

	}
	
	
}

