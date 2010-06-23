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

package com.nokia.carbide.cpp.pi.power;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.power.actions.PowerSettingsDialog;
import com.nokia.carbide.cpp.internal.pi.power.actions.PowerStatisticsDialog;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class PowerPlugin extends AbstractPiPlugin
		implements IViewMenu, ITrace, IClassReplacer, IVisualizable, IEventListener
{

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.power"; //$NON-NLS-1$

	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".power";  //$NON-NLS-1$
	/** context help id of the main page */
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".powerPageContext";  //$NON-NLS-1$

	// There will be three graphs - one each for editor pages 0, 1, 2
	// This code may assume that page 0 has the threads graph, 1 the binaries, and 2 the functions
	private final static int GRAPH_COUNT = 3;

	// the shared instance
	private static PowerPlugin plugin;
	
	private static void setPlugin(PowerPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public PowerPlugin() {
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
	public static PowerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.power", path); //$NON-NLS-1$
	}

	public Class getTraceClass()
	{
		return PwrTrace.class;
	}

	public Class getReplacedClass(String className)
	{
		if (   (className.indexOf("com.nokia.carbide.cpp.pi.power.PwrTrace") != -1) //$NON-NLS-1$
			|| (className.indexOf("com.nokia.carbide.pi.power.PwrTrace") != -1) //$NON-NLS-1$
			|| (className.indexOf("fi.vtt.bappea.pwrTracePlugin.PwrTrace") != -1)) //$NON-NLS-1$
		{
			return PwrTrace.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.power.PwrSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.power.PwrSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.pwrTracePlugin.PwrSample") != -1) //$NON-NLS-1$
		{
			return PwrSample.class;
		}
		else
			return null;
	}
	
	public void initialiseTrace(GenericTrace trace) 
	{
		if (!(trace instanceof PwrTrace))
			return;

		PwrTrace pwrTrace = (PwrTrace)trace;
		
		pwrTrace.setComplete();
		
		// because of the way these traces used to be created, the sample vector may be much too large
		pwrTrace.samples.trimToSize();
		
		NpiInstanceRepository.getInstance().activeUidAddTrace("com.nokia.carbide.cpp.pi.power", trace); //$NON-NLS-1$
	}	

	public String getTraceName() {
		return "Power"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("PowerPlugin.0"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 11;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ParsedTraceData parseTraceFile(File file) throws Exception 
	{
		try
        {
            PwrTraceParser pwrParser = new PwrTraceParser();
            return pwrParser.parse(file);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
	}

	public MenuManager getViewOptionManager() {
		if (NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power") == null) //$NON-NLS-1$
			return null;	// no trace, so no MenuManager

		Action action;
		
		MenuManager manager = new MenuManager(Messages.getString("PowerPlugin.powerGraph")); //$NON-NLS-1$

		action = new Action(Messages.getString("PowerPlugin.powerSettingsAction"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				new PowerSettingsDialog(Display.getCurrent());
			}
		};
		
		action.setToolTipText(Messages.getString("PowerPlugin.powerSettingsTooltip")); //$NON-NLS-1$
		manager.add(action);

		action = new Action(Messages.getString("PowerPlugin.powerStatsAction"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				new PowerStatisticsDialog(Display.getCurrent());
			}
		};
		
		action.setToolTipText(Messages.getString("PowerPlugin.PowerStatsTooltip")); //$NON-NLS-1$
		manager.add(action);
		
		manager.add(new Separator());

		Boolean showLine   = Boolean.TRUE;	// by default, show the interval average power as a line

		// if there is are values associated with the current Analyser tab, then use them
		Object obj;
		// do we show any average line?
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.power.showLine"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showLine = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.power.showLine", showLine); //$NON-NLS-1$

		action = new Action(Messages.getString("PowerPlugin.showIntervalLineAction"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("show_average"); //$NON-NLS-1$
				else
					receiveSelectionEvent("hide_average"); //$NON-NLS-1$
			}
		};
		
		action.setChecked(showLine);
		action.setToolTipText(Messages.getString("PowerPlugin.showIntervalLineTooltip")); //$NON-NLS-1$
		manager.add(action);
	
		return manager;
	}
		
	public void receiveEvent(String actionString, Event event) 
	{
		PwrTrace trace = (PwrTrace) NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$

		if (trace == null)
			return;

		int uid = NpiInstanceRepository.getInstance().activeUid();
		if (   actionString.equals("show_average") //$NON-NLS-1$
			|| actionString.equals("hide_average")) //$NON-NLS-1$
		{
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE,   uid)).action(actionString);	
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE,  uid)).action(actionString);	
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE, uid)).action(actionString);	
		} else if (actionString.equals("scroll")) //$NON-NLS-1$
		{			
			PIEvent be = new PIEvent(event, PIEvent.SCROLLED);
			
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE,   uid)).piEventReceived(be);
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE,  uid)).piEventReceived(be);
			((PowerTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE, uid)).piEventReceived(be);
		}
	}

	public void receiveSelectionEvent(String eventString)
	{
		if (eventString == null)
			return;

		PwrTrace trace = (PwrTrace) NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$

		if (eventString.equals("show_average")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.power.showLine", Boolean.TRUE); //$NON-NLS-1$
 		} else if (eventString.equals("hide_average")){ //$NON-NLS-1$
 			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.power.showLine", Boolean.FALSE); //$NON-NLS-1$
		} else {
			return;
		}

		int uid = NpiInstanceRepository.getInstance().activeUid();
    	trace.getPowerGraph(PIPageEditor.THREADS_PAGE,   uid).action(eventString);
    	trace.getPowerGraph(PIPageEditor.BINARIES_PAGE,  uid).action(eventString);
    	trace.getPowerGraph(PIPageEditor.FUNCTIONS_PAGE, uid).action(eventString);
	}

	public GenericTraceGraph getTraceGraph(int graphIndex) 
	{
		PwrTrace trace = (PwrTrace) NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$

		int uid = NpiInstanceRepository.getInstance().activeUid();
		
		if (trace != null)
			return trace.getTraceGraph(graphIndex, uid);
		else
			return null;
	}

	/*
	public GenericTraceGraph getTraceGraph(int graphIndex, int uid) {
		PwrTrace trace = (PwrTrace) NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$

		if (trace != null)
			return trace.getTraceGraph(graphIndex, uid);
		else
			return null;
	}
	*/

	public Integer getLastSample(int graphIndex) {
		PwrTrace trace = (PwrTrace) NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$

		if (trace != null)
			return Integer.valueOf(trace.getLastSampleNumber());
		else
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
	
	// set whether this plugin's editor pages have been created
	public void setPagesCreated(boolean pagesCreated) {
		return;
	}

	// number of editor pages to create
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
