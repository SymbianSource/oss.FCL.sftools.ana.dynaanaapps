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
 * Description:  Definitions for the class FileAction
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import org.eclipse.jface.action.Action;

/**
 * Expand Action class functionality.
 *
 * @author kihe
 *
 */
public class FileAction extends Action {

	/** data file location. */
	private String fileLocation;

	/**
	 * Gets file location.
	 *
	 * @return File location
	 */
	public final String getFileLocation() {
		return fileLocation;
	}

	/**
	 * Sets file location.
	 *
	 * @param fileLoc
	 *            File location
	 */
	public final void setFileLocation(final String fileLoc) {
		fileLocation = fileLoc;
	}
}
