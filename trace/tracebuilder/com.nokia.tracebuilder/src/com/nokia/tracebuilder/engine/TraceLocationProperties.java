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
* Optional properties for a trace location
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.source.SourceLocationRule;
import com.nokia.tracebuilder.engine.source.TraceFormattingRule;

/**
 * Optional properties for a trace location
 * 
 */
public class TraceLocationProperties {

	/**
	 * Formatting rule for the location
	 */
	private TraceFormattingRule formatRule;

	/**
	 * Location rule for the location
	 */
	private SourceLocationRule locationRule;

	/**
	 * Tags to be added to source as parameters instead of parameter name
	 */
	private ArrayList<String> parameterTags;

	/**
	 * View reference for quick view updates
	 */
	private Object viewReference;

	/**
	 * Event view reference for quick event view updates
	 */
	private Object eventViewReference;

	/**
	 * Gets the location rule
	 * 
	 * @return the location rule
	 */
	public SourceLocationRule getLocationRule() {
		return locationRule;
	}

	/**
	 * Sets a location rule for the location. The rule overrides the default
	 * location rule provided by the trace that owns the location
	 * 
	 * @param rule
	 *            the location formatting rule
	 */
	public void setLocationRule(SourceLocationRule rule) {
		this.locationRule = rule;
	}

	/**
	 * Gets the formatting rule
	 * 
	 * @return the formatting rule
	 */
	public TraceFormattingRule getFormatRule() {
		return formatRule;
	}

	/**
	 * Sets a formatting rule for the location. The formatting rule overrides
	 * the formatting rule provided by the trace that owns the location
	 * 
	 * @param rule
	 *            the location formatting rule
	 */
	public void setFormatRule(TraceFormattingRule rule) {
		this.formatRule = rule;
	}

	/**
	 * Gets the parameter tags for this location. This works based on parameter
	 * index
	 * 
	 * @return the list of parameter tags for the location
	 */
	public Iterator<String> getParameterTags() {
		List<String> list;
		if (parameterTags != null) {
			list = parameterTags;
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}

	/**
	 * Adds a new parameter tag to this location
	 * 
	 * @param tag
	 *            the tag to be added
	 */
	public void addParameterTag(String tag) {
		if (parameterTags == null) {
			parameterTags = new ArrayList<String>();
		}
		parameterTags.add(tag);
	}

	/**
	 * Sets the view reference
	 * 
	 * @param viewReference
	 *            the view reference
	 */
	public void setViewReference(Object viewReference) {
		this.viewReference = viewReference;
	}

	/**
	 * Gets the view reference of the location
	 * 
	 * @return the view reference
	 */
	public Object getViewReference() {
		return viewReference;
	}

	/**
	 * Sets the event view reference
	 * 
	 * @param eventViewReference
	 *            the event view reference
	 */
	public void setEventViewReference(Object eventViewReference) {
		this.eventViewReference = eventViewReference;
	}

	/**
	 * Gets the event view reference of the location
	 * 
	 * @return the event view reference
	 */
	public Object getEventViewReference() {
		return eventViewReference;
	}

}
