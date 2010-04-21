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
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;

import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph.EventTypes;
import com.nokia.s60tools.ui.IImageProvider;
import com.nokia.s60tools.ui.actions.CopyImageToClipboardAction;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Graph to be shown in Analysis tab.
 */
public class LinearIssuesGraph extends ZoomableGraph implements MouseMoveListener, IImageProvider {
	
	private static DecimalFormat MBformat = new DecimalFormat("#####.0");
	private static DecimalFormat Bytes_Format = new DecimalFormat("#####.##");
	
	private HashMap<ResultElements, Polyline> pointsData = new HashMap<ResultElements, Polyline>();
	
	private Composite parentComposite;
	private FigureCanvas yAxis;
	private FigureCanvas numberAxis;
	private FigureCanvas figureCanvas;
	private int[] timeSamples;
	private ArrayList<ResultElements> selectedIssues;
	private boolean scalingNeeded;
	
	private double visY;
	private int maxBytes = 10;
	private int maxCount = 10;
	private double scale = 1.0;
	private int timeOffset = 0;
	
	/**
	 * Construction
	 * @param composite
	 */
	public LinearIssuesGraph(Composite composite)
	{
		this.parentComposite = composite;
	}
	
	/**
	 * Draw the area
	 */
	public void constructGraphArea()
	{		
		if(parentComposite == null)
			return;
		
		Control [] children = parentComposite.getChildren();
		
		if(children != null)
		{
			for(Control child:children)
				child.dispose();
		}

		calculateMultiplier();
	    scalingNeeded = true;
		
		Composite parent = new Composite(parentComposite, SWT.NONE);
		parent.setLayout(new FormLayout());
			
		yAxis = new FigureCanvas(parent);
				
		numberAxis = new FigureCanvas(parent);
		figureCanvas = new FigureCanvas(parent);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.width  = 50;
		numberAxis.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(numberAxis);
		formData.width  = 60;
		yAxis.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(yAxis, 0, SWT.RIGHT);
		formData.right  = new FormAttachment(100);
		
		figureCanvas.setLayoutData(formData);
			
		yAxis.setBackground(ColorConstants.white);
		numberAxis.setBackground(ColorConstants.white);
		
		yAxis.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent event) {
				GC localGC = event.gc;
				
				org.eclipse.swt.graphics.Rectangle rect = figureCanvas.getClientArea();
				
				visY  = rect.height - CommonGraphConstants.XLEGENDSPACE;
						
				double yIncrement = visY / 10;
				int previousBottom = 0;
				
				double multiplier = GraphsUtils.prettyMaxBytes(maxBytes)/visY;
				
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
										
					Point extent = localGC.stringExtent(legend);
					
					localGC.drawLine(CommonGraphConstants.YLEGENDSPACE - 3, (int)y + 1, 60, (int)y + 1);
					
					if (y >= previousBottom)
					{
						localGC.drawString(legend, 60 - extent.x -2, (int)y);
						previousBottom = (int)y + extent.y;
					}
				}
				localGC.setLineWidth(2);
				localGC.drawLine(CommonGraphConstants.YLEGENDSPACE, 0, 60, rect.height);
							
				final Image image = GraphsUtils.getDoubleYAxisVerticalLabel("Bytes");
				
			    localGC.setAdvanced(true);
			    final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
		        Transform transform = new Transform(Display.getDefault());

		        transform.translate(rect2.height / 2f, rect2.width / 2f);
		        transform.rotate(-90);
		        transform.translate(-rect2.width / 2f, -rect2.height / 2f);

		        localGC.setTransform(transform);
		        localGC.drawImage(image, -(int)visY/3, 1);
		        
