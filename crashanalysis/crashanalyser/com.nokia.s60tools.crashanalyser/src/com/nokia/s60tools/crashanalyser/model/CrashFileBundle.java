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

import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.files.SummaryFile.ContentType;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import java.io.*;
import java.util.List;

/**
 * CrashFileBundle class bundles up one folder under Crash Analyser plugin's folder. I.e. one
 * CrashFileBundle is one row in MainView. A bundle can contain an undecoded MobileCrash file, 
 * decoded .crashxml file, partially decoded .crashxml file or an emulator panic xml file. Or a 
 * combination of these. 
 * 
 * All of the files listed above are found e.g. from 
 * c:\my_carbide_workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser\CrashFiles\[bundle_folder].
 * One bundle contains all files under one [bundle_folder]. 
 * 
 * Bundle can also be an empty bundle, so that 'No crash files found' row can be shown in MainView.
 * 
 * Bundle can also be a waiting bundle, so that 'Loading files. Please wait' row can be shown in MainView.
 *
 * Bundle can also be a thread bundle. That is a row in the MainView 
 * which has no crash (i.e. a child in the treeview)
 */
public class CrashFileBundle {

	public static final int INDEX_TIME = 0;
	public static final int INDEX_THREAD = 1;
	public static final int INDEX_PANIC_CATEGORY = 2;
	public static final int INDEX_PANIC_CODE = 3;
	public static final int INDEX_FILE_NAME = 4;
	public static final String EXTENSION_HTML = ".htm";
	
	/**
	 * A binary crash file 
	 */
	UndecodedFile undecodedFile = null;
	
	/**
	 * Fully decoded crash file 
	 */
	CrashFile crashxmlFile = null;
	
	/**
	 * Partially decoded crash file 
	 */
	SummaryFile summaryFile = null;

	/**
	 * A xml file which contains information about one emulator panic
	 */
	PanicFile emulatorPanicFile = null;
	
	/**
	 * Bundle name for an empty or waiting bundle  
	 */
	String bundleName = "";
	
	/**
	 * Folder where all bundle's files can be found from
	 */
	String bundleFolder = "";
	
	/**
	 * If true, bundle is an empty or waiting bundle
	 */
	boolean emptyFile = false;
	
	/**
	 * If true, bundle is a thread bundle
	 */
	private boolean threadBundle = false;

	
	/**
	 * Directory from where files in this bundle originally came from.
	 * This data is not always present.
	 */
	String originatingDirectory = "";
	
	/**
	 * Used for creating an empty or waiting bundle
	 * @param empty If true, an empty bundle is created. If false, a waiting bundle is created. 
	 */
	public CrashFileBundle(boolean empty) {
		if (empty)
			bundleName = "No Crash Files Found.";
		else
			bundleName = "Loading files. Please wait.";
		emptyFile = true;
	}
	
	/**
	 * Creates a bundle from folder
	 * @param folder Bundle folder. Bundle's file will be read from here.
	 * @param library ErrorLibrary instance
	 */
	public CrashFileBundle(String folder, ErrorLibrary library) {
		bundleFolder = FileOperations.addSlashToEnd(folder);
		undecodedFile = UndecodedFile.read(bundleFolder);
		crashxmlFile = CrashFile.read(bundleFolder, library);
		emulatorPanicFile = PanicFile.read(bundleFolder, library);
		summaryFile = SummaryFile.read(bundleFolder, library);
		originatingDirectory = folder;
	}
	
	/**
	 * Initialise bundle with already read Crash file
	 * @param file crash file
	 */
	public CrashFileBundle(CrashFile file, String originalDirectory) {
		crashxmlFile = file;
		originatingDirectory = originalDirectory;
	}
	
	/**
	 * Initialise bundle with already read Summary file
	 * @param file summary file
	 */
	public CrashFileBundle(SummaryFile file, String originalDirectory) {
		summaryFile = file;
		originatingDirectory = originalDirectory;
	}

