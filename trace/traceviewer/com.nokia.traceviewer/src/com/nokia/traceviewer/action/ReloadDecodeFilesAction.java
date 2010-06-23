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
 * Reload changed decode files Action
 *
 */
package com.nokia.traceviewer.action;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerUtils;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;

/**
 * Reload changed decode files Action
 */
public final class ReloadDecodeFilesAction extends TraceViewerAction {

	/**
	 * Maximum number of times to try to open the decode file
	 */
	private static final int MAX_TRIES = 5;

	/**
	 * Polling interval in milliseconds
	 */
	private static final int POLLING_INTERVAL = 4000;

	/**
	 * Waiting time for model to be loaded
	 */
	private static final int WAITING_TIME = 50;

	/**
	 * Empty image
	 */
	private static ImageDescriptor emptyImage;

	/**
	 * Exclamation image
	 */
	private static ImageDescriptor exclamationImage;

	/**
	 * Timer to use to check the files
	 */
	private Timer timer;

	/**
	 * Map of component items and last modified times to watch in case of
	 * changes
	 */
	private volatile Map<TraceActivationComponentItem, Long> files;

	/**
	 * Changed components
	 */
	private final List<TraceActivationComponentItem> changedComponents;

	/**
	 * List of missing dictionaries so we only inform about disappearance once
	 */
	private final List<String> missingDictionaries;

	/**
	 * Empty String
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Number of times we have tried to open decode file
	 */
	private int triesToOpenDecodeFile;

	/**
	 * Category for events
	 */
	private final static String EVENT_CATEGORY = Messages
			.getString("ReloadDecodeFilesAction.DictionaryReloaderCategory"); //$NON-NLS-1$

	static {
		URL url = null;
		URL url2 = null;
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/empty.gif"); //$NON-NLS-1$
		emptyImage = ImageDescriptor.createFromURL(url);
		url2 = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/exclamation.gif"); //$NON-NLS-1$
		exclamationImage = ImageDescriptor.createFromURL(url2);
	}

