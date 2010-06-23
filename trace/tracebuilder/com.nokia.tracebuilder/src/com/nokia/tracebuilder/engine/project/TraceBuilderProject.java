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
* TraceBuilder project file properties
*
*/
package com.nokia.tracebuilder.engine.project;

import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.project.TraceProjectFile;
import com.nokia.tracebuilder.source.SourceConstants;

/**
 * TraceBuilder project file properties
 * 
 */
final class TraceBuilderProject extends TraceProjectFile {

	/**
	 * Title shown in UI
	 */
	private static final String TITLE = Messages
			.getString("TraceBuilderProject.Title"); //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param absolutePath
	 *            the path to the file, including file name
	 */
	TraceBuilderProject(String absolutePath) {
		super(absolutePath, true);
	}

	/**
	 * Constructor
	 * 
	 * @param path
	 *            the path to the file
	 * @param name
	 *            the file name
	 */
	TraceBuilderProject(String path, String name) {
		super(path, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#getFileExtension()
	 */
	@Override
	protected String getFileExtension() {
		return null;
	}

	/**
	 * Creates a project file path from path and name
	 * 
	 * @param path
	 *            the path
	 * @param name
	 *            the file name
	 * @return the absolute path
	 */
	static String createAbsolutePath(String path, String name) {
		StringBuffer sb = new StringBuffer();
		sb.append(FileUtils.convertSeparators(
				SourceConstants.FORWARD_SLASH_CHAR, path, true));
		sb.append(name);
		return sb.toString();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceProjectFile#getTitle()
	 */
	@Override
	public String getTitle() {
		return TITLE;
	}

}
