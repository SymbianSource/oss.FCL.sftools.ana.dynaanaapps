package com.nokia.carbide.cpp.pi.perfcounters;

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
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.internal.perfcounters.PecGuiManager;
import com.nokia.carbide.cpp.pi.internal.perfcounters.PecSample;
import com.nokia.carbide.cpp.pi.internal.perfcounters.PecTrace;
import com.nokia.carbide.cpp.pi.internal.perfcounters.PecTraceParser;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
/**
 * The activator class controls the plug-in life cycle
 */
public class PecPlugin extends AbstractPiPlugin implements ITrace, IEventListener, IVisualizable, IFinalizeTrace, IClassReplacer {

	/** number of graphs created (one on each page)*/
	public static final int GRAPH_COUNT = 3;

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.nokia.carbide.cpp.pi.perfcounters"; //$NON-NLS-1$

	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".perfcounters";  //$NON-NLS-1$
	/** context help id of the main page */
	public static final String HELP_CONTEXT_ID_MAIN_PAGE = HELP_CONTEXT_ID + ".pecPageContext";  //$NON-NLS-1$
	
	/** the trace id for PEC traces */
	private static final int PEC_TRACE_ID = 10;
	
	/** Persistable preference for the performance counter trace draw mode (show single graph or all graphs)  */
	public static final String PECTRACE_DRAWMODE = "com.nokia.carbide.cpp.pi.perfcounters.drawmode";//$NON-NLS-1$
	/** draw mode for showing all graphs */
	public static final String PECTRACE_DRAWMODE_SHOW_ALL = "ShowAll";//$NON-NLS-1$

	// The shared instance
	private static PecPlugin plugin;
	
	private static final Map<Integer, PecGuiManager> guiManagerMap = new HashMap<Integer, PecGuiManager>();

	/**
	 * The constructor
	 */
	public PecPlugin() {
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
	public static PecPlugin getDefault() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceClass()
	 */
	public Class getTraceClass() {
		return PecTrace.class;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#initialiseTrace(com.nokia.carbide.cpp.internal.pi.model.GenericTrace)
	 */
	public void initialiseTrace(GenericTrace trace) {
		if (!(trace instanceof PecTrace)){
			throw new IllegalArgumentException();
		}
			
		PecTrace parsedTrace = (PecTrace)trace;
		
		int samplingInterval = 1; //in milliseconds
		if (parsedTrace.samples.size() > 2) {
			samplingInterval = (int) ((parsedTrace.samples.get(1)).sampleSynchTime - (parsedTrace.samples.get(0)).sampleSynchTime); 
		}
		parsedTrace.setSamplingInterval(samplingInterval);
		
		
		NpiInstanceRepository.getInstance().activeUidAddTrace(PLUGIN_ID, trace);
		
		//create the GUI class which manages the graphs
		int uid = NpiInstanceRepository.getInstance().activeUid();
		guiManagerMap.put(uid, new PecGuiManager(uid, parsedTrace, GRAPH_COUNT, Messages.PecPlugin_0));
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceName()
	 */
	public String getTraceName() {
		return "PEC"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.PecPlugin_1;
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
		PecTraceParser p = new PecTraceParser(true);
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
		PecTrace trace = (PecTrace) NpiInstanceRepository.getInstance().activeUidGetTrace(PLUGIN_ID);
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
		PecGuiManager guiManager = guiManagerMap.get(uid);
		
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
		//no-op
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer#getReplacedClass(java.lang.String)
	 */
	public Class getReplacedClass(String className) {
		if (className.startsWith("com.nokia.carbide.cpp.pi.internal.perfcounters.PecTrace")){ //$NON-NLS-1$
			return PecTrace.class;
		} else if (className.startsWith("com.nokia.carbide.cpp.pi.internal.perfcounters.PecSample")){//$NON-NLS-1$
			return PecSample.class;
		}
		return null;
	}

}
