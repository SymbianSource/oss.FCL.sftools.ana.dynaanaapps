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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JDialog;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Slider;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.analyser.TestGUI;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITitleBarMenu;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.visual.IGenericTraceGraph;

public class PICompositePanel implements ActionListener, PIEventListener {
	private SashForm sashForm;
	private Slider slider = null;

	private static final long serialVersionUID = -6350043592270317606L;

	private static final int MOUSE_DOWN = 0;
	private static final int MOUSE_MOVE = 1;
	private static final int MOUSE_DOUBLECLICK = 2;

	private ArrayList<GraphComponentWrapper> graphComponents;
	private ProfileVisualiser profVisu;

	public int lastSampleX = 0;
	private int sizeX = 0;
	private int sizeY = 450;
	private int preferredSizeX = 100000;
	private int preferredSizeY = 0; // this is constant!

	private double selectionStart = -1;
	private double selectionEnd = -1;

	private boolean buttonDown = false;
	private boolean dragOn = false;
	private double origStart = 0;
	private double scale = 10;
	private double previousScale = 10;

	private PIVisualSharedData sharedData;

	JDialog dialog = null;

	SynchroniseDialog dialogPanel = null;
	private IGenericTraceGraph activeGraph = null;

	// create a ScrollComposite, with a horizontal scrollbar, containing a
	// sashForm without a scrollbar
	public PICompositePanel(Composite parent,
			ProfileVisualiser profileVisualiser) {
		// all graphs will go into a vertical SashForm
		this.sashForm = new SashForm(parent, SWT.VERTICAL);
		this.sashForm.SASH_WIDTH = 5; // 5 pixel wide sash
		this.sashForm.setLayout(new FillLayout());

		// this.scrolled.setContent(this.sashForm);
		//		
		// // listen for when the horizontal scroll bar is selected
		// final ScrollBar horizontalBar = this.scrolled.getHorizontalBar();
		//
		// horizontalBar.addSelectionListener(new SelectionListener() {
		// public void widgetSelected(SelectionEvent e) {
		// // since other pages associated with the GppTraceGraph scroll when
		// this page scrolls,
		// // also let listening plugins know
		// int ai = AnalysisIdentifier.getIdentifier();
		//        		Enumeration enu = PluginInitialiser.getPluginInstances(ai, "com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
		// if (enu != null) {
		// Event event = new Event();
		// event.x = scrolled.getOrigin().x;
		// event.y = scrolled.getOrigin().y;
		//        			event.data = "FigureCanvas"; //$NON-NLS-1$
		//        			
		// while (enu.hasMoreElements())
		// {
		// IEventListener plugin = (IEventListener)enu.nextElement();
		//            			plugin.receiveEvent("scroll", event); //$NON-NLS-1$
		// }
		// }
		// }
		//
		// public void widgetDefaultSelected(SelectionEvent e) {
		// widgetSelected(e);
		// }
		// });
		//
		// // minor scroll is 10 pixels
		// horizontalBar.setIncrement(10);
		//
		// // major scroll is the width of the window
		// horizontalBar.setPageIncrement(this.scrolled.getBounds().width);
		// this.scrolled.addControlListener(new ControlAdapter()
		// {
		// public void controlResized(ControlEvent e) {
		// horizontalBar.setPageIncrement(scrolled.getBounds().width);
		// }
		// });

		this.graphComponents = new ArrayList<GraphComponentWrapper>();
		this.profVisu = profileVisualiser;
		this.sharedData = new PIVisualSharedData();
	}

	public SashForm getSashForm() {
		return this.sashForm;
	}

	// returns true if the graph needs to be updated (it is either
	// active or visible)
	public boolean hasToBeUpdated(IGenericTraceGraph graph) {
		if (graph.getVisualSize().height > 0)
			return true;
		else if (graph.equals(this.getActiveGraph()))
			return true;
		else
			return false;
	}

	// this method adds listeners to graph selections
	private void addMouseListeners(final GraphComponentWrapper wrap,
			final FigureCanvas graphCanvas) {
		if (wrap.panel == null)
			return;

		if (wrap.graphComponent instanceof MouseMotionListener) {
			wrap.panel
					.addMouseMotionListener((MouseMotionListener) wrap.graphComponent);
		}

		if (wrap.graphComponent instanceof MouseMoveListener) {
			if (graphCanvas != null)
				graphCanvas
						.addMouseMoveListener((MouseMoveListener) wrap.graphComponent);
		}

		if (wrap.graphComponent instanceof MouseListener) {
			if (graphCanvas != null)
				graphCanvas
						.addMouseListener((MouseListener) wrap.graphComponent);
		}

		if (graphCanvas == null)
			return;

		graphCanvas.addMouseListener(new MouseListener() {
			public void mouseDown(MouseEvent me) {
				// for button 3, show the popup context menu
				if (me.button == 3) // button 3
				{
					final int x = me.x; // don't worry about me.x, this is
					// always a valid coordinate within the
					// panel
					final int y = me.y;
					final MouseEvent meFinal = me;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							Menu menu = TestGUI.getInstance()
									.getGraphPopupMenu(
											profVisu.getContentPane()
													.getShell(), true);

							// add submenu items
							addSubGraphMenuItems(menu, meFinal);
							menu.setLocation(graphCanvas.toDisplay(x, y));
						}
					});
					return;
				}

				/*
				 * for other buttons, we may be deleting the current graph
				 * selection or, by dragging, creating a new selection
				 */
				buttonDown = true;
				dragOn = false;

				// me.x will always be a valid coordinate within the Widget,
				// rounded to the nearest millisecond
				// since this is the FigureCanvas, not the Panel, adjust x
				selectionStart = (int) ((me.x + getScrolledOrigin(wrap.getGraphComponent()).x) * scale + .0005);

				origStart = selectionStart;

				// send the message to the component under the mouse
				if (me.getSource() instanceof GraphComponentWrapper) {
					IGenericTraceGraph source = ((GraphComponentWrapper) me
							.getSource()).getGraphComponent();
					if (source instanceof PIEventListener
							&& hasToBeUpdated(source)) {
						PIEventListener lis = (PIEventListener) source;

						for (int i = 0; i < graphComponents.size(); i++) {
							IGenericTraceGraph possible = (IGenericTraceGraph) graphComponents
									.get(i).graphComponent;
							if (possible.equals(source)) {
								PIEvent be = new PIEvent(source,
										PIEvent.MOUSE_PRESSED);
								lis.piEventReceived(be);
							}
						}
					}
				}
			}

