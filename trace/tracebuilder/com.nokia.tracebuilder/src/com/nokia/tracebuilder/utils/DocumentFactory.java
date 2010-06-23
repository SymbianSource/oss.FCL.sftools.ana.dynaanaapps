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
* Factory class to create document interfaces
*
*/
package com.nokia.tracebuilder.utils;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.source.SourceDocumentFactory;
import com.nokia.tracebuilder.source.SourceDocumentMonitor;

/**
 * Factory class to create document interfaces
 * 
 */
public final class DocumentFactory {

	/**
	 * Document monitor
	 */
	private static SourceDocumentMonitor monitor;

	/**
	 * Document factory class
	 */
	private static Class<? extends SourceDocumentFactory> factoryClass;

	/**
	 * Registers a document framework to be used by the engine
	 * 
	 * @param monitor
	 *            the document monitor
	 * @param factoryClass
	 *            the document factory class
	 */
	public static void registerDocumentFramework(SourceDocumentMonitor monitor,
			Class<? extends SourceDocumentFactory> factoryClass) {
		DocumentFactory.monitor = monitor;
		DocumentFactory.factoryClass = factoryClass;
	}

	/**
	 * Gets the document monitor
	 * 
	 * @return the monitor
	 */
	public static final SourceDocumentMonitor getDocumentMonitor() {
		return monitor;
	}

	/**
	 * Creates a document factory, which is not associated to a document monitor
	 * 
	 * @return the factory
	 * @throws TraceBuilderException
	 *             if factory cannot be created
	 */
	public static final SourceDocumentFactory createDocumentFactory()
			throws TraceBuilderException {
		SourceDocumentFactory retval = null;
		try {
			retval = factoryClass.newInstance();
		} catch (Exception e) {
			throw new TraceBuilderException(
					TraceBuilderErrorCode.UNEXPECTED_EXCEPTION, e);
		}
		return retval;
	}

}
