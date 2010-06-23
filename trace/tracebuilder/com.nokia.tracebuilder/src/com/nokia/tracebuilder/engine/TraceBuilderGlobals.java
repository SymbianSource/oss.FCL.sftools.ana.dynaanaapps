/*
* Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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
* Access point to the exported interfaces of Trace Builder engine
*
*/
package com.nokia.tracebuilder.engine;

import java.util.Iterator;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.plugin.TraceBuilderPlugin;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;

/**
 * Access point to the exported interfaces of Trace Builder engine
 * 
 */
public class TraceBuilderGlobals {

	/**
	 * Max trace ID
	 */
	public static final int MAX_TRACE_ID = 65535; // CodForChk_Dis_Magic

	/**
	 * Trace builder instance
	 */
	private static TraceBuilder instance;

	/**
	 * Constructor is hidden
	 */
	private TraceBuilderGlobals() {
	}

	/**
	 * Starts Trace Builder engine.
	 */
	public static void start() {
		if (instance == null) {
			instance = new TraceBuilder();
			instance.start();
		}
	}

	/**
	 * Shuts down the TraceBuilder instance
	 */
	public static void shutdown() {
		if (instance != null) {
			instance.shutdown();
			instance = null;
		}
	}

	/**
	 * Gets the configuration interface. The configuration interface is not
	 * available until view has been registered.
	 * 
	 * @return the configuration
	 */
	public static TraceBuilderConfiguration getConfiguration() {
		return instance.getConfiguration();
	}

	/**
	 * Gets the trace model
	 * 
	 * @return the model
	 */
	public static TraceModel getTraceModel() {
		return instance.getModel();
	}

	/**
	 * Gets the source engine
	 * 
	 * @return the source engine
	 */
	public static SourceEngine getSourceEngine() {
		return instance.getSourceEngine();
	}

	/**
	 * Gets the trace builder interface
	 * 
	 * @return trace builder
	 */
	public static TraceBuilderInterface getTraceBuilder() {
		return instance.getTraceBuilder();
	}

	/**
	 * Gets the dialogs interface
	 * 
	 * @return the dialogs interface
	 */
	public static TraceBuilderDialogs getDialogs() {
		return instance.getDialogs();
	}

	/**
	 * Gets the events interface
	 * 
	 * @return the events interface
	 */
	public static TraceBuilderEvents getEvents() {
		return instance.getEvents();
	}

	/**
	 * Called by a plug-in to register itself
	 * 
	 * @param plugin
	 *            the plugin to be registered
	 */
	public static void registerPlugin(TraceBuilderPlugin plugin) {
		instance.registerPlugin(plugin);
	}

	/**
	 * Called by a plug-in to unregister itself
	 * 
	 * @param plugin
	 *            the plugin to be unregistered
	 */
	public static void unregisterPlugin(TraceBuilderPlugin plugin) {
		if (instance != null) {
			instance.unregisterPlugin(plugin);
		}
	}

	/**
	 * Called by the view plug-in to register the view
	 * 
	 * @param view
	 *            the view
	 */
	public static void setView(TraceBuilderView view) {
		instance.setView(view);
	}

	/**
	 * Is view registered
	 * 
	 * @return true if view is registered false if view is not registered
	 */
	public static boolean isViewRegistered() {
		return instance.isViewRegistered();
	}

	/**
	 * Runs an asynchronous operation. Asynchronous operations are not available
	 * until view has been registered
	 * 
	 * @param runner
	 *            the operation to run
	 */
	public static void runAsyncOperation(Runnable runner) {
		instance.runAsyncOperation(runner);
	}

	/**
	 * Gets the source context manager
	 * 
	 * @return the context manager
	 */
	public static SourceContextManager getSourceContextManager() {
		return instance.getSourceContextManager();
	}

	/**
	 * Returns the actions interface. The actions interface is not available
	 * until view has been registered
	 * 
	 * @return the factory
	 */
	public static TraceBuilderActions getActions() {
		return instance.getActions();
	}

	/**
	 * Returns the view
	 * 
	 * @return the view
	 */
	public static TraceBuilderView getView() {
		return instance.getView();
	}

