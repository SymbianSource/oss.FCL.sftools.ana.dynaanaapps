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
 * A singleton access point to functionality of Trace Builder
 *
 */
package com.nokia.tracebuilder.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.event.EventEngine;
import com.nokia.tracebuilder.engine.plugin.PluginEngine;
import com.nokia.tracebuilder.engine.project.ProjectEngine;
import com.nokia.tracebuilder.engine.propertydialog.DeleteConstantCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteConstantTableCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteGroupCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteMultipleTracesCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteObjectCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteParameterCallback;
import com.nokia.tracebuilder.engine.propertydialog.DeleteTraceCallback;
import com.nokia.tracebuilder.engine.propertydialog.InstrumentationEngine;
import com.nokia.tracebuilder.engine.propertydialog.PropertyDialogEngine;
import com.nokia.tracebuilder.engine.propertydialog.RunInstrumenterCallback;
import com.nokia.tracebuilder.engine.propertyfile.PropertyFileEngine;
import com.nokia.tracebuilder.engine.rules.RulesEngine;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.engine.source.SourceListener;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;
import com.nokia.tracebuilder.plugin.TraceBuilderPlugin;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.project.GroupNameHandlerOSTv2;
import com.nokia.tracebuilder.source.OffsetLength;
import com.nokia.tracebuilder.source.SourceContext;
import com.nokia.tracebuilder.source.StringValuePair;

/**
 * A singleton access point to functionality of Trace Builder
 * 
 */
final class TraceBuilder implements SourceListener, TraceBuilderInterface {

	/**
	 * Trace builder plug-in engine
	 */
	private PluginEngine pluginEngine;

	/**
	 * Property dialog logic
	 */
	private PropertyDialogEngine propertyDialogEngine;

	/**
	 * Event handler interface implementation
	 */
	private EventEngine eventEngine;

	/**
	 * Source editor engine
	 */
	private SourceEngine sourceEngine;
	/**
	 * Trace project engine
	 */
	private ProjectEngine projectEngine;

	/**
	 * Rule engine
	 */
	private RulesEngine rulesEngine;

	/**
	 * Trace property file engine
	 */
	private PropertyFileEngine propertyFileEngine;

	/**
	 * Instrumentation engine
	 */
	private InstrumentationEngine instrumentationEngine;

	/**
	 * Source context manager
	 */
	private SourceContextManager contextManager;

	/**
	 * Location map
	 */
	private TraceLocationMap locationMap;

	/**
	 * Location converter
	 */
	private TraceLocationConverter locationConverter;

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Interface to the view
	 */
	private TraceBuilderView view = new ViewAdapter();

	/**
	 * The trace object selected from the view
	 */
	private TraceObject selectedObject;

	/**
	 * Selected trace location
	 */
	private TraceLocation selectedLocation;

	/**
	 * Configuration
	 */
	private ConfigurationDelegate configurationDelegate;

	/**
	 * Flag which is set when a selection is done from the source. It prevents
	 * the selection event from delegating back to the source
	 */
	private boolean selectionFromSource;

	/**
	 * List of engines that need project open / export / close notifications
	 */
	private ArrayList<TraceBuilderEngine> engines = new ArrayList<TraceBuilderEngine>();

	/**
	 * View has been registered flag
	 */
	private boolean isViewRegistered = false;

	/**
	 * Project path is stored in case the view unregisters and re-registers
	 */
	private String currentProjectPath;

	/**
	 * Project name is stored in case the view unregisters and re-registers
	 */
	private String currentProjectName;

	/**
	 * List of components
	 */
	private ArrayList<SoftwareComponent> softwareComponents = new ArrayList<SoftwareComponent>();

	/**
	 * Software component index
	 */
	private int currentSoftwareComponentIndex = 0;

	/**
	 * Previous software component name
	 */
	private String previousSoftwareComponentName;

	/**
	 * Project monitor
	 */
	private TraceProjectMonitorInterface projectMonitor;
	
	/**
	 * Group name handler
	 */
	private GroupNameHandlerBase groupNameHandler;

	/**
	 * Trace header extension
	 */
	String TRACE_HEADER_EXTENSION = "Traces.h"; //$NON-NLS-1$	

	/**
	 * Gets the configuration of Trace Builder
	 * 
	 * @return the configuration
	 */
	TraceBuilderConfiguration getConfiguration() {
		return configurationDelegate;
	}

