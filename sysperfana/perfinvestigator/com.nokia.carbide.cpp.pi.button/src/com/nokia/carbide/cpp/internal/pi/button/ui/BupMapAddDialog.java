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

package com.nokia.carbide.cpp.internal.pi.button.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.button.ComNokiaCarbidePiButtonHelpIDs;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public class BupMapAddDialog extends TrayDialog {

	private int			oldEventKeyCode;
	private String		oldEventEnumString;
	private String		oldEventLabel;
	private int			eventKeyCode;
	private String		eventEnumString;
	private String		eventLabel;
	private ModifyCachedBupEventMap map;
	
	// control
	private Composite composite = null;
	private Composite labelAndValueComposite = null;
	
	private Label keyCodeLabel = null;
	private Text keyCodeValueText = null;
	
	private Label enumLabel = null;
	private Text enumValueText = null;
	
	private Label labelLabel = null;
	private Text labelText = null;
	
	private Label messageLabel = null;
	
	private Label hexNeeds0x = null;
	
	public BupMapAddDialog(Shell shell, ModifyCachedBupEventMap cachedMap, int keyCode, String enumString, String label) {
		super(shell);
		
		// store original values
		oldEventKeyCode = eventKeyCode = keyCode;
		oldEventEnumString = eventEnumString = enumString;
		oldEventLabel = eventLabel = label;
		map = cachedMap;
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("BupMapAddDialog.addMapping"));  //$NON-NLS-1$
		
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ComNokiaCarbidePiButtonHelpIDs.PI_BUTTON_ADD_DIALOG);

		labelAndValueComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(labelAndValueComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelAndValueComposite);
		
		messageLabel = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().indent(15,0).grab(true, true).hint(200, 15).applyTo(messageLabel);
		messageLabel.setFont(PIPageEditor.helvetica_9);
		
		hexNeeds0x = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().indent(15,0).grab(true, true).hint(200, 15).applyTo(hexNeeds0x);
		hexNeeds0x.setText(Messages.getString("BupMapAddDialog.startHexWith0x")); //$NON-NLS-1$
		hexNeeds0x.setFont(PIPageEditor.helvetica_9);
		
		keyCodeLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(keyCodeLabel);
		keyCodeLabel.setFont(PIPageEditor.helvetica_9);
		keyCodeLabel.setText(Messages.getString("BupMapAddDialog.value")); //$NON-NLS-1$
		
		keyCodeValueText = new Text(labelAndValueComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(200, 15).applyTo(keyCodeValueText);
		keyCodeValueText.setFont(PIPageEditor.helvetica_9);
		keyCodeValueText.setText(""); //$NON-NLS-1$
		// add the listener(s)
		keyCodeValueText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				validateKeyCode();
			}
		});
		
		enumLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(enumLabel);
		enumLabel.setFont(PIPageEditor.helvetica_9);
		enumLabel.setText(Messages.getString("BupMapAddDialog.tKeyCode")); //$NON-NLS-1$
		
		enumValueText = new Text(labelAndValueComposite, SWT.BORDER);
		enumValueText.setFont(PIPageEditor.helvetica_9);
		GridDataFactory.fillDefaults().grab(true, true).hint(200, 15).applyTo(enumValueText);
		// add the listener(s)
		enumValueText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				eventEnumString = enumValueText.getText();
			}
		});
		enumValueText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				eventEnumString = enumValueText.getText();
			}
		});
		enumValueText.setText(eventEnumString);
		
		labelLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labelLabel);
		labelLabel.setFont(PIPageEditor.helvetica_9);
		labelLabel.setText(Messages.getString("BupMapAddDialog.label")); //$NON-NLS-1$
		
		labelText = new Text(labelAndValueComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(200, 15).applyTo(labelText);
		labelText.setFont(PIPageEditor.helvetica_9);
		// add the listener(s)
		labelText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				eventLabel = labelText.getText();
			}
		});
		labelText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				eventLabel = labelText.getText();
			}
		});
		labelText.setText(eventLabel);
				
		return composite;
	}
	
	/**
	 * 
	 */
	protected void validateKeyCode() {
		int keyCode = -1;
		String message = ""; //$NON-NLS-1$
		String keyCodeString = keyCodeValueText.getText();
		messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		if (keyCodeString.length() == 0)
			message = " "; //$NON-NLS-1$
		else {
			try {
				keyCode = Integer.parseInt(keyCodeString);
			} catch (NumberFormatException e) {
				try {
					if (keyCodeString.startsWith("0x")) { //$NON-NLS-1$
						keyCode = Integer.parseInt(keyCodeString.substring(2), 16);
					} else {
						Integer.parseInt(keyCodeString, 16);
						message = Messages.getString("BupMapAddDialog.hexNeeds0x") + keyCodeValueText.getText(); //$NON-NLS-1$
						messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
					}
				} catch (NumberFormatException e2) {
					message = keyCodeValueText.getText() + Messages.getString("BupMapAddDialog.invalidNumber"); //$NON-NLS-1$
					messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			}
			if (keyCode >= 0) {
				eventKeyCode = keyCode;
				if (map.getKeyCodeSet().contains(keyCode)) {
					message = keyCode + Messages.getString("BupMapAddDialog.existsInMap1") //$NON-NLS-1$
							+ Integer.toHexString(keyCode) + Messages.getString("BupMapAddDialog.existsInMap2"); //$NON-NLS-1$
					messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			}
		}
		messageLabel.setText(message);
		messageLabel.setVisible(!message.equals("")); //$NON-NLS-1$
		getButton(IDialogConstants.OK_ID).setEnabled(message.equals("")); //$NON-NLS-1$
	}

	public int getKeyCode() {
		return eventKeyCode;
	}
	
	public String getEnumString() {
		if (eventEnumString == null) {
			return ""; //$NON-NLS-1$
		}
		return eventEnumString;
	}
	
	public String getLabel() {
		if (eventLabel == null) {
			return ""; //$NON-NLS-1$
		}
		return eventLabel;
	}

	protected void okPressed() {
		super.okPressed();
	}
	
	protected void cancelPressed() {
		eventKeyCode = oldEventKeyCode;
		eventEnumString = oldEventEnumString;
		eventLabel = oldEventLabel;
		super.cancelPressed();
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		// now buttons are there, we can  disable key
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
}
