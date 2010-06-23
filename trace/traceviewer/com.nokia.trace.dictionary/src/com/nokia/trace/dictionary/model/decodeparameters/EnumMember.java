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
 * Enum member
 *
 */
package com.nokia.trace.dictionary.model.decodeparameters;

/**
 * Enum member
 * 
 */
public class EnumMember {

	/**
	 * Name of the member
	 */
	private String name;

	/**
	 * Value of the member
	 */
	private int value;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of member
	 * @param value
	 *            value of member
	 */
	public EnumMember(String name, int value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Gets name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets value
	 * 
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets value
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

}
