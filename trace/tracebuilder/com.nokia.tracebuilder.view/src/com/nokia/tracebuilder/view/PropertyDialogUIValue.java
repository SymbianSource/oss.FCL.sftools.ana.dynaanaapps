/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Composite for value label, field and tag button
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
 * Composite for value label, field and tag button
 * 
 */
final class PropertyDialogUIValue extends PropertyDialogComposite {

	/**
	 * Each dialog has a different value label
	 */
	private Label valueLabel;

	/**
	 * Value field is mapped to trace text
	 */
	private Text valueField;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            text field modify listener
	 */
	PropertyDialogUIValue(Composite parent, int uiType,
			ModifyListener modifyListener) {
		super(parent);
		create(uiType, modifyListener);
	}

	/**
	 * Creates the value text field and associated label
	 * 
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            the text field modify listener
	 */
	private void create(int uiType, ModifyListener modifyListener) {
		valueLabel = new Label(getParent(), SWT.NONE);
		valueLabel.setText(getValueLabel(uiType));
		valueField = new Text(getParent(), SWT.BORDER);
		valueField.addModifyListener(modifyListener);
		setFieldButtonLayoutData(valueField, null);
	}

	/**
	 * Gets the label for value field
	 * 
	 * @param uiType
	 *            the UI type
	 * @return the label
	 */
	private String getValueLabel(int uiType) {
		String value;
		switch (uiType) {
		case TraceObjectPropertyDialog.ADD_TRACE:
			value = Messages.getString("PropertyDialogUI.AddTraceValueLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_TRACE:
			value = Messages.getString("PropertyDialogUI.EditTraceValueLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			value = Messages.getString("PropertyDialogUI.TraceTextFormat"); //$NON-NLS-1$
			break;
		default:
			value = ""; //$NON-NLS-1$
			break;
		}
		return value;
	}

	/**
	 * Gets the value field contents
	 * 
	 * @return the field contents
	 */
	String getValue() {
		return valueField.getText();
	}

	/**
	 * Sets the value field contents
	 * 
	 * @param value
	 *            the new value
	 */
	void setValue(String value) {
		valueField.setText(value);
	}

	/**
	 * Enables / disables the value field and the tag button
	 * 
	 * @param flag
	 *            new enabled state
	 */
	void setEnabled(boolean flag) {
		valueField.setEnabled(flag);
	}
	
	/**
	 * Sets the label text
	 * 
	 * @param labelText
	 *            the new label text
	 */
	void setLabel(String labelText) {
		if (labelText != null) {
			valueLabel.setText(labelText);
		}
	}
	
	/**
	 * Get value label
	 * 
	 * @return the value label
	 */
	Label getValueLabel() {
		return valueLabel;
	}

}
