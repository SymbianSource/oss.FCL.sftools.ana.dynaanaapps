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

import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * This class contains all needed logic to paint data related to Disks.
 */
public class DisksGraph extends GenericGraph {

	private HashMap<String, ArrayList<DiskOverview>> totalDiskData = new HashMap<String, ArrayList<DiskOverview>>();
	private HashMap<String, Polyline> pointsData = new HashMap<String, Polyline>();
	
	private double visY;
	private double multiplier;
	
	private int [] Ram_Used_Values;
	private int [] Ram_Total_Values;
	private ArrayList<SystemData> sysData;
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paint(org.eclipse.draw2d.Graphics)
	 */
	public void paint(Graphics graphics) {
	
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint START");

		 // Storing original graphics settings
		 Color origColor = graphics.getForegroundColor();
		 int origLineWidth = graphics.getLineWidth();
		 		
		int [] listX = this.calculateTimeIntervals();
	
		this.lastSampleTime = listX[listX.length-1];
		
		if(this.getEvent().equals(EventTypes.RAM_USED) || this.getEvent().equals(EventTypes.RAM_TOTAL))
		{
			if(this.getEvent().equals(EventTypes.RAM_USED))
			{
				valuesToBePlotted = Ram_Used_Values;
			}
			else if(this.getEvent().equals(EventTypes.RAM_TOTAL))
			{
				valuesToBePlotted = Ram_Total_Values;
			}	
			
			if(valuesToBePlotted == null)
				return;
			
			int [] points = new int[valuesToBePlotted.length *2];
			 
			double visY = visualSizeY - CommonGraphConstants.XLEGENDSPACE;
			 
			 for (int i = 0, j = 0; i < valuesToBePlotted.length; i++)
			 {
		    	points[j++] = (int)(listX[i]/getScale());
		    	
		    	points[j] =(int) (visY - valuesToBePlotted[i] /multiplier);
		    	
		    	if (points[j] < 0)
		    		points[j] = 0;
		    	
		    	j++;
			 }
			 
			 graphics.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
			 graphics.drawPolyline(points);
			 // Drawing markers to the data points
			 GraphsUtils.drawMarkers(graphics, points);
			 
			 Polyline line = new Polyline();
			 line.setPoints(new PointList(points));
			 
			 if(this.getEvent().equals(EventTypes.RAM_USED))
			 {
				 pointsData.put("RAM Used", line);
			 }
			 else if(this.getEvent().equals(EventTypes.RAM_TOTAL))
			 {
				 pointsData.put("RAM Total", line);
			 }
			 		
		}
		else{			
			ArrayList<String> disksList = this.getUserSelectedItems();
		
			 if(disksList == null)
				 return;
				 		 
			 int k=0;
		 
			for(String disk: disksList){
			 
			 ArrayList<DiskOverview> data = totalDiskData.get(disk);
				 
			 valuesToBePlotted = new int[data.size()];
			
			 for(int i =0; i<data.size(); i++)
			 {
				 EventTypes event = this.getEvent();
				
				 valuesToBePlotted[i] = getEventValueFromDisksData(data.get(i), event);
				 
			 }	
			 int [] points = new int[valuesToBePlotted.length *2];
			 
			 double visY = visualSizeY - CommonGraphConstants.XLEGENDSPACE;
			 
			 for (int i = 0, j = 0; i < valuesToBePlotted.length; i++)
			 {
			    	points[j++] = (int)(listX[i]/getScale());
			    	
			    	points[j] =(int) (visY - valuesToBePlotted[i] /multiplier);
			    	
			    	if (points[j] < 0)
			    		points[j] = 0;
			    	
			    	j++;
			 }
			 
			 // Setting graph-specific settings
			 graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);			 
			 graphics.setForegroundColor(this.getColors().get(k));
			 graphics.drawPolyline(points);
			 // Drawing markers to the data points
			 GraphsUtils.drawMarkers(graphics, points);
			 
			 Polyline line = new Polyline();
			 line.setPoints(new PointList(points));
			 pointsData.put(disk, line);
			 
			 k++;
			 
			} // for
		} // else

