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
* Control logic for property dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tracebuilder.engine.SourceContextManager;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderDialogs;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogConfiguration;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceObjectUtils;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.ReadOnlyObjectRule;
import com.nokia.tracebuilder.rules.TraceParameterRestrictionRule;

/**
 * Control logic for property dialog. Separated from TraceBuilder, since it was
 * getting too complex
 * 
 */
public class PropertyDialogEngine implements PropertyDialogConfigurationNames {

	/**
	 * Properties dialog is used to add and edit trace objects
	 */
	private TraceObjectPropertyDialog propertyDialog;

	/**
	 * Interface which is used to configure the property dialog
	 */
	private TraceObjectPropertyDialogConfiguration propertyDialogConfiguration;

	/**
	 * Property dialog verifier
	 */
	private PropertyDialogVerifier propertyDialogVerifier;

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param dialogConfiguration
	 *            dialog configuration interface
	 */
	public PropertyDialogEngine(TraceModel model,
			TraceObjectPropertyDialogConfiguration dialogConfiguration) {
		this.model = model;
		propertyDialogConfiguration = dialogConfiguration;
		propertyDialogVerifier = new PropertyDialogVerifier(model,
				propertyDialog);
	}

	/**
	 * Changes the property dialog
	 * 
	 * @param propertyDialog
	 *            the new property dialog
	 */
	public void setPropertyDialog(TraceObjectPropertyDialog propertyDialog) {
		this.propertyDialog = propertyDialog;
		propertyDialogVerifier.setPropertyDialog(propertyDialog);
	}

	/**
	 * Shows "Add Trace" dialog.
	 * 
	 * @param group
	 *            group proposal
	 * @param name
	 *            name proposal
	 * @param value
	 *            value proposal
	 * @param enabler
	 *            dialog enabler interface
	 * @return the new trace
	 */
	public Trace showAddTraceDialog(TraceGroup group, String name,
			String value, PropertyDialogEnabler enabler) {
		return showAddTraceDialog(group, name, value, null, enabler);
	}

	/**
	 * Shows "Add Trace" dialog.
	 * 
	 * @param group
	 *            group proposal
	 * @param name
	 *            name proposal
	 * @param value
	 *            value proposal
	 * @param extensions
	 *            extensions for the next object
	 * @param enabler
	 *            dialog enabler interface
	 * @return the new trace
	 */
	public Trace showAddTraceDialog(TraceGroup group, String name,
			String value, List<TraceModelExtension> extensions,
			PropertyDialogEnabler enabler) {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		// If a group has been selected, the selection is set to the dialog
		int id;
		if (group != null) {
			id = group.getNextTraceID();
		} else {
			id = 1;
		}
		if (name == null) {
			name = config.getText(PROPERTY_DIALOG_TRACE_NAME);
		}
		if (value == null) {
			value = config.getText(PROPERTY_DIALOG_TRACE_TEXT);
		}
		if (group == null) {
			group = model.findGroupByName(config
					.getText(PROPERTY_DIALOG_TRACE_GROUP));
		}
		name = TraceObjectUtils.modifyDuplicateTraceName(model,
				TraceUtils.convertName(name)).getData();
		setAddDialogDefaults(TraceObjectPropertyDialog.ADD_TRACE, id, name,
				value, group, enabler);
		propertyDialog.setEnabler(enabler);
		CreateTraceCallback callback = new CreateTraceCallback(model,
				extensions);
		showDialog(callback);
		return callback.getTrace();
	}

	/**
	 * Shows the instrumenter dialog
	 * 
	 * @param target
	 *            the selected group
	 * @param enabler
	 *            dialog enabler interface
	 * @param callback
	 *            callback processing the instrumentation
	 */
	public void showInstrumenterDialog(TraceGroup target,
			PropertyDialogEnabler enabler, RunInstrumenterCallback callback) {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		String name = config.getText(PROPERTY_DIALOG_INSTRUMENTER_NAME_FORMAT);
		if (name == null || name.length() == 0) {
			name = TraceUtils.getDefaultNameFormat();
		}
		String trace = config
				.getText(PROPERTY_DIALOG_INSTRUMENTER_TRACE_FORMAT);
		if (trace == null || trace.length() == 0) {
			trace = TraceUtils.getDefaultTraceFormat();
		}
		if (target == null) {
			target = model.findGroupByName(config
					.getText(PROPERTY_DIALOG_INSTRUMENTER_TRACE_GROUP));
		}
		setAddDialogDefaults(TraceObjectPropertyDialog.INSTRUMENTER, 0, name,
				trace, target, enabler);
		SourceContextManager manager = TraceBuilderGlobals
				.getSourceContextManager();
		manager.setInstrumenting(true);
		try {
			showDialog(callback);
		} finally {
			manager.setInstrumenting(false);
		}
	}

