/*
 * Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Composite for parameter type label, combo box and new type button
 *
 */
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Composite for parameter type label, combo box and new type button
 * 
 */
final class PropertyDialogUIType extends PropertyDialogComposite {

	/**
	 * Parameter type label
	 */
	private Label typeLabel = null;

	/**
	 * Parameter type selector
	 */
	private Combo typeCombo = null;

	/**
	 * Limited Trace visible type item count
	 */
	private final int limitedTraceVisibleTypeItemCnt = 2; // CodForChk_Dis_Magic

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param uiType
	 *            the UI type
	 * @param listener
	 *            listener for type selection
	 */
	PropertyDialogUIType(Composite parent, int uiType,
			SelectionListener listener) {
		super(parent);
		create(uiType, listener);
	}

	/**
	 * Creates the parameter type combobox and associated label
	 * 
	 * @param uiType
	 *            the type of UI
	 * @param listener
	 *            selection listener for combobox
	 */
	private void create(int uiType, SelectionListener listener) {
		// Label is added to this composite
		typeLabel = new Label(getParent(), SWT.NONE);
		typeLabel.setText(getTypeLabel(uiType));
		typeCombo = new Combo(getParent(), SWT.READ_ONLY);

		// Adds parameter types to the selector
		for (int i = 0; i < TraceViewMessages.PARAMETER_LABEL_MAP.length; i++) {
			typeCombo.add(TraceViewMessages.PARAMETER_LABEL_MAP[i][1]);
		}
		// Adds constant table names after parameter types
		Iterator<TraceConstantTable> tables = TraceBuilderGlobals
				.getTraceModel().getConstantTables();
		while (tables.hasNext()) {
			typeCombo.add(tables.next().getName());
		}

		// If selected object is Performance or State trace, only Signed32 and
		// Unsigned32 types are allowed -> First two types in
		// TraceViewMessages.PARAMETER_LABEL_MAP. Also, disable the use of the
		// "Array Parameter" checkbox
		if (isPerformaceTraceSelected() || isStateTraceSelected()) {
			typeCombo.remove(limitedTraceVisibleTypeItemCnt, typeCombo
					.getItemCount() - 1);
			typeCombo.setVisibleItemCount(limitedTraceVisibleTypeItemCnt);
		} else {
			typeCombo.setVisibleItemCount(typeCombo.getItemCount());
		}

		typeCombo.addSelectionListener(listener);
		setFieldButtonLayoutData(typeCombo, null);
	}

	/**
	 * Gets the label for type field
	 * 
	 * @param uiType
	 *            the UI type
	 * @return the label
	 */
	private String getTypeLabel(int uiType) {
		String type;
		switch (uiType) {
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			type = Messages.getString("PropertyDialogUI.AddParameterTypeLabel"); //$NON-NLS-1$
			break;
		default:
			type = ""; //$NON-NLS-1$
			break;
		}
		return type;
	}

	/**
	 * Check is Performance Event trace selected
	 * 
	 * @return true if Performance Event Trace is selected, otherwise false
	 */
	private boolean isPerformaceTraceSelected() {
		boolean retval = false;

		TraceObject selectedObject = TraceBuilderGlobals.getTraceBuilder()
				.getSelectedObject();

		if (selectedObject instanceof Trace) {
			Trace trace = (Trace) selectedObject;
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals
					.getGroupNameHandler();
			if (trace.getGroup().getName().equals(
					groupNameHandler.getDefaultGroups()[groupNameHandler
							.getPerformanceGroupIdIndex()])) {
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Check is State trace selected
	 * 
	 * @return true if State Trace is selected, otherwise false
	 */
	private boolean isStateTraceSelected() {
		boolean retval = false;

		TraceObject selectedObject = TraceBuilderGlobals.getTraceBuilder()
				.getSelectedObject();

		if (selectedObject instanceof Trace) {
			Trace trace = (Trace) selectedObject;
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals
					.getGroupNameHandler();
			if (trace.getGroup().getName().equals(
					groupNameHandler.getDefaultGroups()[groupNameHandler
							.getStateGroupIdIndex()])) {
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Gets the type selected from the combo box
	 * 
	 * @return the selected type
	 */
	String getSelectedType() {
		String type;
		int index = typeCombo.getSelectionIndex();
		if (index >= TraceViewMessages.PARAMETER_LABEL_MAP.length) {
			type = typeCombo.getText();
		} else {
			type = TraceViewMessages.PARAMETER_LABEL_MAP[index][0];
		}
		return type;
	}

	/**
	 * Selects the type
	 * 
	 * @param type
	 *            the type
	 */
	void setType(String type) {
		boolean found = false;

		// If selected object is Performance or State trace, only Signed32 and
		// Unsigned32 types are allowed, so because it could be that previous
		// type is not allowed, we always select Signed32 type as default in
		// that case and previous type check is skipped.
		if (!isPerformaceTraceSelected() && !isStateTraceSelected()) {
			if (type != null && type.length() > 0) {
				for (int i = 0; i < TraceViewMessages.PARAMETER_LABEL_MAP.length; i++) {
					if (TraceViewMessages.PARAMETER_LABEL_MAP[i][0]
							.equals(type)) {
						typeCombo.select(i);
						i = typeCombo.getItemCount();
						found = true;
						break;
					}
				}
			}
		}
		if (!found) {
			typeCombo.select(0);
		}
	}

	/**
	 * Enables / disables the type combo box
	 * 
	 * @param flag
	 *            new enabled flag
	 */
	void setEnabled(boolean flag) {
		typeCombo.setEnabled(flag);
	}

}
