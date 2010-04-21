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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;

/**
 * SWMT Analyser specific input to the SWMT Editor.
 * 
 */
public class SWMTEditorInput implements IEditorInput {

	private ParsedData parsedData = new ParsedData();
	private OverviewData overview = new OverviewData();
	
	/**
	 * Get parsed data
	 * @return parsed data
	 */
	public ParsedData getParsedData() {
		return parsedData;
	}
	/**
	 * Set parsed data
	 * @param parsedData
	 */
	public void setParsedData(ParsedData parsedData) {
		this.parsedData = parsedData;
	}
	/**
	 * Get overview
	 * @return overview
	 */
	public OverviewData getOverview() {
		return overview;
	}
	/**
	 * Set overview
	 * @param overview
	 */
	public void setOverview(OverviewData overview) {
		this.overview = overview;
	}
	/**
	 * Constructor
	 * @param parsedData
	 * @param ov
	 */
	public SWMTEditorInput(ParsedData parsedData, OverviewData ov) {
		this.parsedData = parsedData;
		this.overview = ov;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return "Result of " + parsedData.getNumberOfCycles() + " SWMT logs";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return "Result of " + parsedData.getNumberOfCycles()+" SWMT Log files";
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if(!(o instanceof SWMTEditorInput))
			return false;
		
		SWMTEditorInput input = (SWMTEditorInput)o;
		
		CycleData[] currentLogData = this.parsedData.getLogData();
		CycleData[] givenLogData = input.parsedData.getLogData();
		
		if (this.parsedData.getNumberOfCycles() != input.parsedData.getNumberOfCycles())
			return false;
		else if(currentLogData == null || givenLogData== null)
			return false;
		else
		{
			for (int i=0; i<currentLogData.length; i++) {
				if(!currentLogData[i].getFileName().equals(givenLogData[i].getFileName()))
					return false;
			}
		}
		return true;
		
	}
}
