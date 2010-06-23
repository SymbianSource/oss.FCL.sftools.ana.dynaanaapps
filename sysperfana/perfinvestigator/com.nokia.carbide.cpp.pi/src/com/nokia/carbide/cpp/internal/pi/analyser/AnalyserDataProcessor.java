/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.IProgressService;

import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.RefinableTrace;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.test.IProvideTraceAdditionalInfo;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.util.PIExceptionRuntime;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;


/*
 * Class for abstracting data processing core routine for open and import
 */

public class AnalyserDataProcessor {
	protected static final int MAX_CPU = 4;
	// whether the profile file was read correctly
	public final static int STATE_OK				= 0;
	public final static int STATE_IMPORTING		= 1;
	public final static int STATE_OPENING			= 2;
	public final static int STATE_TIMESTAMP		= 3;
	public final static int STATE_CANCELED		= 4;
	public final static int STATE_INVALID			= 5;
	public final static int TOTAL_PROGRESS_COUNT	= 10000;

	static AnalyserDataProcessor instance = null;

	// following states should be cleanup for every run
	
	// There should be only one progress monitor used by data processor
	// regardless the task, and we don't want to expose public interface
	// that juggle around progress monitor, to avoid messy situation of
	// multiple progress indicator
	static IProgressMonitor mp = null;
	static Exception lastException = null;
	private static int analyserDataProcessorState = STATE_INVALID;
		
	private AnalyserDataProcessor() {
		// singleton
	}
	
	public static AnalyserDataProcessor getInstance() {
		if (instance == null) {
			instance = new AnalyserDataProcessor();
		}
		return instance;
	}
	
	// return whether the processed data file was successfully read
	public int getState() {
		return analyserDataProcessorState;
	}
	
	public void setImportFailed() {
		analyserDataProcessorState = STATE_INVALID;
	}
	
	private void setProgressMonitor (IProgressMonitor progressMonitor) {
		mp = progressMonitor;
	}
	
	private IProgressMonitor getProgressMonitor() {
		return mp;
	}
		
	public Exception getLastException() {
		return lastException;
	}
	
