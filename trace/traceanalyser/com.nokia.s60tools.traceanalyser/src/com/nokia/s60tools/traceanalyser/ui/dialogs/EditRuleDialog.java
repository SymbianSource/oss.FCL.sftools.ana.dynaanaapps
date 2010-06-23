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


package com.nokia.s60tools.traceanalyser.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleType;
import com.nokia.s60tools.traceanalyser.export.ITraceAnalyserRuleTypeListener;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.model.Engine;
import com.nokia.s60tools.traceanalyser.resources.ImageKeys;
import com.nokia.s60tools.traceanalyser.resources.ImageResourceManager;


/**
 * class Edit rule dialog
 * Dialog that can be used for editing rule's definitions.
 */
public class EditRuleDialog extends TitleAreaDialog implements SelectionListener, ModifyListener, ITraceAnalyserRuleTypeListener {
		
	Composite contents;
	
	/* UI-Components */
	private Group groupConstComponents;
	
	/* Rule Type related components */
	private Composite compositeRuleType;
	private Label labelRuleType;
	private Combo comboRuleType;

	/* Rule Name related components */
	private Composite compositeRuleName;
	private Label labelRuleName;
	private Text textRuleName;

	/* Rule Description relates components */
	private Composite compositeRuleDescription;
	private Label labelRuleDescription;
	private Text textRuleDescription;
	
	/* Rule type that is currently opened */
	private ITraceAnalyserRuleType shownRule;
	
	/* All rule types */
	private ArrayList<ITraceAnalyserRuleType> rules;
	
	/* Group for Rule plugins own UI-components */
	private Group groupAdditionalComponents;
	
	/* Rule that is used when formatting values to ui-components.*/ 
	private TraceAnalyserRule rule;
	
	private Engine engine;
	
	private boolean createNewRule = false;
	
	private String oldRuleName = null;
	
