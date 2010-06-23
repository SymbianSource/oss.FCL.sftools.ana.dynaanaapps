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
 * Data Processor Access class
 *
 */
package com.nokia.traceviewer.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.action.OpenDecodeFileAction;
import com.nokia.traceviewer.engine.dataprocessor.ColorProcessor;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.engine.dataprocessor.Decoder;
import com.nokia.traceviewer.engine.dataprocessor.DummyView;
import com.nokia.traceviewer.engine.dataprocessor.FilterProcessor;
import com.nokia.traceviewer.engine.dataprocessor.LineCountProcessor;
import com.nokia.traceviewer.engine.dataprocessor.Logger;
import com.nokia.traceviewer.engine.dataprocessor.SearchProcessor;
import com.nokia.traceviewer.engine.dataprocessor.TimestampParser;
import com.nokia.traceviewer.engine.dataprocessor.TraceCommentHandler;
import com.nokia.traceviewer.engine.dataprocessor.TriggerProcessor;
import com.nokia.traceviewer.engine.dataprocessor.VariableTracingProcessor;

/**
 * Data Processor Access class
 */
public final class DataProcessorAccess {

	/**
	 * Linked list of DataProcessors
	 */
	private List<DataProcessor> dataProcessors;

	/**
	 * DataProcessor Trigger
	 */
	private DataProcessor trigger;

	/**
	 * DataProcessor TimestampParser
	 */
	private DataProcessor timestampParser;

	/**
	 * DataProcessor Logger
	 */
	private DataProcessor logger;

	/**
	 * ColorProcessor. Is not DataProcessor as it doesn't do anything with the
	 * traces
	 */
	private ColorProcessor colorProcessor;

	/**
	 * DataProcessor SearchProcessor
	 */
	private DataProcessor searchProcessor;

	/**
	 * DataProcessor FilterProcessor
	 */
	private DataProcessor filterProcessor;

	/**
	 * DataProcessor LineCountProcessor
	 */
	private DataProcessor lineCountProcessor;

	/**
	 * DataProcessor VariableTracingProcessor
	 */
	private DataProcessor variableTracingProcessor;

	/**
	 * DataProcessor Decoder
	 */
	private DataProcessor decoder;

	/**
	 * Trace comment handler
	 */
	private DataProcessor traceCommentHandler;

	/**
	 * Dummy view
	 */
	private DataProcessor dummyView;

	/**
	 * Last index of view
	 */
	private int lastIndexOfView;

	/**
	 * Creates DataProcessors
	 */
	public void createDataProcessors() {
		// Creation of dataProcessors
		dataProcessors = new ArrayList<DataProcessor>();
		decoder = new Decoder();
		trigger = new TriggerProcessor();
		timestampParser = new TimestampParser();
		traceCommentHandler = new TraceCommentHandler();
		logger = new Logger();
		colorProcessor = new ColorProcessor();
		searchProcessor = new SearchProcessor();
		filterProcessor = new FilterProcessor();
		lineCountProcessor = new LineCountProcessor();
		variableTracingProcessor = new VariableTracingProcessor();
		dummyView = new DummyView();

		// Add dataprocessors to list
		dataProcessors.add(decoder);
		dataProcessors.add(trigger);
		dataProcessors.add(timestampParser);
		dataProcessors.add(filterProcessor);
		dataProcessors.add(traceCommentHandler);
		dataProcessors.add(logger);
		dataProcessors.add(lineCountProcessor);
		dataProcessors.add(variableTracingProcessor);
		dataProcessors.add(dummyView);
	}

	/**
	 * Gets dataProcessor list
	 * 
	 * @return list of DataProcessors
	 */
	public List<DataProcessor> getDataProcessorList() {
		return dataProcessors;
	}

	/**
	 * Gets trigger processor
	 * 
	 * @return the trigger processor
	 */
	public TriggerProcessor getTriggerProcessor() {
		return (TriggerProcessor) trigger;
	}

	/**
	 * Gets decoder
	 * 
	 * @return the decoder
	 */
	public Decoder getDecoder() {
		return (Decoder) decoder;
	}

	/**
	 * Gets timestamp parser
	 * 
	 * @return the timestamp parser
	 */
	public TimestampParser getTimestampParser() {
		return (TimestampParser) timestampParser;
	}

	/**
	 * Gets logger
	 * 
	 * @return the logger
	 */
	public Logger getLogger() {
		return (Logger) logger;
	}

	/**
	 * Gets colorer
	 * 
	 * @return the colorer
	 */
	public ColorProcessor getColorer() {
		return colorProcessor;
	}

	/**
	 * Gets searchProcessor
	 * 
	 * @return the searchProcessor
	 */
	public SearchProcessor getSearchProcessor() {
		return (SearchProcessor) searchProcessor;
	}

