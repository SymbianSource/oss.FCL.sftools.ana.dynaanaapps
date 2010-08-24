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
 * Description:  Definitions for the class RunResults
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Stores one run related results (these results are created to link multiple
 * XML results file).
 * 
 * @author kihe
 * 
 */
public class RunResults extends ResultsBase {

	/** Run subtests. */
	private final AbstractList<Subtest> subtests;

	/**
	 * Constructor.
	 * 
	 * @param newRunID
	 *            Run ID
	 */
	public RunResults(final int newRunID) {
		super(newRunID);
		subtests = new ArrayList<Subtest>();
	}

	/**
	 * Adds a new subtest to existing subtest list.
	 * 
	 * @param subtest
	 *            subtest
	 */
	public final void addSubtest(final Subtest subtest) {
		subtests.add(subtest);
	}

	/**
	 * Gets current run subtests.
	 * 
	 * @return subtests
	 */
	public final AbstractList<Subtest> getSubtest() {
		return subtests;
	}
}
