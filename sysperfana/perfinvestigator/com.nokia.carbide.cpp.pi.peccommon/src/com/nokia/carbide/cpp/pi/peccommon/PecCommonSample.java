/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.peccommon;

import java.util.Arrays;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;


/**
 * Represents a Performance Counter Sample
 */
public class PecCommonSample extends GenericSample 
{
	private static final long serialVersionUID = 84123271817196064L;
	
	/**
	 * Sample values for the captured performance counter events
	 */
	public int[] values;
	
	/**
	 * Constructor
	 * @param values Sample values for the captured events
	 * @param time Time the sample was taken
	 */
	public PecCommonSample(int[] values, long time)
	{
		super();
		this.sampleSynchTime = time;
		this.values = values;
	}

	@Override
	public String toString() {
		return String.format(Messages.PecCommonSample_0, sampleSynchTime, Arrays.toString(values));
	}
	
	
}
