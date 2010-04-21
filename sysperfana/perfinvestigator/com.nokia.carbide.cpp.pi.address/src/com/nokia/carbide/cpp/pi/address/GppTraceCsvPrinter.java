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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;


public class GppTraceCsvPrinter
{
    private StringWriter csvWriter;
    
    // directory for the address trace plugin
    private String directory = System.getProperty("user.dir") + File.separator   //$NON-NLS-1$
    						+ "plugins" + File.separator  //$NON-NLS-1$
    						+ AddressPlugin.getDefault().getBundle().getSymbolicName() + "_"  //$NON-NLS-1$
    						+ AddressPlugin.getDefault().getBundle().getHeaders().get("Bundle-Version");  //$NON-NLS-1$
    private String eka1 = "fixed_thread_list_eka1_for_csv_print.txt"; //$NON-NLS-1$
    private String eka2 = "fixed_thread_list_eka2_for_csv_print.txt"; //$NON-NLS-1$
    private static final int eka1Mode = 1;
    private static final int eka2Mode = 2;
    
    public String getCsvPrint()
    {
        if (csvWriter == null)
        	return null;
        else
        	return csvWriter.toString();
    }
    
    public GppTraceCsvPrinter(GppTrace trace)
    {
    	String threadListFile;
    	File f;

    	int mode = solveMode(trace);
    	if (mode == eka1Mode)
    		threadListFile = directory + File.separator + eka1;
    	else if (mode == eka2Mode)
    		threadListFile = directory + File.separator + eka2;
    	else
    	{
    		System.out.println(Messages.getString("GppTraceCsvPrinter.1"));  //$NON-NLS-1$
    		return;
    	}
    	
    	f = new File(threadListFile);
    	if (!f.exists())
    	{
    		System.out.println(Messages.getString("GppTraceCsvPrinter.2") + threadListFile + Messages.getString("GppTraceCsvPrinter.3"));   //$NON-NLS-1$ //$NON-NLS-2$
    		return;
    	}
    	csvWriter = new StringWriter();
    	
    	Enumeration pts = trace.getSortedThreadsElements();
    	if (pts == null)
    		return;
    	formCsvPrint(pts, f);
    }
    
