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
* Parser for preprocessor definitions and include statements
*
*/
package com.nokia.tracebuilder.source;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parser for preprocessor definitions and include statements
 * 
 */
class PreprocessorDefinitionParser {

	/**
	 * Source parser
	 */
	private SourceParser parser;

	/**
	 * List of #define's found from source
	 */
	protected ArrayList<SourcePreprocessorDefinition> preprocessorDefinitions;

	/**
	 * List of #include's found from source
	 */
	protected ArrayList<SourceInclude> includes;

	/**
	 * Constructor
	 * 
	 * @param parser
	 *            the source parser
	 */
	PreprocessorDefinitionParser(SourceParser parser) {
		this.parser = parser;
	}

	/**
	 * Resets all definitions parsed from the source
	 */
	void reset() {
		if (preprocessorDefinitions != null) {
			preprocessorDefinitions.clear();
		}
		if (includes != null) {
			includes.clear();
		}
	}

	/**
	 * Locates the value of a preprocessor definition for given name.
	 * 
	 * @param name
	 *            the name of the definition
	 * @return the value or null if not found
	 * @throws SourceParserException
	 *             if parser fails
	 */
	String findDefine(String name) throws SourceParserException {
		if (preprocessorDefinitions == null
				|| preprocessorDefinitions.isEmpty()) {
			createDefines();
		}
		Iterator<SourcePreprocessorDefinition> itr = preprocessorDefinitions
				.iterator();
		String retval = null;
		while (itr.hasNext() && retval == null) {
			SourcePreprocessorDefinition def = itr.next();
			if (def.getName().equals(name)) {
				retval = def.getValue();
			}
		}
		return retval;
	}

	/**
	 * Finds an include definition with given name
	 * 
	 * @param name
	 *            the include file name
	 * @return
	 *            <ul>
	 *            <li>if found, index to start of #include
	 *            <li>if not, -1 - index to end of last #include
	 *            </ul>
	 * @throws SourceParserException
	 *             if parser fails
	 */
	int findInclude(String name) throws SourceParserException {

		createIncludes();

		Iterator<SourceInclude> itr = includes.iterator();
		int ret = -1;
		while (itr.hasNext()) {
			SourceInclude inc = itr.next();
			if (inc.getHeaderName().equalsIgnoreCase(name)) {
				ret = inc.getOffset();
			}
		}
		if (ret == -1 && includes.size() > 0) {
			SourceInclude inc = includes.get(includes.size() - 1);
			ret = -1 - (inc.getOffset() + inc.getLength());
		}
		return ret;
	}

	/**
	 * Creates the list of preprocessor definitions. This only works with simple
	 * values
	 * 
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private void createDefines() throws SourceParserException {
		if (preprocessorDefinitions == null) {
			preprocessorDefinitions = new ArrayList<SourcePreprocessorDefinition>();
		} else {
			preprocessorDefinitions.clear();
		}
		Iterator<SourceExcludedArea> excludedAreas = parser.getExcludedAreas()
				.iterator();
		while (excludedAreas.hasNext()) {
			createDefineFromExcludedArea(excludedAreas.next());
		}
	}

	/**
	 * Creates the list of include definitions
	 * 
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private void createIncludes() throws SourceParserException {
		if (includes == null) {
			includes = new ArrayList<SourceInclude>();
		} else {
			includes.clear();
		}
		Iterator<SourceExcludedArea> excludedAreas = parser.getExcludedAreas()
				.iterator();
		while (excludedAreas.hasNext()) {
			createIncludeFromExcludedArea(excludedAreas.next());
		}
	}

	/**
	 * Creates an #define definition from given excluded area
	 * 
	 * @param area
	 *            the area
	 * @throws SourceParserException
	 */
	private void createDefineFromExcludedArea(SourceExcludedArea area)
			throws SourceParserException {
		if (area.getType() == SourceExcludedArea.PREPROCESSOR_DEFINITION
				&& area.getLength() > SourceConstants.DEFINE.length()) {
			String tag = parser.getSource().get(area.getOffset(),
					SourceConstants.DEFINE.length());
			if (tag.equals(SourceConstants.DEFINE)) {
				// Cannot use SKIP_ALL here, since it contains SKIP_PREPROCESSOR
				SourceIterator itr = parser.createIterator(area.getOffset()
						+ SourceConstants.DEFINE.length(),
						SourceParser.SKIP_WHITE_SPACES
								| SourceParser.SKIP_COMMENTS);
				if (itr.hasNext()) {
					parsePreprocessorDefinition(itr);
				}
			}
		}
	}

	/**
	 * Creates an #include definition from given excluded area
	 * 
	 * @param area
	 *            the area
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private void createIncludeFromExcludedArea(SourceExcludedArea area)
			throws SourceParserException {
		if (area.getType() == SourceExcludedArea.PREPROCESSOR_DEFINITION
				&& area.getLength() > SourceConstants.INCLUDE.length()) {
			String tag = parser.getSource().get(area.getOffset(),
					SourceConstants.INCLUDE.length());
			if (tag.equals(SourceConstants.INCLUDE)) {
				// Cannot use SKIP_ALL here, since it contains SKIP_PREPROCESSOR
				SourceIterator itr = parser.createIterator(area.getOffset()
						+ SourceConstants.INCLUDE.length(),
						SourceParser.SKIP_WHITE_SPACES
								| SourceParser.SKIP_COMMENTS);
				if (itr.hasNext()) {
					parseInclude(itr);
				}
			}
		}
	}

	/**
	 * Parses a preprocessor definition. This only finds simple values
	 * 
	 * @param itr
	 *            source iterator, positioned so that next will return the first
	 *            character of the name of the definition
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private void parsePreprocessorDefinition(SourceIterator itr)
			throws SourceParserException {
		int start = itr.nextIndex();
		int defstart = -1;
		SourcePreprocessorDefinition def = new SourcePreprocessorDefinition(
				parser, start);
		while (itr.hasNext() && def != null) {
			itr.next();
			if (itr.hasSkipped()) {
				if (defstart == -1) {
					if ((itr.previousIndex() + 1) > start) {
						def.setName(parser.getSource().get(start,
								itr.previousIndex() + 1 - start));
						defstart = itr.currentIndex();
					} else {
						def = null;
					}
				} else {
					def.setValue(parser.getSource().get(defstart,
							itr.previousIndex() + 1 - defstart));
					def.setLength(itr.previousIndex() + 1 - def.getOffset());
					preprocessorDefinitions.add(def);
					def = null;
				}
			}
		}
	}

	/**
	 * Parses an include line
	 * 
	 * @param itr
	 *            source iterator, positioned so that next will return the first
	 *            character of the name of the definition
	 * @throws SourceParserException
	 *             if parser fails
	 */
	private void parseInclude(SourceIterator itr) throws SourceParserException {
		int start = itr.nextIndex();
		SourceInclude inc = new SourceInclude(parser, start);
		while (itr.hasNext() && inc != null) {
			itr.next();
			if (itr.hasSkipped() && (itr.previousIndex()) > (start + 1)) {
				inc.setHeaderName(parser.getSource().get(start + 1,
						itr.previousIndex() - (start + 1)));
				inc.setLength(itr.previousIndex() - inc.getOffset() + 1);
				includes.add(inc);
				inc = null;
			}
		}
	}

}
