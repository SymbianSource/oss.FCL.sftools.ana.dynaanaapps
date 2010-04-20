/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
*/

package com.nokia.s60tools.crashanalyser.model;

import java.util.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorPart;
import com.nokia.s60tools.crashanalyser.files.*;

/**
 * Handles opening and closing of Crash Visualizer editors
 *
 */
public final class EditorHandler {
	private EditorHandler() {
		// not meant to be implemented
	}
	
	/**
	 * Closes all open editors which are Crash Analyser editors
	 */
	public static void closeAllEditors() {
		try {
	    	IWorkbench workbench = PlatformUI.getWorkbench();   
	    	if (workbench == null)
	    		return;
	    	
	    	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	    	if (window == null)
	    		return;
	    	
	    	String editorId = "com.nokia.s60tools.crashanalyser.ui.editors.MultiPageEditor";
	    	IWorkbenchPage page = window.getActivePage();
	    	if (page == null)
	    		return;
	    	
	    	IEditorReference[] editorReferences = page.getEditorReferences();
	    	if (editorReferences == null || editorReferences.length < 1)
	    		return;
	    	
	    	// go throug all editors
    		for (int i = 0; i < editorReferences.length; i++) {
    			IEditorReference ref = editorReferences[i];
    			String openEditorId = ref.getId();
    			// if editor is Crash Analyser editor, close it
    	    	if (editorId.compareToIgnoreCase(openEditorId) == 0) {
	    			page.closeEditor(ref.getEditor(true), true);
    	    	}
    		}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if given file is already open in editor area. If it is, that editor is
	 * brought as top most editor.
	 * @param file file which is to be checked
	 * @return true if editor was found and brought to top most, false if editor was not found
	 * or an error occurred
	 */
	static boolean bringEditorOnTopIfFileAlreadyOpen(CrashAnalyserFile file) {
		try {
			if (file == null)
				return false;
			
			// get workbench
	    	IWorkbench workbench = PlatformUI.getWorkbench();
	    	if (workbench == null)
	    		return false;
	    	
	    	// get workbench window
	    	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	    	if (window == null)
	    		return false;
	    	
	    	String editorId = "com.nokia.s60tools.crashanalyser.ui.editors.MultiPageEditor";
	    	
	    	// get workbench page
	    	IWorkbenchPage page = window.getActivePage();
	    	if (page == null)
	    		return false;
	    	
	    	// get open editors
	    	IEditorReference[] editorReferences = page.getEditorReferences();
	    	if (editorReferences == null || editorReferences.length < 1)
	    		return false;
	    	
	    	// go through all open editors
    		for (int i = 0; i < editorReferences.length; i++) {
    			IEditorReference ref = editorReferences[i];
    			String openEditorId = ref.getId();
    			// if editor is Crash Analyser editor
    	    	if (editorId.compareToIgnoreCase(openEditorId) == 0) {
    	    		IEditorPart part = ref.getEditor(true);
    	    		if (part == null)
    	    			continue;
    	    		
    	    		String title = part.getTitle();
    	    		String pageTitle = null;
    	    		if(file.getThread() != null) {
    	    			pageTitle = file.getThread().getFullName();
    	    		} else {
    	    			pageTitle = file.getFileName();
    	    		}
    	    		
	    			if (title.equalsIgnoreCase(pageTitle)) {
	    				page.bringToTop(part);
	    				return true;
	    			}
    	    	}
    		}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Closes all given editors
	 */
	public static void closeEditors(List<CrashFileBundle> files) {
		try {
			if (files == null || files.isEmpty())
				return;
			
			// get workbench
	    	IWorkbench workbench = PlatformUI.getWorkbench();
	    	if (workbench == null)
	    		return;
	    	
	    	// get workbench window
	    	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	    	if (window == null)
	    		return;
	    	
	    	String editorId = "com.nokia.s60tools.crashanalyser.ui.editors.MultiPageEditor";
	    	
	    	// get workbench page
	    	IWorkbenchPage page = window.getActivePage();
	    	if (page == null)
	    		return;
	    	
	    	// get open editors
	    	IEditorReference[] editorReferences = page.getEditorReferences();
	    	if (editorReferences == null || editorReferences.length < 1)
	    		return;
	    	
	    	// go through all open editors
    		for (int i = 0; i < editorReferences.length; i++) {
    			IEditorReference ref = editorReferences[i];
    			String openEditorId = ref.getId();
    			// if editor is Crash Analyser editor
    	    	if (editorId.compareToIgnoreCase(openEditorId) == 0) {
    	    		IEditorPart part = ref.getEditor(true);
    	    		if (part == null)
    	    			continue;
    	    		
    	    		String title = part.getTitle();
    	    		boolean close = false;
    	    		
    	    		// go through all given files and close editor for those
    	    		for (int j = 0; j < files.size(); j++) {
    	    			CrashFileBundle file = files.get(j);
    	    			if (title.equalsIgnoreCase(file.getAnalyzeFileName())) {
    	    				close = true;
    	    				break;
    	    			}
    	    		}
    	    		if (close)
    	    			page.closeEditor(ref.getEditor(true), true);
    	    	}
    		}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens up given crash analyser file to a Crash Analyser editor.
	 * @param file file to be opened
	 * @return true if success, false if not
	 */
	private static boolean openEditor(CrashAnalyserFile file) {
		try {
			if (bringEditorOnTopIfFileAlreadyOpen(file))
				return true;
			
	    	IWorkbench workbench = PlatformUI.getWorkbench();      
	    	if (workbench == null)
	    		return false;
	    	
	    	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	    	if (window == null)
	    		return false;
	    	
	    	String editorId = "com.nokia.s60tools.crashanalyser.ui.editors.MultiPageEditor";
	    	IWorkbenchPage page = window.getActivePage();
	    	if (page == null)
	    		return false;
	    	
    		if (file instanceof CrashFile)
    			page.openEditor((CrashFile)file,editorId );
    		else if (file instanceof SummaryFile)
    			page.openEditor((SummaryFile)file,editorId );
    		else
    			return false;
    		
    		return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Opens given full crash file to a Crash Analyser editor.
	 * @param file file to be opened
	 * @return true if success, false if not
	 */
	public static boolean openCrashAnalyserEditor(CrashFile file) {
		return openEditor(file);
	}
	
	/**
	 * Opens given summary crash file to a Crash Analyser editor.
	 * @param file file to be opened
	 * @return true if success, false if not
	 */
	public static boolean openCrashAnalyserEditor(SummaryFile file) {
		return openEditor(file);
	}
}
