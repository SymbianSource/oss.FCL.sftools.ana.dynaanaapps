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

package com.nokia.carbide.cpp.pi.irq;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * Class containing all samples from one Irq trace
 */
public class IrqTrace extends GenericSampledTraceWithFunctions {

	private static final long serialVersionUID = 5128848053984932839L;

	private transient IrqTraceGraph[] graphs;

	/* Hashtable containing thread id's and swi offsets */
	private Hashtable<Long, Integer> threadToOffset;

	/* Hashtable containing thread id's and names */
	private Hashtable<Long, String> threadToName;

	/* Hashtable containing Irq lines and offsets */
	private Hashtable<Long, Integer> irqToOffset;

	/* Vector containing all thread wrappers */
	private transient Vector<SwiThreadWrapper> allThreadWrappers;

	/* Hashtable containing thread id's and all samples from that thread */
	private transient Hashtable<Long, IrqSampleTypeWrapper> swiTable;

	/* Hashtable containing irq line id's and all samples from that line */
	private transient Hashtable<Long, IrqSampleTypeWrapper> irqTable;

	/* Hashtable containing thread names functions */
	private transient Hashtable<String, Hashtable<String, ArrayList<Long>>> threadsToFunctions;

	/* All colors in use */
	private transient HashSet<RGB> colorSet;
	
	/* Colors in functions and irq lines */
	private Hashtable<String, RGB> colorsOfThreadsAndLines;

	/* maximum amount of swi interrupts at one time frame */
	private int maxAmountOfSWISamples;

	/* maximum amount of irq interrupts at one time frame */
	private int maxAmountOfIRQSamples;

	/* Boolean value that is false if no function names is found from trace
	 * (No Symbol files imported) 
	 */
	boolean functionNamesFound = false;
	
