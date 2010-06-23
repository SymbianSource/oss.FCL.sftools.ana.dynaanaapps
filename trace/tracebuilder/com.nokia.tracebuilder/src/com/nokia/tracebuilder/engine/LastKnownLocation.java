/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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
* Last known  location represents the last known position of a trace
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceConstants;

/**
 * Last known  location represents the last known position of a trace
 * 
 */
public final class LastKnownLocation implements LocationProperties {

	/**
	 * Location list owning this location
	 */
	private LastKnownLocationList list;

	/**
	 * Path of the source file
	 */
	private String path;

	/**
	 * Source file name
	 */
	private String file;

	/**
	 * Line number
	 */
	private int line;

	/**
	 * View reference, used by view for quick access
	 */
	private Object viewReference;

	/**
	 * Name of the class where the trace belongs to
	 */
	private String className;

	/**
	 * Name of the function where the trace belongs to
	 */
	private String functionName;

	/**
	 * Creates a new last known  location
	 * 
	 * @param path
	 *            file path
	 * @param file
	 *            file name
	 * @param line
	 *            line number
	 * @param className
	 *            the name of the class
	 * @param functionName
	 *            the name of the function
	 */
	public LastKnownLocation(String path, String file, int line,
			String className, String functionName) {
		if (path != null && path.length() > 0) {
			path = path.replace(SourceConstants.BACKSLASH_CHAR,
					SourceConstants.FORWARD_SLASH_CHAR);
			if (path.charAt(path.length() - 1) != SourceConstants.FORWARD_SLASH_CHAR) {
				path = path + SourceConstants.FORWARD_SLASH_CHAR;
			}
			this.path = path;
		} else {
			this.path = String.valueOf(SourceConstants.FORWARD_SLASH_CHAR);
		}
		this.file = file == null ? "" : file; //$NON-NLS-1$
		this.line = line;
		this.className = className == null ? "" : className; //$NON-NLS-1$
		this.functionName = functionName == null ? "" : functionName; //$NON-NLS-1$
	}

	/**
	 * Gets the trace
	 * 
	 * @return the trace
	 */
	public Trace getTrace() {
		Trace retval = null;
		if (list != null) {
			TraceObject owner = list.getOwner();
			if (owner instanceof Trace) {
				retval = (Trace) owner;
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LocationProperties#getFilePath()
	 */
	public String getFilePath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LocationProperties#getFileName()
	 */
	public String getFileName() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LocationProperties#getLineNumber()
	 */
	public int getLineNumber() {
		return line;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LocationProperties#getClassName()
	 */
	public String getClassName() {
		return className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.LocationProperties#getFunctionName()
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * Used by view for quick access
	 * 
	 * @return the view reference
	 */
	public Object getViewReference() {
		return viewReference;
	}

	/**
	 * Sets the view reference
	 * 
	 * @param ref
	 *            the view reference
	 */
	public void setViewReference(Object ref) {
		viewReference = ref;
	}

	/**
	 * Sets the location list
	 * 
	 * @param list
	 *            the list
	 */
	void setLocationList(LastKnownLocationList list) {
		this.list = list;
	}

}