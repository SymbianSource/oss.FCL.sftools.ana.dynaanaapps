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
 * Trace Viewer view implementation
 *
 */
package com.nokia.traceviewer.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;
import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.action.ActionFactory;
import com.nokia.traceviewer.action.AddCommentToTraceAction;
import com.nokia.traceviewer.action.EditTraceCommentAction;
import com.nokia.traceviewer.action.OpenTraceLocationAction;
import com.nokia.traceviewer.action.RemoveTraceCommentAction;
import com.nokia.traceviewer.action.ShowTraceInfoAction;
import com.nokia.traceviewer.action.TraceViewerActionUtils;
import com.nokia.traceviewer.dialog.ProgressBarDialog;
import com.nokia.traceviewer.dialog.SearchDialog;
import com.nokia.traceviewer.engine.StateHolder;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.TraceViewerDialog;
import com.nokia.traceviewer.engine.TraceViewerDialogInterface;
import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.TraceViewerTraceViewInterface;
import com.nokia.traceviewer.engine.TraceViewerUtils;
import com.nokia.traceviewer.engine.dataprocessor.DataProcessor;
import com.nokia.traceviewer.engine.preferences.PreferenceConstants;
import com.nokia.traceviewer.engine.preferences.TraceViewerAdvancedPreferencesPage;
import com.nokia.traceviewer.engine.preferences.TraceViewerConnectionPreferencesPage;
import com.nokia.traceviewer.engine.preferences.TraceViewerPreferencesPage;
import com.nokia.traceviewer.view.listener.ScrollerSelectionListener;
import com.nokia.traceviewer.view.listener.SelectionProperties;
import com.nokia.traceviewer.view.listener.ViewerClickListener;
import com.nokia.traceviewer.view.listener.ViewerKeyListener;
import com.nokia.traceviewer.view.listener.ViewerMouseWheelListener;
import com.nokia.traceviewer.view.listener.ViewerSelectionChangedListener;
import com.nokia.traceviewer.view.listener.WindowResizeListener;

/**
 * Trace Viewer view implementation
 */
