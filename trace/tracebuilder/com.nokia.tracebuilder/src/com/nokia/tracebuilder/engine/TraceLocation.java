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
* Represents a location of a trace within a source document
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceParserResult;
import com.nokia.tracebuilder.engine.source.SourceParserRule;
import com.nokia.tracebuilder.engine.source.SourceProperties;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderErrorParameters;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceLocation;
import com.nokia.tracebuilder.source.SourceLocationListener;

/**
 * Represents a location of a trace within a source document. The locations of a
 * document are updated by TraceLocationUpdater when document is changed
 * 
 */
public class TraceLocation extends SourceLocation implements LocationProperties {

	/**
	 * Source which owns this location
	 */
	private SourceProperties source;

	/**
	 * Name of the trace as parsed from source
	 */
	private String originalName;

	/**
	 * Name after conversion to valid trace name
	 */
	private String convertedName;

	/**
	 * Text of the trace as parsed from source
	 */
	private String traceText;

	/**
	 * The tag of the parser that found this trace location
	 */
	private String tag;

	/**
	 * Content changed flag
	 */
	private boolean contentChanged;

	/**
	 * Name changed flag
	 */
	private boolean nameChanged;

	/**
	 * The location list
	 */
	private TraceLocationList list;

	/**
	 * The parameters
	 */
	private List<String> parameters;

	/**
	 * Parser-specific data associated with this location
	 */
	private List<String> parserData;

	/**
	 * Rule which defines how the parameters found from source are interpreted
	 */
	private SourceParserRule parserRule;

	/**
	 * Optional properties for this location.
	 */
	private TraceLocationProperties properties = new TraceLocationProperties();

	/**
	 * Flag, which determines if this location has changed after last convert
	 * operation
	 */
	private boolean changedAfterConvert = true;

	/**
	 * Last notified validity code
	 */
	private TraceBuilderErrorCode notifiedValidity = TraceBuilderErrorCode.OK;

	/**
	 * Last notified validity parameters
	 */
	private TraceBuilderErrorParameters notifiedValidityParameters;

	/**
	 * Parser error code
	 */
	private TraceBuilderErrorCode parserErrorCode = TraceBuilderErrorCode.OK;

	/**
	 * Parser error parameters
	 */
	private TraceBuilderErrorParameters parserErrorParameters;

	/**
	 * Converter error code
	 */
	private TraceBuilderErrorCode converterErrorCode = TraceBuilderErrorCode.OK;

	/**
	 * Converter error parameters
	 */
	private TraceBuilderErrorParameters converterErrorParameters;

	/**
	 * Creates a new location
	 * 
	 * @param source
	 *            the source where the location is associated
	 * @param offset
	 *            the offset to the trace within the source document
	 * @param length
	 *            the length of the trace
	 */
	public TraceLocation(SourceProperties source, int offset, int length) {
		super(source.getSourceEditor(), offset, length);
		this.source = source;
	}

	/**
	 * Sets the content changed flag. If <i>changed</i> is false this also sets
	 * the name changed flag to false
	 * 
	 * @param changed
	 *            the new changed flag
	 */
	public void setContentChanged(boolean changed) {
		this.contentChanged = changed;
		if (!changed) {
			nameChanged = false;
		}
	}

	/**
	 * Checks if the content has changed.
	 * 
	 * @return the content changed flag
	 */
	public boolean isContentChanged() {
		return contentChanged;
	}

	/**
	 * Checks if the name has changed.
	 * 
	 * @return the name changed flag
	 */
	public boolean isNameChanged() {
		return contentChanged && nameChanged;
	}

	/**
	 * Gets the trace this location is associated to or null if unrelated
	 * 
	 * @return the trace
	 */
	public Trace getTrace() {
		Trace retval = null;
		if (list != null) {
			TraceObject object = list.getOwner();
			if (object instanceof Trace) {
				retval = (Trace) object;
			}
		}
		return retval;
	}

	/**
	 * Gets the name of the trace as parsed from source
	 * 
	 * @return the name of the trace
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * Gets the name of the trace after conversion to valid name
	 * 
	 * @return the name of the trace
	 */
	public String getConvertedName() {
		return convertedName;
	}

	/**
	 * Gets the text of the trace
	 * 
	 * @return the text of the trace
	 */
	public String getTraceText() {
		return traceText;
	}

	/**
	 * Returns the source properties this location belongs to
	 * 
	 * @return source properties
	 */
	public SourceProperties getSource() {
		return source;
	}

	/**
	 * Sets the location list which owns this location
	 * 
	 * @param list
	 *            the location list
	 */
	public void setLocationList(TraceLocationList list) {
		this.list = list;
	}

	/**
	 * Gets the location list this location belongs to
	 * 
	 * @return the list
	 */
	public TraceLocationList getLocationList() {
		return list;
	}

	/**
	 * Sets the parser rule that will be used to parse the data of this
	 * location. Must be called before setData
	 * 
	 * @param rule
	 *            the rule used to parse the data
	 */
	public void setParserRule(SourceParserRule rule) {
		parserRule = rule;
	}

	/**
	 * Gets the parser rule that found this location
	 * 
	 * @return the parser rule
	 */
	public SourceParserRule getParserRule() {
		return parserRule;
	}

	/**
	 * Gets the optional properties of this location
	 * 
	 * @return the properties
	 */
	public TraceLocationProperties getProperties() {
		return properties;
	}

