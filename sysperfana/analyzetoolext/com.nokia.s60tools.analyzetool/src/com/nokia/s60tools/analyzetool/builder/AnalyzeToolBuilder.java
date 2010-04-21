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
 * Description:  Definitions for the class AnalyzeToolBuilder
 *
 */

package com.nokia.s60tools.analyzetool.builder;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
import com.nokia.carbide.cdt.builder.builder.CarbideCommandLauncher;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cdt.builder.BuildArgumentsInfo;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.ui.CustomInputDialog;

/**
 * Base class for AnalyzeTool builders.
 *
 * This class modifies Carbide builder settings during the AnalyzeTool build.
 * This class can be used to built whole project or selected components with
 * AnalyzeTool
 *
 * @author kihe
 *
 */
public class AnalyzeToolBuilder extends CarbideCPPBuilder {

	/** Pre-builder id. */
	public static final String AT_BUILDER_ID = "com.nokia.s60tools.analyzetool.analyzeToolPreBuilder";

	/** CarbideCommandLauncher reference. */
	protected static CarbideCommandLauncher cmdLauncher = null;

	/** Used mmp files. */
	protected List<String> mmpFiles;

	/** Verbose atool.exe output? */
	private boolean verbose;

	/**
	 * Gets shared instance of CarbideCommandLauncher.
	 *
	 * @return CarbideCommandLauncher reference
	 */
	protected static CarbideCommandLauncher getCarbideCommandLauncher() {
		return cmdLauncher;
	}

	/**
	 * Checks is SBS v2 build activated for the project.
	 *
	 * @param cpi
	 *            ICarbideProjectInfo
	 * @return True if SBS v2 build is activated otherwise False
	 */
	public static boolean isSBSBuildActivated(final ICarbideProjectInfo cpi) {
		try {
			final Class<?> buildManagerClass = Class
					.forName("com.nokia.carbide.cdt.builder.ICarbideBuildManager");

			java.lang.reflect.Method[] methods = buildManagerClass.getMethods();
			for (int i = 0; i < methods.length; i++) {
				java.lang.reflect.Method oneMethod = methods[i];
				if (oneMethod.toString().contains("isCarbideSBSv2Project")) {
					IProject project = cpi.getProject();
					if (project == null) {
						return false;
					}
					Object[] objs = new Object[1];
					objs[0] = project;
					Boolean obj = (Boolean) oneMethod.invoke(
							CarbideBuilderPlugin.getBuildManager(), objs);
					return obj.booleanValue();

				}
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			return false;
		} catch (SecurityException se) {
			se.printStackTrace();
			return false;
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
			return false;
		} catch (IllegalArgumentException iare) {
			iare.printStackTrace();
			return false;
		} catch (java.lang.reflect.InvocationTargetException ite) {
			ite.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Ask data file name from the user and after that executes atool.exe.
	 *
	 * @param monitor
	 *            {@link IProgressMonitor} reference
	 * @param cpi
	 *            {@link ICarbideProjectInfo} reference
	 * @param files
	 *            Used mmp files
	 *
	 * @return COMMAND_LINE_ERROR_CODE.OK if no errors, otherwise error code
	 */
	private int askFileNameAndBuild(final IProgressMonitor monitor,
			final ICarbideProjectInfo cpi, final List<IFile> files) {
		// ask for the user data file name
		CustomInputDialog dialog = new CustomInputDialog(
				Constants.ANALYZE_TOOL_TITLE,
				Constants.DIALOG_INPUT_DATA_FILE_NAME, "");
		dialog.open();

		//get data file name
		String dataFileName = dialog.getUserInput();
		int errorCode = Constants.COMMAND_LINE_ERROR_CODE.OK.getCode();
		// user press "Cancel"
		if (dataFileName == null || ("").equals(dataFileName)) {
			buildCancelled(monitor, true);
		} else { // if user specify data file name
			errorCode = executeAtool(dataFileName, Constants.ATOOL_INST,
					Constants.LOGGING_S60, files, cpi, monitor);
		}
		return errorCode;
	}

	/**
	 * Ask logging mode from the user.
	 *
	 * @return User selected logging mode
	 */
	private String askLoggingMode() {

		IPreferenceStore store = Activator.getPreferences();

		// logging modes
		AbstractList<String> modes = new ArrayList<String>();
		modes.add(Constants.PREFS_EXT);

		// if fast data gathering mode is enabled => add to selection list
		if( store.getBoolean(Constants.LOGGING_FAST_ENABLED ) ) {
			modes.add(Constants.PREFS_EXT_FAST);
		}
		modes.add(Constants.PREFS_S60);

		// open selection dialog
		String userSelection = Util.openSelectionDialog(
				Constants.DIALOG_SELECT_LOGGING_MODE, null, modes);

		// get used logging mode for the atool.exe
		// user selected logging mode can not be used straight because it is
		// different in the UI and which atool.exe uses
		String modeSelection = "";
		if (userSelection == Constants.PREFS_EXT) {
			modeSelection = Constants.LOGGING_EXT;
		} else if (userSelection == Constants.PREFS_S60) {
			modeSelection = Constants.LOGGING_S60;
		} else if( userSelection == Constants.PREFS_EXT_FAST) {
			modeSelection = Constants.LOGGING_EXT_FAST;
		}
		return modeSelection;
	}

	/**
	 * Cancels AnalyzeTool build.
	 *
	 * @param monitor
	 *            Currently running progress monitor
	 * @param continueBuild
	 *            False stops the whole build chain (including Carbide and other builders)
	 *            otherwise other than AnalyzeTool builds are executed normally.
	 */
	public final void buildCancelled(final IProgressMonitor monitor,
			final boolean continueBuild) {

		IPreferenceStore store = Activator.getPreferences();

		store.setValue(Constants.PREFS_BUILD_CANCELLED, true);

		// if user wants to continue build
		if (!continueBuild) {
			// write info to the Carbide console view
			getCarbideCommandLauncher().writeToConsole(
					Constants.BUILD_CANCELLED);

			// update monitor state
			monitor.setCanceled(true);
			monitor.done();

		}

		boolean promptMPPChange = store.getBoolean(Constants.PREFS_PROMPT_MMP);
		boolean manageDeps = store.getBoolean(Constants.PREFS_MANAGE_DEPS);
		boolean useConcBuild = store.getBoolean(Constants.PREFS_CONC_BUILD);

		// build canceled set project preference back to normal
		IPreferenceStore cStore = CarbideBuilderPlugin.getDefault()
				.getPreferenceStore();
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MMP_CHANGED_ACTION_PROMPT,
						promptMPPChange);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MANAGE_DEPENDENCIES,
						manageDeps);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_USE_CONCURRENT_BUILDING,
						useConcBuild);

		// CarbideCPPBuilder to forgot build state
		super.forgetLastBuiltState();
	}

