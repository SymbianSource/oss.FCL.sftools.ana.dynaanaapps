/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class StatisticView
 *
 */

package com.nokia.s60tools.analyzetool.ui.statistic;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Calendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.cdt.debug.cw.symbian.symbolreader.IFunction;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISourceLocation;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISymbolFile;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.DllLoad;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.SourceFile;
import com.nokia.s60tools.analyzetool.engine.statistic.SymReader;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.ui.ResourceVisitor;

/**
 * Creates a statistic view Provides functions to create memory usage statistic
 * from the AnalyzeTool trace file
 *
 * @author kihe
 *
 */
@SuppressWarnings("restriction")
public class StatisticView implements SelectionListener, MouseListener {

	/** Tab for the this view */
	CTabItem memoryTab;

	/** Table for the statistic information */
	Table memoryTable;

	/** Function column */
	TableColumn columnFunction;

	/** File column */
	TableColumn columnFile;

	/** Count column */
	TableColumn columnCount;

	/** Line column */
	TableColumn columnLine;

	/** Time column */
	TableColumn columnTime;

	/** Size column */
	TableColumn columnSize;

	/** User selected table item */
	TableItem selectedItem;

	/** Selected run id */
	int selectedRun = 0;

	/** Combo for run selection */
	Combo combo;

	/** Active project reference */
	IProject currProject;

	/** Contains project related results */
	HashMap<IProject, AbstractList<ProcessInfo>> projectRelatedRes;

	/** Contains only one(selected) project cache */
	HashMap<Integer, AbstractList<String[]>> cache;

	/** Time cache, this cache is used provide comparison by time */
	HashMap<String, Long> timeCache;

	/** Contains all created results */
	HashMap<IProject, HashMap<Integer, AbstractList<String[]>>> projectCache;

	/** Contains build platform and target information. */
	Label infoLabel;

	/** Default display string when no results are available. */
	String noResults = "---";

	/** Contains cpp files info. */
	private final AbstractList<String> cppFileNames;

	/** Symbol reader */
	SymReader reader;

	boolean errorOccurred = false;

	/**
	 * Constructor
	 */
	public StatisticView() {
		projectRelatedRes = new HashMap<IProject, AbstractList<ProcessInfo>>();
		cache = new HashMap<Integer, AbstractList<String[]>>();
		timeCache = new HashMap<String, Long>();
		projectCache = new HashMap<IProject, HashMap<Integer, AbstractList<String[]>>>();
		cppFileNames = new ArrayList<String>();
	}