		        transform.dispose();
		        localGC.dispose();
			}
		});
		
		numberAxis.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent event) {
				
				GC localGC = event.gc;
				org.eclipse.swt.graphics.Rectangle rect = figureCanvas.getClientArea();
				visY  = rect.height - CommonGraphConstants.XLEGENDSPACE;
				double yIncrement = visY / 10;
				int previousBottom = 0;
				double multiplier = GraphsUtils.roundToNearestNumber(maxCount)/visY;
				for (int k = 10; k >= 0; k--)
				{
					// location for the value indicator is k * 1/10 the height of the display
					int y = (int) (visY - (yIncrement * k));
					int yValue = (int)(yIncrement * multiplier) * k;
					String legend = yValue + "";
					Point extent = localGC.stringExtent(legend);
					localGC.drawLine(50 - 3, (int)y + 1, 50, (int)y + 1);
					if (y >= previousBottom)
					{
						localGC.drawString(legend, 50 - extent.x -2, (int)y);
						previousBottom = (int)y + extent.y;
					}
				}
				localGC.setLineWidth(2);
				localGC.drawLine(50, 0, 50, rect.height);
				
				final Image image = GraphsUtils.getDoubleYAxisVerticalLabel("Count");
				localGC.setAdvanced(true);
			    final org.eclipse.swt.graphics.Rectangle rect2 = image.getBounds();
		        Transform transform = new Transform(Display.getDefault());

		        transform.translate(rect2.height / 2f, rect2.width / 2f);
		        transform.rotate(-90);
		        transform.translate(-rect2.width / 2f, -rect2.height / 2f);

		        localGC.setTransform(transform);
		        localGC.drawImage(image, -(int)visY/3, 10);
		        
		        transform.dispose();
		        localGC.dispose();
		        
			}
		});
		
		
		figureCanvas.setBackground(ColorConstants.white);
		Panel panel = new Panel()
		{
			public void paint(Graphics graphics)
			{
//				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "LinearIssuesGraph/Panel/paint START");
				
				drawBackGroundLines(figureCanvas,graphics);
				paintData(graphics);
				
//				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "LinearIssuesGraph/Panel/paint END");				
			}
		};
		
		panel.setLayoutManager(new FlowLayout());
		figureCanvas.setContents(panel);
		figureCanvas.addMouseMoveListener(this);
		
		final ScrollBar horizontalBar = figureCanvas.getHorizontalBar();
		horizontalBar.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent arg0) {
			
			}

			public void widgetSelected(SelectionEvent event) {
				timeOffset = figureCanvas.getViewport().getViewLocation().x;
				figureCanvas.redraw();
			}
			
		});
		
		figureCanvas.addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent e) {
				horizontalBar.setPageIncrement(figureCanvas.getBounds().width);
				
				if(scalingNeeded)
				{
					zoomGraph();
					scalingNeeded = false;
				}
				//setNewSize();
				yAxis.redraw();
				numberAxis.redraw();
				figureCanvas.redraw();
				
			}
		});
	
		zoomGraph();
		figureCanvas.redraw();
		hookContextMenu();
		parentComposite.layout(true);
	}
	
	/**
	 * Draw background lines on the canvas
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
			for(int x = canvasRect.x; x <= canvasRect.x + canvasRect.width; x += 5)
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
		Image img = GraphsUtils.getDoubleYAxisVerticalLabel(GenericGraph.TIME_X_AXIS_LABEL);
		graphics.drawImage(img, width/2, height-30);

	}
	
	private void paintData(Graphics graphics)
	{
		DbgUtility.println(DbgUtility.PRIORITY_LOOP, this.getClass().getSimpleName() + "/paintData START");
		
		try
		{
			Rectangle canvasRect = graphics.getClip(new org.eclipse.draw2d.geometry.Rectangle());
		
			if(this.selectedIssues == null || selectedIssues.size() == 0)
				return;
		
			int [] listX = timeSamples;
			int k = 0;
		
			calculateMultiplier();
				
			// Storing original graphics settings
			Color origColor = graphics.getForegroundColor();
			int origLineWidth = graphics.getLineWidth();
			// Setting line width for drawing the graph
			graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);				
		
			for(ResultElements item:selectedIssues)
			{
				long [] valuesToBePlotted = item.getEventValues();
			
				if(valuesToBePlotted == null)
					continue;
			
				double visY = canvasRect.height - CommonGraphConstants.XLEGENDSPACE;
				double multiplier = 1;
				
				EventTypes eventType = GraphsUtils.getMappedEvent(item.getEvent());
					
				switch (eventType)
				{
					case NO_OF_FILES:
					case NO_OF_PSHANDLES:
					case HEAP_ALLOC_CELL_COUNT:
					case HEAP_FREE_CELL_COUNT:
					case SYSTEM_DATA:
						multiplier = GraphsUtils.roundToNearestNumber(maxCount)/visY;
						break;
					default:
						multiplier = GraphsUtils.prettyMaxBytes(maxBytes)/visY;
						break;
				}
			
				List<Point> pointsList = new ArrayList<Point>();
				 
				for (int i = 0; i < valuesToBePlotted.length; i++)
				{
			    	// Showing only positive values
			    	if(valuesToBePlotted[i] > 0){
			    		int scaledXPoint = (int)(listX[i]/scale);
			    		int scaledYPoint = (int) (visY - valuesToBePlotted[i] /multiplier);;
				    	Point p = new Point(scaledXPoint, scaledYPoint);
				    	pointsList.add(p);			    		
			    	}
				}
			 
				// Converting point list into integer array for drawing
				int[] points = GraphsUtils.convertPointListToIntArray(pointsList);
				
				graphics.setForegroundColor(item.getColor());
				
				// Drawing graph
				graphics.drawPolyline(points);				
				// Drawing markers to the data points
				GraphsUtils.drawMarkers(graphics, points);
			
				Polyline line = new Polyline();
				line.setPoints(new PointList(points));
				pointsData.put(item, line);
					
				k++;
			}

			// Restoring original settings
			graphics.setForegroundColor(origColor);
			graphics.setLineWidth(origLineWidth);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void calculateMultiplier()
	{
		maxCount = 10;
		maxBytes = 10;
		
		if(selectedIssues == null)
			return;
		
		for(ResultElements item:selectedIssues)
		{
			long [] valuesToBePlotted = item.getEventValues();
		
			if(valuesToBePlotted == null)
				return;
			
			int maxValue = (int)calculateMaxValue(valuesToBePlotted);
			
			EventTypes eventType = GraphsUtils.getMappedEvent(item.getEvent());
			
			switch (eventType)
			{
				case NO_OF_FILES:
				case NO_OF_PSHANDLES:
				case HEAP_ALLOC_CELL_COUNT:
				case HEAP_FREE_CELL_COUNT:
				case SYSTEM_DATA:
					if(maxValue > maxCount)
						maxCount = maxValue;
					break;
				default:
					if(maxValue > maxBytes)
						maxBytes = maxValue;
					break;
			}
		}
		
	}
	/**
	 * Adds Pop-Up menu items on th graph area.
	 *
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
        });
	    Menu menu = menuMgr.createContextMenu(figureCanvas);
	    figureCanvas.setMenu(menu);
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		zoomIn = new Action()
		{
			public void run()
			{
				zoomIn();
			}
			{
				this.setText(ZOOM_IN_CONTEXT_MENU_TITLE);
			}
		};
		
		zoomOut = new Action()
		{
			public void run()
			{
				zoomOut();
			}
			{
				this.setText(ZOOM_OUT_CONTEXT_MENU_TITLE);
			}
		};
		IAction showEntireGraph = new Action()
		{
			public void run()
			{
				zoomGraph();
			}
			{
				this.setText(CommonGraphConstants.SHOW_ENTIRE_GRAPH_CONTEXT_MENU_ITEM_TITLE);
			}
		};
		
		IAction saveGraph = new Action()
		{
			public void run()
			{
				GraphsUtils.saveGraph(parentComposite);
			}
			{
				this.setText(CommonGraphConstants.SAVE_GRAPH_CONTEXT_MENU_ITEM_TITLE);
			}
		};
		
		copy = new CopyImageToClipboardAction(this);		

		manager.add(zoomIn);
		manager.add(new Separator());
		manager.add(zoomOut);
		manager.add(new Separator());
		manager.add(showEntireGraph);
		manager.add(saveGraph);
		manager.add(copy);

		// Finally updating action states
		updateViewActionEnabledStates();

	}

	/**
	 * Sets enabled/disabled states for actions commands
	 * on this view, based on the current application state.
	 * This method should be called whenever an operation is
	 * started or stopped that might have effect on action 
	 * button states.
	 */
	private void updateViewActionEnabledStates() {
		// Zoom In
		setEnableState(zoomIn, !(this.scale == GraphsUtils.nextScale(scale, false)));
		
		// Zoom Out
		boolean zoomOutEnableCondition1 = !(this.scale == GraphsUtils.nextScale(scale, true));
		int width = figureCanvas.getClientArea().width;		
		int lastTimeSample = timeSamples[timeSamples.length - 1];
		boolean zoomOutEnableCondition2 = !(lastTimeSample / this.scale <= width);
		setEnableState(zoomOut, zoomOutEnableCondition1 && zoomOutEnableCondition2);		
	}

	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.ZoomableGraph#zoomIn()
	 */
	protected void zoomIn()
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "LinearIssuesGraph - Zoom In");
		//Preconditions checked already in updateViewActionEnabledStates
		this.scale = GraphsUtils.nextScale(scale, false);
		figureCanvas.redraw();
		setNewSize();
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.ZoomableGraph#zoomOut()
	 */
	protected void zoomOut()
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "LinearIssuesGraph - Zoom Out");
		//Preconditions checked already in updateViewActionEnabledStates
		this.scale = GraphsUtils.nextScale(this.scale, true);
		setNewSize();
	
	}
	/**
	 * This method zoom in the graph area to the maximum possible scale
	 * and then zooms out, so that it fits in the canvas area
	 */
	public void zoomGraph()
	{		
		int width = figureCanvas.getClientArea().width;
		
		if(width <=0)
			return;
		
		double new_scale = this.scale;
		
		double prevNew  = new_scale;
		
		int lastSampleTime = timeSamples[timeSamples.length - 1];
		//first zoom in until it is too big to fit
		while (lastSampleTime / new_scale <= width){
			new_scale = GraphsUtils.nextScale(new_scale, false);
			
			if(prevNew == new_scale)
				break;
			
			prevNew = new_scale;
			
		}
		// now zoom out until it just fits
		while (lastSampleTime / new_scale > width){
			new_scale = GraphsUtils.nextScale(new_scale, true);
			
			if(prevNew == new_scale)
				break;
			
			prevNew = new_scale;
		}
		
		if (new_scale == this.scale)
			return;
	
		this.scale = new_scale;
		
		setNewSize();
	}
	
	
	/**
	 * This method sets the size of the panel, when scale is changed.  
	 * When graph extends beyond visible area, horizontal scroll bar appears automatically.
	 *
	 */
	private void setNewSize()
	{
		int lastSample = timeSamples[timeSamples.length - 1];
		
		int prefSize = (int)(lastSample/scale);
	
		timeOffset = 0;	
		Panel panel = (Panel)(figureCanvas.getContents());
		panel.setPreferredSize(prefSize + 100, 0);
		
		if (prefSize >= figureCanvas.getClientArea().width) {
			timeOffset = figureCanvas.getViewport().getViewLocation().x;
	    	panel.setSize(prefSize + 100, 0);
	    }
		
	}

	/**
	 * Set issues that user has been selected
	 * @param selectedIssues
	 */
	public void setSelectedIssues(ArrayList<ResultElements> selectedIssues) {
		this.selectedIssues = selectedIssues;
	}
	
	/**
	 * Set parsed data
	 * @param logsData
	 */
	public void setLogData(ParsedData logsData)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		timeSamples = utils.getTimeIntervalsFromLogData(logsData);
	}
	
	private long calculateMaxValue(long [] values)
	{
		long maxValue = 0;
		
		for(int i=0; i<values.length; i++)
		{
			if(values[i] > maxValue)
				maxValue = values[i];
		}
		
		return maxValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {
		int x = event.x;
		int y = event.y;
		
		if(y > (int)visY)
			figureCanvas.setToolTipText(null);
		
		String text = "";
		double xValue = x + timeOffset;
		int scaledX = (int)(xValue * scale);
		double valY = visY - y;
					
		double count_multiplier = GraphsUtils.roundToNearestNumber(maxCount)/visY;
		double bytes_multiplier = GraphsUtils.prettyMaxBytes(maxBytes)/visY;
		
		String scaledY_1 = getFormattedValues(valY * bytes_multiplier);
		int scaledY_2 = (int)(Math.round((valY * count_multiplier)));
		
		text += scaledX + "s " + scaledY_1 + " , " + scaledY_2 ;
		
		if(selectedIssues != null)
		{
			for(ResultElements item:selectedIssues)
			{
				Polyline line = pointsData.get(item);
		
				if(line != null && line.containsPoint(x, y))
				{
					text += "\n" + item.getItemName();
					text +=  " -- " + item.getEvent();
				}
			}
		}
					
		figureCanvas.setToolTipText(text);
		figureCanvas.redraw();
	}
	
	/**
	 * 
	 * @param bytes represents value to be formatted.
	 * @return formatted value 
	 */
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

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.actions.IImageProvider#getImage()
	 */
	public Image getImage() {
		return new Image(Display.getCurrent(), parentComposite.getClientArea().width, parentComposite.getClientArea().height);
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.actions.IImageProvider#getDrawable()
	 */
	public Drawable getDrawable() {
		return parentComposite;
	}	
	

}
