/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.pi.irq;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTable;

/**
 * Label provider for swi thread table
 */
public class SwiThreadLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	/* thread table */
	Table table;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            thread table
	 */
	public SwiThreadLabelProvider(Table table) {
		super();
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		int columnId = ((Integer) table.getColumn(columnIndex).getData())
				.intValue();

		switch (columnId) {
		// thread name
		case GenericTable.COLUMN_ID_SWI_THREAD:

			if (element instanceof SwiThreadWrapper) {
				SwiThreadWrapper profiledItem = (SwiThreadWrapper) element;
				return profiledItem.threadName;
			}
			break;

		// address
		case GenericTable.COLUMN_ID_ADDRESS:
			if (element instanceof SwiThreadWrapper) {
				SwiThreadWrapper profiledItem = (SwiThreadWrapper) element;
				return Messages.SwiThreadLabelProvider_0 + Long.toHexString(profiledItem.threadAddress);
			}
		default: {
			break;
		}
		}
		// should never get here
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
