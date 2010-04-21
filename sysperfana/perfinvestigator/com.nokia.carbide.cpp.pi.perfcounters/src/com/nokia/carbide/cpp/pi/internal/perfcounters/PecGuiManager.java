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

import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonGuiManager;

/**
 * This instance manages the PEC graphs of one editor. This functionality
 * was moved here to avoid putting GUI content into the PecTrace class
 */
public class PecGuiManager extends PecCommonGuiManager {
	
	
	/**
	 * Constructor
	 * @param uid UId of the editor
	 * @param trace the PecTrace instance for this editor
	 * @param graphCount number of graphs for this editor
	 * @param graphTitle the title of the graph
	 */
	public PecGuiManager(int uid, PecTrace trace, int graphCount, String graphTitle) {
		super(uid, trace, graphCount, graphTitle);
	}


	/**
	 * Returns the graph generated for this trace
	 * @param graphIndex the index of the graph to get
	 * @param helpContextIdMainPage a help context id
	 * @return the graph
	 */
	@Override
	public GenericTraceGraph getTraceGraph(int graphIndex, String helpContextIdMainPage) {

		// note that graphIndex need not match the index sent to MemTraceGraph
		if ((graphIndex == PIPageEditor.THREADS_PAGE)
				|| (graphIndex == PIPageEditor.BINARIES_PAGE)
				|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null) {
				graphs[graphIndex] = new PecTraceGraph(graphIndex, (PecTrace)trace, uid, this, this.graphTitle, helpContextIdMainPage);
			}
			return graphs[graphIndex];
		}
		
		throw new IllegalArgumentException("Graph index out of range."); //$NON-NLS-1$
	}


}
