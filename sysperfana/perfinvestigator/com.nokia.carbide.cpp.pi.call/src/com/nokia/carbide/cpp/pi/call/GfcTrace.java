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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.utils.QuickSortImpl;
import com.nokia.carbide.cpp.internal.pi.utils.Sortable;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;


public class GfcTrace extends GenericSampledTraceWithFunctions
{
	private static final long serialVersionUID = -8144591614894549185L;

	private long previousSample;
	  
	private Hashtable parsedGfcTrace;
	private Vector    completeGfcTrace;
	private int firstSample;
	private int lastSample;
	
	private transient CallVisualiser callVisualiser;

	private transient boolean completeTrace   = true;
	private transient boolean completeSamples = true;
	private transient int samplingInterval;
	
	public static final int SORT_BY_TOTAL_LOAD = 1;
	public static final int SORT_BY_CALLED_LOAD = 2;
	public static final int SORT_BY_RECURSIVE_LOAD = 3;

	public GfcTrace(int size)
	{	
	    this.completeGfcTrace = new Vector(size);
	    this.samples = new Vector(size);
	    previousSample = 0;  	
	}
	
	public void addSample(GfcSample sample, Long[] element)
	{
	    this.samples.add(sample);
	  	  
	    long sampleNumber   = sample.sampleSynchTime;
	      
	    if (previousSample != 0)
	    {
	    	if (sampleNumber != previousSample + samplingInterval)
	    		System.out.println(Messages.getString("GfcTrace.missingSample1") + previousSample + Messages.getString("GfcTrace.missingSample2") + sample); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    previousSample = sampleNumber;
	
	    if (this.firstSample == -1)
	    	this.firstSample = (int)sampleNumber;
	    else
	    	this.lastSample  = (int)sampleNumber;
	    /*
	    System.out.print("\n\nSample: "+sample+" pc: "+programCounter+" lr: "+linkRegister);
	    System.out.print("\nSample: "+sample+
	                       "\npc: "+symbolParser.getFunctionForAddress(programCounter).functionName+
	                       "\nlr: "+symbolParser.getFunctionForAddress(linkRegister).functionName);
	    */
	    this.completeGfcTrace.add(element);
	}
  
	public Hashtable parseEntries(int startSample, int endSample)
	{
	  	// code to avoid stack overflow in saving analysis,
	  	// with parameters -20,-20 , the parsedGfcTrace field is cleared,
	  	// and it is restored with values -10,-10
	    if (startSample == -20 && endSample == -20)
	    {
	    	if (this.parsedGfcTrace == null)
	    	{
	    		return null;
	    	}
	    	else
	    	{
	    		this.parsedGfcTrace = null;
	    		return new Hashtable();
	    	}
	    }
	    else if (startSample == -10 && endSample == -10)
	    {
	    	this.parseEntries(this.firstSample, this.lastSample);
	    	return null;
	    }
	
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

    	if (startSample < 1)
    		startSample = 0;

    	this.firstSample = startSample / samplingInterval;
	    this.lastSample  = endSample / samplingInterval;
	    
	    this.parsedGfcTrace = new Hashtable();
	    
    	if (this.completeTrace) {
	    	// adjust the sample search
	    	if (startSample > this.completeGfcTrace.size())
	    		startSample = this.completeGfcTrace.size();
	    	if (endSample > this.completeGfcTrace.size())
	    		endSample = this.completeGfcTrace.size();
	    	
	    	for (int i = startSample; i < endSample; i++) {
	    		Long[] data = (Long[])this.completeGfcTrace.elementAt(i);
	    		this.parseOneEntry(data[0], data[1], data[2]);
	    	}
	    } else if (endSample >= samplingInterval) {
	    	// sample is incomplete, so find the sample nearest but after the start
	    	int nearest = 0;
	    	if (startSample >= samplingInterval) {
	    		int last = 0;
	    		int half = this.completeGfcTrace.size() / 2;
	    		nearest = this.completeGfcTrace.size() - 1;
	    		long time = ((Long[]) this.completeGfcTrace.get(nearest))[0];
	    		
	    		while (time != startSample && half > 0) {
	    			if (time < startSample)
	    				nearest = nearest + half;
	    			else
	    				nearest = nearest - half;
	    			half /= 2;
	    			time = ((Long[]) this.completeGfcTrace.get(nearest))[0];
	    		}
	    		
	    		while (nearest > 0 && time > startSample) {
	    			nearest--;
	    			time = ((Long[]) this.completeGfcTrace.get(nearest))[0];
	    		}
	    		
	    		while (nearest < this.completeGfcTrace.size() - 1 && time + samplingInterval < startSample) {
	    			nearest++;
	    			time = ((Long[]) this.completeGfcTrace.get(nearest))[0];
	    		}
	    	}
	    	
	    	// we're starting at the right spot, so collect data (don't worry that we get the first one twice - we only parse it once
	    	Long[] data = (Long[])this.completeGfcTrace.get(nearest);
		    for ( ; data[0].longValue() <= endSample && nearest < this.completeGfcTrace.size(); nearest++) {
		        data = (Long[])this.completeGfcTrace.get(nearest);
		        this.parseOneEntry(data[0], data[1], data[2]);
		    }
	    }

	    countRecursiveLoad();
    
	    return this.parsedGfcTrace;
	}
	
	private class CallerCallee {
		String  functionName;
		Long    startAdress;
		String  binaryName;
		boolean symbolParsed;
	}
  
  	private void parseOneEntry(long sample, long programCounter, long linkRegister)
  	{
	  	GfcSample gSam = null;
 
	  	if (this.completeSamples)
  	    {
	  		// find it directly
	  		if (sample <= this.getLastSampleNumber())
	  			gSam = (GfcSample)this.samples.elementAt((int) (sample - 1));
  	    } else {
  	    	// find it the slow way
  		  	if (this.getLastSampleNumber() <= sample + 1 || this.getFirstSampleNumber() > sample)
  		  		return;

  		  	GenericSample[] samples = this.getSamplesForTime(sample);
	  	    if ((samples != null) && (samples.length != 0))
	  	    	gSam = (GfcSample)samples[0];
  	    }
  	    
  	    if (gSam == null) {
  	    	System.out.println(Messages.getString("GfcTrace.sampleNotFound") + sample);  	    	 //$NON-NLS-1$
  	    	return;
  	    }
  	    
  	    CallerCallee caller = new CallerCallee();
  	    CallerCallee callee = new CallerCallee();

		if (  (gSam.getCallerFunctionSym() == null || gSam.getCallerFunctionSym().getFunctionName().endsWith(Messages.getString("GfcTrace.functionNotFound"))) //$NON-NLS-1$
			&& gSam.getCallerFunctionItt() != null) //$NON-NLS-1$
		{
			caller.functionName = gSam.getCallerFunctionItt().getFunctionName();
    		caller.startAdress  = gSam.getCallerFunctionItt().getStartAddress();
			caller.binaryName   = gSam.getCallerFunctionItt().getFunctionBinary().getBinaryName();
			caller.symbolParsed = false;
		}
		else
		{
			caller.functionName = gSam.getCallerFunctionSym().getFunctionName();
			caller.startAdress  = gSam.getCallerFunctionSym().getStartAddress();
			caller.binaryName   = gSam.getCallerFunctionSym().getFunctionBinary().getBinaryName();
			caller.symbolParsed = Boolean.valueOf(true);
		}
		
		if (   (gSam.getCurrentFunctionSym() == null || gSam.getCurrentFunctionSym().getFunctionName().endsWith(Messages.getString("GfcTrace.functionNotFound"))) //$NON-NLS-1$
			&& gSam.getCurrentFunctionItt() != null) //$NON-NLS-1$
		{
			callee.functionName = gSam.getCurrentFunctionItt().getFunctionName();
    		callee.startAdress  = gSam.getCurrentFunctionItt().getStartAddress();
			callee.binaryName   = gSam.getCurrentFunctionItt().getFunctionBinary().getBinaryName();
			callee.symbolParsed = Boolean.valueOf(false);
		}
		else
		{
			callee.functionName = gSam.getCurrentFunctionSym().getFunctionName();
    		callee.startAdress  = gSam.getCurrentFunctionSym().getStartAddress();
			callee.binaryName   = gSam.getCurrentFunctionSym().getFunctionBinary().getBinaryName();
			callee.symbolParsed = Boolean.valueOf(true);
		}

		this.updateEntryLists(sample, programCounter, linkRegister, caller, callee);
  	}

  	private void updateEntryLists(long sample, long programCounter, long linkRegister, CallerCallee callerObj, CallerCallee calleeObj)
	{
	    String callerName          = callerObj.functionName;
	    Long callerStart           = callerObj.startAdress;
	    String callerDllName       = callerObj.binaryName;
	    boolean callerSymbolParsed = callerObj.symbolParsed;
	
	    String calleeName           = calleeObj.functionName;
	    Long calleeStart            = calleeObj.startAdress;
	    String calleeDllName        = calleeObj.binaryName;
	    boolean calleeSymbolParsed  = calleeObj.symbolParsed;
	    
	    GfcFunctionItem caller;
	    GfcFunctionItem callee;
	
	    // is the callee in the list
	    if (parsedGfcTrace.containsKey(calleeStart))
	    {
		    // the callee is in the list
		    callee = (GfcFunctionItem)this.parsedGfcTrace.get(calleeStart);
	    }
	    else
	    {
		    // the callee is not in the list
		    callee = new GfcFunctionItem(calleeName, calleeStart.longValue(), calleeDllName, calleeSymbolParsed);
		    this.parsedGfcTrace.put(calleeStart, callee);
	    }

	    // is the caller in the list
	    if (this.parsedGfcTrace.containsKey(callerStart))
	    {
	    	// the caller is in the list
	    	caller = (GfcFunctionItem)this.parsedGfcTrace.get(callerStart);
	    }
	    else
	    {
	    	// no, we have to add the caller to the list as well
	    	caller = new GfcFunctionItem(callerName, callerStart.longValue(), callerDllName, callerSymbolParsed);
	    	this.parsedGfcTrace.put(callerStart, caller);
	    }

	    if (!caller.equals(callee))
	    {
	    	callee.addCaller(caller, Integer.valueOf((int)sample));
	    	caller.addCallee(callee, Integer.valueOf((int)sample));
	    }
	}
  
	public void countRecursiveLoad()
	{
		//if (exclude == null)
		ArrayList exclude = new ArrayList();

		GfcFunctionItem[] sorted = this.getEntriesSorted(SORT_BY_CALLED_LOAD);
		
		if (sorted.length == 0)
			return;
		
		int index = sorted.length - 1;
		int lastExclude = 0;

		// add the load of this function to the accumulated load
		for (int i = 0; i < sorted.length; i++)
		{
			GfcFunctionItem gfi = sorted[i];
			gfi.addRecursiveLoad(gfi.countSamplesThatCallThisFunction());
			gfi.setRecursiveLoad(0);
		}

		while (true)
		{
		    GfcFunctionItem item = sorted[index];
		    item.storePercents(-1, -1, -1, this.firstSample, this.lastSample);
		
		    index--;
		    if (index == -1)
		    {
		        index = sorted.length - 1;
		        if (lastExclude == exclude.size())
		        {
		        	break;
		        }
		        else
		        {
		        	lastExclude = exclude.size();
		        }
		    }
		
		    if (!exclude.contains(item))
		    {
		        double load = item.countSamplesThatCallThisFunction();
		
		        GfcFunctionItem[] callers = item.getCallerList();
		        Double[] percentages = item.getCallerPercentages();
		
		        for (int i = 0; i < callers.length; i++)
		        {
			        //System.out.println(item.name+" load:"+load+" Adding "+load*(percentages[i].doubleValue()/100)+" to"+callers[i].name);
			        callers[i].addRecursiveLoad(load*(percentages[i].doubleValue()/100));
			
			        // there is some accumulated recursive load to distribute
			        // between the callers
			        if (item.getRecursiveLoad() > 0)
			        {
			            double recursiveLoadToShare = item.getRecursiveLoad()*(percentages[i].doubleValue()/100);
			
			            // add recursive load only if the caller is not the function itself
			            if (!callers[i].equals(item))
			            {
			            	callers[i].addRecursiveLoad(recursiveLoadToShare);
			            }
			        }
		        }
		        // clear the recursive load
		        item.setRecursiveLoad(0);
		    }
	
		    // this function does not call any other functions so it can be added
		    // to the excluded functions list
		    if (item.getCalleeList().length == 0)
		    {
		        if (!exclude.contains(item))
		        	exclude.add(item);
		        //sorted.remove(item);
		    }
		    else
		    {
		        GfcFunctionItem[] callees = item.getCalleeList();
		        int i;
		        for (i = 0; i < callees.length; i++)
		        {
			        // break if any item is found that is not excluded
			        if (!exclude.contains(callees[i]))
			        	break;
		        }
		
		        // there are only excluded items in this function's call list,
		        // thus it can also be excluded
		        if (i == callees.length)
		        {
			        if (!exclude.contains(item))
			        	exclude.add(item);
			        //sorted.remove(item);
		        }
		    }
		}
	}
  
	public GfcFunctionItem[] getEntriesSorted(final int sortMethod)
	{
	  	class ItemSorter implements Sortable
	  	{
	  		GfcFunctionItem item;
	  		
	  		double itemTotal;
	  		double itemCaller;
	  		double itemRecursive;
	        
	        public ItemSorter(GfcFunctionItem item)
	        {
	        	this.item = item;
	            // calculate and store the percentages for the items for the selected period
	            itemTotal     = item.getAbsoluteTotalPercentage(firstSample, lastSample);
	            itemCaller    = item.getAbsoluteCallerPercentage(firstSample, lastSample);
	            itemRecursive = item.getRecursiveCallerPercentage(firstSample, lastSample);
	            
	            if (itemTotal == -1 || itemCaller == -1 || itemRecursive == -1)
	            {
	            	itemTotal     = getAbsoluteTraditionalPercentageFor(item);
	            	itemCaller    = getAbsoluteCallerPercentageFor(item);
	            	itemRecursive = getRecursiveCallerPrecentageFor(item);
	            
	            	item.storePercents(itemTotal, itemCaller, itemRecursive,
	                               		firstSample, lastSample);
	            }
	        }
        
	  		public long valueOf()
	  		{
	  	        if (sortMethod == GfcTrace.SORT_BY_TOTAL_LOAD)
	  	        {
	  	        	return (long)(1000 * itemTotal);
	  	        }
	  	        else if (sortMethod == GfcTrace.SORT_BY_CALLED_LOAD)
	  	        {
	  	        	return (long)(1000 * itemCaller);
	  	        }
	  	        else if (sortMethod == GfcTrace.SORT_BY_RECURSIVE_LOAD)
	  	        {
	  	        	return (long)(1000 * itemRecursive);
	  	        }
	  	        else
	  	        {
	  	        	return 0;
	  	        }
	  		}
	  	}
  	
		Enumeration elements = this.parsedGfcTrace.elements();
	  	
		Vector s = new Vector();
		while (elements.hasMoreElements())
		{
	  		ItemSorter is = new ItemSorter((GfcFunctionItem)elements.nextElement());
	  		s.add(is);
	  	}
	  	
	  	QuickSortImpl.sortReversed(s);
  	
	  	elements = s.elements();
	  	this.parsedGfcTrace.clear();
	  	
	  	GfcFunctionItem[] f = new GfcFunctionItem[s.size()];
	  	int i = 0;
	  	while (elements.hasMoreElements())
	  	{
	  		ItemSorter is = (ItemSorter)elements.nextElement();
	  		f[i++] = is.item;
	  		this.parsedGfcTrace.put(Long.valueOf(is.item.address), is.item);
	  	}
  	
	  	return f;
	}
  
  	public double getAbsoluteTraditionalPercentageFor(GfcFunctionItem gfi)
	{
	    double samples = gfi.countSamplesThatCallThisFunction();
	    double total = this.lastSample - this.firstSample;
	    return 100 * (samples / total);
	}

  	public double getAbsoluteCallerPercentageFor(GfcFunctionItem gfi)
  	{
  		double samples = gfi.countSamplesWhereThisFunctionIsTheCaller();
  		double total = this.lastSample - this.firstSample;
  		return 100 * (samples / total);
  	}

  	public double getRecursiveCallerPrecentageFor(GfcFunctionItem gfi)
  	{
  		double samples = gfi.getAccumulatedLoad();
  		double total = this.lastSample - this.firstSample;
  		return 100 * (samples / total);
  	}

  	/*
  	 * Check if the function call sample set and trace are complete
  	 * (first sample is at time 1, sample N is at time N)
  	 */
  	public void setComplete()
  	{
  		Object intervalObject = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		int samplingInterval = intervalObject != null ? (Integer) intervalObject : -1; 
  		int size;
  		
  		this.completeTrace = true;
  		
  		size = this.completeGfcTrace.size();
  		for (int i = 0; i < size; i++) {
  			Long[] element = (Long[])this.completeGfcTrace.elementAt(i);
  			if (element[0].longValue() != (i + 1)*samplingInterval) {
  				this.completeTrace = false;
  				break;
  			}
  		}

  		this.completeSamples = true;
  		
  		size = this.samples.size();
  		for (int i = 0; i < size; i++) {
  			GfcSample element = (GfcSample)this.samples.elementAt(i);
  			if (element.sampleSynchTime != (i + 1)*samplingInterval) {
  				this.completeSamples = false;
  				break;
  			}
  		}
  	}
  		
	public CallVisualiser getCallVisualiser()
	{
		return this.callVisualiser;
	}
	
	public void setCallVisualiser(CallVisualiser callVisualiser)
	{
		this.callVisualiser = callVisualiser;
	}
	
	public void setSamplingInterval(int samplingInterval) {
		this.samplingInterval = samplingInterval;
	}
	
	public void refineTrace(FunctionResolver resolver)
	{
		GfcSample.setAddressTraceSamples();
		super.refineTrace(resolver);
	}
	
	public Vector getCompleteGfcTrace()
	{
		return this.completeGfcTrace;
	}

}
