/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
 */

package com.nokia.carbide.cpp.internal.pi.visual;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel.GraphComponentWrapper;
import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.editors.Messages;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;
import com.nokia.carbide.cpp.pi.visual.IGraphChangeListener;

/**
 * 
 * A GraphComposite is added to the SashForm for a tab.
 * 
 * A GraphComposite has an option centered title, a left area to contain the
 * y-axis legend, and a scrollable area containing the graph and the x-axis
 * legend.
 */
public class GraphComposite extends Composite implements SelectionListener,
		ControlListener, IGraphChangeListener {

	// Strings for tooltips
	private static final String TOOLTIP_MAXIMIZE = "Maximize graph";
	private static final String TOOLTIP_MAXIMIZE_RESTORE = "Restore graph";
	private static final String TOOLTIP_MINIMIZE = "Minimize graph";
	private static final String TOOLTIP_MINIMIZE_RESTORE = "Restore graph";
	private static final String TOOLTIP_MOVE_UP = "Move graph up";
	private static final String TOOLTIP_MOVE_DOWN = "Move graph down";

	// size of the titlebar
	private static final int TITLESIZE = 20;

	private static final int TOTAL_SASH_WEIGHT = 1000;

	// UI components
	public FigureCanvas leftLegend;
	public FigureCanvas figureCanvas;
	private PICompositePanel compositePanel;
	private Button buttonMinimize = null;
	private Button buttonMaximize = null;
	private Label labelTitle = null;
	private Button buttonMoveGraphUp = null;
	private Button buttonMoveGraphDown = null;
	private Composite compositeTitleBar = null;
	private Combo comboGraphType = null;
	private Button buttonHelp;

	// Images for title buttons
	private Image imageMoveGraphUp = null;
	private Image imageMoveGraphDown = null;
	private Image imageMinimizeGraph = null;
	private Image imageMinimizeRestoreGraph = null;
	private Image imageMaximizeGraph = null;
	private Image imageMaximizeRestoreGraph = null;

	// PreviousWight variable that is used for saving graph's size when
	// minimizing/restoring
	private double previousWeight = 0.0;

	// State of graph(minimized/maximized)
	private boolean isMaximized = false;
	private boolean isMinimized = false;

	// Is view initialized completely
	private boolean isViewInitialized = false;

	// Title Bar actions
	private Action[] titleBarActions = null;
	private String contextHelpId;

	private GraphComponentWrapper graphComponent;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent where UI components are placed
	 * @param style
	 *            that is used
	 * @param titleString
	 *            String for titlebar
	 * @param compositePanel
	 *            panel that is using this component
	 */
	public GraphComposite(Composite parent, int style, String titleString,
			final PICompositePanel compositePanel, ITitleBarMenu titleBar, 
			GraphComponentWrapper graphComponent) {
		super(parent, style);
		this.setLayout(new FormLayout());
		this.compositePanel = compositePanel;
		this.graphComponent = graphComponent;

		// Create TitleBar if needed
		if (titleString != null) {
			createTitleBarUIComponents(parent, titleString, titleBar);
		}

		// Create left legend and canvas where graph is drawn
		leftLegend = new FigureCanvas(this);
		figureCanvas = new FigureCanvas(this);

		// Set layouts for canvases and title bar
		FormData formData;
		formData = new FormData();
		if (compositeTitleBar != null) {
			formData.top = new FormAttachment(compositeTitleBar, 0, SWT.BOTTOM);

		} else {
			formData.top = new FormAttachment(0);
		}
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(0);
		formData.width = IGenericTraceGraph.Y_LEGEND_WIDTH;
		leftLegend.setLayoutData(formData);

		formData = new FormData();
		if (compositeTitleBar != null) {
			formData.top = new FormAttachment(compositeTitleBar, 0, SWT.BOTTOM);

		} else {
			formData.top = new FormAttachment(0);
		}
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(leftLegend, 0, SWT.RIGHT);
		formData.right = new FormAttachment(100);
		figureCanvas.setLayoutData(formData);

		// Enable/disable needed UI Components
		updateTitleBarButtonsStatus();

		// add listener for resizing the view
		figureCanvas.addControlListener(this);

	}

	/**
	 * @return returns index if this graph in sashform
	 * @throws IllegalStateException
	 *             if component not found in sashform
	 */
	private int getIndexOfThisComponentInSashForm() {

		Control[] array = compositePanel.getSashForm().getChildren();
		for (int i = 0; i < array.length; i++) {
			if (array[i].getClass() == this.getClass()) {
				GraphComposite composite = (GraphComposite) array[i];
				if (composite.equals(this)) {
					return i;
				}
			}
		}

		// if not found return error value
		throw new IllegalStateException();

	}

	/**
	 * @return returns control above this component in the sashform
	 */
	private Control getFormAbove() {
		GraphComposite graphAbove = null;
		GraphComposite[] graphs = getVisibleGraphs();

		for (int i = 0; i < graphs.length; i++) {
			if (graphs[i] == this) {
				if (i > 0) {
					graphAbove = graphs[i - 1];
				}
				break;
			}
		}
		return graphAbove;
	}

	/**
	 * @return returns control below this component in the sashform
	 */
	private Control getFormBelow() {
		GraphComposite graphBelow = null;
		GraphComposite[] graphs = getVisibleGraphs();

		for (int i = 0; i < graphs.length; i++) {
			if (graphs[i] == this) {
				if (i != graphs.length - 1) {
					graphBelow = graphs[i + 1];
				}
				break;
			}
		}
		return graphBelow;
	}

	/**
	 * Updates titlebar buttons status. Updates images and tooltips and
	 * enables/disables needed component according to status
	 * (minimized/maximized) and place of this graph.
	 */
	public void updateTitleBarButtonsStatus() {

		// Ensure that buttons are created.
		if (buttonMinimize == null) {
			return;
		}

		// If graph is maximized, only maximize button needs to be updated
		if (this.isMaximized) {
			buttonMaximize.setImage(imageMaximizeRestoreGraph);
			buttonMaximize.setToolTipText(TOOLTIP_MAXIMIZE_RESTORE);

			// Disable move and minimize buttons if maximized
			buttonMoveGraphUp.setEnabled(false);
			buttonMoveGraphDown.setEnabled(false);
			buttonMinimize.setEnabled(false);

		} else {

			// Minimize/restore button
			if (this.isMinimized) {
				buttonMinimize.setImage(imageMinimizeRestoreGraph);
				buttonMinimize.setToolTipText(TOOLTIP_MINIMIZE_RESTORE);
				buttonMaximize.setEnabled(false);
			} else {
				buttonMinimize.setImage(imageMinimizeGraph);
				buttonMinimize.setToolTipText(TOOLTIP_MINIMIZE);
				buttonMaximize.setEnabled(true);
			}

			buttonMaximize.setImage(imageMaximizeGraph);
			buttonMaximize.setToolTipText(TOOLTIP_MAXIMIZE);

			GraphComposite[] children = getVisibleGraphs();
			int childCnt = children.length;

			// Enable minimize button if more than one graph
			buttonMinimize.setEnabled(childCnt > 1);
			buttonMaximize.setEnabled(childCnt > 1);

			int index = -1;
			for (int i = 0; i < childCnt; i++) {
				if (children[i] == this) {
					index = i;
				}
			}
			// if index number is illegal do nothing.
			if (index < 0) {
				return;
			}
			// disable up arrow if already on the top of the form or only one
			// graph exists on page
			buttonMoveGraphUp.setEnabled(index > 0 && childCnt > 1);
			// disable down arrow if already on the bottom of the form
			buttonMoveGraphDown
					.setEnabled(index < childCnt - 1 && childCnt > 1);
		}

		compositePanel.getSashForm().layout();

	}

	/**
	 * Updates all graphs titlebar buttons status if graph is visible
	 */
	public void updateAllGraphsButtons() {
		if(!this.isDisposed() && this.isVisible()){
			GraphComposite[] controlArray = getVisibleGraphs();
			for (GraphComposite item : controlArray) {
				GraphComposite graph = (GraphComposite) item;
				graph.updateTitleBarButtonsStatus();
			}
		}
	}

	/**
	 * Initializes images for titlebar buttons
	 */
	private void createImagesForTitleBar() {

		// Get budle's location in file system
		URL url;
		ImageDescriptor createFromURL;
		Bundle piBundle = Platform.getBundle("com.nokia.carbide.cpp.pi"); //$NON-NLS-1$
		if (piBundle == null)
			return;

		// Maximize
		url = FileLocator.find(piBundle, new Path(Messages
				.getString("PIPageEditorContributor.maximizeGraphIcon")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMaximizeGraph = createFromURL.createImage();
			buttonMaximize.setImage(imageMaximizeGraph);

		}

		// Maximize restore
		url = FileLocator
				.find(
						piBundle,
						new Path(
								Messages
										.getString("PIPageEditorContributor.maximizeRestoreGraphIcon")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMaximizeRestoreGraph = createFromURL.createImage();

		}

		// Minimize
		url = FileLocator.find(piBundle, new Path(Messages
				.getString("PIPageEditorContributor.minimizeGraphIcon")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMinimizeGraph = createFromURL.createImage();
			buttonMinimize.setImage(imageMinimizeGraph);
		}

		// Minimize restore
		url = FileLocator
				.find(
						piBundle,
						new Path(
								Messages
										.getString("PIPageEditorContributor.minimizeRestoreGraphIcon")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMinimizeRestoreGraph = createFromURL.createImage();

		}

		// Move down
		url = FileLocator.find(piBundle, new Path(Messages
				.getString("PIPageEditorContributor.moveGraphDown")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMoveGraphDown = createFromURL.createImage();
			buttonMoveGraphDown.setImage(imageMoveGraphDown);

		}

		// Move Up
		url = FileLocator.find(piBundle, new Path(Messages
				.getString("PIPageEditorContributor.moveGraphUp")), null); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			imageMoveGraphUp = createFromURL.createImage();
			buttonMoveGraphUp.setImage(imageMoveGraphUp);
		}
		
	}

	/**
	 * Creates ui components located int titlebar
	 * 
	 * @param parent
	 *            parent component where components are placed
	 * @param titleString
	 *            title text
	 * @param titleBar object implementing ITitleBarMenu (actions and help for title bar)
	 */
	private void createTitleBarUIComponents(Composite parent,
			String titleString, ITitleBarMenu titleBar) {

		FormData formData;

		// Create composite where all components are placed
		compositeTitleBar = new Composite(this, SWT.NONE);
		GridLayout layoutComposite = new GridLayout();
		layoutComposite.numColumns = titleBar != null && titleBar.getContextHelpId() != null ? 7 : 6;
		layoutComposite.marginWidth = 0;
		layoutComposite.marginHeight = 0;
		layoutComposite.horizontalSpacing = 1;
		compositeTitleBar.setLayout(layoutComposite);

		// Minimize/restore button
		buttonMinimize = new Button(compositeTitleBar, SWT.PUSH);
		buttonMinimize.setFont(PIPageEditor.helvetica_7);
		buttonMinimize.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// Create drop-down list if action array is not null
		if (titleBar != null && titleBar.addTitleBarMenuItems() != null) {
			// graph type Combo box
			comboGraphType = new Combo(compositeTitleBar, SWT.READ_ONLY);
			GridData gridData = new GridData(GridData.FILL_VERTICAL);
			comboGraphType.setLayoutData(gridData);
			this.titleBarActions = titleBar.addTitleBarMenuItems();
			int checkedItemIndex = 0;
			for (int i = 0; i < titleBarActions.length; i++) {
				comboGraphType.add(titleBarActions[i].getText());
				if (titleBarActions[i].isChecked()) {
					checkedItemIndex = i;
				}
			}
			comboGraphType.select(checkedItemIndex);
			comboGraphType.setFont(PIPageEditor.helvetica_7);
		}
		// title label
		labelTitle = new Label(compositeTitleBar, SWT.CENTER);
		labelTitle.setFont(PIPageEditor.helvetica_8);
		labelTitle.setText(titleString);

		if (titleBar != null && titleBar.getContextHelpId() != null){
			this.contextHelpId = titleBar.getContextHelpId();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, this.contextHelpId);
			
			buttonHelp = new Button(compositeTitleBar, SWT.PUSH);
			buttonHelp.setToolTipText("Help");
			buttonHelp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			Image helpImage = PiPlugin.getImage("icons/linkto_help.gif"); //$NON-NLS-1$
			if (helpImage != null){
				buttonHelp.setImage(helpImage);
			}			
		}

		// move down button
		buttonMoveGraphDown = new Button(compositeTitleBar, SWT.PUSH);
		buttonMoveGraphDown.setToolTipText(TOOLTIP_MOVE_DOWN);
		buttonMoveGraphDown.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// mode graph up
		buttonMoveGraphUp = new Button(compositeTitleBar, SWT.PUSH);
		buttonMoveGraphUp.setToolTipText(TOOLTIP_MOVE_UP);
		buttonMoveGraphUp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// maximize/restore button
		buttonMaximize = new Button(compositeTitleBar, SWT.PUSH);
		buttonMaximize.setFont(PIPageEditor.helvetica_7);
		buttonMaximize.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		createImagesForTitleBar();

		// calculate what intent should be used so that title label is in the
		// center of the view
		int indent = 0;

		// increase intent value by all components that are left from the title

		// get size of the combobox
		if (comboGraphType != null) {
			indent -= comboGraphType.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		}
		indent -= buttonMinimize.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		// decrease intent value by all components that are right from the title
		if (buttonHelp != null){
			indent += buttonHelp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;			
		}
		indent += buttonMaximize.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		indent += buttonMoveGraphDown.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		indent += buttonMoveGraphDown.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = indent;
		labelTitle.setLayoutData(gridData);

		// Set layouts for composite
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.height = TITLESIZE;
		compositeTitleBar.setLayoutData(formData);

		// Set background color for all components
		Color BgColor = parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		compositeTitleBar.setBackground(BgColor);
		buttonMinimize.setBackground(BgColor);
		/*
		 * if (comboGraphType != null) { comboGraphType.setBackground(BgColor);
		 * }
		 */
		labelTitle.setBackground(BgColor);
		buttonMoveGraphUp.setBackground(BgColor);
		buttonMoveGraphDown.setBackground(BgColor);
		buttonMaximize.setBackground(BgColor);

		// Add selection listeners for buttons
		buttonMoveGraphDown.addSelectionListener(this);
		buttonMoveGraphUp.addSelectionListener(this);
		buttonMaximize.addSelectionListener(this);
		buttonMinimize.addSelectionListener(this);
		if (comboGraphType != null) {
			comboGraphType.addSelectionListener(this);
		}
		if (buttonHelp != null){
			buttonHelp.addSelectionListener(this);
		}
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions needed here
	}
	
	public Combo getTitleBarCombo(){
		return comboGraphType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {

		if (e.widget == comboGraphType) {
			if (titleBarActions == null) {
				return;
			}
			titleBarActions[comboGraphType.getSelectionIndex()].run();
		}

		else {
			if (e.widget == buttonMoveGraphDown) {
				this.moveGraphDown();
			} else if (e.widget == buttonMoveGraphUp) {
				this.moveGraphUp();
			} else if (e.widget == buttonMinimize) {
				this.minimizeOrRestoreGraph();
			} else if (e.widget == buttonMaximize) {
				if (this.isMinimized) {
					this.minimizeOrRestoreGraph();
				}
				this.maximizeOrRestoreGraph();
			} else if (e.widget == buttonHelp){
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(this.contextHelpId);
			}
			this.updateAllGraphsButtons();
			compositePanel.getSashForm().layout();
		}
	}

	/**
	 * moves this graph down in the form
	 */
	private void moveGraphDown() {
		Control control = getFormBelow();
		if (control != null) {
			moveBelow(control);
		}
	}

	/**
	 * moves this graph up in the form
	 */
	private void moveGraphUp() {
		Control control = getFormAbove();
		if (control != null) {
			moveAbove(control);
		}
	}

	/**
	 * minimizes or maximizes graph according to its current state
	 */
	private void minimizeOrRestoreGraph() {

		// Since minimizing and restoring forms in SashForm is not very
		// handy this algorithm is pretty complex...

		int graphIndex = getIndexOfThisComponentInSashForm();

		// Set none of the forms in SashForm maximised
		this.compositePanel.getSashForm().setMaximizedControl(null);

		int[] weights = getWeightOfGraphs(true, false);

		// if index not found or it is greater than size of sash
		// do nothing(this should not happen)
		if (graphIndex < 0 || graphIndex >= weights.length) {
			return;
		}

		// count sum of weight values in array
		int weightSum = 0;
		for (int item : weights) {
			weightSum += item;
		}

		// get other(other than the one minimized/restored) graphs
		// combined weight
		double otherGraphsWeight = weightSum - weights[graphIndex];

		int restoredGraphSize = 0;

		if (this.isMinimized && weights[graphIndex] < 100) { // Restore graph

			this.isMinimized = false;
			// calculate restored size based on graphs previous size
			double multiplier = (double) previousWeight * 2;
			restoredGraphSize = (int) (multiplier * otherGraphsWeight);
			weights[graphIndex] = restoredGraphSize;
			graphComponent.graphComponent.graphVisibilityChanged(true);

			// update legend counts in case selection area has changed.
			graphComponent.graphComponent.updateSelectionArea(PIPageEditor
					.currentPageEditor().getStartTime(), PIPageEditor
					.currentPageEditor().getEndTime());

		} else { // minimize
			this.isMinimized = true;
			graphComponent.graphComponent.graphVisibilityChanged(false);

		}
		compositePanel.getSashForm().layout();// after revealing or hiding
												// legend tables
		compositePanel.getVisualiser().getBottomComposite().layout();
		// After graph is restored or marked as minimized, we need calculate
		// size of minimized graphs again.

		// Add restored graphs size to othergraphsSize
		otherGraphsWeight += restoredGraphSize;

		Control[] controlArray = compositePanel.getSashForm().getChildren();

		// calculate percent value of title size(the size which is used for
		// minimizing)
		int xSize = compositePanel.getSashForm().getSize().y;
		double percentValue = (double) TITLESIZE / (double) xSize;
		previousWeight = (double) weights[graphIndex] / (double) weightSum;
		int minimizedSize = (int) ((double) otherGraphsWeight * percentValue);

		// Go through array of form items and set all minimized forms size to
		// calculated minimizedSize
		for (int i = 0; i < controlArray.length; i++) {
			if (controlArray[i].getClass() == this.getClass()) {
				GraphComposite graph = (GraphComposite) controlArray[i];
				if (graph.isMinimized) {
					weights[i] = minimizedSize;
				}
			}
		}

		// set calculated weights to sashform
		try {
			compositePanel.getSashForm().setWeights(weights);
		} catch (Exception e2) {
			// do nothing
		}

	}

	/**
	 * Returns the weight of the children controls.
	 * 
	 * @param zerohidden
	 *            : if true, zero out values for currently hidden controls in
	 *            return array
	 * @param onlyHidden
	 *            : if true, only return weights of currently hidden controls
	 *            (all others zeroed out)
	 * @return
	 */
	private int[] getWeightOfGraphs(boolean zeroHidden, boolean onlyHidden) {
		int[] weights = this.compositePanel.getSashForm().getWeights();

		// ignore the weight of currently hidden controls
		Control[] children = this.compositePanel.getSashForm().getChildren();
		int j = 0;
		for (int i = 0; i < weights.length; i++) {
			while (children[j].getClass() != this.getClass()) {
				j++;// skip any children that are not graphs
			}
			if (children[j].isVisible() && onlyHidden
					|| !children[j].isVisible() && zeroHidden) {
				weights[i] = 0;
			}
			j++;
		}
		return weights;
	}

	/**
	 * @return GraphComposite[] of all currently visible graphs on the current
	 *         page.
	 */
	private GraphComposite[] getVisibleGraphs() {
		List<GraphComposite> graphs = new ArrayList<GraphComposite>();
		for (Control control : this.compositePanel.getSashForm().getChildren()) {
			if (control.getClass() == this.getClass() && control.isVisible()) {
				graphs.add((GraphComposite) control);
			}
		}
		return graphs.toArray(new GraphComposite[graphs.size()]);
	}

	/**
	 * Maximizes or restores graph according to its current state
	 */
	private void maximizeOrRestoreGraph() {
		if (compositePanel.getSashForm().getMaximizedControl() != this) {
			isMaximized = true;
			compositePanel.getSashForm().setMaximizedControl(this);
			graphComponent.graphComponent.graphMaximized(true);
			if (isMinimized) {
				graphComponent.graphComponent.graphVisibilityChanged(true);
			}

		} else {
			isMaximized = false;
			compositePanel.getSashForm().setMaximizedControl(null);
			graphComponent.graphComponent.graphMaximized(false);
			if (isMinimized) {
				graphComponent.graphComponent.graphVisibilityChanged(false);
			}
			
			// update selection area to all visible graphs
			for (GraphComposite graph : getVisibleGraphs()) {
				graph.graphComponent.graphComponent.updateSelectionArea(
						PIPageEditor.currentPageEditor().getStartTime(),
						PIPageEditor.currentPageEditor().getEndTime());
			}
			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events
	 * .ControlEvent)
	 */
	public void controlMoved(ControlEvent e) {
		updateTitleBarButtonsStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt
	 * .events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {

		// update minimize button status if size of the form is changed
		// manually.

		// exclude status update when graph is maximized
		// (so that graph remembers if it was minimized previously)

		this.getParent().layout();
		if (this.isMaximized == false) {
			if (this.isMinimized
					&& (double) this.getSize().y
							/ (double) compositePanel.getSashForm().getSize().y > 0.1) {
				this.isMinimized = false;
				graphComponent.graphComponent.graphVisibilityChanged(true);
				compositePanel.getSashForm().layout();// after revealing or
														// hiding legend tables
				updateTitleBarButtonsStatus();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.visual.IVisibilityListener#onVisiblityChanged
	 * (boolean)
	 */
	public void onVisiblityChanged(boolean visible) {

		// change the visibility of this graph composite
		// and reset to default layout

		if (visible) {
			// check the newly made visible control has a weight
			// if not, set an arbitrary weight to prevent an exception in the
			// sashForm
			int[] weights = this.compositePanel.getSashForm().getWeights();
			int idx = getIndexOfThisComponentInSashForm();
			if (weights[idx] == 0) {
				weights[idx] = 100;
				compositePanel.getSashForm().setWeights(weights);
			}
		}

		// we need to reset maximised controls, because if they are now
		// invisible, they
		// prevent other graphs from showing
		if (compositePanel.getSashForm().getMaximizedControl() != null) {
			((GraphComposite) compositePanel.getSashForm()
					.getMaximizedControl()).isMaximized = false;
			compositePanel.getSashForm().setMaximizedControl(null);
		}

		this.setVisible(visible);
		// deal with the legend as well
		graphComponent.graphComponent.graphVisibilityChanged(visible
				&& isMinimized ? false : visible);

		resetLayoutToDefault();

		// button status needs to be updated for all graphs
		for (GraphComposite graph : getVisibleGraphs()) {
			graph.updateTitleBarButtonsStatus();
		}
	}

	/**
	 * Resets the layout of all visible component to a default layout. Minimised
	 * components stay minimised (just toolbar visible), all other components
	 * are equally sized.
	 */
	private void resetLayoutToDefault() {
		int xSize = compositePanel.getSashForm().getSize().y;
		if (xSize == 0) {// not yet initialised
			return;
		}
		double pixelWeight = (double) TOTAL_SASH_WEIGHT / xSize;
		int minCount = 0;
		GraphComposite[] visibleGraphs = getVisibleGraphs();

		if (visibleGraphs.length > 0) {
			int minimisedWeight = (int) (TITLESIZE * pixelWeight);

			for (GraphComposite cmp : visibleGraphs) {
				if (cmp.isMinimized) {
					minCount++;
				}
			}

			double defaultWeight = TOTAL_SASH_WEIGHT
					- (minCount * minimisedWeight);
			if (minCount < visibleGraphs.length - 1) {
				defaultWeight /= (visibleGraphs.length - minCount);
			}

			int[] weights = compositePanel.getSashForm().getWeights();
			int i = 0;
			for (Control control : this.compositePanel.getSashForm()
					.getChildren()) {
				if (control.getClass() == this.getClass()
						&& control.isVisible()) {
					if (((GraphComposite) control).isMinimized) {
						weights[i] = minimisedWeight;
					} else {
						weights[i] = (int) defaultWeight;
					}
				}
				i++;
			}
			this.compositePanel.getSashForm().setWeights(weights);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.pi.visual.IGraphChangeListener#onTitleChange(java
	 * .lang.String, java.lang.String)
	 */
	public void onTitleChange(final String newTitle) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				labelTitle.setText(newTitle);
			}
		});
	}

	/**
	 * Initialisation code for all graphs should go here. This method is called
	 * on editor part activation
	 */
	public void initialiseGraphs() {
		// minimize other than Memory and Thread Load graphs when the view is
		// opened

		// this code can only be executed once the sashform is visible
		// otherwise the weights of controls on the sashform are zero and the
		// minimise code doesn't work.
		if (this.isViewInitialized == false
				&& compositePanel.getSashForm().isVisible()) {
			this.isViewInitialized = true;
			if (graphComponent.graphComponent.isGraphMinimizedWhenOpened()) {
				this.minimizeOrRestoreGraph();
				updateTitleBarButtonsStatus();
				compositePanel.getSashForm().layout();
			}
		}
	}

}
