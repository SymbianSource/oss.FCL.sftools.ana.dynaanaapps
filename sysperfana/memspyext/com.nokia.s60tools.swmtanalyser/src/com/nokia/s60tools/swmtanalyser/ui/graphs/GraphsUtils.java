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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph.EventTypes;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Graph utilities common to all graph types.
 */
public class GraphsUtils {

	//
	// Constants
	// 

	/**
	 * Constant for no marker possibility.
	 */
	private static final int NO_MARKER = -1;

	/**
	 * Possible marker sizes from smallest to biggest value.
	 */
	private static final int[] MARKER_SIZES = { 4, 6, 8 };

	/**
	 * Default marker size.
	 */
	private static final int DEFAULT_MARKER = MARKER_SIZES[2];

	/**
	 * Array of event names used to map the event names into corresponding event
	 * enumerators.
	 * 
	 * @see com.nokia.s60tools.swmtanalyser.ui.graphs.GenericGraph.EventTypes
	 */
	private static final String[] EVENT_NAMES_ARR = { "Global data size",
			"Non heap chunk size", "Disk used", "Disk total", "No of Files",
			"Max size", "Heap size", "Heap allocated space", "Heap free space",
			"Heap allocated cell count", "Heap free cell count", "Free slack",
			"No of PS Handles", "RAM used", "RAM total", "System Data" };

	/**
	 * Percentage of byte count to be graphed that is added extra reserve in
	 * order to show markers appropriately.
	 */
	private static final int MARKER_MARGIN_PERCENTAGE = 5;

	//
	// Symbolic name constants for different byte units use in setting Y-axis
	// value level according given maximum byte value
	// 
	private static final int KILOBYTE = 1024;
	private static final int MEGABYTE = 1024 * 1024;

	//
	// Members
	// 
	private static String imageFilename;
	private static Composite parentComposite;

	/**
	 * Maps event names into corresponding enumerator type
	 * 
	 * @param event
	 *            event name
	 * @return enumerator constant corresponding to the given event
	 */
	public static EventTypes getMappedEvent(String eventName) {
		int index = Arrays.asList(EVENT_NAMES_ARR).indexOf(eventName);
		GenericGraph.EventTypes eventType = null;

		switch (index) {
		case 0:
			eventType = GenericGraph.EventTypes.GLOBAL_DATA_SIZE;
			break;
		case 1:
			eventType = GenericGraph.EventTypes.NON_HEAP_CHUNK_SIZE;
			break;
		case 2:
			eventType = GenericGraph.EventTypes.DISK_USED_SIZE;
			break;
		case 3:
			eventType = GenericGraph.EventTypes.DISK_TOTAL_SIZE;
			break;
		case 4:
			eventType = GenericGraph.EventTypes.NO_OF_FILES;
			break;
		case 5:
			eventType = GenericGraph.EventTypes.MAX_HEAP_SIZE;
			break;
		case 6:
			eventType = GenericGraph.EventTypes.HEAP_SIZE;
			break;
		case 7:
			eventType = GenericGraph.EventTypes.HEAP_ALLOC_SPACE;
			break;
		case 8:
			eventType = GenericGraph.EventTypes.HEAP_FREE_SPACE;
			break;
		case 9:
			eventType = GenericGraph.EventTypes.HEAP_ALLOC_CELL_COUNT;
			break;
		case 10:
			eventType = GenericGraph.EventTypes.HEAP_FREE_CELL_COUNT;
			break;
		case 11:
			eventType = GenericGraph.EventTypes.HEAP_FREE_SLACK;
			break;
		case 12:
			eventType = GenericGraph.EventTypes.NO_OF_PSHANDLES;
			break;
		case 13:
			eventType = GenericGraph.EventTypes.RAM_USED;
			break;
		case 14:
			eventType = GenericGraph.EventTypes.RAM_TOTAL;
			break;
		case 15:
			eventType = GenericGraph.EventTypes.SYSTEM_DATA;
			break;
		}

		return eventType;
	}

