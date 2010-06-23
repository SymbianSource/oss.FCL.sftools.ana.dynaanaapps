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
 * FilterProcessor DataProcessor
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Iterator;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.FilterDialog;
import com.nokia.traceviewer.dialog.FilterAdvancedDialog.ExitStatus;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeBaseItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeComponentItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeItem;
import com.nokia.traceviewer.dialog.treeitem.FilterTreeTextItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.dialog.treeitem.TreeItemContentProvider;
import com.nokia.traceviewer.dialog.treeitem.TreeItemListener;
import com.nokia.traceviewer.engine.DataWriter;
import com.nokia.traceviewer.engine.PlainTextReader;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet.LogicalOperator;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLFilterConfigurationImporter;

/**
 * Filter DataProcessor
 * 
 */
public final class FilterProcessor implements DataProcessor {

	/**
	 * Interval how often to update progressbar
	 */
	private static final int PROGRESSBAR_UPDATE_INTERVAL = 100;

	/**
	 * Name of Filtered file
	 */
	private static final String DEFAULT_FILTER_FILE_NAME = Messages
			.getString("FilterProcessor.DefaultFilterFileName"); //$NON-NLS-1$

	/**
	 * Path of Filtered file
	 */
	public static final String DEFAULT_FILTER_FILE_PATH = TraceViewerPlugin
			.getDefault().getStateLocation().append(DEFAULT_FILTER_FILE_NAME)
			.toOSString();

	/**
	 * Filter dialog used in setting filters
	 */
	private FilterDialog filterDialog;

	/**
	 * Content provider for the filter dialog
	 */
	private TreeItemContentProvider contentProvider;

	/**
	 * First visible object in the filter dialog tree
	 */
	private TreeItem root;

	/**
	 * Filter rule sets
	 */
	private final FilterRuleSet filterRuleSet;

	/**
	 * DataWriter used to write binary filtered file
	 */
	private DataWriter dataWriter;

	/**
	 * Writer to be used for plain text writing
	 */
	private PrintWriter plainOutput;

	/**
	 * External Filter Processor
	 */
	private ExternalFilterProcessor externalFilterProcessor;

	/**
	 * Indicates that external filter program is in use
	 */
	private boolean usingExternalFilter;

	/**
	 * Indicates that stream must be flushed
	 */
	private boolean needsFlushing;

	/**
	 * Indicates to show traces containing the rule. Otherwise they're hidden
	 */
	private boolean showTracesContainingRule = true;

	/**
	 * Indicates that logical OR is in use. Otherwise AND is in use.
	 */
	private boolean logicalOrInUse = true;

	/**
	 * Advanced filter string
	 */
	private String advancedFilterString = ""; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public FilterProcessor() {
		createInitialFilterTree();
		filterRuleSet = new FilterRuleSet();
		filterRuleSet.setOperator(LogicalOperator.OR);
	}

