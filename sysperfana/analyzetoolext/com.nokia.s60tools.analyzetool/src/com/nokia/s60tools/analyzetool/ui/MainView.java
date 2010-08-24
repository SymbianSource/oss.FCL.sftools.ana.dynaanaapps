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
 * Description:  Definitions for the class MainView
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import java.io.FileInputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.navigator.ResourceNavigator;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.internal.project.ui.views.SymbianProjectNavigatorView;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.AnalyzeToolHelpContextIDs;
import com.nokia.s60tools.analyzetool.builder.BuilderUtil;
import com.nokia.s60tools.analyzetool.engine.AnalysisItem;
import com.nokia.s60tools.analyzetool.engine.AnalyzeFactory;
import com.nokia.s60tools.analyzetool.engine.CallstackItem;
import com.nokia.s60tools.analyzetool.engine.DeferredCallstackManager;
import com.nokia.s60tools.analyzetool.engine.EpocReader;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel;
import com.nokia.s60tools.analyzetool.engine.MMPInfo;
import com.nokia.s60tools.analyzetool.engine.ParseAnalyzeData;
import com.nokia.s60tools.analyzetool.engine.ParseXMLFileSAX;
import com.nokia.s60tools.analyzetool.engine.ProjectResults;
import com.nokia.s60tools.analyzetool.engine.RunResults;
import com.nokia.s60tools.analyzetool.engine.SimpleCallstackManager;
import com.nokia.s60tools.analyzetool.engine.UseAtool;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ReadFile;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.internal.ui.graph.ChartContainer;
import com.nokia.s60tools.analyzetool.ui.actions.DropDownMenu;
import com.nokia.s60tools.analyzetool.ui.actions.FileActionHistory;
import com.nokia.s60tools.analyzetool.ui.statistic.StatisticView;

/**
 * AnalyzeTool main view which displays memory analysis results and provides
 * interface for all the functionalities what AnalyzeTool has.
 * 
 * @author kihe
 * 
 */