	/**
	 * Sets the trace tag that was found from source
	 * 
	 * @param tag
	 *            the trace tag
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * Gets the trace tag
	 * 
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Sets the trace data
	 * 
	 * @param list
	 *            the list of parameters
	 */
	public void setData(ArrayList<String> list) {
		try {
			SourceParserResult result = parserRule.parseParameters(tag, list);
			setData(result.originalName, result.convertedName,
					result.traceText, result.parameters, result.parserData);
			parserRule.getLocationParser().processNewLocation(this);
			parserErrorCode = TraceBuilderErrorCode.OK;
			parserErrorParameters = null;
			converterErrorCode = TraceBuilderErrorCode.OK;
			converterErrorParameters = null;
		} catch (TraceBuilderException e) {
			parserErrorCode = (TraceBuilderErrorCode) e.getErrorCode();
			parserErrorParameters = e.getErrorParameters();
		}
	}

	/**
	 * Sets the trace name and parameter list
	 * 
	 * @param originalName
	 *            the name parsed from source
	 * @param convertedName
	 *            the name after conversion
	 * @param traceText
	 *            the text parsed from source
	 * @param parameters
	 *            the list of parameters parsed from source
	 * @param parserData
	 *            list of parser-specific data
	 */
	private void setData(String originalName, String convertedName,
			String traceText, List<String> parameters, List<String> parserData) {
		if (!convertedName.equals(this.convertedName)) {
			this.convertedName = convertedName;
			nameChanged = true;
		}
		this.originalName = originalName;
		this.traceText = traceText;
		this.parameters = parameters;
		this.parserData = parserData;
	}

	/**
	 * Removes the parameter at given index
	 * 
	 * @param index
	 *            the index
	 */
	public void removeParameterAt(int index) {
		// NOTE: Does not cause validity callbacks
		// This is currently only used temporarily when removing a parameter
		// from trace. This is needed to correctly update the source code
		if (parameters != null && index >= 0 && index < parameters.size()) {
			parameters.remove(index);
		}
	}

	/**
	 * Gets the number of parameters found from the source code
	 * 
	 * @return the number of parameters
	 */
	public int getParameterCount() {
		return parameters != null ? parameters.size() : 0;
	}

	/**
	 * Gets the parameter at given index
	 * 
	 * @param index
	 *            the parameter index
	 * @return the parameter at the index
	 */
	public String getParameter(int index) {
		return parameters.get(index);
	}

	/**
	 * Gets the parameters
	 * 
	 * @return iterator over the parameters
	 */
	public Iterator<String> getParameters() {
		List<String> list = null;
		if (parameters != null) {
			list = parameters;
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}

	/**
	 * Gets the parser-specific data
	 * 
	 * @return the parser data
	 */
	public List<String> getParserData() {
		return parserData;
	}

	/**
	 * Gets the current location validity code.
	 * 
	 * @return the validity code
	 */
	public TraceBuilderErrorCode getValidityCode() {
		return notifiedValidity;
	}

	/**
	 * Gets the parameters associated with the current location validity code.
	 * 
	 * @return the validity code
	 */
	public TraceBuilderErrorParameters getValidityParameters() {
		return notifiedValidityParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.source.SourceLocation#notifyLocationChanged()
	 */
	@Override
	protected void notifyLocationChanged() {
		super.notifyLocationChanged();
		if (isContentChanged()) {
			changedAfterConvert = true;
			Iterator<SourceLocationListener> itr = getListeners();
			while (itr.hasNext()) {
				SourceLocationListener listener = itr.next();
				if (listener instanceof TraceLocationListener) {
					((TraceLocationListener) listener)
							.locationContentChanged(this);
				}
			}
			runValidityCheck();
		}
	}

	/**
	 * Creates a notification if validity has changed. This is initially called
	 * from the location list when a location is added to it and after that from
	 * notifyLocationChanged.
	 */
	void runValidityCheck() {
		TraceBuilderErrorCode code;
		TraceBuilderErrorParameters parameters;
		if (parserErrorCode != TraceBuilderErrorCode.OK) {
			code = parserErrorCode;
			parameters = parserErrorParameters;
		} else if (converterErrorCode != TraceBuilderErrorCode.OK) {
			code = converterErrorCode;
			parameters = converterErrorParameters;
		} else {
			code = TraceBuilderErrorCode.TRACE_DOES_NOT_EXIST;
			parameters = null;
			if (parserRule != null) {
				code = parserRule.getLocationParser().checkLocationValidity(
						this);
			}
		}
		if (code != notifiedValidity) {
			notifiedValidity = code;
			notifiedValidityParameters = parameters;
			Iterator<SourceLocationListener> itr = getListeners();
			while (itr.hasNext()) {
				SourceLocationListener listener = itr.next();
				if (listener instanceof TraceLocationListener) {
					((TraceLocationListener) listener)
							.locationValidityChanged(this);
				}
			}
		}
	}

	/**
	 * Sets the converter error code and runs the validity check to notify
	 * listeners about change in error code
	 * 
	 * @param errorCode
	 *            the new error code
	 * @param parameters
	 *            the error parameters
	 */
	void setConverterErrorCode(TraceBuilderErrorCode errorCode,
			TraceBuilderErrorParameters parameters) {
		converterErrorCode = errorCode;
		converterErrorParameters = parameters;
		runValidityCheck();
	}

	/**
	 * Flag, which determines if the location has changed since last convert
	 * operation
	 * 
	 * @return the flag
	 */
	public boolean hasChangedAfterConvert() {
		return changedAfterConvert;
	}

	/**
	 * Called when the location has been converted. Sets the changed after
	 * convert flag to false
	 */
	public void locationConverted() {
		changedAfterConvert = false;
	}

}
