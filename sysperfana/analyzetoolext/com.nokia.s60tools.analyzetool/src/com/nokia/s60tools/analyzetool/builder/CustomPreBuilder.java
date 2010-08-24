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
 * Description:  Definitions for the class CustomPreBuilder
 *
 */

package com.nokia.s60tools.analyzetool.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.builder.CarbideCommandLauncher;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Class to execute AnalyzeTool custom pre-build actions.
 * 
 * This class holds many static variables because {@link CustomPostBuilder}
 * class need to know what variables is used when calling CustomPreBuilder
 * without creating a new object of this class
 * 
 * @author kihe
 * 
 */
public class CustomPreBuilder extends AnalyzeToolBuilder {

	/**
	 * Executes AnalyzeTool post actions when user builds projects.
	 * 
	 * @see com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected IProject[] build(final int kind, final java.util.Map args,
			final IProgressMonitor monitor) {
		// clear used build parameters
		if (mmpFiles != null) {
			mmpFiles.clear();
		}
		IPreferenceStore store = Activator.getPreferences();
		store.setValue(Constants.PREFS_BUILD_CANCELLED, false);

		// get selected project reference
		final IProject project = super.getProject();

		// get project info
		final ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(project);

		final CarbideCommandLauncher launcher = new CarbideCommandLauncher(
				project, monitor, Constants.atoolParserIds, cpi
						.getINFWorkingDirectory());
		launcher.showCommand(true);

		if (!runPreSteps(launcher, monitor, cpi)) {
			return null;
		}

		runBuild(Constants.ATOOL_INST, monitor, cpi, null);

		return new IProject[0];
	}

	@Override
	protected void clean(final IProgressMonitor arg0) {
		// DO nothing by design
	}
}
