/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
* Creates a plug-in formatter when setData is called
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.TraceModelPersistentExtension;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Creates a plug-in formatter when setData is called
 * 
 */
final class PluginTraceFormatRuleDelegate implements
		TraceModelPersistentExtension {

	/**
	 * 
	 */
	private final RulesEngine engine;

	/**
	 * @param engine
	 */
	PluginTraceFormatRuleDelegate(RulesEngine engine) {
		this.engine = engine;
	}

	/**
	 * Owning trace object
	 */
	private TraceObject owner;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#getData()
	 */
	public String getData() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#getStorageName()
	 */
	public String getStorageName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelPersistentExtension#setData(java.lang.String)
	 */
	public boolean setData(String data) {
		this.engine.changeTraceAPI(data);
		// This extension is not added to model
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
		this.owner = owner;
	}

}