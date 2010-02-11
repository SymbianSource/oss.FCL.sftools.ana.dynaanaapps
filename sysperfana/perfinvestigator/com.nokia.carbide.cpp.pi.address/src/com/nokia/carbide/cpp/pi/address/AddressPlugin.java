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

package com.nokia.carbide.cpp.pi.address;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.actions.SetThresholdsDialog;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledBinary;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IRecordable;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IReportable;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable;
import com.nokia.carbide.cpp.internal.pi.resolvers.SymbolFileFunctionResolver;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphDrawRequest;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.core.SessionPreferences;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


/**
 * The main plugin class to be used in the desktop.
 */
public class AddressPlugin extends AbstractPiPlugin
		implements ITrace, IViewMenu, IClassReplacer, //IExportItem,
					IReportable, IRecordable, IVisualizable, IEventListener
{
	private static final String HELP_CONTEXT_ID = PIPageEditor.PI_ID + ".address";  //$NON-NLS-1$
	
//	private static HashMap<Integer,Long> uidToAddrThreadPeriod = new HashMap<Integer,Long>();

	// There will be three graphs - one each for editor pages 0, 1, 2
	// This code assumes that page 0 has the threads graph, 1 the binaries, and 2 the functions
	private final static int GRAPH_COUNT = 3;
	private boolean pagesCreated = false;

	// The shared instance.
	private static AddressPlugin plugin;

	private int functionsShown = 10;

	// string that should contain the maximum number of functions to display
	private String functionCountString;

	private static void setPlugin(AddressPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public AddressPlugin() {
		super();
		setPlugin(this);
	}

	public Class getTraceClass()
	{
		return GppTrace.class;
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf("com.nokia.carbide.cpp.pi.address.GppTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.GppTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.address.GppTrace") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.gppTracePlugin.GppTrace") != -1) //$NON-NLS-1$
		{
			return GppTrace.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.address.GppSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.address.GppSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.GppSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gppTracePlugin.GppSample") != -1) //$NON-NLS-1$
		{
			return GppSample.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.address.GppThread") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.address.GppThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.GppThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gppTracePlugin.GppThread") != -1) //$NON-NLS-1$
		{
			return GppThread.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.address.GppProcess") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.address.GppProcess") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.GppProcess") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.gppTracePlugin.GppProcess") != -1) //$NON-NLS-1$
		{
			return GppProcess.class;
		}
		else
		{
			return null;
		}
	}

	public void initialiseTrace(GenericTrace genericTrace)
	{
		if (!(genericTrace instanceof GppTrace))
			return;

		GppTrace trace = (GppTrace)genericTrace;
		
		NpiInstanceRepository.getInstance().activeUidAddTrace("com.nokia.carbide.cpp.pi.address", trace); //$NON-NLS-1$
		
		// initialize the address/thread base sampling rate
		int samplingInterval = 1;
		if (trace.samples.size() > 2) {
			// because of a problem in older samplers, the first address/thread sample may have been thrown out
			samplingInterval = (int) (((GppSample) trace.samples.get(1)).sampleSynchTime - ((GppSample) trace.samples.get(0)).sampleSynchTime); 
		}
			
		NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval", new Integer(samplingInterval)); //$NON-NLS-1$
		
		// make sure that the sorted samples array exists
		if (trace.getSortedGppSamples() == null)
			trace.sortGppSamples();

		GppTraceGraph.refreshDataFromTrace(trace);
		PIPageEditor.setTime(0.0f, 0.0f);
		long lastSampleTime = trace.getSample(trace.samples.size() - 1).sampleSynchTime;
		PIPageEditor.currentPageEditor().setMaxEndTime(((double)lastSampleTime) / 1000.0f);
	}

	public GppTrace getTrace()
	{
		return (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$
	}

	public GenericTraceGraph getTraceGraph(int graphIndex)
	{
		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$
		
		if (trace != null) {
			int uid = NpiInstanceRepository.getInstance().activeUid();
			return trace.getTraceGraph(graphIndex, uid);
		} else
			return null;
	}

	public GenericTrace getTrace(int graphIndex)
	{	
		return (GenericTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$
	}
	
/*
	public GenericTraceGraph getTraceGraph(int graphIndex, int uid)
	{	
		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$

		// if we already had it, then we can pass in the formatted trace data
		return trace.getTraceGraph(graphIndex, uid);
	}
*/
	
	public Integer getLastSample(int graphIndex)
	{
		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$

		if (trace == null)
			return null;

	  	//this sets GPP thread list visible by default
	  	((GppTraceGraph)trace.getTraceGraph(graphIndex)).piEventReceived(new PIEvent(null, PIEvent.MOUSE_PRESSED));

	  	return new Integer(trace.getLastSampleNumber());
	}

	public GraphDrawRequest getDrawRequest(int graphIndex) {
		return null;
	}

	public void receiveSelectionEvent(String eventString)
	{
		if (eventString == null)
			return;

		int currentPage = PIPageEditor.currentPageIndex();

		if (   (currentPage != PIPageEditor.THREADS_PAGE)
			&& (currentPage != PIPageEditor.BINARIES_PAGE)
			&& (currentPage != PIPageEditor.FUNCTIONS_PAGE))
			  return;
		
		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$
		
		if (eventString.equals("fillSelected")) //$NON-NLS-1$
	    {
	    	PIEvent be = new PIEvent(null, PIEvent.SET_FILL_SELECTED_THREAD);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
	    	
	    	NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.fillAll", Boolean.FALSE); //$NON-NLS-1$
	    }
		else if (eventString.equals("fillAll")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(null, PIEvent.SET_FILL_ALL_THREADS);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
	    	
	    	NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.fillAll", Boolean.TRUE); //$NON-NLS-1$
		}
		else if (eventString.equals("fillNone")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(null, PIEvent.SET_FILL_OFF);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
	    	
	    	NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.fillAll", Boolean.FALSE); //$NON-NLS-1$
		}
		else if (eventString.equals("setBarOn")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(null, PIEvent.GPP_SET_BAR_GRAPH_ON);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
	    	
	    	NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.bar", Boolean.TRUE); //$NON-NLS-1$
		}
		else if (eventString.equals("setBarOff")) //$NON-NLS-1$
		{
			PIEvent be = new PIEvent(null, PIEvent.GPP_SET_BAR_GRAPH_OFF);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.THREADS_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.BINARIES_PAGE)).piEventReceived(be);
	    	((GppTraceGraph)trace.getTraceGraph(PIPageEditor.FUNCTIONS_PAGE)).piEventReceived(be);
	    	
	    	NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.bar", Boolean.FALSE); //$NON-NLS-1$
		}
	  	else if (eventString.equals("resetToCurrentMode")) //$NON-NLS-1$
	  	{
	  		((GppTraceGraph)trace.getTraceGraph(currentPage)).action(eventString);
	  	}
	}

	public String getTraceName() {
		return "Address/Thread";	//$NON-NLS-1$
	}

	public int getTraceId() {
		return 1;
	}

	public ParsedTraceData parseTraceFile(File file /*, ProgressBar progressBar*/) throws IOException
	{
//		progressBar.setString("Parsing address and thread trace");

        GppTraceParser gppParser = new GppTraceParser();
        ParsedTraceData parsed = gppParser.parse(file);
        SymbolFileFunctionResolver sffp = this.resolveSymbolFileParser(/*progressBar*/);

        parsed.functionResolvers = new FunctionResolver[]{sffp};

        return parsed;
	}

	private SymbolFileFunctionResolver resolveSymbolFileParser(/*ProgressBar progressBar*/)
    {
        //        	progressBar.setString("Parsing symbol file");
        //reads the symbol name path from configuration data
        File symbolFile = new File(
        		SampleImporter.getInstance().getRomSymbolFile());

        SymbolFileFunctionResolver sffp = new SymbolFileFunctionResolver();
        sffp.parseAndProcessSymbolFile(symbolFile);
        return sffp;
    }

	public Hashtable<Integer,Object> getSummaryTable(double startTime, double endTime)
	{
		if (plugin == null)
			return null;

		// This code relies on currentPage being the same as graphIndex
		int currentPage = PIPageEditor.currentPageIndex();

		if (   (currentPage != PIPageEditor.THREADS_PAGE)
			&& (currentPage != PIPageEditor.BINARIES_PAGE)
			&& (currentPage != PIPageEditor.FUNCTIONS_PAGE))
			  return null;

		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$

		Hashtable<Integer,Object> summary = new Hashtable<Integer,Object>();
		Enumeration<ProfiledGeneric> e = trace.getSortedThreadsElements();
		while (e.hasMoreElements())
		{
			Vector<Object> data = new Vector<Object>();
			ProfiledThread pt = (ProfiledThread)e.nextElement();
			data.add(pt.getNameString());
			data.add(pt.getAverageLoadValueString(currentPage));
			summary.put(new Integer(pt.getThreadId()), data);
		}
		return summary;
	}

	public String getGeneralInfo()
	{
		return null;
	}

	public ArrayList<String> getColumnNames()
	{
		ArrayList<String> names = new ArrayList<String>();
		names.add(Messages.getString("AddressPlugin.0"));  //$NON-NLS-1$
		names.add(Messages.getString("AddressPlugin.1"));  //$NON-NLS-1$
		return names;
	}

	public ArrayList<Boolean> getColumnSortTypes()
	{
		ArrayList<Boolean> sortTypes = new ArrayList<Boolean>();
		sortTypes.add(SORT_BY_NAME);
		sortTypes.add(SORT_BY_NUMBER);
		return sortTypes;
	}

	public String getActiveInfo(Object key, double startTime, double endTime)
	{
		int threadId;
		if (key instanceof Integer)
			threadId = ((Integer)key).intValue();
		else
			return null;
		String threadName = ""; //$NON-NLS-1$
		Hashtable<Integer,Object> tmpTable = this.getSummaryTable(startTime, endTime);
		if (tmpTable != null)
		{
			threadName = (String)((Vector<Object>)tmpTable.get(key)).elementAt(0);
		}

		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$

		Vector<GenericSample> samples = trace.getSamplesInsideTimePeriod((long)startTime, (long)endTime);
		Hashtable<String,Integer> functionLoad = new Hashtable<String,Integer>();
		int threadSampleAmount = 0;

		for (Enumeration<GenericSample> e = samples.elements(); e.hasMoreElements();)
		{
			GppSample sample = (GppSample)e.nextElement();

			String functionName = Messages.getString("AddressPlugin.2");  //$NON-NLS-1$

			if (sample.currentFunctionSym != null)
				functionName = sample.currentFunctionSym.functionName;
			else if (sample.currentFunctionItt != null)
				functionName = sample.currentFunctionItt.functionName;

			if (sample.thread.threadId.intValue() == threadId)
			{
				Integer load = (Integer)functionLoad.get(functionName);
				if (load != null)
				{
					functionLoad.remove(functionName);
					functionLoad.put(functionName, new Integer(load.intValue()+1));
				}
				else
				{
					functionLoad.put(functionName, new Integer(1));
				}
				threadSampleAmount++;
			}
		}
		float totalLoad = 0;
		int functions = 0;
//		int functionsShown = 10;
		for (Enumeration<String> e = functionLoad.keys();e.hasMoreElements();)
		{
			String testFunction = e.nextElement();
			float load = ((Integer)functionLoad.get(testFunction)).floatValue()*100/threadSampleAmount;
			totalLoad += load;
			functions++;
		}

		ArrayList<Object> sortedFunctions = new ArrayList<Object>();
//		functionsShown = new Integer(functionsShownField.getText()).intValue();
		while (functionLoad.size() != 0 && sortedFunctions.size() <= (functionsShown * 2) - 1)
		{
			String maxFunction = null;
			int maxCount = 0;

			for (Enumeration<String> e = functionLoad.keys(); e.hasMoreElements();)
			{
				String testFunction = e.nextElement();
				int count = functionLoad.get(testFunction).intValue();
				if (count > maxCount)
				{
					maxFunction = testFunction;
					maxCount = count;
				}
			}
			sortedFunctions.add(maxFunction);
			sortedFunctions.add(new Integer(maxCount));
			functionLoad.remove(maxFunction);
		}

		if (threadSampleAmount > 0)
		{
			String finalString = "\r\n" + Messages.getString("AddressPlugin.4")+threadName+"\r\n";   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			for (int i = 0; i < sortedFunctions.size(); )
			{
				String name = (String)sortedFunctions.get(i++);
				//String percent = ((Integer)e.nextElement()).floatValue()*100/threadSampleAmount+"%";

				float load = ((Integer)sortedFunctions.get(i++)).floatValue()*100/threadSampleAmount;
				String percent = new Float(load/totalLoad*100).toString();

				finalString += percent+"% "+name+"\r\n"; //$NON-NLS-1$ //$NON-NLS-2$ 
			}

			return finalString;
		}
		else return "\r\n"  + Messages.getString("AddressPlugin.8")+threadName+"\r\n";   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public Action getIncludeAction()
	{
		return null;
	}


	public Action getExportAction() {
		Action action = new Action(Messages.getString("AddressPlugin.10"), Action.AS_PUSH_BUTTON) {  //$NON-NLS-1$
			public void run() {
				ParsedTraceData ptd = TraceDataRepository.getInstance().getTrace(NpiInstanceRepository.getInstance().activeUid(), getTraceClass());
				if (ptd != null) {
					final GppTrace gppTraceTmp = (GppTrace)ptd.traceData;
					final FileDialog dlg = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
					
					new Thread()
					{
						public void run()
						{
							dlg.setFilterNames(new String[] {
									Messages.getString("AddressPlugin.11") + " (*.csv)"  //$NON-NLS-1$ //$NON-NLS-2$
							});
							dlg.setFilterExtensions(new String[] {
									"*.csv" //$NON-NLS-1$
							});
							dlg.setText(Messages.getString("AddressPlugin.13"));  //$NON-NLS-1$

							String fileName = dlg.open();
							if (fileName == null) {
								return;
							}
							
							File file = new File(fileName);
						    GppTraceCsvPrinter csvPrinter = new GppTraceCsvPrinter(gppTraceTmp);
						    String csvPrint = csvPrinter.getCsvPrint();
						    if (csvPrint != null)
						    {
						    	try {
							        PIUtilities.saveCsvPrint(file, csvPrint);
						    	}
								catch (Exception e) 
								{
								    e.printStackTrace();
								}
						    }
						}
					}.start();
				}
			}
		};

		return action;
	}

	public MenuManager getReportGeneratorManager()
	{
		MenuManager reportManager;
		Action reportAction;
		
		reportAction = new Action(Messages.getString("AddressPlugin.6")) {  //$NON-NLS-1$
			public void run() {
				Display display = PIPageEditor.currentPageEditor().getEditorSite().getShell().getDisplay();
				Shell shell = new Shell(display);
				shell.setText(Messages.getString("AddressPlugin.15"));  //$NON-NLS-1$
				shell.setLayout(new FillLayout());
				Text number = new Text(shell, SWT.BORDER);
				number.setLayoutData(new FillLayout());
				final Text numberFinal = number;
				number.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						functionCountString = numberFinal.getText();
					}
				});
				shell.pack();
				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
				try {
					functionsShown = Integer.parseInt(functionCountString);
				} catch (NumberFormatException e1) {
		        	GeneralMessages.showErrorMessage(Messages.getString("AddressPlugin.16"));  //$NON-NLS-1$
				}
			}
		};
		reportAction.setToolTipText(Messages.getString("AddressPlugin.17"));  //$NON-NLS-1$

		// hook up to the PI toplevel menu
		reportManager = new MenuManager(Messages.getString("AddressPlugin.3"));  //$NON-NLS-1$
		reportManager.add(reportAction);
		PIPageEditor.currentMenuManager().add(reportManager);

		return reportManager;
	}

	public MenuManager getViewOptionManager()
	{
		Action action;
		
		MenuManager manager = new MenuManager(Messages.getString("AddressPlugin.5"));  //$NON-NLS-1$
		
		action = new Action(Messages.getString("AddressPlugin.7"), Action.AS_CHECK_BOX) {  //$NON-NLS-1$
			public void run() {
				if (this.isChecked())
					receiveSelectionEvent("setBarOn"); //$NON-NLS-1$
				else
					receiveSelectionEvent("setBarOff"); //$NON-NLS-1$
			}
		};
		
		Boolean bar = SessionPreferences.getInstance().getBarMode();		// default value to session preference

		Object obj;
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.bar"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			bar = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.bar", bar); //$NON-NLS-1$

		action.setChecked(bar);
		action.setToolTipText("Show graph as vertical bars"); //$NON-NLS-1$
		manager.add(action);
		
