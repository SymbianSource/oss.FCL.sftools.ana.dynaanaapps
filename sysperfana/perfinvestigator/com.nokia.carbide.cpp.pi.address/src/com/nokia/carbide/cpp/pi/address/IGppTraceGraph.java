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
package com.nokia.carbide.cpp.pi.address;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.widgets.Sash;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

/**
 * Interface for GppTraceGraph functionality
 *
 */
public interface IGppTraceGraph extends IGenericTraceGraph, PIEventListener{
	/** Constant for Change-Threshold-Thread action */
	public static final String ACTION_CHANGE_THRESHOLD_THREAD = "changeThresholdThread";//$NON-NLS-1$ 
	/**
	 * Executes action according to given request. Valid requests include
	 * <br>resetToCurrentMode
	 * <br>add
	 * <br>remove
	 * <br>addall
	 * <br>removeall
	 * <br>recolor
	 * <br>copy
	 * <br>copyTable
	 * <br>copyDrilldown
	 * <br>saveTable
	 * <br>saveDrilldown
	 * <br>saveSamples
	 * <br>savePrioritySamples
	 * <br>selectAll
	 * <br>doubleClick
	 * <br>changeThresholdThread
	 * <br>saveTableTest
	 * <br>saveDrilldownTest
	 * <br>thread-only
	 * <br>thread-binary
	 * <br>thread-binary-function
	 * <br>thread-function
	 * <br>thread-function-binary
	 * <br>binary-only
	 * <br>...several more see {@link AddrThreadTable.action()}
	 * @param actionString the action to perform.
	 */
	public void action(String actionString);

	/**
	 * Draws the bar graph. Called on a paint request. 
	 * @param profiledGenerics Collection of affected ProfiledGeneric
	 * @param graphics the Graphics to use
	 * @param selection - Not used in code?
	 */
	public void drawBarsGpp(Vector<ProfiledGeneric> profiledGenerics, Graphics graphics, Object[] selection);
	
	/**
	 * Returns the Binaries address table, which manages the legend for Binaries
	 * @return AddrBinaryTable
	 */
	public AddrBinaryTable getBinaryTable();
	
	/**
	 * returns the currently active drawMode as defined in {@link Defines}
	 * @return int value of the draw mode
	 */
	public int getDrawMode();	
	
	/**
	 * Returns the Functions address table, which manages the legend for Functions
	 * @return AddrFunctionTable
	 */
	public AddrFunctionTable getFunctionTable();	
	
	/**
	 * Getter for the GppTrace model 
	 * @return GppTrace 
	 */
	public GppTrace getGppTrace();
		
	/**
	 * Returns the left sash of the graph. This is used when switching drawing modes.
	 * @return the left Sash
	 */
	public Sash getLeftSash();
	
	/**
	 * Setter for leftShash. This is used when switching drawing modes.
	 * @param leftSash
	 */
	public void setLeftSash(Sash leftSash);
	
	/**
	 * Setter for right sash of the graph.
	 * @param rightSash The Sash to set
	 */
	public void setRightSash(Sash rightSash);
	
	/**
	 * Getter for rightSash. This is used when switching drawing modes.
	 * @return rightSash
	 */
	public Sash getRightSash();	
	/**
	 * Getter for profiledBinaries
	 * @return profiledBinaries
	 */
	public Vector<ProfiledGeneric> getProfiledBinaries();	
	
	/**
	 * Getter for profiledFunctions
	 * @return profiledFunctions
	 */
	public Vector<ProfiledGeneric> getProfiledFunctions();
	
	/**
	 * Getter for sortedBinaries
	 * @return sortedBinaries
	 */
	public Vector<ProfiledGeneric> getSortedBinaries();
	
	/**
	 * Getter for profiledThread
	 * @return profiledThread
	 */
	public Vector<ProfiledGeneric> getProfiledThreads();
	
	/**
	 * Getter for sortedFunctions
	 * @return sortedFunctions
	 */
	public Vector<ProfiledGeneric> getSortedFunctions();
	
	/**
	 * Getter for sortedThreads
	 * @return sortedThreads
	 */
	public Vector<ProfiledGeneric> getSortedThreads();	
	
	/**
	 * Getter for thresholdThread
	 * @return thresholdThread
	 */
	public ProfiledThreshold getThresholdThread();
	
	/**
	 * Getter for thresholdBinary
	 * @return thresholdBinary
	 */
	public ProfiledThreshold getThresholdBinary();
	
	/**
	 * Getter for thresholdFunction
	 * @return thresholdFunction
	 */
	public ProfiledThreshold getThresholdFunction();
	
	/**
	 * @return the correct GenericTable for the currently active drawing mode
	 */
	public GenericTable getTableUtils();
	
	/**
	 * Getter for threadsTable
	 * @return AddrThreadTable
	 */
	public AddrThreadTable  getThreadTable();
	
	/**
	 * Returns the UId that this graph page was created with
	 * @return the uid
	 */
	public int getUid();

	/**
	 * Getter for graph page's GppVisualiserPanel
	 * @return GppVisualiserPanel
	 */
	public GppVisualiserPanel getVisualiserPanel();

	/**
	 * Changes the draw mode
	 * @param drawMode the new draw mode to set
	 */
    public void setDrawMode(int drawMode);

    /**
	 * Setter for collection of profiledBinaries this graph page uses
     * @param profiledBinaries
     */
    public void setProfiledBinaries(Vector<ProfiledGeneric> profiledBinaries);
    /**
	 * Setter for collection of profiledFunctions this graph page uses
     * @param profiledFunctions
     */
	public void setProfiledFunctions(Vector<ProfiledGeneric> profiledFunctions);
    /**
	 * Setter for collection of profiledThreads this graph page uses
     * @param profiledThreads
     */
	public void setProfiledThreads(Vector<ProfiledGeneric> profiledThreads);
	
	/**
	 * Update the thread table with thread priorities. 
	 * Called after processing the thread priority trace.
	 * @param priorities the priorities data to use
	 */
	public void updateThreadTablePriorities(Hashtable<Integer,String> priorities);

	/**
	 * Reload table colours from Profiled objects.
	 * 
	 */
	public void refreshColoursFromTrace();
	
}
