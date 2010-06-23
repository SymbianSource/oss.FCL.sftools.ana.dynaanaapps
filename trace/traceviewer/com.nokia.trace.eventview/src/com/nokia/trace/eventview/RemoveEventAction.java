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
 * Action to remove an entry from the list
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove an entry from the list
 * 
 */
final class RemoveEventAction extends Action {

	/**
	 * The selected entry
	 */
	private EventListEntry entry;

	/**
	 * Content provider
	 */
	private EventListContentProvider contentProvider;

	/**
	 * Constructor
	 * 
	 * @param provider
	 *            content provider for entry removal
	 */
	RemoveEventAction(EventListContentProvider provider) {
		this.contentProvider = provider;
		setText(Messages.getString("RemoveEventAction.DismissEventAction")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("RemoveEventAction.DismissEventActionTooltip")); //$NON-NLS-1$
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		if (entry != null) {
			contentProvider.removeEntry(entry);
			entry = null;
		}
	}

	/**
	 * Sets the entry to be removed
	 * 
	 * @param entry
	 *            the entry
	 */
	void setEntry(EventListEntry entry) {
		this.entry = entry;
	}
}