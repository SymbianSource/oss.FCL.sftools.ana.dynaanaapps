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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import com.nokia.s60tools.crashanalyser.files.*;

/**
 * This class handles all file export tasks.  
 *
 */
public final class FileExportManager {
	public final static String EXTENSION_ZIP = ".zip";
	
	private FileExportManager() {
		// not to be implemented
	}
	
	private static void showMessage(String message, Shell shell) {
		MessageDialog.openInformation(
				shell,
				"Crash Analyser",
				message);
	}

	/**
	 * Exports all selected files to Zip as Html format
	 * @param selection  IStructuredSelection of MainView's selected rows
	 * @param shell shell for window
	 */
	public static void ExportSelectedFilesAsHtmlToZip(ISelection selection, Shell shell) {
		// return if nothing is selected
		if (selection == null || selection.isEmpty()) {
			showMessage("Please select first the files you want to export", shell);
			return;
		}

		List<CrashFileBundle> files = new ArrayList<CrashFileBundle>();
		@SuppressWarnings("unchecked")
		Iterator i = ((IStructuredSelection)selection).iterator();
		// go through all selected files
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
			// handle only files which contains xml file. only files which contain
			// xml can be saved as html
			if (cFileBundle.hasXml()) {
				files.add(cFileBundle);						
			}
		}
		// no files contained xml, return
		if (files.isEmpty()) {
			showMessage("Only decoded files can be exported as HTML", shell);
			return;
		}
		
