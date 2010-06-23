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
 * Location class
 *
 */
package com.nokia.trace.dictionary.model;

/**
 * Location class
 * 
 */
public class Location {

	/**
	 * ID of the location
	 */
	private int id;

	/**
	 * Path of this location
	 */
	private Path path;

	/**
	 * File name of this location
	 */
	private String filename;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            id of the location
	 * @param path
	 *            path of the location
	 * @param fileName
	 *            file name of the location
	 */
	public Location(int id, Path path, String fileName) {
		this.id = id;
		this.path = path;
		this.filename = fileName;
	}

	/**
	 * Gets id
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets file name
	 * 
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets file name
	 * 
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Gets path
	 * 
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Sets path
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(Path path) {
		this.path = path;
	}
}
