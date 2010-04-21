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

package com.nokia.carbide.cpp.pi.internal.perfcounters;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendElement;

/**
 * An element for the Performance Counter Trace legend view.
 * Represented in one line of the legend table viewer.
 */
public class PecLegendElement extends PecCommonLegendElement {
	
	/** 
	 * Calculated value which unfortunately we have to store here as it requires
	 * access to the other performance counters 
	 */
	private float[] xOverY;
	
	/**
	 * Constructor
	 * @param id trace event id
	 * @param name the name of the event
	 * @param character character representing the event, i.e. A for the first event, B for the second etc.
	 * @param coreValuesOnly Calculate / show only average, min, and max
	 */
	public PecLegendElement(int id, String name, char character, boolean coreValuesOnly) {
		super(id, name, character, coreValuesOnly);
	}
	/**
	 * @param idx index of the element to use, such as 0 for per A, 1 for per B
	 * @return the x over y for for the given element index
	 */
	public float getxOverY(int idx) {
		return xOverY[idx];
	}
	/**
	 * @param xOverY the xOverY to set
	 */
	public void setxOverY(float[] xOverY) {
		this.xOverY = xOverY;
	}

}
