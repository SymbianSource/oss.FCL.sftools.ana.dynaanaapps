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

package com.nokia.carbide.cpp.pi.power;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Panel;
import org.eclipse.swt.graphics.GC;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;


public class PwrTraceGraph extends GenericTraceGraph implements MouseMotionListener, MouseListener
{
	private boolean selected = true;
	
	public PwrTraceGraph(PwrTrace trace)
	{
		super(trace);
	}
	
	public void paint(Graphics g)
	{
		if(!selected) return;
		
		Enumeration samples = ((GenericSampledTrace)this.getTrace()).samples.elements();
		g.setColor(Color.RED);
		
		while(samples.hasMoreElements())
		{
		}
	}

	public void repaint() 
	{
	}

	public void refreshDataFromTrace() 
	{
	}

	public void mouseMoved(MouseEvent me)
	{
		if(!selected) return;
		
		Enumeration samples = ((GenericSampledTrace)this.getTrace()).samples.elements();
		while(samples.hasMoreElements())
		{
		}
	}
	
	public void mouseClicked(MouseEvent me) //add custom tooltip to a pwrtrace tag
	{
		if(!selected || me.getClickCount() < 2) return;
		
		
		Enumeration samples = ((GenericSampledTrace)this.getTrace()).samples.elements();
		while(samples.hasMoreElements())
		{
		}
	}
	
	public void action(String action) 
	{
  		if(action.equals("pwrsel_selected")) //$NON-NLS-1$
  		{
  			this.selected = true;
  			this.repaint();
  		}
  	  	else if(action.equals("pwrsel_not_selected")) //$NON-NLS-1$
  	  	{
  	  		this.selected = false;
  	  		this.repaint();
  	  	}
  	}
  	
	public void mouseDragged(MouseEvent arg0) {}
	
	public void mouseEntered(MouseEvent arg0) {}
	
	public void mouseExited(MouseEvent arg0) {}
	
	public void mousePressed(MouseEvent arg0) {}
	
	public void mouseReleased(MouseEvent arg0) {}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph#paint(org.eclipse.draw2d.Panel, org.eclipse.draw2d.Graphics)
	 */
	@Override
	public void paint(Panel panel, org.eclipse.draw2d.Graphics graphics) {
		this.setSize(this.getSize().width, getVisualSize().height);
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
	}
}