	/**
	 * Gets filterProcessor
	 * 
	 * @return the filterProcessor
	 */
	public FilterProcessor getFilterProcessor() {
		return (FilterProcessor) filterProcessor;
	}

	/**
	 * Gets lineCountProcessor
	 * 
	 * @return the lineCountProcessor
	 */
	public LineCountProcessor getLineCountProcessor() {
		return (LineCountProcessor) lineCountProcessor;
	}

	/**
	 * Gets variableTracingProcessor
	 * 
	 * @return the variableTracingProcessor
	 */
	public VariableTracingProcessor getVariableTracingProcessor() {
		return (VariableTracingProcessor) variableTracingProcessor;
	}

	/**
	 * Gets trace comment handler
	 * 
	 * @return trace comment handler
	 */
	public TraceCommentHandler getTraceCommentHandler() {
		return (TraceCommentHandler) traceCommentHandler;
	}

	/**
	 * Tells if progressbar is visible
	 * 
	 * @return true if progressbar is visible
	 */
	public boolean processingWithProgressBar() {
		// Check if we are decoding traces
		boolean decodingTraces = ((TraceViewerGlobals.getTraceViewer()
				.getView() != null) && ((OpenDecodeFileAction) TraceViewerGlobals
				.getTraceViewer().getView().getActionFactory()
				.getOpenDecodeFileAction()).isDecodingTraces());

		// Check that some processor is processing or we are decoding traces
		boolean processing = getFilterProcessor().isProcessingFilter()
				|| getLineCountProcessor().isProcessingCounting()
				|| getVariableTracingProcessor().isProcessingTracing()
				|| decodingTraces;

		return processing;
	}

	/**
	 * Closes progressbar which is visible
	 */
	public void closeProgressBar() {
		TraceViewerTraceViewInterface view = TraceViewerGlobals
				.getTraceViewer().getView();

		if (view != null) {

			// Check if we are decoding traces
			boolean decodingTraces = ((OpenDecodeFileAction) view
					.getActionFactory().getOpenDecodeFileAction())
					.isDecodingTraces();

			// Close decode progressbar
			if (decodingTraces) {
				view.closeProgressBar(((OpenDecodeFileAction) view
						.getActionFactory().getOpenDecodeFileAction())
						.getProgressBarDialog());

				// Close filter progressbar
			} else if (getFilterProcessor().isProcessingFilter()) {
				view.closeProgressBar(getFilterProcessor().getFilterDialog()
						.getProgressBar());

				// Close line count progressbar
			} else if (getLineCountProcessor().isProcessingCounting()) {
				view.closeProgressBar(getLineCountProcessor()
						.getLineCountDialog().getProgressBar());

				// Close variable tracing progressbar
			} else if (getVariableTracingProcessor().isProcessingTracing()) {
				view.closeProgressBar(getVariableTracingProcessor()
						.getVariableTracingDialog().getProgressBar());
			}
		}
	}

	/**
	 * Imports configurations
	 */
	public void importConfiguration() {
		// Run imports in UI thread to ensure access to dialogs
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				getFilterProcessor().importFilterRules();
				getLineCountProcessor().importLineCountRules();
				getVariableTracingProcessor().importVariableTracingRules();
				getTriggerProcessor().importTriggerRules();
				getColorer().importColorRules();
			}
		});
	}

	/**
	 * Adds Trace View to the DataProcessor list
	 * 
	 * @param traceView
	 *            the trace view
	 */
	public void addTraceViewToList(DataProcessor traceView) {

		// Replace Dummy view when first real view is registered
		int indexOfDummyView = dataProcessors.indexOf(dummyView);
		if (indexOfDummyView != -1) {
			dataProcessors.remove(indexOfDummyView);
			dataProcessors.add(indexOfDummyView, traceView);
			lastIndexOfView = indexOfDummyView;

			// Dummy view is already replaced by real one
		} else {
			int indexOfView = 0;

			// Get the index of the already registered view and insert the new
			// one after it
			if (TraceViewerGlobals.getTraceViewer().getView() != null) {
				indexOfView = dataProcessors.indexOf(TraceViewerGlobals
						.getTraceViewer().getView()) + 1;
			}

			// If not found yet, check another view
			if (indexOfView == 0
					&& TraceViewerGlobals.getTraceViewer().getPropertyView() != null) {
				indexOfView = dataProcessors.indexOf(TraceViewerGlobals
						.getTraceViewer().getPropertyView());
			}

			// If views were not found, it should mean that view is set to null
			if (indexOfView == 0 && traceView == null) {

				// Insert DummyView back
				dataProcessors.add(lastIndexOfView, dummyView);

				// Add the view to list
			} else if (traceView != null) {

				// Save last view index
				if (lastIndexOfView == 0 || lastIndexOfView > indexOfView) {
					lastIndexOfView = indexOfView;
				}

				dataProcessors.add(indexOfView, traceView);
			}
		}
	}
}
