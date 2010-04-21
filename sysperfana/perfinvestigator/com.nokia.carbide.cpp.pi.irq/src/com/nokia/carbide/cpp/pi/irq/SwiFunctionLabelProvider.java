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
 * LabelProvider for software function table
 */
public class SwiFunctionLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	/* function table */
	Table table;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            table
	 */
	public SwiFunctionLabelProvider(Table table) {
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
		// name of the function
		case GenericTable.COLUMN_ID_SWI_FUNCTION:

			if (element instanceof IrqSampleTypeWrapper) {
				IrqSampleTypeWrapper item = (IrqSampleTypeWrapper) element;
				if (item.getPrototypeSample().getFunction() != null
						&& item.getPrototypeSample().getFunction().getFunctionName() != null) {
					return item.getPrototypeSample().getFunction().getFunctionName();
				} else {
					return Messages.SwiFunctionLabelProvider_0
							+ Long.toHexString(item.getPrototypeSample()
									.getLrValue()) + Messages.SwiFunctionLabelProvider_1;
				}
			}

			// return address
		case GenericTable.COLUMN_ID_RETURN_ADDRESS:

			if (element instanceof IrqSampleTypeWrapper) {
				IrqSampleTypeWrapper item = (IrqSampleTypeWrapper) element;
				if (item.getPrototypeSample().getFunction() != null) {
					return Messages.SwiFunctionLabelProvider_2
							+ Long.toHexString(item.getPrototypeSample()
									.getFunction().getStartAddress());
				}
			}

			// count of interrupts in selection area
		case GenericTable.COLUMN_ID_SWI_COUNT:
			if (element instanceof IrqSampleTypeWrapper) {
				IrqSampleTypeWrapper profiledItem = (IrqSampleTypeWrapper) element;
				return Integer.toString(profiledItem.count);
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
