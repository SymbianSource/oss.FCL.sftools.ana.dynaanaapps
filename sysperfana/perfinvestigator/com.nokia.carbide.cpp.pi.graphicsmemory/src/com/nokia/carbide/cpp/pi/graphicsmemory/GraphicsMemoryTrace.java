/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.graphicsmemory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericThread;
import com.nokia.carbide.cpp.internal.pi.model.TraceWithThreads;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public class GraphicsMemoryTrace extends GenericSampledTrace implements
		TraceWithThreads {
	static final long serialVersionUID = 3606238546911055987L;

	private transient GraphicsMemoryTraceGraph[] graphs;
	private GraphicsMemoryProcess[] processArray;

	// maximums for the entire trace
	transient private int traceMaxPrivate = 0;
	transient private int traceMaxShared = 0;
	transient private int traceMaxTotal = 0;
	transient private int traceTotalMemory = 0;

	transient private Hashtable<String, TreeMap<Long, GraphicsMemorySample>> drawDataByMemProcess = null;
	transient private ArrayList<GraphicsMemorySampleByTime> drawDataByTime;
	transient private HashSet<GraphicsMemoryProcess> noDuplicateMemProcesses;

	transient private long intervalStart = -1;
	transient private long intervalEnd = -1;

	private int version;

	public GraphicsMemoryTrace() {
	}

	public GenericThread[] getThreads() {
		return processArray;
	}

	public void setProcesses(GraphicsMemoryProcess[] processArray) {
		this.processArray = processArray;
	}

	public int getTraceMaxPrivate() {
		return traceMaxPrivate;
	}

	public int getTraceMaxShared() {
		return traceMaxShared;
	}

	public int getTraceMaxTotal() {
		return traceMaxTotal;
	}

	public int getTraceTotalMemory() {
		return traceTotalMemory;
	}

	public void setTraceTotalMemory(int traceTotalMemory) {
		this.traceTotalMemory = traceTotalMemory;
	}

	public void setTraceMaxShared(int traceMaxShared) {
		this.traceMaxShared = traceMaxShared;
	}

	public void setTraceMaxTotal(int traceMaxTotal) {
		this.traceMaxTotal = traceMaxTotal;
	}

	public void setTraceMaxPrivate(int traceMaxPrivate) {
		this.traceMaxPrivate = traceMaxPrivate;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public GenericTraceGraph getTraceGraph(int graphIndex) {
		if (graphs == null) {
			graphs = new GraphicsMemoryTraceGraph[3];
		}

		// note that graphIndex need not match the index sent to
		// GraphicsMemoryTraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				graphs[graphIndex] = new GraphicsMemoryTraceGraph(graphIndex,
						this, uid);
			}
			return graphs[graphIndex];
		}
		return null;
	}

	public void gatherDrawData() {
		if (drawDataByMemProcess != null)
			return; // already initialised
		drawDataByMemProcess = new Hashtable<String, TreeMap<Long, GraphicsMemorySample>>();

		getProcessesFromTrace();

		if (drawDataByTime == null)
			drawDataByTime = new ArrayList<GraphicsMemorySampleByTime>();

		// index each sample by process name and by sample time
		for (Enumeration<GenericSample> e = this.getSamples(); e
				.hasMoreElements();) {
			addSampleToProcess((GraphicsMemorySample) e.nextElement());
		}
	}

	private void addSampleToProcess(GraphicsMemorySample sample) {
		// get sample array from process
		TreeMap<Long, GraphicsMemorySample> samples = drawDataByMemProcess
				.get(sample.process.fullName);

		if (samples != null) {
			// Add initial sample to process
			samples.put(sample.sampleSynchTime, sample);
			return;
		}

		System.out.println("PI ERROR: Process not found"); //$NON-NLS-1$
	}

	private void getProcessesFromTrace() {

		noDuplicateMemProcesses = new HashSet<GraphicsMemoryProcess>(131, 0.75f);

		// the parser creates multiple copies of the same GraphicsMemoryProcess item,
		// so we have to use the full name of the GraphicsMemoryProcess to access sample
		// data

		for (GraphicsMemoryProcess memProcess : processArray) {
			memProcess.fullName = memProcess.processName;
			memProcess.enabled = new boolean[3];
			memProcess.enabled[0] = true;
			memProcess.enabled[1] = true;
			memProcess.enabled[2] = true;

			memProcess.maxMemoryItem = new MaxGraphicsMemoryItem();
			memProcess.maxMemoryItem.maxPrivate = 0;
			memProcess.maxMemoryItem.maxShared = 0;
			memProcess.maxMemoryItem.maxTotal = 0;

			if (drawDataByMemProcess.get(memProcess.fullName) == null) {
				drawDataByMemProcess.put(memProcess.fullName,
						new TreeMap<Long, GraphicsMemorySample>());
				noDuplicateMemProcesses.add(memProcess);
			}
		}
	}

	public TreeMap<Long, GraphicsMemorySample> getDrawDataByMemProcess(
			GraphicsMemoryProcess id) {
		return this.drawDataByMemProcess.get(id.fullName);
	}

	public Hashtable<String, TreeMap<Long, GraphicsMemorySample>> getDrawDataByMemProcess() {
		return this.drawDataByMemProcess;
	}

	public ArrayList<GraphicsMemorySampleByTime> getDrawDataByTime() {

		if (drawDataByTime.isEmpty()) {
			TreeMap<Long, GraphicsMemorySample> events = drawDataByMemProcess
					.get(GraphicsMemoryTraceParser.SAMPLE_TOTAL_MEMORY_PROCESS_NAME); //$NON-NLS-1$

			Iterator<GraphicsMemorySample> iterator = events.values()
					.iterator();
			while (iterator.hasNext()) {
				GraphicsMemorySample memSample = iterator.next();
				drawDataByTime.add(new GraphicsMemorySampleByTime(
						memSample.sampleSynchTime, memSample.sharedSize,
						memSample.privateSize));
			}
		}
		return this.drawDataByTime;
	}

	public HashSet<GraphicsMemoryProcess> getNoDuplicateMemProcesses() {
		return this.noDuplicateMemProcesses;
	}

	public ArrayList<GraphicsMemorySample> getMemSampleDataByTime(long time) {
		if ((this.drawDataByTime == null)
				|| (time < this.drawDataByTime.get(0).getTime()))
			return null;

		int i = 0;
		for (; i < this.drawDataByTime.size(); i++) {
			GraphicsMemorySampleByTime sample = this.drawDataByTime.get(i);
			if (sample.getTime() > time)
				break;
		}

		return this.drawDataByTime.get(i - 1).getSamples();
	}

	public MaxGraphicsMemoryItem getSystemUseByInterval(long startTime,
			long endTime) {
		MaxGraphicsMemoryItem item = new MaxGraphicsMemoryItem();

		if ((this.drawDataByTime == null)
				|| ((startTime == intervalStart) && (endTime == intervalEnd)))
			return item;

		if (this.getVersion() == 100) {
			TreeMap<Long, GraphicsMemorySample> events = drawDataByMemProcess
					.get(GraphicsMemoryTraceParser.SAMPLE_TOTAL_MEMORY_PROCESS_NAME); //$NON-NLS-1$
			
			GraphicsMemorySample floorSample = (GraphicsMemorySample) GraphicsMemoryTrace
				.getFloorEntryFromMap(startTime, events);
			
			if(floorSample != null){
				item.maxPrivate = floorSample.privateSize;
				item.maxTotal = floorSample.sharedSize;
			}
			
			SortedMap<Long, GraphicsMemorySample>  sortedMap = events.subMap(startTime, endTime);
			Iterator<GraphicsMemorySample> values = sortedMap.values().iterator();

			while (values.hasNext()) {
				GraphicsMemorySample memSample = values.next();
				// store system used memory as privates and total memory as total
				if (item.maxPrivate < memSample.privateSize)
					item.maxPrivate = memSample.privateSize;
				item.maxTotal = memSample.sharedSize;
				if (memSample.sampleSynchTime > endTime)
					break;

			}

		}
		return item;
	}

	public void setMaxMemDataByInterval(long startTime, long endTime) {

		// Set all values to zero
		for (int i = 0; i < processArray.length; i++) {
			MaxGraphicsMemoryItem item = processArray[i].maxMemoryItem;
			item.maxPrivate = 0;
			item.maxShared = 0;
			item.maxTotal = 0;
		}

		// if no data is found between start and end time or start and end
		// times are same with previous.
		if ((this.drawDataByTime == null)
				|| ((startTime == intervalStart) && (endTime == intervalEnd)))
			return;

		if (this.version == 100) {

			for (Enumeration<TreeMap<Long, GraphicsMemorySample>> e = drawDataByMemProcess
					.elements(); e.hasMoreElements();) {
				TreeMap<Long, GraphicsMemorySample> memSamples = (TreeMap<Long, GraphicsMemorySample>) e
						.nextElement();

				if (memSamples.size() == 0) {
					continue;
				}

				GraphicsMemorySample firstSample = (GraphicsMemorySample) memSamples
						.get(memSamples.firstKey());

				GraphicsMemoryProcess process = firstSample.process;

				MaxGraphicsMemoryItem maxMemoryItem = process.maxMemoryItem;

				SortedMap<Long, GraphicsMemorySample> subMap = memSamples
						.subMap(startTime, endTime);

				ArrayList<GraphicsMemorySample> samples = new ArrayList<GraphicsMemorySample>(
						subMap.values());

				GraphicsMemorySample floorSample = (GraphicsMemorySample) GraphicsMemoryTrace
						.getFloorEntryFromMap(startTime, memSamples);

				if (floorSample != null) {

					maxMemoryItem.maxPrivate = floorSample.privateSize;
					maxMemoryItem.maxShared = floorSample.sharedSize;
					maxMemoryItem.maxTotal = floorSample.privateSize
							+ floorSample.sharedSize;

				}

				for (GraphicsMemorySample item : samples) {
					if (maxMemoryItem.maxPrivate < item.privateSize) {
						maxMemoryItem.maxPrivate = item.privateSize;
					}
					if (maxMemoryItem.maxShared < item.sharedSize) {
						maxMemoryItem.maxShared = item.sharedSize;
					}
					if (maxMemoryItem.maxTotal < item.privateSize
							+ item.sharedSize) {
						maxMemoryItem.maxTotal = item.privateSize
								+ item.sharedSize;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Object getFloorEntryFromMap(long key, TreeMap map) {
		// TODO if JDK 6 is in use this should be used:
		// return map.floorEntry(key).getValue();

		// when JDK 5 in use use this:
		try {
			SortedMap headMap = map.headMap(key);
			return headMap.get(headMap.lastKey());
		} catch (Exception e) {
			return null;
		}

	}
}
