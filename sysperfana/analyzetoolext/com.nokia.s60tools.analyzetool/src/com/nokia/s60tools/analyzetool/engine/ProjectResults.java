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
 * Description:  Definitions for the class ProjectResults
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Util;

/**
 * Stores project related memory analysis results.
 *
 * @author kihe
 *
 */
public class ProjectResults {

	/** Contains project related results. */
	private final Hashtable<IProject, AbstractList<RunResults>> projRelatedRes;

	/** Contains project related data file path and name. */
	private final Hashtable<IProject, String> projDataFile;

	/** Contains list of project unknown componets */
	private final Hashtable<IProject, AbstractList<String>> projectUnknownComp;


	/**
	 * Constructor.
	 */
	public ProjectResults() {
		projRelatedRes = new Hashtable<IProject, AbstractList<RunResults>>();
		projDataFile = new Hashtable<IProject, String>();
		projectUnknownComp = new Hashtable<IProject, AbstractList<String>>();
	}

	/**
	 * Clears all the stored data.
	 */
	public final void clear() {
		projDataFile.clear();
		projRelatedRes.clear();
		projectUnknownComp.clear();
	}

	/**
	 * Clears given project data.
	 *
	 * @param project
	 *            Project reference
	 */
	public final void clearProjectData(final IProject project) {
		// existing data contains project => delete it
		if (projRelatedRes.containsKey(project)) {
			projRelatedRes.remove(project);
		}

		// delete data file
		if (projDataFile.containsKey(project)) {
			projDataFile.remove(project);
		}

		// remove project unknown components
		if(projectUnknownComp.containsKey(project)) {
			projectUnknownComp.remove(project);
		}
	}

	/**
	 * Checks is there stored results available for the given project reference.
	 *
	 * @param project
	 *            Project reference
	 * @return True if project contains results, otherwise False
	 */
	public final boolean contains(final IProject project) {
		if (project == null) {
			return false;
		}
		return projRelatedRes.containsKey(project);
	}

	/**
	 * Gets project related data file name.
	 *
	 * @param projRef
	 *            Project reference
	 * @return Data file or null
	 */
	public final String getDataFileName( final IProject projRef) {

		if( projDataFile == null || projDataFile.isEmpty() ) {
			return null;
		}
		// if selected project contains data file
		if (projDataFile.containsKey(projRef)) {
			// thru projects
			for (java.util.Enumeration<IProject> e = projDataFile.keys(); e
					.hasMoreElements();) {
				// get project ref
				IProject key = e.nextElement();

				// if project ref equals selected project
				if (key.equals(projRef)) {
					// return data file name
					return projDataFile.get(key);
				}
			}
		}

		return null;
	}

	/**
	 * Gets project results by project reference.
	 *
	 * @param projRef
	 *            Project reference
	 * @return Project moduleresults
	 */
	public final AbstractList<RunResults> getResults(final IProject projRef) {
		// if selected project contains results
		if (projRelatedRes.containsKey(projRef)) {
			// thru projects
			for (java.util.Enumeration<IProject> e = projRelatedRes.keys(); e
					.hasMoreElements();) {
				// get project ref
				IProject key = e.nextElement();

				// if project ref equals selected project
				if (key.equals(projRef)) {
					// return project results
					return projRelatedRes.get(key);
				}
			}
		}
		// project does not contain existing results
		// =>just return empty results
		return new ArrayList<RunResults>();
	}

	/**
	 * Gets project run results by run ID.
	 *
	 * @param project
	 *            Project reference
	 * @param runID
	 *            Given run ID
	 * @return Project run results if exists, otherwise null
	 */
	public final RunResults getRun(final IProject project, final int runID) {
		AbstractList<RunResults> runs = getResults(project);
		Iterator<RunResults> iterRuns = runs.iterator();
		while (iterRuns.hasNext()) {
			RunResults oneRunRes = iterRuns.next();
			if (oneRunRes.getItemID() == runID) {
				return oneRunRes;
			}
		}
		return null;
	}

