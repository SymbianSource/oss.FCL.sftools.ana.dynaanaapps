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
 * Dictionary Content Variables
 *
 */
package com.nokia.trace.dictionary.model;

import com.nokia.trace.dictionary.model.decodeparameters.CompoundParameter;
import com.nokia.trace.dictionary.model.decodeparameters.EnumParameter;

/**
 * Dictionary Content Variables keeps track on all important variables that are
 * needed when creating model
 * 
 */
public class DictionaryContentVariables {

	/**
	 * Enumeration telling what is the parent decode object of the current
	 * object
	 */
	public enum ParentDecodeObject {

		/**
		 * Nothing
		 */
		NOTHING,

		/**
		 * Component
		 */
		COMPONENT,

		/**
		 * Group
		 */
		GROUP,

		/**
		 * Trace
		 */
		TRACE,

		/**
		 * Trace instance
		 */
		TRACEINSTANCE
	}

	/**
	 * Previous component element
	 */
	private TraceComponent previousComponent;

	/**
	 * Previous group element
	 */
	private TraceGroup previousGroup;

	/**
	 * Previous trace
	 */
	private Trace previousTrace;

	/**
	 * Previous trace data-ref element
	 */
	private int previousTraceDataRef;

	/**
	 * Previous trace name
	 */
	private String previousTraceName;

	/**
	 * Previous enum parameter
	 */
	private EnumParameter previousEnumParameter;

	/**
	 * Previous compound parameter
	 */
	private CompoundParameter previousCompoundParameter;

	/**
	 * Previous path element
	 */
	private Path previousPath;

	/**
	 * Indicates are we inside a metadata block
	 */
	private boolean insideMetadataBlock;

	/**
	 * List containing trace instances and metadata from previous trace tag
	 */
	private TraceInstanceList traceInstanceList;

	/**
	 * Parent decode object
	 */
	private ParentDecodeObject parentDecodeObject = ParentDecodeObject.NOTHING;

	/**
	 * Constructor
	 */
	public DictionaryContentVariables() {

	}

	/**
	 * Gets previous path
	 * 
	 * @return the previousPath
	 */
	public Path getPreviousPath() {
		return previousPath;
	}

	/**
	 * Sets previous path
	 * 
	 * @param previousPath
	 *            the previousPath to set
	 */
	public void setPreviousPath(Path previousPath) {
		this.previousPath = previousPath;
	}

	/**
	 * Gets previous component
	 * 
	 * @return the previousComponent
	 */
	public TraceComponent getPreviousComponent() {
		return previousComponent;
	}

	/**
	 * Sets previous component
	 * 
	 * @param previousComponent
	 *            the previousComponent to set
	 */
	public void setPreviousComponent(TraceComponent previousComponent) {
		this.previousComponent = previousComponent;
	}

	/**
	 * Gets previous group
	 * 
	 * @return the previousGroup
	 */
	public TraceGroup getPreviousGroup() {
		return previousGroup;
	}

	/**
	 * Sets previous group
	 * 
	 * @param previousGroup
	 *            the previousGroup to set
	 */
	public void setPreviousGroup(TraceGroup previousGroup) {
		this.previousGroup = previousGroup;
	}

	/**
	 * Gets previous trace
	 * 
	 * @return the previous trace
	 */
	public Trace getPreviousTrace() {
		return previousTrace;
	}

	/**
	 * Sets previous trace
	 * 
	 * @param previousTrace
	 *            the previous trace to set
	 */
	public void setPreviousTrace(Trace previousTrace) {
		this.previousTrace = previousTrace;
	}

	/**
	 * Gets the previous trace data reference
	 * 
	 * @return the previous trace data reference
	 */
	public int getPreviousTraceDataRef() {
		return previousTraceDataRef;
	}

	/**
	 * Sets the previous trace reference
	 * 
	 * @param previousTraceDataRef
	 *            the previousTrace to set
	 */
	public void setPreviousTraceDataRef(int previousTraceDataRef) {
		this.previousTraceDataRef = previousTraceDataRef;
	}

	/**
	 * Gets the previous trace name
	 * 
	 * @return the previous trace name
	 */
	public String getPreviousTraceName() {
		return previousTraceName;
	}

	/**
	 * Sets the previous trace name
	 * 
	 * @param previousTraceName
	 *            the previousTraceName to set
	 */
	public void setPreviousTraceName(String previousTraceName) {
		this.previousTraceName = previousTraceName;
	}

	/**
	 * Gets previous enum parameter
	 * 
	 * @return the previousEnumParameter
	 */
	public EnumParameter getPreviousEnumParameter() {
		return previousEnumParameter;
	}

	/**
	 * Sets previous enum parameter
	 * 
	 * @param previousEnumParameter
	 *            the previousEnumParater to set
	 */
	public void setPreviousEnumParameter(EnumParameter previousEnumParameter) {
		this.previousEnumParameter = previousEnumParameter;
	}

	/**
	 * Gets previous compound parameter
	 * 
	 * @return the previousCompoundParameter
	 */
	public CompoundParameter getPreviousCompoundParameter() {
		return previousCompoundParameter;
	}

	/**
	 * Sets previous compound parameter
	 * 
	 * @param previousCompoundParameter
	 *            the previousCompoundParameter to set
	 */
	public void setPreviousCompoundParameter(
			CompoundParameter previousCompoundParameter) {
		this.previousCompoundParameter = previousCompoundParameter;
	}

	/**
	 * Gets inside metadata block boolean
	 * 
	 * @return the insideMetadataBlock
	 */
	public boolean isInsideMetadataBlock() {
		return insideMetadataBlock;
	}

	/**
	 * Sets inside metadata block boolean
	 * 
	 * @param insideMetadataBlock
	 *            the insideMetadataBlock to set
	 */
	public void setInsideMetadataBlock(boolean insideMetadataBlock) {
		this.insideMetadataBlock = insideMetadataBlock;
	}

	/**
	 * Gets the parent decode object
	 * 
	 * @return the parent Decode Object
	 */
	public ParentDecodeObject getParentDecodeObject() {
		return parentDecodeObject;
	}

	/**
	 * Sets the parent decode object
	 * 
	 * @param parentDecodeObject
	 *            the parentDecodeObject to set
	 */
	public void setParentDecodeObject(ParentDecodeObject parentDecodeObject) {
		this.parentDecodeObject = parentDecodeObject;
	}

	/**
	 * Gets trace instance list
	 * 
	 * @return the traceInstanceList
	 */
	public TraceInstanceList getTraceInstanceList() {
		return traceInstanceList;
	}

	/**
	 * Sets trace instance list
	 * 
	 * @param traceInstanceList
	 *            the traceInstanceList to set
	 */
	public void setTraceInstanceList(TraceInstanceList traceInstanceList) {
		this.traceInstanceList = traceInstanceList;
	}
}
