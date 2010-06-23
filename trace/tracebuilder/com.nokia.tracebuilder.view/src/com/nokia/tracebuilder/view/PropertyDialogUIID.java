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
* Composite for ID label, field and hex checkbox
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;

/**
 * Composite for ID label, field and hex checkbox
 * 
 */
class PropertyDialogUIID extends PropertyDialogComposite {

	/**
	 * Configuration name for hex checkbox state
	 */
	private static final String HEX_CHECK_CONFIG = "PropertyDialogUIID.hexCheck"; //$NON-NLS-1$

	/**
	 * Hex checkbox listener
	 * 
	 */
	private final class HexCheckSelectionListener implements SelectionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			try {
				TraceBuilderGlobals.getConfiguration().setFlag(
						HEX_CHECK_CONFIG, hexCheck.getSelection());
				// Converts the ID field to new format. The old value of the
				// hex checkbox is used, since that represents the value
				// before the checkbox was changed
				setID(idFromString(idField.getText(), !hexCheck.getSelection()));
			} catch (NumberFormatException ex) {
			}
		}
	}

	/**
	 * Modification listener for the ID field
	 * 
	 */
	private final class IDFieldModifyListener implements ModifyListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			// If the hex field is not checked, a hex character checks it
			if (!hexCheck.getSelection()) {
				String text = idField.getText();
				if (hasHexPrefix(text) || hasHexChars(text)) {
					hexCheck.setSelection(true);
				}
			}
		}
	}

	/**
	 * Radix for hex values
	 */
	private static final int HEX_RADIX = 16; // CodForChk_Dis_Magic

	/**
	 * ID label
	 */
	private Label idLabel;

	/**
	 * ID field is mapped to trace object ID.
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObject#setID(int)
	 */
	private Text idField;

	/**
	 * Hex ID checkbox
	 */
	private Button hexCheck;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param uiType
	 *            type of UI
	 * @param modifyListener
	 *            the modify listener for ID field
	 */
	PropertyDialogUIID(Composite parent, int uiType,
			ModifyListener modifyListener) {
		super(parent);
		create(uiType, modifyListener);
	}

	/**
	 * Creates the ID text field and associated label
	 * 
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            listener for text field changes
	 */
	private void create(int uiType, ModifyListener modifyListener) {
		// Label is added to this composite
		idLabel = new Label(getParent(), SWT.NONE);
		idLabel.setText(getIDLabel(uiType));
		// Field and button are added to sub-composite
		Composite composite = createFieldButtonComposite();
		idField = new Text(composite, SWT.BORDER);
		idField.addModifyListener(modifyListener);
		idField.addModifyListener(new IDFieldModifyListener());
		hexCheck = new Button(composite, SWT.CHECK);
		hexCheck.setSelection(TraceBuilderGlobals.getConfiguration().getFlag(
				HEX_CHECK_CONFIG));
		hexCheck.setText(Messages.getString("PropertyDialogUI.HexButtonTitle")); //$NON-NLS-1$
		hexCheck.addSelectionListener(new HexCheckSelectionListener());
		setFieldButtonLayoutData(idField, hexCheck);
	}

	/**
	 * Gets the label for ID field
	 * 
	 * @param uiType
	 *            the UI type
	 * @return the label
	 */
	private String getIDLabel(int uiType) {
		String id;
		switch (uiType) {
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			id = Messages.getString("PropertyDialogUI.AddConstantIDLabel"); //$NON-NLS-1$
			break;
		case TraceObjectPropertyDialog.EDIT_CONSTANT:
			id = Messages.getString("PropertyDialogUI.EditConstantIDLabel"); //$NON-NLS-1$
			break;
		default:
			id = ""; //$NON-NLS-1$
			break;
		}
		return id;
	}

	/**
	 * Checks if given string has hex characters
	 * 
	 * @param text
	 *            the text
	 * @return true if there are hex characters
	 */
	private boolean hasHexChars(String text) {
		boolean isHex = false;
		for (int i = 0; i < text.length() && !isHex; i++) {
			char c = text.charAt(i);
			if (c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e'
					|| c == 'f' || c == 'A' || c == 'B' || c == 'C' || c == 'D'
					|| c == 'E' || c == 'F') {
				isHex = true;
			}
		}
		return isHex;
	}

	/**
	 * Checks if the text has hex prefix
	 * 
	 * @param text
	 *            the text
	 * @return true if there is hex prefix
	 */
	private boolean hasHexPrefix(String text) {
		boolean isHex = false;
		String pref = Messages.getString("PropertyDialogUI.HexPrefix"); //$NON-NLS-1$
		if (text.length() > pref.length()) {
			if (text.substring(0, pref.length()).equalsIgnoreCase(pref)) {
				isHex = true;
			}
		}
		return isHex;
	}

	/**
	 * Tries to convert an ID string to number
	 * 
	 * @param text
	 *            the ID string
	 * @param isHex
	 *            true if interpreted as hex
	 * @return the ID
	 * @throws NumberFormatException
	 *             if ID is not valid
	 */
	int idFromString(String text, boolean isHex) throws NumberFormatException {
		if (!isHex) {
			// If hex checkbox is not selected, the content is still checked
			// If there is hex prefix or hex characters, it is interpreted as
			// hex
			isHex = hasHexPrefix(text);
			if (!isHex) {
				isHex = hasHexChars(text);
			} else {
				text = text.substring(Messages.getString(
						"PropertyDialogUI.HexPrefix").length()); //$NON-NLS-1$
			}
		} else {
			// If checkbox is selected, the prefix needs to be removed if
			// it exists
			if (hasHexPrefix(text)) {
				text = text.substring(Messages.getString(
						"PropertyDialogUI.HexPrefix").length()); //$NON-NLS-1$
			}
		}
		int id;
		if (isHex) {
			id = Long.valueOf(text, HEX_RADIX).intValue();
		} else {
			id = Integer.valueOf(text).intValue();
		}
		return id;
	}

	/**
	 * Sets the ID field value
	 * 
	 * @param id
	 *            the new ID value
	 */
	void setID(int id) {
		if (hexCheck.getSelection()) {
			idField.setText(Messages.getString("PropertyDialogUI.HexPrefix") //$NON-NLS-1$ 
					+ Integer.toHexString(id));
		} else {
			idField.setText(Integer.toString(id));
		}
	}

	/**
	 * Returns the value from the ID field. Throws NumberFormatException if the
	 * ID field does not contain a number
	 * 
	 * @return id field value
	 * @throws NumberFormatException
	 *             if the ID field does not contain valid data
	 */
	int getID() throws NumberFormatException {
		return idFromString(idField.getText(), hexCheck.getSelection());
	}

	/**
	 * Enables or disables the ID field and hex button
	 * 
	 * @param flag
	 *            the enabled flag
	 */
	void setEnabled(boolean flag) {
		idField.setEnabled(flag);
		hexCheck.setEnabled(flag);
	}

}