//		manager.add(new Separator());
		
		Boolean fillAll = SessionPreferences.getInstance().getFillAllEnabled();		// default value to session preference
		
		// if there is a value associated with the current Analyser tab, then use it
		obj = NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.fillAll"); //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			fillAll = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.fillAll", fillAll); //$NON-NLS-1$

		action = new Action(Messages.getString("AddressPlugin.9"), Action.AS_CHECK_BOX) {  //$NON-NLS-1$
			public void run() {
				if (this.isChecked()) {
					receiveSelectionEvent("fillAll"); //$NON-NLS-1$
				} else {
					receiveSelectionEvent("fillNone"); //$NON-NLS-1$
				}
			}
		};
		action.setChecked(fillAll);
		action.setToolTipText(Messages.getString("AddressPlugin.22"));  //$NON-NLS-1$
		manager.add(action);
		
//		manager.add(new Separator());
//		
//		action = new Action(Messages.getString("AddressPlugin.saveAddressThreadSamples"), Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
//			public void run() {
//				new SaveSamplesWizard(Display.getCurrent());
//			}
//		};
//		action.setToolTipText(Messages.getString("AddressPlugin.saveSamplesToolTip")); //$NON-NLS-1$
//		manager.add(action);
		
		manager.add(new Separator());
		
		action = new Action(Messages.getString("AddressPlugin.12"), Action.AS_PUSH_BUTTON) {  //$NON-NLS-1$
			public void run() {
				new SetThresholdsDialog(Display.getCurrent());
			}
		};
		action.setToolTipText(Messages.getString("AddressPlugin.24"));  //$NON-NLS-1$
		manager.add(action);

		return manager;
	}

	public Serializable getAdditionalData()
	{
		if (plugin == null)
			return null;

		GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$
		
		if (trace == null)
			return null;

		Vector<Object> wrapper = new Vector<Object>();
		
		Hashtable<Integer,java.awt.Color> threadColors = new Hashtable<Integer,java.awt.Color>();
		Hashtable<String,java.awt.Color> binaryColors  = new Hashtable<String,java.awt.Color>();
		Enumeration<ProfiledGeneric> e = trace.getSortedThreadsElements();
		while (e.hasMoreElements())
		{
			ProfiledThread pt = (ProfiledThread)e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = new java.awt.Color(pt.getColor().getRed(),pt.getColor().getGreen(),pt.getColor().getBlue());
			threadColors.put(new Integer(pt.getThreadId()), tmpAWTColor);
		}
		e = trace.getSortedBinariesElements();
		while (e.hasMoreElements())
		{
			ProfiledBinary pb = (ProfiledBinary)e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = new java.awt.Color(pb.getColor().getRed(),pb.getColor().getGreen(),pb.getColor().getBlue());
			binaryColors.put(pb.getNameString(), tmpAWTColor);
		}

		e = trace.getSortedFunctionsElements();
		
		Hashtable<String,java.awt.Color> functionColors  = new Hashtable<String,java.awt.Color>();

		while (e.hasMoreElements())
		{
			ProfiledFunction pb = (ProfiledFunction)e.nextElement();
			// backward compatibility with old Swing Color
			java.awt.Color tmpAWTColor = new java.awt.Color(pb.getColor().getRed(),pb.getColor().getGreen(),pb.getColor().getBlue());
			functionColors.put(pb.getNameString(), tmpAWTColor);
		}
		
		wrapper.add(threadColors);
		wrapper.add(binaryColors);
		wrapper.add(functionColors);
		return wrapper;
	}

	public void setAdditionalData(int graphIndex, Serializable data)
	{
		if (!(data instanceof Vector))
			return;
		
		try {
			final GppTrace trace = (GppTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.address"); //$NON-NLS-1$

			Vector<Object> tmpData = (Vector<Object>)data;
			Hashtable<Integer,java.awt.Color> threadColors = (Hashtable<Integer,java.awt.Color>)tmpData.elementAt(0);
			Hashtable<String,java.awt.Color> binaryColors = (Hashtable<String,java.awt.Color>)tmpData.elementAt(1);
			Hashtable<String,java.awt.Color> functionColors = (Hashtable<String,java.awt.Color>)tmpData.elementAt(2);

			boolean changed;
			Enumeration<ProfiledGeneric> e;
			
			changed = false;
			e = trace.getSortedThreadsElements();
			while (e.hasMoreElements())
			{
				ProfiledThread pt = (ProfiledThread)e.nextElement();
				// backward compatibility with old Swing Color
				java.awt.Color tmpAWTColor = threadColors.get(new Integer(pt.getThreadId()));
				if (tmpAWTColor != null) {
					Color color = ColorPalette.getColor(new RGB(tmpAWTColor.getRed(), tmpAWTColor.getGreen(), tmpAWTColor.getBlue()));
					if (color != null) {
						pt.setColor(color);
						changed = true;
					}
				}
			}
			
			final int uid = NpiInstanceRepository.getInstance().activeUid();

			if (changed) {
				// need to provide new colors for the thread load table
				trace.getGppGraph(PIPageEditor.THREADS_PAGE, uid).getThreadTable().addColor(Defines.THREADS);
			}

			changed = false;
			e = trace.getSortedBinariesElements();
			while (e.hasMoreElements())
			{
				ProfiledBinary pb = (ProfiledBinary)e.nextElement();
				// backward compatibility with old Swing Color
				java.awt.Color tmpAWTColor = binaryColors.get(pb.getNameString());
				if (tmpAWTColor != null) {
					Color color = ColorPalette.getColor(new RGB(tmpAWTColor.getRed(), tmpAWTColor.getGreen(), tmpAWTColor.getBlue()));
					if (color != null) {
						pb.setColor(color);
						changed = true;
					}
				}
			}

			if (changed) {
				// need to provide new colors for the binary load table
				trace.getGppGraph(PIPageEditor.BINARIES_PAGE, uid).getBinaryTable().addColor(Defines.BINARIES);
			}
			
			changed = false;
			e = trace.getSortedFunctionsElements();
			while (e.hasMoreElements())
			{
				ProfiledFunction pb = (ProfiledFunction)e.nextElement();
				// backward compatibility with old Swing Color
				java.awt.Color tmpAWTColor = functionColors.get(pb.getNameString());
				if (tmpAWTColor != null) {
					Color color = ColorPalette.getColor(new RGB(tmpAWTColor.getRed(), tmpAWTColor.getGreen(), tmpAWTColor.getBlue()));
					if (color != null) {
						pb.setColor(color);
						changed = true;
					}
				}
			}

			if (changed) {
				// need to provide new colors for the binary load table
				trace.getGppGraph(PIPageEditor.FUNCTIONS_PAGE, uid).getFunctionTable().addColor(Defines.FUNCTIONS);
			}
			
		} catch (Exception e) {
			System.out.println("Could not load additional address/thread data!"); //$NON-NLS-1$
//			e.printStackTrace();
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		setPlugin(null);
	}

	/**
	 * Returns the shared instance.
	 */
	public static AddressPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.address", path); //$NON-NLS-1$
	}

	// number of graphs supplied by this plugin
	public int getGraphCount() {
		return GRAPH_COUNT;
	}

	// page number of each graph supplied by this plugin
	public int getPageNumber(int graphIndex) {
		if (graphIndex == 0)
			return PIPageEditor.THREADS_PAGE;
		else if (graphIndex == 1)
			return PIPageEditor.BINARIES_PAGE;
		else if (graphIndex == 2)
			return PIPageEditor.FUNCTIONS_PAGE;

		return PIPageEditor.NEXT_AVAILABLE_PAGE;
	}

	// return whether this plugin's editor pages have been created
	public boolean arePagesCreated() {
		return this.pagesCreated;
	}
	
	// set whether this plugin's editor pages have been created
	public void setPagesCreated(boolean pagesCreated) {
		this.pagesCreated = pagesCreated;
	}

	// number of editor pages to create
	public int getCreatePageCount() {
		return GRAPH_COUNT;
	}

	// editor page index for each created editor page
	public int getCreatePageIndex(int index) {
		if (index == 0)
			return PIPageEditor.THREADS_PAGE;
		else if (index == 1)
			return PIPageEditor.BINARIES_PAGE;
		else if (index == 2)
			return PIPageEditor.FUNCTIONS_PAGE;

		return PIPageEditor.NEXT_AVAILABLE_PAGE;
	}

	// page index actually assigned to a created page
	public void setPageIndex(int index, int pageIndex) {
		// if we didn't get pages 0, 1, and 2, we're in trouble
	}

	// create the page(s)
	public ProfileVisualiser createPage(int index) {
		String pageName = null;
		String pageHelp = null;
		Composite parent = NpiInstanceRepository.getInstance().activeUidGetParentComposite();
		
		if (parent == null) {
			// no parent composite is only for temp instance used by non-GUI importer
			GeneralMessages.showErrorMessage("Address trace failed to create UI"); //$NON-NLS-1$
			return null;
		}

		if (index == 0) {
			pageName = "Threads"; //$NON-NLS-1$
			pageHelp = "threadsPageContext"; //$NON-NLS-1$
		} else if (index == 1) {
			pageName = "Binaries"; //$NON-NLS-1$
			pageHelp = "binariesPageContext"; //$NON-NLS-1$
		} else if (index == 2) {
			pageName = "Functions"; //$NON-NLS-1$
			pageHelp = "functionsPageContext"; //$NON-NLS-1$
		} else {
			return null;
		}

		ProfileVisualiser pV = new ProfileVisualiser(ProfileVisualiser.TOP_AND_BOTTOM, parent, pageName);
		int uid = NpiInstanceRepository.getInstance().activeUid();
		AnalysisInfoHandler infoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
		pV.getParserRepository().setPIAnalysisInfoHandler(infoHandler);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(pV.getContentPane(), HELP_CONTEXT_ID + "." + pageHelp); //$NON-NLS-1$

		return pV;
	}

	public void receiveEvent(String action, Event event) {
		if (action.equals("scroll")) { //$NON-NLS-1$
			if (   !(event.data instanceof String)
				|| !((String)event.data).equals("FigureCanvas")) //$NON-NLS-1$
				return;
			
			// the currently visible page has scrolled, so scroll the other pages
			ArrayList<ProfileVisualiser> pages = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
			ProfileVisualiser pV;

			int currentPV = PIPageEditor.currentPageIndex();
			if (currentPV != PIPageEditor.THREADS_PAGE) {
				pV = pages.get(PIPageEditor.THREADS_PAGE);
				pV.getTopComposite().setScrolledOrigin(event.x, event.y);
			}
			if (currentPV != PIPageEditor.BINARIES_PAGE) {
				pV = (ProfileVisualiser)pages.get(PIPageEditor.BINARIES_PAGE);
				pV.getTopComposite().setScrolledOrigin(event.x, event.y);
			}
			if (currentPV != PIPageEditor.FUNCTIONS_PAGE) {
				pV = (ProfileVisualiser)pages.get(PIPageEditor.FUNCTIONS_PAGE);
				pV.getTopComposite().setScrolledOrigin(event.x, event.y);
			}
		} else if (action.equals("priority_init")) { //$NON-NLS-1$
			// priority trace has been processed, so let the threads page know
			Hashtable<Integer,String> priStringById = (Hashtable<Integer,String>)event.data;
			
			if (getTrace() == null)
				return;

			GppTraceGraph addressGraph = ((GppTraceGraph) getTrace().getTraceGraph(PIPageEditor.THREADS_PAGE));

			if (addressGraph != null) {
				addressGraph.updateThreadTablePriorities(priStringById);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.IVisualizable#getGraphTitle(int)
	 */
	public String getGraphTitle(int graphIndex) {
		return null;
	}
//	
//	static public void putAddrThreadPeriod(long addrThreadPeriod) {
//		uidToAddrThreadPeriod.put(NpiInstanceRepository.getInstance().activeUid(), addrThreadPeriod);
//	}
//	
//	static public long getAddrThreadPeriod() {
//		return uidToAddrThreadPeriod.get(NpiInstanceRepository.getInstance().activeUid());
//	}
}
