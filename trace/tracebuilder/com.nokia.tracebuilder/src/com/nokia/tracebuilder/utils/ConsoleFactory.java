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
* Factory class to create consoles
*
*/
package com.nokia.tracebuilder.utils;

import java.io.OutputStream;

/**
 * Factory class to create consoles
 * 
 */
public abstract class ConsoleFactory {

	/**
	 * The console class
	 */
	private static Class<? extends ConsoleBase> consoleClass;

	/**
	 * Registers a console class to be used. If null, System.out will be used as
	 * console output stream
	 * 
	 * @param consoleClass
	 *            the consoleClass
	 */
	public static void registerConsole(Class<? extends ConsoleBase> consoleClass) {
		ConsoleFactory.consoleClass = consoleClass;
	}

	/**
	 * Creates a console with given title. If a console implementation cannot be
	 * created this returns <code>System.out</code>
	 * 
	 * @param title
	 *            the console title
	 * @return the console output stream
	 */
	public static OutputStream createConsole(String title) {
		OutputStream retval = null;
		if (consoleClass != null) {
			try {
				ConsoleBase console = consoleClass.newInstance();
				console.setTitle(title);
				retval = console.createStream();
			} catch (Exception e) {
			}
		}
		if (retval == null) {
			retval = System.out;
		}
		return retval;
	}

}
