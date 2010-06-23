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
 * Search Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerTraceViewInterface;
import com.nokia.traceviewer.engine.dataprocessor.SearchProperties;
import com.nokia.traceviewer.engine.dataprocessor.SearchUtils;
import com.nokia.traceviewer.engine.dataprocessor.TimestampParser;

/**
 * Search Dialog class
 * 
 */
public final class SearchDialog extends BaseDialog {

	/**
	 * Display used to get UI thread
	 */
	private final Display display;

	/**
	 * UI Label for line count field
	 */
	private Label lineCountLabel;

	/**
	 * UI Combo box for find texts
	 */
	private Combo findCombo;

	/**
	 * UI Find button
	 */
	private Button searchButton;

	/**
	 * UI Checkbox for match whole word
	 */
	private Button matchWholeWordCheckBox;

	/**
	 * UI Checkbox for match case
	 */
	private Button matchCaseCheckBox;

	/**
	 * UI Checkbox for regular expression search
	 */
	private Button regularExpressionCheckBox;

	/**
	 * UI Radio button for up direction
	 */
	private Button upDirectionRadioButton;

	/**
	 * UI Radio button for down direction
	 */
	private Button downDirectionRadioButton;

	/**
	 * UI Button for stop search
	 */
	private Button stopSearchButton;

	/**
	 * Progressbar showing file position
	 */
	private ProgressBar progressBar;

	/**
	 * Reference to View
	 */
	private final TraceViewerTraceViewInterface view;

	/**
	 * Search properties used when searching
	 */
	private final SearchProperties searchProperties;

	/**
	 * Widget used in searching
	 */
	private StyledText widget;

	/**
	 * Document used by viewer
	 */
	private IDocument document;

	/**
	 * Contains combo list items that are saved and restored
	 */
	private String[] comboList;

	/**
	 * Finder object
	 */
	private FindReplaceDocumentAdapter finder;

	/**
	 * Checks if DataReader was paused when we entered dialog -> Don't pause
	 * again and don't unpause in exit
	 */
	private boolean wasPausedWhenEntered;