    private Vector getFixedThreadTable(File fixedThreadFile)
    {
    	try 
    	{
    		BufferedReader br = new BufferedReader(new FileReader(fixedThreadFile));
			String line;
			Vector v = new Vector();
			while ((line = br.readLine()) != null)
			{
				String threadName;
				String mappedName = ""; //$NON-NLS-1$
    			int mappedNameStart = line.indexOf('{');
				int mappedNameEnd = line.indexOf('}');
				if (mappedNameStart != -1 && mappedNameEnd != -1)
				{
					mappedName = line.substring(mappedNameStart + 1, mappedNameEnd);
					threadName = line.substring(0, mappedNameStart).trim();
				}
				else
					threadName = line.trim();
				v.add(new ObjectMap(threadName, mappedName));
			}
			br.close();
			return v;
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch(IOException e1)
		{
			e1.printStackTrace();
		}
		return null;
    	
    }
    
    private void formCsvPrint(Enumeration profiledThreads, File threadFile)
    {
    	Hashtable<String,float[]> activityByName = new Hashtable<String,float[]>();
    	Vector fixedThreads = getFixedThreadTable(threadFile);
    	Vector<ObjectMap> resultVector = new Vector<ObjectMap>();
    	boolean nullListFormed = false;
    	int[] nullList = null;
    	
    	while (profiledThreads.hasMoreElements())
    	{
    		ProfiledThread pt = (ProfiledThread)profiledThreads.nextElement();
    		if (!nullListFormed)
    		{
    			nullList = new int[pt.getActivityList().length];
    			nullListFormed = true;
    		}
    		String name = formatThreadName(pt.getNameString());
    		if (activityByName.containsKey(name))
    		{
    			float[] existingList = activityByName.get(name);
    			float[] combinedList = combineLists(pt.getActivityList(), existingList);
    			activityByName.put(name, combinedList);
    		}
    		else
    			activityByName.put(name, pt.getActivityList());
    	}
    	
    	for (Enumeration e = fixedThreads.elements(); e.hasMoreElements();)
    	{
    		ObjectMap om = (ObjectMap)e.nextElement();
    		boolean threadFound = false;
    		for (Enumeration<String> enu = activityByName.keys(); enu.hasMoreElements();)
    		{
    			String threadName = enu.nextElement();
    			if (threadName.startsWith((String)om.object))
    			{
    				threadFound = true;
    				String mappedName = (String)om.mappedObject;
    				if (mappedName != "") //$NON-NLS-1$
    					resultVector.add(new ObjectMap(mappedName, activityByName.get(threadName)));
    				else
    					resultVector.add(new ObjectMap((String)om.object, activityByName.get(threadName)));
    				break;
    			}
    		}
    		if (!threadFound)
    		{
    			String mappedName = (String)om.mappedObject;
				if (mappedName != "") //$NON-NLS-1$
					resultVector.add(new ObjectMap(mappedName, nullList));
				else
					resultVector.add(new ObjectMap((String)om.object, nullList));
    		}
    	}
    	if (resultVector.size() != 0)
    		writeCsvPrint(resultVector);
    }
    
    private void writeCsvPrint(Vector<ObjectMap> resultVector)
    {
    	csvWriter.write(",Time"); //$NON-NLS-1$
    	int csvTableHeight = ((int[])(resultVector.elementAt(0)).mappedObject).length;
    	int csvTableWidth = 0;
    	for (Enumeration<ObjectMap> e = resultVector.elements(); e.hasMoreElements();)
    	{
    		String key = (String)(e.nextElement()).object;
    		csvWriter.write("," + key); //$NON-NLS-1$
    	}
    	csvWriter.write("\r\n"); //$NON-NLS-1$
    	csvTableWidth = resultVector.size();
    	
    	for (int i = 0; i < csvTableHeight; i++)
    	{
    		csvWriter.write("R" + (i + 1) + "," + i/10f); //$NON-NLS-1$ //$NON-NLS-2$
    		for (int j = 0; j < csvTableWidth; j++)
    		{
    			ObjectMap om = resultVector.elementAt(j);
    			int[] tmp = (int[])om.mappedObject;
    			csvWriter.write("," + tmp[i]/100d); //$NON-NLS-1$
    		}
    		csvWriter.write("\r\n"); //$NON-NLS-1$
    	}
    }
    
    private String formatThreadName(String name)
    {
    	int firstPartEnd = name.indexOf('[');
    	int lastPartStart = name.indexOf(':');
    	int lastPartEnd = name.lastIndexOf('_');
    	if (firstPartEnd == -1 || lastPartStart == -1 || lastPartEnd == -1)
    		return name;
    	String firstPart = name.substring(0, firstPartEnd);
    	String lastPart = name.substring(lastPartStart, lastPartEnd);
    	return firstPart + lastPart;
    	
    }
    
    private float[] combineLists(float[] p1List, float[] p2List)
    {
    	if (p1List.length < p2List.length)
    		return null;
    	float[] combinedList = new float[p2List.length];
    	for (int i = 0; i < p2List.length; i++)
    	{
    		combinedList[i] = p2List[i] + p1List[i];
    	}
    	return combinedList;
    }
    
    private int solveMode(GppTrace trace)
    {
    	String processName = ((GppSample)trace.samples.elementAt(0)).thread.process.name;
    	if (processName == null)
    		return -1;
    	int start = processName.indexOf('[');
    	int end = processName.indexOf(']');
    	String processId = processName.substring(start + 1, end);
    	if (start == -1 || end == -1)
    		return -1;
    	if (processId.length() > 3)
    		return 1;
    	else
    		return 2;
    }
    
    static private class ObjectMap
    {	
    	public Object object;
    	public Object mappedObject;
    	public ObjectMap (Object object, Object mappedObject)
    	{
    		this.object = object;
    		this.mappedObject = mappedObject;
    	}
    }  
}
