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
 * Handles adding of DataProcessors to the list 
 *
 */
package com.nokia.traceviewer.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nokia.traceviewer.api.TraceViewerAPI.TVAPIError;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerViewInterface;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.engine.dataprocessor.Decoder;
import com.nokia.traceviewer.engine.dataprocessor.FilterProcessor;

/**
 * Handles adding of DataProcessors to the list
 */
final class DataProcessorAdder {

	/**
	 * Locations map
	 */
	private Map<DataProcessor, DataProcessorLocation> locations;

	/**
	 * Adds DataProcessor to the DataProcessors list and keeps track of the
	 * locations
	 * 
	 * @param dp
	 *            Dataprocessor
	 * @param location
	 *            location
	 * @param priority
	 *            priority of the DataProcessor
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError addDataProcessor(DataProcessor dp, DPLocation location,
			int priority) {
		TVAPIError errorCode = TVAPIError.NONE;

		// Create new dataprocessor location object
		DataProcessorLocation newLocation = new DataProcessorLocation(location,
				priority);

		// Create the map if it doesn't exist
		if (locations == null) {
			locations = new HashMap<DataProcessor, DataProcessorLocation>();
		}

		List<DataProcessor> list = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getDataProcessorList();

		int decoderPos = 0;
		int filterPos = 0;
		int viewStartPos = 0;
		int viewEndPos = 0;

		// Loop through the list and gather the positions
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof Decoder) {
				decoderPos = i;
			} else if (list.get(i) instanceof FilterProcessor) {
				filterPos = i;
			} else if (list.get(i) instanceof TraceViewerViewInterface) {
				if (viewStartPos == 0) {
					viewStartPos = i;
					viewEndPos = i;
				} else {
					viewEndPos = i;
				}
			}
		}

		// Check if this DataProcessor already exists in the list. If so, don't
		// add anything and return error
		if (locations.containsKey(dp)) {
			errorCode = TVAPIError.DATAPROCESSOR_ALREADY_ADDED;

			// If DataProcessor doesn't exist, add it to the list
		} else {
			// Add the dataprocessor to a right place
			addToRightPlace(dp, location, priority, list, decoderPos,
					filterPos, viewStartPos, viewEndPos);

			// Add location to the list
			locations.put(dp, newLocation);
		}

		return errorCode;

	}

	/**
	 * Adds the dataprocessor to the right place in the DataProcessor list
	 * 
	 * @param dp
	 *            the dataprocessor
	 * @param location
	 *            location where to add the dataprocessor
	 * @param priority
	 *            priority of the dataprocessor
	 * @param list
	 *            list of dataprocessors
	 * @param decoderPos
	 *            position of decoder in the list
	 * @param filterPos
	 *            position of filter in the list
	 * @param viewStartPos
	 *            position of first view in the list
	 * @param viewEndPos
	 *            position of last view in the list
	 */
	private void addToRightPlace(DataProcessor dp, DPLocation location,
			int priority, List<DataProcessor> list, int decoderPos,
			int filterPos, int viewStartPos, int viewEndPos) {

		// Calculate how many dataprocessors there are with the same Location
		// with smaller and bigger prioritys
		int smallerPrioritys = 0;
		int higherPrioritys = 0;

		Iterator<DataProcessorLocation> iterator = locations.values()
				.iterator();
		while (iterator.hasNext()) {
			DataProcessorLocation loc = iterator.next();
			if (loc.getLocation() == location) {
				if (loc.getPriority() > priority) {
					higherPrioritys++;
				} else {
					smallerPrioritys++;
				}
			}
		}

		// Switch the location
		switch (location) {

		case BEFORE_DECODER:
			list.add(decoderPos - smallerPrioritys, dp);
			break;
		case AFTER_DECODER:
			list.add(decoderPos + 1 + higherPrioritys, dp);
			break;
		case BEFORE_FILTER:
			list.add(filterPos - smallerPrioritys, dp);
			break;
		case AFTER_FILTER:
			list.add(filterPos + 1 + higherPrioritys, dp);
			break;
		case BEFORE_VIEW:
			list.add(viewStartPos - smallerPrioritys, dp);
			break;
		case AFTER_VIEW:
			list.add(viewEndPos + 1 + higherPrioritys, dp);
			break;
		default:
			break;
		}
	}

	/**
	 * Removes DataProcessor from the list of DataProcessors
	 * 
	 * @param dataProcessor
	 *            the DataProcessor to be removed
	 * @return error code from TraceViewerAPI
	 */
	public TVAPIError removeDataProcessor(DataProcessor dataProcessor) {
		TVAPIError errorCode = TVAPIError.DATAPROCESSOR_NOT_FOUND;

		// Check if the DataProcessor can be found from the list
		if (locations != null && locations.containsKey(dataProcessor)) {

			// Remove DataProcessor from the list and from the locations
			boolean removed = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getDataProcessorList().remove(
							dataProcessor);

			if (removed) {
				errorCode = TVAPIError.NONE;
			}
			locations.remove(dataProcessor);
		}
		return errorCode;
	}
}
