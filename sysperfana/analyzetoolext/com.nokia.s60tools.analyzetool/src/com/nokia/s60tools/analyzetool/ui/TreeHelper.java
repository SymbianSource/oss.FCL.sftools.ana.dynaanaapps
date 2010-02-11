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
 * Description:  Definitions for the class TreeHelper
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import java.util.AbstractList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.analyzetool.engine.AnalysisItem;
import com.nokia.s60tools.analyzetool.engine.CallstackItem;
import com.nokia.s60tools.analyzetool.engine.MMPInfo;
import com.nokia.s60tools.analyzetool.engine.RunResults;
import com.nokia.s60tools.analyzetool.engine.Subtest;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;

/**
 * Class to create Tree model for the AnalyzeTool view.
 *
 * @author kihe
 *
 */
public class TreeHelper {


	/** Last active memory leak tree item. */
	private TreeObject activeTreeItem;

	/** Last selected tree item. */
	private final Object lastSelectedObject;

	/** Contains module leak info. */
	private final java.util.Hashtable<String, Integer> moduleLeaks;

	/** Used AnalyzeTool preference store */
	private final IPreferenceStore store;

	/** Contains already checked modules states */
	private final Hashtable<String, Boolean> checkedModules;

	/** Contains information which modules are part of the project */
	private final Hashtable<String, Boolean> projectModules;

	/**
	 * Constructor.
	 *
	 * @param selectedObject
	 *            Last selected tree item
	 *
	 * @param storeRef AnalyzeTool preference store reference
	 */
	public TreeHelper(final Object selectedObject,
			final IPreferenceStore storeRef) {
		lastSelectedObject = selectedObject;
		moduleLeaks = new java.util.Hashtable<String, Integer>();
		store = storeRef;
		checkedModules = new Hashtable<String, Boolean>();
		projectModules = new Hashtable<String, Boolean>();
	}

	/**
	 * Compares two different TreeObject.
	 *
	 * @param obj1
	 *            Actual
	 * @param obj2
	 *            New object
	 * @return True if objects equals otherwise false
	 */
	public final boolean compareItems(final Object obj1, final Object obj2) {
		boolean found = false;

		if (obj1 == null || obj2 == null) {
			return false;
		}

		TreeObject tree1 = (TreeObject) obj1;
		TreeObject tree2 = (TreeObject) obj2;

		if (tree1.getMemLeakID() == tree2.getMemLeakID()
				&& tree1.getRunID() == tree2.getRunID()
				&& tree1.getName().equals(tree2.getName())
				&& tree1.getSubtestID() == tree2.getSubtestID()) {
			activeTreeItem = (TreeObject) obj1;
			found = true;
		}
		return found;
	}

	/**
	 * Creates tree node for analysis items.
	 *
	 * @param oneRunTree
	 *            Current item parent tree node
	 * @param items
	 *            Analysis items
	 * @param runID
	 *            Current run id
	 * @param isSubtest
	 *            Is this element subtest
	 * @param subTestID
	 *            Subtest ID
	 * @param modules Selected project target modules
	 * @return How many items created
	 */
	public final int createItemResults(final TreeParent oneRunTree,
			final AbstractList<AnalysisItem> items, final int runID,
			final boolean isSubtest, final int subTestID, AbstractList<MMPInfo> modules) {
		boolean thereAreItemsDisplayed = false;
		moduleLeaks.clear();

		int displayed = 0;
		// thru actual memory leaks
		Iterator<AnalysisItem> iterItem = items.iterator();
		while (iterItem.hasNext()) {
			// get one analysis file
			AnalysisItem oneItem = iterItem.next();

			if (getActiveDetailLevel().equals(Constants.REPORT_EVERY)
					|| oneItem.containValidCallstack()) {
				displayed++;
				char space = ' ';
				StringBuffer leakString = new StringBuffer(64);
				leakString.append(Constants.ITEM_TREE_MEM_LEAKS);
				leakString.append(oneItem.getID());
				leakString.append(" (");
				leakString.append(oneItem.getLeakSize());
				leakString.append(" bytes) (");
				leakString.append(oneItem.getMemoryAddress());
				leakString.append(") ");
				leakString.append(oneItem.getMemoryLeakTime());
				leakString.append(space);
				leakString.append(oneItem.getModuleName());

				TreeObject oneLeak = new TreeObject();
				if (!modules.isEmpty()) {
					oneLeak.setBuild(checkItemBuildState(modules, oneItem.getModuleName()));
					oneLeak.setBelongs(checkProjectModules(modules, oneItem.getModuleName()));
				}

				if (isSubtest) {
					oneLeak.setSubtest(true);
					oneLeak.setSubtestID(subTestID);
				}
				oneLeak.setRunID(runID);
				oneLeak.setMemLeakID(oneItem.getID());
				oneLeak.setName(leakString.toString());
				oneLeak.setModuleName(oneItem.getModuleName());
				oneLeak.setMemAddress(oneItem.getMemoryAddress());

				oneRunTree.addChild(oneLeak);
				thereAreItemsDisplayed = true;
				updateModuleLeaksInfo(oneItem.getModuleName());
				compareItems(oneLeak, lastSelectedObject);
			}

		}

		// if no leaks added to list
		if (!thereAreItemsDisplayed && !items.isEmpty()) {
			TreeObject infoObject = new TreeObject();
			infoObject.setName(Constants.NO_MEM_LEAKS_CURRENT_LEVEL);
			oneRunTree.addChild(infoObject);
		}
		return displayed;
	}

