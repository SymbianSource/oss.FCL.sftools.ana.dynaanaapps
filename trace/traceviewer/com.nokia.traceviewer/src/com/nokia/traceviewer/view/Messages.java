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
 * String localization for view package.
 *
 */
package com.nokia.traceviewer.view;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * String localization for view package.
 * 
 */
final class Messages {

	/**
	 * Bundle name
	 */
	private static final String BUNDLE_NAME = "com.nokia.traceviewer.view.messages"; //$NON-NLS-1$

	/**
	 * Resource bundle
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Prevents construction.
	 */
	private Messages() {
	}

	/**
	 * Maps a key to localized string.
	 * 
	 * @param key
	 *            the key for the string
	 * @return the localized string
	 */
	public static String getString(String key) {
		String value;
		try {
			value = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			value = '!' + key + '!';
		}
		return value;
	}
}
