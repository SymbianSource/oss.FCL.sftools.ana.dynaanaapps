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
 * Description:  Definitions for the class ResultsBase
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Base class for {@link ProjectResults} and {@link RunResults} Provides
 * functionality to store basic information of one results item.
 *
 * Information is parsed from atool.exe generated XML file so we can assume that
 * all the information is valid and no other checking is needed.
 *
 * @author kihe
 *
 */
public class ResultsBase {

	/** Run id. */
	private final int itemID;

	/** Run end time. */
	private String endTime;

	/** Run start time. */
	private String startTime;

	/** Contains information of current run memory leak count for modules. */
	private final Hashtable<String, Integer> moduleLeaks;

	/** Contains information of current run handle leak count for modules. */
	private final Hashtable<String, Integer> handleLeaks;

	/** Run process name. */
	private String processName;

	/** Run build target. */
	private String buildTarget;

	/** Run Analysis items. */
	private final AbstractList<AnalysisItem> analysisItems;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            Results id number
	 */
	public ResultsBase(final int id) {
		itemID = id;
		this.moduleLeaks = new Hashtable<String, Integer>();
		this.handleLeaks = new Hashtable<String, Integer>();
		analysisItems = new ArrayList<AnalysisItem>();
	}

	/**
	 * Adds one item to the existing list.
	 *
	 * @param item
	 *            Analysis item
	 */
	public final void addAnalysisItem(final AnalysisItem item) {
		analysisItems.add(item);
	}

	/**
	 * Adds module handle leak information.
	 *
	 * @param name
	 *            Module name
	 * @param count
	 *            Number of handle leaks
	 */
	public final void addHandleLeak(final String name, final int count) {
		handleLeaks.put(name, count);
	}

	/**
	 * Adds module memory leak information.
	 *
	 * @param name
	 *            Module name
	 * @param count
	 *            Number of module leaks
	 */
	public final void addModuleLeak(final String name, final int count) {
		moduleLeaks.put(name, count);
	}

	/**
	 * Gets run Analysis items.
	 *
	 * @return Run Analysis items
	 */
	public final AbstractList<AnalysisItem> getAnalysisItems() {
		return analysisItems;
	}

	/**
	 * Gets current run build target.
	 *
	 * @return Build target
	 */
	public final String getBuildTarget() {
		if( buildTarget == null) {
			return "";
		}
		return buildTarget;
	}

	/**
	 * Gets the end time of current analysis run.
	 *
	 * @return End time
	 */
	public final String getEndTime() {
		if(endTime == null) {
			return "";
		}
		return this.endTime;
	}

	/**
	 * Gets handle leak count.
	 *
	 * @return Handle leak count
	 */
	public final int getHandleLeakCount() {
		int count = 0;
		if (handleLeaks != null) {
			for (java.util.Enumeration<String> e = handleLeaks.keys(); e
					.hasMoreElements();) {
				String handleLeakName = e.nextElement();
				count = count + handleLeaks.get(handleLeakName);
			}
		}
		return count;

	}

	/**
	 * Gets current run module handle leak information.
	 *
	 * @return Hashtable<String, Integer> Module handle leak information
	 */
	public final Hashtable<String, Integer> getHandleLeaks() {
		return handleLeaks;
	}

	/**
	 * Gets one Analysis item by given id number.
	 *
	 * @param givenID
	 *            Analysis item id
	 * @return Analysis item if found otherwise null
	 */
	public final AnalysisItem getItemByID(final int givenID) {

		// get analysis items
		Iterator<AnalysisItem> itemIter = this.analysisItems.iterator();
		while (itemIter.hasNext()) {
			// get one analysis item
			AnalysisItem oneItem = itemIter.next();

			// if given id and current analysis item id match return current
			// analysis item
			if (oneItem.getID() == givenID) {
				return oneItem;
			}
		}
		return null;
	}

	/**
	 * Gets current run ID.
	 *
	 * @return Run ID
	 */
	public final int getItemID() {
		return itemID;
	}

	/**
	 * Gets current run module memory leak information.
	 *
	 * @return Hashtable<String, Integer> Module memory leak information
	 */
	public final Hashtable<String, Integer> getModuleLeaks() {
		return moduleLeaks;
	}

	/**
	 * Gets current run process name.
	 *
	 * @return Process name of the run
	 */
	public final String getProcessName() {
		if( processName == null ){
			return "";
		}
		return processName;
	}

	/**
	 * Gets the start time of current analysis run.
	 *
	 * @return Start time
	 */
	public final String getStartTime() {
		if( startTime == null ){
			return "";
		}
		return this.startTime;
	}

	/**
	 * Sets current run build target.
	 *
	 * @param target
	 *            Build target
	 */
	public final void setBuildTarget(final String target) {
		buildTarget = target;
	}

	/**
	 * Sets the end time of current analysis run.
	 *
	 * @param newEndTime
	 *            End time
	 */
	public final void setEndTime(final String newEndTime) {
		this.endTime = newEndTime;
	}

	/**
	 * Sets current run process name.
	 *
	 * @param newProcessName
	 *            Process name for the run
	 */
	public final void setProcessName(final String newProcessName) {
		processName = newProcessName;
	}

	/**
	 * Sets the start time of current analysis run.
	 *
	 * @param newStartTime
	 *            Start time
	 */
	public final void setStartTime(final String newStartTime) {
		this.startTime = newStartTime;
	}
}
