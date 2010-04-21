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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;

import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.manager.PluginRegistry;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.RefinableTrace;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IAnalysisItem;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IExportItem;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IImportMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IRecordable;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.test.EnabledTrace;
import com.nokia.carbide.cpp.internal.pi.test.IProvideTraceAdditionalInfo;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalyser;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.test.BappeaAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.test.TraceAdditionalInfo;
import com.nokia.carbide.cpp.internal.pi.utils.PluginClassLoader;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.util.PIExceptionRuntime;


/*
 * Class for reading a processed profile data file and setting up the
 * editor pages, initial graphs, and initial tables
 */
public class ProfileReader
{
	// objects (mainly traces) read, it's lifetime exist between loadAnalysisFile for last usage in loadAnalysisFile
	static private ArrayList<Object> readObjects = new ArrayList<Object>();
	static private ArrayList<Hashtable> recObjects = new ArrayList<Hashtable>();
	static private ProfileReader instance;
	private static ILock lock = WorkspaceJob.getJobManager().newLock();

	public static ProfileReader getInstance() {
		if (instance == null) {
			instance = new ProfileReader();
		}
		
		return instance;
	}
	
	private ProfileReader()
	{
		//singleton
	}
	public boolean readTraceFile(ITrace plugin, File[] traceFiles, AnalyserDataProcessor dataInstance, int instanceUID){

		// prepare the trace repository
		try 
		{
			ParsedTraceData parsedTraceData = null;
			if (traceFiles.length == 1){
				parsedTraceData = plugin.parseTraceFile(traceFiles[0]);
			} else {
				parsedTraceData = plugin.parseTraceFiles(traceFiles);
			}
			
			if (parsedTraceData != null) 
			{
				// each resolvable trace is primarily resolved first 
				// with its own functionresolver(s)
				if (parsedTraceData.traceData instanceof RefinableTrace)
				{
					if (parsedTraceData.functionResolvers != null && parsedTraceData.functionResolvers.length > 0)
					{
						for (int i = 0; i < parsedTraceData.functionResolvers.length; i++)
						{
							FunctionResolver fr = parsedTraceData.functionResolvers[i];
							((RefinableTrace)parsedTraceData.traceData).refineTrace(fr);
						}
					}
				}
				
				TraceDataRepository.getInstance().insertTraceCollection(plugin.getTraceClass(), parsedTraceData, instanceUID);	
				addToMenus(plugin);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
		return true;
	
	}

	// read a plugin's information from a profile data file
	public boolean readTraceFile(ITrace plugin, File traceFile, AnalyserDataProcessor dataInstance, int instanceUID){
		return readTraceFile(plugin, new File[]{traceFile}, dataInstance, instanceUID);
	}
	
	public void loadAnalysisFile(String filePath, String displayName, IProgressMonitor progressMonitor, int instanceUID) throws IOException, InterruptedException
	{
		final int workUnitsForLoad = AnalyserDataProcessor.TOTAL_PROGRESS_COUNT * 20 / 100;
		int workUnitsLeft = workUnitsForLoad;
		progressMonitor.worked(1); // kick it start to please user
		AnalyserDataProcessor.getInstance().checkCancelledThrowIE();
		
	    System.out.println(Messages.getString("ProfileReader.0") + displayName);  //$NON-NLS-1$

	    // find and record all PI plugins that implement the specified interface
	    PluginInitialiser.invokePluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace"); //$NON-NLS-1$

		// the file contains Java objects that have been gziped
	    FileInputStream fis = new FileInputStream(filePath);
		GZIPInputStream ziss = new GZIPInputStream(fis);
		BufferedInputStream bis = new BufferedInputStream(ziss);

		ObjectInputStream ois = new ObjectInputStream(bis)
	    {   	
	        @SuppressWarnings("unchecked") //$NON-NLS-1$
			protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	        {
	        	// each object read must have a class corresponding to a plugin class
	        	String name = desc.getName();
	        	
                Class c = PluginInitialiser.getPluginClass(name);
	    		
	    		if (c == null)
	    		{
	    			try {
						c = Class.forName(name);
					} catch (ClassNotFoundException e) {
						// see if we have a replacement
						PluginClassLoader pcl = (PluginClassLoader)PluginInitialiser.getPluginClassLoader();
						// don't catch for class not found exception, they did it on purpose 
						// to back out of this missing plugin that exist in data file
						c = pcl.findClass(name);
					}	
	    		}
	    		
	       		return c;
	        }
	    };

	    readObjects.clear();

	    String notFoundMessage = ""; //$NON-NLS-1$
	    
		//loads all objects
	    try
	    {
	        while(true)
	        {
            	Object ou = null;
            	try
            	{
	                ou = ois.readObject();
            	}
	    	    catch (ClassNotFoundException cnfe)
	    	    {
	    		    //probably the plugin which has this class is not loaded
    	        	ou = null;
    	        	if (notFoundMessage.length() != 0)
    	        		notFoundMessage += "\n";  //$NON-NLS-1$
    	        	notFoundMessage += "Cannot find class " + cnfe.getMessage() //$NON-NLS-1$
					+ Messages.getString("ProfileReader.1");  //$NON-NLS-1$
	    	    }
    	        if (ou != null)
    	        	readObjects.add(ou);
	            // allocate what's left to roughly all plugins loaded, even some of them may not be in profiled sample
	            progressMonitor.worked(workUnitsLeft / PluginRegistry.getInstance().getRegistrySize());
	            workUnitsLeft -= workUnitsLeft / PluginRegistry.getInstance().getRegistrySize();
	            AnalyserDataProcessor.getInstance().checkCancelledThrowIE();
	        }
	    }
	    catch(EOFException eof)
	    {
	    	if (eof.getMessage() == null)
	        System.out.println(Messages.getString("ProfileReader.2"));  //$NON-NLS-1$
		    	else
		    	{
		    		System.out.println(Messages.getString("ProfileReader.3") + eof.getMessage());  //$NON-NLS-1$
		    		eof.printStackTrace();
		    	}
	    }
	    catch (InvalidClassException ie)
	    {
	    	// assume that the reason you got here was a UID mismatch
	    	
	        // close the readers
	        System.out.println(Messages.getString("ProfileReader.4"));  //$NON-NLS-1$
	        ois.close();
	        fis.close();
	        bis.close();
	        ziss.close();
	        
	        notFoundMessage = ""; //$NON-NLS-1$

	        // fix the UID mismatch problem
	        fis = new FileInputStream(filePath);
	        
	        ziss = new GZIPInputStream(fis);

	        InputStream fixedStream = fixUIDProblem(ziss);

	        bis = new BufferedInputStream(fixedStream);

	        ois = new ObjectInputStream(bis)
	        {       
	            @SuppressWarnings("unchecked") //$NON-NLS-1$
				protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	            {
		        	// each object read must have a class corresponding to a plugin class
		        	String className = desc.getName();
		        	
		        	Class c = PluginInitialiser.getPluginClass(className);
	                
	                if (c == null)
	                {
	                    try {
	                        c = Class.forName(className);
	                    }
	                    catch (ClassNotFoundException e1) {
	                            throw e1;
	                    }
	                }
	                
	                return c;
	            }
	        };

	        // try again to read the objects
            readObjects.clear();
	        try
	        {
	            while (true)
	            {
	            	Object ou = null;
	            	try
	            	{
		                ou = ois.readObject();
	            	}
	    	        catch (ClassNotFoundException cnfe)
	    	        {
	    			    //probably the plugin which has this class is not loaded
	    	        	ou = null;
	    	        	if (notFoundMessage.length() != 0)
	    	        		notFoundMessage += "\n";  //$NON-NLS-1$
	    	        	notFoundMessage += Messages.getString("ProfileReader.5") + cnfe.getMessage()  //$NON-NLS-1$
	    	        			+ Messages.getString("ProfileReader.6");  //$NON-NLS-1$
	    	        }
	    	        if (ou != null)
	    	        	readObjects.add(ou);
		            // allocate what's left to roughly all plugins loaded, even some of them may not be in profiled sample
		            progressMonitor.worked(workUnitsLeft / PluginRegistry.getInstance().getRegistrySize());
		            workUnitsLeft -= workUnitsLeft / PluginRegistry.getInstance().getRegistrySize();
		            AnalyserDataProcessor.getInstance().checkCancelledThrowIE();
	            }
	        }   
	        catch (EOFException eof)
	        {
	            System.out.println(Messages.getString("ProfileReader.7"));  //$NON-NLS-1$
	        }
	        catch (Exception eih)
	        {
	        	eih.printStackTrace();
	        }
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    
	    // close the readers
	    ois.close();
	    fis.close();
	    bis.close();
	    ziss.close();
	    
	    progressMonitor.worked((workUnitsForLoad * 98 / 100) - workUnitsLeft);	// assume reading takes 98%
	    AnalyserDataProcessor.getInstance().checkCancelledThrowIE();
	    workUnitsLeft = workUnitsForLoad * 2 / 100;

	    // display routine wants to see Analysis info first,
	    // and the rest in the order of what you want it to
	    // be displayed, GPP first, and then MEM
		Iterator<Object> readObjectsItr = readObjects.iterator();
		Object analysisInfoObj = null;
		Object gppObj = null;
		Object memObj = null;
		while (readObjectsItr.hasNext())
		{
			Object objFromFile = readObjectsItr.next();
			if (objFromFile.getClass().getName().endsWith("PIAnalysisInfo")) //$NON-NLS-1$
			{
				readObjectsItr.remove();
				analysisInfoObj = objFromFile;
			}
			if (objFromFile.getClass().getName().endsWith("BappeaAnalysisInfo")) //$NON-NLS-1$
			{
				readObjectsItr.remove();
				
				if (analysisInfoObj == null)
					analysisInfoObj = objFromFile;
			}
			if (objFromFile.getClass().getName().endsWith("GppTrace")) //$NON-NLS-1$
			{
				readObjectsItr.remove();
				gppObj = objFromFile;
			}
			if (objFromFile.getClass().getName().endsWith("MemTrace")) //$NON-NLS-1$
			{
				readObjectsItr.remove();
				memObj = objFromFile;
			}
		}
		if (memObj != null)
		{
			readObjects.add(0, memObj);
		}
		if (gppObj != null)
		{
			readObjects.add(0, gppObj);
		}
		if (analysisInfoObj != null)
		{
			readObjects.add(0, analysisInfoObj);
		}
		progressMonitor.worked(workUnitsForLoad - workUnitsLeft);
		AnalyserDataProcessor.getInstance().checkCancelledThrowIE();

		// if any problems were found, print them
	    if (notFoundMessage.length() != 0) {
	    	GeneralMessages.showWarningMessage(notFoundMessage);
	    }
	}
	  
	// setup data from all objects in NPI file, e.g. setup trace repository
	public void processDataReadFromNpiFile(AnalyserDataProcessor dataInstance)
	{
		int uid = NpiInstanceRepository.getInstance().activeUid();
    	AnalysisInfoHandler infoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();

		if (readObjects == null)
			return;
		
		recObjects.clear();
		
		Object o;
		int nextTrace = 1;
	    
	    if (readObjects.size() > 0) 
	    	o = readObjects.get(0);
	    else
	    	o = new Object();

		// the first element must identify the analysis info type
	    String className = o.getClass().getCanonicalName();
	    if (o instanceof PIAnalysisInfo)
	    {
	        try
	        {
	            PIAnalysisInfo info = (PIAnalysisInfo) o;
	            infoHandler.analysisDataReader(info);
	            System.out.println(Messages.getString("ProfileReader.8") + infoHandler.getFileVersion());  //$NON-NLS-1$
	        }
	        catch (Exception eu)
	        {
	            System.out.println(Messages.getString("ProfileReader.9"));  //$NON-NLS-1$
	            return;
	        }
	    }
	    else if (className.indexOf("BappeaAnalysisInfo") != -1) //$NON-NLS-1$
	    {
	        try
	        {
	        	// Old-style analysis info
	            BappeaAnalysisInfo info = (BappeaAnalysisInfo) o;
	            infoHandler.analysisDataReader(info);
	            System.out.println(Messages.getString("ProfileReader.10") + infoHandler.getFileVersion());  //$NON-NLS-1$
	        }
	        catch (Exception eu)
	        {
	            System.out.println(Messages.getString("ProfileReader.11"));  //$NON-NLS-1$
	            return;
	        }
	    }
	    else
	    {
		    //analysis info was not stored in old files
	        System.out.println(Messages.getString("ProfileReader.12"));  //$NON-NLS-1$
            nextTrace = 0;
            PIAnalysisInfo info = null;
            infoHandler.analysisDataReader(info);	// try this and make updater attempts conversion
	    }

	    LinkedHashMap<Class, ParsedTraceData> traces = TraceDataRepository.getInstance().getTraceCollection(uid);

		// Note: The first trace encountered MUST be the address/thread (GPP) trace
	    while (nextTrace < readObjects.size() && o != null)
	    {
	    	o = readObjects.get(nextTrace++);
	    	
	    	if (o instanceof GenericTrace)
	    	{
	    		GenericTrace trace = (GenericTrace)o;
	    		
	    		// iterate through all PI plugins that implement the given interface
	    		Enumeration<AbstractPiPlugin> enumeration = PluginInitialiser.getPluginInstances(uid, "com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace");  //$NON-NLS-1$
		    	while (enumeration.hasMoreElements())
		        {
		    		final ITrace plugin = (ITrace)enumeration.nextElement();
		    		AbstractPiPlugin p = (AbstractPiPlugin)plugin;
		    		NpiInstanceRepository.getInstance().addPlugin(uid, p);

		        	if (plugin.getTraceClass() != null && plugin.getTraceClass().isInstance(o))
		        	{
		        		// add this trace to the trace repository
		        		ParsedTraceData ptd = new ParsedTraceData();
		        		ptd.traceData = trace;
		        		TraceDataRepository.getInstance().insertTraceCollection(plugin.getTraceClass(), ptd, uid);
		        	}
		        }
	    	}
	    	else if (o instanceof Hashtable)
	    	{
	    		recObjects.add((Hashtable)o);
	    	}
        } 
	    
	    // We are done with this
	    readObjects.clear();
	    
		// In general it's a good idea to keep memory footprint low
		// most plugin allocate locals with new in initialiseTrace(GenericTrace)
		// so there should be a bunch of memory to be collected here
		System.runFinalization();
		System.gc();
	}
	
	public void setAddtionalDataForRecordable() {
		final int uid = NpiInstanceRepository.getInstance().activeUid();
		// do the graphic painting with indicator, SWT require it to be in UI thread
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				Enumeration<AbstractPiPlugin> enumeration = PluginInitialiser.getPluginInstances(uid, "com.nokia.carbide.cpp.internal.pi.plugin.model.IRecordable"); //$NON-NLS-1$
				while (enumeration.hasMoreElements())
				{
					IRecordable recPlugin = (IRecordable)enumeration.nextElement();
					ITrace tracePlugin = (ITrace)recPlugin;
					for (Hashtable table : recObjects) {
						Object data = table.get(tracePlugin.getTraceClass());
						if (data != null && data instanceof Serializable)
						{
							for (int i = 0; i < recPlugin.getGraphCount(); i++)
								recPlugin.setAdditionalData(i, (Serializable)data);
						}
					}
				}	
			}
		});
		recObjects.clear();
	}
	  
