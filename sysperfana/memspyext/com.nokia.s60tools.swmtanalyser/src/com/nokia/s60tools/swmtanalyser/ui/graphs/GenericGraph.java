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

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;

public abstract class GenericGraph {

	//
	// Constants
	//
	
	/**
	 * Mega Byte format
	 */
	protected static final DecimalFormat MBformat = new DecimalFormat("#####.0");
	/**
	 * Byte format
	 */
	protected static final DecimalFormat Bytes_Format = new DecimalFormat("#####.##");	
	/**
	 * Label for time in X axis
	 */
	protected static final String TIME_X_AXIS_LABEL = "Time (h:min:s)";	
	
	//
	// Members
	// 
	private EventTypes event;
	private ArrayList<String> userSelectedItems;
	private ArrayList<Color> colors;
	private ParsedData parsedData;
	private double scale = 1.0;
	
	
	protected int maxBytes = 10;	
	protected int[] valuesToBePlotted = null;
	protected int lastSampleTime = 0;
	protected int visualSizeY = 0;
	protected int timeOffset = 0;
	
	/**
	 * Event types to map the event names into corresponding event enumerators.
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GraphsUtils#EVENT_NAMES_ARR
	 */
	public enum EventTypes {GLOBAL_DATA_SIZE, NON_HEAP_CHUNK_SIZE, DISK_USED_SIZE, DISK_TOTAL_SIZE, NO_OF_FILES, MAX_HEAP_SIZE, HEAP_SIZE, HEAP_ALLOC_SPACE, HEAP_FREE_SPACE, HEAP_ALLOC_CELL_COUNT, HEAP_FREE_CELL_COUNT, HEAP_FREE_SLACK, NO_OF_PSHANDLES, RAM_USED, RAM_TOTAL, SYSTEM_DATA};
		
	private static ArrayList<String> graphableKernels = new ArrayList<String>();
	
	/**
	 * Get graphable kernels
	 * @return the graphable kernels
	 */
	public static ArrayList<String> getGraphableKernels() {
		return graphableKernels;
	}

	static{
		graphableKernels.add("Number of Processes");
		graphableKernels.add("Number of Threads");
		graphableKernels.add("Number of Timers");
		graphableKernels.add("Number of Semaphores");
		graphableKernels.add("Number of Servers");
		graphableKernels.add("Number of Sessions");
		graphableKernels.add("Number of Chunks");
		graphableKernels.add("Number of Msg. Queues");
	}
	
	/**
	 * Get cycles data
	 * @return cycle data
	 */
	public ParsedData getCyclesData() {
		return parsedData;
	}

	/**
	 * Set cycles data
	 * @param parsedData
	 */
	public void setCyclesData(ParsedData parsedData) {
		this.parsedData = parsedData;
	}

	/**
	 * Get event
	 * @return event
	 */
	public EventTypes getEvent() {
		return event;
	}

	/**
	 * Set event
	 * @param event
	 */
	public void setEvent(EventTypes event) {
		this.event = event;
	}

	/**
	 * Get items that user has been selected
	 * @return selected items
	 */
	public ArrayList<String> getUserSelectedItems() {
		return userSelectedItems;
	}

	/**
	 * Set items that user has been selected
	 * @param userSelectedItems
	 */
	public void setUserSelectedItems(ArrayList<String> userSelectedItems) {
		this.userSelectedItems = userSelectedItems;
	}

	/**
	 * Draw contents to drawn area
	 * @param graphics
	 */
	public abstract void paint(Graphics graphics);
	/**
	 * Prepare data for drawing
	 */
	public abstract void prepareData();
	/**
	 * Draw headers to Y axis
	 * @param gc
	 */
	public abstract void paintYAxis(GC gc);
	/**
	 * Get tool tip text for coordinates
	 * @param x
	 * @param y
	 * @return tooltip text
	 */
	public abstract String getToolTipText(int x, int y);
		