		 // Restoring original graphics settings
		 graphics.setForegroundColor(origColor);
		 graphics.setLineWidth(origLineWidth);
		 
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint END");
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paintYAxispaintYAxis(org.eclipse.swt.graphics.GC)
	 */
	@Override
	public void paintYAxis( GC gc) {
		
		visY = visualSizeY - CommonGraphConstants.XLEGENDSPACE; 
		multiplier = GraphsUtils.prettyMaxBytes(maxBytes) / visY;
		
		double yIncrement = visY / 10;
		int previousBottom = 0;
		
		for (int k = 10; k >= 0; k--)
		{
			// location for the value indicator is k * 1/10 the height of the display
			int y = (int) (visY - (yIncrement * k));
		
			int bytes = (int)(yIncrement * multiplier) * k;

			String legend = "";
			
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
			Point extent = gc.stringExtent(legend);
			
			gc.drawLine(CommonGraphConstants.YLEGENDSPACE - 3, (int)y + 1, CommonGraphConstants.YLEGENDSPACE, (int)y + 1);
			
			if (y >= previousBottom)
			{
				gc.drawString(legend, CommonGraphConstants.YLEGENDSPACE - extent.x -2, (int)y);
				previousBottom = (int)y + extent.y;
			}
		}
	
		final Image image = this.getVerticalLabel("Bytes");
		gc.setAdvanced(true);
	    final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
        Transform transform = new Transform(Display.getDefault());

        transform.translate(rect2.height / 2f, rect2.width / 2f);
        transform.rotate(-90);
        transform.translate(-rect2.width / 2f, -rect2.height / 2f);

        gc.setTransform(transform);
        gc.drawImage(image, -(int)visY/3, 1);
        
        transform.dispose();
        gc.dispose();				

	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#prepareData()
	 */
	public void prepareData() {
		
		EventTypes event = this.getEvent();
		if(this.getEvent().equals(EventTypes.RAM_USED) || this.getEvent().equals(EventTypes.RAM_TOTAL))
		{
			SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
			
			sysData = utils.getSystemDataFromAllCycles(this.getCyclesData());
			int [] eventVals;
			
			switch(event)
			{
				case RAM_USED:
					eventVals = getRamUsedMemFromSysData(sysData);
					break;
				case RAM_TOTAL:
					eventVals = getRamTotalMemFromSysData(sysData);
					break;
				default:
						eventVals = new int[1];
						break;
			}
		
			int maxValue = calculateMaxValue(eventVals);
			 
			if(maxValue > maxBytes){
				maxBytes = maxValue;
			}
			
			return;
		}
		fetchEntireDataForSelectedDisks();
		
		 for(String disk:getUserSelectedItems())
		 {
			 ArrayList<DiskOverview> data = totalDiskData.get(disk);
			 
			 int[] eventValues = new int[data.size()];
			 for(int i =0; i<data.size(); i++)
			 {
				 eventValues[i] = getEventValueFromDisksData(data.get(i), event);
				
				 int maxValue = calculateMaxValue(eventValues);
				 
				 if(maxValue > maxBytes){
					 maxBytes = maxValue;
				 }
			 }
			 
		 }

	}

	private void fetchEntireDataForSelectedDisks()
	{
		ArrayList<String> selectedDisks = this.getUserSelectedItems();
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		for(String disk:selectedDisks)
		{
			ArrayList<DiskOverview> diskData = utils.getUsedMemoryAndSizesForDisk(disk, this.getCyclesData());
			totalDiskData.put(disk, diskData);
		}
	}
	
	private int getEventValueFromDisksData(DiskOverview diskData, EventTypes event)
	{
		int value = 0;
		
		switch(event)
		{
			case DISK_USED_SIZE:
				value = (int) diskData.getUsedSize();
				break;
			case DISK_TOTAL_SIZE:
				value = (int) diskData.getSize();
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
		double bytes =  valY * multiplier;
		
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
		text += scaledX + " s, " + scaledY;
		
		if(this.getEvent().equals(EventTypes.RAM_USED))
		{
			Polyline line = pointsData.get("RAM Used");
			
			if(line != null && line.containsPoint(x, y))
				text += "\n RAM Used";
		}
		else if (this.getEvent().equals(EventTypes.RAM_TOTAL))
		{
			Polyline line = pointsData.get("RAM Total");
			
			if(line != null && line.containsPoint(x, y))
				text += "\n RAM Total";
		}
		else
		{
			for(String disk: getUserSelectedItems())
			{
				Polyline line = pointsData.get(disk);
			
				if(line != null && line.containsPoint(x, y))
					text += "\n" + disk;
			}
		}
		
		return text;
	}
	

	private int [] getRamUsedMemFromSysData(ArrayList<SystemData> sysData)
	{
		Ram_Used_Values = new int[sysData.size()];
		
		for(int i=0; i<sysData.size(); i++)
		{
			SystemData currentData = sysData.get(i);
			Ram_Used_Values[i] = (int) (currentData.getTotalMemory() - currentData.getFreeMemory());
		}
		
		return Ram_Used_Values;
	}
	
	private int [] getRamTotalMemFromSysData(ArrayList<SystemData> sysData)
	{
		Ram_Total_Values = new int[sysData.size()];
		
		for(int i=0; i<sysData.size(); i++)
		{
			SystemData currentData = sysData.get(i);
			Ram_Total_Values[i] = (int) (currentData.getTotalMemory());
		}
		
		return Ram_Total_Values;
	}
	
	/**
	 * Get {@link DiskOverview} data for given disk name
	 * @param diskName
	 * @return data for given disk name
	 */
	public ArrayList<DiskOverview> getDiskData(String diskName)
	{
		return totalDiskData.get(diskName);
	}
	
	/**
	 * Get {@link SystemData} 
	 * @return system data
	 */
	public ArrayList<SystemData> getSystemData()
	{
		return sysData;
	}
		
}
