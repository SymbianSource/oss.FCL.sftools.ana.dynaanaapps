/*
* Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
* Property verifier for trace objects
*
*/
package com.nokia.tracebuilder.engine.plugin;

import java.io.File;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.FileErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.RangeErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObjectPropertyVerifier;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.plugin.TraceBuilderPlugin;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceUtils;

/**
 * Property verifier for trace objects
 * 
 */
final class PluginTracePropertyVerifier implements TraceObjectPropertyVerifier {

	/**
	 * Valid data character range start
	 */
	private static final int DATA_CHAR_START = 0x20; // CodForChk_Dis_Magic

	/**
	 * Valid data character range end
	 */
	private static final int DATA_CHAR_END = 0x7E; // CodForChk_Dis_Magic

	/**
	 * Plugin engine
	 */
	private PluginEngine pluginEngine;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            plug-in engine
	 */
	PluginTracePropertyVerifier(PluginEngine engine) {
		this.pluginEngine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkConstantProperties(com.nokia.tracebuilder.model.TraceConstantTable,
	 *      com.nokia.tracebuilder.model.TraceConstantTableEntry, int,
	 *      java.lang.String)
	 */
	public void checkConstantProperties(TraceConstantTable table,
			TraceConstantTableEntry entry, int id, String value)
			throws TraceBuilderException {
		if (!SourceUtils.isValidName(value)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_CONSTANT_VALUE);
		}
		if (table != null) {
			// If table exists, the value and ID must be unique
			TraceConstantTableEntry old = table.findEntryByID(id);
			if (old != null && old != entry) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.DUPLICATE_CONSTANT_ID);
			}
			old = table.findEntryByName(value);
			if (old != null && old != entry) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.DUPLICATE_CONSTANT_VALUE);
			}
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider)
						.checkConstantProperties(table, entry, id, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkConstantTableProperties(com.nokia.tracebuilder.model.TraceModel,
	 *      com.nokia.tracebuilder.model.TraceConstantTable, int,
	 *      java.lang.String)
	 */
	public void checkConstantTableProperties(TraceModel model,
			TraceConstantTable table, int id, String tableName)
			throws TraceBuilderException {
		if (!SourceUtils.isValidName(tableName)) {
			 throw new TraceBuilderException(
			 TraceBuilderErrorCode.INVALID_CONSTANT_TABLE_NAME, false);
		}
		TraceConstantTable old = model.findConstantTableByID(id);
		if (old != null && old != table) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.DUPLICATE_CONSTANT_TABLE_ID);
		}
		old = model.findConstantTableByName(tableName);
		if (old != null && old != table) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.DUPLICATE_CONSTANT_TABLE_NAME);
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider)
						.checkConstantTableProperties(model, table, id,
								tableName);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkTraceGroupProperties(com.nokia.tracebuilder.model.TraceModel,
	 *      com.nokia.tracebuilder.model.TraceGroup, int, java.lang.String)
	 */
	public void checkTraceGroupProperties(TraceModel model, TraceGroup group,
			int id, String name) throws TraceBuilderException {
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals.getGroupNameHandler();
		if (!SourceUtils.isValidName(name)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_GROUP_NAME);
		} else if ((id < 0) || (id > groupNameHandler.getMaxGroupId())) {
			RangeErrorParameters params = new RangeErrorParameters();
			params.start = 0;
			params.end = groupNameHandler.getMaxGroupId();
			params.isHex = true;
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_GROUP_ID, params);
		} else {
			TraceGroup old = model.findGroupByID(id);
			if (old != null && old != group) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.DUPLICATE_GROUP_ID);
			}
			old = model.findGroupByName(name);
			if (old != null && old != group) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.DUPLICATE_GROUP_NAME);
			}
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider)
						.checkTraceGroupProperties(model, group, id, name);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkTraceModelProperties(com.nokia.tracebuilder.model.TraceModel,
	 *      int, java.lang.String, java.lang.String)
	 */
	public void checkTraceModelProperties(TraceModel model, int id,
			String name, String path) throws TraceBuilderException {
		if (!SourceUtils.isValidName(name)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_MODEL_NAME);
		}
		// Path is null when updating model
		if (path != null) {
			if (path.length() == 0) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_PATH);
			}
			File f = new File(path);
			if (f.exists() && !f.isDirectory()) {
				FileErrorParameters fp = new FileErrorParameters();
				fp.file = f.getAbsolutePath();
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_PATH, fp);
			}
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider)
						.checkTraceModelProperties(model, id, name, path);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkTraceParameterProperties(com.nokia.tracebuilder.model.Trace,
	 *      com.nokia.tracebuilder.model.TraceParameter, int, java.lang.String,
	 *      java.lang.String)
	 */
	public void checkTraceParameterProperties(Trace owner,
			TraceParameter parameter, int id, String name, String type)
			throws TraceBuilderException {
		if (!SourceUtils.isValidParameterName(name)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_PARAMETER_NAME);
		}
		TraceParameter old = owner.findParameterByID(id);
		if (old != null && old != parameter) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.DUPLICATE_PARAMETER_ID);
		}
		old = owner.findParameterByName(name);
		if (old != null && old != parameter) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.DUPLICATE_PARAMETER_NAME);
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider)
						.checkTraceParameterProperties(owner, parameter, id,
								name, type);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceObjectPropertyVerifier#
	 *      checkTraceProperties(com.nokia.tracebuilder.model.TraceGroup,
	 *      com.nokia.tracebuilder.model.Trace, int, java.lang.String,
	 *      java.lang.String)
	 */
	public void checkTraceProperties(TraceGroup group, Trace trace, int id,
			String name, String data) throws TraceBuilderException {
		if (!isValidData(data)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_TRACE_DATA);
		} else if (!SourceUtils.isValidName(name)) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_TRACE_NAME);
		} else if ((id < 0) || (id > TraceBuilderGlobals.MAX_TRACE_ID)) {
			RangeErrorParameters params = new RangeErrorParameters();
			params.start = 0;
			params.end = TraceBuilderGlobals.MAX_TRACE_ID;
			params.isHex = true;
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_TRACE_ID, params);
		} else {
			// Verifies the trace name is globally unique
			Trace old = TraceBuilderGlobals.getTraceModel().findTraceByName(
					name);
			if (old != trace && old != null) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.DUPLICATE_TRACE_NAME);
			}
			if (group != null) {
				// If group exists, the trace ID and text must be unique within
				// the group
				old = group.findTraceByID(id);
				if (old != trace && old != null) {
					// Trace ID's must be unique within group
					throw new TraceBuilderException(
							TraceBuilderErrorCode.DUPLICATE_TRACE_ID);
				}
			}
		}
		Iterator<TraceBuilderPlugin> itr = pluginEngine.getPlugins();
		while (itr.hasNext()) {
			TraceBuilderPlugin provider = itr.next();
			if (provider instanceof TraceObjectPropertyVerifier) {
				((TraceObjectPropertyVerifier) provider).checkTraceProperties(
						group, trace, id, name, data);
			}
		}
	}

	/**
	 * Checks the validity of data
	 * 
	 * @param data
	 *            the data
	 * @return true if valid
	 */
	private boolean isValidData(String data) {
		boolean retval;
		if (data != null) {
			retval = true;
			for (int i = 0; i < data.length() && retval; i++) {
				char c = data.charAt(i);
				// Unescaped quotes are not allowed
				if (c == SourceConstants.QUOTE_CHAR
						&& (i == 0 || data.charAt(i - 1) != SourceConstants.BACKSLASH_CHAR)) {
					retval = false;
				} else {
					retval = isValidDataChar(c);
				}
			}
		} else {
			retval = false;
		}
		return retval;
	}

	/**
	 * Checks data character validity
	 * 
	 * @param c
	 *            character
	 * @return true if valid
	 */
	private boolean isValidDataChar(char c) {
		// Special and extended characters are not allowed
		return c >= DATA_CHAR_START && c <= DATA_CHAR_END;
	}

}
