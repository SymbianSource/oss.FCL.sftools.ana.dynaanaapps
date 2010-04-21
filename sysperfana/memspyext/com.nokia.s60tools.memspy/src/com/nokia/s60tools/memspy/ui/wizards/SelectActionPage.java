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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.memspy.model.UserEnteredData;
import com.nokia.s60tools.memspy.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

public class SelectActionPage extends S60ToolsWizardPage{

	// Available actions
    public enum MemSpyAction{IMPORT_HEAP, COMPARE_HEAPS, SWMT}
	
    // UI components
	public Button importHeapRadioButton;
	public Button compareTwoHeapsRadioButton;
	public Button swmtRadioButton;
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.s60tools.ui.wizards.S60ToolsWizardPage#setInitialFocus()
	 */
    public void setInitialFocus() {
	}

    public void recalculateButtonStates() {
	}


    protected SelectActionPage(String pageName) {
              super(pageName);
              setTitle("MemSpy Import Wizard, First step");
              setDescription("Select action you wish to perform with MemSpy.");
    }
    public void createControl(Composite parent) {
 		// Radio button group
 		
    	Composite composite = new Composite(parent, SWT.NULL);

 		// create the desired layout for this wizard page
 		GridLayout gl = new GridLayout();
 		gl.numColumns = 1;
 		composite.setLayout(gl);
    	
 		// Radio Button Group
 		GridLayout radioButtonGroupGridLayout = new GridLayout();
 		Group radioButtonGroup = new Group (composite, SWT.NONE);
 		radioButtonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
 		radioButtonGroup.setText("Select Action:");
 		radioButtonGroup.setLayout(radioButtonGroupGridLayout);
 		GridData radioButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
 		radioButtonGridData.horizontalSpan = 2;
 		
 		// Import Heap wizard radio button
 		importHeapRadioButton = new Button(radioButtonGroup, SWT.RADIO);
 		importHeapRadioButton.setText("Import heap and analyse log with Heap Analyser");
 		importHeapRadioButton.setLayoutData(radioButtonGridData);
 		
 		// Compare Two Heaps radio button
 		compareTwoHeapsRadioButton = new Button(radioButtonGroup, SWT.RADIO);
 		compareTwoHeapsRadioButton.setText("Import heaps and compare logs with Heap Analyser");
 		compareTwoHeapsRadioButton.setLayoutData(radioButtonGridData);
 		
 		// SWMT wizard radio button
 		swmtRadioButton = new Button(radioButtonGroup, SWT.RADIO);
 		swmtRadioButton.setText("Import System Wide Memory Tracking logs and analyse them with SWMT Analyser");
 		swmtRadioButton.setLayoutData(radioButtonGridData);
 		
 		// restore previous value of radio button.
 		// if last value is not found, set selection to import heap
 		
 		UserEnteredData data = new UserEnteredData();
		int lastUsed = data.getPreviousRadioButtonSelection(ValueTypes.SELECT_ACTION);
		
		if( lastUsed == 2 ){
			compareTwoHeapsRadioButton.setSelection(true);
		}
		else if( lastUsed == 3 ){
			swmtRadioButton.setSelection(true);
		}
		else {
			importHeapRadioButton.setSelection(true);
		}
		
		setHelps();
		setInitialFocus();
		setControl(composite);
     }
  
     /**
      * getAction
      * @return currently selected action
      */
    public MemSpyAction getAction(){
    	if( importHeapRadioButton.getSelection() ){
    		return MemSpyAction.IMPORT_HEAP;
    	}
    	else if( compareTwoHeapsRadioButton.getSelection() ){
    		return MemSpyAction.COMPARE_HEAPS;
    	}
    	else{
    		return MemSpyAction.SWMT;
    	} 
     
    }
     
     
    /**
     * saves user entered data from UI components so that it can be restored later.
     */
 	public void saveUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
		
		// Save Action radio-buttons state
		if( compareTwoHeapsRadioButton.getSelection() ){
			data.saveRadioButtonSelection(ValueTypes.SELECT_ACTION, 2);
		}
		else if( swmtRadioButton.getSelection() ){
			data.saveRadioButtonSelection(ValueTypes.SELECT_ACTION, 3);
		}
		else {
			data.saveRadioButtonSelection(ValueTypes.SELECT_ACTION, 1);
		}
 	}
 
 	/**
 	 * canFlipPreviousPage
 	 * returns always false because this is first page of wizard.
 	 */
	public boolean canFlipPreviousPage(){
		return false;
	}
	
	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp( importHeapRadioButton, HelpContextIDs.MEMSPY_SELECT_ACTION);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( compareTwoHeapsRadioButton, HelpContextIDs.MEMSPY_SELECT_ACTION);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( swmtRadioButton, HelpContextIDs.MEMSPY_SELECT_ACTION);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( this.getShell(), HelpContextIDs.MEMSPY_SELECT_ACTION);

	}
	
}

