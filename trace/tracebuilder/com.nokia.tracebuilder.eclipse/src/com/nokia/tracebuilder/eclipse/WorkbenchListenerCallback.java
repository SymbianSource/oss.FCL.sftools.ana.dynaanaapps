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
* Callback for workbench listener
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Callback for workbench listener
 * 
 */
interface WorkbenchListenerCallback {

	/**
	 * Editor opened notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the editor resource
	 */
	void editorOpened(ITextEditor editor, IFile file);

	/**
	 * Editor activated notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the editor resource
	 */
	void editorActivated(ITextEditor editor, IFile file);

	/**
	 * Editor content replaced notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the new editor resource
	 */
	void editorReplaced(ITextEditor editor, IFile file);

	/**
	 * Editor closed notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the editor resource
	 */
	void editorClosed(ITextEditor editor, IFile file);

	/**
	 * Editor visible notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the editor resource
	 */
	void editorVisible(ITextEditor editor, IFile file);

	/**
	 * Editor hidden notification
	 * 
	 * @param editor
	 *            the editor
	 * @param file
	 *            the editor resource
	 */
	void editorHidden(ITextEditor editor, IFile file);

}
