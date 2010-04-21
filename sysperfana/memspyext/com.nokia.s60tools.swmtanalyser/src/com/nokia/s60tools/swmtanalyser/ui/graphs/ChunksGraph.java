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

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * This class contains all needed logic to paint data related to Global and Non-Heap chunks. 
 *
 */
public class ChunksGraph extends GenericGraph {
	
	private HashMap<String, ArrayList<GlobalDataChunks>> chunksData = new HashMap<String, ArrayList<GlobalDataChunks>>();
	private HashMap<String, ArrayList<ChunksData>> nonHeapChunksData = new HashMap<String, ArrayList<ChunksData>>();
	
	private HashMap<String, Polyline> pointsData = new HashMap<String, Polyline>();
	
	private double visY;
	private double multiplier;
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paint(org.eclipse.draw2d.Graphics)
	 */
	public void paint(Graphics graphics) {
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint START");

		ArrayList<String> chunksList = this.getUserSelectedItems();
				
		 if(chunksList == null)
			 return;

		// Storing original settings before graphs are painted with case-specific settings
		int origLineWidth = graphics.getLineWidth();		
		Color origColor = graphics.getForegroundColor();
		int origLineStyle = graphics.getLineStyle();
					 
		// Setting graph drawing specific settings
		graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);		
		graphics.setLineStyle(SWT.LINE_SOLID);
		
		 int [] listX = this.calculateTimeIntervals();
		 this.lastSampleTime = listX[listX.length-1];
				 
		 int k=0;
		 
		 for(String chnk: chunksList)
		 { 
			 if(this.getEvent().equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
				 valuesToBePlotted = getGlobalChnkSizeData(chnk);
			 else if(this.getEvent().equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
				 valuesToBePlotted = getNonHeapChnkSizeData(chnk);
			
			 int [] points = new int[valuesToBePlotted.length *2];
			
			 double visY = visualSizeY - CommonGraphConstants.XLEGENDSPACE;
			 boolean handleDeleted = false;
			 
			 List<List<Integer>> ListOfSolidLinePoints = new ArrayList<List<Integer>>();			 
			 ArrayList<Integer> solidLinePoints = new ArrayList<Integer>();
			 
			 for (int i = 0, j = 0; i < valuesToBePlotted.length; i++, j++)
			 {
				if (valuesToBePlotted[i] <= 0){
					// Not showing zero values to a user, not meaningful data 
					DbgUtility.println(DbgUtility.PRIORITY_LOOP, "continued because value <= 0");
					continue;
				}				 
				 
				 int x_point = (int)(listX[i]/getScale());
			     int y_point =(int) (visY - valuesToBePlotted[i] /multiplier);

			     points[j] = x_point;
				 points[++j] = y_point;
			
				 if(!handleDeleted){						
					 if(y_point > 0){
						 solidLinePoints.add(x_point);
						 solidLinePoints.add(y_point);
							 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "add 1: x_point: " + x_point + ", y_point: " + y_point);
						 }
						 else{
						    DbgUtility.println(DbgUtility.PRIORITY_LOOP, "skipped because zero value");
						 }
				 }
				 
				 boolean handleStatus = getHandleStatus(i+1, chnk);
				 
				 if(handleStatus && !handleDeleted){
					 handleDeleted = true;

					 if(solidLinePoints.size() > 0)
						 ListOfSolidLinePoints.add(solidLinePoints);
					 
					 solidLinePoints = new ArrayList<Integer>();
				 }
				 
				 if(handleDeleted && getChunkStatus(i+1, chnk)== CycleData.New )
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
				 
				 points[i] = x_point;
				 points[i+1] = y_point;
			 }
			 
			 if(solidLinePoints.size() > 0)
				 ListOfSolidLinePoints.add(solidLinePoints);
			 
			 graphics.setForegroundColor(this.getColors().get(k));
			 graphics.setLineWidth(2);
			 			
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
					graphics.setLineStyle(SWT.LINE_SOLID);
					graphics.drawPolyline(solidPts);
					
					// Drawing markers to the data points
					GraphsUtils.drawMarkers(graphics, solidPts);
				}
			}
			
			 Polyline line = new Polyline();
			 line.setPoints(new PointList(points)); 
			 			
			 pointsData.put(chnk, line);
			 		 