	/**
	 * Builds user selected components.
	 *
	 * Normally this method is called from the CompileSymbianComponent
	 *
	 * @param selectedFiles
	 *            User selected files
	 */
	public final void buildComponents(final List<IFile> selectedFiles) {
		// create new job
		Job buildJob = new Job(Constants.BUILD_AND_INSTRUMENT) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {

					// get project
					IProject lastProject = selectedFiles.get(0).getProject();

					// get CarbideProjectInfo
					ICarbideProjectInfo cpi = CarbideBuilderPlugin
							.getBuildManager().getProjectInfo(lastProject);

					// get command launcher
					cmdLauncher = new CarbideCommandLauncher(lastProject,
							monitor, Constants.atoolParserIds, cpi
									.getINFWorkingDirectory());
					cmdLauncher.showCommand(true);

					// run pre steps for the build
					if (!runPreSteps(cmdLauncher, monitor, cpi)) {
						return new Status(IStatus.OK, Constants.ANALYZE_TOOL_TITLE,
								IStatus.OK, Constants.CANCELLED, null);
					}

					// build selected components
					runBuild(Constants.ATOOL_INST, monitor, cpi, selectedFiles);

					// run post steps
					runPostSteps(cpi);

					//after the build is finished => open the console view
					Util.openConsoleView();
				} finally {
					monitor.done();
				}
				return new Status(IStatus.OK, Constants.ANALYZE_TOOL_TITLE,
						IStatus.OK, Constants.COMPLETE, null);
			}
		};

		buildJob.setPriority(Job.BUILD);
		buildJob.schedule();
	}

	/**
	 * Checks that platform is ARMV5, GCEE or WINSCW
	 *
	 * @param cpi
	 *            ICarbideProjectInfo
	 * @return True if platform is set to ARMV5, GCCE or WINSCW otherwise False
	 */
	public final boolean checkPlatform(final ICarbideProjectInfo cpi) {
		String platform = cpi.getDefaultConfiguration().getPlatformString();
		if ( platform.equals(Constants.BUILD_TARGET_ARMV5) || platform.equals(Constants.BUILD_TARGET_WINSCW)
				|| platform.equals(Constants.BUILD_TARGET_GCEE) ) {
			return true;
		}
		return false;
	}

	/**
	 * Executes atool.exe with options.
	 *
	 * @param dataFileName
	 *            Used data file name
	 * @param type
	 *            Type of execution. Possible types ATOOL_INST or ATOOL_UNINST
	 * @param loggingModeCommand
	 *            Used logging mode
	 * @param userSelectedMmpFiles
	 *            List of user selected mmp files
	 * @param cpi
	 *            ICarbideProjectInfo reference
	 * @param monitor
	 *            Progress monitor reference
	 *
	 * @return COMMAND_LINE_ERROR_CODE.OK if no errors, otherwise error code
	 */
	protected final int executeAtool(final String dataFileName,
			final String type, final String loggingModeCommand,
			final List<IFile> userSelectedMmpFiles,
			final ICarbideProjectInfo cpi, final IProgressMonitor monitor) {

		// get used platform
		String platform = cpi.getDefaultConfiguration().getPlatformString().toLowerCase(Locale.US);

		// get build target
		String buildTarget = cpi.getDefaultConfiguration().getTargetString().toLowerCase(Locale.US);

		// used arguments to atool.exe
		AbstractList<String> usedArguments = new ArrayList<String>();

		// which logging mode is used
		if( loggingModeCommand.equalsIgnoreCase(Constants.LOGGING_EXT) )
		{
			usedArguments.add(Constants.ATOOL_INST_E);
		}
		else if( loggingModeCommand.equalsIgnoreCase(Constants.LOGGING_EXT_FAST)) {
			usedArguments.add(Constants.ATOOL_INST_EF);
		}
		else
		{
			usedArguments.add(Constants.ATOOL_INST_I);
		}

		// if data file is set
		if (dataFileName != null && !("").equals(dataFileName)) {
			usedArguments.add("-f");
			usedArguments.add(dataFileName);
		}

		// if "verbose atool.exe output" is enabled
		if (verbose) {
			usedArguments.add(Constants.ATOOL_SHOW_DEBUG);
		}

		// get callstack size
		IPreferenceStore store = Activator.getPreferences();
		if( store.getBoolean(Constants.USE_CALLSTACK_SIZE) ) {
			int callstackSize = store.getInt(Constants.CALLSTACK_SIZE);
			usedArguments.add(Constants.CALLSTACK_SIZE_OPTION);
			usedArguments.add(Integer.toString(callstackSize));
		}

		//add build command
		// if project using SBSv2 build system
		boolean sbsBuild = isSBSBuildActivated(cpi);
		if (sbsBuild) {
			usedArguments.add("sbs");
			usedArguments.add("-c");
			StringBuffer buildCommand = new StringBuffer();
			buildCommand.append(platform);
			buildCommand.append('_');
			buildCommand.append(buildTarget);
			usedArguments.add(buildCommand.toString());
		}
		else //use abld 
		{
			usedArguments.add("abld");
			usedArguments.add("build");
			usedArguments.add(platform);
			usedArguments.add(buildTarget);
		}

		int errorCode = Constants.COMMAND_LINE_ERROR_CODE.OK.getCode();
		// if user has selected custom setup of components =>build them
		// this means that call is come from CompileSymbianComponent class
		if (userSelectedMmpFiles != null && !userSelectedMmpFiles.isEmpty()) {

			//get atool.exe command
			Iterator<IFile> files = userSelectedMmpFiles.iterator();
			while(files.hasNext()) {
				IFile file = files.next();
				IPath location = file.getLocation();

				String mmpFileName = getMMPFileName(location, true);
				if( mmpFileName == null || ("").equals(mmpFileName)) {
					continue;
				}

				// if this is first mmp file add it parameter list
				usedArguments.add("-p");
				usedArguments.add(mmpFileName);
			}

			//now the command is ready
			//execute command
			// execute atool.exe via CommandLauncher class
			cmdLauncher.showCommand(true);
			String[] arguments = new String[usedArguments.size()];
		    usedArguments.toArray(arguments);
		    errorCode = cmdLauncher.executeCommand(new Path(Util
					.getAtoolInstallFolder()), arguments,
					CarbideCPPBuilder.getResolvedEnvVars(cpi
							.getDefaultConfiguration()), cpi
							.getINFWorkingDirectory());


			// if user press "Cancel"
			if (monitor.isCanceled()) {
				buildCancelled(monitor, false);
				monitor.done();
			}

			// thru selected files and build with the Carbide builder
			for (int i = 0; i < userSelectedMmpFiles.size(); i++) {

				// get one file
				IFile file = userSelectedMmpFiles.get(i);

				// get file location
				IPath fileLocation = file.getLocation();

				// invoke normal Carbide build
				try {
					CarbideCPPBuilder.invokeSymbianComponenetAction(cpi
							.getDefaultConfiguration(),
							CarbideCPPBuilder.BUILD_COMPONENT_ACTION,
							fileLocation, cmdLauncher, monitor, true);
				} catch (CoreException e) {
					e.printStackTrace();
				}

				// if user press "Cancel"
				if (monitor.isCanceled()) {
					buildCancelled(monitor, false);
					monitor.done();
				}
			}

			// Carbide build is finished
			// now remove AnalyzeTool made modifications
			// if project contains build erros => only uninstrument mmp
			// file
			if (CarbideCPPBuilder.projectHasBuildErrors(cpi
					.getProject())) {
				usedArguments.set(0, Constants.ATOOL_UNINST_FAILED);
			}

			// project succesfully build => uninstrumet project
			runUninstrument(Constants.ATOOL_UNINST, cpi, monitor);

		}
		// if build from bld.inf file
		else if (cpi.isBuildingFromInf()) {
			cmdLauncher.showCommand(true);
			String[] arguments = new String[usedArguments.size()];
		    usedArguments.toArray(arguments);
		    errorCode = cmdLauncher.executeCommand(new Path(Util.getAtoolInstallFolder()),
					arguments,
					CarbideCPPBuilder.getResolvedEnvVars(cpi
							.getDefaultConfiguration()), cpi
							.getINFWorkingDirectory());

			if (mmpFiles != null) {
				mmpFiles.clear();
			}

			// if user press "Cancel"
			if (monitor.isCanceled()) {
				buildCancelled(monitor, false);
				monitor.done();
			}

		} else { // instrument only defined components
			// get build components
			mmpFiles = cpi.getInfBuildComponents();

			for (int i = 0; i < mmpFiles.size(); i++) {
				usedArguments.add("-p");
				usedArguments.add(mmpFiles.get(i));
			}
			cmdLauncher.showCommand(true);
			String[] arguments = new String[usedArguments.size()];
		    usedArguments.toArray(arguments);
		    errorCode = cmdLauncher.executeCommand(new Path(Util
					.getAtoolInstallFolder()), arguments,
					CarbideCPPBuilder.getResolvedEnvVars(cpi
							.getDefaultConfiguration()), cpi
							.getINFWorkingDirectory());

			// if user press "Cancel"
			if (monitor.isCanceled()) {
				buildCancelled(monitor, false);
				monitor.done();
			}
		}
		return errorCode;
	}

	/**
	 * Parses mmp file from the entered path
	 * @param fileLocation MMP file location with path.
	 * @param sbsBuild Is sbsv2 build system activated
	 * @return MMP file name without path. If SBSv2 is not activated the MMP 
	 * 		 file is returned without file extension.
	 */
	public String getMMPFileName(IPath fileLocation, boolean sbsBuild)
	{
		// because mmp file contains project related path
		// we need to parse mmp file name
		int index = Util.getLastSlashIndex(fileLocation.toString());
		String mmpFileName = null;
		if (index == -1) {
			// mmp file is incorrect => skip this file
			return mmpFileName;
		}

		// parse mmp file name
		mmpFileName = fileLocation.toString().substring(
				index + 1, fileLocation.toString().length());

		//if not using the SBS2 builds => needs to remove mmp file name extension
		if( !sbsBuild && mmpFileName.endsWith(".mmp")) {
			mmpFileName = mmpFileName.substring(0, mmpFileName.length()-4);
		}
		return mmpFileName;
	}


	/**
	 * Runs AnalyzeTool build.
	 *
	 * @param type
	 *            Type of execution. Possible types ATOOL_INST or ATOOL_UNINST
	 * @param monitor
	 *            Progress monitor reference
	 * @param cpi
	 *            ICarbideProjectInfo reference
	 * @param files
	 *            List of user selected mmp files
	 * @return True is no errors otherwise False
	 */
	protected final boolean runBuild(final String type,
			final IProgressMonitor monitor, final ICarbideProjectInfo cpi,
			final List<IFile> files) {

		// check AnalyzeTool libraries if the AnalyzeTool libraries missing =>
		// creates error marker for the project and cancels build
		if( !Util.checkAtoolLibs(cpi) )
		{
			buildCancelled(monitor, false);
			return false;
		}

		// get preference store
		IPreferenceStore store = Activator.getPreferences();

		// get active logging mode
		String loggingMode = store.getString(Constants.LOGGING_MODE);
		String s60FileNameMode = store.getString(Constants.S60_LOG_FILE_MODE);
		verbose = store.getBoolean(Constants.ATOOL_VERBOSE);

		// possible error code from command line engine
		int errorCode = Constants.COMMAND_LINE_ERROR_CODE.OK.getCode();

		// if logging mode is set to "ask always"
		// ask for user used logging mode
		if (Constants.LOGGING_ASK_ALLWAYS.equals(loggingMode)) {
			String modeSelection = askLoggingMode();

			// user press "Cancel"
			if (modeSelection == null || ("").equals(modeSelection)) {
				buildCancelled(monitor, true);
				return false;
			}
			// if user selects S60 log file and mode
			// and data file must ask for user
			else if (Constants.LOGGING_S60.equals(modeSelection)
					&& Constants.LOGGING_S60_USER_SPECIFIED
							.equals(s60FileNameMode)) {
				errorCode = askFileNameAndBuild(monitor, cpi, files);
			} else { // no need to ask data file for the user => just build
						// with
				// user selected logging mode
				errorCode = executeAtool(null, type, modeSelection, files, cpi, monitor);
			}

		}
		// if used logging mode is s60
		// and data file name must ask for the user
		else if (Constants.LOGGING_S60.equals(loggingMode)
				&& Constants.LOGGING_S60_USER_SPECIFIED.equals(s60FileNameMode)) {
			errorCode = askFileNameAndBuild(monitor, cpi, files);
		}
		// build with selected mode
		else {
			errorCode = executeAtool(null, type, loggingMode, files, cpi, monitor);
		}


		// no errors from command line engine
		if( errorCode == Constants.COMMAND_LINE_ERROR_CODE.OK.getCode() ) {
			return true;
		}

		// if some error code is returned from command line engine display it to user
		// and cancel build
		Constants.COMMAND_LINE_ERROR_CODE error = Util.getErrorCode(errorCode);
		Util.displayCommandLineError(error);
		buildCancelled(monitor, false);
		return false;
	}

	/**
	 * After the built is finished set preferences back to normal.
	 * @param cpi ICarbideProjectInfo refernece
	 */
	public final void runPostSteps(ICarbideProjectInfo cpi) {

		IPreferenceStore store = Activator.getPreferences();
		boolean keepFilesSync = store.getBoolean(Constants.PREFS_KEEP_IN_SYNC);
		boolean promptMPPChange = store.getBoolean(Constants.PREFS_PROMPT_MMP);
		boolean manageDeps = store.getBoolean(Constants.PREFS_MANAGE_DEPS);
		boolean useConcBuild = store.getBoolean(Constants.PREFS_CONC_BUILD);

		// try to load ProjectUIPlugin class
		try {
			Class.forName("com.nokia.carbide.cpp.project.ui.utils.ProjectUIUtils");

			// no need to keep project files in sync
			// this should fix the mmp selection dialog prompt
			com.nokia.carbide.cpp.project.ui.utils.ProjectUIUtils
					.setKeepProjectsInSync(keepFilesSync);
		} catch (ClassNotFoundException cnte) {
			//Do nothing by design
		}

		// set builder preference to not prompt mmp file change dialog
		IPreferenceStore cStore = CarbideBuilderPlugin.getDefault()
				.getPreferenceStore();


		// add existing/default values of Carbide builder settings
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MMP_CHANGED_ACTION_PROMPT,
						promptMPPChange);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MANAGE_DEPENDENCIES,
						manageDeps);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_USE_CONCURRENT_BUILDING,
						useConcBuild);


		// try to remove -debug parameter from the build arguments list
		try {
			//get build configuration
			ICarbideBuildConfiguration config = cpi.getDefaultConfiguration();

			//get build arguments info
			BuildArgumentsInfo info = config.getBuildArgumentsInfoCopy();

			//get used platform
			String platform = config.getPlatformString();

			String debug = "-debug";
			String abldArgs = info.abldBuildArgs;

			//if platform is set to ARMV5 or GCCE
			if ( platform.contains(Constants.BUILD_TARGET_ARMV5) || platform.contains(Constants.BUILD_TARGET_GCEE)) {

				//get "-debug" string index
				int index = abldArgs.indexOf(debug);

				//if abld build arguments contains only "-debug" parameter
				if( abldArgs.equals(debug) ) {
					info.abldBuildArgs = "";
					config.setBuildArgumentsInfo(info);
				}
				//remove just "-debug" word
				else if( abldArgs.contains(debug) && index != -1 ) {
					info.abldBuildArgs = abldArgs.substring(0,index) + abldArgs.substring(index+debug.length(), abldArgs.length());
					config.setBuildArgumentsInfo(info);
				}
			}

		}catch( java.lang.NoSuchMethodError nsme ) {
			//Do nothing by design
			//user might run AT with too old Carbide version
		}catch( Exception e ) {
			e.printStackTrace();
		}

		//after the build is finished => open the console view
		Util.openConsoleView();
	}

	/**
	 * Checks is atool.exe available and modifies Carbide preferences.
	 *
	 * @param launcher
	 *            Command launcher
	 * @param monitor
	 *            Progress monitor
	 * @param cpi
	 *            Carbide project info
	 * @return True if all checks are OK, otherwise False
	 */
	protected final boolean runPreSteps(final CarbideCommandLauncher launcher,
			final IProgressMonitor monitor, final ICarbideProjectInfo cpi) {


		// set command launchers
		cmdLauncher = launcher;

		// check is atool.exe available
		if (!Util.isAtoolAvailable()) {
			launcher.writeToConsole(Constants.INFO_ATOOL_NOT_AVAILABLE);
			buildCancelled(monitor, false);
			Util.showErrorMessage(Constants.ERROR_ATOOL_NOT_AVAILABLE);
			return false;
		}
		
		// check AnalyzeTool version, 1.6.0 and forward versions is supported
		String atoolVersion = Util.getAtoolVersionNumber(Util.getAtoolInstallFolder());
		int compared = Util.compareVersionNumber(atoolVersion, Constants.MIN_VERSION);
		if( compared == Constants.VERSION_NUMBERS_SECOND || compared == Constants.VERSION_NUMBERS_INVALID ) {
			buildCancelled(monitor, false);
			Util.showMessage(Constants.TOO_OLD_ENGINE);
			return false;
		}

		/**
		 *
		 * Below is code a sample which are related to the AT-682,
		 * but it is decided to let out from current release.
		String coreVersion = Util.getAtoolCoreVersion(cpi.getProject());
		compared = Util.compareVersionNumber(coreVersion, atoolVersion);
		if( compared != Constants.VERSION_NUMBERS_EQUALS ) 
		{
			boolean retValue = Util.openConfirmationDialog("AnalyzeTool command line engine and AnalyzeTool core version mismatch.\n" +
					"This usually leads to problems.\n\nDo you want to continue?");
			if( !retValue ) {
				buildCancelled(monitor, false);
				return false;	
			}
		}
		*/
		// remove existing error markers
		try {
			CarbideCPPBuilder.removeAllMarkers(cpi.getProject());
		}catch (CoreException ce) {
			ce.printStackTrace();
			return false;
		}
		
		// check used platform
		if (!checkPlatform(cpi)) {
			buildCancelled(monitor, Util
					.openConfirmationDialog(Constants.PLATFORM_NOT_SUPPORTED));
			return false;
		}

		// check AnalyzeTool libraries if the AnalyzeTool libraries missing =>
		// creates error marker for the project and cancels build
		if( !Util.checkAtoolLibs(cpi) )
		{
			PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() {
				public void run() {
					// show error message for the user
					Util.showMessage(Constants.CAN_NOT_FIND_LIBRARIES);
				}
			});
			buildCancelled(monitor, false);
			return false;
		}

		// use CarbideCPPBuilder to bldmake bldfiles for selected project
		String[] bldmakeArgs = { "bldfiles" };
		CarbideCPPBuilder.invokeBldmakeCommand(cpi.getDefaultConfiguration(),
				launcher, bldmakeArgs, true);

		IPreferenceStore store = Activator.getPreferences();

		// try to load ProjectUIPlugin class
		try {
			Class.forName("com.nokia.carbide.cpp.project.ui.ProjectUIPlugin");

			// get keep project files in sync flag
			boolean keepFilesSync = com.nokia.carbide.cpp.project.ui.utils.ProjectUIUtils
					.keepProjectsInSync();

			store.setValue(Constants.PREFS_KEEP_IN_SYNC, keepFilesSync);

			// no need to keep project files in sync
			// this should fix the mmp selection dialog prompt
			com.nokia.carbide.cpp.project.ui.utils.ProjectUIUtils
					.setKeepProjectsInSync(false);
		} catch (ClassNotFoundException cnfe) {
			//Do nothing by design
		}

		// set builder preference to not prompt mmp file change dialog
		IPreferenceStore cStore = CarbideBuilderPlugin.getDefault()
				.getPreferenceStore();

		// get existing values
		boolean promptMPPChange = cStore
				.getBoolean(com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MMP_CHANGED_ACTION_PROMPT);
		boolean manageDeps = cStore
				.getBoolean(com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MANAGE_DEPENDENCIES);
		boolean useConcBuild = cStore
				.getBoolean(com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_USE_CONCURRENT_BUILDING);

		//store existing values
		store.setValue(Constants.PREFS_PROMPT_MMP, promptMPPChange);
		store.setValue(Constants.PREFS_MANAGE_DEPS, manageDeps);
		store.setValue(Constants.PREFS_CONC_BUILD, useConcBuild);
		// add custom values for AnalyzeTool build
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MMP_CHANGED_ACTION_PROMPT,
						false);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_MANAGE_DEPENDENCIES,
						false);
		cStore
				.setValue(
						com.nokia.carbide.cdt.builder.BuilderPreferenceConstants.PREF_USE_CONCURRENT_BUILDING,
						false);
		
		// try to add -debug parameter to the build arguments list
		try {
			//get selected build configuration
			ICarbideBuildConfiguration config = cpi.getDefaultConfiguration();

			//get build arguments info
			BuildArgumentsInfo info = config.getBuildArgumentsInfoCopy();

			//get selected platform
			String platform = cpi.getDefaultConfiguration().getPlatformString();

			//if platform is ARMV5 or GCCE and the "-debug" parameter is not set => we need set the parameter
			if ( (platform.contains(Constants.BUILD_TARGET_ARMV5) || platform.contains(Constants.BUILD_TARGET_GCEE)) && !info.abldBuildArgs.contains("-debug") ) {
				if( info.abldBuildArgs.length() > 0 && !info.abldBuildArgs.endsWith(" ") ) {
					info.abldBuildArgs += " ";
				}
				info.abldBuildArgs += "-debug";
				config.setBuildArgumentsInfo(info);
			}

		}
		//catch NoSuchMethodError because is it possible to use older versions where this method is not available
		catch( java.lang.NoSuchMethodError nsme)
		{
			//Do nothing by design
		}

		return true;
	}

	/**
	 * Executes uninstrument for the project.
	 * @param command Uninstrument command
	 * @param cpi ICarbideProjectInfo reference
	 * @param monitor IProgressMonitor reference
	 */
	public void runUninstrument(String command, ICarbideProjectInfo cpi, IProgressMonitor monitor)
	{
		cmdLauncher.showCommand(true);
		String[] arguments = new String[1];
	    arguments[0] = command;
		int error = cmdLauncher.executeCommand(new Path(Util
				.getAtoolInstallFolder()),arguments,
				CarbideCPPBuilder.getResolvedEnvVars(cpi
						.getDefaultConfiguration()), cpi
					.getINFWorkingDirectory());
		
		// if some occurs => display it to user
		if( error != Constants.COMMAND_LINE_ERROR_CODE.OK.getCode() ) {
			Constants.COMMAND_LINE_ERROR_CODE errorCode = Util.getErrorCode(error);
			Util.displayCommandLineError(errorCode);
		}
	}
}
