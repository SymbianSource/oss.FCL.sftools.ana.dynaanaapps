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
 * A singleton access point to functionality of Trace Viewer
 *
 */
package com.nokia.traceviewer.engine;

import java.util.List;

import com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerConsts;
import com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerPlugin;
import com.nokia.carbide.remoteconnections.RemoteConnectionsActivator;
import com.nokia.carbide.remoteconnections.interfaces.IConnection;
import com.nokia.carbide.remoteconnections.internal.api.IConnection2;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.OpenDecodeFileInStartupHandler;
import com.nokia.traceviewer.action.ReloadDecodeFilesAction;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.XMLGeneralConfigurationExporter;
import com.nokia.traceviewer.engine.preferences.XMLGeneralConfigurationImporter;
import com.nokia.traceviewer.internal.api.TraceViewerAPI2Impl;

/**
 * A singleton access point to functionality of Trace Viewer
 * 
 */
final class TraceViewer implements TraceViewerInterface, MediaCallback {

	/**
	 * Default file name
	 */
	private final String DEFAULT_FILE_NAME = Messages
			.getString("TraceViewer.DefaultFileName"); //$NON-NLS-1$

	/**
	 * Default file path
	 */
	private final String DEFAULT_FILE_PATH = TraceViewerPlugin.getDefault()
			.getStateLocation().append(DEFAULT_FILE_NAME).toOSString();

	/**
	 * Data Processor Access
	 */
	private DataProcessorAccess dataProcessorAccess;

	/**
	 * Data Reader Access
	 */
	private DataReaderAccess dataReaderAccess;

	/**
	 * Interface to the view
	 */
	private TraceViewerTraceViewInterface view;

	/**
	 * Interface to the dialogs. Initialize with dummy class.
	 */
	private TraceViewerDialogInterface dialogs = new DummyDialogs();

	/**
	 * Interface to trace file handler. Initialize with internal file handler.
	 */
	private TraceFileHandler fileHandler = new InternalFileHandler(
			DEFAULT_FILE_PATH);

	/**
	 * Interface to the propertyview
	 */
	private TraceViewerPropertyViewInterface propertyView;

	/**
	 * Event listener that listens events from EventRouter
	 */
	private TraceViewerEventListener eventListener;

	/**
	 * Interface to the connection
	 */
	private Connection connection;

	/**
	 * TraceViewer stateHolder
	 */
	private StateHolder stateHolder;

	/**
	 * Trace Provider in use
	 */
	private TraceProvider provider;

	/**
	 * Decode Provider
	 */
	private DecodeProvider decodeProvider;

	/**
	 * Indicates that filter progressbar can be closed when currentDataReader
	 * says EOF next time
	 */
	private boolean closeFilterProgressBar;

	/**
	 * Trace fetcher
	 */
	private TraceFetcher traceFetcher;

	/**
	 * Currently open connection
	 */
	private IConnection currentlyOpenConnection;

	/**
	 * Current connection status listener
	 */
	private ConnectionStatusListener currentConnectionStatusListener;

	/**
	 * Gets the trace viewer interface
	 * 
	 * @return this object
	 */
	TraceViewerInterface getTraceViewer() {
		return this;
	}

	/**
	 * Sets trace provider
	 * 
	 * @param newProvider
	 *            new trace provider
	 * @param clearDataBefore
	 *            if true, clear data file before changing the new provider
	 */
	public void setTraceProvider(TraceProvider newProvider,
			boolean clearDataBefore) {
		// First provider, set as default
		if (this.provider == null) {
			this.provider = newProvider;

			// Create main file
			createMainFile();
			initializeAfterProvider();

			// Check if new provider is selected and not in use already. If yes,
			// set as default.
		} else if (newProvider.getName().equals(
				TraceViewerPlugin.getDefault().getPreferenceStore().getString(
						PreferenceConstants.DATA_FORMAT))
				&& this.provider != newProvider) {

			this.provider = newProvider;

			// Clear old data file
			if (clearDataBefore) {
				clearAllData();
			}

			initializeAfterProvider();
		}
	}

