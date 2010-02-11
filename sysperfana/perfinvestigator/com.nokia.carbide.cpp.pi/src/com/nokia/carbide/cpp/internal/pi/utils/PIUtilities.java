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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

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
}
