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
* Navigator action provider
*
*/
package com.nokia.tracebuilder.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.tracebuilder.action.TraceViewActionProvider;
import com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Navigator action provider
 * 
 */
final class ListNavigatorActionProvider implements TraceViewActionProvider {

	/**
	 * Action list
	 */
	private ArrayList<IAction> actions = new ArrayList<IAction>();

	/**
	 * The list to be navigated
	 */
	private ListWrapper listWrapper;

	/**
	 * Next action
	 */
	private Action nextAction;

	/**
	 * Previous action
	 */
	private Action previousAction;

	/**
	 * View reference
	 */
	private Object viewReference;

	/**
	 * Constructor
	 * 
	 * @param list
	 *            the list wrapper to be navigated
	 */
	ListNavigatorActionProvider(ListWrapper list) {
		this.listWrapper = list;
		nextAction = new Action(Messages
				.getString("ListNavigatorActionProvider.Next")) { //$NON-NLS-1$
			@Override
			public void run() {
				listWrapper.showNext();
			}
		};
		previousAction = new Action(Messages
				.getString("ListNavigatorActionProvider.Previous")) { //$NON-NLS-1$
			@Override
			public void run() {
				listWrapper.showPrevious();
			}
		};
		actions.add(previousAction);
		actions.add(nextAction);
		nextAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_FORWARD));
		previousAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_BACK));
		nextAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_FORWARD_DISABLED));
		previousAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_BACK_DISABLED));
		// The list is initially at the end
		nextAction.setEnabled(false);
	}

	/**
	 * Enables / disables the previous action
	 * 
	 * @param flag
	 *            the flag value
	 */
	void setPreviousEnabled(boolean flag) {
		previousAction.setEnabled(flag);
	}

	/**
	 * Enables / disables the next action
	 * 
	 * @param flag
	 *            the flag value
	 */
	void setNextEnabled(boolean flag) {
		nextAction.setEnabled(flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewActionProvider#getActions()
	 */
	public Iterator<IAction> getActions() {
		return actions.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hideWhenEmpty()
	 */
	public boolean hideWhenEmpty() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getChildren()
	 */
	public Iterator<?> getChildren() {
		List<Object> list = Collections.emptyList();
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hasChildren()
	 */
	public boolean hasChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      addUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void addUpdateListener(TraceModelExtensionUpdateListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      removeUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void removeUpdateListener(TraceModelExtensionUpdateListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getViewReference()
	 */
	public Object getViewReference() {
		return viewReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#setViewReference(java.lang.Object)
	 */
	public void setViewReference(Object reference) {
		this.viewReference = reference;
	}

}
