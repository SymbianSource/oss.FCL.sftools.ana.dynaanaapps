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

package com.nokia.s60tools.crashanalyser.corecomponents.interfaces;

import com.nokia.s60tools.crashanalyser.corecomponents.model.*;
import com.nokia.s60tools.crashanalyser.corecomponents.model.InputXmlGenerator.XMLGeneratorAction;
import com.nokia.s60tools.crashanalyser.corecomponents.plugin.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class is used for running CrashAnalyser.exe
 * 
 */
public final class CommandLineManager {

	private static final String ACTION_START_TAG = "} START - ";

	/**
	 * Verbose output
	 */
	static final boolean verbose = false;

	/**
	 * Progress detailed output
	 */
	static final boolean progress_detailed = false;

	static final String PARAMETERS_XML = "parameters.xml";
	static final String EXE_FOLDER = "Binaries";
	static final String COMMAND_LINE_COMMAND = "CrashAnalyserConsole.exe\" -plugin CRASH_ANALYSIS -input ";
	static final String PROGRESS_PARAMETER = " -PROGRESS";
	static final String PROGRESS_DETAILS_PARAMETER = " -PROGRESS_DETAILS";
	static final String VERBOSE_PARAMETER = " -V";

	private CommandLineManager() {
		// not meant to be implemented
	}

	/**
	 * Executes CrashAnalyser.exe with Full data (i.e. creates an XML file from
	 * e.g. MobileCrash.bin witht symbol etc. information)
	 * 
	 * @param outputFolder
	 *            where output files are created
	 * @param crashFiles
	 *            files to be decoded
	 * @param symbolFiles
	 *            paths to symbol files (or null)
	 * @param mapFilesFolder
	 *            folder which contains map files (or null)
	 * @param imageFiles
	 *            paths to image files (or null)
	 * @param fileExtension
	 *            E.g. crashxml (not .crashxml)
	 * @param failedFileExtension
	 *            extension for failed files
	 * @param selgeEventIniFile
	 *            location of selge_event.ini
	 * @param monitor
	 *            for progress bar
	 * @return true if success, false if not
	 */
	public static boolean executeFullAnalysis(String outputFolder,
			String[] crashFiles, String[] symbolFiles, String mapFilesFolder,
			String[] imageFiles, String fileExtension,
			String failedFileExtension, String selgeEventIniFile,
			IProgressMonitor monitor) {
		List<String> debugMetadataFiles = new ArrayList<String>();

		// collect symbol files if any provided
		if (symbolFiles != null && symbolFiles.length > 0) {
			for (int i = 0; i < symbolFiles.length; i++)
				debugMetadataFiles.add(symbolFiles[i]);
		}

		// collect image files if any provided
		if (imageFiles != null && imageFiles.length > 0) {
			for (int i = 0; i < imageFiles.length; i++)
				debugMetadataFiles.add(imageFiles[i]);
		}

		String[] debugMetadata = null;
		if (!debugMetadataFiles.isEmpty())
			debugMetadata = debugMetadataFiles
					.toArray(new String[debugMetadataFiles.size()]);

		String mapFolder = mapFilesFolder;
		if (mapFilesFolder != null && "".equals(mapFilesFolder))
			mapFolder = null;

		InputXmlGenerator xml = new InputXmlGenerator(
				XMLGeneratorAction.ANALYSE_CRASH_FULL, crashFiles, null,
				debugMetadata, mapFolder, null, null, outputFolder, "."
						+ fileExtension, "." + failedFileExtension,
				selgeEventIniFile);
		return createAndExecute(xml, monitor);
	}

