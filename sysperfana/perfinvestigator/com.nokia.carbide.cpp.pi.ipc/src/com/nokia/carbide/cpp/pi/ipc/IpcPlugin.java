package com.nokia.carbide.cpp.pi.ipc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.internal.ipc.IpcSample;
import com.nokia.carbide.cpp.pi.internal.ipc.IpcTrace;
import com.nokia.carbide.cpp.pi.internal.ipc.IpcTraceParser;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonGuiManager;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;
/**
 * The activator class controls the plug-in life cycle
 */
public class IpcPlugin extends AbstractPiPlugin implements ITrace, IEventListener, IVisualizable, IFinalizeTrace, IClassReplacer {

	/** number of graphs created (one on each page)*/
	public static final int GRAPH_COUNT = 3;

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.ipc"; //$NON-NLS-1$
	
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".ipc";  //$NON-NLS-1$
	/** context help id of the main page */
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".ipcPageContext";  //$NON-NLS-1$

	/** the trace id for PEC traces */
	private static final int PEC_TRACE_ID = 12;
	
	/** Persistable preference for the performance counter trace draw mode (show single graph or all graphs)  */
	public static final String PECTRACE_DRAWMODE = "com.nokia.carbide.cpp.pi.ipc.drawmode";//$NON-NLS-1$
	/** draw mode for showing all graphs */
	public static final String PECTRACE_DRAWMODE_SHOW_ALL = "ShowAll";//$NON-NLS-1$

	// The shared instance
	private static IpcPlugin plugin;
	
	private static final Map<Integer, PecCommonGuiManager> guiManagerMap = new HashMap<Integer, PecCommonGuiManager>();

	/**
	 * The constructor
	 */
	public IpcPlugin() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static IpcPlugin getDefault() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceClass()
	 */
	public Class getTraceClass() {
		return IpcTrace.class;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#initialiseTrace(com.nokia.carbide.cpp.internal.pi.model.GenericTrace)
	 */
	public void initialiseTrace(GenericTrace trace) {
		if (!(trace instanceof IpcTrace)){
			throw new IllegalArgumentException();
		}
			
		IpcTrace parsedTrace = (IpcTrace)trace;
		
		int samplingInterval = 1; //in milliseconds
		if (parsedTrace.samples.size() > 2) {
			samplingInterval = (int) ((parsedTrace.samples.get(1)).sampleSynchTime - (parsedTrace.samples.get(0)).sampleSynchTime); 
		}
		parsedTrace.setSamplingInterval(samplingInterval);
		
		
		NpiInstanceRepository.getInstance().activeUidAddTrace(PLUGIN_ID, trace);
		
		//create the GUI class which manages the graphs
		int uid = NpiInstanceRepository.getInstance().activeUid();
		guiManagerMap.put(uid, new PecCommonGuiManager(uid, parsedTrace, IpcPlugin.GRAPH_COUNT, Messages.IpcPlugin_0));
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceName()
	 */
	public String getTraceName() {
		return "IPC"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.IpcPlugin_1;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceId()
	 */
	public int getTraceId() {
		return PEC_TRACE_ID;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFile(java.io.File)
	 */
	public ParsedTraceData parseTraceFile(File file) throws Exception {
		IpcTraceParser p = new IpcTraceParser();
		ParsedTraceData ptd = p.parse(file);
		
		return ptd;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener#receiveEvent(java.lang.String, org.eclipse.swt.widgets.Event)
	 */
	public void receiveEvent(String action, Event event) {
		//no-op
		
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#arePagesCreated()
	 */
	public boolean arePagesCreated() {
		return false; //PEC traces add graphs to existing pages
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#createPage(int)
	 */
	public ProfileVisualiser createPage(int index) {
		return null; //PEC traces add graphs to existing pages
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getCreatePageCount()
	 */
	public int getCreatePageCount() {
		return 0; //PEC traces don't create pages
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getCreatePageIndex(int)
	 */
	public int getCreatePageIndex(int index) {
		throw new UnsupportedOperationException(); //PEC traces don't create pages
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getDrawRequest(int)
	 */
	public GraphDrawRequest getDrawRequest(int graphIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getGraphCount()
	 */
	public int getGraphCount() {
		return GRAPH_COUNT;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getLastSample(int)
	 */
	public Integer getLastSample(int graphIndex) {
		IpcTrace trace = (IpcTrace) NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID);
		if(trace == null){
			return 0;
		}
		
		return trace.getLastSampleNumber();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getPageNumber(int)
	 */
	public int getPageNumber(int graphIndex) {
		if (graphIndex == 0) {
			return PIPageEditor.THREADS_PAGE;
		} else if (graphIndex == 1) {
			return PIPageEditor.BINARIES_PAGE;
		} else if (graphIndex == 2) {
			return PIPageEditor.FUNCTIONS_PAGE;
		}

		return PIPageEditor.NEXT_AVAILABLE_PAGE;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getTraceGraph(int)
	 */
	public IGenericTraceGraph getTraceGraph(int graphIndex) {
		int uid = NpiInstanceRepository.getInstance().activeUid();
		PecCommonGuiManager guiManager = guiManagerMap.get(uid);
		
		if (guiManager != null){
			return guiManager.getTraceGraph(graphIndex, HELP_CONTEXT_ID_MAIN_PAGE);			
		}
		
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#setPageIndex(int, int)
	 */
	public void setPageIndex(int index, int pageIndex) {
		//no-op
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#setPagesCreated(boolean)
	 */
	public void setPagesCreated(boolean pagesCreated) {
		//no-op
	}
	
	/**
	 * Returns a File corresponding to the given bundle relative path.
	 * @param path the bundle relative path to resource to locate
	 * @return the File corresponding to the given bundle relative path, or null
	 * @throws IOException
	 */
	public File locateFileInBundle(final String path) throws IOException {
		Bundle myBundle= getDefault().getBundle();
		IPath ppath= new Path(path);
		ppath= ppath.makeRelative();
		URL[] urls= FileLocator.findEntries(myBundle, ppath);
		if(urls.length != 1) {
			return null;
		}
		return new File(FileLocator.toFileURL(urls[0]).getFile());
	}

	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace#runOnDispose()
	 */
	public void runOnDispose() {
		// called when editor closes
		
		// Do any cleanup work here when the editor closes
		int uid = NpiInstanceRepository.getInstance().activeUid();
		guiManagerMap.remove(uid);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace#runOnPartOpened()
	 */
	public void runOnPartOpened() {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer#getReplacedClass(java.lang.String)
	 */
	public Class getReplacedClass(String className) {
		if (className.startsWith("com.nokia.carbide.cpp.pi.internal.ipc.IpcTrace")){ //$NON-NLS-1$
			return IpcTrace.class;
		} else if (className.startsWith("com.nokia.carbide.cpp.pi.internal.ipc.IpcSample")){//$NON-NLS-1$
			return IpcSample.class;
		}
		return null;
	}


}
