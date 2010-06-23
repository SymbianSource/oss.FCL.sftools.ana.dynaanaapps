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
 * Search Properties
 *
 */
package com.nokia.traceviewer.engine.dataprocessor;

/**
 * Search Properties class
 * 
 */
public class SearchProperties {

	/**
	 * Line where current search starts
	 */
	private int currentSearchStartLine;

	/**
	 * Line where search originally started
	 */
	private int originalSearchStartLine;

	/**
	 * Search string to be searched for
	 */
	private String searchString;

	/**
	 * Case sensitive attribute
	 */
	private boolean caseSensitive;

	/**
	 * Whole word attribute
	 */
	private boolean wholeWord;

	/**
	 * Regular expression attribute
	 */
	private boolean regExp;

	/**
	 * Tells if we are searching forward
	 */
	private boolean searchingForward = true;

	/**
	 * Tells if search is case sensitive
	 * 
	 * @return the caseSensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Sets case sensitive
	 * 
	 * @param caseSensitive
	 *            the caseSensitive to set
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Gets current search start line
	 * 
	 * @return the currentSearchStartLine
	 */
	public int getCurrentSearchStartLine() {
		return currentSearchStartLine;
	}

	/**
	 * Sets current search start line
	 * 
	 * @param currentSearchStartLine
	 *            the currentSearchStartLine to set
	 */
	public void setCurrentSearchStartLine(int currentSearchStartLine) {
		this.currentSearchStartLine = currentSearchStartLine;
	}

	/**
	 * Gets original search start line
	 * 
	 * @return the originalSearchStartLine
	 */
	public int getOriginalSearchStartLine() {
		return originalSearchStartLine;
	}

	/**
	 * Sets originial search start line
	 * 
	 * @param originalSearchStartLine
	 *            the originalSearchStartLine to set
	 */
	public void setOriginalSearchStartLine(int originalSearchStartLine) {
		this.originalSearchStartLine = originalSearchStartLine;
	}

	/**
	 * Tells if search is regular expression search
	 * 
	 * @return the regExp
	 */
	public boolean isRegExp() {
		return regExp;
	}

	/**
	 * Sets regular expression attribute
	 * 
	 * @param regExp
	 *            the regExp to set
	 */
	public void setRegExp(boolean regExp) {
		this.regExp = regExp;
	}

	/**
	 * Tells if search is forward search
	 * 
	 * @return the searchingForward
	 */
	public boolean isSearchingForward() {
		return searchingForward;
	}

	/**
	 * Sets search to go forward
	 * 
	 * @param searchingForward
	 *            the searchingForward to set
	 */
	public void setSearchingForward(boolean searchingForward) {
		this.searchingForward = searchingForward;
	}

	/**
	 * Gets search string
	 * 
	 * @return the searchString
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * Sets search string
	 * 
	 * @param searchString
	 *            the searchString to set
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * Tells if search need whole word to match
	 * 
	 * @return the wholeWord
	 */
	public boolean isWholeWord() {
		return wholeWord;
	}

	/**
	 * Sets search to need whole word to match
	 * 
	 * @param wholeWord
	 *            the wholeWord to set
	 */
	public void setWholeWord(boolean wholeWord) {
		this.wholeWord = wholeWord;
	}
}