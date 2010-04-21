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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.nokia.s60tools.memspy.model.CategoryProfile;
import com.nokia.s60tools.memspy.model.SWMTCategorys;
import com.nokia.s60tools.memspy.plugin.MemSpyPlugin;
import com.nokia.s60tools.memspy.preferences.MemSpyPreferences;
import com.nokia.s60tools.memspy.ui.dialogs.SWMTCategoriesDialog;
import com.nokia.s60tools.ui.AbstractUIComposite;
import com.nokia.s60tools.util.debug.DbgUtility;


/**
 * This interface is used to notify category selection changes
 */
interface SWMTCategorySelectionMediator{
	
	/**
	 * Gets category selection as bitwise OR:ed integer.
	 * @return category selection
	 */
	public int getCategorySelection();
	
	/**
	 * Sets new category selection
	 * @param newCategorySelection new category selection as bitwise OR:ed integer
	 * @param isProfileSettings <code>true</code> if these settings are profile settings
	 * <code>false</code> if these are custom settings
	 */
	public void setCategorySelection(int newCategorySelection, boolean isProfileSettings);
	
	/**
	 * Set Categories button selection to "All" or "Custom".
	 * @param isProfileCategoriesSelected <code>true</code> if one profile is selected
	 * <code>false</code> otherwise.
	 */
	public void setProfileTrackedCategoriesSelected(boolean isProfileTrackedCategoriesSelected);
	
	/**
	 * Get if user has been selected to use one of the profiles Categories
	 * @return <code>true</code> if one profile is selected 
	 * <code>false</code> if custom categories is selected.
	 */
	public boolean isProfileTrackedCategoriesSelected();	
};


/**
 * Composite for SWMT category selection group in SWMTLogPage wizard page.
 */
public class SWMTCategoryGroupComposite extends AbstractUIComposite implements SelectionListener {

	//
	// Private constants
	//
	private static final int COMPOSITE_COLUMN_COUNT = 1;
	private static final int TRACKED_CATEGORIES_GROUP_COLUMN_COUNT = 2;
	//
	// Private member data
	//
	private Button profileCategoriesRadioBtn;
	private Button customCategoriesRadioBtn;
	private Button editCategoriesPushButton;
	private boolean isCustomCategorySelected = false;
	private final SWMTCategorySelectionMediator mediator;
	private boolean isCustomCategorySelectionEnabled = true;
	private Combo profileCombo;

	/**
	 * Constructor.
	 * @param parentComposite parent composite
	 * @param isCustomCategorySelected <code>true</code> if custom category is initially selected, otherwise <code>false</code>
	 * @param mediator mediator for handling category selection changes
	 * @param isCustomCategorySelectionEnabled <code>true</code> if custom category selection is enabled, otherwise <code>false</code>.
	 */
	public SWMTCategoryGroupComposite(Composite parentComposite, boolean isCustomCategorySelected, SWMTCategorySelectionMediator mediator, boolean isCustomCategorySelectionEnabled) {
		super(parentComposite);
		this.isCustomCategorySelected = isCustomCategorySelected;
		this.mediator = mediator;
		this.isCustomCategorySelectionEnabled = isCustomCategorySelectionEnabled;
		setCustomCategorySelection();
		// Updating widget UI state based on the provided constructor parameters
		setWidgetStates();
	}