		String saveFilePath = FileOperations.saveAsDialog("Crash Analyser - Select zip location", new String[] {"*.zip"}, shell);
		if (saveFilePath != null) {
			List<String> htmlFiles = new ArrayList<String>();
			// get all html files
			for (int j = 0; j < files.size(); j++) {
				CrashFileBundle cfb = files.get(j);
				File htmlFile = cfb.getHtmlFile(true);
				if (htmlFile != null)
					htmlFiles.add(htmlFile.getAbsolutePath());
			}
			 
			FileOperations.zipFiles(htmlFiles.toArray(new String[htmlFiles.size()]), saveFilePath);
		}		
	}
	
	/**
	 * Exports all selected files to Zip as Xml format (.crashxml or .xml)
	 * @param selection  IStructuredSelection of MainView's selected rows
	 * @param shell shell for window
	 */
	public static void ExportSelectedFilesAsXmlToZip(ISelection selection, Shell shell) {
		// return if nothing is selected
		if (selection == null || selection.isEmpty()) {
			showMessage("Please select first the files you want to export", shell);
			return;
		}

		ArrayList<String> files = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Iterator i = ((IStructuredSelection)selection).iterator();
		// go through all selected files
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
			// handle only files which contains xml file
			if (cFileBundle.hasXml()) {
				files.add(cFileBundle.getXmlFilePath());						
			}
		}
		// no files contained xml, return
		if (files.isEmpty()) {
			showMessage("Only decoded files can be exported as XML", shell);
			return;
		}
		
		String saveFilePath = FileOperations.saveAsDialog("Crash Analyser - Select zip location", new String[] {"*.zip"}, shell);
		if (saveFilePath != null) {
			FileOperations.zipFiles(files.toArray(new String[files.size()]), saveFilePath);
		}
	}
	
	/**
	 * Exports a selected file to Html format. If more than one file is selected, does nothing
	 * @param selection  IStructuredSelection of MainView's selected rows
	 * @param shell shell for window
	 */
	public static void ExportSelectedFileToHtml(ISelection selection, Shell shell) {
		// return if nothing is selected or if more than one items are selected
		if (selection == null || 
			selection.isEmpty()) {
			showMessage("Please select first a file you want to export", shell);
			return;
		}
			
		if (((IStructuredSelection)selection).size() > 1) {
			showMessage("Please select a single file", shell);
			return;
		}
		
		List<CrashFileBundle> files = new ArrayList<CrashFileBundle>();
		@SuppressWarnings("unchecked")
		Iterator i = ((IStructuredSelection)selection).iterator();
		// go through all selected files
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
			// handle only files which contains xml file. only files which contain
			// xml can be saved as html
			if (cFileBundle.hasXml()) {
				files.add(cFileBundle);
			}
		}
		
		// return if no xml files were found or if more than one file was found
		if (files.isEmpty() || files.size() > 1) {
			showMessage("Only decoded files can be exported as HTML", shell);
			return;
		}

		String saveFilePath = FileOperations.saveAsDialog("Crash Analyser - Select XML location", 
															new String[] {"*.htm"}, 
															shell);
		if (saveFilePath != null) {
			files.get(0).saveAsHtml(new File(saveFilePath));
		}
	}
	
	/**
	 * Exports a selected file to Xml format (.crashxml or .xml). If more than one 
	 * file is selected, does nothing.
	 * @param selection  IStructuredSelection of MainView's selected rows
	 * @param shell shell for window
	 */
	public static void ExportSelectedFileToXml(ISelection selection, Shell shell) {
		// return if nothing is selected or if more than one items are selected
		if (selection == null || 
			selection.isEmpty()) {
			showMessage("Please select first a file you want to export", shell);
			return;
		}
		if (((IStructuredSelection)selection).size() > 1) {
			showMessage("Please select a single file", shell);
			return;
		}
		
		boolean crashXml = false;
		ArrayList<String> files = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Iterator i = ((IStructuredSelection)selection).iterator();
		// go through all selected files
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
			// handle only files which contains xml file
			if (cFileBundle.hasXml()) {
				files.add(cFileBundle.getXmlFilePath());
				crashXml = cFileBundle.isFullyDecoded();
			}
		}
		
		// return if no xml files were found or if more than one file was found
		if (files.isEmpty() || files.size() > 1) {
			showMessage("Only decoded files can be exported as XML", shell);
			return;
		}

		String[] extension = {"*." + CrashAnalyserFile.SUMMARY_FILE_EXTENSION};
		if (crashXml)
			extension = new String[] {"*." + CrashAnalyserFile.OUTPUT_FILE_EXTENSION};
		
		String saveFilePath = FileOperations.saveAsDialog("Crash Analyser - Select XML location", extension, shell);
		if (saveFilePath != null) {
			FileOperations.copyFile(new File(files.get(0)), new File(saveFilePath), true);
		}
	}
	
	/**
	 * A single row in selection can have multiple files. This method first zips all files found
	 * in a single row in to a zip and then finally zips all "row zips" into one zip.
	 * @param selection  IStructuredSelection of MainView's selected rows
	 * @param shell shell for window
	 */
	public static void ExportSelectedFilesToZipInAllFormats(ISelection selection, Shell shell) {
		// return if nothing is selected
		if (selection == null || selection.isEmpty()) {
			showMessage("Please select first the files you want to export", shell);
			return;
		}

		List<CrashFileBundle> files = new ArrayList<CrashFileBundle>();
		@SuppressWarnings("unchecked")
		Iterator i = ((IStructuredSelection)selection).iterator();
		// go through all selected files
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
			// handle only files which are not empty nor emulator panics
			if (!cFileBundle.isEmpty() && !cFileBundle.isEmulatorPanic()) {
				files.add(cFileBundle);						
			}
		}
		// no files, return
		if (files.isEmpty())
			return;
		
		String saveFilePath = FileOperations.saveAsDialog("Crash Analyser - Select zip location", new String[] {"*.zip"}, shell);
		if (saveFilePath != null) {
			List<String> zipFiles = new ArrayList<String>();
			List<String> bundleFiles = new ArrayList<String>();
			// zip all bundle files to zips
			for (int j = 0; j < files.size(); j++) {
				boolean nameSet = false;
				CrashFileBundle cfb = files.get(j);
				String zipFileName = FileOperations.addSlashToEnd(cfb.getBundleFolder());
				// zip binary file
				UndecodedFile uf = cfb.getUndecodedFile();
				if (uf != null) {
					bundleFiles.add(uf.getFilePath());
					zipFileName += uf.getFileName() + EXTENSION_ZIP;
					nameSet = true;
				}
				// zip .crashxml file
				CrashFile cf = cfb.getCrashFile();
				if (cf != null) {
					bundleFiles.add(cf.getFilePath());
					if (!nameSet) {
						zipFileName += cf.getFileName() + EXTENSION_ZIP;
						nameSet = true;
					}
				}
				// zip .xml file
				SummaryFile sf = cfb.getSummaryFile();
				if (sf != null) {
					bundleFiles.add(sf.getFilePath());
					if (!nameSet) {
						zipFileName += sf.getFileName() + EXTENSION_ZIP;
						nameSet = true;
					}
				}
				// zip html file
				File htmlFile = cfb.getHtmlFile(true);
				if (htmlFile != null) {
					bundleFiles.add(htmlFile.getAbsolutePath());
					if (!nameSet) {
						zipFileName += htmlFile.getName() + EXTENSION_ZIP;
					}
				}
				
				if (!bundleFiles.isEmpty()) {
					FileOperations.zipFiles(bundleFiles.toArray(new String[bundleFiles.size()]), zipFileName);
					zipFiles.add(zipFileName);
				}
			}
			
			if (!zipFiles.isEmpty())
				FileOperations.zipFiles(zipFiles.toArray(new String[zipFiles.size()]), saveFilePath);
		}		
	}
}
