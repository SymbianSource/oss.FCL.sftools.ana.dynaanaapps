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



package com.nokia.s60tools.traceanalyser.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nokia.s60tools.traceanalyser.model.Engine;

/**
 * class RuleTableContentProvider
 * content provider for rule table.
 */
class RuleTableContentProvider implements IStructuredContentProvider {
	
	/* Trace Analyser Engine */
	private final Engine engine;

	/**
	 * RuleTableContentProvider.
	 * constructor.
	 * @param engine 
	 */
	public RuleTableContentProvider(Engine engine){
		this.engine = engine;
	}
	
	/**
	 * refresh
	 * refreshes rule list.
	 */
	public void refresh() {
		engine.refreshRuleList();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		return engine.getRuleInformation();
	}
	
}
