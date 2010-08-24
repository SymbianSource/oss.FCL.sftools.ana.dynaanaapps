/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class UseAtool
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
import com.nokia.carbide.cdt.builder.builder.CarbideCommandLauncher;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;

/**
 * Class to use atool.exe. Atool.exe is usually used in command prompt so that's
 * why we executes atool commands with using CarbideCommandLauncher class.
 * 
 * @author kihe
 * 
 */
public class UseAtool {

	/** XML file path */
	private String xmlFilePath = null;

	/** Clean .dat file path */
	private String cleanDatFilePath = null;

	private static String deviceAtoolVersion = "";

	/**
	 * Sets XML file path
	 * 
	 * @param xmlFilePath
	 *            XML file path
	 */
	private void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
	}

	/**
	 * Returns XML file path.
	 * 
	 * @return XML file path
	 */
	public String getXmlFilePath() {
		return xmlFilePath;
	}

	/**
	 * Sets clean .dat file path
	 * 
	 * @param cleanDatFilePath
	 *            clean .dat file path
	 */
	private void setCleanDatFilePath(String cleanDatFilePath) {
		this.cleanDatFilePath = cleanDatFilePath;
	}

	/**
	 * Returns clean .dat file path.
	 * 
	 * @return clean .dat file path
	 */
	public String getCleanDatFilePath() {
		return cleanDatFilePath;
	}

	/**
	 * Check data file type. Gets first line of file and compares that to
	 * predefined constants.
	 * 
	 * @param path
	 *            data file path
	 * @return type of data file
	 */
	public static int checkFileType(final String path) {
		// return value
		int retValue = Constants.DATAFILE_INVALID;

		// check that file exists
		if (path == null || ("").equals(path)) {
			return retValue;
		}
		File file = new File(path);
		if (!file.exists()) {
			return retValue;
		}

		if (Util.isFileXML(path)) {
			return Constants.DATAFILE_XML;
		}

		// input
		BufferedReader input = null;
		FileInputStream fileInputStream = null;
		InputStreamReader inputReader = null;

		try {
			// get input
			fileInputStream = new FileInputStream(path);
			if (fileInputStream.available() == 0) {
				fileInputStream.close();
				return Constants.DATAFILE_EMPTY;
			}

			// file not empty read file contents
			inputReader = new InputStreamReader(fileInputStream, "UTF-8");
			input = new BufferedReader(inputReader);

			boolean firstLineProcessed = false;
			String line;

			while ((line = input.readLine()) != null) {

				if (!firstLineProcessed) {
					firstLineProcessed = true;

					if (line.contains(Constants.DATAFILE_VERSION)) {
						return Constants.DATAFILE_LOG;
					}
					if (line.contains(Constants.BINARY_FILE_VERSION)) {
						return Constants.DATAFILE_BINARY;
					}
				}

				if (line.contains(Constants.PREFIX_OLD)) {
					return Constants.DATAFILE_OLD_FORMAT;
				}

				if (line.contains(Constants.PREFIX)) {
					int index = line.indexOf(Constants.PREFIX);
					String usedString = line.substring(index, line.length());
					String[] lineFragments = usedString.split(" ");

					if (lineFragments.length > 8) {
						if (lineFragments[2].equals(Constants.PCS)) {

							deviceAtoolVersion = lineFragments[8];

							int traceFormatVersion = 0;
							try {
								traceFormatVersion = Integer.parseInt(
										lineFragments[7], 16);
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
								return Constants.DATAFILE_INVALID;
							}

							if (traceFormatVersion == 3) {
								return Constants.DATAFILE_TRACE;
							} else {
								return Constants.DATAFILE_UNSUPPORTED_TRACE_FORMAT;
							}
						}
					}
				}
			}
			input.close();
			inputReader.close();
			fileInputStream.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			try {
				if (inputReader != null) {
					inputReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return retValue;
	}

	/**
	 * Creates atool_temp folder to current mmp folder location.
	 * 
	 * @param folderLocation
	 *            Current mmp folder location
	 * @return True if given location contains atool_temp folder otherwise false
	 * 
	 */
	public static boolean createAToolFolderIfNeeded(final String folderLocation) {

		boolean returnValue = false;

		// check if folder location contains atool_temp folder
		if (folderLocation.indexOf(Constants.ATOOL_TEMP) != -1) {
			returnValue = true;
		}

		// create atool_temp folder
		if (!returnValue) {
			File file = new File(folderLocation + "\\" + Constants.ATOOL_TEMP);
			if (!file.exists()) {
				returnValue = file.mkdir();
			}
		}
		return returnValue;
	}

	/**
	 * Constructor.
	 */
	public UseAtool() {
		// ConstructorDeclaration[@Private='false'][count(BlockStatement) = 0
		// and ($ignoreExplicitConstructorInvocation = 'true' or
		// not(ExplicitConstructorInvocation)) and @containsComment = 'false']
	}

	/**
	 * Creates XML file to project bld.inf folder.
	 * 
	 * @param monitor
	 *            IProgressMonitor reference
	 * @param project
	 *            Project reference
	 * @param dataFilePath
	 *            Trace file location
	 * @param command
	 *            Command which is used to execute atool.exe
	 * @return XML file name and path if file was successfully created otherwise
	 *         null
	 */
	public final Constants.COMMAND_LINE_ERROR_CODE createXmlAndCleanDatFilesToCarbide(
			final IProgressMonitor monitor, final IProject project,
			final String dataFilePath, final String command) {

		int fileType = 0;

		// get file type
		fileType = checkFileType(dataFilePath);

		if (fileType == Constants.DATAFILE_INVALID) {
			return Constants.COMMAND_LINE_ERROR_CODE.DATA_FILE_INVALID;
		} else if (fileType == Constants.DATAFILE_EMPTY) {
			return Constants.COMMAND_LINE_ERROR_CODE.DATA_FILE_EMPTY;
		} else if (fileType == Constants.DATAFILE_OLD_FORMAT) {
			return Constants.COMMAND_LINE_ERROR_CODE.DATA_FILE_OLD_FORMAT;
		} else if (fileType == Constants.DATAFILE_UNSUPPORTED_TRACE_FORMAT) {
			return Constants.COMMAND_LINE_ERROR_CODE.DATA_FILE_UNSUPPORTED_TRACE_FORMAT;
		}

		// check that command line engine can be executed
		if (!Util.isAtoolAvailable()) {
			return Constants.COMMAND_LINE_ERROR_CODE.EXECUTE_ERROR;
		}
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);

		// if ICarbideProjectInfo not found return null
		if (cpi == null) {
			return Constants.COMMAND_LINE_ERROR_CODE.UNKNOWN_ERROR;
		}
		String absolutePath = cpi.getAbsoluteBldInfPath().toOSString();
		String bldInfFolder = null;
		int index = Util.getLastSlashIndex(absolutePath);
		if (index != -1) {
			bldInfFolder = absolutePath.substring(0, index);
		}
		if (bldInfFolder != null) {
			createAToolFolderIfNeeded(bldInfFolder);
		}

		AbstractList<String> usedArguments = new ArrayList<String>();
		usedArguments.add(command);
		usedArguments.add(dataFilePath);

		// try to load user defined symbol files
		try {
			// property store names
			QualifiedName useRom = new QualifiedName(Constants.USE_ROM_SYMBOL,
					Constants.USE_ROM);
			QualifiedName name = new QualifiedName(
					Constants.USE_ROM_SYMBOL_LOCATION, Constants.ROM_LOC);

			// get value
			String useRomText = project.getPersistentProperty(useRom);
			if (useRomText != null) {
				boolean useRomSymbol = useRomText.equalsIgnoreCase("true") ? true
						: false;
				// is symbol files activated for project
				if (useRomSymbol) {
					String symbolFileLocation = project
							.getPersistentProperty(name);
					if (symbolFileLocation != null
							&& !("").equals(symbolFileLocation)) {
						String[] split = symbolFileLocation.split(";");
						if (split != null) {
							for (int i = 0; i < split.length; i++) {
								// check that file exists
								java.io.File symFile = new java.io.File(
										split[i]);
								if (symFile.exists()) {
									usedArguments.add("-s");
									usedArguments.add(split[i]);
								}
							}
						}
					}
				}
			}
		} catch (CoreException ce) {
			ce.printStackTrace();
		}

		usedArguments.add(bldInfFolder + "\\" + Constants.ATOOL_TEMP + "\\"
				+ Constants.FILENAME_CARBIDE);
		if (Util.verboseAtoolOutput()) {
			usedArguments.add(Constants.ATOOL_SHOW_DEBUG);
		}

		String[] arguments = new String[usedArguments.size()];
		usedArguments.toArray(arguments);

		if (fileType != Constants.DATAFILE_BINARY) {
			String carbideAtoolVersion = Util.getAtoolVersionNumber(Util
					.getAtoolInstallFolder());
			if (!carbideAtoolVersion.equals(deviceAtoolVersion)) {
				Util.showMessageDialog(Constants.CLE_VERSION_MISMATCH,
						MessageFormat.format(
								Constants.AT_BINARIES_VERSION_MISMATCH,
								deviceAtoolVersion, carbideAtoolVersion),
						SWT.ICON_WARNING);
			}
		}

		int returnValue = executeCommand(arguments, bldInfFolder, monitor,
				project);

		// if some error happens
		if (returnValue == Constants.DATAFILE_INVALID
				|| returnValue != Constants.COMMAND_LINE_ERROR_CODE.OK
						.getCode()) {
			Constants.COMMAND_LINE_ERROR_CODE error = Util
					.getErrorCode(returnValue);
			setXmlFilePath(null);
			setCleanDatFilePath(null);
			return error;
		}

		setXmlFilePath(bldInfFolder + "\\" + Constants.ATOOL_TEMP + "\\"
				+ Constants.FILENAME_CARBIDE);

		String cleanDatFileName = null;
		int lastSlashIndex = Util.getLastSlashIndex(dataFilePath);
		if (lastSlashIndex != -1) {
			cleanDatFileName = dataFilePath.substring(lastSlashIndex + 1,
					dataFilePath.length());
		}
		setCleanDatFilePath(bldInfFolder + "\\" + Constants.ATOOL_TEMP + "\\"
				+ cleanDatFileName + ".cleaned");

		return Constants.COMMAND_LINE_ERROR_CODE.OK;
	}

	/**
	 * Executes given command, uses CarbideCommandLauncher class for execution.
	 * 
	 * @param args
	 *            Command argument list
	 * @param path
	 *            Where to run command
	 * @param monitor
	 *            Monitor
	 * @param project
	 *            Project
	 * @return Return value of executeCommand method
	 */
	public final int executeCommand(final String[] args, final String path,
			final IProgressMonitor monitor, final IProject project) {

		int error = Constants.DATAFILE_INVALID;
		try {

			// get project info
			ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
					.getProjectInfo(project);
			if (cpi == null) {
				return Constants.DATAFILE_INVALID;
			}

			// get command launcher
			CarbideCommandLauncher cmdLauncher = new CarbideCommandLauncher(
					project, monitor, Constants.atoolParserIds, cpi
							.getINFWorkingDirectory());

			// start timing stats
			cmdLauncher.startTimingStats();

			// get default config
			ICarbideBuildConfiguration defaultConfig = cpi
					.getDefaultConfiguration();

			// show command on console view
			cmdLauncher.showCommand(true);

			// execute command
			error = cmdLauncher.executeCommand(new Path(Util
					.getAtoolInstallFolder()), args, CarbideCPPBuilder
					.getResolvedEnvVars(defaultConfig), new Path(path));
			cmdLauncher.writeToConsole(cmdLauncher.getTimingStats());

		} catch (OutOfMemoryError oome) {
			oome.printStackTrace();
		}
		return error;
	}
}
