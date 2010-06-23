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
 * Action to remove all entries from the list
 *
 */
package com.nokia.trace.eventview;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove all entries from the list
 * 
 */
final class RemoveAllEventsAction extends Action {

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
	RemoveAllEventsAction(EventListContentProvider provider) {
		this.contentProvider = provider;
		setText(Messages.getString("RemoveEventAction.DismissAllEventsAction")); //$NON-NLS-1$
		setToolTipText(Messages
				.getString("RemoveEventAction.DismissAllEventsActionTooltip")); //$NON-NLS-1$
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
		contentProvider.removeAllEntries();
	}
}