	/**
	 * Shows "Add Parameter" dialog
	 * 
	 * @param trace
	 *            owner of the new parameter
	 * @param enabler
	 *            dialog enabler interface
	 * @return the new parameter
	 * @throws TraceBuilderException
	 *             if dialog cannot be shown
	 */
	public TraceParameter showAddParameterDialog(Trace trace,
			PropertyDialogEnabler enabler) throws TraceBuilderException {
		TraceParameter retval = null;
		// Trace rules are checked first
		TraceParameterRestrictionRule restriction = trace
				.getExtension(TraceParameterRestrictionRule.class);
		ReadOnlyObjectRule readOnly = trace
				.getExtension(ReadOnlyObjectRule.class);
		if (readOnly == null
				&& (restriction == null || restriction.canAddParameters())) {
			int result = PropertyDialogCallback
					.showLocationConfirmationQuery(trace);
			if (result == TraceBuilderDialogs.OK) {
				retval = internalShowAddParameterDialog(trace, enabler);
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.PARAMETER_ADD_NOT_ALLOWED);
		}
		return retval;
	}

	/**
	 * Shows "Select component" dialog
	 */
	public void showSelectComponentDialog() {

		if (propertyDialog != null) {

			// Create enabler
			PropertyDialogEnabler enabler = new PropertyDialogEnabler(
					PropertyDialogEnabler.ENABLE_TARGET);
			TraceBuilderConfiguration config = TraceBuilderGlobals
					.getConfiguration();
			String name = config.getText(PROPERTY_DIALOG_COMPONENT_NAME);
			setAddDialogDefaults(TraceObjectPropertyDialog.SELECT_COMPONENT, 0,
					name, null, model, enabler);
			// Set default target to be same as previous software component
			propertyDialog.setTarget(TraceBuilderGlobals
					.getPreviousSoftwareComponentName());
			showDialog(new SelectComponentCallback(model));
		}
	}

	/**
	 * Shows "Add Parameter" dialog
	 * 
	 * @param trace
	 *            owner of the new parameter
	 * @param enabler
	 *            dialog enabler interface
	 * @return the new parameter
	 */
	private TraceParameter internalShowAddParameterDialog(Trace trace,
			PropertyDialogEnabler enabler) {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		int id = trace.getNextParameterID();
		// Parameter proposal defaults to last used value
		String name = config.getText(PROPERTY_DIALOG_PARAMETER_NAME);
		String type = config.getText(PROPERTY_DIALOG_PARAMETER_TYPE);
		setAddDialogDefaults(TraceObjectPropertyDialog.ADD_PARAMETER, id,
				TraceObjectUtils.modifyDuplicateParameterName(trace, name)
						.getData(), null, trace, enabler);
		propertyDialog.setValue(type);
		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		if (list != null && list.hasLocations()) {
			TraceLocation loc = (TraceLocation) list.iterator().next();
			if (loc.getParameterCount() > trace.getParameterCount()) {
				String proposal = loc.getParameter(trace.getParameterCount());
				if (proposal.length() > 0) {
					propertyDialog.setName(proposal);
					propertyDialog.setValue(proposal);
				}
			}
		}
		CreateParameterCallback callback = new CreateParameterCallback(model,
				trace);
		showDialog(callback);
		return callback.getParameter();
	}

	/**
	 * Shows "Add Constant" dialog
	 * 
	 * @param table
	 *            target constant table
	 * @param enabler
	 *            dialog enabler interface
	 */
	public void showAddConstantDialog(TraceConstantTable table,
			PropertyDialogEnabler enabler) {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		String name = config.getText(PROPERTY_DIALOG_CONSTANT_NAME);
		int id = 0;
		if (table != null) {
			id = table.getNextEntryID();
		}
		setAddDialogDefaults(TraceObjectPropertyDialog.ADD_CONSTANT, id,
				TraceObjectUtils.modifyDuplicateConstantName(table, name)
						.getData(), null, table, enabler);
		showDialog(new CreateConstantCallback(model));
	}

