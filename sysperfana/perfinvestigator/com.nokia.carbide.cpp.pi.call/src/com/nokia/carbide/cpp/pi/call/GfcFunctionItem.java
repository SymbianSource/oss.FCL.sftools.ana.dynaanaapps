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

package com.nokia.carbide.cpp.pi.call;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class GfcFunctionItem implements Serializable
{
	private static final long serialVersionUID = -8082342532211331507L;
	
	// this function has been called by these functions
	private Hashtable callerData;

	// this function has called these functions
	private Hashtable calleeData;

	public long address;
	public String name;
	public String dllName;
	private int totalCallerAmount; // really number of times this function is called
	private int totalCalleeAmount;	// really number of times this function is the caller
	private boolean percentageUpdated;

	private double recursiveLoad = 0;
	private double accumulatedRecursiveLoad = 0;

	private double absoluteTotalPercentage = 0;
	private double absoluteCallerPercentage = 0;
	private double recursiveTotalPercentage = 0;
	private double recursiveSampleCount = 0;

	private int storedPercentageStart = -1;
	private int storedPercentageEnd = -1;

	private boolean isSymbolParsed;

	public GfcFunctionItem(String name, long address, String dllName, boolean isSymbolParsed)
	{
		this.isSymbolParsed  = isSymbolParsed;
		this.address         = address;
		this.name            = name;
		this.dllName         = dllName;
		this.totalCallerAmount   = 0;
		this.percentageUpdated = false;
		this.callerData = new Hashtable<Long,GfcFunctionItemData>();
		this.calleeData = new Hashtable<Long,GfcFunctionItemData>();
	}

	public boolean isSymbolParsed()
	{
		return this.isSymbolParsed;
	}

	public int isCalledCount() {
		return this.totalCallerAmount;
	}
	
	public int isCallerCount() {
		return this.totalCalleeAmount;
	}
	
	public double getAbsoluteTotalPercentage(int start,int end)
	{
		if (this.storedPercentageStart == start && this.storedPercentageEnd == end)
		{
			return this.absoluteTotalPercentage;
		}
		else return -1;
	}

	public double getAbsoluteCallerPercentage(int start,int end)
	{
		if (this.storedPercentageStart == start && this.storedPercentageEnd == end)
		{
			return this.absoluteCallerPercentage;
		}
		else return -1;
	}

	public double getRecursiveCallerPercentage(int start,int end)
	{
		if (this.storedPercentageStart == start && this.storedPercentageEnd == end)
		{
			return this.recursiveTotalPercentage;
		}
		else return -1;
	}

	public void storePercents(double total, double caller, double recursive, int start, int end)
	{
		this.absoluteCallerPercentage = caller;
		this.absoluteTotalPercentage  = total;
		this.recursiveTotalPercentage = recursive;
		this.storedPercentageStart    = start;
		this.storedPercentageEnd      = end;
	}

	public double getRecursiveCallerSamples(int recursion, double cumulativeValue, GfcFunctionItem exclude)
	{
		if (recursion > 10)
			return 0;
		recursion++;

		GfcFunctionItem[] callees = this.getCalleeList();
		Double[] calleePerc = this.getCalleePercentages();

		double callActivity =
			this.countSamplesWhereThisFunctionIsTheCaller() /
			(this.countSamplesThatCallThisFunction() +
			 this.countSamplesWhereThisFunctionIsTheCaller());

		if (this.recursiveSampleCount == 0)
		{
			for (int i = 0; i < callees.length; i++)
			{
				GfcFunctionItem item = callees[i];
				Double itemPerc = calleePerc[i];

				if (item != this)
				{
					// get the direct value this function causes
					this.recursiveSampleCount += (itemPerc.doubleValue() / 100) * item.countSamplesThatCallThisFunction();

					// recursively get the values this function calls:
					// itemPerc = percentage of the function in this functions call share (that sums 100%)
					// callActivity = factor of samples related to this function, that indicate that this
					// function has called another function (has been the caller). Call activity is
					// 100% if all the samples related to this function are such that they have
					// the pointer to this function only in the link register

					this.recursiveSampleCount += (itemPerc.doubleValue() * callActivity / 100) *
													item.getRecursiveCallerSamples(recursion, cumulativeValue, this);
				}
			}
		}

		cumulativeValue += this.recursiveSampleCount;

		//System.out.println("rec "+recursion+" cum"+cumulativeValue);
		return cumulativeValue;
	}

	public void addRecursiveLoad(double samples)
	{
		this.recursiveLoad += samples;
		this.accumulatedRecursiveLoad += samples;
	}

	public void setRecursiveLoad(double load)
	{
		this.recursiveLoad = load;
	}

	public double getRecursiveLoad()
	{
		return this.recursiveLoad;
	}

	public double getAccumulatedLoad()
	{
		return this.accumulatedRecursiveLoad;
	}

	public int countSamplesThatCallThisFunction()
	{
		int callCount = 0;
		Enumeration<GfcFunctionItemData> callerEnum = this.callerData.elements();
		while (callerEnum.hasMoreElements())
		{
			GfcFunctionItemData data = callerEnum.nextElement();
			callCount += data.callTimes.size();
		}
		return callCount;
	}

	public ArrayList getSamplesThatCallThisFunction()
	{
		ArrayList callTimes = new ArrayList();
		Enumeration<GfcFunctionItemData> callerEnum = this.callerData.elements();
		while (callerEnum.hasMoreElements())
		{
			GfcFunctionItemData data = callerEnum.nextElement();
			callTimes.addAll(data.callTimes);
		}
		return callTimes;
	}

	public int countSamplesWhereThisFunctionIsTheCaller()
	{
		int callCount = 0;
		Enumeration<GfcFunctionItemData> calleeEnum = this.calleeData.elements();
		while (calleeEnum.hasMoreElements())
		{
			GfcFunctionItemData data = calleeEnum.nextElement();
			callCount += data.callTimes.size();
		}
		return callCount;
	}

	public ArrayList getSamplesWhereThisFunctionIsTheCaller()
	{
		ArrayList callTimes = new ArrayList();
		Enumeration<GfcFunctionItemData> calleeEnum = this.calleeData.elements();
		while (calleeEnum.hasMoreElements())
		{
			GfcFunctionItemData data = calleeEnum.nextElement();
			callTimes.addAll(data.callTimes);
		}
		return callTimes;
	}

	public static class GfcFunctionItemData implements Serializable
	{
		private static final long serialVersionUID = -8082342532211331507L;
		public GfcFunctionItem function;
		public Double percentage;
		// size of the vector indicates the amount
		// of times this function has been called
		public Vector callTimes = new Vector();
	}

	// this function has been called but the caller is unknown
	public void addUnknownCaller(Integer callTime)
	{
		// store unknown callers with an address -1
		if (!this.callerData.containsKey(Long.valueOf(-1)) )
		{
			GfcFunctionItemData newData = new GfcFunctionItemData();
			newData.function = null;
			newData.percentage = new Double(0);
			newData.callTimes.add(callTime);

			// add a new caller
			this.callerData.put(Long.valueOf(-1),newData);
		}
		else
		{
			// just add the call count of an existing caller
			GfcFunctionItemData oldData = (GfcFunctionItemData)this.callerData.get(Long.valueOf(-1));
			this.totalCallerAmount++;
			oldData.callTimes.add(callTime);
		}

		this.percentageUpdated = false;
	}

	// this function has been called
	public void addCaller(GfcFunctionItem caller, Integer callTime)
	{
		Long address = Long.valueOf(caller.address);
		if (!this.callerData.containsKey(address))
		{
			GfcFunctionItemData newData = new GfcFunctionItemData();
			newData.function = caller;
			newData.percentage = new Double(0);
			newData.callTimes.add(callTime);

			this.totalCallerAmount++;

			// add a new caller
			this.callerData.put(address,newData);
		}
		else
		{
			// just add the call count of an existing caller
			GfcFunctionItemData oldData = (GfcFunctionItemData)this.callerData.get(address);
			this.totalCallerAmount++;
			oldData.callTimes.add(callTime);
		}

		this.percentageUpdated = false;
	}

	// this function has called another function
	public void addCallee(GfcFunctionItem callee, Integer callTime)
	{
		Long address = Long.valueOf(callee.address);
		if (!this.calleeData.containsKey(address))
		{
			GfcFunctionItemData newData = new GfcFunctionItemData();
			newData.function = callee;
			newData.percentage = new Double(0);
			newData.callTimes.add(callTime);

			this.totalCalleeAmount++;

			// add the new callee
			this.calleeData.put(address,newData);
		}
		else
		{
			// just add the call count of an existing callee
			GfcFunctionItemData oldData = (GfcFunctionItemData)this.calleeData.get(address);
			this.totalCalleeAmount++;
			oldData.callTimes.add(callTime);
		}

		this.percentageUpdated = false;
	}

	public GfcFunctionItem[] getCallerList()
	{
		GfcFunctionItem[] functionList = new GfcFunctionItem[callerData.size()];
		Enumeration callEnum = this.callerData.elements();
		int i = 0;

		while (callEnum.hasMoreElements())
		{
			GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
			if (di.function != null)
			{
				functionList[i] = di.function;
			}
			i++;
		}
		return functionList;
	}

	public Double[] getCallerPercentages()
	{
		calculatePercentages();

		Double[] pList = new Double[callerData.size()];
		Enumeration callEnum = this.callerData.elements();
		int i = 0;
		
		while (callEnum.hasMoreElements())
		{
			GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
			if (di.function != null)
			{
				pList[i] = di.percentage;
			}
			i++;
		}
		return pList;
	}

	public GfcFunctionItem[] getCalleeList()
	{
		GfcFunctionItem[] functionList = new GfcFunctionItem[calleeData.size()];
		Enumeration callEnum = this.calleeData.elements();
		int i = 0;

		while (callEnum.hasMoreElements())
		{
			GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
			functionList[i] = di.function;
			i++;
		}
		return functionList;
	}

	public Double[] getCalleePercentages()
	{
		calculatePercentages();

		Double[] pList = new Double[calleeData.size()];
		Enumeration callEnum = this.calleeData.elements();
		int i = 0;

		while (callEnum.hasMoreElements())
		{
			GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
			pList[i] = di.percentage;
			i++;
		}
		return pList;
	}

	public String toString()
	{
		this.calculatePercentages();

		String result = Messages.getString("GfcFunctionItem.calledBy1") + this.name + Messages.getString("GfcFunctionItem.calledBy2"); //$NON-NLS-1$ //$NON-NLS-2$
		Enumeration callers = this.callerData.elements();

		while (callers.hasMoreElements())
		{
			GfcFunctionItemData item = (GfcFunctionItemData)callers.nextElement();
			result += Messages.getString("GfcFunctionItem.calledBy3") + item.percentage + Messages.getString("GfcFunctionItem.calledBy4") + item.function.name + Messages.getString("GfcFunctionItem.calledBy5") + item.function.dllName + Messages.getString("GfcFunctionItem.calledBy6"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		result += Messages.getString("GfcFunctionItem.calls1") + this.name + Messages.getString("GfcFunctionItem.calls2"); //$NON-NLS-1$ //$NON-NLS-2$

		Enumeration callees = this.calleeData.elements();
		while (callees.hasMoreElements())
		{
			GfcFunctionItemData item = (GfcFunctionItemData)callees.nextElement();
			result += Messages.getString("GfcFunctionItem.calls3") + item.percentage + Messages.getString("GfcFunctionItem.calls4") + item.function.name + Messages.getString("GfcFunctionItem.calls5") + item.function.dllName + Messages.getString("GfcFunctionItem.calls6"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		result += Messages.getString("GfcFunctionItem.calls7"); //$NON-NLS-1$

		return result;
	}

	private void calculatePercentages()
	{
		if (this.percentageUpdated == false)
		{
			Enumeration callEnum = this.callerData.elements();
			while (callEnum.hasMoreElements())
			{
				GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
				if (di.function != null)
				{
					di.percentage = new Double((double)((double)di.callTimes.size()/(double)this.totalCallerAmount)*100);
				}
			}

			callEnum = this.calleeData.elements();
			while (callEnum.hasMoreElements())
			{
				GfcFunctionItemData di = (GfcFunctionItemData)callEnum.nextElement();
				if (di.function != null)
				{
					di.percentage = new Double((double)((double)di.callTimes.size()/(double)this.totalCalleeAmount)*100);
				}
			}

			this.percentageUpdated = true;
		}
	}

}
