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

package com.nokia.carbide.cpp.pi.internal.perfcounters;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegend;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonTraceGraph;
import com.nokia.carbide.cpp.pi.perfcounters.PecPlugin;

/**
 * The performance counter graph class, responsible for drawing the several graphs
 * based on performance counters as well as managing the legend views to some extend
 */
public class PecTraceGraph extends PecCommonTraceGraph{

	/**
	 * Constructor
	 * @param graphIndex the index of the graph (corresponds to the page in the editor)
	 * @param pecTrace the trace class
	 * @param uid the uid to identify the current editor
	 * @param guiManager PecGuiManager which manages all graphs
	 * @param title Title of graph
	 * @param helpContextIdMainPage 
	 */
	public PecTraceGraph(int graphIndex, PecTrace pecTrace, int uid, PecGuiManager guiManager, String title, String helpContextIdMainPage) {
		super(graphIndex, pecTrace, uid, guiManager, title, helpContextIdMainPage);
	}
	
	@Override
	protected PecCommonLegend createLegend(Composite bottomComposite) {		
		return new PecLegend(this, bottomComposite, getTitle(), (PecTrace)trace);
	}

}
