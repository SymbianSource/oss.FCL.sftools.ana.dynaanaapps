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
 * Description: GenericTraceGraph.java 
 *
 */

package com.nokia.carbide.cpp.internal.pi.visual;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.pi.util.ColorPalette;


public abstract class GenericTraceGraph
{
	protected int graphIndex = 0;
	private static final int ALPHA_UNSELECTED = 175;
	private static final int SELECTION_BAR_WIDTH = 2;
	
	// amount of space at left of graph to contain y-axis units
	public static final int yLegendWidth = 50;

    // fill selection flags
    private double timeOffset = 0; 
    protected boolean debug = false;
    
    // PI time scale multiplier
    private float piTimeScale = 1;
    private boolean timescalingEnabled = false;
    
    private int visibleAreaIdentifier = 0; //is used to detect changes in drawing area
    private boolean updatePolylinesIsNeeded = true;
    public boolean updateCumulativeThreadTableIsNeeded = true;
    
	public boolean fillFlag = false;	 //fill items is selected, all or selected?
	public boolean fillSelected = false; //fill selected items
	
    private int visibleRightBorder; //these are updated when the visible area changes / polylines need to be updated
    private int visibleLeftBorder;
    
	private int sizeX = 0;
	private int sizeY = 0;

	private int visualSizeX = 0;
	private int visualSizeY = 0;
	
	private int preferredSizeX = 0;
	private int preferredSizeY = 0;

	private double selectionStart = -1;
	private double selectionEnd = -1;
   
//	private double scaleX = 10;	 //default when creating a new analysis
	private double scaleY = 100; //default for percentages
	
	private double scale = 0;
    private boolean highResolution = true;
	
	public PICompositePanel parentComponent;

	private GenericTrace myTrace;
	
	private Vector<GenericTraceGraph> graphSubComponents;
	
	private Image graphImage = null;
	private boolean graphImageChanged = true;

	public GenericTraceGraph(GenericTrace data)
	{
		this.graphSubComponents = new Vector<GenericTraceGraph>();
		this.myTrace = data;
	}
	
	public GenericTrace getTrace()
	{
		return this.myTrace;
	}
    
    public void setTimeOffset(double offset)
    {
        this.timeOffset = offset;
    }

    public double getTimeOffset()
    {
        return this.timeOffset;
    }
	
	public void addSubGraphComponent(GenericTraceGraph subGraph)
	{
		this.graphSubComponents.add(subGraph);
	}
	
	public Enumeration getGraphSubComponents()
	{
		return graphSubComponents.elements();
	}
	
	public void importParentComponent(PICompositePanel parent)
	{
		this.parentComponent = parent;
	}
	
	public void setCurrentInfoComponent(Component infoComponent)
	{
		if (parentComponent instanceof PICompositePanel)
		{
			((PICompositePanel)parentComponent).
				setCurrentInfoComponent(infoComponent);
		}
	}
	
	public PIVisualSharedData getSharedDataInstance()
	{
		if (parentComponent instanceof PICompositePanel)
		{
			return ((PICompositePanel)parentComponent).getSharedData();
		}
		else
			return null;
	}
	
	// a rectangle that describes visible area fo the draw 2D graphics so we paint only that
	public Rectangle getVisibleArea(Graphics graphics) {
		return graphics.getClip(new org.eclipse.draw2d.geometry.Rectangle());
	}
	
	public abstract void paint(Panel panel, Graphics graphics);
	
	public abstract void paintLeftLegend(FigureCanvas figureCanvas, GC gc);
	
	// call all subcomponents' repaint methods
	public abstract void repaint();
	
	public abstract void refreshDataFromTrace();
	
	public abstract void action(String action);
	
	public void setSize(int x, int y)
	{
		this.sizeY = y;
		this.sizeX = x;
	}
	
