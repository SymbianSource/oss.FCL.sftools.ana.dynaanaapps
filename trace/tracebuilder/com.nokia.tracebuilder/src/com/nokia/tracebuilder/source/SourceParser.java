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
* Parser for C++ source files
*
*/
package com.nokia.tracebuilder.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Parser for C++ source files. Commented and quoted areas are located during
 * the construction of this object and those areas are excluded from the
 * searches.
 * 
 */
public class SourceParser {

	/**
	 * Skip white spaces. Does not apply to string searches
	 */
	public final static int SKIP_WHITE_SPACES = 0x01; // CodForChk_Dis_Magic

	/**
	 * Skip string areas
	 */
	public final static int SKIP_STRINGS = 0x02; // CodForChk_Dis_Magic

	/**
	 * Search / iterate backwards
	 */
	public final static int BACKWARD_SEARCH = 0x04; // CodForChk_Dis_Magic

	/**
	 * Ignore character case in string searches
	 */
	public final static int IGNORE_CASE = 0x08; // CodForChk_Dis_Magic

	/**
	 * Skip commented areas
	 */
	public final static int SKIP_COMMENTS = 0x10; // CodForChk_Dis_Magic

	/**
	 * Skip preprocessor definitions
	 */
	public final static int SKIP_PREPROCESSOR = 0x80; // CodForChk_Dis_Magic

	/**
	 * Match beginning of word flag
	 */
	public final static int MATCH_WORD_BEGINNING = 0x20; // CodForChk_Dis_Magic

	/**
	 * Match end of word flag
	 */
	public final static int MATCH_WORD_END = 0x40; // CodForChk_Dis_Magic

	/**
	 * Match whole word flag
	 */
	public final static int MATCH_WHOLE_WORD = MATCH_WORD_BEGINNING
			| MATCH_WORD_END;

	/**
	 * Ignore all special areas during search
	 */
	public final static int SKIP_ALL = SKIP_WHITE_SPACES | SKIP_STRINGS
			| SKIP_COMMENTS | SKIP_PREPROCESSOR;

	/**
	 * Data to be searched.
	 */
	private SourceDocumentInterface source;

	/**
	 * List of source locations
	 */
	private ArrayList<SourceLocation> locations;

	/**
	 * Document factory for creating locations
	 */
	private SourceDocumentFactory documentFactory;

	/**
	 * Excluded area parser
	 */
	private ExcludedAreaParser excludedAreaParser;

	/**
	 * Context area parser
	 */
	private ContextAreaParser contextAreaParser;

	/**
	 * Function return value parser
	 */
	private FunctionReturnValueParser returnValueParser;

	/**
	 * Pre-processor definition parser
	 */
	private PreprocessorDefinitionParser preprocessorParser;

	/**
	 * Parser for type definitions
	 */
	private TypedefParser typedefParser;

	/**
	 * Creates a new parser for given data
	 * 
	 * @param factory
	 *            the document factory
	 * @param sourceData
	 *            the source data
	 */
	public SourceParser(SourceDocumentFactory factory, String sourceData) {
		this(factory, factory.createDocument(sourceData));
	}

	/**
	 * Creates a source parser from source document
	 * 
	 * @param factory
	 *            the document factory
	 * @param source
	 *            the source document
	 */
	public SourceParser(SourceDocumentFactory factory,
			SourceDocumentInterface source) {
		this.documentFactory = factory;
		this.source = source;
		excludedAreaParser = new ExcludedAreaParser(this);
		contextAreaParser = new ContextAreaParser(this);
		returnValueParser = new FunctionReturnValueParser(this);
		try {
			findExcludedAreas();
		} catch (SourceParserException e) {
		}
	}

	/**
	 * Gets the source document
	 * 
	 * @return the source
	 */
	public SourceDocumentInterface getSource() {
		return source;
	}

	/**
	 * Gets part of the source document data
	 * 
	 * @param start
	 *            the start offset
	 * @param length
	 *            the data length
	 * @return the data or null if offsets are not valid
	 */
	public String getData(int start, int length) {
		String retval;
		try {
			retval = source.get(start, length);
		} catch (SourceParserException e) {
			retval = null;
		}
		return retval;
	}

	/**
	 * Gets a character at given offset
	 * 
	 * @param offset
	 *            the offset
	 * @return the character
	 */
	public char getData(int offset) {
		char retval;
		try {
			retval = source.getChar(offset);
		} catch (SourceParserException e) {
			retval = '\0';
		}
		return retval;
	}

	/**
	 * Gets the source document length
	 * 
	 * @return the source length
	 */
	public int getDataLength() {
		return source.getLength();
	}