	/**
	 * Gets project one AnalysisItem by run ID and leak ID.
	 *
	 * @param project
	 *            Project reference
	 * @param runID
	 *            Given run ID
	 * @param leakID
	 *            Given leak ID
	 * @return AlysisItem if exists, otherwise null
	 */
	public final AnalysisItem getSpecific(final IProject project, final int runID, final int leakID) {
		AbstractList<RunResults> runs = getResults(project);
		Iterator<RunResults> iterRuns = runs.iterator();
		while (iterRuns.hasNext()) {
			RunResults oneRunRes = iterRuns.next();
			if (oneRunRes.getItemID() == runID) {
				AbstractList<AnalysisItem> items = oneRunRes.getAnalysisItems();
				Iterator<AnalysisItem> iterItems = items.iterator();
				while (iterItems.hasNext()) {
					AnalysisItem oneItem = iterItems.next();
					if (oneItem.getID() == leakID) {
						return oneItem;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets subtest info for given project and given run.
	 *
	 * @param project
	 *            Project reference
	 * @param runID
	 *            Current run ID
	 * @param leakID
	 *            Current leak ID
	 * @param subtestID
	 *            Subtest ID
	 * @return Subtest if it exists otherwise null
	 */
	public final AnalysisItem getSubtestItem(final IProject project, final int runID, final int leakID,
			final int subtestID) {
		// get project runs
		AbstractList<RunResults> runs = getResults(project);
		Iterator<RunResults> iterRuns = runs.iterator();

		// thru runs
		while (iterRuns.hasNext()) {
			RunResults oneRunRes = iterRuns.next();

			// run ID equals given ID
			if (oneRunRes.getItemID() == runID) {
				// get run subtest
				AbstractList<Subtest> subtests = oneRunRes.getSubtest();
				Iterator<Subtest> iterSubtests = subtests.iterator();
				while (iterSubtests.hasNext()) {
					Subtest oneSubtest = iterSubtests.next();

					// if subtest ID equals with given ID return it
					if (oneSubtest.getItemID() == subtestID) {
						return oneSubtest.getItemByID(leakID);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Set project used data file name and path.
	 *
	 * @param projectRef
	 *            Project reference
	 * @param newDataFileName
	 *            Project used data file name
	 */
	public final void setDataFileName(final IProject projectRef, final String newDataFileName) {
		// store data file name
		projDataFile.put(projectRef, newDataFileName);
	}

	/**
	 * Updates/stores project related memory analysis results.
	 *
	 * @param projRef
	 *            Project reference
	 * @param runResults
	 *            Run results
	 * @param dataFile
	 *            Used data file name and path
	 */
	public final void updateRunResults(final IProject projRef,
			final AbstractList<RunResults> runResults, final String dataFile) {
		// store data
		projRelatedRes.put(projRef, runResults);

		// set project used data file name and path
		setDataFileName(projRef, dataFile);
	}


	/**
	 * Sets project modules, also creating the unknown component list
	 * @param project Project reference
	 * @param mmps Project mmp's
	 * @param modules List of the modules.
	 */
	public void setProjectModules(IProject project, AbstractList<MMPInfo> mmps, AbstractList<String> modules)
	{
		try{
			if( modules.isEmpty() || mmps.isEmpty() ) {
				return;
			}
			Iterator<String> iterModules = modules.iterator();
			AbstractList<String> unknownComponents = new ArrayList<String>();
			while( iterModules.hasNext() ) {
				String moduleName = iterModules.next();
				boolean build = Util.isModulePartOfProject(mmps, moduleName);
				if( !build ) {
					unknownComponents.add(moduleName);
				}
			}
			setProjectUnknownModules(project, unknownComponents);
		}
		catch(Exception e)
		{
			Activator.getDefault().log(IStatus.ERROR, "Can not set project modules", e);
		}
		
		
	}

	/**
	 * Stores project unknown modules.
	 * @param project Project reference
	 * @param modules Project unknown modules
	 */
	private void setProjectUnknownModules(IProject project, AbstractList<String> modules) {

		//update list only it is empty
		if( !projectUnknownComp.containsKey(project) ) {
			projectUnknownComp.put(project, modules);
			return;
		}

		AbstractList<String> existingModules = projectUnknownComp.get(project);
		if( existingModules == null || existingModules.isEmpty() ) {
			projectUnknownComp.put(project, modules);
		}
	}

	/**
	 * Returns project unknown modules.
	 * @param project Project reference
	 * @return If project contains unknown modules returns unknown component list otherwise empty list
	 */
	public AbstractList<String> getProjectUnknownModules(IProject project){
		if( projectUnknownComp == null || projectUnknownComp.isEmpty() ) {
			return new ArrayList<String>();
		}
		if( projectUnknownComp.containsKey(project)) {
			return projectUnknownComp.get(project);
		}

		//no unknown components found for given project => return empty list
		return new ArrayList<String>();
	}

	/**
	 * Updates project unknown components list
	 * @param project Project reference
	 * @param targets Project targets
	 */
	public void updateUnknownModulesList(final IProject project, final AbstractList<MMPInfo> targets) {
		if( project != null && project.isOpen() ) {
			AbstractList<String> unkModules = projectUnknownComp.get(project);
			if( unkModules == null || unkModules.isEmpty() ) {
				return;
			}

			if( targets == null || targets.isEmpty() ) {
				return;
			}
			Iterator<MMPInfo> iterTargets = targets.iterator();

			while(iterTargets.hasNext()) {
				MMPInfo oneInfo = iterTargets.next();
				if( oneInfo == null ) {
					continue;
				}
				String oneModule = oneInfo.getTarget();
				if( oneModule == null || ("").equals(oneModule ) ) {
					continue;
				}

				if(unkModules.contains(oneModule.toLowerCase(Locale.US)) ) {
					unkModules.remove(oneModule.toLowerCase(Locale.US));
				}
			}

			projectUnknownComp.put(project,unkModules);
		}
	}
}