	private static final long UID1 = -6477250745688244253L;
	private static final long UID1Correct = -8144591614894549185L;
	private static final long UID2 = -7847355697200551872L;
	private static final long UID2Correct = -8082342532211331507L;
	private static final long UID3 = -7057416050237320718L;
	private static final long UID3Correct = 8411844143949264073L;


	private InputStream fixUIDProblem(InputStream inputStream) throws IOException
	{
		int[] UID1Array = {
				(int) ((UID1 >>> 56) & 0xff), (int) ((UID1 >>> 48) & 0xff),
				(int) ((UID1 >>> 40) & 0xff), (int) ((UID1 >>> 32) & 0xff),
				(int) ((UID1 >>> 24) & 0xff), (int) ((UID1 >>> 16) & 0xff),
				(int) ((UID1 >>>  8) & 0xff), (int)  (UID1 & 0xff)
		};
		int[] UID1CorrectArray = {
				(int) ((UID1Correct >>> 56) & 0xff), (int) ((UID1Correct >>> 48) & 0xff),
				(int) ((UID1Correct >>> 40) & 0xff), (int) ((UID1Correct >>> 32) & 0xff),
				(int) ((UID1Correct >>> 24) & 0xff), (int) ((UID1Correct >>> 16) & 0xff),
				(int) ((UID1Correct >>>  8) & 0xff), (int)  (UID1Correct & 0xff)
		};
		int[] UID2Array = {
				(int) ((UID2 >>> 56) & 0xff), (int) ((UID2 >>> 48) & 0xff),
				(int) ((UID2 >>> 40) & 0xff), (int) ((UID2 >>> 32) & 0xff),
				(int) ((UID2 >>> 24) & 0xff), (int) ((UID2 >>> 16) & 0xff),
				(int) ((UID2 >>>  8) & 0xff), (int)  (UID2 & 0xff)
		};
		int[] UID2CorrectArray = {
				(int) ((UID2Correct >>> 56) & 0xff), (int) ((UID2Correct >>> 48) & 0xff),
				(int) ((UID2Correct >>> 40) & 0xff), (int) ((UID2Correct >>> 32) & 0xff),
				(int) ((UID2Correct >>> 24) & 0xff), (int) ((UID2Correct >>> 16) & 0xff),
				(int) ((UID2Correct >>>  8) & 0xff), (int)  (UID2Correct & 0xff)
		};
		int[] UID3Array = {
				(int) ((UID3 >>> 56) & 0xff), (int) ((UID3 >>> 48) & 0xff),
				(int) ((UID3 >>> 40) & 0xff), (int) ((UID3 >>> 32) & 0xff),
				(int) ((UID3 >>> 24) & 0xff), (int) ((UID3 >>> 16) & 0xff),
				(int) ((UID3 >>>  8) & 0xff), (int)  (UID3 & 0xff)
		};
		int[] UID3CorrectArray = {
				(int) ((UID3Correct >>> 56) & 0xff), (int) ((UID3Correct >>> 48) & 0xff),
				(int) ((UID3Correct >>> 40) & 0xff), (int) ((UID3Correct >>> 32) & 0xff),
				(int) ((UID3Correct >>> 24) & 0xff), (int) ((UID3Correct >>> 16) & 0xff),
				(int) ((UID3Correct >>>  8) & 0xff), (int)  (UID3Correct & 0xff)
		};
		
		// puukko-based solution
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int[] temp = new int[8];

		int tempIndex = 0;

		int UID1Index = 0;
		int UID2Index = 0;
		int UID3Index = 0;
		
		int data = -1;

		// Now we have exactly three UID problems, which is tricky
		// since a mismatch in one might be a match in the other
		while (true)
		{
			data = bis.read();
	        if (data == -1)
	        	break;
	        
	        if (data == UID1Array[UID1Index])
	        	UID1Index++;
	        else
	        	UID1Index = 0;
	        
	        if (data == UID2Array[UID2Index])
	        	UID2Index++;
	        else
	        	UID2Index = 0;
	        
	        if (data == UID3Array[UID3Index])
	        	UID3Index++;
	        else
	        	UID3Index = 0;

        	// no matches
        	if (UID1Index + UID2Index + UID3Index == 0)
        	{
        		// write the match buffer bytes & the new one, clear the buffer, and restart the matches 
        		if (tempIndex != 0) {
        			for (int i = 0; i < tempIndex; i++)
        				baos.write(temp[i]);
        			tempIndex = 0;
        		}

        		baos.write(data);
        	} else {
        		// store the byte
        		temp[tempIndex++] = data;

        		// if any counter is at 8, we have a match - only look for one
        		// if the buffer is full, write any unmatched strings
            	if (UID1Index == 8)
            	{
            		for (int i = 0; i < 8; i++)
            			baos.write(UID1CorrectArray[i]);
                    System.out.println(Messages.getString("ProfileReader.13"));  //$NON-NLS-1$
                    
                    UID1Index = 0;
                    tempIndex = 0;
                    break;
            	}
            	else if (UID2Index == 8)
            	{
            		for (int i = 0; i < 8; i++)
            			baos.write(UID2CorrectArray[i]);
                    System.out.println(Messages.getString("ProfileReader.14"));  //$NON-NLS-1$
                    UID2Index = 0;
                    tempIndex = 0;
                    break;
            	}
            	else if (UID3Index == 8)
            	{
            		for (int i = 0; i < 8; i++)
            			baos.write(UID3CorrectArray[i]);
                    System.out.println(Messages.getString("ProfileReader.priorityTraceUIDconflict")); //$NON-NLS-1$
 
                    UID3Index = 0;
                    tempIndex = 0;
                    break;
            	} else if (tempIndex == 8) {
            		// write all the bytes not in a match
            		int least = UID1Index < UID2Index ? UID1Index : UID2Index;
            		least = least < UID3Index ? least : UID3Index;
            		
            		for (int i = 0; i < least; i++)
	        			baos.write(temp[i]);
            		
	        		// move the remaining unwritten bytes to start of the temp array
	        		int k = 0;
	        		for (int i = least; i < 8; i++)
	        		{
	        			tempIndex--;
	        			temp[k++] = temp[i];
	        		}
            	}
        	}
		}
		
		// write the match buffer bytes & the new one, clear the buffer, and restart the matches 
		if (tempIndex != 0) {
			for (int i = 0; i < tempIndex; i++)
				baos.write(temp[i]);
		}
		
		while (data != -1) {
			data = bis.read();
			
			if (data != -1)
				baos.write(data);
		}
	      
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	    baos.close();
	    bis.close();
	    return bais;
	}
	
