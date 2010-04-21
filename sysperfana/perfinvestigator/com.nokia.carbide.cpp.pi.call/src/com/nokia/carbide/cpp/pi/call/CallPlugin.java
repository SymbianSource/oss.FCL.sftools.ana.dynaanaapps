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

package com.nokia.carbide.cpp.pi.call;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.interfaces.IToolBarActionListener;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.call.GfcFunctionItem.GfcFunctionItemData;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


/**
 * The main plugin class to be used in the desktop.
 */
public class CallPlugin extends AbstractPiPlugin
		implements ITrace, IClassReplacer, IVisualizable, IEventListener, IToolBarActionListener
{
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".call";  //$NON-NLS-1$

	// the shared instance
	private static CallPlugin plugin;
	
	// page index assigned to the page displaying call info
	private int pageIndex = PIPageEditor.NEXT_AVAILABLE_PAGE;
	
	// call trace
	private GfcTrace trace;

	// call page
	private ProfileVisualiser profileVisualiser;

	// call table handler
	private CallVisualiser callVisualiser;

	private static void setPlugin(CallPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public CallPlugin() {
		super();
		setPlugin(this);
	}
	
	public Class getTraceClass() 
	{
		return GfcTrace.class;
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf("com.nokia.carbide.cpp.pi.call.GfcTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.call.GfcTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.GfcTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.gfcTracePlugin.GfcTrace") != -1) //$NON-NLS-1$
		{
			return GfcTrace.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.call.GfcSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.call.GfcSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.GfcSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gfcTracePlugin.GfcSample") != -1) //$NON-NLS-1$
		{
			return GfcSample.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.call.GfcFunctionItem$GfcFunctionItemData") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.call.GfcFunctionItem$GfcFunctionItemData") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gfcTracePlugin.GfcFunctionItem$GfcFunctionItemData") != -1) //$NON-NLS-1$
		{
			return GfcFunctionItemData.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.call.GfcFunctionItem") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.call.GfcFunctionItem") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.analyser.GfcFunctionItem") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gfcTracePlugin.GfcFunctionItem") != -1) //$NON-NLS-1$
		{
			return GfcFunctionItem.class;
		}
		else 
		{
			return null;
		}
	}

	public void initialiseTrace(GenericTrace trace) 
	{
		if (!(trace instanceof GfcTrace))
			return;
		
		this.trace = (GfcTrace) trace;
		
		this.trace.samples.trimToSize();
		this.trace.getCompleteGfcTrace().trimToSize();
		
		NpiInstanceRepository.getInstance().activeUidAddTrace("com.nokia.carbide.cpp.pi.call", trace); //$NON-NLS-1$
		
	  	/*
	  	 * Check if the trace is complete (first sample is at time 1,
	  	 * sample N is at time N)
	  	 */
		this.trace.setComplete();
		
		ArrayList<CallVisualiser>    callVisualiserList    = new ArrayList<CallVisualiser>();

		NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.callVisualiserList", callVisualiserList);	//$NON-NLS-1$

		ArrayList<ProfileVisualiser> profileVisualiserList = new ArrayList<ProfileVisualiser>();
		NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.profileVisualiserList", profileVisualiserList);	//$NON-NLS-1$

	}

	public String getTraceName() {
		return "Function Call"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("CallPlugin.1"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 2;
	}

	public ParsedTraceData parseTraceFile(File file /*, ProgressBar progressBar*/) throws Exception 
	{
        GfcTraceParser gfcParser;
        gfcParser = new GfcTraceParser();

        return gfcParser.parse(file);
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
	public static CallPlugin getDefault() {
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
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.call", path); //$NON-NLS-1$
	}

	boolean pagesCreated = false;

	// return whether this plugin's editor pages have been created
	public boolean arePagesCreated() {
		return this.pagesCreated;
	}
	
	// set whether this plugin's editor pages have been created
	public void setPagesCreated(boolean pagesCreated) {
		this.pagesCreated = pagesCreated;
	}

	// number of editor pages to create
	public int getCreatePageCount() {
		return 1;
	}

	// editor page index for each created editor page
	public int getCreatePageIndex(int index) {
		return PIPageEditor.NEXT_AVAILABLE_PAGE;
	}

	// page index actually assigned to a created page
	public void setPageIndex(int index, int pageIndex) {
		if (index == 0)
			this.pageIndex = pageIndex;
	}

	// create the page(s)
	public ProfileVisualiser createPage(int index) {
		Composite parent = NpiInstanceRepository.getInstance().activeUidGetParentComposite();
		if (parent == null) {
			// no parent composite is only for temp instance used by non-GUI importer
			GeneralMessages.showErrorMessage(Messages.getString("CallPlugin.0")); //$NON-NLS-1$
			return null;
		}
		
		// there is one page, with no graph
		this.profileVisualiser = new ProfileVisualiser(ProfileVisualiser.TOP_ONLY, parent,
														Messages.getString("CallPlugin.functionCalls")); //$NON-NLS-1$
		AnalysisInfoHandler infoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
		this.profileVisualiser.getParserRepository().setPIAnalysisInfoHandler(infoHandler);
	    
		PlatformUI.getWorkbench().getHelpSystem().setHelp(profileVisualiser.getContentPane(),
														HELP_CONTEXT_ID + ".functionCallsPageContext"); //$NON-NLS-1$

		PIPageEditor pageEditor = PIPageEditor.currentPageEditor();
		
		this.callVisualiser = new CallVisualiser(pageEditor, pageIndex, profileVisualiser.getTopComposite().getSashForm(), this.trace, this.profileVisualiser.getContentPane());

		Object objCallList = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.callVisualiserList"); //$NON-NLS-1$
		ArrayList<CallVisualiser> callVisualiserList = (ArrayList<CallVisualiser>) objCallList;

		Object objProfileList = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.profileVisualiserList"); //$NON-NLS-1$
		ArrayList<ProfileVisualiser> profileVisualiserList = (ArrayList<ProfileVisualiser>) objProfileList;

		if (callVisualiserList != null)
			callVisualiserList.add(this.callVisualiser);

		if (profileVisualiserList != null)
			profileVisualiserList.add(this.profileVisualiser);

		return this.profileVisualiser;
	}

	// number of graphs supplied by this plugin
	public int getGraphCount() {
		return 0;
	}

	// page number of each graph supplied by this plugin
	public int getPageNumber(int graphIndex) {
		Object objCallList = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.callVisualiserList");	//$NON-NLS-1$
		ArrayList<CallVisualiser> callVisualiserList = (ArrayList<CallVisualiser>) objCallList;
		
		if (callVisualiserList == null)
			return this.pageIndex;

		for (int i = 0; i < callVisualiserList.size(); i++) {
			if (callVisualiserList.get(i).getPageEditor() == PIPageEditor.currentPageEditor())
				return callVisualiserList.get(i).getPageIndex();
		}
		
		return this.pageIndex;
	}

	public GenericTraceGraph getTraceGraph(int graphIndex) {
		return null;
	}

	public Integer getLastSample(int graphIndex) {
		return null;
	}

	public GraphDrawRequest getDrawRequest(int graphIndex) {
		return null;
	}

	public void receiveEvent(String action, Event event) {
		Object objCallList = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.callVisualiserList");	//$NON-NLS-1$
		ArrayList<CallVisualiser> callVisualiserList = (ArrayList<CallVisualiser>) objCallList;

		if (callVisualiserList == null)
			return;
		
		Object objProfileList = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.call.CallPlugin.profileVisualiserList");	//$NON-NLS-1$
		ArrayList<ProfileVisualiser> profileVisualiserList = (ArrayList<ProfileVisualiser>) objProfileList;

		if (profileVisualiserList == null)
			return;
		
		if (action.equals("changeSelection")) { //$NON-NLS-1$
			// find the right callVisualiser and ProfileVisualiser
			for (int i = 0; i < callVisualiserList.size(); i++) {
				if (callVisualiserList.get(i).getPageEditor() == PIPageEditor.currentPageEditor()) {
					callVisualiserList.get(i).setStartAndEnd(event.start, event.end);
					double startTime = PIPageEditor.currentPageEditor().getStartTime();
					double endTime   = PIPageEditor.currentPageEditor().getEndTime();
					profileVisualiserList.get(i).updateStatusBarTimeInterval(startTime, endTime);
				}
			}
		}
	}

	public void setActions(boolean entering, int pageIndex) {
		if (pageIndex != this.pageIndex)
			return;
		
		if (entering) {
			PIPageEditor.getZoomInAction().setEnabled(false);
			PIPageEditor.getZoomOutAction().setEnabled(false);
			PIPageEditor.getZoomToSelectionAction().setEnabled(false);
			PIPageEditor.getZoomToTraceAction().setEnabled(false);
		} else {
			PIPageEditor.getZoomInAction().setEnabled(true);
			PIPageEditor.getZoomOutAction().setEnabled(true);
			PIPageEditor.getZoomToSelectionAction().setEnabled(true);
			PIPageEditor.getZoomToTraceAction().setEnabled(true);
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}
}
