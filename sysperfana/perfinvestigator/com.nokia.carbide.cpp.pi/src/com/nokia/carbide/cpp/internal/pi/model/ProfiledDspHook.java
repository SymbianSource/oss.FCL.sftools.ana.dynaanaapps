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

import java.awt.Color;


public class ProfiledDspHook
{
	private Long timeStamp;	
	private Color color = null;
	private boolean enableValue = true;
	private String nameString;
	private int eventCount = 0;
	
    public ProfiledDspHook()
    {
//        super();
        this.color = new Color(
                (int)(Math.random()*255),
                (int)(Math.random()*255),
                (int)(Math.random()*255));
    }
    
    public ProfiledDspHook(String name, int ec)
    {
//        super();
        this.color = new Color(
                (int)(Math.random()*255),
                (int)(Math.random()*255),
                (int)(Math.random()*255));
        this.nameString = name;
        this.eventCount = ec;
    }

    public ProfiledDspHook(String name, long ts)
    {
//        super();
        this.color = new Color(
                (int)(Math.random()*255),
                (int)(Math.random()*255),
                (int)(Math.random()*255));
        this.nameString = name;
        this.timeStamp = new Long(ts);
    }

    public void setEventTimeString(Long ts)
    {
    	this.timeStamp = ts;
    }
    
    public String getEventTimeString()
    {
    	return this.timeStamp.toString();
    }

   
    public void setColor(Color c)
    {
      this.color = c;
    }

    public void setEnabled(boolean enableValue)
    {
      this.enableValue = enableValue;
    }
    
    public void setEventCount(int c)
    {
    	this.eventCount = c;
    }

    public boolean isEnabled()
    {
      return enableValue;
    }

    public Color getColor()
    {
      return this.color;
    }
    
    public String getNameString()
    {
      return this.nameString;
    }
    public int getEventCount()
    {
      return this.eventCount;
    }
    
    public String toString()
    {
    	if (this.isEnabled())
  	{
    		return "true  " + nameString + this.getEventTimeString(); //$NON-NLS-1$
  	}
      else
      {
      	return "false " + nameString + this.getEventTimeString(); //$NON-NLS-1$
      }
    }   
}
