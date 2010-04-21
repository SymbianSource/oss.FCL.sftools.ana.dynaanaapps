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

package com.nokia.carbide.cpp.pi.priority;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.model.GenericThread;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IReportable;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;


/**
 * The main plugin class to be used in the desktop.
 */
public class PriorityPlugin extends AbstractPiPlugin
		implements ITrace, IClassReplacer, IReportable {

	//The shared instance.
	private static PriorityPlugin plugin;
	
	private static DecimalFormat format = new DecimalFormat(Messages.getString("PriorityPlugin.zeroFormat")); //$NON-NLS-1$

	
	private PriTrace trace = null;
	private Hashtable<Integer,String> priStringById;

	private static void setPlugin(PriorityPlugin newPlugin)
	{
		plugin = newPlugin;
	}

	/**
	 * The constructor.
	 */
	public PriorityPlugin() {
		setPlugin(this);
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf("com.nokia.carbide.cpp.pi.priority.PriSample") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.priority.PriSample") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.PriSample") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.priTracePlugin.PriSample") != -1) //$NON-NLS-1$
		{
			return PriSample.class;
		}
		else if (   className.indexOf("[Lcom.nokia.carbide.cpp.pi.priority.PriThread") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lcom.nokia.carbide.pi.priority.PriThread") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lfi.vtt.bappea.model.PriThread;") != -1 //$NON-NLS-1$
				 || className.indexOf("[Lfi.vtt.bappea.priTracePlugin.PriThread") != -1) //$NON-NLS-1$
		{
			return PriThread[].class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.priority.PriThread") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.priority.PriThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.PriThread") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.priTracePlugin.PriThread") != -1) //$NON-NLS-1$
		{
			return PriThread.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.priority.PriTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.priority.PriTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.PriTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.priTracePlugin.PriTrace") != -1) //$NON-NLS-1$
		{
			return PriTrace.class;
		}
		else return null;
	}
	
	public Class getTraceClass() 
	{
		return PriTrace.class;
	}

	public void initialiseTrace(GenericTrace trace /*, ProfileVisualiser pv*/) 
	{
		if (trace == null)
			return;
		this.trace = (PriTrace)trace;

	  	GenericThread[] threads =  this.trace.getThreads();
		int threadCount = threads.length;

		ArrayList<PriSample>[] priData = new ArrayList[threadCount];
		Hashtable<Integer,ArrayList<PriSample>> priDataById = new Hashtable<Integer,ArrayList<PriSample>>();
		priStringById = new Hashtable<Integer,String>();

		//create priority data structure and inserts thread IDs into hashtable
		for (int i = 0; i < threadCount; i++)
		{
			priData[i] = new ArrayList<PriSample>();
			priDataById.put(threads[i].threadId, priData[i]);
		}
		
		//insert priority data into hashtable based on thread id
		for (Enumeration e = this.trace.getSamples(); e.hasMoreElements();)
		{
			PriSample tmp = (PriSample) e.nextElement();
			priDataById.get(tmp.thread.threadId).add(tmp);
		}
		
		for (Enumeration e = priDataById.keys(); e.hasMoreElements();)
		{
			Integer id = (Integer)e.nextElement();
			priStringById.put(id, this.getPriorityString((ArrayList)priDataById.get(id)));
		}
		
		priDataById.clear();
		

		Enumeration e = PluginInitialiser.getPluginInstances("com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
		if (e != null)
		{
			Event event = new Event();
			event.data = priStringById;	

			while (e.hasMoreElements())
			{
				IEventListener plugin = (IEventListener)e.nextElement();

				plugin.receiveEvent("priority_init", event); //$NON-NLS-1$
			}
		}
		
	  	System.out.println(Messages.getString("PriorityPlugin.priorityTraceParsed")); //$NON-NLS-1$
	}

	public String getTraceName() {
		return "Priority"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("PriorityPlugin.0"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 4;	//same id than MEM trace has because PRI trace is parsed from a MEM trace file
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ParsedTraceData parseTraceFile(File file /*, ProgressBar progressBar*/) throws Exception 
	{
		 try
	        {
	            PriTraceParser priParser;
	            priParser = new PriTraceParser(file);
	            return priParser.parse(file);
	        } catch (Exception e)
	        {
	            e.printStackTrace();
	            throw e;
	        }
	}
	
	private String getPriorityString(ArrayList priorityList)
	{
		if (priorityList == null || priorityList.size() == 0)
		{
			return Messages.getString("PriorityPlugin.unsolvedPriority"); //$NON-NLS-1$
		}
		else
		{
			String priorityString = ""; //$NON-NLS-1$
			int priority = -1;
			for (Iterator i = priorityList.iterator(); i.hasNext(); )
			{
				PriSample sample = (PriSample)i.next();
				if (priority != sample.priority)
				{
					priority = sample.priority;
					priorityString += Messages.getString("PriorityPlugin.priorityString1") + format.format(priority) + Messages.getString("PriorityPlugin.priorityString2") + sample.sampleNum/1000.0 + Messages.getString("PriorityPlugin.priorityString3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			return priorityString;
		}
	}

	public Hashtable<Integer,Object> getSummaryTable(double startTime, double endTime) 
	{
		PriThread[] pts = (PriThread[])trace.getThreads();
		Hashtable<Integer,String> tmpTable = new Hashtable<Integer,String>();
		for (int i = 0; i < pts.length; i++)
		{
			tmpTable.put(pts[i].threadId, pts[i].processName + "::" + pts[i].threadName + "_" + pts[i].threadId);   //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (this.priStringById != null)
		{
			Enumeration<Integer> e = priStringById.keys();
			Hashtable<Integer,Object> summary = new Hashtable<Integer,Object>();
			while (e.hasMoreElements())
			{
				Vector<Object> data = new Vector<Object>();
				Integer key = e.nextElement();
				data.add(tmpTable.get(key));
				data.add(((String)priStringById.get(key)).trim());
				summary.put(key, data);
			}
			return summary;
		}
		return null;
	}

	public String getGeneralInfo() 
	{
		return null;
	}

	public ArrayList<String> getColumnNames() 
	{
		ArrayList<String> names = new ArrayList<String>();
		names.add(Messages.getString("PriorityPlugin.thread")); //$NON-NLS-1$
		names.add(Messages.getString("PriorityPlugin.priority")); //$NON-NLS-1$
		return names;
	}
	
	public ArrayList<Boolean> getColumnSortTypes() 
	{
		ArrayList<Boolean> sortTypes = new ArrayList<Boolean>();
		sortTypes.add(SORT_BY_NAME);
		sortTypes.add(SORT_BY_NUMBER);
		return sortTypes;
	}

	public String getActiveInfo(Object arg0, double startTime, double endTime) {
		return null;
	}

	public MenuManager getReportGeneratorManager() {
		return null;
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
	public static PriorityPlugin getDefault() {
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
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.priority", path); //$NON-NLS-1$
	}
}