	/**
	 * Initialize TraceViewer when Trace Provider has changed
	 */
	private void initializeAfterProvider() {
		// Empty views
		TraceViewerGlobals.getTraceViewer().emptyViews();

		// Provider is alive, data reader can now be created
		dataReaderAccess.createMainDataReader();

		// If filtering is on and TraceProvider changes, create a new Filter
		// Data Reader to be able to decode the new data
		if (dataProcessorAccess.getFilterProcessor().isFiltering()) {
			dataReaderAccess.createFilterDataReader();
		}
	}

	/**
	 * Gets trace provider
	 * 
	 * @return trace provider
	 */
	public TraceProvider getTraceProvider() {
		return provider;
	}

	/**
	 * Sets decode provider
	 * 
	 * @param decodeProvider
	 */
	public void setDecodeProvider(DecodeProvider decodeProvider) {
		this.decodeProvider = decodeProvider;

		// Set decode provider to show or not show class and function names
		// before the trace text
		boolean showClassFunctionName = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						PreferenceConstants.SHOW_CLASS_FUNCTION_NAME_CHECKBOX);
		boolean showComponentGroupName = TraceViewerPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						PreferenceConstants.SHOW_COMPONENT_GROUP_NAME_CHECKBOX);
		decodeProvider.setAddPrefixesToTrace(showClassFunctionName,
				showComponentGroupName);
	}

	/**
	 * Gets decode provider
	 * 
	 * @return decode provider
	 */
	public DecodeProvider getDecodeProvider() {
		return decodeProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getConnection()
	 */
	public Connection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerInterface#getDataProcessorAccess
	 * ()
	 */
	public DataProcessorAccess getDataProcessorAccess() {
		return dataProcessorAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerInterface#getDataReaderAccess()
	 */
	public DataReaderAccess getDataReaderAccess() {
		return dataReaderAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getStateHolder()
	 */
	public StateHolder getStateHolder() {
		return stateHolder;
	}

	/**
	 * Starts TraceViewer.
	 */
	void start() {
		// Create DataProcessors
		dataProcessorAccess = new DataProcessorAccess();
		dataProcessorAccess.createDataProcessors();

		stateHolder = new StateHolder();

		// Create Data Reader Access class
		dataReaderAccess = new DataReaderAccess(this, DEFAULT_FILE_PATH);

		// Start listening events from the event router
		eventListener = new TraceViewerEventListener();
		eventListener.register();

		// Create "current connection" listener
		ConnectionChangedListener connectionListener = new ConnectionChangedListener();
		RemoteConnectionsActivator.getConnectionsManager()
				.addConnectionListener(connectionListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerInterface#setTraceView(
	 * TraceViewerTraceViewInterface)
	 */
	public void setTraceView(TraceViewerTraceViewInterface newView) {
		if (view == null || view.isDisposed()) {

			// If old view is disposed but not null, remove it from the list
			if (view != null) {
				dataProcessorAccess.getDataProcessorList().remove(view);
			}

			view = newView;

			// Add view to DataProcessor list
			dataProcessorAccess.addTraceViewToList((DataProcessor) newView);

			if (view != null) {

				// View has notified about himself, we can import old
				// configurations which require view
				dataProcessorAccess.importConfiguration();

				// Import general settings
				XMLGeneralConfigurationImporter importer = new XMLGeneralConfigurationImporter(
						TraceViewerPlugin.getDefault().getPreferenceStore()
								.getString(
										PreferenceConstants.CONFIGURATION_FILE),
						true);
				importer.importData();

				// Clear data when view comes visible. This is to ensure that
				// view is in sync with the data file.
				clearAllData();

				// Open possible previous decode files
				new OpenDecodeFileInStartupHandler().start();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerInterface#setPropertyView(
	 * TraceViewerPropertyViewInterface)
	 */
	public void setPropertyView(TraceViewerPropertyViewInterface newView) {
		if (propertyView == null || propertyView.isDisposed()) {

			// If old one is disposed but not null, remove it from the list
			if (propertyView != null) {
				dataProcessorAccess.getDataProcessorList().remove(propertyView);
			}

			this.propertyView = newView;

			// Add view to DataProcessor list
			dataProcessorAccess.addTraceViewToList((DataProcessor) newView);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerInterface#setDialogs(
	 * TraceViewerDialogInterface)
	 */
	public void setDialogs(TraceViewerDialogInterface dialogs) {
		this.dialogs = dialogs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.MediaCallback#processTrace(com.nokia.traceviewer
	 * .engine.TraceProperties)
	 */
	public void processTrace(TraceProperties properties) {
		// Loop through DataProcessors
		List<DataProcessor> dataProcessors = dataProcessorAccess
				.getDataProcessorList();
		for (int i = 0; i < dataProcessors.size(); i++) {
			dataProcessors.get(i).processData(properties);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.MediaCallback#endOfFile()
	 */
	public void endOfFile(DataReader reader) {
		// If everything is not shown, show it
		if (((view != null && view.hasUnshownData()) || (propertyView != null && propertyView
				.hasUnshownData()))) {
			if (reader == dataReaderAccess.getCurrentDataReader()) {
				if (view != null) {
					view.update();
				}
				if (propertyView != null) {
					propertyView.updatePropertyTables();
				}
			} else {
				// Nothing
			}
		}

		// Plain text filter file must be flushed
		if (reader instanceof PlainTextReader
				&& (reader == dataReaderAccess.getMainDataReader())) {
			dataProcessorAccess.getFilterProcessor().flush();
		}

		// If processing something with the progressbar, close it
		if ((reader == dataReaderAccess.getMainDataReader())
				&& (dataProcessorAccess.processingWithProgressBar())) {
			if (!dataProcessorAccess.getFilterProcessor().isFiltering()) {
				dataProcessorAccess.closeProgressBar();
				// Filtering and main data reader is in the end. Next time
				// current data reader is in the end, progressbar can be closed
			} else {
				closeFilterProgressBar = true;
			}
		}

		// Close filter progressBar when current data reader is in the end after
		// first the main data reader is in the end
		if (closeFilterProgressBar
				&& reader == dataReaderAccess.getCurrentDataReader()) {
			dataProcessorAccess.closeProgressBar();
			closeFilterProgressBar = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.MediaCallback#dataHandleChanged()
	 */
	public void dataHandleChanged() {
		// No need to do anything because main and current reader are handled
		// already in DataReaderAccess
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#connect()
	 */
	public boolean connect() {
		boolean success = false;

		ConnectionCreator creator = new ConnectionCreator(DEFAULT_FILE_PATH);
		connection = creator.getConnection();

		if (connection != null) {

			// Open file
			fileHandler.openFile();

			// Try to connect
			success = connection.connect();
		}

		// Check if connection succeeded
		if (success) {

			currentlyOpenConnection = ConnectionHelper.currentEnsuredConnection;

			// If Ensured connection was not found, try with current ID
			if (currentlyOpenConnection == null) {
				currentlyOpenConnection = ConnectionHelper
						.getConnectionWithCurrentID();
			}

			// Set connection to use mode
			if (currentlyOpenConnection != null) {
				currentlyOpenConnection.useConnection(true);

				// Add status listener to the connection
				if (currentlyOpenConnection instanceof IConnection2) {
					currentConnectionStatusListener = new ConnectionStatusListener();
					((IConnection2) currentlyOpenConnection)
							.addStatusChangedListener(currentConnectionStatusListener);
				}

				// Notifying possible clients interested in connect event
				TraceViewerAPI2Impl.notifyConnection(currentlyOpenConnection);
			}

			// Start using feature
			FeatureUseTrackerPlugin.getFeatureUseProxy().startUsingFeature(
					FeatureUseTrackerConsts.CARBIDE_OST_TRACE);

			// Connection failed, set internal file handler back
		} else {
			fileHandler = new InternalFileHandler(DEFAULT_FILE_PATH);
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#disconnect()
	 */
	public boolean disconnect() {
		boolean success = false;

		// Disconnect and set internal file handler
		success = disconnectAndSetFileHandler();

		// Notifying possible clients interested in disconnect event
		if (success) {
			TraceViewerAPI2Impl.notifyDisconnection();
		}

		return success;
	}

	/**
	 * Create the main trace file
	 */
	private void createMainFile() {
		// Open the file and clear it
		fileHandler.openFile();
		fileHandler.clearFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#shutdown()
	 */
	public void shutdown() {
		// Disconnect
		disconnect();

		// Flush logger
		dataProcessorAccess.getLogger().flush();

		// Dispose search dialog
		dataProcessorAccess.getSearchProcessor().disposeSearchDialog();

		// Stop Dictionary file changed listener
		if (view != null) {
			((ReloadDecodeFilesAction) (view.getActionFactory()
					.getReloadDecodeFilesAction())).stopFileMonitor();
		}

		// Clear data
		clearAllData();

		// Unregister event listener
		eventListener.unregister();

		// Export general configurations
		if (TraceViewerPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.CONFIGURATION_FILE).equals(
				PreferenceConstants.DEFAULT_CONFIGURATION_FILE)
				&& view != null) {

			XMLGeneralConfigurationExporter exporter = new XMLGeneralConfigurationExporter(
					TraceViewerPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.CONFIGURATION_FILE),
					true);
			exporter.export();
		} // noelse, don't export to elsewhere than default configuration file

		setTraceView(null);
	}

	/**
	 * Disconnects and sets internal file handler
	 * 
	 * @return true if disconnected
	 */
	private boolean disconnectAndSetFileHandler() {
		boolean success = false;

		// Disconnect and close the file
		if (connection != null) {
			connection.disconnect();
			fileHandler.closeFile();
			connection = null;

			// Set internal file handler back
			fileHandler = new InternalFileHandler(DEFAULT_FILE_PATH);
			success = true;

			// Set connection to free mode
			if (currentlyOpenConnection != null) {
				currentlyOpenConnection.useConnection(false);

				// Remove status listener from the connection
				if (currentlyOpenConnection instanceof IConnection2) {
					((IConnection2) currentlyOpenConnection)
							.removeStatusChangedListener(currentConnectionStatusListener);
				}
			}
		}

		// Stop using feature
		FeatureUseTrackerPlugin.getFeatureUseProxy().stopUsingFeature(
				FeatureUseTrackerConsts.CARBIDE_OST_TRACE);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#clearAllData()
	 */
	public void clearAllData() {
		createMainFile();
		if (dataReaderAccess.getMainDataReader() != null) {
			dataReaderAccess.getMainDataReader().clearFile();
		}

		// If log file opened, unload it
		if (dataProcessorAccess.getLogger().isLogFileOpened()) {
			dataReaderAccess.createMainDataReader();
			dataProcessorAccess.getLogger().setLogFileOpened(false);
		}

		dataReaderAccess.setFileStartOffset(0);

		// Clear possible filter
		clearFilterFile();

		// Clear trace comments
		dataProcessorAccess.getTraceCommentHandler().getComments().clear();

		// Empty views
		emptyViews();
	}

	/**
	 * Clears filter file
	 */
	private void clearFilterFile() {
		if (dataProcessorAccess.getFilterProcessor().isFiltering()) {
			if (dataReaderAccess.getCurrentDataReader() != null
					&& dataReaderAccess.getMainDataReader() != dataReaderAccess
							.getCurrentDataReader()) {

				// Clear current filter file and create filter writer and reader
				// again
				dataReaderAccess.getCurrentDataReader().clearFile();
				dataProcessorAccess.getFilterProcessor()
						.buildFilterFileWriter();
				dataReaderAccess.createFilterDataReader();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getView()
	 */
	public TraceViewerTraceViewInterface getView() {
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getView()
	 */
	public TraceViewerPropertyViewInterface getPropertyView() {
		return propertyView;
	}

	/**
	 * Empties both views
	 */
	public void emptyViews() {
		if (view != null) {
			view.clearAll();
		}

		if (propertyView != null) {
			propertyView.clearAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getDialogs()
	 */
	public TraceViewerDialogInterface getDialogs() {
		return dialogs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getFileHandler()
	 */
	public TraceFileHandler getFileHandler() {
		return fileHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerInterface#setFileHandler(com.
	 * nokia.traceviewer.engine.TraceFileHandler)
	 */
	public void setFileHandler(TraceFileHandler fileHandler) {
		this.fileHandler = fileHandler;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerInterface#getTraces(int,
	 * int)
	 */
	public List<TraceProperties> getTraces(int start, int end) {
		if (traceFetcher == null) {
			traceFetcher = new TraceFetcher();
		}
		List<TraceProperties> traces = traceFetcher.startGatheringData(start,
				end);
		return traces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerInterface#readDataFileFromBeginning
	 * ()
	 */
	public void readDataFileFromBeginning() {
		// Clear possible filter
		clearFilterFile();

		emptyViews();

		dataReaderAccess.getMainDataReader().setFilePosition(0);
	}
}
