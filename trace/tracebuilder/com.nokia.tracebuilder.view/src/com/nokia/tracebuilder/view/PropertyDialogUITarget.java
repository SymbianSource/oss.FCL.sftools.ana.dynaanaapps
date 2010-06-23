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
 * Composite for target label and combo box
 *
 */
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tracebuilder.engine.SoftwareComponent;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.rules.RuleUtils;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.project.FormattingUtils;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Composite for target label and combo box
 * 
 */
final class PropertyDialogUITarget extends PropertyDialogComposite {

	/**
	 * Target label
	 */
	private Label targetLabel = null;

	/**
	 * Target selector
	 */
	private Combo targetCombo = null;

	/**
	 * Property dialog UI
	 */
	private PropertyDialogUI dialogUI;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param uiType
	 *            the type of UI
	 * @param modifyListener
	 *            combobox text change listener
	 * @param selectionListener
	 *            combobox selection change listener
	 */
	PropertyDialogUITarget(Composite parent, int uiType,
			ModifyListener modifyListener, SelectionListener selectionListener) {
		super(parent);
		this.dialogUI = (PropertyDialogUI) parent;
		create(uiType, modifyListener, selectionListener);
	}

	/**
	 * Creates the label and combo box
	 * 
	 * @param uiType
	 *            the UI type
	 * @param modifyListener
	 *            combobox text change listener
	 * @param selectionListener
	 *            combobox selection change listener
	 */
	private void create(int uiType, ModifyListener modifyListener,
			SelectionListener selectionListener) {
		targetLabel = new Label(getParent(), SWT.NONE);

		if (uiType == TraceObjectPropertyDialog.SELECT_COMPONENT) {
			targetCombo = new Combo(getParent(), SWT.READ_ONLY);
		} else {
			targetCombo = new Combo(getParent(), SWT.NONE);
		}

		int selid = 0;
		String text;
		if (uiType == TraceObjectPropertyDialog.SELECT_COMPONENT) {
			text = Messages
					.getString("PropertyDialogUI.SelectComponentNameLabel"); //$NON-NLS-1$	
			Iterator<SoftwareComponent> components = TraceBuilderGlobals
					.getSoftwareComponents();
			while (components.hasNext()) {
				SoftwareComponent component = components.next();
				targetCombo.add(component.getName());
			}
		} else if (uiType == TraceObjectPropertyDialog.ADD_CONSTANT) {
			text = Messages
					.getString("PropertyDialogUITarget.AddConstantTargetLabel"); //$NON-NLS-1$
			Iterator<TraceConstantTable> tables = TraceBuilderGlobals
					.getTraceModel().getConstantTables();
			while (tables.hasNext()) {
				targetCombo.add(tables.next().getName());
			}
		} else {
			text = Messages.getString("PropertyDialogUI.AddTraceTargetLabel"); //$NON-NLS-1$
			Iterator<String> groups = FormattingUtils
					.getGroupNames(TraceBuilderGlobals.getTraceModel());
			while (groups.hasNext()) {
				String group = groups.next();

				// TRACE_STATE and TRACE_PERFORMACE group names are not added
				// to combo, those can be used only via templates
				GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
				String[] defaultGroups = groupNameHandler.getDefaultGroups();
				if (group != defaultGroups[groupNameHandler.getStateGroupIdIndex()]
						&& group != defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()]) {
					targetCombo.add(group);
				}
			}
		}

		targetLabel.setText(text);
		targetCombo.select(selid);
		targetCombo.addSelectionListener(selectionListener);
		targetCombo.addModifyListener(modifyListener);
		setFieldButtonLayoutData(targetCombo, null);
	}

	/**
	 * Gets the target trace object
	 * 
	 * @return the target
	 */
	String getTarget() {
		return targetCombo.getText();
	}

	/**
	 * Sets the target object
	 * 
	 * @param target
	 *            the new target
	 */
	void setTarget(String target) {
		int found = -1;
		for (int i = 0; i < targetCombo.getItemCount() && found == -1; i++) {
			if (targetCombo.getItem(i).equals(target)) {
				found = i;
			}
		}
		int uiType = dialogUI.getUiType();
		if (found >= 0) {
			targetCombo.select(found);
		} else if (uiType == TraceObjectPropertyDialog.ADD_PARAMETER
				|| uiType == TraceObjectPropertyDialog.ADD_TRACE
				|| uiType == TraceObjectPropertyDialog.INSTRUMENTER) {
			int templateIndex = dialogUI.templateComposite.getTemplateIndex();
			GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
			String[] defaultGroups = groupNameHandler.getDefaultGroups();
			if (((!target
					.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()]) && !target
					.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()])) && templateIndex == 0)
					|| (target
							.equals(defaultGroups[groupNameHandler.getStateGroupIdIndex()]) && templateIndex == RuleUtils.TYPE_STATE_TRACE + 1)
					|| (target
							.equals(defaultGroups[groupNameHandler.getPerformanceGroupIdIndex()]) && templateIndex == RuleUtils.TYPE_PERF_EVENT + 1)
					|| (target
							.equals(defaultGroups[groupNameHandler.getFlowGroupIdIndex()]) && templateIndex == RuleUtils.TYPE_ENTRY_EXIT + 1)) {
				targetCombo.setText(target);
			} else {
				targetCombo.select(groupNameHandler.getNormalGroupIdIndex() - 1);
			}
		}
	}

	/**
	 * Enables / disables the target field
	 * 
	 * @param flag
	 *            new enabled state
	 */
	void setEnabled(boolean flag) {
		targetCombo.setEnabled(flag);
	}
}
