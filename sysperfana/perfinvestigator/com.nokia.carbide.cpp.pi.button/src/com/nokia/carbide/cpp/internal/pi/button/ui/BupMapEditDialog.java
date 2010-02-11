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

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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

public class BupMapEditDialog extends TrayDialog {

	private String		oldEventEnumString;
	private String		oldEventLabel;
	private int			eventKeyCode;
	private String		eventEnumString;
	private String		eventLabel;
	
	// control
	private Composite composite = null;
	private Composite labelAndValueComposite = null;
	
	private Label keyCodeLabel = null;
	private Label keyCodeValueLabel = null;
	
	private Label enumLabel = null;
	private Text enumValueText = null;
	
	private Label labelLabel = null;
	private Text labelText = null;
	
	public BupMapEditDialog(Shell shell, int keyCode, String enumString, String label) {
		super(shell);
		
		// store original values
		eventKeyCode = keyCode;
		oldEventEnumString = eventEnumString = enumString;
		oldEventLabel = eventLabel = label;
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.getString("BupMapEditDialog.editMappingForKey") + "0x" + Integer.toHexString(eventKeyCode));  //$NON-NLS-1$ //$NON-NLS-2$
		
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ComNokiaCarbidePiButtonHelpIDs.PI_BUTTON_EDIT_DIALOG);

		labelAndValueComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(labelAndValueComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelAndValueComposite);
		
		keyCodeLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(keyCodeLabel);
		keyCodeLabel.setFont(PIPageEditor.helvetica_9);
		keyCodeLabel.setText(Messages.getString("BupMapEditDialog.hexValue")); //$NON-NLS-1$
		
		keyCodeValueLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).hint(200, 15).applyTo(keyCodeValueLabel);
		keyCodeValueLabel.setFont(PIPageEditor.helvetica_9);
		keyCodeValueLabel.setText("0x" + Integer.toHexString(eventKeyCode)); //$NON-NLS-1$
		// add the listener(s)
		
		enumLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(enumLabel);
		enumLabel.setFont(PIPageEditor.helvetica_9);
		enumLabel.setText(Messages.getString("BupMapEditDialog.tKeyCode")); //$NON-NLS-1$
		
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
		labelLabel.setText(Messages.getString("BupMapEditDialog.label")); //$NON-NLS-1$
		
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
		eventEnumString = oldEventEnumString;
		eventLabel = oldEventLabel;
		super.cancelPressed();
	}
}
