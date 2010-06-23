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
* Base class for source file updater objects
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Base class for source file updater objects
 * 
 */
public abstract class SourceEditorUpdater {

	/**
	 * Source properties to update
	 */
	private SourceProperties properties;

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            the source to updated
	 */
	protected SourceEditorUpdater(SourceProperties properties) {
		this.properties = properties;
	}

	/**
	 * Updates the source
	 */
	public void update() {
		properties.getUpdateQueue().queueUpdate(this);
	}

	/**
	 * Gets the source properties
	 * 
	 * @return the source properties
	 */
	protected SourceProperties getSource() {
		return properties;
	}

	/**
	 * Checks if this updater equals the given updater. Default implementation
	 * compares the position references
	 * 
	 * @param updater
	 *            the updater to be checked
	 * @return true if the updates are equal
	 */
	protected boolean updaterEquals(SourceEditorUpdater updater) {
		return getPosition() == updater.getPosition();
	}

	/**
	 * Runs the update operation on the source
	 * 
	 * @return true if source was updated, false if not
	 * @throws SourceParserException
	 *             if source parser fails
	 */
	protected abstract boolean runUpdate() throws SourceParserException;

	/**
	 * Gets the position where the update happens
	 * 
	 * @return the update position
	 */
	protected abstract SourceLocationBase getPosition();

}
