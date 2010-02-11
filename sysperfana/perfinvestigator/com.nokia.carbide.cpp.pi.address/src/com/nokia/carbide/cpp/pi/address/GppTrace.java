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

package com.nokia.carbide.cpp.pi.address;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.BinaryColorPalette;
import com.nokia.carbide.cpp.pi.util.FunctionColorPalette;
import com.nokia.carbide.cpp.pi.util.ThreadColorPalette;


public class GppTrace extends GenericSampledTraceWithFunctions
{
	private static final long serialVersionUID = -658505849351283165L;
	
	// sample times start at a set interval, monotonically increase by that interval, and end at a
	// time equal to (number of elements - 1) times the interval
	private boolean complete;
	
	private transient GppSample[] sortedSamples = null;

	// unchanging set of objects in the trace, ordered by total load
	private transient Vector<ProfiledGeneric> sortedProfiledThreads;
	private transient Vector<ProfiledGeneric> sortedProfiledBinaries;
	private transient Vector<ProfiledGeneric> sortedProfiledFunctions;
	
	// unchanging set of objects in the trace, sorted by index
	private transient Vector<ProfiledGeneric> profiledThreads;
	private transient Vector<ProfiledGeneric> profiledBinaries;
	private transient Vector<ProfiledGeneric> profiledFunctions;
	
	// selection time based objects in the trace
	private transient int startSampleIndex;	// first sample in the selection
	private transient int endSampleIndex;		// last sample in the selection

	// sample counts for the currently selected area of the graph 
	private transient int[] threadSamples;
	private transient int[] binarySamples;
	private transient int[] functionSamples;

	private transient GppTraceGraph[] graphs;
	
	// tie palette to a trace instead of graph since trace is the data it represents
	private transient ThreadColorPalette threadColorPalette = null;
	private transient BinaryColorPalette binaryColorPalette = null;
	private transient FunctionColorPalette functionColorPalette = null;
	
	//protected int uid;
	
	public GppTrace() 
	{
	}
	
	// for fast access, created a sorted array of samples
	// this will remove duplicate times, find if times are missing, and
	// find if times are increasing  
	public void sortGppSamples()
	{
		// check if already sorted
		if (this.sortedSamples != null)
			return;
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		complete = this.samples.size()*samplingInterval == this.getLastSampleNumber();
		
		this.sortedSamples = new GppSample[this.samples.size()];
	
		// fill the sorted array, assuming each sample time matches its (index+1)*samplingInterval
		boolean sorted = true;
		int arrayIndex = 0;
		long lastTime = -1;
		
		for (int i = 0, sampleTime = 0; i < this.samples.size(); i++) {
			Object o = this.samples.get(i);
			if (o instanceof GppSample) {
				  GppSample sample = (GppSample)o;

				  // don't copy duplicates
				  if (sample.sampleSynchTime == lastTime)
					  continue;

				  this.sortedSamples[arrayIndex++] = sample;
				  sampleTime += samplingInterval;
				  
				  // make sure times are in increasing order
				  if (sample.sampleSynchTime < lastTime)
					  sorted = false;
				  
				  lastTime = sample.sampleSynchTime;
				  
				  if (sample.sampleSynchTime != sampleTime) {
					  this.complete = false;
					  
					  // find the time it does match, in case one is missing
					  while (sample.sampleSynchTime > sampleTime)
						  sampleTime += samplingInterval;
				  }
			} else {
				// error case
				this.sortedSamples = new GppSample[1];
				this.sortedSamples[0].sampleSynchTime = -1;
				this.complete = false;
				return;
			}
		}
		
		// if any duplicates, create a shorter array
		if (arrayIndex != this.samples.size()) {
			GppSample[] sampleObjects1 = new GppSample[this.samples.size()];
			for (int i = 0; i < this.sortedSamples.length; i++)
				sampleObjects1[i] = this.sortedSamples[i];
			this.sortedSamples = sampleObjects1;
		}
		
		if (!sorted) {
			// now we have to actually sort
			Arrays.sort(this.sortedSamples, new Comparator<Object>() {
	
				public int compare(Object arg0, Object arg1)
				{
					return (int) (((GppSample)arg0).sampleSynchTime - ((GppSample)arg1).sampleSynchTime);
				}
			});

			// get rid of any duplicates and check for completeness
			for (int i = 0, sampleTime = 0, length = this.sortedSamples.length; i < length; i++) {
				GppSample sample = this.sortedSamples[i];

				// don't copy duplicates
				if (sample.sampleSynchTime == lastTime) {
					for (int j = i; j < length - 1; j++) {
						this.sortedSamples[j] = this.sortedSamples[j + 1];
					}
					length--;
					this.complete = false;
					continue;
				}
				
				sampleTime += samplingInterval;
				
				if (sample.sampleSynchTime != sampleTime) {
					  this.complete = false;
						  
					  // find the time it does match, in case one is missing
					  while (sample.sampleSynchTime > sampleTime)
						  sampleTime += samplingInterval;
			    }
			}
		}
	}
	
