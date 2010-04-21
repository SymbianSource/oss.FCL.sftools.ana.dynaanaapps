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

package com.nokia.carbide.cpp.pi.internal.perfcounters;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendLabelProvider;

/**
 * Performance Counter Legend: LabelProvider for TableViewer 
 */
public class PecLegendLabelProvider extends PecCommonLegendLabelProvider {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	@Override
	public String getColumnText(Object element, int columnIndex) {
		String s = null;
		
		if (element instanceof PecLegendElement){
			PecLegendElement le = (PecLegendElement)element;
			
			switch (columnIndex) {
							
			case PecLegend.COLUMN_PER_A:
				if (le.getCnt() > 0 && !le.isCoreValuesOnly()){
					s = String.format(Messages.PecLegendLabelProvider_4, le.getxOverY(0));
				}
				break;
				
			case PecLegend.COLUMN_PER_B:
				if (le.getCnt() > 0 && !le.isCoreValuesOnly()){
					s = String.format(Messages.PecLegendLabelProvider_5, le.getxOverY(1));
				}
				break;
				
			case PecLegend.COLUMN_PER_C:
				if (le.getCnt() > 0 && !le.isCoreValuesOnly()){
					s = String.format(Messages.PecLegendLabelProvider_6, le.getxOverY(2));
				}
				break;
				
			default:
				return super.getColumnText(element, columnIndex);
			}
			
		}
		return s == null ? EMPTY_STRING : s;
	}
	

}
