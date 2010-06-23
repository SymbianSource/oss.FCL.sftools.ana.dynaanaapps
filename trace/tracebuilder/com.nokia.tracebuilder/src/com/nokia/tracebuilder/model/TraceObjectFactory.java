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
 * Creates trace objects and provides configurable rules for trace object creation
 *
 */
package com.nokia.tracebuilder.model;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;

/**
 * Creates trace objects and provides configurable rules for trace object
 * creation
 * 
 */
public class TraceObjectFactory {

	/**
	 * The trace model
	 */
	private TraceModel model;

	/**
	 * Rule factory
	 */
	private TraceObjectRuleFactory ruleFactory;

	/**
	 * Creates a new factory
	 * 
	 * @param model
	 *            the model
	 * @param factory
	 *            the rule factory
	 */
	TraceObjectFactory(TraceModel model, TraceObjectRuleFactory factory) {
		this.model = model;
		this.ruleFactory = factory;
		processNewObjectRules(model);
		model.setComplete();
	}

	/**
	 * Gets the rule factory
	 * 
	 * @return the rule factory
	 */
	public TraceObjectRuleFactory getRuleFactory() {
		return ruleFactory;
	}

	/**
	 * Creates a new trace group
	 * 
	 * @param id
	 *            the group ID
	 * @param name
	 *            the name for the group
	 * @param extensions
	 *            list of extensions to be added to the group
	 * @return the new group
	 * @throws TraceBuilderException
	 */
	public TraceGroup createTraceGroup(int id, String name,
			TraceModelExtension[] extensions) throws TraceBuilderException {

		boolean isPredefineGroup = model.isPredefinedGroup(name);
		if (!isPredefineGroup
				&& model.getNumberOfUserdefinedGroups() >= TraceBuilderGlobals
						.getGroupNameHandler()
						.getMaxNumberOfUserDefinedGroups()) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.RUN_OUT_OF_USER_DEFINED_GROUP_IDS,
					true);
		}

		TraceGroup group = new TraceGroup(model);
		group.setID(id);
		group.setName(name);
		setExtensions(group, extensions);
		processNewObjectRules(group);
		group.setComplete();
		if (!isPredefineGroup) {
			model.increaseNumberOfUserDefinedGroups();
		}
		return group;
	}

	/**
	 * Creates a new trace
	 * 
	 * @param group
	 *            the trace group
	 * @param id
	 *            the id for the trace
	 * @param name
	 *            the trace name
	 * @param traceText
	 *            the trace text
	 * @param extensions
	 *            list of extensions to be added to the trace
	 * @return the new trace
	 */
	public Trace createTrace(TraceGroup group, int id, String name,
			String traceText, TraceModelExtension[] extensions) {
		Trace trace = new Trace(group);
		trace.setID(id);
		trace.setName(name);
		trace.setTrace(traceText);
		setExtensions(trace, extensions);
		processNewObjectRules(trace);
		trace.setComplete();
		return trace;
	}

	/**
	 * Creates a new trace parameter
	 * 
	 * @param trace
	 *            the trace the parameter is associated to
	 * @param id
	 *            the parameter ID
	 * @param name
	 *            the parameter name
	 * @param type
	 *            parameter type
	 * @param extensions
	 *            list of extensions to be added to the parameter
	 * @return the new parameter
	 */
	public TraceParameter createTraceParameter(Trace trace, int id,
			String name, String type, TraceModelExtension[] extensions) {
		TraceParameter parameter = new TraceParameter(trace);
		initializeParameter(parameter, id, name, type, extensions);
		return parameter;
	}

	/**
	 * Creates a new trace parameter inserting it into the specified index
	 * 
	 * @param objectIndex
	 *            the index for the object
	 * @param trace
	 *            the trace the parameter is associated to
	 * @param id
	 *            the parameter ID
	 * @param name
	 *            the parameter name
	 * @param type
	 *            parameter type
	 * @param extensions
	 *            list of extensions to be added to the parameter
	 * @return the new parameter
	 */
	public TraceParameter createTraceParameter(int objectIndex, Trace trace,
			int id, String name, String type, TraceModelExtension[] extensions) {
		TraceParameter parameter = new TraceParameter(trace, objectIndex);
		initializeParameter(parameter, id, name, type, extensions);
		return parameter;
	}

	/**
	 * Initializes a parameter
	 * 
	 * @param parameter
	 *            the parameter
	 * @param id
	 *            the parameter ID
	 * @param name
	 *            the parameter name
	 * @param type
	 *            parameter type
	 * @param extensions
	 *            list of extensions to be added to the parameter
	 */
	private void initializeParameter(TraceParameter parameter, int id,
			String name, String type, TraceModelExtension[] extensions) {
		parameter.setID(id);
		parameter.setName(name);
		parameter.setType(type);
		setExtensions(parameter, extensions);
		processNewObjectRules(parameter);
		parameter.setComplete();
	}

	/**
	 * Creates a new constant table
	 * 
	 * @param id
	 *            id for the table
	 * @param typeName
	 *            the name for the table
	 * @param extensions
	 *            list of extensions to be added to the table
	 * @return the constant table
	 */
	public TraceConstantTable createConstantTable(int id, String typeName,
			TraceModelExtension[] extensions) {
		TraceConstantTable table = new TraceConstantTable(model);
		table.setID(id);
		table.setName(typeName);
		setExtensions(table, extensions);
		processNewObjectRules(table);
		table.setComplete();
		for (TraceGroup group : model) {
			for (Trace trace : group) {
				for (TraceParameter param : trace) {
					if (param.getType().equals(typeName)) {
						table.addParameterReference(param);
					}
				}
			}
		}
		return table;
	}

	/**
	 * Creates a new constant table entry
	 * 
	 * @param table
	 *            constant table
	 * @param id
	 *            id for the entry
	 * @param value
	 *            value for the entry
	 * @param extensions
	 *            list of extensions to be added to the constant
	 * @return the constant table entry
	 */
	public TraceConstantTableEntry createConstantTableEntry(
			TraceConstantTable table, int id, String value,
			TraceModelExtension[] extensions) {
		TraceConstantTableEntry entry = new TraceConstantTableEntry(table);
		entry.setID(id);
		entry.setName(value);
		setExtensions(entry, extensions);
		processNewObjectRules(entry);
		entry.setComplete();
		return entry;
	}

	/**
	 * Creates an extension based on its name. This does not add the extension
	 * to the object, since the setData method should be called first
	 * 
	 * @param object
	 *            the target object for the extension
	 * @param name
	 *            the extension name
	 * @return the created extension
	 */
	public TraceModelPersistentExtension createExtension(TraceObject object,
			String name) {
		return ruleFactory.createExtension(object, name);
	}

	/**
	 * Adds extensions to given object
	 * 
	 * @param object
	 *            the object
	 * @param extensions
	 *            extensions to be added
	 */
	private void setExtensions(TraceObject object,
			TraceModelExtension[] extensions) {
		if (extensions != null) {
			for (TraceModelExtension element : extensions) {
				object.addExtension(element);
			}
		}
	}

	/**
	 * Processes the rules of a new object
	 * 
	 * @param object
	 *            the object to be processed
	 */
	private void processNewObjectRules(TraceObject object) {
		// Calls the factory to preprocess the extensions passed to
		// create-function
		ruleFactory.preProcessNewRules(object);
		// The rules may contain object creation rules
		Iterator<TraceObjectRule> rules = object
				.getExtensions(TraceObjectRule.class);
		while (rules.hasNext()) {
			TraceObjectRule rule = rules.next();
			if (rule instanceof TraceObjectRuleCreateObject) {
				TraceObjectRuleCreateObject createRule = (TraceObjectRuleCreateObject) rule;
				createRule.createObject();
			}
		}
		// Some rules are removed after the objects have been created
		Iterator<TraceObjectRuleRemoveOnCreate> itr;
		boolean changed;
		do {
			changed = false;
			itr = object.getExtensions(TraceObjectRuleRemoveOnCreate.class);
			while (itr.hasNext() && !changed) {
				TraceObjectRuleRemoveOnCreate ext = itr.next();
				if (ext.canBeRemoved()) {
					object.removeExtension(ext);
					changed = true;
				}
			}
		} while (changed);
		// After processing is complete, the rule factory is used to do
		// post-processing
		ruleFactory.postProcessNewRules(object);
	}

}