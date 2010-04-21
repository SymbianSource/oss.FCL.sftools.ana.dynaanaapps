/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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



package com.nokia.s60tools.memspy.ui.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.nokia.s60tools.memspy.model.MemSpyFileBundle;
import com.nokia.s60tools.memspy.resources.ImageKeys;
import com.nokia.s60tools.memspy.resources.ImageResourceManager;

class MemSpyMainViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	/**
	 * Returns text for a column defined by index
	 */
	public String getColumnText(Object obj, int index) {
		MemSpyFileBundle cFileBundle = (MemSpyFileBundle)obj;
		return cFileBundle.getText(index);
	}
	public Image getColumnImage(Object obj, int index) {
		if (index == MemSpyFileBundle.INDEX_FILE_TYPE){
			return getImage(obj);
		}
		else{
			return null;
		}
	}
	public Image getImage(Object obj) {
		MemSpyFileBundle bundle = (MemSpyFileBundle)obj;
		if( bundle.hasHeapDumpFile() ){
			return ImageResourceManager.getImage(ImageKeys.IMG_HEAP_DUMP);
		}
		else{
			return ImageResourceManager.getImage(ImageKeys.IMG_SWMT_LOG);
		}
	}
}