	/**
	 * Sets custom category selection based on the feature availability
	 * and the current selection data.
	 */
	private void setCustomCategorySelection() {
		boolean isAllTrackedCategoriesSelected = mediator.isProfileTrackedCategoriesSelected();		
		if(isCustomCategorySelectionEnabled && !isAllTrackedCategoriesSelected){
			// If feature is enabled and there is pre-selected values => custom category is selected
			isCustomCategorySelected = true;
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.AbstractUIComposite#createControls()
	 */
	@Override
	protected void createControls() {
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + ": createControls()"); //$NON-NLS-1$
		
		// Tracked Categories -group
		Group categoryGroup = new Group(this, SWT.SHADOW_NONE);
		categoryGroup.setText("Tracked Categories and Advanced Options");
		GridLayout gdl = new GridLayout(TRACKED_CATEGORIES_GROUP_COLUMN_COUNT, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		categoryGroup.setLayout(gdl);
		categoryGroup.setLayoutData(gd);
		
		Composite profileCom = new Composite(categoryGroup,SWT.NONE);
		GridLayout pgl = new GridLayout(2, false);
		pgl.marginHeight = 0;
		pgl.marginWidth = 0;
		GridData pgd = new GridData(GridData.FILL_BOTH);
		pgd.horizontalSpan = 2;
		pgd.grabExcessHorizontalSpace = true;
		pgd.grabExcessVerticalSpace = true;
		profileCom.setLayout(pgl);
		profileCom.setLayoutData(pgd);
		
		//
		// Tracked Categories -group contents
		//
		
		boolean isProfileSelected = MemSpyPreferences.isProfileTrackedCategoriesSelected();

		//Profiles button
		profileCategoriesRadioBtn = new Button(profileCom, SWT.RADIO);
		profileCategoriesRadioBtn.addSelectionListener(this);
		profileCategoriesRadioBtn.setSelection(isProfileSelected);
		
		profileCombo = new Combo( profileCom, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		profileCombo.addSelectionListener(this);
		
		List<CategoryProfile> categoryProfiles = SWMTCategorys.getInstance().getCategoryProfiles();
		for (Iterator<CategoryProfile> iterator = categoryProfiles.iterator(); iterator
				.hasNext();) {
			CategoryProfile profile = (CategoryProfile) iterator.next();
			profileCombo.add(profile.getName());			
		}

		
		// Custom -radio button
		customCategoriesRadioBtn = new Button(categoryGroup, SWT.RADIO);
		customCategoriesRadioBtn.setText("Custom Categories and Advanced Options");
		customCategoriesRadioBtn.addSelectionListener(this);
		customCategoriesRadioBtn.setSelection(!isProfileSelected);
		
		// Edit -push button
		editCategoriesPushButton = new Button(categoryGroup, SWT.PUSH);
		editCategoriesPushButton.setText("Edit...");
		editCategoriesPushButton.addSelectionListener(this);		
	}

	/**
	 * Sets selection and enabled disable states for the widgets
	 */
	private void setWidgetStates() {
		
		customCategoriesRadioBtn.setEnabled(true);
		profileCategoriesRadioBtn.setEnabled(true);
		profileCombo.setEnabled(true);
		
		if(isCustomCategorySelected){
			profileCombo.setEnabled(false);
			profileCategoriesRadioBtn.setSelection(false);
			customCategoriesRadioBtn.setSelection(true);
			editCategoriesPushButton.setEnabled(true);
		}
		else{
			profileCombo.setEnabled(true);			
			profileCategoriesRadioBtn.setSelection(true);
			customCategoriesRadioBtn.setSelection(false);
			editCategoriesPushButton.setEnabled(false);			
		}
		setProfileComboSelectionAndTooltipText();
		
		if(!isCustomCategorySelectionEnabled){
			disableControls();
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.AbstractUIComposite#createLayout()
	 */
	@Override
	protected Layout createLayout() {
		return new GridLayout(COMPOSITE_COLUMN_COUNT, false);
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.ui.AbstractUIComposite#createLayoutData()
	 */
	@Override
	protected Object createLayoutData() {
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.verticalSpan = 2;
		return 	gridData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// Must be implemented but not needed in this case		
	}

	/**
	 * Sets selection for profile Combo and sets tooltip text
	 */
	private void setProfileComboSelectionAndTooltipText() {
		int selectedProfile = MemSpyPreferences.getSWMTCategorySettingForProfile();

		List<CategoryProfile> categoryProfiles = SWMTCategorys.getInstance()
				.getCategoryProfiles();
		int j = 0;
		for (Iterator<CategoryProfile> iterator = categoryProfiles.iterator(); iterator
				.hasNext();) {
			CategoryProfile profile = (CategoryProfile) iterator.next();
			if (selectedProfile == profile.getCategories()) {
				//Selection of combo
				profileCombo.select(j);
				//Tootip text for combo selection 
				String categoryNames = "";
				String[] profileCategoryNames = profile.getCategoryEntryNames();
				//Collection names of all categories belongs to profile as comma separated list (but last with "and")
				for (int i = 0; i < profileCategoryNames.length; i++) {
					categoryNames += profileCategoryNames[i];
					if (i == profileCategoryNames.length - 2) {
						categoryNames += " and ";
					} else if (i != profileCategoryNames.length - 1) {
						categoryNames += ", ";
					}
					// else its last item and we dont need to add anything
				}
				String name = profile.getName();
				//To show "&" char in tooltip, it must be replaced with "&&"
				name = name.replace("&", "&&");
				String toolTipText = "Profile: (" + name
						+ ") contains following categories: " + categoryNames;
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, toolTipText);
				profileCombo.setToolTipText(toolTipText);
				break;
			}

			j++;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Widget widget = e.widget;

		if(widget.equals(profileCategoriesRadioBtn)){
			isCustomCategorySelected = false;
			mediator.setProfileTrackedCategoriesSelected(true);
			setWidgetStates();
		}
		else if(widget.equals(customCategoriesRadioBtn)){
			isCustomCategorySelected = true;
			mediator.setProfileTrackedCategoriesSelected(false);
			setWidgetStates();
		}
		else if(widget.equals(editCategoriesPushButton)){
			categoriesEditButtonPressed();
		}		
		else if(widget.equals(profileCombo)){
			String selectedProfile = profileCombo.getText();
			CategoryProfile profile = SWMTCategorys.getInstance().getProfile(selectedProfile);
			mediator.setCategorySelection(profile.getCategories(), true);
			setProfileComboSelectionAndTooltipText();
		}		
	}
	

	
	/**
	 * Checks if custom category set has been selected by a user.
	 * @return <code>true</code> if custom category has been selected, otherwise <code>false</code>.
	 */
	public boolean isCustomCategorySelected() {
		return isCustomCategorySelected;
	}

	/**
	 * Handles Edit...-button press event.
	 */
	private void categoriesEditButtonPressed() {
		Shell sh = MemSpyPlugin.getCurrentlyActiveWbWindowShell();
		SWMTCategoriesDialog entryDialog = new SWMTCategoriesDialog(sh, mediator.getCategorySelection());
		entryDialog.create();
		int userSelection = entryDialog.open();
		if(userSelection == Window.OK){
			int newCategorySelection = entryDialog.getSelectedCategories();
			mediator.setCategorySelection(newCategorySelection, false);
		}
	}	

	/**
	 * Disables custom category selection programmatically and updates UI accordingly
	 */
	public void disableCustomCategorySelection(){
		isCustomCategorySelectionEnabled  = false;
		isCustomCategorySelected = false;
		setWidgetStates();
	}

	/**
	 * Sets enable state false to the controls.
	 */
	private void disableControls() {
		// Disabling custom category selection 
		setEnabled(false);
		profileCategoriesRadioBtn.setEnabled(false);
		customCategoriesRadioBtn.setEnabled(false);
		editCategoriesPushButton.setEnabled(false);
		profileCombo.setEnabled(false);
	}
	
	/**
	 * Refreshes widget state
	 */
	public void refresh(){
		setWidgetStates();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose(){
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + ": dispose()"); //$NON-NLS-1$
		super.dispose();
	}
	
	/**
	 * Sets buttons enabled/disabled
	 * @param isButtonsEnabled set <code>true</code> if buttons are enabled, and <code>false</code>
	 * if buttons are disabled
	 */
	public void setButtonsEnabled(boolean isButtonsEnabled){
		if(isButtonsEnabled){
			setWidgetStates();
		}else{
			disableControls();
		}
	}
	
}
