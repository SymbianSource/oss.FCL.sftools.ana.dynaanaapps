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
 * Handler creator
 *
 */
package com.nokia.trace.dictionary.model.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.nokia.trace.dictionary.model.DictionaryDecodeModel;

/**
 * Creates Dictionary handlers
 * 
 */
public class DictionaryHandlerCreator {

	/**
	 * Model
	 */
	private DictionaryDecodeModel model;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 */
	public DictionaryHandlerCreator(DictionaryDecodeModel model) {
		this.model = model;
	}

	/**
	 * Creates start handlers and puts them into this array
	 * 
	 * @return arrayList of start handlers
	 */
	public ArrayList<DictionaryHandler> createStartHandlers() {
		ArrayList<DictionaryHandler> handlers = new ArrayList<DictionaryHandler>();

		// Add start handlers. Handlers that are commented out have no use in
		// this time. In the future, they can be commented in and implemented

		handlers.add(new ComponentHandler(model));
		// handlers.add(new DataHandler(model));
		handlers.add(new DefHandler(model));
		// handlers.add(new ExternalDefHandler(model));
		handlers.add(new FileHandler(model));
		handlers.add(new GroupHandler(model));
		handlers.add(new InstanceHandler(model));
		// handlers.add(new LocationsHandler(model));
		// handlers.add(new MetaDataHandler(model));
		handlers.add(new ObjectHandler(model));
		handlers.add(new OptionsHandler(model));
		handlers.add(new PathHandler(model));
		handlers.add(new TraceHandler(model));
		// handlers.add(new TraceDictionaryHandler(model));
		// handlers.add(new TypeDefHandler(model));
		handlers.add(new TypeMemberHandler(model));

		// Sort and return the handlers
		sortHandlers(handlers);
		return handlers;
	}

	/**
	 * Creates end handlers and puts them into this array
	 * 
	 * @return arrayList of end handlers
	 */
	public ArrayList<DictionaryHandler> createEndHandlers() {
		ArrayList<DictionaryHandler> handlers = new ArrayList<DictionaryHandler>();

		// Add end handlers. Handlers that are commented out have no use in this
		// time. In the future, they can be commented in if they need to
		// implement processEndElement function.

		handlers.add(new ComponentHandler(model));
		// handlers.add(new DataHandler(model));
		handlers.add(new DefHandler(model));
		// handlers.add(new ExternalDefHandler(model));
		handlers.add(new FileHandler(model));
		handlers.add(new GroupHandler(model));
		handlers.add(new InstanceHandler(model));
		// handlers.add(new LocationsHandler(model));
		// handlers.add(new MetaDataHandler(model));
		// handlers.add(new ObjectHandler(model));
		handlers.add(new OptionsHandler(model));
		// handlers.add(new PathHandler(model));
		handlers.add(new TraceHandler(model));
		// handlers.add(new TraceDictionaryHandler(model));
		// handlers.add(new TypeDefHandler(model));
		// handlers.add(new TypeMemberHandler(model));

		// Sort and return the handlers
		sortHandlers(handlers);
		return handlers;
	}

	/**
	 * Sorts the handlers by the name attribute
	 * 
	 * @param handlers
	 *            the dictionary handlers
	 */
	public void sortHandlers(ArrayList<DictionaryHandler> handlers) {
		Collections.sort(handlers, new Comparator<DictionaryHandler>() {

			/**
			 * Compares two handler names
			 * 
			 * @param o1
			 *            first handler
			 * @param o2
			 *            second handler
			 * @return less than zero if first one is first in alphabetic order.
			 *         More than zero if second is first.
			 */
			public int compare(DictionaryHandler o1, DictionaryHandler o2) {
				int val = o1.getName().compareTo(o2.getName());
				return val;
			}
		});
	}
}
