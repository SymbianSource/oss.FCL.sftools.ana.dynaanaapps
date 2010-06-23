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
 * ActivationHandler handles all activation related things
 *
 */
package com.nokia.traceviewer.dictionary;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.trace.dictionary.model.DictionaryDecodeModel;
import com.nokia.trace.dictionary.model.Trace;
import com.nokia.trace.dictionary.model.TraceComponent;
import com.nokia.trace.dictionary.model.TraceData;
import com.nokia.trace.dictionary.model.TraceGroup;
import com.nokia.trace.dictionary.model.decodeparameters.DecodeParameter;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;
import com.nokia.traceviewer.engine.activation.TraceActivationTraceItem;

/**
 * ActivationHandler handles all activation related things like returning
 * activation information
 */
public class ActivationHandler {

	/**
	 * DecodeFile Model
	 */
	private final DictionaryDecodeModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 */
	public ActivationHandler(DictionaryDecodeModel model) {
		this.model = model;
	}

	/**
	 * Gets activation information
	 * 
	 * @param getAlsoTraces
	 *            if true, also traces are added to the activation model. If
	 *            false, traces arrays in groups are empty.
	 * @return component list
	 */
	public ArrayList<TraceActivationComponentItem> getActivationInformation(
			boolean getAlsoTraces) {
		ArrayList<TraceComponent> modelComponents = model
				.getActivationInformation();
		ArrayList<TraceActivationComponentItem> activationComponents = new ArrayList<TraceActivationComponentItem>();

		// Go trough components
		Iterator<TraceComponent> i = modelComponents.iterator();
		while (i != null && i.hasNext()) {
			// Get model component
			TraceComponent modelComponent = i.next();

			// Create new activation component
			TraceActivationComponentItem componentItem = new TraceActivationComponentItem(
					modelComponent.getId(), modelComponent.getName());
			componentItem.setFilePath(modelComponent.getDefinedInFilePath());

			// Go through groups
			Iterator<TraceGroup> j = modelComponent.getGroups().iterator();
			while (j != null && j.hasNext()) {
				// Get model groups
				TraceGroup modelGroup = j.next();

				// Create new activation group
				TraceActivationGroupItem groupItem = new TraceActivationGroupItem(
						componentItem, modelGroup.getId(), modelGroup.getName());

				// Check if also traces are wanted
				if (getAlsoTraces) {

					// Go through traces
					Iterator<Trace> k = modelGroup.getTraces().iterator();
					while (k != null && k.hasNext()) {
						// Get model traces
						Trace modelTrace = k.next();

						// Create new activation trace
						TraceActivationTraceItem traceItem = new TraceActivationTraceItem(
								groupItem, modelTrace.getId(), modelTrace
										.getName());

						TraceData traceData = modelTrace.getTraceData();

						if (traceData != null) {
							ArrayList<DecodeParameter> parameters = traceData
									.getDecodeParameters();

							// Calculate parameter count
							int paramCount = 0;
							for (int l = 0; l < parameters.size(); l++) {
								if (parameters.get(l).getSize() != 0) {
									paramCount++;
								}
							}

							traceItem.setParameterCount(paramCount);
						}
					}
				}
			}

			// Add this component to activationInformation list
			activationComponents.add(componentItem);
		}

		return activationComponents;
	}
}
