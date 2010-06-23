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
 * Description: 
 *
 */

package com.nokia.carbide.cpp.pi.memory;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.memory.actions.MemoryStatisticsDialog;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.memory.ProfiledLibraryEvent.GraphSamplePoint;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

public class MemTraceGraph extends GenericTraceGraph implements FocusListener,
		PIEventListener, MouseMotionListener, IContextMenu, ITitleBarMenu {
	private enum UsageType {
		CHUNKS, HEAPSTACK, CHUNKS_HEAPSTACK
	};

	private boolean dynamicMemoryVisualisation = false;

	private MemThreadTable memThreadTable;
	private MemLibraryEventTable memLibraryEventTable;
	private CheckboxTableViewer memoryTableViewer;

	Hashtable<Integer, Integer> threadList;

	// 3 tabs can share the same trace, but they need different graphs
	private MemTrace memTrace;

	private FigureCanvas leftFigureCanvas;

	// whether any table items are enabled
	private boolean haveEnabled = false;

	private boolean readyToDraw = false;
	private int width = 600;
	private int height = 400;

	private final int defaultSamplingTime = 3000;
	private int samplingTime;
	private UsageType paintMode;
	private int[] chunkListY;
	private int[] stackListY;
	private int[] chunkStackListY;
	private int[] polyListX;

	int[] stackAndHeapPoints;
	int[] chunkPoints;

	private TreeMap<Long, Integer> eventChunkListY;
	private TreeMap<Long, Integer> eventStackListY;
	private TreeMap<Long, Integer> eventChunkStackListY;

	private int minStack = Integer.MAX_VALUE;
	private int maxStack = 0;
	private int minHeap = Integer.MAX_VALUE;
	private int maxChunks = 0;
	private int minStackHeap = Integer.MAX_VALUE;
	private int maxStackHeap = 0;

	private DecimalFormat memKBFormat = new DecimalFormat(Messages
			.getString("MemTraceGraph.KBformat")); //$NON-NLS-1$
	private DecimalFormat memMBFloatFormat = new DecimalFormat(Messages
			.getString("MemTraceGraph.MBformat")); //$NON-NLS-1$

	private static int xLegendHeight = 50;

	private boolean firstTimeDrawThreadList = true;

	private Composite holder;
	private boolean isLibraryEventTrace = false;
	
	private final boolean[] lockMouseToolTipResolver ={false}; 
	private boolean showMemoryUsageLine = true;
	
	public MemTraceGraph(int graphIndex, MemTrace memTrace, int uid) {
		super((GenericSampledTrace) memTrace);

		if (memTrace != null) {

			// if no version number is found from trace, we can assume
			// that sampling based memory model is in use
			this.memTrace = memTrace;
			if (memTrace.getVersion() == 0) {
				memTrace.setVersion(156);
			}
		}

		this.graphIndex = graphIndex;
		this.memTrace = memTrace;
		this.paintMode = UsageType.CHUNKS_HEAPSTACK;
		this.setScale(10);

		if (memTrace == null) {
			System.out.print(Messages
					.getString("MemTraceGraph.traceDataNotFound")); //$NON-NLS-1$
			return;
		}

		if(memTrace.getVersion() >= 203){
			isLibraryEventTrace = true;
		}
		
		samplingTime = calcSamplingTime();
		memTrace.gatherDrawData();

		
		// create the label and a tableviewer
		holder = new Composite(NpiInstanceRepository.getInstance()
				.getProfilePage(uid, graphIndex).getBottomComposite(), SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(holder, MemoryPlugin.HELP_CONTEXT_ID_MAIN_PAGE);
		
		if(isLibraryEventTrace){	
			FormLayout formLayout = new FormLayout();
			formLayout.marginHeight = 0;
			formLayout.marginWidth = 0;
			formLayout.marginLeft = 0;
			formLayout.marginRight = 0;
			holder.setLayout(formLayout);
			holder.setLayoutData(new GridData(GridData.FILL_BOTH));
			
		    final Sash sash = new Sash(holder, SWT.VERTICAL);
		    final FormData data = new FormData();
		    data.top = new FormAttachment(0);
		    data.bottom = new FormAttachment(100); 
		    data.left = new FormAttachment(50); 
		    sash.setLayoutData(data);
		
		    // Memory usage table
		    FormData leftData = new FormData();
		    leftData.top = new FormAttachment(0);
		    leftData.bottom = new FormAttachment(100);
		    leftData.left = new FormAttachment(0);
		    leftData.right = new FormAttachment(sash);		    
		    SashForm sfLeft = new SashForm(holder, SWT.NONE);
		    sfLeft.setLayout(formLayout);
		    sfLeft.setLayoutData(leftData);		 
		    this.memThreadTable = new MemThreadTable(this, sfLeft);
			    
		    
		    // Library event table
		    FormData rightData = new FormData();
		    rightData.top = new FormAttachment(0, 0);
		    rightData.bottom = new FormAttachment(100, 0);
		    rightData.left = new FormAttachment(sash, 0);
		    rightData.right = new FormAttachment(100, 0);
		    
		    SashForm sfRight = new SashForm(holder, SWT.NONE);
		    sfRight.setLayout(formLayout);
		    sfRight.setLayoutData(rightData);		 
		    this.memLibraryEventTable = new MemLibraryEventTable(this, sfRight);
			
		    // allow to drag the sash
		    sash.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail != SWT.DRAG) {
						data.left = new FormAttachment(0, event.x);
						sash.getParent().layout();					
					}
				}
			});
		}else{				
			holder.setLayout(new FillLayout());
			this.memThreadTable = new MemThreadTable(this, holder);
		}
		this.memoryTableViewer = this.memThreadTable.getTableViewer();

		this.readyToDraw = true;
	}

	public void action(String actionString) {
		if (actionString.equals("chunk_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.CHUNKS;
		} else if (actionString.equals("heapstack_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.HEAPSTACK;
		} else if (actionString.equals("chunk_heapstack_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.CHUNKS_HEAPSTACK;
		} else if (actionString.equals("rescale_on")) //$NON-NLS-1$
		{
			dynamicMemoryVisualisation = true;
			makeThreadDrawLists();
		} else if (actionString.equals("rescale_off")) //$NON-NLS-1$
		{
			dynamicMemoryVisualisation = false;
			makeThreadDrawLists();
		}  else if (actionString.equals("memory_usage_line_on")) //$NON-NLS-1$
		{
			showMemoryUsageLine = true;
		}  else if (actionString.equals("memory_usage_line_off")) //$NON-NLS-1$
		{
			showMemoryUsageLine = false;
		}else {
			return;
		}

		this.repaint();
		if (this.leftFigureCanvas != null)
			this.leftFigureCanvas.redraw();
	}

	public void piEventReceived(PIEvent be) {
		switch (be.getType()) {
		// when the selection area changes, the maximum values shown for each
		// thread/process
		// may change; if dynamicMemoryVisualisation is on, we'll also have a
		// new y-scaling factor
		case PIEvent.SELECTION_AREA_CHANGED:

			// before updating the tables, make sure that the memory trace has
			// computing the
			// maximum usage by each thread/process within the new time interval
			double startTime = PIPageEditor.currentPageEditor().getStartTime();
			double endTime = PIPageEditor.currentPageEditor().getEndTime();

			memTrace.setMaxMemDataByInterval((int) (startTime * 1000),
					(int) (endTime * 1000));
		
			if(isLibraryEventTrace){
				memTrace.setMaxMemLibraryEventDataByInterval((int) (startTime * 1000),
					(int) (endTime * 1000));
			}

			// send this message to the 2 other graphs
			PIEvent be2 = new PIEvent(be.getValueObject(),
					PIEvent.SELECTION_AREA_CHANGED2);

			for (int i = 0; i < 3; i++) {
				MemTraceGraph graph = (MemTraceGraph) memTrace.getTraceGraph(i);

				if (graph != this) {
					graph.piEventReceived(be2);
				}
			}

			be = be2;

			// FALL THROUGH
		case PIEvent.SELECTION_AREA_CHANGED2:
			double[] values = (double[]) be.getValueObject();
			this.setSelectionStart(values[0]);
			this.setSelectionEnd(values[1]);
			this.memThreadTable.piEventReceived(be);
			if(this.memLibraryEventTable != null){
				this.memLibraryEventTable.piEventReceived(be);
			}		
			this.repaint();
			break;

		case PIEvent.CHANGED_MEMORY_TABLE:
			this.makeThreadDrawLists();
			this.repaint();
			if (this.leftFigureCanvas != null)
				this.leftFigureCanvas.redraw();
			if(memLibraryEventTable != null){				
				memLibraryEventTable.updateSelection(memThreadTable.getTableViewer().getCheckedElements());	
			}
			break;

		case PIEvent.SCALE_CHANGED:
			double scale = ((Double) be.getValueObject()).doubleValue();
			this.setScale(scale);
			this.repaint();
			break;

		case PIEvent.SCROLLED:
			Event event = ((Event) be.getValueObject());
			this.parentComponent.setScrolledOrigin(event.x, event.y, (FigureCanvas)event.data);
			this.repaint();
			break;

		default:
			break;
		}
	}

	public void focusGained(FocusEvent fe) {
	}

	public void focusLost(FocusEvent fe) {
	}

	public void paint(Panel panel, Graphics graphics) {
		if (!readyToDraw)
			return;

		this.setSize(this.getSize().width, getVisualSize().height);
		this.makeThreadDrawLists();
		this.paintThreads(graphics);
		this.drawDottedLineBackground(graphics, MemTraceGraph.xLegendHeight);
		
		if(showMemoryUsageLine){
			paintMemoryUsageLine(graphics);
		}		
		if(isLibraryEventTrace){
			paintLibraryEvents(graphics);
		}
		
		// draw the same selection as the Address/Thread trace
		this.drawSelectionSection(graphics, MemTraceGraph.xLegendHeight);
		
	}
	
	private void paintLibraryEvents(Graphics graphics){
		Enumeration<ProfiledLibraryEvent> enume = memTrace.getLibraryEvents().elements();	
		while(enume.hasMoreElements()){
			ProfiledLibraryEvent ple = enume.nextElement();	
			if(!ple.isEnabled(graphIndex)){
				continue;
			}
			List<GraphSamplePoint> pointList = ple.getGraphSamplePoints();
			Iterator<GraphSamplePoint> iterator = pointList.iterator();
			while (iterator.hasNext()) {
				GraphSamplePoint samplePoint = iterator.next();
				graphics.setBackgroundColor(ple.getColor());
				graphics.setForegroundColor(ple.getColor());
				drawThreadMarks((int) samplePoint.firstSamplePoint,
						(int) samplePoint.lastSamplePoint,
						this.getVisualSize().height, graphics);
			}
		}
	}
	
	private void paintMemoryUsageLine(Graphics graphics){
		Color black = ColorPalette.getColor(new RGB(0, 0, 0));
		graphics.setBackgroundColor(black);
		graphics.setForegroundColor(black);
		PointList pl = new PointList();
		Iterator<MemSampleByTime> iterator = memTrace.getDrawDataByTime().iterator();
		while(iterator.hasNext()){
			MemSampleByTime msbt = iterator.next();	
			// calculate new x coord's value and round it to integer
			int xCoord = (int) ((msbt.getTime()/getScale()) + 0.5);
			int maxBytes;
			if (dynamicMemoryVisualisation) {
				if (paintMode == UsageType.CHUNKS) {
					maxBytes = maxChunks;
				} else if (paintMode == UsageType.HEAPSTACK) {
					maxBytes = maxStack;
				} else {
					maxBytes = maxStackHeap;
				}
			} else {
				if (paintMode == UsageType.CHUNKS) {				
					maxBytes = memTrace.getTraceMaxChunks();					
				} else if (paintMode == UsageType.HEAPSTACK) {
					maxBytes = memTrace.getTraceMaxStackHeap();
				} else {
					maxBytes = memTrace.getTraceMaxTotal();		
				}
			}
				
			int yMultiplier = prettyMaxBytes(maxBytes) / height;
			// calculate new y-coord's value and round it to integer
			int yCoord = (int) (((double) height - (double) msbt.getUsedMemory()
					/ yMultiplier) + 0.5);				
			pl.addPoint(xCoord,yCoord);
		}
		graphics.setLineWidth(2);
		graphics.drawPolyline(pl);		
	}

	private void paintSampledChunks(Graphics graphics) {
		if (chunkListY == null)
			return;

		int[] points = new int[chunkListY.length * 2];

		for (int i = 0, j = 0; i < chunkListY.length; i++) {
			points[j++] = polyListX[i];
			points[j++] = chunkListY[i];
		}

		graphics.setBackgroundColor(ColorConstants.orange);
		graphics.fillPolygon(points);
		graphics.setBackgroundColor(ColorConstants.gray);
	}

	private void paintEventBasedChunks(Graphics graphics) {
		/*
		 * if (chunkListY == null) return;
		 */

		/*
		 * int[] points = new int[chunkListY.length * 2];
		 * 
		 * for (int i = 0, j =0; i < chunkListY.length; i++) { points[j++] =
		 * polyListX[i]; points[j++] = chunkListY[i]; }
		 */

		graphics.setBackgroundColor(ColorConstants.orange);
		graphics.fillPolygon(chunkPoints);
		graphics.setBackgroundColor(ColorConstants.gray);
	}

	private void paintSampledStack(Graphics graphics, UsageType paintMode) {
		if (stackListY == null)
			return;

		// if needed, move every y-value that is 0 down a little so that the
		// line is more visible
		boolean increase0 = paintMode != UsageType.HEAPSTACK;

		int[] points = new int[stackListY.length * 2];

		for (int i = 0, j = 0; i < stackListY.length; i++) {
			points[j++] = polyListX[i];
			points[j] = stackListY[i];

			if (increase0 && (points[j] == 0))
				points[j] = 1;

			j++;
		}

		if (paintMode == UsageType.HEAPSTACK) {
			graphics.setBackgroundColor(ColorConstants.blue);
			graphics.fillPolygon(points);
			graphics.setBackgroundColor(ColorConstants.gray);
		} else {
			int lineWidth = graphics.getLineWidth();
			Color color = graphics.getForegroundColor();

			graphics.setForegroundColor(ColorConstants.blue);
			graphics.setLineWidth(2);
			graphics.drawPolyline(points);
			graphics.setForegroundColor(color);
			graphics.setLineWidth(lineWidth);
		}
	}

	private void paintEventBasedStack(Graphics graphics, UsageType paintMode) {
		if (eventStackListY == null)
			return;

		// if needed, move every y-value that is 0 down a little so that the
		// line is more visible
		boolean increase0 = paintMode != UsageType.HEAPSTACK;

		/*
		 * int[] points = new int[stackListY.length * 2];
		 * 
		 * for (int i = 0, j =0; i < stackListY.length; i++) { points[j++] =
		 * polyListX[i]; points[j] = stackListY[i];
		 * 
		 * if (increase0 && (points[j] == 0)) points[j] = 1;
		 * 
		 * j++; }
		 */

		if (paintMode == UsageType.HEAPSTACK) {
			graphics.setBackgroundColor(ColorConstants.blue);
			graphics.fillPolygon(stackAndHeapPoints);
			graphics.setBackgroundColor(ColorConstants.gray);
		} else {
			int lineWidth = graphics.getLineWidth();
			Color color = graphics.getForegroundColor();

			graphics.setForegroundColor(ColorConstants.blue);
			graphics.setLineWidth(2);
			graphics.drawPolyline(stackAndHeapPoints);
			graphics.setForegroundColor(color);
			graphics.setLineWidth(lineWidth);
		}
	}

	private void paintThreads(Graphics graphics) {
		if (memTrace.getVersion() >= 202) {
			paintEventBasedThreads(graphics);
		} else {
			paintSampledThreads(graphics);
		}
	}

	private void paintSampledThreads(Graphics graphics) {
		// if there are no threads to draw
		if (!haveEnabled)
			return;

		int maxBytes = 0;

		double multiplier;

		if (paintMode == UsageType.CHUNKS && chunkListY != null) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks;
			else
				maxBytes = memTrace.getTraceMaxChunks();

			// multiplier is bytes / pixel in the graph
			if (true)// !dynamicMemoryVisualisation)
				multiplier = prettyMaxBytes(maxBytes) / height;
			else
				multiplier = maxBytes / height;
			// System.out.println("maxBytes " + maxBytes + " multiplier " +
			// multiplier + " height " + height);

			for (int j = 1; j < chunkListY.length - 1; j++) {
				chunkListY[j] = (int) (height - chunkListY[j] / multiplier);

				if (chunkListY[j] < 0)
					chunkListY[j] = 0;
			}

			this.paintSampledChunks(graphics);
		} else if (paintMode == UsageType.HEAPSTACK && stackListY != null) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxStack;
			else
				maxBytes = memTrace.getTraceMaxStackHeap();

			// multiplier is bytes / pixel in the final graph
			if (true)// !dynamicMemoryVisualisation)
				multiplier = prettyMaxBytes(maxBytes) / height;
			else
				multiplier = maxBytes / height;
			// System.out.println("maxBytes " + maxBytes + " multiplier " +
			// multiplier + " height " + height);

			for (int j = 1; j < stackListY.length - 1; j++) {
				stackListY[j] = (int) (height - stackListY[j] / multiplier);

				if (stackListY[j] < 0)
					stackListY[j] = 0;
			}

			this.paintSampledStack(graphics, paintMode);
		} else if (chunkStackListY != null) // heap and stack
		{
			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks > maxStack ? maxChunks : maxStack;
			else
				maxBytes = memTrace.getTraceMaxChunks() > memTrace
						.getTraceMaxStackHeap() ? memTrace.getTraceMaxChunks()
						: memTrace.getTraceMaxStackHeap();

			// multiplier is bytes / pixel in the final graph
			if (true)// !dynamicMemoryVisualisation)
				multiplier = prettyMaxBytes(maxBytes) / height;
			else
				multiplier = maxBytes / height;
			// System.out.println("maxBytes " + maxBytes + " multiplier " +
			// multiplier + " height " + height);

			int totalStackUsed = 0;

			for (int j = 1; j < chunkListY.length - 1; j++) {
				totalStackUsed += stackListY[j];

				chunkListY[j] = (int) (height - chunkListY[j] / multiplier);
				stackListY[j] = (int) (height - stackListY[j] / multiplier);

				if (stackListY[j] < 0)
					stackListY[j] = 0;

				if (chunkListY[j] < 0)
					chunkListY[j] = 0;
			}

			this.paintSampledChunks(graphics);
			if (totalStackUsed > 0)
				this.paintSampledStack(graphics, paintMode);
		}
	}

	private void paintEventBasedThreads(Graphics graphics) {
		// paints treads when using event based memory model

		// if there are no threads to draw
		if (!haveEnabled)
			return;

		int maxBytes = 0;

		double yMultiplier;
		double xMultiplier;
		xMultiplier = this.getScale();

		// get las x coordinate of trace
		long lastEvent = ((MemSample) memTrace.samples.get(memTrace.samples
				.size() - 1)).sampleSynchTime;
		int lastXCoord = (int) (lastEvent / xMultiplier);

		if (paintMode == UsageType.CHUNKS && eventChunkListY != null) {
			chunkPoints = new int[eventChunkListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks;
			else
				maxBytes = memTrace.getTraceMaxChunks();

			// multiplier is bytes / pixel in the graph
			if (true)// !dynamicMemoryVisualisation)
				yMultiplier = prettyMaxBytes(maxBytes) / height;
			else
				yMultiplier = maxBytes / height;
			// System.out.println("maxBytes " + maxBytes + " multiplier " +
			// multiplier + " height " + height);

			createGraphPolygon(this.eventChunkListY, chunkPoints, xMultiplier,
					yMultiplier, lastXCoord);

			this.paintEventBasedChunks(graphics);
		} else if (paintMode == UsageType.HEAPSTACK && eventStackListY != null) {
			stackAndHeapPoints = new int[eventStackListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxStack;
			else
				maxBytes = memTrace.getTraceMaxStackHeap();

			// multiplier is bytes / pixel in the final graph
			if (true)// !dynamicMemoryVisualisation)
				yMultiplier = prettyMaxBytes(maxBytes) / height;
			else
				yMultiplier = (double) maxBytes / height;

			createGraphPolygon(this.eventStackListY, stackAndHeapPoints,
					xMultiplier, yMultiplier, lastXCoord);
			this.paintEventBasedStack(graphics, paintMode);
		} else if (eventChunkStackListY != null) // heap and stack
		{
			stackAndHeapPoints = new int[eventStackListY.size() * 4 + 6];
			chunkPoints = new int[eventChunkListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks > maxStack ? maxChunks : maxStack;
			else
				maxBytes = memTrace.getTraceMaxChunks() > memTrace
						.getTraceMaxStackHeap() ? memTrace.getTraceMaxChunks()
						: memTrace.getTraceMaxStackHeap();

			// multiplier is bytes / pixel in the final graph
			if (true)// !dynamicMemoryVisualisation)
				yMultiplier = prettyMaxBytes(maxBytes) / height;
			else
				yMultiplier = maxBytes / height;
			// System.out.println("maxBytes " + maxBytes + " multiplier " +
			// multiplier + " height " + height);

			int totalStackUsed = 1;

			createGraphPolygon(this.eventStackListY, stackAndHeapPoints,
					xMultiplier, yMultiplier, lastXCoord);
			createGraphPolygon(this.eventChunkListY, chunkPoints, xMultiplier,
					yMultiplier, lastXCoord);

			this.paintEventBasedChunks(graphics);
			if (totalStackUsed > 0)
				this.paintEventBasedStack(graphics, paintMode);
		}
	}

	public void createGraphPolygon(TreeMap<Long, Integer> map, int[] points,
			double xMultiplier, double yMultiplier, int endXCoord) {
		// Creates graph polygon from TreeMap that contains all x and y values
		// of one graph.

		int index = 2;
		int xCoord = 0;
		int yCoord = 0;
		int previousYCoord = 0;

		// get first event and key from map.
		Iterator<Long> keys = map.keySet().iterator();
		Iterator<Integer> values = map.values().iterator();

		int previousXCoord = 0;
		int countOfSameXCoords = 1;

		// set first into zero so that polygon is drawn correctly
		points[0] = 0;
		points[1] = height;

		while (keys.hasNext()) {
			// create polygon's points so that each memory allocation is drawn
			// as one leap in graph

			// calculate new x coord's value and round it to integer
			xCoord = (int) (((double) keys.next() / xMultiplier) + 0.5);

			// calculate new y-coord's value and round it to integer
			yCoord = (int) (((double) height - (double) values.next()
					/ yMultiplier) + 0.5);

			if (xCoord == previousXCoord && index > 3) {
				// if more than one sample at one point in the screen, count
				// average value
				// for y coordinate

				// count average value
				double sum = ((double) yCoord + (double) previousYCoord
						* (double) countOfSameXCoords);
				countOfSameXCoords++;
				yCoord = (int) (sum / (double) (countOfSameXCoords));

				// add average coordinate to array
				index = index - 4;
				index = addCoordsToGraphArray(xCoord, yCoord, index, points);

			} else {
				countOfSameXCoords = 1;
			}

			index = addCoordsToGraphArray(xCoord, yCoord, index, points);

			// save coordinates to previousValues
			previousYCoord = yCoord;
			previousXCoord = xCoord;
		}

		// Set last coordinates to zero so that polygon is drawn correctly
		points[points.length - 4] = endXCoord;
		points[points.length - 3] = points[points.length - 5];

		points[points.length - 2] = endXCoord;
		points[points.length - 1] = height;
	}

	private int addCoordsToGraphArray(int xCoord, int yCoord, int arrayIndex,
			int[] array) {

		// adds coordinates to array so that polygon can be drawn from that
		// array

		// instead of straight lines between points, we draw graph more
		// realistically
		// so that single memory events are shown as a leap in graph.

		// first x coordinate
		array[arrayIndex] = xCoord;
		arrayIndex++;

		// first y coordinate, if possible use same y-coord than previous value
		if ((arrayIndex - 2) < 0) {
			array[arrayIndex] = 0;
		} else {
			array[arrayIndex] = array[arrayIndex - 2];
		}
		arrayIndex++;

		// second x coordinate
		array[arrayIndex] = xCoord;
		arrayIndex++;

		// second y coordinate
		array[arrayIndex] = yCoord;
		arrayIndex++;

		return arrayIndex;
	}

	private void makeThreadDrawLists() {
		if (memTrace.getVersion() >= 202) {
			makeEventBasedThreadDrawLists();
		} else {
			makeSamplingBasedThreadDrawLists();
		}
	}

	private void makeEventBasedThreadDrawLists() {

		// Get checked table items
		Object[] checked = this.memoryTableViewer.getCheckedElements();

		// if no items is checked do nothing
		haveEnabled = (checked != null) && (checked.length > 0);
		if (!haveEnabled) {
			this.eventChunkListY = null;
			this.eventStackListY = null;
			this.eventChunkStackListY = null;
			return;
		}

		// create maps for events for chunks, stacks and chunksandstacks
		this.eventChunkListY = new TreeMap<Long, Integer>();
		this.eventStackListY = new TreeMap<Long, Integer>();
		this.eventChunkStackListY = new TreeMap<Long, Integer>();

		// go thru checked items
		for (int j = 0; j < checked.length; j++) {
			// check that item is instance of memory thread
			if (!(checked[j] instanceof MemThread))
				continue;

			MemThread memThread = (MemThread) checked[j];

			// get all samples of the thread
			TreeMap<Long, MemSample> memSamples = memTrace
					.getDrawDataByMemThread(memThread);

			// ensure that thread has samples
			if ((memSamples == null) || (memSamples.size() == 0)) {
				System.out
						.println(Messages
								.getString("MemTraceGraph.threadProcessNoSamples1") + memThread.fullName + Messages.getString("MemTraceGraph.threadProcessNoSamples")); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			// create empty sample
			MemSample previousSample = new MemSample(new MemThread(0, "", ""), //$NON-NLS-1$ //$NON-NLS-2$
					0, 0, 0);

			Iterator<MemSample> values = memSamples.values().iterator();

			while (values.hasNext()) {
				MemSample memSample = values.next();
				// go thru samples from single threas
				// save changes after last received sample into TreeMaps
				addEventToTreeMap(this.eventStackListY,
						memSample.sampleSynchTime, memSample.stackSize
								- previousSample.stackSize);
				addEventToTreeMap(this.eventChunkListY,
						memSample.sampleSynchTime, memSample.heapSize
								- previousSample.heapSize);
				addEventToTreeMap(
						this.eventChunkStackListY,
						memSample.sampleSynchTime,
						memSample.heapSize
								+ memSample.stackSize
								- (previousSample.stackSize + previousSample.heapSize));

				previousSample = memSample;

			}
		}

		// calculate max values and values in each event
		this.maxStack = calculateValuesInEachEvent(eventStackListY);
		this.maxStackHeap = calculateValuesInEachEvent(eventChunkStackListY);
		this.maxChunks = calculateValuesInEachEvent(eventChunkListY);

		if (this.memTrace.getTraceMaxChunks() == 0) {
			this.memTrace.setTraceMaxChunks(maxChunks);
			this.memTrace.setTraceMaxStackHeap(maxStack);
			this.memTrace.setTraceMaxTotal(maxStackHeap);

			// repaint left legend if this is first time that tread lists are
			// made
			if (firstTimeDrawThreadList) {
				this.parentComponent.paintLeftLegend();
				firstTimeDrawThreadList = false;
			}

		}

	}

	private int calculateValuesInEachEvent(TreeMap<Long, Integer> map) {
		// this function calculates total sum memory in each event based
		// on the change map it receives as a parameter

		// function also returns maximum value in whole map

		int maxValue = 0;
		int previousValue = 0;

		Iterator<Integer> values = map.values().iterator();
		Iterator<Long> keys = map.keySet().iterator();

		while (values.hasNext()) {

			int memValue = values.next();
			long memKey = keys.next();

			// go thru array and count actual state of
			// memory in each event
			int value = previousValue + memValue;

			// is value is greater that max value save
			// it as max value
			if (value > maxValue) {
				maxValue = value;
			}

			map.put(memKey, value);
			previousValue = value;
		}
		return maxValue;
	}

	private void addEventToTreeMap(TreeMap<Long, Integer> map, long key,
			int item) {
		// Adds event into tree map.
		// If event with that same key(time code) already exists values are
		// added.

		int previousValue = 0;
		if (map.containsKey(key)) {
			previousValue = map.get(key);
		}

		map.put(key, previousValue + item);
	}

	private void makeSamplingBasedThreadDrawLists() {

		// Get checked table items
		Object[] checked = this.memoryTableViewer.getCheckedElements();

		haveEnabled = (checked != null) && (checked.length > 0);

		// is no items are checked do nothing
		if (!haveEnabled) {
			this.chunkListY = null;
			this.stackListY = null;
			this.chunkStackListY = null;
			calculateMinAndMaxValues();
			return;
		}

		// Get first and last sample from trace
		int firstSample = memTrace.getFirstSampleNumber();
		int lastSample = memTrace.getLastSampleNumber();

		// Get number of sampling points
		int samplesTotal = (int) 1 + (lastSample - firstSample) / samplingTime;

		// create arrays for y axis values of chunks, stacks chunksandstacks +
		// polylistx
		this.chunkListY = new int[samplesTotal * 2 + 1];
		this.stackListY = new int[samplesTotal * 2 + 1];
		this.chunkStackListY = new int[samplesTotal * 2 + 1];
		this.polyListX = new int[samplesTotal * 2 + 1];

		// go thru checked items
		for (int j = 0; j < checked.length; j++) {
			// check that item is instance of memory thread
			if (!(checked[j] instanceof MemThread))
				continue;

			MemThread memThread = (MemThread) checked[j];

			// get all samples of the thread
			TreeMap<Long, MemSample> memSamples = memTrace
					.getDrawDataByMemThread(memThread);

			// ensure that thread has samples
			if ((memSamples == null) || (memSamples.size() == 0)) {
				System.out
						.println(Messages
								.getString("MemTraceGraph.threadProcessNoSamples1") + memThread.fullName + Messages.getString("MemTraceGraph.threadProcessNoSamples")); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			int sampleCount = memSamples.size();
			int[] tempListX = new int[sampleCount * 2 + 1];

			int counter = 0;

			Iterator<MemSample> values = memSamples.values().iterator();
			while (values.hasNext()) {
				MemSample memSample = values.next();
				// get index where sample is located at x-axis
				int index = (int) ((memSample.sampleSynchTime - firstSample) / samplingTime) * 2 + 1;

				tempListX[counter + 1] = (int) (memSample.sampleSynchTime / getScale());

				// add samples value to chunks, stacks chunksandstacks arrays
				this.stackListY[index] += memSample.stackSize;
				this.chunkListY[index] += memSample.heapSize;
				this.chunkStackListY[index] += memSample.heapSize
						+ memSample.stackSize;

				counter++;
				tempListX[counter + 1] = (int) ((memSample.sampleSynchTime + samplingTime) / getScale());

				index++;
				if (this.paintMode == UsageType.CHUNKS) {
					this.chunkListY[index] += memSample.heapSize;
					counter++;
				} else if (this.paintMode == UsageType.HEAPSTACK) {
					this.stackListY[index] += memSample.stackSize;
					counter++;
				} else // paint mode chunk and stack
				{
					this.stackListY[index] += memSample.stackSize;
					this.chunkListY[index] += memSample.heapSize;
					this.chunkStackListY[index] += memSample.heapSize
							+ memSample.stackSize;
					counter++;
				}

			}

			// tempListX[0] = (int)
			// (((MemSample)memSamples.firstEntry().getValue()).sampleSynchTime
			// / getScale());
			MemSample firstMemSample = (MemSample) memSamples.get(memSamples
					.firstKey());
			tempListX[0] = (int) ((firstMemSample.sampleSynchTime / getScale()));
			tempListX[tempListX.length - 1] = tempListX[tempListX.length - 2];

			// defaults the originating and ending points into window corners
			this.stackListY[0] = height;
			this.stackListY[stackListY.length - 1] = height;
			this.chunkListY[0] = height;
			this.chunkListY[chunkListY.length - 1] = height;
			this.chunkStackListY[0] = height;
			this.chunkStackListY[chunkStackListY.length - 1] = height;
		}

		calculatePolylistX();
		calculateMinAndMaxValues();
	}

	private void calculateMinAndMaxValues() {
		// find heapValue;
		minHeap = Integer.MAX_VALUE;
		maxChunks = 0;

		if (chunkListY != null) {
			for (int i = 1; i < chunkListY.length - 1; i++) {
				if (chunkListY[i] < minHeap)
					minHeap = chunkListY[i];
				if (chunkListY[i] > maxChunks)
					maxChunks = chunkListY[i];
			}
		}

		// find stackValue;
		minStack = Integer.MAX_VALUE;
		maxStack = 0;

		if (stackListY != null) {
			for (int i = 1; i < stackListY.length - 1; i++) {
				if (stackListY[i] < minStack)
					minStack = stackListY[i];
				if (stackListY[i] > maxStack)
					maxStack = stackListY[i];
			}
		}

		// find stackValue+HeapValue;
		minStackHeap = Integer.MAX_VALUE;
		maxStackHeap = 0;

		if (stackListY != null && chunkListY != null) {
			for (int i = 1; i < stackListY.length - 1; i++) {
				if ((stackListY[i] + chunkListY[i]) < minStackHeap)
					minStackHeap = stackListY[i] + chunkListY[i];

				if ((stackListY[i] + chunkListY[i]) > maxStackHeap)
					maxStackHeap = stackListY[i] + chunkListY[i];
			}
		}
	}

	private void calculatePolylistX() {
		int currentSample = memTrace.getFirstSampleNumber();
		int lastSample = memTrace.getLastSampleNumber();
		int sampleCount = (int) 1 + (lastSample - currentSample) / samplingTime;

		polyListX = new int[sampleCount * 2 + 1];

		int i = 0;

		for (Enumeration e = memTrace.getSamples(); e.hasMoreElements();) {
			MemSample ms = (MemSample) e.nextElement();
			if ((int) ms.sampleSynchTime != currentSample) {
				i++;
				currentSample = (int) ms.sampleSynchTime;
				polyListX[i + 1] = (int) (ms.sampleSynchTime / getScale());
				i++;
				polyListX[i + 1] = polyListX[i];
			}
		}

		polyListX[0] = (int) (memTrace.getFirstSampleNumber() / getScale());
		polyListX[1] = (int) (memTrace.getFirstSampleNumber() / getScale());
		polyListX[polyListX.length - 1] = polyListX[polyListX.length - 2];
	}

	public void setSize(int x, int y) {
		this.width = x;
		this.height = y - MemTraceGraph.xLegendHeight;

		if (this.height <= 0)
			this.height = 1;
	}

	public Dimension getSize() {
		return new Dimension(width, height);
	}

	private int calcSamplingTime() {
		long time = memTrace.getFirstSampleNumber();
		for (Enumeration e = memTrace.getSamples(); e.hasMoreElements();) {
			MemSample tmp = (MemSample) e.nextElement();
			if (tmp.sampleSynchTime != time) {
				time = tmp.sampleSynchTime - time;
				return (int) time;
			}
		}
		return defaultSamplingTime;
	}

	public void refreshDataFromTrace() {
	}

	public void repaint() {
		this.parentComponent.repaintComponent();
	}

	public int getSamplingTime() {
		return samplingTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu#
	 * addContextMenuItems(org.eclipse.swt.widgets.Menu,
	 * org.eclipse.swt.events.MouseEvent)
	 */
	public void addContextMenuItems(Menu menu,
			org.eclipse.swt.events.MouseEvent me) {

		new MenuItem(menu, SWT.SEPARATOR);

		MenuItem memoryStatsItem = new MenuItem(menu, SWT.PUSH);
		memoryStatsItem.setText(Messages.getString("MemoryPlugin.memoryStats")); //$NON-NLS-1$
		memoryStatsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new MemoryStatisticsDialog(Display.getCurrent());
			}
		});

		Object obj;

		boolean rescale = false;

		// if there is a rescale value associated with the current Analyser tab,
		// then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				"com.nokia.carbide.cpp.pi.memory.rescale"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			rescale = (Boolean) obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					"com.nokia.carbide.cpp.pi.memory.rescale", rescale); //$NON-NLS-1$

		final boolean rescaleFinal = rescale;

		MenuItem rescaleItem = new MenuItem(menu, SWT.CHECK);
		rescaleItem.setText(Messages.getString("MemoryPlugin.dynamicRescale")); //$NON-NLS-1$
		rescaleItem.setSelection(rescale);
		rescaleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String action;
				NpiInstanceRepository
						.getInstance()
						.activeUidSetPersistState(
								"com.nokia.carbide.cpp.pi.memory.rescale", !rescaleFinal); //$NON-NLS-1$
				if (!rescaleFinal) {
					action = "rescale_on"; //$NON-NLS-1$
				} else {
					action = "rescale_off"; //$NON-NLS-1$
				}

				for (int i = 0; i < 3; i++) {
					MemTraceGraph graph = (MemTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action(action);
				}		
				MemoryPlugin.getDefault().updateMenuItems();
			}
		});
		
		// if there is a show memory usage value associated with the current Analyser tab, then use it		
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.memory.showMemoryUsage");	//$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showMemoryUsageLine = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.memory.showMemoryUsage", showMemoryUsageLine);	//$NON-NLS-1$
		
		new MenuItem(menu, SWT.SEPARATOR);		
		MenuItem memoryUsageLine = new MenuItem(menu, SWT.CHECK);
		memoryUsageLine.setText(Messages.getString("MemTraceGraph.showTotalMemoryUsage")); //$NON-NLS-1$
		memoryUsageLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showMemoryUsageLine = !showMemoryUsageLine;
				NpiInstanceRepository
				.getInstance()
				.activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showMemoryUsage", showMemoryUsageLine); //$NON-NLS-1$
				repaint();			
				MemoryPlugin.getDefault().updateMenuItems();
			}
		});
		memoryUsageLine.setSelection(showMemoryUsageLine);
		
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
		// System.out.println("MEM"); if (true)return;
		// if there are no threads to draw
		GC localGC = gc;

		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());

		if (this.leftFigureCanvas == null)
			this.leftFigureCanvas = figureCanvas;

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas
				.getClientArea();
		
		Combo titleCombo = ((GraphComposite) figureCanvas.getParent())
			.getTitleBarCombo();

		double visY = rect.height - MemTraceGraph.xLegendHeight;

		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));

		int maxBytes = 0;

		if (paintMode == UsageType.CHUNKS) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks;
			else
				maxBytes = memTrace.getTraceMaxChunks();
			titleCombo.select(0);
		} else if (paintMode == UsageType.HEAPSTACK) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxStack;
			else
				maxBytes = memTrace.getTraceMaxStackHeap();
			titleCombo.select(1);
		} else {
			if (dynamicMemoryVisualisation)
				maxBytes = maxChunks > maxStack ? maxChunks : maxStack;
			else
				maxBytes = memTrace.getTraceMaxChunks() > memTrace
						.getTraceMaxStackHeap() ? memTrace.getTraceMaxChunks()
						: memTrace.getTraceMaxStackHeap();
			titleCombo.select(2);
		}

		double multiplier = 0;

		if (true)// !dynamicMemoryVisualisation)
			multiplier = prettyMaxBytes(maxBytes) / visY;
		else
			multiplier = maxBytes / visY;

		int previousBottom = 0; // bottom of the previous legend drawn
		String legend;
		double yIncrement = visY / 10;

		// draw 11 value indicators (0..10) to the scale
		for (int k = 10; k >= 0; k--) {
			// location for the value indicator is k * 1/10 the height of the
			// display
			int y = (int) (visY - (yIncrement * k));

			// calculate the exact byte value at the height by multiplying
			// the height with the [bytes / pixel] value
			int bytes = (int) ((visY * multiplier) / 10.0) * k;

			// construct the text for each scale
			legend = ""; //$NON-NLS-1$

			// if the amount of data is less than 512KB, draw it as bytes
			if (maxBytes < 10000) {
				legend += bytes + Messages.getString("MemTraceGraph.byByte"); //$NON-NLS-1$
			}
			// if the amount is more than 512KB, draw it as KB
			else if (maxBytes <= 500 * 1024) {
				legend += (bytes / 1024)
						+ Messages.getString("MemTraceGraph.byKB"); //$NON-NLS-1$
			} else {
				legend += memMBFloatFormat
						.format(((float) bytes / (1024 * 1024)))
						+ Messages.getString("MemTraceGraph.byMB"); //$NON-NLS-1$
			}

			Point extent = gc.stringExtent(legend);

			gc.drawLine(IGenericTraceGraph.Y_LEGEND_WIDTH - 3, (int) y + 1,
					IGenericTraceGraph.Y_LEGEND_WIDTH, (int) y + 1);

			if (y >= previousBottom) {
				gc.drawString(legend, IGenericTraceGraph.Y_LEGEND_WIDTH - extent.x
						- 4, (int) y);
				previousBottom = (int) y + extent.y;
			}
		}

		if (localGC == null) {
			gc.dispose();
			figureCanvas.redraw();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseDragged(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseDragged(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseEntered(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseEntered(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseExited(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseExited(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseHover(org.eclipse.draw2d.
	 * MouseEvent)
	 */
	public void mouseHover(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.
	 * MouseEvent)
	 */
	public void mouseMoved(MouseEvent me) {
		double x = me.x * this.getScale();
		double y = me.y;

		// mouse event may return out of range X, that may
		// crash when we use it to index data array
		x = x >= 0 ? x : 0;
		if (me.x >= this.getVisualSize().width
				+ this.parentComponent.getScrolledOrigin(this).x) {
			x = (this.getVisualSize().width - 1) * this.getScale();
		}

		if (x > PIPageEditor.currentPageEditor().getMaxEndTime() * 1000) {
			this.setToolTipText(null);
			return;
		}
	
		if (y > this.getVisualSizeY() - MemTraceGraph.xLegendHeight) {
			if (isLibraryEventTrace) {
				final long xPoint = (long) (x + 0.5);
				if (!lockMouseToolTipResolver[0]) {
					// Resolve tool tip text in thread
					new Thread() {
						@Override
						public void run() {
							lockMouseToolTipResolver[0] = true;
							try {
								final ArrayList<MemSample> list = memTrace
										.getLibraryEventDataByTime(graphIndex,
												xPoint, (long) getScale());
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										if (list.isEmpty()) {
											setToolTipText(null);
										} else {
											final MemSample ms = list.get(list
													.size() - 1);										
											setToolTipText(getLibraryEventToolTipText(ms));											
										}
									}
								});

							} finally {
								lockMouseToolTipResolver[0] = false;
							}
						}
					}.start();
				}			
			} else {
				this.setToolTipText(null);
			}
			return;
		}
	
		long chunkSize = 0;
		long stackHeapSize = 0;
		if (memTrace.getVersion() >= 202) {
			if (eventStackListY != null) {
				Integer value = (Integer) MemTrace.getFloorEntryFromMap(
						(long) x, eventStackListY);
				if (value != null) {
					stackHeapSize = value;
				}
			}
			if (eventChunkListY != null) {
				Integer value = (Integer) MemTrace.getFloorEntryFromMap(
						(long) x, eventChunkListY);
				if (value != null) {
					chunkSize = value;
				}

			}
		} else {
			ArrayList<MemSample> samples = memTrace
					.getMemSampleDataByTime((long) x);

			if (samples != null) {
				// tooltip always shows totals for the threads/processes that
				// are selected
				for (int i = 0; i < samples.size(); i++) {
					MemSample sample = samples.get(i);
					MemThread memThread = sample.thread;
					if (memThread.isEnabled(this.graphIndex)) {
						chunkSize += sample.heapSize;
						stackHeapSize += sample.stackSize;
						// totalSize += sample.heapSize + sample.stackSize;
					}
				}
			}
		}

		int time = (int) x;

		if (this.paintMode == UsageType.CHUNKS_HEAPSTACK) {
			this.setToolTipText((time / 1000.0)
					+ Messages.getString("MemTraceGraph.totalTooltip1") //$NON-NLS-1$
					+ memKBFormat.format((chunkSize + 512) / 1024)
					+ Messages.getString("MemTraceGraph.totalTooltip2") //$NON-NLS-1$
					+ memKBFormat.format((stackHeapSize + 512) / 1024));
		} else if (this.paintMode == UsageType.CHUNKS) {
			this.setToolTipText((time / 1000.0)
					+ Messages.getString("MemTraceGraph.chunkTooltip1") //$NON-NLS-1$
					+ (chunkSize + 512) / 1024
					+ Messages.getString("MemTraceGraph.chunkTooltip2")); //$NON-NLS-1$
		} else if (this.paintMode == UsageType.HEAPSTACK) {
			this.setToolTipText((time / 1000.0)
					+ Messages.getString("MemTraceGraph.stackHeapTooltip1") //$NON-NLS-1$
					+ (stackHeapSize + 512) / 1024
					+ Messages.getString("MemTraceGraph.stackHeapTooltip2")); //$NON-NLS-1$
		} else
			return;

	}
	
	private String getLibraryEventToolTipText(MemSample memSample) {
		String libraryName = getMemTrace().getLibraryNameString(
				memSample.thread.fullName);
		String process = getMemTrace().getProcessFromLibraryNameString(
				memSample.thread.fullName);
		if (memSample.stackSize != 0) {
			return MessageFormat.format(Messages
					.getString("MemTraceGraph.mouseToolTipLoaded"),
					libraryName, process, memKBFormat
							.format(((memSample.heapSize) + 512) / 1024),
					memSample.sampleNum);
		} else {
			return MessageFormat.format(Messages
					.getString("MemTraceGraph.mouseToolTipUnloaded"),
					libraryName, process, memKBFormat
							.format(((memSample.heapSize) + 512) / 1024),
					memSample.sampleNum);
		}
	}

	public void setCurrentThreads(Hashtable<Integer, Integer> threadList) {
		this.threadList = threadList;
	}

	public int getGraphIndex() {
		return this.graphIndex;
	}

	public MemTrace getMemTrace() {
		return this.memTrace;
	}

	public MemThreadTable getMemThreadTable() {
		return this.memThreadTable;
	}

	public boolean haveEnabled() {
		return this.haveEnabled;
	}

	private int prettyMaxBytes(int bytes) {
		if (bytes < 1000)
			bytes = 1000;
		else if (bytes < 10000)
			bytes = 10000;
		else if (bytes <= 10 * 1024)
			bytes = 10 * 1024;
		else if (bytes <= 20 * 1024)
			bytes = 20 * 1024;
		else if (bytes <= 30 * 1024)
			bytes = 30 * 1024;
		else if (bytes <= 50 * 1024)
			bytes = 50 * 1024;
		else if (bytes <= 100 * 1024)
			bytes = 100 * 1024;
		else if (bytes <= 200 * 1024)
			bytes = 200 * 1024;
		else if (bytes <= 300 * 1024)
			bytes = 300 * 1024;
		else if (bytes <= 500 * 1024)
			bytes = 500 * 1024;
		else if (bytes <= 1000 * 1024)
			bytes = 1000 * 1024;
		else if (bytes <= 1 * 1024 * 1024)
			bytes = 1 * 1024 * 1024;
		else if (bytes <= 2 * 1024 * 1024)
			bytes = 2 * 1024 * 1024;
		else if (bytes <= 3 * 1024 * 1024)
			bytes = 3 * 1024 * 1024;
		else if (bytes <= 5 * 1024 * 1024)
			bytes = 5 * 1024 * 1024;
		else if (bytes <= 10 * 1024 * 1024)
			bytes = 10 * 1024 * 1024;
		else if (bytes <= 20 * 1024 * 1024)
			bytes = 20 * 1024 * 1024;
		else if (bytes <= 30 * 1024 * 1024)
			bytes = 30 * 1024 * 1024;
		else if (bytes <= 50 * 1024 * 1024)
			bytes = 50 * 1024 * 1024;
		else if (bytes <= 100 * 1024 * 1024)
			bytes = 100 * 1024 * 1024;
		else if (bytes <= 200 * 1024 * 1024)
			bytes = 200 * 1024 * 1024;
		else if (bytes <= 300 * 1024 * 1024)
			bytes = 300 * 1024 * 1024;
		else if (bytes <= 500 * 1024 * 1024)
			bytes = 500 * 1024 * 1024;
		else
			bytes = ((bytes + 1024 * 1024 * 1024 - 1) / (1024 * 1024 * 1024))
					* (1024 * 1024 * 1024);

		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#
	 * addTitleBarMenuItems()
	 */
	public Action[] addTitleBarMenuItems() {

		// Create actions for Title Bar's drop-down list 
		
		ArrayList<Action> actionArrayList = new ArrayList<Action>();

		// Action for showing only chunks
		Action actionShowChunk = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showChunk", true); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showHeapStack", false); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					MemTraceGraph graph = (MemTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("chunk_on"); //$NON-NLS-1$
				}
				MemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowChunk.setText(Messages.getString("MemoryPlugin.showChunks")); //$NON-NLS-1$

		// Action for showing only heap/stack
		Action actionShowHeap = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showChunk", false); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showHeapStack", true); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					MemTraceGraph graph = (MemTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("heapstack_on"); //$NON-NLS-1$
				}
				MemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowHeap
				.setText(Messages.getString("MemoryPlugin.showHeapStack")); //$NON-NLS-1$

		// Action for showing both heap/stack and chunks
		Action actionShowBothItem = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showChunk", true); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showHeapStack", true); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					MemTraceGraph graph = (MemTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("chunk_heapstack_on"); //$NON-NLS-1$
				}
				MemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowBothItem.setText(Messages.getString("MemoryPlugin.showAll")); //$NON-NLS-1$

		actionArrayList.add(actionShowChunk);
		actionArrayList.add(actionShowHeap);
		actionArrayList.add(actionShowBothItem);

		// check which drawing mode is selected and set its action's state to
		// checked
		boolean showChunk = isShowChunkEnabled();
		boolean showHeapStack = isShowHeapStackEnabled();

		if (showChunk && !showHeapStack) {
			actionShowChunk.setChecked(true);
		} else if (showHeapStack && !showChunk) {
			actionShowHeap.setChecked(true);
		} else {
			actionShowBothItem.setChecked(true);
		}

		return actionArrayList.toArray(new Action[actionArrayList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#getContextHelpId()
	 */
	public String getContextHelpId() {
		return MemoryPlugin.HELP_CONTEXT_ID_MAIN_PAGE;
	}

	/**
	 * Function for checking if chunk view is enabled
	 * 
	 * @return boolean value that is true when chunk view is enabled
	 */

	private boolean isShowChunkEnabled() {
		// if there is a showChunk value associated with the current Analyser
		// tab, then use it
		Object obj = NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showChunk"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			return true;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					"com.nokia.carbide.cpp.pi.memory.showChunk", true); //$NON-NLS-1$
		return false;
	}

	/**
	 * Function for checking if stack/heap view is enabled
	 * 
	 * @return boolean value that is true when stack/heap view is enabled
	 */
	private boolean isShowHeapStackEnabled() {
		// if there is a showHeapStack value associated with the current
		// Analyser tab, then use it
		Object obj = NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.memory.showHeapStack"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			return true;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					"com.nokia.carbide.cpp.pi.memory.showHeapStack", true); //$NON-NLS-1$
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#setLegendTableVisible(boolean)
	 */
	public void graphVisibilityChanged(boolean value){
		if(holder != null){
			holder.setVisible(value);
			holder.getParent().layout();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#setLegendTableMaximized(boolean)
	 */
	public void graphMaximized(boolean value){
		if(holder != null){
			if(holder.getParent().getClass() == SashForm.class){
				SashForm sashForm = (SashForm)holder.getParent();
				if(value){
					sashForm.setMaximizedControl(holder);
				}
				else{
					sashForm.setMaximizedControl(null);

				}
			}
			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#isGraphMinimizedWhenOpened()
	 */
	public boolean isGraphMinimizedWhenOpened(){
		// Memory Graph is shown when view is opened
		return false;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("MemoryPlugin.pluginTitle"); //$NON-NLS-1$
	}	
}
