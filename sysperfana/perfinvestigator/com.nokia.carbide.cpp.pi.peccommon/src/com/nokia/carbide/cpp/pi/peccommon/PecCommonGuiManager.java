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

import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * This instance manages the IPC graphs of one editor. This functionality
 * was moved here to avoid putting GUI content into the PecCommonTrace class
 */
public class PecCommonGuiManager {
	/** uid of the editor */
	protected int uid;
	/** the PecCommonTrace instance for this editor */
	protected PecCommonTrace trace;
	/** the IpcTraceGraphs for this editor*/
	protected PecCommonTraceGraph[] graphs;
	/** The title of the graph */
	protected String graphTitle;
	
	
	/**
	 * Constructor
	 * @param uid UId of the editor
	 * @param trace the PecCommonTrace instance for this editor
	 * @param graphCount number of this type of graph for the editor
	 * @param title the graph title
	 */
	public PecCommonGuiManager(int uid, PecCommonTrace trace, int graphCount, String title) {
		super();
		this.uid = uid;
		this.trace = trace;
		this.graphTitle = title;

		graphs = new PecCommonTraceGraph[graphCount];
	}


	/**
	 * Returns the graph generated for this trace
	 * @param graphIndex the index of the graph to get
	 * @param helpContextIdMainPage a help context id
	 * @return the graph
	 */
	public GenericTraceGraph getTraceGraph(int graphIndex, String helpContextIdMainPage) {

		// note that graphIndex need not match the index sent to MemTraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null) {
				graphs[graphIndex] = createTraceGraph(graphIndex, trace, uid, this.graphTitle, helpContextIdMainPage);
			}
			return graphs[graphIndex];
		}
		
		throw new IllegalArgumentException("Graph index out of range."); //$NON-NLS-1$
	}


	/**
	 * Creates the graph. This may be implemented by subclasses.
	 * @param graphIndex Index of the graph
	 * @param aTrace The trace model class
	 * @param aUid the uid of the editor
	 * @param helpContextIdMainPage a help context id
	 * @return the newly created graph
	 */
	protected PecCommonTraceGraph createTraceGraph(int graphIndex,
			PecCommonTrace aTrace, int aUid, String aGraphTitle, String helpContextIdMainPage){
		return new PecCommonTraceGraph(graphIndex, aTrace, aUid, this, aGraphTitle, helpContextIdMainPage);		
	}


	/**
	 * Passes PIEvent.SELECTION_AREA_CHANGED to all available PEC graphs
	 * by calling {@link PecCommonTraceGraph#selectionAreaChanged(double, double)}
	 * @param newStart new start of selected area
	 * @param newEnd new end of selected area
	 */
	void selectionAreaChanged(double newStart, double newEnd) {
		for (PecCommonTraceGraph graph : graphs) {
			if (graph != null){
				graph.selectionAreaChanged(newStart, newEnd);
			}
		}
	}

}
