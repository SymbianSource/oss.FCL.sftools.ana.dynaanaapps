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
 * LabelProvider for irq line table
 */
public class IrqLineLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	/* irq table */
    Table table;

    /**
     * constructor
     * @param table irq table
     */
	public IrqLineLabelProvider(Table table) {
		super();
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
        int columnId = ((Integer) table.getColumn(columnIndex).getData()).intValue();

		
		
		switch (columnId)
		{
			case GenericTable.COLUMN_ID_IRQ_LINE:
				
				// name of the irq line
				if (element instanceof IrqSampleTypeWrapper){
					IrqSampleTypeWrapper item = (IrqSampleTypeWrapper) element;
					return IrqSampleTypeWrapper.getLineText(item.getPrototypeSample().getIrqL1Value());
				}
				
				/* Bappea implementation
					if(element instanceof Long){
					Long value = (Long) element;
					return "IRQ interrupt L1 0x" + Long.toHexString(value);
					/*if(value != 0)
					{
						this.setText("IRQ interrupt L1 0x"+Integer.toHexString(valueL1)+" ("+valueL1+")");
					}
					else
					{
						int valueL2 = stw.prototypeSample.getIrqL2Value();
						this.setText("IRQ interrupt L2 0x"+Integer.toHexString(valueL2)+" ("+(32+valueL2)+")");				
					}	*/
				//}
				break;
				
			case GenericTable.COLUMN_ID_IRQ_COUNT:
				// count of interrupts in the selection area
				if (element instanceof IrqSampleTypeWrapper){
					IrqSampleTypeWrapper item = (IrqSampleTypeWrapper) element;
					return Integer.toString(item.count);
				}
			default:
			{
				break;
			}
		}
		// should never get here
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