	/**
	 * Draw lines to background
	 * @param canvas
	 * @param graphics
	 */
	public void drawBackGroundLines(FigureCanvas canvas, Graphics graphics)
	{
		Rectangle canvasRect = graphics.getClip(new org.eclipse.draw2d.geometry.Rectangle());
	  	graphics.setForegroundColor(new Color(Display.getDefault(), new RGB(200, 200, 200)));
	 	graphics.setBackgroundColor(new Color(Display.getDefault(), new RGB(170,170,170)));
		
	 	int height = canvas.getClientArea().height;
	  	int width = canvas.getClientArea().width;
	  	  	
	  	graphics.fillRectangle(new Rectangle(canvasRect.x,0,width, height-50));
	  	
	    double visY = height - CommonGraphConstants.XLEGENDSPACE;
	    			
		int k = 0;
		
		for (float y = 0; k <= 10; y += visY * 10000 / 100001, k++)
		{
			for (int x = canvasRect.x; x <= canvasRect.x + canvasRect.width; x += 5)
			{
				if ((x / 5) % 2 == 0) graphics.drawLine(x, ((int)y) + 1, x + 5, ((int)y) + 1);
			}
		}
		
		graphics.setForegroundColor(new Color(Display.getDefault(), new RGB(100, 100, 100)));
		graphics.setBackgroundColor(new Color(Display.getDefault(),new RGB(255, 255, 255)));
		
		// horizontal lines
		if (width > 0)
		{
			for (int x = 0; x <= canvasRect.x + canvasRect.width; x += 50)
			{	
				if (x % 100 == 0)
					graphics.setForegroundColor(new Color(Display.getDefault(), new RGB(100, 100, 100)));
				else
					graphics.setForegroundColor(new Color(Display.getDefault(),new RGB(200, 200, 200)));
				
				for (int y = 0; y < height; y += 5)
				{
					if ((y / 5) % 2 == 0)
						graphics.drawLine(x, y, x, y + 5);
				}
			}
		}
		
		graphics.setForegroundColor(new Color(Display.getDefault(), new RGB(100, 100, 100)));
		graphics.setBackgroundColor(new Color(Display.getDefault(),new RGB(255, 255, 255)));
		
		for (int x = 0; x <= canvasRect.x + canvasRect.width; x += 50)
		{
			double time = (double) x;
			TimeObject timeObj = new TimeObject(time, scale);
			graphics.drawString(timeObj.getHourMinutesAndSeconds(), x + 5, height - 13);
			if(timeObj.hasDays()){
				graphics.drawString(timeObj.getDays(), x + 5, height - 26);
			}
		}
		Image img = getVerticalLabel(TIME_X_AXIS_LABEL);
		graphics.drawImage(img, width/2, height-30);
	}

	/**
	 * Called when horizontal bar is moved in graphs, to set X location of current selection.
	 * @param x X-location of {@link org.eclipse.draw2d.geometry.Point}
	 */
	public void setScrolledXOrigin(int x)
	{
		this.timeOffset = x;
	}

	/**
	 * Get cycle times from parsed data
	 * @return list of times
	 */
	protected int [] calculateTimeIntervals()
	{
		int [] time = new int[parsedData.getNumberOfCycles()];
		int prevDuration = 0;
		time[0] = 0;
				
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		CycleData [] cycles = parsedData.getLogData();
		for(int i=1; i<parsedData.getNumberOfCycles();i++)
		{
			String currentTime = cycles[i].getTime();
			String prevTime = cycles[i-1].getTime();
			int timeDiff = (int)utils.getDurationInSeconds(prevTime, currentTime);
						
			if(timeDiff < 0)
			{
				//error condition
			}
			else
			{
				timeDiff += prevDuration;
				prevDuration = timeDiff;
			
				time[i] = timeDiff;
			}
			
		}
		
		return time;
	}
	
	/**
	 * Get largest value from given items
	 * @param values
	 * @return largest value found
	 */
	protected int calculateMaxValue(int [] values)
	{
		int maxValue = 0;
		
		for(int i=0; i<values.length; i++)
		{
			if(values[i] > maxValue)
				maxValue = values[i];
		}
		
		return maxValue;
	}

	/**
	 * Get colors
	 * @return colors
	 */
	public ArrayList<Color> getColors() {
		return colors;
	}

	/**
	 * Set colors
	 * @param colors
	 */
	public void setColors(ArrayList<Color> colors) {
		this.colors = colors;
	}
		
	/**
	 * Get label for vertical axis
	 * @param name
	 * @return label
	 */
	protected Image getVerticalLabel(String name)
	{
		return GraphsUtils.getVerticalLabel(name, 110, 15, 9);
	}
	
	/**
	 * Get scale
	 * @return scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Set scale
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	/**
	 * Set visual size
	 * @param height
	 */
	public void setVisualSize(int height)
	{
		this.visualSizeY = height;
	}

	/**
	 * Get count for wanted item
	 * @param item
	 * @param kernelsList
	 * @return count of items wanted
	 */
	protected int [] getValuesForGivenKerenelElement(String item, ArrayList<KernelElements> kernelsList)
	{
		int [] values = new int[kernelsList.size()];

		int index = graphableKernels.indexOf(item);		
		
		for(int i=0; i<kernelsList.size(); i++)
		{
			KernelElements kernels = kernelsList.get(i);
			
			if(index == 0)
				values [i] = kernels.getNumberOfProcesses();
			else if(index == 1)
				values[i] = kernels.getNumberOfThreads();
			else if(index == 2)
				values [i] = kernels.getNumberOfTimers();
			else if(index == 3)
				values [i] = kernels.getNumberOfSemaphores();
			else if(index == 4)
				values [i] = kernels.getNumberOfServers();
			else if(index == 5)
				values [i] = kernels.getNumberOfSessions();
			else if(index == 6)
				values [i] = kernels.getNumberOfChunks();
			else if(index == 7)
				values [i] = kernels.getNumberOfMsgQueues();
	
		}
			
		return values;
		
	}
}