	/**
	 * Gets the trace builder interface
	 * 
	 * @return this object
	 */
	TraceBuilderInterface getTraceBuilder() {
		return this;
	}

	/**
	 * Gets the dialogs interface
	 * 
	 * @return the dialogs
	 */
	TraceBuilderDialogs getDialogs() {
		return view.getDialogs();
	}

	/**
	 * Gets the trace model
	 * 
	 * @return the trace model
	 */
	TraceModel getModel() {
		return model;
	}

	/**
	 * Gets the source engine
	 * 
	 * @return the source engine
	 */
	SourceEngine getSourceEngine() {
		return sourceEngine;
	}

	/**
	 * Gets the events interface
	 * 
	 * @return the events interface
	 */
	TraceBuilderEvents getEvents() {
		return eventEngine;
	}

	/**
	 * Adds a plug-in
	 * 
	 * @param plugin
	 *            the plugin
	 */
	void registerPlugin(TraceBuilderPlugin plugin) {
		pluginEngine.add(plugin);
	}

	/**
	 * Removes an existing plug-in
	 * 
	 * @param plugin
	 *            the plugin
	 */
	void unregisterPlugin(TraceBuilderPlugin plugin) {
		pluginEngine.remove(plugin);
	}

	/**
	 * Gets the source context manager
	 * 
	 * @return the context manager
	 */
	public SourceContextManager getSourceContextManager() {
		return contextManager;
	}

	/**
	 * Gets the actions interface
	 * 
	 * @return actions
	 */
	TraceBuilderActions getActions() {
		return view.getActions();
	}

	/**
	 * Starts TraceBuilder.
	 */
	void start() {
		configurationDelegate = new ConfigurationDelegate();
		
		// Default OST version in OSTv2
		groupNameHandler = new GroupNameHandlerOSTv2();
		rulesEngine = new RulesEngine();
		pluginEngine = new PluginEngine();
		model = new TraceModel(rulesEngine, pluginEngine.getVerifier());
		pluginEngine.setModel(model);
		propertyDialogEngine = new PropertyDialogEngine(model, rulesEngine
				.getPropertyDialogConfiguration());
		projectEngine = new ProjectEngine(model);
		propertyFileEngine = new PropertyFileEngine(model);
		eventEngine = new EventEngine(model);
		sourceEngine = new SourceEngine(model);
		locationMap = new TraceLocationMap(model);
		locationConverter = new TraceLocationConverter(model,
				propertyDialogEngine);
		sourceEngine.addSourceListener(this);
		contextManager = new SourceContextManagerImpl(sourceEngine);
		instrumentationEngine = new InstrumentationEngine(model);
		// Keep this order -> Exports need to be in correct order
		// Property file engine manages the ID cache so it needs to be run
		// before plug-in's and header
		engines.add(projectEngine);
		engines.add(propertyFileEngine);
		engines.add(pluginEngine);
	}

	/**
	 * Shuts down trace builder
	 */
	void shutdown() {
		closeProject();
	}

	/**
	 * Gets the current selection
	 * 
	 * @return the selection
	 */
	public TraceObject getSelectedObject() {
		return selectedObject;
	}

	/**
	 * Runs an asynchronous operation
	 * 
	 * @param runner
	 *            the operation to run
	 */
	void runAsyncOperation(Runnable runner) {
		view.runAsyncOperation(runner);
	}

