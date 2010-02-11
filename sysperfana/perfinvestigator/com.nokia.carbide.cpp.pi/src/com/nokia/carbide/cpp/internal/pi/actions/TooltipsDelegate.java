/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.internal.pi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cpp.pi.core.SessionPreferences;

public class TooltipsDelegate implements IWorkbenchWindowActionDelegate {

	private boolean toolTipsEnabled  = true;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		toolTipsEnabled = SessionPreferences.getInstance().getToolTipsEnabled();
	}

	public void run(IAction action) {
		toolTipsEnabled = !toolTipsEnabled;
		SessionPreferences.getInstance().setToolTipsEnabled(toolTipsEnabled);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
