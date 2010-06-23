/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Software component class
*
*/
package com.nokia.tracebuilder.engine;

/**
 * Software component class
 * 
 */
public class SoftwareComponent {

	/**
	 * Id
	 */
	private String id;

	/**
	 * Name
	 */
	private String name;

	/**
	 * MMP file path
	 */
	private String mmpFilePath;

	/**
	 * Constructor
	 * 
	 * @param componentId
	 *            component id
	 * @param componentName
	 *            component name
	 * @param mmpPath
	 *            MMP path
	 */
	public SoftwareComponent(String componentId, String componentName,
			String mmpPath) {
		id = componentId;
		name = componentName;
		mmpFilePath = mmpPath;
	}

	/**
	 * Get id
	 * 
	 * @return the id of the component
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get name
	 * 
	 * @return the name of the component
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get MMP file path
	 * 
	 * @return the component mmp file path
	 */
	public String getMMPFilePath() {
		return mmpFilePath;
	}

}
