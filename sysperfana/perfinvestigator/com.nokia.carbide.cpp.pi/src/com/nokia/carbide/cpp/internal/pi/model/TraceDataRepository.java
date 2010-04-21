/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.internal.pi.model;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

public class TraceDataRepository 
{	
	private static Hashtable<Integer,LinkedHashMap<Class,ParsedTraceData>> analysisSpecificTraces;
	private static TraceDataRepository instance;
	
	public static TraceDataRepository getInstance() {
		if (instance == null) {
			instance = new TraceDataRepository();
		}
		return instance;
	}
	
	// singleton
	private TraceDataRepository()
	{
		if (analysisSpecificTraces == null)
			analysisSpecificTraces = new Hashtable<Integer,LinkedHashMap<Class,ParsedTraceData>>();
	}
	
	public void registerTraces(int analysisId) {
		if (analysisSpecificTraces.get(analysisId) == null) {
			analysisSpecificTraces.put(Integer.valueOf(analysisId), new LinkedHashMap<Class,ParsedTraceData>());
		}
	}
	
	public void insertTraceCollection(Class traceClass, ParsedTraceData traceData, int analysisId)
	{
		LinkedHashMap<Class,ParsedTraceData> tracesForMyId = getTraceCollection(analysisId);
		if (tracesForMyId == null)
		{
			tracesForMyId = new LinkedHashMap<Class,ParsedTraceData>();
		}
		if (traceClass != null && traceData != null && traceData.traceData != null)
		{
			tracesForMyId.put(traceClass, traceData);
			analysisSpecificTraces.put(Integer.valueOf(analysisId), tracesForMyId);
		}
	}
	
//	public ParsedTraceData getTrace(Class traceClass)
//	{
//		return traces.get(traceClass);
//	}
	
	public ParsedTraceData getTrace(int analysisId, Class traceClass)
	{
		if (analysisSpecificTraces == null || traceClass == null)
			return null;

		LinkedHashMap<Class,ParsedTraceData> tmp = analysisSpecificTraces.get(Integer.valueOf(analysisId));

		if (tmp == null)
			return null;

		return tmp.get(traceClass);
	}
	
	public Enumeration<FunctionResolver> getResolvers(int analysisId)
	{
		Vector<FunctionResolver> resolvers = new Vector<FunctionResolver>();
		
		if (analysisSpecificTraces == null) 
			return resolvers.elements();

		LinkedHashMap<Class,ParsedTraceData> tmp = analysisSpecificTraces.get(Integer.valueOf(analysisId));

		Iterator<ParsedTraceData> e = tmp.values().iterator();
		while (e.hasNext())
		{
			ParsedTraceData data = (ParsedTraceData)e.next();
			if (data.functionResolvers != null)
			{
				for (int i = 0; i < data.functionResolvers.length; i++)
				{
					resolvers.add(data.functionResolvers[i]);
				}
			}
		}
		
		return resolvers.elements();
	}
	
	public ParsedTraceData getTrace(int analysisId, String className)
	{
		Class traceClass;
		try 
		{
			traceClass = Class.forName(className);
		} 
		catch (ClassNotFoundException e2)
		{
			return null;
		}

		if (analysisSpecificTraces == null)
			return null;

		LinkedHashMap<Class,ParsedTraceData> tmp = analysisSpecificTraces.get(Integer.valueOf(analysisId));

		if (tmp == null)
			return null;

		return (ParsedTraceData)tmp.get(traceClass);
	}
	
	public Iterator<ParsedTraceData> getTraceCollectionIter(int analysisId)
	{
		if (analysisSpecificTraces == null)
			return null;

		LinkedHashMap<Class,ParsedTraceData> tmp = analysisSpecificTraces.get(Integer.valueOf(analysisId));

		if (tmp == null)
			return null;
		
		return tmp.values().iterator();
	}

	public LinkedHashMap<Class,ParsedTraceData> getTraceCollection(int analysisId)
	{
		if (analysisSpecificTraces == null)
			return null;

		LinkedHashMap<Class,ParsedTraceData> tmp = analysisSpecificTraces.get(Integer.valueOf(analysisId));

		if (tmp == null)
			return null;

		return tmp;
	}
	
	public void removeTraces(int analysisId)
	{
		if (analysisSpecificTraces != null)
			analysisSpecificTraces.remove(Integer.valueOf(analysisId));
	}
	
	public void removeAll()
	{
		if (analysisSpecificTraces != null)
			analysisSpecificTraces.clear();
	}
}
