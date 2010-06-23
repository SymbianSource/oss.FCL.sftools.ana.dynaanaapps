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
* Selection listener for tree viewer
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.nokia.tracebuilder.engine.LastKnownLocation;
import com.nokia.tracebuilder.engine.TraceBuilderActions;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceLocationList;
import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Selection listener for tree viewer. Updates the active object of TraceBuilder
 * 
 */
final class TraceViewSelectionListener implements ISelectionChangedListener,
		IDoubleClickListener {

	/**
	 * TraceBuilder actions interface
	 */
	private TraceBuilderActions actions;

	/**
	 * Constructor
	 * 
	 * @param actions
	 *            TraceBuilder actions interface
	 */
	TraceViewSelectionListener(TraceBuilderActions actions) {
		this.actions = actions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#
	 *      selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		selectionChanged(selection, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#
	 *      doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		selectionChanged(selection, true);
	}

	/**
	 * Selection change processor
	 * 
	 * @param selection
	 *            the new selection
	 * @param doubleClick
	 *            true if selection was made by double-click
	 */
	private void selectionChanged(ISelection selection, boolean doubleClick) {
		TraceObject object = null;
		TraceLocation location = null;
		TraceLocationList locationList = null;
		LastKnownLocation LastKnownLocation = null;
		TraceViewExtension extension = null;
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			boolean rootFound = false;
			boolean elementFound;
			do {
				elementFound = false;
				// Selects the closest TraceObject parent of the selected
				// element
				if (element instanceof TraceObjectWrapper) {
					// Activates the trace object.
					object = ((TraceObjectWrapper) element).getTraceObject();
					elementFound = true;
				} else if (element instanceof TraceLocationWrapper) {
					TraceLocationWrapper locationWrapper = (TraceLocationWrapper) element;
					TraceLocationListWrapper listWrapper = (TraceLocationListWrapper) locationWrapper
							.getParent();
					// Activates the location
					location = locationWrapper.getLocation();
					if (location != null) {
						locationList = listWrapper.getLocationList();
					}
					elementFound = true;
				} else if (element instanceof TraceLocationListWrapper) {
					locationList = ((TraceLocationListWrapper) element)
							.getLocationList();
					elementFound = true;
				} else if (element instanceof LastKnownLocationWrapper) {
					LastKnownLocationWrapper locationWrapper = (LastKnownLocationWrapper) element;
					LastKnownLocation = locationWrapper.getLocation();
					elementFound = true;
				} else if (element instanceof TraceViewExtensionWrapper) {
					TraceViewExtensionWrapper wrapper = (TraceViewExtensionWrapper) element;
					extension = wrapper.getExtension();
					object = ((TraceObjectWrapper) wrapper.getParent())
							.getTraceObject();
					elementFound = true;
				} else if (element instanceof ListNavigator) {
					extension = ((ListNavigator) element).getActionProvider();
					elementFound = true;
				} else if (element instanceof WrapperBase) {
					// If selection was not supported, the parent is tried
					element = ((WrapperBase) element).getParent();
				} else {
					rootFound = true;
				}
				// If the wrapper element was found but object does not exist,
				// the object has been deleted, but view is not yet updated
				// -> Parent is selected
				if (elementFound && object == null && locationList == null
						&& location == null && extension == null
						&& LastKnownLocation == null) {
					element = ((WrapperBase) element).getParent();
				}
			} while (object == null && locationList == null && location == null
					&& extension == null && LastKnownLocation == null
					&& !rootFound);
		}
		notifySelection(object, location, locationList, LastKnownLocation,
				extension, doubleClick);
	}

	/**
	 * Notification about selected object
	 * 
	 * @param object
	 *            trace object
	 * @param location
	 *            trace location
	 * @param locationList
	 *            trace location list
	 * @param LastKnownLocation
	 *            last known location
	 * @param extension
	 *            extension
	 * @param doubleClick
	 *            double click flag
	 */
	private void notifySelection(TraceObject object, TraceLocation location,
			TraceLocationList locationList,
			LastKnownLocation LastKnownLocation,
			TraceViewExtension extension, boolean doubleClick) {
		if (extension != null) {
			// Enables the actions based on the selected extension, but passes
			// the TraceObject to TraceBuilder
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(object,
					false, false);
			actions.enableActions(extension);
		} else if (object != null) {
			// Selection and actions are based on the TraceObject
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(object,
					false, false);
			actions.enableActions(object);
		} else if (location != null) {
			// Selection and actions are based on the TraceLocation
			TraceBuilderGlobals.getTraceBuilder().locationSelected(
					locationList, location, false);
			actions.enableActions(location);
		} else if (locationList != null) {
			// Selection and actions are based on the TraceLocationList
			TraceBuilderGlobals.getTraceBuilder().locationSelected(
					locationList, null, false);
			actions.enableActions(locationList);
		} else if (LastKnownLocation != null) {
			TraceBuilderGlobals.getTraceBuilder().traceObjectSelected(
					LastKnownLocation.getTrace(), false, false);
			actions.enableActions(LastKnownLocation);
		}
	}

}