	/**
	 * Initialise bundle with already read Crash file
	 * @param file crash file
	 */
	public CrashFileBundle(CrashFile file, String originalDirectory, Thread thread) {
		threadBundle = true;
		crashxmlFile = (CrashFile) file.clone();
		crashxmlFile.setThread(thread);
		crashxmlFile.formatDescription();
		originatingDirectory = originalDirectory;
	}
	
	/**
	 * Initialise bundle with already read Summary file
	 * @param file summary file
	 */
	public CrashFileBundle(SummaryFile file, String originalDirectory, Thread thread) {
		threadBundle = true;
		summaryFile = (CrashFile) file.clone();
		summaryFile.setThread(thread);
		summaryFile.formatDescription();
		originatingDirectory = originalDirectory;
	}

	protected CrashFileBundle(String folder) {
		bundleFolder = FileOperations.addSlashToEnd(folder);
	}

	/**
	 * Creates a dummy bundle, which can be used e.g. for comparison reasons.
	 * A dummy bundle can be compared to another bundle.
	 * @param folder bundle folder
	 * @return crash file bundle
	 */
	public static CrashFileBundle createDummyBundle(String folder) {
		return new CrashFileBundle(folder);
	}
	
	/**
	 * MainView can use this to get description for each column in the grid
	 * @param index index of the column
	 * @return value for asked column
	 */
	public String getText(int index) {
		String retval = "";
		switch (index) {
			case INDEX_TIME:
				retval = getTime();
				break;
				
			case INDEX_THREAD:
				retval = getThreadName();
				break;
				
			case INDEX_PANIC_CATEGORY:
				retval = getPanicCategory();
				break;
				
			case INDEX_PANIC_CODE:
				retval = getPanicCode();
				break;
			
			case INDEX_FILE_NAME:
				retval = getFileName();
				break;
			
			default:
				break;
		}
		
		return retval;
	}
	
	/**
	 * Returns the file name for this bundle. File name depends on what types
	 * of files this bundle contains (or if this bundle is an empty or waiting bundle).
	 *   
	 * @return the file name for this bundle.
	 */
	public String getFileName() {
		if (emptyFile)
			return bundleName;
		
		String retval = "";
		if (undecodedFile != null) {
			retval = undecodedFile.getFileName();
		} else if (crashxmlFile != null) {
			retval = crashxmlFile.getFileName();
		} else if (summaryFile != null) {
			retval = summaryFile.getFileName();
		} else if (emulatorPanicFile != null) {
			retval = "Emulator Panic";
		}

		return retval;
	}
	
	/**
	 * Returns the name of .crashxml or .xml file is they exists
	 * @return the name of .crashxml or .xml file is they exists
	 */
	public String getAnalyzeFileName() {
		String retval = "";
		if (crashxmlFile != null) {
			retval = crashxmlFile.getFileName();
		} else if (summaryFile != null) {
			retval = summaryFile.getFileName();
		} 

		return retval;
	}

	/**
	 * Returns the panic code of this bundle. Panic code is available 
	 * only if bundle contains decoded files or emulator panic.
	 * 
	 * @return panic code or empty
	 */
	public String getPanicCode() {
		if (emptyFile)
			return "";
		
		String retval = "";
		if (crashxmlFile != null) {
			retval = crashxmlFile.getPanicCode();
		} else if (summaryFile != null) {
			retval = summaryFile.getPanicCode();
		} else if (emulatorPanicFile != null) {
			retval = emulatorPanicFile.getPanicCode();
		} else if (threadBundle) {
			retval = "";
		}
			
		
		return retval;
	}

