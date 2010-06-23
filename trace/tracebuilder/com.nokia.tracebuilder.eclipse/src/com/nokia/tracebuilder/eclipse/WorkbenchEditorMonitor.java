/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Workbench editor monitor
*
*/
package com.nokia.tracebuilder.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.nokia.tracebuilder.source.OffsetLength;
import com.nokia.tracebuilder.source.SourceDocumentFactory;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceDocumentProcessor;
import com.nokia.tracebuilder.utils.DocumentMonitorBase;

/**
 * Workbench editor monitor
 * 
 */
public final class WorkbenchEditorMonitor extends DocumentMonitorBase implements
		WorkbenchListenerCallback {

	/**
	 * JFace document factory
	 */
	private JFaceDocumentFactory documentFactory;

	/**
	 * Workbench listener
	 */
	private WorkbenchListener listener = new WorkbenchListener(this);

	/**
	 * Trace project monitor
	 */
	private TraceProjectMonitor projectMonitor;

	/**
	 * Constructor
	 * 
	 * @param projectMonitor
	 *            the project monitor
	 */
	WorkbenchEditorMonitor(TraceProjectMonitor projectMonitor) {
		this.projectMonitor = projectMonitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#
	 * startMonitor(com.nokia.tracebuilder.source.SourceDocumentProcessor)
	 */
	@Override
	public void startMonitor(SourceDocumentProcessor processor) {
		super.startMonitor(processor);
		listener.startListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#stopMonitor()
	 */
	@Override
	public void stopMonitor() {
		listener.stopListener();
		super.stopMonitor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#getFactory()
	 */
	@Override
	public SourceDocumentFactory getFactory() {
		if (documentFactory == null) {
			documentFactory = new JFaceDocumentFactory();
		}
		return documentFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#
	 * isSourceEditable(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	@Override
	public boolean isSourceEditable(SourceDocumentInterface source) {
		ITextEditor editor = ((JFaceDocumentWrapper) source).getTextEditor();
		return editor != null && editor.isEditable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#setFocus()
	 */
	@Override
	public void setFocus() {
		IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			editor.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#getSelectedSource()
	 */
	@Override
	public SourceDocumentInterface getSelectedSource() {
		SourceDocumentInterface retval = null;
		IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			for (SourceDocumentInterface source : this) {
				JFaceDocumentWrapper props = (JFaceDocumentWrapper) source;
				if (props.getTextEditor() == editor) {
					retval = props;
					break;
				}
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.utils.DocumentMonitorBase#
	 * getSelection(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	@Override
	public OffsetLength getSelection(SourceDocumentInterface document) {
		ITextSelection selection = (ITextSelection) ((JFaceDocumentWrapper) document)
				.getTextEditor().getSelectionProvider().getSelection();
		OffsetLength retval;
		if (selection != null) {
			retval = new OffsetLength();
			retval.offset = selection.getOffset();
			retval.length = selection.getLength();
		} else {
			retval = null;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorOpened(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorOpened(ITextEditor editor, IFile file) {
		if (projectMonitor.isFileActive(file)) {
			IDocumentProvider provider = editor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			IDocument document = provider.getDocument(input);
			documentOpened(document, editor, input);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorActivated(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorActivated(ITextEditor editor, IFile file) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		documentActivated(document);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorReplaced(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorReplaced(ITextEditor editor, IFile file) {
		if (projectMonitor.isFileActive(file)) {
			IDocumentProvider provider = editor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			IDocument document = provider.getDocument(editor.getEditorInput());
			documentReplaced(input, document);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorHidden(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorHidden(ITextEditor editor, IFile file) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorVisible(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorVisible(ITextEditor editor, IFile file) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.eclipse.WorkbenchListenerCallback#
	 * editorClosed(org.eclipse.ui.texteditor.ITextEditor,
	 * org.eclipse.core.resources.IFile)
	 */
	public void editorClosed(ITextEditor editor, IFile file) {
		if (editor != null) {
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null) {
				IDocument document = provider.getDocument(editor
						.getEditorInput());
				if (document != null) {
					documentClosed(document);
				}
			}
		}
	}

	/**
	 * Processes a document that has been opened. This locates the trace entries
	 * from the document and creates corresponding TraceLocation objects. The
	 * trace locations are updated by a position updater object when the
	 * document changes.
	 * 
	 * @param document
	 *            the document that was opened
	 * @param editor
	 *            the text editor associated to the document
	 * @param input
	 *            editor input for the document
	 */
	private void documentOpened(IDocument document, ITextEditor editor,
			IEditorInput input) {
		JFaceDocumentWrapper foundSource = null;
		for (SourceDocumentInterface source : this) {
			if (((JFaceDocumentWrapper) source).getTextEditor()
					.getEditorInput().equals(input)) {
				foundSource = (JFaceDocumentWrapper) source;
				break;
			}
		}
		if (foundSource == null) {
			foundSource = new JFaceDocumentWrapper(document);
			foundSource.setTextEditor(editor);
			foundSource.sourceOpened(this);
			addSource(foundSource);
		}
	}

	/**
	 * Document activated event
	 * 
	 * @param document
	 *            the document
	 */
	private void documentActivated(IDocument document) {
		for (SourceDocumentInterface source : this) {
			if (((JFaceDocumentWrapper) source).getDocument() == document) {
				((JFaceDocumentWrapper) source).getSelectionListener()
						.disableNextSelectionEvent();
				break;
			}
		}
	}

	/**
	 * Document replaced event
	 * 
	 * @param input
	 *            the editor input
	 * @param document
	 *            the new document
	 */
	private void documentReplaced(IEditorInput input, IDocument document) {
		JFaceDocumentWrapper foundSource = null;
		for (SourceDocumentInterface source : this) {
			if (((JFaceDocumentWrapper) source).getTextEditor()
					.getEditorInput().equals(input)) {
				foundSource = (JFaceDocumentWrapper) source;
				break;
			}
		}
		if (foundSource != null) {
			documentClosed(foundSource.getDocument());
			documentOpened(document, foundSource.getTextEditor(), input);
		}
	}

	/**
	 * Processes a document close notification. This removes the source from the
	 * sources list and calls sourceClosed
	 * 
	 * @param document
	 *            the document that was closed
	 */
	private void documentClosed(IDocument document) {
		for (SourceDocumentInterface source : this) {
			if (((JFaceDocumentWrapper) source).getDocument() == document) {
				removeSource(source);
				((JFaceDocumentWrapper) source).sourceClosed();
				break;
			}
		}
	}

	/**
	 * Selection change notification
	 * 
	 * @param source
	 *            the source
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 */
	void selectionChanged(JFaceDocumentWrapper source, int offset, int length) {
		getProcessor().selectionChanged(source, offset, length);
	}

	/**
	 * Notification before source is changed
	 * 
	 * @param source
	 *            the source
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @param newText
	 *            the new text
	 */
	void sourceAboutToBeChanged(SourceDocumentInterface source, int offset,
			int length, String newText) {
		getProcessor().sourceAboutToBeChanged(source, offset, length, newText);
	}

	/**
	 * Source change notification
	 * 
	 * @param source
	 *            the source
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @param newText
	 *            the new text
	 */
	void sourceChanged(SourceDocumentInterface source, int offset, int length,
			String newText) {
		getProcessor().sourceChanged(source, offset, length, newText);
	}

	/**
	 * Source save notification
	 * 
	 * @param source
	 *            the source
	 */
	void sourceSaved(JFaceDocumentWrapper source) {
		getProcessor().sourceSaved(source);

		projectMonitor.sourceSaved(source);
	}

}
