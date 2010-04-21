/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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
package com.nokia.s60tools.swmtanalyser.ui.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * This class contains all needed logic to paint data related to Threads. 
 */
public class ThreadsGraph extends GenericGraph {

	//
	// Members
	// 
	private HashMap<String, ArrayList<ThreadData>> threadData = new HashMap<String, ArrayList<ThreadData>>();	
	private HashMap<String, Polyline> samplesData = new HashMap<String, Polyline>();
	private double visY;
	private double multiplier;
	private boolean yAxisNeedsToBeChanged = false;
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paint(org.eclipse.draw2d.Graphics)
	 */
	public void paint(Graphics graphics) {

		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint START");

		// Getting threads that user has been selected
		ArrayList<String> threadsList = this.getUserSelectedItems();
		
		 if(threadsList == null){
			 // No thread data selected for drawing
			 return;			 
		 }
		
		// Storing original settings before graphs are painted with case-specific settings
		int origLineWidth = graphics.getLineWidth();		
		Color origColor = graphics.getForegroundColor();
		int origLineStyle = graphics.getLineStyle();
		
		// Setting graph drawing specific settings
		graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);		
		graphics.setLineStyle(SWT.LINE_SOLID);
		
		 // Getting cycle time stamps		
		 int [] listX = this.calculateTimeIntervals();
		 this.lastSampleTime = listX[listX.length-1];
		 
		// Each thread is drawn by different color stored in external array
		 int colorIndex=0;  
		 visY  = visualSizeY - CommonGraphConstants.XLEGENDSPACE; 
		 
		 // Looping through all the threads
		 for(String th: threadsList)
		 { 
			 ArrayList<ThreadData> data = threadData.get(th);
				 
			 boolean handleDeleted = false;
			 
			 int[] valuesToBePlotted = new int[data.size()];
			 int [] points = new int[valuesToBePlotted.length *2];
			
			 List<List<Integer>> ListOfSolidLinePoints = new ArrayList<List<Integer>>();
			 ArrayList<Integer> solidLinePoints = new ArrayList<Integer>();
			 
			 for(int i =0, j=0; i<data.size(); i++, j++)
			 {
				 EventTypes event = this.getEvent();
				
				 valuesToBePlotted[i] = getEventValueFromThreadData(data.get(i), event);
				 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "valuesToBePlotted[i] before scaling: " + valuesToBePlotted[i]);

				if (valuesToBePlotted[i] <= 0){
					// Not showing zero values to a user, not meaningful data 
					DbgUtility.println(DbgUtility.PRIORITY_LOOP, "continued to next Y-value because value was <= 0");
					continue;
				}
				 
				 // Scaling both X and Y coordinate according currently used scaling
				 int x_point = (int)(listX[i]/getScale());			    	
				 int y_point =(int) (visY - valuesToBePlotted[i] /multiplier);

				 // Drawing data only if handle has been
				 if(!handleDeleted){
					 if(y_point > 0){
						 solidLinePoints.add(x_point);
						 solidLinePoints.add(y_point);	
						 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "add 1: x_point: " + x_point + ", y_point: " + y_point);
					 }
					 else{
						    DbgUtility.println(DbgUtility.PRIORITY_LOOP, "skipped because non-positive Y-axis value");
					 }
				 }
				 
				 if(data.get(i).isKernelHandleDeleted() && !handleDeleted){
					 handleDeleted = true;
				 }
				 
