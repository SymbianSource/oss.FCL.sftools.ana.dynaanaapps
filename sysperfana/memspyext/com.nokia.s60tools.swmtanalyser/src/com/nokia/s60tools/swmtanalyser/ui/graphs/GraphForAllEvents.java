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

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.swmtanalyser.editors.GraphedItemsInput;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph.EventTypes;
import com.nokia.s60tools.ui.IImageProvider;
import com.nokia.s60tools.ui.actions.CopyImageToClipboardAction;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Graphs to be shown in Graphed items -tab when Graphs-tab is active. 
 */
public class GraphForAllEvents extends ZoomableGraph implements MouseMoveListener, IImageProvider {

	//
	// Constants
	//
	
	/**
	 * Marking data points that are not really drawn with zero values.
	 */
	private static final int NOT_DRAWN_DATA_POINT = -1; 
	
	//
	// Members
	//
	private Composite parentComposite;
	
	private HashMap<String, ArrayList<ThreadData>> heapData = new HashMap<String, ArrayList<ThreadData>>();
	private HashMap<String, ArrayList<GlobalDataChunks>> globalDataChunks = new HashMap<String, ArrayList<GlobalDataChunks>>();
	private HashMap<String, ArrayList<ChunksData>> nonHeapChnksData = new HashMap<String, ArrayList<ChunksData>>();
	private HashMap<String, ArrayList<DiskOverview>> disksData = new HashMap<String, ArrayList<DiskOverview>>();
	private HashMap<GraphedItemsInput, int []> valuesForSelectedItems = new HashMap<GraphedItemsInput, int[]>();
	private HashMap<GraphedItemsInput, Polyline> pointsData = new HashMap<GraphedItemsInput, Polyline>();
	
	private ArrayList<SystemData> systemData = new ArrayList<SystemData>();
	private ArrayList<KernelElements> kernelData = new ArrayList<KernelElements>();
	
	private ArrayList<GraphedItemsInput> graphedItemsInput = new ArrayList<GraphedItemsInput>();  
	private ParsedData parsedData = new ParsedData();

	private FigureCanvas yAxis;
	private int maxBytes = 10;
	private int maxCount = 10;
	private double scale = 1.0;
	private int timeOffset = 0;
	
	private double visY;
	private FigureCanvas figureCanvas;
	
	private static DecimalFormat MBformat = new DecimalFormat("#####.0");
	private static DecimalFormat Bytes_Format = new DecimalFormat("#####.##");
	
	private int [] timeSamples;
	private int lastTimeSample;
	private FigureCanvas numberAxis;
	
	private boolean scalingNeeded;

	private IAction showEntireGraph;
	private IAction saveGraph;
	
	/**
	 * Constructor
	 * @param composite
	 */
	public GraphForAllEvents(Composite composite)
	{
		this.parentComposite = composite;
	}
	
	/**
	 * Set graphed items
	 * @param input
	 */
	public void setGraphedItemsInput(ArrayList<GraphedItemsInput> input)
	{
		this.graphedItemsInput = input;
	}
	/**
	 * Set heap data
	 * @param thName
	 * @param values
	 */
	public void setHeapSizeForThread(String thName, ArrayList<ThreadData> values)
	{
		heapData.put(thName, values);
	}
	
	/**
	 * Set global data chunks
	 * @param chnkName
	 * @param values
	 */
	public void setGlobalChunkSizeForChunk(String chnkName, ArrayList<GlobalDataChunks> values) 
	{
		globalDataChunks.put(chnkName, values);
	}
	
	/**
	 * Set non heap chunk data
	 * @param chnkName
	 * @param values
	 */
	public void setNonHeapChunkSizeForChunk(String chnkName, ArrayList<ChunksData> values)
	{
		nonHeapChnksData.put(chnkName, values);
	}
	
	/**
	 * Set disk data
	 * @param diskName
	 * @param values
	 */
	public void setDiskData(String diskName, ArrayList<DiskOverview> values)
	{
		disksData.put(diskName, values);
	}
	
	/**
	 * Set cycles data
	 * @param cyclesData
	 */
	public void setInputCyclesData(ParsedData cyclesData)
	{
		this.parsedData = cyclesData;
		timeSamples = this.calculateTimeIntervals();
		lastTimeSample = timeSamples[timeSamples.length-1];
	}
	
