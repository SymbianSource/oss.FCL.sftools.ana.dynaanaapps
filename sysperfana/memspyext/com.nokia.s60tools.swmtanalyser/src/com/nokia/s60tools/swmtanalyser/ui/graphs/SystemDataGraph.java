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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * This class contains all needed logic to paint data related to System data. 
 */
public class SystemDataGraph extends GenericGraph
{
	private ArrayList<KernelElements> kernelElements;
	private HashMap <String, Polyline> pointsData = new HashMap<String, Polyline>();
	
	private double visY;
	private double multiplier;

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paint(org.eclipse.draw2d.Graphics)
	 */
	public void paint(Graphics graphics) {
		
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint START");
		
		ArrayList<String> selectedItems = this.getUserSelectedItems();
		
		 if(selectedItems == null)
			 return;
		
		 int [] listX = this.calculateTimeIntervals();
		 this.lastSampleTime = listX[listX.length-1];
		 		 
		 int k=0;

		 // Storing drawing settings
		 Color origColor = graphics.getForegroundColor();
		 int origLineWidth = graphics.getLineWidth();
		 // Setting drawing settings
		 graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);
		 
		 for(String item: selectedItems)
		 { 
			 int [] valuesToBePlotted = getValuesForGivenKerenelElement(item, kernelElements);
			 
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
			 
			 graphics.setForegroundColor(this.getColors().get(k));
			 // Drawing graph
			 graphics.drawPolyline(points);
			 // Drawing markers to the data points
			 GraphsUtils.drawMarkers(graphics, points);
			
			 Polyline line = new Polyline();
			 line.setPoints(new PointList(points));
			 pointsData.put(item, line);
			 
			 k++;
		}
		 
		// Restoring drawing settings
		 graphics.setForegroundColor(origColor);
		 graphics.setLineWidth(origLineWidth);		 
			 
		 DbgUtility.println(DbgUtility.PRIORITY_OPERATION, this.getClass().getSimpleName() + "/paint END");
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#paintYAxis(org.eclipse.swt.graphics.GC)
	 */
	public void paintYAxis( GC gc) {
		
		visY = visualSizeY - CommonGraphConstants.XLEGENDSPACE; 
		multiplier = GraphsUtils.roundToNearestNumber(maxBytes) / visY;
			
		double yIncrement = visY / 10;
		int previousBottom = 0;
		
		for (int k = 10; k >= 0; k--)
		{
			// location for the value indicator is k * 1/10 the height of the display
			int y = (int) (visY - (yIncrement * k));
		
			int bytes = (int)(yIncrement * multiplier)* k;

			String legend = "";
			legend += bytes ;
			
			Point extent = gc.stringExtent(legend);
			
			gc.drawLine(CommonGraphConstants.YLEGENDSPACE - 3, (int)y + 1, CommonGraphConstants.YLEGENDSPACE, (int)y + 1);
			
			if (y >= previousBottom)
			{
				gc.drawString(legend, CommonGraphConstants.YLEGENDSPACE - extent.x -2, (int)y);
				previousBottom = (int)y + extent.y;
			}
		}

		final Image image = this.getVerticalLabel("Count");
		gc.setAdvanced(true);
		final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
		Transform transform = new Transform(Display.getDefault());

		transform.translate(rect2.height / 2f, rect2.width / 2f);
		transform.rotate(-90);
		transform.translate(-rect2.width / 2f, -rect2.height / 2f);

		gc.setTransform(transform);
		gc.drawImage(image, -(int)visY/3, 0);

		transform.dispose();
		gc.dispose();				

	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph#prepareData()
	 */
	public void prepareData() {
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		kernelElements = utils.getKerenelElemsFromAllCycles(this.getCyclesData());
		
		for(String item:getUserSelectedItems())
		 {
			 int [] values = getValuesForGivenKerenelElement(item, kernelElements);
			 
			 int maxValue = calculateMaxValue(values);
			 
			 if(maxValue > maxBytes){
					maxBytes = maxValue;
			 }
		 }		
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
		int valY = (int)visY - y;
		int scaledY = (int)(valY * multiplier);
		
		text += scaledX + " s, " + scaledY;
		
		for(String elem: getUserSelectedItems())
		{
			Polyline line = pointsData.get(elem);
		
			if(line != null && line.containsPoint(x, y))
				text += "\n" + elem;
		}
		
		return text;
	}
	
	/**
	 * Get kernel data
	 * @return kernel data
	 */
	public ArrayList<KernelElements> getKernelData()
	{
		return this.kernelElements;
	}
}
