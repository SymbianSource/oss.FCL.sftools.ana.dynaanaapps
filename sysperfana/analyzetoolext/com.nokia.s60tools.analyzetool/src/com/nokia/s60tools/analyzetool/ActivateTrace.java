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
* Description:  Definitions for the class ActivateTrace
 *
*/


package com.nokia.s60tools.analyzetool;

import org.eclipse.ui.IStartup;
import org.eclipse.core.runtime.*;
import com.nokia.s60tools.analyzetool.ui.IActionListener;
import com.nokia.s60tools.analyzetool.ui.MainView;

/**
 * Checks at plugin startup is trace plugin available
 * and sets MainView trace actions state
 * @author kihe
 *
 */
public class ActivateTrace implements IStartup {

	/**
	 * Check is AnalyzeTool trace plugin registered.
	 *
	 * If not registered disables AnalyzeTool trace buttons.
	 *
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public final void earlyStartup() {

		//get action listener
		final IActionListener listener = Activator.getActionListener();

		//get extension registry
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		if( registry == null ) {
			MainView.enableTrace = false;
			if(listener != null) {
				listener.disableTraceActions(false);
			}
			return;
		}

		//get analyzetool extension
		final IExtensionPoint extensionPoint = registry.getExtensionPoint("com.nokia.s60tools.analyzetool.AnalyzeTool");

		//try find analyzetool trace extension
		IExtension extension = null;
		if( extensionPoint != null ) {
			extension = extensionPoint.getExtension("com.nokia.s60tools.analyzetool.trace.ActivateTrace");
		}



		//if trace extension found => enable trace buttons in the MainView
		if(extension != null && extension.isValid()) {
			MainView.enableTrace = true;
			if(listener != null) {
				listener.disableTraceActions(true);
			}
		}
		//no trace extension found => disable trace buttons
		else {
			MainView.enableTrace = false;
			if(listener != null) {
				listener.disableTraceActions(false);
			}
		}

	}
}
