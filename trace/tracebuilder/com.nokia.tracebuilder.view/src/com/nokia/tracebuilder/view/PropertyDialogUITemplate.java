/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Composite for template label and combo box
 *
 */
package com.nokia.tracebuilder.view;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Composite for template label and combo box
 * 
 */
final class PropertyDialogUITemplate extends PropertyDialogComposite {

	/**
	 * Number of visible templates when opening the combo box
	 */
	private static final int VISIBLE_TEMPLATES_COUNT = 8; // CodForChk_Dis_Magic

	/**
	 * Template label
	 */
	private Label templateLabel;

	/**
	 * Template selector
	 */
	private Combo templateCombo;

	/**
	 * List of templates
	 */
	private List<TraceObjectPropertyDialogTemplate> templates;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param uiType
	 *            the UI type
	 * @param templateListener
	 *            listener for template changes
	 * @param templates
	 *            the list of templates
	 */
	PropertyDialogUITemplate(Composite parent, int uiType,
			SelectionListener templateListener,
			List<TraceObjectPropertyDialogTemplate> templates) {
		super(parent);
		this.templates = templates;
		create(uiType, templateListener);
	}

	/**
	 * Creates the template combo box
	 * 
	 * @param uiType
	 *            type of UI
	 * @param templateListener
	 *            listener for template changes
	 */
	private void create(int uiType, SelectionListener templateListener) {
		templateLabel = new Label(getParent(), SWT.NONE);
		templateLabel.setText(Messages
				.getString("PropertyDialogUI.TemplatesLabel")); //$NON-NLS-1$
		templateCombo = new Combo(getParent(), SWT.READ_ONLY);
		templateCombo.add(Messages.getString("PropertyDialogUI.NoTemplate")); //$NON-NLS-1$
		templateCombo.select(0);

		// If selected trace is Performance trace and UI type is "Add parameter"
		// no templates can be selected
		if (templates != null
				&& !(uiType == TraceObjectPropertyDialog.ADD_PARAMETER && isPerformaceTraceSelected())) {
			for (int i = 0; i < templates.size(); i++) {
				templateCombo.add(templates.get(i).getTitle());
			}
		}
		templateCombo.addSelectionListener(templateListener);
		templateCombo.setVisibleItemCount(VISIBLE_TEMPLATES_COUNT);
		setFieldButtonLayoutData(templateCombo, null);
	}

	/**
	 * Gets the index of the selected template. Note that index 0 means "No
	 * template" and actual templates start from index 1
	 * 
	 * @return the index
	 */
	int getTemplateIndex() {
		return templateCombo.getSelectionIndex();
	}

	/**
	 * Gets the template at given index. Note that index 0 means "No template"
	 * and actual templates start from index 1
	 * 
	 * @param index
	 *            the index
	 * @return the template
	 */
	TraceObjectPropertyDialogTemplate getTemplateAt(int index) {
		return templates.get(index - 1);
	}

	/**
	 * Selects the given template. Retuns the index to the selected template.
	 * Note that index 0 means "No template" and actual templates start from
	 * index 1
	 * 
	 * @param template
	 *            the template to be selected
	 * @return index of the selected template
	 */
	int selectTemplate(TraceObjectPropertyDialogTemplate template) {
		int retval = -1;
		if (template != null && templates != null) {
			for (int i = 0; i < templates.size(); i++) {
				if (templates.get(i) == template) {
					templateCombo.select(i + 1);
					retval = i + 1;
					i = templates.size();
				}
			}
		}
		return retval;
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
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			if (trace.getGroup().getName().equals(
					groupNameHandler.getDefaultGroups()[groupNameHandler.getPerformanceGroupIdIndex()])) {
				retval = true;
			}
		}
		return retval;
	}
}
