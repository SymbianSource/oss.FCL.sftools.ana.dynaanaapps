/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Trace project file manager
*
*/
package com.nokia.tracebuilder.engine.project;

import java.io.File;
import com.nokia.tracebuilder.engine.TraceBuilderEngine;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.plugin.TraceAPIPluginManager;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Trace project engine
 * 
 */
public final class ProjectEngine extends TraceBuilderEngine {

	/**
	 * Parameters for create project
	 * 
	 */
	private class CreateProjectParameters {

		/**
		 * Trace project path
		 */
		String traceProjectPath;

		/**
		 * Trace project name
		 */
		String traceProjectName;

		/**
		 * Trace project ID
		 */
		int traceProjectID;

	}

	/**
	 * Default project ID
	 */
	private static final int DEFAULT_PROJECT_ID = 0x0; // CodForChk_Dis_Magic

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Trace directory name
	 */
	public static String traceFolderName;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public ProjectEngine(TraceModel model) {
		this.model = model;
	}

	/**
	 * Opens trace project
	 * 
	 * @param projectPath
	 *            the path to the project to be opened
	 * @param modelName
	 *            the name for the model or null to use directory name
	 * @throws TraceBuilderException
	 *             if opening fails
	 */
	public void openTraceProject(String projectPath, String modelName)
			throws TraceBuilderException {
		if (projectPath != null) {
			CreateProjectParameters parameters = createParameters(projectPath,
					modelName);

			// Create empty project
			if (model.getExtension(TraceBuilderProject.class) == null) {
				createEmptyProjectFile(parameters);
			}
		} else {
			
			// If fileName is null, there's no open source files. In
			// that the project cannot be created
			throw new TraceBuilderException(
					TraceBuilderErrorCode.SOURCE_NOT_OPEN);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectOpened()
	 */
	@Override
	public void projectOpened() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectClosed()
	 */
	@Override
	public void projectClosed() {
		model.removeExtensions(TraceBuilderProject.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#exportProject()
	 */
	@Override
	public void exportProject() {
	}

	/**
	 * Creates the parameters for new project
	 * 
	 * @param projectPath
	 *            the project path
	 * @param projectName
	 *            the name for the project
	 * @return the parameters
	 */
	private CreateProjectParameters createParameters(String projectPath,
			String projectName) {
		CreateProjectParameters queryData = new CreateProjectParameters();
		queryData.traceProjectPath = projectPath + File.separator
				+ ProjectEngine.traceFolderName;
		queryData.traceProjectName = projectName;
		queryData.traceProjectID = DEFAULT_PROJECT_ID;
		return queryData;
	}

	/**
	 * Creates the project file from query results
	 * 
	 * @param queryData
	 *            the query result
	 */
	private void createEmptyProjectFile(CreateProjectParameters queryData) {
		model.setName(queryData.traceProjectName);
		model.setID(queryData.traceProjectID);
		String componentName = model.getName();
		TraceBuilderProject file = new TraceBuilderProject(
				queryData.traceProjectPath, componentName);
		createAPI(file);
	}

	/**
	 * Creates the project API
	 * 
	 * @param file
	 *            the project file
	 */
	private void createAPI(TraceBuilderProject file) {
		model.addExtension(file);
		TraceAPIPluginManager plugin = model
				.getExtension(TraceAPIPluginManager.class);
		plugin.createDefaultAPI();
	}
}
