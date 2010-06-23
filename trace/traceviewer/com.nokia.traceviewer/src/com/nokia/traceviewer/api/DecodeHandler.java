/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Decode handler handles stuff related to decode files
 *
 */
package com.nokia.traceviewer.api;

import java.io.File;
import java.util.ArrayList;

import com.nokia.traceviewer.action.OpenDecodeFileAction;
import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;

/**
 * Decode handler handles stuff related to decode files
 * 
 */
final class DecodeHandler {

	/**
	 * Model valid waiting time
	 */
	private static final long MODEL_VALID_WAITING_TIME = 100;

	/**
	 * Model valid maximum waiting time
	 */
	private static final long MODEL_VALID_MAX_WAITING_TIME = 5000;

	/**
	 * Decode file not loaded error code
	 */
	private static final int DECODE_FILE_NOT_LOADED = -2;

	/**
	 * Gets trace component name from the Decode model with a component id
	 * 
	 * @param componentId
	 *            component ID
	 * @return component name or null if not found or no Decode files are loaded
	 */
	public String getTraceComponentName(int componentId) {
		String componentName = null;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			if (TraceViewerGlobals.getDecodeProvider().isModelLoadedAndValid()) {

				// Returns null if the component is not found
				componentName = TraceViewerGlobals.getDecodeProvider()
						.getComponentName(componentId);
			}
		}

		return componentName;
	}

	/**
	 * Gets trace group ID with a group name
	 * 
	 * @param componentId
	 *            the component ID
	 * @param groupName
	 *            the group name
	 * @return trace group ID or -1 if group ID not found or -2 if no Decode
	 *         files are loaded
	 */
	public int getTraceGroupId(int componentId, String groupName) {
		int groupId = DECODE_FILE_NOT_LOADED;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			if (TraceViewerGlobals.getDecodeProvider().isModelLoadedAndValid()) {

				// Returns -1 if the group is not found
				groupId = TraceViewerGlobals.getDecodeProvider().getGroupId(
						componentId, groupName);
			}
		}

		return groupId;
	}

	/**
	 * Gets trace group name from the Decode model with a component and group
	 * ids
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @return group name or null if not found or no Decode files are loaded
	 */
	public String getTraceGroupName(int componentId, int groupId) {
		String groupName = null;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			if (TraceViewerGlobals.getDecodeProvider().isModelLoadedAndValid()) {

				// Returns null if the component is not found
				groupName = TraceViewerGlobals.getDecodeProvider()
						.getGroupName(componentId, groupId);
			}
		}

		return groupName;
	}

	/**
	 * Gets trace name from the Decode model with a component, group and trace
	 * IDs
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @param traceId
	 *            trace ID
	 * @return trace name or null if not found or no Decode files are loaded
	 */
	public String getTraceName(int componentId, int groupId, int traceId) {
		String traceName = null;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			if (TraceViewerGlobals.getDecodeProvider().isModelLoadedAndValid()) {

				// Returns null if the component is not found
				traceName = TraceViewerGlobals.getDecodeProvider()
						.getTraceName(componentId, groupId, traceId);
			}
		}

		return traceName;
	}

	/**
	 * Loads decode file to the decode model. User must remember that loading
	 * the decode file can take some time and this function will block until the
	 * model is loaded or a maximum of 5 seconds.
	 * 
	 * @param decodeFilePath
	 *            absolute path to the decode file. Path must be in correct
	 *            format for any operating system (Windows, Linux)
	 * @param deleteExistingModel
	 *            if true, old decode model is removed before this decode file
	 *            is loaded
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError loadDecodeFile(String decodeFilePath,
			boolean deleteExistingModel) {
		TVAPIError errorCode = TVAPIError.DECODE_PROVIDER_PLUGIN_MISSING;

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			errorCode = TVAPIError.NONE;
			File file = new File(decodeFilePath);

			// Check that file exists
			if (file.exists()) {

				// TraceViewer view is visible, use it
				if (TraceViewerGlobals.getTraceViewer().getView() != null
						&& !TraceViewerGlobals.getTraceViewer().getView()
								.isDisposed()) {

					OpenDecodeFileAction action = (OpenDecodeFileAction) TraceViewerGlobals
							.getTraceViewer().getView().getActionFactory()
							.getOpenDecodeFileAction();
					action.loadFilesToModel(new String[] { decodeFilePath },
							deleteExistingModel);

					// No TraceViewer view available, use API
				} else {
					// Open the decode file
					TraceViewerGlobals.getDecodeProvider().openDecodeFile(
							decodeFilePath, null, deleteExistingModel);

					// Wait until model is ready and loaded or maximum of five
					// seconds
					long startTime = System.currentTimeMillis();
					while (!TraceViewerGlobals.getDecodeProvider()
							.isModelLoadedAndValid()
							&& startTime + MODEL_VALID_MAX_WAITING_TIME > System
									.currentTimeMillis()) {
						try {
							Thread.sleep(MODEL_VALID_WAITING_TIME);
						} catch (InterruptedException e) {
						}
					}

					// Refresh the view
					refreshViewAfterDecodeFileLoad();
				}

				// File doesn't exist
			} else {
				errorCode = TVAPIError.FILE_DOES_NOT_EXIST;
			}
		}

		return errorCode;
	}

	/**
	 * Refresh the view after Decode file has been loaded
	 */
	private void refreshViewAfterDecodeFileLoad() {

		// Refresh the view
		if (TraceViewerGlobals.getTraceViewer().getView() != null) {

			// Check if reading from the start is needed
			if (TraceViewerUtils.isReadingFromStartNeeded()) {
				TraceViewerGlobals.getTraceViewer().readDataFileFromBeginning();

				// Refresh the current view
			} else {
				TraceViewerGlobals.getTraceViewer().getView()
						.refreshCurrentView();
			}
		}
	}

	/**
	 * Gets components from all loaded Dictionaries
	 * 
	 * @return list of components from all loaded Dictionaries. List can be
	 *         empty.
	 */
	public ArrayList<TraceActivationComponentItem> getDictionaryComponents() {
		ArrayList<TraceActivationComponentItem> components = new ArrayList<TraceActivationComponentItem>();

		if (TraceViewerGlobals.getDecodeProvider() != null) {
			components = TraceViewerGlobals.getDecodeProvider()
					.getActivationInformation(true);
		}

		return components;
	}
}
