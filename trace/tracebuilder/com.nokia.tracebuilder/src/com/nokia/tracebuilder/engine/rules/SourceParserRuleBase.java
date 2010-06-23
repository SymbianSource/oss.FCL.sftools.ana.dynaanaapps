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
* Implementation of parser rule
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.ArrayList;
import java.util.Iterator;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceParserRule;
import com.nokia.tracebuilder.engine.utils.TraceUtils;
import com.nokia.tracebuilder.model.Trace;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceGroup;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.project.TraceLocationParser;
import com.nokia.tracebuilder.project.TraceProjectAPI;
import com.nokia.tracebuilder.source.FormatMapping;
import com.nokia.tracebuilder.source.SourceExcludedArea;
import com.nokia.tracebuilder.source.SourceIterator;
import com.nokia.tracebuilder.source.SourceParser;
import com.nokia.tracebuilder.source.SourceParserException;

/**
 * Base class for parser rules. Instances of this class are added to the trace
 * model. The source engine uses the parses it finds from the model to find
 * trace locations from source files
 * 
 */
public abstract class SourceParserRuleBase extends RuleBase implements
		SourceParserRule, TraceLocationParser {

	/**
	 * Length of line comment tag "// "
	 */
	private static final int LINE_COMMENT_TAG_LENGTH = 3; // CodForChk_Dis_Magic

	/**
	 * Tag to be searched from source
	 */
	private String tag;

	/**
	 * Allowed tag suffixes
	 */
	private ArrayList<String> tagSuffixes = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            the tag to be searched from source
	 * @param tagSuffixes
	 *            the list of allowed suffixes to the tag
	 */
	protected SourceParserRuleBase(String tag, String[] tagSuffixes) {
		this.tag = tag;
		// Adds the sub-formats to the parsers
		if (tagSuffixes != null) {
			int len = tagSuffixes.length;
			for (int i = 0; i < len; i++) {
				this.tagSuffixes.add(tagSuffixes[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#getSearchTag()
	 */
	public String getSearchTag() {
		return tag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      isAllowedTagSuffix(java.lang.String)
	 */
	public boolean isAllowedTagSuffix(String tag) {
		boolean retval = false;
		if (tag != null) {
			if (tag.length() == 0 && tagSuffixes.isEmpty()) {
				retval = true;
			} else {
				for (int i = 0; i < tagSuffixes.size() && !retval; i++) {
					String s = tagSuffixes.get(i);
					if (s.length() == tag.length()) {
						retval = true;
						for (int j = 0; j < s.length() && retval; j++) {
							char c1 = s.charAt(j);
							// '?' can be any character
							if (c1 != '?') {
								retval = tag.charAt(j) == c1;
							}
						}
					}
				}
			}
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      processNewLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public void processNewLocation(TraceLocation location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceLocationParser#getLocationGroup()
	 */
	public String getLocationGroup() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      convertLocation(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public TraceConversionResult convertLocation(TraceLocation location)
			throws TraceBuilderException {
		// If converting an unrelated location to trace, the name
		// from the location is used
		TraceConversionResult result = new TraceConversionResult();
		result.name = location.getConvertedName();
		// Old Symbian traces could include comment associated with the location
		// If it exists, it will be used as text
		String listTitle = location.getLocationList().getListTitle();
		if (listTitle != null && listTitle.equals("SymbianTrace")) { //$NON-NLS-1$
			SourceExcludedArea excludedArea = findLocationComment(location);
			if (excludedArea != null
					&& excludedArea.getType() == SourceExcludedArea.LINE_COMMENT) {
				// This expects the %FORMATTED_TRACE% formatting
				// Removes the '// ' from the comment
				SourceParser parser = location.getParser();
				String comment = parser.getData(excludedArea.getOffset()
						+ LINE_COMMENT_TAG_LENGTH, excludedArea.getLength()
						- LINE_COMMENT_TAG_LENGTH);
				Iterator<TraceGroup> groups = getOwner().getModel().getGroups();
				if (groups.hasNext()) {
					result.group = groups.next().getName();
				}
				result.text = comment.trim();
			}			
		}
		Iterator<String> params = location.getParameters();
		if (params.hasNext()) {
			result.parameters = new ArrayList<ParameterConversionResult>();
			while (params.hasNext()) {
				String label = params.next();
				ParameterConversionResult res = new ParameterConversionResult();
				res.name = TraceUtils.convertName(label);
				res.type = null; // Unknown type -> Warning will be created
				result.parameters.add(res);
			}
		}
		return result;
	}

	/**
	 * Maps a format to conversion result
	 * 
	 * @param mapping
	 *            the mapping
	 * @return the conversion result
	 */
	protected ParameterConversionResult mapFormatToConversionResult(
			FormatMapping mapping) {
		ParameterConversionResult param = new ParameterConversionResult();
		param.type = mapping.type;
		if (mapping.isArray) {
			param.extensions = new ArrayList<TraceModelExtension>();
			param.extensions.add(new ArrayParameterRuleImpl());
		}
		return param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#
	 *      findLocationComment(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public SourceExcludedArea findLocationComment(TraceLocation location) {
		SourceParser parser = location.getParser();
		SourceExcludedArea excludedArea = null;
		if (parser != null) {
			try {
				int offset = location.getOffset() + location.getLength();
				SourceIterator itr = parser.createIterator(offset,
						SourceParser.SKIP_WHITE_SPACES);
				char c = itr.next();
				if (c == ';') {
					offset = itr.currentIndex();
					c = itr.next();
				}
				boolean skippedReturn = false;
				int commentStart = itr.currentIndex();
				for (int i = offset; i < commentStart; i++) {
					c = parser.getData(i);
					if (c == '\n') {
						skippedReturn = true;
					}
				}
				// Comment must be on same line
				if (!skippedReturn) {
					excludedArea = parser.getExcludedArea(commentStart);
				}
				if (excludedArea == null) {
					// If comment is not on same line, the previous line is
					// checked
					offset = parser.findStartOfLine(location.getOffset(), true,
							true);
					excludedArea = parser.getExcludedArea(offset - 1);
				}
			} catch (SourceParserException e) {
			}
		}
		return excludedArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.source.SourceParserRule#getLocationParser()
	 */
	public TraceLocationParser getLocationParser() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceLocationParser#
	 *      isLocationConverted(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public boolean isLocationConverted(TraceLocation location) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.project.TraceLocationParser#
	 *      checkLocationValidity(com.nokia.tracebuilder.engine.TraceLocation)
	 */
	public TraceBuilderErrorCode checkLocationValidity(TraceLocation location) {
		Trace trace = location.getTrace();
		TraceBuilderErrorCode retval = TraceBuilderErrorCode.TRACE_DOES_NOT_EXIST;
		if (trace != null) {
			retval = TraceBuilderErrorCode.OK;
		} else {
			// If the API does not match the parser, the needs conversion flag
			// is set
			TraceProjectAPI api = getOwner().getModel().getExtension(
					TraceProjectAPI.class);
			if (!api.getName().equals(location.getParserRule().getName())) {
				retval = TraceBuilderErrorCode.TRACE_NEEDS_CONVERSION;
			}
		}
		return retval;
	}

}