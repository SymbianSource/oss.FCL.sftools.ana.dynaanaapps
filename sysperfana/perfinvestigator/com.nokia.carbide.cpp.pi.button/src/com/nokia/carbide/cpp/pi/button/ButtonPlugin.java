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

package com.nokia.carbide.cpp.pi.button;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.button.ui.SwitchBupMapDialog;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.test.IProvideTraceAdditionalInfo;
import com.nokia.carbide.cpp.internal.pi.test.TraceAdditionalInfo;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;


/**
 * The main plugin class to be used in the desktop.
 */
public class ButtonPlugin extends AbstractPiPlugin
			implements ITrace, IViewMenu, IEventListener, IClassReplacer, IVisualizable, IFinalizeTrace, IProvideTraceAdditionalInfo
{

	public static final String PLUGIN_ID = PIPageEditor.PI_ID + ".button";  //$NON-NLS-1$

	// There will be three graphs - one each for editor pages 0, 1, 2
	// This code may assume that page 0 has the threads graph, 1 the binaries, and 2 the functions
	private final static int GRAPH_COUNT = 3;

	//The shared instance.
	private static ButtonPlugin plugin;
	
	private static IPreferenceStore prefsStore;
	
	private static void setPlugin(ButtonPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public ButtonPlugin() {
		super();
		setPlugin(this);
	}

	public Class getTraceClass() {
		return BupTrace.class;
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf("com.nokia.carbide.cpp.pi.button.BupTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.button.BupTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.BupTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.bupTracePlugin.BupTrace") != -1) //$NON-NLS-1$
		{
			return BupTrace.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.button.BupSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.button.BupSample") != -1 )//$NON-NLS-1$
		{
			return BupSample.class;
		}
		else
			return null;
	}
	
	public void initialiseTrace(GenericTrace trace) 
	{
		if (!(trace instanceof BupTrace))
			return;

		BupTrace bupTrace = (BupTrace)trace;
		
		NpiInstanceRepository.getInstance().activeUidAddTrace("com.nokia.carbide.cpp.pi.button", trace); //$NON-NLS-1$
	}

	public String getTraceName() {
		return "Button"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("ButtonPlugin.0"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 7;
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
            BupTraceParser parser;
            parser = new BupTraceParser();
            return parser.parse(file);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
	}

	public MenuManager getViewOptionManager() {
		// current tab
		if (NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button") == null) //$NON-NLS-1$
			return null;	// no trace, so no MenuManager

		Action action;

		Boolean showEvents = Boolean.TRUE;		// by default, show button press events

		// if there is a value associated with the current Analyser tab, then use it
		Object obj = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.button.show"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showEvents = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.button.show", showEvents); //$NON-NLS-1$

		action = new Action(Messages.getString("ButtonPlugin.showEventsAction"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("button_events_on"); //$NON-NLS-1$
				else
					receiveSelectionEvent("button_events_off"); //$NON-NLS-1$
			}
		};
		action.setChecked(showEvents);
		action.setToolTipText(Messages.getString("ButtonPlugin.showEventsTooltip")); //$NON-NLS-1$
		PIPageEditor.currentMenuManager().add(action);

		action = new Action("Apply New Key Press Profile", Action.AS_PUSH_BUTTON) {	//$NON-NLS-1$
			public void run() {
				switchMap();
			}
		};
		action.setChecked(showEvents);
		action.setToolTipText(Messages.getString("ButtonPlugin.applyProfile")); //$NON-NLS-1$
		PIPageEditor.currentMenuManager().add(action);

		return PIPageEditor.currentMenuManager();
	}
	
	public void receiveEvent(String actionString, Event event) 
	{
		if (   actionString.equals("button_events_on")  //$NON-NLS-1$
			|| actionString.equals("button_events_off")	//$NON-NLS-1$
			|| actionString.equals("button_map_switch")) //$NON-NLS-1$
	  	{
			BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$
			
			if (bupTrace == null)
				return;

	  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.THREADS_PAGE)).action(actionString);	
	  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).action(actionString);	
	  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).action(actionString);	
	  	}
	}

	public void receiveSelectionEvent(String actionString) 
	{
		if (actionString == null)
			return;
		
		int currentPage = PIPageEditor.currentPageIndex();

		if (   (currentPage != PIPageEditor.THREADS_PAGE)
			&& (currentPage != PIPageEditor.BINARIES_PAGE)
			&& (currentPage != PIPageEditor.FUNCTIONS_PAGE))
			  return;

		if (actionString.equals("button_events_on")) { //$NON-NLS-1$
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.button.show", true); //$NON-NLS-1$
	  	} else if (actionString.equals("button_events_off")) { //$NON-NLS-1$
	  		NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.button.show", false); //$NON-NLS-1$
	  	} else {
	  		return;
	  	}

		BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$
		
		if (bupTrace == null)
			return;

  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.THREADS_PAGE)).action(actionString);	
  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).action(actionString);	
  		((BupTraceGraph)bupTrace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).action(actionString);	
	}

	public Integer getLastSample(int graphIndex) {
		return null;
	}

	public GraphDrawRequest getDrawRequest(int graphIndex) {
		GraphDrawRequest request = new GraphDrawRequest();
		request.addParentGraph(
				graphIndex,
				"com.nokia.carbide.cpp.pi.address.AddressPlugin", //$NON-NLS-1$
				GraphDrawRequest.DRAW_TO_MOST_IMPORTANT_AVAILABLE,
				1);
		return request;
	}

