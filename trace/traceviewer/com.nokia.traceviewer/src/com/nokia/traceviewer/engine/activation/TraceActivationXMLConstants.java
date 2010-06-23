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
 * Constants for TraceActivation XML export/import
 *
 */
package com.nokia.traceviewer.engine.activation;

/**
 * Constants for TraceActivation XML export/import
 */
public class TraceActivationXMLConstants {

	/**
	 * XML Header
	 */
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"; //$NON-NLS-1$

	/**
	 * File start
	 */
	public static final String FILE_START = "<ConfigurationFile Version=\"1\">\n"; //$NON-NLS-1$

	/**
	 * File end
	 */
	public static final String FILE_END = "</ConfigurationFile>\n"; //$NON-NLS-1$

	/**
	 * Activations start
	 */
	public static final String ACTIVATIONS_START = "<Activations>\n"; //$NON-NLS-1$

	/**
	 * Activations end
	 */
	public static final String ACTIVATIONS_END = "</Activations>\n"; //$NON-NLS-1$

	/**
	 * Component tag
	 */
	public static final String COMPONENT_TAG = "Component"; //$NON-NLS-1$

	/**
	 * Group tag
	 */
	public static final String GROUP_TAG = "Group"; //$NON-NLS-1$

	/**
	 * Activations tag
	 */
	public static final String ACTIVATIONS_TAG = "Activations"; //$NON-NLS-1$

	/**
	 * Activation tag
	 */
	public static final String ACTIVATION_TAG = "Activation"; //$NON-NLS-1$

	/**
	 * Name tag
	 */
	public static final String NAME_TAG = "Name"; //$NON-NLS-1$

	/**
	 * ID tag
	 */
	public static final String ID_TAG = "Id"; //$NON-NLS-1$

	/**
	 * Yes tag
	 */
	public static final String YES_TAG = "Yes"; //$NON-NLS-1$

	/**
	 * No tag
	 */
	public static final String NO_TAG = "No"; //$NON-NLS-1$

}
