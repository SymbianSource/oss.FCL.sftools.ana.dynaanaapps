/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
 * UI for property dialogs
 *
 */
package com.nokia.tracebuilder.view;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceParameterPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TracePropertyDialogTemplate;
import com.nokia.tracebuilder.engine.rules.PerformanceEventTemplate;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObjectUtils;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * Selection listener for template combo box
 * 
 */
final class TemplateComboSelectionListener implements SelectionListener {

	/**
	 * Dialog UI
	 */
	private final PropertyDialogUI dialogUI;

	/**
	 * Temporary storage for name
	 */
	private String tmpName;

	/**
	 * Temporary storage for value
	 */
	private String tmpValue;

	/**
	 * Temporary storage for target object
	 */
	private String tmpTarget;

	/**
	 * Temporary storage for type
	 */
	private String tmpType;

	/**
	 * Current selection
	 */
	private int selection = -1;

	/**
	 * Constructor
	 * 
	 * @param dialogUI
	 *            the dialog UI
	 */
	TemplateComboSelectionListener(PropertyDialogUI dialogUI) {
		this.dialogUI = dialogUI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#
	 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		templateSelected(((Combo) e.widget).getSelectionIndex());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#
	 *      widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * Selection event handler
	 * 
	 * @param newSelection
	 *            the selected template index
	 */
	void templateSelected(int newSelection) {
		int oldSelection = selection;
		selection = newSelection;
		if (selection != oldSelection) {
			// User-defined values are stored
			if (oldSelection == 0) {
				storeSelection();
			}
			if (selection == 0) {
				int dialogType = dialogUI.getUiType();
				if (dialogType == TraceObjectPropertyDialog.ADD_TRACE) {
					dialogUI.valueComposite.setLabel(Messages
							.getString("PropertyDialogUI.AddTraceValueLabel")); //$NON-NLS-1$
					dialogUI.valueComposite.getValueLabel().pack();
				}
				if (oldSelection >= 0) {
					loadSelection();
					dialogUI.notifyTemplateChanged(null);
				}
			} else {
				TraceObjectPropertyDialogTemplate template = setFromTemplate();
				dialogUI.notifyTemplateChanged(template);
			}
		} else {
			// If same template is 'selected' the values are reset
			if (selection > 0) {
				setFromTemplate();
			}
		}
	}

	/**
	 * Sets the values from selected template into the dialog
	 * 
	 * @return the selected template
	 */
	private TraceObjectPropertyDialogTemplate setFromTemplate() {
		TraceObjectPropertyDialogTemplate template = dialogUI
				.getTemplate(selection);
		String name = template.getName();
		if (name != null) {
			dialogUI.setNameField(name);
		}
		int dialogType = dialogUI.getUiType();
		if (dialogType == TraceObjectPropertyDialog.ADD_TRACE) {
			if (template instanceof PerformanceEventTemplate) {
				dialogUI.valueComposite.setLabel(Messages
						.getString("PropertyDialogUI.AddTraceEventNameLabel")); //$NON-NLS-1$
			} else {
				dialogUI.valueComposite.setLabel(Messages
						.getString("PropertyDialogUI.AddTraceValueLabel")); //$NON-NLS-1$
			}
			dialogUI.valueComposite.getValueLabel().pack();
		}
		if (template instanceof TraceParameterPropertyDialogTemplate) {
			dialogUI.setType(((TraceParameterPropertyDialogTemplate) template)
					.getType());
		}
		if (template instanceof TracePropertyDialogTemplate) {
			// If the template is a trace property template, the target group
			// name is passed to the template. The trace name is adjusted based
			// on the target group
			String groupName = ((TracePropertyDialogTemplate) template)
					.getGroupName();
			if (groupName == null) {
				groupName = dialogUI.getTarget();
			} else {
				dialogUI.setTarget(groupName);
			}
			if (groupName != null) {
				dialogUI.setValueField(((TracePropertyDialogTemplate) template)
						.getText(groupName));
			} else {
				dialogUI.setValueField(template.getValue());
			}
		} else {
			String value = template.getValue();
			if (value != null) {
				dialogUI.setValueField(value);
			}
		}
		return template;
	}

	/**
	 * Sets the values from the stored selection into the dialog
	 */
	private void loadSelection() {
		String name = null;
		String value = null;

		// If tmpName or tmpValue is null and context is valid, the name and
		// value proposal are based on context
		if ((tmpName == null || tmpName == "") //$NON-NLS-1$
				|| (tmpValue == null || tmpValue == "")) { //$NON-NLS-1$
			SourceContext context = TraceBuilderGlobals
					.getSourceContextManager().getContext();
			if (context != null) {
				String cname = context.getClassName();
				String fname = context.getFunctionName();
				name = TraceUtils.formatTrace(
						TraceUtils.getDefaultNameFormat(), cname, fname);
				TraceModel model = TraceBuilderGlobals.getTraceModel();
				name = TraceObjectUtils.modifyDuplicateTraceName(model,
						TraceUtils.convertName(name)).getData();
				value = TraceUtils.formatTrace(TraceUtils
						.getDefaultTraceFormat(), cname, fname);
			}
		}

		if (tmpName != null && tmpName != "") { //$NON-NLS-1$
			dialogUI.setNameField(tmpName);
		} else {
			if (name != null) {
				dialogUI.setNameField(name);
			} else {
				dialogUI.setNameField(""); //$NON-NLS-1$
			}
		}
		if (tmpValue != null && tmpValue != "") { //$NON-NLS-1$
			dialogUI.setValueField(tmpValue);
		} else {
			if (value != null) {
				dialogUI.setValueField(value);
			} else {
				dialogUI.setValueField(""); //$NON-NLS-1$
			}
		}
		if (tmpType != null) {
			dialogUI.setType(tmpType);
		} else {
			dialogUI.setType(""); //$NON-NLS-1$
		}
		if (tmpTarget != null) {
			dialogUI.setTarget(tmpTarget);
		} else {
			dialogUI.setTarget(""); //$NON-NLS-1$
		}
	}

	/**
	 * Stores the current dialog values
	 */
	private void storeSelection() {
		tmpTarget = dialogUI.getTarget();
		tmpName = dialogUI.getNameField();
		tmpValue = dialogUI.getValueField();
		tmpType = dialogUI.getType();
	}

}