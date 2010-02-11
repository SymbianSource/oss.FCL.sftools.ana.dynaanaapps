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
 * Description:  Definitions for the class Subtest
 *
 */

package com.nokia.s60tools.analyzetool.engine;

/**
 * Contains information of subtest.
 *
 * @author kihe
 *
 */
public class Subtest extends ResultsBase {

	/** Subtest name. */
	private String name = null;

	/**
	 * Constructor.
	 *
	 * @param id Subtest ID
	 */
	public Subtest(final int id) {
		super(id);
	}

	/**
	 * Gets current subtest name.
	 *
	 * @return Subtest name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets current subtest name.
	 *
	 * @param newName
	 *            New subtest name
	 */
	public final void setName(final String newName) {
		name = newName;
	}

}
