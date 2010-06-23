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
 * TraceViewer event listener
 *
 */
package com.nokia.traceviewer.engine;

import org.eclipse.ui.PlatformUI;

import com.nokia.trace.eventrouter.PropertySource;
import com.nokia.trace.eventrouter.TraceEvent;
import com.nokia.trace.eventrouter.TraceEventListener;
import com.nokia.trace.eventrouter.TraceEventRouter;
import com.nokia.traceviewer.TraceViewerPlugin;

/**
 * TraceViewer event listener
 * 
 */
final class TraceViewerEventListener implements TraceEventListener {

	/**
	 * Component ID string
	 */
	private static final String CID_STR = "cid"; //$NON-NLS-1$

	/**
	 * Group ID string
	 */
	private static final String GID_STR = "gid"; //$NON-NLS-1$

	/**
	 * Trace ID string
	 */
	private static final String TID_STR = "tid"; //$NON-NLS-1$

	/**
	 * Search trace action string
	 */
	private static final String SEARCHTRACEACTION = "searchtrace"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.trace.eventrouter.TraceEventListener#processEvent(com.nokia
	 * .trace.eventrouter.TraceEvent)
	 */
	public void processEvent(TraceEvent event) {
		if (event.getSource() instanceof PropertySource) {
			final PropertySource source = (PropertySource) event.getSource();

			// This Event is meant for this plugin if the target is the
			// ID of this Plugin
			if (source.getTargetId().equals(TraceViewerPlugin.PLUGIN_ID)) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see java.lang.Runnable#run()
							 */
							public void run() {
								handleEvent(source);
							}
						});
			}
		}
	}

	/**
	 * Handles the event
	 * 
	 * @param source
	 *            propertysource of the event to handle
	 */
	private void handleEvent(PropertySource source) {
		String action = source.getActionName();
		if (action.equals(SEARCHTRACEACTION)) {
			handleSearchTraceAction(source);
		} else {
			// Unknown event action, do nothing
		}
	}

	/**
	 * Handles search trace action
	 * 
	 * @param source
	 *            propertysource of the event to handle
	 */
	private void handleSearchTraceAction(PropertySource source) {

		// View must exist to use the search functionality
		if (TraceViewerGlobals.getTraceViewer().getView() != null) {
			String cidStr = source.getProperties().get(CID_STR);
			String gidStr = source.getProperties().get(GID_STR);
			String tidStr = source.getProperties().get(TID_STR);

			try {

				int cid = Integer.parseInt(cidStr);

				// Get the group ID in a separate method
				int gid = getGroupID(cid, gidStr);

				int tid = Integer.parseInt(tidStr);

				// Create the search if ID's are valid
				if (gid != -1 && tid != -1) {
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getSearchProcessor()
							.searchTraceWithID(cid, gid, tid);
				}

			} catch (NumberFormatException e) {
				// If numbers parsing failed, show a error message
				String errMsg1 = Messages
						.getString("TraceViewerEventListener.InvalidSearchTraceMsg1"); //$NON-NLS-1$
				String errMsg2 = Messages
						.getString("TraceViewerEventListener.InvalidSearchTraceMsg2"); //$NON-NLS-1$
				TraceViewerGlobals.getTraceViewer().getDialogs()
						.showErrorMessage(
								errMsg1 + source.getSourceId() + errMsg2);
			}
		}

	}

	/**
	 * Gets group ID from the group string
	 * 
	 * @param componentId
	 *            component ID
	 * @param gidStr
	 *            group ID string
	 * @return group ID or -1 if parsing failed
	 */
	private int getGroupID(int componentId, String gidStr) {
		int groupId = -1;
		// First try to parse integer
		try {
			groupId = Integer.parseInt(gidStr);

			// If parsing failed, string contains the group name. Go ask the ID
			// from the DecodeProvider
		} catch (NumberFormatException e) {
			if (TraceViewerGlobals.getDecodeProvider() != null) {
				groupId = TraceViewerGlobals.getDecodeProvider().getGroupId(
						componentId, gidStr);
			}
		}
		return groupId;
	}

	/**
	 * Registers itself to the event router
	 */
	public void register() {
		TraceEventRouter.getInstance().addEventListener(this);
	}

	/**
	 * Unregisters itself from the event router
	 */
	public void unregister() {
		TraceEventRouter.getInstance().removeEventListener(this);
	}
}
