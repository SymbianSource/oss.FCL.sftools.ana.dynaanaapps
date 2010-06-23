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

package com.nokia.carbide.cpp.pi.graphicsmemory;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

public class GraphicsMemoryTraceGraph extends GenericTraceGraph implements
		FocusListener, PIEventListener, MouseMotionListener, IContextMenu,
		ITitleBarMenu {
	private enum UsageType {
		PRIVATE, SHARED, PRIVATE_SHARED
	};

	private boolean dynamicMemoryVisualisation = false;

	private GraphicsMemoryProcessTable graphicsMemoryProcessTable;
	private CheckboxTableViewer memoryTableViewer;

	// 3 tabs can share the same trace, but they need different graphs
	private GraphicsMemoryTrace memTrace;

	private FigureCanvas leftFigureCanvas;

	// whether any table items are enabled
	private boolean haveEnabled = false;

	private boolean readyToDraw = false;
	private int width = 600;
	private int height = 400;

	private UsageType paintMode;

	int[] sharedPoints;
	int[] privatePoints;

	private TreeMap<Long, Integer> eventPrivateListY;
	private TreeMap<Long, Integer> eventSharedListY;
	private TreeMap<Long, Integer> eventPrivateSharedListY;

	private int maxShared = 0;
	private int maxPrivate = 0;
	private int maxSharedPrivate = 0;
	private int maxUsedMemory = 0;

	private DecimalFormat memKBFormat = new DecimalFormat(Messages
			.getString("GraphicsMemoryTraceGraph.KBformat")); //$NON-NLS-1$
	private DecimalFormat memMBFloatFormat = new DecimalFormat(Messages
			.getString("GraphicsMemoryTraceGraph.MBformat")); //$NON-NLS-1$

	private static int xLegendHeight = 50;

	private boolean firstTimeDrawProcessList = true;

	private Composite holder;

	private boolean showMemoryUsageLine = true;

	public GraphicsMemoryTraceGraph(int graphIndex,
			GraphicsMemoryTrace memTrace, int uid) {
		super((GenericSampledTrace) memTrace);

		if (memTrace != null) {

			// if no version number is found from trace, we can assume
			// that the first version of the graphics memory model is in use
			this.memTrace = memTrace;
			if (memTrace.getVersion() == 0) {
				memTrace.setVersion(100);
			}
		}

		this.graphIndex = graphIndex;
		this.memTrace = memTrace;
		this.paintMode = UsageType.PRIVATE_SHARED;
		this.setScale(10);

		if (memTrace == null) {
			System.out.print(Messages
					.getString("GraphicsMemoryTraceGraph.traceDataNotFound")); //$NON-NLS-1$
			return;
		}

		memTrace.gatherDrawData();

		// create the label and a tableviewer
		holder = new Composite(NpiInstanceRepository.getInstance()
				.getProfilePage(uid, graphIndex).getBottomComposite(), SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(holder,
				GraphicsMemoryPlugin.HELP_CONTEXT_ID_MAIN_PAGE);

		holder.setLayout(new FillLayout());
		this.graphicsMemoryProcessTable = new GraphicsMemoryProcessTable(this,
				holder);

		this.memoryTableViewer = this.graphicsMemoryProcessTable
				.getTableViewer();

		this.readyToDraw = true;
	}

	public void action(String actionString) {
		if (actionString.equals("private_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.PRIVATE;
		} else if (actionString.equals("shared_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.SHARED;
		} else if (actionString.equals("private_shared_on")) //$NON-NLS-1$
		{
			paintMode = UsageType.PRIVATE_SHARED;
		} else if (actionString.equals("rescale_on")) //$NON-NLS-1$
		{
			dynamicMemoryVisualisation = true;
			makeProcessDrawLists();
		} else if (actionString.equals("rescale_off")) //$NON-NLS-1$
		{
			dynamicMemoryVisualisation = false;
			makeProcessDrawLists();
		} else if (actionString.equals("memory_usage_line_on")) //$NON-NLS-1$
		{
			showMemoryUsageLine = true;
		} else if (actionString.equals("memory_usage_line_off")) //$NON-NLS-1$
		{
			showMemoryUsageLine = false;
		} else {
			return;
		}

		this.repaint();
		if (this.leftFigureCanvas != null)
			this.leftFigureCanvas.redraw();
	}

	public void piEventReceived(PIEvent be) {
		switch (be.getType()) {
		// when the selection area changes, the maximum values shown for each
		// process
		// may change; if dynamicMemoryVisualisation is on, we'll also have a
		// new y-scaling factor
		case PIEvent.SELECTION_AREA_CHANGED:

			// before updating the tables, make sure that the memory trace has
			// computing the
			// maximum usage by each process within the new time interval
			double startTime = PIPageEditor.currentPageEditor().getStartTime();
			double endTime = PIPageEditor.currentPageEditor().getEndTime();

			memTrace.setMaxMemDataByInterval((int) (startTime * 1000),
					(int) (endTime * 1000));

			// send this message to the 2 other graphs
			PIEvent be2 = new PIEvent(be.getValueObject(),
					PIEvent.SELECTION_AREA_CHANGED2);

			for (int i = 0; i < 3; i++) {
				GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) memTrace
						.getTraceGraph(i);

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
			this.graphicsMemoryProcessTable.piEventReceived(be);
			this.repaint();
			break;

		case PIEvent.CHANGED_MEMORY_TABLE:
			this.makeProcessDrawLists();
			this.repaint();
			if (this.leftFigureCanvas != null)
				this.leftFigureCanvas.redraw();
			break;

		case PIEvent.SCALE_CHANGED:
			double scale = ((Double) be.getValueObject()).doubleValue();
			this.setScale(scale);
			this.repaint();
			break;

		case PIEvent.SCROLLED:
			Event event = ((Event) be.getValueObject());
			this.parentComponent.setScrolledOrigin(event.x, event.y,
					(FigureCanvas) event.data);
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
		this.makeProcessDrawLists();
		this.paintProcesses(graphics);
		this.drawDottedLineBackground(graphics,
				GraphicsMemoryTraceGraph.xLegendHeight);

		if (showMemoryUsageLine) {
			paintMemoryUsageLine(graphics);
		}
		// draw the same selection as the Address/Process trace
		this.drawSelectionSection(graphics,
				GraphicsMemoryTraceGraph.xLegendHeight);

	}

	private void paintMemoryUsageLine(Graphics graphics) {
		Color black = ColorPalette.getColor(new RGB(0, 0, 0));
		graphics.setBackgroundColor(black);
		graphics.setForegroundColor(black);
		PointList pl = new PointList();
		Iterator<GraphicsMemorySampleByTime> iterator = memTrace
				.getDrawDataByTime().iterator();
		while (iterator.hasNext()) {
			GraphicsMemorySampleByTime msbt = iterator.next();
			// calculate new x coord's value and round it to integer
			int xCoord = (int) ((msbt.getTime() / getScale()) + 0.5);
			int maxBytes;
			if (dynamicMemoryVisualisation) {

				if (paintMode == UsageType.PRIVATE) {
					maxBytes = maxPrivate;
				} else if (paintMode == UsageType.SHARED) {
					maxBytes = maxShared;
				} else {
					maxBytes = maxSharedPrivate;
				}
			} else {
				if (paintMode == UsageType.PRIVATE) {
					if (showMemoryUsageLine) {
						maxBytes = memTrace.getTraceTotalMemory();
					} else {
						maxBytes = memTrace.getTraceMaxPrivate();
					}
				} else if (paintMode == UsageType.SHARED) {
					maxBytes = memTrace.getTraceMaxShared();
				} else {
					if (showMemoryUsageLine) {
						maxBytes = memTrace.getTraceTotalMemory();
					} else {
						maxBytes = memTrace.getTraceMaxTotal();
					}
				}
			}

			int yMultiplier = prettyMaxBytes(maxBytes) / height;
			// calculate new y-coord's value and round it to integer
			int yCoord = (int) (((double) height - (double) msbt
					.getUsedMemory()
					/ yMultiplier) + 0.5);
			if(pl.size() > 0) {
				pl.addPoint(xCoord, pl.getLastPoint().y);
			}	
			pl.addPoint(xCoord, yCoord);
		}
		graphics.setLineWidth(2);
		graphics.drawPolyline(pl);
	}

	private void paintPrivates(Graphics graphics) {
		graphics.setBackgroundColor(ColorConstants.lightBlue);
		graphics.fillPolygon(privatePoints);
		graphics.setBackgroundColor(ColorConstants.gray);
	}

	private void paintShared(Graphics graphics, UsageType paintMode) {
		if (eventSharedListY == null)
			return;

		// if needed, move every y-value that is 0 down a little so that the
		// line is more visible
		// boolean increase0 = paintMode != UsageType.SHARED;

		if (paintMode == UsageType.SHARED) {
			graphics.setBackgroundColor(ColorConstants.lightGreen);
			graphics.fillPolygon(sharedPoints);
			graphics.setBackgroundColor(ColorConstants.gray);
		} else {
			int lineWidth = graphics.getLineWidth();
			Color color = graphics.getForegroundColor();

			graphics.setForegroundColor(ColorConstants.lightGreen);
			graphics.setLineWidth(2);
			graphics.drawPolyline(sharedPoints);
			graphics.setForegroundColor(color);
			graphics.setLineWidth(lineWidth);
		}
	}

	private void paintProcesses(Graphics graphics) {
		// paints treads when using event based memory model

		// if there are no processes to draw
		if (!haveEnabled)
			return;

		int maxBytes = 0;

		double yMultiplier;
		double xMultiplier;
		xMultiplier = this.getScale();

		// get las x coordinate of trace
		long lastEvent = ((GraphicsMemorySample) memTrace.samples
				.get(memTrace.samples.size() - 1)).sampleSynchTime;
		int lastXCoord = (int) (lastEvent / xMultiplier);

		if (paintMode == UsageType.PRIVATE && eventPrivateListY != null) {
			privatePoints = new int[eventPrivateListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxPrivate;
			else {
				if (showMemoryUsageLine) {
					maxBytes = memTrace.getTraceTotalMemory();
				} else {
					maxBytes = memTrace.getTraceMaxPrivate();
				}

			}

			// multiplier is bytes / pixel in the graph
			yMultiplier = prettyMaxBytes(maxBytes) / height;

			createGraphPolygon(this.eventPrivateListY, privatePoints,
					xMultiplier, yMultiplier, lastXCoord);

			this.paintPrivates(graphics);
		} else if (paintMode == UsageType.SHARED && eventSharedListY != null) {
			sharedPoints = new int[eventSharedListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxShared;
			else {
				maxBytes = memTrace.getTraceMaxShared();				
			}

			// multiplier is bytes / pixel in the final graph
			yMultiplier = prettyMaxBytes(maxBytes) / height;

			createGraphPolygon(this.eventSharedListY, sharedPoints,
					xMultiplier, yMultiplier, lastXCoord);
			this.paintShared(graphics, paintMode);
		} else if (eventPrivateSharedListY != null) // private and shared
		{
			sharedPoints = new int[eventSharedListY.size() * 4 + 6];
			privatePoints = new int[eventPrivateListY.size() * 4 + 6];

			if (dynamicMemoryVisualisation)
				maxBytes = maxPrivate > maxShared ? maxPrivate : maxShared;
			else {
				if (showMemoryUsageLine) {
					maxBytes = memTrace.getTraceTotalMemory();
				} else {
					maxBytes = memTrace.getTraceMaxPrivate() > memTrace
							.getTraceMaxShared() ? memTrace
							.getTraceMaxPrivate() : memTrace
							.getTraceMaxShared();
				}
			}

			// multiplier is bytes / pixel in the final graph
			yMultiplier = prettyMaxBytes(maxBytes) / height;

			int totalSharedUsed = 1;

			createGraphPolygon(this.eventSharedListY, sharedPoints,
					xMultiplier, yMultiplier, lastXCoord);
			createGraphPolygon(this.eventPrivateListY, privatePoints,
					xMultiplier, yMultiplier, lastXCoord);

			this.paintPrivates(graphics);
			if (totalSharedUsed > 0)
				this.paintShared(graphics, paintMode);
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

	private void makeProcessDrawLists() {
		// Get checked table items
		Object[] checked = this.memoryTableViewer.getCheckedElements();

		// if no items is checked do nothing
		haveEnabled = (checked != null) && (checked.length > 0);
		if (!haveEnabled) {
			this.eventPrivateListY = null;
			this.eventSharedListY = null;
			this.eventPrivateSharedListY = null;
			return;
		}

		// create maps for events for private, shared and privateshared
		this.eventPrivateListY = new TreeMap<Long, Integer>();
		this.eventSharedListY = new TreeMap<Long, Integer>();
		this.eventPrivateSharedListY = new TreeMap<Long, Integer>();

		// go through checked items
		for (int j = 0; j < checked.length; j++) {
			// check that item is instance of memory process
			if (!(checked[j] instanceof GraphicsMemoryProcess))
				continue;

			GraphicsMemoryProcess memProcess = (GraphicsMemoryProcess) checked[j];

			// get all samples of the process
			TreeMap<Long, GraphicsMemorySample> memSamples = memTrace
					.getDrawDataByMemProcess(memProcess);

			// ensure that process has samples
			if ((memSamples == null) || (memSamples.size() == 0)) {
				System.out
						.println(Messages
								.getString("GraphicsMemoryTraceGraph.processNoSamples1") + memProcess.fullName + Messages.getString("GraphicsMemoryTraceGraph.processNoSamples")); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			// create empty sample
			GraphicsMemorySample previousSample = new GraphicsMemorySample(
					new GraphicsMemoryProcess(0, ""), //$NON-NLS-1$ //$NON-NLS-2$
					0);

			Iterator<GraphicsMemorySample> values = memSamples.values()
					.iterator();

			while (values.hasNext()) {
				GraphicsMemorySample memSample = values.next();
				// go thru samples from single threas
				// save changes after last received sample into TreeMaps
				addEventToTreeMap(this.eventSharedListY,
						memSample.sampleSynchTime, memSample.sharedSize
								- previousSample.sharedSize);
				addEventToTreeMap(this.eventPrivateListY,
						memSample.sampleSynchTime, memSample.privateSize
								- previousSample.privateSize);
				addEventToTreeMap(
						this.eventPrivateSharedListY,
						memSample.sampleSynchTime,
						memSample.privateSize
								+ memSample.sharedSize
								- (previousSample.sharedSize + previousSample.privateSize));
				previousSample = memSample;

			}
		}
		Hashtable<String, TreeMap<Long, GraphicsMemorySample>> memProcesses = memTrace
				.getDrawDataByMemProcess();
		Iterator<GraphicsMemorySample> iterator = memProcesses.get(
				GraphicsMemoryTraceParser.SAMPLE_TOTAL_MEMORY_PROCESS_NAME)
				.values().iterator();
		while (iterator.hasNext()) {
			this.maxUsedMemory = iterator.next().sharedSize;
			break;
		}

		// calculate max values and values in each event
		this.maxShared = calculateValuesInEachEvent(eventSharedListY);
		this.maxSharedPrivate = calculateValuesInEachEvent(eventPrivateSharedListY);
		this.maxPrivate = calculateValuesInEachEvent(eventPrivateListY);

		if (this.memTrace.getTraceMaxPrivate() == 0) {
			this.memTrace.setTraceMaxPrivate(maxPrivate);
			this.memTrace.setTraceMaxShared(maxShared);
			this.memTrace.setTraceMaxTotal(maxSharedPrivate);
			this.memTrace.setTraceTotalMemory(maxUsedMemory);
			// repaint left legend if this is first time that tread lists are
			// made
			if (firstTimeDrawProcessList) {
				this.parentComponent.paintLeftLegend();
				firstTimeDrawProcessList = false;
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

	public void setSize(int x, int y) {
		this.width = x;
		this.height = y - GraphicsMemoryTraceGraph.xLegendHeight;

		if (this.height <= 0)
			this.height = 1;
	}

	public Dimension getSize() {
		return new Dimension(width, height);
	}

	public void repaint() {
		this.parentComponent.repaintComponent();
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
		memoryStatsItem.setText(Messages
				.getString("GraphicsMemoryPlugin.memoryStats")); //$NON-NLS-1$
		memoryStatsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new GraphicsMemoryStatisticsDialog(Display.getCurrent());
			}
		});

		Object obj;

		boolean rescale = false;

		// if there is a rescale value associated with the current Analyser tab,
		// then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				GraphicsMemoryPlugin.PLUGIN_ID + ".rescale"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			rescale = (Boolean) obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".rescale", rescale); //$NON-NLS-1$

		final boolean rescaleFinal = rescale;

		MenuItem rescaleItem = new MenuItem(menu, SWT.CHECK);
		rescaleItem.setText(Messages
				.getString("GraphicsMemoryPlugin.dynamicRescale")); //$NON-NLS-1$
		rescaleItem.setSelection(rescale);
		rescaleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String action;
				NpiInstanceRepository
						.getInstance()
						.activeUidSetPersistState(
								GraphicsMemoryPlugin.PLUGIN_ID + ".rescale", !rescaleFinal); //$NON-NLS-1$
				if (!rescaleFinal) {
					action = "rescale_on"; //$NON-NLS-1$
				} else {
					action = "rescale_off"; //$NON-NLS-1$
				}

				for (int i = 0; i < 3; i++) {
					GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action(action);
				}
				GraphicsMemoryPlugin.getDefault().updateMenuItems();
			}
		});

		// if there is a show memory usage value associated with the current
		// Analyser tab, then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				GraphicsMemoryPlugin.PLUGIN_ID + ".showMemoryUsage"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showMemoryUsageLine = (Boolean) obj;
		else
			// set the initial value
			NpiInstanceRepository
					.getInstance()
					.activeUidSetPersistState(
							GraphicsMemoryPlugin.PLUGIN_ID + ".showMemoryUsage", showMemoryUsageLine); //$NON-NLS-1$

		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem memoryUsageLine = new MenuItem(menu, SWT.CHECK);
		memoryUsageLine.setText(Messages
				.getString("GraphicsMemoryTraceGraph.showTotalMemoryUsage")); //$NON-NLS-1$
		memoryUsageLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showMemoryUsageLine = !showMemoryUsageLine;
				NpiInstanceRepository
						.getInstance()
						.activeUidSetPersistState(
								GraphicsMemoryPlugin.PLUGIN_ID
										+ ".showMemoryUsage", showMemoryUsageLine); //$NON-NLS-1$
				repaint();
				GraphicsMemoryPlugin.getDefault().updateMenuItems();
			}
		});
		memoryUsageLine.setSelection(showMemoryUsageLine);

	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
		// if there are no processes to draw
		GC localGC = gc;

		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());

		if (this.leftFigureCanvas == null)
			this.leftFigureCanvas = figureCanvas;

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas
				.getClientArea();

		Combo titleCombo = ((GraphComposite) figureCanvas.getParent())
				.getTitleBarCombo();

		double visY = rect.height - GraphicsMemoryTraceGraph.xLegendHeight;

		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));

		int maxBytes = 0;

		if (paintMode == UsageType.PRIVATE) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxPrivate;
			else {
				if (showMemoryUsageLine) {
					maxBytes = memTrace.getTraceTotalMemory();
				} else {
					maxBytes = memTrace.getTraceMaxPrivate();
				}
			}

			titleCombo.select(0);
		} else if (paintMode == UsageType.SHARED) {
			if (dynamicMemoryVisualisation)
				maxBytes = maxShared;
			else
				maxBytes = memTrace.getTraceMaxShared();
			titleCombo.select(1);
		} else {
			if (dynamicMemoryVisualisation)
				maxBytes = maxPrivate > maxShared ? maxPrivate : maxShared;
			else {
				if (showMemoryUsageLine) {
					maxBytes = memTrace.getTraceTotalMemory();
				} else {
					maxBytes = memTrace.getTraceMaxPrivate() > memTrace
							.getTraceMaxShared() ? memTrace
							.getTraceMaxPrivate() : memTrace
							.getTraceMaxShared();
				}
			}
			titleCombo.select(2);
		}

		double multiplier = 0;

		multiplier = prettyMaxBytes(maxBytes) / visY;

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
				legend += bytes
						+ Messages.getString("GraphicsMemoryTraceGraph.byByte"); //$NON-NLS-1$
			}
			// if the amount is more than 512KB, draw it as KB
			else if (maxBytes <= 500 * 1024) {
				legend += (bytes / 1024)
						+ Messages.getString("GraphicsMemoryTraceGraph.byKB"); //$NON-NLS-1$
			} else {
				legend += memMBFloatFormat
						.format(((float) bytes / (1024 * 1024)))
						+ Messages.getString("GraphicsMemoryTraceGraph.byMB"); //$NON-NLS-1$
			}

			Point extent = gc.stringExtent(legend);

			gc.drawLine(IGenericTraceGraph.Y_LEGEND_WIDTH - 3, (int) y + 1,
					IGenericTraceGraph.Y_LEGEND_WIDTH, (int) y + 1);

			if (y >= previousBottom) {
				gc.drawString(legend, IGenericTraceGraph.Y_LEGEND_WIDTH
						- extent.x - 4, (int) y);
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

		if (y > this.getVisualSizeY() - GraphicsMemoryTraceGraph.xLegendHeight) {
			this.setToolTipText(null);
			return;
		}

		long privateSize = 0;
		long sharedSize = 0;
		if (memTrace.getVersion() == 100) {
			if (eventSharedListY != null) {
				Integer value = (Integer) GraphicsMemoryTrace
						.getFloorEntryFromMap((long) x, eventSharedListY);
				if (value != null) {
					sharedSize = value;
				}
			}
			if (eventPrivateListY != null) {
				Integer value = (Integer) GraphicsMemoryTrace
						.getFloorEntryFromMap((long) x, eventPrivateListY);
				if (value != null) {
					privateSize = value;
				}

			}
		} 
		int time = (int) x;

		if (this.paintMode == UsageType.PRIVATE_SHARED) {
			this
					.setToolTipText((time / 1000.0)
							+ Messages
									.getString("GraphicsMemoryTraceGraph.totalTooltip1") //$NON-NLS-1$
							+ memKBFormat.format((privateSize + 512) / 1024)
							+ Messages
									.getString("GraphicsMemoryTraceGraph.totalTooltip2") //$NON-NLS-1$
							+ memKBFormat.format((sharedSize + 512) / 1024));
		} else if (this.paintMode == UsageType.PRIVATE) {
			this
					.setToolTipText((time / 1000.0)
							+ Messages
									.getString("GraphicsMemoryTraceGraph.privateTooltip1") //$NON-NLS-1$
							+ (privateSize + 512)
							/ 1024
							+ Messages
									.getString("GraphicsMemoryTraceGraph.privateTooltip2")); //$NON-NLS-1$
		} else if (this.paintMode == UsageType.SHARED) {
			this
					.setToolTipText((time / 1000.0)
							+ Messages
									.getString("GraphicsMemoryTraceGraph.sharedTooltip1") //$NON-NLS-1$
							+ (sharedSize + 512)
							/ 1024
							+ Messages
									.getString("GraphicsMemoryTraceGraph.sharedTooltip2")); //$NON-NLS-1$
		} else
			return;

	}

	public int getGraphIndex() {
		return this.graphIndex;
	}

	public GraphicsMemoryTrace getMemTrace() {
		return this.memTrace;
	}

	public GraphicsMemoryProcessTable getGraphicsMemoryProcessTable() {
		return this.graphicsMemoryProcessTable;
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
		else if (bytes <= 40 * 1024 * 1024)
			bytes = 40 * 1024 * 1024;
		else if (bytes <= 50 * 1024 * 1024)
			bytes = 50 * 1024 * 1024;
		else if (bytes <= 60 * 1024 * 1024)
			bytes = 60 * 1024 * 1024;
		else if (bytes <= 80 * 1024 * 1024)
			bytes = 80 * 1024 * 1024;
		else if (bytes <= 100 * 1024 * 1024)
			bytes = 100 * 1024 * 1024;
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

		// Action for showing only privates
		Action actionShowPrivate = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", true); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", false); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("private_on"); //$NON-NLS-1$
				}
				GraphicsMemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowPrivate.setText(Messages
				.getString("GraphicsMemoryPlugin.showPrivate")); //$NON-NLS-1$

		// Action for showing only shareds
		Action actionShowShared = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", false); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", true); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("shared_on"); //$NON-NLS-1$
				}
				GraphicsMemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowShared.setText(Messages
				.getString("GraphicsMemoryPlugin.showShared")); //$NON-NLS-1$

		// Action for showing both shareds and privates
		Action actionShowBothItem = new Action() {
			public void run() {
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", true); //$NON-NLS-1$
				NpiInstanceRepository.getInstance().activeUidSetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", true); //$NON-NLS-1$

				for (int i = 0; i < 3; i++) {
					GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) memTrace
							.getTraceGraph(i);
					graph.action("private_shared_on"); //$NON-NLS-1$
				}
				GraphicsMemoryPlugin.getDefault().updateMenuItems();
			}
		};
		actionShowBothItem.setText(Messages
				.getString("GraphicsMemoryPlugin.showAll")); //$NON-NLS-1$

		actionArrayList.add(actionShowPrivate);
		actionArrayList.add(actionShowShared);
		actionArrayList.add(actionShowBothItem);

		// check which drawing mode is selected and set its action's state to
		// checked
		boolean showPrivate = isShowPrivateEnabled();
		boolean showShared = isShowSharedEnabled();

		if (showPrivate && !showShared) {
			actionShowPrivate.setChecked(true);
		} else if (showShared && !showPrivate) {
			actionShowShared.setChecked(true);
		} else {
			actionShowBothItem.setChecked(true);
		}

		return actionArrayList.toArray(new Action[actionArrayList.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#getContextHelpId
	 * ()
	 */
	public String getContextHelpId() {
		return GraphicsMemoryPlugin.HELP_CONTEXT_ID_MAIN_PAGE;
	}

	/**
	 * Function for checking if private view is enabled
	 * 
	 * @return boolean value that is true when private view is enabled
	 */

	private boolean isShowPrivateEnabled() {
		// if there is a showPrivate value associated with the current Analyser
		// tab, then use it
		Object obj = NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			return true;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", true); //$NON-NLS-1$
		return false;
	}

	/**
	 * Function for checking if shared view is enabled
	 * 
	 * @return boolean value that is true when shared view is enabled
	 */
	private boolean isShowSharedEnabled() {
		// if there is a showShared value associated with the current
		// Analyser tab, then use it
		Object obj = NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						GraphicsMemoryPlugin.PLUGIN_ID + ".showShared"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			return true;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", true); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#
	 * setLegendTableVisible(boolean)
	 */
	public void graphVisibilityChanged(boolean value) {
		if (holder != null) {
			holder.setVisible(value);
			holder.getParent().layout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#
	 * setLegendTableMaximized(boolean)
	 */
	public void graphMaximized(boolean value) {
		if (holder != null) {
			if (holder.getParent().getClass() == SashForm.class) {
				SashForm sashForm = (SashForm) holder.getParent();
				if (value) {
					sashForm.setMaximizedControl(holder);
				} else {
					sashForm.setMaximizedControl(null);

				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#
	 * isGraphMinimizedWhenOpened()
	 */
	public boolean isGraphMinimizedWhenOpened() {
		// Memory Graph is shown when view is opened
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("GraphicsMemoryPlugin.pluginTitle"); //$NON-NLS-1$
	}
}
