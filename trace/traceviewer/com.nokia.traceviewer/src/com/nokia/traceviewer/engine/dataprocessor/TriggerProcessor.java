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
 * TriggerProcessor DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.util.ArrayList;
import java.util.List;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.TriggerDialog;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeItem;
import com.nokia.traceviewer.dialog.treeitem.TriggerTreeTextItem;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationXMLImporter;
import com.nokia.traceviewer.engine.activation.TraceActivator;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLTriggerConfigurationImporter;

/**
 * Trigger DataProcessor
 * 
 */
public final class TriggerProcessor implements DataProcessor {

	/**
	 * Dataprocessors stop delay
	 */
	private static final int DATAPROCESSOR_STOP_DELAY = 500;

	/**
	 * State of the trigger process enumeration
	 */
	public enum TriggerState {

		/**
		 * Searching for start trigger
		 */
		SEARCHING_FOR_STARTTRIGGER,

		/**
		 * Searching for stop trigger
		 */
		SEARCHING_FOR_STOPTRIGGER
	}

	/**
	 * State of the trigger process
	 */
	private TriggerState triggerState;

	/**
	 * Trigger dialog used in setting rules
	 */
	private TriggerDialog triggerDialog;

	/**
	 * Content provider for the dialog
	 */
	private TreeItemContentProvider contentProvider;

	/**
	 * First visible object in the dialog tree
	 */
	private TreeItem root;

	/**
	 * Start triggers
	 */
	private final List<TriggerTreeTextItem> startTriggers;

	/**
	 * Stop triggers
	 */
	private final List<TriggerTreeTextItem> stopTriggers;

	/**
	 * Activation triggers
	 */
	private final List<TriggerTreeTextItem> activationTriggers;

	/**
	 * Constructor
	 */
	public TriggerProcessor() {
		createInitialTree();
		startTriggers = new ArrayList<TriggerTreeTextItem>();
		stopTriggers = new ArrayList<TriggerTreeTextItem>();
		activationTriggers = new ArrayList<TriggerTreeTextItem>();
	}

