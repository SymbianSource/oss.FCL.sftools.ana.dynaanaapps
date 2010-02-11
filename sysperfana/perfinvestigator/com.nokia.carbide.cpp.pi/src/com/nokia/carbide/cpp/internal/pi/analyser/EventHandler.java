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

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.util.Enumeration;

import org.eclipse.swt.widgets.Event;

import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;



public class EventHandler
{
	private static EventHandler instance = null;
	
	private EventHandler ()
	{}
	
	public static EventHandler getInstance()
	{
		if (instance == null)
		{
			instance = new EventHandler();
		}
		return instance;
	}
	
	public void broadcastEvent(String eventString, Event event)
	{
		Enumeration e = PluginInitialiser.getPluginInstances(NpiInstanceRepository.getInstance().activeUid(), "com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
		if (e == null) return;
		while (e.hasMoreElements())
		{
			IEventListener plugin = (IEventListener)e.nextElement();
			plugin.receiveEvent(eventString, event);
		}
	}
}
