/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Enumeration containing the location amongst default DataProcessors
 *
 */
package com.nokia.traceviewer.api;

/**
 * Enumeration containing the location amongst default DataProcessors
 * 
 */
public enum DPLocation {

	/**
	 * Before decoder
	 */
	BEFORE_DECODER,

	/**
	 * After decoder
	 */
	AFTER_DECODER,

	/**
	 * Before filter
	 */
	BEFORE_FILTER,

	/**
	 * After filter
	 */
	AFTER_FILTER,

	/**
	 * Before view
	 */
	BEFORE_VIEW,

	/**
	 * After view
	 */
	AFTER_VIEW;
}
