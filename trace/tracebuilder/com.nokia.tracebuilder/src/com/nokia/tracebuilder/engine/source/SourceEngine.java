/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Source engine manages source documents that are opened to Eclipse UI
*
*/
package com.nokia.tracebuilder.engine.source;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.LocationProperties;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.rules.ReadOnlyObjectRule;
import com.nokia.tracebuilder.source.OffsetLength;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceDocumentMonitor;
import com.nokia.tracebuilder.source.SourceDocumentProcessor;
import com.nokia.tracebuilder.source.SourceParserException;
import com.nokia.tracebuilder.utils.DocumentFactory;

/**
 * Source engine manages source documents that are opened to Eclipse UI.
 * 
 */
public class SourceEngine implements SourceDocumentProcessor,
		Iterable<SourceProperties> {

	/**
	 * Document monitor
	 */
	private SourceDocumentMonitor documentMonitor;

	/**
	 * Trace model listener implementation
	 */
	private SourceEngineModelListener modelListener = new SourceEngineModelListener(
			this);

	/**
	 * Trace model extension listener
	 */
	private SourceEngineModelExtensionListener extensionListener = new SourceEngineModelExtensionListener(
			this);

	/**
	 * The callback interfaces are notified about source file changes
	 */
	private ArrayList<SourceListener> listeners = new ArrayList<SourceListener>();

	/**
	 * Running flag
	 */
	private boolean running;

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Source list
	 */
	private ArrayList<SourceProperties> tempList = new ArrayList<SourceProperties>();

	/**
	 * Read-only files
	 */
	private String[] READ_ONLY = { ".h" //$NON-NLS-1$
	};

	/**
	 * Non-source file list
	 */
	private ArrayList<String> nonSourceFiles = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public SourceEngine(TraceModel model) {
		this.model = model;
	}

	/**
	 * Starts this engine. Does nothing if already running
	 */
	public void start() {
		if (!running) {
			documentMonitor = DocumentFactory.getDocumentMonitor();
			documentMonitor.startMonitor(this);
			running = true;
			model.addModelListener(modelListener);
			model.addExtensionListener(extensionListener);
		}
	}

	/**
	 * Shuts down the source engine. Does nothing if already stopped
	 */
	public void shutdown() {
		if (running) {
			documentMonitor.stopMonitor();
			documentMonitor = null;
			running = false;
			model.removeModelListener(modelListener);
			model.removeExtensionListener(extensionListener);
		}
	}

	/**
	 * Gets the running flag
	 * 
	 * @return the flag
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Adds source listener callback interface
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addSourceListener(SourceListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a source listener
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeSourceListener(SourceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Gets the sources
	 * 
	 * @return the sources
	 */
	public Iterator<SourceProperties> getSources() {
		tempList.clear();
		for (SourceDocumentInterface doc : documentMonitor) {
			tempList.add((SourceProperties) doc.getOwner());
		}
		return tempList.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<SourceProperties> iterator() {
		return getSources();
	}

	/**
	 * Gets the currently active source
	 * 
	 * @return the source
	 */
	public SourceProperties getSelectedSource() {
		SourceProperties srcProperties = null;
		SourceDocumentInterface srcDocInterface = documentMonitor.getSelectedSource();

		if (srcDocInterface != null) {
			srcProperties = (SourceProperties) srcDocInterface.getOwner();
		}
		return srcProperties;
	}

	/**
	 * Gets the current selection from the given source
	 * 
	 * @param props
	 *            the source
	 * @return the selection
	 */
	public OffsetLength getSelection(SourceProperties props) {
		return documentMonitor
				.getSelection(props.getSourceEditor().getSource());
	}

	/**
	 * Sets the focus to the current source
	 */
	public void setFocus() {
		documentMonitor.setFocus();
	}

	/**
	 * Gets the source context based on current caret position. This throws an
	 * exception if the caret is not in valid position. Invalid positions are:
	 * <ul>
	 * <li>Source file is not open</li>
	 * <li>Outside function implementation</li>
	 * <li>Within commented code</li>
	 * <li>Within quoted strings</li>
	 * </ul>
	 * 
	 * @return the context
	 */
	public SourceContext getSelectedContext() {
		SourceContext retval = null;
		// checkInsertLocation verifies that source is editable
		if (checkInsertLocation()) {
			SourceDocumentInterface selectedSource = documentMonitor
					.getSelectedSource();
			SourceProperties properties = (SourceProperties) selectedSource
					.getOwner();
			OffsetLength offset = documentMonitor.getSelection(selectedSource);
			retval = properties.getSourceEditor().getContext(offset.offset);
		}
		return retval;
	}

	/**
	 * Gets the source properties related to given context
	 * 
	 * @param context
	 *            the context
	 * @return the properties
	 */
	public SourceProperties getSourceOfContext(SourceContext context) {
		return (SourceProperties) context.getParser().getSource().getOwner();
	}

	/**
	 * Checks the validity of cursor location
	 * 
	 * @throws TraceBuilderException
	 *             if trace can be inserted to cursor location
	 */
	public void checkCursorLocationValidity() throws TraceBuilderException {
		SourceDocumentInterface document = documentMonitor.getSelectedSource();
		if (document != null) {
			SourceProperties properties = (SourceProperties) document
					.getOwner();
			if (!properties.isReadOnly()) {
				OffsetLength selection = documentMonitor.getSelection(document);
				if (selection == null) {
					throw new TraceBuilderException(
							TraceBuilderErrorCode.INVALID_SOURCE_LOCATION);
				}
			} else {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.SOURCE_NOT_EDITABLE);
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.SOURCE_NOT_OPEN);
		}
	}

	/**
	 * Inserts a trace to the start of the current selection. Creates a
	 * {@link SourceListener#sourceChanged(SourceProperties) sourceChanged}
	 * event to all listeners after each trace has been added.
	 * 
	 * @param trace
	 *            the trace to be added to the start of selection
	 * @return the insert location for the trace
	 * @throws TraceBuilderException
	 *             if insertion fails
	 */
	public int addTrace(Trace trace) throws TraceBuilderException {
		// Checks the location validity before inserting
		checkCursorLocationValidity();
		ReadOnlyObjectRule readOnly = trace
				.getExtension(ReadOnlyObjectRule.class);
		if (readOnly != null) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INSERT_TRACE_DOES_NOT_WORK);
		}
		SourceDocumentInterface document = documentMonitor.getSelectedSource();
		int retval = documentMonitor.getSelection(document).offset;
		insertTrace(trace, (SourceProperties) document.getOwner(), retval);
		return retval;
	}

	/**
	 * Queues the addition of #include statement to source
	 * 
	 * @param source
	 *            the source to be updated
	 * @param name
	 *            the include file name
	 */
	public void addInclude(SourceProperties source, String name) {
		if (!source.isReadOnly()) {
			IncludeStatementAdder adder = new IncludeStatementAdder(source,
					name);
			adder.update();
		}
	}

	/**
	 * Removes a trace location from source
	 * 
	 * @param location
	 *            the location to be removed
	 */
	public void removeLocation(TraceLocation location) {
		if (!location.getSource().isReadOnly()) {
			TraceLocationWriter.removeLocation(location);
		}
	}

	/**
	 * Inserts a trace into the given source file at given offset. Creates a
	 * {@link SourceListener#sourceChanged(SourceProperties) sourceChanged}
	 * event to all listeners after the trace has been added.
	 * 
	 * @param trace
	 *            the trace to be added
	 * @param source
	 *            the source where trace is added
	 * @param offset
	 *            the offset where trace is added
	 * @throws TraceBuilderException
	 *             if insertion fails
	 */
	public void insertTrace(Trace trace, SourceProperties source, int offset)
			throws TraceBuilderException {
		if (!source.isReadOnly()) {
			try {
				TraceLocationWriter.addLocation(source, trace, offset);
			} catch (Exception e) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_SOURCE_LOCATION, null,
						trace);
			}
		}
	}

	/**
	 * Replaces a location with the given trace. The parameters from the
	 * existing location are not changed
	 * 
	 * @param trace
	 *            the trace to be inserted
	 * @param replaced
	 *            the location to be replaced with the trace
	 * @throws TraceBuilderException
	 *             if insertion fails
	 */
	public void replaceLocationWithTrace(Trace trace, TraceLocation replaced)
			throws TraceBuilderException {
		removeLocation(replaced);
		SourceProperties source = replaced.getSource();
		if (source != null && !source.isReadOnly()) {
			try {
				TraceLocationWriter.replaceLocation(source, trace, replaced);
			} catch (Exception e) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_SOURCE_LOCATION, null,
						trace);
			}
		}
	}

	/**
	 * Fires sourceChanged event to listeners
	 * 
	 * @see SourceListener#sourceChanged(SourceProperties)
	 * @param source
	 *            the source
	 */
	private void fireSourceChanged(SourceProperties source) {
		for (SourceListener l : listeners) {
			l.sourceChanged(source);
		}
	}

	/**
	 * Fires sourceProcessingStarted event to listeners
	 * 
	 * @see SourceListener#sourceProcessingStarted(SourceProperties)
	 * @param source
	 *            the source
	 */
	private void fireSourceProcessingStarted(SourceProperties source) {
		for (SourceListener l : listeners) {
			l.sourceProcessingStarted(source);
		}
	}

	/**
	 * Fires sourceProcessingComplete event to listeners
	 * 
	 * @see SourceListener#sourceProcessingComplete(SourceProperties)
	 * @param source
	 *            the source
	 */
	private void fireSourceProcessingComplete(SourceProperties source) {
		for (SourceListener l : listeners) {
			l.sourceProcessingComplete(source);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      sourceOpened(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	public void sourceOpened(SourceDocumentInterface source) {
		SourceProperties properties = new SourceProperties(model,
				documentMonitor.getFactory(), source);
		// Headers are marked read-only
		for (String s : READ_ONLY) {
			String fileName = properties.getFileName();
			if (fileName != null && fileName.endsWith(s)) {
				properties.setReadOnly(true);
				break;
			}
		}
		properties.sourceOpened();
		source.setOwner(properties);
		for (SourceListener l : listeners) {
			l.sourceOpened(properties);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      selectionChanged(com.nokia.tracebuilder.source.SourceDocumentInterface,
	 *      int, int)
	 */
	public void selectionChanged(SourceDocumentInterface source, int offset,
			int length) {
		SourceProperties properties = (SourceProperties) source.getOwner();
		TraceLocation location = properties.getLocation(offset);
		if (location != null && location.getLocationList() != null) {
			for (SourceListener l : listeners) {
				l.selectionChanged(location);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      sourceAboutToBeChanged(com.nokia.tracebuilder.source.SourceDocumentInterface,
	 *      int, int, java.lang.String)
	 */
	public void sourceAboutToBeChanged(SourceDocumentInterface source,
			int offset, int length, String newText) {
		SourceProperties properties = (SourceProperties) source.getOwner();
		try {
			if (!properties.getUpdateQueue().hasQueuedUpdates()) {
				properties.prepareChange(offset, length, newText);
			}
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Document change preprocessor failure", e); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      sourceChanged(com.nokia.tracebuilder.source.SourceDocumentInterface,
	 *      int, int, java.lang.String)
	 */
	public void sourceChanged(SourceDocumentInterface source, int offset,
			int length, String newText) {
		SourceProperties properties = (SourceProperties) source.getOwner();
		try {
			sourceChanged(properties, offset, length, newText);
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Document change processor failure", e); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      sourceClosed(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	public void sourceClosed(SourceDocumentInterface source) {
		SourceProperties properties = (SourceProperties) source.getOwner();
		for (SourceListener l : listeners) {
			l.sourceClosed(properties);
		}
		properties.sourceClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentProcessor#
	 *      sourceSaved(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	public void sourceSaved(SourceDocumentInterface source) {
		SourceProperties properties = (SourceProperties) source.getOwner();
		for (SourceListener l : listeners) {
			l.sourceSaved(properties);
		}
	}

	/**
	 * Checks that the current caret position is valid for trace insertion
	 * 
	 * @return true if valid, false if not
	 */
	private boolean checkInsertLocation() {
		boolean retval = false;
		SourceDocumentInterface selectedSource = documentMonitor
				.getSelectedSource();
		if (selectedSource != null) {
			if (documentMonitor.isSourceEditable(selectedSource)) {
				SourceProperties properties = (SourceProperties) selectedSource
						.getOwner();
				if (!properties.isReadOnly()) {
					OffsetLength selection = documentMonitor
							.getSelection(selectedSource);
					if (selection != null) {
						retval = properties
								.checkInsertLocation(selection.offset);
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Processes a source change notification
	 * 
	 * @param source
	 *            the changed source
	 * @param offset
	 *            offset to the replaced text
	 * @param length
	 *            the length of the replaced text
	 * @param newText
	 *            the text that was added to the document
	 * @throws SourceParserException
	 *             if processing fails
	 */
	private void sourceChanged(SourceProperties source, int offset, int length,
			String newText) throws SourceParserException {
		// When the source update queue contains more than one element, it is
		// faster to disable all updates and do a full sync afterwards
		// The source update queue must ensure that the updates are run starting
		// from the bottom of the file. This way they will not interfere with
		// each other
		if (source.getUpdateQueue().hasQueuedUpdates()) {
			if (source.isActive()) {
				fireSourceProcessingStarted(source);
				source.setActive(false);
			}
		} else {
			if (source.isActive()) {
				source.preProcessChange(offset, length, newText);
				fireSourceChanged(source);
				source.postProcessChange();
			} else {
				source.setActive(true);
				source.preProcessFullSync(offset, length, newText);
				fireSourceProcessingComplete(source);
				source.postProcessFullSync();
			}
		}
	}

	/**
	 * Updates a trace location
	 * 
	 * @param trace
	 *            the trace
	 */
	public void updateTrace(Trace trace) {
		if (trace.getModel().isValid()) {
			// Read-only traces cannot be updated
			if (trace.getExtension(ReadOnlyObjectRule.class) == null) {
				TraceLocationList list = trace
						.getExtension(TraceLocationList.class);
				if (list != null) {
					for (LocationProperties loc : list) {
						TraceLocationWriter.updateLocation((TraceLocation) loc);
					}
				}
			} else {
				String msg = TraceBuilderErrorMessages.getErrorMessage(
						TraceBuilderErrorCode.CANNOT_UPDATE_TRACE_INTO_SOURCE,
						null);
				TraceBuilderGlobals.getEvents().postWarningMessage(msg, trace);
			}
		}
	}

	/**
	 * Parser rule added notification
	 * 
	 * @param rule
	 *            the parser rule
	 */
	void parserAdded(SourceParserRule rule) {
		for (SourceProperties source : this) {
			source.addParserRule(rule);
			try {
				source.preProcessFullSync(0, 0, ""); //$NON-NLS-1$
				fireSourceChanged(source);
				source.postProcessFullSync();
			} catch (Exception e) {
				if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
					TraceBuilderGlobals.getEvents()
							.postCriticalAssertionFailed(
									"Failed to add parser", e); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Parser rule removed notification
	 * 
	 * @param rule
	 *            the parser rule
	 */
	void parserRemoved(SourceParserRule rule) {
		for (SourceProperties source : this) {
			source.removeParserRule(rule);
			try {
				source.preProcessFullSync(0, 0, ""); //$NON-NLS-1$
				fireSourceChanged(source);
				source.postProcessFullSync();
			} catch (Exception e) {
				if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
					TraceBuilderGlobals.getEvents()
							.postCriticalAssertionFailed(
									"Failed to remove parser", e); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Called when a rule is added or removed
	 * 
	 * @param object
	 *            the object which was changed
	 */
	void ruleUpdated(TraceObject object) {
		// Formatting rule changes cause updates to source
		if (object.isComplete()) {
			if (object instanceof TraceParameter) {
				Trace owner = ((TraceParameter) object).getTrace();
				if (owner.isComplete()) {
					updateTrace(owner);
				}
			} else if (object instanceof Trace) {
				updateTrace((Trace) object);
			}
		}
	}

	/**
	 * Adds a non-source file to this list.
	 * 
	 * @param filePath
	 *            the non-source file path to added
	 */
	public void addNonSourceFile(String filePath) {
		nonSourceFiles.add(filePath);
	}

	/**
	 * Removes a non-source file from this list
	 * 
	 * @param filePath
	 *            the non-source file path to be removed
	 * @return true if removed
	 */
	public boolean removeNonSourceFile(String filePath) {
		boolean retVal = nonSourceFiles.remove(filePath);
		return retVal;
	}

	/**
	 * Removes a non-source files
	 * 
	 */
	public void removeNonSourceFiles() {
		nonSourceFiles.clear();
	}

	/**
	 * Gets lis of non-source files
	 * 
	 * @return the list of non-source file paths
	 */
	public ArrayList<String> getNonSourceFiles() {
		return nonSourceFiles;
	}

}
