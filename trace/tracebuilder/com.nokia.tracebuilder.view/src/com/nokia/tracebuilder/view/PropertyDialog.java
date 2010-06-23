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
* Property dialog for TraceObject
*
*/
package com.nokia.tracebuilder.view;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceParameterPropertyDialogDynamicFlag;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Property dialog for TraceObject
 * 
 */
class PropertyDialog extends TitleAreaDialog {

	/**
	 * Callback for dialog UI
	 * 
	 */
	private final class UIChangeCallback implements
			PropertyDialogUIChangeCallback {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.view.PropertyDialogUIChangeCallback#
		 *      templateChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
		 */
		public void templateChanged(TraceObjectPropertyDialogTemplate template) {
			PropertyDialog.this.templateChanged(template, false);
			verifyContents(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.view.PropertyDialogUIChangeCallback#fieldChanged()
		 */
		public void fieldChanged() {
			verifyContents(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.view.PropertyDialogUIChangeCallback#targetChanged(java.lang.String)
		 */
		public void targetChanged(String target) {
			properties.setTarget(target);
			updateTitle();
			verifyContents(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tracebuilder.view.PropertyDialogUIChangeCallback#typeChanged(java.lang.String)
		 */
		public void typeChanged(String type) {
			properties.setValue(type);
			verifyContents(true);
			PropertyDialog.this.typeChanged(type);
		}
	}

	/**
	 * Selection listener for flag buttons
	 * 
	 */
	private final class FlagButtonSelectionListener implements
			SelectionListener {

		/**
		 * The flag to be monitored
		 */
		private TraceObjectPropertyDialogFlag flag;

		/**
		 * Constructor
		 * 
		 * @param flag
		 *            the flag to be monitored
		 */
		FlagButtonSelectionListener(TraceObjectPropertyDialogFlag flag) {
			this.flag = flag;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#
		 *      widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			flag.setEnabled(((Button) e.widget).getSelection());
			flagChanged(flag);
		}
	}

	/**
	 * Dialog margin
	 */
	private static final int MARGIN_WIDTH = 5; // CodForChk_Dis_Magic

	/**
	 * Dialog UI
	 */
	private PropertyDialogUI ui;

	/**
	 * Dialog properties
	 */
	private PropertyDialogProperties properties;

	/**
	 * Buttons for flags
	 */
	private Button[] flagButtons;

	/**
	 * Dialog constructor
	 * 
	 * @param shell
	 *            the shell where the dialog is associated to
	 * @param properties
	 *            the dialog properties
	 */
	PropertyDialog(Shell shell, PropertyDialogProperties properties) {
		super(shell);
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#
	 *      createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		// Registers the help context to the root control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				control,
				TraceViewHelp.getPropertyDialogContext(properties
						.getDialogType()));
		parent.pack();
		Point size = parent.getSize();
		Point location = getInitialLocation(size);
		parent.setBounds(getConstrainedShellBounds(new Rectangle(location.x,
				location.y, size.x, size.y)));
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		ui = new PropertyDialogUI(composite, properties.getDialogType(),
				properties.getTemplates());
		createFlagButtons(composite);
		loadProperties();
		updateTitle();
		TraceObjectPropertyDialogTemplate template = properties.getTemplate();
		ui.setTemplate(template);
		ui.setChangeCallback(new UIChangeCallback());
		// The dynamic flags are notified and thus each can initialize itself to
		// correct state
		templateChanged(template, true);
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null) {
			for (int i = 0; i < flags.size(); i++) {
				flagChanged(flags.get(i));
			}
		}
		// After all initialization has been done, the enabler is used to
		// disabler UI elements that should not be modified
		ui.setEnabler(properties.getEnabler());
		return composite;
	}

	/**
	 * Creates the check-boxes for flags
	 * 
	 * @param composite
	 *            the owner for the check-boxes
	 */
	private void createFlagButtons(Composite composite) {
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null && flags.size() > 0) {
			Composite flagContainer = new Composite(composite, SWT.NONE);
			RowLayout layout = new RowLayout(SWT.VERTICAL);
			layout.marginWidth = MARGIN_WIDTH;
			flagContainer.setLayout(layout);
			flagButtons = new Button[flags.size()];
			for (int i = 0; i < flags.size(); i++) {
				createFlagButton(flags, flagContainer, i);
			}
		}
	}

	/**
	 * Creates a flag button
	 * 
	 * @param flags
	 *            the flags list
	 * @param flagContainer
	 *            the container
	 * @param i
	 *            the list index
	 */
	private void createFlagButton(List<TraceObjectPropertyDialogFlag> flags,
			Composite flagContainer, int i) {
		TraceObjectPropertyDialogFlag flag = flags.get(i);
		if (flag.isVisible()) {
			flagButtons[i] = new Button(flagContainer, SWT.CHECK);
			flagButtons[i].setText(flag.getText());
			flagButtons[i]
					.addSelectionListener(new FlagButtonSelectionListener(flag));
			flagButtons[i].setSelection(flag.isEnabled());
		}
	}

	/**
	 * Template change notification
	 * 
	 * @param template
	 *            the new template
	 * @param forceUiUpdate
	 *            forces UI update even if flag did not change
	 */
	void templateChanged(TraceObjectPropertyDialogTemplate template,
			boolean forceUiUpdate) {
		// When a template changes, the dynamic flags are updated
		properties.setTemplate(template);
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null) {
			TraceObjectPropertyDialogDynamicFlag dynflag;
			for (int i = 0; i < flags.size(); i++) {
				TraceObjectPropertyDialogFlag flag = flags.get(i);
				if (flag instanceof TraceObjectPropertyDialogDynamicFlag
						&& flagButtons[i] != null) {
					// Notifies all dynamic flags with the change in template
					dynflag = (TraceObjectPropertyDialogDynamicFlag) flag;
					if (dynflag.templateChanged(template) || forceUiUpdate) {
						updateDynamicFlag(i, dynflag);
					}
				}
			}
		}
		typeChanged(ui.getType());
	}

	/**
	 * Parameter type change notification
	 * 
	 * @param type
	 *            the new type
	 */
	void typeChanged(String type) {
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null) {
			TraceParameterPropertyDialogDynamicFlag dynflag;
			for (int i = 0; i < flags.size(); i++) {
				TraceObjectPropertyDialogFlag flag = flags.get(i);
				if (flag instanceof TraceParameterPropertyDialogDynamicFlag
						&& flagButtons[i] != null) {
					// Notifies all dynamic parameter flags with the change
					dynflag = (TraceParameterPropertyDialogDynamicFlag) flag;
					if (dynflag.typeChanged(type)) {
						updateDynamicFlag(i, dynflag);
					}
				}
			}
		}
	}

	/**
	 * Updates the flag button of a dynamic flag
	 * 
	 * @param i
	 *            the flag index
	 * @param dynflag
	 *            the flag
	 */
	private void updateDynamicFlag(int i,
			TraceObjectPropertyDialogDynamicFlag dynflag) {
		if (dynflag.isAvailable()) {
			flagButtons[i].setSelection(dynflag.isEnabled());
			flagButtons[i].setEnabled(true);
		} else {
			flagButtons[i].setSelection(dynflag.isAlwaysEnabled());
			flagButtons[i].setEnabled(false);
		}
	}

	/**
	 * Flag change notification
	 * 
	 * @param flag
	 *            the flag that was changed
	 */
	void flagChanged(TraceObjectPropertyDialogFlag flag) {
		// When a flag changes, other flags are updated
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null) {
			TraceObjectPropertyDialogDynamicFlag dynflag;
			for (int i = 0; i < flags.size(); i++) {
				TraceObjectPropertyDialogFlag otherFlag = flags.get(i);
				if (otherFlag instanceof TraceObjectPropertyDialogDynamicFlag
						&& otherFlag != flag && flagButtons[i] != null) {
					// Notifies all dynamic flags with the change in template
					dynflag = (TraceObjectPropertyDialogDynamicFlag) otherFlag;
					if (dynflag.flagChanged(flag)) {
						updateDynamicFlag(i, dynflag);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		verifyContents(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		ui = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		boolean save = saveProperties();
		if (TraceBuilderConfiguration.ASSERTIONS_ENABLED && !save) {
			TraceBuilderGlobals.getEvents().postAssertionFailed(
					"Save dialog properties failed -> Object might be invalid", //$NON-NLS-1$
					null);
		}
		checkFlags();
		super.okPressed();
	}

	/**
	 * Loads the properties to UI
	 */
	private void loadProperties() {
		ui.setNameField(properties.getName());
		ui.setIDField(properties.getID());
		int dialogType = properties.getDialogType();

		// In case of select component, type and name fields are not used
		if (dialogType != TraceObjectPropertyDialog.SELECT_COMPONENT) {
			if (dialogType == TraceObjectPropertyDialog.ADD_PARAMETER) {
				// In case of parameters value is mapped to the type selector
				ui.setType(properties.getValue());
			} else {
				// Otherwise the value is mapped to the value field
				ui.setValueField(properties.getValue());
			}
		}
		ui.setTarget(properties.getTarget());
	}

	/**
	 * Saves the properties from the UI to the properties object
	 * 
	 * @return true if saved, false if failed
	 */
	private boolean saveProperties() {
		boolean retval;
		try {
			properties.setID(ui.getIDField());
			properties.setName(ui.getNameField());
			if (properties.getDialogType() == TraceObjectPropertyDialog.ADD_PARAMETER) {
				properties.setValue(ui.getType());
			} else {
				properties.setValue(ui.getValueField());
			}
			properties.setTarget(ui.getTarget());
			retval = true;
		} catch (Exception e) {
			setErrorMessage(Messages.getString("PropertyDialog.InvalidId")); //$NON-NLS-1$
			retval = false;
		}
		return retval;
	}

	/**
	 * Changes the state of flags depending on the check boxes
	 */
	private void checkFlags() {
		// When user changes a template or updates a flag state, some flags may
		// be disabled in the UI. However, the enabled state of the flag is not
		// changed when that happens. This allows the checkbox to be set to
		// correct selection if it is re-enabled.
		// When OK button is pressed, the enabled state of flags needs to be
		// synchronized with the button selection state
		List<TraceObjectPropertyDialogFlag> flags = properties.getFlags();
		if (flags != null) {
			for (int i = 0; i < flags.size(); i++) {
				if (flagButtons[i] != null) {
					flags.get(i).setEnabled(flagButtons[i].getSelection());
				}
			}
		}
	}

	/**
	 * Uses the verification callback to verify dialog contents
	 * 
	 * @param setError
	 *            true if error needs to be set to dialog, false if not
	 */
	private void verifyContents(boolean setError) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled;
			if (saveProperties()) {
				try {
					properties.verifyContents();
					setErrorMessage(null);
					enabled = true;
				} catch (TraceBuilderException e) {
					if (setError) {
						setErrorMessage(TraceBuilderErrorMessages
								.getErrorMessage((TraceBuilderErrorCode) e
										.getErrorCode(), e.getErrorParameters()));
					}
					enabled = false;
				}
			} else {
				enabled = false;
			}
			okButton.setEnabled(enabled);
		}
	}

	/**
	 * Updates the title of this dialog
	 */
	private void updateTitle() {
		getShell().setText(
				TraceViewMessages.getPropertyDialogCaption(properties
						.getDialogType()));
		setTitle(TraceViewMessages.getPropertyDialogTitle(properties
				.getDialogType()));
		setMessage(TraceViewMessages.getPropertyDialogMessage(properties
				.getDialogType(), properties.getTarget()));
	}

}
