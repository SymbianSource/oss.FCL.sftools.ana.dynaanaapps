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

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import com.nokia.carbide.cpp.internal.pi.manager.PluginRegisterer;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


/*
 * Try to encapsulate data related to each opened NPI file (e.g. AnalysTab and NPI related data) 
 * in one centralized location, instead of tossing around classes within UI/processing code
 */

public class NpiInstanceRepository {
	
	static public final int DISPOSED_UID = -1;
	/** Constant for maximum number of CPUs in an SMP system */
	public static final int MAX_CPU_COUNT = 4;

	/** Constant for persisting the state of Show Combined CPU View in the Address plugin */
	public static final String PERSISTED_SHOW_COMBINED_CPU_VIEW = "com.nokia.carbide.cpp.pi.address.showCombinedCPUView";//$NON-NLS-1$
	
	private class UidObject {
		// this is just for generating UID
	};
	
	static private NpiInstanceRepository instance;
	
	// the UID of current UI
	private static int activeInstance;
	
	// all these maps are maintained by UID of instances
	private static HashMap<Integer, UidObject> uidObjectMap = new HashMap<Integer, UidObject>();
	private static HashMap<Integer, HashMap<String,GenericTrace>> traceCollectionMap = new HashMap<Integer, HashMap<String,GenericTrace>>();
	private static HashMap<Integer, HashMap<String,Object>> persistCollectionMap = new HashMap<Integer, HashMap<String,Object>>();
	private static HashMap<Integer, ArrayList<AbstractPiPlugin>> pluginListMap = new HashMap<Integer, ArrayList<AbstractPiPlugin>>();
	private static HashMap<Integer, ArrayList<ProfileVisualiser>> profilePagesMap = new  HashMap<Integer, ArrayList<ProfileVisualiser>>();
	private static HashMap<Integer, Composite> parentCompositeMap = new HashMap<Integer, Composite>();
	private static HashMap<Integer, AnalysisInfoHandler> analysisInfoHandlerMap = new HashMap<Integer, AnalysisInfoHandler>();

	private NpiInstanceRepository() {
		// singleton
	}

	public static NpiInstanceRepository getInstance() {
		if (instance == null) {
			instance = new NpiInstanceRepository();
		}
		
		return instance;
	}
	
	public int register(Composite composite) {
		UidObject uidObject = new UidObject();
		Integer uid = Integer.valueOf(uidObject.hashCode());
		uidObjectMap.put(uid, uidObject);
		HashMap<String,GenericTrace> traceCollection = new HashMap<String,GenericTrace>();
		traceCollectionMap.put(uid, traceCollection);
		HashMap<String,Object> persistCollection = new HashMap<String,Object>();
		persistCollectionMap.put(uid, persistCollection);
		ArrayList<AbstractPiPlugin> pluginList = new ArrayList<AbstractPiPlugin>();
		pluginListMap.put(uid, pluginList);
		ArrayList<ProfileVisualiser> profilePages = new ArrayList<ProfileVisualiser>();
		profilePagesMap.put(uid, profilePages);
		parentCompositeMap.put(uid, composite);
		AnalysisInfoHandler analysisInfoHandler = new AnalysisInfoHandler();
		analysisInfoHandlerMap.put(uid, analysisInfoHandler);
		TraceDataRepository.getInstance().registerTraces(uid.intValue());
		PluginRegisterer.registerAllPlugins();
		switchActiveUid(uid);
		return uid;	
	}
	
	public void unregister(int instanceUid) {
		Integer uidInteger = Integer.valueOf(instanceUid);
		uidObjectMap.remove(uidInteger);
		HashMap<String,GenericTrace> traceCollection = traceCollectionMap.get(uidInteger);
		if (traceCollection != null){
			traceCollection.clear();
			traceCollectionMap.remove(uidInteger);			
		}
		HashMap<String,Object> persistCollection =  persistCollectionMap.get(uidInteger);
		if (persistCollection != null){
			persistCollection.clear();
			persistCollectionMap.remove(uidInteger);			
		}
		ArrayList<AbstractPiPlugin> pluginList =  pluginListMap.get(uidInteger);
		if (pluginList != null){
			pluginList.clear();
			pluginListMap.remove(uidInteger);			
		}
		ArrayList<ProfileVisualiser> profilePages = profilePagesMap.get(uidInteger);
		if (profilePages != null){
			profilePages.clear();
			profilePagesMap.remove(uidInteger);			
		}
		parentCompositeMap.remove(uidInteger);
		analysisInfoHandlerMap.remove(uidInteger);
		TraceDataRepository.getInstance().removeTraces(instanceUid);
		// In general it's a good idea to keep memory footprint low
		System.runFinalization();
		System.gc();
	}
	
	public int size() {
		return uidObjectMap.size();
	}
	
	public void setParentComposite(int instanceUid, Composite parent) {
		parentCompositeMap.remove(instanceUid);
		parentCompositeMap.put(instanceUid, parent);
	}
	
	public void activeUidAddTrace(String className, GenericTrace trace) {
		int instanceUid = activeUid();
		instance.addTrace(instanceUid, className, trace);
	}

	private void addTrace(int instanceUid, String className, GenericTrace trace) {
		HashMap<String,GenericTrace> newEntry = traceCollectionMap.get(instanceUid);
		newEntry.put(className + ".trace", trace); //$NON-NLS-1$
		traceCollectionMap.put(instanceUid, newEntry);
	}

	public GenericTrace activeUidGetTrace(String className) {
		int instanceUid = activeUid();
		return getTrace(instanceUid, className);
	}
	
