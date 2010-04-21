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

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;


public interface IVisualizable extends ITrace
{
	// return whether this plugin's editor pages have been created
	public boolean arePagesCreated();
	
	// set whether this plugin's editor pages have been created
	public void setPagesCreated(boolean pagesCreated);
	
	// the number of editor pages that this plugin will create
	public int getCreatePageCount();
	
	// the index of each page that this plugin will create
	// return AnalyseTab.NEXT_AVAILABLE_PAGE if you do not care
	// if a page with that number has already been created, this page will replace it
	public int getCreatePageIndex(int index);
	
	// page index actually assigned
	// (usually for a page index of AnalyseTab.NEXT_AVAILABLE_PAGE)
	public void setPageIndex(int index, int pageIndex);

	// the next created page
	public ProfileVisualiser createPage(int index);

	// the number of graphs that this plugin will create
	public int getGraphCount();
	
	// next graph
	public IGenericTraceGraph getTraceGraph(int graphIndex);
	
	// PI editor page to contain the graph
	public int getPageNumber(int graphIndex);
	
	// time (in milliseconds) associated with the last sample
	public Integer getLastSample(int graphIndex);
	
	/*
	 * returns draw request if a graph must be drawn after/on top of another graph
	 * (e.g. Button trace within CPU Load trace); otherwise, returns null
	 */
	public GraphDrawRequest getDrawRequest(int graphIndex);
}
