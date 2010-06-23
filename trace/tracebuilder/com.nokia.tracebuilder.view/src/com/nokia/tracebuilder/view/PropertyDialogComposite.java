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
* Composite, which wraps a text field / combo box and a button
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Composite, which wraps a text field / combo box and a button
 * 
 */
class PropertyDialogComposite {

	/**
	 * Number of columns in sub-composites
	 */
	private static final int SUB_COMPOSITE_COLUMNS = 2; // CodForChk_Dis_Magic

	/**
	 * Button width
	 */
	private static final int BUTTON_WIDTH = 75; // CodForChk_Dis_Magic

	/**
	 * Field width
	 */
	private final static int FIELD_WIDTH = 350; // CodForChk_Dis_Magic

	/**
	 * The parent composite
	 */
	private Composite parent;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 */
	PropertyDialogComposite(Composite parent) {
		this.parent = parent;
	}

	/**
	 * Creates a composite with two columns and grid layout
	 * 
	 * @return the composite
	 */
	protected Composite createFieldButtonComposite() {
		GridLayout layout = new GridLayout();
		layout.numColumns = SUB_COMPOSITE_COLUMNS;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(gridData);
		return composite;
	}

	/**
	 * Sets the layout data of the controls in field-button composite
	 * 
	 * @param field
	 *            the field
	 * @param button
	 *            the button
	 */
	protected void setFieldButtonLayoutData(Control field, Control button) {
		GridData fieldData = new GridData();
		fieldData.horizontalAlignment = GridData.FILL;
		fieldData.widthHint = FIELD_WIDTH;
		field.setLayoutData(fieldData);
		if (button != null) {
			GridData buttonData = new GridData();
			buttonData.widthHint = BUTTON_WIDTH;
			button.setLayoutData(buttonData);
		}
	}

	/**
	 * Gets the parent composite
	 * 
	 * @return the parent composite
	 */
	protected Composite getParent() {
		return parent;
	}

}
