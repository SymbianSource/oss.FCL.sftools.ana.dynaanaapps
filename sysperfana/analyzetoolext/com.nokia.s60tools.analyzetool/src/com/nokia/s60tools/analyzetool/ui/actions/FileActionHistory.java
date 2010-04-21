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
 * Description:  Definitions for the class FileActionHistory
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Contains history of what files are opened.
 *
 * @author kihe
 *
 */
public class FileActionHistory {

	/** File name. */
	private String fileName;

	/** List of opened files. */
	private final AbstractList<String> fileNameHistory;

	/** How many files to displayed. */
	private final int howManyItems;

	/**
	 * Constructor.
	 *
	 * @param howMany
	 *            How many file to display
	 */
	public FileActionHistory(final int howMany) {
		fileNameHistory = new ArrayList<String>();
		howManyItems = howMany;
	}

	/**
	 * Gets history items.
	 *
	 * @return List of history items
	 */
	public final AbstractList<String> getItems() {
		AbstractList<String> tmpFileList = new ArrayList<String>();
		Iterator<String> iterFiles = fileNameHistory.iterator();
		int index = 0;
		while (iterFiles.hasNext() && index < howManyItems) {
			tmpFileList.add(iterFiles.next());
			index++;
		}
		return tmpFileList;
	}

	/**
	 * Checks is list empty.
	 *
	 * @return True if list is empty otherwise False
	 */
	public final boolean isEmpty() {
		return fileNameHistory.isEmpty();
	}

	/**
	 * Removes file name from the list.
	 *
	 * @param usedFileName
	 *            Used file
	 */
	public final void removeFileName(final String usedFileName) {
		if (fileNameHistory.contains(usedFileName)) {
			fileNameHistory.remove(usedFileName);
			removeFileName(usedFileName);
		}
	}

	/**
	 * Sets file location and name.
	 *
	 * @param pathAndName
	 *            File location and name
	 */
	public final void setFileName(final String pathAndName) {
		if (!("").equals(pathAndName)) {
			// if list already contains same file name remove it
			if (fileNameHistory.contains(pathAndName)) {
				removeFileName(pathAndName);

			}
			fileName = pathAndName;

			// add file name to history list
			fileNameHistory.add(0, fileName);
		}
	}
}
