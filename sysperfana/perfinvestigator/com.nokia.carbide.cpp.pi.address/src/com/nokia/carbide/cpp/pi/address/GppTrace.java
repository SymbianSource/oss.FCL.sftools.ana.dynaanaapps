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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.nokia.carbide.cpp.internal.pi.address.GppModelAdapter;
import com.nokia.carbide.cpp.internal.pi.address.GppTraceGraphSMP;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.BinaryColorPalette;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.util.FunctionColorPalette;
import com.nokia.carbide.cpp.pi.util.ThreadColorPalette;

/**
 * Data model of the GPP trace (General Purpose Processor Trace). This class
 * also creates the graphs and the ColorPalette that represent the trace.
 * 
 */
public class GppTrace extends GenericSampledTraceWithFunctions {
	private static final long serialVersionUID = -658505849351283165L;

	// sample times start at a set interval, monotonically increase by that
	// interval, and end at a
	// time equal to (number of elements - 1) times the interval
	private boolean complete;
	
	private int graphCount = -1;

	private transient GppSample[] sortedSamples = null;

	// unchanging set of objects in the trace, ordered by total load
	private transient Vector<ProfiledGeneric> sortedProfiledThreads;
	private transient Vector<ProfiledGeneric> sortedProfiledBinaries;
	private transient Vector<ProfiledGeneric> sortedProfiledFunctions;

	// unchanging set of objects in the trace, sorted by index
	private transient Vector<ProfiledThread> profiledThreads;
	private transient Vector<ProfiledBinary> profiledBinaries;
	private transient Vector<ProfiledFunction> profiledFunctions;

	// selection time based objects in the trace
	private transient int startSampleIndex; // first sample in the selection
	private transient int endSampleIndex; // last sample in the selection

	private transient GppTraceGraph[] graphs;
	private transient GppTraceGraphSMP[] smpGraph;

	// TODO: This should be in the GppTraceGraph and not in the model!
	// tie palette to a trace instead of graph since trace is the data it
	// represents
	private transient ThreadColorPalette threadColorPalette = null;
	private transient BinaryColorPalette binaryColorPalette = null;
	private transient FunctionColorPalette functionColorPalette = null;

	/** number of CPUs in the trace, should be 1 for non-SMP */
	private int cpuCount;
	/**
	 * indicator whether this trace is SMP; false if cpuCount == 1, true if
	 * greater
	 */
	private boolean isSMP;

	// the following structures are for managing legend views in tabFolders
	// this should really go into a GUI-related class but we haven't got 
	// one with visibility of all graphs
	//
	/** tabFolders for the legend views, up to one per page */
	private transient TabFolderWrapper[] legendTabFolders;

	/**
	 * Default constructor
	 */
	public GppTrace() {
		cpuCount = 1; // default
	}

