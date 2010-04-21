/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class MMPInfo
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import com.nokia.s60tools.analyzetool.global.Util;

/**
 * Contains one mmp(module) file info.
 *
 * @author kihe
 *
 */
public class MMPInfo {

	/** String mmp file name. */
	private final String mmpName;

	/** mmp file path. */
	private String mmpFilePath = "";

	/** mmp file target. */
	private String mmpTarget = "";

	/** Is this mmp(module) build successfully. */
	private boolean buildSuccesfully = false;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            MMP file name
	 */
	public MMPInfo(final String name) {
		mmpName = name;
	}

	/**
	 * Gets mmp file location.
	 *
	 * @return MMP file location if it is set otherwise ""
	 */
	public final String getLocation() {
		return mmpFilePath;
	}

	/**
	 * Returns mmp file name.
	 *
	 * @return MMP file name
	 */
	public final String getName() {
		return mmpName;
	}

	/**
	 * Returns mmp file target.
	 *
	 * @return MMP file target
	 */
	public final String getTarget() {
		return mmpTarget;
	}

	/**
	 * Returns info is mmp file built successfully.
	 *
	 * @return True if module is build successfully otherwise False
	 */
	public final boolean isBuildSuccesfully() {
		return buildSuccesfully;
	}

	/**
	 * Based on given MMP file checks is the module already 
	 * built with AnalyzeTool and sets the internal data.
	 *
	 * @param location
	 *            MMP file location
	 */
	private final void setBuildInfo(final String location) {
		if( location != null ) {
			buildSuccesfully = Util.isModuleBuilt(location);	
		}
	}

	/**
	 * Sets the MMP file location. 
	 * Checks that file exists for the given location and updates internal data.
	 *
	 * @param location
	 *            MMP file location
	 */
	public final void setLocation(final String location) {
		IFile file = null;

		// try to find file
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(
				new Path(location));

		// file found from workspace store file location
		if (file.exists()) {
			mmpFilePath = file.getLocation().toOSString();

		}
		setBuildInfo(mmpFilePath);
	}

	/**
	 * Sets mmp file target.
	 *
	 * @param target
	 *            MMP file target
	 */
	public final void setTarget(final String target) {
		mmpTarget = target;
	}
}