			public void mouseUp(MouseEvent me) {
				// ignore releases of button 3 (context menu button)
				if (me.button == 3) // button 3
					return;

				// otherwise, either remove the current selection or replace it
				// with a new one
				buttonDown = false;

				if (dragOn) // if dragging was done
				{
					if (selectionStart == selectionEnd) {
						// no range is selected
						selectionStart = -1;
						selectionEnd = -1;
					}
					if (selectionStart < 0 && selectionStart != -1) {
						selectionStart = 0;
					}

					double maxEndTime = PIPageEditor.currentPageEditor()
							.getMaxEndTime() * 1000;
					if (selectionStart > maxEndTime) {
						// no range is selected
						selectionStart = 0;
						selectionEnd = 0;
					} else if (selectionEnd > maxEndTime) {
						selectionEnd = maxEndTime + .0005;
					}
					profVisu.updateStatusBarTimeInterval(
							selectionStart / 1000, selectionEnd / 1000);

					setSelectionFields();

					// inform all subcomponents that the selection area has
					// changed
					PIEvent be = new PIEvent(new double[] { selectionStart,
							selectionEnd }, PIEvent.SELECTION_AREA_CHANGED);
					sendEventToSubComponents(be);
					repaintComponent();

					// since some pages that depend on the selection start and
					// end times may not have
					// graph components (e.g., the function call plugin page),
					// also let listening plugins know
					Enumeration e = PluginInitialiser
							.getPluginInstances(NpiInstanceRepository
									.getInstance().activeUid(),
									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
					if (e != null) {
						Event event = new Event();
						event.start = (int) selectionStart;
						event.end = (int) selectionEnd;

						while (e.hasMoreElements()) {
							IEventListener plugin = (IEventListener) e
									.nextElement();
							plugin.receiveEvent("changeSelection", event); //$NON-NLS-1$
						}
					}

					dragOn = false;
				}
			}

			public void mouseDoubleClick(MouseEvent me) {
				sendEventToSubGraphs(PICompositePanel.MOUSE_DOUBLECLICK, me);
				buttonDown = false;
			}
		});

		graphCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent me) {

				int xOrigin = getScrolledOrigin(wrap.getGraphComponent()).x;

				me.x += xOrigin;

				if (!buttonDown) {
					sendEventToSubGraphs(PICompositePanel.MOUSE_MOVE, me);
					return;
				}

				// dragging is moving the mouse while a button (other than
				// button 3) is down
				dragOn = true;

				// round to the nearest millisecond
				selectionEnd = (int) (me.x * scale + .0005);

				// mouse event may return out of range X, that may
				// crash when we use it to index data array
				selectionEnd = selectionEnd >= 0 ? selectionEnd : 0;

				if (me.x >= xOrigin + sashForm.getBounds().width) {
					selectionEnd = ((int) ((xOrigin
							+ sashForm.getBounds().width - 1)
							* scale + .0005));
				}

				if (selectionEnd < origStart) // if selection is drawn from
				// right to left
				{
					selectionStart = selectionEnd;
					selectionEnd = origStart;
				} else if (selectionEnd > origStart) {
					selectionStart = origStart;
				}

				setSelectionFields();

				PIPageEditor.currentPageEditor().setLocalTime(
						selectionStart / 1000, selectionEnd / 1000);

				// until mouse button is released, change the time interval
				// display only on this page
				profVisu.updateStatusBarTimeInterval(
						selectionStart / 1000, selectionEnd / 1000);

				drawSelectionRect(me, graphCanvas);
				repaintComponent();

				// why is this not in repaintComponent?
				wrap.panel.repaint();
			}
		});
	}

	private void drawSelectionRect(MouseEvent me, FigureCanvas originFigureCanvas) {
		if ((me.getSource() == null)
				|| !(me.getSource() instanceof Control/* Panel */))
			return;

		// do not use scrolled.getContent().getBounds() because that is the
		// whole
		// length, not the visable one
		Rectangle rect = this.sashForm.getBounds();
		Point origin = 	((FigureCanvas)me.getSource()).getViewport().getViewLocation();

		int newX;
		int tmpMeX = me.x;

		// mouse event may return out of range X, that may
		// crash when we use it to index data array
		tmpMeX = tmpMeX >= 0 ? tmpMeX : 0;
		if (tmpMeX >= origin.x + sashForm.getBounds().width) {
			tmpMeX = origin.x + sashForm.getBounds().width - 1;
		}

		if (tmpMeX - origin.x < 20) {
			newX = tmpMeX - 20;
			if (newX < 0)
				newX = 0;
		} else {
			newX = tmpMeX + 20;
		}

		boolean changeOrigin = false;

		// Scroll each of the windows in the ScrolledComposite so that the
		// rectangle is visible
		if (newX < origin.x) {
			// scroll to the left
			this.setScrolledOrigin(newX, origin.y, null);
			changeOrigin = true;
		} else if (newX > origin.x + rect.width) {
			// scroll to the right
			this.setScrolledOrigin(newX - rect.width, origin.y, null);
			changeOrigin = true;
		}

		if (changeOrigin) {
			// since other pages associated with the GppTraceGraph scroll when
			// this page scrolls,
			// also let listening plugins know
			Enumeration enu = PluginInitialiser
					.getPluginInstances(NpiInstanceRepository.getInstance()
							.activeUid(),
							"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
			if (enu != null) {
				origin = ((FigureCanvas)me.getSource()).getViewport().getViewLocation();
				Event event = new Event();
				event.x = origin.x;
				event.y = origin.y;
				event.data = originFigureCanvas; //$NON-NLS-1$

				while (enu.hasMoreElements()) {
					IEventListener plugin = (IEventListener) enu.nextElement();
					plugin.receiveEvent("scroll", event); //$NON-NLS-1$
				}
			}
		}
	}

	public PIVisualSharedData getSharedData() {
		return this.sharedData;
	}

	public void setToolTipTextForGraphComponent(IGenericTraceGraph graph,
			String text) {
		if (this.graphComponents != null) {
			for (int i = 0; i < graphComponents.size(); i++) {
				GraphComponentWrapper wrap = graphComponents.get(i);
				if (wrap.graphComponent.equals(graph)) {
					wrap.figureCanvas.setToolTipText(text);
				}
			}
		}
	}

	/**
	 * Moves the position of all graphComponent's FigureCanvas to the given location.
	 * If an original FigureCanvas is given, it is skipped since it has already
	 * performed the operation. 
	 * @param x The new location to set to
	 * @param y
	 * @param origin If not null, the FigureCanvas to omit from the operation
	 */
	public void setScrolledOrigin(int x, int y, FigureCanvas origin) {
		if (this.graphComponents != null) {
			for (int i = 0; i < graphComponents.size(); i++) {
				FigureCanvas figureCanvas = graphComponents.get(i).figureCanvas;

				if (figureCanvas == null)
					continue;

				if ((origin == null || figureCanvas != origin) && figureCanvas.getViewport().getViewLocation().x != x){
					figureCanvas.getViewport().setViewLocation(x, y);
					if (figureCanvas.getViewport().getViewLocation().x != x) {
						// force the viewport to accept the new value
						figureCanvas.getViewport().getHorizontalRangeModel()
								.setMaximum(this.sizeX);
						figureCanvas.getViewport().setViewLocation(x, y);
					}					
				}
			}
		}
	}

	public Point getScrolledOrigin(IGenericTraceGraph graph) {
		if (this.graphComponents == null) {
			return null;
		} else {
			if (graph == null) {
				return graphComponents.get(0).figureCanvas.getViewport()
						.getViewLocation();

			} else {
				Iterator<GraphComponentWrapper> iterator = graphComponents
						.iterator();
				while (iterator.hasNext()) {
					GraphComponentWrapper gcw = iterator.next();
					if (gcw.graphComponent == graph) {
						return gcw.figureCanvas.getViewport().getViewLocation();
					}
				}
			}
		}
		return null;
	}

	public void setCurrentInfoComponent(Component currentComponent) {
		this.profVisu.vPanelRepaint(currentComponent);
	}

	private void sendEventToSubGraphs(int type, MouseEvent me) {
		FigureCanvas figureCanvas;
		GraphComponentWrapper component = null;

		if (!(me.getSource() instanceof FigureCanvas))
			return;

		figureCanvas = (FigureCanvas) me.getSource();

		// find the component whose FigureCanvas this is
		for (int i = 0; i < this.graphComponents.size(); i++)
			if (this.graphComponents.get(i).figureCanvas == figureCanvas) {
				component = this.graphComponents.get(i);
				break;
			}

		if (component == null)
			return;

		// loop through the subcomponents, finding listeners
		for (int i = 0; i < component.subGraphs.size(); i++) {
			GraphComponentWrapper subComponents = component.subGraphs.get(i);
			Object subgraphComponent = subComponents.getGraphComponent();
			if (subgraphComponent instanceof MouseListener) {
				MouseListener listener = (MouseListener) subgraphComponent;
				if (type == PICompositePanel.MOUSE_DOUBLECLICK)
					listener.mouseDoubleClick(me);
				else if (type == PICompositePanel.MOUSE_DOWN)
					listener.mouseDown(me);
			}
			if (subgraphComponent instanceof MouseMoveListener) {
				MouseMoveListener listener = (MouseMoveListener) subgraphComponent;
				if (type == PICompositePanel.MOUSE_MOVE)
					listener.mouseMove(me);
			}
		}
	}

	private void addSubGraphMenuItems(Menu menu, MouseEvent me) {
		FigureCanvas figureCanvas;
		GraphComponentWrapper component = null;

		if (!(me.getSource() instanceof FigureCanvas))
			return;

		figureCanvas = (FigureCanvas) me.getSource();

		// find the component whose FigureCanvas this is
		for (int i = 0; i < this.graphComponents.size(); i++)
			if (this.graphComponents.get(i).figureCanvas == figureCanvas) {
				component = this.graphComponents.get(i);
				break;
			}

		if (component == null)
			return;

		// find this component's context menu items
		if (component.graphComponent instanceof IContextMenu) {
			((IContextMenu) component.graphComponent).addContextMenuItems(menu,
					me);
		}

		// loop through the subcomponents, finding context menu items
		for (int i = 0; i < component.subGraphs.size(); i++) {
			GraphComponentWrapper subComponents = component.subGraphs.get(i);
			Object subgraphComponent = subComponents.getGraphComponent();
			if (subgraphComponent instanceof IContextMenu) {
				((IContextMenu) subgraphComponent)
						.addContextMenuItems(menu, me);
			}
		}

	}

	private void sendEventToSubComponents(Object event) {
		for (int i = 0; i < graphComponents.size(); i++) {
			IGenericTraceGraph component = graphComponents.get(i)
					.getGraphComponent();

			// forward the events only if the component has height
			// which means that it is not hidden, or if the component is
			// the currently active component
			if (hasToBeUpdated(component)) {
				if (event instanceof ActionEvent
						&& component instanceof ActionListener) {
					((ActionListener) component)
							.actionPerformed((ActionEvent) event);
				}
				if (event instanceof PIEvent
						&& component instanceof PIEventListener) {
					((PIEventListener) component)
							.piEventReceived((PIEvent) event);
				}
				if (event instanceof FocusEvent
						&& component instanceof FocusListener) {
					FocusEvent fe = (FocusEvent) event;
					if (fe.getID() == FocusEvent.FOCUS_GAINED) {
						((FocusListener) component).focusGained(fe);
					} else if (fe.getID() == FocusEvent.FOCUS_LOST) {
						((FocusListener) component).focusLost(fe);
					}
				}
			}
		}
	}

	private void sendEventToActiveSubComponent(Object event) {
		Object component = this.getActiveGraph();

		if (event instanceof ActionEvent && component instanceof ActionListener) {
			((ActionListener) component).actionPerformed((ActionEvent) event);
		}
		if (event instanceof PIEvent && component instanceof PIEventListener) {
			((PIEventListener) component).piEventReceived((PIEvent) event);
		}
		if (event instanceof FocusEvent && component instanceof FocusListener) {
			FocusEvent fe = (FocusEvent) event;
			if (fe.getID() == FocusEvent.FOCUS_GAINED) {
				((FocusListener) component).focusGained(fe);
			} else if (fe.getID() == FocusEvent.FOCUS_LOST) {
				((FocusListener) component).focusLost(fe);
			}
		}
	}

	public void piEventReceived(PIEvent be) {
		this.sendEventToSubComponents(be);
	}

	public void selectionChanged() {
		this.selectFrame();
		PIEvent be = new PIEvent(new double[] { selectionStart, selectionEnd },
				PIEvent.SELECTION_AREA_CHANGED);
		this.sendEventToSubComponents(be);
	}

	public void actionPerformed(ActionEvent ae) {
		this.sendEventToSubComponents(ae);
		if ((ae.getActionCommand() == "start") //$NON-NLS-1$
				|| (ae.getActionCommand() == "end")) //$NON-NLS-1$
		{
			this.selectFrame();
			PIEvent be = new PIEvent(new double[] { selectionStart,
					selectionEnd }, PIEvent.SELECTION_AREA_CHANGED);
			this.sendEventToSubComponents(be);
		}
	}

	public void focusGained(FocusEvent fe) {
	}

	public void paintComponent(PaintEvent paintEvent) {
		this.refreshSubcomponentValues();
	}

	public void repaintComponent() {
		this.refreshSubcomponentValues();
	}

	public void paintLeftLegend() {
		if (this.graphComponents != null) {
			// for each graph component set updated values
			for (int i = 0; i < graphComponents.size(); i++) {
				GraphComponentWrapper wrap = graphComponents.get(i);
				if (wrap.leftLegend != null) {
					wrap.leftLegend.redraw();
				}
			}
		}
	}

	private void refreshSubcomponentValues() {
		if (this.graphComponents != null) {
			// for each graph component set updated values
			for (int i = 0; i < graphComponents.size(); i++) {
				GraphComponentWrapper wrap = graphComponents.get(i);
				IGenericTraceGraph component = wrap.getGraphComponent();
				component.setSize(this.sizeX, this.sizeY);
				component.setScale(this.scale);
				component.setSelectionEnd(this.selectionEnd);
				component.setSelectionStart(this.selectionStart);
				if (wrap.figureCanvas != null) {
					component.setVisualSize(
							wrap.figureCanvas.getClientArea().width,
							wrap.figureCanvas.getClientArea().height);
					wrap.figureCanvas.redraw();
					wrap.leftLegend.redraw();
				}
			}
		}
	}

	public void addGraphComponent(IGenericTraceGraph component,
			Class pluginClass, GraphDrawRequest request) {
		// insert CompositePanel into wrapper and put it into top part of the
		// splitpane
		component.importParentComponent(this);

		GraphComponentWrapper wrap;

		// add the wrapper to the SashForm
		wrap = new GraphComponentWrapper(this, component, request, pluginClass
				.getName());
		this.graphComponents.add(wrap);

		FigureCanvas graphCanvas = null;


		// This treats all getGraphClassToDraw editor pages the same
		if (request == null
				|| request.getGraphClassToDraw(component.getGraphIndex()).size() == 0) {
			graphCanvas = wrap.setCanvas(component, this.sashForm,
					SWT.NONE);
		}

		// add any mouse listeners from the base class
		this.addMouseListeners(wrap, graphCanvas);
		repaintComponent();

		for (int i = 0; i < graphComponents.size(); i++) {
			wrap = graphComponents.get(i);
			wrap.newPluginAdded();
		}
	}

	public void setSelectionFields(int selectionStart, int selectionEnd) {
		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
		setSelectionFields();
	}

	private void setSelectionFields() {
		if (selectionStart == -1 || selectionEnd == -1) {
			this.profVisu.setTimeInterval(0, 0);
			this.selectionStart = -1;
			this.selectionEnd = -1;
		} else {
			double start = selectionStart / 1000;
			double end = selectionEnd / 1000;

			if ((PIPageEditor.currentPageEditor().getStartTime() != start)
					|| (PIPageEditor.currentPageEditor().getEndTime() != end)) {
				this.profVisu.setTimeInterval(start, end);
				this.profVisu.action("changeInterval"); //$NON-NLS-1$
			}
		}
	}

	public double getSelectionStart() {
		return this.selectionStart;
	}

	public double getSelectionEnd() {
		return this.selectionEnd;
	}

	/*
	 * Check the current page editor's start and end times
	 */
	public boolean validInterval() {
		if ((PIPageEditor.currentPageEditor().getStartTime() < 0.0f)
				|| (PIPageEditor.currentPageEditor().getEndTime() < 0.0f))
			return false;

		int start = (int) (PIPageEditor.currentPageEditor().getStartTime() * 1000);
		int end = (int) (PIPageEditor.currentPageEditor().getEndTime() * 1000);

		if (start > this.lastSampleX)
			start = this.lastSampleX;

		if (end > this.lastSampleX)
			end = this.lastSampleX;

		if ((start >= end) || (start < 0))
			return false;

		return true;
	}

	/*
	 * Based on the current page editor's start and end times, set this
	 * PICompositePanel's start and end times.
	 */
	private void selectFrame() {
		if ((PIPageEditor.currentPageEditor().getStartTime() < 0.0f)
				|| (PIPageEditor.currentPageEditor().getEndTime() < 0.0f))
			return;

		double start = PIPageEditor.currentPageEditor().getStartTime();
		double end = PIPageEditor.currentPageEditor().getEndTime();

		selectionStart = start * 1000;
		selectionEnd = end * 1000;

		if (selectionStart > (double) this.lastSampleX)
			selectionStart = (double) this.lastSampleX;

		if (selectionEnd > (double) this.lastSampleX)
			selectionEnd = (double) this.lastSampleX;

		if ((selectionEnd - selectionStart) <= 0.5) {
			selectionStart = -1;
			selectionEnd = -1;
		}

		if (selectionStart < 0 && selectionStart != -1) {
			selectionStart = 0;
		}

		setSelectionFields();
		repaintComponent();

		for (int i = 0; i < graphComponents.size(); i++) {
			FigureCanvas figureCanvas = graphComponents.get(i).figureCanvas;
			if (figureCanvas != null)
				figureCanvas.update();
		}
	}

	public void zoomTo(double start, double end) {
		this.scale = 10;
		this.setSizeX(false);

		// Window size
		Composite parent = NpiInstanceRepository.getInstance()
				.activeUidGetParentComposite();
		if (parent == null) {
			// no parent composite is only for temp instance used by non-GUI
			// importer
			GeneralMessages.showErrorMessage(Messages
					.getString("PICompositePanel.0")); //$NON-NLS-1$
			return;
		}

		Rectangle window = parent.getBounds();

		// it shouldn't be, but it happened
		if (window.width <= 0)
			return;

		// Set new scale
		int windowLength = window.width;
		double traceLength = end - start;
		double dFactor = ((double) traceLength / (double) windowLength) * 10;

		this.scale = dFactor;

		// Snap to next Zoom out factor unless it fits exactly
		if (this.scale != nextScale(nextScale(this.scale, true), false))
			this.scale = nextScale(this.scale, true);

		// inform all subcomponents
		PIEvent be = new PIEvent(new Double(scale), PIEvent.SCALE_CHANGED);
		this.sendEventToSubComponents(be);

		this.setSizeX(false);
		repaintComponent();
	}

	public void forceSizeX() {

	}

	public void setSizeX(boolean forceUpdate) {
		double sample = this.lastSampleX;
		int prefSize = (int) (sample / scale);

		if ((this.preferredSizeX == prefSize)
				&& (this.sizeX == this.preferredSizeX) && !forceUpdate)
			return;

		this.preferredSizeX = prefSize;
		this.sizeX = this.preferredSizeX;

		// make sure all graphs of this sashForm get the new width
		Control[] controls = this.sashForm.getChildren();

		for (int i = 0; i < controls.length; i++) {
			if (!(controls[i] instanceof GraphComposite))
				continue;

			GraphComposite composite = (GraphComposite) controls[i];
			FigureCanvas figureCanvas = composite.figureCanvas;
			Panel panel = (Panel) figureCanvas.getContents();

			panel.setPreferredSize(prefSize, this.preferredSizeY);
			if (prefSize >= figureCanvas.getClientArea().width) {
				panel.setSize(prefSize, this.preferredSizeY);
			}

		}

		this.sashForm.layout();
		this.sashForm.redraw();
	}

	public void setActive(IGenericTraceGraph graph) {
		activeGraph = graph;
	}

	public IGenericTraceGraph getActiveGraph() {
		return activeGraph;
	}

	public void setSelectionCenter() {
		if (selectionStart == -1 && selectionEnd == -1) {
			this.updateVisibleCenter();
			return;
		}
		previousScale = scale;

		Rectangle window = this.sashForm.getBounds();

		int uid = NpiInstanceRepository.getInstance().activeUid();
		if (window.width == 0) {
			// use the width from the active page
			int currentPage = PIPageEditor.currentPageEditor().getCurrentPage();
			window = NpiInstanceRepository.getInstance().getProfilePage(uid,
					currentPage).getTopComposite().getSashForm().getBounds();
		}

		// Whole trace
		double selectionLength = selectionEnd - selectionStart;
		double selectionMiddle = selectionStart + (selectionLength / 2);
		int windowLength = (int) (window.width * (this.scale / 10));
		int newPositionX = (int) ((selectionMiddle / 10) - ((double) windowLength / 2));

		newPositionX = (int) ((double) newPositionX / (this.scale / 10));

		if (newPositionX < 0) {
			newPositionX = 0;
		}

		this.setScrolledOrigin(newPositionX, 0, null);
		repaintComponent();
	}

	private void updateVisibleCenter() {
		double centerPosition = getSelection()
				+ (this.sashForm.getBounds().width / 2);

		if (scale < previousScale)
			centerPosition = centerPosition * previousScale / scale;
		else if (scale > previousScale)
			centerPosition = centerPosition * scale / previousScale;

		previousScale = scale;

		centerPosition = centerPosition - this.sashForm.getBounds().width / 2;

		setOrigin((int) centerPosition, 0);
		repaintComponent();
	}

	private int getSelection() {
		if (this.slider == null)
			return 0;
		else
			return this.slider.getSelection();
	}

	private void setOrigin(int originX, int originY) {
		// for all components in the sashForm, except the one with the slider,
		// adjust the graphs
	}

	public void performSynchronise() {
		if (dialog == null) {
			// dialog = new JFrame();
			dialog = new JDialog();
			dialog.setTitle("Syncronise"); //$NON-NLS-1$
			dialogPanel = new SynchroniseDialog(this);
			dialog.setContentPane(dialogPanel);
			dialog.setSize(200, 100);
			dialog.setLocation(50, 50);
			dialog.setResizable(false);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
		dialog.setVisible(true);
	}

	public void synchroniseOk() {
		dialog.dispose();
		String synchString = dialogPanel.getGppFileName();
		Double synchValue = new Double(synchString);
		PIEvent be = new PIEvent(synchValue, PIEvent.SYNCHRONISE);
		this.sendEventToActiveSubComponent(be);
		repaintComponent();
	}

	public void synchroniseCancel() {
		dialog.dispose();
	}

	/**
	 * using log10 to extract the first digit in decimal and make any number
	 * fall into buckets to promote/demote to scale of ..., 0.1, 0.2, 0.5, 1, 2,
	 * 5 ...
	 */
	private double nextScale(double scale, boolean bigger) {
		double logScale = Math.log10(scale);
		double floorLogScale = Math.floor(Math.log10(scale));
		double mostSignificantDigit = Math.rint(Math.pow(10,
				(logScale - floorLogScale)));
		double powerOfTen = Math.pow(10, floorLogScale);

		if (bigger) {
			if (mostSignificantDigit < 2) {
				mostSignificantDigit = 2;
			} else if (mostSignificantDigit < 5) {
				mostSignificantDigit = 5;
			} else {
				mostSignificantDigit = 10;
			}
		} else {
			if (mostSignificantDigit > 5) {
				mostSignificantDigit = 5;
			} else if (mostSignificantDigit > 2) {
				mostSignificantDigit = 2;
			} else if (mostSignificantDigit > 1) {
				mostSignificantDigit = 1;
			} else {
				mostSignificantDigit = 0.5;
			}
		}

		double result = mostSignificantDigit * powerOfTen;

		if (result < 0.1)
			result = 0.1;

		return result;
	}

	public void performZoomCommand(String zoomCommand) {
		// only apply when the currently visible page has a graph
		if (getActiveGraph() == null)
			return;

		int uid = NpiInstanceRepository.getInstance().activeUid();

		if (zoomCommand.equals("changeInterval")) { //$NON-NLS-1$
			// just use a refresh because drag select uses this too
			// we keep this update here instead of in ProfileVisualiser
			// to consolidate all screen related calls to be in this file
			getSashForm().update();
			return;
		}

		// the rest are all real zoom command that need repaint on of the image
		for (int i = 0; i < graphComponents.size(); i++) {
			GraphComponentWrapper wrap = graphComponents.get(i);
			IGenericTraceGraph component = wrap.getGraphComponent();
			component.setGraphImageChanged(true);
		}

		// zoom out
		if (zoomCommand.equals("+")) //$NON-NLS-1$
		{
			PICompositePanel visibleComposite = NpiInstanceRepository
					.getInstance().getProfilePage(uid,
							PIPageEditor.currentPageIndex()).getTopComposite();
			Composite parent = NpiInstanceRepository.getInstance()
					.activeUidGetParentComposite();
			if (parent == null) {
				// no parent composite is only for temp instance used by non-GUI
				// importer
				GeneralMessages.showErrorMessage(Messages
						.getString("PICompositePanel.1")); //$NON-NLS-1$
				return;
			}

			// bail out if we already made the whole graph visible
			if (visibleComposite.lastSampleX / scale <= parent.getBounds().width) // TODO
				// visibleComposite
				return;

			// if the scale will not change, do not redraw
			if (scale == nextScale(scale, true))
				return;

			scale = nextScale(scale, true);

			PIEvent be = new PIEvent(new Double(scale), PIEvent.SCALE_CHANGED);
			this.sendEventToSubComponents(be);

			this.setSizeX(false);
			this.setSelectionCenter();
		}
		// zoom in
		else if (zoomCommand.equals("-")) //$NON-NLS-1$
		{
			// if the scale will not change, do not redraw
			if (scale <= 0)
				return;

			if (scale == nextScale(scale, false))
				return;

			scale = nextScale(scale, false);

			PIEvent be = new PIEvent(new Double(scale), PIEvent.SCALE_CHANGED);
			this.sendEventToSubComponents(be);

			this.setSizeX(false);
			this.setSelectionCenter();
		}
		// show whole graph
		else if (zoomCommand.equals("++")) //$NON-NLS-1$
		{
			PICompositePanel visibleComposite = NpiInstanceRepository
					.getInstance().getProfilePage(uid,
							PIPageEditor.currentPageIndex()).getTopComposite();

			performZoomToGraph(visibleComposite,
					NpiInstanceRepository.getInstance()
							.activeUidGetParentComposite().getBounds().width);
		}
		// zoom to selected time interval
		else if (zoomCommand.equals("--")) //$NON-NLS-1$
		{
			selectFrame();
			if (selectionStart != -1 && selectionEnd != -1) {
				zoomTo(selectionStart / 10, selectionEnd / 10);
				setSelectionCenter();
			} else {
				GeneralMessages
						.showErrorMessage(Messages
								.getString("PICompositePanel.selectNonEmptyTimeInterval")); //$NON-NLS-1$
			}
		}
	}

	public void performZoomToGraph(PICompositePanel visibleComposite, int width) {
		// left margin is not available for painting
		int availableWidth = width - IGenericTraceGraph.Y_LEGEND_WIDTH;
		// NOTE: assumes tabs without graphs have sample == 0
		if (visibleComposite.lastSampleX <= 0)
			return;

		double new_scale = scale;

		// first zoom in until it is too big to fit
		while (visibleComposite.lastSampleX / new_scale <= availableWidth)
			new_scale = nextScale(new_scale, false);

		// now zoom out until it just fits
		while (visibleComposite.lastSampleX / new_scale > availableWidth)
			new_scale = nextScale(new_scale, true);

		if (new_scale == scale)
			return;

		scale = new_scale;

		PIEvent be = new PIEvent(new Double(scale), PIEvent.SCALE_CHANGED);
		this.sendEventToSubComponents(be);

		this.setSizeX(false);
		this.setSelectionCenter();
	}

	public class GraphComponentWrapper {
		public IGenericTraceGraph graphComponent;
		public FigureCanvas leftLegend;
		public FigureCanvas figureCanvas;
		public Panel panel = null;
		public ArrayList<GraphComponentWrapper> subGraphs;
		private GraphDrawRequest howToDraw;
		private String myPluginName;
		private PICompositePanel compositePanel;

		public GraphComponentWrapper(PICompositePanel compositePanel,
				IGenericTraceGraph graph, GraphDrawRequest howToDraw,
				String myPluginName) {
			this.compositePanel = compositePanel;
			this.graphComponent = graph;
			this.howToDraw = howToDraw;
			this.myPluginName = myPluginName;

			this.subGraphs = new ArrayList<GraphComponentWrapper>();
		}

		public FigureCanvas setCanvas(IGenericTraceGraph component,
				Composite parent, int style) {
			final IGenericTraceGraph componentFinal = component;
			final ArrayList<GraphComponentWrapper> subGraphsFinal = this.subGraphs;
			final GraphComponentWrapper wrapFinal = this;
			final PICompositePanel compositePanelFinal = this.compositePanel;

			GraphComposite composite = new GraphComposite(parent, style, component.getTitle(),
					compositePanel, component instanceof ITitleBarMenu ? (ITitleBarMenu)component : null, this);
			this.leftLegend = composite.leftLegend;
			this.figureCanvas = composite.figureCanvas;
			component.setVisibilityListener(composite);

			this.leftLegend.setBackground(ColorPalette.getColor(new RGB(255,
					255, 255)));
			final FigureCanvas leftLegendFinal = leftLegend;
			leftLegend.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					leftLegendFinal.setSize(IGenericTraceGraph.Y_LEGEND_WIDTH,
							leftLegendFinal.getSize().y);
					componentFinal.paintLeftLegend(leftLegendFinal, null);
				}
			});

			leftLegend.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					// draw only if the graph is visible
					if (compositePanelFinal.hasToBeUpdated(componentFinal))
						componentFinal.paintLeftLegend(leftLegendFinal, e.gc);
				}
			});

			this.figureCanvas.setBackground(ColorPalette.getColor(new RGB(255,
					255, 255)));

			if (this.panel == null) {
				this.panel = new Panel() {
					public void paint(Graphics graphics) {
						// draw only if the graph is visible
						if (compositePanelFinal.hasToBeUpdated(componentFinal)) {
							componentFinal.paint(this, graphics);

							for (int i = 0; i < subGraphsFinal.size(); i++) {
								GraphComponentWrapper wrap = subGraphsFinal
										.get(i);
								wrap.setVisualParametersFrom(wrapFinal);
								wrap.graphComponent.paint(panel, graphics);
							}
						}
					}
				};
			}
			panel.setLayoutManager(new FlowLayout());
			figureCanvas.setContents(panel);

			// listen for when the horizontal scroll bar is selected
			final ScrollBar horizontalBar = figureCanvas.getHorizontalBar();

			horizontalBar.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					// since other pages associated with the GppTraceGraph
					// scroll when this page scrolls,
					// also let listening plugins know
					Enumeration enu = PluginInitialiser
							.getPluginInstances(NpiInstanceRepository
									.getInstance().activeUid(),
									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
					if (enu != null) {
						Event event = new Event();
						event.x = figureCanvas.getViewport().getViewLocation().x;
						event.y = figureCanvas.getViewport().getViewLocation().y;
						event.data = figureCanvas; 

						while (enu.hasMoreElements()) {
							IEventListener plugin = (IEventListener) enu
									.nextElement();
							plugin.receiveEvent("scroll", event); //$NON-NLS-1$
						}
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			// minor scroll is 10 pixels
			horizontalBar.setIncrement(10);

			// major scroll is the width of the window
			horizontalBar.setPageIncrement(figureCanvas.getBounds().width);
			figureCanvas.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					horizontalBar
							.setPageIncrement(figureCanvas.getBounds().width);
					graphComponent.setGraphImageChanged(true);
					graphComponent.repaint();
				}
			});

			return this.figureCanvas;
		}

		public void setVisualParametersFrom(GraphComponentWrapper parent) {
			if (graphComponent == null)
				return;

			IGenericTraceGraph gtg = parent.getGraphComponent();
			this.graphComponent.setScale(gtg.getScale());
			this.graphComponent.setVisualSize(gtg.getVisualSizeX(), gtg
					.getVisualSizeY());
		}

		public void newPluginAdded()// ArrayList<GraphComponentWrapper>
		// graphComponents)
		{
			if (this.myPluginName == null)
				return;

			this.subGraphs.clear();

			for (int i = 0; i < graphComponents.size(); i++) {
				GraphComponentWrapper wrap = graphComponents.get(i);
				if (wrap.howToDraw != null) {
					// This treats all getGraphClassToDraw editor pages the same
					ArrayList e2 = wrap.howToDraw
							.getGraphClassToDraw(wrap.graphComponent.getGraphIndex());
					for (int j = 0; j < e2.size(); j++) {
						String s = (String) e2.get(j);
						if (this.myPluginName.equals(s)) {
							this.subGraphs.add(wrap);
						}
					}
				}
			}
		}

		public IGenericTraceGraph getGraphComponent() {
			return this.graphComponent;
		}
	}

	public double getScale() {
		return scale;
	}

	public ProfileVisualiser getVisualiser() {
		return this.profVisu;
	}

	public void selectWholeGraph() {

		// Get visible composite
		int uid = NpiInstanceRepository.getInstance().activeUid();
		PICompositePanel visibleComposite = NpiInstanceRepository.getInstance()
				.getProfilePage(uid, PIPageEditor.currentPageIndex())
				.getTopComposite();

		// Set selectionStart and selectionEnd
		selectionStart = 0;
		selectionEnd = visibleComposite.lastSampleX;
		setSelectionFields();

		// inform all subcomponents that the selection area has changed
		PIEvent be = new PIEvent(new double[] { selectionStart, selectionEnd },
				PIEvent.SELECTION_AREA_CHANGED);
		sendEventToSubComponents(be);
		repaintComponent();

		// since some pages that depend on the selection start and end times may
		// not have
		// graph components (e.g., the function call plugin page), also let
		// listening plugins know
		Enumeration e = PluginInitialiser
				.getPluginInstances(NpiInstanceRepository.getInstance()
						.activeUid(),
						"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
		if (e != null) {
			Event event = new Event();
			event.start = (int) selectionStart;
			event.end = (int) selectionEnd;

			while (e.hasMoreElements()) {
				IEventListener plugin = (IEventListener) e.nextElement();
				plugin.receiveEvent("changeSelection", event); //$NON-NLS-1$
			}
		}

	}


	/**
	 * Updates the enabled state of all graph actions (min, max etc.)
	 */
	public void updateGraphActionState() {
		for (final Control control : this.sashForm.getChildren()) {
			if (control instanceof GraphComposite){
				//find the first graph on the page
				//and have all action buttons (min, max etc.) updated for all graphs
				
				Runnable refreshRunnable = new Runnable() {
					public void run() {
						GraphComposite graphComposite = (GraphComposite) control;
						graphComposite.updateAllGraphsButtons();
					}
				};

				Display.getDefault().asyncExec(refreshRunnable);
				break;
			}
		}
		
	}

	/**
	 * Calls initialisation code for all graphs on this page. This
	 * method is called on editor part activation.
	 */	
	public void initialiseGraphs() {
		for (Control control : this.sashForm.getChildren()) {
			if (control instanceof GraphComposite){
				//find the first graph on the page
				//and have all action buttons (min, max etc.) updated for all graphs
				GraphComposite graphComposite = (GraphComposite) control;
				graphComposite.initialiseGraphs();
			}
		}
		
	}

}