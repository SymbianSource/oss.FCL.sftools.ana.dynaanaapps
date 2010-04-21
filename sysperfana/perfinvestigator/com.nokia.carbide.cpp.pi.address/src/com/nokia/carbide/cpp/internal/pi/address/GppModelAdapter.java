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
package com.nokia.carbide.cpp.internal.pi.address;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.address.GppTraceUtil;
import com.nokia.carbide.cpp.pi.address.IGppTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * This is an adapter for GppTrace model classes which decides whether to use the
 * SMP version of a method or the non-SMP version.
 * 
 */
public class GppModelAdapter {
	
	int cpu;
	boolean singleCPUMode;
	/** type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE */
	private int graphType;
	/** should only be true while testing */
	private boolean testing;

	/**
	 * Constructor 
	 * @param graphType type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 */
	public GppModelAdapter(int graphType) {
		if (graphType != PIPageEditor.THREADS_PAGE && graphType != PIPageEditor.BINARIES_PAGE && graphType != PIPageEditor.FUNCTIONS_PAGE){
			throw new IllegalArgumentException();
		}
		singleCPUMode = false;
		this.graphType = graphType;
	}

	/**
	 * Constructor for a GppModelAdapter working in single CPU mode 
	 * on SMP system
	 * @param cpu the CPU index to use
	 * @param graphType type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 */
	public GppModelAdapter(int cpu, int graphType) {
		singleCPUMode = true;
		this.cpu = cpu; 
		this.graphType = graphType;
	}

	/**
	 * Constructor for sole purpose of unit testing. This will try to avoid the use of GUI widgets.
	 * @param graphType type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 * @param testing true if testing
	 */
	public GppModelAdapter(int graphType, boolean testing) {
		this(graphType);
		this.testing = testing;
	}

	/**
	 * Constructor for sole purpose of unit testing. This will try to avoid the use of GUI widgets.
	 * @param cpu the CPU index to use
	 * @param graphType type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 * @param testing true if testing
	 */
	public GppModelAdapter(int cpu, int graphType, boolean testing) {
		this(cpu, graphType);
		this.testing = testing;
	}
	
	/**
	 * @return the cpu
	 */
	public int getCPU() {
		return cpu;
	}

	/**
	 * Indicates whether this graph is a single-CPU graph in an SMP system
	 * @return the singleCPUMode
	 */
	public boolean isSingleCPUMode() {
		return singleCPUMode;
	}

	/**
	 * Returns type of this graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 * @return the graphType
	 */
	public int getGraphType() {
		return graphType;
	}
	
	/**
	 * Wrapper for {@link ProfiledGeneric.getTotalSampleCount(int)}
	 * @param profiled the ProfiledGeneric to use
	 * @return
	 */
	public int getTotalSampleCount(ProfiledGeneric profiled){
		if (singleCPUMode){
			return profiled.getTotalSampleCountForSMP(cpu);						
		} else {
			return profiled.getTotalSampleCount();			
		}
	}
	
	/**
	 * Wrapper for {@link ProfiledThreshold.addItem(int, ProfiledGeneric, count)}
	 * @param pti the ProfiledThreshold to use
	 * @param graphIndex the current graph index
	 * @param pGeneric the ProfiledGeneric to add
	 * @param count the count by which to increase the total sample count
	 */
	public void addItem(ProfiledThreshold pti, int graphIndex, ProfiledGeneric pGeneric, int count){
		if (singleCPUMode){
			pti.addItemForSMP(cpu, graphIndex, pGeneric, count);
		} else {
			pti.addItem(graphIndex, pGeneric, count);
		}
	}
	
	/**
	 * Wrapper for {@link ProfiledThreshold.init(int)}
	 * @param pti the ProfiledThreshold to use
	 * @param graphIndex the current graph index
	 */
	public void init(ProfiledThreshold pti, int graphIndex){
		if (singleCPUMode){
			pti.initForSMP(cpu, graphIndex);
		} else {
			pti.init(graphIndex);
		}
	}

	/**
	 * Returns one of the values in the given array depending on the current CPU.
	 * Not intended to be used for non-SMP trace, or the all-CPU mode 
	 * @param totalSamplesPerCPU the input array to use
	 * @return the chosen value
	 * @throws IllegalStateException if not used in single-CPU mode on an SMP trace
	 */
	public int getValueForCPU(final int[] totalSamplesPerCPU) {
		if (!singleCPUMode){
			throw new IllegalStateException();
		}
		return totalSamplesPerCPU[cpu];
	}