	/**
	 * Check is module built with AnalyzeTool
	 * @param modules Project modules
	 * @param moduleName Module name
	 * @return True if module is built with AnalyzeTool otherwise false
	 */
	private final boolean checkItemBuildState( AbstractList<MMPInfo> modules, String moduleName)
	{
		if( checkedModules.containsKey(moduleName)) {
			return checkedModules.get(moduleName);
		}
		else if (!modules.isEmpty()) {
			boolean build = Util.chechModuleBuildState(modules, moduleName);
			if( !build ) {
				checkedModules.put(moduleName, build);
			}
			return build;
		}
		return false;
	}

	/**
	 * Checks is module part of the project
	 * @param modules Project modules
	 * @param moduleName Module name
	 * @return True if module is part of the project otherwise false
	 */
	private final boolean checkProjectModules(AbstractList<MMPInfo> modules, String moduleName)
	{
		if( projectModules.containsKey(moduleName) ) {
			return projectModules.get(moduleName);
		}
		else if( !modules.isEmpty() ) {
			boolean partOfTheProject = Util.isModulePartOfProject(modules,moduleName);
			projectModules.put(moduleName, partOfTheProject);
			return partOfTheProject;
		}
		return false;
	}

	/**
	 * Creates tree item for run results.
	 *
	 * @param oneRunResults
	 *            One run results
	 * @param modules Selected project target modules
	 * @return Tree item which contains run results
	 */
	public final TreeParent createRunResults(final RunResults oneRunResults, AbstractList<MMPInfo> modules) {

		// create new run tree
		TreeParent oneRunTree = new TreeParent("");
		oneRunTree.setRunID(oneRunResults.getItemID());

		// create new handle leak tree
		TreeParent handleSummary = getHandleLeakInfo(oneRunResults
				.getHandleLeaks(), oneRunResults.getItemID(), modules);
		if (handleSummary.hasChildren()) {
			oneRunTree.addChild(handleSummary);
		}

		// if no memory leaks set "No memory leaks" info
		AbstractList<AnalysisItem> items = oneRunResults.getAnalysisItems();
		if (items.isEmpty()) {
			TreeObject noLeaks = new TreeObject();
			noLeaks.setName(Constants.RUN_NO_LEAKS);
			oneRunTree.addChild(noLeaks);
		}

		// create item tree and calculate how many of memory leaks are displayed
		int howManyDisplayed = createItemResults(oneRunTree, items,
				oneRunResults.getItemID(), false, 0, modules);
		oneRunTree.setName(getRunTreeTitle(oneRunResults, howManyDisplayed));

		// module leaks
		TreeParent moduleSummary = getModuleLeakInfo(getCalcModuleLeaksInfo(),
				oneRunResults.getItemID(), modules);
		if (moduleSummary.hasChildren()) {
			oneRunTree.addChild(0, moduleSummary);
		}

		// get subtests
		AbstractList<Subtest> subtest = oneRunResults.getSubtest();
		createSubtestResults(oneRunTree, subtest, oneRunResults.getItemID(), modules);

		return oneRunTree;
	}