	/**
	 * Executes CrashAnalyser.exe with Summary data (i.e. creates quickly an XML
	 * file from e.g. MobileCrash.bin without symbol etc. information)
	 * 
	 * @param outputFolder
	 *            where output files are created
	 * @param fileOrFolder
	 *            file to be "decoded" or directory from which files are to be
	 *            decoded
	 * @param fileExtension
	 *            E.g. xml (not .xml)
	 * @param monitor
	 *            Progress monitor
	 * @return true if success, false if not
	 */
	public static boolean executeSummary(String outputFolder,
			String fileOrFolder, String fileExtension, IProgressMonitor monitor) {
		File file = new File(fileOrFolder);
		// directory was given
		if (file.isDirectory()) {
			InputXmlGenerator xml = new InputXmlGenerator(
					XMLGeneratorAction.ANALYSE_CRASH_SUMMARY, null,
					fileOrFolder, null, null, null, null, outputFolder, "."
							+ fileExtension, null, "");
			return createAndExecute(xml, monitor);
			// a single file was given
		} else if (file.isFile()) {
			InputXmlGenerator xml = new InputXmlGenerator(
					XMLGeneratorAction.ANALYSE_CRASH_SUMMARY,
					new String[] { fileOrFolder }, null, null, null, null,
					null, outputFolder, "." + fileExtension, null, "");
			return createAndExecute(xml, monitor);
		}
		return false;
	}

	/**
	 * creates an input.xml for CrashAnalyser.exe and executes CrashAnalyser.exe
	 * 
	 * @param xml
	 *            initialized xml generator
	 * @param monitor
	 *            Progress monitor
	 * @return true if success, false if not
	 */
	private static boolean createAndExecute(InputXmlGenerator xml,
			IProgressMonitor monitor) {
		String workingDirectory = getCrashAnalyserPath();
		String fileName = workingDirectory + PARAMETERS_XML;
		File filename = new File(fileName);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			// create input.xml for CrashAnalyser.exe
			xml.GenerateXML(writer);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		try {
			String commandLineCommand = "\"" + workingDirectory
					+ COMMAND_LINE_COMMAND + PARAMETERS_XML;
			if (monitor != null)
				if (progress_detailed) {
					commandLineCommand += PROGRESS_DETAILS_PARAMETER;
				} else {
					commandLineCommand += PROGRESS_PARAMETER;
				}

			if (verbose)
				commandLineCommand += VERBOSE_PARAMETER;

			// execute CrashAnalyser.exe
			Process p = Runtime.getRuntime().exec(commandLineCommand, null,
					new File(workingDirectory));
			// Get the input stream and read from it

			FileWriter fstream = new FileWriter(workingDirectory + "log.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			int c;
			String line = "";
			InputStream in = p.getInputStream();
			boolean monitorStarted = false;
			while ((c = in.read()) != -1) {
				if ((char) c == '\n' || (char) c == '\r') {
					if (line.contains(ACTION_START_TAG)) {
						int beginIndex = line.indexOf(ACTION_START_TAG)
								+ ACTION_START_TAG.length();
						String action = line.substring(beginIndex);
						if (!line.contains("/???") && !monitorStarted) {
							int beginOfIndexOfActionsNbr = line.indexOf("/") + 1;
							int endOfIndexOfActionsNbr = line.indexOf("}",
									beginOfIndexOfActionsNbr);
							Integer nbrOfActions = 0;
							try {
								nbrOfActions = Integer.valueOf((line.substring(
										beginOfIndexOfActionsNbr,
										endOfIndexOfActionsNbr)));
							} catch (NumberFormatException e) {
								nbrOfActions = -1;
							}
							if (nbrOfActions == -1) {
								monitor.beginTask(action,
										IProgressMonitor.UNKNOWN);
							} else {
								monitor.beginTask(action, nbrOfActions
										.intValue());
							}
							monitorStarted = true;
							monitor.worked(1);
						} else {
							if (monitorStarted) {
								monitor.setTaskName(action);
								monitor.worked(1);
							}
						}
					}
					line = "";
				} else {
					line += (char) c;
				}
				out.write((char) c);
			}
			in.close();
			out.close();
			monitor.done();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * Gets the path where CrashAnalyser.exe is located
	 * 
	 * @return the path where CrashAnalyser.exe is located
	 */
	private static String getCrashAnalyserPath() {
		String crashAnalyserExePath = CrashAnalyserCoreComponentsPlugin
				.getPluginInstallPath();
		if (!crashAnalyserExePath.endsWith(File.separator))
			crashAnalyserExePath += File.separator;
		crashAnalyserExePath += EXE_FOLDER + File.separator;
		return crashAnalyserExePath;
	}
}