	/**
	 * Returns the panic category of this bundle. Panic category is available 
	 * only if bundle contains decoded files or emulator panic.
	 * 
	 * @return panic category or empty
	 */
	public String getPanicCategory() {
		if (emptyFile)
			return "";
		
		String retval = "";

		if (crashxmlFile != null) {
			retval = crashxmlFile.getPanicCategory();
		} else if (summaryFile != null) {
			retval = summaryFile.getPanicCategory();
		} else if (emulatorPanicFile != null) {
			retval = emulatorPanicFile.getPanicCategory();
		} else if (undecodedFile != null) {
			retval = "Unknown";
		} 
		
		return retval;
	}
	
	/**
	 * Returns the name of the paniced thread. Thread name is available
	 * only if bundle contains decoded files or emulator panic.
	 * 
	 * @return paniced thread name or empty
	 */
	public String getThreadName() {
		if (emptyFile)
			return "";
		
		String retval = "";
		if (threadBundle && crashxmlFile != null) {
			retval = crashxmlFile.getThread().getFullName();
		} else if (threadBundle && summaryFile != null) {
			retval = summaryFile.getThread().getFullName();
		}else if (crashxmlFile != null) {
			retval = crashxmlFile.getThreadName();
		} else if (summaryFile != null) {
			retval = summaryFile.getThreadName();
		} else if (emulatorPanicFile != null) {
			retval = emulatorPanicFile.getThreadName();
		} else if (undecodedFile != null) {
			retval = "Unknown";
		} 

		return retval;
	}

	/**
	 * Returns the total thread count (all threads in all processes).
	 * Thread count is available
	 * only if bundle contains decoded files or emulator panic.
	 * 
	 * @return thread count or -1 if not available
	 */
	public int getTotalThreadCount() {
		if (emptyFile)
			return -1;
		
		if (threadBundle) {
			return 1;
		}
		
		int retval = -1;
		if (crashxmlFile != null) {
			retval = crashxmlFile.getTotalThreadCount();
		} else if (summaryFile != null) {
			retval = summaryFile.getTotalThreadCount();
		} else if (emulatorPanicFile != null) {
			retval = emulatorPanicFile.getTotalThreadCount();
		} else if (undecodedFile != null) {
			retval = -1;
		}
		
		return retval;
	}

	/**
	 * Returns the threads in this crash file (all threads in all processes).
	 * 
	 * @return thread count or -1 if not available
	 */
	public List<Thread> getThreads() {
		if (emptyFile || threadBundle)
			return null;
		
		if (crashxmlFile != null) {
			return crashxmlFile.getThreads();
		} else if (summaryFile != null) {
			return summaryFile.getThreads();
		} else if (emulatorPanicFile != null) {
			return emulatorPanicFile.getThreads();
		}
		return null;
	}

	/**
	 * Returns the time of a crash. Crash time is not always available
	 * (even in decoded crash files).  If crash time is not available,  
	 * the creation time of the crash file is returned.
	 * 
	 * @return crash time
	 */
	public String getTime() {
		if (emptyFile)
			return "";
		
		String retval = "";
		if (crashxmlFile != null) {
			retval = crashxmlFile.getTime();
			// crashxml does not always contain time
			if ("".equals(retval)) {
				// try to read time: when undecoded file was created
				if (undecodedFile != null) {
					retval = undecodedFile.getTime();
				// there was no undecoded file, read time: when crashxml file was created
				} else {
					retval = crashxmlFile.getCreated();
				}
			}
		}
		else if (summaryFile != null) {
			retval = summaryFile.getTime();
			// summary does not always contain time
			if ("".equals(retval)) {
				// try to read time: when undecoded file was created
				if (undecodedFile != null) {
					retval = undecodedFile.getCreated();
				// there was no undecoded file, read time: when summary.xml file was created
				} else {
					retval = summaryFile.getCreated();
				}
			}
		} else if (undecodedFile != null) {
			retval = undecodedFile.getCreated();
		} else if (emulatorPanicFile != null) {
			retval = emulatorPanicFile.getTime();
			if ("".equals(retval))
				retval = emulatorPanicFile.getCreated();
		} 

		return retval;
	}
	