	/**
	 * Returns the requested threshold item for the current graph 
	 * @param gppTrace GppTrace to use for getting the graph
	 * @param graphIndex index of the current graph
	 * @param thresholdItemType similar to type of graph, must be one of PIPageEditor.THREADS_PAGE for threadThreshold, PIPageEditor.BINARIES_PAGE for binaryThreshold, PIPageEditor.FUNCTIONS_PAGE for functionThreshold
	 * @return the requested ProfiledThreshold
	 */
	public ProfiledThreshold getThresholdItem(GppTrace gppTrace, int graphIndex, int thresholdItemType) {
		int uid = NpiInstanceRepository.getInstance().activeUid();
		if (testing){
			//try getting around to actually having to create a GppTraceGraph as it has too many GUI widgets to be tested as unit test
			
			int granularityValue = gppTrace.samples.size() > 100 ? 100 : gppTrace.samples.size();  
			int totalSampleCount = gppTrace.getSampleAmount();
			int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

			// initialize the threshold items
			int bucketDuration = granularityValue * samplingInterval;
			int numberOfBuckets = GppTraceUtil.calculateNumberOfBuckets(gppTrace.getLastSampleTime(), granularityValue);

			if (thresholdItemType == PIPageEditor.THREADS_PAGE){
				NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread", Integer.valueOf(Double.valueOf(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadThread") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
				ProfiledThreshold thresholdThread = new ProfiledThreshold("dummy[0]::dummy_0", gppTrace.getCPUCount(), gppTrace.getGraphCount()); //$NON-NLS-1$	
				thresholdThread.setColor(gppTrace.getThreadColorPalette().getColor(thresholdThread.getNameString()));
				thresholdThread.createBuckets(numberOfBuckets);
				thresholdThread.initialiseBuckets(bucketDuration);
				return thresholdThread;
			} else if (thresholdItemType == PIPageEditor.BINARIES_PAGE){
				NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountBinary", Integer.valueOf(Double.valueOf(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadBinary") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
				ProfiledThreshold thresholdBinary = new ProfiledThreshold("\\dummy", gppTrace.getCPUCount(), gppTrace.getGraphCount()); //$NON-NLS-1$
				thresholdBinary.setColor(gppTrace.getBinaryColorPalette().getColor(thresholdBinary.getNameString()));
				thresholdBinary.createBuckets(numberOfBuckets);
				thresholdBinary.initialiseBuckets(bucketDuration);
				return thresholdBinary;
			} else if (thresholdItemType == PIPageEditor.FUNCTIONS_PAGE){
				NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountFunction", Integer.valueOf(Double.valueOf(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadFunction") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
				ProfiledThreshold thresholdFunction = new ProfiledThreshold("dummy::dummy()", gppTrace.getCPUCount(), gppTrace.getGraphCount()); //$NON-NLS-1$					
				thresholdFunction.setColor(gppTrace.getFunctionColorPalette().getColor(thresholdFunction.getNameString()));
				thresholdFunction.createBuckets(numberOfBuckets);
				thresholdFunction.initialiseBuckets(bucketDuration);
				return thresholdFunction;
			}
		} else {
			IGppTraceGraph graph = gppTrace.getGppGraph(graphIndex, uid);			
			if (thresholdItemType == PIPageEditor.THREADS_PAGE){
				return graph.getThresholdThread();					
			} else if (thresholdItemType == PIPageEditor.BINARIES_PAGE){
				return graph.getThresholdBinary();					
			} else if (thresholdItemType == PIPageEditor.FUNCTIONS_PAGE){
				return graph.getThresholdFunction();					
			}
		}
		return null;
	}

	/**
	 * In single-CPU mode on an SMP trace, compares the given CPU number with the adapter's CPU number.
	 * In all other cases, returns true
	 * @param cpuNumber the CPU number to compare
	 * @return true, if CPU matches, false otherwise
	 */
	public boolean matchingCPU(int cpuNumber) {
		return singleCPUMode ? cpu == cpuNumber : true;
	}
	
	
	/**
	 * Returns the appropriate activity list for the given ProfiledGeneric.
	 * This may be an SMP-specific list for the current CPU on an SMP trace.
	 * @param pg The ProfiledGeneric to use
	 * @return the sample list, i.e. array of bucket values
	 */
	public float[] getActivityList(ProfiledGeneric pg){
		if (singleCPUMode){
			return pg.getActivityListForSMP(cpu);
		} else {
			return pg.getActivityList();
		}
	}
}
