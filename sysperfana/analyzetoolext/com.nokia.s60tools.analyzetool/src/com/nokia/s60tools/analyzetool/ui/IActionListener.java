/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ActionListener
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.core.resources.IProject;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Notifies when preferences is changed or action must execute.
 *
 * @author kihe
 *
 */
public interface IActionListener {

	/**
	 * Informs when all the modules are built with AnalyzeTool.
	 *
	 * @param projRef
	 *            Project reference
	 */
	void buildStateChanged(IProject projRef);


	/**
	 * Disables or enables trace related actions If TraceViewer plugin could not
	 * loaded the trace actions are disable at plugin startup.
	 *
	 * @param disable
	 *            Boolean
	 */
	void disableTraceActions(boolean disable);

	/**
	 * Notifies when preference values are changed.
	 */
	void preferenceChanged();

	/**
	 * Notifies when action must execute.
	 *
	 * @param project
	 *            Project reference
	 *
	 * @param action
	 *            Action to execute
	 */
	void runAction(org.eclipse.core.resources.IProject project,
			Constants.ACTIONS action);

}
