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
package com.nokia.s60tools.swmtanalyser.editors;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for chunks/threads/disks/sys ebent tables
 *
 */
public class TableLabelColorProvider implements ITableColorProvider, ITableLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	public Color getBackground(Object arg0, int columnIndex) {
		
		if(arg0 instanceof TableViewerInputObject)
		{
			if(columnIndex == 0)
			{
				TableViewerInputObject obj =((TableViewerInputObject)arg0);
				return obj.getColor();
			}
		}
		else if(arg0 instanceof GraphedItemsInput)
		{
			GraphedItemsInput item = (GraphedItemsInput)arg0;
			Color color = item.getColor();
			if(columnIndex == 0)
				return color;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	public Color getForeground(Object arg0, int arg1) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object arg0, int columnIndex) {
		if(arg0 instanceof TableViewerInputObject)
		{
			if(columnIndex == 1)
				return ((TableViewerInputObject)arg0).getName();
		}
		else if(arg0 instanceof GraphedItemsInput)
		{
			GraphedItemsInput item = (GraphedItemsInput)arg0;
			String value=null; 
			switch(columnIndex)
			{
			case 0:
				value = null;
				break;
			case 1:
				value = item.getName();
				break;
			case 2:
				value = item.getEvent();
				break;
			case 3:
				value = item.getType();
				break;	
			}
			return value;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener arg0) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener arg0) {
	}

}