public final class TraceViewerView extends ViewPart implements
		TraceViewerTraceViewInterface, TraceViewerDialogInterface,
		DataProcessor, Runnable {

	/**
	 * Title of the view
	 */
	private static final String TRACE_VIEWER_TITLE = Messages
			.getString("TraceViewerView.ViewTitle"); //$NON-NLS-1$

	/**
	 * Hex prefix
	 */
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	/**
	 * Max lines in the view
	 */
	private static final int MAX_LINES = TraceViewerGlobals.blockSize * 2;

	/**
	 * Update interval in milliseconds
	 */
	private static final int UPDATE_INTERVAL = 100;

	/**
	 * Normal view name
	 */
	private static final String VIEWNAME_NORMAL = TRACE_VIEWER_TITLE;

	/**
	 * Filtered view name
	 */
	private static final String VIEWNAME_FILTERED = Messages
			.getString("TraceViewerView.FilteredText"); //$NON-NLS-1$

	/**
	 * Paused view name
	 */
	private static final String VIEWNAME_PAUSED = Messages
			.getString("TraceViewerView.PausedText"); //$NON-NLS-1$

	/**
	 * View name when storing data while triggering
	 */
	private static final String VIEWNAME_STORING = Messages
			.getString("TraceViewerView.StoringText"); //$NON-NLS-1$

	/**
	 * View name when trigger is armed
	 */
	private static final String VIEWNAME_ARMED = Messages
			.getString("TraceViewerView.ArmedText"); //$NON-NLS-1$

	/**
	 * Opened Log file name separator shown before the log file name
	 */
	private static final String LOG_FILE_NAME_SEPARATOR = " - "; //$NON-NLS-1$

	/**
	 * Action factory
	 */
	private ActionFactory actionFactory;

	/**
	 * Text viewer
	 */
	private TextViewer viewer;

	/**
	 * StyledText widget
	 */
	private StyledText widget;

	/**
	 * String array to append to widget in next update
	 */
	private final StringBuffer newData;

	/**
	 * String array to append to widget in next update
	 */
	private final StringBuffer scrolledData;

	/**
	 * Timestamp of next update
	 */
	private long updateNextTime;

	/**
	 * Slider object
	 */
	private Slider slider;

	/**
	 * Tells from which count Viewer is showing traces from
	 */
	private int showingTracesFrom;

	/**
	 * Start line to highlight
	 */
	private int startHighlightLine;

	/**
	 * End line to highlight
	 */
	private int endHighlightLine;

	/**
	 * Indicates that view name should be updated
	 */
	private boolean updateViewName;

	/**
	 * Contains number of new traces in String buffer
	 */
	private int numberOfNewTraces;

	/**
	 * Contains offsets of new traces in newTraces String buffer
	 */
	private final List<Integer> offsetsOfNewTraces;

	/**
	 * Dialog factory
	 */
	private final DialogFactory dialogFactory;

	/**
	 * Indicates that view update is on
	 */
	private boolean viewUpdateOn = true;

	/**
	 * Inserting last block when scrolling the view
	 */
	private boolean insertingLastBlock;

	/**
	 * The constructor.
	 */
	public TraceViewerView() {
		dialogFactory = new DialogFactory();

		// Create StringBuffers
		newData = new StringBuffer();
		scrolledData = new StringBuffer();

		// Offsets of new traces are also saved
		offsetsOfNewTraces = new ArrayList<Integer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		actionFactory = new ActionFactory();
		// Create layout for the shell
		createLayout(parent);

		// Creates the text viewer and set a document to it
		viewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		Document document = new Document();
		viewer.setDocument(document);
		viewer.setEditable(false);

		// Get styled text widget and configure it
		widget = viewer.getTextWidget();
		setFontSize(FONT_SIZE);
		widget.getVerticalBar().setVisible(false);
		widget.getHorizontalBar().setVisible(true);
		widget
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
						1));

		// Create a slider
		slider = new Slider(parent, SWT.VERTICAL);
		slider.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true, 1,
				1));
		slider.setEnabled(false);

		fillMenuAndToolBar();
		createContextMenu();

		// Create listeners
		createListeners();

		// Sets this view to be the dialog provider
		TraceViewerGlobals.getTraceViewer().setDialogs(this);

		// Sets the view reference to trace viewer engine
		TraceViewerGlobals.getTraceViewer().setTraceView(this);

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				TraceViewerHelpContextIDs.MAIN_VIEW);

		// Set global action handler to own copy selection action
		getViewSite().getActionBars().setGlobalActionHandler(
				org.eclipse.ui.actions.ActionFactory.COPY.getId(),
				actionFactory.getCopySelectionAction());

		// Set global action handler to own seach action
		getViewSite().getActionBars().setGlobalActionHandler(
				org.eclipse.ui.actions.ActionFactory.FIND.getId(),
				actionFactory.getSearchAction());

		// Set global action handler to own select all action
		getViewSite().getActionBars().setGlobalActionHandler(
				org.eclipse.ui.actions.ActionFactory.SELECT_ALL.getId(),
				actionFactory.getSelectAllAction());

	}

	/**
	 * Adds action items to menu and toolbar
	 */
	private void fillMenuAndToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		actionFactory.fillMenu(bars.getMenuManager());
		actionFactory.fillToolBar(bars.getToolBarManager());
	}

	/**
	 * Returns action factory
	 * 
	 * @return action factory
	 */
	public ActionFactory getActionFactory() {
		return actionFactory;
	}

	/**
	 * Create layout for the view
	 * 
	 * @param parent
	 *            parent shell
	 */
	private void createLayout(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		parent.setLayout(layout);
	}

	/**
	 * Create all listeners
	 */
	private void createListeners() {
		// Create scroller selection listener
		ScrollerSelectionListener slistener = new ScrollerSelectionListener(
				viewer);
		slider.addSelectionListener(slistener);

		// Viewer selection changed listener
		ViewerSelectionChangedListener vlistener = new ViewerSelectionChangedListener(
				slider);
		viewer.addSelectionChangedListener(vlistener);

		// Create viewer mouse wheel listener
		Listener mlistener = new ViewerMouseWheelListener(slider, viewer);
		widget.addListener(SWT.MouseWheel, mlistener);

		// Create viewer key listener
		ViewerKeyListener klistener = new ViewerKeyListener(slider, viewer,
				this);
		widget.addKeyListener(klistener);

		// Create window resize listener
		WindowResizeListener rlistener = new WindowResizeListener(slider,
				viewer);
		widget.addControlListener(rlistener);

		// Create viewer click listener
		ViewerClickListener clistener = new ViewerClickListener(viewer);
		widget.addMouseListener(clistener);
	}

	/**
	 * Creates context menu
	 */
	private void createContextMenu() {
		// Create menu manager.
		final MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(menuMgr);
			}
		});

		// Create menu.
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Fills context menu
	 * 
	 * @param menuMgr
	 *            menu manager
	 */
	public void fillContextMenu(MenuManager menuMgr) {
		menuMgr.add(actionFactory.getShowTraceInfoAction());
		menuMgr.add(actionFactory.getOpenTraceLocationAction());
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// Nothing is selected
		if (widget.getSelectionCount() == 0) {
			actionFactory.getShowTraceInfoAction().setEnabled(false);
			actionFactory.getOpenTraceLocationAction().setEnabled(false);

			// More than one trace selected
		} else if (widget.getSelectionText().contains("\n")) { //$NON-NLS-1$
			actionFactory.getShowTraceInfoAction().setEnabled(false);
			actionFactory.getOpenTraceLocationAction().setEnabled(false);

			// One trace is selected
		} else {
			int lineNumber = showingTracesFrom
					+ widget.getLineAtOffset(widget.getCaretOffset());

			// Get the trace from the file
			List<TraceProperties> traceList = TraceViewerGlobals
					.getTraceViewer().getTraces(lineNumber, lineNumber);

			TraceProperties trace = null;
			if (traceList != null && !traceList.isEmpty()) {
				trace = traceList.get(0);
			}

			ShowTraceInfoAction infoAction = (ShowTraceInfoAction) actionFactory
					.getShowTraceInfoAction();
			infoAction.setEnabled(true);
			infoAction.setTrace(trace);

			OpenTraceLocationAction openLocationAction = (OpenTraceLocationAction) actionFactory
					.getOpenTraceLocationAction();

			// If traceInformation is found, enable open trace location action
			if (trace != null && trace.information != null) {
				TraceMetaData metaData = TraceViewerGlobals.getDecodeProvider()
						.getTraceMetaData(trace.information);
				if (metaData != null && metaData.getPath() != null) {
					openLocationAction.setEnabled(true);
					openLocationAction.setMetaData(metaData, true);
				} else {
					openLocationAction.setEnabled(false);
				}
			} else {
				openLocationAction.setEnabled(false);
			}

			menuMgr.add(new Separator());

			// Add / Edit / Remove trace comment actions
			if (trace != null) {
				TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
						.getTraceCommentHandler().processData(trace);
				if (trace.traceComment == null) {
					menuMgr.add(new AddCommentToTraceAction(trace.traceNumber));
				} else {
					menuMgr.add(new EditTraceCommentAction(trace.traceNumber));
					menuMgr
							.add(new RemoveTraceCommentAction(trace.traceNumber));
				}
			}

		}
		// Separator
		menuMgr.add(new Separator());

		// Copy action
		Action copyAction = actionFactory.getCopySelectionAction();
		menuMgr.add(copyAction);
		if (widget.getSelectionCount() > 0) {
			copyAction.setEnabled(true);
		} else {
			copyAction.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (!widget.isDisposed()) {

			// Set redraw off
			widget.setRedraw(false);

			if (TraceViewerGlobals.getTraceViewer().getStateHolder().getState() == StateHolder.State.SEARCHED) {
				slider.setSelection(startHighlightLine);
				scrollViewToLine(slider.getSelection());
			} else if (newData.length() > 0) {
				runNormalUpdate();
			}

			colorTraces();

			// If view name should be updated
			if (updateViewName) {
				generateAndSetViewName();
			}
			// Redraw
			widget.setRedraw(true);
			widget.update();
		}
	}

	/**
	 * Normal update sequence
	 */
	private void runNormalUpdate() {
		// Max offset is number of traces minus 1
		int max = TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getCurrentDataReader().getTraceCount() - 1;

		// If previous data is not from the end, get latest blocks from file
		if (showingTracesFrom + widget.getLineCount() + numberOfNewTraces < max + 1) {
			slider.setMaximum(max);
			slider.setSelection(max);
			scrollViewToLine(max);
			setSliderProperties(max);
		} else {

			// If there is lot of new traces, cut down the StringBuffer already
			// in here to avoid massive insertion to text widget
			if (numberOfNewTraces > MAX_LINES + TraceViewerGlobals.blockSize) {
				int offsetOfDeletion = offsetsOfNewTraces.size() - 1
						- (MAX_LINES / TraceViewerGlobals.blockSize);

				if (offsetOfDeletion >= 0
						&& offsetOfDeletion < offsetsOfNewTraces.size()) {
					newData.replace(0, offsetsOfNewTraces.get(offsetOfDeletion)
							.intValue(), ""); //$NON-NLS-1$
				}
			}

			// Append the new data to current
			widget.append(newData.toString());

			// Set slider properties
			setSliderProperties(max);

			// If there is over MAX_LINES lines, remove extra data blocks
			if (widget.getLineCount() > MAX_LINES) {
				removeDataBlocks(max);
			} // no else
		}

		// Clear variables and selection
		newData.setLength(0);
		numberOfNewTraces = 0;
		offsetsOfNewTraces.clear();
		SelectionProperties.clear();

		// Set the top index and caret to the end of the document
		viewer.setTopIndex(max - slider.getThumb());
		widget.setCaretOffset(widget.getCharCount());
	}

	/**
	 * Removes extra data blocks
	 * 
	 * @param max
	 *            number of lines alltogether
	 */
	private void removeDataBlocks(int max) {
		// Get amount of full blocks
		int fullBlocks = widget.getLineCount() / TraceViewerGlobals.blockSize;

		// Showing traces is starting point of total number of blocks -
		// 1
		showingTracesFrom = (((max + 1) / TraceViewerGlobals.blockSize) - 1)
				* TraceViewerGlobals.blockSize;

		// Delete data from beginning of the widget
		int line = ((fullBlocks - 1) * TraceViewerGlobals.blockSize);
		int replaceTo = widget.getOffsetAtLine(line);
		widget.replaceTextRange(0, replaceTo, ""); //$NON-NLS-1$
	}

	/**
	 * Sets slider properties
	 * 
	 * @param max
	 *            max value
	 */
	private void setSliderProperties(int max) {
		// Set Slider properties
		slider.setEnabled(max > 0);
		slider.setMaximum(max);
		slider.setSelection(max);
		slider.setThumb(getNumberOfLinesInView());
	}

	/**
	 * Color traces
	 */
	private void colorTraces() {
		// If coloring, color lines
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getColorer().isColoring()) {
			TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getColorer().createColorRules();
		}
	}

	/**
	 * Adds traces before current data
	 * 
	 * @param traceToBeShown
	 *            trace to be shown
	 * @param numberOfBlocks
	 *            number of block
	 */
	private void insertBeforeCurrentView(int numberOfBlocks, int traceToBeShown) {

		widget.setRedraw(false);
		// Save the caret position
		int caretPos = widget.getLineAtOffset(widget.getCaretOffset());

		// Save the old data
		String oldData = getOldData(true, numberOfBlocks);

		// Append the old data after the new data
		if (!oldData.equals("")) { //$NON-NLS-1$
			scrolledData.append(oldData);
		}

		// Set the new data to widget
		widget.setText(scrolledData.toString());

		// Update showing traces variable
		showingTracesFrom = (traceToBeShown / TraceViewerGlobals.blockSize)
				* TraceViewerGlobals.blockSize;

		// Set view properties
		updateViewPropertiesWhenInsertingBeforeCurrentData(caretPos,
				traceToBeShown);

		// Empty buffer
		scrolledData.setLength(0);
		colorTraces();
		widget.setRedraw(true);
		widget.update();
	}

	/**
	 * Add traces after current data
	 * 
	 * @param traceToBeShown
	 *            trace to be shown
	 * @param numberOfBlocks
	 *            number of block
	 */
	private void insertAfterCurrentView(int numberOfBlocks, int traceToBeShown) {

		widget.setRedraw(false);
		// Save the old data
		String oldData = getOldData(false, numberOfBlocks);

		// Create new StringBuffer with old and new data in it
		StringBuffer data = new StringBuffer(oldData.length()
				+ scrolledData.length());
		data.append(oldData);
		data.append(scrolledData);

		// Set the new data
		widget.setText(data.toString());

		// Update showingTracesFrom variable
		int fullBlocksInView = (traceToBeShown + getNumberOfLinesInView() + 1)
				/ TraceViewerGlobals.blockSize;
		showingTracesFrom = (fullBlocksInView * TraceViewerGlobals.blockSize)
				- TraceViewerGlobals.blockSize;
		if (showingTracesFrom < 0) {
			showingTracesFrom = 0;
		} // no else

		// Set view properties
		updateViewPropertiesWhenInsertingAfterCurrentData(traceToBeShown);

		// Empty buffer
		scrolledData.setLength(0);
		colorTraces();
		widget.setRedraw(true);
		widget.update();
	}

	/**
	 * Gets old data
	 * 
	 * @param beforeCurrentData
	 *            getting data to before current view
	 * @param numberOfBlocks
	 *            number of blocks to get
	 * @return old data
	 */
	private String getOldData(boolean beforeCurrentData, int numberOfBlocks) {
		String oldData = ""; //$NON-NLS-1$
		if (numberOfBlocks == 1) {
			// More thatn 1 block of data, needs to be cut
			if (widget.getLineCount() > TraceViewerGlobals.blockSize) {

				// Inserting before current view
				if (beforeCurrentData) {
					oldData = widget.getText(0, widget
							.getOffsetAtLine(TraceViewerGlobals.blockSize) - 1);

					// Inserting after current view
				} else if (!beforeCurrentData) {
					int offsetAtBlockStart = widget
							.getOffsetAtLine(TraceViewerGlobals.blockSize);
					oldData = widget.getTextRange(offsetAtBlockStart, widget
							.getOffsetAtLine(widget.getLineCount() - 1)
							- offsetAtBlockStart);
				}
			} else {
				oldData = widget.getText();
			}
		}

		return oldData;
	}

	/**
	 * Sets view properties when inserting before current data
	 * 
	 * @param caretPos
	 *            old caret position
	 * @param traceToBeShown
	 *            trace to be shown
	 */
	private void updateViewPropertiesWhenInsertingBeforeCurrentData(
			int caretPos, int traceToBeShown) {
		StateHolder.State state = TraceViewerGlobals.getTraceViewer()
				.getStateHolder().getState();
		// Null check
		if (state == null) {
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SCROLLING_WITH_SCROLLBAR);
			state = StateHolder.State.SCROLLING_WITH_SCROLLBAR;
		}
		switch (state) {
		// Set top index when scrolling with scrollbar
		case SCROLLING_WITH_SCROLLBAR:

			viewer.setTopIndex((traceToBeShown % TraceViewerGlobals.blockSize));
			break;
		// Set top index and caret offset when scrolling with arrow keys
		case SCROLLING_WITH_ARROWS:
			viewer.setTopIndex((traceToBeShown % TraceViewerGlobals.blockSize));
			if (caretPos + TraceViewerGlobals.blockSize - 1 < widget
					.getLineCount()) {
				widget.setCaretOffset(widget.getOffsetAtLine(caretPos
						+ TraceViewerGlobals.blockSize - 1));
			}
			break;
		// Set top index and selection when searching
		case SEARCHED:
			updateViewerPropertiesAfterSearch();
			break;

		// Set top index and caret offset when scrolling with page up/down
		case SCROLLING_WITH_PAGEUPDOWN:
			int topIndex = traceToBeShown % TraceViewerGlobals.blockSize - 2;
			viewer.setTopIndex(topIndex);
			int caretLine = caretPos + TraceViewerGlobals.blockSize - 1;
			if (caretLine > 0 && widget.getLineCount() > 0) {
				if (caretLine > widget.getLineCount()) {
					caretLine = widget.getLineCount() - 1;
				}
				widget.setCaretOffset(widget.getOffsetAtLine(caretLine));
			}

			break;
		default:
			break;
		}
		TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
				StateHolder.State.NORMAL);
		setSelection();
	}

	/**
	 * Sets view properties when inserting after current data
	 * 
	 * @param traceToBeShown
	 *            trace to be shown
	 */
	private void updateViewPropertiesWhenInsertingAfterCurrentData(
			int traceToBeShown) {
		StateHolder.State state = TraceViewerGlobals.getTraceViewer()
				.getStateHolder().getState();

		// Null check
		if (state == null) {
			TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
					StateHolder.State.SCROLLING_WITH_SCROLLBAR);
			state = StateHolder.State.SCROLLING_WITH_SCROLLBAR;
		}

		switch (state) {
		// Set top index when scrolling with scrollbar
		case SCROLLING_WITH_SCROLLBAR:
			viewer.setTopIndex(traceToBeShown - showingTracesFrom + 1);
			break;

		// Set top index and caret offset when scrolling with arrow keys
		case SCROLLING_WITH_ARROWS:
			viewer.setTopIndex(traceToBeShown - showingTracesFrom - 1);
			int line = traceToBeShown - showingTracesFrom
					+ getNumberOfLinesInView();
			if (line <= widget.getLineCount() && line >= 0) {
				widget.setCaretOffset(widget.getOffsetAtLine(line));
			}
			break;
		// Set top index and selection when searching
		case SEARCHED:
			updateViewerPropertiesAfterSearch();
			break;

		// Set top index and caret offset when scrolling with page up/down
		case SCROLLING_WITH_PAGEUPDOWN:
			viewer.setTopIndex(traceToBeShown - showingTracesFrom + 1);
			int topLine = traceToBeShown - showingTracesFrom
					+ getNumberOfLinesInView() - 1;
			if (topLine < widget.getLineCount()) {
				int caretOffset = widget.getOffsetAtLine(traceToBeShown
						- showingTracesFrom + getNumberOfLinesInView() - 1);
				if (caretOffset >= 0 && caretOffset < widget.getCharCount()) {
					widget.setCaretOffset(caretOffset);
				}
			} else {
				widget.setCaretOffset(widget.getCharCount() - 1);
			}
			break;
		// Unknown case, set basic top index
		case NORMAL:
			viewer.setTopIndex(traceToBeShown
					- TraceViewerGlobals.getTraceViewer().getView()
							.getShowingTracesFrom());
			break;
		default:
			break;
		}

		TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
				StateHolder.State.NORMAL);
		setSelection();
	}

	/**
	 * Sets selection to viewer
	 */
	public void setSelection() {
		if (SelectionProperties.lastClickedLine != -1) {

			// Calculate start offset
			int startOffset = 0;

			// If clicked from the totally first line
			if (SelectionProperties.firstClickedLine < showingTracesFrom) {
				startOffset = 0;

				// If clicked in the totally last line
			} else if (SelectionProperties.firstClickedLine > showingTracesFrom
					+ widget.getLineCount()) {
				startOffset = widget.getCharCount() - 1;

				// Clicked between the first and last line in the viewer
			} else {
				int lineNumber = SelectionProperties.firstClickedLine
						- showingTracesFrom;
				if (lineNumber < widget.getLineCount()) {
					startOffset = widget.getOffsetAtLine(lineNumber)
							+ SelectionProperties.firstClickedLineCaretOffset;
				}
			}

			// Calculate end offset
			int endOffset = 0;

			// If clicked from the totally first line
			if (SelectionProperties.lastClickedLine < showingTracesFrom) {
				endOffset = 0;

				// If clicked in the totally last line
			} else if (SelectionProperties.lastClickedLine >= showingTracesFrom
					+ widget.getLineCount()) {
				endOffset = widget.getCharCount() - 1;

				// Clicked between the first and last line in the viewer
			} else {
				int lineNumber = SelectionProperties.lastClickedLine
						- showingTracesFrom;
				if (lineNumber < widget.getLineCount()) {
					endOffset = widget.getOffsetAtLine(lineNumber)
							+ SelectionProperties.lastClickedLineCaretOffset;
				}
			}

			// Save current top index
			int topIndex = viewer.getTopIndex();

			// Set the selection to the viewer
			widget.setSelection(startOffset, endOffset);

			// Return old top index
			viewer.setTopIndex(topIndex);

			// Put caret to the end
		} else if (SelectionProperties.putCaretToTheEnd) {
			SelectionProperties.putCaretToTheEnd = false;
			widget.setCaretOffset(widget.getCharCount());
		}

		// Update trim
		ViewerSelectionChangedListener.handleTrimInformationUpdate();
	}

	/**
	 * Updates top index and selection after searching
	 */
	private void updateViewerPropertiesAfterSearch() {
		int selIndex = startHighlightLine - showingTracesFrom;
		int topIndex = selIndex - LINES_TO_LEAVE_BEFORE_FOUND_LINE;

		if (topIndex < 0) {
			topIndex = 0;
		}
		viewer.setTopIndex(topIndex);

		// Remove possible old selection values from the Selection Properties
		SelectionProperties.clear();

		// Select range of traces
		if (endHighlightLine != 0) {
			SelectionProperties.firstClickedLine = startHighlightLine;
			SelectionProperties.lastClickedLine = endHighlightLine + 1;

		} else {

			// Select one line
			if (topIndex < widget.getLineCount()
					&& (selIndex + 1 < widget.getLineCount())) {
				widget.setSelection(widget.getOffsetAtLine(selIndex), widget
						.getOffsetAtLine(selIndex + 1) - 1);
			}
		}

		// Enable search button from search dialog
		SearchDialog searchDialog = TraceViewerGlobals.getTraceViewer()
				.getDataProcessorAccess().getSearchProcessor()
				.getSearchDialog();
		if (searchDialog != null && searchDialog.isOpen()) {
			searchDialog.enableSearchButton();
		}
		TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
				StateHolder.State.NORMAL);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		TraceViewerGlobals.getTraceViewer().shutdown();
	}

	/**
	 * Gets shell
	 * 
	 * @return shell from the text viewer
	 */
	private Shell getShell() {
		Shell shell = null;
		if (viewer != null && viewer.getControl() != null
				&& viewer.getControl().getShell() != null
				&& !viewer.getControl().getShell().isDisposed()) {
			shell = viewer.getControl().getShell();
		}
		return shell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#getViewer()
	 */
	public TextViewer getViewer() {
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerTraceViewInterface#
	 * getShowingTracesFrom()
	 */
	public int getShowingTracesFrom() {
		return showingTracesFrom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DataProcessor#
	 * processData(com.nokia.traceviewer.engine.TraceProperties)
	 */
	public void processData(TraceProperties properties) {
		TraceViewerGlobals.debug("processData in View", //$NON-NLS-1$
				TraceViewerGlobals.DebugLevel.TEST);
		// Main if to check if trace is ok to show
		if (!properties.traceConfiguration.isFilteredOut()
				&& properties.traceConfiguration.isShowInView()
				&& !properties.traceConfiguration.isTriggeredOut()) {
			if (properties.traceConfiguration.isScrolledTrace()) {
				processScrolledTrace(properties);

				// Empty new data if we are reading last block
				if (insertingLastBlock && properties.lastTrace) {
					newData.setLength(0);
					insertingLastBlock = false;
				}
			} else if (viewUpdateOn) {
				processNormalTrace(properties);
			}
		}
	}

	/**
	 * Process normal trace
	 * 
	 * @param properties
	 *            trace properties
	 */
	private void processNormalTrace(TraceProperties properties) {
		numberOfNewTraces++;
		// Traces missing
		if (properties.bTraceInformation.isTraceMissing()) {
			newData.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
		}

		// Append timestamp to StringBuffer
		if (properties.timestampString != null) {
			newData.append(properties.timestampString);
			newData
					.append(TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getTimestampParser()
							.getTimeFromPreviousString(
									properties.timeFromPreviousTrace));
			newData.append('\t');
		}
		if (!properties.binaryTrace) {
			newData.append(properties.traceString);
			if (properties.traceComment != null) {
				newData.append(TraceViewerActionUtils.COMMENT_PREFIX);
				newData.append(properties.traceComment);
			}

			// Binary trace
		} else {
			addBinaryTraceToBuffer(properties, newData);
		}

		newData.append('\n');
		// Add offset of every new block to array so we know where to
		// cut the StringBuffer
		if (numberOfNewTraces % TraceViewerGlobals.blockSize == 1
				&& numberOfNewTraces != 1) {
			offsetsOfNewTraces.add(Integer.valueOf(newData.length()));
		}
		long time = System.currentTimeMillis();
		// If update interval has passed, call the view update
		if (time > updateNextTime) {
			updateNextTime = time + UPDATE_INTERVAL;
			update();
		}
	}

	/**
	 * Process scrolled trace
	 * 
	 * @param properties
	 *            trace properties
	 */
	private void processScrolledTrace(TraceProperties properties) {
		// Traces missing
		if (properties.bTraceInformation.isTraceMissing()) {
			scrolledData.append(TraceViewerActionUtils.TRACES_DROPPED_MSG);
		}

		// Append timestamp to StringBuffer
		if (properties.timestampString != null) {
			scrolledData.append(properties.timestampString);
			scrolledData
					.append(TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getTimestampParser()
							.getTimeFromPreviousString(
									properties.timeFromPreviousTrace));
			scrolledData.append('\t');
		}
		if (!properties.binaryTrace) {
			scrolledData.append(properties.traceString);

			if (properties.traceComment != null) {
				scrolledData.append(TraceViewerActionUtils.COMMENT_PREFIX);
				scrolledData.append(properties.traceComment);
			}

			// Binary trace
		} else {
			addBinaryTraceToBuffer(properties, scrolledData);
		}
		scrolledData.append('\n');
	}

	/**
	 * Adds binary traces to buffer
	 * 
	 * @param properties
	 *            trace properties
	 * @param dataBuf
	 *            data buffer to add the trace to
	 */
	private void addBinaryTraceToBuffer(TraceProperties properties,
			StringBuffer dataBuf) {

		// Get the selected value
		String type = TraceViewerPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.SHOW_UNDECODED_TRACES_TYPE);

		// Show binary trace info message
		if (type.equals(PreferenceConstants.UNDECODED_INFO_TEXT)) {
			String binMsg = Messages
					.getString("TraceViewerView.BinaryTraceInformationMessage"); //$NON-NLS-1$);
			dataBuf.append(binMsg);

			// Show binary traces as hex
		} else if (type.equals(PreferenceConstants.UNDECODED_HEX)) {

			// Read the message to a byte array
			String hexTrace = TraceViewerUtils.getTraceAsHexString(
					properties.byteBuffer, properties.messageStart,
					properties.messageLength, true);
			dataBuf.append(hexTrace);

			// Show binary trace as ID and data
		} else if (type.equals(PreferenceConstants.UNDECODED_ID_AND_DATA)) {
			dataBuf.append(Messages
					.getString("TraceViewerView.BinaryTraceText")); //$NON-NLS-1$
			dataBuf.append(Messages.getString("TraceViewerView.ComponentID")); //$NON-NLS-1$
			dataBuf.append(HEX_PREFIX);
			dataBuf.append(Integer.toHexString(properties.information
					.getComponentId()));
			dataBuf.append(Messages.getString("TraceViewerView.GroupID")); //$NON-NLS-1$
			dataBuf.append(HEX_PREFIX);
			dataBuf.append(Integer.toHexString(properties.information
					.getGroupId()));
			dataBuf.append(Messages.getString("TraceViewerView.TraceID")); //$NON-NLS-1$
			dataBuf.append(properties.information.getTraceId());
			dataBuf.append(Messages.getString("TraceViewerView.DataString")); //$NON-NLS-1$

			// Read the message to a byte array
			String hexTrace = TraceViewerUtils.getTraceAsHexString(
					properties.byteBuffer, properties.dataStart,
					properties.dataLength, true);
			dataBuf.append(hexTrace);

			if (properties.traceComment != null) {
				dataBuf.append(TraceViewerActionUtils.COMMENT_PREFIX);
				dataBuf.append(properties.traceComment);
			}
		}
	}

	/**
	 * Returns number of lines in view
	 * 
	 * @return number of lines in view
	 */
	public int getNumberOfLinesInView() {
		return viewer.getBottomIndex() - viewer.getTopIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#clearAll()
	 */
	public void clearAll() {
		// Sync with UI thread
		if (viewer != null && viewer.getControl() != null
				&& viewer.getControl().getDisplay() != null) {

			viewer.getControl().getDisplay().syncExec(new Runnable() {

				public void run() {
					// Remove text from textviewer and disable slider
					if (!widget.isDisposed()) {
						widget.setText(""); //$NON-NLS-1$
					}
					if (!slider.isDisposed()) {
						slider.setEnabled(false);
					}

					// Clear all arrays
					newData.setLength(0);
					scrolledData.setLength(0);
					showingTracesFrom = 0;
					offsetsOfNewTraces.clear();

					// Empty the trim text
					TraceViewerGlobals.getTrimProvider().updateText(""); //$NON-NLS-1$
				}
			});
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerViewInterface#hasUnshownData()
	 */
	public boolean hasUnshownData() {
		return (newData.length() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#update()
	 */
	public void update() {
		update(this);
	}

	/**
	 * Calls syncExec with some Runnable
	 * 
	 * @param r
	 *            runnable object
	 */
	public void update(Runnable r) {
		Control control = viewer.getControl();
		if ((control != null) && !(control.isDisposed())) {
			control.getDisplay().syncExec(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#highlightLines
	 * (int, int, boolean)
	 */
	public void highlightLines(int startLine, int endLine, boolean syncToSource) {
		this.startHighlightLine = startLine;
		this.endHighlightLine = endLine;
		TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
				StateHolder.State.SEARCHED);
		update();

		// Sync to source
		if (syncToSource) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {

					// Jump to selected trace source
					jumpToSelectedTraceSource();
				}
			});
		}
	}

	/**
	 * Jumps to selected traces source if needed metadata exists
	 */
	private void jumpToSelectedTraceSource() {
		// Get clicked line number
		int clickedLine = widget.getLineAtOffset(widget.getSelectionRange().x)
				+ TraceViewerGlobals.getTraceViewer().getView()
						.getShowingTracesFrom();

		// Get the trace from the file
		List<TraceProperties> traceList = TraceViewerGlobals.getTraceViewer()
				.getTraces(clickedLine, clickedLine);

		TraceProperties trace = null;
		if (traceList != null && !traceList.isEmpty()) {
			trace = traceList.get(0);
		}

		OpenTraceLocationAction openLocationAction = (OpenTraceLocationAction) actionFactory
				.getOpenTraceLocationAction();

		// If traceInformation is found, run open location action
		if (trace != null && trace.information != null) {
			TraceMetaData metaData = TraceViewerGlobals.getDecodeProvider()
					.getTraceMetaData(trace.information);
			if (metaData != null && metaData.getPath() != null) {
				openLocationAction.setMetaData(metaData, false);
				openLocationAction.run();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#scrollViewToLine
	 * (int)
	 */
	public void scrollViewToLine(int lineNumber) {
		if (!TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().isStartTriggering()) {

			// Save trace to be shown
			int traceToBeShown = lineNumber;

			int topIndex = viewer.getTopIndex();
			int bottomIndex = viewer.getBottomIndex();

			// If we need more traces to start of the view
			if (traceToBeShown < showingTracesFrom) {
				getMoreTracesBeforeCurrentData(traceToBeShown);

				// More trace to end of the view
			} else if (traceToBeShown + getNumberOfLinesInView() + 2 > showingTracesFrom
					+ widget.getLineCount()
					&& TraceViewerGlobals.getTraceViewer()
							.getDataReaderAccess().getCurrentDataReader() != null
					&& (showingTracesFrom + TraceViewerGlobals.blockSize * 2 < TraceViewerGlobals
							.getTraceViewer().getDataReaderAccess()
							.getCurrentDataReader().getTraceCount())) {

				getMoreTracesAfterCurrentData(traceToBeShown, bottomIndex);

				// If the trace to be shown value is different to real value
				// shown in screen or we need to highlight search result
			} else if ((slider.getSelection() < topIndex + showingTracesFrom
					- 1 || slider.getSelection() > bottomIndex
					+ showingTracesFrom)
					|| TraceViewerGlobals.getTraceViewer().getStateHolder()
							.getState() == StateHolder.State.SEARCHED) {
				moveViewAccordingToSlider();
			} else {
				TraceViewerGlobals.getTraceViewer().getStateHolder().setState(
						StateHolder.State.NORMAL);
			}
		}
	}

	/**
	 * Moves view according to slider position and highlight found text if
	 * searching
	 */
	private void moveViewAccordingToSlider() {
		// If this is because of searching
		if (TraceViewerGlobals.getTraceViewer().getStateHolder().getState() == StateHolder.State.SEARCHED) {
			updateViewerPropertiesAfterSearch();
			setSelection();
		} else {
			viewer.setTopIndex(slider.getSelection() - showingTracesFrom);
		}
	}

	/**
	 * Get more traces after the current data
	 * 
	 * @param traceToBeShown
	 *            trace to be shown
	 * @param bottomIndex
	 *            bottom index value
	 */
	private void getMoreTracesAfterCurrentData(int traceToBeShown,
			int bottomIndex) {

		int numberOfBlocks;
		int offset;

		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTimestampParser().nullPreviousTimestamp();

		// If we need only one block
		if (traceToBeShown < showingTracesFrom + bottomIndex
				+ TraceViewerGlobals.blockSize) {
			numberOfBlocks = 1;
			offset = traceToBeShown + getNumberOfLinesInView() + 2;
		} else {
			numberOfBlocks = 2;
			offset = traceToBeShown + getNumberOfLinesInView()
					- TraceViewerGlobals.blockSize + 1;
		}

		// Check if we are getting the last block from the file
		if (offset + (TraceViewerGlobals.blockSize * numberOfBlocks) > TraceViewerGlobals
				.getTraceViewer().getDataReaderAccess().getCurrentDataReader()
				.getTraceCount()) {
			insertingLastBlock = true;
		} else {
			insertingLastBlock = false;
		}

		// Start scroll reader
		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.startScrollReader(offset, numberOfBlocks);

		// Insert after current view
		insertAfterCurrentView(numberOfBlocks, traceToBeShown);
	}

	/**
	 * Get more traces before the current data
	 * 
	 * @param traceToBeShown
	 *            trace to be shown
	 */
	private void getMoreTracesBeforeCurrentData(int traceToBeShown) {
		int numberOfBlocks;
		TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTimestampParser().nullPreviousTimestamp();
		// If we need only one block
		if (traceToBeShown + TraceViewerGlobals.blockSize > showingTracesFrom) {
			numberOfBlocks = 1;
		} else {
			numberOfBlocks = 2;
		}

		TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.startScrollReader(traceToBeShown, numberOfBlocks);

		// Insert before current view
		insertBeforeCurrentView(numberOfBlocks, traceToBeShown);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#closeProgressBar
	 * (com.nokia.traceviewer.dialog.ProgressBarDialog)
	 */
	public void closeProgressBar(ProgressBarDialog dialog) {
		update(new ProcessProgressBarUpdater(dialog));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#applyColorRules
	 * (org.eclipse.swt.custom.StyleRange[])
	 */
	public void applyColorRules(StyleRange[] ranges) {
		update(new ColorRuleUpdater(ranges));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#updateViewName
	 * ()
	 */
	public void updateViewName() {
		updateViewName = true;
		update();
	}

	/**
	 * Update the name of the view
	 */
	private void generateAndSetViewName() {
		StringBuilder viewName = new StringBuilder(VIEWNAME_NORMAL);

		// Triggering
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getTriggerProcessor().isTriggering()) {

			// Trigger is armed
			if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
					.getTriggerProcessor().isStartTriggering()) {
				viewName.append(VIEWNAME_ARMED);
			} else if (TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getTriggerProcessor()
					.isStopTriggering()) {
				viewName.append(VIEWNAME_STORING);
			}

		}
		// Paused
		if (TraceViewerGlobals.getTraceViewer().getDataReaderAccess()
				.getMainDataReader().isPaused()) {
			viewName.append(VIEWNAME_PAUSED);
		}
		// Filtered
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getFilterProcessor().isFiltering()) {
			viewName.append(VIEWNAME_FILTERED);
		}

		// Log file opened
		if (TraceViewerGlobals.getTraceViewer().getDataProcessorAccess()
				.getLogger().isLogFileOpened()) {
			String logFilePath = TraceViewerGlobals.getTraceViewer()
					.getDataProcessorAccess().getLogger()
					.getOpenedLogFileName();
			viewName.append(LOG_FILE_NAME_SEPARATOR);
			viewName.append(logFilePath);
		}

		this.setPartName(viewName.toString());
		updateViewName = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerViewInterface#isDisposed()
	 */
	public boolean isDisposed() {
		boolean disposed = false;
		Shell shell = getShell();
		if (shell == null || shell.isDisposed()) {
			disposed = true;
		}
		return disposed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#stopViewUpdate
	 * (boolean)
	 */
	public void stopViewUpdate(boolean stop) {
		viewUpdateOn = !stop;

		// If view should be refreshed
		if (!stop) {

			// Sync with UI thread
			viewer.getControl().getDisplay().asyncExec(new Runnable() {

				public void run() {
					refreshTraceBlock(TraceViewerGlobals.getTraceViewer()
							.getDataReaderAccess().getCurrentDataReader()
							.getTraceCount());
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#refreshCurrentView
	 * ()
	 */
	public void refreshCurrentView() {
		// Sync with UI thread
		viewer.getControl().getDisplay().asyncExec(new Runnable() {

			public void run() {
				refreshTraceBlock(slider.getSelection());
			}
		});
	}

	/**
	 * Find the trace block containing given trace and refreshes it. When coming
	 * here, we should be in UI thread
	 * 
	 * @param traceNumber
	 *            the trace number
	 */
	private void refreshTraceBlock(final int traceNumber) {
		// Set "showing traces from" variable to totally wrong and
		// then generate a request to get last block of traces
		showingTracesFrom = 0 - TraceViewerGlobals.blockSize * 5;
		scrollViewToLine(traceNumber);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerTraceViewInterface#setFontSize
	 * (int)
	 */
	public void setFontSize(final int size) {
		if (widget != null && !widget.isDisposed()) {
			// Sync with UI thread
			viewer.getControl().getDisplay().syncExec(new Runnable() {

				public void run() {
					// Set the font
					Font font = new Font(viewer.getControl().getDisplay(),
							new FontData(FONT, size, SWT.NORMAL));
					widget.setFont(font);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface#showErrorMessage
	 * (java.lang.String)
	 */
	public void showErrorMessage(String error) {
		MessageDialog.openError(getShell(), TRACE_VIEWER_TITLE, error);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerDialogInterface#
	 * showConfirmationDialog(java.lang.String)
	 */
	public boolean showConfirmationDialog(String message) {
		boolean ok = MessageDialog.openConfirm(getShell(), TRACE_VIEWER_TITLE,
				message);
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.nokia.traceviewer.engine.TraceViewerDialogInterface#
	 * showInformationMessage(java.lang.String)
	 */
	public void showInformationMessage(String message) {
		MessageDialog.openInformation(getShell(), TRACE_VIEWER_TITLE, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface#openPreferencePage
	 * (
	 * com.nokia.traceviewer.engine.TraceViewerDialogInterface.TVPreferencePage)
	 */
	public boolean openPreferencePage(TVPreferencePage TVpage) {
		boolean ret = false;
		PreferenceManager mgr = new PreferenceManager();

		// Create general preference page
		IPreferencePage generalPage = new TraceViewerPreferencesPage();
		generalPage.setTitle(TRACE_VIEWER_TITLE);
		IPreferenceNode generalNode = new PreferenceNode("1", generalPage); //$NON-NLS-1$
		mgr.addToRoot(generalNode);

		// Create advanced preference page
		IPreferencePage advancedPage = new TraceViewerAdvancedPreferencesPage();
		advancedPage.setTitle(Messages
				.getString("TraceViewerView.AdvancedPageTitle")); //$NON-NLS-1$
		IPreferenceNode advancedNode = new PreferenceNode("2", advancedPage); //$NON-NLS-1$
		mgr.addTo("1", advancedNode); //$NON-NLS-1$

		// Create connection preference page
		IPreferencePage connectionPage = new TraceViewerConnectionPreferencesPage();
		connectionPage.setTitle(Messages
				.getString("TraceViewerView.ConnectionPageTitle")); //$NON-NLS-1$
		IPreferenceNode connectionNode = new PreferenceNode("3", connectionPage); //$NON-NLS-1$
		mgr.addTo("1", connectionNode); //$NON-NLS-1$

		PreferenceDialog dialog = new PreferenceDialog(getShell(), mgr);
		dialog.create();
		dialog.getTreeViewer().expandAll();

		// Switch the page
		switch (TVpage) {
		case GENERAL:
			dialog.getTreeViewer().setSelection(
					new StructuredSelection(generalNode));
			break;
		case ADVANCED:
			dialog.getTreeViewer().setSelection(
					new StructuredSelection(advancedNode));
			break;
		case CONNECTION:
			dialog.getTreeViewer().setSelection(
					new StructuredSelection(connectionNode));
			break;
		default:
			break;
		}

		// Open dialog and get return value
		int ok = dialog.open();
		if (ok == Window.OK) {
			ret = true;
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerDialogInterface
	 * #createDialog
	 * (com.nokia.traceviewer.engine.TraceViewerDialogInterface.Dialog)
	 */
	public TraceViewerDialog createDialog(Dialog name) {
		TraceViewerDialog dialog = dialogFactory.construct(name, getShell());
		return dialog;
	}
}