	/**
	 * Creates tree node for subtests.
	 *
	 * @param oneRunTree
	 *            Parent tree node
	 * @param subtest
	 *            Subtest information
	 * @param runID
	 *            Current run id
	 * @param modules Selected project target modules
	 */
	public final void createSubtestResults(final TreeParent oneRunTree,
			final AbstractList<Subtest> subtest, final int runID, AbstractList<MMPInfo> modules) {
		// thru subtest
		Iterator<Subtest> iterSubtest = subtest.iterator();

		// thru subtest
		while (iterSubtest.hasNext()) {
			// get subtest
			Subtest oneSubtest = iterSubtest.next();

			// create tree item for the subtest
			TreeParent oneSubtestParent = new TreeParent("");

			// get and calculate how many memory leaks subtest contains
			int howManyDisplayed = createItemResults(oneSubtestParent,
					oneSubtest.getAnalysisItems(), runID, true, oneSubtest
							.getItemID(), modules);

			// udpate subtest tree title
			oneSubtestParent.setName(getSubtestTreeTitle(oneSubtest,
					howManyDisplayed));

			if (oneSubtest.getAnalysisItems().isEmpty()) {
				TreeObject noLeaks = new TreeObject();
				noLeaks.setName(Constants.RUN_NO_LEAKS);
				oneSubtestParent.addChild(noLeaks);
			}

			// if subtest contains handle leaks
			TreeParent handleLeaksInfo = getHandleLeakInfo(oneSubtest
					.getHandleLeaks(), oneSubtest.getItemID(), modules);
			if (handleLeaksInfo.hasChildren()) {
				oneSubtestParent.addChild(handleLeaksInfo);
			}

			// set subtest module leak info
			TreeParent moduleLeaksInfo = getModuleLeakInfo(
					getCalcModuleLeaksInfo(), oneSubtest.getItemID(), modules);
			if (moduleLeaksInfo.hasChildren()) {
				oneSubtestParent.addChild(0, moduleLeaksInfo);
			}

			oneRunTree.addChild(oneSubtestParent);
		}
	}

	/**
	 * Gets active detail level.
	 *
	 * @return Active detail level
	 */
	public final String getActiveDetailLevel() {
		// get active report level
		return store.getString(Constants.REPORT_LEVEL);
	}

	/**
	 * Returns active item of tree.
	 *
	 * @return Active itemf
	 */
	public final TreeObject getActiveItem() {
		return activeTreeItem;
	}

	/**
	 * Returns tree item which contains callstack information.
	 *
	 * @param item
	 *            AnalysisItem
	 * @param modules Selected project target modules
	 * @return Tree item
	 */
	public final TreeParent getCallstack(final AnalysisItem item, AbstractList<MMPInfo> modules) {

		// get active logging mode
		String reportLevel = store.getString(Constants.REPORT_LEVEL);

		// create tree parent for test runs
		TreeParent testRuns = new TreeParent(Constants.ITEM_TREE_MEM_LEAKS
				+ item.getID() + " " + item.getMemoryLeakTime());

		// get callstack items
		Iterator<CallstackItem> stackItems = item.getCallstackItems()
				.iterator();

		// is one know line printed
		boolean printed = false;

		// thru call stack items
		while (stackItems.hasNext()) {
			// get one callstack item
			CallstackItem oneStackItem = stackItems.next();

			TreeObject to = new TreeObject();

			// get function name, memory leak line number and file name
			String functionName = oneStackItem.getFunctionName();
			int leakLineNumber = oneStackItem.getLeakLineNumber();
			String fileName = oneStackItem.getFileName();
			String memAddress = oneStackItem.getMemoryAddress();
			String moduleName = oneStackItem.getModuleName();

			if (!modules.isEmpty()) {
				to.setBuild(checkItemBuildState(modules, moduleName));
				to.setBelongs(checkProjectModules(modules, moduleName));
			}

			// report all
			if (Constants.REPORT_EVERY.equals(reportLevel)) {

				if (leakLineNumber > 0) {
					// if build is urel
					// => change info order
					if (oneStackItem.isUrelBuild()) {
						to.setName(memAddress + " " + moduleName + " "
								+ functionName + " " + leakLineNumber + " "
								+ fileName);
					} else {
						to.setName(memAddress + " " + moduleName + " "
								+ functionName + " " + fileName + " "
								+ leakLineNumber);
					}

				} else {
					to.setName(memAddress + " " + moduleName + " "
							+ functionName + " " + fileName);
				}

				to.setCallstackItem(oneStackItem);
				testRuns.addChild(to);
			}
			// display only known lines
			else if (Constants.REPORT_KNOWN.equals(reportLevel)
					&& (functionName != null && leakLineNumber > 0 && fileName != null)) {
				// if build is urel
				// => change info order
				if (oneStackItem.isUrelBuild()) {
					to.setName(memAddress + " " + moduleName + " "
							+ functionName + " " + leakLineNumber + " "
							+ fileName);
				} else {
					to.setName(memAddress + " " + moduleName + " "
							+ functionName + " " + fileName + " "
							+ leakLineNumber);
				}
				to.setCallstackItem(oneStackItem);
				testRuns.addChild(to);
			}
			// display only topmost
			else if (Constants.REPORT_TOPMOST.equals(reportLevel)
					&& !printed
					&& (functionName != null && leakLineNumber > 0 && fileName != null)) {
				// if build is urel
				// => change info order
				if (oneStackItem.isUrelBuild()) {
					to.setName(memAddress + " " + moduleName + " "
							+ functionName + " " + leakLineNumber + " "
							+ fileName);
				} else {
					to.setName(memAddress + " " + moduleName + " "
							+ functionName + " " + fileName + " "
							+ leakLineNumber);
				}
				to.setCallstackItem(oneStackItem);
				testRuns.addChild(to);
				printed = true;
			}

		}

		TreeParent incRoot;
		incRoot = new TreeParent(Constants.ANALYZE_TOOL_TITLE);
		incRoot.addChild(testRuns);
		return incRoot;
	}

