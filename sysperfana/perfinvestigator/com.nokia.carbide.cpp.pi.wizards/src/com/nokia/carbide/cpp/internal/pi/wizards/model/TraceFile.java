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
package com.nokia.carbide.cpp.internal.pi.wizards.model;

import java.io.Serializable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TraceFile implements Serializable {

	private static final long serialVersionUID = 8762830613454627945L;

	private String traceFilePath;
	private String projectName;
	private String sdkName;
	private long importTime;
	private long traceFileSize;
	private long traceLengthInTime;
	private int[] pluginIds;

	/**
	 * Constructor
	 * 
	 * @param traceFilePath location of the trace file
	 * @param projectName trace file is imported under the given project name
	 * @param sdkName SDK name
	 * @param traceFileSize size of the trace file
	 * @param traceLengthInTime trace time of the trace file
	 * @param pluginIds list of the plugin's id
	 */	
	public TraceFile(IPath traceFilePath, String projectName, String sdkName, long traceFileSize, long traceLengthInTime, int[] pluginIds) {
		this.traceFilePath = traceFilePath.toOSString();
		this.projectName = projectName;
		this.sdkName = sdkName;
		this.importTime = System.currentTimeMillis();
		this.traceFileSize = traceFileSize;
		this.traceLengthInTime = traceLengthInTime;
		this.pluginIds = pluginIds;
	}

	/**
	 * @return the traceFilePath
	 */
	public IPath getTraceFilePath() {
		return new Path(traceFilePath);
	}

	/**
	 * @param traceFilePath
	 *            the traceFilePath to set
	 */
	public void setTraceFilePath(IPath traceFilePath) {
		this.traceFilePath = traceFilePath.toString();
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName
	 *            the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the sdkName
	 */
	public String getSdkName() {
		return sdkName;
	}

	/**
	 * @param sdkName
	 *            the sdkName to set
	 */
	public void setSdkName(String sdkName) {
		this.sdkName = sdkName;
	}

	/**
	 * @return the importTime
	 */
	public long getImportTime() {
		return importTime;
	}

	/**
	 * @param importTime
	 *            the importTime to set
	 */
	public void setImportTime(long importTime) {
		this.importTime = importTime;
	}

	/**
	 * @return the traceFileSize
	 */
	public long getTraceFileSize() {
		return traceFileSize;
	}

	/**
	 * @param traceFileSize
	 *            the traceFileSize to set
	 */
	public void setTraceFileSize(long traceFileSize) {
		this.traceFileSize = traceFileSize;
	}

	/**
	 * @return the traceLengthInTime
	 */
	public long getTraceLengthInTime() {
		return traceLengthInTime;
	}

	/**
	 * @param traceLengthInTime
	 *            the traceLengthInTime to set
	 */
	public void setTraceLengthInTime(long traceLengthInTime) {
		this.traceLengthInTime = traceLengthInTime;
	}
	
	/**
	 * @return the pluginIds
	 */
	public int[] getPluginIds() {
		return pluginIds;
	}

	/**
	 * @param pluginIds the pluginIds to set
	 */
	public void setPluginIds(int[] pluginIds) {
		this.pluginIds = pluginIds;
	}

	/**
	 * Checks if trace files are equal. Two trace files are equal if
	 * their path is the same
	 */
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (!(other instanceof TraceFile))
			return false;
		
		TraceFile othr = (TraceFile)other;
		return this.getTraceFilePath().equals(othr.getTraceFilePath());	
	}

}
