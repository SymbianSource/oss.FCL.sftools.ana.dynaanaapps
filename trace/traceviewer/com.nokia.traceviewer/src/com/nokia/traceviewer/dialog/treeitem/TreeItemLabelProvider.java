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
 * TreeItem Label Provider
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.nokia.traceviewer.TraceViewerPlugin;

/**
 * TreeItem Label Provider
 * 
 */
public class TreeItemLabelProvider implements ILabelProvider {

	/**
	 * Group image
	 */
	private final Image groupImage;

	/**
	 * Tree item image
	 */
	private final Image treeItemImage;

	/**
	 * Tree Component item image
	 */
	private final Image treeComponentItemImage;

	/**
	 * Stop trigger tree item image
	 */
	private final Image stopTriggerItemImage;

	/**
	 * Activation trigger tree item image
	 */
	private final Image activationTriggerItemImage;

	/**
	 * Constructor
	 */
	public TreeItemLabelProvider() {
		// Create group icon
		URL url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/folder.gif"); //$NON-NLS-1$
		groupImage = ImageDescriptor.createFromURL(url).createImage();

		// Create tree item icon
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/listitem.gif"); //$NON-NLS-1$
		treeItemImage = ImageDescriptor.createFromURL(url).createImage();

		// Create tree Component item icon
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/listcomponentitem.gif"); //$NON-NLS-1$
		treeComponentItemImage = ImageDescriptor.createFromURL(url)
				.createImage();

		// Create stop trigger item icon
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/stoptrigger.gif"); //$NON-NLS-1$
		stopTriggerItemImage = ImageDescriptor.createFromURL(url).createImage();

		// Create activation trigger item icon
		url = TraceViewerPlugin.getDefault().getBundle().getEntry(
				"/icons/activationtrigger.gif"); //$NON-NLS-1$
		activationTriggerItemImage = ImageDescriptor.createFromURL(url)
				.createImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image image;
		if (((TreeItem) element).isGroup()) {
			image = groupImage;
		} else {
			// Stop trigger image
			if (element instanceof TriggerTreeItem
					&& ((TriggerTreeItem) element).getType() == TriggerTreeItem.Type.STOPTRIGGER) {
				image = stopTriggerItemImage;

				// Activation trigger image
			} else if (element instanceof TriggerTreeItem
					&& ((TriggerTreeItem) element).getType() == TriggerTreeItem.Type.ACTIVATIONTRIGGER) {
				image = activationTriggerItemImage;

				// Component rule tree images
			} else if (componentRuleSelected(element)) {
				image = treeComponentItemImage;

				// Text rule tree images
			} else {
				image = treeItemImage;
			}
		}
		return image;
	}

	/**
	 * Checks if the element is component rule
	 * 
	 * @param element
	 *            the element to check
	 * @return true if the element is component rule
	 */
	private boolean componentRuleSelected(Object element) {
		boolean isComponent = false;
		if (element instanceof ColorTreeComponentItem
				|| element instanceof FilterTreeComponentItem
				|| element instanceof LineCountTreeComponentItem) {
			isComponent = true;
		}
		return isComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String text = ((TreeItem) element).getName();
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.
	 * jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// Dispose images
		if (groupImage != null) {
			groupImage.dispose();
		}
		if (treeItemImage != null) {
			treeItemImage.dispose();
		}
		if (stopTriggerItemImage != null) {
			stopTriggerItemImage.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang
	 * .Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
	 * .jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {

	}

}
