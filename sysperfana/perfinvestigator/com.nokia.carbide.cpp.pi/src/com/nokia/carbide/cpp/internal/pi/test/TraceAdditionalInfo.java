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

package com.nokia.carbide.cpp.internal.pi.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

/**
 * Abstract class as boilerplate for plugins to define their additional info
 * 
 */
public class TraceAdditionalInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5759753038732844008L;
	
	private HashMap<Integer, Vector<Object>> additional_info = new HashMap<Integer, Vector<Object>>();
	
	public void addAdditionalInfo(int traceId, Vector<Object> additionalInfo) {
		additional_info.put(new Integer(traceId), additionalInfo);
	}
	
	public Set<Entry<Integer, Vector<Object>>> getAdditionalInfoSet() {
		return additional_info.entrySet();
	}

	public int size() {
		return additional_info.size();
	}
}
