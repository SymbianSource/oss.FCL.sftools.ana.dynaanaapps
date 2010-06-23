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
 * Label provider for the events view
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.trace.eventrouter.TraceEvent;

/**
 * Label provider for the events view
 * 
 */
final class EventListLabelProvider implements ITableLabelProvider {

	/**
	 * Error icon
	 */
	private Image errorImage;

	/**
	 * Warning icon
	 */
	private Image warningImage;

	/**
	 * Info icon
	 */
	private Image infoImage;

	/**
	 * Constructor
	 */
	EventListLabelProvider() {

		// Get the images from the shared images structure
		errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_ERROR_TSK);
		warningImage = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_WARN_TSK);
		infoImage = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_INFO_TSK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		EventListEntry entry = (EventListEntry) element;
		Image retval = null;
		if (columnIndex == 0) {
			if (entry.getType() == TraceEvent.ERROR) {
				retval = errorImage;
			} else if (entry.getType() == TraceEvent.WARNING) {
				retval = warningImage;
			} else {
				retval = infoImage;
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		String retval;
		EventListEntry entry = (EventListEntry) element;
		if (columnIndex == TraceEventView.TYPE_COLUM_INDEX) {
			if (entry.getType() == TraceEvent.ERROR) {
				retval = Messages
						.getString("EventListLabelProvider.ErrorEventType"); //$NON-NLS-1$
			} else if (entry.getType() == TraceEvent.WARNING) {
				retval = Messages
						.getString("EventListLabelProvider.WarningEventType"); //$NON-NLS-1$
			} else if (entry.getType() == TraceEvent.ASSERT_CRITICAL
					|| entry.getType() == TraceEvent.ASSERT_NORMAL) {
				retval = Messages
						.getString("EventListLabelProvider.AssertionEventType"); //$NON-NLS-1$
			} else {
				retval = Messages
						.getString("EventListLabelProvider.InfoEventType"); //$NON-NLS-1$
			}
		} else if (columnIndex == TraceEventView.CATEGORY_COLUM_INDEX) {
			retval = entry.getCategory();
			if (retval == null) {
				retval = ""; //$NON-NLS-1$
			}
		} else if (columnIndex == TraceEventView.SOURCE_COLUM_INDEX) {
			if (entry.hasSource()) {
				retval = entry.getSourceName();
			} else {
				retval = ""; //$NON-NLS-1$
			}
		} else if (columnIndex == TraceEventView.DESCRIPTION_COLUM_INDEX) {
			retval = entry.getDescription();
			if (retval == null) {
				retval = ""; //$NON-NLS-1$
			}
		} else {
			retval = ""; //$NON-NLS-1$
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#
	 * addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// Shared images must not be disposed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang
	 * .Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#
	 * removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}