	/**
	 * Shows the "Edit Group" dialog
	 * 
	 * @param group
	 *            group to be edited
	 */
	public void showEditGroupDialog(TraceGroup group) {
		int result = PropertyDialogCallback
				.showLocationConfirmationQuery(group);
		if (result == TraceBuilderDialogs.OK) {
			setEditDialogDefaults(TraceObjectPropertyDialog.EDIT_GROUP, group);
			showDialog(new UpdateGroupCallback(model, group));
		}
	}

	/**
	 * Shows the "Edit Trace" dialog
	 * 
	 * @param trace
	 *            trace to be edited
	 */
	public void showEditTraceDialog(Trace trace) {
		int result = PropertyDialogCallback
				.showLocationConfirmationQuery(trace);
		if (result == TraceBuilderDialogs.OK) {
			setEditDialogDefaults(TraceObjectPropertyDialog.EDIT_TRACE, trace);
			propertyDialog.setValue(trace.getTrace());
			showDialog(new UpdateTraceCallback(model, trace));
		}
	}

	/**
	 * Shows the "Edit Constant Table" dialog
	 * 
	 * @param table
	 *            the table to be edited
	 */
	public void showEditConstantTableDialog(TraceConstantTable table) {
		setEditDialogDefaults(TraceObjectPropertyDialog.EDIT_CONSTANT_TABLE,
				table);
		showDialog(new UpdateConstantTableCallback(model, table));
	}

	/**
	 * Shows the "Edit Constant" dialog
	 * 
	 * @param entry
	 *            entry to be edited
	 */
	public void showEditConstantDialog(TraceConstantTableEntry entry) {
		setEditDialogDefaults(TraceObjectPropertyDialog.EDIT_CONSTANT, entry);
		showDialog(new UpdateConstantCallback(model, entry));
	}

	/**
	 * Sets the default values for the dialog
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param id
	 *            value for ID field
	 * @param name
	 *            value for name field
	 * @param value
	 *            value for value field
	 * @param target
	 *            the target object
	 * @param enabler
	 *            dialog enabler interface
	 */
	private void setAddDialogDefaults(int dialogType, int id, String name,
			String value, TraceObject target, PropertyDialogEnabler enabler) {
		ArrayList<TraceObjectPropertyDialogFlag> flags = initFlags(dialogType,
				enabler);
		ArrayList<TraceObjectPropertyDialogTemplate> templates = initTemplates(
				dialogType, enabler);
		TraceObjectPropertyDialogTemplate selectedTemplate = findTemplateByTitle(
				dialogType, templates);
		propertyDialog.setVerifier(propertyDialogVerifier);
		propertyDialog.setTargetObject(target);
		propertyDialog.setEnabler(enabler);
		propertyDialog.setDialogType(dialogType);
		propertyDialog.setFlags(flags);
		propertyDialog.setTemplates(templates, selectedTemplate);
		propertyDialog.setID(id);
		propertyDialog.setName(name);
		propertyDialog.setValue(value);
	}

	/**
	 * Creates the dialog templates array
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param enabler
	 *            the dialog enabler interface
	 * @return the templates array
	 */
	private ArrayList<TraceObjectPropertyDialogTemplate> initTemplates(
			int dialogType, PropertyDialogEnabler enabler) {
		ArrayList<TraceObjectPropertyDialogTemplate> templates;
		if (enabler == null || enabler.isTemplateEnabled()) {
			templates = new ArrayList<TraceObjectPropertyDialogTemplate>();
			addViewTemplates(dialogType, templates);
		} else {
			// If enabler interface exists and disables templates, the templates
			// are not added to the dialog
			templates = null;
		}
		return templates;
	}

