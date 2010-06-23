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

package com.nokia.carbide.cpp.internal.pi.model;

import java.io.File;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;

public class PluginTrace implements ITrace {
	
	private int traceId;
	private String traceName;
	private String traceDescription;
	private String traceTitle;
	private boolean mandatory;
	
	
	public void setTraceId(int traceId) {
		this.traceId = traceId;
	}
	
	public int getTraceId() {
		return traceId;
	}

	public void setTraceName(String traceName) {
		this.traceName = traceName;
	}
	
	public String getTraceName() {		
		return traceName;
	}

	public void setTraceDescription(String traceDescription) {
		this.traceDescription = traceDescription;
	}
	
	public String getTraceDescription() {
		return traceDescription;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isMandatory() {		
		return mandatory;
	}
	
	public void setTraceTitle(String traceTitle) {
		this.traceTitle = traceTitle;
	}

	public String getTraceTitle() {		
		return traceTitle;
	}

	@SuppressWarnings("unchecked")
	public Class getTraceClass() {		
		return null;
	}

	public void initialiseTrace(GenericTrace trace) {
				
	}

	public ParsedTraceData parseTraceFile(File file) throws Exception {		
		return null;
	}

	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {		
		return null;
	}
	
}
