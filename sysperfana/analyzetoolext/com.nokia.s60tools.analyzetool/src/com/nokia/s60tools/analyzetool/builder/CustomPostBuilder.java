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
 * Description:  Definitions for the class CustomPostBuilder
 *
 */

package com.nokia.s60tools.analyzetool.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

//import com.nokia.carbide.cdt.builder.BuildArgumentsInfo;
import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
//import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;


/**
 * Class to execute custom post-build actions.
 *
 * @author kihe
 *
 */
public class CustomPostBuilder extends AnalyzeToolBuilder {

	/** Post-builder id. */
	public static final String POST_BUILDER_ID = "com.nokia.s60tools.analyzetool.analyzeToolPostBuilder";

	/**
	 * Executes AnalyzeTool post actions when user builds projects.
	 *
	 *
	 * @see com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, java.util.Map args,
			IProgressMonitor monitor) {

		// get project reference
		IProject project = super.getProject();

		// get project info
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);


		IPreferenceStore store = Activator.getPreferences();
		boolean buildCanceled = store.getBoolean(Constants.PREFS_BUILD_CANCELLED);
		// if the AnalyzeTool is canceled
		if (buildCanceled) {
			getCarbideCommandLauncher().writeToConsole(
					Constants.BUILD_CANCELLED);
			buildCanceled = false;
			// if atool.exe is not available write info to console view
			if (!Util.isAtoolAvailable()) {
				getCarbideCommandLauncher().writeToConsole(
						Constants.INFO_ATOOL_NOT_AVAILABLE);
			}
			super.forgetLastBuiltState();
			runPostSteps(cpi);
			return null;
		}


		//execute atool.exe
		if (CarbideCPPBuilder.projectHasBuildErrors(cpi.getProject())) {
			runUninstrument(Constants.ATOOL_UNINST_FAILED,cpi, monitor);
		} else {
			runUninstrument(Constants.ATOOL_UNINST,cpi, monitor);
		}


		monitor.worked(1);

		runPostSteps(cpi);

		return new IProject[0];
	}


	@Override
	protected void clean(final IProgressMonitor arg0){
		// DO nothing by design
	}
}
