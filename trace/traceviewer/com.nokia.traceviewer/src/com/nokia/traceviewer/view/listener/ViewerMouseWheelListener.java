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
 * Viewer Mouse Wheel Listener class
 *
 */
package com.nokia.traceviewer.view.listener;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Viewer Mouse Wheel Listener class
 * 
 */
public class ViewerMouseWheelListener implements Listener {

	/**
	 * The scroller object in the view
	 */
	private final Slider slider;

	/**
	 * The viewer object in the view
	 */
	private final TextViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param slider
	 *            reference to the slider
	 * @param viewer
	 *            reference to the viewer
	 */
	public ViewerMouseWheelListener(Slider slider, TextViewer viewer) {
		this.slider = slider;
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
	 * Event)
	 */
	public void handleEvent(Event e) {
		int showingTracesFrom = TraceViewerGlobals.getTraceViewer().getView()
				.getShowingTracesFrom();

		// If there is data left in the up or in the bottom of the current view,
		// move the slider to where top index is
		if ((viewer.getTopIndex() > e.count)
				&& (viewer.getTopIndex() + viewer.getBottomIndex()
						- viewer.getTopIndex() + 2 < viewer.getTextWidget()
						.getLineCount())) {

			slider.setSelection(showingTracesFrom + viewer.getTopIndex());

		} else {
			slider.setSelection(slider.getSelection() - e.count);
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SCROLLING_WITH_SCROLLBAR);
		}

		// Scroll view to correct line
		TraceViewerGlobals.getTraceViewer().getView().scrollViewToLine(
				slider.getSelection());
	}

}
