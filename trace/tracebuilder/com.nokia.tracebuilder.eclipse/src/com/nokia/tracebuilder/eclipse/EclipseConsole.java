/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Eclipse message console
*
*/
package com.nokia.tracebuilder.eclipse;

import java.io.OutputStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import com.nokia.tracebuilder.utils.ConsoleBase;

/**
 * Eclipse message console
 * 
 */
public final class EclipseConsole extends ConsoleBase {

	/**
	 * Creates a console stream
	 * 
	 * @return the stream
	 */
	@Override
	protected OutputStream createStream() {
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		MessageConsole console = null;
		IConsole[] consoles = manager.getConsoles();
		for (IConsole c : consoles) {
			if (c instanceof MessageConsole && getTitle().equals(c.getName())) {
				console = (MessageConsole) c;
				break;
			}
		}
		if (console == null) {
			console = new MessageConsole(getTitle(), null);
			manager.addConsoles(new IConsole[] { console });
		} else {
			console.clearConsole();
		}
		manager.showConsoleView(console);
		return console.newOutputStream();
	}

}