	/**
	 * Creates initial tree
	 */
	public void createInitialTree() {
		contentProvider = new TreeItemContentProvider();
		// Create root node
		TreeItem treeRoot = new TriggerTreeBaseItem(contentProvider, null,
				"root", //$NON-NLS-1$
				TriggerTreeItem.Rule.GROUP, null);
		root = new TriggerTreeBaseItem(contentProvider, treeRoot,
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.CONFIGURATION_FILE),
				TriggerTreeItem.Rule.GROUP, null);
		treeRoot.addChild(root);
	}

	/**
	 * Imports trigger rules from configuration file
	 */
	public void importTriggerRules() {
		// Import rules
		XMLTriggerConfigurationImporter importer = new XMLTriggerConfigurationImporter(
				root, TraceViewerPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.CONFIGURATION_FILE),
				true);
		importer.importData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DataProcessor#processData(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		if (isTriggering() && !properties.traceConfiguration.isScrolledTrace()) {

			// Set the trigger State
			if (!startTriggers.isEmpty()) {
				triggerState = TriggerState.SEARCHING_FOR_STARTTRIGGER;
				properties.traceConfiguration.setTriggeredOut(true);
			} else if (!stopTriggers.isEmpty()) {
				triggerState = TriggerState.SEARCHING_FOR_STOPTRIGGER;
				properties.traceConfiguration.setTriggeredOut(false);
			}

			// Searching for activation line
			if (!activationTriggers.isEmpty()) {
				if (containsTrigger(activationTriggers, properties)) {
					generateAndSendActivationMsg(activationTriggers, properties);
				}
			}

			// State machine
			if (triggerState == TriggerState.SEARCHING_FOR_STARTTRIGGER) {

				// Start trigger is found
				if (containsTrigger(startTriggers, properties)) {

					// One of the start triggers has been found, remove all
					// start triggers and change the view state
					startTriggers.clear();
					TraceViewerGlobals.getTraceViewer().getView()
							.updateViewName();

					// Start using trigger file
					long posInFile = TraceViewerGlobals.getTraceViewer()
							.getDataReaderAccess().getMainDataReader()
							.getTracePositionInFile();
					startUsingTriggerFile(posInFile);
				}

			} else if (triggerState == TriggerState.SEARCHING_FOR_STOPTRIGGER) {

				// One of the stop triggers has been found, remove all
				// stop triggers, and pause data reader. Pausing the data reader
				// will also update the view name so it's not necessary here
				if (containsTrigger(stopTriggers, properties)) {
					stopTriggers.clear();
					TraceViewerGlobals.getTraceViewer().getView()
							.getActionFactory().getPauseAction().run();
				}
			}
		}
	}

	/**
	 * Sets TraceViewer to use the new trigger file as main file
	 * 
	 * @param position
	 *            position of the message containing the start trigger
	 */
	private void startUsingTriggerFile(long position) {
		TraceViewerGlobals.getTraceViewer().getFileHandler().closeFile();

		// Shut down data readers
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getMainDataReader().shutdown();
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.deleteScrollReader();

		// Wait for a while for readers to really stop
		try {
			Thread.sleep(DATAPROCESSOR_STOP_DELAY);
		} catch (InterruptedException e) {
		}

		// Set the start of the trace file
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.setFileStartOffset(position);

		// Create new main file
		TraceViewerGlobals.getTraceViewer().getFileHandler().openFile();

		// Start new main data reader
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.createMainDataReader();

		triggerState = TriggerState.SEARCHING_FOR_STOPTRIGGER;
	}

	/**
	 * Tells if this trace hits any of the triggers in the list
	 * 
	 * @param triggers
	 *            trigger list to find from
	 * @param properties
	 *            trace to find
	 * @return true if trace is contained in the trigger array
	 */
	private boolean containsTrigger(List<TriggerTreeTextItem> triggers,
			TraceProperties properties) {
		boolean found = false;

		// Loop through triggers
		for (int i = 0; i < triggers.size(); i++) {
			found = containsTrigger(triggers.get(i), properties);

			if (found) {
				break;
			}
		}

		return found;
	}

	/**
	 * Tells if this trace hits given trigger
	 * 
	 * @param trigger
	 *            trigger rule
	 * @param properties
	 *            trace
	 * @return true if trace hits the given trigger
	 */
	private boolean containsTrigger(TriggerTreeTextItem trigger,
			TraceProperties properties) {
		boolean found = false;
		String triggerStr = trigger.getTextToCompare();

		String traceLine = ""; //$NON-NLS-1$

		// Traces missing
		if (properties.bTraceInformation.isTraceMissing()) {
			traceLine = TraceViewerActionUtils.TRACES_DROPPED_MSG;
		}
		if (properties.traceString != null) {
			traceLine += properties.traceString;
		}

		if (!trigger.isMatchCase()) {
			traceLine = traceLine.toLowerCase();
		}
		if (traceLine.contains(triggerStr)) {
			found = true;
		}
		return found;
	}

	/**
	 * Generate and send activation message
	 * 
	 * @param triggers
	 *            activation triggers
	 * @param trace
	 *            trace properties
	 */
	private void generateAndSendActivationMsg(
			List<TriggerTreeTextItem> triggers, TraceProperties trace) {

		// Go through the whole list as one trace can hit multiple triggers
		for (int i = 0; i < triggers.size(); i++) {
			TriggerTreeTextItem trigger = triggers.get(i);

			// Trigger hits
			if (containsTrigger(trigger, trace)) {
				// Generate activation list
				TraceActivationXMLImporter importer = new TraceActivationXMLImporter(
						trigger.getConfigurationFilePath());

				// Create the list
				List<TraceActivationComponentItem> activationList = new ArrayList<TraceActivationComponentItem>();

				// Fill the list from the importer
				importer.createComponentListFromConfigurationName(
						activationList, trigger.getConfigurationName());

				// Activate
				new TraceActivator().activate(activationList);
			}
		}
	}

	/**
	 * Tells are we start triggering
	 * 
	 * @return true if start triggering
	 */
	public boolean isStartTriggering() {
		boolean isStartTriggering = !startTriggers.isEmpty();
		return isStartTriggering;
	}

	/**
	 * Tells are we stop triggering
	 * 
	 * @return true if stop triggering
	 */
	public boolean isStopTriggering() {
		boolean isStopTriggering = !stopTriggers.isEmpty();
		return isStopTriggering;
	}

	/**
	 * Tells are we triggering
	 * 
	 * @return true if triggering
	 */
	public boolean isTriggering() {
		boolean isTriggering = !startTriggers.isEmpty()
				|| !stopTriggers.isEmpty() || !activationTriggers.isEmpty();
		return isTriggering;
	}

	/**
	 * Gets trigger dialog
	 * 
	 * @return Trigger dialog
	 */
	public TriggerDialog getTriggerDialog() {
		if (triggerDialog == null) {
			triggerDialog = (TriggerDialog) TraceViewerGlobals.getTraceViewer()
					.getDialogs().createDialog(Dialog.TRIGGER);
		}
		return triggerDialog;
	}

	/**
	 * Gets root node of the tree
	 * 
	 * @return root node of the tree
	 */
	public TreeItem getRoot() {
		return root;
	}

	/**
	 * Gets item listener
	 * 
	 * @return the contentProvider
	 */
	public TreeItemListener getTreeItemListener() {
		return contentProvider;
	}

	/**
	 * Removes triggers
	 */
	public void removeTriggers() {
		startTriggers.clear();
		stopTriggers.clear();
	}

	/**
	 * Gets start triggers
	 * 
	 * @return the startTriggers
	 */
	public List<TriggerTreeTextItem> getStartTriggers() {
		return startTriggers;
	}

	/**
	 * Gets stop triggers
	 * 
	 * @return the stopTriggers
	 */
	public List<TriggerTreeTextItem> getStopTriggers() {
		return stopTriggers;
	}

	/**
	 * Gets activation triggers
	 * 
	 * @return the activationTriggers
	 */
	public List<TriggerTreeTextItem> getActivationTriggers() {
		return activationTriggers;
	}

	/**
	 * Enable variabletracing rule
	 * 
	 * @param item
	 *            the rule item
	 */
	public void enableRule(TriggerTreeItem item) {
		if (item instanceof TriggerTreeTextItem) {
			TriggerTreeTextItem newItem = (TriggerTreeTextItem) item;
			activationTriggers.add(newItem);
		}
	}
}