	/**
	 * Key listener for this dialog
	 */
	private KeyAdapter searchDialogKeyListener;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param view
	 *            view reference
	 */
	public SearchDialog(Shell parent, TraceViewerTraceViewInterface view) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.MODELESS | SWT.MIN);
		this.view = view;
		this.display = view.getViewer().getControl().getDisplay();
		searchProperties = new SearchProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#close()
	 */
	@Override
	public boolean close() {
		boolean close = super.close();

		// Stop the search and kill the search data reader
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().stopSearch(true);
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().shutdownSearchReader();

		// Unpause
		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}

		return close;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#saveSettings()
	 */
	@Override
	protected void saveSettings() {

		// Save position
		super.saveSettings();

		// Save combo list items if dialog is not disposed
		if (findCombo != null && !findCombo.isDisposed()) {
			comboList = new String[findCombo.getItemCount()];
			for (int i = 0; i < findCombo.getItemCount(); i++) {
				comboList[i] = findCombo.getItem(i);
			}
			searchProperties
					.setWholeWord(matchWholeWordCheckBox.getSelection());
			searchProperties.setCaseSensitive(matchCaseCheckBox.getSelection());
			searchProperties
					.setRegExp(regularExpressionCheckBox.getSelection());
			if (upDirectionRadioButton.getSelection()) {
				searchProperties.setSearchingForward(false);
			} else {
				searchProperties.setSearchingForward(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#restoreSettings()
	 */
	@Override
	protected void restoreSettings() {
		super.restoreSettings();

		// Restore other settings
		if (comboList != null) {
			findCombo.setItems(comboList);
		}
		matchWholeWordCheckBox.setSelection(searchProperties.isWholeWord());
		if (searchProperties.isWholeWord()) {
			regularExpressionCheckBox.setEnabled(false);
		}
		matchCaseCheckBox.setSelection(searchProperties.isCaseSensitive());
		regularExpressionCheckBox.setSelection(searchProperties.isRegExp());
		if (searchProperties.isRegExp()) {
			matchWholeWordCheckBox.setEnabled(false);
		}
		upDirectionRadioButton.setSelection(!searchProperties
				.isSearchingForward());
		downDirectionRadioButton.setSelection(searchProperties
				.isSearchingForward());

		// Set text to be what was last searched for
		if (comboList != null && comboList.length > 0) {
			findCombo.setText(comboList[0]);
		}

		// Set default button
		getShell().setDefaultButton(searchButton);

		// Set processor not to stop when next search is started
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().stopSearch(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createShell()
	 */
	@Override
	protected void createDialogContents() {
		// Pause the datareader if it's not paused already
		wasPausedWhenEntered = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getMainDataReader().isPaused();
		if (!wasPausedWhenEntered) {
			TraceViewerGlobals.getTraceViewer().getView().getActionFactory()
					.getPauseAction().run();
		}

		// Create needed items
		TextViewer viewer = view.getViewer();
		widget = viewer.getTextWidget();
		document = viewer.getDocument();
		finder = new FindReplaceDocumentAdapter(document);

		// Shell and Composite
		GridLayout shellGridLayout = new GridLayout();
		shellGridLayout.numColumns = 3;
		getShell().setText(Messages.getString("SearchDialog.DialogTitle")); //$NON-NLS-1$
		composite.setLayout(shellGridLayout);
		Label findLabel = new Label(composite, SWT.NONE);
		findLabel.setText(Messages.getString("SearchDialog.FindText")); //$NON-NLS-1$

		// Create key listener
		createKeyListener();

		// Create find combo
		GridData findComboGridData = new GridData();
		findComboGridData.widthHint = 280;
		findCombo = new Combo(composite, SWT.NONE);
		findCombo.setLayoutData(findComboGridData);
		findCombo.setToolTipText(Messages
				.getString("SearchDialog.FindComboToolTip")); //$NON-NLS-1$
		findCombo.addKeyListener(searchDialogKeyListener);

		// Search Button
		GridData searchButtonGridData = new GridData();
		searchButtonGridData.widthHint = 75;
		searchButtonGridData.heightHint = 25;
		searchButtonGridData.horizontalAlignment = GridData.END;
		searchButton = new Button(composite, SWT.NONE);
		searchButton.setText(Messages
				.getString("SearchDialog.SearchButtonText")); //$NON-NLS-1$
		searchButton.setToolTipText(Messages
				.getString("SearchDialog.SearchButtonToolTip")); //$NON-NLS-1$
		searchButton.setLayoutData(searchButtonGridData);

		// Match whole word Checkbox
		GridData matchWholeWordGridData = new GridData();
		matchWholeWordGridData.horizontalSpan = 2;
		matchWholeWordGridData.verticalAlignment = GridData.END;
		matchWholeWordGridData.horizontalAlignment = GridData.BEGINNING;
		matchWholeWordCheckBox = new Button(composite, SWT.CHECK);
		matchWholeWordCheckBox.setText(Messages
				.getString("SearchDialog.MatchWholeWordText")); //$NON-NLS-1$
		matchWholeWordCheckBox.setToolTipText(Messages
				.getString("SearchDialog.MatchWholeWordToolTip")); //$NON-NLS-1$
		matchWholeWordCheckBox.setLayoutData(matchWholeWordGridData);

		// Stop Search Button
		GridData stopSeachButtonGridData = new GridData();
		stopSeachButtonGridData.widthHint = 75;
		stopSeachButtonGridData.heightHint = 25;
		stopSeachButtonGridData.horizontalAlignment = GridData.END;
		stopSearchButton = new Button(composite, SWT.NONE);
		stopSearchButton.setText(Messages
				.getString("SearchDialog.StopButtonText")); //$NON-NLS-1$
		stopSearchButton.setToolTipText(Messages
				.getString("SearchDialog.StopButtonToolTip")); //$NON-NLS-1$
		stopSearchButton.setLayoutData(stopSeachButtonGridData);
		stopSearchButton.setEnabled(false);

		// Match Case Checkbox
		GridData matchCaseCheckBoxGridData = new GridData();
		matchCaseCheckBoxGridData.horizontalSpan = 2;
		matchCaseCheckBox = new Button(composite, SWT.CHECK);
		matchCaseCheckBox.setText(Messages
				.getString("SearchDialog.MatchCaseText")); //$NON-NLS-1$
		matchCaseCheckBox.setToolTipText(Messages
				.getString("SearchDialog.MatchCaseToolTip")); //$NON-NLS-1$
		matchCaseCheckBox.setLayoutData(matchCaseCheckBoxGridData);

		// Regular Expression CheckBox
		GridData regularExpressionCheckBoxGridData = new GridData();
		regularExpressionCheckBoxGridData.horizontalSpan = 2;
		regularExpressionCheckBoxGridData.verticalAlignment = GridData.BEGINNING;
		regularExpressionCheckBoxGridData.horizontalAlignment = GridData.BEGINNING;
		regularExpressionCheckBox = new Button(composite, SWT.CHECK);
		regularExpressionCheckBox.setText(Messages
				.getString("SearchDialog.RegExpText")); //$NON-NLS-1$
		regularExpressionCheckBox.setToolTipText(Messages
				.getString("SearchDialog.RegExpToolTip")); //$NON-NLS-1$
		regularExpressionCheckBox
				.setLayoutData(regularExpressionCheckBoxGridData);
		createDirectionGroup();

		// Spacer label
		new Label(composite, SWT.NONE);

		// Line Count Label
		GridData lineCountLabelGridData = new GridData();
		lineCountLabelGridData.horizontalSpan = 2;
		lineCountLabelGridData.widthHint = 300;
		lineCountLabel = new Label(composite, SWT.NONE);

		// Get the trace count for the label and set it
		int traceCount = 0;
		if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader() != null) {
			traceCount = TraceViewerGlobals.getTraceViewer()
					.getDataReaderAccess().getCurrentDataReader()
					.getTraceCount();
		}
		lineCountLabel.setText(Messages.getString("SearchDialog.LineViewText") //$NON-NLS-1$
				+ traceCount);
		lineCountLabel.setLayoutData(lineCountLabelGridData);

		// ProgressBar
		GridData progressBarGridData = new GridData();
		progressBarGridData.heightHint = 22;
		progressBarGridData.grabExcessVerticalSpace = false;
		progressBarGridData.grabExcessHorizontalSpace = true;
		progressBarGridData.horizontalAlignment = SWT.FILL;
		progressBarGridData.horizontalSpan = 3;
		progressBar = new ProgressBar(composite, SWT.SMOOTH);
		progressBar.setLayoutData(progressBarGridData);
		updateSearchProgressBar(view.getShowingTracesFrom()
				+ widget.getLineAtOffset(widget.getCaretOffset()));

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.SEARCHING);
	}

	/**
	 * This method initializes findCombo
	 */
	private void createKeyListener() {
		searchDialogKeyListener = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				// ALT + d changes search direction
				if (e.stateMask == SWT.ALT
						&& (e.character == 'd' || e.character == 'D')) {
					upDirectionRadioButton.setSelection(!upDirectionRadioButton
							.getSelection());
					downDirectionRadioButton
							.setSelection(!downDirectionRadioButton
									.getSelection());

					// ALT + c changes "match case" value
				} else if (e.stateMask == SWT.ALT
						&& (e.character == 'c' || e.character == 'C')) {
					matchCaseCheckBox.setSelection(!matchCaseCheckBox
							.getSelection());

					// ALT + w changes "whole word" value
				} else if (e.stateMask == SWT.ALT
						&& (e.character == 'w' || e.character == 'W')) {
					matchWholeWordCheckBox.setSelection(!matchWholeWordCheckBox
							.getSelection());

					// Search again
				} else if (e.keyCode == SWT.F3) {
					findCombo.setText(getPreviousSearchString());
					searchString();
				}
			}
		};
	}

	/**
	 * This method initializes directionGroup
	 */
	private void createDirectionGroup() {

		// Direction Group
		GridData directionGroupGridData = new GridData();
		directionGroupGridData.horizontalSpan = 2;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		Group directionGroup = new Group(composite, SWT.NONE);
		directionGroup.setText(Messages
				.getString("SearchDialog.DirectionGroupName")); //$NON-NLS-1$
		directionGroup.setLayoutData(directionGroupGridData);
		directionGroup.setLayout(gridLayout1);
		directionGroup.setText(Messages
				.getString("SearchDialog.DirectionGroupName")); //$NON-NLS-1$
		directionGroup.setToolTipText(Messages
				.getString("SearchDialog.DirectionToolTip")); //$NON-NLS-1$

		// Up direction Button
		upDirectionRadioButton = new Button(directionGroup, SWT.RADIO);
		upDirectionRadioButton.setText(Messages
				.getString("SearchDialog.UpDirectionText")); //$NON-NLS-1$
		upDirectionRadioButton.setToolTipText(Messages
				.getString("SearchDialog.UpDirectionToolTip")); //$NON-NLS-1$

		// Down direction Button
		downDirectionRadioButton = new Button(directionGroup, SWT.RADIO);
		downDirectionRadioButton.setText(Messages
				.getString("SearchDialog.DownDirectionText")); //$NON-NLS-1$
		downDirectionRadioButton.setToolTipText(Messages
				.getString("SearchDialog.DownDirectionToolTip")); //$NON-NLS-1$
		downDirectionRadioButton.setSelection(true);

		// Add key listeners
		downDirectionRadioButton.addKeyListener(searchDialogKeyListener);
		upDirectionRadioButton.addKeyListener(searchDialogKeyListener);
	}

	/**
	 * Starts searching with given string
	 * 
	 * @param searchString
	 *            search string
	 */
	public void startSearch(String searchString) {
		if (TraceViewerGlobals.getTraceViewer().getStateHolder().getState() != StateHolder.State.SEARCHING) {
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SEARCHING);

			// Has to be before search button is disabled because will
			// enable it in modify listener
			insertTextToComboBox(searchString);

			searchButton.setEnabled(false);
			stopSearchButton.setEnabled(true);

			searchString();
		}

		// Save settings
		saveSettings();
	}

	/**
	 * Inserts text to combo box if necessary
	 * 
	 * @param newString
	 *            new string to add to combo box
	 */
	private void insertTextToComboBox(String newString) {
		// Insert new items to combo

		int nIndex = findCombo.indexOf(newString);
		if (nIndex > -1) {
			findCombo.remove(nIndex);
		}
		findCombo.setText(newString);
		findCombo.add(newString, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
		// Add selection listener to search button
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("SearchButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				if (widget.getLineCount() > 1) {
					startSearch(findCombo.getText());
				}
				TraceViewerGlobals.postUiEvent("SearchButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to stop find button
		stopSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("StopSearchButton", "1"); //$NON-NLS-1$ //$NON-NLS-2$
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getSearchProcessor().stopSearch(true);
				searchButton.setEnabled(true);
				stopSearchButton.setEnabled(false);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				TraceViewerGlobals.postUiEvent("StopSearchButton", "0"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// Add selection listener to match whole word checkbox
		matchWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("MatchWholeWordCheckBox" //$NON-NLS-1$
						+ matchWholeWordCheckBox.getSelection(), "1"); //$NON-NLS-1$
				if (matchWholeWordCheckBox.getSelection()) {
					regularExpressionCheckBox.setEnabled(false);
				} else {
					regularExpressionCheckBox.setEnabled(true);
				}
				TraceViewerGlobals.postUiEvent("MatchWholeWordCheckBox" //$NON-NLS-1$
						+ matchWholeWordCheckBox.getSelection(), "0"); //$NON-NLS-1$
			}
		});

		// Add selection listener to regular expression checkbox
		regularExpressionCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("RegularExpressionCheckBox" //$NON-NLS-1$
						+ regularExpressionCheckBox.getSelection(), "1"); //$NON-NLS-1$
				if (regularExpressionCheckBox.getSelection()) {
					matchWholeWordCheckBox.setEnabled(false);
				} else {
					matchWholeWordCheckBox.setEnabled(true);
				}
				TraceViewerGlobals.postUiEvent("RegularExpressionCheckBox" //$NON-NLS-1$
						+ regularExpressionCheckBox.getSelection(), "0"); //$NON-NLS-1$
			}
		});

		// Add selection listener to match case checkbox
		matchCaseCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TraceViewerGlobals.postUiEvent("MatchCaseCheckBox" //$NON-NLS-1$
						+ matchCaseCheckBox.getSelection(), "1"); //$NON-NLS-1$
				TraceViewerGlobals.postUiEvent("MatchCaseCheckBox" //$NON-NLS-1$
						+ matchCaseCheckBox.getSelection(), "0"); //$NON-NLS-1$
			}
		});

		// Add modify listener to find combo box
		findCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (findCombo.getText().length() > 0) {
					searchButton.setEnabled(true);
				} else {
					searchButton.setEnabled(false);
				}
			}

		});
	}

	/**
	 * Gets progressBar
	 * 
	 * @return progressBar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Sets search properties
	 */
	private void setVariablesToProperties() {

		// Get variable statuses from the ui components
		searchProperties.setSearchString(findCombo.getText());
		searchProperties.setSearchingForward(!upDirectionRadioButton
				.getSelection());
		searchProperties.setCaseSensitive(matchCaseCheckBox.getSelection());
		searchProperties.setWholeWord(matchWholeWordCheckBox.getSelection());
		searchProperties.setRegExp(regularExpressionCheckBox.getSelection());
		searchProperties.setOriginalSearchStartLine(TraceViewerGlobals
				.getTraceViewer().getView().getShowingTracesFrom()
				+ widget.getLineAtOffset(widget.getCaretOffset()));
	}

	/**
	 * Searchs the occurrence of the String value and shows it in view
	 */
	private void searchString() {

		// Set search variables
		setVariablesToProperties();

		// Check if this is a timestamp range search, jump straight to
		// SearchProcessor
		if (SearchUtils.containsTimestampQuery(searchProperties
				.getSearchString())
				&& SearchUtils.containsTimestampRangeQuery(searchProperties
						.getSearchString())) {

			// Do a timestamp range search
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor().doTimestampRangeSearch(
							searchProperties.getSearchString());

			// Other searchs
		} else {

			try {

				// Get the search start offset
				int cursorOffset = getCursorOffset(searchProperties
						.isSearchingForward());

				// Find the trace and get the offset of it
				int offset = findTrace(cursorOffset);

				// String found
				if (offset != SearchUtils.NOT_FOUND) {
					processStringFound(offset);

					// String was not found
				} else {

					// Start the search after or before this view
					if (searchProperties.isSearchingForward()) {
						processSearchingForward();
					} else {
						processSearchingBackward();
					}

				}

			} catch (BadLocationException e) {
				e.printStackTrace();
				enableSearchButton();
			} catch (PatternSyntaxException e) {
				e.printStackTrace();
				enableSearchButton();
			}
		}
	}

	/**
	 * Finds the trace using search properties
	 * 
	 * @param searchStartOffset
	 *            start offset where to start the search
	 * @return offset of the trace found or -1 if not found
	 * @throws BadLocationException
	 */
	private int findTrace(int searchStartOffset) throws BadLocationException {
		int offset = SearchUtils.NOT_FOUND;

		// Search using component, group and trace ID's
		if (SearchUtils.containsIdQuery(searchProperties.getSearchString())) {
			offset = findTraceIDSearch(searchStartOffset);

			// Timestamp search
		} else if (SearchUtils.containsTimestampQuery(searchProperties
				.getSearchString())) {
			offset = findTraceTimestampSearch(searchStartOffset);

			// Create a normal text search
		} else {
			IRegion region = finder.find(searchStartOffset, searchProperties
					.getSearchString(), searchProperties.isSearchingForward(),
					searchProperties.isCaseSensitive(), searchProperties
							.isWholeWord(), searchProperties.isRegExp());

			if (region != null) {
				offset = region.getOffset();
			}
		}

		return offset;
	}

	/**
	 * Finds the trace using ID search
	 * 
	 * @param searchStartOffset
	 *            search start offset
	 * @return found offset
	 * @throws BadLocationException
	 */
	private int findTraceIDSearch(int searchStartOffset)
			throws BadLocationException {
		int offset;

		// Parse ID's from the search string
		TraceInformation inf = SearchUtils.parseIDsFromString(searchProperties
				.getSearchString());

		int startTrace = document.getLineOfOffset(searchStartOffset);

		// Check which way we are searching and modify the start trace
		// accordingly
		boolean forward = searchProperties.isSearchingForward();
		if (forward) {
			startTrace++;
		} else {
			startTrace--;
		}

		List<TraceProperties> traces = null;
		List<TraceInformation> informations = null;

		// Get properties from the traces in the view. -2 comes from empty
		// line in the end of widget and because traces start from offset 0,
		// not 1
		traces = TraceViewerGlobals.getTraceViewer().getTraces(
				view.getShowingTracesFrom(),
				view.getShowingTracesFrom() + widget.getLineCount() - 2);

		// Get the informations array
		if (traces != null) {
			informations = new ArrayList<TraceInformation>(traces.size());
			for (int i = 0; i < traces.size(); i++) {
				informations.add(traces.get(i).information);
			}
		}

		// Get the trace number that matches the ID's
		int foundTrace = SearchUtils.findTraceOffsetFromInformations(
				startTrace, inf, informations, forward);

		if (foundTrace == SearchUtils.NOT_FOUND) {
			offset = SearchUtils.NOT_FOUND;
		} else {
			offset = document.getLineOffset(foundTrace);
		}
		return offset;
	}

	/**
	 * Finds the trace using timestamp search
	 * 
	 * @param searchStartOffset
	 *            search start offset
	 * @return found offset
	 * @throws BadLocationException
	 */
	private int findTraceTimestampSearch(int searchStartOffset)
			throws BadLocationException {
		int offset;

		// Parse timestamp from the search string
		String timestamp = SearchUtils.parseTimestampFromString(
				searchProperties.getSearchString(), true);

		List<TraceProperties> traces = null;
		List<String> timestamps = null;

		// Get properties from the traces in the view. -2 comes from empty
		// line in the end of widget and because traces start from offset 0,
		// not 1
		traces = TraceViewerGlobals.getTraceViewer().getTraces(
				view.getShowingTracesFrom(),
				view.getShowingTracesFrom() + widget.getLineCount() - 2);

		int startTrace = document.getLineOfOffset(searchStartOffset);
		String currentTraceTimestamp = null;

		// Get the informations array
		if (traces != null) {
			TimestampParser parser = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getTimestampParser();
			timestamps = new ArrayList<String>(traces.size());
			for (int i = 0; i < traces.size(); i++) {
				TraceProperties trace = traces.get(i);
				parser.processData(trace);
				timestamps.add(trace.timestampString);
			}

			if (timestamps.size() >= startTrace) {
				currentTraceTimestamp = timestamps.get(startTrace);
			}
		}

		boolean forward = true;

		// Check which way we are searching and modify the start trace
		// accordingly
		if (currentTraceTimestamp != null) {
			if (currentTraceTimestamp.compareTo(timestamp) <= 0) {
				startTrace++;
			} else {
				startTrace--;
				forward = false;
			}
		} else {
			startTrace++;
		}
		upDirectionRadioButton.setSelection(!forward);
		downDirectionRadioButton.setSelection(forward);
		searchProperties.setSearchingForward(forward);

		// Get the trace number that matches the timestamp
		int foundTrace = SearchUtils.findTraceOffsetFromTimestamps(startTrace,
				timestamp, timestamps, forward);

		if (foundTrace == SearchUtils.NOT_FOUND) {
			offset = SearchUtils.NOT_FOUND;
		} else {
			offset = document.getLineOffset(foundTrace);
		}
		return offset;
	}

	/**
	 * Processes searching forward
	 */
	private void processSearchingForward() {
		// There is traces below this block
		int traceCount = TraceViewerGlobals.getTraceViewer()
				.getDataReaderAccess().getCurrentDataReader().getTraceCount();
		if (view.getShowingTracesFrom() + widget.getLineCount() < traceCount) {

			searchProperties.setCurrentSearchStartLine(view
					.getShowingTracesFrom()
					+ widget.getLineCount()
					+ (TraceViewerGlobals.blockSize / 2));

			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor()
					.processSearch(searchProperties, false);
		} else { // End Of File
			// Say EOF to searchProcessor
			updateSearchProgressBar(traceCount);
			searchHitEOF();
		}
	}

	/**
	 * Processes searching backwards
	 */
	private void processSearchingBackward() {
		// There is traces above this block
		if (view.getShowingTracesFrom() > 0) {
			searchProperties.setCurrentSearchStartLine(view
					.getShowingTracesFrom()
					- (TraceViewerGlobals.blockSize / 2));
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor()
					.processSearch(searchProperties, false);
		} else { // End (beginning) Of File
			updateSearchProgressBar(0);
			searchHitEOF();
		}
	}

	/**
	 * Processes situation where result is found
	 * 
	 * @param offset
	 *            offset of result
	 */
	private void processStringFound(int offset) {
		int line = widget.getLineAtOffset(offset);

		// Update progressBar and linecount label
		updateSearchProgressBar(view.getShowingTracesFrom() + line);

		// Set search result line to the view
		TraceViewerGlobals.getTraceViewer().getView().highlightLines(
				view.getShowingTracesFrom() + line, 0, false);
	}

	/**
	 * Gets cursor offset
	 * 
	 * @param searchingForward
	 *            tells which way the search is going
	 * @return cursor offset
	 * @throws BadLocationException
	 */
	private int getCursorOffset(boolean searchingForward)
			throws BadLocationException {
		int cursorOffset;

		if (searchingForward) {
			cursorOffset = widget.getCaretOffset();
		} else {
			int lineNumber = widget.getLineAtOffset(widget.getCaretOffset());
			int lineLength = document.getLineLength(lineNumber);
			cursorOffset = widget.getCaretOffset() - lineLength + 1;
		}
		// To make sure cursor offset isn't anything stupid
		if (cursorOffset < 0) {
			cursorOffset = 0;
		} else if (cursorOffset >= widget.getCharCount()) {
			cursorOffset = widget.getCharCount() - 1;
		}
		return cursorOffset;
	}

	/**
	 * Invokes search from EOF. Is called from view in EOF
	 */
	public void endOfFile() {
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getSearchProcessor().setHitEOFAlready(true);

		setVariablesToProperties();

		if (searchProperties.isSearchingForward()) { // Down
			// Start new search from beginning of the file
			searchProperties.setCurrentSearchStartLine(0);
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor()
					.processSearch(searchProperties, false);
		} else { // Searching up
			searchProperties.setCurrentSearchStartLine(TraceViewerGlobals
					.getTraceViewer().getDataReaderAccess()
					.getCurrentDataReader().getTraceCount());
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getSearchProcessor()
					.processSearch(searchProperties, false);
		}
	}

	/**
	 * Enables search button
	 */
	public void enableSearchButton() {
		display.asyncExec(new Runnable() {

			public void run() {
				if (getShell() != null && !getShell().isDisposed()) {

					// Enable find button
					if (!findCombo.getText().equals(SearchUtils.EMPTY)) {
						searchButton.setEnabled(true);
					}

					// Disable stop button
					stopSearchButton.setEnabled(false);

					// Set state back to normal
					TraceViewerGlobals.getTraceViewer().getStateHolder()
							.setState(StateHolder.State.NORMAL);

					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getSearchProcessor()
							.setHitEOFAlready(false);

					getShell().setFocus();
				}
			}

		});
	}

	/**
	 * Sets search text to dialog. Must be called from UI thread!
	 * 
	 * @param findStr
	 *            search text
	 */
	public void setSearchText(String findStr) {
		findCombo.setText(findStr);
	}

	/**
	 * Updates search progressBar
	 * 
	 * @param selectionPoint
	 *            selection point where to update
	 */
	public void updateSearchProgressBar(int selectionPoint) {
		display.asyncExec(new SearchDialogProgressBarUpdater(selectionPoint,
				lineCountLabel));
	}

	/**
	 * Search hit full round
	 */
	public void searchHitFullRound() {
		display.asyncExec(new SearchHitFullRoundUpdater());
	}

	/**
	 * Search hit EOF
	 */
	public void searchHitEOF() {
		display.asyncExec(new SearchHitEOFUpdater());
	}

	/**
	 * Tells is the search dialog open
	 * 
	 * @return true if dialog is open
	 */
	public boolean isOpen() {
		boolean isOpen = false;
		if (getShell() != null && !getShell().isDisposed()) {
			isOpen = true;
		}
		return isOpen;
	}

	/**
	 * Tells is the search dialog visible
	 * 
	 * @return true if dialog is visible
	 */
	public boolean isVisible() {
		boolean isVisible = false;
		if (isOpen() && getShell().isVisible()) {
			isVisible = true;
		}
		return isVisible;
	}

	/**
	 * Sets focus
	 */
	public void setFocus() {
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.setFocus();
		}
	}

	/**
	 * Check if dialog is open. If yes, close it
	 * 
	 * @return true if dialog was open and was now closed
	 */
	public boolean forceClose() {
		boolean closed = false;
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.close();
			closed = true;
		}
		return closed;
	}

	/**
	 * Gets previous search string
	 * 
	 * @return previous search string or empty String if doesn't exist
	 */
	public String getPreviousSearchString() {
		String previous = ""; //$NON-NLS-1$

		if (comboList != null && comboList.length > 0) {
			previous = comboList[0];
		}
		return previous;
	}
}
