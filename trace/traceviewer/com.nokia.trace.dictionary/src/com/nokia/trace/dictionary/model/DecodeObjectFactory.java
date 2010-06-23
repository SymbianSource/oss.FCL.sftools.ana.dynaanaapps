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
 * Decode Object Factory
 *
 */
package com.nokia.trace.dictionary.model;

/**
 * Decode Object Factory creates components, groups and traces
 * 
 */
public class DecodeObjectFactory {

	/**
	 * Constructor
	 */
	public DecodeObjectFactory() {

	}

	/**
	 * Creates and returns new TraceComponent
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param prefix
	 *            prefix
	 * @param suffix
	 *            suffix
	 * @param model
	 *            model
	 * @return component
	 */
	public TraceComponent createTraceComponent(int id, String name,
			String prefix, String suffix, DictionaryDecodeModel model) {
		TraceComponent newComponent = new TraceComponent(id, name, prefix,
				suffix, DictionaryModelBuilder.getCurrentFile(), model);
		return newComponent;
	}

	/**
	 * Creates and returns new TraceGroup
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param prefix
	 *            prefix
	 * @param suffix
	 *            suffix
	 * @param component
	 *            component
	 * @return group
	 */
	public TraceGroup createTraceGroup(int id, String name, String prefix,
			String suffix, TraceComponent component) {
		TraceGroup newGroup = new TraceGroup(id, name, prefix, suffix,
				component);
		return newGroup;
	}

	/**
	 * Creates and return new Trace
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param traceData
	 *            reference to traceData
	 * @param location
	 *            reference to location
	 * @param lineNum
	 *            line number
	 * @param methodName
	 *            method name
	 * @param className
	 *            class name
	 * @param group
	 *            parent group
	 * @return new trace
	 */
	public Trace createTrace(int id, String name, TraceData traceData,
			Location location, int lineNum, String methodName,
			String className, TraceGroup group) {
		Trace newTrace = new Trace(id, name, traceData, location, lineNum,
				methodName, className, group);
		return newTrace;
	}

}
