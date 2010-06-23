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
* Wrapper for SourceLocationBase within JFace Position
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.jface.text.Position;

import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceLocationInterface;

/**
 * Wrapper for JFace Position
 * 
 */
final class JFaceLocationWrapper extends Position implements
		SourceLocationInterface {

	/**
	 * Location
	 */
	private SourceLocationBase location;

	/**
	 * Contructor
	 * 
	 * @param location
	 *            the location to be wrapped
	 * @param offset
	 *            the location offset
	 * @param length
	 *            the location length
	 */
	JFaceLocationWrapper(SourceLocationBase location, int offset, int length) {
		super(offset, length);
		this.location = location;
	}

	/**
	 * Gets the wrapped location
	 * 
	 * @return the location
	 */
	public SourceLocationBase getLocation() {
		return location;
	}

}
