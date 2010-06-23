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
* Trace model extension listener implementation for SourceEngine
*
*/
package com.nokia.tracebuilder.engine.source;

import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceModelExtensionListener;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Trace model extension listener implementation for SourceEngine
 * 
 */
final class SourceEngineModelExtensionListener implements
		TraceModelExtensionListener {

	/**
	 * Source engine
	 */
	private final SourceEngine sourceEngine;

	/**
	 * Constructor
	 * 
	 * @param engine
	 *            the source engine
	 */
	SourceEngineModelExtensionListener(SourceEngine engine) {
		sourceEngine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      extensionAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionAdded(TraceObject object, TraceModelExtension extension) {
		// Parser rules that are added to model are taken into use
		if (object instanceof TraceModel
				&& extension instanceof SourceParserRule) {
			sourceEngine.parserAdded((SourceParserRule) extension);
		} else if (extension instanceof TraceFormattingRule) {
			sourceEngine.ruleUpdated(object);
		} else if (extension instanceof TraceParameterFormattingRule) {
			sourceEngine.ruleUpdated(object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      extensionRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceModelExtension)
	 */
	public void extensionRemoved(TraceObject object,
			TraceModelExtension extension) {
		// Parser rules that are removed from model are removed from sources
		if (object instanceof TraceModel
				&& extension instanceof SourceParserRule) {
			sourceEngine.parserRemoved((SourceParserRule) extension);
		} else if (extension instanceof TraceFormattingRule) {
			// When changing formatting, the existing formatting rule will
			// be removed and a new one added. When formatting rule is
			// removed, the trace must not be updated since it would
			// disappear from the source
		} else if (extension instanceof TraceParameterFormattingRule) {
			sourceEngine.ruleUpdated(object);
		}
	}

}