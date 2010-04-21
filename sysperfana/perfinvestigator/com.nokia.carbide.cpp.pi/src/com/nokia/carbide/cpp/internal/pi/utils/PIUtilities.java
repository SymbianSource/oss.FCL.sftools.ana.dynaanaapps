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

package com.nokia.carbide.cpp.internal.pi.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.analyser.StreamFileParser;
import com.nokia.carbide.cpp.internal.pi.manager.PluginRegisterer;
import com.nokia.carbide.cpp.internal.pi.manager.PluginRegistry;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITraceSMP;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;

public abstract class PIUtilities
{
    private static String previous_path;
	
	//this is needed by createFileChooser method
    private static String getPath(String fileName)
    {
		if (fileName.length() != 0)
		{
			File file = new File(fileName);
			if (file.exists())
			{
				return fileName.substring(0,fileName.lastIndexOf(System.getProperty(Messages.getString("PIUtilities.fileSeparatorProperty")))); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
    }

	public static String selectAFile()
	{
		//file chooser
		FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);

		String tracefileName = fileDialog.open();

	    File selected = new File(tracefileName);
		
		if ((selected != null) && selected.exists() && selected.isFile()) {
			return selected.getAbsolutePath();
		}
		
	    return null;
	}
	
	public static String selectADirectory()
	{
		FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);

		String tracefileName = fileDialog.open();

	    File selected = new File(tracefileName);
		
		if ((selected != null) && selected.exists() && selected.isDirectory()) {
			return selected.getAbsolutePath();
		}
		
	    return null;
	}
	
	/*************** stuff moved from profile reader *************/
	
	public static File getAFile(String filter) throws Exception
	{
	      
	    return getAFile(false, filter);
	}
	  
	//loads and saves a file, TestGUI import menu
	public static File getAFile(boolean new_file, String filter) throws Exception
	{
		if (previous_path == null)
		{
	        previous_path = System.getProperty(Messages.getString("PIUtilities.userDirPropert")); //$NON-NLS-1$
		}

		// get a file via dialog
		// For now, require a .pi or .bap file
		FileDialog fileDialog;
		
		if (!new_file)
		{
			fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
		} else {
			fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		}

		fileDialog.setFilterPath(previous_path);
		
	  	if (filter != null && filter.length() != 0) {
	  		PIFileFilter fileFilter = new PIFileFilter(filter);
		    fileDialog.setFilterExtensions(new String[] { "." + filter }); //$NON-NLS-1$

		    String fileFilterName = fileFilter.getDescription();
		    if (fileFilterName != null) {
		    	fileDialog.setFilterNames(new String[] { fileFilterName });
		    }
		}

		String tracefileName = fileDialog.open();

		File selected = null;

		if (tracefileName != null) {
			selected = new File(tracefileName);
		
			if ((selected != null) && selected.exists()) {
	            if (!new_file)
	            {
		     		if (   (!tracefileName.endsWith(".dat")) //$NON-NLS-1$
		     			&& (!tracefileName.endsWith("base64")) //$NON-NLS-1$
		                && (!tracefileName.endsWith(".bap")) //$NON-NLS-1$
		                && (!tracefileName.endsWith(".map")) //$NON-NLS-1$
		            	   //performance framework files
		                && (!tracefileName.endsWith(".csv")) //$NON-NLS-1$
		                && (!tracefileName.endsWith(".txt"))) //$NON-NLS-1$
		            {
		            	System.out.println(Messages.getString("PIUtilities.notSupported1") + tracefileName + Messages.getString("PIUtilities.notSupported2")); //$NON-NLS-1$ //$NON-NLS-2$
		            	throw new Exception(Messages.getString("PIUtilities.notSupported3") + tracefileName + Messages.getString("PIUtilities.notSupported4")); //$NON-NLS-1$ //$NON-NLS-2$
		            }
	            } else {
	            	// new file
		            if (filter == "jpg") //$NON-NLS-1$
		            {
		            	if (!selected.getName().endsWith(".jpg")) //$NON-NLS-1$
		            	{
		            		selected = new File(selected.getAbsolutePath() + ".jpg"); //$NON-NLS-1$
		            	}
		            }
		            else if (filter == "bap") //$NON-NLS-1$
		            {
		            	if (!selected.getName().endsWith(".bap")) //$NON-NLS-1$
		            	{
		            		selected = new File(selected.getAbsolutePath() + ".bap"); //$NON-NLS-1$
		            	}
		            }
		            else if (filter == "csv") //$NON-NLS-1$
		            {
		            	if (!selected.getName().endsWith(".csv")) //$NON-NLS-1$
		            	{
		            		selected = new File(selected.getAbsolutePath() + ".csv"); //$NON-NLS-1$
		            	}
		            }
	            }
			} else {
				selected = null;
			}
		}
		    
	    if (selected != null) 
	    {
	    	/*config.setLastOpened(selected);*/
	    	previous_path = getPath(selected.toString());
	    }

	    return selected;
	}
	
