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
 * Description:  Definitions for the class Util
 *
 */

package com.nokia.s60tools.analyzetool.global;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.osgi.framework.Bundle;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.DefaultMMPViewConfiguration;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
import com.nokia.carbide.cdt.builder.builder.CarbideCommandLauncher;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.epoc.engine.EpocEnginePlugin;
import com.nokia.carbide.cpp.epoc.engine.MMPDataRunnableAdapter;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.EMMPStatement;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.IMMPData;
import com.nokia.carbide.cpp.epoc.engine.preprocessor.AcceptedNodesViewFilter;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.builder.AnalyzeToolBuilder;
import com.nokia.s60tools.analyzetool.engine.MMPInfo;
import com.nokia.s60tools.analyzetool.engine.UseAtool;
import com.nokia.s60tools.analyzetool.global.Constants.COMMAND_LINE_ERROR_CODE;

/**
 * Provides commonly used functions.
 * 
 * @author kihe
 * 
 */
public final class Util {

	/** Contains user selection in the confirmation dialog. */
	private static boolean ret;

	/** Contains user selection (integer) in the selection dialog. */
	private static int retValue;

	/** Contains user selection (String) in the selection dialog. */
	private static String userSelection = "";

	/** Contains StringBuffer size. */
	private static int bufferSize = 32;

	// make constructor private so class doesn't get instantiated
	private Util() {
		// do nothing by design
	}

	/**
	 * Checks is module built.
	 * 
	 * @param modules
	 *            Project modules
	 * @param oneModuleName
	 *            Current module
	 * @return True if module found and it is build
	 */
	public static boolean checkModuleBuildState(
			final AbstractList<MMPInfo> modules, final String oneModuleName) {
		Iterator<MMPInfo> iterModules = modules.iterator();
		while (iterModules.hasNext()) {
			MMPInfo oneMMPInfo = iterModules.next();
			String target = oneMMPInfo.getTarget();
			if (target == null) {
				continue;
			} else if (oneMMPInfo.getTarget().equalsIgnoreCase(oneModuleName)) {
				return oneMMPInfo.isBuildSuccesfully();
			}

			// target not found try remove {}
			// for example process name is
			// "AToolMemoryLeakerDll2.dll{000a0000}[04463b81]"
			// but target name is AToolMemoryLeakerDll2.dll
			CharSequence brace = "{";
			if (oneModuleName.contains(brace)) {
				// parse braces
				String tmpTargetName = oneModuleName.substring(0, oneModuleName
						.indexOf('{'));
				if (tmpTargetName != null
						&& oneMMPInfo.getTarget().equalsIgnoreCase(
								tmpTargetName)) {
					return oneMMPInfo.isBuildSuccesfully();
				}
			}
		}
		return false;
	}

	/**
	 * Checks is AnalyzeTool libraries installed from the current SDK.
	 * 
	 * @param cpi
	 *            {@link ICarbideProjectInfo} reference
	 * @return True if libraries are installed otherwise false.
	 */
	public static boolean checkAtoolLibs(final ICarbideProjectInfo cpi) {
		// get active platform
		String platform = cpi.getDefaultConfiguration().getPlatformString();

		// get epoc root
		IPath epocRootPath = EpocEngineHelper.getEpocRootForProject(cpi
				.getProject());

		// check that epocroot path found
		if (epocRootPath == null) {
			return false;
		}

		String epocroot = epocRootPath.toOSString();
		boolean found = true;
		StringBuffer fileBuffer = new StringBuffer(bufferSize);
		fileBuffer.append(Constants.CAN_NOT_FIND_LIBRARIES_MARKER);
		fileBuffer.append(": ");
		if ((Constants.BUILD_TARGET_WINSCW).equalsIgnoreCase(platform)) {
			for (int i = 0; i < Constants.atoolLibsSbs2.length; i++) {
				java.io.File file = new java.io.File(epocroot
						+ Constants.atoolLibsWinscw[i]);
				if (!file.exists()) {
					found = false;
					fileBuffer.append(epocroot);
					fileBuffer.append(Constants.atoolLibsWinscw[i]);
					fileBuffer.append(' ');
				}
			}
		} else if ((Constants.BUILD_TARGET_ARMV5).equalsIgnoreCase(platform)) {
			if (AnalyzeToolBuilder.isSBSBuildActivated(cpi)) {
				for (int i = 0; i < Constants.atoolLibsSbs2.length; i++) {
					java.io.File file = new java.io.File(epocroot
							+ Constants.atoolLibsSbs2[i]);
					if (!file.exists()) {
						found = false;
						fileBuffer.append(epocroot);
						fileBuffer.append(Constants.atoolLibsSbs2[i]);
						fileBuffer.append(' ');
					}
				}
			} else {
				for (int i = 0; i < Constants.atoolLibs.length; i++) {
					java.io.File file = new java.io.File(epocroot
							+ Constants.atoolLibs[i]);
					if (!file.exists()) {
						found = false;
						fileBuffer.append(epocroot);
						fileBuffer.append(Constants.atoolLibs[i]);
						fileBuffer.append(' ');
					}
				}
			}
		}
		if (!found) {
			createErrorMarker(cpi.getProject(), fileBuffer.toString());
		}

		return found;
	}

