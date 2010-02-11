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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.RofsObySymbolPair;
import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;

public class NewPIWizardObySymbolPairDialog extends TitleAreaDialog {
	
	private RofsObySymbolPair pair = new RofsObySymbolPair();

	// control
	private Composite composite = null;
	private Label obyLabel = null;
	private Composite obyComposite = null;
	private Text obyText = null;
	private Button obyButton = null;
	private Label symbolLabel = null;
	private Composite symbolComposite = null;
	private Text symbolText = null;
	private Button symbolButton = null;
	
	protected NewPIWizardObySymbolPairDialog(Shell arg0) {
		super(arg0);
		setHelpAvailable(false);
	}
	
	public Control createDialogArea(Composite parent) {
		setTitle(Messages.getString("NewPIWizardObySymbolPairDialog.rofs.oby.rofs.symbol")); //$NON-NLS-1$
		setTitleImage(CarbideUIPlugin.getSharedImages().getImage(ICarbideSharedImages.IMG_PI_IMPORT_ROM_42_42));
		getShell().setText(Messages.getString("NewPIWizardObySymbolPairDialog.shell.title")); //$NON-NLS-1$
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout);
	
		obyLabel = new Label (composite, SWT.NONE);
		obyLabel.setText(Messages.getString("NewPIWizardObySymbolPairDialog.rofs.oby.file")); //$NON-NLS-1$
		GridLayout obyGridLayout = new GridLayout();
		obyGridLayout.numColumns = 2;
		obyComposite = new Composite(composite, SWT.NONE);
		obyComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		obyComposite.setLayout(obyGridLayout);
		obyText = new Text (obyComposite, SWT.BORDER | SWT.SINGLE);
		obyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		obyText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				getButton(IDialogConstants.OK_ID).setEnabled(validateAll());
				symbolText.setFocus();
			}
			
		});
		obyButton = new Button (obyComposite, SWT.NONE);
		obyButton.setText(Messages.getString("NewPIWizardObySymbolPairDialog.browse")); //$NON-NLS-1$
		obyButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(getShell());
				String[] extensions = {"*.oby"};	//$NON-NLS-1$
				String[] names = {Messages.getString("NewPIWizardObySymbolPairDialog.build.file.extension")}; //$NON-NLS-1$
				dialog.setFilterExtensions(extensions);
				dialog.setFilterNames(names);
				String selectedFilePath = dialog.open();
				if (selectedFilePath != null)  {
					obyText.setText(selectedFilePath);
				}
			}
			
		});
		
		symbolLabel = new Label (composite, SWT.NONE);
		symbolLabel.setText(Messages.getString("NewPIWizardObySymbolPairDialog.rofs.symbol.file")); //$NON-NLS-1$
		GridLayout symbolGridLayout = new GridLayout();
		symbolGridLayout.numColumns = 2;
		symbolComposite = new Composite(composite, SWT.NONE);
		symbolComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		symbolComposite.setLayout(obyGridLayout);
		symbolText = new Text (symbolComposite, SWT.BORDER | SWT.SINGLE);
		symbolText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		symbolText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				getButton(IDialogConstants.OK_ID).setEnabled(validateAll());
				symbolText.setFocus();
			}
			
		});
		symbolButton = new Button (symbolComposite, SWT.NONE);
		symbolButton.setText(Messages.getString("NewPIWizardObySymbolPairDialog.browse")); //$NON-NLS-1$
		symbolButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(getShell());
				String[] extensions = {"*.symbol"};	//$NON-NLS-1$
				String[] names = {Messages.getString("NewPIWizardObySymbolPairDialog.symbol.file")}; //$NON-NLS-1$
				dialog.setFilterExtensions(extensions);
				dialog.setFilterNames(names);
				String selectedFilePath = dialog.open();
				if (selectedFilePath != null)  {
					symbolText.setText(selectedFilePath);
				}
			}

		});
		
		// initial state
		validateAll();

		return composite;
	}
	
	boolean validateAll() {

		setMessage(null);
		setErrorMessage(null);
		
		symbolText.setEnabled(false);
		symbolButton.setEnabled(false);
		if (obyText.getText().length() > 0) {
			if (obyText.getText().toLowerCase().endsWith(".oby") == false) {	//$NON-NLS-1$
				setErrorMessage(Messages.getString("NewPIWizardObySymbolPairDialog.oby.file.extension.error")); //$NON-NLS-1$
				return false;
			}
			if (new java.io.File(obyText.getText()).exists() == false) {
				setErrorMessage(Dialog.shortenText(Messages.getString("NewPIWizardObySymbolPairDialog.oby.file") + obyText.getText() + Messages.getString("NewPIWizardObySymbolPairDialog.does.not.exist.in.file.system"), obyComposite)); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
			symbolText.setEnabled(true);
			symbolButton.setEnabled(true);
		}
		
		if (symbolText.getText().length() > 0) {
			if (symbolText.getText().toLowerCase().endsWith(".symbol") == false) {	//$NON-NLS-1$
				setErrorMessage(Messages.getString("NewPIWizardObySymbolPairDialog.symbol.extension.error")); //$NON-NLS-1$
				return false;
			}
			if (new java.io.File(symbolText.getText()).exists() == false) {
				setErrorMessage(Dialog.shortenText(Messages.getString("NewPIWizardObySymbolPairDialog.symbol.file.2") + symbolText.getText() + Messages.getString("NewPIWizardObySymbolPairDialog.does.not.exist.in.file.system.2"), symbolComposite)); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		
		return true;
	}
	
	RofsObySymbolPair getPair() {
		return pair;
	}
	
	public void okPressed() {
		pair.setObyFile(obyText.getText());
		pair.setSymbolFile(symbolText.getText());
		super.okPressed();
	}
}