	/**
	 * Registers the view
	 * 
	 * @param view
	 *            the view
	 */
	void setView(TraceBuilderView view) {
		if (view == null) {
			// Closes the project, but leaves the currentProjectName and
			// currentProjectPath variables. When view re-registers the project
			// is opened back
			internalCloseProject();
			this.view = new ViewAdapter();
			isViewRegistered = false;
			if (projectMonitor != null) {
				projectMonitor.stopMonitor();
			}
		} else {
			this.view = view;
			isViewRegistered = true;
			if (projectMonitor != null) {
				projectMonitor.startMonitor();
			}
		}
		propertyDialogEngine.setPropertyDialog(this.view.getPropertyDialog());
		configurationDelegate.setConfiguration(this.view.getConfiguration());
		// Tries to open project when view registers
		if (isViewRegistered && currentProjectName != null
				&& currentProjectPath != null) {
			this.view.runAsyncOperation(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					try {
						openProject(currentProjectName);
					} catch (TraceBuilderException e) {
					}
				}

			});
		}
	}

	/**
	 * Is view registered
	 * 
	 * @return true if view is registered false if view is not registered
	 */
	boolean isViewRegistered() {
		return isViewRegistered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#
	 * traceObjectSelected(com.nokia.tracebuilder.model.TraceObject, boolean,
	 * boolean)
	 */
	public void traceObjectSelected(TraceObject object, boolean syncView,
			boolean sourceOpened) {

		// Active object is stored
		selectedObject = object;
		selectedLocation = null;

		// If a trace or parameter was selected from view, the source
		// jumps to the trace in source code if there is one reference
		// to the trace. If there are multiple references, the actual
		// location needs to be selected from the location list
		Trace trace = null;
		if (object instanceof Trace) {
			trace = (Trace) selectedObject;
		} else if (object instanceof TraceParameter) {
			trace = ((TraceParameter) selectedObject).getTrace();
		}
		if (trace != null) {
			TraceLocationList list = null;
			
			// If source file was opened same time as trace was selected. Use
			// trace from model, because trace from selected object is not up to
			// date
			if (sourceOpened) {
				Trace traceFromModel = model.findTraceByName(trace.getName());
				list = traceFromModel.getExtension(TraceLocationList.class);
			} else {
				list = trace.getExtension(TraceLocationList.class);
			}
			if (list != null && list.hasLocations()) {
				((TraceLocation) list.iterator().next()).selectFromSource();

			}
		}

		if (syncView && selectedObject != null) {
			view.selectObject(selectedObject);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#
	 * locationSelected(com.nokia.tracebuilder.engine.TraceLocationList,
	 * com.nokia.tracebuilder.engine.TraceLocation, boolean)
	 */
	public void locationSelected(TraceLocationList list,
			TraceLocation location, boolean syncView) {
		if (location != null) {
			// Activates the trace owning the location
			selectedObject = location.getTrace();
			selectedLocation = location;
			// selectionFromSource flag prevents looped updates
			// from view to source and back
			if (!selectionFromSource) {
				// Jumps to the selected location
				location.selectFromSource();
			} else {
				selectionFromSource = false;
			}
		} else if (list != null) {
			// Activates the trace owning the location list
			selectedObject = list.getOwner();
			selectedLocation = null;
			if (selectedObject != null) {
				if (list.hasLocations()) {
					TraceLocation loc = (TraceLocation) list.iterator().next();
					loc.selectFromSource();
					selectedLocation = loc;
				}
			}
		} else {
			selectedObject = null;
			selectedLocation = null;
		}
		if (syncView && selectedLocation != null) {
			view.selectLocation(selectedLocation);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#addTrace()
	 */
	public void addTrace() throws TraceBuilderException {
		checkIsModelValid();
		addTrace(getOwningTraceGroup(selectedObject));
	}

	/**
	 * Creates a trace and adds it to source
	 * 
	 * @param groupProposal
	 *            the group proposal for add trace dialog
	 * @throws TraceBuilderException
	 *             if creation fails
	 */
	private void addTrace(TraceGroup groupProposal)
			throws TraceBuilderException {
		Trace trace = null;
		// If the context is fully selected, the start and end traces are
		// automatically added
		String name = null;
		String value = null;
		// If adding to source, the context is checked.
		SourceContext context = sourceEngine.getSelectedContext();
		if (context == null) {
			// If context was not valid, the default values are used
			// The cursor location validity is checked before showing dialog.
			sourceEngine.checkCursorLocationValidity();
		} else {
			// If context is valid, the name and value proposal are based on it
			String cname = context.getClassName();
			String fname = context.getFunctionName();
			name = TraceUtils.formatTrace(TraceUtils.getDefaultNameFormat(),
					cname, fname);
			value = TraceUtils.formatTrace(TraceUtils.getDefaultTraceFormat(),
					cname, fname);
		}
		trace = propertyDialogEngine.showAddTraceDialog(groupProposal, name,
				value, null);
		if (trace != null) {
			int insertLocation = sourceEngine.addTrace(trace);
			TraceUtils.multiplyTrace(trace, insertLocation, sourceEngine);
			addInclude();
			view.selectObject(trace);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#convertTrace()
	 */
	public void convertTrace() throws TraceBuilderException {
		checkIsModelValid();
		if (selectedLocation != null && selectedLocation.getTrace() == null) {
			TraceLocation converted = selectedLocation;
			Trace trace = locationConverter.convertLocation(selectedLocation);
			if (trace != null) {
				// The existing location is removed and the new trace inserted
				sourceEngine.replaceLocationWithTrace(trace, converted);
				addInclude();
				view.selectObject(trace);
			}
		} else {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"No trace to convert", null); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Checks that model is valid
	 * 
	 * @throws TraceBuilderException
	 *             if model is not valid
	 */
	private void checkIsModelValid() throws TraceBuilderException {
		if (!model.isValid()) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.MODEL_NOT_READY);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#addParameter()
	 */
	public void addParameter() throws TraceBuilderException {
		checkIsModelValid();
		if (selectedObject instanceof Trace) {
			TraceParameter parameter = propertyDialogEngine
					.showAddParameterDialog((Trace) selectedObject, null);
			if (parameter != null) {
				sourceEngine.updateTrace(parameter.getTrace());
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.TRACE_NOT_SELECTED, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#addConstant()
	 */
	public void addConstant() throws TraceBuilderException {
		checkIsModelValid();
		TraceConstantTable table = null;
		if (selectedObject instanceof TraceConstantTable) {
			table = (TraceConstantTable) selectedObject;
		} else if (model.hasConstantTables()) {
			table = model.getConstantTables().next();
		}
		propertyDialogEngine.showAddConstantDialog(table, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#addConstant()
	 */
	public void selectComponent() throws TraceBuilderException {
		propertyDialogEngine.showSelectComponentDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#delete()
	 */
	public void delete() throws TraceBuilderException {
		checkIsModelValid();
		DeleteObjectCallback callback;
		// Creates a callback object based on object type
		if (selectedObject instanceof TraceGroup) {
			callback = new DeleteGroupCallback((TraceGroup) selectedObject,
					sourceEngine);
		} else if (selectedObject instanceof Trace) {
			callback = new DeleteTraceCallback((Trace) selectedObject,
					sourceEngine);
		} else if (selectedObject instanceof TraceParameter) {
			callback = new DeleteParameterCallback(
					(TraceParameter) selectedObject);
		} else if (selectedObject instanceof TraceConstantTableEntry) {
			callback = new DeleteConstantCallback(
					(TraceConstantTableEntry) selectedObject);
		} else if (selectedObject instanceof TraceConstantTable) {
			callback = new DeleteConstantTableCallback(
					(TraceConstantTable) selectedObject);
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.CANNOT_DELETE_SELECTED_OBJECT);
		}
		int res = callback.delete();
		if (res == TraceBuilderDialogs.OK) {
			getActions().enableActions(null);
			selectedObject = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderInterface#deleteMultipleTraces
	 * ()
	 */
	public void deleteMultipleTraces() throws TraceBuilderException {
		checkIsModelValid();
		DeleteMultipleTracesCallback callback = new DeleteMultipleTracesCallback(
				model, sourceEngine, instrumentationEngine);
		int res = callback.delete();
		if (res == TraceBuilderDialogs.OK) {
			getActions().enableActions(null);
			selectedObject = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#
	 * createConstantTableFromSelection()
	 */
	public void createConstantTableFromSelection() throws TraceBuilderException {
		checkIsModelValid();
		ArrayList<StringValuePair> list = new ArrayList<StringValuePair>();
		SourceProperties props = sourceEngine.getSelectedSource();
		if (props != null) {
			OffsetLength selection = sourceEngine.getSelection(props);
			String type;
			try {
				type = props.getSourceEditor()
						.parseEnum(selection.offset, list);
			} catch (Exception e) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.CONSTANT_TABLE_PARSE_FAILED);
			}

			// If list size is zero, then enum parse has failed
			if (list.size() == 0) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.INVALID_CONSTANT_TABLE_NAME);
			}

			int id = model.getNextConstantTableID();
			pluginEngine.getVerifier().checkConstantTableProperties(model,
					null, id, type);
			TraceConstantTable table = null;
			model.startProcessing();
			try {
				table = model.getFactory().createConstantTable(id, type, null);
				// This needs to be trapped so that the tables gets removed
				// if something fails
				for (int i = 0; i < list.size(); i++) {
					StringValuePair pair = list.get(i);
					pluginEngine.getVerifier().checkConstantProperties(table,
							null, pair.value, pair.string);
					model.getFactory().createConstantTableEntry(table,
							pair.value, pair.string, null);
				}
			} catch (TraceBuilderException e) {
				model.removeConstantTable(table);
				throw e;
			} finally {
				model.processingComplete();
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.CONSTANT_TABLE_NOT_PART_OF_PROJECT,
					false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderInterface#removeFromSource()
	 */
	public void removeFromSource() throws TraceBuilderException {
		if (selectedLocation != null) {
			Trace trace = selectedLocation.getTrace();
			sourceEngine.removeLocation(selectedLocation);
			selectedLocation = null;
			selectedObject = null;
			if (trace != null) {
				view.selectObject(trace);
			} else {
				if (model.hasGroups()) {
					view.selectObject(model.getGroupAt(0));
				}
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.LOCATION_NOT_SELECTED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderInterface#removeUnrelatedFromSource
	 * ()
	 */
	public void removeUnrelatedFromSource() throws TraceBuilderException {
		TraceLocationList list = locationMap.getUnrelatedTraces();
		for (LocationProperties loc : list) {
			sourceEngine.removeLocation((TraceLocation) loc);
		}
		if (model.hasGroups()) {
			view.selectObject(model.getGroupAt(0));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#showProperties()
	 */
	public void showProperties() throws TraceBuilderException {
		checkIsModelValid();
		if (selectedObject != null) {
			// The view flags use the ADD types instead of EDIT types
			if (selectedObject instanceof TraceGroup) {
				propertyDialogEngine
						.showEditGroupDialog((TraceGroup) selectedObject);
			} else if (selectedObject instanceof Trace) {
				propertyDialogEngine
						.showEditTraceDialog((Trace) selectedObject);
			} else if (selectedObject instanceof TraceConstantTable) {
				propertyDialogEngine
						.showEditConstantTableDialog((TraceConstantTable) selectedObject);
			} else if (selectedObject instanceof TraceConstantTableEntry) {
				propertyDialogEngine
						.showEditConstantDialog((TraceConstantTableEntry) selectedObject);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#focusView()
	 */
	public void switchFocus() {
		if (view != null && sourceEngine != null) {
			if (view.hasFocus()) {
				sourceEngine.setFocus();
			} else {
				view.setFocus();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderInterface#openProject(java.
	 * lang.String, java.lang.String)
	 */
	public void openProject(String modelName) throws TraceBuilderException {
		try {
			currentProjectName = modelName;
			if (view != null && isViewRegistered && !model.isValid()) {
				projectEngine.openTraceProject(currentProjectPath, modelName);
				for (TraceBuilderEngine engine : engines) {
					engine.projectOpened();
				}
				model.setValid(true);
				sourceEngine.start();
			}
		} catch (TraceBuilderException e) {
			closeModel();
			throw e;
		}
		// Updates the action configuration
		getActions().enableActions(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#exportProject()
	 */
	public void exportProject() throws TraceBuilderException {
		checkIsModelValid();
		if (model.getID() != 0) {
			for (TraceBuilderEngine engine : engines) {
				engine.exportProject();
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.INVALID_MODEL_PROPERTIES_FOR_EXPORT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#closeProject()
	 */
	public void closeProject() {
		internalCloseProject();
		currentProjectPath = null;
		currentProjectName = null;
	}

	/**
	 * Closes the project
	 */
	private void internalCloseProject() {
		if (model.isValid()) {
			if (!TraceBuilderGlobals.getConfiguration().getFlag(
					TraceBuilderConfiguration.CONSOLE_UI_MODE)) {
				try {
					// Plug-in's are not exported when project is closed
					// They can do cleanup in traceProjectClosed call
					for (TraceBuilderEngine engine : engines) {
						if (engine != pluginEngine) {
							engine.exportProject();
						}
					}
				} catch (TraceBuilderException e) {
					getEvents().postError(e);
				}
			}
			// Engines are closed in reverse order
			for (int i = engines.size() - 1; i >= 0; i--) {
				engines.get(i).projectClosed();
			}
			sourceEngine.shutdown();
			closeModel();
		}
		// Updates the action configuration
		getActions().enableActions(null);
	}

	/**
	 * Closes the trace model
	 */
	private void closeModel() {
		try {
			if (model != null) {
				model.reset();
				model.setValid(false);
			}
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				getEvents().postAssertionFailed("Failed to close model", e); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.engine.TraceBuilderInterface#startInstrumenter()
	 */
	public void startInstrumenter() throws TraceBuilderException {
		checkIsModelValid();
		instrumentationEngine.checkSourceFunctions(sourceEngine);
		propertyDialogEngine.showInstrumenterDialog(
				getOwningTraceGroup(selectedObject), null,
				new RunInstrumenterCallback(model, sourceEngine,
						contextManager, instrumentationEngine
								.getNewInstrumenterID()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceListener#
	 * selectionChanged(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void selectionChanged(TraceLocation location) {
		if (view != null) {
			selectionFromSource = true;
			view.selectLocation(location);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceListener#
	 * sourceOpened(com.nokia.tracebuilder.engine.SourceProperties)
	 */
	public void sourceOpened(SourceProperties properties) {
		locationMap.addSource(properties);
		locationConverter.parseTracesFromSource(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceListener#
	 * sourceChanged(com.nokia.tracebuilder.engine.SourceProperties)
	 */
	public void sourceChanged(SourceProperties properties) {
		locationMap.updateSource(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceListener#
	 * sourceProcessingStarted
	 * (com.nokia.tracebuilder.engine.source.SourceProperties)
	 */
	public void sourceProcessingStarted(SourceProperties properties) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceListener#
	 * sourceProcessingComplete
	 * (com.nokia.tracebuilder.engine.source.SourceProperties)
	 */
	public void sourceProcessingComplete(SourceProperties properties) {
		sourceChanged(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceListener#
	 * sourceClosed(com.nokia.tracebuilder.engine.SourceProperties)
	 */
	public void sourceClosed(SourceProperties properties) {
		locationMap.removeSource(properties, null, null);
		locationConverter.sourceClosed(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceListener#
	 * sourceSaved(com.nokia.tracebuilder.engine.source.SourceProperties)
	 */
	public void sourceSaved(SourceProperties properties) {
		String selectedObjectName = null;

		// If selected object is trace, save trace name so we can use it to
		// return selection after saving operation
		if (selectedObject instanceof Trace && selectedObject != null) {
			selectedObjectName = selectedObject.getName();
		}

		locationConverter.sourceSaved(properties);

		// If selected object before saving operation was trace, return
		// selection
		if (selectedObjectName != null) {
			selectedObject = model.findTraceByName(selectedObjectName);
		}
	}

	/**
	 * Gets the trace group which owns the given object
	 * 
	 * @param object
	 *            the object to be checked
	 * @return the group
	 */
	private TraceGroup getOwningTraceGroup(TraceObject object) {
		TraceGroup owner = null;
		if (object instanceof Trace) {
			owner = ((Trace) object).getGroup();
		} else if (object instanceof TraceGroup) {
			owner = (TraceGroup) object;
		} else if (object instanceof TraceParameter) {
			owner = getOwningTraceGroup(((TraceParameter) object).getTrace());
		}
		return owner;
	}

	/**
	 * Adds the include statement to source
	 */
	private void addInclude() {
		// Adds the include statement if it is not yet in source
		SourceProperties properties = sourceEngine.getSelectedSource();
		String fileName = getHeaderFileName(properties.getFileName());
		if (fileName != null) {
			sourceEngine.addInclude(properties, fileName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderInterface#exportProject()
	 */
	public Iterator<SourceProperties> getOpenSources() {
		return sourceEngine.getSources();

	}

	/**
	 * Get the view
	 * 
	 * @return the view
	 */
	public TraceBuilderView getView() {
		return view;
	}

	/**
	 * Set current software component index
	 * 
	 * @param componentIndex
	 *            software component index
	 */
	public void setCurrentSoftwareComponentIndex(int componentIndex) {
		this.currentSoftwareComponentIndex = componentIndex;
	}

	/**
	 * Get software component Id
	 * 
	 * @return the software component Id as string
	 */
	public String getCurrentSoftwareComponentId() {
		String softwareComponentId = null;
		if (softwareComponents.size() > 0) {
			softwareComponentId = softwareComponents.get(
					currentSoftwareComponentIndex).getId();
		}
		return softwareComponentId;
	}

	/**
	 * Get software component name
	 * 
	 * @return the software component name as string
	 */
	public String getCurrentSoftwareComponentName() {
		String softwareComponentName = null;
		if (softwareComponents.size() > 0) {
			softwareComponentName = softwareComponents.get(
					currentSoftwareComponentIndex).getName();
		}
		return softwareComponentName;
	}

	/**
	 * Add software component
	 * 
	 * @param softwareComponentId
	 *            software component id
	 * @param softwareComponentName
	 *            software component name
	 * @param mmpPath
	 *            components mmp path
	 */
	public void addSoftwareComponent(String softwareComponentId,
			String softwareComponentName, String mmpPath) {
		SoftwareComponent softwareComponent = new SoftwareComponent(
				softwareComponentId, softwareComponentName, mmpPath);
		softwareComponents.add(softwareComponent);
	}

	/**
	 * Clear software components
	 */
	public void clearSoftwareComponents() {
		previousSoftwareComponentName = getCurrentSoftwareComponentName();
		softwareComponents.clear();
	}

	/**
	 * Gets the software components
	 * 
	 * @return the software components iterator
	 */
	public Iterator<SoftwareComponent> getSoftwareComponents() {
		Iterator<SoftwareComponent> componentIterator = softwareComponents
				.iterator();
		return componentIterator;
	}

	/**
	 * Get current software component's MMP path
	 * 
	 * @return the current software component's MMP path
	 */
	public String getCurrentSoftwareComponentMMPPath() {
		String mmpPath = null;
		if (softwareComponents.size() > 0) {
			mmpPath = softwareComponents.get(currentSoftwareComponentIndex)
					.getMMPFilePath();
		}
		return mmpPath;
	}

	/**
	 * Get current software component index
	 * 
	 * @return current software component index
	 */
	public int getCurrentSoftwareComponentIndex() {
		return currentSoftwareComponentIndex;
	}

	/**
	 * Get previous software component name
	 * 
	 * @return previous software component name
	 */
	public String getPreviousSoftwareComponentName() {
		return previousSoftwareComponentName;
	}

	/**
	 * Get project path
	 * 
	 * @return project path
	 */
	public String getProjectPath() {
		return currentProjectPath;
	}

	/**
	 * Set project path
	 * 
	 * @param path
	 *            the path
	 */
	public void setProjectPath(String path) {
		currentProjectPath = path;
	}

	/**
	 * Set project monitor
	 * 
	 * @param projectMonitor
	 *            the project monitor
	 */
	public void setProjectMonitor(TraceProjectMonitorInterface projectMonitor) {
		this.projectMonitor = projectMonitor;
	}

	/**
	 * Gets the name for the trace header file based on given source
	 * 
	 * @param sourceFile
	 *            the source file name
	 * @return the header file name
	 */
	public String getHeaderFileName(String sourceFile) {
		String retval = null;
		if (model != null) {
			// The header file name is the source file name with extension
			// replaced by Traces.h
			File f = new File(sourceFile);
			retval = removeFileExtension(f.getName()) + TRACE_HEADER_EXTENSION;
		}
		return retval;
	}

	/**
	 * Removes the file extension from file name
	 * 
	 * @param fileName
	 *            the file name
	 * @return name without extension
	 */
	private String removeFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		int sep1 = fileName.lastIndexOf('/');
		int sep2 = fileName.lastIndexOf('\\');
		if (index > sep1 && index > sep2) {
			fileName = fileName.substring(0, index);
		}
		return fileName;
	}

	/**
	 * Get location converter
	 * 
	 * @return the location converter
	 */
	public TraceLocationConverter getLocationConverter() {
		return locationConverter;
	}

	/**
	 * Get location map
	 * 
	 * @return the location map
	 */
	public TraceLocationMap getLocationMap() {
		return locationMap;
	}

	/**
	 * Set group name handler
	 * 
	 */
	public void setGroupNameHandler(GroupNameHandlerBase groupNameHandler) {
		this.groupNameHandler = groupNameHandler;
	}	
	
	/**
	 * Get group name handler
	 * 
	 * @return the group name handler
	 */
	public GroupNameHandlerBase getGroupNameHandler() {
		return groupNameHandler;
	}
}
