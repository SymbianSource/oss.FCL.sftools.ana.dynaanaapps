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
package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;


public class ProfilerDataPlugins {
	private Map<ITrace, Boolean> pluginsSelectionMap;
	private final IPath profilerDataPath;
	
	/**
	 * Constructor
	 * 
	 * @param profilerDataPath profiler data file
	 * @param plugins list of the plugins 
	 */
	public ProfilerDataPlugins(IPath profilerDataPath, List<ITrace> plugins){
		this.profilerDataPath = profilerDataPath;
		initPlugins(plugins);
	}
	
	/**
	 * Initialize plugins 
	 * 
	 * @param plugins list of the plugins
	 */
	public void initPlugins(List<ITrace> plugins){
		pluginsSelectionMap = new HashMap<ITrace, Boolean>();
		removeDuplicatePriorityPlugin(plugins);
		for(ITrace trace : plugins){
			pluginsSelectionMap.put(trace, true);
		}
	}
	
	/**
	 * Get list of the plugins
	 * 
	 * @return list of the plugins
	 */
	public List<ITrace> getPlugins() {
		List<ITrace> traceList = new ArrayList<ITrace>();
		Iterator<ITrace> iterator = pluginsSelectionMap.keySet().iterator();
		while(iterator.hasNext()){
			traceList.add(iterator.next());
		}		
		return traceList;
	}
	
	/**
	 * Get list of the selected plugins
	 * 
	 * @return list of the selected plugins
	 */
	public List<ITrace> getSelectedPlugins() {
		List<ITrace> traceList = new ArrayList<ITrace>();
		Iterator<ITrace> iterator = pluginsSelectionMap.keySet().iterator();
		while(iterator.hasNext()){
			ITrace trace = iterator.next();
			if(isChecked(trace)){
				traceList.add(trace);
			}			
		}		
		return traceList;
	}
	
	/**
	 * Check whether given trace selected or not
	 * 
	 * @param trace
	 * @return
	 */
	public boolean isChecked(ITrace trace){
		if(pluginsSelectionMap.containsKey(trace)){
			return pluginsSelectionMap.get(trace);
		}
		return false;
	}
	
	/**
	 * Set given selection value for given trace
	 * 
	 * @param trace
	 * @return
	 */
	public void setChecked(ITrace trace, boolean checked){
		if(pluginsSelectionMap.containsKey(trace)){
			pluginsSelectionMap.put(trace, new Boolean(checked));
		}
	}
	
	/**
	 * Check all
	 * 
	 */
	public void checkAll(){
		for(ITrace trace : getPlugins()){
			setChecked(trace, true);
		}
	}
	
	/**
	 * Uncheck all
	 * 
	 */
	public void unCheckAll(){
		for(ITrace trace : getPlugins()){
			if(!isMandatory(trace)){
				setChecked(trace, false);
			}
		}
	}
	
	/**
	 * Check whether given trace mandatory or not
	 * @param trace
	 * @return
	 */
	public boolean isMandatory(ITrace trace){
		if(pluginsSelectionMap.containsKey(trace)){
			if(trace.getTraceId() == 1){
				return true;
			}		
		}
		return false;
	}
		
	/**
	 * Get profiler data file's path
	 * 
	 * @return
	 */
	public IPath getProfilerDataPath() {
		return profilerDataPath;
	}
	
	/**
	 * If both priority plugins are present in the given list, remove the old-style plugin 
	 * @param plugins List of plugins to check
	 */
	private void removeDuplicatePriorityPlugin(List<ITrace> plugins) {
		//There is no elegant way of doing this, we'll just have to hard-code it		
		ITrace plugin = null;
		for (ITrace iTrace : plugins) {
			if (iTrace.getTraceId() == 4 && iTrace.getTraceName().startsWith("P")){ //$NON-NLS-1$
				//old style priority plugin (shares the trace id with the memory plugin)
				plugin = iTrace;
			} else if (iTrace.getTraceId() == 5 && plugin != null){
				plugins.remove(plugin);
				break;
			}
		}
	}
}
