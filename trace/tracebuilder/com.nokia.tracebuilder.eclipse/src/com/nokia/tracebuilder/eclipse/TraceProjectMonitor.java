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
 * Description:
 *
 * Workbench monitor which opens / closes the trace project
 *
 */
package com.nokia.tracebuilder.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Version;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.DefaultMMPViewConfiguration;
import com.nokia.carbide.cdt.builder.EMMPPathContext;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.InvalidDriveInMMPPathException;
import com.nokia.carbide.cdt.builder.MMPViewPathHelper;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.epoc.engine.EpocEnginePlugin;
import com.nokia.carbide.cpp.epoc.engine.MMPDataRunnableAdapter;
import com.nokia.carbide.cpp.epoc.engine.MMPViewRunnableAdapter;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.EMMPStatement;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.IMMPData;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.IMMPView;
import com.nokia.carbide.cpp.epoc.engine.preprocessor.AcceptedNodesViewFilter;
import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderView;
import com.nokia.tracebuilder.engine.TraceLocationConverter;
import com.nokia.tracebuilder.engine.TraceLocationMap;
import com.nokia.tracebuilder.engine.TraceProjectMonitorInterface;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.FileErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.project.ProjectEngine;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.project.GroupNameHandlerBase;
import com.nokia.tracebuilder.project.GroupNameHandlerOSTv1;
import com.nokia.tracebuilder.project.GroupNameHandlerOSTv2;
import com.nokia.tracebuilder.source.SourceConstants;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.utils.DocumentFactory;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;
import com.nokia.carbide.cpp.sdk.core.ISDKManager;

/**
 * Workbench monitor which opens / closes the trace project
 * 
 */