	public boolean isGppSampleComplete()
	{
		return this.complete;
	}
	
	public GppSample[] getSortedGppSamples()
	{
		return this.sortedSamples;
	}
	
	public GppSample getGppSample(int number)
	{
		return (GppSample)this.samples.elementAt(number);
	}
	
	public GenericTraceGraph getTraceGraph(int graphIndex, int uid)
	{	
		return getGppGraph(graphIndex,uid);
	}
	
	public GppTraceGraph getGppGraph(int graphIndex, int uid)
	{
		if (graphs == null) {
			graphs = new GppTraceGraph[3];
		}
			
		// note that graphIndex needs not match the index sent to GppTraceGraph
		if (   (graphIndex == PIPageEditor.THREADS_PAGE)
			|| (graphIndex == PIPageEditor.BINARIES_PAGE)
			|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null)
				graphs[graphIndex] = new GppTraceGraph(graphIndex, this, uid);
			return graphs[graphIndex];
		}
	
		return null;
	}
	
	public GenericTraceGraph getTraceGraph(int graphIndex)
	{
		int uid = NpiInstanceRepository.getInstance().activeUid();
		return getTraceGraph(graphIndex, uid);
	}
	
	public void refineTrace(FunctionResolver resolver)
	{
		super.refineTrace(resolver);
	}

	public int getSortedThreadsCount()
	{
		return this.sortedProfiledThreads.size();
	}
	
	public Enumeration<ProfiledGeneric> getSortedThreadsElements()
	{
		return this.sortedProfiledThreads.elements();
	}

	public Vector<ProfiledGeneric> getSortedThreads()
	{
		if (this.sortedProfiledThreads == null)
			this.sortedProfiledThreads = new Vector<ProfiledGeneric>();
		return this.sortedProfiledThreads;
	}

	public Vector<ProfiledGeneric> getIndexedThreads()
	{
		if (this.profiledThreads == null)
			this.profiledThreads = new Vector<ProfiledGeneric>();
		return this.profiledThreads;
	}

	public int getSortedBinariesCount()
	{
	    return this.sortedProfiledBinaries.size();
	}
	
	public Enumeration<ProfiledGeneric> getSortedBinariesElements()
	{
	    return this.sortedProfiledBinaries.elements();
	}
	
	public Vector<ProfiledGeneric> getSortedBinaries()
	{
		if (this.sortedProfiledBinaries == null)
			this.sortedProfiledBinaries = new Vector<ProfiledGeneric>();
	    return this.sortedProfiledBinaries;
	}

	public Vector<ProfiledGeneric> getIndexedBinaries()
	{
		if (this.profiledBinaries == null)
			this.profiledBinaries = new Vector<ProfiledGeneric>();
	    return this.profiledBinaries;
	}

	public int getSortedFunctionsCount()
	{
	    return this.sortedProfiledFunctions.size();
	}
	
	public Enumeration<ProfiledGeneric> getSortedFunctionsElements()
	{
        return this.sortedProfiledFunctions.elements();
	}
	
	public Vector<ProfiledGeneric> getSortedFunctions()
	{
		if (this.sortedProfiledFunctions == null)
			this.sortedProfiledFunctions = new Vector<ProfiledGeneric>();
		return this.sortedProfiledFunctions;
	}
	
	public Vector<ProfiledGeneric> getIndexedFunctions()
	{
		if (this.profiledFunctions == null)
			this.profiledFunctions = new Vector<ProfiledGeneric>();
		return this.profiledFunctions;
	}

	/*
	 *	Determine the threads, binaries, and functions associated with a time period
	 *  from the start time up to and including the end time. If the times are equal,
	 *  do not include any samples.
	 */
	public void setSelectedArea() {
		// create empty arrays to hold sample counts
		this.threadSamples   = new int[this.profiledThreads.size()];
		this.binarySamples   = new int[this.profiledBinaries.size()];
		this.functionSamples = new int[this.profiledFunctions.size()];

		double doubleStartTime = PIPageEditor.currentPageEditor().getStartTime();
		double doubleEndTime   = PIPageEditor.currentPageEditor().getEndTime();

		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		GppSample[] sortedGppSamples = this.getSortedGppSamples();

		// get the sample start time in integer multiples of milliseconds
		startSampleIndex = ((int) ((doubleStartTime + 0.0005f)* 1000.0f))/samplingInterval;
		if (startSampleIndex*samplingInterval < this.getFirstSampleNumber()) {
			startSampleIndex = 0;
		} else if (startSampleIndex > sortedGppSamples.length) {
			startSampleIndex = sortedGppSamples.length - 1;
		}
			
		// get the sample end time in integer multiples of milliseconds
		// NOTE: endSampleIndex is one past the last allowed index, so that when no time
		// is selected startSampleIndex will equal end sample index
		endSampleIndex = ((int) ((doubleEndTime + 0.0005f) * 1000.0f))/samplingInterval;
		if (endSampleIndex*samplingInterval < this.getFirstSampleNumber()) {
			endSampleIndex = 0;
		} else if (endSampleIndex > sortedGppSamples.length) {
			endSampleIndex = sortedGppSamples.length - 1;
		}

		if (this.isGppSampleComplete()) {
			// find the sample counts in each category
			// just use the start and end times as indices
			for (int i = startSampleIndex; i < endSampleIndex; i++) {
				threadSamples[sortedGppSamples[i].threadIndex]++;
				binarySamples[sortedGppSamples[i].binaryIndex]++;
				functionSamples[sortedGppSamples[i].functionIndex]++;
			}
		} else {
			// use a binary search to find the first sample
			int startIndex = 0;
			GppSample sample = null;
			int lowerBound = 0;
			int upperBound = sortedGppSamples.length;
		    while (lowerBound <= upperBound) {
		    	startIndex = (lowerBound + upperBound)/2;
				sample = sortedGppSamples[startIndex];
				if (startSampleIndex*samplingInterval == sample.sampleSynchTime) {
		            break;
				} else if (sample.sampleSynchTime > startSampleIndex*samplingInterval)
		            upperBound = startIndex - 1;
		        else
		        	lowerBound = startIndex + 1;
		    }
		    
		    // if there is no match, it's okay if the sample's time is larger than the
		    // startTime, but not if the startTime is less
		    if (sample.sampleSynchTime < startSampleIndex*samplingInterval)
		    	startIndex++;
	    	endSampleIndex = startIndex;
		    
			// find the sample counts in each category
	    	// use comparisons to find the end sample
		    if (startIndex < this.samples.size()) {
				while (sample.sampleSynchTime < endSampleIndex*samplingInterval) {
					threadSamples[sample.threadIndex]++;
					binarySamples[sample.binaryIndex]++;
					functionSamples[sample.functionIndex]++;
	
					endSampleIndex++;
					if (endSampleIndex == sortedGppSamples.length)
						break;
	
					sample = sortedGppSamples[endSampleIndex];
				}
				
				if (   (endSampleIndex == this.samples.size())
					|| (sample.sampleSynchTime > endSampleIndex*samplingInterval))
					endSampleIndex--;
		    }
		    endSampleIndex++;
		}
		
		// set the sample counts and loads for all the trace-related graphs
		// set the % load strings only for a tab's base table
		// To optimise this, we could just set the main sample count and load per graph (e.g., thread stuff for page 0)
		// To ptimise this, we could check drawMode and ignore sample counts when tables don't show them
		// requires sample counts, a
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		// NOTE: For slightly better performance, might have other functions rely on this one zeroing out sample counts
		for (int i = 0; i < this.profiledThreads.size(); i++) {
			ProfiledGeneric pThread = this.profiledThreads.elementAt(i);
			pThread.setSampleCount(PIPageEditor.THREADS_PAGE, this.threadSamples[i]);
			pThread.setLoadAndString(PIPageEditor.THREADS_PAGE, (float)(this.threadSamples[i] * percentPerSample));
		}

		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledGeneric pBinary = this.profiledBinaries.elementAt(i);
			pBinary.setSampleCount(PIPageEditor.BINARIES_PAGE, this.binarySamples[i]);
			pBinary.setLoadAndString(PIPageEditor.BINARIES_PAGE, (float)(this.binarySamples[i] * percentPerSample));
		}

		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledGeneric pFunction = this.profiledFunctions.elementAt(i);
			pFunction.setSampleCount(PIPageEditor.FUNCTIONS_PAGE, this.functionSamples[i]);
			pFunction.setLoadAndString(PIPageEditor.FUNCTIONS_PAGE, (float)(this.functionSamples[i] * percentPerSample));
		}		
	}

	/*
	 * Based on a graph's set of enabled threads, produce a set of binaries, and
	 * disable all other binaries for that graph
	 */
	public Vector<ProfiledGeneric> setThreadBinary(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphBinaries = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundBinaries = new Hashtable<String,String>();
		
		// disable all binaries for the given graph
		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledGeneric pBinary = this.profiledBinaries.elementAt(i);
			pBinary.setEnabled(graphIndex, false);
		}

		// set up in case we find binaries below the threshold
		boolean lowBinary;
		ProfiledThreshold thresholdBinary = this.graphs[graphIndex].getThresholdBinary();
		thresholdBinary.init(graphIndex);

		// for each binary in the selected sample range, if its thread is enabled, add the binary
		int binaryThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledThread pThread = (ProfiledThread)
							this.profiledThreads.elementAt(sample.threadIndex);

			if (pThread.isEnabled(graphIndex)) {
				ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);
				String binaryName = pBinary.getNameString();

				lowBinary = pBinary.getTotalSampleCount() < binaryThreshold;

				if (!foundBinaries.containsKey(binaryName)) {
					pBinary.setEnabled(graphIndex, true);
					foundBinaries.put(binaryName, binaryName);
					if (lowBinary) {
						thresholdBinary.addItem(graphIndex, pBinary, 1);
					} else {
						pBinary.setSampleCount(graphIndex, 1);
						graphBinaries.add(pBinary);
					}
				} else {
					if (lowBinary)
						thresholdBinary.incSampleCount(graphIndex);
					else
						pBinary.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) graphBinaries.elementAt(i);
			pBinary.setLoadAndString(graphIndex, (float)(pBinary.getSampleCount(graphIndex) * percentPerSample));
		}

		return graphBinaries;
	}

	/*
	 * Based on a graph's set of enabled threads and binaries, produce a set of functions, and
	 * disable all other functions for that graph
	 */
	public Vector<ProfiledGeneric> setThreadBinaryFunction(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphFunctions = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundFunctions = new Hashtable<String,String>();
		
		// disable all functions for the given graph
		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(i);
			pFunction.setEnabled(graphIndex, false);
		}

		// set up in case we find functions below the threshold
		boolean lowFunction;
		ProfiledThreshold thresholdFunction = this.graphs[graphIndex].getThresholdFunction();
		thresholdFunction.init(graphIndex);

		// for each function in the selected sample range, if its thread and binary are enabled,
		// add the function
		int functionThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);

			if (pThread.isEnabled(graphIndex) && pBinary.isEnabled(graphIndex)) {
				ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);
				String functionName = pFunction.getNameString();

				lowFunction = pFunction.getTotalSampleCount() < functionThreshold;

				if (!foundFunctions.containsKey(functionName)) {
					pFunction.setEnabled(graphIndex, true);
					foundFunctions.put(functionName, functionName);
					if (lowFunction) {
						thresholdFunction.addItem(graphIndex, pFunction, 1);
					} else {
						pFunction.setSampleCount(graphIndex, 1);
						graphFunctions.add(pFunction);
					}
				} else {
					if (lowFunction)
						thresholdFunction.incSampleCount(graphIndex);
					else
						pFunction.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) graphFunctions.elementAt(i);
			pFunction.setLoadAndString(graphIndex, (float)(pFunction.getSampleCount(graphIndex)* percentPerSample));
		}		

		return graphFunctions;
	}

	public Vector<ProfiledGeneric> setBinaryThreadFunction(int graphIndex)
	{
		return setThreadBinaryFunction(graphIndex);
	}
	
	/*
	 * Based on a graph's set of enabled threads, produce a set of functions, and
	 * disable all other functions for that graph
	 */
	public Vector<ProfiledGeneric> setThreadFunction(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphFunctions = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundFunctions = new Hashtable<String,String>();
		
		// disable all functions for the given graph
		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(i);
			pFunction.setEnabled(graphIndex, false);
		}

		// set up in case we find functions below the threshold
		boolean lowFunction;
		ProfiledThreshold thresholdFunction = this.graphs[graphIndex].getThresholdFunction();
		thresholdFunction.init(graphIndex);

		// for each function in the selected sample range, if its thread is enabled, add the function
		int functionThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);

			if (pThread.isEnabled(graphIndex)) {
				ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);
				String functionName = pFunction.getNameString();

				lowFunction = pFunction.getTotalSampleCount() < functionThreshold;

				if (!foundFunctions.containsKey(functionName)) {
					pFunction.setEnabled(graphIndex, true);
					foundFunctions.put(functionName, functionName);
					if (lowFunction) {
						thresholdFunction.addItem(graphIndex, pFunction, 1);
					} else {
						pFunction.setSampleCount(graphIndex, 1);
						graphFunctions.add(pFunction);
					}
				} else {
					if (lowFunction)
						thresholdFunction.incSampleCount(graphIndex);
					else
						pFunction.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) graphFunctions.elementAt(i);
			pFunction.setLoadAndString(graphIndex, (float)(pFunction.getSampleCount(graphIndex) * percentPerSample));
		}		
		
		return graphFunctions;
	}

	/*
	 * Based on a graph's set of enabled threads and functions, produce a set of binaries, and
	 * disable all other binaries for that graph
	 */
	public Vector<ProfiledGeneric> setThreadFunctionBinary(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphBinaries = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundBinaries = new Hashtable<String,String>();
		
		// disable all binaries for the given graph
		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(i);
			pBinary.setEnabled(graphIndex, false);
		}

		// set up in case we find binaries below the threshold
		boolean lowBinary;
		ProfiledThreshold thresholdBinary = this.graphs[graphIndex].getThresholdBinary();
		thresholdBinary.init(graphIndex);

		// for each binary in the selected sample range, if its thread and function are enabled,
		// add the binary
		int binaryThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);

			if (pThread.isEnabled(graphIndex) && pFunction.isEnabled(graphIndex)) {
				ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);
				String binaryName = pBinary.getNameString();

				lowBinary = pBinary.getTotalSampleCount() < binaryThreshold;

				if (!foundBinaries.containsKey(binaryName)) {
					pBinary.setEnabled(graphIndex, true);
					foundBinaries.put(binaryName, binaryName);
					if (lowBinary) {
						thresholdBinary.addItem(graphIndex, pBinary, 1);
					} else {
						pBinary.setSampleCount(graphIndex, 1);
						graphBinaries.add(pBinary);
					}
				} else {
					if (lowBinary)
						thresholdBinary.incSampleCount(graphIndex);
					else
						pBinary.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) graphBinaries.elementAt(i);
			pBinary.setLoadAndString(graphIndex, (float)(pBinary.getSampleCount(graphIndex) * percentPerSample));
		}
		
		return graphBinaries;
	}

	public Vector<ProfiledGeneric> setFunctionThreadBinary(int graphIndex)
	{
		return setThreadFunctionBinary(graphIndex);
	}

	/*
	 * Based on a graph's set of enabled binaries, produce a set of threads, and
	 * disable all other threads for that graph
	 */
	public Vector<ProfiledGeneric> setBinaryThread(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphThreads = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundThreads = new Hashtable<String,String>();
		
		// disable all threads for the given graph
		for (int i = 0; i < this.profiledThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(i);
			pThread.setEnabled(graphIndex, false);
		}

		// set up in case we find threads below the threshold
		boolean lowThread;
		ProfiledThreshold thresholdThread = this.graphs[graphIndex].getThresholdThread();
		thresholdThread.init(graphIndex);

		// for each thread in the selected sample range, if its binary is enabled, add the thread
		int threadThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);

			if (pBinary.isEnabled(graphIndex)) {
				ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);
				String threadName = pThread.getNameString();

				lowThread = pThread.getTotalSampleCount() < threadThreshold;

				if (!foundThreads.containsKey(threadName)) {
					pThread.setEnabled(graphIndex, true);
					foundThreads.put(threadName, threadName);
					if (lowThread) {
						thresholdThread.addItem(graphIndex, pThread, 1);
					} else {
						pThread.setSampleCount(graphIndex, 1);
						graphThreads.add(pThread);
					}
				} else {
					if (lowThread)
						thresholdThread.incSampleCount(graphIndex);
					else
						pThread.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) graphThreads.elementAt(i);
			pThread.setLoadAndString(graphIndex, (float)(pThread.getSampleCount(graphIndex) * percentPerSample));
		}
		
		return graphThreads;
	}

	/*
	 * Based on a graph's set of enabled functions, produce a set of threads, and
	 * disable all other threads for that graph
	 */
	public Vector<ProfiledGeneric> setFunctionThread(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphThreads = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundThreads = new Hashtable<String,String>();
		
		// disable all threads for the given graph
		for (int i = 0; i < this.profiledThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(i);
			pThread.setEnabled(graphIndex, false);
		}

		// set up in case we find threads below the threshold
		boolean lowThread;
		ProfiledThreshold thresholdThread = this.graphs[graphIndex].getThresholdThread();
		thresholdThread.init(graphIndex);

		// for each thread in the selected sample range, if its function is enabled, add the thread
		int threadThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);

			if (pFunction.isEnabled(graphIndex)) {
				ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);
				String threadName = pThread.getNameString();

				lowThread = pThread.getTotalSampleCount() < threadThreshold;

				if (!foundThreads.containsKey(threadName)) {
					pThread.setEnabled(graphIndex, true);
					foundThreads.put(threadName, threadName);
					if (lowThread) {
						thresholdThread.addItem(graphIndex, pThread, 1);
					} else {
						pThread.setSampleCount(graphIndex, 1);
						graphThreads.add(pThread);
					}
				} else {
					if (lowThread)
						thresholdThread.incSampleCount(graphIndex);
					else
						pThread.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) graphThreads.elementAt(i);
			pThread.setLoadAndString(graphIndex, (float)(pThread.getSampleCount(graphIndex) * percentPerSample));
		}
		
		return graphThreads;
	}

	/*
	 * Based on a graph's set of enabled binaries and functions, produce a set of threads, and
	 * disable all other threads for that graph
	 */
	public Vector<ProfiledGeneric> setBinaryFunctionThread(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphThreads = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundThreads = new Hashtable<String,String>();
		
		// disable all threads for the given graph
		for (int i = 0; i < this.profiledThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(i);
			pThread.setEnabled(graphIndex, false);
		}

		// set up in case we find threads below the threshold
		boolean lowThread;
		ProfiledThreshold thresholdThread = this.graphs[graphIndex].getThresholdThread();
		thresholdThread.init(graphIndex);

		// for each thread in the selected sample range, if its binary and function are enabled,
		// add the thread
		int threadThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);

			if (pBinary.isEnabled(graphIndex)
	 		    && (pFunction.isEnabled(graphIndex))) {
				ProfiledThread pThread = (ProfiledThread) this.profiledThreads.elementAt(sample.threadIndex);
				String threadName = pThread.getNameString();

				lowThread = pThread.getTotalSampleCount() < threadThreshold;

				if (!foundThreads.containsKey(threadName)) {
					pThread.setEnabled(graphIndex, true);
					foundThreads.put(threadName, threadName);
					if (lowThread) {
						thresholdThread.addItem(graphIndex, pThread, 1);
					} else {
						pThread.setSampleCount(graphIndex, 1);
						graphThreads.add(pThread);
					}
				} else {
					if (lowThread)
						thresholdThread.incSampleCount(graphIndex);
					else
						pThread.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) graphThreads.elementAt(i);
			pThread.setLoadAndString(graphIndex, (float)(pThread.getSampleCount(graphIndex) * percentPerSample));
		}
		
		return graphThreads;
	}

	public Vector<ProfiledGeneric> setFunctionBinaryThread(int graphIndex)
	{
		return setBinaryFunctionThread(graphIndex);
	}

	/*
	 * Based on a graph's set of enabled functions, produce a set of binaries, and
	 * disable all other binaries for that graph
	 */
	public Vector<ProfiledGeneric> setFunctionBinary(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphBinaries = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundBinaries = new Hashtable<String,String>();
		
		// disable all binaries for the given graph
		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(i);
			pBinary.setEnabled(graphIndex, false);
		}

		// set up in case we find binaries below the threshold
		boolean lowBinary;
		ProfiledThreshold thresholdBinary = this.graphs[graphIndex].getThresholdBinary();
		thresholdBinary.init(graphIndex);

		// for each binary in the selected sample range, if its function is enabled, add the binary
		int binaryThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);

			if (pFunction.isEnabled(graphIndex)) {
				ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);
				String binaryName = pBinary.getNameString();

				lowBinary = pBinary.getTotalSampleCount() < binaryThreshold;

				if (!foundBinaries.containsKey(binaryName)) {
					pBinary.setEnabled(graphIndex, true);
					foundBinaries.put(binaryName, binaryName);
					if (lowBinary) {
						thresholdBinary.addItem(graphIndex, pBinary, 1);
					} else {
						pBinary.setSampleCount(graphIndex, 1);
						graphBinaries.add(pBinary);
					}
				} else {
					if (lowBinary)
						thresholdBinary.incSampleCount(graphIndex);
					else
						pBinary.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) graphBinaries.elementAt(i);
			pBinary.setLoadAndString(graphIndex, (float)(pBinary.getSampleCount(graphIndex) * percentPerSample));
		}
		
		return graphBinaries;
	}

	/*
	 * Based on a graph's set of enabled binaries, produce a set of functions, and
	 * disable all other functions for that graph
	 */
	public Vector<ProfiledGeneric> setBinaryFunction(int graphIndex)
	{
		Vector<ProfiledGeneric>  graphFunctions = new Vector<ProfiledGeneric>();
		Hashtable<String,String> foundFunctions = new Hashtable<String,String>();
		
		// disable all functions for the given graph
		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(i);
			pFunction.setEnabled(graphIndex, false);
		}

		// set up in case we find functions below the threshold
		boolean lowFunction;
		ProfiledThreshold thresholdFunction = this.graphs[graphIndex].getThresholdFunction();
		thresholdFunction.init(graphIndex);

		// for each function in the selected sample range, if its binary is enabled, add the function
		int functionThreshold = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries.elementAt(sample.binaryIndex);

			if (pBinary.isEnabled(graphIndex)) {
				ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions.elementAt(sample.functionIndex);
				String functionName = pFunction.getNameString();

				lowFunction = pFunction.getTotalSampleCount() < functionThreshold;

				if (!foundFunctions.containsKey(functionName)) {
					pFunction.setEnabled(graphIndex, true);
					foundFunctions.put(functionName, functionName);
					if (lowFunction) {
						thresholdFunction.addItem(graphIndex, pFunction, 1);
					} else {
						pFunction.setSampleCount(graphIndex, 1);
						graphFunctions.add(pFunction);
					}
				} else {
					if (lowFunction)
						thresholdFunction.incSampleCount(graphIndex);
					else
						pFunction.incSampleCount(graphIndex);
				}
			}
		}
		
		// since we are not converting float % load to string % load inside the table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double)(this.endSampleIndex - this.startSampleIndex));

		for (int i = 0; i < graphFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) graphFunctions.elementAt(i);
			pFunction.setLoadAndString(graphIndex, (float)(pFunction.getSampleCount(graphIndex) * percentPerSample));
		}		
		
		return graphFunctions;
	}
	
	public int getStartSampleIndex()
	{
		return this.startSampleIndex;
	}
	
	public int getEndSampleIndex()
	{
		return this.endSampleIndex;
	}
	
	public int[] getThreadSampleCounts()
	{
		return this.threadSamples;
	}
	
	public int[] getBinarySampleCounts()
	{
		return this.binarySamples;
	}
	
	public int[] getFunctionSampleCounts()
	{
		return this.functionSamples;
	}

	public void setThreadSampleCounts(int[] sampleCounts)
	{
		this.threadSamples = sampleCounts;
	}
	
	public void setBinarySampleCounts(int[] sampleCounts)
	{
		this.binarySamples = sampleCounts;
	}
	
	public void setFunctionSampleCounts(int[] sampleCounts)
	{
		this.functionSamples = sampleCounts;
	}
	
	public void setThreadColorPalette(ThreadColorPalette tableColorPalette) {
		this.threadColorPalette = tableColorPalette;
	}

	public ThreadColorPalette getThreadColorPalette() {
		if (this.threadColorPalette == null) {
			this.threadColorPalette = new ThreadColorPalette();
		}
		return this.threadColorPalette;
	}

	public void setBinaryColorPalette(BinaryColorPalette tableColorPalette) {
		this.binaryColorPalette = tableColorPalette;
	}

	public BinaryColorPalette getBinaryColorPalette() {
		if (this.binaryColorPalette == null) {
			this.binaryColorPalette = new BinaryColorPalette();
		}
		return this.binaryColorPalette;
	}

	public void setFunctionColorPalette(FunctionColorPalette tableColorPalette) {
		this.functionColorPalette = tableColorPalette;
	}

	public FunctionColorPalette getFunctionColorPalette() {
		if (this.functionColorPalette == null) {
			this.functionColorPalette = new FunctionColorPalette();
		}
		return this.functionColorPalette;
	}

	@Override
	public void finalizeTrace() {
		samples.trimToSize();
		for (int i = 0; i < samples.size(); i++) {
			GppSample sample = (GppSample) samples.get(i);
			
			if (sample.currentFunctionItt == null && sample.currentFunctionSym == null) {
				sample.currentFunctionItt = new Function(Messages.getString("GppTrace.functionNotFound1") + Long.toHexString(sample.programCounter) + Messages.getString("GppTrace.functionNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
						new Long(sample.programCounter),
						Messages.getString("GppTrace.binaryNotFound1") +  Long.toHexString(sample.programCounter) + Messages.getString("GppTrace.binarynotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