	/**
	 * Returns the crash files ROM ID if available.
	 * @return ROM ID or empty
	 */
	public String getRomId() {
		if (isEmpty())
			return "";
		
		if (crashxmlFile != null) {
			return crashxmlFile.getRomId();
		} else if (summaryFile != null) {
			return summaryFile.getRomId();
		} else {
			return "";
		}
	}
	
	/**
	 * Returns whether this is an empty or waiting bundle.
	 * @return true if bundle is empty or waiting, false if not.
	 */
	public boolean isEmpty() {
		return emptyFile;
	}
	
	/**
	 * Returns whether this bundle contains an emulator panic
	 * @return true if this bundle contains an emulator panic, false if not
	 */
	public boolean isEmulatorPanic() {
		if (emulatorPanicFile != null)
			return true;
		
		return false;
	}
	
	/**
	 * Returns whether this bundle contains a fully decoded crash file (.crashxml)
	 * @return true if this bundle contains a fully decoded crash file, false if not
	 */
	public boolean isFullyDecoded() {
		if (crashxmlFile != null) 
			return true;
		
		return false;
	}
	
	/**
	 * Returns whether this bundle contains a partially decoded crash file (.xml)
	 * @return true if this bundle contains a partially decoded crash file, false if not
	 */
	public boolean isPartiallyDecoded() {
		if (summaryFile != null)
			return true;
		
		return false;
	}
	
	/**
	 * Returns whether this bundle is a thread bundle.
	 * @return true if this bundle is a thread bundle.
	 */
	public boolean isThread() {
		return threadBundle;
	}
	

	/**
	 * Returns whether this bundle contains any files.
	 * @return true if bundle contains files, false if not
	 */
	public boolean hasFiles() {
		if (crashxmlFile != null || summaryFile != null ||
			emulatorPanicFile != null || undecodedFile != null)
			return true;
		
		return false;
	}
	
	/**
	 * Returns whether this bundle contains xml files (.crashxml or .xml).
	 * @return true if bundle contains files, false if not
	 */
	public boolean hasXml() {
		if (crashxmlFile != null || summaryFile != null)
			return true;
			
		return false;
	}
	
	/**
	 * Returns undecoded crash file if exists
	 * @return undecoded crash file or null
	 */
	public UndecodedFile getUndecodedFile() {
		return undecodedFile;
	}
	
	/**
	 * Returns fully decoded crash file if exists
	 * @return fully decoded crash file or null
	 */
	public CrashFile getCrashFile() {
		return crashxmlFile;
	}
	
	/**
	 * Returns partially decoded crash file if exists
	 * @return partially decoded crash file or null
	 */
	public SummaryFile getSummaryFile() {
		return summaryFile;
	}
	
	/**
	 * Returns the directory from where the files in this bundle
	 * originally came from. This information is not always available.
	 * @return directory from where the files in this bundle originally came from. 
	 */
	public String getOriginatingDirectory() {
		return originatingDirectory;
	}
	
	/**
	 * Returns the location for .xml or .crashxml of this bundle.
	 * @return the location for .xml or .crashxml of this bundle.
	 */
	public String getXmlFilePath() {
		String retval = "";
		
		if (crashxmlFile != null) {
			retval = FileOperations.addSlashToEnd(bundleFolder) + crashxmlFile.getFileName();
		} else if (summaryFile != null) {
			retval = FileOperations.addSlashToEnd(bundleFolder) + summaryFile.getFileName();
		}
		
		return retval;
	}
	
