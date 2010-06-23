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
* Background thread that schedules tree view refreshes
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * Background thread that schedules tree view refreshes
 * 
 */
final class TraceViewUpdater extends Thread implements WrapperUpdater {

	/**
	 * View refresh timeout
	 */
	private final static int VIEW_REFRESH_TIMEOUT = 50; // CodForChk_Dis_Magic

	/**
	 * Running flag
	 */
	private boolean running;

	/**
	 * Async refresher timeout
	 */
	private long timeout;

	/**
	 * The view updater process
	 */
	private TraceViewUpdaterRunnable updaterRunnable;

	/**
	 * Tree viewer
	 */
	private TreeViewer viewer;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            the viewer to be refreshed
	 */
	TraceViewUpdater(TreeViewer viewer) {
		this.viewer = viewer;
		updaterRunnable = new TraceViewUpdaterRunnable(viewer);
		timeout = VIEW_REFRESH_TIMEOUT;
		running = true;
		start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (running) {
			try {
				if (running && !updaterRunnable.hasUpdates()) {
					Thread.sleep(timeout);
				}
				if (running && updaterRunnable.hasUpdates()) {
					// Updates one viewer element
					viewer.getControl().getDisplay().syncExec(updaterRunnable);
				}
			} catch (Exception e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperUpdater#
	 *      queueUpdate(com.nokia.tracebuilder.view.WrapperBase)
	 */
	public void queueUpdate(WrapperBase wrapper) {
		updaterRunnable.queueUpdate(wrapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperUpdater#
	 *      queueSelection(com.nokia.tracebuilder.view.WrapperBase)
	 */
	public void queueSelection(WrapperBase wrapper) {
		updaterRunnable.queueSelection(wrapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.WrapperUpdater#
	 *      update(com.nokia.tracebuilder.view.WrapperBase)
	 */
	public void update(WrapperBase wrapper) {
		viewer.refresh(wrapper);
	}

	/**
	 * Stops the thread
	 */
	void stopUpdater() {
		running = false;
	}

	/**
	 * Sets the root wrapper
	 * 
	 * @param modelWrapper
	 *            the root wrapper
	 */
	void setRoot(TraceModelWrapper modelWrapper) {
		updaterRunnable.setRoot(modelWrapper);
	}

}
