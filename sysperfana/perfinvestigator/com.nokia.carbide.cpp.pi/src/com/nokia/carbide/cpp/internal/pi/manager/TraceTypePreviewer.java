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

/**
 * 
 */
package com.nokia.carbide.cpp.internal.pi.manager;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nokia.carbide.cpp.internal.pi.analyser.StreamFileParser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;


/**
 * 
 * Load a .dat file and preview all the traces available
 *
 */
public class TraceTypePreviewer {
	private HashMap<Integer, String> availableTraces = new HashMap<Integer, String>();
	
	public TraceTypePreviewer(File file) {
		try {
			StreamFileParser inputFile;
			inputFile = new StreamFileParser(file);
			Set<Integer> allTraceSet = inputFile.allTraceType();
			// dispose it
			inputFile = null;
			
			Enumeration<AbstractPiPlugin> e = PluginRegistry.getInstance().getRegistryEntries();
			while(e.hasMoreElements()) {
				AbstractPiPlugin plugin = e.nextElement();
				if (plugin instanceof ITrace) {
					ITrace tracePlugin = (ITrace) plugin;
					if (allTraceSet.contains(tracePlugin.getTraceId())) {
						availableTraces.put(tracePlugin.getTraceId(), tracePlugin.getTraceName());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public Map<Integer, String> getAllAvailableTraces() {
		return availableTraces;
	}

}