	/**
	 * Gets handle leaks tree item.
	 *
	 * @param handleLeaks
	 *            Handle leaks information
	 * @param runID
	 *            Run id
	 * @param modules Selected project target modules
	 * @return Handle leak tree item
	 */
	public final TreeParent getHandleLeakInfo(
			final Hashtable<String, Integer> handleLeaks, final int runID, AbstractList<MMPInfo> modules) {
		// get handle leak information
		TreeParent handleSummary = new TreeParent(
				Constants.HANDLE_LEAK_MODULES_TITLE);
		handleSummary.setRunID(runID);
		for (java.util.Enumeration<String> e1 = handleLeaks.keys(); e1
				.hasMoreElements();) {
			String moduleName = e1.nextElement();
			int handleLeakCount = handleLeaks.get(moduleName);
			TreeObject object = new TreeObject();
			object.setRunID(runID);
			object.setName(moduleName + " (" + handleLeakCount
					+ Constants.MODULE_TREE_HANDLE_LEAKS + ")");
			if (!modules.isEmpty()) {
				object.setBuild(checkItemBuildState(modules, moduleName));
				object.setBelongs(checkProjectModules(modules, moduleName));
			}

			handleSummary.addChild(object);
		}
		return handleSummary;
	}

	/**
	 * Gets module leaks tree item.
	 *
	 * @param moduleLeaksInfo
	 *            Module leaks information
	 * @param runID
	 *            Run id
	 * @param modules Selected project target modules
	 * @return Module leak tree item
	 */
	public final TreeParent getModuleLeakInfo(
			final Hashtable<String, Integer> moduleLeaksInfo, final int runID, AbstractList<MMPInfo> modules) {
		TreeParent moduleSummary = new TreeParent(
				Constants.MEMORY_LEAK_MODULES_TITLE);

		moduleSummary.setRunID(runID);
		for (java.util.Enumeration<String> e = moduleLeaksInfo.keys(); e
				.hasMoreElements();) {
			String moduleName = e.nextElement();
			int moduleLeakCount = moduleLeaksInfo.get(moduleName);
			TreeObject object = new TreeObject();
			object.setRunID(runID);
			object.setName(moduleName + " (" + moduleLeakCount
					+ Constants.MODULE_TREE_MEM_LEAKS + ")");
			if (!modules.isEmpty()) {
				object.setBuild(checkItemBuildState(modules, moduleName));
				object.setBelongs(checkProjectModules(modules, moduleName));
			}

			moduleSummary.addChild(object);
		}
		return moduleSummary;
	}

	/**
	 * Return moduleaks info.
	 *
	 * @return Module leaks info
	 */
	public final Hashtable<String, Integer> getCalcModuleLeaksInfo() {
		return moduleLeaks;
	}

