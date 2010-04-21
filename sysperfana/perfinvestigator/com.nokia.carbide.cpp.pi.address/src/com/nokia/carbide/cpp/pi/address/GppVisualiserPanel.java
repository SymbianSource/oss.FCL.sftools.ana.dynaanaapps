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

/*
 * GppVisualiserPanel.java
 */
package com.nokia.carbide.cpp.pi.address;

import java.util.Vector;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.core.SessionPreferences;


public class GppVisualiserPanel implements PIEventListener 
{
	private static final long serialVersionUID = 8984025640418406715L;
	public GppTraceGraph myGppGraph;
	public GppVisualiserPanel myGppVisualiser;

	public GppVisualiserPanel(GppTraceGraph myGppGraphIn)
	{  	
		this.myGppGraph = myGppGraphIn;
	      
	     //sets the percentage scale for y-coordinates
	    this.myGppGraph.setScale(0,100);
	    this.myGppGraph.setFillFlag(SessionPreferences.getInstance().getFillAllEnabled());
	    
	  	this.myGppVisualiser = myGppGraphIn.getVisualiserPanel();
	} //constructor
  
	public void refreshCumulativeThreadTable()
	{	
		this.myGppGraph.genericRefreshCumulativeThreadTable();
	}

	public void piEventReceived(PIEvent be)
	{
		switch (be.getType())
		{
			case PIEvent.SET_FILL_ALL_THREADS:
				this.myGppGraph.setFillSelected(false);
				this.myGppGraph.setFillFlag(true);
				this.myGppGraph.repaint();
				break;
			
			case PIEvent.SET_FILL_OFF:
				this.myGppGraph.setFillSelected(false);
				this.myGppGraph.setFillFlag(false);
				this.myGppGraph.repaint();
				break;
			
			case PIEvent.SET_FILL_SELECTED_THREAD:
				this.myGppGraph.setFillSelected(true);
				this.myGppGraph.setFillFlag(true);
				this.myGppGraph.repaint();
				break;
				
			case PIEvent.GPP_SET_BAR_GRAPH_ON:
				this.myGppGraph.barMode = GppTraceGraph.BAR_MODE_ON;
				this.myGppGraph.repaint();
				break;
	
			case PIEvent.GPP_SET_BAR_GRAPH_OFF:
				this.myGppGraph.barMode = GppTraceGraph.BAR_MODE_OFF;
				this.myGppGraph.repaint();
				break;
				
			case PIEvent.CHANGED_THREAD_TABLE:
				this.myGppGraph.setGraphImageChanged(true);
				if (this.myGppGraph.isFillFlag() && this.myGppGraph.isFillSelected())
				{
				    this.myGppGraph.repaint();
				}
				break;
				
			case PIEvent.CHANGED_BINARY_TABLE:
				this.myGppGraph.setGraphImageChanged(true);
			    if (this.myGppGraph.isFillFlag())
			    {
			        this.myGppGraph.repaint();
			    }
			    break;
				
			case PIEvent.CHANGED_FUNCTION_TABLE:
				this.myGppGraph.setGraphImageChanged(true);
			    if (this.myGppGraph.isFillFlag())
			    {
			        this.myGppGraph.repaint();
			    }
			    break;
				
			default:
				break;
		}
	}
  

	// draw the graph based on the rightmost table displayed
  	private void drawCumulativeGraphLines(Graphics graphics)
	{	
		Vector<ProfiledGeneric> v = this.myGppGraph.getSortedProfiledsForGraph();
		
		if (v == null) 
		    return;

		this.myGppGraph.drawGraphsGeneric(v, graphics, myGppGraph.getTableUtils().getSelectedValues());	  	
	}
	
	private void drawBarGraph(Graphics graphics)
	{
		Vector<ProfiledGeneric> v = this.myGppGraph.getSortedProfiledsForGraph();
		
		if (v == null) 
		    return;
	    
		this.myGppGraph.drawBarsGpp(v, graphics, myGppGraph.getTableUtils().getSelectedValues());	  			
	}

	//unused?
//	//updates values in current profiled generic items
//	public void updateThreadAverages()
//	{
//	    //System.out.println("Refreshing average values");
//		Enumeration enumer = null;
//		int drawMode   = this.myGppGraph.getDrawMode();
//		int graphIndex = this.myGppGraph.getGraphIndex();
//
//		if (   (drawMode == Defines.THREADS)
//				|| (drawMode == Defines.BINARIES_THREADS)
//				|| (drawMode == Defines.BINARIES_FUNCTIONS_THREADS)
//				|| (drawMode == Defines.FUNCTIONS_THREADS)
//				|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS))
//		{
//		    enumer = ((GppTrace) (this.myGppGraph.getTrace())).getSortedThreadsElements();
//		}
//		else if ((drawMode == Defines.BINARIES)
//			  || (drawMode == Defines.THREADS_BINARIES)
//			  || (drawMode == Defines.THREADS_FUNCTIONS_BINARIES)
//			  || (drawMode == Defines.FUNCTIONS_BINARIES)
//			  || (drawMode == Defines.FUNCTIONS_THREADS_BINARIES))
//		{
//		    enumer = ((GppTrace) (this.myGppGraph.getTrace())).getSortedBinariesElements();
//		}
//		else if ((drawMode == Defines.FUNCTIONS)
//			  || (drawMode == Defines.THREADS_FUNCTIONS)
//			  || (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
//			  || (drawMode == Defines.BINARIES_FUNCTIONS)
//			  || (drawMode == Defines.BINARIES_THREADS_FUNCTIONS))
//		{
//		    enumer = ((GppTrace) (this.myGppGraph.getTrace())).getSortedFunctionsElements();
//		}
//
//		if (enumer == null) 
//		    return;
//
//	  	// update the average load strings
//		if (   (myGppGraph.getSelectionStart() == -1)
//			|| (myGppGraph.getSelectionEnd()   == -1))
//		{
//		  	while(enumer.hasMoreElements())
//		  		((ProfiledGeneric) enumer.nextElement()).
//		  				setAverageLoadValueString(graphIndex, "  0.000"); //$NON-NLS-1$
//		} else {
//		  	while(enumer.hasMoreElements())
//		  	{
//		  	    updateThreadAverage(graphIndex, (ProfiledGeneric) enumer.nextElement());
//		  	}
//		}
//	}
//	
//	private void updateThreadAverage(int graphIndex, ProfiledGeneric pg)
//	{
//        float averageLoad = pg.getAverageLoad(
//				myGppGraph.getSelectionStart(),
//				myGppGraph.getSelectionEnd());
//			
//		if (averageLoad >= 0.001)
//		{
//			pg.setAverageLoadValueString(graphIndex, averageLoad);
//		}
//	  	else
//	  	{
//			pg.setAverageLoadValueString(graphIndex, "  0.000"); //$NON-NLS-1$
//	  	}
//	}
  
	public void paintComponent(Panel panel, Graphics graphics)
	{		
		// lay down the cumulative graphs first, then grid, and last with selection
		// so the "effects" shows on the top layer
		if (this.myGppGraph.barMode == GppTraceGraph.BAR_MODE_OFF)
		{
			this.drawCumulativeGraphLines(graphics);
		}
		else
		{
			this.drawBarGraph(graphics);
		}
		
		this.myGppGraph.drawDottedLineBackground(graphics, GppTraceGraph.X_LEGEND_HEIGHT);
		this.myGppGraph.drawSelectionSection(graphics, GppTraceGraph.X_LEGEND_HEIGHT);
	}
 
} //class VisualiserPanel