	/**
	 * Get next scale when zooming in or out
	 * @param scale
	 * @param bigger give <code>true</code> when zooming out and <code>false</code> when zooming in.
	 * @return next scale
	 */
	public static double nextScale(double scale, boolean bigger) {
		double logScale = Math.log10(scale);
		double floorLogScale = Math.floor(Math.log10(scale));
		double mostSignificantDigit = Math.rint(Math.pow(10,
				(logScale - floorLogScale)));
		double powerOfTen = Math.pow(10, floorLogScale);

		if (bigger) {
			if (mostSignificantDigit < 2) {
				mostSignificantDigit = 2;
			} else if (mostSignificantDigit < 5) {
				mostSignificantDigit = 5;
			} else {
				mostSignificantDigit = 10;
			}
		} else {
			if (mostSignificantDigit > 5) {
				mostSignificantDigit = 5;
			} else if (mostSignificantDigit > 2) {
				mostSignificantDigit = 2;
			} else if (mostSignificantDigit > 1) {
				mostSignificantDigit = 1;
			} else {
				mostSignificantDigit = 0.5;
			}
		}

		double result = mostSignificantDigit * powerOfTen;

		if (result < 0.1)
			result = 0.1;

		return result;
	}

	/**
	 * Save the given composite as an image to local file system.
	 * 
	 * @param parent
	 */
	public static void saveGraph(Composite parent) {
		parentComposite = parent;
		FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(),
				SWT.SAVE);
		dlg.setFilterExtensions(new String[] { "*.bmp", "*.png", "*.jpeg" });
		imageFilename = dlg.open();
		if (imageFilename == null)
			return;

