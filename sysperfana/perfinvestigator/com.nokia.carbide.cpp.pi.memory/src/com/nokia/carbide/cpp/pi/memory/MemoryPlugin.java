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
import com.nokia.carbide.cpp.internal.pi.memory.actions.MemoryStatisticsDialog;
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
public class MemoryPlugin extends AbstractPiPlugin
	implements IViewMenu, ITrace, IClassReplacer, IVisualizable, IEventListener, IReportable
{
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".memory";  //$NON-NLS-1$
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".memoryPageContext";  //$NON-NLS-1$
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.memory";

	// There will be 1 graph for editor page 0
	// This code may assume that page 0 has the threads graph
	private final static int GRAPH_COUNT = 3;

	//The shared instance.
	private static MemoryPlugin plugin;
	
	// version number of profiler
	private String profilerVersion = ""; //$NON-NLS-1$
	
	private static void setPlugin(MemoryPlugin newPlugin)
	{
		plugin = newPlugin;
	}
	
	/**
	 * The constructor.
	 */
	public MemoryPlugin() {
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
	public static MemoryPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Update menu items
	 */
	public void updateMenuItems(){
		int uid = NpiInstanceRepository.getInstance().activeUid();
		ProfileReader.getInstance().setTraceMenus(NpiInstanceRepository.getInstance().getPlugins(uid), uid);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path); //$NON-NLS-1$
	}

	public Class getTraceClass() 
	{
		return MemTrace.class;
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf(PLUGIN_ID+".MemTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.memory.MemTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.MemTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.memTracePlugin.MemTrace") != -1) //$NON-NLS-1$
		{
			return MemTrace.class;
		}
		else if (   className.indexOf(PLUGIN_ID+".MemSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.memory.MemSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.MemSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.memTracePlugin.MemSample") != -1) //$NON-NLS-1$
		{
			return MemSample.class;
		}
		else if (   className.indexOf("[L"+PLUGIN_ID+".MemThread") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lcom.nokia.carbide.pi.memory.MemThread") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lfi.vtt.bappea.model.MemThread;") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lfi.vtt.bappea.memTracePlugin.MemThread") != -1) //$NON-NLS-1$
		{
			return MemThread[].class;
		}
		else if (   className.indexOf(PLUGIN_ID+".MemThread") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.memory.MemThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.MemThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.memTracePlugin.MemThread") != -1) //$NON-NLS-1$
		{
			return MemThread.class;
		}
		else
			return null;
	}

	public void initialiseTrace(GenericTrace trace) 
	{
		if (!(trace instanceof MemTrace))
			return;

		MemTrace memTrace = (MemTrace)trace;
		
		/*if(!profilerVersion.equalsIgnoreCase("")) {
	        // set version, needed by mem trace graph to support event based trace data
			// convert version number from string to double
			int versionNumber = convertVersionStringToInt(profilerVersion);
			memTrace.setVersion(versionNumber);
		} */
		
		NpiInstanceRepository.getInstance().activeUidAddTrace(PLUGIN_ID, trace); //$NON-NLS-1$

		memTrace.gatherDrawData();
		
	  	System.out.println(Messages.getString("MemoryPlugin.traceProcessed")); //$NON-NLS-1$
	}

	public String getTraceName() {
		return "Memory"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("MemoryPlugin.1"); //$NON-NLS-1$
	}
	
	public int getTraceId() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ParsedTraceData parseTraceFile(File file) throws Exception 
	{
		ParsedTraceData traceData = null;
		try
        {
            MemTraceParser memParser = new MemTraceParser();
            traceData = memParser.parse(file);
            
            //the profiler version data is stored in mem trace parser
            //profilerVersion = memParser.getProfilerVersion();
           
            return traceData;
        } catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
	}

	public MenuManager getViewOptionManager() {
		if (NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID) == null) //$NON-NLS-1$
			return null;	// no trace, so no MenuManager
		
		boolean showChunk  = true;
		boolean showHeapStack = true;
		
		// if there is a showChunk value associated with the current Analyser tab, then use it
		Object obj;
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(PLUGIN_ID+".showChunk");	//$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showChunk = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showChunk", showChunk);	//$NON-NLS-1$
		
		// if there is a showHeapStack value associated with the current Analyser tab, then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(PLUGIN_ID+".showHeapStack"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showHeapStack = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showHeapStack", showHeapStack); //$NON-NLS-1$
	
		Action action;
		
		MenuManager manager = new MenuManager(Messages.getString("MemoryPlugin.memoryGraph")); //$NON-NLS-1$

		action = new Action(Messages.getString("MemoryPlugin.memoryStats"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				new MemoryStatisticsDialog(Display.getCurrent());
			}
		};
		
		action.setToolTipText(Messages.getString("MemoryPlugin.memoryStatsTooltip")); //$NON-NLS-1$
		manager.add(action);
		
		manager.add(new Separator());

		action = new Action(Messages.getString("MemoryPlugin.showChunks"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("chunk_on"); //$NON-NLS-1$
			}
		};
		action.setChecked(showChunk && !showHeapStack);
		action.setToolTipText(Messages.getString("MemoryPlugin.showChunksTooltip")); //$NON-NLS-1$
		manager.add(action);

		action = new Action(Messages.getString("MemoryPlugin.showHeapStack"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("heapstack_on");  //$NON-NLS-1$
			}
		};
		action.setChecked(showHeapStack && !showChunk);
		action.setToolTipText(Messages.getString("MemoryPlugin.showHeapStackTooltip")); //$NON-NLS-1$
		manager.add(action);

		action = new Action(Messages.getString("MemoryPlugin.showAll"), Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("chunk_heapstack_on"); //$NON-NLS-1$
			}
		};
		action.setChecked(showChunk && showHeapStack);
		action.setToolTipText(Messages.getString("MemoryPlugin.showAllTooltip")); //$NON-NLS-1$
		manager.add(action);

		manager.add(new Separator());

		boolean rescale = false;
		
		// if there is a rescale value associated with the current Analyser tab, then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(PLUGIN_ID+".rescale"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			rescale = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".rescale", rescale); //$NON-NLS-1$
		
		action = new Action(Messages.getString("MemoryPlugin.dynamicRescale"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("rescale_on"); //$NON-NLS-1$
				else
					receiveSelectionEvent("rescale_off"); //$NON-NLS-1$
			}
		};
		action.setChecked(rescale);
		action.setToolTipText(Messages.getString("MemoryPlugin.dynamicRescaleTooltip")); //$NON-NLS-1$
		manager.add(action);
		
		
		boolean showMemoryUsageLine = true;
		// if there is a show memory usage value associated with the current Analyser tab, then use it		
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(PLUGIN_ID+".showMemoryUsage");	//$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showMemoryUsageLine = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showMemoryUsage", showMemoryUsageLine);	//$NON-NLS-1$
		
		action = new Action(Messages.getString("MemTraceGraph.showTotalMemoryUsage"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("memory_usage_line_on"); //$NON-NLS-1$
				else
					receiveSelectionEvent("memory_usage_line_off"); //$NON-NLS-1$
			}
		};
		action.setChecked(showMemoryUsageLine);
		action.setToolTipText(Messages.getString("MemoryPlugin.showTotalMemoryUsageToolTip")); //$NON-NLS-1$
		manager.add(action);

		return manager;
	}
	

	public void receiveEvent(String actionString, Event event) {
		MemTrace trace = (MemTrace)NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID); //$NON-NLS-1$

		if (trace == null)
			return;

		if (   actionString.equals("chunk_on")  //$NON-NLS-1$
			|| actionString.equals("heapstack_on")  //$NON-NLS-1$
			|| actionString.equals("chunk_heapstack_on")  //$NON-NLS-1$
			|| actionString.equals("rescale_on")  //$NON-NLS-1$
			|| actionString.equals("rescale_off")) //$NON-NLS-1$
		{
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).action(actionString);
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).action(actionString);
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).action(actionString);
		} else if (actionString.equals("scroll")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(event, PIEvent.SCROLLED);
			
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
			((MemTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
		}
	}

	public void receiveSelectionEvent(String actionString) 
	{
		if (actionString == null)
			return;

		if (actionString.equals("chunk_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showChunk", true); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showHeapStack", false); //$NON-NLS-1$
		} else if (actionString.equals("heapstack_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showChunk", false); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showHeapStack", true); //$NON-NLS-1$
		} else if (actionString.equals("chunk_heapstack_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showChunk", true); //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showHeapStack", true); //$NON-NLS-1$
		} else if (actionString.equals("rescale_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".rescale", true); //$NON-NLS-1$
		} else if (actionString.equals("rescale_off")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".rescale", false); //$NON-NLS-1$
		} else if (actionString.equals("memory_usage_line_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showMemoryUsage", true); //$NON-NLS-1$
		} else if (actionString.equals("memory_usage_line_off")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState(PLUGIN_ID+".showMemoryUsage", false); //$NON-NLS-1$
		}else {
			return;
		}

		((MemTraceGraph)this.getTraceGraph(PIPageEditor.THREADS_PAGE)).action(actionString);
		((MemTraceGraph)this.getTraceGraph(PIPageEditor.BINARIES_PAGE)).action(actionString);
		((MemTraceGraph)this.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).action(actionString);
	}

//	public GenericTraceGraph getTraceGraph() {
//		MemTrace trace = (MemTrace) PIPageEditor.currentTab().getData("com.nokia.carbide.cpp.pi.memory.trace"); //$NON-NLS-1$
//
//		if (trace != null)
//			return trace.getTraceGraph();
//		else
//			return null;
//	}

	public GenericTraceGraph getTraceGraph(int graphIndex) {
		MemTrace trace = (MemTrace)NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID); //$NON-NLS-1$

		if (trace != null)
			return trace.getTraceGraph(graphIndex);
		else
			return null;
	}

	public Integer getLastSample(int graphIndex) {
		MemTrace trace = (MemTrace)NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID); //$NON-NLS-1$

		if (trace != null)
			return Integer.valueOf(trace.getLastSampleNumber());
		else
			return null;
	}

	public Hashtable<Integer,Object> getSummaryTable(double start, double end) 
	{
//		MemTrace trace = (MemTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.memory"); //$NON-NLS-1$
//		return ((MemTraceGraph)trace.getTraceGraph(PIPageEditor.currentPageIndex(),PIPageEditor.currentTab())).getTableData();
		return null;
	}

	public String getGeneralInfo() {
		return null;
	}

	public ArrayList<String> getColumnNames() 
	{
		ArrayList<String> names = new ArrayList<String>();
		names.add(Messages.getString("MemoryPlugin.namesThread")); //$NON-NLS-1$
		names.add(Messages.getString("MemoryPlugin.namesAvgChunk")); //$NON-NLS-1$
		names.add(Messages.getString("MemoryPlugin.namesAvgStack")); //$NON-NLS-1$
		names.add(Messages.getString("MemoryPlugin.namesTotal")); //$NON-NLS-1$
		return names;
	}
	
	public ArrayList<Boolean> getColumnSortTypes() 
	{
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
		// Assumes page 0 has the threads graph, 1 has the binaries, and 2 has the functions
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