class TraceProjectMonitor implements WorkbenchListenerCallback,
		TraceProjectMonitorInterface {

	/**
	 * OST_INSTRUMENTATION_API_VERSION text
	 */
	private static final String OST_INSTRUMENTATION_API_VERSION_TEXT = "OST_INSTRUMENTATION_API_VERSION"; //$NON-NLS-1$

	/**
	 * Regular expression for OST_INSTRUMENTATION_API_VERSION macro check
	 */
	private static final String OST_INSTRUMENTATION_API_VERSION_REGEX = "#define\\s+OST_INSTRUMENTATION_API_VERSION.*"; //$NON-NLS-1$

	/**
	 * OSTv1 version text
	 */
	private static final String OST_VERSION_1_X_X = "1.x.x"; //$NON-NLS-1$

	/**
	 * OSTv2 version text
	 */
	private static final String OST_VERSION_2_X_X = "2.x.x"; //$NON-NLS-1$

	/**
	 * OpenSystemTrace.h file name
	 */
	private static final String OPEN_SYSTEM_TRACE_H = "opensystemtrace.h"; //$NON-NLS-1$

	/**
	 * epoc32\include\include path as string
	 */
	private static final String EPOC32_INCLUDE_INTERNAL = "epoc32" + File.separator + "include" + File.separator + "internal"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * epoc32\include\platform path as string
	 */
	private static final String EPOC32_INCLUDE_PLATFORM = "epoc32" + File.separator + "include" + File.separator + "platform"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * PLATFORM_PATHS.HRH
	 */
	private static final String PLATFORM_PATHS_HRH = "PLATFORM_PATHS.HRH"; //$NON-NLS-1$

	/**
	 * #INCLUDE
	 */
	private static final String INCLUDE = "#INCLUDE"; //$NON-NLS-1$

	/**
	 * Multiline comment start
	 */
	private static final String MULTILINE_COMMENT_START = "/*"; //$NON-NLS-1$

	/**
	 * Comment
	 */
	private static final String COMMENT = "//"; //$NON-NLS-1$	

	/**
	 * Multiline comment end
	 */
	private static final String MULTILINE_COMMENT_END = "*/"; //$NON-NLS-1$

	/**
	 * ncp sw folder name
	 */
	private static final String NCP_SW_FOLDER_NAME = "ncp_sw"; //$NON-NLS-1$

	/**
	 * os folder name
	 */
	private static final String OS_FOLDER_NAME = "os"; //$NON-NLS-1$

	/**
	 * mw folder name
	 */
	private static final String MW_FOLDER_NAME = "mw"; //$NON-NLS-1$

	/**
	 * sf folder name
	 */
	private static final String SF_FOLDER_NAME = "sf"; //$NON-NLS-1$

	/**
	 * platform_paths.hrh include
	 */
	private static final String INCLUDE_PLATFORM_PATHS_HRH = "#include \"platform_paths.hrh\""; //$NON-NLS-1$

	/**
	 * OS layer system include macro
	 */
	private static final String OS_LAYER_SYSTEMINCLUDE_MACRO = "OS_LAYER_SYSTEMINCLUDE"; //$NON-NLS-1$

	/**
	 * MW layer system include macro
	 */
	private static final String MW_LAYER_SYSTEMINCLUDE_MACRO = "MW_LAYER_SYSTEMINCLUDE"; //$NON-NLS-1$

	/**
	 * APP layer system include macro
	 */
	private static final String APP_LAYER_SYSTEMINCLUDE_MACRO = "APP_LAYER_SYSTEMINCLUDE"; //$NON-NLS-1$

	/**
	 * Group directory
	 */
	private static final String GROUP_DIRECTORY = "group"; //$NON-NLS-1$	

	/**
	 * Traces directory
	 */
	public static final String TRACES_DIRECTORY = "traces"; //$NON-NLS-1$

	/**
	 * MmpFiles directory
	 */
	private static final String MMPFILES_DIRECTORY = "mmpfiles"; //$NON-NLS-1$

	/**
	 * Underscore character
	 */
	public static final String UNDERSCORE = "_"; //$NON-NLS-1$

	/**
	 * The workbench listener
	 */
	private WorkbenchListener listener;

	/**
	 * Project path that is currently open
	 */
	private String openProjectPath;

	/**
	 * Project object that is currently open
	 */
	private IProject openProjectObject;

	/**
	 * Trace project include added to MMP files
	 */
	private boolean mmpFileModified;

	/**
	 * Name of the "traces_<component name>" folder
	 */
	private String tracesComponentNameFolder;

	/**
	 * Name of the "traces_<target_name>_<target_type>" folder
	 */
	private String tracesTargetNameTargetTypeFolder;

	/**
	 * Name of the "traces\<target_name>_<target_ext>" folder
	 */
	private String tracesDirTargetNameTargetExtFolder;

	/**
	 * Trace folder name used in SBSv2
	 */
	private String sbsv2TraceFolderName;

	/**
	 * Select software component flag
	 */
	private boolean selectSoftwareComponent = true;

	/**
	 * Select software component check needed flag
	 */
	private boolean selectSoftwareComponentCheckNeeded = false;

	/**
	 * Trace folder changed flag
	 */
	private boolean traceFolderChanged = false;

	/**
	 * Previous file included more than once flag
	 */
	private boolean previousFileIncludedMoreThanOnce = false;

	/**
	 * Editor opened flag
	 */
	private boolean editorOpened = false;

	/**
	 * File object that is currently visible
	 */
	IFile openFileObject = null;

	/**
	 * Trace folder name of the project that is currently visible
	 */
	String openProjectTraceFolderName = null;

	/**
	 * OSTv1 start text
	 */
	private static final String OSTV1_START_TEXT = "1."; //$NON-NLS-1$	

	/**
	 * OSTv2 start text
	 */
	private static final String OSTV2_START_TEXT = "2."; //$NON-NLS-1$		

	/**
	 * Constructor
	 */
	TraceProjectMonitor() {
		listener = new WorkbenchListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.eclipse.TraceProjectMonitorInterface#startMonitor
	 * ()
	 */
	public void startMonitor() {
		listener.startListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tracebuilder.eclipse.TraceProjectMonitorInterface#stopMonitor()
	 */
	public void stopMonitor() {
		listener.stopListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorActivated(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorActivated(ITextEditor editor, IFile file) {
		TraceBuilderView view = TraceBuilderGlobals.getView();
		view.refresh();
		if (editorOpened) {
			view.expandTraceGroupsBranch();
			editorOpened = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorClosed(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorClosed(ITextEditor editor, IFile file) {
		if (!WorkbenchUtils.isEditorsOpen()) {
			if (openProjectPath != null) {
				TraceBuilderGlobals.getTraceBuilder().closeProject();
				openProjectPath = null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorOpened(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorOpened(ITextEditor editor, IFile file) {
		openFileObject = file;
		updateProject(file);
		editorOpened = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorHidden(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorHidden(ITextEditor editor, IFile file) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorVisible(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorVisible(ITextEditor editor, IFile file) {
		if (!file.equals(openFileObject)) {
			updateProject(file);
			openFileObject = file;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorReplaced(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorReplaced(ITextEditor editor, IFile file) {
		updateProject(file);
	}

	/**
	 * Opens the trace project related to given file
	 * 
	 * @param file
	 *            the file
	 */
	private void updateProject(IFile file) {
		IProject project = file.getProject();

		// Check that project is Carbide project
		boolean isCarbideProject = CarbideBuilderPlugin.getBuildManager()
				.isCarbideProject(project);
		if (isCarbideProject) {

			String projectPath = getProjectPath(file);

			// If trace folder name or project changes, existing project
			// needs to be closed
			if (traceFolderChanged == true
					|| (openProjectPath != null && projectPath != null && !projectPath
							.equals(openProjectPath))) {
				TraceBuilderGlobals.getTraceBuilder().closeProject();
				openProjectPath = null;
				openProjectObject = null;
			}
			if (projectPath != null && !projectPath.equals(openProjectPath)) {
				try {
					mmpFileModified = false;
					openProjectPath = projectPath;
					openProjectObject = file.getProject();
					ProjectEngine.traceFolderName = openProjectTraceFolderName;
					TraceBuilderGlobals.setProjectPath(projectPath);
					initializeDefaultGroupNames();

					// Get project name
					String projectName = getProjectName(projectPath);

					// Save Epoc root of this SDK
					saveEpocRoot(openProjectObject);

					// Open the project
					TraceBuilderGlobals.getTraceBuilder().openProject(
							projectName);

					// Parse traces from other source files in same
					// component
					parseOtherTracesFromComponent(file, project);

				} catch (TraceBuilderException e) {
					openProjectPath = null;
					openProjectObject = null;
					// If a source file is not open, this error is not
					// relevant
					// The project will be opened when a source is opened
					if (e.getErrorCode() != TraceBuilderErrorCode.SOURCE_NOT_OPEN) {
						TraceBuilderGlobals.getEvents().postError(e);
					}
				}
			}
		} else {
			TraceBuilderGlobals.getTraceBuilder().closeProject();
			openProjectPath = null;
			openProjectObject = null;
		}
	}

	/**
	 * Initialize default group names based on used OST version
	 * 
	 * @throws TraceBuilderException
	 */
	private void initializeDefaultGroupNames() throws TraceBuilderException {

		// Initialize default group names

		String ostVersion = null;
		IPath epocroot = WorkbenchUtils
				.getEpocRootForProject(openProjectObject);
		String epocRootAsString = epocroot.toOSString();
		File platformDir = new File(epocRootAsString + EPOC32_INCLUDE_PLATFORM);
		File platformOpenSystemTraceFile = new File(platformDir,
				OPEN_SYSTEM_TRACE_H);
		if (platformOpenSystemTraceFile.exists()
				&& platformOpenSystemTraceFile.isFile()) {
			ostVersion = getOstVersion(platformOpenSystemTraceFile);

			// If OST version is not defined in opensystemtrace.h file then
			// default is 1.x.x
			if (ostVersion == null) {
				ostVersion = OST_VERSION_1_X_X;
			}

		} else {
			File internalDir = new File(epocRootAsString
					+ EPOC32_INCLUDE_INTERNAL);
			File internalOpenSystemTraceFile = new File(internalDir,
					OPEN_SYSTEM_TRACE_H);

			// If opensystemtrace.h file exist only in internal folder then
			// version is 1.x.x. In OSTv2 case opensystemtrace.h file should
			// newer exist only in internal folder, so it is enough that we
			// check version only from header in platform folder, because OSTv1
			// version of header does not contain version information.
			if (internalOpenSystemTraceFile.exists()
					&& internalOpenSystemTraceFile.isFile()) {
				ostVersion = OST_VERSION_1_X_X;
			} else {

				// If opensystemtrace.h file does not exist at all then default
				// is 2.x.x
				ostVersion = OST_VERSION_2_X_X;
			}
		}

		// Check is OST version 1.x.x in use. If it is then use OSTv1 otherwise
		// use OSTv2
		GroupNameHandlerBase groupNameHandler = TraceBuilderGlobals
				.getGroupNameHandler();
		if (ostVersion.startsWith(OSTV1_START_TEXT)) {
			if (!(groupNameHandler instanceof GroupNameHandlerOSTv1)) {
				TraceBuilderGlobals
						.setGroupNameHandler(new GroupNameHandlerOSTv1());
			}
		} else if (ostVersion.startsWith(OSTV2_START_TEXT)) {
			if (!(groupNameHandler instanceof GroupNameHandlerOSTv2)) {
				TraceBuilderGlobals
						.setGroupNameHandler(new GroupNameHandlerOSTv2());
			}
		} else {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.UNKNOWN_OST_VERSION);
		}
	}

	/**
	 * Get OST version from OpenSystemTrace.h file
	 * 
	 * @param platformOpenSystemTraceFile
	 *            open system trace API file
	 * @return OST version Used OST version (e.g. 2.0.0)
	 * @throws TraceBuilderException
	 */
	private String getOstVersion(File platformOpenSystemTraceFile)
			throws TraceBuilderException {
		String ostVersion = null;
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(platformOpenSystemTraceFile
					.toString());
			DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read opensystemtrace.h file line by line
			try {
				while ((strLine = br.readLine()) != null) {
					boolean versionDefinitionFound = strLine
							.matches(OST_INSTRUMENTATION_API_VERSION_REGEX);
					if (versionDefinitionFound) {
						int versionNumberStart = strLine
								.indexOf(OST_INSTRUMENTATION_API_VERSION_TEXT)
								+ OST_INSTRUMENTATION_API_VERSION_TEXT.length();
						ostVersion = strLine.substring(versionNumberStart);
						ostVersion = ostVersion.trim();
						break;
					}
				}
			} catch (IOException e) {
				throw new TraceBuilderException(
						TraceBuilderErrorCode.UNEXPECTED_EXCEPTION, e);
			} finally {
				try {
					in.close();
				} catch (IOException e1) {
					throw new TraceBuilderException(
							TraceBuilderErrorCode.UNEXPECTED_EXCEPTION, e1);
				}
			}
		} catch (FileNotFoundException e2) {
			FileErrorParameters params = new FileErrorParameters();
			params.file = platformOpenSystemTraceFile.toString();
			throw new TraceBuilderException(
					TraceBuilderErrorCode.FILE_NOT_FOUND, params);
		}

		return ostVersion;
	}

	/**
	 * Parse traces from other source files in same component
	 * 
	 * @param file
	 *            the file
	 * @param project
	 *            the project
	 */
	private void parseOtherTracesFromComponent(IFile file, IProject project) {

		if (file != null && project != null) {
			ICarbideBuildConfiguration buildConfig = getBuildConfiguration(project);
			if (buildConfig != null) {

				// Get MMP files for given source file
				for (final IPath mmpPath : EpocEngineHelper.getMMPsForSource(
						openProjectObject, file.getLocation())) {
					IPath fullMmpPath = new Path(openProjectPath);
					fullMmpPath = fullMmpPath.append(mmpPath);

					String componentId = getComponentIdFromMMPFile(mmpPath,
							project);

					// Component ID in MMP file must be same as current software
					// component ID
					if (TraceBuilderGlobals.getCurrentSoftwareComponentId() != null
							&& TraceBuilderGlobals
									.getCurrentSoftwareComponentId().equals(
											componentId)) {

						// Get source files for MMP file
						for (final IPath sourcePath : EpocEngineHelper
								.getSourceFilesForConfiguration(buildConfig,
										fullMmpPath)) {
							File source = new File(sourcePath.toOSString());

							// Check that source file is not same as opened file
							if (!file.getName().equalsIgnoreCase(
									source.getName())
									&& !isFileOpenedInEditor(source)) {
								IWorkspace workspace = ResourcesPlugin
										.getWorkspace();
								IPath location = Path.fromOSString(source
										.getAbsolutePath());
								IFile isource = workspace.getRoot()
										.getFileForLocation(location);

								// Parse traces from source file
								parseTracesFromFile(isource);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Get build configuration
	 * 
	 * @param project
	 *            the project
	 * @return the build configuration
	 */
	private ICarbideBuildConfiguration getBuildConfiguration(IProject project) {
		ICarbideBuildManager buildMgr = CarbideBuilderPlugin.getBuildManager();
		ICarbideBuildConfiguration buildConfig = null;
		if (buildMgr != null) {

			// We do not need to check that is project Carbide
			// project because we have done that earlier
			ICarbideProjectInfo cpi = buildMgr.getProjectInfo(project);
			if (cpi != null) {

				// Get the default build configuration
				buildConfig = cpi.getDefaultConfiguration();
			}
		}
		return buildConfig;
	}

	/**
	 * Parse traces from given file
	 * 
	 * @param file
	 *            the file
	 */
	private void parseTracesFromFile(IFile file) {

		if (file != null) {

			// Get file content to string variable
			String sourceData = getFileContentAsString(file);

			if (sourceData != null) {

				// Create new document from source data
				IDocument document = new Document(sourceData);
				document
						.addPositionCategory(JFaceDocumentWrapper.TRACEBUILDER_POSITION_CATEGORY);
				JFaceLocationUpdater locationUpdater = new JFaceLocationUpdater(
						JFaceDocumentWrapper.TRACEBUILDER_POSITION_CATEGORY);
				document.addPositionUpdater(locationUpdater);
				SourceDocumentInterface sourceDocumentInterface = new JFaceDocumentWrapper(
						document);

				// Create new source properties
				SourceProperties properties = new SourceProperties(
						TraceBuilderGlobals.getTraceModel(), DocumentFactory
								.getDocumentMonitor().getFactory(),
						sourceDocumentInterface);

				// Parse trace locations from source properties
				properties.updateTraces(0, sourceData.length());

				TraceLocationMap locationMap = TraceBuilderGlobals
						.getLocationMap();

				// When we add source to location map it will go to unrelated
				// list
				locationMap.addSource(properties);

				// Parse traces from source so those will be added to the model
				TraceLocationConverter lc = TraceBuilderGlobals
						.getLocationConverter();
				lc.parseTracesFromSource(properties);

				String path = file.getLocation().removeLastSegments(1)
						.toOSString()
						+ "\\"; //$NON-NLS-1$
				String fileName = file.getName();

				// When we remove source from location map it will go to last
				// known locations list
				locationMap.removeSource(properties, path, fileName);
			}
		}
	}

	/**
	 * Get file content as string
	 * 
	 * @param file
	 *            the file
	 * @return file content as string
	 */
	private String getFileContentAsString(IFile file) {
		String fileContentAsString = null;
		boolean continueExecution = true;

		InputStream is = null;
		try {
			is = file.getContents();
		} catch (CoreException e1) {
			continueExecution = false;
		}

		if (continueExecution) {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();

			String line = null;
			try {

				// Read file content line by line to string builder
				while ((line = reader.readLine()) != null) {
					sb.append(line + SourceConstants.LINE_FEED);
				}
			} catch (IOException e) {
				continueExecution = false;
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					continueExecution = false;
				}
			}

			// If everything went OK, get content from string builder to string
			// variable
			if (continueExecution) {
				fileContentAsString = sb.toString();
			}
		}

		return fileContentAsString;
	}

	/**
	 * Check is file opened in editor
	 * 
	 * @param source
	 *            .getAbsolutePath() the file
	 * @return true if file is opened in editor, otherwise false
	 */
	private boolean isFileOpenedInEditor(File source) {
		boolean fileAlredyOpened = false;
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] refs = page.getEditorReferences();
				IFile alreadyOpenedFile = null;
				for (IEditorReference ref : refs) {
					alreadyOpenedFile = WorkbenchUtils.getEditorFile(ref);
					if (alreadyOpenedFile != null
							&& alreadyOpenedFile.getLocation().toOSString()
									.equals(source.getAbsolutePath())) {
						fileAlredyOpened = true;
						break;
					}
				}
				if (fileAlredyOpened) {
					break;
				}
			}
			if (fileAlredyOpened) {
				break;
			}
		}
		return fileAlredyOpened;
	}

	/**
	 * Gets project name from project path
	 * 
	 * @param projectPath
	 *            the project path
	 * @return project name
	 */
	private String getProjectName(String projectPath) {
		String separator = System.getProperty("file.separator"); //$NON-NLS-1$
		String projectName = projectPath.substring(projectPath
				.lastIndexOf(separator) + 1);
		return projectName;
	}

	/**
	 * Gets project path from file
	 * 
	 * @param file
	 *            the file
	 * @return the project path
	 */
	private String getProjectPath(IFile file) {
		String projectPath = null;

		// Initial project path is the file's parent folder
		IPath path = file.getLocation();
		IProject project = file.getProject();

		// Check if file is source file
		if (isSourceFile(file)) {
			projectPath = getProjectPathToSourceFile(project, path);

			// It could be that source file is not included to MMP file and
			// because that we did not find project path. So if source file
			// project path is null, try to find project path to source file
			// same way than non source file.
			if (projectPath == null) {
				projectPath = getProjetcPathToNonSourceFile(project, path);
			}
		} else {
			projectPath = getProjetcPathToNonSourceFile(project, path);
		}

		return projectPath;
	}

	/**
	 * Get project path to source file
	 * 
	 * @param project
	 *            project
	 * @param path
	 *            path where to start find project path
	 * @return the project path
	 */
	private String getProjectPathToSourceFile(IProject project, IPath path) {
		String projectPath = null;
		String mmpFilePath = null;
		IPath currentSoftwareComponentMMPPath = null;
		List<IPath> mmpPaths = EpocEngineHelper.getMMPsForSource(project, path);
		if (mmpPaths.size() > 0) {

			// Check that was source file included to more than one MMP file
			if (mmpPaths.size() > 1) {

				// User need to select software component to work with, if
				// previous file was also included to more than one MMP file or
				// if some of the previous files belonged to different
				// component.
				if (selectSoftwareComponent || previousFileIncludedMoreThanOnce) {

					// Clear old software component list
					TraceBuilderGlobals.clearSoftwareComponents();

					// Add all software components to software component list
					for (int i = 0; i < mmpPaths.size(); i++) {
						IPath mmpPath = mmpPaths.get(i);
						String mmpPathAsString = mmpPath.toString();
						String componentId = getComponentIdFromMMPFile(mmpPath,
								project);
						String componentName = getComponentNameFromMMPFileName(mmpPathAsString);
						TraceBuilderGlobals.addSoftwareComponent(componentId,
								componentName, mmpPathAsString);
					}

					// Ask user to select software component to be used
					try {
						TraceBuilderGlobals.getTraceBuilder().selectComponent();
					} catch (TraceBuilderException e) {
						TraceBuilderGlobals.getEvents().postError(e);
					}

					selectSoftwareComponentCheckNeeded = true;
				}

				currentSoftwareComponentMMPPath = new Path(TraceBuilderGlobals
						.getCurrentSoftwareComponentMMPPath());
				String currentSoftwareComponentName = TraceBuilderGlobals
						.getCurrentSoftwareComponentName();
				setPossibleTraceFolderNames(project,
						currentSoftwareComponentName,
						currentSoftwareComponentMMPPath);

				// Remove mmp file name from mmpPathAsString variable
				IPath projectLocation = project.getLocation();
				String mmpPathAsString = TraceBuilderGlobals
						.getCurrentSoftwareComponentMMPPath();
				int lastIndexOfForwardSlashChar = mmpPathAsString
						.lastIndexOf(SourceConstants.FORWARD_SLASH_CHAR);

				if (lastIndexOfForwardSlashChar != -1) {
					mmpPathAsString = mmpPathAsString.substring(0,
							lastIndexOfForwardSlashChar);
				}

				// Append mmpPathAsString to Project path to get real
				// mmp file path
				mmpFilePath = projectLocation.append(mmpPathAsString)
						.toString();

				previousFileIncludedMoreThanOnce = true;
			} else {
				currentSoftwareComponentMMPPath = mmpPaths.get(0);
				String mmpPathAsString = currentSoftwareComponentMMPPath
						.toString();

				String componentId = getComponentIdFromMMPFile(
						currentSoftwareComponentMMPPath, project);
				String componentName = getComponentNameFromMMPFileName(mmpPathAsString);

				if (selectSoftwareComponentCheckNeeded) {
					if (TraceBuilderGlobals.getCurrentSoftwareComponentId() != null
							&& TraceBuilderGlobals
									.getCurrentSoftwareComponentId().equals(
											componentId)) {
						selectSoftwareComponent = false;
					} else {
						selectSoftwareComponent = true;
						selectSoftwareComponentCheckNeeded = false;
					}
				}

				// Clear old software component list
				TraceBuilderGlobals.clearSoftwareComponents();

				// Add software component to software component list
				TraceBuilderGlobals.addSoftwareComponent(componentId,
						componentName, mmpPathAsString);

				// Because there is only one component current component index
				// is 0
				TraceBuilderGlobals.setCurrentSoftwareComponentIndex(0);

				setPossibleTraceFolderNames(project, componentName,
						currentSoftwareComponentMMPPath);

				// Remove mmp file name from mmpPathAsString variable
				IPath projectLocation = project.getLocation();
				int lastIndexOfForwardSlashChar = mmpPathAsString
						.lastIndexOf(SourceConstants.FORWARD_SLASH_CHAR);

				if (lastIndexOfForwardSlashChar != -1) {
					mmpPathAsString = mmpPathAsString.substring(0,
							lastIndexOfForwardSlashChar);
				}

				// Append mmpPathAsString to Project path to get real
				// mmp file path
				mmpFilePath = projectLocation.append(mmpPathAsString)
						.toString();

				previousFileIncludedMoreThanOnce = false;
			}
			projectPath = findProjectRootAndSetTracesFolderName(
					currentSoftwareComponentMMPPath, project, mmpFilePath);
		}
		return projectPath;
	}

	/**
	 * Find project root and set traces folder name
	 * 
	 * @param mmpFilePath
	 *            MPP file path
	 * @return Project root
	 */
	private String findProjectRootAndSetTracesFolderName(IPath mmpPath,
			IProject project, String mmpFilePath) {
		String projectPath;
		projectPath = findProjectRoot(mmpFilePath);

		// Store previous trace folder name
		String previousTraceFolderName = ProjectEngine.traceFolderName;
		openProjectTraceFolderName = findTraceFolderName(mmpPath, project,
				projectPath);

		// Check has trace folder name changed
		traceFolderChanged = false;
		if (previousTraceFolderName != null
				&& !previousTraceFolderName
						.equalsIgnoreCase(openProjectTraceFolderName)) {
			traceFolderChanged = true;
		}
		return projectPath;
	}

	/**
	 * Set possible trace folder names
	 * 
	 * @param carbideProject
	 *            Current Carbide.c++ project
	 * @param componentName
	 *            Current component name
	 * @param relativeMMPPath
	 *            Relative path to current MMP file
	 */
	private void setPossibleTraceFolderNames(IProject carbideProject,
			String componentName, IPath relativeMMPPath) {
		String targetName = null;
		String targetExt = null;
		tracesComponentNameFolder = TRACES_DIRECTORY + UNDERSCORE
				+ componentName;

		// Append relative MMP path to the project location to get location of
		// the MMP file
		IPath mmpPath = carbideProject.getLocation().append(relativeMMPPath);

		ICarbideBuildManager buildMgr = CarbideBuilderPlugin.getBuildManager();
		ICarbideProjectInfo cpi = buildMgr.getProjectInfo(carbideProject);

		// Get the default build configuration
		ICarbideBuildConfiguration defultConfig = cpi.getDefaultConfiguration();
		IPath pathForExcutable = EpocEngineHelper.getHostPathForExecutable(
				defultConfig, mmpPath);
		if (pathForExcutable != null) {
			String fileName = pathForExcutable.lastSegment();
			int lastIndexOfDot = fileName.lastIndexOf("."); //$NON-NLS-1$
			targetName = fileName.substring(0, lastIndexOfDot);
			targetExt = fileName.substring(lastIndexOfDot + 1);
		}

		Object data = EpocEnginePlugin.runWithMMPData(mmpPath,
				new DefaultMMPViewConfiguration(carbideProject, defultConfig,
						new AcceptedNodesViewFilter()),
				new MMPDataRunnableAdapter() {
					public Object run(IMMPData mmpData) {
						// The real return value, getting a single argument
						// setting
						return mmpData.getSingleArgumentSettings().get(
								EMMPStatement.TARGETTYPE);
					}
				});

		String targetType = (String) data;

		if (targetName != null && targetType != null) {
			tracesTargetNameTargetTypeFolder = TRACES_DIRECTORY + UNDERSCORE
					+ targetName + UNDERSCORE + targetType;
		} else {
			tracesTargetNameTargetTypeFolder = null;
		}

		if (targetName != null && targetExt != null) {
			tracesDirTargetNameTargetExtFolder = TRACES_DIRECTORY
					+ File.separator + targetName + UNDERSCORE + targetExt;
		} else {
			tracesDirTargetNameTargetExtFolder = null;
		}

		setSBSv2TraceFolderName(carbideProject);
	}

	/**
	 * Get component id from MMP file
	 * 
	 * @param mmpPath
	 *            the mmp file path
	 * @param project
	 *            the project
	 * @return the component id as string
	 */
	private String getComponentIdFromMMPFile(IPath mmpPath, IProject project) {
		String uid = null;
		if (mmpPath != null) {
			ICarbideBuildConfiguration buildConfig = getBuildConfiguration(project);
			if (buildConfig != null) {
				Object data = EpocEnginePlugin.runWithMMPData(project
						.getLocation().append(mmpPath),
						new DefaultMMPViewConfiguration(buildConfig
								.getCarbideProject().getProject(), buildConfig,
								new AcceptedNodesViewFilter()),
						new MMPDataRunnableAdapter() {
							public Object run(IMMPData mmpData) {

								// Try first get value of UID3
								String uid = mmpData.getUid3();

								// If value of UID3 is null then
								// try get value of UID2
								if (uid == null) {
									uid = mmpData.getUid2();
								}

								// return value could be null if
								// UID2 is not defined
								return uid; // CodForChk_Dis_Exits
							}
						});
				uid = (String) data;
			}
		}
		return uid;
	}

	/**
	 * Get trace folder name from MMP file
	 * 
	 * @param mmpPath
	 *            the mmp file path
	 * @param project
	 *            the project
	 * @return the trace folder name
	 */
	private String getTraceFolderFromMMPFile(IPath mmpPath, IProject project) {
		String traceFolder = null;
		if (mmpPath != null) {
			ICarbideBuildConfiguration buildConfig = getBuildConfiguration(project);
			if (buildConfig != null) {
				Object data = EpocEnginePlugin.runWithMMPData(project
						.getLocation().append(mmpPath),
						new DefaultMMPViewConfiguration(buildConfig
								.getCarbideProject().getProject(), buildConfig,
								new AcceptedNodesViewFilter()),
						new MMPDataRunnableAdapter() {
							public Object run(IMMPData mmpData) {

								String traceFolder = null;

								// Get the list of user include
								// paths
								List<IPath> userIncludes = mmpData
										.getUserIncludes();

								// Go through existing userincludes
								// to check if the path to add is
								// already present
								for (int i = 0; i < userIncludes.size(); i++) {
									String userIncludeAsOSString = userIncludes
											.get(i).toOSString();
									if (userIncludeAsOSString
											.equalsIgnoreCase(TRACES_DIRECTORY)
											|| userIncludeAsOSString
													.equalsIgnoreCase(tracesComponentNameFolder)
											|| userIncludeAsOSString
													.equalsIgnoreCase(tracesTargetNameTargetTypeFolder)
											|| userIncludeAsOSString
													.equalsIgnoreCase(tracesDirTargetNameTargetExtFolder)) {
										traceFolder = userIncludeAsOSString;
										break;
									}
								}
								return traceFolder; // CodForChk_Dis_Exits
							}
						});
				traceFolder = (String) data;
			}
		}
		return traceFolder;
	}

	/**
	 * Get component name from MMP file name
	 * 
	 * @param mmpPathAsString
	 *            the mmp file path as string
	 * @return the component name
	 */
	private String getComponentNameFromMMPFileName(String mmpPathAsString) {
		String mmpFileName = null;
		if (mmpPathAsString != null) {
			int lastIndexOfForwardSlashChar = mmpPathAsString
					.lastIndexOf(SourceConstants.FORWARD_SLASH_CHAR);
			int lastIndexOfPeriod = mmpPathAsString.lastIndexOf(FileUtils.MMP);
			mmpFileName = mmpPathAsString.substring(
					lastIndexOfForwardSlashChar + 1, lastIndexOfPeriod);
		}

		return mmpFileName;
	}

	/**
	 * Get project path to non source file
	 * 
	 * @param project
	 *            project
	 * @param path
	 *            path where to start find project path
	 * @return the project path
	 */
	private String getProjetcPathToNonSourceFile(IProject project, IPath path) {
		String projectPath = null;

		// We can not parse software components, because file is not source file
		// and it is not listed in MMP files. So just clear software components
		// list.
		TraceBuilderGlobals.clearSoftwareComponents();

		// Check that is file part of open project
		if (project.equals(openProjectObject) && openProjectPath != null) {

			// File part of open project, so use same project path
			projectPath = openProjectPath;

			// Because project path is same, also trace folder is same
			traceFolderChanged = false;
		} else {

			// File not part of open project, so find project path
			projectPath = findProjectRootAndSetTracesFolderName(path, project,
					path.toOSString());
		}
		return projectPath;
	}

	/**
	 * Find files project root
	 * 
	 * @param path
	 *            path where to start find project root
	 * @return the project root
	 */
	private String findProjectRoot(String path) {
		boolean found = false;
		String root = path;
		File file = new File(root);
		while (!found && file != null) {
			if (isProjectRoot(file)) {
				root = file.getPath();
				found = true;
			} else {
				file = file.getParentFile();
			}
		}
		return root;
	}

	/**
	 * Find trace folder name
	 * 
	 * @param mmpFilePath
	 *            MMP file path
	 * @param projectRoot
	 *            project root
	 * @return the trace folder name
	 */
	private String findTraceFolderName(IPath mmpPath, IProject project,
			String projectRoot) {
		String traceFolderName = null;

		traceFolderName = getTraceFolderFromMMPFile(mmpPath, project);

		if (traceFolderName == null) {
			traceFolderName = findTraceFolderNameFromFileSystem(projectRoot);
		}
		return traceFolderName;
	}

	/**
	 * Find trace folder name from file system
	 * 
	 * @param projectRoot
	 *            project root
	 * @return the trace folder name
	 */
	private String findTraceFolderNameFromFileSystem(String projectRoot) {
		String traceFolderName = null;
		File file = new File(projectRoot);

		File[] children = file.listFiles();

		// Check that does subdirectory that name is one of the possible trace
		// folder name exist in this directory. If it exist use that as traces
		// directory name.
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			String childName = child.getName();
			if (child.isDirectory()) {
				if (childName
						.equalsIgnoreCase(tracesDirTargetNameTargetExtFolder)) {
					traceFolderName = tracesDirTargetNameTargetExtFolder;
					break;
				} else if (childName
						.equalsIgnoreCase(tracesTargetNameTargetTypeFolder)) {
					traceFolderName = tracesTargetNameTargetTypeFolder;
					break;
				} else if (childName
						.equalsIgnoreCase(tracesComponentNameFolder)) {
					traceFolderName = tracesComponentNameFolder;
					break;
				} else if (childName.equalsIgnoreCase(TRACES_DIRECTORY)) {
					traceFolderName = TRACES_DIRECTORY;
					break;
				}
			}
		}

		// If trace folder name is "traces" we need to check that does it
		// include folder "<target name>_<target ext>". If it includes then
		// that folder need to be as traces folder name
		if (traceFolderName != null && traceFolderName.equals(TRACES_DIRECTORY) && tracesDirTargetNameTargetExtFolder != null) {
			file = new File(projectRoot + File.separator + TRACES_DIRECTORY);
			children = file.listFiles();
			int startIndex = tracesDirTargetNameTargetExtFolder
					.lastIndexOf(File.separator);
			String targetNameTargetExtFolder = tracesDirTargetNameTargetExtFolder
					.substring(startIndex + 1);
			for (int i = 0; i < children.length; i++) {
				File child = children[i];
				String childName = child.getName();
				if (child.isDirectory()) {
					if (childName.equalsIgnoreCase(targetNameTargetExtFolder)) {
						traceFolderName = tracesDirTargetNameTargetExtFolder;
						break;
					}
				}
			}
		}

		// If trace folder is still null set default value based on used SBS
		// version
		if (traceFolderName == null) {
			if (sbsv2TraceFolderName != null) {
				traceFolderName = sbsv2TraceFolderName;
			} else {
				traceFolderName = tracesComponentNameFolder;
			}
		}
		return traceFolderName;
	}

	/**
	 * Set SBSv2 trace folder name
	 * 
	 * @param project
	 *            Carbide project
	 */
	private void setSBSv2TraceFolderName(IProject project) {
		ICarbideBuildManager buildMgr = CarbideBuilderPlugin.getBuildManager();

		boolean isSBSv2Project = buildMgr.isCarbideSBSv2Project(project);

		if (isSBSv2Project) {
			ISDKManager sdkMgr = SDKCorePlugin.getSDKManager();
			Version sbsv2Version = sdkMgr.getSBSv2Version(true);
			int major = sbsv2Version.getMajor();
			int minor = sbsv2Version.getMinor();
			int micro = sbsv2Version.getMicro();

			if ((major < 2) || (major == 2 && minor < 10)
					|| (major == 2 && minor == 10 && micro == 0)) {
				sbsv2TraceFolderName = tracesComponentNameFolder;
			} else if (major == 2) {
				if ((major == 2 && minor < 14)) {
					sbsv2TraceFolderName = tracesTargetNameTargetTypeFolder;
				} else {
					sbsv2TraceFolderName = tracesDirTargetNameTargetExtFolder;
				}
			} else {
				sbsv2TraceFolderName = tracesDirTargetNameTargetExtFolder;
			}
		} else {
			sbsv2TraceFolderName = null;
		}
	}

	/**
	 * Checks if this folder is the project root
	 * 
	 * @param parentFolder
	 *            parent folder
	 * @return true if this folder is the project root
	 */
	private boolean isProjectRoot(File mmpFile) {
		// Try to find traces, group or mmpfiles folder from this folder
		boolean isProjectRoot = false;

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String name) {
				boolean retval = false;

				if (file.isDirectory()) {
					if (name
							.equalsIgnoreCase(tracesDirTargetNameTargetExtFolder)
							|| name
									.equalsIgnoreCase(tracesTargetNameTargetTypeFolder)
							|| name.equalsIgnoreCase(tracesComponentNameFolder)
							|| name.equalsIgnoreCase(TRACES_DIRECTORY)
							|| name.equalsIgnoreCase(GROUP_DIRECTORY)
							|| name.equalsIgnoreCase(MMPFILES_DIRECTORY)) {
						retval = true;
					}
				}
				return retval; // CodForChk_Dis_Exits
			}
		};

		String[] children = mmpFile.list(filter);

		if (children != null && children.length != 0) {
			isProjectRoot = true;
		}

		return isProjectRoot; // CodForChk_Dis_Exits
	}

	/**
	 * Saves Epoc root to Trace Builder Configuration
	 * 
	 * @param project
	 *            the project
	 */
	private void saveEpocRoot(IProject project) {
		IPath epocRoot = WorkbenchUtils.getEpocRootForProject(project);

		if (epocRoot != null) {
			TraceBuilderGlobals.getConfiguration().setText(
					TraceBuilderConfiguration.ENVIRONMENT_ROOT,
					epocRoot.toOSString());
		} else {
			TraceBuilderGlobals.getConfiguration().setText(
					TraceBuilderConfiguration.ENVIRONMENT_ROOT, ""); //$NON-NLS-1$
		}
	}

	/**
	 * Checks that the given file belongs to active project
	 * 
	 * @param file
	 *            the file
	 * @return true if valid, false if not
	 */
	boolean isFileActive(IFile file) {
		boolean isActive = false;
		IProject project = file.getProject();
		if (project.equals(openProjectObject) == true) {

			// Check that project is Carbide project
			boolean isCarbideProject = CarbideBuilderPlugin.getBuildManager()
					.isCarbideProject(project);
			if (isCarbideProject) {

				// If file is source file then we need to check that trace
				// folcer name is same than open project trace folder name
				if (isSourceFile(file)) {
					IPath path = file.getLocation();
					List<IPath> mmpPaths = EpocEngineHelper.getMMPsForSource(
							project, path);
					if (mmpPaths.size() > 0) {
						for (int i = 0; i < mmpPaths.size(); i++) {
							IPath mmpPath = mmpPaths.get(i);
							String mmpPathAsString = mmpPath.toString();
							String componentName = getComponentNameFromMMPFileName(mmpPathAsString);
							String currentComponentName = TraceBuilderGlobals
									.getCurrentSoftwareComponentName();
							if (componentName != null
									&& componentName
											.equalsIgnoreCase(currentComponentName)) {
								isActive = true;
								break;
							}
						}
					}
				} else {
					isActive = true;
				}
			}
		}
		return isActive;
	}

	/**
	 * Check that is file source file
	 * 
	 * @param file
	 *            file that need to be check
	 * @return true if file is source file, false if not
	 */
	private boolean isSourceFile(IFile file) {
		boolean retVal = false;

		String extension = file.getFileExtension();

		if (extension != null
				&& (extension.equalsIgnoreCase(FileUtils.CPP_EXTENSION) || extension
						.equalsIgnoreCase(FileUtils.C_EXTENSION))) {
			retVal = true;
		}
		return retVal;
	}

	/**
	 * Source saved
	 * 
	 * @param source
	 *            the source
	 */
	void sourceSaved(JFaceDocumentWrapper source) {
		if (!mmpFileModified && openProjectObject != null
				&& TraceBuilderGlobals.getTraceModel().hasTraces()) {
			String fileName = source.getFileName();
			String filepath = source.getFilePath();
			Path path = new Path(filepath + fileName);

			// Add traces folder include path to MMP file
			addTracesFolderInclude(path);

			// Add SYSTEMINCLUDE to MMP file if needed
			addOpenSystemTraceSystemInclude();

			mmpFileModified = true;
		}
	}

	/**
	 * Add layer SYSTEMINCLUDE macro to MMP file if needed
	 * 
	 */
	private void addOpenSystemTraceSystemInclude() {

		if (CarbideBuilderPlugin.getBuildManager().isCarbideProject(
				openProjectObject)
				&& openProjectObject.isAccessible()) {
			ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
					.getProjectInfo(openProjectObject);
			if (cpi != null) {

				// loop through all build configurations since the list of mmp's
				// could be different depending on the build configuration
				for (ICarbideBuildConfiguration config : cpi
						.getBuildConfigurations()) {

					// loop through each mmp file and add the include path if
					// necessary
					for (final IPath mmpPath : EpocEngineHelper
							.getMMPFilesForBuildConfiguration(config)) {
						final IPath epocroot = WorkbenchUtils
								.getEpocRootForProject(openProjectObject);
						String mmpFilePath = epocroot.toOSString()
								+ mmpPath.toOSString();
						try {
							// Open the file mmp file
							FileInputStream fistream = new FileInputStream(
									mmpFilePath);

							// Get the object of DataInputStream
							DataInputStream in = new DataInputStream(fistream);
							BufferedReader br = new BufferedReader(
									new InputStreamReader(in));

							// Read File Line By Line
							String strLine;
							int numberOfLastIncludeLine = 0;
							boolean headerExist = false;
							int headerEnd = -1;
							int line = 0;
							boolean platformPathsIncludeFound = false;
							boolean layerSystemIncludeMacroFound = false;
							Vector<String> mmpFileContent = new Vector<String>();
							while ((strLine = br.readLine()) != null) {
								mmpFileContent.add(strLine);
								line++;
								strLine = strLine.toUpperCase();

								// Check does MMP file start with file header
								if (line == 1) {
									if (strLine
											.indexOf(MULTILINE_COMMENT_START) != -1) {
										headerExist = true;
									} else if (strLine.indexOf(COMMENT) != -1) {
										headerExist = true;
										headerEnd = line;
									}
								}

								// Get header end, if MMP file starts with file
								// header that is made by using multiline
								// comment
								if (strLine.indexOf(MULTILINE_COMMENT_END) != -1
										&& headerExist && headerEnd == -1) {
									headerEnd = line;
								}

								// Get header end, if MMP file starts with file
								// header that is made by using single
								// comments
								if (strLine.indexOf(COMMENT) != -1
										&& headerExist && headerEnd == line - 1) {
									headerEnd = line;
								}

								// Get line of last include statement
								if (strLine.indexOf(INCLUDE) != -1) {
									numberOfLastIncludeLine = line;
								}

								// Check does platform_patsh.hrh include already
								// exist
								if (strLine.indexOf(PLATFORM_PATHS_HRH) != -1) {
									platformPathsIncludeFound = true;
								}

								// Check does layer system include macro already
								// exist
								if (strLine
										.indexOf(APP_LAYER_SYSTEMINCLUDE_MACRO) != -1
										|| strLine
												.indexOf(MW_LAYER_SYSTEMINCLUDE_MACRO) != -1
										|| strLine
												.indexOf(OS_LAYER_SYSTEMINCLUDE_MACRO) != -1) {
									layerSystemIncludeMacroFound = true;
									break;
								}
							}

							// If includes did not exist, but file header exist,
							// then set number of last include line same as
							// header end.
							if (headerExist
									&& numberOfLastIncludeLine < headerEnd) {
								numberOfLastIncludeLine = headerEnd;
							}

							// Close the buffered reader
							br.close();

							// Close the input stream
							in.close();

							// Close file input stream
							fistream.close();

							if (!layerSystemIncludeMacroFound) {
								String layerSystemIncludeMacro = geLayerSystemIncludeMacro(mmpPath
										.toOSString());
								FileOutputStream fostream = new FileOutputStream(
										mmpFilePath);
								BufferedWriter bw = new BufferedWriter(
										new OutputStreamWriter(fostream));

								for (line = 0; line < mmpFileContent.size(); line++) {
									if (line == numberOfLastIncludeLine) {
										if (!platformPathsIncludeFound) {
											bw
													.write(INCLUDE_PLATFORM_PATHS_HRH
															+ SourceConstants.LINE_FEED);
										}
										bw.write(SourceConstants.LINE_FEED
												+ layerSystemIncludeMacro
												+ SourceConstants.LINE_FEED);
									}
									bw.write(mmpFileContent.get(line)
											+ SourceConstants.LINE_FEED);
								}

								// Close the buffered writer
								bw.close();

								// Close file output stream
								fostream.close();
							}
						} catch (Exception e) {// Catch exception if any
							CarbideBuilderPlugin.log(e);
						}
					}
				}
			}
		}
	}

	/**
	 * Get correct layer system include macro
	 * 
	 * @param string
	 *            MMP path as OS string
	 * 
	 * @return correct layer system include macro
	 */
	private String geLayerSystemIncludeMacro(String mmpPathAsOsString) {
		String layerSystemIncludeMacro = APP_LAYER_SYSTEMINCLUDE_MACRO;
		String firstDirectory = mmpPathAsOsString.substring(0,
				mmpPathAsOsString.indexOf(SourceConstants.BACKSLASH));

		if (firstDirectory.equalsIgnoreCase(SF_FOLDER_NAME)) {
			String secondDirectory = mmpPathAsOsString.substring(firstDirectory
					.length() + 1, mmpPathAsOsString.indexOf(
					SourceConstants.BACKSLASH, firstDirectory.length() + 1));
			if (secondDirectory.equalsIgnoreCase(MW_FOLDER_NAME)) {
				layerSystemIncludeMacro = MW_LAYER_SYSTEMINCLUDE_MACRO;
			} else if (secondDirectory.equalsIgnoreCase(OS_FOLDER_NAME)) {
				layerSystemIncludeMacro = OS_LAYER_SYSTEMINCLUDE_MACRO;
			}
		} else if (firstDirectory.equalsIgnoreCase(NCP_SW_FOLDER_NAME)) {
			layerSystemIncludeMacro = OS_LAYER_SYSTEMINCLUDE_MACRO;
		}

		return layerSystemIncludeMacro;
	}

	/**
	 * Adds traces folder include to MMP files
	 * 
	 * @param path
	 *            file path
	 */
	private void addTracesFolderInclude(Path filePath) {
		IPath mainProjectPath = openProjectObject.getLocation();
		IPath traceProjectPath = new Path(openProjectPath);

		// Calculate and remove matching segments
		int matchingSegs = mainProjectPath
				.matchingFirstSegments(traceProjectPath);

		IPath projectRelativePath = traceProjectPath
				.removeFirstSegments(matchingSegs);

		// Add Traces directory and remove device ID
		projectRelativePath = projectRelativePath
				.append(ProjectEngine.traceFolderName);
		projectRelativePath = projectRelativePath.setDevice(null);

		// Add include path to the project MMP files
		addUserIncludeToMmpFiles(filePath, projectRelativePath);
	}

	/**
	 * Get Carbide project info
	 * 
	 * @return Carbide project info
	 */
	private ICarbideProjectInfo getCarbideProjectInfo() {
		ICarbideProjectInfo cpi = null;
		if (CarbideBuilderPlugin.getBuildManager().isCarbideProject(
				openProjectObject)
				&& openProjectObject.isAccessible()) {
			cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(
					openProjectObject);
		}
		return cpi;
	}

	/**
	 * Add user include to MMP files, those uses specific source file
	 * 
	 * @param filePath
	 *            file path of the saved source file
	 * @param projectRelativeIncDirPath
	 *            project relative path to the include directory
	 */
	private void addUserIncludeToMmpFiles(final IPath filePath,
			final IPath projectRelativeIncDirPath) {
		if (openProjectObject != null && projectRelativeIncDirPath != null) {

			final IPath epocroot = WorkbenchUtils
					.getEpocRootForProject(openProjectObject);

			ICarbideProjectInfo cpi = getCarbideProjectInfo();

			// Loop through all build configurations since the list of
			// mmp's could be different depending on the build
			// configuration
			for (ICarbideBuildConfiguration config : cpi
					.getBuildConfigurations()) {

				// Get name of the MMP files where source file is used
				final List<IPath> mmpPaths = EpocEngineHelper.getMMPsForSource(
						openProjectObject, filePath);
				ArrayList<String> sourceMmpFileNames = new ArrayList<String>();
				for (int i = 0; i < mmpPaths.size(); i++) {
					sourceMmpFileNames.add(mmpPaths.get(i).lastSegment());
				}

				// Go through build configuration's MMP files
				for (final IPath mmpPath : EpocEngineHelper
						.getMMPFilesForBuildConfiguration(config)) {

					String mmpFileName = mmpPath.lastSegment();

					if (sourceMmpFileNames.contains(mmpFileName)) {

						EpocEnginePlugin.runWithMMPView(mmpPath,
								new DefaultMMPViewConfiguration(config,
										new AcceptedNodesViewFilter()),
								new MMPViewRunnableAdapter() {

									public Object run(IMMPView view) {
										MMPViewPathHelper helper = new MMPViewPathHelper(
												view, epocroot);
										try {

											// Convert the project relative path
											// to an mmp view path
											IPath incPath = helper
													.convertProjectOrFullPathToMMP(
															EMMPPathContext.USERINCLUDE, // CodForChk_Dis_LengthyLine
															projectRelativeIncDirPath); // CodForChk_Dis_LengthyLine

											// Get the list of user include
											// paths and add it if it's not
											// already there
											List<IPath> userIncludes = view
													.getUserIncludes();

											String incPathAsOSString = incPath
													.toOSString();

											boolean includeExist = false;

											// Go through existing userincludes
											// to check if the path to add is
											// already present
											for (int i = 0; i < userIncludes
													.size(); i++) {
												String pathAsOSString = userIncludes
														.get(i).toOSString();
												pathAsOSString = pathAsOSString
														.toLowerCase();
												if (pathAsOSString
														.equalsIgnoreCase(incPathAsOSString)) {
													includeExist = true;
													break;
												}
											}

											if (!includeExist) {
												userIncludes.add(incPath);
												WorkbenchUtils
														.postUserIncludeMsg(mmpPath);

												// Now commit the changes and
												// release the file
												while (true) {
													try {
														view.commit();
														break;
													} catch (IllegalStateException e) {
														CarbideBuilderPlugin
																.log(e);
														if (!view.merge()) {
															view.revert();
														}
													}
												}
											}

										} catch (InvalidDriveInMMPPathException e) {

											// Shouldn't get here; we passed in
											// a project-relative path
											CarbideBuilderPlugin.log(e);
										}

										return null;
									}
								});
					}
				}
			}
		}
	}
}