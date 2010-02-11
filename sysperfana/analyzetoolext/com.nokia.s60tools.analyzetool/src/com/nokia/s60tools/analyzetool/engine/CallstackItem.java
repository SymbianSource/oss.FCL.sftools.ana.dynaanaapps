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
 * Description:  Definitions for the class CallstackItem
 *
 */

package com.nokia.s60tools.analyzetool.engine;

/**
 * Contains information of one memory leak item Information is parsed from
 * atool.exe generated XML file so we can assume that all the information is
 * valid and no other checking is needed.
 *
 * @author kihe
 *
 */
public class CallstackItem extends BaseItem {

	/** File name. */
	private String fileName;

	/** Function name. */
	private String functionName;

	/** Memory leak line number. */
	private int leakLineNumber;

	/** Flag to inform that is results created for urel builds. */
	private boolean urelBuild = false;


	/**
	 * Gets file name.
	 *
	 * @return File name
	 */
	public final String getFileName() {
		return this.fileName;
	}

	/**
	 * Gets function name.
	 *
	 * @return Function name
	 */
	public final String getFunctionName() {
		return this.functionName;
	}

	/**
	 * Gets leak line number.
	 *
	 * @return Leak line number
	 */
	public final int getLeakLineNumber() {
		return this.leakLineNumber;
	}

	/**
	 * Is results created for the urel build.
	 *
	 * @return True if results are created for the urel build otherwise false
	 */
	public final boolean isUrelBuild() {
		return urelBuild;
	}

	/**
	 * Sets cpp file name which contains memory leaks.
	 *
	 * @param newFileName
	 *            File name
	 */
	public final void setFileName(final String newFileName) {
		this.fileName = newFileName;
	}

	/**
	 * Sets function name.
	 *
	 * @param newFunctionName
	 *            Function name
	 */
	public final void setFunctionName(final String newFunctionName) {
		this.functionName = newFunctionName;
	}

	/**
	 * Sets leak line number.
	 *
	 * @param newLeakLineNumber
	 *            Leak line number
	 */
	public final void setLeakLineNumber(final int newLeakLineNumber) {
		this.leakLineNumber = newLeakLineNumber;
	}

	/**
	 * Sets urel build flag.
	 *
	 * @param build
	 *            Is project built with urel command
	 */
	public final void setUrelBuild(final boolean build) {
		urelBuild = build;
	}

	/**
	 * Checks that at least one needed information is available
	 *
	 * @return False if at least one needed information is available(not empty) otherwise
	 *         True
	 */
	public boolean isEmpty() {
		if ( !checkData() && (fileName == null || ("").equals(fileName))
				&& (functionName == null || ("").equals(functionName) ) ) {
			return true;
		}
		return false;
	}
}