	/**
	 * Constructor
	 */
	ReloadDecodeFilesAction() {
		files = new HashMap<TraceActivationComponentItem, Long>();
		changedComponents = new ArrayList<TraceActivationComponentItem>();
		missingDictionaries = new ArrayList<String>();
		changeToEmptyImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.action.TraceViewerAction#doRun()
	 */
	@Override
	protected void doRun() {
		TraceViewerGlobals.postUiEvent("ReloadDecodeFilesButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		reloadFiles(true);

		// Change back to empty image
		changeToEmptyImage();
		TraceViewerGlobals.postUiEvent("ReloadDecodeFilesButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Reload files
	 * 
	 * @param showProgressBar
	 *            if true, show progressbar while reloading
	 */
	private void reloadFiles(boolean showProgressBar) {

		// Start copying the files from the array
		List<String> filesToLoad = new ArrayList<String>();

		// Copy the files from the map to a String array and remove components
		// from the model
		for (int i = 0; i < changedComponents.size(); i++) {
			TraceActivationComponentItem comp = changedComponents.get(i);
			File file = new File(comp.getFilePath());

			if (file.exists()) {
				filesToLoad.add(file.getAbsolutePath());

				// Remove component from the model
				TraceViewerGlobals.getDecodeProvider()
						.removeComponentFromModel(comp.getId());

				// Insert info about reloading to trace events
				String msg = Messages
						.getString("ReloadDecodeFilesAction.ReloadDescriptionMsg"); //$NON-NLS-1$

				msg += TraceViewerUtils.constructTimeString();
				TraceViewerGlobals.postInfoEvent(msg, EVENT_CATEGORY, file
						.getAbsolutePath());

				// File doesn't exist
			} else {
				// Insert info about disappeared Dictionary
				String msg = Messages
						.getString("ReloadDecodeFilesAction.DictionaryDisappearedMsg"); //$NON-NLS-1$

				TraceViewerGlobals.postInfoEvent(msg, EVENT_CATEGORY, file
						.getAbsolutePath());
			}
		}

		changedComponents.clear();

		// Show progressbar
		if (showProgressBar) {

			// Load the new files
			OpenDecodeFileAction openAction = (OpenDecodeFileAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getOpenDecodeFileAction();
			openAction.loadFilesToModel(getStringArrayFromList(filesToLoad),
					false);

			// Don't show progressbar
		} else {

			// Go through the files
			for (int i = 0; i < filesToLoad.size(); i++) {

				// Open the decode file
				TraceViewerGlobals.getDecodeProvider().openDecodeFile(
						filesToLoad.get(i), null, false);
			}
			updateFilesToBeWatched();

		}
	}

	/**
	 * Changes image to empty
	 */
	private void changeToEmptyImage() {
		setImageDescriptor(emptyImage);
		setText(EMPTY);
		setToolTipText(EMPTY);
		setEnabled(false);
	}

	/**
	 * Changes image to exclamation
	 */
	private void changeToExclamationImage() {
		setImageDescriptor(exclamationImage);
		setText(Messages.getString("ReloadDecodeFilesAction.Title")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ReloadDecodeFilesAction.Tooltip")); //$NON-NLS-1$
		setEnabled(true);
	}

	/**
	 * Update files to be watched from the model
	 */
	public void updateFilesToBeWatched() {
		try {
			if (timer == null) {

				// Create the timer as a daemon
				timer = new Timer(true);
				timer.schedule(new FileMonitorNotifier(), POLLING_INTERVAL,
						POLLING_INTERVAL);
			}

			// Remove old watched files
			files.clear();

			// Wait for a while
			Thread.sleep(WAITING_TIME);

			List<TraceActivationComponentItem> components = TraceViewerGlobals
					.getDecodeProvider().getActivationInformation(false);

			// Add files to the list. If they already exist, update
			for (int i = 0; i < components.size(); i++) {
				TraceActivationComponentItem comp = components.get(i);
				File file = new File(comp.getFilePath());
				if (file.exists()) {
					files.put(comp, Long.valueOf((file.lastModified())));
				} else {
					files.put(comp, Long.valueOf(-1));
				}
			}

			// Set activation dialog to changed
			TraceActivationAction activationAction = (TraceActivationAction) TraceViewerGlobals
					.getTraceViewer().getView().getActionFactory()
					.getTraceActivationAction();
			if (activationAction.getDialog() != null) {
				activationAction.getDialog().setModelChanged(true);
			}
			triesToOpenDecodeFile = 0;

		} catch (Exception e) {
			e.printStackTrace();

			// Something went wrong, try again
			if (triesToOpenDecodeFile < MAX_TRIES) {
				triesToOpenDecodeFile++;
				updateFilesToBeWatched();
			} else {
				triesToOpenDecodeFile = 0;
			}
		}
	}

	/**
	 * Stops file monitor
	 */
	public void stopFileMonitor() {
		if (timer != null) {
			timer.cancel();
		}
	}

	/**
	 * This is the timer thread which is executed every n milliseconds according
	 * to the setting of the file monitor. It investigates the file in question
	 * and notify listeners if changed.
	 * 
	 */
	private class FileMonitorNotifier extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {

			// Check if Dictionaries should be auto-reloaded
			boolean autoReload = TraceViewerPlugin
					.getDefault()
					.getPreferenceStore()
					.getBoolean(
							PreferenceConstants.AUTO_RELOAD_DICTIONARIES_CHECKBOX);

			// Loop over the registered files and see which have changed.
			for (Iterator<TraceActivationComponentItem> i = files.keySet()
					.iterator(); i.hasNext();) {
				TraceActivationComponentItem comp = i.next();
				File file = new File(comp.getFilePath());
				String filePath = file.getAbsolutePath();
				long lastModifiedTime = files.get(comp).longValue();
				boolean fileExists = false;
				boolean fileMissingForTheFirstTime = false;

				long newModifiedTime = -1;
				if (file.exists()) {
					fileExists = true;
					newModifiedTime = file.lastModified();

					// If file was previously missing, remove it from the
					// missing list
					if (missingDictionaries.contains(filePath)) {
						missingDictionaries.remove(filePath);
					}

					// File doesn't exist and it's not yet added to the missing
					// Dictionaries list
				} else {
					if (!missingDictionaries.contains(filePath)) {
						missingDictionaries.add(filePath);
						fileMissingForTheFirstTime = true;
					}
				}

				// Check if file has changed
				if (newModifiedTime != lastModifiedTime) {

					if (!autoReload && fileExists) {
						changeToExclamationImage();
					}

					// File exists or file is missing for the first time and we
					// are using Auto-reload, let's add the component to changed
					// list
					if (fileExists
							|| (autoReload && fileMissingForTheFirstTime)) {

						// Add to changed components
						if (!changedComponents.contains(comp)) {
							changedComponents.add(comp);
						}

					}
				}
			}

			// Reload automatically. Must be synced with UI thread
			if (autoReload && !changedComponents.isEmpty()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {
							public void run() {

								// Re-check to be sure
								if (!changedComponents.isEmpty()) {
									reloadFiles(false);
								}
							}
						});
			}
		}
	}

	/**
	 * Gets string array from list
	 * 
	 * @param list
	 *            list
	 * @return string array
	 */
	private String[] getStringArrayFromList(List<String> list) {
		String[] arr = new String[list.size()];

		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}

		return arr;
	}
}