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
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.swmtanalyser.analysers.AnalyserConstants;
import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.analysers.ResultsParentNodes;

/**
 * LableProvider for the tree viewer in the Analysis tab
 *
 */
public class IssuesTreeLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int column_index) {

		if(column_index == 1)
		{
			if(element instanceof ResultsParentNodes)
			{
				return ((ResultsParentNodes)element).toString();
			}
			else if(element instanceof ResultElements)
			{
				return ((ResultElements)element).getItemName();
			}
		}
		else if(column_index == 2)
		{
			if(element instanceof ResultElements)
				return ((ResultElements)element).getEvent();
		}
		else if(column_index == 3)
		{
			if(element instanceof ResultElements)
			return ((ResultElements)element).getDelta();
		}
		else if(column_index == 4)
		{
			if(element instanceof ResultElements)
				return ((ResultElements)element).getPriority().name();
		}
		//For internal testing.
		/*else if(arg1 == 4)
		{
			if(arg0 instanceof ResultElements)
				return ((ResultElements)arg0).getGrowing_factor()+"";
		}*/
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener arg0) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener arg0) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		
		if(element instanceof ResultElements )
		{
			ResultElements item = (ResultElements)element;
			
			if(columnIndex == 4)
			{
				Color background_color = null;
				
				switch (item.getPriority()) {
				case CRITICAL:
					background_color = AnalyserConstants.COLOR_SEVERITY_CRITICAL; 
					break;
				case HIGH:
					background_color = AnalyserConstants.COLOR_SEVERITY_HIGH; 
					break;
				case NORMAL:
					background_color = AnalyserConstants.COLOR_SEVERITY_NORMAL; 
					break;

				default:
					break;
			}
				return background_color;
			}
			else if(columnIndex == 0)
			{
				return item.getColor();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
	 */
	public Font getFont(Object arg0, int arg1) {
		if(arg0 instanceof ResultsParentNodes)
		{
			Font f = Display.getCurrent().getSystemFont();
			return new Font(Display.getCurrent(), f.getFontData()[0].getName(), 8, SWT.BOLD);
		}
		return null;
	}
	
}
