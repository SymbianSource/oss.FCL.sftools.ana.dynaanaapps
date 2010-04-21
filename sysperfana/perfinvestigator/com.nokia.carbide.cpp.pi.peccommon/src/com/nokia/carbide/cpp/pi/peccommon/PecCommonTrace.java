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

package com.nokia.carbide.cpp.pi.peccommon;

import java.util.Arrays;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;

/**
 * The model class for Performance Counter traces. This manages the data
 * for the IPC trace graphs, and is responsible for creating the graphs. 
 */
public class PecCommonTrace extends GenericSampledTrace {
	private static final long serialVersionUID = 4425739452429422333L;

	private int samplingInterval;
	
	/**
	 * hold the event types which have been traced
	 */
	private String[] valueTypes;

	// the following is needed for calculations for the legend (depend on
	// selected area). We keep it here since the selected area is the same for all graphs.
	// If this assumption ever changes, the calculations need to be moved into
	// the LegendContentProvider
	private double selectionStart;
	private double selectionEnd;
	private transient boolean needsRecalc;

	private transient PecCommonLegendElement[] legendElements;
	
	/**
	 * Constructor
	 */
	public PecCommonTrace() {
		super();
		selectionStart = -1;
		selectionEnd = -1;
		needsRecalc = true;
	}
	
	

	/**
	 * Set the event types present in the interconnect performance counter trace.
	 * @param valueTypes the event types to set
	 */
	public void setValueTypes(String[] valueTypes) {
		this.valueTypes = valueTypes;
	}

	/**
	 * Get the event types present in the interconnect performance counter trace.
	 * @return the event types present in the trace
	 */
	public String[] getValueTypes() {
		return valueTypes;
	}

	/**
	 * @return the sampling interval in milliseconds
	 */
	public int getSamplingInterval() {
		return samplingInterval;
	}

	/**
	 * Setter for the sampling interval in milliseconds
	 * @param samplingInterval the sampling interval to set
	 */
	public void setSamplingInterval(int samplingInterval) {
		this.samplingInterval = samplingInterval;
	}
	
	/**
	 * Callback for PIEvent.SELECTION_AREA_CHANGED
	 * @param newStart new selection start
	 * @param newEnd new selection end
	 */
	public void selectionAreaChanged(double newStart, double newEnd) {
		if (newStart != selectionStart || newEnd != selectionEnd){
			selectionStart = newStart;
			selectionEnd = newEnd;
			needsRecalc = true;
		}
	}    
	
    /**
	 * @return the legendElements
	 */
	public PecCommonLegendElement[] getLegendElements() {
		if (needsRecalc || legendElements == null){
			needsRecalc = false;
			calculateValues();
		}
		return legendElements;
	}
	
	/**
	 * calculates values for the legend depending on the selected time frame
	 */
	private void calculateValues() {
    	int c = this.valueTypes.length;
		int[] cnts = new int[c];
		long[] sums = new long[c];
		int[] mins = new int[c];
		int[] maxs = new int[c];

		if (selectionStart >= 0 && selectionEnd >= 0){
			
			Arrays.fill(mins, Integer.MAX_VALUE);
			Arrays.fill(maxs, Integer.MIN_VALUE);
			
			int start = (int) ((selectionStart + 0.5f));
			int end = (int) ((selectionEnd + 0.5f));
			
			Vector<GenericSample> selectedSamples = this.getSamplesInsideTimePeriod(start, end);
			
			for (GenericSample genericSample : selectedSamples) {
				PecCommonSample sample = (PecCommonSample)genericSample;
				
				//average, sum, min, max
				for (int i = 0; i < sample.values.length; i++) {
					int value = sample.values[i];
					
					cnts[i] ++;
					sums[i] += value;
					if (value < mins[i]){
						mins[i] = value;					
					} 
					if (value > maxs[i]){
						maxs[i] = value;					
					}
				}
			}
		}
		
		legendElements = createLegendElements(legendElements, this.valueTypes, cnts, sums, mins, maxs);
	}
	
	/**
	 * Creates or updates all legend elements
	 * @param existingElements Existing elements, if they need updating. If null, new elements are to be created.
	 * @param typeStrings Array of all trace event strings present in the trace 
	 * @param cnts Array of all count values (one per graph)
	 * @param sums Array of all sum values (one per graph)
	 * @param mins Array of all minimum values (one per graph)
	 * @param maxs Array of all maximum values (one per graph)
	 * @return Array of legend elements created or updated
	 */
	protected PecCommonLegendElement[] createLegendElements(PecCommonLegendElement[] existingElements, String[] typeStrings, int[] cnts,
			long[] sums, int[] mins, int[] maxs) {
		
    	int c = typeStrings.length;
    	boolean create = existingElements == null;    	
    	PecCommonLegendElement[] les = create ? new PecCommonLegendElement[c] : existingElements;
		char shortTitle = 'A';
		
		for (int i = 0; i < c; i++) {
			
			PecCommonLegendElement legendElement = create ? new PecCommonLegendElement(i, typeStrings[i], shortTitle, false) : existingElements[i];
			legendElement.setCnt(cnts[i]);
			legendElement.setSum(sums[i]);
			legendElement.setMax(maxs[i]);
			legendElement.setMin(mins[i]);
			les[i] = legendElement;
			shortTitle ++;
		}
		return les;
	}
}
