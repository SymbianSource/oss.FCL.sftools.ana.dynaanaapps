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

package com.nokia.carbide.cpp.pi.button;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public class BupEventDialog extends TrayDialog {
	
	private int			eventKeyCode;
	private String		eventEnumString;
	private String		eventName;
	private String		eventComment;
	private long		eventTime;
	private boolean		eventPropagate;

	private String		newEventName;
	private String		newEventComment;
	private boolean		newEventPropagate;
	
	
	// control
	private Composite composite = null;
	private Composite labelAndValueComposite = null;
	private Composite checkBoxComposite = null;
	
	private Label keyCodeLabel = null;
	private Label keyCodeValueLabel = null;
	
	private Label enumLabel = null;
	private Label enumValueLabel = null;
	
	private Label nameLabel = null;
	private Text nameText = null;
	
	private Label commentLabel = null;
	private Text commentText = null;
	
	private Label timeLabel = null;
	private Label timeValueLabel = null;
	
	private Button propagateButton = null;
	private Label propagateLabel = null;
	
	public BupEventDialog(Shell shell, String title, int keyCode, String enumString, String label, String comment, boolean propagate,
			long sampleSynchTime) {
		super(shell);
		
		// store original values
		eventKeyCode = keyCode;
		eventEnumString = enumString;
		newEventName = eventName = label;
		newEventComment = eventComment = comment;
		eventTime = sampleSynchTime;
		newEventPropagate = eventPropagate = propagate;
	}
	
	public Control createDialogArea(Composite parent) {
		Point textBoxSize = new Point(150, (int) (PIPageEditor.helvetica_9.getFontData()[0].height + 6));
		getShell().setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.0") + eventName + com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.1") + eventTime/1000d + com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ComNokiaCarbidePiButtonHelpIDs.PI_EDIT_BUTTON_EVENT);

		labelAndValueComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(labelAndValueComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelAndValueComposite);
		
		keyCodeLabel = new Label(labelAndValueComposite, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(keyCodeLabel);
		keyCodeLabel.setFont(PIPageEditor.helvetica_9);
		keyCodeLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.3")); //$NON-NLS-1$
		
		keyCodeValueLabel = new Label(labelAndValueComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(keyCodeValueLabel);
		keyCodeValueLabel.setFont(PIPageEditor.helvetica_9);
		keyCodeValueLabel.setText("0x" + Integer.toHexString(eventKeyCode)); //$NON-NLS-1$
		
		enumLabel = new Label(labelAndValueComposite, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(enumLabel);
		enumLabel.setFont(PIPageEditor.helvetica_9);
		enumLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.5")); //$NON-NLS-1$
		
		enumValueLabel = new Label(labelAndValueComposite, SWT.NONE);
		enumValueLabel.setFont(PIPageEditor.helvetica_9);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(enumValueLabel);
		enumValueLabel.setText(eventEnumString);
		
		nameLabel = new Label(labelAndValueComposite, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameLabel);
		nameLabel.setFont(PIPageEditor.helvetica_9);
		nameLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.6")); //$NON-NLS-1$
		
		nameText = new Text(labelAndValueComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(nameText);
		nameText.setFont(PIPageEditor.helvetica_9);
		nameText.setData(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.7")); //$NON-NLS-1$
		
		if (eventName != null) {
			nameText.setText(eventName);
		}
		nameText.setEditable(true);
		GridDataFactory.fillDefaults().hint(textBoxSize).applyTo(nameText);

		// add the listener(s)
		nameText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEventName = nameText.getText();
			}
		});
		nameText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newEventName = nameText.getText();
			}
		});

		commentLabel = new Label(labelAndValueComposite, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentLabel);
		commentLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.8")); //$NON-NLS-1$
		commentLabel.setFont(PIPageEditor.helvetica_9);

		commentText = new Text(labelAndValueComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(commentText);
		commentText.setFont(PIPageEditor.helvetica_9);
		commentText.setData(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.9")); //$NON-NLS-1$

		if (eventComment != null) {
			commentText.setText(eventComment);
		}
		commentText.setEditable(true);
		GridDataFactory.fillDefaults().hint(textBoxSize).applyTo(commentText);

		// add the listener(s)
		commentText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEventComment = commentText.getText();
			}
		});
		commentText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newEventComment = commentText.getText();
			}
		});
		
		timeLabel = new Label(labelAndValueComposite, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(timeLabel);
		timeLabel.setFont(PIPageEditor.helvetica_9);
		timeLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.10")); //$NON-NLS-1$
		
		timeValueLabel = new Label(labelAndValueComposite, SWT.NONE);
		timeValueLabel.setFont(PIPageEditor.helvetica_9);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(timeValueLabel);
		timeValueLabel.setText(eventTime/1000d + com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.11")); //$NON-NLS-1$


		checkBoxComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(checkBoxComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(checkBoxComposite);
		
		propagateButton = new Button(checkBoxComposite, SWT.CHECK);
		propagateButton.setEnabled(true);
		propagateButton.setSelection(eventPropagate);
		propagateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEventPropagate = propagateButton.getSelection();
			}
		});
		
		propagateLabel = new Label(checkBoxComposite, SWT.NONE);
		propagateLabel.setFont(PIPageEditor.helvetica_9);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(propagateLabel);
		propagateLabel.setText(com.nokia.carbide.cpp.pi.button.Messages.getString("BupEventDialog.12")); //$NON-NLS-1$
		
		return composite;
	}

	public String getNewName() {
		return newEventName;
	}

	public String getNewComment() {
		return newEventComment;
	}

	public boolean getNewSamePropagate() {
		return newEventPropagate;
	}

	protected void okPressed() {
		if (   ((eventName == null) && (newEventName != null))
				|| ((eventName != null) && !eventName.equals(newEventName))
				|| ((eventComment == null) && (newEventComment != null))
				|| ((eventComment != null) && !eventComment.equals(newEventComment))
				|| eventPropagate != newEventPropagate)
			{
				// the file has changed
				PIPageEditor.currentPageEditor().setDirty();
			}
		super.okPressed();
	}
	
	protected void cancelPressed() {
		newEventName    = eventName;
		newEventComment = eventComment;
		newEventPropagate = eventPropagate;
		super.cancelPressed();
	}
}