	/**
	 * Deletes this bundle. I.e deletes all files under this
	 * bundle's folder and finally deletes the bundle folder
	 */
	public void delete() {
		try {
			if (!"".equals(bundleFolder)) {
				FileOperations.deleteFolder(bundleFolder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Tests whether this bundle still exists in the drive.
	 * If bundle is empty or waiting, true is always returned. 
	 * 
	 * @return true if bundle exists, false if not.
	 */
	public boolean exists() {
		if (isEmpty())
			return true;
		
		try {
			File f = new File(bundleFolder);
			if (f.isDirectory() && f.exists())
				return true;
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}
	
	/**
	 * Returns a description for this bundle. The description depends on which
	 * files this bundle contains.
	 * 
	 * @param full defines whether a full description or short description should be returned.
	 * @return file description
	 */
	public String getDescription(boolean full) {
		if (isEmpty())
			return "";
		
		if ((crashxmlFile != null && crashxmlFile.getContentType() == ContentType.REGMSG) ||
			(summaryFile  != null && summaryFile.getContentType()  == ContentType.REGMSG )) {
			return HtmlFormatter.formatRegistrationMessage();
		} else if ((crashxmlFile != null && crashxmlFile.getContentType() == ContentType.REPORT) ||
				   (summaryFile  != null && summaryFile.getContentType()  == ContentType.REPORT )) {
			return HtmlFormatter.formatReport();
		}
		
		if (crashxmlFile != null) {
			if (full)
				return crashxmlFile.getDescription();
			else
				return crashxmlFile.getShortDescription();
		} else if (summaryFile != null) {
			if (full)
				return summaryFile.getDescription();
			else
				return summaryFile.getShortDescription();
		} else if (emulatorPanicFile != null) {
			return emulatorPanicFile.getDescription();
		} else {
			return "";
		}
	}
	
	/**
	 * Returns this bundle's folder
	 * @return bundle's folder or empty
	 */
	protected String getBundleFolder() {
		return bundleFolder;
	}
	
	/**
	 * Returns html file if this bundle is a .crashxml or .xml file.
	 * @param create defines whether the html file should be generated if it doesn't exist, or not
	 * @return html file if success, null if not
	 */
	public File getHtmlFile(boolean create) {
		File htmlFile = null;
		
		// we have .crashxml file
		if (crashxmlFile != null) {
			String crashxml = FileOperations.addSlashToEnd(bundleFolder) + 
							  								crashxmlFile.getFileName() +
							  								EXTENSION_HTML;
			htmlFile = new File(crashxml);
			// if html file doesn't exists, create it
			if (!htmlFile.exists() && create) {
				crashxmlFile.writeTo(htmlFile);
				htmlFile = new File(crashxml);
				// creation of html failed
				if (!htmlFile.exists())
					htmlFile = null;
			}
		// we have .xml file
		} else if (summaryFile != null) {
			String crashxml = FileOperations.addSlashToEnd(bundleFolder) + 
															summaryFile.getFileName() +
															EXTENSION_HTML;
			// if html file doesn't exists, create it
			htmlFile = new File(crashxml);
			if (!htmlFile.exists() && create) {
				summaryFile.writeTo(htmlFile);
				htmlFile = new File(crashxml);
				// creation of html failed
				if (!htmlFile.exists())
					htmlFile = null;
			}
		}
		
		return htmlFile;
	}
	
	/**
	 * Saves .crashxml or .xml as given html file. This is done only if this
	 * bundle contains .crashxml or .xml files
	 * @param destinationFile html file for save as
	 * @return true if success, false if not
	 */
	public boolean saveAsHtml(File destinationFile) {
		File htmlFile = getHtmlFile(true);
		if (htmlFile != null)
			return FileOperations.copyFile(htmlFile, destinationFile, true);
		return false;
	}
	
	/**
	 * Checks if bundles are equal. Two bundles are equal if
	 * their bundleFolder is the same.
	 */
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (!(other instanceof CrashFileBundle))
			return false;
		
		CrashFileBundle othr = (CrashFileBundle)other;
		if (bundleFolder.compareToIgnoreCase(othr.getBundleFolder()) == 0)
			return true;
		return false;
	}
	
	public int hashCode() {
		return 0;
	}
}