	/**
	 * for fast access, created a sorted array of samples this will remove
	 * duplicate times, find if times are missing, and find if times are
	 * increasing
	 */
	public void sortGppSamples() {
		// check if already sorted
		if (this.sortedSamples != null)
			return;

		complete = true;
		List<GppSample> sortedSamplesList = new ArrayList<GppSample>();
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		// add samples to the list and sort the list
		for (GenericSample sample : this.samples) {
			if (sample instanceof GppSample) {
				sortedSamplesList.add((GppSample) sample);
			} else {
				// error case
				this.sortedSamples = new GppSample[1];
				this.sortedSamples[0].sampleSynchTime = -1;
				this.complete = false;
				return;
			}
		}
		Collections.sort(sortedSamplesList, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				return (int) (((GppSample) arg0).sampleSynchTime - ((GppSample) arg1).sampleSynchTime);
			}
		});

		// copy to array while ignoring any duplicates and checking for
		// completeness
		GppSample[] mySortedSamples = new GppSample[this.samples.size()];
		long[] sampleTime = new long[cpuCount];
		for (int cpu = 0; cpu < cpuCount; cpu++) {
			sampleTime[cpu] = -1;
		}
		int i = 0;
		for (GppSample gppSample : sortedSamplesList) {
			int cpu = gppSample.cpuNumber < 0 ? 0 : gppSample.cpuNumber;
			if (sampleTime[cpu] == -1
					|| gppSample.sampleSynchTime > sampleTime[cpu]) {
				if (sampleTime[cpu] != -1
						&& gppSample.sampleSynchTime != sampleTime[cpu]
								+ samplingInterval) {
					complete = false;// gap to previous is larger than sampling
										// interval
				}
				sampleTime[cpu] = gppSample.sampleSynchTime;
				mySortedSamples[i] = gppSample;
				i++;
			}
		}
		if (i < sortedSamplesList.size()) {
			sortedSamples = new GppSample[i];
			System.arraycopy(mySortedSamples, 0, sortedSamples, 0, i);
			complete = false; // due to duplicates
		} else {
			sortedSamples = mySortedSamples;
		}
	}

	/**
	 * Returns true if the samples in the trace were complete, i.e. no
	 * duplicates were found and samples adhered to the sampling interval
	 * 
	 * @return true if complete, false otherwise
	 */
	public boolean isGppSampleComplete() {
		return this.complete;
	}

	/**
	 * Array of GppSamples sorted ascending by their sampling time
	 * 
	 * @return
	 */
	public GppSample[] getSortedGppSamples() {
		return this.sortedSamples;
	}

	/**
	 * Returns the sample at the given index from the raw collection of samples
	 * (i.e. may not be not sorted)
	 * 
	 * @param number
	 *            the index of the sample to return
	 * @return
	 */
	public GppSample getGppSample(int number) {
		return (GppSample) this.samples.elementAt(number);
	}

	/**
	 * Returns the IGenericTraceGraph associated with the given graphIndex
	 * 
	 * @param graphIndex
	 *            the graphIndex to use
	 * @param uid
	 *            the uid to use
	 * @return
	 */
	public IGppTraceGraph getTraceGraph(int graphIndex, int uid) {
		return getGppGraph(graphIndex, uid);
	}

	/**
	 * Returns the IGppTraceGraph associated with the given graphIndex. This
	 * will create the IGppTraceGraph if it doesn't already exist.
	 * 
	 * @param graphIndex
	 *            the graphIndex to use
	 * @param uid
	 *            the uid to use
	 * @return
	 */
	public IGppTraceGraph getGppGraph(int graphIndex, int uid) {
		if (graphs == null) {
			graphs = new GppTraceGraph[3];
		}

		// note that graphIndex needs not match the index sent to GppTraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null){
				graphs[graphIndex] = new GppTraceGraph(graphIndex, this, uid);
				graphs[graphIndex].init(GppTraceUtil.getPageIndex(graphIndex), this);
				
			}
			return graphs[graphIndex];
		} else if (isSingleCPUModeonSMP(graphIndex)
				&& graphIndex <= PIPageEditor.FUNCTIONS_PAGE + cpuCount) {
			
			//a single-CPU threads graph on an SMP trace 
			int cpuIndex = graphIndex - 3; // - PAGE_NUMBER
			if (smpGraph == null) {
				smpGraph = new GppTraceGraphSMP[cpuCount];
			}
			if (smpGraph[cpuIndex] == null){
				smpGraph[cpuIndex] = new GppTraceGraphSMP(graphIndex, this, uid,
						cpuIndex, PIPageEditor.THREADS_PAGE);	
				smpGraph[cpuIndex].init(PIPageEditor.THREADS_PAGE, this);
			}
			return smpGraph[cpuIndex];
		}
		return null;
	}


	/**
	 * Convenience method which will call {@link #getTraceGraph(int, int)} using
	 * the activeUid()
	 * 
	 * @param graphIndex
	 *            the graphIndex to use
	 * @return IGenericTraceGraph for the given graphIndex and the active Uid
	 */
	public IGppTraceGraph getTraceGraph(int graphIndex) {
		int uid = NpiInstanceRepository.getInstance().activeUid();
		return getTraceGraph(graphIndex, uid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions
	 * #refineTrace(com.nokia.carbide.cpp.internal.pi.model.FunctionResolver)
	 */
	@Override
	public void refineTrace(FunctionResolver resolver) {
		super.refineTrace(resolver);
	}

	/**
	 * 
	 * @return the number of ProfiledThreads in the trace
	 */
	public int getSortedThreadsCount() {
		return this.sortedProfiledThreads.size();
	}

	/**
	 * Returns the sorted ProfiledThreads in the trace
	 * 
	 * @return
	 */
	public Enumeration<ProfiledGeneric> getSortedThreadsElements() {
		return this.sortedProfiledThreads.elements();
	}

	/**
	 * Returns the sorted ProfiledThreads Vector
	 * 
	 * @return
	 */
	public Vector<ProfiledGeneric> getSortedThreads() {
		if (this.sortedProfiledThreads == null)
			this.sortedProfiledThreads = new Vector<ProfiledGeneric>();
		return this.sortedProfiledThreads;
	}

	/**
	 * Returns the ProfiledThreads in the trace sorted by thread index (an
	 * ordinal which gets set when ProfiledThreads are created).
	 * 
	 * @return
	 */
	public Vector<ProfiledThread> getIndexedThreads() {
		if (this.profiledThreads == null)
			this.profiledThreads = new Vector<ProfiledThread>();
		return this.profiledThreads;
	}

	public int getSortedBinariesCount() {
		return this.sortedProfiledBinaries.size();
	}

	public Enumeration<ProfiledGeneric> getSortedBinariesElements() {
		return this.sortedProfiledBinaries.elements();
	}

	public Vector<ProfiledGeneric> getSortedBinaries() {
		if (this.sortedProfiledBinaries == null)
			this.sortedProfiledBinaries = new Vector<ProfiledGeneric>();
		return this.sortedProfiledBinaries;
	}

	public Vector<ProfiledBinary> getIndexedBinaries() {
		if (this.profiledBinaries == null)
			this.profiledBinaries = new Vector<ProfiledBinary>();
		return this.profiledBinaries;
	}

	public int getSortedFunctionsCount() {
		return this.sortedProfiledFunctions.size();
	}

	public Enumeration<ProfiledGeneric> getSortedFunctionsElements() {
		return this.sortedProfiledFunctions.elements();
	}

	public Vector<ProfiledGeneric> getSortedFunctions() {
		if (this.sortedProfiledFunctions == null)
			this.sortedProfiledFunctions = new Vector<ProfiledGeneric>();
		return this.sortedProfiledFunctions;
	}

	public Vector<ProfiledFunction> getIndexedFunctions() {
		if (this.profiledFunctions == null)
			this.profiledFunctions = new Vector<ProfiledFunction>();
		return this.profiledFunctions;
	}

	/**
	 * Determine the threads, binaries, and functions associated with a time
	 * period from the start time up to and including the end time. If the times
	 * are equal, do not include any samples.
	 * <p>
	 * Calculates and sets: <br>
	 * - startSampleIndex <br>
	 * - endSampleIndex
	 * <p>
	 * and for each ProfiledGeneric, calls <br>
	 * - setSampleCount() <br>
	 * - setLoadAndString()
	 * 
	 * @param startTime
	 *            start time of the selected area in seconds
	 * @param endTime
	 *            end time of the selected area in seconds
	 */
	public void setSelectedArea(final double startTime, final double endTime) {

		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		GppSample[] sortedGppSamples = this.getSortedGppSamples();

		// get the sample start time in integer multiples of milliseconds
		// the -1 is important, since sample.sampleSynchTime is one-based (not
		// zero-based!)
		startSampleIndex = ((((int) ((startTime + 0.0005f) * 1000.0f)) - 1)
				* cpuCount / (samplingInterval));
		if (startSampleIndex * samplingInterval < this.getFirstSampleNumber()) {
			startSampleIndex = 0;
		} else if (startSampleIndex > sortedGppSamples.length) {
			startSampleIndex = sortedGppSamples.length - 1;
		}

		// get the sample end time in integer multiples of milliseconds
		// NOTE: endSampleIndex is one past the last allowed index, so that when
		// no time
		// is selected startSampleIndex will equal end sample index

		// for SMP, endSampleIndex points to the first sample past this
		// timestamp
		endSampleIndex = (((int) ((endTime + 0.0005f) * 1000.0f)) * cpuCount / (samplingInterval));
		if (endSampleIndex * samplingInterval < this.getFirstSampleNumber()) {
			endSampleIndex = 0;
		} else if (endSampleIndex > sortedGppSamples.length) {
			endSampleIndex = sortedGppSamples.length;
		}

		// TODO: CH: if the start and end index haven't changed (compared to
		// previous), we could return here

		// create empty arrays to hold total sample counts per index of
		// ProfiledGeneric
		int[] threadSamples = new int[this.profiledThreads.size()];
		int[] binarySamples = new int[this.profiledBinaries.size()];
		int[] functionSamples = new int[this.profiledFunctions.size()];

		int[][] threadSamplesSMP = null;
		int[][] binarySamplesSMP = null;
		int[][] functionSamplesSMP = null;
		int[] totalSamplesPerCPU = null;

		if (isSMP) {
			threadSamplesSMP = new int[cpuCount][];
			binarySamplesSMP = new int[cpuCount][];
			functionSamplesSMP = new int[cpuCount][];
			for (int cpu = 0; cpu < cpuCount; cpu++) {
				threadSamplesSMP[cpu] = new int[this.profiledThreads.size()];
				binarySamplesSMP[cpu] = new int[this.profiledBinaries.size()];
				functionSamplesSMP[cpu] = new int[this.profiledFunctions.size()];
			}
			totalSamplesPerCPU = new int[cpuCount];
		}

		if (this.isGppSampleComplete()) {
			// find the sample counts in each category
			// just use the start and end times as indices
			for (int i = startSampleIndex; i < endSampleIndex; i++) {
				GppSample sample = sortedGppSamples[i];
				threadSamples[sample.threadIndex]++;
				binarySamples[sample.binaryIndex]++;
				functionSamples[sample.functionIndex]++;
				if (isSMP) {
					threadSamplesSMP[sample.cpuNumber][sample.threadIndex]++;
					binarySamplesSMP[sample.cpuNumber][sample.binaryIndex]++;
					functionSamplesSMP[sample.cpuNumber][sample.functionIndex]++;
					totalSamplesPerCPU[sample.cpuNumber]++;
				}
			}
		} else {
			// use a binary search to find the first sample
			int startIndex = 0;
			GppSample sample = null;
			int lowerBound = 0;
			int upperBound = sortedGppSamples.length;
			while (lowerBound <= upperBound) {
				startIndex = (lowerBound + upperBound) / 2;
				sample = sortedGppSamples[startIndex];
				if (startSampleIndex * samplingInterval == sample.sampleSynchTime) {
					break;
				} else if (sample.sampleSynchTime > startSampleIndex
						* samplingInterval)
					upperBound = startIndex - 1;
				else
					lowerBound = startIndex + 1;
			}

			// if there is no match, it's okay if the sample's time is larger
			// than the
			// startTime, but not if the startTime is less
			if (sample.sampleSynchTime < startSampleIndex * samplingInterval)
				startIndex++;
			endSampleIndex = startIndex;

			// find the sample counts in each category
			// use comparisons to find the end sample
			if (startIndex < this.samples.size()) {
				while (sample.sampleSynchTime < endSampleIndex
						* samplingInterval) {
					threadSamples[sample.threadIndex]++;
					binarySamples[sample.binaryIndex]++;
					functionSamples[sample.functionIndex]++;
					if (isSMP) {
						threadSamplesSMP[sample.cpuNumber][sample.threadIndex]++;
						binarySamplesSMP[sample.cpuNumber][sample.binaryIndex]++;
						functionSamplesSMP[sample.cpuNumber][sample.functionIndex]++;
						totalSamplesPerCPU[sample.cpuNumber]++;
					}

					endSampleIndex++;
					if (endSampleIndex == sortedGppSamples.length)
						break;

					sample = sortedGppSamples[endSampleIndex];
				}

				if ((endSampleIndex == this.samples.size())
						|| (sample.sampleSynchTime > endSampleIndex
								* samplingInterval))
					endSampleIndex--;
			}
			endSampleIndex++;
		}

		// set the sample counts and loads for all the trace-related graphs
		// set the % load strings only for a tab's base table
		// To optimise this, we could just set the main sample count and load
		// per graph (e.g., thread stuff for page 0)
		// To ptimise this, we could check drawMode and ignore sample counts
		// when tables don't show them
		// requires sample counts, a
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else
			percentPerSample = 100.0 / ((double) (this.endSampleIndex - this.startSampleIndex));

		// NOTE: For slightly better performance, might have other functions
		// rely on this one zeroing out sample counts
		for (int i = 0; i < profiledThreads.size(); i++) {
			ProfiledGeneric pThread = this.profiledThreads.elementAt(i);
			pThread.setSampleCount(PIPageEditor.THREADS_PAGE, threadSamples[i]);
			pThread.setLoadAndString(PIPageEditor.THREADS_PAGE,
					(float) (threadSamples[i] * percentPerSample));
			if (isSMP) {
				for (int cpu = 0; cpu < cpuCount; cpu++) {
					//for now set this for the thread SMP graphs only
					int graphIndex = cpu + 3;
					pThread.setSampleCount(graphIndex, threadSamplesSMP[cpu][i]);
					pThread.setLoadAndString(graphIndex, (float) threadSamplesSMP[cpu][i] * 100 / totalSamplesPerCPU[cpu]);
				}
			}
		}

		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledGeneric pBinary = this.profiledBinaries.elementAt(i);
			pBinary.setSampleCount(PIPageEditor.BINARIES_PAGE, binarySamples[i]);
			pBinary.setLoadAndString(PIPageEditor.BINARIES_PAGE, (float) (binarySamples[i] * percentPerSample));
			if (isSMP) {
				//for now set this for the thread SMP graphs only
				for (int cpu = 0; cpu < cpuCount; cpu++) {
					int graphIndex = cpu + 3;
					pBinary.setSampleCount(graphIndex, binarySamplesSMP[cpu][i]);
					pBinary.setLoadAndString(graphIndex, (float) binarySamplesSMP[cpu][i] * 100	/ totalSamplesPerCPU[cpu]);
				}
			}
		}

		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledGeneric pFunction = this.profiledFunctions.elementAt(i);
			pFunction.setSampleCount(PIPageEditor.FUNCTIONS_PAGE, functionSamples[i]);
			pFunction.setLoadAndString(PIPageEditor.FUNCTIONS_PAGE, (float) (functionSamples[i] * percentPerSample));
			if (isSMP) {
				//for now set this for the thread SMP graphs only
				for (int cpu = 0; cpu < cpuCount; cpu++) {
					int graphIndex = cpu + 3;
					pFunction.setSampleCount(graphIndex, functionSamplesSMP[cpu][i]);
					pFunction.setLoadAndString(graphIndex, (float) functionSamplesSMP[cpu][i] * 100	/ totalSamplesPerCPU[cpu]);
				}
			}
		}
	}

	/**
	 * Based on a graph's set of enabled threads, produce a set of binaries, and
	 * disable all other binaries for that graph
	 * 
	 * @param graphIndex
	 *            index of the graph to operate on
	 * @param adapter a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param binaries Collection of binaries with their sample count within selected
	 *         area and filtered by selected threads in the given
	 *         graph. This collection will be cleared and then updated.
	 */
	public void setThreadBinary(int graphIndex,
			GppModelAdapter adapter, Vector<ProfiledGeneric> binaries) {
		internalSetBinary(graphIndex, adapter, binaries, false, true);
		
	}

	/**
	 * Internal implementation of {@link #setBinaryFunction(int)},
	 * {@link #setBinaryThreadFunction(int, GppModelAdapter)},
	 * {@link #setThreadBinaryFunction(int, GppModelAdapter)},
	 * {@link #setThreadFunction(int, GppModelAdapter)}
	 * 
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphFunctions Collection of functions with their sample count within selected
	 *         area and filtered by selected threads and / or binaries in the
	 *         given graph
	 * @param noThreads
	 * @param noBinaries
	 */
	private void internalSetFunction(int graphIndex,
			GppModelAdapter adapter, Vector<ProfiledGeneric> graphFunctions, boolean noThreads, boolean noBinaries) {
		Hashtable<String, String> foundFunctions = new Hashtable<String, String>();

		// disable all functions for the given graph
		for (int i = 0; i < this.profiledFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions
					.elementAt(i);
			pFunction.setEnabled(graphIndex, false);
		}
		graphFunctions.clear();

		// set up in case we find functions below the threshold
		boolean lowFunction;
		ProfiledThreshold thresholdFunction = adapter.getThresholdItem(this, graphIndex, PIPageEditor.FUNCTIONS_PAGE);
		adapter.init(thresholdFunction, graphIndex);

		// for each function in the selected sample range, if its thread and
		// binary are enabled,
		// add the function
		int functionThreshold = (Integer) NpiInstanceRepository
				.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
		int[] totalSamplesPerCPU = null;
		if (isSingleCPUModeonSMP(graphIndex)) {
			totalSamplesPerCPU = new int[cpuCount];
		}

		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			if (isSingleCPUModeonSMP(graphIndex)) {
				totalSamplesPerCPU[sample.cpuNumber]++;
				if (!adapter.matchingCPU(sample.cpuNumber)){
					continue;
				}
			}

			if ((noThreads || ((ProfiledThread) this.profiledThreads
					.elementAt(sample.threadIndex)).isEnabled(graphIndex))
					&& (noBinaries || ((ProfiledBinary) this.profiledBinaries
							.elementAt(sample.binaryIndex)).isEnabled(graphIndex))) {
				ProfiledFunction pFunction = (ProfiledFunction) this.profiledFunctions
						.elementAt(sample.functionIndex);
				String functionName = pFunction.getNameString();

				lowFunction = adapter.getTotalSampleCount(pFunction) < functionThreshold;

				if (!foundFunctions.containsKey(functionName)) {
					pFunction.setEnabled(graphIndex, true);
					foundFunctions.put(functionName, functionName);
					if (lowFunction) {
						adapter.addItem(thresholdFunction, graphIndex, pFunction, 1);
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

		// since we are not converting float % load to string % load inside the
		// table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else {
			int total = isSingleCPUModeonSMP(graphIndex) ? adapter.getValueForCPU(totalSamplesPerCPU)
					: (this.endSampleIndex - this.startSampleIndex);
			percentPerSample = 100.0 / ((double) total);
		}

		for (int i = 0; i < graphFunctions.size(); i++) {
			ProfiledFunction pFunction = (ProfiledFunction) graphFunctions
					.elementAt(i);
			pFunction.setLoadAndString(graphIndex, (float) (pFunction.getSampleCount(graphIndex) * percentPerSample));
		}
	}
	/**
	 * Based on a graph's set of enabled threads and binaries, produce a set of
	 * functions, and disable all other functions for that graph
	 * 
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param functions The collection to clear and update. Collection of functions with their sample count within selected
	 *         area and filtered by selected threads and binaries in the given
	 *         graph
	 */
	public void setThreadBinaryFunction(int graphIndex,
			GppModelAdapter adapter, Vector<ProfiledGeneric> functions) {
		internalSetFunction(graphIndex, adapter, functions, false, false);		
	}

	/**
	 * Based on a graph's set of enabled threads and binaries, produce a set of
	 * functions, and disable all other functions for that graph
	 * 
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param functions The collection to clear and update. Collection of functions with their sample count within selected
	 *         area and filtered by selected threads and binaries in the given
	 *         graph
	 */
	public void setBinaryThreadFunction(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> functions) {
		internalSetFunction(graphIndex, adapter, functions, false, false);
	}

	/**
	 * Based on a graph's set of enabled threads, produce a set of functions,
	 * and disable all other functions for that graph

	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param functions The collection to clear and update. Collection of functions with their sample count within selected
	 *         area and filtered by selected threads in the given graph
	 */
	public void setThreadFunction(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> functions) {
		internalSetFunction(graphIndex, adapter, functions, false, true);
	}

	/**
	 * internal implementation for
	 * {@link #setFunctionBinary(int, GppModelAdapter)},
	 * {@link #setFunctionThreadBinary(int, GppModelAdapter)},
	 * {@link #setThreadBinary(int, GppModelAdapter)},
	 * {@link #setThreadFunctionBinary(int, GppModelAdapter)}
	 * 
	 * @param graphIndex
	 *            index of the graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphBinaries the collection to update. Collection of ProfiledGeneric binaries filtered by selected
	 *         threads and functions on the given graph in the selected time
	 *         frame
	 * @param noThreads
	 *            ignore threads filtering
	 * @param noFunctions
	 *            ignore functions filtering
	 */
	private Vector<ProfiledGeneric> internalSetBinary(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphBinaries, boolean noThreads, boolean noFunctions) {
		graphBinaries.clear();
		Hashtable<String, String> foundBinaries = new Hashtable<String, String>();

		// disable all binaries for the given graph
		for (int i = 0; i < this.profiledBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries
					.elementAt(i);
			pBinary.setEnabled(graphIndex, false);
		}

		// set up in case we find binaries below the threshold
		boolean lowBinary;
		ProfiledThreshold thresholdBinary = adapter.getThresholdItem(this, graphIndex, PIPageEditor.BINARIES_PAGE);
		adapter.init(thresholdBinary, graphIndex);

		// for each binary in the selected sample range, if its thread and
		// function are enabled,
		// add the binary
		int binaryThreshold = (Integer) NpiInstanceRepository
				.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$

		int[] totalSamplesPerCPU = null;
		if (isSingleCPUModeonSMP(graphIndex)) {
			totalSamplesPerCPU = new int[cpuCount];
		}
		
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];

			if (isSingleCPUModeonSMP(graphIndex)) {
				totalSamplesPerCPU[sample.cpuNumber]++;
				if (!adapter.matchingCPU(sample.cpuNumber)){
					continue;
				}
			}

			if ((noThreads || (this.profiledThreads
					.elementAt(sample.threadIndex)).isEnabled(graphIndex))
					&& (noFunctions || (this.profiledFunctions
							.elementAt(sample.functionIndex)).isEnabled(graphIndex))) {
				ProfiledBinary pBinary = (ProfiledBinary) this.profiledBinaries
						.elementAt(sample.binaryIndex);
				String binaryName = pBinary.getNameString();

				lowBinary = adapter.getTotalSampleCount(pBinary) < binaryThreshold;

				if (!foundBinaries.containsKey(binaryName)) {
					pBinary.setEnabled(graphIndex, true);
					foundBinaries.put(binaryName, binaryName);
					if (lowBinary) {
						adapter.addItem(thresholdBinary, graphIndex, pBinary, 1);
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

		// since we are not converting float % load to string % load inside the
		// table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else{
			int total = isSingleCPUModeonSMP(graphIndex) ? adapter.getValueForCPU(totalSamplesPerCPU)
					: (this.endSampleIndex - this.startSampleIndex);
			percentPerSample = 100.0 / ((double) total);
			
		}

		for (int i = 0; i < graphBinaries.size(); i++) {
			ProfiledBinary pBinary = (ProfiledBinary) graphBinaries
					.elementAt(i);
			pBinary.setLoadAndString(graphIndex, (float) (pBinary.getSampleCount(graphIndex) * percentPerSample));
		}

		return graphBinaries;
	
	}
	/**
	 * Based on a graph's set of enabled threads and functions, produce a set of
	 * binaries, and disable all other binaries for that graph
	 * 
	 * @param graphIndex
	 *            index of the graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphBinaries Collection of ProfiledGeneric binaries to be updated. The collection is filtered by selected
	 *         threads and functions on the given graph in the selected
	 *         time frame
	 */
	public void setThreadFunctionBinary(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphBinaries) {
		internalSetBinary(graphIndex, adapter, graphBinaries, false, false);		
	}

	/**
	 * Based on a graph's set of enabled threads and functions, produce a set of
	 * binaries, and disable all other binaries for that graph
	 * 
	 * @param graphIndex
	 *            index of the graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphThreads Collection of ProfiledGeneric threads filtered by selected
	 *         threads on the given graph in the selected
	 *         time frame. This collection will be updated.
	 */
	public void setFunctionThreadBinary(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphThreads) {
		internalSetBinary(graphIndex, adapter, graphThreads, false, false);
	}

	/**
	 * Based on a graph's set of enabled binaries, produce a set of threads, and
	 * disable all other threads for that graph
	 * 
	 * @param graphIndex
	 *            index of the graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphTreads Collection of ProfiledGeneric threads filtered by selected
	 *         threads on the given graph in the selected
	 *         time frame. This collection will be updated.
	 */
	public void setBinaryThread(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphThreads) {
		internalSetThread(graphIndex, adapter, graphThreads, true, false);
	}

	/**
	 * Based on a graph's set of enabled functions, produce a set of threads,
	 * and disable all other threads for that graph
	 * 
	 * @param graphIndex
	 *            index of the graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param graphThreads list of previous threads, will be updated
	 * @return Collection of ProfiledGeneric threads filtered by selected
	 *         functions on the given graph in the selected time frame
	 */
	public void setFunctionThread(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphThreads) {
		internalSetThread(graphIndex, adapter, graphThreads, false, true);
	}

	/**
	 * Internal implementation for
	 * {@link #setBinaryFunctionThread(int, GppModelAdapter)},
	 * {@link #setFunctionBinaryThread(int, GppModelAdapter)},
	 * {@link #setBinaryThread(int)},
	 * {@link #setFunctionThread(int, GppModelAdapter)}
	 * 
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param noFunctions
	 *            if true omit functions, use for binaries -> threads
	 * @param noBinaries
	 *            if true omit binaries, use for functions -> threads
	 * @param graphThreads Collection of threads to update with their sample count within selected
	 *         area and filtered by selected functions and binaries (or
	 *         functions only or binaries only) in the given graph
	 */
	private void internalSetThread(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> graphThreads, boolean noFunctions, boolean noBinaries){
		graphThreads.clear();
		Hashtable<String, String> foundThreads = new Hashtable<String, String>();

		// disable all threads for the given graph
		for (int i = 0; i < this.profiledThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) this.profiledThreads
					.elementAt(i);
			pThread.setEnabled(graphIndex, false);
		}

		// set up in case we find threads below the threshold
		boolean lowThread;
		ProfiledThreshold thresholdThread = adapter.getThresholdItem(this, graphIndex, PIPageEditor.THREADS_PAGE);
		adapter.init(thresholdThread, graphIndex);

		// for each thread in the selected sample range, if its binary and
		// function are enabled,
		// add the thread
		int threadThreshold = (Integer) NpiInstanceRepository
				.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
		int[] totalSamplesPerCPU = null;
		if (isSingleCPUModeonSMP(graphIndex)) {
			totalSamplesPerCPU = new int[cpuCount];
		}
		for (int i = this.startSampleIndex; i < this.endSampleIndex; i++) {
			GppSample sample = this.sortedSamples[i];
			if (isSingleCPUModeonSMP(graphIndex)) {
				totalSamplesPerCPU[sample.cpuNumber]++;
				if (!adapter.matchingCPU(sample.cpuNumber)){
					continue;
				}
			}

			if ((noBinaries || ((ProfiledBinary) this.profiledBinaries
					.elementAt(sample.binaryIndex)).isEnabled(graphIndex))
					&& (noFunctions || ((ProfiledFunction) this.profiledFunctions
							.elementAt(sample.functionIndex)).isEnabled(graphIndex))) {
				ProfiledThread pThread = (ProfiledThread) this.profiledThreads
						.elementAt(sample.threadIndex);
				String threadName = pThread.getNameString();

				lowThread = adapter.getTotalSampleCount(pThread) < threadThreshold;

				if (!foundThreads.containsKey(threadName)) {
					pThread.setEnabled(graphIndex, true);
					foundThreads.put(threadName, threadName);
					if (lowThread) {
						adapter.addItem(thresholdThread, graphIndex, pThread, 1);
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

		// since we are not converting float % load to string % load inside the
		// table viewers, do it here
		double percentPerSample;
		if (startSampleIndex == endSampleIndex)
			percentPerSample = 0.0;
		else {
			int total = isSingleCPUModeonSMP(graphIndex) ? adapter.getValueForCPU(totalSamplesPerCPU)
					: (this.endSampleIndex - this.startSampleIndex);
			percentPerSample = 100.0 / ((double) total);
		}

		for (int i = 0; i < graphThreads.size(); i++) {
			ProfiledThread pThread = (ProfiledThread) graphThreads.elementAt(i);
			pThread.setLoadAndString(graphIndex, (float) (pThread.getSampleCount(graphIndex) * percentPerSample));
		}
	}
	/**
	 * Based on a graph's set of enabled binaries and functions, produce a set
	 * of threads, and disable all other threads for that graph
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param threads Collection of threads to update with their sample count within selected
	 *         area and filtered by selected functions and binaries in the given
	 *         graph
	 */
	public void setBinaryFunctionThread(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> threads) {
		internalSetThread(graphIndex, adapter, threads, false, false);
	}

	/**
	 * Based on a graph's set of enabled binaries and functions, produce a set
	 * of threads, and disable all other threads for that graph
	 * @param graphIndex
	 *            index of graph to use
	 * @param adapter
	 *            a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param threads Collection of threads to update with their sample count within selected
	 *         area and filtered by selected functions and binaries in the given
	 *         graph
	 */
	public void setFunctionBinaryThread(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> threads) {
		internalSetThread(graphIndex, adapter, threads, false, false);
	}

	/**
	 * Based on a graph's set of enabled functions, produce a set of binaries,
	 * and disable all other binaries for that graph
	 * @param graphIndex
	 *            index of the graph to operate on
	 * @param adapter a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param binaries Collection of binaries to update with their sample count within selected
	 *         area and filtered by selected functions in the given
	 *         graph
	 */
	public void setFunctionBinary(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> binaries) {
		internalSetBinary(graphIndex, adapter, binaries,  true, false);
	}

	/**
	 * Based on a graph's set of enabled binaries, produce a set of functions,
	 * and disable all other functions for that graph
	 * @param graphIndex
	 *            index of the graph to operate on
	 * @param adapter a GppModelAdapter encapsulating SMP or non-SMP specific model
	 *            usage
	 * @param functions Collection of functions with their sample count within selected
	 *         area and filtered by selected binaries in the given
	 *         graph
	 */
	public void setBinaryFunction(int graphIndex, GppModelAdapter adapter, Vector<ProfiledGeneric> functions) {
		internalSetFunction(graphIndex, adapter, functions, true, false);
	}

	/**
	 * returns the first sample index within the time range
	 * 
	 * @return
	 */
	public int getStartSampleIndex() {
		return this.startSampleIndex;
	}

	/**
	 * Returns the first sample index just past the selected time range
	 * 
	 * @return
	 */
	public int getEndSampleIndex() {
		return this.endSampleIndex;
	}

	/**
	 * Getter for function color ThreadColorPalette. Will create the instance
	 * if it doesn't already exist
	 * 
	 * @return threadColorPalette
	 */
	public ThreadColorPalette getThreadColorPalette() {
		if (this.threadColorPalette == null) {
			this.threadColorPalette = new ThreadColorPalette();
		}
		return this.threadColorPalette;
	}

	/**
	 * Getter for function color BinaryColorPalette. Will create the instance
	 * if it doesn't already exist
	 * 
	 * @return binaryColorPalette
	 */
	public BinaryColorPalette getBinaryColorPalette() {
		if (this.binaryColorPalette == null) {
			this.binaryColorPalette = new BinaryColorPalette();
		}
		return this.binaryColorPalette;
	}

	/**
	 * Getter for FunctionColorPalette. Will create the instance
	 * if it doesn't already exist
	 * 
	 * @return functionColorPalette
	 */
	public FunctionColorPalette getFunctionColorPalette() {
		if (this.functionColorPalette == null) {
			this.functionColorPalette = new FunctionColorPalette();
		}
		return this.functionColorPalette;
	}

	@Override
	public void finalizeTrace() {
		samples.trimToSize();
	}

	/**
	 * Builds up the model from trace. The following structures will be filled
	 * with data: <br>
	 * profiledThreads <br>
	 * sortedProfiledThreads <br>
	 * profiledBinaries <br>
	 * sortedProfiledBinaries <br>
	 * profiledFunctions <br>
	 * sortedProfiledFunctions All Profiled objects in these structures will
	 * have sample counts and sample percentages according to the given trace.
	 * @param graphCount the number of graphs for which to prepare the model
	 */
	public void refreshDataFromTrace(int graphCount) {
		// Loop through samples and
		// - create ProfiledThread, ProfiledBinary, ProfiledFunction and save in
		// this.profiledThreads, etc
		// - increase sample counts appropriate in ProfiledThread,
		// ProfiledBinary, ProfiledFunction
		// After all samples have been processed
		// - update percentage counts per bucket in ProfiledThread,
		// ProfiledBinary, ProfiledFunction
		// - sort ProfiledThread, ProfiledBinary, ProfiledFunction and save in
		// this.sortedProfiledThreads, etc.
		// - create/clear this.threadSamples etc, will contain selected areas of
		// the graph later on

		// each bucket has GppTraceGraph.GRANULARITY_VALUE number of samples
		// (the last bucket might contain a few more)
		// except in the SMP case, where we have
		// <cpuNumber>*GppTraceGraph.GRANULARITY_VALUE number of samples per
		// bucket

		this.setGraphCount(graphCount);
		int bucketDuration = getBucketDuration();
		// assume samples are evenly distributed across buckets
		int granularityValue = samples.size() / cpuCount > GppTraceGraph.GRANULARITY_VALUE ? GppTraceGraph.GRANULARITY_VALUE
				: samples.size() / cpuCount;
		int numberOfBuckets = GppTraceUtil.calculateNumberOfBuckets(
				getLastSampleTime(), granularityValue);

		HashMap<String, ProfiledThread> profiledThreadsMap = new HashMap<String, ProfiledThread>();
		HashMap<String, ProfiledBinary> profiledBinariesMap = new HashMap<String, ProfiledBinary>();
		HashMap<String, ProfiledFunction> profiledFunctionsMap = new HashMap<String, ProfiledFunction>();

		createProfiledFromSamples(bucketDuration, numberOfBuckets,
				profiledThreadsMap, profiledBinariesMap, profiledFunctionsMap);

		// for sorting by increasing index
		Comparator<ProfiledGeneric> indexComparator = new Comparator<ProfiledGeneric>() {
			public int compare(ProfiledGeneric o1, ProfiledGeneric o2)
					throws ClassCastException {
				return o1.getIndex() - o2.getIndex();
			}
		};
		// sort by increasing sample load
		Comparator<ProfiledGeneric> loadComparator = new Comparator<ProfiledGeneric>() {
			public int compare(ProfiledGeneric o1, ProfiledGeneric o2)
					throws ClassCastException {
				return o1.getTotalSampleCount() - o2.getTotalSampleCount();
			}
		};

		// now create collections of the profiled elements in the trace

		profiledThreads = new Vector<ProfiledThread>();
		profiledThreads.addAll(profiledThreadsMap.values());
		Collections.sort(profiledThreads, indexComparator);

		profiledBinaries = new Vector<ProfiledBinary>();
		profiledBinaries.addAll(profiledBinariesMap.values());
		Collections.sort(profiledBinaries, indexComparator);

		profiledFunctions = new Vector<ProfiledFunction>();
		profiledFunctions.addAll(profiledFunctionsMap.values());
		Collections.sort(profiledFunctions, indexComparator);

		sortedProfiledThreads = new Vector<ProfiledGeneric>();
		sortedProfiledThreads.addAll(profiledThreadsMap.values());
		Collections.sort(sortedProfiledThreads, loadComparator);

		sortedProfiledBinaries = new Vector<ProfiledGeneric>();
		sortedProfiledBinaries.addAll(profiledBinariesMap.values());
		Collections.sort(sortedProfiledBinaries, loadComparator);

		sortedProfiledFunctions = new Vector<ProfiledGeneric>();
		sortedProfiledFunctions.addAll(profiledFunctionsMap.values());
		Collections.sort(sortedProfiledFunctions, loadComparator);

	}

	/**
	 * Returns the duration for a bucket based on the sampling granularity
	 * and the sampling interval
	 * @return the bucket duration in milliseconds
	 */
	public int getBucketDuration() {
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		return getGranularity() * samplingInterval;
	}

	/**
	 * Returns the granularity value, i.e. the number of samples per bucket
	 * @return granularity
	 */
	public int getGranularity(){
		return  (samples.size() / cpuCount) > GppTraceGraph.GRANULARITY_VALUE ? GppTraceGraph.GRANULARITY_VALUE
				: (samples.size() / cpuCount);
		
	}
	/**
	 * Loops through the given samples and: <br>
	 * - Creates ProfiledThread, ProfiledBinary, ProfiledFunction as needed
	 * which are saved in the passed maps <br>
	 * - Increases sample counts in appropriate ProfiledThread, ProfiledBinary,
	 * ProfiledFunction <br>
	 * - Updates each sample with the index of the ProfiledThread,
	 * ProfiledBinary, ProfiledFunction
	 * 
	 * @param bucketDuration
	 *            length of a bucket in milliseconds
	 * @param numberOfBuckets
	 *            Number of buckets to use
	 * @param profiledThreadsMap
	 *            Map to store the ProfiledThreads keyed by thread name
	 * @param profiledBinariesMap
	 *            Map to store the ProfiledBinaries keyed by binary name
	 * @param profiledFunctionsMap
	 *            Map to store the ProfiledFunctions keyed by function name
	 */
	private void createProfiledFromSamples(final int bucketDuration,
			int numberOfBuckets,
			HashMap<String, ProfiledThread> profiledThreadsMap,
			HashMap<String, ProfiledBinary> profiledBinariesMap,
			HashMap<String, ProfiledFunction> profiledFunctionsMap) {

		int threadCount = 0; // used for thread index
		int binaryCount = 0; // used for binary index
		int functionCount = 0; // used for function index
		char threadSymbol = 'A'; // each thread has a unique character as
									// symbol; the character value increases as
									// threads are created
		int[] bucketTotalArr = new int[numberOfBuckets];
		int[][] bucketTotalArrSMP = null;
		if (isSMP) {
			bucketTotalArrSMP = new int[cpuCount][numberOfBuckets];
		}

		for (GenericSample genericSample : samples) {
			GppSample sample = (GppSample) genericSample;
			String threadName = sample.thread.process.name
					+ "::" + sample.thread.threadName + "_" + sample.thread.threadId; //$NON-NLS-1$ //$NON-NLS-2$
			String binaryName = GppTraceUtil.getBinaryName(sample);
			String functionName = GppTraceUtil.getFunctionName(sample);
			int bucket = (int) sample.sampleSynchTime / bucketDuration;
			bucketTotalArr[bucket]++;
			if (isSMP) {
				bucketTotalArrSMP[sample.cpuNumber][bucket]++;
			}

			// process thread information
			ProfiledThread pThread = profiledThreadsMap.get(threadName);
			if (pThread == null) {
				pThread = new ProfiledThread(cpuCount, graphCount);

				pThread.setIndex(threadCount++);
				pThread.setNameValues(threadSymbol++, threadName);
				pThread.setColor(getThreadColorPalette().getColor(threadName));

				pThread.setThreadId(sample.thread.threadId.intValue());

				pThread.createBuckets(numberOfBuckets);
				pThread.initialiseBuckets(bucketDuration);
				profiledThreadsMap.put(threadName, pThread);
			}
			pThread.increaseSampleCount(bucket, (int) sample.sampleSynchTime,
					sample.cpuNumber);
			sample.threadIndex = pThread.getIndex();

			// process binary information
			ProfiledBinary pBinary = profiledBinariesMap.get(binaryName);
			if (pBinary == null) {
				pBinary = new ProfiledBinary(cpuCount, graphCount);

				pBinary.setIndex(binaryCount++);
				pBinary.setNameString(binaryName);
				pBinary.setColor(getBinaryColorPalette().getColor(binaryName));

				pBinary.createBuckets(numberOfBuckets);
				pBinary.initialiseBuckets(bucketDuration);
				profiledBinariesMap.put(binaryName, pBinary);
			}
			pBinary.increaseSampleCount(bucket, (int) sample.sampleSynchTime,
					sample.cpuNumber);
			sample.binaryIndex = pBinary.getIndex();

			// process function information
			ProfiledFunction pFunction = profiledFunctionsMap.get(functionName);
			if (pFunction == null) {
				pFunction = new ProfiledFunction(cpuCount, graphCount);

				pFunction.setIndex(functionCount++);
				pFunction.setNameString(functionName);
				pFunction.setFunctionAddress(GppTraceUtil
						.getFunctionAddress(sample));
				pFunction.setFunctionBinaryName(binaryName);
				pFunction.setColor(getFunctionColorPalette().getColor(
						functionName));

				pFunction.createBuckets(numberOfBuckets);
				pFunction.initialiseBuckets(bucketDuration);
				profiledFunctionsMap.put(functionName, pFunction);
			}
			pFunction.increaseSampleCount(bucket, (int) sample.sampleSynchTime,
					sample.cpuNumber);
			sample.functionIndex = pFunction.getIndex();
		}

		// update bucket percentages now
		for (ProfiledThread thread : profiledThreadsMap.values()) {
			thread
					.calculateBucketPercentages(bucketTotalArr,
							bucketTotalArrSMP);
		}
		for (ProfiledBinary binary : profiledBinariesMap.values()) {
			binary
					.calculateBucketPercentages(bucketTotalArr,
							bucketTotalArrSMP);
		}
		for (ProfiledFunction function : profiledFunctionsMap.values()) {
			function.calculateBucketPercentages(bucketTotalArr,
					bucketTotalArrSMP);
		}
	}

	/**
	 * Sets the CPU count
	 * 
	 * @param cpuCount
	 *            the number of CPUs encountered in the data
	 */
	public void setCPUCount(int cpuCount) {
		this.cpuCount = cpuCount < 1 ? 1 : cpuCount;
		this.isSMP = cpuCount > 1;
	}

	/**
	 * Returns the number of CPUs encountered. If the number is greater than 1,
	 * it is SMP data
	 * 
	 * @return number of CPUs
	 */
	public int getCPUCount() {
		return cpuCount;
	}

	/**
	 * Sets additional data when trace is loaded from .bup file
	 * 
	 * @param data
	 *            The data to set
	 */
	public void setAdditionalData(Vector<Object> data) {
		Vector<Object> tmpData = (Vector<Object>) data;
		Hashtable<Integer, java.awt.Color> threadColors = (Hashtable<Integer, java.awt.Color>) tmpData
				.elementAt(0);
		Hashtable<String, java.awt.Color> binaryColors = (Hashtable<String, java.awt.Color>) tmpData
				.elementAt(1);
		Hashtable<String, java.awt.Color> functionColors = (Hashtable<String, java.awt.Color>) tmpData
				.elementAt(2);

		boolean changed;
		Enumeration<ProfiledGeneric> e;

		changed = false;
		e = getSortedThreadsElements();
		while (e.hasMoreElements()) {
			ProfiledThread pt = (ProfiledThread) e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = threadColors.get(pt.getThreadId());
			if (tmpAWTColor != null) {
				Color color = ColorPalette.getColor(new RGB(tmpAWTColor
						.getRed(), tmpAWTColor.getGreen(), tmpAWTColor
						.getBlue()));
				if (color != null) {
					pt.setColor(color);
					changed = true;
				}
			}
		}

		final int uid = NpiInstanceRepository.getInstance().activeUid();

		if (changed) {
			// need to provide new colors for the thread load table
			getGppGraph(PIPageEditor.THREADS_PAGE, uid)
					.refreshColoursFromTrace();
		}

		changed = false;
		e = getSortedBinariesElements();
		while (e.hasMoreElements()) {
			ProfiledBinary pb = (ProfiledBinary) e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = binaryColors.get(pb.getNameString());
			if (tmpAWTColor != null) {
				Color color = ColorPalette.getColor(new RGB(tmpAWTColor
						.getRed(), tmpAWTColor.getGreen(), tmpAWTColor
						.getBlue()));
				if (color != null) {
					pb.setColor(color);
					changed = true;
				}
			}
		}

		if (changed) {
			// need to provide new colors for the binary load table
			getGppGraph(PIPageEditor.BINARIES_PAGE, uid)
					.refreshColoursFromTrace();
		}

		changed = false;
		e = getSortedFunctionsElements();
		while (e.hasMoreElements()) {
			ProfiledFunction pb = (ProfiledFunction) e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = functionColors.get(pb.getNameString());
			if (tmpAWTColor != null) {
				Color color = ColorPalette.getColor(new RGB(tmpAWTColor
						.getRed(), tmpAWTColor.getGreen(), tmpAWTColor
						.getBlue()));
				if (color != null) {
					pb.setColor(color);
					changed = true;
				}
			}
		}

		if (changed) {
			// need to provide new colors for the binary load table
			getGppGraph(PIPageEditor.FUNCTIONS_PAGE, uid)
					.refreshColoursFromTrace();
		}

	}
	
	/**
	 * Returns true if this is a single-CPU graph on an SMP trace.
	 * @param graphIndex the index of the graph to check
	 * @return true, if single-CPU graph on an SMP trace, false otherwise
	 */
	private boolean isSingleCPUModeonSMP(int graphIndex){
		return isSMP && graphIndex > PIPageEditor.FUNCTIONS_PAGE;
	}

	/**
	 * Sets the number of graphs to be used. This is necessary because all ProfiledGenerics
	 * hold values per graph. 
	 * 
	 * <p>Value is only set when called for the first time. 
	 * @param graphCount the graphCount to set
	 */
	public void setGraphCount(int graphCount) {
		if (this.graphCount == -1){
			this.graphCount = graphCount;
		}
	}
	
	/**
	 * Returns the number of graphs for this trace. This includes the 3 basic
	 * graphs as well as one extra graph per CPU in the SMP case.
	 * 
	 * @return
	 */
	public int getGraphCount(){
		if (graphCount == -1){
			throw new IllegalStateException();
		}
		return this.graphCount;
	}

	/**
	 * Returns the TabFolder for the legend views for this page.
	 * 
	 * TODO: Ideally, this should be in a GUI-related class, but right now
	 * we don't have any class that has visibility of all graphs
	 * 
	 * 
	 * @param pageIndex
	 *            Index of the page to use (PIPageEditor.THREADS_PAGE,
	 *            PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE)
	 * @param parent
	 *            The parent composite to put the TabFolder in
	 * @return the TabFolder
	 */
	private TabFolderWrapper getTabFolder(int pageIndex, Composite parent) {
		if (pageIndex < PIPageEditor.THREADS_PAGE
				|| pageIndex > PIPageEditor.FUNCTIONS_PAGE
				|| !needTabFolder(pageIndex)) {
			throw new IllegalArgumentException();
		}
		
		if (legendTabFolders == null){
			legendTabFolders = new TabFolderWrapper[3];
		}
		if (legendTabFolders[pageIndex] == null){
			legendTabFolders[pageIndex] = new TabFolderWrapper(new TabFolder(parent, SWT.NONE));
			
		}
		return legendTabFolders[pageIndex];
	}
	
	/**
	 * Create and return the main Composite for the legend view. This methods manages whether to
	 * create the legend on a simple Composite (for single graphs on one page) or to put
	 * the legend composite on a tabbed legend (for multiple CPU graphs on one pages)
	 * @param pageIndex page index 
	 * @param graphIndex graph index
	 * @param parent the parent composite for the legend
	 * @param title the title to display - typically the title of the graph
	 * @return the newly created legend Composite
	 */
	public Composite createLegendComposite(int pageIndex, int graphIndex, Composite parent, String title){
		Composite legendComposite = null;
		
		if (needTabFolder(pageIndex)){
			
			TabFolderWrapper legendTabFolder = getTabFolder(pageIndex, parent);
			legendComposite = legendTabFolder.createTabItem(graphIndex);
		} else {

			// create a wrapper composite so we can add a title in addition to the table viewers
			//
			Composite legendWrapperComposite = new Composite(parent, SWT.NONE);		
			legendWrapperComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).create());

			Label label = new Label(legendWrapperComposite, SWT.CENTER);
			label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setText(title);
			label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).create());
			
			legendComposite = new Composite(legendWrapperComposite, SWT.NONE);	
			legendComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		}
		return legendComposite;
	}

	/**
	 * Returns true if multiple CPU load graphs are on the given page
	 * @param pageIndex one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 * @return true for multiple CPU load graphs on page, false otherwise
	 */
	public boolean needTabFolder(int pageIndex) {
		return graphCount > 3 && pageIndex == PIPageEditor.THREADS_PAGE;
	}

	/**
	 * Changes visibility of the graph legend with the given graph index 
	 * @param graphIndex Index of the graph for which to 
	 * @param visible true to make visible, false to hide
	 * @param legend the main Composite of the legend view
	 */
	public void setLegendVisible(int graphIndex, boolean visible, Composite legend){
		int pageIndex = AddressPlugin.getPageIndex(graphIndex);
		if (needTabFolder(pageIndex)){
			TabFolderWrapper legendTabFolder = legendTabFolders[pageIndex];
			legendTabFolder.setVisible(graphIndex, visible);
		} else {
			legend.getParent().setVisible(visible);
		}
	}
	/**
	 * Sets the graph legend with the given graph index to maximised (or restored) 
	 * @param graphIndex Index of the graph for which to 
	 * @param maximised true to set maximised, false to restore
	 * @param legend the main Composite of the legend view
	 */
	public void setLegendMaximised(int graphIndex, boolean maximised, Composite legend){
		int pageIndex = AddressPlugin.getPageIndex(graphIndex);
		if (needTabFolder(pageIndex)){
			legendTabFolders[pageIndex].setMaximised(graphIndex, maximised);
		} else {
			((SashForm)legend.getParent().getParent()).setMaximizedControl(maximised ? legend.getParent() : null);
		}
	}
	
	/**
	 * Class to wrap TabFolders for legend management
	 * This should really go into a GUI-related class but we haven't got 
	 * one with visibility of all graphs
	 *
	 */
	class TabFolderWrapper{
		
		private TabFolder tabFolder;
		/** indicates which legends are enabled; one per graphIndex */
		private boolean[] legendsVisible;
		/** the Composites for each tabItem; one per graphIndex */
		private Composite[] legendComposites;
		
		public TabFolderWrapper(TabFolder tabFolder) {
			this.tabFolder = tabFolder;
			legendsVisible = new boolean[graphCount];
			legendComposites = new Composite[graphCount];
		}

		public void setVisible(int graphIndex, boolean visible) {
			if (legendsVisible[graphIndex] != visible){
				
				//dispose all tabItems and recreate them so that the correct order is preserved
				//
				legendsVisible[graphIndex] = visible;
				for (TabItem tabItem : tabFolder.getItems()) {
					tabItem.dispose();
				}
				for (int i = 0; i < legendsVisible.length; i++) {
					boolean isVisible = legendsVisible[i];
					if (isVisible){
						TabItem legendTabItem = new TabItem (tabFolder, SWT.NONE);
						legendTabItem.setText(getTraceGraph(i).getShortTitle());
						legendTabItem.setControl(legendComposites[i]);							
					}
				}
			}
		}
		
		public void setMaximised(int graphIndex, boolean maximised){
			if (maximised){
				setVisible(graphIndex, maximised);	//in case it was minimised before			
			}
			((SashForm)tabFolder.getParent()).setMaximizedControl(maximised ? tabFolder : null);
		}

		public Composite createTabItem(int graphIndex) {
			TabItem legendTabItem = new TabItem (tabFolder, SWT.NONE);
			legendTabItem.setText(getTraceGraph(graphIndex).getShortTitle());
			Composite legendComposite = new Composite(tabFolder, SWT.NONE);
			legendTabItem.setControl(legendComposite);	

			legendsVisible[graphIndex] = true;
			legendComposites[graphIndex] = legendComposite;
			return legendComposite;
		}
	}

	
}
