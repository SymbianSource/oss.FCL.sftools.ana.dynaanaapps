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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.nokia.s60tools.swmtanalyser.analysers.AnalyserConstants;
import com.nokia.s60tools.swmtanalyser.analysers.ResultElements;
import com.nokia.s60tools.swmtanalyser.dialogs.AdvancedFilterDialog.FilterInput;

/**
 * Customized ViewerFilter for TableViewer in Analysis tab.
 *
 */
public class IssuesFilter extends ViewerFilter {

	//Filter text
	private String search_key = null;
	//Settings selected in the advanced filter options dialog
	private FilterInput adv_options = null;
	
	/**
	 * Set filter text
	 * @param value
	 */
	public void setFilterText(String value)
	{
		search_key = value;
	}

	/**
	 * Set advanced filter to issues
	 * @param adv_option
	 */
	public void setAdvancedSearchOptions(FilterInput adv_option)
	{
		this.adv_options = adv_option;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		//If the search is done using the advanced filter options dialog
		if(adv_options!=null)
		{
			int filter_option = adv_options.getName_Filter_option();
			String filter_text = adv_options.getFilter_text();
			List<String> events = new ArrayList<String>();
			if(adv_options.getEvents()!=null)
				events = Arrays.asList(adv_options.getEvents());
			
			List<AnalyserConstants.Priority> severities = new ArrayList<AnalyserConstants.Priority>();
			if(adv_options.getSeverities()!=null)
				severities = Arrays.asList(adv_options.getSeverities());
			
			int delta_bytes_option = adv_options.getDelta_bytes_option();
			long s1 = adv_options.getStart_size();
			long e1 = adv_options.getEnd_size();
			
			int delta_count_option = adv_options.getDelta_count_option();
			long s2 = adv_options.getStart_count();
			long e2 = adv_options.getEnd_count();
			
			if(element instanceof ResultElements)
			{
				ResultElements item = (ResultElements)element;
				String item_name = item.getItemName();
				String event = item.getEvent();
				long delta = item.getDeltaValue();
				AnalyserConstants.Priority p = item.getPriority();
				
				boolean flag = true;
				
				if(filter_option == 0 && filter_text!=null && !item_name.toLowerCase().startsWith(filter_text.toLowerCase()))
					return false;
				if(filter_option == 1 && filter_text!=null && item_name.toLowerCase().indexOf(filter_text.toLowerCase()) == -1)
					return false;
				if(events.size()>0 && !events.contains(event))
					return false;
				if(severities.size()>0 && !severities.contains(p))
					return false;
				if(item.getType().compareTo(AnalyserConstants.DeltaType.SIZE)==0)
				{
					switch (delta_bytes_option) {
					case 0:
						//flag = flag && true;
						break;
					case 1:
						if(delta < s1 || delta > e1)
							return false;
						break;
					case 2:
						if(delta != s1)
							return false;
						break;
					case 3:
						if(delta < s1)
							return false;
						break;
					case 4:
						if(delta > s1)
							return false;
						break;

					default:
						break;
					}
				}
				else if(item.getType().compareTo(AnalyserConstants.DeltaType.COUNT)==0)
				{
					switch (delta_count_option) {
					case 0:
						//flag = flag && true;
						break;
					case 1:
						if(delta < s2 || delta > e2)
							return false;
						break;
					case 2:
						if(delta != s2)
							return false;
						break;
					case 3:
						if(delta < s2)
							return false;
						break;
					case 4:
						if(delta > s2)
							return false;
						break;

					default:
						break;
					}
				}
				
				return flag;
			}
		}
		
		//If searching is cancelled. 
		if(search_key == null)
			return true;
		
		//If search is done using the severity dropdown in the Analysis tab.
		if(element instanceof ResultElements)
		{
			ResultElements item = (ResultElements)element;
			AnalyserConstants.Priority p = item.getPriority();
			if(p.name().toLowerCase().matches(search_key.toLowerCase()))
				return true;
			else
				return false;
		}
		return true;
	}
}
