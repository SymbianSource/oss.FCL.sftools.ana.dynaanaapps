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
 * Parses the rule text field for validity
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.dataprocessor.FilterRuleSet.LogicalOperator;

/**
 * Parses the rule text field for validity
 */
public final class FilterAdvancedParser implements FocusListener, KeyListener,
		MouseListener {

	/**
	 * Indicates the max number of brackets in a rule. Used to shut down while
	 * loop if there is something wrong
	 */
	private static final int MAX_NUMBER_OF_BRACKETS = 50;

	/**
	 * AND String
	 */
	public static final String AND = "AND"; //$NON-NLS-1$

	/**
	 * OR String
	 */
	public static final String OR = "OR"; //$NON-NLS-1$

	/**
	 * NOT String
	 */
	public static final String NOT = "NOT"; //$NON-NLS-1$

	/**
	 * Start bracket
	 */
	private static final char START_BRACKET = '(';

	/**
	 * Start bracket String
	 */
	public static final String START_BRACKET_STR = "("; //$NON-NLS-1$

	/**
	 * End bracket
	 */
	private static final char END_BRACKET = ')';

	/**
	 * End bracket String
	 */
	public static final String END_BRACKET_STR = ")"; //$NON-NLS-1$

	/**
	 * Space
	 */
	public static final char SPACE = ' ';

	/**
	 * Space String
	 */
	public static final String SPACE_STR = " "; //$NON-NLS-1$

	/**
	 * Empty String
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Text field to validate
	 */
	private final StyledText textField;

	/**
	 * Rule items
	 */
	private final TreeItem[] items;

	/**
	 * Blue color
	 */
	private final Color blue;

	/**
	 * Gray color
	 */
	private final Color gray;

	/**
	 * Constructor
	 * 
	 * @param ruleTextfield
	 *            rule text field
	 * @param items
	 *            rule items
	 */
	public FilterAdvancedParser(StyledText ruleTextfield, TreeItem[] items) {
		textField = ruleTextfield;
		this.items = items;
		blue = textField.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		gray = textField.getDisplay().getSystemColor(SWT.COLOR_GRAY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if (e.keyCode != SWT.CTRL) {
			validate();

			// Set button states
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().getFilterDialog().getAdvancedDialog()
					.setButtonStates();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (textField.getText().length() > 1) {
			Point selection = null;
			if (textField.getSelectionRange().y > 0) {
				selection = textField.getSelection();
			}
			validate();
			// Set button states
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getFilterProcessor().getFilterDialog().getAdvancedDialog()
					.setButtonStates();
			if (selection != null) {
				textField.setSelection(selection);
			}
		}
	}

	/**
	 * Validates the text field
	 */
	void validate() {
		textField.getCaret().setVisible(false);
		String text = textField.getText();
		// Change logical operations to upper case
		text = text.replace(" and ", SPACE + AND + SPACE); //$NON-NLS-1$
		text = text.replace(" or ", SPACE + OR + SPACE); //$NON-NLS-1$
		text = text.replace(" not ", SPACE + NOT + SPACE); //$NON-NLS-1$

		StringBuffer newText = new StringBuffer(text);
		int caretPos = textField.getCaretOffset();

		int removedBeforeCaret = removeDoubleSpaces(newText, caretPos);
		caretPos -= removedBeforeCaret;

		// Empty, add one space
		if (newText.length() == 0) {
			newText.append(SPACE);
			caretPos = 1;

		}

		// Insert the text and color the caret
		textField.setText(newText.toString());
		textField.setCaretOffset(caretPos);
		colorCaretAndRules();
		textField.getCaret().setVisible(true);
	}

	/**
	 * Removes double spaces
	 * 
	 * @param newText
	 *            StringBuffer to use
	 * @param caretPos
	 *            old caret position
	 * @return number of chars removed before caret position
	 */
	private int removeDoubleSpaces(StringBuffer newText, int caretPos) {
		int charsRemoved = 0;
		boolean lastCharSpace = false;
		for (int i = 0; i < newText.length(); i++) {
			if (newText.charAt(i) == SPACE) {
				if (lastCharSpace) {
					newText.deleteCharAt(i);
					if (i < caretPos) {
						charsRemoved++;
					}
					i--;
				}
				lastCharSpace = true;
			} else {
				lastCharSpace = false;
			}
		}
		return charsRemoved;
	}

	/**
	 * Counts occurrences of a character from a string
	 * 
	 * @param text
	 *            text to find from
	 * @param c
	 *            the character
	 * @return number of occurrences in the string
	 */
	private int countOccurrences(String text, char c) {
		int occ = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == c) {
				occ++;
			}
		}
		return occ;
	}

	/**
	 * Colors the caret and the rules
	 */
	private void colorCaretAndRules() {
		List<StyleRange> ranges = new ArrayList<StyleRange>();
		int caretPos = textField.getCaretOffset();

		// Color the rules
		if (items != null) {
			String text = textField.getText();
			text = text.replace(START_BRACKET, SPACE);
			text = text.replace(END_BRACKET, SPACE);
			StringBuffer buf = new StringBuffer();
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Integer> offsets = new ArrayList<Integer>();

			// Gather the words
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == SPACE) {
					// If the buffer is not empty, it's a word and should be
					// added to the list
					if (!buf.toString().replace(SPACE_STR, EMPTY).equals(EMPTY)) {
						words.add(buf.toString());
						offsets.add(Integer.valueOf(i - buf.length()));
					}
					buf.setLength(0);
				} else {
					buf.append(c);
				}
			}

			// Create the style ranges for the basic rules found
			for (int j = 0; j < words.size(); j++) {
				if (isBasicRule(words.get(j))) {
					ranges.add(new StyleRange(offsets.get(j).intValue(), words
							.get(j).length(), blue, null));
				}
			}
		}

		// Color the caret if not focused
		if (!textField.isFocusControl()) {
			if (caretPos > textField.getCharCount()) {
				caretPos = textField.getCharCount();
			}
			ranges.add(new StyleRange(caretPos - 1, 1, gray, gray));
		}
		// Sort and set the ranges
		ranges = sortColorRanges(ranges);
		textField.setStyleRanges(ranges.toArray(new StyleRange[ranges.size()]));
	}

	/**
	 * Sorts color ranges
	 * 
	 * @param ranges
	 *            color ranges
	 * @return ordered list
	 */
	private List<StyleRange> sortColorRanges(List<StyleRange> ranges) {
		Collections.sort(ranges, new Comparator<StyleRange>() {

			public int compare(StyleRange o1, StyleRange o2) {
				int id1 = o1.start;
				int id2 = o2.start;
				return id1 > id2 ? 1 : id1 < id2 ? -1 : 0;
			}

		});
		return ranges;
	}

	/**
	 * Finds and returns a set from caret pos
	 * 
	 * @param text
	 *            text to search from
	 * @param caretOffset
	 *            caret offset
	 * @return the set where the caret is on
	 */
	private String findSet(String text, int caretOffset) {
		String set = null;
		if (text.length() > 0) {

			// Get counts for the brackets
			int startCount = countOccurrences(text.substring(0, caretOffset),
					START_BRACKET);
			int endCount = countOccurrences(text.substring(0, caretOffset),
					END_BRACKET);

			// Full sets before the caret, start from last end bracket
			if (startCount == endCount) {
				// Get indices for the nearest brackets
				int startIdx = getNearestChar(text, caretOffset, START_BRACKET,
						true);
				int endIdx = getNearestChar(text, caretOffset, END_BRACKET,
						false);
				if (startIdx == -1) {
					startIdx = text.length();
				}
				if (endIdx == -1) {
					endIdx = 0;
				} else {
					endIdx++;
				}
				set = text.substring(endIdx, startIdx);
			} else {
				// Get indices for the nearest brackets
				int startIdx = getNearestChar(text, caretOffset, START_BRACKET,
						false);
				int endIdx = getNearestChar(text, caretOffset, END_BRACKET,
						true);
				if (startIdx == -1) {
					startIdx = 0;
				} else {
					startIdx++;
				}
				if (endIdx == -1) {
					endIdx = text.length();
				}
				set = text.substring(startIdx, endIdx);
			}
		}
		return set;
	}

	/**
	 * Gets nearest char from a string
	 * 
	 * @param text
	 *            text to search from
	 * @param caretOffset
	 *            caret offset
	 * @param c
	 *            character to search for
	 * @param forward
	 *            forward or backward search
	 * @return offset of nearest character. -1 if not found
	 */
	private int getNearestChar(String text, int caretOffset, char c,
			boolean forward) {
		int idx = -1;
		// Searching forwards
		if (forward) {
			idx = text.indexOf(c, caretOffset);

			// Searching backwards
		} else if (text.length() > 0) {
			for (int i = caretOffset - 1; i >= 0; i--) {
				if (text.charAt(i) == c) {
					idx = i;
					break;
				}
			}
		}
		return idx;
	}

	/**
	 * Tells if a set is open
	 * 
	 * @return true if set is open
	 */
	boolean isSetOpen() {
		boolean setOpen = false;
		int startCount = countOccurrences(textField.getText(), START_BRACKET);
		int endCount = countOccurrences(textField.getText(), END_BRACKET);
		if (startCount > endCount) {
			setOpen = true;
		}
		return setOpen;
	}

	/**
	 * Check if AND can be inserted to current pos
	 * 
	 * @return true if AND can be inserted
	 */
	boolean canAndBeInserted() {
		boolean canBeInserted = true;
		String set = findSet(textField.getText(), textField.getCaretOffset());
		// If there is OR
		if (set != null && set.contains(OR)) {
			canBeInserted = false;
		} else if (set != null && !containsFilterRule(set, items)
				&& !isSetOpen()
				&& !containsFilterRule(textField.getText(), items)) {
			canBeInserted = false;
		} else if (set != null && !containsFilterRule(set, items)
				&& isSetOpen()) {
			canBeInserted = false;
		} else if (set == null) {
			canBeInserted = false;
		}

		return canBeInserted;
	}

	/**
	 * Check if OR can be inserted to current pos
	 * 
	 * @return true if OR can be inserted
	 */
	boolean canOrBeInserted() {
		boolean canBeInserted = true;
		String set = findSet(textField.getText(), textField.getCaretOffset());
		// If there is AND
		if (set != null && set.contains(AND)) {
			canBeInserted = false;
		} else if (set != null && !containsFilterRule(set, items)
				&& !isSetOpen()
				&& !containsFilterRule(textField.getText(), items)) {
			canBeInserted = false;
		} else if (set != null && !containsFilterRule(set, items)
				&& isSetOpen()) {
			canBeInserted = false;
		} else if (set == null) {
			canBeInserted = false;
		}

		return canBeInserted;
	}

	/**
	 * Check if new set can be started to current pos
	 * 
	 * @return true if new set can be started
	 */
	boolean canNewSetBeStarted() {
		boolean canBeStarted = false;
		String text = textField.getText();
		int caretPos = textField.getCaretOffset();
		boolean ready = false;
		StringBuffer buf = new StringBuffer();

		// Text has to contain at least one rule
		if (containsFilterRule(text, items) && caretPos > 1) {

			// Go backwards and check the previous word
			for (int i = caretPos - 1; i > 0; i--) {
				char c = text.charAt(i);
				// Space, do nothing
				if (c == SPACE) {
					if (ready) {
						// Remove NOT
						if (buf.toString().equals(NOT)) {
							buf.setLength(0);
							ready = false;
						} else {
							break;
						}
					}
				} else {
					buf.insert(0, c);
					ready = true;
				}
			}

			String word = buf.toString();
			if (word.contains(OR) || word.contains(AND)) {
				canBeStarted = true;
			}
		} else {
			canBeStarted = true;
		}
		return canBeStarted;
	}

	/**
	 * Check if a set can be ended here
	 * 
	 * @return true if a set can be ended here
	 */
	boolean canSetBeEnded() {
		boolean canBeEnded = false;
		String text = textField.getText();
		int caretPos = textField.getCaretOffset();
		boolean ready = false;
		StringBuffer buf = new StringBuffer();

		// Text has to contain at least one rule
		if (containsFilterRule(text, items) && isSetOpen()) {
			// Go backwards and check the previous word
			for (int i = caretPos - 1; i > 0; i--) {
				char c = text.charAt(i);
				// Space, do nothing
				if (c == SPACE) {
					if (ready) {
						break;
					}
				} else {
					buf.insert(0, c);
					ready = true;
				}
			}

			String word = buf.toString();
			if (!word.contains(OR) && !word.contains(AND)) {
				canBeEnded = true;
			}
		}
		return canBeEnded;
	}

	/**
	 * Check if apply can be clicked
	 * 
	 * @return true if apply can be clicked
	 */
	boolean canApplyBeClicked() {
		boolean canBeClicked = false;
		if (!isSetOpen()) {
			String text = textField.getText();

			// Length bigger than one and contains at least one rule
			if (text.length() > 1 && containsFilterRule(text, items)) {
				int nrOfRules = 0;
				// Check that after every rule there cannot be another rule
				text = text.replace(START_BRACKET_STR, SPACE_STR);
				text = text.replace(END_BRACKET_STR, SPACE_STR);
				String[] words = text.split(SPACE_STR);
				boolean lastWasRule = false;
				for (int i = 0; i < words.length; i++) {
					if (containsFilterRule(words[i], items)) {
						nrOfRules++;
						canBeClicked = true;
						if (lastWasRule) {
							canBeClicked = false;
							break;
						}
						lastWasRule = true;
					} else {
						lastWasRule = false;
					}

				}
				// If more than one rule, there has to be logical operator too
				if (canBeClicked && nrOfRules > 1) {
					// All good
					if (text.contains(AND) || text.contains(OR)) {
						// Not good
					} else {
						canBeClicked = false;
					}
				}

				// Check that logical operators have a rule in both sides
				if (canBeClicked) {
					text = text.replace(START_BRACKET_STR, EMPTY);
					text = text.replace(END_BRACKET_STR, EMPTY);
					text = text.replace(NOT, EMPTY);

					// Remove double spaces
					StringBuffer buf = new StringBuffer(text);
					removeDoubleSpaces(buf, 0);
					text = buf.toString();

					canBeClicked = checkLogicalOperatorValidity(canBeClicked,
							text, AND);
					if (canBeClicked) {
						canBeClicked = checkLogicalOperatorValidity(
								canBeClicked, text, OR);
					}
				}

				// Length is 0 or 1
			} else if (text.length() <= 1) {
				canBeClicked = true;
			}
		}
		return canBeClicked;
	}

	/**
	 * Checks that logical operators have filter rule in both sides
	 * 
	 * @param canBeClicked
	 *            variable indicating if apply can be clicked
	 * @param text
	 *            text to process
	 * @param operator
	 *            logical operator as string
	 * @return true if logical operators are ok
	 */
	private boolean checkLogicalOperatorValidity(boolean canBeClicked,
			String text, String operator) {
		StringBuffer buf = new StringBuffer();
		int pos = text.indexOf(operator);
		// Loop while next operator is found
		while (pos != -1) {
			// Check backwards
			for (int i = pos - 2; i > -1; i--) {
				char c = text.charAt(i);
				if (c == SPACE) {
					break;
				}
				buf.insert(0, c);
			}
			if (!ruleInItems(buf.toString(), items)) {
				canBeClicked = false;
			}
			buf.setLength(0);

			// Check forwards
			for (int i = pos + operator.length() + 1; canBeClicked
					&& i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == SPACE) {
					break;
				}
				buf.append(c);
			}
			if (!ruleInItems(buf.toString(), items)) {
				canBeClicked = false;
			}
			buf.setLength(0);

			pos = text.indexOf(operator, pos + 1);
		}

		return canBeClicked;
	}

	/**
	 * Checks if a string contains filter rule from the tree
	 * 
	 * @param text
	 *            text to search from
	 * @param items
	 *            items to go through
	 * @return true if at least one rule can be found from the string
	 */
	boolean containsFilterRule(String text, TreeItem[] items) {
		boolean found = false;
		if (text != null && items != null) {
			for (int i = 0; i < items.length && !found; i++) {

				// Contains children, recurse
				if (items[i].getItemCount() > 0) {
					found = containsFilterRule(text, items[i].getItems());

					// Check item
				} else if (text.contains(items[i].getText())) {
					found = true;
				}
			}
		}
		return found;
	}

	/**
	 * Gets logical operator from a text
	 * 
	 * @param text
	 *            text to get logical operator from
	 * @return logical operator of this set. Returns OR if this is a basic rule.
	 *         Null if both operators are found.
	 */
	LogicalOperator getLogicalOperator(String text) {
		LogicalOperator op = LogicalOperator.OR;
		String mainLevel = getMainLevelOfRule(text);

		// Contains AND but no OR
		if (mainLevel.contains(AND) && !mainLevel.contains(OR)) {
			op = LogicalOperator.AND;
			// Contains OR but no AND
		} else if (mainLevel.contains(OR) && !mainLevel.contains(AND)) {
			op = LogicalOperator.OR;
			// Contains either both or not any
		} else if ((mainLevel.contains(AND) && mainLevel.contains(OR))
				|| (!mainLevel.contains(AND) && !mainLevel.contains(OR))) {
			String newText = removeStartAndEndBracket(text);
			if (newText != null) {
				text = newText;
			}
			text = text.replace(NOT, EMPTY);
			text = text.trim();
			if (!isBasicRule(text)) {
				// Is not basic rule
				op = null;
			}
		}
		return op;
	}

	/**
	 * Gets the main level of the rule
	 * 
	 * @param text
	 *            rule text to process
	 * @return main level of the rule
	 */
	private String getMainLevelOfRule(String text) {
		int safeCounter = 0;
		String mainLevel = removeTextBetweenBrackets(text);
		if (mainLevel != null && mainLevel.indexOf(OR) == -1
				&& mainLevel.indexOf(AND) == -1) {
			mainLevel = text;
		}
		while (mainLevel != null && mainLevel.indexOf(OR) == -1
				&& mainLevel.indexOf(AND) == -1 && mainLevel.length() > 0
				&& safeCounter < MAX_NUMBER_OF_BRACKETS) {
			safeCounter++;
			mainLevel = removeStartAndEndBracket(mainLevel);
			mainLevel = removeTextBetweenBrackets(mainLevel);
		}
		return mainLevel;
	}

	/**
	 * Removes all text between brackets (including brackets)
	 * 
	 * @param text
	 *            text to be processed
	 * @return text without information inside brackets
	 */
	private String removeTextBetweenBrackets(String text) {
		StringBuffer str = new StringBuffer();
		if (text != null && text.length() > 0) {
			int nrOfStartBrackets = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == START_BRACKET) {
					nrOfStartBrackets++;
				} else if (c == END_BRACKET) {
					nrOfStartBrackets--;
				} else if (nrOfStartBrackets == 0) {
					str.append(c);
				}
			}
		}
		return str.toString();
	}

	/**
	 * Removes first start and last end bracket
	 * 
	 * @param text
	 *            text to process
	 * @return text without first start and last end bracket. Null if cannot
	 *         find both brackets
	 */
	private String removeStartAndEndBracket(String text) {
		String ret = null;
		int startPos = text.indexOf(START_BRACKET);
		int endPos = text.lastIndexOf(END_BRACKET);
		if (startPos != -1 && endPos != -1 && startPos + 1 < endPos) {
			ret = text.substring(startPos + 1, endPos);
		}
		return ret;
	}

	/**
	 * Tells if the String given is a basic rule
	 * 
	 * @param text
	 *            String to search from
	 * @return true if the String given is a basic rule
	 */
	boolean isBasicRule(String text) {
		boolean basicRule = false;

		// Find rule from the tree items
		if (text.length() > 0 && !text.equals(SPACE_STR)) {
			if (ruleInItems(text, items)) {
				basicRule = true;
			}
		}
		return basicRule;
	}

	/**
	 * Checks if rule is contained in the items
	 * 
	 * @param text
	 *            rule text
	 * @param items
	 *            items array
	 * @return true if rule is found from the items
	 */
	private boolean ruleInItems(String text, TreeItem[] items) {
		boolean found = false;
		for (int i = 0; i < items.length; i++) {

			// Contains children, recurse
			if ((items[i].getItemCount() > 0)) {
				found = ruleInItems(text, items[i].getItems());

				// Check item
			} else if (text.equals(items[i].getText())) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Gets children rule sets from a String
	 * 
	 * @param text
	 *            String to split to rule sets
	 * @return array of children
	 */
	String[] getChildren(String text) {
		// Add space before start and after end brackets and to both sides of
		// each logical operator
		text = text.replace(START_BRACKET_STR, SPACE_STR + START_BRACKET_STR);
		text = text.replace(END_BRACKET_STR, END_BRACKET_STR + SPACE_STR);
		text = text.replace(AND, SPACE_STR + AND + SPACE_STR);
		text = text.replace(OR, SPACE_STR + OR + SPACE_STR);
		text = text.replace(NOT, SPACE_STR + NOT + SPACE_STR);

		String[] strings;
		int nrOfStartBrackets = 0;
		ArrayList<String> children = new ArrayList<String>();
		StringBuffer buf = new StringBuffer();

		// Go through every character
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == START_BRACKET) {
				nrOfStartBrackets++;
			} else if (c == END_BRACKET) {
				nrOfStartBrackets--;

				// When brackets are processed, create a child from the
				// buffer
				if (nrOfStartBrackets == 0) {
					addBufferToChildList(children, buf);
				}
			} else if (nrOfStartBrackets > 0) {
				buf.append(c);

				// Nr of start brackets is zero
			} else {
				// Character is space
				if (c == SPACE) {
					if (buf.length() > 0) {
						addBufferToChildList(children, buf);
					}
				} else {
					buf.append(c);
					// If processing the last character, add child
					if (i == text.length() - 1) {
						addBufferToChildList(children, buf);
					}
				}
			}
		}

		// Insert the strings to a array
		strings = new String[children.size()];
		for (int j = 0; j < children.size(); j++) {
			strings[j] = children.get(j);
		}

		return strings;
	}

	/**
	 * Adds buffer contents to child list and empties the buffer
	 * 
	 * @param children
	 *            children list
	 * @param buf
	 *            buffer
	 */
	private void addBufferToChildList(List<String> children, StringBuffer buf) {
		// AND and OR cannot be children
		if (!buf.toString().trim().equals(AND)
				&& !buf.toString().trim().equals(OR)) {
			// Add child to the list
			children.add(buf.toString());
		}
		buf.setLength(0);
	}

	/**
	 * Checks written rules
	 * 
	 * @param text
	 *            text to check
	 * @return true if text field contains only valid rules and operations
	 */
	boolean checkWrittenRules(String text) {
		// Replace brackets and logical operations with spaces
		text = text.replace(START_BRACKET_STR, SPACE_STR);
		text = text.replace(END_BRACKET_STR, SPACE_STR);
		text = text.replace(AND, SPACE_STR);
		text = text.replace(OR, SPACE_STR);
		text = text.replace(NOT, SPACE_STR);

		// Add space to start and end and then remove filter rules
		StringBuffer buf = new StringBuffer(text);
		buf.insert(0, SPACE);
		buf.append(SPACE);
		text = removeFilterRules(buf.toString(), items);

		// Remove spaces
		text = text.replace(SPACE_STR, EMPTY);

		// Text should now be empty
		boolean empty = text.length() == 0;
		return empty;
	}

	/**
	 * Removes filter rules from the text
	 * 
	 * @param text
	 *            the text
	 * @param items
	 *            tree items
	 * @return original text without filter rules
	 */
	private String removeFilterRules(String text, TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {

			// Has children, recurse
			if (items[i].getItemCount() > 0) {
				text = removeFilterRules(text, items[i].getItems());

				// Remove the rule from the text.
			} else {
				text = text.replace(items[i].getText() + SPACE_STR, EMPTY);
			}
		}
		return text;
	}

	/**
	 * Gets previous word from the text field
	 * 
	 * @param text
	 *            text to search from
	 * @param pos
	 *            position where to read
	 * @return previous word
	 */
	String getPreviousWord(String text, int pos) {
		StringBuffer buf = new StringBuffer();
		for (int i = pos; i >= 0; i--) {
			char c = text.charAt(i);

			// Space, break
			if ((c == SPACE) && (i < pos - 1)) {
				break;
			}
			buf.insert(0, c);
		}
		return buf.toString().trim();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events
	 * .FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		colorCaretAndRules();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events
	 * .FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		colorCaretAndRules();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
	 * .KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
	}
}
