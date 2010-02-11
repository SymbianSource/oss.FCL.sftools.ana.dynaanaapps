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

package com.nokia.carbide.cpp.internal.pi.model;

import java.util.Enumeration;
import java.util.Vector;

public abstract class GenericEventTrace extends GenericTrace
{
	private Vector<GenericEvent> events;
	
	static final long serialVersionUID = -5793331074508392029L;
	
	public GenericEventTrace()
	{
		this.events = new Vector<GenericEvent>();
	}
	
	public void addEvent(GenericEvent e)
	{
		this.events.add(e);
	}
	
	public Vector<GenericEvent> getEvents()
	{
		return this.events;
	}
    
    public Vector<GenericEvent> getEventsInsideTimePeriod(double start, double end)
    {
        Enumeration<GenericEvent> eEnum = events.elements();
        Vector<GenericEvent> okEvents = new Vector<GenericEvent>();
          
        while(eEnum.hasMoreElements())
        {
            GenericEvent e = eEnum.nextElement();
            if (e.eventTime >= start)
            {
                if (e.eventTime <= end)
                {
                    okEvents.add(e);
                }
            }
        }
        return okEvents;
    }
    
    public double getTimeForFirstEvent()
    {
        GenericEvent e = events.firstElement();
        return e.eventTime;
        
    }
    
    public double getTimeForLastEvent()
    {
        GenericEvent e = events.lastElement();
        return e.eventTime;
    }
    
}
