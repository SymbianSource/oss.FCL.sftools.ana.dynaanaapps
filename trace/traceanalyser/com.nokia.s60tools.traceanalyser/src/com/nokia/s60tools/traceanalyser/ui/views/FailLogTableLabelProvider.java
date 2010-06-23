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



package com.nokia.s60tools.traceanalyser.ui.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import com.nokia.s60tools.traceanalyser.export.RuleEvent;

/**
 * class FailLogTableLabelProvider
 * fail log label provider.
 */
class FailLogTableLabelProvider extends LabelProvider implements ITableLabelProvider {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object obj, int index) {
		RuleEvent logItem = (RuleEvent)obj;
		return logItem.getFailLogText(index);
		
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object obj, int index) {
		/*if (index == MemSpyFileBundle.INDEX_FILE_TYPE){
			return getImage(obj);
		}
		else{
			return null;
		}*/
		return null;

	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		/*MemSpyFileBundle bundle = (MemSpyFileBundle)obj;
		if( bundle.isHeapDumpFile() ){
			return ImageResourceManager.getImage(ImageKeys.IMG_HEAP_DUMP);
		}
		else{
			return ImageResourceManager.getImage(ImageKeys.IMG_SWMT_LOG);
		}*/
		return PlatformUI.getWorkbench().
		getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}