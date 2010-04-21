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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.data.KernelElements;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.ui.IImageProvider;
import com.nokia.s60tools.ui.actions.CopyImageToClipboardAction;
import com.nokia.s60tools.util.debug.DbgUtility;
/**
 * Graph to be shown in Graphs tab.
 *
 */
public class SwmtGraph extends ZoomableGraph implements MouseMoveListener, IImageProvider {

	private FigureCanvas figureCanvas;
	private FigureCanvas yAxis;
	private ParsedData logData;
	private Composite parentComposite;
	
	private boolean scaleNeedsUpdation = true;
	private GenericGraph graph;
	/**
	 * Constructor
	 * @param parent
	 */
	public SwmtGraph(Composite parent)
	{
		this.parentComposite = parent;
	}
	
	/**
	 * Set parsed data
	 * @param parsedData
	 */
	public void setInputCyclesData(ParsedData parsedData)
	{
		this.logData = parsedData;
	}
	
	/**
	 * Draw graph area
	 */
	public void constructGraphArea()
	{
		if(this.logData == null || this.logData.getLogData() == null || parentComposite == null)
			return;
		
		Control [] children = parentComposite.getChildren();
		
		if(children != null)
		{
			for(Control child:children)
				child.dispose();
		}
		
		Composite parent = new Composite(parentComposite, SWT.NONE);
		parent.setLayout(new FormLayout());
			
		yAxis = new FigureCanvas(parent);
		figureCanvas = new FigureCanvas(parent);
		//figureCanvas.setSize(500, 450);
		
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.width  = 60;
		yAxis.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(yAxis, 0, SWT.RIGHT);
		formData.right  = new FormAttachment(100);
		figureCanvas.setLayoutData(formData);
		
		yAxis.setBackground(ColorConstants.white);
		yAxis.addPaintListener(new PaintListener()
		{

			public void paintControl(PaintEvent event) {
				
				if(graph != null)
					graph.paintYAxis(event.gc);
				else
					event.gc.dispose();
							
			}		
		});
		
		yAxis.addControlListener(new ControlAdapter(){
			public void controlResized(ControlEvent e) {
			}
		});
		
		figureCanvas.setBackground(new Color(Display.getDefault(), new RGB(255,255,255)));
		
		Panel panel = new Panel()
		{
			public void paint(Graphics graphics)
			{
				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "SwmtGraph/Panel/paint START");
				
				if(graph != null){
					graph.drawBackGroundLines(figureCanvas, graphics);
					graph.paint(graphics);
				}
				else{
					erase();
				}
				DbgUtility.println(DbgUtility.PRIORITY_LOOP, "SwmtGraph/Panel/paint END");				
			}
					
					
		};
		
		panel.setLayoutManager(new FlowLayout());
				
		figureCanvas.setContents(panel);
								
		figureCanvas.addMouseMoveListener(this);
		hookContextMenu();
				
		final ScrollBar horizontalBar = figureCanvas.getHorizontalBar();
		
