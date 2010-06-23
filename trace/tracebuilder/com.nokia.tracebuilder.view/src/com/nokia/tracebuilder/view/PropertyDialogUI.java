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
* UI for property dialogs
*
*/
package com.nokia.tracebuilder.view;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceParameterPropertyDialogTemplate;

/**
 * UI for property dialogs
 * 
 */
final class PropertyDialogUI extends Composite {

	/**
	 * Target combo-box selection listener
	 * 
	 */
	private final class TargetSelectionListener implements SelectionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			if (changeCallback != null) {
				changeCallback.targetChanged(getTarget());
			}
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
	}

	/**
	 * Target combo-box modify listener
	 * 
	 */
	private final class TargetFieldModifyListener implements ModifyListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			if (changeCallback != null) {
				changeCallback.targetChanged(getTarget());
			}
		}
	}

	/**
	 * Type combo-box selection listener
	 * 
	 */
	private final class TypeSelectionListener implements SelectionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			if (changeCallback != null) {
				changeCallback.typeChanged(getType());
			}
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
	}

	/**
	 * Text field modification listener
	 * 
	 */
	private final class TextFieldModifyListener implements ModifyListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			if (changeCallback != null) {
				changeCallback.fieldChanged();
			}
		}
	}

	/**
	 * Stores target object if combo is not created
	 */
	private String dummyTarget;

	/**
	 * Stores ID if ID field is not created
	 */
	private int dummyID;

	/**
	 * Stores name if name field is not created
	 */
	private String dummyName;

	/**
	 * Stores value if value field is not created
	 */
	private String dummyValue;

	/**
	 * Stores type if type combo is not created
	 */
	private String dummyType;

	/**
	 * Target composite
	 */
	private PropertyDialogUITarget targetComposite;

	/**
	 * ID composite
	 */
	private PropertyDialogUIID idComposite;

	/**
	 * Name composite
	 */
	private PropertyDialogUIName nameComposite;

	/**
	 * Type composite
	 */
	private PropertyDialogUIType typeComposite;

	/**
	 * Value composite
	 */
	public PropertyDialogUIValue valueComposite;

	/**
	 * Template composite
	 */
	public PropertyDialogUITemplate templateComposite;

	/**
	 * Callback to dialog
	 */
	private PropertyDialogUIChangeCallback changeCallback;

	/**
	 * Template combo box listener
	 */
	private TemplateComboSelectionListener templateListener;

	/**
	 * Text field modification listener
	 */
	private ModifyListener textFieldModifyListener;

	/**
	 * Ui type
	 */
	private int uiType;
	
	/**
	 * Number of columns in UI
	 */
	private static final int COLUMN_COUNT = 2; // CodForChk_Dis_Magic

	/**
	 * Creates a dialog UI with given parent component
	 * 
	 * @param parent
	 *            the parent composite
	 * @param uiType
	 *            the UI type
	 * @param templates
	 *            the dialog templates
	 */
	PropertyDialogUI(Composite parent, int uiType,
			List<TraceObjectPropertyDialogTemplate> templates) {
		super(parent, SWT.NONE);
		initialize(uiType, templates);
	}

	/**
	 * Initializes the member widgets
	 * 
	 * @param uiType
	 *            the UI type
	 * @param templates
	 *            the templates list
	 */
	private void initialize(int uiType,
			List<TraceObjectPropertyDialogTemplate> templates) {
		this.uiType = uiType;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = COLUMN_COUNT;
		this.setLayout(gridLayout);
		if (uiType == TraceObjectPropertyDialog.ADD_TRACE
				|| uiType == TraceObjectPropertyDialog.ADD_CONSTANT
				|| uiType == TraceObjectPropertyDialog.INSTRUMENTER
				|| uiType == TraceObjectPropertyDialog.SELECT_COMPONENT) {
			// Target is needed when adding traces or constants
			targetComposite = new PropertyDialogUITarget(this, uiType,
					new TargetFieldModifyListener(),
					new TargetSelectionListener());
		}

		if (uiType != TraceObjectPropertyDialog.SELECT_COMPONENT) {
			textFieldModifyListener = new TextFieldModifyListener();
			nameComposite = new PropertyDialogUIName(this, uiType,
					textFieldModifyListener);
		}
		if (uiType == TraceObjectPropertyDialog.ADD_TRACE
				|| uiType == TraceObjectPropertyDialog.EDIT_TRACE
				|| uiType == TraceObjectPropertyDialog.INSTRUMENTER) {
			// Trace text is the only value
			valueComposite = new PropertyDialogUIValue(this, uiType,
					textFieldModifyListener);
		}
		if (uiType == TraceObjectPropertyDialog.ADD_CONSTANT
				|| uiType == TraceObjectPropertyDialog.EDIT_CONSTANT) {
			// Constants have user-defined ID's. Other ID's are managed by
			// builder
			idComposite = new PropertyDialogUIID(this, uiType,
					textFieldModifyListener);
		}
		if (uiType == TraceObjectPropertyDialog.ADD_PARAMETER) {
			// Type is needed by parameters
			typeComposite = new PropertyDialogUIType(this, uiType,
					new TypeSelectionListener());
		}
		if (uiType == TraceObjectPropertyDialog.ADD_PARAMETER
				|| uiType == TraceObjectPropertyDialog.ADD_TRACE
				|| uiType == TraceObjectPropertyDialog.INSTRUMENTER) {
			templateListener = new TemplateComboSelectionListener(this);
			templateComposite = new PropertyDialogUITemplate(this, uiType,
					templateListener, templates);
		}
	}

	/**
	 * Gets the target object
	 * 
	 * @return the target
	 */
	String getTarget() {
		String target;
		if (targetComposite != null) {
			target = targetComposite.getTarget();
		} else {
			target = dummyTarget;
		}
		return target;
	}

	/**
	 * Selects the target which has given name
	 * 
	 * @param target
	 *            the target to be selected
	 */
	void setTarget(String target) {
		if (targetComposite != null && target != null) {
			targetComposite.setTarget(target);
		} else {
			dummyTarget = target;
		}
	}

	/**
	 * Sets the ID field value
	 * 
	 * @param id
	 *            the ID field value
	 */
	void setIDField(int id) {
		if (idComposite != null) {
			idComposite.setID(id);
		} else {
			dummyID = id;
		}
	}

	/**
	 * Gets the ID field value
	 * 
	 * @return the ID field value
	 */
	int getIDField() {
		int retval;
		if (idComposite != null) {
			retval = idComposite.getID();
		} else {
			retval = dummyID;
		}
		return retval;
	}

	/**
	 * Sets the contents of the name field
	 * 
	 * @param name
	 *            the name field
	 */
	void setNameField(String name) {
		if (nameComposite != null) {
			if (name != null) {
				nameComposite.setName(name);
			} else {
				nameComposite.setName(""); //$NON-NLS-1$
			}
		} else {
			dummyName = name;
		}
	}

	/**
	 * Returns the contents of the name field
	 * 
	 * @return the name field
	 */
	String getNameField() {
		String name;
		if (nameComposite != null) {
			name = nameComposite.getName();
		} else {
			name = dummyName;
		}
		return name;
	}

	/**
	 * Sets the contents of the value field.
	 * 
	 * @param value
	 *            the field contents
	 */
	void setValueField(String value) {
		if (valueComposite != null) {
			if (value != null) {
				valueComposite.setValue(value);
			} else {
				valueComposite.setValue(""); //$NON-NLS-1$
			}
		} else {
			dummyValue = value;
		}
	}

	/**
	 * Gets the value field contents
	 * 
	 * @return value field contents
	 */
	String getValueField() {
		String ret;
		if (valueComposite != null) {
			ret = valueComposite.getValue();
		} else {
			ret = dummyValue;
		}
		return ret;
	}

	/**
	 * Sets the type
	 * 
	 * @param type
	 *            the type
	 */
	void setType(String type) {
		if (typeComposite != null) {
			typeComposite.setType(type);
		} else {
			dummyType = type;
		}
	}

	/**
	 * Gets the type
	 * 
	 * @return the type
	 */
	String getType() {
		String type;
		if (typeComposite != null) {
			type = typeComposite.getSelectedType();
		} else {
			type = dummyType;
		}
		return type;
	}

	/**
	 * Gets the UI type
	 * 
	 * @return the UI type
	 */
	int getUiType() {
		return uiType;
	}
	
	/**
	 * Gets the template at given index
	 * 
	 * @param index
	 *            the index to the template
	 * @return the template
	 */
	TraceObjectPropertyDialogTemplate getTemplate(int index) {
		return templateComposite.getTemplateAt(index);
	}

	/**
	 * Called by the listener when template changes
	 * 
	 * @param template
	 *            the new template
	 */
	void notifyTemplateChanged(TraceObjectPropertyDialogTemplate template) {
		if (targetComposite != null) {
			targetComposite.setEnabled(template == null
					|| template.isTargetEnabled());
		}
		if (idComposite != null) {
			idComposite.setEnabled(template == null || template.isIDEnabled());
		}
		if (valueComposite != null) {
			valueComposite.setEnabled(template == null
					|| template.isValueEnabled());
		}
		if (nameComposite != null) {
			nameComposite.setEnabled(template == null
					|| template.isNameEnabled());
		}
		if (typeComposite != null) {
			boolean typeEnabled = !(template instanceof TraceParameterPropertyDialogTemplate)
					|| ((TraceParameterPropertyDialogTemplate) template)
							.isTypeEnabled();
			typeComposite.setEnabled(typeEnabled);
		}
		if (changeCallback != null) {
			changeCallback.templateChanged(template);
		}
	}

	/**
	 * Selects a template
	 * 
	 * @param template
	 *            the template to be selected
	 */
	void setTemplate(TraceObjectPropertyDialogTemplate template) {
		if (templateComposite != null) {
			int index = templateComposite.selectTemplate(template);
			if (templateListener != null) {
				if (index < 0) {
					templateListener.templateSelected(0);
				} else {
					templateListener.templateSelected(index);
				}
			}
		}
	}

	/**
	 * Disables all UI elements flagged to be disabled
	 * 
	 * @param enabler
	 *            the enabler interface
	 */
	void setEnabler(TraceObjectPropertyDialogEnabler enabler) {
		if (enabler != null) {
			if (nameComposite != null && !enabler.isNameEnabled()) {
				nameComposite.setEnabled(false);
			}
			if (targetComposite != null && !enabler.isTargetEnabled()) {
				targetComposite.setEnabled(false);
			}
			if (idComposite != null && !enabler.isIdEnabled()) {
				idComposite.setEnabled(false);
			}
			if (typeComposite != null && !enabler.isTypeEnabled()) {
				typeComposite.setEnabled(false);
			}
			if (valueComposite != null && !enabler.isValueEnabled()) {
				valueComposite.setEnabled(false);
			}
		}
	}

	/**
	 * Sets the change notification callback
	 * 
	 * @param callback
	 *            the callback
	 */
	void setChangeCallback(PropertyDialogUIChangeCallback callback) {
		this.changeCallback = callback;

	}

}