	/**
	 * Creates initial filter tree
	 */
	public void createInitialFilterTree() {
		contentProvider = new TreeItemContentProvider();
		// Create root node
		TreeItem treeRoot = new FilterTreeBaseItem(contentProvider, null,
				"root", //$NON-NLS-1$
				FilterTreeItem.Rule.GROUP);
		root = new FilterTreeBaseItem(contentProvider, treeRoot,
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.CONFIGURATION_FILE),
				FilterTreeItem.Rule.GROUP);
		treeRoot.addChild(root);
	}

	/**
	 * Imports filter rules from configuration file
	 */
	public void importFilterRules() {
		// Import Filter rules
		XMLFilterConfigurationImporter importer = new XMLFilterConfigurationImporter(
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
		TraceViewerGlobals.debug("processData in Filter", //$NON-NLS-1$
				TraceViewerGlobals.DebugLevel.TEST);
		if (isFiltering()) {
			// Read from filter file or triggered out, don't try to filter again
			if (properties.traceConfiguration.isReadFromFilterFile()
					|| properties.traceConfiguration.isTriggeredOut()) {
				// Don't' do anything
			} else {
				// Filter everything out from view that isn't from filterFile
				properties.traceConfiguration.setFilteredOut(true);

				// External filter processor
				if (isUsingExternalFilter()) {
					externalFilterProcessor.writeTraceToProcess(properties);
				} else {

					// Process filter rules
					boolean filterHits = processFilterRules(properties,
							filterRuleSet);

					// Write trace
					writeTrace(properties, filterHits);
				}
			}
		} else {
			properties.traceConfiguration.setFilteredOut(false);
		}

		// Update progressBar if needed
		updateProgressBar();
	}

	/**
	 * Writes trace if conditions ok
	 * 
	 * @param properties
	 *            trace to write
	 * @param filterHits
	 *            filter hits property
	 */
	private void writeTrace(TraceProperties properties, boolean filterHits) {
		// Write the trace
		if (filterHits && showTracesContainingRule) {
			writeTraceToFile(properties);
		} else if (!filterHits && !showTracesContainingRule) {
			writeTraceToFile(properties);
		}
	}

	/**
	 * Process Filter Rules
	 * 
	 * @param properties
	 *            trace properties
	 * @param ruleObject
	 *            filter rule set
	 * @return true if the rule object should be written to filter file
	 */
	private boolean processFilterRules(TraceProperties properties,
			FilterRuleObject ruleObject) {
		boolean filterHits = true;

		// Process the rule
		if (ruleObject != null) {
			filterHits = ruleObject.processRule(properties);

			// Rule set is null, abort
		} else {
			filterHits = false;
		}

		return filterHits;
	}

	/**
	 * Writes trace to file
	 * 
	 * @param properties
	 *            trace properties
	 */
	private void writeTraceToFile(TraceProperties properties) {

		// Write the trace to filterFile
		if (dataWriter != null) {

			// Write all parts of multipart trace
			if (properties.bTraceInformation.getMultiPartTraceParts() != null) {
				Iterator<byte[]> i = properties.bTraceInformation
						.getMultiPartTraceParts().getTraceParts().iterator();

				while (i.hasNext()) {
					byte[] byteArr = i.next();
					dataWriter.writeMessage(ByteBuffer.wrap(byteArr), 0,
							byteArr.length);
				}
			} else {
				dataWriter.writeMessage(properties.byteBuffer,
						properties.messageStart, properties.messageLength);
			}
		} else if (plainOutput != null) {
			writeStringToPlainTextFilterFile(properties.traceString);
		}
	}

	/**
	 * Writes a string to plain text filter file. Adds line break after the
	 * string
	 * 
	 * @param string
	 *            string to write
	 */
	private void writeStringToPlainTextFilterFile(String string) {
		StringBuffer buf = new StringBuffer(string.length() + 1);
		buf.append(string);
		buf.append('\n');
		plainOutput.write(buf.toString());
		needsFlushing = true;
	}

	/**
	 * Updates progressBar if needed
	 */
	private void updateProgressBar() {

		// Check that we are processing filter and update interval is reached
		if (isProcessingFilter()
				&& TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
						.getMainDataReader().getTraceCount()
						% PROGRESSBAR_UPDATE_INTERVAL == 0) {
			filterDialog.getProgressBar().updateProgressBar(
					TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
							.getMainDataReader().getTraceCount());

		}
	}

	/**
	 * Tells if filters are on
	 * 
	 * @return filtering status
	 */
	public boolean isFiltering() {
		boolean filtering = false;
		if (hasRules()) {
			filtering = true;
		} else if (isUsingExternalFilter()) {
			filtering = true;
		}
		return filtering;
	}

	/**
	 * Creates filtered file
	 */
	public void createFilteredFile() {
		// Build the file writer to filter file
		buildFilterFileWriter();

		// Shut down possible old filter reader
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.deleteFilterReader();

		// Start reading the data file from the beginning
		TraceViewerGlobals.getTraceViewer().readDataFileFromBeginning();

		// Create filter datareader
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.createFilterDataReader();

	}

	/**
	 * Builds the filter file writer
	 * 
	 * @return status of building filter file
	 */
	public boolean buildFilterFileWriter() {
		boolean success = false;
		try {
			// Binary writer
			if (!isUsingExternalFilter()
					&& !(TraceViewerGlobals.getTraceViewer()
							.getDataReaderAccess().getMainDataReader() instanceof PlainTextReader)) {

				// If plain output exists, close it
				if (plainOutput != null) {
					plainOutput.close();
					plainOutput = null;
				}

				// Close possible old binary writer
				if (dataWriter != null) {
					dataWriter.closeChannel();
					dataWriter = null;
				}

				RandomAccessFile filterFile = null;
				filterFile = new RandomAccessFile(DEFAULT_FILTER_FILE_PATH,
						"rw"); //$NON-NLS-1$
				filterFile.setLength(0);

				ByteChannel writeChannel = filterFile.getChannel();

				dataWriter = TraceViewerGlobals.getTraceProvider()
						.createDataWriter(writeChannel);

				success = true;
				// Plain text writer
			} else {
				// Close possible binary writer
				if (dataWriter != null) {
					dataWriter.closeChannel();
					dataWriter = null;
				}

				// Close possible previous plainOutput writer
				if (plainOutput != null) {
					plainOutput.close();
				}

				plainOutput = new PrintWriter(new FileWriter(
						DEFAULT_FILTER_FILE_PATH));
				success = true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Gets filter dialog
	 * 
	 * @return filter dialog
	 */
	public FilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = (FilterDialog) TraceViewerGlobals.getTraceViewer()
					.getDialogs().createDialog(Dialog.FILTER);
		}
		return filterDialog;
	}

	/**
	 * Tells if initial filtering is in process
	 * 
	 * @return true if initial filtering is in progress
	 */
	public boolean isProcessingFilter() {
		boolean isProcessing = false;
		if (filterDialog != null && filterDialog.getProgressBar() != null
				&& filterDialog.getProgressBar().getShell() != null
				&& !filterDialog.getProgressBar().getShell().isDisposed()) {
			isProcessing = true;
		}
		return isProcessing;
	}

	/**
	 * Gets filter rule set
	 * 
	 * @return filter rule set
	 */
	public FilterRuleSet getFilterRules() {
		return filterRuleSet;
	}

	/**
	 * Indicates that some filtering rules are applied
	 * 
	 * @return true if some filtering rules are applied
	 */
	public boolean hasRules() {
		boolean hasRules = false;
		if (filterRuleSet != null && !filterRuleSet.getFilterRules().isEmpty()) {
			hasRules = true;
		}
		return hasRules;
	}

	/**
	 * Gets the visible root of the tree
	 * 
	 * @return the root root of the tree
	 */
	public TreeItem getRoot() {
		return root;
	}

	/**
	 * Gets Filter item listener
	 * 
	 * @return the contentProvider
	 */
	public TreeItemListener getTreeItemListener() {
		return contentProvider;
	}

	/**
	 * Gets data writer
	 * 
	 * @return the dataWriter
	 */
	public DataWriter getDataWriter() {
		return dataWriter;
	}

	/**
	 * Sets using of external filter boolean
	 * 
	 * @param using
	 *            if true, starts using external filter program
	 */
	public void setUsingExternalFilter(boolean using) {
		// Previously true, shut down possible Processes
		if (usingExternalFilter && !using) {
			externalFilterProcessor.stopExternalApplication();
		} else if (using) {
			// Create the processor when first needed
			if (externalFilterProcessor == null) {
				externalFilterProcessor = new ExternalFilterProcessor();
			}
		}
		this.usingExternalFilter = using;
	}

	/**
	 * Flushes plain text writing streams
	 */
	public void flush() {
		// Flush the filter file writer
		if (plainOutput != null && needsFlushing) {
			plainOutput.flush();
			needsFlushing = false;
		}
	}

	/**
	 * Gets External Filter Processor
	 * 
	 * @return the externalFilterProcessor
	 */
	public ExternalFilterProcessor getExternalFilterProcessor() {
		return externalFilterProcessor;
	}

	/**
	 * Indicates that we are using external filter file
	 * 
	 * @return the usingExternalFilter
	 */
	public boolean isUsingExternalFilter() {
		boolean usingFilter = false;
		if (usingExternalFilter && externalFilterProcessor != null
				&& externalFilterProcessor.getProcess() != null) {
			usingFilter = true;
		}
		return usingFilter;
	}

	/**
	 * Checks if logical or is in use. Otherwise it's AND
	 * 
	 * @return the logicalOrInUse
	 */
	public boolean isLogicalOrInUse() {
		return logicalOrInUse;
	}

	/**
	 * Sets logical or to use
	 * 
	 * @param logicalOrInUse
	 *            the logicalOrInUse to set
	 */
	public void setLogicalOrInUse(boolean logicalOrInUse) {
		this.logicalOrInUse = logicalOrInUse;
	}

	/**
	 * Checks if show traces is on. Otherwise hide traces is on.
	 * 
	 * @return the showTracesContainingRule
	 */
	public boolean isShowTracesContainingRule() {
		return showTracesContainingRule;
	}

	/**
	 * Sets the show / hide traces option
	 * 
	 * @param showTracesContainingRule
	 *            the showTracesContainingRule to set
	 */
	public void setShowTracesContainingRule(boolean showTracesContainingRule) {
		this.showTracesContainingRule = showTracesContainingRule;
	}

	/**
	 * Filters string from external process and if it passes, writes it to
	 * filter file
	 * 
	 * @param properties
	 *            trace properties from the external process
	 */
	public void filterStringFromExternalProcess(TraceProperties properties) {
		// If there are filter rules
		if (hasRules()) {

			// Line must hit a filter to be written to the file
			processFilterRules(properties, filterRuleSet);

		} else {
			// Write the trace
			writeStringToPlainTextFilterFile(properties.traceString);
		}
	}

	/**
	 * Enable filter rule
	 * 
	 * @param item
	 *            the rule item
	 * @param op
	 *            logical operator
	 */
	public void enableRule(FilterTreeItem item, LogicalOperator op) {
		// Get or create the set where new items are inserted
		FilterRuleSet set;
		if (filterRuleSet.getFilterRules().isEmpty()) {
			set = new FilterRuleSet();
			set.setOperator(op);
			filterRuleSet.addObject(set);
		} else {
			set = (FilterRuleSet) filterRuleSet.getFilterRules().get(0);
		}

		if (item instanceof FilterTreeTextItem) {
			set.addObject(item);
		} else if (item instanceof FilterTreeComponentItem) {
			set.addObject(0, item);
		}

		// First rule to enable
		if (set.getFilterRules().size() == 1) {
			enableRulesOnStartup();
		}
	}

	/**
	 * Enable advanced filter string
	 * 
	 * @param filterString
	 *            the filter string
	 */
	public void enableAdvancedFilter(String filterString) {
		advancedFilterString = filterString;

		// Create needed shell items to advanced dialog
		getFilterDialog().getAdvancedDialog().create();

		// Check that rules are ok and then enable them
		if (getFilterDialog().getAdvancedDialog().checkWrittenRules(
				filterString)) {
			getFilterDialog().getAdvancedDialog().exitStatus = ExitStatus.APPLYBUTTON;

			// Generate the filter rule set
			FilterRuleObject set = getFilterDialog().getAdvancedDialog()
					.createRuleSet(filterString, false);
			filterRuleSet.addObject(set);

			enableRulesOnStartup();
		}
	}

	/**
	 * Enables rules on startup
	 */
	private void enableRulesOnStartup() {
		createFilteredFile();

		// Update view name
		TraceViewerGlobals.getTraceViewer().getView().updateViewName();
	}

	/**
	 * Gets advanced filter string
	 * 
	 * @return the advancedFilterString
	 */
	public String getAdvancedFilterString() {
		return advancedFilterString.trim();
	}

	/**
	 * Sets advanced filter string
	 * 
	 * @param advancedFilterString
	 *            the advancedFilterString to set
	 */
	public void setAdvancedFilterString(String advancedFilterString) {
		this.advancedFilterString = advancedFilterString;
	}
}