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
* Model listener for trace location mapper
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Model listener for trace location mapper
 * 
 */
final class LocationMapModelListener implements TraceModelListener {

	/**
	 * Trace location mapper
	 */
	private final TraceLocationMap locationMap;

	/**
	 * Constructor
	 * 
	 * @param map
	 *            the location mapper
	 */
	LocationMapModelListener(TraceLocationMap map) {
		locationMap = map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
		// A trace does not have a name yet, so this does nothing
		// The locations are assigned to the trace in propertyChanged
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		if (object instanceof Trace) {
			locationMap.moveToUnrelated((Trace) object);
		} else if (object instanceof TraceGroup) {
			// Moves locations to unrelated list
			for (Trace trace : (TraceGroup) object) {
				locationMap.moveToUnrelated(trace);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertiesUpdated(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void propertyUpdated(TraceObject object, int property) {
		if (object instanceof Trace && property == NAME && object.isComplete()) {
			Trace trace = (Trace) object;
			locationMap.moveFromUnrelated(trace);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
		if (object instanceof Trace) {
			Trace trace = (Trace) object;
			locationMap.moveFromUnrelated(trace);
		} else if (object instanceof TraceParameter) {
			// If a parameter is added, the validity needs to be checked
			TraceLocationList list = ((TraceParameter) object).getTrace()
					.getExtension(TraceLocationList.class);
			if (list != null) {
				for (LocationProperties loc : list) {
					((TraceLocation) loc).runValidityCheck();
				}
			}
		}
	}
}