	private void importNewAnalysis(Hashtable<Integer,String> traceFileNames, int uid, List<ITrace> pluginsToUse) throws InterruptedException, InvocationTargetException {
		analyserDataProcessorState = STATE_IMPORTING;
		final int workUnitsForImport = TOTAL_PROGRESS_COUNT * 60 / 100;
		int workUnitsLeft = workUnitsForImport * 99 / 100;
		
		checkCancelledThrowIE();
			
		// loop through all the plugins associated with traces
		int numberOfPlugins = pluginsToUse.size();
		for (ITrace plugin : pluginsToUse) {
    		int traceId = plugin.getTraceId();
    		AbstractPiPlugin p = (AbstractPiPlugin)plugin;
    		
    		// map trace plugins to this analysis data
    		NpiInstanceRepository.getInstance().addPlugin(uid, p);
    		if (traceId != -1)
    		{
    			if (traceId == 1){ //support SMP by expecting one file per CPU
					List<File> files = new ArrayList<File>();
    				for (int i = 0; i < MAX_CPU; i++) {
    					int smpTraceId = traceId + i * 20;
    					{
    	        			String fileName = traceFileNames.get(smpTraceId);
    	        			if (fileName != null && fileName.endsWith(".dat")) //$NON-NLS-1$
    	        			{
    	        				File traceFile = new File(fileName);
    	        				if (traceFile.exists()){
    	        					files.add(traceFile);
    	        				}
    	        			}    						
    					}
    				}
					ProfileReader.getInstance().readTraceFile(plugin, files.toArray(new File[files.size()]), this,uid);
    				 
    			} else {
        			String fileName = traceFileNames.get(traceId);
        			if (fileName != null)
        			{
        				File traceFile = new File(fileName);
        				if (traceFile.exists()) 
    						if (traceFile.getName().endsWith(".dat"))  //$NON-NLS-1$
    							ProfileReader.getInstance().readTraceFile(plugin, traceFile, this,uid);
        			}    				
    			}
    		}
            // assume this load takes 39%
    		getProgressMonitor().worked((workUnitsForImport * 39 / 100) / numberOfPlugins);
    		checkCancelledThrowIE();
            workUnitsLeft -= (workUnitsForImport * 39 / 100) / numberOfPlugins;
    	}
		getProgressMonitor().worked((workUnitsForImport * 60 / 100) - workUnitsLeft);
		checkCancelledThrowIE();
		workUnitsLeft = (workUnitsForImport * 60 / 100);
    	
		// refine any traces whose addresses can be refined
		Iterator<ParsedTraceData> traces = TraceDataRepository.getInstance().getTraceCollectionIter(uid);
    	if (traces == null) {
    		throw new InvocationTargetException(new PIExceptionRuntime(Messages.getString("AnalyserDataProcessor.0") + uid + Messages.getString("AnalyserDataProcessor.1")));     //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	while (traces.hasNext())
    	{
    		ParsedTraceData trace = (ParsedTraceData)traces.next();
    		// for those trace data items that can be refined
    		if (trace.traceData instanceof RefinableTrace)
    		{
    			Iterator<ParsedTraceData> traces2 = TraceDataRepository.getInstance().getTraceCollectionIter(uid);
    		
    			while (traces2.hasNext())
    			{
    				ParsedTraceData trace2 = traces2.next();

    				// refine with other resolvers but not with own (which has been used already)
    				if (!trace2.equals(trace) && trace2.functionResolvers != null)
    				{
    					for (int i = 0; i < trace2.functionResolvers.length; i++)
    					{
    						FunctionResolver fr = trace2.functionResolvers[i];
    						System.out.println(Messages.getString("AnalyserDataProcessor.2") + trace.traceData.getClass().getName() + Messages.getString("AnalyserDataProcessor.3") + fr.getResolverString());     //$NON-NLS-1$ //$NON-NLS-2$

    						((RefinableTrace)trace.traceData).refineTrace(fr);
    					}
    				}
    				checkCancelledThrowIE();
    			}
    			
    			// after all refinement is done, do any final touches to the samples
    			((RefinableTrace)trace.traceData).finalizeTrace();
    		}
    		getProgressMonitor().worked(workUnitsLeft / 2);
    		checkCancelledThrowIE();
    		workUnitsLeft -= workUnitsLeft / 2;
    	}
    	// Should move resolver out of trace, PI only need resolver in import phase, trace is more of a
    	// core data in NPI file; before that, we would just strip all resolvers here
    	Iterator<ParsedTraceData> tracesItr = TraceDataRepository.getInstance().getTraceCollectionIter(uid);
    	while (tracesItr.hasNext()) {
    		ParsedTraceData ptd = tracesItr.next();
    		ptd.functionResolvers = null;
    	}
    	System.gc();
	}
	
	private void loadExistingAnalysis(final Composite parent, final String analysisFilename, final String displayName, final int uid) throws InvocationTargetException, InterruptedException {
		assertThrowITE(NpiInstanceRepository.getInstance().activeUid() == uid, Messages.getString("AnalyserDataProcessor.4"));   //$NON-NLS-1$
		
		// do the loading part with indicator in non-UI thread
		final IProgressMonitor pm = getProgressMonitor();
		pm.worked(1);
		class LoadRunnable implements Runnable {
			IOException myIOE = null;
			InterruptedException myIE  =null;
			
			public void handleException () throws InvocationTargetException, InterruptedException {
				if (myIOE != null) {
					String reason = Messages.getString("AnalyserDataProcessor.12") + analysisFilename; //$NON-NLS-1$
					if (myIOE.getMessage() != null) {
						reason += " " + myIOE.getMessage(); //$NON-NLS-1$
					}
					assertThrowITE(myIOE, reason);
				}
				if (myIE != null) {
					throw myIE;
				}
			}
			
			public void run() {
				try {
					ProfileReader.getInstance().loadAnalysisFile(analysisFilename, displayName, pm, uid);
				} catch (IOException e) {
					myIOE = e;
				} catch (InterruptedException e) {
					myIE = e;
				}	
			}
		}
		LoadRunnable loadRunnable = new LoadRunnable();
		new Thread(loadRunnable).run();
		loadRunnable.handleException();
		checkCancelledThrowIE();
		
		processTraceDrawAndResize(parent, false);
	}
	
	// called by createPage() of PIPageEditor
	public void openNpiForPIPageEditor(final URI analysisFileURI, final Composite parent, final int uid) {
		boolean isImport = analyserDataProcessorState == STATE_IMPORTING;
		final String displayName = new java.io.File(analysisFileURI).getName();
		
		if (isImport && getProgressMonitor() != null) {
			//import is already wrapped in runnable, and we should have progressmonitor
			getProgressMonitor().setTaskName(Messages.getString("AnalyserDataProcessor.5") + displayName);   //$NON-NLS-1$
			try {
				analyserDataProcessorState = STATE_OPENING;
				// by saying isImport true, we assume trace is set up proper in repository
				processTraceDrawAndResize(parent, true);
				analyserDataProcessorState = STATE_OK;
				if (false)
				internalOpenNPI (analysisFileURI, parent, uid);
			} catch (InvocationTargetException e) {
				analyserDataProcessorState = STATE_INVALID;
				String error = Messages.getString("AnalyserDataProcessor.6") + e.getTargetException().getMessage() + Messages.getString("AnalyserDataProcessor.7");   //$NON-NLS-1$ //$NON-NLS-2$
				if (e.getTargetException().getStackTrace() != null) {
					StringWriter sw = new StringWriter ();
					PrintWriter pw = new PrintWriter(sw);
					e.getTargetException().printStackTrace(pw);
					error += sw.toString() + "\n"; //$NON-NLS-1$
				}
				GeneralMessages.showErrorMessage(error);
			} catch (InterruptedException e) {
				String error = Messages.getString("AnalyserDataProcessor.8") + e.getMessage() + Messages.getString("AnalyserDataProcessor.9");   //$NON-NLS-1$ //$NON-NLS-2$
				analyserDataProcessorState = STATE_CANCELED;
			} catch (Exception e) {
				analyserDataProcessorState = STATE_INVALID;
				String error = Messages.getString("AnalyserDataProcessor.6") + e.getMessage() + Messages.getString("AnalyserDataProcessor.7");   //$NON-NLS-1$ //$NON-NLS-2$
				if (e.getStackTrace() != null) {
					StringWriter sw = new StringWriter ();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					error += sw.toString() + "\n"; //$NON-NLS-1$
				}
				GeneralMessages.showErrorMessage(error);
			}
		} else {
			setUp();
			//open need to be wrapped in runnable, and we should set progressmonitor
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			IRunnableWithProgress runnable= new IRunnableWithProgress() {

				public void run(IProgressMonitor progressMonitor)
				throws InvocationTargetException, InterruptedException {
					setProgressMonitor(progressMonitor);
					progressMonitor.beginTask(Messages.getString("AnalyserDataProcessor.10") + displayName, AnalyserDataProcessor.TOTAL_PROGRESS_COUNT * 20 / 100); //$NON-NLS-1$
					internalOpenNPI (analysisFileURI, parent, uid);
				}

			};
			
			try {
				progressService.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				analyserDataProcessorState = STATE_INVALID;
			} catch (InterruptedException e) {
				analyserDataProcessorState = STATE_CANCELED;
			}
		}
		
		getProgressMonitor().done();
	}
	
	private void internalOpenNPI(final URI analysisFileURI, final Composite parent, final int uid) throws InvocationTargetException, InterruptedException {
	
		analyserDataProcessorState = STATE_OPENING;
		
		if (analysisFileURI == null) {
			assertThrowITE(false, Messages.getString("AnalyserDataProcessor.11")); //$NON-NLS-1$
			return;
		}
		
		String filePath = null;
		String displayName = null;
		
		filePath = analysisFileURI.getPath();
		displayName = new java.io.File(analysisFileURI).getName();
		
		loadExistingAnalysis(parent, filePath, displayName, uid);
		analyserDataProcessorState = STATE_OK;

	}
		
	public void importSaveAndOpen(final IFile analysisFile, boolean pollTillNpiSaved, final List<ITrace> pluginsToUse) {
		analyserDataProcessorState = STATE_IMPORTING;
		setProgressMonitor(null);
		
		final int uid = NpiInstanceRepository.getInstance().register(null);

		setUp();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		IRunnableWithProgress runnableImportAndSave = new IRunnableWithProgress() {

			public void run(IProgressMonitor progressMonitor)
					throws InvocationTargetException, InterruptedException {
				importAndSave(analysisFile, uid, pluginsToUse, null,progressMonitor);
			}
		};
				
		IRunnableWithProgress runnableOpen = new IRunnableWithProgress() {

			public void run(IProgressMonitor arg0)
					throws InvocationTargetException, InterruptedException {
				// open the saved file
				openFile(analysisFile);
			}
			
		};
		
		try {
			progressService.busyCursorWhile(runnableImportAndSave);
			final IRunnableWithProgress saveNpi = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					ProfileReader.getInstance().writeAnalysisFile(
							analysisFile.getLocation().toString(), monitor,
							null, uid);

				}

			};
			progressService.busyCursorWhile(saveNpi);
			progressService.busyCursorWhile(runnableOpen);
		} catch (InvocationTargetException e) {
			handleRunnableException(e, uid, analysisFile);
		} catch (InterruptedException e) {
			handleRunnableException(e, uid, analysisFile);
		}						
	}
	
	public void importSave(final IFile analysisFile, final List<ITrace> pluginsToUse, String suffixTaskName, IProgressMonitor monitor) {
		analyserDataProcessorState = STATE_IMPORTING;
		setUp();
		setProgressMonitor(monitor);		
		final int uid = NpiInstanceRepository.getInstance().register(null);	
		try{
			importAndSave(analysisFile, uid, pluginsToUse, suffixTaskName, monitor);	
			ProfileReader.getInstance().writeAnalysisFile(analysisFile.getLocation().toString(), monitor, suffixTaskName, uid);				
			analyserDataProcessorState = STATE_OK;
		}catch (Exception e) {
			handleRunnableException(e, uid, analysisFile);
			monitor.setCanceled(true);
		}
		
	}
	
	private void importAndSave(final IFile analysisFile, final int uid, List<ITrace> pluginsToUse, String suffixTaskName,IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException{
		if(progressMonitor == null){
			progressMonitor = new NullProgressMonitor();
		}
		setProgressMonitor(progressMonitor);
		String taskName = Messages.getString("AnalyserDataProcessor.17") + analysisFile.getName();; //$NON-NLS-1$
		if(suffixTaskName != null){
			taskName += " "+suffixTaskName; //$NON-NLS-1$
		}

	    progressMonitor.beginTask(taskName, TOTAL_PROGRESS_COUNT);   //$NON-NLS-1$
		progressMonitor.setTaskName(taskName);
		// open a profile data file that should contain at least thread/address information

		// import new .dat
		assertThrowITE(SampleImporter.getInstance().validate(), Messages.getString("AnalyserDataProcessor.18"));	  //$NON-NLS-1$
		
		// invoke analysis-specific plugin instances
		PluginInitialiser.invokePluginInstances(uid, "com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace"); //$NON-NLS-1$

		StreamFileParser stp;
		try {
			stp = new StreamFileParser(new File(SampleImporter.getInstance().getDatFileName()));
			Hashtable<Integer,String> traceFileNames = new Hashtable<Integer,String>();
			ArrayList<File> tracesForCleanUp = new ArrayList<File>();

			// loop through all the plugins associated with traces and note their trace IDs names
			
				
			for (ITrace plugin : pluginsToUse) {
				File tempFile;
				int traceId = plugin.getTraceId();
				if (traceId != -1)
				{
					try {
						
						if (traceId == 1) {// the SMP change; separate temp data files for each CPU
							for (int i = 0; i < MAX_CPU; i++) {
								int smpTraceId = traceId + i * 20;
								tempFile = stp.getTempFileForTraceType(smpTraceId);
								if (tempFile != null) {
									tempFile.deleteOnExit();
									traceFileNames.put(smpTraceId, tempFile.getAbsolutePath());
									tracesForCleanUp.add(tempFile);
								}
							}
						} else {
							tempFile = stp.getTempFileForTraceType(traceId);
							if (tempFile != null)
							{
								tempFile.deleteOnExit();
								traceFileNames.put(traceId, tempFile.getAbsolutePath());
								tracesForCleanUp.add(tempFile);
							}									
						}

					} catch (IOException e) {
						throw new InvocationTargetException(e, Messages.getString("AnalyserDataProcessor.25")); //$NON-NLS-1$
					}
				}
			}
			
			// import a new analysis
			importNewAnalysis(traceFileNames, uid, pluginsToUse);

			// clean up temp file for each trace
			for (File traceFile : tracesForCleanUp) {
				traceFile.delete();
			}
		} catch (IOException e) {
			throw new InvocationTargetException(e, Messages.getString("AnalyserDataProcessor.26") + SampleImporter.getInstance().getDatFileName()); //$NON-NLS-1$
		}

		if (progressMonitor.isCanceled()) {
			throw new InterruptedException(Messages.getString("AnalyserDataProcessor.19"));   //$NON-NLS-1$
		}

		// give the .NPI file null contents
		byte[] b = new byte[0];
		try {
			analysisFile.create(new ByteArrayInputStream(b), true, null);
			// make sure we can open an input stream to the trace file
			analysisFile.getContents();
		} catch (CoreException e) {
			throw new InvocationTargetException(e, Messages.getString("AnalyserDataProcessor.14") + analysisFile.getName()); //$NON-NLS-1$
		}
		
		// extract additional info from importer
		int numberOfTraces = 0;
		Iterator<ParsedTraceData> enuTraces = TraceDataRepository.getInstance().getTraceCollectionIter(uid);
		AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();

		// for all traces exist in .dat set up their additional info
	    while (enuTraces.hasNext()) {
	    	Object object = enuTraces.next();

	    	numberOfTraces++;
	    	
	    	if (object instanceof ParsedTraceData) {
	    		ParsedTraceData parsedTraceData = (ParsedTraceData) object;
	    		if (parsedTraceData.traceData != null) {
		    		Class traceClass = parsedTraceData.traceData.getClass();

					// this code is clumsy because the plugin, not the trace, has the trace ID info
		    		Enumeration<AbstractPiPlugin> enuPlugins = PluginInitialiser.getPluginInstances(uid, "com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace"); //$NON-NLS-1$
					while (enuPlugins.hasMoreElements())
					{
						ITrace plugin = (ITrace)enuPlugins.nextElement();
						// only do when trace exist in .data
						if (traceClass == plugin.getTraceClass()) {
					    	if (plugin instanceof IProvideTraceAdditionalInfo) {
								((IProvideTraceAdditionalInfo)plugin).setupInfoHandler(handler);						    		
					    	}
						}
					}			
	    		}
	    	}
	    }
		
		// refresh so project know the update done by Java(non-Eclipse API)
		try {
			analysisFile.refreshLocal(0, null);
		} catch (CoreException e) {
			throw new InvocationTargetException(e, Messages.getString("AnalyserDataProcessor.15") + analysisFile.getName()); //$NON-NLS-1$
		}		
	}	
	
	private void openFile(final IFile analysisFile) {
		// open the saved file
		if (analysisFile.exists() && AnalyserDataProcessor.getInstance().getState() == STATE_IMPORTING ) {
				// open the file itself
			
			// need to open in UI context
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					IEditorPart editor = null;
					try {
						editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() , analysisFile, true);
					} catch (PartInitException e) {
						try {
							assertThrowITE(e, analysisFile.getName() + Messages.getString("AnalyserDataProcessor.24")); //$NON-NLS-1$
						} catch (InvocationTargetException e1) {
							//already set data structure proper, do nothing
						}
					}
					if (AnalyserDataProcessor.getInstance().getState() == STATE_CANCELED) {
						// close the editor file view
						editor.getSite().getPage().closeEditor(editor, false);
					} else if (AnalyserDataProcessor.getInstance().getState() != STATE_OK ) {
						// close the editor file view
						editor.getSite().getPage().closeEditor(editor, false);
					}							
				}
			});
		}
	}
	
	private void handleRunnableException(Throwable throwable, final int uid, IFile analysisFile) {
		NpiInstanceRepository.getInstance().unregister(uid);
		if (throwable instanceof InvocationTargetException) {
			String error = Messages.getString("AnalyserDataProcessor.20"); //$NON-NLS-1$
			if (throwable.getMessage() != null) {
				error += throwable.getMessage() + "\n"; //$NON-NLS-1$
			}
			error += Messages.getString("AnalyserDataProcessor.21");   //$NON-NLS-1$ //$NON-NLS-2$
			if (((InvocationTargetException)throwable).getTargetException().getStackTrace() != null) {
				StringWriter sw = new StringWriter ();
				PrintWriter pw = new PrintWriter(sw);
				((InvocationTargetException)throwable).printStackTrace(pw);
				error += sw.toString() + "\n"; //$NON-NLS-1$
			}
			GeneralMessages.showErrorMessage(error);
			analyserDataProcessorState = STATE_INVALID;
		} else if (throwable instanceof InterruptedException) {
			GeneralMessages.showErrorMessage(Messages.getString("AnalyserDataProcessor.22"));  //$NON-NLS-1$
			analyserDataProcessorState = STATE_CANCELED;
		} else {
			String error = Messages.getString("AnalyserDataProcessor.20"); //$NON-NLS-1$
			if (throwable.getStackTrace() != null) {
				StringWriter sw = new StringWriter ();
				PrintWriter pw = new PrintWriter(sw);
				((InvocationTargetException)throwable).printStackTrace(pw);
				error += sw.toString() + "\n"; //$NON-NLS-1$
			}
			GeneralMessages.showErrorMessage(error);
			analyserDataProcessorState = STATE_INVALID;		
		}
		// don't leave any garbage behind if we failed or bailed
		if (analysisFile != null && AnalyserDataProcessor.getInstance().getState() != STATE_OK ) {
			java.io.File javaFile = new java.io.File(analysisFile.getLocation().toString());
			boolean deleted = javaFile.delete();
				
			if (deleted == false){
				try {
					analysisFile.delete(true, null);
				} catch (CoreException ce) {
					ce.printStackTrace();
				}
			}

			try {
				// force Eclipse to be aware of the removed file by doing an IFile refresh
				analysisFile.refreshLocal(0, null);
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		}	
	}
	
	// standalone for save as
	public void saveAnalysis(final String filename, final int uid) throws InvocationTargetException, InterruptedException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		IRunnableWithProgress runnable= new IRunnableWithProgress() {

			public void run(IProgressMonitor progressMonitor)
					throws InvocationTargetException, InterruptedException {
				saveAnalysisInternal(filename, uid);
			}
			
		};
		try {
			progressService.busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			analyserDataProcessorState = STATE_INVALID;
		} catch (InterruptedException e) {
			analyserDataProcessorState = STATE_CANCELED;
		}
	}

	// save profiling data to NPI file
	private void saveAnalysisInternal(final String filename, final int uid) throws InvocationTargetException, InterruptedException {
		ProfileReader.getInstance().writeAnalysisFile(filename, getProgressMonitor(), null,uid);
	}
	
	private void processVisualizableItem(ITrace plugin)
	{
		IVisualizable visualizable = (IVisualizable)plugin;
		
		if (!visualizable.arePagesCreated())
		{
			// create any editor pages
			for (int i = 0; i < visualizable.getCreatePageCount(); i++)
			{
				int index = visualizable.getCreatePageIndex(i);
				ArrayList<ProfileVisualiser> pages = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
				if (pages != null) {
					// if we don't care what the page index is, or the index is too big, add it to the end
					if ((index == PIPageEditor.NEXT_AVAILABLE_PAGE) || (index > pages.size())) {
						index = pages.size();
						
						// let the plugin know what index we assigned
						visualizable.setPageIndex(i, index);
					}
					
					ProfileVisualiser pV = visualizable.createPage(index);
					
					// add the page to the editor
					if (pV != null)
						pages.add(index, pV);					
				}
			}
		}
		
		int uid = NpiInstanceRepository.getInstance().activeUid();
		
		// determine how many graphs to draw (several may get added to the same page)
		for (int i = 0; i < visualizable.getGraphCount(); i++)
		{
			GraphDrawRequest gdr   = visualizable.getDrawRequest(i);
			IGenericTraceGraph gtg  = visualizable.getTraceGraph(i);
			int	pageNumber         = visualizable.getPageNumber(i);
			
			ProfileVisualiser page = NpiInstanceRepository.getInstance().getProfilePage(uid, pageNumber);

			if (gtg != null)
			{
				page.getTopComposite().addGraphComponent(gtg, visualizable.getClass(), gdr);
			}

			Integer lastSample = visualizable.getLastSample(i);
			if (lastSample != null)
			{
				page.setLastSampleX(lastSample.intValue());
			}
		}
	}
	
	private void processTraceDrawAndResize(final Composite parent, boolean isImport) {
		// if it is import, everything is already read in place, otherwise
		// setup those from object file read
		if (!isImport) {
			ProfileReader.getInstance().processDataReadFromNpiFile(this);
		}
		
		int uid = NpiInstanceRepository.getInstance().activeUid();
		
		// initialize trace and do visual
		ArrayList<AbstractPiPlugin> plugins = NpiInstanceRepository.getInstance().getPlugins(uid);
		for (final AbstractPiPlugin plugin : plugins) {
			if (plugin instanceof ITrace) {
				final ParsedTraceData parsedData = TraceDataRepository.getInstance().getTrace(uid, ((ITrace)plugin).getTraceClass());
				if (parsedData != null) {
					final ITrace pluginTrace = (ITrace)plugin;
					pluginTrace.initialiseTrace(parsedData.traceData);
	    			// do the graphic painting with indicator, SWT require it to be in UI thread
					if (plugin instanceof IVisualizable) {
						Display.getDefault().syncExec( new Runnable() {
							public void run() {
								processVisualizableItem(pluginTrace);
							}
		    			});	
					}
				}
			}
		}
		
		if (!isImport) {
			ProfileReader.getInstance().setAddtionalDataForRecordable();
		}
		
		ProfileReader.getInstance().setTraceMenus(NpiInstanceRepository.getInstance().getPlugins(uid), uid);
		// do the graphic painting with indicator, SWT require it to be in UI thread
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				parent.addPaintListener( new PaintListener () {
		  			
					public void paintControl(PaintEvent arg0) {

						if(parent.getBounds().width > 0) {
							ArrayList<ProfileVisualiser> pages = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
							
							// make sure we are called after the pages have been created?
							if (pages.size() > 0) {
								for (final ProfileVisualiser page : pages) {
									// NOTE: This assumes that the first profile page has a graph
						        	final PICompositePanel visibleComposite = page.getTopComposite();
						        	visibleComposite.performZoomToGraph(visibleComposite, parent.getBounds().width);
						        	
						        	//TODO uncomment when performance issues relating to fcc are solved
						        	//Select whole graph
						        	//visibleComposite.selectWholeGraph();
								}
			
								// scale to whole trace only once
					        	parent.removePaintListener(this);
							}
						}
					}
		  		});
			}
		});
	}
	
	// This is for test automation, removing time stamps, so we can diff .npi
	public void importForStrippingTimeStamp(final Composite parent) {
		if (SampleImporter.getInstance().isStrippingTimeStamp()) {
			// had to do this monkey business for pi validation because
			// serialization is an untestable format
			// read file
			final SampleImporter sampleImporter = SampleImporter.getInstance();

			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			IRunnableWithProgress runnable= new IRunnableWithProgress() {

				public void run(IProgressMonitor progressMonitor)
						throws InvocationTargetException,
						InterruptedException {
					setProgressMonitor(progressMonitor);
					progressMonitor.beginTask(Messages.getString("AnalyserDataProcessor.23"), 100);   //$NON-NLS-1$
					try {
						int uid = NpiInstanceRepository.getInstance().activeUid();
						loadExistingAnalysis(parent, sampleImporter.getDatFileName(), sampleImporter.getDatFileName(), uid);
						// time stample differs for every save, take it out
						AnalysisInfoHandler infoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
						if (infoHandler != null) {
							infoHandler.eraseTimeStamp();
						}
						// write file
						saveAnalysisInternal(sampleImporter.getPiFileName(), uid);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			try {
				progressService.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				analyserDataProcessorState = STATE_INVALID;
				e.printStackTrace();
			} catch (InterruptedException e) {
				analyserDataProcessorState = STATE_CANCELED;
				e.printStackTrace();
			}
			setProgressMonitor(null);
		}
	}
	
	// catch all for any exception, so we can print an error stack trace page later
	private void assertThrowITE(boolean cond, String message) throws InvocationTargetException {
		if (!cond) {
			PIExceptionRuntime pire = new PIExceptionRuntime(message);	//$NON-NLS-1$
			InvocationTargetException ite = new InvocationTargetException(pire);
			lastException = ite;
			analyserDataProcessorState = STATE_INVALID;
			if (getProgressMonitor() != null) {
				getProgressMonitor().done();
			}
			GeneralMessages.showErrorMessage(message);
			throw ite;
		}
	}
	
	private void assertThrowITE(Exception e, String message) throws InvocationTargetException {
		InvocationTargetException ite;
		if (message != null) {
			ite = new InvocationTargetException(e, message);
		} else {
			ite = new InvocationTargetException(e);
		}
		lastException = ite;
		analyserDataProcessorState = STATE_INVALID;
		if (getProgressMonitor() != null) {
			getProgressMonitor().done();
		}
		GeneralMessages.showErrorMessage(message);
		throw ite;

	}

	
	// General handling for Cancel operation
	public void checkCancelledThrowIE() throws InterruptedException {
		if (getProgressMonitor() != null && getProgressMonitor().isCanceled()) {
			InterruptedException ie = new InterruptedException();
			analyserDataProcessorState = STATE_CANCELED;
			throw ie;
		}
	}
	
	private void setUp() {
		if (getProgressMonitor() != null) {
			getProgressMonitor().done();
			setProgressMonitor(null);
		}
		lastException = null;
		analyserDataProcessorState = STATE_OK;
	}
}