			 k++;
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
	public void paintYAxis(GC gc) {
	
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

	private void fetchEntireDataForSelectedChunks()
	{
		ArrayList<String> selectedChunks = this.getUserSelectedItems();
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		if(this.getEvent().equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
		{
			for(String chunk:selectedChunks)
			{
				ArrayList<GlobalDataChunks> chnkData = utils.getGLOBDataFromAllCycles(chunk, this.getCyclesData());
				chunksData.put(chunk, chnkData);
			}
		}
		else if(this.getEvent().equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
		{
			for(String chunk:selectedChunks)
			{
				ArrayList<ChunksData> chnkData = utils.getChunkDataFromAllCycles(chunk, this.getCyclesData());
				nonHeapChunksData.put(chunk, chnkData);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#prepareData()
	 */
	public void prepareData() {
		fetchEntireDataForSelectedChunks();
		 
		 for(String chnk:getUserSelectedItems())
		 {
			 int [] valuesToBePlotted = null;
			 
			 switch(this.getEvent())
			 {
			 	case GLOBAL_DATA_SIZE:
			 		valuesToBePlotted = getGlobalChnkSizeData(chnk);
			 		break;
			 	case NON_HEAP_CHUNK_SIZE:
			 		valuesToBePlotted = getNonHeapChnkSizeData(chnk);
			 		break;
			 }
			
			 if(valuesToBePlotted == null)
				 continue;
			 
			 int maxValue = calculateMaxValue(valuesToBePlotted);
				 
			 if(maxValue > maxBytes)
				 maxBytes = maxValue;
			 }
				
	}
	
	private boolean getHandleStatus(int cycleNo, String chunkName)
	{
		boolean status = false;
		
		if(this.getEvent().equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
		{
			ArrayList<GlobalDataChunks> glod_data = chunksData.get(chunkName);
			
			status = glod_data.get(cycleNo -1).isKernelHandleDeleted();
		}
		else if(this.getEvent().equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
		{
			ArrayList<ChunksData> chunks_data = nonHeapChunksData.get(chunkName);
			
			status = chunks_data.get(cycleNo -1).isKernelHandleDeleted();
		}
		
		return status;
	}
	
	private int getChunkStatus(int cycleNo, String chunkName)
	{
		int status = 0;
		
		if(this.getEvent().equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
		{
			ArrayList<GlobalDataChunks> glod_data = chunksData.get(chunkName);
			
			status = glod_data.get(cycleNo -1).getAttrib();
		}
		else if(this.getEvent().equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
		{
			ArrayList<ChunksData> chunks_data = nonHeapChunksData.get(chunkName);
			
			status = chunks_data.get(cycleNo -1).getAttrib();
		}
		
		return status;
	}
	private int [] getGlobalChnkSizeData(String chnkName)
	{
		ArrayList<GlobalDataChunks> data = chunksData.get(chnkName);
		
		int [] values = new int[data.size()];
		
		for(int i=0; i<data.size(); i++)
		{
			if(data.get(i).getAttrib() == CycleData.Deleted)
				values[i] = 0;
			else
				values [i] = (int)data.get(i).getSize();
		}
		
		return values;
	}
	private int [] getNonHeapChnkSizeData(String chnkName)
	{
		ArrayList<ChunksData> data = nonHeapChunksData.get(chnkName);
		
		int [] values = new int[data.size()];
		
		for(int i=0; i<data.size(); i++)
		{
			if(data.get(i).getAttrib() == CycleData.Deleted)
				values[i] = 0;
			else
				values [i] = (int)data.get(i).getSize();
		}
		
		return values;
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
		for(String chnk: getUserSelectedItems())
		{
			Polyline line = pointsData.get(chnk);
		
			if(line != null && line.containsPoint(x, y))
				text += "\n" + chnk;
		}
		
		return text;
	}
	
	/**
	 * Get {@link GlobalDataChunks} for given chunk name
	 * @param chnkName
	 * @return List of chunks with given name
	 */
	public ArrayList<GlobalDataChunks> getGlobalChunkData(String chnkName)
	{
		return chunksData.get(chnkName);
	}
	
	/**
	 * Get {@link ChunksData} for given chunk name
	 * @param chnkName
	 * @return List of data with given name
	 */
	public ArrayList<ChunksData> getNonHeapChunkData(String chnkName)
	{
		return nonHeapChunksData.get(chnkName);
	}
	
}