	public static void saveCsvPrint(File f, String print) throws Exception
	{
		if (f == null)
			return;
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(print.getBytes(Messages.getString("PIUtilities.defaultCharacterSet"))); //$NON-NLS-1$
		fos.flush();
		fos.close();
	}
	
	public static void saveFile(File f, String print_data) throws Exception
	{
		if (f == null)
			return;
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(print_data.getBytes(Messages.getString("PIUtilities.defaultCharacterSet"))); //$NON-NLS-1$
		fos.flush();
		fos.close();
	    
	}
	
    public static boolean isSymbolFileOk(String symbol_file)
    {
      if (symbol_file.endsWith("symbol")) //$NON-NLS-1$
    	{
    		GeneralMessages.showErrorMessage(Messages.getString("PIUtilities.cannotLoadSymbolFile")); //$NON-NLS-1$
    		return false;
    	}
    	return true;
    }
    
    /**
     * Returns a set of plugins for trace IDs present in the trace file of the given path. 
     * Plugins which create editor pages are first in the list. 
     * @param absTraceFilePath path of the trace file to use
     * @return list of ITrace plugins for the given trace file
     * @throws IOException in case of problems accessing the trace file
     */
    public static List<ITrace> getPluginsForTraceFile (String absTraceFilePath) throws IOException{
    	
    	File traceFile = new File(absTraceFilePath);
    	StreamFileParser sfp = new StreamFileParser(traceFile);
    	
    	Set<Integer> traceTypesInFile = sfp.allTraceType();
    	sfp = null; //dispose parser structures
    	
    	List<ITrace> ret = new ArrayList<ITrace>();
    	PluginRegisterer.registerAllPlugins();
		Enumeration<AbstractPiPlugin> e = PluginRegistry.getInstance().getRegistryEntries();
		while(e.hasMoreElements()) {
			AbstractPiPlugin plugin = e.nextElement();
			if (plugin instanceof ITrace) {
				ITrace tracePlugin = (ITrace) plugin;
				
				if (traceTypesInFile.contains(tracePlugin.getTraceId()) || (plugin instanceof ITraceSMP && containsTraceId(traceTypesInFile, ((ITraceSMP)plugin).getTraceIdsSMP()))) {
						ret.add(tracePlugin);
				}
			}
		}
		
		// sort by trace id: plugins that create editor pages have to be first
		// this is a design limitation of PI
    	ret = sortPlugins(ret);
		return ret;
    }
    
    /**
     * Sorts the plugins by trace ids
     * @param plugins the list of plugins to sort
     * @return the sorted list
     */
    public static List<ITrace> sortPlugins(List<ITrace> plugins){
		Collections.sort(plugins, new Comparator<ITrace>(){
			public int compare(ITrace arg0, ITrace arg1) {
				return arg0.getTraceId() - arg1.getTraceId();
			}
		});
    	return plugins;
    }

    /**
     * Indicates whether the given set of traces IDs contains any of the trace IDs in traceIdsSMP.
     * @param traceTypesInFile Trace IDs present in the trace file
     * @param traceIdsSMP trace IDs to check against (typically SMP trace IDs for one plugin)
     * @return true if any matching trace IDs are found, false otherwise
     */
	private static boolean containsTraceId(Set<Integer> traceTypesInFile,
			int[] traceIdsSMP) {
		
		for (int traceID : traceIdsSMP) {
			if (traceTypesInFile.contains(Integer.valueOf(traceID))){
				return true;
			}
		}
		
		return false;
	}
}
