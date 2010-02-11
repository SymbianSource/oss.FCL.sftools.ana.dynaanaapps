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

package com.nokia.carbide.cpp.internal.pi.visual;

import java.util.ArrayList;
import java.util.Enumeration;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;


public class GraphDrawRequest 
{
	/*
	 * GraphDrawRequest contains data on how the graph should be drawn
	 * to the composite panel
	 * 
	 * Example: 
	 * draw always ON top of GPP and IRQ traces
	 * 
	 * GraphDrawRequest request = new GraphDrawRequest()
	 * request.addParentGraph(0, "com.nokia.carbide.cpp.pi.address.AddressPlugin", DRAW_TO_ALL_AVAILABLE, 0);
	 * request.addParentGraph(0, "com.nokia.carbide.cpp.pi.irq.IrqPlugin", DRAW_TO_ALL_AVAILABLE, 0);
	 * 
	 * Example:
	 * draw always on MEM trace, plus additionally, primarily on 
	 * GPP trace and secondarily on IRQ trace, but not both
	 * 
	 * GraphDrawRequest request = new GraphDrawRequest()
 	 * request.addParentGraph(0, "com.nokia.carbide.cpp.pi.memory.MemPlugin", DRAW_TO_ALL_AVAILABLE, 0);
	 * request.addParentGraph(0, "com.nokia.carbide.cpp.pi.address.AddressPlugin", DRAW_TO_MOST_IMPORTANT_AVAILABLE, 1);
	 * request.addParentGraph(0, "com.nokia.carbide.cpp.pi.irq.IrqPlugin", DRAW_TO_MOST_IMPORTANT_AVAILABLE, 0);
	 * 
	 */
	
	public static final int DRAW_TO_ALL_AVAILABLE = 1;
	public static final int DRAW_TO_MOST_IMPORTANT_AVAILABLE = 2;
	
	private ArrayList<GraphEntry> internalData;
	
	public GraphDrawRequest()
	{
		this.internalData = new ArrayList<GraphEntry>();
	}
	
	private static class GraphEntry
	{
		protected int editorPage;
		protected String pluginName;
		protected int mode;
		protected int importance;
	}
	
	public void addParentGraph(int editorPage, String pluginName, int mode, int importance)
	{
		GraphEntry ge = new GraphEntry();
		ge.editorPage = editorPage;
		ge.pluginName = pluginName;
		ge.mode = mode;
		ge.importance = importance;
		this.internalData.add(ge);
	}
	
	protected ArrayList getGraphClassToDraw(int editorPage)
	{
		ArrayList<String> finalResult = new ArrayList<String>();
		
		int currentMostImportantImportance  = Integer.MIN_VALUE;
		String currentMostImportantName = null;
		
		for (int i = 0; i < internalData.size(); i++)
		{
			GraphEntry ge = (GraphEntry)internalData.get(i);
			if (ge.editorPage == editorPage)
			{
				if (ge.mode == GraphDrawRequest.DRAW_TO_ALL_AVAILABLE)
				{
					Enumeration plugins = 
						PluginInitialiser.getPluginInstances(
								NpiInstanceRepository.getInstance().activeUid(),"com.nokia.carbide.cpp.internal.pi.plugin.model.Plugin"); //$NON-NLS-1$
					
					while (plugins.hasMoreElements()) 
					{
						Object o = plugins.nextElement();
						if (o.getClass().getName().equals(ge.pluginName))
						{
							finalResult.add(ge.pluginName);
						}
					}
				}
				
				if (ge.mode == GraphDrawRequest.DRAW_TO_MOST_IMPORTANT_AVAILABLE)
				{
					if (ge.importance > currentMostImportantImportance)
					{
						currentMostImportantName = ge.pluginName;
						currentMostImportantImportance = ge.importance;
					}
				}
			}
		}

		if (currentMostImportantName != null)
		{
			finalResult.add(currentMostImportantName);
		}
		
		return finalResult;
	}
}
