/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Constants for Meta File XML export/import
 *
 */
package com.nokia.traceviewer.engine.metafile;

/**
 * Constants for TraceActivation XML export/import
 */
public class MetaFileXMLConstants {

	/**
	 * XML Header
	 */
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"; //$NON-NLS-1$

	/**
	 * File start
	 */
	public static final String FILE_START = "<MetadataFile Version=\"1\">\n"; //$NON-NLS-1$

	/**
	 * File end
	 */
	public static final String FILE_END = "</MetadataFile>\n"; //$NON-NLS-1$

	/**
	 * Trace comments start
	 */
	public static final String TRACECOMMENTS_START = "<TraceComments>\n"; //$NON-NLS-1$

	/**
	 * Trace comments
	 */
	public static final String TRACECOMMENTS_END = "</TraceComments>\n"; //$NON-NLS-1$

	/**
	 * Activations tag
	 */
	public static final String TRACECOMMENTS_TAG = "TraceComments"; //$NON-NLS-1$

	/**
	 * Comment tag
	 */
	public static final String COMMENT_TAG = "Comment"; //$NON-NLS-1$

	/**
	 * Line tag
	 */
	public static final String LINE_TAG = "Line"; //$NON-NLS-1$
}
