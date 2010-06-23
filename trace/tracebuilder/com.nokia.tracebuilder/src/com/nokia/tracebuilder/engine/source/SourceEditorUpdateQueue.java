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
* Update queue for a source editor
*
*/
package com.nokia.tracebuilder.engine.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;

/**
 * Update queue for a source editor
 * 
 */
final class SourceEditorUpdateQueue implements Runnable {

	/**
	 * Comparator for the update queue
	 */
	private Comparator<SourceEditorUpdater> updateComparator = new Comparator<SourceEditorUpdater>() {

		/**
		 * Compares the two source editors
		 * 
		 * @param o1
		 *            editor 1
		 * @param o2
		 *            editor 2
		 * @return result
		 */
		public int compare(SourceEditorUpdater o1, SourceEditorUpdater o2) {
			int i1 = o1.getPosition().getOffset();
			int i2 = o2.getPosition().getOffset();
			return (i1 > i2) ? -1 : ((i1 == i2) ? 0 : 1);
		}

	};

	/**
	 * List of update operations to be run
	 */
	private ArrayList<SourceEditorUpdater> updateQueue;

	/**
	 * Queued updates flag
	 */
	private boolean hasQueuedUpdates;

	/**
	 * Source
	 */
	private SourceProperties properties;

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            the source
	 */
	SourceEditorUpdateQueue(SourceProperties properties) {
		this.properties = properties;
		updateQueue = new ArrayList<SourceEditorUpdater>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Updates are copied, since an update may affect the queue
		SourceEditorUpdater[] updaters = new SourceEditorUpdater[updateQueue
				.size()];
		updateQueue.toArray(updaters);
		// Removes the operations that are run from the queue
		resetUpdateQueue();
		try {
			for (int i = 0; i < updaters.length; i++) {
				if (i == updaters.length - 1) {
					hasQueuedUpdates = false;
				} else {
					hasQueuedUpdates = true;
				}
				boolean updated = updaters[i].runUpdate();
				if (!updated) {
					// Generates a dummy update, otherwise the source will be
					// left in invalid state
					properties.getSourceEditor().updateSource(0, 0, ""); //$NON-NLS-1$
				}
			}
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Source update failure", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Queues an asynchronous operation
	 * 
	 * @param updater
	 *            the operation
	 */
	void queueUpdate(SourceEditorUpdater updater) {
		if (updateQueue.isEmpty()) {
			updateQueue.add(updater);
			TraceBuilderGlobals.runAsyncOperation(this);
		} else {
			int index = Collections.binarySearch(updateQueue, updater,
					updateComparator);
			if (index >= 0) {
				boolean duplicate = false;
				// Checks backwards for duplicates
				for (int i = index; i >= 0; i--) {
					SourceEditorUpdater queuedUpdater = updateQueue.get(i);
					if (queuedUpdater.getPosition().getOffset() == updater
							.getPosition().getOffset()) {
						if (updater.getPosition() == queuedUpdater
								.getPosition()) {
							duplicate = true;
							i = -1;
						}
					}
				}
				// Also checks forwards for duplicates
				for (int i = index; i < updateQueue.size(); i++) {
					SourceEditorUpdater queuedUpdater = updateQueue.get(i);
					if (queuedUpdater.getPosition().getOffset() == updater
							.getPosition().getOffset()) {
						if (updater.getPosition() == queuedUpdater
								.getPosition()) {
							duplicate = true;
							i = updateQueue.size();
						} else {
							// New entry is added after others with same offset
							index++;
						}
					} else {
						i = updateQueue.size();
					}
				}
				if (!duplicate) {
					updateQueue.add(index, updater);
				}
			} else {
				// Adds the update to correct position in the queue
				// The updates are run starting from the end of file so they
				// will not interfere each other
				updateQueue.add(-1 - index, updater);
			}
		}
	}

	/**
	 * Resets the queue when all processing has been done
	 */
	void resetUpdateQueue() {
		updateQueue.clear();
	}

	/**
	 * Flag which determines if there are updates in the queue
	 * 
	 * @return true if queued, false if not
	 */
	boolean hasQueuedUpdates() {
		return hasQueuedUpdates;
	}

}