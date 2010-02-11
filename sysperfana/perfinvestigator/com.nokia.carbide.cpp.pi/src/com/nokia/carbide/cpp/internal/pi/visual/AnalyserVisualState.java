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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;


public class AnalyserVisualState implements Serializable
{
	private static final long serialVersionUID = -826324357879880731L;
	
	private Hashtable<Integer,Color> colorInfo;
	private Hashtable<Integer,Boolean> selectedInfo;
	private int selectionStart;
	private int selectionEnd;
	private double zoomFactor;
	private transient Rectangle viewRect1;
	
	public AnalyserVisualState(Enumeration threads, PICompositePanel bcp)
	{
		colorInfo    = new Hashtable<Integer,Color>();
		selectedInfo = new Hashtable<Integer,Boolean>();
		while (threads.hasMoreElements())
		{
			ProfiledThread pt = (ProfiledThread) threads.nextElement();
			colorInfo.put(new Integer(pt.getThreadId()), pt.getColor());
			// Could instead find  pt.isEnabled() for all pages - not just page 0
			selectedInfo.put(new Integer(pt.getThreadId()), Boolean.valueOf(pt.isEnabled(0)));
		}
		
		selectionStart = (int) bcp.getSelectionStart();
		selectionEnd = (int) bcp.getSelectionEnd();
		viewRect1 = bcp.getSashForm().getBounds();
		zoomFactor = bcp.getScale();
	}
	
	public Enumeration<ProfiledThread> resetThreadState(Enumeration<ProfiledThread> threads)
	{
		Vector<ProfiledThread> v = new Vector<ProfiledThread>();
		Enumeration<ProfiledThread> temp = threads;
		while (threads.hasMoreElements())
		{
			Object o = threads.nextElement();
			if (o instanceof ProfiledThread)
			{
				ProfiledThread pt = (ProfiledThread) o;
				Color c = (Color)colorInfo.get(new Integer(pt.getThreadId()));
				Boolean b = (Boolean)selectedInfo.get(new Integer(pt.getThreadId()));
				if ((c != null) && (b != null))
				{
					pt.setColor(c);
					pt.setEnabled(0, b.booleanValue());
					v.addElement(pt);
				}
				else
				{
					System.out.println(Messages.getString("AnalyserVisualState.noThreadInfo")); //$NON-NLS-1$
					return temp;
				}
			}
		}
		return v.elements();
	}

	public int getSelectionEnd() 
	{
		return selectionEnd;
	}

	public int getSelectionStart() 
	{
		return selectionStart;
	}

	public double getZoomFactor() 
	{
		return zoomFactor;
	}

	public Rectangle getViewRect()
	{
		return viewRect1;
	}
}
