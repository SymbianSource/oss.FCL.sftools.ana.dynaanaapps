/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class AnalyzeToolGraph
 *
 */
package com.nokia.s60tools.analyzetool.internal.ui.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.FreeInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.SymReader;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.internal.ui.util.ColorUtil;
import com.nokia.s60tools.analyzetool.internal.ui.util.GraphUtils;
import com.nokia.s60tools.analyzetool.ui.MainView;
import com.nokia.s60tools.analyzetool.ui.ResourceVisitor;

/**
 * A FigureCanvas containing the graph and X-axis area of the AnalyzeTool chart.
 */
public class AnalyzeToolGraph extends FigureCanvas implements
		IMemoryActivityModelChangeListener {

	private static final int BOUNDARY_OFFSET = 3;
	/** used for range model listener */
	private static final String PROP_MAXIMUM = "maximum"; //$NON-NLS-1$
	private static final int X_AXIS_HEIGHT = 50;
	/** used for "Don't ask again" dialog */
	private static final String PROMPT_KEY = "GraphOptimisationPrompt";//$NON-NLS-1$

	/** the scaling factor used for scaling the x-axis */
	private double scale = 1.0;

	/** the model */
	private IMemoryActivityModel model;

	/** for synchronization with the PropertySheet */
	private DotSelectionProvider iDotsSelecProv = new DotSelectionProvider();
	private IWorkbenchPartSite site;

	/** used when user selects a dot on the graph, and moves with arrow keys */
	private ISelection iCurrentSelectedDot = null;

	/** controls mouse and arrow key movements */
	private MouseAndKeyController mc;
	private SymReader iSymReader = null;
	private IProject iCurrentProject = null;
	/** Contains c++ files info for the current project. */
	private final AbstractList<String> cppFileNames;

	/** "time ->" on axis */
	private Image timeImage;
	private GraphPartServiceListener iGraphPartServiceListener;
	private boolean optimisedDrawing;
	private boolean userInformed;

	/**
	 * The threshold value for filtered drawing of dots. Only draw the dot if
	 * the memory operation size is above or equals / below or equals the
	 * threshold. Zero for no filtering.
	 */
	private long threshold;
	/** indicates whether filtering is above or below the threshold */
	private boolean aboveThreshold;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent composite
	 */
	public AnalyzeToolGraph(Composite parent) {
		super(parent);
		IPartService partService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPartService();
		iGraphPartServiceListener = new GraphPartServiceListener();
		partService.addPartListener(iGraphPartServiceListener);
		PlatformUI.getWorkbench().addWindowListener(iGraphPartServiceListener);
		cppFileNames = new ArrayList<String>();
	}

	/**
	 * Draws the graph on the canvas. Intended to be called on a paint event.
	 * 
	 * @param graphics
	 */
	public void paint(final Graphics graphics) {
		if (optimisedDrawing && !userInformed) {
			userInformed = true;
			IPreferenceStore preferenceStore = Activator.getPreferences();
			if (!preferenceStore.getString(PROMPT_KEY).equals(
					MessageDialogWithToggle.ALWAYS)) {
				String dilaogTitle = "Optimised Graph";
				String message = "The process contains too many memory operations to display efficiently. To optimise, only leaks will be indicated on the graph.";
				String toggleMessage = "Don't show this again";

				MessageDialogWithToggle.openInformation(getShell(),
						dilaogTitle, message, toggleMessage, false,
						preferenceStore, PROMPT_KEY);
			}
		}
		Rectangle visibleRect = graphics.getClip(new Rectangle());
		YConverter yConverter = new YConverter(getClientArea().height, model
				.getHighestCumulatedMemoryAlloc());

		if (model.getSelectedProcess() != null
				&& model.getSelectedProcess().getAllocsFrees().size() > 0) {
			PointList pts = new PointList((model.getSelectedProcess()
					.getAllocsFrees().size() * 2) - 1);
			Point prevPt = null;
			List<Point> dotLocations = new ArrayList<Point>();
			List<Integer> colorDotLocations = new ArrayList<Integer>();

			for (BaseInfo info : model.getSelectedProcess().getAllocsFrees()) {
				int x_point = (int) ((info.getTime() - model
						.getFirstProcessTime()) / getScale());
				int y_point = yConverter.bytesToY(info.getTotalMem());

				if (y_point < 0) {
					y_point = 0;
				}
				if (prevPt != null) {
					pts.addPoint(x_point, prevPt.y);
				}
				Point nextPt = new Point(x_point, y_point);
				if (visibleRect.contains(nextPt)
						&& (isLeak(info) || (!optimisedDrawing
								&& !dotLocations.contains(nextPt) && validInThreshold(info)))) {
					// for improved performance, only draw dots that are in
					// visible clip area
					// and don't draw a dot if there is one already, unless it's
					// a leak
					dotLocations.add(nextPt);
					colorDotLocations.add(getColorForAllocType(info));
				}
				pts.addPoint(nextPt);
				prevPt = nextPt;
			}

			if (pts.size() > 0) {
				graphics.pushState();

				graphics.setForegroundColor(Display.getDefault()
						.getSystemColor(SWT.COLOR_DARK_YELLOW));
				graphics.setLineWidthFloat(optimisedDrawing ? 0.5f : 2.0f);
				graphics.drawPolyline(pts);

				graphics.setLineWidthFloat(optimisedDrawing ? 0.5f : 1.0f);
				graphics.setAntialias(SWT.ON);
				graphics.setForegroundColor(Display.getDefault()
						.getSystemColor(SWT.COLOR_RED));
				graphics.setBackgroundColor(Display.getDefault()
						.getSystemColor(SWT.COLOR_RED));
				int colourCode = SWT.COLOR_RED;
				for (int j = 0; j < dotLocations.size(); j++) {
					Point dotLocation = dotLocations.get(j);
					if (!optimisedDrawing
							&& colorDotLocations.get(j) != colourCode) {
						colourCode = colorDotLocations.get(j);
						graphics.setBackgroundColor(Display.getDefault()
								.getSystemColor(colourCode));
					}
					// paint the dot
					graphics.fillOval(dotLocation.x - 2, dotLocation.y - 2, 5,
							5);
					if (!optimisedDrawing && colourCode == SWT.COLOR_RED) {
						// draw a red line
						graphics.drawLine(dotLocation.x, dotLocation.y,
								dotLocation.x, yConverter.bytesToY(0));
					}
				}
				graphics.popState();
			}
		}
	}

	/**
	 * Returns true of the size of the alloc or free is greater or equals / less
	 * or equals the threshold. Returns true if no threshold is set.
	 * 
	 * @param info
	 *            the memory operation to check
	 * @return
	 */
	private boolean validInThreshold(BaseInfo info) {
		if (threshold <= 0
				|| info instanceof AllocInfo
				&& (aboveThreshold ? (((AllocInfo) info).getSizeInt() >= threshold)
						: (((AllocInfo) info).getSizeInt() <= threshold))) {
			return true;
		}

		if (info instanceof FreeInfo
				&& ((aboveThreshold && ((FreeInfo) info).getSizeInt() >= threshold) || (!aboveThreshold && ((FreeInfo) info)
						.getSizeInt() <= threshold))) {
			// check at least one of its alloc qualifies
			for (AllocInfo allocInfo : ((FreeInfo) info).getFreedAllocs()) {
				if ((aboveThreshold && allocInfo.getSizeInt() >= threshold)
						|| (!aboveThreshold && allocInfo.getSizeInt() <= threshold)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Draws the background grid on the canvas. Intended to be called on a paint
	 * event.
	 * 
	 * @param graphics
	 */
	public void drawBackGroundLines(Graphics graphics) {
		Rectangle canvasRect = graphics
				.getClip(new org.eclipse.draw2d.geometry.Rectangle());
		graphics.setForegroundColor(ColorUtil.COLOR_100); // middle grey
		graphics.setBackgroundColor(ColorUtil.COLOR_170); // lighter grey

		int height = getClientArea().height;
		int width = getClientArea().width;

		graphics.fillRectangle(new Rectangle(canvasRect.x, 0, width, height
				- X_AXIS_HEIGHT));

		double visY = height - X_AXIS_HEIGHT;

		int k = 0;
		// horizontal lines, height is divided into 10 sections, line is dotted
		// (5 pixel length)
		for (float y = 0; k <= 10; y += visY * 10000 / 100001, k++) {
			for (int x = canvasRect.x; x <= canvasRect.x + canvasRect.width; x += 5) {
				if ((x / 5) % 2 == 0)
					graphics.drawLine(x, ((int) y) + 1, x + 5, ((int) y) + 1);
			}
		}

		graphics.setForegroundColor(ColorUtil.COLOR_100);
		graphics.setBackgroundColor(ColorUtil.WHITE);

		int alignedLeftEdge = (canvasRect.x / 50) * 50;

		// vertical lines (one darker, one lighter vertical line in turns every
		// 50 points in width)
		if (width > 0) {
			for (int x = alignedLeftEdge; x <= canvasRect.x + canvasRect.width; x += 50) {
				if (x % 100 == 0)
					graphics.setForegroundColor(ColorUtil.COLOR_100);
				else
					graphics.setForegroundColor(ColorUtil.COLOR_200);

				for (int y = 0; y < height; y += 5) {
					if ((y / 5) % 2 == 0)
						graphics.drawLine(x, y, x, y + 5);
				}
			}
		}

		graphics.setForegroundColor(ColorUtil.COLOR_100);
		graphics.setBackgroundColor(ColorUtil.WHITE);

		for (int x = alignedLeftEdge; x <= canvasRect.x + canvasRect.width; x += 50) {
			String timeStringWithUnits = GraphUtils.getTimeStringWithUnits(x
					* scale);
			graphics.drawString(timeStringWithUnits, x + 5, height - 13);
		}
		if (timeImage == null) {
			timeImage = GraphUtils.getVerticalLabel("Time");
		}
		graphics.drawImage(timeImage, width / 2, height - 30);
	}

	/**
	 * Returns the content of the tooltip appropriate for the given coordinate
	 * 
	 * @param x
	 *            the X-coordinate
	 * @param y
	 *            the Y-coordinate
	 * @return String containing the content of the tooltip
	 */
	public String getToolTipText(int x, int y) {
		StringBuilder text = new StringBuilder();
		if (model.getSelectedProcess() == null) {
			return "";
		}

		if (y > getClientArea().height - X_AXIS_HEIGHT) {
			return null;
		}

		double timeInMs = (x * getScale());// x value in milliseconds

		YConverter yConverter = new YConverter(getClientArea().height, model
				.getHighestCumulatedMemoryAlloc());
		double bytes = yConverter.yToBytes(y); // y value in bytes

		text.append(GraphUtils.renderTime(timeInMs));

		BaseInfo allocInfo = findClosestAlloc((int) timeInMs,
				(int) ((x - BOUNDARY_OFFSET) * getScale()),
				(int) ((x + BOUNDARY_OFFSET) * getScale()), (int) bytes,
				(int) yConverter.yToBytes(y + BOUNDARY_OFFSET),
				(int) yConverter.yToBytes(y - BOUNDARY_OFFSET));
		if (allocInfo != null) {
			text.append(String.format("%n%s: %,d B%nTotal: %,d B",
					getMemOpString(allocInfo),
					Math.abs(allocInfo.getSizeInt()), allocInfo.getTotalMem()));
		} else {
			text.append(", " + GraphUtils.formatBytes(bytes));
		}
		return text.toString();
	}

	/**
	 * Returns a string representation of the type of the given memory operation
	 * 
	 * @param allocInfo
	 *            the memory operation to use
	 * @return
	 */
	private String getMemOpString(BaseInfo allocInfo) {
		return allocInfo instanceof AllocInfo ? (((AllocInfo) allocInfo)
				.isFreed() ? "Alloc" : "Leak") : "Free";
	}

	/**
	 * Convenience method for
	 * {@link #findClosestAlloc(int, int, int, int, int, int)} wrapping
	 * conversion from x,y coordinates to time and byte values.
	 * 
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return closest BaseInfo within bounds, or null if none found
	 */
	private BaseInfo findClosest(int x, int y) {
		if (y > getClientArea().height - X_AXIS_HEIGHT) {
			return null;
		}

		YConverter yConverter = new YConverter(getClientArea().height, model
				.getHighestCumulatedMemoryAlloc());
		return findClosestAlloc((int) (x * getScale()),
				(int) ((x - BOUNDARY_OFFSET) * getScale()),
				(int) ((x + BOUNDARY_OFFSET) * getScale()), (int) (yConverter
						.yToBytes(y)), (int) yConverter.yToBytes(y
						+ BOUNDARY_OFFSET), (int) yConverter.yToBytes(y
						- BOUNDARY_OFFSET));
	}

	/**
	 * Finds the closest matching BaseInfo in the model. BaseInfo has to fit
	 * into the given bounds and be a better match than other BaseInfo in the
	 * same bounds.
	 * 
	 * @param timeInMsMidPoint
	 *            time in milliseconds for the exact point of interest
	 * @param timeInMsBoundLeft
	 *            Left boundary for time in milliseconds
	 * @param timeInMsBoundRight
	 *            Right boundary for time in milliseconds
	 * @param bytesMidPoint
	 *            Cumulative memory in bytes for the exact point of interest
	 * @param bytesBoundLeft
	 *            Left boundary for cumulative memory in bytes
	 * @param bytesBoundRight
	 *            Right boundary for cumulative memory in bytes
	 * @return
	 */
	private BaseInfo findClosestAlloc(int timeInMsMidPoint,
			int timeInMsBoundLeft, int timeInMsBoundRight, int bytesMidPoint,
			int bytesBoundLeft, int bytesBoundRight) {
		BaseInfo ret = null;
		if (model.getSelectedProcess() == null) {
			return ret;
		}

		int marginEnd = timeInMsBoundRight;
		ProcessInfo process = model.getSelectedProcess();
		AbstractList<BaseInfo> allocsFrees = process.getAllocsFrees();
		Long firstTime = model.getFirstProcessTime();

		for (BaseInfo info : allocsFrees) {
			Long infoRelativeTime = info.getTime() - firstTime;
			// check current alloc info is within given bounds
			if (infoRelativeTime >= timeInMsBoundLeft
					&& infoRelativeTime <= marginEnd
					&& info.getTotalMem() >= bytesBoundLeft
					&& info.getTotalMem() <= bytesBoundRight) {
				// check whether current alloc info is a better match than
				// previously found
				if (ret == null
						|| (Math.abs(infoRelativeTime - timeInMsMidPoint) < Math
								.abs((ret.getTime() - firstTime)
										- timeInMsMidPoint))
						|| ((ret.getTime() - firstTime) == infoRelativeTime && Math
								.abs(info.getTotalMem() - bytesMidPoint) < Math
								.abs(ret.getTotalMem() - bytesMidPoint))) {
					ret = info;
				}
			} else if ((info.getTime() - firstTime) > timeInMsBoundRight) {
				break;
			}
		}
		return ret;
	}

	/**
	 * Finds the next AllocInfo in the model.
	 * 
	 * @param allocInfo
	 *            AllocInfo prior to the one to return
	 * @return The next AllocInfo in the model. This may return null if the
	 *         passed object was the last in the model.
	 */
	private BaseInfo findNextAlloc(BaseInfo allocInfo, boolean forward) {
		BaseInfo ret = null;
		ProcessInfo processInfo = model.getSelectedProcess();
		if (processInfo == null) {
			return ret;
		}

		AbstractList<BaseInfo> allocsFrees = processInfo.getAllocsFrees();
		int i = allocsFrees.indexOf(allocInfo);
		if (forward) {
			if (i < allocsFrees.size() - 1) {
				ret = allocsFrees.get(i + 1);
			}
		} else {
			if (i > 0) {
				ret = allocsFrees.get(i - 1);
			}
		}
		return ret;
	}

	/**
	 * This method first zooms in graph to the maximum possible scale and zooms
	 * out so that it fits in the canvas area.
	 * 
	 */
	public void zoomGraph() {
		int width = getClientArea().width;

		if (width <= 0 || model == null)
			return;
		double new_scale = getMaxTimeValueInMilliSeconds() / width;
		setScale(new_scale);
		setZoomedSize(0);
	}

	/**
	 * Returns the current scaling factor for the graph's width
	 * 
	 * @return Scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the scaling factor for the graph's width
	 * 
	 * @param newScale
	 */
	public void setScale(double newScale) {
		this.scale = newScale;
	}

	/**
	 * Returns the highest time value of the current graph in milliseconds
	 * 
	 * @return Last time
	 */
	public long getLastTimeValueInMilliSeconds() {
		return model.getLastProcessTime() - model.getFirstProcessTime();
	}

	/**
	 * Returns the last time value in the model plus 1 per cent.
	 * 
	 * @return
	 */
	private long getMaxTimeValueInMilliSeconds() {
		return getLastTimeValueInMilliSeconds()
				+ (int) (getLastTimeValueInMilliSeconds() * 0.01);
	}

	/**
	 * Adds a new model to this class
	 * 
	 * @param newModel
	 *            the IMemoryActivityModel to use
	 */
	public void setInput(IMemoryActivityModel newModel) {
		if (this.model != null) {
			this.model.removeListener(this);
		}
		threshold = 0; // reset threshold
		this.model = newModel;
		this.model.addListener(this);
	}

	/**
	 * Creates the content of the FigureCanvas. Intended to be called once in
	 * creating the ViewPart content.
	 */
	public void createContent() {
		mc = new MouseAndKeyController();
		Panel panel = new Panel() {
			@Override
			public void paint(Graphics graphics) {
				if (model != null) {
					drawBackGroundLines(graphics);
					AnalyzeToolGraph.this.paint(graphics);
					mc.render(graphics);
				} else {
					erase();
				}
			}
		};

		panel.setLayoutManager(new XYLayout());
		panel.addMouseMotionListener(mc);
		panel.addMouseListener(mc);
		panel.addKeyListener(mc);

		setContents(panel);
		panel.setFocusTraversable(true);
		final org.eclipse.swt.widgets.ScrollBar horizontalBar = getHorizontalBar();

		horizontalBar.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing by design
			}

			public void widgetSelected(SelectionEvent event) {
				AnalyzeToolGraph.this.redraw();
			}

		});

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				horizontalBar.setPageIncrement(getBounds().width);
				redraw();
			}
		});
	}

	/**
	 * Class containing code to deal with mouse and key events
	 * 
	 */
	private class MouseAndKeyController implements MouseMotionListener,
			MouseListener, KeyListener {

		protected int mouseButton;
		protected Point start;
		protected boolean beingDragged;
		private BaseInfo lastShownAlloc;
		protected Point lastMouse;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseMotionListener#mouseDragged(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseDragged(MouseEvent me) {
			if (mouseButton == 1) {// for some reason me.button doesn't work
				// here and always returns 0 so we need to
				// track it from mouse pressed
				beingDragged = true;
				lastMouse = me.getLocation();
			} else {
				beingDragged = false;
			}
		}

		public void render(Graphics graphics) {
			graphics.pushState();
			if (beingDragged) {
				graphics.setForegroundColor(Display.getDefault()
						.getSystemColor(SWT.COLOR_BLACK));
				graphics.setLineWidth(1);

				int dragEnd = lastMouse.x;
				int dragLeftX = start.x <= dragEnd ? start.x : dragEnd;// drag
				// area
				// left
				// edge
				int dragRightX = start.x <= dragEnd ? dragEnd : start.x;// drag
				// area
				// right
				// edge
				graphics.drawRectangle(new Rectangle(dragLeftX, 0,
						(dragRightX - dragLeftX), getClientArea().height));
			}

			if (lastShownAlloc != null) {

				// for the selected dot, try to mark related allocs and free
				FreeInfo freeInfo = null;
				if (lastShownAlloc instanceof FreeInfo) {
					freeInfo = (FreeInfo) lastShownAlloc;
				} else {
					freeInfo = ((AllocInfo) lastShownAlloc).getFreedBy();
				}
				if (freeInfo != null) {
					YConverter yConverter = new YConverter(
							getClientArea().height, model
									.getHighestCumulatedMemoryAlloc());
					Rectangle visibleRect = graphics
							.getClip(new org.eclipse.draw2d.geometry.Rectangle());
					Point pfree = getLocationOnGraph(yConverter, freeInfo);
					if (lastShownAlloc != freeInfo
							&& visibleRect.contains(pfree)) {
						graphics.setForegroundColor(Display.getDefault()
								.getSystemColor(SWT.COLOR_DARK_GREEN));
						graphics.drawLine(pfree.x + 1, pfree.y + 3,
								pfree.x + 1, yConverter.bytesToY(0));
						if (optimisedDrawing) { // draw circle as well since we
							// don't have dots
							graphics.drawOval(pfree.x - 2, pfree.y - 2, 5, 5);
						}
					}
					for (AllocInfo freedAlloc : freeInfo.getFreedAllocs()) {
						Point pAlloc = getLocationOnGraph(yConverter,
								freedAlloc);
						if (lastShownAlloc != freedAlloc
								&& visibleRect.contains(pAlloc)) {
							graphics.setForegroundColor(Display.getDefault()
									.getSystemColor(SWT.COLOR_DARK_BLUE));
							graphics.drawLine(pAlloc.x + 1, pAlloc.y + 3,
									pAlloc.x + 1, yConverter.bytesToY(0));
							if (optimisedDrawing) { // draw circle as well since
								// we don't have dots
								graphics.drawOval(pAlloc.x - 2, pAlloc.y - 2,
										5, 5);
							}
						}

					}
				}
				// this should be the alloc that has its details displayed
				// mark it in the graph
				// draw a small circle around the alloc / dealloc, and a
				// vertical line towards the X-axis
				Point p = getLocationOnGraph(lastShownAlloc);
				graphics.setForegroundColor(Display.getDefault()
						.getSystemColor(SWT.COLOR_CYAN));
				graphics.setLineWidth(1);
				graphics.drawOval(p.x - 2, p.y - 2, 5, 5);
				if (optimisedDrawing || !isLeak(lastShownAlloc)) {
					graphics.drawLine(p.x + 1, p.y + 3, p.x + 1,
							getClientArea().height - X_AXIS_HEIGHT);
				}
			}
			graphics.popState();
		}

		private Point getLocationOnGraph(BaseInfo alloc) {
			YConverter yConverter = new YConverter(getClientArea().height,
					model.getHighestCumulatedMemoryAlloc());
			return getLocationOnGraph(yConverter, alloc);
		}

		private Point getLocationOnGraph(YConverter yConverter, BaseInfo alloc) {
			int x = (int) ((alloc.getTime() - model.getFirstProcessTime()) / getScale());
			int y = yConverter.bytesToY(alloc.getTotalMem());

			if (y < 0) {
				y = 0;
			}
			return new Point(x, y);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseMotionListener#mouseEntered(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			// do nothing by design
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseMotionListener#mouseExited(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			// do nothing by design
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseMotionListener#mouseHover(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseHover(MouseEvent e) {
			if (model != null) {
				setToolTipText(getToolTipText(e.x, e.y));
				redraw();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseMoved(MouseEvent e) {
			// do nothing by design
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseDoubleClicked(MouseEvent e) {
			beingDragged = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.
		 * MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			beingDragged = false;
			start = e.getLocation();
			mouseButton = e.button;
			e.consume(); // don't pass on to other listeners
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d
		 * .MouseEvent)
		 */
		public void mouseReleased(MouseEvent me) {

			if (beingDragged) {
				beingDragged = false;
				// finished drag - perform zoom in now

				// set new scale
				int dragEnd = me.getLocation().x;
				int dragLeftX = start.x <= dragEnd ? start.x : dragEnd;// drag
				// area
				// left
				// edge
				int dragRightX = start.x <= dragEnd ? dragEnd : start.x;// drag
				// area
				// right
				// edge
				int dragWindowStartTime = (int) (dragLeftX * getScale());

				int dragWidth = dragRightX - dragLeftX;// drag area width
				int oldVisibleWidth = getViewport().getSize().width;// visible
				// window
				// width
				double newScale = getScale() * dragWidth / oldVisibleWidth;
				if (newScale < 1.0) {
					// don't allow a scale smaller than 1 millisecond per pixel
					// TODO: indicate a failed zoom
					return;
				}

				int dragLeftXScaled = (int) (dragWindowStartTime / newScale);

				// set new window location
				setScale(newScale);
				setZoomedSize(dragLeftXScaled);

				// make sure the drag window disappears
				AnalyzeToolGraph.this.redraw();
			} else if (me.button == 3) {
				// mouse right click - zoom out

				double maxScale = getMaxTimeValueInMilliSeconds()
						/ getClientArea().width;
				if (getScale() < maxScale) {
					double newScale = GraphUtils.nextScale(getScale(), true);
					if (getScale() != newScale) {
						if (newScale > maxScale) {
							newScale = maxScale;
						}

						// get left window edge is ms to be set to after zooming
						double leftEdgeInMs = getViewport().getViewLocation().x
								* getScale();
						setScale(newScale);
						setZoomedSize((int) (leftEdgeInMs / newScale));
					}
				}
			} else if (me.button == 1) {
				BaseInfo info = findClosest(me.x, me.y);
				if (info != null) {
					iDotsSelecProv
							.setSelection(new StructuredSelection(
									new MemOpDescriptor(model
											.getFirstProcessTime(), info,
											iCurrentProject, iSymReader,
											cppFileNames, model
													.getCallstackManager())));
					lastShownAlloc = info;
				}
			}
		}

		public void keyPressed(KeyEvent ke) {
			// do nothing by design
		}

		public void keyReleased(KeyEvent ke) {
			if (ke.keycode == SWT.ARROW_RIGHT || ke.keycode == SWT.ARROW_LEFT) {
				if (lastShownAlloc != null) {
					BaseInfo info = findNextAlloc(lastShownAlloc,
							ke.keycode == SWT.ARROW_RIGHT);
					if (info != null) {
						iDotsSelecProv.setSelection(new StructuredSelection(
								new MemOpDescriptor(
										model.getFirstProcessTime(), info,
										iCurrentProject, iSymReader,
										cppFileNames, model
												.getCallstackManager())));
						lastShownAlloc = info;

						// if info is hidden from the visible graph area, scroll
						// to reveal
						int x = getLocationOnGraph(info).x;
						int leftEdge = getViewport().getHorizontalRangeModel()
								.getValue();
						int width = getViewport().getHorizontalRangeModel()
								.getExtent();
						if (x < leftEdge) {
							if (x > 10) {
								x -= 10;
							}
							getViewport().getHorizontalRangeModel().setValue(x);
						} else if (x > (leftEdge + width)) {
							x -= (width - 10);
							getViewport().getHorizontalRangeModel().setValue(x);
						}
						AnalyzeToolGraph.this.redraw();
					}
				}
			}
		}

		/**
		 * Resets any state.
		 */
		public void clearState() {
			mouseButton = 0;
			start = null;
			beingDragged = false;
			lastShownAlloc = null;
			lastMouse = null;
			if (iDotsSelecProv != null) {
				iDotsSelecProv.setSelection(StructuredSelection.EMPTY);
			}
		}
	}

	/**
	 * Returns an SWT system color code depending on the type of the paramter
	 * passed. </br> SWT.COLOR_DARK_BLUE for a alloc (that is not a leak) </br>
	 * SWT.COLOR_RED for a leak </br> SWT.COLOR_DARK_GREEN for a free
	 * 
	 * @param info
	 *            the allocation to evaluate
	 * @return the appropriate SWT system color code
	 */
	private int getColorForAllocType(BaseInfo info) {
		int color;
		if (info instanceof AllocInfo) {
			if (((AllocInfo) info).isFreed()) {
				color = SWT.COLOR_DARK_BLUE;
			} else {
				color = SWT.COLOR_RED;
			}
		} else {
			color = SWT.COLOR_DARK_GREEN;
		}
		return color;
	}

	/**
	 * Returns true if the passed info is a leak, false otherwise. AllocInfo
	 * that haven't been freed are considered leaks.
	 * 
	 * @param info
	 *            the BaseInfo to test
	 * @return true if leak, false otherwise
	 */
	private static boolean isLeak(BaseInfo info) {
		return info instanceof AllocInfo && !(((AllocInfo) info).isFreed()) ? true
				: false;
	}

	/**
	 * 
	 * Converts bytes from/to Y values
	 * 
	 */
	class YConverter {
		private final double visY;
		private final double multiplier;

		public YConverter(int clientAreaHeight, int maxAllocValueInBytes) {
			visY = clientAreaHeight - X_AXIS_HEIGHT;
			multiplier = GraphUtils.prettyMaxBytes(maxAllocValueInBytes) / visY;
		}

		public int bytesToY(int bytes) {
			return (int) (visY - (bytes / multiplier));
		}

		public double yToBytes(int y) {
			return (visY - y) * multiplier;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener
	 * #onProcessesAdded()
	 */
	public void onProcessesAdded() {
		// the model is now ready to use - call a redraw on the graph
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				mc.clearState();
				zoomGraph();
				redraw();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener
	 * #onProcessSelected
	 * (com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo)
	 */
	public void onProcessSelected(ProcessInfo p) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				mc.clearState();
				optimisedDrawing = model.getSelectedProcess().getAllocsFrees()
						.size() > ChartContainer.OPT_DRAWING_LIMIT ? true
						: false;
				userInformed = false;
				zoomGraph();
				redraw();
			}
		});
	}

	@Override
	public void dispose() {
		// there is no dispose() entry point in this class; we may have to call
		// this via MainView.dispose()
		site.setSelectionProvider(null);
		iDotsSelecProv = null;
		if (timeImage != null) {
			timeImage.dispose();
		}
		IPartService partService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPartService();
		partService.removePartListener(iGraphPartServiceListener);
		iGraphPartServiceListener = null;
		if (iSymReader != null) {
			iSymReader.dispose();
			iSymReader = null;
		}
	}

	private void setZoomedSize(final int newXLocation) {
		int prefSize = (int) (getMaxTimeValueInMilliSeconds() / getScale());

		// new width has to propagate to viewport first before we can set
		// the new viewport selection
		// set newXLocation in listener
		Panel panel = (Panel) (getContents());
		panel.setPreferredSize(prefSize, 0);

		if (prefSize > getClientArea().width) {
			getViewport().getHorizontalRangeModel().addPropertyChangeListener(
					new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent e) {
							if (e.getPropertyName().equals(PROP_MAXIMUM)) {
								getViewport().getHorizontalRangeModel()
										.removePropertyChangeListener(this);
								getViewport().getHorizontalRangeModel()
										.setValue(newXLocation);
							}
						}

					});
			panel.setSize(prefSize, 0);

		} else {
			// this only works if the canvas is large enough, otherwise use
			// property listener
			getViewport().getHorizontalRangeModel().setValue(newXLocation);
		}
	}

	/**
	 * DotSelectionProvider : when a user selects a Dot on the graph, this class
	 * delivers the associated descriptor MemOpDescriptor to the views
	 * interested in it mainly the Properties View.
	 * 
	 */
	private class DotSelectionProvider implements ISelectionProvider,
			SelectionListener {
		private ListenerList iSelectionChangedListeners = new ListenerList();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
		 * (org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(
				ISelectionChangedListener listener) {
			// TODO is there a way to allow only properties view to register.
			iSelectionChangedListeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return iCurrentSelectedDot;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.eclipse.jface.viewers.ISelectionProvider#
		 * removeSelectionChangedListener
		 * (org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			iSelectionChangedListeners.remove(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
		 * .jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection) {
			iCurrentSelectedDot = selection;
			// notify the listeners mainly the property view
			for (final Object listenerObj : iSelectionChangedListeners
					.getListeners()) {
				((ISelectionChangedListener) listenerObj)
						.selectionChanged(new SelectionChangedEvent(this,
								getSelection()));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org
		 * .eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent event) {
			widgetSelected(event);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse
		 * .swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent arg0) {
			for (final Object listenerObj : iSelectionChangedListeners
					.getListeners()) {
				((ISelectionChangedListener) listenerObj)
						.selectionChanged(new SelectionChangedEvent(this,
								getSelection()));
			}
		}
	}

	/**
	 * GraphPartServiceListener is for convenience only. It serves to find an
	 * appropriate time to add DotSelectionProvider to the part site. It also
	 * covers that case where the properties View is already open before opening
	 * AnalyzeTool View.
	 * 
	 */
	private class GraphPartServiceListener implements IPartListener,
			IWindowListener {

		public void partActivated(IWorkbenchPart part) {
			if (part instanceof MainView || part instanceof PropertySheet) {

				site = part.getSite();
				// set property sheet selection provider.
				site.setSelectionProvider(iDotsSelecProv);
				if (part instanceof MainView && iSymReader != null) {
					iSymReader.reOpenCachedSymbolFiles();
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			// do nothing
		}

		public void partClosed(IWorkbenchPart part) {
			// TODO this is not working. why?
			if (part instanceof MainView || part instanceof PropertySheet) {
				site = part.getSite();
				iDotsSelecProv.setSelection(StructuredSelection.EMPTY);
				// set property sheet selection provider.
				site.setSelectionProvider(null);
			}
		}

		public void partDeactivated(IWorkbenchPart part) {
			// the user might want to rebuild the project, so close the sumbol
			// files
			if (part instanceof MainView && iSymReader != null) {
				iSymReader.closeCachedSymbolFiles();
			}
		}

		public void partOpened(IWorkbenchPart part) {
			if (part instanceof MainView) {
				try {
					part.getSite().getPage().showView(
							"org.eclipse.ui.views.PropertySheet");//$NON-NLS-1$
				} catch (PartInitException e) {
					// just log the exception
					Activator.getDefault()
							.log(IStatus.ERROR, e.getMessage(), e);
				}
			}
		}

		public void windowActivated(IWorkbenchWindow window) {
			if (iSymReader != null
					&& window.getActivePage().getActivePart() instanceof MainView) {
				iSymReader.reOpenCachedSymbolFiles();
			}
		}

		public void windowClosed(IWorkbenchWindow window) {
			// do nothing

		}

		public void windowDeactivated(IWorkbenchWindow window) {
			// the user might do a re-build from the command line
			if (iSymReader != null) {
				iSymReader.closeCachedSymbolFiles();
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
			// do nothing
		}
	}

	/**
	 * set a new project
	 * 
	 * @param aProject
	 */
	public void setProject(IProject aProject) {
		if (iCurrentProject != aProject) {
			iCurrentProject = aProject;
			iSymReader = new SymReader(aProject);
			iSymReader.loadProjectTargetsInfo();
			ResourceVisitor visitor = new ResourceVisitor(this);
			try {
				iCurrentProject.accept(visitor);
			} catch (CoreException ce) {
				// just log the exception
				Activator.getDefault().log(IStatus.ERROR, ce.getMessage(), ce);
			}
		}
	}

	/**
	 * Load all cpp files from the project. This is callback to
	 * ResourcceVisitor.
	 * 
	 * @param resource
	 */
	public final void loadFileInfo(IResource resource) {
		// get all the cpp file info which are belongs to current project
		String cppFileName = Util.getCPPFileNameAndPath(resource);

		// if cpp file found, save it
		if (cppFileName != null && !cppFileNames.contains(cppFileName)) {
			this.cppFileNames.add(cppFileName);
		}
	}

	/**
	 * Sets the threshold. Only memory operations of a size greater or equals /
	 * lower or equals the threshold will be drawn on the graph.
	 * 
	 * @param value
	 *            the threshold value in bytes
	 * @param above
	 *            true if filtering "above", false if "below" the threshold
	 */
	public void setThreshold(long value, boolean above) {
		if (value < 0) {
			throw new IllegalArgumentException(
					"The threshold cannot be less than 0");
		}
		threshold = value;
		aboveThreshold = above;
	}
}