	/**
	 * Copies stored memory leak analysis file to given folder.
	 * 
	 * @param resultFile
	 *            Existing results file
	 * @param targetPath
	 *            Where to save xml file
	 * @return True if copy success otherwise False
	 */
	public static boolean copyFileToFolder(final String resultFile,
			final String targetPath) {
		FileChannel inputChannel = null;
		FileChannel ouputChannel = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		boolean returnValue = true;
		try {

			// get channel to existing file
			inStream = new FileInputStream(resultFile);
			inputChannel = inStream.getChannel();

			// get channel to new file
			outStream = new FileOutputStream(targetPath, false);
			ouputChannel = outStream.getChannel();

			// get existing file size
			final long size = inputChannel.size();

			// position inside the file
			long position = 0;

			// copy file contents if there are data to copy
			while (position < size) {
				position += ouputChannel.transferFrom(inputChannel, position,
						size - position);
			}

			// close opened channels
			inputChannel.close();
			inStream.close();
			ouputChannel.close();
			outStream.close();
		} catch (FileNotFoundException fnfe) {
			returnValue = false;
		} catch (IOException ioe) {
			returnValue = false;
		} finally {
			try {
				if (inputChannel != null) {
					inputChannel.close();
					inputChannel = null;
				}
			} catch (IOException ioe) {
				returnValue = false;
			}

			try {
				if (inStream != null) {
					inStream.close();
					inStream = null;
				}
			} catch (IOException ioe) {
				returnValue = false;
			}

			try {
				if (ouputChannel != null) {
					ouputChannel.close();
					ouputChannel = null;
				}
			} catch (IOException ioe) {
				returnValue = false;
			}

			try {
				if (outStream != null) {
					outStream.close();
					outStream = null;
				}
			} catch (IOException ioe) {
				returnValue = false;
			}

		}

		return returnValue;
	}

	/**
	 * Creates AnalyzeTool related error marker for the selected project.
	 * 
	 * @param project
	 *            Project reference
	 * @param errorText
	 *            Error information
	 */
	public static void createErrorMarker(final IProject project,
			final String errorText) {
		try {
			// check project validity
			if (project == null || !project.isOpen()) {
				return;
			}

			// create marker for the project
			IMarker marker = project
					.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			if (marker.exists()) {
				// set marker attributes
				marker.setAttribute(IMarker.SEVERITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.TEXT, errorText);
				marker.setAttribute(IMarker.MESSAGE, errorText);
			}
		} catch (CoreException ce) {
			// #ifdef DEBUG
			ce.printStackTrace();
			// #endif
		}
	}

	/**
	 * Deletes existing data file.
	 * 
	 * @param project
	 *            Project reference
	 */
	public static void deleteDataFile(final IProject project) {
		String bldInfFolder = getBldInfFolder(project, false);
		String dataFileLocation = bldInfFolder + Constants.FILENAME;
		java.io.File tmpFile = new java.io.File(dataFileLocation);
		if (tmpFile.exists()) {
			tmpFile.delete();
		}

		String xmlFileLocation = bldInfFolder + Constants.FILENAME_CARBIDE;
		java.io.File tmpXMLFile = new java.io.File(xmlFileLocation);
		if (tmpXMLFile.exists()) {
			tmpXMLFile.delete();
		}

		tmpFile = null;
		tmpXMLFile = null;
	}

