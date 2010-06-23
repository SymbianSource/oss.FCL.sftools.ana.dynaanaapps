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
 * Trace Configuration class
 *
 */
package com.nokia.traceviewer.engine;

/**
 * Trace Configuration class
 * 
 */
public class TraceConfiguration {

	/**
	 * This trace is from scroller datareader. IMPORTANT: While scrolling, UI
	 * thread is put to wait until the trace block is read. This means that you
	 * cannot syncExec to the display when scrolled traces are coming. If you do
	 * so, the UI will block forever!
	 */
	private boolean scrolledTrace;

	/**
	 * This trace is filtered out -> don't show it. Can only be set true if
	 * filtering is actually enabled and datareaders are reading the filtered
	 * trace file.
	 */
	private boolean filteredOut;

	/**
	 * Start trigger is not found yet -> don't show this trace in the view. Can
	 * only be set true if triggering is actually enabled.
	 */
	private boolean triggeredOut;

	/**
	 * Show this trace in the view. If set to false, trace won't be processed by
	 * the view. However, if this is used as a filter, it will mess up the
	 * scrolling in the view. This should be only used when processing data that
	 * is already in the view
	 */
	private boolean showInView = true;

	/**
	 * This trace is read from filter file.
	 */
	private boolean readFromFilterFile;

	/**
	 * Gets scrolled trace status
	 * 
	 * @return true if this trace is a scrolled trace
	 */
	public boolean isScrolledTrace() {
		return scrolledTrace;
	}

	/**
	 * Sets scrolled trace status
	 * 
	 * @param scrolledTrace
	 */
	public void setScrolledTrace(boolean scrolledTrace) {
		this.scrolledTrace = scrolledTrace;
	}

	/**
	 * Gets filtered status
	 * 
	 * @return filtering status
	 */
	public boolean isFilteredOut() {
		return filteredOut;
	}

	/**
	 * Sets filtered status
	 * 
	 * @param filtered
	 *            filtering status
	 */
	public void setFilteredOut(boolean filtered) {
		this.filteredOut = filtered;
	}

	/**
	 * Gets triggered out status
	 * 
	 * @return triggered out status
	 */
	public boolean isTriggeredOut() {
		return triggeredOut;
	}

	/**
	 * Sets triggered status
	 * 
	 * @param triggered
	 *            triggering status
	 */
	public void setTriggeredOut(boolean triggered) {
		this.triggeredOut = triggered;
	}

	/**
	 * Gets readFromFilterFile status
	 * 
	 * @return readFromFilterFile status
	 */
	public boolean isReadFromFilterFile() {
		return readFromFilterFile;
	}

	/**
	 * Sets readFromFilterFile status
	 * 
	 * @param readFromFilterFile
	 *            status
	 */
	public void setReadFromFilterFile(boolean readFromFilterFile) {
		this.readFromFilterFile = readFromFilterFile;
	}

	/**
	 * Gets showInView status
	 * 
	 * @return the showInView
	 */
	public boolean isShowInView() {
		return showInView;
	}

	/**
	 * Sets showInView status
	 * 
	 * @param showInView
	 *            the showInView to set
	 */
	public void setShowInView(boolean showInView) {
		this.showInView = showInView;
	}

}
