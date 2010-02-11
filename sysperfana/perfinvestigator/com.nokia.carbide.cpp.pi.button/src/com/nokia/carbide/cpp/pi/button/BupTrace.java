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

package com.nokia.carbide.cpp.pi.button;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class BupTrace extends GenericSampledTrace
{
	
	private static final long serialVersionUID = -2398791759386296139L;
	
	private double scale = 0.0;
	private double min = 0.0;
	private double max = 0.0;
	private double synchValue = 0.0;

	private transient BupTraceGraph[] graphs;
	private transient IBupEventMap map;
		
	public GenericTraceGraph getTraceGraph(int graphIndex)
	{
		if (graphs == null) {
			graphs = new BupTraceGraph[3];
			
			BupUpdater.getInstance().convertToLatest(this.samples);
			
			AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
			BupEventMapManager.getInstance().releaseMap(getCurrentBupMapInUse());
			IBupEventMapProfile profile;
			profile = ButtonPlugin.getDefault().getBupMapProfileFromInfoHandler(handler);
			map = BupEventMapManager.getInstance().captureMap(profile);
		}

		if (   (graphIndex == PIPageEditor.THREADS_PAGE)
			|| (graphIndex == PIPageEditor.BINARIES_PAGE)
			|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null)
				graphs[graphIndex] = new BupTraceGraph(graphIndex, this);
			return graphs[graphIndex];
		}
	
		return null;
	}

	public double getScale()
	{
		return scale;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public double getSynchValue()
	{
		return synchValue;
	}

	public void setSynchValue(double aSynchValue)
	{
		synchValue = aSynchValue;
	}

	public void addSample(BupSample sample)
	{
		this.samples.add(sample);
	}

	public BupSample getBupSample(int number)
	{
		return (BupSample)this.samples.elementAt(number);
	}

	/**
	 * @return
	 */
	public IBupEventMap getCurrentBupMapInUse() {
		return map;
	}
	
	public void setCurrentBupMapInUse(IBupEventMap newMap) {
		map = newMap;
	}

}