				 if(handleDeleted && data.get(i).getStatus() == CycleData.New)
				 {
					 handleDeleted = false;
					 
					 if(y_point > 0){
						 // Graphing only positive values
						 solidLinePoints.add(x_point);
						 solidLinePoints.add(y_point);					 
						 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "add 2:x_point: " + x_point + ", y_point: " + y_point);
					 }
					 else{
					    DbgUtility.println(DbgUtility.PRIORITY_LOOP, "skipped because zero value");
					 }					 
				 }
				 
				 points[j] = x_point;
				 points[++j] = y_point;
			 }	
			 
			 if(solidLinePoints.size() > 0){
				 // Adding point for this thread graph, possible to have more instances for same thread name
				 ListOfSolidLinePoints.add(solidLinePoints);				 
			 }
			 
			visY  = visualSizeY - CommonGraphConstants.XLEGENDSPACE;
							
			// Each thread have a separate color
			graphics.setForegroundColor(this.getColors().get(colorIndex));
						
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "No of solid lists are " + ListOfSolidLinePoints.size());					
				
			for(int i=0; i < ListOfSolidLinePoints.size(); i++)
			{
				int [] solidPts = GraphsUtils.CreateIntArrayFromIntegerList(ListOfSolidLinePoints.get(i));
					
				if(solidPts != null)
				{
					if(ListOfSolidLinePoints.size() > 1)
					{
						int instance_id = i+1;
						graphics.drawString("(0" + instance_id + ")", solidPts[0]+2, solidPts[1] - 15);
					}
					
					// Drawing graph based on the stored data points
					graphics.drawPolyline(solidPts);
					
					// Drawing markers to the data points
					GraphsUtils.drawMarkers(graphics, solidPts);
				}
			}
				
			Polyline line = new Polyline();
			line.setPoints(new PointList(points));
		
			DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Putting the points list in a map for '" + th + "'.");
			samplesData.put(th, line);
			 
			colorIndex++;
		}
		 
		// Restoring original settings before paint call
		graphics.setLineStyle(origLineStyle);
		graphics.setForegroundColor(origColor);
		graphics.setLineWidth(origLineWidth);

		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint END");
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paintYAxis(org.eclipse.swt.graphics.GC)
	 */
	public void paintYAxis(GC gc)
	{	
		double visY  = visualSizeY - CommonGraphConstants.XLEGENDSPACE; 
		
		switch(this.getEvent())
		{
			case NO_OF_FILES:
			case HEAP_ALLOC_CELL_COUNT:
			case HEAP_FREE_CELL_COUNT:
			case NO_OF_PSHANDLES:
				multiplier = GraphsUtils.roundToNearestNumber(maxBytes) / visY;
				yAxisNeedsToBeChanged = true;
				break;
			default:
					multiplier = GraphsUtils.prettyMaxBytes(maxBytes) / visY;
				break;
		}
		int countOfYAxisLabels = 10;
		double yIncrement = visY / countOfYAxisLabels;
		int previousBottom = 0;
		
		for (int k = countOfYAxisLabels; k >= 0; k--)
		{
			// location for the value indicator is k * 1/10 the height of the display
			int y = (int) (visY - (yIncrement * k));
		
			int bytes = (int)(yIncrement * multiplier) * k;

			String legend = "";
			
			switch(this.getEvent())
			{
				case NO_OF_FILES:
				case HEAP_ALLOC_CELL_COUNT:
				case HEAP_FREE_CELL_COUNT:
				case NO_OF_PSHANDLES:
					legend += bytes;
					break;
				default:
					//legend += bytes + "B";
					if (maxBytes < 10000)
					{
						legend += bytes + " B"; //$NON-NLS-1$
					}
					else if (maxBytes <= 500 * 1024)
					{
						legend += (bytes / 1024) + " KB"; //$NON-NLS-1$
					}
					else
					{
						legend +=  MBformat.format(((float) bytes / (1024 * 1024)))  + " MB"; //$NON-NLS-1$
					}
					break;
			}
			
			Point extent = gc.stringExtent(legend);
			
			gc.drawLine(CommonGraphConstants.YLEGENDSPACE - 3, (int)y + 1, CommonGraphConstants.YLEGENDSPACE, (int)y + 1);
			
			if (y >= previousBottom)
			{
				gc.drawString(legend, CommonGraphConstants.YLEGENDSPACE - extent.x -2, (int)y);
				previousBottom = (int)y + extent.y;
			}
		}
		
		if(yAxisNeedsToBeChanged)
			drawCountLabel(gc, (int)(visY/3));
		else
			drawBytesLabel(gc, (int)(visY/3));
	}

	private void fetchEntireDataForSelectedThreads()
	{
		ArrayList<String> selectedThreads = this.getUserSelectedItems();
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		for(String th:selectedThreads)
		{
			ArrayList<ThreadData> thData = utils.getHeapDataFromAllCycles(th, this.getCyclesData());
			threadData.put(th, thData);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#prepareData()
	 */
	public void prepareData()
	{
		fetchEntireDataForSelectedThreads();
		
		 for(String th:getUserSelectedItems())
		 {
			 ArrayList<ThreadData> data = threadData.get(th);
			 
			 valuesToBePlotted = new int[data.size()];
			 for(int i =0; i<data.size(); i++)
			 {
				 EventTypes event = this.getEvent();
				 valuesToBePlotted[i] = getEventValueFromThreadData(data.get(i), event);
				 int maxValue = calculateMaxValue(valuesToBePlotted);
				 if(maxValue > maxBytes)
					 maxBytes = maxValue;
			 }
			 
		 }
		
	}
	
	private int getEventValueFromThreadData(ThreadData thData, EventTypes event)
	{
		int value = 0;
	
		if(thData.getStatus() == CycleData.Deleted)
			return 0;
		
		switch(event)
		{
			case NO_OF_FILES:
				value = (int)thData.getOpenFiles();
				break;
			case MAX_HEAP_SIZE:
				value = (int)thData.getMaxHeapSize();
				break;
			case HEAP_SIZE:
				value = (int)thData.getHeapChunkSize();
				break;
			case HEAP_ALLOC_SPACE:
				value = (int)thData.getHeapAllocatedSpace();
				break;
			case HEAP_FREE_SPACE:
				value = (int)thData.getHeapFreeSpace();
				break;
			case HEAP_ALLOC_CELL_COUNT:
				value = (int)thData.getAllocatedCells();
				break;
			case HEAP_FREE_CELL_COUNT:
				value = (int)thData.getFreeCells();
				break;
			case HEAP_FREE_SLACK:
				value = (int)thData.getFreeSlackSize();
				break;
			case NO_OF_PSHANDLES:
				value = (int)thData.getPsHandles();
				break;
			default:
				value = 0;
				break;
		}
		
		return value;
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#getToolTipText(int, int)
	 */
	public String getToolTipText(int x, int y)
	{
		if(y > (int)visY)
			return null;
		
		String text = "";
		
		double xValue = x + timeOffset;
		int scaledX = (int)(xValue * getScale());
		
		double valY = visY - y;
		double scaledY =  valY * multiplier;			
		
		String yValue = "";
				
		yValue+= (int)(scaledY);
			
		switch(this.getEvent())
		{
			case NO_OF_FILES:
			case NO_OF_PSHANDLES:
			case HEAP_ALLOC_CELL_COUNT:
			case HEAP_FREE_CELL_COUNT:
				text += scaledX + " s, " + yValue;
				break;
			default:
				yValue = getFormattedValues(scaledY);
				text += scaledX + " s, " + yValue;
				break;
		}
		
		for(String th: getUserSelectedItems())
		{
			Polyline line = samplesData.get(th);
			if(line != null && line.containsPoint(x, y))
				text += "\n" + th;
		
		}
				
		return text; 
	}
	
	/**
	 * Get thread data for thread
	 * @param thread
	 * @return thread data
	 */
	public ArrayList<ThreadData> getDataForThread(String thread)
	{
		return this.threadData.get(thread);
	}
	
	/**
	 * Draws Bytes label on given gc at given position
	 * @param gc
	 * @param position
	 */
	private void drawBytesLabel(GC gc, int position)
	{
		final Image image = this.getVerticalLabel("Bytes");
		gc.setAdvanced(true);
		final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
		Transform transform = new Transform(Display.getDefault());

		transform.translate(rect2.height / 2f, rect2.width / 2f);
		transform.rotate(-90);
		transform.translate(-rect2.width / 2f, -rect2.height / 2f);

		gc.setTransform(transform);
		gc.drawImage(image, -position, 0);

		transform.dispose();
		gc.dispose();				

	}
	
	/**
	 * Draws Count label on given gc at given position
	 * @param gc
	 * @param position
	 */
	private void drawCountLabel(GC gc, int position)
	{
		final Image image = this.getVerticalLabel("Count");
		gc.setAdvanced(true);
		final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
		Transform transform = new Transform(Display.getDefault());

		transform.translate(rect2.height / 2f, rect2.width / 2f);
		transform.rotate(-90);
		transform.translate(-rect2.width / 2f, -rect2.height / 2f);

		gc.setTransform(transform);
		gc.drawImage(image, -position, 0);

		transform.dispose();
		gc.dispose();				

	}
	
	private String getFormattedValues(double bytes)
	{
		String scaledY = "";
		
		if (bytes < 10000)
		{
			scaledY += Bytes_Format.format(bytes) + " B"; //$NON-NLS-1$
		}
		else if (bytes <= 500 * 1024)
		{
			scaledY += Bytes_Format.format(bytes / 1024) + " KB"; //$NON-NLS-1$
		}
		else
		{
			scaledY +=  MBformat.format(((float) bytes / (1024 * 1024)))  + " MB"; //$NON-NLS-1$
		}
		
		return scaledY;
	}
}