	/**
	 * Set kernel data
	 * @param kernelData
	 */
	public void setKernelData(ArrayList<KernelElements> kernelData)
	{
		this.kernelData = kernelData;
	}
	
	/**
	 * Set System data
	 * @param sysData
	 */
	public void setSystemData(ArrayList<SystemData> sysData)
	{
		this.systemData = sysData;
	}
	
	/**
	 * Create Graphed items area
	 */
	public void constructGraphArea()	
	{		
		//Prepare data
		prepareData();
		
		if(parsedData == null || parentComposite == null)
			return;
		
		Control [] children = parentComposite.getChildren();
		
		if(children != null)
		{
			for(Control child:children)
				child.dispose();
		}
	
		scalingNeeded = true;
		timeOffset = 0;
		
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
						
				int countOfYAxisLabels = 10;
				double yIncrement = visY / countOfYAxisLabels;
				int previousBottom = 0;
				
				double multiplier = GraphsUtils.prettyMaxBytes(maxBytes)/visY;
				
				for (int k = countOfYAxisLabels; k >= 0; k--)
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
				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "GraphForAllEvents/Panel/paint START");
				
				drawBackGroundLines(figureCanvas,graphics);
				paintData(graphics);
				
				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "GraphForAllEvents/Panel/paint END");
				
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
				yAxis.redraw();
				numberAxis.redraw();
				figureCanvas.redraw();
				
			}
		});
		
		zoomGraph();
		hookContextMenu();
		parentComposite.layout(true);
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
		showEntireGraph = new Action()
		{
			public void run()
			{
				zoomGraph();
			}
			{
				this.setText(CommonGraphConstants.SHOW_ENTIRE_GRAPH_CONTEXT_MENU_ITEM_TITLE);
			}
		};
		
		saveGraph = new Action()
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
		setEnableState(zoomIn, !(this.scale == GraphsUtils.nextScale(scale, false)));		
		boolean zoomOutEnableCondition1 = !(this.scale == GraphsUtils.nextScale(scale, true));
		boolean zoomOutEnableCondition2 = !(this.lastTimeSample / this.scale <= figureCanvas.getClientArea().width);
		setEnableState(zoomOut, zoomOutEnableCondition1 && zoomOutEnableCondition2);		
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.ZoomableGraph#zoomIn()
	 */
	protected void zoomIn()
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "GraphForAllEvents - Zoom In");
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
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "GraphForAllEvents - Zoom Out");
		//Preconditions checked already in updateViewActionEnabledStates
		this.scale = GraphsUtils.nextScale(this.scale, true);
		setNewSize();	
	}
	
	/**
	 * This method zoom in the graph area to the maximum possible scale
	 * and then zooms out, so that it fits in the canvas area
	 */
	private void zoomGraph()
	{
		int width = figureCanvas.getClientArea().width;
		
		if(width <=0 )
			return;
		
		double new_scale = this.scale;
	
		double prevNew  = new_scale;
		
		//first zoom in until it is too big to fit
		while (this.lastTimeSample / new_scale <= width)
		{
			new_scale = GraphsUtils.nextScale(new_scale, false);
		
			if(prevNew == new_scale)
				break;
			
			prevNew = new_scale;
		}
		// now zoom out until it just fits
		while (this.lastTimeSample / new_scale > width)
		{
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
	 */
	private void setNewSize()
	{
		int lastSample = this.lastTimeSample;
		
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
	 * @param canvas
	 * @param graphics
	 */
	private void drawBackGroundLines(FigureCanvas canvas, Graphics graphics)
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
		
		Rectangle canvasRect = graphics.getClip(new org.eclipse.draw2d.geometry.Rectangle());
		
		if(this.graphedItemsInput == null)
			return;
		
		int [] listX = timeSamples;
				
		for(GraphedItemsInput item:graphedItemsInput)
		{
			int [] valuesToBePlotted = valuesForSelectedItems.get(item);
			
			if(valuesToBePlotted == null)
				continue;
			
			int [] points = new int[valuesToBePlotted.length *2];
			 
			double visY = canvasRect.height - CommonGraphConstants.XLEGENDSPACE; 
			
			EventTypes eventType = GraphsUtils.getMappedEvent(item.getEvent());
			
			double multiplier = 1;
			boolean isThreadEvent = false;
			boolean isChunkEvent = false;
			
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
			
			switch(eventType)
			{
				case NO_OF_FILES:
				case NO_OF_PSHANDLES:
				case MAX_HEAP_SIZE:
				case HEAP_SIZE:
				case HEAP_ALLOC_CELL_COUNT:
				case HEAP_FREE_CELL_COUNT:
				case HEAP_ALLOC_SPACE:
				case HEAP_FREE_SLACK:
				case HEAP_FREE_SPACE:
					isThreadEvent = true;
					break;
				case GLOBAL_DATA_SIZE:
				case NON_HEAP_CHUNK_SIZE:
					isChunkEvent = true;
					break;
			}
			
			// Scaling values before actual drawing
			 for (int i = 0, j = 0; i < valuesToBePlotted.length; i++, j++)
			 {
				// Scaling X-coordinate value 
			    int x_point = (int)(listX[i]/scale);
			    
				// Scaling positive Y-coordinate value and ... 
			    int y_point =  NOT_DRAWN_DATA_POINT; // ... zero and negative ones are not drawn
				if (valuesToBePlotted[i] > 0){
				    y_point =  (int) (visY - valuesToBePlotted[i] /multiplier);
				}

				 // Storing value
				 points[j] = x_point;
				 points[++j] = y_point;
			 }
			 
			 // Storing original values
			 Color origColor = graphics.getForegroundColor();
			 int origLineWidth = graphics.getLineWidth();
			 int origLineStyle = graphics.getLineStyle();
			 
			 graphics.setForegroundColor(item.getColor());
			 graphics.setLineWidth(CommonGraphConstants.DEFAULT_GRAPH_LINE_WIDTH);
			 
			 if(isThreadEvent){
					paintThreadEvents(graphics, points, item.getName());
				}
			 else if(isChunkEvent)
			 {
				 paintChunkEvents(graphics, points, item.getName(), item.getEvent());
			 }
			 else{
				 graphics.drawPolyline(points);
			 }
			 
			 // Drawing markers to the data points
			 GraphsUtils.drawMarkers(graphics, points);
			 
			 Polyline line = new Polyline();
			 line.setPoints(new PointList(points));
			 pointsData.put(item, line);
			
			 // Restoring original values
			 graphics.setForegroundColor(origColor);
			 graphics.setLineWidth(origLineWidth);		
			 graphics.setLineStyle(origLineStyle);
		}
				 
		DbgUtility.println(DbgUtility.PRIORITY_LOOP, this.getClass().getSimpleName() + "/paintData END");
	}

	/**
	 * prepare data for drawing
	 */
	private void prepareData()
	{
		maxBytes = 10;
		maxCount = 10;
		
		for(GraphedItemsInput obj:graphedItemsInput)
		{
			String itemName = obj.getName();
			String event = obj.getEvent();
			
			int [] values = getValuesForGivenItemAndEvent(itemName, event);
						
			if(values == null)
			{
				continue;
			}
			valuesForSelectedItems.put(obj, values);
			
			int maxValue = calculateMaxValue(values);
			 
			 EventTypes eventType = GraphsUtils.getMappedEvent(event);
			 
			 switch(eventType)
			 {
			 	case NO_OF_FILES:
			 	case NO_OF_PSHANDLES:
			 	case HEAP_ALLOC_CELL_COUNT:
				case HEAP_FREE_CELL_COUNT:
			 	case SYSTEM_DATA:
			 		if(maxValue > maxCount){
			 			maxCount = maxValue;
			 		}
			 		break;
			 	default:
			 		if(maxValue > maxBytes){
			 			maxBytes = maxValue;			 			
			 		}
			 		break;
			 }
		}
	}
	
	private int [] getValuesForGivenItemAndEvent(String itemName, String event)
	{
		EventTypes eventType = GraphsUtils.getMappedEvent(event);
		int [] valuesForSelectedItem = null;
		
		switch(eventType)
		{	
			case GLOBAL_DATA_SIZE:
				valuesForSelectedItem = getGlobalChunkSize(itemName, globalDataChunks);
				break;
			case NON_HEAP_CHUNK_SIZE:
				valuesForSelectedItem = getNonHeapChunkSize(itemName, nonHeapChnksData);
				break;
			case DISK_USED_SIZE:
				valuesForSelectedItem = getUsedDiskSize(itemName, disksData);
				break;
			case DISK_TOTAL_SIZE:
				valuesForSelectedItem = getTotalDiskSize(itemName, disksData);
				break;
			case NO_OF_FILES:
				valuesForSelectedItem = getAllFiles(itemName, heapData);
				break;
			case MAX_HEAP_SIZE:
				valuesForSelectedItem = getMaxHeapSize(itemName, heapData);
				break;
			case HEAP_SIZE:
				valuesForSelectedItem = getHeapSize(itemName, heapData);
				break;
			case HEAP_ALLOC_SPACE:
				valuesForSelectedItem = getHeapAllocSpace(itemName, heapData);
				break;
			case HEAP_FREE_SPACE:
				valuesForSelectedItem = getHeapFreeSpace(itemName, heapData);
				break;
			case HEAP_ALLOC_CELL_COUNT:
				valuesForSelectedItem = getHeapAllocCellsCount(itemName, heapData);
				break;
			case HEAP_FREE_CELL_COUNT:
				valuesForSelectedItem = getHeapFreeCellsCount(itemName, heapData);
				break;
			case HEAP_FREE_SLACK:
				valuesForSelectedItem = getHeapFreeSlack(itemName, heapData);
				break;
			case NO_OF_PSHANDLES:
				valuesForSelectedItem = getPSHandles(itemName, heapData);
				break;
			case RAM_USED:
				valuesForSelectedItem = getUsedRam(systemData);
				break;
			case RAM_TOTAL:
				valuesForSelectedItem = getTotalRam(systemData);
				break;
			case SYSTEM_DATA:
				valuesForSelectedItem = getDataForSystemElement(itemName, kernelData);
				break;
		}
		return valuesForSelectedItem;
	}
	
	private int[] getDataForSystemElement(String itemName, ArrayList<KernelElements> kernelData)
	{
		SystemDataGraph sysGraph = new SystemDataGraph();
		return sysGraph.getValuesForGivenKerenelElement(itemName, kernelData);
	}
	private int[] getTotalRam(ArrayList<SystemData> systemData) {
		
		int [] totalRam = new int[systemData.size()];
		
		for(int i=0; i < systemData.size(); i++)
		{
			totalRam[i] = (int)systemData.get(i).getTotalMemory();
		}
		return totalRam;
	}
	
	private int[] getUsedRam(ArrayList<SystemData> systemData) {
		int [] usedRam = new int[systemData.size()];
		
		for(int i=0; i < systemData.size(); i++)
		{
			usedRam[i] = (int)(systemData.get(i).getTotalMemory() - systemData.get(i).getFreeMemory());
		}
		return usedRam;
	}
	
	private int[] getPSHandles(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] psValues = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				psValues[i] = (int)data.getPsHandles();
			}
			
			return psValues;
		}
		
		return null;
	}
	private int[] getHeapFreeSlack(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] freeSlack = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					freeSlack[i] = 0;
				else
					freeSlack[i] = (int)data.getFreeSlackSize();
			}
			
			return freeSlack;
		}
		
		return null;
	}
	private int[] getHeapFreeCellsCount(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] freeCellsCount = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					freeCellsCount[i] = 0;
				else
					freeCellsCount[i] = (int)data.getFreeCells();
			}
			
			return freeCellsCount;
		}
		
		return null;
	}
	
	private int[] getHeapAllocCellsCount(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {

		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] allocCellsCount = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					allocCellsCount[i] = 0;
				else
					allocCellsCount[i] = (int)data.getAllocatedCells();
			}
			
			return allocCellsCount;
		}
		return null;
	}
	private int[] getHeapFreeSpace(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {

		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] heapFreeSpace = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					heapFreeSpace[i] = 0;
				else
					heapFreeSpace[i] = (int)data.getHeapFreeSpace();
			}
			
			return heapFreeSpace;
		}
		return null;
	}
	private int[] getHeapAllocSpace(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] heapAllocSpace = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					heapAllocSpace[i] = 0;
				else
					heapAllocSpace[i] = (int)data.getHeapAllocatedSpace();
			}
			
			return heapAllocSpace;
		}
		return null;
	}

	private int[] getHeapSize(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] heapSize = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					heapSize[i] = 0;
				else
					heapSize[i] = (int)data.getHeapChunkSize();
			}
			
			return heapSize;
		}
		return null;
	}
	private int[] getMaxHeapSize(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] maxHeapSize = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					maxHeapSize[i] = 0;
				else
					maxHeapSize[i] = (int)data.getMaxHeapSize();
			}
			
			return maxHeapSize;
		}
		return null;
	}
	private int[] getAllFiles(String itemName, HashMap<String, ArrayList<ThreadData>> heapData) {
		
		ArrayList<ThreadData> totalData = heapData.get(itemName);
		
		if(totalData != null)
		{
			int [] allFiles = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ThreadData data = totalData.get(i);
				allFiles[i] = (int)data.getOpenFiles();
			}
			
			return allFiles;
		}
		return null;
	}
	private int[] getTotalDiskSize(String itemName, HashMap<String, ArrayList<DiskOverview>> disksData) {
		
		ArrayList<DiskOverview> totalData = disksData.get(itemName);
		
		if(totalData != null)
		{
			int [] diskSize = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				DiskOverview data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					diskSize[i] = 0;
				else
					diskSize[i] = (int)data.getSize();
			}
			
			return diskSize;
		}
		return null;
	}
	private int[] getUsedDiskSize(String itemName, HashMap<String, ArrayList<DiskOverview>> disksData) {
		
		ArrayList<DiskOverview> totalData = disksData.get(itemName);
		
		if(totalData != null)
		{
			int [] diskSize = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				DiskOverview data = totalData.get(i);
				
				if(data.getStatus() == CycleData.Deleted)
					diskSize[i] = 0;
				else
					diskSize[i] = (int)data.getUsedSize();
			}
			
			return diskSize;
		}
		return null;
	}
	private int[] getNonHeapChunkSize(String itemName, HashMap<String, ArrayList<ChunksData>> nonHeapChnksData) {
		
		ArrayList<ChunksData> totalData = nonHeapChnksData.get(itemName);
		
		if(totalData != null)
		{
			int [] chunkSizes = new int[totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				ChunksData chnkData = totalData.get(i);
				
				if(chnkData.getAttrib() == CycleData.Deleted)
					chunkSizes[i] = 0;
				else
					chunkSizes[i] = (int)chnkData.getSize();
			}
			
			return chunkSizes;
		}
		
		return null;
	}
	private int[] getGlobalChunkSize(String itemName, HashMap<String, ArrayList<GlobalDataChunks>> glodData) {
		
		ArrayList<GlobalDataChunks> totalData = glodData.get(itemName);
	
		if(totalData != null)
		{
			int [] chnkSize = new int [totalData.size()];
			
			for(int i=0; i<totalData.size(); i++)
			{
				GlobalDataChunks data = totalData.get(i);
				
				if(data.getAttrib() == CycleData.Deleted)
					chnkSize[i] = 0;
				else
					chnkSize[i] = (int)data.getSize();
			}
			
			return chnkSize;
		}
		return null;
	}
	
	private int calculateMaxValue(int [] values)
	{
		int maxValue = 0;
		
		for(int i=0; i<values.length; i++)
		{
			if(values[i] > maxValue)
				maxValue = values[i];
		}
		return maxValue + 1000;
	}
	
	private int [] calculateTimeIntervals()
	{
		CycleData [] cyclesData = parsedData.getLogData();
		int [] time = new int[cyclesData.length];
		int prevDuration = 0;
		time[0] = 0;
				
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		for(int i=1; i<cyclesData.length;i++)
		{
			String currentTime = cyclesData[i].getTime();
			String prevTime = cyclesData[i-1].getTime();
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
		
		for(GraphedItemsInput item:graphedItemsInput)
		{
			Polyline line = pointsData.get(item);
		
			if(line != null && line.containsPoint(x, y))
			{
				text += "\n" + item.getName();
				
				if(!item.getName().equalsIgnoreCase(item.getEvent()))
					text +=  " -- " + item.getEvent();
			}
		}
					
		figureCanvas.setToolTipText(text);
		figureCanvas.redraw();
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
	
	/**
	 * Clear all data from items
	 */
	public void removeAllData()
	{
		heapData.clear();
		globalDataChunks.clear();
		nonHeapChnksData.clear();
		disksData.clear();
		systemData.clear();
		kernelData.clear();
		valuesForSelectedItems.clear();
		pointsData.clear();
	}
	
	/**
	 * Paints thread related events.
	 * @param graphics graphics context
	 * @param points points to draw in array [X0, Y0, X1, Y1, ... ] with zero Y-values pruned out
	 * @param threadName name of the thread
	 */
	private void paintThreadEvents(Graphics graphics, int [] points, String threadName)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<ThreadData> data = utils.getHeapDataFromAllCycles(threadName, parsedData);
	
		boolean handleDeleted = false;
		
		List<List<Integer>> ListOfSolidLinePoints = new ArrayList<List<Integer>>();
		ArrayList<Integer> solidLinePoints = new ArrayList<Integer>();
		 
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "thread: " + threadName);
		 
		for (int i = 0, j = 0; i < parsedData.getNumberOfCycles(); i++, j++){
			 
			 int x_point = points[j];
			 int y_point =  points[++j];
			 
			 if(y_point == NOT_DRAWN_DATA_POINT){
				 continue; // There is no valid data for this cycle
			 }
			 
			 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "Plotting data of threads");
			 if(!handleDeleted){
					 solidLinePoints.add(x_point);
					 solidLinePoints.add(y_point);	
					 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "add 1: x_point: " + x_point + ", y_point: " + y_point);
			 }
						 
			ThreadData threadData = data.get(i);
			
			if(threadData.isKernelHandleDeleted() && !handleDeleted){
				 handleDeleted = true;
				 
				 if(solidLinePoints.size() > 0)
					 ListOfSolidLinePoints.add(solidLinePoints);
					 solidLinePoints = new ArrayList<Integer>();
				 }
						 
				 if(handleDeleted && threadData.getStatus() == CycleData.New)
				 {
					 handleDeleted = false;					 
					 solidLinePoints.add(x_point);
					 solidLinePoints.add(y_point);						 
				 }
						 
		}
		
		if(solidLinePoints.size() > 0)
			 ListOfSolidLinePoints.add(solidLinePoints);
			 
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
			}
		}
		
	}
	
	private void paintChunkEvents(Graphics graphics, int [] points, String item_name, String event)
	{
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<ThreadData> data = utils.getHeapDataFromAllCycles(item_name, parsedData);
	
		boolean handleDeleted = false;
		
		List<List<Integer>> ListOfSolidLinePoints = new ArrayList<List<Integer>>();
		ArrayList<Integer> solidLinePoints = new ArrayList<Integer>();
		 
		for (int i = 0, j = 0; i < parsedData.getNumberOfCycles(); i++, j++)
		 {		 
			 int x_point = points[j];
			 int y_point =  points[++j];

			if (y_point <= 0){
				// Not showing zero values to a user, not meaningful data 
				DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "continued because value <= 0");
				continue;
			}
				
			 DbgUtility.println(DbgUtility.PRIORITY_LOOP, "Plotting data of threads");
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
					
			 getHandleStatus(i+1, item_name, event);
			 if(data.get(i).isKernelHandleDeleted() && !handleDeleted){
				 handleDeleted = true;
				
				 if(solidLinePoints.size() > 0)
					 ListOfSolidLinePoints.add(solidLinePoints);
					 solidLinePoints = new ArrayList<Integer>();
				 }
						 
				 if(handleDeleted && data.get(i).getStatus() == CycleData.New)
				 {
					 handleDeleted = false;
					 
					 solidLinePoints.add(x_point);
					 solidLinePoints.add(y_point);
				 }
						 
			}
		
		if(solidLinePoints.size() > 0)
			 ListOfSolidLinePoints.add(solidLinePoints);
			 
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
			}
		}
		
	}
	
	private boolean getHandleStatus(int cycleNo, String chunkName, String event)
	{
		boolean status = false;
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		
		if(event.equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
		{
			ArrayList<GlobalDataChunks> glod_data = utils.getGLOBDataFromAllCycles(chunkName, parsedData);
				
			status = glod_data.get(cycleNo -1).isKernelHandleDeleted();
		}
		else if(event.equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
		{
			ArrayList<ChunksData> chunks_data = utils.getChunkDataFromAllCycles(chunkName, parsedData);
			
			status = chunks_data.get(cycleNo -1).isKernelHandleDeleted();
		}
		
		return status;
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
