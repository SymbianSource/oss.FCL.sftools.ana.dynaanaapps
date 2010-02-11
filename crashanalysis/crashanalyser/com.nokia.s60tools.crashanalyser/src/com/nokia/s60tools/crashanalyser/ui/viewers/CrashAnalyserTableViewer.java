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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Table;

/**
 * Base table viewer for table viewers used in Crash Analyser project.
 * Providers tooltip functionality and settings for all table viewers. 
 *
 */
public class CrashAnalyserTableViewer extends TableViewer {
	CellLabelProvider labelProvider;
	protected CrashAnalyserTableViewer(Table table) {
		super(table);
		ColumnViewerToolTipSupport.enableFor(this,ToolTip.NO_RECREATE);
		
		labelProvider = new CellLabelProvider() {

			public String getToolTipText(Object element) {
				if ("".equals(element.toString()))
					return null;
				else
					return element.toString();
			}

			public Point getToolTipShift(Object object) {
				return new Point(10, 10);
			}

			public int getToolTipDisplayDelayTime(Object object) {
				return 200;
			}

			public int getToolTipTimeDisplayed(Object object) {
				return 5000;
			}

			public void update(ViewerCell cell) {
				cell.setText(cell.getElement().toString());
			}
		};		
		
	}
}
