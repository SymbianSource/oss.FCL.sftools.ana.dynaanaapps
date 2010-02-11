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

package com.nokia.carbide.cpp.pi.memory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.draw2d.IFigure;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericThread;
import com.nokia.carbide.cpp.internal.pi.model.TraceWithThreads;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public class MemTrace extends GenericSampledTrace implements TraceWithThreads {
	private static final long serialVersionUID = -2073732380166861141L;

	// Symbian OS memory models
	transient public static final long MEMORY_DIRECT = 0;
	transient public static final long MEMORY_MOVING = 1;
	transient public static final long MEMORY_MULTIPLE = 2;
	transient public static final long MEMORY_UNKNOWN = -1;

	transient private long memoryModel;

	private transient MemTraceGraph[] graphs;

	private Hashtable<Integer, Integer> firstSynchTimes;
	private Hashtable<Integer, Integer> lastSynchTimes;
	private MemThread[] threads;

	// maximums for the entire trace
	transient private int traceMaxChunks = 0;
	transient private int traceMaxStackHeap = 0;
	transient private int traceMaxTotal = 0;

	// maximums found the last time setMaxMemDataByInterval was called
	transient private int intervalMaxChunks = 0;
	transient private int intervalMaxStackHeap = 0;
	transient private int intervalMaxTotal = 0;

	transient private Hashtable<String, TreeMap<Long, MemSample>> drawDataByMemThread = null;
	transient private ArrayList<MemSampleByTime> drawDataByTime;
	transient private HashSet<MemThread> noDuplicateMemThreads;

	transient private long intervalStart = -1;
	transient private long intervalEnd = -1;

	private int version;

	public MemTrace() {
		firstSynchTimes = new Hashtable<Integer, Integer>();
		lastSynchTimes = new Hashtable<Integer, Integer>();
	}

	public void addSample(MemSample sample) {
		this.samples.add(sample);
	}

	public MemSample getMemSample(int number) {
		return (MemSample) this.samples.elementAt(number);
	}

	public GenericThread[] getThreads() {
		return threads;
	}

	public void setThreads(MemThread[] threads) {
		this.threads = threads;
	}

	public int getTraceMaxChunks() {
		return traceMaxChunks;
	}

	public int getTraceMaxStackHeap() {
		return traceMaxStackHeap;
	}

	public int getTraceMaxTotal() {
		return traceMaxTotal;
	}

	public void setTraceMaxStackHeap(int traceMaxStackHeap) {
		this.traceMaxStackHeap = traceMaxStackHeap;
	}

	public void setTraceMaxTotal(int traceMaxTotal) {
		this.traceMaxTotal = traceMaxTotal;
	}

	public void setTraceMaxChunks(int traceMaxChunks) {
		this.traceMaxChunks = traceMaxChunks;
	}

	public int getIntervalMaxChunks() {
		return intervalMaxChunks;
	}

	public int getIntervalMaxStackHeap() {
		return intervalMaxStackHeap;
	}

	public int getIntervalMaxTotal() {
		return intervalMaxTotal;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void addFirstSynchTime(Integer id, Integer firstSynchTime) {
		firstSynchTimes.put(id, firstSynchTime);
	}

	public void addLastSynchTime(Integer id, Integer lastSynchTime) {
		lastSynchTimes.put(id, lastSynchTime);
	}

	public GenericTraceGraph getTraceGraph(int graphIndex) {
		if (graphs == null) {
			graphs = new MemTraceGraph[3];
		}

		// note that graphIndex need not match the index sent to MemTraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				graphs[graphIndex] = new MemTraceGraph(graphIndex, this, uid);
			}
			return graphs[graphIndex];
		}
		return null;
	}

	public void gatherDrawData() {
		if (version < 202) {
			gatherSamplingBasedDrawData();
		} else {
			gatherEventBasedDrawData();
		}
	}

	public void gatherSamplingBasedDrawData() {
		if (drawDataByMemThread != null)
			return; // already initialised

		drawDataByMemThread = new Hashtable<String, TreeMap<Long, MemSample>>();
		memoryModel = MemTrace.MEMORY_UNKNOWN;
		getThreadsFromTrace();

		if (drawDataByTime == null)
			drawDataByTime = new ArrayList<MemSampleByTime>();

		long lastTime = Integer.MIN_VALUE;
		ArrayList<MemSample> memSamples = null;

		traceMaxChunks = 0;
		traceMaxStackHeap = 0;
		traceMaxTotal = 0;

		int currentChunks = 0;
		int currentStackHeap = 0;
		int currentTotal = 0;

		long usedMemory = 0;
		long freeMemory = 0;

		// index each sample by thread/process name and by sample time
		MemSample memSample;
		for (Enumeration e = this.getSamples(); e.hasMoreElements();) {
			memSample = (MemSample) e.nextElement();

			if (lastTime != memSample.sampleSynchTime) {
				// the first sample in each time period contains system-wide
				// info
				if (memSample.thread.threadId == 0xffffffffbabbeaaaL) {
					usedMemory = memSample.heapSize;
					freeMemory = memSample.stackSize;
					memSample = (MemSample) e.nextElement();
				}

				// the 2nd sample in each time period has memory model and
				// CodeSeg data
				if (memSample.thread.threadId == 0xffffffffbabbea20L) {
					usedMemory = memSample.heapSize;
					freeMemory = memSample.stackSize;
					memSample = (MemSample) e.nextElement();
				}

				// Create MemSampleByTime object based on sample
				MemSampleByTime memSampleByTime = new MemSampleByTime(
						memSample.sampleSynchTime, usedMemory + freeMemory,
						usedMemory);

				// add sample to drawn samples
				drawDataByTime.add(memSampleByTime);

				// add sample to array that contains all samples
				memSamples = memSampleByTime.getSamples();

				// save time
				lastTime = memSample.sampleSynchTime;

				// find the maximums for the entire graph
				if (currentChunks > traceMaxChunks)
					traceMaxChunks = currentChunks;
				if (currentStackHeap > traceMaxStackHeap)
					traceMaxStackHeap = currentStackHeap;
				if (currentTotal > traceMaxTotal)
					traceMaxTotal = currentTotal;

				currentChunks = 0;
				currentStackHeap = 0;
				currentTotal = 0;
			}

			// Add sample to its thread's data hashtable
			// drawDataByMemThread.get(memSample.thread.fullName).add(memSample);
			drawDataByMemThread.get(memSample.thread.fullName).put(
					memSample.sampleSynchTime, memSample);

			// add sample sample array that contains all samples
			memSamples.add(memSample);

			currentChunks += memSample.heapSize;
			currentStackHeap += memSample.stackSize;
			currentTotal += memSample.heapSize + memSample.stackSize;
		}

		// find the maximums for the entire graph
		if (currentChunks > traceMaxChunks)
			traceMaxChunks = currentChunks;
		if (currentStackHeap > traceMaxStackHeap)
			traceMaxStackHeap = currentStackHeap;
		if (currentTotal > traceMaxTotal)
			traceMaxTotal = currentTotal;
	}

	public void gatherEventBasedDrawData() {
		if (drawDataByMemThread != null)
			return; // already initialised

		drawDataByMemThread = new Hashtable<String, TreeMap<Long, MemSample>>();
		memoryModel = MemTrace.MEMORY_UNKNOWN;
		getThreadsFromTrace();

		if (drawDataByTime == null)
			drawDataByTime = new ArrayList<MemSampleByTime>();

		long lastTime = Integer.MIN_VALUE;
		ArrayList<MemSample> memSamples = null;

		long usedMemory = 0;
		long freeMemory = 0;

		// index each sample by thread/process name and by sample time
		MemSample memSample;
		for (Enumeration e = this.getSamples(); e.hasMoreElements();) {
			memSample = (MemSample) e.nextElement();

			if (lastTime != memSample.sampleSynchTime) {
				// the first sample in each time period contains system-wide
				// info
				if (memSample.thread.threadId == 0xffffffffbabbeaaaL) {
					usedMemory = memSample.heapSize;
					freeMemory = memSample.stackSize;
					addSampleToThread(memSample);
					memSample = (MemSample) e.nextElement();

				}

				// Create MemSampleByTime object based on sample
				// MemSampleByTime memSampleByTime = new
				// MemSampleByTime(memSample.sampleSynchTime, usedMemory +
				// freeMemory, usedMemory);

				// add sample to drawn samples
				// drawDataByTime.add(memSampleByTime);

				// add sample to array that contains all samples
				// memSamples = memSampleByTime.getSamples();

				// save time
				lastTime = memSample.sampleSynchTime;

			}

			// Add sample to its thread's data hashtable
			addSampleToThread(memSample);

			// add sample sample array that contains all samples
			// memSamples.add(memSample);
		}

	}

	private void addSampleToThread(MemSample sample) {

		// get sample array from thread
		TreeMap<Long, MemSample> samples = drawDataByMemThread
				.get(sample.thread.fullName);

		if (sample.type == MemTraceParser.SAMPLE_CODE_DELETE_CHUNK) {
			// event is delete event, set stack and heap size to zero
			sample.stackSize = 0;
			sample.heapSize = 0;
		}

		if (samples != null) {
			// Add initial sample to thread
			samples.put(sample.sampleSynchTime, sample);
			return;
		}

		System.out.println("PI ERROR: Thread not found");

	}

	private void getThreadsFromTrace() {

		noDuplicateMemThreads = new HashSet<MemThread>(131, 0.75f);

		// the parser creates multiple copies of the same MemThread item,
		// so we have to use the full name of the MemThread to access sample
		// data
		for (MemThread memThread : threads) {
			String processedThreadName = memThread.threadName;

			// Add Thread ID into processedThreadName
			// looking for _T and _C suffixes and remove them for thread
			if (processedThreadName.endsWith("_T")) //$NON-NLS-1$
			{
				processedThreadName = processedThreadName.substring(0,
						processedThreadName.length() - 2)
						+ "_" + memThread.threadId; //$NON-NLS-1$
			} else if (processedThreadName.endsWith("_C")) { //$NON-NLS-1$
				processedThreadName = processedThreadName.substring(0,
						processedThreadName.length() - 2)
						+ " [0x" + Integer.toHexString(memThread.threadId) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				processedThreadName += "_" + memThread.threadId; //$NON-NLS-1$
			}

			// Full name contains process name, thread name and thread id
			memThread.fullName = memThread.processName
					+ "::" + processedThreadName; //$NON-NLS-1$

			memThread.enabled = new boolean[3];
			memThread.enabled[0] = true;
			memThread.enabled[1] = true;
			memThread.enabled[2] = true;

			memThread.maxMemoryItem = new MaxMemoryItem();
			memThread.maxMemoryItem.maxChunks = 0;
			memThread.maxMemoryItem.maxStackHeap = 0;
			memThread.maxMemoryItem.maxTotal = 0;

			if (drawDataByMemThread.get(memThread.fullName) == null) {
				drawDataByMemThread.put(memThread.fullName,
						new TreeMap<Long, MemSample>());
				noDuplicateMemThreads.add(memThread);
			}
		}

	}

	public TreeMap<Long, MemSample> getDrawDataByMemThread(MemThread id) {
		return this.drawDataByMemThread.get(id.fullName);
	}

	public ArrayList<MemSampleByTime> getDrawDataByTime() {
		return this.drawDataByTime;
	}

	public HashSet<MemThread> getNoDuplicateMemThreads() {
		return this.noDuplicateMemThreads;
	}

	public ArrayList<MemSample> getMemSampleDataByTime(long time) {
		if ((this.drawDataByTime == null)
				|| (time < this.drawDataByTime.get(0).getTime()))
			return null;

		int i = 0;
		for (; i < this.drawDataByTime.size(); i++) {
			MemSampleByTime sample = this.drawDataByTime.get(i);
			if (sample.getTime() > time)
				break;
		}

		return this.drawDataByTime.get(i - 1).getSamples();
	}

	public MemThread[] getMemThreads() {
		return threads;
	}

	public MaxMemoryItem getSystemUseByInterval(long startTime, long endTime) {
		MaxMemoryItem item = new MaxMemoryItem();

		if ((this.drawDataByTime == null)
				|| ((startTime == intervalStart) && (endTime == intervalEnd)))
			return item;

		if (this.getVersion() >= 202) {
			TreeMap<Long, MemSample> events = drawDataByMemThread
					.get("TOTAL_MEMORY::TOTAL_MEMORY_-1162089814");

			Iterator<MemSample> values = events.values().iterator();

			while (values.hasNext()) {
				MemSample memSample = values.next();
				// store system used memory as chunks and total memory as total
				if (item.maxChunks < memSample.heapSize)
					item.maxChunks = memSample.heapSize;

				if (item.maxTotal < memSample.stackSize + memSample.heapSize)
					item.maxTotal = memSample.stackSize + memSample.heapSize;

				if (memSample.sampleSynchTime > endTime)
					break;

			}

		} else {
			long firstTime = this.drawDataByTime.get(0).getTime();
			if ((startTime < firstTime) && (endTime < firstTime))
				return item;

			MemSampleByTime memSampleByTime;

			int i = 0;

			// find least sampling time greater than or equal to the start index
			for (; i < this.drawDataByTime.size(); i++) {
				if (((MemSampleByTime) this.drawDataByTime.get(i)).getTime() > startTime)
					break;
			}

			if (i != 0)
				i--;

			for (; i < this.drawDataByTime.size(); i++) {
				memSampleByTime = this.drawDataByTime.get(i);

				// store system used memory as chunks and total memory as total
				if (item.maxChunks < memSampleByTime.getUsedMemory())
					item.maxChunks = memSampleByTime.getUsedMemory();

				if (item.maxTotal < memSampleByTime.getTotalMemory())
					item.maxTotal = memSampleByTime.getTotalMemory();

				if (memSampleByTime.getTime() > endTime)
					break;
			}

		}

		return item;
	}

	public void setMaxMemDataByInterval(long startTime, long endTime) {

		// Set all values to zero
		for (int i = 0; i < threads.length; i++) {
			MaxMemoryItem item = threads[i].maxMemoryItem;
			item.maxChunks = 0;
			item.maxStackHeap = 0;
			item.maxTotal = 0;
		}

		// if no data is found between start and end time or start and end
		// times are same with previous.
		if ((this.drawDataByTime == null)
				|| ((startTime == intervalStart) && (endTime == intervalEnd)))
			return;

		if (this.version >= 202) {

			for (Enumeration e = drawDataByMemThread.elements(); e
					.hasMoreElements();) {
				TreeMap<Long, MemSample> memSamples = (TreeMap<Long, MemSample>) e
						.nextElement();

				if (memSamples.size() == 0) {
					continue;
				}

				MemSample firstSample = (MemSample) memSamples.get(memSamples
						.lastKey());

				MemThread thread = firstSample.thread;

				MaxMemoryItem maxMemoryItem = thread.maxMemoryItem;

				SortedMap<Long, MemSample> subMap = memSamples.subMap(
						startTime, endTime);

				ArrayList<MemSample> samples = new ArrayList<MemSample>(subMap
						.values());

				MemSample floorSample = (MemSample) MemTrace
						.getFloorEntryFromMap(startTime, memSamples);

				if (floorSample != null) {

					// TODO check these!
					// MemSample firstSample = floorEntry.getValue();

					if (firstSample != null) {
						samples.add(firstSample);
					}
				}

				for (MemSample item : samples) {
					if (maxMemoryItem.maxChunks < item.heapSize) {
						maxMemoryItem.maxChunks = item.heapSize;
					}
					if (maxMemoryItem.maxStackHeap < item.stackSize) {
						maxMemoryItem.maxStackHeap = item.stackSize;
					}
					if (maxMemoryItem.maxTotal < item.heapSize + item.stackSize) {
						maxMemoryItem.maxTotal = item.heapSize + item.stackSize;
					}

				}

			}

		} else {

			// check that start time is not before first sample's time or end
			// time is not before first sample time
			long firstTime = this.drawDataByTime.get(0).getTime();
			if ((startTime < firstTime) && (endTime < firstTime))
				return;

			MemSampleByTime memSampleByTime;

			int i = 0;

			// find first sample which is greater than start time
			for (; i < this.drawDataByTime.size(); i++) {
				memSampleByTime = this.drawDataByTime.get(i);
				if (memSampleByTime.getTime() > startTime)
					break;
			}

			if (i != 0)
				--i;
			memSampleByTime = this.drawDataByTime.get(i);

			// 
			int size = this.drawDataByTime.size();

			// go thru samples and find max values for chunks and heapsandstacks
			do {
				ArrayList<MemSample> memSamples = memSampleByTime.getSamples();

				for (int j = 0; j < memSamples.size(); j++) {
					MemSample memSample = memSamples.get(j);
					MemThread memThread = memSample.thread;
					MaxMemoryItem maxMemoryItem = memThread.maxMemoryItem;

					if (maxMemoryItem == null)
						continue;

					if (maxMemoryItem.maxChunks < memSample.heapSize)
						maxMemoryItem.maxChunks = memSample.heapSize;
					if (maxMemoryItem.maxStackHeap < memSample.stackSize)
						maxMemoryItem.maxStackHeap = memSample.stackSize;
					if (maxMemoryItem.maxTotal < memSample.heapSize
							+ memSample.stackSize)
						maxMemoryItem.maxTotal = memSample.heapSize
								+ memSample.stackSize;

					// find the maximums for all threads/processes in the
					// interval
					if (memSample.heapSize > traceMaxChunks)
						intervalMaxChunks = memSample.heapSize;
					if (memSample.stackSize > traceMaxStackHeap)
						intervalMaxStackHeap = memSample.stackSize;
					if (memSample.heapSize + memSample.stackSize > traceMaxTotal)
						intervalMaxTotal = memSample.heapSize
								+ memSample.stackSize;
				}
				i++;
				if (i < size)
					memSampleByTime = this.drawDataByTime.get(i);
				else
					break;
			} while (memSampleByTime.getTime() <= endTime);

		}

		return;
	}

	public long getMemoryModel() {
		return this.memoryModel;
	}

	public void setMemoryModel(long memoryModel) {
		if (memoryModel == MemTrace.MEMORY_DIRECT
				|| memoryModel == MemTrace.MEMORY_MOVING
				|| memoryModel == MemTrace.MEMORY_MULTIPLE)
			this.memoryModel = memoryModel;
		else
			this.memoryModel = MemTrace.MEMORY_UNKNOWN;
	}

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
