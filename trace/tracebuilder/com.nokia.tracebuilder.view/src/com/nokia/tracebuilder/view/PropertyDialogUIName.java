/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Composite for name label, field and tag button
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;

/**
 * Composite for name label, field and tag button
 * 
 */
final class PropertyDialogUIName extends PropertyDialogComposite {

	/**
	 * Name label
	 */
	private Label nameLabel;

	/**
	 * Name field is mapped to trace object name.
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#setName(String)
	 */
	private Text nameField;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            the field modify listener
	 */
	PropertyDialogUIName(Composite parent, int uiType,
			ModifyListener modifyListener) {
		super(parent);
		create(uiType, modifyListener);
	}

	/**
	 * Creates the name text field and associated label
	 * 
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            the field modify listener
	 */
	private void create(int uiType, ModifyListener modifyListener) {
		nameLabel = new Label(getParent(), SWT.NONE);
		nameLabel.setText(getNameLabel(uiType));
		nameField = new Text(getParent(), SWT.BORDER);
		nameField.addModifyListener(modifyListener);
		setFieldButtonLayoutData(nameField, null);
	}

	/**
	 * Gets the label for name field
	 * 
	 * @param uiType
	 *            the UI type
	 * @return the label
	 */
	private String getNameLabel(int uiType) {
		String name;
		switch (uiType) {
		case TraceObjectPropertyDialog.EDIT_GROUP:
			name = Messages.getString("PropertyDialogUI.EditGroupNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_TRACE:
			name = Messages.getString("PropertyDialogUI.AddTraceNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			name = Messages.getString("PropertyDialogUI.EditTraceNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			name = Messages.getString("PropertyDialogUI.AddParameterNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			name = Messages.getString("PropertyDialogUI.AddConstantNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			name = Messages.getString("PropertyDialogUI.EditConstantNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE:
			name = Messages
					.getString("PropertyDialogUI.EditConstantTableNameLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			name = Messages.getString("PropertyDialogUI.TraceNameFormat"); //$NON-NLS-1$
			break;
		default:
			name = ""; //$NON-NLS-1$
			break;
		}
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name
	 *            the name
	 */
	void setName(String name) {
		nameField.setText(name);
	}

	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	String getName() {
		return nameField.getText();
	}

	/**
	 * Enables / disables the name field and tag button
	 * 
	 * @param flag
	 *            new enabled state
	 */
	void setEnabled(boolean flag) {
		nameField.setEnabled(flag);
	}

}
