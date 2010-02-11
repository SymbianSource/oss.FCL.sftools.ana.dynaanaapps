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

package com.nokia.s60tools.crashanalyser.ui.editors;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import com.nokia.s60tools.crashanalyser.data.ErrorLibrary;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.interfaces.*;
import com.nokia.s60tools.crashanalyser.resources.ImageKeys;
import com.nokia.s60tools.crashanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.crashanalyser.model.*;
import java.io.*;

/**
 * A Crash Visualiser editor. Editor contains four tab pages:
 * Crash Data, Advanced, Errors & Warnings and XML.
 * 
 * Crash Data page contains summary data of the crash and crash reason
 * and description. Crash Data page also contain stack data if available.
 * 
 * Advanced page contains more advanced details about the crash, such as 
 * registers values, code segments, event log and cpsr details.
 * 
 * Errors & Warnings page contains all error and warning messages which 
 * may have occurred during the xml file creation.
 * 
 * XML tab page contains the raw xml data as an xml tree.
 *
 */
public class MultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener, 
																	IErrorLibraryObserver {
	// editor pages
	private SummaryPage summaryPage;
	private AdvancedPage advancedPage;
	private ErrorPage errorPage;
	private XmlPage xmlPage;
	
	// editor data consists of either crashFile or summaryFile
	private CrashFile crashFile = null;
	private SummaryFile summaryFile = null;
	
	private ErrorLibrary errorLibrary = null;
	private String crashFilePath = "";
	static final int ERROR_PAGE_INDEX = 2;
	
	public MultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	/**
	 * Creates Crash Data page
	 */
	void createSummaryPage() {
		summaryPage = new SummaryPage();
		
		// output.crashxml is opened via CrashAnalyser View
		if (crashFile != null) {
			int index = 
				addPage(summaryPage.createPage(new Composite(getContainer(), SWT.NONE), crashFile));
			setPageText(index, "Crash Data");
		// Summary xml is opened via CrashAnalyser View
		} else if (summaryFile != null) {
			int index = 
				addPage(summaryPage.createPage(new Composite(getContainer(), SWT.NONE), summaryFile));
			setPageText(index, "Crash Summary");
		// output.crashxml file opened from File > Open
		} else {
			int index = 
				addPage(summaryPage.createPage(new Composite(getContainer(), SWT.NONE)));
			setPageText(index, "Crash Data");
		}
	}
	
	/**
	 * Creates advanced page
	 */
	void createAdvancedPage() {
		advancedPage = new AdvancedPage();
		
		// output.crashxml is opened via CrashAnalyser View
		if (crashFile != null) {
			int index = addPage(advancedPage.createPage(new Composite(getContainer(), SWT.NONE), crashFile));
			setPageText(index, "Advanced");
		// Summary xml is opened via CrashAnalyser View			
		} else if (summaryFile != null) {
			int index = addPage(advancedPage.createPage(new Composite(getContainer(), SWT.NONE), summaryFile));
			setPageText(index, "Advanced");
		// output.crashxml file opened from File > Open			
		} else {
			int index = addPage(advancedPage.createPage(new Composite(getContainer(), SWT.NONE)));
			setPageText(index, "Advanced");
		}
	}
	
	/**
	 * Creates Errors & Warnings page
	 */
	void createErrorPage() {
		errorPage = new ErrorPage();
		
		// output.crashxml is opened via CrashAnalyser View
		if (crashFile != null) {
			if (crashFile.containsErrorsOrWarnings()) {
				int index = addPage(errorPage.createPage(new Composite(getContainer(), SWT.NONE), crashFile));
				setPageText(index, "Errors && Warnings");
			}
		// Summary xml is opened via CrashAnalyser View			
		} else if (summaryFile != null) {
			if (summaryFile.containsErrorsOrWarnings()) {
				int index = addPage(errorPage.createPage(new Composite(getContainer(), SWT.NONE), summaryFile));
				setPageText(index, "Errors && Warnings");
			}
		// output.crashxml file opened from File > Open			
		} else {
			int index = addPage(errorPage.createPage(new Composite(getContainer(), SWT.NONE)));
			setPageText(index, "Errors && Warnings");
		}
		
	}
	
	/**
	 * Creates XML page
	 */
	void createXmlPage() {
		xmlPage = new XmlPage();

		// output.crashxml is opened via CrashAnalyser View
		if (crashFile != null) {
			int index = addPage(xmlPage.createPage(new Composite(getContainer(), SWT.NONE), crashFile));
			setPageText(index, "XML");
		// Summary xml is opened via CrashAnalyser View			
		} else if (summaryFile != null) {
			int index = addPage(xmlPage.createPage(new Composite(getContainer(), SWT.NONE), summaryFile));
			setPageText(index, "XML");
		// output.crashxml file opened from File > Open			
		} else {
			int index = addPage(xmlPage.createPage(new Composite(getContainer(), SWT.NONE), crashFile));
			setPageText(index, "XML");
		}
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createSummaryPage();
		createAdvancedPage();
		createErrorPage();
		createXmlPage();
		
		// output.crashxml file was opened via File > Open
		// we need to read error library and CrashFile
		if (crashFilePath != null && !"".equals(crashFilePath)) {
			errorLibrary = ErrorLibrary.getInstance(this);
		} else {
			updatePages();
		}
	}
	
	public void errorLibraryReady() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				crashFile = CrashFile.read(new File(crashFilePath), errorLibrary);
				summaryPage.setFile(crashFile);
				advancedPage.setFile(crashFile);
				if (crashFile.containsErrorsOrWarnings()) {
					errorPage.setFile(crashFile);
				} else {
					removePage(ERROR_PAGE_INDEX);
				}
				xmlPage.setFile(crashFile);
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	public void updatePages() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				summaryPage.update();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		// not supported
	}

	/**
	 * editor's data can be saved as xml, html or txt.
	 */
	public void doSaveAs() {
		String[] filter = null;
		// we have a fully decoded file
		if (crashFile != null) {
			filter = new String[] { "*.crashxml", 
					   				"*.html",
					   				"*.txt"};
		// we have a summary file
		} else {
			filter = new String[] { "*.xml", 
					   				"*.html",
									"*.txt"};	
		}		
		String result = FileOperations.saveAsDialog("Crash Analyser - Save Crash File As", filter, getEditorSite().getShell());
		if (result != null && result.length() > 0) {
			// crash file exists and user did not select the current crash file
			if (crashFile != null && !result.equalsIgnoreCase(crashFile.getFilePath()))
				crashFile.writeTo(new File(result));
			// summary file exists and user did not select the current summary file
			else if (summaryFile != null && !result.equalsIgnoreCase(summaryFile.getFilePath()))
				summaryFile.writeTo(new File(result));
		}
	}
	
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		
		String fileName = "Crash Analyser";
		
		// We don't support files, must be CrashFile or SummaryFile object
		if (editorInput instanceof IFileEditorInput) {
			throw new PartInitException("Not Valid Crash Analyser File.");
			
		// CrashFile or SummaryFile
		} else {
			
			// output.crashxml file opened from Crash Analyser Main View
			if (editorInput instanceof CrashFile) {
				crashFile = (CrashFile)editorInput;
				fileName = crashFile.getFileName();
				super.setTitleImage(ImageResourceManager.getImage(ImageKeys.DECODED_FILE));
			
			// Summary xml file opened from Crash Analyser Main View
			} else if (editorInput instanceof SummaryFile) {
				summaryFile = (SummaryFile)editorInput;
				fileName = summaryFile.getFileName();
				super.setTitleImage(ImageResourceManager.getImage(ImageKeys.PARTIALLY_DECODED_FILE));

			// output.crashxml file opened from Carbide File menu
			} else {
				crashFilePath = ((FileStoreEditorInput)editorInput).getURI().getPath();
				fileName = editorInput.getName();
				super.setTitleImage(ImageResourceManager.getImage(ImageKeys.DECODED_FILE));
			}
		}
		super.init(site, editorInput);
		super.setPartName(fileName);
	}
	
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public void resourceChanged(final IResourceChangeEvent event){
		// nothing to be done
	}
	
}
