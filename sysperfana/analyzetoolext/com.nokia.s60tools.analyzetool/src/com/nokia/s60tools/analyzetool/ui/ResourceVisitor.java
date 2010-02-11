/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ResourceVisitor
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

import com.nokia.s60tools.analyzetool.internal.ui.graph.AnalyzeToolGraph;
import com.nokia.s60tools.analyzetool.ui.statistic.StatisticView;

/**
 * Class to visit resources in the project.
 *
 * @author araj modified kihe
 *
 */
public class ResourceVisitor implements IResourceVisitor {

	/** Parent class reference. */
	private final Object arg0;

	/**
	 * Constructor.
	 *
	 * @param parentClass Parent class reference
	 */
	public ResourceVisitor(final Object parentClass) {
		this.arg0 = parentClass;
	}

	/**
	 * Visit every resource of project.
	 *
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 *
	 * @param resource
	 *            One resource of the project
	 */
	public boolean visit(IResource resource) {

		// if visit request came from main view
		if (arg0 instanceof MainView) {
			MainView mainView = (MainView) arg0;
			mainView.loadFileInfo(resource);
		}
		else if( arg0 instanceof StatisticView )
		{
			StatisticView stats = (StatisticView)arg0;
			stats.loadFileInfo(resource);
		}
		else if(arg0 instanceof AnalyzeToolGraph) {
			AnalyzeToolGraph chart = (AnalyzeToolGraph)arg0;
			chart.loadFileInfo(resource);
		}
		return true;
	}

}
