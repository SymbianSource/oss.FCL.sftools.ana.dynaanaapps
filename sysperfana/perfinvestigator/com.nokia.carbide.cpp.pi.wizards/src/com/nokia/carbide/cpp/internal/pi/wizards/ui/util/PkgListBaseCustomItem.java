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

package com.nokia.carbide.cpp.internal.pi.wizards.ui.util;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PkgListBaseCustomItem {
	private PkgListTreeViewer viewer;
	private Object element;
	private TreeItem treeItem;
	private TreeEditor editor = null;
	// hide button because Button.setSelection doesn't fire event
	// but we need to catch the selection to fire event for the viewer
	// , we can't subclass Button either... so we wrap button around and
	// provide only limited access, so we can intercept setSelection()
	private Button button = null;
	
	PkgListBaseCustomItem(PkgListTreeViewer myViewer, final Object myElement, TreeItem myTreeItem) {
		viewer = myViewer;
		element = myElement;
		treeItem = myTreeItem;
	}
	
	public void createButton(int buttonSytle, SelectionListener listener) {
		Tree tree = viewer.getTree();
		button = new Button(tree, buttonSytle);
		editor = new TreeEditor(tree);
		button.pack();
		button.setBackground(tree.getBackground());
		button.setText(((ILabelProvider) viewer.getLabelProvider()).getText(element));
		button.addSelectionListener(listener);
		editor.grabHorizontal = true;
		editor.setEditor(button, treeItem);
		editor.minimumWidth = button.getSize().x;
		editor.horizontalAlignment = SWT.LEFT;		
	}

	public boolean getSelection() {
		return button.getSelection();
	}
	
	public void setSelection(boolean selection) {
		button.setSelection(selection);
	}
	
	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}
	
	public boolean getEnabled() {
		return button.getEnabled();
	}
	
	public void dispose() {
		if (editor != null) {
			editor.dispose();
		}
		if (button != null) {
			button.dispose();
		}
	}
	
	public TreeItem getTreeItem() {
		return treeItem;
	}
}
