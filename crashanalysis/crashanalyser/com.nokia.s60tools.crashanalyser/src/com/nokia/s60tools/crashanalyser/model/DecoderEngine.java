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

import java.io.*;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.interfaces.IDecodingObserver;
import com.nokia.s60tools.crashanalyser.plugin.*;
import com.nokia.s60tools.crashanalyser.corecomponents.interfaces.CommandLineManager;
import com.nokia.s60tools.crashanalyser.data.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class handles the actual decoding Crash files process. Data entered by
 * user in Wizard pages are passed to this class and this class then handles it
 * properly.
 * 
 */
public class DecoderEngine extends Job {
	protected static final String TOOLS_FOLDER = "tools";
	protected static final String WORK_FOLDER = "work";
	protected static final String TEMPORARY_FOLDER = "temp";
	protected static final String CRASH_FILES_FOLDER = "CrashFiles";
	protected static final String MAP_FILES_ZIP_FOLDER = "MapFilesZip";
	protected static final String SELGE_EVENT_INI = "selge_event.ini";
	protected static final String ERROR_FILE_EXTENSION = "error";
	protected static final int MAX_FILE_COUNT = 500;

	public static enum PathTypes {
		CRASH, SYMBOL
	}

	private List<CrashFileBundle> crashFiles = null;
	private IDecodingObserver decodingObserver = null;
	private DecodingData decodingData = null;

	/**
	 * Constructor
	 */
	public DecoderEngine() {
		super("Handling Crash Files (Note! This might take several minutes)");
	}

	/**
	 * Returns path where selge_event.ini
	 * 
	 * @return path where selge_event.ini is located
	 */
	public static String getSelgeEventIniFile() {
		return FileOperations.addSlashToEnd(CrashAnalyserPlugin
				.getPluginInstallPath())
				+ FileOperations.addSlashToEnd(TOOLS_FOLDER) + SELGE_EVENT_INI;
	}

	/**
	 * Checks whether given file is a known crash file
	 * 
	 * @param file
	 *            crash file
	 * @return true if file seems to be a known crash file, false if not
	 */
	public static boolean isFileValidCrashFile(final String file) {
		return isFileValidCrashFile(new File(file));
	}

	/**
	 * Checks whether given file is a known crash file
	 * 
	 * @param file
	 *            crash file
	 * @return true if file seems to be a known crash file, false if not
	 */
	public static boolean isFileValidCrashFile(final File file) {
		try {
			// file must be a file and it must exist
			if (file.isFile() && file.exists()) {
				final String fileName = file.getName();
				// file extension must match known extension types
				if (fileName
						.endsWith(CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION)
						|| fileName
								.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION)
						|| fileName
								.endsWith(CrashAnalyserFile.D_EXC_FILE_EXTENSION)
						|| fileName
								.endsWith(CrashAnalyserFile.ELF_CORE_DUMP_FILE_EXTENSION))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks if given path contains any Crash files.
	 * 
	 * @param path
	 *            from where Crash files should be found
	 * @return true if given path contains Crash files, false if not
	 */
	protected boolean isCrashPathValid(final String path) {
		try {
			final File file = new File(path);
			if (!file.isDirectory())
				return false;

			// We are looking for files *.bin, or *.txt or *.crashxml
			final FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name
							.endsWith(CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION)
							|| name
									.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION)
							|| name
									.endsWith(CrashAnalyserFile.D_EXC_FILE_EXTENSION)
							|| name
									.endsWith(CrashAnalyserFile.ELF_CORE_DUMP_FILE_EXTENSION));
				}
			};
			final File[] files = file.listFiles(filter);
			if (files != null && files.length > 0)
				return true;

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns an empty working folder under workspace.
	 * 
	 * @param clean
	 *            defines whether the folder should be cleared
	 * @return E.g.
	 *         C:\My_Workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser
	 *         \work\
	 */
	public static String getWorkingFolder(final boolean clean) {
		String workingFolder = FileOperations
				.addSlashToEnd(getPluginWorkingLocation());
		workingFolder += FileOperations.addSlashToEnd(WORK_FOLDER);
		// creates the folder. if the folder already exists, deletes its content
		FileOperations.createFolder(workingFolder, clean);
		return workingFolder;
	}