//	public GenericTraceGraph getTraceGraph() 
//	{
//		return this.trace.getTraceGraph();
//	}

	public GenericTraceGraph getTraceGraph(int graphIndex) 
	{
		BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$
		
		if (bupTrace == null)
			return null;

		return bupTrace.getTraceGraph(graphIndex);
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
	public static ButtonPlugin getDefault() {
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
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.button", path); //$NON-NLS-1$
	}

	public int getGraphCount() {
		return GRAPH_COUNT;
	}

	public int getPageNumber(int graphIndex) {
		// Assumes page 0 has the threads graph, 1 has the binaries, and 2 has the functions
		return graphIndex;
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
	
	/**
	 * Returns the shared preference store of this plugin
	 */
	public static IPreferenceStore getBupPrefsStore(){
		if (prefsStore == null){
			prefsStore = getDefault().getPreferenceStore();
		}
		
		return prefsStore;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IPIPageEditorDispose#runOnDispose()
	 */
	public void runOnDispose() {
		BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$
		if (bupTrace != null) {
			BupEventMapManager.getInstance().releaseMap(bupTrace.getCurrentBupMapInUse());
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace#runOnPartOpened()
	 */
	public void runOnPartOpened() {
		//no-op
	}
	
	public void switchMap() {
		BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$
		IBupEventMapProfile oldProfile = bupTrace.getCurrentBupMapInUse().getProfile();
		SwitchBupMapDialog dialog = new SwitchBupMapDialog(Display.getCurrent().getActiveShell(), oldProfile);
		if (dialog.open() == Window.OK) {
			IBupEventMap oldMap = bupTrace.getCurrentBupMapInUse();
			IBupEventMap newMap = BupEventMapManager.getInstance().captureMap(dialog.getNewProfile());
			if (bupTrace != null) {
				boolean resetAll = dialog.resetAll();
				Vector<GenericSample> samples = bupTrace.samples;
				for (GenericSample genericSample : samples) {
					BupSample sample = (BupSample)genericSample;
					if (resetAll || !sample.isLabelModified()) {
						sample.resetLabelToMapDefault(newMap);
					}
				}
				bupTrace.setCurrentBupMapInUse(newMap);
				AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
				// apply profile change to AnalysisInfoHandler that handling additional info in NPI file lifetime
				setBupMapProfileToInfoHandler(handler, dialog.getNewProfile());
				PIPageEditor.currentPageEditor().setDirty();
				// Use this for sending graph fresh
				receiveEvent("button_map_switch", new Event());	//$NON-NLS-1$
			}
			BupEventMapManager.getInstance().releaseMap(oldMap);
		}
	}
	
	/*
	 * (non-javadoc)
	 * Only Bup Plugin should know about it's own additional info
	 * This class keep around a profile, and handle transfer in and out fo additional info
	 * in one place for consistence.
	 */
	private class BupInfoHandlerAdditionalInfo {
		// index of additional info in NPI file
		private static final transient int BUP_MAP_PROFILE_ID = 0;
		private static final transient int BUP_MAP_SYMBIAN_SDK_ID = 1;
		private static final transient int BUP_MAP_IS_BUILTIN = 2;
		private static final transient int BUP_MAP_IS_WORSPACE = 3;
		
		IBupEventMapProfile profile;
		
		Vector<Object> toAdditionalInfo() {
			
			int maxSize = BUP_MAP_PROFILE_ID;
			maxSize = Math.max(maxSize, BUP_MAP_SYMBIAN_SDK_ID);
			maxSize = Math.max(maxSize, BUP_MAP_IS_WORSPACE);
			maxSize = Math.max(maxSize, BUP_MAP_IS_BUILTIN);
			Vector<Object> result = new Vector<Object>(maxSize + 1);
			result.setSize(maxSize + 1);
			result.setElementAt(profile.getProfileId(), BUP_MAP_PROFILE_ID);
			result.setElementAt(profile.getSDK() != null ? profile.getSDK().getUniqueId() : "", BUP_MAP_SYMBIAN_SDK_ID); //$NON-NLS-1$
			result.setElementAt(Boolean.valueOf(profile.getURI().equals(BupEventMapManager.WORKSPACE_PREF_KEY_MAP_URI)), BUP_MAP_IS_WORSPACE);
			result.setElementAt(Boolean.valueOf(profile.getURI().equals(BupEventMapManager.DEFAULT_PROFILE_URI)), BUP_MAP_IS_BUILTIN);
			
			return result;
		}
		
		void fromAddtionalInfo(Vector<Object> additional_info) {
			if (!(additional_info.get(BUP_MAP_PROFILE_ID) instanceof String) ||
					!(additional_info.get(BUP_MAP_SYMBIAN_SDK_ID) instanceof String) ||
					!(additional_info.get(BUP_MAP_IS_BUILTIN) instanceof Boolean) ||
					!(additional_info.get(BUP_MAP_IS_WORSPACE) instanceof Boolean))
			{
				// did you changed anything in analysisInfoHandlerToAdditonalInfo()?
				// did you changed the data format of addition info?
				GeneralMessages.showErrorMessage(Messages.getString("ButtonPlugin.InternalError")); //$NON-NLS-1$
			}
			ArrayList<IBupEventMapProfile> profiles = new ArrayList<IBupEventMapProfile>();
			if (!additional_info.get(BUP_MAP_SYMBIAN_SDK_ID).equals("")) { //$NON-NLS-1$
				String sdkId = (String) additional_info.get(BUP_MAP_SYMBIAN_SDK_ID);
				ISymbianSDK sdk = SDKCorePlugin.getSDKManager().getSDK(sdkId, true);
				profiles.addAll(BupEventMapManager.getInstance().getProfilesFromSDK(sdk));
			} else if ((Boolean)additional_info.get(BUP_MAP_IS_WORSPACE)) {
				profiles.addAll(BupEventMapManager.getInstance().getProfilesFromWorkspacePref());
			} else if ((Boolean)additional_info.get(BUP_MAP_IS_BUILTIN)) {
				profiles.addAll(BupEventMapManager.getInstance().getProfilesFromBuiltin());
			}
			
			profile = null;
			for (IBupEventMapProfile tempProfile : profiles) {
				if (tempProfile.getProfileId().equals(additional_info.get(BUP_MAP_PROFILE_ID))) {
					profile = tempProfile;
					break;
				}
			}
			if (profile == null) {
				if (additional_info.get(BUP_MAP_PROFILE_ID).equals("")) {	//$NON-NLS-1$
					profile = BupEventMapManager.getInstance().getLegacyProfile();
				} else {
					GeneralMessages.showWarningMessage(Messages.getString("ButtonPlugin.keyMapRemoved")); //$NON-NLS-1$
					profile = BupEventMapManager.getInstance().getPrefSelectedProfile();					
				}
			}
		}
	}

	public void setupInfoHandler(AnalysisInfoHandler handler) {
		ArrayList<IBupEventMapProfile> profiles = new ArrayList<IBupEventMapProfile>();
		if (SampleImporter.getInstance().getBupMapSymbianSDKId() != null &&
			!SampleImporter.getInstance().getBupMapSymbianSDKId().equals("")) { //$NON-NLS-1$
			ISymbianSDK sdk = SDKCorePlugin.getSDKManager().getSDK(SampleImporter.getInstance().getBupMapSymbianSDKId(), true);
			profiles.addAll(BupEventMapManager.getInstance().getProfilesFromSDK(sdk));
		} else if (SampleImporter.getInstance().isBupMapIsWorkspace()) {
			profiles.addAll(BupEventMapManager.getInstance().getProfilesFromWorkspacePref());
		} else if (SampleImporter.getInstance().isBupMapIsBuiltIn()) {
			profiles.addAll(BupEventMapManager.getInstance().getProfilesFromBuiltin());
		}

		for (IBupEventMapProfile profile : profiles) {
			if (profile.getProfileId().equals(SampleImporter.getInstance().getBupMapProfileId())) {
				BupInfoHandlerAdditionalInfo info = (BupInfoHandlerAdditionalInfo) handler.getTraceDefinedInfo(getTraceId());
				if (info == null) {
					info = new BupInfoHandlerAdditionalInfo();
				}
				info.profile = profile;
				handler.setTraceDefinedInfo(getTraceId(), info);
				break;
			}
		}
	}

	public void additionalInfoToAnalysisInfoHandler(Vector<Object> additional_info, AnalysisInfoHandler handler) { 
		BupInfoHandlerAdditionalInfo info = (BupInfoHandlerAdditionalInfo) handler.getTraceDefinedInfo(getTraceId());
		if (info == null) {
			info = new BupInfoHandlerAdditionalInfo();
		}
		info.fromAddtionalInfo(additional_info);
		handler.setTraceDefinedInfo(getTraceId(), info);
	}
	
	public void analysisInfoHandlerToAdditonalInfo(TraceAdditionalInfo info, AnalysisInfoHandler handler) {
		Vector<Object> additional_info = new Vector<Object>();
		Object handlerInfo = handler.getTraceDefinedInfo(getTraceId());
		if (handlerInfo instanceof BupInfoHandlerAdditionalInfo) {
			additional_info = ((BupInfoHandlerAdditionalInfo)handlerInfo).toAdditionalInfo();
		}
		info.addAdditionalInfo(getTraceId(), additional_info);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.test.IProvideTraceAdditionalInfo#InfoHandlerToDisplayString(java.util.Vector)
	 */
	public String InfoHandlerToDisplayString(AnalysisInfoHandler handler) {
		Object info = handler.getTraceDefinedInfo(getTraceId());
		if (info instanceof BupInfoHandlerAdditionalInfo) {
			return Messages.getString("ButtonPlugin.keyPressProfile") +((BupInfoHandlerAdditionalInfo)info).profile.toString(); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * Manipulate additional info among AnalysisInfoHandler during lifetime of loaded NPI file
	 */
	public void setBupMapProfileToInfoHandler(AnalysisInfoHandler handler, IBupEventMapProfile profile) {
		BupInfoHandlerAdditionalInfo info = (BupInfoHandlerAdditionalInfo) handler.getTraceDefinedInfo(getTraceId());
		if (info == null) {
			info = new BupInfoHandlerAdditionalInfo();
		}
		info.profile = profile;
		handler.setTraceDefinedInfo(getTraceId(), info);
	}

	/*
	 * (non-Javadoc)
	 * Read additional info among AnalysisInfoHandler during lifetime of loaded NPI file
	 */
	public IBupEventMapProfile getBupMapProfileFromInfoHandler(AnalysisInfoHandler handler) {
		Object info = handler.getTraceDefinedInfo(getTraceId());
		if (info != null && info instanceof BupInfoHandlerAdditionalInfo) {
			return ((BupInfoHandlerAdditionalInfo)info).profile;
		}
		GeneralMessages.showWarningMessage(Messages.getString("ButtonPlugin.keyMapRemoved")); //$NON-NLS-1$
		return BupEventMapManager.getInstance().getPrefSelectedProfile();
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