	/**
	 * getter for trace graph
	 * 
	 * @param graphIndex
	 *            index of the graph
	 * @return graph
	 */
	public GenericTraceGraph getTraceGraph(int graphIndex) {
		if (graphs == null) {
			graphs = new IrqTraceGraph[3];
		}
		// note that graphIndex need not match the index sent to TraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			int uid = NpiInstanceRepository.getInstance().activeUid();
			
			if(graphs[graphIndex] != null){
				return graphs[graphIndex];
			}
			
			// get first item that is null in graph array
			int index = 0;
			
			while(index < graphs.length){
				if( graphs[index] == null){
					break;
				}
				index++;
			}
			if(index < graphs.length){
				graphs[index] = new IrqTraceGraph(index, this, uid);
				return graphs[index];
			}
			
		}
		return null;
	}

	/**
	 * Fetches thread name for one sample from the address plug-in
	 * 
	 * @param sample
	 *            sample
	 * @return thread name
	 */
	@SuppressWarnings("unchecked")
	private String getThreadName(IrqSample sample) {

		if (sample.getType() == IrqSample.TYPE_SWI) {
			// fetch trace information from address plug-in
			ParsedTraceData ptd = TraceDataRepository.getInstance().getTrace(
					NpiInstanceRepository.getInstance().activeUid(),
					GppTrace.class);

			// if trace data or static data cannot be found
			if (ptd == null || ptd.staticData == null) {
				return Messages.IrqTrace_0
						+ Long.toHexString(sample.getThreadValue());
			}

			// Get that contains name for thread
			ArrayList al = ptd.staticData.getColumnMatch(Messages.IrqTrace_1,
					Messages.IrqTrace_2, new Long(sample.getThreadValue()));
			if (al.size() > 0) {
				return (String) al.get(0);
			} else {
				return Messages.IrqTrace_3
						+ Long.toHexString(sample.getThreadValue());
			}

		}
		return Messages.IrqTrace_4;
	}

	/**
	 * Gathers data that is needed for drawing graph.
	 */
	public void gatherDrawData() {

		boolean readThreadNamesFromTrace = false;

		// if hash containing thread names is not found(meaning that dat file
		// has just been imported) load thread names from trace.
		if (threadToName == null) {
			threadToName = new Hashtable<Long, String>();
			readThreadNamesFromTrace = true;
		}
		
		if(colorsOfThreadsAndLines == null){
			colorsOfThreadsAndLines = new Hashtable<String, RGB>();
		}

		// reset all vectors and Hashtables that are just for optimizing drawing
		allThreadWrappers = new Vector<SwiThreadWrapper>();
		swiTable = new Hashtable<Long, IrqSampleTypeWrapper>();
		irqTable = new Hashtable<Long, IrqSampleTypeWrapper>();
		threadToOffset = new Hashtable<Long, Integer>();
		this.irqToOffset = new Hashtable<Long, Integer>();
		threadsToFunctions = new Hashtable<String, Hashtable<String, ArrayList<Long>>>();
		colorSet = new HashSet<RGB>();
		Hashtable<Long, Hashtable<Long, Integer>> irqLines = new Hashtable<Long, Hashtable<Long, Integer>>();

		int swiOffset = 0;
		int irqOffset = 0;

		maxAmountOfSWISamples = 1;
		maxAmountOfIRQSamples = 1;
		int amountOfSamples = 0;
		long previousTimeCode = 0;

		// go thru all samples from trace
		Enumeration<GenericSample> enumeration = this.getSamples();
		while (enumeration.hasMoreElements()) {
			IrqSample sample = (IrqSample) enumeration.nextElement();
			long val = 0;
			long valL = 0;
			Hashtable<Long, IrqSampleTypeWrapper> table = new Hashtable<Long, IrqSampleTypeWrapper>();

			// if this is software interrupt
			if (sample.getType() == IrqSample.TYPE_SWI) {
				val = sample.getLrValue();
				table = swiTable;

				String threadName = Messages.IrqTrace_5;
				// Get thread name
				if (readThreadNamesFromTrace) {
					threadName = getThreadName(sample);
				} else {
					threadName = threadToName.get(sample.getThreadValue());
				}

				if(sample.getFunction()!= null){
					
					this.functionNamesFound = true;
					
					// Add thread into threadsToFunctions hash if needed
					if (!threadsToFunctions.containsKey(threadName)) {
						// Create new item into thread section
						Hashtable<String, ArrayList<Long>> functionTable = new Hashtable<String, ArrayList<Long>>();
						ArrayList<Long> list = new ArrayList<Long>();
						list.add(sample.sampleSynchTime);
						functionTable.put(sample.getFunction().getFunctionName(), list);
						threadsToFunctions.put(threadName, functionTable);
						previousTimeCode = sample.sampleSynchTime;
						amountOfSamples = 1;
	
					} else {
						// add sample to existing thread
	
						// check if previous sample from this thread was from same
						// time frame and increase amountOfSamples if needed
						if (sample.sampleSynchTime == previousTimeCode) {
							amountOfSamples++;
						} else {
							// check is amount of samples from previous tim frame
							// was greater than maximum amount of interrupts so far
							if (amountOfSamples > maxAmountOfSWISamples) {
								maxAmountOfSWISamples = amountOfSamples;
							}
							amountOfSamples = 0;
							previousTimeCode = sample.sampleSynchTime;
						}
	
						// get thread's hashtable containing all its functions
						Hashtable<String, ArrayList<Long>> functionSet = threadsToFunctions
								.get(threadName);
	
						String functionName = sample.getFunction().getFunctionName();
	
						if (!functionSet.containsKey(functionName)) {
							// add new function into table
							ArrayList<Long> list = new ArrayList<Long>();
							list.add(sample.sampleSynchTime);
							functionSet
									.put(sample.getFunction().getFunctionName(), list);
						} else {
							functionSet.get(functionName).add(
									sample.sampleSynchTime);
						}
					}
				}				
				// if thread is not yet found from threadToOffset
				if (!this.threadToOffset.containsKey(new Long(sample
						.getThreadValue()))) {
					this.threadToOffset.put(new Long(sample.getThreadValue()),
							Integer.valueOf(swiOffset++));

					// read thread names from trace if needed
					if (readThreadNamesFromTrace) {
						try {
							this.threadToName.put(new Long(sample
									.getThreadValue()), this
									.getThreadName(sample));
						} catch (NullPointerException e1) {
							this.threadToName
									.put(new Long(sample.getThreadValue()),
											Messages.IrqTrace_6
													+ Long.toHexString(sample
															.getThreadValue()));
						}
					}

					// create thread wrapper for the thread and add it to allThreadWrappers
					SwiThreadWrapper wrapper = new SwiThreadWrapper();
					wrapper.threadAddress = new Long(sample.getThreadValue());
					wrapper.threadName = threadToName.get(sample
							.getThreadValue());
					this.allThreadWrappers.add(wrapper);
				}
				
			// if this is hardware interrupt
			} else if (sample.getType() == IrqSample.TYPE_IRQ) {
				val = sample.getIrqL1Value() + (sample.getIrqL2Value() << 8);
				table = irqTable;

				if (!this.irqToOffset.containsKey(new Long(val))) {
					this.irqToOffset.put(new Long(val),
							Integer.valueOf(irqOffset++));
				}

				// if line is already found from irq line hashtable
				if (!irqLines.containsKey(val)) {
					Hashtable<Long, Integer> hashTable = new Hashtable<Long, Integer>();
					hashTable.put(val, 1);
					irqLines.put(val, hashTable);
				} else {
					Hashtable<Long, Integer> interrupts = irqLines.get(val);
					
					// if event at this timeframe is not found yet
					if (!interrupts.containsKey(sample.sampleSynchTime)) {
						interrupts.put(sample.sampleSynchTime, 1);
					} else {
						int previousValue = interrupts
								.get(sample.sampleSynchTime);
						interrupts.put(sample.sampleSynchTime,
								previousValue + 1);
						
						// check if value is greater than max amount of interrupts 
						if (previousValue + 1 > maxAmountOfIRQSamples) {
							maxAmountOfIRQSamples = previousValue + 1;
						}
					}
				}
			} else {
				throw new ArrayIndexOutOfBoundsException();
			}

			// Add interrupts into its sampleTypeWrapper or create one if needed
			valL = new Long(val);
			if (!table.containsKey(valL)) {
				IrqSampleTypeWrapper w = new IrqSampleTypeWrapper(sample,
						colorSet, colorsOfThreadsAndLines);
				table.put(valL, w);
			} else {
				IrqSampleTypeWrapper w = (IrqSampleTypeWrapper) table.get(valL);
				w.addSample(sample);
			}
		}
	}


	/**
	 * @return all thread wrappers vector
	 */
	public Vector<SwiThreadWrapper> getAllThreadWrappers() {
		return allThreadWrappers;
	}

	/**
	 * @return irq to offset hashtable
	 */
	public Hashtable<Long, Integer> getIrqToOffset() {
		return irqToOffset;
	}

	/**
	 * @return swi hashtable
	 */
	public Hashtable<Long, IrqSampleTypeWrapper> getSwiTable() {
		return swiTable;
	}

	/**
	 * @return irq hashtable
	 */
	public Hashtable<Long, IrqSampleTypeWrapper> getIrqTable() {
		return irqTable;
	}

	/**
	 * @return threadsToFunctions hashtable
	 */
	public Hashtable<String, Hashtable<String, ArrayList<Long>>> getThreadsToFunctions() {
		return threadsToFunctions;
	}

	/**
	 * @return max amount of swi interrupts per one timeframe
	 */
	public int getMaxAmountOfSWISamples() {
		return maxAmountOfSWISamples;
	}

	/**
	 * @return max amount of irq interrupts per one timeframe
	 */
	public int getMaxAmountOfIRQSamples() {
		return maxAmountOfIRQSamples;
	}

	/**
	 * @return thread and irq line colors
	 */
	public Hashtable<String, RGB> getColorsOfThreadsAndLines() {
		return colorsOfThreadsAndLines;
	}
	
	/**
	 * @return set containing all colors of the trace
	 */
	public HashSet<RGB> getColorSet(){
		return colorSet;
	}
	
	/**
	 * changes color of one thread or irq line
	 * @param wrapper threadwrapper
	 * @param newColor new color for item
	 */
	public void changeColorOfThreadOrIRQLine(IrqSampleTypeWrapper wrapper, RGB newColor){
		
		// save color into colorsets at trace information
		colorSet.remove(wrapper.rgb);
		colorSet.add(newColor);
		if(wrapper.getPrototypeSample().getType() == IrqSample.TYPE_IRQ){
			colorsOfThreadsAndLines.put(Long.toString(wrapper.getPrototypeSample().getIrqL1Value()), newColor);
		}
		else{
			if(wrapper.getPrototypeSample().getFunction() != null){
				colorsOfThreadsAndLines.put(wrapper.getPrototypeSample().getFunction().getFunctionName(), newColor);
			}
		}
		wrapper.rgb = newColor;
		
		for(IrqTraceGraph item : graphs){
			// refresh table and set editor window dirty so that it can be saved.
			if(wrapper.getPrototypeSample().getType() == IrqSample.TYPE_IRQ){
				item.irqLineUnchecked(wrapper);
				item.irqLineChecked(wrapper);
			}
			else{
				item.recalculateWholeGraph();
			}
		}
		PIPageEditor.currentPageEditor().setDirty();
	}
	
	/**
	 * updates all table viewers
	 */
	public void updateAllTableViewers(){
		for(IrqTraceGraph item : graphs){
			item.updateTableViewers();
		}
	}
	
	/**
	 * @return all graphs for this trace
	 */
	public IrqTraceGraph[] getGraphs() {
		return graphs;
	}

	/**
	 * @return true if function names are found from trace
	 */
	public boolean isFunctionNamesFound() {
		return functionNamesFound;
	}
	
	

}