		Runnable p = new Runnable() {
			public void run() {
				GC gc = new GC(parentComposite);
				Image image = new Image(Display.getCurrent(), parentComposite
						.getClientArea().width,
						parentComposite.getClientArea().height);
				parentComposite.setFocus();
				gc.copyArea(image, 0, 0);
				gc.dispose();
				ImageData data = image.getImageData();
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { data };
				if (imageFilename != null)
					loader.save(imageFilename, SWT.IMAGE_BMP);
				image.dispose();
			}
		};
		Display.getDefault().timerExec(500, p);
	}

	/**
	 * Generate random color.
	 * 
	 * @return a random color
	 */
	public static Color getRandomColor() {
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		return new Color(Display.getCurrent(), r, g, b);
	}

	/**
	 * Creates an image and writes the given text vertically on the image. This
	 * is used to represent the Y-axis names in the Analysis tab and Graphed
	 * events -graphs. Those graphs show double Y-axis and therefore layout
	 * differs from single Y-axis situation.
	 * 
	 * @param axisLabelName
	 *            name of the label
	 * @return vertical axis label image
	 */
	public static Image getDoubleYAxisVerticalLabel(String axisLabelName) {
		return getVerticalLabel(axisLabelName, 90, 18, 10);
	}

	/**
	 * Creates an image and writes the given text vertically on the image with
	 * given coordinates and font size.
	 * 
	 * @param axisLabelName
	 *            name of the label
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @param fontSize
	 *            font size
	 * @return vertical axis label image
	 */
	public static Image getVerticalLabel(String axisLabelName, int x, int y,
			int fontSize) {
		final Image image = new Image(Display.getDefault(), x, y);
		GC gc = new GC(image);
		Font font = new Font(Display.getDefault(), Display.getDefault()
				.getSystemFont().getFontData()[0].getName(), fontSize, SWT.BOLD);
		gc.setFont(font);
		gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, 90, 15);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		gc.drawText(axisLabelName + " ->", 0, 0, true);
		font.dispose();
		gc.dispose();
		return image;
	}

	/**
	 * Draws markers to data points with current foreground color and marker
	 * size.
	 * 
	 * @param graphics
	 *            Graphics context
	 * @param points
	 *            Data point array in format [ X0, Y0, X1, Y1, ... ]
	 */
	private static void drawMarkers(Graphics graphics, int[] points,
			int markerSize) {
		Color backgroundColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(graphics.getForegroundColor());
		for (int j = 0; j < points.length; j += 2) {
			int width = markerSize;
			int height = width;
			int x = points[j] - (width / 2);
			int y = points[j + 1] - (width / 2);
			graphics.fillRectangle(x, y, width, height);
		}
		graphics.setBackgroundColor(backgroundColor);
	}

	/**
	 * Resolving minimum space between subsequent X-coordinate data points. This
	 * method expects that there are at least 2 x,y pairs in the array
	 * 
	 * @param points
	 *            Data point array in format [ X0, Y0, X1, Y1, ... ]
	 * @return minimum space between subsequent X-coordinate data points
	 */
	private static int resolveMinumumXDelta(int[] points) {
		int minDelta = points[2] - points[0];
		for (int i = 4; i < points.length; i += 2) {
			int delta = points[i] - points[i - 2];
			if (delta < minDelta) {
				minDelta = delta;
			}
		}
		return minDelta;
	}

	/**
	 * Draws markers to data points with current foreground color. Markers drawn
	 * only if there is enough room in X-axis to draw them between all
	 * individual data points.
	 * 
	 * @param graphics
	 *            Graphics context
	 * @param points
	 *            Data point array in format [ X0, Y0, X1, Y1, ... ]
	 */
	public static void drawMarkers(Graphics graphics, int[] points) {
		int markerSize = NO_MARKER; // By default not drawing markers if there
									// is no room for them
		// Checking deltas only if there is more than single point to draw
		if (points.length > 2) {
			// Resolving minimum space between subsequent X-coordinate data
			// points
			int minDelta = resolveMinumumXDelta(points);
			// Resolving if there is at all need to draw markers
			for (int i = MARKER_SIZES.length - 1; i >= 0; i--) {
				int size = MARKER_SIZES[i];
				if (size < minDelta) {
					markerSize = size; // Using this size in drawing
					break;
				}
			}
			//			DbgUtility.println(DbgUtility.PRIORITY_LOOP, "minDelta: " + minDelta); //$NON-NLS-1$
		} else {
			// Using default marker in case there
			markerSize = DEFAULT_MARKER;
		}
		//		DbgUtility.println(DbgUtility.PRIORITY_LOOP, "markerSize: " + markerSize); //$NON-NLS-1$
		// Drawing markers
		if (markerSize != NO_MARKER) {
			drawMarkers(graphics, points, markerSize);
		}
	}

	/**
	 * Gets nearest Y-legend bytes label from the given bytes
	 * 
	 * @param bytes
	 *            bytes number
	 * @return nearest Y-legend bytes label from the given bytes
	 */
	static public int prettyMaxBytes(int bytes) {

		// Adding some margin that makes possible to show also markers
		int byteMarginForMarkers = (int) Math
				.ceil((MARKER_MARGIN_PERCENTAGE / 100.0) * bytes);
		bytes = bytes + byteMarginForMarkers;

		//		DbgUtility.println(DbgUtility.PRIORITY_LOOP, "prettyMaxBytes/bytes: " + bytes); //$NON-NLS-1$
		//		DbgUtility.println(DbgUtility.PRIORITY_LOOP, "byteMarginForMarkers: " + byteMarginForMarkers); //$NON-NLS-1$

		// Before 10 KB limit using byte units are used Y-axis legend and
		// therefore thousand is used as checkpoint limit instead if KILOBYTE
		final int thousand = 1000;

		if (bytes < thousand)
			bytes = thousand;
		else if (bytes < 10 * thousand)
			bytes = 10 * thousand;
		else if (bytes < 20 * KILOBYTE)
			bytes = 20 * KILOBYTE;
		else if (bytes < 30 * KILOBYTE)
			bytes = 30 * KILOBYTE;
		else if (bytes < 50 * KILOBYTE)
			bytes = 50 * KILOBYTE;
		else if (bytes < 100 * KILOBYTE)
			bytes = 100 * KILOBYTE;
		else if (bytes < 150 * KILOBYTE)
			bytes = 150 * KILOBYTE;
		else if (bytes < 200 * KILOBYTE)
			bytes = 200 * KILOBYTE;
		else if (bytes < 300 * KILOBYTE)
			bytes = 300 * KILOBYTE;
		else if (bytes < 400 * KILOBYTE)
			bytes = 400 * KILOBYTE;
		else if (bytes < 500 * KILOBYTE)
			bytes = 500 * KILOBYTE;
		else if (bytes < 600 * KILOBYTE)
			bytes = 600 * KILOBYTE;
		else if (bytes < 700 * KILOBYTE)
			bytes = 700 * KILOBYTE;
		else if (bytes < 800 * KILOBYTE)
			bytes = 800 * KILOBYTE;
		else if (bytes < 900 * KILOBYTE)
			bytes = 900 * KILOBYTE;
		else if (bytes < 1000 * KILOBYTE)
			bytes = 1000 * KILOBYTE;
		else if (bytes < 1 * MEGABYTE)
			bytes = 1 * MEGABYTE;
		else if (bytes < 2 * MEGABYTE)
			bytes = 2 * MEGABYTE;
		else if (bytes < 3 * MEGABYTE)
			bytes = 3 * MEGABYTE;
		else if (bytes < 5 * MEGABYTE)
			bytes = 5 * MEGABYTE;
		else if (bytes < 10 * MEGABYTE)
			bytes = 10 * MEGABYTE;
		else if (bytes < 20 * MEGABYTE)
			bytes = 20 * MEGABYTE;
		else if (bytes < 30 * MEGABYTE)
			bytes = 30 * MEGABYTE;
		else if (bytes < 50 * MEGABYTE)
			bytes = 50 * MEGABYTE;
		else if (bytes < 100 * MEGABYTE)
			bytes = 100 * MEGABYTE;
		else if (bytes < 200 * MEGABYTE)
			bytes = 200 * MEGABYTE;
		else if (bytes < 300 * MEGABYTE)
			bytes = 300 * MEGABYTE;
		else if (bytes < 500 * MEGABYTE)
			bytes = 500 * MEGABYTE;
		else
			bytes = ((bytes + 1024 * MEGABYTE - 1) / (1024 * MEGABYTE))
					* (1024 * MEGABYTE);

		return bytes;
	}

	/**
	 * Converts list of <code>Point</code> objects into integer array.
	 * 
	 * @param pointsList
	 *            list of point objects
	 * @return points converted in 1-dimensional integer array.
	 */
	public static int[] convertPointListToIntArray(List<Point> pointsList) {
		int[] integerArray = new int[pointsList.size() * 2];
		for (int i = 0, j = 0; i < pointsList.size(); i++, j += 2) {
			Point pnt = pointsList.get(i);
			integerArray[j] = pnt.x;
			integerArray[(j + 1)] = pnt.y;
		}
		return integerArray;
	}

	/**
	 * Gets nearest Y-legend count value label from the given input count value.
	 * Maximum count value handled is 999*100 i.e. 99900 counts.
	 * 
	 * @param inputCountValue
	 *            bytes input count value
	 * @return nearest Y-legend count value label from the given input count
	 *         value.
	 * @return
	 */
	static public int roundToNearestNumber(int inputCountValue) {
		int tempCount = inputCountValue;

		// Adding some safe margin for making sure that all data points with
		// markers are drawn appropriately
		int countMarginForMarkers = (int) Math
				.ceil((MARKER_MARGIN_PERCENTAGE / 100.0) * tempCount);
		tempCount = tempCount + countMarginForMarkers;

		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"roundToNearestNumber/count: " + tempCount); //$NON-NLS-1$
		DbgUtility.println(DbgUtility.PRIORITY_OPERATION,
				"countMarginForMarkers: " + countMarginForMarkers); //$NON-NLS-1$

		if (tempCount < 10)
			tempCount = 10;
		else if (tempCount < 50)
			tempCount = 50;
		else if (tempCount < 100)
			tempCount = 100;
		else {
			for (int i = 2; i < 1000; i++) {
				if (tempCount < (i * 100)) {
					tempCount = i * 100;
					break;
				}
			}
		}

		return tempCount;
	}

	/**
	 * Builds int array from Integer List object
	 * 
	 * @param solidsList
	 *            Integer list
	 * @return int array
	 */
	public static int[] CreateIntArrayFromIntegerList(List<Integer> solidsList) {
		int[] solidPts = new int[solidsList.size()];
		for (int j = 0; j < solidsList.size(); j++) {
			solidPts[j] = solidsList.get(j);
		}
		return solidPts;
	}

}
