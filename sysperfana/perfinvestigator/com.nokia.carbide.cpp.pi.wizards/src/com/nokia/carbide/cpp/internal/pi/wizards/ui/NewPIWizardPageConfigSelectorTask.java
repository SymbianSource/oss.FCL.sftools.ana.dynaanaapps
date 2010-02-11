/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;


public class NewPIWizardPageConfigSelectorTask 
extends NewPIWizardPage
implements INewPIWizardSettings
{

	// control
	private Composite composite = null;
	private Group optionGroup = null;
	private Composite appComposite = null;
	private Composite appLabelComposite = null;
	private Button buttonApp = null;
	private Label labelAppTitle = null;
	private Label labelApp2 = null;
	private Composite romAppComposite = null;
	private Composite romAppLabelComposite = null;
	private Button buttonRomApp = null;
	private Label labelRomAppTitle = null;
	private Label labelRomApp2 = null;
	private Label labelRomApp3 = null;
	private Composite romComposite = null;
	private Composite romLabelComposite = null;
	private Button buttonRom = null;
	private Label labelRomTitle = null;
	private Label labelRom2 = null;
	private Composite noneComposite = null;
	private Composite noneLabelComposite = null;
	private Button buttonNone = null;
	private Label labelNoneTitle = null;
	private Label labelNone2 = null;
	private Label labelNone3 = null;
	@SuppressWarnings("unused") //$NON-NLS-1$
	private Group customTraceGroup = null;
	@SuppressWarnings("unused") //$NON-NLS-1$
	private Button buttonCustomTrace = null;
	@SuppressWarnings("unused") //$NON-NLS-1$
	private Label labelCustomTrace = null;

	private ICarbideSharedImages carbideImages = CarbideUIPlugin.getSharedImages();
	private Image phonensisImage = carbideImages.getImage(ICarbideSharedImages.IMG_PI_IMPORT_ROM_AND_APP_100_42);
	private Image phoneImage = carbideImages.getImage(ICarbideSharedImages.IMG_PI_IMPORT_ROM_42_42);
	private Image sisImage = carbideImages.getImage(ICarbideSharedImages.IMG_PI_IMPORT_APP_42_42);
	private Image noneImage = carbideImages.getImage(ICarbideSharedImages.IMG_PI_IMPORT_NONE_100_42);
	private Image cusTraceImage = carbideImages.getImage(ICarbideSharedImages.IMG_CUSTOM_TRACE_BADGE_24_24);

	protected NewPIWizardPageConfigSelectorTask() {
		super(Messages.getString("NewPIWizardPageConfigSelectorTask.title"));	//$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageConfigSelectorTask.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageConfigSelectorTask.description")); //$NON-NLS-1$
	}

	SelectionListener buttonListener = new SelectionListener () {
		// handle all buttons including radio behavior among toggle
		
		public void widgetDefaultSelected(SelectionEvent arg0) {
		}

		public void widgetSelected(SelectionEvent arg0) {
			buttonApp.setSelection(false);
			buttonRomApp.setSelection(false);
			buttonRom.setSelection(false);
			buttonNone.setSelection(false);
			((Button)arg0.widget).setSelection(true);
			// write back to our status
			NewPIWizardSettings.getInstance().haveAppRom = false;
			NewPIWizardSettings.getInstance().haveAppOnly = false;
			NewPIWizardSettings.getInstance().haveRomOnly = false;
			NewPIWizardSettings.getInstance().haveNothing = false;
			if (((Button)arg0.widget) == buttonRomApp) {
				NewPIWizardSettings.getInstance().haveAppRom = true;
			} else if (((Button)arg0.widget) == buttonApp) {
				NewPIWizardSettings.getInstance().haveAppOnly = true;
			} else if (((Button)arg0.widget) == buttonRom) {
				NewPIWizardSettings.getInstance().haveRomOnly = true;
			} else if (((Button)arg0.widget) == buttonNone) {
				NewPIWizardSettings.getInstance().haveNothing = true;
			}
			validatePage();
		}
	};

	public void createControl(Composite parent) {
		super.createControl(parent);
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		composite = new Composite (parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout1);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		optionGroup = new Group(composite, SWT.NONE);
		optionGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		optionGroup.setLayout(gridLayout2);
		optionGroup.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.option.group")); //$NON-NLS-1$
		labelAppTitle = new Label(optionGroup, SWT.NONE);
		labelAppTitle.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelAppTitle")); //$NON-NLS-1$
		createAppComposite();
		labelRomAppTitle = new Label(optionGroup, SWT.NONE);
		labelRomAppTitle.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelRomAppTitle")); //$NON-NLS-1$		
		createRomAppComposite();
		labelRomTitle = new Label(optionGroup, SWT.NONE);
		labelRomTitle.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelRomTitle")); //$NON-NLS-1$				
		createRomComposite();
		labelNoneTitle = new Label(optionGroup, SWT.NONE);
		labelNoneTitle.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelNoneTitle")); //$NON-NLS-1$				
		createNoneComposite();
//		createCustomComposite();
		
		GridData buttonWidthGridData = new GridData();
		buttonWidthGridData.widthHint = Math.max(Math.max(Math.max(phonensisImage.getBounds().width, phoneImage.getBounds().width), sisImage.getBounds().width), noneImage.getBounds().width) + 30;
		buttonWidthGridData.heightHint = Math.max(Math.max(Math.max(phonensisImage.getBounds().height, phoneImage.getBounds().height), sisImage.getBounds().height), noneImage.getBounds().height) + 30;
		buttonRomApp.setLayoutData(buttonWidthGridData);
		buttonRom.setLayoutData(buttonWidthGridData);
		buttonApp.setLayoutData(buttonWidthGridData);
		buttonNone.setLayoutData(buttonWidthGridData);
		
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_CONFIG_SELECTOR);
		
		validatePage();
	}
	
	void createAppComposite() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		appComposite = new Composite (optionGroup, SWT.NONE);
		appComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		appComposite.setLayout(gridLayout1);
		buttonApp = new Button(appComposite, SWT.TOGGLE);
		buttonApp.setImage(sisImage);
		buttonApp.addSelectionListener(buttonListener);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		appLabelComposite = new Composite (appComposite, SWT.NONE);
		appLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		appLabelComposite.setLayout(gridLayout2);
		labelApp2 = new Label(appLabelComposite, SWT.NONE);
		labelApp2.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelApp2")); //$NON-NLS-1$
	}
	
	void createRomAppComposite() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		romAppComposite = new Composite (optionGroup, SWT.NONE);
		romAppComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		romAppComposite.setLayout(gridLayout1);
		buttonRomApp = new Button(romAppComposite, SWT.TOGGLE);
		buttonRomApp.setImage(phonensisImage);
		buttonRomApp.addSelectionListener(buttonListener);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		romAppLabelComposite = new Composite (romAppComposite, SWT.NONE);
		romAppLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		romAppLabelComposite.setLayout(gridLayout2);
		labelRomApp2 = new Label(romAppLabelComposite, SWT.NONE);
		labelRomApp2.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelRomApp2")); //$NON-NLS-1$		
		labelRomApp3 = new Label(romAppLabelComposite, SWT.NONE);
		labelRomApp3.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelRomApp3")); //$NON-NLS-1$		
	}
	
	void createRomComposite() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		romComposite = new Composite (optionGroup, SWT.NONE);
		romComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		romComposite.setLayout(gridLayout1);
		buttonRom = new Button(romComposite, SWT.TOGGLE);
		buttonRom.setImage(phoneImage);
		buttonRom.addSelectionListener(buttonListener);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		romLabelComposite = new Composite (romComposite, SWT.NONE);
		romLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		romLabelComposite.setLayout(gridLayout2);
		labelRom2 = new Label(romLabelComposite, SWT.NONE);
		labelRom2.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelRom2")); //$NON-NLS-1$				
	}
	
	void createNoneComposite() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		noneComposite = new Composite (optionGroup, SWT.NONE);
		noneComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		noneComposite.setLayout(gridLayout1);
		buttonNone = new Button(noneComposite, SWT.TOGGLE);
		buttonNone.setImage(noneImage);
		buttonNone.addSelectionListener(buttonListener);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		noneLabelComposite = new Composite (noneComposite, SWT.NONE);
		noneLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
		noneLabelComposite.setLayout(gridLayout2);
		labelNone2 = new Label(noneLabelComposite, SWT.NONE);
		labelNone2.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelNone2")); //$NON-NLS-1$				
		labelNone3 = new Label(noneLabelComposite, SWT.NONE);
		labelNone3.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelNone3")); //$NON-NLS-1$				
	}

	void createCustomComposite() {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 2;
		customTraceGroup = new Group(composite, SWT.NONE);
		customTraceGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		customTraceGroup.setLayout(gridLayout3);
		buttonCustomTrace = new Button(customTraceGroup, SWT.CHECK);
		buttonCustomTrace.setImage(cusTraceImage);
		buttonCustomTrace.addSelectionListener(buttonListener);
		labelCustomTrace = new Label(customTraceGroup, SWT.WRAP);
		labelCustomTrace.setText(Messages.getString("NewPIWizardPageConfigSelectorTask.labelCustomTrace")); //$NON-NLS-1$
	}
	
	public void validatePage() {
		if (buttonRomApp.getSelection() == false &&
			buttonApp.getSelection() == false &&
			buttonRom.getSelection() == false 
			&&
			buttonNone.getSelection() == false
			) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
		setErrorMessage(null);
	}

	public void setupPageFromFromNewPIWizardSettings() {
		if (NewPIWizardSettings.getInstance().haveAppRom == true) {
//			buttonApp.setSelection(false);
//			buttonRomApp.setSelection(true);
//			buttonRom.setSelection(false);
			// set focus, and block them
			buttonRomApp.setFocus();
		} else if (NewPIWizardSettings.getInstance().haveAppOnly == true) {
//			buttonApp.setSelection(true);
//			buttonRomApp.setSelection(false);
//			buttonRom.setSelection(false);
			// set focus, and block them
			buttonApp.setFocus();
		} else if (NewPIWizardSettings.getInstance().haveRomOnly == true) {
//			buttonApp.setSelection(false);
//			buttonRomApp.setSelection(false);
//			buttonRom.setSelection(true);
//			 set focus, and block them
			buttonRom.setFocus();
		} else {
			buttonNone.setFocus();
		}
		
//		buttonCustomTrace.setSelection(NewPIWizardSettings.getInstance().enableCust);
		
		validatePage();
	}

}
