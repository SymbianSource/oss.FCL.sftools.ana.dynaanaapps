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
package com.nokia.s60tools.swmtanalyser.analysers;

import java.util.ArrayList;
/**
 * Input object(parent) for tree viewer in analysis tab.
 *
 */
public class ResultsParentNodes{

	private String description;
	private ArrayList<ResultElements> children;
	private ArrayList<ResultElements> filteredChildren = new ArrayList<ResultElements>();
	
    /**
     * Construction
     * @param description
     */
    ResultsParentNodes(String description)
	{
		this.description = description;
	}
	
	/**
	 * Set the childrens of this element
	 * @param childArrayList
	 */
	public void setChildren(ArrayList<ResultElements> childArrayList)
	{
		this.children = childArrayList;
	}

	/**
	 * Get childrens
	 * @return childrens as {@link ResultElements}
	 */
	public Object[] getChildren()
	{
		if(children != null)
			return children.toArray(new ResultElements[0]);
		else
			return null;
	}
	
	/**
	 * Get description 
	 */
	public String toString()
	{
		return this.description;
	}
	
	/**
	 * Add a filtered children
	 * @param child
	 */
	public void addFilteredChild(ResultElements child)
	{
		filteredChildren.add(child);
	}
	
	/**
	 * Get all filtered childrens
	 * @return
	 */
	public ArrayList<ResultElements> getFilteredChildrenList()
	{
		return filteredChildren;
	}
	/**
	 * Get count for filtered childrens
	 * @return count
	 */
	public int getFilteredCount()
	{
		return filteredChildren.size();
	}
}
