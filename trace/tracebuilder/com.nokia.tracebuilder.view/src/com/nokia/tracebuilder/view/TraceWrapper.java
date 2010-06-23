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
* Wrapper for one trace object
*
*/
package com.nokia.tracebuilder.view;

import com.nokia.tracebuilder.engine.LastKnownLocationList;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Wrapper for one trace object
 * 
 * @see com.nokia.tracebuilder.model.Trace
 */
final class TraceWrapper extends TraceObjectWrapper {

	/**
	 * Last known location list wrapper
	 */
	private LastKnownLocationListWrapper LastKnownLocationListWrapper;

	/**
	 * List of object parameters
	 */
	private TraceParameterListWrapper parameterListWrapper;

	/**
	 * Location list wrapper
	 */
	private TraceLocationListWrapper locationListWrapper;

	/**
	 * Default constructor
	 * 
	 * @param trace
	 *            the trace
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceWrapper(Trace trace, WrapperBase parent, WrapperUpdater updater) {
		super(trace, parent, updater);
		addParameterList();
		TraceLocationList list = trace.getExtension(TraceLocationList.class);
		if (list != null) {
			setLocationList(list);
		}
		LastKnownLocationList plist = trace
				.getExtension(LastKnownLocationList.class);
		if (plist != null) {
			setLastKnownLocationList(plist);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.TraceObjectWrapper#delete()
	 */
	@Override
	void delete() {
		if (locationListWrapper != null && !contains(locationListWrapper)) {
			locationListWrapper.delete();
		}
		if (LastKnownLocationListWrapper != null
				&& !contains(LastKnownLocationListWrapper)) {
			LastKnownLocationListWrapper.delete();
		}
		super.delete();
	}

	/**
	 * Creates and adds the parameter list
	 */
	void addParameterList() {
		Trace trace = (Trace) getTraceObject();
		parameterListWrapper = new TraceParameterListWrapper(trace, this,
				getUpdater());
		if (trace.hasParameters()) {
			add(parameterListWrapper);
		}
	}

	/**
	 * Adds a parameter to the parameter list
	 * 
	 * @param parameter
	 *            the parameter to be added
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase addParameter(TraceParameter parameter) {
		if (parameterListWrapper.hasChildren()) {
			// If parameters already exist, the parameter list is updated
			parameterListWrapper.addParameter(parameter);
		} else {
			// If not, the parameter list is added
			parameterListWrapper.addParameter(parameter);
			add(parameterListWrapper);
		}
		return this;
	}

	/**
	 * Removes a parameter
	 * 
	 * @param parameter
	 *            the parameter to be removed
	 * @return the wrapper which needs to be refreshed
	 */
	WrapperBase removeParameter(TraceParameter parameter) {
		parameterListWrapper.removeParameter(parameter);
		// If the parameter list no longer contains elements, it is removed
		if (!parameterListWrapper.hasChildren()) {
			hide(parameterListWrapper);
		}
		return this;
	}

	/**
	 * Sets the last known location list to this object
	 * 
	 * @param list
	 *            the location list
	 * @return the wrapper to be updated
	 */
	WrapperBase setLastKnownLocationList(LastKnownLocationList list) {
		if (list != null) {
			if (LastKnownLocationListWrapper == null) {
				LastKnownLocationListWrapper = new LastKnownLocationListWrapper(
						list, this, getUpdater());
				if (LastKnownLocationListWrapper.hasChildren()) {
					add(LastKnownLocationListWrapper);
				}
			} else {
				if (LastKnownLocationListWrapper.getLocationList() != list) {
					LastKnownLocationListWrapper.updateLocationList(list);
				}
			}
		} else {
			if (LastKnownLocationListWrapper != null) {
				LastKnownLocationListWrapper.updateLocationList(null);
				hide(LastKnownLocationListWrapper);
			}
		}
		return this;
	}

	/**
	 * Shows the last known locations list
	 */
	void showLastKnownLocationList() {
		add(LastKnownLocationListWrapper);
	}

	/**
	 * Hides the last known locations list
	 */
	void hideLastKnownLocationList() {
		hide(LastKnownLocationListWrapper);
	}

	/**
	 * Sets the location list to this object
	 * 
	 * @param list
	 *            the location list
	 * @return the wrapper to be updated
	 */
	WrapperBase setLocationList(TraceLocationList list) {
		if (list != null) {
			if (locationListWrapper == null) {
				locationListWrapper = new TraceLocationListWrapper(list, this,
						getUpdater());
				if (locationListWrapper.hasChildren()) {
					add(locationListWrapper);
				}
			} else {
				if (locationListWrapper.getLocationList() != list) {
					locationListWrapper.updateLocationList(list);
				}
			}
		} else {
			if (locationListWrapper != null) {
				locationListWrapper.updateLocationList(null);
				hide(locationListWrapper);
			}
		}
		return this;
	}

	/**
	 * Shows the location list
	 */
	void showLocationList() {
		add(locationListWrapper);
	}

	/**
	 * Hides the location list
	 */
	void hideLocationList() {
		hide(locationListWrapper);
	}

	/**
	 * Gets the location list wrapper
	 * 
	 * @return the list wrapper
	 */
	TraceLocationListWrapper getLocationListWrapper() {
		return locationListWrapper;
	}

}