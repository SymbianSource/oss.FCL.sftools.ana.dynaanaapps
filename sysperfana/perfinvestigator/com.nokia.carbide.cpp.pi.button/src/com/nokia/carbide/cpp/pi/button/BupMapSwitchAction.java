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

package com.nokia.carbide.cpp.pi.button;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class BupMapSwitchAction implements IObjectActionDelegate {
	
	// IDs defined in plugin.xml
	public static final String BUP_MAP_SWITCH_POP_UP_ID = ButtonPlugin.PLUGIN_ID + ".BupMapSwitchAction"; //$NON-NLS-1$
	
	// private members
	private ISelection selection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction arg0) {
		if (arg0.getId().equals(BUP_MAP_SWITCH_POP_UP_ID)){
			handleBupMapSwitch();
		}
	}
		
	private void handleBupMapSwitch() {
		if (selection != null && selection instanceof IStructuredSelection) {
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object selectedItem = iter.next();
				IResource selectedResource = getResourceFromObject(selectedItem);
				if (selectedResource != null && selectedResource instanceof IFile) {
					IEditorPart selectedEditorPart = ResourceUtil.findEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) selectedResource);
					if (selectedEditorPart != null) {
						if (selectedEditorPart instanceof PIPageEditor) {
							// found an opened editor instance
							PIPageEditor selectedPIPageEditor = (PIPageEditor) selectedEditorPart;
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(selectedPIPageEditor);
							ButtonPlugin.getDefault().switchMap();
						} else {
							GeneralMessages.showErrorMessage(Messages.getString("BupMapSwitchAction.notPIPageeditor")); //$NON-NLS-1$
						}
					} else {
						// NPI file is not opened
						try {
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) selectedResource);
							// found an opened editor instance
							PIPageEditor selectedPIPageEditor = (PIPageEditor) selectedEditorPart;
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(selectedPIPageEditor);
							ButtonPlugin.getDefault().switchMap();
						} catch (PartInitException e) {
						}
					}
				}
			}
		}

	}
	
	/**
	 * Return the resource associated with an object.
	 * @param element - object in question
	 * @return associated resource if there is one
	 */
	private IResource getResourceFromObject(Object element) {
		if (element instanceof IResource) {
			return (IResource)element;
		} else if (element instanceof IAdaptable) {
			return (IResource)((IAdaptable)element).getAdapter(IResource.class);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
		this.selection = arg1;
	}

}
