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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;

import com.nokia.carbide.cpp.internal.pi.address.GppModelAdapter;
import com.nokia.carbide.cpp.internal.pi.address.GppTraceGraphSMP;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThreshold;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

/**
 * Each instance of this class represents one trace graph for profiling
 * CPU activity. 
 *
 */
public class GppTraceGraph extends GenericTraceGraph implements IGppTraceGraph,
		ActionListener, FocusListener, MouseMotionListener, MouseListener,
		MouseMoveListener, ITitleBarMenu, IContextMenu {

	/**
	 * amount of space at bottom of graph to contain things like x-axis units
	 * and button icons
	 */
	public static final int X_LEGEND_HEIGHT = 50;

	/**
	 * When the graph is drawn, this is the finest granularity drawn. E.g.,
	 * assume a granularity of 100, and a sample every ms. If the 1st function
	 * draw appears in 5 samples from 101ms to 200ms and 10 samples from 200ms
	 * to 300ms then the graph will show a point at height 5 at time 200
	 * connected by a line to a point at height 10 at time 300.
	 */
	static final int GRANULARITY_VALUE = 100;
	
	/** constant to indicate graph is a bar graph */
	public static final int BAR_MODE_ON = 3;
	/** constant to indicate graph is stacked-area graph */
	public static final int BAR_MODE_OFF = 4;

	private static final String EMPTY_STRING = "";//$NON-NLS-1$ 

	protected GppVisualiserPanel vPanel;
	protected String title;
	protected String shortTitle;

	/*
	 * Depending on this trace graph's graphIndex, one of these will be the base
	 * table, and others will be derived from its selections. The other tables
	 * will appear and disappear based on drawMode.
	 * 
	 * E.g., say that for graphIndex = 0, the base table is threadTable. Then
	 * this graph can represent the draw modes of: THREADS THREADS_BINARIES
	 * THREADS_BINARIES_FUNCTIONS THREADS_FUNCTIONS THREADS_FUNCTIONS_BINARIES
	 */
	protected AddrThreadTable threadTable;
	protected AddrBinaryTable binaryTable;
	protected AddrFunctionTable functionTable;
	
	/*
	 * Depending on this trace graph's graphIndex, one of these will match the
	 * entire trace's profiled vector, while the others will be derived from
	 * drilldown selections. The other vectors will only be meaningful depending
	 * on the drawMode.
	 */
	protected Vector<ProfiledGeneric> profiledThreads = new Vector<ProfiledGeneric>();
	protected Vector<ProfiledGeneric> profiledBinaries = new Vector<ProfiledGeneric>();
	protected Vector<ProfiledGeneric> profiledFunctions = new Vector<ProfiledGeneric>();

	private Vector<ProfiledGeneric> sortedProfiledThreads = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> sortedProfiledBinaries = new Vector<ProfiledGeneric>();
	private Vector<ProfiledGeneric> sortedProfiledFunctions = new Vector<ProfiledGeneric>();

	protected ProfiledThreshold thresholdThread;
	private ProfiledThreshold thresholdBinary;
	private ProfiledThreshold thresholdFunction;

	// when multiple tables are visible, they are separated by sashes
	private Sash leftSash;
	private Sash rightSash;

	protected int drawMode = Defines.THREADS;
	
	/** current graph drawing mode: either bar or stacked-area graph */
	public int barMode = BAR_MODE_OFF;

	protected int uid;

	private static class BarGraphData {
		public int x;
		public Color color;
	}

	private Vector<BarGraphData> barGraphData;

	/**
	 * The model adapter hides some of the SMP specifics. It must be created
	 * during initialisation of this graph.
	 */
	protected GppModelAdapter adapter;

	/** main Composite of legend view */
	private Composite holdTablesComposite;


	/**
	 * Constructor
	 * 
	 * @param graphIndex
	 *            the unique index of this graph
	 * @param trace
	 *            the trace data model to use
	 * @param uid
	 *            the Uid of the editor session
	 */
	public GppTraceGraph(int graphIndex, GppTrace trace, int uid) {
		super(trace);
		this.graphIndex = graphIndex;
		this.uid = uid;

	}

	/**
	 * Initialises the graph.
	 * 
	 * @param pageIndex
	 *            page index of the page which displays the graph
	 * @param trace
	 *            the trace model
	 */
	public void init(int pageIndex, GppTrace trace) {
		doCreateAdapter();
		doSetTitle();
		int granularityValue = trace.samples.size() > GppTraceGraph.GRANULARITY_VALUE ? GppTraceGraph.GRANULARITY_VALUE
				: trace.samples.size();

		// create the graph's 3 table objects - without any table items yet
		ProfileVisualiser pV = NpiInstanceRepository.getInstance()
				.getProfilePage(uid, pageIndex);
		holdTablesComposite = trace.createLegendComposite(pageIndex, graphIndex, pV.getBottomComposite(), getShortTitle());
		holdTablesComposite.setLayout(new FormLayout());

		createLegendTables(holdTablesComposite);

		// initialize the threshold counts
		int totalSampleCount = trace.getSampleAmount();

		// CH: this is called too many times (those values are global for the
		// whole trace) -> can we move it to GppTrace?
		NpiInstanceRepository
				.getInstance()
				.setPersistState(
						uid,
						"com.nokia.carbide.cpp.pi.address.thresholdCountThread", Integer.valueOf(new Double(totalSampleCount * (Double) NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadThread") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
		NpiInstanceRepository
				.getInstance()
				.setPersistState(
						uid,
						"com.nokia.carbide.cpp.pi.address.thresholdCountBinary", Integer.valueOf(new Double(totalSampleCount * (Double) NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadBinary") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$
		NpiInstanceRepository
				.getInstance()
				.setPersistState(
						uid,
						"com.nokia.carbide.cpp.pi.address.thresholdCountFunction", Integer.valueOf(new Double(totalSampleCount * (Double) NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdLoadFunction") + 0.5).intValue())); //$NON-NLS-1$ //$NON-NLS-2$

		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		// initialize the threshold items
		int bucketDuration = granularityValue * samplingInterval;
		int numberOfBuckets = GppTraceUtil.calculateNumberOfBuckets(trace
				.getLastSampleTime(), granularityValue);

		thresholdThread = new ProfiledThreshold(
				"dummy[0]::dummy_0", trace.getCPUCount(), trace.getGraphCount()); //$NON-NLS-1$
		thresholdBinary = new ProfiledThreshold(
				"\\dummy", trace.getCPUCount(), trace.getGraphCount()); //$NON-NLS-1$
		thresholdFunction = new ProfiledThreshold(
				"dummy::dummy()", trace.getCPUCount(), trace.getGraphCount()); //$NON-NLS-1$
		thresholdThread.setColor(trace.getThreadColorPalette().getColor(
				thresholdThread.getNameString()));
		thresholdThread.createBuckets(numberOfBuckets);
		thresholdThread.initialiseBuckets(bucketDuration);
		thresholdBinary.setColor(trace.getBinaryColorPalette().getColor(
				thresholdBinary.getNameString()));
		thresholdBinary.createBuckets(numberOfBuckets);
		thresholdBinary.initialiseBuckets(bucketDuration);
		thresholdFunction.setColor(trace.getFunctionColorPalette().getColor(
				thresholdFunction.getNameString()));
		thresholdFunction.createBuckets(numberOfBuckets);
		thresholdFunction.initialiseBuckets(bucketDuration);

		int graphType = adapter.getGraphType();

		if (graphType == PIPageEditor.THREADS_PAGE) {
			this.drawMode = Defines.THREADS;
			int threshold = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$

			for (ProfiledGeneric profiled : trace.getSortedThreads()) {
				if (adapter.getTotalSampleCount(profiled) > 0){
					if (adapter.getTotalSampleCount(profiled) < threshold){//check below threshold
						adapter.addItem(thresholdThread, graphIndex, profiled, adapter.getTotalSampleCount(profiled));
					} else {
						profiledThreads.add(profiled);
					}
				}
			}

			this.threadTable.setTableViewer(Defines.THREADS);
			this.profiledThreads = trace.getSortedThreads();
		} else if (graphType == PIPageEditor.BINARIES_PAGE) {
			this.drawMode = Defines.BINARIES;
			int threshold = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$

			for (ProfiledGeneric profiled : trace.getSortedBinaries()) {
				if (adapter.getTotalSampleCount(profiled) > 0){
					if (adapter.getTotalSampleCount(profiled) < threshold){//check below threshold
						adapter.addItem(thresholdBinary, graphIndex, profiled, adapter.getTotalSampleCount(profiled));
					} else {
						profiledBinaries.add(profiled);
					}
					
				}
			}

			this.binaryTable.setTableViewer(Defines.BINARIES);

			this.profiledBinaries = trace.getSortedBinaries();
		} else if (graphType == PIPageEditor.FUNCTIONS_PAGE) {
			this.drawMode = Defines.FUNCTIONS;
			int threshold = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$

			for (ProfiledGeneric profiled : trace.getSortedFunctions()) {
				if (adapter.getTotalSampleCount(profiled) > 0){
					if (adapter.getTotalSampleCount(profiled) < threshold){//check below threshold
						adapter.addItem(thresholdFunction, graphIndex, profiled, adapter.getTotalSampleCount(profiled));
					} else {
						profiledFunctions.add(profiled);
					}
				}
			}
			this.functionTable.setTableViewer(Defines.FUNCTIONS);

			this.profiledFunctions = trace.getSortedFunctions();
		} else {
			throw new IllegalStateException(Messages
					.getString("GppTraceGraph.traceGraphInternalErrorIn")); //$NON-NLS-1$
		}


		// since the trace can be shown any of 3 ways (by thread, by binary, or
		// by function), make sure that all 3 are ready for display
		// Sse sorted vector to be consistent with other call to
		// genericRefreshCumulativeThreadTable()
		// fix issue with wrong color on newly opened npi file before a change
		// in the table
		if (this.profiledThreads.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledThreads.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledThreads
					.elements());
		}
		if (this.profiledBinaries.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledBinaries
					.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledBinaries
					.elements());
		}
		if (this.profiledFunctions.size() > 0) {
			genericRefreshCumulativeThreadTable(this.profiledFunctions
					.elements());
			genericRefreshCumulativeThreadTable(this.sortedProfiledFunctions
					.elements());
		}

		if (this.profiledThreads.size() <= 0
				&& this.profiledBinaries.size() <= 0
				&& this.profiledFunctions.size() <= 0) {
			try {
				throw new Exception(Messages
						.getString("GppTraceGraph.traceGraphInternalErrorAt")); //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.vPanel = new GppVisualiserPanel(this);

	}

	/**
	 * Sets the graph-specific title
	 */
	protected void doSetTitle() {
		title = Messages.getString("GppTraceGraph.1");		 //$NON-NLS-1$
		shortTitle = Messages.getString("GppTraceGraph.0"); //$NON-NLS-1$
	}

	/**
	 * Creates a GppModelAdapter
	 */
	protected void doCreateAdapter() {
		adapter = new GppModelAdapter(this.graphIndex);
	}

	/**
	 * Creates the three legend tables for this graph (for threads, binaries,
	 * functions)
	 * 
	 * @param holdTablesComposite
	 */
	protected void createLegendTables(Composite holdTablesComposite) {
		this.threadTable = new AddrThreadTable(this, holdTablesComposite, adapter);
		this.binaryTable = new AddrBinaryTable(this, holdTablesComposite, adapter);
		this.functionTable = new AddrFunctionTable(this, holdTablesComposite, adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.pi.address.IGppTraceGraph#getGppTrace()
	 */
	public GppTrace getGppTrace() {
		return (GppTrace) this.getTrace();
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
		// determine the threads that can be shown, and get rid of all
		// drilldowns
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
			// this is the first GPP graph to be told of the selection area
			// change,
			// so it gathers the overall trace information
			double doubleStartTime = PIPageEditor.currentPageEditor()
					.getStartTime();
			double doubleEndTime = PIPageEditor.currentPageEditor()
					.getEndTime();
			GppTrace trace = (GppTrace) this.getTrace();
			trace.setSelectedArea(doubleStartTime, doubleEndTime);

			// take care of the threshold members
			int sampleCount = 0;
			int thresholdCount;

			if (graphIndex == PIPageEditor.THREADS_PAGE) {
				thresholdCount = (Integer) NpiInstanceRepository
						.getInstance()
						.getPersistState(uid,
								"com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					for (int i = 0; i < profiledThreads.size(); i++)
						if (profiledThreads.elementAt(i).getTotalSampleCount() < thresholdCount)
							sampleCount += profiledThreads.elementAt(i)
									.getSampleCount(graphIndex);
					thresholdThread
							.setSampleCount(this.graphIndex, sampleCount);
				}
			} else if (graphIndex == PIPageEditor.BINARIES_PAGE) {
				thresholdCount = (Integer) NpiInstanceRepository
						.getInstance()
						.getPersistState(uid,
								"com.nokia.carbide.cpp.pi.address.thresholdCountBinary"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					for (int i = 0; i < profiledBinaries.size(); i++)
						if (profiledBinaries.elementAt(i).getTotalSampleCount() < thresholdCount)
							sampleCount += profiledBinaries.elementAt(i)
									.getSampleCount(graphIndex);
					thresholdBinary
							.setSampleCount(this.graphIndex, sampleCount);
				}
			} else if (graphIndex == PIPageEditor.FUNCTIONS_PAGE) {
				thresholdCount = (Integer) NpiInstanceRepository
						.getInstance()
						.getPersistState(uid,
								"com.nokia.carbide.cpp.pi.address.thresholdCountFunction"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					for (int i = 0; i < profiledFunctions.size(); i++)
						if (profiledFunctions.elementAt(i)
								.getTotalSampleCount() < thresholdCount)
							sampleCount += profiledFunctions.elementAt(i)
									.getSampleCount(graphIndex);
					thresholdFunction.setSampleCount(this.graphIndex,
							sampleCount);
				}
			}

			double startTime = PIPageEditor.currentPageEditor().getStartTime();
			double endTime = PIPageEditor.currentPageEditor().getEndTime();

			// send this message to the other GPP graphs
			PIEvent be2 = new PIEvent(be.getValueObject(),
					PIEvent.SELECTION_AREA_CHANGED2);

			// update the selection area shown
			for (int i = 0; i < trace.getGraphCount(); i++) {
				IGppTraceGraph graph = trace.getGppGraph(i, getUid());

				if (graph != this) {
					graph.piEventReceived(be2);
					// once per graph, update the selection interval shown
					graph.getCompositePanel().getVisualiser().updateStatusBarTimeInterval(startTime, endTime);
				}

				// change the graph's selected time interval
				graph.setSelectionStart(startTime * 1000);
				graph.setSelectionEnd(endTime * 1000);
				graph.getCompositePanel().setSelectionFields(
						(int) (startTime * 1000), (int) (endTime * 1000));
			}

			this.parentComponent.getSashForm().redraw();
			be = be2;
			// FALL THROUGH
		case PIEvent.SELECTION_AREA_CHANGED2: {
			// this code lets each graph's base thread/binary/function table
			// update the other tables
			switch (drawMode) {
			case Defines.THREADS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_BINARIES_FUNCTIONS: {
				this.threadTable.piEventReceived(be);
				break;
			}
			case Defines.BINARIES:
			case Defines.BINARIES_THREADS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS_THREADS: {
				this.binaryTable.piEventReceived(be);
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES_THREADS: {
				this.functionTable.piEventReceived(be);
				break;
			}
			}

			this.vPanel.refreshCumulativeThreadTable();
			this.setGraphImageChanged(true); // any selection change to drill
			// down will change graph
			this.repaint();
			break;
		}

			// in the graph, show all values from the rightmost table
		case PIEvent.SET_FILL_ALL_THREADS:
			this.setGraphImageChanged(true); // any selection change to drill
			// down will change graph
			this.vPanel.piEventReceived(be);
			break;

		// in the graph, don't fill between the lines will the color of
		// the line above
		case PIEvent.SET_FILL_OFF:
			this.setGraphImageChanged(true); // any selection change to drill
			// down will change graph
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
			this.setGraphImageChanged(true); // any selection change to drill
			// down will change graph
			this.vPanel.piEventReceived(be);
			break;

		// Redraw the graph because the thread table's selected values have
		// changed.
		// The thread table is handled (setting of array of selected threads
		// and table redraw) by the table selection listener or mouse listener.
		case PIEvent.CHANGED_THREAD_TABLE:
			switch (drawMode) {
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES: {
				this.threadTable.piEventReceived(be);
				break;
			}
			case Defines.BINARIES_THREADS_FUNCTIONS: {
				this.binaryTable.piEventReceived(be);
				break;
			}
			case Defines.FUNCTIONS_THREADS_BINARIES: {
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
			// if fill selected, the graph is drawn again
			this.vPanel.piEventReceived(be);
			break;

		// Redraw the graph because the binary table's selected values have
		// changed.
		// The binary table is handled (setting of array of selected binaries
		// and table redraw) by the table selection listener or mouse listener.
		case PIEvent.CHANGED_BINARY_TABLE:
			switch (drawMode) {
			case Defines.BINARIES_THREADS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS_THREADS: {
				this.binaryTable.piEventReceived(be);
				break;
			}
			case Defines.THREADS_BINARIES_FUNCTIONS: {
				this.threadTable.piEventReceived(be);
				break;
			}
			case Defines.FUNCTIONS_BINARIES_THREADS: {
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
			// if fill selected, the graph is drawn again
			this.vPanel.piEventReceived(be);
			break;

		// Redraw the graph because the function table's selected values have
		// changed.
		// The function table is handled (setting of array of selected functions
		// and table redraw) by the table selection listener or mouse listener.
		case PIEvent.CHANGED_FUNCTION_TABLE:
			switch (drawMode) {
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES_THREADS: {
				this.functionTable.piEventReceived(be);
				break;
			}
			case Defines.THREADS_FUNCTIONS_BINARIES: {
				this.threadTable.piEventReceived(be);
				break;
			}
			case Defines.BINARIES_FUNCTIONS_THREADS: {
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
			case Defines.THREADS: {
				break;
			}
			case Defines.BINARIES: {
				break;
			}
			case Defines.FUNCTIONS: {
				break;
			}
			default: {
				break;
			}
			}
			this.parentComponent.setActive(this);
			break;

		case PIEvent.SCALE_CHANGED:
			this.setGraphImageChanged(true);
			break;
			
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#action(java
	 * .lang.String)
	 */
	@Override
	public void action(String actionString) {
		System.out
				.println(Messages.getString("GppTraceGraph.actionString") + actionString); //$NON-NLS-1$

		if (actionString.equals("resetToCurrentMode")) //$NON-NLS-1$
		{
			if (drawMode == Defines.THREADS_FUNCTIONS)
				this.setDrawMode(Defines.THREADS);
			else if (drawMode == Defines.BINARIES_FUNCTIONS)
				this.setDrawMode(Defines.BINARIES);
			else
				System.out
						.println(Messages.getString("GppTraceGraph.drawMode") + drawMode); //should not print this ever  //$NON-NLS-1$
		} else {
			switch (drawMode) {
			case Defines.THREADS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_BINARIES_FUNCTIONS: {
				this.threadTable.action(actionString);
				break;
			}
			case Defines.BINARIES:
			case Defines.BINARIES_THREADS:
			case Defines.BINARIES_THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS_THREADS: {
				this.binaryTable.action(actionString);
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_THREADS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES_THREADS: {
				this.functionTable.action(actionString);
				break;
			}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent fe) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent fe) {
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
	 * org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent
	 * )
	 */
	public void mousePressed(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent
	 * )
	 */
	public void mouseReleased(MouseEvent me) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d
	 * .MouseEvent)
	 */
	public void mouseDoubleClicked(MouseEvent me) {
		Object[] result = this.getProfiledGenericUnderMouseImproved(me, false);
		if ((result == null)) {
			return;
		}

		ProfiledGeneric pg = (ProfiledGeneric) result[0];
		GenericTable gtu = getLegendTableForGraph();

		if (pg != null) {
			if (gtu.getIndex(pg) == null)
				return;
			int[] index = new int[1];
			index[0] = gtu.getIndex(pg).intValue();
			gtu.setSelectedIndicesXOR(index);
		}
	}

	/**
	 * For a stacked-area chart, this method retrieves the ProfiledGeneric
	 * currently pointed to by the mouse. It also determines the percentage
	 * activity load of this ProfiledGeneric in the current bucket. The load
	 * will also be determined if no ProfiledGeneric is currently pointed to
	 * (unresolved items).
	 * 
	 * 
	 * @param me
	 * @return Object[2] of which [0] is the ProfiledGeneric (may be null) and
	 *         [1] is a String containing the load
	 */
	private Object[] getProfiledGenericUnderMouseImproved(MouseEvent me, boolean includeLoadString) {
		Object[] result = new Object[2];

		int adjustedX = me.x;
		if (me.x >= (this.getSize().width)) {
			adjustedX = this.getSize().width - 1;
		}
		double x = getTimeForXCoordinate(adjustedX, this.getScale());
		if (x > PIPageEditor.currentPageEditor().getMaxEndTime() * 1000) {
			return result;
		}

		double y = me.y;

		y = y * 100	/ (this.getVisualSize().height - GppTraceGraph.X_LEGEND_HEIGHT);
		y = 100 - y;
		if (y <= 0){
			return result;
		}
		
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState(
				"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		int granularityValue = ((GppTrace) (this.getTrace())).getGranularity();

		int bucket = getBucketForTime(x);

		//since the value is drawn at bucket mid-point, we need to work out whether
		//x is to the left or right of the midpoint, in other words
		//whether we need to take into account the gradient from the previous
		//bucket or the next bucket
		int offset = ((int)(x / samplingInterval)) % granularityValue; //offset into the bucket
		int d = offset - (int)(granularityValue/2); //distance in samples off bucket mid-point, negative for left
		double prt = Math.abs(d / (granularityValue * 1d)); //expressed as proportion
		double saveLastPeak = 0f;
		
		for (ProfiledGeneric p : getSortedProfiledsForGraph()) {
			if (!p.isEnabled(graphIndex)){
				continue;
			}
			float[] cum = p.getCumulativeList(graphIndex);
			float[] val = adapter.getActivityList(p);
			if (cum == null || val == null || bucket >= cum.length){
				return result;			
			}

			float lowCur = cum[bucket];
			float topCur = cum[bucket] + val[bucket];

			float lowNext = 0;
			float topNext = 0;

			double lowDiff = 0;
			double topDiff = 0;

			if (d < 0 && bucket > 0) {
				// use gradient of previous bucket
				lowNext = cum[bucket - 1];
				topNext = cum[bucket - 1] + val[bucket - 1];

			} else if (d > 0 && bucket < cum.length - 2) {
				// use gradient of next bucket
				lowNext = cum[bucket + 1];
				topNext = cum[bucket + 1] + val[bucket + 1];

			}
			lowDiff = (lowNext - lowCur) * prt;
			topDiff = (topNext - topCur) * prt;

			// check whether y is in the profiled's value range
			if (y >= lowCur + lowDiff && y < topCur + topDiff) {
				result[0] = p;
				if (includeLoadString){
					//round to 2 dec places
					result[1] = String.format("%.2f", (topCur + topDiff - lowCur - lowDiff));//$NON-NLS-1$
				}
				return result;
			}
			saveLastPeak = topCur + topDiff;
		}
		
		if (includeLoadString){
			// within the area of not shown profiled items
			result[0] = null;
			result[1] = String.format("%.2f", (100 - saveLastPeak));//$NON-NLS-1$
		}
		
		return result;
	}
	
	/**
	 * Returns the bucket index for the given time
	 * @param time
	 * @return
	 */
	private int getBucketForTime(double time) {
		return ((int) (time / ((GppTrace) (this.getTrace())).getBucketDuration()));
	}

	/**
	 * @return the sorted collection of ProfiledGeneric appropriate for the
	 *         current draw mode of the graph
	 */
	public Vector<ProfiledGeneric> getSortedProfiledsForGraph() {
		switch (drawMode) {
		case Defines.THREADS:
		case Defines.BINARIES_THREADS:
		case Defines.FUNCTIONS_THREADS:
		case Defines.BINARIES_FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_BINARIES_THREADS: {
			return sortedProfiledThreads ;
		}
		case Defines.BINARIES:
		case Defines.THREADS_BINARIES:
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_THREADS_BINARIES: {
			return sortedProfiledBinaries;
		}
		case Defines.FUNCTIONS:
		case Defines.THREADS_FUNCTIONS:
		case Defines.BINARIES_FUNCTIONS:
		case Defines.THREADS_BINARIES_FUNCTIONS:
		case Defines.BINARIES_THREADS_FUNCTIONS: {
			return sortedProfiledFunctions;
		}
		}
		throw new IllegalArgumentException();
		}

	/**
	 * @return the sorted collection of ProfiledGeneric appropriate for the
	 *         current draw mode of the graph
	 */
	private GenericAddrTable getLegendTableForGraph() {

		switch (drawMode) {
		case Defines.THREADS:
		case Defines.BINARIES_THREADS:
		case Defines.FUNCTIONS_THREADS:
		case Defines.BINARIES_FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_BINARIES_THREADS: {
			return this.threadTable;
		}
		case Defines.BINARIES:
		case Defines.THREADS_BINARIES:
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_THREADS_BINARIES: {
			return this.binaryTable;
		}
		case Defines.FUNCTIONS:
		case Defines.THREADS_FUNCTIONS:
		case Defines.BINARIES_FUNCTIONS:
		case Defines.THREADS_BINARIES_FUNCTIONS:
		case Defines.BINARIES_THREADS_FUNCTIONS: {
			return this.functionTable;
		}
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Converts the passed X-coordiate into a time value (in milliseconds) using
	 * the scale provided. Makes sure the return value is non-negative.
	 * 
	 * @param x
	 *            the x coordinate to use
	 * @param scale
	 *            the scale to use
	 * @return time in milliseconds
	 */
	protected double getTimeForXCoordinate(int x, double scale) {
		double time = x * scale;
		// mouse event may return out of range X, that may
		// crash when we use it to index data array
		time = time >= 0 ? time : 0;
		return time;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.
	 * MouseEvent)
	 */
	public void mouseMoved(MouseEvent me) {
		double x = getTimeForXCoordinate(me.x, this.getScale());
		double y = me.y;

		if (y >= this.getVisualSizeY() - GppTraceGraph.X_LEGEND_HEIGHT
				|| x >= PIPageEditor.currentPageEditor().getMaxEndTime() * 1000) {
			//don't set the tooltip to null here since it might affect other plugins, such as button plugin
			return;
		}

		if (NpiInstanceRepository.getInstance() == null
				|| NpiInstanceRepository
						.getInstance()
						.activeUidGetPersistState(
								"com.nokia.carbide.cpp.pi.address.samplingInterval") == null) //$NON-NLS-1$)
			return;

		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		if (this.barMode == GppTraceGraph.BAR_MODE_ON) {
			GppSample samp = getSampleUnderMouse(me.x, this.getScale(),
					samplingInterval);
			if (samp == null) {
				this.setToolTipText(null);
				return;
			}
			switch (drawMode) {
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS: {
				try {
					this.setToolTipText(samp.sampleSynchTime + "ms @" + //$NON-NLS-1$
							Long.toHexString(samp.programCounter) + " " + //$NON-NLS-1$
							samp.thread.process.name + "::" + //$NON-NLS-1$
							samp.thread.threadName + "_" + //$NON-NLS-1$
							samp.thread.threadId);
				} catch (NullPointerException e2) {
					this
							.setToolTipText(Messages
									.getString("GppTraceGraph.cannotResolveThreadName")); //$NON-NLS-1$
				}
				break;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES: {
				try {
					if (samp.getCurrentFunctionSym().getFunctionBinary().getBinaryName()
							.endsWith(Messages
									.getString("GppTraceGraph.NotFound"))) //$NON-NLS-1$
						throw new NullPointerException();
					this.setToolTipText(samp.sampleSynchTime + "ms @" + //$NON-NLS-1$
							Long.toHexString(samp.programCounter) + " " + //$NON-NLS-1$
							samp.getCurrentFunctionSym().getFunctionBinary().getBinaryName());
				} catch (NullPointerException e) {
					try {
						this
								.setToolTipText(samp.sampleSynchTime + "ms @" + //$NON-NLS-1$
										Long.toHexString(samp.programCounter)
										+ " " + //$NON-NLS-1$
										samp.getCurrentFunctionItt().getFunctionBinary().getBinaryName());
					} catch (NullPointerException e2) {
						this
								.setToolTipText(Messages
										.getString("GppTraceGraph.cannotResolveBinaryName")); //$NON-NLS-1$
					}
				}
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS: {
				try {
					if (samp.getCurrentFunctionSym().getFunctionBinary().getBinaryName()
							.endsWith(Messages
									.getString("GppTraceGraph.notFound"))) //$NON-NLS-1$
						throw new NullPointerException();

					this.setToolTipText(samp.sampleSynchTime + "ms @" + //$NON-NLS-1$
							Long.toHexString(samp.programCounter) + " " + //$NON-NLS-1$
							samp.getCurrentFunctionSym().getFunctionName());
				} catch (NullPointerException e) {
					try {
						this.setToolTipText(samp.sampleSynchTime + "ms @" + //$NON-NLS-1$
								Long.toHexString(samp.programCounter) + " " + //$NON-NLS-1$
								samp.getCurrentFunctionItt().getFunctionName());
					} catch (NullPointerException e2) {
						this
								.setToolTipText(Messages
										.getString("GppTraceGraph.cannotResolveFunctionName")); //$NON-NLS-1$
					}
				}
				break;
			}
			default:
				return;
			}

			return; // return for barMode == GppTraceGraph.BAR_MODE_ON
		}

		// barMode == GppTraceGraph.BAR_MODE_OFF

		Object[] result = this.getProfiledGenericUnderMouseImproved(me, true);
		if (result == null || (result[0] == null && result[1] == null)) {
			this.setToolTipText(null);
			return;
		}

		if (me.x >= (this.getSize().width)) {
			x = (this.getSize().width - 1) * this.getScale();
		}

		ProfiledGeneric pg = (ProfiledGeneric) result[0];

		String string = (String) result[1];
		if (pg == null) {
			switch (drawMode) {
			case Defines.THREADS:
			case Defines.BINARIES_THREADS:
			case Defines.FUNCTIONS_THREADS:
			case Defines.BINARIES_FUNCTIONS_THREADS:
			case Defines.FUNCTIONS_BINARIES_THREADS: {
				this
						.setToolTipText(string
								+ "% " + Messages.getString("GppTraceGraph.unknownOrExcludedThreads")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			case Defines.BINARIES:
			case Defines.THREADS_BINARIES:
			case Defines.THREADS_FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_BINARIES:
			case Defines.FUNCTIONS_THREADS_BINARIES: {
				this
						.setToolTipText(string
								+ "% " + Messages.getString("GppTraceGraph.unknownOrExcludedBinaries")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			case Defines.FUNCTIONS:
			case Defines.THREADS_FUNCTIONS:
			case Defines.BINARIES_FUNCTIONS:
			case Defines.THREADS_BINARIES_FUNCTIONS:
			case Defines.BINARIES_THREADS_FUNCTIONS: {
				this
						.setToolTipText(string
								+ "% " + Messages.getString("GppTraceGraph.unknownOrExcludedFunctions")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			default:
				break;
			}
		} else {
			this.setToolTipText(string + "% " + pg.getNameString()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the closest matching sample with the given x-coordinate. May
	 * return null if there isn't a matching sample in the immediate vicinity of
	 * x. This method is typically used in bar mode.
	 * 
	 * @param xPoint
	 * @param scale
	 * @param samplingInterval
	 * @return
	 */
	protected GppSample getSampleUnderMouse(int xPoint, double scale,
			int samplingInterval) {
		GppTrace trace = (GppTrace)this.getTrace();
		GppSample match = null;
		double x = xPoint * scale;
		x = x >= 0 ? x : 0;
		double xStart = (xPoint -5 ) * scale;
		double xEnd = (xPoint +5) * scale;
		
		int start =  ((int)(xStart + .0005) * trace.getCPUCount())/samplingInterval;
		if (start < 0){
			start = 0;
		} else if (start >= trace.getSampleAmount()){
			start = trace.getSampleAmount()-1;
		}

		int end =  ((int)(xEnd + .0005) * trace.getCPUCount())/samplingInterval;
		if (end >= trace.getSampleAmount()){
			end = trace.getSampleAmount()-1;
		}

		//loop through samples with the correct CPU id in close vicinity, and find the closest one to the given x-coordinate 
		for (int i = start; i <= end; i++) {
			GppSample tmp = trace.getSortedGppSamples()[i];
			if (doCheckSampleMatch(tmp) && (match == null || Math.abs(tmp.sampleSynchTime - x) < Math.abs(match.sampleSynchTime - x)) && isSampleEnabled(tmp)){
				match = tmp;
			}
		}
		
		return match;
	}
	
	/**
	 * According to current graph index and drawing mode, check whether this 
	 * sample is enabled  
	 * @param sample the sample to use
	 * @return true, if enabled, false if disabled or cannot be determined
	 */
	private boolean isSampleEnabled(GppSample sample) {
		
		switch (drawMode) {
		case Defines.THREADS:
			return isSampleEnabled(sample, true, false, false);			
		case Defines.BINARIES:
			return isSampleEnabled(sample, false, true, false);			
		case Defines.FUNCTIONS:
			return isSampleEnabled(sample, false, false, true);			
		case Defines.BINARIES_THREADS:
		case Defines.THREADS_BINARIES:
			return isSampleEnabled(sample, true, true, false);			
		case Defines.FUNCTIONS_THREADS:
		case Defines.THREADS_FUNCTIONS:
			return isSampleEnabled(sample, true, false, true);			
		case Defines.FUNCTIONS_BINARIES:
		case Defines.BINARIES_FUNCTIONS:
			return isSampleEnabled(sample, false, true, true);	
		case Defines.BINARIES_FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_BINARIES_THREADS: 
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_THREADS_BINARIES: 
		case Defines.THREADS_BINARIES_FUNCTIONS:
		case Defines.BINARIES_THREADS_FUNCTIONS: 
			return isSampleEnabled(sample, true, true, true);	
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Check whether a combination of thread / function / binary for this sample
	 * is enabled in the current graph. 
	 * @param sample
	 * @param checkThreads
	 * @param checkBinaries
	 * @param checkFunctions
	 * @return
	 */
	private boolean isSampleEnabled(GppSample sample, boolean checkThreads, boolean checkBinaries,
			boolean checkFunctions) {
		
		boolean ret = true;
		GppTrace trace = (GppTrace) this.getTrace();
		
		if (checkThreads){
			ret &= trace.getIndexedThreads().get(sample.threadIndex).isEnabled(graphIndex);
		}
		if (checkBinaries){
			ret &= trace.getIndexedBinaries().get(sample.binaryIndex).isEnabled(graphIndex);			
		}
		if (checkFunctions){
			ret &= trace.getIndexedFunctions().get(sample.functionIndex).isEnabled(graphIndex);			
		}
		return ret;
	}

	/**
	 * Check the sample fulfils conditions for getSampleUnderMouse()
	 * @return true if it fulfils matching conditions, false otherwise
	 */
	protected boolean doCheckSampleMatch(GppSample sample){
		return true;
	}

	public GenericTable getTableUtils() {
		switch (drawMode) {
		case Defines.THREADS:
		case Defines.BINARIES_THREADS:
		case Defines.FUNCTIONS_THREADS:
		case Defines.BINARIES_FUNCTIONS_THREADS:
		case Defines.FUNCTIONS_BINARIES_THREADS: {
			return this.threadTable;
		}
		case Defines.BINARIES:
		case Defines.THREADS_BINARIES:
		case Defines.THREADS_FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_BINARIES:
		case Defines.FUNCTIONS_THREADS_BINARIES: {
			return this.binaryTable;
		}
		case Defines.FUNCTIONS:
		case Defines.THREADS_FUNCTIONS:
		case Defines.BINARIES_FUNCTIONS:
		case Defines.THREADS_BINARIES_FUNCTIONS:
		case Defines.BINARIES_THREADS_FUNCTIONS: {
			return this.functionTable;
		}
		default:
			break;
		}

		System.out
				.println(Messages.getString("GppTraceGraph.debugDrawMode") + drawMode); //$NON-NLS-1$
		return null;
	}

	public GppVisualiserPanel getVisualiserPanel() {
		return this.vPanel;
	}

	public AddrThreadTable getThreadTable() {
		return this.threadTable;
	}

	public AddrBinaryTable getBinaryTable() {
		return this.binaryTable;
	}

	public AddrFunctionTable getFunctionTable() {
		return this.functionTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paint(org.
	 * eclipse.draw2d.Panel, org.eclipse.draw2d.Graphics)
	 */
	@Override
	public void paint(Panel panel, Graphics graphics) {
		this.setSize(panel.getClientArea().width, panel.getClientArea().height);
		this.vPanel.paintComponent(panel, graphics);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paintLeftLegend
	 * (org.eclipse.draw2d.FigureCanvas, org.eclipse.swt.graphics.GC)
	 */
	@Override
	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
		GC localGC = gc;

		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas
				.getClientArea();

		int visY = rect.height;

		float visYfloat = visY - GppTraceGraph.X_LEGEND_HEIGHT;

		if (visYfloat < 0f)
			visYfloat = 0f;

		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));

		// write each next number if there is space
		// float values will be slightly smaller than the actual result
		// and they will be incremented by one, since rounding to int
		// discards the remaining decimals
		int percent = 100;
		int previousBottom = 0; // bottom of the previous legend drawn
		for (float y = 0f; percent >= 0; y += visYfloat * 10000f / 100001f, percent -= 10) {
			String legend = "" + percent + "%"; //$NON-NLS-1$ //$NON-NLS-2$
			Point extent = gc.stringExtent(legend);

			gc.drawLine(IGenericTraceGraph.Y_LEGEND_WIDTH - 3, (int) y + 1,
					IGenericTraceGraph.Y_LEGEND_WIDTH, (int) y + 1);

			if ((int) y >= previousBottom) {
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
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#repaint()
	 */
	@Override
	public void repaint() {
		this.parentComponent.repaintComponent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.address.IGppTraceGraph#drawBarsGpp(java.util
	 * .Vector, org.eclipse.draw2d.Graphics, java.lang.Object[])
	 */
	public void drawBarsGpp(Vector<ProfiledGeneric> profiledGenerics,
			Graphics graphics, Object[] selection) {
		if (this.updateCumulativeThreadTableIsNeeded
				|| this.barGraphData == null) {
			this.updateBarGraphData(profiledGenerics);
		}

		this.updateIfNeeded(profiledGenerics);

		Enumeration<BarGraphData> barEnum = this.barGraphData.elements();

		int drawX = -1;
		int lastDrawX = -10;
		double scale = this.getScale();
		int y = this.getVisualSizeY() - 51;
		org.eclipse.draw2d.geometry.Rectangle visibleArea = this
				.getVisibleArea(graphics);

		while (barEnum.hasMoreElements()) {
			BarGraphData bgd = barEnum.nextElement();
			drawX = (int) ((bgd.x) / scale);

			if (drawX >= visibleArea.x
					&& drawX < visibleArea.x + visibleArea.width) {
				if (debug)
					System.out
							.println(Messages.getString("GppTraceGraph.draw") + drawX + " " + scale + " " + bgd.x); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (drawX != lastDrawX) {
					graphics.setForegroundColor(bgd.color);
					graphics.drawLine(drawX, 0, drawX, y);
					lastDrawX = drawX;
				}
			}
		}
	}

	/**
	 * Updates this.barGraphData.
	 * 
	 * @param profiledGenerics
	 *            Vector of either sortedThreads / sortedBinaries /
	 *            sortedFunctions
	 */
	private void updateBarGraphData(Vector<ProfiledGeneric> profiledGenerics) {
		if (this.barGraphData == null)
			this.barGraphData = new Vector<BarGraphData>();
		this.barGraphData.clear();

		int x = 0;

		// find the first enabled profiled generic
		int firstEnabled;
		for (firstEnabled = 0; firstEnabled < profiledGenerics.size(); firstEnabled++)
			if (((ProfiledGeneric) profiledGenerics.get(firstEnabled))
					.isEnabled(this.graphIndex))
				break;

		// return if there are no enabled profiled generics
		if (firstEnabled == profiledGenerics.size())
			return;

		int samplingInterval = (Integer) NpiInstanceRepository.getInstance()
				.activeUidGetPersistState(
						"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		for (GppSample gs : ((GppTrace) this
				.getTrace()).getSortedGppSamples()) {
			if (!sampleInChart(gs)) {
				continue;
			}

			// for each of the tens of thousands of samples, loop through each
			// of the
			// perhaps thousands of functions, hundreds of binaries, or tens of
			// threads
			// CH: the following is inefficient and needs refactoring (GppTrace
			// has a Vector<ProfiledThread> profiledThreads etc. which are
			// already sorted by index)
			for (int i = firstEnabled; i < profiledGenerics.size(); i++) {
				// find the next enabled profiled generic, if any
				while ((i < profiledGenerics.size() && !((ProfiledGeneric) profiledGenerics
						.get(i)).isEnabled(this.graphIndex)))
					i++;
				if (i >= profiledGenerics.size())
					break;

				ProfiledGeneric pg = (ProfiledGeneric) profiledGenerics.get(i);

				if (((pg instanceof ProfiledThread) && (pg.getIndex() == gs.threadIndex))
						|| ((pg instanceof ProfiledBinary) && (pg.getIndex() == gs.binaryIndex))
						|| ((pg instanceof ProfiledFunction) && (pg.getIndex() == gs.functionIndex))) {
					BarGraphData bgd = new BarGraphData();
					bgd.color = pg.getColor();
					//bgd.x = x;
					bgd.x = (int)gs.sampleSynchTime;
					this.barGraphData.add(bgd);
					break;
				}
			}
			x += samplingInterval;
			
		}

	}

	/**
	 * Returns true if this sample is applicable to this chart. This method is
	 * intended to be overridden, for example for SMP charts where samples
	 * belong to the chart with the matching CPU number.
	 * 
	 * @param gs
	 *            the sample to check
	 * @return true, if sample belongs to chart, false otherwise
	 */
	protected boolean sampleInChart(GppSample gs) {
		return true;
	}

	// /*
	// * Because a table to the left of a function table has changed, update
	// * the function table.
	// * If there is no table to the right of this one, redraw the graph based
	// * on the changed data.
	// */
	// public void refreshProfiledThreadData(int drawMode)
	// {
	// // Must have a table to its left
	// if ( (drawMode != Defines.BINARIES_THREADS)
	// && (drawMode != Defines.BINARIES_THREADS_FUNCTIONS)
	// && (drawMode != Defines.BINARIES_FUNCTIONS_THREADS)
	// && (drawMode != Defines.FUNCTIONS_THREADS)
	// && (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
	// && (drawMode != Defines.FUNCTIONS_BINARIES_THREADS)
	// )
	// {
	//	        System.out.println(Messages.getString("GppTraceGraph.wrongDrawMode"));  //$NON-NLS-1$
	// return;
	// }
	//
	// // boolean to use inside loops (should trust a compiler to optimize this
	// out of the loop...)
	// boolean basedOnBinaries = (drawMode == Defines.BINARIES_THREADS)
	// || (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)
	// || (drawMode == Defines.FUNCTIONS_BINARIES_THREADS);
	//
	// Hashtable<String,ProfiledThread> profiledThreads = new
	// Hashtable<String,ProfiledThread>();
	//		
	// GenericSampledTrace trace = (GenericSampledTrace)this.getTrace();
	// int granularityValue = trace.samples.size() >
	// GppTraceGraph.GRANULARITY_VALUE ? GppTraceGraph.GRANULARITY_VALUE :
	// trace.samples.size();
	//		
	// String[] selectedItems;
	// int[] selectedFunctionHashCodes = null;
	// int[] selectedBinaryHashCodes = null;
	// int count = 0;
	// int timeStamp = 0;
	// int stepValue = granularityValue;
	//		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
	// boolean exit = false;
	//		
	// Hashtable<ProfiledThread,Integer> percentages = new
	// Hashtable<ProfiledThread,Integer>();
	// PIVisualSharedData shared = this.getSharedDataInstance();
	//		
	// if (basedOnBinaries)
	// {
	// selectedItems = shared.GPP_SelectedBinaryNames;
	// if (selectedItems == null)
	// {
	// selectedItems = new String[0];
	// }
	// int[] tmpHashCodes = new int[selectedItems.length];
	// for (int i = 0; i < selectedItems.length; i++)
	// {
	// String tmp = selectedItems[i];
	// tmpHashCodes[i] = tmp.hashCode();
	// }
	// selectedBinaryHashCodes = tmpHashCodes;
	// }
	// else
	// {
	// selectedItems = shared.GPP_SelectedFunctionNames;
	// if (selectedItems == null)
	// {
	// selectedItems = new String[0];
	// }
	// int[] tmpHashCodes = new int[selectedItems.length];
	// for (int i = 0; i < selectedItems.length; i++)
	// {
	// String tmp = selectedItems[i];
	// tmpHashCodes[i] = tmp.hashCode();
	// }
	// selectedFunctionHashCodes = tmpHashCodes;
	// }
	//		
	// for (Enumeration enumer = trace.getSamples(); !exit;)
	// {
	// exit = !enumer.hasMoreElements();
	// if (exit)
	// {
	// // for the final samples, modify the step value
	// // so that they will also be included
	// // now there are no new samples, so proceed directly to
	// // adding the final values to the percent list
	// stepValue = count;
	// }
	// else
	// {
	// count++;
	// int compareValue = 0;
	// boolean match = false;
	// GppSample sample = (GppSample)enumer.nextElement();
	// if (basedOnBinaries)
	// {
	// compareValue = GppTraceGraphUtil.getBinaryName(sample).hashCode();
	// for (int i = 0; i < selectedBinaryHashCodes.length; i++)
	// {
	// if (compareValue == selectedBinaryHashCodes[i])
	// {
	// match = true;
	// break;
	// }
	// }
	// }
	// else
	// {
	// compareValue = GppTraceGraphUtil.getFunctionName(sample).hashCode();
	// for (int i = 0; i < selectedFunctionHashCodes.length; i++)
	// {
	// if (compareValue == selectedFunctionHashCodes[i])
	// {
	// match = true;
	// break;
	// }
	// }
	// }
	//			    
	// if (match)
	// {
	// ProfiledThread pt = null;
	// String name = sample.thread.threadName;
	// if (profiledThreads.containsKey(name))
	// {
	// pt = profiledThreads.get(name);
	// }
	//				
	// if (pt == null)
	// {
	// pt = new ProfiledThread();
	//					
	// pt.setNameString(name);
	// pt.setColor(((GppTrace)this.getTrace()).getThreadColorPalette().getColor(name));
	// pt.setThreadId(sample.thread.threadId.intValue());
	//						
	// pt.setActivityMarkCount((trace.samples.size() + granularityValue) /
	// granularityValue + 1);
	// for (int i = 0; i < timeStamp + stepValue * samplingInterval; i +=
	// stepValue * samplingInterval)
	// {
	// pt.zeroActivityMarkValues(i);
	// }
	// pt.setEnabled(this.graphIndex, true);
	// profiledThreads.put(name, pt);
	// }
	//	
	// if (percentages.containsKey(pt))
	// {
	// Integer value = percentages.get(pt);
	// value = Integer.valueOf(value.intValue()+1);
	// percentages.remove(pt);
	// percentages.put(pt,value);
	// }
	// else
	// {
	// percentages.put(pt,Integer.valueOf(1));
	// }
	// }
	// }
	//
	// if (stepValue != 0 && count == stepValue)
	// {
	// Vector<ProfiledGeneric> v = new
	// Vector<ProfiledGeneric>(profiledThreads.values());
	// Enumeration<ProfiledGeneric> pfEnum = v.elements();
	// while (pfEnum.hasMoreElements())
	// {
	// ProfiledThread updatePt = (ProfiledThread)pfEnum.nextElement();
	// if (percentages.containsKey(updatePt))
	// {
	// int samples = ((percentages.get(updatePt))).intValue();
	// int finalPerc = (samples * 100) / stepValue;
	// updatePt.addActivityMarkValues(timeStamp + stepValue * samplingInterval,
	// finalPerc, samples);
	// }
	// else
	// {
	// updatePt.zeroActivityMarkValues(timeStamp + stepValue *
	// samplingInterval);
	// }
	// }
	//				
	// percentages.clear();
	// count = 0;
	// timeStamp += stepValue * samplingInterval;
	// }
	// }
	//
	// this.threadTable.getTable().deselectAll();
	// this.threadTable.updateProfiledAndItemData(true);
	// this.threadTable.getTable().redraw();
	//
	// // if this is not the last table, set the selected names to set up
	// // the next table
	// if ( (drawMode == Defines.BINARIES_THREADS_FUNCTIONS)
	// || (drawMode == Defines.FUNCTIONS_THREADS_BINARIES))
	// {
	// this.threadTable.setSelectedNames();
	// }
	// else
	// {
	// // This may not be needed needed
	// shared.GPP_SelectedThreadNames = new String[0];
	// }
	// }
	//
	// /*
	// * Because a table to the left of a binary table has changed, update
	// * the binary table.
	// * If there is no table to the right of this one, redraw the graph based
	// * on the changed data.
	// */
	// public void refreshProfiledBinaryData(int drawMode)
	// {
	// // Must have a table to its left
	// if ( (drawMode != Defines.THREADS_BINARIES)
	// && (drawMode != Defines.THREADS_BINARIES_FUNCTIONS)
	// && (drawMode != Defines.THREADS_FUNCTIONS_BINARIES)
	// && (drawMode != Defines.FUNCTIONS_BINARIES)
	// && (drawMode != Defines.FUNCTIONS_BINARIES_THREADS)
	// && (drawMode != Defines.FUNCTIONS_THREADS_BINARIES)
	// )
	// {
	//	        System.out.println(Messages.getString("GppTraceGraph.wrongDrawMode"));  //$NON-NLS-1$
	// return;
	// }
	//
	// // boolean to use inside loops (never trust a compiler...)
	// boolean basedOnThreads = (drawMode == Defines.THREADS_BINARIES)
	// || (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
	// || (drawMode == Defines.FUNCTIONS_THREADS_BINARIES);
	//
	// Hashtable<String,ProfiledBinary> profiledBinaries = new
	// Hashtable<String,ProfiledBinary>();
	//		
	// GenericSampledTrace trace = (GenericSampledTrace)this.getTrace();
	// int granularityValue = trace.samples.size() >
	// GppTraceGraph.GRANULARITY_VALUE ? GppTraceGraph.GRANULARITY_VALUE :
	// trace.samples.size();
	//
	// String[] selectedItems;
	// int[] selectedThreadIds = null;
	// int[] selectedFunctionHashCodes = null;
	// int count = 0;
	// int timeStamp = 0;
	// int stepValue = granularityValue;
	//		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
	// boolean exit = false;
	//		
	// Hashtable<ProfiledBinary,Integer> percentages = new
	// Hashtable<ProfiledBinary,Integer>();
	// PIVisualSharedData shared = this.getSharedDataInstance();
	//		
	// if (basedOnThreads)
	// {
	// selectedItems = shared.GPP_SelectedThreadNames;
	// if (selectedItems == null)
	// {
	// selectedItems = new String[0];
	// }
	// int[] tmpThreadIds = new int[selectedItems.length];
	// for (int i = 0; i < selectedItems.length; i++)
	// {
	// String tmp = selectedItems[i].substring(selectedItems[i].lastIndexOf('_')
	// + 1,
	// selectedItems[i].length());
	// tmpThreadIds[i] = Integer.parseInt(tmp);
	// }
	// selectedThreadIds = tmpThreadIds;
	// }
	// else
	// {
	// selectedItems = shared.GPP_SelectedFunctionNames;
	// if (selectedItems == null)
	// {
	// selectedItems = new String[0];
	// }
	// int[] tmpHashCodes = new int[selectedItems.length];
	// for (int i = 0; i < selectedItems.length; i++)
	// {
	// String tmp = selectedItems[i];
	// tmpHashCodes[i] = tmp.hashCode();
	// }
	// selectedFunctionHashCodes = tmpHashCodes;
	// }
	//		
	// for (Enumeration enumer = trace.getSamples(); !exit;)
	// {
	// exit = !enumer.hasMoreElements();
	// if (exit)
	// {
	// // for the final samples, modify the step value
	// // so that they will also be included
	// // now there are no new samples, so proceed directly to
	// // adding the final values to the percent list
	// stepValue = count;
	// }
	// else
	// {
	// count++;
	// int compareValue = 0;
	// boolean match = false;
	// GppSample sample = (GppSample)enumer.nextElement();
	// if (basedOnThreads)
	// {
	// compareValue = sample.thread.threadId.intValue();
	// for (int i = 0; i < selectedThreadIds.length; i++)
	// {
	// if (compareValue == selectedThreadIds[i])
	// {
	// match = true;
	// break;
	// }
	// }
	// }
	// else
	// {
	// compareValue = GppTraceGraphUtil.getFunctionName(sample).hashCode();
	// for (int i = 0; i < selectedFunctionHashCodes.length; i++)
	// {
	// if (compareValue == selectedFunctionHashCodes[i])
	// {
	// match = true;
	// break;
	// }
	// }
	// }
	//			    
	// if (match)
	// {
	// ProfiledBinary pb = null;
	// String name = GppTraceGraphUtil.getFunctionName(sample);
	// if (profiledBinaries.containsKey(name))
	// {
	// pb = profiledBinaries.get(name);
	// }
	//				
	// if (pb == null)
	// {
	// pb = new ProfiledBinary();
	//						
	// pb.setNameString(name);
	// pb.setColor(((GppTrace)this.getTrace()).getBinaryColorPalette().getColor(name));
	//						
	// pb.setActivityMarkCount((trace.samples.size() + granularityValue) /
	// granularityValue + 1);
	// for (int i = 0; i < timeStamp + stepValue * samplingInterval; i +=
	// stepValue * samplingInterval)
	// {
	// pb.zeroActivityMarkValues(i);
	// }
	// profiledBinaries.put(name, pb);
	// }
	//	
	// if (percentages.containsKey(pb))
	// {
	// Integer value = percentages.get(pb);
	// value = Integer.valueOf(value.intValue()+1);
	// percentages.remove(pb);
	// percentages.put(pb,value);
	// }
	// else
	// {
	// percentages.put(pb,Integer.valueOf(1));
	// }
	// }
	// }
	//
	// if (stepValue != 0 && count == stepValue)
	// {
	// Vector<ProfiledGeneric> v = new
	// Vector<ProfiledGeneric>(profiledBinaries.values());
	// Enumeration<ProfiledGeneric> pfEnum = v.elements();
	// while (pfEnum.hasMoreElements())
	// {
	// ProfiledFunction updatePf = (ProfiledFunction)pfEnum.nextElement();
	// if (percentages.containsKey(updatePf))
	// {
	// int samples = ((percentages.get(updatePf))).intValue();
	// int finalPerc = (samples * 100) / stepValue;
	// updatePf.addActivityMarkValues(timeStamp + stepValue * samplingInterval,
	// finalPerc, samples);
	// }
	// else
	// {
	// updatePf.zeroActivityMarkValues(timeStamp + stepValue *
	// samplingInterval);
	// }
	// }
	//				
	// percentages.clear();
	// count = 0;
	// timeStamp += stepValue * samplingInterval;
	// }
	// }
	//
	// this.binaryTable.getTable().deselectAll();
	// this.binaryTable.updateProfiledAndItemData(true);
	// this.binaryTable.getTable().redraw();
	//
	// // if this is not the last table, set the selected names to set up
	// // the next table
	// if ( (drawMode == Defines.THREADS_BINARIES_FUNCTIONS)
	// || (drawMode == Defines.FUNCTIONS_BINARIES_THREADS))
	// {
	// this.binaryTable.setSelectedNames();
	// }
	// else
	// {
	// // This may not be needed
	// shared.GPP_SelectedBinaryNames = new String[0];
	// }
	// }

	// public void updateGraph()
	// {
	// if (drawMode == Defines.BINARIES)
	// {
	// vPanel.refreshCumulativeThreadTable();
	// }
	// else if (drawMode == Defines.THREADS)
	// {
	// vPanel.refreshCumulativeThreadTable();
	// }
	//
	// this.repaint();
	// }

	public void updateThreadTablePriorities(
			Hashtable<Integer, String> priorities) {
		this.threadTable.addPriorityColumn(priorities);
	}

	public int getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(int drawMode) {
		if ((drawMode == Defines.THREADS)
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
				|| (drawMode == Defines.FUNCTIONS_BINARIES_THREADS)) {
			if (this.drawMode != drawMode) {
				this.setGraphImageChanged(true);
				this.drawMode = drawMode;
				refreshMode();
				if (this.graphChangeListener != null){
					graphChangeListener.onTitleChange(getTitle());
				}
			}
		} else {
			throw new IllegalArgumentException(Messages
					.getString("GppTraceGraph.unknownDrawMode")); //$NON-NLS-1$
		}
	}

	private void refreshMode() {
		this.vPanel.refreshCumulativeThreadTable();
	}

	public int getUid() {
		return this.uid;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#graphVisibilityChanged(boolean)
	 */
	@Override
	public void graphVisibilityChanged(boolean visible){
		getGppTrace().setLegendVisible(graphIndex, visible, holdTablesComposite);	
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#graphMaximized(boolean)
	 */
	@Override
	public void graphMaximized(boolean value){
		//TODO this needs to be re-implemented probably using setVisible() on all graphs (rather than calling max on one graph)
		getGppTrace().setLegendMaximised(graphIndex, value, holdTablesComposite);	
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#graphMaximized(boolean)
	 */
	public void refreshColoursFromTrace() {
		if (graphIndex == PIPageEditor.THREADS_PAGE) {
			getThreadTable().addColor(Defines.THREADS);
		} else if (graphIndex == PIPageEditor.BINARIES_PAGE) {
			getBinaryTable().addColor(Defines.BINARIES);
		} else if (graphIndex == PIPageEditor.FUNCTIONS_PAGE) {
			getFunctionTable().addColor(Defines.FUNCTIONS);
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#doGetActivityList(com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric)
	 */
	@Override
	protected float[] doGetActivityList(ProfiledGeneric pg) {
		return adapter.getActivityList(pg);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		return String.format(Messages.getString("GppTraceGraph.2"), title, getTranslatedDrawMode()); //$NON-NLS-1$
	}

	private String getTranslatedDrawMode() {
		String s = EMPTY_STRING;

		switch (this.drawMode) {
		case Defines.THREADS: {
			s = Messages.getString("GppTraceGraph.3"); //$NON-NLS-1$
			break;
		}
		case Defines.THREADS_FUNCTIONS: {
			s = Messages.getString("GppTraceGraph.4"); //$NON-NLS-1$
			break;
		}
		case Defines.THREADS_FUNCTIONS_BINARIES: {
			s = Messages.getString("GppTraceGraph.5"); //$NON-NLS-1$
			break;
		}
		case Defines.THREADS_BINARIES: {
			s = Messages.getString("GppTraceGraph.6"); //$NON-NLS-1$
			break;
		}
		case Defines.THREADS_BINARIES_FUNCTIONS: {
			s = Messages.getString("GppTraceGraph.7"); //$NON-NLS-1$
			break;
		}
		case Defines.BINARIES: {
			s = Messages.getString("GppTraceGraph.8"); //$NON-NLS-1$
			break;
		}
		case Defines.BINARIES_THREADS: {
			s = Messages.getString("GppTraceGraph.9"); //$NON-NLS-1$
			break;
		}
		case Defines.BINARIES_THREADS_FUNCTIONS: {
			s = Messages.getString("GppTraceGraph.10"); //$NON-NLS-1$
			break;
		}
		case Defines.BINARIES_FUNCTIONS: {
			s = Messages.getString("GppTraceGraph.11"); //$NON-NLS-1$
			break;
		}
		case Defines.BINARIES_FUNCTIONS_THREADS: {
			s = Messages.getString("GppTraceGraph.12"); //$NON-NLS-1$
			break;
		}
		case Defines.FUNCTIONS: {
			s = Messages.getString("GppTraceGraph.13"); //$NON-NLS-1$
			break;
		}
		case Defines.FUNCTIONS_THREADS: {
			s = Messages.getString("GppTraceGraph.14"); //$NON-NLS-1$
			break;
		}
		case Defines.FUNCTIONS_THREADS_BINARIES: {
			s = Messages.getString("GppTraceGraph.15"); //$NON-NLS-1$
			break;
		}
		case Defines.FUNCTIONS_BINARIES: {
			s = Messages.getString("GppTraceGraph.16"); //$NON-NLS-1$
			break;
		}
		case Defines.FUNCTIONS_BINARIES_THREADS: {
			s = Messages.getString("GppTraceGraph.17"); //$NON-NLS-1$
			break;
		}
		default:
			break;
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#getShortTitle()
	 */
	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	public Action[] addTitleBarMenuItems() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu#getContextHelpId()
	 */
	public String getContextHelpId() {
		return AddressPlugin.getPageHelpContextId(GppTraceUtil.getPageIndex(graphIndex));
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean show) {
		super.setVisible(show); //this sets visibility of the graph component
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#isGraphMinimizedWhenOpened()
	 */
	@Override
	public boolean isGraphMinimizedWhenOpened(){
		// CPU load graph is shown when view is opened
		return false;
	}

	public void addContextMenuItems(Menu menu,
			org.eclipse.swt.events.MouseEvent me) {
		if (getGppTrace() != null && getGppTrace().getCPUCount() > 1){
			new MenuItem(menu, SWT.SEPARATOR);
			
			final boolean isSeparate = this instanceof GppTraceGraphSMP; 

			MenuItem changeViewAction = new MenuItem(menu, SWT.PUSH);
			changeViewAction.setText(isSeparate ? Messages.getString("GppTraceGraph.18") : Messages.getString("GppTraceGraph.19"));  //$NON-NLS-1$ //$NON-NLS-2$
			changeViewAction.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					AddressPlugin.getDefault().receiveSelectionEvent(isSeparate ? AddressPlugin.ACTION_COMBINED_CPU_VIEW : AddressPlugin.ACTION_SEPARATE_CPU_VIEW);
				}
			});			
		}
		
	}
	
}
