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
 * Description:  Definitions for the class ActiveSubtests
 *
 */

package com.nokia.s60tools.analyzetool.ui;

/**
 * Holds information of one active subtest.
 *
 * @author kihe
 *
 */
public class ActiveSubtests {

	/** Active subtest name. */
	private final String name;

	/** Subtest target name. */
	private final String targetName;

	/** Subtest process id. */
	private final int processID;

	/**
	 * Constructor.
	 *
	 * @param subTestName
	 *            Subtest name
	 * @param subTestTargetName
	 *            Subtest target name
	 * @param newProcessID
	 *            Subtest process id
	 */
	public ActiveSubtests(final String subTestName, final String subTestTargetName,
			final int newProcessID) {
		name = subTestName;
		targetName = subTestTargetName;
		processID = newProcessID;
	}

	/**
	 * Gets active subtest name.
	 *
	 * @return Subtest name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets active subtest process id.
	 *
	 * @return Process id
	 */
	public final String getProcessID() {
		return Integer.toString(processID ,16);
	}

	/**
	 * Gets active subtest target name.
	 *
	 * @return Target name
	 */
	public final String getTargetName() {
		return targetName;
	}
}
