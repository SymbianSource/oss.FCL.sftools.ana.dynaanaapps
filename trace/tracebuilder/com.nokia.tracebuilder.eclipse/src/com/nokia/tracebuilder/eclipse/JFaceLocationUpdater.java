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
* Keeps the TraceLocation objects up-to-date when source document changes
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.jface.text.DefaultPositionUpdater;

import com.nokia.tracebuilder.engine.TraceLocation;

/**
 * Keeps the TraceLocation objects up-to-date when source document changes.
 * 
 */
final class JFaceLocationUpdater extends DefaultPositionUpdater {

	/**
	 * Default constructor
	 * 
	 * @param category
	 *            the position category
	 */
	JFaceLocationUpdater(String category) {
		super(category);
	}

	/**
	 * Called when data is removed from the document. If the trace is modified,
	 * the length of the position is reduced. If the document is modified prior
	 * the trace, the offset of the position is changed.
	 * 
	 * @see org.eclipse.jface.text.DefaultPositionUpdater#adaptToRemove()
	 */
	@Override
	protected void adaptToRemove() {
		super.adaptToRemove();
		if (fOriginalPosition.length > fPosition.length) {
			// If the length changes, the content needs to be re-examined
			// when updating the view
			((TraceLocation) ((JFaceLocationWrapper) fPosition).getLocation())
					.setContentChanged(true);
		}
	}

	/**
	 * Called when data is added to the document. If the trace is modified, the
	 * length of the position is adjusted. If the document is modified prior the
	 * trace, the offset of the position is changed.
	 * 
	 * @see org.eclipse.jface.text.DefaultPositionUpdater#adaptToInsert()
	 */
	@Override
	protected void adaptToInsert() {
		super.adaptToInsert();
		if (fOriginalPosition.length < fPosition.length) {
			// If the length changes, the content needs to be re-examined
			// when updating the view
			((TraceLocation) ((JFaceLocationWrapper) fPosition).getLocation())
					.setContentChanged(true);
		}
	}

	/**
	 * Sets the changed flag if the position is changed.
	 * 
	 * @see org.eclipse.jface.text.DefaultPositionUpdater#adaptToReplace()
	 */
	@Override
	protected void adaptToReplace() {
		if (fPosition.offset == fOffset && fPosition.length == fLength
				&& fPosition.length > 0) {
			// DefaultPositionUpdater does not call adaptToInsert /
			// adaptToRemove if the entire position is replaced. In that case,
			// the content needs to be re-examined
			super.adaptToReplace();
			((TraceLocation) ((JFaceLocationWrapper) fPosition).getLocation())
					.setContentChanged(true);
		} else {
			super.adaptToReplace();
		}
	}
}