	private void closeOutputStreams(FileOutputStream fos, GZIPOutputStream gzo, ObjectOutputStream oos) {
		try {
			if (oos != null) {
				oos.flush();
				oos.close();
			}
			if (gzo != null) {
				gzo.flush();
				gzo.close();
			}
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		} catch (IOException e) {
			GeneralMessages.showErrorMessage(e.getMessage());
		}
	}

	void writeAnalysisFile(String fileName, IProgressMonitor monitor, String suffixTaskName, int instanceUID) throws InvocationTargetException, InterruptedException {
		final int workUnitsForSave = AnalyserDataProcessor.TOTAL_PROGRESS_COUNT * 20 / 100;
		FileOutputStream fos = null;
		GZIPOutputStream gzo = null;
		ObjectOutputStream oos = null;

		try {
			lock.acquire();
			File f = new File(fileName);
		    if (f.exists())
		    {
		        f.delete();
		    }
		    fos = new FileOutputStream(f);
		    gzo = new GZIPOutputStream(fos);
		    oos = new ObjectOutputStream(gzo);
		    
			String taskName = Messages.getString("ProfileReader.15")+ f.getName(); //$NON-NLS-1$
			if(suffixTaskName != null){
				taskName += " "+suffixTaskName; //$NON-NLS-1$
			}
		    monitor.setTaskName(taskName );  //$NON-NLS-1$
		    monitor.worked(workUnitsForSave * 1 / 100);
			int workUnitsLeft = workUnitsForSave;
			monitor.worked(workUnitsForSave * 1 / 100); // kick it start to please user
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
			// even other update routine among traces are not activated, any save should be in the newest version number
			NpiInstanceRepository.getInstance().getAnalysisInfoHandler(instanceUID).setFileVersion(PIAnalyser.NPIFileFormat);
			AnalysisInfoHandler analysisInfoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
			PIAnalysisInfo info = analysisInfoHandler.getAnalysisInfo();
			
			if (info.trace_info == null)
				info.trace_info = new Vector<Object>();
			else
				info.trace_info.clear();
			
			if (info.additional_info == null)
				info.additional_info = new Vector<Object>();
			else
				info.additional_info.clear();
			
			// count the number of traces and create the list of enabled traces
		    int numberOfTraces = 0;
		    Iterator<ParsedTraceData> enuTraces = TraceDataRepository.getInstance().getTraceCollectionIter(instanceUID);
		    AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().getAnalysisInfoHandler(instanceUID);
		    TraceAdditionalInfo traceAdditionalInfos = new TraceAdditionalInfo();

		    while (enuTraces.hasNext()) {
		    	Object object = enuTraces.next();

		    	numberOfTraces++;
		    	
		    	if (object instanceof ParsedTraceData) {
		    		ParsedTraceData parsedTraceData = (ParsedTraceData) object;
		    		if (parsedTraceData.traceData != null) {
			    		Class traceClass = parsedTraceData.traceData.getClass();

						// this code is clumsy because the plugin, not the trace, has the trace ID info
			    		Enumeration<AbstractPiPlugin> enuPlugins = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace"); //$NON-NLS-1$
						while (enuPlugins.hasMoreElements())
						{
							ITrace plugin = (ITrace)enuPlugins.nextElement();
							if (   (traceClass == plugin.getTraceClass())
								|| (   (plugin instanceof IClassReplacer)
									&& (((IClassReplacer)plugin).getReplacedClass(traceClass.getCanonicalName()) == traceClass))) {
						    	info.trace_info.add(new EnabledTrace(plugin.getTraceId(), plugin.getTraceName()));
						    	if (plugin instanceof IProvideTraceAdditionalInfo) {
									((IProvideTraceAdditionalInfo)plugin).analysisInfoHandlerToAdditonalInfo(traceAdditionalInfos, handler);						    		
						    	}
							}
						}			
		    		}
		    	}
		    }
		    
		    // write trace specific info if exist
		    if (traceAdditionalInfos.size() > 0) {
		    	info.additional_info.add(traceAdditionalInfos);
		    }
		    
		    // no traces were found in the DAT or NPI file
		    if (numberOfTraces == 0) {
		    	throw new PIExceptionRuntime(Messages.getString("ProfileReader.16"));  //$NON-NLS-1$
		    }

		    // write the analysis data
			try {
				oos.writeObject(info);
			}catch (Exception oops) {
				oops.printStackTrace();
			}

			Iterator<ParsedTraceData> e = TraceDataRepository.getInstance().getTraceCollectionIter(instanceUID);
		    while (e.hasNext())
		    {
		    	ParsedTraceData ptd = (ParsedTraceData)e.next();
		    	if (ptd != null)
		    	{
		    		GenericTrace gt = ptd.traceData;
		    		if (gt != null)
		    		{
		    			try {
		    			  oos.writeObject(gt);
			    		}catch (Exception oops) {
			    			  oops.printStackTrace();
			    		}
		    		}
		    	}
		        // let's assume this loading take 94
		    	monitor.worked((workUnitsForSave * 94 / 100) / numberOfTraces);
	    		if (monitor.isCanceled()) {
	    			throw new InterruptedException(Messages.getString("ProfileReader.17"));  //$NON-NLS-1$
	    		}
	            workUnitsLeft -= 94 / numberOfTraces;
		    }
		    // assume writing traces takes 90%
		    monitor.worked((workUnitsForSave * 9 / 10) - workUnitsLeft);
			if (monitor.isCanceled()) {
				throw new InterruptedException(Messages.getString("ProfileReader.18"));  //$NON-NLS-1$
			}
	        workUnitsLeft = 10;

		    Hashtable<Class,Serializable> additionalData = new Hashtable<Class,Serializable>();
		      
		    // iterate through PI plugins that implement the given interface
		    Enumeration<AbstractPiPlugin> enumeration = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.IRecordable"); //$NON-NLS-1$
			while (enumeration.hasMoreElements())
			{
				IRecordable recPlugin = (IRecordable)enumeration.nextElement();
				Serializable data = recPlugin.getAdditionalData();
				ITrace tracePlugin = (ITrace)recPlugin;
				if (data != null && tracePlugin.getTraceName() != null)
					additionalData.put(tracePlugin.getTraceClass(), data);
			}
			
			oos.writeObject(additionalData);
		    
			if (monitor.isCanceled()) {
				throw new InterruptedException(Messages.getString("ProfileReader.19")); //$NON-NLS-1$
			}
			
		    monitor.worked(workUnitsForSave * 5 / 100);
		} catch (InterruptedException e) {
			lock.release();
			throw e;
		} catch (Exception e){
			lock.release();
			throw new InvocationTargetException(e);
		} finally {
			lock.release();
		}

		closeOutputStreams(fos, gzo, oos);
	}
	  