	/**
	 * Creates a new folder for crash file. E.g.
	 * C:\My_Workspace\.metadata\.plugins
	 * \com.nokia.s60tools.crashanalyser\CrashFiles\5\
	 * 
	 * @return created folder, or null if failed
	 */
	public static String getNewCrashFolder() {
		final String crashFilesFolder = FileOperations
				.addSlashToEnd(getCrashFilesFolder());
		for (int i = 1; i < MAX_FILE_COUNT; i++) {
			final File freeFolder = new File(crashFilesFolder + Integer.toString(i));
			if (!freeFolder.exists()) {
				FileOperations.createFolder(freeFolder.getAbsolutePath());
				return freeFolder.getAbsolutePath();
			}
		}

		return null;
	}

	/**
	 * Creates a new folder for temporary crash files. This folder is used for
	 * crash files which are read from device.
	 * 
	 * @param clean
	 *            defines whether the folder should be cleared
	 * @return E.g.
	 *         C:\My_Workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser
	 *         \temp
	 */
	public static String getTemporaryCrashFileFolder(final boolean clean) {
		String tempFolder = FileOperations
				.addSlashToEnd(getPluginWorkingLocation());
		tempFolder += TEMPORARY_FOLDER;
		// creates the folder. if the folder already exists, deletes its content
		FileOperations.createFolder(tempFolder, clean);
		return tempFolder;
	}

	/**
	 * Checks whether given path is valid (i.e. contains correct files etc)
	 * 
	 * @param path
	 *            path to be checked
	 * @param pathType
	 *            defines "how" the path is validated
	 * @return true if path is valid, false if not
	 */
	public boolean isPathValid(final String path, final PathTypes pathType) {

		boolean retVal = false;

		if (PathTypes.CRASH.equals(pathType))
			retVal = isCrashPathValid(path);

		return retVal;
	}

	/**
	 * Decoder Engine owns crash files which are being handled by Crash Analyser
	 * wizard. This will get all files which are currently being handled.
	 * 
	 * @return list of crash files.
	 */
	public List<CrashFileBundle> getCrashFiles() {
		return crashFiles;
	}

	/**
	 * This method is used to set crash files which are to be decoded. This
	 * method is used when user has selected files to be re-decoded.
	 * 
	 * @param files
	 *            files to be decoded
	 */
	public void setCrashFiles(final List<CrashFileBundle> files) {
		crashFiles = files;
	}

	/**
	 * This method executes CrashAnalyser.exe for summary information for given
	 * file or folder.
	 * 
	 * @param fileOrFolder
	 *            a file or a folder
	 * @param errorLibrary
	 *            Error Library
	 * @param progress
	 *            progress monitor
	 * @return true if any files were found for summary info, false if not
	 */
	public boolean processSummaryInfoForFiles(final String fileOrFolder,
			ErrorLibrary errorLibrary, final IProgressMonitor progress) {
		if (crashFiles != null)
			crashFiles.clear();
		final String workingFolder = DecoderEngine.getWorkingFolder(true);
		if (!"".equals(fileOrFolder)) {

			String originatingDirectory = fileOrFolder;

			final File f = new File(fileOrFolder);
			// if only one .crashxml file is selected, no need to run command
			// line
			if (f.isFile()
					&& fileOrFolder
							.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION)) {
				FileOperations.copyFile(f, workingFolder, true);
				originatingDirectory = FileOperations.getFolder(fileOrFolder);
				// one binary file is selected, run CrashAnalyser.exe for
				// summary data
			} else if (f.isFile()) {
				CommandLineManager.executeSummary(workingFolder, fileOrFolder,
						CrashAnalyserFile.SUMMARY_FILE_EXTENSION, progress);
				originatingDirectory = FileOperations.getFolder(fileOrFolder);
				// directory was given
			} else {
				// copy .crashxml files to working folder.
				// copySummaryFilesToWorkingDirectory will return true if folder
				// contained only .crashxml files -> so if false is returned, it
				// means that we need to run CrashAnalyser.exe for other files
				// in the directory.
				if (!copyCrashFilesToWorkingDirectory(fileOrFolder,
						workingFolder))
					CommandLineManager.executeSummary(workingFolder,
							fileOrFolder,
							CrashAnalyserFile.SUMMARY_FILE_EXTENSION, progress);
			}
			readSummaryFiles(workingFolder, originatingDirectory, errorLibrary);
		}

