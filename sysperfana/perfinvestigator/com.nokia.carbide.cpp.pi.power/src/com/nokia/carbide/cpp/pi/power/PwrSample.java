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

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;

/**
 * A class representing a multimeter reading in amperes
 */

public class PwrSample extends GenericSample 
{   
	private static final long serialVersionUID = 3943366134331361386L;
	public double current;
    public double capacity;
    public double voltage;
    public int backlight;
    
    public PwrSample( long time, double ampValue, double voltage, double capacity ) 
	{
        this.current = ampValue;
        this.capacity = capacity;
        this.voltage = voltage;
        this.sampleSynchTime = time;
    }
    
    public PwrSample( long time, double ampValue, double voltage, double capacity, int backlight) 
	{
        this.current = ampValue;
        this.capacity = capacity;
        this.voltage = voltage;
        this.sampleSynchTime = time;
        this.backlight = backlight;
    }

    public String toString() 
	{
        return Messages.getString("PwrSample.toString1") + this.sampleSynchTime + Messages.getString("PwrSample.toString2") //$NON-NLS-1$ //$NON-NLS-2$
        		+ this.current + Messages.getString("PwrSample.toString3") + this.voltage + Messages.getString("PwrSample.toString4") //$NON-NLS-1$ //$NON-NLS-2$
        		+ this.capacity +Messages.getString("PwrSample.toString5")+ PwrSample.getBacklightInformation(backlight) +Messages.getString("PwrSample.toString6"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static String getBacklightInformation(int code){
		switch (code) {
		case 0:	
			return Messages.getString("PwrSample.backlightUnknown"); //$NON-NLS-1$
		case 1:		
			return Messages.getString("PwrSample.backlightOn"); //$NON-NLS-1$
		case 2:	
			return Messages.getString("PwrSample.backlightOff"); //$NON-NLS-1$
		case 3:		
			return Messages.getString("PwrSample.backlightBlink"); //$NON-NLS-1$
		default:
			return Messages.getString("PwrSample.backlightUnknown"); //$NON-NLS-1$
		}	
	}
	
}
