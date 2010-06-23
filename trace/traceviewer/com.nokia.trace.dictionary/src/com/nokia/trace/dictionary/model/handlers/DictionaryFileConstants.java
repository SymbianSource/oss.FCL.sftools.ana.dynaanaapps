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
 * Dictionary File Constants
 *
 */
package com.nokia.trace.dictionary.model.handlers;

/**
 * Dictionary File Constants
 * 
 */
interface DictionaryFileConstants {

	/**
	 * Category for events
	 */
	String EVENT_CATEGORY = Messages
			.getString("DictionaryFileConstants.EventCategory"); //$NON-NLS-1$

	/**
	 * Variable indicator
	 */
	String VARIABLE_INDICATOR = "%"; //$NON-NLS-1$

	/**
	 * ID String
	 */
	String ID = "id"; //$NON-NLS-1$

	/**
	 * Name String
	 */
	String NAME = "name"; //$NON-NLS-1$

	/**
	 * Prefix String
	 */
	String PREFIX = "prefix"; //$NON-NLS-1$

	/**
	 * Suffix String
	 */
	String SUFFIX = "suffix"; //$NON-NLS-1$

	/**
	 * Type String
	 */
	String TYPE = "type"; //$NON-NLS-1$

	/**
	 * Location reference String
	 */
	String LOCREF = "loc-ref"; //$NON-NLS-1$

	/**
	 * Line String
	 */
	String LINE = "line"; //$NON-NLS-1$

	/**
	 * Method name String
	 */
	String METHODNAME = "methodname"; //$NON-NLS-1$

	/**
	 * Class name String
	 */
	String CLASSNAME = "classname"; //$NON-NLS-1$

	/**
	 * Array indicator
	 */
	String ARRAY_INDICATOR = "[]"; //$NON-NLS-1$

	/**
	 * Classification String
	 */
	String CLASSIFICATION = "classification"; //$NON-NLS-1$

	/**
	 * Format character String
	 */
	String FORMATCHAR = "formatchar"; //$NON-NLS-1$

	/**
	 * Size String
	 */
	String SIZE = "size"; //$NON-NLS-1$

	/**
	 * Val String
	 */
	String VAL = "val"; //$NON-NLS-1$

	/**
	 * Value String
	 */
	String VALUE = "value"; //$NON-NLS-1$

	/**
	 * Data reference String
	 */
	String DATAREF = "data-ref"; //$NON-NLS-1$

	/**
	 * Start bracket character
	 */
	char START_BRACKET = '{';

	/**
	 * End bracket character
	 */
	char END_BRACKET = '}';

	/**
	 * Long format character indicator L
	 */
	char FORMATCHAR_INDICATOR_BIG_L = 'L';

	/**
	 * Long format character indicator l
	 */
	char FORMATCHAR_INDICATOR_SMALL_L = 'l';

	/**
	 * Long format character indicator h
	 */
	char FORMATCHAR_INDICATOR_H = 'h';

	/**
	 * Short format character length
	 */
	int SHORT_FORMATCHAR_LENGTH = 1;

	/**
	 * Long format character length
	 */
	int LONG_FORMATCHAR_LENGTH = 2;
}