	public void setPITimeScale(float scale)
	{
	    System.out.println(Messages.getString("GenericTraceGraph.setTimeScaleTo") + scale); //$NON-NLS-1$
	    this.piTimeScale = scale;
	    
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setPITimeScale(scale);
	    }
	}
	
	public void setTimescalingEnabled(boolean flag)
	{
	    this.timescalingEnabled = flag;
	    
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setTimescalingEnabled(flag);
	    }
	}
	
	public float getPITimeScale()
	{
	    if (this.timescalingEnabled)
	        return this.piTimeScale;
	    else
	        return 1f;
	}
	
	public Dimension getSize()
	{
		return new Dimension(this.sizeX, this.sizeY);
	}
	
	public void setVisualSize(int x, int y)
	{
		this.visualSizeX = x;
		this.visualSizeY = y;
		
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setVisualSize(x, y);
	    }
	}
	
	public Dimension getVisualSize()
	{
		return new Dimension(this.visualSizeX, this.visualSizeY);
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(this.preferredSizeX, this.preferredSizeY);
	}
	
	public void setSelectionStart(double start)
	{
        if (highResolution)
            selectionStart = start;
        else
            selectionStart = (int) (start / 100 + 0.5) * 100;
		
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setSelectionStart(selectionStart);
	    }
	}

	public void setSelectionEnd(double end)
	{
        if (highResolution)
            this.selectionEnd = end;
        else
            selectionEnd = (int) (end / 100 + 0.5) * 100;

	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setSelectionEnd(selectionEnd);
	    }
	}
    
    public void setHighResolution(boolean flag)
    {
        this.highResolution = flag;
    }
    
	public double getSelectionStart()
	{
		return this.selectionStart;
	}

	public double getSelectionEnd()
	{
		return this.selectionEnd;
	}
	
	public void setScale(double scaleX, double scaleY)
	{
//	    if (scaleX > 0)
//	        this.scaleX = scaleX;
	    if (scaleY > 0)
	        this.scaleY = scaleY;
	    
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setScale(scaleX, scaleY);
	    }
	}
	
	public double getScale()
	{
		return this.scale;
	}
	
	public void setScale(double scale)
	{
        this.scale = scale;
		
	    Enumeration sc = this.getGraphSubComponents();
	    while (sc.hasMoreElements())
	    {
	    	GenericTraceGraph gtc = (GenericTraceGraph)sc.nextElement();
	    	gtc.setScale(scale);
	    }
	}
	
	public void setToolTipText(String text)
	{
		this.parentComponent.setToolTipTextForGraphComponent(this, text);
	}
	
	public PICompositePanel getCompositePanel()
	{
		return this.parentComponent;
	}
	
	//new generic method to draw background
	public void drawDottedLineBackground(Graphics graphics, int yLegendSpace)
	{
		Rectangle visibleArea = getVisibleArea(graphics);
		int visY = this.getVisualSize().height;
		float visYfloat = visY - yLegendSpace;

		// draw the dotted lines
		graphics.setForegroundColor(ColorPalette.getColor(new RGB(200, 200, 200)));

		// vertical lines
		// float values will be slightly smaller than the actual result
		// and they will be incremented by one, since rounding to int
		// discards the remaining decimals
		int k = 0;
		for (float y = 0; k <= 10; y += visYfloat * 10000f / 100001f, k++)
		{
			for (int x = visibleArea.x; x <= visibleArea.x + visibleArea.width; x += 5)
			{
				if ((x / 5) % 2 == 0) graphics.drawLine(x, ((int)y) + 1, x + 5, ((int)y) + 1);
			}
		}

		// horizontal lines
		if (visibleArea.width > 0)
		{
			for (int x = visibleArea.x; x <= visibleArea.x + visibleArea.width; x += 50)
			{
				if (x % 100 == 0)
					graphics.setForegroundColor(ColorPalette.getColor(new RGB(100, 100, 100)));
				else
					graphics.setForegroundColor(ColorPalette.getColor(new RGB(200, 200, 200)));
				
				for (int y = 0; y < visY; y += 5)
				{
					if ((y / 5) % 2 == 0)
						graphics.drawLine(x, y, x, y + 5);
				}
			}
		}

		// draw the line indices
		graphics.setForegroundColor(ColorPalette.getColor(new RGB(100, 100, 100)));
		graphics.setBackgroundColor(ColorPalette.getColor(new RGB(255, 255, 255)));
		for (int x = visibleArea.x; x <= visibleArea.x + visibleArea.width; x += 50)
		{
			double time = (double) x;
			time = time * this.getScale();
            time += this.timeOffset;
			time = time / 1000;

			String stringTime = String.valueOf(time);
			if (stringTime.length() > 4)
			{
				int i;
				for (i = 0; i < stringTime.length(); i++)
					if (stringTime.charAt(i) == '.')
						break;
					
				if (i + 4 < stringTime.length())
					stringTime = stringTime.substring(0, i + 4);
			}
			graphics.drawString(stringTime + Messages.getString("GenericTraceGraph.seconds"), x + 5, visY - 13); //$NON-NLS-1$
	    }
	}
	
	public void drawSelectionSection(Graphics graphics, int yLegendSpace)
	{
        //draws the new selection
        this.drawSelection(graphics, selectionStart, selectionEnd, yLegendSpace);
 	}
    
    protected void drawSelection(Graphics graphics, double start, double end, int yLegendSpace)
    {
    	if (start > end) {
    		double tmp = end;
    		end = start;
    		start = tmp;
    	}

        double scale = getScale();

        int savedAlpha   = graphics.getAlpha();
		Color savedColor = graphics.getForegroundColor();
		
		// shade unselected area with a fixed alpha of darker gray
		Color selectColor = ColorPalette.getColor(new RGB(170, 170, 170));
		graphics.setBackgroundColor(selectColor);
		graphics.setAlpha(ALPHA_UNSELECTED);
		
		Point origin = this.parentComponent.getScrolledOrigin();
        
		int visX = this.getVisualSizeX();
		int visY = this.getVisualSizeY()- yLegendSpace;

		if (start != -1 && end != -1)
        {
            // mask with alpha adjusted the unselected area before the indicators
			if (origin.x < (start / scale) - SELECTION_BAR_WIDTH)
				graphics.fillRectangle(origin.x, 0,
						(int)(start / scale) - SELECTION_BAR_WIDTH - origin.x, visY);

			// mask with alpha adjusted the unselected area after the indicators
			if ((int)(end/scale + SELECTION_BAR_WIDTH) < origin.x + visX)
	           	graphics.fillRectangle((int)(end / scale + SELECTION_BAR_WIDTH), 0,
	           			origin.x + visX - (int)(end / scale + SELECTION_BAR_WIDTH), visY);

            // draw two indicators		
            graphics.setForegroundColor(ColorPalette.getColor(new RGB(255, 255, 0)));
            for (int i = 0; i < SELECTION_BAR_WIDTH; i++) {
            	graphics.drawLine((int)(start/scale) - i, 0, (int)(start/scale) - i, visY);
            	graphics.drawLine((int)(end/scale)   + i, 0, (int)(end/scale)   + i, visY);
            }
        }
		else
		{
            // mask the entire visible area with alpha adjusted
			graphics.fillRectangle(origin.x, 0, visX, visY);
		}

		//restore proper alpha
		graphics.setBackgroundColor(savedColor);
		graphics.setAlpha(savedAlpha);
	}
	
	protected void drawThreadMarks(ProfiledGeneric pg, int visY, Graphics graphics)
	{
		Rectangle visibleArea = getVisibleArea(graphics);
		int firstSample = (int)(pg.getFirstSample() / this.getScale());
		int lastSample  = (int)(pg.getLastSample()  / this.getScale());
		
		// Thread start mark
		if (firstSample >= visibleArea.x && firstSample <= visibleArea.x + visibleArea.width) {
			graphics.drawLine(firstSample,     (visY - 50), firstSample,     (visY - 40));
			graphics.drawLine(firstSample + 1, (visY - 50), firstSample + 1, (visY - 40));
			graphics.drawLine(firstSample - 1, (visY - 50), firstSample - 1, (visY - 40));
			graphics.drawLine(firstSample,(visY - 40), firstSample + 10, (visY - 40));
			graphics.drawLine(firstSample,(visY - 41), firstSample + 10, (visY - 41));
			graphics.drawLine(firstSample,(visY - 39), firstSample + 10, (visY - 39));
	
		}
		
		// Thread end mark
		if (lastSample >= visibleArea.x && lastSample <= visibleArea.x + visibleArea.width) {
			graphics.drawLine(lastSample,      (visY - 50), lastSample,      (visY - 40));
			graphics.drawLine(lastSample  + 1, (visY - 50), lastSample  + 1, (visY - 40));
			graphics.drawLine(lastSample  - 1, (visY - 50), lastSample  - 1, (visY - 40));
			graphics.drawLine(lastSample, (visY - 40), lastSample  - 10, (visY - 40));
			graphics.drawLine(lastSample, (visY - 41), lastSample  - 10, (visY - 41));
			graphics.drawLine(lastSample, (visY - 39), lastSample  - 10, (visY - 39));
		}
	}
	
	public void updateVisibleBorders()
	{
	    SashForm rect = this.parentComponent.getSashForm();	    
	    int scale = (int) this.getScale();
	    int correctionValue = 10;

	    // calculate display overscan to remove graphical glitches
        if (scale >= 10)
            correctionValue = 10;
        else if (scale < 10 && scale > 1)
	        correctionValue = 100 - (scale * 10);
	    else if ( scale <= 1 && scale > 0.3)
	        correctionValue = 200;
        else if ( scale <= 0.3)
            correctionValue = 800;

        Point origin = this.parentComponent.getScrolledOrigin();

		visibleLeftBorder  = origin.x - (int)(correctionValue * 1.2);
		visibleRightBorder = origin.x + rect.getBounds().width + (int)(correctionValue * 1.1);
	}
	
	public void genericRefreshCumulativeThreadTable()
	{
	    updateCumulativeThreadTableIsNeeded = true;
	}
	
	protected void genericRefreshCumulativeThreadTable(Enumeration<ProfiledGeneric> threads)
	{	
	  	int[] cumulativePerc = null;

	  	while (threads.hasMoreElements())
	  	{		
			ProfiledGeneric pg = threads.nextElement();

			// go through all samples
	        if (pg.isEnabled(graphIndex))   
	        {
	        	// zero this profiled generic's cumulative values
	            pg.setupCumulativeList(graphIndex);

				// Note: getSampleList() and getActivityList() create the lists
				int[] samples = pg.getSampleList();
				int[] values  = pg.getActivityList();

				if (samples == null || values == null || samples.length == 0)
				    return;
				
			  	if (cumulativePerc == null)
			  	{
			  		// create a zeroed array of cumulative values
			  		cumulativePerc = new int[samples.length];
			  	}

				for (int sampIndx = 0; sampIndx < samples.length; sampIndx++)
				{
					// get this profiled generic's sample count in each chunk of samples
					int thisValue = values[sampIndx];
				  	
					int oldCumValue = cumulativePerc[sampIndx];
							
					// add the percentage to the current cumulative value
					cumulativePerc[sampIndx] += thisValue;

					// set the profiled generic's cumulative value for this bucket
					pg.setCumulativeValue(graphIndex, sampIndx, oldCumValue);
				}
	        }
	  	}
	  	updatePolylinesIsNeeded = true;
	  	updateCumulativeThreadTableIsNeeded = false;
  	}
	
    private void updatePolyLinesGeneric(Enumeration enumer)
    {
        // updatePolylinesIsNeeded will be true if refreshCumulativeThreadTable has been invoked
	    /******************************************************************/
		if (!updatePolylinesIsNeeded)
		{
		    int visY = this.getVisualSize().height;
		    int newVisibleAreaIdentifier;
		    SashForm rect = this.parentComponent.getSashForm();	    
		    Point point = this.parentComponent.getScrolledOrigin();
		    int scale = (int) this.getScale();
		    
			int visibleLeft  = point.x;
			int visibleRight = point.x + rect.getBounds().width;
		    
			newVisibleAreaIdentifier = scale * 13 + visibleLeft + visibleRight * 2 + visY * 3;
	
			if (!(visibleAreaIdentifier == newVisibleAreaIdentifier))
			    updatePolylinesIsNeeded = true;
			
			visibleAreaIdentifier = newVisibleAreaIdentifier;
		}
		/**********************************************************/

		if (updatePolylinesIsNeeded)
			this.updatePolyLinesGeneric(enumer, (int) this.scaleY);
    }
	  
	private void updatePolyLinesGeneric(Enumeration enumer, int heightDivider)
	{
	    if (!updatePolylinesIsNeeded)
	        return;
	    
	    this.updateVisibleBorders();
		int visY = this.getVisualSize().height;
	  	
	  	float xscale;
	  	if (this.timescalingEnabled)
	  	    xscale = this.piTimeScale;
	  	else
	  	    xscale = 1;
	  	
	  	// draw one thread/binary/function at a time
	  	ProfiledGeneric pg = null;
	  	while (enumer.hasMoreElements())
	  	{
  	        pg = (ProfiledGeneric ) enumer.nextElement();
	  
	  		if (pg.isEnabled(graphIndex)) //is visualised
		  	{
	  		    if (debug)
	  		        System.out.println(Messages.getString("GenericTraceGraph.debug")+pg.getNameString()); //$NON-NLS-1$
	  		    
			  	// get samples and their corresponding values
			  	int[] samples = pg.getSampleList();  //time stamps
			  	int[] values = pg.getActivityList(); //percentage values
			  	
			  	// if a ProfiledGeneric has a null cumulative list, give
			  	// it a list of zeros
			  	if (pg.getCumulativeList(graphIndex) == null) {
			  		pg.setupCumulativeList(graphIndex);
			  	}

			  	int[] cumulatives = pg.getCumulativeList(graphIndex);

		  	    pg.resetPolyline(graphIndex);
			  	    
			  	double tmpScale = this.getScale();
			  	    
			  	// go through all samples
        
	            int thisSample, x, cumValue, thisValue;
	            int y = visY - 50;
	            
			    for (int sampIndx = 0; sampIndx < samples.length; sampIndx++)
				{
		            // the x value in thisSample is the timestamp
		            thisSample = samples[sampIndx];
		            x = (int)(thisSample / tmpScale / xscale);

		            // the y value is the percentage
		            thisValue = values[sampIndx];	
		            cumValue = cumulatives[sampIndx];
		            y = ((thisValue + cumValue) * (visY - 50)) / heightDivider;

                    pg.addPointToPolyline(graphIndex, x, (visY - 50) - y);
				} // for
	  		} //if (pg.isenabled)
	  	}//while (enum.hasmoreElements)
	  	updatePolylinesIsNeeded = false;
	}
	
	public void drawGraphsGeneric(Vector<ProfiledGeneric> profiledGenerics, Graphics graphics, Object[] selection)
	{
	    if (profiledGenerics.elements() == null)
	       return;
	    
	    // update cumulative thread table
	    if (this.updateCumulativeThreadTableIsNeeded)
	       this.genericRefreshCumulativeThreadTable(profiledGenerics.elements());
	    
	    // update polylines based on cumulative thread table
        this.updatePolyLinesGeneric(profiledGenerics.elements());
	    
	  	boolean threadIsSelected = false;
	  	int visY = this.getVisualSize().height;
	  	ProfiledGeneric pg;

  		GC gc = null;
  		boolean useImage = false;
  		int width = sizeX;
  		int height= sizeY;
  		// if image < 16KB and image can be allocated, use a pre drawn image instead of
  		// painting on draw2D graphics, scrolling and repaint from coming into focus
  		// are faster
  		if (width * height * 4 <= 16777216){	// 16KB image under 32bit color
  			if (getGraphImageChanged()) {
  			  	if (graphImage != null) {
  	  		  		graphImage.dispose();
  	  		  	}
  			  	try {
  			  		graphImage = new Image(Display.getDefault(), width, height);
  			  		gc = new GC(this.graphImage);
  			  		useImage = true;
  			  	} catch (SWTError e) {
  			  		// we cannot allocate any more image, stick to slow draw2D
  			  	}
  		  	} else {
  		  		useImage = true;
  		  	}
  		} else {
  			if (graphImage != null) {
  	  		  	graphImage.dispose();	// don't need this anymore, use draw2D
  	  		}
  		}
	  	
		if (useImage == false || getGraphImageChanged() == true) { // using graphics for paint or image needs update
		  	// draw one thread/binary/function at a time
		  	for (int i = profiledGenerics.size() - 1; i >= 0; i--)
		  	{
	  	        pg = profiledGenerics.get(i);
			  
		  		if (pg.isEnabled(this.graphIndex)) //is visualised
			  	{
				  	// get samples and their corresponding values
				  	int[] samples = pg.getSampleList();   //time stamps
				  	int[] values  = pg.getActivityList(); //percentage values
				  	int[] cumulatives = pg.getCumulativeList(this.graphIndex);

				  	if (this.fillSelected)  //selected items are filled, not all of them
				  	{
				  		threadIsSelected = selection.length > 0;
				  	}
				  	
					if (gc != null) {
						gc.setForeground(pg.getColor());
						gc.setBackground(pg.getColor());
					} else {
						graphics.setForegroundColor(pg.getColor());
			  			graphics.setBackgroundColor(pg.getColor());
					}
		  			drawGraph(pg, threadIsSelected, visY, graphics, gc, cumulatives, samples, values);			
		  		} //if (pg.isenabled)
		  	}//while (enum.hasmoreElements)
		}
	  	
	  	if (gc != null) {
	  		gc.dispose();
	  	}
	  	if (useImage) {
	  		graphics.drawImage(this.graphImage, 0, 0);
	  		setGraphImageChanged(false);
	  	}
	  	
	  	for (int i = profiledGenerics.size() - 1; i >= 0; i--) {
	  		pg = profiledGenerics.get(i);
	  		if (pg.isEnabled(this.graphIndex)) //is visualised
		  	{
	  			graphics.setForegroundColor(pg.getColor());
	  			graphics.setBackgroundColor(pg.getColor());
		  		drawThreadMarks(pg, visY, graphics);	
		  	}
	  	}
	}
	
	public boolean getGraphImageChanged() {
		return graphImageChanged;
	}
	
	public void setGraphImageChanged(boolean status) {
		graphImageChanged = status;
	}

	private int searchSortedPointListForX(PointList list, int xValue, boolean firstLowerIfNoMatch) {
		// bin search for now
		if (list.getFirstPoint().x == xValue) {
			return 0;
		}
		if (list.getLastPoint().x == xValue) {
			return list.size() - 1;
		}
		int start = 0;
		int end = list.size() - 1;
		int match = 0;
		while (match == 0) {
			// no match
			if (start + 1 == end) {
				if (firstLowerIfNoMatch) {
					return start;
				} else {
					return end;
				}
			}
			int middle = (start + end) / 2;
			int middleX = list.getPoint(middle).x;
			if (middleX == xValue) {
				match = middle;
			} else {
				if (middleX < xValue) {
					start = middle;
				} else {
					end = middle;
				}
			}
		}
		return match;
	}
	
	/*
	 *  this method draws a polyline or a polygon in the graph
	 *  
	 *  Note: it assumes all polylines have the same number of points 
	 */
	private void drawGraph(ProfiledGeneric pg, boolean threadIsSelected, int visY, Graphics graphics, GC gc,
	        int[] cumulatives, int[] samples, int[] values)
	{
		int pointCount = pg.getPointList(this.graphIndex).size();
		Rectangle drawArea;
		
		if (gc != null) {
			// draw one big picture including non-visible when drawing on image
			drawArea = new Rectangle(0, 0, sizeX, this.getVisibleArea(graphics).height);
		} else {
			drawArea = this.getVisibleArea(graphics);
		}
		
		if (pointCount < 2)	// these lusers really want to see a two pixel wide chart? what's wrong with them
			return;

		PointList allPointList = pg.getPointList(graphIndex);
		int xIndex = searchSortedPointListForX(allPointList, drawArea.x, true);
		PointList visiblePointList = new PointList();
		
		Point currentPoint = allPointList.getPoint(xIndex++);
		visiblePointList.addPoint(currentPoint);
		
		currentPoint = allPointList.getPoint(xIndex++);
		visiblePointList.addPoint(currentPoint);
		
		Point peakPointInVertical = null;
		Point troughPointInVertical = null;
		Point lastPointInVertical = null;
		
		while (xIndex < allPointList.size()) {
			// only draw visible area
			if (currentPoint.x >= drawArea.x + drawArea.width) {
				break;
			}
			int lastX = currentPoint.x;
			currentPoint = allPointList.getPoint(xIndex++);
			//optimized polyline and draw slighly inaccurate graph for those pixel fine movement, 
			//only account for peak/valley if x doesn't move(e.g. vertical lines)
			
			if (lastX == currentPoint.x) {
				if (lastPointInVertical == null) {
					// seen first point in the vertical line 
					peakPointInVertical = troughPointInVertical = visiblePointList.getLastPoint();
				}
				if (troughPointInVertical.y > currentPoint.y) {
					troughPointInVertical = currentPoint;
				} else if (peakPointInVertical.y < currentPoint.y) {
					peakPointInVertical = currentPoint;
				}
				lastPointInVertical = currentPoint;
			}
			else {
				// we just write two points of peak and trough of the vertical line if needed and the last
				// if we seen peak and trough, this way we can render the conceptually inaccurate graph
				// we less point, but still show the same vertical line on screen
				if (lastPointInVertical != null) {
					boolean seenPeakOrTrough = false;
					if (lastPointInVertical.y != peakPointInVertical.y) {
						visiblePointList.addPoint(peakPointInVertical);
						seenPeakOrTrough = true;
					}
					if (lastPointInVertical.y != troughPointInVertical.y) {
						visiblePointList.addPoint(troughPointInVertical);
						seenPeakOrTrough = true;
					}
					if (seenPeakOrTrough) {
						visiblePointList.addPoint(lastPointInVertical);
					}
					peakPointInVertical = null;
					troughPointInVertical = null;
					lastPointInVertical = null;
				}
				visiblePointList.addPoint(currentPoint);
			}
		}

		if (fillFlag)
		{
			// close the bottom parameter
			visiblePointList.addPoint(new Point(visiblePointList.getLastPoint().x, visY - 50));
			visiblePointList.addPoint(new Point(visiblePointList.getFirstPoint().x, visY - 50));

			if (gc != null) {
				gc.fillPolygon(visiblePointList.toIntArray());
			} else {
				graphics.fillPolygon(visiblePointList.toIntArray());
			}
		}
		else
		{
			if (gc != null) {
				gc.drawPolyline(visiblePointList.toIntArray());
			} else {
				graphics.drawPolyline(visiblePointList.toIntArray());
			}
		}
		
		//creates the first polyListX and polyListY
		if (fillFlag && (!threadIsSelected && this.fillSelected))
	  	{
		    this.resetPolyList(pg, visY, samples, values, cumulatives);
	  	}
	}		

	//this method is used to reset the list of points
	private void resetPolyList(ProfiledGeneric pg, int visY, int[] samples, int[] values, int[] cumulatives)
	{
		pg.resetPolyline(graphIndex);
			
		int thisValue, cumValue, thisSample, x;
		int y = (visY - 50);
		double tmpScale = this.getScale();
		for (int sampIndx = 0; sampIndx < samples.length; sampIndx++)
		{
		  	// the x value in thisSample is the timestamp
		  	thisSample = samples[sampIndx];
		  	x = (int)(thisSample / tmpScale);
												
            if (x > visibleLeftBorder && x < visibleRightBorder) //draws only visible stuff
            {
   			  	// the y value is the percentage
   			  	thisValue = values[sampIndx];	
   			  	cumValue = cumulatives[sampIndx];
   			  	y = ((thisValue + cumValue) * (visY - 50)) / 100;
	    			  	
                pg.addPointToPolyline(graphIndex, x, (visY - 50) - y);
            }
            else if (x >= visibleRightBorder)  //optimises end drawing
            {
                pg.addPointToPolyline(graphIndex, x, visY - 50);
                sampIndx = samples.length; //breaks the for loop
            }
		} // for
	}
	
	public void updateIfNeeded(Vector<ProfiledGeneric> profiledGenerics)
	{
	    // updates cumulative thread table
	    if (this.updateCumulativeThreadTableIsNeeded)
	       this.genericRefreshCumulativeThreadTable(profiledGenerics.elements());
	    
	    // updates polylines based on cumulative thread table
        this.updatePolyLinesGeneric(profiledGenerics.elements());
	}
	
	public int getVisibleRightBorder()
	{
		return this.visibleRightBorder;
	}

	public int getVisibleLeftBorder()
	{
		return this.visibleLeftBorder;
	}

	public int getVisualSizeX()
	{
		return this.visualSizeX;
	}

	public int getVisualSizeY()
	{
		return this.visualSizeY;
	}
}