	private void addToMenus(ITrace plugin) {
		if (plugin instanceof IExportItem) {
			((IExportItem)plugin).getExportAction();
		}

		if (plugin instanceof IImportMenu)
		{
			((IImportMenu)plugin).getIncludeAction();
		}
		
		if (plugin instanceof IAnalysisItem) {
			((IAnalysisItem)plugin).getAnalysisAction();
		}

		if (plugin instanceof IViewMenu) {
			((IViewMenu)plugin).getViewOptionManager();
		}
	}
	
	public void setTraceMenus(ArrayList<AbstractPiPlugin> plugins, int instanceUID)
	{
		// Note: This ignores at least:
		// 			com.nokia.carbide.cpp.internal.pi.plugin.model.IOpenOtherMenu
		//			com.nokia.carbide.cpp.internal.pi.plugin.model.IOpenOtherTrace
		
		Enumeration<AbstractPiPlugin> enu;
		AbstractPiPlugin plugin;
		Action action;
		MenuManager manager;
		
		PIPageEditor.initialiseMenuManager();
		
		enu = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.IExportItem"); //$NON-NLS-1$
		while (enu.hasMoreElements())
		{
			// can only add export items if the plugin is associated with this tab
			plugin = (AbstractPiPlugin)enu.nextElement();
			if (   (plugin instanceof IExportItem)
				&& (plugins != null)
				&& (plugins.indexOf(plugin) >= 0)) {
				action = ((IExportItem)plugin).getExportAction();
				PIPageEditor.addExportAction(action);
			}
		}
		
		enu = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.IImportMenu"); //$NON-NLS-1$
		while (enu.hasMoreElements())
		{
			// can add import items for any tab
			plugin = (AbstractPiPlugin)enu.nextElement();
			if (plugin instanceof IImportMenu) {
				action = ((IImportMenu)plugin).getIncludeAction();
				PIPageEditor.addIncludeAction(action);
			}
		}
		
		enu = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.IAnalysisItem"); //$NON-NLS-1$
		while (enu.hasMoreElements())
		{
			// can only add analysis items if the plugin is associated with this tab
			plugin = (AbstractPiPlugin)enu.nextElement();
			if (   (plugin instanceof IAnalysisItem)
				&& (plugins != null)
				&& (plugins.indexOf(plugin) >= 0)) {
				action = ((IAnalysisItem)plugin).getAnalysisAction();
				PIPageEditor.addReportAction(action);
			}
		}

		enu = PluginInitialiser.getPluginInstances(instanceUID, "com.nokia.carbide.cpp.internal.pi.plugin.model.IViewMenu"); //$NON-NLS-1$
		while (enu.hasMoreElements())
		{
			// can only add view items if the plugin is associated with this tab
			plugin = (AbstractPiPlugin)enu.nextElement();
			if (   (plugin instanceof IViewMenu)
				&& (plugins != null)
				&& (plugins.indexOf(plugin) >= 0)) {
					manager = ((IViewMenu)plugin).getViewOptionManager();
					if (manager != null)
						PIPageEditor.addViewOptionManager(manager);
				}
		}
	}
}
