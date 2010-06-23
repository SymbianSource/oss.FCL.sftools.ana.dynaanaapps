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

package com.nokia.carbide.cpp.pi.peccommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.Viewport;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

/**
 * The performance counter graph class, responsible for drawing the several graphs
 * based on performance counters as well as managing the legend views to some extend
 */
public class PecCommonTraceGraph extends GenericTraceGraph implements 
		MouseMotionListener, PIEventListener,  ITitleBarMenu  {

	private static int X_LEGEND_HEIGHT = 20;

	/** for tooltip: margin for search area for finding a domain object */
	private static final int MARGIN_IN_PIXELS = 5;
	
	/** the graph index; there is one graph per editor page (with identical content)*/
	//private int graphIndex;
	protected PecCommonTrace trace;
	private String graphTitle;
	private String helpContextId;

	/** number of series to draw; each in a separate graph section */
	private int typeAmount = 0;
	protected List<Integer> drawSeries;
	
	private int[][] origYCoords;
	private int[][] points;

	private int[] exact_mins;
	private int[] exact_maxs;

	private int[] mins;
	private int[] maxs;
	
	private double currentlyCalcFinalScale = -1;
	private int currentlyCalcFinalHeight = -1;

	protected FigureCanvas leftFigureCanvas;

	/** marks graph as dirty so it gets re-drawn */
	protected boolean dirty;

	private PecCommonLegend legend;
	
	/** GUI manager has knowledge off all IpcTraceGraphs; can broadcast some of the events */
	private PecCommonGuiManager guiManager;

	private int lastEdgeX;
	private int paintCount;
	
	/**
	 * Constructor
	 * @param graphIndex the index of the graph (corresponds to the page in the editor)
	 * @param pecTrace the trace class
	 * @param uid the uid to identify the current editor
	 * @param guiManager IpcGuiManager which manages all graphs
	 * @param title The title of the graph
	 * @param helpContextIdMainPage 
	 */
	public PecCommonTraceGraph(int graphIndex, PecCommonTrace pecTrace, int uid, PecCommonGuiManager guiManager, String title, String helpContextIdMainPage) {
		super(pecTrace);

//		this.graphIndex = graphIndex;
		this.trace = pecTrace;
		this.guiManager = guiManager;
		this.graphTitle = title;
		this.helpContextId = helpContextIdMainPage;
		
		typeAmount = pecTrace.getValueTypes().length;

		int sampleCount = pecTrace.getSampleAmount();
		origYCoords = new int[typeAmount][sampleCount];
		points = new int[typeAmount][sampleCount*2];
		exact_mins = new int[typeAmount];
		exact_maxs = new int[typeAmount];
		mins = new int[typeAmount];
		maxs = new int[typeAmount];
		
		drawSeries = new ArrayList<Integer>();
		//by default, draw all graphs on screen
		showAll();
		
		this.initialiseData();
		
		ProfileVisualiser pV = NpiInstanceRepository.getInstance().getProfilePage(uid, graphIndex);
		legend = createLegend(pV.getBottomComposite());
	}
	
	/**
	 * Creates the legend
	 * @param bottomComposite The composite to create the legend in
	 */
	protected PecCommonLegend createLegend(Composite bottomComposite) {		
		return new PecCommonLegend(this, bottomComposite, getTitle(), trace);
	}

	/**
	 * Fill mins, maxs, and origYCoords arrays
	 */
	private void initialiseData() {
		Vector<GenericSample> sv = trace.samples;

		Arrays.fill(exact_mins, Integer.MAX_VALUE);
		Arrays.fill(exact_maxs, Integer.MIN_VALUE);

		for (int x = 0; x < sv.size(); x++) {
			PecCommonSample s = (PecCommonSample) sv.get(x);

			for (int i = 0; i < typeAmount; i++) {
				int value = s.values[i];

				if (value < exact_mins[i]){
					exact_mins[i] = value;					
				} 
				if (value > exact_maxs[i]){
					exact_maxs[i] = value;					
				}

				origYCoords[i][x] = value;
			}
		}
		
		//let the graph draw in area [0, prettyMaxValue]
		for (int i = 0; i < typeAmount; i++) {
			mins[i] = 0;
			maxs[i] = prettyMaxValue(exact_maxs[i]);
		}
	}
	
	private void showAll(){
		drawSeries.clear();
		for (int i = 0; i < typeAmount; i++) {
			drawSeries.add(i);
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#repaint()
	 */
	@Override
	public void repaint() {
		this.parentComponent.repaintComponent();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paint(org.eclipse.draw2d.Panel, org.eclipse.draw2d.Graphics)
	 */
	@Override
	public void paint(org.eclipse.draw2d.Panel panel,
			org.eclipse.draw2d.Graphics graphics) {
		this.setSize(this.getSize().width, getVisualSize().height);
		this.drawDottedLineBackground(graphics, X_LEGEND_HEIGHT);
		
		drawGraphs(panel, graphics);
		drawLabelsInGraph(panel, graphics);

		this.drawSelectionSection(graphics, X_LEGEND_HEIGHT);
	}

	private void drawLabelsInGraph(Panel panel, Graphics graphics) {

		float sectionHeight = getSectionHeight();
		
		if (sectionHeight > 60f){ //only draw the label if the section has a decent height
			
			int edgeX = ((Viewport) panel.getParent()).getViewLocation().x;
			if (lastEdgeX != edgeX && paintCount < 1 && guiManager != null) {
				paintCount++;
				guiManager.selectionAreaChanged(PIPageEditor
						.currentPageEditor().getStartTime(), PIPageEditor
						.currentPageEditor().getEndTime());
			} else {
				paintCount = 0;
			}
			lastEdgeX = edgeX;

			for (int i = 0; i < drawSeries.size(); i++) {
				int seriesIdx = drawSeries.get(i);
				int y = (int) sectionHeight * i;

				graphics.setForegroundColor(ColorConstants.black);
				graphics.drawString(trace.getValueTypes()[seriesIdx],
						edgeX + 10, y + 6);
				if (sectionHeight > 80f){
					graphics.drawString(String.format(Messages.PecCommonTraceGraph_0,
							this.exact_mins[seriesIdx], this.exact_maxs[seriesIdx]),
							edgeX + 10, y + 18);					
				}
			}
		}
	}

	private void drawGraphs(Panel panel, Graphics graphics) {
		int visY = this.getVisualSize().height;
		double scale = getScale();

		if (dirty || this.currentlyCalcFinalHeight != visY
				|| this.currentlyCalcFinalScale != scale) {

			Vector<GenericSample> sv = trace.samples;
			float sectionHeight = getSectionHeight();
			
			for (int sampleIdx = 0; sampleIdx < sv.size(); sampleIdx++) {
				// calculate x-coordinate per sample
				int xCoord = (int) (sampleIdx / scale);

				for (int seriesIdx : drawSeries) {
					points[seriesIdx][sampleIdx * 2] = xCoord;
					points[seriesIdx][sampleIdx * 2 + 1] = convertValueToYCoordinate(origYCoords[seriesIdx][sampleIdx], mins[seriesIdx], maxs[seriesIdx], drawSeries.indexOf(seriesIdx), sectionHeight);
				}
			}
			
			dirty = false;
			this.currentlyCalcFinalHeight = visY;
			this.currentlyCalcFinalScale = scale;
		}

		graphics.setForegroundColor(ColorConstants.blue);

		for (int seriesIdx : drawSeries) {
			graphics.drawPolyline(points[seriesIdx]);
		}
	}
	
	/**
	 * Converts a y coordinate into the actual value.
	 * @param yCoordinate the y coordinate to convert
	 * @param minValue the lowest value in the series
	 * @param maxValue the highest value in the series
	 * @param section the number of the section (for 3 sections this would be 0, 1, or 2)
	 * @param sectionHeight the height of the section in pixels. All sections are of equal height
	 * @return the converted value
	 */
	static int convertYCoordinateToValue(int yCoordinate, int minValue, int maxValue, int section, float sectionHeight){
		float offset = sectionHeight * section;
		
		float location = sectionHeight - (yCoordinate - offset) ;
		return (int)(location * (maxValue - minValue) / sectionHeight ) + minValue;
	} 
	
	/**
	 * Converts a value into a y coordinate; the opposite of convertYCoordinateToValue()
	 * @param value the value to convert
	 * @param minValue the lowest value in the series
	 * @param maxValue the highest value in the series
	 * @param section the number of the section (for 3 sections this would be 0, 1, or 2)
	 * @param sectionHeight the height of the section in points. All sections are of equal height
	 * @return the converted value
	 */
	static int convertValueToYCoordinate(int value, int minValue, int maxValue, int section, float sectionHeight){
		float offset = sectionHeight * section;
		
		float location = (value - minValue) * sectionHeight / (maxValue - minValue); //lowest shown value is min (not 0)
		return(int) ((sectionHeight - location) + offset);
	}
	
	/**
	 * Returns the height of a section in pixels. All sections are of equal height.
	 * @return
	 */
	private float getSectionHeight(){
		int visualHeight = getVisualSize().height - X_LEGEND_HEIGHT;
		return drawSeries.size() == 0 ? visualHeight :  visualHeight / drawSeries.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paintLeftLegend
	 * (org.eclipse.draw2d.FigureCanvas, org.eclipse.swt.graphics.GC)
	 */
	@Override
	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
		GC localGC = gc;

		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas
				.getClientArea();

		int visY = rect.height;

		float visYfloat = visY - X_LEGEND_HEIGHT;

		if (visYfloat < 0f)
			visYfloat = 0f;

		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));
		
		int legendsToDraw = drawSeries.size();
		float legendHeight = visYfloat / legendsToDraw;
		double yIncrement = legendHeight / 10;
		int previousBottom = 0;
		
		for (int section = 0; section < legendsToDraw; section++) {
			int seriesIdx = drawSeries.get(section);
			int maxValue = maxs[seriesIdx];

			float valuePerPixel = maxValue / legendHeight;

			for (int k = 10; k >= 0; k--) {
				// location for the value indicator is k * 1/10 the height of
				// the
				// height of the section
				int y = (int) (legendHeight * (section+1) - (yIncrement * k));
				int value = (int) ((((legendHeight * valuePerPixel) / 10.0) * k));

				// construct the text for each scale
				//use grouping for small numbers, then without grouping to fit the text
				String sValue = String.format(value < 1000000 ? Messages.PecCommonTraceGraph_1 : Messages.PecCommonTraceGraph_2, value);

				Point extent = gc.stringExtent(sValue);

				gc.drawLine(IGenericTraceGraph.Y_LEGEND_WIDTH - 3,  y + 1,
						IGenericTraceGraph.Y_LEGEND_WIDTH, y + 1);

				if (y >= previousBottom) {
					gc.drawString(sValue, IGenericTraceGraph.Y_LEGEND_WIDTH
							- extent.x - 4, y);
					previousBottom = y + extent.y;
				}
			}
		}

		if (localGC == null) {
			gc.dispose();
			figureCanvas.redraw();
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		return this.graphTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseDragged(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseDragged(MouseEvent arg0) {
		// no-op		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		// no-op		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		// no-op		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseHover(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseHover(MouseEvent arg0) {
		// no-op		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseMoved(MouseEvent me) {
		//setting the tooltip
		float sectionHeight = getSectionHeight();
		int section = (int) (me.y / sectionHeight);
		if (section < 0 || section >= drawSeries.size()){
			this.setToolTipText(null);
			return;
		}
		int seriesIdx = drawSeries.get(section);
		
		PecCommonSample sample = getSampleUnderMouse(me);

		if (sample != null){
			setToolTipText(String.format(Messages.PecCommonTraceGraph_3, trace.getValueTypes()[seriesIdx], sample.sampleSynchTime /1000f, sample.values[seriesIdx]));			
		} else {
			String tooltip = null;

			//display default values
			
			if (section < drawSeries.size()) {
				double time = me.x * this.getScale();
				if (time >= 0 && time <= trace.getLastSampleTime()){
					int value = convertYCoordinateToValue(me.y, mins[seriesIdx], maxs[seriesIdx], section, sectionHeight);
					tooltip = String.format(Messages.PecCommonTraceGraph_4, time / 1000f, value);
				}
			}
			
			this.setToolTipText(tooltip);			
		}
	}

	/**
	 * Returns the sample for the given mouse location, or null
	 * @param me mouse event for the mouse location
	 * @return the sample if found, or null
	 */
	private PecCommonSample getSampleUnderMouse(MouseEvent me) {
		PecCommonSample ret = null;
		
		if (drawSeries.size() == 0){
			return ret; //no graphs are being drawn so no samples can be found
		}

		int mex = me.x;
		int mey = me.y;
		float sectionHeight = getSectionHeight();
		int section = (int) (mey / sectionHeight);
		
		if (section >= 0 && section < drawSeries.size()) {
			int seriesIdx = drawSeries.get(section);
			double idealTime = mex * this.getScale();
			if (idealTime < 0 || idealTime > trace.getLastSampleTime()){
				return null; 
			}
			int idealValue = convertYCoordinateToValue(mey, mins[seriesIdx], maxs[seriesIdx], section, sectionHeight);

			//calculate the boundary of the area in which to look for a sample
			double leftBoudaryTime =  ((mex - MARGIN_IN_PIXELS) * getScale());
			if (leftBoudaryTime < 0){
				leftBoudaryTime = 0;
			}
			double rightBoundaryTime = ((mex + MARGIN_IN_PIXELS) * getScale());
			if (rightBoundaryTime < 0){
				return null; 
			} else if (rightBoundaryTime > trace.getLastSampleTime()){
				rightBoundaryTime = trace.getLastSampleTime();
			}
			 
			int topBoundaryValue = convertYCoordinateToValue(Math.max((int)(section * sectionHeight), mey - MARGIN_IN_PIXELS), mins[seriesIdx], maxs[seriesIdx], section, sectionHeight);
			int bottomBoundaryValue = convertYCoordinateToValue(Math.min((int)((section +1) * sectionHeight), mey + MARGIN_IN_PIXELS), mins[seriesIdx], maxs[seriesIdx], section, sectionHeight);
			
			int leftSample = (int)(leftBoudaryTime / trace.getSamplingInterval());
			int rightSample = (int)(rightBoundaryTime+.5 / trace.getSamplingInterval());
			
			for (int i = leftSample; i <= rightSample; i++) {
				PecCommonSample sample = (PecCommonSample)trace.getSample(i);
				if (sample.values[seriesIdx] < topBoundaryValue && sample.values[seriesIdx] > bottomBoundaryValue){
					//System.out.println("Cur: "+ sample.values[seriesIdx]+" Range: "+topBoundaryValue+", "+bottomBoundaryValue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					//check whether we have found a closer match to mid-point
					if (ret == null 
							|| (Math.abs(sample.sampleSynchTime - idealTime) < Math.abs(ret.sampleSynchTime - idealTime))
							|| (ret.sampleSynchTime == sample.sampleSynchTime 
									&& Math.abs(sample.values[seriesIdx] - idealValue) < Math.abs(ret.values[seriesIdx]	- idealValue))){
						ret = sample;
					}
				}
			} 
		}
		
		return ret;
	}

	/**
	 * Converts the passed X-coordiate into a time value (in milliseconds) using
	 * the scale provided. Makes sure the return value is non-negative.
	 * 
	 * @param x
	 *            the x coordinate to use
	 * @param scale
	 *            the scale to use
	 * @return time in milliseconds
	 */
	protected static double getTimeForXCoordinate(int x, double scale) {
		double time = x * scale;
		// mouse event may return out of range X, that may
		// crash when we use it to index data array
		time = time >= 0 ? time : 0;
		return time;
	}
	
	/**
	 * Make the input value a pretty value to display, such as 423,345 => 500,000
	 * @param value the value to convert
	 * @return the prettier value
	 */
	static int prettyMaxValue(final int value) {
		double prettyVal = value;
		int len = 0;

		while (prettyVal >= 10) {
			prettyVal/= 10;
			len++;
		}
		
		prettyVal = Math.ceil(prettyVal);
		
		if (prettyVal <= 1){
			prettyVal = 1;
		} else if (prettyVal <= 2){
			prettyVal = 2;
		} else if (prettyVal <= 3){
			prettyVal = 3;
		} else if (prettyVal <= 5){
			prettyVal = 5;
		} else {
			prettyVal = 10;
		}
		
		return (int)(prettyVal * Math.pow(10, len));
	}

	/**
	 * Callback for PIEvent.SELECTION_AREA_CHANGED
	 * @param newStart new selection start
	 * @param newEnd new selection end
	 */
	void selectionAreaChanged(double newStart, double newEnd) {
		this.setSelectionStart(newStart);
		this.setSelectionEnd(newEnd);
		
		trace.selectionAreaChanged(newStart, newEnd);
		legend.refreshLegend();
		
		this.repaint();				
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.PIEventListener#piEventReceived(com.nokia.carbide.cpp.internal.pi.visual.PIEvent)
	 */
	public void piEventReceived(PIEvent be) {
		switch (be.getType()) {
		
		case PIEvent.SELECTION_AREA_CHANGED:
			double[] values = (double[]) be.getValueObject();
			//broadcast to all IPC graphs
			guiManager.selectionAreaChanged(values[0], values[1]);				
			break;

		case PIEvent.SCROLLED:
			Event event = ((Event) be.getValueObject());
			//this broadcasts to all graphs on this PICompositePanel
			this.parentComponent.setScrolledOrigin(event.x, event.y, (FigureCanvas)event.data);
			this.repaint();
			break;

		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#graphMaximized(boolean)
	 */
	@Override
	public void graphMaximized(boolean value) {
		legend.setLegendMaximised(value);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#graphVisibilityChanged(boolean)
	 */
	@Override
	public void graphVisibilityChanged(boolean value) {
		legend.setLegendVisible(value);
	}

	@Override
	public void action(String action) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Adds or removes a series to be drawn
	 * @param seriesId series id of the series to add or remove
	 * @param add true if series is to be added, false for remove
	 */
	public void addOrRemoveSeries(int seriesId, boolean add){
		if (add){
			if (!drawSeries.contains(seriesId)){
				drawSeries.add(seriesId);
				Collections.sort(drawSeries);
			}
		} else {
			drawSeries.remove(Integer.valueOf(seriesId));
		}
		
		//repaint graphs
		redrawGraphArea();
	}
	
	private void redrawGraphArea() {
		dirty = true;
		setGraphImageChanged(true);
		repaint();		
	}

	/**
	 * Removes all series from display
	 */
	public void removeAllSeries(){
		drawSeries.clear();
		redrawGraphArea();
		
	}

	/**
	 * Shows all series in graph area
	 */
	public void showAllSeries(){
		showAll();
		redrawGraphArea();
	}

	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#addTitleBarMenuItems()
	 */
	public Action[] addTitleBarMenuItems() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#getContextHelpId()
	 */
	public String getContextHelpId() {
		return this.helpContextId;
	}

}