	/**
	 * Creates the dialog flags array
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param enabler
	 *            dialog enabler interface
	 * @return the flags array
	 */
	private ArrayList<TraceObjectPropertyDialogFlag> initFlags(int dialogType,
			PropertyDialogEnabler enabler) {
		ArrayList<TraceObjectPropertyDialogFlag> flags;
		if (enabler == null || enabler.isFlagsEnabled()) {
			flags = new ArrayList<TraceObjectPropertyDialogFlag>();
			propertyDialogConfiguration.addViewFlags(flags, dialogType);
		} else {
			// If enabler interface exists and disables flags, the flags
			// are not added to the dialog
			flags = null;
		}
		if (flags != null) {
			String flagPrefix = null;
			switch (dialogType) {
			case TraceObjectPropertyDialog.ADD_TRACE:
				flagPrefix = PROPERTY_DIALOG_TRACE_FLAG;
				break;
			case TraceObjectPropertyDialog.ADD_PARAMETER:
				flagPrefix = PROPERTY_DIALOG_PARAMETER_FLAG;
				break;
			case TraceObjectPropertyDialog.ADD_CONSTANT:
				flagPrefix = PROPERTY_DIALOG_CONSTANT_FLAG;
				break;
			case TraceObjectPropertyDialog.INSTRUMENTER:
				flagPrefix = PROPERTY_DIALOG_INSTRUMENTER_FLAG;
				break;
			}
			// The flag values are set to what has been stored into preferences
			if (flagPrefix != null) {
				TraceBuilderConfiguration config = TraceBuilderGlobals
						.getConfiguration();
				for (TraceObjectPropertyDialogFlag flag : flags) {
					String flagName = flagPrefix + flag.getText();
					if (config.hasEntry(flagName)) {
						flag.setEnabled(config.getFlag(flagName));
					}
				}
			}
		}
		return flags;
	}

	/**
	 * Finds a template from given list by title stored into configuration
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param templates
	 *            the templates list
	 * @return the template that should be activated
	 */
	private TraceObjectPropertyDialogTemplate findTemplateByTitle(
			int dialogType, List<TraceObjectPropertyDialogTemplate> templates) {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		String templateTitle = null;
		switch (dialogType) {
		case TraceObjectPropertyDialog.ADD_TRACE:
			templateTitle = config.getText(PROPERTY_DIALOG_TRACE_TEMPLATE);
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			templateTitle = config.getText(PROPERTY_DIALOG_PARAMETER_TEMPLATE);
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			templateTitle = config.getText(PROPERTY_DIALOG_CONSTANT_TEMPLATE);
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			templateTitle = config
					.getText(PROPERTY_DIALOG_INSTRUMENTER_TEMPLATE);
			break;
		}
		// Selects the template which has the title stored into preferences
		TraceObjectPropertyDialogTemplate template = null;
		if (templateTitle != null && templates != null) {
			for (TraceObjectPropertyDialogTemplate temp : templates) {
				if (temp.getTitle().equals(templateTitle)) {
					template = temp;
					break;
				}
			}
		}
		return template;
	}

	/**
	 * Adds the templates to "Add" dialog
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param templates
	 *            the list of templates
	 */
	private void addViewTemplates(int dialogType,
			ArrayList<TraceObjectPropertyDialogTemplate> templates) {
		propertyDialogConfiguration.addViewTemplates(templates, dialogType);
		if (dialogType != TraceObjectPropertyDialog.INSTRUMENTER) {
			// Removes all templates that are not available in current context
			// Instrumenter affects multiple contexts, so templates are always
			// available at this point
			for (int i = 0; i < templates.size(); i++) {
				TraceObjectPropertyDialogTemplate template = templates.get(i);
				if (template instanceof ContextBasedTemplate) {
					if (!((ContextBasedTemplate) template)
							.isAvailableInContext(TraceBuilderGlobals
									.getSourceContextManager().getContext())) {
						templates.remove(i);
						i--;
					}
				}
			}
		}
	}

	/**
	 * Initializes the edit dialog with values from object
	 * 
	 * @param dialogType
	 *            the dialog type
	 * @param object
	 *            the object to be edited
	 */
	private void setEditDialogDefaults(int dialogType, TraceObject object) {
		ArrayList<TraceObjectPropertyDialogFlag> list = new ArrayList<TraceObjectPropertyDialogFlag>();
		propertyDialogConfiguration.addViewFlags(list, dialogType);
		propertyDialog.setVerifier(propertyDialogVerifier);
		propertyDialog.setTargetObject(object);
		propertyDialog.setEnabler(object
				.getExtension(TraceObjectPropertyDialogEnabler.class));
		propertyDialog.setDialogType(dialogType);
		propertyDialog.setFlags(list);
		propertyDialog.setTemplates(null, null);
		propertyDialog.setID(object.getID());
		propertyDialog.setName(object.getName());
		propertyDialog.setValue(null);
	}