		horizontalBar.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent arg0) {
			
			}

			public void widgetSelected(SelectionEvent event) {
				
				if(graph != null){
					graph.setScrolledXOrigin((figureCanvas.getViewport().getViewLocation().x));
				}
		
				figureCanvas.redraw();
			}
			
		});
		
		figureCanvas.addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent e) {
				horizontalBar.setPageIncrement(figureCanvas.getBounds().width);
				
				if(graph != null)
					graph.setVisualSize(figureCanvas.getClientArea().height);
				if(scaleNeedsUpdation)
				{
					zoomGraph();
					scaleNeedsUpdation = false;
				}
				
				yAxis.redraw();
				figureCanvas.redraw();
				
			}
		});
		parentComposite.layout();
		
	}	
	
	/**
	 * Add Pop-Up Menus on Graph area.
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
	 * This method acts as an interface for redrawig graphs
	 * @param graph 
	 */
	public void redraw(GenericGraph graph)
	{
		this.graph = graph;
		
		try{

		graph.setVisualSize(figureCanvas.getClientArea().height);
		graph.prepareData();
		int [] timeSamples = graph.calculateTimeIntervals();
		graph.lastSampleTime = timeSamples[timeSamples.length -1];
		
		//Zooms Graph to fit last sample value
		zoomGraph();
		yAxis.redraw();
		figureCanvas.redraw();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {
			
		if(this.graph != null)
		{
			this.figureCanvas.setToolTipText(this.graph.getToolTipText(event.x, event.y));
			this.figureCanvas.redraw();
		}
	}
	
	/**
	 * Clear graph
	 */
	public void clearGraph()
	{
		this.graph = null;
		try{
			yAxis.redraw();
			figureCanvas.redraw();
		
		}catch(NullPointerException e){
			
		}
	}
	
	/**
	 * This method stores data corresponding to given item, so that it can be 
	 * used in Graphed items view.
	 * @param itemName 
	 * @param allEventsGraph
	 */
	public void storeClearedEventValues(String itemName, GraphForAllEvents allEventsGraph)
	{
		if(this.graph != null)
		{
			if(graph instanceof ThreadsGraph){
				ArrayList<ThreadData> values = ((ThreadsGraph)(graph)).getDataForThread(itemName);
			
				allEventsGraph.setHeapSizeForThread(itemName, values);
				
			}
			else if(graph instanceof ChunksGraph)
			{
				if(graph.getEvent().equals(GenericGraph.EventTypes.GLOBAL_DATA_SIZE))
				{
					ArrayList<GlobalDataChunks> values = ((ChunksGraph)(graph)).getGlobalChunkData(itemName);
					
					allEventsGraph.setGlobalChunkSizeForChunk(itemName, values);
				}
				else if(graph.getEvent().equals(GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE))
				{
					ArrayList<ChunksData> values = ((ChunksGraph)(graph)).getNonHeapChunkData(itemName);
					
					allEventsGraph.setNonHeapChunkSizeForChunk(itemName, values);
				}
					
			}
			else if(graph instanceof DisksGraph)
			{
				if(graph.getEvent().equals(GenericGraph.EventTypes.DISK_USED_SIZE) || 
						graph.getEvent().equals(GenericGraph.EventTypes.DISK_TOTAL_SIZE))
				{
					ArrayList<DiskOverview> values = ((DisksGraph)(graph)).getDiskData(itemName);
					
					allEventsGraph.setDiskData(itemName, values);
				}
				
				else if(graph.getEvent().equals(GenericGraph.EventTypes.RAM_USED)||
						graph.getEvent().equals(GenericGraph.EventTypes.RAM_TOTAL))
				{
					ArrayList<SystemData> sysData = ((DisksGraph)(graph)).getSystemData();
					
					allEventsGraph.setSystemData(sysData);
				}
			}
			else if(graph instanceof SystemDataGraph)
			{
				ArrayList<KernelElements> kernelData = ((SystemDataGraph)(graph)).getKernelData();
				
				allEventsGraph.setKernelData(kernelData);
			}
		}
	}

	/**
	 * Sets enabled/disabled states for actions commands
	 * on this view, based on the current application state.
	 * This method should be called whenever an operation is
	 * started or stopped that might have effect on action 
	 * button states.
	 */
	private void updateViewActionEnabledStates() {
		//Zoom In
		setEnableState(zoomIn, !(this.graph != null && graph.getScale() == GraphsUtils.nextScale(graph.getScale(), false)));
		
		//Zoom Out
		if(this.graph != null)
		{
			boolean zoomOutEnableCondition1 = !(graph.getScale() == GraphsUtils.nextScale(graph.getScale(), true));
			int width = figureCanvas.getClientArea().width;
			boolean zoomOutEnableCondition2 = !(graph.lastSampleTime / graph.getScale() <= width);
			setEnableState(zoomOut, zoomOutEnableCondition1 && zoomOutEnableCondition2);
		}
		else{
			setEnableState(zoomOut, false);			
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.ZoomableGraph#zoomIn()
	 */
	protected void zoomIn()
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SwmtGraph - Zoom In");
		//Preconditions checked already in updateViewActionEnabledStates
		graph.setScale(GraphsUtils.nextScale(graph.getScale(), false));
		setNewSize();
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.ZoomableGraph#zoomOut()
	 */
	protected void zoomOut()
	{
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "SwmtGraph - Zoom Out");
		//Preconditions checked already in updateViewActionEnabledStates
		graph.setScale(GraphsUtils.nextScale(graph.getScale(), true));
		setNewSize();
	}
	
	/**
	 * This method first zooms in graph to the maximum possible scale
	 * and zooms out so that it fits in the canvas area.
	 *
	 */
	public void zoomGraph()
	{		
		int width = figureCanvas.getClientArea().width;
		
		if(width <=0 || this.graph == null)
			return;
		
		double new_scale = graph.getScale();
		
		double prevNew  = new_scale;
		
		//first zoom in until it is too big to fit
		while (graph.lastSampleTime / new_scale <= width){
			new_scale = GraphsUtils.nextScale(new_scale, false);
			
			if(prevNew == new_scale)
				break;
			
			prevNew = new_scale;
			
		}
		// now zoom out until it just fits
		while (graph.lastSampleTime / new_scale > width){
			new_scale = GraphsUtils.nextScale(new_scale, true);
			
			if(prevNew == new_scale)
				break;
			
			prevNew = new_scale;
		}
		
		if (new_scale == graph.getScale())
			return;
	
		graph.setScale(new_scale);
		setNewSize();
	}
	
	/**
	 * This method sets the size of the panel, when scale is changed.  
	 * When graph extends beyond visible area, horizontal scroll bar appears automatically.
	 *
	 */
	private void setNewSize()
	{
		if(graph == null)
			return;
		
		graph.setScrolledXOrigin(0);
		double scale = graph.getScale();
		int lastSample = graph.lastSampleTime;
		
		int prefSize = (int)(lastSample/scale);
	
		Panel panel = (Panel)(figureCanvas.getContents());
		panel.setPreferredSize(prefSize + 100, 0);
	
		if (prefSize >= figureCanvas.getClientArea().width) {
			graph.setScrolledXOrigin(figureCanvas.getViewport().getViewLocation().x);
	    	panel.setSize(prefSize + 100, 0);
	    }
	
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