	/**
	 * TraceSelectionDialog.
	 * Constructor.
	 * @param parentShell
	 */
	public EditRuleDialog(Shell parentShell, ArrayList<ITraceAnalyserRuleType> rules, TraceAnalyserRule rule, Engine engine, boolean createNewRule) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
		this.rules = rules;
		this.rule = rule;
		this.engine = engine;
		this.createNewRule = createNewRule;
		if(createNewRule == false){
			oldRuleName = rule.getName();
		}
	}
	

	@Override
	/**
	 * createDialogArea.
	 * Method that places ui components into give composite.
	 */
	protected Control createDialogArea(Composite parent) {

		getShell().setText("Edit Rule");
		setTitle("Edit Rule Definitions");

		setTitleImage(ImageResourceManager.getImage(ImageKeys.IMG_TRACE_ANALYSER_BANNER));
		
		// Set the minimum size for dialog
		getShell().setMinimumSize(new Point( 550, 500));

		// create composite where all components are placed
		contents = new Composite(parent, SWT.NONE);
 		GridLayout contentsLayout = new GridLayout();
 		contentsLayout.numColumns = 1;
 		contents.setLayout(contentsLayout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
		// create group for components that are present for every rule type.
		groupConstComponents = new Group(contents, SWT.NONE);
		GridLayout layoutGroupConstComponents = new GridLayout();
 		layoutGroupConstComponents.numColumns = 1;
 		groupConstComponents.setLayout(layoutGroupConstComponents);
		GridData gridDataGroupConstComponents = new GridData(GridData.FILL_HORIZONTAL);
		groupConstComponents.setLayoutData(gridDataGroupConstComponents);
		groupConstComponents.setText("Basic Settings");
		
		
		// create ui components that are present for every rule type.
		this.createConstComponents(groupConstComponents);
		
		// create Group for additional components
		groupAdditionalComponents = new Group(contents, SWT.NONE);
 		GridLayout layoutGroupAdditionalComponents = new GridLayout();
 		layoutGroupAdditionalComponents.numColumns = 1;
 		groupAdditionalComponents.setLayout(layoutGroupAdditionalComponents);
 		groupAdditionalComponents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		groupAdditionalComponents.setText("Rule type specific settings");

 		
 		// Add all additional rule type's ui components into Group groupAdditionalComponents
 		for( ITraceAnalyserRuleType item : rules ){
 			item.createUIComponents(groupAdditionalComponents, this);
 			// hide components.
 			item.setVisible(false);
 			
 		}
 					
 		// if editing existing rule, format values from it to UI-components and disable rule type selection.
		if(rule != null){
			textRuleName.setText(rule.getName());
			textRuleDescription.setText(rule.getDescription());
			for(int i = 0; i < rules.size(); i++){
				if( rules.get(i).formatRuleDefinitions(rule)){
					comboRuleType.select(i);
					comboRuleType.setEnabled(false);
					break;
				}
			}
		}
		
		// hide all rule components except selected rule's components.
		this.hideAndRevealItems();
		setHelps();
		return contents;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	public void createButtonsForButtonBar( Composite composite ){
		super.createButtonsForButtonBar(composite);
		this.canFinish();
	}
	
	/**
	 * createConstComponents.
	 * Method that created UI components that are same on every rule.
	 * @param composite composite where components are placed.
	 */
	private void createConstComponents(Composite composite){
		this.createRuleTypeComposite(composite);
		this.createRuleNameComposite(composite);
		this.createRuleDescriptionComposite(composite);
	}
	
	/**
	 * createRuleTypeComposite.
	 * Method that creates all rule name related components.
	 * @param composite
	 */
	private void createRuleTypeComposite(Composite composite){
		
		// create composite for rule type components 
		compositeRuleType = new Composite(composite, SWT.NONE);
		GridLayout contentsLayout = new GridLayout();
		contentsLayout.numColumns = 2;
		compositeRuleType.setLayout(contentsLayout);
		compositeRuleType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Create rule type label
		labelRuleType = new Label(compositeRuleType, SWT.NONE);
		labelRuleType.setText("Rule Type:");
		
		// Create rule type combo
		comboRuleType = new Combo(compositeRuleType, SWT.READ_ONLY);
		GridData dataGridComboRuleType = new GridData(GridData.FILL_HORIZONTAL);
		comboRuleType.setLayoutData(dataGridComboRuleType);
		comboRuleType.addSelectionListener(this);
		
		// get rule type's names from rule array and add them to combo box.
		for(ITraceAnalyserRuleType item : rules){
			comboRuleType.add(item.getRuleType());
		}
		
		// Select first item if possible
		if( comboRuleType.getItems().length > 0 ){
			comboRuleType.select(0);
		}

		
		
	}

	/**
	 * createRuleNameComposite.
	 * Method that creates all rule name related components.
	 * @param composite composite where components are placed.
	 */
	private void createRuleNameComposite(Composite composite){

		// create composite for rule name components 
		compositeRuleName = new Composite(composite, SWT.NONE);
 		GridLayout layoutCompositeRuleName = new GridLayout();
 		layoutCompositeRuleName.numColumns = 2;
 		compositeRuleName.setLayout(layoutCompositeRuleName);
 		compositeRuleName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Create rule name label
		labelRuleName = new Label(compositeRuleName, SWT.NONE);
		labelRuleName.setText("Rule Name:");
		
		// Create rule name text field
		textRuleName = new Text(compositeRuleName, SWT.BORDER);
		GridData gridDataTextRuleName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textRuleName.setToolTipText("Define Name of the Rule.");
		textRuleName.setLayoutData(gridDataTextRuleName);
		textRuleName.addModifyListener(this);
	}
	
	/**
	 * createRuleDescriptionComposite.
	 * Method that creates all rule description related components.
	 * @param composite
	 */
	private void createRuleDescriptionComposite(Composite composite){
		// create composite for rule name components 
		compositeRuleDescription = new Composite(composite, SWT.NONE);
 		GridLayout layoutCompositeRuleDescription = new GridLayout();
 		layoutCompositeRuleDescription.numColumns = 1;
 		compositeRuleDescription.setLayout(layoutCompositeRuleDescription);
 		compositeRuleDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
		// Create rule type label
		labelRuleDescription = new Label(compositeRuleDescription, SWT.NONE);
		labelRuleDescription.setText("Rule Description(optional):");
		
		// Create rule description text field
		textRuleDescription = new Text(compositeRuleDescription, SWT.BORDER | SWT.MULTI);

		GridData gridDataTextRuleDescription = new GridData(GridData.FILL_HORIZONTAL);
		textRuleDescription.setToolTipText("Define description for rule.");
		textRuleDescription.setLayoutData(gridDataTextRuleDescription);
	}

	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// nothing to be done.
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		if( event.widget == comboRuleType ){
			this.hideAndRevealItems();
		}
		this.canFinish();
	}
	
	/**
	 * 
	 */
	private void hideAndRevealItems(){
		int selectedIndex = comboRuleType.getSelectionIndex();
		if(selectedIndex >= 0 && selectedIndex < rules.size()){
			
			if(shownRule != null){
				shownRule.setVisible(false);
			}
			shownRule = rules.get(selectedIndex);
			shownRule.setVisible(true);
		}
		
		contents.layout();
	}
	
	/**
	 * Set this page's context sensitive helps
	 */
	protected void setHelps() {
		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_RULE_EDITOR);
	}
	
	/**
	 * canFinish.
	 * Method that checks that all needed information is filled into UI-components. 
	 * Method also takes care enabling/disabling OK-button and printing error messages if needed.
	 */
	public void canFinish(){
		String message = "Some of the mandatory fields is not filled properly:\n";
		
		if(this.getButton(OK) != null){
		
			// if no rule types found, set error message and disable ok
			if( comboRuleType.getItems().length == 0 ){
		 		setMessage("No Trace Analyser rule types found from plugins-directory. Probably some parts of the Trace Analyser plug-in are missing.", org.eclipse.jface.dialogs.IMessageProvider.ERROR);
				this.getButton(OK).setEnabled(false);
				return;
			}
			
			// if name-field is empty, set warning message and disable ok-button.
			else if( textRuleName.getText().length() == 0 ){
				message += "Rule name missing";
				setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
				this.getButton(OK).setEnabled(false);
				return;
			}
			
			else if(engine.ruleExists(textRuleName.getText()) && createNewRule){
				message += "Rule with same name already exists.";
				setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
				this.getButton(OK).setEnabled(false);
				return;
			}
			
			// ask from shown rule if all needed information is present.
			else if(this.shownRule != null){
				String error = shownRule.canFinish();
				if( error != null){
					message += error;
					setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
					this.getButton(OK).setEnabled(false);
					return;
				}	
			}
			
			// clear error message and set ok enabled.
			
			setMessage("");
			this.getButton(OK).setEnabled(true);
			return;	
		}
		return;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent event) {
		if(event.widget == textRuleName){
			try{
				this.canFinish();
			}
			catch (NullPointerException e) {
				// Do nothing
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	public void okPressed(){
		if( shownRule != null){
			TraceAnalyserRule newRule = shownRule.getRule(textRuleName.getText(), textRuleDescription.getText());
			if(newRule!= null){
				
				if(createNewRule == false){
					
					// Confirm that rule user really wants to save new rule defitions.
					MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				    messageBox.setText("Trace Analyse - Edit Rule");
				    messageBox.setMessage("Editing rule will clear rule's history. Are you sure you want to save new rule definitions?");
				    int retval = messageBox.open();
				    if(retval == SWT.YES){
						if(!engine.removeRule(oldRuleName)){
							MessageDialog.openError( getShell(), "Trace Analyser - error", "An error occured when trying to save rule." );
							return;
						}

				    }
					else{ //if retval == SWT.NO
						return;
					}

						
				}
				
				if(!engine.addRule(newRule)){
					MessageDialog.openError( getShell(), "Trace Analyser - error", "Unable to save rule, perhaps rule with same name already exists?" );

				}
				this.close();	
			}
			else{
				MessageDialog.openError( getShell(), "Trace Analyser - error", "An error occured when trying to save rule." );
			}
			
		}
	}
	
	
	
}
