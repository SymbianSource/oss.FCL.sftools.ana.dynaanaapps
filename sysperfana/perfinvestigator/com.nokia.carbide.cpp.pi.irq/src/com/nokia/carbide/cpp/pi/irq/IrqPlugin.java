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

import java.io.File;
import org.osgi.framework.BundleContext;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * The activator class controls the plug-in life cycle
 */
public class IrqPlugin extends AbstractPiPlugin implements ITrace,
		IClassReplacer, IVisualizable {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.irq"; //$NON-NLS-1$

	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".irq";  //$NON-NLS-1$
	/** context help id of the main page */
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".irqPageContext";  //$NON-NLS-1$

	private IrqTrace trace = null;

	// There will be 1 graph for editor page 0
	// This code may assume that page 0 has the threads graph
	private final static int GRAPH_COUNT = 3;

	// The plug-in ID

	// The shared instance
	private static IrqPlugin plugin;

	/**
	 * The constructor
	 */
	public IrqPlugin() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static IrqPlugin getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceClass()
	 */
	@SuppressWarnings("unchecked")
	public Class getTraceClass() {
		return IrqTrace.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceId()
	 */
	public int getTraceId() {
		return 6;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceName()
	 */
	public String getTraceName() {
		return Messages.IrqPlugin_0;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.IrqPlugin_5;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#initialiseTrace
	 * (com.nokia.carbide.cpp.internal.pi.model.GenericTrace)
	 */
	public void initialiseTrace(GenericTrace genericTrace) {
		trace = (IrqTrace) genericTrace;
		NpiInstanceRepository.getInstance().activeUidAddTrace(
				"com.nokia.carbide.cpp.pi.irq", trace); //$NON-NLS-1$
		trace.gatherDrawData();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFile(
	 * java.io.File)
	 */
	public ParsedTraceData parseTraceFile(File file) throws Exception {
		IrqTraceParser irqParser;

		irqParser = new IrqTraceParser();
		ParsedTraceData ptd = irqParser.parse(file);
		return ptd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer#
	 * getReplacedClass(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Class getReplacedClass(String className) {
		if (className.indexOf(Messages.IrqPlugin_1) != -1
				|| className.indexOf(Messages.IrqPlugin_2) != -1)

		{
			return IrqTrace.class;
		} else if (className
				.indexOf(Messages.IrqPlugin_3) != -1
				|| className.indexOf(Messages.IrqPlugin_4) != -1) {
			return IrqSample.class;
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#arePagesCreated
	 * ()
	 */
	public boolean arePagesCreated() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#createPage
	 * (int)
	 */
	public ProfileVisualiser createPage(int index) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#
	 * getCreatePageCount()
	 */
	public int getCreatePageCount() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#
	 * getCreatePageIndex(int)
	 */
	public int getCreatePageIndex(int index) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getDrawRequest
	 * (int)
	 */
	public GraphDrawRequest getDrawRequest(int graphIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getGraphCount
	 * ()
	 */
	public int getGraphCount() {
		return GRAPH_COUNT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getLastSample
	 * (int)
	 */
	public Integer getLastSample(int graphIndex) {
		IrqTrace trace = (IrqTrace) NpiInstanceRepository.getInstance()
				.activeUidGetTrace("com.nokia.carbide.cpp.pi.irq"); //$NON-NLS-1$

		if (trace != null)
			return Integer.valueOf(trace.getLastSampleNumber());
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getPageNumber
	 * (int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getTraceGraph
	 * (int)
	 */
	public GenericTraceGraph getTraceGraph(int graphIndex) {
		IrqTrace trace = (IrqTrace) NpiInstanceRepository.getInstance()
				.activeUidGetTrace("com.nokia.carbide.cpp.pi.irq"); //$NON-NLS-1$

		if (trace != null)
			return trace.getTraceGraph(graphIndex);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#setPageIndex
	 * (int, int)
	 */
	public void setPageIndex(int index, int pageIndex) {
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#setPagesCreated
	 * (boolean)
	 */
	public void setPagesCreated(boolean pagesCreated) {
		return;
	}

}