	/**
	 * Set current software component index
	 * 
	 * @param currentSoftwareComponentIndex
	 *            Current component index
	 */
	public static void setCurrentSoftwareComponentIndex(
			int currentSoftwareComponentIndex) {
		instance
				.setCurrentSoftwareComponentIndex(currentSoftwareComponentIndex);
	}

	/**
	 * Get current software component Id
	 * 
	 * @return the software component Id as string
	 */
	public static String getCurrentSoftwareComponentId() {
		String softwareComponentId = instance.getCurrentSoftwareComponentId();
		return softwareComponentId;
	}

	/**
	 * Get current software component name
	 * 
	 * @return the current software component name as string
	 */
	public static String getCurrentSoftwareComponentName() {
		String softwareComponentName = instance
				.getCurrentSoftwareComponentName();
		return softwareComponentName;
	}

	/**
	 * Add software component
	 * 
	 * @param softwareComponentId
	 *            software component Id
	 * @param softwareComponentName
	 *            software component name
	 * @param mmpPath
	 *            software component's mmp path
	 */
	public static void addSoftwareComponent(String softwareComponentId,
			String softwareComponentName, String mmpPath) {
		instance.addSoftwareComponent(softwareComponentId,
				softwareComponentName, mmpPath);
	}

	/**
	 * Clear software components
	 */
	public static void clearSoftwareComponents() {
		instance.clearSoftwareComponents();
	}

	/**
	 * Gets the software components
	 * 
	 * @return the software components iterator
	 */
	public static Iterator<SoftwareComponent> getSoftwareComponents() {
		Iterator<SoftwareComponent> softwareComponentIterator = instance
				.getSoftwareComponents();
		return softwareComponentIterator;
	}

	/**
	 * Get current software component's MMP path
	 * 
	 * @return the current software component's MMP path
	 */
	public static String getCurrentSoftwareComponentMMPPath() {
		String mmpPath = instance.getCurrentSoftwareComponentMMPPath();
		return mmpPath;
	}

	/**
	 * Get current software component index
	 * 
	 * @return current software component index
	 */
	public static int getCurrentSoftwareComponentIndex() {
		int index = instance.getCurrentSoftwareComponentIndex();
		return index;
	}

	/**
	 * Get previous software component name
	 * 
	 * @return previous software component name
	 */
	public static String getPreviousSoftwareComponentName() {
		String componentName = instance.getPreviousSoftwareComponentName();
		return componentName;
	}

	/**
	 * Get project path
	 * 
	 * @return project path
	 */
	public static String getProjectPath() {
		String projetcPath = instance.getProjectPath();
		return projetcPath;
	}

	/**
	 * Set project path
	 * 
	 * @param path
	 *            the path
	 */
	public static void setProjectPath(String path) {
		instance.setProjectPath(path);
	}

	/**
	 * Gets the name for the trace header file based on given source
	 * 
	 * @param sourceFile
	 *            the source file name
	 * @return the header file name
	 */
	public static String getHeaderFileName(String sourceFile) {
		String retval = instance.getHeaderFileName(sourceFile);
		return retval;
	}

	/**
	 * Set project monitor
	 * 
	 * @param projectMonitor
	 *            the project monitor
	 */
	public static void setProjectMonitor(TraceProjectMonitorInterface projectMonitor) {
		instance.setProjectMonitor(projectMonitor);
	}

	/**
	 * Get location converter
	 * 
	 * @return the location converter
	 */
	public static TraceLocationConverter getLocationConverter() {
		return instance.getLocationConverter();
	}

	/**
	 * Get location map
	 * 
	 * @return the location map
	 */
	public static TraceLocationMap getLocationMap() {
		return instance.getLocationMap();
	}
	
	/**
	 * Get group name handler
	 * 
	 * @return the group name handler
	 */
	public static GroupNameHandlerBase getGroupNameHandler() {
		return instance.getGroupNameHandler();
	}
	
	/**
	 * Set group name handler
	 * 
	 */
	public static void setGroupNameHandler(GroupNameHandlerBase groupNameHandler) {
		instance.setGroupNameHandler(groupNameHandler);
	}
}
