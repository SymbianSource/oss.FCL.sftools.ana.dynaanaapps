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
 * Trace Activator
 *
 */
package com.nokia.traceviewer.engine.activation;

import java.util.List;

/**
 * Trace Activator
 */
public class TraceActivator {

	/**
	 * Sends activation messages from given components
	 * 
	 * @param components
	 *            the components
	 */
	public void activate(List<TraceActivationComponentItem> components) {

		// Send OST activations
		if (!components.isEmpty()) {
			OstTraceActivator.activate(components);
		}
	}
}
