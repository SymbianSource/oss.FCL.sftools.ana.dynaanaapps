/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.pi.call;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
import org.eclipse.swt.graphics.GC;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;


public class CallTraceGraph extends GenericTraceGraph implements ActionListener, 
	PIEventListener {

	public CallTraceGraph(GenericTrace data) {
		super(data);
	}

	public void paint(Panel panel, Graphics graphics) {
	}

	public void repaint() {
	}

	public void refreshDataFromTrace() {
	}

	public void action(String action) {
	}

	public void actionPerformed(ActionEvent e) {
	}

	public void piEventReceived(PIEvent event) {
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
	}
}
