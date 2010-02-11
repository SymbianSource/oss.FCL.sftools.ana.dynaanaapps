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

package com.nokia.carbide.cpp.internal.pi.visual;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 *
 * A GraphComposite is added to the SashForm for a tab.
 * 
 * A GraphComposite has an option centered title, a left area to contain the y-axis
 * legend, and a scrollable area containing the graph and the x-axis legend.
 */
public class GraphComposite extends Composite
{
	public FigureCanvas leftLegend;
	public FigureCanvas figureCanvas;

	public GraphComposite(Composite parent, int style, String titleString)
	{
		super(parent, style);
		this.setLayout(new FormLayout());

       	FormData formData;
    	Label title = null;

       	if (titleString != null) {
			title = new Label(this, SWT.CENTER);
			title.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));
			title.setFont(PIPageEditor.helvetica_8);
			title.setText(titleString);

			formData = new FormData();
    		formData.top    = new FormAttachment(0);
    		formData.left   = new FormAttachment(0);
    		formData.right  = new FormAttachment(100);
    		title.setLayoutData(formData);
		}

		leftLegend   = new FigureCanvas(this);
       	figureCanvas = new FigureCanvas(this);

		formData = new FormData();
		if (titleString != null)
    		formData.top = new FormAttachment(title, 0, SWT.BOTTOM);
		else
			formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.width  = GenericTraceGraph.yLegendWidth;
		leftLegend.setLayoutData(formData);

		formData = new FormData();
		if (titleString != null)
    		formData.top = new FormAttachment(title, 0, SWT.BOTTOM);
		else
			formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(leftLegend, 0, SWT.RIGHT);
		formData.right  = new FormAttachment(100);
		figureCanvas.setLayoutData(formData);
	}
}
