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



package com.nokia.s60tools.traceanalyser.ui.editors;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.traceanalyser.containers.RuleInformation;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;
import com.nokia.s60tools.traceanalyser.export.TraceAnalyserRule;
import com.nokia.s60tools.traceanalyser.model.Engine;
import com.nokia.s60tools.traceanalyser.plugin.TraceAnalyserPlugin;


/**
 * HistoryGraph.
 * Graph UI-component for Trace Analyser
 */
public class HistoryGraph implements MouseMoveListener {

	// Margins for each border
	private static final int MARGIN_LEFT = 100;
	private static final int MARGIN_OTHERS = 40;
	private static final int GRAPHMARGIN = 10;
	
	/* RuleInformation that is drawn to screen */
	private RuleInformation information;
	
	/* TraceAnalyser engine */
	Engine engine;
	
	/* UI components */
	private Composite composite;
	private Canvas canvas;
	
	/**
	 * HistoryGraph.
	 * @param composite composite where components are placed.
	 * @param information information which is drawn to screen.
	 */
	HistoryGraph(Composite composite, RuleInformation information){
		this.composite = composite;
		this.information = information;
		this.engine = TraceAnalyserPlugin.getEngine();
	}
	
	/**
	 * drawGraph.
	 * Draws graph to screen.
	 */
	void drawGraph(){
		if(canvas != null){
			canvas.dispose();
		}
		canvas = new Canvas(composite, SWT.BORDER);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add mouse mouse listener.
		canvas.addMouseMoveListener(this);
		
		// Add paint listener
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				drawBox(gc);
				drawLimits(gc);
				drawEvents(gc);
			}

			
		});
		
	}
	
	
	/**
	 * redraw.
	 * Redraws screen.
	 */
	public void redraw(){
		canvas.redraw();
	}
	
	
	/**
	 * getCanvasSize.
	 * @return size or the canvas.
	 */
	private Point getCanvasSize(){
		Point point = canvas.getSize();
		point.x -= 5;
		point.y -= 5;
		return point;
	}
	
	/**
	 * drawBox.
	 * Draws box for graph into graphics content.
	 * @param gc graphics content where content is drawn
	 */
	private void drawBox(GC gc){

		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// count start points of box
		int startPointX = MARGIN_LEFT - GRAPHMARGIN;
		int startPointY = MARGIN_OTHERS - GRAPHMARGIN;
		
		// Count width and height of box
		Point canvasSize = getCanvasSize();
		int width = canvasSize.x - (MARGIN_LEFT + MARGIN_OTHERS - GRAPHMARGIN*2);
		int height = canvasSize.y - (MARGIN_OTHERS*2 - GRAPHMARGIN*2);
		
		// draw and fill rectangle.
		gc.fillRectangle(startPointX, startPointY, width, height);
		gc.drawRectangle(startPointX, startPointY, width, height);
	

	}
	
	
	/**
	 * drawEvents.
	 * Draws all history events to screen.
	 * @param gc graphics content where content is drawn
	 */
	private void drawEvents(GC gc){
		
		ArrayList<RuleEvent> events = information.getEvents();
		Point canvasSize = getCanvasSize();
		
		// Count start points
		double sizeY = canvasSize.y - MARGIN_OTHERS * 2;
		double sizeX = canvasSize.x - (MARGIN_LEFT + MARGIN_OTHERS);
		
		
		int[] minAndMax = getMinAndMax();
		int min = minAndMax[0];
		int max = minAndMax[1];

		
		// get difference between min and max value.
		int difference = max - min;
		
		// get multiplier for Y values from that difference.
		double yMultiplier = sizeY / difference;
		
		// get multiplier for X values from canvas size and smount of events.
		double xMultiplier = sizeX / (events.size() - 1);
		
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		int[] previousValues = new int[2]; 
		int i = 0;

		while(i < events.size()){
			
			gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			
			// count x-value based on what event is drawn to screen
			int xValue = (int)(i * xMultiplier);
			
			// count y-value based on what event value is.
			double yValueDouble = events.get(i).getValue() - min;
			yValueDouble *= yMultiplier;
			int yValue = (int)(sizeY - yValueDouble);
			
			// add margins
			xValue += MARGIN_LEFT;
			yValue += MARGIN_OTHERS;
			
			// Draw mark on one event(currently disabled)
			//			drawMark(gc, xValue, yValue);
			
			gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

			// Draw line between this and previous event.
			if(i > 0 ){
				gc.drawLine(xValue, yValue, previousValues[0], previousValues[1]);
			}
			
			// save value to previous value variable
			previousValues[0] = xValue;
			previousValues[1] = yValue;
				
			i++;
		}
	}
	
	/**
	 * drawMark.
	 * Draws mark of one event into given coordinate
	 * @param gc graphics content where content is drawn
	 * @param xValue x-coordinate
	 * @param yValue y-coordinate
	 */
	@SuppressWarnings("unused")
	private void drawMark(GC gc, int xValue, int yValue){
		gc.fillOval(xValue - 3, yValue - 3, 6, 6);
		gc.drawOval(xValue - 3, yValue - 3, 6, 6);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {
		
		// sets tooltip based on what is x-coordinate of mouse.
		
		Point canvasSize = getCanvasSize();
		int drawingAreaSize = canvasSize.x - (MARGIN_LEFT + MARGIN_OTHERS) ;
		
		// Count divider for x-coordinate
		double unit = (double)drawingAreaSize / ((double)information.getEvents().size() - 1);
		
		// If mouse pointer is over graph area
		if(event.x < (canvasSize.x - (MARGIN_OTHERS-GRAPHMARGIN)) && 
		   event.x > (MARGIN_LEFT - GRAPHMARGIN) &&
		   event.y > (MARGIN_OTHERS - GRAPHMARGIN) &&
		   event.y < (canvasSize.y - (MARGIN_OTHERS-GRAPHMARGIN)) )
		{
			// Count item's index based on x-coordinate
			double indexDouble = (event.x - MARGIN_LEFT) / unit;
			indexDouble += 0.5;
			int index = (int)indexDouble;
			
			if(index > information.getEvents().size()){
				index = information.getEvents().size()-1;
			}
			
			// Get time and measured value from object and set tooltip
			Date date = information.getEvents().get(index).getTime();
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
			canvas.setToolTipText("Time:" + formatter.format(date) + " Value:" + Integer.toString(information.getEvents().get(index).getValue()));

		}
		else{
			canvas.setToolTipText(null);
		}

		
		
	}
	
	/**
	 * dispose.
	 */
	public void dispose(){
		canvas.dispose();
	}
	
	
	/**
	 * drawLimits.
	 * Draws y-axis max and min values + 3 values between those into given graphics content.
	 * @param gc graphics content where content is drawn
	 */
	private void drawLimits(GC gc) {
		
		// Left margin before texts
		int numberMargin = 20;
		int letterSize = 12;
		Point canvasSize = getCanvasSize();
		
		int min = 0;
		int max = 0;
		
		int[] minAndMax = getMinAndMax();
		min = minAndMax[0];
		max = minAndMax[1];

		// get difference between min and max value.
		double difference = max - min;
		
		// get multiplier for Y values from that difference.
		double yMultiplier = (canvasSize.y - MARGIN_OTHERS * 2) / difference;
		
		
		double xCoordDifference = ((double)(canvasSize.y - MARGIN_OTHERS * 2)) / 4;
		double valueDifference = ((double)(difference)) / 4;

		// Set colors
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		// Get start values
		double yCoord = (double)MARGIN_OTHERS;
		double value = (double)max;

		int i = 0;
		while(i < 5){
			
			// draw value
			String printedValueText = Integer.toString((int)(value + 0.5)) + information.getRule().getUnit();
			gc.drawText(printedValueText, numberMargin, (int) yCoord - letterSize);
			
			// draw text
			drawLine(gc, (int)yCoord, numberMargin, Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
			yCoord += xCoordDifference;
			value -= valueDifference;
			i++;
		
		}
		
		// draw "Time ->" text into x-axis.
		Point timeText = new Point(canvasSize.x / 2, canvasSize.y - MARGIN_OTHERS + 12); 
		gc.drawText("Time ->", timeText.x, timeText.y);
		
		
		// draw limits into graph
		
		TraceAnalyserRule rule = engine.getRule(this.information.getRule().getName());
		int[] limits = rule.getLimits();

		
		for(int item : limits){
			int yValue = (int)((item - min) * yMultiplier);
			yCoord = canvasSize.y - MARGIN_OTHERS - yValue;
			drawLine(gc, (int)yCoord, numberMargin, Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		}
		
	}
	
	/**
	 * drawLine.
	 * Draws straight line into given y-coordinate.
	 * @param gc graphics content where content is drawn
	 * @param yCoord y-coordinate
	 * @param numbeMargin margin in front of line.
	 */
	private void drawLine(GC gc, int yCoord, int numberMargin, Color color){
		Color previousColor = gc.getForeground();
		gc.setForeground(color);	
		gc.drawLine(numberMargin, yCoord,getCanvasSize().x-(MARGIN_OTHERS-GRAPHMARGIN), yCoord );
		gc.setForeground(previousColor);


	}
	
	/**
	 * Returnns min and max value from one rule's history
	 * @return
	 */
	private int[] getMinAndMax(){
		TraceAnalyserRule rule = engine.getRule(this.information.getRule().getName());

		int[] limits = rule.getLimits();

		int min = information.getMin();
		int max = information.getMax();
		
		for(int item : limits){
			if(item > max){
				max = item;
			}
			else if(item < min){
				min = item;
			}
		}
		
		return new int[]{min,max};
	}

	/**
	 * sets context sensitive helps
	 */
	public void setHelps(){
		PlatformUI.getWorkbench().getHelpSystem().setHelp( canvas, com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_HISTORY_VIEW);

	}
}
