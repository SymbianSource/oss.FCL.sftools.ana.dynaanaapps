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
 * Interface for filter rule objects
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import com.nokia.traceviewer.engine.TraceProperties;

/**
 * Interface for filter rule objects
 * 
 */
public interface FilterRuleObject {

	/**
	 * Tells that this filter rule object is inside a logical NOT
	 * 
	 * @return true if object is inside a logical NOT
	 */
	public boolean isLogicalNotRule();

	/**
	 * Sets this filter rule object to NOT condition
	 * 
	 * @param notRule
	 *            the new status of NOT condition
	 */
	public void setLogicalNotRule(boolean notRule);

	/**
	 * Processes this filter rule
	 * 
	 * @param properties
	 *            trace properties
	 * @return true if filter rule hits, false otherwise
	 */
	public boolean processRule(TraceProperties properties);
}