public class MainView extends ViewPart implements ISelectionListener,
		ITreeViewerListener, IActionListener, ISelectionChangedListener,
		KeyListener {

	/**
	 * Sorts tree view objects.
	 * 
	 * @author kihe
	 */
	public static class NameSorter extends ViewerSorter {

		/**
		 * Compares view items.
		 * 
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 * 
		 * @param viewer
		 *            Viewer
		 * @param e1
		 *            Object
		 * @param e2
		 *            Object
		 * @return int Always return 0
		 */
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			return 0;
		}
	}

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements ITreeContentProvider {

		/**
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// MethodDeclaration/Block[count(BlockStatement) = 0 and
			// @containsComment = 'false']
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
		 * java.lang.Object)
		 */
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null) {
					getStartupContent();
				}
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
		 * .Object)
		 */
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
		 * .Object)
		 */
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).hasChildren();
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// MethodDeclaration/Block[count(BlockStatement) = 0 and
			// @containsComment = 'false']
		}

	}

	/**
	 * Provides elements of tree view.
	 * 
	 * @author kihe
	 * 
	 */
	public class ViewLabelProvider extends LabelProvider {

		/**
		 * Used when tree model item is TreeParent.
		 */
		private final Image folder;

		/**
		 * Default element only used to show error situations.
		 */
		private final Image element;

		/**
		 * Used when tree model item is part of the module which is build with
		 * AnalyzeTool.
		 */
		private final Image build;

		/**
		 * Used when tree model item is part of the project but not build with
		 * with AnalyzeTool.
		 */
		private final Image notBuild;

		/**
		 * Used when tree model item is outside of the project modules.
		 */
		public Image outside;

		/**
		 * Constructor. Created all the images which is used in the AnalyzeTool
		 * tree model.
		 */
		public ViewLabelProvider() {
			// create images
			folder = PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FOLDER);
			element = PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
			build = Activator.getImageDescriptor(Constants.ICON_BUILD)
					.createImage();
			notBuild = Activator.getImageDescriptor(Constants.ICON_NOT_BUILD)
					.createImage();
			outside = Activator.getImageDescriptor(Constants.ICON_OUTSIDE)
					.createImage();
		}

		/**
		 * Gets current tree object image.
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 * 
		 * @param obj
		 *            Current tree model item
		 * @return Corresponding image of tree view object
		 */
		@Override
		public Image getImage(final Object obj) {

			// if object is TreeParent return "folder" icon
			if (obj instanceof TreeParent) {
				return folder;
			}

			// if object is TreeObject, need to change icon to match object
			// state
			else if (obj instanceof TreeObject) {
				// get TreeObject
				TreeObject tempObject = (TreeObject) obj;

				// change object icon if module belongs to selected project
				if (tempObject.isBelongs()) {
					// if module is built with AnalyzeTool
					if (tempObject.isBuild()) {
						return build;
					}
					// module not build with AnalyzeTool
					return notBuild;
				}
				// module not belong to selected project
				return outside;
			}
			return element;
		}

		/**
		 * Gets current tree view object name.
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 * 
		 * @return Current tree view object name
		 */
		@Override
		public final String getText(final Object obj) {
			return obj.toString();
		}
	}

	/** Is trace actions enabled. */
	public static boolean enableTrace;

	/** Title of the AnalyzeTool view */
	private String viewTitle = Constants.ANALYZE_TOOL_TITLE;

	/** Line feed character. */
	private static String lineFeed = "\n";

	/** Contains information of test runs. */
	public TreeViewer runView;

	/** Contains one memory leak call stack addresses. */
	private TreeViewer callstackView;

	/** Tree parent object which not shown to user. */
	private TreeParent invisibleRoot;

	/** Double click action. */
	private Action doubleClickAction;

	/** Click action. */
	private Action clickAction;

	/** Change detail level action. */
	public Action changeDetails;

	/** Select S60 log file action. */
	private Action s60LogTargetAction;

	/** Select TraceViewer connection action. */
	// private Action externalLogTargetAction;

	/** Select fast data gathering mode */
	private Action externalFastLogTargetAction;

	/** Select Ask always action. */
	private Action askLogTargetAction;

	/** Save report file action. */
	private Action saveReportAction;

	/** Save test run action. */
	private Action saveDataFileAction;

	/** AnalyzeTool results action. */
	private Action analyzeResults;

	/** Activate AnalyzeTool build action. */
	public Action buildWithAtool;

	/** Clean AnalyzeTool changes action. */
	private Action cleanAtoolChanges;

	/** Start/Stop trace action. */
	public Action traceAction;

	/** Start subtest action. */
	private Action startSubtest;

	/** Stop subtest action. */
	private Action stopSubtest;

	/** Refresh(re-creates) project results */
	private Action refreshResults;

	/** Copies selected memory leak item info to the clipboard. */
	private Action copyAction;

	/** Action to open AnalyzeTool preference page. */
	private Action openPrefs;

	/**
	 * Clears selected project results without removing temporary files
	 */
	private Action clearProjectResults;

	/** Selected project reference. */
	public IProject project;

	/** Previously select project reference. */
	public IProject lastProjectRef;

	/** Project reference for active trace. */
	private IProject traceStartedProjectRef;

	/** Contains detailed information of selected run or memory leak. */
	public Label informationLabel;

	/** Memory analysis parser. */
	public ParseAnalyzeData parser;

	/** Used data file. */
	public String usedDataFileName = null;

	/** Is trace active. */
	private boolean traceActive = false;

	/** Last active memory leak tree item. */
	private TreeObject activeTreeItem;

	/** Contains workbench (all project) related cpp file info. */
	private final AbstractList<String> cppFileNames;

	/** Contains started subtest information. */
	private final AbstractList<ActiveSubtests> startedSubtest;

	/** Contains project related module results. */
	private final ProjectResults projectResults;

	/** Job for analyzing data files. */
	private Job analyzeJob;

	/** Job for reading the data files for the graph. */
	private GraphLoadJob graphLoadJob;

	/** Last selected tree item. */
	private Object lastSelectedObject;

	/** Contains information of which files were opened. */
	public FileActionHistory fileOpenHistory;

	/** File open drop down menu. */
	private DropDownMenu fileOpenMenu;

	/** Log target action drop down menu. */
	private DropDownMenu logTargetMenu;

	/** Save file drop down menu. */
	private DropDownMenu saveFileMenu;

	/** Tab item for the "Top allocation locations" tab */
	CTabItem memoryTab;

	/** Tab item for the memory results tab */
	CTabItem mainTab;

	/** StatisticView reference */
	StatisticView statisticView;

	/** Contains project related modules */
	private final Hashtable<IProject, AbstractList<MMPInfo>> projectModules;

	/** Reads epocwind.out file */
	EpocReader listeningJob;

	/** The chart view composite */
	protected ChartContainer chart;

	/**
	 * The constructor.
	 */
	public MainView() {
		parser = new ParseAnalyzeData(true, false, true);
		cppFileNames = new ArrayList<String>();
		startedSubtest = new ArrayList<ActiveSubtests>();
		projectResults = new ProjectResults();
		fileOpenHistory = new FileActionHistory(Constants.HISTORY_LEVEL);
		projectModules = new Hashtable<IProject, AbstractList<MMPInfo>>();
	}

	/**
	 * Activates AnalyzeTool custom build if it is not activated.
	 */
	public final void activateAnalysisBuild() {
		// check project validity
		if (checkProjectValidity()) {
			// add AnalyzeTool custom builder natures to project build nature
			BuilderUtil bUtil = new BuilderUtil();
			if (bUtil.isNatureEnabled(project)) {
				bUtil.disableNatures(project);
			} else {
				bUtil.enableNatures(project);
			}
		}
		// update build state
		updateBuildState(project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.s60tools.analyzetool.ui.IActionListener#allModulesBuilt()
	 */
	public final void buildStateChanged(final IProject projRef) {

		// check validity
		if (!project.equals(projRef) || projectResults == null) {
			return;
		}

		final String datafile = projectResults.getDataFileName(projRef);

		// if trace is captured or data file is opened
		if (datafile != null && runView != null) {

			// need to check is data file available in the disk
			FileInputStream fis = null;
			try {
				// get existing file stream
				fis = new FileInputStream(datafile);

				// if file is empty => do nothing
				if (fis.available() == 0) {
					return;
				}
			} catch (java.io.FileNotFoundException fnfe) {
				fnfe.printStackTrace();
				return;
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
				return;
			} finally { // finally close input stream
				try {
					if (fis != null) {
						fis.close();
						fis = null;
					}
				} catch (java.io.IOException ioe) {
					ioe.printStackTrace();
				}
			}

			// data file is available
			int dataFileType = UseAtool.checkFileType(datafile);

			if (dataFileType == Constants.DATAFILE_INVALID
					|| dataFileType == Constants.DATAFILE_XML
					|| dataFileType == Constants.DATAFILE_EMPTY) {
				return;
			}
			boolean reGenerate = Util
					.openConfirmationDialog(Constants.BUILD_STATE_CHANGED);

			if (reGenerate) {
				// sync with UI thread
				runView.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						analyzeDataFile(Constants.ANALYZE_USE_DATA_FILE,
								datafile, false);
					}
				});
			}
		}
	}

	/**
	 * Opens file dialog and analyzing data file for given location.
	 * 
	 * @param type
	 *            Type to define is data file asked from the user or using the
	 *            give data file
	 * @param existingDataFile
	 *            Data file path
	 * @param showErrorInfo
	 *            Flag to determinate that displaying error info or not
	 */
	public final void analyzeDataFile(final int type,
			final String existingDataFile, final boolean showErrorInfo) {

		// is project selected
		if (!checkProjectValidity()) {
			return;
		}

		// user selected file
		final String selectedFile;

		// ask for user data file
		if (type == Constants.ANALYZE_ASK_FOR_USER) {
			selectedFile = Util.openFileDialog(getSite().getShell(),
					Constants.DIALOG_SELECT_DATA_FILE, project.getLocation()
							.toOSString());
		} else if (existingDataFile == null) {
			selectedFile = parser.getDataFileName();
		} else {
			selectedFile = existingDataFile;
		}

		// if user select some file
		if (selectedFile != null) {

			// clear previous data
			projectResults.clearProjectData(project);
			activeTreeItem = null;
			clearCallstackViewContent();
			updateInformationLabel("");
			runView.setInput(getStartupContent());
			changeViewTitle(viewTitle);

			AbstractList<MMPInfo> modules = Util
					.loadProjectTargetsInfo(project);
			projectModules.put(project, modules);

			boolean xmlFile = Util.isFileXML(selectedFile);
			// if file is XML file
			// no need to analyze data file
			// => just create results from XML file
			if (xmlFile) {
				Job analyzingXMLJob = new Job(Constants.ANALYZE_TOOL_TITLE) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {

						// update progress monitor state
						monitor.beginTask(Constants.PROGRESSDIALOG_TITLE,
								IProgressMonitor.UNKNOWN);
						// Parse the data file
						ParseXMLFileSAX dataFileParser = new ParseXMLFileSAX(
								project, selectedFile, projectResults);
						boolean ret = dataFileParser.parse();

						// set used datafile name
						usedDataFileName = selectedFile;

						fileOpenHistory.setFileName(selectedFile);

						// if parsing success
						// display memory leak results
						if (ret) {
							// update project results
							projectResults.setProjectModules(project,
									projectModules.get(project), dataFileParser
											.getModules());
						} else {
							fileOpenHistory.removeFileName(selectedFile);
							if (showErrorInfo) {
								showErrorMessage(Constants.INFO_FILE_INVALID);
							}
						}
						updateChangeDetailState(project);
						refreshView();
						dataFileParser = null;
						return new Status(IStatus.OK,
								Constants.ANALYZE_CONSOLE_ID, IStatus.OK,
								Constants.PROGRESSDIALOG_ANALYZE_COMPLETE, null);
					}
				};
				analyzingXMLJob.setUser(true);
				analyzingXMLJob.schedule();

			} else {
				try {
					analyzeWithAtool(project, selectedFile, showErrorInfo);
				} catch (Exception e) {
					analyzeJob = null;
				}
			}
		}
	}

	/**
	 * Analyzing memory analysis results using atool.exe.
	 * 
	 * @param projectRef
	 *            Project reference
	 * @param usedFile
	 *            Data file which contains memory analyze data
	 * @param showErrorInfo
	 *            Flag to determinate that displaying error info or not
	 */
	public final void analyzeWithAtool(final IProject projectRef,
			final String usedFile, final boolean showErrorInfo) {
		// check that no existing job running
		if (analyzeJob == null
				|| analyzeJob.getResult().getCode() == IStatus.CANCEL
				|| analyzeJob.getResult().getCode() == IStatus.ERROR) {
			analyzeJob = new Job(Constants.ANALYZE_TOOL_TITLE) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					// inform progress dialog that task execution starts
					// this make progress dialog visible on the UI
					monitor.beginTask(Constants.PROGRESSDIALOG_ATOOL,
							IProgressMonitor.UNKNOWN);

					fileOpenHistory.setFileName(usedFile);
					// add2UserActionHistory( "File opened: " + usedFile );

					// set used datafile name
					usedDataFileName = usedFile;

					if (chart != null) {
						resetGraphView();// clear out the graph view
					}

					// create atool object and execute atool
					UseAtool atool = new UseAtool();

					// create XML file
					Constants.COMMAND_LINE_ERROR_CODE errorCode = atool
							.createXmlAndCleanDatFilesToCarbide(monitor,
									projectRef, usedFile, "-a");

					String xmlFileLocation = atool.getXmlFilePath();
					String cleanDatFileLocation = atool.getCleanDatFilePath();

					// if some error occurs display it to user.
					if (errorCode != Constants.COMMAND_LINE_ERROR_CODE.OK) {
						fileOpenHistory.removeFileName(usedFile);
						Util.displayCommandLineError(errorCode);
					}

					// if XML file generation failed => info to the user
					else if (xmlFileLocation == null) {
						fileOpenHistory.removeFileName(usedFile);
						if (showErrorInfo) {
							showErrorMessage(Constants.INFO_FILE_INVALID);
						}
					} else {
						// Parse the XML file
						ParseXMLFileSAX dataFileParser = new ParseXMLFileSAX(
								project, xmlFileLocation, projectResults);
						boolean error = dataFileParser.parse();
						if (showErrorInfo && !error) {
							fileOpenHistory.removeFileName(usedFile);
							showErrorMessage(Constants.INFO_FILE_INVALID);
						}

						projectResults.setProjectModules(project,
								projectModules.get(project), dataFileParser
										.getModules());
						projectResults.setDataFileName(projectRef,
								usedDataFileName);
						// update display
						refreshView();
					}

					updateChangeDetailState(projectRef);

					if (!monitor.isCanceled()) {
						// create the job for loading the graph model
						// but only schedule it when the user gets to the graph
						// tab
						graphLoadJob = new GraphLoadJob(cleanDatFileLocation);
						graphLoadJob.setUser(true);// set progress bar
						graphLoadJob.setPriority(Job.LONG);

						// run the following in UI thread, since widgets get
						// accessed
						PlatformUI.getWorkbench().getDisplay().syncExec(
								new Runnable() {
									public void run() {
										if (((CTabFolder) chart.getParent())
												.getSelection() != null
												&& ((CTabFolder) chart
														.getParent())
														.getSelection()
														.getControl() == chart) {
											// chart tab is currently selected
											// so we can run the load job
											// straight away
											graphLoadJob.schedule();
										}
									}
								});
					}
					analyzeJob = null;
					return new Status(IStatus.OK, Constants.ANALYZE_CONSOLE_ID,
							IStatus.OK,
							Constants.PROGRESSDIALOG_ANALYZE_COMPLETE, null);
				}
			};

			if (graphLoadJob != null && graphLoadJob.getState() == Job.RUNNING) {
				graphLoadJob.cancel();
			}
			graphLoadJob = null;

			analyzeJob.setUser(true);
			analyzeJob.setPriority(Job.LONG);
			analyzeJob.schedule();

		} else {
			// if existing job is running display info to user
			showMessage(Constants.INFO_ALLREADY_RUNNING);
		}
	}

	/**
	 * Change report detail level.
	 */
	public final void changeDetailLevel() {

		// sync with UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// get preference store
				IPreferenceStore store = Activator.getPreferences();

				// get active report level
				String reportLevel = store.getString(Constants.REPORT_LEVEL);

				boolean updateMemLeakAlso = false;

				// set new report level
				if (Constants.REPORT_EVERY.equals(reportLevel)) {
					store.setValue(Constants.REPORT_LEVEL,
							Constants.REPORT_KNOWN);
					updateMemLeakAlso = true;
				} else if (Constants.REPORT_KNOWN.equals(reportLevel)) {
					store.setValue(Constants.REPORT_LEVEL,
							Constants.REPORT_TOPMOST);
				} else if (Constants.REPORT_TOPMOST.equals(reportLevel)) {
					store.setValue(Constants.REPORT_LEVEL,
							Constants.REPORT_EVERY);
					updateMemLeakAlso = true;
				}

				if (updateMemLeakAlso && runView != null) {
					activeTreeItem = null;

					// set view content
					runView.setInput(getResults(false));

					// refresh view
					runView.refresh();

					// if last selected item not found current item list
					if (activeTreeItem == null) {
						runView.setAutoExpandLevel(2);
					} else {
						// set selection to correct item
						runView.setSelection(new StructuredSelection(
								activeTreeItem), true);
					}
				}

				if (callstackView != null) {
					// update view callstack view content
					callstackView.setInput(getCallStack(activeTreeItem));

					// expand all the trees on call stack view
					callstackView.expandAll();

				}
				// change tooltip
				changeReportActionTooltip();
			}
		});
	}

	/**
	 * Change logging mode.
	 * 
	 * @param loggingMode
	 *            Used logging mode
	 */
	public final void changeLogTarget(final String loggingMode) {

		if (logTargetMenu == null) {
			return;
		}

		// get preference store
		IPreferenceStore store = Activator.getPreferences();
		String usedLoggingMode = "";

		// if no logging mode given get it from the AnalyzeTool preferences
		if (loggingMode == null) {
			usedLoggingMode = store.getString(Constants.LOGGING_MODE);
		}
		// logging mode is given => so start to use it
		else {
			store.setValue(Constants.LOGGING_MODE, loggingMode);
			usedLoggingMode = loggingMode;
		}

		if (Constants.LOGGING_S60.equals(usedLoggingMode)) {

			logTargetMenu.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_CELLURAR));
			logTargetMenu
					.setToolTipText(Constants.ACTION_CHANGE_LOGGING_MODE_TOOLTIP_S60);
			if (loggingMode == null) {
				s60LogTargetAction.setChecked(true);
				externalFastLogTargetAction.setChecked(false);
				askLogTargetAction.setChecked(false);
			}
		} else if (Constants.LOGGING_EXT_FAST.equals(usedLoggingMode)) {
			logTargetMenu.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_COMPUTER_FAST));
			logTargetMenu
					.setToolTipText(Constants.ACTION_CHANGE_LOGGING_MODE_TOOLTIP_FAST);
			if (loggingMode == null) {
				s60LogTargetAction.setChecked(false);
				externalFastLogTargetAction.setChecked(true);
				askLogTargetAction.setChecked(false);
			}
		}
		// current logging mode is "ask_always"
		else {
			logTargetMenu.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_ASK));
			logTargetMenu
					.setToolTipText(Constants.ACTION_CHANGE_LOGGING_MODE_TOOLTIP_ASK);
			if (loggingMode == null) {
				s60LogTargetAction.setChecked(false);
				externalFastLogTargetAction.setChecked(false);
				askLogTargetAction.setChecked(true);
			}
		}

		// if the fast data gathering mode is enabled by the preference page =>
		// enable also toolbar option
		// else disable fast data gathering mode
		externalFastLogTargetAction.setEnabled(store
				.getBoolean(Constants.LOGGING_FAST_ENABLED));
	}

	/**
	 * Changes "Change report detail level" action tooltip.
	 */
	public final void changeReportActionTooltip() {
		if (changeDetails == null) {
			return;
		}

		// get preference store
		IPreferenceStore store = Activator.getPreferences();

		// get active report level
		String reportLevel = store.getString(Constants.REPORT_LEVEL);

		// set new report level
		if (Constants.REPORT_EVERY.equals(reportLevel)) {
			changeDetails.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_DETAILS_ALL));
			changeDetails
					.setToolTipText(Constants.ACTION_CHANGE_REPORT_LEVEL_ALL);
		} else if (Constants.REPORT_KNOWN.equals(reportLevel)) {
			changeDetails.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_DETAILS_KNOWN));
			changeDetails
					.setToolTipText(Constants.ACTION_CHANGE_REPORT_LEVEL_KNOWN);
		} else if (Constants.REPORT_TOPMOST.equals(reportLevel)) {
			changeDetails.setImageDescriptor(Activator
					.getImageDescriptor(Constants.BUTTON_DETAILS_TOPMOST));
			changeDetails
					.setToolTipText(Constants.ACTION_CHANGE_REPORT_LEVEL_TOPMOST);
		}
	}

	/**
	 * Change view title.
	 * 
	 * @param newTitle
	 *            New title text
	 */
	private void changeViewTitle(final String newTitle) {

		// if newTitle contains text
		if (newTitle != null) {
			super.setContentDescription(newTitle);
		}
	}

	/**
	 * Check that selected project is open and project information can be read.
	 * 
	 * @return True if project is open and accessible otherwise False
	 */
	public final boolean checkProjectValidity() {
		// project is not selected show info to user
		if (project == null || !project.isOpen()) {
			Util.showMessage(Constants.NO_PROJ_SELECT);
			return false;
		}
		return true;
	}

	/**
	 * Cleans atool.exe made changes.
	 */
	public final void clean() {
		if (!checkProjectValidity()) {
			return;
		}

		// check is atool.exe available
		if (!Util.isAtoolAvailable()) {
			showErrorMessage(Constants.INFO_ATOOL_NOT_AVAILABLE);
			return;
		}

		// ask for user
		boolean ret = Util.openConfirmationDialog(Constants.CONFIRM_DELETE_ALL);

		// if user confirms
		if (ret) {
			// clear AnalyzeTool made changes
			Util.clearAtoolChanges(project);

			cleanAnalyzeData(null);
			resetGraphView();
			updateChangeDetailState(project);
		}

		if (statisticView != null) {
			statisticView.clean(null);
		}
	}

	/**
	 * Cleans all the saved data.
	 */
	private void cleanAnalyzeData(final IProject projectRef) {
		// clean all data if project not specified
		if (projectRef == null) {
			// clean all the project related info and data
			projectResults.clear();
			projectModules.clear();
		} else {
			// clear only one project results
			if (projectResults.contains(projectRef)) {
				projectResults.clearProjectData(projectRef);
			}

			if (projectModules.contains(projectRef)) {
				projectModules.remove(projectRef);
			}
		}

		cppFileNames.clear();

		// update variables
		activeTreeItem = null;
		usedDataFileName = "";

		// set default view contents
		if (runView != null) {
			runView.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					runView.setInput(getStartupContent());
					if (informationLabel != null) {
						updateInformationLabel("");
					}

					changeViewTitle(viewTitle);
					if (statisticView != null) {
						statisticView.clean(projectRef);
					}
				}
			});
		}

		clearCallstackViewContent();
		if (clearProjectResults != null && clearProjectResults != null) {
			clearProjectResults.setEnabled(false);
		} else if (clearProjectResults != null) {
			updateChangeDetailState(projectRef);
		}
	}

	/**
	 * Clears callstack view contents.
	 */
	private void clearCallstackViewContent() {
		if (runView == null) {
			return;
		}

		// if view exists
		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {

				if (callstackView != null) {
					callstackView.setInput(null);
				}
			}
		});
	}

	/**
	 * Contributes action bar.
	 */
	private void contributeToActionBars() {
		if (getViewSite() == null) {
			return;
		}
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {

		// create a new Tab
		final CTabFolder mainFolder = new CTabFolder(parent, SWT.TOP);

		// create main view and add it tab
		createMainView(mainFolder);

		// create graph
		createGraphView(mainFolder);

		// set initial selection
		mainFolder.setSelection(mainTab);

		mainFolder.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				// if we changed to the graph tab and the graph load job isn't
				// already running
				// schedule it now
				if (graphLoadJob != null
						&& graphLoadJob.getState() != Job.RUNNING
						&& mainFolder.getSelection() != null
						&& mainFolder.getSelection().getControl() == chart) {
					graphLoadJob.schedule();
				}

				if (mainFolder.getSelectionIndex() == 1) {
					changeDetails.setEnabled(false);
				} else {
					changeDetails.setEnabled(true);
				}
			}
		});

		// stop any jobs that may be scheduled
		mainFolder.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (graphLoadJob != null) {
					graphLoadJob.cancel();
					graphLoadJob = null;
				}
				if (analyzeJob != null) {
					analyzeJob.cancel();
					analyzeJob = null;
				}
			}
		});
	}

	/**
	 * Creates graph view and add it to graph tab
	 * 
	 * @param mainFolder
	 *            CTabFolder parent of the view
	 */
	private void createGraphView(CTabFolder mainFolder) {
		final CTabItem chartTabItem = new CTabItem(mainFolder, SWT.NONE);
		chartTabItem.setText("Graph");
		chartTabItem.setToolTipText("AnalyzeTool graph per process");

		chart = new ChartContainer(mainFolder, SWT.NONE);
		chartTabItem.setControl(chart);
		IMemoryActivityModel model = AnalyzeFactory.getEmptyModel();
		chart.setInput(project, model);
		model.addProcesses(model.getProcesses());// charts should get notified
		// via listeners on the
		// model
	}

	/**
	 * Clears the graph by setting an empty model and causing paint events
	 */
	private void resetGraphView() {
		if (chart != null) {
			chart.setInput(project, AnalyzeFactory.getEmptyModel());
		}
	}

	/**
	 * Creates new statistic view and add it to memory tab
	 * 
	 * @param parent
	 *            Statistic view parent( CTabFolder )
	 */
	public void createMemoryView(CTabFolder parent) {
		statisticView = new StatisticView();
		memoryTab = statisticView.createView(parent);
	}

	/**
	 * Creates memory results view
	 * 
	 * @param parent
	 *            View parent ( CTabFolder )
	 */
	public void createMainView(CTabFolder parent) {

		// Create SashForm this form includes all the current view components
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		mainTab = new CTabItem(parent, SWT.NONE);
		mainTab.setControl(sashForm);
		mainTab.setText(Constants.MAIN_TAB_TITLE);

		// create new treeviewer to shown memory analysis runs and leaks
		runView = new TreeViewer(sashForm, SWT.VIRTUAL);

		// create SashForm to display call stack addresses and more detailed
		// information
		// of selected run or leak
		SashForm callstackForm = new SashForm(sashForm, SWT.VERTICAL);

		// set content and label providers
		runView.setContentProvider(new ViewContentProvider());
		runView.setLabelProvider(new ViewLabelProvider());

		// get init content
		runView.setInput(getStartupContent());

		// add listener to provide selection change events
		runView.addTreeListener(this);

		runView.setAutoExpandLevel(2);

		// create new information label
		// this label contains more detailed information of selected item
		informationLabel = new Label(callstackForm, SWT.BORDER | SWT.CENTER);

		// create grid data => this provides layout changes
		GridData data = new GridData();

		// add grid data to label, this enables label ui modifications e.g. line
		// feed
		informationLabel.setLayoutData(data);

		// set init text
		informationLabel.setText(Constants.INFO_NO_DATA);

		// create new call stack view
		// this components contains information of one memory leak call stack
		// addresses
		callstackView = new TreeViewer(callstackForm, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);

		// modify UI components layouts
		// reserve more space for left side of UI
		sashForm.setWeights(new int[] { 5, 3 });
		callstackForm.setWeights(new int[] { 2, 8 });

		// add content and label providers
		callstackView.setContentProvider(new ViewContentProvider());
		callstackView.setLabelProvider(new ViewLabelProvider());

		// make actions and construct click listeners
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookClicks();
		contributeToActionBars();

		// set view title
		viewTitle = String.format(Constants.ANALYZE_TOOL_TITLE_WITH_VERSION,
				Util.getAToolFeatureVersionNumber());
		this.setContentDescription(viewTitle);

		// add selection listener
		if (getSite() != null) {
			getSite().getPage().addSelectionListener(this);
			runView.getControl().addKeyListener(this);
		}

		// set actionlistener
		Activator.setActionListener(this);

		// set help shortcuts
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				callstackView.getControl(),
				AnalyzeToolHelpContextIDs.ANALYZE_MAIN);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(runView.getControl(),
				AnalyzeToolHelpContextIDs.ANALYZE_MAIN);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				new ATResourceListener());

		IPreferenceStore store = Activator.getPreferences();
		store.setValue(Constants.LOGGING_FAST_ENABLED, true);

		// get default value for logging mode
		preferenceChanged();
	}

	/**
	 * When AnalyzeTool view is activated and TraceViewer plug-in is not
	 * available disable AnalyzeTool trace actions.
	 * 
	 * @see com.nokia.s60tools.analyzetool.ui.IActionListener#disableTraceActions(boolean)
	 * 
	 * @param disable
	 *            Boolean state of trace action
	 */
	public void disableTraceActions(final boolean disable) {
		if (traceAction != null && disable) {
			// enable trace action
			traceAction.setToolTipText(Constants.ACTION_START_TRACE);
			traceAction.setEnabled(disable);
		} else if (traceAction != null && !disable) {
			// disable trace action
			traceAction.setToolTipText(Constants.TRACE_NOT_FOUND);
			traceAction.setEnabled(disable);
		}
		traceActive = false;
	}

	/**
	 * Fills context menu.
	 * 
	 * @param manager
	 *            Menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(buildWithAtool);
		manager.add(new Separator());
		manager.add(traceAction);
		manager.add(startSubtest);
		manager.add(stopSubtest);
		manager.add(new Separator());
		manager.add(changeDetails);
		manager.add(refreshResults);
		manager.add(clearProjectResults);
		manager.add(new Separator());
		manager.add(cleanAtoolChanges);
		manager.add(copyAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Fills local pull down menu.
	 * 
	 * @param manager
	 *            Menu manager
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(buildWithAtool);
		manager.add(new Separator());
		manager.add(traceAction);
		manager.add(startSubtest);
		manager.add(stopSubtest);
		manager.add(new Separator());
		manager.add(changeDetails);
		manager.add(refreshResults);
		manager.add(new Separator());
		manager.add(cleanAtoolChanges);
		manager.add(new Separator());
		manager.add(openPrefs);
	}

	/**
	 * Fills local toolbar.
	 * 
	 * @param manager
	 *            Menu manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {

		logTargetMenu = new DropDownMenu(Constants.ACTION_SAVE, this, false,
				false);
		makeLogTargetActions();
		logTargetMenu.addAction(externalFastLogTargetAction);
		logTargetMenu.addAction(s60LogTargetAction);
		logTargetMenu.addAction(askLogTargetAction);
		manager.add(logTargetMenu);

		manager.add(buildWithAtool);
		manager.add(new Separator());
		manager.add(traceAction);
		manager.add(startSubtest);
		manager.add(stopSubtest);
		manager.add(new Separator());
		manager.add(changeDetails);
		manager.add(new Separator());
		fileOpenMenu = new DropDownMenu(Constants.ACTION_OPEN, this, false,
				true);
		makeFileOpenActions();
		manager.add(fileOpenMenu);

		// get drop down menu for save action
		saveFileMenu = new DropDownMenu(Constants.ACTION_SAVE, this, true,
				false);
		saveFileMenu.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_SAVE));

		saveFileMenu.addAction(saveReportAction);
		saveFileMenu.addAction(saveDataFileAction);
		manager.add(saveFileMenu);
		manager.add(new Separator());
		manager.add(cleanAtoolChanges);
		manager.add(new Separator());
		manager.add(openPrefs);
	}

	/**
	 * Gets call stack information of current leak.
	 * 
	 * @param treeObject
	 *            Tree object
	 * @return Object
	 */
	Object getCallStack(final TreeObject treeObject) {
		IPreferenceStore store = Activator.getPreferences();
		// if project is not selected or treeobject is not created
		// => leave
		if (project == null || treeObject == null) {
			return null;
		}

		// set active tree item
		activeTreeItem = treeObject;

		AnalysisItem item = null;
		if (treeObject.isSubTest()) {
			item = projectResults.getSubtestItem(project,
					treeObject.getRunID(), treeObject.getMemLeakID(),
					treeObject.getSubtestID());
		}
		// get AnalysisItem
		else {
			item = projectResults.getSpecific(project, treeObject.getRunID(),
					treeObject.getMemLeakID());
		}

		if (item == null) {
			return null;
		}

		if (store == null) {
			return null;
		}
		AbstractList<MMPInfo> modules = projectModules.get(project);
		TreeHelper helper = new TreeHelper(null, store);

		TreeParent parent = helper.getCallstack(item, modules);

		return parent;
	}

	/**
	 * Gets callstack item name.
	 * 
	 * @param callstackItem
	 *            One callstack item
	 * @return Callstack name if found otherwise null
	 */
	public final String getCallstackItemName(final CallstackItem callstackItem) {
		// if call stack item not set => return
		if (callstackItem == null) {
			return null;
		}

		String cppFileName = "";
		String fileName = callstackItem.getFileName().toLowerCase(Locale.US);

		if (fileName.indexOf(".cpp") == -1) {
			return null;
		}

		// check that project contains cpp file which is parsed from call stack
		// list
		Iterator<String> iterCppFiles = cppFileNames.iterator();
		while (iterCppFiles.hasNext()) {
			String cppFileLocation = iterCppFiles.next();

			// parse file name from the source path
			int slash = Util.getLastSlashIndex(cppFileLocation);
			if (slash != -1) {
				String cppFile = cppFileLocation.substring(slash + 1,
						cppFileLocation.length());
				if (cppFile.equalsIgnoreCase(fileName)) {
					cppFileName = cppFileLocation;
					break;
				}
			}
		}
		return cppFileName;
	}

	/**
	 * Gets file info for current project.
	 * 
	 * @param projectRef
	 *            Project reference
	 */
	private void getFileInfos(final IProject projectRef) {
		ResourceVisitor visitor = new ResourceVisitor(this);

		// go thru all open projects
		// this enables to display one memory address corresponding source code
		// line
		// for outside of active project
		try {
			if (projectRef == null) {
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
						.getRoot();
				IProject[] projects = myWorkspaceRoot.getProjects();
				for (int i = 0; i < projects.length; i++) {
					IProject tempProject = projects[i];
					if (tempProject.isOpen()) {
						tempProject.accept(visitor);
					}
				}
			} else { // project is selected
				// if project is open => accept resource visitor
				if (projectRef.isOpen()) {
					projectRef.accept(visitor);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets project results.
	 * 
	 * @param projectRef
	 *            Project reference
	 */
	public void getProjectResults(final IProject projectRef) {
		// if project reference is null => leave
		if (projectRef == null) {
			return;
		}
		// if project is selected and it is open
		else if (projectRef.isOpen()) {
			// if this is the first time to get project results
			if (lastProjectRef == null) {
				lastProjectRef = projectRef;
				updateBuildState(lastProjectRef);
				updateChangeDetailState(lastProjectRef);
			}
			// project selection changed
			// need to get selected project results and mmp file info
			else if (!lastProjectRef.equals(projectRef)) {
				activeTreeItem = null;
				lastProjectRef = projectRef;

				runView.setInput(getResults(false));
				callstackView.setInput(getCallStack(null));

				updateInformationLabel("");
				updateBuildState(lastProjectRef);
				updateChangeDetailState(lastProjectRef);
			}
		}

		// update clear project results action state
		if (projectResults.contains(projectRef)) {
			clearProjectResults.setEnabled(true);
		} else {
			clearProjectResults.setEnabled(false);
		}
	}

	/**
	 * Gets memory leak analysis results.
	 * 
	 * @param showErrorInfo
	 *            Display error info or not
	 * @return Object memory leak analysis results
	 */
	private Object getResults(final boolean showErrorInfo) {

		try {
			// create a new tree parent
			TreeParent testRuns = new TreeParent(Constants.TEST_RUNS_TREE_TITLE);
			invisibleRoot = null;
			invisibleRoot = new TreeParent(Constants.TREE_TITLE);
			invisibleRoot.addChild(testRuns);

			// if current project does not contain results => just update
			// default view values
			if (!projectResults.contains(project)) {
				changeViewTitle(viewTitle);
				updateInformationLabel(Constants.INFO_NO_DATA);
				return getStartupContent();
			}

			if (!projectModules.containsKey(project)) {
				AbstractList<MMPInfo> modules = Util
						.loadProjectTargetsInfo(project);
				projectModules.put(project, modules);
			}

			// get run results
			AbstractList<RunResults> runs = projectResults.getResults(project);
			AbstractList<MMPInfo> modules = projectModules.get(project);

			// if no results available => show default view content
			if (runs == null || runs.isEmpty()) {
				changeViewTitle(viewTitle);
				updateInformationLabel(Constants.INFO_NO_DATA);

				// because no results created => delete project empty results
				projectResults.clearProjectData(project);

				fileOpenHistory.removeFileName(projectResults
						.getDataFileName(project));

				// display info to user
				if (showErrorInfo) {
					showErrorMessage(Constants.INFO_FILE_INVALID);
				}
				return getStartupContent();
			}

			IPreferenceStore store = Activator.getPreferences();

			// create TreeHelper object
			// TreeHelper class creates tree model to this view.
			TreeHelper helper = new TreeHelper(lastSelectedObject, store);

			// clear active item
			activeTreeItem = null;

			// update used data file name
			usedDataFileName = projectResults.getDataFileName(project);

			// change view title
			changeViewTitle(viewTitle + " results from file: "
					+ usedDataFileName);

			// thru runs
			Iterator<RunResults> runIterator = runs.iterator();
			while (runIterator.hasNext()) {
				// get one run info
				RunResults oneRunResults = runIterator.next();

				// creates one run information at the time
				TreeParent oneRunTree = helper.createRunResults(oneRunResults,
						modules);

				// get active item
				// active must ask from the TreeHelper class
				// because it can be filtered out when creating new tree model
				activeTreeItem = helper.getActiveItem();
				testRuns.addChild(oneRunTree);
			}
		} catch (java.lang.NullPointerException npe) {
			npe.printStackTrace();
			return null;
		}
		// expand to runs level
		runView.setAutoExpandLevel(2);

		return invisibleRoot;
	}

	/**
	 * Sets startup contents for view.
	 * 
	 * @return Object which can be displayd
	 */
	public TreeParent getStartupContent() {
		// create default treeviewer content
		invisibleRoot = new TreeParent(Constants.ANALYZE_TOOL_TITLE);
		TreeParent child = new TreeParent(Constants.INFO_NO_DATA_FILE_AVAILABLE);
		invisibleRoot.addChild(child);

		return invisibleRoot;
	}

	/**
	 * Adds selection changed listener to view.
	 */
	private void hookClicks() {
		runView.addSelectionChangedListener(this);
	}

	/**
	 * Hooks context menu.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@SuppressWarnings("synthetic-access")
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(runView.getControl());
		runView.getControl().setMenu(menu);
		if (getSite() != null) {
			getSite().registerContextMenu(menuMgr, runView);
		}
	}

	/**
	 * Hook double click actions.
	 */
	private void hookDoubleClickAction() {
		callstackView.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {

				doubleClickAction.run();
			}
		});
	}

	/**
	 * Check that if subtest by given name already exists.
	 * 
	 * @param subTestName
	 *            Subtest name
	 * @param target
	 *            Testing target
	 * @return True if already exists otherwise False
	 */
	public final boolean isSubtestExists(final String subTestName,
			final String target) {
		Iterator<ActiveSubtests> subTestIter = startedSubtest.iterator();
		while (subTestIter.hasNext()) {
			ActiveSubtests oneSubtest = subTestIter.next();
			if (oneSubtest.getName().equals(subTestName)
					&& oneSubtest.getTargetName().equals(target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Go thru the project files and stores mmp files.
	 * 
	 * @param resource
	 *            One resource file of project
	 */
	public final void loadFileInfo(IResource resource) {
		// get all the cpp file info which are imported to workspace
		// this enable pinpointing the code line for outside active project cpp
		// file than
		// the memory analysis is made
		// get cpp file info
		String cppFileName = Util.getCPPFileNameAndPath(resource);

		// if cpp file found, save it
		if (cppFileName != null && !cppFileNames.contains(cppFileName)) {
			this.cppFileNames.add(cppFileName);
		}
	}

	/**
	 * Construct all the actions.
	 */
	private void makeActions() {

		// listens click actions
		clickAction = new Action() {
			@Override
			public void run() {
				// get selection
				ISelection selection = runView.getSelection();

				// get selection object
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				// if object exists
				if (obj == null) {
					copyAction.setEnabled(false);
					return;
				}

				// get call stack addresses to view
				if (obj instanceof TreeObject) {
					lastSelectedObject = obj;

					// get callstack items
					Object resultObject = getCallStack((TreeObject) obj);

					// if results not found
					if (resultObject == null) {
						copyAction.setEnabled(false);
					} else {
						callstackView.setInput(resultObject);
						copyAction.setEnabled(true);
					}
				}

				// expand all the trees on call stack view
				callstackView.expandAll();

				setTextToInformationLabel();
			}
		};

		// listens double click actions
		doubleClickAction = new Action() {
			@Override
			public void run() {
				// get selection
				ISelection selection = callstackView.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				// open file in editor
				if (obj instanceof TreeObject) {
					openEditor((TreeObject) obj);
				}
			}
		};

		// data capture
		traceAction = new Action() {
			@Override
			public void run() {

				// if trace is active => stop the trace
				if (traceActive) {
					stop(true);
				}
				// if trace is not active => start the trace
				else {
					start();
				}
			}
		};
		traceAction.setText(Constants.ACTION_START_TRACE);
		traceAction.setToolTipText(Constants.ACTION_START_TRACE);
		traceAction.setImageDescriptor(Activator
				.getImageDescriptor((Constants.BUTTON_RUN)));

		// if trace actions are disabled
		if (!enableTrace) {
			traceAction.setEnabled(false);
			traceAction.setToolTipText(Constants.TRACE_NOT_FOUND);
		}

		// build with atool
		buildWithAtool = new Action() {
			@Override
			public void run() {
				activateAnalysisBuild();
			}
		};
		buildWithAtool.setText(Constants.ACTION_AT_BUILD_ACTIVE);
		buildWithAtool.setToolTipText(Constants.ACTION_AT_BUILD_ACTIVE);
		buildWithAtool.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_BUILD));

		// clean atool changes
		cleanAtoolChanges = new Action() {
			@Override
			public void run() {
				clean();
			}
		};
		cleanAtoolChanges.setText(Constants.ACTION_CLEAR_CHANGES);
		cleanAtoolChanges
				.setToolTipText(Constants.ACTION_CLEAR_CHANGES_TOOLTIP);
		cleanAtoolChanges.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_CLEAN));

		// change detail level
		changeDetails = new Action() {
			@Override
			public void run() {
				changeDetailLevel();
			}
		};
		changeDetails.setText(Constants.ACTION_CHANGE_REPORT_LEVEL);

		// save report( xml ) file
		saveReportAction = new Action() {
			@Override
			public void run() {
				saveReportFile(Constants.SAVE_REPORT_FILE_XML);
			}
		};
		saveReportAction.setText(Constants.ACTION_SAVE_REPORT);
		saveReportAction.setToolTipText(Constants.ACTION_SAVE_REPORT);

		// save data file
		saveDataFileAction = new Action() {
			@Override
			public void run() {
				saveReportFile(Constants.SAVE_REPORT_FILE_DATA);

			}
		};
		saveDataFileAction.setText(Constants.ACTION_SAVE_DATA);
		saveDataFileAction.setToolTipText(Constants.ACTION_SAVE_DATA);

		// start subtest
		startSubtest = new Action() {
			@Override
			public void run() {
				startSubTest();
			}
		};
		startSubtest.setText(Constants.ACTION_START_SUBTEST);
		startSubtest.setToolTipText(Constants.ACTION_START_SUBTEST);
		startSubtest.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_START_SUBTEST));

		// stop subtest
		stopSubtest = new Action() {
			@Override
			public void run() {
				stopSubTest();
			}
		};
		stopSubtest.setText(Constants.ACTION_STOP_SUBTEST);
		stopSubtest.setToolTipText(Constants.ACTION_STOP_SUBTEST);
		stopSubtest.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_STOP_SUBTEST));
		stopSubtest.setDescription(Constants.ACTION_STOP_SUBTEST);

		// set start and stop subtest not visible at the beginning
		startSubtest.setEnabled(false);
		stopSubtest.setEnabled(false);

		analyzeResults = new Action() {
			@Override
			public void run() {
				analyzeDataFile(Constants.ANALYZE_ASK_FOR_USER, null, true);
			}
		};

		analyzeResults.setText(Constants.ACTION_OPEN);
		analyzeResults.setToolTipText(Constants.ACTION_OPEN);
		analyzeResults.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_OPEN));

		clearProjectResults = new Action() {
			@Override
			public void run() {
				cleanAnalyzeData(project);
				if (statisticView != null) {
					statisticView.clean(project);
				}
				updateChangeDetailState(project);
			}
		};
		clearProjectResults.setText(Constants.ACTION_CLEAR_RESULTS);
		clearProjectResults.setToolTipText(Constants.ACTION_CLEAR_RESULTS);

		refreshResults = new Action() {
			@Override
			public void run() {
				if (project != null && project.isOpen()
						&& projectResults != null) {
					String dataFile = projectResults.getDataFileName(project);
					if (dataFile != null || !("").equals(dataFile)) {
						analyzeDataFile(Constants.ANALYZE_USE_DATA_FILE,
								dataFile, true);
					} else {
						// some internal error occurred => disable this action
						refreshResults.setEnabled(false);
					}

				}
			}
		};
		refreshResults.setText(Constants.ACTION_RE_ANALYZE);
		refreshResults.setToolTipText(Constants.ACTION_RE_ANALYZE_TOOLTIP);
		refreshResults.setEnabled(false);

		// copy active item contents to clipboard
		copyAction = new Action() {
			@Override
			public void run() {
				// copy active item contents to clipboard

				// Create new clipboard object
				Clipboard cp = new Clipboard(runView.getControl().getDisplay());

				// Create new TextTransfer object
				// TextTransfer converts plain text represented as a java String
				// to a platform specific representation of the data and vice
				// versa
				TextTransfer tt = TextTransfer.getInstance();

				// new StringBuffer which contains the copied text
				StringBuffer sb = new StringBuffer(64);

				// chech that project contains results
				if (projectResults == null || !projectResults.contains(project)
						|| activeTreeItem == null) {
					return;
				}
				// get active item info (also callstack info)
				AnalysisItem item = null;

				// if selected item is subtest
				if (activeTreeItem.isSubTest()) {
					item = projectResults.getSubtestItem(project,
							activeTreeItem.getRunID(), activeTreeItem
									.getMemLeakID(), activeTreeItem
									.getSubtestID());
				} else {
					item = projectResults.getSpecific(project, activeTreeItem
							.getRunID(), activeTreeItem.getMemLeakID());
				}

				// check that item found
				if (item == null) {
					return;
				}

				sb.append(activeTreeItem.getName());
				String separator = System.getProperty("line.separator");
				sb.append(separator);
				char space = ' ';
				AbstractList<CallstackItem> callstackItems = item
						.getCallstackItems();
				Iterator<CallstackItem> iterCallstack = callstackItems
						.iterator();
				while (iterCallstack.hasNext()) {
					CallstackItem oneItem = iterCallstack.next();
					sb.append("      ");
					sb.append(oneItem.getMemoryAddress());
					sb.append(space);
					sb.append(oneItem.getModuleName());
					sb.append(space);
					sb.append(oneItem.getFunctionName());
					sb.append(space);
					sb.append(oneItem.getFileName());
					sb.append(space);
					int lineNbr = oneItem.getLeakLineNumber();
					if (lineNbr > 0) {
						sb.append(lineNbr);
					}
					sb.append(separator);
				}

				// info is ready => now copy info to clipboard
				cp.setContents(new Object[] { sb.toString() },
						new Transfer[] { tt });
			}
		};
		copyAction.setText(Constants.ACTION_COPY);
		copyAction.setEnabled(false);

		// open preferences action
		openPrefs = new Action() {
			@Override
			public void run() {
				PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								Constants.ANALYZE_TOOL_PREFS_ID, null, null);

				if (dialog != null) {
					dialog.open();
				}
			}
		};
		openPrefs.setText(Constants.ACTION_OPEN_PREFS);
		openPrefs.setToolTipText(Constants.ACTION_OPEN_PREFS_TOOLTIP);
		openPrefs.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_OPEN_PREFS));

		changeReportActionTooltip();
		updateChangeDetailState(project);
		updateBuildState(project);
	}

	/**
	 * Creates file open actions.
	 */
	private void makeFileOpenActions() {
		fileOpenMenu.setText(Constants.ACTION_OPEN);
		fileOpenMenu.setToolTipText(Constants.ACTION_OPEN);
		fileOpenMenu.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_OPEN));
	}

	/**
	 * Creates data gathering actions.
	 */
	private void makeLogTargetActions() {

		s60LogTargetAction = new Action(Constants.LOGGING_S60,
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				changeLogTarget(Constants.LOGGING_S60);
			}
		};
		s60LogTargetAction.setText(Constants.PREFS_S60);
		s60LogTargetAction
				.setToolTipText(Constants.ACTION_CHANGE_LOGGING_MODE_TOOLTIP_S60);
		s60LogTargetAction.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_CELLURAR));

		externalFastLogTargetAction = new Action(Constants.LOGGING_EXT_FAST,
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				changeLogTarget(Constants.LOGGING_EXT_FAST);
			}
		};
		externalFastLogTargetAction.setText(Constants.PREFS_EXT_FAST);
		externalFastLogTargetAction
				.setToolTipText(Constants.PREFS_EXT_FAST_TOOLTIP);
		externalFastLogTargetAction.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_COMPUTER_FAST));

		askLogTargetAction = new Action(Constants.LOGGING_ASK_ALLWAYS,
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				changeLogTarget(Constants.LOGGING_ASK_ALLWAYS);
			}
		};
		askLogTargetAction.setText(Constants.PREFS_ASK_ALWAYS);
		askLogTargetAction
				.setToolTipText(Constants.ACTION_CHANGE_LOGGING_MODE_TOOLTIP_ASK);
		askLogTargetAction.setImageDescriptor(Activator
				.getImageDescriptor(Constants.BUTTON_ASK));
	}

	/**
	 * Displays selected callstack item location on default file editor.
	 * 
	 * @param treeObject
	 *            Tree object
	 */
	public final void openEditor(final TreeObject treeObject) {
		// get file info for all projects
		getFileInfos(project);

		try {

			// if no project selected
			if (project == null || !project.isOpen()) {
				Util.showMessage(Constants.NO_PROJ_SELECT);
				return;
			}

			// get selected call stack item
			CallstackItem callstackItem = treeObject.getCallstackItem();
			if (callstackItem == null) {
				return;
			}

			String cppFileName = getCallstackItemName(callstackItem);

			if (cppFileName == null || ("").equals(cppFileName)) {
				cppFileName = callstackItem.getFileName();
			}

			// if leak number is invalid => leave
			if (callstackItem.getLeakLineNumber() < 1) {
				return;
			}

			String line = Integer.toString(callstackItem.getLeakLineNumber());

			IFile file = null;
			if (project.isOpen()) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(
						new Path(project.getName() + "\\" + cppFileName));
			}

			// if file not found in active project
			// go thru all open projects in current workbench
			if (file == null || !file.exists()) {
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
						.getRoot();
				IProject[] projects = myWorkspaceRoot.getProjects();
				for (int i = 0; i < projects.length; i++) {
					file = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(
									new Path(projects[i].getName() + "\\"
											+ cppFileName));

					// file found => skip the rest of the projects
					if (file != null && file.exists()) {
						break;
					}
				}

			}

			// if file still not found
			// display info to user and leave
			if (file == null || !file.exists()) {
				Util.showMessage(Constants.SOURCE_NOT_FOUND);
				return;
			}

			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(IMarker.LINE_NUMBER, Integer.parseInt(line));
			map.put(IDE.EDITOR_ID_ATTR, Constants.SOURCE_FILE_EDITOR_ID);
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker);

		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates log target action and report action tooltips.
	 * 
	 * @see com.nokia.s60tools.analyzetool.ui.IActionListener#preferenceChanged()
	 */
	public void preferenceChanged() {
		// get active logging mode
		changeLogTarget(null);

		changeReportActionTooltip();
	}

	/**
	 * Refresh AnalyzeTool view.
	 */
	public final void refreshView() {
		// update display content
		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {

				updateInformationLabel("");

				// refresh view
				runView.setInput(getResults(false));

				// refresh callstack view
				getCallStack(activeTreeItem);
				if (project != null && project.isOpen()
						&& projectResults.contains(project)) {
					clearProjectResults.setEnabled(true);
				} else {
					clearProjectResults.setEnabled(false);
				}
				updateChangeDetailState(project);
			}
		});
	}

	/**
	 * Runs user selected AnalyzeTool action.
	 * 
	 * @see com.nokia.s60tools.analyzetool.ui.IActionListener#runAction(IProject,
	 *      com.nokia.s60tools.analyzetool.global.Constants.ACTIONS)
	 * 
	 * @param projectRef
	 *            Project reference
	 * 
	 * @param action
	 *            Which action to execute
	 */
	public void runAction(IProject projectRef, Constants.ACTIONS action) {
		project = projectRef;

		switch (action) {
		case RUN_VIEW_MEM_LEAKS:
			analyzeResults.run();
			break;

		case RUN_BUILD:
			buildWithAtool.run();
			break;

		case RUN_CLEAN:
			cleanAtoolChanges.run();
			break;

		default: // by design default statement is empty
			break;
		}
	}

	/**
	 * Copies existing report or data file to the new location. Asks from user
	 * new location where to copy the file.
	 * 
	 * @param type
	 *            Which kind of type the file is. Possible types XML or data
	 *            file
	 * 
	 * @return True if saving successfully otherwise false
	 */
	public final boolean saveReportFile(final int type) {
		// copy success?
		boolean success = false;

		// check if project is selected
		if (project == null) {
			showMessage(Constants.NO_PROJ_SELECT);
			return success;
		}

		// folder location
		String folder = null;

		// file path and name to use
		String usedFile = null;

		// save xml file
		if (type == Constants.SAVE_REPORT_FILE_XML) {
			String targetPath = Util.getBldInfFolder(project, true)
					+ Constants.FILENAME_CARBIDE;
			java.io.File file = new java.io.File(targetPath);
			if (file.exists()) {
				String[] names = new String[2];
				names[0] = "*.xml";
				names[1] = "*.*";

				Shell shell = null;

				// get shell from teh active view
				if (runView != null) {
					shell = runView.getControl().getShell();
				}

				// if for some reason shell is null => leave
				if (shell == null) {
					return success;
				}

				// ask user where to save xml report file
				folder = Util.fileSaveDialog(Constants.DIALOG_SAVE_REPORT,
						names, shell);
				usedFile = targetPath;
			} else {
				Util.showMessage(Constants.INFO_NO_RESULTS_FILE);
				return success;
			}
		}
		// save data file
		else {
			String dataFile = Util.isDataFileAvailable(project);
			// check is there data file which can be used
			if ((dataFile == null || dataFile.equals(""))
					&& (usedDataFileName == null || usedDataFileName.equals(""))) {
				Util.showMessage(Constants.INFO_NO_DATA_FILE);
				return success;
			}
			// if no existing data file opened
			else if (usedDataFileName == null || usedDataFileName.equals("")) {
				usedFile = dataFile;

			}
			// user is already opened data file => save it
			else {
				usedFile = usedDataFileName;
			}

			String[] names = new String[2];
			names[0] = "*.dat";
			names[1] = "*.*";

			Shell shell = null;

			// get shell from the active view
			if (runView != null) {
				shell = runView.getControl().getShell();
			}

			// if for some reason shell is null => leave
			if (shell == null) {
				return success;
			}

			// ask user where to save XML report file
			folder = Util.fileSaveDialog(Constants.DIALOG_SAVE_TRACE, names,
					shell);
		}

		// no folder selected
		// user press "cancel" button;
		if (folder == null) {
			return success;
		}

		// copy file to folder
		success = Util.copyFileToFolder(usedFile, folder);

		// report to user
		if (success) {
			Util.showMessage(Constants.INFO_SAVE_SUCCESS + folder);
		} else {
			Util.showMessage(Constants.MAIN_CAN_NOT_COPY + usedFile);
		}
		return success;
	}

	/**
	 * Notifies this action delegate that the selection in the workbench has
	 * changed.
	 * 
	 * @param part
	 *            Workbench part
	 * 
	 * @param selection
	 *            User selection
	 */
	@SuppressWarnings("restriction")
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// project reference
		IProject selectedProject = null;

		// Check where the selection comes from
		// Supported views: CommonNavigator, SymbianProjectNavigatorView and
		// ResourceNavigator

		if (!(part instanceof CommonNavigator
				|| part instanceof SymbianProjectNavigatorView || part instanceof ResourceNavigator)) {
			return;
		}

		// get selection
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		// get first element of selection
		Object element = structuredSelection.getFirstElement();

		// check selection
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;

			IResource resource = null;
			if (adaptable instanceof IResource) {
				resource = (IResource) adaptable;
			} else if (adaptable instanceof org.eclipse.cdt.internal.ui.cview.IncludeRefContainer) {
				selectedProject = ((org.eclipse.cdt.internal.ui.cview.IncludeRefContainer) adaptable)
						.getCProject().getProject();
			} else if (adaptable instanceof org.eclipse.cdt.core.model.ICProject) {
				selectedProject = ((org.eclipse.cdt.core.model.ICProject) adaptable)
						.getProject();
			} else {
				resource = (IResource) adaptable.getAdapter(IResource.class);
			}

			// resource found => get resource project
			if (resource != null) {
				selectedProject = resource.getProject();
			}
		}
		// first item is null => update build state
		else if (element == null) {
			updateBuildState(null);
		}

		// if project found and it is open => get project results
		if (selectedProject != null && selectedProject.isOpen()) {
			project = selectedProject;
			getProjectResults(selectedProject);
			updateBuildState(project);
			if (statisticView != null) {
				statisticView.handleProjectChange(project);
			}
		}
	}

	/**
	 * Executes clickAction when user selects item in the AnalyzeTool view.
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 * 
	 * @param event
	 *            Selection changed event
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		clickAction.run();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		runView.getControl().setFocus();
	}

	/**
	 * Sets text to information label.
	 */
	public final void setTextToInformationLabel() {
		if (activeTreeItem == null) {
			return;
		}

		// run information
		RunResults oneRunResults = projectResults.getRun(project,
				activeTreeItem.getRunID());

		// no results found => update label
		if (oneRunResults == null) {
			updateInformationLabel("");
		} else {
			StringBuffer buffer = new StringBuffer(32);
			buffer.append("Run: ");
			buffer.append(oneRunResults.getItemID());

			buffer.append(" Memory leaks: ");
			buffer.append(oneRunResults.getAnalysisItems().size());
			buffer.append(" Handle leaks: ");
			buffer.append(oneRunResults.getHandleLeaks().size());
			if (("").equals(oneRunResults.getEndTime())) {
				buffer.append("\nStart time: " + oneRunResults.getStartTime()
						+ " \nEnd time: FAILED");
			} else {
				buffer.append("\nStart time: " + oneRunResults.getStartTime()
						+ " \nEnd time: " + oneRunResults.getEndTime());
			}
			updateInformationLabel(buffer.toString());
		}
	}

	/**
	 * Shows error message.
	 * 
	 * @param message
	 *            Error message to show
	 */
	public final void showErrorMessage(final String message) {
		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				Util.showErrorMessage(message);
			}
		});
	}

	/**
	 * Shows message.
	 * 
	 * @param message
	 *            Message to show
	 */
	public final void showMessage(final String message) {
		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				Util.showMessage(message);
			}
		});
	}

	/**
	 * Starts traceviewer connection.
	 */
	private void start() {

		// check if the project is selected and open
		if (!checkProjectValidity()) {
			return;
		}

		// get project file infos
		getFileInfos(project);

		String dataFile = Util.isDataFileAvailable(project);
		// if data file already exists, ask for overwrite
		if (dataFile != null && !dataFile.equalsIgnoreCase("")) {
			int saveDataFiles = Util
					.openConfirmationDialogWithCancel(Constants.CONFIRM_OVERWRITE_FILE);
			if (saveDataFiles == Constants.SAVE_DATA_FILE) {
				if (!saveReportFile(Constants.SAVE_REPORT_FILE_DATA)) {
					return;
				}
			} else if (saveDataFiles == Constants.SAVE_DATA_FILE_CANCEL) {
				// user press "Cancel" so return and do nothing
				return;
			}

			// delete existing data file
			Util.deleteDataFile(project);
		}

		ICarbideProjectInfo info = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);
		ICarbideBuildConfiguration config = info.getDefaultConfiguration();

		// start listening emulator output
		if (config.getPlatformString().equals(Constants.BUILD_TARGET_WINSCW)) {

			String dbghelpDllVersionInfo = Util.getDbghelpDllVersionInfo(Util
					.getAtoolInstallFolder());

			if (dbghelpDllVersionInfo != Constants.DBGHELPDLL_IS_UP_TO_DATE) {

				DbghelpDllVersionInfoDialog dialog = new DbghelpDllVersionInfoDialog(
						getSite().getShell(), dbghelpDllVersionInfo);

				if (dialog.open() == Window.CANCEL)
					return;
			}

			listeningJob = new EpocReader(project, this);
			listeningJob.start();

			traceStarted();

			return;
		}

		// else start trace capturing using TraceViewer connection

		// main view class instance
		// this instance if passed to the TraceWrapper class
		final MainView selfInstance = this;
		Job activateTrace = new Job(Constants.STARTING_TRACE) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					// update progress monitor state
					monitor.beginTask(Constants.STARTING_TRACE,
							IProgressMonitor.UNKNOWN);

					// load TraceWrapper class
					Class<?> buildManagerClass = Class
							.forName("com.nokia.s60tools.analyzetool.trace.TraceWrapper");

					// get TraceWrapper class available methods
					java.lang.reflect.Method[] methods = buildManagerClass
							.getMethods();

					// thru methods
					for (int i = 0; i < methods.length; i++) {
						// get one method
						java.lang.reflect.Method oneMethod = methods[i];

						// if method name is "connect"
						if (oneMethod.getName().equalsIgnoreCase("connect")) {

							// parameters for the connect method
							Object[] objs = new Object[1];
							objs[0] = selfInstance;

							// call connect method
							String returnValue = (String) oneMethod.invoke(
									buildManagerClass.newInstance(), objs);

							// if there was no errors while connection
							if (("").equals(returnValue)) {

								// invoke parser to open needed streams
								parser.openStreams(Util.getBldInfFolder(
										project, true));

								traceStarted();
							} else {
								traceAction
										.setImageDescriptor(Activator
												.getImageDescriptor((Constants.BUTTON_RUN)));
								traceActive = false;

								showErrorMessage(returnValue);
							}
						}
					}
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();

				} catch (SecurityException se) {
					se.printStackTrace();

				} catch (IllegalAccessException iae) {
					iae.printStackTrace();

				} catch (IllegalArgumentException iare) {
					iare.printStackTrace();

				} catch (java.lang.reflect.InvocationTargetException ite) {
					ite.printStackTrace();

				} catch (InstantiationException iv) {
					iv.printStackTrace();
				}

				return new Status(IStatus.OK, Constants.ANALYZE_CONSOLE_ID,
						IStatus.OK, "Trace started", null);
			}
		};
		activateTrace.setUser(true);
		activateTrace.schedule();
	}

	/**
	 * Starts subtests.
	 */
	public final void startSubTest() {
		// get started processes
		Hashtable<String, Integer> startedPros = null;

		// if parser exists => means that the tracing is started and processes
		// info is available
		if (parser != null) {
			startedPros = parser.getStartedProcesses();
		}

		// no processes = >show info and leave
		if (startedPros == null || startedPros.isEmpty()) {
			Util.showMessage(Constants.SUBTEST_NO_PROCESSES);
			return;
		}

		// get started target info
		AbstractList<String> targets = new ArrayList<String>();
		for (java.util.Enumeration<String> e = startedPros.keys(); e
				.hasMoreElements();) {
			String processName = e.nextElement();
			targets.add(processName);
		}

		// used target
		String target = null;

		// processes found, print info to user
		if (targets.size() == 1) {
			target = targets.get(0);
		} else {
			target = Util.openSelectionDialog(Constants.SUBTEST_SELECT_TARGET,
					Constants.SUBTEST_RUNNING_PROCESSES_INFO, targets);

		}
		if (target == null || ("").equals(target)) {
			return;
		}

		// ask for subtest name
		CustomInputDialog dialog = new CustomInputDialog(
				Constants.ANALYZE_TOOL_TITLE, Constants.SUBTEST_INPUT_NAME, "");
		dialog.open();
		String subTestName = dialog.getUserInput();

		if (subTestName == null || subTestName.length() == 0) {
			return;
		}

		// get process id for selected target
		int processID = startedPros.get(target);

		// if subtest already exists
		if (isSubtestExists(subTestName, target)) {
			Util.showMessage(Constants.SUBTEST_ALLREADY_RUNNING);
			return;
		}

		// create new subtes object and add it to AbstractList
		ActiveSubtests subtes = new ActiveSubtests(subTestName, target,
				processID);
		startedSubtest.add(subtes);

		// start subtest
		parser.parse(Constants.PREFIX + " " + subtes.getProcessID()
				+ " TSS 0000 " + subTestName);
		updateSubtestInfoText(Constants.SUBTEST_STARTED + target
				+ Constants.ENRULE + subTestName);

		stopSubtest.setEnabled(true);
	}

	/**
	 * Stop external message tracing.
	 */
	public final void stop(boolean analyze) {

		ICarbideProjectInfo info = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);
		ICarbideBuildConfiguration config = info.getDefaultConfiguration();
		if (config.getPlatformString().equalsIgnoreCase(
				Constants.BUILD_TARGET_WINSCW)) {
			listeningJob.stop();
			traceStopped(analyze);
			return;
		}

		// else stop TraceViewer connection
		try {
			Class<?> buildManagerClass = Class
					.forName("com.nokia.s60tools.analyzetool.trace.TraceWrapper");

			java.lang.reflect.Method[] methods = buildManagerClass.getMethods();
			for (int i = 0; i < methods.length; i++) {
				java.lang.reflect.Method oneMethod = methods[i];
				if (oneMethod.getName().equalsIgnoreCase("disconnect")) {

					Object[] objs = new Object[1];
					objs[0] = this;
					String returnValue = (String) oneMethod.invoke(
							buildManagerClass.newInstance(), new Object[] {});
					if (("").equals(returnValue)) {
						traceStopped(analyze);
					} else {
						showErrorMessage("Error while disconnecting TraceViewer");
						traceAction.setImageDescriptor(Activator
								.getImageDescriptor((Constants.BUTTON_STOP)));
						traceActive = true;
					}
				}
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();

		} catch (SecurityException se) {
			se.printStackTrace();

		} catch (IllegalAccessException iae) {
			iae.printStackTrace();

		} catch (IllegalArgumentException iare) {
			iare.printStackTrace();

		} catch (java.lang.reflect.InvocationTargetException ite) {
			ite.printStackTrace();

		} catch (InstantiationException iv) {
			iv.printStackTrace();
		}
	}

	/**
	 * Updates button states and creates results for project.
	 */
	private void traceStopped(boolean analyze) {
		// update icon and information text
		traceAction.setImageDescriptor(Activator
				.getImageDescriptor((Constants.BUTTON_RUN)));
		traceAction.setText(Constants.ACTION_START_TRACE);
		traceAction.setToolTipText(Constants.ACTION_START_TRACE);
		traceActive = false;

		// close any active subtests
		if (!startedSubtest.isEmpty()) {
			for (int j = 0; j < startedSubtest.size(); j++) {
				ActiveSubtests oneSubtest = startedSubtest.get(j);
				parser.parse(Constants.PREFIX + " " + oneSubtest.getProcessID()
						+ " TSE 0000 " + oneSubtest.getName());
			}
			startedSubtest.clear();
		}

		// tell parser to finish => write data to file
		parser.finish();

		// inform user
		updateLabel(Constants.INFO_TRACE_STOP);

		// set start and stop trace not visible
		startSubtest.setEnabled(false);
		stopSubtest.setEnabled(false);
		fileOpenMenu.setEnabled(true);
		saveFileMenu.setEnabled(true);
		cleanAtoolChanges.setEnabled(true);

		project = traceStartedProjectRef;

		// parse and analyze saved data file
		if (analyze) {
			analyzeDataFile(Constants.ANALYZE_USE_DATA_FILE, null, true);
		}
	}

	/**
	 * Stop one subtest. If there is multiple subtests running ask for user to
	 * which one to stop.
	 */
	public final void stopSubTest() {
		// no processes show info
		if (startedSubtest.isEmpty()) {
			Util.showMessage(Constants.SUBTEST_NO_SUBTESTS);
		} else {
			// only one subtest no need to ask for users
			if (startedSubtest.size() == 1) {
				ActiveSubtests oneSubtest = startedSubtest.get(0);
				parser.parse(Constants.PREFIX + " " + oneSubtest.getProcessID()
						+ " TSE 0000 " + oneSubtest.getName());
				updateSubtestInfoText(Constants.SUBTEST_ENDED
						+ oneSubtest.getTargetName() + Constants.ENRULE
						+ oneSubtest.getName());
				startedSubtest.remove(0);
				stopSubtest.setEnabled(false);
			} else { // multiple subtest found ask for the user which to stop
				// multiple subtest found ask for user which to be ended
				AbstractList<String> tmpSubtest = new ArrayList<String>();

				// get list of active subtests
				for (int i = 0; i < startedSubtest.size(); i++) {
					ActiveSubtests oneSubtest = startedSubtest.get(i);
					String tmpStr = oneSubtest.getTargetName()
							+ Constants.ENRULE + oneSubtest.getName();
					tmpSubtest.add(tmpStr);
				}

				String selection = Util.openSelectionDialog(
						Constants.SUBTEST_SELECT_SUBTEST_TO_STOP, null,
						tmpSubtest);
				// thru active subtests
				for (int k = 0; k < startedSubtest.size(); k++) {
					// get one subtest
					ActiveSubtests oneSubtest = startedSubtest.get(k);

					// split user selected subtest information to get subtest
					// name
					String[] splittedText = selection.split(" ");

					// if user selected subtest name and current subtest equals
					if (splittedText[2].equals(oneSubtest.getName())) {
						// write subtest end tag to data file
						parser.parse(Constants.PREFIX + " "
								+ oneSubtest.getProcessID() + " TSE 0000 "
								+ oneSubtest.getName());
						updateSubtestInfoText(Constants.SUBTEST_ENDED
								+ oneSubtest.getTargetName() + Constants.ENRULE
								+ oneSubtest.getName());
						// remove current subtest from active subtest
						// AbstractList
						startedSubtest.remove(k);
						break;
					}
				}
				if (startedSubtest.isEmpty()) {
					stopSubtest.setEnabled(false);
				}
			}
		}
	}

	/**
	 * When AnalyzeTool view tree model is collapsed.
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
	 * 
	 * @param event
	 *            Tree expansion event
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

	/**
	 * When AnalyzeTool view tree model is expanded.
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
	 * 
	 * @param event
	 *            Tree expansion event
	 */
	public void treeExpanded(TreeExpansionEvent event) {
		// MethodDeclaration/Block[count(BlockStatement) = 0 and
		// @containsComment = 'false']
	}

	/**
	 * Updates allocation count in the information label.
	 * 
	 * The information layout is hard to modify so we need to manage information
	 * label text
	 */
	public final void updateAllocNumber() {

		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {

				// if label is created
				if (informationLabel != null) {
					// get existing label text
					String tmpText = informationLabel.getText();

					// split existing text
					String[] strArray = tmpText.split(lineFeed);

					// if text contains more than 3 lines
					if (strArray.length >= 2) {
						// create new buffer
						StringBuffer tmpBuffer = new StringBuffer(
								strArray.length);

						// set memory allocation size
						strArray[1] = Constants.INFO_ALLOCATED_MEM
								+ parser.getAllocationsSize();

						// get updated text to buffer
						for (int i = 0; i < strArray.length; i++) {
							tmpBuffer.append(strArray[i]);
							tmpBuffer.append(lineFeed);
						}

						// set new text to information label
						informationLabel.setText(tmpBuffer.toString());
					} else {
						informationLabel.setText(tmpText + lineFeed
								+ Constants.INFO_ALLOCATED_MEM
								+ parser.getAllocationsSize());
					}
				}
			}
		});
	}

	/**
	 * Update build action icon and tooltip text.
	 * 
	 * @param projectRef
	 *            Project reference
	 */
	public final void updateBuildState(final IProject projectRef) {
		if (buildWithAtool == null) {
			return;
		}

		BuilderUtil util = new BuilderUtil();

		// if project is not selected
		if (projectRef == null) {
			buildWithAtool.setText(Constants.ACTION_AT_BUILD_DEACTIVE);
			buildWithAtool.setToolTipText(Constants.ACTION_AT_BUILD_DEACTIVE);
			buildWithAtool.setChecked(false);
		}
		// if AnalyzeTool build is enabled
		else if (util.isNatureEnabled(projectRef)) {
			buildWithAtool.setText(Constants.ACTION_AT_BUILD_ACTIVE);
			buildWithAtool.setToolTipText(Constants.ACTION_AT_BUILD_ACTIVE);
			buildWithAtool.setChecked(true);
		} else {
			buildWithAtool.setText(Constants.ACTION_AT_BUILD_DEACTIVE);
			buildWithAtool.setToolTipText(Constants.ACTION_AT_BUILD_DEACTIVE);
			buildWithAtool.setChecked(false);
		}
	}

	/**
	 * Indicates the target (emulator/device) by inspecting the given projects
	 * build configuration.
	 * 
	 * @param selectedProject
	 *            the currently active project
	 * @return "emulator" if the build configuration is WINSCW, "device"
	 *         otherwise
	 */
	private static String getTraceTarget(final IProject selectedProject) {
		String target = "";
		if (selectedProject != null && selectedProject.isOpen()) {
			ICarbideProjectInfo info = CarbideBuilderPlugin.getBuildManager()
					.getProjectInfo(selectedProject);
			if (info != null) {
				ICarbideBuildConfiguration config = info
						.getDefaultConfiguration();
				target = config.getPlatformString().equals(
						Constants.BUILD_TARGET_WINSCW) ? Constants.INFO_TRACE_FROM_EMULATOR
						: Constants.INFO_TRACE_FROM_DEVICE;
			}
		}
		return target;
	}

	/**
	 * Update change detail action state.
	 * 
	 * @param projectRef
	 *            Current project
	 */
	public final void updateChangeDetailState(final IProject projectRef) {
		if (changeDetails == null) {
			return;
		}

		if (projectRef == null) {
			changeDetails.setEnabled(false);
			refreshResults.setEnabled(false);
			return;
		} else if (projectResults.contains(projectRef)) {
			changeDetails.setEnabled(true);
		} else {
			changeDetails.setEnabled(false);
		}

		String dataFile = projectResults.getDataFileName(projectRef);
		if (dataFile != null) {
			int fileType = UseAtool.checkFileType(dataFile);
			if (fileType == Constants.DATAFILE_TRACE
					|| fileType == Constants.DATAFILE_LOG
					|| fileType == Constants.DATAFILE_BINARY) {
				refreshResults.setEnabled(true);
			} else {
				refreshResults.setEnabled(false);
			}
		} else {
			refreshResults.setEnabled(false);
		}
	}

	/**
	 * Sets information to the information label.
	 * 
	 * @param infoText
	 *            Info text
	 */
	private void updateInformationLabel(final String infoText) {
		// trace is active => do not update information label
		if (traceActive) {
			return;
		}

		// sync with the UI thread
		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				// if informationlabel exists set text
				if (informationLabel != null) {
					informationLabel.setText(infoText);
				}
			}
		});
	}

	/**
	 * Updates label by given string.
	 * 
	 * @param line
	 *            String to display
	 */
	public final void updateLabel(final String line) {

		final String oneLine = line;

		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				// if informationlabel exists set text
				if (informationLabel != null) {
					informationLabel.setText(oneLine);
				}
			}
		});
	}

	/**
	 * Updates Subtest info to the information label.
	 * 
	 * The information layout is hard to modify so we need to manage information
	 * label text
	 * 
	 * @param text
	 *            New label text
	 */
	private void updateSubtestInfoText(final String text) {
		final String newText = text;

		runView.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {

				// if label exists
				if (informationLabel != null) {
					// get existing label text
					String origText = informationLabel.getText();

					// split existing text
					String[] strArray = origText.split(lineFeed);

					// create new buffer
					StringBuffer tmpBuffer = new StringBuffer(strArray.length);
					if (strArray.length >= 3) {
						// update text
						strArray[2] = newText;

						// get updated text to buffer
						for (int i = 0; i < strArray.length; i++) {
							tmpBuffer.append(strArray[i]);
							tmpBuffer.append(lineFeed);
						}
						// set new text to information label
						informationLabel.setText(tmpBuffer.toString());
					} else {
						informationLabel.setText(origText + newText);
					}
				}
			}
		});
	}

	/**
	 * Updates button states, clears existing project data and updating view.
	 */
	private void traceStarted() {
		// clear view contents
		cleanAnalyzeData(project);
		clearCallstackViewContent();
		traceStartedProjectRef = project;

		// change icon and information text
		traceAction.setImageDescriptor(Activator
				.getImageDescriptor((Constants.BUTTON_STOP)));
		traceAction.setText(Constants.ACTION_STOP_TRACE);
		traceAction.setToolTipText(Constants.ACTION_STOP_TRACE);
		traceActive = true;

		String fromTarget = getTraceTarget(traceStartedProjectRef);
		updateLabel(fromTarget.length() == 0 ? Constants.INFO_TRACE_START
				: String.format(Constants.INFO_TRACE_FROM_TARGET_START,
						fromTarget));
		// add2UserActionHistory( "Trace started for
		// project: "
		// +project.getName() +" at "+ Util.getTime() );

		// set start and stop subtest visible
		startSubtest.setEnabled(true);
		stopSubtest.setEnabled(false);
		fileOpenMenu.setEnabled(false);
		saveFileMenu.setEnabled(false);
		cleanAtoolChanges.setEnabled(false);

		updateAllocNumber();
	}

	/**
	 * Overrided method to capture keyevents.
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {

		// C key address(hex)
		final int CTRL_C = 0x3;

		// key event character
		int charValue = e.character;

		// c key pressed
		boolean cPressed = (charValue == CTRL_C); // This should be enough

		// ctrl key pressed
		boolean ctrlPressed = (e.stateMask & SWT.CTRL) != 0;

		// if ctrl and c key pressed => run copy action
		if (ctrlPressed & cPressed) {
			// Triggering copy action
			copyAction.run();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// This method is overrided
	}

	/**
	 * This job parses the .dat file and loads the graph
	 * 
	 */
	class GraphLoadJob extends Job {

		/** Location of file to parse for graph model */
		private String datFileLocation;

		/**
		 * Constructor
		 * 
		 * @param aDatFileLocation
		 *            Location of file to parse for graph model
		 */
		public GraphLoadJob(String aDatFileLocation) {
			super(Constants.GRAPH_LOAD_JOB_TITLE);
			this.datFileLocation = aDatFileLocation;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			monitor.beginTask(Constants.GRAPH_GENERATING_PROG_TITLE,
					IProgressMonitor.UNKNOWN);
			try {
				ReadFile fileReader = new ReadFile();
				boolean success = fileReader.readFile(datFileLocation);
				if (success) {
					AbstractList<ProcessInfo> processes = fileReader
							.getStatistic();
					IMemoryActivityModel model = new AnalyzeFactory()
							.createModel(processes.size() == 0);
					if (processes.size() > 0) {
						model.setDeferredCallstackReading(fileReader
								.hasDeferredCallstacks());
						if (model.isDeferredCallstackReading()) {
							DeferredCallstackManager callstackManager = new DeferredCallstackManager(
									datFileLocation);
							callstackManager.setProcesses(processes);
							model.setCallstackManager(callstackManager);
						} else {
							model
									.setCallstackManager(new SimpleCallstackManager());
						}
					}
					if (!monitor.isCanceled()) {
						chart.setInput(project, model);
						model.addProcesses(processes);
					}
					fileReader.finish();
				}
			} catch (OutOfMemoryError oome) {
				Activator
						.getDefault()
						.logInfo(IStatus.ERROR, IStatus.ERROR,
								"Can not allocate enough memory for the memory usage graph model.");
			} catch (Exception e) {
				Activator.getDefault().log(IStatus.ERROR,
						"Error while generating graph model", e);
			}
			graphLoadJob = null;
			return Status.OK_STATUS;
		}
	}
}
