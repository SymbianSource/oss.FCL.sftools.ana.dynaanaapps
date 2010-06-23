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
package com.nokia.carbide.cpp.pi.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;

public class ProfiledLibraryEvent extends ProfiledGeneric {
	class GraphSamplePoint {
		long firstSamplePoint;
		long lastSamplePoint;

		public GraphSamplePoint(long firstSamplePoint, long lastSamplePoint) {
			this.firstSamplePoint = firstSamplePoint;
			this.lastSamplePoint = lastSamplePoint;
		}
	}

	private TreeMap<Long, MemSample> memSamples;
	private List<GraphSamplePoint> grapSamplePoints;

	
	public ProfiledLibraryEvent(String name) {
		super(-1, 3);
		setNameString(name);
		this.memSamples = new TreeMap<Long, MemSample>();	
	}
	
	public void addMemSample(MemSample memSample){
		if(memSample.stackSize == 0 && memSample.sampleNum == 0){
			memSample.heapSize = 0;
		}
		memSamples.put(memSample.sampleSynchTime, memSample);
	}
	

	public void updateSelection(long startTime, long endTime) {

		if (memSamples.size() == 0) {
			return;
		}
	
		MemSample firstSample = (MemSample) memSamples
				.get(memSamples.lastKey());

		MemThread thread = firstSample.thread;

		MaxMemoryItem maxMemoryItem = thread.maxMemoryItem;

		SortedMap<Long, MemSample> subMap = memSamples.subMap(startTime,
				endTime);

		ArrayList<MemSample> samples = new ArrayList<MemSample>(subMap.values());		

		if(samples.isEmpty()){
			maxMemoryItem.maxChunks = 0;
			maxMemoryItem.maxTotal = 0;
		}else{
			int count = 0;
			maxMemoryItem.maxChunks = 0;
			for(MemSample memSample : samples){
				if(memSample.type != MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK){
					count++;
				}		
				if(memSample.type == MemTraceParser.SAMPLE_CODE_NEW_CHUNK || memSample.type == MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK){
					maxMemoryItem.maxChunks += memSample.heapSize;
				}else if(memSample.type == MemTraceParser.SAMPLE_CODE_DELETE_CHUNK ){
					if(memSample.heapSize == 0){
						maxMemoryItem.maxChunks = 0;
					}else{
						maxMemoryItem.maxChunks -= memSample.heapSize;
					}					
				}
			}
			if(maxMemoryItem.maxChunks  < 0 ){
				maxMemoryItem.maxChunks = 0;
			}
			maxMemoryItem.maxTotal = count;
		}	
	}
		
	public MemThread getLastMemThread(){
		if(memSamples.isEmpty()){
			MemThread mt = new MemThread(-1, "","");
			mt.maxMemoryItem = new MaxMemoryItem();
			return mt;
		}
		return memSamples.get(memSamples.lastKey()).thread;
	}

	public TreeMap<Long, MemSample> getMemSamples() {
		return memSamples;
	}
	
	public List<GraphSamplePoint> getGraphSamplePoints() {
		if(grapSamplePoints == null){
			grapSamplePoints = calculateGraphSamplePoints();
		}
		return grapSamplePoints;
	}

	@Override
	public void setEnabled(int graphIndex, boolean enableValue) {
		super.setEnabled(graphIndex, enableValue);
		Iterator<MemSample> iterator = memSamples.values().iterator();
		while(iterator.hasNext()){
			MemSample memSample = iterator.next();
			memSample.thread.setEnabled(graphIndex, enableValue);
		}
		
	}

	private List<GraphSamplePoint> calculateGraphSamplePoints() {

		List<GraphSamplePoint> graphSamples = new ArrayList<GraphSamplePoint>();
		Iterator<MemSample> iterator = memSamples.values().iterator();
		while (iterator.hasNext()) {
			MemSample memSample = iterator.next();	
			if(memSample.type == MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK){
				continue;
			}
			if (memSamples.values().size() == 1) {
				if (memSample.stackSize == 0) {
					// library unloaded
					graphSamples.add(new GraphSamplePoint(-100, memSample.sampleSynchTime));
				} else {
					// library loaded
					graphSamples.add(new GraphSamplePoint(memSample.sampleSynchTime, -100));
				}
			} else {
				if (memSample.stackSize == 0) {
					// library unloaded
					graphSamples.add(new GraphSamplePoint(-100, memSample.sampleSynchTime));
				} else {
					// library loaded
					graphSamples.add(new GraphSamplePoint(memSample.sampleSynchTime, -100));
				}
			}

		}
		return graphSamples;
	}

}