	/**
	 * Shows the property dialog
	 * 
	 * @param callback
	 *            the callback for OK processing
	 */
	private void showDialog(PropertyDialogEngineCallback callback) {
		TraceBuilderErrorCode valid;
		do {
			int res = propertyDialog.open();
			if (res == TraceObjectPropertyDialog.OK) {
				valid = TraceBuilderErrorCode.OK;
				try {
					callback.okSelected(propertyDialog);
					saveData();
				} catch (TraceBuilderException e) {
					valid = (TraceBuilderErrorCode) e.getErrorCode();
					TraceBuilderGlobals.getEvents().postError(e);
				}
			} else {
				valid = TraceBuilderErrorCode.OK;
			}
		} while (valid != TraceBuilderErrorCode.OK);
	}

	/**
	 * Saves the data into properties for later use
	 */
	private void saveData() {
		TraceBuilderConfiguration config = TraceBuilderGlobals
				.getConfiguration();
		String templateTitle = null;
		TraceObjectPropertyDialogTemplate template = propertyDialog
				.getTemplate();
		// Template is saved based on localized title -> Not so good but works
		if (template != null) {
			templateTitle = template.getTitle();
		}
		if (templateTitle == null) {
			templateTitle = ""; //$NON-NLS-1$
		}
		String flagPrefix = getFlagPrefix(config, templateTitle);
		if (flagPrefix != null) {
			List<TraceObjectPropertyDialogFlag> flags = propertyDialog
					.getFlags();
			if (flags != null) {
				for (int i = 0; i < flags.size(); i++) {
					TraceObjectPropertyDialogFlag flag = flags.get(i);
					config.setFlag(flagPrefix + flag.getText(), flag
							.isEnabled());
				}
			}
		}
	}

	/**
	 * Gets the prefix for flags and saves other properties
	 * 
	 * @param config
	 *            the configuration
	 * @param templateTitle
	 *            the template title
	 * @return the prefix
	 */
	private String getFlagPrefix(TraceBuilderConfiguration config,
			String templateTitle) {
		String flagPrefix = null;
		// Each dialog type is separately saved
		switch (propertyDialog.getDialogType()) {
		case TraceObjectPropertyDialog.ADD_TRACE:
			config
					.setText(PROPERTY_DIALOG_TRACE_NAME, propertyDialog
							.getName());
			config.setText(PROPERTY_DIALOG_TRACE_TEXT, propertyDialog
					.getValue());
			config.setText(PROPERTY_DIALOG_TRACE_TEMPLATE, templateTitle);
			config.setText(PROPERTY_DIALOG_TRACE_GROUP, propertyDialog
					.getTarget());
			flagPrefix = PROPERTY_DIALOG_TRACE_FLAG;
			break;
		case TraceObjectPropertyDialog.ADD_PARAMETER:
			config.setText(PROPERTY_DIALOG_PARAMETER_NAME, propertyDialog
					.getName());
			config.setText(PROPERTY_DIALOG_PARAMETER_TYPE, propertyDialog
					.getValue());
			config.setText(PROPERTY_DIALOG_PARAMETER_TEMPLATE, templateTitle);
			flagPrefix = PROPERTY_DIALOG_PARAMETER_FLAG;
			break;
		case TraceObjectPropertyDialog.SELECT_COMPONENT:
			config.setText(PROPERTY_DIALOG_COMPONENT_NAME, propertyDialog
					.getName());
			config.setText(PROPERTY_DIALOG_SELECT_COMPONENT_TEMPLATE,
					templateTitle);
			flagPrefix = PROPERTY_DIALOG_COMPONENT_FLAG;
			break;
		case TraceObjectPropertyDialog.ADD_CONSTANT:
			config.setText(PROPERTY_DIALOG_CONSTANT_NAME, propertyDialog
					.getName());
			config.setText(PROPERTY_DIALOG_CONSTANT_TEMPLATE, templateTitle);
			flagPrefix = PROPERTY_DIALOG_CONSTANT_FLAG;
			break;
		case TraceObjectPropertyDialog.INSTRUMENTER:
			config.setText(PROPERTY_DIALOG_INSTRUMENTER_NAME_FORMAT,
					propertyDialog.getName());
			config.setText(PROPERTY_DIALOG_INSTRUMENTER_TRACE_FORMAT,
					propertyDialog.getValue());
			config
					.setText(PROPERTY_DIALOG_INSTRUMENTER_TEMPLATE,
							templateTitle);
			flagPrefix = PROPERTY_DIALOG_INSTRUMENTER_FLAG;
			config.setText(PROPERTY_DIALOG_INSTRUMENTER_TRACE_GROUP,
					propertyDialog.getTarget());
			break;
		}
		return flagPrefix;
	}
}