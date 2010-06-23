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
 * Scroller Selection Listener class
 *
 */
package com.nokia.traceviewer.view.listener;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Slider;

import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Scroller Selection Listener listens changes in view's scroller
 */
public class ScrollerSelectionListener implements SelectionListener {

	/**
	 * Text viewer
	 */
	private final TextViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            the viewer
	 */
	public ScrollerSelectionListener(TextViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Slider slider = (Slider) e.getSource();

		TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
				StateHolder.State.SCROLLING_WITH_SCROLLBAR);

		// Scroll to new line
		TraceViewerGlobals.getTraceViewer().getView().scrollViewToLine(
				slider.getSelection());

		viewer.setTopIndex(slider.getSelection()
				- TraceViewerGlobals.getTraceViewer().getView()
						.getShowingTracesFrom());

	}

}
