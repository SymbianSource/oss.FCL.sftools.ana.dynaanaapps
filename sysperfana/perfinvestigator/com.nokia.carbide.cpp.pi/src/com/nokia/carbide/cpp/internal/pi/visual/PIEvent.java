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
 * Description: PIEvent.java 
 *
 */

package com.nokia.carbide.cpp.internal.pi.visual;

public class PIEvent 
{
	public static final int SELECTION_AREA_CHANGED = 0; 
	// event should contain an int[2] object, 
	// where selection start = object[0] and end = object[1]

	public static final int SELECTION_AREA_CHANGED2 = 1; 
	// event should contain an int[2] object, 
	// where selection start = object[0] and end = object[1]

	public static final int SELECTION_AREA_CHANGED3 = 20; 
	// event should contain an int[2] object, 
	// where selection start = object[0] and end = object[1]

	public static final int SCALE_CHANGED = 2;
	// event should contain a Double object, 
	// containing the new scale value
	
	public static final int SET_FILL_ALL_THREADS = 3; 
	// no value object required
	
	public static final int SET_FILL_OFF = 4;
	// no value object required
	
	public static final int SET_FILL_SELECTED_THREAD = 5; 
	// no value object required
	
	public static final int CHANGED_THREAD_TABLE  = 6;
	// no value object required
	
	public static final int CHANGED_BINARY_TABLE = 7;
	// no value object required
	
	public static final int CHANGED_FUNCTION_TABLE = 8;
	// no value object required
	
	public static final int SYNCHRONISE = 9;
	// value object is the number of seconds added/reduced
	
	public static final int DSP_SET_FILL_ALL_THREADS = 10; 
	// no value object required
	
	public static final int DSP_SET_FILL_OFF = 11;
	// no value object required
	
	public static final int DSP_SET_FILL_SELECTED_THREAD = 12; 
	// no value object required
	
	public static final int DSP_RESOLUTION_CHANGED = 13;
	// event should contain a Double object, 
	// containing the new DSP resolution value
	
	public static final int PF_LIST_VALUE_CHANGED = 14;
	// event should contain a Double object, 
	// containing the new DSP resolution value
	
	public static final int TIME_SCALE_CHANGED = 15;
	// event should contain a Double object, 
	// containing the new DSP resolution value	

	public static final int SUBGRAPH_INSERTED = 16;
	// event contains a reference to the subcomponent
	
	public static final int GPP_SET_BAR_GRAPH_ON = 17;
	// set bar graph mode in GPP trace visualisation
	
	public static final int GPP_SET_BAR_GRAPH_OFF = 18;
	// set normal mode in GPP trace visualisation
	
	public static final int MOUSE_PRESSED = 19;
	// value object is the source object of the click

	public static final int THRESHOLD_THREAD_CHANGED   = 20;
	public static final int THRESHOLD_BINARY_CHANGED   = 21;
	public static final int THRESHOLD_FUNCTION_CHANGED = 22;
	// no value object because we can use the global value
	
	public static final int POWER_GRAPH_AVG_ON     = 23;
	public static final int POWER_GRAPH_AVG_OFF    = 24;
	public static final int POWER_INTERVAL_AVG_ON  = 25;
	public static final int POWER_INTERVAL_AVG_OFF = 26;
	public static final int POWER_SHOW_AVG_ON      = 27;
	public static final int POWER_SHOW_AVG_OFF     = 28;
	// no value object required

	public static final int CHANGED_MEMORY_TABLE = 29;
	// no value object required
	
	public static final int SCROLLED = 30;
	// event should contain an org.eclipse.swt.widgets.Event
	// with the new origin in its x and y

	public static final int PLUGIN_STRING_MESSAGE = 100;
	// event should contain a Double object, 
	// containing the new DSP resolution value

	private Object valueObject;
	private int type;
	
	public PIEvent(Object valueObject, int type)
	{
		this.valueObject = valueObject;
		this.type = type;
	}
	
	public Object getValueObject()
	{
		return this.valueObject;
	}
	
	public int getType()
	{
		return this.type;
	}
}
