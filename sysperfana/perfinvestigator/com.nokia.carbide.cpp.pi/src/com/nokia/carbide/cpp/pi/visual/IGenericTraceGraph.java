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
package com.nokia.carbide.cpp.pi.visual;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel;
import com.nokia.carbide.cpp.internal.pi.visual.PIVisualSharedData;
/**
 * Interface for the generic trace graph
 *
 */
public interface IGenericTraceGraph {

	/** amount of space at left of graph to contain y-axis units */
	public static final int Y_LEGEND_WIDTH = 50;

	public GenericTrace getTrace();

	public void setTimeOffset(double offset);

	public double getTimeOffset();

	public void addSubGraphComponent(GenericTraceGraph subGraph);

	public Enumeration getGraphSubComponents();

	public void importParentComponent(PICompositePanel parent);

	public void setCurrentInfoComponent(Component infoComponent);

	public PIVisualSharedData getSharedDataInstance();

	/** a rectangle that describes visible area fo the draw 2D graphics so we paint only that*/
	public Rectangle getVisibleArea(Graphics graphics);

	public void paint(Panel panel, Graphics graphics);

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc);

	/** call all subcomponents' repaint methods */
	public void repaint();

	public void action(String action);

	public void setSize(int x, int y);

	public void setPITimeScale(float scale);

	public void setTimescalingEnabled(boolean flag);

	public float getPITimeScale();

	public Dimension getSize();

	public void setVisualSize(int x, int y);

	public Dimension getVisualSize();

	public Dimension getPreferredSize();

	public void setSelectionStart(double start);

	public void setSelectionEnd(double end);

	public void setHighResolution(boolean flag);

	public double getSelectionStart();

	public double getSelectionEnd();

	public void setScale(double scaleX, double scaleY);

	public double getScale();

	public void setScale(double scale);

	public void setToolTipText(String text);

	public PICompositePanel getCompositePanel();

	/** new generic method to draw background */
	public void drawDottedLineBackground(Graphics graphics,
			int yLegendSpace);

	public void drawSelectionSection(Graphics graphics,
			int yLegendSpace);

	public void updateVisibleBorders();

	public void genericRefreshCumulativeThreadTable();

	/**
	 * Draws the graph image. 
	 * @param profiledGenerics Collection of sorted and enabled ProfiledGenerics (threads, binaries, or functions) to draw 
	 * @param graphics Graphics context
	 * @param selection 
	 */
	public void drawGraphsGeneric(
			Vector<ProfiledGeneric> profiledGenerics, Graphics graphics,
			Object[] selection);

	public boolean getGraphImageChanged();

	public void setGraphImageChanged(boolean status);

	public void updateIfNeeded(Vector<ProfiledGeneric> profiledGenerics);

	public int getVisibleRightBorder();

	public int getVisibleLeftBorder();

	public int getVisualSizeX();

	public int getVisualSizeY();
	
	/**
	 * @return the fillFlag
	 */
	public boolean isFillFlag();

	/**
	 * @param fillFlag the fillFlag to set
	 */
	public void setFillFlag(boolean fillFlag);

	/**
	 * @return the fillSelected
	 */
	public boolean isFillSelected();

	/**
	 * @param fillSelected the fillSelected to set
	 */
	public void setFillSelected(boolean fillSelected);
	
	/**
	 * Returns the index of this graph page, i.e.
	 * <br>0 for Thread
	 * <br>1 for Binaries
	 * <br>2 for Functions
	 * <br>3 for SMP
	 * @return the graph's page index
	 */
	public int getGraphIndex();	

	/**
	 * Hides or reveals the graph and it's legend view on the page
	 * @param show true to reveal, false to hide
	 */
	public void setVisible(boolean show);
	
	/**
	 * Adds the IVisibilityListener to the graph
	 * @param listener the IVisibilityListener to set
	 */
	public void setVisibilityListener(IGraphChangeListener listener);

	/**
	 * Function that is called each time graph is minimized or restored in the
	 * PI editor window. 
	 * Plug-ins that have for example legend table need to
	 * implement this and hide/restore that table in this function.
	 * 
	 * @param value true when graph is restored
	 */
	public void graphVisibilityChanged(boolean value);

	/**
	 * Function that is called each time graph is maximized or restored in the
	 * PI editor window.
	 * Plug-ins that have for example legend table need to
	 * implement this and maximize/restore that table in this function.
	 * 
	 * @param value true when graph is restored
	 */
	public void graphMaximized(boolean value);

	/**
	 * @return true if graph needs to be minimized when PI window is opened.
	 */
	public boolean isGraphMinimizedWhenOpened();
	
	/**
	 * Returns the full title of this graph. When returning null,
	 * no title bar will be created. 
	 * @return graph title, or null
	 */
	public String getTitle();
	
	/**
	 * Returns a short title of the graph. This may, for example,
	 * be used on graph tabs
	 * @return short graph title
	 */
	public String getShortTitle();
	
	/**
	 * updates selecion area of the graph if it has event listener
	 * @param start new start time
	 * @param end new end time
	 */
	public void updateSelectionArea(double start, double end);
	
}