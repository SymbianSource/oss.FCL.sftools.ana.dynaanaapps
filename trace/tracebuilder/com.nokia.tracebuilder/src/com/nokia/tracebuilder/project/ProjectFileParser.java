/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Base class for project file parsers
*
*/
package com.nokia.tracebuilder.project;

import java.io.File;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.FileErrorParameters;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Base class for project file parsers
 * 
 */
public abstract class ProjectFileParser {

	/**
	 * Trace model
	 */
	protected TraceModel model;

	/**
	 * Project file to be parsed
	 */
	protected File projectFile;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 * @param fileName
	 *            the project file name
	 * @throws TraceBuilderException
	 *             if file does not exist
	 */
	protected ProjectFileParser(TraceModel model, String fileName)
			throws TraceBuilderException {
		File file = new File(fileName);
		if (file.exists()) {
			this.model = model;
			this.projectFile = file;
			createParser();
		} else {
			FileErrorParameters params = new FileErrorParameters();
			params.file = fileName;
			throw new TraceBuilderException(
					TraceBuilderErrorCode.FILE_NOT_FOUND, params);
		}
	}

	/**
	 * Creates the file parser
	 * 
	 * @throws TraceBuilderException
	 *             if creation fails
	 */
	protected abstract void createParser() throws TraceBuilderException;

	/**
	 * Parses the project file
	 * 
	 * @throws TraceBuilderException
	 *             if parser fails
	 */
	public abstract void parse() throws TraceBuilderException;

}
