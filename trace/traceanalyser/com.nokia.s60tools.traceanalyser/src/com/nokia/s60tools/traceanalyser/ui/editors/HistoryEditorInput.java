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



package com.nokia.s60tools.traceanalyser.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import com.nokia.s60tools.traceanalyser.containers.RuleInformation;

/**
 * Trace Analyser specific input to the HistoryEditor.
 * 
 */
public class HistoryEditorInput implements IEditorInput {

	/* Event history */
	private RuleInformation events;

	/**
	 * HistoryEditorInput.
	 * constructor
	 * @param events history events.
	 */
	public HistoryEditorInput(RuleInformation events) {
		this.events = events;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return events.getRule().getName();
	}

	/*
	 * (non-Javadoc)
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
		if(!(o instanceof HistoryEditorInput)){
			return false;
		}
		HistoryEditorInput otherInput = (HistoryEditorInput) o;
		return otherInput.getEvents().getRule().getName().equals(events.getRule().getName());
		
		
	}
	
	/**
	 * getEvents.
	 * @return history events
	 */
	public RuleInformation getEvents() {
		return events;
	}
	
	/**
	 * getName
	 * @return name of the rule.
	 */
	public String getName() {
		return events.getRule().getName();
	}
}
