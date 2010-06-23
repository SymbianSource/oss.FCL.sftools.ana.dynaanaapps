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
* Wrapper for JFace IDocument
*
*/
package com.nokia.tracebuilder.eclipse;

import java.io.File;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceErrorCodes;
import com.nokia.tracebuilder.source.SourceLocationBase;
import com.nokia.tracebuilder.source.SourceLocationInterface;
import com.nokia.tracebuilder.source.SourceParserException;
import com.nokia.tracebuilder.source.SourcePropertyProvider;
import com.nokia.tracebuilder.source.SourceSelector;

/**
 * Wrapper for JFace IDocument and Eclipse ITextEditor
 * 
 */
final class JFaceDocumentWrapper implements SourceDocumentInterface,
		SourceSelector, SourcePropertyProvider {

	/**
	 * Document position category
	 */
	public static final String TRACEBUILDER_POSITION_CATEGORY = "TraceBuilder.positions"; //$NON-NLS-1$

	/**
	 * Document
	 */
	private IDocument document;

	/**
	 * Text editor for the document, may be null
	 */
	private ITextEditor textEditor;

	/**
	 * Selection listener for the editor
	 */
	private JFaceSelectionListener selectionListener;

	/**
	 * Change listener for the editor
	 */
	private JFaceSourceChangeListener changeListener;

	/**
	 * Save listener
	 */
	private JFaceDirtyStateListener saveListener;

	/**
	 * Location updater
	 */
	private JFaceLocationUpdater locationUpdater;

	/**
	 * Owner of this source
	 */
	private Object owner;

	/**
	 * Constructor
	 * 
	 * @param document
	 *            the document
	 */
	JFaceDocumentWrapper(IDocument document) {
		this.document = document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#get(int, int)
	 */
	public String get(int start, int length) throws SourceParserException {
		try {
			return document.get(start, length);
		} catch (BadLocationException e) {
			throw new SourceParserException(SourceErrorCodes.BAD_LOCATION);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getChar(int)
	 */
	public char getChar(int offset) throws SourceParserException {
		try {
			return document.getChar(offset);
		} catch (BadLocationException e) {
			throw new SourceParserException(SourceErrorCodes.BAD_LOCATION);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getLength()
	 */
	public int getLength() {
		return document.getLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getLineOfOffset(int)
	 */
	public int getLineOfOffset(int offset) throws SourceParserException {
		try {
			return document.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			throw new SourceParserException(SourceErrorCodes.BAD_LOCATION);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#replace(int,
	 *      int, java.lang.String)
	 */
	public void replace(int offset, int length, String newText)
			throws SourceParserException {
		try {
			document.replace(offset, length, newText);
		} catch (BadLocationException e) {
			throw new SourceParserException(SourceErrorCodes.BAD_LOCATION);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getPropertyProvider()
	 */
	public SourcePropertyProvider getPropertyProvider() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getSourceSelector()
	 */
	public SourceSelector getSourceSelector() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceSelector#setSelection(int, int)
	 */
	public void setSelection(int offset, int length) {
		if (textEditor != null) {
			textEditor.getEditorSite().getPage().bringToTop(textEditor);
			textEditor.selectAndReveal(offset, length);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourcePropertyProvider#getFileName()
	 */
	public String getFileName() {
		String retval = null;
		if (textEditor != null) {
			IEditorInput input = textEditor.getEditorInput();
			String path = WorkbenchUtils.getEditorInputPath(input);
			if (path != null) {
				retval = new File(path).getName();
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourcePropertyProvider#getFilePath()
	 */
	public String getFilePath() {
		String retval = null;
		if (textEditor != null) {
			IEditorInput input = textEditor.getEditorInput();
			String path = WorkbenchUtils.getEditorInputPath(input);
			if (path != null) {
				retval = new File(path).getParent();
			}
		}
		if (retval != null && !retval.endsWith(File.separator)) {
			retval += File.separator;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      addLocation(com.nokia.tracebuilder.source.SourceLocationInterface)
	 */
	public void addLocation(SourceLocationInterface location) {
		String category = getLocationCategory(((JFaceLocationWrapper) location)
				.getLocation());
		try {
			if (category == null) {
				document.addPosition((JFaceLocationWrapper) location);
			} else {
				document.addPosition(category, (JFaceLocationWrapper) location);
			}
		} catch (Exception e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postCriticalAssertionFailed(
						"Failed to add location", e); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#
	 *      removeLocation(com.nokia.tracebuilder.source.SourceLocationInterface)
	 */
	public void removeLocation(SourceLocationInterface location) {
		String category = getLocationCategory(((JFaceLocationWrapper) location)
				.getLocation());
		if (category == null) {
			document.removePosition((JFaceLocationWrapper) location);
		} else {
			try {
				document.removePosition(category,
						(JFaceLocationWrapper) location);
			} catch (Exception e) {
				if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
					TraceBuilderGlobals.getEvents()
							.postCriticalAssertionFailed(
									"Failed to remove location", e); //$NON-NLS-1$
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#getOwner()
	 */
	public Object getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentInterface#setOwner(java.lang.Object)
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	/**
	 * Called by the workbench editor monitor to initialize listeners
	 * 
	 * @param monitor
	 *            the workbench monitor
	 */
	void sourceOpened(WorkbenchEditorMonitor monitor) {
		if (monitor != null) {
			locationUpdater = new JFaceLocationUpdater(
					TRACEBUILDER_POSITION_CATEGORY);
			document.addPositionCategory(TRACEBUILDER_POSITION_CATEGORY);
			document.addPositionUpdater(locationUpdater);
			if (changeListener == null) {
				changeListener = new JFaceSourceChangeListener(monitor, this);
				document.addDocumentListener(changeListener);
			}
			if (textEditor != null && selectionListener == null) {
				selectionListener = new JFaceSelectionListener(monitor, this);
				textEditor.getSite().getPage().addPostSelectionListener(
						selectionListener);
				saveListener = new JFaceDirtyStateListener(monitor, this);
				textEditor.addPropertyListener(saveListener);
			}
		}
	}

	/**
	 * Performs cleanup. Called by WorkbenchEditorMonitor when source is removed
	 */
	void sourceClosed() {
		if (textEditor != null) {
			if (selectionListener != null) {
				textEditor.getSite().getPage().removePostSelectionListener(
						selectionListener);
			}
			if (saveListener != null) {
				textEditor.removePropertyListener(saveListener);
			}
		}
		if (changeListener != null) {
			document.removeDocumentListener(changeListener);
		}
		document.removePositionUpdater(locationUpdater);
		try {
			document.removePositionCategory(TRACEBUILDER_POSITION_CATEGORY);
		} catch (BadPositionCategoryException e) {
			if (TraceBuilderConfiguration.ASSERTIONS_ENABLED) {
				TraceBuilderGlobals.getEvents().postAssertionFailed(
						"Failed to remove position updater", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Gets the document
	 * 
	 * @return the document
	 */
	IDocument getDocument() {
		return document;
	}

	/**
	 * Sets the document
	 * 
	 * @param document
	 *            the new document
	 */
	void setDocument(IDocument document) {
		this.document = document;
	}

	/**
	 * Gets the text editor
	 * 
	 * @return the text editor
	 */
	ITextEditor getTextEditor() {
		return textEditor;
	}

	/**
	 * Sets the text editor
	 * 
	 * @param textEditor
	 *            the new text editor
	 */
	void setTextEditor(ITextEditor textEditor) {
		this.textEditor = textEditor;
	}

	/**
	 * Gets the selection listener
	 * 
	 * @return the selection listener
	 */
	JFaceSelectionListener getSelectionListener() {
		return selectionListener;
	}

	/**
	 * Gets a category for location
	 * 
	 * @param location
	 *            the location
	 * @return the category
	 */
	private String getLocationCategory(SourceLocationBase location) {
		String retval;
		if (location instanceof TraceLocation) {
			retval = TRACEBUILDER_POSITION_CATEGORY;
		} else {
			retval = null;
		}
		return retval;
	}

}
