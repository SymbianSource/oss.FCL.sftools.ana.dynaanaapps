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
* SourceContextManager interface implementation
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.source.SourceContext;

/**
 * SourceContextManager interface implementation
 * 
 */
final class SourceContextManagerImpl implements SourceContextManager {

	/**
	 * Source engine
	 */
	private SourceEngine sourceEngine;

	/**
	 * Active context
	 */
	private SourceContext activeContext;

	/**
	 * Instrumenting flag
	 */
	private boolean isInstrumenting;

	/**
	 * Converting flag
	 */
	private boolean isConverting;

	/**
	 * Instrumenter ID
	 */
	private String instrumenterID;

	/**
	 * Constructor
	 * 
	 * @param sourceEngine
	 *            the source engine
	 */
	SourceContextManagerImpl(SourceEngine sourceEngine) {
		this.sourceEngine = sourceEngine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#getContext()
	 */
	public SourceContext getContext() {
		SourceContext retval = activeContext;
		if (retval == null) {
			retval = sourceEngine.getSelectedContext();
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#
	 *      setContext(com.nokia.tracebuilder.source.SourceContext)
	 */
	public void setContext(SourceContext context) {
		activeContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#isInstrumenting()
	 */
	public boolean isInstrumenting() {
		return isInstrumenting;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#setInstrumenting(boolean)
	 */
	public void setInstrumenting(boolean flag) {
		isInstrumenting = flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#getInstrumenterType()
	 */
	public String getInstrumenterID() {
		return instrumenterID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#
	 *      setInstrumenterID(java.lang.String)
	 */
	public void setInstrumenterID(String id) {
		instrumenterID = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#isConverting()
	 */
	public boolean isConverting() {
		return isConverting;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.SourceContextManager#setConverting(boolean)
	 */
	public void setConverting(boolean flag) {
		this.isConverting = flag;
	}
}