	/**
	 * Gets run tree title.
	 *
	 * @param oneRunResults
	 *            Run results
	 * @param howManyDisplayed
	 *            How many of memory leak items are displayed
	 * @return Run tree title
	 */
	public final String getRunTreeTitle(final RunResults oneRunResults,
			final int howManyDisplayed) {
		StringBuffer runTreeTitle = new StringBuffer(64);
		runTreeTitle.append(Constants.RUN_TREE_RUN);
		runTreeTitle.append(Integer.toString(oneRunResults.getItemID()));
		runTreeTitle.append(Constants.RUN_TREE_RUN_MEM_LEAKS);
		runTreeTitle.append(oneRunResults.getAnalysisItems().size());
		if (howManyDisplayed > 0
				&& oneRunResults.getAnalysisItems().size() != howManyDisplayed) {
			int diff = oneRunResults.getAnalysisItems().size()
					- howManyDisplayed;
			runTreeTitle.append(" (");
			runTreeTitle.append(Integer.toString(diff));
			runTreeTitle.append(Constants.RUN_TREE_FILTERED);

		}
		runTreeTitle.append(Constants.RUN_TREE_RUN_HANDLE_LEAKS);
		runTreeTitle.append(oneRunResults.getHandleLeakCount());
		runTreeTitle.append(Constants.RUN_TREE_START_TIME);
		runTreeTitle.append(oneRunResults.getStartTime());
		runTreeTitle.append(Constants.RUN_TREE_PROCESS_NAME);
		runTreeTitle.append(oneRunResults.getProcessName());
		runTreeTitle.append(Constants.RUN_TREE_BUILD_TARGET);
		runTreeTitle.append(oneRunResults.getBuildTarget());

		if (oneRunResults.getEndTime() == null
				|| ("").equals(oneRunResults.getEndTime())) {
			runTreeTitle.append(Constants.RUN_FAILED);
		} else if ((Constants.RUN_ABNORMAL).equals(oneRunResults.getEndTime())) {
			runTreeTitle.append(' ');
			runTreeTitle.append(Constants.RUN_ABNORMAL);
			runTreeTitle.append(' ');
		}

		return runTreeTitle.toString();
	}

	/**
	 * Gets subtest title.
	 *
	 * @param oneSubtest
	 *            Subtest info
	 * @param howManyDisplayed
	 *            How many of subtest items are displayed
	 * @return Subtest title
	 */
	public final String getSubtestTreeTitle(final Subtest oneSubtest,
			final int howManyDisplayed) {
		StringBuffer subtestTitle = new StringBuffer(64);
		String startTime = oneSubtest.getStartTime();
		subtestTitle.append(Constants.SUBTEST_TREE_TITLE);
		subtestTitle.append(oneSubtest.getName());
		subtestTitle.append(Constants.RUN_TREE_RUN_MEM_LEAKS);
		subtestTitle.append(oneSubtest.getAnalysisItems().size());

		if (howManyDisplayed > 0
				&& oneSubtest.getAnalysisItems().size() != howManyDisplayed) {
			int diff = oneSubtest.getAnalysisItems().size() - howManyDisplayed;

			subtestTitle.append(" (");
			subtestTitle.append(diff);
			subtestTitle.append(Constants.RUN_TREE_FILTERED);
		}
		subtestTitle.append(Constants.RUN_TREE_RUN_HANDLE_LEAKS);
		subtestTitle.append(oneSubtest.getHandleLeakCount());
		if (startTime.length() != 0) {
			subtestTitle.append(Constants.RUN_TREE_START_TIME);
			subtestTitle.append(oneSubtest.getStartTime());
		}
		return subtestTitle.toString();
	}

	/**
	 * Updates module leak count for current module.
	 *
	 * @param moduleName
	 *            Module name
	 */
	public final void updateModuleLeaksInfo(final String moduleName) {

		//check module name
		if( moduleName == null || ("").equals(moduleName) ) {
			return;
		}
		//if same module name is already added => update count value
		else if (moduleLeaks.containsKey(moduleName)) {
			int currentCount = moduleLeaks.get(moduleName);
			currentCount++;
			moduleLeaks.put(moduleName, currentCount);
		} else {
			moduleLeaks.put(moduleName, 1);
		}
	}
}
