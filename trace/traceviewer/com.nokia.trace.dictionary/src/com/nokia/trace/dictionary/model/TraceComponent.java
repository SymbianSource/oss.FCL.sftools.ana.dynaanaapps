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
 * Component
 *
 */
package com.nokia.trace.dictionary.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Component
 * 
 */
public class TraceComponent extends DecodeObject {

	/**
	 * Prefix of the component
	 */
	private String prefix;

	/**
	 * Suffix of the component
	 */
	private String suffix;

	/**
	 * Model this component belongs to
	 */
	private DictionaryDecodeModel model;

	/**
	 * File path where this component is defined
	 */
	private String definedInFile;

	/**
	 * List of groups belonging to this component
	 */
	private ArrayList<TraceGroup> groups;

	/**
	 * Constructor Should be only used from DecodeObjectFactory
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param prefix
	 *            prefix
	 * @param suffix
	 *            suffix
	 * @param filePath
	 *            file path
	 * @param model
	 *            model
	 */
	public TraceComponent(int id, String name, String prefix, String suffix,
			String filePath, DictionaryDecodeModel model) {
		super(id, name);
		this.prefix = prefix;
		this.suffix = suffix;
		this.model = model;
		this.definedInFile = filePath;
		groups = new ArrayList<TraceGroup>();
	}

	/**
	 * Adds group to the list to right position
	 * 
	 * @param group
	 *            group to be added
	 * @return null if adding succeeded, old group if group with same ID already
	 *         exists.
	 */
	public TraceGroup addGroup(TraceGroup group) {
		TraceGroup returnGroup = null;
		int pos = Collections.binarySearch(groups, group,
				new Comparator<TraceGroup>() {

					public int compare(TraceGroup o1, TraceGroup o2) {
						int id1 = o1.getId();
						int id2 = o2.getId();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		if (pos < 0) {
			groups.add(-pos - 1, group);
		} else {
			returnGroup = groups.get(pos);
		}
		return returnGroup;

	}

	/**
	 * Gets group for specific id
	 * 
	 * @param id
	 *            id of the group
	 * @return the group
	 */
	public TraceGroup getGroup(int id) {
		int pos = Collections.binarySearch(groups, Integer.valueOf(id),
				new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						int id1 = ((TraceGroup) o1).getId();
						int id2 = ((Integer) o2).intValue();
						return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
					}

				});
		TraceGroup group = null;
		if (pos >= 0) {
			group = groups.get(pos);
		}
		return group;

	}

	/**
	 * Gets all groups as a list
	 * 
	 * @return all groups
	 */
	public ArrayList<TraceGroup> getGroups() {
		return groups;
	}

	/**
	 * Gets prefix
	 * 
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets prefix
	 * 
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Gets suffix
	 * 
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * Sets suffix
	 * 
	 * @param suffix
	 *            the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Gets file where this component is defined in
	 * 
	 * @return the file path
	 */
	public String getDefinedInFilePath() {
		return definedInFile;
	}

	/**
	 * Gets the model
	 * 
	 * @return the model
	 */
	public DictionaryDecodeModel getModel() {
		return model;
	}

}