	/**
	 * Cleans stored statistic
	 * @param project Project reference
	 */
	public void clean(IProject project) {
		memoryTable.clearAll();

		combo.removeAll();

		if (project == null) {
			projectCache.clear();
			projectRelatedRes.clear();
			cppFileNames.clear();
			timeCache.clear();
			cache.clear();
		} else {
			projectRelatedRes.remove(project);
			projectCache.remove(project);
		}

		setDefaultContent();

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				infoLabel.setText("");
			}
		});
	}

	/**
	 * Sorts the given statistic and displays it to user
	 * @param createAllResults Create all the results, not just one run results
	 */
	public void compareAndPrint(boolean createAllResults) {
		// if current project does not contain statistic => leave
		if (!projectRelatedRes.containsKey(currProject)) {
			return;
		}

		// if cache contains statistic for selected run use it
		if (cache.containsKey(selectedRun)) {
			AbstractList<String[]> items = cache.get(selectedRun);
			updateView(items, false);
			return;
		}

		if (reader == null) {
			reader = new SymReader(currProject);
			reader.loadProjectTargetsInfo();
		}

		// get project related results
		AbstractList<ProcessInfo> processes = projectRelatedRes
				.get(currProject);
		if (processes.isEmpty()) {
			setDefaultContent();
			return;
		}
		// create all the results
		else if (createAllResults) {

			int runIndex = 0;
			Iterator<ProcessInfo> iterProcess = processes.iterator();
			while (iterProcess.hasNext()) {
				ProcessInfo tempProcess = iterProcess.next();
				createResults(tempProcess, runIndex);
				runIndex++;
			}

			// all the runs results are generated => now display the first
			// results
			// compareAndPrint(false);

		}
		// create only one(specific) run results
		else {
			ProcessInfo tempProcessInfo = processes.get(selectedRun);
			createResults(tempProcessInfo, selectedRun);
		}
		if (reader != null) {
			reader.dispose();
			reader = null;
		}

	}

	/**
	 * Find symbol reader api pinpointed class file for project class files.
	 *
	 * @param fileName
	 *            Cpp file name
	 * @return Found cpp file location
	 */
	private String getFileNames(String fileName) {
		Iterator<String> iterFiles = cppFileNames.iterator();
		int slash = Util.getLastSlashIndex(fileName);
		String tempFile = fileName.substring(slash + 1, fileName.length());

		String realFileName = fileName;
		while (iterFiles.hasNext()) {
			String tempFileName = iterFiles.next();
			int slashTemp = Util.getLastSlashIndex(tempFileName);
			String tempFileWithoutExt = tempFileName.substring(slashTemp + 1,
					tempFileName.length());
			if (tempFileWithoutExt.equalsIgnoreCase(tempFile)) {
				realFileName = tempFileName;
				break;
			}
		}

		return realFileName;
	}

	/**
	 * Converts hex time to time value
	 *
	 * @param timeInHex
	 *            Time in hex
	 * @return Time
	 */
	public String converTime(Long timeInHex) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(timeInHex / 1000);

		StringBuffer timeStr = new StringBuffer();
		String[] monthName = { "Jan", "Feb", "March", "Apr", "May", "June",
				"July", "Aug", "Sep", "Oct", "Nov", "Dec" };
		char space = ' ';
		String[] day = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		timeStr.append(day[cal.get(Calendar.DAY_OF_WEEK) - 1]);
		timeStr.append(space);
		timeStr.append(monthName[cal.get(Calendar.MONTH)]);
		timeStr.append(space);
		timeStr.append(cal.get(Calendar.DAY_OF_MONTH));
		timeStr.append(space);
		timeStr.append(cal.get(Calendar.YEAR));
		timeStr.append(space);
		timeStr.append(cal.get(Calendar.HOUR_OF_DAY));
		timeStr.append(':');
		int minute = cal.get(Calendar.MINUTE);
		if (minute < 10) {
			timeStr.append('0');
		}
		timeStr.append(minute);
		timeStr.append(':');
		int second = cal.get(Calendar.SECOND);
		if (second < 10) {
			timeStr.append('0');
		}
		timeStr.append(second);
		timeStr.append('.');
		int millis = cal.get(Calendar.MILLISECOND);
		if (millis < 10) {
			timeStr.append("00");
		} else if (millis < 100) {
			timeStr.append('0');
		}
		timeStr.append(millis);
		return timeStr.toString();
	}

	/**
	 * Creates results
	 *
	 * @param tempProcessInfo
	 *           Process info
	 * @param runIndex
	 *            Selected run index
	 */
	private void createResults(ProcessInfo tempProcessInfo,
			int runIndex) {

		//create cache for found source files
		HashMap<Long, SourceFile> sourceFileCache = new HashMap<Long, SourceFile>();

		//list for found files
		AbstractList<SourceFile> files = new ArrayList<SourceFile>();

		//get process allocations
		AbstractList<AllocInfo> allocs = tempProcessInfo.getAllocs();
		Iterator<AllocInfo> iterAllocs = allocs.iterator();

		//thru allocations
		while (iterAllocs.hasNext()) {

			//get one allocation
			AllocInfo oneInfo = iterAllocs.next();

			//get allocation callstack
			AbstractList<AllocCallstack> allocCalls = oneInfo.getCallstack();
			Iterator<AllocCallstack> iterAllocCalls = allocCalls.iterator();

			//go thru callstack values until find first source file
			while (iterAllocCalls.hasNext()) {

				if( errorOccurred ) {
					return;
				}

				AllocCallstack oneCallstack = iterAllocCalls.next();

				//if callstack item do not contain dll load item => could not be pinpointed
				DllLoad tempLoad = oneCallstack.getDllLoad();
				if (tempLoad == null) {
					continue;
				}

				//if callstack item memory addreess match to dll load start address => skip these values
				if (oneCallstack.getMemoryAddress()== tempLoad.getStartAddress()) {
					continue;
				}

				//find memory address from the cache
				if (sourceFileCache
						.containsKey(oneCallstack.getMemoryAddress())) {
					SourceFile tempFile = sourceFileCache.get(oneCallstack
							.getMemoryAddress());
					int intValue = oneInfo.getSizeInt();
					tempFile.updateSize(intValue);
					tempFile.setTime(oneInfo.getTime());
					tempFile.updateHowManyTimes();
					break;
				}

				//not found in the cache => try to pinpoint it
				SourceFile file = pinpoint(oneCallstack.getMemoryAddress(),
						oneCallstack.getDllLoad());

				//if source file found
				if (file != null) {
					//set source file values
					int intValue = oneInfo.getSizeInt();
					file.updateSize(intValue);
					file.setTime(oneInfo.getTime());
					files.add(file);

					//add found source file to cache
					sourceFileCache.put(oneCallstack.getMemoryAddress(), file);
					break;
				}
			}
		}

		//store found results to cache
		storeResults(files, runIndex);

		//clear used cache
		sourceFileCache.clear();
	}

	/**
	 * Creates statistic view
	 *
	 * @param parent
	 *            View parent
	 * @return CTabItem created view
	 */
	public CTabItem createView(CTabFolder parent) {
		// create new sashform for the statistic view
		SashForm allFrom = new SashForm(parent, SWT.VERTICAL);

		/** new sashform for the run selection */
		SashForm infoFrom = new SashForm(allFrom, SWT.HORIZONTAL);

		/** new sashform for the memory statistic information */
		SashForm memoryFrom = new SashForm(allFrom, SWT.NONE);

		// create new tab
		memoryTab = new CTabItem(parent, SWT.NONE);
		memoryTab.setText(Constants.STATISTICS_TAB_TITLE);

		// initialize tab layout
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;

		/** create new label */
		Label label = new Label(infoFrom, SWT.NONE);
		label.setText(Constants.STATISTICS_SELECT_RUN);

		/** create new choisebox */
		combo = new Combo(infoFrom, SWT.DROP_DOWN);
		combo.addSelectionListener(this);

		/** create empty label to fill non-used space */
		infoLabel = new Label(infoFrom, SWT.NONE);
		infoLabel.setText("");

		/** Set sashform layout */
		infoFrom.setWeights(new int[] { 1, 1, 9 });

		/** Set sashform layout */
		allFrom.setWeights(new int[] { 1, 14 });

		// create new table for the tab
		memoryTable = new Table(memoryFrom, SWT.VIRTUAL | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);
		memoryTable.setHeaderVisible(true);
		memoryTable.setLinesVisible(true);
		memoryTable.setItemCount(12);
		memoryTab.setControl(allFrom);

		columnFile = new TableColumn(memoryTable, SWT.Selection);
		columnFile.setText(Constants.STATISTICS_NODE_FILE);
		columnFile.setWidth(160);

		// create table items
		columnFunction = new TableColumn(memoryTable, SWT.Selection);
		columnFunction.setText(Constants.STATISTICS_NODE_FUNCTION);
		columnFunction.setWidth(120);

		columnLine = new TableColumn(memoryTable, SWT.Selection);
		columnLine.setText(Constants.STATISTICS_NODE_LINE);
		columnLine.setWidth(60);

		columnCount = new TableColumn(memoryTable, SWT.Selection);
		columnCount.setText(Constants.STATISTICS_NODE_ALLOCS);
		columnCount.setWidth(60);
		columnCount.addListener(SWT.Selection, new ATComparator(this,
				memoryTable, 3, true));

		columnTime = new TableColumn(memoryTable, SWT.Selection);
		columnTime.setText(Constants.STATISTICS_NODE_TIME);
		columnTime.setWidth(60);
		columnTime.addListener(SWT.Selection, new ATComparator(this,
				memoryTable, 4, false));

		columnSize = new TableColumn(memoryTable, SWT.Selection);
		columnSize.setText(Constants.STATISTICS_NODE_SIZE);
		columnSize.setWidth(60);
		columnSize.addListener(SWT.Selection, new ATComparator(this,
				memoryTable, 5, true));

		memoryTable.addSelectionListener(this);
		memoryTable.addMouseListener(this);

		memoryTable.pack();
		memoryTable.update();
		setDefaultContent();
		return memoryTab;
	}

	/**
	 * Fit columns size to match column text size.
	 */
	public void doPack() {
		TableColumn[] columns = memoryTable.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
	}

	/**
	 * Return time from the time cache
	 *
	 * @param time
	 *            Time
	 * @return Time in milliseconds if time found otherwise null
	 */
	public Long getTimeFromCache(String time) {
		// if time is set to cache
		if (timeCache.containsKey(time)) {
			return timeCache.get(time);
		}
		return null;
	}

	/**
	 * Handles project changed actions
	 *
	 * @param projectRef
	 *            Project reference
	 */
	public void handleProjectChange(IProject projectRef) {

		//save selected project info
		if (projectRef == null) {
			return;
		} else if (currProject == null) {
			currProject = projectRef;
		} else if (currProject.equals(projectRef)) {
			return;
		}

		currProject = projectRef;

		//update UI items to match selected project results
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				memoryTable.clearAll();
				infoLabel.setText("");
				selectedRun = 0;
				AbstractList<ProcessInfo> processes = projectRelatedRes
						.get(currProject);

				if (processes == null || processes.isEmpty()) {
					String[] text = new String[1];
					text[0] = "Run: 1";
					combo.setItems(text);
					combo.select(0);
				} else {
					String[] text = new String[processes.size()];
					for (int i = 0; i < processes.size(); i++) {
						text[i] = "Run: " + Integer.toString(i + 1);
					}
					combo.setItems(text);
					combo.select(0);
				}

				updateCache();
				compareAndPrint(false);
			}
		});
	}

	/**
	 * Go thru the project files and stores cpp files.
	 *
	 * @param resource
	 *            One resource file of project
	 */
	public final void loadFileInfo(IResource resource) {
		// get all the cpp file info which are belongs to current project
		String cppFileName = Util.getCPPFileNameAndPath(resource);

		// if cpp file found, save it
		if (cppFileName != null && !cppFileNames.contains(cppFileName)) {
			this.cppFileNames.add(cppFileName);
		}
	}

	/**
	 * Listen mouse double click actions. Opens selected item in editor
	 */
	public void mouseDoubleClick(MouseEvent arg0) {
		openSourceFile(null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent arg0) {
		//this method is overwrite method of MouseListnener
		//and AT do not listens mouse DOWN events
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent arg0) {
		//this method is overwrite method of MouseListnener
		//and AT do not listens mouse UP events
	}

	/**
	 * Opens current callstack item on default editor and pinpoints memory leak
	 * line
	 *
	 * @param cppFileName
	 *            Cpp file name
	 * @param lineNumber
	 *            Cpp file line number
	 */
	private void openEditor(String cppFileName, String lineNumber) {

		//check that used information is given
		//we need to know file name and file line number
		//that we could open the right line in editor
		if (cppFileName == null || ("").equals(cppFileName)
				|| lineNumber == null || ("").equals(lineNumber)) {
			return;
		}
		try {

			IFile file = null;
			String usedFileName = null;
			usedFileName = getFileNames(cppFileName);
			if (currProject.isOpen()) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(
						new Path(currProject.getName()
								+ usedFileName.toLowerCase(Locale.US)));
			}

			// if file not found in active project
			// go thru all open projects in current workbench
			if (file == null || !file.exists()) {
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
						.getRoot();
				IProject[] projects = myWorkspaceRoot.getProjects();
				for (int i = 0; i < projects.length; i++) {
					file = ResourcesPlugin.getWorkspace().getRoot().getFile(
							new Path(projects[i].getName() + "\\"
									+ usedFileName));

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
			map.put(IMarker.LINE_NUMBER, Integer.parseInt(lineNumber));
			// map.put(IDE.EDITOR_ID_ATTR,
			// "org.eclipse.jdt.ui.ClassFileEditor");
			map.put(IDE.EDITOR_ID_ATTR, Constants.SOURCE_FILE_EDITOR_ID);
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker, true);

		} catch (PartInitException pie) {
			pie.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens source file for the selected item
	 *
	 * @param item
	 *            Selected item
	 */
	private void openSourceFile(TableItem item) {
		TableItem usedItem;

		if (item == null && selectedItem == null) {
			return;
		} else if (item == null) {
			usedItem = selectedItem;
		} else {
			usedItem = item;
		}

		// get source code line
		String sourceLine = usedItem.getText(2);

		// get source code file name
		String sourceFile = usedItem.getText(0);

		// open file in editor
		openEditor(sourceFile, sourceLine);
	}

	/**
	 * Pinpoints one memory address to source code line.
	 *
	 * @param memoryAddress
	 *            Memory address
	 * @param dllLoad
	 *           DllLoad item
	 * @return SourceFile if found otherwise null
	 */
	private SourceFile pinpoint(Long memoryAddress, DllLoad dllLoad) {

		//if dll load item exists
		if (dllLoad != null) {

			//get symbol file
			ISymbolFile symbolFile = reader.getSymbolFile(dllLoad.getName(),
					true);

			//if symbol file found => try to pinpoint memory address to source code line
			if( symbolFile != null ) {
				return pinpointToSrcLine(symbolFile, memoryAddress, dllLoad);
			}
		}
		return null;
	}

	/**
	 * Pinpoints memory address to source code line
	 *
	 * @param symbolFile
	 *            Opened symbol file
	 * @param memoryAddress
	 *            Used memory address
	 * @param dllLoad
	 *            DllLoad object where to memory address belongs
	 * @return SourceFile
	 */
	private SourceFile pinpointToSrcLine(ISymbolFile symbolFile,
			Long memoryAddress, DllLoad dllLoad) {

		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(currProject);
		String platform = cpi.getDefaultConfiguration().getPlatformString();

		try {
			// this is the start address of each symbol file
			long defaultLinkAddress = 0;

			// if the platform is other than winscw => adjust the memory address
			if (!(Constants.BUILD_TARGET_WINSCW).equalsIgnoreCase(platform)) {
				defaultLinkAddress = Long.parseLong("8000", 16);
			}

			// calculate memory address in symbol file
			long calculated = (memoryAddress - dllLoad.getStartAddress()) + defaultLinkAddress;

			java.math.BigInteger bigAddress = new java.math.BigInteger(Long
					.toHexString(calculated), 16);
			IFunction func = symbolFile.findFunctionByAddress(bigAddress);
			ISourceLocation loc = symbolFile.findSourceLocation(bigAddress);
			if (func != null && loc != null) {
				if (loc.getSourceFile() == null
						|| loc.getSourceFile().equalsIgnoreCase(""))
					return null;
				if (loc.getLineNumber() == 0)
					return null;
				if (func.getName() == null
						|| func.getName().equalsIgnoreCase(""))
					return null;
				/*
				 * if( onlyForProjectFiles &&
				 * !isFilePartOfTheProject(loc.getSourceFile()) ) return null;
				 */
				SourceFile file = new SourceFile();
				file.setFileName(loc.getSourceFile());
				file.setLineNumber(loc.getLineNumber());
				file.setFunctionName(func.getName());
				return file;
			}

		} catch (java.lang.NumberFormatException nfe) {
			errorOccurred = true;
			// do nothing by design
			nfe.printStackTrace();
		} catch (Exception e) {
			errorOccurred = true;
			// do nothing by design
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Sets statistic data
	 *
	 * @param project
	 *            Project reference
	 * @param processes
	 *            Statistic info for the processes
	 */
	public void setData(IProject project,
			final AbstractList<ProcessInfo> processes) {
		currProject = project;
		cache.clear();

		// update ui items
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				memoryTable.clearAll();
				memoryTable.setItemCount(1);
				TableItem tableItem = memoryTable.getItem(0);
				String[] data = new String[6];
				data[0] = Constants.STATISTICS_GENERATING;
				data[1] = "";
				data[2] = "";
				data[3] = "";
				data[4] = "";
				data[5] = "";
				tableItem.setText(data);
				doPack();
				String[] text = new String[processes.size()];
				for (int i = 0; i < processes.size(); i++) {
					text[i] = "Run: " + Integer.toString(i + 1);
				}
				combo.setItems(text);
				combo.select(0);
				combo.setEnabled(false);
				infoLabel.setText("");

			}
		});
		// store data
		projectRelatedRes.put(project, new ArrayList<ProcessInfo>(processes));

		// create results
		compareAndPrint(true);

		//update view
		updateView(cache.get(selectedRun), true);

		//load project cpp files => this helps us to open found source files in editor
		ResourceVisitor visitor = new ResourceVisitor(this);
		try {
			currProject.accept(visitor);
		} catch (CoreException ce) {
			// DO nothing be design
		}
	}


	@SuppressWarnings("unchecked")
	private void storeSortedItems(final int runIndex) {
		HashMap<Integer, AbstractList<String[]>> uiItems = new HashMap<Integer, AbstractList<String[]>>();
			AbstractList<String[]> strings = new ArrayList<String[]>();
			TableItem[] items = memoryTable.getItems();
			for(int i=0; i<items.length; i++) {
				TableItem oneItem = items[i];
				String[] text = new String[6];
				for(int j=0; j<6;j++) {
					text[j] = oneItem.getText(j);
				}
				strings.add(text);
			}
			uiItems.put(runIndex, strings);

			cache.put(runIndex, new ArrayList<String[]>(strings));
			projectCache.put(currProject,(HashMap<Integer, AbstractList<String[]>>) uiItems.clone());
	}


	/**
	 * Sets default content for the Statistic view.
	 */
	public void setDefaultContent() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				memoryTable.clearAll();
				TableItem oneItem = memoryTable.getItem(0);
				String[] data = new String[6];
				data[0] = Constants.STATISTICS_NO_STATS;
				data[1] = noResults;
				data[2] = noResults;
				data[3] = noResults;
				data[4] = noResults;
				data[5] = noResults;
				oneItem.setText(data);
			}
		});
	}

	/**
	 * Stores results to cache
	 *
	 * @param sortedFiles
	 *            Sorted statistic results
	 * @param runIndex
	 *            Selected run index
	 */
	@SuppressWarnings("unchecked")
	private void storeResults(AbstractList<SourceFile> sortedFiles, int runIndex) {
		int founds = 0;
		AbstractList<String[]> usedItems = new ArrayList<String[]>();
		Iterator<SourceFile> iterSourceFile = sortedFiles.iterator();

		//thru sorted source files
		//create string table for each found source file because memoryTable use string tables when inserting items
		while (iterSourceFile.hasNext()) {
			SourceFile tempSrcFile = iterSourceFile.next();
			String[] sdata = new String[6];
			sdata[0] = tempSrcFile.getFileName();
			sdata[1] = tempSrcFile.getFunctionName();
			sdata[2] = Integer.toString(tempSrcFile.getLineNumber());
			sdata[3] = Integer.toString(tempSrcFile.getHowManyTimes());
			String time = converTime(tempSrcFile.getTime());
			sdata[4] = time;
			sdata[5] = Integer.toString(tempSrcFile.getSize());

			//add time to cache
			//when sorting UI items with time we using the timeCache
			try {
				timeCache.put(time, tempSrcFile.getTime());
			} catch (java.lang.NumberFormatException nfe) {
				// do nothing by design
			}

			usedItems.add(sdata);
			founds++;
		}

		//if no items => set default content to UI
		if (founds == 0) {
			String[] data = new String[6];
			data[0] = Constants.STATISTICS_NO_STATS;
			data[1] = noResults;
			data[2] = noResults;
			data[3] = noResults;
			data[4] = noResults;
			data[5] = noResults;
			usedItems.add(data);
		}

		// add pinpointed info to the cache
		cache.put(runIndex, usedItems);
		projectCache.put(currProject,
				(HashMap<Integer, AbstractList<String[]>>) cache.clone());
	}

	/**
	 * Updates cache for match project changes.
	 */
	@SuppressWarnings("unchecked")
	public void updateCache() {

		//project cache contains result for selected project
		if (projectCache.containsKey(currProject)) {

			//clear existing caches
			cache.clear();

			//update cache
			cache = (HashMap<Integer, AbstractList<String[]>>) projectCache
					.get(currProject).clone();
		}
	}

	/**
	 * Updates build target and platform information.
	 */
	public void updateTargetInfo() {
		if (currProject == null) {
			return;
		}
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(currProject);
		if (cpi != null) {
			String platform = cpi.getDefaultConfiguration().getPlatformString();
			String buildTarget = cpi.getDefaultConfiguration()
					.getTargetString();
			infoLabel.setText("Platform: " + platform + "       Build target: "
					+ buildTarget);
		}
	}

	/**
	 * Updates statistic view with entered elements
	 *
	 * @param usedItems
	 *            Elements to UI
	 * @param notify Notify column to sort items
	 */
	public void updateView(final AbstractList<String[]> usedItems, final boolean notify) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				memoryTable.clearAll();

				if (usedItems.isEmpty()) {
					memoryTable.setItemCount(1);
					TableItem item = memoryTable.getItem(0);
					String[] data = new String[6];
					data[0] = Constants.STATISTICS_NO_STATS;
					data[1] = noResults;
					data[2] = noResults;
					data[3] = noResults;
					data[4] = noResults;
					data[5] = noResults;
					item.setText(data);
					return;
				}
				memoryTable.setItemCount(usedItems.size());

				Iterator<String[]> iterItems = usedItems.iterator();
				int counter = 0;
				while (iterItems.hasNext()) {
					String[] text = iterItems.next();
					TableItem item = memoryTable.getItem(counter);
					item.setText(text);
					counter++;
				}
				updateTargetInfo();
				doPack();
				if( notify ) {
					columnCount.notifyListeners(SWT.Selection, null);
					storeSortedItems(selectedRun);

				}
				combo.setEnabled(true);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		//this method is overwrite method of SelectionListener
		//and AT do not listens widgetDefaultSelected events
	}

	/**
	 * Checks which component is used and perform actions
	 *
	 * @param arg0
	 *            Selection event
	 */
	public void widgetSelected(SelectionEvent arg0) {

		// get selected table item
		if (arg0.item instanceof TableItem) {
			selectedItem = (TableItem) arg0.item;
		} else if (arg0.getSource() instanceof Combo) {
			Combo c = (Combo) arg0.getSource();
			selectedRun = c.getSelectionIndex();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					compareAndPrint(false);
				}
			});
		} else if (arg0.getSource() instanceof Button) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					compareAndPrint(false);
				}
			});
		}
	}
}
