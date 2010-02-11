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

/*
 * GppTraceGraph.java
 */
package com.nokia.carbide.cpp.pi.address;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.internal.pi.visual.PIVisualSharedData;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;


public class GppTraceGraph extends GenericTraceGraph implements ActionListener, 
																FocusListener, 
																PIEventListener,
																MouseMotionListener,
																MouseListener,
																MouseMoveListener
{
	private GppTraceGraph thisTraceGraph;
	
	// amount of space at bottom of graph to contain things like x-axis units and button icons
	public static final int xLegendHeight = 50;
	
	// When the graph is drawn, this is the finest granularity drawn. E.g., assume
	// a granularity of 100, and a sample every ms. If the 1st function draw appears
	// in 5 samples from 101ms to 200ms and 10 samples from 200ms to 300ms then the
	// graph will show a point at height 5 at time 200 connected by a line to a
	// point at height 10 at time 300.
	private static int granularityValue = 100;
	
	private GppVisualiserPanel vPanel;

	/*
	 *  Depending on this trace graph's graphIndex, one of these will be
	 *  the base table, and others will be derived from its selections.
	 *  The other tables will appear and disappear based on drawMode.
	 *  
	 *  E.g., say that for graphIndex = 0, the base table is threadTable.
	 *  Then this graph can represent the draw modes of:
	 *  	THREADS
	 *  	THREADS_BINARIES
	 *  	THREADS_BINARIES_FUNCTIONS
	 *  	THREADS_FUNCTIONS
	 *  	THREADS_FUNCTIONS_BINARIES
	 */
	private AddrThreadTable   threadTable;
	private AddrBinaryTable   binaryTable;
	private AddrFunctionTable functionTable;

	/*
	 *	Depending on this trace graph's graphIndex, one of these will match
	 *	the entire trace's profiled vector, while the others will be derived
	 *	from drilldown selections. The other vectors will only be meaningful
	 *  depending on the drawMode.
	 */
	private Vector<ProfiledGeneric> profiledThreads   = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> profiledBinaries  = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> profiledFunctions = new Vector<ProfiledGeneric>();
	
	private Vector<ProfiledGeneric> sortedProfiledThreads   = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> sortedProfiledBinaries  = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> sortedProfiledFunctions = new Vector<ProfiledGeneric>();
	
	private ProfiledThreshold thresholdThread   = new ProfiledThreshold("dummy[0]::dummy_0"); //$NON-NLS-1$
	private ProfiledThreshold thresholdBinary   = new ProfiledThreshold("\\dummy"); //$NON-NLS-1$
	private ProfiledThreshold thresholdFunction = new ProfiledThreshold("dummy::dummy()"); //$NON-NLS-1$

	// when multiple tables are visible, they are separated by sashes
	private Sash leftSash;
	private Sash rightSash;

	public static final int NOFILL       = 0;
	public static final int FILLSELECTED = 1;
	public static final int FILLALL      = 2;
	public static final int BAR_MODE_ON  = 3;
	public static final int BAR_MODE_OFF = 4;
		
	private int drawMode = Defines.THREADS;
	public int barMode   = BAR_MODE_OFF;
	
	private int uid;
	
	private static class BarGraphData
	{
		public int x;
		public Color color;
	}
	
	private Vector<BarGraphData> barGraphData;
	
	public GppTraceGraph(int graphIndex, GppTrace trace, int uid)
	{
		super((GenericSampledTrace)trace);
		
		int granularityValue = trace.samples.size() > GppTraceGraph.granularityValue ? GppTraceGraph.granularityValue : trace.samples.size();  
		
		this.thisTraceGraph = this;
		this.graphIndex     = graphIndex;
		
		// create the graph's 3 table objects - without any table items yet
		ProfileVisualiser pV = NpiInstanceRepository.getInstance().getProfilePage(uid, graphIndex);
		Composite holdTables = new Composite(pV.getBottomComposite(), SWT.NONE);
		holdTables.setLayout(new FormLayout());

		this.threadTable   = new AddrThreadTable(this, holdTables);
		this.binaryTable   = new AddrBinaryTable(this, holdTables);
		this.functionTable = new AddrFunctionTable(this, holdTables);
		
		this.uid = uid;

		Label graphTitle = pV.getTitle();
		Label graphTitle2 = pV.getTitle2();
		if 	(graphTitle2 != null)
			graphTitle2.setText(""); //$NON-NLS-1$

		// initialize the threshold counts
		int totalSampleCount = trace.getSampleAmount();
		NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread", new Integer(new Double(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadThread") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
		NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountBinary", new Integer(new Double(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadBinary") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
		NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountFunction", new Integer(new Double(totalSampleCount * (Double)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadFunction") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		// initialize the threshold items
		thresholdThread.setColor(trace.getThreadColorPalette().getColor(thresholdThread.getNameString()));
		thresholdThread.setActivityMarkCount((trace.samples.size() + granularityValue) / granularityValue + 1);
		for (int i = 0; i < trace.samples.size() + granularityValue; i += granularityValue)
		{
			thresholdThread.zeroActivityMarkValues(i * samplingInterval);
		}
		thresholdBinary.setColor(trace.getBinaryColorPalette().getColor(thresholdBinary.getNameString()));
		thresholdBinary.setActivityMarkCount((trace.samples.size() + granularityValue) / granularityValue + 1);
		for (int i = 0; i < trace.samples.size() + granularityValue; i += granularityValue)
		{
			thresholdBinary.zeroActivityMarkValues(i * samplingInterval);
		}
		thresholdFunction.setColor(trace.getFunctionColorPalette().getColor(thresholdFunction.getNameString()));
		thresholdFunction.setActivityMarkCount((trace.samples.size() + granularityValue) / granularityValue + 1);
		for (int i = 0; i < trace.samples.size() + granularityValue; i += granularityValue)
		{
			thresholdFunction.zeroActivityMarkValues(i * samplingInterval);
		}

		// all the trace data is known, and you have it sorted 3 different ways
		// so set the drawmode, create the page's base table, and set the graph title
		if (graphIndex == PIPageEditor.THREADS_PAGE) {
			this.drawMode = Defines.THREADS;

			// tables will show threads in decreasing total sample order, but
			// then put them back in increasing sample order so that the graph
			// shows most common (e.g., EKern) threads on top
			this.profiledThreads.clear();
			for (int i = trace.getSortedThreads().size() - 1; i >= 0; i--)
				this.profiledThreads.add(trace.getSortedThreads().get(i));
			filterSortedByThreshold(graphIndex, 
									(Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread"), //$NON-NLS-1$
									thresholdThread, this.profiledThreads);
			this.threadTable.setTableViewer(Defines.THREADS);

			this.profiledThreads = trace.getSortedThreads();
			graphTitle.setText(Messages.getString("GppTraceGraph.threadLoad"));  //$NON-NLS-1$
		} else
		if (graphIndex == PIPageEditor.BINARIES_PAGE) {
			this.drawMode = Defines.BINARIES;

			// tables will show binaries in decreasing total sample order, but
			// then put them back in increasing sample order so that the graph
			// shows most common (e.g., EKern) binaries on top
			this.profiledBinaries.clear();
			for (int i = trace.getSortedBinaries().size() - 1; i >= 0; i--)
				this.profiledBinaries.add(trace.getSortedBinaries().get(i));
			filterSortedByThreshold(graphIndex, (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountBinary"), //$NON-NLS-1$
								thresholdBinary, this.profiledBinaries);
			this.binaryTable.setTableViewer(Defines.BINARIES);

			this.profiledBinaries = trace.getSortedBinaries();
			graphTitle.setText(Messages.getString("GppTraceGraph.binaryLoad"));  //$NON-NLS-1$
		} else
		if (graphIndex == PIPageEditor.FUNCTIONS_PAGE) {
			this.drawMode = Defines.FUNCTIONS;

			// tables will show functions in decreasing total sample order, but
			// then put them back in increasing sample order so that the graph
			// shows most common (e.g., EKern) functions on top
			this.profiledFunctions.clear();
			for (int i = trace.getSortedFunctions().size() - 1; i >= 0; i--)
				this.profiledFunctions.add(trace.getSortedFunctions().get(i));
			filterSortedByThreshold(graphIndex, (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountFunction"), //$NON-NLS-1$
								thresholdFunction, this.profiledFunctions);
			this.functionTable.setTableViewer(Defines.FUNCTIONS);

			this.profiledFunctions = trace.getSortedFunctions();
			graphTitle.setText(Messages.getString("GppTraceGraph.FunctionLoad"));  //$NON-NLS-1$
		} else
		{
			try {
				throw new Exception(Messages.getString("GppTraceGraph.traceGraphInternalErrorIn"));  //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// since the trace can be shown any of 3 ways (by thread, by binary, or
		// by function), make sure that all 3 are ready for display
		// Sse sorted vector to be consistent with other call to genericRefreshCumulativeThreadTable()
		// fix issue with wrong color on newly opened npi file before a change in the table
		if (this.profiledThreads.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledThreads.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledThreads.elements());
		}
		if (this.profiledBinaries.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledBinaries.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledBinaries.elements());
		}
		if (this.profiledFunctions.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledFunctions.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledFunctions.elements());
		}
		
		if (   this.profiledThreads.size() <= 0
			&& this.profiledBinaries.size() <= 0
			&& this.profiledFunctions.size() <= 0) {
			try {
				throw new Exception(Messages.getString("GppTraceGraph.traceGraphInternalErrorAt"));  //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.vPanel = new GppVisualiserPanel(this);
	}
	
	/*
	 * Combine all vectors elements that are below the threshold into a single
	 * vector element. Assumes that the input vector is sorted by decreasing sample count.
	 */
	private void filterSortedByThreshold(int graphIndex, int thresholdCount, ProfiledThreshold pThreshold, Vector<ProfiledGeneric> pGenerics)
	{
		// count and remove items below the threshold 
		for (int i = pGenerics.size() - 1; i >= 0; i--) {
			if (pGenerics.elementAt(i).getTotalSampleCount() < thresholdCount) {
				pThreshold.addItem(graphIndex, pGenerics.elementAt(i), 0);
				pGenerics.removeElementAt(i);
			} else {
				break;
			}
		}
	}

	public GppTrace getGppTrace() {
		return (GppTrace)this.getTrace();
	}

	public int getGraphIndex()
	{
		return this.graphIndex;
	}

	public void piEventReceived(PIEvent be)
	{
		switch (be.getType())
		{
			// determine the threads that can be shown, and get rid of all drilldowns
			case PIEvent.THRESHOLD_THREAD_CHANGED:
				if (this.getGraphIndex() == PIPageEditor.THREADS_PAGE)
					this.getThreadTable().action("changeThresholdThread"); //$NON-NLS-1$
				switch (this.drawMode) {
				case Defines.THREADS:
				case Defines.THREADS_FUNCTIONS:
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.THREADS_BINARIES:
				case Defines.THREADS_BINARIES_FUNCTIONS:
				case Defines.BINARIES_THREADS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				case Defines.FUNCTIONS_BINARIES_THREADS:
					this.setGraphImageChanged(true);
				default:
					break;
			}
				break;
			case PIEvent.THRESHOLD_BINARY_CHANGED:
				if (this.getGraphIndex() == PIPageEditor.BINARIES_PAGE)
					this.getBinaryTable().action("changeThresholdBinary"); //$NON-NLS-1$
				switch (this.drawMode) {
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.THREADS_BINARIES:
				case Defines.THREADS_BINARIES_FUNCTIONS:
				case Defines.BINARIES:
				case Defines.BINARIES_THREADS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				case Defines.FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_BINARIES_THREADS:
					this.setGraphImageChanged(true);
				default:
					break;
			}
				break;
			case PIEvent.THRESHOLD_FUNCTION_CHANGED:
				if (this.getGraphIndex() == PIPageEditor.FUNCTIONS_PAGE)
					this.getFunctionTable().action("changeThresholdFunction"); //$NON-NLS-1$
				switch (this.drawMode) {
				case Defines.THREADS_FUNCTIONS:
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.THREADS_BINARIES_FUNCTIONS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				case Defines.FUNCTIONS:
				case Defines.FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				case Defines.FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_BINARIES_THREADS:
					this.setGraphImageChanged(true);
				default:
					break;
			}
				break;
			// when the selection area changes, change the percent loads
			// and the sample counts in all tables of this GPP graph
			case PIEvent.SELECTION_AREA_CHANGED:
				// this is the first GPP graph to be told of the selection area change,
				// so it gathers the overall trace information
				GppTrace trace = (GppTrace)this.getTrace();
				trace.setSelectedArea();
				
				// take care of the threshold members
				int sampleCount = 0;
				int thresholdCount;
				
				if (graphIndex == PIPageEditor.THREADS_PAGE) {
					thresholdCount = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
					if (thresholdCount > 0) {
						for (int i = 0; i < profiledThreads.size(); i++)
							if (profiledThreads.elementAt(i).getTotalSampleCount() < thresholdCount)
								sampleCount += profiledThreads.elementAt(i).getSampleCount(graphIndex);
						thresholdThread.setSampleCount(this.graphIndex, sampleCount);
					}
				} else if (graphIndex == PIPageEditor.BINARIES_PAGE) {
					thresholdCount = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$
					if (thresholdCount > 0) {
						for (int i = 0; i < profiledBinaries.size(); i++)
							if (profiledBinaries.elementAt(i).getTotalSampleCount() < thresholdCount)
								sampleCount += profiledBinaries.elementAt(i).getSampleCount(graphIndex);
						thresholdBinary.setSampleCount(this.graphIndex, sampleCount);
					}
				} else if (graphIndex == PIPageEditor.FUNCTIONS_PAGE) {
					thresholdCount = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
					if (thresholdCount > 0) {
						for (int i = 0; i < profiledFunctions.size(); i++)
							if (profiledFunctions.elementAt(i).getTotalSampleCount() < thresholdCount)
								sampleCount += profiledFunctions.elementAt(i).getSampleCount(graphIndex);
						thresholdFunction.setSampleCount(this.graphIndex, sampleCount);
					}
				}

				double startTime = PIPageEditor.currentPageEditor().getStartTime();
				double endTime   = PIPageEditor.currentPageEditor().getEndTime();
				
				// send this message to the 2 other GPP graphs
				PIEvent be2 = new PIEvent(be.getValueObject(),
						PIEvent.SELECTION_AREA_CHANGED2);
				
				// update the selection area shown
				for (int i = 0; i < 3; i++)
				{
					GppTraceGraph graph = trace.getGppGraph(i, getUid());

					if (graph != this) {
						graph.piEventReceived(be2);
						// once per graph, update the selection interval shown
						graph.getCompositePanel().getVisualiser().getTimeString().setText(ProfileVisualiser.getTimeInterval(startTime, endTime));
					}
					
					// change the graph's selected time interval
					graph.setSelectionStart((double) startTime * 1000);
					graph.setSelectionEnd((double) endTime * 1000);
					graph.parentComponent.setSelectionFields((int)(startTime * 1000), (int)(endTime * 1000));
				}

				this.parentComponent.getSashForm().redraw();
				be = be2;
				// FALL THROUGH
			case PIEvent.SELECTION_AREA_CHANGED2:
			{
				// this code lets each graph's base thread/binary/function table update the other tables
				switch (drawMode)
				{
					case Defines.THREADS:
					case Defines.THREADS_FUNCTIONS:
					case Defines.THREADS_FUNCTIONS_BINARIES:
					case Defines.THREADS_BINARIES:
					case Defines.THREADS_BINARIES_FUNCTIONS:
				    {
				        this.threadTable.piEventReceived(be);
				        break;
				    }
					case Defines.BINARIES:
					case Defines.BINARIES_THREADS:
					case Defines.BINARIES_THREADS_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS_THREADS:
					{
				        this.binaryTable.piEventReceived(be);
						break;
					}
					case Defines.FUNCTIONS:
					case Defines.FUNCTIONS_THREADS:
					case Defines.FUNCTIONS_THREADS_BINARIES:
					case Defines.FUNCTIONS_BINARIES:
					case Defines.FUNCTIONS_BINARIES_THREADS:
					{
				        this.functionTable.piEventReceived(be);
						break;
					}
				}

				this.vPanel.refreshCumulativeThreadTable();
				this.setGraphImageChanged(true);	// any selection change to drill down will change graph
				this.repaint();
				break;
			}
			
			// in the graph, show all values from the rightmost table
			case PIEvent.SET_FILL_ALL_THREADS:
				this.setGraphImageChanged(true);	// any selection change to drill down will change graph
				this.vPanel.piEventReceived(be);
				break;

			// in the graph, don't fill between the lines will the color of
			// the line above
			case PIEvent.SET_FILL_OFF:
				this.setGraphImageChanged(true);	// any selection change to drill down will change graph
				this.vPanel.piEventReceived(be);
				break;

			// in the graph, show bars
			case PIEvent.GPP_SET_BAR_GRAPH_ON:
				this.vPanel.piEventReceived(be);
				break;

			// in the graph, show polylines 
			case PIEvent.GPP_SET_BAR_GRAPH_OFF:
				this.vPanel.piEventReceived(be);
				break;

			// in the graph, show only the values from selected rows in the
			// rightmost table
			case PIEvent.SET_FILL_SELECTED_THREAD:
				this.setGraphImageChanged(true);	// any selection change to drill down will change graph
				this.vPanel.piEventReceived(be);
				break;

			// Redraw the graph because the thread table's selected values have changed.
			// The thread table is handled (setting of array of selected threads
			// and table redraw) by the table selection listener or mouse listener.
			case PIEvent.CHANGED_THREAD_TABLE:
				switch (drawMode)
				{
					case Defines.THREADS_BINARIES:
					case Defines.THREADS_BINARIES_FUNCTIONS:
					case Defines.THREADS_FUNCTIONS:
					case Defines.THREADS_FUNCTIONS_BINARIES:
					{
						this.threadTable.piEventReceived(be);
						break;
					}
					case Defines.BINARIES_THREADS_FUNCTIONS:
					{
						this.binaryTable.piEventReceived(be);
						break;
					}
					case Defines.FUNCTIONS_THREADS_BINARIES:
					{
						this.functionTable.piEventReceived(be);
						break;
					}
					case Defines.THREADS:
					case Defines.BINARIES:
					case Defines.BINARIES_THREADS:
					case Defines.BINARIES_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS_THREADS:
					case Defines.FUNCTIONS:
					case Defines.FUNCTIONS_THREADS:
					case Defines.FUNCTIONS_BINARIES:
					case Defines.FUNCTIONS_BINARIES_THREADS:
					default:
						break;
				}

				this.vPanel.refreshCumulativeThreadTable();
				this.repaint();
			    //if fill selected, the graph is drawn again
				this.vPanel.piEventReceived(be);
				break;

			// Redraw the graph because the binary table's selected values have changed.
			// The binary table is handled (setting of array of selected binaries
			// and table redraw) by the table selection listener or mouse listener.
			case PIEvent.CHANGED_BINARY_TABLE:
				switch(drawMode)
				{
					case Defines.BINARIES_THREADS:
					case Defines.BINARIES_THREADS_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS_THREADS:
					{
						this.binaryTable.piEventReceived(be);
						break;
					}
					case Defines.THREADS_BINARIES_FUNCTIONS:
					{
						this.threadTable.piEventReceived(be);
						break;
					}
					case Defines.FUNCTIONS_BINARIES_THREADS:
					{
						this.functionTable.piEventReceived(be);
						break;
					}
					case Defines.THREADS:
					case Defines.THREADS_BINARIES:
					case Defines.THREADS_FUNCTIONS:
					case Defines.THREADS_FUNCTIONS_BINARIES:
					case Defines.BINARIES:
					case Defines.FUNCTIONS:
					case Defines.FUNCTIONS_THREADS:
					case Defines.FUNCTIONS_THREADS_BINARIES:
					case Defines.FUNCTIONS_BINARIES:
					default:
						break;
				}

				this.vPanel.refreshCumulativeThreadTable();
				this.repaint();
			    //if fill selected, the graph is drawn again
				this.vPanel.piEventReceived(be);
				break;

			// Redraw the graph because the function table's selected values have changed.
			// The function table is handled (setting of array of selected functions
			// and table redraw) by the table selection listener or mouse listener.
			case PIEvent.CHANGED_FUNCTION_TABLE:
				switch(drawMode)
				{
					case Defines.FUNCTIONS_THREADS:
					case Defines.FUNCTIONS_THREADS_BINARIES:
					case Defines.FUNCTIONS_BINARIES:
					case Defines.FUNCTIONS_BINARIES_THREADS:
					{
						this.functionTable.piEventReceived(be);
						break;
					}
					case Defines.THREADS_FUNCTIONS_BINARIES:
					{
						this.threadTable.piEventReceived(be);
						break;
					}
					case Defines.BINARIES_FUNCTIONS_THREADS:
					{
						this.binaryTable.piEventReceived(be);
						break;
					}
					case Defines.THREADS:
					case Defines.THREADS_BINARIES:
					case Defines.THREADS_BINARIES_FUNCTIONS:
					case Defines.THREADS_FUNCTIONS:
					case Defines.BINARIES:
					case Defines.BINARIES_THREADS:
					case Defines.BINARIES_THREADS_FUNCTIONS:
					case Defines.BINARIES_FUNCTIONS:
					case Defines.FUNCTIONS:
					default:
						break;
				}

				this.vPanel.refreshCumulativeThreadTable();
				this.repaint();
				this.vPanel.piEventReceived(be);
				break;

			case PIEvent.MOUSE_PRESSED:
				switch (drawMode) {
					case Defines.THREADS:
					{
						break;
					}
					case Defines.BINARIES:
					{
						break;
					}
					case Defines.FUNCTIONS:
					{
						break;
					}
					default:
					{
						break;
					}
				}
			    this.parentComponent.setActive(this);
				break;
				
			default:
				break;
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
	}
	
	public void action(String actionString)
	{
	    System.out.println(Messages.getString("GppTraceGraph.actionString")+actionString);  //$NON-NLS-1$
	    
	    if (actionString.equals("resetToCurrentMode")) //$NON-NLS-1$
	    {
	        if (drawMode == Defines.THREADS_FUNCTIONS)
	            this.setDrawMode(Defines.THREADS);
	        else if (drawMode == Defines.BINARIES_FUNCTIONS)
	            this.setDrawMode(Defines.BINARIES);
	        else
	            System.out.println(Messages.getString("GppTraceGraph.drawMode") + drawMode); //should not print this ever  //$NON-NLS-1$
	    }
	    else
	    {
			switch (drawMode)
			{
				case Defines.THREADS:
				case Defines.THREADS_FUNCTIONS:
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.THREADS_BINARIES:
				case Defines.THREADS_BINARIES_FUNCTIONS:
			    {
					this.threadTable.action(actionString);
			        break;
			    }
				case Defines.BINARIES:
				case Defines.BINARIES_THREADS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				{
					this.binaryTable.action(actionString);
					break;
				}
				case Defines.FUNCTIONS:
				case Defines.FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				case Defines.FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_BINARIES_THREADS:
				{
					this.functionTable.action(actionString);
					break;
				}
			}
	    }
	}
	
	public void focusGained(FocusEvent fe)
	{
	}

	public void focusLost(FocusEvent fe)
	{
	}

	public void mouseDragged(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mouseHover(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
	}

	public void mouseReleased(MouseEvent me) {
	}

	public void mouseDoubleClicked(MouseEvent me)
	{
	    Object[] result = this.getProfiledGenericUnderMouse(me);
		if ((result == null)) 
		{
			return;
		}
		
		ProfiledGeneric pg = (ProfiledGeneric)result[0];
		GenericTable gtu = null;

		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
			    gtu = this.threadTable;
			    break;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
			    gtu = this.binaryTable;
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
			    gtu = this.functionTable;
			    break;
			}
			default:
				break;
		}

		if (pg != null)
		{
			if (gtu.getIndex(pg) == null)
				return;
			int[] index = new int[1];
			index[0] = gtu.getIndex(pg).intValue();
			gtu.setSelectedIndicesXOR(index);
		}
	}

	private Object[] getProfiledGenericUnderMouse(MouseEvent me)
	{		
		Object[] result = new Object[2];
		double x = me.x * this.getScale();
		double y = me.y;
		
		y = y * 100 / (this.getVisualSize().height - GppTraceGraph.xLegendHeight);
		y = 100 - y;
		if (y <= 0)
			return null;
		
		// mouse event may return out of range X, that may 
		// crash when we use it to index data array
		x = x >= 0 ? x : 0;
		
		if (x > PIPageEditor.currentPageEditor().getMaxEndTime() * 1000)
			return null;

		if (me.x >= (int)(this.getSize().width)) {
			x = (this.getSize().width - 1) * this.getScale();
		}
		
		GppTrace gppTrace = (GppTrace) (this.getTrace());

		Enumeration<ProfiledGeneric> enumer = null;
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
				enumer = gppTrace.getSortedThreadsElements();
			    break;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
				enumer = gppTrace.getSortedBinariesElements();
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
		        enumer = gppTrace.getSortedFunctionsElements();
			    break;
			}
			default:
				break;
		}

		if (enumer == null)
			return null;

		Vector<ProfiledGeneric> activeThreads = new Vector<ProfiledGeneric>();
		while(enumer.hasMoreElements())
		{
			ProfiledGeneric pg = (ProfiledGeneric)enumer.nextElement();
			if (pg.isEnabled(this.graphIndex))		
			{		
				activeThreads.add(pg);
			}
		}
		
		int cumPrev = 0;
		int cumNext = 0;
		
		int topPrev = 0;
		int topNext = 0;

		double cumDiff = 0;
		double topDiff = 0;
		
		ProfiledGeneric currentProfiled = null;
		
		enumer = activeThreads.elements();
		if (enumer.hasMoreElements())
		    currentProfiled = enumer.nextElement();
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		int granularityValue = gppTrace.samples.size() > GppTraceGraph.granularityValue ? GppTraceGraph.granularityValue : gppTrace.samples.size();  

		while (true)
		{
			if (currentProfiled != null)
			{
				int[] cum = currentProfiled.getCumulativeList(graphIndex);
				int[] val = currentProfiled.getActivityList();
				if (cum == null || val == null)
					return null;
				
				int current = ((int)(x / samplingInterval)) / granularityValue;
				
				if ((current >= cum.length) || (current + 1 >= cum.length)) 
					return null;
					
				cumPrev = cum[current];	
				cumNext = cum[current + 1];

				topPrev = val[current];
				topNext = val[current + 1];

				cumDiff = (cumNext - cumPrev) * ((double)((x / samplingInterval) % granularityValue) / (granularityValue * 1d));
				topDiff = (topNext - topPrev) * ((double)((x / samplingInterval) % granularityValue) / (granularityValue * 1d));
			}
			else
			{
				if (y >= cumPrev + topPrev + topDiff + cumDiff) 
				{
				    currentProfiled = null;
					break;
				}
				else
				{
					break;
				}
			}
			
			if (y >= cumPrev + cumDiff && y < cumPrev + topPrev + topDiff + cumDiff ) 
			{
				break;
			}
			else
			{
				if (enumer.hasMoreElements())
				{
				    currentProfiled = (ProfiledGeneric)enumer.nextElement();
				}
				else
				{
				    currentProfiled = null;
				}
			}
		}
		
		if (currentProfiled != null)
		{
			String loadString = "" + (topPrev + topDiff); //$NON-NLS-1$
			int index = loadString.indexOf('.');
			
			if (index > 0 && ((index + 2) < loadString.length()))
				loadString = loadString.substring(0, index + 2); 
			
			result[0] = currentProfiled;
			result[1] = loadString;				
		}
		else
		{
			String totalString = "" + (100 - (cumPrev + topPrev + topDiff + cumDiff)); //$NON-NLS-1$
			int index = totalString.indexOf('.');
			
			if (index > 0 && ((index + 2) < totalString.length()))
				totalString = totalString.substring(0, index + 2);

			result[0] = null;
			result[1] = totalString;
		}
		return result;
	}

	public void mouseMoved(MouseEvent me)
	{
		double x = me.x * this.getScale();
		double y = me.y;
		
		// mouse event may return out of range X, that may 
		// crash when we use it to index data array
		x = x >= 0 ? x : 0;
		
		if (   y >= this.getVisualSizeY() - GppTraceGraph.xLegendHeight
			|| x >= PIPageEditor.currentPageEditor().getMaxEndTime() * 1000)
		{
			this.setToolTipText(null);
			return;
		}

		if (   NpiInstanceRepository.getInstance() == null
			|| NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval") == null) //$NON-NLS-1$)
			return;
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		if (this.barMode == GppTraceGraph.BAR_MODE_ON) {
			GppSample samp = (GppSample)((GenericSampledTrace)this.getTrace()).getSample(((int)(x + .0005))/samplingInterval);
			switch (drawMode)
			{
				case Defines.THREADS:
				case Defines.BINARIES_THREADS:
				case Defines.FUNCTIONS_THREADS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_BINARIES_THREADS:
				{
					try {
						this.setToolTipText(samp.sampleSynchTime+"ms @"+  //$NON-NLS-1$
											Long.toHexString(samp.programCounter)+" "+  //$NON-NLS-1$
											samp.thread.process.name+"::"+  //$NON-NLS-1$
											samp.thread.threadName+"_"+  //$NON-NLS-1$
											samp.thread.threadId);}
					catch (NullPointerException e2)
					{
						this.setToolTipText(Messages.getString("GppTraceGraph.cannotResolveThreadName"));  //$NON-NLS-1$
					}
					break;
				}
				case Defines.BINARIES:
				case Defines.THREADS_BINARIES:
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				{
					try {
						if (samp.currentFunctionSym.functionBinary.binaryName.endsWith(Messages.getString("GppTraceGraph.NotFound")))  //$NON-NLS-1$
							throw new NullPointerException();
						this.setToolTipText(samp.sampleSynchTime+"ms @"+  //$NON-NLS-1$
											Long.toHexString(samp.programCounter)+" "+  //$NON-NLS-1$
											samp.currentFunctionSym.functionBinary.binaryName);
					} catch (NullPointerException e)
					{
						try {
							this.setToolTipText(samp.sampleSynchTime+"ms @"+  //$NON-NLS-1$
												Long.toHexString(samp.programCounter)+" "+  //$NON-NLS-1$
												samp.currentFunctionItt.functionBinary.binaryName);}
						catch (NullPointerException e2)
						{
							this.setToolTipText(Messages.getString("GppTraceGraph.cannotResolveBinaryName"));  //$NON-NLS-1$
						}
					}
					break;
				}
				case Defines.FUNCTIONS:
				case Defines.THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS:
				case Defines.THREADS_BINARIES_FUNCTIONS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				{
					try {
						if (samp.currentFunctionSym.functionBinary.binaryName.endsWith(Messages.getString("GppTraceGraph.notFound")))  //$NON-NLS-1$
							throw new NullPointerException();
						
						this.setToolTipText(samp.sampleSynchTime+"ms @"+  //$NON-NLS-1$
											Long.toHexString(samp.programCounter)+" "+  //$NON-NLS-1$
											samp.currentFunctionSym.functionName);}
					catch (NullPointerException e)
					{
						try{this.setToolTipText(samp.sampleSynchTime+"ms @"+  //$NON-NLS-1$
												Long.toHexString(samp.programCounter)+" "+  //$NON-NLS-1$
												samp.currentFunctionItt.functionName);}
						catch (NullPointerException e2)
						{
							this.setToolTipText(Messages.getString("GppTraceGraph.cannotResolveFunctionName"));  //$NON-NLS-1$
						}
					}
					break;
				}
				default:
					return;
			}

			return;  // return for barMode == GppTraceGraph.BAR_MODE_ON
		}

		// barMode == GppTraceGraph.BAR_MODE_OFF

    	Object[] result = this.getProfiledGenericUnderMouse(me);
		if (result == null)
		{
			this.setToolTipText(null);
			return;
		}
		
		if (me.x >= (int)(this.getSize().width)) {
			x = (this.getSize().width - 1) * this.getScale();
		}
		
		ProfiledGeneric pg = null;
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
			    pg = (ProfiledThread)result[0];
			    break;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
			    pg = (ProfiledBinary)result[0];
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
			    pg = (ProfiledFunction)result[0];
			    break;
			}
			default:
				break;
		}

		String string = (String)result[1];
		if (pg == null)
		{
			switch (drawMode)
			{
				case Defines.THREADS:
				case Defines.BINARIES_THREADS:
				case Defines.FUNCTIONS_THREADS:
				case Defines.BINARIES_FUNCTIONS_THREADS:
				case Defines.FUNCTIONS_BINARIES_THREADS:
				{
				    this.setToolTipText(string + "% " + Messages.getString("GppTraceGraph.unknownOrExcludedThreads"));  //$NON-NLS-1$ //$NON-NLS-2$
				    break;
				}
				case Defines.BINARIES:
				case Defines.THREADS_BINARIES:
				case Defines.THREADS_FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_BINARIES:
				case Defines.FUNCTIONS_THREADS_BINARIES:
				{
				    this.setToolTipText(string + "% " + Messages.getString("GppTraceGraph.unknownOrExcludedBinaries"));  //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
				case Defines.FUNCTIONS:
				case Defines.THREADS_FUNCTIONS:
				case Defines.BINARIES_FUNCTIONS:
				case Defines.THREADS_BINARIES_FUNCTIONS:
				case Defines.BINARIES_THREADS_FUNCTIONS:
				{
				    this.setToolTipText(string + "% " + Messages.getString("GppTraceGraph.unknownOrExcludedFunctions"));  //$NON-NLS-1$ //$NON-NLS-2$
				    break;
				}
				default:
					break;
			}
		}
		else
		{
			this.setToolTipText(string + "% " + pg.getNameString());  //$NON-NLS-1$
		}
	}
	
	public GenericTable getTableUtils()
	{
		switch (drawMode)
		{
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS:
			{
			    return this.threadTable;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			{
			    return this.binaryTable;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			{
			    return this.functionTable;
			}
			default:
				break;
		}

		System.out.println(Messages.getString("GppTraceGraph.debugDrawMode") + drawMode);  //$NON-NLS-1$
	    return null;
	}
	
	public GppVisualiserPanel getVisualiserPanel()
	{
		return this.vPanel;
	}
	
	public AddrThreadTable  getThreadTable() {
		return this.threadTable;
	}
	
	public AddrBinaryTable getBinaryTable() {
		return this.binaryTable;
	}
	
	public AddrFunctionTable getFunctionTable() {
		return this.functionTable;
	}

	public void setThreadTableViewer(CheckboxTableViewer tableViewer) {
		this.threadTable.setTableViewer(tableViewer);
	}
	
	public void setBinaryTableViewer(CheckboxTableViewer tableViewer) {
		this.binaryTable.setTableViewer(tableViewer);;
	}
	
	public void setFunctionTableViewer(CheckboxTableViewer tableViewer) {
		this.functionTable.setTableViewer(tableViewer);
	}

	public void paint(Panel panel, Graphics graphics)
	{
		this.setSize(panel.getClientArea().width, panel.getClientArea().height);
		this.vPanel.paintComponent(panel, graphics);
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc)
	{
		GC localGC = gc;
		
		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas.getClientArea();
		
		int visY = rect.height;
		
		float visYfloat = visY - GppTraceGraph.xLegendHeight;
		
		if (visYfloat < 0f)
			visYfloat = 0f;
		
		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));
		
		// write each next number if there is space
		// float values will be slightly smaller than the actual result
		// and they will be incremented by one, since rounding to int
		// discards the remaining decimals
		int percent = 100;
		int previousBottom = 0;		// bottom of the previous legend drawn
		for (float y = 0f; percent >= 0; y += visYfloat * 10000f / 100001f, percent -= 10)
		{
			String legend = "" + percent + "%"; //$NON-NLS-1$ //$NON-NLS-2$
			Point extent = gc.stringExtent(legend);
			
			gc.drawLine(GenericTraceGraph.yLegendWidth - 3, (int)y + 1, GenericTraceGraph.yLegendWidth, (int)y + 1);

			if ((int)y >= previousBottom)
			{
				gc.drawString(legend, GenericTraceGraph.yLegendWidth - extent.x - 4, (int)y);
				previousBottom = (int)y + extent.y;
			}
		}
		
		if (localGC == null) {
			gc.dispose();
			figureCanvas.redraw();
		}
	}
	
	public void repaint()
	{
		this.parentComponent.repaintComponent();
	}
	
	public void drawBarsGpp(Vector<ProfiledGeneric> profiledGenerics, Graphics graphics, Object[] selection)
	{
		if (   this.updateCumulativeThreadTableIsNeeded
			|| this.barGraphData == null)
		{
			this.updateBarGraphData(profiledGenerics);
		}
		
		this.updateIfNeeded(profiledGenerics);
		
		Enumeration<BarGraphData> barEnum = this.barGraphData.elements();

		int drawX = -1;
		int lastDrawX = -10;
		double scale = this.getScale();
		int y = this.getVisualSizeY() - 51;
		org.eclipse.draw2d.geometry.Rectangle visibleArea = this.getVisibleArea(graphics);

		while(barEnum.hasMoreElements())
		{
			BarGraphData bgd = barEnum.nextElement();
			drawX = (int)(((double)bgd.x) / scale);
			
			if (   drawX >= visibleArea.x
				&& drawX < visibleArea.x + visibleArea.width )
			{
				if (debug)
					System.out.println(Messages.getString("GppTraceGraph.draw") + drawX + " " + scale + " " + bgd.x);    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (drawX != lastDrawX)
				{
					graphics.setForegroundColor(bgd.color);
					graphics.drawLine(drawX, 0, drawX, y);
					lastDrawX = drawX;
				}
			}
		}
	}

	public void updateBarGraphData(Vector profiledGenerics)
	{
		if (this.barGraphData == null)
			this.barGraphData = new Vector<BarGraphData>();
		this.barGraphData.clear();

		int x = 0;
		
		// find the first enabled profiled generic
		int firstEnabled;
		for (firstEnabled = 0; firstEnabled < profiledGenerics.size(); firstEnabled++)
			if (((ProfiledGeneric)profiledGenerics.get(firstEnabled)).isEnabled(this.graphIndex))
				break;
		
		// return if there are no enabled profiled generics
		if (firstEnabled == profiledGenerics.size())
			return;
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		Enumeration samples = ((GenericSampledTrace)this.getTrace()).getSamples();
		while (samples.hasMoreElements())
		{
			GppSample gs = (GppSample)samples.nextElement();
			
			// for each of the tens of thousands of samples, loop through each of the
			// perhaps thousands of functions, hundreds of binaries, or tens of threads
			for (int i = firstEnabled; i < profiledGenerics.size(); i++)
			{
				// find the next enabled profiled generic, if any
				while (   (i < profiledGenerics.size()
					   && !((ProfiledGeneric)profiledGenerics.get(i)).isEnabled(this.graphIndex)))
					i++;
				if (i >= profiledGenerics.size())
					break;

				ProfiledGeneric pg = (ProfiledGeneric)profiledGenerics.get(i);

				if (   ((pg instanceof ProfiledThread)   && (pg.getIndex() == gs.threadIndex))
				    || ((pg instanceof ProfiledBinary)   && (pg.getIndex() == gs.binaryIndex))
				    || ((pg instanceof ProfiledFunction) && (pg.getIndex() == gs.functionIndex))) {
					BarGraphData bgd = new BarGraphData();
					bgd.color = pg.getColor();
					bgd.x = x;
					this.barGraphData.add(bgd);
					break;
				}
			}
			x += samplingInterval;
		}
	}	

	public void refreshDataFromTrace()
	{
		refreshDataFromTrace((GppTrace)this.getTrace());

	    if (this.vPanel != null)
		{
			this.vPanel.refreshCumulativeThreadTable();
		}
	}

	public static void refreshDataFromTrace(GppTrace gppTrace)
	{
		Enumeration enumer = gppTrace.getSamples();
		
		int granularityValue = gppTrace.samples.size() > GppTraceGraph.granularityValue ? GppTraceGraph.granularityValue : gppTrace.samples.size();  

		Hashtable<String,ProfiledGeneric> profiledThreads   = new Hashtable<String,ProfiledGeneric>();
		Hashtable<String,ProfiledGeneric> profiledBinaries  = new Hashtable<String,ProfiledGeneric>();
		Hashtable<String,ProfiledGeneric> profiledFunctions = new Hashtable<String,ProfiledGeneric>();
		
		Hashtable<ProfiledGeneric,Integer> threadPercentages   = new Hashtable<ProfiledGeneric,Integer>();
		Hashtable<ProfiledGeneric,Integer> binaryPercentages   = new Hashtable<ProfiledGeneric,Integer>();
		Hashtable<ProfiledGeneric,Integer> functionPercentages = new Hashtable<ProfiledGeneric,Integer>();
		
		int threadCount = 0;
		int binaryCount = 0;
		int functionCount = 0;
		int count = 0;
		int timeStamp = 0;
		int stepValue = granularityValue;
		char threadSymbol = 'A';
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		Vector<ProfiledGeneric> sortedProfiledThreads   = gppTrace.getSortedThreads();
		Vector<ProfiledGeneric> sortedProfiledBinaries  = gppTrace.getSortedBinaries();
		Vector<ProfiledGeneric> sortedProfiledFunctions = gppTrace.getSortedFunctions();

		Vector<ProfiledGeneric> unsortedProfiledThreads   = gppTrace.getIndexedThreads();
		Vector<ProfiledGeneric> unsortedProfiledBinaries  = gppTrace.getIndexedBinaries();
		Vector<ProfiledGeneric> unsortedProfiledFunctions = gppTrace.getIndexedFunctions();
		
		// reset these list so we can call refreshDataFromTrace mulitple times (e.g. import) while keeping size consistent
		sortedProfiledThreads.clear();
		sortedProfiledBinaries.clear();
		sortedProfiledFunctions.clear();

		unsortedProfiledThreads.clear();
		unsortedProfiledBinaries.clear();
		unsortedProfiledFunctions.clear();

		boolean exit = false;
		while (exit == false)
		{
			exit = !enumer.hasMoreElements();
			if (exit == true) 
			{
				// for the final samples, modify the step value
				// so that they will also be included
				// now there are no new samples, so proceed directly to
				// adding the final values to the percent list
				stepValue = count;
			}
			else
			{
				count++;
				
				// there is at least one new sample in the enumeration, so resolve it 
				GppSample sample = (GppSample)enumer.nextElement();
				String threadName   = sample.thread.process.name + "::" + sample.thread.threadName + "_" + sample.thread.threadId;   //$NON-NLS-1$ //$NON-NLS-2$
				String binaryName   = getBinaryName(sample);
				String functionName = getFunctionName(sample);

				ProfiledThread   pThread = null;
				ProfiledBinary   pBinary = null;
				ProfiledFunction pFunction = null;
				
				// handle new thread names
				if (profiledThreads.containsKey(threadName))
				{
					pThread = (ProfiledThread)profiledThreads.get(threadName);
					if (pThread.getThreadId() != sample.thread.threadId.intValue())
					{
						// this was not the same thread, even though the
						// name was the same
						pThread = null;
					}
				}
			
				if (pThread == null)
				{
					pThread = new ProfiledThread();
					
					pThread.setIndex(threadCount++);
					pThread.setNameValues(threadSymbol++, threadName);
					pThread.setColor(gppTrace.getThreadColorPalette().getColor(threadName));
					
					pThread.setThreadId(sample.thread.threadId.intValue());
					
					pThread.setActivityMarkCount((gppTrace.samples.size() + granularityValue) / granularityValue + 1);
					for (int i = 0; i < timeStamp + stepValue * samplingInterval; i += stepValue * samplingInterval)
					{
						pThread.zeroActivityMarkValues(i);
					}
					profiledThreads.put(threadName, pThread);
					sortedProfiledThreads.add((ProfiledGeneric)pThread);			
					unsortedProfiledThreads.add((ProfiledGeneric)pThread);			
				}
				
				pThread.incTotalSampleCount();
				sample.threadIndex = pThread.getIndex();

				if (threadPercentages.containsKey(pThread))
				{
					Integer value = (Integer)threadPercentages.get(pThread);
					value = new Integer(value.intValue()+1);
					threadPercentages.remove(pThread);
					threadPercentages.put(pThread, value);
				}
				else
				{
					threadPercentages.put(pThread, new Integer(1));
				}
				
				// handle new binary names
				if (profiledBinaries.containsKey(binaryName))
				{
					pBinary = (ProfiledBinary)profiledBinaries.get(binaryName);
				}
			
				if (pBinary == null)
				{
					pBinary = new ProfiledBinary();

					pBinary.setIndex(binaryCount++);
					pBinary.setNameString(binaryName);
					pBinary.setColor(gppTrace.getBinaryColorPalette().getColor(binaryName));
					
					pBinary.setActivityMarkCount((gppTrace.samples.size() + granularityValue) / granularityValue + 1);
					for (int i = 0; i < timeStamp + stepValue * samplingInterval; i += stepValue * samplingInterval)
					{
						pBinary.zeroActivityMarkValues(i);
					}
					profiledBinaries.put(binaryName,pBinary);
					sortedProfiledBinaries.add((ProfiledGeneric)pBinary);			
					unsortedProfiledBinaries.add((ProfiledGeneric)pBinary);			
				}
				
				pBinary.incTotalSampleCount();
				sample.binaryIndex = pBinary.getIndex();

				if (binaryPercentages.containsKey(pBinary))
				{
					Integer value = (Integer)binaryPercentages.get(pBinary);
					value = new Integer(value.intValue()+1);
					binaryPercentages.remove(pBinary);
					binaryPercentages.put(pBinary,value);
				}
				else
				{
					binaryPercentages.put(pBinary, new Integer(1));
				}														
				
				// handle new function names
				if (profiledFunctions.containsKey(functionName))
				{
					pFunction = (ProfiledFunction)profiledFunctions.get(functionName);
				}
			
				if (pFunction == null)
				{
					pFunction = new ProfiledFunction();

					pFunction.setIndex(functionCount++);
					pFunction.setNameString(functionName);
					pFunction.setFunctionAddress(getFunctionAddress(sample));
					pFunction.setFunctionBinaryName(binaryName);
					pFunction.setColor(gppTrace.getFunctionColorPalette().getColor(functionName));
					
					pFunction.setActivityMarkCount((gppTrace.samples.size() + granularityValue) / granularityValue + 1);
					for (int i = 0; i < timeStamp + stepValue * samplingInterval; i += stepValue * samplingInterval)
					{
						pFunction.zeroActivityMarkValues(i);
					}
					profiledFunctions.put(functionName,pFunction);
					sortedProfiledFunctions.add((ProfiledGeneric)pFunction);			
					unsortedProfiledFunctions.add((ProfiledGeneric)pFunction);			
				}

				pFunction.incTotalSampleCount();
				sample.functionIndex = pFunction.getIndex();

				if (functionPercentages.containsKey(pFunction))
				{
					Integer value = (Integer)functionPercentages.get(pFunction);
					value = new Integer(value.intValue()+1);
					functionPercentages.remove(pFunction);
					functionPercentages.put(pFunction,value);
				}
				else
				{
					functionPercentages.put(pFunction, new Integer(1));
				}														
			}
			
			// for each stepValue (or final values) samples
			// add the data to the profiled threads, binaries, functions
			if (stepValue != 0 && count == stepValue)
			{
				Vector<ProfiledGeneric> tmpVector;
				
				tmpVector = new Vector<ProfiledGeneric>(profiledThreads.values());
				Enumeration<ProfiledGeneric> ptEnum = tmpVector.elements();
				while (ptEnum.hasMoreElements())
				{
					ProfiledThread updatePt = (ProfiledThread)ptEnum.nextElement();
					if (threadPercentages.containsKey(updatePt))
					{
						int samples = ((Integer)(threadPercentages.get(updatePt))).intValue();
						int finalPerc = (samples * 100) / stepValue;
						updatePt.addActivityMarkValues(timeStamp + stepValue * samplingInterval, finalPerc, samples);
					}
					else
					{
						updatePt.zeroActivityMarkValues(timeStamp + stepValue * samplingInterval);
					}					
				}
				
				tmpVector = new Vector<ProfiledGeneric>(profiledBinaries.values());
				Enumeration<ProfiledGeneric> pbEnum = tmpVector.elements();
				while (pbEnum.hasMoreElements())
				{
					ProfiledBinary updatePb = (ProfiledBinary)pbEnum.nextElement();
					if (binaryPercentages.containsKey(updatePb))
					{
						int samples = ((Integer)(binaryPercentages.get(updatePb))).intValue();
						int finalPerc = (samples * 100) / stepValue;
						updatePb.addActivityMarkValues(timeStamp + stepValue * samplingInterval, finalPerc, samples);
					}
					else
					{
						updatePb.zeroActivityMarkValues(timeStamp + stepValue * samplingInterval);
					}					
				}
				
				tmpVector = new Vector<ProfiledGeneric>(profiledFunctions.values());
				Enumeration<ProfiledGeneric> pfEnum = tmpVector.elements();
				while (pfEnum.hasMoreElements())
				{
					ProfiledFunction updatePf = (ProfiledFunction)pfEnum.nextElement();
					if (functionPercentages.containsKey(updatePf))
					{
						int samples = ((Integer)(functionPercentages.get(updatePf))).intValue();
						int finalPerc = (samples * 100) / stepValue;
						updatePf.addActivityMarkValues(timeStamp + stepValue * samplingInterval, finalPerc, samples);
					}
					else
					{
						updatePf.zeroActivityMarkValues(timeStamp + stepValue * samplingInterval);
					}					
				}

				threadPercentages.clear();
				binaryPercentages.clear();
				functionPercentages.clear();
				count = 0;
				timeStamp += stepValue * samplingInterval;
			}
		}
		
		// if there is no end point for a profiled element with samples, set the end to the last sample in the trace 
		for (Enumeration<ProfiledGeneric> e = profiledThreads.elements(); e.hasMoreElements(); ) {
			ProfiledGeneric pg = e.nextElement();
			
			if (pg.getRealLastSample() == -1 && pg.getTotalSampleCount() != 0)
				pg.setLastSample(gppTrace.samples.size() * samplingInterval);
		}

		for (Enumeration<ProfiledGeneric> e = profiledBinaries.elements(); e.hasMoreElements(); ) {
			ProfiledGeneric pg = e.nextElement();
			
			if (pg.getRealLastSample() == -1 && pg.getTotalSampleCount() != 0)
				pg.setLastSample(gppTrace.samples.size() * samplingInterval);
		}

		for (Enumeration<ProfiledGeneric> e = profiledFunctions.elements(); e.hasMoreElements(); ) {
			ProfiledGeneric pg = e.nextElement();
			
			if (pg.getRealLastSample() == -1 && pg.getTotalSampleCount() != 0)
				pg.setLastSample(gppTrace.samples.size() * samplingInterval);
		}

		// sort the thread, binary, and function vectors by load
		sortProfiledGenerics(profiledThreads, gppTrace);
		sortProfiledGenerics(profiledBinaries, gppTrace);
		sortProfiledGenerics(profiledFunctions, gppTrace);
		
		// the trace-level count arrays are initialized to 0
		gppTrace.setThreadSampleCounts(new int[profiledThreads.size()]);
		gppTrace.setBinarySampleCounts(new int[profiledBinaries.size()]);
		gppTrace.setFunctionSampleCounts(new int[profiledFunctions.size()]);
	}
	
	/*
	 * Because a table to the left of a function table has changed, update
	 * the function table.
	 * If there is no table to the right of this one, redraw the graph based
	 * on the changed data.
	 */
	public void refreshProfiledThreadData(int drawMode)
	{
		// Must have a table to its left
	    if (   (drawMode != Defines.BINARIES_THREADS)
	    	&& (drawMode != Defines.BINARIES_THREADS_FUNCTIONS)
	    	&& (drawMode != Defines.BINARIES_FUNCTIONS_THREADS)
	    	&& (drawMode != Defines.FUNCTIONS_THREADS)
	    	&& (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
	    	&& (drawMode != Defines.FUNCTIONS_BINARIES_THREADS)
	    	)
	    {
	        System.out.println(Messages.getString("GppTraceGraph.wrongDrawMode"));  //$NON-NLS-1$
	        return;
	    }

	    // boolean to use inside loops (should trust a compiler to optimize this out of the loop...)
	    boolean basedOnBinaries = (drawMode == Defines.BINARIES_THREADS)
	    					   || (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)
	    					   || (drawMode == Defines.FUNCTIONS_BINARIES_THREADS); 

	    Hashtable<String,ProfiledThread> profiledThreads = new Hashtable<String,ProfiledThread>();
		
	    GenericSampledTrace trace = (GenericSampledTrace)this.getTrace();
		int granularityValue = trace.samples.size() > GppTraceGraph.granularityValue ? GppTraceGraph.granularityValue : trace.samples.size();  
		
	    String[] selectedItems;
		int[] selectedFunctionHashCodes = null;
		int[] selectedBinaryHashCodes   = null;
		int count = 0;
		int timeStamp = 0;
		int stepValue = granularityValue;
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		boolean exit = false;
		
		Hashtable<ProfiledThread,Integer> percentages = new Hashtable<ProfiledThread,Integer>();
		PIVisualSharedData shared = this.getSharedDataInstance();
		
	    if (basedOnBinaries)
		{
		    selectedItems = shared.GPP_SelectedBinaryNames;
		    if (selectedItems == null) 
	        {
		        selectedItems = new String[0];
	        }
		    int[] tmpHashCodes = new int[selectedItems.length];
		    for (int i = 0; i < selectedItems.length; i++)
		    {
		        String tmp = selectedItems[i];
		        tmpHashCodes[i] = tmp.hashCode();
		    }
		    selectedBinaryHashCodes = tmpHashCodes;
		}
		else
		{
		    selectedItems = shared.GPP_SelectedFunctionNames;
		    if (selectedItems == null) 
	        {
		        selectedItems = new String[0];
	        }
		    int[] tmpHashCodes = new int[selectedItems.length];
		    for (int i = 0; i < selectedItems.length; i++)
		    {
		        String tmp = selectedItems[i];
		        tmpHashCodes[i] = tmp.hashCode();
		    }
		    selectedFunctionHashCodes = tmpHashCodes;
		}
		
		for (Enumeration enumer = trace.getSamples(); !exit;)
		{
		    exit = !enumer.hasMoreElements();
			if (exit)
			{
				// for the final samples, modify the step value
				// so that they will also be included
				// now there are no new samples, so proceed directly to
				// adding the final values to the percent list
				stepValue = count;
			}
			else
			{
			    count++;
			    int compareValue = 0;
			    boolean match = false;
			    GppSample sample = (GppSample)enumer.nextElement();
			    if (basedOnBinaries)
				{
				    compareValue = getBinaryName(sample).hashCode();
				    for (int i = 0; i < selectedBinaryHashCodes.length; i++)
				    {
				        if (compareValue == selectedBinaryHashCodes[i])
				        {
				            match = true;
				            break;
				        }
				    }
				}
			    else
			    {
				    compareValue = getFunctionName(sample).hashCode();
				    for (int i = 0; i < selectedFunctionHashCodes.length; i++)
				    {
				        if (compareValue == selectedFunctionHashCodes[i])
				        {
				            match = true;
				            break;
				        }
				    }
			    }
			    
			    if (match)
			    {
			        ProfiledThread pt = null;
			        String name = sample.thread.threadName;
					if (profiledThreads.containsKey(name))
					{
						pt = (ProfiledThread)profiledThreads.get(name);
					}
				
					if (pt == null)
					{
						pt = new ProfiledThread();
					
						pt.setNameString(name);
						pt.setColor(((GppTrace)this.getTrace()).getThreadColorPalette().getColor(name));
						pt.setThreadId(sample.thread.threadId.intValue());
						
						pt.setActivityMarkCount((trace.samples.size() + granularityValue) / granularityValue + 1);
						for (int i = 0; i < timeStamp + stepValue * samplingInterval; i += stepValue * samplingInterval)
						{
							pt.zeroActivityMarkValues(i);
						}
						pt.setEnabled(this.graphIndex, true);
						profiledThreads.put(name, pt);
					}
	
					if (percentages.containsKey(pt))
					{
						Integer value = (Integer)percentages.get(pt);
						value = new Integer(value.intValue()+1);
						percentages.remove(pt);
						percentages.put(pt,value);
					}
					else
					{
						percentages.put(pt,new Integer(1));
					}
			    }
			}

			if (stepValue != 0 && count == stepValue)
			{	
				Vector<ProfiledGeneric> v = new Vector<ProfiledGeneric>(profiledThreads.values());
				Enumeration<ProfiledGeneric> pfEnum = v.elements();
				while (pfEnum.hasMoreElements())
				{
					ProfiledThread updatePt = (ProfiledThread)pfEnum.nextElement();
					if (percentages.containsKey(updatePt))
					{
						int samples = ((Integer)(percentages.get(updatePt))).intValue();
						int finalPerc = (samples * 100) / stepValue;
						updatePt.addActivityMarkValues(timeStamp + stepValue * samplingInterval, finalPerc, samples);
					}
					else
					{
						updatePt.zeroActivityMarkValues(timeStamp + stepValue * samplingInterval);
					}					
				}
				
				percentages.clear();
				count = 0;
				timeStamp += stepValue * samplingInterval;
			}
		}

		this.threadTable.getTable().deselectAll();
		this.threadTable.updateProfiledAndItemData(true);
		this.threadTable.getTable().redraw();

		// if this is not the last table, set the selected names to set up
		// the next table
	    if (   (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)
	    	|| (drawMode == Defines.FUNCTIONS_THREADS_BINARIES))
	    {
	    	this.threadTable.setSelectedNames();
	    }
	    else
	    {
	    	// This may not be needed needed
			shared.GPP_SelectedThreadNames = new String[0];
	    }
	}

	/*
	 * Because a table to the left of a binary table has changed, update
	 * the binary table.
	 * If there is no table to the right of this one, redraw the graph based
	 * on the changed data.
	 */
	public void refreshProfiledBinaryData(int drawMode)
	{
		// Must have a table to its left
	    if (   (drawMode != Defines.THREADS_BINARIES)
	    	&& (drawMode != Defines.THREADS_BINARIES_FUNCTIONS)
	    	&& (drawMode != Defines.THREADS_FUNCTIONS_BINARIES)
	    	&& (drawMode != Defines.FUNCTIONS_BINARIES)
	    	&& (drawMode != Defines.FUNCTIONS_BINARIES_THREADS)
	    	&& (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
	       )
	    {
	        System.out.println(Messages.getString("GppTraceGraph.wrongDrawMode"));  //$NON-NLS-1$
	        return;
	    }

	    // boolean to use inside loops (never trust a compiler...)
	    boolean basedOnThreads = (drawMode == Defines.THREADS_BINARIES)
	    					  || (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
	    					  || (drawMode == Defines.FUNCTIONS_THREADS_BINARIES); 

	    Hashtable<String,ProfiledBinary> profiledBinaries = new Hashtable<String,ProfiledBinary>();
		
	    GenericSampledTrace trace = (GenericSampledTrace)this.getTrace();
		int granularityValue = trace.samples.size() > GppTraceGraph.granularityValue ? GppTraceGraph.granularityValue : trace.samples.size();  

		String[] selectedItems;
		int[] selectedThreadIds = null;
		int[] selectedFunctionHashCodes = null;
		int count = 0;
		int timeStamp = 0;
		int stepValue = granularityValue;
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		boolean exit = false;
		
		Hashtable<ProfiledBinary,Integer> percentages = new Hashtable<ProfiledBinary,Integer>();
		PIVisualSharedData shared = this.getSharedDataInstance();
		
	    if (basedOnThreads)
	    {
	    	selectedItems = shared.GPP_SelectedThreadNames;
		    if (selectedItems == null)
		    {
		        selectedItems = new String[0];
		    }
		    int[] tmpThreadIds = new int[selectedItems.length];
		    for (int i = 0; i < selectedItems.length; i++)
		    {
		        String tmp = selectedItems[i].substring(selectedItems[i].lastIndexOf('_') + 1,
		                selectedItems[i].length());
		        tmpThreadIds[i] = Integer.parseInt(tmp);
		    }
		    selectedThreadIds = tmpThreadIds;
		}
		else
		{
		    selectedItems = shared.GPP_SelectedFunctionNames;
		    if (selectedItems == null) 
	        {
		        selectedItems = new String[0];
	        }
		    int[] tmpHashCodes = new int[selectedItems.length];
		    for (int i = 0; i < selectedItems.length; i++)
		    {
		        String tmp = selectedItems[i];
		        tmpHashCodes[i] = tmp.hashCode();
		    }
		    selectedFunctionHashCodes = tmpHashCodes;
		}
		
		for (Enumeration enumer = trace.getSamples(); !exit;)
		{
		    exit = !enumer.hasMoreElements();
			if (exit) 
			{
				// for the final samples, modify the step value
				// so that they will also be included
				// now there are no new samples, so proceed directly to
				// adding the final values to the percent list
				stepValue = count;
			}
			else
			{
			    count++;
			    int compareValue = 0;
			    boolean match = false;
			    GppSample sample = (GppSample)enumer.nextElement();
			    if (basedOnThreads)
			    {
				    compareValue = sample.thread.threadId.intValue();
				    for (int i = 0; i < selectedThreadIds.length; i++)
				    {
				        if (compareValue == selectedThreadIds[i])
				        {
				            match = true;
				            break;
				        }
				    }
			    }
				else
				{
				    compareValue = getFunctionName(sample).hashCode();
				    for (int i = 0; i < selectedFunctionHashCodes.length; i++)
				    {
				        if (compareValue == selectedFunctionHashCodes[i])
				        {
				            match = true;
				            break;
				        }
				    }
				}
			    
			    if (match)
			    {
			        ProfiledBinary pb = null;
			        String name = getFunctionName(sample);
					if (profiledBinaries.containsKey(name))
					{
						pb = (ProfiledBinary)profiledBinaries.get(name);
					}
				
					if (pb == null)
					{
						pb = new ProfiledBinary();
						
						pb.setNameString(name);
						pb.setColor(((GppTrace)this.getTrace()).getBinaryColorPalette().getColor(name));
						
						pb.setActivityMarkCount((trace.samples.size() + granularityValue) / granularityValue + 1);
						for (int i = 0; i < timeStamp + stepValue * samplingInterval; i += stepValue * samplingInterval)
						{
							pb.zeroActivityMarkValues(i);
						}
						profiledBinaries.put(name, pb);
					}
	
					if (percentages.containsKey(pb))
					{
						Integer value = (Integer)percentages.get(pb);
						value = new Integer(value.intValue()+1);
						percentages.remove(pb);
						percentages.put(pb,value);
					}
					else
					{
						percentages.put(pb,new Integer(1));
					}
			    }
			}

			if (stepValue != 0 && count == stepValue)
			{	
				Vector<ProfiledGeneric> v = new Vector<ProfiledGeneric>(profiledBinaries.values());
				Enumeration<ProfiledGeneric> pfEnum = v.elements();
				while (pfEnum.hasMoreElements())
				{
					ProfiledFunction updatePf = (ProfiledFunction)pfEnum.nextElement();
					if (percentages.containsKey(updatePf))
					{
						int samples = ((Integer)(percentages.get(updatePf))).intValue();
						int finalPerc = (samples * 100) / stepValue;
						updatePf.addActivityMarkValues(timeStamp + stepValue * samplingInterval, finalPerc, samples);
					}
					else
					{
						updatePf.zeroActivityMarkValues(timeStamp + stepValue * samplingInterval);
					}					
				}
				
				percentages.clear();
				count = 0;
				timeStamp += stepValue * samplingInterval;
			}
		}

		this.binaryTable.getTable().deselectAll();
		this.binaryTable.updateProfiledAndItemData(true);
		this.binaryTable.getTable().redraw();

		// if this is not the last table, set the selected names to set up
		// the next table
	    if (   (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
	    	|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS))
	    {
	    	this.binaryTable.setSelectedNames();
	    }
	    else
	    {
	    	// This may not be needed
			shared.GPP_SelectedBinaryNames = new String[0];
	    }
	}
	
	// sort profiled generics by decreasing total sample count
	private static void sortProfiledGenerics(Hashtable<String,ProfiledGeneric> prof, GppTrace gppTrace)
	{
		// use an insertion sort to create a sorted linked list
		// from the hashtable values
		Collection<ProfiledGeneric> values = prof.values();
		Vector<ProfiledGeneric> unsorted =   new Vector<ProfiledGeneric>(values);
		LinkedList<ProfiledGeneric> sorted = new LinkedList<ProfiledGeneric>();

		Enumeration<ProfiledGeneric> prEnum = unsorted.elements();
		while (prEnum.hasMoreElements())
		{
			ProfiledGeneric pg = prEnum.nextElement();
			if (sorted.size() == 0)
			{
				sorted.addFirst(pg);
			}
			else 
			{
				Iterator i = sorted.iterator();
				boolean ok = false;
				while (i.hasNext())
				{
					ProfiledGeneric next = (ProfiledGeneric)i.next();
					if (next.getTotalSampleCount() < pg.getTotalSampleCount())
					{
						sorted.add(sorted.indexOf(next), pg);
						ok = true;
						break;
					}
				}
				if (!ok)
					sorted.addLast(pg);
			}
		}
		
		// Add the sorted data 
		Iterator<ProfiledGeneric> iterator = sorted.iterator();
		if (sorted.size() > 0)
		{
			Vector<ProfiledGeneric> v = null;
			if (sorted.get(0) instanceof ProfiledThread)
			{
				v = gppTrace.getSortedThreads();
			}
			else if (sorted.get(0) instanceof ProfiledBinary)
			{
				v = gppTrace.getSortedBinaries();
			}
			else if (sorted.get(0) instanceof ProfiledFunction)
			{
				v = gppTrace.getSortedFunctions();
			}
			if (v != null)
			{
				v.clear();
				while (iterator.hasNext())
					v.add(0, iterator.next());
			}
		}
	}
	
	public void updateGraph()
	{
	    if (drawMode == Defines.BINARIES)
	    {
	        vPanel.refreshCumulativeThreadTable();
	    }
	    else if (drawMode == Defines.THREADS)
	    {
	        vPanel.refreshCumulativeThreadTable();
	    }

		this.repaint();
	}
	
	public void updateThreadTablePriorities(Hashtable<Integer,String> priorities)
	{
		this.threadTable.addPriorityColumn(priorities);
	}
	
	static private String stringNotFound = Messages.getString("GppTraceGraph.notFound");  //$NON-NLS-1$

	static private String stringBinaryAt         = Messages.getString("GppTraceGraph.binaryAt");  //$NON-NLS-1$
	static private String stringBinaryForAddress = Messages.getString("GppTraceGraph.binaryForAddress");  //$NON-NLS-1$
	static private String stringBinaryNotFound   = Messages.getString("GppTraceGraph.binaryNotFound");  //$NON-NLS-1$

	static private String stringFunctionAt         = Messages.getString("GppTraceGraph.functionAt"); //$NON-NLS-1$
	static private String stringFunctionForAddress = Messages.getString("GppTraceGraph.functionForAddress"); //$NON-NLS-1$
	static private String stringFunctionNotFound   = Messages.getString("GppTraceGraph.functionNotFound"); //$NON-NLS-1$
	
	public static String getBinaryName(GppSample s)
	{
	    String name = null;
	    
	    if (s.currentFunctionSym != null)
	    	name = s.currentFunctionSym.functionBinary.binaryName;

        if (   (s.currentFunctionItt != null)
           	&& ((name == null) || name.endsWith(stringNotFound)))
        {
            name = s.currentFunctionItt.functionBinary.binaryName;
        }
	    
	    if (   name == null
	    	|| name.startsWith(stringBinaryAt)
	    	|| name.startsWith(stringBinaryForAddress))
	    {
	        name = stringBinaryNotFound;
	    }
	    return name;
	}
	
	public static String getFunctionName(GppSample s)
	{
	    String name = null;
	    if (s.currentFunctionSym != null)
	    	name = s.currentFunctionSym.functionName;
	    
        if (   (s.currentFunctionItt != null)
        	&& ((name == null) || name.endsWith(stringNotFound)))
        {
        	name = s.currentFunctionItt.functionName;
        }

	    if (   (name == null)
	    	|| (name.startsWith(stringFunctionAt))
	    	|| (name.startsWith(stringFunctionForAddress)))
	    {
	        name = stringFunctionNotFound;
	    }
	    
	    return name;
	}
	
	public static long getFunctionAddress(GppSample s)
	{
	    if (s.currentFunctionSym != null)
	    {
	        String name = s.currentFunctionSym.functionName;
	        if (   (s.currentFunctionItt != null)
	        	&& (name == null || name.endsWith(Messages.getString("GppTraceGraph.notFound"))))  //$NON-NLS-1$
	        {
        		return s.currentFunctionItt.startAddress.longValue();
	        }
	        else
	        {
	        	return s.currentFunctionSym.startAddress.longValue();
	        }
	    }
	    else if (s.currentFunctionItt != null)
	    {
	        if (s.currentFunctionItt.functionName == null)
	        {
	        	if (s.currentFunctionSym != null)
	        		return s.currentFunctionSym.startAddress.longValue();
	        }
	        else
	        {
        		return s.currentFunctionItt.startAddress.longValue();
	        }
	    }

	    return 0;
	}

	public int getDrawMode()
    {
        return drawMode;
    }

    public void setDrawMode(int drawMode)
    {
    	if (
    		   (drawMode == Defines.THREADS)
    		|| (drawMode == Defines.THREADS_FUNCTIONS)
    		|| (drawMode == Defines.THREADS_FUNCTIONS_BINARIES)
    		|| (drawMode == Defines.THREADS_BINARIES)
    		|| (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
    		|| (drawMode == Defines.BINARIES)
    		|| (drawMode == Defines.BINARIES_THREADS)
    		|| (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)
    		|| (drawMode == Defines.BINARIES_FUNCTIONS)
    		|| (drawMode == Defines.BINARIES_FUNCTIONS_THREADS)
    		|| (drawMode == Defines.FUNCTIONS)
    		|| (drawMode == Defines.FUNCTIONS_THREADS)
    		|| (drawMode == Defines.FUNCTIONS_THREADS_BINARIES)
    		|| (drawMode == Defines.FUNCTIONS_BINARIES)
    		|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS))
    	{
    		if (this.drawMode != drawMode) {
    			this.setGraphImageChanged(true);
    		}
	        this.drawMode = drawMode;
	        refreshMode();
        }
        else
        {
            System.out.println(Messages.getString("GppTraceGraph.unknownDrawMode"));  //$NON-NLS-1$
            this.drawMode = Defines.THREADS;
            refreshMode();
        }
    }
    
    private void refreshMode()
    {
    	this.vPanel.refreshCumulativeThreadTable();
    }

	public int getUid() {
		return this.uid;
	}
	
	public GppTraceGraph getTraceGraph() {
		return this.thisTraceGraph;
	}
	
	public void setLeftSash(Sash leftSash) {
		this.leftSash = leftSash;
	}
	
	public Sash getLeftSash() {
		return this.leftSash;
	}
	
	public void setRightSash(Sash rightSash) {
		this.rightSash = rightSash;
	}
	
	public Sash getRightSash() {
		return this.rightSash;
	}

	public void setProfiledThreads(Vector<ProfiledGeneric> profiledThreads) {
		this.profiledThreads = profiledThreads;
	}
	
	public Vector<ProfiledGeneric> getProfiledThreads() {
		return this.profiledThreads;
	}
	
	public Vector<ProfiledGeneric> getSortedThreads() {
		return this.sortedProfiledThreads;
	}

	public void setProfiledBinaries(Vector<ProfiledGeneric> profiledBinaries) {
		this.profiledBinaries = profiledBinaries;
	}
	
	public Vector<ProfiledGeneric> getProfiledBinaries() {
		return this.profiledBinaries;
	}
	
	public Vector<ProfiledGeneric> getSortedBinaries() {
		return this.sortedProfiledBinaries;
	}

	public void setProfiledFunctions(Vector<ProfiledGeneric> profiledFunctions) {
		this.profiledFunctions = profiledFunctions;
	}
	
	public Vector<ProfiledGeneric> getProfiledFunctions() {
		return this.profiledFunctions;
	}
	
	public Vector<ProfiledGeneric> getSortedFunctions() {
		return this.sortedProfiledFunctions;
	}
	
	public ProfiledThreshold getThresholdThread() {
		return thresholdThread;
	}
	
	public ProfiledThreshold getThresholdBinary() {
		return thresholdBinary;
	}
	
	public ProfiledThreshold getThresholdFunction() {
		return thresholdFunction;
	}

	public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
	}
}
