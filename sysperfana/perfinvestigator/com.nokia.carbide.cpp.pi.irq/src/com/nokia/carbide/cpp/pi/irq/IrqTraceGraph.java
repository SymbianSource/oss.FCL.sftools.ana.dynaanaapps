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

package com.nokia.carbide.cpp.pi.irq;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

/**
 * class for irq trace graph
 */
public class IrqTraceGraph extends GenericTraceGraph implements ITitleBarMenu,
		PIEventListener, MouseMotionListener {

	public static final char TYPE_IRQ = 1;
	public static final char TYPE_SWI = 2;
	public static final char TYPE_BOTH = 3;

	/* composite where tables are placed */
	private Composite holder;

	/* Irq trace */
	private IrqTrace irqTrace;
	private boolean readyToDraw = false;

	/* margin in the bottom of the graph */
	private static final int BOTTOM_MARGIN = 20;

	/* tables containing threads, functions and irq lines */
	private SwiThreadTable tableThreadIRQ;
	private SwiFunctionTable tableSWIFunction;
	private IrqLineTable tableIrqLine;

	/* composites for tables */
	private Composite compositeSWI;
	private Composite compositeIRQ;

	/* height of the the graph */
	private int panelHeight;

	/* hashtables that are used for drawing graphs */
	private Hashtable<String, Hashtable<RGB, TreeMap<Long, Integer>>> swiDrawList;
	private Hashtable<Long, Hashtable<RGB, TreeMap<Long, Integer>>> irqDrawList;

	/* is swi table shown */
	private boolean swiTableVisible = true;

	/* places of vectical dash lines */
	private ArrayList<Integer> interruptLines;

	/**
	 * Constructor
	 * 
	 * @param graphIndex
	 *            Index of the graph
	 * @param irqTrace
	 *            trace that is analysed
	 * @param uid
	 *            uid number of the trace
	 */
	public IrqTraceGraph(int graphIndex, IrqTrace irqTrace, int uid) {
		super((GenericSampledTrace) irqTrace);

		swiDrawList = new Hashtable<String, Hashtable<RGB, TreeMap<Long, Integer>>>();
		irqDrawList = new Hashtable<Long, Hashtable<RGB, TreeMap<Long, Integer>>>();

		this.irqTrace = irqTrace;

		// create the label and a tableviewer
		holder = new Composite(NpiInstanceRepository.getInstance()
				.getProfilePage(uid, graphIndex).getBottomComposite(), SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(holder, IrqPlugin.HELP_CONTEXT_ID_MAIN_PAGE);

		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.marginLeft = 0;
		gl.marginRight = 0;
		holder.setLayout(gl);

		// titlebar
		Label label = new Label(holder, SWT.CENTER);
		label
				.setBackground(holder.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
		label.setFont(PIPageEditor.helvetica_8);
		label.setText(Messages.IrqTraceGraph_0);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create composite for SWI tables
		compositeSWI = new Composite(holder, SWT.NONE);
		compositeSWI.setLayout(new FormLayout());
		compositeSWI.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create composite for irq table
		compositeIRQ = new Composite(holder, SWT.NONE);
		compositeIRQ.setLayout(new FormLayout());
		compositeIRQ.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create Thread table
		tableThreadIRQ = new SwiThreadTable(this, compositeSWI);

		// vertical sash
		Sash acrossSash = new Sash(compositeSWI, SWT.VERTICAL);

		// Create SWI function table
		tableSWIFunction = new SwiFunctionTable(this, compositeSWI);

		// Create Thread table
		tableIrqLine = new IrqLineTable(this, compositeIRQ);

		// Set layout data for tables and sash

		// FormData for IRQ/Threads
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(acrossSash);
		tableThreadIRQ.getTable().setLayoutData(formData);

		// FormData for SWT functions
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(acrossSash);
		formData.right = new FormAttachment(100);
		tableSWIFunction.getTable().setLayoutData(formData);

		// FormData for irq table
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		tableIrqLine.getTable().setLayoutData(formData);

		// FormData for acrossSash
		// Put it initially in the middle
		formData = new FormData();
		formData.left = new FormAttachment(50);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		acrossSash.setLayoutData(formData);

		// add selection listener to sash so that user can change sash's place
		final FormData acrossSashData = formData;
		final Composite parentFinal = acrossSash.getParent();
		acrossSash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail != SWT.DRAG) {
					acrossSashData.left = new FormAttachment(0, event.x);
					parentFinal.layout();
				}
			}
		});

		// initially set swi table visible
		this.setSwiTableVisible();
		this.readyToDraw = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#action(java
	 * .lang.String)
	 */
	public void action(String action) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paint(org.
	 * eclipse.draw2d.Panel, org.eclipse.draw2d.Graphics)
	 */
	public void paint(Panel panel, Graphics graphics) {

		// save height for tooltip
		this.panelHeight = panel.getSize().height;

		if (!this.readyToDraw) {
			return;
		}

		this.drawDottedLineBackground(graphics, BOTTOM_MARGIN, false);

		if (this.swiTableVisible) {
			// draw selected swi threads
			if (irqTrace.isFunctionNamesFound() == false) {
				this
						.drawText(
								graphics,
								Messages.IrqTraceGraph_1);
				return;

			}
			if (swiDrawList.size() == 0) {
				this
						.drawText(graphics,
								Messages.IrqTraceGraph_2);
				return;
			} else {
				int[] linePlaces = this.drawHorizontalLines(panel, graphics,
						swiDrawList.size());
				this.drawLineNames(panel, graphics, linePlaces);
				this.drawSoftwareInterrupts(panel, graphics, linePlaces);
			}

		} else {
			// draw selected irq lines
			if (irqDrawList.size() == 0) {
				this
						.drawText(graphics,
								Messages.IrqTraceGraph_3);
				return;
			} else {
				int[] linePlaces = this.drawHorizontalLines(panel, graphics,
						irqDrawList.size());
				this.drawLineNames(panel, graphics, linePlaces);
				this.drawHardwareInterrupts(panel, graphics, linePlaces);
			}
		}

		// draw dashed lines in the graph
		this.drawInterruptLines(graphics, panel.getSize().width);

		// draw the same selection as the Address/Thread trace
		this.drawSelectionSection(graphics, BOTTOM_MARGIN);
	}

	/**
	 * Draws interrupt count lines
	 * 
	 * @param graphics
	 *            graphics context
	 * @param width
	 *            width of the panel
	 */
	private void drawInterruptLines(Graphics graphics, int width) {

		// get color for line
		Color previousColor = graphics.getForegroundColor();
		graphics.setForegroundColor(holder.getDisplay().getSystemColor(
				SWT.COLOR_GRAY));

		if (interruptLines != null) {
			// draw lines only if space between them is less than 7
			if (interruptLines.get(0) > 7) {
				for (Integer item : interruptLines) {
					graphics.setLineStyle(Graphics.LINE_DASH);
					graphics.drawLine(0, item - 1, width, item - 1);
				}
			}
		}
		graphics.setForegroundColor(previousColor);
	}

	/**
	 * Draws text into graph
	 * 
	 * @param graphics
	 *            graphics where text is drawn
	 * @param text
	 *            text that is drawn
	 */
	private void drawText(Graphics graphics, String text) {
		// get current font and color
		Font previousFont = graphics.getFont();
		Color previousColor = graphics.getForegroundColor();

		// Set font size
		FontData[] fontData = previousFont.getFontData();
		fontData[0].height = 10;
		Font newFont = new Font(previousFont.getDevice(), fontData);

		// set text color as black
		Color newColor = holder.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		graphics.setForegroundColor(newColor);

		graphics.setFont(newFont);
		graphics.drawText(text, new Point(5, 5));

		// restore previous values to color and font
		graphics.setForegroundColor(previousColor);
		graphics.setFont(previousFont);
	}

	/**
	 * Gets places of horizontal lines between threads/irq lines
	 * 
	 * @param panelHeight
	 *            Height of the panel
	 * @param tableSize
	 *            amount of threads/irq lines
	 * @return array containing places of each line
	 */

	public int[] getHorizontalLinePlaces(double panelHeight, int tableSize) {

		// count height of one thread/irq line in graph
		int[] array = new int[tableSize];
		double spaceForOneThread = ((panelHeight - BOTTOM_MARGIN) / (double) tableSize);

		// draw lines
		int i = 0;
		while (i < array.length) {
			array[i] = (int) ((i + 1) * spaceForOneThread);
			i++;
		}
		return array;
	}

	/**
	 * Draws horizontal lines between threads/irq lines
	 * 
	 * @param panel
	 *            panel where lines are drawn
	 * @param graphics
	 *            graphics that is used for drawing
	 * @param tablesize
	 *            table's size which details are used for drawing
	 * @return array containing places of lines
	 */
	public int[] drawHorizontalLines(Panel panel, Graphics graphics,
			int tableSize) {

		// count height of one thread/irq line in graph
		int[] array = new int[tableSize];
		double height = (double) panel.getSize().height;
		double spaceForOneThread = ((height - BOTTOM_MARGIN) / (double) tableSize);

		// draw lines
		int i = 0;
		while (i < array.length) {
			array[i] = (int) ((i + 1) * spaceForOneThread);
			graphics.drawLine(new Point(0, array[i]), new Point(
					panel.getSize().width, array[i]));
			i++;
		}
		return array;
	}

	/**
	 * Draws thread names/irq lines into graph
	 * 
	 * @param panel
	 *            panel where names are drawn
	 * @param graphics
	 *            graphics that is used for drawing
	 * @param linePlaces
	 *            places of lines that are separating each line in the graph
	 */
	public void drawLineNames(Panel panel, Graphics graphics, int[] linePlaces) {

		if (linePlaces.length != 0) {

			// calculate font size
			int fontSize = (int) (linePlaces[0] * 0.7);

			if (fontSize > 10) {
				fontSize = 10;
			}

			if (fontSize == 0) {
				return;
			}

			Font previousFont = graphics.getFont();
			FontData[] fontData = previousFont.getFontData();
			fontData[0].height = fontSize;

			Font newFont = new Font(previousFont.getDevice(), fontData);

			graphics.setFont(newFont);

			int i = 0;

			if (swiTableVisible) {

				Enumeration<String> eventIterator = swiDrawList.keys();

				// go thru threads and draw its name into graph
				while (eventIterator.hasMoreElements()) {
					String threadName = eventIterator.nextElement();
					graphics.drawString(threadName, new Point(5, linePlaces[i]
							- linePlaces[0]));
					i++;

				}

			} else {
				Enumeration<Long> eventIterator = irqDrawList.keys();

				// go thru irq lines and draw its name into graph
				while (eventIterator.hasMoreElements()) {
					Long id = eventIterator.nextElement();
					String printText = IrqSampleTypeWrapper.getLineText(id);
					graphics.drawString(printText, new Point(5, linePlaces[i]
							- linePlaces[0]));
					i++;

				}
			}

			// restore previous font
			graphics.setFont(previousFont);

		}
	}

	/**
	 * Draws hardware interrupts
	 * 
	 * @param panel
	 *            panel where names are drawn
	 * @param graphics
	 *            graphics that is used for drawing
	 * @param linePlaces
	 *            places of lines that are separating each irq line in the graph
	 */
	public void drawHardwareInterrupts(Panel panel, Graphics graphics,
			int[] linePlaces) {

		Enumeration<Long> irqLineIterator = irqDrawList.keys();
		int tableItemIndex = 0;

		// go thru all irq lines
		while (irqLineIterator.hasMoreElements()) {

			Hashtable<RGB, TreeMap<Long, Integer>> irqLines = irqDrawList
					.get(irqLineIterator.nextElement());

			// draw this irq lines interrupts into graph
			this.drawInterrupts(graphics, tableItemIndex, linePlaces, irqLines,
					irqTrace.getMaxAmountOfIRQSamples());
			tableItemIndex++;

		}

	}

	/**
	 * Draws software interrupts
	 * 
	 * @param panel
	 *            panel where names are drawn
	 * @param graphics
	 *            graphics that is used for drawing
	 * @param table
	 *            table which details are used for drawing
	 * @param linePlaces
	 *            places of lines that are separating each irq line in the graph
	 */
	public void drawSoftwareInterrupts(Panel panel, Graphics graphics,
			int[] linePlaces) {

		Enumeration<String> threadIterator = swiDrawList.keys();
		int tableItemIndex = 0;

		// go thru all threads
		while (threadIterator.hasMoreElements()) {
			Hashtable<RGB, TreeMap<Long, Integer>> allThreadsFunctions = swiDrawList
					.get(threadIterator.nextElement());
			Hashtable<RGB, TreeMap<Long, Integer>> checkedFunctions = new Hashtable<RGB, TreeMap<Long, Integer>>();

			Enumeration<RGB> functionIterator = allThreadsFunctions.keys();

			// go thru each function from thread and gather draw data into
			// hashtable
			while (functionIterator.hasMoreElements()) {
				RGB thisColor = functionIterator.nextElement();
				TreeMap<Long, Integer> events = allThreadsFunctions
						.get(thisColor);
				checkedFunctions.put(thisColor, events);
			}
			// draw threads interrupts into graph
			this.drawInterrupts(graphics, tableItemIndex, linePlaces,
					checkedFunctions, irqTrace.getMaxAmountOfSWISamples());
			tableItemIndex++;

		}
	}

	/**
	 * draws one thread/irq lines interrupts into graph
	 * 
	 * @param graphics
	 *            graphics that is used for drawing
	 * @param tableItemIndex
	 *            index of the table item
	 * @param linePlaces
	 *            places of lines that are separating each thread/irq line in
	 *            the graph
	 * @param events
	 *            events of the thread/irq line
	 * @param maxAmountOfSamples
	 *            maximum amount of events in one time frame
	 */
	private void drawInterrupts(Graphics graphics, int tableItemIndex,
			int[] linePlaces, Hashtable<RGB, TreeMap<Long, Integer>> events,
			int maxAmountOfSamples) {

		// get scale for x axis.
		double scale = this.getScale();

		// get top and bottom of the irq line that is drawn into screen
		int top = 0;
		if (tableItemIndex != 0) {
			top = linePlaces[tableItemIndex - 1];
		}
		int bottom = linePlaces[tableItemIndex] - 1;

		// count one interrupts weight
		double weightOfOneInterrupt = (double) (bottom - top)
				/ (double) maxAmountOfSamples;

		Hashtable<Long, Integer> drawnInterrupts = new Hashtable<Long, Integer>();
		Enumeration<RGB> functionIterator = events.keys();

		Color previousColor = graphics.getForegroundColor();

		// go thru each color
		while (functionIterator.hasMoreElements()) {
			RGB thisRGB = functionIterator.nextElement();
			Color thisColor = new Color(holder.getDisplay(), thisRGB);
			TreeMap<Long, Integer> eventsFromOneColor = events.get(thisRGB);

			graphics.setForegroundColor(thisColor);

			Set<Long> eventSet = eventsFromOneColor.keySet();
			Iterator<Long> eventIterator = eventSet.iterator();

			// go thru each interrupt from table and draw them into graph
			while (eventIterator.hasNext()) {

				// check if this time frame already has events and calculate
				// place of line if needed
				Long thisTime = eventIterator.next();
				int amountOfEvents = eventsFromOneColor.get(thisTime);

				if (!drawnInterrupts.containsKey(thisTime)) {

					graphics.drawLine(new Point(thisTime / scale, bottom
							- weightOfOneInterrupt * amountOfEvents),
							new Point(thisTime / scale, bottom));
					drawnInterrupts.put(thisTime, amountOfEvents);
				} else {
					int previousValue = drawnInterrupts.get(thisTime);
					int newBottom = bottom
							- (int) (previousValue * weightOfOneInterrupt);
					graphics.drawLine(new Point(thisTime / scale, newBottom
							- weightOfOneInterrupt * amountOfEvents),
							new Point(thisTime / scale, newBottom));
					drawnInterrupts.put(thisTime, previousValue
							+ amountOfEvents);

				}

			}
		}
		graphics.setForegroundColor(previousColor);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paintLeftLegend
	 * (org.eclipse.draw2d.FigureCanvas, org.eclipse.swt.graphics.GC)
	 */
	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
		int lineItemCount = 0;
		int maxItemsPerMS = 0;

		if (gc == null) {
			return;
		}

		// Left legend not in use
		if (swiTableVisible) {
			lineItemCount = swiDrawList.size();
			maxItemsPerMS = irqTrace.getMaxAmountOfSWISamples();
		} else {
			lineItemCount = irqDrawList.size();
			maxItemsPerMS = irqTrace.getMaxAmountOfIRQSamples();

		}

		if (lineItemCount == 0) {
			return;
		}

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas
				.getClientArea();
		double heightOfCanvas = rect.height;
		int[] linePlaces = getHorizontalLinePlaces(heightOfCanvas,
				lineItemCount);
		double heightOfOneLine = (heightOfCanvas - BOTTOM_MARGIN)
				/ lineItemCount;

		interruptLines = new ArrayList<Integer>();

		this.drawLegendOfOneLine(gc, 0, (int) heightOfOneLine, maxItemsPerMS);
		for (int index = 1; index < linePlaces.length; index++) {
			this.drawLegendOfOneLine(gc, linePlaces[index - 1],
					(int) heightOfOneLine, maxItemsPerMS);
		}

	}

	/**
	 * Draws legend of one thread or irq line
	 * 
	 * @param graphics
	 *            graphics context
	 * @param top
	 *            place of the top of the line
	 * @param size
	 *            size of line
	 * @param maxValue
	 *            maximum value of interrupts at one ms
	 */
	private void drawLegendOfOneLine(GC graphics, int top, int size,
			int maxValue) {

		double quarterOfMaxValue = (double) maxValue / 4.0;
		int drawTop = top;
		int drawValue = maxValue;

		for (int index = 1; index < 5; index++) {

			String legend = Integer.toString(drawValue) + Messages.IrqTraceGraph_4;
			org.eclipse.swt.graphics.Point extent = graphics
					.stringExtent(legend);

			// texts are drawn only if size of line is greater than 50 pixels
			if (size > 50) {
				graphics.drawString(legend, IGenericTraceGraph.Y_LEGEND_WIDTH
						- extent.x - 4, (int) drawTop - 2);
			}

			// get next draw value
			drawValue = (int) (maxValue - (double) index * quarterOfMaxValue);
			double doublevalue = size
					- ((double) drawValue / (double) maxValue) * size;
			// get place of the line
			drawTop = (int) (top + doublevalue);

			// add line place into interruptLines
			if (index < 4) {
				interruptLines.add(drawTop);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#repaint()
	 */
	public void repaint() {
		if (parentComponent != null) {
			this.parentComponent.repaintComponent();
		}
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

	/**
	 * @return irq trace
	 */
	public IrqTrace getIrqTrace() {
		return irqTrace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph#getTitle()
	 */
	public String getTitle() {
		return Messages.IrqTraceGraph_5;
	}

	/**
	 * sets Irq table visible
	 */
	public void setIrqTableVisible() {
		this.setSwiTableVisible(false);
	}

	/**
	 * sets Swi table visible
	 */
	public void setSwiTableVisible() {
		this.setSwiTableVisible(true);

	}

	/**
	 * changes swi tables visibility according to parameter
	 * 
	 * @param value
	 */
	private void setSwiTableVisible(boolean value) {

		this.swiTableVisible = value;

		compositeIRQ.setVisible(!value);
		compositeSWI.setVisible(value);

		if (compositeIRQ.getLayoutData().getClass() == GridData.class) {
			((GridData) compositeIRQ.getLayoutData()).exclude = value;
		}
		if (compositeSWI.getLayoutData().getClass() == GridData.class) {
			((GridData) compositeSWI.getLayoutData()).exclude = !value;
		}
		holder.layout();
		this.repaint();
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

		// Action for showing swi interrupts
		Action showSwiTable = new Action() {
			public void run() {
				setSwiTableVisible();
			}

		};
		showSwiTable.setText(Messages.IrqTraceGraph_6);
		actionArrayList.add(showSwiTable);

		// Action for showing irq interrupts
		Action showIrqTable = new Action() {
			public void run() {
				setIrqTableVisible();
			}

		};
		showIrqTable.setText(Messages.IrqTraceGraph_7);
		actionArrayList.add(showIrqTable);

		return actionArrayList.toArray(new Action[actionArrayList.size()]);

	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#getContextHelpId()
	 */
	public String getContextHelpId() {
		return IrqPlugin.HELP_CONTEXT_ID_MAIN_PAGE;
	}

	/**
	 * Function that is called when thread is checked from the thread table
	 * 
	 * @param threadName
	 */
	public void threadChecked(String threadName) {

		Hashtable<RGB, TreeMap<Long, Integer>> hashAllFunctions = new Hashtable<RGB, TreeMap<Long, Integer>>();
		Hashtable<String, ArrayList<Long>> hash = irqTrace
				.getThreadsToFunctions().get(threadName);

		if (hash == null) {
			return;
		}
		Enumeration<String> it = hash.keys();

		// go thru function hashtable
		while (it.hasMoreElements()) {
			String threadname = it.nextElement();

			IrqSampleTypeWrapper wrapper = tableSWIFunction.getFunctionColors()
					.get(threadname);
			if (!wrapper.isSelected()) {
				continue;
			}

			// get add events from one function into hashOneFunction map
			TreeMap<Long, Integer> hashOneFunction = new TreeMap<Long, Integer>();
			ArrayList<Long> eventList = hash.get(threadname);
			for (Long item : eventList) {
				// if interrupt at this time does not yet exist create new map
				// item
				if (!hashOneFunction.containsKey(item)) {
					hashOneFunction.put(item, 1);
				}
				// if interrupt exists increase its values
				else {
					int previousvalue = hashOneFunction.get(item);
					hashOneFunction.put(item, previousvalue + 1);
				}
			}
			RGB rgp = wrapper.rgb;
			// save event into hash
			hashAllFunctions.put(rgp, hashOneFunction);
		}
		// add hashtable into draw list
		swiDrawList.put(threadName, hashAllFunctions);

	}

	/**
	 * function that is called when thread is unchecked
	 * 
	 * @param threadName
	 *            name of the thread
	 */
	public void threadUnchecked(String threadName) {

		// remove thread from draw list
		if (swiDrawList.containsKey(threadName)) {
			swiDrawList.remove(threadName);
		}

	}

	/**
	 * function that is called then irq line is checked
	 * 
	 * @param irqLine
	 *            irq line number
	 */
	public void irqLineChecked(IrqSampleTypeWrapper irqLine) {

		// get add events from one line into oneLinesInterrupts map
		TreeMap<Long, Integer> oneLinesInterrupts = new TreeMap<Long, Integer>();

		Vector<IrqSample> samples = irqLine.samples;
		for (IrqSample item : samples) {
			// if interrupt at this time does not yet exist create new map item
			if (!oneLinesInterrupts.containsKey(item.sampleSynchTime)) {
				oneLinesInterrupts.put(item.sampleSynchTime, 1);
			}
			// if interrupt exists increase its values

			else {
				int previousvalue = oneLinesInterrupts
						.get(item.sampleSynchTime);
				oneLinesInterrupts.put(item.sampleSynchTime, previousvalue + 1);
			}
		}

		// add hashtable into draw list
		RGB rgpColor = irqLine.rgb;
		Hashtable<RGB, TreeMap<Long, Integer>> drawTable = new Hashtable<RGB, TreeMap<Long, Integer>>();
		drawTable.put(rgpColor, oneLinesInterrupts);
		irqDrawList.put((long) irqLine.getPrototypeSample().getIrqL1Value(),
				drawTable);

	}

	/**
	 * function that is called when irq line is unchecked
	 * 
	 * @param irqLine
	 *            irq line number
	 */
	public void irqLineUnchecked(IrqSampleTypeWrapper irqLine) {

		// remove line from the draw list
		if (irqDrawList.containsKey((long) irqLine.getPrototypeSample()
				.getIrqL1Value())) {
			irqDrawList.remove((long) irqLine.getPrototypeSample()
					.getIrqL1Value());
		}
	}

	/**
	 * recalculated swi draw values for whole graph. This needs be done when
	 * functions are checked or unchecked
	 */
	public void recalculateWholeGraph() {

		// reset draw list
		swiDrawList = new Hashtable<String, Hashtable<RGB, TreeMap<Long, Integer>>>();
		Object[] wrappers = tableThreadIRQ.getTableViewer()
				.getCheckedElements();

		// go thru each checked thread and add them again into draw list
		for (Object item : wrappers) {
			if (item.getClass() == SwiThreadWrapper.class) {
				SwiThreadWrapper wrapper = (SwiThreadWrapper) item;
				this.threadChecked(wrapper.threadName);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.PIEventListener#piEventReceived
	 * (com.nokia.carbide.cpp.internal.pi.visual.PIEvent)
	 */
	public void piEventReceived(PIEvent be) {

		switch (be.getType()) {
		case PIEvent.SELECTION_AREA_CHANGED:

			// send this message to the 2 other graphs
			PIEvent be2 = new PIEvent(be.getValueObject(),
					PIEvent.SELECTION_AREA_CHANGED2);

			for (IrqTraceGraph item : irqTrace.getGraphs()) {
				if (item != this) {
					item.piEventReceived(be2);
				}
			}

			be = be2;

			// FALL THROUGH
		case PIEvent.SELECTION_AREA_CHANGED2:
			double[] values = (double[]) be.getValueObject();
			this.setSelectionStart(values[0]);
			this.setSelectionEnd(values[1]);
			this.updateIrqCountsInLegendsAsynch(TYPE_BOTH);
			this.repaint();
			break;
		default:
			break;
		}

	}

	/**
	 * updates count columns values based on the selected graph area
	 * asynchronously
	 * 
	 * @param updateType
	 *            value stating which legends need to be updated
	 */
	public void updateIrqCountsInLegendsAsynch(final int updateType) {

		Runnable refreshRunnable = new Runnable() {
			public void run() {
				updateIrqCountsInLegends(updateType);
			}
		};

		Display.getDefault().asyncExec(refreshRunnable);

	}

	/**
	 * updates count columns values based on the selected graph area
	 * 
	 * @param updateType
	 *            value stating which legends need to be updated
	 */
	public void updateIrqCountsInLegends(int updateType) {
		// get selection area
		double startTime = PIPageEditor.currentPageEditor().getStartTime();
		double endTime = PIPageEditor.currentPageEditor().getEndTime();
		long longStartTime = (long) (startTime * 1000);
		long longEndTime = (long) (endTime * 1000);

		Hashtable<RGB, Integer> counts = new Hashtable<RGB, Integer>();

		if (updateType == TYPE_BOTH || updateType == TYPE_SWI) {
			getColorCountsForSWILegend(counts, swiDrawList, longStartTime,
					longEndTime);
			setCountsForIrqLegendTable(tableSWIFunction.getTableViewer(),
					counts);
			tableSWIFunction.refreshTableViewer();
		}
		if (updateType == TYPE_BOTH || updateType == TYPE_IRQ) {
			getColorCountsForIRQLegend(counts, irqDrawList, longStartTime,
					longEndTime);
			setCountsForIrqLegendTable(tableIrqLine.getTableViewer(), counts);
			tableIrqLine.refreshTableViewer();
		}
	}

	/**
	 * Gets amount of interrupts for each color in the area between starttime
	 * and endtime
	 * 
	 * @param counts
	 *            amount of interrupts
	 * @param drawList
	 *            drawlist in use
	 * @param startTime
	 *            start time
	 * @param endTime
	 *            end time
	 */
	private void getColorCountsForSWILegend(Hashtable<RGB, Integer> counts,
			Hashtable<String, Hashtable<RGB, TreeMap<Long, Integer>>> drawList,
			long startTime, long endTime) {

		Enumeration<String> it = drawList.keys();
		// go thru drawlist and collect amounts for each color
		while (it.hasMoreElements()) {
			String threadName = it.nextElement();
			Hashtable<RGB, TreeMap<Long, Integer>> functionsFromThread = drawList
					.get(threadName);
			Enumeration<RGB> colorEnum = functionsFromThread.keys();

			// Go thru each color
			while (colorEnum.hasMoreElements()) {
				RGB color = colorEnum.nextElement();

				// get first event after start time
				TreeMap<Long, Integer> allEvents = functionsFromThread
						.get(color);

				// get tailmap for it
				SortedMap<Long, Integer> tailMap = allEvents.tailMap(startTime);
				Set<Long> set = tailMap.keySet();
				Iterator<Long> eventIterator = set.iterator();

				// save events into count array until entry is higher that end
				// time
				while (eventIterator.hasNext()) {
					long time = eventIterator.next();
					if (time > endTime) {
						break;
					}

					if (!counts.containsKey(color)) {
						counts.put(color, tailMap.get(time));
					} else {
						int previousValue = counts.get(color);
						counts.put(color, previousValue + tailMap.get(time));
					}
				}
			}
		}
	}

	/**
	 * Sets amount of interrupts into count column of legend table
	 * 
	 * @param tableViewer
	 *            viewer of the table in use
	 * @param counts
	 *            hashtable containing counts for each color
	 */
	private void setCountsForIrqLegendTable(TableViewer tableViewer,
			Hashtable<RGB, Integer> counts) {
		TableItem[] swItems = tableViewer.getTable().getItems();
		for (TableItem item : swItems) {
			if (item.getData().getClass().equals(IrqSampleTypeWrapper.class)) {
				IrqSampleTypeWrapper wrapper = (IrqSampleTypeWrapper) item
						.getData();

				RGB rgb = wrapper.rgb;

				if (counts.containsKey(rgb)) {
					wrapper.count = counts.get(rgb);
				} else {
					wrapper.count = 0;

				}

			}
		}
	}

	/**
	 * Gets amount of interrupts for each color in the area between starttime
	 * and endtime
	 * 
	 * @param counts
	 *            amount of interrupts
	 * @param drawList
	 *            drawlist in use
	 * @param startTime
	 *            start time
	 * @param endTime
	 *            end time
	 */
	private void getColorCountsForIRQLegend(Hashtable<RGB, Integer> counts,
			Hashtable<Long, Hashtable<RGB, TreeMap<Long, Integer>>> drawList,
			long startTime, long endTime) {
		Enumeration<Long> it = drawList.keys();

		// go thru drawlist and collect amounts for each color
		while (it.hasMoreElements()) {
			Long lineNumber = it.nextElement();
			Hashtable<RGB, TreeMap<Long, Integer>> colorsFromLine = drawList
					.get(lineNumber);
			Enumeration<RGB> colorEnum = colorsFromLine.keys();
			while (colorEnum.hasMoreElements()) {
				RGB color = colorEnum.nextElement();

				// get first event after start time
				TreeMap<Long, Integer> allEvents = colorsFromLine.get(color);

				// get tailmap for it
				SortedMap<Long, Integer> tailMap = allEvents.tailMap(startTime);
				Set<Long> set = tailMap.keySet();
				Iterator<Long> eventIterator = set.iterator();

				// save events into count array until entry is higher that end
				// time
				while (eventIterator.hasNext()) {
					long time = eventIterator.next();
					if (time > endTime) {
						break;
					}

					if (!counts.containsKey(color)) {
						counts.put(color, tailMap.get(time));
					} else {
						int previousValue = counts.get(color);
						counts.put(color, previousValue + tailMap.get(time));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseDragged(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseDragged(MouseEvent arg0) {
		// nothing to be done
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseEntered(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		// nothing to be done
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseExited(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		// nothing to be done
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseHover(org.eclipse.draw2d.
	 * MouseEvent)
	 */
	public void mouseHover(MouseEvent arg0) {
		// nothing to be done
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.
	 * MouseEvent)
	 */
	public void mouseMoved(MouseEvent event) {

		// get list sie that is shown
		double listSize = 0;
		if (swiTableVisible) {
			listSize = swiDrawList.size();
		} else {
			listSize = irqDrawList.size();

		}

		int graphHeight = panelHeight - BOTTOM_MARGIN;

		if (listSize == 0) {
			this.setToolTipText(null);
			return;
		}

		// get size of one item in the graph are
		double sizeOfOneItem = (double) graphHeight / listSize;

		// get index of item on top of which mouse is on
		int index = (int) (event.y / sizeOfOneItem);

		String tooltip = null;

		// go thru needed drawlist and set thread/irq line name as tooltip
		if (swiTableVisible) {
			Enumeration<String> enumeration = swiDrawList.keys();
			int ii = 0;
			while (enumeration.hasMoreElements()) {
				if (ii == index) {
					tooltip = enumeration.nextElement();
					break;
				}
				ii++;
				enumeration.nextElement();
			}
		} else {
			Enumeration<Long> enumeration = irqDrawList.keys();
			int ii = 0;
			while (enumeration.hasMoreElements()) {
				if (ii == index) {
					tooltip = IrqSampleTypeWrapper.getLineText(enumeration
							.nextElement());
					break;
				}
				ii++;
				enumeration.nextElement();
			}
		}

		this.setToolTipText(tooltip);

	}

	/**
	 * updates all table viewers of the graph
	 */
	public void updateTableViewers() {
		tableIrqLine.refreshTableViewer();
		tableSWIFunction.refreshTableViewer();
	}

	/**
	 * @return swi thread table
	 */
	public SwiThreadTable getTableThreadIRQ() {
		return tableThreadIRQ;
	}

	/**
	 * @return swi function table
	 */
	public SwiFunctionTable getTableSWIFunction() {
		return tableSWIFunction;
	}

	/**
	 * @return irq line table
	 */
	public IrqLineTable getTableIrqLine() {
		return tableIrqLine;
	}

}
