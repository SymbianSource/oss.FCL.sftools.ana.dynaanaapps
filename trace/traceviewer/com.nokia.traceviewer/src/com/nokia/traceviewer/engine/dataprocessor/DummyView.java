/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Dummy View to be a placeholder for real TraceViewer views
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerViewInterface;

/**
 * Dummy View to be a placeholder for real TraceViewer views
 * 
 */
public final class DummyView implements TraceViewerViewInterface, DataProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#clearAll()
	 */
	public void clearAll() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerViewInterface#hasUnshownData()
	 */
	public boolean hasUnshownData() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#isDisposed()
	 */
	public boolean isDisposed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#update()
	 */
	public void update() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.dataprocessor.DataProcessor#processData(
	 * com.nokia.traceviewer.engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
	}

}