	private GenericTrace getTrace(int instanceUid, String className) {
		HashMap<String,GenericTrace> entry = traceCollectionMap.get(instanceUid);
		assertLog(entry != null, Messages.getString("NpiInstanceRepository.0") + instanceUid); //$NON-NLS-1$
		if (entry == null) {
			entry = new HashMap<String,GenericTrace>();
		}
		return entry.get(className + ".trace"); //$NON-NLS-1$
	}

	
	public void activeUidRemoveTrace(String className) {
		int instanceUid = activeUid();
		removeTrace(instanceUid, className);
	}
	
	private void removeTrace(int instanceUid, String className) {
		HashMap<String,GenericTrace> entry = traceCollectionMap.get(instanceUid);
		// if we don't have any opened NPI, the active one == disposed and trace will be
		// harmlessly removed one more time upon exit
		if (instanceUid != DISPOSED_UID) {
			assertLog(entry != null, Messages.getString("NpiInstanceRepository.1") + instanceUid); //$NON-NLS-1$
		}
		if (entry == null) {
			return;
		}
		entry.remove(className);
	}
	
	public void switchActiveUid(int instanceUid) {
		activeInstance = instanceUid;
	}
	
	public int activeUid() {
		return activeInstance;
	}
	
	// return array list of this tab's plugins
	public ArrayList<AbstractPiPlugin> getPlugins(int instanceUid)	{
		ArrayList<AbstractPiPlugin> pluginList = pluginListMap.get(instanceUid);
		assertLog(pluginList != null, Messages.getString("NpiInstanceRepository.2") + instanceUid); //$NON-NLS-1$
		if (pluginList == null) {
			pluginList = new ArrayList<AbstractPiPlugin>();
		}
		return pluginList;
	}
	
	// add the plugin to this tab's list
	public void addPlugin(int instanceUid, AbstractPiPlugin plugin) {
		ArrayList<AbstractPiPlugin> pluginList = pluginListMap.get(instanceUid);
		assertLog(pluginList != null, Messages.getString("NpiInstanceRepository.3") + instanceUid); //$NON-NLS-1$
		if (pluginList != null && !pluginList.contains(plugin))
			pluginList.add(plugin);
	}
	
	// return array list of this tab's pages
	public ArrayList<ProfileVisualiser> activeUidGetProfilePages() {
		int instanceUid = activeUid();
		return getProfilePages(instanceUid);
	}
	
	// return array list of this tab's pages
	private ArrayList<ProfileVisualiser> getProfilePages(int instanceUid) {
		ArrayList<ProfileVisualiser> pages = profilePagesMap.get(instanceUid);
		assertLog(pages != null, Messages.getString("NpiInstanceRepository.4") + instanceUid); //$NON-NLS-1$
		if (pages == null) {
			pages = new ArrayList<ProfileVisualiser>();
		}
		return pages;
	}
	
	// return a particular page
	public ProfileVisualiser getProfilePage(int instanceUid, int index) {
		ArrayList<ProfileVisualiser> pages  = profilePagesMap.get(instanceUid);
		
		assertLog(pages != null, Messages.getString("NpiInstanceRepository.5") + instanceUid); //$NON-NLS-1$
		if (pages == null) {
			pages = new ArrayList<ProfileVisualiser>();
		}
		
		ProfileVisualiser visualizer;
		
		try {
			visualizer = pages.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		
		return visualizer;
	}

	public Composite activeUidGetParentComposite() {
		int instanceUid = activeUid();
		Composite parent = parentCompositeMap.get(instanceUid);
		return parent;
	}
	
	public AnalysisInfoHandler activeUidGetAnalysisInfoHandler() {
		int instanceUid = activeUid();
	    return getAnalysisInfoHandler(instanceUid);
	}

	public AnalysisInfoHandler getAnalysisInfoHandler(int instanceUid) {
		return analysisInfoHandlerMap.get(instanceUid);
	}
	
	public void activeUidSetPersistState (String key, Object state) {
		int instanceUid = activeUid();
		setPersistState(instanceUid, key, state);
	}
	
	public void setPersistState (int instanceUid, String key, Object state) {
		HashMap<String, Object> entry = persistCollectionMap.get(instanceUid);
		entry.put(key, state);
	}
	
	private Object getPersistDefault (String key) {
		if (key.equals("com.nokia.carbide.cpp.pi.address.thresholdCountThread") || //$NON-NLS-1$
				key.equals("com.nokia.carbide.cpp.pi.address.thresholdCountBinary") || //$NON-NLS-1$
				key.equals("com.nokia.carbide.cpp.pi.address.thresholdCountFunction") ) { //$NON-NLS-1$
			return Integer.valueOf(-1);
		} else if (key.equals("com.nokia.carbide.cpp.pi.address.thresholdLoadThread") || //$NON-NLS-1$
				key.equals("com.nokia.carbide.cpp.pi.address.thresholdLoadBinary")) { //$NON-NLS-1$
			return Double.valueOf(0.001);
		} else if (key.equals("com.nokia.carbide.cpp.pi.address.thresholdLoadFunction")) { //$NON-NLS-1$
			return Double.valueOf(0.0001);
		} else if (key.equals("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds")) {	//$NON-NLS-1$
			return Boolean.FALSE;
		} else if (key.equals(PERSISTED_SHOW_COMBINED_CPU_VIEW)){
			return Boolean.TRUE;
		}
		return null;
	}
	
	public Object activeUidGetPersistState (String key) {
		int instanceUid = activeUid();
		return getPersistState(instanceUid, key);
	}
	
	public Object getPersistState (int instanceUid, String key) {
		HashMap<String, Object> entry = persistCollectionMap.get(instanceUid);
		Object object = entry.get(key);
		if (object != null) {
			return object;
		}
		return getPersistDefault(key);
	}
	
	private void assertLog(boolean cond, String message) {
		if (!cond) {
			GeneralMessages.PiLog(message, IStatus.ERROR);
		}
	}
}
