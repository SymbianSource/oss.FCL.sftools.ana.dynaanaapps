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
*/

package com.nokia.s60tools.crashanalyser.ui.viewers;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;

/**
 * Table viewer for Code Segments table in Advanced page in 
 * Crash Visualiser editor. 
 *
 */
public class CodeSegmentsTableViewer extends CrashAnalyserTableViewer {
	/**
	 * Constructor
	 * @param table
	 */
	public CodeSegmentsTableViewer(Table table) {
		super(table);

		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Base Address");

		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Top Address");
		
		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("File Name");		
	}
}
