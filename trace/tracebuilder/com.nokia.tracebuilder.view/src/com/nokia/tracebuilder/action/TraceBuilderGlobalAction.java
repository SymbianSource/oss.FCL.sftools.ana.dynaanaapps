/*
 * Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Base class for all global action objects of Trace Builder
 *
 */
package com.nokia.tracebuilder.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Base class for all global action objects of Trace Builder
 * 
 */
public abstract class TraceBuilderGlobalAction implements IHandler {

	/**
	 * Constructor
	 */
	public TraceBuilderGlobalAction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#addHandlerListener(org.eclipse.core
	 *      .commands.IHandlerListener)
	 */
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 *      ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if (TraceBuilderGlobals.isViewRegistered()) {
				doRun();
			}
		} catch (TraceBuilderException e) {
			TraceBuilderGlobals.getEvents().postError(e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#isHandled()
	 */
	public boolean isHandled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#removeHandlerListener(org.eclipse.
	 *      core.commands.IHandlerListener)
	 */
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	/**
	 * Runs this action
	 * 
	 * @throws TraceBuilderException
	 *             if processing fails, shows an error dialog based on the
	 *             exception content
	 */
	protected abstract void doRun() throws TraceBuilderException;

}
