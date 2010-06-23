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
 * Interface implemented by the property view of Trace Viewer
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Interface implemented by the property view of TraceViewer
 */
public interface TraceViewerPropertyViewInterface extends
		TraceViewerViewInterface {

	/**
	 * Updates property tables
	 */
	public void updatePropertyTables();

	/**
	 * Updated trace comments
	 */
	public void updateTraceComments();

	/**
	 * Creates new propertytable items
	 */
	public void createNewPropertyTableItems();

	/**
	 * Sets linecount table changed
	 */
	public void setLineCountTableChanged();

	/**
	 * Sets variable tracing table changed
	 */
	public void setVariableTracingTableChanged();

	/**
	 * Gets selected variableTable indices
	 * 
	 * @return indices of selected variable items
	 */
	public int[] getSelectedVariableIndices();
}
