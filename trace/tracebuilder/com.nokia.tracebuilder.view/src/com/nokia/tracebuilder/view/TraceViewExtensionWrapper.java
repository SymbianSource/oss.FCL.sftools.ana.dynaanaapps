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
* Wrapper for a trace view extension
*
*/
package com.nokia.tracebuilder.view;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener;

/**
 * Wrapper for a trace view extension
 * 
 */
final class TraceViewExtensionWrapper extends ListWrapper implements
		TraceModelExtensionUpdateListener {

	/**
	 * The wrapped extension
	 */
	private TraceViewExtension extension;

	/**
	 * Creates a new extension wrapper.
	 * 
	 * @param extension
	 *            the extension to be shown in the view
	 * @param parent
	 *            parent wrapper
	 * @param updater
	 *            the update notifier
	 */
	TraceViewExtensionWrapper(TraceViewExtension extension, WrapperBase parent,
			WrapperUpdater updater) {
		super(parent, updater);
		this.extension = extension;
		extension.addUpdateListener(this);
		extension.setViewReference(this);
		createChildren();
	}

	/**
	 * Returns the wrapped extension
	 * 
	 * @return the extension
	 */
	TraceViewExtension getExtension() {
		return extension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.view.ListWrapper#delete()
	 */
	@Override
	void delete() {
		extension.setViewReference(null);
		extension.removeUpdateListener(this);
		extension = null;
		super.delete();
	}

	/**
	 * Creates the child wrappers
	 */
	private void createChildren() {
		Iterator<?> itr = extension.getChildren();
		while (itr.hasNext()) {
			ObjectWrapper wrapper = new ObjectWrapper(itr.next(), this,
					getUpdater());
			add(wrapper);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener#extensionUpdated()
	 */
	public void extensionUpdated() {
		clear();
		createChildren();
		getUpdater().update(
				((TraceObjectWrapper) getParent()).updateExtension(extension));
	}

}
