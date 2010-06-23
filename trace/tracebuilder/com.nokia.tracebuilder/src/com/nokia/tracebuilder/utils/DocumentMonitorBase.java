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
* Base class for document monitors
*
*/
package com.nokia.tracebuilder.utils;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.source.OffsetLength;
import com.nokia.tracebuilder.source.SourceDocumentFactory;
import com.nokia.tracebuilder.source.SourceDocumentInterface;
import com.nokia.tracebuilder.source.SourceDocumentMonitor;
import com.nokia.tracebuilder.source.SourceDocumentProcessor;

/**
 * Base class for document monitors.
 * 
 */
public class DocumentMonitorBase implements SourceDocumentMonitor {

	/**
	 * Zero offset, zero length
	 */
	private static final OffsetLength ZERO_OFFSET_LENGTH = new OffsetLength();

	/**
	 * Document factory adapter
	 */
	private DocumentFactoryBase factory;

	/**
	 * Document processor
	 */
	private SourceDocumentProcessor processor;

	/**
	 * List of sources
	 */
	private ArrayList<SourceDocumentInterface> sources = new ArrayList<SourceDocumentInterface>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#getFactory()
	 */
	public SourceDocumentFactory getFactory() {
		if (factory == null) {
			factory = new DocumentFactoryBase();
		}
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#getSelectedSource()
	 */
	public SourceDocumentInterface getSelectedSource() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#
	 *      getSelection(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	public OffsetLength getSelection(SourceDocumentInterface props) {
		return ZERO_OFFSET_LENGTH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<SourceDocumentInterface> iterator() {
		return sources.iterator();
	}

	/**
	 * Adds a source to this monitor and notifies the document processor
	 * 
	 * @param source
	 *            the source to be added
	 */
	protected void addSource(SourceDocumentInterface source) {
		sources.add(source);
		processor.sourceOpened(source);
	}

	/**
	 * Removes a source from this monitor and calls the document processor
	 * 
	 * @param source
	 *            the source to be removed
	 */
	protected void removeSource(SourceDocumentInterface source) {
		sources.remove(source);
		processor.sourceClosed(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#
	 *      isSourceEditable(com.nokia.tracebuilder.source.SourceDocumentInterface)
	 */
	public boolean isSourceEditable(SourceDocumentInterface selectedSource) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#setFocus()
	 */
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#
	 *      startMonitor(com.nokia.tracebuilder.source.SourceDocumentProcessor)
	 */
	public void startMonitor(SourceDocumentProcessor processor) {
		this.processor = processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceDocumentMonitor#stopMonitor()
	 */
	public void stopMonitor() {
		for (SourceDocumentInterface document : sources) {
			processor.sourceClosed(document);
		}
		sources.clear();
		processor = null;
	}

	/**
	 * Gets the source processor
	 * 
	 * @return the processor
	 */
	protected SourceDocumentProcessor getProcessor() {
		return processor;
	}

}