		if (crashFiles.isEmpty())
			return false;

		return true;
	}

	/**
	 * copies all crashfiles (.crashxml) from given folder to given workfolder.
	 * 
	 * @param fromFolder
	 *            folder where crash files are copied from
	 * @param workingFolder
	 *            folder where crash files are copied to
	 * @return true if fromFolder contained only crash files (.crashxml), false
	 *         if folder contained other files also
	 */
	boolean copyCrashFilesToWorkingDirectory(final String fromFolder,
			final String workingFolder) {
		boolean retval = true;
		final File from = new File(fromFolder);
		// given from folder needs to be an existing directory
		if (from.isDirectory() && from.exists()) {
			final String[] files = from.list(); // get all files
			// if files were found
			if (files != null && files.length > 0) {
				// go through all files in fromFolder
				for (int i = 0; i < files.length; i++) {
					final String file = files[i];
					// files is .crashxml
					if (file.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION)) {
						FileOperations.copyFile(new File(FileOperations
								.addSlashToEnd(fromFolder)
								+ file), workingFolder, true);
						// file was not .crashxml
					} else {
						retval = false;
					}
				}
			}
		}

		return retval;
	}

	/**
	 * Reads all summary and crash files (.xml & .crashxml) from given
	 * directory. Creates a CrashFileBundle for all found files and adds them to
	 * this.crashFiles
	 * 
	 * @param summaryFileDirectory
	 *            directory where files are to be read from
	 * @param originatingDirectory
	 *            directory from where the original files are being imported
	 * @param errorLibrary
	 *            Error Library
	 */
	void readSummaryFiles(final String summaryFileDirectory,
			final String originatingDirectory, final ErrorLibrary errorLibrary) {
		final File file = new File(summaryFileDirectory);
		crashFiles = new ArrayList<CrashFileBundle>();

		// read all files from the directory
		if (file.isDirectory()) {

			// accept summary and output files (.xml & .crashxml)
			final FilenameFilter filter = new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return ((name
							.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION) || name
							.endsWith("."
									+ CrashAnalyserFile.SUMMARY_FILE_EXTENSION)) && 
									! name.equals(CommandLineManager.PARAMETERS_XML));
				}
			};

			final File[] files = file.listFiles(filter);

			// go through all found files
			for (int i = 0; i < files.length; i++) {
				final File crashFile = files[i];

				// file is output.crashxml type
				if (crashFile.getName().endsWith(
						CrashAnalyserFile.OUTPUT_FILE_EXTENSION)) {
					CrashFile crashxml = CrashFile
							.read(crashFile, errorLibrary);
					if (crashxml != null)
						crashFiles.add(new CrashFileBundle(crashxml,
								originatingDirectory));
					// file is summary file
				} else {
					final SummaryFile summaryXml = SummaryFile.read(crashFile,
							errorLibrary);
					if (summaryXml != null)
						crashFiles.add(new CrashFileBundle(summaryXml,
								originatingDirectory));
				}
			}
		}
	}

	/**
	 * Used for passing decoding parameters
	 * 
	 * @param data
	 *            decoding parameters
	 */
	public void setDecodingData(final DecodingData data) {
		decodingData = data;
	}

	/**
	 * Returns a path where Crash Analyser plugin can do various tasks (located
	 * under workspace).
	 * 
	 * @return E.g.
	 *         C:\My_Workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser
	 *         \
	 */
	protected static String getPluginWorkingLocation() {
		final IPath location = Platform.getStateLocation(CrashAnalyserPlugin
				.getDefault().getBundle());
		return location.toOSString();
	}

	/**
	 * Returns the folder under which crash file folders are located
	 * 
	 * @return E.g.
	 *         C:\My_Workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser
	 *         \CrashFiles
	 */
	public static String getCrashFilesFolder() {
		final String crashFilesFolder = FileOperations
				.addSlashToEnd(getPluginWorkingLocation())
				+ CRASH_FILES_FOLDER;
		FileOperations.createFolder(crashFilesFolder);
		return crashFilesFolder;
	}

	/**
	 * Start the decoding process
	 * 
	 * @param observer
	 *            observer for the decoding process
	 */
	public void decode(final IDecodingObserver observer) {
		decodingObserver = observer;
		setPriority(Job.SHORT);
		setUser(true);
		schedule();
	}

	/**
	 * Runs CrashAnalyser.exe for full analysis for (user) selected files. This
	 * also handles already decoded files (.crashxml) so that they are not given
	 * to CrashAnalyser.exe. Outcome of this method is that all decoded files
	 * are under given workingFolder.
	 * 
	 * @param workingFolder
	 *            where files are to be decoded
	 * @param monitor
	 *            for progress bar
	 */
	void importCrashFiles(final String workingFolder, final IProgressMonitor monitor) {
		if (crashFiles != null) {
			final List<String> filesToBeDecoded = new ArrayList<String>();
			// go through all crash files
			for (int i = 0; i < crashFiles.size(); i++) {
				final CrashFileBundle crashFile = crashFiles.get(i);

				// we are importing already decoded file (.crashxml)
				if (crashFile.isFullyDecoded()) {
					final CrashFile f = crashFile.getCrashFile();
					// this index was not chosen by user to be imported, delete
					// this .crash from working folder
					if (f != null && !decodingData.crashFileIndexes.contains(i)) {
						FileOperations.deleteFile(f.getFilePath());
					}
					// we are decoding a crash file
				} else {
					// this index was not chosen by user to be imported, skip
					// this crash file
					if (!decodingData.crashFileIndexes.contains(i))
						continue;

					final SummaryFile f = crashFile.getSummaryFile();
					filesToBeDecoded.add(f.getSourceFilePath());
				}
			}

			if (!filesToBeDecoded.isEmpty()) {
				String mapFilesFolder = decodingData.mapFilesFolder;

				// if MapFiles.zip is provided
				if (mapFilesFolder != null && "".equals(mapFilesFolder)
						&& decodingData.mapFilesZip != null
						&& !"".equals(decodingData.mapFilesZip)) {
					final String zipFolder = FileOperations
							.addSlashToEnd(workingFolder)
							+ FileOperations
									.addSlashToEnd(MAP_FILES_ZIP_FOLDER);
					FileOperations.createFolder(zipFolder, true);
					FileOperations.unZipFiles(
							new File(decodingData.mapFilesZip), zipFolder);
					mapFilesFolder = zipFolder;
				}

				CommandLineManager.executeFullAnalysis(workingFolder,
						filesToBeDecoded.toArray(new String[filesToBeDecoded
								.size()]), decodingData.symbolFiles,
						mapFilesFolder, decodingData.imageFiles,
						decodingData.traceDictionaryFiles,
						CrashAnalyserFile.OUTPUT_FILE_EXTENSION,
						ERROR_FILE_EXTENSION, getSelgeEventIniFile(), monitor);
			}
		}
	}

	/**
	 * Searches the crash file bundle for the given crashFile
	 * 
	 * @param crashFile
	 *            .xml or .crashxml file
	 * @return CrashFileBundle for given file if found, null if not found
	 */
	CrashFileBundle getCrashFileBundle(final File crashFile) {
		if (crashFiles != null) {
			for (int i = 0; i < crashFiles.size(); i++) {
				final CrashFileBundle cfb = crashFiles.get(i);
				String fileName = "";
				final SummaryFile sf = cfb.getSummaryFile();
				if (sf != null) {
					fileName = FileOperations.getFileNameWithoutExtension(sf
							.getFileName());
				}
				final CrashFile cf = cfb.getCrashFile();
				if (cf != null) {
					fileName = FileOperations.getFileNameWithoutExtension(cf
							.getFileName());
				}

				if ("".equals(fileName)) {
					final UndecodedFile uf = cfb.getUndecodedFile();
					if (uf != null
							&& uf
									.getFileName()
									.equals(
											FileOperations
													.getFileNameWithoutExtension(crashFile
															.getName())))
						return cfb;
				}

				if (FileOperations.getFileNameWithoutExtension(
						crashFile.getName()).equalsIgnoreCase(fileName))
					return cfb;
			}
		}
		return null;
	}

	/**
	 * Moves decoded crash files from working folder to their own folders under
	 * CrashFiles folder
	 * 
	 * @param workingFolder
	 *            from where decoded files are moved from
	 */
	CrashAnalyserFile moveDecodedFiles(final String workingFolder) {
		CrashAnalyserFile cafile = null;
		final File folder = new File(workingFolder);

		// accept output files (.crashxml)
		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return (name.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION));
			}
		};

		final File[] files = folder.listFiles(filter);

		// go through all found files
		for (int i = 0; i < files.length; i++) {
			try {
				final File crashFile = files[i];

				final String crashFolder = getNewCrashFolder();

				// copy crash file (.crashxml) to crash folder
				FileOperations.copyFile(crashFile, crashFolder, true);

				// try to copy the original binary file also to crash folder
				// (only mobilecrash, not D_EXC)
				final String binaryFile = SummaryFile.getSourceFilePath(crashFile
						.getAbsolutePath());
				if (!"".equals(binaryFile)
						&& binaryFile
								.toLowerCase()
								.endsWith(
										"."
												+ CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION)) {
					FileOperations.copyFile(new File(binaryFile), crashFolder,
							true);
				}

				// if only one file was decoded, pass this file eventually to
				// MainView so that this file can be opened up after decoding.
				if (files.length == 1) {
					cafile = CrashFile.read(crashFolder,
							decodingData.errorLibrary);
				}

				// html and/or text page needs to be generated
				if ((decodingData.html || decodingData.text)
						&& decodingData.htmlTextOutputFolder != null) {
					final CrashFile crashxml = CrashFile.read(crashFile,
							decodingData.errorLibrary);
					if (crashxml != null) {
						// create html/text file to the original folder where
						// file came from
						if ("".equals(decodingData.htmlTextOutputFolder)) {
							final CrashFileBundle cfb = getCrashFileBundle(crashFile);
							if (cfb != null) {
								if (decodingData.html)
									crashxml.writeTo(cfb
											.getOriginatingDirectory(), true);
								if (decodingData.text)
									crashxml.writeTo(cfb
											.getOriginatingDirectory(), false);
							}

							// create html/text file to user defined location
						} else {
							if (decodingData.html)
								crashxml
										.writeTo(
												decodingData.htmlTextOutputFolder,
												true);
							if (decodingData.text)
								crashxml.writeTo(
										decodingData.htmlTextOutputFolder,
										false);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return cafile;
	}

	/**
	 * Runs CrashAnalyser.exe for full analysis for provided files. This handles
	 * only binary files. Outcome of this method is that all decoded files are
	 * under given workingFolder.
	 * 
	 * @param workingFolder
	 *            where files are to be decoded
	 * @param monitor
	 */
	void reDecodeCrashFiles(final String workingFolder, final IProgressMonitor monitor) {
		if (crashFiles != null) {
			final List<String> filesToBeDecoded = new ArrayList<String>();
			// collect all binary file paths
			for (int i = 0; i < crashFiles.size(); i++) {
				final CrashFileBundle crashFile = crashFiles.get(i);
				final UndecodedFile udf = crashFile.getUndecodedFile();
				filesToBeDecoded.add(udf.getFilePath());
			}

			if (!filesToBeDecoded.isEmpty()) {
				String mapFilesFolder = decodingData.mapFilesFolder;

				// if MapFiles.zip is provided
				if (mapFilesFolder != null && "".equals(mapFilesFolder)
						&& decodingData.mapFilesZip != null
						&& !"".equals(decodingData.mapFilesZip)) {
					final String zipFolder = FileOperations
							.addSlashToEnd(workingFolder)
							+ FileOperations
									.addSlashToEnd(MAP_FILES_ZIP_FOLDER);
					FileOperations.createFolder(zipFolder, true);
					FileOperations.unZipFiles(
							new File(decodingData.mapFilesZip), zipFolder);
					mapFilesFolder = zipFolder;
				}
				CommandLineManager.executeFullAnalysis(workingFolder,
						filesToBeDecoded.toArray(new String[filesToBeDecoded
								.size()]), decodingData.symbolFiles,
						mapFilesFolder, decodingData.imageFiles, 
						decodingData.traceDictionaryFiles,
						CrashAnalyserFile.OUTPUT_FILE_EXTENSION,
						ERROR_FILE_EXTENSION, getSelgeEventIniFile(), monitor);
			}
		}
	}

	/**
	 * Moves re-decoded crash files from working folder to their own folders
	 * under CrashFiles folder
	 * 
	 * @param workingFolder
	 *            from where decoded files are moved from
	 */
	CrashFile moveReDecodedFiles(final String workingFolder) {
		CrashFile cafile = null;
		final File folder = new File(workingFolder);

		// accept output files (.crashxml)
		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return (name.endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION));
			}
		};

		final File[] files = folder.listFiles(filter);

		// go through all found files
		for (int i = 0; i < files.length; i++) {
			try {
				final File crashFile = files[i];

				final CrashFileBundle cfb = getCrashFileBundle(crashFile);
				if (cfb == null)
					continue;

				final String crashFolder = FileOperations.addSlashToEnd(cfb
						.getOriginatingDirectory());
				final File fCrashFolder = new File(crashFolder);

				// folder should exist
				if (!fCrashFolder.exists())
					continue;

				// copy crash file (.crashxml) to crash folder
				if (FileOperations.copyFile(crashFile, crashFolder, true)) {
					final String[] crashFolderFiles = fCrashFolder.list();

					// if only one file was decoded, pass this file eventually
					// to MainView so that this file can be opened up after decoding.
					if (files.length == 1) {
						cafile = CrashFile.read(crashFolder,
								decodingData.errorLibrary);
					}

					// remove .html and .xml files from crash folder if they
					// exist
					if (crashFolderFiles != null && crashFolderFiles.length > 0) {
						for (int j = 0; j < crashFolderFiles.length; j++) {
							final String crashFolderFile = crashFolderFiles[j];
							if (crashFolderFile.toLowerCase().endsWith(
									CrashFileBundle.EXTENSION_HTML)
									|| crashFolderFile
											.toLowerCase()
											.endsWith(
													"."
															+ CrashAnalyserFile.SUMMARY_FILE_EXTENSION))
								FileOperations.deleteFile(crashFolder
										+ crashFolderFile);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return cafile;
	}

	String getToolsPath() {
		String toolsPath = CrashAnalyserPlugin.getPluginInstallPath();
		if (!toolsPath.endsWith(File.separator))
			toolsPath += File.separator;
		toolsPath += TOOLS_FOLDER + File.separator;
		return toolsPath;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		String workingFolder = "";
		CrashAnalyserFile caFile = null;
		try {
			// we are importing files
			if (decodingData.importingFiles) {
				workingFolder = getWorkingFolder(false);
				importCrashFiles(workingFolder, monitor);
				caFile = moveDecodedFiles(workingFolder);
				// we are re-decoding selected crash files
			} else {
				workingFolder = getWorkingFolder(true);
				reDecodeCrashFiles(workingFolder, monitor);
				caFile = moveReDecodedFiles(workingFolder);
			}
		} catch (Exception e) {
			FileOperations.deleteFolder(workingFolder);
			decodingObserver.decodingFinished(e.getMessage(), caFile);
			return Status.OK_STATUS;
		}

		FileOperations.deleteFolder(workingFolder);
		decodingObserver.decodingFinished("", caFile);
		return Status.OK_STATUS;
	}
}
