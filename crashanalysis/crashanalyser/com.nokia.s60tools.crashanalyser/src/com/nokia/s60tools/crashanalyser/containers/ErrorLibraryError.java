/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
*/

package com.nokia.s60tools.crashanalyser.containers;

/**
 * A class which represents one error in error library. 
 * An error can be a panic, category, error or exception. 
 *
 */
public class ErrorLibraryError {

	String errorName;
	String errorDescription;
	public static enum ErrorType {ERROR, PANIC, CATEGORY, EXCEPTION}
	
	/**
	 * Constructor
	 * @param name error name
	 * @param description error description
	 */
	public ErrorLibraryError(String name, String description) {
		errorName = name;
		errorDescription = description;
	}
	
	/**
	 * Constructor
	 */
	public ErrorLibraryError() {
		// for empty ErrorLibraryError creation
	}
	
	/**
	 * Set name for an error
	 * @param name error name
	 */
	public void SetName(String name) {
		errorName = name;
	}
	
	/**
	 * Set description for an error.
	 * @param description error description
	 */
	public void SetDescription(String description) {
		errorDescription = description;
	}
	
	/**
	 * Adds information to an error description
	 * @param description error description
	 */
	public void AddToDescription(String description) {
		errorDescription += description;
	}
	
	public String toString() {
		return errorName;
	}
	
	public String getDescription() {
		return errorDescription;
	}
}