	/**
	 * Gets the line number of given offset
	 * 
	 * @param offset
	 *            the offset
	 * @return the line number or -1 if offset is not valid
	 */
	public int getLineNumber(int offset) {
		int retval;
		try {
			retval = source.getLineOfOffset(offset) + 1;
		} catch (SourceParserException e) {
			retval = -1;
		}
		return retval;
	}

	/**
	 * Starts a new string search from given offset
	 * 
	 * @param searchString
	 *            the string to be searched
	 * @param startOffset
	 *            the offset to start of search
	 * @param endOffset
	 *            the end of search or -1 to search the whole document
	 * @param searchFlags
	 *            the search flags
	 * @return the search object
	 */
	public SourceStringSearch startStringSearch(String searchString,
			int startOffset, int endOffset, int searchFlags) {
		return new SourceStringSearch(this, searchString, startOffset,
				endOffset, searchFlags);
	}

	/**
	 * Tokenizes the parameter list starting from next bracket. White spaces are
	 * discarded. For example (a, b , c ) returns { "a", "b", "c" }. This method
	 * is independent of the current string search and thus can be used during
	 * one.
	 * 
	 * @param startIndex
	 *            the index where to start
	 * @param list
	 *            the list where the parameters are added
	 * @param findSeparator
	 *            if true, the processing stops after ';' or '{' character. If
	 *            false, processing stops after ')' at end of parameters
	 * @return the offset at end of the parameters
	 * @throws SourceParserException
	 *             if parameters are not valid
	 */
	public int tokenizeParameters(int startIndex, List<String> list,
			boolean findSeparator) throws SourceParserException {
		SourceParameterTokenizer tokenizer = new SourceParameterTokenizer(this,
				startIndex);
		return tokenizer.tokenize(list, findSeparator);
	}

	/**
	 * Parses the parameter list of given source context. Each entry added into
	 * the list will be an instance of SourceParameter class. This method is
	 * independent of the current string search and thus can be used during one.
	 * 
	 * @param parameterIndex
	 *            the index where to start
	 * @param list
	 *            the list where the parameters are added
	 * @return the offset at end of the parameters
	 * @throws SourceParserException
	 *             if context is not valid
	 */
	int parseFunctionParameters(int parameterIndex, List<SourceParameter> list)
			throws SourceParserException {
		SourceParameterTokenizer tokenizer = new SourceParameterTokenizer(this,
				parameterIndex);
		return tokenizer.tokenizeTyped(list);
	}

	/**
	 * Parses the return values of given source context
	 * 
	 * @param context
	 *            the context to be parsed
	 * @param list
	 *            the list of return values
	 */
	void parseReturnValues(SourceContext context, List<SourceReturn> list) {
		returnValueParser.parseReturnValues(context, list);
	}

