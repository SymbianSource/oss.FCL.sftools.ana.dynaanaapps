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

package com.nokia.carbide.cpp.internal.pi.address.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cpp.pi.address.AddressPlugin;

public class BarGraphDelegate implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		// get the active page
		if (window == null)
			return;

		if (action.isChecked()) {
			AddressPlugin.getDefault().receiveSelectionEvent("setBarOn"); //$NON-NLS-1$
		}
		else {
			AddressPlugin.getDefault().receiveSelectionEvent("setBarOff"); //$NON-NLS-1$
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
