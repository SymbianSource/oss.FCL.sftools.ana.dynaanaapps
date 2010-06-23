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
 * Window Resize Listener
 *
 */
package com.nokia.traceviewer.view.listener;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Slider;

/**
 * Window Resize Listener
 */
public class WindowResizeListener implements ControlListener {

	/**
	 * The slider in the view
	 */
	private final Slider slider;

	/**
	 * The TextViewer in the view
	 */
	private final TextViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param slider
	 *            slider of the view
	 * @param viewer
	 *            viewer of the view
	 */
	public WindowResizeListener(Slider slider, TextViewer viewer) {
		this.slider = slider;
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events
	 * .ControlEvent)
	 */
	public void controlMoved(ControlEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt
	 * .events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {
		slider.setThumb(viewer.getBottomIndex() - viewer.getTopIndex());
	}

}
