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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileReader;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IReportable;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * The main plugin class to be used in the desktop.
 */
public class GraphicsMemoryPlugin extends AbstractPiPlugin implements
		IViewMenu, ITrace, IClassReplacer, IVisualizable, IEventListener,
		IReportable {
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID
			+ ".graphicsmemory"; //$NON-NLS-1$
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID
			+ ".graphicsMemoryPageContext"; //$NON-NLS-1$

	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.graphicsmemory"; //$NON-NLS-1$

	// There will be 1 graph for editor page 0
	// This code may assume that page 0 has the threads graph
	private final static int GRAPH_COUNT = 3;

	// The shared instance.
	private static GraphicsMemoryPlugin plugin;

	private static void setPlugin(GraphicsMemoryPlugin newPlugin) {
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public GraphicsMemoryPlugin() {
		super();
		setPlugin(this);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		setPlugin(null);
	}

	/**
	 * Returns the shared instance.
	 */
	public static GraphicsMemoryPlugin getDefault() {
		return plugin;
	}

	/**
	 * Update menu items
	 */
	public void updateMenuItems() {
		int uid = NpiInstanceRepository.getInstance().activeUid();
		ProfileReader.getInstance().setTraceMenus(
				NpiInstanceRepository.getInstance().getPlugins(uid), uid);
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin(
				GraphicsMemoryPlugin.PLUGIN_ID, path); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	public Class getTraceClass() {
		return GraphicsMemoryTrace.class;
	}

	@SuppressWarnings("unchecked")
	public Class getReplacedClass(String className) {
		if (className.indexOf(GraphicsMemoryPlugin.PLUGIN_ID
				+ ".GraphicsMemoryTrace") != -1)//$NON-NLS-1$
		{
			return GraphicsMemoryTrace.class;
		} else if (className.indexOf(GraphicsMemoryPlugin.PLUGIN_ID
				+ ".GraphicsMemorySample") != -1)//$NON-NLS-1$
		{
			return GraphicsMemorySample.class;
		} else if (className
				.indexOf("[L" + GraphicsMemoryPlugin.PLUGIN_ID + ".GraphicsMemoryProcess") != -1)//$NON-NLS-1$ //$NON-NLS-2$
		{
			return GraphicsMemoryProcess[].class;
		} else if (className.indexOf(GraphicsMemoryPlugin.PLUGIN_ID
				+ ".GraphicsMemoryProcess") != -1)//$NON-NLS-1$
		{
			return GraphicsMemoryProcess.class;
		} else
			return null;
	}

	public void initialiseTrace(GenericTrace trace) {
		if (!(trace instanceof GraphicsMemoryTrace))
			return;

		GraphicsMemoryTrace memTrace = (GraphicsMemoryTrace) trace;

		NpiInstanceRepository.getInstance().activeUidAddTrace(
				GraphicsMemoryPlugin.PLUGIN_ID, trace); //$NON-NLS-1$

		memTrace.gatherDrawData();

		System.out.println(Messages
				.getString("GraphicsMemoryPlugin.traceProcessed")); //$NON-NLS-1$
	}

	public String getTraceName() {
		return "GraphicsMemory"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("GraphicsMemoryPlugin.1"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 14;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles
	 * (java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ParsedTraceData parseTraceFile(File file) throws Exception {
		ParsedTraceData traceData = null;
		try {
			GraphicsMemoryTraceParser memParser = new GraphicsMemoryTraceParser();
			traceData = memParser.parse(file);
			return traceData;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public MenuManager getViewOptionManager() {
		if (NpiInstanceRepository.getInstance().activeUidGetTrace(
				GraphicsMemoryPlugin.PLUGIN_ID) == null) //$NON-NLS-1$
			return null; // no trace, so no MenuManager

		boolean showPrivate = true;
		boolean showShared = true;

		// if there is a showPrivate value associated with the current Analyser
		// tab, then use it
		Object obj;
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showPrivate = (Boolean) obj;
		else
			// set the initial value
			NpiInstanceRepository
					.getInstance()
					.activeUidSetPersistState(
							GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", showPrivate); //$NON-NLS-1$

		// if there is a showShared value associated with the current
		// Analyser tab, then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				GraphicsMemoryPlugin.PLUGIN_ID + ".showShared"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showShared = (Boolean) obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", showShared); //$NON-NLS-1$

		Action action;

		MenuManager manager = new MenuManager(Messages
				.getString("GraphicsMemoryPlugin.graphicsMemoryGraph")); //$NON-NLS-1$

		action = new Action(
				Messages.getString("GraphicsMemoryPlugin.memoryStats"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				new GraphicsMemoryStatisticsDialog(Display.getCurrent());
			}
		};

		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.memoryStatsTooltip")); //$NON-NLS-1$
		manager.add(action);

		manager.add(new Separator());

		action = new Action(
				Messages.getString("GraphicsMemoryPlugin.showPrivate"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("private_on"); //$NON-NLS-1$
			}
		};
		action.setChecked(showPrivate && !showShared);
		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.showPrivateTooltip")); //$NON-NLS-1$
		manager.add(action);

		action = new Action(
				Messages.getString("GraphicsMemoryPlugin.showShared"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("shared_on"); //$NON-NLS-1$
			}
		};
		action.setChecked(showShared && !showPrivate);
		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.showSharedTooltip")); //$NON-NLS-1$
		manager.add(action);

		action = new Action(
				Messages.getString("GraphicsMemoryPlugin.showAll"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("private_shared_on"); //$NON-NLS-1$
			}
		};
		action.setChecked(showPrivate && showShared);
		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.showAllTooltip")); //$NON-NLS-1$
		manager.add(action);

		manager.add(new Separator());

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

		action = new Action(
				Messages.getString("GraphicsMemoryPlugin.dynamicRescale"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("rescale_on"); //$NON-NLS-1$
				else
					receiveSelectionEvent("rescale_off"); //$NON-NLS-1$
			}
		};
		action.setChecked(rescale);
		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.dynamicRescaleTooltip")); //$NON-NLS-1$
		manager.add(action);

		boolean showMemoryUsageLine = true;
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

		action = new Action(
				Messages
						.getString("GraphicsMemoryTraceGraph.showTotalMemoryUsage"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("memory_usage_line_on"); //$NON-NLS-1$
				else
					receiveSelectionEvent("memory_usage_line_off"); //$NON-NLS-1$
			}
		};
		action.setChecked(showMemoryUsageLine);
		action.setToolTipText(Messages
				.getString("GraphicsMemoryPlugin.showTotalMemoryUsageToolTip")); //$NON-NLS-1$
		manager.add(action);

		return manager;
	}

	public void receiveEvent(String actionString, Event event) {
		GraphicsMemoryTrace trace = (GraphicsMemoryTrace) NpiInstanceRepository
				.getInstance()
				.activeUidGetTrace(GraphicsMemoryPlugin.PLUGIN_ID); //$NON-NLS-1$

		if (trace == null)
			return;

		if (actionString.equals("private_on") //$NON-NLS-1$
				|| actionString.equals("shared_on") //$NON-NLS-1$
				|| actionString.equals("private_shared_on") //$NON-NLS-1$
				|| actionString.equals("rescale_on") //$NON-NLS-1$
				|| actionString.equals("rescale_off")) //$NON-NLS-1$
		{
			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.THREADS_PAGE))
					.action(actionString);
			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.BINARIES_PAGE))
					.action(actionString);
			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE))
					.action(actionString);
		} else if (actionString.equals("scroll")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(event, PIEvent.SCROLLED);

			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.THREADS_PAGE))
					.piEventReceived(be);
			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.BINARIES_PAGE))
					.piEventReceived(be);
			((GraphicsMemoryTraceGraph) trace
					.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE))
					.piEventReceived(be);
		}
	}

	public void receiveSelectionEvent(String actionString) {
		if (actionString == null)
			return;

		if (actionString.equals("private_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", true); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", false); //$NON-NLS-1$
		} else if (actionString.equals("shared_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", false); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", true); //$NON-NLS-1$
		} else if (actionString.equals("private_shared_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showPrivate", true); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showShared", true); //$NON-NLS-1$
		} else if (actionString.equals("rescale_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".rescale", true); //$NON-NLS-1$
		} else if (actionString.equals("rescale_off")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".rescale", false); //$NON-NLS-1$
		} else if (actionString.equals("memory_usage_line_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showMemoryUsage", true); //$NON-NLS-1$
		} else if (actionString.equals("memory_usage_line_off")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					GraphicsMemoryPlugin.PLUGIN_ID + ".showMemoryUsage", false); //$NON-NLS-1$
		} else {
			return;
		}

		((GraphicsMemoryTraceGraph) this
				.getTraceGraph(PIPageEditor.THREADS_PAGE)).action(actionString);
		((GraphicsMemoryTraceGraph) this
				.getTraceGraph(PIPageEditor.BINARIES_PAGE))
				.action(actionString);
		((GraphicsMemoryTraceGraph) this
				.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE))
				.action(actionString);
	}

	public GenericTraceGraph getTraceGraph(int graphIndex) {
		GraphicsMemoryTrace trace = (GraphicsMemoryTrace) NpiInstanceRepository
				.getInstance()
				.activeUidGetTrace(GraphicsMemoryPlugin.PLUGIN_ID); //$NON-NLS-1$

		if (trace != null)
			return trace.getTraceGraph(graphIndex);
		else
			return null;
	}

	public Integer getLastSample(int graphIndex) {
		GraphicsMemoryTrace trace = (GraphicsMemoryTrace) NpiInstanceRepository
				.getInstance()
				.activeUidGetTrace(GraphicsMemoryPlugin.PLUGIN_ID); //$NON-NLS-1$

		if (trace != null)
			return Integer.valueOf(trace.getLastSampleNumber());
		else
			return null;
	}

	public Hashtable<Integer, Object> getSummaryTable(double start, double end) {
		return null;
	}

	public String getGeneralInfo() {
		return null;
	}

	public ArrayList<String> getColumnNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add(Messages.getString("GraphicsMemoryPlugin.namesProcess")); //$NON-NLS-1$
		names.add(Messages.getString("GraphicsMemoryPlugin.namesAvgPrivate")); //$NON-NLS-1$
		names.add(Messages.getString("GraphicsMemoryPlugin.namesAvgShared")); //$NON-NLS-1$
		names.add(Messages.getString("GraphicsMemoryPlugin.namesTotal")); //$NON-NLS-1$
		return names;
	}

	public ArrayList<Boolean> getColumnSortTypes() {
		ArrayList<Boolean> sortTypes = new ArrayList<Boolean>();
		sortTypes.add(SORT_BY_NAME);
		sortTypes.add(SORT_BY_NUMBER);
		sortTypes.add(SORT_BY_NUMBER);
		sortTypes.add(SORT_BY_NUMBER);
		return sortTypes;
	}

	public String getActiveInfo(Object arg0, double startTime, double endTime) {
		return null;
	}

	public MenuManager getReportGeneratorManager() {
		return null;
	}

	public GraphDrawRequest getDrawRequest(int graphIndex) {
		return null;
	}

	public int getGraphCount() {
		return GRAPH_COUNT;
	}

	public int getPageNumber(int graphIndex) {
		// Assumes page 0 has the threads graph, 1 has the binaries, and 2 has
		// the functions
		if (graphIndex == 0)
			return PIPageEditor.THREADS_PAGE;
		else if (graphIndex == 1)
			return PIPageEditor.BINARIES_PAGE;
		else if (graphIndex == 2)
			return PIPageEditor.FUNCTIONS_PAGE;

		return PIPageEditor.NEXT_AVAILABLE_PAGE;
	}

	// return whether this plugin's editor pages have been created
	public boolean arePagesCreated() {
		return false;
	}

	public void setPagesCreated(boolean pagesCreated) {
		return;
	}

	public int getCreatePageCount() {
		return 0;
	}

	public int getCreatePageIndex(int index) {
		return 0;
	}

	public ProfileVisualiser createPage(int index) {
		return null;
	}

	public void setPageIndex(int index, int pageIndex) {
		return;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#isMandatory()
	 */
	public boolean isMandatory() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceDescription()
	 */
	public String getTraceDescription() {
		return getTraceTitle();
	}
}
