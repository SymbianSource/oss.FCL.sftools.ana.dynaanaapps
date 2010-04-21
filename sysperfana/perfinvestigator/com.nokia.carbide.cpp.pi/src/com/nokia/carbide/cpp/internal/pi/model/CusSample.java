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

public class CusSample extends GenericSample {
	
	private static final long serialVersionUID =-7966954495413654189L;
	
	public String name;
	public double value;
	public Color color;
	public String comment;
	
	public CusSample(long p_sampleNum, String p_name, double p_value, Color p_color)
	{
		sampleSynchTime = p_sampleNum;
		name = p_name;
		value = p_value;
		color = p_color;
	}
	
	public String toString()
	{
		String sampleNum = Long.valueOf(sampleSynchTime).toString();
		String valueString = new Double(value).toString();
		return sampleNum+" "+name+" "+valueString+" "+color.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