	/**
	 * Parses an enum statement from the source
	 * 
	 * @param offset
	 *            the offset to the start of the enum type
	 * @param list
	 *            the list for the name-value pairs are parsed from source
	 * @return the name of the enum
	 * @throws SourceParserException
	 *             if parser fails
	 */
	public String parseEnum(int offset, List<StringValuePair> list)
			throws SourceParserException {
		if (typedefParser == null) {
			typedefParser = new TypedefParser(this);
		}
		typedefParser.parseEnum(offset, list);
		String retval;
		SourceIterator back = createIterator(offset,
				SourceParser.BACKWARD_SEARCH | SourceParser.SKIP_ALL);
		SourceIterator forw = createIterator(offset, SourceParser.SKIP_ALL);
		int start = -1;
		int end = -1;
		while (back.hasNext() && start == -1) {
			back.next();
			if (back.hasSkipped()) {
				start = back.previousIndex();
			}
		}
		while (forw.hasNext() && end == -1) {
			forw.next();
			if (forw.hasSkipped()) {
				end = forw.previousIndex();
			}
		}
		if (start != -1 && end != -1) {
			retval = source.get(start, end - start + 1);
		} else {
			retval = null;
		}
		return retval;
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
	public String findPreprocessorDefinition(String name)
			throws SourceParserException {
		if (preprocessorParser == null) {
			preprocessorParser = new PreprocessorDefinitionParser(this);
		}
		return preprocessorParser.findDefine(name);
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
	public int findInclude(String name) throws SourceParserException {
		if (preprocessorParser == null) {
			preprocessorParser = new PreprocessorDefinitionParser(this);
		}
		return preprocessorParser.findInclude(name);
	}

	/**
	 * Checks if the offset if within an excluded area
	 * 
	 * @param offset
	 *            the offset
	 * @return true if in excluded area
	 */
	public boolean isInExcludedArea(int offset) {
		return findExcludedAreaIndex(offset) >= 0;
	}

	/**
	 * Gets an excluded area
	 * 
	 * @param offset
	 *            the data offset
	 * @return the area or null
	 */
	public SourceExcludedArea getExcludedArea(int offset) {
		return excludedAreaParser.getArea(offset);
	}

	/**
	 * Creates a new source iterator
	 * 
	 * @param offset
	 *            the offset where to start
	 * @param iteratorFlags
	 *            the flags for the iterator
	 * @return the new iterator
	 */
	public SourceIterator createIterator(int offset, int iteratorFlags) {
		return new SourceIterator(this, offset, iteratorFlags);
	}

	/**
	 * Resets the parser
	 */
	public void reset() {
		if (locations != null) {
			// Sends delete notifications for all locations
			while (!locations.isEmpty()) {
				SourceLocation loc = locations.get(0);
				removeLocation(loc);
				loc.notifyLocationDeleted();
			}
		}
		source = null;
		resetExcludedAreas();
		resetContexts();
		resetPreprocessor();
	}

	/**
	 * Resets the context parser
	 */
	void resetContexts() {
		contextAreaParser.reset();
	}

	/**
	 * Resets the context parser
	 */
	void resetExcludedAreas() {
		excludedAreaParser.reset();
	}

	/**
	 * Resets the preprocessor parser
	 */
	void resetPreprocessor() {
		if (preprocessorParser != null) {
			preprocessorParser.reset();
		}
	}

	/**
	 * Returns the context at given offset. This parses the source if it has not
	 * been parsed before.
	 * 
	 * @param offset
	 *            the offset to the source data
	 * @return the context at the offset or null if no context exists
	 */
	public SourceContext getContext(int offset) {
		SourceContext retval;
		try {
			retval = contextAreaParser.parseAndGet(offset);
		} catch (SourceParserException e) {
			retval = null;
		}
		return retval;
	}

	/**
	 * Gets the context areas. This parses the source if it has not been parsed
	 * before.
	 * 
	 * @return the areas
	 */
	public Iterator<SourceContext> getContexts() {
		Iterator<SourceContext> retval;
		try {
			retval = contextAreaParser.parseAndGetAll();
		} catch (SourceParserException e) {
			List<SourceContext> list = Collections.emptyList();
			retval = list.iterator();
		}
		return retval;
	}

	/**
	 * Locates the start-of-line starting from given offset
	 * 
	 * @param offset
	 *            the offset
	 * @param cancelIfNotWhitespace
	 *            flag that tells to stop processing and return the original
	 *            value if a non-whitespace is found before start of line
	 * @param stayInContext
	 *            flag that tells to stay within the context offset currently
	 *            resides. If this would come out of the context, this locates
	 *            the start of the next line following offset
	 * @return the start-of-line
	 * @throws SourceParserException
	 *             if the offset is not valid
	 */
	public int findStartOfLine(int offset, boolean cancelIfNotWhitespace,
			boolean stayInContext) throws SourceParserException {
		int retval = offset == 0 ? offset : offset - 1;
		for (int i = retval; i >= 0; i--) {
			char c = source.getChar(i);
			if (source.getChar(i) == '\n') {
				retval = i + 1;
				i = -1;
			} else if (cancelIfNotWhitespace && !Character.isWhitespace(c)) {
				retval = offset;
				i = -1;
			}
		}
		if (stayInContext) {
			SourceContext context = getContext(offset);
			if (context != null && retval < context.getOffset()) {
				retval = context.getOffset();
				int end = context.getOffset() + context.getLength();
				for (int i = retval; i < end; i++) {
					if (source.getChar(i) == '\n') {
						retval = i + 1;
						i = end;
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Gets the indentation from the given line
	 * 
	 * @param offset
	 *            offset to a start-of-line
	 * @return the intendation
	 * @throws SourceParserException
	 *             if offset is not valid
	 */
	public String getIndent(int offset) throws SourceParserException {
		String retval = ""; //$NON-NLS-1$
		for (int i = offset; i < source.getLength(); i++) {
			char c = source.getChar(i);
			if (c != '\t' && c != ' ') {
				retval = source.get(offset, i - offset);
				i = source.getLength();
			}
		}
		return retval;
	}

	/**
	 * Locates the last return statement from the given context
	 * 
	 * @param context
	 *            the context
	 * @return the offset to the beginning of the return statement
	 */
	public int findReturn(SourceContext context) {
		return returnValueParser.findLast(context);
	}

	/**
	 * Creates a location to this parser. The reference count of the location is
	 * set to 1
	 * 
	 * @param index
	 *            the location index
	 * @param length
	 *            the length of the location
	 * @return the location
	 */
	public SourceLocation createLocation(int index, int length) {
		SourceLocation loc = new SourceLocation(this, index, length);
		return loc;
	}

	/**
	 * Finds the array index of the excluded area which contains the offset. If
	 * none of the areas contain the offset, returns negative integer indicating
	 * the index of the excluded area following the offset
	 * 
	 * @param offset
	 *            the offset to the data
	 * @return the excluded area index
	 */
	int findExcludedAreaIndex(int offset) {
		return excludedAreaParser.find(offset);
	}

	/**
	 * Finds the excluded source file areas. Excluded areas include comments and
	 * quoted strings. Overwrites possible old areas.
	 * 
	 * @throws SourceParserException
	 *             if parser fails
	 */
	public void findExcludedAreas() throws SourceParserException {
		excludedAreaParser.parseAll();
	}

	/**
	 * Gets the list of excluded areas
	 * 
	 * @return the list
	 */
	List<SourceExcludedArea> getExcludedAreas() {
		return excludedAreaParser.getAreas();
	}

	/**
	 * Checks if the given range contains excluded areas
	 * 
	 * @param offset
	 *            the start of range
	 * @param end
	 *            the end of rance
	 * @return true if it contains excluded areas
	 */
	boolean containsExcludedArea(int offset, int end) {
		return findExcludedAreaIndex(offset) != findExcludedAreaIndex(end);
	}

	/**
	 * Finds the array index of the context area which contains the offset. If
	 * none of the areas contain the offset, returns negative integer indicating
	 * the index of the excluded area following the offset
	 * 
	 * @param offset
	 *            the offset to the data
	 * @return the context area index
	 */
	int findContextAreaIndex(int offset) {
		return contextAreaParser.find(offset);
	}

	/**
	 * Builds the SourceContext array. findExcludedAreas needs to be called
	 * first
	 * 
	 * @throws SourceParserException
	 *             if parser fails
	 */
	void findContextAreas() throws SourceParserException {
		contextAreaParser.parseAll();
	}

	/**
	 * Checks if the area is excluded with given flags
	 * 
	 * @param type
	 *            the area type
	 * @param flags
	 *            the flags
	 * @return true if skipped
	 */
	static boolean isExcluded(int type, int flags) {
		boolean string = ((flags & SKIP_STRINGS) != 0)
				&& (type == SourceExcludedArea.STRING);
		boolean comment = ((flags & SKIP_COMMENTS) != 0)
				&& (type == SourceExcludedArea.MULTILINE_COMMENT);
		boolean linecomment = ((flags & SKIP_COMMENTS) != 0)
				&& (type == SourceExcludedArea.LINE_COMMENT);
		boolean preProcessor = ((flags & SKIP_PREPROCESSOR) != 0)
				&& (type == SourceExcludedArea.PREPROCESSOR_DEFINITION);
		return string || comment || linecomment || preProcessor;
	}

	/**
	 * Adds a hidden location to this source. The location offset, length and
	 * deleted flag will be updated as the source is updated, but the location
	 * will not be returned by getLocations. The location is not automatically
	 * removed from the source even if it is deleted.
	 * 
	 * @param offset
	 *            the location offset
	 * @param length
	 *            the location length
	 * @return the location or null if the document does not exist
	 */
	public SourceLocationBase createHiddenLocation(int offset, int length) {
		SourceLocationBase location = null;
		if (source != null && documentFactory != null) {
			location = new SourceLocationBase(this, offset, length);
			source.addLocation(location.getLocation());
		}
		return location;
	}

	/**
	 * Removes a hidden location from this source.
	 * 
	 * @param location
	 *            the location to be removed
	 */
	public void removeHiddenLocation(SourceLocationBase location) {
		if (source != null) {
			source.removeLocation(location.getLocation());
		}
	}

	/**
	 * Adds a location to this parser
	 * 
	 * @param location
	 *            the location
	 */
	void addLocation(SourceLocation location) {
		if (locations == null) {
			locations = new ArrayList<SourceLocation>();
		}
		locations.add(location);
		if (source != null) {
			source.addLocation(location.getLocation());
		}
	}

	/**
	 * Removes a location from this parser
	 * 
	 * @param location
	 *            the location to be removed
	 */
	void removeLocation(SourceLocation location) {
		if (locations != null) {
			locations.remove(location);
		}
		if (source != null) {
			source.removeLocation(location.getLocation());
		}
	}

	/**
	 * Gets the source locations
	 * 
	 * @return the list of locations
	 */
	Iterator<SourceLocation> getLocations() {
		List<SourceLocation> list;
		if (locations != null) {
			list = locations;
		} else {
			list = Collections.emptyList();
		}
		return list.iterator();
	}

	/**
	 * Gets the document framework
	 * 
	 * @return the document framework
	 */
	public SourceDocumentFactory getDocumentFramework() {
		return documentFactory;
	}

}
