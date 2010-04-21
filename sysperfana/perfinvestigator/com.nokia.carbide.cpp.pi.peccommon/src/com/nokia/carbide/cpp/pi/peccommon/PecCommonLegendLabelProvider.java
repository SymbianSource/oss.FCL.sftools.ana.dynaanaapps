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

package com.nokia.carbide.cpp.pi.peccommon;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Performance Counter Legend: LabelProvider for TableViewer 
 */
public class PecCommonLegendLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String s = null;
		
		if (element instanceof PecCommonLegendElement){
			PecCommonLegendElement le = (PecCommonLegendElement)element;
			
			switch (columnIndex) {
			
			case PecCommonLegend.COLUMN_SHORT_TITLE:
				s = String.valueOf(le.getCharacter());
				break;
				
			case PecCommonLegend.COLUMN_NAME:
				s = le.getName(); 
				break;
				
			case PecCommonLegend.COLUMN_AVERAGE:
				if (le.getCnt() > 0){
					s = String.format(Messages.PecCommonLegendLabelProvider_0, le.getSum() / (float)le.getCnt());					
				}
				break;
				
			case PecCommonLegend.COLUMN_SUM:
				if (le.getCnt() > 0 && !le.isCoreValuesOnly()){
					s = String.format(Messages.PecCommonLegendLabelProvider_1, le.getSum());
				}
				break;
				
			case PecCommonLegend.COLUMN_MIN:
				if (le.getCnt() > 0){
					s = String.format(Messages.PecCommonLegendLabelProvider_2, le.getMin());
				}
				break;
				
			case PecCommonLegend.COLUMN_MAX:
				if (le.getCnt() > 0){
					s = String.format(Messages.PecCommonLegendLabelProvider_3, le.getMax());
				}
				break;
				
			default:
				break;
			}
			
		}
		return s == null ? EMPTY_STRING : s;
	}
	

}