	/**
	 * Open file save dialog.
	 * 
	 * @param title
	 *            Save dialog title
	 * @param ext
	 *            Used extension filters
	 * 
	 * @param shell
	 *            Used Shell reference
	 * 
	 * @return User selected path
	 */
	public static String fileSaveDialog(final String title, final String[] ext,
			final Shell shell) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setText(title);
		fileDialog.setFilterExtensions(ext);
		return fileDialog.open();

	}

	/**
	 * Gets atool.exe installation folder which is specified on preferences
	 * page.
	 * 
	 * @return Atool.exe installation folder
	 */
	public static String getAtoolInstallFolder() {
		// get preference store
		IPreferenceStore store = Activator.getPreferences();

		// create new string buffer
		StringBuffer atoolInstallFolder = new StringBuffer();

		boolean useInternal = store.getBoolean(Constants.USE_INTERNAL);

		// append atool.exe installation folder
		String folder = store.getString(Constants.ATOOL_FOLDER);
		if (useInternal) {
			atoolInstallFolder.append(getDefaultAtoolLocation());
		} else if (("").equals(folder)) {
			store.setValue(Constants.USE_INTERNAL, true);
			atoolInstallFolder.append(getDefaultAtoolLocation());
		} else if (folder.equals(Constants.DEFAULT_ATOOL_FOLDER)) {
			java.io.File file = new java.io.File(Constants.DEFAULT_ATOOL_FOLDER);
			if (file.exists()) {
				atoolInstallFolder.append(Constants.DEFAULT_ATOOL_FOLDER);
			} else {
				store.setValue(Constants.USE_INTERNAL, true);
				atoolInstallFolder.append(getDefaultAtoolLocation());
			}
		} else {
			atoolInstallFolder.append(folder);
		}
		// append atool.exe to installation path
		atoolInstallFolder.append("\\atool.exe");
		return atoolInstallFolder.toString();
	}

	/**
	 * Returns atool.exe version number Executes atool.exe via Process class and
	 * parses atool.exe output to get version number.
	 * 
	 * @param path
	 *            Atool.exe path
	 * @return Atool.exe version number if found otherwise "Not available"
	 */
	public static String getAtoolVersionNumber(final String path) {

		BufferedInputStream br = null;

		try {
			// used atool.exe location
			String usedPath = null;

			// if path not given => use default location
			if (path == null) {
				usedPath = getDefaultAtoolLocation() + "\\atool.exe";
			} else if (path.contains("atool.exe")) {
				usedPath = path;
			}
			// else use given location
			else {
				usedPath = path + "\\atool.exe";
			}

			// command
			String[] args = { "cmd", "/c", usedPath, "-v" };

			// execute command
			Process pr = Runtime.getRuntime().exec(args);

			// get atool.exe response to buffer
			br = new BufferedInputStream(pr.getInputStream());

			// wait that all the input is captured
			pr.waitFor();

			// create new stringbuffer for the input
			StringBuffer bf = new StringBuffer("");
			int r = -1;

			// read atool.exe response to stringbufffer
			while ((r = br.read()) != -1) {
				bf.append((char) r);
			}

			// because atool.exe response is following format
			// Version: [version number]
			// Path: [location]
			// Modified: [date and time]
			// we only need to get first row of response
			String[] array = bf.toString().split("\r\n");

			// check array
			if (array != null && array.length > 0) {

				String version = "Version:";

				// get first array
				String versionStr = array[0];

				// if first row contains "version:"
				if (versionStr.contains(version)) {

					// get atool.exe version number
					String atoolVersionNbr = versionStr.substring(version
							.length() + 1, versionStr.length());

					if (atoolVersionNbr == null || ("").equals(atoolVersionNbr)) {
						return Constants.PREFS_ATOOL_VER_NOT_FOUND;
					}

					return atoolVersionNbr;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		}
		return Constants.PREFS_ATOOL_VER_NOT_FOUND;
	}

	/**
	 * Returns dbghelp.dll version information
	 * 
	 * @param path
	 *            atool.exe path
	 * @return dbghelp.dll version information
	 */
	public static String getDbghelpDllVersionInfo(final String path) {

		String dbghelpDllVersionInfo = Constants.DBGHELPDLL_IS_UP_TO_DATE;
		BufferedInputStream bis = null;

		try {
			String usedPath = null;

			if (path == null) {
				usedPath = getDefaultAtoolLocation() + "\\atool.exe";
			} else if (path.contains("atool.exe")) {
				usedPath = path;
			} else {
				usedPath = path + "\\atool.exe";
			}

			String[] args = { "cmd", "/c", usedPath, "-vdbghelp" };

			Process pr = Runtime.getRuntime().exec(args);

			bis = new BufferedInputStream(pr.getInputStream());

			pr.waitFor();

			StringBuffer sb = new StringBuffer("");
			int r = -1;

			while ((r = bis.read()) != -1) {
				sb.append((char) r);
			}

			if (pr.exitValue() == 1)
				dbghelpDllVersionInfo = sb.toString();

			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
					bis = null;
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return dbghelpDllVersionInfo;
	}

	/**
	 * Returns the version number of the AnalyzeTool host-side feature, or
	 * "Unknown" if it cannot be determined
	 * 
	 * @return feature version string
	 */
	public static String getAToolFeatureVersionNumber() {
		String version = Constants.UNKNOWN;
		IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		if (providers != null) {
			for (IBundleGroupProvider bundleGroupProvider : providers) {
				for (IBundleGroup feature : bundleGroupProvider
						.getBundleGroups()) {
					if (feature.getIdentifier().equals(
							"com.nokia.carbide.extensions.analyzetool")) {
						version = feature.getVersion();
						break;
					}
				}
			}
		}
		return version;
	}

	/**
	 * 
	 * Below is code a sample which are related to the AT-682, but it is decided
	 * to let out from current release.
	 * 
	 * /** Finds and returns AnalyzeTool core version number. Version number is
	 * read from the analyzetool header file and it can be located under the
	 * epoc32 folder.
	 * 
	 * @param project
	 *            Project reference
	 * @return Found version number, otherwise null.
	 * 
	 *         public static String getAtoolCoreVersion(IProject project) { if(
	 *         project != null && project.isOpen() ) {
	 * 
	 *         //get Carbide project info IPath epocRootPath =
	 *         EpocEngineHelper.getEpocRootForProject(project); if( epocRootPath
	 *         == null ) { return null; }
	 * 
	 *         File file = null; //thru all the possible locations for( int i=0;
	 *         i< Constants.AT_CORE_INCLUDE_FILE_WITH_VERSION_NUMBER.length;
	 *         i++) { //get file path StringBuffer location = new
	 *         StringBuffer(); location.append(epocRootPath.toOSString());
	 *         location
	 *         .append(Constants.AT_CORE_INCLUDE_FILE_WITH_VERSION_NUMBER[i]);
	 * 
	 *         //check that file exists file = new File(location.toString());
	 *         if( file.exists() ) { break; } }
	 * 
	 *         //read file content FileInputStream fis = null; BufferedReader
	 *         input = null; try{ // get input fis = new FileInputStream(file);
	 *         input = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
	 * 
	 *         // get first line of data file String line = null; String version
	 *         = null; // go thru file while ((line = input.readLine()) != null)
	 *         { if( line.contains(Constants.AT_CORE_VERSION_NUMBER_TAG) ) {
	 *         String[] text = line.split(" "); version = text[1]; } }
	 *         fis.close(); input.close(); return version; } catch(Exception e)
	 *         { e.printStackTrace(); } } return null; }
	 */

	/**
	 * Gets project bld.inf folder path.
	 * 
	 * @param project
	 *            Project reference
	 * @param createTempFolder
	 *            Flag to create atool_temp folder
	 * @return Project bld.inf folder path
	 */
	public static String getBldInfFolder(final IProject project,
			final boolean createTempFolder) {
		// get bld.inf directory
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);
		if (cpi == null) {
			return "";
		}

		String infDir = cpi.getINFWorkingDirectory().toOSString();

		// create atool_temp folder if it does not exists
		if (createTempFolder) {
			UseAtool.createAToolFolderIfNeeded(infDir);
		}

		return infDir + "\\atool_temp\\";
	}

	/**
	 * Gets cpp file name and path.
	 * 
	 * @param resource
	 *            One resource file
	 * @return MMP file name and path if resource is mmp file, otherwise null
	 */
	public static String getCPPFileNameAndPath(final IResource resource) {
		// get file extension
		String fileExt = resource.getFileExtension();
		String cppFileAndPath = null;

		// if file contains extension and it equals to mmp
		if (fileExt != null && fileExt.compareTo("cpp") == 0) {
			// get resource location/path
			String resourcePath = resource.getLocation().toString();

			// if resource path does not contain atool_temp folder
			// save resource location
			if (resourcePath.indexOf(Constants.ATOOL_TEMP) == -1) {
				cppFileAndPath = resource.getProjectRelativePath().toString();
			}

		}

		// return cpp file name and path
		return cppFileAndPath;
	}

	/**
	 * Returns atool.exe location inside the plugin.
	 * 
	 * @return Atool.exe location folder could be found otherwise ""
	 */
	public static String getDefaultAtoolLocation() {
		try {
			// AnalyzeTool bundle
			Bundle bunble = Platform
					.getBundle("com.nokia.s60tools.analyzetool.corecomponents"); //$NON-NLS-1$

			// if bundle not found return empty path
			if (bunble == null) {
				return "";
			}
			// get bundle URL
			URL bundleURL = bunble.getEntry("/");

			// get file URL
			URL fileURL = FileLocator.toFileURL(bundleURL);

			// create new file
			File file = new File(fileURL.getPath());

			// if file exists return file path + atool.exe folder name
			if (file.exists()) {
				return file.getAbsolutePath();
			}
			return "";

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Gets last slash index for current string.
	 * 
	 * @param line
	 *            Line where to find slash index
	 * @return Integer value of slash index
	 */
	public static int getLastSlashIndex(final String line) {
		// get last slash index
		char slash = '/';
		int slashIndex = line.lastIndexOf(slash);
		if (slashIndex == -1) {
			char backSlash = '\\';
			slashIndex = line.lastIndexOf(backSlash);
		}

		return slashIndex;
	}

	/**
	 * Check is atool.exe available.
	 * 
	 * @return True if atool.exe found otherwise False
	 */
	public static boolean isAtoolAvailable() {
		IPreferenceStore store = Activator.getPreferences();
		String folderLocation = store.getString(Constants.ATOOL_FOLDER);

		if (folderLocation.length() > 0) {
			if (!folderLocation.endsWith("\\")) {
				folderLocation += "\\";
			}

			IPath atoolPath = new Path(folderLocation + "atool.exe");

			// if folder does not exists
			if (atoolPath.toFile().exists()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks is data file available.
	 * 
	 * @param project
	 *            Project reference
	 * @return Data file path if it's available otherwise null
	 */
	public static String isDataFileAvailable(final IProject project) {
		String bldInfFolder = getBldInfFolder(project, false);
		String dataFileLocation = bldInfFolder + Constants.FILENAME;
		java.io.File dataFile = new java.io.File(dataFileLocation);
		if (dataFile.exists() && dataFile.length() > 0) {
			return dataFileLocation;
		}
		return null;
	}

	/**
	 * Checks is file extension .xml.
	 * 
	 * @param filePath
	 *            File location
	 * @return True if file contains xml extension otherwise false
	 */
	public static boolean isFileXML(final String filePath) {
		String fileNameAndExt = null;
		// get index of last backslash
		int index = Util.getLastSlashIndex(filePath);

		// if backslash found
		if (index != -1) {

			// get file name
			fileNameAndExt = filePath.substring(index + 1, filePath.length());

			// if file name contains xml extension
			if (fileNameAndExt.contains(".xml")) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Check if current module is built.
	 * 
	 * @param mmpFile
	 *            MMP file name and path
	 * @return True if module is build otherwise False
	 */
	public static boolean isModuleBuilt(final String mmpFile) {
		String path = null;
		// remove mmp file name for path
		int slash = Util.getLastSlashIndex(mmpFile);
		if (slash == -1) {
			return false;
		}
		path = mmpFile.substring(0, slash);
		String buildFile = path + "\\atool_temp\\BuildComplete";
		java.io.File file = new java.io.File(buildFile);
		return file.exists();
	}

	/**
	 * Checks that does given module belong to selected project.
	 * 
	 * @param modules
	 *            Project modules
	 * @param oneModuleName
	 *            One module name
	 * @return True if module belongs to selected project otherwise false
	 */
	public static boolean isModulePartOfProject(
			final AbstractList<MMPInfo> modules, final String oneModuleName) {
		Iterator<MMPInfo> iterModules = modules.iterator();
		while (iterModules.hasNext()) {
			MMPInfo oneMMPInfo = iterModules.next();
			String target = oneMMPInfo.getTarget();
			if (target == null) {
				continue;
			} else if (oneMMPInfo.getTarget().equalsIgnoreCase(oneModuleName)) {
				return true;
			}

			// target not found try remove {}
			// for example process name is
			// "AToolMemoryLeakerDll2.dll{000a0000}[04463b81]"
			// but target name is AToolMemoryLeakerDll2.dll
			CharSequence brace = "{";
			if (oneModuleName.contains(brace)) {
				// parse braces
				String tmpTargetName = oneModuleName.substring(0, oneModuleName
						.indexOf('{'));
				if (tmpTargetName != null
						&& oneMMPInfo.getTarget().equalsIgnoreCase(
								tmpTargetName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets current project targets info.
	 * 
	 * @param project
	 *            Project reference
	 * 
	 * @return AbstractList<MMPInfo> List of project targets
	 */
	public static AbstractList<MMPInfo> loadProjectTargetsInfo(
			final IProject project) {
		AbstractList<MMPInfo> targets = new ArrayList<MMPInfo>();
		if (project == null || !project.isOpen()) {
			return targets;
		}

		// Assumes buildConfig (ICarbideBuildConfiguration) is known
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);
		if (cpi == null) {
			return new ArrayList<MMPInfo>();
		}

		ICarbideBuildConfiguration buildConfig = cpi.getDefaultConfiguration();

		for (IPath mmpPath : EpocEngineHelper
				.getMMPFilesForBuildConfiguration(buildConfig)) {
			Object data = EpocEnginePlugin.runWithMMPData(mmpPath,
					new DefaultMMPViewConfiguration(buildConfig
							.getCarbideProject().getProject(), buildConfig,
							new AcceptedNodesViewFilter()),
					new MMPDataRunnableAdapter() {
						public Object run(IMMPData mmpData) {
							// The real return value, getting a single argument
							// setting
							return mmpData.getSingleArgumentSettings().get(
									EMMPStatement.TARGET);

						}
					});

			// Make sure to test for and cast to proper Object type!
			int index = Util.getLastSlashIndex(mmpPath.toOSString());
			String mmpFileName = "";
			if (index != -1) {
				mmpFileName = mmpPath.toOSString().substring(index + 1,
						mmpPath.toOSString().length());
			}
			MMPInfo oneMMPInfo = new MMPInfo(mmpFileName);
			oneMMPInfo.setLocation(mmpPath.toOSString());

			String mmpStatement = (String) data; // Now we should have the
			// TARGETTYPE
			oneMMPInfo.setTarget(mmpStatement);
			targets.add(oneMMPInfo);
		}
		return targets;
	}

	/**
	 * Opens confirmation Dialog.
	 * 
	 * @param text
	 *            Dialog info text
	 * @return boolean True if user selects "Yes" False if user selects "No"
	 */
	public static boolean openConfirmationDialog(final String text) {

		Activator.getDefault().getWorkbench().getDisplay().syncExec(
				new Runnable() {
					public void run() {
						ret = MessageDialog.openQuestion(new Shell(),
								Constants.ANALYZE_TOOL_TITLE, text);
					}
				});
		return ret;
	}

	/**
	 * Opens confirmation Dialog.
	 * 
	 * @param text
	 *            Dialog info text
	 * @return int User selected index
	 */
	public static int openConfirmationDialogWithCancel(final String text) {

		Activator.getDefault().getWorkbench().getDisplay().syncExec(
				new Runnable() {
					public void run() {

						String[] labels = new String[3];
						labels[0] = "Yes";
						labels[1] = "No";
						labels[2] = "Cancel";
						MessageDialog mDialog = new MessageDialog(new Shell(),
								Constants.ANALYZE_TOOL_TITLE, null, text, 0,
								labels, 0);
						mDialog.open();
						mDialog.create();
						retValue = mDialog.getReturnCode();
					}
				});
		return retValue;
	}

	/**
	 * Opens console view.
	 */
	public static void openConsoleView() {
		// sync with UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {

				try {
					// get active workspace page
					IWorkbenchPage page = Activator.getDefault().getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();

					// display main view
					if (page != null) {
						IWorkbenchPart part = page.getActivePart();

						String activePageID = part.getSite().getId();

						if (activePageID.equals(Constants.ANALYZE_TOOL_VIEW_ID)
								|| part instanceof org.eclipse.ui.navigator.CommonNavigator) {
							page
									.showView(org.eclipse.ui.console.IConsoleConstants.ID_CONSOLE_VIEW);
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Opens selection dialog.
	 * 
	 * @param shell
	 *            Shell reference
	 * @param title
	 *            Dialog title
	 * @param defaultLocation
	 *            Dialog default location
	 * @return User selection
	 */
	public static String openFileDialog(final Shell shell, final String title,
			final String defaultLocation) {
		FileDialog fileDialog = new FileDialog(shell);
		fileDialog.setText(title);
		// set the file extension filter
		String[] filterExt = { "*.*", "*.dat", "*.xml" };
		fileDialog.setFilterExtensions(filterExt);

		// fileDialog.setFilterPath( defaultLocation );
		return fileDialog.open();
	}

	/**
	 * Opens selection dialog.
	 * 
	 * @param title
	 *            Dialog title
	 * @param message
	 *            Dialog message text
	 * @param input
	 *            Dialog input
	 * @return Selected item
	 */
	public static String openSelectionDialog(final String title,
			final String message, final AbstractList<String> input) {
		Activator.getDefault().getWorkbench().getDisplay().syncExec(
				new Runnable() {
					public void run() {
						userSelection = "";

						ListDialog ld = new ListDialog(new Shell());
						ld.setAddCancelButton(true);
						ld.setContentProvider(new ArrayContentProvider());
						ld.setLabelProvider(new LabelProvider());

						int width = 0;
						// calculate dialog width
						for (int i = 0; i < input.size(); i++) {
							String tempStr = input.get(i);

							if (tempStr.length() > width) {
								width = tempStr.length();
							}
						}

						// set dialog width
						// ld.setWidthInChars( width + 2 );
						ld.setInput(input);
						ld.setTitle(title);
						if (message != null) {
							ld.setMessage(message);
						}

						ld.setHelpAvailable(false);
						ld.open();

						Object[] objs = ld.getResult();
						if (objs != null) {
							userSelection = objs[0].toString();
						}

					}
				});
		return userSelection;
	}

	/**
	 * Displays error message.
	 * 
	 * @param message
	 *            Error note content
	 */
	public static void showErrorMessage(final String message) {

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						Constants.DIALOG_TITLE, message);
			}
		});

	}

	/**
	 * Displays information note.
	 * 
	 * @param message
	 *            Information note content
	 */
	public static void showMessage(final String message) {

		// sync with the UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {

				MessageDialog.openInformation(Activator.getDefault()
						.getWorkbench().getDisplay().getActiveShell(),
						Constants.DIALOG_TITLE, message);
			}
		});

	}

	/**
	 * Check is atool.exe verbose output checked.
	 * 
	 * @return True verbose output is checked otherwise False
	 */
	public static boolean verboseAtoolOutput() {
		IPreferenceStore store = Activator.getPreferences();
		return store.getBoolean(Constants.ATOOL_VERBOSE);
	}

	/**
	 * Creates progress dialog and clears atool.exe made changes.
	 * 
	 * @param newProject
	 *            Project reference
	 */
	public static final void clearAtoolChanges(final IProject newProject) {

		// project reference
		final IProject project = newProject;

		Job cleanJob = new Job(Constants.PROGRESSDIALOG_CLEAR_CHANGES) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// update progress monitor state
				monitor.beginTask(Constants.PROGRESSDIALOG_CLEAR_CHANGES,
						IProgressMonitor.UNKNOWN);

				// get project info
				ICarbideProjectInfo cpi = CarbideBuilderPlugin
						.getBuildManager().getProjectInfo(project);
				if (cpi == null) {
					return new Status(IStatus.OK, Constants.ANALYZE_CONSOLE_ID,
							IStatus.OK,
							Constants.PROGRESSDIALOG_CLEAN_COMPLETE, null);
				}

				// new command launcher
				CarbideCommandLauncher cmdLauncher = new CarbideCommandLauncher(
						project, monitor, Constants.atoolParserIds, cpi
								.getINFWorkingDirectory());
				cmdLauncher.startTimingStats();

				cmdLauncher.showCommand(true);

				cmdLauncher.writeToConsole(cmdLauncher.getTimingStats());

				AbstractList<String> usedArguments = new ArrayList<String>();
				usedArguments.add("-c");
				if (Util.verboseAtoolOutput()) {
					usedArguments.add(Constants.ATOOL_SHOW_DEBUG);
				}

				String[] arguments = new String[usedArguments.size()];
				usedArguments.toArray(arguments);
				int error = Constants.COMMAND_LINE_ERROR_CODE.OK.getCode();
				error = cmdLauncher.executeCommand(new Path(Util
						.getAtoolInstallFolder()), arguments, CarbideCPPBuilder
						.getResolvedEnvVars(cpi.getDefaultConfiguration()), cpi
						.getINFWorkingDirectory());

				// if command line engine returns some error code => display the
				// error
				if (error != Constants.COMMAND_LINE_ERROR_CODE.OK.getCode()) {
					Constants.COMMAND_LINE_ERROR_CODE errorCode = Util
							.getErrorCode(error);
					Util.displayCommandLineError(errorCode);
				}

				return new Status(IStatus.OK, Constants.ANALYZE_CONSOLE_ID,
						IStatus.OK, Constants.PROGRESSDIALOG_CLEAN_COMPLETE,
						null);
			}
		};
		cleanJob.setUser(true);
		cleanJob.schedule();
	}

	/**
	 * Compares two atool.exe version numbers and returns index of the higher
	 * one.
	 * 
	 * @param firstVersion
	 *            First version number
	 * @param secondVersion
	 *            Second version number
	 * @return 0 if the first one is higher, 1 if the second one is higher, 2 if
	 *         the versions equals otherwise -1
	 */
	public static int compareVersionNumber(String firstVersion,
			String secondVersion) {

		// split version numbers
		String first[] = firstVersion.split("[.]");
		String second[] = secondVersion.split("[.]");

		// check that version numbers contains valid formatting
		if (first == null || second == null || first.length < 1
				|| second.length < 1 || (first.length != second.length)) {
			return Constants.VERSION_NUMBERS_INVALID;
		}
		// if version number equals => no need to check which one is higher
		if (firstVersion.equalsIgnoreCase(secondVersion)) {
			return Constants.VERSION_NUMBERS_EQUALS;
		}
		// thru splitted version number
		for (int i = 0; i < first.length; i++) {
			try {
				int firstNumber = Integer.parseInt(first[i]);
				int secondNumber = Integer.parseInt(second[i]);

				// if first and second given number equals => skip to next
				// number
				if (firstNumber == secondNumber) {
					continue;
				} else if (firstNumber > secondNumber) {
					return Constants.VERSION_NUMBERS_FIRST;
				} else {
					return Constants.VERSION_NUMBERS_SECOND;
				}

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				return Constants.VERSION_NUMBERS_INVALID;
			}
		}
		return Constants.VERSION_NUMBERS_INVALID;
	}

	/**
	 * Displays corresponding error message.
	 * 
	 * @param errorCode
	 *            Error code
	 */
	public static final void displayCommandLineError(
			Constants.COMMAND_LINE_ERROR_CODE errorCode) {
		switch (errorCode) {
		case EXECUTE_ERROR:
			Util.showErrorMessage(Constants.ERROR_ATOOL_NOT_AVAILABLE);
			break;
		case OK:
			// everything is OK, no need to do anything
			break;

		case INVALID_ARGUMENT_ERROR:
			Util
					.showErrorMessage("AnalyzeTool tried to execute command line engine with invalid parameters. \n\nTo avoid this go to AnalyzeTool preference page and select \"Use internal "
							+ "command line engine\".");
			Activator
					.getDefault()
					.logInfo(
							IStatus.ERROR,
							IStatus.ERROR,
							"AnalyzeTool - Extension tried to execute command line engine with invalid parameters.");
			break;

		case CANNOT_FIND_EPOCROOT:
			Util
					.showErrorMessage("AnalyzeTool can not find epocroot. \nCheck SDK preferences and build project again.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Can not find epocroot.");
			break;

		case MAKEFILE_ERROR:
			Util
					.showErrorMessage("AnalyzeTool command line engine fails to create makefiles.\n\nTry to clean AnalyzeTool changes and build project again."
							+ "\nIf problem still occurs contact AnalyzeTool development team.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Make file error.");
			break;

		case NO_SUPPORTED_MODULES_ERROR:
			Util
					.showErrorMessage("Project contains unsupported modules, this project could not build with AnalyzeTool!");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - No supported modules error.");
			break;

		case WRONG_DATA_FILE_VERSION:
			Util
					.showErrorMessage("Selected data file contains invalid version number. \nThis usually means that data file "
							+ "format is changed and AnalyzeTool command line engine can not resolve that. \n\nTry run tests again with the newest version of AnalyzeTool.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Wrong data file version.");
			break;

		case ANALYZE_ERROR:
			Util
					.showErrorMessage("AnalyzeTool can not analyze the selected data file.\nThis problem usually occurs when selected file contains corrupted AnalyzeTool data. \nTry to run "
							+ "tests again with the newest version of AnalyzeTool.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - General Analyze Error.");
			break;

		case SYMBOL_FILE_ERROR:
			Util
					.showErrorMessage("AnalyzeTool can not resolve selected symbol file(s). \n\nGo to AnalyzeTool advanced preference page and check selected symbol file(s).");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Symbol file error.");
			break;

		case DATA_FILE_EMPTY:
			Util
					.showErrorMessage("Can not analyze. \n\nSelected file is empty.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Selected file is empty.");
			break;

		case DATA_FILE_INVALID:
			Util
					.showErrorMessage("Can not analyze. \n\nSelected data file does not contain AnalyzeTool data.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Invalid data file.");
			break;

		case RELEASABLES_ERROR:
			Util
					.showErrorMessage("AnalyzeTool can not copy needed files, therefore callstack can not be displayed when analyzing data."
							+ "\n\nTry to clean AnalyzeTool made changes and build project again.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Releasebles error.");
			break;

		case RESTORE_MODULES_ERROR:
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Restore module error.");
			break;

		case CREATING_TEMP_CPP_ERROR:
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Creating temp cpp file error.");
			break;

		case CLEANING_TEMP_ERROR:
			Util
					.showErrorMessage("AnalyzeTool failed to clean project. You may have to clean changes manually.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Clean error.");
			break;

		case READ_MAKEFILE_ERROR:
			Util
					.showErrorMessage("AnalyzeTool command line engine can not read project make files. \n\nTry to clean AnalyzeTool"
							+ " made changes and build project again.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Read make file error.");
			break;

		case MODIFY_MODULES_ERROR:
			Util
					.showErrorMessage("AnalyzeTool command line engine can not modify project modules.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Modify modules error.");
			break;

		case INVALID_MMP_DEFINED:
			// I think this case is not possible in extension side.
			// because we ask mmp files from Carbide
			// however if this case happens we just log it Carbide error log
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Invalid mmp file.");
			break;

		case EMPTY_DATA_FILE:
			Util
					.showErrorMessage("Can not analyze. \n\nSelected file is empty.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - Selected file is empty.");
			break;
		case UNKNOWN_ERROR:
			Util
					.showErrorMessage("Unable to execute action for current project.");
			Activator.getDefault().logInfo(IStatus.ERROR, IStatus.ERROR,
					"AnalyzeTool - unknown error.");
			break;

		}
	}

	/**
	 * Finds correct Enum value for integer value.
	 * 
	 * @param value
	 *            Value to find from enum values
	 * @return Enum value if found otherwise
	 *         COMMAND_LINE_ERROR_CODE.UNKNOWN_ERROR
	 */
	public static COMMAND_LINE_ERROR_CODE getErrorCode(final int value) {
		for (COMMAND_LINE_ERROR_CODE errorCode : COMMAND_LINE_ERROR_CODE
				.values()) {
			if (errorCode.getCode() == value) {
				return errorCode;
			}
		}
		return COMMAND_LINE_ERROR_CODE.UNKNOWN_ERROR;
	}
}
