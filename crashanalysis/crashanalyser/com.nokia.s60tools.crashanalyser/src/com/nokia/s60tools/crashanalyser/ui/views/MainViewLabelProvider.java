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
*/

package com.nokia.s60tools.crashanalyser.ui.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nokia.s60tools.crashanalyser.model.CrashFileBundle;
import com.nokia.s60tools.crashanalyser.resources.ImageKeys;
import com.nokia.s60tools.crashanalyser.resources.ImageResourceManager;

/**
 * Label provider for MainView's table. Provides a correct image
 * for a row in MainView. Provides also a text for each column in a row.
 *
 */
public class MainViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	
	/**
	 * Returns text for a column defined by index
	 */
	public String getColumnText(Object obj, int index) {
		CrashFileBundle cFileBundle = (CrashFileBundle)obj;
		return cFileBundle.getText(index);
	}
	
	/**
	 * Returns an image for first column only. Returns null
	 * for other columns.
	 */
	public Image getColumnImage(Object obj, int index) {
		if (index == CrashFileBundle.INDEX_TIME)
			return getImage(obj);
		else
			return null;
	}
	
	/**
	 * Returns a correct image for a CrashFileBundle type. 
	 * Correct image depends on which types of files CrashFileBundle contains.
	 */
	public Image getImage(Object obj) {
		CrashFileBundle cFile = (CrashFileBundle)obj;

		// empty file does not need an image
		if (cFile.isEmpty()) {
			return null;
		// image for emulator panic
		} else if (cFile.isEmulatorPanic()) {
			return ImageResourceManager.getImage(ImageKeys.EMULATOR_PANIC);
		// decoded file
		} else if (cFile.isFullyDecoded()) {
			return ImageResourceManager.getImage(ImageKeys.DECODED_FILE);
		// summary file
		} else if (cFile.isPartiallyDecoded()) {
			return ImageResourceManager.getImage(ImageKeys.PARTIALLY_DECODED_FILE);
		// undecoded file
		} else {
			return ImageResourceManager.getImage(ImageKeys.CODED_FILE);
		}
	}
}
